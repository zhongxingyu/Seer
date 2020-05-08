 
 package org.bjartek.nullref;
 
 import com.google.common.base.Objects;
 import org.bjartek.nullref.model.Person;
 
 public class PlainJavaNullsafe implements StreetnameProvider {
 
     @Override
     public String streetName(Person person) {
         if (person == null || person.getAddress() == null) {
             return StreetnameProvider.DEFAULT;
         }
        return Objects.firstNonNull(person.getAddress().getStreetName(), StreetnameProvider.DEFAULT);
     }
 }
