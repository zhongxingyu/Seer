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
 * Created on 16.11.2005
 */
 package org.eclipse.graphiti.features.context.impl;
 
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 
 /**
  * The Class ResizeShapeContext.
  */
 public class ResizeShapeContext extends ResizeContext implements IResizeShapeContext {
 
 	private Shape shape;
 	private int direction = DIRECTION_UNSPECIFIED;
 
 	/**
 	 * Creates a new {@link ResizeShapeContext}.
 	 * 
 	 * @param shape
 	 *            the shape
 	 */
 	public ResizeShapeContext(Shape shape) {
 		super();
 		setShape(shape);
 	}
 
 	/**
 	 * Gets the shape.
 	 * 
 	 * @return Returns the shape.
 	 */
 	public Shape getShape() {
 		return this.shape;
 	}
 
 	/**
 	 * @param shape
 	 *            The shape to set.
 	 */
 	private void setShape(Shape shape) {
 		this.shape = shape;
 	}
 
 	public PictogramElement getPictogramElement() {
 		return getShape();
 	}
 
 	@Override
 	public String toString() {
 		String ret = super.toString();
 		return ret + " shape: " + getShape(); //$NON-NLS-1$
 	}
 
 	/**
 	 * @since 0.9
 	 */
 	public int getDirection() {
 		return direction;
 	}
 
 	/**
 	 * @since 0.9
 	 */
 	public void setDirection(int direction) {
 		this.direction = direction;
 	}
 }
