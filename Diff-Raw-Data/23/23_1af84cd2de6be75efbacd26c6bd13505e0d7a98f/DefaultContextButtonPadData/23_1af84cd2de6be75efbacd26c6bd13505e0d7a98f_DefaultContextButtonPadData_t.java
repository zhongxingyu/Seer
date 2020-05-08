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
 *    mwenz - Bug 331290 - Allowed subclassing (JavaDoc) 
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
 * Users may subclass this class.
 * <p>
 * NOTE: By doing so it is also possible to alter the standard behavior of the
 * editor (e.g. change the location of the standard context button pad). This
 * might lead to inconsistent behavior in different editor implemented on top of
 * Graphiti, which might be irritating to users. From a consistency point of
 * view it is advisable in such cases to stick to the Graphiti standard, and to
 * only change it in case you really need to.
 * 
  * @noinstantiate This class is not intended to be instantiated by clients.
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
 
	@Override
 	public List<IContextButtonEntry> getGenericContextButtons() {
 		return this.genericContextButtons;
 	}
 
	@Override
 	public List<IContextButtonEntry> getDomainSpecificContextButtons() {
 		return this.domainSpecificContextButtons;
 	}
 
	@Override
 	public IContextButtonEntry getCollapseContextButton() {
 		return this.collapseContextButton;
 	}
 
	@Override
 	public void setCollapseContextButton(IContextButtonEntry collapseContextButton) {
 		this.collapseContextButton = collapseContextButton;
 	}
 
	@Override
 	public IRectangle getPadLocation() {
 		return this.location;
 	}
 }
