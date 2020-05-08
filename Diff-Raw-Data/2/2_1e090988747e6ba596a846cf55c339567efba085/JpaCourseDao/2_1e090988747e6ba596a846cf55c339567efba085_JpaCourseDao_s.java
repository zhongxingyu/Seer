 package com.alwold.classwatch.dao;
 
 import com.alwold.classwatch.model.Course;
 import com.alwold.classwatch.model.Term;
 import com.alwold.classwatch.model.User;
 import com.alwold.classwatch.model.UserCourse;
 import com.alwold.classwatch.model.UserCoursePk;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceException;
 import org.apache.log4j.Logger;
 import org.springframework.orm.jpa.JpaCallback;
 import org.springframework.orm.jpa.support.JpaDaoSupport;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author alwold
  */
 @Transactional
 public class JpaCourseDao extends JpaDaoSupport implements CourseDao {
 	private static Logger logger = Logger.getLogger(JpaCourseDao.class);
 
 	public void saveCourse(Course course) {
 		getJpaTemplate().persist(course);
 	}
 	
 	public List<Course> getCourses(String email) {
		return getJpaTemplate().find("from Course c where c.user.email = ?", email);
 	}
 
 	public void addCourse(final String email, final String termCode, final String courseNumber) {
 		getJpaTemplate().execute(new JpaCallback<Object>(){
 
 			public Object doInJpa(EntityManager em) throws PersistenceException {
 				// load the course
 				// TODO add school
 				String schoolId = "asu";
 				Course course = em.createQuery("from Course c where c.term.pk.termCode = ? and c.term.pk.school.id = ? and c.courseNumber = ?", Course.class)
 						.setParameter(1, termCode)
 						.setParameter(2, schoolId)
 						.setParameter(3, courseNumber)
 						.getSingleResult();
 				if (course == null) {
 					course = new Course();
 					Term term = em.createQuery("from Term t where t.pk.termCode = ? and t.pk.school.id = ?", Term.class)
 							.setParameter(1, termCode)
 							.setParameter(2, schoolId)
 							.getSingleResult();
 					course.setTerm(term);
 					course.setCourseNumber(courseNumber);
 				}
 
 				logger.trace("finding user");
 				User user = (User) em.createQuery("from User u where u.email = ?").setParameter(1, email).getSingleResult();
 				logger.trace("got a user? "+(user != null));
 				UserCourse userCourse = new UserCourse();
 				UserCoursePk userCoursePk = new UserCoursePk();
 				userCoursePk.setUser(user);
 				userCoursePk.setCourse(course);
 				userCourse.setPk(userCoursePk);
 				userCourse.setNotified(false);
 				em.persist(userCourse);
 				return null;
 			}
 		});
 	}
 
 	public void deleteCourse(Long id) {
 		Course course = getJpaTemplate().find(Course.class, id);
 		getJpaTemplate().remove(course);
 	}
 	
 }
