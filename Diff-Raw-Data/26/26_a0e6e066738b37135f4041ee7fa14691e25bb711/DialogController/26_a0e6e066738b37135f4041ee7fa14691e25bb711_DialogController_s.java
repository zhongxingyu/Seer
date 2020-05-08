 package wb.receiptslibrary;
 
 import java.io.File;
 import java.sql.Date;
 import java.util.Calendar;
 import java.util.EnumSet;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.database.SQLException;
 import android.text.format.DateFormat;
 import android.text.format.Time;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 import wb.android.autocomplete.AutoCompleteAdapter;
 import wb.android.dialog.BetterDialogBuilder;
 import wb.android.dialog.DirectDialogOnClickListener;
 import wb.android.dialog.DirectLongLivedOnClickListener;
 import wb.android.flex.Flex;
 import wb.android.storage.StorageManager;
 import wb.receiptslibrary.EmailAttachmentWriter.EmailOptions;
 
 public class DialogController {
 	
 	private static final boolean D = true;
 	private static final String TAG = "DialogController";
 	
 	private static DialogController INSTANCE = null;
 	
 	private SmartReceiptsActivity _activity;
 	private StorageManager _sdCard;
 	private Flex _flex;
 	private Dialog _dialogHolder;
 	private AutoCompleteAdapter _receiptsAdapter, _tripsAdapter;
     private DateEditTextListener _dateTextListener;
 	private DurationDateEditTextListener _defaultDurationListener;
 	private MyCalendarDialog _calendar;
 	private Date _dateCache;
 	
 	private static final CharSequence[] RESERVED_CHARS = {"|","\\","?","*","<","\"",":",">","+","[","]","/","'","\n","\r","\t","\0","\f"};
 	
 	private DialogController(SmartReceiptsActivity activity, StorageManager sdCard, Flex flex) {
 		_activity = activity;
 		_sdCard = sdCard;
 		_flex = flex;
 	}
 	
 	public static final DialogController getInstance(SmartReceiptsActivity activity, StorageManager sdCard, Flex flex) {
 		if (INSTANCE != null) {
 			if (INSTANCE._activity != activity) { INSTANCE._activity = activity; INSTANCE._sdCard = sdCard; INSTANCE._flex = flex; }
 			return INSTANCE;
 		}
 		INSTANCE = new DialogController(activity, sdCard, flex);
 		return INSTANCE;
 	}
 	
 	public final void onPause()
 	{
 		if (_tripsAdapter != null) {
 			_tripsAdapter.onPause();
 			_tripsAdapter = null;
 		}
 		if (_receiptsAdapter != null) {
 			_receiptsAdapter.onPause();
 			_receiptsAdapter = null;
 		}
 		_dateCache = null;
 	}
 	
 	public final void showTripMenu(final TripRow trip) {
 		if (!_sdCard.isExternal()) {
     		Toast.makeText(_activity, _activity._flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
     		return;
     	}
 		if (_dateTextListener == null) _dateTextListener = new DateEditTextListener(this);
 		if (_defaultDurationListener == null) _defaultDurationListener = new DurationDateEditTextListener(this);
     	final boolean newTrip = (trip == null);
     	
 		final View scrollView = _flex.getView(R.layout.dialog_tripmenu);
 		final AutoCompleteTextView nameBox = (AutoCompleteTextView) _flex.getSubView(scrollView, R.id.DIALOG_TRIPMENU_NAME);
 		final DateEditText startBox = (DateEditText) _flex.getSubView(scrollView, R.id.DIALOG_TRIPMENU_START);
 		final DateEditText endBox = (DateEditText) _flex.getSubView(scrollView, R.id.DIALOG_TRIPMENU_END);
 		
 		startBox.setFocusableInTouchMode(false); startBox.setOnClickListener(_dateTextListener);
 		endBox.setFocusableInTouchMode(false); endBox.setOnClickListener(_dateTextListener);
 		
 		//Fill Out Fields
 		if (newTrip) {
 			if (_tripsAdapter == null) _tripsAdapter = AutoCompleteAdapter.getInstance(_activity, _activity._db, DatabaseHelper.TAG_TRIPS);
 			nameBox.setAdapter(_tripsAdapter);
 			_defaultDurationListener.setEnd(endBox);
 			startBox.setOnClickListener(_defaultDurationListener);
 		}
 		else {
 			if (trip.dir != null) nameBox.setText(trip.dir.getName());
 			if (trip.from != null) { startBox.setText(DateFormat.getDateFormat(_activity).format(trip.from)); startBox.date = trip.from; }
 			if (trip.to != null) { endBox.setText(DateFormat.getDateFormat(_activity).format(trip.to)); endBox.date = trip.to; }
 		}
 		nameBox.setSelection(nameBox.getText().length()); //Put the cursor at the end
 		
 		//Show the DialogController
 		final BetterDialogBuilder builder = new BetterDialogBuilder(_activity);
 		builder.setTitle((newTrip)?_flex.getString(R.string.DIALOG_TRIPMENU_TITLE_NEW):_flex.getString(R.string.DIALOG_TRIPMENU_TITLE_EDIT))
 			 .setCancelable(true)
 			 .setView(scrollView)
 			 .setLongLivedPositiveButton((newTrip)?_flex.getString(R.string.DIALOG_TRIPMENU_POSITIVE_BUTTON_CREATE):_flex.getString(R.string.DIALOG_TRIPMENU_POSITIVE_BUTTON_UPDATE), new DirectLongLivedOnClickListener<SmartReceiptsActivity>(_activity) {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 					 String name = nameBox.getText().toString().trim();
 					 final String startDate = startBox.getText().toString();
 					 final String endDate = endBox.getText().toString();
 					 //Error Checking
 					 if (name.length() == 0 || startDate.length() == 0 || endDate.length() == 0) {
 						 Toast.makeText(activity, activity._flex.getString(R.string.DIALOG_TRIPMENU_TOAST_MISSING_FIELD), Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 if (startBox.date == null || endBox.date == null) {
 						 Toast.makeText(activity, activity._flex.getString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 if (startBox.date.getTime() > endBox.date.getTime()) {
 						 Toast.makeText(activity, activity._flex.getString(R.string.DURATION_ERROR), Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 if (name.startsWith(" ")) {
 						 Toast.makeText(activity, activity._flex.getString(R.string.SPACE_ERROR), Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 for (int i=0; i < RESERVED_CHARS.length; i++) {
 						 if (name.contains(RESERVED_CHARS[i])) {
 							 Toast.makeText(activity, activity._flex.getString(R.string.ILLEGAL_CHAR_ERROR), Toast.LENGTH_SHORT).show();
 							 return;
 						 }
 					 }
 					 if (newTrip) { //Insert
 						 File dir = activity._sdCard.mkdir(name);
 						 if (dir != null) {
 							 try {
 								 activity._currentTrip = activity._db.insertTrip(dir, startBox.date, endBox.date);
 							 }
 							 catch (SQLException e) {
 								 Toast.makeText(activity, "Error: An expense report with that name already exists", Toast.LENGTH_SHORT).show();
 								 return;
 							 }
 							 if (activity._currentTrip != null) {
 								 activity._tripAdapter.notifyDataSetChanged(activity._db.getTrips());
 							 }
 							 else {
 								 Toast.makeText(activity, _activity._flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 								 activity._sdCard.delete(dir);
 								 return;
 							 }
 						 }
 						 else {
 							 Toast.makeText(activity, _activity._flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
 						 }
 						 dialog.cancel();
 					 }
 					 else { //Update
 						 final File dir = activity._sdCard.rename(trip.dir, name);
 						 if (dir == trip.dir) {
 							 Toast.makeText(activity, _activity._flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
 							 return;
 						 }
 						 activity._currentTrip = activity._db.updateTrip(trip, dir, (startBox.date != null) ? startBox.date : trip.from, (endBox.date != null) ? endBox.date : trip.from);
 						 if (activity._currentTrip != null) {
 							 activity._tripAdapter.notifyDataSetChanged(activity._db.getTrips());
 						 }
 						 else {
 							 Toast.makeText(activity, _activity._flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 							 activity._sdCard.rename(dir, trip.dir.getName());
 							 return;
 						 }
 						 dialog.cancel();
 					 }
 				}
 			 })
 			 .setNegativeButton(_flex.getString(R.string.DIALOG_TRIPMENU_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
 				 public void onClick(DialogInterface dialog, int which) {
 					 dialog.cancel();   
 				 }
 			 })
 			 .show();
 	}
 	
 	public final void showReceiptMenu(final TripRow trip, final ReceiptRow receipt, final File img) {
 		final boolean newReceipt = (receipt == null);
 		final View scrollView = _flex.getView(R.layout.dialog_receiptmenu);
 		final AutoCompleteTextView nameBox = (AutoCompleteTextView) _flex.getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_NAME);
 		final EditText priceBox = (EditText) _flex.getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_PRICE);
 		final Spinner currencySpinner = (Spinner) _flex.getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_CURRENCY);
 		final DateEditText dateBox = (DateEditText) _flex.getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_DATE);
 		final EditText commentBox = (EditText) _flex.getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_COMMENT);
 		final Spinner categoriesSpinner =  (Spinner) _flex.getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_CATEGORY);
 		final CheckBox expensable = (CheckBox) _flex.getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_EXPENSABLE);
 		final CheckBox fullpage = (CheckBox) _flex.getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_FULLPAGE);
 		
 		//Extras
 		final LinearLayout extras = (LinearLayout) _flex.getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_EXTRAS);
 		final EditText extra_edittext_box_1 = (EditText) extras.findViewWithTag(_flex.getString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_1));
 		final EditText extra_edittext_box_2 = (EditText) extras.findViewWithTag(_flex.getString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_2));
 		final EditText extra_edittext_box_3 = (EditText) extras.findViewWithTag(_flex.getString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_3));
 		
 		final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(_activity, android.R.layout.simple_spinner_item, _activity._db.getCurrenciesList());
 		currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		currencySpinner.setAdapter(currenices);
 		if (_dateTextListener == null) _dateTextListener = new DateEditTextListener(this);
 		dateBox.setFocusableInTouchMode(false); dateBox.setOnClickListener(_dateTextListener);
 		final ArrayAdapter<CharSequence> categories = new ArrayAdapter<CharSequence>(_activity, android.R.layout.simple_spinner_item, _activity._db.getCategoriesList());
 		categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		categoriesSpinner.setAdapter(categories);
 		
 		if (newReceipt) {
 			if (_receiptsAdapter == null) _receiptsAdapter = AutoCompleteAdapter.getInstance(_activity, _activity._db, DatabaseHelper.TAG_RECEIPTS);
 			nameBox.setAdapter(_receiptsAdapter);
 			Time now = new Time(); now.setToNow();
 			if (_dateCache == null) dateBox.date = new Date(now.toMillis(false));
 			else dateBox.date = _dateCache;
 			dateBox.setText(DateFormat.getDateFormat(_activity).format(dateBox.date));
 			expensable.setChecked(true);
 			if (_activity._matchCommentCats && _activity._matchNameCats) categoriesSpinner.setOnItemSelectedListener(new SpinnerSelectionListener(nameBox, commentBox, categories));
 			else if (_activity._matchCommentCats) categoriesSpinner.setOnItemSelectedListener(new SpinnerSelectionListener(null, commentBox, categories));
 			else if (_activity._matchNameCats) categoriesSpinner.setOnItemSelectedListener(new SpinnerSelectionListener(nameBox, null, categories));
 			if (_activity._predictCategories) { //Predict Breakfast, Lunch, Dinner by the hour
 				if (now.hour >= 4 && now.hour < 11) { //Breakfast hours
 					int idx = categories.getPosition("Breakfast");
 					if (idx > 0)
 						categoriesSpinner.setSelection(idx);
 				}
 				else if (now.hour >= 11 && now.hour < 16) { //Lunch hours
 					int idx = categories.getPosition("Lunch");
 					if (idx > 0)
 						categoriesSpinner.setSelection(idx);
 				}
 				else if (now.hour >= 16 && now.hour < 23) { //Dinner hours
 					int idx = categories.getPosition("Dinner");
 					if (idx > 0)
 						categoriesSpinner.setSelection(idx);
 				}
 			}
 			int idx = currenices.getPosition(_activity._currency);
 			if (idx > 0) currencySpinner.setSelection(idx);
 		}
 		else {
 			if (receipt.name.length() != 0) { nameBox.setText(receipt.name); }
 			if (receipt.price.length() != 0) { priceBox.setText(receipt.price); }
 			if (receipt.date != null) { dateBox.setText(DateFormat.getDateFormat(_activity).format(receipt.date)); dateBox.date = receipt.date; }
 			if (receipt.category.length() != 0) { categoriesSpinner.setSelection(categories.getPosition(receipt.category)); }
 			if (receipt.comment.length() != 0) { commentBox.setText(receipt.comment); }
 			int idx = currenices.getPosition(receipt.currency.getCurrencyCode());
 			if (idx > 0) currencySpinner.setSelection(idx);
 			expensable.setChecked(receipt.expensable);
 			fullpage.setChecked(receipt.fullpage);
 			if (extra_edittext_box_1 != null && receipt.extra_edittext_1 != null) extra_edittext_box_1.setText(receipt.extra_edittext_1);
 			if (extra_edittext_box_2 != null && receipt.extra_edittext_2 != null) extra_edittext_box_2.setText(receipt.extra_edittext_2);
 			if (extra_edittext_box_3 != null && receipt.extra_edittext_3 != null) extra_edittext_box_3.setText(receipt.extra_edittext_3);
 		}
 		nameBox.setSelection(nameBox.getText().length()); //Put the cursor at the end
 		
 		//Show DialogController
 		final BetterDialogBuilder builder = new BetterDialogBuilder(_activity);
 		builder.setTitle((newReceipt)?_flex.getString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW):_flex.getString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT))
 			 .setCancelable(true)
 			 .setView(scrollView)
 			 .setLongLivedPositiveButton((newReceipt)?_flex.getString(R.string.DIALOG_RECEIPTMENU_POSITIVE_BUTTON_CREATE):_flex.getString(R.string.DIALOG_RECEIPTMENU_POSITIVE_BUTTON_UPDATE), new DirectLongLivedOnClickListener<SmartReceiptsActivity>(_activity) {
 				 @Override
 				 public void onClick(DialogInterface dialog, int whichButton) {
 					 final String name = nameBox.getText().toString();
 					 String price = priceBox.getText().toString();
 					 final String category = categoriesSpinner.getSelectedItem().toString();
 					 final String currency = currencySpinner.getSelectedItem().toString();
 					 final String comment = commentBox.getText().toString();
 					 final String extra_edittext_1 = (extra_edittext_box_1 == null) ? null : extra_edittext_box_1.getText().toString();
 					 final String extra_edittext_2 = (extra_edittext_box_1 == null) ? null : extra_edittext_box_2.getText().toString();
 					 final String extra_edittext_3 = (extra_edittext_box_1 == null) ? null : extra_edittext_box_3.getText().toString();
 					 if (name.length() == 0) {
 						 Toast.makeText(activity, activity._flex.getString(R.string.DIALOG_RECEIPTMENU_TOAST_MISSING_NAME), Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 if (dateBox.date == null) {
 						 Toast.makeText(activity, activity._flex.getString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 Calendar receiptCalendar = Calendar.getInstance(); receiptCalendar.setTime(dateBox.date); 
 					 receiptCalendar.set(Calendar.HOUR_OF_DAY, 0); receiptCalendar.set(Calendar.MINUTE, 0); receiptCalendar.set(Calendar.SECOND, 0); receiptCalendar.set(Calendar.MILLISECOND, 0);
 					 Calendar tripCalendar = Calendar.getInstance(); tripCalendar.setTime(trip.from);
 					 tripCalendar.set(Calendar.HOUR_OF_DAY, 0); tripCalendar.set(Calendar.MINUTE, 0); tripCalendar.set(Calendar.SECOND, 0); tripCalendar.set(Calendar.MILLISECOND, 0);
 					 if (receiptCalendar.compareTo(tripCalendar) < 0) {
 						 Toast.makeText(activity, activity._flex.getString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
 					 }
 					 tripCalendar.setTime(trip.to);
 					 tripCalendar.set(Calendar.HOUR_OF_DAY, 0); tripCalendar.set(Calendar.MINUTE, 0); tripCalendar.set(Calendar.SECOND, 0); tripCalendar.set(Calendar.MILLISECOND, 0);
 					 if (receiptCalendar.compareTo(tripCalendar) > 0) {
 						 Toast.makeText(activity, activity._flex.getString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
 					 }
 					 if (newReceipt) {//Insert
 						 final ReceiptRow newReceipt = activity._db.insertReceiptFile(trip, img, activity._currentTrip.dir, name, category, dateBox.date, comment, price, expensable.isChecked(), currency, fullpage.isChecked(), extra_edittext_1, extra_edittext_2, extra_edittext_3);
 						 if (newReceipt != null) {
 							 activity._receiptAdapter.notifyDataSetChanged(activity._db.getReceipts(activity._currentTrip));
 							 activity.updateTitlePrice(trip, receipt, newReceipt);
 						 }
 						 else {
 							 Toast.makeText(activity, activity._flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 							 return;
 						 }
 						 _dialogHolder = null;
 						 dialog.cancel();
 					 }
 					 else { //Update
 						 if (price == null || price.length() == 0)
 							 price = "0";
 						 final ReceiptRow updatedReceipt = activity._db.updateReceipt(receipt, trip, name, category, (dateBox.date == null) ? receipt.date : dateBox.date, comment, price, expensable.isChecked(), currency, fullpage.isChecked(), extra_edittext_1, extra_edittext_2, extra_edittext_3);
 						 if (updatedReceipt != null) {
 							 activity._receiptAdapter.notifyDataSetChanged(activity._db.getReceipts(activity._currentTrip));
 							 activity.updateTitlePrice(trip, receipt, updatedReceipt);
 						 }
 						 else {
 							 Toast.makeText(activity, activity._flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 							 return;
 						 }
 						 _dialogHolder = null;
 						 dialog.cancel();
 					 }
 				 }
 			 })  
 			 .setNegativeButton(_flex.getString(R.string.DIALOG_RECEIPTMENU_NEGATIVE_BUTTON), new DirectDialogOnClickListener<SmartReceiptsActivity>(_activity) {
 				 public void onClick(DialogInterface dialog, int which) {
 					 if (img != null && newReceipt)
 						 activity._sdCard.delete(img); //Clean Up On Cancel
 					 _dialogHolder = null;
 					 dialog.cancel();   
 				 }
 			 });
 		final AlertDialog dialog = builder.show();
 		_dialogHolder = dialog;
 		if (newReceipt) {
 			nameBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 				@Override
 				public void onFocusChange(View v, boolean hasFocus) {
 					if (hasFocus && _activity.getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
 						dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
 				}
 			});
 		}
 		categoriesSpinner.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					InputMethodManager imm = (InputMethodManager)_activity.getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(nameBox.getWindowToken(), 0);
 				}
 				return false;
 			}
 		});
 		currencySpinner.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					InputMethodManager imm = (InputMethodManager)_activity.getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(nameBox.getWindowToken(), 0);
 				}
 				return false;
 			}
 		});
 	}
 	
 	public final void showEmailTripMenu() {
 		if (!_sdCard.isExternal()) {
     		Toast.makeText(_activity, _flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
     		return;
     	}
     	View scrollView = _flex.getView(R.layout.dialog_email);
     	final CheckBox pdfFull = (CheckBox) _flex.getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_PDF_FULL);
     	final CheckBox pdfImages = (CheckBox) _flex.getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_PDF_IMAGES);
     	final CheckBox csv = (CheckBox) _flex.getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_CSV);
     	final CheckBox zipStampedImages = (CheckBox) _flex.getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_ZIP_IMAGES_STAMPED);
     	final BetterDialogBuilder builder = new BetterDialogBuilder(_activity);
     	String msg = _flex.getString(R.string.DIALOG_EMAIL_MESSAGE);
     	if (msg.length() > 0) 
     		builder.setMessage(msg);
 		builder.setTitle(_flex.getString(R.string.DIALOG_EMAIL_TITLE))
 			   .setCancelable(true)
 			   .setView(scrollView)
 			   .setPositiveButton(_flex.getString(R.string.DIALOG_EMAIL_POSITIVE_BUTTON), new DirectDialogOnClickListener<SmartReceiptsActivity>(_activity) {
 				   @Override
 		           public void onClick(DialogInterface dialog, int id) {
 					   if (!pdfFull.isChecked() && !pdfImages.isChecked() && !csv.isChecked() && !zipStampedImages.isChecked()) {
 						   Toast.makeText(activity, _flex.getString(R.string.DIALOG_EMAIL_TOAST_NO_SELECTION), Toast.LENGTH_SHORT).show();
 						   dialog.cancel();
 						   return;
 					   }
 					   if (activity._db.getReceipts(activity._currentTrip).length == 0) {
 						   Toast.makeText(activity, _flex.getString(R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS), Toast.LENGTH_SHORT).show();
 						   dialog.cancel();
 						   return;
 					   }
 		        	   ProgressDialog progress = ProgressDialog.show(_activity, "", "Building Reports...", true, false);
 		        	   EnumSet<EmailOptions> options = EnumSet.noneOf(EmailOptions.class);
 		        	   if (pdfFull.isChecked()) options.add(EmailOptions.PDF_FULL);
 		        	   if (pdfImages.isChecked()) options.add(EmailOptions.PDF_IMAGES_ONLY);
 		        	   if (csv.isChecked()) options.add(EmailOptions.CSV);
 		        	   if (zipStampedImages.isChecked()) options.add(EmailOptions.ZIP_IMAGES_STAMPED);
 		        	   EmailAttachmentWriter attachmentWriter = new EmailAttachmentWriter(_activity, activity._sdCard, activity._db, progress, options);
 		        	   attachmentWriter.execute(activity._currentTrip);
 		           }
 		       })
 		       .setNegativeButton(_flex.getString(R.string.DIALOG_EMAIL_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       })
 		       .show();
 	}
 	
 	public final void setCachedDate(Date date) {
 		if (_activity._isViewingTrip)
 			_dateCache = date;
 	}
 	
     public final void initCalendar(DateEditText edit) {
 		if (_calendar == null)
 			_calendar = new MyCalendarDialog(_activity, this);
 		_calendar.set(edit.date);
 		_calendar.setEditText(edit);
 		_calendar.buildDialog(_activity).show();
 		if (_dialogHolder != null) { _dialogHolder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); }
     }
     
     public final void initDurationCalendar(DateEditText start, DateEditText end) {
 		if (_calendar == null)
 			_calendar = new MyCalendarDialog(_activity, this);
 		_calendar.set(start.date);
 		_calendar.setEditText(start);
 		_calendar.setEnd(end, _activity._defaultTripDuration);
 		_calendar.buildDialog(_activity).show();
 
     }
 	
 	//Private Listener Classes
 	private final class DateEditTextListener implements OnClickListener {
 		private final DialogController _dialog;
 		public DateEditTextListener(DialogController dialog) {_dialog = dialog;}
 		@Override public final void onClick(final View v) { _dialog.initCalendar((DateEditText)v); }
 	}
 	private final class DurationDateEditTextListener implements OnClickListener {
 		private final DialogController _dialog;
 		private DateEditText _end;
 		public DurationDateEditTextListener(DialogController dialog) {_dialog = dialog;}
 		public final void setEnd(DateEditText end) {_end = end;}
 		@Override public final void onClick(final View v) {_dialog.initDurationCalendar((DateEditText)v, _end);}
 	}
 	private final class SpinnerSelectionListener implements OnItemSelectedListener {
 		private final TextView _nameBox, _commentBox;
 		private final ArrayAdapter<CharSequence> _categories;
 		public SpinnerSelectionListener(TextView nameBox, TextView commentBox, ArrayAdapter<CharSequence> categories) {_nameBox = nameBox; _commentBox = commentBox; _categories = categories;}
 		@Override public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
 			if (_nameBox != null) _nameBox.setText(_categories.getItem(position));
 			if (_commentBox != null) _commentBox.setText(_categories.getItem(position)); 
 		}
 		@Override public void onNothingSelected(AdapterView<?> arg0) {}	
 	}
 	
 }
