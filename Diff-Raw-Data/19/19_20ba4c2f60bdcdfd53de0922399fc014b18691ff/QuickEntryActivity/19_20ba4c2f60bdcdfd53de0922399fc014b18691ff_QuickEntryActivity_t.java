 /**
  * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
  * 
  * #TODO License
  */
 package rednus.gncandroid;
 
 import java.util.Calendar;
 import java.util.TreeMap;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 /**
  * This class displays Quick entry screen.
  * 
  * @author John Gray
  * 
  */
 public class QuickEntryActivity extends Activity {
 	// TAG for this activity
 	private static final String TAG = "QuickEntryActivity";
 	// Log information boolean
 	private GNCAndroid app;
 
 	static final int DATE_DIALOG_ID = 0;
 
 	private int currentView = 0;
 
 	private int mYear;
 	private int mMonth;
 	private int mDay;
 
 	private AutoCompleteTextView mDescription;
 	private Spinner mTo;
 	private Spinner mFrom;
 	private EditText mAmount;
 	private Button dateButton;
 	private Spinner transtypeSpinner;
 	private String[] descs;
 
 	private String[] toAccountNames;
 	private String[] toAccountGUIDs;
 	private String[] fromAccountNames;
 	private String[] fromAccountGUIDs;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// get application
 		app = (GNCAndroid) getApplication();
 		// add log entry
 		if (app.localLOGV)
 			Log.i(TAG, "Activity created");
 		setContentView(R.layout.quickentry);
 
 		Button saveButton = (Button) findViewById(R.id.ButtonSave);
 		Button clearButton = (Button) findViewById(R.id.ButtonClear);
 
 		transtypeSpinner = (Spinner) findViewById(R.id.transtype_spinner);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				QuickEntryActivity.this, R.array.transtype_array,
 				android.R.layout.simple_spinner_item);
 		adapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		transtypeSpinner.setAdapter(adapter);
 
 		transtypeSpinner
 				.setOnItemSelectedListener(new TransTypeOnItemSelectedListener());
 
 		constructAccountLists();
 
 		setupTransferControls();
 
 		saveButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				int toPos = mTo.getSelectedItemPosition();
 				int fromPos = mFrom.getSelectedItemPosition();
 
 				String toGUID = toAccountGUIDs[toPos];
 				String fromGUID = fromAccountGUIDs[fromPos];
 
 				String date = dateButton.getText().toString();
 				String amount = mAmount.getText().toString();
 
 				app.gncDataHandler.insertTransaction(toGUID, fromGUID,
 						mDescription.getText().toString(), amount, date);
 			}
 		});
 
 		clearButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (currentView == 0) {
 					final Calendar c = Calendar.getInstance();
 					dateButton.setText(DateFormat.format("MM/dd/yyyy", c));
 
 					mDescription.setText("");
 					mAmount.setText("");
 				}
 			}
 		});
 
 		// add log entry
 		if (app.localLOGV)
 			Log.i(TAG, "Activity Finished");
 	}
 
 	private void constructAccountLists() {
 		TreeMap<String, String> toAccounts = app.gncDataHandler
 				.GetAccountList(true);
 		toAccountNames = new String[toAccounts.size()];
 		toAccountGUIDs = new String[toAccounts.size()];
 		toAccounts.keySet().toArray(toAccountNames);
 		for (int i = 0; i < toAccounts.size(); i++)
 			toAccountGUIDs[i] = toAccounts.get(toAccountNames[i]);
 
 		TreeMap<String, String> fromAccounts = app.gncDataHandler
 				.GetAccountList(false);
 		fromAccountNames = new String[fromAccounts.size()];
 		fromAccountGUIDs = new String[fromAccounts.size()];
 		fromAccounts.keySet().toArray(fromAccountNames);
 		for (int i = 0; i < fromAccounts.size(); i++)
 			fromAccountGUIDs[i] = fromAccounts.get(fromAccountNames[i]);
 	}
 
 	private void setupTransferControls() {
 		mDescription = (AutoCompleteTextView) findViewById(R.id.EditTextDescriptoin);
 		mTo = (Spinner) findViewById(R.id.spinner_to);
 		mFrom = (Spinner) findViewById(R.id.spinner_from);
 		mAmount = (EditText) findViewById(R.id.amount);
 		dateButton = (Button) findViewById(R.id.ButtonDate);
 
 		ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, toAccountNames);
 		toAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mTo.setAdapter(toAdapter);
 
 		ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, fromAccountNames);
 		fromAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mFrom.setAdapter(fromAdapter);
 
 		descs = app.gncDataHandler.GetTransactionDescriptions();
 		ArrayAdapter<String> descAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, descs);
 		mDescription.setAdapter(descAdapter);
 		mDescription
 				.setOnItemClickListener(new DescriptionOnItemClickListener());
 
 		// get the current date
 		final Calendar c = Calendar.getInstance();
 		dateButton.setText(DateFormat.format("MM/dd/yyyy", c));
 
 		mYear = c.get(Calendar.YEAR);
 		mMonth = c.get(Calendar.MONTH);
 		mDay = c.get(Calendar.DAY_OF_MONTH);
 
 		dateButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
 					mDay);
 		}
 		return null;
 	}
 
 	// the call back received when the user "sets" the date in the dialog
 	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 
 		public void onDateSet(DatePicker view, int year, int monthOfYear,
 				int dayOfMonth) {
 			mYear = year;
 			mMonth = monthOfYear;
 			mDay = dayOfMonth;
 
 			Button dateButton = (Button) findViewById(R.id.ButtonDate);
 
 			dateButton.setText(new StringBuilder()
 					// Month is 0 based so add 1
 					.append(mMonth + 1).append("/").append(mDay).append("/")
 					.append(mYear));
 		}
 	};
 
 	public class TransTypeOnItemSelectedListener implements
 			OnItemSelectedListener {
 
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 
 			if (currentView != pos) {
 				currentView = pos;
 
 				TableLayout field_table = (TableLayout) findViewById(R.id.field_table);
 				field_table.removeAllViews();
 
 				// Create new LayoutInflater - this has to be done this way, as
 				// you can't directly inflate an XML without creating an
 				// inflater object first
 				LayoutInflater inflater = getLayoutInflater();
 
 				switch (pos) {
 				case 0:
 					field_table.addView(inflater.inflate(R.layout.transfer,
 							null));
 					setupTransferControls();
 					break;
 				case 1:
 					field_table.addView(inflater
 							.inflate(R.layout.invoice, null));
 					break;
 				case 2:
 					field_table.addView(inflater.inflate(
 							R.layout.expensevoucher, null));
 					break;
 				}
 
 				transtypeSpinner = (Spinner) findViewById(R.id.transtype_spinner);
 				ArrayAdapter<CharSequence> adapter = ArrayAdapter
 						.createFromResource(QuickEntryActivity.this,
 								R.array.transtype_array,
 								android.R.layout.simple_spinner_item);
 				adapter
 						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 				transtypeSpinner.setAdapter(adapter);
 				transtypeSpinner.setSelection(pos);
 
 				transtypeSpinner
 						.setOnItemSelectedListener(new TransTypeOnItemSelectedListener());
 			}
 
 		}
 
 		public void onNothingSelected(AdapterView<?> parent) {
 			// Do nothing.
 		}
 	}
 
 	public class DescriptionOnItemClickListener implements OnItemClickListener {
 		public void onItemClick(AdapterView<?> parent, View view, int pos,
 				long id) {
 			String[] accountGUIDs = app.gncDataHandler
 					.GetAccountsFromTransactionDescription(mDescription
 							.getText().toString());
 			for (int i = 0; i < accountGUIDs.length; i++) {
 				for (int j = 0; j < toAccountGUIDs.length; j++)
 					if (toAccountGUIDs[j].equals(accountGUIDs[i])) {
 						mTo.setSelection(j);
 						break;
 					}
 				for (int k = 0; k < fromAccountGUIDs.length; k++)
 					if (fromAccountGUIDs[k].equals(accountGUIDs[i])) {
 						mFrom.setSelection(k);
 						break;
 					}
 			}
 		}
 	}
 }
