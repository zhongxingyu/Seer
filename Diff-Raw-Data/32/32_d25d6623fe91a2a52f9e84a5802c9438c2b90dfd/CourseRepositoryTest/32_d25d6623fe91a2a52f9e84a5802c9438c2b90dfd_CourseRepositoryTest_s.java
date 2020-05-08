 package no.niths.infrastructure.school;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.school.Course;
 import no.niths.domain.school.Subject;
 import no.niths.infrastructure.school.interfaces.CourseRepository;
 import no.niths.infrastructure.school.interfaces.SubjectRepository;
 
 import org.hibernate.exception.ConstraintViolationException;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes= { TestAppConfig.class, HibernateConfig.class })
 @Transactional
 public class CourseRepositoryTest {
 
     @Autowired
     private CourseRepository courseRepo;
 
     @Autowired
     private SubjectRepository subjectRepo;
 
     @Test
     public void testCreateCourseWithSubjects(){
 
         // Subject 1
         Subject pg111 = new Subject(
                 "Java innføring",
                 "PG111",
                 "Lær java",
                 "10:00",
                 "12:00");
         pg111.setWeekday("Monday");
         subjectRepo.create(pg111);
 
         // Subject 2
         Subject pg211 = new Subject(
                 "Java viderekomne",
                 "PG211",
                 "Lær java",
                 "12:00",
                 "14:00");
         pg211.setWeekday("Monday");
         subjectRepo.create(pg211);
 
         // Add both to a Course
         Course course = new Course("Programmering", "Java and the like");
         course.getSubjects().add(pg111);
         course.getSubjects().add(pg211);
         courseRepo.create(course);
 
         assertEquals(
                 2,
                 courseRepo.getById(course.getId()).getSubjects().size());
     }
 
    
     @Test(expected=ConstraintViolationException.class)
     public void testWhenAddCourseWithSameName(){
         int size = courseRepo.getAll(null).size();
         Course c1 = new Course("XXX", "XXX");
 
         courseRepo.create(c1);
         assertEquals(size + 1, courseRepo.getAll(null).size());
         //Course with same name and grade
         //Should throw ConstraintViolationEx
         Course c2 = new Course("XXX", "XXX");
         courseRepo.create(c2);
     }
     
     
     @Test
     public void whenAddedTopics_CourseShouldHaveThem() {
         int numTopics = subjectRepo.getAll(null).size();
         int numCourses = courseRepo.getAll(null).size();
 
         Course c1 = new Course("Programmering", "Java, c++");
         Subject t1 = new Subject();
         t1.setSubjectCode("PG111");
         c1.getSubjects().add(t1);
         courseRepo.create(c1);
         
         
         assertEquals(numCourses + 1, courseRepo.getAll(null).size());
         assertEquals(numTopics + 1, subjectRepo.getAll(null).size());
         
         Course res = courseRepo.getById(c1.getId());
         int numOfTopics = res.getSubjects().size();
         if(numOfTopics > 0){
             res.getSubjects().remove(0);
             courseRepo.update(res);
             
             assertEquals(numOfTopics - 1, courseRepo.getById(res.getId()).getSubjects().size() );
             assertEquals(numTopics + 1, subjectRepo.getAll(null).size());
         }
         
         
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void whenInsertNull_persistenceShouldFail() {
         courseRepo.create(null);
     }
     
     @Test
     public void whenInsertCourse_CourseShouldBePersisted(){
         int size = courseRepo.getAll(null).size();
         
         Course c = new Course("Name", "Desc");
         courseRepo.create(c);
         
         assertEquals(size + 1, courseRepo.getAll(null).size());
     }
     
     
     
     @Test
     public void getCoursesByName_shouldReturnListWithCourses(){
         int  size = courseRepo.getAll(null).size();
         Course c1 = new Course("One", "oneDesc");
         Course c2 = new Course("Two", "oneDesc");// equal desc
         Course c3 = new Course("Three", "threeDesc");
         Course c4 = new Course("Four", "fourDesc");
         courseRepo.create(c1);
         courseRepo.create(c2);
         courseRepo.create(c3);
         courseRepo.create(c4);
         assertEquals(size + 4, courseRepo.getAll(null).size());
         
         Course mockCourse= new Course();
         mockCourse.setName("One");
         
         assertEquals(1, courseRepo.getAll(mockCourse).size());
         mockCourse.setName("Two");
         assertEquals(1, courseRepo.getAll(mockCourse).size());
         mockCourse.setName("*AASDASD");
         assertEquals(0, courseRepo.getAll(mockCourse).size());
     }
     
     @Test
     public void getCoursesByAttributes_shouldReturnListWithCourses(){
         int  size = courseRepo.getAll(null).size();
         Course c1 = new Course("One", "oneDesc");
         Course c2 = new Course("Two", "oneDesc");// equal desc
         Course c3 = new Course("Three", "threeDesc");
         Course c4 = new Course("Four", "fourDesc");
         courseRepo.create(c1);
         courseRepo.create(c2);
         courseRepo.create(c3);
         courseRepo.create(c4);
         
         assertEquals(size + 4, courseRepo.getAll(null).size());
         
         Course toFind = new Course();
         toFind.setName("One");
         
         List<Course> results = courseRepo.getAll(toFind);
         assertEquals(1, results.size());
         
         toFind = new Course();
         toFind.setDescription("oneDesc");
         results = courseRepo.getAll(toFind);
         assertEquals(2, results.size());
 
         toFind = new Course();
         toFind.setName("x");
         results = courseRepo.getAll(toFind);
         assertEquals(0, results.size());
     }
     
     @Test
     public void testCRUD() {
         int size = courseRepo.getAll(null).size();
         Course c = new Course("Name222", "Desc");
         courseRepo.create(c);
         assertEquals(size + 1, courseRepo.getAll(null).size());
         
         Course c1 = courseRepo.getById(c.getId());
         assertEquals(c1.getId(), c.getId());
         long id = c1.getId();
         
         Course testCourse= new Course();
         testCourse.setName("Name222");
         assertEquals(c1, courseRepo.getAll(testCourse).get(0));
         
         boolean isDeleted = courseRepo.delete(id);
         assertTrue(isDeleted);
         assertEquals(size, courseRepo.getAll(null).size());
         
         assertEquals(0,courseRepo.getAll(testCourse).size());
         
     }
     
     
     @Test
     public void testRead(){
         
         Course courseProg = new Course("Programmering", "programmering er kult");
 
         // adding course
         courseProg.setId(courseRepo.create(courseProg));
     
         assertNotSame(0, courseRepo.getAll(null).size());
         
         // deleting courses
         courseRepo.delete(courseProg.getId());
     }
 }
