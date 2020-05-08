 package model.common;
 
 import java.io.Serializable;
 
 import model.item.ItemVault;
 import model.product.ProductVault;
 import model.productcontainer.ProductGroupVault;
 import model.productcontainer.StorageUnitVault;
 import common.Result;
 
 /**
  * The base class for the basic data models.
  */
 public abstract class Model implements IModel, Serializable {
 	
 
 	public transient  ItemVault itemVault = ItemVault.getInstance();
 	public transient  ProductVault productVault = ProductVault.getInstance();
 	public transient  StorageUnitVault storageUnitVault = StorageUnitVault.getInstance();
 	public transient  ProductGroupVault productGroupVault = ProductGroupVault.getInstance();
 	
 	
 	/**
      * When a change is made to the data it becomes invalid and 
      * must be validated before it can be saved.
      * _valid maintains this state
      */
     protected boolean _valid;
    protected boolean _deleted;
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
 	 * Is it saved?
 	 */
 	public boolean isSaved(){
 		return this._saved;
 	}
 
 	/**
 	 * Is it valid?
 	 */
 	public boolean isValid(){
 		return this._valid;
 	}
 
 	
 	/*
      * 
      */
     public String getDeleted(){
     	if (this._deleted == false)
     		return "false";
     	else
     		return "true"; 
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
 }
