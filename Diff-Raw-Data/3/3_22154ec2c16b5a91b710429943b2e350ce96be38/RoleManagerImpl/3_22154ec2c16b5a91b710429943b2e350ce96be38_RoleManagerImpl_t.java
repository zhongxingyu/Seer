 package polly.core.roles;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import de.skuzzle.polly.sdk.PersistenceManager;
 import de.skuzzle.polly.sdk.WriteAction;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.exceptions.RoleException;
 import de.skuzzle.polly.sdk.model.User;
 import de.skuzzle.polly.sdk.roles.RoleManager;
 import de.skuzzle.polly.sdk.roles.SecurityContainer;
 import de.skuzzle.polly.sdk.roles.SecurityObject;
 
 
 public class RoleManagerImpl implements RoleManager {
     
     private final static Logger logger = Logger.getLogger(RoleManagerImpl.class
         .getName());
     
     private final static Object SYNC = new Object();
     
     private PersistenceManager persistence;
     private boolean rolesStale;
     private Set<String> allRoles;
     
     
     
     public RoleManagerImpl(PersistenceManager persistence) {
         this.persistence = persistence;
     }
     
     
 
     @Override
     public boolean roleExists(String roleName) {
         synchronized (SYNC) {
             return this.getRoles().contains(roleName);
         }
     }
     
     
 
     @Override
     public boolean permissionExists(String permissionName) {
         try {
             this.persistence.readLock();
             return this.persistence.findSingle(Permission.class, 
                 Permission.PERMISSION_BY_NAME, permissionName) != null;
         } finally {
             this.persistence.readUnlock();
         }
     }
 
     
     
     @Override
     public Set<String> getRoles() {
         synchronized(SYNC) {
             if (this.rolesStale || this.allRoles == null) {
                 List<Role> roles = this.persistence.atomicRetrieveList(
                     Role.class, Role.ALL_ROLES);
                 this.allRoles = new HashSet<String>(roles.size());
                 for (Role role : roles) {
                     this.allRoles.add(role.getName());
                 }
                 this.rolesStale = false;
             }
         }
         return Collections.unmodifiableSet(this.allRoles);
     }
     
     
 
     @Override
     public Set<String> getRoles(User user) {
         Set<String> result = new HashSet<String>();
         for (Role role : ((polly.core.users.User)user).getRoles()) {
             result.add(role.getName());
         }
         return Collections.unmodifiableSet(result);
     }
     
     
     
     @Override
     public Set<String> getPermissions(String roleName) {
         try {
             this.persistence.readLock();
             Role role = this.persistence.findSingle(
                     Role.class, Role.ROLE_BY_NAME, roleName);
             
            if (role == null) {
                return Collections.emptySet();
            }
             return role.getPermissionNames();
         } finally {
             this.persistence.readUnlock();
         }
     }
     
     
 
     @Override
     public void createRole(final String newRoleName) 
                 throws DatabaseException {
         synchronized(SYNC) {
             try {
                 this.persistence.writeLock();
                     final Role role = 
                         this.persistence.findSingle(
                             Role.class, Role.ROLE_BY_NAME, newRoleName);
                     
                     if (role != null) {
                         return;
                     }
 
                     logger.info("Creating new Role: '" + newRoleName + "'");
                     this.persistence.startTransaction();
                     this.persistence.persist(new Role(newRoleName));
                     this.persistence.commitTransaction();
                     this.rolesStale = true;
             } finally {
                 this.persistence.writeUnlock();
             }
         }
     }
     
     
 
     @Override
     public void createRole(String baseRoleName, String newRoleName) 
                 throws RoleException, DatabaseException {
         synchronized (SYNC) {
             try {
                 this.persistence.writeLock();
                 final Role role = 
                     this.persistence.findSingle
                     (Role.class, Role.ROLE_BY_NAME, newRoleName);
                 
                 if (role != null) {
                     return;
                 }
                 
                 Role baseRole = 
                     this.persistence.findSingle(
                         Role.class, Role.ROLE_BY_NAME, baseRoleName);
                 
                 if (baseRole == null) {
                     throw new RoleException("Unknown base role: '" + baseRoleName + "'");
                 }
                 
                 logger.info("Creating new Role: '" + newRoleName + "' from base role '" + 
                         baseRoleName + "'");
                 
                 this.persistence.startTransaction();
                 this.persistence.persist(new Role(newRoleName, 
                         new HashSet<Permission>(baseRole.getPermissions())));
                 this.persistence.commitTransaction();
                 this.rolesStale = true;
             } finally {
                 this.persistence.writeUnlock();
             }
         }
     }
     
     
     
     @Override
     public void deleteRole(String roleName) throws RoleException, DatabaseException {
         if (roleName.equals(ADMIN_ROLE) || roleName.equals(DEFAULT_ROLE)) {
             throw new RoleException("Default roles cant be deleted");
         }
         
         try {
             this.persistence.writeLock();
             
             Role role = this.persistence.findSingle(
                     Role.class, Role.ROLE_BY_NAME, roleName);
             
             if (role == null) {
                 return;
             }
             
             List<User> allUsers = this.persistence.findList(
                     User.class, polly.core.users.User.ALL_USERS);
             logger.debug("Deleting role: '" + roleName + "'");
             this.persistence.startTransaction();
             this.persistence.remove(role);
             logger.trace("Removing role from all users.");
             for (User user : allUsers) {
                 polly.core.users.User puser = (polly.core.users.User) user;
                 puser.getRoles().remove(role);
             }
             this.persistence.commitTransaction();
         } finally {
             this.persistence.writeUnlock();
         }
     }
     
     
 
     @Override
     public void registerPermission(final String permission) throws DatabaseException {
         synchronized(SYNC) {
             if (!permissionExists(permission)) {
                 logger.debug("Registering permission: '" + permission + "'");
                 this.persistence.atomicPersist(new Permission(permission));
             }
         }
     }
 
     
     
     @Override
     public void registerPermissions(final Set<String> permissions) 
                 throws DatabaseException {
         synchronized(SYNC) {
             this.persistence.atomicWriteOperation(new WriteAction() {
                 @Override
                 public void performUpdate(PersistenceManager persistence) {
                     for (String perm : permissions) {
                         if (!permissionExists(perm)) {
                             logger.debug("Registering permission: '" + perm + "'");
                             persistence.persist(new Permission(perm));
                         }
                     }
                 }
             });
         }
     }
     
     
     
     @Override
     public void registerPermissions(SecurityContainer container)
             throws DatabaseException {
         this.registerPermissions(container.getContainedPermissions());
     }
     
     
 
     @Override
     public void assignPermission(final String roleName, final String permission) 
                 throws DatabaseException, RoleException {
         
         synchronized(SYNC) {
             final Role role = 
                 this.persistence.findSingle(Role.class, Role.ROLE_BY_NAME, roleName);
         
             if (role == null) {
                 throw new RoleException("Unknown role: " + roleName);
             }
             
             final Permission perm = this.persistence.findSingle(Permission.class, 
                     Permission.PERMISSION_BY_NAME, permission);
             
             if (perm == null) {
                 throw new RoleException("Unknown permission: " + permission);
             }
             
             // TODO: add permission to admin role
             logger.debug("Assigning permission '" + 
                     permission + "' to role '" + roleName + "'");
             
             this.persistence.atomicWriteOperation(new WriteAction() {
                 
                 @Override
                 public void performUpdate(PersistenceManager persistence) {
                     role.getPermissions().add(perm);
                     role.setStale(true); // this updates the permission name string set
                 }
             });
         }
     }
     
     
     
     @Override
     public void assignPermissions(String roleName, final Set<String> permissions) 
                 throws RoleException, DatabaseException {
         
         synchronized(SYNC) {
             final Role role = 
                 this.persistence.findSingle(Role.class, Role.ROLE_BY_NAME, roleName);
         
             if (role == null) {
                 throw new RoleException("Unknown role: " + roleName);
             }
             
             final List<Permission> perms = new ArrayList<Permission>(permissions.size());
             for (String permission : permissions) {
                 Permission perm = this.persistence.findSingle(Permission.class, 
                     Permission.PERMISSION_BY_NAME, permission);
                 if (perm == null) {
                     throw new RoleException("Unknown permission: '" + permission + "'");
                 }
                 perms.add(perm);
             }
             
             // TODO: add permission to admin role
             logger.debug("Assigning permission '" + 
                 permissions + "' to role '" + roleName + "'");
             this.persistence.atomicWriteOperation(new WriteAction() {
                 
                 @Override
                 public void performUpdate(PersistenceManager persistence) {
                     role.getPermissions().addAll(perms);
                     role.setStale(true); // this updates the permission name string set
                 }
             });
         }
     }
     
     
     
     @Override
     public void assignPermissions(String roleName, SecurityContainer container) 
                 throws RoleException, DatabaseException {
         this.assignPermissions(roleName, container.getContainedPermissions());
     }
     
     
 
     @Override
     public synchronized void removePermission(final String roleName, 
             final String permission) throws RoleException, DatabaseException {
         synchronized(SYNC) {
             final Role role = 
                     this.persistence.findSingle(Role.class, Role.ROLE_BY_NAME, roleName);
             
             if (role == null) {
                 throw new RoleException("Unknown role: " + roleName);
             }
             
             final Permission perm = this.persistence.findSingle(Permission.class, 
                 Permission.PERMISSION_BY_NAME, permission);
             
             if (perm == null) {
                 return;
             }
             
             logger.debug("Removing permission '" + 
                 permission + "' from role '" + roleName + "'");
             
             this.persistence.atomicWriteOperation(new WriteAction() {
                 
                 @Override
                 public void performUpdate(PersistenceManager persistence) {
                     role.getPermissions().remove(perm);
                     role.setStale(true);
                 }
             });
         }
     }
     
     
 
     @Override
     public synchronized void assignRole(final User user, final String roleName) 
             throws RoleException, DatabaseException {
         synchronized (SYNC) {
             final Role role = 
                 this.persistence.findSingle(Role.class, Role.ROLE_BY_NAME, roleName);
         
             if (role == null) {
                 throw new RoleException("Unknown role: " + roleName);
             }
             
             logger.debug("Assigning role '" + 
                 roleName + "' to user '" + user + "'");
             
             this.persistence.atomicWriteOperation(new WriteAction() {
                 @Override
                 public void performUpdate(PersistenceManager persistence) {
                     ((polly.core.users.User)user).getRoles().add(role);
                 }
             });
         }
     }
     
     
 
     @Override
     public synchronized void removeRole(final User user, final String roleName) 
             throws RoleException, DatabaseException {
         
         synchronized (SYNC) {
             logger.debug("Removing role '" + 
                 roleName + "' from user '" + user + "'");
             this.persistence.atomicWriteOperation(new WriteAction() {
                 @Override
                 public void performUpdate(PersistenceManager persistence) {
                     ((polly.core.users.User)user).getRoles().remove(new Role(roleName));
                 }
             });
         }
     }
     
     
 
     @Override
     public boolean hasPermission(User user, String permission) {
         if (permission.equals(RoleManager.NONE_PERMISSION)) {
             return true;
         } else if (user == null) {
             return false;
         }
         polly.core.users.User puser = (polly.core.users.User) user;
         
         synchronized (SYNC) {
             for (Role role : puser.getRoles()) {
                 if (role.getName().equals(RoleManager.ADMIN_ROLE) || 
                         role.getPermissionNames().contains(permission)) {
                     return true;
                 }
             }
         }
         return false;
     }
     
     
     
     @Override
     public boolean hasPermission(User user, Set<String> permissions) {
         for (String perm : permissions) {
             if (!this.hasPermission(user, perm)) {
                 return false;
             }
         }
         return true;
     }
 
     
     
     
     @Override
     public boolean canAccess(User user, SecurityObject securityObject) {
         return this.hasPermission(user, securityObject.getRequiredPermission());
     }
     
     
     
     @Override
     public void checkAccess(User user, SecurityObject securityObject)
             throws InsufficientRightsException {
         if (!this.canAccess(user, securityObject)) {
             this.denyAccess(securityObject);
         }
     }
     
     
     
     @Override
     public void denyAccess(SecurityObject securityObject) 
             throws InsufficientRightsException {
         throw new InsufficientRightsException(securityObject);
     }
 }
