 package org.bjartek.nullref;
 
 import com.google.common.base.Optional;
 import org.bjartek.nullref.gmodel.Person;
 
 public class GuavaManual implements StreetnameProviderOptional {
 
     @Override
     public String streetName(Optional<Person> p) {
         if (!p.isPresent()) {
             return StreetnameProviderOptional.DEFAULT;
         }
         Person person = p.get();
         if (!person.getAddress().isPresent()) {
             return StreetnameProviderOptional.DEFAULT;
         }
 
        Optional<String> name =    person.getAddress().get().getStreetName();
        return name.or(StreetnameProviderOptional.DEFAULT);
     }
 }
 
