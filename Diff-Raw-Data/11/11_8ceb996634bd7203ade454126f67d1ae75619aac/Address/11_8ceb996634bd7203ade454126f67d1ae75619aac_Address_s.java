 package ro.danix.first.model.domain;
 
 import lombok.Getter;
 import org.springframework.util.Assert;
 
 /**
  * An address.
  *
  * @author danix
  */
 public class Address {
 
     @Getter
     private final String street, city, state, country, zipCode;
 
     /**
      * Creates a new {@link Address} from the given street, city and country.
      *
      * @param street must not be {@literal null} or empty.
      * @param city must not be {@literal null} or empty.
      * @param country must not be {@literal null} or empty.
      */
     public Address(String street, String city, String state, String country, 
             String zipCode) {
 
         Assert.hasText(street, "Street must not be null or empty!");
         Assert.hasText(city, "City must not be null or empty!");
         Assert.hasText(state, "State must not be null or empty!");
         Assert.hasText(country, "Country must not be null or empty!");
         Assert.hasText(zipCode, "Zip code must not be null or empty!");
 
         this.street = street;
         this.city = city;
         this.state = state;
         this.country = country;
         this.zipCode = zipCode;
     }
 
     /**
      * Returns a copy of the current {@link Address} instance which is a new
      * entity in terms of persistence.
      *
      * @return
      */
     public Address getCopy() {
         return new Address(this.street, this.city, this.state, this.country, this.zipCode);
     }
 }
