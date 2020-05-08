 package com.group5.android.fd.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 
 import com.group5.android.fd.FdConfig;
 import com.group5.android.fd.Main;
 import com.group5.android.fd.adapter.TaskAdapter;
 import com.group5.android.fd.entity.TaskEntity;
 import com.group5.android.fd.entity.UserEntity;
 import com.group5.android.fd.service.TaskUpdaterService;
 import com.group5.android.fd.service.TaskUpdaterServiceReceiver;
 import com.group5.android.fd.view.TaskGroupView;
 
 /**
  * The activity to display a list of tasks
  * 
  * @author Tran Viet Son
  * 
  */
 public class TaskListActivity extends ServerBasedActivity {
 
 	final public static String EXTRA_DATA_NAME_TASK_OBJ = "taskObj";
 
 	protected UserEntity m_user;
 	protected TaskAdapter m_taskAdapter;
 	protected View m_vwSelected = null;
 
 	protected BroadcastReceiver m_broadcastReceiverForNewTask = null;
 	protected PowerManager.WakeLock wakeLock;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Intent intent = getIntent();
 		m_user = (UserEntity) intent
 				.getSerializableExtra(Main.EXTRA_DATA_NAME_USER_OBJ);
 
 		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
 				FdConfig.DEBUG_TAG);
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		// we want to preserve our order information when configuration is
 		// change, say.. orientation change?
 		return m_taskAdapter.getTaskList();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		getTasksAndInitLayoutEverything();
 
 		wakeLock.acquire();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
		unbindService(m_taskAdapter);
 
 		if (m_broadcastReceiverForNewTask != null) {
 			unregisterReceiver(m_broadcastReceiverForNewTask);
 			m_broadcastReceiverForNewTask = null;
 		}
 
 		wakeLock.release();
 	}
 
 	/**
 	 * Initiates the layout (inflate from a layout resource named
 	 * activity_main). And then maps all the object properties with their view
 	 * instance. Finally, initiates required listeners on those views.
 	 * 
 	 * @param taskList
 	 *            a <code>List</code> of {@link TaskEntity} to pre-populate the
 	 *            list
 	 */
 	protected void initLayout(List<TaskEntity> taskList) {
 		m_taskAdapter = new TaskAdapter(this, m_user, taskList);
 		setListAdapter(m_taskAdapter);
 
 		// start our service
 		Intent service = new Intent(this, TaskUpdaterService.class);
 		bindService(service, m_taskAdapter, Context.BIND_AUTO_CREATE);
 
 		// listen to the service intent
 		m_broadcastReceiverForNewTask = new TaskUpdaterServiceReceiver(this) {
 
 			@Override
 			protected void onReceive(Context context, TaskEntity task) {
 				m_taskAdapter.addTask(task);
 			}
 
 		};
 	}
 
 	/**
 	 * Gets the pending tasks for current user and set them up.
 	 */
 	@SuppressWarnings("unchecked")
 	protected void getTasksAndInitLayoutEverything() {
 		Object lastNonConfigurationInstance = getLastNonConfigurationInstance();
 		List<TaskEntity> taskList = null;
 		if (lastNonConfigurationInstance != null
 				&& lastNonConfigurationInstance instanceof List<?>) {
 			// found our long lost task list, yay!
 			taskList = (List<TaskEntity>) lastNonConfigurationInstance;
 
 			Log.i(FdConfig.DEBUG_TAG, "List<TaskEntity> has been recovered");
 		}
 
 		if (taskList == null) {
 			// String tasksUrl = UriStringHelper.buildUriString("tasks");
 			//
 			// new HttpRequestAsyncTask(this, tasksUrl) {
 			//
 			// @Override
 			// protected Object process(JSONObject jsonObject) {
 			// List<TaskEntity> taskList = new ArrayList<TaskEntity>();
 			//
 			// try {
 			// Object obj = jsonObject.get("tasks");
 			// if (obj instanceof JSONArray) {
 			// // this is the case when there are no tasks
 			// } else {
 			// JSONObject tasks = (JSONObject) obj;
 			// JSONArray taskIds = tasks.names();
 			// for (int i = 0; i < taskIds.length(); i++) {
 			// TaskEntity task = new TaskEntity();
 			// JSONObject jsonObject2 = tasks
 			// .getJSONObject(taskIds.getString(i));
 			// task.parse(jsonObject2);
 			// taskList.add(task);
 			// }
 			// }
 			// } catch (NullPointerException e) {
 			// Log.d(FdConfig.DEBUG_TAG,
 			// "getTasks/preProcess got NULL response");
 			// e.printStackTrace();
 			// } catch (JSONException e) {
 			//
 			// e.printStackTrace();
 			// }
 			//
 			// return taskList;
 			// }
 			//
 			// @Override
 			// protected void onSuccess(JSONObject jsonObject, Object processed)
 			// {
 			// if (processed != null && processed instanceof List<?>) {
 			// initLayout((List<TaskEntity>) processed);
 			// }
 			// }
 			//
 			// }.execute();
 			initLayout(new ArrayList<TaskEntity>());
 		} else {
 			initLayout(taskList);
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		if (arg1 instanceof TaskGroupView) {
 			((TaskGroupView) arg1).expandTasks();
 		}
 
 		if (m_vwSelected != null && m_vwSelected instanceof TaskGroupView
 				&& m_vwSelected != arg1) {
 			((TaskGroupView) m_vwSelected).collapseTasks();
 		}
 
 		m_vwSelected = arg1;
 	}
 }
