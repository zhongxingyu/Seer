 /*******************************************************************************
  * Copyright (c) 2009, 2012 Wind River Systems and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.debug.test;
 
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputRequestor;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.ViewerInputService;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
 import org.eclipse.debug.ui.contexts.DebugContextEvent;
 import org.eclipse.debug.ui.contexts.IDebugContextListener;
 import org.eclipse.debug.ui.contexts.IDebugContextProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * 
  */
 @SuppressWarnings("restriction")
 public class VariablesVirtualTreeModelViewer extends VirtualTreeModelViewer implements IDebugContextListener {
 
     private IDebugContextProvider fDebugContextProvider;
     private ViewerInputService fInputService;
     private boolean fActive = false;
 
     public VariablesVirtualTreeModelViewer(String contextId, IDebugContextProvider debugContextProvider) {
         this(new PresentationContext(contextId), debugContextProvider);
     }
 
     public VariablesVirtualTreeModelViewer(IPresentationContext context, IDebugContextProvider debugContextProvider) {
         super(Display.getDefault(), SWT.NONE, context);
         fInputService = new ViewerInputService(this, new IViewerInputRequestor() {
             
             public void viewerInputComplete(IViewerInputUpdate update) {
                 if (!update.isCanceled()) {
                     setInput(update.getInputElement());
                 }
             }
         });
         fDebugContextProvider = debugContextProvider;
         debugContextProvider.addDebugContextListener(this);
     }
 
     public void setActive(boolean active) {
         if (fActive == active) {
             return;
         }
         fActive = active;
         if (fActive) {
             setActiveContext(fDebugContextProvider.getActiveContext());
         } else {
             fInputService.resolveViewerInput(ViewerInputService.NULL_INPUT);
         }
     }
     
     @Override
     public void dispose() {
         fDebugContextProvider.removeDebugContextListener(this);
         fInputService.dispose();
         super.dispose();
     }
     
     public void debugContextChanged(DebugContextEvent event) {
        if (fActive && (event.getFlags() & DebugContextEvent.ACTIVATED) != 0) {
             setActiveContext(event.getContext());
         }
     }    
     
     private void setActiveContext(ISelection selection) {
         if (selection instanceof IStructuredSelection) {
             fInputService.resolveViewerInput(((IStructuredSelection)selection).getFirstElement());
         }
     }
 }
