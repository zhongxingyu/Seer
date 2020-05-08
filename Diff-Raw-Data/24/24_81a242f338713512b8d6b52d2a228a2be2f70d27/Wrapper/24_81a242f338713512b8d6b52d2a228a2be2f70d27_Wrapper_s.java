 package capstone.wrapper;
 
 import static capstone.wrapper.DebuggerCommand.*;
 import capstone.util.*;
 import org.json.simple.*;
 import java.util.*;
 import java.util.concurrent.*;
 import java.io.*;
 
 public abstract class Wrapper extends Thread
 {
    private Object requestLock;
     private DebuggerRequest request;
     private Object activeLock;
     private boolean active;
 
     public Wrapper()
     {
         requestLock = new Object();
         request = null;
         activeLock = new Object();
         active = false;
     }
 
     /**
      * prepare is called to compile (or load) a program and prepare the debugger for use.
      * @param   programText     the text of the program to prepare
      * @return  an empty list if it prepared successfully, a list of errors if the program was bad, or null if we had some other error
      */
     protected abstract List<ProgramError> prepare(String programText) throws IOException, InterruptedException;
 
     /**
      * killDebugger is called to kill the debugger process.
      * Note: killDebugger should function correctly even if it is called multiple times in a row.
      */
     protected abstract void killDebugger();
 
     /**
      * cleanup is called to remove all files or system resources the wrapper allocated.
      */
     protected abstract void cleanup(); // TODO require a cleanup method
 
     /**
      * runProgram is called to run the program from the main entry point, until an error or a breakpoint.
      * TODO set return to a ProgramStatus object which contains the current line and whether it has restarted
      */
     protected abstract void runProgram() throws IOException;
 
     /**
      * getStdOut fetches the current standard output from the program.
      * The output is cleared after this call, so the user must buffer the partial results to get the complete result.
      * @return  the stdout output of the user program since the last call to getStdOut()
      */
     protected abstract String getStdOut() throws IOException;
 
     /**
      * getStdErr fetches the current standard error output from the program.
      * The output is cleared after this call, so the user must buffer the partial results to get the complete result.
      * @return  the stderr output of the user program since the last call to getStdErr()
      */
     //public abstract String getStdErr() throws IOException; // TODO add this
 
     /**
      * provideInput pushes input into the file which is fed to the user program.
      * It may be used successfully any time before the user program reaches an input line.
      * Note: it does not add newlines, so you should add those if you need them.
      * @param   input   the input string to feed into the use program
      */
     public abstract void provideInput(String input) throws IOException;
 
     /**
      * getLocalValues will return a StackFrame object containing the current stackframe.
      * This includes all the local variables.
      * @return  the current stackframe
      */
     protected abstract StackFrame getLocalValues() throws IOException;
 
     /**
      * getStack will return a list of all the stackframes of the user program.
      * @return  the entire stack
      */
     protected abstract List<StackFrame> getStack() throws IOException;
 
     /**
      * evaluateExpression evaluates a given expression in the debugger and returns the result.
      * @param   expression  the expression to evaluate
      * @return  the result of the expression, or null if it was an invalid expression
      */
     protected abstract String evaluateExpression(String expression) throws IOException;
 
     /**
      * stepIn will perform a single step; it will step into a function if possible.
      * TODO set return to a ProgramStatus object which contains the current line and whether it has restarted
      */
     protected abstract void stepIn() throws IOException;
 
     /**
      * stepOut will run until it exits the current function.
      * TODO set return to a ProgramStatus object which contains the current line and whether it has restarted
      */
     protected abstract void stepOut() throws IOException;
 
     /**
      * stepOver will perform a single step; it will not enter functions.
      * TODO set return to a ProgramStatus object which contains the current line and whether it has restarted
      */
     protected abstract void stepOver() throws IOException;
 
     /**
      * addBreakpoint sets a breakpoint at the specified line.
      * TODO figure out what the expected behavior is if the breakpoint is not valid.
      * @param   lineNumber  the line to break on
      */
     protected abstract void addBreakpoint(int lineNumber) throws IOException;
 
     /**
      * Fetches the current line number of the program.
      * @return  the current line of the program
      */
     protected abstract int getLineNumber() throws IOException;
 
     /**
      * Submits a request for a command to the wrapper.
      * @return  true if it is accepted and false if the wrapper is currently processing a command
      */
     public boolean submitRequest(DebuggerRequest request)
     throws InterruptedException
     {
         synchronized (requestLock)
         {
             if (this.request == null && request != null)
             {
                 this.request = request;
                 System.out.println("[gdb] Notifying to wake up the wrapper!");
                 requestLock.notify();
                 System.out.println("[gdb] Notified to wake up the wrapper!");
                 return true;
             }
             else
             {
                 return false;
             }
         }
     }
 
     /**
      * @return  true if the wrapper is currently processing a command
      */
     public boolean waiting()
     {
         synchronized (requestLock)
         {
             return (request != null);
         }
     }
 
     /**
      * Clears the current request so that a new one can be submitted.
      * This command will notify the daemon, since it is waiting on the request.
      */
     private void clearRequest()
     {
         synchronized (requestLock)
         {
             System.out.println("[gdb] Notifying the daemon!");
             synchronized (request.monitor)
             {
                 request.monitor.notifyAll();
             }
             System.out.println("[gdb] Notified the daemon!");
             request = null;
         }
     }
 
     /**
      * shutdown() makes the entire thread shut down and cleans up its resources.
      */
     public void shutdown()
     {
         setActive(false);
         this.interrupt();
     }
 
     /**
      * This indicates whether the wrapper is active or not.
      * If it is not active, a new wrapper needs to be created to handle requests.
      * @return  whether or not the wrapper is running
      */
     public boolean isActive()
     {
         synchronized (activeLock)
         {
             return active;
         }
     }
 
     /**
      * Threadsafe setter of the active status.
      * @param   active  the status to set it to
      */
     private void setActive(boolean active)
     {
         synchronized (activeLock)
         {
             this.active = active;
         }
     }
 
     /**
      * This is the main activity loop for the wrapper.
      * It will loop until there is a non-recoverable error, it receives a killDebugger() commmand,
      * or it is forcibly interrupted. When it shuts down, it will clean up its resources.
      */
     @Override
     public void run()
     {
         System.out.println("[gdb] Starting the thread!");
         setActive(true);
         activeLoop: while (isActive())
         {
             System.out.println("[gdb] entering request lock block");
             synchronized (requestLock)
             {
                 while (request == null)
                 {
                     try
                     {
                         System.out.println("[gdb] waiting for request lock");
                         requestLock.wait();
                         System.out.println("[gdb] woken up");
                     }
                     catch (InterruptedException exception)
                     {
                         System.out.println("[gdb] exception!");
                         if (isActive())
                         {
                             continue;
                         }
                         else
                         {
                             // To whoever reads this: I'm sorry.
                             // I decided to use a label and I
                             // should be punished.  --ntietz
                             break activeLoop;
                         }
                     }
                 }
 
                 try
                 {
                     JSONObject jsonResult = new JSONObject();
                     request.result = "";
                     switch (request.command)
                     {
                         case PREPARE:
                             System.out.println("[gdb] Handling a prepare command!");
                             List<ProgramError> errors = prepare(request.data);
                             JSONArray jsonErrorList = new JSONArray();
                             for (ProgramError each : errors)
                             {
                                 JSONObject jsonEach = new JSONObject();
                                 jsonEach.put("linenumber", "" + each.lineNumber);
                                 jsonEach.put("errortext", each.errorText);
                                 jsonErrorList.add(jsonEach);
                             }
                             jsonResult.put("errors", jsonErrorList);
                             System.out.println("[gdb] Results: " + request.result);
                             break;
 
                         case RUN:
                             runProgram();
                             break;
 
                         case STEPIN:
                             stepIn();
                             break;
 
                         case STEPOUT:
                             stepOut();
                             break;
 
                         case STEPOVER:
                             stepOver();
                             break;
 
                         case GETVALUES:
                             // TODO this hasn't been implemented yet...
                             request.result = "";
                             break;
 
                         case GETSTDOUT:
                             String output = getStdOut();
                             System.out.println("[gdb] Output was: " + output);
                             jsonResult.put("stdout", output);
                             break;
 
                         case GETSTDERR:
                             // TODO this hasn't been implemented yet...
                             break;
 
                         //note: GIVEINPUT case is not handled because
                         // it should be handled by the daemon itself
 
                         case ADDBREAKPOINT:
                             try
                             {
                                 addBreakpoint(Integer.parseInt(request.data));
                             }
                             catch (NumberFormatException badFormatException)
                             {
                                 request.result = "Error: invalid line number";
                             }
                             break;
 
                         case GETLINENUMBER:
                             System.out.println("[gdb] Fetching line number");
                             int lineNumber = getLineNumber();
                             jsonResult.put("linenumber", lineNumber);
                             request.result = String.valueOf(lineNumber);
                             break;
 
                         case KILLDEBUGGER:
                             setActive(false);
                             killDebugger();
                             break;
 
                         case UNKNOWN:
                             break;
                     }
                     request.result = jsonResult.toString();
                 }
                 catch (IOException exception)
                 {
                     request.result = null;
                     setActive(false);
                     killDebugger();
                 }
                 catch (InterruptedException exception)
                 {
                     request.result = null;
                     setActive(false);
                     killDebugger();
                 }
             }
 
             clearRequest();
         } 
         killDebugger();
         cleanup();
     }
 }
 
