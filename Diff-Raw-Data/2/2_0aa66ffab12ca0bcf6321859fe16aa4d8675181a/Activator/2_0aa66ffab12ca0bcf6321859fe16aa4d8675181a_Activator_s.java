 package de.ptb.epics.eve.resources;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.FileLocator;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 /**
  * 
  * @author Marcus Michalsky
  * @since 1.2
  */
 public class Activator implements BundleActivator {
 
 	/** */
	public static String PLUGIN_ID = "de.ptb.epics.eve.resources";
 
 	private static final Logger logger = Logger.getLogger(
 			Activator.class.getName());
 
 	private static BundleContext context;
 	private static Activator plugin;
 
 	/**
 	 * Constructor
 	 */
 	public Activator() {
 		plugin = this;
 	}
 
 	/**
 	 * Returns the activator.
 	 * 
 	 * @return the activator
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the bundle context.
 	 * 
 	 * @return the bundle context
 	 */
 	public static BundleContext getContext() {
 		return context;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
 		Activator.context = bundleContext;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		Activator.context = null;
 	}
 	
 	public static File getDefaultDeviceDefinition() {
 		try {
 			URL url = new URL(
 				"platform:/plugin/de.ptb.epics.eve.resources/cfg/default.xml");
 			File file = new File(FileLocator.toFileURL(url).toURI());
 			return file;
 		} catch (MalformedURLException e1) {
 			logger.error(e1.getMessage(), e1);
 		} catch (URISyntaxException e) {
 			logger.error(e.getMessage(), e);
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the XML schema definition.
 	 * 
 	 * @return the XML schema definition
 	 */
 	public static File getXMLSchema() {
 		try {
 			URL url = new URL(
 				"platform:/plugin/de.ptb.epics.eve.resources/cfg/schema.xsd");
 			File file = new File(FileLocator.toFileURL(url).toURI());
 			return file;
 		} catch (MalformedURLException e1) {
 			logger.error(e1.getMessage(), e1);
 		} catch (URISyntaxException e) {
 			logger.error(e.getMessage(), e);
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the defaults schema definition.
 	 * 
 	 * @return the defaults schema definition
 	 */
 	public static File getDefaultsSchema() {
 		try {
 			URL url;
 			url = new URL(
 				"platform:/plugin/de.ptb.epics.eve.resources/cfg/defaults.xsd");
 			File file = new File(FileLocator.toFileURL(url).toURI());
 			return file;
 		} catch (MalformedURLException e1) {
 			logger.error(e1.getMessage(), e1);
 		} catch (URISyntaxException e) {
 			logger.error(e.getMessage(), e);
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the logging configuration either for debug or production.
 	 * 
 	 * @param debug <code>true</code> if debug mode, <code>false</code> otherwise
 	 * @return the logging configuration or <code>null</code> if an error occurred
 	 */
 	public static File getLoggerConfiguration(boolean debug) {
 		try {
 			URL url;
 			if (debug) {
 				url = new URL(
 					"platform:/plugin/de.ptb.epics.eve.resources/cfg/logger-debug.xml");
 			} else {
 				url = new URL(
 					"platform:/plugin/de.ptb.epics.eve.resources/cfg/logger.xml");
 			}
 			File file = new File(FileLocator.toFileURL(url).toURI());
 			return file;
 		} catch (MalformedURLException e) {
 			logger.error(e.getMessage(), e);
 		} catch (URISyntaxException e) {
 			logger.error(e.getMessage(), e);
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 		} 
 		return null;
 	}
 }
