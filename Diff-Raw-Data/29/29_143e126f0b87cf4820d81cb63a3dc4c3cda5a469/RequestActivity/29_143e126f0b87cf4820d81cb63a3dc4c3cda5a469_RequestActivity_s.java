 /* 
   Copyright 2010 Duncan Mac-Vicar P. <dmacvicar@suse.de>
  
   This file is part of the openSUSE Build Service Android Client.
 
   openSUSE Build Service Android Client is free software: you
   can  redistribute it and/or modify it under the terms of the GNU
   General Public License as published by the Free Software Foundation,
   either version 3 of the License, or (at your option) any later
   version.
 
   openSUSE Build Service Android Client is distributed in the hope
   that it will be useful, but WITHOUT ANY WARRANTY; without even the
   implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.opensuse.android.obs;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.res.Resources;
import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Spannable;
 import android.text.style.BackgroundColorSpan;
 import android.util.Log;
import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import org.opensuse.android.XmlPersister;
 import org.opensuse.android.obs.data.Request;
 import org.opensuse.android.obs.data.Request.Action;
 import org.opensuse.android.obs.data.Request.Action.Type;
 import org.simpleframework.xml.Serializer;
 
 public class RequestActivity extends Activity {
 	private ProgressDialog progressDialog = null; 
     private Request request = null;
     private Runnable viewRequest;
     
     private String diff;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.request);
         
         viewRequest = new Runnable(){
             @Override
             public void run() {
                 getRequest();
             }
         };
         Thread thread =  new Thread(null, viewRequest, "MagentoBackground");
         thread.start();
         progressDialog = ProgressDialog.show(RequestActivity.this,    
               "Please wait...", "Retrieving data ...", true);
     }
     
     private Runnable returnRes = new Runnable() {
         @Override
         public void run() {
             if(request != null) {
             	
             	TextView descView = (TextView) findViewById(R.id.descView);
             	TextView diffView = (TextView) findViewById(R.id.diffView);
             	
             	descView.setText(request.getDescription());
             	// Apply some styles
     			Resources res = getResources();
             	// now apply some color to the diff        	    			
     			diffView.setText(diff, TextView.BufferType.SPANNABLE);
     			// Apply some styles
     			//Resources res = m_context.getResources();
     			Spannable str = (Spannable) diffView.getText();
     			// Separate the diff by lines
     			String[] diffContents = diff.split("\n");
     			for(String diffLine : diffContents){
     				if(diffLine.startsWith("+")){
     					// TODO: Using indexOf here might be problems if there are two identical lines (it'll only pick the first one), should use the first line of the diff to see changed line number
     					str.setSpan(new BackgroundColorSpan(res.getColor(R.color.addedText)), diff.indexOf(diffLine), diff.indexOf(diffLine) + diffLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
     				} else if(diffLine.startsWith("-")){
     					str.setSpan(new BackgroundColorSpan(res.getColor(R.color.removedText)), diff.indexOf(diffLine), diff.indexOf(diffLine) + diffLine.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
     				}
     			}            	
             }
             progressDialog.dismiss();
         }
     };
     
 	private void getRequest(){
         try {
         	// see if we got the data as xml
         	Bundle extras = getIntent().getExtras();
         	if (extras.containsKey("xml")) {
         		String xml = extras.getString("xml");
         		Serializer serializer = new XmlPersister();
         		request = serializer.read(Request.class, xml);        		
         	}
         	else
         	{
         		String id = extras.getString("id");        		
         		Client client = new Client(this);
                 request = client.getRequest(id);                
         	}
             
         	// get the diff
         	diff = "";
         	// set the diff of the request
         	for (Action action : request.getActions() ) {
         		if (action.getType() == Type.SUBMIT &&
         		    action.getTarget() != null) {
         			Client client = new Client(this);
         			diff += client.getDiff(action.getSource().getProject(),
         					action.getSource().getPackage(),
         					action.getTarget().getProject(),
         					action.getTarget().getPackage(),
         					action.getSource().getRev());
         		}
         	}
         	
         	// Replace tabs with two spaces so more file fits on the screen
         	diff = diff.replaceAll("\t", "  ");
         	
           } catch (Exception e) { 
             Log.e("BACKGROUND_PROC", e.getMessage());
           }
           runOnUiThread(returnRes);
       }
 	
 	  
 }
