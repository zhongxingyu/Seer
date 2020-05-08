 package org.epstudios.epcoding;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * A fragment representing a single Procedure detail screen. This fragment is
  * either contained in a {@link ProcedureListActivity} in two-pane mode (on
  * tablets) or a {@link ProcedureDetailActivity} on handsets.
  */
 public class ProcedureDetailFragment extends Fragment implements
 		OnClickListener {
 	/**
 	 * The fragment argument representing the item ID that this fragment
 	 * represents.
 	 */
 	public static final String ARG_ITEM_ID = "item_id";
 
 	/**
 	 * The content this fragment is presenting.
 	 */
 	private int mItem = 0;
 
 	// get context from owning Activity
 	private Context context;
 
 	// Settings
	boolean plusShownInDisplay; // show plus in main display
	boolean allowChangingPrimaryCodes;
 	boolean plusShownInSummary;
 	boolean codeDescriptionInSummary;
 	String codeVerbosity;
 	// shorten description based on screen width?
 
 	private LinearLayout primaryCheckBoxLayout;
 	private LinearLayout secondaryCheckBoxLayout;
 	private TextView secondaryCodeTextView;
 
 	private Button summarizeButton;
 	private Button clearButton;
 	private Button helpButton;
 
 	private final Map<String, CodeCheckBox> primaryCheckBoxMap = new HashMap<String, CodeCheckBox>();
 	private final Map<String, CodeCheckBox> secondaryCheckBoxMap = new HashMap<String, CodeCheckBox>();
 
 	// these correspond with the procedure list order
 	final private int afbAblation = 0;
 	final private int svtAblation = 1;
 	final private int vtAblation = 2;
 	final private int avnAblation = 3;
 	final private int epTesting = 4;
 	final private int newPpm = 5;
 	final private int newIcd = 6;
 	final private int ppmReplacement = 7;
 	final private int icdReplacement = 8;
 	final private int deviceUpgrade = 9;
 	final private int subQIcd = 10;
 	final private int otherProcedure = 11;
 	final private int allProcedures = 12;
 
 	private Procedure procedure;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public ProcedureDetailFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (getArguments().containsKey(ARG_ITEM_ID)) {
 			String itemID = getArguments().getString(ARG_ITEM_ID);
 			try {
 				mItem = Integer.parseInt(itemID);
 			} catch (NumberFormatException e) {
 				mItem = 0; // AFB ablation will be shown
 			}
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		loadSettings();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 		// store check marks here
 		boolean[] primaryCodeState = new boolean[primaryCheckBoxMap.size()];
 		int i = 0;
 		for (Map.Entry<String, CodeCheckBox> entry : primaryCheckBoxMap
 				.entrySet())
 			primaryCodeState[i++] = entry.getValue().isChecked();
 		i = 0;
 		boolean[] secondaryCodeState = new boolean[secondaryCheckBoxMap.size()];
 		for (Map.Entry<String, CodeCheckBox> entry : secondaryCheckBoxMap
 				.entrySet())
 			secondaryCodeState[i++] = entry.getValue().isChecked();
 		savedInstanceState.putBooleanArray("primary_codes", primaryCodeState);
 		savedInstanceState.putBooleanArray("secondary_codes",
 				secondaryCodeState);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.summary_button:
 			summarizeCoding();
 			break;
 		case R.id.clear_button:
 			clearEntries();
 			break;
 		case R.id.help_button:
 			help();
 			break;
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.procedure_codes, container,
 				false);
 		context = getActivity();
 
 		primaryCheckBoxLayout = (LinearLayout) rootView
 				.findViewById(R.id.primary_checkbox_layout);
 		secondaryCheckBoxLayout = (LinearLayout) rootView
 				.findViewById(R.id.secondary_checkbox_layout);
 		secondaryCodeTextView = (TextView) rootView
 				.findViewById(R.id.secondary_code_textView);
 
 		loadSettings();
 
 		switch (mItem) {
 		case allProcedures:
 			procedure = new AllCodes();
 			break;
 		case afbAblation:
 			procedure = new AfbAblation();
 			break;
 		case svtAblation:
 			procedure = new SvtAblation();
 			break;
 		case vtAblation:
 			procedure = new VtAblation();
 			break;
 		case avnAblation:
 			procedure = new AvnAblation();
 			break;
 		case epTesting:
 			procedure = new EpTesting();
 			break;
 		case newPpm:
 			procedure = new NewPpm();
 			break;
 		case newIcd:
 			procedure = new NewIcd();
 			break;
 		case ppmReplacement:
 			procedure = new PpmReplacement();
 			break;
 		case icdReplacement:
 			procedure = new IcdReplacement();
 			break;
 		case deviceUpgrade:
 			procedure = new DeviceUpgrade();
 			break;
 		case subQIcd:
 			procedure = new SubQIcd();
 			break;
 		case otherProcedure:
 			procedure = new OtherProcedure();
 			break;
 		default:
 			procedure = new AllCodes();
 			break;
 		}
 
 		getActivity().setTitle(procedure.title(context));
 
 		Code[] secondaryCodes = procedure.secondaryCodes();
 
 		if (secondaryCodes.length > 0) {
 			createCheckBoxLayoutAndCodeMap(secondaryCodes,
 					secondaryCheckBoxMap, secondaryCheckBoxLayout);
 		} else {
 			secondaryCheckBoxLayout.setVisibility(View.GONE);
 			secondaryCodeTextView.setVisibility(View.GONE);
 		}
 
 		Code[] primaryCodes = procedure.primaryCodes();
 		String[] disabledCodeNumbers = procedure.disabledCodeNumbers();
 
 		for (int i = 0; i < disabledCodeNumbers.length; ++i)
 			secondaryCheckBoxMap.get(disabledCodeNumbers[i]).disable();
 
 		createCheckBoxLayoutAndCodeMap(primaryCodes, primaryCheckBoxMap,
 				primaryCheckBoxLayout);
 
 		if (procedure.disablePrimaryCodes()) {
 			// check and disable primary checkboxes for ablation type items
 			for (Map.Entry<String, CodeCheckBox> entry : primaryCheckBoxMap
 					.entrySet()) {
 				entry.getValue().setEnabled(false);
 				entry.getValue().setChecked(true);
 			}
 		}
 
 		// apply saved configurations here
 		if (null != savedInstanceState) {
 			// restore state
 			boolean[] primaryCodesState = savedInstanceState
 					.getBooleanArray("primary_codes");
 			boolean[] secondaryCodesState = savedInstanceState
 					.getBooleanArray("secondary_codes");
 			int i = 0;
 			for (Map.Entry<String, CodeCheckBox> entry : primaryCheckBoxMap
 					.entrySet())
 				entry.getValue().setChecked(primaryCodesState[i++]);
 			i = 0;
 			for (Map.Entry<String, CodeCheckBox> entry : secondaryCheckBoxMap
 					.entrySet())
 				entry.getValue().setChecked(secondaryCodesState[i++]);
 		} else
 			loadCoding();
 		// set up buttons
 		summarizeButton = (Button) rootView.findViewById(R.id.summary_button);
 		summarizeButton.setOnClickListener(this);
 		clearButton = (Button) rootView.findViewById(R.id.clear_button);
 		clearButton.setOnClickListener(this);
 		helpButton = (Button) rootView.findViewById(R.id.help_button);
 		helpButton.setOnClickListener(this);
 		return rootView;
 	}
 
 	private void createCheckBoxLayoutAndCodeMap(Code[] codes,
 			Map<String, CodeCheckBox> codeCheckBoxMap, LinearLayout layout) {
 		for (int i = 0; i < codes.length; ++i) {
 			CodeCheckBox codeCheckBox = new CodeCheckBox(context);
			codes[i].setPlusShown(plusShownInDisplay);
 			codeCheckBox.setCode(codes[i]);
 			codeCheckBoxMap.put(codes[i].getCodeNumber(), codeCheckBox);
 			layout.addView(codeCheckBox);
 		}
 	}
 
 	private void clearEntries() {
 		for (Map.Entry<String, CodeCheckBox> entry : primaryCheckBoxMap
 				.entrySet())
 			entry.getValue().clearIfEnabled();
 		for (Map.Entry<String, CodeCheckBox> entry : secondaryCheckBoxMap
 				.entrySet())
 			entry.getValue().setChecked(false);
 
 	}
 
 	private void summarizeCoding() {
 		String message = "";
 		// we will extract the raw selected codes and shoot them to the code
 		// analyzer
 		Code[] codes = new Code[Codes.allCodesSize()];
 		int i = 0;
 		for (Map.Entry<String, CodeCheckBox> entry : primaryCheckBoxMap
 				.entrySet()) {
 			if (entry.getValue().isChecked()) {
 				codes[i] = entry.getValue().getCode();
 				codes[i].setPlusShown(plusShownInSummary);
 				codes[i].setDescriptionShown(codeDescriptionInSummary);
				if (codeDescriptionInSummary)
					message += codes[i++].getCodeFirstFormatted() + "\n";
 			}
 		}
 		for (Map.Entry<String, CodeCheckBox> entry : secondaryCheckBoxMap
 				.entrySet()) {
 			if (entry.getValue().isChecked()) {
 				codes[i] = entry.getValue().getCode();
 				codes[i].setPlusShown(plusShownInSummary);
 				codes[i].setDescriptionShown(codeDescriptionInSummary);
				if (codeDescriptionInSummary)
					message += codes[i++].getCodeFirstFormatted() + "\n";
 			}
 		}
 		if (message.isEmpty())
 			message = getString(R.string.no_codes_selected_label);
 		else
 			// analyze codes for problems
 			message += codeAnalysis(codes);
 		displayMessage(getString(R.string.coding_summary_dialog_label), message);
 	}
 
 	private String codeAnalysis(Code[] codes) {
 		// use primary and secondary code maps for analysis
 		// assumes user has selected some codes (checked in calling function)
 		// No code analysis done for All Codes
 		if (mItem == allProcedures)
 			return getString(R.string.no_code_analysis_performed_message);
 		// this is a big chunk of analysis that can be as sophisticated as we
 		// want
 		// Maybe delegate to a class
 		// String message = CodeAnalyzer.analyze(codes);
 		// return message;
 		// if no errors found above will:
 		return getString(R.string.no_code_errors_message);
 	}
 
 	private void help() {
 		String message = procedure.helpText(context);
 		displayMessage(getString(R.string.help_label), message);
 	}
 
 	private void displayMessage(String title, String message) {
 		AlertDialog dialog = new AlertDialog.Builder(context).create();
 		dialog.setMessage(message);
 		dialog.setTitle(title);
 		dialog.show();
 	}
 
 	public void saveCoding() {
 		// don't bother if no secondary codes to save
 		if (secondaryCheckBoxMap.isEmpty()) {
 			Toast toast = Toast.makeText(context,
 					getString(R.string.no_secondary_codes_error_message),
 					Toast.LENGTH_SHORT);
 			toast.show();
 			return;
 		}
 		AlertDialog dialog = new AlertDialog.Builder(context).create();
 		String message = "Save these selections as a default?";
 		dialog.setMessage(message);
 		dialog.setTitle("Save Defaults");
 		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						save();
 					}
 				});
 		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				});
 		dialog.show();
 	}
 
 	private void save() {
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		String mItemString = String.valueOf(mItem);
 		Set<String> checkedCodeNumbers = new TreeSet<String>();
 		for (Map.Entry<String, CodeCheckBox> entry : secondaryCheckBoxMap
 				.entrySet()) {
 			if (entry.getValue().isChecked())
 				checkedCodeNumbers.add(entry.getValue().getCodeNumber());
 		}
 		SharedPreferences.Editor prefsEditor = prefs.edit();
 		prefsEditor.putStringSet(mItemString, checkedCodeNumbers);
 		prefsEditor.commit();
 	}
 
 	public void loadCoding() {
 		load(context);
 	}
 
 	private void load(Context context) {
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		String mItemString = String.valueOf(mItem);
 		Set<String> defaultStringSet = new TreeSet<String>();
 		Set<String> codeNumbersChecked = prefs.getStringSet(mItemString,
 				defaultStringSet);
 		// String numbers = codeNumbersChecked.toString();
 		for (Map.Entry<String, CodeCheckBox> entry : secondaryCheckBoxMap
 				.entrySet()) {
 			if (codeNumbersChecked.contains(entry.getValue().getCodeNumber()))
 				entry.getValue().setChecked(true);
 		}
 
 	}
 
 	public void loadSettings() {
 		SharedPreferences sharedPreferences = PreferenceManager
 				.getDefaultSharedPreferences(context);
		plusShownInDisplay = sharedPreferences.getBoolean(
				"show_plus_code_display", true);
		allowChangingPrimaryCodes = sharedPreferences.getBoolean(
				"allow_changing_primary_codes", false);
 		plusShownInSummary = sharedPreferences.getBoolean(
 				"show_plus_code_summary", true);
 		codeDescriptionInSummary = sharedPreferences.getBoolean(
 				"show_details_code_summary", false);
 		codeVerbosity = sharedPreferences
 				.getString("code_verbosity", "VERBOSE");
 	}
 }
