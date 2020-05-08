 package edu.course.hibernate;
 
 import java.io.Serializable;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 
 
 @Entity
 public class Course implements Serializable{
 
 	private static final long serialVersionUID = 1L;
 	private Set<Student> students;
 	private Teacher teacher;
 	private String name;
 	
 	public Course() {
 		super();
 	}
 	public Course(Teacher teacher, String name) {
 		super();
 		this.teacher = teacher;
 		this.setName(name);
 	}
 	private Long id;
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@ManyToMany
 	public Set<Student> getStudents() {
 		return students;
 	}
 	public void setStudents(Set<Student> students) {
 		this.students = students;
 	}
 	@ManyToOne
 	public Teacher getTeacher() {
 		return teacher;
 	}
 	public void setTeacher(Teacher teacher) {
 		this.teacher = teacher;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getName() {
 		return name;
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((teacher == null) ? 0 : teacher.hashCode());
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Course other = (Course) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (teacher == null) {
 			if (other.teacher != null)
 				return false;
 		} else if (!teacher.equals(other.teacher))
 			return false;
 		return true;
 	}
 	@Override
 	public String toString() {
		return "Course [students=" + students + ", teacher=" + teacher
 				+ ", name=" + name + ", id=" + id + "]";
 	}
 	
 }
