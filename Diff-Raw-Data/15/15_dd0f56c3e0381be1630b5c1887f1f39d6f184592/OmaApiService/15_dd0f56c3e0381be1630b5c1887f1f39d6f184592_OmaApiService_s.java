 /*
  * Copyright 2013 Bryan Sullivan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.example.omaapi;
 
 import java.io.IOException;
 import java.util.Map;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.view.View;
 
 import com.example.omaapi.SampleHttpd.Response.Status;
 import com.example.omaapi.R;
 
 public class OmaApiService extends Service {
 	public static HttpListener httpd = null;
 	private Context context;
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		context = getApplicationContext();
 		if (httpd == null) {
 			httpd = new HttpListener(this, Integer.parseInt(this.getString(R.string.port)));
 		}
 		try {
 			httpd.start();
 		}
 		catch (IOException ioe) {
       		// error handling
     	}
     return Service.START_NOT_STICKY;
 	}
 
 	@Override
 	public void onDestroy() {
 		if (httpd != null) httpd.stop();
         super.onDestroy();
 	}
 
     /**
      * Class for clients to access.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with
      * IPC.
      */
     public class LocalBinder extends Binder {
         OmaApiService getService() {
             return OmaApiService.this;
         }
     }
     
     @Override
     public IBinder onBind(Intent intent) {
         return mBinder;
     }   
     
     // This is the object that receives interactions from clients.  See
     // RemoteService for a more complete example.
     private final IBinder mBinder = new LocalBinder();
 
     public class HttpListener extends SampleHttpd {
     	private int http_port;
     	private boolean mc_async_response = false;
     	private String mc_result = "This is supposed to be mc scanning result";
 
         public HttpListener(Service service, int port) {
             super(port);
         	http_port = port;
         }
 
         @Override
         public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
             // System.out.println(method + " '" + uri + "' ");
 
         	int mc = uri.indexOf("/mc");
             int cmapi = uri.indexOf("/cmapi");
 
             String choice = parms.get("choice");
             if (choice != null) {
             	if (choice.equalsIgnoreCase("mc")) mc = 0;
             	if (choice.equalsIgnoreCase("cmapi")) cmapi = 0;
             }

            if (mc==0) {
                 mc_async_response = false;
                 Intent intent = new Intent(context, ScanCodeActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 context.startActivity(intent);
                 while (!mc_async_response) {}            
                 SampleHttpd.Response resp = new SampleHttpd.Response(Status.OK,"text/plain", mc_result);
                 resp.addHeader("Access-Control-Allow-Origin","*");
 
                 return resp;
             }
             else if (cmapi == 0) {     	
                 SampleHttpd.Response resp = new SampleHttpd.Response(Status.OK,"text/html", handleCMAPI());
                 resp.addHeader("Access-Control-Allow-Origin","*");
                 return resp;
             }
      
             String msg = "<html><body><h1>Request to "+method + " '" + uri + "'\n"+
             		"Hello from AT&T HTTP Listener at port " + http_port + "</h1>\n";
 
             msg += "<form action='?' method='get'>\n" +
                    "<h1>Please select:</h1>\n" +
                    "<input type='radio' name='choice' value='mc' checked='yes'/>Mobile Code<br/>\n" +
                    "<input type='radio' name='choice' value='cmapi'/>OpenCMAPI<br/>\n" +
                    "<input type='submit' value='SUBMIT'/>" +
                    "</form>\n";	
     /*
                 if (parms.get("username") == null) {
                     msg += "<form action='?' method='get'>\n" +
                            "<p>Your name: <input type='text' name='username'></p>\n" +
                            "</form>\n";
                 }
                 else {
                 	msg += "<p>How are you, " + parms.get("username") + "?</p>";
                 }
     */
             msg += "</body></html>\n";
                 
             return new SampleHttpd.Response(msg);        	
          }
 
         public void scanResult(String result) {
         	mc_result = result;
         	mc_async_response = true;
         }
         
         private String handleCMAPI() {
         	return "<html><body>\n<h1>You have chosen CMAPI</h1>\n</body></html>\n";
         }
     }	
 }
