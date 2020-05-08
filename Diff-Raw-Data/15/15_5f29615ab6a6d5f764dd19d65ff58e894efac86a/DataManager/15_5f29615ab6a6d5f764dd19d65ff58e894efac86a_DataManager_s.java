 package ceu.marten.model.io;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import ceu.marten.bplux.R;
 import ceu.marten.model.DeviceConfiguration;
 
 import plux.android.bioplux.Device.Frame;
 import android.content.Context;
 import android.os.Environment;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 
 
 /**
  * Saves and compresses recording data into android' external file system
  * @author Carlos Marten
  *
  */
 public class DataManager {
 	
 	// Standard debug constant
 	private static final String TAG = DataManager.class.getName();
 	
 	public static final int MSG_PERCENTAGE = 99;
 	private Messenger client = null;
 	
 	private DeviceConfiguration configuration;
 	private OutputStreamWriter outStreamWriter;
 	private BufferedWriter bufferedWriter;
 	
 	private int numberOfChannelsActivated;
 	private int frameCounter = 0;
 	
 	private String channelFormat = "%-4s ";
 	private String recordingName;
 	private String duration;
 	
 	private Context context;
 	
 	/**
 	 * Constructor. Initializes the number of channels activated, the outStream
 	 * write and the Buffered writer
 	 * 
 	 * @param serviceContext
 	 * @param _recordingName
 	 * @param _configuration
 	 */
 	public DataManager(Context serviceContext, String _recordingName, DeviceConfiguration _configuration) {
 		this.context = serviceContext;
 		this.recordingName = _recordingName;
 		this.configuration = _configuration;
 		
 		this.numberOfChannelsActivated = configuration.getActiveChannelsNumber();
 		try {
			outStreamWriter = new OutputStreamWriter(context.openFileOutput("tmp.txt", Context.MODE_APPEND));//TODO HARD CODED
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "file to write frames on, not found", e);
 			//TODO NOT INFORMING THE USER
 		}
 		bufferedWriter = new BufferedWriter(outStreamWriter);
 	}
 	
 	/**
 	 * Writes a frame (row) on text file that will go after the header. Returns
 	 * true if wrote successfully and false otherwise.
 	 * 
 	 * @param frame
 	 * @return boolean
 	 */
 	public boolean writeFramesToTmpFile(Frame frame) {
 		frameCounter ++;
 
 		try {
 			// WRITE THE FIRST COLUMN (THE FRAME COUNTER)
 			bufferedWriter.write(String.format(channelFormat, frameCounter));
 			// WRITE THE DATA OF ACTIVE CHANNELS ONLY
 			for(int i=0; i< numberOfChannelsActivated;i++){
 				bufferedWriter.write(String.format(channelFormat, frame.an_in[i]));
 			}
 			// WRITE A NEW LINE
 			bufferedWriter.write("\n");
 			
 		} catch (IOException e) {
 			try {bufferedWriter.close();} catch (IOException e1) {}
 			Log.e(TAG, "Exception while writing frame row", e);
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Returns true if compressed successfully and false otherwise.
 	 * @return boolean
 	 */
 	private boolean compressFile(Messenger client){
 		this.client = client;
 		BufferedInputStream origin = null;
 		ZipOutputStream out = null;
 		try {
 			String zipFileName = recordingName + ".zip";//TODO HARD CODED
 			String fileName = recordingName + ".txt";//TODO HARD CODED
 			String appDirectory = Environment.getExternalStorageDirectory().toString()+"/Bioplux/";//TODO HARD CODED
 			File root = new File(appDirectory);
 			root.mkdirs();
 			int BUFFER = 10000;//TODO HARD CODED best buffer size?
 			
 			FileOutputStream dest = new FileOutputStream(root +"/"+ zipFileName);//TODO HARD CODED
 					
 			out = new ZipOutputStream(new BufferedOutputStream(dest));
 			byte data[] = new byte[BUFFER];
 
 			FileInputStream fi = new FileInputStream(context.getFilesDir() + "/" + fileName);//TODO HARD CODED
 			origin = new BufferedInputStream(fi, BUFFER);
 			
 			ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1));//TODO HARD CODED
 			out.putNextEntry(entry);
 			int count;
 			
 			Long recordingSize = (new File(context.getFilesDir() + "/" + fileName)).length();
 			long currentBitsCompressed = 0;
 			while ((count = origin.read(data, 0, BUFFER)) != -1) {
 				out.write(data, 0, count);
 				currentBitsCompressed += BUFFER;
 				sendPercentageToActivity((int)( currentBitsCompressed * 100 / recordingSize));
 			}
 			context.deleteFile(recordingName + ".txt");//TODO HARD CODED
 
 		} catch (Exception e) {
 			Log.e(TAG, "Exception while zipping", e);
 			return false;
 		}
 		finally{
 			try {
 				origin.close();
 				out.close();
 			} catch (IOException e) {
 				try {out.close();} catch (IOException e1) {}
 				Log.e(TAG, "Exception while closing streams", e);
 				return false;
 			}	
 		}
 		return true;
 	}
 	
 	private void sendPercentageToActivity(int percentage) {
 		try {
 			this.client.send(Message.obtain(null, MSG_PERCENTAGE, percentage, 0));
 		} catch (RemoteException e) {
 			Log.e(TAG, "Exception sending percentage message to activity", e);
 		}
 	}
 	
 	/**
 	 * Returns true if text file was written successfully and false if an exception was caught
 	 * @return boolean
 	 */
 	private boolean writeTextFile() {
 
 		DateFormat dateFormat = DateFormat.getDateTimeInstance();		
 		Date date = new Date();
 		OutputStreamWriter out = null;
 		BufferedInputStream origin = null;
 		BufferedOutputStream dest = null;
 		FileInputStream fi = null;
 		
 		try {
 			out = new OutputStreamWriter(context.openFileOutput(recordingName + ".txt", Context.MODE_PRIVATE));
 			out.write(String.format("%-10s %-10s%n",   "# " + context.getString(R.string.bs_header_name), configuration.getName()));
 			out.write(String.format("%-10s %-14s%n",   "# " + context.getString(R.string.bs_header_date), dateFormat.format(date)));
 			out.write(String.format("%-10s %-4s%n",    "# " + context.getString(R.string.bs_header_frequency), configuration.getReceptionFrequency() + " Hz"));
 			out.write(String.format("%-10s %-10s%n",   "# " + context.getString(R.string.bs_header_bits), configuration.getNumberOfBits() + " bits"));
 			out.write(String.format("%-10s %-14s%n",   "# " + context.getString(R.string.bs_header_duration), duration + " " + context.getString(R.string.bs_header_seconds)));
 			out.write(String.format("%-10s %-14s%n%n", "# " + context.getString(R.string.bs_header_active_channels), configuration.getActiveChannels().toString()));
 			out.write("#num ");
 			
 			for(int i: configuration.getActiveChannels())
 				out.write("ch " + i + " ");
 			
 			out.write("\n");
 			out.flush();
 			out.close();
 
 			// APPEND DATA
 			FileOutputStream outBytes = new FileOutputStream(context.getFilesDir()
 					+ "/" + recordingName + ".txt", true);//TODO HARD CODED
 			dest = new BufferedOutputStream(outBytes);
 			fi = new FileInputStream(context.getFilesDir() + "/"//TODO HARD CODED
 					+ "tmp.txt");//TODO HARD CODED
 			 
 			origin = new BufferedInputStream(fi, 1000);//TODO HARD CODED best 1000?
 			int count;
 			byte data[] = new byte[1000];//TODO HARD CODED
 			while ((count = origin.read(data, 0, 1000)) != -1) {//TODO HARD CODED
 				dest.write(data, 0, count);
 			}
 
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "File to write header on, not found", e);
 			return false;
 		} catch (IOException e) {
 			Log.e(TAG, "Write header stream exception", e);
 			return false;
 		}
 		finally{
 			try {
 				fi.close();
 				out.close();
 				origin.close();
 				dest.close();
 				context.deleteFile("tmp.txt");//TODO HARD CODED
 			} catch (IOException e) {
 				try {out.close();} catch (IOException e1) {}
 				try {origin.close();} catch (IOException e1) {}
 				try {dest.close();} catch (IOException e1) {};
 				Log.e(TAG, "Closing streams exception", e);
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Returns true if writers were closed properly. False if an exception was
 	 * caught closing them
 	 * 
 	 * @return boolean
 	 */
 	public boolean closeWriters(){
 		try {
 			bufferedWriter.flush();
 			bufferedWriter.close();
 			outStreamWriter.close();
 		} catch (IOException e) {
 			try {bufferedWriter.close();} catch (IOException e1) {}
 			try {outStreamWriter.close();} catch (IOException e2) {}
 			Log.e(TAG, "Exception while closing Writers", e);
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Saves and compress a recording. Returns true if the writing and the
 	 * compression were successful or false if either one of them failed
 	 * 
 	 * @return boolean
 	 */
 	public boolean saveAndCompressFile(Messenger client){
 		if(!writeTextFile())
 			return false;
 		if(!compressFile(client))
 			return false;
 		return true;
 	}
 
 	/**
 	 * sets the duration of the recording
 	 * @param _duration
 	 */
 	public void setDuration(String _duration) {
 		this.duration = _duration;
 	}
 
 }
