 package com.edwise.dedicamns;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.edwise.dedicamns.adapters.ActivityListAdapter;
 import com.edwise.dedicamns.asynctasks.AppData;
 import com.edwise.dedicamns.beans.ActivityDay;
 import com.edwise.dedicamns.beans.DayRecord;
 import com.edwise.dedicamns.connections.ConnectionException;
 import com.edwise.dedicamns.connections.ConnectionFacade;
 import com.edwise.dedicamns.connections.WebConnection;
 import com.edwise.dedicamns.menu.MenuUtils;
 import com.edwise.dedicamns.utils.DayUtils;
 import com.edwise.dedicamns.utils.ErrorUtils;
 
 public class GeneralDetailDayActivity extends Activity {
     private static final String LOGTAG = GeneralDetailDayActivity.class.toString();
 
     final static int ACTIVITY_REQUEST = 0;
 
     private DayRecord dayRecord = null;
     private boolean dayModif = false;
 
     private TextView gDayInfoTextView = null;
     private TextView gTotalHoursTextView = null;
     private ListView listView = null;
 
     private Button removeAllButton = null;
 
     private ProgressDialog pDialog = null;
     private AlertDialog alertDialog = null;
     private boolean alertDialogActive = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.general_detail_day);
 	Log.d(LOGTAG, "onCreate");
 	AppData.setCurrentActivity(this);
 
	restoreDialogs(savedInstanceState);
 
 	this.dayRecord = (DayRecord) getIntent().getSerializableExtra("dayRecord");
	this.dayModif = false;
 	if (dayRecord != null) {
 	    removeAllButton = (Button) findViewById(R.id.generalRemoveAllButton);
 	    gDayInfoTextView = (TextView) findViewById(R.id.gDayInfoTextView);
 	    gDayInfoTextView.setText(dayRecord.getDayNum() + " - " + dayRecord.getDayName());
 	    gTotalHoursTextView = (TextView) findViewById(R.id.gTotalHoursTextView);
 	    gTotalHoursTextView.setText(dayRecord.getHours());
 	    listView = (ListView) findViewById(R.id.gListActivities);
 
 	    showORhideRemoveAllButton();
 	    initListView();
 	} else {
 	    Log.e(LOGTAG, "DayRecord ha venido nulo");
 	    throw new UnsupportedOperationException("DayRecord ha venido nulo!");
 	}
     }
 
    private void restoreDialogs(Bundle savedInstanceState) {
	if (savedInstanceState != null && savedInstanceState.getBoolean("pDialogON")) {
	    showDialog();
	} else if (savedInstanceState != null && savedInstanceState.getBoolean("alertDialogON")) {
	    launchRemoveProcessWithAlertDialog();
 	}

     }
 
     @Override
     protected void onRestart() {
 	super.onRestart();
 	AppData.setCurrentActivity(this);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
 	Log.d(LOGTAG, "onSaveInstanceState");
 	if (pDialog != null) {
 	    pDialog.cancel();
 	    outState.putBoolean("pDialogON", true);
 	} else if (alertDialogActive) {
 	    alertDialog.dismiss();
 	    outState.putBoolean("alertDialogON", true);
 	}
 	super.onSaveInstanceState(outState);
     }
 
     public void closeDialog() {
 	if (pDialog != null) {
 	    pDialog.dismiss();
 	    pDialog = null;
 	}
     }
 
     private void showORhideRemoveAllButton() {
 	if (this.dayRecord.isEmptyDay()) {
 	    removeAllButton.setVisibility(View.GONE);
 	} else if (removeAllButton.getVisibility() == View.GONE) {
 	    removeAllButton.setVisibility(View.VISIBLE);
 	}
     }
 
     private void initListView() {
 	listView.setAdapter(new ActivityListAdapter(this, dayRecord.getActivities()));
 
 	listView.setOnItemClickListener(new OnItemClickListener() {
 	    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 		ActivityDay activityDay = (ActivityDay) listView.getItemAtPosition(position);
 		launchDetailDayActivity(activityDay);
 	    }
 
 	});
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
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	Log.d(LOGTAG, "onActivityResult...");
 
 	if (requestCode == ACTIVITY_REQUEST) {
 	    if (resultCode == RESULT_OK) {
 		this.dayModif = true;
 		ActivityDay activityDay = (ActivityDay) data.getSerializableExtra("activityModif");
 		this.reDrawListActivities(activityDay);
 		this.recalculateTotalHours();
 		showORhideRemoveAllButton();
 	    }
 	}
     }
 
     private void reDrawListActivities(ActivityDay activityDay) {
 	// Repintado de toda la lista, cambiando la activity que ha cambiado
 	changeActivityInList(activityDay);
 	listView.setAdapter(new ActivityListAdapter(this, dayRecord.getActivities()));
 
 	Log.d(LOGTAG, "changeActivityDataList: done");
     }
 
     private void recalculateTotalHours() {
 	String totalHours = "00:00";
 	for (ActivityDay activityDay : dayRecord.getActivities()) {
 	    totalHours = DayUtils.addHours(totalHours, activityDay.getHours());
 	}
 	dayRecord.setHours(totalHours);
 	gTotalHoursTextView.setText(totalHours);
     }
 
     private void emptyListAndRedraw() {
 	dayRecord.getActivities().clear();
 	listView.setAdapter(new ActivityListAdapter(this, dayRecord.getActivities()));
 	this.recalculateTotalHours();
     }
 
     private void changeActivityInList(ActivityDay activityDay) {
 	if (activityDay.isUpdate()) {
 	    for (ActivityDay oldActivityDay : dayRecord.getActivities()) {
 		if (oldActivityDay.getIdActivity().equals(activityDay.getIdActivity())) {
 		    if (activityDay.isToRemove()) {
 			dayRecord.getActivities().remove(oldActivityDay);
 			oldActivityDay = null;
 		    } else { // Es modificada
 			oldActivityDay.copyActivityDayData(activityDay);
 		    }
 		    break;
 		}
 	    }
 	} else { // Es nueva, la añadimos a la lista
 	    dayRecord.getActivities().add(activityDay);
 	    activityDay.setUpdate(true); // Para si luego se modifica
 	}
     }
 
     public void doAddActivity(View view) {
 	Log.d(LOGTAG, "doAddActivity...");
 	this.launchDetailDayActivity(new ActivityDay());
     }
 
     public void doRemoveAllActivities(View view) {
 	Log.d(LOGTAG, "doRemoveAllActivities...");
 	launchRemoveProcessWithAlertDialog();
     }
 
     private void showDialog() {
 	pDialog = ProgressDialog.show(this, getString(R.string.msgRemovingActivities),
 		getString(R.string.msgPleaseWait), true);
     }
 
     @Override
     public void onBackPressed() {
 	Log.d(LOGTAG, "onBackPressed");
 
 	Intent returnIntent = new Intent();
 	if (dayModif) {
 	    returnIntent.putExtra("dayRecordModif", dayRecord);
 	    setResult(RESULT_OK, returnIntent);
 	} else {
 	    setResult(RESULT_CANCELED, returnIntent);
 	}
 
 	super.onBackPressed();
 	finish();
     }
 
     private void launchDetailDayActivity(ActivityDay activityDay) {
 	Intent intent = new Intent(GeneralDetailDayActivity.this, DetailDayActivity.class);
 	intent.putExtra("activityDay", activityDay);
 	intent.putExtra("dateForm", GeneralDetailDayActivity.this.dayRecord.getDateForm());
 	intent.putExtra("dayNum", GeneralDetailDayActivity.this.dayRecord.getDayNum());
 	startActivityForResult(intent, ACTIVITY_REQUEST);
     }
 
     private void launchRemoveProcessWithAlertDialog() {
 	this.alertDialogActive = true;
 	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 	alertDialogBuilder.setTitle(getString(R.string.msgRemoveAllActivities));
 	alertDialogBuilder.setMessage(getString(R.string.msgContinue));
 	alertDialogBuilder.setPositiveButton(getString(R.string.msgAlertOK),
 		new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int which) {
 			GeneralDetailDayActivity.this.alertDialogActive = false;
 			showDialog();
 			GeneralDetailDayActivity.this.dayModif = true;
 			AsyncTask<Integer, Integer, Integer> connectionAsyncTask = new RemoveAllActivitiesAsyncTask();
 			connectionAsyncTask.execute(1);
 		    }
 		});
 	alertDialogBuilder.setNegativeButton(getString(R.string.msgAlertCancel),
 		new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int which) {
 			GeneralDetailDayActivity.this.alertDialogActive = false;
 		    }
 		});
 	alertDialog = alertDialogBuilder.show();
     }
 
     private class RemoveAllActivitiesAsyncTask extends AsyncTask<Integer, Integer, Integer> {
 
 	@Override
 	protected Integer doInBackground(Integer... param) {
 	    Log.d(RemoveAllActivitiesAsyncTask.class.toString(), "doInBackground...");
 	    WebConnection webConnection = ConnectionFacade.getWebConnection();
 
 	    Integer result = null;
 	    for (ActivityDay activityDay : dayRecord.getActivities()) {
 		try {
 		    activityDay.setToRemove(true);
 		    result = webConnection.removeDay(activityDay);
 		} catch (ConnectionException e) {
 		    Log.e(LOGTAG, "Error al borrar datos de una actividad", e);
 		    result = -2;
 		}
 
 		if (result != 1) {
 		    break;
 		}
 	    }
 
 	    return result;
 	}
 
 	@Override
 	protected void onPostExecute(Integer result) {
 	    Log.d(RemoveAllActivitiesAsyncTask.class.toString(), "onPostExecute...");
 
 	    this.closeDialog();
 	    if (result == 1) {
 		GeneralDetailDayActivity activity = (GeneralDetailDayActivity) AppData.getCurrentActivity();
 		activity.emptyListAndRedraw();
 		activity.showORhideRemoveAllButton();
 		showToastMessage(getString(R.string.msgRemoveOK));
 	    } else {
 		showToastMessage(ErrorUtils.getMessageError(result));
 	    }
 	}
 
 	private void closeDialog() {
 	    GeneralDetailDayActivity activity = (GeneralDetailDayActivity) AppData.getCurrentActivity();
 	    activity.closeDialog();
 	}
 
 	private void showToastMessage(String message) {
 	    Toast toast = Toast.makeText(AppData.getCurrentActivity(), message, Toast.LENGTH_LONG);
 	    toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 20);
 	    toast.show();
 	}
     }
 }
