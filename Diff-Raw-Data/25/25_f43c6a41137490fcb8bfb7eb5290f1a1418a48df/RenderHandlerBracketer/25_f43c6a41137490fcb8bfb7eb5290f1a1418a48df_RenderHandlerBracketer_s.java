 /*
  * Created on Aug 5, 2005
  */
 package uk.org.ponder.rsf.processor.support;
 
 import java.net.MalformedURLException;
 
 import uk.org.ponder.errorutil.ConfigurationException;
 import uk.org.ponder.errorutil.CoreMessages;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.rsf.flow.errors.SilentRedirectException;
 import uk.org.ponder.rsf.flow.errors.ViewExceptionStrategy;
 import uk.org.ponder.rsf.processor.RenderHandler;
 import uk.org.ponder.rsf.state.support.ErrorStateManager;
 import uk.org.ponder.rsf.viewstate.AnyViewParameters;
 import uk.org.ponder.rsf.viewstate.ErrorViewParameters;
 import uk.org.ponder.rsf.viewstate.RedirectViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * This request-scope bean exists to bracket the creation and operation of the
  * rendering process, such that any "first" exception causes a redirect to a
  * default view. A "double fault", caught above in the RootHandlerBean, will
  * render a fatal error message in the raw.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class RenderHandlerBracketer {
   private ViewExceptionStrategy ves;
   private ErrorStateManager errorstatemanager;
   private AnyViewParameters anyviewparams;
   private RenderHandler renderhandler;
   private boolean redirectlevel1 = true;
 
   public void setViewExceptionStrategy(ViewExceptionStrategy ves) {
     this.ves = ves;
   }
 
   public void setRedirectOnLevel1Error(boolean redirectlevel1) {
     this.redirectlevel1 = redirectlevel1;
   }
 
   public void setErrorStateManager(ErrorStateManager errorstatemanager) {
     this.errorstatemanager = errorstatemanager;
   }
 
   public void setViewParameters(AnyViewParameters viewparams) {
     this.anyviewparams = viewparams;
   }
 
   // This is a lazy dependency
   public void setRenderHandler(RenderHandler renderhandler) {
     this.renderhandler = renderhandler;
   }
 
   public AnyViewParameters handle(PrintOutputStream pos) {
     AnyViewParameters incoming = null;
     boolean iserrorredirect = false;
     incoming = anyviewparams.get();
     // An AnyViewParameters is CERTAINLY a redirect, issued by an interceptor
     if (!(anyviewparams instanceof ViewParameters)) {
       return anyviewparams;
     }
     if (anyviewparams instanceof RedirectViewParameters) {
       return ((RedirectViewParameters)anyviewparams).target;
       }
     ViewParameters viewparams = (ViewParameters) incoming;
     iserrorredirect = viewparams.errorredirect != null;
     // YES, reach into the original request! somewhat bad...
     viewparams.errorredirect = null;
     
     try {
      
       if (viewparams instanceof ErrorViewParameters) {
         throw UniversalRuntimeException.accumulate(new MalformedURLException(),
             "Request for bad view with ID " + viewparams.viewID + " ");
       }
       renderhandler.handle(pos);
     }
     catch (Exception e) {
 
       // if a request comes in for an invalid view, redirect it onto a default
       AnyViewParameters redirect = handleLevel1Error((ViewParameters) incoming, e,
           iserrorredirect);
       return redirect;
     }
     finally {
       errorstatemanager.requestComplete();
     }
     // if an exception escapes this block, handler will externally call
     // renderFatalError
     return null;
   }
 
   // a "Level 1" GET error simply attempts to redirect onto a default
   // view, with errors intact.
   public AnyViewParameters handleLevel1Error(ViewParameters viewparams,
       Exception e, boolean iserrorredirect) {
     UniversalRuntimeException invest = UniversalRuntimeException.accumulate(e);
     Throwable target = invest.getTargetException();
     boolean issilent = target != null
         && target instanceof SilentRedirectException;
     if (!issilent) {
       Logger.log.warn("Exception rendering view: ", e);
     }
 
     if (target != null) {
       Logger.log.warn("Got target exception of " + target.getClass());
     }
 
     if (target instanceof ConfigurationException || target instanceof Error
         || iserrorredirect || !redirectlevel1) {
       throw invest;
     }
     AnyViewParameters outgoingo = null;
     if (issilent) {
       SilentRedirectException silent = (SilentRedirectException) target;
       outgoingo = silent.redirectTo;
     }
     if (outgoingo == null) {
       outgoingo = ves.handleException(e, viewparams);
     }
 
     // make sure this method is prodded FIRST, because requestComplete
     // (which would ordinarily allocate the token on seeing an error state) is
     // only called AFTER we visibly return from handle() above.
     if (!issilent && outgoingo instanceof ViewParameters) {
       ViewParameters outgoing = (ViewParameters) outgoingo;
       String tokenid = errorstatemanager.allocateOutgoingToken();
       outgoing.errortoken = tokenid;
       outgoing.errorredirect = "1";
       Logger.log.warn("Error creating view tree - token " + tokenid);
       TargettedMessage newerror = new TargettedMessage(
           CoreMessages.GENERAL_SHOW_ERROR, new Object[] { tokenid });
       errorstatemanager.getTargettedMessageList().addMessage(newerror);
     }
 
     return outgoingo;
   }
 
 }
