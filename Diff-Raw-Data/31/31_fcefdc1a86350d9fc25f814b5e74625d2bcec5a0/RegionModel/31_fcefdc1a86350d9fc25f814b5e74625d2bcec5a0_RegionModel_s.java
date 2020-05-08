 package se.atrosys.birds.model;
 
 import javax.annotation.Generated;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  * TODO write comment
  */
 @Entity
 @Table(name = "REGION")
 public class RegionModel {
 	@Id
	@GeneratedValue
	private int id;
 	private String name;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 }
