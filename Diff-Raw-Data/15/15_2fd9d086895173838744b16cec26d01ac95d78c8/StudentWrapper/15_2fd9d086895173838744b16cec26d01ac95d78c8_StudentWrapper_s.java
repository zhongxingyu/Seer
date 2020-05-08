 package models.rest;
 
 import static org.apache.commons.lang.Validate.notNull;
 
 import java.util.List;
 
 import models.Student;
 
 public class StudentWrapper {
 
 	private InnerStudent student;
 
 	// Default constructor for Gson
 	public StudentWrapper() {
 	}
 
 	public StudentWrapper(final Student student) {
 		notNull(student, "Student can't be null");
		this.student = new InnerStudent(
				student.getId(), 
				student.fullname, 
				student.phone);
 	}
 
 	public Student getStudent() {
 		notNull(student, "InnerStudent can't be null");
		return new Student(student.name, "", student.phone);
 	}
 
 	public static class InnerStudent {
 
 		private Long id;
 		private String name;
 		private String phone;
 
 		// Default constructor for Gson
 		public InnerStudent() {
 		}
 
		public InnerStudent(final Long id, final String name, final String phone) {
 			this.id = id;
 			this.name = name;
 			this.phone = phone;
 		}
 	}
 }
