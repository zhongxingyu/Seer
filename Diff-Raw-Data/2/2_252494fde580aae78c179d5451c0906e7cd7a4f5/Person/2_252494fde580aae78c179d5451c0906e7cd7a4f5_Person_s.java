 package entities;
 
 import java.util.LinkedList;
 
 /**
  * The person class is an abstract class that
  * acts as supertype for student and lecturer
  * 
  * @author Gruppe222 - Rainer
  */
 public abstract class Person {
 	// firstname of the person
 	private String firstName;
 	
 	// lastname of the person
 	private String lastName;
 	
 	// email address of the person
 	private String email;
 	
 	// messagebox of the person
	private LinkedList<Message> lst_messages;
 	
 	/**
 	 * Getter firstname
 	 * @return firstname of the person
 	 */
 	public String getFirstName() {
 		return firstName;
 	}
 	
 	/**
 	 * Setter firstname
 	 * @param firstName of the person to set
 	 */
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 	
 	/**
 	 * Getter lastname
 	 * @return lastname of the person
 	 */
 	public String getLastName() {
 		return lastName;
 	}
 	
 	/**
 	 * Setter lastname
 	 * @param lastName of the person to set
 	 */
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 	
 	/**
 	 * Getter email
 	 * @return email address of the person
 	 */
 	public String getEmail() {
 		return email;
 	}
 	
 	/**
 	 * Setter email
 	 * @param email address of the person to set
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	
 	/**
 	 * Informs the person about changes
 	 * @return informationtext
 	 */
 	public String inform(Message m) {
 		this.lst_messages.add(m);
 		return "Email send to " + this.email;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		
 		result = prime * result + ((email == null) ? 0 : email.hashCode());
 		result = prime * result
 				+ ((firstName == null) ? 0 : firstName.hashCode());
 		result = prime * result
 				+ ((lastName == null) ? 0 : lastName.hashCode());
 		
 		return result;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		
 		if (obj == null)
 			return false;
 		
 		if (!(obj instanceof Person))
 			return false;
 		
 		Person other = (Person) obj;
 		
 		if (email == null) {
 			if (other.email != null)
 				return false;
 		} else if (!email.equals(other.email))
 			return false;
 		
 		if (firstName == null) {
 			if (other.firstName != null)
 				return false;
 		} else if (!firstName.equals(other.firstName))
 			return false;
 		
 		if (lastName == null) {
 			if (other.lastName != null)
 				return false;
 		} else if (!lastName.equals(other.lastName))
 			return false;
 		
 		return true;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return this.firstName + " " + this.lastName
 				+ ", " + this.email;
 	}
 
 }
