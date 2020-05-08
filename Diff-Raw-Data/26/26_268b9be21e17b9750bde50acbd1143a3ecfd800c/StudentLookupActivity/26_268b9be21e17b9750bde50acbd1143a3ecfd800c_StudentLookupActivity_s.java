 package awesome.app;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ZoomButton;
 
 public class StudentLookupActivity extends CallBackActivity {
 
 	private Button searchButton;
 	private LinearLayout lookupLayout;
 	private LinearLayout backgroundLayout;
 	private static Handler scheduleButtonClickHandler;
 	private static Runnable launchSchedulePageTask;
 	private static String selectedUsername;
 	private NetworkManager mNetworkManager;
 	private StudentHandler studentHandler;
 	private EditText mEditName;
 
 	public static Handler getScheduleButtonClickHandler() {
 		return scheduleButtonClickHandler;
 	}
 
 	public static Runnable getLaunchSchedulePageTask() {
 		return launchSchedulePageTask;
 	}
 
 	public static String getSelectedUsername() {
 		return selectedUsername;
 	}
 
 	public static void setSelectedUsername(String username) {
 		selectedUsername = username;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.student_lookup);
 		lookupLayout = (LinearLayout) findViewById(R.id.lookup_layout);
 		backgroundLayout = (LinearLayout) findViewById(R.id.backgroundLayout);
 		studentHandler = new StudentHandler();
 		mNetworkManager = new NetworkManager(getString(R.string.serverURL) + getString(R.string.searchPage),
 				studentHandler, this);
 		scheduleButtonClickHandler = new Handler();
 		launchSchedulePageTask = new Runnable() {
 			public void run() {
 				Bundle bundle = new Bundle();
				bundle.putString("username", getSelectedUsername());
 				Intent newIntent = new Intent(getApplicationContext(), ScheduleLookupActivity.class);
 				newIntent.putExtras(bundle);
 				startActivity(newIntent);
 			}
 		};
 		searchButton = (Button) findViewById(R.id.lookup_button);
 		searchButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(mEditName.getWindowToken(), 0);
 				lookupLayout.removeAllViews();
 				doStudentSearch();
 			}
 		});
 		
 		mEditName = (EditText) findViewById(R.id.lookup_text);
 		mEditName.setOnKeyListener(new OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
 					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(mEditName.getWindowToken(), 0);
 					//The next two lines makes the loading screen display forever for some reason
 					//lookupLayout.removeAllViews();
 					//doStudentSearch();
 					
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 
 	private void doStudentSearch() {
 		String searchString = ((EditText) findViewById(R.id.lookup_text)).getText().toString().trim();
 		if (!isValidSearchString(searchString)) {
 			showBackground();
 			Toast.makeText(getApplicationContext(), getString(R.string.emptySearchErr), Toast.LENGTH_SHORT).show();
 			return;
 		}
 		peformStudentSearchRequest(searchString);
 	}
 
 	private boolean isValidSearchString(String searchString) {
 		return searchString != null && searchString.trim().length() > 0;
 	}
 
 	private void peformStudentSearchRequest(String searchString) {
 		if (NetworkManager.isOnline(this)) {
 			String fieldName = getString(R.string.fieldNameLookup);
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 			pairs.add(new BasicNameValuePair(fieldName, searchString));
 			mNetworkManager.getData(pairs);
 		} else {
 			Toast.makeText(this, "No Network Connection Available", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void update() {
 		hideBackground();
 		ArrayList<StudentData> students = studentHandler.getStudentList();
 		String searchResultInfo = getResources().getQuantityString(R.plurals.numberOfResultsAvailable, students.size(),
 				students.size());
 		createAndShowTextView(searchResultInfo);
 		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
 				Context.LAYOUT_INFLATER_SERVICE);
 		ListView resultList = (ListView) inflater.inflate(R.layout.student_list, null);
 		resultList.setAdapter(new StudentListAdapter(getApplicationContext(), students));
 		lookupLayout.addView(resultList);
 	}
 
 	private void createAndShowTextView(String content) {
 		TextView newTextView = new TextView(StudentLookupActivity.this);
 		newTextView.setText(content);
 		newTextView.setGravity(Gravity.CENTER);
 		newTextView.setTextColor(Color.DKGRAY);
 		newTextView.setFocusable(false);
 		lookupLayout.addView(newTextView);
 	}
 
 	private void hideBackground() {
 		backgroundLayout.setVisibility(View.GONE);
 	}
 
 	private void showBackground() {
 		backgroundLayout.setVisibility(View.VISIBLE);
 	}
 }
 
 class StudentListAdapter extends ArrayAdapter<StudentData> {
 	private final Context context;
 	private final ArrayList<StudentData> values;
 
 	public StudentListAdapter(Context context, ArrayList<StudentData> values) {
 		super(context, R.layout.student_data_row, values);
 		this.context = context;
 		this.values = values;
 	}
 
 	@Override
 	public View getView(int position, final View convertView, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		View rowView = inflater.inflate(R.layout.student_data_row, parent, false);
 		TextView usernameView = (TextView) rowView.findViewById(R.id.studentUsername);
 		TextView nameView = (TextView) rowView.findViewById(R.id.studentName);
 		TextView statusView = (TextView) rowView.findViewById(R.id.studentStatus);
 		TextView cmView = (TextView) rowView.findViewById(R.id.studentCM);
 		TextView departmentView = (TextView) rowView.findViewById(R.id.studentDepartment);
 		TextView phoneView = (TextView) rowView.findViewById(R.id.studentPhone);
 		TextView roomView = (TextView) rowView.findViewById(R.id.studentRoom);
 
 		usernameView.setText(values.get(position).getEntitledUsername());
 		nameView.setText(values.get(position).getName());
 		statusView.setText(values.get(position).getEntitledStatus());
 		cmView.setText(values.get(position).getEntitledCm());
 		departmentView.setText(values.get(position).getEntitledDepartment());
 		phoneView.setText(values.get(position).getEntitledPhone());
 		roomView.setText(values.get(position).getEntitledRoom());
 
 		ZoomButton goToSchedule = (ZoomButton) rowView.findViewById(R.id.checkScheduleButton);
 		goToSchedule.setTag(values.get(position).getUsername());
 		goToSchedule.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				StudentLookupActivity.setSelectedUsername((String) v.getTag());
 				StudentLookupActivity.getScheduleButtonClickHandler().post(
 						StudentLookupActivity.getLaunchSchedulePageTask());
 			}
 		});
 		return rowView;
 	}
 }
