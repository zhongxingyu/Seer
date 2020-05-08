 package ca.charland.tanita.manage;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import ca.charland.tanita.R;
 
 /**
  * The first screen you will see and will allow you basic account maintenance.
  * 
  * @author mcharland
  */
 public class FirstActivity extends RoboActivity {
 
 	@InjectView(R.id.add)
 	private Button add;
 
 	@InjectView(R.id.view)
 	private Button view;
 
 	@InjectView(R.id.settings)
 	private Button settings;
 
 	private MultipleActivityData activityData;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		int mode = R.layout.main;
 		setContentView(mode);
 		setActivitySpecificValues();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		setActivitySpecificValues();
 	}
 
 	private void setActivitySpecificValues() {
 		createActivityData();
 
 		add.setText(activityData.getAdd());
 		view.setText(activityData.getView());
 
 		setOnClickListeners();
 	}
 
 	private void createActivityData() {
 		if (PreferencesActivity.isSingleUserModeSet(this)) {
 			activityData = new PersonHomeActivityDataHolder();
 		} else {
 			activityData = new FirstActivityDataHolder();
 		}
 	}
 
 	private void setOnClickListeners() {
 		add.setOnClickListener(getOnClickListener(activityData.getNextAddClass()));
 		view.setOnClickListener(getOnClickListener(activityData.getNextViewClass()));
 		settings.setOnClickListener(getOnClickListener(PreferencesActivity.class));
 	}
 
 	private OnClickListener getOnClickListener(final Class<?> nextClass) {
 		final Activity activity = this;
 		final Intent oldIntent = getIntent();
 		return new View.OnClickListener() {
 
 			@Override
 			public void onClick(View view) {
 				startActivity(getActivity(nextClass, activity, oldIntent));
 			}
 
 			private Intent getActivity(final Class<?> nextClass, final Activity activity, final Intent oldIntent) {
 				Intent intent = new Intent(getBaseContext(), nextClass);
 				if (PreferencesActivity.isSingleUserModeSet(activity) && nextClass != PreferencesActivity.class) {
 					int id = PreferencesActivity.getPersonID(activity);
 					intent.putExtra(AllPeopleListActivity.PERSON_ID, id);
 				}
 				return intent;
 			}
 		};
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		
 		// replaces the default 'Back' button action
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
			FirstActivityDataHolder.exit(this);
 		}
 		return true;
 	}
 	
 }
