 package models.user;
 
 import models.contracts.Contract;
 import models.contracts.JobOrder;
 import models.school.Course;
 import models.school.YearCourse;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import play.data.validation.CheckWith;
 import play.db.jpa.Model;
 import validation.check.NumericCheck;
 
 import javax.persistence.*;
 import java.util.Set;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 @Entity
 @Table(name = "users")
 public class User extends Model {
 
     public long staNumber;
 
     @CheckWith(NumericCheck.class)
     public String idBooster;
 
     public String firstName;
 
     public String lastName;
 
     public String password;
 
     public String street;
 
     public String postalCode;
 
     public String city;
 
     public String siret;
 
     public String rcs;
 
     public boolean active;
 
     public boolean desactivated;
 
     @Enumerated(EnumType.STRING)
     public Profile profile;
 
     @OneToOne(cascade = CascadeType.ALL)
     public Contract contract;
 
     @ManyToMany
     public Set<Course> skills;
 
     @OneToMany(mappedBy = "professor")
     public Set<YearCourse> courses;
 
     @OneToMany(mappedBy = "user")
     public Set<JobOrder> orders;
 
     public User() {
     }
 
     public User(String idBooster, Profile profile) {
         this.idBooster = idBooster;
         this.profile = profile;
     }
 
     public String getOfficialFullName() {
 
         if (StringUtils.isBlank(lastName)) {
             return null;
         }
 
         return lastName + " " + firstName;
     }
 
     public String getFullName() {
 
         if (StringUtils.isBlank(lastName)) {
             return null;
         }
 
         return firstName + " " + lastName;
     }
 
     public String getEmail() {
         return idBooster + "@supinfo.com";
     }
 
     public boolean hasContract() {
         return null != contract;
     }
 
     /**
      * Add 0 before if the user id is in 1 -> 9, in case to have id number like 01, 02, 03, etc...
      */
     public String getFormattedStaNumber() {
 
         if (staNumber < 10) {
             return "0" + staNumber;
         }
 
         return String.valueOf(staNumber);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         if (!super.equals(o)) return false;
 
         User user = (User) o;
 
         if (active != user.active) return false;
         if (desactivated != user.desactivated) return false;
         if (staNumber != user.staNumber) return false;
         if (city != null ? !city.equals(user.city) : user.city != null) return false;
         if (contract != null ? !contract.equals(user.contract) : user.contract != null) return false;
         if (courses != null ? !courses.equals(user.courses) : user.courses != null) return false;
         if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null) return false;
         if (idBooster != null ? !idBooster.equals(user.idBooster) : user.idBooster != null) return false;
         if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null) return false;
         if (orders != null ? !orders.equals(user.orders) : user.orders != null) return false;
         if (password != null ? !password.equals(user.password) : user.password != null) return false;
         if (postalCode != null ? !postalCode.equals(user.postalCode) : user.postalCode != null) return false;
         if (profile != user.profile) return false;
         if (rcs != null ? !rcs.equals(user.rcs) : user.rcs != null) return false;
         if (siret != null ? !siret.equals(user.siret) : user.siret != null) return false;
         if (skills != null ? !skills.equals(user.skills) : user.skills != null) return false;
         if (street != null ? !street.equals(user.street) : user.street != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = super.hashCode();
         result = 31 * result + (int) (staNumber ^ (staNumber >>> 32));
         result = 31 * result + (idBooster != null ? idBooster.hashCode() : 0);
         result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
         result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
         result = 31 * result + (password != null ? password.hashCode() : 0);
         result = 31 * result + (street != null ? street.hashCode() : 0);
         result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
         result = 31 * result + (city != null ? city.hashCode() : 0);
         result = 31 * result + (siret != null ? siret.hashCode() : 0);
         result = 31 * result + (rcs != null ? rcs.hashCode() : 0);
         result = 31 * result + (active ? 1 : 0);
         result = 31 * result + (desactivated ? 1 : 0);
         result = 31 * result + (profile != null ? profile.hashCode() : 0);
         result = 31 * result + (contract != null ? contract.hashCode() : 0);
         result = 31 * result + (skills != null ? skills.hashCode() : 0);
         result = 31 * result + (courses != null ? courses.hashCode() : 0);
         result = 31 * result + (orders != null ? orders.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this)
                .append("staNumber", staNumber)
                 .append("idBooster", idBooster)
                 .append("firstName", firstName)
                 .append("lastName", lastName)
                 .append("password", password)
                .append("street", street)
                .append("postalCode", postalCode)
                .append("city", city)
                 .append("siret", siret)
                .append("rcs", rcs)
                 .append("active", active)
                .append("desactivated", desactivated)
                 .append("profile", profile)
                 .appendSuper(super.toString())
                 .toString();
     }
 }
