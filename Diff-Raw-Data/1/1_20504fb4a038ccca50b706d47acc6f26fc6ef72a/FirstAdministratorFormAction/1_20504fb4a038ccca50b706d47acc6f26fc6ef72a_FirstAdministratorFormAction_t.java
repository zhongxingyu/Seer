 package edu.northwestern.bioinformatics.studycalendar.web.setup;
 
 import org.springframework.webflow.action.FormAction;
 import org.springframework.webflow.execution.RequestContext;
 import org.springframework.webflow.execution.ScopeType;
 import org.springframework.beans.factory.annotation.Required;
 import edu.northwestern.bioinformatics.studycalendar.web.CreateUserCommand;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.service.UserService;
 import edu.northwestern.bioinformatics.studycalendar.service.UserRoleService;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
 
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 public class FirstAdministratorFormAction extends FormAction {
     private SiteDao siteDao;
     private UserDao userDao;
     private UserService userService;
     private UserRoleService userRoleService;
 
     public FirstAdministratorFormAction() {
         super(CreateUserCommand.class);
         setFormObjectName("adminCommand");
         setValidator(new ValidatableValidator());
         setFormObjectScope(ScopeType.REQUEST);
     }
 
     protected Object createFormObject(RequestContext context) throws Exception {
         CreateUserCommand command = new CreateUserCommand(null, siteDao, userService, userDao, userRoleService);
         command.setUserActiveFlag(true);
        command.setPasswordModified(true);
         // set sys admin role for all sites, just to be safe (there should only be one site at this point)
         for (Map<Role, CreateUserCommand.RoleCell> map : command.getRolesGrid().values()) {
             map.get(Role.SYSTEM_ADMINISTRATOR).setSelected(true);
         }
         return command;
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     @Required
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     @Required
     public void setUserService(UserService userService) {
         this.userService = userService;
     }
 
     @Required
     public void setUserRoleService(UserRoleService userRoleService) {
         this.userRoleService = userRoleService;
     }
 }
