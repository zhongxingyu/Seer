 package ch.helvetia.jax2011.error;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.ejb.NoSuchEJBException;
 import javax.enterprise.context.NonexistentConversationException;
 import javax.faces.FacesException;
 import javax.faces.application.NavigationHandler;
 import javax.faces.application.ViewExpiredException;
 import javax.faces.context.ExceptionHandler;
 import javax.faces.context.ExceptionHandlerWrapper;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ExceptionQueuedEvent;
 import javax.persistence.OptimisticLockException;
 
 import org.jboss.logging.Logger;
 import org.jboss.weld.Container;
 import org.jboss.weld.context.http.HttpConversationContext;
 
 import ch.helvetia.jax2011.security.Identity;
 import ch.helvetia.jax2011.util.BeanManagerUtil;
 
 /**
  * JSF ExceptionHandler for the Todo App
 * TODO Use Seam Catch
  */
 public class TodoExceptionHandler extends ExceptionHandlerWrapper {
 
 	private transient final Logger log = Logger.getLogger(TodoExceptionHandler.class);
 
 	private static final String ERROR_PAGE = "/error.xhtml";
 
 	private final ExceptionHandler wrappedExceptionHandler;
 
 	public TodoExceptionHandler(ExceptionHandler wrapped) {
 		this.wrappedExceptionHandler = wrapped;
 	}
 
 	@Override
 	public ExceptionHandler getWrapped() {
 		return this.wrappedExceptionHandler;
 	}
 
 	@Override
 	public void handle() throws FacesException {
 		Iterator<ExceptionQueuedEvent> it = getUnhandledExceptionQueuedEvents().iterator();
 		if (it.hasNext()) {
 			log.trace("Handling first exception");
 			ExceptionQueuedEvent event = it.next();
 			Throwable t = event.getContext().getException();
 
 			// TODO Use Seam Faces
 			FacesContext facesContext = event.getContext().getContext();
 			NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();
 
			activateConversationContext();
 			HandledException handledException = handleException(t, facesContext);
 
 			ErrorInfo errorInfo = getErrorInfo();
 			errorInfo.setHandledException(handledException);
 			errorInfo.setStacktrace(printHtmlStackTrace(handledException));
 			errorInfo.setUser(getUsername());
 			errorInfo.setDate(new Date());
 			navigationHandler.handleNavigation(facesContext, null, ERROR_PAGE + "?" + "faces-redirect=true");
 			facesContext.renderResponse();
 		}
 	}
 
 	private HandledException handleException(Throwable t, FacesContext facesContext) {
 		ResourceBundle messages = this.getMessageBundle(facesContext);
 		List<Throwable> exceptionStack = this.createExceptionStack(t);
 		for (Throwable cause : exceptionStack) {
 			if (cause instanceof NonexistentConversationException) {
 				this.log.debugv(t, "Handling conversation timeout exception {0}", cause.getClass().getName());
 				return new HandledException(messages.getString("error.sessionTimeout"), cause);
 			} else if (cause instanceof ViewExpiredException) {
 				this.log.debugv(t, "Handling session timeout exception {0}", cause.getClass().getName());
 				return new HandledException(messages.getString("error.sessionTimeout"), cause);
 			} else if (cause instanceof OptimisticLockException) {
 				this.log.debugv(t, "Handling concurrency exception {0}", cause.getClass().getName());
 				return new HandledException(messages.getString("error.concurrency"), cause);
 			} else if (cause instanceof NoSuchEJBException) {
 				this.log.debugv(t, "Handling concurrency exception {0}", cause.getClass().getName());
 				return new HandledException(messages.getString("error.concurrency"), cause);
 			}
 		}
 		this.log.errorv(t, "Handling exception {0}", t.getClass().getName());
 		return new HandledException(messages.getString("error.general"), t);
 	}
 
 	private ErrorInfo getErrorInfo() {
 		return BeanManagerUtil.getInstance(ErrorInfo.class);
 	}
 
 	// TODO Use Seam Solder
 	private ResourceBundle getMessageBundle(FacesContext facesContext) {
 		Locale locale;
 		if (facesContext.getViewRoot() != null) {
 			locale = facesContext.getViewRoot().getLocale();
 		} else {
 			locale = Locale.getDefault();
 		}
 		return ResourceBundle.getBundle("messages", locale, Thread.currentThread().getContextClassLoader());
 	}
 
 	private List<Throwable> createExceptionStack(Throwable t) {
 		List<Throwable> stack = new ArrayList<Throwable>();
 		for (Throwable cause = t; cause != null; cause = cause.getCause()) {
 			stack.add(cause);
 		}
 		Collections.reverse(stack);
 		return stack;
 	}
 
 	// TODO Use Seam Conversation
	private void activateConversationContext() {
 		HttpConversationContext conversationContext = Container.instance().deploymentManager().instance()
 				.select(HttpConversationContext.class).get();
 		if (!conversationContext.isActive()) {
 			conversationContext.activate();
 		}
 	}
 
 	private String getUsername() {
 		try {
 			return BeanManagerUtil.getInstance(Identity.class).getUser().getName();
 		} catch (Exception ex) {
 			return "Unknown";
 		}
 	}
 
 	// TODO Security risk! Use HTML escaping.
 	private String printHtmlStackTrace(Throwable t) {
 		StringBuilder stackTrace = new StringBuilder();
 		stackTrace.append("<span>");
 		stackTrace.append(t);
 		stackTrace.append("</span>");
 		for (StackTraceElement element : t.getStackTrace()) {
 			stackTrace.append("<br/>");
 			stackTrace.append("<span style=\"padding-left: 20px;\">");
 			stackTrace.append("at " + element);
 			stackTrace.append("</span>");
 		}
 		for (Throwable cause = t.getCause(); cause != null; cause = cause.getCause()) {
 			stackTrace.append("<br/>");
 			stackTrace.append("<span>");
 			stackTrace.append("Caused by: " + cause);
 			stackTrace.append("</span>");
 			for (StackTraceElement element : cause.getStackTrace()) {
 				stackTrace.append("<br/>");
 				stackTrace.append("<span style=\"padding-left: 20px;\">");
 				stackTrace.append("at " + element);
 				stackTrace.append("</span>");
 			}
 		}
 		return stackTrace.toString();
 	}
 
}
