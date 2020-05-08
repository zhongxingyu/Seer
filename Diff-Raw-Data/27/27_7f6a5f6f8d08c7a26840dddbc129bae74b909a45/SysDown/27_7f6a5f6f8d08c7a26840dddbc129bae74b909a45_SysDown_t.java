 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.gui.impl.servlets;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.paxle.gui.ALayoutServlet;
 import org.paxle.gui.IServiceManager;
 
 public class SysDown extends ALayoutServlet {
 	
 	private static final long serialVersionUID = 1L;
 	
 	@Override
 	protected void fillContext(Context context, HttpServletRequest request) {
 		IServiceManager manager = (IServiceManager) context.get(SERVICE_MANAGER);		
 		try {
 			
 			int shutdownDelay = 5;
 			
 			// If restart is true, restart, in any other case simply shut down
			if (request.getParameter("action") != null) {
				String action = request.getParameter("action");
			
				if (action.equalsIgnoreCase("restart")) {
					manager.restartFrameworkDelayed(shutdownDelay);					
				} else if (action.equalsIgnoreCase("shutdown")) {
					manager.shutdownFrameworkDelayed(shutdownDelay);
				}
				context.put("action", action);
			} 
 		} catch( Exception e ) {
 			this.logger.error(e);
 		}
 	}
 	
 	/**
 	 * Choosing the template to use 
 	 */
 	@Override
 	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
 		return this.getTemplate("/resources/templates/SysDown.vm");
 	}
 }
