 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 356218 - Added hasDoneChanges updates to update diagram feature
  *                         and called features via editor command stack to check it
  *    mwenz - Bug 363539 - Enabled feature delegation via IDiagramEditor.execute method - contributed by Hernan
  *    Bug 336488 - DiagramEditor API
  *    mwenz - Bug 367204 - Correctly return the added PE inAbstractFeatureProvider's addIfPossible method
  *    mgorning - Bug 371671 - addGraphicalRepresentation returns null in dark mode
  *    pjpaulin - Bug 352120 - Added additional methods required by interface
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.editor;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.platform.IDiagramBehavior;
 import org.eclipse.graphiti.platform.IDiagramContainer;
 
 /**
  * 
  * can be used in the dark feature processing mode
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class DiagramEditorDummy implements IDiagramContainer {
 
 	private IDiagramTypeProvider diagramTypeProvider;
 	private IDiagramBehavior diagramBehavior;
 
 	public DiagramEditorDummy(IDiagramTypeProvider diagramTypeProvider, TransactionalEditingDomain eDomain) {
 		setDiagramTypeProvider(diagramTypeProvider);
		diagramBehavior = new DiagramBehaviorDummy(this, eDomain);
 	}
 
 	public IDiagramTypeProvider getDiagramTypeProvider() {
 		return diagramTypeProvider;
 	}
 
 	public PictogramElement[] getSelectedPictogramElements() {
 		return new PictogramElement[0];
 	}
 
 	public boolean isDirty() {
 		return false;
 	}
 
 	public void refreshTitle() {
 	}
 
 	public void refreshTitleToolTip() {
 	}
 
 	public void selectPictogramElements(PictogramElement[] pictogramElements) {
 	}
 
 	public void setPictogramElementForSelection(PictogramElement pictogramElement) {
 	}
 
 	public void setPictogramElementsForSelection(PictogramElement[] pictogramElements) {
 	}
 
 	protected void setDiagramTypeProvider(IDiagramTypeProvider diagramTypeProvider) {
 		this.diagramTypeProvider = diagramTypeProvider;
 	}
 
 	public boolean isAlive() {
 		return false;
 	}
 
 	public void doSave(IProgressMonitor monitor) {
 	}
 
 	public String getTitle() {
 		return "Dummy Editor"; //$NON-NLS-1$
 	}
 
 	public String getTitleToolTip() {
 		return "Dummy Editor"; //$NON-NLS-1$
 	}
 
 	public void updateDirtyState() {
 	}
 
 	public void close() {
 	}
 
 	public IDiagramBehavior getDiagramBehavior() {
 		return diagramBehavior;
 	}
 }
