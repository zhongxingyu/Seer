 /**
  *  NoteEditLayoutManager is a helper class  
  */
 
 package noteBlock.hig.noteedit;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import noteBlock.hig.R;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
import android.util.Log;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 
 
 
 /**
  * 
  * @author Kristoffer Benum, and Solveig Srheim
  *
  */
 public class NoteEditLayoutManager{
 	private NoteEdit noteEdit;
 	private Long mRowId;
 	private ToggleButton alarmPosition, alarmTime, showAlarmInfo;
 	private TextView alarmPositionInfo, alarmTimeInfo;
 	private LinearLayout showAlarmLayout;
 	private NoteEditSavePopulate savePopulate;
 	
 	public NoteEditLayoutManager(Context con, Long rowId
 			, NoteEditSavePopulate savePop) {
 		mRowId = rowId;
 		noteEdit = (NoteEdit) con;
 		savePopulate = savePop;
 		
 		alarmPosition = (ToggleButton) noteEdit.findViewById(R.id.toggleAlarmPosition);
 		alarmTime = (ToggleButton) noteEdit.findViewById(R.id.toggleAlarmTime);
 		alarmPositionInfo = (TextView) noteEdit.findViewById(R.id.alarmPositionInfo);
 		alarmTimeInfo = (TextView) noteEdit.findViewById(R.id.alarmTimeInfo);
 		showAlarmInfo = (ToggleButton) noteEdit.findViewById(R.id.showAlarmNoteInfo);
 		showAlarmLayout = (LinearLayout) noteEdit.findViewById(R.id.alarmInfo);
 	}
 	
 	public void changePositionBtnStatus() {
 		boolean changePosition = alarmPosition.isChecked();
 
 		// If changePosition was set to "on":
 		if (changePosition) {
 
 			// If the note was previously initiated with latitude and longitude:
 			if (!savePopulate.getLongitude().contains("long")) {
 				savePopulate.updatePositionNotify("true");
 				setPositionInfo(true, savePopulate.getSnippet());
 			}
 
 			// If the note does not have valid latitude and longitude stored:
 			else {
 				setPositionInfo(false, "");
 				createNoMapsAlert();
 				//TODO: Check if there should be a return statement here..
 			}
 		}
 		// If the toggle was set to off:
 		else {
 			savePopulate.updatePositionNotify("false");
 			setPositionInfo(false, savePopulate.getSnippet());
 		}
 	}
 
 	private void createNoMapsAlert(){
 		// Use the Builder class for convenient dialog construction
 		AlertDialog.Builder builder = new AlertDialog.Builder(noteEdit);
 		builder.setMessage(R.string.no_maps_alert)
 				.setPositiveButton(R.string.start_google_maps,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int id) {
 								//mDbHelper.updatePositionNotification(mRowId, "true");
 								// FIRE ZE MISSILES!
 							}
 						})
 				.setNegativeButton(R.string.cancel,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int id) {
 								// User cancelled the dialog
 							}
 						});
 		// Create the AlertDialog object and return it
 		builder.show();
 	}
 	
 	
 	// TODO: Finish this
 	public void changeTimeStatus() {
 		boolean changePosition = alarmTime.isChecked();
 
 		// If changePosition was set to "on":
 		if (changePosition) {
 
 			// If the note was previously initiated with time:
 			if (savePopulate.getTime()!=0) {
 				savePopulate.updateTimeNotification("true");
 				setTimeInfo(true, savePopulate.getTime());
 				updateAlarm("updateTime");
 			}
 
 			// If the note does not have valid time stored:
 			else {
 				setTimeInfo(false, savePopulate.getTime());
 				//mDbHelper.updateTime(mRowId, time, "true");
 				//TODO: ask the user if he/she want to be reminded of time. or just initialize timedialog..
 				//TODO: Check if there should be a return statement here..
 			}
 		}
 		// If the toggle was set to off:
 		else {
 			savePopulate.updateTimeNotification("false");
 			updateAlarm("stopTimeAlarm");
 		}
 	}
 	
 	public void setPositionInfo(boolean checked, String text) {
 		alarmPosition.setChecked(checked);
 		alarmPositionInfo.setText(text);
 	}
 	
 	public void setTimeInfo(boolean checked, long time){
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
 		
 		alarmTime.setChecked(checked);
 		alarmTimeInfo.setText(String.valueOf(dateFormat.format(time)));
 	}
 	
 	public void displayAlarmInfo() {
 		Date date = new Date();
 		long now = date.getTime();
 		//if(savePopulate.getTime()<=now) savePopulate.updateTimeNotification("false");
 		
 		if (!savePopulate.getLongitude().contains("long") || savePopulate.getTime()!=0) {
 			showAlarmInfo.setVisibility(View.VISIBLE);
 
 			if (savePopulate.getPositionReminder().contains("true"))
 				setPositionInfo(true, savePopulate.getSnippet());
 			else
 				setPositionInfo(false, savePopulate.getSnippet());
 			
			Log.i("cake-test. ", "time is: " + savePopulate.getTime());
			
			if(savePopulate.getTime()!=0){
				if(savePopulate.getTimeReminder().contains("true")) 
					setTimeInfo(true, savePopulate.getTime());
				else setTimeInfo(false, savePopulate.getTime());
			}
			
 		}
 	}
 	
 	public void showAlarmLayout(){
 		boolean on = (showAlarmInfo).isChecked();
 
 		if (on)	showAlarmLayout.setVisibility(View.VISIBLE);
 		else	showAlarmLayout.setVisibility(View.GONE);
 	}
 	
 	public void updateAlarm(String command){
 		Intent i = new Intent("com.example.ass2note.notepad.NoteEdit.connectionReceiver");
 		i.putExtra("fromCaller", "InitiateAlarmButtons");
 		i.putExtra("command", command);
 		i.putExtra("time", savePopulate.getTime());
 		noteEdit.sendBroadcast(i);
 	}
 }
