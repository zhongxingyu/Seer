 package io.coltrane.auth.repository;
 
 import io.coltrane.auth.domain.Developer;
 import io.coltrane.auth.domain.User;
 import junit.framework.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author nik
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/spring/repositories-context.xml")
 @Transactional
 public class UserRepositoryTest {
 
     @Autowired
     private UserRepository userRepository;
     
     @Autowired
     private DeveloperRepository developerRepository;
     
     @Test
     public void testFindByNickNameAndPasswordHash() {
         Developer developer = DomainHelper.createDeveloper();
         developerRepository.save(developer);
         
        User findedUser = userRepository.findByNickNameAndPasswordHash(developer.getNickName(), 
                 developer.getPasswordHash());
        Assert.assertEquals(developer, findedUser);
     }
 }
