 package open.gtaskdroid.activity;
 
 
 import open.gtaskdroid.adaptors.ListCursorAdapter;
 import open.gtaskdroid.dataaccess.EventsDbAdapter;
 import open.Gtaskdroid.R;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 
 
 public class EventListActivity extends ListActivity {
 	
 	private static final int ACTIVITY_CREATE=0;
 	private static final int ACTIVITY_EDIT=1;
 	private static final int EVENT_COMPLETED = 1;
 	private static final int EVENT_INCOMPLETE= 0;
 	
 	private EventsDbAdapter mDbHelper;
    
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.event_list);        
         
         mDbHelper=new EventsDbAdapter(this);
         mDbHelper.open();
         fillData();
                 
         registerForContextMenu(getListView());
     }
     
     
     private void fillData(){
     	
     	Cursor reMinderCursor=mDbHelper.fetchAllEvents();
     	startManagingCursor(reMinderCursor);
     	
     	//create an array to specify fields we want (only the TITLE)
     	String [] from=new String[] {EventsDbAdapter.KEY_TITLE,EventsDbAdapter.KEY_EVENT_START_DATE_TIME, EventsDbAdapter.KEY_STATUS};
     	
     	//And array of the field that want to bind in the view
     	int [] to=new int[]{R.id.taskTitle};
     	
     	//create a simple cursor adaptor and set it to display
     	ListCursorAdapter reminders=new ListCursorAdapter(this, R.layout.event_list_row, reMinderCursor,
     			from, to);
     	
     	setListAdapter(reminders);
     	
     	
     }
     
        
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	MenuInflater mi=getMenuInflater();
     	mi.inflate(R.menu.list_menu, menu);
     	return true;    	
     }
    
     
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
     	switch (item.getItemId()) {
 		case R.id.menu_insert:			
 			createReminder();
 			return true;
 			
 		case R.id.menu_settings:			
 			Intent intent=new Intent(this, TaskPreferencesActivity.class);
 			startActivity(intent);
 			return true;
 			
 		case R.id.menu_google_sync:
 			syncAccount();
 			return true;
 			
 		}
     	return super.onMenuItemSelected(featureId, item);
     }
     
   //handling context menu
 
 	@Override
     public void onCreateContextMenu(ContextMenu menu, View v,
     		ContextMenuInfo menuInfo) {
     	
     	super.onCreateContextMenu(menu, v, menuInfo);   	
     	
     	MenuInflater mi=getMenuInflater();
     	mi.inflate(R.menu.list_menu_item_longpress, menu);
     }
 
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_delete:	    	  		
 	    	setEventDeleteDialog(item);	    	
 			return true;
 		case R.id.menu_mark_as_completed:	    	  		
 			markAsCompleted(item);	    	
 			return true;
 		case R.id.menu_mark_as_incomplete:	    	  		
 			markAsInComplete(item);	    	
 			return true;
 		}
 		
 		
 		
 		
 		return super.onContextItemSelected(item);
 	}
     
 	private void markAsCompleted(final MenuItem item){
 		
 		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
 		mDbHelper.updateEventStatus(info.id, EVENT_COMPLETED);
 		fillData();
 		
 	}
 	
 	private void markAsInComplete(final MenuItem item){
 		
 		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
 		mDbHelper.updateEventStatus(info.id, EVENT_INCOMPLETE);
 		fillData();
 		
 	}
 	
 	//deleting an event
 	private void setEventDeleteDialog(final MenuItem item) {
 		
 		AlertDialog.Builder builder=
     			new AlertDialog.Builder(EventListActivity.this);
     	builder.setMessage(R.string.event_delete_message)
     	.setTitle(R.string.event_delete_title)
     	.setCancelable(false)
     	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
 				mDbHelper.deleteEvent(info.id);
 				fillData();
 				
 			}
 		})
     	.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 				
 			}
 		});
     	builder.create().show();
 	} 
 	
 	
 	
 	
 	//list item clicked
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {      
     	
     	super.onListItemClick(l, v, position, id);
     	Intent intent=new Intent(this,EventEditActivity.class);
     	intent.putExtra(EventsDbAdapter.KEY_ROWID, id);
     	startActivityForResult(intent, ACTIVITY_EDIT);
     	
     }
     
     //new event creating
     private void createReminder() {
 		Intent intent=new Intent(this, EventEditActivity.class);
 		startActivityForResult(intent, ACTIVITY_CREATE);
 		
 	}
     
    private void syncAccount(){
     	Intent intent=new Intent(this, GtaskListActivity.class);
     	startActivityForResult(intent, ACTIVITY_CREATE);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	super.onActivityResult(requestCode, resultCode, intent);
     	fillData();
     	
     }
 }
