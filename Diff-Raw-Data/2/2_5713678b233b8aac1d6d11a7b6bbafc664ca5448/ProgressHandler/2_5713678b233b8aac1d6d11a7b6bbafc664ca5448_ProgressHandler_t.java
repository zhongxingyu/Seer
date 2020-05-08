 package org.varoa.soundcloud.handlers;
 
 import java.io.IOException;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.nio.entity.NStringEntity;
 import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
 import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
 import org.apache.http.nio.protocol.HttpAsyncExchange;
 import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
 import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
 import org.apache.http.protocol.HttpContext;
 import org.apache.log4j.Logger;
 import org.varoa.soundcloud.SessionExtractor;
 
 /**
  * Informs about the state of the active upload of the current session, if any.
  */
 public class ProgressHandler implements HttpAsyncRequestHandler<HttpRequest> {
 
 	private static Logger log = Logger.getLogger(ProgressHandler.class);
 
 	@Override
 	final public void handle(final HttpRequest request, final HttpAsyncExchange httpexchange, final HttpContext context) throws HttpException, IOException {
 		String sessionId = SessionExtractor.extractSession(context);
 		Float progress = UploadRequestTracker.getInstance().getUploadProgressPercent(sessionId);
		log.debug("Poll received for session: \"" + sessionId + "\", sending " + progress);
 		HttpResponse response = httpexchange.getResponse();
 		NStringEntity stringEntity = new NStringEntity((progress == null)?"NULL" : progress.toString());
 		response.setEntity(stringEntity);
 		httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
 	}
 
 	@Override
 	public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest arg0, HttpContext arg1) throws HttpException, IOException {
 		return new BasicAsyncRequestConsumer();
 	}
 
 }
