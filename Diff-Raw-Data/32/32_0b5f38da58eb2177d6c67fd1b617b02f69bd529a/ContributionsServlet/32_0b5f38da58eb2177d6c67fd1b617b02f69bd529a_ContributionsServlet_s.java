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
 
 import java.util.Collection;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class ContributionsServlet extends JAMWikiServlet {
 
 	private static final Logger logger = Logger.getLogger(ContributionsServlet.class);
 
 	/**
 	 *
 	 */
 	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
 		ModelAndView next = new ModelAndView("wiki");
 		try {
 			contributions(request, next);
 		} catch (Exception e) {
 			viewError(request, next, e);
 		}
 		loadDefaults(request, next, this.pageInfo);
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void contributions(HttpServletRequest request, ModelAndView next) throws Exception {
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		String userString = request.getParameter("contributor");
 		int num = Environment.getIntValue(Environment.PROP_RECENT_CHANGES_DAYS);
 		if (request.getParameter("num") != null) {
 			// FIXME - verify it's a number
 			num = new Integer(request.getParameter("num")).intValue();
 		}
 		Collection all = null;
 		try {
 			all = WikiBase.getHandler().getUserContributions(virtualWiki, userString, num, true);
 		} catch (Exception e) {
 			logger.error(e);
 			throw e;
 		}
 		next.addObject("contributions", all);
 		next.addObject("num", new Integer(num));
 		next.addObject("contributor", userString);
		this.pageInfo.setPageTitle(new WikiMessage("contributions.title", userString));
 		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_CONTRIBUTIONS);
 		this.pageInfo.setSpecial(true);
 	}
 }
