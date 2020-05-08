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
 package org.eclipse.tcf.internal.debug.model;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.Launch;
 import org.eclipse.tcf.internal.debug.Activator;
 import org.eclipse.tcf.internal.debug.actions.TCFAction;
 import org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.IService;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IContextQuery;
 import org.eclipse.tcf.services.IDPrintf;
 import org.eclipse.tcf.services.IFileSystem;
 import org.eclipse.tcf.services.IFileSystem.FileSystemException;
 import org.eclipse.tcf.services.IFileSystem.IFileHandle;
 import org.eclipse.tcf.services.IMemory;
 import org.eclipse.tcf.services.IMemory.MemoryContext;
 import org.eclipse.tcf.services.IMemoryMap;
 import org.eclipse.tcf.services.IPathMap;
 import org.eclipse.tcf.services.IProcesses;
 import org.eclipse.tcf.services.IProcesses.ProcessContext;
 import org.eclipse.tcf.services.IProcessesV1;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.IRunControl.RunControlContext;
 import org.eclipse.tcf.services.IStreams;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.tcf.util.TCFTask;
 
 /**
  * TCFLaunch class represents an active TCF debug connection.
  * The class handles initialization and synchronization of Memory Map and Path Map services,
  * supports downloading and starting a remote process, maintains breakpoint status information, etc.
  */
 public class TCFLaunch extends Launch {
 
     /**
      * Clients can use LaunchListener interface for notifications of launch being created, connected or
      * disconnected. The interface also allows to receive remote process output.
      */
     public interface LaunchListener {
 
         public void onCreated(TCFLaunch launch);
 
         public void onConnected(TCFLaunch launch);
 
         public void onDisconnected(TCFLaunch launch);
 
         public void onProcessOutput(TCFLaunch launch, String process_id, int stream_id, byte[] data);
 
         public void onProcessStreamError(
                 TCFLaunch launch, String process_id, int stream_id,
                 Exception error, int lost_size);
     }
 
     /**
      * Launch object handles queue of user actions, like debugger stepping commands.
      * ActionsListener allows clients to be notified when an action execution is started and finished.
      */
     public interface ActionsListener {
 
         public void onContextActionStart(TCFAction action);
 
         public void onContextActionResult(String id, String result);
 
         public void onContextActionDone(TCFAction action);
     }
 
     private abstract class LaunchStep implements Runnable {
 
         LaunchStep() {
             launch_steps.add(this);
         }
 
         abstract void start() throws Exception;
 
         void done() {
             if (channel.getState() != IChannel.STATE_OPEN) return;
             try {
                 launch_steps.removeFirst().start();
             }
             catch (Throwable x) {
                 channel.terminate(x);
             }
         }
 
         public void run() {
             done();
         }
     }
 
     private static final Collection<LaunchListener> listeners = new ArrayList<LaunchListener>();
     private static LaunchListener[] listeners_array;
 
     private final Collection<ActionsListener> action_listeners = new ArrayList<ActionsListener>();
 
     private TCFTask<Boolean> launch_task;
     private IProgressMonitor launch_monitor;
 
     private IChannel channel;
     private Throwable error;
     private TCFBreakpointsStatus breakpoints_status;
     private String mode;
     private boolean connecting;
     private boolean disconnecting;
     private boolean disconnected;
     private boolean shutdown;
     private boolean last_context_exited;
     private long actions_interval;
 
     private final LinkedList<TCFTask<Boolean>> disconnect_wait_list = new LinkedList<TCFTask<Boolean>>();
 
     private final HashSet<Object> pending_clients = new HashSet<Object>();
     private long pending_clients_timestamp;
 
     private String peer_name;
 
     private Runnable update_memory_maps;
 
     private ProcessContext process;
     private Collection<Map<String,Object>> process_signals;
     private IToken process_start_command;
     private String process_input_stream_id;
     private boolean process_exited;
     private int process_exit_code;
     private final HashMap<String,String> process_env = new HashMap<String,String>();
 
     private final HashMap<String,TCFAction> active_actions = new HashMap<String,TCFAction>();
     private final HashMap<String,LinkedList<TCFAction>> context_action_queue = new HashMap<String,LinkedList<TCFAction>>();
     private final HashMap<String,Long> context_action_timestamps = new HashMap<String,Long>();
     private final HashMap<String,String> process_stream_ids = new HashMap<String,String>();
     private final HashMap<String,String> uart_tx_stream_ids = new HashMap<String,String>();
     private final HashMap<String,String> uart_rx_stream_ids = new HashMap<String,String>();
     private final HashSet<String> disconnected_stream_ids = new HashSet<String>();
     private final LinkedList<LaunchStep> launch_steps = new LinkedList<LaunchStep>();
     private final LinkedList<String> redirection_path = new LinkedList<String>();
 
     private List<IPathMap.PathMapRule> host_path_map;
     private TCFDataCache<IPathMap.PathMapRule[]> target_path_map;
 
     private HashMap<String,IStorage> target_path_mapping_cache = new HashMap<String,IStorage>();
 
     private final HashMap<String,TCFDataCache<String[]>> context_query_cache = new HashMap<String,TCFDataCache<String[]>>();
 
     private Set<String> context_filter;
 
     private boolean supports_memory_map_preloading;
 
     private String dprintf_stream_id;
 
     private final IStreams.StreamsListener streams_listener = new IStreams.StreamsListener() {
 
         public void created(String stream_type, String stream_id, String context_id) {
             disconnected_stream_ids.remove(stream_id);
             if (stream_type.equals("UART-TX")) {
                 uart_tx_stream_ids.put(stream_id, context_id);
                 readStream(context_id, stream_id, 0);
             }
             else if (stream_type.equals("UART-RX")) {
                 uart_rx_stream_ids.put(stream_id, context_id);
             }
             else {
                 process_stream_ids.put(stream_id, context_id);
                 if (process_start_command == null) {
                     disconnectStream(stream_id);
                 }
             }
         }
 
         public void disposed(String stream_type, String stream_id) {
             disconnected_stream_ids.add(stream_id);
         }
     };
 
     private final IProcesses.ProcessesListener prs_listener = new IProcesses.ProcessesListener() {
 
         public void exited(String process_id, int exit_code) {
             if (process_id.equals(process.getID())) {
                 process_exit_code = exit_code;
                 process_exited = true;
             }
         }
     };
 
     private final IRunControl.RunControlListener rc_listener = new IRunControl.RunControlListener() {
 
         private void flushContextQueryCache() {
             for (TCFDataCache<?> c : context_query_cache.values()) c.reset();
         }
 
         public void contextAdded(RunControlContext[] contexts) {
             flushContextQueryCache();
         }
 
         public void contextChanged(RunControlContext[] contexts) {
             flushContextQueryCache();
         }
 
         public void contextRemoved(String[] context_ids) {
             flushContextQueryCache();
         }
 
         public void contextSuspended(String context, String pc, String reason, Map<String, Object> params) {
         }
 
         public void contextResumed(String context) {
         }
 
         public void containerSuspended(String context, String pc, String reason, Map<String, Object> params, String[] suspended_ids) {
         }
 
         public void containerResumed(String[] context_ids) {
         }
 
         public void contextException(String context, String msg) {
         }
     };
 
     private static LaunchListener[] getListeners() {
         if (listeners_array != null) return listeners_array;
         return listeners_array = listeners.toArray(new LaunchListener[listeners.size()]);
     }
 
     public TCFLaunch(ILaunchConfiguration launchConfiguration, String mode) {
         super(launchConfiguration, mode, null);
         for (LaunchListener l : getListeners()) l.onCreated(TCFLaunch.this);
     }
 
     private void onConnected() throws Exception {
         // The method is called when TCF channel is successfully connected.
 
         final IRunControl rc_service = getService(IRunControl.class);
         if (rc_service != null) {
             rc_service.addListener(rc_listener);
         }
 
         final IPathMap path_map_service = getService(IPathMap.class);
         if (path_map_service != null) {
             target_path_map = new TCFDataCache<IPathMap.PathMapRule[]>(channel) {
                 @Override
                 protected boolean startDataRetrieval() {
                     command = path_map_service.get(new IPathMap.DoneGet() {
                         public void doneGet(IToken token, Exception error, IPathMap.PathMapRule[] map) {
                             set(token, error, map);
                         }
                     });
                     return false;
                 }
             };
             path_map_service.addListener(new IPathMap.PathMapListener() {
                 public void changed() {
                     target_path_map.reset();
                     target_path_mapping_cache = new HashMap<String,IStorage>();
                 }
             });
         }
 
         final ILaunchConfiguration cfg = getLaunchConfiguration();
         if (cfg != null) {
             // Send file path map:
             if (getService(IPathMap.class) != null) {
                 new LaunchStep() {
                     @Override
                     void start() throws Exception {
                         downloadPathMaps(cfg, this);
                     }
                 };
             }
         }
 
         if (redirection_path.size() > 0) {
             // Connected to intermediate peer (value-add).
             // Redirect to next peer:
             new LaunchStep() {
                 @Override
                 void start() throws Exception {
                     String id = redirection_path.removeFirst();
                     IPeer p = Protocol.getLocator().getPeers().get(id);
                     if (p != null) channel.redirect(p.getAttributes());
                     else channel.redirect(id);
                     if (launch_monitor != null) {
                         String name = null;
                         if (p != null) name = p.getName();
                         if (name == null) name = id;
                         launch_monitor.subTask("Connecting to " + name);
                     }
                 }
             };
         }
         else {
             final IStreams streams = getService(IStreams.class);
             if (streams != null) {
                 // Subscribe Streams service:
                 new LaunchStep() {
                     @Override
                     void start() {
                         final Set<IToken> cmds = new HashSet<IToken>();
                         String[] nms = { IProcesses.NAME, IProcessesV1.NAME, "UART-RX", "UART-TX" };
                         for (String s : nms) {
                             cmds.add(streams.subscribe(s, streams_listener, new IStreams.DoneSubscribe() {
                                 public void doneSubscribe(IToken token, Exception error) {
                                     cmds.remove(token);
                                     if (error != null) channel.terminate(error);
                                     if (cmds.size() == 0) done();
                                 }
                             }));
                         }
                         if (cmds.size() == 0) done();
                     }
                 };
             }
 
             if (mode.equals(ILaunchManager.DEBUG_MODE)) {
                 String attach_to_context = getAttribute("attach_to_context");
                 if (attach_to_context != null) {
                     context_filter = new HashSet<String>();
                     context_filter.add(attach_to_context);
                 }
                 final IMemoryMap mem_map = channel.getRemoteService(IMemoryMap.class);
                 if (mem_map != null) {
                     // Send manual memory map items:
                     new LaunchStep() {
                         @Override
                         void start() throws Exception {
                             final Runnable done = this;
                             // Check if preloading is supported
                             mem_map.set("\001", null, new IMemoryMap.DoneSet() {
                                 public void doneSet(IToken token, Exception error) {
                                     try {
                                         supports_memory_map_preloading = error == null;
                                         if (!supports_memory_map_preloading) {
                                             // Older agents (up to ver. 0.4) don't support preloading of memory maps.
                                             updateMemoryMapsOnProcessCreation(cfg, done);
                                         }
                                         else {
                                             downloadMemoryMaps(cfg, done);
                                         }
                                     }
                                     catch (Exception x) {
                                         channel.terminate(x);
                                     }
                                 }
                             });
                         }
                     };
                 }
                 // Send breakpoints:
                 new LaunchStep() {
                     @Override
                     void start() throws Exception {
                         breakpoints_status = new TCFBreakpointsStatus(TCFLaunch.this);
                         Activator.getBreakpointsModel().downloadBreakpoints(channel, this);
                     }
                 };
                 final IDPrintf dprintf = getService(IDPrintf.class);
                 if (dprintf != null) {
                     // Open dprintf stream:
                     new LaunchStep() {
                         @Override
                         void start() throws Exception {
                             dprintf.open(null, new IDPrintf.DoneCommandOpen() {
                                 @Override
                                 public void doneCommandOpen(IToken token, Exception error, final String id) {
                                     if (error != null) {
                                         channel.terminate(error);
                                         return;
                                     }
                                     dprintf_stream_id = id;
                                     streams.connect(id, new IStreams.DoneConnect() {
                                         @Override
                                         public void doneConnect(IToken token, Exception error) {
                                             if (error != null) {
                                                 channel.terminate(error);
                                                 return;
                                             }
                                             readStream(null, id, 0);
                                             done();
                                         }
                                     });
                                 }
                             });
                         }
                     };
                 }
             }
 
             if (cfg != null && getService(IMemory.class) != null) {
                 String s = cfg.getAttribute(TCFLaunchDelegate.ATTR_FILES, (String)null);
                 if (s != null) {
                     @SuppressWarnings("unchecked")
                     Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)JSON.parseOne(s.getBytes("UTF-8"));
                     final ElfLoader loader = new ElfLoader(channel);
                     for (final Map<String,Object> m : c) {
                         Boolean b1 = (Boolean)m.get(TCFLaunchDelegate.FILES_DOWNLOAD);
                         Boolean b2 = (Boolean)m.get(TCFLaunchDelegate.FILES_SET_PC);
                         if (b1 != null && b1.booleanValue() || b2 != null && b2.booleanValue()) {
                             new LaunchStep() {
                                 @Override
                                 void start() throws Exception {
                                     loader.load(m, this);
                                 }
                             };
                         }
                     }
                     new LaunchStep() {
                         @Override
                         void start() throws Exception {
                             loader.dispose();
                             done();
                         }
                     };
                 }
             }
 
             // Call client launch sequence:
             new LaunchStep() {
                 @Override
                 void start() {
                     runLaunchSequence(this);
                 }
             };
 
             if (cfg != null) startRemoteProcess(cfg);
 
             // Final launch step.
             // Notify clients:
             new LaunchStep() {
                 @Override
                 void start() {
                     connecting = false;
                     for (LaunchListener l : getListeners()) l.onConnected(TCFLaunch.this);
                     fireChanged();
                     if (launch_task != null) launch_task.done(true);
                     launch_monitor = null;
                     launch_task = null;
                 }
             };
         }
 
         launch_steps.removeFirst().start();
     }
 
     private void onDisconnected(Throwable error) {
         // The method is called when TCF channel is closed.
         assert !disconnected;
         assert !shutdown;
         this.error = error;
         breakpoints_status = null;
         connecting = false;
         disconnected = true;
         for (LaunchListener l : getListeners()) l.onDisconnected(this);
         for (TCFDataCache<?> c : context_query_cache.values()) c.dispose();
         context_query_cache.clear();
         if (DebugPlugin.getDefault() != null) fireChanged();
         if (launch_task != null) launch_task.done(false);
         launch_monitor = null;
         launch_task = null;
         runShutdownSequence(new Runnable() {
             public void run() {
                 shutdown = true;
                 fireTerminate();
                 for (TCFTask<Boolean> tsk : disconnect_wait_list) {
                     tsk.done(Boolean.TRUE);
                 }
                 disconnect_wait_list.clear();
             }
         });
         // Log severe exceptions: bug 386067
         if (error instanceof RuntimeException) {
             Activator.log("Channel disconnected with error", error);
         }
     }
 
     protected void runLaunchSequence(Runnable done) {
         done.run();
     }
 
     private void downloadMemoryMaps(ILaunchConfiguration cfg, final Runnable done) throws Exception {
         final IMemoryMap mmap = channel.getRemoteService(IMemoryMap.class);
         if (mmap == null) {
             done.run();
             return;
         }
         final HashMap<String,ArrayList<IMemoryMap.MemoryRegion>> maps = new HashMap<String,ArrayList<IMemoryMap.MemoryRegion>>();
         getMemMaps(maps, cfg);
         final HashSet<IToken> cmds = new HashSet<IToken>(); // Pending commands
         final Runnable done_all = new Runnable() {
             boolean launch_done;
             public void run() {
                 if (launch_done) return;
                 done.run();
                 launch_done = true;
             }
         };
         final IMemoryMap.DoneSet done_set_mmap = new IMemoryMap.DoneSet() {
             public void doneSet(IToken token, Exception error) {
                 assert cmds.contains(token);
                 cmds.remove(token);
                 if (error != null) Activator.log("Cannot update context memory map", error);
                 if (cmds.isEmpty()) done_all.run();
             }
         };
         for (String id : maps.keySet()) {
             ArrayList<IMemoryMap.MemoryRegion> map = maps.get(id);
             TCFMemoryRegion[] arr = map.toArray(new TCFMemoryRegion[map.size()]);
             cmds.add(mmap.set(id, arr, done_set_mmap));
         }
         update_memory_maps = new Runnable() {
             public void run() {
                 try {
                     Set<String> set = new HashSet<String>(maps.keySet());
                     maps.clear();
                     getMemMaps(maps, getLaunchConfiguration());
                     for (String id : maps.keySet()) {
                         ArrayList<IMemoryMap.MemoryRegion> map = maps.get(id);
                         TCFMemoryRegion[] arr = map.toArray(new TCFMemoryRegion[map.size()]);
                         cmds.add(mmap.set(id, arr, done_set_mmap));
                     }
                     for (String id : set) {
                         if (maps.get(id) != null) continue;
                         cmds.add(mmap.set(id, null, done_set_mmap));
                     }
                 }
                 catch (Throwable x) {
                     channel.terminate(x);
                 }
             }
         };
         if (cmds.isEmpty()) done_all.run();
     }
 
     private void updateMemoryMapsOnProcessCreation(ILaunchConfiguration cfg, final Runnable done) throws Exception {
         final IMemory mem = channel.getRemoteService(IMemory.class);
         final IMemoryMap mmap = channel.getRemoteService(IMemoryMap.class);
         if (mem == null || mmap == null) {
             done.run();
             return;
         }
         final HashSet<String> deleted_maps = new HashSet<String>();
         final HashMap<String,ArrayList<IMemoryMap.MemoryRegion>> maps = new HashMap<String,ArrayList<IMemoryMap.MemoryRegion>>();
         getMemMaps(maps, cfg);
         final HashSet<String> mems = new HashSet<String>(); // Already processed memory IDs
         final HashSet<IToken> cmds = new HashSet<IToken>(); // Pending commands
         final HashMap<String,String> mem2map = new HashMap<String,String>();
         final Runnable done_all = new Runnable() {
             boolean launch_done;
             public void run() {
                 mems.clear();
                 deleted_maps.clear();
                 if (launch_done) return;
                 done.run();
                 launch_done = true;
             }
         };
         final IMemoryMap.DoneSet done_set_mmap = new IMemoryMap.DoneSet() {
             public void doneSet(IToken token, Exception error) {
                 cmds.remove(token);
                 if (error != null) Activator.log("Cannot update context memory map", error);
                 if (cmds.isEmpty()) done_all.run();
             }
         };
         final IMemory.DoneGetContext done_get_context = new IMemory.DoneGetContext() {
             public void doneGetContext(IToken token, Exception error, MemoryContext context) {
                 cmds.remove(token);
                 if (context != null && mems.add(context.getID())) {
                     String id = context.getName();
                     if (id == null) id = context.getID();
                     if (id != null) {
                         ArrayList<IMemoryMap.MemoryRegion> map = maps.get(id);
                         if (map != null) {
                             TCFMemoryRegion[] arr = map.toArray(new TCFMemoryRegion[map.size()]);
                             cmds.add(mmap.set(context.getID(), arr, done_set_mmap));
                             mem2map.put(context.getID(), id);
                         }
                         else if (deleted_maps.contains(id)) {
                             cmds.add(mmap.set(context.getID(), null, done_set_mmap));
                             mem2map.remove(context.getID());
                         }
                     }
                 }
                 if (cmds.isEmpty()) done_all.run();
             }
         };
         final IMemory.DoneGetChildren done_get_children = new IMemory.DoneGetChildren() {
             public void doneGetChildren(IToken token, Exception error, String[] ids) {
                 cmds.remove(token);
                 if (ids != null) {
                     for (String id : ids) {
                         cmds.add(mem.getChildren(id, this));
                         cmds.add(mem.getContext(id, done_get_context));
                     }
                 }
                 if (cmds.isEmpty()) done_all.run();
             }
         };
         cmds.add(mem.getChildren(null, done_get_children));
         mem.addListener(new IMemory.MemoryListener() {
             public void memoryChanged(String context_id, Number[] addr, long[] size) {
             }
             public void contextRemoved(String[] context_ids) {
                 for (String id : context_ids) {
                     mems.remove(id);
                     mem2map.remove(id);
                 }
             }
             public void contextChanged(MemoryContext[] contexts) {
                 for (MemoryContext context : contexts) {
                     String id = context.getName();
                     if (id == null) id = context.getID();
                     if (id == null) continue;
                     if (id.equals(mem2map.get(context.getID()))) continue;
                     ArrayList<IMemoryMap.MemoryRegion> map = maps.get(id);
                     if (map == null) continue;
                     TCFMemoryRegion[] arr = map.toArray(new TCFMemoryRegion[map.size()]);
                     cmds.add(mmap.set(context.getID(), arr, done_set_mmap));
                     mem2map.put(context.getID(), id);
                 }
             }
             public void contextAdded(MemoryContext[] contexts) {
                 for (MemoryContext context : contexts) {
                     if (!mems.add(context.getID())) continue;
                     String id = context.getName();
                     if (id == null) id = context.getID();
                     if (id == null) continue;
                     ArrayList<IMemoryMap.MemoryRegion> map = maps.get(id);
                     if (map == null) continue;
                     TCFMemoryRegion[] arr = map.toArray(new TCFMemoryRegion[map.size()]);
                     cmds.add(mmap.set(context.getID(), arr, done_set_mmap));
                     mem2map.put(context.getID(), id);
                 }
             }
         });
         update_memory_maps = new Runnable() {
             public void run() {
                 try {
                     maps.clear();
                     mems.clear();
                     getMemMaps(maps, getLaunchConfiguration());
                     for (String id : mem2map.values()) {
                         if (maps.get(id) == null) deleted_maps.add(id);
                     }
                     cmds.add(mem.getChildren(null, done_get_children));
                 }
                 catch (Throwable x) {
                     channel.terminate(x);
                 }
             }
         };
     }
 
     @SuppressWarnings("unchecked")
     private void getMemMaps(Map<String,ArrayList<IMemoryMap.MemoryRegion>> maps, ILaunchConfiguration cfg) throws Exception {
         // Parse ATTR_FILES
         String s = cfg.getAttribute(TCFLaunchDelegate.ATTR_FILES, (String)null);
         if (s != null) {
             Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)JSON.parseOne(s.getBytes("UTF-8"));
             for (Map<String,Object> m : c) {
                 Boolean b = (Boolean)m.get(TCFLaunchDelegate.FILES_LOAD_SYMBOLS);
                 if (b != null && b.booleanValue()) {
                     String id = (String)m.get(TCFLaunchDelegate.FILES_CONTEXT_ID);
                     if (id == null) id = (String)m.get(TCFLaunchDelegate.FILES_CONTEXT_FULL_NAME);
                     if (id != null) {
                         Map<String,Object> map = new HashMap<String,Object>();
                         map.put(IMemoryMap.PROP_FILE_NAME, m.get(TCFLaunchDelegate.FILES_FILE_NAME));
                         b = (Boolean)m.get(TCFLaunchDelegate.FILES_RELOCATE);
                         if (b != null && b.booleanValue()) {
                             map.put(IMemoryMap.PROP_ADDRESS, m.get(TCFLaunchDelegate.FILES_ADDRESS));
                             map.put(IMemoryMap.PROP_OFFSET, m.get(TCFLaunchDelegate.FILES_OFFSET));
                             map.put(IMemoryMap.PROP_SIZE, m.get(TCFLaunchDelegate.FILES_SIZE));
                         }
                         b = (Boolean)m.get(TCFLaunchDelegate.FILES_ENABLE_OSA);
                         if (b != null && b.booleanValue()) {
                             map.put(IMemoryMap.PROP_OSA, new HashMap<String,Object>());
                         }
                         ArrayList<IMemoryMap.MemoryRegion> l = maps.get(id);
                         if (l == null) {
                             l = new ArrayList<IMemoryMap.MemoryRegion>();
                             maps.put(id, l);
                         }
                         l.add(new TCFMemoryRegion(map));
                     }
                 }
             }
         }
         // Parse ATTR_MEMORY_MAP
         TCFLaunchDelegate.getMemMapsAttribute(maps, cfg);
     }
 
     private void readPathMapConfiguration(ILaunchConfiguration cfg) throws CoreException {
         String s = cfg.getAttribute(TCFLaunchDelegate.ATTR_PATH_MAP, "");
         host_path_map = new ArrayList<IPathMap.PathMapRule>();
         host_path_map.addAll(TCFLaunchDelegate.parsePathMapAttribute(s));
         s = cfg.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, "");
         host_path_map.addAll(TCFLaunchDelegate.parseSourceLocatorMemento(s));
         readCustomPathMapConfiguration(channel, cfg, host_path_map);
         int cnt = 0;
         String id = getClientID();
         if (id != null) {
             for (IPathMap.PathMapRule r : host_path_map) r.getProperties().put(IPathMap.PROP_ID, id + "/" + cnt++);
         }
     }
 
     /**
      * Returns the client ID to use to mark the path map rules managed by this client.
      *
      * @return The client ID.
      */
     protected String getClientID() {
         return Activator.getClientID();
     }
 
     /**
      * Add custom path map rules to the host path map before applying the path map.
      *
      * @param channel The channel. Must not be <code>null</code>.
      * @param cfg The launch configuration. Must not be <code>null</code>.
      * @param host_path_map The host path map. Must not be <code>null</code>.
      */
     protected void readCustomPathMapConfiguration(IChannel channel, ILaunchConfiguration cfg, List<IPathMap.PathMapRule> host_path_map) {
         // Default implementation do nothing
     }
 
     private void downloadPathMaps(ILaunchConfiguration cfg, final Runnable done) throws Exception {
         readPathMapConfiguration(cfg);
         applyPathMap(channel, host_path_map.toArray(new IPathMap.PathMapRule[host_path_map.size()]), new IPathMap.DoneSet() {
             @Override
             public void doneSet(IToken token, Exception error) {
                 if (error != null) channel.terminate(error);
                 else done.run();
             }
         });
     }
 
     /**
      * Apply the path map to the given channel.
      *
      * @param channel The channel. Must not be <code>null</code>.
      * @param map The path map. Must not be <code>null</code>.
      * @param done The done to invoke. Must not be <code>null</code>.
      */
     protected void applyPathMap(final IChannel channel, final IPathMap.PathMapRule[] map, final IPathMap.DoneSet done) {
         IPathMap path_map_service = getService(IPathMap.class);
         path_map_service.set(map, done);
     }
 
     private String[] toArgsArray(String file, String cmd) {
         // Create arguments list from a command line.
         int i = 0;
         int l = cmd.length();
         List<String> arr = new ArrayList<String>();
         arr.add(file);
         for (;;) {
             while (i < l && cmd.charAt(i) == ' ') i++;
             if (i >= l) break;
             String s = null;
             if (cmd.charAt(i) == '"') {
                 i++;
                 StringBuffer bf = new StringBuffer();
                 while (i < l) {
                     char ch = cmd.charAt(i++);
                     if (ch == '"') break;
                     if (ch == '\\' && i < l) ch = cmd.charAt(i++);
                     bf.append(ch);
                 }
                 s = bf.toString();
             }
             else {
                 int i0 = i;
                 while (i < l && cmd.charAt(i) != ' ') i++;
                 s = cmd.substring(i0, i);
             }
             arr.add(s);
         }
         return arr.toArray(new String[arr.size()]);
     }
 
     private void copyFileToRemoteTarget(String local_file, String remote_file, final Runnable done) {
         if (local_file == null) {
             channel.terminate(new Exception("Program does not exist"));
             return;
         }
         final IFileSystem fs = channel.getRemoteService(IFileSystem.class);
         if (fs == null) {
             channel.terminate(new Exception(
                     "Cannot download program file: target does not provide File System service"));
             return;
         }
         try {
             final File local_fd = new File(local_file);
             final InputStream inp = new FileInputStream(local_fd);
             final String task_name = "Downloading: " + local_fd.getName();
             int flags = IFileSystem.TCF_O_WRITE | IFileSystem.TCF_O_CREAT | IFileSystem.TCF_O_TRUNC;
             if (launch_monitor != null) launch_monitor.subTask(task_name);
             fs.open(remote_file, flags, null, new IFileSystem.DoneOpen() {
 
                 IFileHandle handle;
                 long offset = 0;
                 final Set<IToken> cmds = new HashSet<IToken>();
                 final byte[] buf = new byte[0x1000];
 
                 public void doneOpen(IToken token, FileSystemException error, IFileHandle handle) {
                     this.handle = handle;
                     if (error != null) {
                         TCFLaunch.this.error = new Exception("Cannot download program file", error);
                         fireChanged();
                         done.run();
                     }
                     else {
                         write_next();
                     }
                 }
 
                 private void write_next() {
                     try {
                         while (cmds.size() < 8) {
                             int rd = inp.read(buf);
                             if (rd < 0) {
                                 close();
                                 break;
                             }
                             final long kb_done = (offset + rd) / 1024;
                             cmds.add(fs.write(handle, offset, buf, 0, rd, new IFileSystem.DoneWrite() {
 
                                 public void doneWrite(IToken token, FileSystemException error) {
                                     cmds.remove(token);
                                     if (launch_monitor != null) {
                                         launch_monitor.subTask(task_name + ", " + kb_done + " KB done");
                                     }
                                     if (error != null) channel.terminate(error);
                                     else write_next();
                                 }
                             }));
                             offset += rd;
                         }
                     }
                     catch (Throwable x) {
                         channel.terminate(x);
                     }
                 }
 
                 private void close() {
                     if (cmds.size() > 0) return;
                     try {
                         inp.close();
                         fs.close(handle, new IFileSystem.DoneClose() {
 
                             public void doneClose(IToken token, FileSystemException error) {
                                 if (error != null) channel.terminate(error);
                                 else done.run();
                             }
                         });
                     }
                     catch (Throwable x) {
                         channel.terminate(x);
                     }
                 }
             });
         }
         catch (Throwable x) {
             channel.terminate(x);
         }
     }
 
     @SuppressWarnings("unchecked")
     private void startRemoteProcess(final ILaunchConfiguration cfg) throws Exception {
         final String project = cfg.getAttribute(TCFLaunchDelegate.ATTR_PROJECT_NAME, "");
         final String local_file = cfg.getAttribute(TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE, "");
         final String remote_file = cfg.getAttribute(TCFLaunchDelegate.ATTR_REMOTE_PROGRAM_FILE, "");
         if (local_file.length() != 0 && remote_file.length() != 0) {
             // Download executable file
             new LaunchStep() {
                 @Override
                 void start() throws Exception {
                     copyFileToRemoteTarget(TCFLaunchDelegate.getProgramPath(project, local_file), remote_file, this);
                 }
             };
         }
         final String attach_to_process = getAttribute("attach_to_process");
         if (attach_to_process != null) {
             final IProcesses ps = channel.getRemoteService(IProcesses.class);
             if (ps == null) throw new Exception("Target does not provide Processes service");
             // Attach the process
             new LaunchStep() {
                 @Override
                 void start() {
                     IProcesses.DoneGetContext done = new IProcesses.DoneGetContext() {
                         public void doneGetContext(IToken token, final Exception error, final ProcessContext process) {
                             if (error != null) {
                                 channel.terminate(error);
                             }
                             else {
                                 process.attach(new IProcesses.DoneCommand() {
                                     public void doneCommand(IToken token, final Exception error) {
                                         if (error != null) {
                                             channel.terminate(error);
                                         }
                                         else {
                                             context_filter = new HashSet<String>();
                                             context_filter.add(process.getID());
                                             TCFLaunch.this.process = process;
                                             ps.addListener(prs_listener);
                                             readProcessStreams();
                                             done();
                                         }
                                     }
                                 });
                             }
                         }
                     };
                     ps.getContext(attach_to_process, done);
                 }
             };
         }
         else if (local_file.length() != 0 || remote_file.length() != 0) {
             final IProcesses ps = channel.getRemoteService(IProcesses.class);
             if (ps == null) throw new Exception("Target does not provide Processes service");
             final boolean append = cfg.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
             if (append) {
                 // Get system environment variables
                 new LaunchStep() {
                     @Override
                     void start() throws Exception {
                         ps.getEnvironment(new IProcesses.DoneGetEnvironment() {
                             public void doneGetEnvironment(IToken token, Exception error, Map<String,String> env) {
                                 if (error != null) {
                                     channel.terminate(error);
                                 }
                                 else {
                                     if (env != null) process_env.putAll(env);
                                     done();
                                 }
                             }
                         });
                     }
                 };
             }
             final String dir = cfg.getAttribute(TCFLaunchDelegate.ATTR_WORKING_DIRECTORY, "");
             final String args = cfg.getAttribute(TCFLaunchDelegate.ATTR_PROGRAM_ARGUMENTS, "");
             final Map<String,String> env = cfg.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String,String>)null);
             final boolean attach_children = cfg.getAttribute(TCFLaunchDelegate.ATTR_ATTACH_CHILDREN, true);
             final boolean stop_at_entry = cfg.getAttribute(TCFLaunchDelegate.ATTR_STOP_AT_ENTRY, true);
             final boolean stop_at_main = cfg.getAttribute(TCFLaunchDelegate.ATTR_STOP_AT_MAIN, true);
             final boolean use_terminal = cfg.getAttribute(TCFLaunchDelegate.ATTR_USE_TERMINAL, true);
             String dont_stop = cfg.getAttribute(TCFLaunchDelegate.ATTR_SIGNALS_DONT_STOP, "");
             String dont_pass = cfg.getAttribute(TCFLaunchDelegate.ATTR_SIGNALS_DONT_PASS, "");
             final int no_stop = dont_stop.length() > 0 ? Integer.parseInt(dont_stop, 16) : 0;
             final int no_pass = dont_pass.length() > 0 ? Integer.parseInt(dont_pass, 16) : 0;
             // Start the process
             new LaunchStep() {
                 @Override
                 void start() {
                     if (env != null) process_env.putAll(env);
                     String file = remote_file;
                     if (file == null || file.length() == 0) file = TCFLaunchDelegate.getProgramPath(project, local_file);
                     if (file == null || file.length() == 0) {
                         channel.terminate(new Exception("Program file does not exist"));
                         return;
                     }
                     IProcesses.DoneStart done = new IProcesses.DoneStart() {
                         public void doneStart(IToken token, final Exception error, ProcessContext process) {
                             process_start_command = null;
                             if (error != null) {
                                 for (String id : new HashSet<String>(process_stream_ids.keySet())) disconnectStream(id);
                                 process_stream_ids.clear();
                                 Protocol.sync(new Runnable() {
                                     public void run() {
                                         channel.terminate(error);
                                     }
                                 });
                             }
                             else {
                                 context_filter = new HashSet<String>();
                                 context_filter.add(process.getID());
                                 TCFLaunch.this.process = process;
                                 ps.addListener(prs_listener);
                                 readProcessStreams();
                                 done();
                             }
                         }
                     };
                     if (launch_monitor != null) launch_monitor.subTask("Starting: " + file);
                     String[] args_arr = toArgsArray(file, args);
                     IProcessesV1 ps_v1 = channel.getRemoteService(IProcessesV1.class);
                     if (ps_v1 != null) {
                         Map<String,Object> params = new HashMap<String,Object>();
                         if (mode.equals(ILaunchManager.DEBUG_MODE)) {
                             params.put(IProcessesV1.START_ATTACH, true);
                             params.put(IProcessesV1.START_ATTACH_CHILDREN, attach_children);
                             params.put(IProcessesV1.START_STOP_AT_ENTRY, stop_at_entry);
                             params.put(IProcessesV1.START_STOP_AT_MAIN, stop_at_main);
                             params.put(IProcessesV1.START_SIG_DONT_STOP, no_stop);
                             params.put(IProcessesV1.START_SIG_DONT_PASS, no_pass);
                         }
                         if (use_terminal) params.put(IProcessesV1.START_USE_TERMINAL, true);
                         process_start_command = ps_v1.start(dir, file, args_arr, process_env, params, done);
                     }
                     else {
                         boolean attach = mode.equals(ILaunchManager.DEBUG_MODE);
                         process_start_command = ps.start(dir, file, args_arr, process_env, attach, done);
                     }
                 }
             };
             if (mode.equals(ILaunchManager.DEBUG_MODE)) {
                 // Get process signal list
                 new LaunchStep() {
                     @Override
                     void start() {
                         ps.getSignalList(process.getID(), new IProcesses.DoneGetSignalList() {
                             public void doneGetSignalList(IToken token, Exception error, Collection<Map<String,Object>> list) {
                                 if (error != null && !process_exited) Activator.log("Can't get process signal list", error);
                                 process_signals = list;
                                 done();
                             }
                         });
                     }
                 };
                 // Set process signal masks
                 if (no_stop != 0 || no_pass != 0) {
                     new LaunchStep() {
                         @Override
                         void start() {
                             final HashSet<IToken> cmds = new HashSet<IToken>();
                             final IProcesses.DoneCommand done_set_mask = new IProcesses.DoneCommand() {
                                 public void doneCommand(IToken token, Exception error) {
                                     cmds.remove(token);
                                     if (error != null && !process_exited) channel.terminate(error);
                                     else if (cmds.size() == 0) done();
                                 }
                             };
                             cmds.add(ps.setSignalMask(process.getID(), no_stop, no_pass, done_set_mask));
                             final IRunControl rc = channel.getRemoteService(IRunControl.class);
                             if (rc != null) {
                                 final IRunControl.DoneGetChildren done_get_children = new IRunControl.DoneGetChildren() {
                                     public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                                         if (context_ids != null) {
                                             for (String id : context_ids) {
                                                 cmds.add(ps.setSignalMask(id, no_stop, no_pass, done_set_mask));
                                                 cmds.add(rc.getChildren(id, this));
                                             }
                                         }
                                         cmds.remove(token);
                                         if (error != null && !process_exited) channel.terminate(error);
                                         else if (cmds.size() == 0) done();
                                     }
                                 };
                                 cmds.add(rc.getChildren(process.getID(), done_get_children));
                             }
                         }
                     };
                 }
             }
         }
     }
 
     private void readProcessStreams() {
         assert process_start_command == null;
         final IStreams streams = getService(IStreams.class);
         if (streams == null) return;
         final String inp_id = (String)process.getProperties().get(IProcesses.PROP_STDIN_ID);
         final String out_id = (String)process.getProperties().get(IProcesses.PROP_STDOUT_ID);
         final String err_id = (String)process.getProperties().get(IProcesses.PROP_STDERR_ID);
         for (final String id : process_stream_ids.keySet().toArray(new String[process_stream_ids.size()])) {
             if (id.equals(inp_id)) {
                 process_input_stream_id = id;
             }
             else if (id.equals(out_id)) {
                 readStream(process.getID(), id, 0);
             }
             else if (id.equals(err_id)) {
                 readStream(process.getID(), id, 1);
             }
             else {
                 disconnectStream(id);
             }
         }
     }
 
     private void readStream(final String ctx_id, final String id, final int no) {
         if (ctx_id != null) {
             // Force creation of console
             for (LaunchListener l : getListeners()) l.onProcessOutput(this, ctx_id, no, null);
         }
         final IStreams streams = getService(IStreams.class);
         IStreams.DoneRead done = new IStreams.DoneRead() {
             public void doneRead(IToken token, Exception error, int lost_size, byte[] data, boolean eos) {
                 if (lost_size > 0) {
                     Exception x = new IOException("Process output data lost due buffer overflow");
                     for (LaunchListener l : getListeners()) l.onProcessStreamError(TCFLaunch.this, ctx_id, no, x, lost_size);
                 }
                 if (data != null && data.length > 0) {
                     for (LaunchListener l : getListeners()) l.onProcessOutput(TCFLaunch.this, ctx_id, no, data);
                 }
                 if (disconnected_stream_ids.contains(id)) return;
                 if (error != null) {
                     for (LaunchListener l : getListeners()) l.onProcessStreamError(TCFLaunch.this, ctx_id, no, error, 0);
                     disconnected_stream_ids.add(id);
                 }
                 if (!eos && error == null) {
                     streams.read(id, 0x1000, this);
                 }
             }
         };
         streams.read(id, 0x1000, done);
         streams.read(id, 0x1000, done);
         streams.read(id, 0x1000, done);
         streams.read(id, 0x1000, done);
     }
 
     private void disconnectStream(String id) {
         assert process_stream_ids.get(id) != null;
         process_stream_ids.remove(id);
         if (channel.getState() != IChannel.STATE_OPEN) return;
         IStreams streams = getService(IStreams.class);
         streams.disconnect(id, new IStreams.DoneDisconnect() {
             public void doneDisconnect(IToken token, Exception error) {
                 if (channel.getState() != IChannel.STATE_OPEN) return;
                 if (error != null) channel.terminate(error);
             }
         });
     }
 
     protected void runShutdownSequence(final Runnable done) {
         done.run();
     }
 
     /*--------------------------------------------------------------------------------------------*/
 
     /**
      * Return error object if launching failed.
      */
     public Throwable getError() {
         return error;
     }
 
     /**
      * Terminate the launch because of fatal error.
      * @param x - the error object.
      */
     public void setError(Throwable x) {
         error = x;
         if (x != null) {
             if (channel != null && channel.getState() == IChannel.STATE_OPEN) {
                 channel.terminate(x);
             }
             else if (!connecting) {
                 disconnected = true;
                 shutdown = true;
             }
         }
         fireChanged();
     }
 
     /**
      * Get current target breakpoints status information.
      * @return status information object
      */
     public TCFBreakpointsStatus getBreakpointsStatus() {
         return breakpoints_status;
     }
 
     /**
      * Check if the agent supports setting of user defined memory map entries
      * for a context that does not exits yet.
      * @return true if memory map preloading is supported.
      */
     public boolean isMemoryMapPreloadingSupported()  {
         return supports_memory_map_preloading;
     }
 
     /**
      * Register a launch listener.
      * @param listener - client object implementing TCFLaunch.LaunchListener interface.
      */
     public static void addListener(LaunchListener listener) {
         assert Protocol.isDispatchThread();
         listeners.add(listener);
         listeners_array = null;
     }
 
     /**
      * Remove a launch listener.
      * @param listener - client object implementing TCFLaunch.LaunchListener interface.
      */
     public static void removeListener(LaunchListener listener) {
         assert Protocol.isDispatchThread();
         listeners.remove(listener);
         listeners_array = null;
     }
 
     @Override
     public void launchConfigurationChanged(final ILaunchConfiguration cfg) {
         super.launchConfigurationChanged(cfg);
         if (!cfg.equals(getLaunchConfiguration())) return;
         if (channel != null && channel.getState() == IChannel.STATE_OPEN) {
             new TCFTask<Boolean>(channel) {
                 public void run() {
                     try {
                         if (update_memory_maps != null) update_memory_maps.run();
                         if (host_path_map != null) {
                             readPathMapConfiguration(cfg);
                             applyPathMap(channel, host_path_map.toArray(new IPathMap.PathMapRule[host_path_map.size()]), new IPathMap.DoneSet() {
                                 public void doneSet(IToken token, Exception error) {
                                     if (error != null) channel.terminate(error);
                                     done(false);
                                 }
                             });
                         }
                         else {
                             done(true);
                         }
                     }
                     catch (Throwable x) {
                         channel.terminate(x);
                         done(false);
                     }
                 }
             }.getE();
             // TODO: update signal masks when launch configuration changes
         }
     }
 
     /**
      * Get TCF communication channel that is used by the launch.
      * Thread safe method.
      */
     public IChannel getChannel() {
         return channel;
     }
 
     /**
      * If the launch has started a remote process, return the process information.
      * Starting a process is optional and not applicable to all launches.
      * @return remote process information or null.
      */
     public IProcesses.ProcessContext getProcessContext() {
         return process;
     }
 
     /**
      * Write to stdin stream of remote process that was started by the launch.
      * @param prs_id - TCF ID of the process.
      * @param buf - data to write
      * @param pos - starting position in 'buf'
      * @param len - number of bytes to write.
      * @throws Exception
      */
     public void writeProcessInputStream(final String prs_id, byte[] buf, int pos, final int len) throws Exception {
         assert Protocol.isDispatchThread();
         if (channel.getState() != IChannel.STATE_OPEN) throw new IOException("Connection closed");
         IStreams streams = getService(IStreams.class);
         if (streams == null) throw new IOException("Streams service not available");
         if (process != null && prs_id.equals(process.getID())) {
             final String id = process_input_stream_id;
             if (process_stream_ids.get(id) == null) throw new IOException("Input stream not available");
             streams.write(id, buf, pos, len, new IStreams.DoneWrite() {
                 public void doneWrite(IToken token, Exception error) {
                     if (error == null) return;
                     if (process_stream_ids.get(id) == null) return;
                     for (LaunchListener l : getListeners()) l.onProcessStreamError(TCFLaunch.this, prs_id, 0, error, len);
                     disconnectStream(id);
                 }
             });
             return;
         }
         for (final String rx_id : uart_rx_stream_ids.keySet()) {
             if (!prs_id.equals(uart_rx_stream_ids.get(rx_id))) continue;
             streams.write(rx_id, buf, pos, len, new IStreams.DoneWrite() {
                 public void doneWrite(IToken token, Exception error) {
                     if (error == null) return;
                     if (uart_rx_stream_ids.get(rx_id) == null) return;
                     for (LaunchListener l : getListeners()) l.onProcessStreamError(TCFLaunch.this, prs_id, 0, error, len);
                     disconnectStream(rx_id);
                 }
             });
             return;
         }
         throw new IOException("No target process");
     }
 
     public void openUartStreams(final String ctx_id, Map<String,Object> uart_props) {
         assert Protocol.isDispatchThread();
         if (uart_props == null) return;
         IStreams streams = getService(IStreams.class);
         if (streams == null) return;
         final String rx_id = (String)uart_props.get("RXStreamID");
         if (rx_id != null && uart_rx_stream_ids.get(rx_id) == null) {
             streams.connect(rx_id, new IStreams.DoneConnect() {
                 @Override
                 public void doneConnect(IToken token, Exception error) {
                     if (uart_rx_stream_ids.get(rx_id) != null) return;
                     uart_rx_stream_ids.put(rx_id, ctx_id);
                     if (error == null) return;
                     for (LaunchListener l : getListeners()) l.onProcessStreamError(TCFLaunch.this, ctx_id, 0, error, 0);
                 }
             });
         }
         final String tx_id = (String)uart_props.get("TXStreamID");
         if (tx_id != null && uart_tx_stream_ids.get(tx_id) == null) {
             streams.connect(tx_id, new IStreams.DoneConnect() {
                 @Override
                 public void doneConnect(IToken token, Exception error) {
                     if (uart_tx_stream_ids.get(tx_id) != null) return;
                    uart_tx_stream_ids.put(tx_id, ctx_id);
                     if (error == null) {
                         readStream(ctx_id, tx_id, 0);
                         return;
                     }
                     for (LaunchListener l : getListeners()) l.onProcessStreamError(TCFLaunch.this, ctx_id, 0, error, 0);
                 }
             });
         }
     }
 
     public boolean isConnecting() {
         return connecting;
     }
 
     public boolean isConnected() {
         return channel != null && !connecting && !disconnected;
     }
 
     public void onDetach(String prs_id) {
         if (disconnecting) return;
         if (process == null) return;
         if (process_exited) return;
         if (!prs_id.equals(process.getID())) return;
         IProcesses processes = getService(IProcesses.class);
         processes.removeListener(prs_listener);
         IStreams streams = getService(IStreams.class);
         for (String id : process_stream_ids.keySet()) {
             streams.disconnect(id, new IStreams.DoneDisconnect() {
                 public void doneDisconnect(IToken token, Exception error) {
                     if (error != null) channel.terminate(error);
                 }
             });
         }
         process_stream_ids.clear();
         process_input_stream_id = null;
         process = null;
     }
 
     public void onLastContextRemoved() {
         ILaunchConfiguration cfg = getLaunchConfiguration();
         try {
             if (process != null && cfg.getAttribute(TCFLaunchDelegate.ATTR_DISCONNECT_ON_CTX_EXIT, true)) {
                 last_context_exited = true;
                 closeChannel();
             }
         }
         catch (Throwable e) {
             Activator.log("Cannot access launch configuration", e);
         }
     }
 
     public void closeChannel() {
         assert Protocol.isDispatchThread();
         if (channel == null) return;
         if (channel.getState() == IChannel.STATE_CLOSED) return;
         if (disconnecting) return;
         disconnecting = true;
         final Set<IToken> cmds = new HashSet<IToken>();
         if (process != null && !process_exited) {
             cmds.add(process.terminate(new IProcesses.DoneCommand() {
                 public void doneCommand(IToken token, Exception error) {
                     cmds.remove(token);
                     if (error != null) channel.terminate(error);
                     else if (cmds.isEmpty()) channel.close();
                 }
             }));
         }
         IStreams streams = getService(IStreams.class);
         IStreams.DoneDisconnect done_disconnect = new IStreams.DoneDisconnect() {
             public void doneDisconnect(IToken token, Exception error) {
                 cmds.remove(token);
                 if (error != null) channel.terminate(error);
                 else if (cmds.isEmpty()) channel.close();
             }
         };
         for (String id : process_stream_ids.keySet()) {
             cmds.add(streams.disconnect(id, done_disconnect));
         }
         for (String id : uart_rx_stream_ids.keySet()) {
             cmds.add(streams.disconnect(id, done_disconnect));
         }
         for (String id : uart_tx_stream_ids.keySet()) {
             cmds.add(streams.disconnect(id, done_disconnect));
         }
         process_stream_ids.clear();
         process_input_stream_id = null;
         uart_rx_stream_ids.clear();
         uart_tx_stream_ids.clear();
         if (dprintf_stream_id != null) {
             disconnected_stream_ids.add(dprintf_stream_id);
             cmds.add(streams.disconnect(dprintf_stream_id, done_disconnect));
             dprintf_stream_id = null;
         }
         if (cmds.isEmpty()) channel.close();
     }
 
     public IPeer getPeer() {
         assert Protocol.isDispatchThread();
         return channel.getRemotePeer();
     }
 
     public String getPeerName() {
         // Safe to call from any thread.
         return peer_name;
     }
 
     public <V extends IService> V getService(Class<V> cls) {
         assert Protocol.isDispatchThread();
         return channel.getRemoteService(cls);
     }
 
     @Override
     public boolean canDisconnect() {
         return !disconnected;
     }
 
     @Override
     public boolean isDisconnected() {
         return disconnected;
     }
 
     @Override
     public void disconnect() throws DebugException {
         try {
             new TCFTask<Boolean>(8000) {
                 public void run() {
                     if (channel == null || shutdown) {
                         done(true);
                     }
                     else {
                         disconnect_wait_list.add(this);
                         closeChannel();
                     }
                 }
             }.get();
         }
         catch (IllegalStateException x) {
             // Don't report this exception - it means Eclipse is being shut down
         }
         catch (Exception x) {
             throw new TCFError(x);
         }
     }
 
     @Override
     public boolean canTerminate() {
         return false;
     }
 
     @Override
     public boolean isTerminated() {
         return disconnected;
     }
 
     @Override
     public void terminate() throws DebugException {
     }
 
     public boolean isExited() {
         return last_context_exited;
     }
 
     public boolean isProcessExited() {
         return process_exited;
     }
 
     public int getExitCode() {
         return process_exit_code;
     }
 
     public Collection<Map<String,Object>> getSignalList() {
         return process_signals;
     }
 
     public List<IPathMap.PathMapRule> getHostPathMap() {
         assert Protocol.isDispatchThread();
         return host_path_map;
     }
 
     public TCFDataCache<IPathMap.PathMapRule[]> getTargetPathMap() {
         assert Protocol.isDispatchThread();
         return target_path_map;
     }
 
     public Map<String,IStorage> getTargetPathMappingCache() {
         return target_path_mapping_cache;
     }
 
     public TCFDataCache<String[]> getContextQuery(final String query) {
         if (query == null) return null;
         TCFDataCache<String[]> cache = context_query_cache.get(query);
         if (cache == null) {
             if (disconnected) return null;
             final IContextQuery service = channel.getRemoteService(IContextQuery.class);
             if (service == null) return null;
             cache = new TCFDataCache<String[]>(channel) {
                 @Override
                 protected boolean startDataRetrieval() {
                     command = service.query(query, new IContextQuery.DoneQuery() {
                         public void doneQuery(IToken token, Exception error, String[] contexts) {
                             set(token, error, contexts);
                         }
                     });
                     return false;
                 }
             };
             context_query_cache.put(query, cache);
         }
         return cache;
     }
 
     /**
      * Activate TCF launch: open communication channel and perform all necessary launch steps.
      * @param mode - on of launch mode constants defined in ILaunchManager.
      * @param id - TCF peer ID.
      */
     public void launchTCF(String mode, String id) {
         launchTCF(mode, id, null, null);
     }
 
     /**
      * Activate TCF launch: open communication channel and perform all necessary launch steps.
      * @param mode - on of launch mode constants defined in ILaunchManager.
      * @param id - TCF peer ID.
      * @param task - TCF task that is waiting until the launching is done, can be null
      * @param monitor - launching progress monitor, can be null
      */
     public void launchTCF(String mode, String id, TCFTask<Boolean> task, IProgressMonitor monitor) {
         assert Protocol.isDispatchThread();
         this.mode = mode;
         this.launch_task = task;
         this.launch_monitor = monitor;
         try {
             if (id == null || id.length() == 0) throw new IOException("Invalid peer ID");
             redirection_path.clear();
             for (;;) {
                 int i = id.indexOf('/');
                 if (i <= 0) {
                     redirection_path.add(id);
                     break;
                 }
                 redirection_path.add(id.substring(0, i));
                 id = id.substring(i + 1);
             }
             String id0 = redirection_path.removeFirst();
             IPeer peer = Protocol.getLocator().getPeers().get(id0);
             if (peer == null) throw new Exception("Cannot locate peer " + id0);
             peer_name = peer.getName();
             channel = peer.openChannel();
             channel.addChannelListener(new IChannel.IChannelListener() {
 
                 public void onChannelOpened() {
                     try {
                         peer_name = getPeer().getName();
                         onConnected();
                     }
                     catch (Throwable x) {
                         channel.terminate(x);
                     }
                 }
 
                 public void congestionLevel(int level) {
                 }
 
                 public void onChannelClosed(Throwable error) {
                     channel.removeChannelListener(this);
                     onDisconnected(error);
                 }
 
             });
             assert channel.getState() == IChannel.STATE_OPENING;
             if (launch_monitor != null) launch_monitor.subTask("Connecting to " + peer_name);
             connecting = true;
         }
         catch (Throwable e) {
             onDisconnected(e);
         }
     }
 
     /**
      * Activate TCF launch: Re-use the passed in communication channel and perform all necessary launch steps.
      *
      * @param mode - on of launch mode constants defined in ILaunchManager.
      * @param peer_name - TCF peer name.
      * @param channel - TCF communication channel.
      */
     public void launchTCF(String mode, String peer_name, IChannel channel) {
         assert Protocol.isDispatchThread();
         this.mode = mode;
         this.redirection_path.clear();
         try {
             if (channel == null || channel.getRemotePeer() == null) throw new IOException("Invalid channel");
             this.peer_name = peer_name;
             this.channel = channel;
 
             IChannel.IChannelListener listener = new IChannel.IChannelListener() {
 
                 public void onChannelOpened() {
                     try {
                         TCFLaunch.this.peer_name = getPeer().getName();
                         onConnected();
                     }
                     catch (Throwable x) {
                         TCFLaunch.this.channel.terminate(x);
                     }
                 }
 
                 public void congestionLevel(int level) {
                 }
 
                 public void onChannelClosed(Throwable error) {
                     TCFLaunch.this.channel.removeChannelListener(this);
                     onDisconnected(error);
                 }
 
             };
             channel.addChannelListener(listener);
 
             connecting = true;
             if (channel.getState() == IChannel.STATE_OPEN) {
                 listener.onChannelOpened();
             }
             else if (channel.getState() != IChannel.STATE_OPENING) {
                 throw new IOException("Channel is in invalid state");
             }
         }
         catch (Throwable e) {
             onDisconnected(e);
         }
     }
 
     /****************************************************************************************************************/
 
     private long getActionTimeStamp(String id) {
         Long l = context_action_timestamps.get(id);
         if (l == null) return 0;
         return l.longValue();
     }
 
     private void startAction(final String id) {
         if (active_actions.get(id) != null) return;
         LinkedList<TCFAction> list = context_action_queue.get(id);
         if (list == null || list.size() == 0) return;
         final TCFAction action = list.removeFirst();
         if (list.size() == 0) context_action_queue.remove(id);
         active_actions.put(id, action);
         final long timestamp = getActionTimeStamp(id);
         long time = System.currentTimeMillis();
         Protocol.invokeLater(timestamp + actions_interval - time, new Runnable() {
             public void run() {
                 if (active_actions.get(id) != action) return;
                 long time = System.currentTimeMillis();
                 synchronized (pending_clients) {
                     if (pending_clients.size() > 0) {
                         if (time - timestamp < actions_interval + 1000) {
                             Protocol.invokeLater(20, this);
                             return;
                         }
                         pending_clients.clear();
                     }
                     else if (time < pending_clients_timestamp + 10) {
                         Protocol.invokeLater(pending_clients_timestamp + 10 - time, this);
                         return;
                     }
                 }
                 context_action_timestamps.put(id, time);
                 for (ActionsListener l : action_listeners) l.onContextActionStart(action);
                 action.run();
             }
         });
     }
 
     /**
      * Add an object to the set of pending clients.
      * Actions execution will be delayed until the set is empty,
      * but not longer then 1 second.
      * @param client
      */
     public void addPendingClient(Object client) {
         synchronized (pending_clients) {
             pending_clients.add(client);
             pending_clients_timestamp = System.currentTimeMillis();
         }
     }
 
     /**
      * Remove an object from the set of pending clients.
      * Actions execution resumes when the set becomes empty.
      * @param client
      */
     public void removePendingClient(Object client) {
         synchronized (pending_clients) {
             if (pending_clients.remove(client) && pending_clients.size() == 0) {
                 pending_clients_timestamp = System.currentTimeMillis();
             }
         }
     }
 
     /**
      * Set minimum interval between context actions execution.
      * @param interval - minimum interval in milliseconds.
      */
     public void setContextActionsInterval(long interval) {
         actions_interval = interval;
     }
 
     /**
      * Add a context action to actions queue.
      * Examples of context actions are resume/suspend/step commands,
      * which were requested by a user.
      * @param action
      */
     public void addContextAction(TCFAction action) {
         assert Protocol.isDispatchThread();
         String id = action.getContextID();
         LinkedList<TCFAction> list = context_action_queue.get(id);
         if (list == null) context_action_queue.put(id, list = new LinkedList<TCFAction>());
         int priority = action.getPriority();
         for (ListIterator<TCFAction> i = list.listIterator();;) {
             if (i.hasNext()) {
                 if (priority <= i.next().getPriority()) continue;
                 i.previous();
             }
             i.add(action);
             break;
         }
         startAction(id);
     }
 
     /**
      * Set action result for given context ID.
      * Action results are usually presented to a user same way as context suspend reasons.
      * @param id - debug context ID.
      * @param result - a string to be shown to user.
      */
     public void setContextActionResult(String id, String result) {
         assert Protocol.isDispatchThread();
         for (ActionsListener l : action_listeners) l.onContextActionResult(id, result);
     }
 
     /**
      * Remove an action from the queue.
      * The method should be called when the action execution is done.
      * @param action
      */
     public void removeContextAction(TCFAction action) {
         assert Protocol.isDispatchThread();
         String id = action.getContextID();
         assert active_actions.get(id) == action;
         active_actions.remove(id);
         for (ActionsListener l : action_listeners) l.onContextActionDone(action);
         startAction(id);
     }
 
     /**
      * Remove all actions from the queue of a debug context.
      * @param id - debug context ID.
      */
     public void removeContextActions(String id) {
         assert Protocol.isDispatchThread();
         context_action_queue.remove(id);
         context_action_timestamps.remove(id);
     }
 
     /**
      * Get action queue size of a debug context.
      * @param id - debug context ID.
      * @return count of pending actions.
      */
     public int getContextActionsCount(String id) {
         assert Protocol.isDispatchThread();
         LinkedList<TCFAction> list = context_action_queue.get(id);
         int n = list == null ? 0 : list.size();
         if (active_actions.get(id) != null) n++;
         return n;
     }
 
     /**
      * Add a listener that will be notified when an action execution is started or finished,
      * or when an action result is posted.
      * @param l - action listener.
      */
     public void addActionsListener(ActionsListener l) {
         action_listeners.add(l);
     }
 
     /**
      * Remove an action listener that was registered with addActionsListener().
      * @param l - action listener.
      */
     public void removeActionsListener(ActionsListener l) {
         action_listeners.remove(l);
     }
 
     /**
      * Get context filer of the launch.
      * By default, TCF debugger shows all remote debug contexts.
      * Context filter is used to hide unwanted contexts to reduce UI clutter.
      * @return context filter.
      */
     public Set<String> getContextFilter() {
         return context_filter;
     }
 }
