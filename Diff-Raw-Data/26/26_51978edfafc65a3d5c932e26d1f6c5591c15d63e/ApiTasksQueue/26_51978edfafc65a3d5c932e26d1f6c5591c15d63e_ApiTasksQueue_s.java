 /**
  * 
  */
 package org.vimeoid.activity.user;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.vimeoid.activity.base.ApiPagesReceiver;
 import org.vimeoid.activity.base.ListApiTask_.Judge;
 import org.vimeoid.activity.user.ApiTaskInQueue.TaskListener;
 import org.vimeoid.util.ApiParams;
 
 import android.util.Log;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.activity.user</dd>
  * </dl>
  *
  * <code>ApiTasksQueue</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Oct 1, 2010 10:22:29 PM 
  *
  */
 public abstract class ApiTasksQueue implements SuccessiveApiTasksSupport {
 	
 	public static final String TAG = "ApiTasksQueue";   
 
     private IApiTaskWithNextTask firstTask = null;
     private IApiTaskWithNextTask lastTask = null;
     private Map<Integer, ApiParams> tasksParams = null;
     private int currentTask = -1;
     private boolean running = false;
     private boolean started = false;
     private int size = 0;
     
     @Override
     public ApiTask add(int taskId, String apiMethod, ApiParams params) {
     	Log.d(TAG, "Adding task " + taskId + " " + apiMethod + " : " + params);
         return (ApiTask)addTask(new ApiTaskInQueue(this, taskId, apiMethod), params); 
     }
     
     public ApiTask add(int taskId, String apiMethod, ApiParams params, TaskListener listener) {
         Log.d(TAG, "Adding task " + taskId + " " + apiMethod + " : " + params);
         return (ApiTask)addTask(new ApiTaskInQueue(this, taskId, apiMethod, listener), params); 
     }    
     
     public ListApiTask addListTask(int taskId, String apiMethod, ApiParams params, ApiPagesReceiver<JSONObject> receiver) {
         return addListTask(taskId, apiMethod, params, receiver, 5, 25, null);
     }
     
     public ListApiTask addListTask(int taskId, String apiMethod, ApiParams params, ApiPagesReceiver<JSONObject> receiver, Judge<JSONObject> filter) {
     	return addListTask(taskId, apiMethod, params, receiver, 5, 25, filter);
     }
     
     public ListApiTask addListTask(int taskId, String apiMethod, ApiParams params, ApiPagesReceiver<JSONObject> receiver, int maxPages, int perPage) {
     	return addListTask(taskId, apiMethod, params, receiver, maxPages, perPage, null);
     }    
     
     public ListApiTask addListTask(int taskId, String apiMethod, ApiParams params, ApiPagesReceiver<JSONObject> receiver, int maxPages, int perPage, Judge<JSONObject> filter) {
         Log.d(TAG, "Adding list task " + taskId + " " + apiMethod + " : " + params);
         final ListApiTaskInQueue newTask = new ListApiTaskInQueue(this, taskId, apiMethod, receiver);
         newTask.setMaxPages(maxPages);
         newTask.setPerPage(perPage);
         newTask.setFilter(filter);
         addTask(newTask, params);
         return newTask;
     }
 
     private IApiTaskWithNextTask addTask(IApiTaskWithNextTask task, ApiParams params) {
         if (isEmpty()) {
             firstTask = task;
             lastTask = task;
             tasksParams = new HashMap<Integer, ApiParams>();
         } else {
             lastTask.setNextTask(task);
             lastTask = task;
         }
         tasksParams.put(task.getId(), params);
         size += 1;
         return task;
     }
 
     @Override
     public void run() {
     	Log.d(TAG, "Running first task");
         if (!isEmpty())
             try {
                 started = true;                
                 execute(firstTask);
             } catch (Exception e) {
                 onError(e, e.getLocalizedMessage());
             }
         else throw new IllegalStateException("Queue is empty");
     }
     
     @Override
     public void onPerfomed(int taskId, JSONObject result) throws JSONException {
     	Log.d(TAG, "Task " + taskId + " performed");
         if (taskId != currentTask) throw new IllegalStateException("Tasks queue desynchronized");
         running = false;
     }
     
     @Override
     public void execute(IApiTaskWithNextTask task) throws Exception {
     	Log.d(TAG, "Trying to run task " + task.getId());
         if (running) throw new IllegalStateException("Tasks queue desynchronized");
         currentTask = task.getId();
         running = true;
         Log.d(TAG, "Running task " + task.getId() + " with params " + tasksParams.get(task.getId()));
         task.execute(tasksParams.get(task.getId())).get();
     }
     
     public boolean started() {
         return started;
     }
     
     public boolean running() {
         return running;
     }
     
     public int size() {
         return size;
     }
     
     public void finish() {
         firstTask = null;
         lastTask = null;
         if (tasksParams != null) tasksParams.clear();
         tasksParams = null;
         currentTask = -1;
         running = false;
         started = false;
         size = 0;        
     }
     
    
    
     public boolean isEmpty() {
         return (firstTask == null);
     }
 
 }
