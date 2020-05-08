 package com.pivotal.hamster.appmaster.utils;
 
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.yarn.api.ApplicationConstants;
 import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
 import org.apache.hadoop.yarn.api.records.ContainerId;
 import org.apache.hadoop.yarn.util.ConverterUtils;
 
 import com.pivotal.hamster.common.HamsterConfig;
 import com.pivotal.hamster.common.HamsterException;
 
 public class HamsterAppMasterUtils {
   private static final Log LOG = LogFactory.getLog(HamsterAppMasterUtils.class);
   
   public static ApplicationAttemptId getAppAttemptIdFromEnv() {
     ContainerId containerId = getContainerIdFromEnv();
     if (containerId == null) {
       return null;
     }
     return containerId.getApplicationAttemptId();
   }
   
   public static ContainerId getContainerIdFromEnv() {
     String containerIdStr =
         System.getenv(ApplicationConstants.AM_CONTAINER_ID_ENV);
     if (null == containerIdStr) {
       return null;
     }
     ContainerId containerId = ConverterUtils.toContainerId(containerIdStr);
     return containerId;
   }
   
   public static String getLocalNMHttpAddr() {
     if (null == System.getenv("NM_HOST")) {
       return null;
     }
     if (null == System.getenv("NM_HTTP_PORT")) {
       return null;
     }
     return System.getenv("NM_HOST") + ":" + System.getenv("NM_HTTP_PORT");
   }
   
   public static String normlizeHostName(String host) {
     try {
       InetAddress addr = InetAddress.getByName(host);
      return addr.getHostName();
     } catch (UnknownHostException e) {
       return host;
     }
   }
   
   public static String getNormalizedLocalhost() throws UnknownHostException {
     return HamsterAppMasterUtils.normlizeHostName(java.net.InetAddress.getLocalHost().getHostName());
   }
   
   public static Configuration getLocalConfiguration() {
     try {
       // open the file and try to read
       FileInputStream fis = new FileInputStream(
           HamsterConfig.DEFAULT_LOCALCONF_SERIALIZED_FILENAME);
       DataInputStream is = new DataInputStream(fis);
       
       // read the content from file
       Configuration conf = new Configuration();
       conf.readFields(is);
       
       fis.close();
       is.close();
       
       return conf;
     } catch (Exception e) {
       throw new HamsterException(e);
     }
   }
   
   public static boolean isLocalHost(String host) {
     InetAddress addr;
     try {
       addr = InetAddress.getByName(host);
     } catch (UnknownHostException e) {
       LOG.warn("unkown host, please check, host:" + host);
       return false;
     }
     // Check if the address is a valid special local or loop back
     if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
         return true;
 
     // Check if the address is defined on any interface
     try {
         return NetworkInterface.getByInetAddress(addr) != null;
     } catch (SocketException e) {
         return false;
     }
 
   }
 }
