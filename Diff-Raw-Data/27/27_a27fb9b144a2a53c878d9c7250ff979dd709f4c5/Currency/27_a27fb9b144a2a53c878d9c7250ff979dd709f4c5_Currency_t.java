 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package exchangeRate;
 
import java.util.regex.Pattern;

 /**
  *
  * @author ronald
  */
 public final class Currency {
     
     private final String name;
     
     public Currency(String name) throws CurrencyException{
         if (name == null) throw new CurrencyException("currency name cannot be null");
        if (name.length() != 3) throw new CurrencyException("currency name must be 3 characters long");
        Pattern validName = Pattern.compile("[a-zA-Z]{3}");
        if (!validName.matcher(name).matches()) throw new CurrencyException("currency name must be 3 [A-Z] characters long");
         
        this.name = name.toUpperCase();
     }
     
     public final String getName(){
         return name;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Currency other = (Currency) obj;
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
         return hash;
     }
     
   
    
     
 }
