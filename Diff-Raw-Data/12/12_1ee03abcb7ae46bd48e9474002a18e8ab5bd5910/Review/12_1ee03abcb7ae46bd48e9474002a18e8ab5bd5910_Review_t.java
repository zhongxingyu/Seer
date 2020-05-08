 package com.drexelexp.review;
 
 import java.sql.Timestamp;
 import java.text.DecimalFormat;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.drexelexp.baseDAO.BaseDAO;
 import com.drexelexp.course.Course;
 import com.drexelexp.course.JdbcCourseDAO;
 import com.drexelexp.professor.JdbcProfessorDAO;
 import com.drexelexp.professor.Professor;
 import com.drexelexp.user.JdbcUserDAO;
 import com.drexelexp.user.User;
 
 /**
  * Model for the Professor object
  * 
  * @author
  * 
  */
 public class Review {
 	private int id;
 	private String text;
 	private float rating;
 	private Timestamp timestamp;
 	private int userId;
 	private User user;
 	private int professorId;
 	private Professor professor;
 	private int courseId;
 	private Course course;
 	
 	public Review() {
 		this.id = -1;
 		this.text = "";
 		this.rating = 0;
 		this.professor = null;
 		this.course = null;
 	}
 
 	public Review(int id, String text, float rating, Timestamp timestamp,
 			int userId, int professorId, int courseId) {
 		this.id = id;
 		this.text = text;
 		this.rating = rating;
 		this.timestamp = timestamp;
 		this.userId = userId;
 		this.professorId = professorId;
 		this.courseId = courseId;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
	public String getContent(){
 		return text;
 	}
	public void setContent(String text) {
 		this.text = text;
 	}
 	public float getRating(){
 		return rating;
 	}
 	public String getRatingString(){
 		return new DecimalFormat("0.0").format(rating);
 	}
 	public void setRating(float rating) {
 		this.rating = rating;
 	}
 	public Timestamp getTimestamp(){
 		return timestamp;
 	}
 	public void setTimestamp(Timestamp timestamp) {
 		this.timestamp = timestamp;
 	}
 	public User getUser(){
 		if(user!=null)
 			return user;
 		
 		ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Module.xml");
 		BaseDAO<User> dao = (JdbcUserDAO) context.getBean("userDAO");
 		
 		user = dao.getById(userId);
 		
 		return user;
 	}
 	public void setUser(User user) {
 		this.user = user;
 	}
 	public Professor getProfessor(){
 		if(professor!=null)
 			return professor;
 		
 		ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Module.xml");
 		BaseDAO<Professor> dao = (JdbcProfessorDAO) context.getBean("professorDAO");
 		
 		professor = dao.getById(professorId);
 		
 		return professor;
 	}
 	public int getProfessorId() {
 		return professorId;
 	}
 	public void setProfessorId(int professorId) {
 		this.professorId=professorId;
 	}
 	public Course getCourse(){
 		if(course!=null)
 			return course;
 		
 		ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Module.xml");
 		BaseDAO<Course> dao = (JdbcCourseDAO) context.getBean("courseDAO");
 		
 		course = dao.getById(courseId);
 		
 		return course;
 	}
 	public int getCourseId(){
 		return courseId;
 	}
 	public void setCourseId(int courseId) {
 		this.courseId=courseId;
 	}
 }
