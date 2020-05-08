 /*******************************************************************************
  * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
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
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationListener;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.debug.ui.ISourcePresentation;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.debug.ui.ITCFAnnotationProvider;
 import org.eclipse.tcf.internal.debug.launch.TCFSourceLookupDirector;
 import org.eclipse.tcf.internal.debug.model.ITCFBreakpointListener;
 import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;
 import org.eclipse.tcf.internal.debug.model.TCFBreakpointsStatus;
 import org.eclipse.tcf.internal.debug.model.TCFContextState;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.model.TCFSourceRef;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.ImageCache;
 import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IBreakpoints;
 import org.eclipse.tcf.services.ILineNumbers;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.IStackTrace;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWindowListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.ui.texteditor.MarkerAnnotation;
 
 public class TCFAnnotationManager {
 
     private static final String
         TYPE_BP_INSTANCE = "org.eclipse.tcf.debug.breakpoint_instance",
         TYPE_TOP_FRAME = "org.eclipse.tcf.debug.top_frame",
         TYPE_STACK_FRAME = "org.eclipse.tcf.debug.stack_frame";
 
     static class TCFAnnotation extends Annotation {
 
         final String ctx;
         final String bp_id;
         final BigInteger addr;
         final ILineNumbers.CodeArea area;
         final Image image;
         final String text;
         final String type;
         final int hash_code;
 
         final ArrayList<IAnnotationModel> models = new ArrayList<IAnnotationModel>();
 
         IBreakpoint breakpoint;
 
         TCFAnnotation(String ctx, String bp_id, BigInteger addr,
                 ILineNumbers.CodeArea area, Image image, String text, String type) {
             super(type, false, text);
             this.ctx = ctx;
             this.bp_id = bp_id;
             this.addr = addr;
             this.area = area;
             this.image = image;
             this.text = text;
             this.type = type;
             hash_code = image.hashCode() + text.hashCode() + type.hashCode();
         }
 
         protected Image getImage() {
             return image;
         }
 
         void dispose() {
             for (IAnnotationModel model : models) {
                 model.removeAnnotation(this);
             }
         }
 
         @Override
         public boolean equals(Object o) {
             if (!(o instanceof TCFAnnotation)) return false;
             TCFAnnotation a = (TCFAnnotation)o;
             if (!ctx.equals(a.ctx)) return false;
             if (bp_id != a.bp_id) {
                 if (bp_id == null) return false;
                 if (!bp_id.equals(a.bp_id)) return false;
             }
             if (addr != a.addr) {
                 if (addr == null) return false;
                 if (!addr.equals(a.addr)) return false;
             }
             if (area != a.area) {
                 if (area == null) return false;
                 if (!area.equals(a.area)) return false;
             }
             if (!image.equals(a.image)) return false;
             if (!text.equals(a.text)) return false;
             if (!type.equals(a.type)) return false;
             return true;
         }
 
         @Override
         public int hashCode() {
             return hash_code;
         }
 
         @Override
         public String toString() {
             StringBuffer bf = new StringBuffer();
             bf.append('[');
             bf.append(addr != null ? addr.toString(16) : "null");
             bf.append(',');
             bf.append(area);
             bf.append(',');
             bf.append(text);
             bf.append(',');
             bf.append(type);
             bf.append(']');
             return bf.toString();
         }
     }
 
     private class WorkbenchWindowInfo {
         final HashSet<TCFAnnotation> annotations = new HashSet<TCFAnnotation>();
         final Map<IEditorInput,IEditorPart> editors = new HashMap<IEditorInput,IEditorPart>();
         final Map<IViewPart,ITCFDisassemblyPart> views = new HashMap<IViewPart,ITCFDisassemblyPart>();
 
         ITCFAnnotationProvider provider;
         UpdateTask update_task;
         TCFNode update_node;
 
         void dispose() {
             for (TCFAnnotation a : annotations) a.dispose();
             annotations.clear();
         }
     }
 
     private static abstract class UpdateTask implements Runnable {
         boolean done;
     }
 
     private final HashMap<IWorkbenchWindow,WorkbenchWindowInfo> windows =
         new HashMap<IWorkbenchWindow,WorkbenchWindowInfo>();
 
     private final HashSet<IWorkbenchWindow> dirty_windows = new HashSet<IWorkbenchWindow>();
     private final HashSet<TCFLaunch> dirty_launches = new HashSet<TCFLaunch>();
     private final HashSet<TCFLaunch> changed_launch_cfgs = new HashSet<TCFLaunch>();
 
     private final TCFLaunch.LaunchListener launch_listener = new TCFLaunch.LaunchListener() {
 
         @Override
         public void onCreated(TCFLaunch launch) {
         }
 
         @Override
         public void onConnected(final TCFLaunch launch) {
             updateAnnotations(null, launch);
             TCFBreakpointsStatus bps = launch.getBreakpointsStatus();
             if (bps == null) return;
             bps.addListener(new ITCFBreakpointListener() {
 
                 public void breakpointStatusChanged(String id) {
                     updateAnnotations(null, launch);
                 }
 
                 public void breakpointRemoved(String id) {
                     updateAnnotations(null, launch);
                 }
 
                 public void breakpointChanged(String id) {
                 }
             });
         }
 
         @Override
         public void onDisconnected(final TCFLaunch launch) {
             assert Protocol.isDispatchThread();
             updateAnnotations(null, launch);
         }
 
         @Override
         public void onProcessOutput(TCFLaunch launch, String process_id, int stream_id, byte[] data) {
         }
 
         @Override
         public void onProcessStreamError(TCFLaunch launch, String process_id,
                 int stream_id, Exception error, int lost_size) {
         }
     };
 
     private final ISelectionListener selection_listener = new ISelectionListener() {
 
         @Override
         public void selectionChanged(IWorkbenchPart part, ISelection selection) {
             updateAnnotations(part.getSite().getWorkbenchWindow(), (TCFLaunch)null);
             if (selection instanceof IStructuredSelection) {
                 final Object obj = ((IStructuredSelection)selection).getFirstElement();
                 if (obj instanceof TCFNodeStackFrame && ((TCFNodeStackFrame)obj).isTraceLimit()) {
                     Protocol.invokeLater(new Runnable() {
                         public void run() {
                             ((TCFNodeStackFrame)obj).riseTraceLimit();
                         }
                     });
                 }
             }
         }
     };
 
     private final IWindowListener window_listener = new IWindowListener() {
 
         @Override
         public void windowActivated(IWorkbenchWindow window) {
         }
 
         @Override
         public void windowClosed(IWorkbenchWindow window) {
            assert windows.get(window) != null;
             window.getSelectionService().removeSelectionListener(
                     IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
             windows.remove(window).dispose();
         }
 
         @Override
         public void windowDeactivated(IWorkbenchWindow window) {
         }
 
         @Override
         public void windowOpened(IWorkbenchWindow window) {
             if (windows.get(window) != null) return;
             window.getSelectionService().addSelectionListener(
                     IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
             windows.put(window, new WorkbenchWindowInfo());
             window.getActivePage().addPartListener(part_listener);
             updateAnnotations(window, (TCFLaunch)null);
         }
     };
 
     private final IPartListener part_listener = new IPartListener() {
 
         @Override
         public void partActivated(IWorkbenchPart part) {
         }
 
         @Override
         public void partBroughtToTop(IWorkbenchPart part) {
         }
 
         @Override
         public void partClosed(IWorkbenchPart part) {
         }
 
         @Override
         public void partDeactivated(IWorkbenchPart part) {
         }
 
         @Override
         public void partOpened(IWorkbenchPart part) {
             updateAnnotations(part.getSite().getPage().getWorkbenchWindow(), (TCFLaunch)null);
         }
     };
 
     private final ILaunchConfigurationListener launch_conf_listener = new ILaunchConfigurationListener() {
 
         @Override
         public void launchConfigurationAdded(ILaunchConfiguration cfg) {
         }
 
         @Override
         public void launchConfigurationChanged(final ILaunchConfiguration cfg) {
             displayExec(new Runnable() {
                 public void run() {
                     ILaunch[] arr = launch_manager.getLaunches();
                     for (ILaunch l : arr) {
                         if (l instanceof TCFLaunch) {
                             TCFLaunch t = (TCFLaunch)l;
                             if (cfg.equals(t.getLaunchConfiguration())) {
                                 changed_launch_cfgs.add(t);
                                 updateAnnotations(null, t);
                             }
                         }
                     }
                 }
             });
         }
 
         @Override
         public void launchConfigurationRemoved(ILaunchConfiguration cfg) {
         }
     };
 
     private final Display display = Display.getDefault();
     private final ILaunchManager launch_manager = DebugPlugin.getDefault().getLaunchManager();
     private int update_unnotations_cnt = 0;
     private boolean started;
     private boolean disposed;
 
     public TCFAnnotationManager() {
         assert Protocol.isDispatchThread();
         TCFLaunch.addListener(launch_listener);
         launch_manager.addLaunchConfigurationListener(launch_conf_listener);
         displayExec(new Runnable() {
             public void run() {
                 if (!PlatformUI.isWorkbenchRunning() || PlatformUI.getWorkbench().isStarting()) {
                     display.timerExec(200, this);
                 }
                 else if (!PlatformUI.getWorkbench().isClosing()) {
                     started = true;
                     PlatformUI.getWorkbench().addWindowListener(window_listener);
                     for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                         window_listener.windowOpened(window);
                     }
                     IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                     if (w != null) window_listener.windowActivated(w);
                 }
             }
         });
     }
 
     public void dispose() {
         if (disposed) return;
         assert Protocol.isDispatchThread();
         disposed = true;
         launch_manager.removeLaunchConfigurationListener(launch_conf_listener);
         TCFLaunch.removeListener(launch_listener);
         displayExec(new Runnable() {
             public void run() {
                 if (!started) return;
                 PlatformUI.getWorkbench().removeWindowListener(window_listener);
                 for (IWorkbenchWindow window : windows.keySet()) {
                     window.getSelectionService().removeSelectionListener(
                             IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
                     windows.get(window).dispose();
                 }
                 windows.clear();
             }
         });
     }
 
     private void displayExec(Runnable r) {
         synchronized (Device.class) {
             if (!display.isDisposed()) {
                 display.asyncExec(r);
             }
         }
     }
 
     /**
      * Return breakpoint status info for all active TCF debug sessions.
      * @param breakpoint
      * @return breakpoint status as defined by TCF Breakpoints service.
      */
     Map<TCFLaunch,Map<String,Object>> getBreakpointStatus(IBreakpoint breakpoint) {
         assert Protocol.isDispatchThread();
         Map<TCFLaunch,Map<String,Object>> map = new HashMap<TCFLaunch,Map<String,Object>>();
         if (disposed) return null;
         ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
         for (ILaunch l : launches) {
             if (l instanceof TCFLaunch) {
                 TCFLaunch launch = (TCFLaunch)l;
                 TCFBreakpointsStatus bs = launch.getBreakpointsStatus();
                 if (bs != null) map.put(launch, bs.getStatus(breakpoint));
             }
         }
         return map;
     }
 
     /**
      * Return breakpoint status text for all active TCF debug sessions.
      * @param breakpoint
      * @return breakpoint status as a string.
      */
     @SuppressWarnings("unchecked")
     String getBreakpointStatusText(IBreakpoint breakpoint) {
         assert Protocol.isDispatchThread();
         String error = null;
         for (Map<String,Object> map : getBreakpointStatus(breakpoint).values()) {
             if (map != null) {
                 String s = (String)map.get(IBreakpoints.STATUS_ERROR);
                 if (s != null && error == null) error = s;
                 Object planted = map.get(IBreakpoints.STATUS_INSTANCES);
                 if (planted != null) {
                     Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)planted;
                     for (Map<String,Object> m : list) {
                         if (m.get(IBreakpoints.INSTANCE_ERROR) == null) {
                             return "Planted";
                         }
                     }
                 }
             }
         }
         return error;
     }
 
     @SuppressWarnings("unchecked")
     private Object[] toObjectArray(Object o) {
         if (o == null) return null;
         Collection<Object> c = (Collection<Object>)o;
         return (Object[])c.toArray(new Object[c.size()]);
     }
 
     @SuppressWarnings("unchecked")
     private Map<String,Object> toObjectMap(Object o) {
         return (Map<String,Object>)o;
     }
 
     private ILineNumbers.CodeArea getBreakpointCodeArea(TCFLaunch launch, String id) {
         Map<String,Object> props = launch.getBreakpointsStatus().getProperties(id);
         if (props != null) {
             String file = (String)props.get(IBreakpoints.PROP_FILE);
             Number line = (Number)props.get(IBreakpoints.PROP_LINE);
             if (file != null && line != null) {
                 return new ILineNumbers.CodeArea(null, file,
                         line.intValue(), 0, line.intValue() + 1, 0,
                         null, null, 0, false, false, false, false);
             }
         }
         return null;
     }
 
     private void addBreakpointErrorAnnotation(Set<TCFAnnotation> set, TCFLaunch launch, String ctx, String id, String error) {
         ILineNumbers.CodeArea area = getBreakpointCodeArea(launch, id);
         if (area != null) {
             TCFAnnotation a = new TCFAnnotation(ctx, id, null, area,
                     ImageCache.getImage(ImageCache.IMG_BREAKPOINT_ERROR),
                     "Cannot plant breakpoint: " + error,
                     TYPE_BP_INSTANCE);
             set.add(a);
         }
     }
 
     private boolean isLineAdjusted(ILineNumbers.CodeArea x, ILineNumbers.CodeArea y) {
         if (x == null || y == null) return false;
         if (x.start_line == 0 || y.start_line == 0) return false;
         if (x.start_line != y.start_line) return true;
         return false;
     }
 
     private boolean hidePlantingAnnotation(IAnnotationModel model, IBreakpoint bp, Position p) {
         // Check if a breakpoint annotation does not need to be shown
         // since it has same position as the breakpoint marker.
         @SuppressWarnings("rawtypes")
         Iterator i = model.getAnnotationIterator();
         while (i.hasNext()) {
             Annotation a = (Annotation)i.next();
             if (a instanceof MarkerAnnotation) {
                 Position q = model.getPosition(a);
                 if (q != null && p.getOffset() == q.getOffset()) {
                     if (bp.getMarker().equals(((MarkerAnnotation)a).getMarker())) return true;
                 }
             }
         }
         return false;
     }
 
     private void updateAnnotations(IWorkbenchWindow window, TCFNode node, Set<TCFAnnotation> set) {
         if (disposed) return;
         assert Thread.currentThread() == display.getThread();
         WorkbenchWindowInfo win_info = windows.get(window);
         if (win_info == null) return;
         Map<IEditorInput,IEditorPart> editors = new HashMap<IEditorInput,IEditorPart>();
         for (IEditorReference ref : window.getActivePage().getEditorReferences()) {
             IEditorPart editor = ref.getEditor(false);
             if (editor == null) continue;
             editors.put(editor.getEditorInput(), editor);
         }
         Map<IViewPart,ITCFDisassemblyPart> views = new HashMap<IViewPart,ITCFDisassemblyPart>();
         for (IViewReference ref : window.getActivePage().getViewReferences()) {
             IViewPart view = ref.getView(false);
             if (view == null) continue;
             ITCFDisassemblyPart disasm = (ITCFDisassemblyPart)view.getAdapter(ITCFDisassemblyPart.class);
             if (disasm != null) views.put(view, disasm);
         }
         boolean flush_all =
                 node == null ||
                 !views.keySet().equals(win_info.views.keySet()) ||
                 !editors.equals(win_info.editors) ||
                 changed_launch_cfgs.contains(node.launch);
         win_info.views.clear();
         win_info.views.putAll(views);
         win_info.editors.clear();
         win_info.editors.putAll(editors);
         Iterator<TCFAnnotation> i = win_info.annotations.iterator();
         while (i.hasNext()) {
             TCFAnnotation a = i.next();
             if (!flush_all && set != null && set.remove(a)) continue;
             a.dispose();
             i.remove();
         }
         if (set != null) win_info.annotations.addAll(set);
         ISourcePresentation presentation = TCFModelPresentation.getDefault();
         // Disassembly views
         for (TCFAnnotation a : win_info.annotations) {
             if (a.addr == null) continue;
             for (ITCFDisassemblyPart disasm : views.values()) {
                 IAnnotationModel ann_model = disasm.getAnnotationModel();
                 if (ann_model == null) continue;
                 if (a.models.contains(ann_model)) {
                     ann_model.removeAnnotation(a);
                     a.models.remove(ann_model);
                 }
                 Position p = disasm.getAddressPosition(a.addr);
                 if (p == null) continue;
                 if (a.breakpoint != null && hidePlantingAnnotation(ann_model, a.breakpoint, p)) continue;
                 ann_model.addAnnotation(a, p);
                 a.models.add(ann_model);
             }
         }
         // Disassembly editor
         for (TCFAnnotation a : win_info.annotations) {
             if (a.addr == null) continue;
             IEditorPart editor = editors.get(TCFModel.DisassemblyEditorInput.INSTANCE);
             if (editor == null) continue;
             ITCFDisassemblyPart disasm = (ITCFDisassemblyPart)editor.getAdapter(ITCFDisassemblyPart.class);
             if (disasm == null) continue;
             IAnnotationModel ann_model = disasm.getAnnotationModel();
             if (ann_model == null) continue;
             if (a.models.contains(ann_model)) {
                 ann_model.removeAnnotation(a);
                 a.models.remove(ann_model);
             }
             Position p = disasm.getAddressPosition(a.addr);
             if (p == null) continue;
             if (a.breakpoint != null && hidePlantingAnnotation(ann_model, a.breakpoint, p)) continue;
             ann_model.addAnnotation(a, p);
             a.models.add(ann_model);
         }
         // Source editors
         if (set == null) return;
         for (TCFAnnotation a : set) {
             if (a.area == null) continue;
             Object source_element = TCFSourceLookupDirector.lookup(node.launch, a.ctx, a.area);
             if (source_element == null) continue;
             IEditorInput editor_input = presentation.getEditorInput(source_element);
             IEditorPart editor = editors.get(editor_input);
             if (!(editor instanceof ITextEditor)) continue;
             IDocumentProvider doc_provider = ((ITextEditor)editor).getDocumentProvider();
             IAnnotationModel ann_model = doc_provider.getAnnotationModel(editor_input);
             if (ann_model == null) continue;
             IRegion region = null;
             try {
                 doc_provider.connect(editor_input);
             }
             catch (CoreException e) {
             }
             try {
                 IDocument document = doc_provider.getDocument(editor_input);
                 if (document != null) region = document.getLineInformation(a.area.start_line - 1);
             }
             catch (BadLocationException e) {
             }
             finally {
                 doc_provider.disconnect(editor_input);
             }
             if (region == null) continue;
             Position p = new Position(region.getOffset(), region.getLength());
             if (a.breakpoint != null && hidePlantingAnnotation(ann_model, a.breakpoint, p)) continue;
             ann_model.addAnnotation(a, p);
             a.models.add(ann_model);
         }
     }
 
     private void updateAnnotations(final IWorkbenchWindow window, final TCFNode node) {
         if (disposed) return;
         assert Thread.currentThread() == display.getThread();
         final WorkbenchWindowInfo win_info = windows.get(window);
         if (win_info == null) return;
         ITCFAnnotationProvider provider = TCFAnnotationProvider.getAnnotationProvider(node);
         if (win_info.provider != provider) {
             if (win_info.provider != null) win_info.provider.updateAnnotations(window, null);
             win_info.provider = provider;
         }
         if (win_info.provider != null) {
             if (win_info.annotations.size() > 0) {
                 for (TCFAnnotation a : win_info.annotations) a.dispose();
                 win_info.annotations.clear();
             }
             win_info.update_node = node;
             win_info.update_task = null;
             win_info.provider.updateAnnotations(window, node);
             return;
         }
         if (win_info.update_node == node && win_info.update_task != null && !win_info.update_task.done) return;
         win_info.update_node = node;
         win_info.update_task = new UpdateTask() {
             public void run() {
                 if (win_info.update_task != this) {
                     /* Selection has changed and another update has started - abort this */
                     return;
                 }
                 if (node == null) {
                     /* No selection - no annotations */
                     done(null);
                     return;
                 }
                 if (node.isDisposed()) {
                     /* Selected node disposed - no annotations */
                     done(null);
                     return;
                 }
                 TCFNodeExecContext thread = null;
                 TCFNodeExecContext memory = null;
                 TCFNodeStackFrame frame = null;
                 TCFNodeStackFrame last_top_frame = null;
                 String bp_group = null;
                 boolean suspended = false;
                 if (node instanceof TCFNodeStackFrame) {
                     thread = (TCFNodeExecContext)node.parent;
                     frame = (TCFNodeStackFrame)node;
                     // Make sure frame.getFrameNo() is valid
                     TCFChildrenStackTrace trace = thread.getStackTrace();
                     if (!trace.validate(this)) return;
                 }
                 else if (node instanceof TCFNodeExecContext) {
                     thread = (TCFNodeExecContext)node;
                     // Make sure frame.getTopFrame() is valid
                     TCFChildrenStackTrace trace = thread.getStackTrace();
                     if (!trace.validate(this)) return;
                     frame = trace.getTopFrame();
                 }
                 if (thread != null) {
                     TCFDataCache<IRunControl.RunControlContext> rc_ctx_cache = thread.getRunContext();
                     if (!rc_ctx_cache.validate(this)) return;
                     IRunControl.RunControlContext rc_ctx_data = rc_ctx_cache.getData();
                     if (rc_ctx_data != null) bp_group = rc_ctx_data.getBPGroup();
                     TCFDataCache<TCFNodeExecContext> mem_cache = thread.getMemoryNode();
                     if (!mem_cache.validate(this)) return;
                     memory = mem_cache.getData();
                     if (bp_group == null && memory != null && rc_ctx_data != null && rc_ctx_data.hasState()) bp_group = memory.id;
                     last_top_frame = thread.getLastTopFrame();
                     TCFDataCache<TCFContextState> state_cache = thread.getState();
                     if (!state_cache.validate(this)) return;
                     suspended = state_cache.getData() != null && state_cache.getData().is_suspended;
                 }
                 Set<TCFAnnotation> set = new LinkedHashSet<TCFAnnotation>();
                 if (memory != null) {
                     TCFLaunch launch = node.launch;
                     TCFBreakpointsStatus bs = launch.getBreakpointsStatus();
                     if (bs != null) {
                         for (String id : bs.getStatusIDs()) {
                             Map<String,Object> map = bs.getStatus(id);
                             if (map == null) continue;
                             String error = (String)map.get(IBreakpoints.STATUS_ERROR);
                             if (error != null) addBreakpointErrorAnnotation(set, launch, memory.id, id, error);
                             Object[] arr = toObjectArray(map.get(IBreakpoints.STATUS_INSTANCES));
                             if (arr == null) continue;
                             for (Object o : arr) {
                                 Map<String,Object> m = toObjectMap(o);
                                 String ctx_id = (String)m.get(IBreakpoints.INSTANCE_CONTEXT);
                                 if (ctx_id == null) continue;
                                 if (!ctx_id.equals(node.id) && !ctx_id.equals(bp_group)) continue;
                                 error = (String)m.get(IBreakpoints.INSTANCE_ERROR);
                                 BigInteger addr = JSON.toBigInteger((Number)m.get(IBreakpoints.INSTANCE_ADDRESS));
                                 ILineNumbers.CodeArea area = null;
                                 ILineNumbers.CodeArea org_area = getBreakpointCodeArea(launch, id);
                                 if (addr != null) {
                                     TCFDataCache<TCFSourceRef> line_cache = memory.getLineInfo(addr);
                                     if (line_cache != null) {
                                         if (!line_cache.validate(this)) return;
                                         TCFSourceRef line_data = line_cache.getData();
                                         if (line_data != null) area = line_data.area;
                                     }
                                 }
                                 if (area == null) area = org_area;
                                 String bp_name = "Breakpoint";
                                 IBreakpoint bp = TCFBreakpointsModel.getBreakpointsModel().getBreakpoint(id);
                                 if (bp != null) bp_name = bp.getMarker().getAttribute(TCFBreakpointsModel.ATTR_MESSAGE, bp_name);
                                 int i = bp_name.indexOf(':');
                                 if (i > 0) bp_name = bp_name.substring(0, i);
                                 if (error != null) {
                                     String location = "";
                                     if (addr != null) location = " at 0x" + addr.toString(16);
                                     if (org_area == null) org_area = area;
                                     TCFAnnotation a = new TCFAnnotation(memory.id, id, addr, org_area,
                                             ImageCache.getImage(ImageCache.IMG_BREAKPOINT_ERROR),
                                             bp_name + " failed to plant" + location + ": " + error,
                                             TYPE_BP_INSTANCE);
                                     set.add(a);
                                     error = null;
                                 }
                                 else if (area != null && addr != null) {
                                     String location = " planted at 0x" + addr.toString(16) + ", line " + area.start_line;
                                     TCFAnnotation a = new TCFAnnotation(memory.id, id, addr, area,
                                             ImageCache.getImage(ImageCache.IMG_BREAKPOINT_INSTALLED),
                                             bp_name + location,
                                             TYPE_BP_INSTANCE);
                                     a.breakpoint = bp;
                                     set.add(a);
                                     if (isLineAdjusted(area, org_area)) {
                                         TCFAnnotation b = new TCFAnnotation(memory.id, id, null, org_area,
                                                 ImageCache.getImage(ImageCache.IMG_BREAKPOINT_WARNING),
                                                 "Breakpoint location is adjusted: " + location,
                                                 TYPE_BP_INSTANCE);
                                         set.add(b);
                                     }
                                 }
                             }
                         }
                     }
                 }
                 if (suspended && frame != null && frame.getFrameNo() >= 0) {
                     TCFDataCache<TCFSourceRef> line_cache = frame.getLineInfo();
                     if (!line_cache.validate(this)) return;
                     TCFSourceRef line_data = line_cache.getData();
                     if (line_data != null && line_data.area != null) {
                         TCFAnnotation a = null;
                         String addr_str = "";
                         TCFDataCache<BigInteger> addr_cache = frame.getAddress();
                         if (!addr_cache.validate(this)) return;
                         BigInteger addr_data = addr_cache.getData();
                         if (addr_data != null) addr_str += ", IP: 0x" + addr_data.toString(16);
                         TCFDataCache<IStackTrace.StackTraceContext> frame_cache = frame.getStackTraceContext();
                         if (!frame_cache.validate(this)) return;
                         IStackTrace.StackTraceContext frame_data = frame_cache.getData();
                         if (frame_data != null) {
                             BigInteger i = JSON.toBigInteger(frame_data.getFrameAddress());
                             if (i != null) addr_str += ", FP: 0x" + i.toString(16);
                         }
                         addr_str += ", line: " + line_data.area.start_line;
                         if (frame.getFrameNo() == 0) {
                             a = new TCFAnnotation(line_data.context_id, null, null, line_data.area,
                                     DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER_TOP),
                                     "Current Instruction Pointer" + addr_str,
                                     TYPE_TOP_FRAME);
                         }
                         else {
                             a = new TCFAnnotation(line_data.context_id, null, null, line_data.area,
                                     DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER),
                                     "Call Stack Frame" + addr_str,
                                     TYPE_STACK_FRAME);
                         }
                         set.add(a);
                     }
                 }
                 if (!suspended && last_top_frame != null) {
                     TCFDataCache<TCFSourceRef> line_cache = last_top_frame.getLineInfo();
                     if (!line_cache.validate(this)) return;
                     TCFSourceRef line_data = line_cache.getData();
                     if (line_data != null && line_data.area != null) {
                         TCFAnnotation a = new TCFAnnotation(line_data.context_id, null, null, line_data.area,
                                 DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER),
                                 "Last Instruction Pointer position",
                                 TYPE_STACK_FRAME);
                         set.add(a);
                     }
                 }
                 done(set);
             }
             private void done(final Set<TCFAnnotation> res) {
                 done = true;
                 final Runnable update_task = this;
                 displayExec(new Runnable() {
                     public void run() {
                         if (update_task != win_info.update_task) return;
                         assert win_info.update_node == node;
                         win_info.update_task = null;
                         try {
                             ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                                 public void run(IProgressMonitor monitor) throws CoreException {
                                     updateAnnotations(window, node, res);
                                 }
                             }, null);
                         }
                         catch (Exception e) {
                             Activator.log(e);
                         }
                     }
                 });
             }
         };
         Protocol.invokeLater(win_info.update_task);
     }
 
     private void updateAnnotations(final int cnt) {
         displayExec(new Runnable() {
             public void run() {
                 synchronized (TCFAnnotationManager.this) {
                     if (cnt != update_unnotations_cnt) return;
                 }
                 for (IWorkbenchWindow window : windows.keySet()) {
                     if (dirty_windows.contains(null) || dirty_windows.contains(window)) {
                         TCFNode node = null;
                         try {
                             ISelection active_context = DebugUITools.getDebugContextManager()
                                     .getContextService(window).getActiveContext();
                             if (active_context instanceof IStructuredSelection) {
                                 IStructuredSelection selection = (IStructuredSelection)active_context;
                                 if (!selection.isEmpty()) {
                                     Object first_element = selection.getFirstElement();
                                     if (first_element instanceof IAdaptable) {
                                         node = (TCFNode)((IAdaptable)first_element).getAdapter(TCFNode.class);
                                     }
                                 }
                             }
                             if (dirty_launches.contains(null) || node != null && dirty_launches.contains(node.launch)) {
                                 updateAnnotations(window, node);
                             }
                         }
                         catch (Throwable x) {
                             if (node == null || !node.isDisposed()) {
                                 Activator.log("Cannot update editor annotations", x);
                             }
                         }
                     }
                 }
                 for (TCFLaunch launch : dirty_launches) {
                     if (launch != null) launch.removePendingClient(TCFAnnotationManager.this);
                 }
                 changed_launch_cfgs.clear();
                 dirty_windows.clear();
                 dirty_launches.clear();
             }
         });
     }
 
     synchronized void updateAnnotations(final IWorkbenchWindow window, final TCFLaunch launch) {
         final int cnt = ++update_unnotations_cnt;
         displayExec(new Runnable() {
             public void run() {
                 dirty_windows.add(window);
                 dirty_launches.add(launch);
                 updateAnnotations(cnt);
             }
         });
     }
 }
