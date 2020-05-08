 package com.android_prod;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import android.content.Context;
 import android.content.Intent;
 
 import android.os.AsyncTask;
 
 import android.util.Log;
 import android.widget.Toast;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import java.lang.Object;
 
 import java.nio.Buffer;
 
 public class HttpRequestClass extends AsyncTask<Void, Integer, Void> {
 
     // Variable
     Context mContext;
     Intent  intent;
     String  dataBike;    // dataBus;
 
 	 String FILENAME = "bus.json";
     
     FileOutputStream fos;
     FileInputStream fis;
   
 
     // Constuctor
     public HttpRequestClass(Context context, Intent i) {
         mContext = context;
         intent   = i;
     }
 
     @SuppressWarnings("null")
     protected Void doInBackground(Void... arg0) {
     	
     	
          
  		try {
  			
  			
  			
  			File file = mContext.getFileStreamPath(FILENAME);
  			System.out.println(file.getPath());
  			if(!file.exists())
  			{
  				
  				 
  		
 
         /** *** Get the bike data *** */
 
         // First we create the variable for the call
         InputStream is         = null;
         String      resultBike = "";
 
         // String resultBus = "";
 
         // First let's get the bus
         try {
             HttpClient httpclient = new DefaultHttpClient();
             HttpPost   httppost   =
                 new HttpPost("http://data.keolis-rennes.com/json/?version=2.0&key=FR6UMKCXT1TY5GJ&cmd=getbikestations");
             HttpResponse response = httpclient.execute(httppost);
             HttpEntity   entity   = response.getEntity();
 
             is = entity.getContent();
         } catch (Exception e) {
             System.out.println("Error in http connection " + e.toString());
         }
 
         // Now we convert the response into a String
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
             StringBuilder  sb     = new StringBuilder();
             String         line   = null;
 
             while ((line = reader.readLine()) != null) {
                 sb.append(line + "\n");
             }
 
             is.close();
             resultBike = sb.toString();
 
             // System.out.println("Here is the server result: " + result);
         } catch (Exception e) {
             System.out.println("Error converting result " + e.toString());
         }
 
         dataBike = resultBike;
        // wirte in data cache file
         fos = mContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
 		fos.write(dataBike.getBytes());	
 		fos.close();
         
  			}
  			else
  			{	
 			
 				// read in the data cache file
  				fis=mContext.openFileInput(FILENAME);
 				InputStreamReader isr = new InputStreamReader(fis);
 		        BufferedReader br = new BufferedReader(isr);
 		        dataBike = br.readLine();
 		        Log.i("Reading file" , dataBike); // log info
  				fis.close();
  			}
         
  		} catch (Exception e) {
 			Log.i(" Exception :", e.toString()); // Exception traces
  		}
 
 
         return null;
     }
 
     // Executed once the calls are done
     protected void onPostExecute(Void result) {
     	
 
         // Then put data in the intent
         intent.putExtra("bikeData", dataBike);
 
         // Start the other Activity
         mContext.startActivity(intent);
     }
 }
 
 
 //~ Formatted by Jindent --- http://www.jindent.com
