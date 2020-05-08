 package com.livestrong.myplate.fragment;
 
 import java.lang.reflect.Method;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.livestrong.myplate.MyPlateApplication;
 import com.livestrong.myplate.R;
 import com.livestrong.myplate.activity.AddWaterActivity;
 import com.livestrong.myplate.activity.AddWeightActivity;
 import com.livestrong.myplate.activity.ExerciseSelectorActivity;
 import com.livestrong.myplate.activity.FoodSelectorActivity;
 import com.livestrong.myplate.animations.WidthAnimation;
 import com.livestrong.myplate.back.DataHelper;
 import com.livestrong.myplate.back.models.DiaryEntries.DiaryEntryType;
 import com.livestrong.myplate.back.models.FoodDiaryEntry.TimeOfDay;
 import com.livestrong.myplate.back.models.UserProgress;
 
 public class MyPlateFragment extends FragmentDataHelperDelegate {
 	private final static Class<?> NEXT_ACTIVITY_FOOD     = FoodSelectorActivity.class; 
 	private final static Class<?> NEXT_ACTIVITY_EXERCICE = ExerciseSelectorActivity.class;
 	private final static Class<?> NEXT_ACTIVITY_WATER    = AddWaterActivity.class;
 
 	View view;
 	Button breakfastBtn, lunchBtn, dinnerBtn, snacksBtn, exerciseBtn, waterBtn;
 	ImageView progressBar;
 	Integer progressBarWidth;
 	TextView caloriesConsumedTextView, calorieGoalTextView;
 	private UserProgress _userProgress;
 	
 	private class UserProgressTask extends AsyncTask<Void, Void, UserProgress>
 	{
		protected void onPreExecute() 
		{
			MyPlateFragment.this.view.findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
		}
 		@Override
 		protected UserProgress doInBackground(Void... params) 
 		{
 			int goal = DataHelper.getUserDailyCaloriesGoal();
 			return DataHelper.getUserCaloriesProgress(new Date(), goal);
 		}
 		
 		protected void onPostExecute(UserProgress progress) 
 		{
 			if (progress != null) 
 			{
 				// Set Progress bar width
 				Display display = getActivity().getWindowManager().getDefaultDisplay();   
 		        
 				_userProgress = progress;
 				 
 				int width = (int) (display.getWidth() * _userProgress.getProgressBarPercentage());
 				if (width < 0) {
 					width = 0;
 				}
 				
 				if (progressBarWidth != width){
 					animateProgressBarChange(width, _userProgress.isOverGoal());
 				} else {
 					if (_userProgress.isOverGoal()){
 						progressBar.setImageResource(R.drawable.progress_foreground_red);
 						progressBar.getLayoutParams().width = display.getWidth();
 					} else {
 						progressBar.setImageResource(R.drawable.progress_foreground);
 						progressBar.getLayoutParams().width = width;
 					}
 					
 					progressBar.requestLayout();
 				}
 				
 				// Update TextViews
 				caloriesConsumedTextView.setText(_userProgress.getProgress());
 				calorieGoalTextView.setText(_userProgress.getDailyCaloriesGoal());
 
				MyPlateFragment.this.view.findViewById(R.id.progressBar1).setVisibility(View.GONE);
 			}
 		};
 	};
 	
 	/** (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
 	 */
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		if (container == null) {
             // We have different layouts, and in one of them this
             // fragment's containing frame doesn't exist.  The fragment
             // may still be created from its saved state, but there is
             // no reason to try to create its view hierarchy because it
             // won't be displayed.  Note this is not needed -- we could
             // just run the code below, where we would create and return
             // the view hierarchy; it would just never be used.
             return null;
         }
 		
 		progressBarWidth = (Integer) DataHelper.getPref(DataHelper.PREFS_PROGRESS_BAR_WIDTH, 0);
 		
 		// Hook up outlets
 		this.view 						= (LinearLayout) inflater.inflate(R.layout.fragment_my_plate, container, false);
 		this.progressBar 				= (ImageView) this.view.findViewById(R.id.progressForegroundImageView);
         this.breakfastBtn 				= (Button) this.view.findViewById(R.id.breakfastButton);
         this.lunchBtn 					= (Button) this.view.findViewById(R.id.lunchButton);
         this.dinnerBtn 					= (Button) this.view.findViewById(R.id.dinnerButton);
         this.snacksBtn 					= (Button) this.view.findViewById(R.id.snacksButton);
         this.exerciseBtn 				= (Button) this.view.findViewById(R.id.exerciseButton);
         this.waterBtn 					= (Button) this.view.findViewById(R.id.waterButton);
 		this.caloriesConsumedTextView	= (TextView) this.view.findViewById(R.id.caloriesConsumedTextView);
 		this.calorieGoalTextView		= (TextView) this.view.findViewById(R.id.calorieGoalTextView);
 		
         this.initializeButtons();
         
 		return view;  
 	}
 	
 	public void initializeButtons(){
 		OnClickListener onClickListener = new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				int id = v.getId();
 				
 				Class<?> nextActivity = null;
 				switch(id){
 					case R.id.breakfastButton:
 						MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.BREAKFAST);
 						nextActivity = NEXT_ACTIVITY_FOOD;
 						break;
 					case R.id.lunchButton:
 						MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.LUNCH);
 						nextActivity = NEXT_ACTIVITY_FOOD;
 						break;
 					case R.id.dinnerButton:
 						MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.DINNER);
 						nextActivity = NEXT_ACTIVITY_FOOD;
 						break;
 					case R.id.snacksButton:
 						MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.SNACKS);
 						nextActivity = NEXT_ACTIVITY_FOOD;
 						break;
 					case R.id.exerciseButton:
 						nextActivity = NEXT_ACTIVITY_EXERCICE;
 						break;
 					case R.id.waterButton:
 						nextActivity = NEXT_ACTIVITY_WATER;
 						break;
 					default:
 						MyPlateApplication.setWorkingTimeOfDay(TimeOfDay.BREAKFAST);
 						nextActivity = NEXT_ACTIVITY_FOOD;
 				}
 				MyPlateApplication.setWorkingDateStamp(new Date());
 				if (nextActivity != null){
 					Intent intent = new Intent(getActivity(), nextActivity);
 					startActivity(intent);
 				}
 			}
 		};
 		
 		this.breakfastBtn.setOnClickListener(onClickListener);
 		this.lunchBtn.setOnClickListener(onClickListener);
 		this.dinnerBtn.setOnClickListener(onClickListener);
 		this.snacksBtn.setOnClickListener(onClickListener);
 		this.exerciseBtn.setOnClickListener(onClickListener);
 		this.waterBtn.setOnClickListener(onClickListener);     		
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		Log.d("MyPlateFragment","onResume");
 		this.refreshViewState();
 
         MyPlateApplication app = (MyPlateApplication) getActivity().getApplication();
         if (app.wasInBackground()) {
         	app.hasBeenLoaded();
 
         	// Diary sync etc.
         	long lastOnloadSync = DataHelper.getPref(DataHelper.PREFS_LAST_ONLOAD_SYNC, (long) 0);
         	Calendar yesterday = Calendar.getInstance();
         	yesterday.add(Calendar.DATE, -1);
         	if (lastOnloadSync < yesterday.getTime().getTime()) {
         		DataHelper.refreshData(this);
         	}
         }
 	}
 	
 	@Override
 	public void onStop() {
 		DataHelper.setPref(DataHelper.PREFS_PROGRESS_BAR_WIDTH, this.progressBarWidth);
 		
 		super.onStop();
 	}
 
 	private void refreshViewState(){
 		if (!isAdded() && !isVisible()){  
 			return; // Do not have access to Activity so must return 
 		}
 		
 		Map<DiaryEntryType, String>diaryEntry = DataHelper.getDiarySummaryPerType(new Date());
 		
 		String defaultStr = getString(R.string.track);
 		String breakfastStr = diaryEntry.get(DiaryEntryType.BREAKFAST);
 		if (breakfastStr.equals("0")){
 			this.breakfastBtn.setText(String.format(getString(R.string.btn_track_breakfast_format), defaultStr));
 		} else {
 			this.breakfastBtn.setText(String.format(getString(R.string.btn_track_breakfast_format), breakfastStr));
 			this.breakfastBtn.setBackgroundResource(R.drawable.btn_track_blue_selector);
 		}
 		
 		String lunchStr = diaryEntry.get(DiaryEntryType.LUNCH);
 		if (lunchStr.equals("0")){
 			this.lunchBtn.setText(String.format(getString(R.string.btn_track_lunch_format), defaultStr));	
 		} else {
 			this.lunchBtn.setText(String.format(getString(R.string.btn_track_lunch_format), lunchStr));
 			this.lunchBtn.setBackgroundResource(R.drawable.btn_track_blue_selector);
 		}
 				
 		String dinnerStr = diaryEntry.get(DiaryEntryType.DINNER);
 		if (dinnerStr.equals("0")){
 			this.dinnerBtn.setText(String.format(getString(R.string.btn_track_dinner_format), defaultStr));	
 		} else {
 			this.dinnerBtn.setText(String.format(getString(R.string.btn_track_dinner_format), dinnerStr));
 			this.dinnerBtn.setBackgroundResource(R.drawable.btn_track_blue_selector);
 		}
 
 		String snacksStr = diaryEntry.get(DiaryEntryType.SNACKS);
 		if (snacksStr.equals("0")){
 			this.snacksBtn.setText(String.format(getString(R.string.btn_track_snacks_format), defaultStr));
 		} else {
 			this.snacksBtn.setText(String.format(getString(R.string.btn_track_snacks_format), snacksStr));
 			this.snacksBtn.setBackgroundResource(R.drawable.btn_track_blue_selector);
 		}
 		
 		String exerciseStr = diaryEntry.get(DiaryEntryType.EXERCISE);
 		if (exerciseStr.equals("0")){
 			this.exerciseBtn.setText(String.format(getString(R.string.btn_track_exercise_format), defaultStr));
 		} else {
 			this.exerciseBtn.setText(String.format(getString(R.string.btn_track_exercise_format), exerciseStr));
 			this.exerciseBtn.setBackgroundResource(R.drawable.btn_track_orange_selector);
 		}
 		
 		String waterStr = diaryEntry.get(DiaryEntryType.WATER);
 		if (waterStr.equals("0")){
 			this.waterBtn.setText(String.format(getString(R.string.btn_track_water_format), defaultStr));
 		} else {
 			this.waterBtn.setText(String.format(getString(R.string.btn_track_water_format), waterStr));
 			this.waterBtn.setBackgroundResource(R.drawable.btn_track_blue_selector);
 		}
 		
 		new UserProgressTask().execute(new Void[]{});
 		
 		}
 
 	private void animateProgressBarChange(int targetWidth, final boolean isOverGoal){
 		if (!isOverGoal){
 			this.progressBar.setImageResource(R.drawable.progress_foreground);
 		}
 		
 		WidthAnimation animation = new WidthAnimation(this.progressBar, this.progressBarWidth, targetWidth);
 		animation.setDuration(400);
 		animation.setStartOffset(300);
 		animation.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				if (isOverGoal){
 					progressBar.setImageResource(R.drawable.progress_foreground_red);
 				}
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 
 			@Override
 			public void onAnimationStart(Animation animation) {}
 		});
 		this.progressBar.startAnimation(animation);
 		
 		this.progressBarWidth = targetWidth;
 	}
 	
 	@Override
 	public void dataReceived(Method methodCalled, Object data) {
 		if (methodCalled.equals(DataHelper.METHOD_SYNC_DIARY)) {
     		DataHelper.setPref(DataHelper.PREFS_LAST_ONLOAD_SYNC, new Date().getTime());
 
     		// Possibly received new data from the server; refresh the UI
 			this.refreshViewState();
 		}
 	}
 }
