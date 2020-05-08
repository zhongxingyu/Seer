 package gov.va.mumps.debug.core.model;
 
 import gov.va.mumps.debug.core.MDebugConstants;
 import gov.va.mumps.debug.xtdebug.vo.ReadResultsVO;
 import gov.va.mumps.debug.xtdebug.vo.StackVO;
 import gov.va.mumps.debug.xtdebug.vo.StepResultsVO;
 import gov.va.mumps.debug.xtdebug.vo.StepResultsVO.ResultReasonType;
 import gov.va.mumps.debug.xtdebug.vo.VariableVO;
 import gov.va.mumps.launching.InputReadyListener;
 import gov.va.mumps.launching.ReadCommandListener;
 import gov.va.mumps.launching.WriteCommandListener;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.IMemoryBlock;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.core.model.IThread;
 
 public class MDebugTarget extends MDebugElement implements IDebugTarget, InputReadyListener {
 
 	private ILaunch launch;
 	private MDebugRpcProcess rpcDebugProcess; //TODO: I'm supposed to just store this for the getProcess method. I shouldn't invoke it otherwise.
 	private boolean suspended;
 	private MThread debugThread;
 	private String name;
 	
 	//Console handling
 	private boolean linkedToConsole;
 	private List<WriteCommandListener> writeCommandListeners = new LinkedList<WriteCommandListener>();
 	private List<ReadCommandListener> readCommandListeners = new LinkedList<ReadCommandListener>();
 	
 	//variables
 	//variables already defined at debug start
 	private SortedSet<VariableVO> initialVars;
 	//All the currently defined variables
 	private List<VariableVO> allVariables;
 	//Only variables created during the debug process
 	private MVariable[] variables;
 	
 	//process stack
 	private MStackFrame[] stack;
 	
 	//state
 	private StepMode stepMode;
 	
 	public MDebugTarget(ILaunch launch, MDebugRpcProcess rpcProcess) {
 		super(null);
 		setDebugTarget(this);
 		this.launch = launch;
 		this.rpcDebugProcess = rpcProcess;
 		setLinkedToConsole(false);
 		
 		
 		
 		debugThread = new MThread(this);		
 		suspended = true; //false in tutorial, because it waits for the event to come back on the socket saying suspended
 		
 //		stack = new Stack<MStackFrame>(15); //the server can't handle more than 15 anyway.
 		
 //		eventDispatch = new EventDispatchJob();
 //		eventDispatch.schedule();
 		
 		stack = new MStackFrame[0];
 		
 		handleResponse(rpcProcess.getResponseResults()); //TODO: should results really be handled while setting up the launch config? this may be too soon.
 		
 		// temp just for testing
 //		if (Math.random() > .8) {
 //			for (int i = 0; i < 30; i++)
 //				System.out.println("test breakpoint added!");
 //			rpcProcess.addBreakPoint("STACK5+3^TSTBLAH2");
 //		}
 		
 		fireCreationEvent(); //to register that the DebugTarget has been started.
 		installDeferredBreakpoints();
 		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
 
 		Job resumeJob = new Job("Resume") { //the LaunchManager is (probably?) executing this and it shouldn't block and hold while the code is resuming (running). It needs to let go at this point
 			
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				try {
 					resume();
 				} catch (Throwable t) {
 					t.printStackTrace();
 					return Status.CANCEL_STATUS; //was it really cancelled? like by a user?
 				}
 				
 				return Status.OK_STATUS;
 			}
 		};
 		resumeJob.schedule();
 
 		
 	}
 	
 	/**
 	 * Install breakpoints that are already registered with the breakpoint
 	 * manager.
 	 */
 	private void installDeferredBreakpoints() {
 		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(MDebugConstants.M_DEBUG_MODEL);
 		for (int i = 0; i < breakpoints.length; i++) {
 			breakpointAdded(breakpoints[i]);
 		}
 	}
 	
 	/**
 	 * Removes all active breakpoints currently registered with the breakpoint
 	 * manager.
 	 */
 	private void uninstallActiveBreakpoints() {
 		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(MDebugConstants.M_DEBUG_MODEL);
 		for (int i = 0; i < breakpoints.length; i++) {
 			breakpointRemoved(breakpoints[i], null);
 		}
 	}
 
 	@Override
 	public IDebugTarget getDebugTarget() {
 		return this;
 	}
 
 	@Override
 	public ILaunch getLaunch() {
 		return launch;
 	}
 
 	@Override
 	public boolean canTerminate() {
 		return rpcDebugProcess.canTerminate();
 	}
 
 	@Override
 	public boolean isTerminated() {
 		return rpcDebugProcess.isTerminated();
 	}
 
 	@Override
 	public void terminate() throws DebugException {
 		terminated();
 	}
 
 	@Override
 	public boolean canResume() {
 		//System.out.println("canResume()");
 		return !isTerminated() && isSuspended();
 	}
 
 	@Override
 	public boolean canSuspend() {
 		//System.out.println("canSuspend()");
 		return !isTerminated() && !isSuspended(); //maybe always return false since the suspend button doesn't do anything?
 	}
 
 	@Override
 	public boolean isSuspended() {
 		//TODO: what is this for? when and why is it called?
 		//System.out.println("isSuspended()");
 		return suspended;
 	}
 
 	@Override
 	public void resume() {
 		stepMode = StepMode.RESUME;
 		debugThread.fireResumeEvent(DebugEvent.CLIENT_REQUEST);
 		rpcDebugProcess.resume();
 		suspended = false;
 		StepResultsVO results = rpcDebugProcess.getResponseResults();
 		suspended = true;
 		handleResponse(results);
 		//debugThread.fireResumeEvent(DebugEvent.CLIENT_REQUEST); //CLIENT_REQUEST - user/guiclient requested
 	}
 
 	@Override
 	public void suspend() {
 		//rpcProcess has no suspend command
 		//suspended = true; //what would this do though?
 	}
 
 	@Override
 	public void breakpointAdded(IBreakpoint breakpoint) {
 		if (!supportsBreakpoint(breakpoint) || isTerminated())
 			return;
 		
 		try {
 			if (!breakpoint.isEnabled())
 				return;
 
 			if (breakpoint instanceof AbstractMBreakpoint)
 				rpcDebugProcess
 						.addBreakPoint(((AbstractMBreakpoint) breakpoint)
 								.getBreakpointAsTag());
 			else if (breakpoint instanceof MWatchpoint)
 				rpcDebugProcess.addWatchPoint(((MWatchpoint) breakpoint)
 						.getWatchpointVariable());
 		} catch (CoreException e) {
 		}
 	}
 
 	@Override
 	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta marker) {
 		if (!supportsBreakpoint(breakpoint) || isTerminated())
 			return;
 		
 		try {
 			// this code is from the custom debugger tutorial. it assumes that
 			// breakpoints only property being changed is the enable/disable
 			// toggle. Which is not quite true, what if a line number changes?
 			// if so, use the IMarkerDelta parm to compute this change and
 			// remove the old value from the RPC api... although I think that
 			// would only be benefecial for editing a file while debugging it
 			// at the same time, which I am not a big fan of supporting.
 			if (breakpoint.isEnabled()) {
 				breakpointAdded(breakpoint);
 			} else {
 				breakpointRemoved(breakpoint, null);
 			}
 		} catch (CoreException e) {
 		}
 		
 	}
 
 	@Override
 	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta marker) {
 		if (!supportsBreakpoint(breakpoint) || isTerminated())
 			return;
 		
 		if (breakpoint instanceof AbstractMBreakpoint)
 			rpcDebugProcess.removeBreakPoint(((AbstractMBreakpoint)breakpoint).getBreakpointAsTag());
 		else if (breakpoint instanceof MWatchpoint)
 			rpcDebugProcess.removeWatchPoint(((MWatchpoint)breakpoint).getWatchpointVariable());
 	}
 
 	@Override
 	public boolean canDisconnect() {
 		return false;
 	}
 
 	@Override
 	public void disconnect() throws DebugException {
 	}
 
 	@Override
 	public boolean isDisconnected() {
 		return false;
 	}
 
 	@Override
 	public IMemoryBlock getMemoryBlock(long arg0, long arg1)
 			throws DebugException {
 		return null;
 	}
 
 	@Override
 	public boolean supportsStorageRetrieval() {
 		return false;
 	}
 
 	@Override
 	public String getName() throws DebugException {
 		if (name == null) {
 			name = "MUMPS Code";
 			try {
 				name = getLaunch().getLaunchConfiguration().getAttribute(MDebugConstants.ATTR_M_ENTRY_TAG, "MUMPS Code");
 				//name += " [DebugTarget]";
 			} catch (CoreException e) {
 			}
 		}
 		return name;
 	}
 
 	@Override
 	public IProcess getProcess() {
 		return rpcDebugProcess;
 	}
 
 	@Override
 	public IThread[] getThreads() throws DebugException {
 		return new IThread[] {debugThread};
 	}
 
 	@Override
 	public boolean hasThreads() throws DebugException {
 		return true; //apparently a bug otherwise
 	}
 
 	@Override
 	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
 		if (breakpoint.getModelIdentifier().equals(MDebugConstants.M_DEBUG_MODEL) &&
 				(breakpoint instanceof AbstractMBreakpoint || breakpoint instanceof MWatchpoint)) {
 			return true;
 			//the previous example only supports breakpoints on a per file basis.
 			//many languages, if not all common languages, will allow you to jump
 			//from one code file to another. this debug target should support all
 			//M_DEBUG_MODEL breakpoints in the workspace.
 //			try {
 //				String program = getLaunch().getLaunchConfiguration().getAttribute(MDebugConstants.ATTR_PDA_PROGRAM, (String)null);
 //				if (program != null) {
 //					IMarker marker = breakpoint.getMarker();
 //					if (marker != null) {
 //						IPath p = new Path(program);
 //						return marker.getResource().getFullPath().equals(p);
 //					}
 //				}
 //			} catch (CoreException e) {
 //			}			
 		}
 		return false;
 	}
 
 	public void stepOver() {
 		stepMode = StepMode.STEP_OVER;
 		suspended = true; //note that handleResponse can set this to false if it the debug is completed
 		fireResumeEvent(DebugEvent.STEP_OVER);
 		rpcDebugProcess.stepOver();
 		handleResponse(rpcDebugProcess.getResponseResults()); //TODO: move this to a new thread if yo want asychon. also move the suspend events to the async thread
 	}
 
 	public void stepInto() {
 		stepMode = StepMode.STEP_INTO;
 		suspended = true;
 		fireResumeEvent(DebugEvent.STEP_INTO);
 		rpcDebugProcess.stepInto();
 		handleResponse(rpcDebugProcess.getResponseResults());
 	}
 
 	public void stepOut() {
 		stepMode = StepMode.STEP_OUT;
 		suspended = true;
 		fireResumeEvent(DebugEvent.STEP_RETURN);
 		rpcDebugProcess.stepOut();
 		handleResponse(rpcDebugProcess.getResponseResults());
 	}
 	
 	public MStackFrame[] getStackFrames() {
 		//System.out.println("getStackFrame()");
 		return stack;
 	}
 	
 	public MVariable[] getVariables() {
 		return variables;
 	}
 	
 	public List<VariableVO> getAllVariables() {
 		return allVariables;
 	}
 	
 	private synchronized void handleResponse(StepResultsVO vo) {
 		
 		//handle any lines that come back (as result of a read or write command)
 		if (vo.getWriteLine() != null && !vo.getWriteLine().equals("")) {
 			for (WriteCommandListener listener : writeCommandListeners) {
				listener.handleWriteCommand(vo.getWriteLine() + (vo.getResultReason() == ResultReasonType.WRITE ? '\n' : ""));
 			}
 		}
 		
 		if (vo.isComplete()) {
 			terminated();
 			return;
 		}
 		
 		//resend commands. Wheneever the debugger comes back it suspends execution of the MUMPS code
 		
 		//resend resume when write is encountered command
 		if (vo.getResultReason() == ResultReasonType.WRITE && stepMode == StepMode.RESUME) {
 			resume();
 			return;
 		}
 		
 		//when a read command is encountered, while stepping in resume mode, it must send a step into. wierd bug with the RPC is that is must step over the read command even though it sends a read command back to the client.
 		if (vo.getResultReason() == ResultReasonType.READ && stepMode == StepMode.RESUME) {
 			rpcDebugProcess.stepInto();
 		}
 		
 		//create stack objects from incoming RPC results
 		Iterator<StackVO> stackItr = vo.getStack();
 		List<StackVO> svoList = new LinkedList<StackVO>();
 		while (stackItr.hasNext()) {
 			 svoList.add(stackItr.next());
 		}
 		stack = new MStackFrame[svoList.size()];
 		
 		//String prevStackCaller = null;
 		for (int i = svoList.size() - 1; i >= 0; i--) {
 			StackVO svo = svoList.get(i);
 			
 			/*
 			 * (1) use locationAsTag and (2) for stacks bellow the top stack,
 			 * subsitute the callerName from the parent stack. Then convert the
 			 * locationAsATag to a lineNumber and routine name.
 			 */
 			
 			//TODO: implement this as async or have the api fixed
 			// update: no, location as a tag will not work so well because it will
 			// require opening the entire file and lightly parsing it. way to
 			// much work to occur here in this model thread. will have to either
 			// index these somehow, or at least make this asycnrhonus/event
 			// based by putting the rpc call in a new thread.
 			if (i == svoList.size() - 1)
 				stack[0] = 
 				new MStackFrame(debugThread, svo.getStackName(), svo.getCaller(),
 						vo.getRoutineName(), vo.getLineLocation(), vo.getNextCommnd());
 			else
 				stack[svoList.size() - 1 - i] = 
 				new MStackFrame(debugThread, svo.getStackName(), svo.getCaller(),
 						null, -1, null);
 			
 			//prevStackCaller = svo.getCaller();
 		}
 		
 		//handle variables
 		allVariables = new LinkedList<VariableVO>();
 		Iterator<VariableVO> varItr = vo.getVariables();
 		while (varItr.hasNext())
 			allVariables.add(varItr.next());
 		//TODO: fire selectionChanged event.
 		
 		if (initialVars == null) { //set all the initial variables
 			initialVars = new TreeSet<VariableVO>(allVariables);
 		} else {
 			List<VariableVO> currVars = new LinkedList<VariableVO>();
 			varItr = vo.getVariables();
 			while (varItr.hasNext()) {
 				VariableVO varVO = varItr.next();
 				if (!initialVars.contains(varVO))
 					currVars.add(varVO);
 			}
 			
 			variables = new MVariable[currVars.size()];
 			for (int s = 0; s < stack.length; s++)
 				for (int i = 0; i < currVars.size(); i++) {
 					variables[i] = new MVariable(
 							stack[s], 
 							currVars.get(i).getName());
 					variables[i].setValue(new MValue(variables[i], currVars.get(i).getValue()));
 				}
 		}
 		
 		//handle read command results
 		//TODO: set suspend = true, fire no debug events, and set stepping = false
 		//TODO: the state of teh debug target should be updated wrt to the readCmdResults (isStarRead,etc). This may be important for sending the response back, if it isn't persisted it won't know what it was
 		if (vo.getReadResults() != null) {
 			ReadResultsVO readCmdResults = vo.getReadResults();
 			for (ReadCommandListener listener : readCommandListeners) {
 				int maxReadChars;
 				if (readCmdResults.isStarRead())
 					maxReadChars = 1;
 				else
 					maxReadChars = readCmdResults.getMaxChars() == null ? Integer.MAX_VALUE : readCmdResults.getMaxChars();
 				listener.handleReadCommand(maxReadChars);
 			}
 		} else if (vo.getNextCommnd() != null) { //why is it checking for null nextCommand. when is this null? this was just bad defensive programming
 			fireSuspendEvent(DebugEvent.STEP_END); //TODO: use the value from the API for breakpoint/watchpoint/step end
 		}
 	}
 	
 	private void terminated() {
 		uninstallActiveBreakpoints();
 		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
 		rpcDebugProcess.terminate();
 		suspended = false;
 		fireTerminateEvent(); //this fires the event to indicate that the debugtarget has terminated.
 	}
 
 	public boolean isLinkedToConsole() {
 		return linkedToConsole;
 	}
 
 	public void setLinkedToConsole(boolean linkedToConsole) {
 		this.linkedToConsole = linkedToConsole;
 	}
 
 	@Override
 	public void handleInput(final String input) {
 		if (isTerminated())
 			return;
 		
 		//input must be handled because it causes the program to continue (resume) running, and it completes the read command which may update variable values
 		rpcDebugProcess.sendReadInput(input);
 		handleResponse(rpcDebugProcess.getResponseResults());
 
 	}
 	
 	public void addWriteCommandListener(WriteCommandListener listener) {
 		writeCommandListeners.add(listener);
 	}
 
 	public void addReadCommandListener(ReadCommandListener listener) {
 		readCommandListeners.add(listener);
 	}
 	
 	public void removeWriteCommandListener(WriteCommandListener listener) {
 		writeCommandListeners.remove(listener);
 	}
 
 	public void removeReadCommandListener(ReadCommandListener listener) {
 		readCommandListeners.remove(listener);
 	}
 
 	private enum StepMode {
 		RESUME, STEP_INTO, STEP_OUT, STEP_OVER;
 	}
 	
 }
