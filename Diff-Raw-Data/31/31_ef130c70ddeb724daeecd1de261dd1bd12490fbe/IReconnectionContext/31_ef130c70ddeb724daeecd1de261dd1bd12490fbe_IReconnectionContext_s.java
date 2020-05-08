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
 package org.eclipse.graphiti.features.context;
 
 import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 
 /**
  * The Interface IReconnectionContext.
  * 
  * @noimplement This interface is not intended to be implemented by clients.
  * @noextend This interface is not intended to be extended by clients.
  */
 public interface IReconnectionContext extends IContext {
 
 	/**
 	 * Gets the connection.
 	 * 
 	 * @return the connection
 	 */
 	Connection getConnection();
 
 	/**
 	 * Gets the old anchor.
 	 * 
 	 * @return the old anchor
 	 */
 	Anchor getOldAnchor();
 
 	/**
 	 * Gets the new anchor.
 	 * 
 	 * @return the new anchor
 	 */
 	Anchor getNewAnchor();
 
 	/**
 	 * Gets the target pictogram element.
 	 * 
 	 * @return the target pictogram element
 	 */
 	PictogramElement getTargetPictogramElement();
 
 	/**
 	 * Sets the target pictogram element.
 	 * 
 	 * @param targetPictogramElement
 	 *            the new target pictogram element
 	 */
 	void setTargetPictogramElement(PictogramElement targetPictogramElement);
 
 	/**
 	 * Indicate whether the end of an existing connection is being reconnected
 	 * to a new source node or a new target node.
 	 * 
 	 * @return {@link ReconnectionContext#RECONNECT_TARGET} or
 	 *         {@link ReconnectionContext#RECONNECT_SOURCE}
 	 */
 	String getReconnectType();
 }
