 package com.crowdstore.models.store;
 
 import com.crowdstore.models.common.GenericIdentifiableContainer;
 
 /**
  * @author damienriccio
  */
 public class FlatStore extends GenericIdentifiableContainer<StoreIdentity> {

    public FlatStore(StoreIdentity identity){
        super(identity);
    }

     @Override
     public StoreIdentity getIdentity() {
         return identity;
     }
 }
