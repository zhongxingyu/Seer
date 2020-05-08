 package edu.ucla.nesl.datacollector;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Looper;
 import android.util.Log;
 import edu.ucla.nesl.datacollector.service.DataService;
 import edu.ucla.nesl.datacollector.service.SyncService;
 import edu.ucla.nesl.datacollector.tools.ZipUtils;
 import edu.ucla.nesl.flowengine.SensorType;
 
 public class DataArchive {
 
 	public static final String archiveDir = "data_archive";
 
 	private static final String ONGOING_PREFIX = "ongoing_";
 	public static final String READY_FILE_EXT = ".ready";
 	public static final String ZIP_FILE_EXT = ".zip";
 
	private static final long FILE_SIZE_LIMIT = 1024 * 1024 * 1024 * 5; // bytes
 	private static final long FILE_AGE_LIMIT = 1000 * 60 * 10; // ms
 	//private static final long FILE_AGE_LIMIT = 1000 * 10; // ms
 	
 	private static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
 	private static final String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
 
 	private static final int INIT_STRING_CAPACITY = 1024;
 
 	private String extPath = Environment.getExternalStorageDirectory().getPath() + "/" + archiveDir;
 	private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
 	private SimpleDateFormat sqlDateFormat = new SimpleDateFormat(SQL_DATE_FORMAT);
 
 	private Map<String, File> fileMap = new HashMap<String, File>();
 	private Map<String, Writer> writerMap = new HashMap<String, Writer>();
 
 	private DataArchiveHandler mHandler; 
 	private Looper mLooper;
 
 	private Context context;
 
 	private StringBuilder mStringBuilder;
 	private char[] mCharBuffer;
 
 	final class DataArchiveHandler extends Handler {
 		public DataArchiveHandler(Looper looper) {
 			super(looper);
 		}
 	}
 
 	public DataArchive(Context context) {
 		this.context = context;
 
 		HandlerThread thread = new HandlerThread(this.getClass().getName());
 		thread.start();
 		mLooper = thread.getLooper();
 		mHandler = new DataArchiveHandler(mLooper);
 		mStringBuilder = new StringBuilder(INIT_STRING_CAPACITY);
 		mCharBuffer = new char[INIT_STRING_CAPACITY];
 
 		if (!isExternalStorageWritable()) {
 			return;
 		}
 
 		File file = new File(extPath);
 		if (!file.exists()) {
 			file.mkdirs();
 		} else if (!file.isDirectory()) {
 			file.delete();
 			file.mkdirs();
 		}
 
 		loadDataFiles();
 	}
 
 	class OngoingCsvFileFilter implements FileFilter {
 		private static final String REGEX_PREFIX = "^" + ONGOING_PREFIX;
 		private static final String REGEX_POSTFIX = ".*\\.csv$";
 		private final String regex;
 		
 		public OngoingCsvFileFilter() {
 			regex = REGEX_PREFIX + REGEX_POSTFIX;
 		}
 		
 		public OngoingCsvFileFilter(String sensor) {
 			regex = REGEX_PREFIX + sensor + REGEX_POSTFIX;
 		}
 		
 		@Override
 		public boolean accept(File file) {
 			if (file.getName().matches(regex)) {
 				return true;
 			}
 			return false;
 		}
 	}
 
 	private void loadDataFiles() {
 
 		// Load ongoing files.
 		File dir = new File(extPath);
 		File[] files = dir.listFiles(new OngoingCsvFileFilter());
 		Arrays.sort(files, Collections.reverseOrder());
 		for (File csvFile : files) {
 			String name = csvFile.getName().split("_")[1];
 			if (fileMap.get(name) != null) {
 				continue;
 			} else {
 				fileMap.put(name, csvFile);
 				try {
 					writerMap.put(name, new BufferedWriter(new FileWriter(csvFile.getAbsolutePath(), true)));
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		// Check if ongoing files are too old or too big
 		long timestamp = System.currentTimeMillis();
 		List<String> toDeleteNames = new ArrayList<String>();
 		for (Map.Entry<String, File> entry : fileMap.entrySet()) {
 			String name = entry.getKey();
 			File csvFile = entry.getValue();
 			try {
 				if (isFileTooOld(csvFile, timestamp) || csvFile.length() > FILE_SIZE_LIMIT) {
 					Writer writer = writerMap.get(name);
 					try {
 						writer.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 					// Rename the file
 					File renamedFile = new File(csvFile.getAbsolutePath().replace(ONGOING_PREFIX, ""));
 					csvFile.renameTo(renamedFile);
 
 					// Start zipping.
 					mHandler.post(new ZipRunnable(renamedFile.getAbsolutePath()));
 					
 					toDeleteNames.add(name);
 				}
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		for (String name : toDeleteNames) {
 			fileMap.remove(name);
 			writerMap.remove(name);
 		}
 	}
 
 	public void close() {
 		mLooper.quit();
 
 		for (Writer writer : writerMap.values()) {
 			try {
 				writer.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void storeData(Bundle bundle) {
 
 		if (!isExternalStorageWritable()) {
 			return;
 		}
 
 		String name = bundle.getString(DataService.BUNDLE_NAME);
 		long timestamp = bundle.getLong(DataService.BUNDLE_TIMESTAMP);
 
 		Writer bw = getWriterForData(name, timestamp);
 
 		if (bw == null) {
 			return;
 		}
 
 		StringBuilder sb = convertBundleDataToString(bundle); 
 
 		if (sb == null) {
 			return;
 		}
 
 		if (mCharBuffer.length != sb.capacity()) {
 			Log.e(Const.TAG, "buffer length mismatch: " + mCharBuffer.length + ", " + sb.capacity());
 			mCharBuffer = new char[sb.capacity()];
 		}
 
 		int length = sb.length();
 		sb.getChars(0, length, mCharBuffer, 0);
 
 		try {
 			bw.write(mCharBuffer, 0, length);
 			bw.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private StringBuilder convertBundleDataToString(Bundle bundle) {
 		String name = bundle.getString(DataService.BUNDLE_NAME);
 		long timestamp = bundle.getLong(DataService.BUNDLE_TIMESTAMP);
 		String timeStr = sqlDateFormat.format(new Date(timestamp));
 		String type = bundle.getString(DataService.BUNDLE_TYPE);
 
 		mStringBuilder.setLength(0);
 		if (name.equals(SensorType.PHONE_ACCELEROMETER_NAME)) {
 			double[] data = bundle.getDoubleArray(DataService.BUNDLE_DATA);
 			mStringBuilder.append(timeStr);
 			for (double v : data) {
 				mStringBuilder.append(", ").append(v);
 			}
 			mStringBuilder.append("\n");
 			return mStringBuilder;
 		} else if (type.equals("String")) {
 			String data = bundle.getString(DataService.BUNDLE_DATA);
 			mStringBuilder.append(timeStr).append(", ").append(data).append("\n");
 			return mStringBuilder;
 		} else if (type.equals("int")) {
 			int data = bundle.getInt(DataService.BUNDLE_DATA);
 			mStringBuilder.append(timeStr).append(", ").append(data).append("\n");
 			return mStringBuilder;
 		} else if (type.equals("double")) {
 			double data = bundle.getDouble(DataService.BUNDLE_DATA);
 			mStringBuilder.append(timeStr).append(", ").append(data).append("\n");
 			return mStringBuilder;
 		} else if (type.equals("double[]")) {
 			double[] data = bundle.getDoubleArray(DataService.BUNDLE_DATA);
 			long ts = timestamp;
 			int sampleInterval = determineSampleInterval(name);
 			for (double v : data) {
 				mStringBuilder.append(sqlDateFormat.format(new Date(ts))).append(", ").append(v).append("\n");
 				ts += sampleInterval;
 			}
 			return mStringBuilder;
 		} else if (type.equals("int[]")) {
 			int[] data = bundle.getIntArray(DataService.BUNDLE_DATA);
 			long ts = timestamp;
 			int sampleInterval = determineSampleInterval(name);
 			for (int v : data) {
 				mStringBuilder.append(sqlDateFormat.format(new Date(ts))).append(", ").append(v).append("\n");
 				ts += sampleInterval;
 			}
 			return mStringBuilder;
 		} else {
 			Log.e(Const.TAG, "Unknown type: " + type);
 		}
 
 		return null;
 	}
 
 	private int determineSampleInterval(String name) {
 		if (name.equals(SensorType.ECG_NAME)) {
 			return SensorType.ECG_SAMPLE_INTERVAL;
 		} else if (name.equals(SensorType.RIP_NAME)) {
 			return SensorType.RIP_SAMPLE_INTERVAL;
 		} 
 		return 1;
 	}
 
 	private Writer getWriterForData(String name, long timestamp) {
 
 		File file = fileMap.get(name);
 
 		if (file != null) {
 			try {
 				if (file.length() > FILE_SIZE_LIMIT || isFileTooOld(file, timestamp)) {
 					Writer writer = writerMap.get(name);
 					try {
 						writer.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 					// Rename the file
 					File renamedFile = new File(file.getAbsolutePath().replace(ONGOING_PREFIX, ""));
 					file.renameTo(renamedFile);
 
 					// Start zipping.
 					mHandler.post(new ZipRunnable(renamedFile.getAbsolutePath()));
 
 					return createNewFile(name, timestamp);
 				} else {
 					return writerMap.get(name);
 				}
 			} catch (ParseException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 
 		return createNewFile(name, timestamp);
 	}
 
 	final class ZipRunnable implements Runnable {
 
 		private String path;
 
 		public ZipRunnable(String path) {
 			this.path = path;
 		}
 
 		@Override
 		public void run() {
 			File file = new File(path);
 
 			if (!file.exists()) {
 				return;
 			}
 
 			if (file.length() <= 0) {
 				file.delete();
 			} else {
 				try {
 					String zipFileName = path + ZIP_FILE_EXT;
 					ZipUtils.zip(new String[] { path }, zipFileName);
 					file.delete();
 
 					// Rename to mark it as ready.
 					File oriZipFile = new File(zipFileName);
 					File readyZipFile = new File(path + READY_FILE_EXT + ZIP_FILE_EXT);
 					oriZipFile.renameTo(readyZipFile);
 
 					// Notify SyncService
 					Intent intent = new Intent(context, SyncService.class);
 					context.startService(intent);
 
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private Writer createNewFile(String name, long timestamp) {
 		String dateString = dateFormat.format(new Date(timestamp));
 		File newFile = new File(extPath + "/" + ONGOING_PREFIX + name + "_" + dateString + ".csv");
 		Writer writer;
 		try {
 			if (!newFile.exists()) {
 				newFile.createNewFile();
 			}
 			writer = new BufferedWriter(new FileWriter(newFile.getAbsoluteFile(), true));
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		fileMap.put(name, newFile);		
 		writerMap.put(name, writer);
 		return writer;
 	}
 
 	private boolean isFileTooOld(File file, long timestamp) throws ParseException {
 		// ongoing_sensorname_2013-01-01-11-11-11-123.csv
 		String time = file.getName().replace(".csv", "").split("_")[2];
 		Date date = dateFormat.parse(time);
 		long epoch = date.getTime();
 		if (timestamp - epoch > FILE_AGE_LIMIT) {
 			return true; 
 		}
 		return false;
 	}
 
 	private boolean isExternalStorageWritable() {
 		String state = Environment.getExternalStorageState();
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			return true;
 		}
 		return false;
 	}
 
 	public void finish(String sensor) {
 		File dir = new File(extPath);
 		File[] files = dir.listFiles(new OngoingCsvFileFilter(sensor));
 		Arrays.sort(files);
 		for (File csvFile : files) {
 			String name = csvFile.getName().split("_")[1];
 			if (fileMap.get(name) != null) {
 				fileMap.remove(name);
 			}
 			if (writerMap.get(name) != null) {
 				try {
 					writerMap.get(name).close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				writerMap.remove(name);
 			}
 
 			// Rename the file
 			File renamedFile = new File(csvFile.getAbsolutePath().replace(ONGOING_PREFIX, ""));
 			csvFile.renameTo(renamedFile);
 
 			// Start zipping.
 			mHandler.post(new ZipRunnable(renamedFile.getAbsolutePath()));
 		}
 	}
 }
