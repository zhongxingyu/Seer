 package th.ac.kbu.cs.ExamProject.Domain;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.ProjectionList;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import th.ac.kbu.cs.ExamProject.Entity.Course;
 import th.ac.kbu.cs.ExamProject.Entity.TeacherCourse;
 
 public class CourseComboBoxDomain extends ComboBox{
 
 	public List<HashMap<String,Object>> getCourseComboBoxAdmin(){
 		DetachedCriteria criteria = DetachedCriteria.forClass(Course.class,"course");
 		
 		ProjectionList projectionList = Projections.projectionList();
 		projectionList.add(Projections.property("course.courseId"),"courseId");
 		projectionList.add(Projections.property("course.courseCode"),"courseCode");
 		criteria.setProjection(projectionList);
 		
 		criteria.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
 		
 		return basicFinderService.findByCriteria(criteria);
 	}
 
 	public Object getCourseComboBoxTeacher(String username) {
 		DetachedCriteria criteria = DetachedCriteria.forClass(TeacherCourse.class,"teacherCourse");
 		criteria.createAlias("teacherCourse.course", "course");
 		
 
 		ProjectionList projectionList = Projections.projectionList();
 		projectionList.add(Projections.property("course.courseId"),"courseId");
 		projectionList.add(Projections.property("course.courseCode"),"courseCode");
 		criteria.setProjection(projectionList);
 		
 		criteria.add(Restrictions.eq("teacherCourse.username",username));
 		criteria.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
 
 		return basicFinderService.findByCriteria(criteria);
 	}
 
 }
