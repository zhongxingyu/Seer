 /*
  * Created on May 4, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.camod.domain;
 
 import org.apache.commons.lang.builder.*;
 
 /**
  * @author rajputs
  */
 public class Person extends Party {
 
     private static final long serialVersionUID = 3258795453799404851L;
 
     private String firstName;
     private String middleName;
     private String lastName;
     private String username;
    private Boolean isPrincipleInvestigtor;
 
     /**
      * @return Returns the username.
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * @param username
      *            The username to set.
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * @return Returns the middleName.
      */
     public String getMiddleName() {
         return middleName;
     }
 
     /**
      * @param middleName
      *            The middleName to set.
      */
     public void setMiddleName(String middleName) {
         this.middleName = middleName;
     }
 
     /**
      * @return Returns the firstName.
      */
     public String getFirstName() {
         return firstName;
     }
 
     /**
      * @param firstName
      *            The firstName to set.
      */
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     /**
      * @return Returns the lastName.
      */
     public String getLastName() {
         return lastName;
     }
 
     /**
      * @param lastName
      *            The lastName to set.
      */
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     /**
      * @return Returns the isPrincipleInvestigator flag.
      */
    public Boolean getIsPrincipleInvestigator() {
        return isPrincipleInvestigtor;
     }
 
     /**
      * @param inIsPrincipleInvestigtor
      *            Set's whether or not this user is a PI
      */
    public void setIsPrincipleInvestigator(Boolean inIsPrincipleInvestigtor) {
        this.isPrincipleInvestigtor = inIsPrincipleInvestigtor;
     }
 
     /**
      * @see java.lang.Object#equals(Object)
      */
     public boolean equals(Object object) {
         if (!(object instanceof Person)) {
             return false;
         }
         Person rhs = (Person) object;
         return new EqualsBuilder().appendSuper(super.equals(object)).append(this.username, rhs.username).append(
                 this.middleName, rhs.middleName).append(this.firstName, rhs.firstName).append(this.lastName,
                 rhs.lastName).isEquals();
     }
 
     /**
      * @see java.lang.Object#hashCode()
      */
     public int hashCode() {
         return new HashCodeBuilder(-1768193905, -237999699).appendSuper(super.hashCode()).append(this.username).append(
                 this.middleName).append(this.firstName).append(this.lastName).toHashCode();
     }
 
     /**
      * @see java.lang.Object#toString()
      */
     public String toString() {
         return new ToStringBuilder(this).append("middleName", this.middleName).append("id", this.getId()).append(
                 "lastName", this.lastName).append("username", this.username).append("contactInfoCollection",
                 this.getContactInfoCollection()).append("firstName", this.firstName).append("roleCollection",
                 this.getRoleCollection()).toString();
     }
 }
