 package ro.danix.first.model.domain;
 
 import java.util.regex.Pattern;
 import javax.validation.constraints.Size;
 import lombok.EqualsAndHashCode;
 
 import org.springframework.data.mongodb.core.mapping.Field;
 import org.springframework.util.Assert;
 
 /**
  * Value object to represent email addresses.
  *
  * @author Oliver Gierke
  */
 @EqualsAndHashCode
 public final class EmailAddress {
 
     private static final String EMAIL_REGEX = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 
     private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);
 
     @Field("email")
     private final String value;
 
     /**
      * Creates a new {@link EmailAddress} from the given {@link String}
      * representation.
      *
      * @param emailAddress must not be {@literal null} or empty.
      */
     public EmailAddress(String emailAddress) {
         Assert.isTrue(isValid(emailAddress), "Invalid email address!");
         this.value = emailAddress;
     }
 
     /**
      * Returns whether the given {@link String} is a valid {@link EmailAddress}
      * which means you can safely instantiate the class.
      *
      * @param candidate
      * @return
      */
     public static boolean isValid(String candidate) {
         return candidate == null ? false : PATTERN.matcher(candidate).matches();
     }
 
     @Override
     public String toString() {
         return value;
     }
 }
