 package jDistsim.core.simulation.simulator.entity;
 
 import java.io.Serializable;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 22.2.13
  * Time: 16:42
  */
 public class Attribute implements Serializable {
 
     public String name;
     public String value;
 
     public Attribute(String name) {
        this(name, "");
     }
 
     public Attribute(String name, String value) {
         this.name = name;
         this.value = value;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     @Override
     public String toString() {
         return getName() + ": " + getValue();
     }
 }
