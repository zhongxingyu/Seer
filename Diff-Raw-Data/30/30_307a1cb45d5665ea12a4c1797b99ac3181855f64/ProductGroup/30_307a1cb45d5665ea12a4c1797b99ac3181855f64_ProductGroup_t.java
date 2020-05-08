 package model.productcontainer;
 
 import common.Result;
 import model.common.Size;
 import model.item.Item;
 import model.product.Product;
 
 import java.util.ArrayList;
 
 /**
  * The ProductGroup class encapsulates all the funtions and data associated with a "ProductGroup".
  * It extends the {@link model.productcontainer.ProductContainer ProductContainer}
  * 	class which contains getters and setters for the various datas.
  */
 public class ProductGroup extends ProductContainer{
 
 	/* A pointer to the ProductContainer that this group belongs to */
 	private int _parentId;
 
 	/* A variable representing the three month supply for this group. */
 	private Size _3MonthSupply;
	
 	/**
 	 * No-args constructor.
 	 */
 	public ProductGroup(){
 		super();
 		_parentId = -1;
 	}
 
 	/**
 	 * Expects the parent group, and the storage unit that this group belongs
 	 * to. They can be the same value. Also expects some non-negative integer
 	 * for the three-moth supply for this group.
 	 */
 	public ProductGroup(ProductGroup parent, ProductGroup rootParent, 
 	                    Size threeMonthSupply){
 		super();
 		_parentId = parent.getId();
 		_3MonthSupply = threeMonthSupply;
     }
 
     /**
      * Copy constructor.
      */
     public ProductGroup(ProductGroup container) {
         super(container);
         assert container != null;
         _parentId = container.getParentId();
         _3MonthSupply = container.get3MonthSupply();
     }
 
     /**
 	 * Returns the three-month supply for this group as an int. */	
 	public Size get3MonthSupply(){
 		return _3MonthSupply;
 	}
 
 	/**
 	 * Allows a non-zero integer to be set for the three month supply.
 	 */
 	public Result set3MonthSupply(Size value){
         if (!value.validate().getStatus()){
             return new Result(false, "The 3 mo. is invalid.");
         }
 		_3MonthSupply = value;
         invalidate();
         return new Result(true, "Successfully set.");
 	}
 
     /**
      * Returns the parent ID
      */
     public int getParentId(){
         return _parentId;
     }
 
     /**
      * Return the parent ID as a string.
      */
     public String getParentIdString(){
     	return Integer.toString(_parentId);
     }
 
     /**
      * Return a copy of the Parent.
      */
     public ProductContainer getParent(){
         return this.productGroupVault.get(_parentId);
     }
 
     /**
      * Set this to the ID of the desired parent for this ProductGroup.
      */
     public Result setParentId(int id){
         _parentId = id;
         invalidate();
         return new Result(true);
     }
 
     public StorageUnit getStorageUnit(){
     	ProductGroup tempGroup = this;
     	int id=-1;
     	while(tempGroup != null){
     		if(tempGroup.getParent()!=null){
 	    		id = tempGroup.getParent().getId();
 	    		tempGroup = this.productGroupVault.get(id);
     		} else {
     			id = tempGroup.getParentId();
     			tempGroup = null;
     		}
     	}
     	return this.storageUnitVault.get(id);
     	
     }
 
     /**
      * Checks if this is a valid ProductGroup.
      */
     public Result validate(){
         if(getId() == -1)
             return productGroupVault.validateNew(this);
         else
             return productGroupVault.validateModified(this);
     }
 
     /**
      * Saves the ProductGroup to its vault.
      * @Pre The ProductGroup must be validated before it can be saved.
      */
     public Result save(){
         if(!isValid())
             return new Result(false, "Item must be valid before saving.");
         if(getId() == -1)
             return productGroupVault.saveNew(this);
         else
             return productGroupVault.saveModified(this);
     }
 
 }
