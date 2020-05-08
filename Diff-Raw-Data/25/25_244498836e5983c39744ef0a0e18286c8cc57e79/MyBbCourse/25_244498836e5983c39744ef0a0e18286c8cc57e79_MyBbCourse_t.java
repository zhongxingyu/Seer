 package ca.ubc.med.blackboard.b2template.data;
 
 /**
  * A data bean to store information about a Course. <br/>
  * Note that we could use a Blackboard object, but we also want to store the user role which is not
  * in the BB Course Object, so we create our own object.  
  * @author rwalker
  *
  */
 public class MyBbCourse {
 	private String courseName;
 	private String courseId;
 	private String description;
 	private String roleIdentifier;  // The BB Identifier for the user's role in this course.
 	private String role;  			// String description of user's course role.
 	
 	public String getCourseName() {
 		return courseName;
 	}
 	public void setCourseName(String courseName) {
 		this.courseName = courseName;
 	}
 	public String getCourseId() {
 		return courseId;
 	}
 	public void setCourseId(String courseId) {
 		this.courseId = courseId;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String courseDesc) {
		    description = courseDesc;
 	}
 	public String getRoleIdentifier() {
 		return roleIdentifier;
 	}
 	public void setRoleIdentifier(String roleIdentifier) {
 		this.roleIdentifier = roleIdentifier;
 	}
 	public String getRole() {
 		return role;
 	}
 	public void setRole(String role) {
 		this.role = role;
 	}
 }
