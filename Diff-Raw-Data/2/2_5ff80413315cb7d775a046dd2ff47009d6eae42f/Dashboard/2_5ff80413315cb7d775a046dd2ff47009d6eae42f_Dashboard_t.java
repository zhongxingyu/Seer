 /*
  * Copyright (C) 2009 Dimagi Inc., UNICEF
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  *
  */
 
 /**
  * 
  */
 package org.rapidandroid.activity;
 
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.rapidandroid.R;
 import org.rapidandroid.content.translation.ModelTranslator;
 import org.rapidandroid.data.SmsDbHelper;
 import org.rapidandroid.data.controller.DashboardDataLayer;
 import org.rapidandroid.data.controller.FormController;
 import org.rapidandroid.data.controller.MessageDataReporter;
 import org.rapidandroid.data.controller.ParsedDataReporter;
 import org.rapidandroid.view.SingleRowHeaderView;
 import org.rapidandroid.view.adapter.FormDataGridCursorAdapter;
 import org.rapidandroid.view.adapter.MessageCursorAdapter;
 import org.rapidandroid.view.adapter.MultipartMessageCursorAdapter;
 import org.rapidandroid.view.adapter.SummaryCursorAdapter;
 import org.rapidsms.java.core.Constants;
 import org.rapidsms.java.core.model.Form;
 import org.rapidsms.java.core.model.Message;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 /**
  * Main entry point activity for RapidAndroid. It is a simple view with a
  * pulldown for for form type, and a listview of messages below that pertain to
  * that message.
  * 
  * @author Daniel Myung dmyung@dimagi.com
  * @created Jan 9, 2009
  * ----------------------------------------------------------------------
  * @author SAGES/pokuam1 - Modifications for Form deletion
  * @author SAGES/pokuam1 - Modifications for Individual Record deletion
  * 
  */
 public class Dashboard extends Activity {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onRestart()
 	 */
 	@Override
 	protected void onRestart() {
 		// TODO Auto-generated method stub
 		super.onRestart();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	private SingleRowHeaderView headerView;
 	private SummaryCursorAdapter summaryView;
 	private FormDataGridCursorAdapter rowView;
 	private MessageCursorAdapter messageCursorAdapter;
 
 	private ViewSwitcher mViewSwitcher;
 	private TableLayout mHeaderTable;
 	private int traceCount = 0;
 
 	// private ProgressDialog mLoadingDialog;
 
 	private Form mChosenForm = null;
 	private boolean mShowAllMessages = false;
 
 	private static final int ACTIVITY_CREATE = 0;
 	private static final int ACTIVITY_FORM_REVIEW = 1;
 	// private static final int ACTIVITY_DATERANGE = 2;
 	private static final int ACTIVITY_CHARTS = 3; // this and ACTIVITY_CHARTS
 	private static final int ACTIVITY_GLOBALSETTINGS = 4;
 
 	private static final int MENU_CREATE_ID = Menu.FIRST;
 	private static final int MENU_FORM_REVIEW_ID = Menu.FIRST + 1;
 	private static final int MENU_CHANGE_DATERANGE = Menu.FIRST + 2;
 	private static final int MENU_CHARTS_ID = Menu.FIRST + 3;
 	private static final int MENU_GLOBAL_SETTINGS = Menu.FIRST + 4;
 	private static final int MENU_ERASE_DATA = Menu.FIRST + 5;
 	private static final int MENU_DELETE_FORM = Menu.FIRST + 6;
 	// private static final int MENU_SHOW_REPORTS = Menu.FIRST + 3;
 	// private static final int MENU_EXIT = Menu.FIRST + 3; //waitaminute, we
 	// don't want to exit this thing, do we?
 
 	private static final String STATE_DATE_START = "startdate";
 	private static final String STATE_DATE_END = "enddate";
 	private static final String STATE_SPINNER_POSITION = "spinneritem";
 	private static final String STATE_SELECTED_FORM = "selectedform";
 	private static final String STATE_LSV_POSITION = "listposition";
 	private static final String STATE_LSV_VIEWMODE = "viewmode";
 	private static final String STATE_RAD_INDEX = "radselected";
 
 	// private static final int CONTEXT_ITEM_SUMMARY_VIEW = Menu.FIRST;
 	// private static final int CONTEXT_ITEM_TABLE_VIEW = Menu.FIRST + 1;
 	// private static final int CONTEXT_ITEM_TEST3 = ContextMenu.FIRST + 2;
 	// private static final int CONTEXT_ITEM_TEST4 = ContextMenu.FIRST + 3;
 
 	private static final int LISTVIEW_MODE_SUMMARY_VIEW = 0;
 	private static final int LISTVIEW_MODE_TABLE_VIEW = 1;
 	// private static final int LISTVIEW_MODE_SUMMARY_VIEW = 0;
 
 	private static final int SHOW_ALL = 5000;
 	private static final CharSequence TXT_WAIT = "Please Wait...";
 
 	private int mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;
 	private ImageButton mBtnViewModeSwitcher;
 
 	private Form[] mAllForms;
 
 	boolean mIsInitializing = false;
 	boolean resetCursor = true;
 	Cursor mListviewCursor = null;
 	ArrayAdapter<String> mSpinnerAdapter;
 	
 	// private Date mStartDate = Constants.NULLDATE;
 	// private Date mEndDate = Constants.NULLDATE;
 
 	final Runnable mToastNotImplemented = new Runnable() {
 		public void run() {
 			alertNotImplemented();
 		}
 	};
 	
 	private void alertNotImplemented() {
 		Toast.makeText(getApplicationContext(), "Not implemented yet!", Toast.LENGTH_SHORT).show();
 	}
 	
 	private int mScreenWidth;
 	private int mListCount = 100;
 	private RadioButton rb100;
 	private RadioButton rb500;
 	private RadioButton rball;
 
 	private OnClickListener radioClickListener = new OnClickListener() {
 
 		public void onClick(View v) {
 			RadioButton buttonView = (RadioButton) v;
 			if (buttonView.equals(rb100)) {
 				mListCount = 100;
 				rb100.setChecked(true);
 				rb500.setChecked(false);
 				rball.setChecked(false);
 			} else if (buttonView.equals(rb500)) {
 				mListCount = 500;
 				rb100.setChecked(false);
 				rb500.setChecked(true);
 				rball.setChecked(false);
 
 			} else if (buttonView.equals(rball)) {
 				mListCount = SHOW_ALL;
 				rb100.setChecked(false);
 				rb500.setChecked(false);
 				rball.setChecked(true);
 
 			}
 			if (!mIsInitializing) {
 				resetCursor = true;
 				beginListViewReload();
 			}
 
 		}
 
 	};
 	private boolean mShowAllMultismsMessages;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		//setTitle();
 		setContentView(R.layout.dashboard);
 		setTitle(R.string.dash_title);
 		this.initFormSpinner();
 		// Set the event listeners for the spinner and the listview
 		Spinner spin_forms = (Spinner) findViewById(R.id.cbx_forms);
 		spin_forms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> parent, View theview, int position, long rowid) {
 				spinnerItemSelected(position);
 			}
 
 			public void onNothingSelected(AdapterView<?> parent) {
 				// blow away the listview's items
 				mChosenForm = null;
 				resetCursor = true;
 				loadListViewWithFormData();
 			}
 		});
 
 		// add some events to the listview
 		ListView lsv = (ListView) findViewById(R.id.lsv_dashboardmessages);
 
 		DisplayMetrics dm = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 		mScreenWidth = dm.widthPixels - 8;
 
 		lsv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 
 		// // bind a context menu
 		// lsv.setOnCreateContextMenuListener(new
 		// View.OnCreateContextMenuListener() {
 		// public void onCreateContextMenu(ContextMenu menu, View v,
 		// ContextMenuInfo menuInfo) {
 		// if (mChosenForm != null) {
 		// menu.add(0, CONTEXT_ITEM_SUMMARY_VIEW, 0, "Summary View");
 		// menu.add(0, CONTEXT_ITEM_TABLE_VIEW, 0, "Table View");
 		// } else {
 		// menu.clear();
 		// }
 		// }
 		// });
 
 		lsv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> adapter, View view, int position, long row) {
 				if (adapter.getAdapter().getClass().equals(SummaryCursorAdapter.class)) {
 					((SummaryCursorAdapter) adapter.getAdapter()).toggle(position);
 				} 
 			}
 		});
 		
 		// SAGES/pokuam1: long click enables deletion of record 
 		lsv.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long row) {
 				if (adapter.getAdapter().getClass().equals(FormDataGridCursorAdapter.class)){
 					
 					Object obj = adapter.getItemAtPosition(position);
 					long objlong = adapter.getItemIdAtPosition(position);
 					obj.toString();
 					
 					SmsDbHelper dbHelper = new SmsDbHelper(Dashboard.this);
 					final SQLiteDatabase db = dbHelper.getWritableDatabase();
 					final String table = "formdata_" + mChosenForm.getPrefix();
 					final String whereClause = "_id = ?";
 					final String[] whereArgs = {String.valueOf(objlong)};
 					
 					AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
 					builder.setMessage(R.string.confirm_delete)
 					       .setCancelable(false)
 					       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 					           public void onClick(DialogInterface dialog, int id) {
 					               //Dashboard.this.finish();
 					               int rows = db.delete(table, whereClause, whereArgs);
 					               mListviewCursor = null;
 					               beginListViewReload();
 					               db.close();
 					           }
 					       })
 					       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 					           public void onClick(DialogInterface dialog, int id) {
 					                dialog.cancel();
 					                db.close();
 					           }
 					       });
 					
 					builder.create().show();
 				}
 				return false;
 			}
 			
 		});
 		rb100 = (RadioButton) findViewById(R.id.dashboard_rad_100);
 		rb100.setOnClickListener(radioClickListener);
 
 		rb500 = (RadioButton) findViewById(R.id.dashboard_rad_500);
 		rb500.setOnClickListener(radioClickListener);
 
 		rball = (RadioButton) findViewById(R.id.dashboard_rad_all);
 		rball.setOnClickListener(radioClickListener);
 
 		rb100.setChecked(true);
 
 		// by default on startup:
 		// mEndDate = new Date();
 		// mStartDate = new Date();
 		// mStartDate.setDate(mEndDate.getDate() - 7);
 
 		mViewSwitcher = (ViewSwitcher) findViewById(R.id.dashboard_switcher);
 
 		mHeaderTable = (TableLayout) findViewById(R.id.dashboard_headertbl);
 		// these animations are too fracking slow
 		// Animation in = AnimationUtils.loadAnimation(this,
 		// android.R.anim.fade_in);
 		// Animation out = AnimationUtils.loadAnimation(this,
 		// android.R.anim.fade_out);
 		// mViewSwitcher.setInAnimation(in);
 		// mViewSwitcher.setOutAnimation(out);
 
 		this.mBtnViewModeSwitcher = (ImageButton) findViewById(R.id.btn_switch_mode);
 		mBtnViewModeSwitcher.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				// this is on click, so we want to toggle it!
 				switch (mFormViewMode) {
 					case LISTVIEW_MODE_SUMMARY_VIEW:
 						mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;
 
 						break;
 					case LISTVIEW_MODE_TABLE_VIEW:
 						mFormViewMode = LISTVIEW_MODE_SUMMARY_VIEW;
 
 						break;
 				}
 				resetCursor = false;
 				beginListViewReload();
 			}
 		});
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		if (mListviewCursor != null){
 			mListviewCursor.close();
 		}
 		super.onStop();
 	}
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 
 		if (savedInstanceState != null) {
 			if (savedInstanceState.containsKey(STATE_SPINNER_POSITION)
 					// && savedInstanceState.containsKey(STATE_LSV_POSITION)
 					&& savedInstanceState.containsKey(STATE_LSV_VIEWMODE)
 					&& savedInstanceState.containsKey(STATE_RAD_INDEX) // savedInstanceState.containsKey(STATE_DATE_START)
 			// &&
 			// savedInstanceState.containsKey(STATE_DATE_END)
 			// STATE_RAD_COUNT
 			// && savedInstanceState.containsKey(STATE_SELECTED_FORM)
 			) {
 
 				// mStartDate.setTime(savedInstanceState.getLong(STATE_DATE_START));
 				// mEndDate.setTime(savedInstanceState.getLong(STATE_DATE_END));
 
 				mIsInitializing = true;
 				int chosenRadio = savedInstanceState.getInt(STATE_RAD_INDEX);
 				if (chosenRadio == 0) {
 					rb100.setChecked(true);
 					this.mListCount = 100;
 				} else if (chosenRadio == 1) {
 					rb500.setChecked(true);
 					this.mListCount = 500;
 				} else if (chosenRadio == 2) {
 					rball.setChecked(true);
 					this.mListCount = 5000;
 				}
 
 				mIsInitializing = false;
 				mFormViewMode = savedInstanceState.getInt(STATE_LSV_VIEWMODE);
 
 				Spinner spin_forms = (Spinner) findViewById(R.id.cbx_forms);
 				spin_forms.setSelection(savedInstanceState.getInt(STATE_SPINNER_POSITION));
 			}
 
 			// String from = savedInstanceState.getString("from");
 			// String body = savedInstanceState.getString("body");
 			// //dialogMessage = "SMS :: " + from + " : " + body;
 			// //showDialog(160);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 		// outState.putLong(STATE_DATE_START, mStartDate.getTime());
 		// outState.putLong(STATE_DATE_END, mEndDate.getTime());
 
 		int chosenRadio = 0;
 
 		if (rb100.isChecked()) {
 			chosenRadio = 0;
 		} else if (rb500.isChecked()) {
 			chosenRadio = 1;
 		} else if (rball.isChecked()) {
 			chosenRadio = 2;
 		}
 		outState.putInt(STATE_RAD_INDEX, chosenRadio);
 		outState.putInt(STATE_LSV_VIEWMODE, mFormViewMode);
 		Spinner spin_forms = (Spinner) findViewById(R.id.cbx_forms);
 		outState.putInt(STATE_SPINNER_POSITION, spin_forms.getSelectedItemPosition());
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 		Bundle extras = null;
 		if (intent != null) {
 			extras = intent.getExtras(); // right now this is a case where we
 			// don't do much activity back and
 			// forth
 		}
 
 		switch (requestCode) {
 			case ACTIVITY_CREATE:
 				// we should do an update of the view
 				initFormSpinner();
 				resetCursor = true;
 				beginListViewReload();
 				break;
 			case ACTIVITY_FORM_REVIEW:
 				// dialogMessage = "Activity Done";
 				// showDialog(12);
 				resetCursor = true;
 				beginListViewReload();
 				break;
 			case ACTIVITY_CHARTS:
 				// dialogMessage = "Activity Done";
 				// showDialog(13);
 				resetCursor = true;
 				beginListViewReload();
 				break;
 			case ACTIVITY_GLOBALSETTINGS:
 				resetCursor = true;
 				beginListViewReload();
 				break;
 			// case ACTIVITY_DATERANGE:
 			// if (extras != null) {
 			// mStartDate = new
 			// Date(extras.getLong(DateRange.ResultParams.RESULT_START_DATE));
 			// mEndDate = new
 			// Date(extras.getLong(DateRange.ResultParams.RESULT_END_DATE));
 			// resetCursor = true;
 			// beginListViewReload();
 			//
 			// }
 			// break;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// add images:
 		// http://developerlife.com/tutorials/?p=304
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, MENU_CREATE_ID, 0, R.string.dashboard_menu_create).setIcon(android.R.drawable.ic_menu_add);
 		menu.add(0, MENU_FORM_REVIEW_ID, 0, R.string.dashboard_menu_edit).setIcon(android.R.drawable.ic_menu_agenda);
 		menu.add(0, MENU_ERASE_DATA, 0, R.string.dashboard_menu_erase_data).setIcon(android.R.drawable.ic_input_delete);
 		// menu.add(0, MENU_CHANGE_DATERANGE, 0,
 		// R.string.chart_menu_change_parameters.setIcon(android.R.drawable.ic_menu_recent_history);
 		menu.add(0, MENU_CHARTS_ID, 0, R.string.dashboard_menu_view).setIcon(android.R.drawable.ic_menu_sort_by_size);
 		menu.add(0, MENU_GLOBAL_SETTINGS, 0, R.string.glb_change_settings).setIcon(android.R.drawable.ic_menu_preferences);
 		menu.add(1, MENU_DELETE_FORM, 0, R.string.dashboard_menu_delete_form).setIcon(android.R.drawable.ic_menu_delete);
 		// menu.add(0, MENU_SHOW_REPORTS, 0,
 		// R.string.dashboard_menu_show_reports);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 			case MENU_CREATE_ID:
 				startActivityFormCreate();
 				return true;
 			case MENU_FORM_REVIEW_ID:
 				startActivityFormReview();
 				return true;
 			case MENU_ERASE_DATA:
 				startDialogEraseData();
 				return true;
 
 				// case MENU_CHANGE_DATERANGE:
 				// startDateRangeActivity();
 				// return true;
 			case MENU_CHARTS_ID:
 				startActivityChart();
 				return true;
 			case MENU_GLOBAL_SETTINGS:
 				startActivityGlobalSettings();
 				return true;
 			// SAGES/pokuam1
 			case MENU_DELETE_FORM:
 				startDialogDeleteForm();
 				return true;
 		}
 		return true;
 	}
 
 	
 
 	/**
 	 * @author SAGES/pokuam1
 	 * 
 	 *  Enables deletion of the form through the UI
 	 */
 	private void startDialogDeleteForm() {
 		if (!ParsedDataReporter.getOldestMessageDate(this, mChosenForm).equals(Constants.NULLDATE)) {
 			Builder noDateDialog = new AlertDialog.Builder(this);
 			noDateDialog.setPositiveButton(R.string.ok, null);
 			noDateDialog.setTitle(R.string.alert);
 			noDateDialog.setMessage("You cannot delete a form containing data or messages");
 			noDateDialog.show();
 		} else {	
 		Builder deleteFormDialog = new AlertDialog.Builder(this);
 		SmsDbHelper dbHelper = new SmsDbHelper(Dashboard.this);
 		final SQLiteDatabase db = dbHelper.getWritableDatabase();
 		final String table = "formdata_" + mChosenForm.getPrefix();
 		final String whereClause = null;
 		final String[] whereArgs = null;
 		final String formPrefix = mChosenForm.getPrefix();
 		deleteFormDialog.setMessage(R.string.confirm_delete)
 	       .setCancelable(false)
 	       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 	           public void onClick(DialogInterface dialog, int id) {
 	        	   FormController.deleteFormByPrefix(Dashboard.this, formPrefix);
 	               //int rows = db.delete(table, whereClause, whereArgs);
 	               //mListviewCursor = null;
 	        	   initFormSpinner();
 	           }
 	       })
 	       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 	           public void onClick(DialogInterface dialog, int id) {
 	                dialog.cancel();
 	           }
 	       });
 	
 		deleteFormDialog.create().show();
 		}
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// Flip the enabled status of menu items depending on selection of a
 		// form
 		super.onPrepareOptionsMenu(menu);
 
 		boolean formOptionsEnabled = false;
 		if (this.mChosenForm != null) {
 			formOptionsEnabled = true;
 		}
 
 		MenuItem editMenu = menu.findItem(MENU_FORM_REVIEW_ID);
 		editMenu.setEnabled(formOptionsEnabled);
 		MenuItem viewMenu = menu.findItem(MENU_CHARTS_ID);
 
 		return true;
 	}
 
 	// @Override
 	// // http://www.anddev.org/tinytutcontextmenu_for_listview-t4019.html
 	// // UGH, things changed from .9 to 1.0
 	// public boolean onContextItemSelected(MenuItem item) {
 	// AdapterView.AdapterContextMenuInfo menuInfo =
 	// (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 	// switch (item.getItemId()) {
 	// case CONTEXT_ITEM_SUMMARY_VIEW:
 	// mFormViewMode = LISTVIEW_MODE_SUMMARY_VIEW;
 	// break;
 	// case CONTEXT_ITEM_TABLE_VIEW:
 	// mFormViewMode = LISTVIEW_MODE_TABLE_VIEW;
 	// break;
 	// default:
 	// return super.onContextItemSelected(item);
 	// }
 	// this.resetCursor = false;
 	// beginListViewReload();
 	// return true;
 	// }
 
 	/**
 	 * @deprecated
 	 */
 	@Deprecated
 	private void startActivityDateRange() {
 		Intent i = new Intent(this, DateRange.class);
 		// Date endDate = java.sql.Date.
 		Date endDate = new Date();
 		if (mChosenForm != null) {
 			endDate = ParsedDataReporter.getOldestMessageDate(this, mChosenForm);
 			if (endDate.equals(Constants.NULLDATE)) {
 				Builder noDateDialog = new AlertDialog.Builder(this);
 				noDateDialog.setPositiveButton("Ok", null);
 				noDateDialog.setTitle("Alert");
 				noDateDialog.setMessage("This form has no messages or data to chart");
 				noDateDialog.show();
 				return;
 			}
 		} else {
 			endDate = MessageDataReporter.getOldestMessageDate(this);
 		}
 		i.putExtra(DateRange.CallParams.ACTIVITY_ARG_STARTDATE, endDate.getTime());
 		// startActivityForResult(i, ACTIVITY_DATERANGE);
 
 	}
 
 	/**
 	 * 
 	 */
 	private void startActivityGlobalSettings() {
 		Intent i;
 		i = new Intent(this, GlobalSettings.class);
 		startActivityForResult(i, ACTIVITY_GLOBALSETTINGS);
 		
 	}
 	
 	// Start the form edit/create activity
 	private void startActivityFormReview() {
 		Intent i;
 		i = new Intent(this, FormReviewer.class);
 		i.putExtra(FormReviewer.CallParams.REVIEW_FORM, mChosenForm.getFormId());
 		startActivityForResult(i, ACTIVITY_FORM_REVIEW);
 	}
 
 	// Start the form edit/create activity
 	private void startActivityFormCreate() {
 		Intent i;
 		i = new Intent(this, FormCreator.class);
 		startActivityForResult(i, ACTIVITY_CREATE);
 	}
 
 	// Start the dialoge erase/delete ALL data for the selected form
 	private void startDialogEraseData(){
 		Builder eraseDataDialog = new AlertDialog.Builder(this);
 		final SmsDbHelper dbHelper = new SmsDbHelper(Dashboard.this);
 		final SQLiteDatabase db = dbHelper.getWritableDatabase();
 		final String table = "formdata_" + mChosenForm.getPrefix();
 		final String whereClause = null;
 		final String[] whereArgs = null;
 		
 		eraseDataDialog.setMessage(R.string.confirm_delete)
 	       .setCancelable(false)
 	       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 	           public void onClick(DialogInterface dialog, int id) {
 	               //Dashboard.this.finish();
 	               int rows = db.delete(table, whereClause, whereArgs);
 	               db.close();
 	               dbHelper.close();
 	               mListviewCursor = null;
 	               beginListViewReload();
 	           }
 	       })
 	       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 	           public void onClick(DialogInterface dialog, int id) {
 	                dialog.cancel();
 	           }
 	       });
 	
 		AlertDialog alert = eraseDataDialog.create();
 		eraseDataDialog.show();
 	}
 	
 	private void startActivityChart() {
 		// Debug.stopMethodTracing();
 		if (mListviewCursor == null) {
 			Builder noDataDialog = new AlertDialog.Builder(this);
 			noDataDialog.setPositiveButton(R.string.ok, null);
 			noDataDialog.setTitle(R.string.alert);
 			noDataDialog.setMessage(R.string.no_data_to_chart);
 			noDataDialog.show();
 			return;
 		}
 
 		Intent i = new Intent(this, ChartData.class);
 		Date now = new Date();
 		i.putExtra(ChartData.CallParams.END_DATE, now.getTime());
 		// we want to chart for a form
 		if (mChosenForm != null && !mShowAllMessages && !mShowAllMultismsMessages) {
 			Date startDate = ParsedDataReporter.getOldestMessageDate(this, mChosenForm);
 			if (startDate.equals(Constants.NULLDATE)) {
 				Builder noDateDialog = new AlertDialog.Builder(this);
 				noDateDialog.setPositiveButton(R.string.ok, null);
 				noDateDialog.setTitle(R.string.alert);
 				noDateDialog.setMessage(R.string.no_msgs_to_chart);
 				noDateDialog.show();
 				return;
 			}
 
 			if (mListviewCursor.getCount() > 0) {
 				mListviewCursor.moveToLast();
 				// int msg_id =
 				// mListviewCursor.getInt(Message.COL_PARSED_MESSAGE_ID);
 				String datestring = mListviewCursor.getString(mListviewCursor.getColumnCount()
 						+ Message.COL_JOINED_MESSAGE_TIME);
 
 				try {
 					startDate = Message.SQLDateFormatter.parse(datestring);
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				// Message m = MessageTranslator.GetMessage(this, msg_id);
 
 			} else {
 				Calendar startCal = Calendar.getInstance();
 				startCal.add(Calendar.DATE, -7);
 				startDate = startCal.getTime();
 			}
 			i.putExtra(ChartData.CallParams.START_DATE, startDate.getTime());
 			i.putExtra(ChartData.CallParams.CHART_FORM, mChosenForm.getFormId());
 		} else if (mShowAllMessages) {
 			// Chart for messages
 			Date startDate = null;
 			boolean setDate = false;
 			if (mListviewCursor.getCount() > 0) {
 				mListviewCursor.moveToLast();
 				try {
 					startDate = Message.SQLDateFormatter.parse(mListviewCursor.getString(Message.COL_TIME));
 				} catch (ParseException e) {
 					setDate = true;
 				}
 			} else {
 				setDate = true;
 			}
 			if (setDate) {
 				Calendar startCal = Calendar.getInstance();
 				startCal.add(Calendar.DATE, -7);
 				startDate = startCal.getTime();
 			}
 
 			mListviewCursor.moveToLast();
 			i.putExtra(ChartData.CallParams.START_DATE, startDate.getTime());
 
 			i.putExtra(ChartData.CallParams.CHART_MESSAGES, true);
 		} else if (mShowAllMultismsMessages){
 			Toast.makeText(getApplicationContext(), getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
 		}
 
 		// i.putExtra(ChartData.CallParams.START_DATE, mStartDate.getTime());
 		// i.putExtra(ChartData.CallParams.END_DATE, mEndDate.getTime());
 		startActivityForResult(i, ACTIVITY_CHARTS);
 	}
 
 	// This is a call to the DB to get all the forms that this form can support.
 	private void initFormSpinner() {
 		// The steps:
 		// get the spinner control from the layouts
 		Spinner spin_forms = (Spinner) findViewById(R.id.cbx_forms);
 		// Get an array of forms from the DB
 		// in the current iteration, it's mForms
 		this.mAllForms = ModelTranslator.getAllForms();
 
 		String[] monitors = new String[mAllForms.length + 2];
 
 		for (int i = 0; i < mAllForms.length; i++) {
 			monitors[i] = getString(R.string.lbl_form) + mAllForms[i].getFormName();
 		}
 
 		// add some special selections:
 		monitors[monitors.length - 2] = getString(R.string.show_all_messages);
 		monitors[monitors.length - 1] = getString(R.string.show_all_multisms_messages);
 		// monitors[monitors.length - 1] = "Show Monitors";
 
 		mSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, monitors);
 		mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		// apply it to the spinner
 		spin_forms.setAdapter(mSpinnerAdapter);
 		
 	}
 
 	private void spinnerItemSelected(int position) {
 		if (position == mAllForms.length) {
 			// if it's forms+1, then it's ALL messages
 			mChosenForm = null;
 			this.mShowAllMessages = true;
 			resetCursor = true;
 			beginListViewReload();
 			// loadListViewWithRawMessages();
 
 		} else if (position == mAllForms.length + 1){
 			// if it's forms+2, then it's ALL multisms messages
 			mChosenForm = null;
 			this.mShowAllMessages = false;
 			this.mShowAllMultismsMessages = true;
 			resetCursor = true;
 			beginListViewReload();
 		} else {
 			this.mShowAllMessages = false;
 			mChosenForm = mAllForms[position];
 			resetCursor = true;
 			beginListViewReload();
 		}
 	}
 
 	private synchronized void finishListViewReload() {
 		if (mListviewCursor == null) {
 			return;
 		}
 		TextView lbl_recents = (TextView) findViewById(R.id.lbl_dashboardmessages);
 
 		lbl_recents.setText(this.mListviewCursor.getCount() + getString(R.string.lbl_messages));
 
 		ListView lsv = (ListView) findViewById(R.id.lsv_dashboardmessages);
 
 		if (mChosenForm != null && !mShowAllMessages) {
 			loadListViewWithFormData();
 		} else if ((mShowAllMessages || mShowAllMultismsMessages) && mChosenForm == null) {
 			this.mBtnViewModeSwitcher.setVisibility(View.INVISIBLE);
 			if (mShowAllMessages){
 				this.messageCursorAdapter = new MessageCursorAdapter(this, mListviewCursor);
 			} else if (mShowAllMultismsMessages){
 //				this.messageCursorAdapter = new MessageCursorAdapter(this, mListviewCursor);
 				this.messageCursorAdapter = new MultipartMessageCursorAdapter(this, mListviewCursor);
 			}
 			lsv.setAdapter(messageCursorAdapter);
 		}
 		// lsv.setVisibility(View.VISIBLE);
 
 		// Debug.stopMethodTracing();
 	}
 
 	final Handler mDashboardHandler = new Handler();
 	final Runnable mUpdateResults = new Runnable() {
 		public void run() {
 			// while(!mIsInitializing) {
 			finishListViewReload();
 			mViewSwitcher.showNext();
 			// }
 		}
 	};
 
 	private synchronized void beginListViewReload() {
 		// Debug.startMethodTracing("listview_load" + traceCount++);
 		switch (mFormViewMode) {
 			case LISTVIEW_MODE_SUMMARY_VIEW:
 				mBtnViewModeSwitcher.setImageResource(R.drawable.summaryview);
 				break;
 			case LISTVIEW_MODE_TABLE_VIEW:
 				mBtnViewModeSwitcher.setImageResource(R.drawable.gridview);
 				break;
 		}
 
 		this.mIsInitializing = true;
 		TextView lbl_recents = (TextView) findViewById(R.id.lbl_dashboardmessages);
 		lbl_recents.setText(TXT_WAIT);
 		mViewSwitcher.showNext();
 		resetListAdapters();
 		new Thread(new Runnable() {
 			public void run() {
 				fillCursorInBackground();
 				mIsInitializing = false;
 				// finishListViewReload();//might puke
 				mDashboardHandler.post(mUpdateResults);
 			}
 		}).start();
 	}
 
 	private synchronized void fillCursorInBackground() {
 		if (mListviewCursor == null) {
 			if (mChosenForm != null && !mShowAllMessages) {
 				mListviewCursor = DashboardDataLayer.getCursorForFormData(this, mChosenForm, mListCount);
 			} else if (mShowAllMessages && mChosenForm == null) {
 				mListviewCursor = DashboardDataLayer.getCursorForRawMessages(this, mListCount);
 			} else if (mShowAllMultismsMessages && mChosenForm == null){
 				mListviewCursor = DashboardDataLayer.getCursorForMultipartMessages(this, mListCount);
 				this.runOnUiThread(mToastNotImplemented);		
 				
 			}
 		} else {
 			Log.d("Dashboard","mListviewCursor wasn't null....");
 		}
 	}
 
 	// this is a call to the DB to update the ListView with the messages for a
 	// selected form
 	private void loadListViewWithFormData() {
 		ListView lsv = (ListView) findViewById(R.id.lsv_dashboardmessages);
 		this.mBtnViewModeSwitcher.setVisibility(View.VISIBLE);
 		if (mChosenForm == null) {
 			lsv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
 													new String[] { "Select an item" }));
 		} else {
 
 			if (mListviewCursor.getCount() == 0) {
 				lsv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
														new String[] { getText(R.string.prompt_graph_no_data).toString() }));
 				return;
 			}
 
 			/*
 			 * if we want to get super fancy, we can do a join to make it all
 			 * accessible in one cursor instead of having to requery select
 			 * formdata_bednets.,
 			 * rapidandroid_message.message,rapidandroid_message.time from
 			 * formdata_bednets join rapidandroid_message on
 			 * (formdata_bednets.message_id = rapidandroid_message._id)
 			 */
 
 			if (this.mFormViewMode == Dashboard.LISTVIEW_MODE_SUMMARY_VIEW) {
 				this.summaryView = new SummaryCursorAdapter(this, mListviewCursor, mChosenForm);
 				lsv.setAdapter(summaryView);
 			} else if (this.mFormViewMode == Dashboard.LISTVIEW_MODE_TABLE_VIEW) {
 				if (this.headerView == null) {
 					headerView = new SingleRowHeaderView(this, mChosenForm, mScreenWidth);
 					mHeaderTable.addView(headerView);
 					int colcount = headerView.getColCount();
 					for (int i = 0; i < colcount; i++) {
 						mHeaderTable.setColumnShrinkable(i, true);
 					}
 				}
 				rowView = new FormDataGridCursorAdapter(this, mChosenForm, mListviewCursor, mScreenWidth);
 				lsv.setAdapter(rowView);
 			}
 		}
 
 	}
 
 	/**
 	 * @param changedforms
 	 */
 	private void resetListAdapters() {
 		ListView lsv = (ListView) findViewById(R.id.lsv_dashboardmessages);
 
 		if (this.headerView != null) {
 			mHeaderTable.removeAllViews();
 			headerView = null;
 		}
 
 		if (rowView != null) {
 
 			rowView = null;
 		}
 		if (summaryView != null) {
 			summaryView = null;
 		}
 		if (messageCursorAdapter != null) {
 			messageCursorAdapter = null;
 		}
 		// monitorCursorAdapter
 
 		if (resetCursor) {
 			// need to reset the cursor
 			if (mListviewCursor != null) {
 				mListviewCursor.close();
 				mListviewCursor = null;
 			}
 			resetCursor = false;
 		}
 	}
 
 }
