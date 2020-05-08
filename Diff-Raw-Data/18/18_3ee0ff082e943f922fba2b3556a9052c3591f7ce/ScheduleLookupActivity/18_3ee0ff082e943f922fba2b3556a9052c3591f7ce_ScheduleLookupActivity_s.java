 package awesome.app.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 import awesome.app.R;
 import awesome.app.connectivity.NetworkManager;
 import awesome.app.data.ScheduleData;
 import awesome.app.handler.ScheduleHandler;
 import awesome.app.handler.ScheduleQuarterHandler;
 import awesome.app.security.PasswordManager;
 
 public class ScheduleLookupActivity extends CallBackActivity {
 
 	private String mCurrentStudent;
 	private NetworkManager mNetworkManager;
 	private ScheduleHandler mScheduleHandler;
 	private String mUsername;
 	private String mPassword;
 	private EditText mEditName;
 	private Spinner mSpinner;
 	private ScheduleQuarterHandler mQuarterHandler;
 	private boolean flag;
 	private ArrayList<ArrayList<String>> mQuarterOptionData;
 	private View scheduleDataScrollView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Remove title bar
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		flag = true;
 		setContentView(R.layout.schedule_lookup);
 		mUsername = getIntent().getStringExtra(PasswordManager.USERNAME);
 		mPassword = getIntent().getStringExtra(PasswordManager.PASSWORD);
 		setUpSpinner();
 		mScheduleHandler = new ScheduleHandler();
 		mNetworkManager = new NetworkManager(getString(R.string.serverURL)
 				+ getString(R.string.scheduleSearchURL), mScheduleHandler, this);
 
 		makeButtonWork();
 
 		mEditName = (EditText) findViewById(R.id.schedule_text);
 		mEditName.setText(getIntent().getStringExtra("usernameToSeach"));
 		mEditName.setOnKeyListener(new OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
 					String searchString = mEditName.getText().toString();
 					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(mEditName.getWindowToken(), 0);
 					if (!isValidUsername(searchString)) {
 						Toast.makeText(
 								getApplicationContext(),
 								getResources().getString(R.string.emptyTextbox),
 								Toast.LENGTH_SHORT).show();
 					} else {
 						doScheduleSearch(searchString);
 					}
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 
 	public void reinflateScheduleScrollLayout() {
 		LinearLayout dataHolderLayout = (LinearLayout) findViewById(R.id.schedule_data_holder);
 		dataHolderLayout.removeAllViews();
 		LayoutInflater inflater = (LayoutInflater) this
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		scheduleDataScrollView = inflater.inflate(R.layout.schedule_table_part,
 				dataHolderLayout, false);
 		dataHolderLayout.addView(scheduleDataScrollView);
 	}
 
 	public void setUpSpinner() {
 		mSpinner = (Spinner) findViewById(R.id.quarterSelecter);
 		mQuarterHandler = new ScheduleQuarterHandler();
 		mNetworkManager = new NetworkManager(getString(R.string.serverURL)
 				+ getString(R.string.scheduleQuarterURL), mQuarterHandler, this);
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 		pairs.add(new BasicNameValuePair(
 				getString(R.string.bandwidthUsernameVariableName), mUsername));
 		pairs.add(new BasicNameValuePair(
 				getString(R.string.bandwidthPasswordVariableName), mPassword));
 		mNetworkManager.getData(pairs);
 
 	}
 
 	private boolean isValidUsername(String username) {
 		return username != null && username.trim().length() > 0;
 	}
 
 	private void makeButtonWork() {
 		Button button = (Button) findViewById(R.id.schedule_lookup_button);
 		button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				String searchString = mEditName.getText().toString();
 				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(mEditName.getWindowToken(), 0);
 				if (!isValidUsername(searchString)) {
 					Toast.makeText(getApplicationContext(),
 							getResources().getString(R.string.emptyTextbox),
 							Toast.LENGTH_SHORT).show();
 				} else {
 					doScheduleSearch(searchString);
 				}
 			}
 		});
 	}
 
 	private void makeClassDataTable(ArrayList<ScheduleData> classList) {
 		reinflateScheduleScrollLayout();
 		TableLayout classDataTable = (TableLayout) scheduleDataScrollView
 				.findViewById(R.id.classDataTable);
 		for (ScheduleData classData : classList) {
 			TableRow tableRow = new TableRow(this);
 			tableRow.addView(getConfiguredTextView(classData.classNumber));
 			tableRow.addView(getConfiguredTextView(classData.className));
 			tableRow.addView(getConfiguredTextView(classData.instructor));
 			tableRow.addView(getConfiguredTextView(classData.GetMeetingString()));
 			tableRow.addView(getConfiguredTextView(classData.finalData));
 			classDataTable.addView(tableRow);
 		}
 	}
 
 	private TextView getConfiguredTextView(String text) {
 		TextView textView = new TextView(this);
 		textView.setPadding(8, 5, 8, 5);
 		textView.setBackgroundDrawable((Drawable) getResources().getDrawable(
 				R.drawable.cell_border));
 		textView.setTextColor(R.color.black);
 		textView.setTextSize(18);
 		textView.setText(text);
 		return textView;
 	}
 
 	private void doScheduleSearch(String searchString) {
		if (searchString == mCurrentStudent)
			return;
		mCurrentStudent = searchString;
		clearData();
		getClassList(searchString);

 	}
 
 	private void clearData() {
 		LinearLayout dataHolderLayout = (LinearLayout) findViewById(R.id.schedule_data_holder);
 		dataHolderLayout.removeAllViews();
 	}
 
 	private void makeWeeklyScheduleTable(ArrayList<ScheduleData> classList) {
 		String[] days = { "M", "T", "W", "R", "F" };
 		for (String day : days) {
 			TableRow currentRow = new TableRow(this);
 			if (day == "M") {
 				currentRow = (TableRow) scheduleDataScrollView
 						.findViewById(R.id.mondayRow);
 			} else if (day == "T") {
 				currentRow = (TableRow) scheduleDataScrollView
 						.findViewById(R.id.tuesdayRow);
 			} else if (day == "W") {
 				currentRow = (TableRow) scheduleDataScrollView
 						.findViewById(R.id.wednesdayRow);
 			} else if (day == "R") {
 				currentRow = (TableRow) scheduleDataScrollView
 						.findViewById(R.id.thursdayRow);
 			} else if (day == "F") {
 				currentRow = (TableRow) scheduleDataScrollView
 						.findViewById(R.id.fridayRow);
 			}
 
 			for (int period = 1; period <= 10; period++) {
 				TextView textView = getConfiguredTextView("");
 				for (ScheduleData eachClass : classList) {
 					if (eachClass.MeetsOn(period, day)) {
 						textView.setText(eachClass.classNumber);
 					}
 				}
 				currentRow.addView(textView);
 			}
 		}
 	}
 
 	private void getClassList(String username) {
 		if (NetworkManager.isOnline(this)) {
 			// Search Parameters
 			String fieldName = getString(R.string.fieldNameLookup);
 			String quarterName = getString(R.string.quarterNameLookup);
 			String quarterSelected = mQuarterOptionData.get(
 					mSpinner.getSelectedItemPosition()).get(1);
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 			pairs.add(new BasicNameValuePair(fieldName, username));
 			pairs.add(new BasicNameValuePair(quarterName, quarterSelected));
 			pairs.add(new BasicNameValuePair(
 					getString(R.string.bandwidthUsernameVariableName),
 					mUsername));
 			pairs.add(new BasicNameValuePair(
 					getString(R.string.bandwidthPasswordVariableName),
 					mPassword));
 			mNetworkManager.getData(pairs);
 		}
 	}
 
 	public void update() {
 		if (flag) {
 			ArrayList<String> names = new ArrayList<String>();
 			mQuarterOptionData = mQuarterHandler.getData();
 			for (List<String> list : mQuarterOptionData) {
 				names.add(list.get(0));
 			}
 			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 					android.R.layout.simple_spinner_item, names);
 			mSpinner.setAdapter(adapter);
 			flag = false;
 		} else {
 			ArrayList<ScheduleData> classList = mScheduleHandler.getClassList();
 			if (classList.size() > 0) {
 				makeClassDataTable(classList);
 				makeWeeklyScheduleTable(classList);
 			} else {
 				Toast.makeText(this,
 						getResources().getString(R.string.noClasses),
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 }
