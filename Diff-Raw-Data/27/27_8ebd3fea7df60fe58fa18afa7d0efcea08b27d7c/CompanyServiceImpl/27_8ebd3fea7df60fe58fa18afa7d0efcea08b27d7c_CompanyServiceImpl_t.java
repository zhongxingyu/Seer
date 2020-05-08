 package com.blazebit.security.web.service.impl;
 
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import com.blazebit.security.ActionFactory;
 import com.blazebit.security.Permission;
 import com.blazebit.security.PermissionDataAccess;
 import com.blazebit.security.PermissionManager;
 import com.blazebit.security.PermissionService;
 import com.blazebit.security.constants.ActionConstants;
 import com.blazebit.security.impl.model.Company;
 import com.blazebit.security.impl.model.EntityField;
 import com.blazebit.security.impl.model.EntityObjectField;
 import com.blazebit.security.impl.model.User;
 import com.blazebit.security.impl.model.UserGroup;
 import com.blazebit.security.impl.utils.ActionUtils;
 import com.blazebit.security.metamodel.ResourceMetamodel;
 import com.blazebit.security.web.bean.UserSession;
 import com.blazebit.security.web.service.api.CompanyService;
 import com.blazebit.security.web.service.api.UserGroupService;
 import com.blazebit.security.web.service.api.UserService;
 
 @Stateless
 public class CompanyServiceImpl implements CompanyService {
 
     @PersistenceContext(unitName = "TestPU")
     EntityManager entityManager;
 
     @Inject
     private UserService userService;
     @Inject
     private UserGroupService userGroupService;
     @Inject
     private UserSession userSession;
     @Inject
     private PermissionManager permissionManager;
     @Inject
     private PermissionService permissionService;
     @Inject
     private PermissionDataAccess permissionDataAccess;
     @Inject
     private ActionUtils actionUtils;
     @Inject
     private ActionFactory actionFactory;
     @Inject
     private ResourceMetamodel resourceMetaModel;
 
     @Override
     public List<Company> findCompanies() {
         return entityManager.createQuery("SELECT company FROM " + Company.class.getCanonicalName() + " company order by company.name", Company.class).getResultList();
     }
 
     @Override
     public Company saveCompany(Company selectedCompany) {
         Company company = entityManager.merge(selectedCompany);
         adjustFieldLevelPermissions(company);
         removeObjectLevelPermissions(company);
         removeGroupHierarchy(company);
         // fix user level permissions
         // TODO adjust current user permissions to the groups the user belongs to?
         return company;
     }
 
     private void removeGroupHierarchy(Company company) {
         if (!company.isGroupHierarchyEnabled()) {
             List<UserGroup> groups = userGroupService.getAllGroups(userSession.getSelectedCompany());
             for (UserGroup group : groups) {
                 group.setParent(null);
                 userGroupService.saveGroup(group);
             }
 
         }
     }
 
     private void removeObjectLevelPermissions(Company company) {
         // fix object level permissions
         if (!company.isObjectLevelEnabled()) {
             List<User> users = userService.findUsers(userSession.getSelectedCompany());
             for (User user : users) {
                 List<Permission> permissions = permissionManager.getPermissions(user);
                 for (Permission permission : permissions) {
                     EntityField entityField = (EntityField) permission.getResource();
                     if (entityField instanceof EntityObjectField) {
                         permissionManager.remove(permission);
                     }
                 }
             }
             List<UserGroup> groups = userGroupService.getAllParentGroups(userSession.getSelectedCompany());
             for (UserGroup parent : groups) {
                 removeGroupObjectPermissions(parent);
             }
 
         }
     }
 
     private void adjustFieldLevelPermissions(Company company) {
         // fix existing permissions
         List<User> users = userService.findUsers(userSession.getSelectedCompany());
         for (User user : users) {
             List<Permission> permissions = permissionManager.getPermissions(user);
             for (Permission permission : permissions) {
                 if (!company.isFieldLevelEnabled()) {
                     removeFieldPermissions(user, permission);
                 } else {
                     addFieldPermissions(user, permission);
                 }
             }
         }
         List<UserGroup> groups = userGroupService.getAllParentGroups(userSession.getSelectedCompany());
         for (UserGroup parent : groups) {
             if (!company.isFieldLevelEnabled()) {
                 removeGroupFieldPermissions(parent);
             } else
                 addGroupFieldPermissions(parent);
         }
 
     }
 
     private void addFieldPermissions(User user, Permission permission) {
         EntityField entityField = (EntityField) permission.getResource();
         List<String> fields;
         try {
             fields = resourceMetaModel.getCollectionFields(entityField.getEntity());
            if (permissionDataAccess.findPermission(user, actionFactory.createAction(ActionConstants.UPDATE), entityField) != null) {
                for (String field : fields) {
                    if (permissionDataAccess.isGrantable(user, actionFactory.createAction(ActionConstants.ADD), entityField.getParent().getChild(field))) {
                        permissionService.grant(userSession.getUser(), user, actionFactory.createAction(ActionConstants.ADD), entityField.getParent().getChild(field));
                    }
                    if (permissionDataAccess.isGrantable(user, actionFactory.createAction(ActionConstants.REMOVE), entityField.getParent().getChild(field))) {
                        permissionService.grant(userSession.getUser(), user, actionFactory.createAction(ActionConstants.REMOVE), entityField.getParent().getChild(field));
                    }
                 }
             }
         } catch (ClassNotFoundException e) {
         }
 
     }
 
     private void removeFieldPermissions(User user, Permission permission) {
         EntityField entityField = (EntityField) permission.getResource();
         if (!entityField.isEmptyField()) {
             if (actionUtils.getActionsForCollectionField().contains(permission.getAction())) {
                 if (permissionDataAccess.isGrantable(user, actionFactory.createAction(ActionConstants.UPDATE), entityField.getParent())) {
                     permissionService.grant(userSession.getUser(), user, actionFactory.createAction(ActionConstants.UPDATE), entityField.getParent());
                 }
             } else {
                 // replace with parent resource(=entity permission) permission
                 if (permissionDataAccess.isGrantable(user, permission.getAction(), entityField.getParent())) {
                     permissionService.grant(userSession.getUser(), user, permission.getAction(), entityField.getParent());
                 }
             }
             // delete field permission
             permissionManager.remove(permission);
         }
     }
 
     private void processPermission(UserGroup group, Permission permission) {
         EntityField entityField = (EntityField) permission.getResource();
         if (!entityField.isEmptyField()) {
 
             if (actionUtils.getActionsForCollectionField().contains(permission.getAction())) {
                 if (permissionDataAccess.isGrantable(group, actionFactory.createAction(ActionConstants.UPDATE), entityField.getParent())) {
                     permissionService.grant(userSession.getUser(), group, actionFactory.createAction(ActionConstants.UPDATE), entityField.getParent());
                 }
             } else {
                 // replace with parent resource(=entity permission) permission
                 if (permissionDataAccess.isGrantable(group, permission.getAction(), entityField.getParent())) {
                     permissionService.grant(userSession.getUser(), group, permission.getAction(), entityField.getParent());
                 }
             }
             // delete field permission
             permissionManager.remove(permission);
         }
     }
 
     private void removeGroupFieldPermissions(UserGroup parent) {
         List<Permission> permissions = permissionManager.getPermissions(parent);
         for (Permission permission : permissions) {
             processPermission(parent, permission);
         }
         for (UserGroup child : userGroupService.getGroupsForGroup(parent)) {
             removeGroupFieldPermissions(child);
         }
 
     }
 
     private void addGroupFieldPermissions(UserGroup parent) {
         List<Permission> permissions = permissionManager.getPermissions(parent);
         for (Permission permission : permissions) {
             addGroupFieldPermissions(parent, permission);
         }
         for (UserGroup child : userGroupService.getGroupsForGroup(parent)) {
             addGroupFieldPermissions(child);
         }
 
     }
 
     private void addGroupFieldPermissions(UserGroup parent, Permission permission) {
         EntityField entityField = (EntityField) permission.getResource();
         List<String> fields;
         try {
             fields = resourceMetaModel.getCollectionFields(entityField.getEntity());
             for (String field : fields) {
                 if (permissionDataAccess.isGrantable(parent, actionFactory.createAction(ActionConstants.ADD), entityField.getChild(field))) {
                     permissionService.grant(userSession.getUser(), parent, actionFactory.createAction(ActionConstants.ADD), entityField.getChild(field));
                 }
                 if (permissionDataAccess.isGrantable(parent, actionFactory.createAction(ActionConstants.REMOVE), entityField.getChild(field))) {
                     permissionService.grant(userSession.getUser(), parent, actionFactory.createAction(ActionConstants.REMOVE), entityField.getChild(field));
                 }
             }
         } catch (ClassNotFoundException e) {
         }
 
     }
 
     private void removeGroupObjectPermissions(UserGroup parent) {
         List<Permission> permissions = permissionManager.getPermissions(parent);
         for (Permission permission : permissions) {
             EntityField entityField = (EntityField) permission.getResource();
             if (entityField instanceof EntityObjectField) {
                 permissionManager.remove(permission);
             }
         }
         for (UserGroup child : userGroupService.getGroupsForGroup(parent)) {
             removeGroupObjectPermissions(child);
         }
     }
 
 }
