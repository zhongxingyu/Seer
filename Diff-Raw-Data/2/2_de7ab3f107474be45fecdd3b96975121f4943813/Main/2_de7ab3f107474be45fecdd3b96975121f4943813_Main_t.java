 package com.andyengle.peanutbutter;
 
 import android.app.Activity;
 import android.os.Bundle;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import android.util.Log;
 
 public class Main extends Activity {
     private String TAG = "PeanutButter";
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         try {
             Log.d(TAG, "Displaying the prof list...");
             displayProfList();
         } catch (Exception e) {
             Log.d(TAG, "Trouble! " + e);
         }
     }
 
     private void displayProfList() throws Exception {
         /**
          * This calls grabMeSomeJson(), which calls the server for a
          * JSON-formatted list of selected CS faculty.
          */
         String jsonStr = grabMeSomeJson();
         Log.d(TAG, "myStr: " + jsonStr);
         /**
          * Turn the JSON string into an object we can work with
          */
         JSONObject jObject = new JSONObject(jsonStr);
         /**
          * Let's print some stuff
          */
         Log.d(TAG, "jString: " + jsonStr); // TEMP
         Log.d(TAG, "success: " + jObject.getBoolean("success"));
         Log.d(TAG, "auth: " + jObject.getBoolean("auth"));
         Log.d(TAG, "time: " + jObject.getString("currentTime"));
         /**
          * This profListArr contains an array of JSON objects. We need to
          * iterate through the list in order to pull out the data contained in
          * that array.
          */
         JSONArray profListArr = jObject.getJSONArray("profList");
         for (int i = 0; i < profListArr.length(); i++) {
             Log.d(TAG, "========================================");
             Log.d(TAG, "Prof ID: " + profListArr.getJSONObject(i).getString("id"));
             Log.d(TAG, " first name: " + profListArr.getJSONObject(i).getString("firstName"));
             Log.d(TAG, "last name: " + profListArr.getJSONObject(i).getString("lastName"));
             Log.d(TAG, "phone: " + profListArr.getJSONObject(i).getString("phone"));
             Log.d(TAG, "photo: " + profListArr.getJSONObject(i).getString("photoUrl"));
             /**
              * Within each object in the profList array is another array
             * containing even more information. In this case, it's an array of
              * name/value pairs containing a course listing for each professor.
              */
             JSONArray courseArr = profListArr.getJSONObject(i).getJSONArray("courseList");
             /**
              * Iterate through the list to grab the course information then
              * print that junk out
              */
             for (int j = 0; j < courseArr.length(); j++) {
                 String key = courseArr.getJSONObject(j).getString("name");
                 String value = courseArr.getJSONObject(j).getString("value");
                 Log.d(TAG, "course: " + key + " ==> " + value);
             }
         }
     }
 
     private String grabMeSomeJson() {
         /**
          * Create your objects so they will return something gracefully if
          * everything goes down in flames.
          */
         StringBuffer sb = new StringBuffer("");
         BufferedReader in = null;
         try {
             Log.d(TAG, "Firing off the HTTP request");
             HttpClient client = new DefaultHttpClient();
             HttpGet request = new HttpGet();
             request.setURI(new URI("http://kalosoft.com/cs490/profs.php"));
             HttpResponse response = client.execute(request);
             /**
              * Now that we should have received a response, load the response
              * into a StringBuffer (good practice to use StringBuffers when
              * building long Strings), which we'll later return to the caller.
              */
             in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
             String line = "";
             while ((line = in.readLine()) != null) {
                 sb.append(line);
             }
             Log.d(TAG, "Sweet! We have completed the HTTP request.");
         } catch (Exception e) {
             Log.d(TAG, "Trouble: " + e);
         } finally {
             /**
              * Close that junk!
              */
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
         /**
          * Convert the StringBuffer into a String; much easier to work with
          */
         return sb.toString();
     }
 }
