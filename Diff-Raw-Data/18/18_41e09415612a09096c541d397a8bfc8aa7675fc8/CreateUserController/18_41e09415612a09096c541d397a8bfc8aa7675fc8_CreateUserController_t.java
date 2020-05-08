 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import static java.lang.String.valueOf;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.User;
 import edu.northwestern.bioinformatics.studycalendar.service.UserService;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
 import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
 import edu.northwestern.bioinformatics.studycalendar.utils.editors.RoleEditor;
 import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
 import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @AccessControl(roles = Role.STUDY_ADMIN)
 public class CreateUserController extends PscCancellableFormController {
     private UserService userService;
     private SiteDao siteDao;
     private UserDao userDao;
 
     public CreateUserController() {
         setCommandClass(CreateUserCommand.class);
         setFormView("createUser");
         setValidator(new ValidatableValidator());
         setSuccessView("listUsers");
         setCancelView("listUsers");
         setCrumb(new Crumb());
     }
 
    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        CreateUserCommand command = (CreateUserCommand) o;
         Map<String, Object> refdata = new HashMap<String, Object>();
         List<Role> roles = Arrays.asList(Role.values());
         refdata.put("roles", roles);
 
        String actionText = ServletRequestUtils.getIntParameter(request, "id") == null ? "Create" : "Edit";
         refdata.put("actionText", actionText);
 
        refdata.put("user", command.getUser());

         return refdata;
     }
 
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
 
         binder.registerCustomEditor(Site.class, new DaoBasedEditor(siteDao));
         binder.registerCustomEditor(Role.class, new RoleEditor());
     }
 
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
         CreateUserCommand command = (CreateUserCommand) oCommand;
         
         command.apply();
 
         return new ModelAndView(new RedirectView(getSuccessView()));
     }
 
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
         Integer editId = ServletRequestUtils.getIntParameter(request, "id");
         User user = (editId != null) ? user = userService.getUserById(editId) : new User();
 
         CreateUserCommand command = new CreateUserCommand(user, siteDao, userService);
 
         if (command.getUser().getPassword() != null) {
             command.setRePassword(command.getUser().getPlainTextPassword());
         }
 
         return command;
     }
 
     protected ModelAndView onCancel(Object command) throws Exception {
 		return new ModelAndView(new RedirectView(getCancelView()));
 	}
 
 
 
     @Required
     public void setUserService(UserService userService) {
         this.userService = userService;
     }
 
     @Required
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     @Required
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     private static class Crumb extends DefaultCrumb {
         public String getName(BreadcrumbContext context) {
             StringBuilder sb = new StringBuilder();
            if (context.getUser() == null || context.getUser().getId() == null) {
                 sb.append( "Create User");
             } else {
                 sb.append(" Edit User ").append(context.getUser().getName());
             }
             return sb.toString();
         }
 
         public Map<String, String> getParameters(BreadcrumbContext context) {
             Map<String, String> params = new HashMap<String, String>();
            if (context.getUser() != null && context.getUser().getId() != null) {
                 params.put("id", context.getUser().getId().toString());
             }
             return params;
         }
     }
 }
