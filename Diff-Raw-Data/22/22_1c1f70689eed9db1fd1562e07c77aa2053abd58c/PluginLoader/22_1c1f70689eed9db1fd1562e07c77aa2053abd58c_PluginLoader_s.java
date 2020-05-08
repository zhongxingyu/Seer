 package com.deviceteam.kezdet.host;
 
 import com.deviceteam.kezdet.Log;
 import com.deviceteam.kezdet.exception.PluginCreateException;
 import com.deviceteam.kezdet.exception.PluginLoadException;
 import com.deviceteam.kezdet.exception.PluginVerifyException;
 import com.deviceteam.kezdet.interfaces.IPlugin;
 import com.google.dexmaker.AppDataDirGuesser;
 import org.whipplugin.data.bundle.JarVerifier;
 
 import java.io.*;
 import java.lang.reflect.InvocationTargetException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.*;
 import java.util.jar.JarFile;
 
 public class PluginLoader
 {
   private static final String TAG = "KezdetHostLib::PluginLoader";
   private X509Certificate _verificationCert;
   private ClassLoader _parentLoader;
   private Map<UUID, ClassLoader> _loaderMap = new HashMap<UUID, ClassLoader>();
   private Set<File> _cachedContainers = new HashSet<File>();
 
   /**
    * PluginLoader constructor
    *
    * @param parentLoader     ClassLoader object to use as a loader context
    * @param verificationCert public X509 certificate used to verify that the plugin has been correctly signed
    */
   public PluginLoader(ClassLoader parentLoader, X509Certificate verificationCert)
   {
     _parentLoader = parentLoader;
     _verificationCert = verificationCert;
     Log.verbose( TAG, "constructed." );
   }
 
   /**
    * @param containerStream stream encapsulating the JAR file that contains the plugin to load, the stream will be closed by this method
    * @return UUID as a unique key identifier for the plugin container, pass this to {@link #loadPlugin}
    * @throws PluginLoadException   if the plugin container cannot be loaded
    * @throws PluginVerifyException if the plugin container verification does not pass (incorrectly signed or cryptographic failure)
    * @throws PluginCreateException if the plugin ClassLoader could not be created
    */
   public UUID registerContainer(InputStream containerStream) throws PluginLoadException, PluginVerifyException, PluginCreateException
   {
     try
     {
       File dexCache = new AppDataDirGuesser().guess();
       File containerCacheFile = File.createTempFile( "Generated", ".jar", dexCache );
       saveToFile( containerStream, containerCacheFile );
       _cachedContainers.add( containerCacheFile );
       Log.verbose( TAG, "Container: " + containerCacheFile );
 
       JarFile jf = new JarFile( containerCacheFile );
       JarVerifier.verify( jf, new X509Certificate[]{_verificationCert} );
 
       // TBD: loaded reflectively to overcome platform issues
      ClassLoader cl = (ClassLoader) Class.forName( "dalvik.system.DexClassLoader" )
           .getConstructor( String.class, String.class, String.class, ClassLoader.class )
          .newInstance( containerCacheFile.getPath(), dexCache.getAbsolutePath(), null, _parentLoader );
 
       UUID key = UUID.randomUUID();
       _loaderMap.put( key, cl );
       return (key);
     } catch(IOException e)
     {
       throw new PluginLoadException( String.format( "Failed to load plugin container" ), e );
     } catch(CertificateException e)
     {
       throw new PluginVerifyException( String.format( "Certificate issue while verifying plugin container" ), e );
     } catch(SecurityException e)
     {
       throw new PluginVerifyException( String.format( "Invalid JAR while verifying plugin container" ), e );
     } catch(ClassNotFoundException e)
     {
       throw new PluginCreateException( String.format( "Cannot find dalvik.system.DexClassLoader!" ), e );
     } catch(NoSuchMethodException e)
     {
       throw new PluginCreateException( String.format( "Cannot construct ClassLoader for plugin container" ), e );
     } catch(IllegalArgumentException e)
     {
       throw new PluginCreateException( String.format( "Cannot instantiate ClassLoader in plugin container" ), e );
     } catch(IllegalAccessException e)
     {
       throw new PluginCreateException( String.format( "Cannot instantiate ClassLoader in plugin container" ), e );
     } catch(InvocationTargetException e)
     {
       throw new PluginCreateException( String.format( "Cannot instantiate ClassLoader in plugin container" ), e );
     } catch(InstantiationException e)
     {
       throw new PluginCreateException( String.format( "Cannot instantiate ClassLoader in plugin container" ), e );
     }
   }
 
   /**
    * Loads a plugin from a container
    *
    * @param containerId UUID representing the registered plugin container, see: {@link #registerContainer(InputStream)}
    * @param className   name of the class in the JAR file that implements IPlugin
    * @return an IPlugin instance if the plugin is successfully loaded
    * @throws PluginLoadException   if the plugin cannot be loaded
    * @throws PluginVerifyException if the plugin verification does not pass (incorrectly signed or cryptographic failure)
    * @throws PluginCreateException if the plugin class could not be created (incorrect name or other error)
    */
   public IPlugin loadPlugin(UUID containerId, String className) throws PluginCreateException, PluginLoadException
   {
     try
     {
       ClassLoader cl = _loaderMap.get( containerId );
       if (cl == null)
       {
         throw new PluginLoadException( String.format( "Invalid plugin container: %s", containerId.toString() ) );
       }
 
       Class<?> pluginClass = cl.loadClass( className );
       IPlugin plugin = (IPlugin) pluginClass.newInstance();
       return (plugin);
     } catch(SecurityException e)
     {
       throw new PluginCreateException( String.format( "Cannot instantiate class %s in plugin", className ), e );
     } catch(ClassNotFoundException e)
     {
       throw new PluginCreateException( String.format( "Cannot find class %s in plugin", className ), e );
     } catch(InstantiationException e)
     {
       throw new PluginCreateException( String.format( "Cannot instantiate class %s in plugin", className ), e );
     } catch(IllegalArgumentException e)
     {
       throw new PluginCreateException( String.format( "Cannot instantiate class %s in plugin", className ), e );
     } catch(IllegalAccessException e)
     {
       throw new PluginCreateException( String.format( "Cannot instantiate class %s in plugin", className ), e );
     }
   }
 
   public void dispose()
   {
     _loaderMap.clear();
 
     Iterator<File> i = _cachedContainers.iterator();
     while (i.hasNext())
     {
       File container = i.next();
       if (container.exists())
       {
         container.delete();
       }
     }
     _cachedContainers.clear();
     Log.verbose( TAG, "disposed." );
   }
 
   private void saveToFile(InputStream inputStream, File destination) throws IOException
   {
     FileOutputStream fos = new FileOutputStream( destination );
     copyStream( inputStream, fos );
     fos.close();
 
   }
 
   private void copyStream(InputStream is, OutputStream os) throws IOException
   {
     byte[] buffer = new byte[1024];
     int bytesRead;
 
     while ((bytesRead = is.read( buffer )) != -1)
     {
       os.write( buffer, 0, bytesRead );
     }
   }
 }
