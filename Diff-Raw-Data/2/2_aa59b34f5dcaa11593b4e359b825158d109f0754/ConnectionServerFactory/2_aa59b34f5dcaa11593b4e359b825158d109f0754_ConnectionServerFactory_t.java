 /**
  * Appia: Group communication and protocol composition framework library
  * Copyright 2006 University of Lisbon
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  *
  * Initial developer(s): Alexandre Pinto and Hugo Miranda.
  * Contributor(s): See Appia web page for a list of contributors.
  */
  /**
  * Title:        Appia<p>
  * Description:  Protocol development and composition framework<p>
  * Copyright:    Copyright (c) Nuno Carvalho and Luis Rodrigues<p>
  * Company:      F.C.U.L.<p>
  * @author Nuno Carvalho and Luis Rodrigues
  * @version 1.0
  */
 
 package org.continuent.appia.management.jmx;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Hashtable;
 
 import javax.management.Attribute;
 import javax.management.AttributeNotFoundException;
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.InstanceNotFoundException;
 import javax.management.InvalidAttributeValueException;
 import javax.management.MBeanException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MBeanServerFactory;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.management.remote.JMXConnectorServer;
 import javax.management.remote.JMXConnectorServerFactory;
 import javax.management.remote.JMXServiceURL;
 
 import org.apache.log4j.Logger;
 import org.continuent.appia.core.Channel;
 import org.continuent.appia.management.AppiaManagementException;
 
 /**
  * This class defines a ConnectionServerFactory
  * 
  * @author <a href="mailto:nunomrc@di.fc.ul.pt">Nuno Carvalho</a>
  * @version 1.0
  */
 public class ConnectionServerFactory {
 
    private static Logger log = Logger.getLogger(ConnectionServerFactory.class);
 
     private static final Hashtable FACTORIES = new Hashtable();
     private MBeanServer mbeanServer = null;
     private JMXConnectorServer connectorServer = null; 
     private Hashtable managedChannels = null;
     
     private ConnectionServerFactory(JMXConfiguration config) throws AppiaManagementException {
         managedChannels = new Hashtable();
         createMBeanServer(config);
     }
     
     /**
      * Gets the factory instance for a given configuration. Creates the factory if it does not exist.
      * @param config the given configuration.
      * @return the factory instance.
      * @throws AppiaManagementException
      */
     public static ConnectionServerFactory getInstance(JMXConfiguration config) throws AppiaManagementException{
         ConnectionServerFactory factory = (ConnectionServerFactory) FACTORIES.get(config);
         if(factory == null){
             factory = new ConnectionServerFactory(config);
             FACTORIES.put(config,factory);
         }
         return factory;
     }
 
     private void createMBeanServer(JMXConfiguration config) throws AppiaManagementException{
         if(mbeanServer != null)
             return;
         
         log.info("Creating MBean server for this Appia instance.");
         // The MBeanServer
         mbeanServer = MBeanServerFactory.createMBeanServer();
         // Register and start the rmiregistry MBean, needed by JSR 160 RMIConnectorServer
         ObjectName namingName=null;
         try {
             namingName = ObjectName.getInstance("naming:type=rmiregistry");
             mbeanServer.createMBean("mx4j.tools.naming.NamingService", namingName, null);
             log.info("Starting naming service...");
             
             // FIXME: This should discover a free port by default, without the need to be defined by the user.            
             mbeanServer.setAttribute(namingName,new Attribute("Port",new Integer(config.getNamingPort())));
             mbeanServer.invoke(namingName, "start", null, null);
         } catch (MalformedObjectNameException e) {
             throw new AppiaManagementException(e);
         } catch (InstanceAlreadyExistsException e) {
             throw new AppiaManagementException("MBean "+namingName+" already exists",e);
         } catch (MBeanRegistrationException e) {
             throw new AppiaManagementException(e);
         } catch (NotCompliantMBeanException e) {
             throw new AppiaManagementException(e);
         } catch (InstanceNotFoundException e) {
             throw new AppiaManagementException(e);
         } catch (ReflectionException e) {
             throw new AppiaManagementException(e);
         } catch (MBeanException e) {
             throw new AppiaManagementException(e);
         } catch (AttributeNotFoundException e) {
             throw new AppiaManagementException(e);
         } catch (InvalidAttributeValueException e) {
             throw new AppiaManagementException(e);
         }
         
         // Create and start the RMIConnectorServer        
         try{
             log.info("Starting MBean Server...");
             final int namingPort = ((Integer)mbeanServer.getAttribute(namingName, "Port")).intValue();
             final String connectionServiceURL = "service:jmx:rmi://localhost/jndi/rmi://localhost:" + namingPort + "/appia";
             log.info("Starting RMI Connection server on the URL: "+connectionServiceURL);
             connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(new JMXServiceURL(connectionServiceURL), 
                     null, mbeanServer);
             connectorServer.start();
         } catch (AttributeNotFoundException e) {
             throw new AppiaManagementException(e);
         } catch (MalformedURLException e) {
             throw new AppiaManagementException(e);
         } catch (IOException e) {
             throw new AppiaManagementException(e);
         } catch (InstanceNotFoundException e) {
             throw new AppiaManagementException(e);
         } catch (MBeanException e) {
             throw new AppiaManagementException(e);
         } catch (ReflectionException e) {
             throw new AppiaManagementException(e);
         }   
     }
     
     /**
      * Registers a MBean for the Channel.
      * @param channel the managed channel
      * @param mbean the mbean to register.
      * @throws AppiaManagementException
      */
     public void registerMBean(Channel channel, Object mbean)
     throws AppiaManagementException {
         try {
             mbeanServer.registerMBean(mbean, new ObjectName(Channel.class.getName()+":name="+channel.getChannelID()));
             managedChannels.put(channel,mbean);
         } catch (InstanceAlreadyExistsException e) {
             throw new AppiaManagementException(e);
         } catch (NotCompliantMBeanException e) {
             throw new AppiaManagementException(e);
         } catch (MalformedObjectNameException e) {
             throw new AppiaManagementException(e);
         } catch (MBeanRegistrationException e) {
             throw new AppiaManagementException(e);
         }
     }
 
     /**
      * Unregisters the MBean of the Channel.
      * @param channel the managed channel
      * @return the channel manager
      * @throws AppiaManagementException
      */
     public Object unregisterMBean(Channel channel)
     throws AppiaManagementException {
         try {
             mbeanServer.unregisterMBean(new ObjectName(Channel.class.getName()+":name="+channel.getChannelID()));
             return managedChannels.remove(channel);
         } catch (InstanceNotFoundException e) {
             throw new AppiaManagementException(e);
         } catch (MBeanRegistrationException e) {
             throw new AppiaManagementException(e);
         } catch (MalformedObjectNameException e) {
             throw new AppiaManagementException(e);
         }
     }
 
 }
