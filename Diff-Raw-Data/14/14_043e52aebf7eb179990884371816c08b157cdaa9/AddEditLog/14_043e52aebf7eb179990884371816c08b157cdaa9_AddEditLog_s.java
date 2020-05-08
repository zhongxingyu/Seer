 package me.toddpickell.baristalog;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.NumberPicker;
 import android.widget.TextView;
 
 public class AddEditLog extends Activity implements OnClickListener {
 
 	private String deviceName;
 	private ArrayList<Integer> device_sub_times;
 	private ArrayList<String> device_sub_titles;
 	private String device_sub_title_one;
 	private String device_sub_title_two;
 	private String device_sub_title_three;
 	private DeviceState device;
 	private List<Integer> subTimes;
 	private List<String> subTitles;
 	private TextView device_label;
 	private TextView date_label;
 	private EditText coffee_notes;
 	private EditText coffee_blend;
 	private TextView pre_label;
 	private TextView bloom_label;
 	private TextView brew_label;
 	private TextView pre_label_time;
 	private TextView bloom_label_time;
 	private TextView brew_label_time;
 	private Button save_log_button;
 
 	private DataManager data_manager;
 	private NumberPicker rating_picker;
 	private NumberPicker temp_picker;
 	private NumberPicker grind_picker;
 	private NumberPicker tamp_picker;
 	private EditText rating_edit_text;
 	private EditText temp_edit_text;
 	private EditText grind_edit_text;
 	private EditText tamp_edit_text;
 
 	@SuppressLint({ "NewApi", "DefaultLocale" })
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_log);
 
 		deviceName = getIntent().getStringExtra("device_name");
 		Log.d("DEBUG_ME!", "device name: " + deviceName);
 		device = new DeviceState(this, deviceName);
 
 		data_manager = new DataManager(this);
 
 		subTimes = device.getSubTimes();
 		subTitles = device.getSubTitles();
 
 		device_label = (TextView) findViewById(R.id.device_label);
 		date_label = (TextView) findViewById(R.id.date_label);
 		coffee_notes = (EditText) findViewById(R.id.log_note_edit);
 		coffee_blend = (EditText) findViewById(R.id.blend_name_edit);
 		pre_label = (TextView) findViewById(R.id.pre_label);
 		bloom_label = (TextView) findViewById(R.id.bloom_label);
 		brew_label = (TextView) findViewById(R.id.brew_label);
 		save_log_button = (Button) findViewById(R.id.save_log_button);
 
 		save_log_button.setOnClickListener(this);
 
 		LinearLayout time_container = (LinearLayout) findViewById(R.id.time_container);
 
 		pre_label_time = new TextView(this);
 		bloom_label_time = new TextView(this);
 		brew_label_time = new TextView(this);
 
 		pre_label_time.setTextSize(18.0F);
 		bloom_label_time.setTextSize(18.0F);
 		brew_label_time.setTextSize(18.0F);
 		pre_label_time.setPadding(0, 10, 0, 0);
 		bloom_label_time.setPadding(0, 10, 0, 0);
 		brew_label_time.setPadding(0, 10, 0, 0);
 
 		pre_label_time.setLayoutParams(new LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		bloom_label_time.setLayoutParams(new LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		brew_label_time.setLayoutParams(new LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
 		coffee_notes.setMaxLines(2);
 		// I am pretty sure this does nothing b/c it is an edittext w/
 		// multiline and this is for textview
 		Date date = new Date();
 		java.text.DateFormat df = android.text.format.DateFormat
 				.getDateFormat(this);
 		device_label.setText(formatToCapWords(deviceName));
 		date_label.setText(df.format(date));
 
 		/*
 		 * <TextView android:id="@+id/brew_label"
 		 * android:layout_width="wrap_content"
 		 * android:layout_height="wrap_content" android:paddingTop="10dp"
 		 * android:textSize="18sp" android:color="@android:color/black" />
 		 */
 
 		/* more than one way to skin a cat when sh*t dont work right */
 
 		Log.d("FUCH", "SubTimes => " + subTimes.toString());
 		Log.d("FUCH", "SubTitles => " + subTitles.toString());
 
 		if (device.getDevice_type().equals("espresso")) {
 			pre_label.setText("Time");
 			pre_label_time.setText(getIntent().getStringExtra("shot_time"));
 		} else {
 
 			if (subTitles.size() > 0) {
 				pre_label.setText(subTitles.get(0));
 				pre_label_time.setText(subTimes.get(0).toString());
 			}
 			if (subTitles.size() > 1) {
 				bloom_label.setText(subTitles.get(1));
 				bloom_label_time.setText(subTimes.get(1).toString());
 			}
 			if (subTitles.size() > 2) {
 				brew_label.setText(subTitles.get(2));
 				brew_label_time.setText(subTimes.get(2).toString());
 			}
 		}
 		time_container.addView(pre_label_time);
 		time_container.addView(bloom_label_time);
 		time_container.addView(brew_label_time);
 
 		LinearLayout number_pickers_container = (LinearLayout) findViewById(R.id.number_pickers_container);
 		TextView tamp_label = (TextView) findViewById(R.id.tamp_label);
 
 		if (Build.VERSION.SDK_INT >= 11) {
 			rating_picker = new NumberPicker(this);
 			temp_picker = new NumberPicker(this);
 			grind_picker = new NumberPicker(this);
 			rating_picker
 					.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
 			temp_picker
 					.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
 			grind_picker
 					.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
 
 			// could vary these by device as well for optimal default
 			// settings.
 			rating_picker.setMinValue(1);
 			rating_picker.setMaxValue(5);
 			rating_picker.setValue(5);
 			rating_picker.setWrapSelectorWheel(false);
 			rating_picker.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			number_pickers_container.addView(rating_picker);
 
 			temp_picker.setMinValue(150);
 			temp_picker.setMaxValue(225);
 			temp_picker.setValue(205);
 			temp_picker.setWrapSelectorWheel(false);
 			temp_picker.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			number_pickers_container.addView(temp_picker);
 
 			grind_picker.setMinValue(1);
 			grind_picker.setMaxValue(40);
 			grind_picker.setValue(10);
 			grind_picker.setWrapSelectorWheel(false);
 			grind_picker.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			number_pickers_container.addView(grind_picker);
 
 			if (deviceName.equals("espresso")) {
 
 				tamp_picker = new NumberPicker(this);
 				tamp_picker
 						.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
 				tamp_label.setText("Tamp");
 				tamp_picker.setMinValue(20);
 				tamp_picker.setMaxValue(40);
 				tamp_picker.setValue(30);
 				tamp_picker.setWrapSelectorWheel(false);
 				tamp_picker.setLayoutParams(new LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 				number_pickers_container.addView(tamp_picker);
 			}
 
 		} else {
 			rating_edit_text = new EditText(this);
 			temp_edit_text = new EditText(this);
 			grind_edit_text = new EditText(this);
 
 			rating_edit_text.setHint("1-5");
 			temp_edit_text.setHint("150-225");
 			grind_edit_text.setHint("1-40");
 
 			rating_edit_text.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			temp_edit_text.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			grind_edit_text.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			number_pickers_container.addView(rating_edit_text);
 			number_pickers_container.addView(temp_edit_text);
 			number_pickers_container.addView(grind_edit_text);
 
 			if (deviceName.equals("espresso")) {
 				tamp_edit_text = new EditText(this);
 				tamp_edit_text.setHint("20-40");
 				tamp_edit_text.setLayoutParams(new LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 				number_pickers_container.addView(tamp_picker);
 			}
 		}
 
 	}
 
 	/**** Button interaction methods ****/
 
 	public void onClick(View view) {
 		if (view.equals(save_log_button)) {
 			Log.d("WTF_SQL", "called in onClick from save log button");// save_log_button
 			saveLogToDB();
 			Intent intent = new Intent();
 			setResult(RESULT_OK, intent);
 			finish();
 		} else {
 			// if had another button
 		}
 	}
 
 	private void saveLogToDB() {
 		saveLogNote(createLogNote());
 	}
 
 	private void saveLogNote(LogNote log_note) {
 		Log.d("SAVE_DB", "LogNote out => " + log_note.toString());
 		Long returned_id = data_manager.saveLogNote(log_note);
 		Log.d("SAVE_DB", "the returned id from saveLogNote: " + returned_id);
 	}
 
 	@SuppressLint("NewApi")
 	private LogNote createLogNote() {
 		LogNote log_note = new LogNote();
 		log_note.setDevice(deviceName);
 		log_note.setNotes(coffee_notes.getText().toString());
 		log_note.setBlend(coffee_blend.getText().toString());
 		log_note.setDate(date_label.getText().toString());
 		log_note.setRating(Build.VERSION.SDK_INT >= 11 ? rating_picker
 				.getValue() : Integer.parseInt(rating_edit_text.getText()
 				.toString()));
 		log_note.setTemp(Build.VERSION.SDK_INT >= 11 ? temp_picker.getValue()
 				: Integer.parseInt(temp_edit_text.getText().toString()));
 		log_note.setGrind(Build.VERSION.SDK_INT >= 11 ? grind_picker.getValue()
 				: Integer.parseInt(grind_edit_text.getText().toString()));
 
 		/// !!! this is a bit ugly !!!
 		log_note.setTamp(deviceName.equals("espresso") ? Build.VERSION.SDK_INT >= 11 ? tamp_picker.getValue() : Integer.parseInt(tamp_edit_text.getText().toString()) : 0);
		log_note.setPre_time(Integer.parseInt(pre_label_time.getText().toString()));
 		log_note.setBloom_time(subTimes.size() > 1 ? subTimes.get(1) : 0);
 		log_note.setBrew_time(subTimes.size() > 2 ? subTimes.get(2) : 0);
 
 		return log_note;
 	}
 
 	private String formatToCapWords(String words) {
 		int space = words.indexOf(" ");
 		String returnString;
 
 		if (space > 0) {
 			returnString = capWord(words.substring(0, space)) + " "
 					+ capWord(words.substring(space + 1));
 
 		} else {
 			returnString = capWord(words);
 		}
 
 		return returnString;
 	}
 
 	@SuppressLint("DefaultLocale")
 	private String capWord(String word) {
 
 		return word.substring(0, 1).toUpperCase() + word.substring(1);
 	}
 
 }
