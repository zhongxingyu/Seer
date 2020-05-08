 package duke.cs.fall2012.catseverywhere.model;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Random;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import com.google.android.maps.GeoPoint;
 
 import duke.cs.fall2012.catseverywhere.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.media.ExifInterface;
 import android.os.Environment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 
 
 /**
  * hardcoded image adapter to showcase the use of grid and gallery project
  */
 public class ImageAdapter {
 
 	private static LayoutInflater inflater = null;
 	private Activity activity;
 	private ArrayList<Drawable> myImages = new ArrayList<Drawable>();
 	private String mode = "";
 	private ExifInterface myExifInterface;
 	private final String LOC_SEPARATOR = "A";
 	private ArrayList<GeoPoint> myGeoPoints;
 	InputStream is;
 //
 //	public ImageAdapter(Activity act, String mode) {
 //		String[] allPaths = getImagePathsFromDb();
 //		getImagesFromUrls(allPaths);
 //		inflater = (LayoutInflater) act
 //				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 //		activity = act;
 //		this.mode = mode;
 //	}
 	public ImageAdapter() {
 		myGeoPoints =getGeoPointsFromLocs(getImageLocsFromDb());
 	}
 	
 	public ArrayList<GeoPoint> getRandomGeopoints(int n)
 	{
 		ArrayList<GeoPoint> myRandomGeoPoints = new ArrayList<GeoPoint>();
 		HashSet<Integer> imgIndexes = new HashSet<Integer>();
 		Random myRandGen = new Random();
 		if(n> myGeoPoints.size() -1)
 		{
 			n = myGeoPoints.size() -1;
 		}
 		
 		while(imgIndexes.size()< n)
 		{
 			imgIndexes.add(myRandGen.nextInt(n+1));
 		}
 		int i=0;
 		for(int index: imgIndexes)
 		{
			myRandomGeoPoints.add(myGeoPoints.get(index));
 		}
 		
 	
 		return myRandomGeoPoints;
 		
 	}
 //	
 //	private void getFileFromStream(InputStream in) throws IOException {  
 //		   
 //        File tempFile = File.createTempFile("tempFile", ".tmp");  
 //        tempFile.deleteOnExit();  
 //   
 //        FileOutputStream fout = null;  
 //   
 //        try {  
 //   
 //            fout = new FileOutputStream(tempFile);  
 //            int c;  
 //   
 //            while ((c = in.read()) != -1) {  
 //                fout.write(c);  
 //            }  
 //   
 //        }finally {  
 //            if (in != null) {  
 //                in.close();  
 //            }  
 //            if (fout != null) {  
 //                fout.close();  
 //            }  
 //        }  
 //    } 
 //		
 
 
 	
 //	public String[] getImagePathsFromDb() {
 //		// UPDATE TO PULL FROM DB
 //		// http post
 //		String result = "";
 //		try {
 //			HttpClient httpclient = new DefaultHttpClient();
 //			HttpPost httppost = new HttpPost(
 //					"http://squashysquash.com/CatsEverywhere/getImages.php");
 //			HttpResponse response = httpclient.execute(httppost);
 //			HttpEntity entity = response.getEntity();
 //			is = entity.getContent();
 //		} catch (Exception e1) {
 //			Log.e("log_tag", "Error in http connection " + e1.toString());
 //		}
 //		// convert response to string
 //		try {
 //			BufferedReader reader = new BufferedReader(new InputStreamReader(
 //					is, "iso-8859-1"), 8);
 //			StringBuilder sb = new StringBuilder();
 //			String line = null;
 //			while ((line = reader.readLine()) != null) {
 //				sb.append(line + "\n");
 //			}
 //			is.close();
 //
 //			result = sb.toString();
 //		} catch (Exception e2) {
 //			Log.e("log_tag", "Error converting result " + e2.toString());
 //		}
 //
 //		// parse json data
 //		JSONArray jArray = null;
 //		try {
 //			jArray = new JSONArray(result);
 //			String[] sArray = new String[jArray.length()];
 //			for (int i = 0; i < jArray.length(); i++) {
 //				sArray[i] = jArray.getString(i);
 //				System.out.println(sArray[i]);
 //			}
 //			return sArray;
 //		} catch (JSONException e3) {
 //			Log.e("log_tag", "Error parsing data " + e3.toString());
 //		}
 //		return null;
 //	}
 	
 	public ArrayList<GeoPoint> getGeoPointsFromLocs(String[] imgLocations)
 	{
 		
 		myGeoPoints = new ArrayList<GeoPoint>();
 		for(String location: imgLocations)
 		{
 		 if(parseLatLong(location,LOC_SEPARATOR)!=null)
 		 {
 			 float[] myLatLong = parseLatLong(location, LOC_SEPARATOR);
 			 GeoPoint geoP = new GeoPoint((int) (myLatLong[0] * 1E6), (int) (myLatLong[1] * 1E6));
 			 myGeoPoints.add(geoP);
 		 }
 		}
 		
 		return myGeoPoints;
 	}
 	
 	private float[] parseLatLong(String location, String separator) {
 		// TODO Auto-generated method stub
 		float[] latLong = new float[2];
 		if(location.contains(separator))
 		{
 			String[] latLongS = location.split(separator);
 			latLong[0] = Float.parseFloat(latLongS[0]);
 			latLong[1] = Float.parseFloat(latLongS[1]);
 			return latLong;
 		}
 		return null;
 	}
 	public String[] getImageLocsFromDb() {
 		// UPDATE TO PULL FROM DB
 		// http post
 		String result = "";
 		try {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(
 					"http://squashysquash.com/CatsEverywhere/getImageLocations.php");
 			HttpResponse response = httpclient.execute(httppost);
 			HttpEntity entity = response.getEntity();
 			is = entity.getContent();
 		} catch (Exception e1) {
 			Log.e("log_tag", "Error in http connection " + e1.toString());
 		}
 		// convert response to string
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "iso-8859-1"), 8);
 			StringBuilder sb = new StringBuilder();
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 			is.close();
 
 			result = sb.toString();
 		} catch (Exception e2) {
 			Log.e("log_tag", "Error converting result " + e2.toString());
 		}
 
 		// parse json data
 		JSONArray jArray = null;
 		try {
 			jArray = new JSONArray(result);
 			String[] sArray = new String[jArray.length()];
 			for (int i = 0; i < jArray.length(); i++) {
 				sArray[i] = jArray.getString(i);
 				System.out.println(sArray[i]);
 			}
 			return sArray;
 		} catch (JSONException e3) {
 			Log.e("log_tag", "Error parsing data " + e3.toString());
 		}
 		return null;
 	}
 
 //	public void getImagesFromUrls(String[] paths)
 //	{
 //		URL url;
 //		try {
 //			for(String tempPath: paths)
 //			{
 //				url = new URL("http://squashysquash.com/CatsEverywhere/" + tempPath);
 //				InputStream content = (InputStream) url.getContent();
 //				//String fileName = url.getFile();
 //				
 //			    Drawable d = Drawable.createFromStream(content , "src");
 //			    System.out.println("boo");
 //			    myImages.add(d);
 //			}
 //		} catch (MalformedURLException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //
 //	}
 	
 //	public int getCount() {
 //		return myImages.size();
 //	}
 //
 //	public Object getItem(int position) {
 //		return myImages.get(position);
 //	}
 //
 //	public long getItemId(int position) {
 //		return position;
 //	}
 
 //	public View getView(int position, View view, ViewGroup parent) {
 //		
 //		return null;
 //	}
 	
 //	public void DownloadFile(String fileURL, String fileName) {
 //        try {
 //            File root = Environment.getExternalStorageDirectory();
 //            URL u = new URL(fileURL);
 //            HttpURLConnection c = (HttpURLConnection) u.openConnection();
 //            c.setRequestMethod("GET");
 //            c.setDoOutput(true);
 //            c.connect();
 //            FileOutputStream f = new FileOutputStream(new File(root, fileName));
 //
 //            InputStream in = c.getInputStream();
 //
 //            byte[] buffer = new byte[1024];
 //            int len1 = 0;
 //            while ((len1 = in.read(buffer)) > 0) {
 //                f.write(buffer, 0, len1);
 //            }
 //            f.close();
 //        } catch (Exception e) {
 //            Log.d("Downloader", e.getMessage());
 //        }
 //	}
 
 
 }
