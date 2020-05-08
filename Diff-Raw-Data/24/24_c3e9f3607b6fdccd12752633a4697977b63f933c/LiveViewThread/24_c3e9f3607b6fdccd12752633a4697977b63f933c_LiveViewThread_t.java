 /*
  * Workerthread for communication with the LiveView device
  * @author Robert (xperimental@solidproject.de);
  * This file has been changed by Renze Nicolai (RN+)
  * Image file loader was changed by: basty149
  */
 package nl.rnplus.olv.service;
 
 import android.annotation.TargetApi;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.media.AudioManager;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.BatteryManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.view.KeyEvent;
 import nl.rnplus.olv.LiveViewPreferences;
 import nl.rnplus.olv.MainActivity;
 import nl.rnplus.olv.R;
 import nl.rnplus.olv.data.LiveViewDbConstants;
 import nl.rnplus.olv.data.LiveViewDbHelper;
 import nl.rnplus.olv.data.Prefs;
 import nl.rnplus.olv.messages.*;
 import nl.rnplus.olv.messages.calls.*;
 import nl.rnplus.olv.messages.events.CapsResponse;
 import nl.rnplus.olv.messages.events.DeviceStatus;
 import nl.rnplus.olv.messages.events.GetAlert;
 import nl.rnplus.olv.messages.events.Navigation;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.UUID;
 
 @TargetApi(17)
 public class LiveViewThread extends Thread {
 
     /* Service constants */
     private static final String TAG = "LiveViewThread";
     private static final UUID SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
     private static final int SERVICE_NOTIFY = 100;
 
     /* GUI elements */
     private final byte[] lvImage;
     private final byte[] lvImage_announce_android;
     @SuppressWarnings("unused")
 	private final byte[] lvImage_announce_mail;
     private final byte[] lvImage_announce_note;
     private final byte[] lvImage_announce_sms;
     private final byte[] lvImage_menu_battery;
     private final byte[] lvImage_menu_debug;
     @SuppressWarnings("unused")
 	private final byte[] lvImage_menu_gmail;
     private final byte[] lvImage_menu_left;
     private final byte[] lvImage_menu_location;
     @SuppressWarnings("unused")
 	private final byte[] lvImage_menu_mail;
     private final byte[] lvImage_menu_media;
     @SuppressWarnings("unused")
 	private final byte[] lvImage_menu_min;
     private final byte[] lvImage_menu_music;
     private final byte[] lvImage_menu_notebook;
     private final byte[] lvImage_menu_notification;
     private final byte[] lvImage_menu_phone;
     private final byte[] lvImage_menu_right;
     private final byte[] lvImage_menu_sms;
     private final byte[] lvImage_menu_warning;
     private final byte[] lvImage_music_pause;
     private final byte[] lvImage_music_play;
     private final byte[] lvImage_plugin_loading;
 
     
 
     /* Communication */
     private final BluetoothAdapter btAdapter;
     private BluetoothServerSocket serverSocket;
     private long startUpTime;
     private LiveViewService parentService;
     private BluetoothSocket clientSocket;
     private Notification notification;
 
     /* Menu variables */
     private Integer menu_state = 0;
     private byte device_status = 0;
     private byte last_menu_id = 0;
     private byte alertId = 0;
     private byte menu_button_count = 0;
     private byte menu_button_notifications_id = -1;
     private byte menu_button_media_next_id = -1;
     private byte menu_button_media_play_id = -1;
     private byte menu_button_media_previous_id = -1;
     private byte menu_button_findphone_id = -1;
     private byte menu_button_battery_status_id = -1;
     private byte menu_button_plugintest_id = -1;
     private byte menu_button_debug_id = -1;
     private byte menu_button_mediamenu_id = -1;
     private byte menu_button_android_notifications_id = -1;
     private byte menu_button_sms_id = -1;
     private byte menu_button_notes_id = -1;
     
     private byte debugx = 0;
     private byte debugy = 0;
     
     private String PLUGIN_COMMAND = "nl.rnplus.olv.plugin.command";
     private String PLUGIN_EVENT = "nl.rnplus.olv.plugin.event";
 
     private ThreadLocal<DateFormat> sdf = new ThreadLocal<DateFormat>() {
         protected DateFormat initialValue() {
             return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT, Locale.getDefault());
         }
     };
 
     @SuppressWarnings("deprecation") //To support Android 2.x the depricated notification method is still used on low pre-JellyBean devices.
 	public LiveViewThread(LiveViewService parentService) {
         super("LiveViewThread");
         this.parentService = parentService;
         
         int sdk = Build.VERSION.SDK_INT;
 
         //Pending intent 1: Open the mainActivity
         Intent notificationIntent = new Intent(parentService, MainActivity.class);
         PendingIntent pi_content = PendingIntent.getActivity(parentService, 0, notificationIntent, 0);
 
         //Pending intent 2: Find the LiveView
         Intent bci = new Intent(PLUGIN_COMMAND);
         Bundle bcb = new Bundle();
         bcb.putString("command", "vibrate");
         bcb.putInt("delay", 0);
         bcb.putInt("time", 1000);
         long time = System.currentTimeMillis();
         bcb.putLong("timestamp", time);
         bci.putExtras(bcb);
         PendingIntent pi_findliveview = PendingIntent.getBroadcast(parentService, 0, bci, 0);
 
         //Pending intent 3: Open the settings
         Intent i_opensettings = new Intent();
         i_opensettings.setClass(parentService, LiveViewPreferences.class);
         PendingIntent pi_opensettings = PendingIntent.getActivity(parentService, 0, i_opensettings, 0);
 
         if (sdk < Build.VERSION_CODES.JELLY_BEAN) //Use deprecated notification code when running on android versions older than Jelly Bean.
         {
             notification = new Notification(R.drawable.icon, "LiveView connected...", System.currentTimeMillis());
             Context context = parentService.getApplicationContext();
             CharSequence contentTitle = parentService.getString(R.string.app_name);
             CharSequence contentText = parentService.getString(R.string.notify_service_running);
             notification.setLatestEventInfo(context, contentTitle, contentText, pi_content);
         } else {
             CharSequence contentTitle = parentService.getString(R.string.app_name);
             CharSequence contentText = parentService.getString(R.string.notify_service_running);
             //Bitmap icon = BitmapFactory.decodeResource(parentService.getResources(), R.drawable.olv_icon);
             notification = new Notification.Builder(parentService.getApplicationContext())
                     .setContentTitle(contentTitle)
                     .setContentText(contentText)
                     .setSmallIcon(R.drawable.ic_liveview)
                     .setContentIntent(pi_content)
                     .addAction(R.drawable.ic_menu_find, parentService.getString(R.string.notification_findliveview), pi_findliveview)
                     .addAction(R.drawable.ic_menu_manage, parentService.getString(R.string.notification_settings), pi_opensettings)
                     .setOngoing(true)
 					.setWhen(0)
 					.setPriority(Notification.PRIORITY_LOW)
                     .build();
         }
         
         btAdapter = BluetoothAdapter.getDefaultAdapter();
 
         lvImage = loadImageByteArray(parentService, "menu_blank.png");
         lvImage_announce_android = loadImageByteArray(parentService, "announce_android.png");
         lvImage_announce_mail = loadImageByteArray(parentService, "announce_mail.png");
         lvImage_announce_note = loadImageByteArray(parentService, "announce_note.png");
         lvImage_announce_sms = loadImageByteArray(parentService, "announce_sms.png");
         lvImage_menu_battery = loadImageByteArray(parentService, "menu_battery.png");
         lvImage_menu_debug = loadImageByteArray(parentService, "menu_debug.png");
         lvImage_menu_gmail = loadImageByteArray(parentService, "menu_gmail.png");
         lvImage_menu_left = loadImageByteArray(parentService, "menu_left.png");
         lvImage_menu_location = loadImageByteArray(parentService, "menu_location.png");
         lvImage_menu_mail = loadImageByteArray(parentService, "menu_mail.png");
         lvImage_menu_media = loadImageByteArray(parentService, "menu_media.png");
         lvImage_menu_min = loadImageByteArray(parentService, "menu_min.png");
         lvImage_menu_music = loadImageByteArray(parentService, "menu_music.png");
         lvImage_menu_notebook = loadImageByteArray(parentService, "menu_notebook.png");
         lvImage_menu_notification = loadImageByteArray(parentService, "menu_notification.png");
         lvImage_menu_phone = loadImageByteArray(parentService, "menu_phone.png");
         lvImage_menu_right = loadImageByteArray(parentService, "menu_right.png");
         lvImage_menu_sms = loadImageByteArray(parentService, "menu_sms.png");
         lvImage_menu_warning = loadImageByteArray(parentService, "menu_warning.png");
         lvImage_music_pause = loadImageByteArray(parentService, "music_pause.png");
         lvImage_music_play = loadImageByteArray(parentService, "music_play.png");
         lvImage_plugin_loading = loadImageByteArray(parentService, "plugin_loading.png");
 
         menu_button_count = 0;
 
         Prefs prefs = new Prefs(parentService);
         Boolean menu_show_find_my_phone = prefs.getMenuShowFindMyPhone();
         Boolean menu_show_notifications = prefs.getMenuShowNotifications();
         Boolean menu_show_media_next = prefs.getMenuShowMediaNext();
         Boolean menu_show_media_play = prefs.getMenuShowMediaPlay();
         Boolean menu_show_media_previous = prefs.getMenuShowMediaPrevious();
         Boolean menu_show_battery_status = prefs.getMenuShowBatteryStatus();
         Boolean menu_show_mediamenu_status = prefs.getMenuShowMediaMenuStatus();
         if (menu_show_notifications) {
             menu_button_notifications_id = menu_button_count;
             menu_button_count += 1;
             menu_button_android_notifications_id = menu_button_count;
             menu_button_count += 1;
             menu_button_sms_id = menu_button_count;
             menu_button_count += 1;
             menu_button_notes_id = menu_button_count;
             menu_button_count += 1;
         }
         if (menu_show_media_next) {
             menu_button_media_next_id = menu_button_count;
             menu_button_count += 1;
         }
         if (menu_show_media_play) {
             menu_button_media_play_id = menu_button_count;
             menu_button_count += 1;
         }
         if (menu_show_media_previous) {
             menu_button_media_previous_id = menu_button_count;
             menu_button_count += 1;
         }
         if (menu_show_find_my_phone) {
             menu_button_findphone_id = menu_button_count;
             menu_button_count += 1;
         }
         if (menu_show_battery_status) {
             menu_button_battery_status_id = menu_button_count;
             menu_button_count += 1;
         }
         
         if (menu_show_mediamenu_status) {
         	menu_button_mediamenu_id = menu_button_count;
             menu_button_count += 1;
         }
        
         
         menu_button_plugintest_id = menu_button_count;
         menu_button_count += 1;
         /*
         
         menu_button_debug_id = menu_button_count;
         menu_button_count += 1; */
         
         menu_state = 0;
     }
 
     /**
      * This function was added by Basty149
      * Return byte array for the supplied image file
      *
      * @param parentService
      * @param imageFileName Image file name
      * @return byte[] for the image file
      */
     private byte[] loadImageByteArray(LiveViewService parentService, String imageFileName) {
         try {
             InputStream stream = parentService.getAssets().open(imageFileName);
             ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
             byte[] buffer = new byte[1024];
             while (stream.available() > 0) {
                 int read = stream.read(buffer);
                 arrayStream.write(buffer, 0, read);
             }
             stream.close();
             return arrayStream.toByteArray();
         } catch (IOException e) {
             String message = "Error reading icon " + imageFileName + " : " + e.getMessage();
             Log.e(TAG, message);
             throw new RuntimeException(message, e);
         }
     }
 
     /*
      * (non-Javadoc)
      * @see java.lang.Thread#run()
      */
     @Override
     public void run() {
         parentService.startForeground(SERVICE_NOTIFY, notification);
 
         Log.d(TAG, "Starting LiveView thread.");
         startUpTime = System.currentTimeMillis();
         serverSocket = null;
         try {
             Log.d(TAG, "Starting server...");
             serverSocket = btAdapter.listenUsingRfcommWithServiceRecord("LiveView", SERIAL);
         } catch (IOException e) {
             Log.e(TAG, "Error starting BT server: " + e.getMessage());
             return;
         }
 
         try {
             Log.d(TAG, "Listening for LV...");
             clientSocket = serverSocket.accept();
             EventReader reader = new EventReader(clientSocket.getInputStream());
 
             // Single connect only
             serverSocket.close();
             serverSocket = null;
             Log.d(TAG, "LV connected.");
             sendCall(new CapsRequest());
             Log.d(TAG, "Message sent.");
             sendEvent("connectionstatus".toString(), 1, 0, "connected".toString(), null);
             
             device_status = 2; //Bugfix: after connecting device is in menu state.
             
             do {
                 try {
                     LiveViewEvent response = reader.readMessage();
                     sendCall(new MessageAck(response.getId()));
                     Log.d(TAG, "Got message: " + response);
                     processEvent(response);
                 } catch (DecodeException e) {
                     Log.e(TAG, "Error decoding message: " + e.getMessage());
                 }
             } while (true);
         } catch (IOException e) {
             String msg = e.getMessage();
             if (!msg.contains("Connection timed out")) {
                 Log.e(TAG, "Error communicating with LV: " + e.getMessage());
             }
         }
         Log.d(TAG, "Stopped LiveView thread.");
 
         // Log runtime
         long runtime = (System.currentTimeMillis() - startUpTime) / 1000;
         long runHour = runtime / 3600;
         runtime -= runHour * 3600;
         long runMinute = runtime / 60;
         runtime -= runMinute * 60;
         /* String message = String.format(
                 "Service runtime: %d hours %d minutes %d seconds", runHour,
                 runMinute, runtime);
         Log.d(TAG, message);
          */
 
         // Stop surrounding service
         ((NotificationManager) parentService
                 .getSystemService(Context.NOTIFICATION_SERVICE))
                 .cancel(SERVICE_NOTIFY);
         parentService.stopForeground(true);
         parentService.stopSelf();
     }
     
     private byte[] getImageForNotification(int type) {
     	if (type==LiveViewDbConstants.ALERT_ALL) {
     		//type = parentService.getNotificationType(alertId);
     	}
     	if (type==LiveViewDbConstants.ALERT_GENERIC) {
     		return lvImage_announce_android;
     	}
     	if (type==LiveViewDbConstants.ALERT_ANDROID) {
     		return lvImage_announce_android;
     	}
     	if (type==LiveViewDbConstants.ALERT_SMS) {
     		return lvImage_announce_sms;
     	}
     	if (type==LiveViewDbConstants.ALERT_NOTE) {
     		return lvImage_announce_note;
     	}
     	return lvImage_announce_android;
     } //Would like to use current "announce_android" image as "announce_generic" and a different image for android notifications...
 
     /**
      * Process a message that was sent by the LiveView device.
      *
      * @param event Event sent by device.
      * @throws IOException
      */
 
     private void processEvent(LiveViewEvent event) throws IOException {
         Prefs prefs = new Prefs(parentService);
         Boolean enable_media_menu = prefs.getEnableMediaMenu();
         //Log.d(TAG, "OLV will now process an event with id: " + event.getId());
         switch (event.getId()) {
             case MessageConstants.MSG_GETCAPS_RESP:
                 CapsResponse caps = (CapsResponse) event;
                 Log.d(TAG, "LV capabilities: " + caps.toString());
                 Log.d(TAG, "LV Version: " + caps.getSoftwareVersion());
                 if (menu_state == 0) {
                 	if (prefs.getInitialMenuItemId()>menu_button_count) prefs.setInitialMenuItemId(menu_button_count);
                 	if (prefs.getInitialMenuItemId()<0) prefs.setInitialMenuItemId(0);
                 	if (prefs.getMenuVibrationTime()<0) prefs.setMenuVibrationTime(0);
                 	if (prefs.getMenuVibrationTime()>255) prefs.setMenuVibrationTime(255);
                 	sendCall(new SetMenuSettings(prefs.getMenuVibrationTime(), prefs.getInitialMenuItemId()));
                     sendCall(new SetMenuSize(menu_button_count));
                 }
                 sendCall(new SetVibrate(0, 100));
                 break;
             case MessageConstants.MSG_GETTIME:
                 Log.d(TAG, "Sending current time...");
                 sendCall(new GetTimeResponse(prefs.getClockMode()));
                 break;
             case MessageConstants.MSG_DEVICESTATUS:
                 DeviceStatus status = (DeviceStatus) event;
                 Log.d(TAG, "Acknowledging status: " + status.toString());
                 sendCall(new DeviceStatusAck());
                 //menu_state = 0; //Reset menu state when screen turns off
                 device_status = status.getStatus();
                 updateGuiAfterStandby();
                 sendEvent("devicestatus", device_status, 0, null, null);
                 break;
             case MessageConstants.MSG_SETVIBRATE_ACK:
                 Log.d(TAG, "Got setvibrate ack.");
                 break;
             case MessageConstants.MSG_SETLED_ACK:
             	Log.d(TAG, "Got setled ack.");
             	break;
             case MessageConstants.MSG_DISPLAYBITMAP_ACK:
             	Log.d(TAG, "Got displaybitmap ack.");
             	break;	
             case MessageConstants.MSG_DISPLAYPANEL_ACK:
                 Log.d(TAG, "Got display panel ack.");
                 if ((parentService.MediaInfoNeedsUpdate) && (menu_state == 1)) {
                     draw_media_menu();
                     parentService.MediaInfoNeedsUpdate = false;
                 }
                 break;
             case MessageConstants.MSG_GETMENUITEMS:
                 Log.d(TAG, "Sending menu items...");
                 if (menu_state == 0) {
                 	for (byte current_id = 0; current_id<=menu_button_count; current_id++)
                 	{
 	                    if (menu_button_notifications_id == current_id) {
 	                        sendCall(new MenuItem(current_id, true, new UShort((short) (parentService.getNotificationUnreadCount(LiveViewDbConstants.ALERT_ALL))),
 	                                "All notifications", lvImage_menu_notification));
 	                    }
 	                    if (menu_button_media_next_id == current_id) {
 	                        sendCall(new MenuItem(current_id, false, new UShort((short) 0),
 	                                "Next", lvImage_menu_right));
 	                    }
 	                    if (menu_button_media_play_id == current_id) {
 	                        sendCall(new MenuItem(current_id, false, new UShort((short) 0),
 	                                "Play / Pause", lvImage_menu_music));
 	                    }
 	                    if (menu_button_media_previous_id == current_id) {
 	                        sendCall(new MenuItem(current_id, false, new UShort((short) 0),
 	                                "Previous", lvImage_menu_left));
 	                    }
 	                    if (menu_button_findphone_id == current_id) {
 	                        sendCall(new MenuItem(current_id, false, new UShort((short) 0),
 	                                "Find my phone", lvImage_menu_location));
 	                    }
 	                    if (menu_button_battery_status_id == current_id) {
 	                        sendCall(new MenuItem(current_id, false, new UShort((short) (get_battery_status() * 100)),
 	                                "Battery", lvImage_menu_battery));
 	                    }
 	                    if (menu_button_plugintest_id == current_id) {
 	                        sendCall(new MenuItem(current_id, false, new UShort((short) 0),
 	                                "Demo", lvImage_menu_debug));
 	                    }
 	                    if (menu_button_mediamenu_id == current_id) {
 	                        sendCall(new MenuItem(current_id, false, new UShort((short) 0),
 	                                "Media menu", lvImage_menu_media));
 	                    }
 	                    if (menu_button_debug_id == current_id) {
 	                        sendCall(new MenuItem(current_id, false, new UShort((short) 0),
 	                                "Bitmap navigation test", lvImage_menu_debug));
 	                    }
 	                    if (menu_button_android_notifications_id == current_id) {
 	                        sendCall(new MenuItem(current_id, true, new UShort((short) parentService.getNotificationUnreadCount(LiveViewDbConstants.ALERT_ANDROID)),
 	                                "Android notifications", lvImage_menu_warning));
 	                    }
 	                    if (menu_button_sms_id == current_id) {
 	                        sendCall(new MenuItem(current_id, true, new UShort((short) parentService.getNotificationUnreadCount(LiveViewDbConstants.ALERT_SMS)),
 	                                "SMS messages", lvImage_menu_sms));
 	                    }
 	                    if (menu_button_notes_id == current_id) {
 	                        sendCall(new MenuItem(current_id, true, new UShort((short) parentService.getNotificationUnreadCount(LiveViewDbConstants.ALERT_NOTE)),
 	                                "Notes", lvImage_menu_notebook));
 	                    }
                 	}
                     Log.d(TAG, "Menu items sent, menu_state is 0.");
                 } else {
                     Log.d(TAG, "Menu items not sent, not in menu_state 0.");
                 }
                 //sendCall(new MenuItem((byte) 5, true, new UShort((short) 4),
                 //        "Alerts test", menuImage));
                 break;
             case MessageConstants.MSG_GETALERT:
                 GetAlert alert = (GetAlert) event;
                 Log.d(TAG, "Alert triggered (" + alert.getMenuItemId() + ", " + alert.getAlertAction() + ", " + alert.getMaxBodySize() + ")");
                 if (last_menu_id != alert.getMenuItemId()) {
                     alertId = 0;
                 }
                 last_menu_id = alert.getMenuItemId();
                 int type = LiveViewDbConstants.ALERT_ALL;
                 if (last_menu_id==menu_button_android_notifications_id){
                 	type = LiveViewDbConstants.ALERT_ANDROID;
                 }
                 if (last_menu_id==menu_button_sms_id){
                 	type = LiveViewDbConstants.ALERT_SMS;
                 }
                 if (last_menu_id==menu_button_notes_id){
                 	type = LiveViewDbConstants.ALERT_NOTE;
                 }
                 if (alert.getAlertAction() == MessageConstants.ALERTACTION_FIRST) {
                     alertId = 0;
                 }
                 if (alert.getAlertAction() == MessageConstants.ALERTACTION_LAST) {
                 		alertId = (byte) (parentService.getNotificationTotalCount(type) - 1);
                 }
                 if (alert.getAlertAction() == MessageConstants.ALERTACTION_NEXT) {
                     alertId += 1;
                     if (alertId > (byte) parentService.getNotificationTotalCount(type) - 1) {
                         alertId = 0;
                     }
                 }
                 if (alert.getAlertAction() == MessageConstants.ALERTACTION_PREV) {
                     alertId -= 1;
                     if (alertId < 0) {
                         alertId = (byte) (parentService.getNotificationTotalCount(type) - 1);
                     }
                 }
                 Log.d(TAG, "Notifications alert (ID: " + alertId + ") Time:" + parentService.getNotificationTime(alertId, type));
                 if (parentService.getNotificationTotalCount(type) > 0) {
                     String notificationTimeString = sdf.get().format(new Date(parentService.getNotificationTime(alertId, type)));
                     sendCall(new GetAlertResponse((byte) parentService.getNotificationTotalCount(type),
                             (byte) parentService.getNotificationUnreadCount(type), alertId, notificationTimeString,
                             parentService.getNotificationTitle(alertId, type), parentService.getNotificationContent(alertId, type), getImageForNotification(type)));
 
                     //if (parentService.getNotificationType(alertId)==LiveViewDbConstants.ALERT_SMS)
                 } else {
                     sendCall(new GetAlertResponse(0, 0, alertId, "", "No notifications", "", lvImage_announce_android));
                 }
                 break;
             case MessageConstants.MSG_NAVIGATION:
             /* Some random lines of code that I want to remember for later use:
              * sendCall(new SetScreenMode((byte) MessageConstants.BRIGHTNESS_DIM));
         	 * sendCall(new SetLed(Color.GREEN,1,500));
         	 * sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
              */
                 Navigation nav = (Navigation) event;
                 Log.d(TAG, "Received navigation packet (isInAlert: " + nav.isInAlert() + " and getNavType: " + nav.getNavType() + ").");
                 if (nav.isInAlert()) {
                     Log.d(TAG, "User pressed button in alert. Wiping all the notifications from the liveview...");
                 	try {
                 		Prefs pref = new Prefs(parentService);
                 		Boolean removeNotifications = prefs.getWipeNotifications();
                 		if (removeNotifications) {
                 			//ADD THIS AGAIN!
                 			//LiveViewDbHelper.deleteAllNotifications(parentService);
                 		} else {
                 			String notificationContentFilter = pref.getNotificationFilter();
                 			String currentNotificationText = parentService.getNotificationContent(alertId, LiveViewDbConstants.ALERT_ALL);
                 			pref.setNotificationFilter(notificationContentFilter + " " + currentNotificationText);
                 		}
                 	} catch(Exception e) {
                 		String message = "Error while deleting all notifications from the database: "+e.getMessage();
                         Log.e(TAG, message);
                          		
                 	}
                     sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                 } else {
                     switch (menu_state) {
                         case 0:
                             switch (nav.getNavType()) {
                                 case MessageConstants.NAVTYPE_SELECT:
                                     switch (nav.getNavAction()) {
                                         case MessageConstants.NAVACTION_LONGPRESS:
                                             Log.d(TAG, "Long press on select key.");
                                             if (enable_media_menu) {
                                                 sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                                 menu_state = 1;
                                                 draw_media_menu();
                                             }
                                             break;
                                         default:
                                             Log.d(TAG, "Navigation error: unknown action with select button!");
                                             sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                                             break;
                                     }
                                    break;
                                 case MessageConstants.NAVTYPE_MENUSELECT:
                                 	Log.d(TAG, "Menuselect key triggered.");
                                     boolean hasdonesomething = false;
                                     if (nav.getMenuItemId() == menu_button_findphone_id) {
                                         Log.d(TAG, "Find my phone: generating noise and vibration...");
                                         Vibrator v = (Vibrator) parentService.getSystemService(Context.VIBRATOR_SERVICE);
                                         v.vibrate(300);
                                         Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                         Ringtone r = RingtoneManager.getRingtone(parentService.getApplicationContext(), notification);
                                         r.setStreamType(AudioManager.STREAM_ALARM);
                                         r.play();
                                         Log.d(TAG, "Find my phone: done, returning to menu.");
                                         sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                                         hasdonesomething = true;
                                     }
                                     if (nav.getMenuItemId() == menu_button_media_next_id) {
                                         Log.d(TAG, "MEDIA: Next");
                                         emulate_media(KeyEvent.KEYCODE_MEDIA_NEXT);
                                         sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                                         hasdonesomething = true;
                                     }
                                     if (nav.getMenuItemId() == menu_button_media_play_id) {
                                         Log.d(TAG, "MEDIA: Play / Pause");
                                         emulate_media(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                                         sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                                         hasdonesomething = true;
                                     }
                                     if (nav.getMenuItemId() == menu_button_media_previous_id) {
                                         Log.d(TAG, "MEDIA: Previous");
                                         emulate_media(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                                         sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                                         hasdonesomething = true;
                                     }
                                     if (nav.getMenuItemId() == menu_button_battery_status_id) {
                                         Log.d(TAG, "Show battery status.");
                                         sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                         menu_state = 2;
                                         draw_battery_status();
                                         hasdonesomething = true;
                                     }
                                     if (nav.getMenuItemId() == menu_button_plugintest_id) {
                                         Log.d(TAG, "PLUGIN MENU ITEM TEST");
                                         pluginMenuAction(0);
                                         hasdonesomething = true;
                                     }
                                     if (nav.getMenuItemId() == menu_button_debug_id) {
                                         Log.d(TAG, "DEBUG MENU OPENED");
                                         sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                         menu_state = 4;
                                         debug1();
                                         hasdonesomething = true;
                                     }
                                     if (nav.getMenuItemId() == menu_button_mediamenu_id) {
                                         Log.d(TAG, "Media menu button.");
                                         sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                         menu_state = 1;
                                         draw_media_menu();
                                         hasdonesomething = true;
                                     }
                                     if (!hasdonesomething) {
                                         Log.d(TAG, "Navigation error: unknown menu id " + nav.getMenuItemId() + "!");
                                         sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                                     }
                                     break;
                                 case MessageConstants.NAVTYPE_DOWN:
                                     Log.d(TAG, "Down button pressed while in main menu.");
                                     if (prefs.getMenuUpDownAction() == 1) action_volume_down();
                                     if (prefs.getMenuUpDownAction() == 2) sendCall(new SetLed(Color.BLUE, 1, 500));
                                     sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                                     break;
                                 case MessageConstants.NAVTYPE_UP:
                                     Log.d(TAG, "Up button pressed while in main menu.");
                                     if (prefs.getMenuUpDownAction() == 1) action_volume_up();
                                     if (prefs.getMenuUpDownAction() == 2) sendCall(new SetLed(Color.BLUE, 1, 500));
                                     sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                                     break;
                                 default:
                                     String message = "Navigation error: unknown button (" + nav.getNavAction() + ")!";
                                     Log.e(TAG, message);
                                     
                                     sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                                     break;
                             }
                             break;
                         case 1:
                             switch (nav.getNavType()) {
                                 case MessageConstants.NAVTYPE_SELECT:
                                     switch (nav.getNavAction()) {
                                         case MessageConstants.NAVACTION_LONGPRESS:
                                             Log.d(TAG, "Long press on select key while in media menu.");
                                             menu_state = 0;
                                             sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                                             sendCall(new SetMenuSize(menu_button_count));
                                             Log.d(TAG, "Returning to main menu.");
                                             break;
                                         case MessageConstants.NAVACTION_PRESS:
                                             emulate_media(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                                             draw_media_menu();
                                             sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                             Log.d(TAG, "Navigation: Play / pause from media menu.");
                                             break;
                                         default:
                                             String message = "Navigation error: unknown action with select button while in media menu (" + nav.getNavAction() + ")!";
                                             Log.e(TAG, message);
                                             
                                             sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                             break;
                                     }
                                     break;
                                 case MessageConstants.NAVTYPE_UP:
                                     action_volume_up();
                                     sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                     draw_media_menu();
                                     Log.d(TAG, "Navigation: Volume up from media menu.");
                                     break;
                                 case MessageConstants.NAVTYPE_DOWN:
                                     action_volume_down();
                                     sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                     draw_media_menu();
                                     Log.d(TAG, "Navigation: Volume down from media menu.");
                                     break;
                                 case MessageConstants.NAVTYPE_LEFT:
                                     emulate_media(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                                     sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                     draw_media_menu();
                                     Log.d(TAG, "Navigation: Previous track from media menu.");
                                     break;
                                 case MessageConstants.NAVTYPE_RIGHT:
                                     emulate_media(KeyEvent.KEYCODE_MEDIA_NEXT);
                                     sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                     draw_media_menu();
                                     Log.d(TAG, "Navigation: Next track from media menu.");
                                     break;
                                 default:
                                     String message = "Error: Navigation: unknown button while in media menu! (" + nav.getNavType() + ")";
                                     Log.e(TAG, message);
                                     
                                     sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                                     break;
                             }
                             break;
                         case 2:
                             Log.e(TAG, "Menu state 2: Panel with information is visible. Returning to main menu.");
                             menu_state = 0;
                             sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                             sendCall(new SetMenuSize(menu_button_count));
                             break;
                         case 3:
                             Log.e(TAG, "Menu state 3: Plugin");
                             switch (nav.getNavType()) {
                             case MessageConstants.NAVTYPE_SELECT:
                                 switch (nav.getNavAction()) {
                                     case MessageConstants.NAVACTION_LONGPRESS:
                                     	menu_state = 0;
     	                                sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
     	                                sendCall(new SetMenuSize(menu_button_count));
                                     	break;
                                 }
                                 break;
 	                            default:
 	                            	pluginNavigate(nav.getNavType());
 	                            	//sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
 	                            	break;
                             }
                             break; 
                         case 4:
                             Log.e(TAG, "Menu state 4: Debug");
                             switch (nav.getNavType()) {
                             case MessageConstants.NAVTYPE_SELECT:
                                 switch (nav.getNavAction()) {
                                     case MessageConstants.NAVACTION_LONGPRESS:
                                     	menu_state = 0;
     	                                sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
     	                                sendCall(new SetMenuSize(menu_button_count));
                                     	break;
                                 }
                                 break;
                     			case MessageConstants.NAVTYPE_UP:
                     				debug2();
                     				sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                     				break;
                     			case MessageConstants.NAVTYPE_DOWN:
                     				debug3();
                     				sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
                     				break;
 	                    		case MessageConstants.NAVTYPE_LEFT:
 	                    			debug4();
 	                    			sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
 	                    			break;
                         		case MessageConstants.NAVTYPE_RIGHT:
                         			debug5();
                         			sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
 	                            	break;
 	                            default:
 	                            	sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
 	                            	break;
                             }
                             break;    
                         default:
                             String message = "Error: Navigation: unknown menu state!";
                             Log.e(TAG, message);
                             
                             sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                             break;
                     }
                 }
                 break;
             default:
                 String message = "Error: Unknown event (" + event.getId() + ")!";
                 Log.e(TAG, message);
                 
                 sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
                 break;
         }
     }
 
     public void draw_media_menu() throws IOException {
         Log.d(TAG, "Drawing media menu.");
         AudioManager amgr = (AudioManager) parentService.getSystemService(Context.AUDIO_SERVICE);
         boolean playing = amgr.isMusicActive();
         if (playing) {
             Log.d(TAG, "A song is playing.");
             sendCall(new DisplayPanel(parentService.getMediaInfoTrack(), "Artist: " + parentService.getMediaInfoArtist() + " Album: " + parentService.getMediaInfoAlbum(), lvImage_music_pause, false));
             //sendCall(new DisplayBitmap((byte) 34, (byte) 34, menuImage_media_isplaying));
             //sendCall(new DisplayPanel("HEADER", "FOOTER", menuImage_media_isplaying, false));
         } else {
             Log.d(TAG, "Nope, there is no music playing.");
             sendCall(new DisplayPanel("There is no music playing.", "", lvImage_music_play, false));
             //sendCall(new DisplayBitmap((byte) 34, (byte) 34, menuImage_media_isnotplaying));
         }
     }
 
     public void draw_battery_status() throws IOException {
         sendCall(new DisplayPanel("Battery status", (Math.round(get_battery_status() * 100)) + "%", lvImage_menu_battery, false));
     }
     
     public void pluginMenuAction(int menuId) throws IOException {
         sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
         menu_state = 3;
         sendCall(new DisplayPanel("Press buttons!", "", lvImage_menu_music, false));
         sendEvent("menuitem_opened", menuId, 0, "", "");
     }
     
     public void pluginNavigate(byte navAction) throws IOException {
     	sendEvent("navigation", navAction, 0, "", "");
         sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
     }
     
     public void showPanel(String top_string, String bottom_string, boolean isAlert, boolean useImage, byte[] image) throws IOException {
     	byte[] img;
     	if (useImage) {
     		img = image;
     	} else {
     		img = lvImage_menu_warning;
     	}
     	sendCall(new DisplayPanel(top_string, bottom_string, img, isAlert));
     }
     
     public void openMenuFromStandby() throws IOException {
         //sendCall(new DisplayPanel("Please wait...", "Loading...", menuImage_notification, false));
         sendCall(new SetMenuSize(menu_button_count));
     }
     
     public void showIncomingCallScreen(int state, String topline, String bottomline) throws IOException {
     	Prefs prefs = new Prefs(parentService);
     	if (prefs.getEnableIncomingCallNotify())
     	{
 	    	menu_state = 2;
 	    	sendCall(new SetScreenMode((byte) MessageConstants.BRIGHTNESS_MAX));
 	        sendCall(new DisplayPanel(topline, bottomline, lvImage_menu_phone, true));
     	}
     	if (prefs.getEnableIncomingCallVibrate())
     	{
     		sendCall(new SetVibrate(0, 1000));
     	}
     }
 
     public void action_volume_up() {
         AudioManager audioManager = (AudioManager) parentService.getSystemService(Context.AUDIO_SERVICE);
         audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1,
                 AudioManager.FLAG_SHOW_UI);
     }
 
     public void action_volume_down() {
         AudioManager audioManager = (AudioManager) parentService.getSystemService(Context.AUDIO_SERVICE);
         audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 1,
                 AudioManager.FLAG_SHOW_UI);
     }
 
     /**
      * Send a message to the LiveView device if one is connected.
      *
      * @param call {@link LiveViewCall} to send to device.
      * @throws IOException If the message could not be sent successfully.
      */
     void sendCall(LiveViewCall call) throws IOException {
         if (clientSocket == null) {
             throw new IOException("No client connected!");
         } else {
             clientSocket.getOutputStream().write(call.getEncoded());
         }
     }
 
     public void stopLoop() {
         if (serverSocket != null) {
             try {
                 serverSocket.close();
             } catch (IOException e) {
                 String message = "Error while closing server socket: " + e.getMessage();
                 Log.e(TAG, message);
             }
         }
     }
 
     public float get_battery_status() //This function was added by jkorp (user on XDA)
     {
         IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
         Intent batteryStatus = parentService.registerReceiver(null, ifilter);
         int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
         int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
         return level / (float) scale;
     }
     
     public float get_battery_charge_status()
     {
         IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
         Intent batteryStatus = parentService.registerReceiver(null, ifilter);
         int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
         return status;
     }
     
     public float get_battery_voltage()
     {
         IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
         Intent batteryStatus = parentService.registerReceiver(null, ifilter);
         int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
         return voltage;
     }
 
     public void emulate_media(int keycode) {
         Intent buttonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
         buttonIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
         parentService.sendOrderedBroadcast(buttonIntent, null);
         buttonIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keycode));
         parentService.sendOrderedBroadcast(buttonIntent, null);
     }
 
     public boolean isLooping() {
         return serverSocket != null;
     }
 
     public byte getLiveViewStatus() {
         return device_status;
     }
 
     public void sendEvent(String event, int a, int b, String string1, String string2) {
         Intent bci = new Intent(PLUGIN_EVENT);
         Bundle bcb = new Bundle();
         bcb.putString("event", event);
         bcb.putString("plugin", "Here should be the target plugin package name");
         bcb.putInt("p1", a);
         bcb.putInt("p2", b);
         if (string1==null) string1="no value";
         if (string2==null) string2="no value";
         bcb.putString("p3", (String) string1.toString());
         bcb.putString("p4", (String) string2.toString());
         long time = System.currentTimeMillis();
         bcb.putLong("timestamp", time);
         bci.putExtras(bcb);
         parentService.sendBroadcast(bci);
     }
     
     public void showNewAlert(String line1, String line2, int icon_type, byte[] img) {
     	menu_state = 2;	
     	if (icon_type != LiveViewDbConstants.ALERT_PLUGIN_I) {
     		img = getImageForNotification(icon_type-1);
     	}	
         try {
         	sendCall(new DisplayPanel(line1, line2, img, false));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			Log.e(TAG, "Error while showing alert! ("+e.toString()+")");
 			e.printStackTrace();
 		}
     
     }
     
     public void updateGuiAfterStandby(){
 		try {
 			
 		        Prefs prefs = new Prefs(parentService);
 		        Boolean update_unread_count_when_menu_opens = prefs.getUpdateUnreadCountWhenMenuOpens();
 		        Boolean enable_notification_buzzer = prefs.getenablenotificationbuzzer();
 		         
 		        if (update_unread_count_when_menu_opens && (device_status==MessageConstants.DEVICESTATUS_ON)) {		        	
 		        	menu_state = 0; //Because that little piece of code commented out below this does not work.
 		        	
 		        	if (menu_state==0)
 		        	{
 		        		sendCall(new SetMenuSize(menu_button_count));
 		        	}
 		        	//if (menu_state==1)
 		        	//{
 		        		//sendCall(new NavigationResponse(MessageConstants.RESULT_OK));
 		        		//draw_media_menu();
 		        	//}
 		        }
 		        else
 		        {
 		        	menu_state = 0; //Go to the menu without sending anything.
 		        }
 		        
 		        if (enable_notification_buzzer && (parentService.getNotificationUnreadCount(LiveViewDbConstants.ALERT_ALL)>0) && (device_status==MessageConstants.DEVICESTATUS_ON)) {
 		                sendCall(new SetLed(Color.GREEN, 0, 1000));
 		                sendCall(new SetVibrate(0, 200));
 		        }
 	    } catch(Exception e) {
 	        String message = "Error while updating notifications: " + e.getMessage();
 	        Log.e(TAG, message);   	
 	    }
     }
     
     public void debug1() throws IOException {
     	sendCall(new SetMenuSize((byte) 0));
     	debugdraw();
         Log.i(TAG, "DEBUG1");
     }
     public void debug2() throws IOException {
     	debugdraw();
     	debugy-=4;
     	if (debugy<0) debugy = (byte) 0;
         Log.i(TAG, "DEBUG2");
     }
     public void debug3() throws IOException {
     	debugdraw();
     	debugy+=4;
     	if (debugy>128) debugy = (byte) 128;   	
         Log.i(TAG, "DEBUG3");
     }
     public void debug4() throws IOException {
     	debugdraw();
     	debugx-=4;
     	if (debugx<0) debugx = (byte) 0;    	
         Log.i(TAG, "DEBUG4");
     }
     public void debug5() throws IOException {
     	debugdraw();
     	debugx+=4;
     	if (debugx>128) debugx = (byte) 128;
         Log.i(TAG, "DEBUG5");    	
     }
     public void debugdraw() throws IOException {
     	//sendCall(new ClearDisplay());
     	//sendCall(new DisplayBitmap((byte) 0, (byte) 0, bgImage_blank));
     	sendCall(new DisplayBitmap((byte) debugx, (byte) debugy, lvImage));
     }
 }
