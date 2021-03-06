 /*******************************************************************************
  * Copyright (c) 2004 - 2005 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylar.ide;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.mylar.core.IMylarElement;
 import org.eclipse.mylar.core.IMylarStructureBridge;
 import org.eclipse.mylar.core.InteractionEvent;
 import org.eclipse.mylar.core.MylarPlugin;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * TODO: refactor into bridges?
  * 
  * @author Mik Kersten
  */
 public class ResourceInterestUpdater {
 
 	public static final String SOURCE_ID = "org.eclipse.mylar.ide.resource.interest.updater";
 
 	private boolean syncExec = false;
 	
 	public void addResourceToContext(final IResource resource) {
 		if (syncExec) {
 			internalAddResourceToContext(resource);
 		} else {
 			final IWorkbench workbench = PlatformUI.getWorkbench();
 			workbench.getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					internalAddResourceToContext(resource);
 				}
 			});
 		}
 	}
 
 	private void internalAddResourceToContext(IResource resource) {
 		IMylarStructureBridge bridge = MylarPlugin.getDefault().getStructureBridge(resource);
 		String handle = bridge.getHandleIdentifier(resource);
 
 		if (handle != null) {
 			IMylarElement element = MylarPlugin.getContextManager().getElement(handle);
 			if (!element.getInterest().isInteresting()) {
//				MylarPlugin.log("adding to context: " + resource, this);
 				InteractionEvent interactionEvent = new InteractionEvent(
 						InteractionEvent.Kind.SELECTION,
 						bridge.getContentType(), 
 						handle, 
 						SOURCE_ID);
 				MylarPlugin.getContextManager().handleInteractionEvent(interactionEvent, true);
 			}
 		}
 	}
 	
 	/**
 	 * For testing.
 	 */
 	public void setSyncExec(boolean syncExec) {
 		this.syncExec = syncExec;
 	}
 }
