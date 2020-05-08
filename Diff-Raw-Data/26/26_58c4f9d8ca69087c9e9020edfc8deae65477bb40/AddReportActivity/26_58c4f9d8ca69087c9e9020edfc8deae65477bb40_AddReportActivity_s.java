 /**
  ** Copyright (c) 2010 Ushahidi Inc
  ** All rights reserved
  ** Contact: team@ushahidi.com
  ** Website: http://www.ushahidi.com
  **
  ** GNU Lesser General Public License Usage
  ** This file may be used under the terms of the GNU Lesser
  ** General Public License version 3 as published by the Free Software
  ** Foundation and appearing in the file LICENSE.LGPL included in the
  ** packaging of this file. Please review the following information to
  ** ensure the GNU Lesser General Public License version 3 requirements
  ** will be met: http://www.gnu.org/licenses/lgpl.html.
  **
  **
  ** If you have questions regarding the use of this file, please contact
  ** Ushahidi developers at team@ushahidi.com.
  **
  **/
 
 package foam.doris.android.app.ui.phone;
 
 import java.io.File;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Vector;
 
 import android.content.SharedPreferences;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.util.Log;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.support.v4.view.MenuItem;
 import android.text.Editable;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.KeyEvent;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.DatePicker;
 import android.widget.ImageSwitcher;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TimePicker;
 import android.widget.ViewSwitcher;
 
 // things for camera
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import java.io.FileOutputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.net.URI;
 import android.net.Uri;
 import android.widget.FrameLayout;
 import android.content.pm.ActivityInfo;
 import android.widget.Button;
 
 import foam.doris.android.app.ImageManager;
 import foam.doris.android.app.Preferences;
 import foam.doris.android.app.R;
 import foam.doris.android.app.activities.BaseEditMapActivity;
 import foam.doris.android.app.adapters.UploadPhotoAdapter;
 import foam.doris.android.app.database.Database;
 import foam.doris.android.app.database.IOpenGeoSmsSchema;
 import foam.doris.android.app.database.OpenGeoSmsDao;
 import foam.doris.android.app.entities.Category;
 import foam.doris.android.app.entities.Media;
 import foam.doris.android.app.entities.Photo;
 import foam.doris.android.app.entities.Report;
 import foam.doris.android.app.entities.ReportCategory;
 import foam.doris.android.app.models.AddReportModel;
 import foam.doris.android.app.models.ListReportModel;
 import foam.doris.android.app.tasks.GeocoderTask;
 import foam.doris.android.app.util.PhotoUtils;
 import foam.doris.android.app.util.Util;
 import foam.doris.android.app.views.AddReportView;
 import foam.doris.android.app.ui.phone.PictureTaker;
 
 /**
  * @author eyedol
  */
 public class AddReportActivity extends
 		BaseEditMapActivity<AddReportView, AddReportModel> implements
 		OnClickListener, ViewSwitcher.ViewFactory, OnItemClickListener,
 		DialogInterface.OnClickListener {
 
 	private static final int DIALOG_ERROR_NETWORK = 0;
 	private static final int DIALOG_ERROR_SAVING = 1;
 	private static final int DIALOG_CHOOSE_IMAGE_METHOD = 2;
 	private static final int DIALOG_MULTIPLE_CATEGORY = 3;
 	private static final int TIME_DIALOG_ID = 4;
 	private static final int DATE_DIALOG_ID = 5;
 	private static final int DIALOG_SHOW_MESSAGE = 6;
 	private static final int DIALOG_SHOW_REQUIRED = 7;
 	private static final int DIALOG_SHOW_PROMPT = 8;
 	private static final int DIALOG_SHOW_DELETE_PROMPT = 9;
 	private static final int REQUEST_CODE_CAMERA = 0;
 	private static final int REQUEST_CODE_IMAGE = 1;
 	private Calendar mCalendar;
 	private String mDateToSubmit = "";
 	private int mCategoryLength;
 	private Vector<String> mVectorCategories = new Vector<String>();
 	private Vector<String> mCategoriesId = new Vector<String>();
 	private HashMap<String, String> mCategoriesTitle = new HashMap<String, String>();
 	private boolean mError = false;
 	private int id = 0;
 	private UploadPhotoAdapter pendingPhoto;
 	private String mErrorMessage;
 	private String photoName;
 	private AddReportModel model;
 
     private CameraPreview mCameraPreview;
     private PictureTaker mPictureTaker;
 
     private String mLatitude;
     private String mLongitude;
 
     private long mStartTime=0;
     private Boolean mKeyPressed=false;
 
 	public AddReportActivity() {
 		super(AddReportView.class, R.layout.add_report, R.menu.add_report,
 				R.id.location_map);
         Log.i("DORIS","addreportact ctr");
 		model = new AddReportModel();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         Log.i("DORIS","onCreate");
 
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
         mPictureTaker = new PictureTaker();
         mCameraPreview = new CameraPreview(this, mPictureTaker);
         FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
         preview.addView(mCameraPreview);
 
 /*        Button captureButton = (Button) findViewById(R.id.button_capture);
         captureButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // get a file name for the photo to be uploaded
                 photoName = Util.getDateTime() + ".jpg";
                 
                 //keep a copy of the filename for later reuse
                 Preferences.fileName = photoName;
                 Preferences.saveSettings(AddReportActivity.this);
                 
                 Intent intent = new Intent(
                     android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 
 //            Intent intent = new Intent(AddReportActivity.this,
 //              DorisCameraActivity.class);
                 
                 intent.putExtra(MediaStore.EXTRA_OUTPUT, PhotoUtils
                                 .getPhotoUri(photoName,
                                              AddReportActivity.this));
                 startActivityForResult(intent, REQUEST_CODE_CAMERA);
                 //dialog.dismiss();
             }
         });
 */
 
 		mCalendar = Calendar.getInstance();
 		pendingPhoto = new UploadPhotoAdapter(this);
 
 		if (getIntent().getExtras() != null) {
 			this.id = getIntent().getExtras().getInt("id", 0);
 //			view.mIncidentId.setText(""+this.id);
 		}
 		mOgsDao = Database.mOpenGeoSmsDao;
 		// edit existing report
 		if (id > 0) {
 			setSavedReport(id);
 		} else {
 			// add a new report
 			updateDisplay();
 			pendingPhoto.refresh();
 		}
 
 //		registerForContextMenu(view.gallery);
 		createSendMethodDialog();
 
 	}
 
 	@Override
 	protected void onStart() {
         Log.i("DORIS","onStart");
 		super.onStart();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
         mPictureTaker.Shutdown();
         Log.i("DORIS","onPause");
 	}
 
 	@Override
     protected void onStop() {
         super.onPause();
         mPictureTaker.Shutdown();
         Log.i("DORIS","onStop");
      }
 
 	@Override
 	protected void onDestroy() {
         Log.i("DORIS","onDestroy");
         super.onDestroy();
     }
 
 	/**
 	 * Upon being resumed we can retrieve the current state. This allows us to
 	 * update the state if it was changed at any time while paused.
 	 */
 	@Override
 	protected void onResume() {
         Log.i("DORIS","on resume");
 		super.onResume();
 		getSharedText();
 	}
 
 	// Context Menu Stuff
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		new MenuInflater(this).inflate(R.menu.photo_context, menu);
 
 	}
 
 	@Override
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
 				.getMenuInfo();
 		boolean result = performAction(item, info.position);
 
 		if (!result) {
 			result = super.onContextItemSelected(item);
 		}
 
 		return result;
 
 	}
 
 	public boolean performAction(android.view.MenuItem item, int position) {
 
 		if (item.getItemId() == R.id.remove_photo) {
 
 			// adding a new report
 			if (id == 0) {
 
 				// Delete by name
 				if (ImageManager.deletePendingPhoto(this, "/"
 						+ pendingPhoto.getItem(position).getPhoto())) {
 					pendingPhoto.refresh();
 				}
 				return true;
 			} else {
 
 				// editing existing report
 				if (ImageManager.deletePendingPhoto(this, "/"
 						+ pendingPhoto.getItem(position).getPhoto())) {
 
 					pendingPhoto.removeItem(position);
 				}
 				return true;
 			}
 
 		}
 		return false;
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			showDialog();
 			return true;
 
 		} else if (item.getItemId() == R.id.menu_send) {
 			validateReports();
 			return true;
 		} else if (item.getItemId() == R.id.menu_clear) {
 			showDialog(DIALOG_SHOW_PROMPT);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 
 	}
 
 
     // disable volume graphic
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) 
     { 
         if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) return true;
 
         if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) { 
             
             if (!mKeyPressed) {
                 Log.i("DORIS","keydown");
                 mKeyPressed = true;
                 mStartTime = System.currentTimeMillis();
             }
             else
             {
                 long elapsed = System.currentTimeMillis() - mStartTime;
                 if (elapsed>1000) {
                     // increment string id
                     IncString();
                     Log.i("DORIS","TAKING PICTURE -------->");
                     mPictureTaker.TakePicture(mCameraPreview,mPicture);
                     Log.i("DORIS","TAKEN PICTURE <--------");
                     mKeyPressed=false;
                 }
             }
 
             return true;
         } else {
             return super.onKeyDown(keyCode, event); 
         }
     }
 
 	@Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
 
         if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) return true;
 
         if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
 
             if (!mPictureTaker.mTakingPicture && mKeyPressed) {
 
                 mKeyPressed=false;
                 long elapsed = System.currentTimeMillis() - mStartTime;
 
                 Log.i("DORIS","elapsed:"+elapsed);
                 
                 if (elapsed>10) {
 
                     if (elapsed>1000) {
                         return true;
                     } 
                     else
                     {
                         // increment the lobster id
                         IncLobster();
                     }
                     
                     Log.i("DORIS","TAKING PICTURE -------->");
                     mPictureTaker.TakePicture(mCameraPreview,mPicture);
                     Log.i("DORIS","TAKEN PICTURE <--------");
                 }
             }
             return true;
         }
         return false;
     }
 
 	@Override
 	public void onClick(View button) {
		if (button.getId() == R.id.button_capture) {
 			// get a file name for the photo to be uploaded
 			photoName = Util.getDateTime() + ".jpg";
 			
 			//keep a copy of the filename for later reuse
 			Preferences.fileName = photoName;
 			Preferences.saveSettings(AddReportActivity.this);
 //			showDialog(DIALOG_CHOOSE_IMAGE_METHOD);
   
             Intent intent = new Intent(
               android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 
/*            Intent intent = new Intent(AddReportActivity.this,
              DorisCameraActivity.class);*/
 
             intent.putExtra(MediaStore.EXTRA_OUTPUT, PhotoUtils
                             .getPhotoUri(photoName,
                                          AddReportActivity.this));
             startActivityForResult(intent, REQUEST_CODE_CAMERA);
             //dialog.dismiss();
             
 		}
 	}
 
 	private void validateReports() {
 		// STATE_SENT means no change in report fields
 		// only the list of photos can be changed
 		if ( !mIsReportEditable){
 			onClick(mDlgSendMethod, 1);
 			return;
 		}
 		// Dipo Fix
 		mError = false;
 		boolean required = false;
 		// @inoran
 		// validate the title field
 		mErrorMessage = "";
 
         // category not used here (yet)
 		if (mVectorCategories.size() == 0) {
             //	mErrorMessage += getString(R.string.category) + "\n";
 			//required = true;
 
             Log.i("DORIS","adding fake category");
 
             mVectorCategories.add("1");
 		}
 
 		// validate lat long
 		if (TextUtils.isEmpty(mLatitude)) {
 			mErrorMessage += getString(R.string.latitude) + "\n";
 			required = true;
 		} else {
 
 			try {
 				Double.parseDouble(mLatitude);
 			} catch (NumberFormatException ex) {
 				mErrorMessage += getString(R.string.invalid_latitude) + "\n";
 				mError = true;
 			}
 		}
 
 		// validate lat long
 		if (TextUtils.isEmpty(mLongitude)) {
 			mErrorMessage += getString(R.string.longitude) + "\n";
 			mError = true;
 		} else {
 
 			try {
 				Double.parseDouble(mLongitude);
 			} catch (NumberFormatException ex) {
 				mErrorMessage += getString(R.string.invalid_longitude) + "\n";
 				mError = true;
 			}
 		}
 
 
 		if (required) {
 			showDialog(DIALOG_SHOW_REQUIRED);
 		} else if (mError) {
 			showDialog(DIALOG_SHOW_MESSAGE);
 		} else {
 			if ( Preferences.canReceiveOpenGeoSms() ){
 				mDlgSendMethod.show();
 			}else{
 				onClick(mDlgSendMethod, 0);
 			}
 		}
 	}
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		mSendOpenGeoSms = which==1;
 		new SaveTask(this).execute((String) null);
 	}
 	private boolean mSendOpenGeoSms = false;
 	private AlertDialog mDlgSendMethod;
 
 	private void createSendMethodDialog(){
 		Resources r = getResources();
 		String[] items = new String[]{
 			r.getString(R.string.internet),
 			r.getString(R.string.opengeosms)
 		};
 		mDlgSendMethod = new AlertDialog.Builder(this)
 			.setItems(items, this)
 			.setTitle(R.string.send_report_dlg_title)
 			.create();
 	}
 	private OpenGeoSmsDao mOgsDao;
 	/**
 	 * Post to local database
 	 * 
 	 * @author henryaddo
 	 */
 	private boolean addReport() {
 		log("Adding new reports");
 		File[] pendingPhotos = PhotoUtils.getPendingPhotos(this);
 
 		Report report = new Report();
 
         // build title from id numbers
         report.setTitle(GetIDString());
         report.setDescription("no description");
         report.setLatitude(mLatitude);
         report.setLongitude(mLongitude);
         report.setLocationName("no location set");
         report.setReportDate(mDateToSubmit);
 		report.setMode(String.valueOf(0));
 		report.setVerified(String.valueOf(0));
 		report.setPending(1);
 
 		if (id == 0) {
 			// Add a new pending report
 			if (model.addPendingReport(report, mVectorCategories,
                                        pendingPhotos, "")) { //view.mNews.getText().toString())) {
 				// move saved photos
 				log("Moving photos to fetched folder");
 				ImageManager.movePendingPhotos(this);
 				id=report.getDbId();
 			}else{
 				return false;
 			}
 		} else {
 			// Update exisiting report
 			List<Photo> photos = new ArrayList<Photo>();
 			for (int i = 0; i < pendingPhoto.getCount(); i++) {
 				photos.add(pendingPhoto.getItem(i));
 			}
 			if (model.updatePendingReport(id, report, mVectorCategories,
                                           photos, "")) { //view.mNews.getText().toString())) {
 				// move saved photos
 				log("Moving photos to fetched folder");
 				ImageManager.movePendingPhotos(this);
 			}else{
 				return false;
 			}
 		}
 		if ( mSendOpenGeoSms ){
 			mOgsDao.addReport(id);
 		}else{
 			mOgsDao.deleteReport(id);
 		}
 		return true;
 
 	}
 
 	/**
 	 * Edit existing report
 	 * 
 	 * @author henryaddo
 	 */
 	private void setSavedReport(int reportId) {
 
 		// set text part of reports
 		Report report = model.fetchPendingReportById(reportId);
 		if (report != null) {
 			// set date and time
 			setDateAndTime(report.getReportDate());
 		}
 
 		// set Categories.
 		mVectorCategories.clear();
 		for (ReportCategory reportCategory : model
 				.fetchReportCategories(reportId)) {
 			mVectorCategories
 					.add(String.valueOf(reportCategory.getCategoryId()));
 		}
 		
 		setSelectedCategories(mVectorCategories);
 
 		// set the photos
 		pendingPhoto.refresh(id);
 
 		// set news
 		List<Media> newsMedia = model.fetchReportNews(reportId);
 		if (newsMedia != null && newsMedia.size() > 0) {
 			//view.mNews.setText(newsMedia.get(0).getLink());
 		}
 
 	}
 
 	private boolean mIsReportEditable=true;
 
 	private void deleteReport() {
 		// make sure it's an existing report
 		if (id > 0) {
 			if (model.deleteReport(id)) {
 				// delete images
 				for (int i = 0; i < pendingPhoto.getCount(); i++) {
 					ImageManager.deletePendingPhoto(this, "/"
 							+ pendingPhoto.getItem(i).getPhoto());
 				}
 				// return to report listing page.
 				finish();
 			}
 		}
 	}
 
 	/**
 	 * Create various dialog
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_ERROR_NETWORK: {
 			AlertDialog dialog = (new AlertDialog.Builder(this)).create();
 			dialog.setTitle(getString(R.string.network_error));
 			dialog.setMessage(getString(R.string.network_error_msg));
 			dialog.setButton2(getString(R.string.ok),
 					new Dialog.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					});
 			dialog.setCancelable(false);
 			return dialog;
 		}
 		case DIALOG_ERROR_SAVING: {
 			AlertDialog dialog = (new AlertDialog.Builder(this)).create();
 			dialog.setTitle(getString(R.string.network_error));
 			dialog.setMessage(getString(R.string.file_system_error_msg));
 			dialog.setButton2(getString(R.string.ok),
 					new Dialog.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					});
 			dialog.setCancelable(false);
 			return dialog;
 		}
 
 		case DIALOG_CHOOSE_IMAGE_METHOD: {
 
 			AlertDialog dialog = (new AlertDialog.Builder(this)).create();
 			dialog.setTitle(getString(R.string.choose_method));
 			dialog.setMessage(getString(R.string.how_to_select_pic));
 			dialog.setButton(getString(R.string.gallery_option),
 					new Dialog.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							Intent intent = new Intent();
 							intent.setAction(Intent.ACTION_PICK);
 							intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 							startActivityForResult(intent, REQUEST_CODE_IMAGE);
 							dialog.dismiss();
 						}
 					});
 			dialog.setButton2(getString(R.string.cancel),
 					new Dialog.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					});
 			dialog.setButton3(getString(R.string.camera_option),
 					new Dialog.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							Intent intent = new Intent(
 									android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 							intent.putExtra(MediaStore.EXTRA_OUTPUT, PhotoUtils
 									.getPhotoUri(photoName,
 											AddReportActivity.this));
 							startActivityForResult(intent, REQUEST_CODE_CAMERA);
 							dialog.dismiss();
 						}
 					});
 
 			dialog.setCancelable(false);
 			return dialog;
 		}
 
 		case DIALOG_MULTIPLE_CATEGORY: {
 			if (showCategories() != null) {
 				return new AlertDialog.Builder(this)
 						.setTitle(R.string.choose_categories)
 						.setMultiChoiceItems(
 								showCategories(),
 								setCheckedCategories(),
 								new DialogInterface.OnMultiChoiceClickListener() {
 									public void onClick(DialogInterface dialog,
 											int whichButton, boolean isChecked) {
 										// see if categories have previously
 
 										if (isChecked) {
 											mVectorCategories.add(mCategoriesId
 													.get(whichButton));
 
 											mError = false;
 										} else {
 											mVectorCategories
 													.remove(mCategoriesId
 															.get(whichButton));
 										}
 
 										setSelectedCategories(mVectorCategories);
 									}
 								})
 						.setPositiveButton(R.string.ok,
 								new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog,
 											int whichButton) {
 
 										/* User clicked Yes so do some stuff */
 									}
 								}).create();
 			}
 		}
 
 		case TIME_DIALOG_ID:
 			return new TimePickerDialog(this, mTimeSetListener,
 					mCalendar.get(Calendar.HOUR),
 					mCalendar.get(Calendar.MINUTE), false);
 
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, mDateSetListener,
 					mCalendar.get(Calendar.YEAR),
 					mCalendar.get(Calendar.MONTH),
 					mCalendar.get(Calendar.DAY_OF_MONTH));
 
 		case DIALOG_SHOW_MESSAGE:
 			AlertDialog.Builder messageBuilder = new AlertDialog.Builder(this);
 			messageBuilder.setMessage(mErrorMessage).setPositiveButton(
 					getString(R.string.ok),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					});
 
 			AlertDialog showDialog = messageBuilder.create();
 			showDialog.show();
 			break;
 
 		case DIALOG_SHOW_REQUIRED:
 			AlertDialog.Builder requiredBuilder = new AlertDialog.Builder(this);
 			requiredBuilder.setTitle(R.string.required_fields);
 			requiredBuilder.setMessage(mErrorMessage).setPositiveButton(
 					getString(R.string.ok),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
                             finish();
 						}
 					});
 
 			AlertDialog showRequiredDialog = requiredBuilder.create();
 			showRequiredDialog.show();
 			break;
 
 		// prompt for unsaved changes
 		case DIALOG_SHOW_PROMPT: {
 			AlertDialog dialog = (new AlertDialog.Builder(this)).create();
 			dialog.setTitle(getString(R.string.unsaved_changes));
 			dialog.setMessage(getString(R.string.want_to_cancel));
 			dialog.setButton(getString(R.string.no),
 					new Dialog.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 
 							dialog.dismiss();
 						}
 					});
 			dialog.setButton2(getString(R.string.yes),
 					new Dialog.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							new DiscardTask(AddReportActivity.this).execute((String)null);
 							finish();
 							dialog.dismiss();
 						}
 					});
 
 			dialog.setCancelable(false);
 			return dialog;
 		}
 
 		// prompt for report deletion
 		case DIALOG_SHOW_DELETE_PROMPT: {
 			AlertDialog dialog = (new AlertDialog.Builder(this)).create();
 			dialog.setTitle(getString(R.string.delete_report));
 			dialog.setMessage(getString(R.string.want_to_delete));
 			dialog.setButton(getString(R.string.no),
 					new Dialog.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 
 							dialog.dismiss();
 						}
 					});
 			dialog.setButton2(getString(R.string.yes),
 					new Dialog.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							// delete report
 							deleteReport();
 							dialog.dismiss();
 						}
 					});
 
 			dialog.setCancelable(false);
 			return dialog;
 		}
 
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		switch (id) {
 		case TIME_DIALOG_ID:
 			((TimePickerDialog) dialog).updateTime(
 					mCalendar.get(Calendar.HOUR_OF_DAY),
 					mCalendar.get(Calendar.MINUTE));
 			break;
 		case DATE_DIALOG_ID:
 			((DatePickerDialog) dialog).updateDate(
 					mCalendar.get(Calendar.YEAR),
 					mCalendar.get(Calendar.MONTH),
 					mCalendar.get(Calendar.DAY_OF_MONTH));
 			break;
 
 		case DIALOG_MULTIPLE_CATEGORY:
 			final AlertDialog alert = (AlertDialog) dialog;
 			final ListView list = alert.getListView();
 			// been
 			// selected, then uncheck
 			// selected categories
 			if (mVectorCategories.size() > 0) {
 				for (String s : mVectorCategories) {
 					try {
 						// @inoran fix
 						if (list != null) {
 							list.setItemChecked(
 									mCategoryLength - Integer.parseInt(s), true);
 						}
 					} catch (NumberFormatException e) {
 						log("NumberFormatException", e);
 					}
 				}
 			} else {
 				if (list != null) {
 					list.clearChoices();
 				}
 			}
 
 			break;
 
 		}
 	}
 
 	// fetch categories
 	public String[] showCategories() {
 		ListReportModel mListReportModel = new ListReportModel();
 		List<Category> listCategories = mListReportModel.getAllCategories();
 		if (listCategories != null && listCategories.size() > 0) {
 			int categoryCount = listCategories.size();
 			int categoryAmount = 0;
 			if (categoryCount > 0) {
 				categoryAmount = categoryCount;
 			} else {
 				mCategoriesId.clear();
 				mCategoriesTitle.clear();
 				categoryAmount = 1;
 			}
 
 			String categories[] = new String[categoryAmount];
 			mCategoryLength = categories.length;
 
 			int i = 0;
 			for (Category category : mListReportModel.getAllCategories()) {
 
 				categories[i] = category.getCategoryTitle();
 				mCategoriesTitle.put(String.valueOf(category.getCategoryId()),
 						category.getCategoryTitle());
 				mCategoriesId.add(String.valueOf(category.getCategoryId()));
 				i++;
 			}
 			return categories;
 		}
 		return null;
 	}
 
 	private void updateDisplay() {
 		Date date = mCalendar.getTime();
 		if (date != null) {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
 //			view.mPickDate.setText(dateFormat.format(date));
 
 			SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
 //			view.mPickTime.setText(timeFormat.format(date));
 
 			// Because the API doesn't support dates in diff Locale mode, force
 			// it to show time in US
 			SimpleDateFormat submitFormat = new SimpleDateFormat(
 					"yyy-MM-dd kk:mm:ss", Locale.US);
 			mDateToSubmit = submitFormat.format(date);
 		} else {
 //			view.mPickDate.setText(R.string.change_date);
 //			view.mPickTime.setText(R.string.change_time);
 			mDateToSubmit = null;
 		}
 	}
 
 	private void setDateAndTime(String dateTime) {
 
 		if (dateTime != null && !(TextUtils.isEmpty(dateTime))) {
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"yyy-MM-dd kk:mm:ss", Locale.US);
 			Date date;
 			try {
 
 				date = dateFormat.parse(dateTime);
 
 				if (date != null) {
 					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
 							"MMMM dd, yyyy");
 					//view.mPickDate.setText(simpleDateFormat.format(date));
 
 					SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
 					//view.mPickTime.setText(timeFormat.format(date));
 
 					// Because the API doesn't support dates in diff Locale
 					// mode,
 					// force
 					// it to show time in US
 					SimpleDateFormat submitFormat = new SimpleDateFormat(
 							"yyy-MM-dd kk:mm:ss", Locale.US);
 					mDateToSubmit = submitFormat.format(date);
 				} else {
 					//view.mPickDate.setText(R.string.change_date);
 					//view.mPickTime.setText(R.string.change_time);
 					mDateToSubmit = null;
 				}
 
 			} catch (ParseException e) {
 				log(e.getMessage());
 
 			}
 		}
 	}
 
 	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 		public void onDateSet(DatePicker view, int year, int monthOfYear,
 				int dayOfMonth) {
 			mCalendar.set(year, monthOfYear, dayOfMonth);
 			updateDisplay();
 		}
 	};
 
 	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			mCalendar.set(Calendar.MINUTE, minute);
 			updateDisplay();
 		}
 	};
 
 	/**
 	 * Sets the selected categories for submission
 	 * 
 	 * @param aSelectedCategories
 	 */
 	private void setSelectedCategories(Vector<String> aSelectedCategories) {
 		// initilaize categories
 		showCategories();
     }
 
 	/**
 	 * Get check selected categories
 	 * 
 	 * @param aSelectedCategories
 	 */
 	private boolean[] setCheckedCategories() {
 		// FIXME: Look into making this more efficient
 		if (mVectorCategories != null && mVectorCategories.size() > 0) {
 			ListReportModel mListReportModel = new ListReportModel();
 			List<Category> listCategories = mListReportModel
 					.getAllCategories();
 			if (listCategories != null && listCategories.size() > 0) {
 				int categoryCount = listCategories.size();
 				int categoryAmount = 0;
 				if (categoryCount > 0) {
 					categoryAmount = categoryCount;
 				} else {
 					categoryAmount = 1;
 				}
 
 				boolean categories[] = new boolean[categoryAmount];
 				mCategoryLength = categories.length;
 
 				int i = 0;
 				for (Category category : mListReportModel.getAllCategories()) {
 
 					if (mVectorCategories.contains(String.valueOf(category
 							.getCategoryId()))) {
 
 						categories[i] = true;
 					} else {
 						categories[i] = false;
 					}
 
 					i++;
 				}
 				return categories;
 
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Set photo to be attached to an existing report
 	 */
 	private void addPhotoToReport() {
 		File[] pendingPhotos = PhotoUtils.getPendingPhotos(this);
 		if (pendingPhotos != null && pendingPhotos.length > 0) {
 			int id = 0;
 			for (File file : pendingPhotos) {
 				if (file.exists()) {
 					id += 1;
 					Photo photo = new Photo();
 					photo.setDbId(id);
 					photo.setPhoto("pending/" + file.getName());
 					pendingPhoto.addItem(photo);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get shared text from other Android applications
 	 */
 	public void getSharedText() {
 		Intent intent = getIntent();
 		String action = intent.getAction();
 		if (action != null) {
 			if (action.equals(Intent.ACTION_SEND)
 					|| action.equals(Intent.ACTION_CHOOSER)) {
 				CharSequence text = intent
 						.getCharSequenceExtra(Intent.EXTRA_TEXT);
 				if (text != null) {
 //					view.mIncidentDesc.setText(text);
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
         Log.i("DORIS","back from something");
 
 		if (resultCode == RESULT_OK) {
 			//get the saved file name
 			Preferences.loadSettings(AddReportActivity.this);
 			photoName = Preferences.fileName;
 			if (requestCode == REQUEST_CODE_CAMERA) {
 			
                 Log.i("DORIS","back from camera");
 
 				Uri uri = PhotoUtils.getPhotoUri(photoName, this);
 				Bitmap bitmap = PhotoUtils.getCameraPhoto(this, uri);
 				PhotoUtils.savePhoto(this, bitmap, photoName);
 				log(String.format("REQUEST_CODE_CAMERA %dx%d",
 						bitmap.getWidth(), bitmap.getHeight()));
                 
                 // send immediately
                 validateReports();
 
 
 			} else if (requestCode == REQUEST_CODE_IMAGE) {
 				
 				Bitmap bitmap = PhotoUtils
 						.getGalleryPhoto(this, data.getData());
 				PhotoUtils.savePhoto(this, bitmap, photoName);
 				log(String.format("REQUEST_CODE_IMAGE %dx%d",
 						bitmap.getWidth(), bitmap.getHeight()));
 			}
 
 			if (id > 0) {
 				addPhotoToReport();
 			} else {
 				pendingPhoto.refresh();
 			}
 		}
 	}
 
 	@Override
 	protected void locationChanged(double latitude, double longitude) {
 		if ( !mIsReportEditable){
 			return;
 		}
         
         mLatitude=String.valueOf(latitude);
         mLongitude=String.valueOf(longitude);
 	}
 
 	/**
 	 * Go to reports screen
 	 */
 	public void goToReports() {
 		finish();
 	}
 
 	public void onLocationChanged(Location arg0) {
 	}
 
 	public void onProviderDisabled(String provider) {
 	}
 
 	public void onProviderEnabled(String provider) {
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 
 	@Override
 	protected boolean onSaveChanges() {
 		return addReport();
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.widget.ViewSwitcher.ViewFactory#makeView()
 	 */
 	@Override
 	public View makeView() {
 		ImageView i = new ImageView(this);
 		i.setAdjustViewBounds(true);
 		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
 		i.setLayoutParams(new ImageSwitcher.LayoutParams(
 				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
 				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
 
 		return i;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
 	 *      .AdapterView, android.view.View, int, long)
 	 */
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 //		this.view.mSwitcher.setImageDrawable(ImageManager.getPendingDrawables(
 //				this, pendingPhoto.getItem(position).getPhoto(),
 //				Util.getScreenWidth(this)));
 
 	}
 
 	/**
 	 * Delete any existing photo in the pending folder
 	 */
 	private void deleteExistingPhoto() {
 		File[] pendingPhotos = PhotoUtils.getPendingPhotos(this);
 		if (pendingPhotos != null && pendingPhotos.length > 0) {
 			for (File file : pendingPhotos) {
 				if (file.exists()) {
 
 					file.delete();
 				}
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * foam.doris.android.app.activities.BaseEditMapActivity#onDiscardChanges
 	 * ()
 	 */
 	@Override
 	protected boolean onDiscardChanges() {
 		deleteExistingPhoto();
 		return true;
 	}
 
 
     PictureCallback mPicture = new PictureCallback() {
         @Override
         public void onPictureTaken(byte[] data, Camera camera) {
 			// get a file name for the photo to be uploaded
         
             Log.i("DORIS","ON PICTURE TAKEN");
 
             String datetime=Util.getDateTime();
 			photoName = datetime + ".jpg";			
 			String dataName = datetime + ".txt";			
 
 			//keep a copy of the filename for later reuse
 			Preferences.fileName = photoName;
 			Preferences.saveSettings(AddReportActivity.this);
 
             Log.i("DORIS","ON PICTURE TAKEN2");
 
             String bakdata=
                GetIDString()+
                 mLatitude+"\n"+
                 mLongitude+"\n"+
                 datetime;
 
             Log.i("DORIS","ON PICTURE TAKEN3");
 
             Uri uri = PhotoUtils.getPhotoUri(photoName,AddReportActivity.this);
             Uri backup_photo_uri = PhotoUtils.getBackupUri(photoName,AddReportActivity.this);
             Uri backup_text_uri  = PhotoUtils.getBackupUri(dataName,AddReportActivity.this);
 
             SaveData(uri,data);
             SaveData(backup_photo_uri,data);
             SaveData(backup_text_uri,bakdata.getBytes());
 
             Log.i("DORIS","ON PICTURE TAKEN4");
 
             Preferences.saveSettings(AddReportActivity.this);
 
             // send immediately
             validateReports();
 
             Log.i("DORIS","ON PICTURE TAKEN5");
 
             //setResult(RESULT_OK);
             //finish();
         }    };
 
 
     static public String GetIDString() {
         Uri LobsterUri = PhotoUtils.getIDUri("Lobster.txt");
         Uri StringUri = PhotoUtils.getIDUri("String.txt");
 
         return Preferences.firstname+"-"+GetID(StringUri)+"-"+GetID(LobsterUri);
     }
 
     static public void ResetID() {
         Uri LobsterUri = PhotoUtils.getIDUri("Lobster.txt");
         Uri StringUri = PhotoUtils.getIDUri("String.txt");
         SetID(LobsterUri,0);
         SetID(StringUri,0);
     }
 
     static private void IncLobster() {
         Uri LobsterUri = PhotoUtils.getIDUri("Lobster.txt");
         IncID(LobsterUri);
     }
 
     static private void IncString() {
         Uri LobsterUri = PhotoUtils.getIDUri("Lobster.txt");
         Uri StringUri = PhotoUtils.getIDUri("String.txt");
         IncID(StringUri);
         SetID(LobsterUri,1);
     }
 
     static public void SetLobster(int v) {
         Uri LobsterUri = PhotoUtils.getIDUri("Lobster.txt");
         SetID(LobsterUri,v);
     }
 
     static public void SetString(int v) {
         Uri StringUri = PhotoUtils.getIDUri("String.txt");
         SetID(StringUri,v);
     }
 
 
     static private int GetID(Uri uri) {
         String sid=LoadData(uri);
         if (sid=="") {
             Log.i("DORIS","get id firsttime");
             SaveData(uri,"1\0".getBytes());
             return 1;
         }
         else
         {
             Log.i("DORIS","get id found");
             int id=Integer.parseInt(sid);
             return id;
         }
     }
 
     static private void IncID(Uri uri) {
         String sid=LoadData(uri);
         if (sid=="") {
             Log.i("DORIS","get id firsttime");
             SaveData(uri,"1\0".getBytes());
         }
         else
         {
             Log.i("DORIS","get id found");
             int id=Integer.parseInt(sid);
             id++;
             String temp=""+id+"\0";
             SaveData(uri,temp.getBytes());
         }
     }
 
     static private void SetID(Uri uri, int v) {
         Log.i("DORIS","setting id");
         String t=""+v+"\0";
         SaveData(uri,t.getBytes());
     }
 
 
     static private void SaveData(Uri uri, byte[] data) {
         try {
             File file = new File(new URI(uri.toString()));
             
             if (file == null) {
                 return;
             }
             try {
                 FileOutputStream fos = new FileOutputStream(file);
                 fos.write(data);
                 fos.close();
             } catch (FileNotFoundException e) {           
             } catch (IOException e) {
             }
         } catch (Exception e) {
         }
     }
 
     static private String LoadData(Uri uri) {
         try {
             File file = new File(new URI(uri.toString()));
             
             if (file == null) {
                 return "";
             }
             try {
                 FileInputStream fis = new FileInputStream(file);
                 String ret="";
                 StringBuffer fileContent = new StringBuffer("");
                 byte[] buffer = new byte[1024];
                 int length;
                 if ((length = fis.read(buffer)) != -1) {
                     String t=new String(buffer).split("\0")[0]; // hacky null terminator
                     Log.i("DORIS",t);
                     ret=t;
                 }
                 fis.close();
                 return ret;
                 
             } catch (FileNotFoundException e) {           
             } catch (IOException e) {
             }
         } catch (Exception e) {
         }
 
         return "";
     }
 
 
 }
