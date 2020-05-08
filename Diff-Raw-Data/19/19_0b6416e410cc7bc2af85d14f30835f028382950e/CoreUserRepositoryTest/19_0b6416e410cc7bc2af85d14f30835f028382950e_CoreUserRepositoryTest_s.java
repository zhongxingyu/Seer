 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.hibernate.coreuser.domain;
 
 import org.junit.Before;
 
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.CoreMatchers.*;
 
 import com.globant.katari.hibernate.SpringTestUtils;
 
 public class CoreUserRepositoryTest {
 
   private CoreUserRepository userRepository = null;
 
   /** Creates a sample user named test.
    */
   @Before
  protected void setUp() throws Exception {
     userRepository = (CoreUserRepository) SpringTestUtils.get().getBean(
         "coreuser.userRepository");
     userRepository.getHibernateTemplate().bulkUpdate("delete from CoreUser");
     CoreUser user = new SampleUser("test");
     userRepository.getHibernateTemplate().save(user);
   }
 
   public void testFindUserByName() throws Exception {
     CoreUser user = userRepository.findUserByName("test");
    assertThat(user, is(nullValue()));
     assertThat(user.getName(), is("test"));
   }
 
   public void testFindUser() throws Exception {
     CoreUser user = userRepository.findUserByName("test");
     long id = user.getId();
 
     user = userRepository.findUser(id);
     assertThat(user.getName(), is("test"));
   }
 
   public CoreUserRepository getUserRepository() {
     return userRepository;
   }
 
   public void setUserRepository(final CoreUserRepository repository) {
     userRepository = repository;
   }
 }
 
