 /*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.model;
 
 import java.math.BigInteger;
 import java.net.URI;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.URIUtil;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
 import org.eclipse.debug.ui.IDebugModelPresentation;
 import org.eclipse.debug.ui.IValueDetailListener;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.model.TCFSourceRef;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.ImageCache;
 import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.services.IBreakpoints;
 import org.eclipse.tcf.services.ILineNumbers;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.tcf.util.TCFTask;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorRegistry;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.part.FileEditorInput;
 
 
 public class TCFModelPresentation implements IDebugModelPresentation {
 
     public final static String DISPLAY_FULL_PATHS = "DISPLAY_FULL_PATHS"; //$NON-NLS-1$
 
     private static final String[] attr_names = {
         TCFBreakpointsModel.ATTR_LINE, "line",
         TCFBreakpointsModel.ATTR_ADDRESS, "address",
         TCFBreakpointsModel.ATTR_FUNCTION, "location",
         TCFBreakpointsModel.ATTR_EXPRESSION, "expression",
         TCFBreakpointsModel.ATTR_CONDITION, "condition",
         TCFBreakpointsModel.ATTR_EVENT_TYPE, "event type",
         TCFBreakpointsModel.ATTR_EVENT_ARGS, "event args",
         TCFBreakpointsModel.ATTR_CONTEXTNAMES, "scope (names)",
         TCFBreakpointsModel.ATTR_CONTEXTIDS, "scope (IDs)",
         TCFBreakpointsModel.ATTR_EXE_PATHS, "scope (modules)",
         TCFBreakpointsModel.ATTR_STOP_GROUP, "stop group",
     };
 
     private final Collection<ILabelProviderListener> listeners = new HashSet<ILabelProviderListener>();
     private HashMap<String,Object> attrs = new HashMap<String,Object>();
 
     private static final TCFModelPresentation default_instance = new TCFModelPresentation();
 
     public static TCFModelPresentation getDefault() {
         return default_instance;
     }
 
     public void addListener(ILabelProviderListener listener) {
         listeners.add(listener);
     }
 
     public void removeListener(ILabelProviderListener listener) {
         listeners.remove(listener);
     }
 
     public void dispose() {
     }
 
     public void computeDetail(IValue value, IValueDetailListener listener) {
     }
 
     public Image getImage(Object element) {
         ImageDescriptor descriptor = null;
         if (element instanceof IBreakpoint) {
             final IBreakpoint breakpoint = (IBreakpoint)element;
             descriptor = ImageCache.getImageDescriptor(ImageCache.IMG_BREAKPOINT_DISABLED);
             try {
                 if (breakpoint.isEnabled()) {
                     descriptor = new TCFTask<ImageDescriptor>() {
                         public void run() {
                             boolean installed = false;
                             boolean warning = false;
                             boolean error = false;
                             boolean moved = false;
                             ImageDescriptor d = ImageCache.getImageDescriptor(ImageCache.IMG_BREAKPOINT_ENABLED);
                             Map<TCFLaunch,Map<String,Object>> status = Activator.getAnnotationManager().getBreakpointStatus(breakpoint);
                             for (TCFLaunch launch : status.keySet()) {
                                 Map<String,Object> map = status.get(launch);
                                 if (map == null) continue;
                                 if ((String)map.get(IBreakpoints.STATUS_ERROR) != null) error = true;
                                 Object planted = map.get(IBreakpoints.STATUS_INSTANCES);
                                 if (planted == null) continue;
                                 @SuppressWarnings("unchecked")
                                 Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)planted;
                                 for (Map<String,Object> m : list) {
                                     if (m.get(IBreakpoints.INSTANCE_ERROR) == null) {
                                         installed = true;
                                         if (!moved) {
                                             TCFNodeExecContext ctx = null;
                                             String ctx_id = (String)m.get(IBreakpoints.INSTANCE_CONTEXT);
                                             BigInteger addr = JSON.toBigInteger((Number)m.get(IBreakpoints.INSTANCE_ADDRESS));
                                             int line = breakpoint.getMarker().getAttribute(TCFBreakpointsModel.ATTR_LINE, 0);
                                             if (ctx_id != null && addr != null && line > 0) {
                                                 TCFModel model = TCFModelManager.getModelManager().getModel(launch);
                                                 if (model != null) {
                                                     if (!model.createNode(ctx_id, this)) return;
                                                     TCFDataCache<TCFNodeExecContext> mem = model.searchMemoryContext(model.getNode(ctx_id));
                                                     if (mem != null) {
                                                         if (!mem.validate(this)) return;
                                                         ctx = mem.getData();
                                                     }
                                                 }
                                             }
                                             if (ctx != null) {
                                                 ILineNumbers.CodeArea area = null;
                                                 TCFDataCache<TCFSourceRef> line_cache = ctx.getLineInfo(addr);
                                                 if (line_cache != null) {
                                                     if (!line_cache.validate(this)) return;
                                                     TCFSourceRef line_data = line_cache.getData();
                                                     if (line_data != null && line_data.area != null) area = line_data.area;
                                                 }
                                                 if (area != null && area.start_line != line) moved = true;
                                             }
                                         }
                                     }
                                     else {
                                         warning = true;
                                     }
                                 }
                             }
                             if (moved) d = ImageCache.addOverlay(d, ImageCache.IMG_BREAKPOINT_MOVED, 0, 0);
                             else if (installed) d = ImageCache.addOverlay(d, ImageCache.IMG_BREAKPOINT_INSTALLED, 0, 8);
                             if (warning) d = ImageCache.addOverlay(d, ImageCache.IMG_BREAKPOINT_WARNING, 9, 8);
                             if (error) d = ImageCache.addOverlay(d, ImageCache.IMG_BREAKPOINT_ERROR, 9, 8);
                             done(d);
                         }
                     }.getE();
                 }
                 String cond = breakpoint.getMarker().getAttribute(TCFBreakpointsModel.ATTR_CONDITION, null);
                 if (cond != null && cond.length() > 0) {
                     descriptor = ImageCache.addOverlay(descriptor, ImageCache.IMG_BREAKPOINT_CONDITIONAL);
                 }
             }
             catch (Throwable x) {
             }
         }
         if (descriptor != null) return ImageCache.getImage(descriptor);
         return null;
     }
 
     public String getText(Object element) {
         String text = null;
         if (element instanceof IBreakpoint) {
             final IBreakpoint breakpoint = (IBreakpoint)element;
             IMarker marker = breakpoint.getMarker();
             if (marker == null) return null;
             StringBuffer bf = new StringBuffer();
             try {
                 Map<String,Object> m = marker.getAttributes();
                 String file = marker.getAttribute(TCFBreakpointsModel.ATTR_FILE, null);
                 if (file != null && file.length() > 0) {
                     IPath path = new Path(file);
                     if (path.isValidPath(file)) {
                         bf.append(isShowQualifiedNames() ? path.toOSString() : path.lastSegment());
                         bf.append(' ');
                     }
                 }
                 for (int i = 0; i < attr_names.length; i += 2) {
                     Object obj = m.get(attr_names[i]);
                     if (obj == null) continue;
                     String s = obj.toString();
                     if (s.length() == 0) continue;
                     bf.append('[');
                     bf.append(attr_names[i + 1]);
                     bf.append(": ");
                     bf.append(s);
                     bf.append(']');
                 }
                 if (bf.length() == 0) {
                     String id = marker.getAttribute(TCFBreakpointsModel.ATTR_ID, null);
                     if (id == null) id = Long.toString(marker.getId());
                     bf.append(id);
                 }
             }
             catch (Throwable x) {
                 return x.toString();
             }
             text = bf.toString();
             String status = new TCFTask<String>() {
                 public void run() {
                     done(Activator.getAnnotationManager().getBreakpointStatusText(breakpoint));
                 }
             }.getE();
             if (status != null) text += " (" + status + ")";
         }
         return text;
     }
 
     protected boolean isShowQualifiedNames() {
         Boolean show_qualified = (Boolean)attrs.get( DISPLAY_FULL_PATHS );
         if (show_qualified == null) return false;
         return show_qualified.booleanValue();
     }
 
     public void setAttribute(String attribute, Object value) {
         if (value == null) attrs.remove(attribute);
         else attrs.put(attribute, value);
     }
 
     public boolean isLabelProperty(Object element, String property) {
         return true;
     }
 
     public String getEditorId(IEditorInput input, Object element) {
         String id = null;
         if (input != null) {
             IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
             IEditorDescriptor descriptor = registry.getDefaultEditor(input.getName());
             if (descriptor != null) id = descriptor.getId();
         }
         return id;
     }
 
     public IEditorInput getEditorInput(Object element) {
         if (element instanceof ILineBreakpoint) {
             element = ((ILineBreakpoint)element).getMarker();
         }
         if (element instanceof IMarker) {
             element = ((IMarker)element).getResource();
         }
         if (element instanceof IFile) {
             return new FileEditorInput((IFile)element);
         }
         if (element instanceof IStorage) {
            IPath fullPath;
            if (element instanceof LocalFileStorage) {
                // Bug 390572: Don't use LocalFileStorage.getFullPath() as it resolves symlinks
                fullPath = new Path(((LocalFileStorage)element).getFile().getAbsolutePath());
            }
            else {
                fullPath = ((IStorage)element).getFullPath();
            }
             URI uri = URIUtil.toURI(fullPath);
             if (uri != null) {
                 try {
                     return new FileStoreEditorInput(EFS.getStore(uri));
                 }
                 catch (CoreException e) {
                     Activator.log(e);
                 }
             }
         }
         return null;
     }
 }
