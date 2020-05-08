 package com.coffeeandpower.utils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.RelativeLayout;
 import android.widget.ToggleButton;
 
 import com.coffeeandpower.AppCAP;
 import com.coffeeandpower.Constants;
 import com.coffeeandpower.R;
 import com.coffeeandpower.activity.ActivityEnterInviteCode;
 import com.coffeeandpower.activity.ActivitySettings;
 import com.coffeeandpower.cont.DataHolder;
 import com.coffeeandpower.inter.TabMenu;
 import com.coffeeandpower.inter.UserMenu;
 import com.coffeeandpower.tab.activities.ActivityCheckInList;
 import com.coffeeandpower.tab.activities.ActivityContacts;
 import com.coffeeandpower.tab.activities.ActivityPeopleAndPlaces;
 import com.coffeeandpower.views.CustomDialog;
 
 public class UserAndTabMenu implements UserMenu, TabMenu {
 
     public static final int HANDLE_CHECK_OUT = 1800;
     public static final int HANDLE_LOG_OUT = 1801;
     public static final int HANDLE_GET_NOTIFICATION_SETTINGS = 1802;
 
     private ProgressDialog progress;
     private ToggleButton toggle;
     private Button btnFrom;
 
     private Context context;
     private DataHolder result;
     private DataHolder resultNotificationSettings;
 
     public interface OnUserStateChanged {
         public void onCheckOut();
 
         public void onLogOut();
     }
 
     OnUserStateChanged userState = new OnUserStateChanged() {
         @Override
         public void onCheckOut() {
         }
 
         public void onLogOut() {
         }
     };
 
     public void setOnUserStateChanged(OnUserStateChanged userState) {
         this.userState = userState;
     }
 
     public UserAndTabMenu(Context context) {
         this.context = context;
         this.progress = new ProgressDialog(context);
 
         // If user is not logged in
         if (!AppCAP.isLoggedIn()) {
             View v = ((Activity) context).findViewById(R.id.btn_menu);
             RelativeLayout r = (RelativeLayout) ((Activity) context)
                     .findViewById(R.id.rel_log_in);
             RelativeLayout r1 = (RelativeLayout) ((Activity) context)
                     .findViewById(R.id.rel_contacts);
 
             if (v != null) {
                 v.setVisibility(View.GONE);
             }
             if (r != null) {
                 r.setVisibility(View.VISIBLE);
             }
             if (r1 != null) {
                 r1.setVisibility(View.GONE);
             }
         }
     }
 
     private Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
 
             progress.dismiss();
 
             switch (msg.what) {
             case AppCAP.HTTP_ERROR:
                 new CustomDialog(context, "Error", result.getResponseMessage())
                         .show();
                 break;
 
             case HANDLE_CHECK_OUT:
                 AppCAP.getCounter().checkOutTrigger();
                 userState.onCheckOut();
                 break;
 
             case HANDLE_GET_NOTIFICATION_SETTINGS:
                 if (resultNotificationSettings.getObject() != null
                         && resultNotificationSettings.getObject() instanceof Object[]) {
                     Object[] obj = (Object[]) resultNotificationSettings
                             .getObject();
 
                     String pushDistance = (String) obj[0];
                     String checkedInOnly = (String) obj[1];
 
                     AppCAP.setNotificationFrom(pushDistance.equals("city") ? "in city"
                             : "in venue");
                     AppCAP.setNotificationToggle(checkedInOnly.equals("1"));
 
                     if (toggle != null && btnFrom != null) {
                         if (Constants.debugLog)
                             Log.d("LOG", "text: " + pushDistance + ":"
                                     + checkedInOnly);
                         toggle.setChecked(checkedInOnly.matches("1"));
                         btnFrom.setText(pushDistance.matches("venue") ? "in venue"
                                 : "in city");
                     }
                 }
                 break;
 
             }
         }
 
     };
 
     @Override
     public void onClickMap(View v) {
         // Intent intent = new Intent(context, ActivityMap.class);
         // context.startActivity(intent);
     }
 
     @Override
     public void onClickPlaces(View v) {
         double[] data = new double[6];
         data = AppCAP.getUserCoordinates();
 
         Intent intent = new Intent(context, ActivityPeopleAndPlaces.class);
         intent.putExtra("sw_lat", data[0]);
         intent.putExtra("sw_lng", data[1]);
         intent.putExtra("ne_lat", data[2]);
         intent.putExtra("ne_lng", data[3]);
         intent.putExtra("user_lat", data[4]);
         intent.putExtra("user_lng", data[5]);
         intent.putExtra("from", "from_tab");
         intent.putExtra("type", "place");
         context.startActivity(intent);
 
     }
 
     @Override
     public void onClickCheckIn(View v) {
         if (AppCAP.isUserCheckedIn()) {
             AlertDialog.Builder builder = new AlertDialog.Builder(context);
             builder.setTitle("Check Out");
             builder.setMessage("Are you sure you want to be checked out?")
                     .setCancelable(false)
                     .setPositiveButton("Check Out",
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,
                                         int id) {
                                     progress.setMessage("Checking out...");
                                     progress.show();
                                     AppCAP.setUserCheckedIn(false);
                                     new Thread(new Runnable() {
                                         @Override
                                         public void run() {
                                             result = AppCAP.getConnection()
                                                     .checkOut();
                                             handler.sendEmptyMessage(result
                                                     .getHandlerCode());
                                         }
                                     }).start();
                                 }
                             })
                     .setNegativeButton("Cancel",
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,
                                         int id) {
                                     dialog.cancel();
                                 }
                             });
             AlertDialog alert = builder.create();
             alert.show();
 
         } else {
             double[] data = new double[6];
             data = AppCAP.getUserCoordinates();
 
             double userLat = data[4];
             double userLng = data[5];
 
             if (userLat != 0 && userLng != 0) {
                AppCAP.getCounter().refreshAllData();
                 Intent intent = new Intent(context, ActivityCheckInList.class);
                 context.startActivity(intent);
             }
         }
     }
 
     @Override
     public void onClickPeople(View v) {
         double[] data = new double[6];
         data = AppCAP.getUserCoordinates();
 
         Intent intent = new Intent(context, ActivityPeopleAndPlaces.class);
         intent.putExtra("sw_lat", data[0]);
         intent.putExtra("sw_lng", data[1]);
         intent.putExtra("ne_lat", data[2]);
         intent.putExtra("ne_lng", data[3]);
         intent.putExtra("user_lat", data[4]);
         intent.putExtra("user_lng", data[5]);
         intent.putExtra("from", "from_tab");
         intent.putExtra("type", "people");
         context.startActivity(intent);
     }
 
     @Override
     public void onClickContacts(View v) {
         Intent intent = new Intent(context, ActivityContacts.class);
         context.startActivity(intent);
     }
 
     @Override
     public void onClickEnterInviteCode(View v) {
         Intent intent = new Intent(context, ActivityEnterInviteCode.class);
         context.startActivity(intent);
     }
 
     @Override
     public void onClickSettings(View v) {
         Intent intent = new Intent(context, ActivitySettings.class);
         context.startActivity(intent);
     }
 
     @Override
     public void onClickLogout(View v) {
         AppCAP.setLoggedInUserId(0);
         AppCAP.setLocalUserPhotoLargeURL("");
         AppCAP.setLocalUserPhotoURL("");
         AppCAP.setLoggedInUserNickname("");
         AppCAP.setShouldFinishActivities(true);
         userState.onLogOut();
     }
 
     /**
      * Toggle Button listener and checker
      * 
      * @param toggle
      */
     public void setOnNotificationSettingsListener(final ToggleButton toggle,
             final Button btnFrom,
             boolean allowGetNotificationSettingsFromInternet) {
         this.toggle = toggle;
         this.btnFrom = btnFrom;
 
         // Get notification settings
         if (allowGetNotificationSettingsFromInternet) {
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     resultNotificationSettings = AppCAP.getConnection()
                             .getNotificationSettings();
                     handler.sendEmptyMessage(HANDLE_GET_NOTIFICATION_SETTINGS);
                 }
             }).start();
         }
 
         // Check Toggle State
         toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView,
                     final boolean isChecked) {
                 AppCAP.setNotificationToggle(isChecked);
 
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         AppCAP.getConnection().setNotificationSettings(
                                 AppCAP.getPushDistance(), isChecked);
                     }
                 }).start();
             }
         });
 
         // Button From onClickListener
         final CharSequence[] data = { "City", "Place" };
 
         btnFrom.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Show dialog for pending Jobs
                 new AlertDialog.Builder(context)
                         .setTitle("Show me new check-ins from:")
                         .setSingleChoiceItems(
                                 data,
                                 AppCAP.getNotificationFrom().equals("in city") ? 0
                                         : 1,
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog,
                                             int whichButton) {
                                         if (whichButton != -1) {
                                             AppCAP.setPushDistance(whichButton == 1 ? "venue"
                                                     : "city");
                                             AppCAP.setNotificationFrom(whichButton == 1 ? "in venue"
                                                     : "in city");
 
                                             btnFrom.setText(whichButton == 1 ? "in venue"
                                                     : "in city");
                                             new Thread(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     AppCAP.getConnection()
                                                             .setNotificationSettings(
                                                                     AppCAP.getPushDistance(),
                                                                     toggle.isChecked());
                                                 }
                                             }).start();
                                         }
                                         dialog.dismiss();
                                     }
                                 })
                         .setNegativeButton("Cancel",
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog,
                                             int whichButton) {
                                         dialog.cancel();
                                     }
                                 }).show();
             }
         });
     }
 
 }
