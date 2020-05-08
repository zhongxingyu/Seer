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
 package org.eclipse.riena.ui.swt.lnf;
 
 import org.eclipse.riena.core.injector.extension.ExtensionInterface;
 import org.eclipse.riena.core.injector.extension.MapName;
 
 /**
  * Descriptor of a look & feel renderer.
  * 
  * @since 1.2
  */
@ExtensionInterface(id = "lnfRenderers")
 public interface ILnfRendererExtension {
 
 	/**
 	 * Returns the key in the table of the renderers of the look and feel.
 	 * 
 	 * @return key
 	 */
 	@MapName("lnfkey")
 	String getLnfKey();
 
 	/**
 	 * Returns the ID of the look and feel.
 	 * 
 	 * @return id
 	 */
 	@MapName("lnfid")
 	String getLnfId();
 
 	/**
 	 * Creates a renderer.
 	 * 
 	 * @return renderer
 	 */
 	@MapName("class")
 	ILnfRenderer createRenderer();
 
 }
