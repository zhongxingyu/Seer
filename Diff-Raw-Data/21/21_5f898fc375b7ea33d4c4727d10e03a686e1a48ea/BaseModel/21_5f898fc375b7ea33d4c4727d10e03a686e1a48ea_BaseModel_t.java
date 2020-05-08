 package model.common;
 
 import common.Result;
 
 import model.item.Item;
 import model.item.ItemVault;
 import model.product.Product;
 import model.product.ProductVault;
 import model.productcontainer.*;
 
 import java.util.ArrayList;
 
 /**
  * This class is the controller for the complicated actions to be done with the
  * model data. Here is where you can do all of your adding, deleting, moving 
  * etc...
  */
 public class BaseModel {
 	public transient  ItemVault itemVault = ItemVault.getInstance();
 	public transient  ProductVault productVault = ProductVault.getInstance();
 	public transient  StorageUnitVault storageUnitVault = StorageUnitVault.getInstance();
 	public transient  ProductGroupVault productGroupVault = ProductGroupVault.getInstance();
 	public VaultPickler vp = new VaultPickler();
 	
     /**
      * Moves an item from one storage unit to another.
      */
 	public Result MoveItem(StorageUnit targetSU, Item item){
         assert targetSU != null;
         assert item != null;
 		AddItem(targetSU, item);
         return new Result(true);
 	}
 
     /**
      * Adds an Item to a StorageUnit and ProductGroup.
      */
 	public Result AddItem(StorageUnit targetSU, Item item){
         assert targetSU != null;
         assert item != null;
         Product targetP = item.getProduct();
         assert targetP != null : "Product should be in vault";
 
         if(targetP.getStorageUnitId() != targetSU.getId()){
         	//Check that a product does not exist already
         	Product existingProduct = this.productVault.find("StorageUnitId = "+targetSU.getId());
         	if(existingProduct==null){
 	            Product p2 = new Product();
 	            p2.set3MonthSupply(targetP.get3MonthSupply());
 	            p2.setBarcode(targetP.getBarcode());
 	            p2.setCreationDate(targetP.getCreationDate());
 	            p2.setDescription(targetP.getDescription());
 	            p2.setShelfLife(targetP.getShelfLife());
 	            p2.setSize(targetP.getSize());
 	
 	            
 	            p2.setStorageUnitId(targetSU.getId());
	            p2.setContainerId(-1);
 	            p2.validate();
 	            p2.save();
 	            targetP = p2;
         	} else 
         		targetP = existingProduct;
             
         }
 
         item.setProductId(targetP.getId());
         item.validate();
         Result r = item.validate();
         if(!r.getStatus())
             return r;
 
         return item.save();
 	}
 
     /**
      * Removes an Item from its StorageUnit and ProductGroup.
      */
     public Result RemoveItem(Item item){
         assert item != null;
 		return item.delete();
 	}
 
     /**
      * Moves a Product from one StorageUnit / ProductGroup to another.
      */
     public Result MoveProduct(StorageUnit targetSU, ProductContainer targetPC,
     Product product){
         assert targetSU != null;
         assert targetPC != null;
         assert product != null;
         
         //Delete the current product so we don't find it
 		ArrayList<Product> products = productVault.findAll("Barcode = " + product.getBarcode());
         Product p = null;
 
         for(Product possibleProduct : products){
             if(possibleProduct.getId() != product.getId() && possibleProduct.getStorageUnitId() == targetSU.getId()){
                 p = possibleProduct;
                 break;
             }
         }
        
         //If there isn't currently a product just change the ID's
         //If there is a product then delete old one and move items
         if(p != null){
         	//For each item in "p" change product Id
             ArrayList<Item> items = itemVault.findAll("ProductId = " + p.getId());
 
             for(Item item : items){
                 item.setProductId(product.getId());
                 item.validate();
                 item.save();
             }
             p.delete();
             p.save();
         }
         //Move the product to a neutral area before moving to the target
         //Helps avoid bugs
         product.setStorageUnitId(-1);
         product.validate();
         product.save();
         
         product.setContainerId(targetPC.getId());
         product.setStorageUnitId(targetSU.getId());
         product.validate();
         product.save();
         return new Result(true);
 	}
 
     /**
      * Deletes a Product from the vaults.
      */
 	public Result DeleteProduct(Product product){
         assert product != null; 
 		if(product.isDeleteable().getStatus())
             product.delete();
         else
             return product.isDeleteable();
         return new Result(true);
 	}
 	
     /**
      * Serializes the data in the vaults to a file on disk.
      */
 	public Result Serialize(){
 		return vp.SerializeMe();
 	}
 
     /**
      * Reads the serialized data from a file on disk into the vaults.
      */
 	public Result Deserialize(){
 		return vp.DeSerializeMe();
 	}
 	
 	/*
 	 * 
 	 */
 }
