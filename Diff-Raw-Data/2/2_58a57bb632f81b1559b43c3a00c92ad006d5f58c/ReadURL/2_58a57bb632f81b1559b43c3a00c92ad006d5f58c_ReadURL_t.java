 package dk.markedsbooking.loppemarkeder.util;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import dk.markedsbooking.loppemarkeder.MainActivity;
 import dk.markedsbooking.loppemarkeder.dummy.MarkedContent;
 import dk.markedsbooking.loppemarkeder.model.MarkedItem;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class ReadURL extends AsyncTask<String, Void, String>{
 
 	@Override
 	public String doInBackground(String... urls) {
 		
         String response = "";
         for (String url : urls) {
             DefaultHttpClient client = new DefaultHttpClient();
             HttpGet httpGet = new HttpGet(url);
             try {
                 HttpResponse execute = client.execute(httpGet);
                 InputStream content = execute.getEntity().getContent();
 
                 BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                 String s = "";
                 while ((s = buffer.readLine()) != null) {
                     response += s;
                 }
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         
         try {
         	
         	JSONObject jObject = new JSONObject(response);
         	String aJsonString = jObject.getString("markedItemInstanceList");
         	Log.i(MainActivity.class.getName(), "aJsonString "+aJsonString);
         	
 
             JSONArray jsonArray = new JSONArray(aJsonString);
             Log.i(MainActivity.class.getName(),
                     "Number of entries " + jsonArray.length());
             for (int i = 0; i < jsonArray.length(); i++) {
             	JSONObject jsonObject = jsonArray.getJSONObject(i);
              	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2013-04-10T04:00:00Z
             	Date fromDate  = formatter.parse(jsonObject.getString("fromDate"));
             	Date toDate = null;
            	if (jsonObject.getString("toDate") != null) {
             		toDate  = formatter.parse(jsonObject.getString("toDate"));
             	}
             	Long id = jsonObject.getLong("id");
             	String name = jsonObject.getString("name");
             	String address = jsonObject.getString("address");
             	String dateExtraInfo = jsonObject.getString("dateExtraInfo");
             	String entreInfo =jsonObject.getString("entreInfo"); 
             	double latitude = jsonObject.getDouble("latitude");
             	double longitude = jsonObject.getDouble("longitude");
             	String markedInformation = jsonObject.getString("markedInformation");
             	
             	String markedRules= jsonObject.getString("markedRules");
             	
                 Log.i(MainActivity.class.getName(), "name " +name);
                 Log.i(MainActivity.class.getName(), "id "+ jsonObject.getLong("id"));
                 Log.i(MainActivity.class.getName(), "fromDate "+ jsonObject.getString("fromDate"));
                 Log.i(MainActivity.class.getName(), "date "+ fromDate);
                 
                 MarkedContent.addMarkedItem(new MarkedItem(""+id, name, fromDate, toDate, dateExtraInfo, entreInfo, markedRules, markedInformation, latitude, longitude,address ));
             }
             
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         return response;
     }
 
 }
