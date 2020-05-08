 package no.niths.infrastructure.school;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.school.Subject;
 import no.niths.infrastructure.school.interfaces.SubjectRepository;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
@Transactional
@TransactionConfiguration(transactionManager = "transactionManager",defaultRollback=true) 
 public class SubjectRepositoryTest {
 
     @Autowired
     private SubjectRepository repo;
 
     @Test(expected = IllegalArgumentException.class)    
     public void whenInsertNull_persistenceShouldFail() {
         repo.create(null);
     }
 
     @Test
     public void whenCreateSubject_subjectShouldBePersisted() {
         int size = repo.getAll(null).size();
 
         Subject subject = new Subject();
         subject.setSubjectCode("PG111");
         repo.create(subject);
 
         assertEquals(size + 1, repo.getAll(null).size());
     }
 
     @Test
     public void whenGetById_SubjectShouldBeReturned() {
         int size = repo.getAll(null).size();
 
         Subject subject = new Subject();
         subject.setName("Java 1");
         subject.setSubjectCode("PG111");
         repo.create(subject);
 
         assertEquals(size + 1, repo.getAll(null).size());
         
         Subject result = repo.getById(subject.getId());
         assertEquals(result, subject);
         
         result = repo.getById(new Long(999));
         assertNull(result);
     }
     
     @Test
     public void whenUpdateSubject_SubjectShouldBeUpdatet() {
         int size = repo.getAll(null).size();
         
         Subject subject = new Subject();
         subject.setName("Java 1");
         subject.setSubjectCode("PG111");
         repo.create(subject);
         
         assertEquals(size + 1, repo.getAll(null).size());
         
         subject.setSubjectCode("PG211");
         repo.update(subject);
         
         assertEquals("PG211", repo.getById(subject.getId()).getSubjectCode());
         
     }
     
     @Test
     public void whenGetAll_allShouldBeReturnedt() {
         int size = repo.getAll(null).size();
         
         Subject subject1 = new Subject();
         subject1.setName("Java 1");
         repo.create(subject1);
         Subject subject2 = new Subject();
         subject2.setName("Java 2");
         repo.create(subject2);
         Subject subject3 = new Subject();
         subject3.setName("Java 3");
         repo.create(subject3);
 
         assertEquals(size + 3, repo.getAll(null).size());
 
         assertEquals(size + 1, repo.getAll(subject1).size());
     }
 }
