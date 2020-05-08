 package org.bjartek.nullref;
 
 import com.google.common.base.Optional;
 import org.bjartek.nullref.gmodel.*;
 
 
 public class MainGuava {
 
 
     public static void main(String[] args) {
 
 
         StreetnameProviderOptional service = new GuavaManual();
 //        StreetnameProviderOptional service = new GuavaIterator();
 
 
         Optional<String> streetName = Optional.of("Lofthusveien");
        Optional<Person> person = Optional.of(new Person(Optional.of(new Address(streetName))));
         System.out.println(service.streetName(person));
 
 
         Optional<Person> noPerson = Optional.absent();
         System.out.println(service.streetName(noPerson));
 
 
         Optional<Address> addr = Optional.absent();
         Optional<Person> noAdr = Optional.of(new Person(addr));
         System.out.println(service.streetName(noAdr));
 
 
         Optional<String> noName = Optional.absent();
         Optional<Address> adrWithNoStreetname = Optional.of(new Address(noName));
         Optional<Person> noStreetname = Optional.of(new Person(adrWithNoStreetname));
         System.out.println(service.streetName(noStreetname));
 
 
     }
 }
