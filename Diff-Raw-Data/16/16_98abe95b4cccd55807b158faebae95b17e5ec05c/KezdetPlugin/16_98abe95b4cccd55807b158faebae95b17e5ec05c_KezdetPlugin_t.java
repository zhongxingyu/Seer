 package com.deviceteam.kezdet.plugin;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.BatteryManager;
 import android.util.Log;
 
 import com.deviceteam.kezdet.helpers.KezdetInterfaceMap;
 import com.deviceteam.kezdet.interfaces.IInvokeMethod;
 import com.deviceteam.kezdet.interfaces.IPlugin;
 import com.deviceteam.kezdet.interfaces.IPluginCallback;
 import com.google.gson.Gson;
 
 public class KezdetPlugin implements IPlugin
 {
   private IPluginCallback _callback;
   private int _batteryLevel = -1;
   private BroadcastReceiver _batteryLevelReceiver;
   private Boolean _isSupported = false;
   private Context _context;
   private Gson _gson = new Gson();
 
   @Override
   public void initialise( Context context, Activity activity, IPluginCallback callback )
   {
     _context = context;
     _callback = callback;
     try
     {
       _batteryLevelReceiver = new BroadcastReceiver()
       {
         public void onReceive( Context context, Intent intent )
         {
           int rawlevel = intent.getIntExtra( BatteryManager.EXTRA_LEVEL, -1 );
           int scale = intent.getIntExtra( BatteryManager.EXTRA_SCALE, -1 );
           int level = -1;
           if( rawlevel >= 0 && scale > 0 )
           {
             level = ( rawlevel * 100 ) / scale;
           }
 
           _batteryLevel = level;
           _callback.onPluginCallback( "level", Integer.toString( _batteryLevel ) );
         }
 
       };
 
       IntentFilter batteryLevelFilter = new IntentFilter( Intent.ACTION_BATTERY_CHANGED );
       Intent value = _context.registerReceiver( _batteryLevelReceiver, batteryLevelFilter );
       if( value != null )
       {
         _isSupported = true;
       }
     }
     catch( Exception ex )
     {
       Log.d("battery", ex.toString() );
     }
   }
 
   @Override
   public void dispose()
   {
     if( _batteryLevelReceiver != null )
     {
       _context.unregisterReceiver( _batteryLevelReceiver );
     }
   }
 
   @Override
   public byte[] getBinaryData() throws UnsupportedOperationException
   {
     throw( new UnsupportedOperationException() );
   }
 
   @Override
   public String getJSONData() throws UnsupportedOperationException
   {
     return _gson.toJson( _batteryLevel );
   }
 
   @Override
  public EventDataResponseType getResponseType()
   {
     return( EventDataResponseType.JSON );
   }
 
   @Override
   public void registerMethods( KezdetInterfaceMap methods )
   {
     methods.put( "batterySupported", new IInvokeMethod()
     {
       @Override
       public String invoke( String jsonArgs )
       {
         return( _gson.toJson( _isSupported, Boolean.class ) );
       }
     } );
 
     methods.put( "batteryLevel", new IInvokeMethod()
     {
       @Override
       public String invoke( String jsonArgs )
       {
         return( _gson.toJson( _batteryLevel, Integer.class ) );
       }
     } );
   }
 
   @Override
   public void clearResponseData()
   {
   }
 
 }
