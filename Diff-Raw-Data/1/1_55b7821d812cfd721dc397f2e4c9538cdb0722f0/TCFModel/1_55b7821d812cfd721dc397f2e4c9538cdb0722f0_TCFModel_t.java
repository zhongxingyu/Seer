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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IExpressionManager;
 import org.eclipse.debug.core.IExpressionsListener;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.commands.IDisconnectHandler;
 import org.eclipse.debug.core.commands.IDropToFrameHandler;
 import org.eclipse.debug.core.commands.IResumeHandler;
 import org.eclipse.debug.core.commands.IStepIntoHandler;
 import org.eclipse.debug.core.commands.IStepOverHandler;
 import org.eclipse.debug.core.commands.IStepReturnHandler;
 import org.eclipse.debug.core.commands.ISuspendHandler;
 import org.eclipse.debug.core.commands.ITerminateHandler;
 import org.eclipse.debug.core.model.IDebugModelProvider;
 import org.eclipse.debug.core.model.IExpression;
 import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
 import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
 import org.eclipse.debug.core.model.ISourceLocator;
 import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
 import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.debug.ui.IDebugView;
 import org.eclipse.debug.ui.ISourcePresentation;
 import org.eclipse.debug.ui.contexts.ISuspendTrigger;
 import org.eclipse.debug.ui.contexts.ISuspendTriggerListener;
 import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditorInput;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.tcf.core.Command;
 import org.eclipse.tcf.debug.ui.ITCFModel;
 import org.eclipse.tcf.debug.ui.ITCFPresentationProvider;
 import org.eclipse.tcf.debug.ui.ITCFSourceDisplay;
 import org.eclipse.tcf.internal.debug.actions.TCFAction;
 import org.eclipse.tcf.internal.debug.launch.TCFSourceLookupDirector;
 import org.eclipse.tcf.internal.debug.launch.TCFSourceLookupParticipant;
 import org.eclipse.tcf.internal.debug.model.ITCFConstants;
 import org.eclipse.tcf.internal.debug.model.TCFContextState;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.model.TCFSourceRef;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.adapters.TCFNodePropertySource;
 import org.eclipse.tcf.internal.debug.ui.commands.BackIntoCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.BackOverCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.BackResumeCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.BackReturnCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.DisconnectCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.DropToFrameCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.ResumeCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.StepIntoCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.StepOverCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.StepReturnCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.SuspendCommand;
 import org.eclipse.tcf.internal.debug.ui.commands.TerminateCommand;
 import org.eclipse.tcf.internal.debug.ui.preferences.TCFPreferences;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IErrorReport;
 import org.eclipse.tcf.protocol.IService;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IDisassembly;
 import org.eclipse.tcf.services.ILineNumbers;
 import org.eclipse.tcf.services.IMemory;
 import org.eclipse.tcf.services.IMemoryMap;
 import org.eclipse.tcf.services.IPathMap;
 import org.eclipse.tcf.services.IProcesses;
 import org.eclipse.tcf.services.IRegisters;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.IStackTrace;
 import org.eclipse.tcf.services.ISymbols;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.tcf.util.TCFTask;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPersistableElement;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * TCFModel represents remote target state as it is known to host.
  * The main job of the model is caching remote data,
  * keeping the cache in a coherent state,
  * and feeding UI with up-to-date data.
  */
 public class TCFModel implements ITCFModel, IElementContentProvider, IElementLabelProvider, IViewerInputProvider,
         IModelProxyFactory, IColumnPresentationFactory, ITCFSourceDisplay, ISuspendTrigger, IElementMementoProvider {
 
     /** The id of the expression hover presentation context */
     public static final String ID_EXPRESSION_HOVER = Activator.PLUGIN_ID + ".expression_hover";
 
     /** The id of a pinned view description presentation context */
     public static final String ID_PINNED_VIEW = Activator.PLUGIN_ID + ".pinned_view";
 
     public static final int
         UPDATE_POLICY_AUTOMATIC  = 0,
         UPDATE_POLICY_MANUAL     = 1,
         UPDATE_POLICY_BREAKPOINT = 2;
 
     /**
      * A dummy editor input to open the disassembly view as editor.
      */
     public static class DisassemblyEditorInput implements IEditorInput {
         final static String EDITOR_ID = "org.eclipse.cdt.dsf.ui.disassembly";
         final static DisassemblyEditorInput INSTANCE = new DisassemblyEditorInput();
 
         @SuppressWarnings("rawtypes")
         public Object getAdapter(Class adapter) {
             return null;
         }
 
         public boolean exists() {
             return false;
         }
 
         public ImageDescriptor getImageDescriptor() {
             return null;
         }
 
         public String getName() {
             return "Disassembly";
         }
 
         public IPersistableElement getPersistable() {
             return null;
         }
 
         public String getToolTipText() {
             return "Disassembly";
         }
     }
 
     private final TCFLaunch launch;
     private final Display display;
     private final IExpressionManager expr_manager;
     private final TCFAnnotationManager annotation_manager;
 
     private final List<ISuspendTriggerListener> suspend_trigger_listeners =
         new LinkedList<ISuspendTriggerListener>();
 
     final List<ITCFPresentationProvider> view_request_listeners;
 
     private final Map<IWorkbenchPage,Object> display_source_generation =
             new WeakHashMap<IWorkbenchPage,Object>();
 
     private int suspend_trigger_generation;
     private int auto_disconnect_generation;
 
     private boolean reverse_debug_enabled;
 
     // Debugger preferences:
     private long min_view_updates_interval;
     private boolean view_updates_throttle_enabled;
     private boolean channel_throttle_enabled;
     private boolean wait_for_pc_update_after_step;
     private boolean wait_for_views_update_after_step;
     private boolean delay_stack_update_until_last_step;
     private boolean stack_frames_limit_enabled;
     private int stack_frames_limit_value;
     private boolean show_function_arg_names;
     private boolean show_function_arg_values;
     private boolean delay_children_list_updates;
     private boolean auto_children_list_updates;
 
     private final Map<String,String> action_results = new HashMap<String,String>();
     private final HashMap<String,TCFAction> active_actions = new HashMap<String,TCFAction>();
 
     private final List<TCFModelProxy> model_proxies = new ArrayList<TCFModelProxy>();
 
     private final Map<String,TCFNode> id2node = new HashMap<String,TCFNode>();
 
     private final Map<Class<?>,Object> adapters = new HashMap<Class<?>,Object>();
 
     private class MemoryBlocksUpdate extends TCFDataCache<Map<String,TCFMemoryBlockRetrieval>> {
 
         final Set<String> changeset = new HashSet<String>();
         final Set<String> suspended = new HashSet<String>();
 
         MemoryBlocksUpdate(IChannel channel) {
             super(channel);
             Protocol.invokeLater(new Runnable() {
                 public void run() {
                     if (!validate(this)) return;
                     Map<String,TCFMemoryBlockRetrieval> map = getData();
                     if (map != null) { // map can be null if, for example, the channel was closed
                         for (TCFMemoryBlockRetrieval r : map.values()) {
                             r.onMemoryChanged(suspended.contains(r.getMemoryID()));
                         }
                     }
                     launch.removePendingClient(mem_blocks_update);
                     mem_blocks_update = null;
                 }
             });
         }
 
         void add(String id, boolean suspended) {
             changeset.add(id);
             if (suspended) this.suspended.add(id);
         }
 
         public boolean startDataRetrieval() {
             // Map changed contexts to memory nodes, and then to memory block retrieval objects
             Map<String,TCFMemoryBlockRetrieval> map = new HashMap<String,TCFMemoryBlockRetrieval>();
             for (String id : changeset) {
                 if (map.get(id) != null) continue;
                 TCFNode node = id2node.get(id);
                 if (node == null) {
                     if (!createNode(id, this)) return false;
                     if (isValid()) {
                         Activator.log("Cannot create debug model node", getError());
                         reset();
                         continue;
                     }
                     node = id2node.get(id);
                 }
                 if (node instanceof TCFNodeExecContext) {
                     TCFDataCache<TCFNodeExecContext> c = ((TCFNodeExecContext)node).getMemoryNode();
                     if (!c.validate(this)) return false;
                     node = c.getData();
                     if (node == null) continue;
                     TCFMemoryBlockRetrieval r = mem_retrieval.get(node.id);
                     if (r != null) {
                         map.put(node.id, r);
                         if (suspended.contains(id)) suspended.add(node.id);
                     }
                 }
             }
             set(null, null, map);
             return true;
         }
     }
 
     private final Map<String,TCFMemoryBlockRetrieval> mem_retrieval = new HashMap<String,TCFMemoryBlockRetrieval>();
     private MemoryBlocksUpdate mem_blocks_update;
 
     private final Map<String,String> cast_to_type_map = new HashMap<String,String>();
 
     private final Map<String,Object> context_map = new HashMap<String,Object>();
 
     private final Map<IWorkbenchPart,TCFNode> pins = new HashMap<IWorkbenchPart,TCFNode>();
     private final Map<IWorkbenchPart,TCFSnapshot> locks = new HashMap<IWorkbenchPart,TCFSnapshot>();
     private final Map<IWorkbenchPart,Integer> lock_policy = new HashMap<IWorkbenchPart,Integer>();
 
     private final Map<String,TCFConsole> process_consoles = new HashMap<String,TCFConsole>();
     private final List<TCFConsole> debug_consoles = new ArrayList<TCFConsole>();
     private TCFConsole dprintf_console;
 
     private static final Map<ILaunchConfiguration,IEditorInput> editor_not_found =
         new HashMap<ILaunchConfiguration,IEditorInput>();
 
     private final IModelSelectionPolicyFactory model_selection_factory = new IModelSelectionPolicyFactory() {
         public IModelSelectionPolicy createModelSelectionPolicyAdapter(
                 Object element, IPresentationContext context) {
             return selection_policy;
         }
     };
 
     private final IModelSelectionPolicy selection_policy;
 
     private IChannel channel;
     private TCFNodeLaunch launch_node;
     private boolean disposed;
 
     private final IMemory.MemoryListener mem_listener = new IMemory.MemoryListener() {
 
         public void contextAdded(IMemory.MemoryContext[] contexts) {
             for (IMemory.MemoryContext ctx : contexts) {
                 String id = ctx.getParentID();
                 if (id == null) {
                     launch_node.onContextAdded(ctx);
                 }
                 else {
                     TCFNode node = getNode(id);
                     if (node instanceof TCFNodeExecContext) {
                         ((TCFNodeExecContext)node).onContextAdded(ctx);
                     }
                 }
             }
             launch_node.onAnyContextAddedOrRemoved();
         }
 
         public void contextChanged(IMemory.MemoryContext[] contexts) {
             for (IMemory.MemoryContext ctx : contexts) {
                 String id = ctx.getID();
                 TCFNode node = getNode(id);
                 if (node instanceof TCFNodeExecContext) {
                     ((TCFNodeExecContext)node).onContextChanged(ctx);
                 }
                 onMemoryChanged(id, true, false);
             }
         }
 
         public void contextRemoved(final String[] context_ids) {
             onContextRemoved(context_ids);
         }
 
         public void memoryChanged(String id, Number[] addr, long[] size) {
             TCFNode node = getNode(id);
             if (node instanceof TCFNodeExecContext) {
                 ((TCFNodeExecContext)node).onMemoryChanged(addr, size);
             }
             onMemoryChanged(id, true, false);
         }
     };
 
     private final IRunControl.RunControlListener run_listener = new IRunControl.RunControlListener() {
 
         public void containerResumed(String[] context_ids) {
             for (String id : context_ids) {
                 TCFNode node = getNode(id);
                 if (node instanceof TCFNodeExecContext) {
                     ((TCFNodeExecContext)node).onContainerResumed();
                 }
             }
             updateAnnotations(null);
         }
 
         public void containerSuspended(String context, String pc, String reason,
                 Map<String,Object> params, String[] suspended_ids) {
             boolean func_call = false;
             if (params != null) {
                 Boolean b = (Boolean)params.get("FuncCall");
                 func_call = b != null && b.booleanValue();
             }
             int action_cnt = 0;
             for (String id : suspended_ids) {
                 TCFNode node = getNode(id);
                 action_results.remove(id);
                 if (active_actions.get(id) != null) action_cnt++;
                 if (!id.equals(context) && node instanceof TCFNodeExecContext) {
                     ((TCFNodeExecContext)node).onContainerSuspended(func_call);
                 }
                 onMemoryChanged(id, false, true);
             }
             TCFNode node = getNode(context);
             if (node instanceof TCFNodeExecContext) {
                 ((TCFNodeExecContext)node).onContextSuspended(pc, reason, params, func_call);
             }
             launch_node.onAnyContextSuspendedOrChanged();
             if (!func_call && action_cnt == 0) {
                 setDebugViewSelection(node, reason);
                 updateAnnotations(null);
                 TCFNodePropertySource.refresh(node);
             }
             action_results.remove(context);
         }
 
         public void contextAdded(IRunControl.RunControlContext[] contexts) {
             for (IRunControl.RunControlContext ctx : contexts) {
                 String id = ctx.getParentID();
                 if (id == null) {
                     launch_node.onContextAdded(ctx);
                 }
                 else {
                     TCFNode node = getNode(id);
                     if (node instanceof TCFNodeExecContext) {
                         ((TCFNodeExecContext)node).onContextAdded(ctx);
                     }
                 }
                 context_map.put(ctx.getID(), ctx);
             }
             launch_node.onAnyContextAddedOrRemoved();
         }
 
         public void contextChanged(IRunControl.RunControlContext[] contexts) {
             for (IRunControl.RunControlContext ctx : contexts) {
                 String id = ctx.getID();
                 context_map.put(id, ctx);
                 TCFNode node = getNode(id);
                 if (node instanceof TCFNodeExecContext) {
                     ((TCFNodeExecContext)node).onContextChanged(ctx);
                 }
                 onMemoryChanged(id, true, false);
                 if (active_actions.get(id) == null) {
                     TCFNodePropertySource.refresh(node);
                 }
             }
             launch_node.onAnyContextSuspendedOrChanged();
         }
 
         public void contextException(String context, String msg) {
             TCFNode node = getNode(context);
             if (node instanceof TCFNodeExecContext) {
                 ((TCFNodeExecContext)node).onContextException(msg);
             }
         }
 
         public void contextRemoved(final String[] context_ids) {
             onContextRemoved(context_ids);
         }
 
         public void contextResumed(String id) {
             TCFNode node = getNode(id);
             if (node instanceof TCFNodeExecContext) {
                 ((TCFNodeExecContext)node).onContextResumed();
             }
             updateAnnotations(null);
         }
 
         public void contextSuspended(String id, String pc, String reason, Map<String,Object> params) {
             boolean func_call = false;
             if (params != null) {
                 Boolean b = (Boolean)params.get("FuncCall");
                 func_call = b != null && b.booleanValue();
             }
             TCFNode node = getNode(id);
             action_results.remove(id);
             if (node instanceof TCFNodeExecContext) {
                 ((TCFNodeExecContext)node).onContextSuspended(pc, reason, params, func_call);
             }
             launch_node.onAnyContextSuspendedOrChanged();
             if (!func_call && active_actions.get(id) == null) {
                 setDebugViewSelection(node, reason);
                 updateAnnotations(null);
                 TCFNodePropertySource.refresh(node);
             }
             onMemoryChanged(id, false, true);
         }
     };
 
     private final IMemoryMap.MemoryMapListener mmap_listener = new IMemoryMap.MemoryMapListener() {
 
         public void changed(String id) {
             TCFNode node = getNode(id);
             if (node instanceof TCFNodeExecContext) {
                 TCFNodeExecContext exe = (TCFNodeExecContext)node;
                 exe.onMemoryMapChanged();
             }
             onMemoryChanged(id, true, false);
             refreshSourceView();
         }
     };
 
     private final IPathMap.PathMapListener pmap_listener = new IPathMap.PathMapListener() {
 
         public void changed() {
             refreshSourceView();
         }
     };
 
     private final IRegisters.RegistersListener reg_listener = new IRegisters.RegistersListener() {
 
         public void contextChanged() {
             for (TCFNode node : id2node.values()) {
                 if (node instanceof TCFNodeExecContext) {
                     ((TCFNodeExecContext)node).onRegistersChanged();
                 }
             }
         }
 
         public void registerChanged(final String id) {
             /*
              * We need to propagate register changes to parent node,
              * because, for example, stack frames might need to be updated.
              * For that we need to know parent of the register.
              * It means we have to create register node if it does not exist yet.
              */
             // TODO: async handling of registerChanged event - potential data coherency issue
             Runnable done = new Runnable() {
                 @Override
                 public void run() {
                     TCFNode node = getNode(id);
                     if (node instanceof TCFNodeRegister) {
                         ((TCFNodeRegister)node).onValueChanged();
                     }
                 }
             };
             if (createNode(id, done)) done.run();
         }
     };
 
     private final IProcesses.ProcessesListener prs_listener = new IProcesses.ProcessesListener() {
 
         public void exited(String process_id, int exit_code) {
             IProcesses.ProcessContext prs = launch.getProcessContext();
             if (prs != null && process_id.equals(prs.getID())) onContextOrProcessRemoved();
         }
     };
 
     private final IExpressionsListener expressions_listener = new IExpressionsListener() {
 
         int generation;
 
         public void expressionsAdded(IExpression[] expressions) {
             expressionsRemoved(expressions);
         }
 
         public void expressionsChanged(IExpression[] expressions) {
             expressionsRemoved(expressions);
         }
 
         public void expressionsRemoved(IExpression[] expressions) {
             final int g = ++generation;
             Protocol.invokeLater(new Runnable() {
                 public void run() {
                     if (g != generation) return;
                     for (TCFNode n : id2node.values()) {
                         if (n instanceof TCFNodeExecContext) {
                             ((TCFNodeExecContext)n).onExpressionAddedOrRemoved();
                         }
                     }
                     for (TCFModelProxy p : model_proxies) {
                         String id = p.getPresentationContext().getId();
                         if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(id)) {
                             Object o = p.getInput();
                             if (o instanceof TCFNode) {
                                 TCFNode n = (TCFNode)o;
                                 if (n.model == TCFModel.this) p.addDelta(n, IModelDelta.CONTENT);
                             }
                         }
                     }
                 }
             });
         }
     };
 
     private final TCFLaunch.ActionsListener actions_listener = new TCFLaunch.ActionsListener() {
 
         public void onContextActionStart(TCFAction action) {
             final String id = action.getContextID();
             active_actions.put(id, action);
             updateAnnotations(null);
         }
 
         public void onContextActionResult(String id, String reason) {
             if (reason == null) action_results.remove(id);
             else action_results.put(id, reason);
         }
 
         public void onContextActionDone(final TCFAction action) {
             Runnable r = new Runnable() {
                 @Override
                 public void run() {
                     TCFContextState state_data = null;
                     String id = action.getContextID();
                     TCFNode node = getNode(id);
                     if (node instanceof TCFNodeExecContext) {
                         TCFDataCache<TCFContextState> state_cache = ((TCFNodeExecContext)node).getState();
                         if (!state_cache.validate(this)) return;
                         state_data = state_cache.getData();
                     }
                     if (active_actions.get(id) == action) {
                         active_actions.remove(id);
                         if (node instanceof TCFNodeExecContext) {
                             ((TCFNodeExecContext)node).onContextActionDone();
                             if (state_data != null && state_data.is_suspended) {
                                 String reason = action_results.get(id);
                                 if (reason == null) reason = state_data.suspend_reason;
                                 setDebugViewSelection(id2node.get(id), reason);
                             }
                         }
                         for (TCFModelProxy p : model_proxies) p.post();
                         updateAnnotations(null);
                         TCFNodePropertySource.refresh(node);
                     }
                     launch.removePendingClient(this);
                 }
             };
             launch.addPendingClient(r);
             Protocol.invokeLater(r);
         }
     };
 
     private final IDebugModelProvider debug_model_provider = new IDebugModelProvider() {
         public String[] getModelIdentifiers() {
             return new String[] { ITCFConstants.ID_TCF_DEBUG_MODEL };
         }
     };
 
     private class InitialSelection implements Runnable {
         final TCFModelProxy proxy;
         boolean done;
         InitialSelection(TCFModelProxy proxy) {
             this.proxy = proxy;
         }
         public void run() {
             if (done) return;
             if (launch_node != null) {
                 ArrayList<TCFNodeExecContext> nodes = new ArrayList<TCFNodeExecContext>();
                 if (!searchSuspendedThreads(launch_node.getFilteredChildren(), nodes)) return;
                 if (nodes.size() == 0) {
                     setDebugViewSelectionForProxy(proxy, launch_node, "Launch");
                 }
                 else if (nodes.size() == 1) {
                     TCFNodeExecContext n = nodes.get(0);
                     setDebugViewSelectionForProxy(proxy, n, "Launch");
                 }
                 else {
                     for (TCFNodeExecContext n : nodes) {
                         String reason = n.getState().getData().suspend_reason;
                         setDebugViewSelectionForProxy(proxy, n, reason);
                     }
                 }
             }
             done = true;
         }
         private boolean searchSuspendedThreads(TCFChildren c, ArrayList<TCFNodeExecContext> nodes) {
             if (!c.validate(this)) return false;
             for (TCFNode n : c.toArray()) {
                 if (!searchSuspendedThreads((TCFNodeExecContext)n, nodes)) return false;
             }
             return true;
         }
         private boolean searchSuspendedThreads(TCFNodeExecContext n, ArrayList<TCFNodeExecContext> nodes) {
             TCFDataCache<IRunControl.RunControlContext> run_context = n.getRunContext();
             if (!run_context.validate(this)) return false;
             IRunControl.RunControlContext ctx = run_context.getData();
             if (ctx != null && ctx.hasState()) {
                 TCFDataCache<TCFContextState> state = n.getState();
                 if (!state.validate(this)) return false;
                 TCFContextState s = state.getData();
                 if (s != null && s.is_suspended) nodes.add(n);
                 return true;
             }
             return searchSuspendedThreads(n.getChildren(), nodes);
         }
     }
 
     private volatile boolean instruction_stepping_enabled;
 
     TCFModel(final TCFLaunch launch) {
         this.launch = launch;
         display = PlatformUI.getWorkbench().getDisplay();
         selection_policy = new TCFModelSelectionPolicy(this);
         adapters.put(ILaunch.class, launch);
         adapters.put(IModelSelectionPolicy.class, selection_policy);
         adapters.put(IModelSelectionPolicyFactory.class, model_selection_factory);
         adapters.put(IDebugModelProvider.class, debug_model_provider);
         adapters.put(ISuspendHandler.class, new SuspendCommand(this));
         adapters.put(IResumeHandler.class, new ResumeCommand(this));
         adapters.put(BackResumeCommand.class, new BackResumeCommand(this));
         adapters.put(ITerminateHandler.class, new TerminateCommand(this));
         adapters.put(IDisconnectHandler.class, new DisconnectCommand(this));
         adapters.put(IStepIntoHandler.class, new StepIntoCommand(this));
         adapters.put(IStepOverHandler.class, new StepOverCommand(this));
         adapters.put(IStepReturnHandler.class, new StepReturnCommand(this));
         adapters.put(BackIntoCommand.class, new BackIntoCommand(this));
         adapters.put(BackOverCommand.class, new BackOverCommand(this));
         adapters.put(BackReturnCommand.class, new BackReturnCommand(this));
         adapters.put(IDropToFrameHandler.class, new DropToFrameCommand(this));
         expr_manager = DebugPlugin.getDefault().getExpressionManager();
         expr_manager.addExpressionListener(expressions_listener);
         annotation_manager = Activator.getAnnotationManager();
         launch.addActionsListener(actions_listener);
         final IPreferenceStore prefs = TCFPreferences.getPreferenceStore();
         IPropertyChangeListener listener = new IPropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent event) {
                 launch.setContextActionsInterval(prefs.getLong(TCFPreferences.PREF_MIN_STEP_INTERVAL));
                 min_view_updates_interval = prefs.getLong(TCFPreferences.PREF_MIN_UPDATE_INTERVAL);
                 view_updates_throttle_enabled = prefs.getBoolean(TCFPreferences.PREF_VIEW_UPDATES_THROTTLE);
                 channel_throttle_enabled = prefs.getBoolean(TCFPreferences.PREF_TARGET_TRAFFIC_THROTTLE);
                 wait_for_pc_update_after_step = prefs.getBoolean(TCFPreferences.PREF_WAIT_FOR_PC_UPDATE_AFTER_STEP);
                 wait_for_views_update_after_step = prefs.getBoolean(TCFPreferences.PREF_WAIT_FOR_VIEWS_UPDATE_AFTER_STEP);
                 delay_stack_update_until_last_step = prefs.getBoolean(TCFPreferences.PREF_DELAY_STACK_UPDATE_UNTIL_LAST_STEP);
                 stack_frames_limit_enabled = prefs.getBoolean(TCFPreferences.PREF_STACK_FRAME_LIMIT_ENABLED);
                 stack_frames_limit_value = prefs.getInt(TCFPreferences.PREF_STACK_FRAME_LIMIT_VALUE);
                 show_function_arg_names = prefs.getBoolean(TCFPreferences.PREF_STACK_FRAME_ARG_NAMES);
                 show_function_arg_values = prefs.getBoolean(TCFPreferences.PREF_STACK_FRAME_ARG_VALUES);
                 auto_children_list_updates = prefs.getBoolean(TCFPreferences.PREF_AUTO_CHILDREN_LIST_UPDATES);
                 delay_children_list_updates = prefs.getBoolean(TCFPreferences.PREF_DELAY_CHILDREN_LIST_UPDATES);
                 Protocol.invokeLater(new Runnable() {
                     public void run() {
                         for (TCFNode n : id2node.values()) {
                             if (n instanceof TCFNodeExecContext) {
                                 ((TCFNodeExecContext)n).onPreferencesChanged();
                             }
                         }
                     }
                 });
             }
         };
         listener.propertyChange(null);
         prefs.addPropertyChangeListener(listener);
         List<ITCFPresentationProvider> l = new ArrayList<ITCFPresentationProvider>();
         for (ITCFPresentationProvider p : TCFPresentationProvider.getPresentationProviders()) {
             try {
                 if (p.onModelCreated(this)) l.add(p);
             }
             catch (Throwable x) {
                 Activator.log("Unhandled exception in a presentation provider", x);
             }
         }
         view_request_listeners = l.size() > 0 ? l : null;
     }
 
     /**
      * Add an adapter for given type.
      *
      * @param adapterType  the type the adapter implements
      * @param adapter  the adapter implementing <code>adapterType</code>
      */
     public void setAdapter(Class<?> adapterType, Object adapter) {
         synchronized (adapters) {
             assert adapterType.isInstance(adapter);
             adapters.put(adapterType, adapter);
         }
     }
 
     @SuppressWarnings("rawtypes")
     public Object getAdapter(final Class adapter, final TCFNode node) {
         synchronized (adapters) {
             Object o = adapters.get(adapter);
             if (o != null) return o;
         }
         if (adapter == IMemoryBlockRetrieval.class || adapter == IMemoryBlockRetrievalExtension.class) {
             return new TCFTask<Object>() {
                 public void run() {
                     Object o = null;
                     TCFDataCache<TCFNodeExecContext> cache = searchMemoryContext(node);
                     if (cache != null) {
                         if (!cache.validate(this)) return;
                         if (cache.getData() != null) {
                             TCFNodeExecContext ctx = cache.getData();
                             if (!ctx.getMemoryContext().validate(this)) return;
                             if (ctx.getMemoryContext().getError() == null) {
                                 o = mem_retrieval.get(ctx.id);
                                 if (o == null) {
                                     TCFMemoryBlockRetrieval m = new TCFMemoryBlockRetrieval(ctx);
                                     mem_retrieval.put(ctx.id, m);
                                     o = m;
                                 }
                             }
                         }
                     }
                     assert o == null || adapter.isInstance(o);
                     done(o);
                 }
             }.getE();
         }
         return null;
     }
 
     void onConnected() {
         assert Protocol.isDispatchThread();
         assert launch_node == null;
         channel = launch.getChannel();
         launch_node = new TCFNodeLaunch(this);
         IMemory mem = launch.getService(IMemory.class);
         if (mem != null) mem.addListener(mem_listener);
         IRunControl run = launch.getService(IRunControl.class);
         if (run != null) run.addListener(run_listener);
         IMemoryMap mmap = launch.getService(IMemoryMap.class);
         if (mmap != null) mmap.addListener(mmap_listener);
         IPathMap pmap = launch.getService(IPathMap.class);
         if (pmap != null) pmap.addListener(pmap_listener);
         IRegisters reg = launch.getService(IRegisters.class);
         if (reg != null) reg.addListener(reg_listener);
         IProcesses prs = launch.getService(IProcesses.class);
         if (prs != null) prs.addListener(prs_listener);
         launchChanged();
         for (TCFModelProxy p : model_proxies) {
             String id = p.getPresentationContext().getId();
             if (IDebugUIConstants.ID_DEBUG_VIEW.equals(id)) {
                 Protocol.invokeLater(new InitialSelection(p));
             }
         }
     }
 
     void onDisconnected() {
         assert Protocol.isDispatchThread();
         if (locks.size() > 0) {
             TCFSnapshot[] arr = locks.values().toArray(new TCFSnapshot[locks.size()]);
             locks.clear();
             for (TCFSnapshot s : arr) s.dispose();
         }
         if (launch_node != null) {
             launch_node.dispose();
             launch_node = null;
         }
         refreshLaunchView();
         assert id2node.size() == 0;
     }
 
     void onProcessOutput(String process_id, final int stream_id, byte[] data) {
         if (process_id != null) {
             TCFConsole c = process_consoles.get(process_id);
             if (c == null) process_consoles.put(process_id,
                     c = new TCFConsole(this, TCFConsole.TYPE_PROCESS, process_id));
             c.write(stream_id, data);
         }
         else {
             if (dprintf_console == null) {
                 dprintf_console = new TCFConsole(TCFModel.this, TCFConsole.TYPE_DPRINTF, null);
             }
             dprintf_console.write(stream_id, data);
         }
     }
 
     void onProcessStreamError(String process_id, int stream_id, Exception x, int lost_size) {
         if (channel != null && channel.getState() == IChannel.STATE_CLOSED) return;
         StringBuffer bf = new StringBuffer();
         bf.append("Debugger console IO error");
         if (process_id != null) {
             bf.append(". Process ID ");
             bf.append(process_id);
         }
         bf.append(". Stream ");
         bf.append(stream_id);
         if (lost_size > 0) {
             bf.append(". Lost data size ");
             bf.append(lost_size);
         }
         Activator.log(bf.toString(), x);
     }
 
     void onMemoryChanged(String id, boolean notify_references, boolean context_suspended) {
         if (channel == null) return;
         if (notify_references) {
             for (Object obj : context_map.values()) {
                 if (obj instanceof IRunControl.RunControlContext) {
                     IRunControl.RunControlContext subctx = (IRunControl.RunControlContext)obj;
                     if (id.equals(subctx.getProcessID()) && !id.equals(subctx.getID())) {
                         TCFNode subnode = getNode(subctx.getID());
                         if (subnode instanceof TCFNodeExecContext) {
                             ((TCFNodeExecContext)subnode).onMemoryChanged(null, null);
                         }
                     }
                 }
             }
         }
         if (mem_retrieval.size() == 0) return;
         if (mem_blocks_update == null) {
             mem_blocks_update = new MemoryBlocksUpdate(channel);
             if (wait_for_views_update_after_step) {
                 launch.addPendingClient(mem_blocks_update);
             }
         }
         mem_blocks_update.add(id, context_suspended);
     }
 
     public TCFAction getActiveAction(String id) {
         return active_actions.get(id);
     }
 
     String getContextActionResult(String id) {
         return action_results.get(id);
     }
 
     public long getMinViewUpdatesInterval() {
         return min_view_updates_interval;
     }
 
     public boolean getViewUpdatesThrottleEnabled() {
         return view_updates_throttle_enabled;
     }
 
     public boolean getWaitForViewsUpdateAfterStep() {
         return wait_for_views_update_after_step;
     }
 
     public boolean getDelayStackUpdateUtilLastStep() {
         return delay_stack_update_until_last_step;
     }
 
     public boolean getChannelThrottleEnabled() {
         return channel_throttle_enabled;
     }
 
     public boolean getStackFramesLimitEnabled() {
         return stack_frames_limit_enabled;
     }
 
     public int getStackFramesLimitValue() {
         return stack_frames_limit_value;
     }
 
     public boolean getShowFunctionArgNames() {
         return show_function_arg_names;
     }
 
     public boolean getShowFunctionArgValues() {
         return show_function_arg_values;
     }
 
     public boolean getAutoChildrenListUpdates() {
         return auto_children_list_updates;
     }
 
     public boolean getDelayChildrenListUpdates() {
         return delay_children_list_updates;
     }
 
     void onProxyInstalled(TCFModelProxy mp) {
         IPresentationContext pc = mp.getPresentationContext();
         model_proxies.add(mp);
         if (launch_node != null && pc.getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) {
             Protocol.invokeLater(new InitialSelection(mp));
         }
     }
 
     void onProxyDisposed(TCFModelProxy mp) {
         model_proxies.remove(mp);
     }
 
     private void onContextRemoved(String[] context_ids) {
         for (String id : context_ids) {
             TCFNode node = getNode(id);
             if (node instanceof TCFNodeExecContext) {
                 ((TCFNodeExecContext)node).onContextRemoved();
                 for (TCFModelProxy p : model_proxies) {
                     p.saveExpandState(node);
                     p.clearAutoExpandStack(id);
                 }
             }
             action_results.remove(id);
             Object o = context_map.remove(id);
             if (o instanceof CreateNodeRunnable) ((CreateNodeRunnable)o).onContextRemoved();
             if (mem_blocks_update != null) mem_blocks_update.changeset.remove(id);
         }
 
         launch_node.onAnyContextAddedOrRemoved();
         // Close debug session if the last context is removed:
         onContextOrProcessRemoved();
         updateAnnotations(null);
     }
 
     void onContextRunning() {
         updateAnnotations(null);
     }
 
     Map<String,Object> getContextMap() {
         return context_map;
     }
 
     private void onContextOrProcessRemoved() {
         final int generation = ++auto_disconnect_generation;
         Protocol.invokeLater(1000, new Runnable() {
             public void run() {
                 if (generation != auto_disconnect_generation) return;
                 if (launch_node == null) return;
                 if (launch_node.isDisposed()) return;
                 TCFChildren children = launch_node.getFilteredChildren();
                 if (!children.validate(this)) return;
                 if (children.size() > 0) return;
                 launch.onLastContextRemoved();
             }
         });
     }
 
     void launchChanged() {
         if (launch_node != null) {
             for (TCFModelProxy p : model_proxies) {
                 String id = p.getPresentationContext().getId();
                 if (IDebugUIConstants.ID_DEBUG_VIEW.equals(id)) {
                     p.addDelta(launch_node, IModelDelta.STATE | IModelDelta.CONTENT);
                 }
             }
         }
         else {
             refreshLaunchView();
         }
     }
 
     public TCFModelProxy[] getModelProxies(IPresentationContext ctx) {
         TCFModelProxy[] proxies =  new TCFModelProxy[0];
         for (TCFModelProxy proxy : model_proxies) {
             if (ctx.equals(proxy.getPresentationContext())) {
                 TCFModelProxy[] old_proxies = proxies;
                 proxies = new TCFModelProxy[proxies.length + 1];
                 System.arraycopy(old_proxies, 0, proxies, 0, old_proxies.length);
                 proxies[proxies.length - 1] = proxy;
             }
         }
         return proxies;
     }
 
     public Collection<TCFModelProxy> getModelProxies() {
         return model_proxies;
     }
 
     void dispose() {
         if (view_request_listeners != null) {
             for (ITCFPresentationProvider p : view_request_listeners) {
                 try {
                     p.onModelDisposed(this);
                 }
                 catch (Throwable x) {
                     Activator.log("Unhandled exception in a presentation provider", x);
                 }
             }
         }
         launch.removeActionsListener(actions_listener);
         expr_manager.removeExpressionListener(expressions_listener);
         for (TCFConsole c : process_consoles.values()) c.close();
         for (TCFConsole c : debug_consoles) c.close();
         if (dprintf_console != null) dprintf_console.close();
     }
 
     void addNode(String id, TCFNode node) {
         assert id != null;
         assert Protocol.isDispatchThread();
         assert id2node.get(id) == null;
         assert !node.isDisposed();
         id2node.put(id, node);
     }
 
     void removeNode(String id) {
         assert id != null;
         assert Protocol.isDispatchThread();
         id2node.remove(id);
         mem_retrieval.remove(id);
     }
 
     void flushAllCaches() {
         ISourceLocator l = launch.getSourceLocator();
         if (l instanceof ISourceLookupDirector) {
             ISourceLookupDirector d = (ISourceLookupDirector)l;
             ISourceLookupParticipant[] participants = d.getParticipants();
             for (int i = 0; i < participants.length; i++) {
                 ISourceLookupParticipant participant = participants[i];
                 participant.sourceContainersChanged(d);
             }
         }
         for (TCFMemoryBlockRetrieval b : mem_retrieval.values()) b.flushAllCaches();
         for (TCFNode n : id2node.values()) n.flushAllCaches();
     }
 
     public IExpressionManager getExpressionManager() {
         return expr_manager;
     }
 
     public Display getDisplay() {
         return display;
     }
 
     /**
      * @return debug model launch object.
      */
     public TCFLaunch getLaunch() {
         return launch;
     }
 
     /**
      * @return communication channel that this model is using.
      */
     public IChannel getChannel() {
         return channel;
     }
 
     /**
      * Get top level (root) debug model node.
      * Same as getNode("").
      * @return root node.
      */
     public TCFNodeLaunch getRootNode() {
         return launch_node;
     }
 
     /**
      * Set current hover expression for a given model node,
      * and return a cache of expression nodes that represents given expression.
      * The model allows only one current hover expression per node at any time,
      * however it will cache results of recent expression evaluations,
      * and it will re-use cached results when current hover expression changes.
      * The cache getData() method should not return more then 1 node,
      * and it can return an empty collection.
      * @param parent - a thread or stack frame where the expression should be evaluated.
      * @param expression - the expression text, can be null.
      * @return a cache of expression nodes.
      */
     public TCFChildren getHoverExpressionCache(TCFNode parent, String expression) {
         assert Protocol.isDispatchThread();
         if (parent instanceof TCFNodeStackFrame) {
             return ((TCFNodeStackFrame)parent).getHoverExpressionCache(expression);
         }
         if (parent instanceof TCFNodeExecContext) {
             return ((TCFNodeExecContext)parent).getHoverExpressionCache(expression);
         }
         return null;
     }
 
     /**
      * Get a model node with given ID.
      * ID == "" means launch node.
      * @param id - node ID.
      * @return debug model node or null if no node exists with such ID.
      */
     public TCFNode getNode(String id) {
         if (id == null) return null;
         if (id.equals("")) return launch_node;
         assert Protocol.isDispatchThread();
         return id2node.get(id);
     }
 
     /**
      * Get a type that should be used to cast a value of an expression when it is shown in a view.
      * Return null if original type of the value should be used.
      * @param id - expression node ID.
      * @return a string that designates a type or null.
      */
     public String getCastToType(String id) {
         return cast_to_type_map.get(id);
     }
 
     /**
      * Register a type that should be used to cast a value of an expression when it is shown in a view.
      * 'type' == null means original type of the value should be used.
      * @param id - expression node ID.
      * @param type - a string that designates a type.
      */
     public void setCastToType(String id, String type) {
         if (type != null && type.trim().length() == 0) type = null;
         if (type == null) cast_to_type_map.remove(id);
         else cast_to_type_map.put(id, type);
         TCFNode node = id2node.get(id);
         if (node instanceof ICastToType) {
             ((ICastToType)node).onCastToTypeChanged();
         }
     }
 
     /**
      * Get a data cache that contains properties of a symbol.
      * New cache object is created if it does not exist yet.
      * @param sym_id - the symbol ID.
      * @return data cache object.
      */
     public TCFDataCache<ISymbols.Symbol> getSymbolInfoCache(final String sym_id) {
         if (sym_id == null) return null;
         TCFNodeSymbol n = (TCFNodeSymbol)getNode(sym_id);
         if (n == null) n = new TCFNodeSymbol(launch_node, sym_id);
         return n.getContext();
     }
 
     /**
      * Get a data cache that contains children of a symbol.
      * New cache object is created if it does not exist yet.
      * @param sym_id - the symbol ID.
      * @return data cache object.
      */
     public TCFDataCache<String[]> getSymbolChildrenCache(final String sym_id) {
         if (sym_id == null) return null;
         TCFNodeSymbol n = (TCFNodeSymbol)getNode(sym_id);
         if (n == null) n = new TCFNodeSymbol(launch_node, sym_id);
         return n.getChildren();
     }
 
     /**
      * Search memory context that owns the object represented by given node.
      * @return data cache item that holds the memory context node.
      */
     public TCFDataCache<TCFNodeExecContext> searchMemoryContext(final TCFNode node) {
         TCFNode n = node;
         while (n != null && !n.isDisposed()) {
             if (n instanceof TCFNodeExecContext) return ((TCFNodeExecContext)n).getMemoryNode();
             n = n.parent;
         }
         return null;
     }
 
     /**
      * Asynchronously create model node for given ID.
      * If 'done' is TCFDataCache and it is valid after the method returns,
      * the node cannot be created because of an error,
      * and the cache will contain the error report.
      * @param id - context ID.
      * @param done - an object waiting for cache validation.
      * @return - true if all done, false if 'done' is waiting for remote data.
      */
     public boolean createNode(String id, final Runnable done) {
         TCFNode parent = getNode(id);
         if (parent != null) return true;
         LinkedList<Object> path = null;
         for (;;) {
             Object obj = context_map.get(id);
             if (obj == null) obj = new CreateNodeRunnable(id);
             if (obj instanceof CreateNodeRunnable) {
                 ((CreateNodeRunnable)obj).wait(done);
                 return false;
             }
             if (obj instanceof Throwable) {
                 if (done instanceof TCFDataCache<?>) {
                     ((TCFDataCache<?>)done).set(null, (Throwable)obj, null);
                 }
                 return true;
             }
             if (path == null) path = new LinkedList<Object>();
             path.add(obj);
             String parent_id = null;
             if (obj instanceof IRunControl.RunControlContext) {
                 parent_id = ((IRunControl.RunControlContext)obj).getParentID();
             }
             else if (obj instanceof IStackTrace.StackTraceContext) {
                 parent_id = ((IStackTrace.StackTraceContext)obj).getParentID();
             }
             else {
                 parent_id = ((IRegisters.RegistersContext)obj).getParentID();
             }
             parent = parent_id == null ? launch_node : getNode(parent_id);
             if (parent != null) break;
             id = parent_id;
         }
         while (path.size() > 0) {
             Object obj = path.removeLast();
             if (obj instanceof IRunControl.RunControlContext) {
                 IRunControl.RunControlContext ctx = (IRunControl.RunControlContext)obj;
                 TCFNodeExecContext n = new TCFNodeExecContext(parent, ctx.getID());
                 if (parent instanceof TCFNodeLaunch) ((TCFNodeLaunch)parent).getChildren().add(n);
                 else ((TCFNodeExecContext)parent).getChildren().add(n);
                 n.setRunContext(ctx);
                 parent = n;
             }
             else if (obj instanceof IStackTrace.StackTraceContext) {
                 IStackTrace.StackTraceContext ctx = (IStackTrace.StackTraceContext)obj;
                 TCFNodeStackFrame n = new TCFNodeStackFrame((TCFNodeExecContext)parent, ctx.getID(), false);
                 ((TCFNodeExecContext)parent).getStackTrace().add(n);
                 parent = n;
             }
             else if (obj instanceof IRegisters.RegistersContext) {
                 IRegisters.RegistersContext ctx = (IRegisters.RegistersContext)obj;
                 TCFNodeRegister n = new TCFNodeRegister(parent, ctx.getID());
                 if (parent instanceof TCFNodeRegister) ((TCFNodeRegister)parent).getChildren().add(n);
                 else if (parent instanceof TCFNodeStackFrame) ((TCFNodeStackFrame)parent).getRegisters().add(n);
                 else ((TCFNodeExecContext)parent).getRegisters().add(n);
                 parent = n;
             }
             else {
                 assert false;
             }
         }
         return true;
     }
 
     private class CreateNodeRunnable implements Runnable {
 
         final String id;
         final ArrayList<Runnable> waiting_list = new ArrayList<Runnable>();
         final ArrayList<IService> service_list = new ArrayList<IService>();
 
         // TODO: context_map can accumulate "Invalid context ID" errors
         CreateNodeRunnable(String id) {
             this.id = id;
             assert context_map.get(id) == null;
             String[] arr = { IRunControl.NAME, IStackTrace.NAME, IRegisters.NAME };
             for (String nm : arr) {
                 IService s = channel.getRemoteService(nm);
                 if (s != null) service_list.add(s);
             }
             context_map.put(id, this);
             Protocol.invokeLater(this);
         }
 
         void wait(Runnable r) {
             assert context_map.get(id) == this;
            if (waiting_list.contains(r)) return;
             waiting_list.add(r);
         }
 
         void done(Object res) {
             assert res != null;
             assert res != this;
             context_map.put(id, res);
             for (Runnable r : waiting_list) {
                 if (createNode(id, r)) r.run();
             }
         }
 
         void onContextRemoved() {
             assert context_map.get(id) == null;
             done(new Exception("Invalid context ID"));
         }
 
         public void run() {
             assert context_map.get(id) == this;
             if (service_list.size() == 0) {
                 done(new Exception("Invalid context ID"));
             }
             else {
                 IService s = service_list.remove(0);
                 if (s instanceof IRunControl) {
                     ((IRunControl)s).getContext(id, new IRunControl.DoneGetContext() {
                         public void doneGetContext(IToken token, Exception error, IRunControl.RunControlContext context) {
                             if (error == null && context != null) done(context);
                             else run();
                         }
                     });
                 }
                 else if (s instanceof IStackTrace) {
                     ((IStackTrace)s).getContext(new String[]{ id }, new IStackTrace.DoneGetContext() {
                         public void doneGetContext(IToken token, Exception error, IStackTrace.StackTraceContext[] context) {
                             if (error == null && context != null && context.length == 1 && context[0] != null) done(context[0]);
                             else run();
                         }
                     });
                 }
                 else {
                     ((IRegisters)s).getContext(id, new IRegisters.DoneGetContext() {
                         public void doneGetContext(IToken token, Exception error, IRegisters.RegistersContext context) {
                             if (error == null && context != null) done(context);
                             else run();
                         }
                     });
                 }
             }
         }
     }
 
     public void update(IChildrenCountUpdate[] updates) {
         for (IChildrenCountUpdate update : updates) {
             Object o = update.getElement();
             if (o instanceof TCFLaunch) {
                 if (launch_node != null) {
                     launch_node.update(update);
                 }
                 else {
                     update.setChildCount(0);
                     update.done();
                 }
             }
             else {
                 ((TCFNode)o).update(update);
             }
         }
     }
 
     public void update(IChildrenUpdate[] updates) {
         for (IChildrenUpdate update : updates) {
             Object o = update.getElement();
             if (o instanceof TCFLaunch) {
                 if (launch_node != null) {
                     launch_node.update(update);
                 }
                 else {
                     update.done();
                 }
             }
             else {
                 ((TCFNode)o).update(update);
             }
         }
     }
 
     public void update(IHasChildrenUpdate[] updates) {
         for (IHasChildrenUpdate update : updates) {
             Object o = update.getElement();
             if (o instanceof TCFLaunch) {
                 if (launch_node != null) {
                     launch_node.update(update);
                 }
                 else {
                     update.setHasChilren(false);
                     update.done();
                 }
             }
             else {
                 ((TCFNode)o).update(update);
             }
         }
     }
 
     public void update(ILabelUpdate[] updates) {
         for (ILabelUpdate update : updates) {
             Object o = update.getElement();
             // Launch label is provided by TCFLaunchLabelProvider class.
             assert !(o instanceof TCFLaunch);
             ((TCFNode)o).update(update);
         }
     }
 
     public void update(final IViewerInputUpdate update) {
         Protocol.invokeLater(new Runnable() {
             public void run() {
                 TCFNode node = pins.get(update.getPresentationContext().getPart());
                 if (node != null) {
                     node.update(update);
                 }
                 else {
                     if (IDebugUIConstants.ID_BREAKPOINT_VIEW.equals(update.getPresentationContext().getId())) {
                         // Current implementation does not support flexible hierarchy for breakpoints
                         IViewerInputProvider p = (IViewerInputProvider)launch.getAdapter(IViewerInputProvider.class);
                         if (p != null) {
                             p.update(update);
                             return;
                         }
                     }
                     Object o = update.getElement();
                     if (o instanceof TCFLaunch) {
                         update.setInputElement(o);
                         update.done();
                     }
                     else {
                         ((TCFNode)o).update(update);
                     }
                 }
             }
         });
     }
 
     public void encodeElements(final IElementMementoRequest[] requests) {
         for (IElementMementoRequest request : requests) {
             Object element = request.getElement();
             if (element instanceof TCFNode) ((TCFNode)element).encodeElement(request);
         }
         display.asyncExec(new Runnable() {
             public void run() {
                 for (IElementMementoRequest request : requests) request.done();
             }
         });
     }
 
     public void compareElements(final IElementCompareRequest[] requests) {
         for (IElementCompareRequest request : requests) {
             Object element = request.getElement();
             if (element instanceof TCFNode) ((TCFNode)element).compareElements(request);
         }
         display.asyncExec(new Runnable() {
             public void run() {
                 for (IElementMementoRequest request : requests) request.done();
             }
         });
     }
 
     public IModelProxy createModelProxy(Object element, IPresentationContext context) {
         return new TCFModelProxy(this);
     }
 
     public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
         String id = getColumnPresentationId(context, element);
         if (id == null) return null;
         if (id.equals(TCFColumnPresentationRegister.PRESENTATION_ID)) return new TCFColumnPresentationRegister();
         if (id.equals(TCFColumnPresentationExpression.PRESENTATION_ID)) return new TCFColumnPresentationExpression();
         if (id.equals(TCFColumnPresentationModules.PRESENTATION_ID)) return new TCFColumnPresentationModules();
         return null;
     }
 
     public String getColumnPresentationId(IPresentationContext context, Object element) {
         if (IDebugUIConstants.ID_REGISTER_VIEW.equals(context.getId())) {
             return TCFColumnPresentationRegister.PRESENTATION_ID;
         }
         if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId())) {
             return TCFColumnPresentationExpression.PRESENTATION_ID;
         }
         if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(context.getId())) {
             return TCFColumnPresentationExpression.PRESENTATION_ID;
         }
         if (ID_EXPRESSION_HOVER.equals(context.getId())) {
             return TCFColumnPresentationExpression.PRESENTATION_ID;
         }
         if (IDebugUIConstants.ID_MODULE_VIEW.equals(context.getId())) {
             return TCFColumnPresentationModules.PRESENTATION_ID;
         }
         return null;
     }
 
     public void setPin(IWorkbenchPart part, TCFNode node) {
         assert Protocol.isDispatchThread();
         if (node == null) pins.remove(part);
         else pins.put(part, node);
     }
 
     private IPresentationContext getPresentationContext(IWorkbenchPart part) {
         if (part instanceof IDebugView) {
             Viewer viewer = ((IDebugView)part).getViewer();
             if (viewer instanceof ITreeModelViewer) {
                 ITreeModelViewer t = ((ITreeModelViewer)viewer);
                 return t.getPresentationContext();
             }
         }
         return null;
     }
 
     public void setLock(IWorkbenchPart part) {
         if (launch_node == null) return;
         IPresentationContext ctx = getPresentationContext(part);
         if (ctx == null) return;
         locks.put(part, new TCFSnapshot(ctx));
         for (TCFModelProxy proxy : model_proxies) {
             if (ctx.equals(proxy.getPresentationContext())) {
                 proxy.addDelta((TCFNode)proxy.getInput(), IModelDelta.CONTENT);
             }
         }
     }
 
     public boolean isLocked(IWorkbenchPart part) {
         return locks.get(part) != null;
     }
 
     public boolean clearLock(IWorkbenchPart part) {
         TCFSnapshot snapshot = locks.remove(part);
         if (snapshot == null) return false;
         snapshot.dispose();
         IPresentationContext ctx = getPresentationContext(part);
         if (ctx != null) {
             for (TCFModelProxy proxy : model_proxies) {
                 if (ctx.equals(proxy.getPresentationContext())) {
                     proxy.addDelta((TCFNode)proxy.getInput(), IModelDelta.CONTENT);
                 }
             }
         }
         return true;
     }
 
     /**
      * Set locking policy (aka update policy) for given workbench part.
      * @param part - the workbench part.
      * @param policy - locking policy code.
      */
     public void setLockPolicy(IWorkbenchPart part, int policy) {
         if (policy == UPDATE_POLICY_AUTOMATIC) {
             clearLock(part);
             lock_policy.remove(part);
         }
         else {
             if (!isLocked(part)) setLock(part);
             lock_policy.put(part, policy);
         }
     }
 
     /**
      * Return locking policy (aka update policy) for given workbench part.
      * @param part - the workbench part.
      * @return - locking policy code.
      */
     public int getLockPolicy(IWorkbenchPart part) {
         if (locks.get(part) == null) return UPDATE_POLICY_AUTOMATIC;
         Integer i = lock_policy.get(part);
         if (i == null || i.intValue() == 0) return UPDATE_POLICY_MANUAL;
         return i.intValue();
     }
 
     /**
      * Get a snapshot that is associated with given presentation context.
      * @param ctx - the presentation context.
      * @return - debug model snapshot, or null if the context is not locked.
      */
     TCFSnapshot getSnapshot(IPresentationContext ctx) {
         return locks.get(ctx.getPart());
     }
 
     /**
      * Handle debug view selection and suspend triggers when debug context is suspended.
      * @param node - model node that represents the debug context.
      * @param reason - suspend reason.
      */
     public void setDebugViewSelection(TCFNode node, String reason) {
         assert Protocol.isDispatchThread();
         if (node == null) return;
         if (node.isDisposed()) return;
         runSuspendTrigger(node);
         if (reason == null) return;
         for (TCFModelProxy proxy : model_proxies) {
             if (proxy.getPresentationContext().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) {
                 setDebugViewSelectionForProxy(proxy, node, reason);
             }
             if (reason.equals(IRunControl.REASON_BREAKPOINT)) {
                 IWorkbenchPart part = proxy.getPresentationContext().getPart();
                 int policy = getLockPolicy(part);
                 if (policy == UPDATE_POLICY_BREAKPOINT) {
                     clearLock(part);
                     setLock(part);
                 }
             }
         }
     }
 
     /**
      * Sets the selection in debug view for a given debug view instance.
      * @param proxy - model proxy for a given debug view instance
      * @param node - model node that represents the debug context.
      * @param reason - suspend reason.
      */
     public void setDebugViewSelectionForProxy(TCFModelProxy proxy, TCFNode node, String reason) {
         assert Protocol.isDispatchThread();
         if (!proxy.getPresentationContext().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) return;
         if (node == null) return;
         if (node.isDisposed()) return;
         if (reason == null) return;
 
         boolean user_request =
             reason.equals(IRunControl.REASON_USER_REQUEST) ||
             reason.equals(IRunControl.REASON_STEP) ||
             reason.equals(IRunControl.REASON_CONTAINER) ||
             delay_stack_update_until_last_step && launch.getContextActionsCount(node.id) != 0;
         if (proxy.getAutoExpandNode(node, user_request)) proxy.expand(node);
         if (reason.equals(IRunControl.REASON_USER_REQUEST)) return;
         proxy.setSelection(node);
     }
 
     /**
      * Update debugger annotations in a workbench window.
      * @param window - workbench window, null means update all windows.
      */
     public void updateAnnotations(IWorkbenchWindow window) {
         annotation_manager.updateAnnotations(window, launch);
     }
 
     private synchronized Object displaySourceStart(IWorkbenchPage page, boolean wait) {
         Object generation = new Object();
         if (wait) launch.addPendingClient(generation);
         if (page != null) {
             Object prev = display_source_generation.put(page, generation);
             if (prev != null) launch.removePendingClient(prev);
         }
         return generation;
     }
 
     private synchronized boolean displaySourceCheck(IWorkbenchPage page, Object generation) {
         return page == null || generation == display_source_generation.get(page);
     }
 
     private synchronized void displaySourceEnd(Object generation) {
         launch.removePendingClient(generation);
     }
 
     /**
      * Reveal source code associated with given model element.
      * The method is part of ISourceDisplay interface.
      * The method is normally called from SourceLookupService.
      */
     public void displaySource(Object model_element, final IWorkbenchPage page, boolean forceSourceLookup) {
         final Object generation = displaySourceStart(page, wait_for_pc_update_after_step);
         /* Because of racing in Eclipse Debug infrastructure, 'model_element' value can be invalid.
          * As a workaround, get current debug view selection.
          */
         if (page != null) {
             ISelection context = DebugUITools.getDebugContextManager().getContextService(page.getWorkbenchWindow()).getActiveContext();
             if (context instanceof IStructuredSelection) {
                 IStructuredSelection selection = (IStructuredSelection)context;
                 model_element = selection.isEmpty() ? null : selection.getFirstElement();
             }
         }
         final Object element = model_element;
         Protocol.invokeLater(25, new Runnable() {
             public void run() {
                 if (!displaySourceCheck(page, generation)) return;
                 TCFNodeStackFrame stack_frame = null;
                 if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                     if (element instanceof TCFNodeExecContext) {
                         TCFNodeExecContext exec_ctx = (TCFNodeExecContext)element;
                         if (!exec_ctx.isDisposed() && active_actions.get(exec_ctx.id) == null) {
                             TCFDataCache<TCFContextState> state_cache = exec_ctx.getState();
                             if (!state_cache.validate(this)) return;
                             if (!exec_ctx.isNotActive()) {
                                 TCFContextState state_data = state_cache.getData();
                                 if (state_data != null && state_data.is_suspended) {
                                     TCFChildrenStackTrace stack_trace = exec_ctx.getStackTrace();
                                     if (!stack_trace.validate(this)) return;
                                     stack_frame = stack_trace.getTopFrame();
                                 }
                             }
                         }
                     }
                     else if (element instanceof TCFNodeStackFrame) {
                         TCFNodeStackFrame f = (TCFNodeStackFrame)element;
                         TCFNodeExecContext exec_ctx = (TCFNodeExecContext)f.parent;
                         if (!f.isDisposed() && !exec_ctx.isDisposed() && active_actions.get(exec_ctx.id) == null) {
                             TCFDataCache<TCFContextState> state_cache = exec_ctx.getState();
                             if (!state_cache.validate(this)) return;
                             if (!exec_ctx.isNotActive()) {
                                 TCFContextState state_data = state_cache.getData();
                                 if (state_data != null && state_data.is_suspended) stack_frame = f;
                             }
                         }
                     }
                 }
                 String ctx_id = null;
                 String mem_id = null;
                 boolean top_frame = false;
                 ILineNumbers.CodeArea area = null;
                 if (stack_frame != null) {
                     TCFDataCache<TCFSourceRef> line_info = stack_frame.getLineInfo();
                     if (!line_info.validate(this)) return;
                     Throwable error = line_info.getError();
                     TCFSourceRef src_ref = line_info.getData();
                     if (error == null && src_ref != null) error = src_ref.error;
                     if (error != null) Activator.log("Error retrieving source mapping for a stack frame", error);
                     if (src_ref != null) {
                         mem_id = src_ref.context_id;
                         area = src_ref.area;
                     }
                     top_frame = stack_frame.getFrameNo() == 0;
                     ctx_id = stack_frame.parent.id;
                 }
                 displaySource(generation, page, element, ctx_id, mem_id, top_frame, area);
             }
         });
     }
 
     /**
      * Open source text editor.
      * The editor is shown in currently active Eclipse window.
      * Source file name is translated using source lookup settings of the debugger.
      * "Source not found" window is shown if the file cannot be located.
      * This method should be called on the display thread.
      * @param context_id - debug context ID.
      * @param source_file_name - compile-time source file name.
      * @param line - scroll the editor to reveal this line.
      * @return - text editor interface.
      */
     public ITextEditor displaySource(final String context_id, String source_file_name, int line) {
         if (PlatformUI.getWorkbench().isClosing()) return null;
         IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
         if (window == null) return null;
         String editor_id = null;
         IEditorInput editor_input = null;
         Object source_element = TCFSourceLookupDirector.lookup(launch, context_id, source_file_name);
         if (source_element != null) {
             ISourcePresentation presentation = TCFModelPresentation.getDefault();
             editor_input = presentation.getEditorInput(source_element);
             if (editor_input != null) editor_id = presentation.getEditorId(editor_input, source_element);
         }
         if (editor_input == null || editor_id == null) {
             ILaunchConfiguration cfg = launch.getLaunchConfiguration();
             TCFNode node = new TCFTask<TCFNode>(channel) {
                 public void run() {
                     if (!createNode(context_id, this)) return;
                     done(getNode(context_id));
                 }
             }.getE();
             if (node != null) {
                 ISourceNotFoundPresentation presentation = (ISourceNotFoundPresentation)DebugPlugin.getAdapter(node, ISourceNotFoundPresentation.class);
                 if (presentation != null) {
                     editor_input = presentation.getEditorInput(node, cfg, source_file_name);
                     editor_id = presentation.getEditorId(editor_input, node);
                 }
             }
             if (editor_id == null || editor_input == null) {
                 editor_id = IDebugUIConstants.ID_COMMON_SOURCE_NOT_FOUND_EDITOR;
                 editor_input = editor_not_found.get(cfg);
                 if (editor_input == null) {
                     editor_input = new CommonSourceNotFoundEditorInput(cfg);
                     editor_not_found.put(cfg, editor_input);
                 }
             }
         }
         IWorkbenchPage page = window.getActivePage();
         return displaySource(page, editor_id, editor_input, line);
     }
 
     private void displaySource(final Object generation, final IWorkbenchPage page,
             final Object element, final String exe_id, final String mem_id, final boolean top_frame, final ILineNumbers.CodeArea area) {
         final boolean disassembly_available = channel.getRemoteService(IDisassembly.class) != null;
         display.asyncExec(new Runnable() {
             public void run() {
                 try {
                     if (!displaySourceCheck(page, generation)) return;
                     String editor_id = null;
                     IEditorInput editor_input = null;
                     int line = 0;
                     if (area != null) {
                         Object source_element = TCFSourceLookupDirector.lookup(launch, mem_id, area);
                         if (source_element != null) {
                             ISourcePresentation presentation = TCFModelPresentation.getDefault();
                             editor_input = presentation.getEditorInput(source_element);
                             if (editor_input != null) editor_id = presentation.getEditorId(editor_input, source_element);
                             line = area.start_line;
                         }
                     }
                     if (area != null && !instruction_stepping_enabled && (editor_input == null || editor_id == null)) {
                         ILaunchConfiguration cfg = launch.getLaunchConfiguration();
                         ISourceNotFoundPresentation presentation = (ISourceNotFoundPresentation)DebugPlugin.getAdapter(element, ISourceNotFoundPresentation.class);
                         if (presentation != null) {
                             String filename = TCFSourceLookupParticipant.toFileName(area);
                             editor_input = presentation.getEditorInput(element, cfg, filename);
                             editor_id = presentation.getEditorId(editor_input, element);
                         }
                         if (editor_id == null || editor_input == null) {
                             editor_id = IDebugUIConstants.ID_COMMON_SOURCE_NOT_FOUND_EDITOR;
                             editor_input = editor_not_found.get(cfg);
                             if (editor_input == null) {
                                 editor_input = new CommonSourceNotFoundEditorInput(cfg);
                                 editor_not_found.put(cfg, editor_input);
                             }
                         }
                     }
                     if (exe_id != null && disassembly_available &&
                             (editor_input == null || editor_id == null || instruction_stepping_enabled) &&
                             PlatformUI.getWorkbench().getEditorRegistry().findEditor(
                                     DisassemblyEditorInput.EDITOR_ID) != null) {
                         editor_id = DisassemblyEditorInput.EDITOR_ID;
                         editor_input = DisassemblyEditorInput.INSTANCE;
                     }
                     if (!displaySourceCheck(page, generation)) return;
                     displaySource(page, editor_id, editor_input, line);
                     if (wait_for_pc_update_after_step) launch.addPendingClient(annotation_manager);
                     updateAnnotations(page.getWorkbenchWindow());
                 }
                 finally {
                     displaySourceEnd(generation);
                 }
             }
         });
     }
 
     /*
      * Open an editor for given editor input and scroll it to reveal given line.
      * @param page - workbench page that will contain the editor
      * @param editor_id - editor type ID
      * @param editor_input - IEditorInput representing a source file to be shown in the editor
      * @param line - scroll the editor to reveal this line.
      * @return - IEditorPart if the editor was opened successfully, or null otherwise.
      */
     private ITextEditor displaySource(IWorkbenchPage page, String editor_id, IEditorInput editor_input, int line) {
         ITextEditor text_editor = null;
         if (page != null && editor_input != null && editor_id != null) {
             IEditorPart editor = openEditor(editor_input, editor_id, page);
             if (editor instanceof ITextEditor) {
                 text_editor = (ITextEditor)editor;
             }
             else if (editor != null) {
                 text_editor = (ITextEditor)editor.getAdapter(ITextEditor.class);
             }
         }
         IRegion region = null;
         if (text_editor != null) {
             region = getLineInformation(text_editor, line);
             if (region != null) text_editor.selectAndReveal(region.getOffset(), 0);
         }
         return text_editor;
     }
 
     /*
      * Refresh source view when memory or path mappings change.
      */
     private void refreshSourceView() {
         synchronized (Device.class) {
             if (display.isDisposed()) return;
             display.asyncExec(new Runnable() {
                 public void run() {
                     if (!PlatformUI.isWorkbenchRunning()) return;
                     IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
                     if (windows == null) return;
                     for (IWorkbenchWindow window : windows) {
                         IWorkbenchPage page = window.getActivePage();
                         if (page != null) displaySource(null, page, true);
                     }
                 }
             });
         }
     }
 
     /*
      * Refresh Launch View.
      * Normally the view is updated by sending deltas through model proxy.
      * This method is used only when launch is not yet connected or already disconnected.
      */
     private void refreshLaunchView() {
         // TODO: there should be a better way to refresh Launch View
         synchronized (Device.class) {
             if (display.isDisposed()) return;
             display.asyncExec(new Runnable() {
                 public void run() {
                     if (!PlatformUI.isWorkbenchRunning()) return;
                     IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
                     if (windows == null) return;
                     for (IWorkbenchWindow window : windows) {
                         IWorkbenchPage page = window.getActivePage();
                         if (page != null) {
                             IDebugView view = (IDebugView)page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
                             if (view != null) ((StructuredViewer)view.getViewer()).refresh(launch);
                         }
                     }
                 }
             });
         }
     }
 
     /**
      * Open debugger console that provide command line UI for the debugger.
      */
     public void showDebugConsole() {
         debug_consoles.add(new TCFConsole(this, TCFConsole.TYPE_CMD_LINE, null));
     }
 
     /**
      * Show error message box in active workbench window.
      * @param title - message box title.
      * @param error - error to be shown.
      */
     public void showMessageBox(final String title, final Throwable error) {
         display.asyncExec(new Runnable() {
             public void run() {
                 Shell shell = display.getActiveShell();
                 if (shell == null) {
                     Shell[] shells = display.getShells();
                     HashSet<Shell> set = new HashSet<Shell>();
                     for (Shell s : shells) set.add(s);
                     for (Shell s : shells) {
                         if (s.getParent() != null) set.remove(s.getParent().getShell());
                     }
                     for (Shell s : shells) shell = s;
                 }
                 MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                 mb.setText(title);
                 mb.setMessage(getErrorMessage(error, true));
                 mb.open();
             }
         });
     }
 
     /**
      * Create human readable error message from a Throwable object.
      * @param error - a Throwable object.
      * @param multiline - true if multi-line text is allowed.
      * @return error message text.
      */
     public static String getErrorMessage(Throwable error, boolean multiline) {
         StringBuffer buf = new StringBuffer();
         while (error != null) {
             String msg = null;
             if (!multiline && error instanceof IErrorReport) {
                 msg = Command.toErrorString(((IErrorReport)error).getAttributes());
             }
             else {
                 msg = error.getLocalizedMessage();
             }
             if (msg == null || msg.length() == 0) msg = error.getClass().getName();
             buf.append(msg);
             error = error.getCause();
             if (error != null) {
                 char ch = buf.charAt(buf.length() - 1);
                 if (multiline && ch != '\n') {
                     buf.append('\n');
                 }
                 else if (ch != '.' && ch != ';') {
                     buf.append(';');
                 }
                 buf.append("Caused by:");
                 buf.append(multiline ? '\n' : ' ');
             }
         }
         if (buf.length() > 0) {
             char ch = buf.charAt(buf.length() - 1);
             if (multiline && ch != '\n') {
                 buf.append('\n');
             }
         }
         return buf.toString();
     }
 
     /*
      * Open an editor for given editor input.
      * @param input - IEditorInput representing a source file to be shown in the editor
      * @param id - editor type ID
      * @param page - workbench page that will contain the editor
      * @return - IEditorPart if the editor was opened successfully, or null otherwise.
      */
     private IEditorPart openEditor(final IEditorInput input, final String id, final IWorkbenchPage page) {
         final IEditorPart[] editor = new IEditorPart[]{ null };
         Runnable r = new Runnable() {
             public void run() {
                 if (!page.getWorkbenchWindow().getWorkbench().isClosing()) {
                     try {
                         editor[0] = page.openEditor(input, id, false, IWorkbenchPage.MATCH_ID|IWorkbenchPage.MATCH_INPUT);
                     }
                     catch (PartInitException e) {
                         Activator.log("Cannot open editor", e);
                     }
                 }
             }
         };
         BusyIndicator.showWhile(display, r);
         return editor[0];
     }
 
     /*
      * Returns the line information for the given line in the given editor
      */
     private IRegion getLineInformation(ITextEditor editor, int line) {
         IDocumentProvider provider = editor.getDocumentProvider();
         IEditorInput input = editor.getEditorInput();
         try {
             provider.connect(input);
         }
         catch (CoreException e) {
             return null;
         }
         try {
             IDocument document = provider.getDocument(input);
             if (document != null) return document.getLineInformation(line - 1);
         }
         catch (BadLocationException e) {
         }
         finally {
             provider.disconnect(input);
         }
         return null;
     }
 
     /**
      * Registers the given listener for suspend notifications.
      * @param listener suspend listener
      */
     public synchronized void addSuspendTriggerListener(ISuspendTriggerListener listener) {
         suspend_trigger_listeners.add(listener);
     }
 
     /**
      * Unregisters the given listener for suspend notifications.
      * @param listener suspend listener
      */
     public synchronized void removeSuspendTriggerListener(ISuspendTriggerListener listener) {
         suspend_trigger_listeners.remove(listener);
     }
 
     /*
      * Lazily run registered suspend listeners.
      * @param node - suspended context.
      */
     private synchronized void runSuspendTrigger(final TCFNode node) {
         if (suspend_trigger_listeners.size() == 0) return;
         final ISuspendTriggerListener[] listeners = suspend_trigger_listeners.toArray(
                 new ISuspendTriggerListener[suspend_trigger_listeners.size()]);
 
         final int generation = ++suspend_trigger_generation;
         if (wait_for_views_update_after_step) {
             launch.addPendingClient(suspend_trigger_listeners);
         }
         display.asyncExec(new Runnable() {
             public void run() {
                 synchronized (TCFModel.this) {
                     if (generation != suspend_trigger_generation) return;
                 }
                 for (final ISuspendTriggerListener listener : listeners) {
                     try {
                         listener.suspended(launch, node);
                     }
                     catch (Throwable x) {
                         Activator.log(x);
                     }
                 }
                 synchronized (TCFModel.this) {
                     if (generation != suspend_trigger_generation) return;
                     launch.removePendingClient(suspend_trigger_listeners);
                 }
             }
         });
     }
 
     /**
      * Set whether instruction stepping mode should be enabled or not.
      * @param enabled
      */
     public void setInstructionSteppingEnabled(boolean enabled) {
         instruction_stepping_enabled = enabled;
     }
 
     /**
      * @return whether instruction stepping is enabled
      */
     public boolean isInstructionSteppingEnabled() {
         return instruction_stepping_enabled;
     }
 
     /**
      * Set whether reverse debugging should be enabled or not.
      * @param enabled
      */
     public void setReverseDebugEnabled(boolean enabled) {
         reverse_debug_enabled = enabled;
     }
 
     /**
      * @return whether reverse debugging is enabled.
      */
     public boolean isReverseDebugEnabled() {
         return reverse_debug_enabled;
     }
 }
