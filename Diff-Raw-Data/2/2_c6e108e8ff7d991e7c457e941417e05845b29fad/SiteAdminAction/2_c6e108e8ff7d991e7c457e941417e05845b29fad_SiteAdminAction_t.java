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
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 
 import jamm.backend.MailManager;
 import jamm.backend.DomainInfo;
 
 /**
  * Calls MailManager to loads the information the site_admin page
  * needs and then forwards to the site_admin page.  Currently, the
  * list of domains is loaded and saved in the request attribute
  * <code>domains</code>.  Prepopulates the SiteConfigForm with data.
  *
  * @see jamm.backend.MailManager
  * @see jamm.webapp.SiteConfigForm
  */
 public class SiteAdminAction extends JammAction
 {
     /**
      * Performs the action.
      *
      * @param mapping <code>ActionMapping</code> of possible locations.
      * @param actionForm <code>ActionForm</code>, ignored in this action
      * @param request a <code>HttpServletRequest</code> that caused the action
      * @param response a <code>HttpServletResponse</code>
      *
      * @return an <code>ActionForward</code> value
      *
      * @exception Exception if an error occurs
      */
     public ActionForward execute(ActionMapping mapping,
                                  ActionForm actionForm,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
         throws Exception
     {
         User user = getUser(request);
         MailManager manager = getMailManager(user);
         List domains = manager.getDomains();
 
         SiteConfigForm siteConfigForm = new SiteConfigForm();
 
         List domainNames = new ArrayList();
         Iterator i = domains.iterator();
         while (i.hasNext())
         {
             DomainInfo di = (DomainInfo) i.next();
             domainNames.add(di.getName());
         }
             
         siteConfigForm.setDomains(
            (String []) domainNames.toArray(new String[0]));
 
         siteConfigForm.setOriginalAllowEditAliases(new String[0]);
         siteConfigForm.setOriginalAllowEditAccounts(new String[0]);
         siteConfigForm.setOriginalAllowEditPostmasters(new String[0]);
         siteConfigForm.setOriginalAllowEditCatchalls(new String[0]);
 
         request.setAttribute("siteConfigForm", siteConfigForm);
 
         return (mapping.findForward("site_admin"));
     }
 }
