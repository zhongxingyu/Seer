 package net.sourceforge.guacamole.net.cvp;
 
 import java.util.Map;
 import net.sourceforge.guacamole.protocol.GuacamoleConfiguration;
 
 public interface Connector {
 
     static String PROP_KEY_HOSTNAME = "hostname";
     static String PROP_KEY_PORT = "port";
     static String PROP_KEY_PASSWORD = "password";
 
     Map<String, GuacamoleConfiguration> findConfigurations(String key);
 }
