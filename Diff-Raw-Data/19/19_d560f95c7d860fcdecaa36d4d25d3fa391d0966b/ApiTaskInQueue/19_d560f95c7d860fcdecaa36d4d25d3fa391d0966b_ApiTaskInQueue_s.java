 /**
  * 
  */
 package org.vimeoid.activity.user;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.activity.user</dd>
  * </dl>
  *
  * <code>ApiTaskInQueue</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Oct 1, 2010 9:25:33 PM 
  *
  */
 public class ApiTaskInQueue extends ApiTask implements IApiTaskWithNextTask {
     
     public static interface TaskListener {
         public void onPerformed(JSONObject jsonObj) throws Exception;  
     };
 
     private final int taskId;
     private IApiTaskWithNextTask nextTask = null;
     private final SuccessiveApiTasksSupport performer;
     private final TaskListener listener;
     
     public ApiTaskInQueue(SuccessiveApiTasksSupport performer, int taskId, String apiMethod) {
         this(performer, taskId, apiMethod, null);
     }
     
     protected ApiTaskInQueue(SuccessiveApiTasksSupport performer, int taskId, String apiMethod, TaskListener listener) {
         super(apiMethod);
         this.taskId = taskId;
         this.performer = performer;
         this.listener = listener;
     }
     
     @Override
     protected void onPostExecute(JSONObject jsonObj) {
         super.onPostExecute(jsonObj);
         try {        
             if (listener != null) listener.onPerformed(jsonObj);
             if (nextTask != null) {
                 performer.execute(nextTask);
             } else performer.finish();
         } catch (Exception e) {
            onAnyError(e, "Error while executing task " + nextTask.getId());
         }        
     }
     
     @Override
     public void onAnswerReceived(JSONObject jsonObj) throws JSONException {
         performer.onPerfomed(taskId, jsonObj);
     }
     
     public int getId() { return taskId; }
     
     public void setNextTask(IApiTaskWithNextTask nextTask) {
         if (this.nextTask != null) throw new IllegalStateException("Next task is already set");
         this.nextTask = nextTask;
     }
 
     @Override protected void onAnyError(Exception e, String message) {
         performer.onError(e, message);
     }    
     
 }
