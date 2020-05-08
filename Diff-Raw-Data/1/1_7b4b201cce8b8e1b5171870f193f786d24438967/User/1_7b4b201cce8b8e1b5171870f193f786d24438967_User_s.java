 package com.forum.domain;
 
 
 import com.forum.service.validation.Age;
 import com.forum.service.validation.PhoneNumber;
 import com.forum.service.validation.UniqueEmail;
 import com.forum.service.validation.UniqueUsername;
 import com.forum.util.Encrypter;
 import org.hibernate.validator.constraints.NotEmpty;
 
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class User implements Serializable {
 
     private Encrypter encrypter;
 
     private int id;
     private Privilege privilege;
 
     @NotEmpty
     @UniqueUsername
     private String username;
 
     @Size(min = 8)
     private String password;
 
     @NotEmpty
     private String name;
 
     @UniqueEmail
     @Pattern(regexp =
             "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)+$",
             message = "Please enter a valid email address.")
     private String email;
 
     @PhoneNumber
     private String phoneNumber;
 
     @NotEmpty
     private String country;
 
     @NotEmpty
     private String gender;
 
     @Age
     private Integer ageRange;
 
     @NotEmpty
     private List<Integer> interests;
 
     private List<Integer> knowledge;
     private Boolean privacy;
 
 
     public User() {
         this.encrypter = new Encrypter();
     }
 
     public User(String username, String password, String name, String email, String phoneNumber,
                 String country, String gender, Integer ageRange, Boolean privacy) throws UnsupportedOperationException {
         this();
         this.username = username;
         setPassword(password);
         this.name = name;
         this.email = email;
         this.phoneNumber = phoneNumber;
         this.country = country;
         this.gender = gender;
         this.ageRange = ageRange;
         this.interests = interests;
         this.knowledge = knowledge;
         this.privacy = privacy;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public Privilege getPrivilege() {
         return privilege;
     }
 
     public void setPrivilege(Privilege privilege) {
         this.privilege = privilege;
     }
 
     public List<Integer> getInterests() {
         if (interests == null)
             return new ArrayList<Integer>();
         return interests;
     }
 
     public List<Integer> getKnowledge() {
         if (knowledge == null)
             return new ArrayList<Integer>();
         return knowledge;
     }
 
     public Boolean getPrivacy() {
         return privacy;
     }
 
     public String getUsername() {
 
         return username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public String getName() {
         return name;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setPassword(String password) throws UnsupportedOperationException, IllegalArgumentException {
         if (password == null) throw new IllegalArgumentException();
         if (password.length() < 8) {
             this.password = "";
         } else
             this.password = encrypter.encryptUsingMd5(password);
     }
 
     public void setUsername(String username) {
         this.username = username.trim();
     }
 
     public void setName(String name) {
         this.name = name.trim();
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public void setPhoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
 
     public void setCountry(String country) {
         this.country = country;
     }
 
     public void setGender(String gender) {
         this.gender = gender;
     }
 
     public void setAgeRange(Integer ageRange) {
         this.ageRange = ageRange;
     }
 
     public void setInterests(List<Integer> interests) {
         this.interests = interests;
     }
 
     public void setKnowledge(List<Integer> knowledge) {
         this.knowledge = knowledge;
     }
 
     public void setPrivacy(Boolean privacy) {
         this.privacy = privacy;
     }
 
     public String getPhoneNumber() {
         return phoneNumber;
 
     }
 
     public String getCountry() {
         return country;
     }
 
     public String getGender() {
         return gender;
     }
 
     public Integer getAgeRange() {
         return ageRange;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         User user = (User) o;
 
         if (ageRange != null ? !ageRange.equals(user.ageRange) : user.ageRange != null) return false;
         if (country != null ? !country.equals(user.country) : user.country != null) return false;
         if (email != null ? !email.equals(user.email) : user.email != null) return false;
         if (gender != null ? !gender.equals(user.gender) : user.gender != null) return false;
         if (interests != null ? !interests.equals(user.interests) : user.interests != null) return false;
         if (knowledge != null ? !knowledge.equals(user.knowledge) : user.knowledge != null) return false;
         if (name != null ? !name.equals(user.name) : user.name != null) return false;
 
         /*
         * The password is not used for assessing equality as the password is re-hashed when reading a user from
         * the database.
         *
         * TODO do not create the hash of the password in the setter but in the UserController class and uncomment code
         */
         //if (password != null ? !password.equals(user.password) : user.password != null) return false;
 
         if (phoneNumber != null ? !phoneNumber.equals(user.phoneNumber) : user.phoneNumber != null) return false;
         if (privacy != null ? !privacy.equals(user.privacy) : user.privacy != null) return false;
         if (username != null ? !username.equals(user.username) : user.username != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = username != null ? username.hashCode() : 0;
 
         /*
         * The password is not used for calculating the hashCode as the password is re-hashed when reading a user from
         * the database.
         *
         * TODO do not create the hash of the password in the setter but in the UserController class and uncomment code
         */
         //result = 31 * result + (password != null ? password.hashCode() : 0);
 
         result = 31 * result + (name != null ? name.hashCode() : 0);
         result = 31 * result + (email != null ? email.hashCode() : 0);
         result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
         result = 31 * result + (country != null ? country.hashCode() : 0);
         result = 31 * result + (gender != null ? gender.hashCode() : 0);
         result = 31 * result + (ageRange != null ? ageRange.hashCode() : 0);
         result = 31 * result + (interests != null ? interests.hashCode() : 0);
         result = 31 * result + (knowledge != null ? knowledge.hashCode() : 0);
         result = 31 * result + (privacy != null ? privacy.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return "User{" +
                 " username='" + username + '\'' +
                 ", password=********" + '\'' +
                 ", name='" + name + '\'' +
                 ", email='" + email + '\'' +
                 ", phoneNumber='" + phoneNumber + '\'' +
                 ", country='" + country + '\'' +
                 ", gender='" + gender + '\'' +
                 ", ageRange=" + ageRange +
                 ", interests=" + interests +
                 ", knowledge=" + knowledge +
                 ", privacy=" + privacy +
                 '}';
     }
 
     public int getExpectedRowCount() {
         return getInterests().size() + getKnowledge().size() + 1;
     }
 }
