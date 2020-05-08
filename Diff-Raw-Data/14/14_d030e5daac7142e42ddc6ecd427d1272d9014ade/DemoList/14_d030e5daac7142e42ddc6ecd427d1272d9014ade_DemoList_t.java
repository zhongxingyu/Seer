 package org.racenet.racesow;
 
 import java.io.File;
 
 import org.racenet.framework.AndroidFileIO;
 import org.racenet.racesow.models.DemoAdapter;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.support.v4.view.ViewPager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 /**
  * Shows pages of lists of map-highscores
  * 
  * @author soh#zolex
  *
  */
 public class DemoList extends ListActivity {
 
 	WakeLock wakeLock;
 	ViewPager viewPager;
 	DemoAdapter adapter;
 	AndroidFileIO fileIO;
 	short orderBy = AndroidFileIO.ORDER_CREATED;
 	
     @Override
     /**
      * Acquires wakelock and creates an instance of
      * the View-Pager and corresponding adapter
      */
     public void onCreate(Bundle savedInstanceState) {
         
     	super.onCreate(savedInstanceState);
     	
     	PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
     	this.wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "racesow");
     	
     	this.fileIO = new AndroidFileIO(getAssets());
     	this.adapter = new DemoAdapter(this, this.fileIO, this.orderBy);
     	
    	if (this.adapter.isEmpty()) {
    		
    		Toast.makeText(this, "No demos recorded yet.", Toast.LENGTH_LONG).show();
    	}
    	
     	setListAdapter(this.adapter);
         setContentView(R.layout.listview);
         
         ListView list = getListView();
         registerForContextMenu(list);
         
         list.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 			
 				final String demo = (String)DemoList.this.adapter.getItem(arg2);
 				new AlertDialog.Builder(DemoList.this)
 		            .setMessage("Do you want to play the demo '" + demo +"'?")
 		            .setPositiveButton("Play", new OnClickListener() {
 						
 						public void onClick(DialogInterface arg0, int arg1) {
 							
 							Intent i = new Intent(DemoList.this, Racesow.class);
 							i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 							i.putExtra("screen", "demo");
 							i.putExtra("demo", demo);
 							startActivityForResult(i, 0);
 						}
 					})
 					.setNegativeButton("No", null)
 		            .show();
 			}
 		});
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
     	
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
         menu.setHeaderTitle((String)this.adapter.getItem(info.position));
         menu.add(0, 0, 0, "Delete Demo");
         menu.add(0, 1, 1, "Rename Demo");
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	
       final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
       int menuItemIndex = item.getItemId();
       final String fileName = (String)DemoList.this.adapter.getItem(info.position);
       final String demoDir = "racesow" + File.separator + "demos" + File.separator;
       
       switch (menuItemIndex) {
    
       	  case 0:
 	    	  new AlertDialog.Builder(this)
 		        .setMessage("Do you really want to delete this demo?")
 		        .setPositiveButton("Yes", new OnClickListener() {
 					
 					public void onClick(DialogInterface arg0, int arg1) {
 						
 						String message;
 						if (DemoList.this.fileIO.deleteFile(demoDir + fileName)) {
 							
 							message = "Deleted demo '" + fileName + "'";
 							DemoList.this.adapter = new DemoAdapter(DemoList.this, DemoList.this.fileIO, DemoList.this.orderBy);
 							DemoList.this.getListView().setAdapter(DemoList.this.adapter);
 							DemoList.this.getListView().setSelection(info.position > 0 ? info.position - 1 : 0);
 
 						} else {
 							
 							message = "Could not delete '" + fileName + "'";
 						}
 						
 						Toast.makeText(DemoList.this, message, Toast.LENGTH_SHORT).show();
 					}
 				})
 				.setNegativeButton("No", null)
 		        .show();
 	    	  break;
 	    	  
       	  case 1:
       		  final EditText input = new EditText(DemoList.this);
       		  input.setText(fileName);
       		  new AlertDialog.Builder(this)
       		      .setMessage("Rename '"+ fileName + "'")
       		  	  .setView(input)
       		  	  .setPositiveButton("OK", new OnClickListener() {
 					
 					public void onClick(DialogInterface dialog, int which) {
 								
 						String message;
 						String newName = input.getText().toString();
 						if (!newName.endsWith(".r2d")) {
 							
 							newName += ".r2d";
 						}
 						
 						if (newName.contains("/") || newName.contains("\\")) {
 							
 							message = "Invalid filename";
 							
 						} else {
 						
 							if (DemoList.this.fileIO.renameFile(demoDir + fileName, demoDir + newName)) {
 								
 								message = "Demo renamed to '" + newName + "'";
 
 								DemoList.this.adapter = new DemoAdapter(DemoList.this, DemoList.this.fileIO, DemoList.this.orderBy);
 								DemoList.this.getListView().setAdapter(DemoList.this.adapter);
 								int newPosition = DemoList.this.adapter.demos.indexOf(newName);
 								DemoList.this.getListView().setSelection(newPosition);
 								
 							} else {
 								
 								message = "Could not rename demo to '" + newName +"'";
 							}
 						}
 						
 						Toast.makeText(DemoList.this, message, Toast.LENGTH_SHORT).show();
 					}
 				})
 				.setNegativeButton("Cancel", null)
 				.show();
       		  break;
       }
       return true;
     }
     
     @Override
 	/**
 	 * Show options for the demo list
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.demos, menu);
 	    
 	    menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 	    	
 	    	public boolean onMenuItemClick(MenuItem arg0) {
 	    		
 	    		new AlertDialog.Builder(DemoList.this)
 	            .setMessage("Do you really want to delete all your demos?")
 	            .setPositiveButton("Yes", new OnClickListener() {
 					
 					public void onClick(DialogInterface arg0, int arg1) {
 						
						String[] demos = DemoList.this.fileIO.listFiles("racesow" + File.separator + "demos", AndroidFileIO.ORDER_NAME);
						int length = demos.length;
						for (int i = 0; i < length; i++) {
						
							DemoList.this.fileIO.deleteFile("racesow" + File.separator + "demos" + File.separator + demos[i]);
						}						
						
 						DemoList.this.adapter = new DemoAdapter(DemoList.this, DemoList.this.fileIO, DemoList.this.orderBy);
 						DemoList.this.getListView().setAdapter(DemoList.this.adapter);
 					}
 				})
 				.setNegativeButton("No", null)
 	            .show();
 	    		
 	    		return true;
 	    	}
 	    });
 	    
 	    menu.getItem(1).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 	    	
 	    	public boolean onMenuItemClick(MenuItem arg0) {
 	    		
 	    		DemoList.this.orderBy = AndroidFileIO.ORDER_NAME;
 	    		DemoList.this.adapter = new DemoAdapter(DemoList.this, DemoList.this.fileIO, DemoList.this.orderBy);
 				DemoList.this.getListView().setAdapter(DemoList.this.adapter);
 	    		return true;
 	    	}
 	    });
 	    
 	    menu.getItem(2).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			
 			public boolean onMenuItemClick(MenuItem arg0) {
 				
 				DemoList.this.orderBy = AndroidFileIO.ORDER_CREATED;
 	    		DemoList.this.adapter = new DemoAdapter(DemoList.this, DemoList.this.fileIO, DemoList.this.orderBy);
 				DemoList.this.getListView().setAdapter(DemoList.this.adapter);
 	    		return true;
 			}
 		});
 	    
 		return true;
 	}
     
     /**
 	 * Acquire the wakelock on resume
 	 */
     public void onResume() {
     	
     	super.onResume();
     	this.wakeLock.acquire();
     }
     
     /**
      * Release the wakelock when leaving the activity
      */
     public void onDestroy() {
     	
     	super.onDestroy();
     	this.wakeLock.release();
     }
     
     /**
      * Disable animations when leaving the activity
      */
 	public void onBackPressed() {
     	
     	this.finish();
     	this.overridePendingTransition(0, 0);
     }
 }
