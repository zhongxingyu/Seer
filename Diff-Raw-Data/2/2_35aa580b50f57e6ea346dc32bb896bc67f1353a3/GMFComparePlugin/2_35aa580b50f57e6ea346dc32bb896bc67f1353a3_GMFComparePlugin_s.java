 /*******************************************************************************
  * Copyright (c) 2009, 2011 Tobias Jaehnel and Others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Tobias Jaehnel - Bug#241385
  *   Obeo - rework on generic gmf comparison
  *******************************************************************************/
 package org.eclipse.emf.compare.diagram.ui;
 
 import org.eclipse.gmf.runtime.notation.Edge;
 import org.eclipse.gmf.runtime.notation.Node;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle.
  * 
  * @author Tobias Jaehnel <tjaehnel@gmail.com>
  * @author <a href="mailto:stephane.bouchet@obeo.fr">Stephane Bouchet</a>
  */
 public class GMFComparePlugin extends AbstractUIPlugin {
 
 	/** The plug-in ID. */
 	public static final String PLUGIN_ID = "org.eclipse.emf.compare.diagram.ui"; //$NON-NLS-1$
 
 	/** The contant for the moved icon. */
 	public static final String ICON_MOVED = "EMFCompareGMF.icon.moved"; //$NON-NLS-1$
 
 	/** The contant for the added icon. */
 	public static final String ICON_ADDED = "EMFCompareGMF.icon.added"; //$NON-NLS-1$
 
 	/** The contant for the deleted icon. */
 	public static final String ICON_DELETED = "EMFCompareGMF.icon.deleted"; //$NON-NLS-1$
 
 	/** The shared instance. */
 	private static GMFComparePlugin plugin;
 
 	/**
 	 * The constructor.
 	 */
 	public GMFComparePlugin() {

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
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance.
 	 * 
 	 * @return the shared instance
 	 */
 	public static GMFComparePlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
 	 */
 	@Override
 	protected void initializeImageRegistry(ImageRegistry registry) {
 		registry.put(ICON_ADDED,
 				PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
 		registry.put(ICON_DELETED,
 				PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_DELETE));
 		registry.put(ICON_MOVED,
 				PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_SYNCED));
 	}
 
 	/**
 	 * Returns a SWT image from registry.
 	 * 
 	 * @param key
 	 *            the image key
 	 * @return the image from the registry
 	 */
 	public Image getImage(String key) {
 		return getImageRegistry().get(key);
 	}
 
 	/**
 	 * Utility method to check if the object is a node or edge. Annotation can be set only on these types.
 	 * 
 	 * @param gmfView
 	 *            the view to be tested
 	 * @return true if the object is instance of Node or Edge
 	 */
 	public static boolean isValid(Object gmfView) {
 		return gmfView instanceof Node || gmfView instanceof Edge;
 	}
 }
