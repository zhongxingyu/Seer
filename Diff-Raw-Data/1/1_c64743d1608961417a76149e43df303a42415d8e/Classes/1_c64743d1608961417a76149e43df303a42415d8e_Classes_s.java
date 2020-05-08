 package info.vanderkooy.ucheck;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Tracker;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Classes extends Activity {
 	private APIHandler handler;
 	private Preferences prefs;
 	private JSONObject data;
 	private HashMap<String, String> stopIDs;
 	private String unenrollSubject;
 	private ArrayList<String> studies;
 	private Object[] studieArray;
 	private JSONArray enrollments;
 	private Spinner spinner;
 	private ProgressDialog dialog;
 	private Button refreshButton;
 	private Button enrollButton;
 	private Tracker tracker;
 
 	private Map<String, String> studieLijst = Meta.getStudieLijst();
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.classes);
 		spinner = (Spinner) findViewById(R.id.spinner);
 		refreshButton = (Button) findViewById(R.id.refresh);
 		enrollButton = (Button) findViewById(R.id.add);
 		handler = new APIHandler(getApplicationContext());
 		prefs = new Preferences(getApplicationContext());
 		tracker = GoogleAnalytics.getInstance(getApplicationContext())
 				.getDefaultTracker();
 
 		refreshButton.setOnClickListener(refreshListener);
 		enrollButton.setOnClickListener(enrollListener);
 		spinner.setVisibility(8);
 		prefs.forceNewClasses();
 		
 		studies = new ArrayList<String>();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		tracker.trackView("/classes");
 		if (prefs.classesNeedUpdate()) {
 			tracker.trackEvent("Classes", "load", "auto", (long) 0);
 			load();
 		}
 	}
 
 	private void load() {
 		dialog = ProgressDialog.show(Classes.this, "", getString(R.string.getClasses), true);
 		dialog.setCancelable(true);
 
 		Thread thread = new Thread(new Runnable() {
 			public void run() {
 				if (handler.isNetworkAvailable()) {
 					data = handler.getClasses();
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							if (dialog.isShowing()) {
 								processData();
 								dialog.hide();
 								dialog.dismiss();
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
 
 	private void processData() {
 		if (data == null) {
 			Toast toast = Toast.makeText(getApplicationContext(),
 					getString(R.string.loadError), Toast.LENGTH_LONG);
 			toast.show();
 		} else {
 			prefs.setLastClassesUpdate();
 			try {
 				stopIDs = new HashMap<String, String>();
 				enrollments = data.getJSONArray("inschrijvingen");
 				for (int i = 0; i < enrollments.length(); i++) {
 					String studieString = enrollments.getJSONObject(i).getString("studie");
 					if(!studies.contains(studieString) && !studieString.equals(""))
 						studies.add(studieString);
 					stopIDs.put(enrollments.getJSONObject(i).getString("vak"),
 							enrollments.getJSONObject(i).getString("stopid"));
 				}
 				studieArray = studies.toArray();
 				if (studieArray.length > 1) {
 					spinner.setVisibility(0);
 					for (int i = 0; i < studies.size(); i++) {
 						tracker.trackEvent("uCheck", "Studies",
 								studieLijst.get(studieArray[i]), (long) 0);
 					}
 					updateSpinner();
 				} else {
 					spinner.setVisibility(8);
 					if (studieArray.length > 0)
 						tracker.trackEvent("uCheck", "Studies",
 								studieLijst.get((String) studieArray[0]),
 								(long) 0);
 					makeList(getString(R.string.allClasses));
 				}
 			} catch (JSONException e) {
 				tracker.trackEvent("Exception", "Classes",
 						"processData JSONException", (long) 0);
 				Toast toast = Toast.makeText(getApplicationContext(),
 						getString(R.string.loadError), Toast.LENGTH_LONG);
 				toast.show();
 				prefs.forceNewClasses();
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void updateSpinner() {
 		ArrayList<String> spinnerArray = new ArrayList<String>();
 		spinnerArray.add(getString(R.string.allClasses));
 		for (int i = 0; i < studieArray.length; i++) {
 			if (studieLijst.get(studieArray[i]) != null)
 				spinnerArray.add(studieLijst.get(studieArray[i]));
 			else
 				spinnerArray.add((String) studieArray[i]);
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
 		map.put("classes", getString(R.string.subject));
 		map.put("info", getString(R.string.info));
 		mylist.add(map);
 		for (int i = 0; i < enrollments.length(); i++) {
 			map = new HashMap<String, String>();
 			try {
 				map.put("classes",
 						(String) enrollments.getJSONObject(i).get("vak"));
 				map.put("info", (String) enrollments.getJSONObject(i).get("id"));
 				studie = (String) enrollments.getJSONObject(i).get("studie");
 			} catch (JSONException e) {
 				tracker.trackEvent("Exception", "Classes",
 						"makeList JSONException", (long) 0);
 				e.printStackTrace();
 			}
 			if (subject.equals(getString(R.string.allClasses))
 					|| subject.equals(studie)) {
 				mylist.add(map);
 			}
 		}
 		ListAdapter mSchedule = new ListAdapter(this, mylist,
 				R.layout.rowclasses, new String[] { "classes", "info" },
 				new int[] { R.id.classes, R.id.info });
 		list.setAdapter(mSchedule);
 		list.setOnItemLongClickListener(unenrollListener);
 		list.setSelector(android.R.color.transparent);
 
 	}
 
 	private OnClickListener enrollListener = new OnClickListener() {
 		public void onClick(View v) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(Classes.this);
 			CharSequence[] cs = { "Inschrijven", "Uitschrijven" };
 			builder.setTitle("Wat wil je doen?")
 					.setItems(cs, enrollOptionListener).show();
 		}
 	};
 
 	private DialogInterface.OnClickListener enrollOptionListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			switch (which) {
 			case 0:
 				verifyEnrollPass();
 				break;
 
 			case 1:
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						Classes.this);
 				builder.setMessage(
 						"Houd een inschrijving lang ingedrukt om je er voor uit te schrijven")
 						.setNeutralButton("OK", emptyListener).show();
 				break;
 			}
 
 		}
 	};
 
 	private void verifyEnrollPass() {
 		Intent verifyPassIntent = new Intent().setClass(Classes.this,
 				ConfirmPass.class);
 		startActivityForResult(verifyPassIntent, 1);
 	}
 
 	private void verifyUnenrollPass() {
 		Intent verifyPassIntent = new Intent().setClass(Classes.this,
 				ConfirmPass.class);
 		startActivityForResult(verifyPassIntent, 2);
 	}
 
 	String response = "";
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case 1:
 			if (resultCode == 1) {
 				Intent enrollIntent = new Intent().setClass(Classes.this,
 						Enroll.class);
 				enrollIntent.putExtra("studies", studies);
 				startActivity(enrollIntent);
 			}
 			break;
 		case 2:
 			if (resultCode == 1) {
 				dialog = ProgressDialog
 						.show(Classes.this, "", "Bezig...", true);
 
 				Thread thread = new Thread(new Runnable() {
 					public void run() {
 						if (handler.isNetworkAvailable()) {
 							response = handler.unenroll(stopIDs.get(unenrollSubject));
 							runOnUiThread(new Runnable() {
 								@Override
 								public void run() {
 									if (dialog.isShowing()) {
 										dialog.hide();
 										dialog.dismiss();
 									}
 									Toast toast = Toast.makeText(
 											getApplicationContext(),
 											"Antwoord van uSis: " + response,
 											Toast.LENGTH_LONG);
 									toast.show();
 									load();
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
 			break;
 		}
 	}
 
 	private DialogInterface.OnClickListener emptyListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 
 		}
 	};
 
 	private OnItemLongClickListener unenrollListener = new OnItemLongClickListener() {
 		@Override
 		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 				int arg2, long arg3) {
 			if (arg2 == 0) {
 				// First row, not a subject
 			} else {
 				unenrollSubject = (String) ((TextView) arg1
 						.findViewById(R.id.classes)).getText();
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						Classes.this);
 				builder.setMessage(
 						"Weet je zeker dat je je wilt uitschrijven voor: "
 								+ unenrollSubject + "?")
 						.setPositiveButton("Ja", unenrollDialogClickListener)
 						.setNegativeButton("Nee", unenrollDialogClickListener)
 						.show();
 			}
 			return false;
 		}
 	};
 
 	DialogInterface.OnClickListener unenrollDialogClickListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface _dialog, int which) {
 			switch (which) {
 			case DialogInterface.BUTTON_POSITIVE:
 				verifyUnenrollPass();
 				break;
 
 			case DialogInterface.BUTTON_NEGATIVE:
 				// No button clicked
 				break;
 			}
 		}
 	};
 
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
 			tracker.trackEvent("Classes", "load", "manual", (long) 0);
 			load();
 		}
 	};
 	
 }
