 package fr.kevinya.todolist.model;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 @Entity
 public class Task {
 	
 	@Id
 	@GeneratedValue
     private Integer id;
 	private String name;
 	private Integer status;
 	private Integer version;
 	
 	public Task() {
 	}
 	
	public Task(String name, Integer status) {
 		this.name = name;
 		this.status = status;
		this.version = 0;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Integer getStatus() {
 		return status;
 	}
 
 	public void setStatus(Integer status) {
 		this.status = status;
 	}
 
 	public Integer getVersion() {
 		return version;
 	}
 
 	public void setVersion(Integer version) {
 		this.version = version;
 	}
 	
 }
