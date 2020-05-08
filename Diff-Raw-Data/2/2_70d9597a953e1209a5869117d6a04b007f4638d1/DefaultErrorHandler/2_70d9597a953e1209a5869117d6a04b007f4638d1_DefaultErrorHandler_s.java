 /*
  * DefaultErrorHandler.java (weberknecht)
  *
  * Copyright 2012-2013 Patrick Mairif.
  * The program is distributed under the terms of the Apache License (ALv2).
  *
  * tabstop=4, charset=UTF-8
  */
 package de.highbyte_le.weberknecht.request.error;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import de.highbyte_le.weberknecht.Controller;
 import de.highbyte_le.weberknecht.db.DBConnectionException;
 import de.highbyte_le.weberknecht.request.ContentProcessingException;
 import de.highbyte_le.weberknecht.request.actions.ActionExecutionException;
 import de.highbyte_le.weberknecht.request.actions.ActionInstantiationException;
 import de.highbyte_le.weberknecht.request.actions.ActionNotFoundException;
 import de.highbyte_le.weberknecht.request.processing.ProcessingException;
 import de.highbyte_le.weberknecht.request.routing.RoutingTarget;
 
 /**
  * error handler used by default
  *
  * @author pmairif
  */
 public class DefaultErrorHandler implements ErrorHandler {
 
 	private int statusCode;
 
 	/**
 	 * Logger for this class
 	 */
 	private final Log log = LogFactory.getLog(Controller.class);
 
 	/* (non-Javadoc)
 	 * @see de.highbyte_le.weberknecht.request.ErrorHandler#handleException(java.lang.Exception, javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	public boolean handleException(Exception exception, HttpServletRequest request, RoutingTarget routingTarget) {
 		if (exception instanceof ActionNotFoundException) {
 			log.warn("action not found: "+exception.getMessage()+"; request URI was "+request.getRequestURI());
 			this.statusCode = HttpServletResponse.SC_NOT_FOUND;	//throw 404, if action doesn't exist
 		}
 		else if (exception instanceof ContentProcessingException) {
			log.error("doGet() - ContentProcessingException: "+exception.getMessage());	//$NON-NLS-1$
 			this.statusCode =  ((ContentProcessingException) exception).getHttpStatusCode();
 			//TODO error page with error message or set request attribute to be able to write it on standard error pages 
 		}
 		else if (exception instanceof ActionInstantiationException) {
 			log.warn("action could not be instantiated: "+exception.getMessage(), exception);
 			this.statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;	//throw 500, if action could not instantiated
 		}
 		else if (exception instanceof ProcessingException) {
 			log.error("doGet() - PreProcessingException: "+exception.getMessage(), exception);	//$NON-NLS-1$
 			this.statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;	//throw 500
 		}
 		else if (exception instanceof DBConnectionException) {
 			log.error("doGet() - DBConnectionException: "+exception.getMessage(), exception);	//$NON-NLS-1$
 			this.statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;	//throw 500
 		}
 		else if (exception instanceof ActionExecutionException) {
 			log.error("doGet() - ActionExecutionException: "+exception.getMessage(), exception);	//$NON-NLS-1$
 			this.statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;	//throw 500
 		}
 		else {
 			log.error("doGet() - "+exception.getClass().getSimpleName()+": "+exception.getMessage(), exception);	//$NON-NLS-1$
 			this.statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;	//throw 500
 		}
 		
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.highbyte_le.weberknecht.request.ErrorHandler#getStatus()
 	 */
 	@Override
 	public int getStatus() {
 		return statusCode;
 	}
 
 	/**
 	 * @param statusCode the statusCode to set
 	 */
 	protected void setStatusCode(int statusCode) {
 		this.statusCode = statusCode;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.highbyte_le.weberknecht.request.view.AutoView#getViewProcessorName()
 	 */
 	@Override
 	public String getViewProcessorName() {
 		return null;	//no view, let the controller use sendError()
 	}
 }
