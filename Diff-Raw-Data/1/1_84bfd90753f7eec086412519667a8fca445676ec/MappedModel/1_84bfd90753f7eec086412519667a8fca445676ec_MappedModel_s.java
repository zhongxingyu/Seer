 package com.me.src.pojo;
 
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 
 import org.hibernate.annotations.GenericGenerator;
 
 @MappedSuperclass
 public abstract class MappedModel {
 	@Id
 	@GeneratedValue(generator = "system-uuid")
 	@GenericGenerator(name = "system-uuid", strategy = "uuid")
 	@Column(name = "id")
 	private String id;
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 }
