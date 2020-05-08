 /*
  * Jamm
  * Copyright (C) 2002 Dave Dribin and Keith Garner
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
  */
 
 package jamm.webapp;
 
 import jamm.backend.MailManager;
 
 import java.util.Set;
 import java.util.HashSet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.RedirectingActionForward;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionError;
 
 /**
  * Verifies the username and password passed in via the LoginForm and
  * logs the user in.  If there is an error it'll return to the login
  * page with an error condition.  If "root" tries to log in, it will
  * use the root dn defined in Globals.
  *
  * @see jamm.webapp.LoginForm
  * @see jamm.webapp.Globals
  */
 public class LoginAction extends Action
 {
     /**
      * Performs the action.
      *
      * @see jamm.webapp.LoginForm
      *
      * @param mapping The action mapping with possible destinations.
      * @param actionForm a LoginForm with the login info
      * @param request the http request that caused this action.
      * @param response the http response
      *
      * @return an <code>ActionForward</code>
      *
      * @exception Exception if an error occurs
      */
     public ActionForward execute(ActionMapping mapping,
                                  ActionForm actionForm,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
         throws Exception
     {
         if (isCancelled(request))
         {
             return mapping.findForward("home");
         }
         
         ActionErrors errors = new ActionErrors();
         LoginForm form = (LoginForm) actionForm;
    
         MailManager manager = new MailManager(Globals.getLdapHost(),
                                               Globals.getLdapPort(),
                                               Globals.getLdapSearchBase());
                                               
         boolean isRoot = false;
         String userDn;
         if (form.getUsername().equals(Globals.getRootLogin()))
         {
             userDn = Globals.getRootDn();
             isRoot = true;
         }
         else
         {
             userDn = manager.getDnFromMail(form.getUsername());
         }
         
         if (userDn == null)
         {
             errors.add(ActionErrors.GLOBAL_ERROR,
                        new ActionError("login.error.invalid_login"));
         }
         else
         {
             manager.setBindEntry(userDn, form.getPassword());
             if (!manager.authenticate())
             {
                 errors.add(ActionErrors.GLOBAL_ERROR,
                            new ActionError("login.error.invalid_login"));
             }
         }
 
        if (!errors.empty())
         {
             saveErrors(request, errors);
             // Clear out password
             form.setPassword(null);
             return new ActionForward(mapping.getInput());
         }
 
         Set roles = new HashSet();
         if (isRoot)
         {
             roles.add(User.SITE_ADMIN_ROLE);
             roles.add(User.DOMAIN_ADMIN_ROLE);
         }
         else if (manager.isPostmaster(form.getUsername()))
         {
             roles.add(User.DOMAIN_ADMIN_ROLE);
         }
 
         User user = new User(form.getUsername(), userDn, form.getPassword(),
                              roles);
         HttpSession session = request.getSession();
         session.setAttribute("is_authenticated", "true");
         session.setAttribute("user", user);
         return new RedirectingActionForward(form.getDone());
     }
 }
