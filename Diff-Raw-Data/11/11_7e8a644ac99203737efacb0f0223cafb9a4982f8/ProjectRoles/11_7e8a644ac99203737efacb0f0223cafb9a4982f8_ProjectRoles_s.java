 package controllers.admin;
 
 import java.util.List;
 
 import controllers.Lookups;
 import controllers.TMController;
 import controllers.deadbolt.Deadbolt;
 import controllers.deadbolt.Restrict;
 import controllers.deadbolt.Restrictions;
 import controllers.tabularasa.TableController;
 import models.general.UnitRole;
 import models.tm.Project;
 import models.tm.ProjectRole;
 import models.tm.TMUser;
 import play.db.jpa.GenericModel;
 import play.mvc.With;
 import util.Logger;
 
 /**
  * @author Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
 @With(Deadbolt.class)
 public class ProjectRoles extends TMController {
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
    public static void create(ProjectRole role, Long projectId) {
        Project project = Lookups.getProject(projectId);
        if(project == null) {
            Logger.error(Logger.LogType.SECURITY, "Unknown project %s", projectId);
            notFound("Project was not found");
        }
        role.project = project;
        role.account = project.account;
         boolean created = role.create();
         if(!created) {
             Logger.error(Logger.LogType.DB, "Could not create ProjectRole");
             error("Error creating the role, please try again");
         } else {
             ok();
         }
     }
 
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void renameRole(Long projectId, Long roleId, String roleName){
         List<ProjectRole> projectRoles = ProjectRole.find("id!=? and name=? and project.id=?", roleId, roleName, projectId).fetch();
         if(projectRoles.size()>0){
             error("Project role with this name is already defined for this project. Role is not renamed!");
         }
         else{
             ProjectRole projectRole = ProjectRole.find("id=?",roleId).first();
             projectRole.name = roleName;
             projectRole.save();
             ok();
         }
     }
 
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void data(String tableId,
                             Integer iDisplayStart,
                             Integer iDisplayLength,
                             String sColumns,
                             String sEcho,
                             Long projectId) {
 
         if (projectId == null) {
             error();
         } else {
             GenericModel.JPAQuery query;
             query = ProjectRole.find("from ProjectRole r where r.project.id = ?", projectId).from(iDisplayStart == null ? 0 : iDisplayStart);
 
             List<ProjectRole> roles = query.fetch(iDisplayLength == null ? 10 : iDisplayLength);
             long totalRecords = ProjectRole.count();
             TableController.renderJSON(roles, ProjectRole.class, totalRecords, sColumns, sEcho);
             ok();
         }
     }
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void roleDefinition(Long roleId) {
         if (roleId == null) {
             error("No roleId provided");
         } else {
             ProjectRole role = Lookups.getRole(roleId);
             checkInAccount(role);
             List<String> unitRoles = role.unitRoles;
             render(role, unitRoles);
         }
     }
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void edit(Long roleId, String[] unitRoles) {
         if (roleId == null) {
             error("No roleId provided");
         } else {
             ProjectRole role = Lookups.getRole(roleId);
 
             checkInAccount(role);
 
             role.unitRoles.clear();
             for (int i = 0, unitRolesLength = unitRoles.length; i < unitRolesLength; i++) {
                 String unitRole = unitRoles[i];
                 role.unitRoles.add(unitRole);
             }
             role.save();
             ok();
         }
     }
 
     @Restrictions({@Restrict(UnitRole.PROJECTEDIT), @Restrict(UnitRole.PROJECTADMIN)})
     public static void roleDelete(Long roleId) {
         ProjectRole role = Lookups.getRole(roleId);
         if (role != null) {
             List<TMUser> tmUser = TMUser.find("from TMUser tmu where ? in elements(tmu.projectRoles)", role).fetch();
             if (tmUser.size() == 0) {
                 role.delete();
             } else {
                 error("This role is assigned at least to one of users. Role is not deleted!");
                 Logger.error(Logger.LogType.USER, "Attempt to delete a role id %s which is assigned to at least one user", roleId);
             }
         } else {
             error("Not existing role!");
             Logger.error(Logger.LogType.SECURITY, "Role with id %s doesn't exist", roleId);
         }
     }
 }
