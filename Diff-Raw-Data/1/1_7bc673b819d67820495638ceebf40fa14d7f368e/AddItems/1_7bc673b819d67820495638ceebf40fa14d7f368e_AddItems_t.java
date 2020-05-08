 package model.undo;
 
 import java.util.Date;
 import java.util.Set;
 import java.util.TreeSet;
 
 import model.BarcodePrinter;
 import model.Item;
 import model.ItemManager;
 import model.Product;
 import model.ProductContainer;
 import model.ProductManager;
 
 /**
  * Encapsulates the reversible action of adding a new Item (and perhaps a new Product) to the
  * system.
  * 
  * @author clayton
  */
 public class AddItems implements Command {
 	private final ProductContainer container;
 	private final Date entryDate;
 	private final int count;
 	private final ProductManager productManager;
 	private Product product;
 	private final ItemManager itemManager;
 	private final Set<Item> addedItems;
 	private final AddProduct addProduct;
 
 	/**
 	 * Constructs an AddItem command with the given dependencies.
 	 * 
 	 * @param productBarcode
 	 *            Barcode of the to-be-created Item's Product
 	 * @param entryDate
 	 *            Entry date of the to-be-created Item
 	 * @param count
 	 *            Number of Items to be created
 	 * @param productManager
 	 *            Manager to notify if a new Product is created
 	 * @param itemManager
 	 *            Manager to notify of the new Item
 	 * 
 	 * @pre productBarcode != null
 	 * @pre entryDate != null
 	 * @pre count > 0
 	 * @pre productManager != null
 	 * @pre itemManager != null
 	 * @post getAddedItem() == null
 	 */
 	public AddItems(ProductContainer container, AddProduct addProduct, Product product,
 			Date entryDate, int count, ProductManager productManager, ItemManager itemManager) {
 		this.container = container;
 		this.addProduct = addProduct;
 		this.product = product;
 		addedItems = new TreeSet<Item>();
 		this.entryDate = entryDate;
 		this.count = count;
 		this.productManager = productManager;
 		this.itemManager = itemManager;
 	}
 
 	/**
 	 * Adds Items to the model based on the data provided on construction.
 	 * 
 	 * @pre true
 	 * @post !(getAddedItems().isEmpty())
 	 */
 	@Override
 	public void execute() {
 		if (addProduct != null) {
 			addProduct.execute();
 			product = addProduct.getProduct();
 		}
 		if (!addedItems.isEmpty()) {
 			for (Item item : addedItems) {
 				container.add(item);
 				itemManager.manage(item);
				product.addItem(item);
 				BarcodePrinter.getInstance().addItemToBatch(item);
 			}
 		} else {
 			for (int i = 0; i < count; i++) {
 				Item item = new Item(product, container, entryDate, itemManager);
 				addedItems.add(item);
 
 				BarcodePrinter.getInstance().addItemToBatch(item);
 			}
 		}
 	}
 
 	/**
 	 * Returns a set of Items added by the execute method of this Command.
 	 * 
 	 * @return the Items added by the execute method of this Command.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public final Set<Item> getAddedItems() {
 		return addedItems;
 	}
 
 	public final AddProduct getAddProductCommand() {
 		return addProduct;
 	}
 
 	public int getItemCount() {
 		return count;
 	}
 
 	/**
 	 * Remove an Item previously added by the execute method.
 	 * 
 	 * @pre getAddedItem() != null
 	 * @post getAddedItem() == null
 	 */
 	@Override
 	public void undo() {
 		for (Item item : addedItems) {
 			container.remove(item, itemManager, false);
 			BarcodePrinter.getInstance().removeItemFromBatch(item);
 		}
 		// addedItems.clear();
 		if (addProduct != null)
 			addProduct.undo();
 		product = null;
 	}
 }
