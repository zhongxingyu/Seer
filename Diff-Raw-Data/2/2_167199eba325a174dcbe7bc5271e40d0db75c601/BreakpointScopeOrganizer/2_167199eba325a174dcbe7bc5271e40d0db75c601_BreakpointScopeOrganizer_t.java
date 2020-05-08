 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.cdt.ui.breakpoints;
 
 import org.eclipse.cdt.debug.core.model.ICBreakpoint;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IAdapterFactory;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IBreakpointsListener;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointContainer;
 import org.eclipse.debug.ui.AbstractBreakpointOrganizerDelegate;
 import org.eclipse.debug.ui.IBreakpointOrganizerDelegate;
 import org.eclipse.tcf.internal.cdt.ui.Activator;
 import org.eclipse.tcf.internal.debug.model.ITCFConstants;
 import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;
 
 /**
  * Breakpoint organizer which groups breakpoints according to their
  * breakpoint scope attributes.
  *
  * @see IBreakpointOrganizerDelegate
  */
 @SuppressWarnings("restriction")
 public class BreakpointScopeOrganizer extends AbstractBreakpointOrganizerDelegate implements IBreakpointsListener {
 
     private static IAdaptable[] DEFAULT_CATEGORY_ARRAY = new IAdaptable[] { new BreakpointScopeCategory(null, null) };
 
     static
     {
         Platform.getAdapterManager().registerAdapters(new BreakpointScopeContainerAdapterFactory(), IBreakpointContainer.class);
     }
 
     public BreakpointScopeOrganizer() {
         DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
     }
 
     public IAdaptable[] getCategories(IBreakpoint breakpoint) {
         IMarker marker = breakpoint.getMarker();
         if (marker != null) {
             String filter = marker.getAttribute(TCFBreakpointsModel.ATTR_CONTEXT_QUERY, null);
             String contextIds = marker.getAttribute(TCFBreakpointsModel.ATTR_CONTEXTIDS, null);
             return new IAdaptable[] { new BreakpointScopeCategory(filter, contextIds) };
         }
         return DEFAULT_CATEGORY_ARRAY;
     }
 
     @Override
     public void dispose() {
         DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
         super.dispose();
     }
 
     public void breakpointsAdded(IBreakpoint[] breakpoints) {
     }
 
     public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
         // Using delta's to see which attributes have changed is not reliable.
         // Therefore we need to force a full refresh of scope categories whenever
         // we get a breakpoints changed notiifcation.
         fireCategoryChanged(null);
     }
 
     public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
     }
 
     @Override
     public void addBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
         if (category instanceof BreakpointScopeCategory && breakpoint instanceof ICBreakpoint) {
             String filter = ((BreakpointScopeCategory)category).getFilter();
             String contextIds = ((BreakpointScopeCategory)category).getContextIds();
             ICBreakpoint cBreakpoint = (ICBreakpoint) breakpoint;
             TCFBreakpointScopeExtension scopeExtension;
             try {
                scopeExtension = (TCFBreakpointScopeExtension)cBreakpoint.getExtension(
                         ITCFConstants.ID_TCF_DEBUG_MODEL, TCFBreakpointScopeExtension.class);
                 if (scopeExtension != null) {
                     scopeExtension.setPropertiesFilter(filter);
                     scopeExtension.setRawContextIds(contextIds);
                 }
             }
             catch (CoreException e) {
                 Activator.log(e);
             }
         }
     }
 
     @Override
     public boolean canAdd(IBreakpoint breakpoint, IAdaptable category) {
         return category instanceof BreakpointScopeCategory && breakpoint instanceof ICBreakpoint;
     }
 
     @Override
     public boolean canRemove(IBreakpoint breakpoint, IAdaptable category) {
         return breakpoint instanceof ICBreakpoint;
     }
 
     @Override
     public void removeBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
         // Nothing to do, changes handled by add.
     }
 }
 
 /**
  * Adapter factory which returns the breakpoint category for a given breakpoint
  * container element that is shown in Breakpoints view.
  */
 @SuppressWarnings("restriction")
 class BreakpointScopeContainerAdapterFactory implements IAdapterFactory {
 
     private static final Class<?>[] fgAdapterList = new Class[] {
         BreakpointScopeCategory.class
     };
 
     public Object getAdapter(Object obj, @SuppressWarnings("rawtypes") Class adapterType) {
         if ( !(obj instanceof IBreakpointContainer) ) return null;
 
 
         if ( BreakpointScopeCategory.class.equals(adapterType) ) {
             IAdaptable category = ((IBreakpointContainer)obj).getCategory();
             if (category instanceof BreakpointScopeCategory) {
                 return category;
             }
         }
         return null;
     }
 
     @SuppressWarnings("rawtypes")
     public Class[] getAdapterList() {
         return fgAdapterList;
     }
 }
