 package com.drexelexp.test;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.drexelexp.baseDAO.BaseDAO;
 import com.drexelexp.course.Course;
 import com.drexelexp.course.JdbcCourseDAO;
 import com.drexelexp.professor.JdbcProfessorDAO;
 import com.drexelexp.professor.Professor;
 import com.drexelexp.subject.JdbcSubjectDAO;
 import com.drexelexp.subject.Subject;
 
 public class ProfessorTest {
 
 	@Test
 	public void createProfessor() throws Exception {
 		Professor professor = new Professor(-1, "Professor Test");
 		
 		ApplicationContext context = 
 	    		new ClassPathXmlApplicationContext("Spring-Module.xml");
 		BaseDAO<Professor> professorDAO = (JdbcProfessorDAO) context.getBean("professorDAO");
 		((JdbcProfessorDAO)professorDAO).insert(professor);
 		
 		Professor result = ((JdbcProfessorDAO)professorDAO).getByName("Professor Test");
 		((JdbcProfessorDAO)professorDAO).delete(result);
 	}
 	
 	@Test
 	public void createProfessorCourse() throws Exception {
 		Professor professor = new Professor(-1, "Professor Test");
 		
 		Subject subject = new Subject(-1, "TEST", "TEST");
 		ApplicationContext context = 
 	    		new ClassPathXmlApplicationContext("Spring-Module.xml");
 		BaseDAO<Subject>subjectDAO = (JdbcSubjectDAO) context.getBean("subjectDAO");
 		((JdbcSubjectDAO)subjectDAO).insert(subject);
 		
 		Subject resultSubject = ((JdbcSubjectDAO)subjectDAO).getByCode("TEST");
 		
 		Course course = new Course(-1, 1, "Test Course", "Blah", resultSubject.getId());
 		
 		BaseDAO<Course>courseDAO = (JdbcCourseDAO) context.getBean("courseDAO");
 		((JdbcCourseDAO)courseDAO).insert(course);
 		
 		Course resultCourse = ((JdbcCourseDAO)courseDAO).getByCode(resultSubject, 1);
 		
 		BaseDAO<Professor> professorDAO = (JdbcProfessorDAO) context.getBean("professorDAO");
 		((JdbcProfessorDAO)professorDAO).insert(professor);
 		
 		Professor resultProfessor = ((JdbcProfessorDAO)professorDAO).getByName("Professor Test");
 		
 		((JdbcProfessorDAO)professorDAO).addProfessorCourse(resultProfessor, resultCourse);
 		
 		List<Professor> getByCourseResults = ((JdbcProfessorDAO)professorDAO).getByCourse(resultCourse);
 		boolean resultFound = false;
 		for(Professor p : getByCourseResults) {
			if(p.getName() == "Professor Test")	{
 				resultFound = true;
 				break;
 			}
 		}
 		assertTrue(resultFound);
 		
 		((JdbcCourseDAO)courseDAO).delete(resultCourse);
 		((JdbcProfessorDAO)professorDAO).delete(resultProfessor);
 		((JdbcSubjectDAO)subjectDAO).delete(resultSubject);
 	}
 }
