 package org.appfuse.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 /**
  * User class
  *
  * This class is used to generate the Struts Validator Form
  * as well as the Hibernate mapping file.
  *
  * <p>
  * <a href="User.java.html"><i>View Source</i></a>
  * </p>
  *
  * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
  *
  * @struts.form include-all="true" extends="BaseForm"
  * @hibernate.class table="app_user"
  */
 public class User extends BaseObject {
     
     protected Long id;
     protected String username;
     protected String password;
     protected String confirmPassword;
     protected String firstName;
     protected String lastName;
     protected Address address = new Address();
     protected String phoneNumber;
     protected String email;
     protected String website;
     protected String passwordHint;
     protected List roles = new ArrayList();
 
     /**
      * Returns the id.
      * @return String
      *
      * @hibernate.id column="id"
      *  generator-class="increment" unsaved-value="null"
      */
     public Long getId() {
         return id;
     }
 
     /**
      * Returns the username.
      * @return String
      *
      * @struts.validator type="required"
      * @hibernate.property
      * @hibernate.column name="username" not-null="true" unique="true"
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * Returns the password.
      * @return String
      *
      * @struts.validator type="required"
      * @struts.validator type="twofields" msgkey="errors.twofields"
      * @struts.validator-args arg1resource="userFormEx.password"
      * @struts.validator-args arg1resource="userFormEx.confirmPassword"
      * @struts.validator-var name="secondProperty" value="confirmPassword"
      * @hibernate.property column="password" not-null="true"
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * Returns the confirmedPassword.
      * @return String
      *
      * @struts.validator type="required"
      */
     public String getConfirmPassword() {
         return confirmPassword;
     }
 
     /**
      * Returns the firstName.
      * @return String
      *
      * @struts.validator type="required"
      * @hibernate.property column="first_name" not-null="true"
      */
     public String getFirstName() {
         return firstName;
     }
 
     /**
      * Returns the lastName.
      * @return String
      *
      * @struts.validator type="required"
      * @hibernate.property column="last_name" not-null="true"
      */
     public String getLastName() {
         return lastName;
     }
 
     public String getFullName() {
         return firstName + ' ' + lastName;
     }
 
     /**
      * Returns the address.  The struts.validator tag is needed on
      * this method in order for child validation rules to be picked up
      * when generating validation.xml.
      *
      * @return Address
      *
      * @hibernate.component
      */
     public Address getAddress() {
         return address;
     }
 
     /**
      * Returns the email.  This is an optional field for specifying a
      * different e-mail than the username.
      * @return String
      *
      * @struts.validator type="required"
      * @struts.validator type="email"
      * @hibernate.property column="email" not-null="false" unique="true"
      */
     public String getEmail() {
         return email;
     }
 
     /**
      * Returns the phoneNumber.
      * @return String
      *
      * @struts.validator type="mask" msgkey="errors.phone"
      * @struts.validator-var name="mask" value="${phone}"
      * @hibernate.property column="phone_number" not-null="false"
      */
     public String getPhoneNumber() {
         return phoneNumber;
     }
 
     /**
      * Returns the website.
      * @return String
      *
      * @struts.validator type="required"
      * @hibernate.property column="website" not-null="false"
      */
     public String getWebsite() {
         return website;
     }
 
     /**
      * Returns the passwordHint.
      * @return String
      *
      * @struts.validator type="required"
      * @hibernate.property column="password_hint" not-null="false"
      */
     public String getPasswordHint() {
         return passwordHint;
     }
 
     /**
      * Returns the user's roles.
      * @return List
      *
      * @hibernate.bag name="roles" inverse="true" lazy="false" cascade="delete"
      * @hibernate.collection-key column="user_id"
      * @hibernate.collection-one-to-many class="org.appfuse.model.UserRole"
      */
     public List getRoles() {
         return roles;
     }
 
     /**
      * Adds a role for the user
      * @param rolename
      */
     public void addRole(String rolename) {
         boolean exists = false;
         if (roles == null) {
             roles = new ArrayList();
         } else {
             // loop through and make sure the rolename doesn't already exist
             for (Iterator it = roles.iterator(); it.hasNext();) {
                UserRole r = (UserRole) it.next();
                if (rolename.equals(r.getRoleName())) {
                    exists = true;
                     break;
                 }
             }
         }
         if (!exists) {
             UserRole role = new UserRole();
             role.setRoleName(rolename);
             role.setUserId(this.id);
             role.setUsername(this.username);
             roles.add(role);
         }
     }
 
     /**
      * Sets the id.
      * @param id The id to set
      */
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * Sets the username.
      * @param username The username to set
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * Sets the password.
      * @param password The password to set
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     /**
      * Sets the confirmedPassword.
      * @param confirmPassword The confirmed password to set
      */
     public void setConfirmPassword(String confirmPassword) {
         this.confirmPassword = confirmPassword;
     }
 
     /**
      * Sets the firstName.
      * @param firstName The firstName to set
      */
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     /**
      * Sets the lastName.
      * @param lastName The lastName to set
      */
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     /**
      * Sets the address.
      * @param address The address to set
      */
     public void setAddress(Address address) {
         this.address = address;
     }
 
     /**
      * Sets the email.
      * @param email The email to set
      */
     public void setEmail(String email) {
         this.email = email;
     }
 
     /**
      * Sets the phoneNumber.
      * @param phoneNumber The phoneNumber to set
      */
     public void setPhoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
 
     /**
      * Sets the website.
      * @param website The website to set
      */
     public void setWebsite(String website) {
         this.website = website;
     }
 
     /**
      * @param passwordHint The password hint to set
      */
     public void setPasswordHint(String passwordHint) {
         this.passwordHint = passwordHint;
     }
 
     /**
      * Sets the roles.
      * @param roles The roles to set
      */
     public void setRoles(List roles) {
         this.roles = roles;
     }
 
     /**
      * Generated using Commonclipse (http://commonclipse.sf.net)
      */
     public boolean equals(Object object) {
         if (!(object instanceof User)) {
             return false;
         }
 
         User rhs = (User) object;
 
         return new EqualsBuilder().append(this.password, rhs.password)
                                   .append(this.passwordHint, rhs.passwordHint)
                                   .append(this.address, rhs.address)
                                   .append(this.confirmPassword,
                                           rhs.confirmPassword)
                                   .append(this.username, rhs.username)
                                   .append(this.email, rhs.email)
                                   .append(this.phoneNumber, rhs.phoneNumber)
                                   .append(this.roles, rhs.roles)
                                   .append(this.website, rhs.website)
                                   .append(this.firstName, rhs.firstName)
                                   .append(this.id, rhs.id)
                                   .append(this.lastName, rhs.lastName).isEquals();
     }
 
     /**
      * Generated using Commonclipse (http://commonclipse.sf.net)
      */
     public int hashCode() {
         return new HashCodeBuilder(-2022315247, 1437659757).append(this.password)
                                                            .append(this.passwordHint)
                                                            .append(this.address)
                                                            .append(this.confirmPassword)
                                                            .append(this.username)
                                                            .append(this.email)
                                                            .append(this.phoneNumber)
                                                            .append(this.roles)
                                                            .append(this.website)
                                                            .append(this.firstName)
                                                            .append(this.id)
                                                            .append(this.lastName)
                                                            .toHashCode();
     }
 }
