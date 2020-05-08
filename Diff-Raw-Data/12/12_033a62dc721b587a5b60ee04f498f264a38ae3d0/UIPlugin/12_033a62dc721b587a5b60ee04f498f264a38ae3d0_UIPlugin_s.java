 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.activator;
 
 import java.net.URL;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.te.runtime.preferences.ScopedEclipsePreferences;
 import org.eclipse.tcf.te.runtime.tracing.TraceHandler;
 import org.eclipse.tcf.te.ui.views.Managers;
 import org.eclipse.tcf.te.ui.views.interfaces.ImageConsts;
 import org.eclipse.tcf.te.ui.views.listeners.WorkbenchWindowListener;
 import org.eclipse.ui.IWindowListener;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class UIPlugin extends AbstractUIPlugin {
 	// The shared instance
 	private static UIPlugin plugin;
 	// The scoped preferences instance
 	private static volatile ScopedEclipsePreferences scopedPreferences;
 	// The trace handler instance
 	private static volatile TraceHandler traceHandler;
 
 	// The global window listener instance
 	private IWindowListener windowListener;
 
 	/**
 	 * The constructor
 	 */
 	public UIPlugin() {
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static UIPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Convenience method which returns the unique identifier of this plugin.
 	 */
 	public static String getUniqueIdentifier() {
 		if (getDefault() != null && getDefault().getBundle() != null) {
 			return getDefault().getBundle().getSymbolicName();
 		}
 		return "org.eclipse.tcf.te.ui.views"; //$NON-NLS-1$
 	}
 
 	/**
 	 * Return the scoped preferences for this plugin.
 	 */
 	public static ScopedEclipsePreferences getScopedPreferences() {
 		if (scopedPreferences == null) {
 			scopedPreferences = new ScopedEclipsePreferences(getUniqueIdentifier());
 		}
 		return scopedPreferences;
 	}
 
 	/**
 	 * Returns the bundles trace handler.
 	 *
 	 * @return The bundles trace handler.
 	 */
 	public static TraceHandler getTraceHandler() {
 		if (traceHandler == null) {
 			traceHandler = new TraceHandler(getUniqueIdentifier());
 		}
 		return traceHandler;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 
 		if (windowListener == null && PlatformUI.getWorkbench() != null) {
 			windowListener = new WorkbenchWindowListener();
 			PlatformUI.getWorkbench().addWindowListener(windowListener);
 			activateContexts();
 		}
 	}
 
 	void activateContexts() {
 		if (Display.getCurrent() != null) {
 			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) windowListener.windowOpened(window);
 		}
 		else {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){
 				@Override
                 public void run() {
 					activateContexts();
                 }});
 		}
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		if (windowListener != null && PlatformUI.getWorkbench() != null) {
 			PlatformUI.getWorkbench().removeWindowListener(windowListener);
 			windowListener = null;
 		}
 
 		Managers.dispose();
 
 		plugin = null;
 		scopedPreferences = null;
 		traceHandler = null;
 		super.stop(context);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
 	 */
 	@Override
 	protected void initializeImageRegistry(ImageRegistry registry) {
 		URL url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_EVIEW + "prop_ps.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.EDITOR, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_EVIEW + "targets_view.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.VIEW, ImageDescriptor.createFromURL(url));
 
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_ETOOL + "help.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.HELP, ImageDescriptor.createFromURL(url));
 
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "favorites.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.FAVORITES, ImageDescriptor.createFromURL(url));
 	}
 
 	/**
 	 * Loads the image registered under the specified key from the image
 	 * registry and returns the <code>Image</code> object instance.
 	 *
 	 * @param key The key the image is registered with.
 	 * @return The <code>Image</code> object instance or <code>null</code>.
 	 */
 	public static Image getImage(String key) {
 		return getDefault().getImageRegistry().get(key);
 	}
 
 	/**
 	 * Loads the image registered under the specified key from the image
 	 * registry and returns the <code>ImageDescriptor</code> object instance.
 	 *
 	 * @param key The key the image is registered with.
 	 * @return The <code>ImageDescriptor</code> object instance or <code>null</code>.
 	 */
 	public static ImageDescriptor getImageDescriptor(String key) {
 		return getDefault().getImageRegistry().getDescriptor(key);
 	}
 }
