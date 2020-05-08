 package net.intensicode;
 
 import net.intensicode.core.*;
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 
 class Updater
     {
     public Updater( final GameSystem aGameSystem )
         {
         myGameSystem = aGameSystem;
         }
 
     public void check( final String aBaseUrl, final int aVersionNumber, final UpdateCallback aCallback )
         {
        myGameSystem.network.sendAndReceive( aBaseUrl, null, new NetworkCallback()
         {
         public void onReceived( final byte[] aBytes )
             {
             try
                 {
                 final String updateInfo = new String( aBytes );
                 System.out.println( updateInfo );
 
                 final JSONObject json = new JSONObject( updateInfo );
                 final int updateVersion = json.getInt( "version" );
                 if ( updateVersion <= aVersionNumber )
                     {
                     aCallback.noUpdateAvailable();
                     }
                 else
                     {
                     aCallback.onUpdateAvailable( new BasicUpdateContext(myGameSystem, json), updateVersion );
                     }
                 }
             catch ( JSONException e )
                 {
                 aCallback.updateCheckFailed( e );
                 }
             }
 
         public void onError( final Throwable aThrowable )
             {
             aCallback.updateCheckFailed( aThrowable );
             }
         } );
         }
 
     private final GameSystem myGameSystem;
     }
