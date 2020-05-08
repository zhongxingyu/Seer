 package org.kvj.quebec4.service;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.kvj.bravo7.SuperService;
 import org.kvj.quebec4.R;
 import org.kvj.quebec4.service.Q4Controller.ControllerListener;
 import org.kvj.quebec4.service.data.TaskBean;
 import org.kvj.quebec4.ui.TaskList;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 
 public class Q4Service extends SuperService<Q4Controller, Q4App> implements
 		ControllerListener {
 
 	protected static final String TAG = "Q4Service";
 	protected static String LOCK_NAME = "Q4Service";
 	private static final String GET_TASKS_ACTION = "org.kvj.quebec4.action.GET_LIST";
 	private static final String GET_TASKS_RESPONSE_ACTION = "org.kvj.quebec4.action.GET_LIST_RESP";
 	private static final String GET_TASK_ACTION = "org.kvj.quebec4.action.GET";
 	private static final String GET_TASK_RESPONSE_ACTION = "org.kvj.quebec4.action.GET_RESP";
 	private static final String GOT_TASK_ACTION = "org.kvj.quebec4.action.GOT";
 
 	private BroadcastReceiver getTaskReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			try {
 				synchronized (controller) {
 					int id = intent.getIntExtra("id", -1);
 					Log.i(TAG, "Get task: " + id);
 					TaskBean task = controller.getTask(id);
 					if (null != task) {
 						JSONObject taskObject = controller.taskToJSON(task,
 								true);
 						Intent outIntent = new Intent(GET_TASK_RESPONSE_ACTION);
 						outIntent.putExtra("object", taskObject.toString());
 						sendBroadcast(outIntent);
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	};
 
 	private BroadcastReceiver gotTaskReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.i(TAG, "Item created: " + intent);
 			try {
 				synchronized (controller) {
 					int id = intent.getIntExtra("id", -1);
 					if (controller.updateStatus(id, TaskBean.STATUS_SENT)) {
 						// TODO: Add cleanup old tasks to configuration
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	};
 
 	private BroadcastReceiver getTasksReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			try {
 				synchronized (controller) {
 					Log.i(TAG, "Get tasks");
 					List<TaskBean> tasks = controller.getTasks(
 							TaskBean.STATUS_CONSUME, TaskBean.STATUS_SLEEP,
 							TaskBean.STATUS_CONSUME_AND_FINISH,
 							TaskBean.STATUS_READY);
 					JSONArray result = new JSONArray();
 					for (int i = 0; i < tasks.size(); i++) {
 						TaskBean task = tasks.get(i);
 						result.put(controller.taskToJSON(task, false));
 					}
 					Intent outIntent = new Intent(GET_TASKS_RESPONSE_ACTION);
 					outIntent.putExtra("list", result.toString());
 					sendBroadcast(outIntent);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	};
 
 	public Q4Service() {
 		super(Q4Controller.class, "Quebec4");
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		setAlarmBroadcastReceiverClass(DataAlarmReceiver.class);
 		registerReceiver(getTasksReceiver, new IntentFilter(GET_TASKS_ACTION));
 		registerReceiver(getTaskReceiver, new IntentFilter(GET_TASK_ACTION));
 		registerReceiver(gotTaskReceiver, new IntentFilter(GOT_TASK_ACTION));
 		controller.setListener(this);
 		controller.rescheduleTasks();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.i(TAG, "Got intent: " + intent);
 		if (null != intent && null != intent.getExtras()) {
 			if (intent.hasExtra("id")) {
 				synchronized (controller) {
 					try {
 						int taskID = intent.getIntExtra("id", -1);
 						Log.i(TAG, "Task: " + taskID);
 						TaskBean task = controller.getTask(taskID);
 						if (null != task
 								&& task.status == TaskBean.STATUS_SLEEP) {
 							if (controller.updateStatus(taskID,
 									TaskBean.STATUS_CONSUME)) {
 								controller.wantLocation();
 							}
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					powerUnlock(this);
 				}
 			}
 		}
 		return Service.START_STICKY;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		controller.setListener(null);
 		unregisterReceiver(getTaskReceiver);
 		unregisterReceiver(getTasksReceiver);
 		unregisterReceiver(gotTaskReceiver);
 	}
 
 	public void searching() {
 		raiseNotification(R.drawable.s_searching, "Searching for location...",
 				TaskList.class);
 	}
 
 	public void waiting() {
		raiseNotification(R.drawable.s_sleep, "Idle in path", TaskList.class);
 	}
 
 	public void done() {
 		hideNotification();
 	}
 
 	public void schedule(int taskID, int mins) {
 		Calendar c = Calendar.getInstance();
 		c.add(Calendar.MINUTE, mins);
 		Bundle bundle = new Bundle();
 		bundle.putInt("id", taskID);
 		Log.i(TAG,
 				"Scheduling: " + taskID + ", " + new Date(c.getTimeInMillis()));
 		runAtTime(c.getTimeInMillis(), taskID, bundle);
 	}
 }
