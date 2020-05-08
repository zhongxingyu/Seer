 package com.crowdstore.models.store;
 
 import com.crowdstore.models.common.GenericIdentifiableContainer;
 
 /**
  * @author damienriccio
  */
 public class FlatStore extends GenericIdentifiableContainer<StoreIdentity> {
     @Override
     public StoreIdentity getIdentity() {
         return identity;
     }
 }
