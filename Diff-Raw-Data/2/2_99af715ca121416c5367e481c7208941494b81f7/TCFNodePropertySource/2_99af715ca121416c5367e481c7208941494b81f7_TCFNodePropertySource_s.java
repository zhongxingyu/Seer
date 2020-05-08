 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others.
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
 import java.util.Set;
 
 import org.eclipse.debug.ui.IDebugView;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.internal.debug.model.TCFContextState;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.model.TCFSourceRef;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExpression;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeLaunch;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeModule;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeRegister;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext.MemoryRegion;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeStackFrame;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IExpressions;
 import org.eclipse.tcf.services.IRegisters;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.IStackTrace;
 import org.eclipse.tcf.services.ISymbols;
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
     private final Map<String,Object> properties = new HashMap<String,Object>();
 
     private IPropertyDescriptor[] descriptors;
 
     public TCFNodePropertySource(TCFNode node) {
         this.node = node;
     }
 
     public Object getEditableValue() {
         return null;
     }
 
     public IPropertyDescriptor[] getPropertyDescriptors() {
         if (descriptors == null) {
             if (node == null) {
                 // A disconnected TCF launch was selected
                 return descriptors = new IPropertyDescriptor[0];
             }
             try {
                 final List<IPropertyDescriptor> list = new ArrayList<IPropertyDescriptor>();
                 descriptors = new TCFTask<IPropertyDescriptor[]>(node.getChannel()) {
                     public void run() {
                         list.clear();
                         properties.clear();
                         if (node instanceof TCFNodeLaunch) {
                             getLaunchDescriptors((TCFNodeLaunch)node);
                         }
                         else if (node instanceof TCFNodeExecContext) {
                             getExecContextDescriptors((TCFNodeExecContext)node);
                         }
                         else if (node instanceof TCFNodeStackFrame) {
                             getFrameDescriptors((TCFNodeStackFrame)node);
                         }
                         else if (node instanceof TCFNodeRegister) {
                             getRegisterDescriptors((TCFNodeRegister)node);
                         }
                         else if (node instanceof TCFNodeModule) {
                             getModuleDescriptors((TCFNodeModule)node);
                         }
                         else if (node instanceof TCFNodeExpression) {
                             getExpressionDescriptors((TCFNodeExpression)node);
                         }
                         else {
                             done(list.toArray(new IPropertyDescriptor[list.size()]));
                         }
                     }
 
                     private void getExpressionDescriptors(TCFNodeExpression exp_node) {
                         TCFDataCache<IExpressions.Expression> exp_cache = exp_node.getExpression();
                         if (!exp_cache.validate(this)) return;
                         TCFDataCache<ISymbols.Symbol> type_cache = exp_node.getType();
                         if (!type_cache.validate(this)) return;
                         TCFDataCache<IExpressions.Value> value_cache = exp_node.getValue();
                         if (!value_cache.validate(this)) return;
 
                         Throwable exp_error = exp_cache.getError();
                         if (exp_error != null) addDescriptor("Expression", "Error", TCFModel.getErrorMessage(exp_error, false));
                         IExpressions.Expression exp_data = exp_cache.getData();
                         if (exp_data != null) {
                             Map<String,Object> props = exp_data.getProperties();
                             for (String key : props.keySet()) {
                                 Object value = props.get(key);
                                 if (value instanceof Number) {
                                     value = toHexAddrString((Number)value);
                                 }
                                 addDescriptor("Expression", key, value);
                             }
                         }
 
                         Throwable type_error = type_cache.getError();
                         if (type_error != null) addDescriptor("Expression Type", "Error", TCFModel.getErrorMessage(type_error, false));
                         ISymbols.Symbol type_data = type_cache.getData();
                         if (type_data != null) {
                             Map<String,Object> props = type_data.getProperties();
                             for (String key : props.keySet()) {
                                 Object value = props.get(key);
                                 if (value instanceof Number) {
                                     value = toHexAddrString((Number)value);
                                 }
                                 addDescriptor("Expression Type", key, value);
                             }
                         }
 
                        Throwable value_error = type_cache.getError();
                         if (value_error != null) addDescriptor("Expression Value", "Error", TCFModel.getErrorMessage(value_error, false));
                         IExpressions.Value value_data = value_cache.getData();
                         if (value_data != null) {
                             Map<String,Object> props = value_data.getProperties();
                             for (String key : props.keySet()) {
                                 Object value = props.get(key);
                                 if (value instanceof Number) {
                                     value = toHexAddrString((Number)value);
                                 }
                                 addDescriptor("Expression Value", key, value);
                             }
                             String sym_id = value_data.getSymbolID();
                             if (sym_id != null) {
                                 TCFDataCache<ISymbols.Symbol> sym_cache = exp_node.getModel().getSymbolInfoCache(sym_id);
                                 if (!sym_cache.validate(this)) return;
                                 Throwable sym_error = sym_cache.getError();
                                 if (sym_error != null) addDescriptor("Expression Value Symbol", "Error", TCFModel.getErrorMessage(sym_error, false));
                                 ISymbols.Symbol sym_data = sym_cache.getData();
                                 if (sym_data != null) {
                                     props = sym_data.getProperties();
                                     for (String key : props.keySet()) {
                                         Object value = props.get(key);
                                         if (value instanceof Number) {
                                             value = toHexAddrString((Number)value);
                                         }
                                         addDescriptor("Expression Value Symbol", key, value);
                                     }
                                 }
                             }
                         }
                         done(list.toArray(new IPropertyDescriptor[list.size()]));
                     }
 
                     private void getModuleDescriptors(TCFNodeModule mod_node) {
                         TCFDataCache<MemoryRegion> mod_cache = mod_node.getRegion();
                         if (!mod_cache.validate(this)) return;
                         Throwable error = mod_cache.getError();
                         if (error != null) addDescriptor("Module Properties", "Error", TCFModel.getErrorMessage(error, false));
                         MemoryRegion ctx = mod_cache.getData();
                         if (ctx != null && ctx.region != null) {
                             Map<String,Object> props = ctx.region.getProperties();
                             for (String key : props.keySet()) {
                                 Object value = props.get(key);
                                 if (value instanceof Number) {
                                     value = toHexAddrString((Number)value);
                                 }
                                 addDescriptor("Module Properties", key, value);
                             }
                         }
                         done(list.toArray(new IPropertyDescriptor[list.size()]));
                     }
 
                     private void getRegisterDescriptors(TCFNodeRegister reg_node) {
                         TCFDataCache<IRegisters.RegistersContext> ctx_cache = reg_node.getContext();
                         if (!ctx_cache.validate(this)) return;
                         Throwable error = ctx_cache.getError();
                         if (error != null) addDescriptor("Register Properties", "Error", TCFModel.getErrorMessage(error, false));
                         IRegisters.RegistersContext ctx = ctx_cache.getData();
                         if (ctx != null) {
                             Map<String,Object> props = ctx.getProperties();
                             for (String key : props.keySet()) {
                                 Object value = props.get(key);
                                 if (value instanceof Number) {
                                     value = toHexAddrString((Number)value);
                                 }
                                 addDescriptor("Register Properties", key, value);
                             }
                         }
                         done(list.toArray(new IPropertyDescriptor[list.size()]));
                     }
 
                     private void getFrameDescriptors(TCFNodeStackFrame frameNode) {
                         TCFDataCache<IStackTrace.StackTraceContext> ctx_cache = frameNode.getStackTraceContext();
                         TCFDataCache<TCFSourceRef> line_info_cache = frameNode.getLineInfo();
                         if (!validateAll(ctx_cache, line_info_cache)) return;
                         IStackTrace.StackTraceContext ctx = ctx_cache.getData();
                         if (ctx != null) {
                             Map<String,Object> props = ctx.getProperties();
                             for (String key : props.keySet()) {
                                 Object value = props.get(key);
                                 if (value instanceof Number) {
                                     value = toHexAddrString((Number)value);
                                 }
                                 addDescriptor("Stack Frame Properties", key, value);
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
                             Map<String,Object> props = ctx.getProperties();
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
                         done(list.toArray(new IPropertyDescriptor[list.size()]));
                     }
 
                     private void getLaunchDescriptors(TCFNodeLaunch launch_node) {
                         IChannel channel = launch_node.getChannel();
                         for (String s : channel.getRemoteServices()) {
                             addDescriptor("Target Services", s, "");
                         }
                         TCFLaunch launch = launch_node.getModel().getLaunch();
                         Set<String> filter = launch.getContextFilter();
                         if (filter != null) addDescriptor("Context Filter", "Context IDs", filter.toString());
                         done(list.toArray(new IPropertyDescriptor[list.size()]));
                     }
 
                     private void addDescriptor(String category, String key, Object value) {
                         String id = category + '.' + key;
                         PropertyDescriptor desc = new PropertyDescriptor(id, key);
                         desc.setCategory(category);
                         list.add(desc);
                         properties.put(id, value);
                     }
 
                     private boolean validateAll(TCFDataCache<?>... caches) {
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
                     @Override
                     public void done(IPropertyDescriptor[] r) {
                         need_refresh = true;
                         super.done(r);
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
     private static boolean need_refresh = false;
 
     public static void refresh(TCFNode node) {
         assert Protocol.isDispatchThread();
         if (!need_refresh) return;
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
                 need_refresh = false;
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
