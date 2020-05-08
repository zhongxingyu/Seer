 package org.oaky.repository;
 
 import org.junit.Before;
 import org.oaky.repository.RoleRepository;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.AfterTransaction;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.sql.DataSource;
 import java.io.Serializable;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/integration-test-context.xml")
 @Transactional
 public class RoleRepositoryIT implements Serializable {
 
     @Autowired
 	RoleRepository roleRepository;
 
 //    @PersistenceContext(unitName = "demoPU")
 //    EntityManager entityManager;
 
     @Autowired
     @Qualifier("demoDS")
     DataSource dataSource;
 
     private SimpleJdbcTemplate jdbc;
 
     @Before
     public void setUp() {
        System.out.println("SETUP BEGIN");
         jdbc = new SimpleJdbcTemplate(dataSource);
         jdbc.update("insert into ROLE(role_id,role_name) VALUES(?,?)", 999, "TESTROLE");
//        int recs = jdbc.queryForInt("select count(*) from ROLE");
//        Assert.assertEquals(2, recs);
//        Assert.assertEquals("TESTROLE", roleRepository.getRole(999).getName());
        System.out.println("SETUP END");
     }
     
     @Test
     public void should_be_emtpy() throws Exception {
        System.out.println("TEST BEGIN");
        int recs = jdbc.queryForInt("select count(*) from ROLE");
        Assert.assertEquals(2, recs);
        System.out.println("TEST JDBC DONE");
        try {
            Assert.assertEquals("TESTROLE", roleRepository.getRole(999).getName());
        } catch (Exception e) {
            System.out.println("TEST FAIL:");
            e.printStackTrace();
            throw e;
        } finally {
            System.out.println("TEST END");
        }
     }
 
     @AfterTransaction
     public void afterTransaction() {
         int recs = jdbc.queryForInt("select count(*) from ROLE");
         Assert.assertEquals(1, recs);
     }
 }
