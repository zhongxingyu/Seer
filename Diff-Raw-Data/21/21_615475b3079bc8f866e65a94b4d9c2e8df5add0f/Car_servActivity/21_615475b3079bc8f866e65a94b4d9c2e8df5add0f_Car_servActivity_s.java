 package pl.xt.jokii.carserv;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONStringer;
 
 import pl.xt.jokii.adapter.EventAdapter;
 import pl.xt.jokii.db.CarServEntry;
 import pl.xt.jokii.db.CarServProviderMetaData;
 import pl.xt.jokii.db.DbUtils;
 import pl.xt.jokii.db.CarServProviderMetaData.CarServTableMetaData;
 import pl.xt.jokii.db.CarServResultsSet;
 import pl.xt.jokii.utils.ImportExportBackup;
 
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class Car_servActivity extends Activity {
 	private static final String TAG = "Car_servActivity";
 	
 	protected final static  String DB_NAME 				= "mojaBaza";
 	protected final static  String NEW_ENTRY_RES 		= "new_entry";
 	protected final static  String EDIT_ENTRY_RES 		= "edit_entry";
 	public final static  	String SHOW_ITEM_RES 		= "item";
 	protected final static  String ACTION_ITEM_RES 		= "action_item";
 	protected final static  String HEADER_ITEM_RES 		= "header_item";
 	protected final static  String DELETE_ITEM_RES 		= "delete_item";
 	protected final static  String UPDATE_ITEM_RES 		= "update_item";
 	protected final static  int ADD_NEW_ENTRY_REQUEST 	= 1364;						// random number being a key for result
 	protected final static  int DELETE_ENTRY_REQUEST 	= 1365;						// random number being a key for result
 	protected final static  int EDIT_ENTRY_REQUEST 		= 1366;						// random number being a key for result
 	protected final static  int IMPORT_ENTRY_REQUEST 	= 1367;						// random number being a key for result
 	protected final static  int EXPORT_ENTRY_REQUEST 	= 1368;						// random number being a key for result
 	protected final static  int UPDATE_ENTRY_REQUEST 	= 1369;						// random number being a key for result
 	protected final static  int DIALOG_ACTION_DELETE 	= 1;
 	protected final static  int DIALOG_ACTION_IMPORT 	= 2;
 	protected final static  int DIALOG_ACTION_EXPORT 	= 3;
 	private ListView listView							= null;
 	protected static CarServResultsSet resultsSet;
 	private ArrayList<CarServEntry> listEntries 		= new ArrayList<CarServEntry>();
 	private DbUtils dbUtils;
 	private SorOrder mLastSortOrder = SorOrder.DATE_ASC;
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);   	    	        
         setContentView(R.layout.main);
         
         dbUtils = new DbUtils(getContentResolver());								// Set content resolver for DbUtils class
         this.listView = (ListView)findViewById(R.id.listView1);       
         
        	     
     	resultsSet = dbUtils.retrieveResultSet();
     	
 		 // If empty create new empty set
 		 if(resultsSet == null){
 			 resultsSet = new CarServResultsSet();
 			 resultsSet.init();
 		 }
     
     	// Adding dummy entry
 //    	 if(resultsSet == null)
 //         {
 //        	 Log.v("cursor", "NO DATA, creating some");	        	         	 				 				 
 //			 
 //			 CarServEntry carServEntryTmp = new CarServEntry();
 //			 
 //			 carServEntryTmp.setType(0);
 //			 carServEntryTmp.setHeader("Pierwszy");
 //			 carServEntryTmp.setMileage(99999);
 //			 carServEntryTmp.setDate(1262304000000L);
 //			 carServEntryTmp.setExpired(true);
 //			 
 //			 dbUtils.insertEntryDB(carServEntryTmp);				 
 //			 
 //			 // Then read again from data base
 //			 resultsSet = dbUtils.retrieveResultSet();
 //			 
 //			 // If something goes wrong create new empty set
 //			 if(resultsSet == null)
 //			 {
 //				 resultsSet = new CarServResultsSet();
 //				 resultsSet.init();
 //				 Log.e("WARNING", "resultsSet wasn't properly initialized!");
 //			 }				 		        
 //         }
          
          
          this.listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 				public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
 
 					Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
 					intent.putExtra(DELETE_ITEM_RES, resultsSet.getEntries().get(position).getId());
 					intent.putExtra(HEADER_ITEM_RES, getResources().getString(R.string.delete_dialog_text));
 					intent.putExtra(ACTION_ITEM_RES, DIALOG_ACTION_DELETE);
 					startActivityForResult(intent, DELETE_ENTRY_REQUEST);
 					return true;
 				}
 		});
          
          this.listView.setOnItemClickListener(new OnItemClickListener() {
 
 				public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
 					
 					long idT = resultsSet.getEntries().get(position).getId();
 					Intent intent = new Intent(getApplicationContext(), EntryDetail.class);
 					
 					intent.putExtra(SHOW_ITEM_RES, idT);
 					
 					Log.v("Show detail", idT + ", position: "+position);
 					
 					startActivity(intent);
 				}
 		});
          
         listEntries.clear();	    
         if(resultsSet != null){
         	listEntries.addAll(resultsSet.getEntries());
         }
          
      	EventAdapter ea = new EventAdapter(listEntries, getLayoutInflater());
      	ea.setResource(getResources());
      	this.listView.setAdapter(ea);	                      
     }
     
     
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
 
 	    // If the request went well (OK) and the request was ADD_NEW_ENTRY_REQUEST
 	    if (resultCode == Activity.RESULT_OK) 
 	    {	
 	    	if(requestCode == ADD_NEW_ENTRY_REQUEST)
 	    	{
 		    	// Add the newest on Top
 		    	long newId = data.getLongExtra(NEW_ENTRY_RES, -1);
 	
 		    	if(newId != -1)
 		    	{
 		    		 Cursor cursor = null;		
 			   	    	 
 			   	    	 // Utworzenie URI na potrzeby zapytania			   	    	
 			   	    	 cursor = getContentResolver().query(Uri.withAppendedPath(CarServProviderMetaData.CarServTableMetaData.CONTENT_URI, newId+""), null, null, null, null);			   	       
 			   	       
 			   	         if(cursor.moveToFirst())			//Metoda zwraca FALSE jesli cursor jest pusty
 			   	         { 		   	        	 
 				        	CarServEntry carServEntryTmp;
 				        	
 				        	carServEntryTmp = new CarServEntry();
 
 				        	carServEntryTmp.setId		(cursor.getInt		(cursor.getColumnIndex(CarServTableMetaData._ID)));
 				        	carServEntryTmp.setHeader	(cursor.getString	(cursor.getColumnIndex(CarServTableMetaData.SERVICE_HEADER)));
 				        	carServEntryTmp.setMileage	(cursor.getInt		(cursor.getColumnIndex(CarServTableMetaData.SERVICE_MILEAGE)));
 				        	carServEntryTmp.setType		(cursor.getInt		(cursor.getColumnIndex(CarServTableMetaData.SERVICE_TYPE)));
 				        	carServEntryTmp.setDate		(cursor.getLong		(cursor.getColumnIndex(CarServTableMetaData.SERVICE_DATE)));
 				        	carServEntryTmp.setExpired  (cursor.getInt		(cursor.getColumnIndex(CarServTableMetaData.SERVICE_EXPIRED)) == 1 );
 
 				        	resultsSet.addEnd(carServEntryTmp);
 //				        	sortByDateDesc();
 				        	sortLastSortOrder();
 			   	         }
 			   	         else
 			   	         {		      
 			   	        	Log.e("ERROR NEW_ENTRY_RES", "cursor pusty");       	 
 			   	         }   	          			 
 		   				cursor.close();
 		    	}		    	
 		    	
 		    	updateListView();
 	    	}
 	    	else if (requestCode == DELETE_ENTRY_REQUEST)
 	    	{
 	    		long EntryId = data.getLongExtra(DELETE_ITEM_RES, 0);
 	    		
     			getContentResolver().delete(Uri.withAppendedPath(CarServProviderMetaData.CarServTableMetaData.CONTENT_URI, EntryId+""), null, null);
 	    		
 	    		resultsSet.deleteEntryId(EntryId);
 //	    		sortByDateDesc();
 	    		sortLastSortOrder();
 	    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.deleted)+"", Toast.LENGTH_SHORT).show();
 	    		
 	    	}else if(requestCode == IMPORT_ENTRY_REQUEST){
 				String readedFromFile = ImportExportBackup.readFromBackupFile();
 				
 				if(readedFromFile == null){
 					Toast.makeText(this, "Can't find/read from backup file: \"" 
 							+ Environment.getExternalStorageDirectory().toString() + "/"
 							+ ImportExportBackup.BACKUP_FILE_NAME +"\"", Toast.LENGTH_LONG).show();
 				}else{
 					Log.i(TAG, "Odczyt z pliku, tekst \""+readedFromFile+"\"");
					
 					ImportExportBackup.restoreFromBackup(this, readedFromFile);
 					resultsSet = dbUtils.retrieveResultSet();
 					updateListView();
 				}
 	    		
 	    	}else if(requestCode == EXPORT_ENTRY_REQUEST){
 	    		ImportExportBackup.writeToBackupFile(ImportExportBackup.createBackup(this));
 	    		Log.i(TAG, "Zapis do pliku");	    		
 	    	}
 	    }
 	    else
 	    {
 	    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.canceled)+"", Toast.LENGTH_SHORT).show();
 	    }    	
     }
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		
 		menu.add(Menu.NONE, Menu.FIRST, 	Menu.FIRST, 	getResources().getString(R.string.import_entries));
     	menu.add(Menu.NONE, Menu.FIRST+1, 	Menu.FIRST+1, 	getResources().getString(R.string.export_entries));
     	
     	// show menu
     	return true;		
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
 		
     	switch(item.getItemId())
     	{   
 	    	// import
 	    	case (Menu.FIRST):
 				intent.putExtra(HEADER_ITEM_RES, getResources().getString(R.string.import_dialog_text));
 				intent.putExtra(ACTION_ITEM_RES, DIALOG_ACTION_IMPORT);
 				startActivityForResult(intent, IMPORT_ENTRY_REQUEST);    		
 		    	break;  
 		    	
 		    // export
 	    	case (Menu.FIRST+1):
 				intent.putExtra(HEADER_ITEM_RES, getResources().getString(R.string.export_dialog_text));
 				intent.putExtra(ACTION_ITEM_RES, DIALOG_ACTION_EXPORT);
 				startActivityForResult(intent, EXPORT_ENTRY_REQUEST);    		    		
 	    		break;    	
 	    		
 	    	default:
 	    		return super.onOptionsItemSelected(item);
     	}
     	
     	return true;
 	}    
 
     
     // BUTTONs ***********************************************************************
     /**
      * Callback method defined by the View
      * "Add new"
      * @param v
      */
     public void onAddNewEntry(View v)
     {
 		Intent intent = new Intent(getApplicationContext(), NewEntry.class);
 		startActivityForResult(intent, ADD_NEW_ENTRY_REQUEST);       	
     } 
     
     /**
      * Callback method defined by the View
      * "Date"
      * @param v
      */
     public void sortByDate(View v)
     {
     	sortByDateDesc();
     }  
     
     /**
      * Callback method defined by the View
      * "Mileage"
      * @param v
      */
     public void sortByMileage(View v)
     {
     	sortByMileageDesc();
     }
     
     /**
      * Callback method defined by the View
      * "Expired"
      * @param v
      */
     public void sortByExpired(View v)
     {
     	sortByExpiredAsc();
     }  
     // BUTTONs *********************************************************************** END
     
     /**
      * Update list view
      */
     private void updateListView()
     {
     	listEntries.clear();  
     	if(resultsSet != null){
     		listEntries.addAll(resultsSet.getEntries());    	
     	}
     	this.listView.invalidateViews();    	
     }
      
     
     /**
      * Sort entries by added date DESC
      */
     private void sortByDateDesc()
     {
     	mLastSortOrder = SorOrder.DATE_DESC;
     	resultsSet.sortByIdDesc();
     	updateListView();
     }  
     
     /**
      * Sort entries by mileage DESC
      */
     private void sortByMileageDesc()
     {
     	mLastSortOrder = SorOrder.MILEAGE_DESC;
     	resultsSet.sortByMileageDesc();
     	updateListView();
     }
     
     /**
      * Sort entries by expired date DESC
      */
 //    private void sortByExpiredDesc()
 //    {
 //    	resultsSet.sortByDateDesc();
 //    	updateListView();
 //    }    
     
     /**
      * Sort entries by expired date ASC
      */
     private void sortByExpiredAsc()
     {
     	mLastSortOrder = SorOrder.EXPIRED_ASC;
     	resultsSet.sortByDateAsc();
     	updateListView();
     }    
     
     @Override
     protected void onResume() {
     	super.onResume();
     	updateListView();
     }
     
     
     private enum SorOrder{
     	DATE_ASC, DATE_DESC, EXPIRED_ASC, EXPIRED_DESC, MILEAGE_DESC, MILEAGE_ASC
     }
     
     
     /**
      * Sort last used sorting method
      */
     private void sortLastSortOrder(){
     	switch(mLastSortOrder){
     	case DATE_ASC:
     	case DATE_DESC:
     		sortByDateDesc();
     		break;
     	case EXPIRED_DESC:
     	case EXPIRED_ASC:
     		sortByExpiredAsc();
     		break;
     	case MILEAGE_DESC:
     	case MILEAGE_ASC:
     		sortByMileageDesc();
     		break;
 		default:
 			break;
     	}
    } 
     
 }
