 package org.homebudget.services;
 
 import java.util.List;
 import javax.annotation.Resource;
 import org.apache.log4j.Logger;
 import org.homebudget.dao.UserRepository;
 import org.homebudget.dao.UserRoleRepository;
 import org.homebudget.model.BinaryResource;
 import org.homebudget.model.UserDetails;
 import org.homebudget.model.UserRole;
 import org.springframework.beans.BeanUtils;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.multipart.MultipartFile;
 
 @Service
 public class UserManagementService {
 
    private static final Logger gLogger = Logger.getLogger(UserManagementService.class);
 
    @Resource
    private UserRepository userRepositoryDao;
 
    @Resource
    private UserRoleRepository userRoleRepository;
 
    @Resource
    private MailService mailService;
 
    @Resource
    private ResourceManagementService resourceManagementService;
 
    public UserDetails getNewUser(UserRole.Role role) {
 
       return new UserDetails();
    }
 
    @Transactional
    public UserDetails saveUserDetails(UserDetails userDetails, MultipartFile userPic) {
       if (userPic != null) {
          BinaryResource resource = resourceManagementService.getResource(userPic);
          userDetails.setUserPic(resource);
       }
       return saveUserDetails(userDetails);
    }
 
    @Transactional
    public UserDetails saveUserDetails(UserDetails userDetails) {
       return userRepositoryDao.save(userDetails);
    }
 
    @Transactional
    public void deleteUserDetails(UserDetails userDetails) {
 
       userRepositoryDao.delete(userDetails);
    }
 
    @Transactional
    public void deleteAllUserDetails() {
 
       userRepositoryDao.deleteAll();
    }
 
    @Transactional
    public UserRole saveUserRole(UserRole userRole) {
 
      return userRoleRepository.save(userRole);
    }
 
    public UserDetails registerUser(UserDetails aUserDetails) {
 
       final String userPassword = aUserDetails.getPassword();
       try {
          String passwordHash = PasswordService.getHash(aUserDetails.getPassword());
          aUserDetails.setPassword(passwordHash);
       }
       catch (Exception e) {// with is a hack. Must be removed. If hashing
          // fails, user must be notified.
          aUserDetails.setPassword(userPassword);
          gLogger.error("Failed to create hash from password", e);
       }
       aUserDetails.addUserRole(userRoleRepository.findByRole(UserRole.Role.USER_ROLE));
       // TODO: set to 0, when email confirmation is implemented
       aUserDetails.getMetadata().setEnabled(1);
       mailService.sendConfirmation(aUserDetails);
      return saveUserDetails(aUserDetails);
    }
 
    public List<UserDetails> getAllUsers() {
 
       return userRepositoryDao.findAll();
    }
 
    public UserDetails getUserDetailsByUsername(String userName) {
 
       return userRepositoryDao.findByUsername(userName);
    }
 
    public UserDetails getUserByEmail(String email) {
 
       return userRepositoryDao.findByEmail(email);
    }
 
    public UserDetails updateUserDetails(UserDetails oldUserDetails, UserDetails newUserDetails) {
 
       BeanUtils.copyProperties(newUserDetails, oldUserDetails, new String[]{"password", "id",
         "roles", "enabled"});
       final String userPassword = newUserDetails.getPassword();
       try {
          final String passwordHash = PasswordService.getHash(newUserDetails.getPassword());
          oldUserDetails.setPassword(passwordHash);
       }
       catch (Exception e) {// with is a hack. Must be removed. If hashing
          // fails, user must be notified.
          oldUserDetails.setPassword(userPassword);
          gLogger.error("Failed to create hash from password", e);
       }
       return userRepositoryDao.save(oldUserDetails);
 
    }
 
    public void updateUserDetails(UserDetails oldUserDetails, UserDetails newUserDetails,
        MultipartFile userPic) {
       if (userPic != null) {
          BinaryResource resource = resourceManagementService.getResource(userPic);
          newUserDetails.setUserPic(resource);
       }
       updateUserDetails(oldUserDetails, newUserDetails);
    }
 
    public String getUserPicString(BinaryResource userPic) {
       String userPicString = "";
       if (userPic != null) {
          userPicString = resourceManagementService.getBase64ImageString(userPic);
       }
       return userPicString;
    }



 }
