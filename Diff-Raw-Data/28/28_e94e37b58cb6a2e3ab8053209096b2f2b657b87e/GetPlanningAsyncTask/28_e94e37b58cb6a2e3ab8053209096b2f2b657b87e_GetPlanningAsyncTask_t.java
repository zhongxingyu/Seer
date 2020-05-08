 package com.level42.mixit.tasks;
 
 import java.util.List;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.level42.mixit.exceptions.FunctionnalException;
 import com.level42.mixit.exceptions.TechnicalException;
 import com.level42.mixit.listeners.OnTaskPostExecuteListener;
 import com.level42.mixit.models.Talk;
 import com.level42.mixit.services.IPlanningService;
 import com.level42.mixit.utils.Utils;
 
 /**
  * Tâche asynchrone pour la collecte des informations des sessions.
  */
 public class GetPlanningAsyncTask extends
         AsyncTask<Object, Integer, List<Talk>> {
 
     /**
      * Listener.
      */
     private OnTaskPostExecuteListener<List<Talk>> onTaskPostExecuteListener = null;
 
     /**
      * Raison de l'interuption.
      */
     private Exception cancelReason;
 
     /*
      * (non-Javadoc)
      * @see android.os.AsyncTask#doInBackground(Params[])
      */
     @Override
     protected List<Talk> doInBackground(Object... params) {
         try {
             IPlanningService service = (IPlanningService) params[0];
             Integer delay = (Integer) params[1];
            Boolean hide = (Boolean) params[2];
            return service.getTalksForPlanning(delay, hide);
         } catch (FunctionnalException e) {
             Log.e(Utils.LOGTAG, e.getMessage());
             Log.d(Utils.LOGTAG, e.getCause().getMessage());
             cancelReason = e;
         } catch (TechnicalException e) {
             Log.e(Utils.LOGTAG, e.getMessage());
             Log.d(Utils.LOGTAG, e.getCause().getMessage());
             cancelReason = e;
         }
         this.cancel(true);
         return null;
     }
 
     /*
      * (non-Javadoc)
      * @see android.os.AsyncTask#onCancelled()
      */
     @Override
     protected void onCancelled() {
         super.onCancelled();
 
         if (onTaskPostExecuteListener != null && cancelReason != null) {
             onTaskPostExecuteListener.onTaskInterruptListener(cancelReason);
         }
         if (onTaskPostExecuteListener != null && cancelReason == null) {
             onTaskPostExecuteListener.onTaskCancelledListener();
         }
     }
 
     /*
      * (non-Javadoc)
      * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
      */
     @Override
     protected void onPostExecute(List<Talk> result) {
         super.onPostExecute(result);
 
         if (result != null) {
             Log.d(Utils.LOGTAG, "Nombre de talks : " + result.size());
         } else {
             Log.d(Utils.LOGTAG, "Aucun talks");
         }
         
         if (onTaskPostExecuteListener != null) {
             onTaskPostExecuteListener.onTaskPostExecuteListener(result);
         }
     }
 
     /**
      * Permet de renseigner un listener.
      * @param taskPostExecute
      *            Listener de la tâche
      */
     public void setPostExecuteListener(
             OnTaskPostExecuteListener<List<Talk>> taskPostExecute) {
         onTaskPostExecuteListener = taskPostExecute;
     }
 
 }
