 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.adapters;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.debug.ui.IDebugView;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.internal.debug.model.TCFContextState;
 import org.eclipse.tcf.internal.debug.model.TCFSourceRef;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext.MemoryRegion;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.IStackTrace;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.tcf.util.TCFTask;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertySource;
 import org.eclipse.ui.views.properties.PropertyDescriptor;
 import org.eclipse.ui.views.properties.PropertySheet;
 import org.eclipse.ui.views.properties.PropertySheetPage;
 
 /**
  * Adapts TCFNode to IPropertySource.
  */
 public class TCFNodePropertySource implements IPropertySource {
 
     private final TCFNode node;
     private final Map<String, Object> properties = new HashMap<String, Object>();
 
     private IPropertyDescriptor[] descriptors;
 
     public TCFNodePropertySource(TCFNode node) {
         this.node = node;
     }
 
     public Object getEditableValue() {
         return null;
     }
 
     public IPropertyDescriptor[] getPropertyDescriptors() {
         if (descriptors == null) {
             try {
                 final List<IPropertyDescriptor> list = new ArrayList<IPropertyDescriptor>();
                 descriptors = new TCFTask<IPropertyDescriptor[]>(node.getChannel()) {
                     public void run() {
                         list.clear();
                         properties.clear();
                         if (node instanceof TCFNodeExecContext) {
                             getExecContextDescriptors((TCFNodeExecContext)node);
                         }
                         else if (node instanceof TCFNodeStackFrame) {
                             getFrameDescriptors((TCFNodeStackFrame)node);
                         }
                         else {
                             done(list.toArray(new IPropertyDescriptor[list.size()]));
                         }
                     }
 
                     private void getFrameDescriptors(TCFNodeStackFrame frameNode) {
                         TCFDataCache<IStackTrace.StackTraceContext> ctx_cache = frameNode.getStackTraceContext();
                         TCFDataCache<TCFSourceRef> line_info_cache = frameNode.getLineInfo();
                         if (!validateAll(ctx_cache, line_info_cache)) return;
                         IStackTrace.StackTraceContext ctx = ctx_cache.getData();
                         if (ctx != null) {
                             Map<String, Object> props = ctx.getProperties();
                             for (String key : props.keySet()) {
                                 Object value = props.get(key);
                                 if (value instanceof Number) {
                                     value = toHexAddrString((Number)value);
                                 }
                                 addDescriptor("Context", key, value);
                             }
                         }
                         TCFSourceRef ref = line_info_cache.getData();
                         if (ref != null) {
                             if (ref.area != null) {
                                 if (ref.area.directory != null) addDescriptor("Source", "Directory", ref.area.directory);
                                 if (ref.area.file != null) addDescriptor("Source", "File", ref.area.file);
                                 if (ref.area.start_line > 0) addDescriptor("Source", "Line", ref.area.start_line);
                                 if (ref.area.start_column > 0) addDescriptor("Source", "Column", ref.area.start_column);
                             }
                             if (ref.error != null) {
                                 addDescriptor("Source", "Error", TCFModel.getErrorMessage(ref.error, false));
                             }
                         }
                         done(list.toArray(new IPropertyDescriptor[list.size()]));
                     }
 
                     private void getExecContextDescriptors(TCFNodeExecContext exe_node) {
                         TCFDataCache<IRunControl.RunControlContext> ctx_cache = exe_node.getRunContext();
                         TCFDataCache<TCFContextState> state_cache = exe_node.getState();
                         TCFDataCache<MemoryRegion[]> mem_map_cache = exe_node.getMemoryMap();
                         if (!validateAll(ctx_cache, state_cache, mem_map_cache)) return;
                         IRunControl.RunControlContext ctx = ctx_cache.getData();
                         if (ctx != null) {
                             Map<String, Object> props = ctx.getProperties();
                             for (String key : props.keySet()) {
                                 Object value = props.get(key);
                                 if (value instanceof Number) {
                                     value = toHexAddrString((Number)value);
                                 }
                                 addDescriptor("Context", key, value);
                             }
                         }
                         TCFContextState state = state_cache.getData();
                         if (state != null) {
                             addDescriptor("State", "Suspended", state.is_suspended);
                             if (state.suspend_reason != null) addDescriptor("State", "Suspend reason", state.suspend_reason);
                             if (state.suspend_pc != null) addDescriptor("State", "PC", toHexAddrString(new BigInteger(state.suspend_pc)));
                             addDescriptor("State", "Active", !exe_node.isNotActive());
                             if (state.suspend_params != null) {
                                 for (String key : state.suspend_params.keySet()) {
                                     Object value = state.suspend_params.get(key);
                                     if (value instanceof Number) {
                                         value = toHexAddrString((Number)value);
                                     }
                                     addDescriptor("State Properties", key, value);
                                 }
                             }
                         }
                         MemoryRegion[] mem_map = mem_map_cache.getData();
                         if (mem_map != null && mem_map.length > 0) {
                             int idx = 0;
                             for (MemoryRegion region : mem_map) {
                                 Map<String, Object> props = region.region.getProperties();
                                 for (String key : props.keySet()) {
                                     Object value = props.get(key);
                                     if (value instanceof Number) {
                                         value = toHexAddrString((Number)value);
                                     }
                                     addDescriptor("MemoryRegion[" + idx + ']', key, value);
                                 }
                                 idx++;
                             }
                         }
                         done(list.toArray(new IPropertyDescriptor[list.size()]));
                     }
 
                     private void addDescriptor(String category, String key, Object value) {
                         String id = category + '.' + key;
                         PropertyDescriptor desc = new PropertyDescriptor(id, key);
                         desc.setCategory(category);
                         list.add(desc);
                         properties.put(id, value);
                     }
 
                     boolean validateAll(TCFDataCache<?>... caches) {
                         TCFDataCache<?> pending = null;
                         for (TCFDataCache<?> cache : caches) {
                             if (!cache.validate()) pending = cache;
                         }
                         if (pending != null) {
                             pending.wait(this);
                             return false;
                         }
                         return true;
                     }
                }.get();
             }
             catch (Exception e) {
                if (node.getChannel().getState() != IChannel.STATE_CLOSED) {
                    Activator.log("Error retrieving property data", e);
                }
                 descriptors = new IPropertyDescriptor[0];
             }
         }
         return descriptors;
     }
 
     public Object getPropertyValue(final Object id) {
         return properties.get(id);
     }
 
     public boolean isPropertySet(Object id) {
         return false;
     }
 
     public void resetPropertyValue(Object id) {
     }
 
     public void setPropertyValue(Object id, Object value) {
     }
 
     private static String toHexAddrString(Number num) {
         BigInteger n = JSON.toBigInteger(num);
         String s = n.toString(16);
         int sz = s.length() > 8 ? 16 : 8;
         int l = sz - s.length();
         if (l < 0) l = 0;
         if (l > 16) l = 16;
         return "0x0000000000000000".substring(0, 2 + l) + s;
     }
 
     private static final long REFRESH_DELAY = 250;
     private static boolean refresh_posted = false;
     private static long refresh_time = 0;
 
     public static void refresh(TCFNode node) {
         assert Protocol.isDispatchThread();
         refresh_time = System.currentTimeMillis();
         if (refresh_posted) return;
         refresh_posted = true;
         Protocol.invokeLater(REFRESH_DELAY, new Runnable() {
             public void run() {
                 long time = System.currentTimeMillis();
                 if (time - refresh_time < REFRESH_DELAY * 3 / 4) {
                     Protocol.invokeLater(refresh_time + REFRESH_DELAY - time, this);
                     return;
                 }
                 refresh_posted = false;
                 synchronized (Device.class) {
                     Display display = Display.getDefault();
                     if (!display.isDisposed()) {
                         display.asyncExec(new Runnable() {
                             public void run() {
                                 for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                                     IWorkbenchPart active_part = window.getActivePage().getActivePart();
                                     if (active_part instanceof IDebugView) {
                                         IViewPart part = window.getActivePage().findView("org.eclipse.ui.views.PropertySheet");
                                         if (part instanceof PropertySheet) {
                                             PropertySheet props = (PropertySheet)part;
                                             PropertySheetPage page = (PropertySheetPage)props.getCurrentPage();
                                             // TODO: need to check Properties view selection to skip unnecessary refreshes
                                             if (page != null) page.refresh();
                                         }
                                     }
                                 }
                             }
                         });
                     }
                 }
             }
         });
     }
 }
