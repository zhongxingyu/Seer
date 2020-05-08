 package info.vanderkooy.ucheck;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Tracker;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class Grades extends Activity {
 	private APIHandler handler;
 	private Preferences prefs;
 	private JSONObject data;
 	private JSONArray subjects;
 	private List<String> studies;
 	private Spinner spinner;
 	private ProgressDialog dialog;
 	private Button refreshButton;
 	private Tracker tracker;
 
 	private Map<String, String> studieLijst = Meta.getStudieLijst();
 
 	private int numberOfStudies;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.grades);
 		
 		tracker = GoogleAnalytics.getInstance(getApplicationContext()).getDefaultTracker();
 
 		spinner = (Spinner) findViewById(R.id.spinner);
 		refreshButton = (Button) findViewById(R.id.refresh);
 		handler = new APIHandler(getApplicationContext());
 		prefs = new Preferences(getApplicationContext());
 		studies = new ArrayList<String>();
 		
 		refreshButton.setOnClickListener(refreshListener);
 		spinner.setVisibility(8);
 		prefs.forceNewGrades();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		tracker.trackView("/grades");
 		if (prefs.gradesNeedUpdate()) {
 			tracker.trackEvent("Grades", "load", "auto", (long) 0);
 			load();
 		}
 	}
 
 	private void load() {
 		dialog = ProgressDialog.show(Grades.this, "", getString(R.string.getGrades),
 				true);
 
 		Thread thread = new Thread(new Runnable() {
 			public void run() {
 				if(handler.isNetworkAvailable()) {
 					data = handler.getGrades();
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							int success = processData();
 							if (dialog.isShowing()) {
 								dialog.hide();
 								dialog.dismiss();
 							}
 							if (success == -1 && !prefs.getKey().equals("")) {
 								Intent loginIntent = new Intent().setClass(
 										Grades.this, Login.class);
 								Grades.this.startActivity(loginIntent);
 							}
 						}
 					});
 				} else {
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							if (dialog.isShowing()) {
 								dialog.hide();
 								dialog.dismiss();
 							}
 							handler.noNetworkToast();
 						}
 					});
 				}
 			}
 		});
 		thread.start();
 	}
 
 	private int processData() {
 		if (data == null) {
 			Toast toast = Toast
 					.makeText(
 							getApplicationContext(),
 							getString(R.string.loadError),
 							Toast.LENGTH_LONG);
 			toast.show();
 			return 0;
 		} else {
 			String loginerror = "uninit";
 			try {
 				loginerror = data.getString("error");
 			} catch (JSONException e1) {
 				// No error, so password was correct
 			}
 			if (loginerror.equals("loginerror")) {
 				return -1;
 			}
 			prefs.setLastGradesUpdate();
 			try {
 				subjects = data.getJSONArray("vakken");
 			} catch (JSONException e) {
				tracker.trackEvent("Exception", "Grades", "processData subjects =", (long) 0);
 				Toast toast = Toast
 						.makeText(
 								getApplicationContext(),
 								getString(R.string.loadError),
 								Toast.LENGTH_LONG);
 				toast.show();
 				e.printStackTrace();
 			}
 			numberOfStudies = 0;
 			studies.clear();
 			for (int i = 0; i < subjects.length(); i++) {
 				String vak = "";
 				try {
 					vak = (String) subjects.getJSONObject(i).get("studie");
 				} catch (JSONException e) {
					tracker.trackEvent("Exception", "Grades", "processData vak =", (long) 0);
 					e.printStackTrace();
 				}
 				if (!studies.contains(vak) && !vak.equals("")) {
 					studies.add(vak);
 					numberOfStudies++;
 				}
 			}
 			if (numberOfStudies > 1) {
 				spinner.setVisibility(0);
 				updateSpinner();
 			} else {
 				spinner.setVisibility(8);
 				makeList(getString(R.string.allGrades));
 			}
 			return 0;
 		}
 	}
 
 	private void updateSpinner() {
 		ArrayList<String> spinnerArray = new ArrayList<String>();
 		spinnerArray.add(getString(R.string.allGrades));
 		for (int i = 0; i < studies.size(); i++) {
 			if (studieLijst.get(studies.get(i)) != null)
 				spinnerArray.add(studieLijst.get(studies.get(i)));
 			else
 				spinnerArray.add(studies.get(i));
 		}
 
 		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
 				this, android.R.layout.simple_spinner_item, spinnerArray);
 		spinnerArrayAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(spinnerArrayAdapter);
 		spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
 	}
 
 	private <T, E> T getKeyByValue(Map<T, E> map, E value) {
 		for (Entry<T, E> entry : map.entrySet()) {
 			if (value.equals(entry.getValue())) {
 				return entry.getKey();
 			}
 		}
 		return null;
 	}
 
 	private void makeList(String subject) {
 		ListView list = (ListView) findViewById(R.id.list);
 		String studie = "";
 
 		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
 		HashMap<String, String> map;
 		map = new HashMap<String, String>();
 		map.put("subject", getString(R.string.subject));
 		map.put("grade", getString(R.string.grade));
 		map.put("EC", getString(R.string.EC));
 		mylist.add(map);
 		for (int i = 0; i < subjects.length(); i++) {
 			map = new HashMap<String, String>();
 			try {
 				map.put("subject", (String) subjects.getJSONObject(i)
 						.get("vak"));
 				map.put("grade",
 						(String) subjects.getJSONObject(i).get("cijfer"));
 				map.put("EC", (String) subjects.getJSONObject(i).get("ects"));
 				if (!(Boolean) subjects.getJSONObject(i).get("gehaald"))
 					map.put("gehaald", "false");
 				studie = (String) subjects.getJSONObject(i).get("studie");
 			} catch (JSONException e) {
				tracker.trackEvent("Exception", "Grades", "makeList JSONException", (long) 0);
 				e.printStackTrace();
 			}
 			if (subject.equals(getString(R.string.allGrades)) || subject.equals(studie)) {
 				mylist.add(map);
 			}
 		}
 		ListAdapter mSchedule = new ListAdapter(this, mylist,
 				R.layout.rowgrades, new String[] { "subject", "grade", "EC" },
 				new int[] { R.id.subject, R.id.grade, R.id.EC });
 		list.setAdapter(mSchedule);
 		list.setSelector(android.R.color.transparent);
 
 	}
 
 	private class MyOnItemSelectedListener implements OnItemSelectedListener {
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 			String value = getKeyByValue(studieLijst,
 					parent.getItemAtPosition(pos).toString());
 			String subject = "";
 			if (value != null)
 				subject = value;
 			else
 				subject = parent.getItemAtPosition(pos).toString();
 			makeList(subject);
 		}
 
 		public void onNothingSelected(AdapterView<?> parent) {
 			// Do nothing.
 		}
 	}
 	
 	private OnClickListener refreshListener = new OnClickListener() {
 		public void onClick(View v) {
 			tracker.trackEvent("Grades", "load", "manual", (long) 0);
 			load();
 		}
 	};
 }
