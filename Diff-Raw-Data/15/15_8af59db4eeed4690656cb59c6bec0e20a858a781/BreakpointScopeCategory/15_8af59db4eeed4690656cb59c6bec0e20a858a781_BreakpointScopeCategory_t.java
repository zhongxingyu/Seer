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
 
 import java.text.MessageFormat;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.cdt.debug.core.model.ICBreakpoint;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.PlatformObject;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.tcf.internal.cdt.ui.Activator;
 import org.eclipse.tcf.internal.cdt.ui.ImageCache;
 import org.eclipse.tcf.internal.debug.model.ITCFConstants;
 import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;
 import org.eclipse.ui.model.IWorkbenchAdapter;
 
 /**
  * Element which represents a breakpoint scope grouping in Breakpoints view.
  * <p>
  * Scope grouping contains both a filter and context IDs string, however
  * only one of them can be active on a breakpoint at one time.
  *
  */
 public class BreakpointScopeCategory extends PlatformObject implements IWorkbenchAdapter {
 
     private static Object[] EMPTY_CHILDREN_ARRAY = new Object[0];
 
    private final String fFilter;
    private final String fContextIds;
 
     public BreakpointScopeCategory(String filter, String contextIds) {
         fFilter = filter;
         fContextIds = contextIds;
     }
 
     public String getFilter() {
         return fFilter;
     }
 
     public String getContextIds() {
         return fContextIds;
     }
 
     /**
      * Sets the given filter and context strings to all breakpoints which match
      * the group's current scope settings.
      */
    void setFilter(final String filter, final String contextIds) {
         final List<ICBreakpoint> bps = findCategoryBreakpoints();
 
         if (bps.isEmpty()) return;  // No breakpoints to update
         try {
             ResourcesPlugin.getWorkspace().run(
                 new IWorkspaceRunnable() {
                     public void run(IProgressMonitor monitor) throws CoreException {
                         for (ICBreakpoint bp : bps) {
                            getScopeExtension(bp).setPropertiesFilter(filter);
                            getScopeExtension(bp).setRawContextIds(contextIds);
                         }
                     }
                 },
                 new NullProgressMonitor()
                 );
         }
         catch (CoreException e) {
             Activator.log(e);
         }
     }
 
     public String getLabel(Object o) {
         if (getFilter() != null && getContextIds() != null) {
             return MessageFormat.format(Messages.BreakpointScopeCategory_filter_and_contexts_label, new Object[] { getFilter(), getContextIds() });
         } else if (getFilter() != null) {
             return MessageFormat.format(Messages.BreakpointScopeCategory_filter_label, new Object[] { getFilter() });
         } else if (getContextIds() != null) {
             return MessageFormat.format(Messages.BreakpointScopeCategory_contexts_label, new Object[] { getContextIds() });
         }
         return Messages.BreakpointScopeCategory_global_label;
     }
 
     public ImageDescriptor getImageDescriptor(Object object) {
         return ImageCache.getImageDescriptor(ImageCache.IMG_BREAKPOINT_SCOPE);
     }
 
     public Object[] getChildren(Object o) {
         // Not used
         return EMPTY_CHILDREN_ARRAY;
     }
 
     public Object getParent(Object o) {
         // Not used
         return null;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof BreakpointScopeCategory) {
             BreakpointScopeCategory other = (BreakpointScopeCategory)obj;
             return ((getFilter() == null && other.getFilter() == null) ||
                     (getFilter() != null && getFilter().equals(other.getFilter()))) &&
                    ((getContextIds() == null && other.getContextIds() == null) ||
                     (getContextIds() != null && getContextIds().equals(other.getContextIds())));
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return (getFilter() != null ? getFilter().hashCode() : 0) +
                 (getContextIds() != null ? getContextIds().hashCode() : 0);
     }
 
     private List<ICBreakpoint> findCategoryBreakpoints() {
         List<ICBreakpoint> categoryBreakpoints = new LinkedList<ICBreakpoint>();
         IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
         for (IBreakpoint bp : breakpoints) {
             IMarker bpMarker = bp.getMarker();
             if (bp instanceof ICBreakpoint && bpMarker != null) {
                 String filter = bpMarker.getAttribute(TCFBreakpointsModel.ATTR_CONTEXT_QUERY, (String)null);
                 String contextIds = bpMarker.getAttribute(TCFBreakpointsModel.ATTR_CONTEXTIDS, (String)null);
                 if( ((getFilter() == null && filter == null) ||
                         (getFilter() != null && getFilter().equals(filter))) &&
                        ((getContextIds() == null && contextIds == null) ||
                         (getContextIds() != null && getContextIds().equals(contextIds))) )
                 {
                     categoryBreakpoints.add((ICBreakpoint)bp);
                 }
             }
         }
         return categoryBreakpoints;
     }
 
     private TCFBreakpointScopeExtension getScopeExtension(ICBreakpoint bp) throws CoreException {
         return (TCFBreakpointScopeExtension) bp.getExtension(
                 ITCFConstants.ID_TCF_DEBUG_MODEL, TCFBreakpointScopeExtension.class);
     }
 
 }
