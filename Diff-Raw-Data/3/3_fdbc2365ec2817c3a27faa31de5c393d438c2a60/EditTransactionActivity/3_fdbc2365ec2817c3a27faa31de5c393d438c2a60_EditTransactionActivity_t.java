 package net.axelschumacher.accountoid;
 
 import java.text.DecimalFormat;
 import java.util.Calendar;
 import java.util.Currency;
 
 import net.axelschumacher.accountoid.Accountoid.Account;
 import net.axelschumacher.accountoid.Accountoid.Categories;
 import net.axelschumacher.accountoid.Accountoid.Currencies;
 import net.axelschumacher.accountoid.Accountoid.States;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 /**
  * Edit a single transaction
  */
 public class EditTransactionActivity extends Activity {
 	/** log tag */
 	private static final String TAG = "EditTransactionActivity";
 
 	/** Model */
 	private Model model;
 
 	/** Insert or edit */
 	private int state;
 
 	private static final int STATE_EDIT = 0;
 	private static final int STATE_INSERT = 1;
 
 	/** Query cursor for account table */
 	private Cursor cursorAccount = null;
 
 	/** Query cursor for category table */
 	private Cursor cursorCategory = null;
 
 	/** Query cursor for currency table */
 	private Cursor cursorCurrency = null;
 
 	/** Amount input */
 	private EditText amountEditText;
 
 	/** Description input */
 	private EditText descriptionEditText;
 
 	/** Date input */
 	private DatePicker dateDatePicker;
 
 	/** Category input */
 	private Spinner categorySpinner;
 
 	/** Category selected index */
 	private int selectedCategory = -1;
 
 	/** Id */
 	private long id = -1;
 
 	/** State input */
 	private Spinner stateSpinner;
 
 	/** Currency input */
 	private Spinner currencySpinner;
 
 	/** Currency selected index */
 	private int selectedCurrency = -1;
 
 	/** Init timestamp */
 	long initTimestamp;
 
 	/** Init amount */
 	float initAmount;
 
 	/** Init description */
 	String initDescription;
 
 	/** Init currency id */
 	long initCurrency;
 
 	/** Init category id */
 	long initCategory;
 
 	/** Init state id */
 	States initState;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Program model
 		model = new Model(this);
 
 		// Knowing where we come from
 		final Intent intent = getIntent();
 
 		// Categories cursor
 		cursorCategory = model.getDataBase().getCategories();
 
 		// Currencies cursor
 		cursorCurrency = model.getDataBase().getCurrencies();
 
 		// fetching action type
 		final String action = intent.getAction();
 		if (Intent.ACTION_EDIT.equals(action)) {
 			state = STATE_EDIT;
 			id = intent.getLongExtra(Accountoid.INTENT_ID_NAME, 0);
 			initFields();
 
 		} else if (Intent.ACTION_INSERT.equals(action)) {
 			state = STATE_INSERT;
 		} else {
 			// Whoops, unknown action! Bail.
 			Log.e(TAG, "Unknown action, exiting");
 			finish();
 			return;
 		}
 
 		// Setting the layout
 		setContentView(R.layout.transaction_editor);
 
 		// Amount input
 		amountEditText = (EditText) findViewById(R.id.amount_field_edit);
 		initAmount();
 
 		// Description input
 		descriptionEditText = (EditText) findViewById(R.id.description_field_edit);
 		initDescription();
 
 		// Date input
 		dateDatePicker = (DatePicker) findViewById(R.id.date_field_edit);
 		initDate();
 
 		// Category input
 		categorySpinner = (Spinner) findViewById(R.id.category_field_edit);
 		initCategory();
 
 		// State input
 		stateSpinner = (Spinner) findViewById(R.id.state_field_edit);
 		initState();
 
 		// Currency input
 		currencySpinner = (Spinner) findViewById(R.id.currency_field_edit);
 		initCurrency();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		model.getDataBase().closeDataBase();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		model.getDataBase().closeDataBase();
 	}
 
 	/**
 	 * Init the amount
 	 */
 	private void initAmount() {
 		// Format the input
 		amountEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (!hasFocus) {
 					try {
 						amountEditText
 								.setText(formatAmountFromCurrencyAndAmount());
 					} catch (NumberFormatException e) {
 					}
 				}
 			}
 		});
 		if (state == STATE_EDIT)
 			amountEditText.setText(Float.toString(initAmount));
 	}
 
 	/**
 	 * Init the description
 	 */
 	private void initDescription() {
 		if (state == STATE_EDIT)
 			descriptionEditText.setText(initDescription);
 	}
 
 	/**
 	 * Init the state
 	 */
 	private void initState() {
 		// Prepare list
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				this, R.array.states, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		stateSpinner.setAdapter(adapter);
 		stateSpinner.setSelection(Accountoid.DEFAULT_STATE.ordinal());
 
 		// init when editing
 		if (state == STATE_EDIT) {
 			stateSpinner.setSelection(initState.ordinal());
 		}
 	}
 
 	/**
 	 * Init the currency
 	 */
 	private void initCurrency() {
 		startManagingCursor(cursorCurrency);
 		SimpleCursorAdapter currencyAdapter = new SimpleCursorAdapter(this,
 				android.R.layout.simple_spinner_dropdown_item, cursorCurrency,
 				new String[] { Currencies.CODE },
 				new int[] { android.R.id.text1 });
 		// Change the code to the currency string
 		currencyAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 			public boolean setViewValue(View view, Cursor cursor, int column) {
 				TextView tv = (TextView) view;
 				String code = cursor.getString(cursor
 						.getColumnIndex(Currencies.CODE));
 				tv.setText(Currency.getInstance(code).getSymbol());
 				return true;
 			}
 		});
 		currencySpinner.setAdapter(currencyAdapter);
 		currencySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int pos, long id) {
 				Cursor c = (Cursor) parent.getItemAtPosition(pos);
 				selectedCurrency = c.getInt(c.getColumnIndex(Currencies._ID));
 			}
 
 			public void onNothingSelected(AdapterView<?> parent) {
				Log.d(TAG, "Nothing selected currencies");
 				selectedCurrency = -1;
 			}
 		});
 
 		// init when editing
 		if (state == STATE_EDIT) {
 			for (int i = 0; i < currencyAdapter.getCount(); i++) {
 				Cursor c = (Cursor) (currencyAdapter.getItem(i));
 				if (c.getLong(c.getColumnIndex(Currencies._ID)) == initCurrency) {
 					currencySpinner.setSelection(i);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Init the category
 	 */
 	private void initCategory() {
 		// Prepare list
 		startManagingCursor(cursorCategory);
 		String[] columns = new String[] { Categories.NAME };
 		int[] to = new int[] { android.R.id.text1 };
 		SimpleCursorAdapter categoryAdapter = new SimpleCursorAdapter(this,
 				android.R.layout.simple_spinner_dropdown_item, cursorCategory,
 				columns, to);
 		categorySpinner.setAdapter(categoryAdapter);
 		// Get selected category ID
 		categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int pos, long id) {
 				Cursor c = (Cursor) parent.getItemAtPosition(pos);
 				selectedCategory = c.getInt(c.getColumnIndex(Categories._ID));
 			}
 
 			public void onNothingSelected(AdapterView<?> parent) {
 				selectedCategory = -1;
 			}
 		});
 
 		// init when editing
 		if (state == STATE_EDIT) {
 			for (int i = 0; i < categoryAdapter.getCount(); i++) {
 				Cursor c = (Cursor) (categoryAdapter.getItem(i));
 				if (c.getLong(c.getColumnIndex(Categories._ID)) == initCategory) {
 					categorySpinner.setSelection(i);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Fill all the insert fields
 	 */
 	private void initFields() {
 		cursorAccount = model.getDataBase().getAccount(id);
 		if (cursorAccount.getCount() != 1) {
 			Log.e(TAG, "Given ID not acceptable");
 			finish();
 			return;
 		}
 
 		cursorAccount.moveToFirst();
 		initTimestamp = cursorAccount.getLong(cursorAccount
 				.getColumnIndex(Account.DATE));
 		initAmount = cursorAccount.getFloat(cursorAccount
 				.getColumnIndex(Account.AMOUNT));
 		initDescription = cursorAccount.getString(cursorAccount
 				.getColumnIndex(Account.DESCRIPTION));
 		initCategory = cursorAccount.getLong(cursorAccount
 				.getColumnIndex(Account.CATEGORY));
 		initCurrency = cursorAccount.getLong(cursorAccount
 				.getColumnIndex(Account.CURRENCY));
 		initState = States.values()[cursorAccount.getInt(cursorAccount
 				.getColumnIndex(Account.STATE))];
 		Log.d(TAG, "Existing values are: id: " + id + " amount: " + initAmount
 				+ " desc: " + initDescription + " cat: " + initCategory
 				+ " cur: " + initCurrency + " state: " + initState);
 	}
 
 	/**
 	 * Set a value to the date picker
 	 */
 	private void initDate() {
 		int year, monthOfYear, dayOfMonth;
 		// If we create a new transaction, now will be used
 		if (state == STATE_EDIT) {
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTimeInMillis(initTimestamp * 1000L);
 			year = calendar.get(Calendar.YEAR);
 			monthOfYear = calendar.get(Calendar.MONTH);
 			dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
 			dateDatePicker.updateDate(year, monthOfYear, dayOfMonth);
 		} else if (state == STATE_INSERT) {
 			Calendar c = Calendar.getInstance();
 			year = c.get(Calendar.YEAR);
 			monthOfYear = c.get(Calendar.MONTH);
 			dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
 		}
 	}
 
 	/**
 	 * Add a category
 	 * 
 	 * @param v
 	 */
 	public void addCategory(View v) {
 		startActivity(new Intent(this, EditCategoriesActivity.class));
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (state == STATE_EDIT) {
 			Cursor c = model.getDataBase().getAccount(id);
 			if (c.getCount() != 1) {
 				Log.i(TAG, "Transaction being edited has been deleted!");
 				finish();
 			}
 		}
 		// Update views
 		((SimpleCursorAdapter) currencySpinner.getAdapter()).changeCursor(model
 				.getDataBase().getCurrencies());
 		((SimpleCursorAdapter) categorySpinner.getAdapter()).changeCursor(model
 				.getDataBase().getCategories());
 	}
 
 	/**
 	 * Add a currency
 	 * 
 	 * @param v
 	 */
 	public void addCurrency(View v) {
 		startActivity(new Intent(this, EditCurrenciesActivity.class));
 	}
 
 	/**
 	 * Format a string of float according to selected currency and
 	 * amountTextEdit value
 	 * 
 	 * @throws NumberFormatException
 	 */
 	public String formatAmountFromCurrencyAndAmount() {
 		// Start with parsing to avoid unnecessary computations in case of
 		// throwing
 		Log.d(TAG, "formatAmountFromCurrencyAndAmount");
 		float amount = Float.parseFloat(amountEditText.getEditableText()
 				.toString());
 		Currency currency = null;
 		if (selectedCurrency != -1) {
 			currency = model.getDataBase().getCurrencyFromIndex(
 					selectedCurrency);
 		}
 		DecimalFormat df = model.getDecimalFormat(currency);
 		return df.format(amount);
 	}
 
 	/**
 	 * Save transaction
 	 */
 	public void saveTransaction(View v) {
 		Log.v(TAG, "Save transaction");
 		ContentValues value = new ContentValues();
 
 		// Parse amount and potentially handle a NumberFormatException
 		float amount;
 		try {
 			amount = Float.parseFloat(formatAmountFromCurrencyAndAmount());
 			if (amount == 0)
 				throw new NumberFormatException("Amount cannot be null");
 		} catch (NumberFormatException e) {
 			showDialog(DIALOG_COMPLAIN_AMOUNT);
 			return;
 		}
 		value.put(Account.AMOUNT, amount);
 
 		// Parse description and ask for it not to be empty
 		String description = descriptionEditText.getEditableText().toString();
 		// Remove trailing spaces
 		description = description.replaceAll("\\s+$", "");
 		if (description.isEmpty()) {
 			showDialog(DIALOG_COMPLAIN_DESCRIPTION);
 			return;
 		}
 		value.put(Account.DESCRIPTION, description);
 
 		// If no currency selected, user will create at least one
 		Object currencyObject = currencySpinner.getSelectedItem();
 		if (currencyObject == null) {
 			showDialog(DIALOG_COMPLAIN_CURRENCY);
 			return;
 		}
 		value.put(Account.CURRENCY, selectedCurrency);
 
 		// If no category selected, user will create at least one
 		Object categoryObject = categorySpinner.getSelectedItem();
 		if (categoryObject == null) {
 			showDialog(DIALOG_COMPLAIN_CATEGORY);
 			return;
 		}
 		value.put(Account.CATEGORY, selectedCategory);
 
 		// Handle state choice
 		long state = stateSpinner.getSelectedItemId();
 		value.put(Account.STATE, state);
 
 		// Handle date choice
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.YEAR, dateDatePicker.getYear());
 		c.set(Calendar.MONTH, dateDatePicker.getMonth());
 		c.set(Calendar.DAY_OF_MONTH, dateDatePicker.getDayOfMonth());
 		c.set(Calendar.HOUR, 12);
 		long date = c.getTimeInMillis() / 1000L;
 		value.put(Account.DATE, date);
 
 		if (this.state == STATE_INSERT)
 			model.getDataBase().insertAccount(value);
 		else {
 			model.getDataBase().updateAccount(id, value);
 		}
 		returnBrowsing();
 	}
 
 	public static final int DIALOG_COMPLAIN_AMOUNT = 0;
 	public static final int DIALOG_COMPLAIN_DESCRIPTION = 1;
 	public static final int DIALOG_COMPLAIN_CURRENCY = 2;
 	public static final int DIALOG_COMPLAIN_CATEGORY = 3;
 
 	protected void returnBrowsing() {
 		startActivity(new Intent(Intent.ACTION_DEFAULT, null, this,
 				BrowseActivity.class));
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		AlertDialog.Builder alert = new Builder(this);
 		switch (id) {
 		case DIALOG_COMPLAIN_AMOUNT:
 			alert.setTitle(R.string.dialog_error);
 			alert.setMessage(R.string.dialog_complain_amount_message);
 			alert.setPositiveButton(R.string.ok, null);
 			alert.setCancelable(true);
 			amountEditText.requestFocus();
 			findViewById(R.id.transaction_scroll).scrollTo(
 					amountEditText.getScrollX(), amountEditText.getScrollY());
 			break;
 		case DIALOG_COMPLAIN_DESCRIPTION:
 			alert.setTitle(R.string.dialog_error);
 			alert.setMessage(R.string.dialog_complain_description_message);
 			alert.setPositiveButton(R.string.ok, null);
 			alert.setCancelable(false);
 			descriptionEditText.requestFocus();
 			findViewById(R.id.transaction_scroll).scrollTo(
 					descriptionEditText.getScrollX(),
 					descriptionEditText.getScrollY());
 			break;
 		case DIALOG_COMPLAIN_CURRENCY:
 			alert.setTitle(R.string.dialog_create_currency);
 			alert.setMessage(R.string.dialog_complain_currency_message);
 			alert.setPositiveButton(R.string.ok, new OnClickListener() {
 
 				public void onClick(DialogInterface dialog, int which) {
 					addCurrency(null);
 				}
 			});
 			alert.setCancelable(false);
 			break;
 		case DIALOG_COMPLAIN_CATEGORY:
 			alert.setTitle(R.string.dialog_create_category);
 			alert.setMessage(R.string.dialog_complain_category_message);
 			alert.setPositiveButton(R.string.ok, new OnClickListener() {
 
 				public void onClick(DialogInterface dialog, int which) {
 					addCategory(null);
 				}
 			});
 			alert.setCancelable(true);
 			break;
 
 		default:
 			return null;
 		}
 		return alert.create();
 	}
 
 	/**
 	 * Cancel transaction
 	 */
 	public void cancelTransaction(View v) {
 		Log.v(TAG, "Cancel transaction");
 		// We simply return to the browsing activity
 		returnBrowsing();
 	}
 }
