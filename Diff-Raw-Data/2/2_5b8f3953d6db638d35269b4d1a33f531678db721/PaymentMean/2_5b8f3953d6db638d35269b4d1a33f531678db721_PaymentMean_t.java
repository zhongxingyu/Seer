 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Model;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 
 /**
  *
  * @author i101068
  */
@Entity
 public class PaymentMean extends BaseType implements Serializable{
 
     private String mean;
 
     public PaymentMean() {
     }
 
     public PaymentMean(String description, String mean) {
         super(description);
         this.mean = mean;
     }
 
     /**
      * @return the mean
      */
     public String getMean() {
         return mean;
     }
 
     /**
      * @param mean the mean to set
      */
     public void setMean(String mean) {
         this.mean = mean;
     }
 
     @Override
     public String toString() {
         return "PaymentMean: " + this.mean + " Description: " + this.description;
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
 
         if (other instanceof PaymentMean) {
             PaymentMean that = (PaymentMean) other;
             result = (this.description.equalsIgnoreCase(that.description) 
                     && this.mean.equalsIgnoreCase(that.mean));
         }
 
         return result;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 83 * hash + (this.mean != null ? this.mean.hashCode() : 0);
         return hash;
     }
 }
