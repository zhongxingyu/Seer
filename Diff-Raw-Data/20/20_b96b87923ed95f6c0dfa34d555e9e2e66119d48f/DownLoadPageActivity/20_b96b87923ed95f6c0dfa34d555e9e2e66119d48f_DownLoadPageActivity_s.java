 package com.tang.DownLoadPage;
 
 
 
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 
 import control.stage.StageManager;
 
 import second.prototype.ContainerBox;
 import second.prototype.R;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DownLoadPageActivity extends Activity {
     /** Called when the activity is first created. */
 	private StageManager manager = ContainerBox.stageManager;
 	
 	private ListView list;
 	private Button refresh;
 	private Button back;
 	  
 	
 	private ProgressDialog progressDialog;
 
 	private JSONArray scriptList;
 	
 	private int selected;
 	
 	private SimpleAdapter MyAdapter;
     private ArrayList<HashMap<String,Object>> listItem;

 	
 	public void findView()
 	{
 		list = (ListView)findViewById(R.id.onLinelist);
 		refresh = (Button)findViewById(R.id.refresh);
 		back = (Button)findViewById(R.id.back);
 		
 	}
 
 	
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.downloadmode);
         findView();
        
          listItem = new  ArrayList<HashMap<String,Object>>();
 	     MyAdapter = new SimpleAdapter(this,
 	        		listItem,
 	        		R.layout.stageitem,
 	        		new String[]{"item Title","item Text"},
 	        		new int[]{R.id.Name,R.id.Description}
 	        		);
 	 	 list.setAdapter(MyAdapter);
 	 	 list.setOnItemClickListener(onClickListItem); 
 	 	
 	 	
 		 	
 	 	 
 	 	 refresh();
 	 	
 	 	 
 	 	 
 	 	 
 		 back.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				DownLoadPageActivity.this.finish();				
 			}});
      
 		 refresh.setOnClickListener(new OnClickListener(){
 
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					refresh();
 				}});
 		
 	
 	
 	}
 
 	public void refresh()
 	{
 		 
 	 if(haveInternet())
 	 {	 
 		 listItem.clear();
 		 
 		 Thread thread = new Thread()
 		 {
 		     @Override
 		     public void run() {
 		    	 
 		    	 try {
 		    		 String receivedMsg = MyServer.sendPostData("scripts");
 					 JSONObject json = new JSONObject(receivedMsg);
 					 scriptList = json.getJSONArray("Scripts");
 					 
 					 DownLoadPageActivity.this.runOnUiThread(new Runnable(){
 					            @Override
 								public void run() {
 									// TODO Auto-generated method stub
 									updateList();
 									}
 			         });
 					            
 		   	 
 		    	 } catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		    	 
 		     }
 		 };
 		 thread.start();
 	  	}
 	 	else
 	 	{
 	 		Toast.makeText(DownLoadPageActivity.this,"No Internet Detected!! Please turn the wifi on",Toast.LENGTH_SHORT)
 	        .show();
 	 	}
 	}
 	
 	public void updateList()
 	{
 		list.setVisibility(View.VISIBLE);
 		try
 		{
 					
 	 	  for(int i = 0; i < scriptList.length(); i++)
         {
 	 		 String name = scriptList.getJSONObject(i).getString("Name");
 			 String description = scriptList.getJSONObject(i).getString("Description");
 			 String activate = scriptList.getJSONObject(i).getString("Activate");
	 		if(activate.equals("YES"))
	 		{
 	 			HashMap<String,Object> map = new HashMap<String,Object>();
 	 			map.put("item Title", name);
 	 			map.put("item Text", description);
 	 			listItem.add(map);
 	 			MyAdapter.notifyDataSetChanged();
	 		}
           
 	 	  
 		}} catch (JSONException e)
 		{
 			e.fillInStackTrace();
 		}
 		
 	
 	}
 
 	private OnItemClickListener onClickListItem = new OnItemClickListener()
 	{
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
 		{
 			// TODO Auto-generated method stub
 			LayoutInflater inflater = LayoutInflater.from(DownLoadPageActivity.this);  
 	        View textEntryView = inflater.inflate(R.layout.listdialog, null);  
 	        TextView showId=(TextView)textEntryView.findViewById(R.id.ShowID);
 	        TextView showName=(TextView)textEntryView.findViewById(R.id.ShowName);
 	        TextView showDescription=(TextView)textEntryView.findViewById(R.id.ShowDescription);
 	        AlertDialog.Builder builder = new AlertDialog.Builder(DownLoadPageActivity.this);  
 	        builder.setCancelable(false);  
 	        builder.setView(textEntryView);  
	        selected = arg2;      
 	        
 	        try {
 	        	builder.setTitle(scriptList.getJSONObject(selected).getString("Name"));
 				showDescription.setText(scriptList.getJSONObject(selected).getString("Description"));
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	        
 	        builder.setNegativeButton("Cancel",  
 	                new DialogInterface.OnClickListener() {  
 	                    public void onClick(DialogInterface dialog, int whichButton) {  
 	                        setTitle("");  
 	                    }  
 	        });
 	        
 	        builder.setPositiveButton("DownLoad",downLoadListener);
 	        
 	        
 	        
 	               
 	        builder.show();
 	        
 	        
 		}
 	};
 	
 	
 	private DialogInterface.OnClickListener downLoadListener = new DialogInterface.OnClickListener()
 	{
 		private int progress = 0;
 		
 		@Override
 		public void onClick(DialogInterface arg0, int arg1) {
 			// TODO Auto-generated method stub
 			Thread thread = new Thread()
 			 {
 			     @Override
 			     public void run() {
 			    	 
 			    	 try {
 			    		     DownLoadPageActivity.this.runOnUiThread(new Runnable(){
 					            @Override
 								public void run() {
 									// TODO Auto-generated method stub
 					            	progressDialog = ProgressDialog.show(DownLoadPageActivity.this, "DownLoad....", "Please Wait", true, false);
 									}
 			                    });
 			    		 
 			    		    
 
 			    		    String requireID = scriptList.getJSONObject(selected).getString("ID");
 			    		    String postMsg = "scripts/"+requireID;		   	 
 			    		    String receivedMsg = MyServer.sendPostData(postMsg);
 			    		    MyServer.writeInFile(DownLoadPageActivity.this, "100"+requireID, receivedMsg);
 			    		    manager.importStage("100"+requireID);
 			    		    manager.commit();
 			    		    
 			    		    
 			    		    
 			    		    DownLoadPageActivity.this.runOnUiThread(new Runnable(){
 					            @Override
 								public void run() {
 									// TODO Auto-generated method stub
 					            	progressDialog.dismiss();
 									}
 			                    });
 			    		    
 			    		        		    
 			    		    
 			    	    } catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					    }
 			    	 
 			     }
 			 };
 			 thread.start();
 	    }
 	};
 
 
 
 	private boolean haveInternet()
     {
     	boolean result = false;
     	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
     	NetworkInfo info=connManager.getActiveNetworkInfo();
     	if (info == null || !info.isConnected())
     	{
     		result = false;
     	}
     	else 
     	{
     		if (!info.isAvailable())
     		{
     			result =false;
     		}
     		else
     		{
     			result = true;
     		}
     	}
     	
     	return result;
     }
 
 
 
 }
