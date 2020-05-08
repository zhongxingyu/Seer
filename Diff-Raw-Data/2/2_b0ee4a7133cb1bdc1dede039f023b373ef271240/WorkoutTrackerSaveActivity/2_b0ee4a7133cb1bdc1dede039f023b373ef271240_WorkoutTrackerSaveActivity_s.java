 package com.maveric;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.maveric.contentprovider.ExceriseProvider;
 import com.maveric.contentprovider.WorkoutProvider;
 import com.maveric.database.model.ExceriseValue;
 import com.maveric.database.model.WorkOutTrackerTable;
 
 public class WorkoutTrackerSaveActivity extends MavericBaseActiity {
 
 	Context ctx;
 	TextView countTypeText;
 	TextView countTypeTime;
 	TextView exceriseTypeText;
 	TextView woroutCalories;
 	TextView caloriesCalculation;
 	EditText inputData;
 	Button saveData;
 	String countData;
 	String caloriesCount;
 	CheckBox favoriteDataSave;
 
 	int calories;
 	String exceriseType;
 	String countType;
 	Boolean isCheckbox;
 
 	@Override
 	protected void setContentToLayout() {
 		setContentView(R.layout.workoutdatasavecontainer);
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		ctx = getApplicationContext();
 
 		Bundle extras = getIntent().getExtras();
 
 		calories = extras.getInt("calories");
 		exceriseType = extras.getString("type");
 		countType = extras.getString("count");
 
 		exceriseTypeText = (TextView) findViewById(R.id.excerisetypetext);
 		exceriseTypeText.setText(exceriseType);
 		countTypeText = (TextView) findViewById(R.id.favouritebefretext);
 		countTypeTime = (TextView) findViewById(R.id.favouriteaftertext);
 		inputData = (EditText) findViewById(R.id.favouritecount);
 		caloriesCalculation = (TextView) findViewById(R.id.favouritecount);
 		favoriteDataSave = (CheckBox) findViewById(R.id.favoritcheckbox);
 		caloriesCalculation = (TextView) findViewById(R.id.favouritecaloriescount);
 
 		/*
 		 * set Text depends upon excerise
 		 */
 		if (!TextUtils.isEmpty(countType)) {
 			if (countType.equalsIgnoreCase("1")) {
 				countTypeText.setText("HowMany step?");
 				countTypeTime.setText("Count");
 			}
 		} else {
 
 		}
 
 		saveData = (Button) findViewById(R.id.saveexcerisedata);
 
 		inputData.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 
 				try {
 					countData = inputData.getText().toString();
 					caloriesCount = String.valueOf(Integer.parseInt(countData)
 							* calories);
					if (Integer.parseInt(countData) > 5) {
 						toast("Hey !! Enter correct value,Are you did excerise more than five hour? dont cheat");
 						return;
 					}
 					// calculation of calories
 					caloriesCalculation.setText(caloriesCount);
 				} catch (NumberFormatException e) {
 					toast("Hey !! Enter Number only");
 				}
 
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 
 			}
 		});
 
 		favoriteDataSave
 				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 					@Override
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						isCheckbox = isChecked;
 
 					}
 				});
 
 		saveData.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				if (isAllfilled()) {
 					final ProgressDialog progressDialog = ProgressDialog.show(
 							WorkoutTrackerSaveActivity.this, "Saving...",
 							"Wait a few sec your data is saving");
 
 					new Thread() {
 						public void run() {
 							try {
 
 								Calendar c = Calendar.getInstance();
 								SimpleDateFormat format = new SimpleDateFormat(
 										"dd-MM-yyyy");
 								String cureentDate = format.format(c.getTime());
 
 								Log.i("kumar" + this.getClass(), "date"
 										+ cureentDate);
 								ContentValues values = new ContentValues();
 
 								values.put(WorkOutTrackerTable.Column.DATE,
 										cureentDate);
 								values.put(
 										WorkOutTrackerTable.Column.SELECT_EXCERISE,
 										exceriseType);
 								values.put(WorkOutTrackerTable.Column.WORKOUT,
 										countData);
 								values.put(WorkOutTrackerTable.Column.CALORIES,
 										caloriesCount);
 
 								getContentResolver()
 										.insert(WorkoutProvider.INSERT_WORKOUT_DETAILS_URI,
 												values);
 								// save favourite
 
 								if (isCheckbox) {
 									ContentValues favoriteValues = new ContentValues();
 									favoriteValues
 											.put(ExceriseValue.Column.FAVOURITE_STATUS,
 													"1");
 									getContentResolver().update(
 											ExceriseProvider.ADD_FAVOURITE_URI,
 											favoriteValues, exceriseType, null);
 								}
 
 								handler.sendEmptyMessage(0);
 							} catch (Exception e) {
 								if (progressDialog != null) {
 									progressDialog.dismiss();
 								}
 								Log.e("kumar:" + this.getClass(),
 										"error in sve data into workout table"
 												+ e.getMessage(), e);
 								WorkoutTrackerSaveActivity.this.finish();
 							}
 							progressDialog.dismiss();
 						}
 					}.start();
 				} else
 					toast(getString(R.string.REQUIRE_FIELD_TOAST));
 			}
 
 		});
 
 	}
 
 	private Boolean isAllfilled() {
 		return !(TextUtils.isEmpty(countData));
 	}
 
 	Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 
 			switch (msg.what) {
 			case 0:
 				WorkoutTrackerSaveActivity.this.finish();
 				break;
 			case 1:
 				toast("favorite saved successfully");
 			}
 		}
 
 	};
 }
