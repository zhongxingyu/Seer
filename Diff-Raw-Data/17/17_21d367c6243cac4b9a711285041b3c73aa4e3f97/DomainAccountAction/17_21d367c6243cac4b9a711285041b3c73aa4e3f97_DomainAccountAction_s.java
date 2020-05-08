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
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 
 import jamm.backend.MailManager;
 import jamm.backend.MailManagerException;
 import jamm.backend.AccountInfo;
 import jamm.backend.DomainInfo;
 
 /**
  * It should call MailManager to perform the assigned actions on
  * accounts from a domain.  Currently, it changes actives, prints
  * out/logs the rest of the changes to the checkboxes and then
  * forwards to user_home.
  *
  * @see jamm.webapp.DomainConfigForm
  * @struts.action scope="request" validate="false"
 *                name="domainAccountAliasForm"
  *                path="/private/domain_account"
  *                roles="Site Administrator, Domain Administrator"
  */
 public class DomainAccountAction extends JammAction
 {
     /**
      * Performs the action.
      *
      * @see jamm.webapp.DomainConfigForm
      *
      * @param mapping The action mapping of possible destinations
      * @param actionForm a DomainConfigForm with the required info
      * @param request the http request
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
         // int modifiedItems = 0;
         DomainConfigForm form = (DomainConfigForm) actionForm;
         User user = getUser(request);
 
         MailManager manager = getMailManager(user);
         DomainInfo domainInfo = manager.getDomain(form.getDomain());
         boolean userIsSiteAdmin = user.isUserInRole(User.SITE_ADMIN_ROLE);
         
         HashMap accountInfos = new HashMap();
         
         System.out.println("====================================" +
                            "====================================");
         
         System.out.println("Delete: " +
                            Arrays.asList(form.getItemsToDelete()));
 
         Iterator i;
         Set s;
 
         if (domainInfo.getCanEditAccounts() || userIsSiteAdmin)
         {
             s = form.getUncheckedActiveItems();
             // modifiedItems += s.size();
             i = s.iterator();
             modifyActive(manager, accountInfos, false, i);
 
             s = form.getCheckedActiveItems();
             // modifiedItems += s.size();
             i = s.iterator();
             modifyActive(manager, accountInfos, true, i);
 
             s = form.getUncheckedDeleteItems();
             i = s.iterator();
             modifyDelete(manager, accountInfos, false, i);
 
             s = form.getCheckedDeleteItems();
             i = s.iterator();
             modifyDelete(manager, accountInfos, true, i);
         }
 
         if (domainInfo.getCanEditPostmasters() || userIsSiteAdmin)
         {
             s = form.getUncheckedAdminItems();
             // modifiedItems += s.size();
             i = s.iterator();
             modifyAdministrator(manager, accountInfos, false, i);
 
             s = form.getCheckedAdminItems();
             // modifiedItems += s.size();
             i = s.iterator();
             modifyAdministrator(manager, accountInfos, true, i);
         }
 
         i = accountInfos.values().iterator();
         while (i.hasNext())
         {
             AccountInfo ai = (AccountInfo) i.next();
             manager.modifyAccount(ai);
         }
 
         /*  Am starting to put in user feedback.
         if (modifiedItems > 0)
         {
             ActionMessages am = new ActionMessage();
             am.
         */  
 
         return findForward(mapping, "domain_admin", request);
     }
 
     /**
      * Gets the account out of the accountInfo HashMap.  If the
      * accountInfo isn't containted in there, it looks it up in mail
      * manager.
      *
      * @param manager The mail manager to use
      * @param accountInfos the account info cache
      * @param account The mail address of the account
      * @return an AccountInfo object
      * @exception MailManagerException if an error occurs
      */
     private AccountInfo getAccount(MailManager manager, HashMap accountInfos,
                                    String account)
         throws MailManagerException
     {
         AccountInfo ai = (AccountInfo) accountInfos.get(account);
         if (ai == null)
         {
             ai = manager.getAccount(account);
             accountInfos.put(account, ai);
         }
         return ai;
     }
             
     /**
      * Modifies the active flag on the account.
      *
      * @param manager The mail manager to use
      * @param accountInfos the account info cache
      * @param setTo boolean value to set to
      * @param i an iterator filled with AccountInfo
      * @exception MailManagerException if an error occurs
      */
     private void modifyActive(MailManager manager, HashMap accountInfos,
                               boolean setTo, Iterator i)
         throws MailManagerException
     {
         while (i.hasNext())
         {
             String account = (String) i.next();
             AccountInfo ai = getAccount(manager, accountInfos, account);
             ai.setActive(setTo);
         }
     }
 
     /**
      * This modifies the delete flag for the accounts in the iterator.
      *
      * @param manager the mail manger
      * @param accountInfos account info cache
      * @param setTo boolean value to change to
      * @param i an iterator filled with AccountInfo
      * @exception MailManagerException if an error occurs
      */
     private void modifyDelete(MailManager manager, HashMap accountInfos,
                               boolean setTo, Iterator i)
         throws MailManagerException
     {
         while (i.hasNext())
         {
             String account = (String) i.next();
             AccountInfo ai = getAccount(manager, accountInfos, account);
             ai.setDelete(setTo);
         }
     }
 
     /**
      * Modifies the administrator flag on the account.
      *
      * @param manager The mail manager to use
      * @param accountInfos the account info cache
      * @param setTo boolean value to set to
      * @param i an iterator filled with AccountInfo
      * @exception MailManagerException if an error occurs
      */
     private void modifyAdministrator(MailManager manager, HashMap accountInfos,
                                      boolean setTo, Iterator i)
         throws MailManagerException
     {
         while (i.hasNext())
         {
             String account = (String) i.next();
             AccountInfo ai = getAccount(manager, accountInfos, account);
             ai.setAdministrator(setTo);
         }
     }
 }
