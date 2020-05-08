 /**
  * Optimus, framework for Model Transformation
  *
  * Copyright (C) 2013 Worldline or third-party contributors as
  * indicated by the @author tags or express copyright attribution
  * statements applied by the authors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.optimus.m2m.engine.ui.prefs;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import net.atos.optimus.m2m.engine.core.masks.PreferencesTransformationMask;
 import net.atos.optimus.m2m.engine.core.requirements.AbstractRequirement;
 import net.atos.optimus.m2m.engine.core.transformations.ExtensionPointTransformationDataSource;
 import net.atos.optimus.m2m.engine.core.transformations.TransformationReference;
 
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 
 /**
  * Check State Listener, used to detect user actions of check boxes from the
  * {@link TransformationsPreferencesPage}
  * 
  * @author mvanbesien
  * 
  */
 public class TransformationsTreeCheckListener implements ICheckStateListener {
 
 	private CheckboxTreeViewer treeViewer;
 
 	private Map<TransformationReference, Boolean> userActions = new HashMap<TransformationReference, Boolean>();
 
 	public TransformationsTreeCheckListener(CheckboxTreeViewer treeViewer) {
 		this.treeViewer = treeViewer;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse
 	 * .jface.viewers.CheckStateChangedEvent)
 	 */
 	public void checkStateChanged(CheckStateChangedEvent event) {
 		if (event.getElement() instanceof TransformationReference) {
 			this.userActions.put(((TransformationReference) event.getElement()), event.getChecked());
 			if (!event.getChecked()) {
 				Set<TransformationReference> uncheckedReferences = new HashSet<TransformationReference>();
 				this.checkForRequirementsToUncheck((TransformationReference) event.getElement(), uncheckedReferences);
 			}
 		}
 	}
 
 	/**
 	 * This method should only be called when an element in unchecked from the
 	 * preferences view.
 	 * 
 	 * This method will look deeply into the referenced transformations to know
 	 * whether the current reference is required by another transformation. If
 	 * this is the case, the transformation requiring this one will be unchecked
 	 * too.
 	 * 
 	 * @param reference
 	 * @param uncheckedReferences
 	 */
 	private void checkForRequirementsToUncheck(TransformationReference reference,
 			Set<TransformationReference> uncheckedReferences) {
 		String referenceId = reference.getId();
 		for (TransformationReference otherRef : ExtensionPointTransformationDataSource.instance().getAll()) {
 			for (AbstractRequirement otherReq : otherRef.getRequirements()) {
 				String otherReqId = otherReq.getId();
 				if (otherReqId.equals(referenceId) && !uncheckedReferences.contains(otherRef)) {
 					this.userActions.put(otherRef, false);
 					this.treeViewer.refresh(otherRef);
 					uncheckedReferences.add(otherRef);
 					checkForRequirementsToUncheck(otherRef, uncheckedReferences);
 				}
 			}
 		}
 	}
 
 	public void apply() {
 		for (TransformationReference key : this.userActions.keySet()) {
			PreferencesTransformationMask.getInstance()
 					.setTransformationEnabled(key.getId(), this.userActions.get(key));
 		}
 
 	}
 }
