 /*******************************************************************************
  * Copyright (c) 2004, 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.ui;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jst.jsf.common.ui.internal.guiutils.Alerts;
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.jsf.facesconfig.ui.preference.GEMPreferences;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class EditorPlugin extends AbstractUIPlugin {//implements IStartup {
 
 	// The shared instance.
 	private static EditorPlugin plugin;
 
 	private Logger log;
 
 	private ResourceBundle resourceBundle;
 
 	private static Alerts alerts;
 
 	private URL pluginBase;
 
 	/**
 	 * The constructor.
 	 */
 	public EditorPlugin() {
 		super();
 		plugin = this;
 	}
 	
 
 	/**
 	 * This method is called upon plug-in activation
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		try {
 			// get resource bundle.
 			resourceBundle = ResourceBundle
 					.getBundle("org.eclipse.jst.jsf.facesconfig.ui.EditorPluginResources"); //$NON-NLS-1$
 			alerts = new Alerts(this, resourceBundle);
 			pluginBase = getBundle().getEntry("/"); //$NON-NLS-1$
 
 			// set up logging for this plugin and everthing under it.
 			log = new Logger(this.getBundle(), resourceBundle);
 
 			// log.info("log.StartMessage", CommonPlugin.getVersion());
 		} catch (Exception ee) {
 			// only log if the logger was configured correctly.
 			if (log != null) {
 				log.error("log.msg", //$NON-NLS-1$
 						"Problems starting plug-in Faces Config Editor.", ee); //$NON-NLS-1$
 			}
 
 			throw new CoreException(new Status(IStatus.ERROR, getBundle()
 					.getSymbolicName(), IStatus.OK,
 					"Problems starting plug-in Faces Config Editor", ee)); //$NON-NLS-1$
 		}
 		
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		plugin = null;
 	}
 
 	/**
 	 * @return the shared instance.
 	 */
 	public static EditorPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * @return the alerts objects associated with this plugin for alerting the user.
 	 */
 	public static Alerts getAlerts() {
 		return alerts;
 	}
 
 	/**
 	 * Returns a logger for the new class using this plugin for reference.
 	 * @param theClass 
 	 * @return the default root logger
 	 */
 	public static Logger getLogger(Class theClass) {
 		return getDefault().getRootLogger(); // .getLogger(theClass);
 	}
 
 	/**
 	 * Returns the plugin's root logger
 	 * @return the root logger
 	 */
 	public Logger getRootLogger() {
 		return log;
 	}
 
 	/**
 	 * Returns the plugin's resource bundle,
 	 * @return the resource bundle
 	 */
 	public ResourceBundle getResourceBundle() {
 		return resourceBundle;
 	}
 
 	/**
 	 * Returns the string from the plugin's resource bundle, or 'key' if not
 	 * found.
 	 * @param key 
 	 * @return the resource bundle string for key
 	 */
 	public static String getResourceString(String key) {
 		ResourceBundle bundle = getDefault().getResourceBundle();
 		try {
 			return (bundle != null) ? bundle.getString(key) : key;
 		} catch (MissingResourceException e) {
 			return key;
 		}
 	}
 
 	/**
 	 * Returns the standard display to be used. The method first checks, if the
 	 * thread calling this method has an associated dispaly. If so, this display
 	 * is returned. Otherwise the method returns the default display.
 	 * @return the standard display
 	 */
 	public static Display getStandardDisplay() {
 		Display display;
 		display = Display.getCurrent();
 		if (display == null) {
 			display = Display.getDefault();
 		}
 		return display;
 	}
 
 	/**
 	 * Returns the active workbench window.
 	 * 
 	 * @return the active workbench window. this can be null but I've never seen
 	 *         it.
 	 */
 	public static IWorkbenchWindow getActiveWorkbenchWindow() {
 		if (getDefault().getWorkbench() == null) {
 			return null;
 		}
         return getDefault().getWorkbench().getActiveWorkbenchWindow();
 	}
 
 	/**
 	 * @return the active shell
 	 */
 	public static Shell getActiveShell() {
 		Shell shell = null;
 		IWorkbenchWindow workbenchWindow = getActiveWorkbenchWindow();
 		if (workbenchWindow.getShell() != null) {
 			shell = workbenchWindow.getShell();
 		} else {
 			shell = new Shell();
 		}
 		return shell;
 	}
 
 	/**
 	 * Returns a shared image for the given name
 	 * <p>
 	 * Note: Images returned from this method will be automitically disposed of
 	 * when this plug-in shuts down. Callers must not dispose of these images
 	 * themselves.
 	 * </p>
 	 * 
 	 * @param name
 	 *            the image name found in /icons (with extension)
 	 * @return the image, null on error or not found.
 	 */
 	public Image getImage(String name) {
 		if (name == null) {
 			return null;
 		}
 
 		ImageRegistry images = getImageRegistry();
 		Image image = images.get(name);
 		if (image == null) {
 			try {
 				ImageDescriptor id = ImageDescriptor.createFromURL(new URL(
 						pluginBase, "icons/" + name)); //$NON-NLS-1$
 				images.put(name, id);
 
 				image = images.get(name);
 			} catch (MalformedURLException ee) {
 				// log.EditorPlugin.image.error=Image {0} not found.
 				log.error("log.msg", "log.EditorPlugin.image.error", name, ee); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		return image;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given plug-in
 	 * relative path.
 	 * 
 	 * @param path
 	 *            the path
 	 * @return the image descriptor
 	 */
 	public ImageDescriptor getImageDescriptor(String path) {
 		if (path == null) {
 			return null;
 		}
 
 		ImageRegistry images = getImageRegistry();
 		ImageDescriptor id = images.getDescriptor(path);
 		if (id == null) {
 			try {
 				id = ImageDescriptor.createFromURL(new URL(pluginBase, "icons/" //$NON-NLS-1$
 						+ path));
 				images.put(path, id);
 			} catch (MalformedURLException ee) {
 				// log.EditorPlugin.image.error=Image {0} not found.
 				log.error("log.msg", "log.EditorPlugin.image.error", path, ee); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		return id;
 	}
 
 	/**
 	 * Returns this plugin's unique identifier
 	 * 
 	 * @return this plugin's unique identifier
 	 * 
 	 */
 	public static String getPluginId() {
 		return getDefault().getBundle().getSymbolicName();
 	}
 
 	protected void initializeDefaultPluginPreferences() {
		GEMPreferences.initializeDefaults();
 	}
 }
