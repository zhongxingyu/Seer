 package no.uka.findmyapp.ukaprogram.activities;
 
import no.uka.findmyapp.android.library.R;
 import no.uka.findmyapp.android.rest.datamodels.models.UkaEvent;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public abstract class PopupMenuActivity extends Activity {
 	
 	private static final String debug = "PopupMenuActivity";
 	private UkaEvent selectedEvent;
 	
 	public UkaEvent getSelectedEvent() {
 		return selectedEvent;
 	}
 
 	public void setSelectedEvent(UkaEvent selectedEvent) {
 		this.selectedEvent = selectedEvent;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(R.string.setting_menu_item);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = new Intent(this, SettingsActivity.class); 
 		intent.putExtra("previous_context", this.getClass());
 		
 		if(this.getClass().equals(EventDetailsActivity.class)) {
 			intent.putExtra("selectedEvent", this.selectedEvent);
 		}
 		
 		startActivity(intent);
 		return true;
 	}
 }
