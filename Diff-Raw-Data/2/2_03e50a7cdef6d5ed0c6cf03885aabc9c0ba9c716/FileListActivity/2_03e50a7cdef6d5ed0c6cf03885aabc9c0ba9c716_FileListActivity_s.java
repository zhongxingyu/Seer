 package com.cs456.a2;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class FileListActivity extends ListActivity {
 	private SocketClient clientSocket = null;
 	private Map<String,String> MACIPMap = null;
 	private ArrayAdapter<String> listing = null;
 	private FileListActivity This = this;
 	
 	private EditText statusText = null;
 	
 	private Handler handle = new Handler();
 	
 	public void onCreate(Bundle b) {
 		super.onCreate(b);
 		setContentView(R.layout.filelist);
 		b = this.getIntent().getExtras(); 
 		// Create an ArrayAdapter, that will actually make the Strings
 		// appear in the ListView
 		final ArrayList<String> key = b.getStringArrayList("keys");
 		MACIPMap = new HashMap<String,String>();
 		
 		//listing = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, key);
 		//this.setListAdapter(listing);
 		statusText = (EditText)findViewById(R.id.fileListStatus);
 		statusText.setText("Searching for appropriate MACs");
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 		    	for (int i = 0; i < key.size();i++) {
 		    		String[] content = BLSQuery.query(key.get(i));
 		    		if (content == null) {
 		    			//ERROR::
 		    			continue;
 		    		}
 		    		
 		    		for (int j = 0; j<content.length; j++) {
 		    			if (content[j]!=null) {
 		    				if (j==1 && !content[j].isEmpty()) {
 		    					MACIPMap.put(key.get(i),content[j]);
 		    				}
 		    			}
 		    		}
 		    	}
 		    	
 		    	handle.post(new Runnable() {
 					
 					@Override
 					public void run() {
 						ArrayList<String> tmp = new ArrayList<String>();
 						tmp.addAll(MACIPMap.keySet());
 						This.setListAdapter(new ArrayAdapter<String>(This, android.R.layout.simple_list_item_1,tmp));
 					}
 				});
 			}
 		}).start();
 		
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		// Get the item that was clicked
 		Object o = this.getListAdapter().getItem(position);
 		
 		if(clientSocket == null || clientSocket.hasQuit()) {
    		clientSocket = new SocketClient(null);
     		clientSocket.execute(MACIPMap.get(o.toString()));
     	}
     	//handle.post(new Runnable() {
 			
 		//	@Override
 		//	public void run() {
 				statusText.setText("Getting File List");				
 		//	}
 		//});
     	
 		this.setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new ArrayList<String>()));
 		
     	new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				String filelist;
 				try {
 					filelist = (String) clientSocket.get();
 					final List<String> files = Arrays.asList(filelist.split("\\n"));
 					handle.post(new Runnable() {
 						
 						@Override
 						public void run() {
 							This.setListAdapter(new ArrayAdapter<String>(This, android.R.layout.simple_list_item_1,files));
 						}
 					});
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}				
 			}
 		}).start();
 		}
 }
