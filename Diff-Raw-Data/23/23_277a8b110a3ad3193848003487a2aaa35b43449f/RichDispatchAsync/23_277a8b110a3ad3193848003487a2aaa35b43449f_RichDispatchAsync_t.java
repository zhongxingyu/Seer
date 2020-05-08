 package net.apptao.highway.client.dispatch;
 
 import net.apptao.highway.client.HwyClientCache;
 import net.apptao.highway.shared.dispatch.HwyCommand;
 import net.apptao.highway.shared.dispatch.HwyResult;
 import net.customware.gwt.dispatch.client.AbstractDispatchAsync;
 import net.customware.gwt.dispatch.client.ExceptionHandler;
 import net.customware.gwt.dispatch.client.secure.SecureSessionAccessor;
 import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
 import net.customware.gwt.dispatch.shared.Result;
 import net.customware.gwt.dispatch.shared.secure.InvalidSessionException;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.inject.Inject;
 
 public class RichDispatchAsync extends AbstractDispatchAsync {
 
 	private static final HwySecureDispatchServiceAsync realService = GWT.create( HwySecureDispatchService.class );
 	private static final String baseUrl = ((ServiceDefTarget)realService).getServiceEntryPoint() + "/";
	public static final int DEFAULT_MAX_RETRY = 3;
 
 
     private final SecureSessionAccessor secureSessionAccessor;
 	private HwyClientCache cache;
 
 	@Inject
 	public RichDispatchAsync(ExceptionHandler exceptionHandler, 
 			SecureSessionAccessor secureSessionAccessor, HwyClientCache cache) {
 		super(exceptionHandler);
 		this.secureSessionAccessor = secureSessionAccessor;
 		this.cache = cache;
 	}
 	
	public <A extends Action<R>, R extends Result> void execute( final A action, final AsyncCallback<R> callback ) {
		this.execute(action, DEFAULT_MAX_RETRY, callback);
	}
	
    private <A extends Action<R>, R extends Result> void execute( final A action, final int retriesLeft,  
     		final AsyncCallback<R> callback ) {
     	final boolean isCachedCommand = action instanceof HwyCachedCommand<?>;
     	if(isCachedCommand){
 			HwyResult result = cache.get((HwyCommand<HwyResult>)action);
 			if(result != null){
 				// command cache hit! return immediately 
 				RichDispatchAsync.this.onSuccess(action, (R) result, callback);
 			}
 		} else {
 			// normal call
     	
 	    	// Append action class name as extra path info
 	        String className = action.getClass().getName();
 	        int namePos = className.lastIndexOf(".") + 1;
 	        className = className.substring(namePos);
 	        ((ServiceDefTarget)realService).setServiceEntryPoint(baseUrl + className);
 	        
 	        String sessionId = secureSessionAccessor.getSessionId();
 	
 	        realService.execute(action, sessionId, new AsyncCallback<Result>() {
 	            public void onFailure( Throwable caught ) {
	            	if (!(caught instanceof ActionException) && retriesLeft > 0) {
	            		// This failure is not normal and
	            		// may be due to a bad connection
	            		// (i.e. Google shut down the app, probably).
	            		execute(action, retriesLeft - 1, callback);
	            	} else {
	            		// We've failed too many times or this is an exception 
	            		// thrown by the action.
	            		callback.onFailure(caught);
	            	} 
 	            	RichDispatchAsync.this.onFailure( action, caught, callback );
 	            }
 	
 	            @SuppressWarnings({"unchecked"})
 	            public void onSuccess( Result result ) {
 	            	// cache the result if need be
 	            	if(isCachedCommand){
 	            		cache.put((HwyCommand<HwyResult>)action, (HwyResult) result, 
 	            				((HwyCachedCommand)action).secondsUntilExpire());
 	            	}
 	            	RichDispatchAsync.this.onSuccess( action, (R) result, callback );
 	            }
 	        });
 		}
     }
 
     protected <A extends Action<R>, R extends Result> void onFailure( A action, Throwable caught, final AsyncCallback<R> callback ) {
         if ( caught instanceof InvalidSessionException ) {
             secureSessionAccessor.clearSessionId();
         }
 
         super.onFailure( action, caught, callback );
 
     }
 }
