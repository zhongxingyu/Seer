 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.bug.ws.module;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.ws.Activator;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.MutableModuleProperty;
 import com.buglabs.util.xml.XmlNode;
 
 public class ModuleServlet extends HttpServlet implements ServiceListener {
 
 	private static final long serialVersionUID = -6977609397049223637L;
 
 	private IModuleControl[] modules;
 
	private BundleContext context;

 	public ModuleServlet() {
		context = Activator.getContext();
 		//Initialize the array that tracks modules.
 		this.modules = new IModuleControl[4];
 		for (int i = 0; i < modules.length; ++i)
 			modules[i] = null;
 	}
 
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		String path = req.getPathInfo();
 		// Must have a module in the path.
 		if (path == null) {
 			resp.sendError(665, "Error: invalid module.");
 			return;
 		}
 
 		int index = Integer.parseInt(path.substring(1).trim().toUpperCase());
 		IModuleControl mc = modules[index];
 		// Must be a valid module
 		if (mc == null) {
 			resp.sendError(666, "Error: unknown module " + path);
 			return;
 		}
 
 		// Iterate thru post variables and set them on
 		for (Enumeration names = req.getParameterNames(); names.hasMoreElements();) {
 			String paramName = (String) names.nextElement();
 			String paramValue = req.getParameter(paramName);
 
 			// TODO: Make sure all properties are valid before setting them.
 			if (!containsProperty(mc.getModuleProperties(), paramName)) {
 				resp.sendError(667, "Error: invalid property " + paramName);
 				return;
 			}
 
 			if (!mc.setModuleProperty(new MutableModuleProperty(paramName, paramValue))) {
 				resp.sendError(668, "Error: unable to set property " + paramName);
 			}
 		}
 	}
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		String path = req.getPathInfo();
 		String response = null;
 
 		resp.setContentType("text/xml");
 
 		if (path == null) {
 			response = getModules();
 		} else {
 			try {
 				int index = Integer.parseInt(path.substring(1).trim());
 
 				if (index < 0 || index > 3) {
 					resp.setContentType("text/plain");
 					response = "Error: expected BMI slot index (0 - 3)";
 				}
 
 				IModuleControl mc = modules[index];
 
 				if (mc != null) {
 					response = getModuleXML(mc);
 				} else {
 					resp.setContentType("text/plain");
 					response = "Error: no module.";
 				}
 			} catch (NumberFormatException e) {
 				resp.setContentType("text/plain");
 				response = "Error: expected BMI slot index (0 - 3)";
 			}
 		}
 
 		resp.getWriter().print(response);
 	}
 
 	private String getModuleXML(IModuleControl mc) {
 		XmlNode root = new XmlNode("module");
 		root.setAttribute("name", mc.getModuleName());
 
 		for (Iterator i = mc.getModuleProperties().iterator(); i.hasNext();) {
 			IModuleProperty prop = (IModuleProperty) i.next();
 			XmlNode propNode = new XmlNode("property");
 			propNode.setAttribute("name", prop.getName());
 			if (prop.getValue() != null) {
 				propNode.setAttribute("value", prop.getValue().toString());
 			} else {
 				propNode.setAttribute("value", "null");
 			}
 			propNode.setAttribute("type", prop.getType());
 
 			propNode.setAttribute("mutable", Boolean.toString(prop.isMutable()));
 
 			root.addChild(propNode);
 		}
 
 		return root.toString();
 	}
 
 	/**
 	 * @return
 	 */
 	private String getModules() {
 		XmlNode root = new XmlNode("modules");
 
 		for (int i = 0; i < modules.length; ++i) {
 			if (modules[i] != null) {
 				String moduleName = modules[i].getModuleName();
 				XmlNode mod = new XmlNode("module");
 				mod.setAttribute("name", moduleName);
 				mod.setAttribute("index", "" + modules[i].getSlotId());
 				root.addChild(mod);
 			}
 		}
 
 		return root.toString();
 	}
 
 	// TODO rewrite property storage from List to map to reduce tc of search.
 	private boolean containsProperty(List properties, String name) {
 		for (Iterator i = properties.iterator(); i.hasNext();) {
 			IModuleProperty p = (IModuleProperty) i.next();
 
 			if (p.getName().toUpperCase().equals(name.toUpperCase())) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public void serviceChanged(ServiceEvent event) {
 		//Here we get notified when IModuleControls are added and removed from OSGi service registry.  We want to keep our state up-to-date with the modules array.
		IModuleControl imc = (IModuleControl) context.getService(event.getServiceReference());
 		
 		if (event.getType() == ServiceEvent.REGISTERED) {
 			if (modules[imc.getSlotId()] != null)
 				Activator.getLog().log(LogService.LOG_WARNING, "A new IModuleControl registration is overwriting a pre-existing module.");
 			
 			modules[imc.getSlotId()] = imc;
 		} else if (event.getType() == ServiceEvent.UNREGISTERING) {
 			modules[imc.getSlotId()] = null;
 		}
 	}
 }
