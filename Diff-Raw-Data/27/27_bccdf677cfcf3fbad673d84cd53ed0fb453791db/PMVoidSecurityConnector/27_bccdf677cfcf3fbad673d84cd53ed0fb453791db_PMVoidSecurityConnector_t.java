 package jpaoletti.jpm.security.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This security connector acceps any user as ok
  * 
  * @author jpaoletti
  */
 public class PMVoidSecurityConnector extends PMSecurityAbstractConnector {
 
     private static final List<PMSecurityUser> users = new ArrayList<PMSecurityUser>();
     private static final List<PMSecurityPermission> permissions = new ArrayList<PMSecurityPermission>();
     private static final List<PMSecurityUserGroup> groups = new ArrayList<PMSecurityUserGroup>();
 
     static {
 
         final PMSecurityPermission sysadmin = new PMSecurityPermission();
         sysadmin.setName("sysadmin");
         sysadmin.setDescription("Sysadmin");
 
         final PMSecurityPermission useradmin = new PMSecurityPermission();
         useradmin.setName("useradmin");
         useradmin.setDescription("Useradmin");
 
         permissions.add(useradmin);
         permissions.add(sysadmin);
 
         final PMSecurityUserGroup group = new PMSecurityUserGroup();
         group.setName("all");
         group.setDescription("All");
         group.setId("all");
         group.setActive(true);
         group.setPermissions(permissions);
         groups.add(group);
 
         final PMSecurityUser user = new PMSecurityUser();
         user.setUsername("test");
         user.setName("test");
         user.setActive(true);
         user.setDeleted(false);
         user.setChangePassword(false);
         user.setGroups(groups);
 
         users.add(user);
     }
 
     @Override
     public PMSecurityUser authenticate(String username, String password) throws PMSecurityException {
        try {
            return super.authenticate(username, password);
        } catch (InvalidPasswordException e) {
            return getUser(username);
        }
     }
 
     @Override
     public PMSecurityUser getUser(String username) throws PMSecurityException {
         return users.get(0);
     }
 
     @Override
     public List<PMSecurityUser> getUsers() throws PMSecurityException {
         return users;
     }
 
     @Override
     public void addUser(PMSecurityUser user) throws PMSecurityException {
         users.add(user);
     }
 
     @Override
     public void updateUser(PMSecurityUser user) throws PMSecurityException {
     }
 
     @Override
     public PMSecurityUserGroup getGroup(String id) throws PMSecurityException {
         for (PMSecurityUserGroup group : groups) {
             if (id.equals(group.getId())) {
                 return group;
             }
         }
         return null;
     }
 
     @Override
     public List<PMSecurityUserGroup> getGroups() throws PMSecurityException {
         return groups;
     }
 
     @Override
     public void addGroup(PMSecurityUserGroup group) throws PMSecurityException {
         groups.add(group);
     }
 
     @Override
     public void updateGroup(PMSecurityUserGroup group) throws PMSecurityException {
     }
 
     @Override
     public void removeGroup(PMSecurityUserGroup group) throws PMSecurityException {
         groups.remove(group);
     }
 
     @Override
     public List<PMSecurityPermission> getPermissions() throws PMSecurityException {
         return permissions;
     }
 }
