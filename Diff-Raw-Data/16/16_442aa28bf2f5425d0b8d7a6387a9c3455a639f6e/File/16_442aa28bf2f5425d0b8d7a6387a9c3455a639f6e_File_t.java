 package ru.korpse.springtestapp.entities;
 
 import java.sql.Blob;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.Table;
 
 import lombok.Data;
 
import com.fasterxml.jackson.annotation.JsonIgnore;

 @Data
 @Entity
 @Table(name = "files")
 public class File {
 	
 	@Id
 	@Column(name = "id")
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private long id;
 	
 	@Column(name = "name")
 	private String name;
 	
 	@Column(name = "contentType")
 	private String contentType;
 	
 	@JsonIgnore
 	@Column(name="data")
     @Lob
     private Blob data;
 }
