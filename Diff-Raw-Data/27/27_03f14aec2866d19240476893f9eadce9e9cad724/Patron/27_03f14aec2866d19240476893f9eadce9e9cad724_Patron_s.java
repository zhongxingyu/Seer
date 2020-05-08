 /**AUTHOR: Laurence Toal
   *CLASS: CS 230 Data Structures
   *Final Project Phase 2 - Class outlines
   *LAST MODIFIED: 21 November 2011
 **/
 
 import java.util.*;
 
 public class Patron {
     //instance variables
     private String name;
     private String cardNumber;
     private LinkedList<Book> checkedOut;
     private LinkedList<Book> overdue;
     private LinkedList<Book> requested;
   
     //constructor: create a patron given a name & card number
     public Patron(String name, String cardNumber) {
         this.name = name;
         this.cardNumber = cardNumber;
         LinkedList<Book> checkedOut = new LinkedList<Book>();
         LinkedList<Book> overdue = new LinkedList<Book>();
         LinkedList<Book> requested = new LinkedList<Book>();
     }
   
     /**Returns the name of the patron
       *
       *@return name a <code>String</code> representing 
       *the patron's name
       **/
     public String getName() {
         return name;
     }
   
     /**Returns the patron's card number
       *
       *@return cardNumber a <code>long</code> representing 
       *the patron's card number
     **/
     public String getCardNumber() {
         return cardNumber;
     }
   
     /**Returns the books the patron has currently checked out
       *
       *@return checkedOut a <code>LinkedList</code> of Book objects
       *representing the books the patron currently has out
     **/
     public LinkedList<Book> getCheckedOutBooks() {
         return checkedOut;
     }
   
     /**Returns the books the patron currently has overdue
       *
       *@return checkedOut a <code>LinkedList</code> of Book objects
       *representing the books the patron currently has overdue
     **/
     public LinkedList<Book> getOverdueBooks() {
         return overdue;
     }
   
     /**Returns the books the patron has currently requested
       *
       *@return checkedOut a <code>LinkedList</code> of Book objects
       *representing the books the patron currently has requested
     **/
     public LinkedList<Book> getRequestedBooks() {
         return requested;
    } 
}
