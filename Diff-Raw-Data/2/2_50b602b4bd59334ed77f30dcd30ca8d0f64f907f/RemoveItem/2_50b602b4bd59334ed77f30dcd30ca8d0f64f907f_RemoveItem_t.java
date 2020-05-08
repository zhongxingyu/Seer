 package model.undo;
 
 import model.Item;
 import model.ItemManager;
 import model.ProductContainer;
 
 /**
  * Class that stores the RemoveItem state for undo/redo operations
  * 
  * @author mgh14
  * 
  * @invariant manager != null
  * @invariant toRemove != null
  * @invariant itemContainer != null
  * 
  */
 public class RemoveItem implements Command {
 	private ItemManager manager;
 	private Item toRemove;
 	private ProductContainer itemContainer;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param itemBarcode
 	 *            the item to locate
 	 * @param manager
 	 *            the program ItemManager
 	 * 
 	 * @pre itemBarcode != null
 	 * @pre manager.getItemByItemBarcode(itemBarcode) != null
 	 * @pre manager.getItemByItemBarcode(itemBarcode).getContainer() != null
 	 * @pre manager != null
 	 * @post true
 	 * 
 	 */
 	public RemoveItem(String itemBarcode, ItemManager manager) {
 		this.manager = manager;
 
 		toRemove = manager.getItemByItemBarcode(itemBarcode);
 		itemContainer = toRemove.getContainer();
 	}
 
 	/**
 	 * Removes an item from the model based on command data from the constructor
 	 * 
 	 * @pre true
 	 * @post toRemove.getContainer() == null
 	 * @post manager.removedItems.contains(toRemove)
 	 */
 	@Override
 	public void execute() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Returns the Item removed by the execute method of this Command.
 	 * 
 	 * @return the Item removed by the execute method of this Command.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
	public Item getRemovedItem() {
 		return toRemove;
 	}
 
 	/**
 	 * Undoes the remove item action performed previously on toRemove
 	 * 
 	 * @pre !itemContainer.contains(toRemove)
 	 * @post itemContainer.contains(toRemove)
 	 * @post toRemove.getContainer().equals(itemContainer)
 	 */
 	@Override
 	public void undo() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
