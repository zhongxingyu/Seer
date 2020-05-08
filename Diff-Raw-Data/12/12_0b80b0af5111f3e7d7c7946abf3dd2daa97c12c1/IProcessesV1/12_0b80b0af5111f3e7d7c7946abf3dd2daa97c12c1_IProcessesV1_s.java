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
 package org.eclipse.tcf.services;
 
 import java.util.Map;
 
 import org.eclipse.tcf.protocol.IToken;
 
 /**
  * Extension of Processes service.
  * It provides new "start" command that supports additional parameters.
  */
 public interface IProcessesV1 extends IProcesses {
 
     static final String NAME = "ProcessesV1";
 
     /** Process start parameters */
     static final String
         /** Boolean, attach the debugger to the process */
         START_ATTACH = "Attach",
         /** Boolean, auto-attach process children */
         START_ATTACH_CHILDREN = "AttachChildren",
         /** Boolean, stop at process entry */
         START_STOP_AT_ENTRY = "StopAtEntry",
         /** Boolean, stop at main() */
         START_STOP_AT_MAIN = "StopAtMain",
         /** Boolean, Use pseudo-terminal for the process standard I/O */
         START_USE_TERMINAL = "UseTerminal",
         /** Bit set of signals that should not be intercepted by the debugger */
         START_SIG_DONT_STOP = "SigDontStop",
         /** Bit set of signals that should not be delivered to the process */
         START_SIG_DONT_PASS = "SigDontPass";
 
     /**
      * Client call back interface for getCapabilities().
      */
     interface DoneGetCapabilities {
         /**
          * Called when the capability retrieval is done.
          *
          * @param error The error description if the operation failed, <code>null</code> if succeeded.
         * @param properties The global ModuleLoad service or context specific capabilities.
          */
         public void doneGetCapabilities(IToken token, Exception error, Map<String, Object> properties);
     }
 
     /**
      * Start a new process on remote machine.
      * @param directory - initial value of working directory for the process.
      * @param file - process image file.
      * @param command_line - command line arguments for the process.
      * Note: the service does NOT add image file name as first argument for the process.
      * If a client wants first parameter to be the file name, it should add it itself.
      * @param environment - map of environment variables for the process,
      * if null then default set of environment variables will be used.
      * @param params - additional process start parameters, see START_*.
      * @param done - call back interface called when operation is completed.
      * @return pending command handle, can be used to cancel the command.
      */
     IToken start(String directory, String file,
             String[] command_line, Map<String,String> environment,
             Map<String,Object> params, DoneStart done);
 
     /**
      * The command reports the ProcessesV1 service capabilities to clients so they can adjust
      * to different implementations of the service. When called with a null ("") context
      * ID the global capabilities are returned, otherwise context specific capabilities
      * are returned.
      *
      * @param id The context ID or <code>null</code>.
      * @param done The call back interface called when the operation is completed. Must not be <code>null</code>.
      */
     public IToken getCapabilities(String id, DoneGetCapabilities done);
 
 }
