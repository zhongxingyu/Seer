 /*
  * Created on Nov 23, 2004
  */
 package uk.org.ponder.rsf.processor;
 
 import java.util.Map;
 
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 import uk.org.ponder.rsf.flow.ARIResolver;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.flow.ActionResultInterceptor;
 import uk.org.ponder.rsf.flow.ActionResultInterpreter;
 import uk.org.ponder.rsf.flow.errors.ActionErrorStrategy;
 import uk.org.ponder.rsf.flow.support.FlowStateManager;
 import uk.org.ponder.rsf.preservation.StatePreservationManager;
 import uk.org.ponder.rsf.request.RequestSubmittedValueCache;
 import uk.org.ponder.rsf.state.ErrorStateManager;
 import uk.org.ponder.rsf.state.RSVCApplier;
 import uk.org.ponder.rsf.viewstate.AnyViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.RunnableInvoker;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * ActionHandler is a request scope bean responsible for handling an HTTP POST
  * request, or other non-idempotent web service "action" cycle. Defines the core
  * logic for this processing cycle.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 public class RSFActionHandler implements ActionHandler, ErrorHandler {
   // application-scope dependencies
   private ARIResolver ariresolver;
   private RunnableInvoker postwrapper;
   private RequestSubmittedValueCache requestrsvc;
 
   // request-scope dependencies
   private Map normalizedmap;
   private ViewParameters viewparams;
   private ErrorStateManager errorstatemanager;
   private RSVCApplier rsvcapplier;
   private StatePreservationManager presmanager; // no, not that of OS/2
   private ActionErrorStrategy actionerrorstrategy;
   private FlowStateManager flowstatemanager;
   private TargettedMessageList messages;
   private ActionResultInterceptor arinterceptor;
 
   public void setFlowStateManager(FlowStateManager flowstatemanager) {
     this.flowstatemanager = flowstatemanager;
   }
 
   public void setErrorStateManager(ErrorStateManager errorstatemanager) {
     this.errorstatemanager = errorstatemanager;
   }
 
   public void setARIResolver(ARIResolver ariresolver) {
     this.ariresolver = ariresolver;
   }
 
   public void setActionResultInterceptor(ActionResultInterceptor arinterceptor) {
     this.arinterceptor = arinterceptor;
   }
   
   public void setViewParameters(ViewParameters viewparams) {
     this.viewparams = viewparams;
   }
 
   public void setRequestRSVC(RequestSubmittedValueCache requestrsvc) {
     this.requestrsvc = requestrsvc;
   }
 
   public void setAlterationWrapper(RunnableInvoker postwrapper) {
     this.postwrapper = postwrapper;
   }
 
   public void setRSVCApplier(RSVCApplier rsvcapplier) {
     this.rsvcapplier = rsvcapplier;
   }
 
   public void setNormalizedRequestMap(Map normalizedmap) {
     this.normalizedmap = normalizedmap;
   }
 
   public void setStatePreservationManager(StatePreservationManager presmanager) {
     this.presmanager = presmanager;
   }
 
   // actually the ActionErrorStrategyManager
   public void setActionErrorStrategy(ActionErrorStrategy actionerrorstrategy) {
     this.actionerrorstrategy = actionerrorstrategy;
   }
 
   public void setTargettedMessageList(TargettedMessageList messages) {
     this.messages = messages;
   }
 
   // The public ARIResult after all inference is concluded.
   private ARIResult ariresult = null;
   // The original raw action result - cannot make this final for wrapper access
   private Object actionresult = null;
 
   /**
    * The result of this post cycle will be of interest to some other request
    * beans, in particular the alteration wrapper. This bean must however be
    * propagated lazily since it is only constructed partway through this
    * handler, if indeed it is a POST cycle at all.
    * 
    * @return The ARIResult resulting from this action cycle.
    */
   public ARIResult getARIResult() {
     return ariresult;
   }
 
   public Object handleError(Object actionresult, Exception exception) {
     // an ARIResult is at the end-of-line - this is now unsupported though.
     if (actionresult != null && !(actionresult instanceof String))
       return actionresult;
     TargettedMessage tmessage = new TargettedMessage();
     Object newcode = actionerrorstrategy.handleError((String) actionresult,
         exception, null, viewparams.viewID, tmessage);
 
     if (newcode != null && !(newcode instanceof String))
       return newcode;
     for (int i = 0; i < messages.size(); ++i) {
       TargettedMessage message = messages.messageAt(i);
       if (message.exception != null) {
         try {
           Object testcode = actionerrorstrategy.handleError((String) newcode,
               message.exception, null, viewparams.viewID, message);
           if (testcode != null)
             newcode = testcode;
         }
         catch (Exception e) {
           // rethrow *original* more accurate URE, as per Az discovery
           throw UniversalRuntimeException.accumulate(message.exception,
               "Error invoking action");
         }
 
       }
     }
     if (tmessage.messagecodes != null) {
       messages.addMessage(tmessage);
     }
     return newcode;
   }
 
   public AnyViewParameters handle() {
 
     try {
       final String actionmethod = PostDecoder.decodeAction(normalizedmap);
       // Do this FIRST in case it discovers any scopelocks required
       presmanager.scopeRestore();
       // invoke all state-altering operations within the runnable wrapper.
       postwrapper.invokeRunnable(new Runnable() {
         public void run() {
 
           if (viewparams.flowtoken != null) {
             presmanager.restore(viewparams.flowtoken,
                 viewparams.endflow != null);
           }
           Exception exception = null;
           try {
             rsvcapplier.applyValues(requestrsvc); // many errors possible here.
           }
           catch (Exception e) {
             exception = e;
           }
           Object newcode = handleError(actionresult, exception);
           exception = null;
           if ((newcode == null && !messages.isError())
               || newcode instanceof String) {
             // only proceed to actually invoke action if no ARIResult already
             // note all this odd two-step procedure is only required to be able
             // to pass AES error returns and make them "appear" to be the
             // returns of the first action method in a Flow.
             try {
               if (actionmethod != null) {
                 actionresult = rsvcapplier.invokeAction(actionmethod,
                     (String) newcode);
               }
             }
             catch (Exception e) {
               exception = e;
             }
             newcode = handleError(actionresult, exception);
           }
           if (newcode != null)
             actionresult = newcode;
           // must interpret ARI INSIDE the wrapper, since it may need it
           // on closure.
           if (actionresult instanceof ARIResult) {
             ariresult = (ARIResult) actionresult;
           }
           else {
             ActionResultInterpreter ari = ariresolver
                 .getActionResultInterpreter();
             ariresult = ari.interpretActionResult(viewparams, actionresult);
           }
           arinterceptor.interceptActionResult(ariresult, viewparams, actionresult);
         }
       });
       presmanager.scopePreserve();
 
       flowstatemanager.inferFlowState(viewparams, ariresult);
 
       // moved inside since this may itself cause an error!
       String submitting = PostDecoder.decodeSubmittingControl(normalizedmap);
       errorstatemanager.globaltargetid = submitting;
     }
     catch (Throwable e) { // avoid masking errors from the finally block
      Logger.log.warn("Error invoking action", e);
       // ThreadErrorState.addError(new TargettedMessage(
       // CoreMessages.GENERAL_ACTION_ERROR));
       // Detect failure to fill out arires properly.
       if (ariresult == null || ariresult.resultingView == null
           || e instanceof IllegalStateException) {
         ariresult = new ARIResult();
         ariresult.propagateBeans = ARIResult.FLOW_END;
 
         ViewParameters defaultparameters = viewparams.copyBase();
         ariresult.resultingView = defaultparameters;
       }
     }
     finally {
       String errortoken = null;
       if (messages.size() != 0) {
         // messages on a POST cycle are DEFINITELY from our cycle
         errortoken = errorstatemanager.allocateOutgoingToken();
       }
       errorstatemanager.requestComplete();
 
       if (ariresult.resultingView instanceof ViewParameters) {
         ((ViewParameters) ariresult.resultingView).errortoken = errortoken;
       }
     }
     return ariresult.resultingView;
   }
 
 }
