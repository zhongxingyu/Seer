 package Bing;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HTTP;
 
 import android.R.integer;
 import android.media.MediaScannerConnection;
 import android.os.Environment;
 import android.util.Log;
 import ActivationManager.EventCandidate;
 import ActivationManager.EventCandidateContainer;
 
 import Common.Photo;
 import Common.GPSPoint;
 
 public class BingServices {
 
 	private final static String TAG = "BingServices";
 
 	/**
 	 * Queries BING for JPG & Metadata for ActualEvent points
 	 * @param points list of all Points in ActualEvents
 	 * @param width width in pixels
 	 * @param height height in pixels
 	 * @return StaticMap, or NULL if map could not be created
 	 */
 	public static StaticMap getStaticMap(List<GPSPoint> points, int width, int height) {
 
 		StaticMap map = null;
 
 		if (points.size()  > 0) { // Request only iff there is at least one photo
 
 			map = new StaticMap(width, height);
			map.setJpgPath(getJPG(points));
 			map.setMetadataPath(getJPGMetadata(points));
 			
 			if (map.getJpgPath() == null || map.getMetadataPath() == null) {
 				map = null;  // free map for GC
 			}
 
 		}
 		else {
 			Log.d(TAG, "getStaticMap: Zero locations in request");
 		}
 
 		return map;
 	}
 
 	private static String getJPG(List<GPSPoint> points, int width, int height) {
 		return createHTTPRequest(false, points, width, height);
 	}
 
 	private static String getJPGMetadata(List<GPSPoint> points) {
 		return createHTTPRequest(true, points, -1, -1);
 	}
 
 	/**
 	 * creates a List of Points to be sent to BING from all cuurent ActualEvents in ActualEventContainer
 	 * @return List of all Points in ActualEventContainer
 	 */
 	public static List<GPSPoint> getImagesPointsList() {
 		List<GPSPoint> points = new ArrayList<GPSPoint>();
 		//TODO: this should work with the ActualEventContainer
 		for (EventCandidate event: EventCandidateContainer.getInstance().getAllEventsInContainer()) {
 			for (Photo photo : event.getEventPhotos()) {
 				points.add(photo.getLocation());
 			}
 		}
 		return points;
 	}
 
 	//
 	//	private static String getStaticMapOrMetadataFile(boolean metadata, List<Point> points) {
 	//
 	//		File file = null;
 	//		try {
 	//
 	//			URL                 url;
 	//			URLConnection   urlConn;
 	//			DataOutputStream    printout;
 	//			DataInputStream     input;
 	//
 	//			String urlString ="http://dev.virtualearth.net/REST/v1/Imagery/Map/AerialWithLabels?";
 	//			//Make the actual connection
 	//			if (metadata) {
 	//				urlString += "mmd=1&o=xml";
 	//			}
 	//			else {
 	//				urlString += "mmd=0";
 	//			}
 	//
 	//			urlString = urlString + "&mapSize=700,600&dcl=1&key=AjuPzlE1V8n1TJJK7T7elqCZlfi6wdLGvjyYUn2aUsNJ5ORSwnc-ygOwBvTa9Czt";
 	//
 	//			url = new URL(urlString);
 	//			urlConn = url.openConnection();
 	//			urlConn.setDoInput (true);
 	//			urlConn.setDoOutput (true); // POST Request
 	//			urlConn.setUseCaches (false);
 	//			urlConn.setRequestProperty("Content-Type", "text/plain");
 	//			urlConn.setRequestProperty("charset",  "charset=utf-8");
 	//
 	//			// adding pushpins coordinates to BING request
 	//			StringBuilder builder = new StringBuilder();
 	//
 	//			for (Point point : points)  {
 	//				builder.append("pp=");
 	//				builder.append(point.toString());
 	//				builder.append(";14;\r\n");
 	//			}
 	//
 	//			String strContent = builder.toString();
 	//
 	//			urlConn.setRequestProperty("Content-Length", Integer.valueOf(strContent.getBytes().length).toString()); 
 	//			printout = new DataOutputStream (urlConn.getOutputStream ());
 	//			printout.writeBytes (strContent);
 	//			printout.flush ();
 	//
 	//			// Get response
 	//			input = new DataInputStream (urlConn.getInputStream());
 	//
 	//			File externalStorageDir = Environment.getExternalStorageDirectory();
 	//			File testsDir = new File(externalStorageDir, "Tests");
 	//			file = new File(testsDir, "moshiko.");
 	//
 	//			if (!metadata) {
 	//				// TODO: make jpg data work with imageIO and not with file
 	//				file = new File(file.getPath() + "jpg");
 	//			}
 	//			else {
 	//				file = new File(file.getPath()  + "xml");
 	//			}
 	//
 	//			readFromStreamAndWriteToFile(input, testsDir, file);
 	//
 	//
 	//		} catch (IOException e) {
 	//			// TODO Auto-generated catch block
 	//			e.printStackTrace();
 	//		}   
 	//
 	//		return file.getPath();
 	//	}
 
 	/**
 	 * Queries Bing for JPG or Metadata
 	 * @param metadata TRUE if method should query for Metadata, FALSE if method should query for JPG
 	 * @param points 
 	 * @return Path of newly saved .JPG/XML or NULL
 	 */
 	private static String createHTTPRequest(boolean metadata, List<GPSPoint> points, int width, int height) {
 
 		String file = null;
 
 		try {
 			String urlString ="http://dev.virtualearth.net/REST/v1/Imagery/Map/AerialWithLabels?";
 			//Make the actual connection
 			if (metadata) {
 				urlString += "mmd=1&o=xml";
 			}
 			else {
 				urlString += "mmd=0";
 			}
 
 			// concatenate with BING key
 			urlString = urlString + "&mapSize=" + width +"," + height + "&dcl=1&key=AjuPzlE1V8n1TJJK7T7elqCZlfi6wdLGvjyYUn2aUsNJ5ORSwnc-ygOwBvTa9Czt";
 
 			// Construct POST Request
 			
 			HttpPost postReq = new HttpPost(urlString);
 
 			// adding pushpins coordinates to BING request
 			StringBuilder builder = new StringBuilder();
 			for (GPSPoint point : points)  {
 				builder.append("pp=");
 				builder.append(point.toString());
 				builder.append(";14;\r\n");
 			}
 			StringEntity entity = null;
 			try {
 				entity = new StringEntity(builder.toString(), HTTP.UTF_8);
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			postReq.setEntity(entity);
 
 			// Set BING Requested headers
 			postReq.setHeader("Content-Type", "text/plain");
 			postReq.setHeader("charset",  "charset=utf-8");
 
 			HttpResponse response = null;
 			try {
 				HttpClient httpclient = new DefaultHttpClient();
 				response = httpclient.execute(postReq);
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			StatusLine statusLine = response.getStatusLine();
 			if(statusLine.getStatusCode() == HttpStatus.SC_OK){
 				ByteArrayOutputStream out = new ByteArrayOutputStream();
 				response.getEntity().writeTo(out);
 				out.close();
 
 				// normal return
 				file = createOutputFile(metadata, out);
 
 			} else {
 				//Closes the connection.
 				response.getEntity().getContent().close();
 				throw new IOException(statusLine.getReasonPhrase());
 			}
 		}
 		catch (FileNotFoundException exception) {
 			//TODO : DIE
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// an error occured
 		return file;
 	}
 
 	private static String createOutputFile(boolean metadata, ByteArrayOutputStream out) throws IOException {
 		
 		File externalStorageDir = new File(Environment.getExternalStorageDirectory(), "Pictures");
 		File testsDir = new File(externalStorageDir.getAbsolutePath() + File.separator + "Output");
 		File file = new File(testsDir, "moshiko.");
 
 		// Construct right file according to requested content
 		if (!metadata) {
 			// TODO: make jpg data work with imageIO and not with file
 			file = new File(file.getPath() + "jpg");
 		}
 		else {
 			file = new File(file.getPath()  + "xml");
 		}
 
 		// create Directories & files, if needed
 		if (!testsDir.exists()) {
 			testsDir.mkdirs();
 		}
 		if (file.exists()) {
 			file.delete();
 		}
 		String state = Environment.getExternalStorageState();
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 		    Log.d("Test", "sdcard mounted and writable");
 			file.createNewFile();
 		}
 		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 		    Log.d("Test", "sdcard mounted readonly");
 		}
 		else {
 		    Log.d("Test", "sdcard state: " + state);
 		}
 
 		// actual write to file
 		OutputStream outputStream = new FileOutputStream (file); 
 		out.writeTo(outputStream);
 		out.flush();
 		out.close();
 		
 		return file.getPath();
 	}
 }
 
