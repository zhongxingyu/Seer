 /**
  *  @author dgeorge
  *  
 *  $Id: AdminEditUserRolesAction.java,v 1.6 2006-05-15 15:42:49 georgeda Exp $
  *  
  *  $Log: not supported by cvs2svn $
  *  Revision 1.5  2006/04/17 19:09:40  pandyas
  *  caMod 2.1 OM changes
  *
  *  Revision 1.4  2005/11/16 15:31:16  georgeda
  *  Defect #41. Clean up of email functionality
  *
  *  Revision 1.3  2005/11/07 13:53:18  georgeda
  *  Dynamically update roles
  *
  *  Revision 1.2  2005/11/01 15:54:46  georgeda
  *  Call the manager to save the person
  *
  *  Revision 1.1  2005/10/17 13:28:45  georgeda
  *  Initial revision
  *
  *  Revision 1.8  2005/10/10 14:12:01  georgeda
  *  Changes for comment curation
  *
  *  Revision 1.7  2005/09/16 15:52:56  georgeda
  *  Changes due to manager re-write
  *
  *  
  */
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Person;
 import gov.nih.nci.camod.domain.Role;
 import gov.nih.nci.camod.service.impl.PersonManagerSingleton;
 import gov.nih.nci.camod.service.impl.UserManagerSingleton;
 import gov.nih.nci.camod.webapp.form.RolesAssignmentForm;
 import gov.nih.nci.common.persistence.Search;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 public class AdminEditUserRolesAction extends BaseAction
 {
 
     /**
      * Change the state for the animal model
      */
     public ActionForward execute(ActionMapping inMapping,
                                  ActionForm inForm,
                                  HttpServletRequest inRequest,
                                  HttpServletResponse inResponse) throws Exception
     {
         log.trace("Entering AdminEditUserRolesAction.execute");
 
         // The user didn't press the cancel button
         if (!isCancelled(inRequest))
         {
             try
             {
                 // Get the animal model
                 RolesAssignmentForm theForm = (RolesAssignmentForm) inForm;
 
                 Person thePerson = PersonManagerSingleton.instance().get(theForm.getPersonId());
 
                 // Did the id match?
                 if (thePerson != null)
                 {
                     log.debug("Changing the roles for user: " + thePerson.getDisplayName());

                    Set<Role> theRoles = new HashSet<Role>();

                     if (theForm.isCoordinator() == true)
                     {
                         Role theQBERole = new Role();
                         theQBERole.setName(Constants.Admin.Roles.COORDINATOR);
                         List theQBEList = Search.query(theQBERole);
 
                         if (theQBEList.size() > 0)
                         {
                            theRoles.add((Role) theQBEList.get(0));
                         }
                     }
                     if (theForm.isEditor() == true)
                     {
                         Role theQBERole = new Role();
                         theQBERole.setName(Constants.Admin.Roles.EDITOR);
                         List theQBEList = Search.query(theQBERole);
 
                         if (theQBEList.size() > 0)
                         {
                            theRoles.add((Role)theQBEList.get(0));
                         }
                     }
                     if (theForm.isScreener() == true)
                     {
                         Role theQBERole = new Role();
                         theQBERole.setName(Constants.Admin.Roles.SCREENER);
                         List theQBEList = Search.query(theQBERole);
 
                         if (theQBEList.size() > 0)
                         {
                            theRoles.add((Role)theQBEList.get(0));
                         }
                     }
 
                    thePerson.setRoleCollection(theRoles);
                     PersonManagerSingleton.instance().save(thePerson);
 
                     UserManagerSingleton.instance().updateCurrentUserRoles(inRequest);
                 }
             }
             catch (Exception e)
             {
                 log.error("Caught an setting the user roles: ", e);
 
                 // Encountered an error saving the model.
                 ActionMessages theMsg = new ActionMessages();
                 theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
                 saveErrors(inRequest, theMsg);
             }
         }
 
         log.trace("Exiting AdminEditUserRolesAction.execute");
 
         return inMapping.findForward("next");
     }
 }
