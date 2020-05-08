 /*
  * Created on Aug 5, 2005
  */
 package uk.org.ponder.rsf.servlet;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import uk.org.ponder.errorutil.ErrorUtil;
 import uk.org.ponder.rsf.components.ParameterList;
 import uk.org.ponder.rsf.processor.ActionHandler;
 import uk.org.ponder.rsf.processor.HandlerHook;
 import uk.org.ponder.rsf.processor.RenderHandlerBracketer;
 import uk.org.ponder.rsf.renderer.RenderUtil;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewStateHandler;
 import uk.org.ponder.streamutil.write.OutputStreamPOS;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * The RootHandlerBean is the main entry point for handling of the
  * HttpServletRequest cycle. Virtually all servlet-dependent logic is placed in
  * this class, including initiating parameter decoding (eventually done by
  * RSAC), setting up the response writer and issuing client redirects. From
  * here, handling forks to the GetHandler and PostHandler in the
  * <code>processor</code> package.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class RootHandlerBean implements HandlerHook {
   private String requesttype;
   private HttpServletRequest request;
   private HttpServletResponse response;
   private ViewStateHandler viewstatehandler;
   private ParameterList outgoingparams;
   private RenderHandlerBracketer renderhandlerbracketer;
   private ActionHandler actionhandler;
   private HandlerHook handlerhook;
 
   public void setHttpServletRequest(HttpServletRequest request) {
     this.request = request;
   }
 
   public void setHttpServletResponse(HttpServletResponse response) {
     this.response = response;
   }
 
   public void setRequestType(String requesttype) {
     this.requesttype = requesttype;
   }
 
   public void setViewStateHandler(ViewStateHandler viewstatehandler) {
     this.viewstatehandler = viewstatehandler;
   }
 
   public void setOutgoingParams(ParameterList outgoingparams) {
     this.outgoingparams = outgoingparams;
   }
 
   public void setRenderHandlerBracketer(
       RenderHandlerBracketer renderhandlerbracketer) {
     this.renderhandlerbracketer = renderhandlerbracketer;
   }
 
   public void setActionHandler(ActionHandler actionhandler) {
     this.actionhandler = actionhandler;
   }
 
   public boolean handle() {
     if (handlerhook == null || !handlerhook.handle()) {
       if (requesttype.equals(ViewParameters.RENDER_REQUEST)) {
         handleGet();
       }
       else {
         handlePost();
       }
     }
     return true;
   }
 
   public void setHandlerHook(HandlerHook handlerhook) {
     this.handlerhook = handlerhook;
   }
 
   
   
   private void handleGet() {
     PrintOutputStream pos = setupResponseWriter(request, response);
     try {
       ViewParameters redirect = renderhandlerbracketer.handle(pos);
 
       if (redirect != null) {
         issueRedirect(redirect, response);
       }
     }
     catch (Throwable t) {
       // moved here to avoid triggering bean creation error.
       renderFatalError(t, pos);
     }
    finally {
      pos.close();
    }
   }
 
   private void handlePost() {
     
     ViewParameters redirect = actionhandler.handle();
 
     issueRedirect(redirect, response);
   }
 
   // If this is a web service request, send the required redirect URL
   // to the client via the body of the POST response. Otherwise, issue
   // the redirect directly to the client via this connection.
   // TODO: This method might need some state, depending on the client.
   // maybe we can do this all with "request beans"?
   public void issueRedirect(ViewParameters viewparams,
       HttpServletResponse response) {
     String path = viewstatehandler.getFullURL(viewparams);
     path = RenderUtil.appendAttributes(path, RenderUtil
         .makeURLAttributes(outgoingparams));
     // TODO: This is a hack, pending a bit more thought.
 
     Logger.log.info("Redirecting to " + path);
     try {
       response.sendRedirect(path);
     }
     catch (IOException ioe) {
       Logger.log.warn("Error redirecting to URL " + path, ioe);
     }
   }
 
 
   public void renderFatalError(Throwable t, PrintOutputStream pos) {
     // We may have such a fatal misconfiguration that we can't even rely on
     // IKAT to format this error message
     Logger.log.fatal("Completely fatal error populating view root", t);
 
     pos.println("<html><head><title>Internal Error</title></head></body><pre>");
     pos.println("Fatal internal error handling request: " + t);
     ErrorUtil.dumpStackTrace(t, pos);
     pos.println("</pre></body></html>");
     pos.close();
   }
   
   public static PrintOutputStream setupResponseWriter(
       HttpServletRequest request, HttpServletResponse response) {
     try {
       response.setContentType("text/html; charset=UTF-8");
       // response.setContentType("application/xhtml+xml; charset=UTF-8");
 
       OutputStream os = response.getOutputStream();
       PrintOutputStream pos = new OutputStreamPOS(os, "UTF-8");
 
       String acceptHeader = request.getHeader("Accept");
 
       // Work-around for JSF 1.0 RI bug: failing to accept "*/*" and
       // and "text/*" as valid replacements for "text/html"
       if (acceptHeader != null
           && (acceptHeader.indexOf("*/*") != -1 || acceptHeader
               .indexOf("text/*") != -1)) {
         acceptHeader += ",text/html";
       }
 
       String responseencoding = response.getCharacterEncoding();
       response.setHeader("Accept", acceptHeader);
       return pos;
     }
     catch (IOException ioe) {
       throw UniversalRuntimeException.accumulate(ioe,
           "Error setting up response writer");
     }
   }
 
 }
