 package org.jcors.web;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.jcors.model.CorsHeaders;
 
 /**
  * Class that produces the correct instance for CORS requests handling 
  * 
  * @author Diego Silveira
  */
 public final class RequestHandlerFactory {
 
 	private static final Logger log = Logger.getLogger(RequestHandlerFactory.class);
 	
 	public static RequestHandler getRequestHandler(HttpServletRequest request) {
 	
 		// CORS Request
 		if(!isEmptyHeader(request, CorsHeaders.ORIGIN_HEADER)) {
 			
 			if(!isEmptyHeader(request, CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD_HEADER)) {
 				log.info("Handling preflight request");
 				return new PreflightRequestHandler();
 			}
 			
 			log.info("Handling actual request");
 			return new ActualRequestHandler();
 			
 		}
 		
 		log.info("Handling non-CORS request");
 		return new SimpleRequestHandler();
 	}
 	
 	private static boolean isEmptyHeader(HttpServletRequest request, String header) {
 		
 		String headerValue = request.getHeader(header);
		return headerValue == null || "".equals(headerValue.trim());
 	}
 	
 }
