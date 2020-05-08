 package org.fourdnest.androidclient.comm;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.comm.CommUtils;
 import org.fourdnest.androidclient.tools.LocationHelper;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.net.Uri;
 import android.util.Log;
 
 public class OsmStaticMapGetter implements StaticMapGetter {
 	private static final String TAG = "StaticMapGetter";
 	private static final String BASEURI = "http://pafciu17.dev.openstreetmap.org/?module=map";
 	private static final String JPG_TYPE = "imgType=jpg";
 	private static final int DEFAULT_SIZE = 600;
 	private static final float MARGIN = 0.0001f;
 	private static final String FLOAT_TO_STRING_FORMAT = "%.6f";
 
 	
 	/**
 	 * Retrieves static map image for a route egg using OSM static maps API.
 	 * 
 	 * @param egg Egg that has a route file as local file
 	 * @return boolean value of result (Ok / not OK)
 	 */
 	public boolean getStaticMap(Egg egg) {
 		String internalUriString = ThumbnailManager.getThumbnailUriString(egg, null); // size not needed
 		Log.d(TAG, internalUriString);
 		List<String> list = new ArrayList<String>();
 		try {
 			list = getLocationListFromEgg(egg);
 		} catch (Exception e) {
 			Log.d(TAG, "Failed to produce location list from location file");
 			e.printStackTrace();
 			return false;
 		}
 		String uriString = BASEURI;
 		uriString = setBoundBox(uriString, list);
 		uriString = addPath(uriString, list);
 		uriString = setHeight(uriString, DEFAULT_SIZE);
 		uriString = setWidth(uriString, DEFAULT_SIZE);
 		
 		uriString = uriString + JPG_TYPE;
 		Log.d("thumbs", uriString);
 		boolean val = CommUtils.getNetFile(Uri.parse(uriString), Uri.parse(internalUriString));
 		Log.d("map_val", String.valueOf(val));
 		return val;
 	}
 	/*
 	 * Set map limits (bound box)
 	 */
 	private String setBoundBox(String uriString, List<String> list) {
 		
 		Float leftBoundLongitude = Float.POSITIVE_INFINITY;
 		Float topBoundLatitude = Float.NEGATIVE_INFINITY;
 		Float rightBoundLongitude = Float.NEGATIVE_INFINITY;
 		Float lowBoundLatitude = Float.POSITIVE_INFINITY;
 		Float lat;
 		Float lon;
 		String[] temp;
 		
 		// find limits
 		for (String locString : list) {
 			temp = locString.split(",");
 			lat = Float.valueOf(temp[1]);
 			lon = Float.valueOf(temp[0]);
 			if (lon < leftBoundLongitude) {
 				leftBoundLongitude = lon;
 			}
 			if (lon > rightBoundLongitude) {
 				rightBoundLongitude = lon;
 			}
 			if (lat < lowBoundLatitude) {
 				lowBoundLatitude = lat;
 			}
 			if (lat > topBoundLatitude) {
 				topBoundLatitude = lat;
 			}
 		}
 		// add margins, static at the moment
 		topBoundLatitude = topBoundLatitude + MARGIN;
		lowBoundLatitude = lowBoundLatitude + MARGIN;
		leftBoundLongitude = leftBoundLongitude + MARGIN;
 		rightBoundLongitude = rightBoundLongitude + MARGIN;
 		
 		// generate request attribute
 		String attribute = "&bbox="
 		 + String.format(FLOAT_TO_STRING_FORMAT, leftBoundLongitude)
 		 + "," + String.format(FLOAT_TO_STRING_FORMAT, topBoundLatitude)
 		 + "," + String.format(FLOAT_TO_STRING_FORMAT, rightBoundLongitude)
 		 + "," + String.format(FLOAT_TO_STRING_FORMAT, lowBoundLatitude);
 		
 		return uriString + attribute;
 	}
 	
 	/*
 	 * Add path information from list of locations to request string
 	 */
 	private String addPath(String uriString, List<String> list) {
 		String attribute = "&paths=";
 		String separator = ",";
 		uriString = uriString + attribute;
 		for (String location : list) {
 			uriString = uriString + location + separator; // there can be a , in the end, too
 		}
 		return uriString;
 	}
 	
 	private String addPoint(String uriString, Egg egg) {
 		return uriString;
 	}
 	
 	private String setCenterPoint(String uriString, float longitude, float latitude) {
 		return uriString + "&center=" + String.format("%.4f", longitude) + "," +  String.format("%.4f", longitude);
 	}
 	
 	private String setWidth(String uriString, int width) {
 		return uriString + "&width=" + width;
 	}
 	
 	private String setHeight(String uriString, int height) {
 		return uriString + "&height=" + height;
 	}
 	
 	private String setZoom(String uriString, int zoomLevel) {
 		return uriString;
 	}
 	
 	/*
 	 * Generate a list of locations from egg's route file
 	 */
 	private List<String> getLocationListFromEgg(Egg egg) throws Exception {
 		List<String> locList = new ArrayList<String>();
 		FileInputStream fstream = new FileInputStream(egg.getLocalFileURI().getEncodedPath());
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader buffRead = new BufferedReader(new InputStreamReader(in));
 		String line;
 		try {
 			while ((line = buffRead.readLine()) != null) {
 				JSONObject temp = new JSONObject(line);
				String lat = temp.optString(LocationHelper.JSON_LATITUDE);
				String lon = temp.optString(LocationHelper.JSON_LONGITUDE);
				locList.add(lat + "," + lon);			
 			}
 		} catch (JSONException e) {
 			Log.d(TAG, "Could not convert location file line to json object");
 			e.printStackTrace();
 		} finally {
 			buffRead.close();
 			in.close();
 			fstream.close();
 		}
 		return locList;
 	}
 
 
 }
