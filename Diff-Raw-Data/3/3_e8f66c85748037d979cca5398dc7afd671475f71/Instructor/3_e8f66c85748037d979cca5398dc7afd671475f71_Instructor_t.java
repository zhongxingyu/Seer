 package uk.co.idinetwork.core.model;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.Key;
import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
@PersistenceCapable
 public class Instructor {
 	@PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Key instructorKey;
 	private String forename;
 	private String surname;
 	private String adiCode;
 
 	public Instructor(String forename, String surname, String adiCode) {
 		this.forename = forename;
 		this.surname = surname;
 		this.adiCode = adiCode;
 	}
 	
 	public Key getInstructorKey() {
 		return instructorKey;
 	}
 
 	public void setInstructorKey(Key instructorKey) {
 		this.instructorKey = instructorKey;
 	}
 
 	public String getForename() {
 		return forename;
 	}
 
 	public void setForename(String forename) {
 		this.forename = forename;
 	}
 
 	public String getSurname() {
 		return surname;
 	}
 
 	public void setSurname(String surname) {
 		this.surname = surname;
 	}
 
 	public String getAdiCode() {
 		return adiCode;
 	}
 
 	public void setAdiCode(String adiCode) {
 		this.adiCode = adiCode;
 	}
 }
