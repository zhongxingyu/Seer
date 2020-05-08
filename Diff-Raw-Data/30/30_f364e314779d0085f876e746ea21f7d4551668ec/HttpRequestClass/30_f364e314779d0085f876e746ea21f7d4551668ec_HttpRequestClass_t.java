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
     String  dataBike;    // dataBike;
     String  dataBus;    // dataBus;
 
 	 String FILENAME_BIKE = "bike.json";
 	 String FILENAME_BUS = "bus.json";
     
     FileOutputStream fos;
     FileInputStream fis;
    
 
     // Constuctor
     public HttpRequestClass(Context context, Intent i) {
         mContext = context;
         intent   = i;
     }
 
     @SuppressWarnings("null")
     protected Void doInBackground(Void... arg0) {
     	
     	/**FOR Bike*/
  		
  		dataBike =callServer("http://data.keolis-rennes.com/json/?version=2.0&key=FR6UMKCXT1TY5GJ&cmd=getbikestations",FILENAME_BIKE,5000);
  		/**For bus*/
 
 		dataBus =callServer("http://148.60.11.208:3000/android/data/bus",FILENAME_BUS,5000);
  		/**For Metro*/
         return null;
     }
 
     // Executed once the calls are done
     protected void onPostExecute(Void result) {
     	
         // Then put data in the intent
         intent.putExtra("bikeData", dataBike);
        intent.putExtra("busData", dataBus);
         // Start the other Activity
         mContext.startActivity(intent);
     }
     
     
     
   /** method use to do serveur call*/
     public String callServer(String URL, String nameCacheFile, int timeUpdate)
     {
     	
 	  String result = "";
  		
  		
 try {
  			
 	// First we create the variable for the call
     	InputStream is         = null;
 
     	
  			File file = mContext.getFileStreamPath(nameCacheFile);
  			if(!file.exists() || (file.lastModified() < timeUpdate && timeUpdate!=-1)) // if the file do not excite 
  			{
  				
  				 
  		
 
         /** *** Get the bus data *** */
 
        
 
         // First get
         try {
             HttpClient httpclient = new DefaultHttpClient();
             HttpPost   httppost   =
                 new HttpPost(URL);
             HttpResponse response = httpclient.execute(httppost);
             HttpEntity   entity   = response.getEntity();
 
             is = entity.getContent();
         } catch (Exception e) {
         	Log.i("Error in http connection " , e.getMessage());
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
             result = sb.toString();
 
            
         } catch (Exception e) {
         	Log.i("Error converting result " , e.getMessage());
         }
       
         // wirte in data cache file
         fos = mContext.openFileOutput(nameCacheFile, Context.MODE_PRIVATE);
 		fos.write(result.getBytes());	
 		fos.close();
 		Log.i("download info" , result); // log info
 		 // return result;
         
  			}
  			else
  			{	
  			
  				// read in the data cache file
  				fis=mContext.openFileInput(nameCacheFile);
  				InputStreamReader isr = new InputStreamReader(fis);
  		        BufferedReader br = new BufferedReader(isr);
  				fis.close();
  				
  				Log.i("Reading file" , result); // log info
  				
  				//return result;
  			}
         
  		} catch (Exception e) {
  			Log.i(" Exception :", e.getMessage()); // Exception traces
  		}
 
 return result;
     }
 }
 
 
 //~ Formatted by Jindent --- http://www.jindent.com
