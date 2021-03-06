 /***************************************
  * Copyright (c) 2008
  * Bjoern Wagner
  *
  * Permission is hereby granted, free of charge, to any person obtaining a
  * copy of this software and associated documentation files (the "Software"),
  * to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
 ****************************************/
 
 package org.b3mn.poem.handler;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.b3mn.poem.Identity;
 import org.b3mn.poem.util.HandlerWithoutModelContext;
 
 @HandlerWithoutModelContext(uri="/new")
 public class NewModelHandler extends HandlerBase {
 
 	@Override
     public void doGet(HttpServletRequest request, HttpServletResponse response, Identity subject, Identity object) throws IOException {
 		String stencilset = "/stencilsets/bpmn/bpmn.json";
 		if (request.getParameter("stencilset") != null) {
 			stencilset = request.getParameter("stencilset");
 		}
 
 		String content = "<div id=\"oryx-canvas123\" class=\"-oryx-canvas\">"
 			+ "<span class=\"oryx-mode\">writeable</span>"
 			+ "<span class=\"oryx-mode\">fullscreen</span>"
 			+ "<a href=\"" + "/oryx" + stencilset + "\" rel=\"oryx-stencilset\"></a>\n"
 			+ "</div>\n";
 		response.getWriter().print(this.getOryxModel("New Process Model", content,
 				this.getLanguageCode(request), this.getCountryCode(request)));
 
 		response.setStatus(200);
 		response.setContentType("application/xhtml+xml");
 	}
 	
 	@Override
     public void doPost(HttpServletRequest request, HttpServletResponse response, Identity subject, Identity object) throws IOException {
 		// Check whether the user is public
 		if (subject.getUri().equals(getPublicUser())) {
 			response.getWriter().println("The public user is not allowed to create new models. Please login first.");
 			response.setStatus(403);
 			return;
 		}
 		// Check whether the request contains at least the data and svg parameters
 		if ((request.getParameter("data") != null) && (request.getParameter("svg") != null)) {
			response.setStatus(201);
 			String title = request.getParameter("title");
 			if (title == null) title = "New Process";
 			String type = request.getParameter("type");
 			if (type == null) type = "/stencilsets/bpmn/bpmn.json";
 			String summary = request.getParameter("summary");
 			if (summary == null) summary = "This is a new process.";
 			
 			Identity identity = Identity.newModel(subject, title, type, summary, 
 					request.getParameter("svg"), request.getParameter("data"));
 			response.setHeader("location", this.getServerPath(request) + identity.getUri() + "/self");
 		}
 		else {
 			response.setStatus(400);
 			response.getWriter().println("Data and/or SVG missing");
 		}
 			
 	}
 }
