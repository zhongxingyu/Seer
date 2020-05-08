 package no.niths.services;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.Student;
 import no.niths.services.interfaces.StudentService;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 public class StudentServiceTest {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(StudentServiceTest.class);
 	
 	@Autowired
 	private StudentService studService;
 	
 //	@Test
 //	@Rollback(true)
 //	public void shouldReturnAllStudentsWithThatName(){
 //		int size = studService.getAllStudents().size();
 //		
 //		Student s1 = new Student("John", "Doe");		
 //		Student s2 = new Student("Vera", "Fine");		
 //		Student s3 = new Student("Vera", "Fine");		
 //		Student s4 = new Student("Vera", "Fine");		
 //		Student s5 = new Student("Vera", "Fine");		
 //		studService.createStudent(s1);
 //		studService.createStudent(s2);
 //		studService.createStudent(s3);
 //		studService.createStudent(s4);
 //		studService.createStudent(s5);
 //		
 //		assertEquals(size + 5, studService.getAllStudents().size());
 //		
 //		String term = "Vera Fine";
 //		
 //		assertEquals(4, studService.getStudentByName(term).size());
 //		
 //		
 //	}
 	
 	
 	@Test
 	@Rollback(true)
 	public void testCRUD(){
 		
 		assertEquals(true, studService.getAll(null).isEmpty());
 		
 		//Testing create
 		Student s = new Student("John", "Doe");		
		Student x = new Student("Vera", "Fine");		
 		studService.create(s);
 		studService.create(x);
 		
 		//Testing get
 		assertEquals(2, studService.getAll(null).size());
 		assertEquals(s, studService.getById(s.getId()));
 		
 		//Testing update
 		s.setEmail("john@doe.com");
 		
		assertEquals(null, studService.getById(s.getId()).getEmail());
 		
 		studService.update(s);
 		assertEquals("john@doe.com", studService.getById(s.getId()).getEmail());
 		
 		
 		//Testing delete
 		boolean isDeleted = studService.delete(s.getId());
 		
 		assertTrue(isDeleted);
 		assertEquals(1, studService.getAll(null).size());
 		
 		
 		
 		assertNull(studService.getById(s.getId()));
 	
 		studService.delete(x.getId());
 		assertEquals(true, studService.getAll(null).isEmpty());
 		
 	}
 
 }
