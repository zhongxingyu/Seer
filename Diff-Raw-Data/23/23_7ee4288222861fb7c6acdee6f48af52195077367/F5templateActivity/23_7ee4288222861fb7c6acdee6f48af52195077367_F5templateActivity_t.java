 package com.flow5.template;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import android.content.res.Resources;
 import android.content.res.XmlResourceParser;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Xml;
 
 import com.flow5.framework.MDNSResolver;
 import com.phonegap.*;
 import com.phonegap.api.LOG;
 
 public class F5templateActivity extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	LOG.setLogLevel("VERBOSE");
         super.onCreate(savedInstanceState);
                 
         final F5templateActivity activity = this;
         
         new Thread(new Runnable() {
 			@Override
 			public void run() {
 				String devservhostname = "unknown.local";
 				
				XmlPullParser xpp = getApplicationContext().getResources().getXml(R.xml.f5gen);				
 				try {
 					while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {						 
 				        if (xpp.getEventType() == XmlPullParser.START_TAG) {	
 				        	if (xpp.getName().equals("devservhost")) {
 				        		LOG.d("F5", xpp.getName());
 				        		devservhostname = xpp.getAttributeValue(null, "name");
 					        	break;
 				        	}
 				        }				 
 				        xpp.next();				 
 					}									
 				} catch (Exception e) {
 					LOG.d("F5", e.getMessage());
 				}
 				
 				MDNSResolver resolver = new MDNSResolver();               
                 final String address = resolver.resolve(devservhostname);
                 
                 activity.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						String url = "http://" + address + ":8008/generate?app=jitc&native=true&inline=true&debug=true";
 						LOG.d("F5", url);
 		                activity.loadUrl(url);					
 					}
                 	
                 });				
 			}        	
         }).start();
     }
 }
