 package no.niths.application.rest.school;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 import java.util.GregorianCalendar;
 
 import javax.validation.ConstraintViolationException;
 
 import no.niths.application.rest.battlestation.interfaces.LoanController;
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.school.interfaces.CommitteeController;
 import no.niths.application.rest.school.interfaces.CourseController;
 import no.niths.application.rest.school.interfaces.FeedController;
 import no.niths.application.rest.school.interfaces.StudentController;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.battlestation.Loan;
 import no.niths.domain.school.Committee;
 import no.niths.domain.school.Course;
 import no.niths.domain.school.Feed;
 import no.niths.domain.school.Student;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 public class StudentControllerTest {
 
     private MockHttpServletResponse res;
 
     public static final String EMAIL = "epost@nith.no";
 
     @Autowired
 	private StudentController studController;
 
     @Autowired
 	private CourseController courseController;
 
     @Autowired
 	private CommitteeController committeeController;
 
     @Autowired
 	private FeedController feedController;
 
     @Autowired
 	private LoanController loanController;
 
     @Before
     public void setUp() {
         res = new MockHttpServletResponse();
     }
 
 	@Test(expected= ConstraintViolationException.class)
 	public void testInsertNullObject_shallThrowException() {
 		
 		Student s = new Student();
 		studController.create(s, res);
 	}
 	
 	@Test
 	public void testCreateAndDeleteWithExistingIds(){
 		int size = 0;
 		try {
 			size = studController.getAll(null).size();
 		} catch (ObjectNotFoundException e) {
 			//size = 0
 		}
 		
 		Student s = new Student("mail@mail.com");
 		studController.create(s, res);
 		
 		assertEquals(size + 1, studController.getAll(null).size());
 		
 		studController.delete(s.getId());
 		int currentSize = 0;
 		try {
 			currentSize  = studController.getAll(null).size();
 		} catch (ObjectNotFoundException e) {
 			//size = 0
 		}
 		
 		assertEquals(size, currentSize);
 		
 	}
 	
 	@Test(expected = ObjectNotFoundException.class)
 	public void testDeleteWithUnvalidId(){
 		studController.delete(new Long(9391));
 	}
 	
 	@Test
 	public void testGetMethods(){
 		int size = 0;
 		try {
 			size = studController.getAll(null).size();
 		} catch (ObjectNotFoundException e) {
 			//size = 0
 		}
 		Student s1 = new Student("mail1@mail.com");
 		Student s2 = new Student("mail2@mail.com");
 		Student s3 = new Student("mail3@mail.com");
 		
 		studController.create(s1, res);
 		studController.create(s2, res);
 		studController.create(s3, res);
 		
 		assertEquals(size + 3, studController.getAll(null).size());
 		
 		assertEquals(s1, studController.getById(s1.getId()));
 		
 		assertEquals(1, studController.getAll(s1).size());
 		
 		s1.setEmail("xxx@mail.com");
 		assertEquals("mail1@mail.com", studController.getById(s1.getId()).getEmail());
 		
 		studController.update(s1);
 		assertEquals("xxx@mail.com", studController.getById(s1.getId()).getEmail());
		
		studController.delete(s1.getId());
		studController.delete(s2.getId());
		studController.delete(s3.getId());
 	}
 
     @Test
     public void testCreateAndDeleteOfCourses() {
         Student student = new Student(EMAIL);
         studController.create(student, res);
 
         assertThat(student, is(equalTo(studController.getById(student.getId()))));
 
         Course course = new Course("Programmering", "Litt av hvert");
         Course otherCourse = new Course("Spillprogrammering", "Mye rart");
 
         courseController.create(course, res);
         courseController.create(otherCourse, res);
 
         studController.addCourse(student.getId(), course.getId());
         studController.addCourse(student.getId(), otherCourse.getId());
 
         assertThat(2, is(equalTo(studController.getById(student.getId()).getCourses().size())));
 
         studController.removeCourse(student.getId(), course.getId());
 
         assertThat(1, is(equalTo(studController.getById(student.getId()).getCourses().size())));
         assertThat(courseController.getById(otherCourse.getId()).getId(), is(equalTo(studController.getById(student.getId()).getCourses().get(0).getId())));
 
         studController.delete(student.getId());
         courseController.delete(course.getId());
         courseController.delete(otherCourse.getId());
     }
 
     @Test
     public void testCreateAndDeleteOfCommittees() {
         Student student = new Student(EMAIL);
         studController.create(student, res);
 
         assertThat(student, is(equalTo(studController.getById(student.getId()))));
 
         Committee committee = new Committee("UFF", "Utvalg for Fantastiske fritidssysseler");
         Committee otherCommittee = new Committee("KIT", "Kvinner og IT");
 
         committeeController.create(committee, res);
         committeeController.create(otherCommittee, res);
 
         studController.addCommittee(student.getId(), committee.getId());
         studController.addCommittee(student.getId(), otherCommittee.getId());
 
         assertThat(2, is(equalTo(studController.getById(student.getId()).getCommittees().size())));
 
         studController.removeCommittee(student.getId(), committee.getId());
 
         assertThat(1, is(equalTo(studController.getById(student.getId()).getCommittees().size())));
         assertThat(committeeController.getById(otherCommittee.getId()).getId(), is(equalTo(studController.getById(student.getId()).getCommittees().get(0).getId())));
 
         studController.delete(student.getId());
         committeeController.delete(committee.getId());
         committeeController.delete(otherCommittee.getId());
     }
 
     @Test
     public void testCreateAndDeleteOfFeeds() {
         Student student = new Student(EMAIL);
         studController.create(student, res);
 
         assertThat(student, is(equalTo(studController.getById(student.getId()))));
 
         Feed feed = new Feed("Husk at kroa er åpen");
         Feed otherFeed = new Feed("Minglekveld");
 
         feedController.create(feed, res);
         feedController.create(otherFeed, res);
 
         studController.addFeed(student.getId(), feed.getId());
         studController.addFeed(student.getId(), otherFeed.getId());
 
         assertThat(2, is(equalTo(studController.getById(student.getId()).getFeeds().size())));
 
         studController.removeFeed(student.getId(), feed.getId());
 
         assertThat(1, is(equalTo(studController.getById(student.getId()).getFeeds().size())));
         assertThat(feedController.getById(otherFeed.getId()).getId(), is(equalTo(studController.getById(student.getId()).getFeeds().get(0).getId())));
 
         studController.delete(student.getId());
         feedController.delete(feed.getId());
         feedController.delete(otherFeed.getId());
     }
 
     @Test
     public void testCreateAndDeleteOfConsoles() {
         Student student = new Student(EMAIL);
         studController.create(student, res);
 
         assertThat(student, is(equalTo(studController.getById(student.getId()))));
 
         Loan loan = new Loan(new GregorianCalendar(), new GregorianCalendar());
         Loan otherLoan = new Loan(new GregorianCalendar());
 
         loanController.create(loan, res);
         loanController.create(otherLoan, res);
 
         studController.addLoan(student.getId(), loan.getId());
         studController.addLoan(student.getId(), otherLoan.getId());
 
         assertThat(2, is(equalTo(studController.getById(student.getId()).getLoans().size())));
 
         studController.removeLoan(student.getId(), loan.getId());
 
         assertThat(1, is(equalTo(studController.getById(student.getId()).getLoans().size())));
         assertThat(loanController.getById(otherLoan.getId()).getId(), is(equalTo(studController.getById(student.getId()).getLoans().get(0).getId())));
 
         studController.delete(student.getId());
         loanController.delete(loan.getId());
         loanController.delete(otherLoan.getId());
     }
     
     @Test
     public void testGetStudentWithRoles(){
     	//TODO make test here!
     }
 }
