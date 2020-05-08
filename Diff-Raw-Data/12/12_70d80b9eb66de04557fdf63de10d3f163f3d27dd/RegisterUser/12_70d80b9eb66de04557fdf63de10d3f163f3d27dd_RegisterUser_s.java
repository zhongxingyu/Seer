 package models;
 
 
 import com.avaje.ebean.validation.Length;
 import com.avaje.ebean.validation.NotNull;
 import com.avaje.ebean.validation.Range;
 import play.data.validation.Constraints;
 
 import javax.persistence.Id;
 
 public class RegisterUser {
     @Id
     @Constraints.Email
     @Constraints.Required
     public String email;
 
     @NotNull
     @Length(min = 7, max = 30)
     // Using only ebean validators causes Play not to validate
     // those constraints.
     @Constraints.Required
     @Constraints.MinLength(value = 7)
     @Constraints.MaxLength(value = 30)
     public String name;
 
     @NotNull
     @Length(max = 60)
     @Constraints.MaxLength(value = 60)
     public String companyName;
 
 
     @Length(max = 50)
     @Constraints.MaxLength(value = 50)
     public String street;
 
     @Length(max = 8)
     @Constraints.MaxLength(value = 8)
     public String postalCode;
 
     @Length(max = 30)
     @Constraints.MaxLength(value = 30)
     public String city;
 
     @NotNull
     @Length(max = 30)
     @Constraints.Required
     @Constraints.MaxLength(value = 30)
     public String phoneNumber;
 
 }
