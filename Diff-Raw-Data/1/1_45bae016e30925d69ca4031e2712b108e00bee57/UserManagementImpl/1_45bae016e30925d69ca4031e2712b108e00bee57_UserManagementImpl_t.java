 package org.synyx.minos.umt.service;
 
 import org.springframework.dao.IncorrectResultSizeDataAccessException;
 
 import org.springframework.transaction.annotation.Transactional;
 
 import org.springframework.util.Assert;
 
 import org.synyx.hades.domain.Page;
 import org.synyx.hades.domain.Pageable;
 
 import org.synyx.minos.core.domain.Role;
 import org.synyx.minos.core.domain.User;
 import org.synyx.minos.core.security.AuthenticationService;
 import org.synyx.minos.umt.dao.RoleDao;
 import org.synyx.minos.umt.dao.UserDao;
 
 import java.util.List;
 
 
 /**
  * Implementation of user management module. Uses an implementation of {@code PasswordCreator} to generate a random
  * password, that is used as initial password for a new user after successful registration.
  *
  * @author  Oliver Gierke - gierke@synyx.de
  */
 @Transactional(readOnly = true)
 public class UserManagementImpl implements UserManagement, UserAccountManagement {
 
     private final UserDao userDao;
     private final RoleDao roleDao;
 
     private final AuthenticationService authenticationService;
     private final PasswordCreator passwordCreator;
 
     /**
      * Creates a new {@link UserManagementImpl} with the given {@link UserDao}, {@link RoleDao},
      * {@link AuthenticationService} and {@link PasswordCreator}.
      *
      * @param  userDao
      * @param  roleDao
      * @param  authenticationService
      */
     public UserManagementImpl(UserDao userDao, RoleDao roleDao, AuthenticationService authenticationService,
         PasswordCreator passwordCreator) {
 
         Assert.notNull(userDao);
         Assert.notNull(roleDao);
         Assert.notNull(authenticationService);
 
         this.userDao = userDao;
         this.roleDao = roleDao;
         this.authenticationService = authenticationService;
         this.passwordCreator = passwordCreator;
     }
 
     @Override
     @Transactional(readOnly = false)
     public void delete(User user) throws UserNotFoundException {
 
         Assert.notNull(user);
 
         if (authenticationService.isCurrentUser(user)) {
             throw new IllegalArgumentException("Illegal attempt to delete the currently logged in user!");
         }
 
         if (!exists(user.getId())) {
             throw new UserNotFoundException(user.getId());
         }
 
         userDao.delete(user);
     }
 
 
     @Override
     @Transactional(readOnly = false)
     public void save(User user) {
 
         Assert.notNull(user);
 
         if (!user.isNew() && !exists(user.getId())) {
             throw new IllegalArgumentException("User with id " + user.getId() + " does not exist!");
         }
 
         if (user.isNew() && !user.hasPassword()) {
             user.setPassword(passwordCreator.generatePassword());
         }
 
         if (!user.isNew() && !user.hasPassword()) {
             user.setPassword(userDao.readByPrimaryKey(user.getId()).getPassword());
         }
 
         // Save prior to encrypting to make sure potential salts get populated
         userDao.save(user);
 
         if (passwordEncryptionNeeded(user)) {
             user.setPassword(authenticationService.getEncryptedPasswordFor(user));
         }
 
         userDao.save(user);
     }
 
 
     @Override
     @Transactional(readOnly = false)
     public void save(Role role) {
 
         Assert.notNull(role);
 
         roleDao.save(role);
     }
 
 
     @Override
     public List<User> getUsers() {
 
         return userDao.readAll();
     }
 
 
     @Override
     public Page<User> getUsers(Pageable pageable) {
 
         return userDao.readAll(pageable);
     }
 
 
     @Override
     public User getUser(Long id) {
 
         Assert.notNull(id);
 
         try {
             return userDao.readByPrimaryKey(id);
         } catch (IncorrectResultSizeDataAccessException e) {
             return null;
         }
     }
 
 
     @Override
     public User getUser(String username) {
 
         Assert.notNull(username);
 
         try {
             return userDao.findByUsername(username);
         } catch (IncorrectResultSizeDataAccessException e) {
             return null;
         }
     }
 
 
     private boolean passwordEncryptionNeeded(User user) {
 
         Assert.notNull(user);
         Assert.isTrue(user.hasPassword());
 
         return authenticationService != null && !user.getPassword().isEncrypted();
     }
 
 
     @Override
     public boolean exists(Long id) {
 
         if (null == id) {
             return false;
         }
 
         return null != userDao.readByPrimaryKey(id);
     }
 
 
     @Override
     public List<Role> getRoles() {
 
         return roleDao.readAll();
     }
 
 
     @Override
     public Role getRole(Long id) {
 
         return roleDao.readByPrimaryKey(id);
     }
 
 
     @Override
     public Role getRole(String name) {
 
         return roleDao.findByName(name);
     }
 
 
     @Override
     public List<User> getUsersByEmail(String argEmail) {
 
         return userDao.findByEmailAddress(argEmail);
     }
 
 
     @Override
     @Transactional(readOnly = false)
     public void deleteRole(Role role) {
 
         roleDao.delete(role);
     }
 
 
     @Override
    @Transactional(readOnly = false)
     public void saveUserAccount(User user) {
 
         Assert.notNull(user);
 
         User existingUser = getUser(user.getId());
         user.setUsername(existingUser.getUsername());
         user.setRoles(existingUser.getRoles());
         user.setActive(existingUser.isActive());
         user.setPassword(existingUser.getPassword());
 
         save(user);
     }
 
 
     @Override
     public Page<User> getUsersByRole(Role role, Pageable pageable) {
 
         return userDao.findByRole(pageable, role);
     }
 
 
     @Override
     public List<User> getUsersByRole(Role role) {
 
         return userDao.findByRole(role);
     }
 }
