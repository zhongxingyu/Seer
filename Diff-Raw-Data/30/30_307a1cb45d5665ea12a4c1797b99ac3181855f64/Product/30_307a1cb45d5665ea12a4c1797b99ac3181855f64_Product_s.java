 package model.product;
 
 import common.Result;
 import model.common.Size;
 import model.item.Item;
 import model.productcontainer.StorageUnit;
 import model.productcontainer.ProductGroup;
 import model.common.Barcode;
 import model.common.Model;
 import model.common.Size.Unit;
 
 import org.joda.time.DateTime;
 
 import java.util.ArrayList;
 
 /**
  * The Product class encapsulates all the funtions and data associated with a "Product".
  * It extends the {@link model.common.Model Model}
  * 	class which contains getters and setters for the various datas.
  */
 
 public class Product extends Model{
 	/**
 	 * A unique ID is associated with every Product once it is presisted to the vault.
 	 * _id is not set by the user, but by the vault when it is saved.
 	 * _id can be -1 if it is new and has not been saved
 	 */
 	private int _id;
 
     private int _storageUnitId;
 
     private int _containerId;
 
     private DateTime _creationDate;
 
     private Barcode _barcode;
 
     private String _description;
 
     private Size _size;
 
     private int _shelfLife;
 
     private int _3MonthSupply;
 
    private boolean _deleted;
 
 	/**
 	 * Constructor
 	 */
 	public Product(){
 		_id = -1;
 		_valid = false;
 		_saved = false;
         _creationDate = new DateTime();
 	}
 
     /**
      * Copy constructor
      */
     public Product(Product p){
         assert p != null;
         _id = p.getId();
         _valid = true;
         _saved = true;
         _deleted = p.isDeleted();
         _storageUnitId = p.getStorageUnitId();
         _containerId = p.getContainerId();
         _creationDate = p.getCreationDate();
         _barcode = p.getBarcode();
         _description = p.getDescription();
         _size = p.getSize();
         _shelfLife = p.getShelfLife();
         _3MonthSupply = p.get3MonthSupply();
         
     }
 
 	/**
 	 * Is the Product saved?
 	 */
 	public boolean isSaved(){
 		return _saved;
 	}
 
     /**
      * Is the Product deleted?
      */
 	public boolean isDeleted(){
 		return _deleted;
 	}
 	
     /**
      * Set the product to saved.
      */
     protected Result setSaved(boolean saved){
         _saved = saved;
         return new Result(true);
     }
 
 	/**
 	 * Is the Product valid?
 	 */
 	public boolean isValid(){
 		return _valid;
 	}
 
     /**
      * Set the product's state to valid.
      */
     protected Result setValid(boolean valid){
         _valid = valid;
         return new Result(true);
     }
 
     /**
      * Return the ID of the product.
      */
     public int getId(){
         return _id;
     }
 
     /**
      * Set the ID of the product. 
      */
     protected Result setId(int id){
         assert true;
         _id = id;
         return new Result(true);
     }
 
     /**
      * Return a copy of the {@link model.productcontainer.StorageUnit
      * StorageUnit} that holds the product.
      */
     public StorageUnit getStorageUnit(){
         if(_storageUnitId < 0)
             return null;
         return storageUnitVault.get(_storageUnitId);
     }
 
     /**
      * Return the ID of the {@link model.productcontainer.StorageUnit
      * StorageUnit} that holds the product. 
      */
     public int getStorageUnitId(){
         return _storageUnitId;
     }
 
     /**
      * Set the ID of the StorageUnit that contains this product.
      */
     public Result setStorageUnitId(int id){
         assert true;
         _storageUnitId = id;
         invalidate();
         return new Result(true);
     }
 
     /**
      * Get a copy of the ProductGroup that holds this Product. If the Product 
      * has no ProductGroup, a null pointer is returned.
      */
     public ProductGroup getContainer(){
         return productGroupVault.get(_containerId);
     }
 
     /**
      * Get the id of the ProductGroup that holds this product. If the Product 
      * is not in a ProductGroup, -1 is returned.
      */
     public int getContainerId(){
         return _containerId;
     }
 
     /**
      * Set the ID for the ProductGroup that holds this Product.
      */
     public Result setContainerId(int id){
         assert true;
         _containerId = id;
         invalidate();
         return new Result(true);
     }
 
     /**
      * Get the creation date of the Product.
      */
     public DateTime getCreationDate(){
         return _creationDate;
     }
 
     /**
      * Set the creation date of the Product.
      */
     public Result setCreationDate(DateTime d){
         assert d != null;
         _creationDate = d;
         invalidate();
         return new Result(true);
     }
 
     /**
      * Get a reference to the Barcode object that belongs to this Product.
      */
     public Barcode getBarcode(){
         return _barcode;
     }
 
     /**
      * Get the string that represents the Barcode number belonging to this 
      * Product.
      */
     public String getBarcodeString(){
     	return _barcode.toString();
     }
 
     /**
      * Assign a Barcode object as this Product's Barcode.
      */
     public Result setBarcode(Barcode b){
         assert b != null;
         _barcode = b;
         invalidate();
         return new Result(true);
     }
 
     /**
      * Get the description of the Product.
      * @Pre the description cannot be empty.
      */
     public String getDescription(){
         return _description;
     }
 
     /**
      * Set the description of the product. 
      */
     public Result setDescription(String d){
         assert d != null;
         if (d.length() == 0){
             return new Result(false, "Description cannon be empty");
         }
         _description = d;
         invalidate();
         return new Result(true);
     }
 
     /**
      * Get a reference to the Size of the Product.
      */
     public Size getSize(){
         return _size;
     }
 
     /**
      * Set the size of the product.
      * @Pre The size cannot be 0.
      * @Pre The size must be a valid Size object.
      */
     public Result setSize(Size u){
         assert u != null;
         if(u.getAmount() == 0){
             return new Result(false, "The size of a product cannot be 0.");
         }
         if(!u.validate().getStatus()){
             return new Result(false, "That's an invlaid size.");
         }
         _size = u;
         invalidate();
         return new Result(true);
     }
 
     /**
      * Return the shelf life of the Product.
      */
     public int getShelfLife(){
         return _shelfLife;
     }
 
     /**
      * Set the shelf life of the Product.
      * @Pre The shelf life must be a non-negative number.
      */
     public Result setShelfLife(int i){
         if (i<0){
             return new Result(false, "The shelf life must be non-negative.");
         }
         _shelfLife = i;
         invalidate();
         return new Result(true);
     }
 
     /**
      * Get the 3 month supply of the Product.
      */
     public int get3MonthSupply(){
         return _3MonthSupply;
     }
 
     /**
      * Set the 3 month supply of the product. 
      * @Pre Must be non-negative.
      */
     public Result set3MonthSupply(int i){
         if (i<0){
             return new Result(false, "The 3 mo. supply must be non-negative.");
         }
         _3MonthSupply = i;
         invalidate();
         return new Result(true);
     }
 
 	/**
 	 * Save the product to its Vault.
      * @Pre Product must be validated in order to be saved.
 	 */
 	public Result save(){
         if(!isValid())
             return new Result(false, "Product must be valid before saving.");
         if(getId() == -1)
             return productVault.saveNew(this);
         else
             return productVault.saveModified(this);
 	}
 
 	/**
 	 * Make sure all parts of the product are valid. Put the Product into a 
      * validated state.
 	 */
 	public Result validate(){
         if (_barcode == null || !_barcode.validate().getStatus()) {
             return new Result(false, "The barcode must be set and valid.");
         }
         if (_description == null || _description.equals("")){
             return new Result(false, "The description cannot be empty.");
         }
         if (_size==null || !_size.validate().getStatus()){
             return new Result(false, "The size is invalid.");
         }
         if (_shelfLife<0){
             return new Result(false, "The shelf life must be non-negative.");
         }
         if (_3MonthSupply<0){
             return new Result(false, "The 3 mo. supply must be non-negative.");
         }
         
         if(getId() == -1)
             return productVault.validateNew(this);
         else
             return productVault.validateModified(this);
 	}
 	
 	/*
 	 * Sets all the product attributes to defaults which
 	 * pass validation.
 	 */
 	public void setToBlankProduct(){
 		_barcode = new Barcode("1");
 		_description = "A Description";
 		this._size = new Size(1,Unit.count);
 	}
 	
     /**
      * Put the Product into a deleted state.
      */
 	public Result delete(){
         if(!isDeleteable().getStatus())
             return new Result(false, "Must be deleteable");
 		this._deleted = true;
 		this._valid = true;
 		return new Result(true);
 	}
 
     /**
      * Put the Product into an undeleted state.
      */
 	public Result unDelete(){
 		this._deleted = false;
 		this._valid = true;
 		this.save();
 		return new Result(true);
 	}
 
     /**
      * Check if the product is void of items, and thus, deletable.
      */
     public Result isDeleteable(){
         ArrayList<Item> items = itemVault.findAll("ProductId = " + _id);
         for(Item item : items){
             if(!item.isDeleted())
                 return new Result(false, "All items must be deleted first");
         }
 
         return new Result(true);
     }
 
     /**
      * Put the Product into an invalid state.
      */
     public void invalidate(){
         _valid = false;
         _saved = false;
     }
 
 	public String getCount() {
 		return this._size.toString();
 	}
 }
