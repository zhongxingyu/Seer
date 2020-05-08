 package org.booncode.android.loco;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.app.AlarmManager;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Binder;
 import android.os.PowerManager;
 import android.os.SystemClock;
 import android.telephony.SmsManager;
 import android.telephony.TelephonyManager;
 import android.telephony.NeighboringCellInfo;
 import android.telephony.gsm.GsmCellLocation;
 import android.telephony.cdma.CdmaCellLocation;
 import android.util.Log;
 import java.lang.*;
 import java.util.List;
 import java.util.Vector;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.regex.*;
 
 
 public class Stalker extends Service implements LocationListener
 {
   public static final String EXTRA_KEY_CMD = "cmd";
   public static final String EXTRA_KEY_NUMBER = "number";
   public static final String EXTRA_KEY_MESSAGE = "msg";
   
   public static final int CMD_ILLEGAL = -1;
   public static final int CMD_LOCATE = 1;
   public static final int CMD_RECEIVE_RESULT_POSITION = 2;
   public static final int CMD_RECEIVE_RESULT_CELLS = 3;
   public static final int CMD_WAKE = 4;
   
   /*! \brief After this period of time, locating will return the location
    * 
    *  If the location couldn't be found tue to disabled internet connection
    *  the cell ids will be returned. If this isn't possible, a message
    *  should be written to the address the request came from.
    * */
   protected static final long LOCATING_TIMEOUT = 1000 * 60;
   
   //! \brief After this period of time locating will be shut off.
   protected static final long LOCATING_SHUTOFF_TIME = 3 * LOCATING_TIMEOUT + 1000 * 60 * 5;
   //! \brief Maximum time a location is valid (Currently 1 minute).
   protected static final long LOCATING_MAX_AGE = 2 * 60 * 1000;
   
   protected static final long WAKE_UP_TIMEOUT_FAST = 5 * 1000;
   protected static final long WAKE_UP_TIMEOUT_SLOW = 30 * 1000;
   
   protected static final int MAX_NOTIFY_ID_COUNT = 20;
   
   protected static final String NOTIFY_TITLE = "Stalker";
   protected static final String POS_NOTIFY_TEXT = "Position of %s";
   protected static final String POS_NOTIFY_TICKER = "Stalked %s";
   
   protected static final String CELL_NOTIFY_TEXT = "Cell-id of %s";
   protected static final String CELL_NOTIFY_TICKER = "Got Cell-id of %s";
   
   protected static final String TAG = "loco.Stalker";
   protected static final Pattern RE_GEO = Pattern.compile("^\\s*(\\d+.\\d*)\\s*,\\s*(\\d+.\\d*)\\s*$");
  protected static final Pattern RE_GSM_CELL = Pattern.compile("^-gsm:\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*$");
   
  protected static final Pattern RE_CDMA_CELL = Pattern.compile("^-cdma:\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*$");
   protected static final String POSITION_SMS = MsgReceiver.LOCO_CMD_VIEW_POSITION + "%s, %s";
   
   protected final IBinder         m_binder = new PrivateBinder();
   protected LocationManager       m_loc_man;
   protected AlarmManager          m_alarm_man;
   protected NotificationManager   m_notify_man;
   protected TelephonyManager      m_tel_man;
   protected StalkerDatabase       m_db;
   protected ApplicationSettings   m_settings;
   protected PowerManager.WakeLock m_lock;
   protected List<LocRequest>      m_requests = new LinkedList<LocRequest>();
   protected Location              m_best_location = null;
   protected PendingIntent         m_wake_intent;
   protected long                  m_loc_shutoff_time;
   protected boolean               m_loc_is_listening;
   protected int                   m_current_notify_id = 0;
   
   
   public class PrivateBinder extends Binder
   {
     
     Stalker getStalker()
     {
       return Stalker.this;
     }
   }
   
   protected class LocRequest
   {
     public String number;
     public long   timeout;
     
     public LocRequest(String num)
     {
       this.number = num;
       this.timeout = SystemClock.elapsedRealtime() + LOCATING_TIMEOUT;
     }
   }
   
   @Override
   public void onCreate()
   {
     m_loc_man = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
     m_alarm_man = (AlarmManager)getSystemService(ALARM_SERVICE);
     m_notify_man = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
     m_tel_man = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
     
     m_db = new StalkerDatabase(getApplicationContext());
     
     PowerManager power_man = (PowerManager)getSystemService(POWER_SERVICE);
     m_lock = power_man.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
     
     Intent intent = new Intent(this, Stalker.class);
     intent.putExtra(EXTRA_KEY_CMD, CMD_WAKE);
     m_wake_intent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
     m_loc_shutoff_time = SystemClock.elapsedRealtime();
     
     m_settings = new ApplicationSettings(this);
     m_current_notify_id = m_settings.getNotifyID();
   }
     
   @Override
   public void onLocationChanged(Location location)
   {
     Log.d(TAG, String.format("Location update: lat=%s, long=%s",
         String.valueOf(location.getLatitude()),
         String.valueOf(location.getLongitude())));
     
     if (m_best_location != null)
     {
       // m_best_location is too old
       boolean old_outdated = (m_best_location.getTime() + LOCATING_MAX_AGE < location.getTime());
       
       boolean old_accuracy = m_best_location.hasAccuracy();
       boolean new_accuracy = location.hasAccuracy();
       
       // location accuracy isn't worse than old one.
       boolean accuracy_not_worse;
       
       // check accuracy:
       if (old_accuracy && new_accuracy)
       {
         accuracy_not_worse = (location.getAccuracy() <= m_best_location.getAccuracy());
       }
       else if(old_accuracy)
       {
         accuracy_not_worse = false;
       }
       else
       {
         accuracy_not_worse = true;
       }
       
       // decide:
       if (old_outdated || accuracy_not_worse)
       {
         Log.d(TAG, "Replacing best-location...");
         m_best_location = location;
       }
       else
       {
         Log.d(TAG, "Keep old location...");
       }
     }
     else
     {
       Log.d(TAG, "Initializing best-location");
       m_best_location = location;
     }
   }
   
   @Override
   public void onProviderDisabled(String provider)
   {
     Log.d(TAG, String.format("Provider %s disabled", provider));
   }
   
   @Override
   public void onProviderEnabled(String provider)
   {
     Log.d(TAG, String.format("Provider %s enabled", provider));
   }
   
   @Override
   public void onStatusChanged(String provider, int status, Bundle extras)
   {
     Log.d(TAG, String.format("Provider %s status changed: %d", provider, status));
   }
   
   private void enableLocationListener()
   {
     if (!m_loc_is_listening)
     {
       Log.d(TAG, "Turning on LocationListener");
       m_loc_man.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.0f, this);
       m_loc_man.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, this);
       m_loc_is_listening = true;
     }
   }
   
   private void disableLocationListener()
   {
     if (m_loc_is_listening)
     {
       Log.d(TAG, "Turning off LocationListener");
       m_loc_man.removeUpdates(this);
       m_loc_is_listening = false;
     }
   }
   
   protected void releaseLock()
   {
     if (m_lock.isHeld())
     {
       Log.d(TAG, "Release WakeLock...");
       m_lock.release();
     }
     else
     {
       Log.d(TAG, "Request to release WakeLock (has not been acquired)...");
     }
   }
   
   protected void acquireLock()
   {
     if (!m_lock.isHeld())
     {
       Log.d(TAG, "Aquire WakeLock...");
       m_lock.acquire();
     }
     else
     {
       Log.d(TAG, "Request to acquire WakeLock (but has already been done) ...");
     }
   }
   
   protected void startLocating(Bundle bundle)
   {
     String number = bundle.getString(EXTRA_KEY_NUMBER);
     
     if (number != null)
     {
       if (m_db.isAuthorisedNumber(number))
       {
         LocRequest req = new LocRequest(number);
         enableLocationListener();
         m_requests.add(req);
         m_loc_shutoff_time = SystemClock.elapsedRealtime() + LOCATING_SHUTOFF_TIME;
         setNextAlarm(true);
       }
       else
       {
         Log.w(TAG, "Unauthorised Request " + number);
       }
       
     }
     else
     {
       Log.w(TAG, "Couldn't start locating -> number == null");
     }
   }
   
   private void setNextAlarm(boolean fast)
   {
     long next_time = SystemClock.elapsedRealtime();
     
     if (fast)
     {
       next_time += WAKE_UP_TIMEOUT_FAST;
       Log.d(TAG, String.format("Setting up next alarm (+%d)", WAKE_UP_TIMEOUT_FAST));
     }
     else
     {
       next_time += WAKE_UP_TIMEOUT_SLOW;
       Log.d(TAG, String.format("Setting up next alarm (+%d)", WAKE_UP_TIMEOUT_SLOW));
     }
     
     m_alarm_man.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, next_time, m_wake_intent);
   }
   
   private boolean isLocationUseable()
   {
     if (m_best_location != null)
     {
       if (m_best_location.getTime() + LOCATING_MAX_AGE >= System.currentTimeMillis())
       {
         return true;
       }
     }
     
     return false;
   }
   
   private void respondPosition(String number)
   {
     if (isLocationUseable())
     {
       String latitude = Double.toString(m_best_location.getLatitude());
       String longitude = Double.toString(m_best_location.getLongitude());
       m_db.increaseSMSCount(number);
       
       Utils.sendSMS(number, String.format(POSITION_SMS, latitude, longitude));
     }
     else
     {
       String data = null;
       try
       {
         GsmCellLocation gsm_loc = (GsmCellLocation)m_tel_man.getCellLocation();
         String net_op = m_tel_man.getNetworkOperator();
         String mcc = net_op.substring(0, 3);
         String mnc = net_op.substring(3);
         Log.d(TAG, String.format("GSM-Cell: mcc=%s mnc=%s cid=%d lac=%d",
             mcc, mnc, gsm_loc.getCid(), gsm_loc.getLac()));
         
         data = String.format("%s-gsm: %s, %s, %d, %d", MsgReceiver.LOCO_CMD_VIEW_CELLS,
             mcc, mnc, gsm_loc.getCid(), gsm_loc.getLac());
       }
       catch(Exception ex)
       {
         Log.d(TAG, "Couldn't retrieve gsm-cell location", ex);
       }
       
       try
       {
         CdmaCellLocation cdma_loc = (CdmaCellLocation)m_tel_man.getCellLocation();
         Log.d(TAG, String.format("CDMA-Cell: bid=%d blat=%d blong=%d nid=%d sid=%d", 
             cdma_loc.getBaseStationId(), cdma_loc.getBaseStationLatitude(),
             cdma_loc.getBaseStationLongitude(), cdma_loc.getNetworkId(),
             cdma_loc.getSystemId()));
         data = String.format("%s-cdma: %d, %d, %d, %d, %d", MsgReceiver.LOCO_CMD_VIEW_CELLS,
             cdma_loc.getBaseStationId(), cdma_loc.getBaseStationLatitude(),
             cdma_loc.getBaseStationLongitude(), cdma_loc.getNetworkId(),
             cdma_loc.getSystemId());
       }
       catch(Exception ex)
       {
         Log.d(TAG, "Couldn't retrieve cdma-cell-data", ex);
       }
       
       if (data != null)
       {
         m_db.increaseSMSCount(number);
         Utils.sendSMS(number, data);
       }
       else
       {
         Log.w(TAG, "Couldn't retrieve cell-information...");
       }
     }
   }
   
   private int handleRequests()
   {
     long current_time = SystemClock.elapsedRealtime();
     ListIterator<LocRequest> iter = m_requests.listIterator();
     
     Log.d(TAG, String.format("Entering handleRequests: size=%d", m_requests.size()));
     
     while(iter.hasNext())
     {
       LocRequest req = iter.next();
       if (req.timeout <= current_time)
       {
         respondPosition(req.number);
         iter.remove();
       }
     }
     
     int size = m_requests.size();
     Log.d(TAG, String.format("Leaving handleRequests: size=%d", size));
     return size;
   }
   
   protected void wakeStalker()
   {
     Log.d(TAG, "wake Stalker...");
     
     int open_reqs = handleRequests();
     long timestamp = SystemClock.elapsedRealtime();
     
     if ((open_reqs <= 0) && (m_loc_shutoff_time < timestamp))
     {
       // no alarm; stop service...
       Log.d(TAG, "Stopping alarm -> all work is done...");
       shutdownEventually();
     }
     else
     {
       Log.d(TAG, String.format("Current Timestamp %d, Timeout: %d", timestamp, m_loc_shutoff_time));
       setNextAlarm((open_reqs > 0));
     }
   }
   
   private int increaseNotifyID()
   {
     m_current_notify_id = (m_current_notify_id + 1) % MAX_NOTIFY_ID_COUNT;
     m_settings.setNotifyID(m_current_notify_id);
     return m_current_notify_id;
   }
   
   private void addPositionNotify(StalkerDatabase.Person person, String geodata)
   {
     final int icon = R.drawable.prog_icon;
     Uri uri = Uri.parse(geodata);
     Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
     PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, 0);
     Notification notify = new Notification(icon,
                                            String.format(POS_NOTIFY_TICKER, person.name),
                                            System.currentTimeMillis());
     
     notify.setLatestEventInfo(this, NOTIFY_TITLE,
                               String.format(POS_NOTIFY_TEXT, person.name),
                               pintent);
     notify.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
     m_notify_man.notify(increaseNotifyID(), notify);
   }
   
   protected void notifyPosition(Bundle bundle)
   {
     String msg = bundle.getString(EXTRA_KEY_MESSAGE);
     String number = bundle.getString(EXTRA_KEY_NUMBER);
     
     if ((msg != null) && (number != null))
     {
       Matcher m = RE_GEO.matcher(msg);
       StalkerDatabase.Person person = m_db.getPersonFromNumber(number);
       if (m.matches() && (person != null))
       {
         MatchResult result = m.toMatchResult();
         String geo = Utils.formatGeoData(result.group(1), result.group(2), person.name);
         addPositionNotify(person, geo);
       }
       else
       {
         if(person == null)
         {
           Log.w(TAG, String.format("notifyPosition: Number (%s) not in database", number));
         }
         else
         {
           Log.w(TAG, String.format("notifyPosition: Msg is wrong (%s) %s", number, msg));
         }
       }
     }
     else
     {
       Log.d(TAG, "notifyPosition failed: missing bundle-keys...");
     }
   }
   
   private void addGsmCellNotify(StalkerDatabase.Person person, String[] info)
   {
     final int icon = R.drawable.prog_icon;
     Intent intent = new Intent(this, GsmCellActivity.class);
     intent.putExtra(GsmCellActivity.EXTRA_KEY_GSM_DATA, info);
     intent.putExtra(GsmCellActivity.EXTRA_SHOW_NAME, person.name);
     intent.putExtra(GsmCellActivity.EXTRA_SHOW_NUMBER, person.number);
     PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, 0);
     Notification notify = new Notification(icon,
                                            String.format(CELL_NOTIFY_TICKER, person.name),
                                            System.currentTimeMillis());
     
     notify.setLatestEventInfo(this, NOTIFY_TITLE,
                               String.format(CELL_NOTIFY_TEXT, person.name),
                               pintent);
     notify.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
     m_notify_man.notify(increaseNotifyID(), notify);
   }
   
   private void addCdmaCellNotify(StalkerDatabase.Person person, String[] info)
   {
     final int icon = R.drawable.prog_icon;
     Intent intent = new Intent(this, CdmaCellActivity.class);
     intent.putExtra(CdmaCellActivity.EXTRA_KEY_CDMA_DATA, info);
     intent.putExtra(CdmaCellActivity.EXTRA_SHOW_NAME, person.name);
     intent.putExtra(CdmaCellActivity.EXTRA_SHOW_NUMBER, person.number);
     PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, 0);
     Notification notify = new Notification(icon,
                                            String.format(CELL_NOTIFY_TICKER, person.name),
                                            System.currentTimeMillis());
     
     notify.setLatestEventInfo(this, NOTIFY_TITLE,
                               String.format(CELL_NOTIFY_TEXT, person.name),
                               pintent);
     notify.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
     m_notify_man.notify(increaseNotifyID(), notify);
   }
   
   protected void notifyCell(Bundle bundle)
   {
     String msg = bundle.getString(EXTRA_KEY_MESSAGE);
     String number = bundle.getString(EXTRA_KEY_NUMBER);
     
     if ((msg != null) && (number != null))
     {
       Matcher match_gsm = RE_GSM_CELL.matcher(msg);
       Matcher match_cdma = RE_CDMA_CELL.matcher(msg);
       StalkerDatabase.Person person = m_db.getPersonFromNumber(number);
       
       if (person != null)
       {
         if (match_gsm.matches())
         {
           MatchResult result = match_gsm.toMatchResult();
           addGsmCellNotify(person, new String[]{result.group(1), result.group(2),
               result.group(3), result.group(4)});
         }
         else if(match_cdma.matches())
         {
           MatchResult result = match_cdma.toMatchResult();
           addCdmaCellNotify(person, new String[]{result.group(1), result.group(2),
               result.group(3), result.group(4), result.group(5)});
         }
         else
         {
           Log.w(TAG, String.format("notifyCell: Number (%s) illegal command: %s", number, msg));
         }
       }
       else
       {
         Log.w(TAG, String.format("notifyCell: Number (%s) not in database", number));
       }
       
     }
     else
     {
       Log.d(TAG, "notifyCells failed: no message or number...");
     }
   }
   
   private boolean shutdownEventually()
   {
     if ((m_requests.size() <= 0) && (m_loc_shutoff_time < SystemClock.elapsedRealtime()))
     {
       disableLocationListener();
       Log.d(TAG, "Stopping Stalker...");
       this.stopSelf();
       return true;
     }
     else
     {
       Log.d(TAG, "Request to stop stalker (but not finished yet)...");
       return false;
     }
   }
   
   @Override
   public IBinder onBind(Intent intent)
   {
     return m_binder;
   }
   
   @Override
   public int onStartCommand(Intent intent, int flags, int startId)
   {
     int cmd = CMD_ILLEGAL;
     
     acquireLock();
     Log.d(TAG, "Stalker::onStartCommand");
     
     Bundle bundle = intent.getExtras();
     if (bundle != null)
     {
       cmd = bundle.getInt(EXTRA_KEY_CMD, CMD_ILLEGAL);
     }
     // else: cmd = CMD_ILLEGAL
     
     switch(cmd)
     {
       case CMD_ILLEGAL:
         shutdownEventually();
         break;
       
       case CMD_LOCATE:
         startLocating(bundle);
         break;
       
       case CMD_RECEIVE_RESULT_POSITION:
         notifyPosition(bundle);
         shutdownEventually();
         break;
       
       case CMD_RECEIVE_RESULT_CELLS:
         notifyCell(bundle);
         shutdownEventually();
         break;
       
       case CMD_WAKE:
         wakeStalker();
         break;
       
       default:
         Log.d(TAG, "Unknown command " + cmd);
         shutdownEventually();
     }
     
     releaseLock();
     
     return START_NOT_STICKY;
   }
   
   @Override
   public void onDestroy()
   {
     Log.d(TAG, "Stalker::onDestroy()");
     disableLocationListener();
     releaseLock();
     m_db.close();
     super.onDestroy();
   }
 }
