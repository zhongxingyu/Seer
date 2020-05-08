 package gui.modellistener;
 
 import gui.common.DataWrapper;
 import gui.inventory.IInventoryView;
 import gui.inventory.ProductContainerData;
 
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import model.Action;
 import model.Action.ActionType;
 import model.ProductContainer;
 import model.ProductContainerManager;
 import model.ProductGroup;
 import model.StorageUnit;
 
 /**
  * Controller object that acts as a liaison between the model's StorageUnitManager and the GUI
  * view.
  * 
  * @author Matt Hess
  * @version 1.0 CS 340 Group 4 Phase 2
  * 
  */
 public class ProductContainerListener extends InventoryListener implements Observer {
 
 	public ProductContainerListener(IInventoryView view, ProductContainerManager manager) {
 		super(view);
 		manager.addObserver(this);
 	}
 
 	/**
 	 * Method intended to notify the view when StorageUnitManager sends a "change" notice.
 	 * 
 	 * @param o
 	 *            The object being observed (which has access to the model)
 	 * @param arg
 	 *            Object passed to the view so the view can determine which changes to make
 	 * 
 	 * @pre o != null
 	 * @pre arg != null
 	 * @pre o instanceof StorageUnitManager
 	 * @post true
 	 * 
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		Action action = (Action) arg;
 		ActionType type = action.getAction();
 		Object pc = action.getObject();
 
 		ProductContainerData selectedContainer = view.getSelectedProductContainer();
 
 		if (action.getAction().equals(ActionType.CREATE)) {
 			ProductContainerData newData = new ProductContainerData();
 			if (pc instanceof ProductGroup) {
 				// Get data for inserted PC
 				ProductGroup newGroup = (ProductGroup) pc;
 				newData.setName(newGroup.getName());
 				newData.setTag(newGroup);
 			} else {
 				// Get data for new SU
 				StorageUnit newStorageUnit = (StorageUnit) pc;
 				newData.setName(newStorageUnit.getName());
 				newData.setTag(newStorageUnit);
 			}
 
 			// Get data for parent (main root)
 			ProductContainerData parent = view.getSelectedProductContainer();
 
 			// Insert
 			view.insertProductContainer(parent, newData,
 					parent.getSortedIndex(newData.getName()));
 			view.selectProductContainer(newData);
 			showContext((ProductContainer) pc);
 			return;
 		} else if (type.equals(ActionType.EDIT)) {
 			ProductContainerData data = view.getSelectedProductContainer();
 			ProductContainer container = (ProductContainer) action.getObject();
 			if (container instanceof StorageUnit) {
 				ProductContainerData realRootData = new ProductContainerData();
 				realRootData.setName("");
 				Set<StorageUnit> storageUnits = view.getProductContainerManager()
 						.getStorageUnits();
 				for (StorageUnit su : storageUnits) {
 					if (su != container)
 						realRootData.addChild(DataWrapper.wrap(su));
 				}
 				view.renameProductContainer(data, container.getName(),
 						realRootData.getSortedIndex(data.getName()));
 			} else if (container instanceof ProductGroup) {
 				ProductContainer parent = ((ProductGroup) container).getContainer();
 				ProductContainerData parentData = DataWrapper.wrap(parent);
 				int sortIndex = parentData.getSortedIndex(data.getName());
 				if (sortIndex >= parentData.getChildCount())
 					sortIndex--;
 				view.renameProductContainer(data, container.getName(), sortIndex);
 			}
 			view.selectProductContainer(data);
 		} else if (type.equals(ActionType.DELETE)) {
 			ProductContainerData data = view.getSelectedProductContainer();
 			view.deleteProductContainer(data);
 			showContext(null);
 			return; // nothing should be selected
 		}
 		view.selectProductContainer(selectedContainer);
 		updateProducts(true);
 		updateItems(true);
 		showContext((ProductContainer) pc);
 	}
 
 	private ProductContainerData loadProductContainerData(ProductContainerData parentData,
 			ProductContainer container) {
 		ProductContainerData pcData = new ProductContainerData(container.getName());
 		pcData.setTag(container);
 		parentData.addChild(pcData);
 		for (ProductGroup child : container.getProductGroups()) {
 			pcData = loadProductContainerData(pcData, child);
 		}
 		return parentData;
 	}
 }
