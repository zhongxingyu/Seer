 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.core.viewers;
 
 import org.eclipse.wazaabi.engine.core.editparts.AbstractWidgetEditPart;
 import org.eclipse.wazaabi.engine.core.editparts.ContainerEditPart;
 import org.eclipse.wazaabi.engine.core.editparts.WidgetEditPart;
 import org.eclipse.wazaabi.engine.core.gef.EditPart;
 import org.eclipse.wazaabi.engine.core.gef.EditPartViewer;
 import org.eclipse.wazaabi.engine.core.gef.RootEditPart;
 
 public abstract class AbstractWidgetRootEditPart extends ContainerEditPart
 		implements RootEditPart {
 
 	private WidgetEditPart contents = null;
 	private EditPartViewer viewer = null;
 
 	public EditPart getContents() {
 		return contents;
 	}
 
 	/**
 	 * Returns <code>this</code>.
 	 * 
 	 * @see org.eclipse.wazaabi.engine.core.gef.EditPart#getRoot()
 	 */
 	public RootEditPart getRoot() {
 		return this;
 	}
 
 	public EditPartViewer getViewer() {
 		return viewer;
 	}
 
 	protected void refreshChildren() {
 	}
 
 	public void renewVisuals() {
 		super.renewVisuals();
 	}
 
 	public void setContents(EditPart editpart) {
		assert editpart instanceof WidgetEditPart || editpart == null;
 		if (contents == editpart)
 			return;
 		if (contents != null)
 			removeChild(contents);
 
 		contents = (WidgetEditPart) editpart;
 
 		if (contents != null) {
 			addChild(contents, -1);
 			((AbstractWidgetEditPart) contents).processPostUIBuilding();
 		}
 		getWidgetView().validate();
 	}
 
 	/**
 	 * @see RootEditPart#setViewer(EditPartViewer)
 	 */
 	public void setViewer(EditPartViewer newViewer) {
 		assert newViewer == null || newViewer instanceof EditPartViewer;
 		if (viewer == newViewer)
 			return;
 		if (viewer != null)
 			unregister();
 		viewer = newViewer;
 		if (viewer != null)
 			register();
 	}
 
 	protected void hookModel() {
 		// there is no model associated to the RootEditPart
 	}
 
 	protected void unhookModel() {
 		// there is no model associated to the RootEditPart
 	}
 
 	@Override
 	protected void refreshVisuals() {
 	}
 
 	@Override
 	public void refresh() {
 		if (getContents() != null)
 			getContents().refresh();
 	}
 
 }
