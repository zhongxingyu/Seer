 package ar.edu.itba.paw.grupo1.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.hibernate.validator.constraints.Range;
 
 @Entity
 @Table(name = "rooms")
 public class Room {
 
 	@Id
 	@SequenceGenerator(sequenceName = "rooms_seq", name = "rooms_seq")
 	@GeneratedValue(generator = "rooms_seq")
 	private Integer id;
 	
 	@NotNull
 	@Size(min=0, max=50)
 	@Column(nullable = false, length = 50)
 	private String name;
 	
 	@NotNull
 	@Range(min=0, max=Integer.MAX_VALUE)
 	@Column(nullable = false)
 	private double length;
 	
 	@NotNull
 	@Range(min=0, max=Integer.MAX_VALUE)
 	@Column(nullable = false)
 	private double width;
 
 	@ManyToOne
 	private Property property;
 	
 	public Room() {
 	}
 
 	public Room(Integer id, String name, double lenght, double width,
 			Property property) {
 		this.id = id;
 		this.name = name;
 		this.length = lenght;
 		this.width = width;
 		this.property = property;
 	}
 
 	public Room(String name, double lenght, double width, Property property) {
 		this.name = name;
 		this.length = lenght;
 		this.width = width;
 		this.property = property;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public double getLength() {
 		return length;
 	}
 
 	public double getWidth() {
 		return width;
 	}
 
 	public Property getProperty() {
 		return property;
 	}
 	
 }
