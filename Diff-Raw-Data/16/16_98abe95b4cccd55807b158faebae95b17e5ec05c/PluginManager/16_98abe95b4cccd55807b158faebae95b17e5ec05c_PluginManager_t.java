 package com.deviceteam.kezdet.host;
 
 import android.app.Activity;
 import android.content.Context;
 import com.deviceteam.kezdet.Log;
 import com.deviceteam.kezdet.exception.PluginCreateException;
 import com.deviceteam.kezdet.exception.PluginLoadException;
 import com.deviceteam.kezdet.exception.PluginVerifyException;
 import com.deviceteam.kezdet.helpers.KezdetInterfaceMap;
 import com.deviceteam.kezdet.interfaces.IPlugin;
 import com.deviceteam.kezdet.interfaces.IPluginCallback;
 import com.deviceteam.kezdet.interfaces.exception.BadPluginException;
 
 import java.io.InputStream;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.UUID;
 
 /**
  * Manages plugins :)
  *
  * @author sweetlilmre
  */
 public class PluginManager
 {
   public static final String TAG = "KezdetHostLib::PluginManager";
   private PluginLoader _loader;
   private Context _context;
   private Activity _activity;
   private ArrayList<PluginInfo> _pluginList = new ArrayList<PluginInfo>();
 
 
   /**
    * PluginManager constructor
    */
   public PluginManager()
   {
   }
 
   /**
    * Initialises the PluginLoader
    *
    * @param context           the Android application context
    * @param activity          the Android activity
    * @param parentClassloader the parent {@link java.lang.ClassLoader ClassLoader} context
    * @param certificateStream a stream representing an X506 public certificate that will be used to validate the signature of any loaded plugins
    * @throws PluginVerifyException on failure to load the certificate
    */
   public void init(Context context, Activity activity, ClassLoader parentClassloader, InputStream certificateStream) throws PluginVerifyException
   {
     _context = context;
     _activity = activity;
     X509Certificate verificationCert = null;
     try
     {
       CertificateFactory cf = CertificateFactory.getInstance( "X.509" );
       verificationCert = (X509Certificate) cf.generateCertificate( certificateStream );
 
       _loader = new PluginLoader( parentClassloader, verificationCert );
       Log.debug( TAG, "Verification Certificate loaded." );
     } catch(CertificateException e)
     {
       throw new PluginVerifyException( "Failure creating certificate from stream", e );
     }
     Log.verbose( TAG, "initialized." );
   }
 
   /**
    * Disposes all loaded plugins
    */
   public void dispose()
   {
     for (int i = 0; i < _pluginList.size(); i++)
     {
       _pluginList.get( i ).get_plugin().dispose();
     }
     _pluginList.clear();
     _loader.dispose();
     Log.verbose( TAG, "disposed." );
   }
 
   public UUID registerContainer(InputStream containerStream) throws PluginLoadException, PluginVerifyException, PluginCreateException
   {
     return (_loader.registerContainer( containerStream ));
   }
 
   /**
    * Dynamically loads a plugin from a JAR file represented by the pluginStream parameter
    *
    * @param containerId     a stream representing a JAR file that contains the plugin
    * @param pluginClassName the name of the class in the JAR stream that implements IPlugin
    * @throws PluginLoadException   when the plugin fails to load (IOError)
    * @throws PluginVerifyException when the plugin cannot be successfully verified against the supplied public certificate
    * @throws PluginCreateException when the plugin has been loaded and verified but cannot be instantiated
    */
   public int loadPlugin(UUID containerId, String pluginClassName) throws PluginLoadException, PluginVerifyException, PluginCreateException
   {
     int pluginId = _pluginList.size() + 1;
     IPlugin plugin = _loader.loadPlugin( containerId, pluginClassName );
 
     KezdetInterfaceMap methodMap = new KezdetInterfaceMap();
     plugin.registerMethods( methodMap );
     PluginInfo info = new PluginInfo( plugin, methodMap );
 
     _pluginList.add( info );
     Log.debug( TAG, "Plugin " + pluginClassName + " loaded with id " + pluginId );
     return (pluginId);
   }
 
   /**
    * Initialises a loaded plugin
    *
    * @param pluginId the plugin to initialise
    * @param callback a callback method to pass to the plugin
    * @throws IndexOutOfBoundsException if the pluginId is invalid (out of range)
    */
   public void initPlugin(int pluginId, IPluginCallback callback) throws IndexOutOfBoundsException
   {
     if (pluginId < 1 || pluginId > _pluginList.size())
     {
       throw new IndexOutOfBoundsException( "PluginId: " + pluginId + " does not exist" );
     }
     _pluginList.get( pluginId - 1 ).get_plugin().initialise( _context, _activity, callback );
     Log.verbose( TAG, "Plugin " + pluginId + " initialized." );
   }
 
   /**
    * Invokes a method on a plugin method list
    *
    * @param pluginId   the integer identifier of the plugin
    * @param methodName the name of the method to invoke
    * @param jsonArgs   a string in JSON object format passed to the method representing the method parameters
    * @return a string in JSON object format representing the return result of the method
    * @throws IndexOutOfBoundsException, NoSuchMethodException
    */
   public String invokePluginMethod(int pluginId, String methodName, String jsonArgs) throws IndexOutOfBoundsException, NoSuchMethodException, BadPluginException
   {
     if (pluginId < 1 || pluginId > _pluginList.size())
     {
       throw new IndexOutOfBoundsException( "PluginId: " + pluginId + " does not exist" );
     }
     KezdetInterfaceMap methods = _pluginList.get( pluginId - 1 ).get_methods();
     if (methods.containsKey( methodName ))
     {
       try
       {
         return (methods.get( methodName ).invoke( jsonArgs ));
       } catch(Exception e)
       {
         throw (new BadPluginException( "Plugin failed to handle exception", e ));
       }
     }
     throw (new NoSuchMethodException( "No such method: " + methodName ));
   }
 
   public void clearPluginData(int pluginId) throws IndexOutOfBoundsException, BadPluginException
   {
     if (pluginId < 1 || pluginId > _pluginList.size())
     {
       throw new IndexOutOfBoundsException( "PluginId: " + pluginId + " does not exist" );
     }
     try
     {
       _pluginList.get( pluginId - 1 ).get_plugin().clearResponseData();
     } catch(Exception e)
     {
       throw (new BadPluginException( "Plugin failed to handle exception", e ));
     }
   }
 
   public String getResponseType(int pluginId) throws IndexOutOfBoundsException, BadPluginException
   {
     if (pluginId < 1 || pluginId > _pluginList.size())
     {
       throw new IndexOutOfBoundsException( "PluginId: " + pluginId + " does not exist" );
     }
     try
     {
      return (_pluginList.get( pluginId - 1 ).get_plugin().getResponseType().toString());
     } catch(Exception e)
     {
       throw (new BadPluginException( "Plugin failed to handle exception", e ));
     }
   }
 
   public String getJSONResponse(int pluginId) throws IndexOutOfBoundsException, BadPluginException, UnsupportedOperationException
   {
     if (pluginId < 1 || pluginId > _pluginList.size())
     {
       throw new IndexOutOfBoundsException( "PluginId: " + pluginId + " does not exist" );
     }
     try
     {
       return (_pluginList.get( pluginId - 1 ).get_plugin().getJSONData());
     } catch(UnsupportedOperationException e)
     {
       throw (e);
     } catch(Exception e)
     {
       throw (new BadPluginException( "Plugin failed to handle exception", e ));
     }
   }
 
   public byte[] getBinaryResponse(int pluginId) throws IndexOutOfBoundsException, BadPluginException, UnsupportedOperationException
   {
     if (pluginId < 1 || pluginId > _pluginList.size())
     {
       throw new IndexOutOfBoundsException( "PluginId: " + pluginId + " does not exist" );
     }
     try
     {
       return (_pluginList.get( pluginId - 1 ).get_plugin().getBinaryData());
     } catch(UnsupportedOperationException e)
     {
       throw (e);
     } catch(Exception e)
     {
       throw (new BadPluginException( "Plugin failed to handle exception", e ));
     }
   }
 }
