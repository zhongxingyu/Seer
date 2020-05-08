 package com.bradmcevoy.http;
 
 import java.io.PrintWriter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.bradmcevoy.http.exceptions.ConflictException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 
 public abstract class Handler {
     
     private static final Logger log = LoggerFactory.getLogger(Handler.class);
     
     public static final String METHOD_NOT_ALLOWED_HTML = "<html><body><h1>Method Not Allowed</h1></body></html>";
     public static final String NOT_FOUND_HTML = "<html><body><h1>Not Found (404)</h1></body></html>";
     public static final String METHOD_NOT_IMPLEMENTED_HTML = "<html><body><h1>Method Not Implemented</h1></body></html>";
     public static final String CONFLICT_HTML = "<html><body><h1>Conflict</h1></body></html>";
 
     protected final HttpManager manager;
 
     public abstract void process(HttpManager httpManager, Request request, Response response) throws ConflictException, NotAuthorizedException;
     
     protected abstract boolean isCompatible(Resource r);
     
     /** The method that this handler handles
      */
     abstract Request.Method method();
 
     
     public Handler(HttpManager manager) {
         this.manager = manager;                
     }
 
     protected ResponseHandler getResponseHandler() {
         return manager.getResponseHandler();
     }
 
     /**
      * Checks the expect header, and responds if necessary
      * 
      * @param resource
      * @param request
      * @param response
      * @return - true if the expect header is ok
      */
     protected boolean checkExpects( Request request, Response response) {
         String s = request.getExpectHeader(); 
         if( s != null && s.length() > 0 ) {
             getResponseHandler().respondExpectationFailed(response, request );
             return false;
         } else {
             return true;
         }
     }
 
     protected boolean checkAuthorisation(Resource handler, Request request) {
         Auth auth = request.getAuthorization();
         if( auth != null ) {
             Object authTag = handler.authenticate(auth.user,auth.password);
             if( authTag == null ) {
                 log.warn("failed to authenticate");
                 auth = null;
             } else {
                 auth.setTag(authTag);
             }
         } else {
             auth = manager.getSessionAuthentication(request);
         }
         
         
         boolean authorised = handler.authorise(request,request.getMethod(),auth);
         if( !authorised ) {
             log.warn("Not authorised, requesting basic authentication");
             return false;
         } else {
             return true;
         }
     }
 
     
     
     protected void respondUnauthorised(Resource resource, Response response, Request request) {
         manager.getResponseHandler().respondUnauthorised(resource, response, request);
     }
 
     protected void respondMethodNotImplemented(Resource resource, Response response, Request request) {
         manager.getResponseHandler().respondMethodNotImplemented(resource, response, request);
     }
 
     protected void respondMethodNotAllowed(Resource res, Response response, Request request) {
         manager.getResponseHandler().respondMethodNotAllowed(res, response, request);
     }
 
     protected void respondConflict(Resource resource, Response response, Request request) {
         manager.getResponseHandler().respondConflict(resource, response, request,null);
     }
     
     protected void respondRedirect(Response response, Request request, String redirectUrl) {
         manager.getResponseHandler().respondRedirect(response, request, redirectUrl);
     }
    
     protected  void respondNotFound(Response response, Request request) {
         getResponseHandler().respondNotFound(response, request);
     }
 
     protected void output(final Response response, final String s) {
         PrintWriter pw = new PrintWriter(response.getOutputStream(),true);
         pw.print(s);
         pw.flush();
     }
  
 	protected boolean isLockedOut(Request inRequest, Resource inResource)
 	{
 		if( inResource == null || !(inResource instanceof LockableResource))
 		{
 			return false;
 		}
 		LockableResource lr = (LockableResource)inResource;
 		LockToken token = lr.getCurrentLock();
 		if( token != null)
 		{
 			Auth auth = inRequest.getAuthorization();
			String owner = token.info.owner;
			if( !owner.equals(auth.getUser()))
 			{
	    	    log.info("fail: lock owned by: " + owner + " not by " + auth.getUser());
 	    	    String value = inRequest.getHeaders().get("If");
 	    	    if( value != null)
 	    	    {
 	    	    	if( value.contains("opaquelocktoken:" + token.tokenId + ">") )
 	    	    	{
 	    	    		log.info("Contained valid token. so is unlocked");
 	    	    		return false;
 	    	    	}
 	    	    }
 				return true;
 			}
 		}
 		return false;
 	}
 
     
 }
