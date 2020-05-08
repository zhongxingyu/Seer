 package net.kjonigsen.forcedockrotation;
 
 import android.app.UiModeManager;
 import android.content.Context;
 import android.content.Intent;
import android.content.IntentFilter;
 import android.content.res.Configuration;
import android.os.BatteryManager;
 
 import java.util.zip.DeflaterInputStream;
 
 /**
  * Created by jostein on 29/06/13.
  */
 public class DeviceStatus {
 
     private final static String POWER_DISCONNECTED = "android.intent.action.ACTION_POWER_DISCONNECTED";
     private final static String POWER_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED";
 
 
     public final boolean IsDocked;
 
     public DeviceStatus(boolean isDocked)
     {
         IsDocked = isDocked;
     }
 
     public static DeviceStatus fromSystem(android.content.Context context)
     {
//        UiModeManager manager = getUiModeManager(context);
//        int modeType = manager.getCurrentModeType();
//        boolean docked = (modeType == Configuration.UI_MODE_TYPE_DESK);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
        Intent dockStatus = context.registerReceiver(null, ifilter);

        int dockState = dockStatus.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
        boolean isDocked = dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;

        return new DeviceStatus(isDocked);
     }
 
     public static DeviceStatus fromIntent(Intent intent, Context context) {
         Context appContext = context.getApplicationContext();
         String action = intent.getAction();
 
         Boolean enterDeskMode = UiModeManager.ACTION_ENTER_DESK_MODE.equals(action);
         if (enterDeskMode)
         {
             return new DeviceStatus(true);
         }
 
         Boolean exitDeskMode = UiModeManager.ACTION_EXIT_DESK_MODE.equals(action);
         if (exitDeskMode)
         {
             return new DeviceStatus(false);
         }
 
         Boolean powerConnected = POWER_CONNECTED.equals(action);
         if (powerConnected)
         {
             return new DeviceStatus(true);
         }
 
         Boolean powerDisconnected = POWER_DISCONNECTED.equals(action);
         if (powerDisconnected)
         {
             return new DeviceStatus(false);
         }
 
         return fromSystem(context);
     }
 
     private static UiModeManager getUiModeManager(android.content.Context context)
     {
         UiModeManager manager = (UiModeManager)context.getSystemService("uimode");
         return manager;
     }
 }
