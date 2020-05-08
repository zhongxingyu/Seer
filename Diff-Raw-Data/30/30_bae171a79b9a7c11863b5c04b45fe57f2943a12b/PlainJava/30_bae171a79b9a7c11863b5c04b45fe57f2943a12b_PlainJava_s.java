 package org.bjartek.nullref;
 
 
 import com.google.common.base.Objects;
 import org.bjartek.nullref.model.Person;
 
 public class PlainJava implements StreetnameProvider {
 
     @Override
     public String streetName(Person person) {
        return Objects.firstNonNull(person.getAddress().getStreetName(), StreetnameProvider.DEFAULT);
     }
 }
