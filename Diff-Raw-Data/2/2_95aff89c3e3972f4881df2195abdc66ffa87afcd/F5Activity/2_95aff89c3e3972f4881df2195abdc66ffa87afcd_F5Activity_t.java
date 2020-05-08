 package com.flow5.framework;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import android.os.Bundle;
 
 import com.phonegap.*;
 import com.phonegap.api.LOG;
 
 public class F5Activity extends DroidGap {
     public void onCreate(Bundle savedInstanceState, final int configId) {
     	LOG.setLogLevel("VERBOSE");
         super.onCreate(savedInstanceState);
                 
  //       super.init();
         
         final F5Activity activity = this;
         final MDNSResolver resolver = new MDNSResolver(this); 
         
         // TODO: ONLY DO ON DEBUG
         
         new Thread(new Runnable() {
 			@Override
 			public void run() {
 				String devservhostname = "unknown.local";
 				String appName = "unknown";
 				XmlPullParser xpp = getApplicationContext().getResources().getXml(configId);				
 				try {
 					while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {						 
 				        if (xpp.getEventType() == XmlPullParser.START_TAG) {	
 				        	if (xpp.getName().equals("devservhost")) {
 				        		LOG.d("F5", xpp.getName());
 				        		devservhostname = xpp.getAttributeValue(null, "name");
 				        	}
 				        	if (xpp.getName().equals("app")) {
 				        		LOG.d("F5", xpp.getName());
 				        		appName = xpp.getAttributeValue(null, "name");
 				        	}				        	
 				        }				 
 				        xpp.next();				 
 					}									
 				} catch (Exception e) {
 					LOG.d("F5", e.getMessage());
 				}
 								              
                final String address = null; // resolver.resolve(devservhostname);
                 final String app = appName;
                 
                 activity.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						String url;
 						if (address != null) {
 							url = "http://" + address + ":8008/" + app + 
 									"?native=true&inline=true&debug=true&compress=true&platform=android&mobile=true";
 						} else {
 							url = "file:///android_asset/index.html";							
 						}
 									
 						LOG.d("F5", url);
 		                activity.loadUrl(url);				                		               
 					}
                 	
                 });				
 			}        	
         }).start();
     }
 }
