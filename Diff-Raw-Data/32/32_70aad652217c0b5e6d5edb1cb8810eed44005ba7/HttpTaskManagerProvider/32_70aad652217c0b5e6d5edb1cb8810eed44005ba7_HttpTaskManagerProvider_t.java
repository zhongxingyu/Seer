 package org.cotrix.web.common.server.async;
 
 import static org.cotrix.common.Constants.*;
 
 import javax.annotation.Priority;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.inject.Alternative;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 
 import org.cotrix.common.async.TaskManagerProvider;
 import org.cotrix.web.common.server.util.HttpServletRequestHolder;
 import org.jboss.weld.context.http.HttpRequestContext;
 import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.servlet.SessionHolder;
 
 @ApplicationScoped @Alternative 
 @Priority(RUNTIME)
 public class HttpTaskManagerProvider implements TaskManagerProvider {
 	
 	@Inject 
 	HttpSessionContext httpContext;
 	
 	@Inject 
 	HttpRequestContext httpReqContext;
 	
 	@Inject
 	private HttpServletRequestHolder holder;
 
 
 	public class HttpTaskManager implements TaskManager {
 		
 		final HttpServletRequest request;
 		
 		public HttpTaskManager(HttpServletRequest req) {
 			this.request = req;
 		}
 		
 		@Override
 		public void started() {
			
			//WELD 2.0.4 final requires this workaround
			SessionHolder.sessionCreated(request.getSession());
 		
 			httpReqContext.associate(request);
 			httpReqContext.activate();
 			
 			httpContext.associate(request);
 			httpContext.activate();
 		}
 		
 		@Override
 		public void finished() {
 			
 			httpReqContext.dissociate(request);
 			httpContext.dissociate(request);
 		
 		}
 	}
 	
 	@Override
 	public TaskManager get() {
 		return new HttpTaskManager(holder.getRequest());
 	}
 	
 	
 }
