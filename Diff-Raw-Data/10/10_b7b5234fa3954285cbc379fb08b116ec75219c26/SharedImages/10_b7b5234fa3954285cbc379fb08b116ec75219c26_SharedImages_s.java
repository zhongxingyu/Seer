 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 
 /**
  * Provides access to a set of shared images. The images can be accessed using
  * one of the predifined string constants.
  * <p>
  * Example:
  * 
  * <pre>
  * Image image = Activator.getSharedImage(SharedImages.IMG_LEAF);
  * </pre>
  * 
  * @see Activator
  */
 public final class SharedImages {
 
 	/**
 	 * Collapsed icon used in trees.
 	 */
 	public static final String IMG_NODE_COLLAPSED = "IMG_NODE_COLLAPSED"; //$NON-NLS-1$
 	/**
 	 * Expanded icon used in trees.
 	 */
 	public static final String IMG_NODE_EXPANDED = "IMG_NODE_EXPANDED"; //$NON-NLS-1$
 	/**
 	 * Lead icon used in trees.
 	 */
 	public static final String IMG_LEAF = "IMG_LEAF"; //$NON-NLS-1$
 	/**
 	 * Checked check box image.
 	 */
 	public static final String IMG_CHECKED = "IMG_CHECKED"; //$NON-NLS-1$
 	/**
 	 * Unchecked check box image.
 	 */
 	public static final String IMG_UNCHECKED = "IMG_UNCHECKED"; //$NON-NLS-1$
 	/**
 	 * Error decoration for markers.
 	 */
 	public static final String IMG_ERROR_DECO = "IMG_ERROR_DECO"; //$NON-NLS-1$
 
 	static void initializeImageRegistry(ImageRegistry reg) {
 		doPut(reg, IMG_NODE_COLLAPSED, SharedImages.class, "node_collapsed.gif"); //$NON-NLS-1$
 		doPut(reg, IMG_NODE_EXPANDED, SharedImages.class, "node_expanded.gif"); //$NON-NLS-1$
 		doPut(reg, IMG_LEAF, SharedImages.class, "leaf.gif"); //$NON-NLS-1$
 		doPut(reg, IMG_CHECKED, SharedImages.class, "checkbox_checked.gif"); //$NON-NLS-1$
 		doPut(reg, IMG_UNCHECKED, SharedImages.class, "checkbox_unchecked.gif"); //$NON-NLS-1$
 		if (Activator.getDefault() != null) { // running as plug-in
			reg.put(IMG_ERROR_DECO, LnfManager.getLnf().getImage(ILnfKeyConstants.ERROR_MARKER_ICON));
 		} else {
 			doPut(reg, IMG_ERROR_DECO, SharedImages.class, "errorMarker.png"); //$NON-NLS-1$
 		}
 	}
 
 	private SharedImages() {
 		super();
 	}
 
 	// helping methods
 	// ////////////////
 
 	private static void doPut(ImageRegistry reg, String key, Class<?> location, String filename) {
 		ImageDescriptor descr = ImageDescriptor.createFromFile(location, filename);
 		if (descr == null) {
 			descr = ImageDescriptor.getMissingImageDescriptor();
 		}
 		reg.put(key, descr);
 	}
 
 }
