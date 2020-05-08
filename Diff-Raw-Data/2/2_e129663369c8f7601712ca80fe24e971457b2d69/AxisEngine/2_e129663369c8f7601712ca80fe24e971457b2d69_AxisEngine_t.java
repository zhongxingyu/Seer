 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 
 package org.apache.axis2.engine;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.alt.Flows;
 import org.apache.axis2.client.async.AxisCallback;
 import org.apache.axis2.context.ConfigurationContext;
 import org.apache.axis2.context.MessageContext;
 import org.apache.axis2.context.OperationContext;
 import org.apache.axis2.description.AxisOperation;
 import org.apache.axis2.description.TransportOutDescription;
 import org.apache.axis2.description.WSDL2Constants;
 import org.apache.axis2.engine.Handler.InvocationResponse;
 import org.apache.axis2.i18n.Messages;
 import org.apache.axis2.transport.TransportSender;
 import org.apache.axis2.util.CallbackReceiver;
 import org.apache.axis2.util.CopyOnWriteOnceList;
 import org.apache.axis2.util.LoggingControl;
 import org.apache.axis2.wsdl.WSDLConstants;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * There is one engine for the Server and the Client. the send() and receive()
  * Methods are the basic operations the Sync, Async messageing are build on top.
  */
 public class AxisEngine {
 
     private static final Log log = LogFactory.getLog(AxisEngine.class);
 
     private static final boolean RESUMING_EXECUTION = true;
     private static final boolean NOT_RESUMING_EXECUTION = false;
 
     /**
      * This methods represents the inflow of the Axis, this could be either at the server side or the client side.
      * Here the <code>ExecutionChain</code> is created using the Phases. The Handlers at the each Phases is ordered in
      * deployment time by the deployment module
      *
      * @throws AxisFault
      * @see MessageContext
      * @see Phase
      * @see Handler
      */
     public static InvocationResponse receive(MessageContext msgContext)
     	throws AxisFault
     {
         if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
             log.trace(msgContext.getLogCorrelationID() + " receive:" + msgContext.getMessageID());
         }
         prepForInvoke(msgContext);
         try {
             InvocationResponse pi = invoke(msgContext, NOT_RESUMING_EXECUTION);
 
             switch(pi) {
             case CONTINUE:
             	receiveContinue(msgContext);
                 break;
             case SUSPEND:
                 return pi;
             case ABORT:
             	receiveAbort(msgContext);
                 return pi;
             default:
                 String errorMsg =
                         "Unrecognized InvocationResponse encountered in AxisEngine.receive()";
                 log.error(msgContext.getLogCorrelationID() + " " + errorMsg);
                 throw new AxisFault(errorMsg);
             }
         } catch (AxisFault e) {
            log.error(e);
             msgContext.setFailureReason(e);
             flowComplete(msgContext);
             throw e;
         }
 
         return InvocationResponse.CONTINUE;
     }
 
     private static void receiveContinue(MessageContext msgContext) throws AxisFault {
         msgContext.checkMustUnderstand();
         if (msgContext.isServerSide()) {
             // invoke the Message Receivers
 
             MessageReceiver receiver = msgContext.getAxisOperation().getMessageReceiver();
             if (receiver == null) {
                 throw new AxisFault(Messages.getMessage(
                         "nomessagereciever",
                         msgContext.getAxisOperation().getName().toString()));
             }
             receiver.receive(msgContext);
         }
         flowComplete(msgContext);
     }
 
     private static void receiveAbort(MessageContext msgContext) throws AxisFault {
         flowComplete(msgContext);
         // Undo any partial work.
         // Remove the incoming message context
         if (log.isDebugEnabled()) {
             log.debug("InvocationResponse is aborted.  " +
                         "The incoming MessageContext is removed, " +
                         "and the OperationContext is marked as incomplete");
         }
 		AxisOperation axisOp = msgContext.getAxisOperation();
         if(axisOp!=null){
 			String mepURI  = axisOp.getMessageExchangePattern();
 			if (WSDL2Constants.MEP_URI_OUT_IN.equals(mepURI)) {
 				OperationContext opCtx = msgContext.getOperationContext();
 				if (opCtx != null) {
 					opCtx.removeMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
 				}
 			}
 		}
 		else{
 			log.debug("Could not clean up op ctx for " + msgContext);
 		}
     }
 
     private static void prepForInvoke(MessageContext msgContext) {
         ConfigurationContext confContext = msgContext.getConfigurationContext();
         List<Phase> preCalculatedPhases;
         if (msgContext.isFault() || msgContext.isProcessingFault()) {
             preCalculatedPhases = confContext.getAxisConfiguration().getInFaultFlowPhases();
             msgContext.setFlow(Flows.IN_FAULT);
         } else {
             preCalculatedPhases = confContext.getAxisConfiguration().getInFlowPhases();
             msgContext.setFlow(Flows.IN);
         }
         // Set the initial execution chain in the MessageContext to a *copy* of what
         // we got above.  This allows individual message processing to change the chain without
         // affecting later messages.
         List<Phase> executionChain
         	= new CopyOnWriteOnceList<Phase>(preCalculatedPhases);
         msgContext.setExecutionChain(executionChain);
     }
 
     /**
      * Take the execution chain from the msgContext , and then take the current Index
      * and invoke all the phases in the arraylist
      * if the msgContext is pauesd then the execution will be breaked
      *
      * @param msgContext
      * @return An InvocationResponse that indicates what
      *         the next step in the message processing should be.
      * @throws AxisFault
      */
     private static InvocationResponse invoke(MessageContext msgContext, boolean resuming)
             throws AxisFault {
 
         if (msgContext.getCurrentHandlerIndex() == -1) {
             msgContext.setCurrentHandlerIndex(0);
         }
 
         InvocationResponse pi = InvocationResponse.CONTINUE;
 
         while (msgContext.getCurrentHandlerIndex() < msgContext.getExecutionChain().size()) {
             Handler currentHandler = msgContext.getExecutionChain().
                     get(msgContext.getCurrentHandlerIndex());
 
             try {
                 if (!resuming) {
                     msgContext.addExecutedPhase(currentHandler);
                 } else {
                     /* If we are resuming the flow, we don't want to add the phase
                     * again, as it has already been added.
                     */
                     resuming = false;
                 }
                 pi = currentHandler.invoke(msgContext);
             }
             catch (AxisFault e) {
                 if (msgContext.getCurrentPhaseIndex() == 0) {
                     /* If we got a fault, we still want to add the phase to the
                     list to be executed for flowComplete(...) unless this was
                     the first handler, as then the currentPhaseIndex will be
                     set to 0 and this will look like we've executed all of the
                     handlers.  If, at some point, a phase really needs to get
                     notification of flowComplete, then we'll need to introduce
                     some more complex logic to keep track of what has been
                     executed.*/
                     msgContext.removeFirstExecutedPhase();
                 }
                 throw e;
             }
 
             if (pi.equals(InvocationResponse.SUSPEND) ||
                     pi.equals(InvocationResponse.ABORT)) {
                 break;
             }
 
             msgContext.setCurrentHandlerIndex(msgContext.getCurrentHandlerIndex() + 1);
         }
 
         return pi;
     }
 
     private static void flowComplete(MessageContext msgContext) {
         for(final Handler currentHandler: msgContext.getExecutedPhases()) {
             currentHandler.flowComplete(msgContext);
         }
 
         /*This is needed because the OutInAxisOperation currently invokes
         * receive() even when a fault occurs, and we will have already executed
         * the flowComplete on those before receiveFault() is called.
         */
         msgContext.resetExecutedPhases();
     }
 
     /**
      * If the msgConetext is puased and try to invoke then
      * first invoke the phase list and after the message receiver
      *
      * @param msgContext
      * @return An InvocationResponse allowing the invoker to perhaps determine
      *         whether or not the message processing will ever succeed.
      * @throws AxisFault
      */
     public static InvocationResponse resumeReceive(MessageContext msgContext) throws AxisFault {
         if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
             log.trace(msgContext.getLogCorrelationID() + " resumeReceive:" + msgContext.getMessageID());
         }
 
         //REVIEW: This name is a little misleading, as it seems to indicate that there should be a resumeReceiveFault as well, when, in fact, this does both
         //REVIEW: Unlike with receive, there is no wrapping try/catch clause which would
         //fire off the flowComplete on an error, as we have to assume that the
         //message will be resumed again, but perhaps we need to unwind back to
         //the point at which the message was resumed and provide another API
         //to allow the full unwind if the message is going to be discarded.
         //invoke the phases
         InvocationResponse pi = invoke(msgContext, RESUMING_EXECUTION);
         //invoking the MR
 
         if (pi.equals(InvocationResponse.CONTINUE)) {
             receiveContinue(msgContext);
         }
 
         return pi;
     }
 
     /**
      * To resume the invocation at the send path , this is neened since it is require to call
      * TransportSender at the end
      *
      * @param msgContext
      * @return An InvocationResponse allowing the invoker to perhaps determine
      *         whether or not the message processing will ever succeed.
      * @throws AxisFault
      */
     public static InvocationResponse resumeSend(MessageContext msgContext) throws AxisFault {
         if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
             log.trace(msgContext.getLogCorrelationID() + " resumeSend:" + msgContext.getMessageID());
         }
 
         //REVIEW: This name is a little misleading, as it seems to indicate that there should be a resumeSendFault as well, when, in fact, this does both
         //REVIEW: Unlike with send, there is no wrapping try/catch clause which would
         //fire off the flowComplete on an error, as we have to assume that the
         //message will be resumed again, but perhaps we need to unwind back to
         //the point at which the message was resumed and provide another API
         //to allow the full unwind if the message is going to be discarded.
         //invoke the phases
         InvocationResponse pi = invoke(msgContext, RESUMING_EXECUTION);
         //Invoking Transport Sender
         if (pi.equals(InvocationResponse.CONTINUE)) {
             // write the Message to the Wire
             TransportOutDescription transportOut = msgContext.getTransportOut();
             TransportSender sender = transportOut.getSender();
             sender.invoke(msgContext);
             flowComplete(msgContext);
         }
 
         return pi;
     }
 
     /**
      * Resume processing of a message.
      *
      * @param msgctx
      * @return An InvocationResponse allowing the invoker to perhaps determine
      *         whether or not the message processing will ever succeed.
      * @throws AxisFault
      */
     public static InvocationResponse resume(MessageContext msgctx) throws AxisFault {
         if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
             log.trace(msgctx.getLogCorrelationID() + " resume:" + msgctx.getMessageID());
         }
 
         msgctx.setPaused(false);
         if (msgctx.getFlow() == Flows.IN) {
             return resumeReceive(msgctx);
         } else {
             return resumeSend(msgctx);
         }
     }
 
     /**
      * This methods represents the outflow of the Axis, this could be either at the server side or the client side.
      * Here the <code>ExecutionChain</code> is created using the Phases. The Handlers at the each Phases is ordered in
      * deployment time by the deployment module
      *
      * @param msgContext
      * @throws AxisFault
      * @see MessageContext
      * @see Phase
      * @see Handler
      */
     public static void send(MessageContext msgContext) throws AxisFault {
         if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
             log.trace(msgContext.getLogCorrelationID() + " send:" + msgContext.getMessageID());
         }
         // find and invoke the Phases
         OperationContext operationContext = msgContext.getOperationContext();
         List<Phase> executionChain = operationContext.getAxisOperation().getPhasesOutFlow();
         //rather than having two steps added both operation and global chain together
         List<Phase> outPhases = new ArrayList<Phase>();
         outPhases.addAll(executionChain);
         outPhases.addAll(msgContext.getConfigurationContext().getAxisConfiguration().getOutFlowPhases());
         msgContext.setExecutionChain(outPhases);
         msgContext.setFlow(Flows.OUT);
         try {
             InvocationResponse pi = invoke(msgContext, NOT_RESUMING_EXECUTION);
 
             switch(pi) {
             case CONTINUE:
                 // write the Message to the Wire
                 TransportOutDescription transportOut = msgContext.getTransportOut();
                 if (transportOut == null) {
                     throw new AxisFault("Transport out has not been set");
                 }
                 TransportSender sender = transportOut.getSender();
                 // This boolean property only used in client side fireAndForget invocation
                 //It will set a property into message context and if some one has set the
                 //property then transport sender will invoke in a diffrent thread
                 Object isTransportNonBlocking = msgContext.getProperty(
                         MessageContext.TRANSPORT_NON_BLOCKING);
                 if (isTransportNonBlocking != null &&
                         ((Boolean) isTransportNonBlocking).booleanValue()) {
                     msgContext.getConfigurationContext().getExecutor().execute(
                             new TransportNonBlockingInvocationWorker(msgContext, sender));
                 } else {
                     sender.invoke(msgContext);
                 }
                 //REVIEW: In the case of the TransportNonBlockingInvocationWorker, does this need to wait until that finishes?
                 flowComplete(msgContext);
                 break;
             case SUSPEND:
             	break;
             case ABORT:
                 flowComplete(msgContext);
                 break;
             default:
                 String errorMsg =
                         "Unrecognized InvocationResponse encountered in AxisEngine.send()";
                 log.error(msgContext.getLogCorrelationID() + " " + errorMsg);
                 throw new AxisFault(errorMsg);
             }
         } catch (AxisFault e) {
             msgContext.setFailureReason(e);
             flowComplete(msgContext);
             throw e;
         }
     }
 
     /**
      * Sends the SOAP Fault to another SOAP node.
      *
      * @param msgContext
      * @throws AxisFault
      */
     public static void sendFault(MessageContext msgContext) throws AxisFault {
         if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
             log.trace(msgContext.getLogCorrelationID() + " sendFault:" + msgContext.getMessageID());
         }
         OperationContext opContext = msgContext.getOperationContext();
 
         //FIXME: If this gets paused in the operation-specific phases, the resume is not going to function correctly as the phases will not have all been set
 
         // find and execute the Fault Out Flow Handlers
         if (opContext != null) {
             AxisOperation axisOperation = opContext.getAxisOperation();
             List<Phase> faultExecutionChain = axisOperation.getPhasesOutFaultFlow();
 
             //adding both operation specific and global out fault flows.
 
             final List<Phase> outFaultPhases
             	= new ArrayList<Phase>(faultExecutionChain.size());
             outFaultPhases.addAll(faultExecutionChain);
             msgContext.setExecutionChain(outFaultPhases);
             msgContext.setFlow(Flows.OUT_FAULT);
             try {
                 InvocationResponse pi = invoke(msgContext, NOT_RESUMING_EXECUTION);
                 if(shouldReturn(pi, msgContext)) {
                 	return;
                 }
             }
             catch (AxisFault e) {
                 msgContext.setFailureReason(e);
                 flowComplete(msgContext);
                 throw e;
             }
         }
         finishSendFault(msgContext);
     }
 
     /**
      * here we assume that it is resume from an operation level handler
      * @param msgContext
      * @throws AxisFault
      */
     public static void resumeSendFault(MessageContext msgContext) throws AxisFault{
         if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
             log.trace(msgContext.getLogCorrelationID() + " resumeSendFault:" + msgContext.getMessageID());
         }
         OperationContext opContext = msgContext.getOperationContext();
 
         if (opContext != null) {
             try {
                 InvocationResponse pi = invoke(msgContext, RESUMING_EXECUTION);
                 if(shouldReturn(pi, msgContext)) {
                 	return;
                 }
             } catch (AxisFault e) {
                 msgContext.setFailureReason(e);
                 flowComplete(msgContext);
                 throw e;
             }
         }
         finishSendFault(msgContext);
     }
 
     private static void finishSendFault(MessageContext msgContext)
     	throws AxisFault
     {
         ArrayList<Handler> executionChain = new ArrayList<Handler>(msgContext.getConfigurationContext()
                 .getAxisConfiguration().getOutFaultFlowPhases());
         msgContext.setExecutionChain(executionChain);
         msgContext.setFlow(Flows.OUT_FAULT);
         InvocationResponse pi = invoke(msgContext, NOT_RESUMING_EXECUTION);
 
         switch(pi) {
         case CONTINUE:
             // Actually send the SOAP Fault
             TransportOutDescription transportOut = msgContext.getTransportOut();
             if (transportOut == null) {
                 throw new AxisFault("Transport out has not been set");
             }
             TransportSender sender = transportOut.getSender();
 
             sender.invoke(msgContext);
             flowComplete(msgContext);
             break;
         case SUSPEND:
         	break;
         case ABORT:
         	flowComplete(msgContext);
         	break;
         default:
             String errorMsg = "Unrecognized InvocationResponse";
             log.error(msgContext.getLogCorrelationID() + " " + errorMsg);
             throw new AxisFault(errorMsg);
         }
     }
 
     private static boolean shouldReturn(InvocationResponse pi, MessageContext msgContext) throws AxisFault {
         switch(pi) {
         case SUSPEND:
         	log.warn(msgContext.getLogCorrelationID() +
         		" The resumption of this flow may function incorrectly, as the OutFaultFlow will not be used");
         	return true;
         case ABORT:
             flowComplete(msgContext);
             return true;
         case CONTINUE:
         	return false;
         default:
         	String errorMsg = "Unrecognized InvocationResponse";
             log.error(msgContext.getLogCorrelationID() + " " + errorMsg);
             throw new AxisFault(errorMsg);
         }
     }
 
 
     /**
      * This class is used when someone invoke a service invocation with two transports
      * If we dont create a new thread then the main thread will block untill it gets the
      * response . In the case of HTTP transportsender will block untill it gets HTTP 200
      * So , main thread also block till transport sender rereases the tread. So there is no
      * actual non-blocking. That is why when sending we creat a new thead and send the
      * requset via that.
      * <p/>
      * So whole porpose of this class to send the requset via a new thread
      * <p/>
      * way transport.
      */
     private static class TransportNonBlockingInvocationWorker implements Runnable {
         private final MessageContext msgctx;
         private final TransportSender sender;
 
         public TransportNonBlockingInvocationWorker(MessageContext msgctx,
             TransportSender sender) {
             this.msgctx = msgctx;
             this.sender = sender;
         }
 
         public void run() {
             try {
                 sender.invoke(msgctx);
             } catch (Exception e) {
                 log.info(msgctx.getLogCorrelationID() + " " + e.getMessage());
                 if (msgctx.getProperty(MessageContext.DISABLE_ASYNC_CALLBACK_ON_TRANSPORT_ERROR) ==
                         null) {
                     AxisOperation axisOperation = msgctx.getAxisOperation();
                     if (axisOperation != null) {
                         MessageReceiver msgReceiver = axisOperation.getMessageReceiver();
                         if ((msgReceiver != null) && (msgReceiver instanceof CallbackReceiver)) {
                             AxisCallback callback = ((CallbackReceiver) msgReceiver)
                                     .lookupCallback(msgctx.getMessageID());
                             if (callback == null) {
 								return; // TODO: should we log this??
 							}
 
                             // The AxisCallback (which is OutInAxisOperationClient$SyncCallBack
                             // used to support async-on-the-wire under a synchronous API
                             // operation) need to be told the MEP is complete after being told
                             // of the error.
                             (callback).onError(e);
                             (callback).onComplete();
                         }
                     }
                 }
             }
         }
     }
 }
