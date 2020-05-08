 /**********************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.server.ui.internal.view.servers;
 
import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerDropAdapter;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.TransferData;
 import org.eclipse.ui.part.ResourceTransfer;
 import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.ui.internal.actions.RunOnServerActionDelegate;
 /**
  *
  */
 public class ServersViewDropAdapter extends ViewerDropAdapter {
 	protected ServersViewDropAdapter(Viewer viewer) {
 		super(viewer);
 	}
 	
 	public void dragEnter(DropTargetEvent event) {
 		if (event.detail == DND.DROP_DEFAULT)
 			event.detail = DND.DROP_COPY;
 
 		super.dragEnter(event);
 	}
 
 	public boolean performDrop(Object data) {
 		Object target = getCurrentTarget();
 		IServer server = null;
 		if (target instanceof IServer)
 			server = (IServer) target;
 		
 		if (server == null)
 			return false;
 		
 		final IServer finalServer = server;
 		RunOnServerActionDelegate ros = new RunOnServerActionDelegate() {
			public IServer getServer(IModule module, String launchMode, IProgressMonitor monitor) {
 				return finalServer;
 			}
 		};
 		Action action = new Action() {
 			//
 		};
		if (data instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) data;
			data = sel.getFirstElement();
		}
 		ros.selectionChanged(action, new StructuredSelection(data));
 		
 		//if (!action.isEnabled())
 		//	return false;
 		
 		ros.run(action);
 		return true;
 	}
 
 	public boolean validateDrop(Object target, int operation, TransferData transferType) {
 		if (target == null)
 			return false;
 		/*IServer server = null;
 		if (target instanceof IServer)
 			server = (IServer) target;*/
 		//if (!ServerUIPlugin.hasModuleArtifact(target))
 		//	return false;
 		
 		System.out.println("Target: " + target + " " + operation + " " + transferType);
 		
 		if (FileTransfer.getInstance().isSupportedType(transferType))
 			return true;
 		if (ResourceTransfer.getInstance().isSupportedType(transferType))
 			return true;
 		if (LocalSelectionTransfer.getInstance().isSupportedType(transferType))
 			return true;
 		
 		return false;
 	}
 	
 	/**
     * Returns the resource selection from the LocalSelectionTransfer.
     * 
     * @return the resource selection from the LocalSelectionTransfer
     */
    /*private IResource[] getSelectedResources() {
        ArrayList selectedResources = new ArrayList();
 
        ISelection selection = LocalSelectionTransfer.getInstance()
                .getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection) selection;
            for (Iterator i = ssel.iterator(); i.hasNext();) {
                Object o = i.next();
                if (o instanceof IResource) {
                    selectedResources.add(o);
                }
                else if (o instanceof IAdaptable) {
                    IAdaptable a = (IAdaptable) o;
                    IResource r = (IResource) a.getAdapter(IResource.class);
                    if (r != null) {
                        selectedResources.add(r);
                    }
                }
            }
        }
        return (IResource[]) selectedResources.toArray(new IResource[selectedResources.size()]);
    }*/
 }
