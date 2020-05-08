 package ru.tulupov.nsuconnect.helper;
 
 
 import android.content.Context;
 import android.content.Intent;
 
 import ru.tulupov.nsuconnect.activity.HomeActivity;
 
 public class IntentActionHelper {
 
     public static final Intent getHomeIntent(Context context) {
         return new Intent(context, HomeActivity.class);
     }
 }
