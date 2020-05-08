 package net.cheney.motown.dispatcher;
 
 import net.cheney.motown.api.Request;
 import net.cheney.motown.api.Response;
 import net.cheney.motown.protocol.common.HttpRequestHandler;
 import net.cheney.motown.protocol.common.HttpResponseHandler;
 
 import org.apache.log4j.Logger;
 
 public abstract class Dispatcher implements HttpRequestHandler {
 	private static final Logger LOG = Logger.getLogger(Dispatcher.class);
 	
 	private final ResourceHandler dispatchable;
 
 	public Dispatcher(ResourceHandler dispatchable) {
 		this.dispatchable = dispatchable;
 	}
 	
 	public abstract void handleRequest(Request request, HttpResponseHandler responseHandler);
 	
 	protected Runnable createWorker(final Request request, final HttpResponseHandler responseHandler) {
 		return new Worker(request, responseHandler);
 	}
 	
 	protected final class Worker implements Runnable {
 
 		private final Request request;
 		private final HttpResponseHandler responseHandler;
 
 		public Worker(final Request request, final HttpResponseHandler responseHandler) {
 			this.request = request;
 			this.responseHandler = responseHandler;
 		}
 		
 		public final void run() {
 			try {
 				final Response response = dispatchable.dispatch(request);
 				boolean close = request.closeRequested();
 				if (!close) {
 					close = response.closeRequested();
 				}
 				responseHandler.sendResponse(response, close);
 			} catch (Throwable e) {
 				LOG.error("Unable to handle request: " + request.toString(), e);
 				responseHandler.sendResponse(Response.serverErrorInternal(), true);
 			}
 		}
 	}
 
 }
