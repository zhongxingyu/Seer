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
 import java.io.Writer;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Iterator;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.tpspencer.tal.mvc.View;
 import org.tpspencer.tal.mvc.Window;
 import org.tpspencer.tal.mvc.config.AppConfig;
 import org.tpspencer.tal.mvc.config.PageConfig;
 import org.tpspencer.tal.mvc.config.WindowConfig;
 import org.tpspencer.tal.mvc.model.ModelAttribute;
 import org.tpspencer.tal.mvc.model.ModelConfiguration;
 import org.tpspencer.tal.mvc.model.StandardModel;
 import org.tpspencer.tal.mvc.process.ModelAttributeResolver;
 import org.tpspencer.tal.mvc.render.BasicRenderModel;
 import org.tpspencer.tal.mvc.servlet.MVCRequestHandler;
 import org.tpspencer.tal.mvc.servlet.RequestCoordinates;
 import org.tpspencer.tal.mvc.servlet.UrlGenerator;
 import org.tpspencer.tal.mvc.servlet.util.RequestAttributeUtils;
 import org.tpspencer.tal.mvc.servlet.util.RequestLogUtils;
 import org.tpspencer.tal.mvc.servlet.util.ServletUrlGenerator;
 import org.tpspencer.tal.template.core.TemplateConfigurationLocator;
 import org.tpspencer.tal.util.htmlhelper.GenericElement;
 import org.tpspencer.tal.util.htmlhelper.HtmlConstants;
 
 /**
  * This class aids the dispatcher servlet in a window
  * request by finding the current state of the given
  * window and rendering it or dispatching the request
  * to the views template.
  * 
  * @author Tom Spencer
  */
 public class RenderRequestHandler implements MVCRequestHandler {
 	private static final Log logger = LogFactory.getLog(RenderRequestHandler.class);
 	
 	/**
 	 * Always returns false
 	 */
 	public boolean canHandlePost() {
 		return false;
 	}
 	
 	/**
 	 * Ensures there is a window to process. Also ensures that
 	 * there is a model, app and page in the current request
 	 * as this handler can only, erm, handle when included from
 	 * the page.
 	 */
 	public void validate(HttpServletRequest request, RequestCoordinates coords) {
 		if( coords.getWindow() == null ) throw new IllegalArgumentException("Cannot handle window render request with no window: " + coords);
 		if( RequestAttributeUtils.getModel(request) == null ) throw new IllegalArgumentException("Cannot handle window render request with no model in request: " + coords);
 		if( RequestAttributeUtils.getCurrentApp(request) == null ) throw new IllegalArgumentException("Cannot handle window render request with no current app in request: " + coords);
 		if( RequestAttributeUtils.getCurrentPage(request) == null ) throw new IllegalArgumentException("Cannot handle window render request with no current page in request: " + coords);
 	}
 	
 	/**
 	 * Obtains the view and passes to its prepareRender method.
 	 * If this returns a template then the template is included.
 	 */
 	@SuppressWarnings("unchecked")
 	public void handleRequest(HttpServletRequest req, HttpServletResponse resp, ModelAttributeResolver resolver, RequestCoordinates coords) throws ServletException, IOException {
 		RequestLogUtils.traceRequestAttributes(req, logger);
 		
 		StandardModel model = null;
 		WindowConfig window = null;
 		View view = null;
 		
 		try {
 			model = RequestAttributeUtils.getModel(req);
 			
 			window = coords.getWindow();
 			model.pushLayer(window.getModel());
 			RequestAttributeUtils.saveCurrentWindow(req, window);
 			
 			Window w = window.getWindow();
 			view = w.getCurrentState(model);
 			model.pushLayer(view.getModel());
 			
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
 			
 			// Output a hidden form with windows configuration
 			outputWindowModelForm(req, resp, coords.getApp(), coords.getPage(), window, w, model);
 			
 			// Ask current state for the render settings
 			BasicRenderModel renderModel = new BasicRenderModel();
 			view.prepareRender(renderModel, model);
 			
 			// Add on any auto attributes to model
 			if( window.getModel() != null ) {
 				Collection<ModelAttribute> attrs = window.getModel().getAttributes();
 				if( attrs != null ) {
 					Iterator<ModelAttribute> it = attrs.iterator();
 					while( it.hasNext() ) {
 						ModelAttribute attr = it.next();
 						if( attr.isAutoRenderAttribute() ) {
 							Object val = model.getAttribute(attr.getName());
 							if( val != null ) renderModel.setAttribute(attr.getName(), val);
 						}
 					}
 				}
 			}
 			
 			if( renderModel.getAttributes() != null ) RequestAttributeUtils.saveRenderModel(req, renderModel.getAttributes());
 			
 			dispatchView(req, resp, renderModel);
 		}
 		finally {
 			RequestAttributeUtils.clearCurrentWindow(req);
 			RequestAttributeUtils.clearRenderModel(req);
 			if( view != null ) model.popLayer(view.getModel());
 			if( window != null ) model.popLayer(window.getModel());
 		}
 	}
 	
 	/**
 	 * Resolves and dispatches the view. Initially this is hard coded
 	 * to support JSPs and treat all others a Web Templates
 	 * 
 	 * @param req
 	 * @param resp
 	 * @param model
 	 */
 	private void dispatchView(HttpServletRequest req, HttpServletResponse resp, BasicRenderModel model) throws IOException, ServletException {
 		String template = model.getTemplate();
 		
 		// See if its a template
 		if( TemplateConfigurationLocator.getInstance().getTemplate(template) != null ) {
 			template = "/template/" + template + ".html"; 
 		}
 		
 		if( logger.isTraceEnabled() ) logger.trace("\tIncluding template: " + template);
 		req.getRequestDispatcher(template).include(req, resp);
 	}
 	
 	/**
 	 * Helper to write out a hidden form with the windows model
 	 * attributes for AJAX requests. Only called for HTML renders
 	 * 
 	 * @param window The window
 	 */
 	protected void outputWindowModelForm(HttpServletRequest req, HttpServletResponse resp, AppConfig app, PageConfig page, WindowConfig config, Window window, StandardModel model) throws IOException {
 		ModelConfiguration originalModel = window.getModel();
 		
 		Writer writer = resp.getWriter();
 		
 		UrlGenerator urlGenerator = ServletUrlGenerator.getUrlGenerator(req);
 		String action = urlGenerator.generateCustomUrl("asyncRender", app.getName(), page.getName(), config.getName(), null, null);
 		
 		GenericElement elem = new GenericElement();
 		String formName = elem.getTempBuffer().append(config.getNamespace()).append("-form").toString();
 		elem.reset(HtmlConstants.ELEM_FORM);
 		elem.addIdAttribute(HtmlConstants.ATTR_ID, formName);
 		elem.addNameAttribute(HtmlConstants.ATTR_NAME, formName);
 		elem.addAttribute(HtmlConstants.ATTR_METHOD, "GET", false);
 		elem.addAttribute(HtmlConstants.ATTR_ACTION, action, false);
 		elem.write(writer, false);
 		
 		// Write out each attribute
 		if( originalModel != null ) {
 			Iterator<ModelAttribute> it = originalModel.getAttributes().iterator();
 			while( it.hasNext() ) {
 				ModelAttribute attr = it.next();
 				if( attr.isSimple() && !attr.isFlash() ) {
 					String name = attr.getName();
 					Object val = model.get(name);
 					
 					elem.reset(HtmlConstants.ELEM_INPUT);
 					elem.addAttribute(HtmlConstants.ATTR_TYPE, "hidden", false);
 					elem.addIdAttribute(HtmlConstants.ATTR_ID, elem.getTempBuffer().append(formName).append('-').append(name).toString());
 					elem.addNameAttribute(HtmlConstants.ATTR_NAME, name);
 					elem.addAttribute(HtmlConstants.ATTR_VALUE, val != null ? val.toString() : null, true, true);
 					elem.write(writer, true);
 				}
 			}
 		}
 		
 		GenericElement.writeTerminate(writer, HtmlConstants.ELEM_FORM);
 		writer.append('\n');
 	}
 }
