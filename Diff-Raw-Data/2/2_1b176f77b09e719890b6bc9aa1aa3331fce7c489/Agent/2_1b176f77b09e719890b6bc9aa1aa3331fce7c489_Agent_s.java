 package net.anzix.proxyagent;
 
 import java.lang.instrument.Instrumentation;
 import java.net.Authenticator;
 import java.net.PasswordAuthentication;
 
 /**
  * Force to use proxyHost and proxy password parameters.
  *
  * @see: http://rolandtapken.de/blog/2012-04/java-process-httpproxyuser-and-httpproxypassword
  */
 public class Agent {
     /**
      * Get the property with proto prefix or if it's missing try without prefix.
      *
      */
     public static String getProperty(String prot, String type) {
         return System.getProperty(prot + "." + type, System.getProperty(type, ""));
     }
 
     public static void premain(String agentArgs, Instrumentation inst) {
 
        // Java ignores http.proxyUser. Here come's the workaround.
         Authenticator.setDefault(new Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 if (getRequestorType() == RequestorType.PROXY) {
                     String prot = getRequestingProtocol().toLowerCase();
                     String host = getProperty(prot, "proxyHost");
                     String port = getProperty(prot, "proxyPort");
                     String user = getProperty(prot, "proxyUser");
                     String password = getProperty(prot, "proxyPassword");
 
                     if (getRequestingHost().toLowerCase().equals(host.toLowerCase()) &&
                             Integer.parseInt(port) == getRequestingPort()) {
                         // Seems to be OK.
                         return new PasswordAuthentication(user, password.toCharArray());
                     }
                 }
                 return null;
             }
         });
     }
 }
