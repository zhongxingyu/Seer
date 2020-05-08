 package ua.in.dmitry404.money;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Class that describe money box with ability to deposit and withdraw money
  *
  * @author Dmitriy Butakov
  */
 public class MoneyBox {
     private Map<String, NotesHolder> money = new HashMap<String, NotesHolder>();
 
     /**
      * Deposit banknotes with specified currency, value and quantity
      *
      * @param currencyCode currency code
      * @param value banknote value
      * @param quantity quantity of banknotes
      */
     public void deposit(String currencyCode, int value, int quantity) {
         NotesHolder notesHolder = money.containsKey(currencyCode) ?
                 money.get(currencyCode) : new NotesHolder();
 
         notesHolder.add(value, quantity);
         
         money.put(currencyCode, notesHolder);
     }
 
     /**
      * Withdraw banknotes with specified currency
      *
      * @param currencyCode currency code
      * @param amount amount that will be withdrawn
      * @return NotesHolder instance if operation was successful, null otherwise
      */
     public NotesHolder withdraw(String currencyCode, int amount) {
         if (!exists(currencyCode)) {
             return null;
         }
         
         NotesHolder copyOfCurrentNotesHolder = money.get(currencyCode).clone();
         NotesHolder newNotesHolder = new NotesHolder();
 
        for (int banknotesValue : copyOfCurrentNotesHolder.getValues()) {
             while (amount >= banknotesValue && !copyOfCurrentNotesHolder.isEmpty()) {
                 copyOfCurrentNotesHolder.pop(banknotesValue, 1);
                 newNotesHolder.add(banknotesValue, 1);
 
                 amount -= banknotesValue;
             }
         }
 
         if (amount == 0) {
             if (copyOfCurrentNotesHolder.isEmpty()) {
                 money.remove(currencyCode);
             } else {
                 money.put(currencyCode, copyOfCurrentNotesHolder);
             }
 
             return newNotesHolder;
         } else {
             return null;
         }
     }
 
     /**
      * Check of existing NotesHolder with specified currency code
      *
      * @param currencyCode currency code
      * @return true if NotesHolder with specified currency code, false otherwise
      */
     public boolean exists(String currencyCode) {
         return money.containsKey(currencyCode);
     }
 
     /**
      * Return available currency values in money box
      *
      * @return Set of available currencies
      */
     public Set<String> getValues() {
         return money.keySet();
     }
 
     /**
      * Return copy of existing NotesHolder by specified currency code
      *
      * @param currencyCode currency code
      * @return copy of existing NotesHolder related to specified currency code
      */
     public NotesHolder getNotesHolder(String currencyCode) {
         return money.get(currencyCode).clone();
     }
 }
