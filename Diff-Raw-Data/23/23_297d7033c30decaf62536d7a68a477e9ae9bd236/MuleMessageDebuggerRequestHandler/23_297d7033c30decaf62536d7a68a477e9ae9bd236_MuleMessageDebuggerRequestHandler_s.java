 package org.mule.debugger.server;
 
 import org.mule.debugger.MuleDebuggingContext;
 import org.mule.debugger.commands.ICommand;
 import org.mule.debugger.request.IDebuggerRequest;
 import org.mule.debugger.response.IDebuggerResponse;
 
 import java.util.logging.Logger;
 
 public class MuleMessageDebuggerRequestHandler implements IDebuggerRequestHandler {
 
 
     private static Logger log = Logger.getLogger(MuleMessageDebuggerRequestHandler.class.getName());
     private MuleDebuggingContext debuggingContext;
     private DebuggerService debuggerHandler;
 
     public MuleMessageDebuggerRequestHandler(DebuggerService debuggerHandler) {
         this.debuggerHandler = debuggerHandler;
     }
 
 

     public IDebuggerResponse handleRequest(IDebuggerRequest request) {
         ICommand command = request.createCommand();
         command.setHandler(debuggerHandler);
         command.setDebuggingContext(debuggingContext);
        IDebuggerResponse response = command.execute();
         response.setRequest(request);
         return response;
     }
 
    public void setMuleDebuggingContext( MuleDebuggingContext debuggingContext) {
 
         this.debuggingContext = debuggingContext;
     }
 }
