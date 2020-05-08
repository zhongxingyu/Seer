 package awesome.app;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ScheduleLookupActivity extends Activity {
 
	private String mCurrentStudent;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Remove title bar
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.schedule_lookup);
 
 		makeButtonWork();
 		String username = getIntent().getStringExtra("username");
 		if (isValidUsername(username)) {
 			doScheduleSearch(username);
 		}
 	}
 
 	private boolean isValidUsername(String username) {
 		return username != null && username.trim().length() > 0;
 	}
 
 	private void makeButtonWork() {
 		Button button = (Button) findViewById(R.id.schedule_lookup_button);
 		button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				String searchString = ((EditText) findViewById(R.id.schedule_text))
 						.getText().toString();
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
 		TableLayout classDataTable = (TableLayout) findViewById(R.id.classDataTable);
 		for (ScheduleData classData : classList) {
 			TableRow tableRow = new TableRow(this);
 			tableRow.addView(getConfiguredTextView(classData.classNumber));
 			tableRow.addView(getConfiguredTextView(classData.className));
 			tableRow.addView(getConfiguredTextView(classData.instructor));
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
 		clearTable();
 		ScrollView scrollingTable = (ScrollView) findViewById(R.id.pageScrollView);
 		scrollingTable.setVisibility(View.VISIBLE);
 		ArrayList<ScheduleData> classList = getClassList(searchString);
 		if (classList.size() > 0) {
 			makeClassDataTable(classList);
 			makeWeeklyScheduleTable(classList);
 		} else {
 			Toast.makeText(this, getResources().getString(R.string.noClasses),
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	private void makeWeeklyScheduleTable(ArrayList<ScheduleData> classList) {
 		String[] days = { "M", "T", "W", "R", "F" };
 		for (String day : days) {
 			TableRow currentRow = new TableRow(this);
 			if (day == "M") {
 				currentRow = (TableRow) findViewById(R.id.mondayRow);
 			} else if (day == "T") {
 				currentRow = (TableRow) findViewById(R.id.tuesdayRow);
 			} else if (day == "W") {
 				currentRow = (TableRow) findViewById(R.id.wednesdayRow);
 			} else if (day == "R") {
 				currentRow = (TableRow) findViewById(R.id.thursdayRow);
 			} else if (day == "F") {
 				currentRow = (TableRow) findViewById(R.id.fridayRow);
 			}
 
 			for (int period = 1; period <= 10; period++) {
 				TextView textView = getConfiguredTextView("");
 				for (ScheduleData eachClass : classList) {
 					if (eachClass.MeetsOn(day)
 							&& eachClass.MeetingDuringPeriod(period)) {
 						textView.setText(eachClass.classNumber);
 					}
 				}
 				currentRow.addView(textView);
 			}
 		}
 	}
 
 	private ArrayList<ScheduleData> getClassList(String username) {
 		if (NetworkManager.isOnline(this)) {
 			String url = getString(R.string.serverURL)
 					+ getString(R.string.scheduleSearchURL);
 
 			// Search Parameters
 			String fieldName = getString(R.string.fieldNameLookup);
 			String quarterName = getString(R.string.quarterNameLookup);
 			String quarterSelected = getString(R.string.quarterSelectionLookup);
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 			pairs.add(new BasicNameValuePair(fieldName, username));
 			pairs.add(new BasicNameValuePair(quarterName, quarterSelected));
 
 			ScheduleHandler scheduleHandler = new ScheduleHandler();
 			NetworkManager.getData(url, scheduleHandler, pairs, this);
 			return scheduleHandler.getClassList();
 		}
 		return null;
 	}
 
 	private void clearTable() {
 		TableRow mondayRow = (TableRow) findViewById(R.id.mondayRow);
 		mondayRow.removeAllViews();
 		TableRow fridayRow = (TableRow) findViewById(R.id.fridayRow);
 		fridayRow.removeAllViews();
 		TableRow thursdayRow = (TableRow) findViewById(R.id.thursdayRow);
 		thursdayRow.removeAllViews();
 		TableRow wednesdayRow = (TableRow) findViewById(R.id.wednesdayRow);
 		wednesdayRow.removeAllViews();
 		TableRow tuesdayRow = (TableRow) findViewById(R.id.tuesdayRow);
 		tuesdayRow.removeAllViews();
 		TableLayout classDataTable = (TableLayout) findViewById(R.id.classDataTable);
 		classDataTable.removeAllViews();
 		setContentView(R.layout.schedule_lookup);
 		makeButtonWork();
 	}
 }
