 package org.strjdbg.eclipse.core.str.model;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 import org.eclipse.debug.core.model.IMemoryBlock;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.debug.core.model.IThread;
 import org.strategoxt.debug.core.control.DebugSessionManager;
 import org.strategoxt.debug.core.control.VMMonitor;
 import org.strategoxt.debug.core.eventspec.BreakPoint;
 import org.strategoxt.debug.core.eventspec.RuleEnterBreakPoint;
 import org.strategoxt.debug.core.eventspec.StrategyEnterBreakPoint;
 import org.strategoxt.debug.core.eventspec.StrategyStepBreakPoint;
 import org.strategoxt.debug.core.model.StrategoStackFrame;
 import org.strategoxt.debug.core.model.StrategoState;
 import org.strategoxt.debug.core.util.DebugSessionSettings;
 import org.strategoxt.debug.core.util.VMLauncherHelper;
 import org.strategoxt.debug.core.util.table.EventEntry;
 import org.strategoxt.debug.core.util.table.EventTable;
 import org.strjdbg.eclipse.core.Activator;
 import org.strjdbg.eclipse.core.str.launching.IStrategoConstants;
 
 import com.sun.jdi.VirtualMachine;
 import com.sun.jdi.VirtualMachineManager;
 import com.sun.jdi.connect.Connector;
 import com.sun.jdi.connect.IllegalConnectorArgumentsException;
 import com.sun.jdi.connect.ListeningConnector;
 
 public class StrategoDebugTarget extends StrategoDebugElement implements IDebugTarget {
 
 	// containing launch object
 	private ILaunch fLaunch;
 	
 	// threads
 	private StrategoThread fThread;
 	private IThread[] fThreads;
 	
 	// event dispatch job
 	private EventDispatchJob fEventDispatch;
 	
 	// suspend state
 	private boolean fSuspended = false;
 	
 	// terminated state
 	private boolean fTerminated = false;
 	
 	private DebugSessionManager manager = null;
 
 //	private String projectName = null; // TODO: use this one?
 	
 	/**
 	 * Listens to events from the Stratego VM and fires corresponding 
 	 * debug events.
 	 */
 	class EventDispatchJob extends Job implements VMMonitor {
 		
 		public EventDispatchJob() {
 			super("Stratego Event Dispatch");
 			setSystem(true);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
 		 */
 		protected IStatus run(IProgressMonitor monitor) {
 			String event = "";
 			while (!isTerminated() && event != null) {
 				//try {
 					//event = fEventReader.readLine();
 					// fetch the next event
 					if (event != null) {
 						fThread.setBreakpoints(null);
 						fThread.setStepping(false);
 						if (event.equals("started")) {
 							// vm started event received
 							started();
 						} else if (event.equals("terminated")) {
 							// vm terminated event received
 							terminated();
 						} else if (event.startsWith("resumed")) {
 							if (event.endsWith("step")) {
 								// resume and step
 								fThread.setStepping(true);
 								resumed(DebugEvent.STEP_OVER);
 							} else if (event.endsWith("client")) {
 								// resume
 								resumed(DebugEvent.CLIENT_REQUEST);
 							}
 						} else if (event.startsWith("suspended")) {
 							if (event.endsWith("client")) {
 								// vm suspended
 								suspended(DebugEvent.CLIENT_REQUEST);
 							} else if (event.endsWith("step")) {
 								// step
 								suspended(DebugEvent.STEP_END);
 							} else if (event.indexOf("breakpoint") >= 0) {
 								// hit breakpoint
 								//breakpointHit(event);
 							}
 						}
 					}
 				//} catch (IOException e) {
 				//	terminated();
 				//}
 			}
 			return Status.OK_STATUS;
 		}
 
 		public void stateChanged(StrategoState state) {
 			// TODO Auto-generated method stub
 			//System.out.println("State change!"); // we hit a breakpoint!
 			updateState(state);
 			breakpointHit(state);
 			//suspended(DebugEvent.CLIENT_REQUEST); // suspended due to client request
 		}
 
 		public void vmEvent(String event) {
 			// TODO Auto-generated method stub
 			//System.out.println("VM EVENT: " + event);
 			if ("VMSTARTED".equals(event))
 			{
 				started();
 			}
 			else if ("VMDEATH".equals(event))
 			{
 				terminated();
 			}
 		}
 		
 	}
 	
 	
 	public StrategoDebugTarget(DebugSessionSettings debugSessionSettings, ILaunch launch, final String port) throws CoreException {
 		super(null);
 		this.fLaunch = launch;
 		// running threads
 		fThread = new StrategoThread(this);
 		fThreads = new IThread[] {fThread};
 		
 		// handle events fired by the VM
 		fEventDispatch = new EventDispatchJob();
 		fEventDispatch.schedule();
 		
 		//VirtualMachine vm = this.setupAttacher(port); // when suspended at this line the debug target has time to setup the socket
 		// TODO: Use SocketListen (wait for VM to connect) instead of SocketAttach (connect to existing VM)
 		
 		Thread thread = new Thread("Listen on socket for target VM.") {
 			public void run()  {
 				VirtualMachine t_vm = null;
 				try {
 					t_vm = setupListener(port);
 				} catch (CoreException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				connectedToVM(t_vm);
 			}
 		};
 		
 		
 
 		
 		manager = new DebugSessionManager(debugSessionSettings, fEventDispatch);
 		
 		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
 		// we have to know when we get removed, so that we can shut off the debugger
 		// Used by PyDev debugger
 		// TODO: fires launchRemoved
 		//DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
 	    
 		thread.start(); // start the listener thread
 	}
 	
 	private void connectedToVM(VirtualMachine vm)
 	{
 		if (vm != null)
 		{
 			manager.setVirtualMachine(vm);
 			System.out.println("init listeners");
 			manager.setupEventListeners();
 			System.out.println("Redirect");
 			manager.redirectOutput();
 			//System.out.println("RUN");
 			//manager.runVM();
 			//System.out.println("Done");
 		}
 	}
 	
     public void launchRemoved(ILaunch launch) {
         // shut down the remote debugger when parent launch
         if (launch == this.fLaunch) {
             DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);;
         }
     }
 	
     /*
     // use setupListener instead of setupAttacher
 	private VirtualMachine setupAttacher(String port) throws CoreException
 	{
 		VirtualMachineManager vmManager = org.eclipse.jdi.Bootstrap.virtualMachineManager(); // eclipse vmManager
 		//VirtualMachineManager m2 = Bootstrap.virtualMachineManager(); // com.sun.jdi.BootStrap, this method returns null
 		
 		System.out.println("ATTACH@"+port);
 		AttachingConnector attachConnector = VMLauncherHelper.findSocketAttachConnector(vmManager);
 		
 		VirtualMachine vm = null;
 		try {
 			vm = this.attach(attachConnector, port);
 		} catch (IllegalConnectorArgumentsException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			abort("Unable to connect to Stratego VM", e);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			abort("Unable to connect to Stratego VM", e);
 		}
 		//System.out.println("Connected to VM! " + vm.description());
 		return vm;
 	}
 	
 	// attach running vm
 	@SuppressWarnings("unchecked")
 	private VirtualMachine attach(AttachingConnector connector, String port)
 			throws IllegalConnectorArgumentsException, IOException 
 	{
 		Map<String, Connector.Argument> args = connector.defaultArguments();
 		Connector.Argument pidArgument = args.get("port");
 		if (pidArgument == null) {
 			throw new IllegalStateException();
 		}
 		pidArgument.setValue(port);
 
 		return connector.attach(args);
 	}
 	*/
 	
 	private VirtualMachine setupListener(String port) throws CoreException
 	{
 		VirtualMachineManager vmManager = org.eclipse.jdi.Bootstrap.virtualMachineManager(); // eclipse vmManager
 		//VirtualMachineManager m2 = Bootstrap.virtualMachineManager(); // com.sun.jdi.BootStrap, this method returns null
 		
 		System.out.println("LISTEN@"+port);
 		ListeningConnector listenConnector = VMLauncherHelper.findSocketListenConnector(vmManager);	
 		
 		VirtualMachine vm = null;
 		try {
 			vm = this.listen(listenConnector, port);
 		} catch (IllegalConnectorArgumentsException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			abort("Unable to connect to Stratego VM", e);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			abort("Unable to connect to Stratego VM", e);
 		}
 		//System.out.println("Connected to VM! " + vm.description());
 		return vm;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private VirtualMachine listen(ListeningConnector connector, String port) throws IOException, IllegalConnectorArgumentsException
 	{
 		VirtualMachine vm = null;
 		Map<String, Connector.Argument> args = connector.defaultArguments();
 		Connector.Argument pidArgument = args.get("port");
 		if (pidArgument == null) {
 			throw new IllegalStateException();
 		}
 		pidArgument.setValue(port);
 
 		connector.startListening(args); // returns listening address
         vm = connector.accept(args);
         connector.stopListening(args);
         
 		return vm;
 	}
 	
 	
 	private String fName = null;
 
 	private StrategoState state;
 	
 	public String getName() throws DebugException {
 		if (fName == null) {
 			fName = "Stratego Program";
 			try {
 				fName = getLaunch().getLaunchConfiguration().getAttribute(IStrategoConstants.ATTR_STRATEGO_PROGRAM, "Stratego VM");
 			} catch (CoreException e) {
 			}
 		}
 		return fName;
 	}
 
 	/**
 	 *  TODO: JDIDebugTarget implements this as <code>return breakpoint instanceof IJavaBreakpoint;</code>
 	 */
 	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
 		if (breakpoint.getModelIdentifier().equals(IStrategoConstants.ID_STRATEGO_DEBUG_MODEL)) {
 			try {
 				String program = getLaunch().getLaunchConfiguration().getAttribute(IStrategoConstants.ATTR_STRATEGO_PROGRAM, (String)null);
 				if (program != null) {
 					IMarker marker = breakpoint.getMarker();
 					if (marker != null) {
 						IPath p = new Path(program);
 						return marker.getResource().getFullPath().equals(p);
 					}
 				}
 			} catch (CoreException e) {
 			}			
 		}
 		return false;
 	}
 
 	public IProcess getProcess() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IThread[] getThreads() throws DebugException {
 		// TODO Auto-generated method stub
 		return this.fThreads;
 	}
 
 	public boolean hasThreads() throws DebugException {
 		return true;
 	}
 
 	public IDebugTarget getDebugTarget() {
 		// TODO Auto-generated method stub
 		return this;
 	}
 
 	public ILaunch getLaunch() {
 		// TODO Auto-generated method stub
 		return this.fLaunch;
 	}
 
 	public boolean canTerminate() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	public boolean isTerminated() {
 		return this.fTerminated;
 	}
 
 	public void terminate() throws DebugException {
 		fTerminated = true;
 		fSuspended = false;
 		manager.terminateVM();
 	}
 
 	public boolean canResume() {
 		return !isTerminated() && isSuspended();
 	}
 
 	public boolean canSuspend() {
 		return !isTerminated() && !isSuspended();
 	}
 
 	public boolean isSuspended() {
 		return fSuspended;
 	}
 
 	/**
 	 * Resume the VM.
 	 */
 	public void resume() throws DebugException {
 		// TODO Auto-generated method stub
 		//System.out.println("resume program");
 		manager.resumeVM();
 	}
 
 	/**
 	 * Suspend the VM.
 	 */
 	public void suspend() throws DebugException {
 		// TODO Auto-generated method stub
 		manager.suspendVM();
 	}
 
 	public void breakpointAdded(IBreakpoint breakpoint) {
 		//System.out.println("breakpointAdded");
 		if (supportsBreakpoint(breakpoint)) {
 			
 			try {
 				if (breakpoint.isEnabled())
 				{
 					// only add the breakpoint to the Stratego debugger when the breakpoint is enabled
 					int linenumber = breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1);
 					
 					if (linenumber > 0)
 					{
 						// only linenumbers greater than 0 are valid as linenumber is 1-based
 						BreakPoint bp = createBreakpoint(linenumber);
 						if (bp != null)
 						{
 							this.manager.getEventSpecManager().add(bp);
 						}
 					}
 				}
 			} catch (CoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	
 	private BreakPoint createBreakpoint(int linenumber)
 	{
 		BreakPoint bp = null;
 		
 		// returns the type of events (s-step/r-enter/s-enter/s-exit/r-exit) can occur at the given linenumber
 		// the event type determines the breakpoint type
 		List<EventEntry> entries = this.manager.getEventSpecManager().getEventTable().getEventEntries(linenumber);
 
 		// prefer s-step over enter/exit breakpoints
 		EventEntry stepEvent = EventTable.getFirstStepEventEntry(entries);
 		if (stepEvent != null)
 		{
 			//create a step breakpoint
			bp = new StrategyStepBreakPoint(stepEvent.getStrategyName(), stepEvent.getLocationInfo().getStart_line_num(), stepEvent.getLocationInfo().getStart_token_pos());
 		}
 		else
 		{
 			// try to create an enter breakpoint
 			EventEntry enterEvent = EventTable.getFirstEnterEventEntry(entries);
 			if (enterEvent == null)
 			{
 				// TODO: what should we do when we have no step and no enter event?
 			}
 			else
 			{
 				if ("s-enter".equals(enterEvent.getEventType()))
 				{
					bp = new StrategyEnterBreakPoint(enterEvent.getStrategyName(), enterEvent.getLocationInfo().getStart_line_num(), enterEvent.getLocationInfo().getStart_token_pos());
 				}
 				else if ("r-enter".equals(enterEvent.getEventType()))
 				{
					bp = new RuleEnterBreakPoint(enterEvent.getStrategyName(), enterEvent.getLocationInfo().getStart_line_num(), enterEvent.getLocationInfo().getStart_token_pos());
 				}
 				else
 				{
 					// should never happen has enterEvent always returns a s-enter or r-enter
 				}
 			}
 		}
 		//breakpoint.getMarker().getAttribute(IMarker., defaultValue)
 		//BreakPoint bp = new RuleEnterBreakPoint("find-comment-match", -1);
 		
 		return bp;
 	}
 
 	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
 		// TODO Auto-generated method stub
 		//System.out.println("breakpointChanged");
 		if (supportsBreakpoint(breakpoint)) {
 			try {
 				if (breakpoint.isEnabled()) {
 					breakpointAdded(breakpoint);
 				} else {
 					breakpointRemoved(breakpoint, null);
 				}
 			} catch (CoreException e) {
 			}
 		}
 	}
 
 	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
 		// TODO Auto-generated method stub
 		//System.out.println("breakpointRemoved");
 		if (supportsBreakpoint(breakpoint)) {
 
 			// convert IBreakpoint to Stratego Breakpoint
 
 			int linenumber = breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1);
 			
 			if (linenumber > 0)
 			{
 				// only linenumbers greater than 0 are valid as linenumber is 1-based
 				BreakPoint bp = createBreakpoint(linenumber);
 				if (bp != null)
 				{
 					this.manager.getEventSpecManager().remove(bp);
 				}
 			}
 
 		}
 	}
 
 	public boolean canDisconnect() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void disconnect() throws DebugException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public boolean isDisconnected() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public IMemoryBlock getMemoryBlock(long startAddress, long length)
 			throws DebugException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public boolean supportsStorageRetrieval() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public IStackFrame[] getStackFrames() {
 		StrategoStackFrame[] ssFrames = this.state.getStackFrames();
 		IStackFrame[] frames = new IStackFrame[ssFrames.length];
 		for(int i = 0; i < ssFrames.length; i++)
 		{
 			int id = ssFrames.length - i - 1;
 			frames[id] = new EStrategoStackFrame(this.fThread, ssFrames[i], id);
 		}
 		
 		return frames;
 	}
 
 	public void step() {
 		// TODO Auto-generated method stub
 		// manager.stepVM();
 	}
 
 
 	/**
 	 * Notification we have connected to the VM and it has started.
 	 * Resume the VM.
 	 */
 	private void started() {
 		fireCreationEvent();
 		installDeferredBreakpoints();
 		try {
 			resume();
 		} catch (DebugException e) {
 		}
 	}
 	
 	/**
 	 * Install breakpoints that are already registered with the breakpoint
 	 * manager.
 	 */
 	private void installDeferredBreakpoints() {
 		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IStrategoConstants.ID_STRATEGO_DEBUG_MODEL);
 		for (int i = 0; i < breakpoints.length; i++) {
 			breakpointAdded(breakpoints[i]);
 		}
 	}
 	
 	/**
 	 * Called when this debug target terminates.
 	 */
 	private void terminated() {
 		fTerminated = true;
 		fSuspended = false;
 		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
 		fireTerminateEvent();
 	}
 	
 	/**
 	 * Notification the target has resumed for the given reason
 	 * 
 	 * @param detail reason for the resume
 	 */
 	private void resumed(int detail) {
 		fSuspended = false;
 		fThread.fireResumeEvent(detail);
 	}
 	
 	/**
 	 * Notification the target has suspended for the given reason
 	 * 
 	 * @param detail reason for the suspend
 	 */
 	private void suspended(int detail) {
 		fSuspended = true;
 		fThread.fireSuspendEvent(detail);
 	}	
 	
 	
 	/**
 	 * Notification a breakpoint was encountered. Determine
 	 * which breakpoint was hit and fire a suspend event.
 	 * 
 	 * @param event debug event
 	 */
 	private void breakpointHit(StrategoState state) {
 		// determine which breakpoint was hit, and set the thread's breakpoint
 		//int lineNumber = state.getLocationInfo().getStart_line_num(); // 1-based index // Use the currentFrame
 		int lineNumber = state.currentFrame().getCurrentLocationInfo().getStart_line_num(); // 1-based index
 		
 		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IStrategoConstants.ID_STRATEGO_DEBUG_MODEL);
 		for (int i = 0; i < breakpoints.length; i++) {
 			IBreakpoint breakpoint = breakpoints[i];
 			if (supportsBreakpoint(breakpoint)) {
 				if (breakpoint instanceof ILineBreakpoint) {
 					ILineBreakpoint lineBreakpoint = (ILineBreakpoint) breakpoint;
 					
 					try {
 						
 						if (lineBreakpoint.getLineNumber() == lineNumber) {
 							fThread.setBreakpoints(new IBreakpoint[]{breakpoint});
 							break;
 						}
 						
 					} catch (CoreException e) {
 					}
 				}
 			}
 		}
 		
 		suspended(DebugEvent.BREAKPOINT);
 	}
 
 	public VMMonitor getVMMonitor() {
 		// TODO Auto-generated method stub
 		return fEventDispatch;
 	}	
 	
 	private void updateState(StrategoState state)
 	{
 		this.state = state;
 	}
 	
 	protected void abort(String message, Throwable e) throws DebugException {
 		throw new DebugException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), 
 				DebugPlugin.INTERNAL_ERROR, message, e));
 	}
 }
