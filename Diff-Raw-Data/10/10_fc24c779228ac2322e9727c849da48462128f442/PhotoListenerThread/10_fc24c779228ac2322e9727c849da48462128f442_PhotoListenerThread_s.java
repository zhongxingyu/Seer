 package PhotoListener;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import com.drew.imaging.ImageMetadataReader;
 import com.drew.imaging.ImageProcessingException;
 import com.drew.lang.GeoLocation;
 import com.drew.metadata.Metadata;
 import com.drew.metadata.MetadataException;
 import com.drew.metadata.exif.ExifSubIFDDirectory;
 import com.drew.metadata.exif.GpsDirectory;
 import com.drew.metadata.jpeg.JpegDirectory;
 
 import ActivationManager.ActivationManager;
import Common.Photo;
 import Common.GPSPoint;
 import android.os.Environment;
 import android.os.FileObserver;
 import android.util.Log;
 
 public class PhotoListenerThread extends FileObserver {
 
 	private final String TAG = PhotoListenerThread.class.getName();
 	String absolutePath;
 
 	public PhotoListenerThread(String path) {
 		super(path, FileObserver.CLOSE_WRITE);
 		this.absolutePath = path;
 	}
 
 	@Override
 	public void onEvent(int event, String path) {
 		if (isExternalStorageReadable()) {
 			if (path.endsWith(".jpg")) {
 				Photo photo = null;
 				while (photo == null) {
 					String file = absolutePath + path;
 					try {
 						photo = createPhotoFromFile(file);
 					}
 					catch (ImageProcessingException e) { // this means photo was not saved fully
 						Log.e(TAG, "Sleeping until photo completely saved");
 						int counter = 0;
 						try {
 							counter++;
 							if (counter < 10)
 								Thread.sleep(2000, 0);
 							else {
 								// give up
 								return;
 							}
 						} catch (InterruptedException e1) {
 							Log.d(TAG, "onEvent: InterruptedException");
 						}
 					}
 				}
 				if (photo != null)
 					ActivationManager.getInstance().addToBuffer(photo);
 			}
 		}
 		else {
 			Log.d(TAG,"External storage is not readable");
 		}
 	}
 
 
 	public static Photo createPhotoFromFile(String file) throws ImageProcessingException {
 
 		File path = new File(file);
 
 		// extract photo metadata
 		Metadata metadata = null;
 		try {
 			metadata = ImageMetadataReader.readMetadata(path);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		//get location
 		GpsDirectory directory1 = metadata.getDirectory(GpsDirectory.class);
 
 		GeoLocation location = directory1.getGeoLocation();
 		if (location == null) { // photo has not location
 			return null;
 		}
 
 		//get time
 		ExifSubIFDDirectory directory2 = metadata.getDirectory(ExifSubIFDDirectory.class);
 
 		Date date = directory2.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
 
 		Photo photo = null;
 
 		//get dimensions
 		JpegDirectory jpgDirectory = metadata.getDirectory(JpegDirectory.class);
 		try {
 			int width = jpgDirectory.getImageWidth();
 			int	height = jpgDirectory.getImageHeight();
 
 			photo = new Photo(
 					date,
 					width,
 					height,
 					new GPSPoint(location.getLatitude(),location.getLongitude()),
 					path.getPath());
 		} catch (MetadataException e) {
 			// TODO ERROR reading EXIF details of photo
 			e.printStackTrace();
 		}
 
 		return photo;
 	}
 
 	// TODO: switch to private
 	/* Checks if external storage is available to at least read */
 	private static boolean isExternalStorageReadable() {
 		String state = Environment.getExternalStorageState();
 		if (Environment.MEDIA_MOUNTED.equals(state) ||
 				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			return true;
 		}
 		return false;
 	}
 
 }
