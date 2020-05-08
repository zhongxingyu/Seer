 package com.crowdstore.models.store;
 
 import com.crowdstore.models.common.GenericIdentifiable;
 
 /**
  * @author damienriccio
  */
 public class StoreIdentity extends GenericIdentifiable {
     private String name;
 
     public StoreIdentity(Long id) {
         super(id);
     }
 
     public String getName() {
         return name;
     }
 
    public StoreIdentity setName(String name) {
         this.name = name;
        return this;
     }
 }
