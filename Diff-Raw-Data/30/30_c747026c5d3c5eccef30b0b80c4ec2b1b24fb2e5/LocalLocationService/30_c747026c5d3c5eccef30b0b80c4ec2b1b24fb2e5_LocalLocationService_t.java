 package nz.gen.wellington.penguin.data;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Date;
 import java.util.List;
 
 import nz.gen.wellington.penguin.config.Config;
 import nz.gen.wellington.penguin.model.Location;
 import nz.gen.wellington.penguin.utils.DateTimeHelper;
 import nz.gen.wellington.penguin.utils.FileService;
 import android.content.Context;
 import android.util.Log;
 
 public class LocalLocationService implements LocationService {
 	
 	private static final String TAG = "LocalLocationService";	
	private static final String CACHE_FILE_NAME = "locations_" + Config.SER_VERSION +".ser";
 
 	public List<Location> getLocations(Context context) {
 		if (!isLocallyCached(context)) {
 			return null;
 		}
 				
 		Log.i(TAG, "Reading from disk: " + CACHE_FILE_NAME);
 		try {
 			FileInputStream fis = FileService.getFileInputStream(context, CACHE_FILE_NAME);
 			ObjectInputStream in = new ObjectInputStream(fis);
			
			List<Location> loaded = null;
			try {
				loaded = (List<Location>) in.readObject();

			} catch (Exception e) {
				Log.e(TAG, "Error while trying to deserialize: " + e.getMessage());
				loaded = null;
			}
 			in.close();
 			
 			if (loaded != null) {			
 				return loaded;
 			} else {
 				Log.w(TAG, "Article bundle was null after read attempt");
 			}
 			return null;
 
 		} catch (IOException ex) {
 			Log.e(TAG, "IO Exception while reading locations");
 		}
		
 		return null;
 	}
 	
 	public void saveLocations(Context context, List<Location> locations) {
 		Log.d(TAG, "Writing to disk: " + CACHE_FILE_NAME);
 		try {
 			FileOutputStream fos = FileService.getFileOutputStream(context, CACHE_FILE_NAME);
 			ObjectOutputStream out = new ObjectOutputStream(fos);
 			out.writeObject(locations);
 			out.close();
 
 		} catch (IOException ex) {
 			Log.e(TAG, "IO Exception while writing: " + CACHE_FILE_NAME);
 			Log.e(TAG, ex.getMessage());
 		}
 		Log.d(TAG, "Finished writing to disk: '" + CACHE_FILE_NAME);
 	}
 	
 	public boolean isCurrent(Context context) {
 		Date modificationTime = FileService.getModificationTime(context, CACHE_FILE_NAME);
 		int age = DateTimeHelper.durationInSecords(modificationTime, DateTimeHelper.now());
 		Log.i(TAG, "Cache file age is: " + age);
 		return age < Config.CACHE_TTL;
 	}
 	
 	public boolean isLocallyCached(Context context) {
 		boolean locallyCached = FileService.existsLocally(context, CACHE_FILE_NAME);
 		Log.i(TAG, "Locations are locally cached: " + locallyCached);
 		return locallyCached;
 	}	
 	
 }
