 package ru.terra.jbrss.activity.components;
 
 import android.content.Context;
 import android.widget.Toast;
 import ru.terra.jbrss.constants.Constants;
 import ru.terra.jbrss.core.AsyncTaskEx;
 import ru.terra.jbrss.core.WorkIsDoneListener;
 import ru.terra.jbrss.dto.LoginDTO;
 import ru.terra.jbrss.network.JBRssRest;
 
 public class LoginAsyncTask extends AsyncTaskEx<Void, Void, Boolean> {
 
     private JBRssRest jbRssRest;
     private WorkIsDoneListener workIsDoneListener;
     private Context context;
     private String message = "";
 
     public LoginAsyncTask(JBRssRest jbRssRest, WorkIsDoneListener workIsDoneListener, Context context) {
         super(20000l, context);
         this.jbRssRest = jbRssRest;
         this.workIsDoneListener = workIsDoneListener;
         this.context = context;
         showDialog("Вход", "Выполняется вход");
     }
 
     @Override
     protected Boolean doInBackground(Void... params) {
         LoginDTO ret = null;
         try {
             ret = jbRssRest.login();
         } catch (Exception e) {
             exception = e;
         }
        message = ret.message;
        return ret.logged;
     }
 
     @Override
     protected void onPostExecute(Boolean result) {
         dismissDialog();
         if (message.length() > 0)
             Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
         if (workIsDoneListener != null)
             workIsDoneListener.workIsDone(Constants.LOGIN_ACTION, exception, result.toString());
     }
 
     @Override
     protected void onCancelled() {
         dismissDialog();
         Toast.makeText(context, "Превышено время ожидания", Toast.LENGTH_SHORT).show();
     }
 }
