 package no.niths.infrastructure;
 
 import static org.junit.Assert.*;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.signaling.AccessField;
 import no.niths.infrastructure.interfaces.AccessFieldRepository;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 @Transactional
 public class AccessFieldRepositoryTest {
 
     @Autowired
     private AccessFieldRepository repo;
 
     @Test
     public void testCRUD() {
         final int size = repo.getAll(null).size();
         final Integer maxRange = 2;
 
         // Create
         AccessField accessField1 = new AccessField(1, maxRange);
         repo.create(accessField1);
         assertEquals(size + 1, repo.getAll(null).size());
 
         Long accessField1Id = accessField1.getId();
 
         // Read
         AccessField accessField2 = repo.getById(accessField1Id);
         assertEquals(accessField1Id, accessField2.getId());
 
         // Update
         
         AccessField accessField3 = new AccessField();
         accessField3.setMaxRange(maxRange);
        assertEquals(accessField1, repo.getAll(null).get(0));
 
         // Delete
         assertTrue(repo.delete(accessField1Id));
         assertEquals(size, repo.getAll(null).size());
         assertEquals(0, repo.getAll(accessField3).size());
     }
 }
