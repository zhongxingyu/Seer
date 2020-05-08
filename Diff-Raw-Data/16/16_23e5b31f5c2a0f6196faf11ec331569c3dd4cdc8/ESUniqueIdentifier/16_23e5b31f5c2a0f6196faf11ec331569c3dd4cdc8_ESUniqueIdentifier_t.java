 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk, Edgar Mueller - initial API and implementation
  * Edgar Mueller - API annotations
  ******************************************************************************/
 package org.eclipse.emf.emfstore.common.model;
 
 /**
  * Represents a workspace wide unique identifier.
  * 
  * @author emueller
  * @author wesendon
  * 
 * @noextend This interface is not intended to be extended by clients.
  * @noimplement This interface is not intended to be implemented by clients.
  */
 public interface ESUniqueIdentifier {
 
 	/**
 	 * Returns the actual identifier as a string.
 	 * 
 	 * @return the ID as a string
 	 */
 	String getId();
 }
