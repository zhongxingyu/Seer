 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.icon.impl;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="org.paxle.servlet.path" value="/favicon"
 */
 public class IconServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private Log logger = LogFactory.getLog(this.getClass());
 
 	@Override
 	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {		
 		String url = null;
 		try {
 			url = req.getParameter("url");
 			if (url == null || url.length() == 0) {
 				rsp.setStatus(404);
 				return;
 			}
 
 			IconData icon = IconTool.getIcon(new URL(url));
 			if (icon != null) {
 				rsp.setContentType(icon.mimeType);
 
 				OutputStream out = rsp.getOutputStream();
 				out.write(icon.data);
 				out.close();
 				return;
 			} else {
 				rsp.setStatus(404);
 				return;
 			}	
 		} catch (Throwable e) {
 			this.logger.error(String.format(
 					"Unexpected '%s' while loading icon for '%s': %s",
 					e.getClass().getName(),
 					url,
 					e.getMessage()
 			));
 			rsp.setStatus(400);
 			return;
 		}
 	}
 }
