 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package th.co.geniustree.virgo.server.api;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanException;
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.management.remote.JMXConnector;
 import javax.swing.SwingUtilities;
 import th.co.geniustree.virgo.server.JmxConnectorHelper;
 import th.co.geniustree.virgo.server.VirgoServerInstanceImplementation;
 
 /**
  *
  * @author pramoth
  */
 public class Deployer {
 
     private final VirgoServerInstanceImplementation instance;
 
     public Deployer(VirgoServerInstanceImplementation instance) {
         this.instance = instance;
     }
 
     public void deploy(File file, boolean recoverable) throws Exception {
         if (SwingUtilities.isEventDispatchThread()) {
             throw new IllegalStateException("Can't call in EDT.");
         }
         JMXConnector connector = null;
         try {
             connector = JmxConnectorHelper.createConnector(instance.getAttr());
         } catch (IOException iOException) {
             StartCommand startCommand = instance.getLookup().lookup(StartCommand.class);
             if (startCommand != null) {
                connector = startCommand.startAndWait(true);
             }
         }
         if (connector != null) {
             try {
                 MBeanServerConnection mBeanServerConnection = connector.getMBeanServerConnection();
                 ObjectName name = new ObjectName(Constants.MBEAN_DEPLOYER);
                 Object[] params = {file.toURI().toString(), recoverable};
                 Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deploy {0} ", new String[]{file.toURI().toString()});
                 String[] signature = {"java.lang.String", "boolean"};
                 // invoke the execute method of the Deployer MBean
                 mBeanServerConnection.invoke(name, "deploy", params, signature);
             } catch (IOException iOException) {
                 Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Can't connect Virgo JMX.", iOException);
                 instance.stoped();
             } catch (MalformedObjectNameException malformedObjectNameException) {
                 Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Can't connect Virgo JMX.", malformedObjectNameException);
                 instance.stoped();
             } catch (InstanceNotFoundException instanceNotFoundException) {
                 Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Can't connect Virgo JMX.", instanceNotFoundException);
                 instance.stoped();
             } catch (MBeanException mBeanException) {
                 Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Operation fail..", mBeanException);
             } catch (ReflectionException reflectionException) {
                 Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Operation fail..", reflectionException);
             } finally {
                 JmxConnectorHelper.silentClose(connector);
             }
 
         } else {
             Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Can't connect Virgo JMX.");
         }
     }
 
     public void undeploy(String simbolicname, String bundleVersion) {
         if (SwingUtilities.isEventDispatchThread()) {
             throw new IllegalStateException("Ca'nt call in EDT.");
         }
         JMXConnector connector = null;
         try {
             connector = JmxConnectorHelper.createConnector(instance.getAttr());
             MBeanServerConnection mBeanServerConnection = connector.getMBeanServerConnection();
             ObjectName name = new ObjectName(Constants.MBEAN_DEPLOYER);
             Object[] params = {simbolicname, bundleVersion};
             Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Undeploy  {0} ;version={1}", new String[]{simbolicname, bundleVersion});
             String[] signature = {"java.lang.String", "java.lang.String"};
             try {
                 // invoke the execute method of the Deployer MBean
                 mBeanServerConnection.invoke(name, "undeploy", params, signature);
             } catch (Exception ex) {
                 Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Can't undeploy bundle {0} ;version={1}", new String[]{simbolicname, bundleVersion});
             }
         } catch (Exception ex) {
             Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Can't connect Virgo JMX.", ex);
         } finally {
             JmxConnectorHelper.silentClose(connector);
         }
     }
 
     public void refresh(File file, String bundleVersion) {
         if (SwingUtilities.isEventDispatchThread()) {
             throw new IllegalStateException("Ca'nt call in EDT.");
         }
         JMXConnector connector = null;
         try {
             connector = JmxConnectorHelper.createConnector(instance.getAttr());
             MBeanServerConnection mBeanServerConnection = connector.getMBeanServerConnection();
             ObjectName name = new ObjectName(Constants.MBEAN_DEPLOYER);
             Object[] params = {file.toURI().toString(), bundleVersion};
             Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Refresh {0} ;version={1}", new String[]{file.toURI().toString(), bundleVersion});
             String[] signature = {"java.lang.String", "java.lang.String"};
             try {
                 // invoke the execute method of the Deployer MBean
                 mBeanServerConnection.invoke(name, "refresh", params, signature);
             } catch (Exception ex) {
                 Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Can't refresh bundle {0} ;version={1}", new String[]{file.toURI().toString(), bundleVersion});
             }
         } catch (Exception ex) {
             Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Can't connect Virgo JMX.", ex);
         } finally {
             JmxConnectorHelper.silentClose(connector);
         }
     }
 }
