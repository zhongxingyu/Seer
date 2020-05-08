 package com.rakursy.timetable.model;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.validation.constraints.NotNull;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 public class Room implements Serializable {
 	
 	private static final long serialVersionUID = -1733021797640740886L;
 
 	@Id
 	@GeneratedValue
 	private Long id;
 	
 	@NotNull
 	private Integer number;
 
 	@NotNull
 	private Integer capacity;
 	
 	@NotNull
 	@NotEmpty
 	@ManyToMany
 	private List<Subject> possibleSubjects;
 	
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder()
 				.append(id)
 				.append(number)
 				.append(capacity)
 				.toHashCode();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         } else if (obj instanceof Room) {
         	Room other = (Room) obj;
             return new EqualsBuilder()
                     .append(id, other.id)
                     .append(number, other.number)
                     .append(capacity, other.capacity)
                     .isEquals();
         } else {
             return false;
         }
 	}
 	
 
 	@Override
 	public String toString() {
		return "Room " + number;
 	}
 
 	public Long getId() {
 		return this.id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Integer getNumber() {
 		return this.number;
 	}
 
 	public void setNumber(Integer number) {
 		this.number = number;
 	}
 	
 	public Integer getCapacity() {
 		return this.capacity;
 	}
 
 	public void setCapacity(Integer capacity) {
 		this.capacity = capacity;
 	}
 
 	public List<Subject> getPossibleSubjects() {
 		return possibleSubjects;
 	}
 
 	public void setPossibleSubjects(List<Subject> possibleSubjects) {
 		this.possibleSubjects = possibleSubjects;
 	}
 
 }
