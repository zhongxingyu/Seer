 /*
  * The AssignUsersBean class handles the functionality
  * for associating individual users with courses.
  */
 
 package com.myboard.bean;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 import com.myboard.dao.CourseRoles;
 import com.myboard.dao.CourseRolesDao;
 import com.myboard.dao.CourseUsers;
 import com.myboard.dao.Courses;
 import com.myboard.dao.CoursesDao;
 import com.myboard.dao.Users;
 import com.myboard.dao.UsersDao;
 
 @ManagedBean
 @RequestScoped
 public class AssignUsersBean {
 
 	private Integer courseId;
 	private Integer userId;
 	private Integer courseRoleId;
 
 	private CourseRoles role;
 	private Courses course;
 	private Users user;
 
 	public void assign() {
 		if (this.courseId != null) {
 			this.course = getCoursesObjById();
 		}
 
 		if (this.userId != null) {
 			this.user = getUserObjById();
 		}
 
 		if (this.courseRoleId != null) {
 			this.role = getRoleObjById();
 		}
 		course.getUsers().add(new CourseUsers(role,course,user));
 	}//assign
 
 	private com.myboard.dao.Courses getCoursesObjById() {
 		CoursesDao dao = new CoursesDao();
		return dao.read(courseId.toString());
 	}
 
 	private com.myboard.dao.Users getUserObjById() {
 		UsersDao dao = new UsersDao();
 		return dao.read(this.userId.toString());
 	}
 
 	private com.myboard.dao.CourseRoles getRoleObjById() {
 		CourseRolesDao dao = new CourseRolesDao();
 		return dao.read(this.courseRoleId.toString());
 	}
 
 	public void setCourseId(Integer courseId) {
 		this.courseId = courseId;
 	}
 
 	public Integer getCourseId() {
 		return courseId;
 	}
 
 	public void setUserId(Integer userId) {
 		this.userId = userId;
 	}
 
 	public Integer getUserId() {
 		return userId;
 	}
 
 	public void setCourseRoleId(Integer courseRoleId) {
 		this.courseRoleId = courseRoleId;
 	}
 
 	public Integer getCourseRoleId() {
 		return courseRoleId;
 	}
 
 }// class
 
