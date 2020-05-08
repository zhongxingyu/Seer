 package org.yajug.users.domain;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQuery;
 import javax.persistence.Transient;
 
 /**
  * This domain pojo represent a member of the jug.
  * 
  * @author Bertrand Chevrier <bertrand.chevrier@yajug.org>
  */
 @Entity
 @Access(AccessType.FIELD)
 @Inheritance(strategy=InheritanceType.JOINED)
 @NamedQuery(name="Member.findAll", query="select m from Member m")
 public class Member extends DomainObject {
 
 	@Basic private String firstName;
 	@Basic private String lastName;
 	@Basic private String email;
 	@Basic private String company;
 	
 	@ElementCollection(targetClass=Role.class, fetch=FetchType.EAGER)
 	@Enumerated(EnumType.STRING)
 	private List<Role> roles;
 	
 	@ManyToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY)
 	private List<Membership> memberships;
 	
 	@Transient private boolean valid;
 	
 	/**
 	 * Default constructor needed by openjpa.
 	 */
 	public Member() {
 	}
 	
 	/**
 	 * Convenient constructor
 	 * @param firstName
 	 * @param lastName
 	 * @param email
 	 * @param company
 	 * @param roles
 	 */
 	public Member(String firstName, String lastName ,String email, String company, List<Role> roles) {
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.email = email;
 		this.company = company;
 		this.roles = roles;
 	}
 	
 	public Member(String firstName, String lastName ,String email, String company, List<Role> roles ,List<Membership> memberships) {
 		this(firstName, lastName, email, company, roles);
 		this.setMemberships(memberships);
 	}
 	
 	/**
 	 * @return the firstName
 	 */
 	public String getFirstName() {
 		return firstName;
 	}
 	
 	/**
 	 * @param firstName the firstName to set
 	 */
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 	
 	/**
 	 * @return the lastName
 	 */
 	public String getLastName() {
 		return lastName;
 	}
 	
 	/**
 	 * @param lastName the lastName to set
 	 */
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 	
 	/**
 	 * @return the email
 	 */
 	public String getEmail() {
 		return email;
 	}
 	
 	/**
 	 * @param email the email to set
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	
 	/**
 	 * @return the company
 	 */
 	public String getCompany() {
 		return company;
 	}
 	
 	/**
 	 * @param company the company to set
 	 */
 	public void setCompany(String company) {
 		this.company = company;
 	}
 	
 	/**
 	 * @return the role
 	 */
 	public List<Role> getRoles() {
 		return roles;
 	}
 	
 	public void setRole(Role role){
 		if(this.roles == null){
 			this.roles = new ArrayList<Role>();
 		}
 		if(!this.roles.contains(role)){
 			this.roles.add(role);
 		}
 	}
 	
 	/**
 	 * @param role the role to set
 	 */
 	public void setRoles(List<Role> role) {
 		this.roles = role;
 	}
 
 	/**
 	 * @return the memberships
 	 */
 	public List<Membership> getMemberships() {
 		return memberships;
 	}
 	
 	/**
 	 * 
 	 * @param membership
 	 */
 	public void setMembership(Membership membership){
 		if(this.memberships == null){
 			this.memberships = new ArrayList<Membership>();
 		}
 		this.memberships.add(membership);
 		if(Calendar.getInstance().get(Calendar.YEAR) == membership.getYear()){
 			this.valid = true;
 		}
 	}
 
 	/**
 	 * @param memberships the memberships to set
 	 */
 	public void setMemberships(List<Membership> memberships) {
 		this.memberships = memberships;
 		if(memberships != null){
 			this.valid = isValidFor(Calendar.getInstance().get(Calendar.YEAR));
 		}
 	}
 
 	/**
 	 * Get the membership status for the current year.
 	 * 
 	 * @return true if the user is a valid member
 	 */
 	public boolean isValid() {
 		return valid;
 	}
 	
 	/**
 	 * Check if this member instance has 
 	 * a valid membership for the current year.
 	 * 
 	 * @return true if valid
 	 */
 	public boolean checkValidity(){
 		this.valid = isValidFor(Calendar.getInstance().get(Calendar.YEAR));
 		return this.valid;
 	}
 	
 	/**
 	 * Check if this member instance has 
 	 * a valid membership for the specified year.
 	 * 
 	 * @param year the year we check for validity
 	 * @return true if valid
 	 */
 	public boolean isValidFor(int year) {
 		boolean validFor = false;
 		
 		if(this.memberships == null){
 			validFor = false;
 		} else {
 			for(Membership ms : this.memberships){
 				if(ms.getYear() == year){
 					validFor = true;
 					break;
 				}
 			}
 		}
 		return validFor;
 	}
 
 	
 	@Override
 	public String toString() {
 		return "Member [firstName=" + firstName + ", lastName=" + lastName
 				+ ", email=" + email + ", company=" + company + ", roles="
 				+ roles + ", memberships=" + memberships + ", valid=" + valid
 				+ "]";
 	}
 	
 	
 }
