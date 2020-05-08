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
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 
 import jamm.backend.MailManager;
 import jamm.backend.MailManagerException;
 import jamm.backend.AliasInfo;
 import jamm.backend.DomainInfo;
 
 /**
  * Calls MailManager to perform the assigned actions on aliases in a
  * domain based on the information in the DomainConfigForm.  Currently
  * only deleting works.  Admin and active changes are just printed
  * out/logged.
  *
  * @see jamm.backend.MailManager
  * @see jamm.webapp.DomainConfigForm
  * 
  * @struts.action scope="request" validate="false"
 *                name="domainAccountAliasForm"
  *                path="/private/domain_alias"
  *                roles="Site Administrator, Domain Administrator"
  */
 public class DomainAliasAction extends JammAction
 {
     /**
      * Performs the action.
      *
      * @param mapping the action mapping of destinations
      * @param actionForm a DomainConfigForm with the required info
      * @param request the http request for this action
      * @param response the http response for this action.
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
         DomainConfigForm form = (DomainConfigForm) actionForm;
         User user = getUser(request);
 
         MailManager manager = getMailManager(user);
         DomainInfo domainInfo = manager.getDomain(form.getDomain());
         boolean userIsSiteAdmin = user.isUserInRole(User.SITE_ADMIN_ROLE);
         
         HashMap aliasInfos = new HashMap();
 
         System.out.println("====================================" +
                            "====================================");
 
         Iterator j;
 
         j = form.getUncheckedActiveItems().iterator();
         modifyActive(manager, aliasInfos, false, j);
 
         j = form.getCheckedActiveItems().iterator();
         modifyActive(manager, aliasInfos, true, j);
 
         if (domainInfo.getCanEditPostmasters() || userIsSiteAdmin)
         {
             j = form.getUncheckedAdminItems().iterator();
             modifyAdministrator(manager, aliasInfos, false, j);
 
             j = form.getCheckedAdminItems().iterator();
             modifyAdministrator(manager, aliasInfos, true, j);
         }
 
         // We'll modify first and then delete.  We probably should not
         // modify anything marked for deletion, to save some cycles,
         // but we'll do that as an improvement later.
         j = aliasInfos.values().iterator();
         while (j.hasNext())
         {
             AliasInfo ai = (AliasInfo) j.next();
             manager.modifyAlias(ai);
         }
 
         String[] deletions = form.getItemsToDelete();
         for (int i = 0; i < deletions.length; i++)
         {
             manager.deleteAlias(deletions[i]);
         }
 
         return findForward(mapping, "domain_admin", request);
     }
 
     /**
      * Gets the alias out of the aliasInfo HashMap.  If the
      * aliasInfo isn't containted in there, it looks it up in mail
      * manager.
      *
      * @param manager The mail manager to use
      * @param aliasInfos the alias info cache
      * @param alias the e-mail address of the alias to get
      * @return an AliasInfo object for alias
      * @exception MailManagerException if an error occurs
      */
     private AliasInfo getAlias(MailManager manager, HashMap aliasInfos,
                                String alias)
         throws MailManagerException
     {
         AliasInfo ai = (AliasInfo) aliasInfos.get(alias);
         if (ai == null)
         {
             ai = manager.getAlias(alias);
             aliasInfos.put(alias, ai);
         }
         return ai;
     }
             
     /**
      * Modifies the active flag on the alias.
      *
      * @param manager The mail manager to use
      * @param aliasInfos the alias info cache
      * @param setTo boolean value to set active to
      * @param i an iterator of AliasInfo objects to change.
      * @exception MailManagerException if an error occurs
      */
     private void modifyActive(MailManager manager, HashMap aliasInfos,
                               boolean setTo, Iterator i)
         throws MailManagerException
     {
         while (i.hasNext())
         {
             String alias = (String) i.next();
             AliasInfo ai = getAlias(manager, aliasInfos, alias);
             ai.setActive(setTo);
         }
     }
 
     /**
      * Modifies the administrator flag on the alias.
      *
      * @param manager the mail manager to use
      * @param aliasInfos the alias info cache
      * @param setTo boolean value to set adminitrator to
      * @param i an iterator of AliasInfo objects to change.
      * @exception MailManagerException if an error occurs
      */
     private void modifyAdministrator(MailManager manager, HashMap aliasInfos,
                                      boolean setTo, Iterator i)
         throws MailManagerException
     {
         while (i.hasNext())
         {
             String alias = (String) i.next();
             AliasInfo ai = getAlias(manager, aliasInfos, alias);
             ai.setAdministrator(setTo);
         }
     }
 }
