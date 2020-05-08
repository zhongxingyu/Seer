 package org.tcrun.slickij.api.data;
 
 import com.google.code.morphia.annotations.PostLoad;
 import com.google.code.morphia.annotations.PrePersist;
 import com.google.code.morphia.annotations.Property;
 import org.tcrun.slickij.api.data.AbstractSystemConfiguration;
 
 /**
  * User: jcorbett
  * Date: 12/3/12
  * Time: 1:17 PM
  */
 public class AMQPSystemConfiguration extends AbstractSystemConfiguration
 {
     public static final String CONFIGURATION_TYPE_NAME = "amqp-system-configuration";
 
     @Property
     private String hostname;
 
     @Property
     private Integer port;
 
     @Property
     private String virtualHost;
 
     @Property
     private String exchangeName;
 
     @Property
     private String username;
 
     @Property
     private String password;
 
     public AMQPSystemConfiguration()
     {
         this.configurationType = AMQPSystemConfiguration.CONFIGURATION_TYPE_NAME;
     }
 
     public String getHostname()
     {
         return hostname;
     }
 
     public void setHostname(String hostname)
     {
         this.hostname = hostname;
     }
 
     public String getVirtualHost()
     {
         return virtualHost;
     }
 
     public void setVirtualHost(String virtualHost)
     {
         this.virtualHost = virtualHost;
     }
 
     public Integer getPort()
     {
         return port;
     }
 
     public void setPort(Integer port)
     {
         this.port = port;
     }
 
     public String getExchangeName()
     {
         return exchangeName;
     }
 
     public void setExchangeName(String exchangeName)
     {
         this.exchangeName = exchangeName;
     }
 
     public String getUsername()
     {
         return username;
     }
 
     public void setUsername(String username)
     {
         this.username = username;
     }
 
     public String getPassword()
     {
         return password;
     }
 
     public void setPassword(String password)
     {
         this.password = password;
     }
 
     @Override
     public void validate() throws InvalidDataError
     {
         if (this.name == null)
             throw new InvalidDataError(AMQPSystemConfiguration.class.getName(), "name", "cannot be null");
         if (this.hostname == null)
             throw new InvalidDataError(AMQPSystemConfiguration.class.getName(), "hostname", "cannot be null");
         if (port == null)
             port = 5672;
     }
 
     @Override
     public void update(SystemConfiguration update) throws InvalidDataError
     {
         if (! (update instanceof AMQPSystemConfiguration))
             throw new InvalidDataError(AMQPSystemConfiguration.class.getName(), "type", "can only update an instance of AMQPSystemConfiguration with another instance of the same type.  Type provided: " + update.getClass().getName());
         AMQPSystemConfiguration realUpdate = (AMQPSystemConfiguration)update;
         if(realUpdate.getName() != null)
             this.name = update.getName();
         if(realUpdate.getHostname() != null)
             this.hostname = realUpdate.getHostname();
         if(realUpdate.getPort() != null)
             this.port = realUpdate.getPort();
         if(realUpdate.getUsername() != null)
             this.username = realUpdate.getUsername();
         if(realUpdate.getPassword() != null)
             this.password = realUpdate.getPassword();
         if(realUpdate.getExchangeName() != null)
             this.exchangeName = realUpdate.getExchangeName();
     }
 
     @PrePersist
     public void prePersist()
     {
         this.configurationType = AMQPSystemConfiguration.CONFIGURATION_TYPE_NAME;
     }
 
     @PostLoad
     public void postLoad()
     {
         this.configurationType = AMQPSystemConfiguration.CONFIGURATION_TYPE_NAME;
     }
 }
