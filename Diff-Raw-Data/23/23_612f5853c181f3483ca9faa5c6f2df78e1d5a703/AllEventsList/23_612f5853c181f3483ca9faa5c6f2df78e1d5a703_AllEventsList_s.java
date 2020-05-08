 package fi.vincit.babyschedule.activities;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 import fi.vincit.babyschedule.BabyEvent;
 import fi.vincit.babyschedule.R;
 import fi.vincit.babyschedule.ScheduleDatabase;
 import fi.vincit.babyschedule.adapters.AllEventsListAdapter;
 
 public class AllEventsList extends ListActivity 
 							implements View.OnCreateContextMenuListener {
 	
 	private AllEventsListAdapter mListAdapter;
 	
 	@Override
 	public void onCreate(Bundle b) {
 		Log.d("Babyschedule", "Single action onCreate()");
     	super.onCreate(b);
     	
     	ScheduleDatabase.open(getApplicationContext());            
     	
     	setTitle("List of all events");    
     	
     	mListAdapter = new AllEventsListAdapter(ScheduleDatabase.getAllDbActionsSortedByDate());
     	setListAdapter(mListAdapter);
     	
     	registerForContextMenu(getListView());
 	}
 	
 	@Override
     public void onListItemClick(ListView l, View v, int position, long id) {
 		openEditViewForEvent((BabyEvent)v.getTag());
     }
 	
 	@Override
     public void onStart() {
     	super.onStart();
     	mListAdapter.setActionsList(ScheduleDatabase.getAllDbActionsSortedByDate());
     }
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 									ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.listcontextmenu, menu);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		BabyEvent event = (BabyEvent)info.targetView.getTag();
 		switch (item.getItemId()) {
 		case R.id.delete_activity:			
 			ScheduleDatabase.deleteEntryBasedOnDate(event.getLastAction());
 			mListAdapter.setActionsList(ScheduleDatabase.getAllDbActionsSortedByDate());
 			return true;
 		case R.id.edit_activity:
 			openEditViewForEvent(event);
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 	
 	private void openEditViewForEvent(BabyEvent event) {
 		Intent i = new Intent(this, EventEditor.class);
 		Bundle b = new Bundle();
 		b.putSerializable("EVENT", event);
 		i.putExtras(b);
 		startActivity(i);
 	}
 }
