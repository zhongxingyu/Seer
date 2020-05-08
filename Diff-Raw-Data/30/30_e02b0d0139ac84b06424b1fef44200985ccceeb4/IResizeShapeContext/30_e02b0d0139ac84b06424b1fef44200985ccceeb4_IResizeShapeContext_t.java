 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
 *    mgorning - Bug 342262 - enhanced resize behavior for container shapes
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.features.context;
 
 import org.eclipse.graphiti.mm.pictograms.Shape;
 
 /**
  * The Interface IResizeShapeContext.
  * 
  * @noimplement This interface is not intended to be implemented by clients.
  * @noextend This interface is not intended to be extended by clients.
  */
 public interface IResizeShapeContext extends IResizeContext, IPictogramElementContext {
 
 	/**
 	 * North
 	 * 
 	 * @since 0.9
 	 */
 	int DIRECTION_NORTH = 1;
 	/**
 	 * South
 	 * 
 	 * @since 0.9
 	 */
 	int DIRECTION_SOUTH = 4;
 	/**
 	 * West
 	 * 
 	 * @since 0.9
 	 */
 	int DIRECTION_WEST = 8;
 	/**
 	 * East
 	 * 
 	 * @since 0.9
 	 */
 	int DIRECTION_EAST = 16;
 
 	/**
 	 * North-East: a bit-wise OR of {@link #DIRECTION_NORTH} and
 	 * {@link #DIRECTION_EAST}
 	 * 
 	 * @since 0.9
 	 */
 	int DIRECTION_NORTH_EAST = DIRECTION_NORTH | DIRECTION_EAST;
 	/**
 	 * North-West: a bit-wise OR of {@link #DIRECTION_NORTH} and
 	 * {@link #DIRECTION_WEST}
 	 * 
 	 * @since 0.9
 	 */
 	int DIRECTION_NORTH_WEST = DIRECTION_NORTH | DIRECTION_WEST;
 	/**
 	 * South-East: a bit-wise OR of {@link #DIRECTION_SOUTH} and
 	 * {@link #DIRECTION_EAST}
 	 * 
 	 * @since 0.9
 	 */
 	int DIRECTION_SOUTH_EAST = DIRECTION_SOUTH | DIRECTION_EAST;
 	/**
 	 * South-West: a bit-wise OR of {@link #DIRECTION_SOUTH} and
 	 * {@link #DIRECTION_WEST}
 	 * 
 	 * @since 0.9
 	 */
 	int DIRECTION_SOUTH_WEST = DIRECTION_SOUTH | DIRECTION_WEST;
 
 	/**
 	 * Unspecified direction
 	 * 
 	 * @since 0.9
 	 */
 	int DIRECTION_UNSPECIFIED = 0;
 
 	/**
 	 * Gets the shape.
 	 * 
 	 * @return the shape
 	 */
 	Shape getShape();
 
 	/**
 	 * Returns the specified direction. The direction is specified using
 	 * {@link IResizeShapeContext#NORTH}, {@link IResizeShapeContext#NORTH_EAST}
 	 * , etc.
 	 * 
 	 * @return the direction
 	 * 
 	 * @since 0.9
 	 */
 	int getDirection();
 }
