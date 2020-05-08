 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.loader.internal;
 
 import java.io.IOException;
 
 import org.amanzi.awe.console.AweConsolePlugin;
 import org.amanzi.neo.loader.core.preferences.DataLoadPreferences;
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.preference.IPersistentPreferenceStore;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.osgi.framework.BundleContext;
 
 /**
  * Activator class for org.amanzi.neo.loader plugin
  * 
  * @author Lagutko_N
  */
 
 public class NeoLoaderPlugin extends Plugin {
     private static final Logger LOGGER = Logger.getLogger(NeoLoaderPlugin.class);
     /** String DEFAULT_CHARSET field */
     public static final String DEFAULT_CHARSET = "UTF-8";
 	
 	/*
 	 * Plugin variable
 	 */
 
 	static private NeoLoaderPlugin plugin;
 	
 
     private IPreferenceStore preferenceStore = null;
 	
 	/*
 	 * Logging properties
 	 */
 	public static boolean debug = false;
 	private static boolean verbose = true;	
 	
 	/**
 	 * Constructor for SplashPlugin.
 	 */
 	public NeoLoaderPlugin() {
 		super();
 		plugin = this;	
 	}
 	
 	@Override
     public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
     public void stop(BundleContext context) throws Exception {
         try {
             ((IPersistentPreferenceStore)getPreferenceStore()).save();
         } catch (IOException e) {
             exception(e);
         }
 		plugin = null;
 		super.stop(context);
 	}
 	
 	/**
 	 * Returns the shared instance.
 	 */
 	public static NeoLoaderPlugin getDefault() {
 		return plugin;
 	}
 	
 	/**
 	 * Print debug message
 	 * 
 	 * @param line
 	 */
 	
 	public static void debug(String line) {
         AweConsolePlugin.debug(line);
 	}
 	
 	/**
 	 * Print info message
 	 * 
 	 * @param line
 	 */
 	
 	public static void info(String line) {
         AweConsolePlugin.info(line);
 	}
 	
 	/**
 	 * Print a notification message
 	 * 
 	 * @param line
 	 */
 	
 	public static void notify(String line) {
         AweConsolePlugin.notify(line);
 	}
 	
 	/**
 	 * Print an error message
 	 * 
 	 * @param line
 	 */
 	
 	public static void error(String line) {
         AweConsolePlugin.error(line);
 	}
 	
 	/**
 	 * Print an exception
 	 * 
 	 * @param line
 	 */
 	
 	public static void exception(Exception e) {
         AweConsolePlugin.exception(e);
 	}
 	
 
     /**
      * Returns the preference store for this plugin
      * 
      * @return the preference store
      */
     public IPreferenceStore getPreferenceStore() {
         // Create the preference store lazily.
         if (preferenceStore == null) {
             preferenceStore = new ScopedPreferenceStore(new InstanceScope(), getBundle().getSymbolicName());
 
         }
         return preferenceStore;
     }
 
     /**
      * Gets character set from preferences
      * 
      * @return
      */
     public String getCharacterSet() {
        String characterSet = getPreferenceStore().getString(DataLoadPreferences.DEFAULT_CHARSET);
         if (characterSet == null) {
             characterSet = DEFAULT_CHARSET;
         }
         return characterSet;
     }
 }
