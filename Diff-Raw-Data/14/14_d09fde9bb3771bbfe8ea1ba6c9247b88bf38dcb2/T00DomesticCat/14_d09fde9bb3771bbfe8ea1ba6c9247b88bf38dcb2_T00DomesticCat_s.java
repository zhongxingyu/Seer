 package org.maziarz.hbn.example00;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 @Entity
 public class T00DomesticCat implements Cat {
 
 	@Id
 	@GeneratedValue
 	private Long id;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	private String name;
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 }
