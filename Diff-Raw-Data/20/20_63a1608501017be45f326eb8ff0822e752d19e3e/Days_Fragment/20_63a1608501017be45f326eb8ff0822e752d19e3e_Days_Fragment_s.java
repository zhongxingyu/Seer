 package com.appsmarttech.utpro;
 
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 
 public class Days_Fragment extends SherlockFragment{
 	
 	//Declarations
 	DBHelper_activity db;
 	ListView lvDays;
 	Cursor cDays, cPrograms;
 	SharedPreferences spPreferences;
 	Editor ePreferences;
 	String sActiveProgram, sDayName;
 	Integer iActiveProgram, iProgramID, iImageVisible;
 	ActionMode mActionMode;
 	ActionBar actionBar;
 	OnItemClickListener lvDaysListener;
 	OnItemLongClickListener lvDaysLongListener;
 	Boolean bActionPresent;
 	Program pActiveProgram;
 	Day dSelected;
 	ListAdapter lvDaysAdapter;
 	List<Day> Days;
 	List<Exercise> Exercises;
 	Intent inExerDetails;
 	
 	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, 
         Bundle savedInstanceState) {
     	// Inflate the layout for this fragment
    	 	View vDays = inflater.inflate(R.layout.days_fragment, container, false);
    	 	
         //open preferences
         spPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
 
         //declare preference editor
         ePreferences = spPreferences.edit();
         
         //boolean for tracking the presence of the cab
         bActionPresent = false;
         
         //grabbing the active program from preferences
         sActiveProgram = spPreferences.getString("kActiveProgram", "0");
         
         //converting it to Integer
         iActiveProgram = Integer.valueOf(sActiveProgram);
    	 	
    	 	//assigning listview to listview widget
    	 	lvDays = (ListView)vDays.findViewById(R.id.lvDays);
    	 	
    	 	//telling it that it has an actionbar
    	 	setHasOptionsMenu(true);
    	 	
    	 	//declaring intents
    	 	inExerDetails = new Intent(getActivity(), ExerDetail_Activity.class);
    	 	
         //opening database
    	 	db = (new DBHelper_activity(getActivity()));
 
         
         //getting the name of the program based on the program id in preferences
         pActiveProgram = db.getProgram(iActiveProgram);
         
         //setting activity title based on selected program
         getActivity().setTitle(pActiveProgram.getName());
         
         
         //building list of days based on program ID
         Days = db.getAllProgramDays(iActiveProgram);
 
     	//building the onclick listener for the lvPrograms Listview
         lvDaysListener = new OnItemClickListener() {
 
     		@Override
     		public void onItemClick(AdapterView<?> parent, View view, int position,
     				long id) {
    			//grabbing the selected day object
    			dSelected = (Day) (lvDays.getItemAtPosition(position));
    			//putting the dayID of the selected day in a bundle to send to the next activity
    			inExerDetails.putExtra("DAY_ID", dSelected.getDayID());
    			//launching the exercise details activity
    			startActivity(inExerDetails);
     		}
       	  
         };
         //building the long onclick listener for the lvPrograms listview
         
         lvDaysLongListener = new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view,
 					int position, long id) {
     			//grabbing the selected item from lvPrograms
     			dSelected = (Day) (lvDays.getItemAtPosition(position));
 				//grabbing dayname to update the action bar with
 				sDayName = dSelected.getName();
 				//launching the contextual action bar
 				mActionMode = getSherlockActivity().startActionMode(new mActionModeCallback());
 				return true;
 			}
         	
         };
         
         //setting up adapter
         lvDaysAdapter = new DayArrayAdapter(getActivity(),Days);
         
         //setting click listener, long click listener, and adapter to the listview
         lvDays.setOnItemClickListener(lvDaysListener);
         lvDays.setOnItemLongClickListener(lvDaysLongListener);
         lvDays.setAdapter(lvDaysAdapter);
         
    	 	return vDays;
     }
     
     
   //creating custom listview adapter for days
   	public class DayArrayAdapter extends ArrayAdapter<Day> {
   		private final Context context;
   		
   	 
   		public DayArrayAdapter(Context context, List<Day> values) {
   			super(context, R.layout.days_row, values);
   			this.context = context;
   			
   		}
   	 
   		@Override
   		public View getView(int position, View convertView, ViewGroup parent) {
   			LayoutInflater inflater = (LayoutInflater) context
   				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   	 
   			View rowView = inflater.inflate(R.layout.days_row, parent, false);
   			//declaring textview widgets in the row
   			TextView tvDayName = (TextView)rowView.findViewById(R.id.tvDayName);
   			TextView tvDayNumber = (TextView)rowView.findViewById(R.id.tvDayNumber);
   	 
     	     	//declaring image views and assigning widgets
     	      	ImageView ivCheck = (ImageView)rowView.findViewById(R.id.ivCheck);
     	      	ImageView ivX = (ImageView)rowView.findViewById(R.id.ivX);	 
   	 
   			//grab current day
   			dSelected = getItem(position);
   			
   			//setting text of day name and day number
   			tvDayName.setText(dSelected.getName());
   			tvDayNumber.setText(String.valueOf(dSelected.getDayNumber()));
   			
     	      //determining imageview visibility based on dayCompleted flag
     	      switch(dSelected.getCompleted()){
     	      case 1:
 
     	    	  ivCheck.setVisibility(View.VISIBLE);
   	    	  ivX.setVisibility(View.GONE);
   	    	  break;
     	      case 2:
     	    	  ivCheck.setVisibility(View.GONE);
   	    	  ivX.setVisibility(View.VISIBLE);
   	    	  break;
   	      default:
   	    	  ivCheck.setVisibility(View.GONE);
   	    	  ivX.setVisibility(View.GONE);
   	    	  break;
     	      }
 
   			return rowView;
   		}
   	} 
   	
 	   //creating the actionbar
 		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
 			inflater.inflate(R.menu.days_ab, menu);
 			
 			super.onCreateOptionsMenu(menu, inflater);
 			
 		}
 		
 		//setting the actions for the actionbar
 		@Override
 		public boolean onOptionsItemSelected(MenuItem item) {
 			switch (item.getItemId()) {
 			case R.id.miRestart:
 				//reset complete/skip flags
 				db.progClearFlags(iActiveProgram);
 		        for (Day d : Days) {
 		            d.setCompleted(0);
 		        }
 				((BaseAdapter) lvDaysAdapter).notifyDataSetChanged();
 				break;
 				
 			default:
 				Toast.makeText(getActivity(), "You pressed some other shit", Toast.LENGTH_SHORT)
 				.show();
 				break;
 			}
 
 			return true;
 		}
 		
 		//creating the contextual action bar
 		public final class mActionModeCallback implements ActionMode.Callback {
 
 			// Called when the action mode is created; startActionMode() was called
 			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 				// Inflate a menu resource providing context menu items
 				MenuInflater inflater = mode.getMenuInflater();
 				// Assumes that you have "contexual.xml" menu resources
 				inflater.inflate(R.menu.days_cab, menu);
 				mode.setTitle(sDayName);
 				bActionPresent = true;
 				return true;
 			}
 
 			// Called each time the action mode is shown. Always called after
 			// onCreateActionMode, but
 			// may be called multiple times if the mode is invalidated.
 			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 				return false; // Return false if nothing is done
 			}
 
 			// Called when the user selects a contextual menu item
 			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 				switch (item.getItemId()) {
 				case R.id.miComplete:
 					//marking the day as completed in the DB
 					db.dayCompleteSkipped(1, dSelected.getID());
 					//marking the object as completed in the array
 					dSelected.setCompleted(1);
 					//refreshing the listview to reflect the change
 					((BaseAdapter) lvDaysAdapter).notifyDataSetChanged();
 					mode.finish(); // Action picked, so close the CAB
 					return true;
 				case R.id.miSkip:
 					//marking the day as skipped in the DB
 					db.dayCompleteSkipped(2, dSelected.getID());
 					//marking the object as skipped in the array
 					dSelected.setCompleted(2);
 					//refreshing the listview to reflect the change
 					((BaseAdapter) lvDaysAdapter).notifyDataSetChanged();
 					mode.finish(); // Action picked, so close the CAB
 					return true;
 				default:
 					return false;
 				}
 			}
 
 			// Called when the user exits the action mode
 			public void onDestroyActionMode(ActionMode mode) {
 				bActionPresent = false;
 				mActionMode = null;
 			}
 
 		}    
 }
