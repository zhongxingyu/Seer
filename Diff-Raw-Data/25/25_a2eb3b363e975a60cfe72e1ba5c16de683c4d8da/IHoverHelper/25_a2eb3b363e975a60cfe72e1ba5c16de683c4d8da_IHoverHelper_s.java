 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.bpel.ui;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.emf.ecore.EObject;
 
 public interface IHoverHelper {
 	/**
	 * Return a figure which will be used as the hoverhelp figure for the
 	 * specified model object.
 	 * 
	 * @param modelObject  The model object to create hoverhelp for
	 * @return a string which will be displayed in the popup window
 	 */
	public String getHoverFigure(EObject modelObject);
 	
 	/**
	 * Return a string which will be used as the hoverhelp for the
 	 * specified marker.
 	 * 
 	 * @param marker  The marker which the user is hovering over
	 * @return a string which will be displayed in the popup window
 	 */
 	public String getHoverHelp(IMarker marker);
 }
