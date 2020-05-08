 package com.edwise.dedicamns;
 
 import java.io.Serializable;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.ActionMode;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.edwise.dedicamns.adapters.DayListAdapter;
 import com.edwise.dedicamns.asynctasks.AppData;
 import com.edwise.dedicamns.asynctasks.MonthListAsyncTask;
 import com.edwise.dedicamns.beans.ActivityDay;
 import com.edwise.dedicamns.beans.DayRecord;
 import com.edwise.dedicamns.beans.MonthListBean;
 import com.edwise.dedicamns.beans.MonthReportBean;
 import com.edwise.dedicamns.connections.ConnectionException;
 import com.edwise.dedicamns.connections.ConnectionFacade;
 import com.edwise.dedicamns.connections.WebConnection;
 import com.edwise.dedicamns.menu.MenuUtils;
 import com.edwise.dedicamns.utils.ErrorUtils;
 
 public class MonthViewActivity extends Activity {
 	private static final String LOGTAG = MonthViewActivity.class.toString();
 
 	final static int DAY_REQUEST = 0;
 
 	private ListView listView = null;
 	private MonthListBean monthList = null;
 	private Parcelable listState = null;
 	private Spinner monthSpinner = null;
 	private Spinner yearSpinner = null;
 
 	private ProgressDialog pDialogRemovingDay = null;
 	private ProgressDialog pDialogCopingDay = null;
 	private ProgressDialog pDialogMonthCharge = null;
 	private ProgressDialog pDialogReportCharge = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.d(LOGTAG, "onCreate...");
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.month_view);
 		AppData.setCurrentActivity(this);
 
 		if (savedInstanceState != null) {
 			restoreFromChangeOrientation(savedInstanceState);
 		} else {
 			monthList = (MonthListBean) getIntent().getSerializableExtra("monthList");
 		}
 
 		List<String> listMonths = ConnectionFacade.getWebConnection().getMonths();
 		List<String> listYears = ConnectionFacade.getWebConnection().getYears();
 		linkMonthSpinner(listMonths);
 		linkYearsSpinner(listYears);
 
 		this.markMonthYearSelected(monthList.getMonthName(), monthList.getYear());
 
 		listView = (ListView) findViewById(R.id.listV_main);
 		initListView();
 	}
 
 	private void restoreFromChangeOrientation(Bundle savedInstanceState) {
 		if (savedInstanceState.getBoolean("pDialogRemovingDayON")) {
 			showDialogRemovingDay();
 		} else if (savedInstanceState.getBoolean("pDialogCopingDayON")) {
 			showDialogCopingDay();
 		} else if (savedInstanceState.getBoolean("pDialogMonthChargeON")) { //
 			showDialogMonthCharge(getString(R.string.msgGettingMonthData));
 		} else if (savedInstanceState.getBoolean("pDialogReportChargeON")) {
 			showDialogReportCharge(getString(R.string.msgGettingReportData));
 		} 
 		listState = savedInstanceState.getParcelable("listState");
 		monthList = (MonthListBean) savedInstanceState.getSerializable("monthList");
 		dayToCopy = (DayRecord) savedInstanceState.getSerializable("dayToCopy");
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		AppData.setCurrentActivity(this);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		Log.d(LOGTAG, "onSaveInstanceState");
 
 		if (pDialogRemovingDay != null) {
 			pDialogRemovingDay.cancel();
 			pDialogRemovingDay = null;
 			outState.putBoolean("pDialogRemovingDayON", true);
 		} else if (pDialogCopingDay != null) {
 			pDialogCopingDay.cancel();
 			pDialogCopingDay = null;
 			outState.putBoolean("pDialogCopingDayON", true);
 		} else if (pDialogMonthCharge != null) {
 			pDialogMonthCharge.cancel();
 			outState.putBoolean("pDialogMonthChargeON", true);
 		} else if (pDialogReportCharge != null) {
 			pDialogReportCharge.cancel();
 			outState.putBoolean("pDialogReportChargeON", true);
 		} 
 		outState.putSerializable("monthList", monthList);
 		if (listState != null) {
 			outState.putParcelable("listState", listState);
 		}
 		outState.putSerializable("dayToCopy", dayToCopy);
 
 		super.onSaveInstanceState(outState);
 	}
 
 	public void closeDialog() {
 		if (pDialogRemovingDay != null) {
 			pDialogRemovingDay.dismiss();
 			pDialogRemovingDay = null;
 		} else if (pDialogCopingDay != null) {
 			pDialogCopingDay.dismiss();
 			pDialogCopingDay = null;
 		} else if (pDialogMonthCharge != null) {
 			pDialogMonthCharge.dismiss();
 			pDialogMonthCharge = null;
 		} else if (pDialogReportCharge != null) {
 			pDialogReportCharge.dismiss();
 			pDialogReportCharge = null;
 		} 		
 	}
 
 	private void initListView() {
 		listView.setAdapter(new DayListAdapter(this, monthList.getListDays()));
 		
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 				listState = listView.onSaveInstanceState();
 
 				DayRecord dayRecord = (DayRecord) listView.getItemAtPosition(position);
 				Intent intent = new Intent(MonthViewActivity.this, GeneralDetailDayActivity.class);
 				intent.putExtra("dayRecord", dayRecord);
 				startActivityForResult(intent, DAY_REQUEST);
 			}
 
 		});
 
 		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 				Log.d(LOGTAG, "Long click");				
 				listState = listView.onSaveInstanceState();
 				
 				((DayListAdapter)listView.getAdapter()).setCurrentSelection(position);		
 				startActionMode(mActionModeCallback);
 				view.setSelected(true);
 				
 				return true;
 			}
 		});
 	}
 
 	private void linkMonthSpinner(List<String> listMonths) {
 		monthSpinner = (Spinner) findViewById(R.id.listMonthsSpinner);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listMonths);
 		monthSpinner.setAdapter(adapter);
 		monthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 			}
 
 			public void onNothingSelected(AdapterView<?> parent) {
 			}
 
 		});
 	}
 
 	private void linkYearsSpinner(List<String> listYears) {
 		yearSpinner = (Spinner) findViewById(R.id.listYearsSpinner);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listYears);
 		yearSpinner.setAdapter(adapter);
 		yearSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 			}
 
 			public void onNothingSelected(AdapterView<?> parent) {
 			}
 
 		});
 	}
 
 	@SuppressWarnings("unchecked")
 	private void markMonthYearSelected(String month, String year) {
 		ArrayAdapter<String> yearSpinnerAdapter = (ArrayAdapter<String>) yearSpinner.getAdapter();
 		int spinnerPosition = yearSpinnerAdapter.getPosition(year);
 		yearSpinner.setSelection(spinnerPosition);
 
 		ArrayAdapter<String> monthSpinnerAdapter = (ArrayAdapter<String>) monthSpinner.getAdapter();
 		spinnerPosition = monthSpinnerAdapter.getPosition(month);
 		monthSpinner.setSelection(spinnerPosition);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		boolean returned = false;
 		switch (item.getItemId()) {
 		case R.id.menu_logout:
 			MenuUtils.doLogout(this);
 			returned = true;
 			break;
 		case R.id.menu_about_us:
 			MenuUtils.goToAbout(this);
 			returned = true;
 			break;
 		default:
 			returned = super.onOptionsItemSelected(item);
 		}
 
 		return returned;
 	}
 
 	public void doUpdateList(View view) {
 		Log.d(LOGTAG, "doUpdateList: Click en actualizar...");
 		showDialogMonthCharge(getString(R.string.msgGettingMonthData));
 
 		Integer month = this.monthSpinner.getSelectedItemPosition() + 1;
 		Integer year = Integer.valueOf((String) this.yearSpinner.getSelectedItem());
 
 		AsyncTask<Integer, Integer, Integer> monthListAsyncTask = new MonthListAsyncTask();
 		monthListAsyncTask.execute(new Integer[] { month, year, MonthListAsyncTask.IS_UPDATE_LIST });
 	}
 
 	public void doShowReportMonth(View view) {
 		Log.d(LOGTAG, "doShowReportMonth: Click en mostrar informe mensual...");
 
 		showDialogReportCharge(getString(R.string.msgGettingReportData));
 		AsyncTask<Integer, Integer, Integer> monthReportAsyncTask = new MonthReportAsyncTask();
 		monthReportAsyncTask.execute(1);
 	}
 
 	private void showDialogMonthCharge(String message) {
 		this.pDialogMonthCharge = ProgressDialog.show(this, message, getString(R.string.msgPleaseWait), true);
 	}
 
 	private void showDialogReportCharge(String message) {
 		this.pDialogReportCharge = ProgressDialog.show(this, message, getString(R.string.msgPleaseWait), true);
 	}
 	
 	private void showDialogRemovingDay() {
 		this.pDialogRemovingDay = ProgressDialog.show(this, getString(R.string.msgRemovingDay),
 				getString(R.string.msgPleaseWait), true);
 	}
 	
 	private void showDialogCopingDay() {
 		this.pDialogCopingDay = ProgressDialog.show(this, getString(R.string.msgCopingDay),
 				getString(R.string.msgPleaseWait), true);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d(LOGTAG, "onActivityResult...");
 
 		if (requestCode == DAY_REQUEST) {
 			if (resultCode == RESULT_OK) {
 				DayRecord dayRecord = (DayRecord) data.getSerializableExtra("dayRecordModif");
 				this.reDrawList(dayRecord);
 			}
 		}
 	}
 
 	private void reDrawList(DayRecord dayRecord) {
 		// Repintado de toda la lista, cambiando el dia que ha cambiado
 		changeDayRecordInList(dayRecord);
 		listView.setAdapter(new DayListAdapter(this, monthList.getListDays()));
 		listView.onRestoreInstanceState(listState);
 
 		Log.d(LOGTAG, "changeDataList: done");
 	}
 
 	private void changeDayRecordInList(DayRecord dayRecord) {
 		for (DayRecord oldDayRecord : monthList.getListDays()) {
 			if (oldDayRecord.getDayNum() == dayRecord.getDayNum()) {
 				if (dayRecord.getActivities().size() == 0) {
 					oldDayRecord.clearDay();
 				} else {
 					oldDayRecord.copyDayData(dayRecord);
 				}
 				break;
 			}
 		}
 	}
 
 	public void updateList(MonthListBean monthListBean) {
 		this.monthList = monthListBean;
 		initListView();
 	}
 	
 	private void showToastMessage(String message) {
 		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
 		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 20);
 		toast.show();
 	}
 	
 	private void removeDay(DayRecord selected) {
 		showDialogRemovingDay();
 		AsyncTask<DayRecord, Integer, Integer> removeDayAsyncTask = new RemoveDayRecordsAsyncTask();
 		removeDayAsyncTask.execute(selected);
 	}
 	
 	private void copyDay(DayRecord selected) {
 		showDialogCopingDay();
 		AsyncTask<DayRecord, Integer, Integer> copyDayAsyncTask = new CopyDayRecordsAsyncTask();
 		copyDayAsyncTask.execute(selected);
 	}
 
 	private class MonthReportAsyncTask extends AsyncTask<Integer, Integer, Integer> {
 
 		private MonthReportBean monthReport = null;
 
 		@Override
 		protected Integer doInBackground(Integer... params) {
 			Log.d(MonthReportAsyncTask.class.toString(), "doInBackground...");
 
 			WebConnection webConnection = ConnectionFacade.getWebConnection();
 			Integer result = 1;
 			try {
 				monthReport = webConnection.getMonthReport();
 			} catch (ConnectionException e) {
 				Log.e(LOGTAG, "Error al obtener el informe de horas", e);
 				result = -1;
 			}
 
 			return result;
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			Log.d(LOGTAG, "onPostExecute...");
 			super.onPostExecute(result);
 
 			if (result == 1) {
 				this.launchMonthReportActivity();
 				this.closeDialog();
 			} else {
 				this.closeDialog();
 				showToastMessage(getString(R.string.msgWebError));
 			}
 		}
 
 		private void closeDialog() {
 			MonthViewActivity activity = (MonthViewActivity) AppData.getCurrentActivity();
 			activity.closeDialog();
 		}
 
 		private void launchMonthReportActivity() {
 			Intent intent = new Intent(AppData.getCurrentActivity(), MonthReportActivity.class);
 			intent.putExtra("monthReport", (Serializable) monthReport);
 
 			AppData.getCurrentActivity().startActivity(intent);
 		}		
 
 	}
 	
 	private class RemoveDayRecordsAsyncTask extends AsyncTask<DayRecord, Integer, Integer> {
 
 		private DayRecord dayToRemove = null;
 		
 		@Override
 		protected Integer doInBackground(DayRecord... param) {
 			Log.d(RemoveDayRecordsAsyncTask.class.toString(), "doInBackground...");
 			WebConnection webConnection = ConnectionFacade.getWebConnection();
 
 			this.dayToRemove = param[0];			
 			Integer result = 1;
 			for (ActivityDay activityDay : dayToRemove.getActivities()) {
 				try {
 					result = webConnection.removeDay(activityDay);
 				} catch (ConnectionException e) {
 					Log.e(LOGTAG, "Error al borrar datos de una actividad", e);
 					result = -2;
 				}
 
 				if (result != 1) {
 					break;
 				}
 
 			}		
 			this.dayToRemove.clearDay();
 
 			return result;
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			Log.d(RemoveDayRecordsAsyncTask.class.toString(), "onPostExecute...");
 
 			this.closeDialog();			
 			if (result == 1) {
 				reDrawList(this.dayToRemove);
 				showToastMessage(getString(R.string.msgRemoveOK));				
 			} else {
 				showToastMessage(ErrorUtils.getMessageError(result));
 			}
 		}
 		
 		private void closeDialog() {
 			MonthViewActivity activity = (MonthViewActivity) AppData.getCurrentActivity();
 			activity.closeDialog();			
 		}
 	}
 	
 	private class CopyDayRecordsAsyncTask extends AsyncTask<DayRecord, Integer, Integer> {
 
 		private DayRecord dayToBeOverwrited = null;
 		
 		@Override
 		protected Integer doInBackground(DayRecord... param) {
 			Log.d(CopyDayRecordsAsyncTask.class.toString(), "doInBackground...");
 			WebConnection webConnection = ConnectionFacade.getWebConnection();
 			this.dayToBeOverwrited = param[0];			
 			
 			// Borramos primero el día a machacar
 			Integer result = removeDayRecord(webConnection);
 			if (result == 1) {
 				result = copyDayRecord(webConnection);
 			}
 
 			return result;
 		}
 
 		private Integer removeDayRecord(WebConnection webConnection) {
 			Integer result = 1;
 			for (ActivityDay activityDay : dayToBeOverwrited.getActivities()) {
 				try {
 					result = webConnection.removeDay(activityDay);
 				} catch (ConnectionException e) {
 					Log.e(LOGTAG, "Error al borrar datos de una actividad", e);
 					result = -2;
 				}
 
 				if (result != 1) {
 					break;
 				}
 
 			}		
 			this.dayToBeOverwrited.clearDay();
 			
 			return result;
 		}
 		
 		private Integer copyDayRecord(WebConnection webConnection) {
 			Integer result = 1;
 			
 			this.dayToBeOverwrited.copyDayDataCloned(dayToCopy);
 			try {
 				result = webConnection.saveDayBatch(this.dayToBeOverwrited, false);
 			} catch (ConnectionException e) {
 				Log.e(LOGTAG, "Error al copiar datos de un día a otro", e);
 				result = -2;
 			}
 			
			setActivitiesAsToUpdate();
			
 			return result;
 		}
 
		private void setActivitiesAsToUpdate() {
			for(ActivityDay activityDay: this.dayToBeOverwrited.getActivities()) {
				activityDay.setUpdate(true);
			}
		}

 		@Override
 		protected void onPostExecute(Integer result) {
 			Log.d(LOGTAG, "onPostExecute...");
 
 			this.closeDialog();			
 			if (result == 1) {
 				reDrawList(this.dayToBeOverwrited);
 				showToastMessage(getString(R.string.msgCopiedOK));				
 			} else {
 				showToastMessage(ErrorUtils.getMessageError(result));
 			}
 		}
 		
 		private void closeDialog() {
 			MonthViewActivity activity = (MonthViewActivity) AppData.getCurrentActivity();
 			activity.closeDialog();			
 		}
 	}
 	
 	private DayRecord dayToCopy = null;
 	
 	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
 		
 		
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			Log.d(LOGTAG, "onCreateActionMode...");
 			MenuInflater inflater = mode.getMenuInflater();
 			mode.setTitle(getString(R.string.optionsMenu));
 			inflater.inflate(R.menu.month_context_menu, menu);
 			return true;
 		}
 
 		@Override
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			return false;
 		}
 
 		@Override
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			Log.d(LOGTAG, "onActionItemClicked...");
 			DayRecord selected = (DayRecord) listView.getItemAtPosition(((DayListAdapter) listView.getAdapter())
 					.getCurrentSelection());
 			switch (item.getItemId()) {
 			case R.id.copy:
 				dayToCopy = selected;
 				unselectItem();
 				mode.finish(); 
 				return true;
 			case R.id.paste:
 				if (dayToCopy == null || dayToCopy.getActivities().size() == 0) {
 					showToastMessage(getString(R.string.msgDayNotCopied));
 				}
 				else {
 					if (selected.equals(dayToCopy)) {
 						showToastMessage(getString(R.string.msgDayEqualToCopied));
 					}
 					else {
 						copyDay(selected);
 					}
 				}
 				
 				unselectItem();
 				mode.finish(); 
 				return true;
 			case R.id.delete:
 				if (selected.getActivities().size() == 0) {
 					showToastMessage(getString(R.string.msgDayYetEmpty));
 				}
 				else {
 					removeDay(selected);
 				}
 				
 				unselectItem();
 				mode.finish(); 				
 				return true;
 			default:
 				return false;
 			}
 		}		
 
 		@Override
 		public void onDestroyActionMode(ActionMode mode) {
 			Log.d(LOGTAG, "onDestroyActionMode...");	
 		}
 		
 		private void unselectItem() {
 			listView.clearChoices(); 
 			listView.requestLayout();
 		}
 		
 		
 	};
 }
 
