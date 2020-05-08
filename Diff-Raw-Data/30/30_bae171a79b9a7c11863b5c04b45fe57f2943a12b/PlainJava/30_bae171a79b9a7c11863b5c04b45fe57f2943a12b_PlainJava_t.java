 package org.bjartek.nullref;
 
 
 import com.google.common.base.Objects;
 import org.bjartek.nullref.model.Person;
 
 public class PlainJava implements StreetnameProvider {
 
     @Override
     public String streetName(Person person) {
        String name = person.getAddress().getStreetName();
        return Objects.firstNonNull(name, StreetnameProvider.DEFAULT);
     }
 }
