 package Common;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import PhotoListener.CameraObserver;
 import android.content.Context;
 import android.os.Environment;
 import android.util.Log;
 
 import com.drew.imaging.ImageProcessingException;
 import com.summaphoto.ScheduledModeService;
 import com.summaphoto.SettingsActivity;
 
 public class Tester {
 
 	public static void SmartWithMapTest() {
 		insertFilesToObservedDir(1, 1);
 	}
 	
 	public static void SmartWithBlocksTest() {
 		insertFilesToObservedDir(1, 2);
 	}
 	
 	public static void ScheduledWithMapTest(Context context, int hour, int min) {
 		insertFilesToObservedDir(2, 1);
 		ScheduledModeService.startScheduledMode(context, hour, min);
 	}
 	
 	public static void ScheduledWithBlocksTest(int hour, int min) {
 		insertFilesToObservedDir(2, 2);
 	}
 	
 	private static void insertFilesToObservedDir(int mode, int collage_type) {
 
 		Log.d("Tester", "Test started");
 		File ROOT = new File(Environment.getExternalStorageDirectory(), "DCIM");
 
 		SettingsActivity.MODE = mode;
 		SettingsActivity.COLLAGE_TYPE =collage_type;
 
 		File dest = new File(Constants.ROOT, "Watched");
 		if (!dest.exists()) {
 			dest.mkdirs();
 		}
 
 //		File source = new File(SettingsActivity.ROOT, "Tests5");
 		File source = new File(Constants.ROOT, "tom");
 
 
 		File[] files = source.listFiles();
 		Photo tempPhoto;
 		List<Photo> photos = new LinkedList<Photo>();
 		for (File file : files) {
 			try {
 				tempPhoto =Utils.createPhotoFromFile(file.getAbsolutePath()); 
 				if (tempPhoto != null)
 					photos.add(tempPhoto);
 			} catch (ImageProcessingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		Collections.sort(photos, new Comparator<Photo>() {
 
 			@Override
 			public int compare(Photo lhs, Photo rhs) {
 				return lhs.compareTo(rhs);
 			}
 		});
 
 		List<File> sortedFileList = new LinkedList<File>();
 		for (Photo photo : photos) {
 			sortedFileList.add(new File(photo.getFilePath()));
 		}
 
 		//photos added by the order that they were taken in
 		for (File file : sortedFileList) {
 			try {
 				copyFile(file, new File(dest, file.getName()));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 
 	public static void omriInsertFilesToObservedDir() {
 
 		File ROOT = new File(Environment.getExternalStorageDirectory(), "DCIM");
 		String  PHOTO_DIR = ROOT + File.separator + "Watched" + File.separator;
 
 		CameraObserver observer = new CameraObserver(PHOTO_DIR); // observer over the gallery directory
 		observer.startWatching();
 
		SettingsActivity.MODE = 1;
 		SettingsActivity.COLLAGE_TYPE = 1;
 
 		File dest = new File(Constants.ROOT, "Watched");
 		if (!dest.exists()) {
 			dest.mkdirs();
 		}
 		File source = new File(Constants.ROOT, "fuck");
 
 		File[] files = source.listFiles();
 		List<Photo> photos = new LinkedList<Photo>();
 		Photo tempPhoto;
 		for (File file : files) {
 			try {
 				tempPhoto = Utils.createPhotoFromFile(file.getAbsolutePath());
 				if (tempPhoto != null)
 					photos.add(tempPhoto);
 			} catch (ImageProcessingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		Collections.sort(photos, new Comparator<Photo>() {
 
 			@Override
 			public int compare(Photo lhs, Photo rhs) {
 				return lhs.compareTo(rhs);
 			}
 		});
 
 		List<File> sortedFileList = new LinkedList<File>();
 		for (Photo photo : photos) {
 			sortedFileList.add(new File(photo.getFilePath()));
 		}
 
 		//photos added by the order that they were taken in
 		for (File file : sortedFileList) {
 			try {
 				copyFile(file, new File(dest, file.getName()));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	@SuppressWarnings("resource")
 	private static void copyFile(File sourceFile, File destFile) throws IOException {
 		if (!sourceFile.exists()) {
 			return;
 		}
 		if (destFile.exists()) {
 			destFile.delete();
 		}
 		destFile.createNewFile();
 
 		FileChannel source = null;
 		FileChannel destination = null;
 		source = new FileInputStream(sourceFile).getChannel();
 		destination = new FileOutputStream(destFile).getChannel();
 		if (destination != null && source != null) {
 			destination.transferFrom(source, 0, source.size());
 		}
 		if (source != null) {
 			source.close();
 		}
 		if (destination != null) {
 			destination.close();
 		}
 	}
 }
