 package org.hackystat.sensor.eclipse;
 
 import java.net.URL;
 
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 /**
  * Provides a main plug-in functionality for Eclipse, namely instantiation of the
  * <code>org.hackystat.sensor.eclipse.EclipseSensor</code> class to start gathering
  * necessary data.
  * <p>
  * Since <code>earlyStartup()</code> method was called when Eclipse runs, there is a major
  * instantiation in the method such as instantiation of EclipseSensor.
  * 
  * 
  * Please note that resource bundle is defined since Eclipse ver3. Basically bundle is 
  * interchangable with plugin. 
  *
  * @author Hongbing Kou
  */
 public class EclipseSensorPlugin extends AbstractUIPlugin implements BundleActivator {
  /** The plug-in ID. */
   public static final String PLUGIN_ID = "org.hackystat.sensor.eclipse";
   /** The shared instance (note that it is different from singleton). */
   public static EclipseSensorPlugin plugin;
   
   /**
    * Creates an Hackystat sensor plug-in runtime object for the given plug-in descriptor.
    * <p>Note that instances of plug-in runtime classes are automatically created by
    * the platform in the course of plug-in activation.
    *
    */
   public EclipseSensorPlugin() {
     super();
     // Note that this is a non-standard way to initialize a singleton instance
     // due to Eclipse's auto startup nature. 
     EclipseSensorPlugin.plugin = this;
   }
   
   /**
    * Reimplement start to handle Eclipse plugin version check.
    * 
    * 
    * @param context Bundle context for Hackystat sensor.
    * @throws Exception If error while starting hackystat sensor.
    */
   public void start(BundleContext context) throws Exception {
     //Note that eclipse impose a time limitation on this method. The time consuming
     //sensor initialization code is moved to earlyStartup().
     super.start(context);
     
     // Preference.
 //    IPreferenceStore store = this.getPreferenceStore();
   
 //    boolean isSensorAutoUpdateEnabled = store.getBoolean(PreferenceConstants.P_ENABLE_AUTOUPDATE);
 //  
 //    if (isSensorAutoUpdateEnabled) {
 //      String updateSite = store.getString(PreferenceConstants.P_UPDATE_SITE);
 //      SensorUpdateThread sensorUpdateThread = new SensorUpdateThread(context.getBundle(), 
 //            updateSite);
 //      sensorUpdateThread.start();
 //    }
   }
 
   /**
    * Reimplement stop to terminate the Hackystat sensor.
    * 
    * @param context Bundle context for Hackystat sensor.
    * @throws Exception If error while starting hackystat sensor.
    */
   public void stop(BundleContext context) throws Exception {
     super.stop(context);
     
     EclipseSensor sensor = EclipseSensor.getInstance();
     sensor.stop();
   }
   
   /**
   * Returns the shared instance.
    *
    * @return the shared instance
    */
   public static EclipseSensorPlugin getDefault() {
     return plugin;
   }
   
   /**
   * Returns the shared instance.
    *
    * @return the shared instance
    */
   public static EclipseSensorPlugin getInstance() {
     return plugin;
   }
   
   /**
    * Implements an inner thread class to handle sensor update.  
    */
   private class SensorUpdateThread extends Thread {
     /** Resource bundle. */
     private Bundle bundle;
     /** Update site. */
     private String updateSite; 
     
     /**
      * Hackystat sensor update thread.
      * 
      * @param bundle Plugin bundle.
      * @param updateSite Update site.
      */
     SensorUpdateThread(Bundle bundle, String updateSite) {
       this.setName("SensorUpdateThread");
       this.updateSite = updateSite;
     }
     
     /**
      * Thread execution function.
      */
     public void run() {
       String title = EclipseSensorI18n.getString("VersionCheck.messageDialogTitle");
       String first = EclipseSensorI18n.getString("VersionCheck.messageDialogMessageFirst");
       String betweenKey = "VersionCheck.messageDialogMessageBetween";
       String between = EclipseSensorI18n.getString(betweenKey);
       String last = EclipseSensorI18n.getString("VersionCheck.messageDialogMessageLast");
       String messages[] = {first, between, last};
       
       VersionCheck versionCheck = new VersionCheck(this.bundle); 
       versionCheck.processUpdateDialog(updateSite, title, messages);
     }
   }
   
   /**
    * Gets the path to the sensorshell.jar.
    * 
    * @return the path to the sensorshell.jar.
    */
   public String getSensorShellPath() {
     URL pluginUrl = super.getBundle().getEntry("/");
     try {      
       return FileLocator.toFileURL(new URL(pluginUrl, "sensorshell.jar")).getFile();
     }
     catch (Exception e) {
       return null;
     }
   }
   
   /**
    * Returns the workspace instance. This method might be overridden due to AbstractUIPlugin
    * abstract class although it is not necessary to be overridden.
    *
    * @return The IWorkspace instance.
    *
    * @see AbstractUIPlugin
    */
   public static IWorkspace getWorkspace() {
     return ResourcesPlugin.getWorkspace();
   }
 
   /**
    * Logs out the exception or error message for Eclispe sensor plug-in.
    * 
    * @param message Error message.
    * @param e Exception. 
    */
   public void log(String message, Exception e) {
     String pluginName = super.getBundle().getSymbolicName();
     IStatus status = new Status(IStatus.ERROR, pluginName, 0, message + " " + e.getMessage(), e);
     
     plugin.getLog().log(status);
   }
   
   /**
    * Logs out the exception or error message for Eclipse sensor plug-in.
    * 
    * @param e Exception. 
    */
   public void log(Exception e) {
     String pluginName = super.getBundle().getSymbolicName();
     IStatus status = new Status(IStatus.ERROR, pluginName, 0, e.getMessage(), e);
     plugin.getLog().log(status);
   }
 }
