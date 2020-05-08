 package com.example.filemanager;      
 
  
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.StatFs;
import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.webkit.MimeTypeMap;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 
 public class MainActivity  
         extends SherlockListActivity     
   {	     public static ProgressDialog dialog ;//dialog
 
 	  RelativeLayout paste_layout=null;
 	  TextView copyormove;
 	  private ActionMode mActionMode;
      ListView listview;         
     String dir_Path;      
     File[] file_list; //array of items
     FileAdapter Adapter = null;
      @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);           
          setContentView(R.layout.activity_main);
 	      paste_layout =(RelativeLayout)findViewById(R.id.paste_layout);
 
 	      copyormove =(TextView)findViewById(R.id.copyormove);
 
         listview = getListView();     
         dir_Path = Environment.getExternalStorageDirectory().toString();     //initial path
         try {                   
             dir_Path = getIntent().getExtras().getString("path");//get the path from intent
         } catch(NullPointerException e) {}
 
          
          
          
 		try { //this try catch block is to check the exception if we dont have root permission
 			Adapter = new FileAdapter(this, 1, new ArrayList<FileObject>());
 			   file_list = (new File(dir_Path)).listFiles();
 			 for (int i = 0; i < file_list.length; i++) //itrating over the length of file list
 			    Adapter.add(new FileObject(file_list[i])); //adding item to fileobjec
 			 Adapter.sort();
 			 
 		} catch (Exception e) {
 			 new AlertDialog.Builder(this)//opening a dialog box wiht msg
              .setTitle("Need root permission to open this")
              .setNeutralButton("OK", new DialogInterface.OnClickListener(){
                  public void onClick(DialogInterface dialog, int button){
                      onBackPressed();// on press ok come back to home
 
                  	}
              })
              .show();			e.printStackTrace();
 		}
  
          listview.setAdapter(Adapter);
          
          
          listview.setOnItemLongClickListener(new OnItemLongClickListener() {
  			@Override
  			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
  				onListItemCheck(position);
  				//listview.setEnabled(false);
 
  				return true;
  			}
  		});
          setTitle(dir_Path);//setting title 
     }
 
 
      @Override
 	protected void onResume() {
 	      dialog=new ProgressDialog(MainActivity.this);	
 	  	Button paste =(Button)findViewById(R.id.paste);
     	Button cancel =(Button)findViewById(R.id.cancel);
     	
     	
     	
     	
  		    final ArrayList<String> path_recived  ;
 		    if(File_move.path_list!=null){
  		    	path_recived=File_move.path_list;
 		    	
 		    	paste_layout.setVisibility(View.VISIBLE);
 		    	if(File_move.move){
 	    				copyormove.setText("Move here");
 
 		    	}
 		    	else{
 	    				copyormove.setText("Copy here");
 		    		
 		    	}
 		    
 		    	
 		    	paste.setOnClickListener(new View.OnClickListener(){
 		    		public void onClick(View View3) {
 		    			
 		    			if(File_move.move){
 		    				 class file_move_async extends AsyncTask<Void, Void, Integer> {
 		    			    	 
 		    			         protected Integer doInBackground(Void... params) {
 		    			        	 
 		    			        		for(int i=0;i<path_recived.size();i++){
 		    			    				String sourcePath=path_recived.get(i);
 		    			    				File sourc_file = new File(sourcePath);
 		    			    				String filename = null;
 		    			    				int lastslashPosition = sourcePath.lastIndexOf('/');
 		    			    				if( lastslashPosition > 0 ) {
 		    			    					filename = sourcePath.substring(lastslashPosition + 1);
 		    			    				}
 		    			    				File dest_file=new File(dir_Path+"/"+filename);
 
   		    			    				String destcheck=dir_Path+"/"+filename;
  
		    			    				if(destcheck.equals(sourcePath)){}
 		    			    				else{
 		    			    				if(sourc_file.isDirectory()) //if its a directry
 		    			    			        {
  
 		    			    					  try {
 		    										copy_fileordir(sourc_file, dest_file);
 		    	 								} catch (IOException e) {
 		    										// TODO Auto-generated catch block
 		    										e.printStackTrace();
 		    									}
 		    			    					  
 		    			    			         }
 		    			    				  else{
 		    			    				
 		    			    				 
 		    			    				
 		    			    				
 		    			    				try {
 		    			    					
 		    			    					copy_fileordir(sourc_file, dest_file) ; 
 		    	 							} catch (IOException e) {
 		    									// TODO Auto-generated catch block
 		    									e.printStackTrace();
 		    								}
 		    			    				  }
 		    	 					    	
 		    			    			}}
 		    			             return null;
 		    			              
 		    			             
 		    			         }
 
 		    			         
 		    			@Override
 		    			         protected void onPreExecute() {
 		    					dialog.setMessage("Moving please wait");
 		    					dialog.show();
 		    			             super.onPreExecute();
 		    			         }
 
 
 		    				protected void onPostExecute(Integer result) {
 		    					
 		    					for(int i=0;i<path_recived.size();i++){
 		    	    				
 		    	    				String path=path_recived.get(i);
 		    	    				File file = new File(path);
 		    	    				DeleteRecursive(file);
 		    	    				
 		    	    				
 		    	    			}
 		    	    			
 		    	    			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
 		    	    					Uri.parse("file://" + Environment.getExternalStorageDirectory())));//refreshing
 		    	    			
  		       				 	 
 		    					
 		    					
 		    					File_move.path_list = null; //removing list
 			    				File_move.move=false;
 						    	paste_layout.setVisibility(View.GONE);
 						    	dialog.dismiss();
 
 						    	refresh_activity();
 		    			             super.onPostExecute(result);
 		    			         }
 
 
 							private void DeleteRecursive(File fileOrDirectory) {
 								if (fileOrDirectory.isDirectory())
 							        for (File child : fileOrDirectory.listFiles())
 							            DeleteRecursive(child);
 
 							    fileOrDirectory.delete();								
 							}
 		    			     }
 		    				
 		    				
 		    				
 		    		            new file_move_async().execute();//checking api after the values are updated in conf_api
 
 		    				 
 		    				 
 		    				 
 		    				 
 		    				 
 		    				 
 		    				 
 		    			}	else{	    	
  		    					class Copy_file_async extends AsyncTask<Void, Void, Integer> {
 		    			    	 
 		    			         protected Integer doInBackground(Void... params) {
 
 		    			        		for(int i=0;i<path_recived.size();i++){
 		    			    				String sourcePath=path_recived.get(i);
 		    			    				File sourc_file = new File(sourcePath);
 		    			    				String filename = null;
 		    			    				int lastslashPosition = sourcePath.lastIndexOf('/');
 		    			    				if( lastslashPosition > 0 ) {
 		    			    					filename = sourcePath.substring(lastslashPosition + 1);
 		    			    				}
 		    			    				
 		    			    				File dest_file=new File(dir_Path+"/"+filename);
  		    			    				String destcheck=dir_Path+"/"+filename;
  
		    			    				if(destcheck.equals(sourcePath)){}
 		    			    				else{
 		    			    				  if(sourc_file.isDirectory()) //if its a directry
 		    			    			        {
 		    			    					  
 		    	 
 		    			    					  try {
 		    										copy_fileordir(sourc_file, dest_file);
 		    	 								} catch (IOException e) {
 		    										// TODO Auto-generated catch block
 		    										e.printStackTrace();
 		    									}
 		    			    					  
 		    			    			         }
 		    			    				  else{
 		    			    				
 		    			    				
 		    			    				
 		    			    				try {
 		    			    					
 		    			    					copy_fileordir(sourc_file, dest_file) ; 
 		    	 							} catch (IOException e) {
 		    									// TODO Auto-generated catch block
 		    									e.printStackTrace();
 		    								}
 		    			    				
 		    			    				
 		    			    				  }
 		    			    					
 		    			    			}
 		    			    				 
 		    			        		}
 		    			             return null;
 		    			              
 		    			             
 		    			         }
 
 		    			         
 		    			@Override
 		    			         protected void onPreExecute() {
 		    					dialog.setMessage("Copying please wait");
 		    					dialog.show();
 		    			             super.onPreExecute();
 		    			         }
 
 
 		    				protected void onPostExecute(Integer result) {
 		    					
 		    					File_move.path_list = null; //removing list
 			    				File_move.move=false;
 						     	paste_layout.setVisibility(View.GONE);
  						    	dialog.dismiss();
 
 		    	    			refresh_activity();
 	    			             super.onPostExecute(result);
 
 		    			         }
 
 
 						 
 		    			     }
 		    				
 		    				
 		    				
 		    		            new Copy_file_async().execute();//checking api after the values are updated in conf_api
 
 		    				
 		    				
 		    		
 					    	
 		    			}
 		    		
  		    		} });
 
  		    }
 		    else{
 		    	cancel.setOnClickListener(new View.OnClickListener(){
 		    		public void onClick(View View3) {
 		    			File_move.path_list = null; //removing list
 				    	paste_layout.setVisibility(View.GONE);
 
 			    		} });
 		    	paste.setOnClickListener(new View.OnClickListener(){
 		    		public void onClick(View View3) {
 		 	    		Toast.makeText(MainActivity.this, "Select other Directory", Toast.LENGTH_SHORT).show();
 
 
 			    		} });
 		    	
 		    }
 		super.onResume();
 	}
   
      
      
     
  
      
      
 
      public static void copy_fileordir(File sourceLocation, File targetLocation)
     	        throws IOException {
 
     	    if (sourceLocation.isDirectory()) {
     	    	
     	        if (!targetLocation.exists()) {
     	            targetLocation.mkdir();
     	        }
     	         
     	        
     	        String[] children = sourceLocation.list();
     	        for (int i = 0; i < sourceLocation.listFiles().length; i++) {
 
     	            copy_fileordir(new File(sourceLocation, children[i]),
     	                    new File(targetLocation, children[i]));
     	        }
     	    } else {
 
     	        InputStream in = new FileInputStream(sourceLocation);
 
     	        OutputStream out = new FileOutputStream(targetLocation);
 
      	        byte[] buf = new byte[1024];
     	        int len;
     	        while ((len = in.read(buf)) > 0) {
     	            out.write(buf, 0, len);
     	        }
     	        in.close();
     	        out.close();
     	    }
 
     	}
  
 	 
    
  	@Override
  	protected void onListItemClick(ListView l, View v, int position, long id) {		
  		if(mActionMode == null) {//normal list click
 
  			onsingleclick(v);
  		
  		
  		
  		} else
  			// add or remove selection for current list item
  			onListItemCheck(position);		
  	}
  	private void onListItemCheck(int position) {
  		Adapter.toggleSelection(position);
         boolean hasCheckedItems = Adapter.getSelectedCount() > 0;        
 
         if (hasCheckedItems && mActionMode == null)
         	// there are some selected items, start the actionMode
             mActionMode = startActionMode(new ActionModeCallback());
         else if (!hasCheckedItems && mActionMode != null)
         	// there no selected items, finish the actionMode
             mActionMode.finish();
         
 
         if(mActionMode != null)
         	mActionMode.setTitle(String.valueOf(Adapter.getSelectedCount()) + " selected");
     }
  	
  	 public void onsingleclick(View view)
      {
  		 String path = dir_Path + "/" + ((TextView)view.findViewById(R.id.fileName)).getText().toString();
  		 
           File file1 = new File(path);
  	    	paste_layout.setVisibility(View.GONE);
 
        if(file1.isDirectory()) //if its a directry
       {
           
           Intent next = new Intent(MainActivity.this, MainActivity.class);
           next.putExtra("path", path);//putting path
          // finish();
           startActivity(next); //starting it again with new path and show that again 
  		    overridePendingTransition(0,0);
 
        }
        else
       { // otherwise open the file using defult 
           Intent intent = new Intent();
           //using a builtin Intent.
 
            intent.setAction(android.content.Intent.ACTION_VIEW);
 
            File file = new File(path); // defining file
           MimeTypeMap mime = MimeTypeMap.getSingleton();// this is used to open files
           String ext=file.getName().substring(file.getName().lastIndexOf(".")+1); //getting the extension after .
           String type = mime.getMimeTypeFromExtension(ext);  //
          
            intent.setDataAndType(Uri.fromFile(file),type);//when we have mime will put this in intent to open the file
           startActivity(intent);
           
       }
      }
  	
  	
  	private class ActionModeCallback implements ActionMode.Callback {
 
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			// inflate contextual menu	
 			mode.getMenuInflater().inflate(R.menu.contextual, menu);
 			return true;
 		}
 
 		@Override
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {			
 			return false;
 		}
 
 		@Override
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			// retrieve selected items 
  			SparseBooleanArray selected = Adapter.getSelectedIds();
  		    ArrayList<String> paths = new ArrayList<String>();
 
  			for (int i = 0; i < selected.size(); i++){				
 			    if (selected.valueAt(i)) {
 			    	paths.add(Adapter.getItem(selected.keyAt(i)).getFile().getAbsolutePath());
   			    }
  			}
 			
 			switch(item.getItemId()){
 	    	case R.id.copy:
 	    		File_move send_path = new File_move();
 	    		send_path.setpath(paths);
 	    		send_path.setmove(false);
  	    		//Toast.makeText(MainActivity.this, paths+"", Toast.LENGTH_LONG).show();
     			//refresh_activity();
 		    	paste_layout.setVisibility(View.VISIBLE);
 		    	copyormove.setText("Copy here");
 	    		break;
 	    	case R.id.delet:
 	    		
 	    			for(int i=0;i<paths.size();i++){
 	    				
 	    				String path=paths.get(i);
 	    				File file = new File(path);
 	    				DeleteRecursive(file);
 	    				
 	    				
 	    			}
 	    			
 	    			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
 	    					Uri.parse("file://" + Environment.getExternalStorageDirectory())));//refreshing
 	    			
 	    			Toast.makeText(MainActivity.this, "Successfully Deleted", Toast.LENGTH_LONG).show();
 	    			refresh_activity();
 	    		
 	    		break;
 	    	case R.id.move:
 	    		File_move send_path_move = new File_move();
 	    		send_path_move.setpath(paths);
 	    		send_path_move.setmove(true);
 		    	paste_layout.setVisibility(View.VISIBLE);
 		    	copyormove.setText("Move here");
     			//refresh_activity();
 
  	    		
 	    		break;	        		
 	   		
 
 	    	}
 			
 						
 			
 			// close action mode
 			mode.finish();
 			return false;
 		}
 		
 		public void DeleteRecursive(File fileOrDirectory) {
 		    if (fileOrDirectory.isDirectory())
 		        for (File child : fileOrDirectory.listFiles())
 		            DeleteRecursive(child);
 
 		    fileOrDirectory.delete();
 		}
 		@Override
 		public void onDestroyActionMode(ActionMode mode) {
 			// remove selection 
 			Adapter.removeSelection();
 			mActionMode = null;
 		}
 		
 	}
  	public  void refresh_activity(){
 		Intent intent = getIntent();
 		    finish();
 		    startActivity(intent);
 		    overridePendingTransition(0,0);
 
 	}
 
 	private void updateStorageLabel() {
 		long total, aval;
 		int kb = 1024;
 		
 		StatFs fs = new StatFs(Environment.
 								getExternalStorageDirectory().getPath());
 		
 		total = fs.getBlockCount() * (fs.getBlockSize() / kb);
 		aval = fs.getAvailableBlocks() * (fs.getBlockSize() / kb);
 		
 //		storagetext.setText(String.format("sdcard: Total %.2f GB " +
 //							  "\t\tAvailable %.2f GB", 
 //							  (double)total / (kb * kb), (double)aval / (kb * kb)));
 	}
 	@Override
 	public void onBackPressed() {
 //		String sdcard = Environment.getExternalStorageDirectory().toString();     //initial path
 //		if(dir_Path.equals(sdcard)){
 //				finish();
 //		}else{
 //		int lastslashPosition = dir_Path.lastIndexOf('/');
 //
 //		Intent next = new Intent(MainActivity.this, MainActivity.class);
 //        next.putExtra("path", dir_Path.subSequence(0, lastslashPosition));//putting path
 //         startActivity(next); //starting it again with new path and show that again
 //		    overridePendingTransition(0,0);
 //
 //		}
  		super.onBackPressed();
 	}
  
 
  } 
  
