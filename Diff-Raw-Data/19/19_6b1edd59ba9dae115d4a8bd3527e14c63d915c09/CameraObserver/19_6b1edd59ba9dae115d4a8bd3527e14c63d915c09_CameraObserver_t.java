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
 import com.summaphoto.SettingsActivity;
 import com.summaphoto.SmartModeFlow;
 
 import Common.GPSPoint;
 import Common.Photo;
 import Common.PhotoContainer;
 import Common.Utils;
 import android.os.FileObserver;
 import android.util.Log;
 
 /**
 * The FileObserver thread that listens to the camera's folder
  * @author yonatan
  *
  */
 public class CameraObserver extends FileObserver {
 
 	private static final String TAG = CameraObserver.class.getName();
 	String absolutePath;
 
 	private int locationlessPhotos = 0; 
 
 	public CameraObserver(String path) {
 		super(path, FileObserver.CLOSE_WRITE | FileObserver.DELETE);
 		this.absolutePath = path;
 	}
 
 	@Override
 	public void onEvent(int event, String path) {
 		if (path != null) {
 			if (Utils.isExternalStorageReadable()) {
 				if (path.endsWith(".jpg") ||
 						path.endsWith(".JPG") ||
 						path.endsWith(".jpeg") ||
 						path.endsWith("JPEG")) {
 
 					Photo photo = null;
 					String file = absolutePath + path;
 
 					if ((event & FileObserver.CLOSE_WRITE) > 0) {
 						try {
 							photo = createPhotoFromFile(file);
 							Log.d(TAG, "Photo taken: " + path + " was read from file");
 
 						} catch (ImageProcessingException e) {
 							Log.e(TAG, "Photo taken: " + path + " was not yet fully saved properly");
 							try {
 								Thread.sleep(2000); // sometimes it takes a bit more time to save
 							} catch (InterruptedException e1) {
 								Log.e(TAG, "Waiting for photo to be saved was interrupted");
 							}
 							try { // try reading again
 								photo = createPhotoFromFile(file);
 							} catch (ImageProcessingException e1) {
 								Log.e(TAG, "Photo taken: " + "was NOT read from file properly");
 							}
 						}
 
 						if (photo != null) {
 							PhotoContainer.getInstance().addToBuffer(photo);
 							if (SettingsActivity.MODE == 1)
 								if (!SmartModeFlow.isFlowRunning()) { // SMART MODE - starts whenever a photo is received if not busy
 									SmartModeFlow.startFlow();
 								}
 								else {
 									// nothing
 								}
 						}
 
 						else {
 							if (locationlessPhotos > 5) {
 								Utils.notifyUserWithError("Photos have no location", "Make sure geo-tagging is on in your device.");
 								locationlessPhotos = 0;
 							}
 						}
 					}
 
 					if ((event & FileObserver.DELETE) > 0) {
 						PhotoContainer.getInstance().onDelete(file);
 					}
 				}
 			}
 			else {
 				Log.wtf(TAG,"External storage is not readable!");
 			}
 		}
 	}
 
	/**
	 * Creates photo object from intercepted file using Metadata-extractor library
	 * @param file
	 * @return
	 * @throws ImageProcessingException
	 */
 	private Photo createPhotoFromFile(String file) throws ImageProcessingException {
 
 		Photo photo = null;
 		File path = new File(file);
 
 		// extract photo metadata
 		Metadata metadata = null;
 		try {
 			metadata = ImageMetadataReader.readMetadata(path);
 		} catch (IOException e) {
 			throw new ImageProcessingException(e);
 		}
 
 		//get location
 		GeoLocation location = null;
 		try {
 			GpsDirectory directory1 = metadata.getDirectory(GpsDirectory.class);
 			location = directory1.getGeoLocation();
 			if (location == null) { // photo has no location, don't create photo
 				this.locationlessPhotos++;
 				throw new NullPointerException();
 			}
 		}
 		catch (NullPointerException exception) {
 			Log.e(TAG, "Photo " + path.getName() + " has no location.");
 			return null;
 		}
 
 		//get time
 		ExifSubIFDDirectory directory2 = metadata.getDirectory(ExifSubIFDDirectory.class);
 		Date date = directory2.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
 
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
 			Log.e(TAG, "Error getting photo dimensions");
 			return null;
 		}
 
 		return photo;
 	}
 
 
 
 }
