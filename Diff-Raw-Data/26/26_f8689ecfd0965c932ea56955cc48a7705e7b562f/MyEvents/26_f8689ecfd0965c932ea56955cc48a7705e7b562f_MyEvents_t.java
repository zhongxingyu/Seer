 package com.appchallenge.android;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MyEvents extends SherlockListActivity {
 	/**
 	 * The selected event from the list.
 	 */
 	Event selectedEvent;
 	
 	/** Event object for storing the Event to be deleted (persists after selectedEvent is emptied) */
 	Event deletionEvent;
 
 	/**
 	 * Whether or not the context menu is open.
 	 */
 	boolean contextMenuOpen = false;
 
 	/**
      * Provides access to our local sqlite database.
      */
 	LocalDatabase localDB;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_my_events);
 
         // The home button takes the user back to the map display.
         ActionBar bar = getSupportActionBar();
         bar.setDisplayHomeAsUpEnabled(true);
 
         // Restore bundle contents.
         if (savedInstanceState != null) {
         	this.contextMenuOpen = savedInstanceState.getBoolean("contextMenuOpen");
         	this.selectedEvent = savedInstanceState.getParcelable("selectedEvent");
         	this.deletionEvent = savedInstanceState.getParcelable("deletionEvent");
         }
 
     	// Build the list of events.
         this.refreshMyEventsList();
         registerForContextMenu(getListView());
 
         // Persist the context menu state. This required a Runnable to overcome issues
         // with the Activity window not being ready during calls to openContextMenu().
         findViewById(android.R.id.list).post(new Runnable() {
             public void run() {
                 // Reopen the context menu on configuration changes.
                 if (contextMenuOpen)
                     openContextMenu(getListView());
             }
         });
     }
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putBoolean("contextMenuOpen", this.contextMenuOpen);
 		savedInstanceState.putParcelable("selectedEvent", this.selectedEvent);
 		savedInstanceState.putParcelable("deletionEvent", this.deletionEvent);
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		Log.d("MyEvents.onCreateContextMenu", "Context menu created.");
         super.onCreateContextMenu(menu, v, menuInfo);
 
         // Retrieve the Event that was selected.
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        if (this.selectedEvent == null)
            this.selectedEvent = (Event)getListAdapter().getItem(info.position);
 
         // Determine the available actions based on event list section.
         if (this.selectedEvent.getEndDate().before(new Date()))
         	getMenuInflater().inflate(R.menu.my_events_past_context, menu);
         else
             getMenuInflater().inflate(R.menu.my_events_active_context, menu);
 
         this.contextMenuOpen = true;
     }
 
 	public void onContextMenuClosed(Menu menu) {
 		Log.d("MyEvents.onContextMenuClosed", "Context menu closed.");
 	    super.onContextMenuClosed(menu);
 	    this.contextMenuOpen = false;
 	    this.selectedEvent = null;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case android.R.id.home:
 	        	// Handle going back to the EventViewer without recreating it.
                 Intent viewerIntent = new Intent(this, EventViewer.class);
                 if (NavUtils.shouldUpRecreateTask(this, viewerIntent)) {
                     NavUtils.navigateUpTo(this, viewerIntent);
                     finish();
                 } else
                     finish();
                 return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 		// Ensure we have selected an item.
 		if (selectedEvent == null) {
 			Log.e("MyEvents.onContextItemSelected", "No selected event item available.");
 			return super.onContextItemSelected((android.view.MenuItem) item);
 		}
 
 		if (item.getItemId() == R.id.my_events_details) {
 			Intent eventDetails = new Intent(MyEvents.this, EventDetails.class);
 	     	eventDetails.putExtra("event", selectedEvent);
 	     	startActivity(eventDetails);
 		}
 		else if (item.getItemId() == R.id.my_events_delete) {
 	    	this.deletionEvent = selectedEvent;
 	    	new deleteEventAPICaller().execute(selectedEvent);
 	    	return true;
 	    }
 	    else if (item.getItemId() == R.id.my_events_update) {
 	    	// Pass the event to the Edit Event activity.
 	    	Intent editEvent = new Intent(MyEvents.this, EditEvent.class);
 	    	editEvent.putExtra("event", selectedEvent);
 	    	startActivity(editEvent);
 	    	return true;
 	    }
 	    else if (item.getItemId() == R.id.my_events_repost) {
 	    	return true;
 	    }
 	    else if (item.getItemId() == R.id.my_events_forget) {
 	    	// Remove the past event from the cache and refresh the list.
 	    	if (localDB == null)
 	            localDB = new LocalDatabase(this);
 			boolean deleted = localDB.deleteEventFromCache(selectedEvent);
 			if (!deleted)
 				Log.e("deleteEventAPICaller.onPostExecute", "Could not delete event from local cache");
 
 			this.refreshMyEventsList();
 	    	return true;
 	    }
 	    return super.onContextItemSelected((android.view.MenuItem) item);
 	}
 
 	protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         Log.d("MyEvents.onListItemClick", "List item has been clicked.");
 
         Event event = (Event)l.getItemAtPosition(position);
         if (event == null)
         	return;
 
         // Pass information about the event to the details activity.
         Intent eventDetails = new Intent(MyEvents.this, EventDetails.class);
      	eventDetails.putExtra("event", event);
      	startActivity(eventDetails);
 	}
 
 	@Override
     protected void onPause() {
     	super.onPause();
 
     	// Close our database helper if necessary.
     	if (localDB != null)
             localDB.close();
     }
 	
 	protected void onDestroy() {
 		super.onDestroy();
 		closeContextMenu();
 	}
 
 	private void refreshMyEventsList() {
 		if (localDB == null)
             localDB = new LocalDatabase(getApplicationContext());
 
 		// Get a list of the events we have created over time.
     	ArrayList<Event> allMyEvents = localDB.getMyEvents();
     	if (allMyEvents.size() < 1) {
     		findViewById(R.id.my_events_empty).setVisibility(View.VISIBLE);
     		findViewById(android.R.id.list).setVisibility(View.GONE);
     		return;
     	}
 
     	findViewById(R.id.my_events_empty).setVisibility(View.GONE);
     	findViewById(android.R.id.list).setVisibility(View.VISIBLE);
 
     	// Partition the events into active and past lists.
     	ArrayList<Event> activeEvents = new ArrayList<Event>();
     	ArrayList<Event> pastEvents = new ArrayList<Event>();
     	for (Event e : allMyEvents) {
     		if (e.getEndDate().before(new Date()))
     			pastEvents.add(e);
     		else
     			activeEvents.add(e);
     	}
 
     	// Build the list adapter and sections.
 		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
         if (activeEvents.size() > 0) {
 		    EventAdapter active = new EventAdapter(this, R.layout.list_event, activeEvents);
 			adapter.addSection(getResources().getString(R.string.active), active);
 		}
         if (pastEvents.size() > 0) {
 		    EventAdapter past = new EventAdapter(this, R.layout.list_event, pastEvents);
 			adapter.addSection(getResources().getString(R.string.past), past);
 		}
         setListAdapter(adapter);  
 	}
 
 	private class deleteEventAPICaller extends AsyncTask<Event, Void, Boolean> {
 		/**
 	     * Informs the user that the event is being deleted.
 	     */
 	    ProgressDialog dialog;
 
 		@Override
 		protected void onPreExecute() {
 			// Set up progress indication.
 			dialog = ProgressDialog.show(MyEvents.this, "Deleting...", "");
 		}
 
 		@Override
 		protected Boolean doInBackground(Event... event) {
 			return APICalls.deleteEvent(event[0], Identity.getUserId(getApplicationContext()));
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			// Close any progress indication.
 			dialog.dismiss();
 			dialog = null;
 			if (result == false) {
 				(Toast.makeText(getApplicationContext(), "The event could not be deleted!", Toast.LENGTH_LONG)).show();
 				return;
 			}
 
 			// Update the local cache to recognize deletion.
 			if (localDB == null)
 	            localDB = new LocalDatabase(getApplicationContext());
 			boolean deleted = localDB.deleteEventFromCache(deletionEvent);
 			if (!deleted)
 				Log.e("deleteEventAPICaller.onPostExecute", "Could not delete event from local cache");
 
 			// Update the list view and internal events list.
 			deletionEvent = null;
 			refreshMyEventsList();
 		}
 	}
 }
