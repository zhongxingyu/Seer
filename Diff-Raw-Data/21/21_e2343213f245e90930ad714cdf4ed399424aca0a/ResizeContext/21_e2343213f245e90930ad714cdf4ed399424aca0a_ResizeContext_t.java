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
 /*
  * Created on 20.06.2005
  */
 package org.eclipse.graphiti.features.context.impl;
 
 import org.eclipse.graphiti.features.context.IResizeContext;
 
 /**
  * The Class ResizeContext.
  */
public class ResizeContext extends LocationContext implements IResizeContext {
 
 	private int height = -1;
 
 	private int width = -1;
 
 	/**
 	 * Creates a new {@link ResizeContext}.
 	 */
 	public ResizeContext() {
 	}
 
 	public int getHeight() {
 		return this.height;
 	}
 
 	public int getWidth() {
 		return this.width;
 	}
 
 	/**
 	 * Sets the height.
 	 * 
 	 * @param height
 	 *            The height to set.
 	 */
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	/**
 	 * Sets the size.
 	 * 
 	 * @param width
 	 *            the width
 	 * @param height
 	 *            the height
 	 */
 	public void setSize(int width, int height) {
 		this.width = width;
 		this.height = height;
 	}
 
 	/**
 	 * Sets the width.
 	 * 
 	 * @param width
 	 *            The width to set.
 	 */
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	@Override
 	public String toString() {
 		String ret = super.toString();
 		ret = ret + "(width=" + getWidth() + ", height=" + getHeight() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		return ret;
 	}
 }
