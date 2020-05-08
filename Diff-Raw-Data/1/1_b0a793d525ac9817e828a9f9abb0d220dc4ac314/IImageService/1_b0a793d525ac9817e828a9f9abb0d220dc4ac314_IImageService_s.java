 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.services;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * This interface provides services for the creation of images.
  * 
  * @noimplement This interface is not intended to be implemented by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public interface IImageService {
 	/**
 	 * Gets an image descriptor for the given image id. This image id must be
 	 * supported by an graphiti image provider. The image registry of the plugin
 	 * <code>org.eclipse.graphiti.ui</code> is used. This ensures that the image
 	 * descriptor will only created once.
 	 * 
 	 * @param imageId
 	 *            the image id which is supported by an graphiti image provider
 	 * 
 	 * @return the image descriptor for the id
 	 * 
 	 * @see org.eclipse.jface.resource.ImageDescriptor
 	 */
 	ImageDescriptor getImageDescriptorForId(String imageId);
 
 	/**
 	 * Gets an image for the given image id. This image id must be supported by
 	 * an graphiti image provider. The image registry of the plugin
 	 * <code>org.eclipse.graphiti.ui</code> is used. This ensures that the image
 	 * will only created once. The image returned must not be disposed by the
 	 * caller.
 	 * 
 	 * @param imageId
 	 *            the image id which is supported by an graphiti image provider
 	 * 
 	 * @return the image for the id
 	 * 
 	 * @see org.eclipse.swt.graphics.Image
 	 */
 	Image getImageForId(String imageId);
 
 	/**
 	 * Removes the corresponding image entry from the image registry and
 	 * disposes the corresponding image (if existent). The passed image id must
 	 * be supported by an graphiti image provider. The image registry of the
 	 * plugin <code>org.eclipse.graphiti.ui</code> is used. Only call this
 	 * method if you can guarantee that the image/image descriptor is no longer
 	 * in use.
 	 * 
 	 * @param imageId
 	 *            the image id which is supported by an graphiti image provider
 	 * 
 	 * @see org.eclipse.swt.graphics.Image
 	 */
 	void removeImageFromRegistry(String imageId);
 }
