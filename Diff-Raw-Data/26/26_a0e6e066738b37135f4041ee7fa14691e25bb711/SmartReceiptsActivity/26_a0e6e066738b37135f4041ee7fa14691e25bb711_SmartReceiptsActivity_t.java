 package wb.receiptslibrary;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Currency;
 import java.util.Locale;
 
 import wb.android.storage.StorageManager;
 import wb.android.dialog.BetterDialogBuilder;
 import wb.android.dialog.DirectDialogOnClickListener;
 import wb.android.dialog.DirectLongLivedOnClickListener;
 import wb.android.flex.Flex;
 import wb.android.flex.Flexable;
 import wb.android.util.AppRating;
 import wb.receiptslibrary.EmailAttachmentWriter.EmailOptions;
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.Bitmap.CompressFormat;
 import android.media.ExifInterface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.text.InputType;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
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
import wb.receiptspro.R;
 
 public abstract class SmartReceiptsActivity extends Activity implements Flexable {
     
 	//logging variables
     private static final boolean D = true;
     private static final String TAG = "SmartReceiptsActivity";
     
 	//Public 
 	public static final Locale LOCALE = Locale.getDefault();
     
     //Activity Request ints
     private static final int NEW_RECEIPT_CAMERA_REQUEST = 1;
     private static final int ADD_PHOTO_CAMERA_REQUEST = 2;
     private static final int RETAKE_PHOTO_CAMERA_REQUEST = 3;
     private static final int NATIVE_NEW_RECEIPT_CAMERA_REQUEST = 4;
     private static final int NATIVE_ADD_PHOTO_CAMERA_REQUEST = 5;
     private static final int NATIVE_RETAKE_PHOTO_CAMERA_REQUEST = 6;
     
     //Camera Request Extras
     static final String STRING_DATA = "strData";
     static final int DIR = 0;
     static final int NAME = 1;
     
     //Menu Settings
     private static final int ABOUT_ID = 0;
     private static final int SETTINGS_ID = 1;
     private static final int CATEGORIES_ID = 2;
     private static final int RETAKE_PHOTO_ID = 3;
     private static final int CSV_MENU_ID = 4;
     
     //Preferences
     protected static final String SMART_PREFS = "SmartReceiptsPrefFile";
     protected static final String INT_DEFAULT_TRIP_DURATION = "TripDuration";
     protected static final String STRING_DEFAULT_EMAIL_TO = "EmailTo";
     protected static final String STRING_USERNAME = "UserName";
     protected static final String BOOL_PREDICT_CATEGORIES = "PredictCats";
     protected static final String BOOL_MATCH_COMMENT_WITH_CATEGORIES = "MatchCommentCats";
     protected static final String BOOL_MATCH_NAME_WITH_CATEGORIES = "MatchNameCats";
     protected static final String BOOL_USE_NATIVE_CAMERA = "UseNativeCamera";
     protected static final String BOOL_ACTION_SEND_SHOW_HELP_DIALOG = "ShowHelpDialog";
     protected static final String BOOL_ONLY_INCLUDE_EXPENSABLE_ITEMS ="OnlyIncludeExpensable";
     protected static final String STRING_CURRENCY = "isocurr";
     protected static final String FLOAT_MIN_RECEIPT_PRICE = "MinReceiptPrice";
     
     //SubClass Preferences
     protected static final String SUBCLASS_PREFS = "SubClassPrefs";
     protected static final String PREF1 = "pref1";
     
     //Receiver Settings
     protected static final String FILTER_ACTION = "wb.receiptslibrary";
     
     //AppRating
     private static final int LAUNCHES_UNTIL_PROMPT = 15;
     
     //Private Instance Variables. Only used in this class
     private String _title;
     private RelativeLayout _mainLayout;
     private ReceiptRow _highlightedReceipt;
     private ListView _listView;
     private ImageView _imgView;
     private boolean _isViewingImg, _calledFromActionSend;
     private Uri _imageUri, _actionSendUri;
     
     //Package-Accessible Instance Variables. None of these require get/set to improve performance
     StorageManager _sdCard;
     TripAdapter _tripAdapter;
     ReceiptAdapter _receiptAdapter;
     DatabaseHelper _db;
     TripRow _currentTrip;
     boolean _isViewingTrip = false; 
     
     //Protected Instance Variables
     protected Flex _flex;
     
     //Preferences
     protected boolean _predictCategories, _matchCommentCats, _matchNameCats, _useNativeCamera, _onlyIncludeExpensable;
     protected String _emailTo, _currency, _userID;
     protected int _defaultTripDuration;
     protected float _minReceiptPrice;
     
 	/* OCR Stuff:
 	 * 	 1. Check that the DC card is mounted and an OCR isn't currently in progress (boolean checks)
 	 *	 2. Mark the OCR as having started (boolean)
 	 *	 3. Apply fancy formatting/status bars if desired
 	 */
     
     protected final void onCreate(final Bundle savedInstanceState, final RelativeLayout mainLayout, final ListView listView) {
     	if(D) Log.d(TAG, "onCreate");
         super.onCreate(savedInstanceState);
         setResult(Activity.RESULT_CANCELED); //In case the user backs out
         _flex = Flex.getInstance(this, this);
         if (this.getIntent().getAction().equalsIgnoreCase(Intent.ACTION_SEND)) {
         	_calledFromActionSend = true;
         	if (this.getIntent().getExtras() != null) {
     	        String[] proj = {MediaStore.Images.Media.DATA};
     	        Cursor cursor = managedQuery((Uri) this.getIntent().getExtras().get(Intent.EXTRA_STREAM), proj, null, null, null);
     	        if (cursor == null) {
     	        	Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.IMG_SEND_ERROR), Toast.LENGTH_LONG).show();
     	        	return;
     	        }
     	        int col = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
     	        cursor.moveToFirst();
     	        _actionSendUri = Uri.fromFile(new File(cursor.getString(col)));
     	        cursor.close();
         	}
         	else {
         		Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.IMG_SEND_ERROR), Toast.LENGTH_LONG).show();
 	        	return;
         	}
         }
         else
         	_calledFromActionSend = false;
         _title = _flex.getString(R.string.APP_TITLE);
         _isViewingImg = false;
     	SharedPreferences prefs = getSharedPreferences(SMART_PREFS, 0);
     	_defaultTripDuration = prefs.getInt(INT_DEFAULT_TRIP_DURATION, 3);
     	_emailTo = prefs.getString(STRING_DEFAULT_EMAIL_TO, "");
     	_predictCategories = prefs.getBoolean(BOOL_PREDICT_CATEGORIES, true);
     	_useNativeCamera = prefs.getBoolean(BOOL_USE_NATIVE_CAMERA, false);
     	_matchCommentCats = prefs.getBoolean(BOOL_MATCH_COMMENT_WITH_CATEGORIES, false);
     	_matchNameCats = prefs.getBoolean(BOOL_MATCH_NAME_WITH_CATEGORIES, false);
     	_onlyIncludeExpensable = prefs.getBoolean(BOOL_ONLY_INCLUDE_EXPENSABLE_ITEMS, true);
     	_minReceiptPrice = prefs.getFloat(FLOAT_MIN_RECEIPT_PRICE, -Float.MAX_VALUE);
     	_userID = prefs.getString(STRING_USERNAME, "");
     	try {
     		_currency = prefs.getString(STRING_CURRENCY, Currency.getInstance(LOCALE).getCurrencyCode());
     	} catch (IllegalArgumentException ex) {
 			_currency = "USD";
 		}
 	    _db = DatabaseHelper.getInstance(this);
 	    _mainLayout = mainLayout;
         _listView = listView;
         _tripAdapter = new TripAdapter(this, _db.getTrips());
         _receiptAdapter = new ReceiptAdapter(this, new ReceiptRow[0]);
         _listView.setAdapter(_tripAdapter);
         AppRating.onLaunch(this, LAUNCHES_UNTIL_PROMPT, _title, this.getPackageName());
     }
     
     @Override
     protected final void onStart() {
     	super.onStart();
         if (!_isViewingTrip || _currentTrip == null)
 	        super.setTitle(_title);
         else if (_isViewingImg && _highlightedReceipt != null)
         	super.setTitle(_highlightedReceipt.name);
         else
         	this.viewTrip(_currentTrip);
     }
     
     public abstract String getPackageName(); 
     
     @Override
     protected final void onResume() {
     	super.onResume();
     	_flex = Flex.getInstance(this, this);
     	_flex.onResume();
     	_sdCard = StorageManager.getInstance(this);
     	if (!_sdCard.isExternal())
         	Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.SD_WARNING), Toast.LENGTH_LONG).show();
     	if (_calledFromActionSend) {
     		final SharedPreferences prefs = getSharedPreferences(SMART_PREFS, 0);
     		if (prefs.getBoolean(BOOL_ACTION_SEND_SHOW_HELP_DIALOG, true)) {
 	        	BetterDialogBuilder builder = new BetterDialogBuilder(this);
 	        	builder.setTitle("Add Picture to Receipt")
 	        		   .setMessage("Tap on an existing receipt to add this image to it.")
 	        		   .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
 	    					@Override
 	    					public void onClick(DialogInterface dialog, int which) {
 	    						dialog.cancel();
 	    					}
 	        		   })
 	        		   .setNegativeButton("Don't Show Again", new DialogInterface.OnClickListener() {
 	    					@Override
 	    					public void onClick(DialogInterface dialog, int which) {
 	    						SharedPreferences.Editor editor = prefs.edit();
 	    						editor.putBoolean(BOOL_ACTION_SEND_SHOW_HELP_DIALOG, false);
 	    						editor.commit();
 	    						dialog.cancel();
 	    					}
 	        		   })
 	        		   .show();
     		}
     	}
     	if (_isViewingTrip) {
     		if (_currentTrip != null)
     			viewTrip(_currentTrip);
     		else
     			_isViewingTrip = false;
     	}
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	DialogController.getInstance(this, _sdCard, _flex).onPause();
     	if (_flex != null) _flex.onPause();
     }
     
     @Override
     protected final void onDestroy() {
     	super.onDestroy();
     	if (_db != null) _db.onDestroy();
     }
     
     /**
      * This method is only called the first time Smart Receipts is run.
      * It gives a brief overview of how to use the app.
      */
     protected void onFirstRun() {
     	BetterDialogBuilder builder = new BetterDialogBuilder(this);
     	builder.setTitle(_flex.getString(R.string.DIALOG_WELCOME_TITLE))
     		   .setMessage(_flex.getString(R.string.DIALOG_WELCOME_MESSAGE))
     		   .setPositiveButton(_flex.getString(R.string.DIALOG_WELCOME_POSITIVE_BUTTON), new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
     		   })
     		   .show();
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
     	DialogController.getInstance(this, _sdCard, _flex).showTripMenu(trip);
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
     	if (_sdCard == null) _sdCard = StorageManager.getInstance(this);
     	String dirPath;
     	try {
 			if (_currentTrip.dir.exists())
 				dirPath = _currentTrip.dir.getCanonicalPath();
 			else
 				dirPath = _sdCard.mkdir(_currentTrip.dir.getName()).getCanonicalPath();
 		} catch (IOException e) {
 			Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
 			return;
 		}
     	if (_useNativeCamera) {
         	final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
             _imageUri = Uri.fromFile(new File(dirPath, System.currentTimeMillis() + "x" + _db.getReceipts(_currentTrip).length + ".jpg"));
             intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
             startActivityForResult(intent, NATIVE_NEW_RECEIPT_CAMERA_REQUEST);
     	}
     	else {
 			final Intent intent = new Intent(this, MyCameraActivity.class);
 			String[] strings  = new String[] {dirPath, System.currentTimeMillis() + "x" + _db.getReceipts(_currentTrip).length + ".jpg"};
 			intent.putExtra(STRING_DATA, strings);
 			this.startActivityForResult(intent, NEW_RECEIPT_CAMERA_REQUEST);
     	}
     }
     
     public final void addTextReceipt() {
     	this.receiptMenu(_currentTrip ,null, null);
     }
     
     public final void receiptMenu(final TripRow trip, final ReceiptRow receipt, final File img) {
     	DialogController.getInstance(this, _sdCard, _flex).showReceiptMenu(trip, receipt, img);
     }
     
     final void updateTitlePrice(TripRow trip, ReceiptRow oldReceipt, ReceiptRow newReceipt) {
     	if (newReceipt.price == null || newReceipt.price.length() == 0)
     		return;
     	else {
 			final String currency = SmartReceiptsActivity.CurrencyValue(trip.price, trip.currency);
 			this.setTitle(currency + " - " + trip.dir.getName());
 		}
     }
     
     public final void emailTrip() {
     	DialogController.getInstance(this, _sdCard, _flex).showEmailTripMenu();
     }
     
     public final void postCreateAttachments(File[] files) {
 		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);  
 		emailIntent.setType("application/octet-stream");
 		ArrayList<Uri> uris = new ArrayList<Uri>();
 		if (files[EmailOptions.PDF_FULL.getIndex()] != null) uris.add(Uri.fromFile(files[EmailOptions.PDF_FULL.getIndex()]));
 		if (files[EmailOptions.PDF_IMAGES_ONLY.getIndex()] != null) uris.add(Uri.fromFile(files[EmailOptions.PDF_IMAGES_ONLY.getIndex()]));
 		if (files[EmailOptions.CSV.getIndex()] != null) uris.add(Uri.fromFile(files[EmailOptions.CSV.getIndex()]));
 		if (files[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] != null) uris.add(Uri.fromFile(files[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()]));
 		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{_emailTo});
 		emailIntent.putExtra(Intent.EXTRA_SUBJECT, _flex.getString(R.string.EMAIL_DATA_SUBJECT).replaceAll("%REPORT_NAME%", _currentTrip.dir.getName()).replaceAll("%USER_ID%", _userID));
 		if (uris.size() == 1) emailIntent.putExtra(Intent.EXTRA_TEXT, uris.size() + " report attached");
 		if (uris.size() > 1) emailIntent.putExtra(Intent.EXTRA_TEXT, uris.size() + " reports attached");
 		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
     	startActivity(Intent.createChooser(emailIntent, "Send mail..."));
     }
     
     public final boolean editTrip(final TripRow trip) {
 		final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 		final String[] editTripItems = _flex.getStringArray(R.array.EDIT_TRIP_ITEMS);
 		builder.setTitle(trip.dir.getName())
 			   .setCancelable(true)
 			   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       })
 		       .setItems(editTripItems, new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	final String selection = editTripItems[item].toString();
 				    	if (selection == editTripItems[0]) { //Email Trip
 				    		activity._currentTrip = trip;
 				    		activity.emailTrip();
 				    	}
 				    	else if (selection == editTripItems[1]) //Edit Trip
 				    		activity.tripMenu(trip); 
 				    	else if (selection == editTripItems[2]) //Delete Trip
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
 		                			Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
 		                	activity._tripAdapter.notifyDataSetChanged(activity._db.getTrips());
 		                }
 		                else
 		                	Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
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
 		if (_calledFromActionSend) {
 			if (receipt.img == null) {
 				final String[] actionSendNoImgEditReceiptItems = _flex.getStringArray(R.array.ACTION_SEND_NOIMG_EDIT_RECEIPT_ITEMS);
 				builder.setItems(actionSendNoImgEditReceiptItems, new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 					@Override
 					public void onClick(DialogInterface dialog, int item) {
 						final String selection = actionSendNoImgEditReceiptItems[item].toString();
 						if (selection == actionSendNoImgEditReceiptItems[0]) { //Attach Image to Receipt
 							String dirPath;
 							try {
 								if (_currentTrip.dir.exists())
 									dirPath = _currentTrip.dir.getCanonicalPath();
 								else
 									dirPath = _sdCard.mkdir(_currentTrip.dir.getName()).getCanonicalPath();
 							} catch (IOException e) {
 								   Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
 								   return;
 							}
 							File imgFile = transformNativeCameraBitmap(_actionSendUri, null, Uri.fromFile(new File(dirPath, receipt.id + "x.jpg")));
 							if (imgFile != null) {
 								Log.e(TAG, imgFile.getPath());
 								final ReceiptRow updatedReceipt = _db.updateReceiptImg(receipt, imgFile);
 								if (updatedReceipt != null) {
 									_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
 									Toast.makeText(SmartReceiptsActivity.this, "Receipt Image Successfully Added to " + _highlightedReceipt.name, Toast.LENGTH_SHORT).show();
 									setResult(RESULT_OK, new Intent(Intent.ACTION_SEND, Uri.fromFile(imgFile)));
 									_isViewingTrip = false;
 									finish();
 								}
 								else {
 									Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 									_sdCard.delete(imgFile); //Rollback
 									finish();
 									return;
 								}
 							}
 							else {
 								Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
 								finish();
 							}
 						}
 					}
 				});
 			}
 			else {
 				final String[] actionSendImgEditReceiptItems = _flex.getStringArray(R.array.ACTION_SEND_IMG_EDIT_RECEIPT_ITEMS);
 				builder.setItems(actionSendImgEditReceiptItems, new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 					@Override
 					public void onClick(DialogInterface dialog, int item) {
 						final String selection = actionSendImgEditReceiptItems[item].toString();
 						if (selection == actionSendImgEditReceiptItems[0]) //View Image
 							activity.showImage(receipt);
 						else if (selection == actionSendImgEditReceiptItems[1]) { //Attach Image to Receipt
 							String dirPath;
 							try {
 								if (_currentTrip.dir.exists())
 									dirPath = _currentTrip.dir.getCanonicalPath();
 								else
 									dirPath = _sdCard.mkdir(_currentTrip.dir.getName()).getCanonicalPath();
 							} catch (IOException e) {
 								   Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
 								   return;
 							}
 							File imgFile = transformNativeCameraBitmap(_actionSendUri, null, Uri.fromFile(new File(dirPath, receipt.id + "x.jpg")));
 							if (imgFile != null) {
 								Log.e(TAG, imgFile.getPath());
 								final ReceiptRow retakeReceipt = _db.updateReceiptImg(_highlightedReceipt, imgFile);
 								if (retakeReceipt != null) {
 									_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
 									Toast.makeText(SmartReceiptsActivity.this, "Receipt Image Successfully Replaced for " + _highlightedReceipt.name, Toast.LENGTH_SHORT).show();
 									setResult(RESULT_OK, new Intent(Intent.ACTION_SEND, Uri.fromFile(imgFile)));
 									_isViewingTrip = false;
 									finish();
 								}
 								else {
 									Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 									//Add overwrite rollback here
 									finish();
 								}
 							}
 							else {
 								Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
 								finish();
 							}
 						}
 					}
 				});
 			}
 		}
 		else {
 			if (receipt.img == null) {
 				final String[] noImgEditReceiptItems = _flex.getStringArray(R.array.NOIMG_EDIT_RECEIPT_ITEMS);
 				builder.setItems(noImgEditReceiptItems, new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 					@Override
 					public void onClick(DialogInterface dialog, int item) {
 				    	final String selection = noImgEditReceiptItems[item].toString();
 				    	if (selection == noImgEditReceiptItems[0]) //Edit Receipt
 				    		activity.receiptMenu(activity._currentTrip, receipt, null);
 				    	else if (selection == noImgEditReceiptItems[1]) { //Take Photo
 							String dirPath;
 							try {
 								if (_currentTrip.dir.exists())
 									dirPath = _currentTrip.dir.getCanonicalPath();
 								else
 									dirPath = _sdCard.mkdir(_currentTrip.dir.getName()).getCanonicalPath();
 							} catch (IOException e) {
 								   Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
 								   return;
 							}
 				    		if (_useNativeCamera) {
 				            	final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 				                _imageUri = Uri.fromFile(new File(dirPath, receipt.id + "x.jpg"));
 				                intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
 				                startActivityForResult(intent, NATIVE_ADD_PHOTO_CAMERA_REQUEST);
 				        	}
 				        	else {
 								final Intent intent = new Intent(activity, MyCameraActivity.class);
 								String[] strings  = new String[] {dirPath, receipt.id + "x.jpg"};
 								intent.putExtra(STRING_DATA, strings);
 								activity.startActivityForResult(intent, ADD_PHOTO_CAMERA_REQUEST);
 				        	}
 				    	}
 				    	else if (selection == noImgEditReceiptItems[2]) //Delete Receipt
 				    		activity.deleteReceipt(receipt);
 				    	else if (selection == noImgEditReceiptItems[3]) //Move Up
 				    		activity.moveReceiptUp(receipt);
 				    	else if (selection == noImgEditReceiptItems[4]) //Move Down
 				    		activity.moveReceiptDown(receipt);
 				    	dialog.cancel();
 				    }
 				});
 			}
 			else {
 				final String[] imgEditReceiptItems = _flex.getStringArray(R.array.IMG_EDIT_RECEIPT_ITEMS);
 				builder.setItems(imgEditReceiptItems, new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 					@Override
 				    public void onClick(DialogInterface dialog, int item) {
 				    	final String selection = imgEditReceiptItems[item].toString();
 				    	if (selection == imgEditReceiptItems[0]) //Edit Receipt
 				    		activity.receiptMenu(_currentTrip, receipt, receipt.img);
 				    	else if (selection == imgEditReceiptItems[1]) //View/Retake Image 
 				    		activity.showImage(receipt);
 				    	else if (selection == imgEditReceiptItems[2]) //Delete Receipt
 				    		activity.deleteReceipt(receipt);
 				    	else if (selection == imgEditReceiptItems[3]) //Move Up
 				    		activity.moveReceiptUp(receipt);
 				    	else if (selection == imgEditReceiptItems[4]) //Move Down
 				    		activity.moveReceiptDown(receipt);
 				    	dialog.cancel();
 				    }
 				});
 			}
 		}
 		builder.show();
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
     			Toast.makeText(this, _flex.getString(R.string.IMG_OPEN_ERROR), Toast.LENGTH_SHORT).show();
     		System.gc(); 
     		_imgView.setImageBitmap(BitmapFactory.decodeFile(receipt.img.getCanonicalPath()));
     		//_imgView.setImageBitmap(testImageManipulation(receipt));
     		this.setTitle(receipt.name);
     		this.setContentView(_imgView, params);
     	}
     	catch (IOException e) {
     		Toast.makeText(this, _flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
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
 		                			Toast.makeText(activity, _flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
 		                	}
 		                	if (receipt.price != null && receipt.price.length() != 0) {
 			    				final String currency = SmartReceiptsActivity.CurrencyValue(_currentTrip.price, _currentTrip.currency);
 			    				activity.setTitle(currency + " - " + activity._currentTrip.dir.getName());
 		                	}
 		                }
 		                else
 		                	Toast.makeText(activity, _flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 		           }
 		       })
 		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       })
 		       .show();
     }
     
     final void moveReceiptUp(final ReceiptRow receipt) {
     	_db.moveReceiptUp(_currentTrip, receipt);
     	_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
     }
     
     final void moveReceiptDown(final ReceiptRow receipt) {
     	_db.moveReceiptDown(_currentTrip, receipt);
     	_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
     }
     
     private final void deleteDuplicateGalleryImage() { //Deletes any gallery images that were taken within the last 5 seconds (i.e. duplicates)
     	try {
 	    	final String[] imageColumns = { MediaStore.Images.Media._ID };
 	    	Cursor c = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, MediaStore.Images.Media.DATE_TAKEN + " > " + (System.currentTimeMillis() - 5000), null); //Get all images that occured within the last 5secs
 	        if(c.moveToFirst()){
 	            int id = c.getInt(c.getColumnIndex(MediaStore.Images.Media._ID));
 	            getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media._ID + "=?", new String[]{ Integer.toString(id) } );
 	        }
 	        c.close();
     	}
     	catch (Exception e) { } //Ignore any errors
     }
     
     @Override
     protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
     	if (D) Log.d(TAG, "Result Code: " + resultCode);
     	if (resultCode == RESULT_OK) { //-1
     		deleteDuplicateGalleryImage(); //Some devices duplicate the gallery images
 			File imgFile = this.transformNativeCameraBitmap(_imageUri, data, null);
 			if (imgFile == null) {
 				Toast.makeText(this, _flex.getString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
 				return;
 			}
     		switch (requestCode) {
 				case NATIVE_NEW_RECEIPT_CAMERA_REQUEST:
 					this.receiptMenu(_currentTrip, null, imgFile);
 				break;
 				case NATIVE_ADD_PHOTO_CAMERA_REQUEST:
 					final ReceiptRow updatedReceipt = _db.updateReceiptImg(_highlightedReceipt, imgFile);
 					if (updatedReceipt != null) {
 						_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
 						Toast.makeText(SmartReceiptsActivity.this, "Receipt Image Successfully Added to " + _highlightedReceipt.name, Toast.LENGTH_SHORT).show();
 					}
 					else {
 						Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 						_sdCard.delete(imgFile); //Rollback
 						return;
 					}
 				break;
 				case NATIVE_RETAKE_PHOTO_CAMERA_REQUEST:
 					final ReceiptRow retakeReceipt = _db.updateReceiptImg(_highlightedReceipt, imgFile);
 					if (retakeReceipt != null) {
 						_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
 						Toast.makeText(SmartReceiptsActivity.this, "Receipt Image Successfully Replaced for " + _highlightedReceipt.name, Toast.LENGTH_SHORT).show();
 		    			_isViewingImg = false;
 		    			this.setContentView(_mainLayout);
 						final String currency = SmartReceiptsActivity.CurrencyValue(_currentTrip.price, _currentTrip.currency);
 						this.setTitle(currency + " - " + _currentTrip.dir.getName());
 					}
 					else {
 						Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 						//Add overwrite rollback here
 						return;
 					}
 				break;
 				default:
 					Log.e(TAG, "Unrecognized Request Code: " + requestCode);
 					super.onActivityResult(requestCode, resultCode, data);
 				break;
 			}
     	}
     	else if (resultCode == MyCameraActivity.PICTURE_SUCCESS) {  //51
 	    	switch (requestCode) {
 				case NEW_RECEIPT_CAMERA_REQUEST:
 					this.receiptMenu(_currentTrip, null, new File(data.getStringExtra(MyCameraActivity.IMG_FILE)));
 				break;
 				case ADD_PHOTO_CAMERA_REQUEST:
 					File img = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
 					final ReceiptRow updatedReceipt = _db.updateReceiptImg(_highlightedReceipt, img);
 					if (updatedReceipt != null) {
 						_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
 						Toast.makeText(SmartReceiptsActivity.this, "Receipt Image Successfully Added to " + _highlightedReceipt.name, Toast.LENGTH_SHORT).show();
 					}
 					else {
 						Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 						return;
 					}
 				break;
 				case RETAKE_PHOTO_CAMERA_REQUEST:
 					File retakeImg = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
 					final ReceiptRow retakeReceipt = _db.updateReceiptImg(_highlightedReceipt, retakeImg);
 					if (retakeReceipt != null) {
 						_receiptAdapter.notifyDataSetChanged(_db.getReceipts(_currentTrip));
 						Toast.makeText(SmartReceiptsActivity.this, "Receipt Image Successfully Replaced for " + _highlightedReceipt.name, Toast.LENGTH_SHORT).show();
 		    			_isViewingImg = false;
 		    			this.setContentView(_mainLayout);
 						final String currency = SmartReceiptsActivity.CurrencyValue(_currentTrip.price, _currentTrip.currency);
 						this.setTitle(currency + " - " + _currentTrip.dir.getName());
 					}
 					else {
 						Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
 						//Add overwrite rollback here
 						return;
 					}
 				break;
 				default:
 					Log.e(TAG, "Unrecognized Request Code: " + requestCode);
 					super.onActivityResult(requestCode, resultCode, data);
 				break;
 			}
     	}
     	else {
 			Log.e(TAG, "Unrecgonized Result Code: " + resultCode);
 			super.onActivityResult(requestCode, resultCode, data);
     	}
     }
     
     //If imageDestination == null, then it's set to the imageUri location
     private final File transformNativeCameraBitmap(final Uri imageUri, final Intent data, Uri imageDestination) {
 		// Move this all to a separate thread
 		System.gc();
 		Uri imageUriCopy;
 		if (imageUri != null)
 			imageUriCopy = Uri.parse(imageUri.toString());
 		else {
 			if (data != null)
 				imageUriCopy = data.getData();
 			else
 				return null;
 		}
 		if (imageDestination == null)
 			imageDestination = imageUriCopy;
 		File imgFile = new File(imageDestination.getPath());
 		final int maxDimension = 1024;
 		BitmapFactory.Options fullOpts = new BitmapFactory.Options();
 		fullOpts.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(imageUriCopy.getPath(), fullOpts);
 		int fullWidth=fullOpts.outWidth, fullHeight=fullOpts.outHeight;
 		fullOpts = null;
 		int scale=1;
 		while(fullWidth > maxDimension && fullHeight > maxDimension){
 			fullWidth>>>=1;
 			fullHeight>>>=1;
 			scale<<=1;
 		}
 		BitmapFactory.Options smallerOpts = new BitmapFactory.Options();
 		smallerOpts.inSampleSize=scale;
 		System.gc();
 		Bitmap smallerSize = BitmapFactory.decodeFile(imageUriCopy.getPath(), smallerOpts);
 		ExifInterface exif;
 		int degrees = 0;
 		try {
 			exif = new ExifInterface(imageUriCopy.getPath());
 			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
 			switch(orientation) {
 				case ExifInterface.ORIENTATION_ROTATE_270:
 					degrees-=90;
 				case ExifInterface.ORIENTATION_ROTATE_180:
 					degrees-=90;
 				case ExifInterface.ORIENTATION_ROTATE_90:
 					degrees-=90;
 			}
 		} catch (IOException e) {}
 		Bitmap endBitmap = smallerSize;
 		if (degrees != 0) {
 			Matrix matrix = new Matrix();
 			matrix.setRotate(degrees, smallerSize.getWidth()/2, smallerSize.getHeight()/2);
 			endBitmap = Bitmap.createBitmap(smallerSize, 0, 0, smallerSize.getWidth(), smallerSize.getHeight(), matrix, false);
 			smallerSize = null;
 		}
 		if (_sdCard == null) _sdCard = StorageManager.getInstance(this);
 		if (!_sdCard.writeBitmap(imageDestination, endBitmap, CompressFormat.JPEG, 85)) {
 			Toast.makeText(this, "Error: The Image Failed to Save Properly", Toast.LENGTH_SHORT).show();
 			imgFile = null;
 		}
     	return imgFile;
     }
     
     
     private static final String STATE_IMAGE_URI = "imageUriState";
     private static final String STATE_CURR_TRIP = "currTripState";
     private static final String STATE_HIGH_RCPT = "highRcptState";
     private static final String STATE_VIEW_TRIP = "viewTripState";
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	outState.putBoolean(STATE_VIEW_TRIP, _isViewingTrip);
     	if (_imageUri != null) outState.putString(STATE_IMAGE_URI, _imageUri.toString());
     	if (_highlightedReceipt != null) outState.putInt(STATE_HIGH_RCPT, _highlightedReceipt.id);
     	try {if (_currentTrip != null) outState.putString(STATE_CURR_TRIP, _currentTrip.dir.getCanonicalPath());} catch (IOException e) {}
     	super.onSaveInstanceState(outState);
     }
     
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		if (_sdCard == null) _sdCard = StorageManager.getInstance(this); //These null checks aren't needed, because getInstance will handle it
 		if (_db == null) _db = DatabaseHelper.getInstance(this);
     	if (savedInstanceState != null) {
     		_isViewingTrip = savedInstanceState.getBoolean(STATE_VIEW_TRIP);
     		_imageUri = (savedInstanceState.getString(STATE_IMAGE_URI) != null) ? Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI)) : null;
     		_highlightedReceipt = _db.getReceiptByID(savedInstanceState.getInt(STATE_HIGH_RCPT));
     		_currentTrip = _db.getTripByName(savedInstanceState.getString(STATE_CURR_TRIP));
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
 			this.setTitle(_title);
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
 		MenuItem csv = menu.add(Menu.NONE, CSV_MENU_ID, Menu.NONE, "Customize CSV Output");
 		csv.setIcon(android.R.drawable.ic_menu_set_as);
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
     		MenuItem csv = menu.getItem(CSV_MENU_ID);
     		if (retake != null) csv.setVisible(false);
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
     		MenuItem csv = menu.getItem(CSV_MENU_ID);
     		if (retake != null) csv.setVisible(true);
     	}
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	if (item.getItemId() == ABOUT_ID) {
     		String about = _flex.getString(R.string.DIALOG_ABOUT_MESSAGE);
     		try {
     			about = about.replace("VERSION_NAME", this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
     		} catch (NameNotFoundException e) { }
 	    	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
 			builder.setTitle(_flex.getString(R.string.DIALOG_ABOUT_TITLE))
 				   .setMessage(about)
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
 	    	final EditText email = new EditText(this); email.setText(_emailTo); email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
 	    	final TextView daysText = new TextView(this); daysText.setPadding(6, 10, 6, 0); daysText.setTextSize(16F); daysText.setText("Default Trip Length (Days):");
 	    	final EditText days = new EditText(this); days.setInputType(InputType.TYPE_CLASS_NUMBER); days.setText("" + _defaultTripDuration);
 	    	final TextView currencyText = new TextView(this); currencyText.setPadding(6, 10, 6, 0); currencyText.setTextSize(16F); currencyText.setText("Default Currency:");
 			final Spinner currencySpinner = new Spinner(this);
 			final TextView minPriceText = new TextView(this); minPriceText.setPadding(6, 10, 6, 0); minPriceText.setTextSize(16F); minPriceText.setText("Min. Receipt Price to Report:");
 			final EditText minPrice = new EditText(this); minPrice.setInputType(InputType.TYPE_CLASS_NUMBER); minPrice.setHint("Leave Blank to Include All");
 			final TextView userIDText = new TextView(this); userIDText.setPadding(6, 10, 6, 0); userIDText.setTextSize(16F); userIDText.setText("User ID:");
 			final EditText userID = new EditText(this); userID.setText(_userID);
 			if (_minReceiptPrice != -Float.MAX_VALUE) minPrice.setText(Float.toString(_minReceiptPrice));
 			final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, this._db.getCurrenciesList());
 			currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			currencySpinner.setAdapter(currenices); currencySpinner.setPrompt("Default Currency");
 			int idx = currenices.getPosition(_currency);
 			if (idx > 0) currencySpinner.setSelection(idx);
 	    	final CheckBox predictCategoires = new CheckBox(this); predictCategoires.setText(" Predict Receipt Categories"); predictCategoires.setChecked(_predictCategories);
 	    	final CheckBox useNativeCamera = new CheckBox(this); useNativeCamera.setText(" Use Native Camera"); useNativeCamera.setChecked(_useNativeCamera);
 	    	final CheckBox matchNameToCategory = new CheckBox(this); matchNameToCategory.setText(" Match Name to Categories"); matchNameToCategory.setChecked(_matchNameCats);
 	    	final CheckBox matchCommentsToCategory = new CheckBox(this); matchCommentsToCategory.setText(" Match Comments to Categories"); matchCommentsToCategory.setChecked(_matchCommentCats);
 	    	final CheckBox onlyIncludeExpensableItems = new CheckBox(this); onlyIncludeExpensableItems.setText(" Only Report Expensable Receipts"); onlyIncludeExpensableItems.setChecked(_onlyIncludeExpensable);
 	    	layout.addView(emailText); layout.addView(email);
 	    	layout.addView(daysText); layout.addView(days);
 	    	layout.addView(currencyText); layout.addView(currencySpinner);
 	    	layout.addView(minPriceText); layout.addView(minPrice);
 	    	layout.addView(userIDText); layout.addView(userID);
 	    	layout.addView(predictCategoires);
 	    	layout.addView(useNativeCamera);
 	    	layout.addView(matchNameToCategory);
 	    	layout.addView(matchCommentsToCategory);
 	    	layout.addView(onlyIncludeExpensableItems);
 	    	scrollView.addView(layout);
 			builder.setTitle("Settings")
 				   .setView(scrollView)
 				   .setCancelable(true)
 				   .setPositiveButton("Save", new DirectDialogOnClickListener<SmartReceiptsActivity>(this) {
 			           public void onClick(DialogInterface dialog, int id) {
 			        	   try {
 				        	   if (days.getText().toString() != null && days.getText().toString().length() > 0 && days.getText().toString().length() < 4)
 				        		   activity._defaultTripDuration = Integer.parseInt(days.getText().toString());
 				        	   if (minPrice.getText().toString() != null) {
 				        		   if (minPrice.getText().toString().length() > 0 && minPrice.getText().toString().length() < 4)
 				        			   activity._minReceiptPrice = Integer.parseInt(minPrice.getText().toString());
 				        		   else if (minPrice.getText().toString().length() == 0)
 				        			   activity._minReceiptPrice = -Float.MAX_VALUE;
 				        	   }
 				               activity._emailTo = email.getText().toString();
 				               activity._currency = currencySpinner.getSelectedItem().toString();
 				               activity._predictCategories = predictCategoires.isChecked();
 				               activity._useNativeCamera = useNativeCamera.isChecked();
 				               activity._matchNameCats = matchNameToCategory.isChecked();
 				               activity._matchCommentCats = matchCommentsToCategory.isChecked();
 				               activity._onlyIncludeExpensable = onlyIncludeExpensableItems.isChecked();
 				               activity._userID = userID.getText().toString();
 				               SharedPreferences prefs = getSharedPreferences(SMART_PREFS, 0);
 				               SharedPreferences.Editor editor = prefs.edit();
 				               editor.putInt(INT_DEFAULT_TRIP_DURATION, activity._defaultTripDuration);
 				               editor.putFloat(FLOAT_MIN_RECEIPT_PRICE, activity._minReceiptPrice);
 					           editor.putString(STRING_DEFAULT_EMAIL_TO, activity._emailTo);
 					           editor.putString(STRING_CURRENCY, _currency);
 					           editor.putBoolean(BOOL_PREDICT_CATEGORIES, activity._predictCategories);
 					           editor.putBoolean(BOOL_USE_NATIVE_CAMERA, activity._useNativeCamera);
 					           editor.putBoolean(BOOL_MATCH_NAME_WITH_CATEGORIES, activity._matchNameCats);
 					           editor.putBoolean(BOOL_MATCH_COMMENT_WITH_CATEGORIES, activity._matchCommentCats);
 					           editor.putBoolean(BOOL_ONLY_INCLUDE_EXPENSABLE_ITEMS, activity._onlyIncludeExpensable);
 					           editor.putString(STRING_USERNAME, activity._userID);
 					           editor.commit();
 			        	   } catch (java.lang.NumberFormatException e) {
 			        		   activity._emailTo = email.getText().toString();
 			        		   activity._currency = currencySpinner.getSelectedItem().toString();
 				               activity._predictCategories = predictCategoires.isChecked();
 				               activity._useNativeCamera = useNativeCamera.isChecked();
 				               activity._matchNameCats = matchNameToCategory.isChecked();
 				               activity._matchCommentCats = matchCommentsToCategory.isChecked();
 				               activity._onlyIncludeExpensable = onlyIncludeExpensableItems.isChecked();
 				               activity._userID = userID.getText().toString();
 				               SharedPreferences prefs = getSharedPreferences(SMART_PREFS, 0);
 				               SharedPreferences.Editor editor = prefs.edit();
 					           editor.putString(STRING_DEFAULT_EMAIL_TO, activity._emailTo);
 					           editor.putString(STRING_CURRENCY, _currency);
 					           editor.putBoolean(BOOL_PREDICT_CATEGORIES, activity._predictCategories);
 					           editor.putBoolean(BOOL_USE_NATIVE_CAMERA, activity._useNativeCamera);
 					           editor.putBoolean(BOOL_MATCH_NAME_WITH_CATEGORIES, activity._matchNameCats);
 					           editor.putBoolean(BOOL_MATCH_COMMENT_WITH_CATEGORIES, activity._matchCommentCats);
 					           editor.putBoolean(BOOL_ONLY_INCLUDE_EXPENSABLE_ITEMS, activity._onlyIncludeExpensable);
 					           editor.putString(STRING_USERNAME, activity._userID);
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
 			return true;
     	}
     	else if (item.getItemId() == CATEGORIES_ID) {
     		this.showCategoriesMenu();
     		return true;
     	}
     	else if (item.getItemId() == RETAKE_PHOTO_ID) {
     		String dirPath;
     		try {
 				if (_currentTrip.dir.exists())
 					dirPath = _currentTrip.dir.getCanonicalPath();
 				else
 					dirPath = _sdCard.mkdir(_currentTrip.dir.getName()).getCanonicalPath();
     		} catch (IOException e) {
 				Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
 				return false;
 			}
     		if (_useNativeCamera) {
     			final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     			_imageUri = Uri.fromFile(new File(dirPath, _highlightedReceipt.img.getName()));
                 intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
                 startActivityForResult(intent, NATIVE_RETAKE_PHOTO_CAMERA_REQUEST);				
     		}
     		else {
 	    		final Intent intent = new Intent(this, MyCameraActivity.class);
 				String[] strings  = new String[] {dirPath, _highlightedReceipt.img.getName()};
 				intent.putExtra(STRING_DATA, strings);
 				this.startActivityForResult(intent, RETAKE_PHOTO_CAMERA_REQUEST);
     		}
     	}
     	else if (item.getItemId() == CSV_MENU_ID) {
     		this.showCustomCSVMenu();
     		return true;
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
 		if (_db == null) _db = DatabaseHelper.getInstance(this);
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
 													Toast.makeText(this.activity, this.activity._flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
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
 													Toast.makeText(this.activity, this.activity._flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
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
 						if (categoriesSpinner.getSelectedItem() == null) //There are no categories left to delete
 							dialog.cancel();
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
 												Toast.makeText(this.activity, this.activity._flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
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
     
     private class CSVColumnSelectionListener implements OnItemSelectedListener {
     	private DatabaseHelper _db;
     	private int _index;
     	private boolean _firstCall; //During the Spinner Creation, onItemSelected() is automatically called. This boolean ignores the initial call
     	public CSVColumnSelectionListener(DatabaseHelper db, int index) {_db = db; _index = index; _firstCall = true;}
 		@Override
 		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 			if (_firstCall) { //Ignore creation call
 				_firstCall = false;
 				return;
 			}
 			_db.updateCSVColumn(_index, position);
 		}
 		@Override public void onNothingSelected(AdapterView<?> arg0) {}
     	
     }
     private void showCustomCSVMenu() {
     	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
     	ScrollView scrollView = new ScrollView(this);
     	final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		final LinearLayout layout = new LinearLayout(this);
 		layout.setOrientation(LinearLayout.VERTICAL);
 		layout.setGravity(Gravity.BOTTOM);
 		layout.setPadding(6, 6, 6, 6);
 		final CSVColumns csvColumns = _db.getCSVColumns(_flex); 
 		for (int i=0; i < csvColumns.size(); i++) {
 			final LinearLayout horiz = addHorizontalCSVLayoutItem(csvColumns, i);
 			layout.addView(horiz, params);
 		}
 		scrollView.addView(layout);
 		builder.setTitle("Customize CSV File")
 			   .setView(scrollView)
 			   .setCancelable(true)
 			   .setLongLivedPositiveButton("Add Column", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						activity._db.insertCSVColumn();
 						layout.addView(addHorizontalCSVLayoutItem(csvColumns, csvColumns.size() - 1), params);
 					}
 				})
 				.setLongLivedNegativeButton("Remove Column", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (csvColumns.isEmpty())
 							return;
 						activity._db.deleteCSVColumn();
 						layout.removeViews(csvColumns.size(), 1);
 					}
 				})
 			   .show();
     }
     
     private final LinearLayout addHorizontalCSVLayoutItem(CSVColumns csvColumns, int i) {
 		final LinearLayout horiz = new LinearLayout(this);
 		final CSVColumnSelectionListener selectionListener = new CSVColumnSelectionListener(_db, i);
 		horiz.setOrientation(LinearLayout.HORIZONTAL);
 		final Spinner spinner = new Spinner(this);
 		final ArrayAdapter<CharSequence> options = CSVColumns.getNewArrayAdapter(this, _flex);
 		options.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(options); spinner.setPrompt("Column Type");
 		String type = csvColumns.getType(i);
 		int pos = options.getPosition(type);
 		if (pos < 0) { //This was a customized, non-accessbile entry
 			options.add(type);
 			spinner.setSelection(options.getPosition(type));
 			spinner.setEnabled(false);
 		}
 		else {
 			spinner.setSelection(pos);
 		}
 		spinner.setOnItemSelectedListener(selectionListener);
 		final TextView textView = new TextView(this);
 		textView.setPadding(12, 0, 0, 0);
 		textView.setText("Col. " + (i+1));
 		textView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
 		horiz.addView(textView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 2f)); 
 		horiz.addView(spinner, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
 		return horiz;
     }
     
     @Override
     public int getFleXML() {
     	return Flexable.UNDEFINED;
     }
     
     protected void insertCSVDefaults(final DatabaseHelper db) { //Called in onCreate and onUpgrade
 		db.insertCSVColumnNoCache(CSVColumns.CATEGORY_CODE(_flex));
 		db.insertCSVColumnNoCache(CSVColumns.NAME(_flex));
 		db.insertCSVColumnNoCache(CSVColumns.PRICE(_flex));
 		db.insertCSVColumnNoCache(CSVColumns.CURRENCY(_flex));
 		db.insertCSVColumnNoCache(CSVColumns.DATE(_flex));
 	}
     
     protected void insertCategoryDefaults(final DatabaseHelper db) {
 		db.insertCategoryNoCache("<Category>", "NUL");
 		db.insertCategoryNoCache("Airfare", "AIRP");
 		db.insertCategoryNoCache("Breakfast", "BRFT");
 		db.insertCategoryNoCache("Dinner", "DINN");
 		db.insertCategoryNoCache("Entertainment", "ENT");
 		db.insertCategoryNoCache("Gasoline", "GAS");
 		db.insertCategoryNoCache("Gift", "GIFT");
 		db.insertCategoryNoCache("Hotel", "HTL");
 		db.insertCategoryNoCache("Laundry", "LAUN");
 		db.insertCategoryNoCache("Lunch", "LNCH");
 		db.insertCategoryNoCache("Other", "MISC");
 		db.insertCategoryNoCache("Parking/Tolls", "PARK");
 		db.insertCategoryNoCache("Postage/Shipping", "POST");
 		db.insertCategoryNoCache("Car Rental", "RCAR");
 		db.insertCategoryNoCache("Taxi/Bus", "TAXI");
 		db.insertCategoryNoCache("Telephone/Fax", "TELE");
 		db.insertCategoryNoCache("Tip", "TIP");
 		db.insertCategoryNoCache("Train", "TRN");
 		db.insertCategoryNoCache("Books/Periodicals", "ZBKP");
 		db.insertCategoryNoCache("Cell Phone", "ZCEL");
 		db.insertCategoryNoCache("Dues/Subscriptions", "ZDUE");
 		db.insertCategoryNoCache("Meals (Justified)", "ZMEO");
 		db.insertCategoryNoCache("Stantionery/Stations", "ZSTS");
 		db.insertCategoryNoCache("Training Fees", "ZTRN");
     }
 	   
 }
