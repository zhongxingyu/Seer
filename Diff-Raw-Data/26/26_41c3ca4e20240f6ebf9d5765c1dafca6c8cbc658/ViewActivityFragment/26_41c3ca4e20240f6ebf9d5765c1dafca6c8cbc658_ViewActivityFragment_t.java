 package com.linqcan.mytime;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class ViewActivityFragment extends Fragment implements OnItemSelectedListener{
 	
 	private void putLogMessage(String msg){
 		MainActivity.putLogMessage("NewActivityFragment", msg);
 	}
 	
 	public enum MODE{
 		NEW, VIEW
 	}
 	
 	public interface ViewActivityListener{
 		public void editActivity(long id);
 		public void onDelete();
 		public void onFinsihedActivity(long id);
 	}
 	
 	private TimeActivity mCurrentTimeActivity;
 	private DatabaseProvider mDatabase;
 	private MODE mMode = MODE.NEW;
 	private long mTaId = -1;
 	private Activity mActivity;
 	private ViewActivityListener mListener;
 	private boolean mOngoing = false;
 	
	/*
	 * mFirstTime
	 * 
	 * To make sure onItemSelected is not triggered when activity is raised
	 * for the first time
	 */
	private boolean mFirstTime = true; 
	
 	//Views
 	private Spinner mLabelsSpinner;
 	private TextView mNoSpinnerTxt;
 	private TextView mStartedTxt;
 	private TextView mEndedTxt;
 	private TextView mDurationTxt;
 	private TextView mDescTxt;
 	private Button mStopBtn;
 	private View mEndedBox;
 	private View mDescBox;
 	
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		mActivity = activity;
 		try{
 			mListener = (ViewActivityListener) activity;
 		}
 		catch(ClassCastException e){
 			throw new ClassCastException("ViewActivityListener not implemented by activity " + e.getMessage());
 		}
 		mDatabase = new DatabaseProvider(mActivity);
 		mDatabase.open();
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		putLogMessage("onCreate");
 		setHasOptionsMenu(true);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.view_activity_fragment, container);
 		mLabelsSpinner = (Spinner) v.findViewById(R.id.spinner_labels);
 		mStartedTxt = (TextView) v.findViewById(R.id.txt_activity_started_at);
 		mEndedTxt = (TextView) v.findViewById(R.id.txt_activity_ended_at);
 		mDurationTxt = (TextView) v.findViewById(R.id.txt_activity_duration);
 		mStopBtn = (Button) v.findViewById(R.id.btn_stop_timer);
 		mNoSpinnerTxt = (TextView) v.findViewById(R.id.txt_spinner_no_spinner_txt);
 		mDescTxt = (TextView) v.findViewById(R.id.txt_description);
 		mEndedBox = v.findViewById(R.id.box_activity_ended_at);
 		mDescBox = v.findViewById(R.id.box_desc);
 				
 		return v;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		if(mMode == MODE.NEW){
 			putLogMessage("Mode NEW");
 			Date start_date = new Date();
 			mCurrentTimeActivity = new TimeActivity();
 			
 			mCurrentTimeActivity.setActivity_date(start_date);
 			mCurrentTimeActivity.setStart_date(start_date);
 			mCurrentTimeActivity.setEnd_date(start_date);
 			mCurrentTimeActivity.setCreated_on(start_date);
 			updateOngoing(true);
 			
 			TimeActivity newta = mDatabase.insertActivity(mCurrentTimeActivity);
 			
 			if(newta == null){
 				Toast.makeText(getActivity(), "Database insertion error!", Toast.LENGTH_SHORT).show();
 				return;
 			}
 			mCurrentTimeActivity = newta;
 			putLogMessage("Created activity with id " + Long.toString(mCurrentTimeActivity.getId()));
 		}
 		else if(mMode == MODE.VIEW){
 			putLogMessage("Mode VIEW");
 			if(mTaId != -1){
 				mCurrentTimeActivity = mDatabase.getActivityById(mTaId);
				mOngoing = mCurrentTimeActivity.getOngoing();
 			}
 		}
 	
 		if(mCurrentTimeActivity.getOngoing() == true){
 			//The activity is ongoing and hence the mode should be VIEW
 			mStopBtn.setOnClickListener(stopBtnListener);
 			
 			Cursor cursor = mDatabase.getAllLabelsCursor();
 		
 			if(cursor.getCount() > 0){
 				putLogMessage("Items in the cursor");
 				SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), 
 						android.R.layout.simple_spinner_item, 
 						cursor, 
 						new String[] {"name"}, 
 						new int[] {android.R.id.text1}, 
 						0);
 				adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
 				mLabelsSpinner.setAdapter(adapter);
 				setSpinner();
				mLabelsSpinner.setOnItemSelectedListener(this);				
 			}
 		}
 		else{
 			mStopBtn.setVisibility(View.GONE);
 			mLabelsSpinner.setVisibility(View.GONE);
 			
 			Label label = mDatabase.getLabelById(mCurrentTimeActivity.getLabel_id());
 			mNoSpinnerTxt.setText(label.getName());
 			mNoSpinnerTxt.setVisibility(View.VISIBLE);
 			mDescTxt.setText(mCurrentTimeActivity.getDescription());
 			mDescBox.setVisibility(View.VISIBLE);
 			showEndedAndDurationText();
 		}
 		
 		mStartedTxt.setText(mCurrentTimeActivity.getActivity_date().toString());
 		mActivity.invalidateOptionsMenu();
 	}
 	
 	@Override
 	public void onResume() {
 		putLogMessage("onResume");
 		super.onResume();
 		mDatabase.open();
 		updateDuration();
 	}
 	
 	@Override
 	public void onPause() {
 		putLogMessage("onPause");
 		super.onPause();
 		updateDatabase();
 		mDatabase.close();
 	}
 	
 	private void setSpinner(){
 		//Set the spinner selection to the correct item
 		long lId = mCurrentTimeActivity.getLabel_id();
 		long sId = -1;
 		for(int i = 0; i < mLabelsSpinner.getCount(); i++){
 			sId = mLabelsSpinner.getItemIdAtPosition(i);
 			if(sId == lId){
 				mLabelsSpinner.setSelection(i);
 				break;
 			}
 		}
 	}
 
 	private void updateDatabase(){
 		boolean result = mDatabase.updateActivity(mCurrentTimeActivity);
 		
 		if(!result){
 			Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		putLogMessage("Updated database!");
 		
 	}
 	
 	private void updateDuration(){
 		if(mOngoing){
 			Date now = new Date();
 			putLogMessage("Update time: " + now.toString());
 			mCurrentTimeActivity.setEnd_date(now);
 		}
 		mDurationTxt.setText(SharedHelpers.getDurationAsString(mCurrentTimeActivity));
 	}
 	
 	private void updateOngoing(boolean ongoing){
 		mOngoing = ongoing;
 		mCurrentTimeActivity.setOngoing(ongoing);
 	}
 	
 	private void showEndedAndDurationText(){
 		mEndedTxt.setText(mCurrentTimeActivity.getEnd_date().toString());
 		mEndedBox.setVisibility(View.VISIBLE);
 		mDurationTxt.setText(SharedHelpers.getDurationAsString(mCurrentTimeActivity));
 		mDurationTxt.setVisibility(View.VISIBLE);
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos,
 			long id) {
		if(mFirstTime){
			mFirstTime = false;
			return;
		}
 		putLogMessage("Label with id " + Long.toString(id) + " was selected");
 		mCurrentTimeActivity.setLabel_id(id);
		Toast.makeText(mActivity, "Label updated", Toast.LENGTH_SHORT).show();		
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub		
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		putLogMessage("onCreateOptionsMenu");
 		if(!mOngoing){
 			putLogMessage("Adding options menu");
 			inflater.inflate(R.menu.view_activity, menu);
 		}
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()){
 			case R.id.menu_edit_activity:
 				mListener.editActivity(mTaId);
 				return true;
 			case R.id.menu_delete:
 				boolean result = mDatabase.deleteActivity(mTaId);
 				if(!result){
 					Toast.makeText(mActivity, "Database error: Activity could not be deleted", Toast.LENGTH_LONG).show();
 				}
 				mListener.onDelete();
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void setMode(MODE mode){
 		mMode = mode;
 	}
 	
 	public void setTaId(long id){
 		mTaId = id;
 	}
 	
 	private OnClickListener stopBtnListener = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			putLogMessage("Stop button pressed");
 			mCurrentTimeActivity.setEnd_date(new Date());
 			updateOngoing(false);			
 			updateDatabase();
 			//Show the activity in View mode instead
 			mListener.onFinsihedActivity(mCurrentTimeActivity.getId());
 		}
 	};
 }
