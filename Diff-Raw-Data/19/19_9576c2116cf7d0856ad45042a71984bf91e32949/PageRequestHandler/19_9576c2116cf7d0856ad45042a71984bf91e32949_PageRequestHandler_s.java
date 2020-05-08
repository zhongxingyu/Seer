 /*
  * Copyright 2009 Thomas Spencer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.tpspencer.tal.mvc.servlet.handlers;
 
 import java.io.IOException;
 import java.util.Enumeration;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.tpspencer.tal.mvc.model.StandardModel;
 import org.tpspencer.tal.mvc.process.ModelAttributeResolver;
 import org.tpspencer.tal.mvc.servlet.MVCRequestHandler;
 import org.tpspencer.tal.mvc.servlet.RequestCoordinates;
 import org.tpspencer.tal.mvc.servlet.model.CookieModelAttributeResolver;
 import org.tpspencer.tal.mvc.servlet.util.RequestAttributeUtils;
 import org.tpspencer.tal.mvc.servlet.util.RequestLogUtils;
 
 /**
  * This class aids the dispatching servlet to render
  * an entire page.
  * 
  * @author Tom Spencer
  */
 public class PageRequestHandler implements MVCRequestHandler {
 	private static final Log logger = LogFactory.getLog(PageRequestHandler.class);
 	
 	/**
 	 * Always returns false
 	 */
 	public boolean canHandlePost() {
 		return false;
 	}
 	
 	/**
 	 * Nothing to do as coordinates will always have a page
 	 */
 	public void validate(HttpServletRequest request, RequestCoordinates coords) {
 	}
 	
 	/**
 	 * Simply forwards request to the page template
 	 */
 	@SuppressWarnings("unchecked")
 	public void handleRequest(HttpServletRequest req, HttpServletResponse resp, ModelAttributeResolver resolver, RequestCoordinates coords) throws ServletException, IOException {
 		RequestLogUtils.traceRequestParameters(req, logger);
 		
 		StandardModel model = null;
 		
 		try {
 			model = new StandardModel(new CookieModelAttributeResolver(req, resp), true);
 			model.pushLayer(coords.getApp().getModel());
 			model.pushLayer(coords.getPage().getModel());
 			
 			RequestAttributeUtils.saveModel(req, model);
 			RequestAttributeUtils.saveCurrentApp(req, coords.getApp());
 			RequestAttributeUtils.saveCurrentPage(req, coords.getPage());
 			
 			// Update the model with any parameters in request if they are valid
 			Enumeration e = req.getParameterNames();
 			if( e != null ) {
 				while( e.hasMoreElements() ) {
 					String attr = (String)e.nextElement();
 					if( model.containsKey(attr) ) {
						model.setAttribute(attr, req.getParameter(attr));
 					}
 				}
 			}
 			
 			String template = coords.getPage().getTemplate();
 			if( logger.isTraceEnabled() ) logger.trace("\tIncluding page template: " + template);
 			req.getRequestDispatcher(template).forward(req, resp);
 		}
 		finally {
 			if( model != null ) {
 				model.popLayer(coords.getPage().getModel());
 				model.popLayer(coords.getApp().getModel());
 			}
 		}
 	}
 }
