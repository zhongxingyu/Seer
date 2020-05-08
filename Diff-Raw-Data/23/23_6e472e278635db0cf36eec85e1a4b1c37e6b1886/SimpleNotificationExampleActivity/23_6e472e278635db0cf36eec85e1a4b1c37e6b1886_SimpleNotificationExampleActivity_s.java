 package com.java.example;
 
 import android.app.Activity;
 import android.os.Bundle;
 import com.java.R;
 import com.java.notification.AppNotifier;
 import com.java.notification.StorageFactory;
 import com.java.notification.content.NotificationContent;
 import com.java.notification.store.IUserPrefStore;
 
 public class SimpleNotificationExampleActivity extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
 
     @Override
     protected void onResume()
     {
         super.onResume();
         showNotification();
     }
 
     private String mTitle = "Title";
     private String mBody = "Body";
     private String mTickerText = "TickerText";
 
     private void showNotification()
     {
        NotificationContent notificationContent = new NotificationContent(mTitle, mBody, mTickerText);
       // notificationContent.setNotificationIcon(((BitmapDrawable)getResources().getDrawable(R.drawable.accounts_cross)).getBitmap());
         IUserPrefStore userPrefStore = new StorageFactory().getUserPrefStore();
       // notificationContent.setAlertPreference(new AlertPreference(userPrefStore.getSoundPreference(), userPrefStore.getVibratePreference()));
         AppNotifier appNotifier = new AppNotifier(getApplicationContext());
         appNotifier.generateMessageNotification(notificationContent);
     }
 }
