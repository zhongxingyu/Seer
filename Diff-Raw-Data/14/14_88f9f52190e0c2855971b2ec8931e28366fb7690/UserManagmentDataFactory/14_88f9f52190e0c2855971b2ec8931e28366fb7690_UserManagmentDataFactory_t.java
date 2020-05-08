 package org.ddelizia.vcrud.core.test.util;
 
 import com.google.common.collect.Sets;
 import org.ddelizia.vcrud.core.basic.helper.MongoHelper;
 import org.ddelizia.vcrud.core.config.AppConfig;
 import org.ddelizia.vcrud.core.usermanagement.model.Customer;
 import org.ddelizia.vcrud.core.usermanagement.model.User;
 import org.ddelizia.vcrud.core.usermanagement.model.UserGroup;
 import org.ddelizia.vcrud.core.usermanagement.repository.UserGroupRepository;
 import org.ddelizia.vcrud.core.usermanagement.repository.UserRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ddelizia
  * Date: 24/02/14
  * Time: 21:29
  * To change this template use File | Settings | File Templates.
  */
 public class UserManagmentDataFactory {
 
     @Autowired
     private UserRepository userRepository;
 
     @Autowired
     private UserGroupRepository userGroupRepository;
 
     @Autowired
     private AppConfig appConfig;
 
     @Autowired
     private MongoTemplate mongoTemplate;
 
     @Autowired
     private MongoHelper mongoHelper;
 
     public static final String CUSTOMER_VERIFIED_RESTGROUP_NAME = "customerName";
     public static final String CUSTOMER_VERIFIED_RESTGROUP_EMAIL = "email@example.com";
 
     public void createData(){
         UserGroup userGroupAuthenticated = new UserGroup();
         userGroupAuthenticated.setGroupName(appConfig.getProperty(AppConfig.USER_USERGROUP_AUTHENTICATED, String.class, null));
         userGroupAuthenticated.setName(appConfig.getProperty(AppConfig.USER_USERGROUP_AUTHENTICATED, String.class, null));
         userGroupRepository.save(userGroupAuthenticated);
 
         UserGroup userGroupRest = new UserGroup();
         userGroupRest.setGroupName(appConfig.getProperty(AppConfig.USER_USERGROUP_REST, String.class, null));
         userGroupRest.setName(appConfig.getProperty(AppConfig.USER_USERGROUP_REST, String.class, null));
         userGroupRest.setFather(userGroupAuthenticated);
         userGroupRepository.save(userGroupRest);
 
         UserGroup userGroupAuthenticatedUpdate = userGroupRepository.findOne(userGroupAuthenticated.getId());
         userGroupAuthenticatedUpdate.setChilds(Sets.newHashSet(userGroupRest));
        //userGroupRepository.save(userGroupAuthenticatedUpdate);
 
         User customerVerifiedRestGroup = new Customer();
         customerVerifiedRestGroup.setAccountLocked(false);
         customerVerifiedRestGroup.setCredentialsExpireTime(null);
         customerVerifiedRestGroup.setEmail(CUSTOMER_VERIFIED_RESTGROUP_EMAIL);
         customerVerifiedRestGroup.setEnabled(true);
         customerVerifiedRestGroup.setExpireDate(null);
         customerVerifiedRestGroup.setFirstName("firstNameCustomer");
         customerVerifiedRestGroup.setLastName("lastNameCustomer");
         customerVerifiedRestGroup.setMiddleName("middleNameCustomer");
         customerVerifiedRestGroup.setPassword("password");
         customerVerifiedRestGroup.setName(CUSTOMER_VERIFIED_RESTGROUP_NAME);
         customerVerifiedRestGroup.setUserGroups(Sets.newHashSet(userGroupRest));
         userRepository.save(customerVerifiedRestGroup);
     }
 
     public void removeData(){
         mongoHelper.removeAllDataFromCollection(User.class);
         mongoHelper.removeAllDataFromCollection(UserGroup.class);
     }
 
     public void setUserRepository(UserRepository userRepository) {
         this.userRepository = userRepository;
     }
 
     public void setAppConfig(AppConfig appConfig) {
         this.appConfig = appConfig;
     }
 }
