 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.jsf.facesconfig.ui.pageflow;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.gef.requests.CreationFactory;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jst.jsf.facesconfig.ui.util.WebrootUtil;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
 
 /**
  * This is the listener for resource drag and drop operations
  * 
  */
 public abstract class ResourceTransferDropTargetListener extends
 		AbstractTransferDropTargetListener {
 	/** the pageflow editor */
 	private IEditorPart editor = null;
 
 	/**
 	 * Creates a new ResourceTransferDropTargetListener instance.
 	 * 
 	 * @param viewer -
 	 *            target pageflow editor view.
 	 * @param editor 
 	 */
 	public ResourceTransferDropTargetListener(EditPartViewer viewer,
 			IEditorPart editor) {
 		super(viewer, ResourceTransfer.getInstance());
 
 		this.editor = editor;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see AbstractTransferDropTargetListener#createTargetRequest()
 	 */
 	protected Request createTargetRequest() {
 		CreateRequest request = new CreateRequest();
 		if (getCurrentEvent().data == null) {
 			request.setFactory(getFactory(null));
 		} else {
 			IResource[] resources = (IResource[]) getCurrentEvent().data;
 			request.setFactory(getFactory(resources[0]));
 		}
 		return request;
 	}
 
 	/**
 	 * get the request for creating a new resource object
 	 * 
 	 * @return - creation request
 	 */
 	protected final CreateRequest getCreateRequest() {
 		return (CreateRequest) getTargetRequest();
 	}
 
 	/**
 	 * get creation factory for resource object, which should be implemented.
 	 * 
 	 * @param obj -
 	 *            resource object
 	 * @return - creation factory for resource object
 	 */
 	abstract protected CreationFactory getFactory(Object obj);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see AbstractTransferDropTargetListener#handleDragOperationChanged()
 	 */
 	protected void handleDragOperationChanged() {
 		getCurrentEvent().detail = DND.DROP_COPY;
 		super.handleDragOperationChanged();
 	}
 
 	/**
 	 * get the current transfered local selection object.
 	 * 
 	 * @return
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
 	 * @see AbstractTransferDropTargetListener#handleDragOver()
 	 */
 	protected void handleDragOver() {
 		if (!isValidJSFFile(getCurrentLocalObject())) {
 			getCurrentEvent().detail = DND.ERROR_INVALID_DATA;
 		} else {
 			getCurrentEvent().detail = DND.DROP_COPY;
 		}
 		getCurrentEvent().feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;
 		super.handleDragOver();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see AbstractTransferDropTargetListener#handleDrop()
 	 */
 	protected void handleDrop() {
 		getCurrentEvent().detail = DND.DROP_COPY;
 		super.handleDrop();
 		// because the handle drop can change this event to DND.DROP_NONE
 		if (getCurrentEvent().detail == DND.DROP_COPY) {
 			selectAddedObject();
 		}
 	}
 
 	/**
 	 * select the new drag and drop object in the pageflow view.
 	 */
 	private void selectAddedObject() {
 		Object model = getCreateRequest().getNewObject();
 		if (null == model) {
 			return;
 		}
 		EditPartViewer viewer = getViewer();
 		Object editpart = viewer.getEditPartRegistry().get(model);
 		if (editpart instanceof EditPart) {
 			viewer.flush();
 			viewer.select((EditPart) editpart);
 			// Activate the editor window
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 					.getActivePage().activate(editor);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see AbstractTransferDropTargetListener#updateTargetRequest()
 	 */
 	protected void updateTargetRequest() {
 		CreateRequest request = getCreateRequest();
 		request.setLocation(getDropLocation());
 	}
 
 	/**
 	 * Allow drop if any of these types in the drop data, which can be jsp file
 	 * or a sub folder of the webroot folder.
 	 * 
 	 */
 	private boolean isValidJSFFile(Object resource) {
 		if (resource == null || !(resource instanceof IResource)) {
 			return false;
 		}
 
 		if (((IResource) resource).getProject() == ((FileEditorInput) editor
 				.getEditorInput()).getFile().getProject()) {
 			if (WebrootUtil.getWebPath(((IResource) resource).getFullPath())
 					.length() > 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see AbstractTransferDropTargetListener#isEnabled()
 	 */
 	public boolean isEnabled(DropTargetEvent dropTargetEvent) {
 		if (super.isEnabled(dropTargetEvent)) {
 			if (dropTargetEvent.data != null) {
 				IResource[] resources = (IResource[]) dropTargetEvent.data;
 				// The resource should be under the same project of the current
 				// faces-config file
 				if (isValidJSFFile(resources[0])) {
 					return true;
 				}
 				return false;
 			}
 			// If no data, can't verify, so allow the drop
 			return true;
 		}
 		return false;
 	}
 }
