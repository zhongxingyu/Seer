 package gov.va.med.iss.mdebugger;
 
 import gov.va.med.foundations.adapter.cci.VistaLinkConnection;
 import gov.va.med.foundations.adapter.record.VistaLinkFaultException;
 import gov.va.med.foundations.rpc.RpcRequest;
 import gov.va.med.foundations.rpc.RpcRequestFactory;
 import gov.va.med.foundations.rpc.RpcResponse;
 import gov.va.med.foundations.utilities.FoundationsException;
 import gov.va.med.iss.connection.actions.VistaConnection;
 import gov.va.med.iss.mdebugger.vo.StackVO;
 import gov.va.med.iss.mdebugger.vo.StepResultsVO;
 
 import java.util.Iterator;
 
 public class MDebugger {
 	
 	private VistaLinkConnection myConnection;
 	private String rpcName = "";
 	private boolean handleResults = true;
 	//private boolean repeatLastDebug = false; //this is always null, perhaps earlier in dev it was being set --jspivey
 	private String lastCommand = "";
 
 	public StepResultsVO doDebug(String dbCommand) {
 		lastCommand = dbCommand;
 		myConnection = VistaConnection.getConnection();
 		
 		if (myConnection == null) {
 			throw new RuntimeException("Not connected to VistaServer"); //TODO: throw core exception? I think throw a not connected exception so that it can retry connecting.
 		}
 
 		String strResults = "";
 		try {
 			strResults = callRPC(dbCommand);
 			
 			//this helps it to skip over line labels, which appear to return an empty string from the server
 			for (int i = 1; strResults.trim().equals("") && i <= 4; i++) {
 				//TODO: a blank string is ok for DELETEing watchpoints, don't retry on that.
 				System.out.println("Response was empty, sending "+ dbCommand +" again: "+ i);
 				strResults = callRPC(dbCommand);
 			}
 		} catch (VistaLinkFaultException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e); //TODO: use checked ex?
 		} catch (FoundationsException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e); //TODO: use checked ex?
 		}
 		
 		if (strResults.trim().equals(""))
 			throw new RuntimeException("Unable to fetch any results from Debugger after 5 requests"); //TODO: this should definetly be a a custom exception to indicate to the caller to terminate the IProcess
 		
 		StepResultsVO results = null;		
 		if (handleResults) {
 			System.out.println("RESPONSE for: " +dbCommand);
 			//StepResults.ProcessInput(vResp.getResults()); //comment out, no longer pass resulting to this, but isntead returning them--jspivey
 			results = new StepResultsParser().parse(strResults);
 
 			System.out.println("complete: "+ results.isComplete());
 			System.out.println("nextCommand: "+ results.getNextCommnd());
 			System.out.println("lastCommand: "+ results.getLastCommand());
 			System.out.println("LineLocation: "+ results.getLineLocation());
 			System.out.println("TagLocation: "+ results.getLocationAsTag());
 			System.out.println("Routine:" + results.getRoutineName());
 			System.out.println("Has variables: "+ results.getVariables().hasNext());
 			System.out.println("STACK:");
 			Iterator<StackVO> stackItr = results.getStack();
 			while (stackItr.hasNext()) {
 				StackVO stack = stackItr.next();
 				System.out.println(stack.getStackName() +" called by: "+ stack.getCaller());
 			}
 			
 			/*
 			 * XTDEBUG API fix for suspending on linelabels:
 			 * Whenever a linelabel is hit by the server side debugger it will
 			 * suspend, and furthermore it returns only the stack and 
 			 * variables, while leaving location and next command null. If
 			 * this situation is encountered, the client side fix is to resend
 			 * the request again.
 			 */
 			if (dbCommand.equals("RUN")) {
 				if (
 						!results.isComplete() && 
 						results.getLineLocation() == -1 &&
 						results.getNextCommnd() == null) {
 					return doDebug(dbCommand); //TODO: this could potentially endless loop, maybe do a for command with a limit
 				}
 			}
 
 		}
 		
 		return results;
 	}
 
 	private String callRPC(String dbCommand) throws FoundationsException,
 			VistaLinkFaultException {
 		RpcRequest vReq = RpcRequestFactory.getRpcRequest("",rpcName);
 		vReq.setUseProprietaryMessageFormat(false);
 		vReq.getParams().setParam(1, "string", dbCommand);  // RD  RL  GD  GL  RS
 		RpcResponse vResp = myConnection.executeRPC(vReq);
 		return vResp.getResults();
 	}
 	
 	public StepResultsVO resume() {
 		return stepDebug("RUN");
 	}
 	
 	public StepResultsVO stepOver() {
 		return stepDebug("STEP");
 	}
 	
 	public StepResultsVO stepInto() {
 		return stepDebug("STEPINTO");
 	}
 	
 	public StepResultsVO stepOut() {
 		return stepDebug("STEPOUT");
 	}
 	
 	//not supported
 //	public StepResultsVO terminate() {
 //		return stepDebug("TERMINATE");
 //	}
 
 	/**
 	 * method used to indicate to the server to process more
 	 * of the code.  The range of code to be covered is
 	 * indicated by the value of dbCommand.
 	 * @param dbCommand - contains a text value indicating
 	 * the amount of code to be processed:
 	 *    "STEP" the next command should be processed 
 	 *    "STEPLINE" commands on the current line should be 
 	 *             processed
 	 *    "RUN" commands are executed until a specified 
 	 *             reason to pause (e.g., breakpoint, watched
 	 *             value change, etc.) is reached.
 	 *    "STEPOUT" commands are processed until the processing
 	 *             exits the current stack level for an earlier
 	 *             one
 	 *    "STEPINTO" the processing is traced into the next higher
 	 *             stack level
 	 */
 	public StepResultsVO stepDebug(String dbCommand) {
 		rpcName = "XTDEBUG NEXT";
 		handleResults = true;
 		return doDebug(dbCommand);
 //		while (repeatLastDebug) { // commented out because repeatLastDebug has no chance to become true--jspivey
 //			doDebug(dbCommand);
 //		}
 	}
 	
 	/**
 	 * method to start a debugging session
 	 * @param dbCommand - contains the line of code to be executed.
 	 */
 	public StepResultsVO startDebug(String dbCommand) {
 		rpcName = "XTDEBUG START";
 		handleResults = true;
 		//MDebuggerConsoleDisplay.clearConsole();
 		return doDebug(dbCommand);
 	}
 	
 	public void doLastCommand() {
 		stepDebug(lastCommand);
 	}
 	
 	public String getLastCommand() {
 		return lastCommand;
 	}
 		
 	public void addWatchpoint(String watchPoint) {
 		rpcName = "XTDEBUG ADD WATCH";
 		handleResults = false;
 		doDebug(watchPoint);
 	}
 	
 	public void removeWatchpoint(String watchPoint) {
 		rpcName = "XTDEBUG DELETE WATCH";
 		handleResults = false;
 		doDebug(watchPoint);
 	}
 	
 	public void addBreakpoint(String breakPoint) {
 		rpcName = "XTDEBUG ADD BREAKPOINT";
 		handleResults = false;
 		doDebug(breakPoint);
 	}
 	
 	public void removeBreakpoint(String breakPoint) {
		rpcName = "XTDEBUG DELETE BREAKPOINT"; //TODO: for some reason this an invalid RPC call and fails.
 		handleResults = false;
 		doDebug(breakPoint);
 	}
 	
 	public void setTimer() {
 		   new Thread(new Runnable() {
 			      public void run() {
 			            try { Thread.sleep(50); } catch (Exception e) { }
 //			            MDebuggerConsoleDisplay.text.getDisplay().getDefault().asyncExec(new Runnable() {
 //			               public void run() {
 //			                  stepDebug(lastCommand);
 //			               }
 //			            });
 			            stepDebug(lastCommand);
 			         }
 			   }).start();
 	}
 
 }
