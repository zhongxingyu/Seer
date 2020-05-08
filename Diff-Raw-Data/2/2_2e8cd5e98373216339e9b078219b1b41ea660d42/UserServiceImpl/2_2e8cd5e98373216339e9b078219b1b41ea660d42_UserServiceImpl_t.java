 /*
 *	http://www.jrecruiter.org
 *
 *	Disclaimer of Warranty.
 *
 *	Unless required by applicable law or agreed to in writing, Licensor provides
 *	the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
 *	including, without limitation, any warranties or conditions of TITLE,
 *	NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are
 *	solely responsible for determining the appropriateness of using or
 *	redistributing the Work and assume any risks associated with Your exercise of
 *	permissions under this License.
 *
 */
 package org.jrecruiter.service.impl;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.jasypt.digest.StringDigester;
 import org.jrecruiter.common.CollectionUtils;
 import org.jrecruiter.common.Constants;
 import org.jrecruiter.dao.ConfigurationDao;
 import org.jrecruiter.dao.RoleDao;
 import org.jrecruiter.dao.UserDao;
 import org.jrecruiter.model.Role;
 import org.jrecruiter.model.User;
 import org.jrecruiter.model.UserToRole;
 import org.jrecruiter.service.NotificationService;
 import org.jrecruiter.service.UserService;
 import org.jrecruiter.service.exceptions.DuplicateUserException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.userdetails.UserDetails;
 import org.springframework.security.userdetails.UserDetailsService;
 import org.springframework.security.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.rrze.idmone.utils.jpwgen.BlankRemover;
 import de.rrze.idmone.utils.jpwgen.PwGenerator;
 
 /**
  * Provides user specific services.
  *
  * @author Dorota Puchala
  * @author Gunnar Hillert
  *
  * @version $Id$
  */
 @Service("userService")
 @Transactional
 public class UserServiceImpl implements UserService, UserDetailsService {
 
     /**
      *   Initialize Logging.
      */
     private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
 
     private @Autowired NotificationService notificationService;
 
     private @Autowired MessageSource messageSource;
 
     /**
      * User Dao.
      */
     private @Autowired UserDao userDao;
 
     /**
      * UserRole Dao.
      */
     private @Autowired RoleDao roleDao;
 
     /**
      * Access to settings.
      */
     private @Autowired ConfigurationDao configurationDao;
 
     private @Autowired StringDigester stringDigester;
 
     //~~~~~Business Methods~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
     /** {@inheritDoc} */
     public User addUser(User user) throws DuplicateUserException{
 
         if (user == null) {
             throw new IllegalArgumentException("User must not be null.");
         }
 
         Date registerDate = new Date();
         user.setRegistrationDate(registerDate);
         user.setUpdateDate(registerDate);
         user.setEnabled(Boolean.FALSE);
         user.setVerificationKey(generateUuid());
         user.setUsername(user.getEmail());
         User duplicateUser = userDao.getUser(user.getUsername());
 
         if (duplicateUser!= null) {
             throw new DuplicateUserException("User " + duplicateUser.getUsername()
                                            + "(Id="  + duplicateUser.getId()
                                            + ") already exists!");
         }
 
         Role role = roleDao.getRole(Constants.Roles.MANAGER.name());
 
         if (role == null) {
             throw new IllegalStateException("Role was not found but is required.");
         }
 
         Set<UserToRole> userToRoles = user.getUserToRoles();
 
         UserToRole utr = new UserToRole();
         utr.setRole(role);
         utr.setUser(user);
 
         userToRoles.add(utr);
 
         User savedUser = this.saveUser(user);
 
         return savedUser;
 
     }
 
     /** {@inheritDoc} */
     public User addUser(User user, String accountValidationUrl) throws DuplicateUserException{
 
         if (user == null) {
             throw new IllegalArgumentException("User must not be null.");
         }
         if (accountValidationUrl == null) {
             throw new IllegalArgumentException("accountValidationUrl must not be null.");
         }
 
         final User savedUser = this.addUser(user);
 
         final Map<String, Object> context = CollectionUtils.getHashMap();
         context.put("user", savedUser);
         context.put("registrationCode", this.generateUuid());
         context.put("accountValidationUrl", accountValidationUrl);
 
         notificationService.sendEmail(savedUser.getEmail(), messageSource.getMessage("class.UserServiceImpl.addUser.account.validation.subject", null, LocaleContextHolder.getLocale()), context, "account-validation");
 
         return savedUser;
 
 
     }
 
     /** {@inheritDoc} */
     public User saveUser(User user) {
         return userDao.save(user);
     }
 
     /** {@inheritDoc} */
     @Transactional(readOnly = true, propagation=Propagation.SUPPORTS)
     public User getUser(String username) {
         return userDao.getUser(username);
     }
 
     /** {@inheritDoc} */
     public void updateUser(User user) {
         Date updateDate = new Date();
         user.setUpdateDate(updateDate);
         userDao.save(user);
     }
 
     /** {@inheritDoc} */
     @Transactional(readOnly = true, propagation=Propagation.SUPPORTS)
     public List<User> getAllUsers() {
 
         return userDao.getAllUsers();
     }
 
     /** {@inheritDoc} */
     public void deleteUser(User user){
         userDao.remove(user.getId());
     }
 
     /** {@inheritDoc} */
     public void resetPassword(User user) {
 
         String flags = "-N 1 -M 100 -B -n -c -y -s 10 -o ";
         flags = BlankRemover.itrim(flags);
         String[] ar = flags.split(" ");
         PwGenerator generator = new PwGenerator();
         List <String> passwords = generator.process(ar);
 
         String password = null;
 
         for (Iterator<String> iterator = passwords.iterator(); iterator.hasNext();) {
             password = iterator.next();
         }
 
         user.setPassword(this.stringDigester.digest(password));
 
         this.updateUser(user);
 
         final Map<String, Object> context = CollectionUtils.getHashMap();
         context.put("user", user);
         context.put("password", password);
 
         this.notificationService.sendEmail(user.getEmail(),
                                            messageSource.getMessage("class.UserServiceImpl.resetPassword.email.subject",
                                            null,
                                            LocaleContextHolder.getLocale()),
                                            context,
                                            "get-password");
 
         LOGGER.info("resetPassword - Email sent to: " + user.getEmail() + "; id: " + user.getId());
     }
 
     /** {@inheritDoc} */
     @Transactional(readOnly = true, propagation=Propagation.SUPPORTS)
     public UserDetails loadUserByUsername(final String emailOrUserName) throws UsernameNotFoundException, DataAccessException {
 
         final User user = userDao.getUserByUsernameOrEmail(emailOrUserName.trim());
 
         if (user==null){
             LOGGER.warn("loadUserByUsername() - No user with id " + emailOrUserName + " found.");
             throw new UsernameNotFoundException("loadUserByUsername() - No user with id " + emailOrUserName + " found.");
         }
 
        LOGGER.info("User {} ({}) loaded.", new Object[] { user.getUsername(), user.getEmail()});
 
         return user;
     }
 
     /** {@inheritDoc} */
     @Transactional(readOnly = true, propagation=Propagation.SUPPORTS)
     public User getUser(Long userId) {
         return userDao.get(userId);
     }
 
     /** {@inheritDoc} */
     @Transactional(readOnly = true, propagation=Propagation.SUPPORTS)
     public List<User> getUsers(Integer pageSize, Integer pageNumber, Map<String, String> sortOrders, Map<String, String> userFilters) {
         return userDao.getUsers(pageSize, pageNumber, sortOrders, userFilters);
     }
 
     /** {@inheritDoc} */
     @Transactional(readOnly = true, propagation=Propagation.SUPPORTS)
     public Long getUsersCount() {
         return userDao.getUsersCount();
     }
 
     public String generateUuid() {
         return UUID.randomUUID().toString();
     }
 
     /** {@inheritDoc} */
     @Transactional(readOnly = true, propagation=Propagation.SUPPORTS)
     public User getUserByVerificationKey(final String key) {
         return userDao.getUserByVerificationKey(key);
     }
 }
