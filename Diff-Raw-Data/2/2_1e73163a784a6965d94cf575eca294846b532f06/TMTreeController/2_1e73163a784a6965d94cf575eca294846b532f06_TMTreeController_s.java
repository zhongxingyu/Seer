 package controllers;
 
 import java.util.Map;
 
 import controllers.tree.TreeController;
 import models.deadbolt.RoleHolder;
 import models.general.TreeRoleHolder;
 import models.general.UnitRole;
 import models.tm.Project;
 import org.apache.commons.collections.CollectionUtils;
 import org.hibernate.Session;
 import play.db.jpa.JPA;
 import play.mvc.After;
 import play.mvc.Before;
 import tree.TreeDataHandler;
 import tree.TreePlugin;
 import util.Logger;
 
 /**
  * Wrapper of the TreeController for TM (extending TMController means we inherit the routes and @Before methods).
  * Handles authorization and security.
  * <p/>
  * TODO optimization - caching for canXXX() methods
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class TMTreeController extends TMController {
 
     public static ThreadLocal<Project> projectThreadLocal = new ThreadLocal<Project>();
 
     /**
      * Set a Hibernate filter so that all queries get a project id appended.
      * We also set a threadLocal so that the {@link TMTree} knows how to create new {@link models.tm.ProjectTreeNode} instances.
      */
     @Before
     public static void setProjectFilter() {
         if (Security.isConnected() && controllerHasActiveProject()) {
             @SuppressWarnings("unchecked")
             Long projectId = params.get("args[projectId]", Long.class);
             if (projectId != null) {
                 Project project = Lookups.getProject(projectId);
                 projectThreadLocal.set(project);
            } else {
                 projectThreadLocal.set(getActiveProject());
             }
             if(TMController.controllerHasActiveProject()) {
                 ((Session) JPA.em().getDelegate()).enableFilter("project").setParameter("project_id", projectThreadLocal.get().getId());
             }
         }
 
     }
 
     @After
     public static void cleanup() {
         projectThreadLocal.set(null);
     }
 
     public static void create(String treeId, Long parentId, String parentType, Long position, String name, String type, Map<String, String> args) {
         if (canCreate(treeId)) {
             TreeController.createDirect(treeId, parentId, parentType, position, name, type, args);
         } else {
             Logger.error(Logger.LogType.SECURITY, "Illegal attempt to create a node for tree %s, parentId %s, parentType %s, name %s, type %s", treeId, parentId, parentType, name, type);
             forbidden();
         }
     }
 
     public static void remove(String treeId, Long id, Long parentId, String type, Map<String, String> args) {
         if (canDelete(treeId)) {
             TreeController.removeDirect(treeId, id, parentId, type, args);
         } else {
             Logger.error(Logger.LogType.SECURITY, "Illegal attempt to delete a node for tree %s, id %s, parentId %s, type %s", treeId, id, parentId, type);
             forbidden();
         }
     }
 
     public static void rename(String treeId, Long id, String name, String type) {
         if (canUpdate(treeId)) {
             TreeController.renameDirect(treeId, id, name, type);
         } else {
             Logger.error(Logger.LogType.SECURITY, "Illegal attempt to rename a node for tree %s, id %s, new name %s, type %s", treeId, id, name, type);
             forbidden();
         }
     }
 
     public static void move(String treeId, Long id, String type, Long target, String targetType, Long position, String name, boolean copy) {
         if (!copy && canUpdate(treeId)) {
             TreeController.moveDirect(treeId, id, type, target, targetType, position, name, copy);
         } else if (!copy && !canUpdate(treeId)) {
             Logger.error(Logger.LogType.SECURITY, "Illegal attempt to move a node for tree %s, id %s, type %s, target %s", treeId, id, type, target);
             forbidden();
         } else if (copy && canCreate(treeId)) {
             TreeController.moveDirect(treeId, id, type, target, targetType, position, name, copy);
         } else if (copy && !canCreate(treeId)) {
             Logger.error(Logger.LogType.SECURITY, "Illegal attempt to copy a node for tree %s, id %s, type %s, target %s", treeId, id, type, target);
             forbidden();
         }
     }
 
     public static void getChildren(String treeId, Long id, String type, Map<String, String> args) {
         if (canView(treeId)) {
             TreeController.getChildrenDirect(treeId, id, type, args);
         } else {
             Logger.error(Logger.LogType.SECURITY, "Illegal attempt to view a node for tree %s, id %s, type %s", treeId, id, type);
             forbidden();
         }
     }
 
     private static boolean canView(String treeId) {
         RoleHolder user = TMDeadboltHandler.getUserRoles(getActiveProject());
         TreeRoleHolder treeRoleHolder = getTreeRoleHolder(treeId);
         return CollectionUtils.containsAny(user.getRoles(), treeRoleHolder.getViewRoles()) || canDoWhateverTheyWant();
     }
 
     private static boolean canCreate(String treeId) {
         RoleHolder user = TMDeadboltHandler.getUserRoles(getActiveProject());
         TreeRoleHolder treeRoleHolder = getTreeRoleHolder(treeId);
         return CollectionUtils.containsAny(user.getRoles(), treeRoleHolder.getCreateRoles()) || canDoWhateverTheyWant();
     }
 
     private static boolean canUpdate(String treeId) {
         RoleHolder user = TMDeadboltHandler.getUserRoles(getActiveProject());
         TreeRoleHolder treeRoleHolder = getTreeRoleHolder(treeId);
         return CollectionUtils.containsAny(user.getRoles(), treeRoleHolder.getUpdateRoles()) || canDoWhateverTheyWant();
     }
 
     private static boolean canDelete(String treeId) {
         RoleHolder user = TMDeadboltHandler.getUserRoles(getActiveProject());
         TreeRoleHolder treeRoleHolder = getTreeRoleHolder(treeId);
         return CollectionUtils.containsAny(user.getRoles(), treeRoleHolder.getDeleteRoles()) || canDoWhateverTheyWant();
     }
 
     private static boolean canDoWhateverTheyWant() {
         return TMDeadboltHandler.getUserRoles(getActiveProject()).getRoles().contains(UnitRole.role(UnitRole.ACCOUNTADMIN));
     }
 
     private static TreeRoleHolder getTreeRoleHolder(String treeId) {
         TreeDataHandler tree = TreePlugin.getTree(treeId);
         if (!(tree instanceof TreeRoleHolder)) {
             throw new RuntimeException(String.format("Programmer error: tree of class %s is not implementing the TreeRoleHolder interface", tree.getClass().getName()));
         }
         return (TreeRoleHolder) tree;
     }
 }
