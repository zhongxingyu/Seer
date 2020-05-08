 package common.command;
 
 import gui.batches.IAddItemBatchController;
 import java.util.Collection;
 import common.Result;
 import model.common.ModelFacade;
 import model.item.Item;
 import model.product.Product;
 import model.productcontainer.StorageUnit;
 
 /**
  * Adds the specified item to the specified storage unit.
  * @author murphyra
  */
 public class AddItemCommand extends AbstractCommand {
 
 	private Collection<Item> _items;
 	private StorageUnit _sUnit;
 	private Product _product;
 	private IAddItemBatchController _controller;
 	
 	/**
 	 * Construct a new command from an item and a storage unit.
 	 * Item will be added to the system. When undo is called, the item will be removed from the system.
 	 * @param i the item to be added. The item must be valid.
 	 * @param su the storage unit to which the item should be added.
 	 */
 	public AddItemCommand(Collection<Item> newItems, Product product, StorageUnit sUnit) {
 		_items = newItems;
 		_product = product;
 		_sUnit = sUnit;
 	}
 	
 	public AddItemCommand(Collection<Item> newItems, Product product,
 			StorageUnit sUnit, IAddItemBatchController addItemBatchController) {
 		this(newItems, product, sUnit);
 		_controller = addItemBatchController;
 	}
 
 	@Override
 	protected Result executeGuts() {
 		if(_product != null && !_product.isSaved())
 			_product.save();
 		
 		for (Item item : _items){
			if (!item.isValid())
 				return new Result(false, "All items must be valid in order to add.");
 		}
 		for (Item item : _items){
 			item.save();
 		}
 		return new Result(true);
 	}
 
 	@Override
 	protected Result undoGuts() {
 		if(_product != null && _product.isSaved())
 			_product.obliterate();
 		for (Item item : _items){
 			item.obliterate();
 		}
 		return new Result(true);
 	}
 }
