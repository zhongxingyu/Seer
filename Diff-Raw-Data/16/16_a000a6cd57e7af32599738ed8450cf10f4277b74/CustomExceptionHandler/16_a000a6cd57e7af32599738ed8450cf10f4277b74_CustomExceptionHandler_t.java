 package com.bc.d3.faces;
 
 import javax.faces.FacesException;
 import javax.faces.application.ViewExpiredException;
 import javax.faces.context.ExceptionHandler;
 import javax.faces.context.ExceptionHandlerWrapper;
 import javax.faces.event.ExceptionQueuedEvent;
 import javax.faces.event.ExceptionQueuedEventContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.bc.d3.NotFoundException;
 import com.bc.d3.Outcome;
 
 @Configurable
 public class CustomExceptionHandler extends ExceptionHandlerWrapper {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(CustomExceptionHandler.class);
 
 	private ExceptionHandler wrapped;
 
 	@Autowired
 	private FacesUtils facesUtil;
 
 	public CustomExceptionHandler(ExceptionHandler wrapped) {
 		this.wrapped = wrapped;
 	}
 
 	@Override
 	public ExceptionHandler getWrapped() {
 		return this.wrapped;
 	}
 
 	@Override
 	public void handle() throws FacesException {
 		for (ExceptionQueuedEvent event : getUnhandledExceptionQueuedEvents()) {
 			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext)event.getSource();
 			Throwable exception = context.getException();
 			do {
 				if (handleException(exception)) {
 					return;
 				}
 				exception = exception.getCause();
 			} while (exception != null);
 		}
 
 		// exception trapped is not defined in bliiss.web; process normally now
 		getWrapped().handle();
 	}
 
 	private boolean handleException(Throwable exception) {
 		if (exception instanceof NotFoundException) {
 			logger.warn("NotFoundException encountered.", exception);
 			facesUtil.handleNavigation(null, Outcome.NOT_FOUND);
			facesUtil.responseComplete();
 			return true;
 		}
 		if (exception instanceof ViewExpiredException) {
 			logger.warn("ViewExpiredException encountered.", exception);
 			facesUtil.handleNavigation(null, Outcome.VIEW_EXPIRED);
			facesUtil.responseComplete();
 			return true;
 		}
 		return false;
 	}
 
 }
