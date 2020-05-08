 /**
  * Copyright (c) 2009 Aurora Software Technology Studio. All rights reserved.
  */
 package com.corona.servlet.handling.resource;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.corona.context.annotation.Application;
 import com.corona.logging.Log;
 import com.corona.logging.LogFactory;
 import com.corona.servlet.HandleException;
 import com.corona.servlet.Handler;
 import com.corona.servlet.MatchResult;
 import com.corona.servlet.Matcher;
 import com.corona.util.StringUtil;
 
 /**
  * <p>This handler is used to serve response with static files under root by matching URI and
  * file name. </p>
  *
  * @author $Author$
  * @version $Id$
  */
 @Application
 public class ResourceHandler implements Handler {
 
 	/**
 	 * the logger
 	 */
 	private final Log logger = LogFactory.getLog(ResourceHandler.class);
 	
 	/**
 	 * the match priority
 	 */
 	private int priority = Integer.MAX_VALUE;
 	
 	/**
 	 * the request URI head to match
 	 */
 	private String head = null;
 	
 	/**
 	 * the expiration for content (< 0, don't care; 0, no cache, > 0, cache)
 	 */
 	private long expiration = -1;
 	
 	/**
 	 * the welcome file name
 	 */
 	private String welcomeFileName = "";
 	
 	/**
 	 * the matcher
 	 */
 	private ResourceMatcher matcher = new ResourceMatcher(this);
 	
 	/**
 	 * {@inheritDoc}
 	 * @see com.corona.servlet.Handler#getMatcher()
 	 */
 	@Override
 	public Matcher getMatcher() {
 		return this.matcher;
 	}
 
 	/**
 	 * @return the match priority
 	 */
 	public int getPriority() {
 		return this.priority;
 	}
 	
 	/**
 	 * @param priority the match priority
 	 */
 	public void setPriority(final int priority) {
 		this.priority = priority;
 	}
 	
 	/**
 	 * @return the request URI head to match
 	 */
 	public String getHead() {
 		return head;
 	}
 	
 	/**
 	 * @param head the request URI head to match
 	 */
 	public void setHead(final String head) {
 		this.head = head;
 	}
 	
 	/**
 	 * @return how to cache the content in client
 	 */
 	public long getExpiration() {
 		return expiration;
 	}
 	
 	/**
 	 * @param expiration how to cache the content in client
 	 */
 	public void setExpiration(final long expiration) {
 		this.expiration = expiration;
 	}
 
 	/**
 	 * @return the welcome file name
 	 */
 	public String getWelcomeFileName() {
 		return welcomeFileName;
 	}
 	
 	/**
 	 * @param welcomeFileName the welcome file name to set
 	 */
 	public void setWelcomeFileName(final String welcomeFileName) {
 		
 		this.welcomeFileName = welcomeFileName;
 		if (StringUtil.isBlank(this.welcomeFileName)) {
 			this.welcomeFileName = "";
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see com.corona.servlet.Handler#handle(
 	 * 	com.corona.servlet.MatchResult, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse
 	 * )
 	 */
 	@Override
 	public void handle(final MatchResult result, final HttpServletRequest request, final HttpServletResponse response)
 			throws HandleException {
 		
 		// set HTTP expires by Expires annotation in producer method of component
 		if (this.expiration == 0) {
 			
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setDateHeader("Last-Modified", System.currentTimeMillis());
 		} else if (this.expiration > 0) {
 			
 			Long current = System.currentTimeMillis();
 			response.setHeader("Cache-Control", "max-age=" + (this.expiration / 1000));
 			response.setDateHeader("Expires", current + this.expiration);
 			response.setDateHeader("Last-Modified", current);
 		}
 
 		// fill response stream by resource file under web root
 		String path = request.getPathInfo();
 		if (path.endsWith("/")) {
 			path = path + this.welcomeFileName;
 		}
		InputStream in = request.getSession().getServletContext().getResourceAsStream(path);
 		if (in != null) {
 			try {
 				
 				OutputStream out = response.getOutputStream();
 				for (int c = in.read(); c != -1; c = in.read()) {
 					out.write(c);
 				}
 			} catch (Exception e) {
 				
 				this.logger.error("Fail to fill response by resource file [{0}]", e, path);
 				throw new HandleException("Fail to fill response by resource file [{0}]", e, path);
 			} finally {
 				
 				try {
 					in.close();
 				} catch (IOException e) {
 					this.logger.error("Fail to close resource file [{0}], just skip this exception", e, path);
 				}
 			}
 		} else {
 			
 			try {
 				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page Not Found!");
 			} catch (IOException e) {
 				this.logger.error("Fail to send \"Page Not Found\" to response, just skip this exception", e, path);
 			}
 		}
 	}
 }
