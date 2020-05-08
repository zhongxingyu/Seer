 package controllers.admin;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import controllers.Lookups;
 import controllers.TMController;
 import controllers.deadbolt.Deadbolt;
 import controllers.deadbolt.Restrict;
 import controllers.tabularasa.TableController;
 import models.account.User;
 import models.general.UnitRole;
 import models.tm.AccountRole;
 import models.tm.Project;
 import models.tm.ProjectCategory;
 import models.tm.Role;
 import models.tm.TMUser;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 import play.db.jpa.GenericModel;
 import play.mvc.Router;
 import play.mvc.With;
 import play.templates.JavaExtensions;
 import util.Logger;
 
 /**
  * TODO security checks
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @With(Deadbolt.class)
 public class Users extends TMController {
 
     @Restrict(UnitRole.USEREDIT)
     public static void index() {
         List<User> users = User.findAll();
         render(users);
     }
 
     @Restrict(UnitRole.USEREDIT)
     public static void userDetails(Long userId) {
         Router.ActionDefinition action = Router.reverse("admin.Users.edit");
         TMUser user = Lookups.getUser(userId);
        if (user == null) {
            notFound();
        }
         render("/general/userProfile.html", action, user);
     }
 
     @Restrict(UnitRole.USEREDIT)
     public static void projects(Long userId) {
         TMUser user = null;
         if (userId != null) {
             user = TMUser.findById(userId);
             if (user == null) {
                 notFound();
             } else {
                 checkInAccount(user);
                 List<ProjectCategory> projectCategories = ProjectCategory.findByAccount(user.user.account.getId());
                 render("/admin/Users/projects.html", user, projectCategories);
             }
         } else {
             render("/admin/Users/projects.html");
         }
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void account(Long userId) {
         TMUser user = null;
         if (userId != null) {
             user = TMUser.findById(userId);
         }
         boolean userAdmin = false, projectAdmin = false, accountAdmin = false;
         if (user != null) {
             List<AccountRole> accountRoles = AccountRole.getAccountRoles(user.accountRoles);
             userAdmin = accountRoles.contains(AccountRole.USER_ADMIN);
             projectAdmin = accountRoles.contains(AccountRole.PROJECT_ADMIN);
             accountAdmin = accountRoles.contains(AccountRole.ACCOUNT_ADMIN);
         }
         render("/admin/Users/account.html", user, userAdmin, projectAdmin, accountAdmin);
     }
 
 
     @Restrict(UnitRole.USERCREATE)
     public static void create(TMUser user) {
         if (Validation.hasErrors()) {
             // TODO add some flash message
             render("@index", user);
         }
 
         user.user.account = getConnectedUser().user.account;
         user.account = getConnectedUser().user.account;
         user.create();
         index();
     }
 
     @Restrict(UnitRole.USEREDIT)
     public static void edit(@Valid TMUser user) {
         if (Validation.hasErrors()) {
             // TODO test if this works
             // display a message at least with the validation errors?
             Long selectedUser = user.getId();
             render("@index", user, selectedUser);
         }
         user.save();
         ok();
     }
 
     @Restrict(UnitRole.ACCOUNTADMIN)
     public static void updateAccountRoles(Long userId, boolean userAdmin, boolean projectAdmin, boolean accountAdmin) {
         List<String> accountRoles = new ArrayList<String>();
         if (userAdmin) {
             accountRoles.addAll(AccountRole.USER_ADMIN.getUnitRoles());
         }
         if (projectAdmin) {
             accountRoles.addAll(AccountRole.PROJECT_ADMIN.getUnitRoles());
         }
         if (accountAdmin) {
             accountRoles.addAll(AccountRole.ACCOUNT_ADMIN.getUnitRoles());
         }
         TMUser user = Lookups.getUser(userId);
         if (user != null) {
             Logger.info(Logger.LogType.ADMIN, "Updating roles of user '%s' to contain '%s'", user.user.getDebugString(), JavaExtensions.join(accountRoles, ", "));
             user.accountRoles.clear();
             user.accountRoles.addAll(accountRoles);
             user.save();
         } else {
             Logger.fatal(Logger.LogType.SECURITY, "Attempting to update account roles of unknown user '%s'", userId);
             notFound();
         }
         ok();
     }
 
     @Restrict(UnitRole.USERDELETE)
     public static void removeUser(Long userId) {
         User u = User.findById(userId);
         if (u != null) {
             Logger.info(Logger.LogType.ADMIN, "Deactivating user '%s'", u.getDebugString());
 
             TMUser tmUser = TMUser.find("from TMUser tmu where tmu.user = ?", u).first();
 
             u.active = false;
             u.save();
 
             tmUser.accountRoles.clear();
             tmUser.projectRoles.clear();
             tmUser.save();
         } else {
             Logger.fatal(Logger.LogType.SECURITY, "Attempting to deactivate unknown user '%s'", userId);
             notFound();
         }
         ok();
     }
 
     @Restrict(UnitRole.USEREDIT)
     public static void data(String tableId,
                             Integer iDisplayStart,
                             Integer iDisplayLength,
                             String sColumns,
                             String sEcho,
                             String sSearch) {
         GenericModel.JPAQuery query = null;
         if (sSearch != null && sSearch.length() > 0) {
             String sLike = "%" + sSearch + "%";
             query = TMUser.find("from TMUser u where u.user.firstName like ? or u.user.lastName like ?", sLike, sLike);
         } else {
             query = TMUser.find("from TMUser u").from(iDisplayStart == null ? 0 : iDisplayStart);
         }
         List<TMUser> people = query.fetch(iDisplayLength == null ? 10 : iDisplayLength);
         long totalRecords = TMUser.count();
         TableController.renderJSON(people, TMUser.class, totalRecords, sColumns, sEcho);
     }
 
     @Restrict(UnitRole.USEREDIT)
     public static void projectOptions(Long categoryId, Long accountId) {
         if (categoryId == null || accountId == null) {
             error();
         } else {
             if (!accountId.equals(getUserAccount().getId())) {
                 unauthorized();
             }
             List<Project> projects;
             if (categoryId == -1l) {
                 // all non-assigned projects
                 projects = Project.find("from Project p where p.projectCategory is null and p.account.id = ?", accountId).<Project>fetch();
             } else {
                 ProjectCategory pc = ProjectCategory.findById(categoryId);
                 checkInAccount(pc);
                 projects = pc.getProjects();
             }
             Map<Long, String> m = new HashMap<Long, String>();
             for (Project p : projects) {
                 m.put(p.getId(), p.name);
             }
             renderJSON(m);
         }
     }
 
     @Restrict(UnitRole.USEREDIT)
     public static void rolesOptions(Long projectId) {
         if (projectId == null) {
             error();
         } else {
             Project p = Project.findById(projectId);
             if (p == null) {
                 notFound();
             }
             checkInAccount(p);
             List<Role> roles = Role.find("from Role r where r.project.id = ?", projectId).<Role>fetch();
             Map<Long, String> m = new HashMap<Long, String>();
             for (Role r : roles) {
                 m.put(r.getId(), r.name);
             }
             renderJSON(m);
         }
     }
 
 }
