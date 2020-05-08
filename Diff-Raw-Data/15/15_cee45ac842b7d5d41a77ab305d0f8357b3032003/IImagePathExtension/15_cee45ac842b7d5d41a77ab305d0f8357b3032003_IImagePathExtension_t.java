 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.utils;
 
 import org.osgi.framework.Bundle;
 
 import org.eclipse.riena.core.injector.extension.ExtensionInterface;
 
 /**
  * Extension interface for the path of images.
  */
@ExtensionInterface(id = "imagePaths")
 public interface IImagePathExtension {
 
 	/**
 	 * Returns the path of the images.
 	 * 
 	 * @return path of images
 	 */
 	String getPath();
 
 	/**
 	 * Returns the contributing bundle. This is required for loading the images.
 	 * 
 	 * @return the contributing bundle
 	 */
 	Bundle getContributingBundle();
 
 }
