 package de.ifcore.argue.domain.entities;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 
 @Entity
 public class JsonString
 {
 	@Id
 	@GeneratedValue
 	private Long id;
 
 	@Lob
 	@Column(nullable = false, updatable = false)
 	private String jsonString;
 
 	JsonString()
 	{
 	}
 
 	public JsonString(String jsonString)
 	{
 		this.jsonString = jsonString;
 	}
 
 	public Long getId()
 	{
 		return id;
 	}
 
 	public String getJsonString()
 	{
 		return jsonString;
 	}
 }
