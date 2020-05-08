 package gui.inventory;
 
 import gui.common.Controller;
 import gui.common.DataWrapper;
 import gui.item.ItemData;
 import gui.product.ProductData;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import mcontrollers.ItemListener;
 import mcontrollers.ProductContainerListener;
 import mcontrollers.ProductListener;
 import model.Item;
 import model.ItemManager;
 import model.Product;
 import model.ProductContainer;
 import model.ProductGroup;
 import model.StorageUnit;
 
 /**
  * Controller class for inventory view.
  */
 public class InventoryController extends Controller implements IInventoryController {
 	/**
 	 * Constructor.
 	 * 
 	 * @param view
 	 *            Reference to the inventory view
 	 * 
 	 * @pre view != null
 	 * @post true
 	 */
 	public InventoryController(IInventoryView view) {
 		super(view);
 		new ProductContainerListener(getView(), getProductContainerManager());
 		new ItemListener(getView(), getItemManager());
 		new ProductListener(getView(), getProductManager());
 		construct();
 	}
 
 	/**
 	 * This method is called when the user selects the "Add Items" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canAddItems())
 	 * 
 	 * @pre canAddItems()
 	 * @post true
 	 */
 	@Override
 	public void addItems() {
 		if (!canAddItems()) {
 			throw new IllegalStateException("Unable to add Items");
 		}
 		getView().displayAddItemBatchView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Add Product Group" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canAddProductGroup())
 	 * 
 	 * @pre canAddProductGroup()
 	 * @post true
 	 */
 	@Override
 	public void addProductGroup() {
 		if (!canAddProductGroup()) {
 			throw new IllegalStateException("Unable to add Product Groups");
 		}
 		getView().displayAddProductGroupView();
 	}
 
 	/**
 	 * This method is called when the user drags a product into a product container.
 	 * 
 	 * @param productData
 	 *            Product dragged into the target product container
 	 * @param containerData
 	 *            Target product container
 	 * @throws IllegalArgumentException
 	 *             if either parameter is null
 	 * @throws IllegalStateException
 	 *             if either parameter's getTag() == null
 	 * 
 	 * @pre productData != null && productData.getTag() != null
 	 * @pre containerData != null && containerData.getTag() != null
 	 * @post containerData.getChildCount() == old(getChildCount()) + 1
 	 */
 	@Override
 	public void addProductToContainer(ProductData productData,
 			ProductContainerData containerData) {
 
 		/*
 		 * Desired Behavior
 		 * 
 		 * Target Product Container = the Product Container the user dropped the Product on
 		 * 
 		 * Target StorageUnit = the StorageUnit containing the Target Product Container
 		 * 
 		 * If the Product is already contained in a Product Container in the Target StorageUnit
 		 * Else Add the Product to the Target Product Container
 		 */
 
 		if (productData == null)
 			throw new IllegalArgumentException("ProductData should not be null");
 		if (containerData == null)
 			throw new IllegalArgumentException("ProductContainerData should not be null");
 
 		Product productToAdd = (Product) productData.getTag();
 		if (productToAdd == null)
 			throw new IllegalStateException("Product must have a tag.");
 		ProductContainer targetContainer = (ProductContainer) containerData.getTag();
 		if (targetContainer == null)
 			throw new IllegalStateException("ProductContainer must have a tag.");
 
 		ProductContainer oldContainer = getSelectedProductContainerTag();
 		StorageUnit targetSU = getProductContainerManager().getRootStorageUnitByName(
 				targetContainer.getName());
 
 		// add product to container
 
 		if (targetSU.hasDescendantProductContainer(oldContainer)
 				|| targetSU.equals(oldContainer)) {
 			// Staying in the same tree, move items
 
 			// Get all the items
 			ItemManager itemManager = getItemManager();
 			Set<Item> itemsToMove = itemManager.getItemsByProduct(productToAdd);
 
 			// copy the items so we can loop over them to remove and add
 			Set<Item> itemsToRemove = new HashSet<Item>();
 			Set<Item> itemsToAdd = new HashSet<Item>();
 
 			for (Item item : itemsToMove) {
 				itemsToAdd.add(item);
 				itemsToRemove.add(item);
 			}
 
 			// remove the items so we can remove the product
 			for (Item item : itemsToRemove) {
 				oldContainer.remove(item, itemManager);
 			}
 
 			// remove the product
 			oldContainer.remove(productToAdd);
 			productToAdd.removeContainer(oldContainer);
 
 			// add the product to the target
 			productToAdd.addContainer(targetContainer);
 			targetContainer.add(productToAdd);
 
 			// add the items
 			for (Item item : itemsToAdd) {
 				targetContainer.add(item);
 				itemManager.manage(item);
 			}
 		} else {
 			productToAdd.addContainer(targetContainer);
 			targetContainer.add(productToAdd);
 		}
 
 	}
 
 	/**
 	 * This method is called when the user selects the "Add Storage Unit" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canAddStorageUnit())
 	 * 
 	 * @pre canAddStorageUnit()
 	 * @post true
 	 */
 	@Override
 	public void addStorageUnit() {
 		if (!canAddStorageUnit()) {
 			throw new IllegalStateException("Unable to add Storage Units");
 		}
 		getView().displayAddStorageUnitView();
 	}
 
 	/**
 	 * Returns true if and only if the "Add Items" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canAddItems() {
 		// Always enabled per Functional Spec p17
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Add Product Group" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canAddProductGroup() {
 		// Always enabled per Functional Spec p15
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Add Storage Unit" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canAddStorageUnit() {
 		// Always enabled per Functional Spec p14
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Delete Product" menu item should be enabled.
 	 * 
 	 * @pre if(getView().getSelectedProductContainer() != null)
 	 *      getView().getSelectedProductContainer().getTag() != null
 	 * @pre if(getView().getSelectedProductContainer() != null) getSelectedProductTag() != null
 	 * @post true
 	 * 
 	 */
 	@Override
 	public boolean canDeleteProduct() {
 		// 3 cases depending on getView().getSelectedProductContainer().
 		// See Functional Spec p21-22
 
 		// case 1: No product container is selected
 		if (getView().getSelectedProductContainer() == null)
 			return false;
 
 		ProductContainer containerTag = getSelectedProductContainerTag();
 		if (getView().getSelectedProduct() == null)
 			return false;
 
 		Product productTag = getSelectedProductTag();
 
 		// case 2: selected product container is the root node
 		if (containerTag == null) // root 'Storage Units' is assigned 'null' for its tag
 			return productTag.canRemove();
 
 		// case 3: selected product container is a child StorageUnit or ProductGroup
 		else
 			return containerTag.canRemove(productTag);
 	}
 
 	/**
 	 * Returns true if and only if the "Delete Product Group" menu item should be enabled.
 	 * 
 	 * @pre getView().getSelectedProductContainer() != null
 	 * @pre getView().getSelectedProductContainer().getTag() instanceof ProductGroup
 	 * @post true
 	 */
 	@Override
 	public boolean canDeleteProductGroup() {
 		// Enabled only if getView().getSelectedProductContainer() does not contain any
 		// items (including it's sub Product Groups)
 		// See Functional Spec p17
 
 		return getSelectedProductContainerTag().canRemove();
 	}
 
 	/**
 	 * Returns true if and only if the "Delete Storage Unit" menu item should be enabled.
 	 * 
 	 * @pre getView().getSelectedProductContainer() != null
 	 * @pre getView().getSelectedProductContainer().getTag() instanceof StorageUnit
 	 * @post true
 	 */
 	@Override
 	public boolean canDeleteStorageUnit() {
 		// Enabled only if getView().getSelectedProductContainer() does not contain any
 		// items (including it's Product Groups)
 		// See Functional Spec p15
 
 		if (getView().getSelectedProductContainer() == null)
 			return false;
 
 		return getSelectedProductContainerTag().canRemove();
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Item" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditItem() {
 		return getView().getSelectedItem() != null;
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Product" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditProduct() {
 		return getView().getSelectedProduct() != null;
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Product Group" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditProductGroup() {
 		ProductContainerData pcData = getView().getSelectedProductContainer();
 		return pcData != null && (pcData.getTag() instanceof ProductGroup);
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Storage Unit" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditStorageUnit() {
 		ProductContainerData pcData = getView().getSelectedProductContainer();
 		return pcData != null && (pcData.getTag() instanceof StorageUnit);
 	}
 
 	/**
 	 * Returns true if and only if the "Remove Item" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canRemoveItem() {
 		ItemData id = getView().getSelectedItem();
 		return id != null && id.getTag() != null;
 	}
 
 	/**
 	 * Returns true if and only if the "Remove Items" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canRemoveItems() {
 		ProductContainerData pcData = getView().getSelectedProductContainer();
 		return pcData.getTag() == null;
 	}
 
 	/**
 	 * Returns true if and only if the "Transfer Items" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canTransferItems() {
 		// Always enabled per Functional Spec p24
 		return true;
 	}
 
 	/**
 	 * This method is called when the user selects the "Delete Product" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canDeleteProduct())
 	 * 
 	 * @pre canDeleteProduct()
 	 * @pre getSelectedProductTag() != null
 	 * @post !getProductManager().contains(old(getView().getSelectedProduct().getTag()))
 	 */
 	@Override
 	public void deleteProduct() {
 		if (!canDeleteProduct()) {
 			throw new IllegalStateException("Unable to delete Product");
 		}
 		ProductContainer parent = (ProductContainer) getView().getSelectedProductContainer()
 				.getTag();
 		parent.remove(getSelectedProductTag());
 
 		getProductManager().unmanage(getSelectedProductTag());
 	}
 
 	/**
 	 * This method is called when the user selects the "Delete Product Group" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canDeleteProductGroup())
 	 * 
 	 * @pre canDeleteProductGroup()
 	 * @pre getSelectedProductContainerTag() != null
 	 * @post !getProductContainerManager().contains(PREVIOUS
 	 *       getView().getSelectedProductContainer().getTag())
 	 */
 	@Override
 	public void deleteProductGroup() {
 		if (!canDeleteProductGroup()) {
 			throw new IllegalStateException("Unable to delete Product Group");
 		}
 
 		deleteSelectedProductContainer();
 	}
 
 	/**
 	 * This method is called when the user selects the "Delete Storage Unit" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canDeleteStorageUnit())
 	 * 
 	 * @pre canDeleteStorageUnit()
 	 * @pre getSelectedProductContainerTag() != null
 	 * @post !getProductContainerManager().contains(PREVIOUS
 	 *       getView().getSelectedProductContainer().getTag())
 	 */
 	@Override
 	public void deleteStorageUnit() {
 		if (!canDeleteStorageUnit()) {
 			throw new IllegalStateException("Unable to delete Storage Unit");
 		}
 
 		deleteSelectedProductContainer();
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Item" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canEditItem())
 	 * 
 	 * @pre canEditItem()
 	 * @post getItemManager().contains(old(getView().getSelectedItem().getTag()))
 	 */
 	@Override
 	public void editItem() {
 		ItemData selectedItem = getView().getSelectedItem();
 		if (!canEditItem()) {
 			throw new IllegalStateException("Unable to edit Item");
 		}
 		getView().displayEditItemView();
 		getView().selectItem(selectedItem);
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Product" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canEditProduct())
 	 * 
 	 * @pre canEditProduct()
 	 * @post getProductManager().contains(old(getView().getSelectedProduct().getTag()))
 	 */
 	@Override
 	public void editProduct() {
 		if (!canEditProduct()) {
 			throw new IllegalStateException("Unable to edit Product");
 		}
 		getView().displayEditProductView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Product Group" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canEditProductGroup)
 	 * 
 	 * @pre canEditProductGroup()
 	 * @post getProductContainerManager().contains(PREVIOUS
 	 *       getView().getSelectedProductContainer().getTag())
 	 */
 	@Override
 	public void editProductGroup() {
 		if (!canEditProductGroup()) {
 			throw new IllegalStateException("Unable to edit Product Group");
 		}
 		getView().displayEditProductGroupView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Storage Unit" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canEditStorageUnit())
 	 * 
 	 * @pre canEditStorageUnit()
 	 * @post getProductContainerManager().contains(PREVIOUS
 	 *       getView().getSelectedProductContainer().getTag())
 	 */
 	@Override
 	public void editStorageUnit() {
 		if (!canEditStorageUnit()) {
 			throw new IllegalStateException("Unable to edit Storage Unit");
 		}
 		getView().displayEditStorageUnitView();
 	}
 
 	/**
 	 * This method is called when the selected item changes.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public void itemSelectionChanged() {
 		// The only possible change is in the context menus available
 		enableComponents();
 	}
 
 	/**
 	 * This method is called when the user drags an item into a product container.
 	 * 
 	 * @param itemData
 	 *            Item dragged into the target product container
 	 * @param containerData
 	 *            Target product container
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if either parameter is null
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if either parameter is null
 	 * @throws IllegalStateException
 	 *             if the target product container doesn't have a tag
 	 * 
 	 * @pre itemData != null
 	 * @pre containerData != null
 	 * @pre getView().getSelectedProductContainer() != null
 	 * @pre getView().getSelectedProductContainer().getTag().contains(itemData.getTag())
 	 * @pre !containerData.getTag().contains(itemData.getTag())
 	 * @post !old(getView().getSelectedProductContainer().getTag().contains(itemData.getTag()))
 	 * @post containerData.getTag().contains(itemData.getTag())
 	 * 
 	 */
 	@Override
 	public void moveItemToContainer(ItemData itemData, ProductContainerData containerData) {
 		if (itemData == null)
			throw new NullPointerException("ItemData should not be null.");
 		if (containerData == null)
			throw new NullPointerException("ProductContainerData should not be null.");
 
 		ProductContainer targetContainer = (ProductContainer) containerData.getTag();
 		if (targetContainer == null)
			throw new NullPointerException("Target product container must have a tag.");
 
 		// note: the currently-selected ProductContainer is the source
 		getSelectedProductContainerTag().moveIntoContainer(getSelectedItemTag(),
 				targetContainer);
 	}
 
 	/**
 	 * This method is called when the selected item container changes.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public void productContainerSelectionChanged() {
 		// Load Products in selected ProductContainer
 		List<ProductData> productDataList = new ArrayList<ProductData>();
 		ProductContainerData selectedContainer = getView().getSelectedProductContainer();
 		if (selectedContainer != null) {
 			ProductContainer selected = (ProductContainer) selectedContainer.getTag();
 			if (selected != null) {
 				Iterator<Product> it = selected.getProductsIterator();
 				while (it.hasNext()) {
 					Product p = it.next();
 					int count = selected.getItemsForProduct(p).size();
 					productDataList.add(DataWrapper.wrap(p, count));
 				}
 			}
 		}
 		getView().setProducts(productDataList.toArray(new ProductData[0]));
 
 		// Clear ItemTable
 		List<ItemData> itemDataList = new ArrayList<ItemData>();
 		getView().setItems(itemDataList.toArray(new ItemData[0]));
 
 		// Update contextView
 		ProductContainer currentContainer = (ProductContainer) selectedContainer.getTag();
 		if (currentContainer instanceof StorageUnit) {
 			getView().setContextGroup("");
 			getView().setContextSupply("");
 			getView().setContextUnit(selectedContainer.getName());
 		} else if (currentContainer instanceof ProductGroup) {
 			ProductGroup group = (ProductGroup) currentContainer;
 			StorageUnit root = getView().getProductContainerManager()
 					.getRootStorageUnitByName(group.getName());
 			getView().setContextGroup(group.getName());
 			getView().setContextSupply(group.getThreeMonthSupply().toString());
 			getView().setContextUnit(root.getName());
 		}
 	}
 
 	/**
 	 * This method is called when the selected product changes.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public void productSelectionChanged() {
 		ArrayList<ItemData> itemsToDisplay = new ArrayList<ItemData>();
 		ProductData selectedProduct = getView().getSelectedProduct();
 
 		if (selectedProduct != null) {
 			Product product = (Product) selectedProduct.getTag();
 			ProductContainerData pcData = getView().getSelectedProductContainer();
 			if (pcData == null)
 				throw new NullPointerException("Selected product container should not be null");
 			ProductContainer container = (ProductContainer) pcData.getTag();
 			if (container == null)
 				throw new NullPointerException("Product container tag should not be null");
 
 			Iterator<Item> itemIterator = container.getItemsForProduct(product).iterator();
 			while (itemIterator.hasNext()) {
 				ItemData id = DataWrapper.wrap(itemIterator.next());
 				itemsToDisplay.add(id);
 			}
 		}
 		getView().setItems(itemsToDisplay.toArray(new ItemData[0]));
 	}
 
 	/**
 	 * This method is called when the user selects the "Remove Item" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if !canRemoveItem()
 	 * 
 	 * @pre canRemoveItem()
 	 * @pre getSelectedItemTag() != null
 	 * @post !getItemManager().contains(old(getView().getSelectedItem().getTag()))
 	 */
 	@Override
 	public void removeItem() {
 		if (!canRemoveItem()) {
 			throw new IllegalStateException("Unable to remove Item");
 		}
 
 		ItemData itemData = getView().getSelectedItem();
 		if (itemData == null)
 			throw new NullPointerException("ItemData object should not be null");
 		Item item = (Item) itemData.getTag();
 		if (item == null)
 			throw new NullPointerException("Item object should not be null");
 		Product itemsProduct = item.getProduct();
 		if (itemsProduct == null)
 			throw new NullPointerException("Item should always have a product");
 		ItemManager itemManager = getItemManager();
 		ProductContainer container = item.getContainer();
 		container.remove(item, itemManager);
 
 		// update view
 		Set<Item> pcItems = container.getItemsForProduct(itemsProduct);
 		ItemData[] viewItems = new ItemData[pcItems.size()];
 		Iterator<Item> it = pcItems.iterator();
 		int counter = 0;
 		while (it.hasNext())
 			viewItems[counter++] = DataWrapper.wrap(it.next());
 		getView().setItems(viewItems);
 	}
 
 	/**
 	 * This method is called when the user selects the "Remove Items" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canRemoveItems())
 	 * 
 	 * @pre canRemoveItems()
 	 * @post itemManager (from getItemManager() ) no longer contains any of the items matching
 	 *       those removed by the user.
 	 */
 	@Override
 	public void removeItems() {
 		if (!canRemoveItems()) {
 			throw new IllegalStateException("Unable to remove Items");
 		}
 		getView().displayRemoveItemBatchView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Transfer Items" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canTransferItems())
 	 * 
 	 * @pre canTransferItems()
 	 * @post true
 	 */
 	@Override
 	public void transferItems() {
 		if (!canTransferItems()) {
 			throw new IllegalStateException("Unable to edit Storage Unit");
 		}
 		getView().displayTransferItemBatchView();
 	}
 
 	private void deleteSelectedProductContainer() {
 		ProductContainer selectedSU = getSelectedProductContainerTag();
 		assert (selectedSU != null);
 
 		getProductContainerManager().unmanage(selectedSU);
 	}
 
 	private Item getSelectedItemTag() {
 		ItemData selectedItem = getView().getSelectedItem();
 		assert (selectedItem != null);
 
 		Item selectedTag = (Item) selectedItem.getTag();
 		assert (selectedTag != null);
 
 		return selectedTag;
 	}
 
 	private ProductContainer getSelectedProductContainerTag() {
 		ProductContainerData selectedPC = getView().getSelectedProductContainer();
 		assert (selectedPC != null);
 
 		ProductContainer selectedTag = (ProductContainer) selectedPC.getTag();
 		assert (selectedTag != null);
 
 		return selectedTag;
 	}
 
 	private Product getSelectedProductTag() {
 		ProductData selectedProduct = getView().getSelectedProduct();
 		assert (selectedProduct != null);
 
 		Product selectedTag = (Product) selectedProduct.getTag();
 		assert (selectedTag != null);
 
 		return selectedTag;
 	}
 
 	private ProductContainerData loadProductContainerData(ProductContainerData parentData,
 			ProductContainer container) {
 		ProductContainerData pcData = new ProductContainerData(container.getName());
 		pcData.setTag(container);
 		parentData.addChild(pcData);
 		Iterator<ProductGroup> productGroupIterator = container.getProductGroupIterator();
 		while (productGroupIterator.hasNext()) {
 			ProductGroup child = productGroupIterator.next();
 			pcData = loadProductContainerData(pcData, child);
 		}
 		return parentData;
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
 		return;
 	}
 
 	/**
 	 * Returns a reference to the view for this controller.
 	 */
 	@Override
 	protected IInventoryView getView() {
 		return (IInventoryView) super.getView();
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
 		ProductContainerData root = new ProductContainerData();
 		root.setTag(null);
 		Iterator<StorageUnit> storageUnitIterator = getProductContainerManager()
 				.getStorageUnitIterator();
 		while (storageUnitIterator.hasNext()) {
 			ProductContainer pc = storageUnitIterator.next();
 			root = loadProductContainerData(root, pc);
 		}
 		getView().setProductContainers(root);
 	}
 }
