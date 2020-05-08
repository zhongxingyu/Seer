 package com.googlecode.qualitas.internal.dao;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.googlecode.qualitas.engines.api.configuration.ProcessStatus;
 import com.googlecode.qualitas.engines.api.configuration.ProcessType;
 import com.googlecode.qualitas.internal.model.Process;
 import com.googlecode.qualitas.internal.model.ProcessBundle;
 import com.googlecode.qualitas.internal.model.Process_;
 import com.googlecode.qualitas.internal.model.User;
 import com.googlecode.qualitas.internal.model.User_;
 
 public class RepositoryTest {
 
     private static EntityManager em;
 
     private static Repository repository;
 
     private static User user;
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         EntityManagerFactory emf = Persistence
                 .createEntityManagerFactory("qualitas-pu-test");
         em = emf.createEntityManager();
 
         repository = new Repository();
         repository.em = em;
     }
 
     @Before
     public void setUp() {
         em.getTransaction().begin();
     }
 
     @After
     public void tearDown() {
         if (em.getTransaction().isActive()) {
             em.getTransaction().commit();
         }
     }
 
     @Test
     public void testPersist() {
         user = new User();
         user.setOpenIDUsername("openIDUsername");
         repository.persist(user);
         Assert.assertNotNull(user.getUserId());
     }
 
     @Test
     public void testMerge() {
         user.setOpenIDUsername("openIDUsername2");
         user = repository.merge(user);
     }
 
     @Test
     public void testFindById() {
         User copy = repository.findById(User.class, user.getUserId());
 
         Assert.assertEquals(user.getOpenIDUsername(), copy.getOpenIDUsername());
     }
 
     @Test
     public void testGetSingleResultBySingularAttribute() {
         User copy = repository.getSingleResultBySingularAttribute(User.class,
                 User_.openIDUsername, user.getOpenIDUsername());
 
         Assert.assertEquals(user.getOpenIDUsername(), copy.getOpenIDUsername());
     }
 
     @Test
     public void testGetResultListBySingularAttribute() {
         List<User> copy = repository.getResultListBySingularAttribute(
                 User.class, User_.openIDUsername, user.getOpenIDUsername());
 
         Assert.assertTrue(copy.size() == 1);
         Assert.assertEquals(user.getOpenIDUsername(), copy.get(0)
                 .getOpenIDUsername());
     }
 
     @Test
     public void testGetResultListBySingularAttributeFK() {
         Process process = new Process();
        process.setUploadedTimestamp(new Date());
         ProcessBundle originalProcessBundle = new ProcessBundle();
         originalProcessBundle.setContents("cpntents".getBytes());
         process.setOriginalProcessBundle(originalProcessBundle);
         process.setProcessType(ProcessType.WS_BPEL_2_0_APACHE_ODE);
         process.setProcessStatus(ProcessStatus.UPLOADED);
 
         process.setUser(user);
 
         repository.persist(process);
 
         em.getTransaction().commit();
 
         List<Process> processes = repository.getResultListBySingularAttribute(
                 com.googlecode.qualitas.internal.model.Process.class,
                 Process_.user, user);
 
         Assert.assertTrue(processes.size() == 1);
         Assert.assertEquals(user, processes.get(0).getUser());
     }
 
 }
