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
 package org.eclipse.graphiti.ui.internal.editor;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.EditPart;
 import org.eclipse.graphiti.features.IDirectEditingInfo;
 import org.eclipse.graphiti.internal.pref.GFPreferences;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.graphiti.ui.internal.parts.ConnectionDecoratorEditPart;
 import org.eclipse.graphiti.ui.internal.parts.DiagramEditPart;
 import org.eclipse.graphiti.ui.internal.parts.ShapeEditPart;
 import org.eclipse.ui.progress.UIJob;
 
 /**
  * Package-private Refresh Job used by the DiagramListener to update the
  * DiagramEditorInternal.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 class DiagramRefreshJob extends UIJob {
 
 	/**
 	 * The Constant UIJOB_FAMILY_REFRESH.
 	 */
 	private final String UIJOB_FAMILY_REFRESH = "org.eclipse.graphiti.ui.internal.refresh"; //$NON-NLS-1$
 
 	private final Set<EditPart> editParts = new HashSet<EditPart>();
 
 	private boolean refreshAll = false;
 
 	private DiagramEditorInternal ed;
 
 	DiagramRefreshJob(String name, DiagramEditorInternal ed) {
 		super(name);
 		this.ed = ed;
 	}
 
 	void addEditPart(EditPart ep) {
 		if (ep instanceof DiagramEditPart) {
 			setRefreshAll();
 			return;
 		}
 
 		editParts.add(ep);
 	}
 
 	/**
 	 * @return true if and only if job is set to refresh all or
 	 *         editparts-to-refresh list is not empty.
 	 */
 	boolean shouldBeRun() {
 		return isRefreshAll() || !editParts.isEmpty();
 	}
 
 	@Override
 	public boolean belongsTo(Object family) {
 		return UIJOB_FAMILY_REFRESH.equals(family);
 	}
 
 	boolean isRefreshAll() {
 		return refreshAll;
 	}
 
 	@Override
 	public IStatus runInUIThread(IProgressMonitor monitor) {
 		if (GFPreferences.getInstance().isCPUProfilingTraceActive()) {
 			if (T.racer().info()) {
 				T.racer().info("RUN UI  =>  DiagramEditorInternal.RefreshDiagramJob.runInUIThread()"); //$NON-NLS-1$
 			}
 		}
 
 		ed.initRefresh();
 		// prove if switch to auto activate direct editing is required
 		// if yes, call always global editor refresh -> this refresh will activate the direct editing
 		IDirectEditingInfo dei = ed.getConfigurationProvider().getFeatureProvider().getDirectEditingInfo();
 		if (refreshAll || dei.isActive()) {
 			ed.refresh();
 		} else {
 			for (EditPart ep : editParts) {
 				if (!hasNewParent(ep)) {
 					ed.internalRefreshEditPart(ep);
 				}
 			}
 			// refresh all active connection decorators
 			Set<Entry> entrySet = ed.getGraphicalViewer().getEditPartRegistry().entrySet();
 			for (Entry e : entrySet) {
 				Object value = e.getValue();
 				if (value instanceof ConnectionDecoratorEditPart) {
 					EditPart ep = (EditPart) value;
 					ep.refresh();
 				}
 			}
			ed.selectBufferedPictogramElements();
 		}
 
 		refreshAll = false;
 		editParts.clear();
 
 		return Status.OK_STATUS;
 	}
 
 	private boolean hasNewParent(EditPart ep) {
 
 		if (ep == null || !(ep instanceof ShapeEditPart) || (ep instanceof DiagramEditPart) || (ep instanceof ConnectionDecoratorEditPart)) {
 			return false;
 		}
 
 		if (ep.getParent() == null) {
 			return true;
 		}
 		Object parentModel = ep.getParent().getModel();
 		if (parentModel != null && ep.getModel() instanceof EObject) {
 			EObject eo = (EObject) ep.getModel();
 			if (!parentModel.equals(eo.eContainer())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	void setRefreshAll() {
 		this.refreshAll = true;
 	}
 
 }
