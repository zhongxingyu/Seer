 package com.kcjavadev.contactbook;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * 
  * ContactBook is the main entry into the app.  This sets everything up and communicates
  * with the client to get input.
  * 
  * @author scott
  *
  */
 public class ContactBook	{
 	
 
 	//List of Persons
                private List<Person> contacts;
 
 	// Typically want to make constructors public unless you definitely don't want others accessing.
 	public ContactBook(){
 
 		contacts = new ArrayList<Person>();
 	}
 	
 	
     public void addContact(Person person){
           contacts.add(person);
     }
 
     public void printContacts( ) {
         // this is the new way to do a for-loop.  Java converts the Set to an iterator and does the base work for you.
         for ( Person person : contacts ) {
             System.out.println( person.getName() + " : " + person.getAddress() );
         }
     }
 
     /**
      * Main (currently only) way into the application.  
      * @param args
      */
 	public static void main( String[] args ) {
         System.out.println( "Starting application" );
         
         // initialize a new class to work with
        ContactBook contacBook = new ContactBook();
         
         System.out.println( "Creating initial contact book" );
 
         Person person = new Person;
         person.setName("Jo Blow");
         person.setCity("JoVille");
         person.SetState("CA");
 
         contactBook.addContact(person);
 
         person.setName("Suzie Smiles");
         person.setCity("Happy Town");
         person.SetState("TX");
 
         contactBook.addContact(person);
         
         // now print the entries
         contactBook.printContacts( );
     	}
     
    
 }
