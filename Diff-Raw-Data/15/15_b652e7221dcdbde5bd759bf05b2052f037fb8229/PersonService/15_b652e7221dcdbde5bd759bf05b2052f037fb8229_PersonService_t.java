 package de.kacperbak.service;
 
 import de.kacperbak.beans.Address;
 import de.kacperbak.beans.Person;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 /**
  * User: bakka
  * Date: 16.06.13
  */
 public class PersonService implements Serializable {
 
     /**
      * singleton
      */
     private static PersonService service;
 
     /**
      * consts
      */
     private static int PERSON_AMOUNT = 2;
     private static int ADDRESS_AMOUNT = 1;
 
     private ArrayList<Person> persons;
     private ArrayList<Address> addresses;
 
     private PersonService() {
         this.addresses = createDefaultAddresses();
        this.persons = createDefaultPersons();
     }
 
     private ArrayList<Person> createDefaultPersons(){
         ArrayList<Person> result = new ArrayList<Person>();
        result.add(new Person("Kacper", 30, addresses.get(0), 1));
        result.add(new Person("Max", 25, addresses.get(1), 2));
        result.add(new Person("Markus", 50, addresses.get(2), 3));
         return result;
     }
 
     private ArrayList<Address> createDefaultAddresses(){
         ArrayList<Address> result = new ArrayList<Address>();
         result.add(new Address("Augsburg", "46a", 86163, 100));
        result.add(new Address("Kempten", "2", 87443, 101));
        result.add(new Address("Memmingen", "30", 87700, 102));
         return result;
     }
 
     private Person createPerson(int number){
         return new Person("Kacper", 30 + number, createDefaultAddress(), number);
     }
 
     private Address createDefaultAddress(){
         return new Address("Augsburg", "46a", 86163, 0);
     }
 
     public static PersonService getPersonService(){
         if(service == null){
             service = new PersonService();
         }
         return service;
     }
 
     public Person createDefaultPerson(){
         return new Person("Kacper", 30, createDefaultAddress(), 0);
     }
 
     public void addPerson(Person person){
         persons.add(person);
     }
 
     public void addPerson(String name, int age, Address address, int checkNumber){
         persons.add(new Person(name, age, address, checkNumber));
     }
 
     public void addAddress(Address address){
         addresses.add(address);
     }
 
     public Person findPersonByCheckNumber(int checkNumber){
         Person result = null;
         for(Person person : persons){
             if(person.getCheckNumber() == checkNumber){
                 result = person;
             }
         }
         return result;
     }
 
     public ArrayList<Person> getPersons() {
         return persons;
     }
 
     public ArrayList<Address> getAddresses() {
         return addresses;
     }
 
     public boolean removePerson(Person person){
         return persons.remove(person);
     }
 
     public boolean removeAddress(Address address){
         return addresses.remove(address);
     }
 }
