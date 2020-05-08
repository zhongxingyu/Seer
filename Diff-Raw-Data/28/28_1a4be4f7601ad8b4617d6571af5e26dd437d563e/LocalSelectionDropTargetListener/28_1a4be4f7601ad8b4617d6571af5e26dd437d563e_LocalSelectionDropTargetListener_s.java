 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.dnd.internal;
 
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jst.jsf.common.ui.internal.guiutils.Alerts;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.dnd.LocalDropRequest;
 import org.eclipse.jst.pagedesigner.viewer.IHTMLGraphicalViewer;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
 
 /**
  * @author mengbo
  */
 public class LocalSelectionDropTargetListener extends
 		AbstractTransferDropTargetListener {
 	/**
 	 * @param viewer
 	 */
 	public LocalSelectionDropTargetListener(EditPartViewer viewer) {
		super(viewer, LocalSelectionTransfer.getInstance());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.dnd.AbstractTransferDropTargetListener#createTargetRequest()
 	 */
 	protected Request createTargetRequest() {
 		LocalDropRequest request = new LocalDropRequest();
 		request.setLocation(getDropLocation());
 		request.setLocalObject(getCurrentLocalObject());
 		return request;
 	}
 
 	/**
 	 * @return the current local object
 	 */
 	private Object getCurrentLocalObject() {
		ISelection sel = LocalSelectionTransfer.getInstance().getSelection();
 		if (sel instanceof IStructuredSelection) {
 			return ((IStructuredSelection) sel).getFirstElement();
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.dnd.AbstractTransferDropTargetListener#updateTargetRequest()
 	 */
 	protected void updateTargetRequest() {
 		LocalDropRequest dropRequest = (LocalDropRequest) getTargetRequest();
 		dropRequest.setLocation(getDropLocation());
 		dropRequest.setLocalObject(getCurrentLocalObject());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.dnd.AbstractTransferDropTargetListener#setCurrentEvent(org.eclipse.swt.dnd.DropTargetEvent)
 	 */
 	public void setCurrentEvent(DropTargetEvent currentEvent) {
 		super.setCurrentEvent(currentEvent);
 		if (currentEvent != null) {
 			if (currentEvent.detail != DND.DROP_NONE) {
 				currentEvent.detail = DND.DROP_COPY;
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
 	 */
 	public void dragOperationChanged(DropTargetEvent event) {
 		// switch the insert or update
 		if ((event.detail & (DND.DROP_COPY)) != 0) {
 			LocalDropEditPolicy.setCheckUpdate(false);
 		} else {
 			LocalDropEditPolicy.setCheckUpdate(true);
 		}
 		super.dragOperationChanged(event);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
 	 */
 	public void drop(DropTargetEvent event) {
 		String path = ((IHTMLGraphicalViewer) getViewer()).getModel()
 				.getBaseLocation();
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IWorkspaceRoot root = workspace.getRoot();
 		IPath filePath = new Path(path);
 		if (root.getFile(filePath).exists()) {
 			getViewer().getControl().setFocus();
 			super.drop(event);
 			LocalDropEditPolicy.setCheckUpdate(true);
 		} else {
 			Alerts alert = PDPlugin.getAlerts();
 			alert.info("LocalSelectionDropTargetListener.MessageDialog.Title", //$NON-NLS-1$
 					"LocalSelectionDropTargetListener.MessageDialog.Message"); //$NON-NLS-1$
 		}
 	}
 }
