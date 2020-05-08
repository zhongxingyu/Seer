 /**
  * Copyright (c) 2010 Sierra Wireless and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jacques Lescot, Sierra Wireless - initial API and implementation (bug 300462)
  *
 * $Id: RemoveAction.java,v 1.2 2010/06/02 10:16:32 bcabe Exp $
  */
 package org.eclipse.pde.emfforms.editor.actions;
 
 import java.util.List;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.pde.emfforms.editor.EmfActionBarContributor;
 import org.eclipse.pde.emfforms.editor.EmfMasterDetailBlock;
 import org.eclipse.pde.emfforms.internal.Activator;
 import org.eclipse.pde.emfforms.internal.editor.IEmfFormsImages;
 
 public class RemoveAction extends Action {
 
 	public void addPropertyChangeListener(IPropertyChangeListener listener) {
 		getDeleteAction().addPropertyChangeListener(listener);
 	}
 
 	public int getAccelerator() {
 		return getDeleteAction().getAccelerator();
 	}
 
 	public String getActionDefinitionId() {
 		return getDeleteAction().getActionDefinitionId();
 	}
 
 	public String getDescription() {
 		return getDeleteAction().getDescription();
 	}
 
 	public String getToolTipText() {
 		return getDeleteAction().getToolTipText();
 	}
 
 	@Override
 	public void setEnabled(boolean enabled) {
 		getDeleteAction().setEnabled(enabled);
 	}
 
 	public boolean isEnabled() {
 		return getDeleteAction().isEnabled();
 	}
 
 	@Override
 	public String getText() {
 		return getDeleteAction().getText();
 	}
 
 	@Override
 	public void run() {
 		TreeViewer treeViewer = masterDetail.getTreeViewer();
 
		EObject nearestEltToSelect = computeElementToSelectAfterDeletion(treeViewer);
 
 		getDeleteAction().run();
 
 		treeViewer.refresh();
		if (nearestEltToSelect != null) {
			treeViewer.setSelection(new StructuredSelection(nearestEltToSelect));
 			treeViewer.getTree().setFocus();
 		}
 	}
 
 	/**
 	 * Find the next element to be selected in the tree once the delete operation has been performed
 	 * 
 	 * @param treeViewer The {@link TreeViewer}
 	 * @return the next {@link EObject} to be selected in the tree or <code>null</code> if none found. A possible reason to that last case is because there is no remaining element to select in the tree.
 	 */
 	protected EObject computeElementToSelectAfterDeletion(TreeViewer treeViewer) {
 		EObject nextObjToSelect = null;
 
 		// Iterate over all the selected element to be removed to define the new element to select in the tree after the delete operation is performed. 
 		List<EObject> selectedElements = (List<EObject>) ((StructuredSelection) treeViewer.getSelection()).toList();
 		for (EObject eltToRemove : selectedElements) {
 			EObject containerElt = eltToRemove.eContainer();
 			EStructuralFeature eContainingFeature = eltToRemove.eContainingFeature();
 			// Retrieve all the siblings elements and try to select the nearest one
 			Object featureValue = containerElt.eGet(eContainingFeature);
 			if (featureValue instanceof List<?>) {
 				List<EObject> siblingElts = (List<EObject>) featureValue;
 				EObject candidateElt = getSelectableElement(selectedElements, siblingElts, eltToRemove);
 				if (candidateElt != null) {
 					return candidateElt;
 				}
 			}
 		}
 		return nextObjToSelect;
 	}
 
 	private EObject getSelectableElement(List<EObject> eltsToRemoveList, List<EObject> siblingElts, EObject eltToRemove) {
 		int indexEltToRemove = siblingElts.indexOf(eltToRemove);
 		// Try to search the next element in the list
 		for (int i = indexEltToRemove + 1; i < siblingElts.size(); i++) {
 			if (!eltsToRemoveList.contains(siblingElts.get(i))) {
 				return siblingElts.get(i);
 			}
 		}
 		// Or search among the previous elements in the list
 		for (int i = indexEltToRemove - 1; i >= 0; i--) {
 			if (!eltsToRemoveList.contains(siblingElts.get(i))) {
 				return siblingElts.get(i);
 			}
 		}
 		// Or return the parent element
 		if (!eltsToRemoveList.contains(eltToRemove.eContainer())) {
 			return eltToRemove.eContainer();
 		}
 		return null;
 	}
 
 	private EmfMasterDetailBlock masterDetail;
 
 	public RemoveAction(EmfMasterDetailBlock masterDetail) {
 		super();
 		this.masterDetail = masterDetail;
 		getDeleteAction();
 
 	}
 
 	private IAction getDeleteAction() {
 		EditingDomainActionBarContributor actionBarContributor = masterDetail.getEditor().getActionBarContributor();
 		if (actionBarContributor != null && actionBarContributor instanceof EmfActionBarContributor)
 			return ((EmfActionBarContributor) actionBarContributor).getDeleteAction();
 		return null;
 	}
 
 	@Override
 	public ImageDescriptor getImageDescriptor() {
 		return ImageDescriptor.createFromURL(Activator.getDefault().getBundle().getResource(IEmfFormsImages.REMOVE_TOOLBAR_BUTTON));
 	}
 }
