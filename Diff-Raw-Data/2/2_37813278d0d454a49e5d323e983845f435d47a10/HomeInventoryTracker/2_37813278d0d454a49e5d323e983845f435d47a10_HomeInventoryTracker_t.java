 package model;
 
 import java.util.Date;
 import java.io.*;
 
 /** HomeInventoryTracker: Home Inventory Tracker (HIT) is a system for tracking
  * home storage inventories. 
  * @author Seth Stewart
  * @version 1.0 - Snell 340 Group 4 Phase 1
  * 
  * @invariant removedItems != null
  * @invariant rootStorageUnits != null
  * @invariant itemManager != null
  * @invariant productManager != null
  */
 @SuppressWarnings("serial")
 public class HomeInventoryTracker implements Serializable {
 	private ItemManager itemManager;
 	private ProductManager productManager;
 	private StorageUnitManager storageUnitManager;
 	
 	/** Initializes the HomeInventoryTracker. 
 	 * @pre true
 	 * @post true
 	 */
 	public HomeInventoryTracker() {
 		itemManager = new ConcreteItemManager();
 		productManager = new ConcreteProductManager();
 		storageUnitManager = new ConcreteStorageUnitManager();
 	}
 	
 	/**
 	 * Writes this instance of HomeInventoryTracker to persistent storage.
 	 * 
 	 * @param filename The filename to write to.
 	 * @throws IOException if the write failed.
 	 * 
 	 * @pre filename != null
 	 * @pre !filename.equals("")
 	 * @post File f(filename).exists()
 	 */
 	public void write(String filename) throws IOException {
 		assert(filename != null);
 		assert(!filename.equals(""));
 		
 		PersistentStorageManager persistentStorageManager = new SerializationManager();
 		persistentStorageManager.writeObject(this,filename);
 	}
 	
 	/** Determines whether the specified Storage Unit name is valid for adding a new Storage Unit.
 	 * 
 	 * @param name 	The name to be tested
 	 * @return true if name is valid, false otherwise
 	 * 
 	 * @pre name != null
 	 * @post true
 	 */
 	public boolean isValidStorageUnitName(String name) {
 		assert(name != null);
 		
 		return storageUnitManager.isValidStorageUnitName(name);
 	}
 	
 	/** Removes the identified Item from a given ProductContainer
 	 * @param item			The Item to be removed
 	 * @param container		The ProductContainer to remove the item from
 	 * 
 	 * @pre item != null && container != null
 	 * @post !containsItem(item)
 	 */
 	public void remove(Item item, ProductContainer container) throws IllegalStateException {
 		assert(item != null);
 		assert(container != null);
 		
 		container.remove(item, itemManager);
 	}
 	
 	/** Moves the identified Item from one ProductContainer to another
 	 * @param source		The ProductContainer you are moving the item from
  	 * @param destination	The ProductContainer you are moving the item to
 	 * @param item			The Item to be moved
 	 * 
 	 * @pre source != null && destination != null && item != null
 	 * @pre source.contains(item)
 	 * @post destination.contains(item) && !source.contains(item)
 	 */
 	public void move(ProductContainer source, ProductContainer destination, Item item) {
 		assert(source != null);
 		assert(destination != null);
 		assert(item != null);
 		assert(source.contains(item));
		source.remove(item, new MockItemManager());
 		destination.add(item);
 	}
 	
 	/** Deletes the identified Product from the home inventory system.
 	 * @param product		The Product to be deleted
 	 * 
 	 * @pre product != null
 	 * @post !contains(product)
 	 */
 	public void remove(Product product) throws IllegalStateException {
 		assert(product != null);
 		
 		if (!canRemove(product))
 			throw new IllegalStateException(
 					"Cannot remove product from the system; it still has items that refer to it");
 		productManager.unmanage(product);
 		storageUnitManager.remove(product);
 	}
 	
 	/** Removes the identified Product from the specified ProductContainer
 	 * @param product		The Product to be removed
 	 * @param container		The ProductContainer to remove the Product from
 	 * 
 	 * @pre product != null
 	 * @post !contains(product)
 	 */
 	public void removeFromContainer(Product product, ProductContainer container) 
 			throws IllegalStateException {
 		assert(product != null);
 		
 		container.remove(product);
 	}
 	
 	/**
 	 * Determines whether the specified product can be removed.
 	 * @param product		The Product to test
 	 * @return				true if the Product can be safely removed, false otherwise.
 	 * 
 	 * @pre product != null
 	 * @post true
 	 */
 	public boolean canRemove(Product product) {
 		// From the Data Dictionary: A Product can be removed from the system only if
 		//    the system contains no Items of the Product
 		assert(product != null);
 		
 		return !itemManager.productHasItems(product);
 	}
 	
 	/**
 	 * Checks if the identified Product exists in the home inventory system.
 	 * @param product 	The Product to check
 	 * @return			true if the product exists in the home inventory system, false otherwise.
 	 * 
 	 * @pre product != null
 	 * @post true
 	 */
 	public boolean contains(Product product) {
 		assert(product != null);
 		
 		return productManager.contains(product);
 	}
 	
 	/** Adds the identified Product to a given ProductContainer
 	 * @param container			The ProductContainer to add the Product to
 	 * @param product			The Product to be added
 	 * @return					true if the item was added to the container, false otherwise.
 	 * 
 	 * @pre container != null
 	 * @pre product != null
 	 * @post container.contains(product)
 	 */
 	public void addProductToContainer(Product product, ProductContainer container) {
 		assert(container != null);
 		assert(product != null);
 		container.add(product);
 	}
 	
 	/** Removes a ProductGroup from the system
 	 * 
 	 * @param productGroup the ProductGroup to remove
 	 * 
 	 * @pre productGroup != null
 	 * @pre productGroup.canRemove()
 	 */
 	public void remove(ProductGroup productGroup) {
 		assert(productGroup != null);
 		assert(productGroup.canRemove());
 		
 		storageUnitManager.remove(productGroup);
 	}
 	
 	/**
 	 * Removes a Storage Unit from the system
 	 * 
 	 * @param storageUnit The Storage Unit to remove
 	 * 
 	 * @pre storageUnit != null
 	 * @pre storageUnit.canRemove()
 	 */
 	public void remove(StorageUnit storageUnit) {
 		assert(storageUnit != null);
 		assert(storageUnit.canRemove());
 		
 		storageUnitManager.remove(storageUnit);
 	}
 	
 	/**
 	 * Determines whether a storage unit with the given name can be added to the system.
 	 * @param storageUnitName	the name of the storage unit to test
 	 * @return	 				true if it can be added, false otherwise
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public boolean canAddStorageUnit(String storageUnitName) {
 		return storageUnitManager.isValidStorageUnitName(storageUnitName);
 	}
 
 	/**
 	 * Add a Storage Unit to the system with the specified name
 	 * @param storageUnit The Storage Unit to add to the system
 	 * 
 	 * @pre canAddStorageUnit(storageUnit.getName())
 	 * @post true
 	 */
 	public void addStorageUnit(StorageUnit storageUnit) {
 		assert(canAddStorageUnit(storageUnit.getName()));
 		
 		storageUnitManager.add(storageUnit);
 	}
 
 	/**
 	 * Rename a Storage Unit
 	 * @param storageUnit The Storage Unit to rename
 	 * @param newStorageUnitName New name to be given to Storage Unit
 	 * 
 	 * @pre canAddStorageUnit(newStorageUnitName)
 	 * @post true
 	 */
 	public void renameStorageUnit(StorageUnit storageUnit, String newStorageUnitName) {
 		assert(canAddStorageUnit(newStorageUnitName));
 		
 		storageUnitManager.renameStorageUnit(storageUnit, newStorageUnitName);
 	}
 
 	/** Get an existing Product from the system by its barcode
 	 * 
 	 * @param barcodeScanned The Product's barcode
 	 * @return Product with the given barcode, if it exists. null, otherwise.
 	 * 
 	 * @pre barcodeScanned != null
 	 */
 	public Product getProductByBarcode(String barcodeScanned) {
 		assert(barcodeScanned != null);
 		
 		return productManager.getByBarcode(barcodeScanned);
 	}
 
 	/** Create a new Product
 	 * 
 	 * @param barcode
 	 * @param description
 	 * @param shelfLife
 	 * @param threeMonthSupply
 	 * @param productQuantity
 	 * @return
 	 * 
 	 * @pre getProductByBarcode(barcode) == null
 	 * @post getProductByBarcode(barcode) != null
 	 */
 	public Product createProduct(String barcode, String description, int shelfLife, 
 			int threeMonthSupply, ProductQuantity productQuantity) {
 		assert(getProductByBarcode(barcode) == null);
 		
 		return new Product(barcode, description, shelfLife,
 				threeMonthSupply, productQuantity, productManager);
 	}
 
 	/** Add a new Item to the system
 	 * 
 	 * @param product The new Item's Product
 	 * @param entryDate Date the Item was scanned
 	 * @param storageUnit The Storage Unit in which to place the item
 	 * @return the newly created Item
 	 * 
 	 * @pre product != null
 	 * @pre entryDate != null
 	 * @pre storageUnit != null
 	 */
 	public Item addItem(Product product, Date entryDate, StorageUnit storageUnit) {
 		assert(product != null);
 		assert(entryDate != null);
 		assert(storageUnit != null);
 		
 		Item newItem = new Item(product, entryDate, storageUnit, itemManager);
 		storageUnit.add(newItem);
 		
 		return newItem;
 	}
 	
 }
