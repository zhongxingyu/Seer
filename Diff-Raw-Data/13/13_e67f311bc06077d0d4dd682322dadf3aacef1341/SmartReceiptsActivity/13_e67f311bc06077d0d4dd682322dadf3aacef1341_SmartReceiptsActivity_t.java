 package wb.receiptslibrary;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.sql.Date;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Currency;
 import java.util.Locale;
 
 import wb.android.storage.SDCardFileManager;
 import wb.android.storage.InternalStorageManager;
 import wb.android.storage.SDCardStateException;
 import wb.android.storage.StorageManager;
 import wb.android.dialog.BetterDialogBuilder;
 import wb.android.dialog.DirectDialogOnClickListener;
 import wb.android.dialog.DirectLongLivedOnClickListener;
 import wb.android.util.AppRating;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.SQLException;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.InputType;
 import android.text.format.DateFormat;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ScrollView;
 
 public abstract class SmartReceiptsActivity extends Activity {
     
 	//logging variables
     private static final boolean D = false;
     private static final String TAG = "SmartReceiptsActivity";
     
     //Toast Error Messages
     static final String SD_ERROR = "Error: Please make sure that your SD Card is available and not mounted to your computer.";
     static final String SD_WARNING = "Warning: Your SD Card is not available or is mounted to your computer. Some images may be inaccessible. Switching to internal storage...";
     static final String DB_ERROR = "Error: Another application is using the SQLite Database.";
     static final String IMG_ERROR = "Error: The image is currently inaccessible or corrupted. Your SD Card may be unavailable or mounted to your computer. If not, click the menu button to retake the photo.";
     static final String ILLEGAL_CHAR_ERROR = "Error: The name contains an illegal character";
     static final String SPACE_ERROR = "Error: The name cannot begin with a space";
     static final String CALENDAR_TAB_ERROR = "Error: Please Touch the Date TextBox to set the Date";
     static final String DURATION_ERROR = "Error: The Start Date Must Occur Prior To The End Date";
     
     //Menus Items
     private static final CharSequence[] EDIT_TRIP_ITEMS = {"Email Report", "Edit Report", "Delete Report"};
     private static final CharSequence[] IMG_EDIT_RECEIPT_ITEMS = {"Edit Receipt", "View Receipt Image", "Delete Receipt"};
     private static final CharSequence[] NOIMG_EDIT_RECEIPT_ITEMS = {"Edit Receipt", "Take Receipt Image", "Delete Receipt"};
     
     //About
     private static final String ABOUT = "Smart Receipts v1.1.0\nCreated and maintained by Will Baumann\nLicensed under GNU Affero General Public License";
     
 	//Public 
 	public static final Locale LOCALE = Locale.getDefault();
     
     //Activity Request ints
     private static final int NEW_RECEIPT_CAMERA_REQUEST = 1;
     private static final int ADD_PHOTO_CAMERA_REQUEST = 2;
     private static final int RETAKE_PHOTO_CAMERA_REQUEST = 3;
     
     //Camera Request Extras
     static final String STRING_DATA = "strData";
     static final int DIR = 0;
     static final int NAME = 1;
     
     //Menu Settings
     private static final int ABOUT_ID = 0;
     private static final int SETTINGS_ID = 1;
     private static final int CATEGORIES_ID = 2;
     private static final int RETAKE_PHOTO_ID = 3;
     
     //Preferences
     private static final String SMART_PREFS = "SmartReceiptsPrefFile";
     private static final String INT_DEFAULT_TRIP_DURATION = "TripDuration";
     private static final String STRING_DEFAULT_EMAIL_TO = "EmailTo";
     private static final String BOOL_PREDICT_CATEGORIES = "PredictCats";
     private static final String BOOL_MATCH_COMMENT_WITH_CATEGORIES = "MatchCommentCats";
     private static final String STRING_CURRENCY = "isocurr";
     
     //Receiver Settings
     protected static final String FILTER_ACTION = "wb.receiptslibrary";
     
     //AppRating
     private static final int LAUNCHES_UNTIL_PROMPT = 35;
     private static final String TITLE = "Smart Receipts";
     
     private static final CharSequence[] RESERVED_CHARS = {"|","\\","?","*","<","\"",":",">","+","[","]","/","'","\n","\r","\t","\0","\f"};
     
     //instance variables (not final to improve access performance by removing virtual get methods used within dialog interfaces)
     RelativeLayout _mainLayout;
     StorageManager _sdCard;
     TripAdapter _tripAdapter;
     ReceiptAdapter _receiptAdapter;
     DatabaseHelper _db;
     TripRow _currentTrip;
     ReceiptRow _highlightedReceipt;
     ListView _listView;
     ImageView _imgView;
     MyCalendarDialog _calendar;
     boolean _isViewingTrip, _firstTrip, _isViewingImg, _predictCategories, _matchCommentCats; 
     int _defaultTripDuration;
     String _emailTo, _currency;
     
 	/* OCR Stuff:
 	 * 	 1. Check that the DC card is mounted and an OCR isn't currently in progress (boolean checks)
 	 *	 2. Mark the OCR as having started (boolean)
 	 *	 3. Apply fancy formatting/status bars if desired
 	 */
     
     protected final void onCreate(final Bundle savedInstanceState, final RelativeLayout mainLayout, final ListView listView) {
     	if(D) Log.d(TAG, "onCreate");
         super.onCreate(savedInstanceState);
         _isViewingTrip = false; _firstTrip = true; _isViewingImg = false;
 	    _currentTrip = null;
     	SharedPreferences prefs = getSharedPreferences(SMART_PREFS, 0);
     	_defaultTripDuration = prefs.getInt(INT_DEFAULT_TRIP_DURATION, 3);
     	_emailTo = prefs.getString(STRING_DEFAULT_EMAIL_TO, "");
     	_predictCategories = prefs.getBoolean(BOOL_PREDICT_CATEGORIES, true);
     	_matchCommentCats = prefs.getBoolean(BOOL_MATCH_COMMENT_WITH_CATEGORIES, false);
     	try {
     		_currency = prefs.getString(STRING_CURRENCY, Currency.getInstance(LOCALE).getCurrencyCode());
     	} catch (IllegalArgumentException ex) {
 			_currency = "USD";
 		}
 	    _db = new DatabaseHelper(this);
 	    _mainLayout = mainLayout;
         _listView = listView;
         _tripAdapter = new TripAdapter(this, _db.getTrips());
         _receiptAdapter = new ReceiptAdapter(this, new ReceiptRow[0]);
         _listView.setAdapter(_tripAdapter);
     }
     
     @Override
     protected final void onStart() {
     	super.onStart();
         if (!_isViewingTrip || _currentTrip == null)
 	        super.setTitle(TITLE);
         else if (_isViewingImg && _highlightedReceipt != null)
         	super.setTitle(_highlightedReceipt.name);
         else
         	this.viewTrip(_currentTrip);
         AppRating.onLaunch(this, LAUNCHES_UNTIL_PROMPT, TITLE, this.getPackageName());
     }
     
     public abstract String getPackageName(); 
     
     @Override
     protected final void onResume() {
     	super.onResume();
     	try {
 			_sdCard = new SDCardFileManager(this);
 		} 
         catch (SDCardStateException e) {
         	Log.e(TAG, "Using internal memory...");
         	_sdCard = new InternalStorageManager(this);
         	Toast.makeText(SmartReceiptsActivity.this, SD_WARNING, Toast.LENGTH_LONG).show();
         }
     }
     
     @Override
     protected final void onDestroy() {
     	super.onDestroy();
     	_db.onDestroy();
     }
     
     public static final String CurrencyValue(final String price, final Currency	currency) {
     	BigDecimal amnt;
     	if (price == null || price.length() == 0)
     		amnt = new BigDecimal(0);
     	else
     		amnt = new BigDecimal(price);
     	try {
     		if (currency != null) {
     			NumberFormat numFormat = NumberFormat.getCurrencyInstance(SmartReceiptsActivity.LOCALE);
     			numFormat.setCurrency(currency);
     			return numFormat.format(amnt.doubleValue());
     		}
     		else
     			return "Mixed";
     	} catch (java.lang.NumberFormatException e) {
     		return "$0.00";
     	}
     }
     
     public final void tripMenu(final TripRow trip) {
     	if (!_sdCard.isExternal()) {
     		Toast.makeText(this, SD_ERROR, Toast.LENGTH_LONG).show();
     		return;
     	}
     	final boolean newTrip = (trip == null);    	
 		final ScrollView scrollView = new ScrollView(this);
 		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		final LinearLayout layout = new LinearLayout(this);
 		layout.setOrientation(LinearLayout.VERTICAL);
 		layout.setGravity(Gravity.BOTTOM);
 		layout.setPadding(6, 6, 6, 6);
 		final EditText nameBox = new EditText(this);  
 		final DateEditText startBox = new DateEditText(this); startBox.setFocusableInTouchMode(false); startBox.setOnClickListener(_dateTextListener);
 		final DateEditText endBox = new DateEditText(this); endBox.setFocusableInTouchMode(false); endBox.setOnClickListener(_dateTextListener);
 		layout.addView(nameBox, params);
 		layout.addView(startBox, params);
 		layout.addView(endBox, params);
 		scrollView.addView(layout);
 		
 		//Fill Out Fields
 		if (newTrip) {
 			nameBox.setHint("Name");
 			startBox.setHint("Start Date");
 			_defaultDurationListener.setEnd(endBox);
 			startBox.setOnClickListener(_defaultDurationListener);
 			endBox.setHint("End Date");
 		}
 		else {
 			//Fill out fields
 			if (trip.dir != null) 
 				nameBox.setText(trip.dir.getName());
 			else 
 				nameBox.setHint("Name"); 
 			if (trip.from != null) {
 				startBox.setText(DateFormat.getDateFormat(this).format(trip.from));
 				startBox.date = trip.from;
 			}
 			else 
 				startBox.setHint("Start Date");
 			if (trip.to != null) { 
 				endBox.setText(DateFormat.getDateFormat(this).format(trip.to));
 				endBox.date = trip.to;
 			}
 			else 
 				endBox.setHint("End Date");
 		}
 		
 		//Show the Dialog
 		final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 		builder.setTitle((newTrip)?"New Expense Report":"Edit Report")
 			 .setCancelable(true)
 			 .setView(scrollView)
 			 .setLongLivedPositiveButton((newTrip)?"Create":"Update", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 					 String name = nameBox.getText().toString().trim();
 					 final String startDate = startBox.getText().toString();
 					 final String endDate = endBox.getText().toString();
 					 //Error Checking
 					 if (name.length() == 0 || startDate.length() == 0 || endDate.length() == 0) {
 						 Toast.makeText(activity, "Please Fill Out All Fields", Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 if (startBox.date == null || endBox.date == null) {
 						 Toast.makeText(activity, CALENDAR_TAB_ERROR, Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 if (startBox.date.getTime() > endBox.date.getTime()) {
 						 Toast.makeText(activity, DURATION_ERROR, Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 if (name.startsWith(" ")) {
 						 Toast.makeText(activity, SPACE_ERROR, Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 for (int i=0; i < RESERVED_CHARS.length; i++) {
 						 if (name.contains(RESERVED_CHARS[i])) {
 							 Toast.makeText(activity, ILLEGAL_CHAR_ERROR, Toast.LENGTH_SHORT).show();
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
 								 Toast.makeText(activity, DB_ERROR, Toast.LENGTH_SHORT).show();
 								 activity._sdCard.delete(dir);
 								 return;
 							 }
 						 }
 						 else {
 							 Toast.makeText(activity, SD_ERROR, Toast.LENGTH_LONG).show();
 						 }
 						 dialog.cancel();
 					 }
 					 else { //Update
 						 final File dir = activity._sdCard.rename(trip.dir, name);
 						 if (dir == trip.dir) {
 							 Toast.makeText(activity, SD_ERROR, Toast.LENGTH_LONG).show();
 							 return;
 						 }
 						 activity._currentTrip = activity._db.updateTrip(trip, dir, (startBox.date != null) ? startBox.date : trip.from, (endBox.date != null) ? endBox.date : trip.from);
 						 if (activity._currentTrip != null) {
 							 activity._tripAdapter.notifyDataSetChanged(activity._db.getTrips());
 						 }
 						 else {
 							 Toast.makeText(activity, DB_ERROR, Toast.LENGTH_SHORT).show();
 							 activity._sdCard.rename(dir, trip.dir.getName());
 							 return;
 						 }
 						 dialog.cancel();
 					 }
 				}
 			 })
 			 .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				 public void onClick(DialogInterface dialog, int which) {
 					 dialog.cancel();   
 				 }
 			 })
 			 .show();
 	}
     
     public void viewTrip(final TripRow trip) {
     	_isViewingTrip = true;
     	_currentTrip = trip;
 		final String currency = SmartReceiptsActivity.CurrencyValue(trip.price, trip.currency);
     	this.setTitle(currency + " - " + trip.dir.getName());
     	_listView.setAdapter(_receiptAdapter);
     	_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
     }
     
     public final void addPictureReceipt() {
 		try {
 			final Intent intent = new Intent(this, MyCameraActivity.class);
 			String dirPath;
 			if (_currentTrip.dir.exists())
 				dirPath = _currentTrip.dir.getCanonicalPath();
 			else
 				dirPath = _sdCard.mkdir(_currentTrip.dir.getName()).getCanonicalPath();
 			String[] strings  = new String[] {dirPath, System.currentTimeMillis() + "x" + _db.getReceipts(_currentTrip).length + ".jpg"};
 			intent.putExtra(STRING_DATA, strings);
 			this.startActivityForResult(intent, NEW_RECEIPT_CAMERA_REQUEST);
 		} catch (IOException e) {
 			Toast.makeText(SmartReceiptsActivity.this, SD_ERROR, Toast.LENGTH_LONG).show();
 		}
     }
     
     public final void addTextReceipt() {
     	this.receiptMenu(_currentTrip ,null, null);
     }
     
     public final void receiptMenu(final TripRow trip, final ReceiptRow receipt, final File img) {
     	final boolean newReceipt = (receipt == null);
 		ScrollView scrollView = new ScrollView(this);
     	final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		final LinearLayout layout = new LinearLayout(this);
 		layout.setOrientation(LinearLayout.VERTICAL);
 		layout.setGravity(Gravity.BOTTOM);
 		layout.setPadding(6, 6, 6, 6);
 		final EditText nameBox = new EditText(this);
 		final LinearLayout pricingLayout = new LinearLayout(this);
 		pricingLayout.setOrientation(LinearLayout.HORIZONTAL);
 		final EditText priceBox = new EditText(this); priceBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
 		final Spinner currencySpinner = new Spinner(this);
 		final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, this._db.getCurrenciesList());
 		currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		currencySpinner.setAdapter(currenices); currencySpinner.setPrompt("Currency");
 		pricingLayout.addView(priceBox, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f)); pricingLayout.addView(currencySpinner, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 2f));
 		final DateEditText dateBox = new DateEditText(this); dateBox.setFocusableInTouchMode(false); dateBox.setOnClickListener(_dateTextListener);
 		final EditText commentBox = new EditText(this);
 		final Spinner categoriesSpinner = new Spinner(this);
 		final ArrayAdapter<CharSequence> categories = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, this._db.getCategoriesList());
 		categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		categoriesSpinner.setAdapter(categories); categoriesSpinner.setPrompt("Category");
 		final CheckBox expensable = new CheckBox(this); expensable.setText(" Expensable?");
 		final CheckBox fullpage = new CheckBox(this); fullpage.setText(" Full-Page Image?");
 		layout.addView(nameBox, params);
 		layout.addView(pricingLayout, params);
 		layout.addView(dateBox, params);
 		layout.addView(categoriesSpinner, params);
 		layout.addView(commentBox, params);
 		layout.addView(expensable, params);
 		layout.addView(fullpage, params);
 		scrollView.addView(layout);
 	
 		// Fill out the fields as appropriate
 		if (newReceipt) {
 			nameBox.setHint("Name");
 			priceBox.setHint("Price (e.g. 150.00)"); 
 			Time now = new Time(); now.setToNow();
 			dateBox.date = new Date(now.toMillis(false)); dateBox.setText(DateFormat.getDateFormat(this).format(dateBox.date));
 			commentBox.setHint("Comment");
 			expensable.setChecked(true);
 			if (_matchCommentCats) categoriesSpinner.setOnItemSelectedListener(new SpinnerSelectionListener(commentBox, categories));
 			if (_predictCategories) { //Predict Breakfast, Lunch, Dinner by the hour
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
 			int idx = currenices.getPosition(_currency);
 			if (idx > 0) currencySpinner.setSelection(idx);
 		}
 		else {
 			if (receipt.name.length() == 0) nameBox.setHint("Name"); else nameBox.setText(receipt.name);
 			if (receipt.price.length() == 0) priceBox.setHint("Price (e.g. 150.00)"); else priceBox.setText(receipt.price);
 			if (receipt.date == null) dateBox.setHint("Date"); else { dateBox.setText(DateFormat.getDateFormat(this).format(receipt.date)); dateBox.date = receipt.date; }
 			if (receipt.category.length() != 0) categoriesSpinner.setSelection(categories.getPosition(receipt.category));
 			if (receipt.comment.length() == 0) commentBox.setHint("Comment"); else commentBox.setText(receipt.comment);
 			int idx = currenices.getPosition(receipt.currency.getCurrencyCode());
 			if (idx > 0) currencySpinner.setSelection(idx);
 			expensable.setChecked(receipt.expensable);
 			fullpage.setChecked(receipt.fullpage);
 		}
 		
 		//Show Dialog
 		final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 		builder.setTitle((newReceipt)?"New Receipt":"Edit Receipt")
 			 .setCancelable(true)
 			 .setView(scrollView)
 			 .setLongLivedPositiveButton((newReceipt)?"Create":"Update", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
 				 @Override
 				 public void onClick(DialogInterface dialog, int whichButton) {
 					 final String name = nameBox.getText().toString();
 					 String price = priceBox.getText().toString();
 					 final String category = categoriesSpinner.getSelectedItem().toString();
 					 final String currency = currencySpinner.getSelectedItem().toString();
 					 final String comment = commentBox.getText().toString();
 					 if (name.length() == 0) {
 						 Toast.makeText(activity, "Please provide a name for this receipt.", Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 if (dateBox.date == null) {
 						 Toast.makeText(activity, CALENDAR_TAB_ERROR, Toast.LENGTH_SHORT).show();
 						 return;
 					 }
 					 if (newReceipt) {//Insert
 						 final ReceiptRow newReceipt = activity._db.insertReceiptFile(trip, img, activity._currentTrip.dir, name, category, dateBox.date, comment, price, expensable.isChecked(), currency, fullpage.isChecked());
 						 if (newReceipt != null) {
 							 activity._receiptAdapter.notifyDataSetChanged(activity._db.getReceipts(activity._currentTrip));
 							 activity.updateTitlePrice(trip, receipt, newReceipt);
 						 }
 						 else {
 							 Toast.makeText(activity, DB_ERROR, Toast.LENGTH_SHORT).show();
 							 return;
 						 }
 						 dialog.cancel();
 					 }
 					 else { //Update
 						 if (price == null || price.length() == 0)
 							 price = "0";
 						 final ReceiptRow updatedReceipt = activity._db.updateReceipt(receipt, trip, name, category, (dateBox.date == null) ? receipt.date : dateBox.date, comment, price, expensable.isChecked(), currency, fullpage.isChecked());
 						 if (updatedReceipt != null) {
 							 activity._receiptAdapter.notifyDataSetChanged(activity._db.getReceipts(activity._currentTrip));
 							 activity.updateTitlePrice(trip, receipt, updatedReceipt);
 						 }
 						 else {
 							 Toast.makeText(activity, DB_ERROR, Toast.LENGTH_SHORT).show();
 							 return;
 						 }
 						 dialog.cancel();
 					 }
 				 }
 			 })  
 			 .setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 				 public void onClick(DialogInterface dialog, int which) {
					 if (img != null && newReceipt)
 						 activity._sdCard.delete(img); //Clean Up On Cancel
 					 dialog.cancel();   
 				 }
 			 })
 			 .show();
     }
     
     final void updateTitlePrice(TripRow trip, ReceiptRow oldReceipt, ReceiptRow newReceipt) {
     	if (newReceipt.price == null || newReceipt.price.length() == 0)
     		return;
     	if (oldReceipt == null) {
     		if (newReceipt.expensable) {
     			try {
 					Float oldPrice = Float.valueOf(trip.price);
 					Float deltaPrice = Float.valueOf(newReceipt.price);
 					float newPrice = oldPrice + deltaPrice;
 					trip.price = Float.toString(newPrice);
 					final String currency = SmartReceiptsActivity.CurrencyValue(trip.price, trip.currency);
 					this.setTitle(currency + " - " + trip.dir.getName());
     			} catch (java.lang.NumberFormatException e) {
     	    		return;
     	    	}
     		}
     	}
     	else {
     		try {
 	    		Float oldPrice = Float.valueOf(trip.price);
 				Float subPrice = Float.valueOf(oldReceipt.price);
 				Float addPrice = Float.valueOf(newReceipt.price);
 				float newPrice = oldPrice;
 				if (oldReceipt.expensable) newPrice -= subPrice;
 				if (newReceipt.expensable) newPrice += addPrice;
 				trip.price = Float.toString(newPrice);
 				final String currency = SmartReceiptsActivity.CurrencyValue(trip.price, trip.currency);
 				this.setTitle(currency + " - " + trip.dir.getName());
 			} catch (java.lang.NumberFormatException e) {
 				return;
 			}
 		}
     }
     
     public final void emailTrip() {
     	if (!_sdCard.isExternal()) {
     		Toast.makeText(this, SD_ERROR, Toast.LENGTH_LONG).show();
     		return;
     	}
     	final ScrollView scrollView = new ScrollView(this);
     	final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		final LinearLayout layout = new LinearLayout(this);
 		layout.setOrientation(LinearLayout.VERTICAL);
 		layout.setGravity(Gravity.BOTTOM);
 		layout.setPadding(6, 6, 6, 6);
 		final CheckBox pdfFull = new CheckBox(this);
 		pdfFull.setText("Full PDF Report");
 		final CheckBox pdfImages = new CheckBox(this);
 		pdfImages.setText("PDF Images Only");
 		final CheckBox csv = new CheckBox(this);
 		csv.setText("CSV File");
 		layout.addView(pdfFull, params);
 		layout.addView(pdfImages, params);
 		layout.addView(csv, params);
 		scrollView.addView(layout, params);
 		final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 		builder.setTitle("Select Attachments...")
 			   .setCancelable(true)
 			   .setView(scrollView)
 			   .setPositiveButton("Email", new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 				   @Override
 		           public void onClick(DialogInterface dialog, int id) {
 					   if (!pdfFull.isChecked() && !pdfImages.isChecked() && !csv.isChecked()) {
 						   Toast.makeText(activity, "Please Check At Least Attachment Option", Toast.LENGTH_SHORT).show();
 						   dialog.cancel();
 						   return;
 					   }
 					   if (activity._db.getReceipts(activity._currentTrip).length == 0) {
 						   Toast.makeText(activity, "There are no receipts in this report.", Toast.LENGTH_SHORT).show();
 						   dialog.cancel();
 						   return;
 					   }
 		        	   ProgressDialog progress = ProgressDialog.show(SmartReceiptsActivity.this, "", "Building Reports...", true, false);
 		        	   EmailAttachmentWriter attachmentWriter = new EmailAttachmentWriter(SmartReceiptsActivity.this, activity._sdCard, activity._db, progress, pdfFull.isChecked(), pdfImages.isChecked(), csv.isChecked());
 		        	   attachmentWriter.execute(activity._currentTrip);
 		           }
 		       })
 		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       })
 		       .show();
     }
     
     public final void postCreateAttachments(File[] files) {
 		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);  
 		emailIntent.setType("application/octet-stream");
 		ArrayList<Uri> uris = new ArrayList<Uri>();
 		if (files[EmailAttachmentWriter.FULL_PDF] != null) uris.add(Uri.fromFile(files[EmailAttachmentWriter.FULL_PDF]));
 		if (files[EmailAttachmentWriter.IMG_PDF] != null) uris.add(Uri.fromFile(files[EmailAttachmentWriter.IMG_PDF]));
 		if (files[EmailAttachmentWriter.CSV] != null) uris.add(Uri.fromFile(files[EmailAttachmentWriter.CSV]));
 		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{_emailTo});
 		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SmartReceipts - " + _currentTrip.dir.getName());
 		if (uris.size() == 1) emailIntent.putExtra(Intent.EXTRA_TEXT, uris.size() + " report attached");
 		if (uris.size() > 1) emailIntent.putExtra(Intent.EXTRA_TEXT, uris.size() + " reports attached");
 		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
     	startActivity(Intent.createChooser(emailIntent, "Send mail..."));
     }
     
     public final boolean editTrip(final TripRow trip) {
 		final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 		builder.setTitle(trip.dir.getName())
 			   .setCancelable(true)
 			   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       })
 		       .setItems(EDIT_TRIP_ITEMS, new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	final String selection = EDIT_TRIP_ITEMS[item].toString();
 				    	if (selection == EDIT_TRIP_ITEMS[0]) { //Email Trip
 				    		activity._currentTrip = trip;
 				    		activity.emailTrip();
 				    	}
 				    	else if (selection == EDIT_TRIP_ITEMS[1]) //Edit Trip
 				    		activity.tripMenu(trip); 
 				    	else if (selection == EDIT_TRIP_ITEMS[2]) //Delte Trip
 				    		activity.deleteTrip(trip);
 				    	dialog.cancel();
 				    }
 				})
 				.show();
     	return true;
     }
     
     public final void deleteTrip(final TripRow trip) {
     	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 		builder.setTitle("Delete " + trip.dir.getName() + "?")
 			   .setCancelable(true)
 			   .setPositiveButton("Delete", new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 		           public void onClick(DialogInterface dialog, int id) {
 		                if (activity._db.deleteTrip(trip)) {
 		                	if (!activity._sdCard.deleteRecursively(trip.dir))
 		                			Toast.makeText(SmartReceiptsActivity.this, SD_ERROR, Toast.LENGTH_LONG).show();
 		                	activity._tripAdapter.notifyDataSetChanged(activity._db.getTrips());
 		                }
 		                else
 		                	Toast.makeText(SmartReceiptsActivity.this, DB_ERROR, Toast.LENGTH_SHORT).show();
 		           }
 		       })
 		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       })
 		       .show();
     }
     
     public final boolean editReceipt(final ReceiptRow receipt) {
     	_highlightedReceipt = receipt;
     	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 		builder.setTitle(receipt.name)
 			   .setCancelable(true)
 			   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       });
 		if (receipt.img == null) {
 			builder.setItems(NOIMG_EDIT_RECEIPT_ITEMS, new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 			    public void onClick(DialogInterface dialog, int item) {
 			    	final String selection = NOIMG_EDIT_RECEIPT_ITEMS[item].toString();
 			    	//TODO: Remove the virtual get calls below
 			    	if (selection == NOIMG_EDIT_RECEIPT_ITEMS[0]) //Edit Receipt
			    		activity.receiptMenu(activity._currentTrip, receipt, null);
 			    	else if (selection == NOIMG_EDIT_RECEIPT_ITEMS[1]) { //Take Photo
 			    		try {
 							final Intent intent = new Intent(activity, MyCameraActivity.class);
 							String dirPath;
 							if (_currentTrip.dir.exists())
 								dirPath = _currentTrip.dir.getCanonicalPath();
 							else
 								dirPath = _sdCard.mkdir(_currentTrip.dir.getName()).getCanonicalPath();
 							String[] strings  = new String[] {dirPath, receipt.id + "x.jpg"};
 							intent.putExtra(STRING_DATA, strings);
 							activity.startActivityForResult(intent, ADD_PHOTO_CAMERA_REQUEST);
 			    		} catch (IOException e) {
 							   Toast.makeText(SmartReceiptsActivity.this, SD_ERROR, Toast.LENGTH_LONG).show();
 						}
 			    	}
 			    	else if (selection == NOIMG_EDIT_RECEIPT_ITEMS[2]) //Delete Receipt
 			    		activity.deleteReceipt(receipt);
 			    	dialog.cancel();
 			    }
 			});
 		}
 		else {
 			builder.setItems(IMG_EDIT_RECEIPT_ITEMS, new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 			    public void onClick(DialogInterface dialog, int item) {
 			    	final String selection = IMG_EDIT_RECEIPT_ITEMS[item].toString();
 			    	//TODO: Remove the virtual get calls below
 			    	if (selection == IMG_EDIT_RECEIPT_ITEMS[0]) //Edit Receipt
 			    		activity.receiptMenu(_currentTrip, receipt, receipt.img);
 			    	else if (selection == IMG_EDIT_RECEIPT_ITEMS[1]) { //View/Retake Image 
 			    		activity.showImage(receipt);
 			    	}
 			    	else if (selection == IMG_EDIT_RECEIPT_ITEMS[2]) //Delete Receipt
 			    		activity.deleteReceipt(receipt);
 			    	dialog.cancel();
 			    }
 			});
 		}
 		builder.create().show();
     	return true;
     }
     
     private final void showImage(final ReceiptRow receipt) {
     	try {
     		_isViewingImg = true;
     		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
     		if (_imgView == null) {
     			_imgView = new ImageView(this);
     			_imgView.setScaleType(ScaleType.FIT_CENTER);
     		}
     		if (!receipt.img.exists())
     			Toast.makeText(this, IMG_ERROR, Toast.LENGTH_SHORT).show();
     		System.gc();
     		_imgView.setImageBitmap(BitmapFactory.decodeFile(receipt.img.getCanonicalPath()));
     		this.setTitle(receipt.name);
     		this.setContentView(_imgView, params);
     	}
     	catch (IOException e) {
     		Toast.makeText(this, SD_ERROR, Toast.LENGTH_LONG).show();
     		_isViewingImg = false;
     		this.setContentView(_mainLayout);
     		this.setTitle(_currentTrip.price + " - " + _currentTrip.dir.getName());
     	}
     }
     
     public final void deleteReceipt(final ReceiptRow receipt) {
     	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 		builder.setTitle("Delete " + receipt.name + "?")
 			   .setCancelable(true)
 			   .setPositiveButton("Delete", new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 		           public void onClick(DialogInterface dialog, int id) {
 		                if (activity._db.deleteReceipt(receipt, activity._currentTrip)) {
 		                	_receiptAdapter.notifyDataSetChanged(activity._db.getReceipts(activity._currentTrip));
 		                	if (receipt.img != null) {
 		                		if (!activity._sdCard.delete(receipt.img))
 		                			Toast.makeText(activity, SD_ERROR, Toast.LENGTH_LONG).show();
 		                	}
 		                	if (receipt.price != null && receipt.price.length() != 0) {
 		                		try {
 				                	BigDecimal amnt = new BigDecimal(activity._currentTrip.price);
 				                	BigDecimal delta = new BigDecimal(receipt.price);
 				                	final float priceFloat = amnt.floatValue() - delta.floatValue();
 				    				_currentTrip.price = Float.toString(priceFloat);
 				    				final String currency = SmartReceiptsActivity.CurrencyValue(_currentTrip.price, _currentTrip.currency);
 				    				activity.setTitle(currency + " - " + activity._currentTrip.dir.getName());
 		                		} catch (java.lang.NumberFormatException e) {}
 		                	}
 		                }
 		                else
 		                	Toast.makeText(activity, DB_ERROR, Toast.LENGTH_SHORT).show();
 		           }
 		       })
 		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       })
 		       .show();
     }
     
     @Override
     protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
     	if (D) Log.d(TAG, "Result Code: " + resultCode);
     	if (resultCode == MyCameraActivity.PICTURE_SUCCESS) {
 	    	switch (requestCode) {
 				case NEW_RECEIPT_CAMERA_REQUEST:
 					if (resultCode == MyCameraActivity.PICTURE_SUCCESS)
 						this.receiptMenu(_currentTrip, null, new File(data.getStringExtra(MyCameraActivity.IMG_FILE)));
 					else
 						Log.e(TAG, "Unexpected Result Code: " + resultCode);
 				break;
 				case ADD_PHOTO_CAMERA_REQUEST:
 					if (resultCode == MyCameraActivity.PICTURE_SUCCESS) {
 						File img = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
 						final ReceiptRow updatedReceipt = _db.updateReceiptImg(_highlightedReceipt, img);
 						if (updatedReceipt != null) {
 							_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
 							Toast.makeText(SmartReceiptsActivity.this, "Receipt Image Successfully Added to " + _highlightedReceipt.name, Toast.LENGTH_SHORT).show();
 						}
 						else {
 							Toast.makeText(SmartReceiptsActivity.this, DB_ERROR, Toast.LENGTH_SHORT).show();
 							return;
 						}
 					}
 					else
 						Log.e(TAG, "Unexpected Result Code: " + resultCode);
 				break;
 				case RETAKE_PHOTO_CAMERA_REQUEST:
 					if (resultCode == MyCameraActivity.PICTURE_SUCCESS) {
 						File img = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
 						final ReceiptRow updatedReceipt = _db.updateReceiptImg(_highlightedReceipt, img);
 						if (updatedReceipt != null) {
 							_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
 							Toast.makeText(SmartReceiptsActivity.this, "Receipt Image Successfully Replaced for " + _highlightedReceipt.name, Toast.LENGTH_SHORT).show();
 			    			_isViewingImg = false;
 			    			this.setContentView(_mainLayout);
 							final String currency = SmartReceiptsActivity.CurrencyValue(_currentTrip.price, _currentTrip.currency);
 							this.setTitle(currency + " - " + _currentTrip.dir.getName());
 						}
 						else {
 							Toast.makeText(SmartReceiptsActivity.this, DB_ERROR, Toast.LENGTH_SHORT).show();
 							return;
 						}
 					}
 					else
 						Log.e(TAG, "Unexpected Result Code: " + resultCode);
 				break;
 			}
     	}
     }
     
     @Override
     public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
     	if (keyCode == KeyEvent.KEYCODE_BACK) {
     		if (!back())
     			return super.onKeyDown(keyCode, event);
 			else
 				return true;
     	}
 		return false;
     }
     
     public boolean back() {
     	if (_isViewingImg) {
 			_isViewingImg = false;
 			this.setContentView(_mainLayout);
 			final String currency = SmartReceiptsActivity.CurrencyValue(_currentTrip.price, _currentTrip.currency);
 			this.setTitle(currency + " - " + _currentTrip.dir.getName());
 			return true;
 		}
 		else if (_isViewingTrip) {
 			_isViewingTrip = false;
 			this.setTitle(TITLE);
 			_listView.setAdapter(_tripAdapter);
 			_tripAdapter.notifyDataSetChanged(_db.getTrips());
 			return true;
 		}
 		else 
 			return false;
     }
     
     public boolean isHome() {
     	return (!_isViewingImg && !_isViewingTrip);
     }
     
     public final void initCalendar(DateEditText edit) {
 		if (_calendar == null)
 			_calendar = new MyCalendarDialog(this);
 		_calendar.set(edit.date);
 		_calendar.setEditText(edit);
 		//showDialog(REQUEST_CALENDAR_DIALOG);
 		_calendar.buildDialog(this).show();
     }
     
     public final void initDurationCalendar(DateEditText start, DateEditText end) {
 		if (_calendar == null)
 			_calendar = new MyCalendarDialog(this);
 		_calendar.set(start.date);
 		_calendar.setEditText(start);
 		_calendar.setEnd(end, _defaultTripDuration);
 		_calendar.buildDialog(this).show();
     }
         
     @Override
     public final boolean onCreateOptionsMenu(Menu menu) {
 		MenuItem settings = menu.add(Menu.NONE, SETTINGS_ID, Menu.NONE, "Settings");
 		settings.setIcon(android.R.drawable.ic_menu_preferences);
 		MenuItem categories = menu.add(Menu.NONE, CATEGORIES_ID, Menu.NONE, "Categories");
 		categories.setIcon(android.R.drawable.ic_menu_agenda);
 		MenuItem about = menu.add(Menu.NONE, ABOUT_ID, Menu.NONE, "About");
 		about.setIcon(android.R.drawable.ic_menu_info_details);
 		MenuItem retake = menu.add(Menu.NONE, RETAKE_PHOTO_ID, Menu.NONE, "Retake Photo");
 		retake.setIcon(android.R.drawable.ic_menu_camera);
 		return true;
     }
     
     @Override
     public final boolean onPrepareOptionsMenu(Menu menu) {
     	if (_isViewingImg) {
     		MenuItem settings = menu.getItem(SETTINGS_ID);
     		if (settings != null) settings.setVisible(false);
     		MenuItem categories = menu.getItem(CATEGORIES_ID);
     		if (categories != null) categories.setVisible(false);
     		MenuItem about = menu.getItem(ABOUT_ID);
     		if (about != null) about.setVisible(false);
     		MenuItem retake = menu.getItem(RETAKE_PHOTO_ID);
     		if (retake != null) retake.setVisible(true);
     	}
     	else {
     		MenuItem settings = menu.getItem(SETTINGS_ID);
     		if (settings != null) settings.setVisible(true);
     		MenuItem categories = menu.getItem(CATEGORIES_ID);
     		if (categories != null) categories.setVisible(true);
     		MenuItem about = menu.getItem(ABOUT_ID);
     		if (about != null) about.setVisible(true);
     		MenuItem retake = menu.getItem(RETAKE_PHOTO_ID);
     		if (retake != null) retake.setVisible(false);
     	}
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
     	if (item.getItemId() == ABOUT_ID) {
 	    	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 	    	final ScrollView scrollView = new ScrollView(this);
 	    	final TextView text = new TextView(this); text.setPadding(6, 0, 6, 0); text.setTextSize(16F);
 	    	text.setText(ABOUT);
 	    	scrollView.addView(text, params);
 			builder.setTitle("About")
 				   .setView(scrollView)
 				   .setCancelable(true)
 				   .show();
 			return true;
     	}
     	else if (item.getItemId() == SETTINGS_ID) {
 	    	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 	    	final ScrollView scrollView = new ScrollView(this);
 	    	final LinearLayout layout = new LinearLayout(this);
 	    	layout.setOrientation(LinearLayout.VERTICAL); layout.setGravity(Gravity.BOTTOM); layout.setPadding(6, 6, 6, 6);
 	    	final TextView emailText = new TextView(this); emailText.setPadding(6, 10, 6, 0); emailText.setTextSize(16F); emailText.setText("Default Email Recipient:");
 	    	final EditText email = new EditText(this); email.setText(_emailTo);
 	    	final TextView daysText = new TextView(this); daysText.setPadding(6, 10, 6, 0); daysText.setTextSize(16F); daysText.setText("Default Trip Length (Days):");
 	    	final EditText days = new EditText(this); days.setInputType(InputType.TYPE_CLASS_NUMBER); days.setText("" + _defaultTripDuration);
 	    	final TextView currencyText = new TextView(this); currencyText.setPadding(6, 10, 6, 0); currencyText.setTextSize(16F); currencyText.setText("Default Currency:");
 			final Spinner currencySpinner = new Spinner(this);
 			final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, this._db.getCurrenciesList());
 			currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			currencySpinner.setAdapter(currenices); currencySpinner.setPrompt("Default Currency");
 			int idx = currenices.getPosition(_currency);
 			if (idx > 0) currencySpinner.setSelection(idx);
 	    	final CheckBox predictCategoires = new CheckBox(this); predictCategoires.setText(" Predict Receipt Categories"); predictCategoires.setChecked(_predictCategories);
 	    	final CheckBox matchCommentsToCategory = new CheckBox(this); matchCommentsToCategory.setText(" Match Comments to Categories"); matchCommentsToCategory.setChecked(_matchCommentCats);
 	    	layout.addView(emailText); layout.addView(email);
 	    	layout.addView(daysText); layout.addView(days);
 	    	layout.addView(currencyText); layout.addView(currencySpinner);
 	    	layout.addView(predictCategoires);
 	    	layout.addView(matchCommentsToCategory);
 	    	scrollView.addView(layout);
 			builder.setTitle("Settings")
 				   .setView(scrollView)
 				   .setCancelable(true)
 				   .setPositiveButton("Save", new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 			           public void onClick(DialogInterface dialog, int id) {
 			        	   try {
 				        	   if (days.getText().toString() != null && days.getText().toString().length() > 0 && days.getText().toString().length() < 4)
 				        		   activity._defaultTripDuration = Integer.parseInt(days.getText().toString());
 				               activity._emailTo = email.getText().toString();
 				               activity._currency = currencySpinner.getSelectedItem().toString();
 				               activity._predictCategories = predictCategoires.isChecked();
 				               activity._matchCommentCats = matchCommentsToCategory.isChecked();
 				               SharedPreferences prefs = getSharedPreferences(SMART_PREFS, 0);
 				               SharedPreferences.Editor editor = prefs.edit();
 				               editor.putInt(INT_DEFAULT_TRIP_DURATION, activity._defaultTripDuration);
 					           editor.putString(STRING_DEFAULT_EMAIL_TO, activity._emailTo);
 					           editor.putString(STRING_CURRENCY, _currency);
 					           editor.putBoolean(BOOL_PREDICT_CATEGORIES, activity._predictCategories);
 					           editor.putBoolean(BOOL_MATCH_COMMENT_WITH_CATEGORIES, activity._matchCommentCats);
 					           editor.commit();
 			        	   } catch (java.lang.NumberFormatException e) {
 			        		   activity._emailTo = email.getText().toString();
 			        		   activity._currency = currencySpinner.getSelectedItem().toString();
 				               activity._predictCategories = predictCategoires.isChecked();
 				               activity._matchCommentCats = matchCommentsToCategory.isChecked();
 				               SharedPreferences prefs = getSharedPreferences(SMART_PREFS, 0);
 				               SharedPreferences.Editor editor = prefs.edit();
 					           editor.putString(STRING_DEFAULT_EMAIL_TO, activity._emailTo);
 					           editor.putString(STRING_CURRENCY, _currency);
 					           editor.putBoolean(BOOL_PREDICT_CATEGORIES, activity._predictCategories);
 					           editor.putBoolean(BOOL_MATCH_COMMENT_WITH_CATEGORIES, activity._matchCommentCats);
 					           editor.commit();
 					       }
 			           }
 			       })
 				   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			           }
 			       })
 				   .show();
     	}
     	else if (item.getItemId() == CATEGORIES_ID) {
     		this.showCategoriesMenu();
     	}
     	else if (item.getItemId() == RETAKE_PHOTO_ID) {
     		try {
 				final Intent intent = new Intent(this, MyCameraActivity.class);
 				String dirPath;
 				if (_currentTrip.dir.exists())
 					dirPath = _currentTrip.dir.getCanonicalPath();
 				else
 					dirPath = _sdCard.mkdir(_currentTrip.dir.getName()).getCanonicalPath();
 				String[] strings  = new String[] {dirPath, _highlightedReceipt.img.getName()};
 				intent.putExtra(STRING_DATA, strings);
 				this.startActivityForResult(intent, RETAKE_PHOTO_CAMERA_REQUEST);
     		} catch (IOException e) {
 				Toast.makeText(SmartReceiptsActivity.this, SD_ERROR, Toast.LENGTH_LONG).show();
 			}
     	}
     	return false;
     }
     
     private final void showCategoriesMenu() {
     	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 		final LinearLayout outerLayout = new LinearLayout(this);
 		outerLayout.setOrientation(LinearLayout.VERTICAL);
 		outerLayout.setGravity(Gravity.BOTTOM);
 		outerLayout.setPadding(6, 6, 6, 6);
 		final Spinner categoriesSpinner = new Spinner(this);
 		final ArrayAdapter<CharSequence> categories = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, this._db.getCategoriesList());
 		categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		categoriesSpinner.setAdapter(categories); categoriesSpinner.setPrompt("Category");
 		outerLayout.addView(categoriesSpinner);
 		builder.setTitle("Select A Category")
 			   .setView(outerLayout)
 			   .setCancelable(true)
 			   .setLongLivedPositiveButton("Add", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
 					@Override
 					public void onClick(DialogInterface dialog, int whichButton) {
 						final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(activity);
 						final LinearLayout layout = new LinearLayout(activity);
 						layout.setOrientation(LinearLayout.VERTICAL);
 						layout.setGravity(Gravity.BOTTOM);
 						layout.setPadding(6, 6, 6, 6);
 						final TextView nameLabel = new TextView(activity); nameLabel.setText("Name:");
 						final EditText nameBox = new EditText(activity);
 						final TextView codeLabel = new TextView(activity); codeLabel.setText("Code:");
 						final EditText codeBox = new EditText(activity);
 						layout.addView(nameLabel);
 						layout.addView(nameBox);
 						layout.addView(codeLabel);
 						layout.addView(codeBox);
 						innerBuilder.setTitle("Add Category")
 									.setView(layout)
 									.setCancelable(true)
 									.setPositiveButton("Add", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 											final String name = nameBox.getText().toString();
 											final String code = codeBox.getText().toString();
 											try {
 												if (this.activity._db.insertCategory(name, code)) {
 													categories.notifyDataSetChanged();
 													categoriesSpinner.setSelection(categories.getPosition(name));
 												}
 												else {
 													Toast.makeText(this.activity, SmartReceiptsActivity.DB_ERROR, Toast.LENGTH_SHORT).show();
 												}
 											}
 											catch (SQLException e) {
 												 Toast.makeText(this.activity, "Error: An category with that name already exists", Toast.LENGTH_SHORT).show();
 											}
 										}
 									})
 									.setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 											dialog.cancel();
 										}
 									})
 									.show();
 					} 
 			   })
 			   .setLongLivedNeutralButton("Edit", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
 					@Override
 					public void onClick(DialogInterface dialog, int whichButton) {
 						final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(activity);
 						final LinearLayout layout = new LinearLayout(activity);
 						layout.setOrientation(LinearLayout.VERTICAL);
 						layout.setGravity(Gravity.BOTTOM);
 						layout.setPadding(6, 6, 6, 6);
 						final String oldName = categoriesSpinner.getSelectedItem().toString();
 						final TextView nameLabel = new TextView(activity); nameLabel.setText("Name:");
 						final EditText nameBox = new EditText(activity); nameBox.setText(oldName);
 						final String oldCode = activity._db.getCategoryCode(oldName);
 						final TextView codeLabel = new TextView(activity); codeLabel.setText("Code:");
 						final EditText codeBox = new EditText(activity); codeBox.setText(oldCode);
 						layout.addView(nameLabel);
 						layout.addView(nameBox);
 						layout.addView(codeLabel);
 						layout.addView(codeBox);
 						innerBuilder.setTitle("Edit Category")
 									.setView(layout)
 									.setCancelable(true)
 									.setPositiveButton("Update", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 											final String newName = nameBox.getText().toString();
 											final String newCode = codeBox.getText().toString();
 											try {
 												if (this.activity._db.updateCategory(oldName, newName, newCode)) {
 													categories.notifyDataSetChanged();
 													categoriesSpinner.setSelection(categories.getPosition(newName));
 												}
 												else {
 													Toast.makeText(this.activity, SmartReceiptsActivity.DB_ERROR, Toast.LENGTH_SHORT).show();
 												}
 											}
 											catch (SQLException e) {
 												 Toast.makeText(this.activity, "Error: An category with that name already exists", Toast.LENGTH_SHORT).show();
 											}
 										}
 									})
 									.setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 											dialog.cancel();
 										}
 									})
 									.show();	
 					} 
 			   })
 			   .setLongLivedNegativeButton("Delete", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
 					@Override
 					public void onClick(DialogInterface dialog, int whichButton) {
 						final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(activity);
 						innerBuilder.setTitle("Delete " + categoriesSpinner.getSelectedItem().toString() + "?")
 									.setCancelable(true)
 									.setPositiveButton("Delete", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 											final String name = categoriesSpinner.getSelectedItem().toString();
 											if (this.activity._db.deleteCategory(name)) {
 												categories.notifyDataSetChanged();
 											}
 											else {
 												Toast.makeText(this.activity, SmartReceiptsActivity.DB_ERROR, Toast.LENGTH_SHORT).show();
 											}
 										}
 									})
 									.setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 											dialog.cancel();
 										}
 									})
 									.show();
 					} 
 			   })
 			   .show();
     }
     
    	//Private Listener Classes
     private DateEditTextListener _dateTextListener = new DateEditTextListener(this);
 	private DurationDateEditTextListener _defaultDurationListener = new DurationDateEditTextListener(this);	
 	private final class DateEditTextListener implements OnClickListener {
 		private final SmartReceiptsActivity _activity;
 		public DateEditTextListener(SmartReceiptsActivity activity) {_activity = activity;}
 		@Override public final void onClick(final View v) {_activity.initCalendar((DateEditText)v);}
 	}
 	private final class DurationDateEditTextListener implements OnClickListener {
 		private final SmartReceiptsActivity _activity;
 		private DateEditText _end;
 		public DurationDateEditTextListener(SmartReceiptsActivity activity) {_activity = activity;}
 		public final void setEnd(DateEditText end) {_end = end;}
 		@Override public final void onClick(final View v) {_activity.initDurationCalendar((DateEditText)v, _end);}
 	}
 	private final class SpinnerSelectionListener implements OnItemSelectedListener {
 		private final TextView _commentBox;
 		private final ArrayAdapter<CharSequence> _categories;
 		public SpinnerSelectionListener(TextView commentBox, ArrayAdapter<CharSequence> categories) {_commentBox = commentBox; _categories = categories;}
 		@Override public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {_commentBox.setText(_categories.getItem(position));}
 		@Override public void onNothingSelected(AdapterView<?> arg0) {}
 		
 	}
 	
     
 }
