 package com.x.web.service;
 
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.x.web.constant.ActivationType;
 import com.x.web.constant.OauthSource;
 import com.x.web.constant.UserStatus;
 import com.x.web.dao.hibernate.ActivationDAO;
 import com.x.web.dao.hibernate.SinaOauthUserDAO;
 import com.x.web.dao.hibernate.UserDAO;
 import com.x.web.domain.Activation;
 import com.x.web.domain.SinaOauthUser;
 import com.x.web.domain.User;
 import com.x.web.exception.AuthException;
 import com.x.web.exception.ServiceException;
 import com.x.web.exception.WebException;
 import com.x.web.util.AuthUtil;
 
 /**
  * 
  * @author sqlxx
  *
  */
 @Service
 @Transactional
 public class UserManagementService {
     private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);
     
     @Autowired
     private UserDAO userDAO;
     @Autowired
     private ActivationDAO activationDAO;
     @Autowired
     private SinaOauthUserDAO sinaOauthUserDAO;
     
     public User authenticate(String usernameOrEmail, String password) throws AuthException {
         log.debug("Start authenticate for user '{}'", usernameOrEmail);
         
         User user = userDAO.findByUsernameOrActiveEmail(usernameOrEmail);
         
        if (user != null && user.getPassword() != null) { //Password == null means it's a external platform user.
             
             if (user.getPassword().equals(AuthUtil.encodePassword(password))) {
                 log.debug("Password validation succeeded for user/email '{}'.", usernameOrEmail);
             } else {
                 log.warn("Password validation failed for user/email '{}'.", usernameOrEmail);
                 throw new AuthException("用户名或密码错误，请重试！");
             }
             
             if (user.getStatus() == UserStatus.DISABLE) {
                 log.warn("Password validation failed for user/email '{}'.", usernameOrEmail);
                 throw new AuthException("该用户已被禁用，请与管理员联系！");
             }
             
             return user;
         } else {
             log.warn("Unknown user '{}' try to login.", usernameOrEmail);
             throw new AuthException("用户名或密码错误，请重试！");
         }
     }
     
     public User login(String usernameOrEmail, String password) {
         User user = authenticate(usernameOrEmail, password);
         if (user != null) {
             user.setLastLogin(new Date());
             userDAO.saveOrUpdate(user);
         }
         return user;
     }
     
     public User login(Long userId) {
         User user = userDAO.findById(userId);
         if (user != null) {
             user.setLastLogin(new Date());
             userDAO.saveOrUpdate(user);
         }
         return user;
     }
     
     public User registerUser(String userName, String email, String password) {
         log.debug("Starting register user with {} & {}", userName, email);
         
         User user = userDAO.findByUsername(userName);
         if (user != null) {
             log.warn("The user name {} has already registered!", userName);
             throw new WebException("用户名:" + userName + " 已被注册，请更换！");
         }
         
         user = userDAO.findByEmail(email);
         if (user != null && !user.getStatus().equals(UserStatus.INACTIVE)) {
             log.warn("The email {} has already registered by {}!", email, user.getUsername());
             throw new WebException("email: " + email +" 已被注册，请更换！");
         }
         
         user = new User();
         user.setPlainPassword(password);
         user.setUsername(userName);
         user.setEmail(email);
         user.setStatus(UserStatus.INACTIVE);
         user.setCreatedDatetime(new Date());
         
         try {
             user = userDAO.saveOrUpdate(user);
             log.debug("Register user ({}, {}) successfully", userName, email);
             return user;
         } catch (DataAccessException ex) {
             log.warn("Can't save user into the database. " + ex.getMessage(), ex);
             throw new ServiceException("创建用户失败！", ex);
         }
     }
     
     public User modifyUser(User user) {
         log.debug("Start updating user information: {}", user);
         try {
             user = userDAO.saveOrUpdate(user);
             log.debug("Saved the {} into database.", user);
             return user;
         
         } catch (DataAccessException ex) {
             log.warn("Can't save user into the database. " + ex.getMessage(), ex);
             throw new ServiceException("更新用户信息失败！", ex);
         }
     }
     
     public User activateUser(String verifyString, ActivationType type) {
         log.debug("Start activating user with verifyString: {}, activation type: {}", verifyString, type.name());
         
         Activation activation = activationDAO.findActivation(verifyString, type);
         if (activation != null) {
             
             User user = userDAO.findById(activation.getUserId());
             if (user.getStatus().equals(UserStatus.INACTIVE)) {
                 user.setStatus(UserStatus.ACTIVE);
             }
             return user;
         } else {
             log.warn("The activation string is not found in database. str={}. type={}", verifyString, type.name());
             throw new ServiceException("激活字符串已失效，请重新申请激活！");
         }
         
     }
     
     
     public List<User> listAllUsers() {
         try {
             return userDAO.listAll();
         } catch (DataAccessException ex) {
             log.warn("Data access exception in listAllUser() " + ex.getMessage(), ex);
             throw new ServiceException("查询用户失败！", ex);
         }
     }
     
     public SinaOauthUser saveOrUpdateSinaOauthUser(Long sinaUserId, String token, String tokenSecret) {
         SinaOauthUser sinaOauthUser = new SinaOauthUser();
         sinaOauthUser.setSinaUserId(sinaUserId);
         sinaOauthUser.setToken(token);
         sinaOauthUser.setTokenSecret(tokenSecret);
         sinaOauthUser.setUpdatedDate(new Date());
         
         sinaOauthUser = sinaOauthUserDAO.saveOrUpdate(sinaOauthUser);
         return sinaOauthUser;
     }
 
     /**
      * @param string
      */
    public User createUser(Long externalId, String userName, OauthSource source) {
         User user = new User();
         user.setCreatedDatetime(new Date());
         user.setLoginFromOauth(true);
         user.setOauthSource(source);
         user.setUsername(userName);
         user.setStatus(UserStatus.ACTIVE);
         
        user = userDAO.saveOrUpdate(user);
        
        if (source.equals(OauthSource.SINA)) {
            sinaOauthUserDAO.findById(externalId).setUserId(user.getId());
        }
         
         return user;
     }
     
      
 }
