 package byku.traindroid;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 
public class AsyncTimeTableUpdater extends AsyncTask<Object, Void, Void>
 {
     private ProgressDialog _dialog;
     private Activity _activity;
 
     public AsyncTimeTableUpdater(ProgressDialog dialog)
     {
         _dialog = dialog;
     }
 
     @Override
    protected Void doInBackground(Object... objects)
     {
         _activity = (Activity) objects[7];
 
         String result = DataFacade.UpdateTimeTable((Station)objects[0], (Station)objects[1], (Integer)objects[2],
                 (Integer)objects[3], (Boolean)objects[4], (Boolean)objects[5]);
         if ((Boolean)objects[6])
         {
             result += DataFacade.UpdateTimeTable((Station)objects[1], (Station)objects[0], (Integer)objects[2],
                     (Integer)objects[3], (Boolean)objects[4], (Boolean)objects[5]);
         }
 
        if (result != null && !result.equals(""))
        {
            UIUtils.ShowToast(_activity, result);
        }
        return null;
     }
 
     @Override
     protected void onPreExecute()
     {
         _dialog.show();
     }
 
     @Override
    protected void onPostExecute(Void result)
     {
         _dialog.dismiss();
         _activity.finish();
     }
 }
