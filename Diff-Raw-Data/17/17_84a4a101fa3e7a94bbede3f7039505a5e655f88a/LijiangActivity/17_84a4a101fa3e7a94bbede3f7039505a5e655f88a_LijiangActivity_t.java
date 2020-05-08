 package com.utopia.lijiang;
 
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 
 import com.utopia.lijiang.alarm.Alarm;
 import com.utopia.lijiang.alarm.AlarmManager;
 import com.utopia.lijiang.service.LocationService;
 /** Main Activity of the application.
  * When this activity is created, the application's global variables will be initialed.
  * And a location service will be started. 
  * But this service will be not closed, when this activity is finished.
  * 
  * This activity has a singleton method. 
  * So you can get last instance of this activity anywhere in the application.
  * For example: <i>LijiangActivity.getLatestInstance().setTaskInfoText();</i>
  * 
  * @author chao_zhou
  * @version 1.0.0.0
  * */
 public class LijiangActivity extends Activity  {
 	
 	
 	private static LijiangActivity instance = null;
 	
 	private final int STATE_LIST = AlarmAdapter.ListAlarmViewState;
 	private final int STATE_EDIT = AlarmAdapter.EditAlarmViewState;
 	
 	
 	private int viewState;
 	private ListView listView = null;
 	private View emptyView = null;
 	AlarmManager alarmMgr = null;
 	
 	/**
 	 * Singleton method, this will be update after this activity is launched
 	 * @return The Latest showed LijiangActivity
 	 */
 	public static LijiangActivity getLatestInstance(){
 		return instance;
 	}
 		
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	Log.d(getString(R.string.debug_tag),"Create LijiangActivity ");
     	
     	super.onCreate(savedInstanceState);     	
     	setContentView(R.layout.main_new);
     	
     	listView = (ListView)this.findViewById(R.id.alarmList);
     	emptyView = (View)this.findViewById(R.id.alarmListEmpty);
     	
     	alarmMgr = AlarmManager.getInstance();
 
     }    
 
     @Override
	protected void onResume() {
    	Log.d(getString(R.string.debug_tag),"Resume LijiangActivity ");
    	super.onResume();
     	
     	showAlarmingAlarms();
     	setListViewState(STATE_LIST);
	}

	/**Called when the activity is finished*/
     @Override
     public void onDestroy(){
     	Log.d(getString(R.string.debug_tag),"Destroy LijiangActivity");
     	super.onDestroy();
     	
     	alarmMgr.save2DB(this);
     }   
        
    
     /*
 	 * Pass the Back Press event to parent
 	 * @see android.app.Activity#onBackPressed()
 	 */
 	@Override
 	public void onBackPressed() {
 		if(viewState == STATE_EDIT){
 			setListViewState(STATE_LIST);
 			return;
 		}
 		
 		Activity parent = this.getParent();
 		if(parent != null){
 			parent.onBackPressed();
 		}
 	}
     
    
     /** Do work after showAddAlarm button is clicked
      * @param target trigger
      * */
     public void showAddAlarmActivity(View target){
    	 //Just test Map 
      //this.startActivity(new Intent(this,LijiangMapActivity.class));
     	MainActivity.getInstance().setCurrentTab(MainActivity.ADD_POSITION_TAB_INDEX);
     }
     
     public void showEditView(View target){
     	int viewState = getListViewState();
     	
     	if(viewState == STATE_LIST){
     		setListViewState(STATE_EDIT);
     		return;
     	}
     	
     	setListViewState(STATE_LIST);
     }
     
     public void showAlarms(View target){
     	setListViewState(STATE_LIST);
     }
     
     /**Fill Data into Task List*/
 	private void bindList(List<Alarm> alarms){ 
 	       AlarmAdapter adapter = 
 	    		   new AlarmAdapter(this,alarms);
 	       adapter.setViewState(viewState);
 	       listView.setAdapter(adapter);  
 	}
 	
 	private int getListViewState(){
 		return viewState;
 	}
 	
 	private void setListViewState(int stateNum){
 		viewState = stateNum;
 		List<Alarm> alarms  = null;
 		switch(stateNum){
 			case STATE_LIST: 
 				alarms = alarmMgr.getAllAlarms();
 				setEmptyView(emptyView);
 				bindList(alarms);
 				break;
 			case STATE_EDIT:
 				alarms = alarmMgr.getAllAlarms();
 				setEmptyView(emptyView);
 				bindList(alarms);
 				break;
 		}
 	}
 	
 	private void setEmptyView(View view){
 		Log.d(getString(R.string.debug_tag),"setEmptyView");
 		
 		View originView = listView.getEmptyView();	
 		
 		//Because we change the emptyView,
 		//we should let that layout gone manually
 		if(originView !=null 
 		&& originView.getVisibility() != View.GONE){
 			originView.setVisibility(View.GONE);
 		}
 		
 		listView.setEmptyView(view);
 	}
 
 	private void refreshList(){
 		setListViewState(getListViewState());
 	}
 	
 
 	//--------------------------
 	// Show alarming alarms
 	//--------------------------
 	private void showAlarmingAlarms(){
 		Iterator<Alarm> it = alarmMgr.getAlarmingAlarms().iterator();
 		while(it.hasNext()){
 			showAlarmDialog(it.next());
 		}
 		
 		
 	}
 	
 	private void showAlarmDialog(final Alarm alarm){
 		String posStr = getString(R.string.known);
 		@SuppressWarnings("unused")
 		String negStr = getString(R.string.no);
 		String msg = String.format(getString(R.string.locatinNearFormat), alarm.getTitle());
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(msg)
 		       .setPositiveButton(posStr, new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	   alarm.setActive(false);
 		        	   alarmMgr.getAlarmingAlarms().remove(alarm);
 		        	   LocationService.getLatestInstance().refreshAlarmNotification();
 		        	   refreshList();
 		           }
 		       })
 		      /* .setNegativeButton(negStr, new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       })*/;
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 }
