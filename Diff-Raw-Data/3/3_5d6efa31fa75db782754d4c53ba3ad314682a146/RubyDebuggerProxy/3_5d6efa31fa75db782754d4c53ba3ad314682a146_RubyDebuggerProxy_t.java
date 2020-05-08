 /*
  * Copyright (c) 2007-2008, debug-commons team
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package org.rubyforge.debugcommons;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.rubyforge.debugcommons.model.ExceptionSuspensionPoint;
 import org.rubyforge.debugcommons.model.IRubyBreakpoint;
 import org.rubyforge.debugcommons.model.IRubyExceptionBreakpoint;
 import org.rubyforge.debugcommons.model.IRubyLineBreakpoint;
 import org.rubyforge.debugcommons.model.SuspensionPoint;
 import org.rubyforge.debugcommons.model.RubyThreadInfo;
 import org.rubyforge.debugcommons.model.RubyDebugTarget;
 import org.rubyforge.debugcommons.model.RubyFrame;
 import org.rubyforge.debugcommons.model.RubyFrameInfo;
 import org.rubyforge.debugcommons.model.RubyThread;
 import org.rubyforge.debugcommons.model.RubyVariable;
 import org.rubyforge.debugcommons.model.RubyVariableInfo;
 
 public final class RubyDebuggerProxy {
 
     private static final Logger LOGGER = Logger.getLogger(RubyDebuggerProxy.class.getName());
 
     public static enum DebuggerType { CLASSIC_DEBUGGER, RUBY_DEBUG }
     
     public static final DebuggerType CLASSIC_DEBUGGER = DebuggerType.CLASSIC_DEBUGGER;
     public static final DebuggerType RUBY_DEBUG = DebuggerType.RUBY_DEBUG;
     
     public static final List<RubyDebuggerProxy> PROXIES = new CopyOnWriteArrayList<RubyDebuggerProxy>();
     
     private final List<RubyDebugEventListener> listeners;
     private final Map<Integer, IRubyLineBreakpoint> breakpointsIDs;
     private final int timeout;
     
     private final DebuggerType debuggerType;
     private RubyDebugTarget debugTarget;
     private Socket commandSocket;
     private boolean finished;
     
     private PrintWriter commandWriter;
     private ICommandFactory commandFactory;
     private ReadersSupport readersSupport;
     
     private boolean supportsCondition;
     
     // catchpoint removing is not supported by backend yet, handle it in the
     // debug-commons-java until the support is added
     // http://rubyforge.org/tracker/index.php?func=detail&aid=20237&group_id=1900&atid=7436
     private Set<String> removedCatchpoints;
     
     public RubyDebuggerProxy(final DebuggerType debuggerType) {
         this(debuggerType, 10); // default reading timeout 10s
     }
     
     public RubyDebuggerProxy(final DebuggerType debuggerType, final int timeout) {
         this.debuggerType = debuggerType;
         this.listeners = new CopyOnWriteArrayList<RubyDebugEventListener>();
         this.breakpointsIDs = new HashMap<Integer, IRubyLineBreakpoint>();
         this.removedCatchpoints = new HashSet<String>();
         this.timeout = timeout;
         this.readersSupport = new ReadersSupport(timeout);
     }
     
     public void setDebugTarget(RubyDebugTarget debugTarget) throws IOException, RubyDebuggerException {
         this.debugTarget = debugTarget;
         LOGGER.fine("Proxy target: " + debugTarget);
     }
     
     public RubyDebugTarget getDebugTarget() {
         return debugTarget;
     }
     
     /** <b>Package-private</b> for unit tests only. */
     ReadersSupport getReadersSupport() {
         return readersSupport;
     }
     
     /**
      * Set initial breakpoints and start the debugging process stopping (and
      * firing event to the {@link #addRubyDebugEventListener}) on the first
      * breakpoint.
      *
      * @param initialBreakpoints initial set of breakpoints to be set before
      *        triggering the debugging
      */
     public void attach(final IRubyBreakpoint[] initialBreakpoints) throws RubyDebuggerException {
         try {
             switch(debuggerType) {
             case CLASSIC_DEBUGGER:
                 attachToClassicDebugger(initialBreakpoints);
                 break;
             case RUBY_DEBUG:
                 attachToRubyDebug(initialBreakpoints);
                 break;
             default:
                 throw new IllegalStateException("Unhandled debugger type: " + debuggerType);
             }
         } catch (RubyDebuggerException e) {
             PROXIES.remove(this);
             throw e;
         }
         startSuspensionReaderLoop();
     }
 
 
     /**
      * Whether client might send command to the proxy. When the debuggee has
      * finished (in a standard manner or unexpectedly, e.g. was killed) or the
      * proxy did not start yet, false is returned.
      */
     public synchronized boolean isReady() {
         return !finished && commandWriter != null && debugTarget.isAvailable();
     }
 
     private synchronized void attachToClassicDebugger(final IRubyBreakpoint[] initialBreakpoints) throws RubyDebuggerException {
         try {
             commandFactory = new ClassicDebuggerCommandFactory();
             readersSupport.startCommandLoop(getCommandSocket().getInputStream());
             commandWriter = new PrintWriter(getCommandSocket().getOutputStream(), true);
             setBreakpoints(initialBreakpoints);
             sendCommand("cont");
         } catch (IOException ex) {
             throw new RubyDebuggerException(ex);
         }
     }
 
     private synchronized void attachToRubyDebug(final IRubyBreakpoint[] initialBreakpoints) throws RubyDebuggerException {
         try {
             commandFactory = new RubyDebugCommandFactory();
             readersSupport.startCommandLoop(getCommandSocket().getInputStream());
             commandWriter = new PrintWriter(getCommandSocket().getOutputStream(), true);
             setBreakpoints(initialBreakpoints);
             sendCommand("start");
         } catch (IOException ex) {
             throw new RubyDebuggerException(ex);
         }
     }
     
     public void fireDebugEvent(final RubyDebugEvent e) {
         for (RubyDebugEventListener listener : listeners) {
             listener.onDebugEvent(e);
         }
     }
     
     public void addRubyDebugEventListener(final RubyDebugEventListener listener) {
         listeners.add(listener);
     }
     
     public void removeRubyDebugEventListener(final RubyDebugEventListener listener) {
         listeners.remove(listener);
     }
     
     private PrintWriter getCommandWriter() throws RubyDebuggerException {
         assert commandWriter != null : "Proxy has to be started, before using the writer";
         return commandWriter;
     }
 
     protected void setBreakpoints(final IRubyBreakpoint[] breakpoints) throws RubyDebuggerException {
         for (IRubyBreakpoint breakpoint: breakpoints) {
             addBreakpoint(breakpoint);
         }
     }
     
     public synchronized void addBreakpoint(final IRubyBreakpoint breakpoint) {
         LOGGER.fine("Adding breakpoint: " + breakpoint);
         if (!isReady()) {
             LOGGER.fine("Session and/or debuggee is not ready, skipping addition of breakpoint: " + breakpoint);
             return;
         }
         assert breakpoint != null : "breakpoint cannot be null";
         if (breakpoint.isEnabled()) {
             try {
                 if (breakpoint instanceof IRubyLineBreakpoint) {
                     IRubyLineBreakpoint lineBreakpoint = (IRubyLineBreakpoint) breakpoint;
                     String command = commandFactory.createAddBreakpoint(
                             lineBreakpoint.getFilePath(), lineBreakpoint.getLineNumber());
                     sendCommand(command);
                     Integer id = getReadersSupport().readAddedBreakpointNo();
                     String condition = lineBreakpoint.getCondition();
                     if (condition != null && supportsCondition) {
                         command = commandFactory.createSetCondition(id, condition);
                         if (command != null) {
                             sendCommand(command);
                             getReadersSupport().readConditionSet(); // read response
                         } else {
                             LOGGER.info("conditional breakpoints are not supported by backend");
                         }
                     }
                     breakpointsIDs.put(id, lineBreakpoint);
                 } else if (breakpoint instanceof IRubyExceptionBreakpoint) {
                     IRubyExceptionBreakpoint excBreakpoint = (IRubyExceptionBreakpoint) breakpoint;
                     // just 're-enable' if contained in removedCatchpoints
                     if (!removedCatchpoints.remove(excBreakpoint.getException())) {
                         String command = commandFactory.createCatchOn(excBreakpoint);
                         sendCommand(command);
                         getReadersSupport().readCatchpointSet(); // read response
                     }
                 } else {
                     throw new IllegalArgumentException("Unknown breakpoint type: " + breakpoint);
                 }
             } catch (final RubyDebuggerException ex) {
                 if (isReady()) {
                     LOGGER.log(Level.WARNING, "Cannot add breakpoint to: " + getDebugTarget(), ex);
                 }
             }
         }
     }
     
     public synchronized void removeBreakpoint(final IRubyBreakpoint breakpoint) {
         removeBreakpoint(breakpoint, false);
     }
     
     /**
      * Remove the given breakpoint from this debugging session.
      *
      * @param breakpoint breakpoint to be removed
      * @param silent whether info message should be omitted if the breakpoint
      *        has not been set in this session
      */
     public synchronized void removeBreakpoint(final IRubyBreakpoint breakpoint, boolean silent) {
         LOGGER.fine("Removing breakpoint: " + breakpoint);
         if (!isReady()) {
             LOGGER.fine("Session and/or debuggee is not ready, skipping removing of breakpoint: " + breakpoint);
             return;
         }
         if (breakpoint instanceof IRubyLineBreakpoint) {
             IRubyLineBreakpoint lineBreakpoint = (IRubyLineBreakpoint) breakpoint;
             Integer id = findBreakpointId(lineBreakpoint);
             if (id != null) {
                 String command = commandFactory.createRemoveBreakpoint(id);
                 try {
                     sendCommand(command);
                     getReadersSupport().waitForRemovedBreakpoint(id);
                     breakpointsIDs.remove(id);
                     LOGGER.fine("Breakpoint " + breakpoint + " with id " + id + " successfully removed");
                 } catch (RubyDebuggerException e) {
                     LOGGER.log(Level.SEVERE, "Exception during removing breakpoint.", e);
                 }
             } else if (!silent) {
                 LOGGER.fine("Breakpoint [" + breakpoint + "] cannot be removed since " +
                         "its ID cannot be found. Might have been already removed.");
             }
         } else if (breakpoint instanceof IRubyExceptionBreakpoint) {
             // catchpoint removing is not supported by backend yet, handle in
             // the debug-commons-java until the support is added
             // http://rubyforge.org/tracker/index.php?func=detail&aid=20237&group_id=1900&atid=7436
             IRubyExceptionBreakpoint catchpoint = (IRubyExceptionBreakpoint) breakpoint;
             removedCatchpoints.add(catchpoint.getException());
         } else {
             throw new IllegalArgumentException("Unknown breakpoint type: " + breakpoint);
         }
     }
     
     /**
      * Update the given breakpoint. Use when <em>enabled</em> property has
      * changed.
      */
     public void updateBreakpoint(IRubyBreakpoint breakpoint) throws RubyDebuggerException {
         removeBreakpoint(breakpoint, true);
         addBreakpoint(breakpoint);
     }
     
     /**
      * Find ID under which the given breakpoint is known in the current
      * debugging session.
      *
      * @return found ID; might be <tt>null</tt> if none is found
      */
     private synchronized Integer findBreakpointId(final IRubyLineBreakpoint wantedBP) {
         for (Iterator<Map.Entry<Integer, IRubyLineBreakpoint>> it = breakpointsIDs.entrySet().iterator(); it.hasNext();) {
             Map.Entry<Integer, IRubyLineBreakpoint> breakpointID = it.next();
             IRubyLineBreakpoint bp = breakpointID.getValue();
             int id = breakpointID.getKey();
             if (wantedBP.getFilePath().equals(bp.getFilePath()) &&
                     wantedBP.getLineNumber() == bp.getLineNumber()) {
                 return id;
             }
         }
         return null;
     }
     
     private void startSuspensionReaderLoop() {
         new SuspensionReaderLoop().start();
     }
     
     public Socket getCommandSocket() throws RubyDebuggerException {
         if (commandSocket == null) {
             commandSocket = attach();
         }
         return commandSocket;
     }
     
     public void resume(final RubyThread thread) {
         try {
             sendCommand(commandFactory.createResume(thread));
         } catch (RubyDebuggerException e) {
             LOGGER.log(Level.SEVERE, "resuming of " + thread.getId() + " failed", e);
         }
     }
     
     private void sendCommand(final String s) throws RubyDebuggerException {
         LOGGER.fine("Sending command debugger: " + s);
         if (!isReady()) {
             throw new RubyDebuggerException("Trying to send a command [" + s +
                     "] to non-started or finished proxy (debuggee: " + getDebugTarget() + ", output: \n\n" +
                     Util.dumpAndDestroyProcess(debugTarget));
         }
         getCommandWriter().println(s);
     }
 
     public void sendStepOver(RubyFrame frame, boolean forceNewLine) {
         try {
             if (forceNewLine) {
                 sendCommand(commandFactory.createForcedStepOver(frame));
             } else {
                 sendCommand(commandFactory.createStepOver(frame));
             }
         } catch (RubyDebuggerException e) {
             LOGGER.log(Level.SEVERE, "Stepping failed", e);
         }
     }
     
     public void sendStepReturnEnd(RubyFrame frame) {
         try {
             sendCommand(commandFactory.createStepReturn(frame));
         } catch (RubyDebuggerException e) {
             LOGGER.log(Level.SEVERE, "Stepping failed", e);
         }
     }
     
     public void sendStepIntoEnd(RubyFrame frame, boolean forceNewLine) {
         try {
             if (forceNewLine) {
                 sendCommand(commandFactory.createForcedStepInto(frame));
             } else {
                 sendCommand(commandFactory.createStepInto(frame));
             }
         } catch (RubyDebuggerException e) {
             LOGGER.log(Level.SEVERE, "Stepping failed", e);
         }
     }
     
     public RubyThreadInfo[] readThreadInfo() throws RubyDebuggerException {
         sendCommand(commandFactory.createReadThreads());
         return getReadersSupport().readThreads();
     }
     
     public RubyFrame[] readFrames(RubyThread thread) throws RubyDebuggerException {
         RubyFrameInfo[] infos;
         try {
             sendCommand(commandFactory.createReadFrames(thread));
             infos = getReadersSupport().readFrames();
         } catch (RubyDebuggerException e) {
             if (isReady()) {
                 throw e;
             }
             LOGGER.fine("Session and/or debuggee is not ready, returning empty thread list.");
             infos = new RubyFrameInfo[0];
         }
         RubyFrame[] frames = new RubyFrame[infos.length];
         for (int i = 0; i < infos.length; i++) {
             RubyFrameInfo info = infos[i];
             frames[i] = new RubyFrame(thread, info);
         }
         return frames;
     }
     
     public RubyVariable[] readVariables(RubyFrame frame) throws RubyDebuggerException {
         sendCommand(commandFactory.createReadLocalVariables(frame));
         RubyVariableInfo[] infos = getReadersSupport().readVariables();
         RubyVariable[] variables= new RubyVariable[infos.length];
         for (int i = 0; i < infos.length; i++) {
             RubyVariableInfo info = infos[i];
             variables[i] = new RubyVariable(info, frame);
         }
         return variables;
     }
     
     public RubyVariable[] readInstanceVariables(final RubyVariable variable) throws RubyDebuggerException {
         sendCommand(commandFactory.createReadInstanceVariable(variable));
         RubyVariableInfo[] infos = getReadersSupport().readVariables();
         RubyVariable[] variables= new RubyVariable[infos.length];
         for (int i = 0; i < infos.length; i++) {
             RubyVariableInfo info = infos[i];
             variables[i] = new RubyVariable(info, variable);
         }
         return variables;
     }
     
     public RubyVariable[] readGlobalVariables() throws RubyDebuggerException {
         sendCommand(commandFactory.createReadGlobalVariables());
         RubyVariableInfo[] infos = getReadersSupport().readVariables();
         RubyVariable[] variables= new RubyVariable[infos.length];
         for (int i = 0; i < infos.length; i++) {
             RubyVariableInfo info = infos[i];
             variables[i] = new RubyVariable(this, info);
         }
         return variables;
     }
     
     public RubyVariable inspectExpression(RubyFrame frame, String expression) throws RubyDebuggerException {
         expression = expression.replaceAll("\n", "\\\\n");
         sendCommand(commandFactory.createInspect(frame, expression));
         RubyVariableInfo[] infos = getReadersSupport().readVariables();
         return infos.length == 0 ? null : new RubyVariable(infos[0], frame);
     }
     
     public void finish(final boolean forced) {
         synchronized(this) {
             if (finished) {
                 // possible if client call this explicitly and then second time from RubyLoop
                 LOGGER.fine("Trying to finish the same proxy more than once: " + this);
                 return;
             }
             if (getDebugTarget().isRemote()) {
                 // TBD rather detach
                 sendExit();
             }
             PROXIES.remove(RubyDebuggerProxy.this);
             if (forced) {
                 sendExit();
                 try {
                     // Needed to let the IO readers to read the last pieces of input and
                     // output streams.
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                     LOGGER.log(Level.INFO, "Interrupted during IO readers waiting", e);
                 }
                 RubyDebugTarget target = getDebugTarget();
                 if (!target.isRemote()) {
                     LOGGER.fine("Destroying process: " + target);
                     target.getProcess().destroy();
                 }
             }
             finished = true;
         }
         fireDebugEvent(RubyDebugEvent.createTerminateEvent());
     }
     
     private synchronized void sendExit() {
         if (commandSocket != null && debugTarget.isAvailable()) {
             try {
                 sendCommand("exit");
             } catch (RubyDebuggerException ex) {
                 LOGGER.fine("'exit' command failed. Remote process? -> " + debugTarget.isRemote());
                 if (!debugTarget.isRemote()) {
                     LOGGER.fine("'exit' command failed. Process running? -> " + debugTarget.isRunning());
                 }
             }
         }
     }
     
     /**
      * Tries to attach to the <code>target</code>'s process and gives up in
      * <code>timeout</code> seconds.
      */
     private Socket attach() throws RubyDebuggerException {
         int port = debugTarget.getPort();
         String host = debugTarget.getHost();
         Socket socket = null;
         for (int tryCount = (timeout*2), i = 0; i < tryCount && socket == null; i++) {
             try {
                 socket = new Socket(host, port);
                 LOGGER.finest("Successfully attached to " + host + ':' + port);
             } catch (ConnectException e) {
                 synchronized (this) {
                     if (finished) { // terminated by frontend before process started
                         throw new RubyDebuggerException("Process was terminated before debugger connection was established.");
                     }
                     if (i == tryCount - 1) {
                         failWithInfo(e);
                     }
                 }
                 try {
                     if (debugTarget.isAvailable()) {
                         LOGGER.finest("Cannot connect to " + host + ':' + port + ". Trying again...(" + (tryCount - i - 1) + ')');
                         Thread.sleep(500);
                     } else {
                         failWithInfo(e);
                     }
                 } catch (InterruptedException e1) {
                     LOGGER.log(Level.SEVERE, "Interrupted during attaching.", e1);
                     Thread.currentThread().interrupt();
                 }
             } catch (IOException e) {
                 throw new RubyDebuggerException(e);
             }
         }
         return socket;
     }
 
     private void failWithInfo(ConnectException e) throws RubyDebuggerException {
         String info = debugTarget.isRemote()
                 ? "[Remote Process at " + debugTarget.getHost() + ':' + debugTarget.getPort() + "]"
                 : Util.dumpAndDestroyProcess(debugTarget);
        throw new RubyDebuggerException("Cannot connect to the debugged process at port "
                + debugTarget.getPort() + " in " + timeout + "s:\n\n" + info, e);
     }
 
     /**
      * Tells the proxy whether condition on breakpoint is supported. Older
      * engines version do not support it.
      */
     void setConditionSupport(final boolean supportsCondition) {
         this.supportsCondition = supportsCondition;
     }
 
     private class SuspensionReaderLoop extends Thread {
         
         SuspensionReaderLoop() {
             this.setName("RubyDebuggerLoop [" + System.currentTimeMillis() + ']');
         }
         
         public void suspensionOccurred(final SuspensionPoint hit) {
             new Thread() {
                 public @Override void run() {
                     debugTarget.suspensionOccurred(hit);
                 }
             }.start();
         }
         
         public @Override void run() {
             LOGGER.finest("Waiting for breakpoints.");
             while (true) {
                 SuspensionPoint sp = getReadersSupport().readSuspension();
                 if (sp == SuspensionPoint.END) {
                     break;
                 }
                 LOGGER.finest(sp.toString());
 
                 // see removedCatchpoints's JavaDoc
                 if (sp.isException()) {
                     ExceptionSuspensionPoint exceptionSP = (ExceptionSuspensionPoint) sp;
                     if (removedCatchpoints.contains(exceptionSP.getExceptionType())) {
                         RubyThread thread = getDebugTarget().getThreadById(sp.getThreadId());
                         if (thread != null) {
                             RubyDebuggerProxy.this.resume(thread);
                             continue;
                         }
                     }
                 }
                 if (!RubyDebuggerProxy.this.isReady()) { // flush events after proxy is finished
                     LOGGER.info("Session and/or debuggee is not ready, ignoring backend event - suspension point: " + sp);
                 } else {
                     SuspensionReaderLoop.this.suspensionOccurred(sp);
                 }
             }
             boolean unexpectedFail = getReadersSupport().isUnexpectedFail();
             if (unexpectedFail) {
                 LOGGER.warning("Unexpected fail. Debuggee: " + getDebugTarget() +
                         ", output: \n\n" + Util.dumpAndDestroyProcess(debugTarget));
             }
             finish(unexpectedFail);
             LOGGER.finest("Socket reader loop finished.");
         }
     }
     
 }
