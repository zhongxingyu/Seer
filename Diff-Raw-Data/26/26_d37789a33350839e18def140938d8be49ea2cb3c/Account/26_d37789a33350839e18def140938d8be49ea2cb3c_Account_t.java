 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Model;
 
 import java.util.Objects;
 import javax.persistence.CascadeType;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToOne;
 import javax.persistence.Transient;
 
 /**
  * A class representing a User. 
  * 
  * A User is a person with a userName and a password. The username is unique.
  * 
  * @author lisastenberg
  */
 @Entity
 public class Account extends AbstractEntity {
     @OneToOne (cascade = CascadeType.ALL)
     private AbstractPerson person;
     private String userName;
     private String passWord;
     private Boolean activated;
     public Account() {
     }
     
     public Account(AbstractPerson person, String userName, String passWord) {
         this.person = person;
         this.userName = userName;
         this.passWord = passWord;
         this.activated=false;
     }
    public Account(Long id, AbstractPerson person, String userName, String passWord) {
         super(id);
         this.person = person;
         this.userName = userName;
         this.passWord = passWord;
         this.activated=false;
     }
 
     public AbstractPerson getPerson() {
         return person;
     }
 
     public String getUserName() {
         return userName;
     }
 
     public String getPassWord() {
         return passWord;
     }
 
     public void setPerson(Person person) {
         this.person = person;
     }
 
     public void setUserName(String userName) {
         this.userName = userName;
     }
 
     public void setPassWord(String passWord) {
         this.passWord = passWord;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 59 * hash + Objects.hashCode(this.userName);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Account other = (Account) obj;
         if (!Objects.equals(this.userName, other.userName)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "User{" + "person=" + person + ", userName=" + userName + '}';
     }
 
     /**
      * @return the activated
      */
     public Boolean getActivated() {
         return activated;
     }
 
     /**
      * @param activated the activated to set
      */
     public void setActivated(Boolean activated) {
         this.activated = activated;
     }
 }
