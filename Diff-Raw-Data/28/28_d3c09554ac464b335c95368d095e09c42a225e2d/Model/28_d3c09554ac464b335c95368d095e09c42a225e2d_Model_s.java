 package model.common;
 
 import java.io.Serializable;
 import java.lang.Comparable;
 
 import model.item.ItemVault;
 import model.product.ProductVault;
 import model.productcontainer.ProductGroupVault;
 import model.productcontainer.StorageUnitVault;
 import model.reports.Ivisitor;
 import common.Result;
 
 /**
  * The base class for the basic data models.
  */
 public abstract class Model implements IModel, Serializable, Comparable<IModel>{
 	
 
 	public transient  ItemVault _itemVault = ItemVault.getInstance();
 	public transient  ProductVault _productVault = ProductVault.getInstance();
 	public transient  StorageUnitVault _storageUnitVault = StorageUnitVault.getInstance();
 	public transient  ProductGroupVault _productGroupVault = ProductGroupVault.getInstance();
 	
 	
 	/**
      * When a change is made to the data it becomes invalid and 
      * must be validated before it can be saved.
      * _valid maintains this state
      */
     protected boolean _valid;
     protected boolean _deleted;
 
     protected int _id;
     /**
      * _saved maintaines the state of if the instance of the model is the same as the 
      * persisted model in the vault.
      */
     protected boolean _saved;
     
 	/**
 	 * Constructor
 	 */
 	public Model(){
 		super();
 	}
 
     /**
      * Return the ID of the model.
      */
     public int getId(){
         return _id;
     }
 
     /**
      * Set the ID of the model.
      */
     public Result setId(int id){
         assert true;
         _id = id;
         return new Result(true);
     }
 
     /**
      * Is the model saved?
      */
     public boolean isSaved(){
         return _saved;
     }
 
     /**
      * Is the model deleted?
      */
     public boolean isDeleted(){
         return _deleted;
     }
 
     /**
      * Set the model to saved.
      */
     public Result setSaved(boolean saved){
         _saved = saved;
         return new Result(true);
     }
 
     /**
      * Is the model valid?
      */
     public boolean isValid(){
         return _valid;
     }
 
     /**
      * Set the product's state to valid.
      */
     public Result setValid(boolean valid){
         _valid = valid;
         return new Result(true);
     }
 
 
     /**
      * Put the model into a deleted state.
      */
     public Result delete(){
         if(!isDeleteable().getStatus())
             return new Result(false, "Must be deleteable");
         this._deleted = true;
         this._valid = true;
         return new Result(true);
     }
 
     /**
      * Put the model into an undeleted state.
      */
     public Result unDelete(){
         this._deleted = false;
         this._valid = true;
         this.save();
         return new Result(true);
     }
 
 	/*
      * 
      */
     public boolean getDeleted(){
     	return isDeleted();
     }
 
     /*
      *
      */
    public Result isDeleteable(){
        assert false;
        return new Result(false, "isDeleteable needs to be overridden");
    }
 
     /**
      * Put the Product into an invalid state.
      */
     public void invalidate(){
         _valid = false;
         _saved = false;
     }
 
 	/**
 	 * This function should be overridden by subclasses. If you're calling this directly, you're in
 	 * trouble.
 	 */
 	public Result save(){
 		assert false;
 		return new Result(false, "Saving is not overridden!");
 	}
 
 	/**
 	 * This function should be overridden by subclasses. If you're calling this directly, you're in
 	 * trouble.
 	 */
 	public Result validate(){
 		assert false;
 		return new Result(false, "Validating is not overridden!");
 	}
 	
 	public int compareTo(IModel other) {
 		if (other.getId() < this.getId())
 			return 1;
 		if (other.getId() > this.getId())
 			return -1;
 		return 0;
 	}
 }
