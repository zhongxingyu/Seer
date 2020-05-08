 package org.bpaye1.research.model;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 @Table(name="PLAYER")
 public class Player {
 	
 	@Id
 	@GeneratedValue
 	@Column(name="ID")
 	private Long id;
 	
 	@NotEmpty(message="Last Name is required.")
 	@Column(name="LAST_NAME")
 	private String lastName;
 	
 	@NotEmpty(message="First Name is required.")
 	@Column(name="FIRST_NAME")
 	private String firstName;
 	
	@Pattern(regexp="[A-Za-z0-9._%=-]+@[A-za-z0-9.-]+\\.[A-Za-z]{2,4}", message="Invalid email address")
 	@Column(name="EMAIL")
 	private String email;
 	
	@NotNull
     @Temporal(value=TemporalType.DATE)
 	@Column(name="DATE_OF_BIRTH")
 	private Date dateOfBirth;
 	
 	@Embedded
 	private Address address;
 	
 	@Column(name="PHONE_NUMBER")
 	private String phoneNumber;
 	
 	@NotNull(message="Jersey Number is required.")
 	@Column(name="JERSEY_NUMBER")
 	private Integer jerseyNumber;
 	
 	@Column(name="HOME_TOWN")
 	private String homeTown;
 	
 	@Enumerated(EnumType.STRING)
 	@Column(name="POSITION")
 	private Position position;
 	
 	@Enumerated(EnumType.STRING)
 	@Column(name="STATUS")
 	private Status status;
 	
 	public Player(){
 		this.status = Status.ACTIVE;
 	}
 	
 	public Player(String lastName, String firstName, Date dateOfBirth,
 			Integer jerseyNumber) {
 		this.lastName = lastName;
 		this.firstName = firstName;
 		this.dateOfBirth = dateOfBirth;
 		this.jerseyNumber = jerseyNumber;
 		this.status = Status.ACTIVE;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 	
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 	
 	public String getFirstName() {
 		return firstName;
 	}
 	
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public Date getDateOfBirth() {
 		return dateOfBirth;
 	}
 	
 	public void setDateOfBirth(Date dateOfBirth) {
 		this.dateOfBirth = dateOfBirth;
 	}
 	
 	public Address getAddress() {
 		return address;
 	}
 	
 	public void setAddress(Address address) {
 		this.address = address;
 	}
 	
 	public String getPhoneNumber() {
 		return phoneNumber;
 	}
 
 	public void setPhoneNumber(String phoneNumber) {
 		this.phoneNumber = phoneNumber;
 	}
 
 	public Integer getJerseyNumber() {
 		return jerseyNumber;
 	}
 	
 	public void setJerseyNumber(Integer jerseyNumber) {
 		this.jerseyNumber = jerseyNumber;
 	}
 	
 	public Status getStatus() {
 		return status;
 	}
 	
 	public void setStatus(Status status) {
 		this.status = status;
 	}
 	
 	public String getHomeTown() {
 		return homeTown;
 	}
 
 	public void setHomeTown(String homeTown) {
 		this.homeTown = homeTown;
 	}
 	
 	public Position getPosition() {
 		return position;
 	}
 
 	public void setPosition(Position position) {
 		this.position = position;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((dateOfBirth == null) ? 0 : dateOfBirth.hashCode());
 		result = prime * result
 				+ ((firstName == null) ? 0 : firstName.hashCode());
 		result = prime * result
 				+ ((jerseyNumber == null) ? 0 : jerseyNumber.hashCode());
 		result = prime * result
 				+ ((lastName == null) ? 0 : lastName.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Player other = (Player) obj;
 		if (dateOfBirth == null) {
 			if (other.dateOfBirth != null)
 				return false;
 		} else if (!dateOfBirth.equals(other.dateOfBirth))
 			return false;
 		if (firstName == null) {
 			if (other.firstName != null)
 				return false;
 		} else if (!firstName.equals(other.firstName))
 			return false;
 		if (jerseyNumber == null) {
 			if (other.jerseyNumber != null)
 				return false;
 		} else if (!jerseyNumber.equals(other.jerseyNumber))
 			return false;
 		if (lastName == null) {
 			if (other.lastName != null)
 				return false;
 		} else if (!lastName.equals(other.lastName))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Player [id=" + id + ", lastName=" + lastName + ", firstName="
 				+ firstName + ", dateOfBirth=" + dateOfBirth
 				+ ", jerseyNumber=" + jerseyNumber + "]";
 	}
 }
