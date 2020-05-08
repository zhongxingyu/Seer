 package com.deviceteam.kezdet.anehost.functions;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Context;
 
 import com.adobe.fre.FREContext;
 import com.adobe.fre.FREFunction;
 import com.adobe.fre.FREObject;
 import com.deviceteam.kezdet.anehost.KezdetANEHost;
 import com.deviceteam.kezdet.anehost.utils.HostResponseValues;
 import com.deviceteam.kezdet.exception.PluginVerifyException;
 
 public class InitManagerFunction extends BaseFunction implements FREFunction
 {
   public InitManagerFunction( KezdetANEHost host )
   {
     super( host, "KezdetANEHost-InitManagerFunction" );
   };
 
   @Override
   public FREObject call( FREContext arg0, FREObject[] arg1 )
   {
     Activity activity = arg0.getActivity();
     Context context = activity.getApplicationContext();
     String jarLocation;
    String certName;
     HostResponseValues returnCode = HostResponseValues.UnknownError;
     try
     {
      certName = arg1[0].getAsString();
      jarLocation = arg1[1].getAsString();
      _host.initManager( context, activity, certName, jarLocation );
       returnCode = HostResponseValues.OK;
     }
     catch( PluginVerifyException e )
     {
       returnCode = HostResponseValues.CertificateVerifyError;
       LogE( "certificate failed verification: ", e );
     }
     catch( IOException e )
     {
       LogE( "certificate failed to load: ", e );
       returnCode = HostResponseValues.CertificateLoadError;
     }
     catch( Exception e )
     {
       LogE( "initManagerFunction failed: ", e );
     }
     
     return( GenerateReturnObject( returnCode, 0 ) );
   }
 }
