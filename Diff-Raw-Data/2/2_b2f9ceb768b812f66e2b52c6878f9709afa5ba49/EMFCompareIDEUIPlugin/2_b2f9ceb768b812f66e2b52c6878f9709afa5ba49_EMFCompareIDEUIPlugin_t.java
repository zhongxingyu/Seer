 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.accessor.IAccessorFactory;
 import org.eclipse.emf.compare.ide.utils.AbstractRegistryEventListener;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.resource.LocalResourceManager;
 import org.eclipse.jface.resource.ResourceManager;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class, controls the plug-in life cycle.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class EMFCompareIDEUIPlugin extends AbstractUIPlugin {
 	/** The plugin ID. */
 	public static final String PLUGIN_ID = "org.eclipse.emf.compare.ide.ui"; //$NON-NLS-1$
 
 	public static final String ACCESSOR_FACTORY_PPID = "accessor_factory"; //$NON-NLS-1$
 
 	/** Plug-in's shared instance. */
 	private static EMFCompareIDEUIPlugin plugin;
 
 	/** Manages the images that were loaded by EMF Compare. */
 	private LocalResourceManager fResourceManager;
 
 	private AbstractRegistryEventListener listener;
 
 	private IAccessorFactory.Registry registry;
 
 	/** Default constructor. */
 	public EMFCompareIDEUIPlugin() {
 		// Empty constructor
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 
 		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 
 		registry = new IAccessorFactory.RegistryImpl();
 
 		listener = new AccessorFactoryExtensionRegistryListener(PLUGIN_ID, ACCESSOR_FACTORY_PPID);
		extensionRegistry.addListener(listener, PLUGIN_ID + "." + ACCESSOR_FACTORY_PPID);
 		listener.readRegistry(extensionRegistry);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		Platform.getExtensionRegistry().removeListener(listener);
 		registry = null;
 
 		if (fResourceManager != null) {
 			fResourceManager.dispose();
 		}
 
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance.
 	 * 
 	 * @return the shared instance.
 	 */
 	public static EMFCompareIDEUIPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * @return the registry
 	 */
 	public IAccessorFactory.Registry getAccessorFactoryRegistry() {
 		return registry;
 	}
 
 	public ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(EMFCompareIDEUIPlugin.PLUGIN_ID, path);
 	}
 
 	public Image getImage(ImageDescriptor descriptor) {
 		ResourceManager rm = getResourceManager();
 		return rm.createImage(descriptor);
 	}
 
 	/**
 	 * Loads an image from this plugin's path and returns it.
 	 * 
 	 * @param path
 	 *            Path to the image we are to load.
 	 * @return The loaded image.
 	 */
 	public Image getImage(String path) {
 		final ImageDescriptor descriptor = imageDescriptorFromPlugin(EMFCompareIDEUIPlugin.PLUGIN_ID, path);
 		Image result = null;
 		if (descriptor != null) {
 			ResourceManager rm = getResourceManager();
 			result = rm.createImage(descriptor);
 		}
 		return result;
 	}
 
 	/**
 	 * Log an {@link Exception} in the {@link #getLog() current logger}.
 	 * 
 	 * @param e
 	 *            the exception to be logged.
 	 */
 	public void log(Throwable e) {
 		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
 	}
 
 	/**
 	 * Log the given message with the give severity level. Severity is one of {@link IStatus#INFO},
 	 * {@link IStatus#WARNING} and {@link IStatus#ERROR}.
 	 * 
 	 * @param severity
 	 *            the severity of the message
 	 * @param message
 	 *            the message
 	 */
 	public void log(int severity, String message) {
 		getLog().log(new Status(severity, PLUGIN_ID, message));
 	}
 
 	/**
 	 * Returns the resource manager for this plugin, creating it if needed.
 	 * 
 	 * @return The resource manager for this plugin, creating it if needed.
 	 */
 	private ResourceManager getResourceManager() {
 		if (fResourceManager == null) {
 			fResourceManager = new LocalResourceManager(JFaceResources.getResources());
 		}
 		return fResourceManager;
 	}
 
 	private class AccessorFactoryExtensionRegistryListener extends AbstractRegistryEventListener {
 
 		static final String TAG_FACTORY = "factory"; //$NON-NLS-1$
 
 		static final String ATT_CLASS = "class"; //$NON-NLS-1$
 
 		static final String ATT_RANKING = "ranking"; //$NON-NLS-1$
 
 		/**
 		 * @param pluginID
 		 * @param extensionPointID
 		 * @param registry
 		 */
 		public AccessorFactoryExtensionRegistryListener(String pluginID, String extensionPointID) {
 			super(pluginID, extensionPointID);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.compare.ide.utils.AbstractRegistryEventListener#readElement(org.eclipse.core.runtime.IConfigurationElement,
 		 *      org.eclipse.emf.compare.ide.utils.AbstractRegistryEventListener.Action)
 		 */
 		@Override
 		protected boolean readElement(IConfigurationElement element, Action b) {
 			if (element.getName().equals(TAG_FACTORY)) {
 				if (element.getAttribute(ATT_CLASS) == null) {
 					logMissingAttribute(element, ATT_CLASS);
 				} else if (element.getAttribute(ATT_RANKING) == null) {
 					String rankingStr = element.getAttribute(ATT_RANKING);
 					try {
 						Integer.parseInt(rankingStr);
 					} catch (NumberFormatException nfe) {
 						logError(element, "Attribute '" + ATT_RANKING
 								+ "' is malformed, should be an integer.");
 					}
 					logMissingAttribute(element, ATT_RANKING);
 				} else {
 					switch (b) {
 						case ADD:
 							try {
 								IAccessorFactory factory = (IAccessorFactory)element
 										.createExecutableExtension(ATT_CLASS);
 								factory.setRanking(Integer.parseInt(element.getAttribute(ATT_RANKING)));
 								IAccessorFactory previous = registry.add(factory);
 								if (previous != null) {
 									log(IStatus.WARNING, "The factory '" + factory.getClass().getName()
 											+ "' is registered twice.");
 								}
 							} catch (CoreException e) {
 								logError(element, e.getMessage());
 							}
 							break;
 						case REMOVE:
 							registry.remove(element.getAttribute(ATT_CLASS));
 							break;
 					}
 					return true;
 				}
 			}
 			return false;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.compare.ide.utils.AbstractRegistryEventListener#logError(org.eclipse.core.runtime.IConfigurationElement,
 		 *      java.lang.String)
 		 */
 		@Override
 		protected void logError(IConfigurationElement element, String string) {
 			log(IStatus.ERROR, string);
 		}
 	}
 }
