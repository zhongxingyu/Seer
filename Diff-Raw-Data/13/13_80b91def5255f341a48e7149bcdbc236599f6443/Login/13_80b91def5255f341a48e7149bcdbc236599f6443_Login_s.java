 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense.  When WebMacro's "Simple Public License" is
  * finalised, we'll offer it as an alternative license for Melati.
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati.login;
 
 import java.util.*;
 import java.io.*;
 import org.melati.util.*;
 import org.melati.*;
 import org.melati.poem.*;
 import org.webmacro.*;
 import org.webmacro.util.*;
 import org.webmacro.servlet.*;
 import org.webmacro.engine.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class Login extends MelatiServlet {
   static final String
       TRIGGERING_REQUEST_PARAMETERS =
           "org.melati.Login.triggeringRequestParameters",
       TRIGGERING_EXCEPTION =
           "org.melati.Login.triggeringException";
 
 
   protected Template loginTemplate(String name) throws WebMacroException {
         return getTemplate("login/" + name);
   }
 
   protected Template loginPageTemplate() throws WebMacroException {
     return loginTemplate("Login.wm");
   }
 
   protected Template usernameUnknownTemplate() throws WebMacroException {
     return loginTemplate("LoginFailure.wm");
   }
 
   protected Template passwordIncorrectTemplate() throws WebMacroException {
     return loginTemplate("LoginFailure.wm");
   }
 
   protected Template loginSuccessTemplate() throws WebMacroException {
     return loginTemplate("LoginSuccess.wm");
   }
 
   protected Template handle(WebContext context)
       throws PoemException, WebMacroException {

     HttpSession session = context.getSession();
 
     AccessPoemException triggeringException =
         (AccessPoemException)session.getValue(TRIGGERING_EXCEPTION);
 
     if (triggeringException != null)
       context.put("triggeringException", triggeringException);
 
    String username = context.getForm("field-login");
    String password = context.getForm("field-password");

     UserTable users = PoemThread.database().getUserTable();
     context.put("login", new Field(username, users.getLoginColumn()));
     context.put("password",
                 new Field(password, users.getPasswordColumn()));
 
     if (username == null)
       return loginPageTemplate();
 
     User user = (User)PoemThread.database().getUserTable().getLoginColumn().
                     firstWhereEq(username);
     if (user == null)
       return usernameUnknownTemplate();
 
    if (!user.getPassword_unsafe().equals(context.getForm("field-password")))
       return passwordIncorrectTemplate();
 
     // Authenticated successfully.
 
     // Arrange for the original parameters from the request that triggered the
     // login to be overlaid on the next request that comes in if it's a match
     // (this allows POSTed fields to be recovered without converting the
     // request into a GET that the browser will repeat on reload with giving
     // any warning).
 
     HttpServletRequestParameters triggeringParams =
         (HttpServletRequestParameters)session.getValue(
             TRIGGERING_REQUEST_PARAMETERS);
 
     if (triggeringParams != null) {
       session.putValue(HttpSessionAccessHandler.OVERLAY_PARAMETERS,
 		       triggeringParams);
       session.removeValue(TRIGGERING_REQUEST_PARAMETERS);
       session.removeValue(TRIGGERING_EXCEPTION);
       context.put("continuationURL", triggeringParams.continuationURL());
     } else {
       if (context.getForm("continuationURL") != null) {
 		  context.put("continuationURL",context.getForm("continuationURL"));
 	  }
 	}
 
     session.putValue(HttpSessionAccessHandler.USER, user);
 
     return loginSuccessTemplate();
   }
 }
