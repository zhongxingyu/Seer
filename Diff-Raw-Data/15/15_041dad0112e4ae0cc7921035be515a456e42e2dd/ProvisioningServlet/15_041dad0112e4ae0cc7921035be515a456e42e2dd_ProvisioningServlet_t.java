 /*******************************************************************************
  * Copyright (c) 2010 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.p2.internal.deploy.server;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.swordfish.p2.deploy.server.IDirector;
 
 public class ProvisioningServlet extends HttpServlet {
 	/**
 	 * Constant parameter name for operation
 	 */
 	public static final String PARAM_IU_OPERATION = "iuOperation";
 
 	/**
 	 * Constant for install operation value
 	 */
 	public static final String OPERATION_INSTALL = "install";
 
 	/**
 	 * Constant for update operation value
 	 */
 	public static final String OPERATION_UPDATE = "update";
 
 	/**
 	 * Constant for remove operation value
 	 */
 	public static final String OPERATION_REMOVE = "remove";
 
 	/**
 	 * Constant parameter name for version
 	 */
 	public static final String PARAM_IU_VERSION = "iuVersion";
 	
 	/**
 	 * Constant parameter name for iu id
 	 */
 	public static final String PARAM_IU_ID = "iuId.";
 
 	/**
 	 * Constant parameter name for target profile (optional)
 	 */
 	public static final String PARAM_IU_TARGET_PROFILE = "iuTargetProfile";
 	static final String NEW_LINE = System.getProperty("line.separator", "\n");
 	private static final String READABLE_OPERATION = "{0}(\"{1}\")\n{2}";
 	private static final String ILLEGAL_OPERATION = "Illegal operation: \"{0}\".";
 	static final String DEFAULT_PROFILE = "Swordfish";
 	private static final long serialVersionUID = 1L;
 	private static final Log LOG = LogFactory.getLog(ProvisioningServlet.class);
 	private IDirector director;
 
 	public void setDirector(IDirector director) {
 		this.director = director;
 	}
 
 
 	IDirector getDirector() {
 		return this.director;
 	}
 
 
 	@Override
 	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String operation;
 		String[] iuList;
 		String iuTargetProfile;
 		
 		try {
 			iuTargetProfile = getSaveParameter(req, PARAM_IU_TARGET_PROFILE, DEFAULT_PROFILE);
 			operation = getSaveParameter(req, PARAM_IU_OPERATION);
 			iuList = getIUList(req);
 			IStatus result = null;
 
 			logOperation(operation, iuTargetProfile, iuList);
 			
 			if (OPERATION_INSTALL.equals(operation)) {
 				result = director.install(iuList, req.getInputStream(), iuTargetProfile);
 				
 			} else if (OPERATION_UPDATE.equals(operation)) {
 				result = director.update(iuList, req.getInputStream(), iuTargetProfile);
 				
 			} else if (OPERATION_REMOVE.equals(operation)) {
 				result = director.remove(iuList, iuTargetProfile);
 				
 			} else {
 				sendError(resp, MessageFormat.format(ILLEGAL_OPERATION, operation));
 			}
 
 			if (result != null && !result.isOK()) {
 				sendError(resp, formatError(operation, iuList, result));
 			}
 			
 		} catch (Exception e) {
 			sendError(resp, e.toString());
 		}
 	}
 
 	
 	final String getSaveParameter(HttpServletRequest request, String parameter, String defaultValue) throws ServletException {
 		String value = !isEmpty(request.getHeader(parameter)) ? request.getHeader(parameter) : defaultValue;
 		
 		if (isEmpty(value)) {
 			throw new ServletException("Value for parameter '" + parameter + "' missing: '" + value + "'");
 
 		} else {
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Got parameter '" + parameter + "' with value '" + value + "'");
 			}
 		}
 		
 		return value;
 	}
 
 	
 	final String getSaveParameter(HttpServletRequest request, String parameter) throws ServletException {
 		return getSaveParameter(request, parameter, null);
 	}
 
 	
 	final String[] getIUList(HttpServletRequest request) throws ServletException {
 		List<String> iuList = new ArrayList<String>();
		for (Enumeration<String> en = request.getHeaderNames(); en.hasMoreElements();) {
 			String pName = en.nextElement();
 			String value = request.getHeader(pName);
 
 			if (pName.startsWith(PARAM_IU_ID) && !isEmpty(value)) {
 				iuList.add(value);
 			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Header: "  + pName +  " = " + value);
				LOG.debug("List is now: " + list2String(iuList.toArray(new String[] {})));
			}
 		}
 		
 		if (iuList.size() == 0) {
 			throw new ServletException("No IU entry (" + PARAM_IU_ID + "*) given.");
 		}
 		
 		return iuList.toArray(new String[] {});
 	}
 
 	
 	final void sendError(HttpServletResponse resp, String error) throws IOException {
 		LOG.error(error);
 		resp.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
 	}
 	
 
 	final String formatError(String operation, String[] iuList, IStatus status) {
 		return MessageFormat.format(READABLE_OPERATION, operation, list2String(iuList)
 				, status.getMessage() + NEW_LINE + flattenStatus(status.getChildren(), "  "));
 	}
 	
 
 	private String flattenStatus(IStatus[] childs, String indent) {
 		StringBuffer sb = new StringBuffer();
 
 		for (int i = 0; (childs != null) && (i < childs.length); i++) {
 			sb.append(indent).append(childs[i].getMessage()).append(NEW_LINE);
 			sb.append(flattenStatus(childs[i].getChildren(), indent + "  "));
 		}
 		return sb.toString();
 	}
 
 	
 	private final static void logOperation(String operation, String profile, String... ius) {
 		if (LOG.isDebugEnabled()) {			
 			LOG.debug("Operation: "  + operation +  "    Profile: " + profile + "     IU: " + list2String(ius));
 		}
 	}
 	
 	
 	private final static String list2String(String... l) {
 		StringBuilder strb = new StringBuilder();
 		String delim = "";
 		
 		for (String iu : l) {
 			strb.append(delim);
 			strb.append(iu);
 			delim = ",";
 		}
 		
 		return strb.toString();
 	}
 
 
 	private final boolean isEmpty(String value) {
 		return (value == null || "".equals(value));
 	}
 }
