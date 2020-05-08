 package org.booncode.android.loco;
 
 import android.content.Context;
 import android.content.Intent;
 import android.app.Service;
 import android.app.PendingIntent;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Binder;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.telephony.SmsManager;
 
 import android.location.LocationManager;
 import android.location.Location;
 import android.location.LocationListener;
 
 import java.util.regex.*;
 
 import android.widget.Toast;
 
 public class Stalker extends Service
 {
   public static final String SETTINGS_FILE = "settings";
   public static final String AUTH_LIST = "auth_list";
   public static final String STALKING_ACTION = "org.booncode.android.loco.Stalker";
   
   private static final Pattern RE_GEO = Pattern.compile("^geo:\\s*(\\d+.\\d*)\\s*,\\s*(\\d+.\\d*)\\s*$");
   
   private final IBinder m_binder = new PrivateBinder();
   
   private SettingsActivity m_activity = null;
   
   
   public class PrivateBinder extends Binder
   {
     
     Stalker getStalker()
     {
       return Stalker.this;
     }
   }
   
   protected class LocateAndSend implements LocationListener
   {
     protected String m_number;
     protected LocationManager m_manager;
     protected boolean m_is_sent = false;
     
     public LocateAndSend(String number)
     {
       this.m_number = number;
       this.m_manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
       this.m_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0.0f, this);
       this.m_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0.0f, this);
     }
     
     @Override
     public void onLocationChanged(Location loco)
     {
       m_manager.removeUpdates(this);
       if (!this.m_is_sent)
       {
         Double latitude = new Double(loco.getLatitude());
         Double longitude = new Double(loco.getLongitude());
         this.m_is_sent = true;
         String geo = String.format("%sgeo: %s, %s", MsgReceiver.LOCO_CMD_START, 
                                    latitude.toString(), longitude.toString());
         //Toast.makeText(Stalker.this, "Sending Location", Toast.LENGTH_LONG).show();
         Stalker.this.sendSMS(this.m_number, geo);
       }
     }
     
     @Override
     public void onProviderDisabled(String provider)
     {}
     
     @Override
     public void onProviderEnabled(String provider)
     {}
     
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras)
     {}
   }
   
   
   public void setCurrentActivity(SettingsActivity activity)
   {
     m_activity = activity;
   }
   
   protected boolean isNumberAuthorised(String number)
   {
     SharedPreferences settings = this.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
     String auth_list[] = settings.getString(AUTH_LIST, "").split(";");
     for (String nr : auth_list)
     {
       if (number.equals(nr))
       {
         return true;
       }
     }
     return false;
   }
   
   protected void processLocationResponse(String cmd)
   {
     Matcher m = RE_GEO.matcher(cmd);
     if (m.matches() && (m_activity != null))
     {
       MatchResult result = m.toMatchResult();
       String geo = String.format("geo:0,0?q=%s,%s (Position)", result.group(1), result.group(2));
       m_activity.viewMap(geo);
       
       /*Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       this.getApplication().startActivity(intent);
       * */
     }
   }
   
   protected void processLocating(String addr)
   {
     //Toast.makeText(this, "Start locating...", Toast.LENGTH_LONG).show();
     LocateAndSend tmp = new LocateAndSend(addr);
   }
   
   protected void sendSMS(String number, String text)
   {
     SmsManager man = SmsManager.getDefault();
     man.sendTextMessage(number, null, text, null, null);
   }
   
   protected void doCommand(String cmd, String addr)
   {
     if (cmd.startsWith("geo"))
     {
       this.processLocationResponse(cmd);
     }
     else if (cmd.startsWith("locate"))
     {
       this.processLocating(addr);
     }
     else
     {
       Toast.makeText(this, "Unknown CMD: " + cmd, Toast.LENGTH_LONG).show();
     }
   }
   
   @Override
  public IBinder onBind(Intent intent)
   {
    return m_binder;
  }
   
   @Override
   public void onStart(Intent intent, int startId)
   {
     Bundle bundle = intent.getExtras();
     
     if (bundle != null)
     {
       String cmd = bundle.getString("cmd");
       String addr = bundle.getString("phone-number");
       if (this.isNumberAuthorised(addr))
       {
         this.doCommand(cmd, addr);
       }
       else
       {
         Toast.makeText(this, "Unauthorised CMD: " + cmd, Toast.LENGTH_LONG).show();
       }
     }
     
     this.stopSelf();
   }
 }
