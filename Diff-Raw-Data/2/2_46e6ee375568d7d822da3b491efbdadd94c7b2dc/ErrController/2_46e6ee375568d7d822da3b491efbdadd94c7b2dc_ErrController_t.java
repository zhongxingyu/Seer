 /*
  * ELW : e-learning workspace
  * Copyright (C) 2010  Anton Kraievoy
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package elw.web;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Enumeration;
 
 @Controller
 @RequestMapping("/err/**")
 public class ErrController {
 	private static final Logger log = LoggerFactory.getLogger(ErrController.class);
 
 	@RequestMapping(value = "*")
 	public ModelAndView do_handle(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		try {
 			final Integer statusCode = (Integer) req.getAttribute("javax.servlet.error.status_code");
 			final String message = (String) req.getAttribute("javax.servlet.error.message");
 			final Throwable throwable = (Throwable) req.getAttribute("javax.servlet.error.exception");
 			final String eventId = Long.toString(System.currentTimeMillis(), 36);
 
 			final StringWriter logDest = new StringWriter();
 			final PrintWriter logOut = new PrintWriter(logDest);
 			final HttpSession session = req.getSession(false);
 
 			logOut.println("web error: eventId="+eventId+" status=" + statusCode + " message='" + message + "'");
			logOut.println("url: " + req.getAttribute("javax.servlet.error.request_uri"));
 			logOut.print("attributes: ");
 			final Enumeration reqAttrNames = req.getAttributeNames();
 			while (reqAttrNames.hasMoreElements()) {
 				final String aName = (String) reqAttrNames.nextElement();
 				if (!aName.startsWith("javax.servlet.error")) {
 					logOut.print(aName + "="+String.valueOf(req.getAttribute(aName)) + " ");
 				}
 			}
 			logOut.println();
 
 			if (session != null) {
 				logOut.println("session id: " + session.getId());
 				logOut.print("session: ");
 				final Enumeration sessAttrNames = session.getAttributeNames();
 				while (sessAttrNames.hasMoreElements()) {
 					final String aName = (String) sessAttrNames.nextElement();
 					if (!aName.startsWith("javax.servlet.error")) {
 						logOut.print(aName + "="+String.valueOf(session.getAttribute(aName)) + " ");
 					}
 				}
 				logOut.println();
 			}
 
 			log.error(
 					logDest.toString(), throwable
 			);
 
 			final PrintWriter out = resp.getWriter();
 
 			out.print(
 					"<html><title>HTTP status " + statusCode + " : " + message + "</title>" +
 					"<body><h3>HTTP status " + statusCode + " : " + message + "</h3>" +
 					"Sorry for inconvenience and thanks for finding just another bug out there.<br/>" +
 					"For the time being, you may log out, log in and then try the operation once again.<br/><br/>" +
 					"Event reference id: <b>"+eventId+"</b>." +
 					"</body></html>"
 			);
 		} catch (Throwable t) {
 			log.error("failed on reporting error", t);
 		}
 
 		return null;
 	}
 }
