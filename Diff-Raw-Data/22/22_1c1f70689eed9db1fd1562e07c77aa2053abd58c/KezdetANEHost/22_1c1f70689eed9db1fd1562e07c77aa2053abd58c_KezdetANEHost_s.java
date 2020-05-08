 package com.deviceteam.kezdet.anehost;
 
 import android.app.Activity;
 import android.content.Context;
 import com.adobe.fre.FREContext;
 import com.adobe.fre.FREExtension;
 import com.deviceteam.kezdet.exception.PluginCreateException;
 import com.deviceteam.kezdet.exception.PluginLoadException;
 import com.deviceteam.kezdet.exception.PluginVerifyException;
 import com.deviceteam.kezdet.Log;
 import com.deviceteam.kezdet.host.PluginManager;
 import com.deviceteam.kezdet.interfaces.IPluginCallback;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 
 public class KezdetANEHost implements FREExtension
 {
   public static final String TAG = "KezdetAirHost::KezdetANEHost";
   private PluginManager _manager;
   private String _pluginDir;
   private Context _context;
   private Boolean _inAssets = false;
   private Map<String, UUID> _fileContainerMap = new HashMap<String, UUID>();
 
   public KezdetANEHost()
   {
     // set the log level for debugging.
    Log.setLogLevel( android.util.Log.INFO );
     Log.verbose( TAG, "1: constructing." );
   }
 
   public PluginManager getPluginManager()
   {
     return (_manager);
   }
 
 
   @Override
   public FREContext createContext(String arg)
   {
     Log.verbose( TAG, "3: creating KezdetANEHostContext." );
     return (new KezdetANEHostContext( this ));
   }
 
   @Override
   public void initialize()
   {
     // SXD: this only gets fired on the first createExtensionContext call.
     _manager = new PluginManager();
     Log.verbose( TAG, "2: initialized." );
   }
 
   @Override
   public void dispose()
   {
     _fileContainerMap.clear();
     _manager.dispose();
     Log.verbose( TAG, "disposed." );
   }
 
   public void initManager(Context context, Activity activity, String certPath, String pluginDir) throws PluginVerifyException, IOException
   {
     _context = context;
     if (pluginDir.startsWith( "app:/" ))
     {
       _inAssets = true;
       _pluginDir = pluginDir.replace( "app:/", "" );
     }
     else if (pluginDir.startsWith( "file://" ))
     {
       _pluginDir = pluginDir.replace( "file://", "" );
     }
     else
     {
       _pluginDir = pluginDir;
     }
     ClassLoader parentClassloader = KezdetANEHost.class.getClassLoader();
     InputStream certificateStream = null;
     try
     {
       certificateStream = context.getAssets().open( certPath );
       _manager.init( context, activity, parentClassloader, certificateStream );
       Log.verbose( TAG, "4: manager initialized." );
     } finally
     {
       if (certificateStream != null)
       {
         certificateStream.close();
       }
     }
   }
 
   public int loadPlugin(FREContext context, String pluginFile, String pluginClassName) throws PluginLoadException, PluginVerifyException, PluginCreateException
   {
     UUID containerId;
     if (_fileContainerMap.containsKey( pluginFile ))
     {
       containerId = _fileContainerMap.get( pluginFile );
       Log.verbose( TAG, "8: Found containerId " + containerId + " for " + pluginFile );
     }
     else
     {
       String pluginPath = combinePath( _pluginDir, pluginFile );
       containerId = loadPluginContainer( pluginPath );
       _fileContainerMap.put( pluginFile, containerId );
       Log.verbose( TAG, "5: Registered containerId " + containerId + " for " + pluginFile );
     }
 
     final FREContext ctx = context;
     final int id = _manager.loadPlugin( containerId, pluginClassName );
     Log.verbose( TAG, "6: loaded plugin " + pluginClassName + " - id: " + id );
     _manager.initPlugin( id, new IPluginCallback()
     {
       @Override
       public void onPluginCallback(String message, String param)
       {
         ctx.dispatchStatusEventAsync( Integer.toString( id ) + ":" + message, param );
       }
     } );
     Log.verbose( TAG, "7: initialized plugin " + pluginClassName + " - id: " + id );
     return (id);
   }
 
   private UUID loadPluginContainer(String path) throws PluginLoadException, PluginVerifyException, PluginCreateException
   {
     InputStream jarStream = null;
     try
     {
       if (_inAssets)
       {
         jarStream = _context.getAssets().open( path );
       }
       else
       {
         jarStream = new FileInputStream( path );
       }
 
       UUID containerId = _manager.registerContainer( jarStream );
       return containerId;
     } catch(IOException e)
     {
       throw new PluginLoadException( "Unable to load plugin: " + path + " from( " + (_inAssets ? "assets" : "file system") + " )", e );
     } finally
     {
       if (jarStream != null)
       {
         try
         {
           jarStream.close();
         } catch(IOException e)
         {
           // swallow exception
         }
       }
     }
   }
 
   private String combinePath(String path1, String path2)
   {
     File file1 = new File( path1 );
     File file2 = new File( file1, path2 );
     return (file2.getPath());
   }
 }
