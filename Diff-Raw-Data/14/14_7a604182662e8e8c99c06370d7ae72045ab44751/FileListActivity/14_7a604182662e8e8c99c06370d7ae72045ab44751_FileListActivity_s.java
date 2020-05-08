 package com.cs456.a2;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 
 /***
  * Activity for going to a new page and letting them see mac addresses
  * and let them click into one if they want the file list from 1 of the places
  * @author Kuen
  *
  */
 public class FileListActivity extends ListActivity {
 	
 	//member variables
 	private SocketClient clientSocket = null;
 	private Map<String,String> MACIPMap = null;
 	private FileListActivity This = this;
 	private Boolean showingFileList = false;
 	private EditText statusText = null;
 	
 	private Handler handle = new Handler();
 	
 	public void onCreate(Bundle b) {
 		super.onCreate(b);
 		setContentView(R.layout.filelist);
 		
 		//get the value passed from deviceComm
 		b = this.getIntent().getExtras(); 
 		
 		// appear in the ListView as the keys
 		final ArrayList<String> key = b.getStringArrayList("keys");
 		MACIPMap = new HashMap<String,String>();
 		
 		//set a helpful text
 		statusText = (EditText)findViewById(R.id.fileListStatus);
 		statusText.setText("Searching for valid MACs");
 		
 		//Querying the server seeing if MACs are valid
 		new Thread(new Runnable() {
 			Boolean error = false;
 			@Override
 			public void run() {
 				//Get all the MAC addresses
 		    	for (int i = 0; i < key.size();i++) {
 		    		String[] content = BLSQuery.query(key.get(i));
 		    		if (content == null) {
 		    			error=true;
 		    			continue;
 		    		}
 		    		
 		    		//If you find it, 
 		    		for (int j = 0; j<content.length; j++) {
 		    			if (content[j]!=null) {
 		    				if (j==1 && !content[j].isEmpty()) {
 		    					MACIPMap.put(key.get(i),content[j]);
 		    				}
 		    			}
 		    		}
 		    	}
 		    	
 		    	//update the UI thread
 		    	handle.post(new Runnable() {
 					@Override
 					public void run() {
 						ArrayList<String> tmp = new ArrayList<String>();
 						tmp.addAll(MACIPMap.keySet());
 						
 						//If there are no valid MAC addresses tell them
 						if (error) {
 							statusText.setText("Query Failed... Are you on WIFI?");
 						} else if (tmp.isEmpty()) {
 							statusText.setText("No valid MAC address\nGo back and search for new BlueTooth devices!");
 						} else {
 							statusText.setText("Click on 1 of the MAC addresses");
 							//Update the UI thread
 							This.setListAdapter(new ArrayAdapter<String>(This, android.R.layout.simple_list_item_1,tmp));
 						}
 						
 					}
 				});
 			}
 		}).start();
 		showingFileList=false;
 	}
 	
 	@Override
     protected void onDestroy() {
         super.onDestroy();
        clientSocket.killThread();
     }
 
 	@Override
 	protected void onListItemClick(final ListView l, final View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		
 		if (!showingFileList) {
 			// Get the item that was clicked
 			Object o = this.getListAdapter().getItem(position);
 			
 			//Getting the client socket
 			if(clientSocket == null || clientSocket.hasQuit()) {
 	    		clientSocket = new SocketClient(this);
 	    		clientSocket.execute(MACIPMap.get(o.toString()));
 	    	}
 	    	
 			statusText.setText("Getting File List");				
 			
 			//This clears the list
 			this.setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new ArrayList<String>()));
 			
 			//Getting the file list in a different thread, to not hang UI thread
 	    	new Thread(new Runnable() {
 				
 				@Override
 				public void run() {
 					String filelist;
 					try {
 						//This waits until the async task is done
 						filelist = (String) clientSocket.get();
 						
 						//splitting the returned string
 						final List<String> files = Arrays.asList(filelist.split("\\n"));
 						
 						handle.post(new Runnable() {
 							@Override
 							public void run() {
 								//Update the UI with appropriate information
 								if (files.isEmpty())
 								{
 									statusText.setText("There are no Files");
 								} else {
 									statusText.setText("Fetch Complete!");
 								}
 								This.setListAdapter(new ArrayAdapter<String>(This, android.R.layout.simple_list_item_1,files));
 								showingFileList = true;
 							}
 						});
 					} catch (InterruptedException e) {
 						handleError(e.getMessage());
 					} catch (ExecutionException e) {
 						handleError(e.getMessage());
 					}				
 				}
 			}).start();
 		}
 	}
 	
 	// Creates a dialog box stating there was an error, and prints the text which the calling code provides it
     protected void handleError(final String error) {
     	handle.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(This);
 				alertBuilder.setMessage(error);
 				alertBuilder.setCancelable(false);
 				
 				alertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 				
 				AlertDialog alert = alertBuilder.create();
 				alert.setTitle("Error!");
 				alert.show();
 			}
 		});
     }
 }
