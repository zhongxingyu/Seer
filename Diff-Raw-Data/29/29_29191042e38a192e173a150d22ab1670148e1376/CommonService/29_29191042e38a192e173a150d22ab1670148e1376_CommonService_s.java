 package cz.zcu.kiv.eeg.mobile.base.archetypes;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import cz.zcu.kiv.eeg.mobile.base.R;
 import cz.zcu.kiv.eeg.mobile.base.data.ServiceState;
 import cz.zcu.kiv.eeg.mobile.base.data.Values;
 import org.springframework.http.HttpStatus;
 import org.springframework.web.client.HttpClientErrorException;
 import org.springframework.web.client.RestClientException;
 
 import java.lang.ref.SoftReference;
 
 public abstract class CommonService<T, U, V> extends AsyncTask<T, U, V> {
 
     protected SoftReference<CommonActivity> activity;
 
     public CommonService(CommonActivity context) {
         this.activity = new SoftReference<CommonActivity>(context);
     }
 
     protected void setState(ServiceState state) {
         activity.get().changeProgress(state);
     }
 
     protected void setState(ServiceState state, int messageCode) {
         activity.get().changeProgress(state, activity.get().getString(messageCode));
     }
 
     protected void setState(ServiceState state, String message) {
         activity.get().changeProgress(state, message);
     }
 
     protected void setState(ServiceState state, Throwable error) {
 
         String message = error.getMessage() == null ? activity.get().getString(R.string.error_connection) : error.getMessage();
         //TODO more detailed error handling
         if (error instanceof RestClientException) {
 
             if (error instanceof HttpClientErrorException) {
                 HttpStatus status = ((HttpClientErrorException) error).getStatusCode();
 
                 switch (status) {
                    case SERVICE_UNAVAILABLE:
                        message = activity.get().getString(R.string.error_http_503);
                         break;
                     case NOT_FOUND:
                         message = activity.get().getString(R.string.error_http_404);
                         break;
                 }
             } else {

                 error = ((RestClientException) error).getRootCause();
                 message = error.getMessage() == null ? activity.get().getString(R.string.error_connection) : error.getMessage();
                 if (message.contains("EHOSTUNREACH"))
                     message = activity.get().getString(R.string.error_host_unreach);
                 else if (message.contains("ECONNREFUSED"))
                     message = activity.get().getString(R.string.error_host_con_refused);
                 else if (message.contains("ETIMEDOUT"))
                     message = activity.get().getString(R.string.error_host_timeout);
             }

         }

         activity.get().changeProgress(state, message);
     }
 
     public void setActivity(CommonActivity activity) {
         this.activity = new SoftReference<CommonActivity>(activity);
     }
 
     protected SharedPreferences getCredentials() {
         return activity.get().getSharedPreferences(Values.PREFS_CREDENTIALS, Context.MODE_PRIVATE);
     }
 }
