 package org.ddelizia.vcrud.core.test.repository;
 
 import org.apache.log4j.Logger;
 import org.ddelizia.vcrud.core.basic.helper.MongoHelper;
 import org.ddelizia.vcrud.core.config.AppConfig;
 import org.ddelizia.vcrud.core.test.util.UserManagmentDataFactory;
 import org.ddelizia.vcrud.core.usermanagement.model.User;
 import org.ddelizia.vcrud.core.usermanagement.model.UserGroup;
 import org.ddelizia.vcrud.core.usermanagement.repository.UserGroupRepository;
 import org.ddelizia.vcrud.core.usermanagement.repository.UserRepository;
 import org.ddelizia.vcrud.test.AbstractJunit4Vcrud;
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ddelizia
  * Date: 24/02/14
  * Time: 22:02
  * To change this template use File | Settings | File Templates.
  */
 public class UserGroupRepositoryTest extends AbstractJunit4Vcrud {
 
     private static final Logger LOGGER = Logger.getLogger(UserRepositoryTest.class);
 
     @Autowired
     private UserRepository userRepository;
 
     @Autowired
     private UserGroupRepository userGroupRepository;
 
     @Autowired
     private UserManagmentDataFactory userManagmentDataFactory;
 
     @Autowired
     private AppConfig appConfig;
 
     @Test
     public void findByGroupName(){
         UserGroup userGroup = userGroupRepository.findByGroupName(
                appConfig.getProperty(AppConfig.USER_USERGROUP_AUTHENTICATED, String.class, null)
             );
         Assert.assertNotNull(userGroup);
        Assert.assertEquals(userGroup.getName(), AppConfig.USER_USERGROUP_AUTHENTICATED);
     }
 
     /**
      * Implement this method instead of @After
      */
     @Override
     public void vcrudAfter() {
         userManagmentDataFactory.removeData();
     }
 
     /**
      * Implement this method instead of @Before
      */
     @Override
     public void vcrudBefore() {
         userManagmentDataFactory.createData();
     }
 }
