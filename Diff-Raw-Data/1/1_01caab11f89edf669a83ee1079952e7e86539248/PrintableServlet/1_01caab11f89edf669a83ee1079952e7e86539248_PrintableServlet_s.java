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
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class PrintableServlet extends JAMWikiServlet {
 
 	private static Logger logger = Logger.getLogger(PrintableServlet.class);
 
 	/**
 	 *
 	 */
 	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
 		ModelAndView next = new ModelAndView("printable");
 		try {
 			print(request, next);
 		} catch (Exception e) {
 			next = new ModelAndView("wiki");
 			viewError(request, next, e);
			loadDefaults(request, next, this.pageInfo);
 		}
 		loadDefaults(request, next, this.pageInfo);
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void print(HttpServletRequest request, ModelAndView next) throws Exception {
 		String topic = JAMWikiServlet.getTopicFromRequest(request);
 		if (!StringUtils.hasText(topic)) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		// FIXME - full URLs should be printed, need some sort of switch
 		viewTopic(request, next, topic);
 	}
 }
