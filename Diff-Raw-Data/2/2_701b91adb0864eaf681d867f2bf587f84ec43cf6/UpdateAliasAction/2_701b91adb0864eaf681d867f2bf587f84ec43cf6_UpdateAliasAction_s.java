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
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Arrays;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionError;
 
 import jamm.backend.MailManager;
 import jamm.backend.AliasInfo;
 
 /**
  * This action updates an alias entry with the changes as specified by
  * the information in the UpdateAliasForm.  After processing the form
  * data to see what needs to be removed and what needs to be added, it
  * calls the MailManager to then modify the alias.
  *
  * @see jamm.webapp.UpdateAliasForm
  * @see jamm.backend.MailManager
  */
 public class UpdateAliasAction extends JammAction
 {
     /**
      * Performs the action.
      *
      * @param mapping an <code>ActionMapping</code> of possible destinations
      * @param actionForm an <code>UpdateAliasForm</code> with our
      *                   required information.
      * @param request a <code>HttpServletRequest</code>
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
         UpdateAliasForm form = (UpdateAliasForm) actionForm;
         User user = getUser(request);
         MailManager manager =
             new MailManager(Globals.getLdapHost(),
                             Globals.getLdapPort(),
                             Globals.getLdapSearchBase(),
                             user.getDn(),
                             user.getPassword());
 
         String mail = form.getMail();
         AliasInfo alias = manager.getAlias(mail);
         
         Set newDestinations = new HashSet(alias.getMailDestinations());
         newDestinations.addAll(form.getAddedAddresses());
         newDestinations.removeAll(Arrays.asList(form.getDeleted()));
 
         ActionErrors errors = new ActionErrors();
 
         if (newDestinations.size() == 0)
         {
             errors.add(ActionErrors.GLOBAL_ERROR,
                        new ActionError("alias.error.non_zero_aliases"));
         }
 
        if (!errors.empty())
         {
             saveErrors(request, errors);
             return new ActionForward(mapping.getInput());
         }
 
         alias.setMailDestinations(newDestinations);
         manager.modifyAlias(alias);
 
         return findForward(mapping, "account_admin", request);
     }
 }
