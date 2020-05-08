 package com.fabiale.vegetarianguide.web;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javassist.NotFoundException;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
import javax.servlet.http.HttpSession;
 
 import org.hibernate.exception.ConstraintViolationException;
 import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
 
 import com.dropbox.client2.exception.DropboxException;
 import com.fabiale.vegetarianguide.exception.RestaurantNotFoundException;
 
 @ControllerAdvice
 public class ExceptionHandlerBinder {
 
 	private Logger logger = Logger.getLogger(ExceptionHandlerBinder.class.getName());
 
 	@ExceptionHandler({ RestClientException.class })
 	public ModelAndView handleException(final RestClientException exception) {
 		logger.log(Level.SEVERE, "Exception found: " + exception, exception);
 		return errorModelAndView(exception, "");
 	}
 	
 	@ExceptionHandler({ DropboxException.class })
 	public ModelAndView handleException(final DropboxException exception) {
 		logger.log(Level.SEVERE, "Exception found: " + exception, exception);
 		return errorModelAndView(exception, "");
 	}
 	
 	@ExceptionHandler({ NotFoundException.class })
 	public ModelAndView handleException(final NotFoundException exception) {
 		logger.log(Level.SEVERE, "Exception found: " + exception, exception);
 		return errorModelAndView(exception, "");
 	}
 	
 	@ExceptionHandler({ RestaurantNotFoundException.class })
 	public ModelAndView handleException(final RestaurantNotFoundException exception) {
 		logger.log(Level.SEVERE, "Exception found: " + exception, exception);
 		return errorModelAndView(exception, "restaurant.not.found");
 	}
 	
 	@ExceptionHandler({ IOException.class })
 	public ModelAndView handleException(final IOException exception) {
 		logger.log(Level.SEVERE, "Exception found: " + exception, exception);
 		return errorModelAndView(exception, "");
 	}
 
 	@ExceptionHandler(DataIntegrityViolationException.class)
 	public ModelAndView handleIOException(DataIntegrityViolationException ex) {
 		ConstraintViolationException constraint = (ConstraintViolationException) ex.getCause();
 		logger.log(Level.SEVERE, "handleIOException - Catching: " + ex.getClass().getSimpleName(), ex);
 		return errorModelAndView(ex, constraint.getConstraintName() + ".error");
 	}
 	
 	@ExceptionHandler(AddressException.class)
 	public ModelAndView handleIOException(AddressException ex) {
 		logger.log(Level.SEVERE, "Exception found: " + ex, ex);
 		return errorModelAndView(ex, "");
 	}
 	
 	@ExceptionHandler(MessagingException.class)
 	public ModelAndView handleIOException(MessagingException ex) {
 		logger.log(Level.SEVERE, "Exception found: " + ex, ex);
 		return errorModelAndView(ex, "");
 	}
 	
 	@ExceptionHandler(NumberFormatException.class)
 	public ModelAndView handleIOException(NumberFormatException ex) {
 		logger.log(Level.SEVERE, "Exception found: " + ex, ex);
 		return errorModelAndView(ex, "");
 	}
 	
 	@ExceptionHandler(Throwable.class)
 	public ModelAndView handleIOException(Throwable ex) {
 		logger.log(Level.SEVERE, "Exception found: " + ex, ex);
 		return errorModelAndView(ex, "system.error");
 	}
 	
	@ExceptionHandler(AccessDeniedException.class)
	public ModelAndView handleIOException(AccessDeniedException ex, HttpSession session) {
		logger.log(Level.INFO, "Access denied" + ex);
		session.setAttribute("current", "");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("error");
		modelAndView.addObject("warn", "login.request");

		return modelAndView;
	}
	
 	@ExceptionHandler(NoSuchRequestHandlingMethodException.class)
 	public ModelAndView handleIOException(NoSuchRequestHandlingMethodException ex) {
 		logger.log(Level.SEVERE, "Exception found: " + ex, ex);
 		return errorModelAndView(ex, "page.not.found");
 	}
 
 	/**
 	 * Get the users details for the 'personal' page
 	 */
 	private ModelAndView errorModelAndView(Throwable ex, String msg) {
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.setViewName("error");
		modelAndView.addObject("error", msg);
 
 		return modelAndView;
 	}
 }
