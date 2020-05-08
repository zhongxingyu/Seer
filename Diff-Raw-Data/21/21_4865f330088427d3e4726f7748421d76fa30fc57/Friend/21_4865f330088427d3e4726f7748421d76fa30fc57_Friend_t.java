 package ruffkat.hombucha.model;
 
 import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 
 @Entity
@DiscriminatorValue("friend")
 public class Friend
         extends Source {
 
     @Basic
     public String phone;
 
     @Basic
     public String email;
 
     public Friend() {
         this(null);
     }
 
     public Friend(String name) {
         super(name);
     }
 
     public String getPhone() {
         return phone;
     }
 
     public void setPhone(String phone) {
         this.phone = phone;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Friend)) return false;
         if (!super.equals(o)) return false;
 
         Friend friend = (Friend) o;
 
         if (email != null ? !email.equals(friend.email) : friend.email != null) return false;
         if (phone != null ? !phone.equals(friend.phone) : friend.phone != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = super.hashCode();
         result = 31 * result + (phone != null ? phone.hashCode() : 0);
         result = 31 * result + (email != null ? email.hashCode() : 0);
         return result;
     }
 }
