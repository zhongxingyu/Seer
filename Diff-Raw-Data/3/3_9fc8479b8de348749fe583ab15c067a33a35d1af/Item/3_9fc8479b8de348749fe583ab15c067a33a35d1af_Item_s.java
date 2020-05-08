 package model.item;
 
 import model.common.IModel;
 import model.common.Barcode;
 import model.common.Model;
 import model.product.Product;
 import model.product.ProductVault;
 import common.Result;
 
 import org.joda.time.DateTime;
 
 /**
  * The Item class encapsulates all the funtions and data associated with a "Item".
  * It extends the {@link gui.item.ItemData ItemData} 
  * 	class which contains getters and setters for the various datas.
  */
 public class Item extends Model{
 
 		/**
 	 * A unique ID is associated with every Item once it is presisted to the vault.
 	 * _id is not set by the user, but by the vault when it is saved.
 	 * _id can be -1 if it is new and has not been saved
 	 */
 	private int _id;
 	/**
 	 * When a change is made to the data it becomes invalid and 
 	 * must be validated before it can be saved.
 	 * _valid maintains this state
 	 */
 	private boolean _valid;
 
 	/**
 	 * _saved maintaines the state of if the instance of the model is the same as the 
 	 * persisted model in the vault.
 	 */
 	private boolean _saved;
 
     private int _productId;
 
     private Barcode _barcode;
 
     private DateTime _entryDate;
 
     private DateTime _exitDate;
 
     private DateTime _expirationDate;
 
     private boolean _deleted;
 
 	/**
 	 * Constructor
 	 */
 	public Item(){
 		//super();
 		_id = -1;
 		_valid = false;
 		_saved = false;
 	}
 
     /**
      *  Copy Constructor
      */
     public Item(Item i){
         assert i != null;
         _id = i.getId();
         _valid = false;
         _saved = false;
         _productId = i.getProductId();
         _barcode = i.getBarcode();
         _entryDate = i.getEntryDate();
         _exitDate = i.getExitDate();
         _expirationDate = i.getExpirationDate();
     }
 
     /**
      *  Checks if the Item is in a deleted state.
      */
     @Override
     public boolean isDeleted() {
         return _deleted;
     }
 
     /**
      *  Returns the ID of the Item.
      */
     public int getId(){
         return _id;
     }
 
     /**
      *  Sets the ID of the Item.
      */
     protected Result setId(int id){
        assert id != null;
         _id = id;
         return new Result(true);
     }
 
     /**
      *  Stores in the Item the ID of the Product to which the Item belongs.
      */
     public Result setProductId(int id){
        assert != null;
         _productId = id;
         invalidate();
         return new Result(true);
     }
 
     /**
      *  Returns a copy of the Product to which this Item belongs.
      */
     public Product getProduct(){
         return productVault.get(_productId);
     }
 
     /**
      *  Returns the ID of the Product to which this Item belongs.
      */
     public int getProductId(){
         return _productId;
     }
 
     /**
      *  Sets the Barcode instance belonging to this Item.
      */
     public Result setBarcode(Barcode b){
         assert b != null;
         _barcode = b;
         invalidate();
         return new Result(true, "Barcode set successfully.");
     }
 
     /**
      *  Returns the barcode belonging to this Item.
      */
     public Barcode getBarcode(){
         return _barcode;
     }
 
     /**
      *  Sets the entry date for this Item.
      */
     public Result setEntryDate(DateTime d){
         _entryDate = d;
         invalidate();
         return new Result(true);
     }
 
     /**
      *  Returns a reference to the entry date for this Item.
      */
     public DateTime getEntryDate(){
         return _entryDate;
     }
 
     /**
      *  Set the exit date for this Item.
      */
     public Result setExitDate(DateTime d){
         assert d != null;
         _exitDate = d;
         invalidate();
         return new Result(true);
     }
 
     /**
      *  Return the exit date of this Item.
      */
     public DateTime getExitDate(){
         return _exitDate;
     }
 
     /**
      *  Set the expiration date of this Item.
      */
     public Result setExpirationDate(DateTime d){
         assert d != null;
         _expirationDate = d;
         invalidate();
         return new Result(true);
     }
 
     /**
      *  Return the expiration date for the Item.
      */
     public DateTime getExpirationDate(){
         return _expirationDate;
     }
 
 	/**
 	 * Is the Item saved?
 	 */
 	public boolean isSaved(){
 		return this._saved;
 	}
 
     /**
      *  Put the item in a saved state.
      *  @Pre Item must be validated before saving.
      */
     protected Result setSaved(boolean saved){
         if (!isValid()){
             return new Result(false, "Item must be saved first, y'know?");
         }
         _saved = saved;
         return new Result(true);
     }
 
 	/**
 	 * Is the Item valid?
 	 */
 	public boolean isValid(){
 		return this._valid;
 	}
 
     /**
      *  Puts the item in a valid state.
      */
     protected Result setValid(boolean valid){
         _valid = valid;
         return new Result(true);
     }
 
 	/**
 	 * If the Item is valid it is saved into the vault.
 	 */
 	public Result save(){
 		if(!isValid())
             return new Result(false, "Item must be valid before saving.");
         if(getId() == -1)
             return itemVault.saveNew(this);
         else
             return itemVault.saveModified(this);
 	}
 
 	/**
 	 * Validate that the Item is able to be saved into the vault.
 	 */
 	public Result validate(){
         if(getId() == -1)
             return itemVault.validateNew(this);
         else
             return itemVault.validateModified(this);
 	}
 	
     /**
      * Put the item into a deleted state.
      */
 	public Result delete(){
 		this._deleted = true;
 		this._valid = true;
 		this.save();
 		return new Result(true);
 	}
 
     /**
      * Put the item into an un-deleted state.
      */
 	public Result unDelete(){
 		this._deleted = false;
 		this._valid = true;
 		this.save();
 		return new Result(true);
 	}
 
     /**
      * Put the item into an invalid state.
      */
     public void invalidate(){
         _saved = false;
         _valid = false;
     }
     
     /**
      * Return the string of the Barcode for the Product that this Item belongs
      * to.
      */
     public String getProductBarcode(){
     	return this.getProduct().getBarcodeString();
     }
 
     /**
      * Return the description of the Item.
      */
     public String getProductDescription(){
     	return this.getProduct().getDescription();
     }
 
     /**
      * Return the ID of the StorageUnit that holds this Item.
      */
     public String getProductStorageUnitId(){
     	return Integer.toString(this.getProduct().getStorageUnitId());
     }
 }
