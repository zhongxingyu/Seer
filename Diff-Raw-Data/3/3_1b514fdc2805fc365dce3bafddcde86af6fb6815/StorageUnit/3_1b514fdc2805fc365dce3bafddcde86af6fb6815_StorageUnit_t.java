 package model.productcontainer;
 
 import common.Result;
 /**
  * The StorageUnit class encapsulates all the funtions and data associated with a "StorageUnit".
  * It extends the {@link model.productcontainer.ProductContainer ProductContainer} 
  * implements the general functions for StorageUnit and for 
  * {@link ProductGroup ProductGroup}.
  */
 public class StorageUnit extends ProductContainer{
 
 
     /**
      * Constructor.
      */
     public StorageUnit(){
         super();
     }
 
     /**
      * Copy Constructor.
      */
     public StorageUnit(StorageUnit su){
         super(su);
        assert su != null;
     }
 
     /**
      * Uses the {@link model.productcontainer.StorageVault StorageVault} to make
      * sure that the proper data constraints for a StorageUnit instance are met.
      * This function must be called and complete successfully before this
      * instance can be saved into the vault. 
      */
     public Result validate(){
         if(getId() == -1)
             return storageUnitVault.validateNew(this);
         else
             return storageUnitVault.validateModified(this);
     }
 
     /**
      * Puts a copy of this instance into the {@link
      *  model.productcontainer.StorageVault StorageVault} and links it to all of
      *  its relatives. Before saving, an instance must be validated using the
      *  validate() call. */
     public Result save(){
         if(!isValid())
             return new Result(false, "Item must be valid before saving.");
         if(getId() == -1)
             return storageUnitVault.saveNew(this);
         else
             return storageUnitVault.saveModified(this);
     }
 }
