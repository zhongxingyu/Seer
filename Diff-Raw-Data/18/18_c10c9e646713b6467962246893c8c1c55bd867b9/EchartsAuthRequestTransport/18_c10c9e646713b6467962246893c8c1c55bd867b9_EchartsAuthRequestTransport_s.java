 package org.eastway.echartsrequest.client;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.web.bindery.requestfactory.gwt.client.DefaultRequestTransport;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
 public class EchartsAuthRequestTransport extends DefaultRequestTransport {
 	private final EventBus eventBus;
 
 	public EchartsAuthRequestTransport(EventBus eventBus) {
 		this.eventBus = eventBus;
 	}
 
 	@Override
 	protected RequestCallback createRequestCallback(final TransportReceiver receiver) {
 		final RequestCallback superCallback = super.createRequestCallback(receiver);
 		return new RequestCallback() {
 			@Override
 			public void onResponseReceived(Request request, Response response) {
 				if (Response.SC_UNAUTHORIZED == response.getStatusCode()) {
 					String loginUrl = response.getHeader("login");
 					if (loginUrl != null) {
 						receiver.onTransportFailure(new ServerFailure("Unauthenticated user", null, null, false));
 						eventBus.fireEvent(new EchartsAuthenticationFailureEvent(loginUrl));
 						return;
 					}
 				} else if (response.getStatusCode() != Response.SC_OK) {
					receiver.onTransportFailure(new ServerFailure("Server failure: " + response.getStatusCode(), null, null, true));
 					return;
 				}
 				superCallback.onResponseReceived(request, response);
 			}
 
 			@Override
 			public void onError(Request request, Throwable exception) {
 				superCallback.onError(request, exception);
 			}
 		};
 	}
 }
