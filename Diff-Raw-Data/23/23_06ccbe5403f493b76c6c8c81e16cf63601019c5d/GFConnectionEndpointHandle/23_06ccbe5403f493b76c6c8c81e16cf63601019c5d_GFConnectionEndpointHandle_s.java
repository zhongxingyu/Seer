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
 package org.eclipse.graphiti.ui.internal.util.draw2d;
 
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.gef.ConnectionEditPart;
 import org.eclipse.gef.handles.ConnectionEndpointHandle;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class GFConnectionEndpointHandle extends ConnectionEndpointHandle {
 
 	private static Dimension HANDLE_DIMENSION = new Dimension(6, 6);
 
 	public GFConnectionEndpointHandle(ConnectionEditPart owner, int endPoint) {
 		super(owner, endPoint);
 	}
 
 	@Override
 	protected void init() {
 		setPreferredSize(HANDLE_DIMENSION);
 	}
 }
