 package it.chalmers.dat255_bearded_octo_lama.activities;
 
 import it.chalmers.dat255_bearded_octo_lama.Alarm;
 import it.chalmers.dat255_bearded_octo_lama.AlarmController;
 import it.chalmers.dat255_bearded_octo_lama.R;
 import it.chalmers.dat255_bearded_octo_lama.utilities.Time;
 
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ListAlarmsActivity extends AbstractActivity {
 	private AlarmAdapter adapter;
 	
 	private static final int DELETE_CONTEXT_MENU_ID = 0;
 	private static final int EDIT_CONTEXT_MENU_ID   = 1;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_list_alarms);
         
         List<Alarm> allAlarms = AlarmController.INSTANCE.getAllAlarms(this);
         adapter = new AlarmAdapter(this, R.layout.row_alarm, allAlarms);
         
         ListView lv = (ListView)findViewById(R.id.listView);
         lv.setAdapter(adapter);
         registerForContextMenu(lv);
     }
     
     private final static OnClickListener checkboxClickListener = new OnClickListener() {
 		public void onClick(View v) {
 			Alarm alarm = (Alarm)v.getTag();
 			
 			AlarmController.INSTANCE.toggleAlarm(v.getContext(), alarm.getId());
 			
 			// Get the updated alarm and display time left if re-enabled
 			alarm = AlarmController.INSTANCE.getAlarm(v.getContext(), alarm.getId());
 			if(alarm.isEnabled()) {
 				Toast.makeText(v.getContext(), "Time left " + Time.getTimeLeft(alarm.getTimeInMS()), Toast.LENGTH_SHORT).show();
 			}
 		}
 	};
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 										ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		// Create a context for the list alarm ListView
 		if (v.getId() == R.id.listView) {
 			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 			menu.setHeaderTitle(adapter.getItem(info.position).toPrettyString());
 			
 			menu.add(Menu.NONE, DELETE_CONTEXT_MENU_ID, Menu.NONE, R.string.contextMenuDelete);
 			menu.add(Menu.NONE, EDIT_CONTEXT_MENU_ID, Menu.NONE, R.string.contextMenuEdit);	  
 		  }
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();	
 		  Alarm affectedAlarm = adapter.getItem(info.position);
 	
 		  switch(item.getItemId()) {
 		  	case DELETE_CONTEXT_MENU_ID:
		  		deleteAlarm(affectedAlarm, info.position);
 		  		break;
 		  	case EDIT_CONTEXT_MENU_ID:
 		  		editAlarm(affectedAlarm);
 		  		break;
 		  }
 		  
 		  return true;
 	}
 
 	/** Launches the AddAlarmActivity with the specified alarm used for template/base */
 	private void editAlarm(Alarm affectedAlarm) {
 		//TODO: Launch AddAlarmActivity with this alarm as base so the user can edit it.
 	}
 
 	/** Removes the specified alarm from the database and from the adapter backed list containing all alarms */
	private void deleteAlarm(Alarm affectedAlarm, int rowIndex) {
		AlarmController.INSTANCE.deleteAlarm(this, affectedAlarm.getId());
 		adapter.remove(affectedAlarm);

 		Toast.makeText(this, "Removed " + affectedAlarm.toPrettyString(), Toast.LENGTH_LONG).show();
 	}
 
 	/**
 	 * This class adapts an {@code Alarm} for use in a {@code ListView}.
 	 * @author Emil Edholm
 	 * @date 5 okt 2012
 	 */
 	private static class AlarmAdapter extends ArrayAdapter<Alarm> {
 		
 		private final List<Alarm>    items;
 		private final int            viewID;
 		private final LayoutInflater inflater;
 		
 		/**
 		 * @param context the context
 		 * @param textViewResourceId id of the layout file for displaying each ListView item.
 		 * @param items a list of all the items to adapt.
 		 * @see AlarmController#getAllAlarms
 		 */
 		public AlarmAdapter(Context context, int textViewResourceId,
 				List<Alarm> items) {
 			super(context, textViewResourceId, items);
 			
 			this.viewID  = textViewResourceId;
			this.items   = items;
 			
 			this.inflater = LayoutInflater.from(context);
 		}
 
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 			AlarmHolder holder = null;
 	        
 	        if(row == null) {
 	            row = inflater.inflate(viewID, parent, false);
 	            
 	            holder = new AlarmHolder();
 	            holder.clock  = (TextView)row.findViewById(R.id.clockTextView);
 	            holder.enabled = (CheckBox)row.findViewById(R.id.alarmEnabledCheckBox);
 	            
 	            row.setTag(holder);
 	        } else {
 	            holder = (AlarmHolder)row.getTag();
 	        }
 	        
 	        Alarm alarm = items.get(position);
 	        
 	        holder.clock.setText(alarm.toPrettyString());
 	        holder.enabled.setChecked(alarm.isEnabled());
 	
 	        // Make the checkbox change listenable.
 	        holder.enabled.setTag(alarm);
 	        holder.enabled.setOnClickListener(checkboxClickListener);
 	        
 	        return row;
 		}
 		
 		/**
 		 * Utilizes the view holder pattern for updating a row.
 		 */
 		private static class AlarmHolder {
 			private TextView clock;
 			private CheckBox enabled;
 		}	
 	}
 }
