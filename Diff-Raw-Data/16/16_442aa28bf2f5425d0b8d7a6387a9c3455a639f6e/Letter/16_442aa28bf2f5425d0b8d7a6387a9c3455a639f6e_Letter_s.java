 package ru.korpse.springtestapp.entities;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import lombok.Data;
 
 @Data
 @Entity
 @Table(name = "letters")
 public class Letter {
 	@Id
 	@Column(name = "id")
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private long id;
 
 	@Column(name = "number")
 	private String number;
	
 	@Column(name = "dt")
 	private Date date;
 	
 	@Column(name = "subject")
 	private String subject;
 	
 	@Column(name = "published")
 	private boolean published;
 	
 	@ManyToOne
 	@JoinColumn(name = "file_id")
 	private File file;
 	
 	@Column(name = "description")
 	private String description;
 }
