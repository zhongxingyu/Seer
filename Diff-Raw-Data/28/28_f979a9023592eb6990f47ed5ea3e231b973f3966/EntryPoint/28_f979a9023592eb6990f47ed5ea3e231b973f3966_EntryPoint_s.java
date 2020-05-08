 package org.nebulostore.peers;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
 import org.nebulostore.appcore.exceptions.NebuloException;
 
 /**
  * NebuloStore entry point.
  * Reads peer class name from configuration file, creates and runs it.
  * @author Bolek Kulbabinski
  */
 public final class EntryPoint {
   private static Logger logger_ = Logger.getLogger(EntryPoint.class);
   private static final String CONFIGURATION_PATH = "resources/conf/Peer.xml";
   private static final String DEFAULT_PEER_CLASS = "org.nebulostore.appcore.Peer";
 
   public static void main(String[] args) {
     try {
       XMLConfiguration config = initConfig();
       setDefaultThreadUncaughtExceptionHandler();
       AbstractPeer peer = createPeer(config);
       Thread peerThread = new Thread(peer, "Peer Main Thread");
       peerThread.start();
       peerThread.join();
     } catch (NebuloException exception) {
       logger_.fatal("Unable to start NebuloStore! (" + exception.getMessage() + ")");
     } catch (InterruptedException e) {
       logger_.fatal("InterruptedException while waiting for peer thread!");
     }
   }
 
   /**
    * Default exception handler for threads.
    *
    * Logs error down and shuts down the application.
    * @author Grzegorz Milka
    */
   private static final class NebuloUncaughtExceptionHandler implements
     Thread.UncaughtExceptionHandler {
     @Override
     public void uncaughtException(Thread t, Throwable e) {
       logger_.fatal("Thread: " + t + " has caught an irrecoverable " +
           "exception: " + e + ". Shutting down Nebulostore.", e);
       System.exit(1);
     }
   }
 
   private static void setDefaultThreadUncaughtExceptionHandler() {
     try {
       Thread.setDefaultUncaughtExceptionHandler(
           new NebuloUncaughtExceptionHandler());
     } catch (SecurityException e) {
       logger_.warn("Caught security exception: " + e +
           " when setting exception handler.");
     }
   }
 
   private static XMLConfiguration initConfig() throws NebuloException {
     try {
       return new XMLConfiguration(CONFIGURATION_PATH);
     } catch (ConfigurationException cex) {
       throw new NebuloException("Configuration read error in: " + CONFIGURATION_PATH, cex);
     }
   }
 
   private static AbstractPeer createPeer(XMLConfiguration xmlConfig) throws NebuloException {
     String className = xmlConfig.getString("class-name", DEFAULT_PEER_CLASS);
     String confClassName = className + "Configuration";
     Class<?> configurationClass = loadConfigurationClass(confClassName, className);
     try {
       GenericConfiguration genericConfig = null;
       if (configurationClass == null) {
         logger_.warn("Configuration class not found, using default.");
         Class<? extends AbstractPeer> peerClass =
             (Class<? extends AbstractPeer>) Class.forName(className);
         genericConfig = new DefaultConfiguration(peerClass);
       } else {
         Constructor<?> ctor = configurationClass.getConstructor();
         genericConfig = (GenericConfiguration) ctor.newInstance();
       }
       genericConfig.setXMLConfig(xmlConfig);
       Injector injector = Guice.createInjector(genericConfig);
       return injector.getInstance(AbstractPeer.class);
     } catch (InstantiationException e) {
       throw new NebuloException("Could not instantiate class " + className + ".", e);
     } catch (IllegalAccessException e) {
       throw new NebuloException("Constructor for class " + className + " is not accessible.", e);
     } catch (ClassNotFoundException e) {
       throw new NebuloException("Class " + className + " not found.", e);
     } catch (SecurityException e) {
       throw new NebuloException("Could not access constructor of class " + className +
           " due to SecurityException.", e);
     } catch (NoSuchMethodException e) {
       throw new NebuloException("Constructor for class " + className + " is not accessible.", e);
     } catch (IllegalArgumentException e) {
       throw new NebuloException("Incorrect parameters for constructor for " + className + ".", e);
     } catch (InvocationTargetException e) {
       throw new NebuloException("Unable to invoke constructor for " + className + ".", e);
     }
   }
 
   private static Class<?> loadConfigurationClass(String confClassName, String className) {
     try {
       return Class.forName(confClassName);
     } catch (ClassNotFoundException e1) {
       return null;
     }
   }
 
   /**
    * Default configuration using given peer name.
    * @author Bolek Kulbabinski
    */
   private static class DefaultConfiguration extends PeerConfiguration {
     Class<? extends AbstractPeer> peerClass_;
 
     protected DefaultConfiguration(Class<? extends AbstractPeer> peerClass) {
       peerClass_ = peerClass;
     }
 
     @Override
     protected void configurePeer() {
       bind(AbstractPeer.class).to(peerClass_);
     }
   }
 
   private EntryPoint() { }
 }
