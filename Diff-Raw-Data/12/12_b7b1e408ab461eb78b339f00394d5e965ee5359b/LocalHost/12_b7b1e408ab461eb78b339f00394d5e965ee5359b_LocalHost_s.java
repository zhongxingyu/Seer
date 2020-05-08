 package comm;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import system.CiAN;
 import system.CiAN.CiANProperties;
 
 /**
  * This helper class finds the first non-loopback address associated with a
  * network adapter. Unlike <tt>InetAddress.getLocalHost()</tt>, it behaves as
  * expected on Linux (i.e., it does not return "127.0.0.1").
  * 
  * @author Greg Hackmann
  * @author Jordan Alliot
  */
 public abstract class LocalHost
 {
     private static InetAddress localHost = null;
 
     public static InetAddress getLocalHost() {
         if (null == localHost) {
             // We don't have localhost yet so we will try to retrieve it using
             // the method defined in GET_LOCALHOST_METHOD property
             String methodName = CiAN.getProperty(CiANProperties.GET_LOCALHOST_METHOD, "getIPLocalHost");
             try {
                 Method getLocalhostMethod = LocalHost.class.getDeclaredMethod(methodName, (Class[]) null);
                 getLocalhostMethod.setAccessible(true);
                 localHost = (InetAddress) getLocalhostMethod.invoke(null, (Object[]) null);
             } catch (Exception e) {
                 // We couldn't find the method or something went wrong, we fall
                 // back to the default implementation
                 localHost = LocalHost.getIPLocalHost();
             }
         }
 
         return localHost;
     }
 
     /**
      * Default implementation of CiANProperties.GET_LOCALHOST_METHOD.
      * Usually invoked by reflection.
      * 
      * @return the InetAddress of localhost guessed from the several interfaces
      *         of this machine
      */
     private static InetAddress getIPLocalHost() {
        InetAddress localhost = null;
         try {
             // Get all network interfaces
             // We use reflection in this method because NetworkInterface may be
             // not available (e.g. in J2ME)
             Class networkInterface = Class.forName("java.net.NetworkInterface");
             Enumeration interfaces = (Enumeration) networkInterface.getMethod("getNetworkInterfaces", (Class[]) null)
                     .invoke(null, (Object[]) null);
 
             while (null == localHost && interfaces.hasMoreElements()) {
                 // Get the next interface and its associated addresses
                 Object interfaze = interfaces.nextElement();
                 Enumeration addresses = (Enumeration) interfaze.getClass()
                         .getMethod("getInetAddresses", (Class[]) null).invoke(interfaze, (Object[]) null);
 
                 while (null == localHost && addresses.hasMoreElements()) {
                     InetAddress address = (InetAddress) addresses.nextElement();
                     Boolean isLoopback = (Boolean) address.getClass().getMethod("isLoopbackAddress", (Class[]) null)
                             .invoke(address, (Object[]) null);
 
                     if (4 == address.getAddress().length && !isLoopback.booleanValue()) {
                         localHost = address;
                     }
                     /*
                      * If it's a 32-bit (IPv4) address and it's not a loopback,
                      * that's the one we want
                      */
                 }
             }
 
             if (localHost == null) {
                 // Let's try the fallback method...
                 throw new Exception();
             }
         } catch (Exception e) {
             // If everything else has failed, fall back on
             // InetAddress.getLocalHost()
             try {
                 if (localHost == null) {
                     localHost = InetAddress.getLocalHost();
                 }
             } catch (UnknownHostException e2) {
                 e2.printStackTrace();
                 System.exit(1);
             }
         }
 
        return localhost;
     }
 
     /**
      * Invoked by reflection if CiANProperties.GET_LOCALHOST_METHOD equals
      * "getFileLocalHost".
      * If the file does not exist, fallback to getIPLocalHost.
      * 
      * @return the InetAddress of localhost read from file localhost.properties
      */
     @SuppressWarnings("unused")
     private static InetAddress getFileLocalHost() {
         InetAddress localHost = null;
 
         Properties props = new Properties();
         File propsFile = new File("localhost.properties");
 
         // Try to open the localhost.properties file if it's there
         if (propsFile.exists()) {
             try {
                 // Load its contents
                 props.load(new FileInputStream(propsFile));
 
                 String localHostProp = props.getProperty("localhost.address");
                 if (null != localHostProp) {
                     // Try to set localhost to the localhost.address property
                     localHost = InetAddress.getByName(localHostProp);
                 }
             } catch (Exception e) {
             }
         }
 
         if (null == localHost) {
             localHost = getIPLocalHost();
         }
 
         return localHost;
     }
 
     /**
      * Invoked by reflection if CiANProperties.GET_LOCALHOST_METHOD equals
      * "getJistSwansLocalHost".
      * 
      * @return the InetAddress of localhost given by the instance of Jist/Swans
      */
     @SuppressWarnings("unused")
     private static InetAddress getJistSwansLocalHost() {
         InetAddress localHost = null;
         try {
             if (null == CiAN.getExternalTool()) {
                 throw new Exception("Jist/Swans does not look to have been injected into CiAN");
             }
 
             // We don't want to have a dependency between CiAN and the simulator
             // so we have to only use Reflection here...
             Class appCiANClass = Class.forName("ext.jist.swans.app.AppCiAN");
             if (!appCiANClass.isInstance(CiAN.getExternalTool())) {
                 throw new Exception("The external dependency doesn't look like the Jist/Swans CiAN application");
             }
 
             Method getInetAddressMethod = appCiANClass.getMethod("getInetAddress", (Class[]) null);
             localHost = (InetAddress) getInetAddressMethod.invoke(CiAN.getExternalTool(), (Object[]) null);
 
             if (null == localHost) {
                 throw new Exception("Cannot retrieve localhost address through Jist/Swans...");
             }
         } catch (Exception e) {
             // We certainly don't want to fall back to the IP localhost in that
             // case so we exit...
             e.printStackTrace();
             System.exit(1);
         }
         return localHost;
     }
 }
