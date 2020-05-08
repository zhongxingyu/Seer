 package com.nelsonjrodrigues.twitter.web.rest.exceptionhandler;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.http.HttpStatus;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
 
 public class RestApiExceptionHandler extends AbstractHandlerExceptionResolver {
 
 	@Override
 	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
 		if (ex instanceof EmptyResultDataAccessException) {
 			try {
 				response.sendError(HttpStatus.NOT_FOUND.value());
 			} catch (Exception e) {
 				logger.warn("Handling of exception threw exception", e);
 			}
 		}
 
		return null;
 	}
 
 }
