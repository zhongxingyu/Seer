 /**
  * Copyright (C) 2011 Adriano Monteiro Marques
  *
  * Author:  Zubair Nabi <zn.zubairnabi@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 
 package org.umit.icm.mobile.gui;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import org.apache.http.HttpException;
 import org.umit.icm.mobile.R;
 import org.umit.icm.mobile.aggregator.AggregatorRetrieve;
 import org.umit.icm.mobile.connectivity.ConnectivityService;
 import org.umit.icm.mobile.gui.dialogs.IntervalDialog;
 import org.umit.icm.mobile.gui.dialogs.MapSelectionDialog;
 import org.umit.icm.mobile.gui.dialogs.SuggestionDialog;
 import org.umit.icm.mobile.gui.dialogs.TwitterDialog;
 import org.umit.icm.mobile.p2p.MessageForwardingAggregator;
 import org.umit.icm.mobile.process.Globals;
 import org.umit.icm.mobile.proto.MessageProtos.AgentData;
 import org.umit.icm.mobile.proto.MessageProtos.RequestHeader;
 import org.umit.icm.mobile.proto.MessageProtos.ServiceSuggestion;
 import org.umit.icm.mobile.proto.MessageProtos.WebsiteSuggestion;
 
 import twitter4j.TwitterException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 /**
  * ControlActivity that handles the Control tab.
  * Lets the user manipulate various cross-application parameters.
  */
 
 public class ControlActivity extends Activity {
     /** Called when the activity is first created. */
 	private Button sendButton, intervalButton, scanButton
 	, filterButton, servicesFilterButton, mapSelectionButton,
	enableTwitterButton, aboutButton,bugReportButton;	
 	private ProgressDialog progressDialog;
 		
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);    
         setContentView(R.layout.controlactivity);
         sendButton = (Button) this.findViewById(R.id.selected);
         intervalButton = (Button) this.findViewById(R.id.intervalButton);
         scanButton = (Button) this.findViewById(R.id.scanButton);
         filterButton = (Button) this.findViewById(R.id.filterButton);
         servicesFilterButton = (Button) this.findViewById(R.id.serviceFilterButton);
         mapSelectionButton = (Button) this.findViewById(R.id.mapSelectionButton);
         enableTwitterButton = (Button) this.findViewById(R.id.enableTwitterButton);
        bugReportButton = (Button) this.findViewById(R.id.bugReportButton);
        aboutButton = (Button) this.findViewById(R.id.aboutButton);        
 		scanButton.setText(getString(R.string.scan_text)
        				+" "+ getString(R.string.scan_off));
 		try {
 			if(Globals.runtimeParameters.getTwitter().equals("Off")) {
 				enableTwitterButton.setText(getString(R.string.enable_twitter_button));
 			} else {
 				enableTwitterButton.setText(getString(R.string.disable_twitter_button));
 			}
 		} catch (RuntimeException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		BroadcastReceiver receiver = new BroadcastReceiver() {
 		   
 			@Override
 			public void onReceive(Context context, Intent intent) {				
 				if(intent.getAction().equals("org.umit.icm.mobile.CONTROL_ACTIVITY")) {
 					scanButton.setText(getString(R.string.scan_text)
 		       				+" "+ getString(R.string.scan_on));
 			      }				
 			}
 		  };
 
 		registerReceiver(receiver
 				, new IntentFilter("org.umit.icm.mobile.CONTROL_ACTIVITY"));
    	   	   	        
         sendButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  	       		
 	       		SuggestionDialog suggestionDialog = 
 	       			new SuggestionDialog(ControlActivity.this, "", new OnReadyListener());
 	            suggestionDialog.show();	        		
 	       	}
 
 	    }  );
         
         enableTwitterButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  
 	       		try {
 					if(Globals.runtimeParameters.getTwitter().equals("Off")) {
 						  progressDialog = ProgressDialog.show(ControlActivity.this, 
 					        		getString(R.string.loading)	, getString(R.string.retrieving_website)
 					        		, true, false);
 						new LaunchBrowser().execute();  
 						TwitterDialog twitterDialog = 
 							new TwitterDialog(ControlActivity.this, "");
 						twitterDialog.show();	     
 						enableTwitterButton.setText(getString(R.string.disable_twitter_button));
 					} else {
 						Globals.runtimeParameters.setTwitter("Off");
 						enableTwitterButton.setText(getString(R.string.enable_twitter_button));
 					}
 				} catch (RuntimeException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	       		  
 	       	}
 
 	    }  );
         
         mapSelectionButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  	       		
 	       		MapSelectionDialog MapSelectionDialog = 
 	       			new MapSelectionDialog(ControlActivity.this, "");
 	            MapSelectionDialog.show();	        		
 	       	}
 
 	    }  );
         
         filterButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  	       				            		 
 	       		Intent intent = new Intent(ControlActivity.this, WebsiteFilterActivity.class);	       		
 	            startActivity(intent); 
 	       	}
 
 	   	}  );
         
         servicesFilterButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  	       				            		 
 	       		Intent intent = new Intent(ControlActivity.this, ServiceFilterActivity.class);	       		
 	            startActivity(intent); 
 	       	}
 
 	   	}  );
         
         
         intervalButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  	       			       		       			
 	       		IntervalDialog intervalDialog = 
 	       			new IntervalDialog(ControlActivity.this, "", new OnReadyIntervalListener());
 	            intervalDialog.show(); 
 	       		
 	       	}
 
 	   	}  );
        bugReportButton.setOnClickListener(new OnClickListener() { 
	       	public void onClick(View v) {  	       		
	       		Intent intent = new Intent(ControlActivity.this, BugReportActivity.class);
	       		startActivity(intent);
	       	}

	    }  );
         
         aboutButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  	       			       		       			
 	       		AlertDialog alertDialog = new AlertDialog.Builder(ControlActivity.this).create();
 	       		alertDialog.setTitle(getString(R.string.about_button));
 	       		alertDialog.setMessage(getString(R.string.about_text));
 	       		alertDialog.setIcon(R.drawable.umit_128);
 	       		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int which) {
 		            	 
 		            	dialog.dismiss();
 		          
 		             } });
 	       		alertDialog.show();
 	       		
 	       	}
 
 	   	}  );
         
         scanButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {
 	       		if(Globals.scanStatus.equalsIgnoreCase(getString(R.string.scan_on))){
 	       			scanButton.setText(getString(R.string.scan_text)
 		       				+" "+ getString(R.string.scan_on));
 	       			Globals.scanStatus = getString(R.string.scan_off);
 	       			stopService(new Intent(ControlActivity.this, ConnectivityService.class));
 	       		}
 	       			
 	       		else{
 	       			scanButton.setText(getString(R.string.scan_text)
 		       				+" "+ getString(R.string.scan_off));
 	       			Globals.scanStatus = getString(R.string.scan_on);
 	       			startService(new Intent(ControlActivity.this, ConnectivityService.class));
 	       		}
 	       			
 	       		try {
 					Globals.runtimeParameters.setScanStatus(Globals.scanStatus);
 				} catch (RuntimeException e) {
 						e.printStackTrace();
 				}
 	       		
 	       		Context context = getApplicationContext();
         		CharSequence text = getString(R.string.toast_scan_change) 
         		+ " " + Globals.scanStatus;
         		int duration = Toast.LENGTH_SHORT;
 
         		Toast toast = Toast.makeText(context, text, duration);
         		toast.show();
 	       	}
 
 	   	}  );
               
     }
     
     private class OnReadyListener implements SuggestionDialog.ReadyListener {
         @Override
         public void ready(String selection) {            
             StringTokenizer stringTokenizer 
             = new StringTokenizer(selection, "&");
             String option = stringTokenizer.nextToken();
             String suggestion = stringTokenizer.nextToken();
             String email = stringTokenizer.nextToken();
             String host = stringTokenizer.nextToken();
             String ip = stringTokenizer.nextToken();          
             if(option.equals("Website")) {          
             	  Toast.makeText(ControlActivity.this
                   		, getString(R.string.text_selected) 
                   		+ " " + option + " " + suggestion + " " + email
                   		, Toast.LENGTH_LONG).show();
             	new SendWebsiteTask().execute(email, suggestion);
             }
             else {
             	  Toast.makeText(ControlActivity.this
                   		, getString(R.string.text_selected) 
                   		+ " " + option + " " + suggestion + " " + email 
                   		+ " " + host + " " + ip
                   		, Toast.LENGTH_LONG).show();
             	new SendServiceTask().execute(email, suggestion
             			,host, ip);
             }
             
         }
     }
     
     private class OnReadyIntervalListener implements IntervalDialog.ReadyIntervalListener {
         @Override
         public void ready(String interval) {
             Toast.makeText(ControlActivity.this, interval, Toast.LENGTH_LONG).show();
         }
     }
     
     private class SendWebsiteTask extends AsyncTask<String,String,String> {
     	  
     	protected void onPostExecute(String result) {
     		if (result.equals("true")) {
     			int duration = Toast.LENGTH_SHORT;
         		Toast toast = Toast.makeText(ControlActivity.this,
         				getString(R.string.website_suggestion_sent), duration);
         		toast.show();	
     		}
     	}
          
 		protected String doInBackground(String... args) {
 			RequestHeader requestHeader = RequestHeader.newBuilder()
 			.setAgentID(Globals.runtimeParameters.getAgentID())
 			.build();
 			
         	WebsiteSuggestion websiteSuggestion
         	= WebsiteSuggestion.newBuilder()
         	.setWebsiteURL(args[1])
         	.build();
         	
         	if(Globals.aggregatorCommunication == true) {
 				try {
 					if(AggregatorRetrieve.sendWebsiteSuggestion(websiteSuggestion))
 						return "true";
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (RuntimeException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
         	} else if (Globals.p2pCommunication == true) {
         		Iterator<AgentData> iterator 
         		= Globals.runtimesList.getSuperPeersList().iterator();
         		
         		AgentData peer = null;
         		while(iterator.hasNext()) {
         			peer = iterator.next();
         			try {
         				if(Globals.authenticatedPeers.checkPeer(peer)) {
         					MessageForwardingAggregator.forwardWebsiteSuggestion(
     								peer, websiteSuggestion);
         				}
 						
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
         		}
         		return "true";
         	}
         	return "false";
 		}
 			
     }  
     
     private class SendServiceTask extends AsyncTask<String,String,String> {
   	  
     	protected void onPostExecute(String result) {
     		if (result.equals("true")) {
     			int duration = Toast.LENGTH_SHORT;
         		Toast toast = Toast.makeText(ControlActivity.this,
         				getString(R.string.service_suggestion_sent), duration);
         		toast.show();	
     		}
     	}
          
 		protected String doInBackground(String... args) {
 			RequestHeader requestHeader = RequestHeader.newBuilder()
 			.setAgentID(Globals.runtimeParameters.getAgentID())
 			.build();
 			
         	ServiceSuggestion serviceSuggestion
         	= ServiceSuggestion.newBuilder()
         	.setServiceName(args[1])
         	.setHostName(args[2])
         	.setIp(args[3])      
         	.build();
 
         	if(Globals.aggregatorCommunication == true) {
 				try {
 					if(AggregatorRetrieve.sendServiceSuggestion(serviceSuggestion))
 						return "true";
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (RuntimeException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
         	} else if (Globals.p2pCommunication == true) {
         		Iterator<AgentData> iterator 
         		= Globals.runtimesList.getSuperPeersList().iterator();
         		AgentData peer = null;
         		while(iterator.hasNext()) {
         			peer = iterator.next();
         			try {
         				if(Globals.authenticatedPeers.checkPeer(peer)) {
         					MessageForwardingAggregator.forwardServiceSuggestion(
     								peer, serviceSuggestion);
         				}
 						
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
         		}
         		return "true";
         	}
         	return "false";
 		}
 			
     }    
     
     private class LaunchBrowser extends AsyncTask<String,String,String> {
   	  
     	protected void onPostExecute(String str) {  
     		try {
     			Globals.runtimeParameters.setTwitter("Off");
     		} catch (RuntimeException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
     		Globals.twitterUpdate.reset();
     		try {
     			Globals.twitterUpdate.requestToken(ControlActivity.this);
     		} catch (TwitterException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		} catch (IOException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		} catch (HttpException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}		
     		
     		progressDialog.dismiss();
     	}
 
 		@Override
 		protected String doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			return null;
 		}
          										
     }
       	 
 }
