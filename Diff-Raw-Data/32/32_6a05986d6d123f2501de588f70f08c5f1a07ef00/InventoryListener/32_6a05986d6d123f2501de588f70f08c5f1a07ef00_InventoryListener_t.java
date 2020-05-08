 package mcontrollers;
 
 import gui.common.DataWrapper;
 import gui.inventory.IInventoryView;
 import gui.inventory.ProductContainerData;
 import gui.item.ItemData;
 import gui.product.ProductData;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import model.Item;
 import model.Product;
 import model.ProductContainer;
 import model.ProductGroup;
 import model.StorageUnit;
 
 public abstract class InventoryListener {
 	protected IInventoryView view;
 
 	public InventoryListener(IInventoryView view) {
 		this.view = view;
 	}
 
 	// TODO: This code is duplicated from
 	// InventoryController.productContainerSelectionChanged(). It should not be!!!
 	// Proposed solution is to add a call to
 	// getController().productContainerSelectionChanged()
 	// in InvetoryView.selectProductContainer() -- essentially when the views selection changes
 	// programatically
 	// it will notify the controller. This will eliminate the need for this method, but may
 	// have adverse side
 	// effects.
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
 			view.setContextGroup(group.getName());
 			view.setContextSupply(group.getThreeMonthSupply().toString());
 			view.setContextUnit(root.getName());
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void updateAll() {
 		updateProductContainers();
 		updateProducts(true);
 		updateItems(true);
 	}
 
 	/**
 	 * Updates all of the items in the view to match the model.
 	 */
 	public void updateItems(boolean restoreSelected) {
 		ItemData selectedItem = view.getSelectedItem();
 		ArrayList<ItemData> itemsToDisplay = new ArrayList<ItemData>();
 		ProductContainerData parent = view.getSelectedProductContainer();
 		if (parent != null && parent.getTag() != null) {
 			ProductContainer pc = (ProductContainer) parent.getTag();
 			ProductData pd = view.getSelectedProduct();
 			if (pd != null && pd.getTag() != null) {
 				Product product = (Product) pd.getTag();
 				Iterator<Item> itemIterator = pc.getItemsForProduct(product).iterator();
 				while (itemIterator.hasNext()) {
 					ItemData id = DataWrapper.wrap(itemIterator.next());
 					itemsToDisplay.add(id);
 				}
 			}
 		}
 		view.setItems(itemsToDisplay.toArray(new ItemData[itemsToDisplay.size()]));
 		if (restoreSelected && selectedItem != null)
 			view.selectItem(selectedItem);
 	}
 
 	/**
 	 * Updates all of the ProductContainers in the view to match the model.
 	 */
 	public void updateProductContainers() {
 		// TODO: Do we really need this? This one is less commonly used because only its child
 		// class ProductContainerListener should need to update this...
 	}
 
 	/**
 	 * Updates all of the products in the view to match the model.
 	 */
 	public void updateProducts(boolean restoreSelected) {
 		ProductData selectedProduct = view.getSelectedProduct();
 		ArrayList<ProductData> products = new ArrayList<ProductData>();
 
 		ProductContainerData selectedContainer = view.getSelectedProductContainer();
 		if (selectedContainer != null && selectedContainer.getTag() != null) {
 			ProductContainer container = (ProductContainer) selectedContainer.getTag();
 			Iterator<Product> iter = container.getProductsIterator();
 
 			while (iter.hasNext()) {
 				Product product = iter.next();
 				int count = container.getItemsForProduct(product).size();
 				ProductData productData = DataWrapper.wrap(product, count);
 				products.add(productData);
 			}
 		}
 
 		view.setProducts(products.toArray(new ProductData[products.size()]));
 		if (restoreSelected && selectedProduct != null)
 			view.selectProduct(selectedProduct);
 	}
 }
