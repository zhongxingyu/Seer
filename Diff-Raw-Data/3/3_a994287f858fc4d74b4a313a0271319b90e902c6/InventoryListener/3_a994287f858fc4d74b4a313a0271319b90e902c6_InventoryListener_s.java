 package gui.modellistener;
 
 import gui.common.DataWrapper;
 import gui.inventory.IInventoryView;
 import gui.inventory.ProductContainerData;
 import gui.item.ItemData;
 import gui.product.ProductData;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import model.Item;
 import model.Product;
 import model.ProductContainer;
 import model.ProductContainerManager;
 import model.ProductGroup;
 import model.ProductManager;
 import model.StorageUnit;
 
 public abstract class InventoryListener {
 	protected IInventoryView view;
 
 	public InventoryListener(IInventoryView view) {
 		this.view = view;
 	}
 
 	// TODO: This code is duplicated from
 	// InventoryController.productContainerSelectionChanged(). It should not be!!!
 	// Proposed solution is to add a call to getController().productContainerSelectionChanged()
 	// in InvetoryView.selectProductContainer() -- essentially when the views selection changes
 	// programatically it will notify the controller. This will eliminate the need for this
 	// method, but may have adverse side effects.
 	/**
 	 * Sets the context view information when a ProductContainer is created or edited.
 	 * 
 	 * @param currentContainer
 	 *            - selected container used to get information for the view
 	 */
 	public void showContext(ProductContainer currentContainer) {
 		if (currentContainer instanceof StorageUnit) {
 			view.setContextGroup("");
 			view.setContextSupply("");
 			view.setContextUnit(currentContainer.getName());
 		} else if (currentContainer instanceof ProductGroup) {
 			ProductGroup group = (ProductGroup) currentContainer;
 			StorageUnit root = view.getProductContainerManager().getRootStorageUnitForChild(
 					group);
 			if (root == null)
 				return;
 			view.setContextGroup(group.getName());
 			view.setContextSupply(group.getThreeMonthSupply().toString());
 			view.setContextUnit(root.getName());
 		} else {
 			view.setContextGroup("");
 			view.setContextSupply("");
 			view.setContextUnit("");
 		}
 	}
 
 	/**
 	 * Updates all of the items in the view to match the model.
 	 */
 	public void updateItems(boolean restoreSelected) {
 		/*
 		 * Item Table The table is sorted by Entry Date (ascending). The Expiration Date column
 		 * contains the Item s expiration date, or empty if this is unspecified. The Storage
 		 * Unit column contains the name of the Storage Unit that contains the Item. The
 		 * Product Group column contains the name of the Product Group that contains the Item,
 		 * or empty if the Item is not in a Product Group.
 		 */
 
 		ProductData selectedProduct = view.getSelectedProduct();
 		ArrayList<ItemData> itemsToDisplay = new ArrayList<ItemData>();
 		ItemData selectedItem = view.getSelectedItem();
 
 		if (selectedProduct != null) {
 			Product product = (Product) selectedProduct.getTag();
 			ProductContainerData pcData = view.getSelectedProductContainer();
 			if (pcData == null)
 				throw new NullPointerException("Selected product container should not be null");
 			ProductContainer container = (ProductContainer) pcData.getTag();
 
 			Collection<Item> items;
 
 			if (container == null) { // Root container is selected
 				items = product.getItems();
 			} else {
 				items = container.getItemsForProduct(product);
 			}
 			for (Item item : items) {
 				ItemData id = DataWrapper.wrap(item);
 				itemsToDisplay.add(id);
 			}
 		}
 
 		Collections.sort(itemsToDisplay, new Comparator<ItemData>() {
 			@Override
 			public int compare(ItemData arg0, ItemData arg1) {
 				return arg0.getEntryDate().compareTo(arg1.getEntryDate());
 			}
 		});
 
 		view.setItems(itemsToDisplay.toArray(new ItemData[itemsToDisplay.size()]));
 		if (restoreSelected && selectedItem != null)
 			view.selectItem(selectedItem);
 	}
 
 	/**
 	 * Updates all of the ProductContainers in the view to match the model.
 	 */
 	public void updateProductContainers(boolean restoreSelected) {
 		// TODO: Do we really need this? This one is less commonly used because only its child
 		// class ProductContainerListener should need to update this...
 		updateProducts(restoreSelected);
 	}
 
 	/**
 	 * Updates all of the products in the view to match the model.
 	 */
 	public void updateProducts(boolean restoreSelected) {
 		/*
 		 * Product Table The table is sorted by Description (ascending). The Count column
 		 * displays the number of Items of that Product contained in the selected node.
 		 * Specifically, if the root Storage Units node is selected, Count is the total number
 		 * of Items of that Product in the entire system . If a Product Container (Storage Unit
 		 * or Product Group) node is selected, Count is the number of Items of that Product
 		 * contained in the selected Product Container.
 		 */
 
 		// MERGED from InventoryController
 		// Load Products in selected ProductContainer
 		List<ProductData> productDataList = new ArrayList<ProductData>();
 		ProductContainerData selectedContainer = view.getSelectedProductContainer();
 		ProductData selectedProduct = view.getSelectedProduct();
 
 		if (selectedContainer != null) {
 			ProductContainer selected = (ProductContainer) selectedContainer.getTag();
 			if (selected != null) {
 				for (Product p : selected.getProducts()) {
 					int count = selected.getItemsForProduct(p).size();
 					productDataList.add(DataWrapper.wrap(p, count));
 				}
 				// Update contextView
 				ProductContainer currentContainer = (ProductContainer) selectedContainer
 						.getTag();
 				if (currentContainer instanceof StorageUnit) {
 					view.setContextUnit(selectedContainer.getName());
 					view.setContextGroup("");
 					view.setContextSupply("");
 				} else if (currentContainer instanceof ProductGroup) {
 					ProductGroup group = (ProductGroup) currentContainer;
 					StorageUnit root = view.getProductContainerManager()
 							.getRootStorageUnitForChild(group);
 					view.setContextUnit(root.getName());
 					view.setContextGroup(group.getName());
 					view.setContextSupply(group.getThreeMonthSupply().toString());
 				}
 			} else {
 				// Root "Storage units" node is selected; display all Products in system
 				ProductManager manager = view.getProductManager();
 				ProductContainerManager pcManager = view.getProductContainerManager();
 				for (Product p : manager.getProducts()) {
 					int count = 0;
 					for (StorageUnit su : pcManager.getStorageUnits()) {
						count += su.getItemsForProduct(p).size();
 					}
 					productDataList.add(DataWrapper.wrap(p, count));
 				}
 				view.setContextUnit("All");
 			}
 		}
 
 		Collections.sort(productDataList, new Comparator<ProductData>() {
 			@Override
 			public int compare(ProductData o1, ProductData o2) {
 				return o1.getDescription().compareTo(o2.getDescription());
 			}
 		});
 
 		view.setProducts(productDataList.toArray(new ProductData[productDataList.size()]));
 		if (restoreSelected && selectedProduct != null)
 			view.selectProduct(selectedProduct);
 		updateItems(restoreSelected);
 	}
 }
