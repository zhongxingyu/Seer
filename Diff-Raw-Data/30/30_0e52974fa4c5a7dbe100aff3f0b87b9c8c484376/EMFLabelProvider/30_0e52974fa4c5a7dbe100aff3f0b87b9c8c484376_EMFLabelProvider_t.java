 /*  
  * Copyright (c) 2006, Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 
 package org.eclipse.emf.compare.ui;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Generic emf label provider
  * 
 * @author Cedric Brun  <a href="mailto:cedric.brun@obeo.fr ">cedric.brun@obeo.fr</a> 
  * 
  */
 public class EMFLabelProvider extends AdapterFactoryLabelProvider {
 	/**
 	 * Initializer
 	 * 
 	 */
 	public EMFLabelProvider() {
 		super(EMFAdapterFactoryProvider.getAdapterFactory());
 	}
 
 	/**
 	 * Returns the platform icon for a file. You can replace with your own icon
 	 * If not a IFile, then passes to the regular EMF.Edit providers
 	 */
 	public Image getImage(final Object object) {
 		if (object instanceof IFile) {
 			return PlatformUI.getWorkbench().getSharedImages().getImage(
 					ISharedImages.IMG_OBJ_FILE);
 		}
 		return super.getImage(object);
 	}
 
 	public String getText(final Object object) {
 		if (object instanceof IFile) {
 			return ((IFile) object).getName();
 		}
 		return super.getText(object);
 	}
 }
