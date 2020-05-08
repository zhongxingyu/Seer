 package com.deviceteam.kezdet;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.deviceteam.kezdet.exception.PluginCreateException;
 import com.deviceteam.kezdet.exception.PluginLoadException;
 import com.deviceteam.kezdet.exception.PluginVerifyException;
 import com.deviceteam.kezdet.host.PluginManager;
 import com.deviceteam.kezdet.interfaces.IPluginCallback;
 import com.deviceteam.kezdet.interfaces.exception.BadPluginException;
 import com.google.gson.Gson;
 
 public class AndroidTestAppActivity extends Activity
 {
   public static String TAG = "AndroidTestAppActivity";
   private Gson _gson = new Gson();
 
   @Override
   public void onCreate( Bundle savedInstanceState )
   {
     super.onCreate( savedInstanceState );
     setContentView( R.layout.activity_android_test_app );
 
     Context context = getApplicationContext();
 
     PluginManager _manager = new PluginManager( );
     InputStream is = null;
     int pluginId;
     
     try
     {
      is = context.getAssets().open( "certificates/kezdet-public-test-certificate.cer" );
       _manager.init( context, this, AndroidTestAppActivity.class.getClassLoader(), is );
       is.close();
       
       File pluginLoc = new File( Environment.getExternalStorageDirectory(), "/jar/TestPlugin.jar" );
       is = new FileInputStream( pluginLoc );
       
       pluginId = _manager.loadPlugin( is, "com.deviceteam.kezdet.plugin.KezdetPlugin" );
       _manager.initPlugin( pluginId, new IPluginCallback()
       {
         @Override
         public void onPluginCallback( String message, String param )
         {
           DisplayResult( message, param );
         }
       } );
       is.close();
  
       String battLevel = _manager.invokePluginMethod( pluginId, "batteryLevel", "1000000" );
       int batt = _gson.fromJson( battLevel, Integer.class );
       Log.d( TAG, "Battery Level is: " + batt );
     }
     catch( PluginVerifyException e )
     {
       Log.e( TAG, e.toString() );
     }
     catch( IOException e )
     {
       Log.e( TAG, e.toString() );
     }
     catch( PluginLoadException e )
     {
       Log.e( TAG, e.toString() );
     }
     catch( PluginCreateException e )
     {
       Log.e( TAG, e.toString() );
     }
     catch( NoSuchMethodException e )
     {
       Log.e( TAG, e.toString() );
     }
     catch( IndexOutOfBoundsException e )
     {
       Log.e( TAG, e.toString() );
     }
     catch( BadPluginException e )
     {
       Log.e( TAG, e.toString() );
     }
     finally
     {
       if( is != null )
       {
         try
         {
           is.close();
         }
         catch( IOException e )
         {
         }
       }
     }
   }
 
 
   public void DisplayResult( String arg0, String arg1 )
   {
     TextView txt1 = (TextView) findViewById( R.id.txtUpdate1 );
     txt1.setText( arg0 );
     TextView txt2 = (TextView) findViewById( R.id.txtUpdate2 );
     txt2.setText( arg1 );
   }
 
 }
