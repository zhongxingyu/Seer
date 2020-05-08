 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Model;
 
 /**
  *
  * @author Pedro
  */
public abstract class BaseType {
     
     int id;
     
     String description;
 
     static int lastID;
     
     public BaseType(){
         
         }
         
     public BaseType(String description){
         
         if (description == null) {
             throw new IllegalArgumentException();
         }
         // cannot record a negative expense or a zero EUR expense
         if (id == -1) {
             throw new IllegalArgumentException();
         }
         lastID++;
         this.id=lastID;
         this.description=description;
         
     }
 
     public String getDescription() {
         return description;
     }
 
     public int getId() {
         return id;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
    /**
     * Comparação de objectos -> Metodo abstracto que deverá ser implementado nas classes filho
     * 
     * @autor 1110186 & 1110590
     * @param other - Objecto a ser comparado
     * @return True -> Objectos iguais | False -> Objectos diferentes
     */
    @Override
    public abstract boolean equals(Object other);

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + this.id;
        hash = 71 * hash + (this.description != null ? this.description.hashCode() : 0);
        return hash;
    }
     
 }
