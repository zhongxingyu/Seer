 package edu.pugetsound.npastor.utils;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureStore;
 import org.geotools.data.Transaction;
 import org.geotools.data.shapefile.ShapefileDataStore;
 import org.geotools.data.shapefile.ShapefileDataStoreFactory;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.data.simple.SimpleFeatureStore;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 import edu.pugetsound.npastor.TacomaDRTMain;
 import edu.pugetsound.npastor.routing.Vehicle;
 
 public class DRTUtils {
 
 	public final static String TAG = "DRTUtils";
 	
 	/**
 	 * Returns age group code which the specified age falls in
 	 * @param age Age
 	 * @return Age group code for specified age
 	 */
 	public static int getGroupForAge(int age) {
 		if(age < 15)
 			return Constants.APTA_AGE_0_14;
 		else if(age < 20)
 			return Constants.APTA_AGE_15_19;
 		else if(age < 25)
 			return Constants.APTA_AGE_20_24;
 		else if(age < 35)
 			return Constants.APTA_AGE_25_34;
 		else if(age < 45)
 			return Constants.APTA_AGE_35_44;
 		else if(age < 55)
 			return Constants.APTA_AGE_45_54;
 		else if(age < 65)
 			return Constants.APTA_AGE_55_64;
 		else
 			return Constants.APTA_AGE_65_OVER;
 	}
 	
 	public static String getTripTypeString(int tripType) {
 		String tripString = "";
 		
 		if(tripType ==Constants.TRIP_COMMUTE)
 			tripString = "commute";
 		else if(tripType ==Constants.TRIP_SCHOOL)
 			tripString = "school";
 		else if(tripType ==Constants.TRIP_SOCIAL)
 			tripString = "social";
 		else if(tripType ==Constants.TRIP_SHOPPING_DINING)
 			tripString = "shopping/dining";
 		else if(tripType ==Constants.TRIP_MEDICAL_DENTAL)
 			tripString = "medical/dental";
 		else if(tripType == Constants.TRIP_PERSONAL_BUSINESS)
 			tripString = "personal_business";
 		else if(tripType ==Constants.TRIP_OTHER)
 			tripString = "other";
 		
 		return tripString;
 	}
 	
 	/**
 	 * Convert minutes to HH:mm format
 	 * @param mins Desired time in minutes
 	 * @return Formatted String representation of specified time
 	 */
 	public static String minsToHrMin(int mins) {
 		int hh = mins / 60;
 		int mm = mins % 60;
 		String str = hh + ":";
 		if(mm < 10) str += "0" + mm;
 		else str += mm;
 		return str;
 	}
 	
 	/**
 	 * Formats a millisecond time to "yyyy-MM-dd'_'HH-mm-ss" format
 	 * @param millis desired time in milliseconds
 	 * @return Formatted String representation of specified time
 	 */
 	public static String formatMillis(long millis) {
 		
 		Date date = new Date(millis);
 		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'_'HH-mm-ss");
 		String dateFormatted = formatter.format(date);
 		return dateFormatted;
 	}
 	
 	public static void writeTxtFile(ArrayList<String> text, String filename, boolean readOnly) {
 		
 		// Get filename
 		String path = TacomaDRTMain.getSimulationDirectory() + filename;
 		
 		// Write to file
 		try {
 			FileWriter writer = new FileWriter(path, true);
 			PrintWriter lineWriter = new PrintWriter(writer);
 			
 			for(String str : text) {
 				// Write to file
 				lineWriter.println(str);
 			}
 			lineWriter.close();
 			writer.close();
 			
 			if(readOnly) {
 				boolean result = new File(path).setReadOnly();
 				if(!result)
 					Log.e(TAG, "Unable to make file read only at: " + path);
 			}
			Log.iln(TAG, "Text file successfully written at: " + path);
 		} catch (IOException ex) {
 			Log.e(TAG, "Unable to write to file at: " + path);
 			ex.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Writes the geographic data to a shapefile
 	 * Adapted from: http://docs.geotools.org/latest/tutorials/feature/csv2shp.html#write-the-feature-data-to-the-shapefile
 	 * @param featureType The feature type of the collection
 	 * @param collection A collection of feature types
 	 * @param shpFile A file descripter. This specifies name and destination of the file
 	 */
 	public static void writeShapefile(SimpleFeatureType featureType, SimpleFeatureCollection collection, File shpFile) {
 
         ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
 
         try {
 	        Map<String, Serializable> params = new HashMap<String, Serializable>();
 	        params.put("url", shpFile.toURI().toURL());
 	        params.put("create spatial index", Boolean.TRUE);
 	        
 	        // Build the data store, which will hold our collection
 	        ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
 	        dataStore.createSchema(featureType);
 	        
 	        // Finally, write the features to the shapefile
 	        Transaction transaction = new DefaultTransaction("create");
 	        String typeName = dataStore.getTypeNames()[0];
 	        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
 	        
 	        if (featureSource instanceof FeatureStore) {
 	            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
 	            featureStore.setTransaction(transaction);
                 featureStore.addFeatures(collection);
                 transaction.commit();
                 Log.iln(TAG, "Shp file succesfully writen at: " + shpFile.getPath());
 	        }
 	        transaction.close();
         } catch (MalformedURLException ex) {
         	Log.e(TAG, "Unable to save trips to shapefile at: " + shpFile.getPath()
         			+ "\n  " + ex.getMessage());
         	ex.printStackTrace();
         } catch (IOException ex) {
         	Log.e(TAG, "Unable to open or write to shapefile at: " + shpFile.getPath() 
         			+ "\n  " + ex.getMessage());
         	ex.printStackTrace();
         }
 	}
 	
 	public static double metersToMiles(double meters) {
 		return meters * 0.000621371;
 	}
 }
