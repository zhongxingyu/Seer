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
 package org.eclipse.graphiti.tb;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.graphiti.datatypes.IRectangle;
 import org.eclipse.graphiti.internal.datatypes.impl.RectangleImpl;
 
 /**
  * A very simple implementation of {@link IContextButtonPadData} without any
  * real functionality.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
  */
 public class DefaultContextButtonPadData implements IContextButtonPadData {
 
 	private List<IContextButtonEntry> genericContextButtons;
 	private List<IContextButtonEntry> domainSpecificContextButtons;
 	private IContextButtonEntry collapseContextButton;
 	private IRectangle location;
 
 	/**
 	 * Creates a new {@link DefaultContextButtonPadData}.
 	 */
 	public DefaultContextButtonPadData() {
 		this.genericContextButtons = new ArrayList<IContextButtonEntry>();
 		this.domainSpecificContextButtons = new ArrayList<IContextButtonEntry>();
 		this.location = new RectangleImpl(0, 0, 0, 0);
 	}
 
 	public List<IContextButtonEntry> getGenericContextButtons() {
 		return this.genericContextButtons;
 	}
 
 	public List<IContextButtonEntry> getDomainSpecificContextButtons() {
 		return this.domainSpecificContextButtons;
 	}
 
 	public IContextButtonEntry getCollapseContextButton() {
 		return this.collapseContextButton;
 	}
 
 	public void setCollapseContextButton(IContextButtonEntry collapseContextButton) {
 		this.collapseContextButton = collapseContextButton;
 	}
 
 	public IRectangle getPadLocation() {
 		return this.location;
 	}
 }
