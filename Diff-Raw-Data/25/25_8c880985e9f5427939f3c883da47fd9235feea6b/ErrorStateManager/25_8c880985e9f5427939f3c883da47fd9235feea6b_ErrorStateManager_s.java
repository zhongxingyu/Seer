 /*
  * Created on Nov 23, 2004
  */
 package uk.org.ponder.rsf.state.support;
 
 import uk.org.ponder.hashutil.EighteenIDGenerator;
 import uk.org.ponder.messageutil.TargettedMessageList;
 import uk.org.ponder.rsf.request.RequestSubmittedValueCache;
 import uk.org.ponder.rsf.state.TokenStateHolder;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.util.Logger;
 
 /**
  * Holds the token-related state which is stored DURING a request. After the
  * request is completed, some of this information may be copied to a
  * TokenRequestState entry for the token, stored in the TokenStateHolder.
  * 
  * Keyed by a token which identifies a view that has been presented to the user.
  * The token is allocated afresh on each POST, and redirected to a GET view that
  * shares the same token. If the POST is non-erroneous, the rsvc can be blasted,
  * however if it is in error, it will be kept so that the values can be
  * resubmitted back to the user. We should CHANGE the old behaviour that carried
  * along the old token - we simply want to make a new token and erase the entry
  * for the old one.
  * 
  * When a further POST is made again from that view, it should mark that token
  * as USED, possibly by simply removing the values from the cache.
  * 
  * When the user tries to make a submission from an erased token, there should
  * be some (ideally) application-defined behaviour. But in general we don't have
  * much option but to erase their data, since it will probably conflict somehow.
  * 
  * NB RequestStateEntry is now a request-scope bean.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class ErrorStateManager {
   /**
    * The id of the submitting control responsible for this POST cycle, if there
    * is one. This will be used to target the error messages delivered to the
    * following RENDER cycle.
    */
   public String globaltargetid = null;
   // this field will be set if there is an incoming error state.
   public ErrorTokenState errorstate;
 
   private TokenStateHolder errortsholder;
   // for a GET request this will be set, but empty.
   private RequestSubmittedValueCache requestrsvc;
  private ViewParameters viewparams;
   private TargettedMessageList messages;
   private String outgoingToken;
   private TMLFixer messageProcessor;
 
   public void setTMLFixer(TMLFixer messageProcessor) {
     this.messageProcessor = messageProcessor;
   }
 
   public void setTargettedMessageList(TargettedMessageList tml) {
     this.messages = tml;
   }
   
   public TargettedMessageList getTargettedMessageList() {
     return messages;
   }
   
   public void setTSHolder(TokenStateHolder errortsholder) {
     this.errortsholder = errortsholder;
   }
 
  public void setViewParameters(ViewParameters viewparams) {
    this.viewparams = viewparams;
   }
 
   public void init() {
     if (viewparams.errortoken != null) {
       Object storederrorstateo = errortsholder
           .getTokenState(viewparams.errortoken);
       if (storederrorstateo == null || 
           !(storederrorstateo instanceof ErrorTokenState)) {
         // it may be from a stale ClassLoader
         Logger.log.warn("Client requested error state " + viewparams.errortoken
             + " which has expired from the cache");
         errortsholder.clearTokenState(viewparams.errortoken);
       }
       else {
         errorstate = (ErrorTokenState) storederrorstateo;
         messages.addMessages(errorstate.messages);
         return;
       }
     }
     errorstate = new ErrorTokenState();
   }
   
 
   public void setRequestRSVC(RequestSubmittedValueCache requestrsvc) {
     this.requestrsvc = requestrsvc;
   }
 
   private static EighteenIDGenerator idgenerator = new EighteenIDGenerator();
 
   public String allocateToken() {
     return idgenerator.generateID();
   }
 
   public String allocateOutgoingToken() {
     if (outgoingToken == null) {
       outgoingToken = errorstate.tokenID = viewparams.errortoken == null ? allocateToken()
           : viewparams.errortoken;
     }
     return errorstate.tokenID;
   }
 
   /**
    * Signal that all errors for this request cycle have been accumulated into
    * the current ese. It will now be cached in the tokenrequeststate for
    * reference by further requests, under the OUTGOING token ID, and cleared
    * from the current thread.
    * 
    * This is called at the end of both render and action cycles.
    * 
    * @return The error token ID to be used for the outgoing request, or null if
    *         there is no error.
    */
   public String requestComplete() {
     // If tokenID is null, it is probably an odd condition where we are on a
     // GET cycle and the errors are not for this view (perhaps SpringMVC)
     if (messages.size() > 0 && errorstate.tokenID != null) {
      
       // the errors arose from this cycle, and hence must be referred to
       // by SVEs from this cycle. If it is a GET cycle, rsvc will be empty,
       // but then all errors will have global target.
       if (outgoingToken != null) {
          // Do not fix up if the errors were not from this cycle
         messageProcessor.fixupMessages(messages, requestrsvc);
       }
 
       errorstate.globaltargetid = globaltargetid;
       if (messages.isError()) {
         // do not store the rsvc if no error, submitted values were accepted
         // we are propagating messages only
         errorstate.rsvc = requestrsvc.copy();
       }
       errorstate.messages = messages;
       if (errorstate.rsvc != null) {
         Logger.log.info(errorstate.rsvc.getEntries()
           + " RSVC values stored under error token " + errorstate.tokenID);
       }
       errortsholder.putTokenState(errorstate.tokenID, errorstate);
       return errorstate.tokenID;
     }
     else {
       if (errorstate.tokenID != null) {
         errortsholder.clearTokenState(errorstate.tokenID);
       }
       return null;
     }
   }
 
 }
