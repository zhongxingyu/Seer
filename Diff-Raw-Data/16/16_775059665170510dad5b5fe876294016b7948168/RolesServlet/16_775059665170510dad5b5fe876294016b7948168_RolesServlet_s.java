 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.servlets;
 
import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class RolesServlet extends JAMWikiServlet {
 
 	private static WikiLogger logger = WikiLogger.getLogger(RolesServlet.class.getName());
 	protected static final String JSP_ADMIN_ROLES = "admin-roles.jsp";
 
 	/**
 	 * This method handles the request after its parent class receives control.
 	 *
 	 * @param request - Standard HttpServletRequest object.
 	 * @param response - Standard HttpServletResponse object.
 	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String function = request.getParameter("function");
 		if (!StringUtils.hasText(function)) {
 			view(request, next, pageInfo);
 		} else if (function.equals("createRole")) {
 			// for now view is the only action allowed...
 			view(request, next, pageInfo);
 		} else if (function.equals("assignRole")) {
 			// for now view is the only action allowed...
 			view(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		// FIXME - dummy data
		Vector roles = new Vector();
		roles.add("ROLE_VIEW");
		roles.add("ROLE_EDIT");
		roles.add("ROLE_MOVE");
		roles.add("ROLE_TRANSLATE");
		roles.add("ROLE_ADMIN");
		roles.add("ROLE_SYSTEM_ADMIN");
 		next.addObject("roles", roles);
 		pageInfo.setAdmin(true);
 		pageInfo.setContentJsp(JSP_ADMIN_ROLES);
 		pageInfo.setPageTitle(new WikiMessage("roles.title"));
 	}
 }
