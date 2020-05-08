 
 package Model;
 
 /**
  * @change 1110186
  * @author i100905
  */
 public class IncomeType {
     
     private String type;
     private String description;
     
     protected IncomeType() {}
 
     public IncomeType(String type, String description) {
         this.type = type;
         this.description = description;
     }
     
        /**
      * Comparação de objectos
      *
      * @autor 1110186 & 1110590
      * @param other - Objecto a comparar
      * @return True -> Objectos iguais | False -> Objectos diferentes
      */
     @Override
     public boolean equals(Object other) {
         boolean result = false;
 
         if (other instanceof IncomeType) {
             IncomeType that = (IncomeType) other;
             result = (this.description.equalsIgnoreCase(that.description)
                    && this.type.equalsIgnoreCase(that.type));
         }
 
         return result;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
        hash = 59 * hash + (this.type != null ? this.type.hashCode() : 0);
         hash = 59 * hash + (this.description != null ? this.description.hashCode() : 0);
         return hash;
     }
     
 }
 
