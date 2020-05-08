 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.nitrox.formular;
 
 import java.math.BigDecimal;
 
 /**
  *
  * @author Alessandro Lima (alessandrolima@gmail.com)
  */
public class Variable {
     
     private String name;
 
     private BigDecimal value;
     
     public String getName() {
         return name;
     }
     
    public void setName(String name) {
         this.name = name;
     }
 
     public BigDecimal getValue() {
         return value;
     }    
     
    public void setValue(BigDecimal value) {
         this.value = value;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Variable other = (Variable) obj;
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         return true;
     }
 
 }
