 package deeva;
 
 import com.sun.jdi.*;
 import com.sun.jdi.connect.Connector;
 import com.sun.jdi.connect.IllegalConnectorArgumentsException;
 import com.sun.jdi.connect.LaunchingConnector;
 import com.sun.jdi.connect.VMStartException;
 import com.sun.jdi.event.*;
 import com.sun.jdi.request.*;
 import deeva.breakpoint.Breakpoint;
 import deeva.exception.NoLoadedClassException;
 import deeva.exception.NoLocationException;
 import deeva.exception.NoVMException;
 import deeva.exception.WrongStateError;
 import deeva.io.StdInRedirectThread;
 import deeva.io.StreamRedirectThread;
 import deeva.processor.JVMValue;
 import deeva.processor.ReferenceValue;
 import deeva.sourceutil.SourceClassFinder;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.Semaphore;
 
 public class Debug extends EventHandlerBase {
     public static enum State {
         NO_INFERIOR,
         STASIS,
         RUNNING,
         AWAITING_IO;
     }
 
     private final String[] excludes = {"java.*", "javax.*", "sun.*", "com.sun.*"};
 
     private VirtualMachine vm;
     private StreamRedirectThread outThread;
     private StreamRedirectThread errThread;
     private StdInRedirectThread inThread;
     private BlockingQueue<String> inQueue;
     private DebugResponseQueue outQueue;
     private State state = State.NO_INFERIOR;
     private Semaphore sema;
     private List<StackFrameMeta> stacks;
     private Map<Breakpoint, BreakpointRequest> breakpoints;
     private SourceClassFinder finder;
     private String currentClass;
     private int line_number = 0;
     private ObjectReference systemInObj;
     private Method sysInReadMethod;
     private Method sysInAvailableMethod;
     private List<String> programArgs;
     private boolean enableAssertions = false;
     private final String classPaths;
     private final DeevaEventDispatcher dispatcher;
 
     public Debug(DebugResponseQueue outQueue,
                  String classPaths, String sourcePaths,
                  String mainClass, boolean enableAssertions,
                  List<String> initialArgs, DeevaEventDispatcher dispatcher) {
         /* TODO: Use Builder pattern to setup a Debug Object or split Debug
         into smaller bits */
         this.classPaths = classPaths;
         this.programArgs = initialArgs;
         this.breakpoints = new HashMap<Breakpoint, BreakpointRequest>();
         this.outQueue = outQueue;
         this.inQueue = new LinkedBlockingQueue<String>();
         this.sema = new Semaphore(0);
         this.state = State.NO_INFERIOR;
         this.finder = new SourceClassFinder(classPaths, sourcePaths);
         this.currentClass = mainClass;
         this.enableAssertions = enableAssertions;
         this.dispatcher = dispatcher;
 
         /*  Generate all the classes and their relevant sources the debuggee
             may need
          */
         finder.getAllClasses();
         finder.getAllSources();
     }
 
     public void start(String programName, List<String> programArgs,
                       boolean enableAssertions) {
         this.programArgs = programArgs;
         this.enableAssertions = enableAssertions;
 
         vm = launchTarget(programName, programArgs);
         EventThread eventThread = new EventThread(vm, excludes, this);
         eventThread.start();
         redirectOutput();
 
         state = State.STASIS;
         EventRequestManager reqMgr = getRequestManager();
         ClassPrepareRequest prepareRequest = reqMgr.createClassPrepareRequest();
         for (String ex : excludes) {
             prepareRequest.addClassExclusionFilter (ex);
         }
 
         /* Find system.in which is part of the bootstrap classloading,
         maybe put in a new class or something */
         List<ReferenceType> classes = vm.classesByName("java.lang.System");
         ReferenceType systemClass = classes.get(0);
         Field sysInField = systemClass.fieldByName("in");
         this.systemInObj = (ObjectReference) systemClass.getValue(sysInField);
         ReferenceType sysInRefType = this.systemInObj.referenceType();
         List<Method> readMethods = sysInRefType.methodsByName("read");
         this.sysInAvailableMethod = sysInRefType.methodsByName("available").get(0);
 
         /* Get the read method we're interested in */
         this.sysInReadMethod = null;
         for (Method method : readMethods) {
             if (method.argumentTypeNames().size() == 3) {
                 this.sysInReadMethod = method;
             }
         }
 
         /* Listen for method entry requests in the systemInObj instance */
         MethodEntryRequest mer = reqMgr.createMethodEntryRequest();
         mer.addClassFilter(sysInRefType);
         mer.addInstanceFilter(this.systemInObj);
 
         mer.setSuspendPolicy(EventRequest.SUSPEND_ALL);
         mer.enable();
 
         prepareRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);    // suspend so we can examine vars
         prepareRequest.enable();
         attemptToSetWaitingBreakpoints();
     }
 
     public Map<String, String> getSources() {
         return this.finder.getAllSources();
     }
 
     public DeevaState putStdInMessage(String msg) throws
             InterruptedException {
         /* Possibly more validation if necessary */
 
         /* Pushes given string msg, on to the inQueue that will be fed
          * into the debuggee stdin */
         this.inQueue.put(msg);
 
         /* If we are awaiting_io */
         if (state == State.AWAITING_IO) {
             state = State.RUNNING;
             sema.acquire();
             DeevaState state = getState();
             /* TODO: Fire event somewhere else */
             return state;
         }
 
         /* If we are not awaiting io, i.e. premature push of input data,
         then just continue */
 
         DeevaStateBuilder dsb = new DeevaStateBuilder();
         dsb.setState(state);
         DeevaState state = dsb.create();
         state.premature_push = true;
         return state;
     }
 
     public JVMValue getHeapObject(Long uniqueRefID, String refType) {
         /* We can assume that the class would be loaded, since we're not
          * allowing arbitrary introspection */
 
         /* If we're stopped we need to look at the last location, if we're not
          * stopped we need to ignore this or throw an exception. */
 
         System.err.println("Printing Heap");
         System.err.println("uniqueRefID: " + uniqueRefID);
         System.err.println("refType: " + refType);
         List<ReferenceType> matchingClasses = vm.classesByName(refType);
 
         ObjectReference objectFound = null;
 
         /* Go through each matching class and look for unique ID */
         for (ReferenceType matchingClass : matchingClasses) {
             /* Go through all instances of this class and look for the id */
             List<ObjectReference> instances = matchingClass.instances(0);
             for (ObjectReference instance : instances) {
                 if (instance.uniqueID() == uniqueRefID) {
                     objectFound = instance;
                     break;
                 }
             }
 
             if (objectFound != null) {
                 break;
             }
         }
 
         if (objectFound == null) {
             System.err.println("Can't find object with given ID, either class unloaded or wrong ID");
             return null;
         }
 
         /* Process the heap object*/
         Set<String> classes = finder.getAllClasses().keySet();
 
         return ValueProcessor.processValueFull(objectFound, classes);
     }
 
     /**
      * Given a locatable event, we extract all the stack frames up until this
      * point in the execution. (note vm should be suspended atm)
      *
      * @param event
      * @return
      */
     private List<StackFrameMeta> getStacks(LocatableEvent event)
             throws
             IncompatibleThreadStateException, ClassNotLoadedException
              {
         /* Get the thread in which we're stepping */
         ThreadReference threadRef = event.thread();
 
         System.err.println("-------------");
         System.err.println("Number of Frames: " + threadRef.frameCount());
 
         List<StackFrameMeta> stackFrames = new
                 LinkedList<StackFrameMeta>();
 
         List<StackFrame> frames = threadRef.frames();
         int frameCount = 1;
         for (StackFrame frame : frames) {
 
             System.err.println("Frame: " + frameCount);
             frameCount++;
 
             /* Get some information about the stack frame */
             String methodName = frame.location().method().name();
             String className = frame.location().declaringType().name();
             try {
                 List<JVMValue> stack = this.getStack(frame);
                 StackFrameMeta meta
                         = new StackFrameMeta(methodName, className, stack);
 
                 /* Add to the front of the queue */
                 stackFrames.add(0, meta);
             } catch (AbsentInformationException e) {
                 /* Send an event to other side of Deeva */
                 dispatcher.absent_information_event(className);
                 /* TODO: Do something else, we can't continue */
             }
         }
         return stackFrames;
     }
 
     private List<JVMValue> getStack(StackFrame stackFrame) throws
             AbsentInformationException, ClassNotLoadedException
     {
         List<JVMValue> localVariables = new LinkedList<JVMValue>();
 
         /* List all the variables on the stack */
         for (LocalVariable var : stackFrame.visibleVariables()) {
             String name = var.name();
             Type type = var.type();
 
             Value variableValue = stackFrame.getValue(var);
             System.err.println("-------------");
             System.err.println("Name: " + name);
             System.err.println("Type: " + type.name());
 
             /* Get an overview for the variable */

             JVMValue jvmValue
                 = ValueProcessor.processVariable(var, variableValue,
                                                  finder.getAllClasses()
                                                          .keySet());
 
             localVariables.add(jvmValue);
         }
 
         return localVariables;
     }
 
     public DeevaState run() throws InterruptedException {
         vm.resume();
         state = State.RUNNING;
         sema.acquire();
         return getState();
     }
 
     public DeevaState getState() {
         DeevaStateBuilder dsb = new DeevaStateBuilder();
         dsb.setState(state);
         dsb.setLineNumber(line_number);
         dsb.setStacks(stacks);
         dsb.setCurrentClass(currentClass);
         dsb.setArguments(programArgs);
         dsb.setEa(enableAssertions);
         DeevaState deevaState = dsb.create();
 
         /* TODO: rename function to send state, and make it return void */
         dispatcher.stack_heap_object_event(deevaState.getStacks(), null);
         dispatcher.suspended_event(deevaState);
         return deevaState;
     }
 
     public DeevaState stepInto() throws InterruptedException {
         if (state != State.STASIS) {
             throw new WrongStateError("Should be in STASIS state.");
         }
         step(StepRequest.STEP_INTO);
         sema.acquire();
         return getState();
     }
 
     public DeevaState stepReturn() throws InterruptedException {
         if (state != State.STASIS) {
             throw new WrongStateError("Should be in STASIS state.");
         }
         step(StepRequest.STEP_OUT);
         sema.acquire();
         return getState();
     }
 
     public DeevaState stepOver() throws InterruptedException {
         if (state != State.STASIS) {
             throw new WrongStateError("Should be in STASIS state.");
         }
         step(StepRequest.STEP_OVER);
         sema.acquire();
         return getState();
     }
 
     public boolean setBreakpoint(String clas, int lineNum) {
         Breakpoint bkpt = new Breakpoint(clas, lineNum);
 
         // If the breakpoint exists return true.
         if (breakpoints.keySet().contains(bkpt)) {
             return true;
         }
 
         try {
             BreakpointRequest req = attemptToSetBreakpoint(clas, lineNum);
             breakpoints.put(bkpt, req);
             return true;
         } catch (NoVMException error) {
             System.err.println("No vm loaded, saving breakpoint for later.");
             breakpoints.put(bkpt, null);
             return true;
         } catch (NoLoadedClassException error) {
             System.err.println("No class loaded, saving breakpoint for later.");
             breakpoints.put(bkpt, null);
             return true;
         } catch (NoLocationException error) {
             /* The VM exists and the class was loaded but we can't set a
                breakpoint here. */
             return false;
         } catch (AbsentInformationException error) {
             System.err.println("Absent Information!");
             dispatcher.absent_information_event(clas);
             return false;
         }
     }
 
     public boolean unsetBreakpoint(String clas, int lineNum) {
         Breakpoint bkpt = new Breakpoint(clas, lineNum);
         if (breakpoints.containsKey(bkpt)) {
             BreakpointRequest req = breakpoints.remove(bkpt);
             if (req != null) {
                 EventRequestManager mgr = vm.eventRequestManager();
                 mgr.deleteEventRequest(req);
             }
             return true;
         }
         return false;
     }
 
     public Set<Breakpoint> getBreakpoints() {
         return breakpoints.keySet();
     }
 
     private void step(int depth) {
         EventRequestManager reqMgr = vm.eventRequestManager();
         StepRequest stepRequest = reqMgr.createStepRequest(getThread(),
                 StepRequest.STEP_LINE, depth);
 
         /* Don't step into excluded files i.e. system library */
         for (String exclude : excludes) {
             stepRequest.addClassExclusionFilter(exclude);
         }
 
         stepRequest.enable();
         vm.resume();
     }
 
     @Override
     public void handleEvent(Event e)
             throws IncompatibleThreadStateException,
             ClassNotLoadedException
     {
         System.err.println(e.getClass());
         if (e instanceof LocatableEvent && !(e instanceof MethodEntryEvent)) {
             locatableEvent((LocatableEvent)e);
         }
         super.handleEvent(e);
     }
 
     private void locatableEvent(LocatableEvent e) {
         Location location = e.location();
         this.line_number = location.lineNumber();
         this.currentClass = location.declaringType().name();
     }
 
     @Override
     public void classPrepareEvent(ClassPrepareEvent e) {
         attemptToSetWaitingBreakpoints();
         vm.resume();
     }
 
     private void attemptToSetWaitingBreakpoints() {
         for (Breakpoint b : breakpoints.keySet()) {
             if (breakpoints.get(b) == null) {
                 String className = b.getClas();
                 System.err.println("Attempting to set saved breakpoint.");
                 try {
                     BreakpointRequest req = attemptToSetBreakpoint(className,
                             b.getLineNumber());
                     breakpoints.put(b, req);
                 } catch (NoVMException error) {
                     System.err.println("1");
                     // Ignore.
                 } catch (NoLoadedClassException error) {
                     System.err.println("2");
                     // Ignore.
                 } catch (NoLocationException error) {
                     System.err.println("3");
                     // Ignore this.
                 } catch (AbsentInformationException error) {
                     System.err.println("Absent Information!");
                     dispatcher.absent_information_event(className);
                     /* TODO: Do something else, we can't continue */
                 }
             }
         }
     }
 
     private BreakpointRequest attemptToSetBreakpoint(String clas, int lineNum) throws
             NoVMException,
             AbsentInformationException,
             NoLoadedClassException,
             NoLocationException
     {
         if (state == State.NO_INFERIOR) { throw new NoVMException(); }
 
         List<ReferenceType> classes = vm.classesByName(clas);
         if (classes.size() < 1) { throw new NoLoadedClassException(); }
         // XXX: test with emmbeded classes...
         ReferenceType classRef = classes.get(0);
 
         List<Location> locs = classRef.locationsOfLine(lineNum);
         if (locs.size() < 1) { throw new NoLocationException(); }
         Location loc = locs.get(0);
         EventRequestManager reqMgr = vm.eventRequestManager();
 
         BreakpointRequest req = reqMgr.createBreakpointRequest(loc);
         req.enable();
         return req;
     }
 
     @Override
     public void stepEvent(StepEvent event)
             throws IncompatibleThreadStateException,
             ClassNotLoadedException
     {
         System.err.println(event.location().method() + "@" + event.location().lineNumber());
         stacks = getStacks(event);
         /* Delete the request */
         getRequestManager().deleteEventRequest(event.request());
         sema.release();
     }
 
     @Override
     public void breakpointEvent(BreakpointEvent event) throws ClassNotLoadedException, IncompatibleThreadStateException {
         System.err.println(event.location().method() + "@" + event.location().lineNumber());
 
         /* Try to extract the stack variables */
         state = State.STASIS;
         stacks = getStacks(event);
         sema.release();
     }
 
     @Override
     public void methodEntryEvent(MethodEntryEvent event) {
         /* Tailored towards the other thing, refactor later (observer) */
         try {
             Method method = event.method();
 
             final ThreadReference thread = event.thread();
             final ObjectReference bisRef = thread.frame(0).thisObject();
 
             /* Skip any method we're not interested in i.e. read */
             if (!method.equals(this.sysInReadMethod)
                     || !bisRef.equals(this.systemInObj)) {
                 vm.resume();
                 return;
             }
 
             final Method availableMethod = this.sysInAvailableMethod;
 
             /* See if we need to get more data from the user */
 
             Runnable vmInvoker = new Runnable() {
                 /* This should run in a new thread to avoid deadlock,
                 i.e. let the event handler finish its loop and go back to
                 expecting the next event. This is described in more detail in
                  the JDI spec */
                 @Override
                 public void run() {
                     IntegerValue available;
                     try {
                         available = (IntegerValue)bisRef
                                 .invokeMethod(thread, availableMethod,
                                         new LinkedList<Value>(), 0);
                         int bytesAvailable = available.value();
                         System.err.println("JDI - Available Bytes: " +
                                 bytesAvailable);
 
                         if (bytesAvailable == 0) {
                             /* Change the state that we're in */
                             state = State.AWAITING_IO;
                             dispatcher.awaiting_io_event();
                             /* Release the semaphore that is being held */
                             sema.release();
                         }
 
                     } catch (InvalidTypeException e) {
                         System.err.println("EXCEPTION: ite");
                         System.err.println(e.getMessage());
                         e.printStackTrace();
                     } catch (ClassNotLoadedException e) {
                         System.err.println("EXCEPTION: cnl");
                         System.err.println(e.getMessage());
                         e.printStackTrace();
                     } catch (IncompatibleThreadStateException e) {
                         System.err.println("EXCEPTION: its");
                         System.err.println(e.getMessage());
                         e.printStackTrace();
                     } catch (InvocationException e) {
                         System.err.println("EXCEPTION: i");
                         System.err.println(e.getMessage());
                         e.printStackTrace();
                     }
 
                     vm.resume();
                 }
             };
 
             /* Start this in a new thread */
             Thread t = new Thread(vmInvoker);
             t.start();
         } catch (IncompatibleThreadStateException e) {
             System.err.println(e);
             vm.resume();
         }
     }
 
     @Override
     public void exceptionEvent(ExceptionEvent event) {
         System.err.println("EXCEPTION");
         cleanUp();
     }
 
     @Override
     public void threadDeathEvent(ThreadDeathEvent event) {
         System.err.println("THREAD_DEATH");
         cleanUp();
     }
 
     @Override
     public void vmDeathEvent(VMDeathEvent event) {
         System.err.println("DEATH");
         cleanUp();
     }
 
     private EventRequestManager getRequestManager() {
         return vm.eventRequestManager();
     }
 
     private void cleanUp() {
         for (Breakpoint b : breakpoints.keySet()) {
             breakpoints.put(b, null);
         }
 
         try {
             errThread.join();
             outThread.join();
         } catch (InterruptedException e) {
             System.err.println("Could not get all output.");
         }
         state = State.NO_INFERIOR;
         sema.release();
     }
 
     public String getStateName() {
         return state.toString();
     }
 
     private ThreadReference getThread() {
         // We only handle the main thread.
         for (ThreadReference ref : vm.allThreads()) {
             if (ref.name().equals("main")) {
                 return ref;
             }
         }
         throw new Error("No main thread.");
     }
 
     // XXX: Refactor beneath this line... and above this line...
 
     /** Implements same semantics as StringUtils.join() without the dependency
     on the library */
     private static String stringListJoin(List<String> list, String delimiter) {
         if (list == null) {
             return null;
         }
 
         Object[] strList = list.toArray();
         StringBuilder sb = new StringBuilder();
 
         for (int i = 0; i < strList.length; i++) {
             String elem = (String)strList[i];
             if (elem == null || elem.equals("")) {
                 continue;
             }
 
             sb.append(elem);
 
             /* Don't append the delimiter if we're on the last item */
             if (i != strList.length - 1) {
                 sb.append(delimiter);
             }
         }
 
         return sb.toString();
     }
 
     public static void main(String[] args) {
         System.out.println("Null List:");
         System.out.println(Debug.stringListJoin(null, "-"));
 
         System.out.println("Empty List:");
         String[] a = {};
         List<String> list = new LinkedList<String>(Arrays.asList(a));
         System.out.println(Debug.stringListJoin(list, "-"));
 
         System.out.println("List with null elem");
         a = new String[]{null};
         list = new LinkedList<String>(Arrays.asList(a));
         System.out.println(Debug.stringListJoin(list, "-"));
 
         System.out.println("List with normal elems");
         a = new String[]{"a", "b", "c"};
         list = new LinkedList<String>(Arrays.asList(a));
         System.out.println(Debug.stringListJoin(list, "-"));
 
         System.out.println("List with null and normal elems");
         a = new String[]{"a", null, "c", "d"};
         list = new LinkedList<String>(Arrays.asList(a));
         System.out.println(Debug.stringListJoin(list, "-"));
     }
 
 
     private VirtualMachine launchTarget(String programName,
                                         List<String> programArgs) {
         System.err.println("finding launching connector");
         LaunchingConnector connector = findLaunchingConnector();
         String programArgsString = Debug.stringListJoin(programArgs, " ");
         String mainString = programName + " " + programArgsString;
         System.err.println("Final main string: " + mainString);
         Map<String, Connector.Argument> arguments = connectorArguments
                 (connector, mainString, enableAssertions);
 
         try {
             System.err.println("beginning launch");
             return connector.launch(arguments);
         } catch (IOException exc) {
             throw new Error("Unable to launch target VM: " + exc);
         } catch (IllegalConnectorArgumentsException exc) {
             throw new Error("Internal error: " + exc);
         } catch (VMStartException exc) {
             throw new Error("Target VM failed to initialize: " + exc.getMessage());
         }
     }
 
     private void redirectOutput() {
         Process process = vm.process();
 
         errThread = new StreamRedirectThread("stderr",
                 process.getErrorStream(),
                 this.outQueue, new OutputDispatcher() {
             @Override
             public void dispatchOutput(String s) {
                 dispatcher.stderr(s);
             }
         });
 
         outThread = new StreamRedirectThread("stdout",
                 process.getInputStream(),
                 this.outQueue, new OutputDispatcher() {
             @Override
             public void dispatchOutput(String s) {
                 dispatcher.stdout(s);
             }
         });
 
         inThread = new StdInRedirectThread("stdin",
                 process.getOutputStream(), this.inQueue);
 
         outThread.start();
         errThread.start();
         inThread.start();
     }
 
     LaunchingConnector findLaunchingConnector() {
         List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
         for (Connector connector : connectors) {
             if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
                 return (LaunchingConnector)connector;
             }
         }
         throw new Error("No launching connector");
     }
 
     /**
      * Return the launching connector's arguments.
      */
     Map<String, Connector.Argument> connectorArguments(LaunchingConnector
                                                                connector,
                                                        String mainArgs,
                                                        boolean ea) {
         Map<String, Connector.Argument> arguments = connector.defaultArguments();
 
         /* Set the options argument, where -classpath and -ea is passed in */
         StringBuilder optionsSB = new StringBuilder();
         Connector.Argument optionArg = arguments.get("options");
 
         if (optionArg == null) {
             throw new Error("Bad launching connector");
         }
 
         if (ea) {
             optionsSB.append("-ea ");
         }
 
         if (this.classPaths != null) {
             optionsSB.append("-cp ").append(this.classPaths).append(" ");
         }
 
         optionArg.setValue(optionsSB.toString());
 
         /* Main Arg */
         Connector.Argument mainArg = arguments.get("main");
         if (mainArg == null) {
             throw new Error("Bad launching connector");
         }
 
         mainArg.setValue(mainArgs);
 
         return arguments;
     }
 }
