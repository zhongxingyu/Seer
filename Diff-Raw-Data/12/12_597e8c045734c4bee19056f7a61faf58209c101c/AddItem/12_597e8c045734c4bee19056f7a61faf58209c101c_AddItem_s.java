 package undo;
 
 import java.util.Date;
 
 import model.Item;
 import model.ItemManager;
 import model.ProductManager;
 
 /**
  * Encapsulates the reversible action of adding a new Item (and perhaps a new Product) to the
  * system.
  * 
  * @author clayton
  */
 public class AddItem implements Command {
 	private String productBarcode;
 	private Date entryDate;
 	private int count;
 	private ProductManager productManager;
 	private ItemManager itemManager;
 	private Item addedItem;
 
 	/**
 	 * Constructs an AddItem command with the given dependencies.
 	 * 
	 * @param itemToAdd
	 *            ItemData object containing all the data for the to-be-created Item
 	 * 
 	 * @pre productBarcode != null
 	 * @pre entryDate != null
 	 * @pre count > 0
 	 * @pre productManager != null
 	 * @pre itemManager != null
 	 * @post getAddedItem() == null
 	 */
 	public AddItem(String productBarcode, Date entryDate, int count,
 			ProductManager productManager, ItemManager itemManager) {
 	}
 
 	/**
 	 * Add an Item to the model based on the data provided on construction.
 	 * 
 	 * @pre getAddedItem() == null
 	 * @post getAddedItem() != null
 	 * @post getAddedItem().getProductBarcode() == productBarcode
 	 */
 	@Override
 	public void execute() {
 	}
 
 	/**
 	 * Returns the Item added by the execute method of this Command.
 	 * 
 	 * @return the Item added by the execute method of this Command.
 	 */
 	public Item getAddedItem() {
 		return addedItem;
 	}
 
 	/**
 	 * Remove an Item previously added by the execute method.
 	 * 
 	 * @pre getAddedItem() != null
 	 * @post getAddedItem() == null
 	 */
 	@Override
 	public void undo() {
 	}
 }
