 package it.chalmers.dat255_bearded_octo_lama.activities;
 
 import it.chalmers.dat255_bearded_octo_lama.Alarm;
 import it.chalmers.dat255_bearded_octo_lama.AlarmController;
 import it.chalmers.dat255_bearded_octo_lama.R;
import it.chalmers.dat255_bearded_octo_lama.utilities.Time;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.TextView;
import android.widget.Toast;
 
 public class ListAlarmsActivity extends AbstractActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_list_alarms);
         
         
         AlarmAdapter adapter = new AlarmAdapter(this, 
                 R.layout.row_alarm, AlarmController.INSTANCE.getAllAlarms(this));
         
         
         ListView lv = (ListView)findViewById(R.id.listView);
         lv.setAdapter(adapter);
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
 			this.items   = new ArrayList<Alarm>(items);
 			
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
 	        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
 	        
 	        holder.clock.setText(sdf.format(new Date(alarm.getTimeInMS())));
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
