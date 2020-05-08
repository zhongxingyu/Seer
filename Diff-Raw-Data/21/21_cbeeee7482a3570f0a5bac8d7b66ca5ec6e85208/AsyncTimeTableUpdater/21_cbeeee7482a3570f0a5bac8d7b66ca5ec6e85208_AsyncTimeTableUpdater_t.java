 package byku.traindroid;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 
public class AsyncTimeTableUpdater extends AsyncTask<Object, Void, String>
 {
     private ProgressDialog _dialog;
     private Activity _activity;
 
     public AsyncTimeTableUpdater(ProgressDialog dialog)
     {
         _dialog = dialog;
     }
 
     @Override
    protected String doInBackground(Object... objects)
     {
         _activity = (Activity) objects[7];
 
         String result = DataFacade.UpdateTimeTable((Station)objects[0], (Station)objects[1], (Integer)objects[2],
                 (Integer)objects[3], (Boolean)objects[4], (Boolean)objects[5]);
         if ((Boolean)objects[6])
         {
             result += DataFacade.UpdateTimeTable((Station)objects[1], (Station)objects[0], (Integer)objects[2],
                     (Integer)objects[3], (Boolean)objects[4], (Boolean)objects[5]);
         }
 
        return result;
     }
 
     @Override
     protected void onPreExecute()
     {
         _dialog.show();
     }
 
     @Override
    protected void onPostExecute(String result)
     {
        if (result != null && !result.equals(""))
        {
            UIUtils.ShowToast(_activity, result);
        }

         _dialog.dismiss();
         _activity.finish();
     }
 }
