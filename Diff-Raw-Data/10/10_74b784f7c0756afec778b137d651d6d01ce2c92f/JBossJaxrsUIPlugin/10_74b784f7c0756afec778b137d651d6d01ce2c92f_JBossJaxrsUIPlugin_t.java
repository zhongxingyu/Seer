 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.ui;
 
 import java.net.URL;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.DecorationOverlayIcon;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.jboss.tools.ws.jaxrs.ui.internal.utils.Logger;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class JBossJaxrsUIPlugin extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.jboss.tools.ws.jaxrs.ui"; //$NON-NLS-1$
 
 	// The shared instance
 	private static JBossJaxrsUIPlugin plugin;
 	
	private ImageRegistry imageRegistry = null;
 
	private ImageRegistry imageDescriptorRegistry = null;
 	/**
 	 * The constructor
 	 */
 	public JBossJaxrsUIPlugin() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
		imageRegistry = new ImageRegistry(Display.getDefault());
		imageDescriptorRegistry = new ImageRegistry(Display.getDefault());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		imageRegistry.dispose();
 		imageDescriptorRegistry.dispose();
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static JBossJaxrsUIPlugin getDefault() {
 		return plugin;
 	}
 	
 	/**
 	 * Creates an image by loading it from a file in the plugin's images
 	 * directory, and then keeping it in the plugin's image registry for later calls.
 	 * 
 	 * @param imagePath path to the image, relative to the /icons directory of the plugin
 	 * @return The image object loaded from the image file
 	 */
 	public final ImageDescriptor getImageDescriptor(final String imagePath) {
 		if(imageDescriptorRegistry.get(imagePath) == null) {
 			imageDescriptorRegistry.put(imagePath, createImageDescriptor(imagePath, 0));
 		}
 		return imageDescriptorRegistry.getDescriptor(imagePath);
 	}
 
 	/**
 	 * Creates an image by loading it from a file in the plugin's images
 	 * directory, and then keeping it in the plugin's image registry for later calls.
 	 * 
 	 * @param imagePath path to the image, relative to the /icons directory of the plugin
 	 * @return The image object loaded from the image file
 	 */
 	public final Image getImage(final String imagePath) {
 		return getImage(imagePath, 0);
 	}
 	
 	/**
 	 * Creates an image by loading it from a file in the plugin's images
 	 * directory, adding a decorator at the bottom left corner of it, and then
 	 * keeping it in the plugin's image registry for later calls.
 	 * 
 	 * @param imagePath
 	 *            path to the image, relative to the /icons directory of the
 	 *            plugin
 	 * @return The image object loaded from the image file
 	 */
 	public final Image getImage(final String imagePath, final int level) {
 		Logger.debug("Loading image {} with decorator level {}", imagePath, level);
 		if(imagePath == null) {
 			return null;
 		}
 		final String imageKey = imagePath.concat(String.valueOf(level));
 		if(imageRegistry.get(imageKey) == null) {
 			final ImageDescriptor imageDescriptor = createImageDescriptor(imagePath, level);
 			imageRegistry.put(imageKey, imageDescriptor.createImage());
 		}
 		return imageRegistry.get(imageKey);
 	}
 	
 	/**
 	 * Creates an image descriptor by loading it from a file in the plugin's images
 	 * directory.
 	 * 
 	 * @param imagePath path to the image, relative to the /icons directory of the plugin
 	 * @return The image object loaded from the image file
 	 */
 	public final ImageDescriptor createImageDescriptor(final String imagePath, final int problemLevel) {
 		final IPath imageFilePath = new Path("/icons/" + imagePath);
 		final URL imageFileUrl = FileLocator.find(this.getBundle(), imageFilePath, null);
 		final ImageDescriptor baseImageDescriptor = ImageDescriptor.createFromURL(imageFileUrl);
 		switch (problemLevel) {
 		case IMarker.SEVERITY_ERROR:
 			return createDecoratedImageDescriptor(baseImageDescriptor, "org.eclipse.jface.fieldassist.IMG_DEC_FIELD_ERROR");
 		case IMarker.SEVERITY_WARNING:
 			return createDecoratedImageDescriptor(baseImageDescriptor, "org.eclipse.jface.fieldassist.IMG_DEC_FIELD_WARNING");
 
 		default:
 			return baseImageDescriptor;
 		}
 	}
 
 	/**
 	 * Creates an image from the given baseImage with the given decorator at the bottom left corner
 	 * @param baseImage
 	 * @param decoratorId
 	 * @return
 	 * 
 	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=383810 to use PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR),
 	 * which returns null for now.
 	 */
 	private ImageDescriptor createDecoratedImageDescriptor(final ImageDescriptor baseImageDescriptor, String decoratorId) {
 		final ImageDescriptor decoratorDescriptor = JFaceResources.getImageRegistry().getDescriptor(decoratorId);
 		final Image baseImage = baseImageDescriptor.createImage();
 		final DecorationOverlayIcon result = new DecorationOverlayIcon(baseImage, new ImageDescriptor[] { null, null, decoratorDescriptor, null, null },
 				new Point(16, 16));
 		return result;
 	}
 	
 }
