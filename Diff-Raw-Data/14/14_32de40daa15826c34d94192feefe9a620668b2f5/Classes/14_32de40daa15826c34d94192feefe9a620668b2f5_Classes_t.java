 package info.vanderkooy.ucheck;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class Classes extends Activity {
 	private APIHandler handler;
 	private Preferences prefs;
 	private JSONObject data;
 	private JSONArray studies;
 	private JSONArray enrollments;
 	private Spinner spinner;
 	private ProgressDialog dialog;
 
 	private Map<String, String> studieLijst = Meta.getStudieLijst();
 
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.classes);
 		spinner = (Spinner) findViewById(R.id.spinner);
 		handler = new APIHandler(getApplicationContext());
 		prefs = new Preferences(getApplicationContext());
 
 		prefs.forceNewClasses();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (prefs.classesNeedUpdate()) {
 			load();
 		}
 	}
 
     public void refreshData(View view) {
         load();
     }
 	
 	private void load() {
 		dialog = ProgressDialog.show(Classes.this, "", "Inschrijvingen worden opgehaald", true);
 
 		Thread thread = new Thread(new Runnable() {
 			public void run() {
 				data = handler.getClasses();
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						processData();
 						if (dialog.isShowing()) {
 							dialog.hide();
 							dialog.dismiss();
 						}
 					}
 				});
 			}
 		});
 		thread.start();
 	}
 
 	private void processData() {
 		if (data == null) {
 			Toast toast = Toast
 					.makeText(
 							getApplicationContext(),
 							"Er is iets mis gegaan bij het ophalen van cijferdata. Probeer het later nog een keer.",
 							6);
 			toast.show();
 		}
 		prefs.setLastClassesUpdate();
 		try {
 			studies = data.getJSONArray("studies");
 			enrollments = data.getJSONArray("inschrijvingen");
 			if (studies.length() > 1) {
 				spinner.setVisibility(0);
 				updateSpinner();
 			} else {
 				spinner.setVisibility(8);
				makeList("Alle vakken");
 			}
 		} catch (JSONException e) {
 			Toast toast = Toast.makeText(getApplicationContext(), 
 					"Er is iets mis gegaan bij het ophalen van cijferdata. Probeer het later nog een keer.", 6);
 			toast.show();
 			prefs.forceNewClasses();
 			e.printStackTrace();
 		}
 	}
 
 	private void updateSpinner() {
 		ArrayList<String> spinnerArray = new ArrayList<String>();
 		spinnerArray.add("Alle vakken");
 		for (int i = 0; i < studies.length(); i++) {
 			try {
 				if (studieLijst.get((String) studies.get(i)) != null)
 					spinnerArray.add(studieLijst.get(studies.get(i)));
 				else
 					spinnerArray.add((String) studies.get(i));
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
 				this, android.R.layout.simple_spinner_item, spinnerArray);
 		spinnerArrayAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(spinnerArrayAdapter);
 		spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
 	}
 
 	private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
 		for (Entry<T, E> entry : map.entrySet()) {
 			if (value.equals(entry.getValue())) {
 				return entry.getKey();
 			}
 		}
 		return null;
 	}
 
 	public void makeList(String subject) {
 		ListView list = (ListView) findViewById(R.id.list);
 		String studie = "";
 		 
 		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
 		HashMap<String, String> map;
 		map = new HashMap<String, String>();
 		map.put("classes", "Vak");
 		map.put("info", "Info");
 		mylist.add(map);
 		for (int i = 0; i < enrollments.length(); i++) {
 			map = new HashMap<String, String>();
 			try {
 				map.put("classes", (String) enrollments.getJSONObject(i).get("vak"));
 				map.put("info", (String) enrollments.getJSONObject(i).get("id"));
 				studie = (String) enrollments.getJSONObject(i).get("studie");
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			if(subject.equals("Alle vakken") || subject.equals(studie)) {
 				mylist.add(map);
 			}
 		}
 		ListAdapter mSchedule = new ListAdapter(this, mylist, R.layout.rowclasses,
 		            new String[] {"classes", "info"}, new int[] {R.id.classes, R.id.info});
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
 }
