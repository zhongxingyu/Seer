 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dupa.events;
 
 import dupa.Duplicate;
 import java.util.EventObject;
 
 /**
  *
  * @author glumoff
  */
 public class HashDuplicateFoundEvent extends EventObject {
 
   private Duplicate dup;
 
   public Duplicate getDup() {
     return dup;
   }
   
   public HashDuplicateFoundEvent(Object source, Duplicate dup) {
     super(source);
     this.dup = dup;
   }
 }
