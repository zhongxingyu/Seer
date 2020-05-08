 package org.jojen.wikistudy.entity;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.validation.constraints.Max;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 @Entity
 public class Course implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue
 	private Integer id;
 
 
 	@Size(min = 1, max = 50)
 	@NotNull
 	private String name;
 
 
 	@Min(1)
 	@Max(200)
 	private Integer age;
 
 
 	private String description;
 
 	@OneToMany
 	private List<Lesson> lessons;
 
 	public Integer getId() {
 		return id;
 	}
 
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Boolean getHasDraft() {
 		return true;
 	}
 
 	public List<Lesson> getLessons() {
 		if (lessons == null) {
		   lessons = new ArrayList();
 		}
 		return lessons;
 	}
 
 	public void addLessons(Lesson lesson) {
 		if (lessons == null) {
 			lessons = new ArrayList<Lesson>();
 		}
 		if (lesson != null) {
 			lessons.add(lesson);
 		}
 	}
 
 	public Integer getAge() {
 		return age;
 	}
 
 	public void setAge(Integer age) {
 		this.age = age;
 	}
 
 
 	@Override
 	public String toString() {
 		return "Course [id=" + id + ", name=" + name + "]";
 	}
 }
