 package ru.tulupov.nsuconnect.protocol;
 
 
 import android.content.Context;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 
 import ru.tulupov.nsuconnect.model.Chat;
 import ru.tulupov.nsuconnect.model.RequestSession;
 import ru.tulupov.nsuconnect.model.Status;
 import ru.tulupov.nsuconnect.model.User;
 
 
 public interface CommandContext {
     Context getApplicationContext();
 
     Status getStatus();
 
     Chat getChat();
 
     User getAnonymousUser();
 
     User getSystemUser();
 
     User getCurrentUser();
 
     RequestQueue getRequestQueue();
 
     RequestSession getRequestSession();
 
     Response.ErrorListener getErrorListener();
 
    Session getSession();

 }
