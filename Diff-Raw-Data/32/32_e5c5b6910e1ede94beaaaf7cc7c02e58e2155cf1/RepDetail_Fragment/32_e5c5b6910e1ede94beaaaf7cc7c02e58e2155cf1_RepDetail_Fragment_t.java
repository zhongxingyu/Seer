 package com.appsmarttech.ut90;
 
 import java.util.Calendar;
 import java.util.List;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 
 public class RepDetail_Fragment extends SherlockFragment{
 	Button bDate , bPlusRep, bMinusRep, bPlusWeight, bMinusWeight;
 	DBHelper_activity db;
 	int iDayID, iSize, e, ae, iReps, iWeight, iExerID, iYear, iMonth, iDay, iActiveBandSet;
 	List<Stat> Stats;
 	List<Band> Bands;
	Band loadedBand, selectedBand;
 	EditText etRep, etWeight, etNotes;
 	TextView tvDate;
 	OnClickListener bPlusListener, bMinusListener, bPlusWeightListener, bMinusWeightListener, bDateListener;
 	String sDate, saDate, sActiveBandSet;
 	Bundle bArgs;
 	Menu mnuActionBar;
 	repNavListener repNavListener;
 	setDateListener setDateListener;
 	SharedPreferences spPreferences;
 	Spinner spBands;
 	SpinnerAdapter spBandsAdapter;
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, 
         Bundle savedInstanceState) {
     	// Inflate the layout for this fragment
    	 	View vExercises = inflater.inflate(R.layout.repdetail_fragment, container, false);
    	 	//assigning widgets
    	 	bDate = (Button)vExercises.findViewById(R.id.bDate);
    	 	bPlusRep = (Button)vExercises.findViewById(R.id.bPlusRep);
    	 	bMinusRep = (Button)vExercises.findViewById(R.id.bMinusRep);
    	 	bPlusWeight = (Button)vExercises.findViewById(R.id.bPlusWeight);
    	 	bMinusWeight = (Button)vExercises.findViewById(R.id.bMinusWeight);
    	 	etRep = (EditText)vExercises.findViewById(R.id.etRep);
    	 	etWeight = (EditText)vExercises.findViewById(R.id.etWeight);
    	 	etNotes = (EditText)vExercises.findViewById(R.id.etNotes);
    	 	tvDate = (TextView)vExercises.findViewById(R.id.tvDate);
    	 	spBands = (Spinner)vExercises.findViewById(R.id.spBand);
    	 	
    	 	//grabbing arguments from the activity
    	 	bArgs = getArguments();
    	 	saDate = bArgs.getString("kDate");  //date
    	 	iExerID = bArgs.getInt("kExerID", 1);
    	 	
     	//open preferences
         spPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
         
         //grabbing the active program from preferences
         sActiveBandSet = spPreferences.getString("kActiveBandSet", "1");
         
         //converting it to Integer
         iActiveBandSet = Integer.valueOf(sActiveBandSet);
    	 	
    	 	//setting current date to the date select button
    	 	bDate.setText(saDate);
 
    	 	//declaring db helper class
    	 	db = (new DBHelper_activity(getActivity()));
         
         //grabbing list of bands
         Bands = db.getAllSetBands(iActiveBandSet);
         
         //setting up the adatper for the spinner
       //setting up days adapter
         spBandsAdapter = new BandArrayAdapter(getActivity(),Bands);
         
         //has action bar
         setHasOptionsMenu(true);
         
         //building onclick listeners
         bPlusListener = new OnClickListener() {
 
 
 			@Override
 			public void onClick(View vExercises) {
 				onPlusRep();
 				
 			}
       	  
         };
         
         bMinusListener = new OnClickListener() {
 
 
 			@Override
 			public void onClick(View vExercises) {
 				onMinusRep();
 				
 			}
       	  
         };
         
         bPlusWeightListener = new OnClickListener() {
 
 
  			@Override
  			public void onClick(View vExercises) {
  				onPlusWeight();
  				
  			}
        	  
          };
          
          bMinusWeightListener = new OnClickListener() {
 
 
  			@Override
  			public void onClick(View vExercises) {
  				onMinusWeight();
  				
  			}
        	  
          };
          
          bDateListener = new OnClickListener() {
  			@Override
  			public void onClick(View vExercises) {
  				diaglogDatePick(vExercises);	
  			}
          };
 		//assigning onclicklisteners
         bPlusRep.setOnClickListener(bPlusListener);
         bMinusRep.setOnClickListener(bMinusListener);
         bPlusWeight.setOnClickListener(bPlusWeightListener);
         bMinusWeight.setOnClickListener(bMinusWeightListener);
         bDate.setOnClickListener(bDateListener);
         //assigning adapter to spinner
         spBands.setAdapter(spBandsAdapter);
         spBands.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 
 			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				selectedBand = (Band) parent.getItemAtPosition(pos);
				
				if(selectedBand.getWeight()!=0){
					etWeight.setText(String.valueOf(selectedBand.getWeight()));
					}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
         });
       //grabbing last stats
         getLastStat();
 
    	 	return vExercises;
 	}
 	
 	//declaring fragment listener for moving between the fragments inside of the activity
 	public interface repNavListener{
 	public void mNavigation(int iNav);
 
 	}
 	
 	//declaring fragment listener for setting the date in the activity
 	public interface setDateListener{
 	public void mSetDate(String Date);
 
 	}
 	
	//save the stat
 	public void onSave(){
 		int tWeight = Integer.parseInt(etWeight.getText().toString());
 		int tRep = Integer.parseInt(etRep.getText().toString());
		int iColorID = selectedBand.getColorID();
		int iBandID = selectedBand.getBandID();
 		sDate = bDate.getText().toString();
 		String tNotes = etNotes.getText().toString();
		db.saveStat(1, iExerID, tWeight, tRep, iBandID, "0", sDate, tNotes, iColorID);
 	}
 	
 	//actions when user clicks the "+" button for reps
 	public void onPlusRep(){
 		int iLen = etRep.length();
 		if(iLen <=0){//checking if the reps edit box is null
 			iReps = 0; //if it is make iReps 0
 		}
 		else
 		{
 			iReps = Integer.parseInt(etRep.getText().toString());
 					}
 		//then add one to iReps and put that value in the edit box
 		iReps = iReps +1;
 		etRep.setText(String.valueOf(iReps));
 	}
 	
 	//actions when the user clicks the "-" button 
 	public void onMinusRep(){
 		int iLen = etRep.length();
 		if(iLen <=0){//checking if the reps edit box is null
 			
 		}
 		else
 		{
 			iReps = Integer.parseInt(etRep.getText().toString());
 			if(iReps == 0){
 				
 			}
 			else{
 			iReps = iReps -1;
 			etRep.setText(String.valueOf(iReps));
 			}
 		}
 	}
 		//actions when user clicks the "+" button for weight
 		public void onPlusWeight(){
 			int iLen = etWeight.length();
 			if(iLen <=0){//checking if the weight edit box is null
 				iWeight = 0; //if it is make iWeight 0
 			}
 			else
 			{
 				iWeight = Integer.parseInt(etWeight.getText().toString());
 						}
 			//then add one to iWeight and put that value in the edit box
 			iWeight = iWeight +1;
 			etWeight.setText(String.valueOf(iWeight));
 		}
 		
 		public void onMinusWeight(){
 			int iLen = etWeight.length();
 			if(iLen <=0){//checking if the reps edit box is null
 				
 			}
 			else
 			{
 				iWeight = Integer.parseInt(etWeight.getText().toString());
 				if(iWeight == 0){
 					
 				}
 				else{
 				iWeight = iWeight -1;
 				etWeight.setText(String.valueOf(iWeight));
 				}
 			}
 	}
 		
 		//method for getting the last exercise stats
 		public void getLastStat(){
 			//getting the stats for the current exercise
 			Stats = db.getExerciseLastStat(iExerID);
 			int s = Stats.size() -1 ;
 			if (s >= 0){
 			//setting the appropriate fields to the last user stat
 			etRep.setText(String.valueOf(Stats.get(0).getReps()));
 			etWeight.setText(String.valueOf(Stats.get(0).getWeight()));
 			etNotes.setText(Stats.get(0).getNotes());
 			spBands.setSelection(Stats.get(0).getBandID());
 			}
 		}
 		
 		//creating custom listview adapter for the bandsets
     	class BandArrayAdapter extends ArrayAdapter<Band> {
     		private final Context context;
     		
     	 
     		public BandArrayAdapter(Context context, List<Band> values) {
     			super(context, R.layout.bands_row, values);
     			this.context = context;
     			
     		}
     	 
     		@Override
     		public View getView(int position, View convertView, ViewGroup parent) {
     			LayoutInflater inflater = (LayoutInflater) context
     				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     	 
     			View rowView = inflater.inflate(R.layout.bands_row, parent, false);
     			TextView tvBandWeight = (TextView)rowView.findViewById(R.id.tvWeight);
     	 
     			//grab current band
    			loadedBand = getItem(position);
     			
     			//setting text of bands
    			tvBandWeight.setText(String.valueOf(loadedBand.getWeight()));
    			String mDrawableName = loadedBand.getColor();
     			int resID = getResources().getIdentifier(mDrawableName , "drawable", getActivity().getPackageName());
     			Drawable myIcon = getResources().getDrawable(resID);
     			myIcon.setBounds(0,0,30,30);
     			tvBandWeight.setCompoundDrawables(myIcon, null, null, null);
     			
 
     			
     		    return rowView;
     		}
     		
     	    @Override
     	    public View getDropDownView(int position, View convertView,
     	            ViewGroup parent) {
     	    	LayoutInflater inflater = (LayoutInflater) context
         				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     	    	View rowView = inflater.inflate(R.layout.bands_row, parent, false);
     			TextView tvBandWeight = (TextView)rowView.findViewById(R.id.tvWeight);
     	 
     			//grab current band
     			 Band bSelected = getItem(position);
     			
     			//setting text of bands
     			tvBandWeight.setText(String.valueOf(bSelected.getWeight()));
     			String mDrawableName = bSelected.getColor();
     			int resID = getResources().getIdentifier(mDrawableName , "drawable", getActivity().getPackageName());
     			Drawable myIcon = getResources().getDrawable(resID);
     			myIcon.setBounds(0,0,30,30);
     			tvBandWeight.setCompoundDrawables(myIcon, null, null, null);
 
     	        return rowView;
     	    }
     	}
 	    //dialog box for choosing exercise date
 	    public void diaglogDatePick(View v){
 	        final Dialog dialog = new Dialog(getActivity());
 	        dialog.setContentView(R.layout.datepicker_dialog);
 	        dialog.setTitle("Choose Exercise Date");
 	        dialog.setCancelable(true);
 	        //declare dialog buttons
 	        Button btnOK = (Button) dialog.findViewById(R.id.btnDateOK);
 	        Button btnCancel = (Button) dialog.findViewById(R.id.btnDateCancel);
 	        final DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);
 	        //setting date
 	        final Calendar c = Calendar.getInstance();
 	        iYear = c.get(Calendar.YEAR);
 	        iMonth = c.get(Calendar.MONTH);
 	        iDay = c.get(Calendar.DAY_OF_MONTH);
 	        datePicker.init(iYear, iMonth, iDay, null);
 
 	        btnOK.setOnClickListener(new OnClickListener() {
 	        @Override
 	            public void onClick(View v) {
 	            iYear = datePicker.getYear();
 	            iMonth = datePicker.getMonth();
 	            iDay = datePicker.getDayOfMonth();
 	            String sYear = String.valueOf(iYear);
 	            String sMonth = "0" + String.valueOf(iMonth + 1);
 	            String sDay = String.valueOf(iDay);
 	            String sDate = sYear + "-" + sMonth + "-" + sDay;
 	            bDate.setText(sDate);
 	            //sets date in the activity so it can be used in the next fragment
 	            setDateListener.mSetDate(sDate);
 	        	dialog.dismiss();
 	            }
 	        });
 	        btnCancel.setOnClickListener(new OnClickListener() {
 	        @Override
 	            public void onClick(View v) {
 	        	dialog.dismiss();
 	            }
 	        });
 
 	    	dialog.show();
 
 	    }
 		
 		//creating the actionbar
 				public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
 					inflater.inflate(R.menu.exer_ab, menu);
 					mnuActionBar = menu;
 					super.onCreateOptionsMenu(menu, inflater);
 
 				}
 
 
 				//setting the actions for the actionbar icons
 				@Override
 				public boolean onOptionsItemSelected(MenuItem item) {
 					switch (item.getItemId()) {
 					case R.id.miSaveRep:
 						onSave();
 						repNavListener.mNavigation(1);
 						break;
 					case R.id.miPrevRep:
 						repNavListener.mNavigation(0);
 						break;
 					case R.id.miSkip:
 						repNavListener.mNavigation(1);
 						break;
 					default:
 						break;
 					}
 
 					return true;
 				}
 
 				//attaching to the listener in the activity
 			    @Override
 			    public void onAttach(Activity activity) {
 			        super.onAttach(activity);
 			        try {
 			            repNavListener = (repNavListener) activity;
 			        } catch (ClassCastException e) {
 			            throw new ClassCastException(activity.toString()
 			                    + " must implement updateEListener");
 			        }
 			        try {
 			            setDateListener = (setDateListener) activity;
 			        } catch (ClassCastException e) {
 			            throw new ClassCastException(activity.toString()
 			                    + " must implement setDateListener");
 			        }
 			}
 }
