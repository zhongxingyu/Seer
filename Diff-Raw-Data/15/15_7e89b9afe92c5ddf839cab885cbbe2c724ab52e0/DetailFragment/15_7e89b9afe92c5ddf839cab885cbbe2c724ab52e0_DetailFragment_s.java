 package de.da_sense.moses.client;
 
 import java.util.ArrayList;
 
 import android.app.Fragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
import de.da_sense.moses.client.abstraction.SensorsEnum;
 import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
 import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplicationsManager;
import de.da_sense.moses.client.abstraction.apks.ImageAdapter;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
 import de.da_sense.moses.client.service.MosesService;
 import de.da_sense.moses.client.util.Log;
 
 /**
  * Shows the details for a user study.
  * 
  * @author Sandra Amend, Wladimir Schmidt
  */
 public class DetailFragment extends Fragment {
 
 	/** Belongs to Available. Constant for creating the view. */
 	protected final static int AVAILABLE = 0;
 	/** Belongs to Running. Constant for creating the view. */
 	protected final static int RUNNING = 1;
 	/** Belongs to History. Constant for creating the view. */
 	protected final static int HISTORY = 2;
 	/** a log tag for this class */
 	private final static String TAG = "DetailFragment";
 
 	/**
 	 * 
 	 * @return the index from the arguments
 	 */
 	public int getShownIndex() {
 		return getArguments().getInt("index", 0);
 	}
 
 	/**
 	 * 
 	 * @return the application name
 	 */
 	public String getShownAppName() {
 		return getArguments().getString("de.da_sense.moses.client.index");
 	}
 
 	/**
 	 * Constructor for a new placeholder DetailFragment.
 	 * 
 	 * @param index
 	 *            the index of the detail to show
 	 * @return the created DetailFragment
 	 */
 	public static DetailFragment newInstance() {
 		DetailFragment f = new DetailFragment();
 		// set arguments for the Fragment
 		Bundle args = new Bundle();
 		args.putString("de.da_sense.moses.client.placeholder", "yes");
 		f.setArguments(args);
 
 		return f;
 	}
 
 	/**
 	 * Constructor for a new DetailFragment with the application information.
 	 * 
 	 * @param index the index of the app in the list
 	 * @param appname the name of the app
 	 * @param description the description for the app
 	 * @param id the apkid
 	 * @param apkVersion the apkVersion
 	 * @param startDate the start date
 	 * @param endDate the end date
 	 * @return the created DetailFragment
 	 */
 	public static DetailFragment newInstance(int index, int belongsTo,
 			String appname, String description,
 			String id, String apkVersion, String startDate, String endDate) {
 		DetailFragment f = new DetailFragment();
 		// set arguments for the Fragment
 		Bundle args = new Bundle();
 		args.putInt("de.da_sense.moses.client.index", index);
 		args.putInt("de.da_sense.moses.client.belongsTo", belongsTo);
 		args.putString("de.da_sense.moses.client.appname", appname);
 		args.putString("de.da_sense.moses.client" +
 				".description", description);
 		args.putString("de.da_sense.moses.client.apkid", id);
 		args.putString("de.da_sense.moses.client.apkVersion", apkVersion);
 		args.putString("de.da_sense.moses.client.startDate", startDate);
 		args.putString("de.da_sense.moses.client.endDate", endDate);
 		f.setArguments(args);
 
 		return f;
 	}
 
 	/**
 	 * Constructor for a new DetailFragment with the application information.
 	 * 
 	 * @param appname the name of the app
 	 * @param description the description for the app
 	 * @param sensors the sensors the app wants to use
 	 * @param id the apkid
 	 * @param apkVersion the apkVersion
 	 * @param startDate the start date
 	 * @param endDate the end date
 	 * @return the created DetailFragment
 	 */
 	public static DetailFragment newInstance(int belongsTo, String appname,
 			String description, String id,
 			String apkVersion, String startDate, String endDate) {
 		DetailFragment f = new DetailFragment();
 		// set arguments for the Fragment
 		Bundle args = new Bundle();
 		args.putInt("de.da_sense.moses.client.index", 0);
 		args.putInt("de.da_sense.moses.client.belongsTo", belongsTo);
 		args.putString("de.da_sense.moses.client.appname", appname);
 		args.putString("de.da_sense.moses.client.description", description);
 		args.putString("de.da_sense.moses.client.apkid", id);
 		args.putString("de.da_sense.moses.client.apkVersion", apkVersion);
 		args.putString("de.da_sense.moses.client.startDate", startDate);
 		args.putString("de.da_sense.moses.client.endDate", endDate);
 		f.setArguments(args);
 
 		return f;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		if (container == null) {
 			return null;
 		} else {
 		container.setBackgroundColor(getResources().getColor(
 				android.R.color.background_light));
 
 		final View detailFragmentView; // should get set in all cases
 		// get the arguments
 		final String appname;
 		final int belongsTo, index;
 		String description;
 		final String apkid;
 		String startDate, endDate, apkVersion;
 		ArrayList<Integer> sensors;
 
 		// if this gets started as a fragment
 		if (savedInstanceState == null) {
 			Log.d(TAG, "savedInstance == null");
 			savedInstanceState = getArguments();
 			Log.d(TAG, "NOW savedInstance = " + savedInstanceState);
 		}
 
 		// supposed to show a placeholder?
 		String placeholder = savedInstanceState
 				.getString("de.da_sense.moses.client.placeholder");
 		if (placeholder != null && placeholder.equals("yes")) {
 			// inflate the placeholder
 			Log.d(TAG, "onCreateView about to inflate PLACEHOLDER");
 			detailFragmentView = inflater.inflate(
 					R.layout.app_info_placeholder, container, false);
 		} else { // normal display of details
 			// retrieve the arguments
 			index = savedInstanceState.getInt("de.da_sense.moses.client.index");
 			belongsTo = savedInstanceState
 					.getInt("de.da_sense.moses.client.belongsTo");
 			appname = savedInstanceState
 					.getString("de.da_sense.moses.client.appname");
 			description = savedInstanceState
 					.getString("de.da_sense.moses.client.description");
 			sensors = savedInstanceState
 					.getIntegerArrayList("de.da_sense.moses.client.sensors");
 			apkid = savedInstanceState
 					.getString("de.da_sense.moses.client.apkid");
 			apkVersion = savedInstanceState
 					.getString("de.da_sense.moses.client.apkVersion");
 			startDate = savedInstanceState
 					.getString("de.da_sense.moses.client.startDate");
 			endDate = savedInstanceState
 					.getString("de.da_sense.moses.client.endDate");
 
 			Log.d(TAG, "\nretrieved index = " + index
 					+ "\nretrieved belongsTo = " + belongsTo
 					+ "\nretrieved appname = " + appname
 					+ "\nretireved description = " + description
 					+ "\nretireved sensors = " + sensors
 					+ "\nretireved apkid = " + apkid
 					+ "\nretireved startDate = " + startDate
 					+ "\nretireved endDate = " + endDate
 					+ "\nretireved apkVersion = " + apkVersion);
 
 			if (appname != null) {
 				// inflate the detail view
 				Log.d(TAG, "onCreateView about to inflate View");
 				detailFragmentView = inflater.inflate(R.layout.app_info,
 						container, false);
 				// insert app name
 				TextView t = (TextView) detailFragmentView
 						.findViewById(R.id.usname);
 				t.setText(appname);
 				// insert description
 				t = (TextView) detailFragmentView
 						.findViewById(R.id.description);
 				t.setMovementMethod(ScrollingMovementMethod.getInstance());
 				t.setText(description);
 				t = (TextView) detailFragmentView
 						.findViewById(R.id.tv_us_startdate);
 				t.setText(startDate);
 				t = (TextView) detailFragmentView
 						.findViewById(R.id.tv_us_enddate);
 				t.setText(endDate);
 				t = (TextView) detailFragmentView
 						.findViewById(R.id.tv_us_apkversion);
 				t.setText(apkVersion);
 
 				if (belongsTo == AVAILABLE) {
 					// get start button
 					Button button = (Button) detailFragmentView
 							.findViewById(R.id.startapp);
 					// change the text of it to install
 					button.setText(getString(R.string.install));
 					// make an action listener for it
 					button.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							Log.i(TAG, "install process for app " + appname
 									+ " should be running");
 							ExternalApplication app = AvailableFragment
 									.getInstance().getExternalApps().get(index);// getShownIndex());
 							Log.d(TAG, "installing app ( " + app.getName()
 									+ " ) with apkid = " + app.getID());
 							AvailableFragment.getInstance().handleInstallApp(
 									app);
 						}
 					});
 					// get update button
 					button = (Button) detailFragmentView
 							.findViewById(R.id.update);
 					button.setVisibility(View.GONE); // there is no update for
 														// this new app
 					// get questionnaire button
 					button = (Button) detailFragmentView
 							.findViewById(R.id.btn_questionnaire);
 					button.setVisibility(View.GONE); // there is no
 														// questionnaire for
 														// this new app
 				} else if (belongsTo == RUNNING) {
 					// get start button
 					Button button = (Button) detailFragmentView
 							.findViewById(R.id.startapp);
 					// TODO: check if this is working
 					Button updateButton = (Button) detailFragmentView
 							.findViewById(R.id.update);
 					updateButton.setVisibility(RunningFragment.getInstance()
 							.getInstalledApps().get(getShownIndex())
 							.getUpdateAvailable() ? View.VISIBLE : View.GONE);
 					// change the text of it to install
 					button.setText(getString(R.string.open));
 					// make an action listener for it
 					button.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							Log.i(TAG, "Open app: " + appname
 									+ " should be running");
 							InstalledExternalApplication app = RunningFragment
 									.getInstance().getInstalledApps()
 									.get(index);// getShownIndex());
 							Log.d(TAG, "open app ( " + app.getName()
 									+ " ) with apkid = " + app.getID());
 							RunningFragment.getInstance().handleStartApp(app);
 						}
 					});
 					// get questionnaire button, if the questionnaire is not yet
 					// sent
 					button = (Button) detailFragmentView
 							.findViewById(R.id.btn_questionnaire);
 					// check if it has Questionnaire and if it's sent
 					if (InstalledExternalApplicationsManager.getInstance() == null)
 						InstalledExternalApplicationsManager.init(MosesService
 								.getInstance());
 					InstalledExternalApplication app = InstalledExternalApplicationsManager.getInstance().getAppForId(apkid);
 					Log.d(TAG, "app = "+app);
 					if (app != null) {
 					boolean hasQuestionnaire = app.couldHaveQuestionnaire();
 					boolean hasQuestionnaireLocal = app.hasQuestionnaire();
 					boolean isQuestionnaireSent = hasQuestionnaireLocal ? app.getMultiQuestionnaire().hasBeenSent(): false;
 					Log.d(TAG, "hasQuest" + hasQuestionnaire + "hasQuestLocal" + hasQuestionnaireLocal + "isQuestSent" + isQuestionnaireSent);
 					// set button according to the booleans
 					if (!hasQuestionnaire) {
 						Log.d(TAG, "no questionnaire available for userstudy "
 								+ appname);
 						button.setText(getString(
 								R.string.details_running_noquestionnaire));
 						button.setClickable(false);
 						button.setEnabled(false);
 					} else if (isQuestionnaireSent) {
 						Log.d(TAG, "questionnaire to userstudy " + appname
 								+ " was already sent");
 						button.setText(getString(
 								R.string.details_running_questionnairesent));
 						button.setClickable(false);
 						button.setEnabled(false);
 					} else {
 						button.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View v) {
 								if (InstalledExternalApplicationsManager.getInstance().getAppForId(apkid).hasQuestionnaire()){
 								Log.i(TAG, "Display questionnaires of "
 										+ appname + " to fill");
 								Intent intent = new Intent();
 								intent.setClass(getActivity(),
 										QuestionnaireActivity.class);
 								intent.putExtra(
 										"de.da_sense.moses.client.apkid", apkid);
 								intent.putExtra(
 										"de.da_sense.moses.client.belongsTo",
 										RUNNING);
 								intent.putExtra(
 										"de.da_sense.moses.client.current", 0);
 								startActivity(intent);
 								} else {
 									Log.d(TAG, "Getting Questionnaire from Server");
 									InstalledExternalApplicationsManager.getInstance().getAppForId(apkid).getQuestionnaireFromServer();
 									Toast.makeText(getActivity().getApplicationContext(),
 											"Getting Questionnaire from Server",
 											Toast.LENGTH_LONG).show();
 								}
 							}
 						});
 					}
 					// get update button
 					boolean updateAvailable = InstalledExternalApplicationsManager
 							.getInstance().getAppForId(apkid).isUpdateAvailable();
 					button = (Button) detailFragmentView.findViewById(R.id.update);
 					if (updateAvailable) {
 						button.setVisibility(View.VISIBLE);
 					} else {
 						button.setVisibility(View.GONE);
 					}
 					}
 				} else if (belongsTo == HISTORY) {
 					// get start button
 					Button button = (Button) detailFragmentView
 							.findViewById(R.id.startapp);
 					button.setVisibility(View.GONE); // hide open / install
 														// button
 					// get update button
 					button = (Button) detailFragmentView
 							.findViewById(R.id.update);
 					button.setVisibility(View.GONE); // there is no update for
 														// this old app
 					// get questionnaire button, if the questionnaire is not yet
 					// sent
 					button = (Button) detailFragmentView
 							.findViewById(R.id.btn_questionnaire);
 					// check if it has Questionnaire and if it's sent
 					if (HistoryExternalApplicationsManager.getInstance() == null)
 						HistoryExternalApplicationsManager.init(MosesService
 								.getInstance());
 					boolean hasQuestionnaire = HistoryExternalApplicationsManager
 							.getInstance().getAppForId(apkid)
 							.hasQuestionnaire();
 					boolean isQuestionnaireSent = hasQuestionnaire ? HistoryExternalApplicationsManager
 							.getInstance().getAppForId(apkid)
 							.getMultiQuestionnaire().hasBeenSent()
 							: true;
 					// set button according to the booleans
 					if (!hasQuestionnaire) {
 						button.setText(getString(
 								R.string.details_running_noquestionnaire));
 						button.setClickable(false);
 						button.setEnabled(false);
 					} else if (isQuestionnaireSent) {
 						button.setText(getString(
 								R.string.details_running_questionnairesent));
 						button.setClickable(false);
 						button.setEnabled(false);
 					} else {
 						button.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View v) {
 								Log.i(TAG, "Display questionnaires of "
 										+ appname + " to fill");
 								Intent intent = new Intent();
 								intent.setClass(getActivity(),
 										QuestionnaireActivity.class);
 								intent.putExtra(
 										"de.da_sense.moses.client.apkid", apkid);
 								intent.putExtra(
 										"de.da_sense.moses.client.belongsTo",
 										HISTORY);
 								startActivity(intent);
 							}
 						});
 					}
 				}
 			} else {
 				Log.e(TAG, "User study's informations are missing");
 				return null;
 			}
 		}
 
 		return detailFragmentView;
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.d(TAG, "onResume index=" + getShownIndex());
 	}
 }
