 package org.esupportail.opi.web.utils.exceptions;
 
 
 import org.esupportail.commons.services.exceptionHandling.ExceptionService;
 import org.esupportail.commons.services.exceptionHandling.ExceptionUtils;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 
 import javax.faces.FacesException;
 import javax.faces.application.NavigationHandler;
 import javax.faces.context.ExceptionHandler;
 import javax.faces.context.ExceptionHandlerWrapper;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ExceptionQueuedEvent;
 import javax.faces.event.ExceptionQueuedEventContext;
 import javax.servlet.http.HttpServletRequest;
 import java.util.Iterator;
 
 public class ExHandler extends ExceptionHandlerWrapper {
 
     private final Logger log = new LoggerImpl(getClass());
 
 
     private ExceptionHandler wrapped;
 
     public ExHandler() {
         super();
     }
 
     public ExHandler(final ExceptionHandler wrapped) {
         super();
         this.wrapped = wrapped;
     }
 
     @Override
     public ExceptionHandler getWrapped() {
         if (log.isDebugEnabled()) {
             log.debug("entering ExceptionHandler::getWrapped");
         }
         return this.wrapped;
     }
 
     @Override
     public void handle() throws FacesException {
         if (log.isDebugEnabled()) {
             log.debug("entering ExceptionHandler::handle");
         }
         Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();
         if (!i.hasNext()) {
             // At this point, the queue will not contain any ViewExpiredEvents.
             // Therefore, let the parent handle them.
             getWrapped().handle();
         } else {
             ExceptionQueuedEvent event = i.next();
             ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
             Throwable t = context.getException();
             Throwable result = getRootCause(t);
             FacesContext fc = FacesContext.getCurrentInstance();
             ExceptionService e = null;
             HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
 
             try {
                 // IMPORTANT : Patch de org.esupportail.commons.exceptions.EsupExceptionHandler
                 // pour éviter une NPE
                 //e = ExceptionUtils.catchException(result.getCause());
                 e = ExceptionUtils.catchException(result);
                 request.getSession().setAttribute(ExceptionUtils.EXCEPTION_MARKER_NAME, e);
             } catch (Throwable th) {
                 log.error("problem to catch exception = " + th, th);
                 getWrapped().handle();
             }
            NavigationHandler navigation = fc.getApplication().getNavigationHandler();
            // Redirection vers la page des erreurs
            String view = e.getExceptionView();
            navigation.handleNavigation(fc, null, view);
            fc.renderResponse();
         }
     }
 }
