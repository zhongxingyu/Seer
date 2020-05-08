 package gui.batches;
 
 import gui.common.Controller;
 import gui.common.DataWrapper;
 import gui.common.IView;
 import gui.item.ItemData;
 import gui.product.ProductData;
 
 import java.util.Set;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import model.Action;
 import model.Action.ActionType;
 import model.ConcreteItemManager;
 import model.Item;
 import model.ItemManager;
 import model.Product;
 import model.ProductContainer;
 import model.undo.RemoveItem;
 import model.undo.UndoManager;
 
 /**
  * Controller class for the remove item batch view.
  */
 public class RemoveItemBatchController extends Controller implements
 		IRemoveItemBatchController {
 	
 	private UndoManager manager;
 	private Map<Product, ArrayList<Item>> removedItems;
 	private Map<Item,String> itemsPCnames;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view
 	 *            Reference to the remove item batch view.
 	 */
 	public RemoveItemBatchController(IView view) {
 		super(view);
 
 		manager = new UndoManager();
 		
 		removedItems = new TreeMap<Product, ArrayList<Item>>();
 		
 		itemsPCnames = new TreeMap<Item, String>();
 		
 		construct();
 	}
 
 	/**
 	 * This method is called when the "Item Barcode" field is changed in the remove item batch
 	 * view by the user.
 	 */
 	@Override
 	public void barcodeChanged() {
 		enableComponents();
 	}
 
 	/**
 	 * This method is called when the user clicks the "Done" button in the remove item batch
 	 * view.
 	 */
 	@Override
 	public void done() {
 		getView().close();
 	}
 
 	/**
 	 * This method is called when the user clicks the "Redo" button in the remove item batch
 	 * view.
 	 * 
 	 * @pre manager.canRedo()
 	 * @post manager.canUndo()
 	 * 
 	 */
 	@Override
 	public void redo() {
 		if(!manager.canRedo())
 			throw new IllegalStateException("No redo action available");
 		
 		RemoveItem command = (RemoveItem) manager.redo();
 		
 		((ConcreteItemManager) getItemManager()).notifyObservers(
 				new Action(command.getRemovedItem(),ActionType.DELETE));
 		
 		enableComponents();
 	}
 
 	/**
 	 * This method is called when the user clicks the "Remove Item" button in the remove item
 	 * batch view.
 	 */
 	@Override
 	public void removeItem() {
 		RemoveItemBatchView view = (RemoveItemBatchView) getView();
 
 		// show dialog box if item doesn't exist
 		if (getItemManager().getItemByItemBarcode(view.getBarcode()) == null) {
 			view.displayErrorMessage("This item doesn't exist.");
 			view.setBarcode("");
 			return;
 		}
 		
 		Item item = getItemManager().getItemByItemBarcode(view.getBarcode());
 		
 		manager.execute(new RemoveItem(view.getBarcode(),getItemManager(),this));
 		
 		((ConcreteItemManager) getItemManager()).notifyObservers(new Action(item,ActionType.DELETE));
 		
 		enableComponents();
 	}
 
 	/**
 	 * This method is called when the selected product changes in the remove item batch view.
 	 */
 	@Override
 	public void selectedProductChanged() {
 		RemoveItemBatchView view = (RemoveItemBatchView) getView();
 		ItemManager manager = getItemManager();
 
 		ProductData selected = view.getSelectedProduct();
 		if (selected == null)
 			return;
 		
 		Product selectedProduct = (Product) selected.getTag();
 		
 		ArrayList<Item> productItems = removedItems.get(selectedProduct);
 		if(productItems == null || productItems.isEmpty())
 			throw new IllegalStateException("This product should have item(s)");
 		
 		ItemData[] items = new ItemData[productItems.size()];
 		int counter = 0;
 		for(Item item : productItems) {
 			items[counter] = DataWrapper.wrap(item);
			items[counter++].setProductGroup(itemsPCnames.get(item));
 		}
 		getView().setItems(items);
 	}
 
 	/**
 	 * This method is called when the user clicks the "Undo" button in the remove item batch
 	 * view.
 	 * 
 	 * @pre manager.canUndo()
 	 * @post manager.canRedo();
 	 * 
 	 */
 	@Override
 	public void undo() {
 		if(!manager.canUndo())
 			throw new IllegalStateException("No undo action available");
 		
 		RemoveItem command = (RemoveItem) manager.undo();
 		
 		((ConcreteItemManager) getItemManager()).notifyObservers(
 				new Action(command.getRemovedItem(),ActionType.DELETE));
 		
 		enableComponents();
 	}
 
 	/**
 	 * This method is called when the "Use Barcode Scanner" setting is changed in the remove
 	 * item batch view by the user.
 	 */
 	@Override
 	public void useScannerChanged() {
 	}
 
 	/**
 	 * Sets the enable/disable state of all components in the controller's view. A component
 	 * should be enabled only if the user is currently allowed to interact with that component.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The enable/disable state of all components in the controller's view have been set
 	 * appropriately.}
 	 */
 	@Override
 	protected void enableComponents() {
 		getView().enableItemAction(!getView().getBarcode().equals(""));
 		
 		getView().enableRedo(manager.canRedo());
 		getView().enableUndo(manager.canUndo());
 	}
 
 	/**
 	 * Returns a reference to the view for this controller.
 	 */
 	@Override
 	protected IRemoveItemBatchView getView() {
 		return (IRemoveItemBatchView) super.getView();
 	}
 
 	/**
 	 * Loads data into the controller's view.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The controller has loaded data into its view}
 	 */
 	@Override
 	protected void loadValues() {
 		// no code needed - no pre-loaded state for this batch
 	}
 	
 	/**
 	 * Method that updates the view after an execute() or redo() call
 	 * 
 	 * @param removedItem the item just removed
 	 * 
 	 * @pre removedItem != null
 	 * @post true
 	 */
 	public void updateOnExecuteOrRedo(Item removedItem) {
 		if(removedItem == null)
 			throw new IllegalArgumentException("removedItem can't be null");
 		
 		updateRemovedProducts(removedItem);
 	}
 	
 	/**
 	 * Method that adds an Item to the removedItems map
 	 * 
 	 * @param item the item that was just removed
 	 * @param itemContainer the removed item's previous container
 	 * 
 	 * @pre item != null
 	 * @pre itemContainer != null
 	 * @post removedItems.get(item.getProduct()) != null
 	 * @post if(removedItems.get(item.getProduct()@pre != null)
 	 * 		removedItems.get(item.getProduct()@post == 
 	 * 		removedItems.get(item.getProduct()@pre + 1
 	 */
 	public void addItemToRemoved(Item item,ProductContainer itemContainer) {
 		if(item == null)
 			throw new IllegalArgumentException("Item shouldn't be null");
 		
 		if(removedItems.get(item.getProduct()) == null)
 			removedItems.put(item.getProduct(), new ArrayList<Item>());
 		
 		ArrayList<Item> removedByProduct = removedItems.get(item.getProduct());
 		itemsPCnames.put(item, itemContainer.getName());
 		if(removedByProduct.contains(item))
 			throw new IllegalStateException("RemovedItems already contains this item");
 		
 		removedByProduct.add(item);
 	}
 
 	/**
 	 * Method that adds an Item to the removedItems map
 	 * 
 	 * @param item the item that was just removed
 	 * @param itemContainer the removed item's previous container
 	 * 
 	 * @pre item != null
 	 * @pre itemContainer != null
 	 * @post removedItems.get(item.getProduct()) != null
 	 * @post if(removedItems.get(item.getProduct()@pre != null)
 	 * 		removedItems.get(item.getProduct()@post == 
 	 * 		removedItems.get(item.getProduct()@pre + 1
 	 */
 	public void removeItemFromRemoved(Item item) {
 		if(item == null)
 			throw new IllegalArgumentException("Item shouldn't be null");
 		
 		Product itemProduct = item.getProduct();
 		
 		if(removedItems.get(itemProduct) == null)
 			removedItems.put(itemProduct, new ArrayList<Item>());
 		
 		ArrayList<Item> removedByProduct = removedItems.get(itemProduct);
 		itemsPCnames.remove(item);
 		if(!removedByProduct.contains(item))
 			throw new IllegalStateException("Removed items doesn't contain this item");
 		
 		removedByProduct.remove(item);
 		
 		if(removedByProduct.isEmpty())
 			removedItems.remove(itemProduct);
 	}
 		
 	/**
 	 * Method that updates the view after an Undo call
 	 * 
 	 * @param removedItem the Item's removal to undo
 	 * 
 	 * @pre removedItem != null
 	 * @post true
 	 */
 	public void updateOnUndo(Item removedItem) {
 		updateRemovedProducts(removedItem);
 	}
 	
 	private void updateRemovedProducts(Item item) {
 		assert(item != null);
 		
 		// update product view
 		Set<Product> keys = removedItems.keySet();
 		ProductData[] products = new ProductData[keys.size()];
 		int counter = 0;
 		for(Product product : keys) {
 			products[counter++] = DataWrapper.wrap(product, removedItems.get(product).size());
 		}
 		getView().setProducts(products);
 
 		// update item view
 		getView().setItems(new ItemData[0]);
 		
 		enableComponents();
 	}
 }
