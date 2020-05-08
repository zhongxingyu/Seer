 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Model;
 
 /**
  *
  * @author dg5s5dfsd5fs5d
  */
 public class ExpenseType extends BaseType {
 
     private String type;
 
     protected ExpenseType() {
     }
 
     public ExpenseType(String description, String type) {
         super(description);
         this.type = type;
     }
 
     /**
      * @return the type
      */
     public String getType() {
         return type;
     }
 
     /**
      * @param type the type to set
      */
     public void setType(String type) {
         this.type = type;
     }
 
    @Override
     public String toString() {
         return type;
     }
 
     /**
      * Comparação de objectos
      *
      * @autor 1110186 & 1110590
      * @param other - Objecto a ser comparado
      * @return True -> Objectos iguais | False -> Objectos diferentes
      */
     @Override
     public boolean equals(Object other) {
         boolean result = false;
 
         if (other instanceof ExpenseType) {
             ExpenseType that = (ExpenseType) other;
            result = (this.description.equalsIgnoreCase(that.description) 
                    && this.type.equalsIgnoreCase(that.type));
         }
 
         return result;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 67 * hash + (this.type != null ? this.type.hashCode() : 0);
         return hash;
     }
 }
