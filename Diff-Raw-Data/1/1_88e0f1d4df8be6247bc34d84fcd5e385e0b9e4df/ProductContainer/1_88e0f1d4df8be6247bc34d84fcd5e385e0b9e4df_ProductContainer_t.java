 package model;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import model.Action.ActionType;
 import model.visitor.InventoryVisitable;
 import model.visitor.InventoryVisitor;
 
 import common.NonEmptyString;
 
 /**
  * Product Container class: Represents an object that can hold various types of items,
  * products, and product groups.
  * 
  * @author Matt Hess
  * @version 1.0 - Snell 340 Group 4 Phase 1
  * 
  * @invariant name != null
  * @invariant !name.equals("")
  * @invariant items != null
  * @invariant products != null
  * @invariant productGroups != null
  */
 @SuppressWarnings("serial")
 public abstract class ProductContainer implements Comparable<ProductContainer>, Serializable,
 		InventoryVisitable {
 	private NonEmptyString name;
 	private final Map<String, Item> items;
 	private final Map<String, Product> products;
 	private final Map<String, ProductGroup> productGroups;
 	// TODO: Implement this map for all descendant nodes
 	private final Map<Product, Set<Item>> productsToItems;
 	protected final ProductContainerManager manager;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param name
 	 *            - the name of the Product Container
 	 * 
 	 * @pre NonEmptyString.isValid(name)
 	 * @pre manager != null
 	 * @post true
 	 * 
 	 */
 	public ProductContainer(String name, ProductContainerManager manager) {
 		if (!NonEmptyString.isValid(name)) {
 			throw new IllegalArgumentException("Invalid Name name");
 		}
 		if (manager == null) {
 			throw new NullPointerException("Null Manager manager");
 		}
 
 		this.name = new NonEmptyString(name);
 		items = new TreeMap<String, Item>();
 		productGroups = new TreeMap<String, ProductGroup>();
 		products = new TreeMap<String, Product>();
 		productsToItems = new TreeMap<Product, Set<Item>>();
 		this.manager = manager;
 	}
 
 	@Override
 	public void accept(InventoryVisitor visitor) {
 		visitor.visit(this);
 
 		for (Product product : getProducts()) {
 			product.accept(visitor);
 
 			for (Item item : getItemsForProduct(product)) {
 				item.accept(visitor);
 			}
 		}
 
 		for (ProductGroup child : getProductGroups()) {
 			child.accept(visitor);
 		}
 	}
 
 	/**
 	 * Method that adds an Item to the collection.
 	 * 
 	 * @param i
 	 *            - the Item object to add to the collection
 	 * @return true if the item was added to this container or one of its children, false
 	 *         otherwise.
 	 * 
 	 * @pre i != null
 	 * @pre !items.containsKey(i.getBarcode())
 	 * @post items.size() == items.size()@pre + 1
 	 * @post items.contains(i)
 	 * 
 	 */
 	public boolean add(Item i) {
 		if (i == null) {
 			throw new NullPointerException("Null Item i");
 		}
 		if (items.containsKey(i.getBarcode()))
 			throw new IllegalStateException("Cannot have two items with same barcode");
 
 		// A new item is added to the same Product Container that contains the Item's Product
 		// within the target Storage Unit
 		if (!contains(i.getProduct())) {
 			for (ProductGroup productGroup : productGroups.values()) {
 				if (productGroup.add(i)) {
 					return true;
 				}
 			}
 			if (this instanceof ProductGroup)
 				return false;
 		}
 		// This container either contains the Item's Product or is the storage unit; add it
 		// here.
 		// Product not found anywhere else; add Item here
 		if (canAddProduct(i.getProduct().getBarcode()))
 			add(i.getProduct());
 		registerItem(i);
 		return true;
 	}
 
 	/**
 	 * Method that adds a Product to the collection.
 	 * 
 	 * @param p
 	 *            - the Product object to add to the collection
 	 * 
 	 * @pre p != null
 	 * @pre canAddProduct(p.getBarcode())
 	 * @post products.size() == products.size()@pre + 1
 	 * @post products.contains(p)
 	 * 
 	 */
 	public void add(Product p) {
 		if (p == null) {
 			throw new NullPointerException("Null Product p");
 		}
 		if (!canAddProduct(p.getBarcode()))
 			throw new IllegalStateException(
 					"Cannot add two products of the same name into a single parent container");
 		products.put(p.getBarcode(), p);
 		productsToItems.put(p, new TreeSet<Item>());
 		if (!p.hasContainer(this))
 			p.addContainer(this);
 	}
 
 	/**
 	 * Determines whether the specified Product can be added to this Storage Unit.
 	 * 
 	 * @param productBarcode
 	 *            the Product barcode to check
 	 * @return true if it can be added, false otherwise
 	 * 
 	 * @pre productBarcode != null
 	 * @post true
 	 */
 	public boolean canAddProduct(String productBarcode) {
 		if (productBarcode == null) {
 			throw new NullPointerException("Null String productBarcode");
 		}
 		// A Product may appear at most once in a given Storage Unit.
 		if (containsProduct(productBarcode))
 			return false;
 		for (ProductGroup productGroup : productGroups.values()) {
 			if (productGroup.hasDescendantProduct(productBarcode))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Determines whether the specified ProductGroup can be added to this ProductContainer.
 	 * 
 	 * @param productGroupName
 	 *            - the ProductGroup name to test
 	 * @return true if the ProductGroup can safely be added, false otherwise.
 	 * 
 	 * @pre productGroupName != null
 	 * @post true
 	 */
 	public boolean canAddProductGroup(String productGroupName) {
 		if (productGroupName == null) {
 			throw new NullPointerException("Null String productGroupName");
 		}
 		return !productGroupName.equals("") && !containsProductGroup(productGroupName);
 	}
 
 	/**
 	 * Determines if this ProductContainer can be removed from the system.
 	 * 
 	 * @return true if this ProductContainer can be removed, false otherwise.
 	 */
 	public boolean canRemove() {
 		if (!items.isEmpty()) {
 			return false;
 		}
 
 		for (Product product : products.values()) {
 			if (!product.canRemove()) {
 				return false;
 			}
 		}
 
 		for (ProductGroup productGroup : productGroups.values()) {
 			if (!productGroup.canRemove()) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Determines whether we can remove a given Product from this ProductContainer.
 	 * 
 	 * @param product
 	 *            - the Product object to be removed from the container
 	 * 
 	 * @pre product != null
 	 * @post true
 	 */
 	public boolean canRemove(Product product) {
 		if (product == null) {
 			throw new NullPointerException("Null Product product");
 		}
 		if (productsToItems.get(product) == null)
 			return true;
 		return (productsToItems.get(product).isEmpty());
 	}
 
 	/**
 	 * Compares this ProductContainer to another
 	 * 
 	 * @param su
 	 * @return 0 if equal, some other integer representing the comparison otherwise
 	 * @pre pc != null
 	 * @post true
 	 */
 	@Override
 	public int compareTo(ProductContainer pc) {
 		if (pc == null) {
 			throw new NullPointerException("Null ProductContainer pc");
 		}
 		return name.compareTo(pc.name);
 	}
 
 	/**
 	 * Determines whether this Product Container contains a specific Item
 	 * 
 	 * @param item
 	 *            the Item to check
 	 * @return true if this Product Container contains the Item, false otherwise
 	 * 
 	 * @pre item != null
 	 * @post true
 	 */
 	public boolean contains(Item item) {
 		if (item == null) {
 			throw new NullPointerException("Null Item item");
 		}
 		if (items.containsKey(item.getBarcode()))
 			return true;
 
 		for (ProductGroup pGroup : productGroups.values()) {
 			if (pGroup.contains(item))
 				return true;
 		}
 		return false;
 
 	}
 
 	/**
 	 * Determines whether this Product Container contains a specific Product
 	 * 
 	 * @param product
 	 *            the Product to check
 	 * @return true if this Product Container contains the Product, false otherwise
 	 * 
 	 * @pre product != null
 	 * @post true
 	 * 
 	 */
 	public boolean contains(Product product) {
 		if (product == null) {
 			throw new NullPointerException("Null Product product");
 		}
 		return products.containsKey(product.getBarcode());
 	}
 
 	/**
 	 * Determines whether a ProductGroup with the same name as the given ProductGroup is a
 	 * child of this ProductContainer
 	 * 
 	 * @param productGroupName
 	 *            - the ProductGroup to test
 	 * @return true if the ProductGroup exists in this ProductContainer, false otherwise.
 	 * 
 	 * @pre productGroup != null
 	 * @post true
 	 */
 	public boolean contains(ProductGroup productGroup) {
 		if (productGroup == null) {
 			throw new NullPointerException("Null ProductGroup productGroup");
 		}
 		return productGroups.containsKey(productGroup.getName());
 	}
 
 	/**
 	 * Determines whether the specified ProductGroup is contained in this ProductContainer.
 	 * 
 	 * @param productGroupName
 	 *            - the ProductGroup to test
 	 * @return true if the ProductGroup exists in this ProductContainer, false otherwise.
 	 * 
 	 * @pre productGroup != null
 	 * @post true
 	 */
 	public boolean containsExactProductGroup(ProductGroup productGroup) {
 		if (productGroup == null) {
 			throw new NullPointerException("Null ProductGroup productGroup");
 		}
 		for (ProductGroup group : productGroups.values()) {
 			if (group.equals(productGroup))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Determines whether this Product Container contains a specific Item
 	 * 
 	 * @param barcode
 	 *            the barcode of the Item to check
 	 * @return true if this Product Container contains the Item, false otherwise
 	 * 
 	 * @pre barcode != null
 	 * @post true
 	 * 
 	 */
 	public boolean containsItem(String barcode) {
 		if (barcode == null) {
 			throw new NullPointerException("Null String barcode");
 		}
 		return items.containsKey(barcode);
 	}
 
 	/**
 	 * Determines whether this Product Container contains a specific Product
 	 * 
 	 * @param productBarcode
 	 *            the Product's barcode to check
 	 * @return true if this Product Container contains the Product, false otherwise
 	 * 
 	 * @pre productBarcode != null
 	 * @post true
 	 * 
 	 */
 	public boolean containsProduct(String productBarcode) {
 		if (productBarcode == null) {
 			throw new NullPointerException("Null String productBarcode");
 		}
 		return products.containsKey(productBarcode);
 	}
 
 	/**
 	 * Determines whether the specified ProductGroup is contained in this ProductContainer.
 	 * 
 	 * @param productGroupName
 	 *            - the ProductGroup to test
 	 * @return true if the ProductGroup exists in this ProductContainer, false otherwise.
 	 * 
 	 * @pre productGroupName != null
 	 * @post true
 	 */
 	public boolean containsProductGroup(ProductGroup pg) {
 		if (pg == null) {
 			throw new NullPointerException("Null productGroup");
 		}
 		Collection<ProductGroup> groups = productGroups.values();
 		for (ProductGroup group : groups) {
 			if (group.equals(pg))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Determines whether the specified ProductGroup is contained in this ProductContainer.
 	 * 
 	 * @param productGroup
 	 *            - the ProductGroup to test
 	 * @return true if the ProductGroup exists in this ProductContainer, false otherwise.
 	 * 
 	 * @pre productGroup != null
 	 * @post true
 	 */
 	public boolean containsProductGroup(String productGroupName) {
 		if (productGroupName == null) {
 			throw new NullPointerException("Null String productGroupName");
 		}
 		return productGroups.containsKey(productGroupName);
 	}
 
 	/**
 	 * Allows the name of a productContainer to be modified. Notifies manager a change took
 	 * place.
 	 * 
 	 * @param newName
 	 *            The new name of the ProductGroup. If null, no change to the name will take
 	 *            place
 	 * 
 	 * @post value of this.name equals newName
 	 */
 	public void edit(String newName) {
 		if (newName != null && newName.length() > 0 && !getName().equals(newName))
 			setName(newName);
 		manager.notifyObservers(new Action(this, ActionType.EDIT));
 	}
 
 	/**
 	 * Finds the child ProductContainer that contains the given Product
 	 * 
 	 * @param p
 	 *            Product for which to find the ProductContainer
 	 * @return found ProductContainer, or null if the Product is not found in this tree
 	 */
 	public ProductContainer getContainerForProduct(Product p) {
 		if (this.contains(p))
 			return this;
 
 		for (ProductGroup group : productGroups.values()) {
 			if (group.getContainerForProduct(p) != null)
 				return group.getContainerForProduct(p);
 		}
 
 		return null;
 	}
 
 	public Collection<Item> getItems() {
 		return items.values();
 	}
 
 	/**
 	 * Gets the set of items in the container of Product p
 	 * 
 	 * @param p
 	 *            Product used to find items
 	 * @return Set<Item> containing Items where item.getProduct().equals(p)
 	 */
 	public Set<Item> getItemsForProduct(Product p) {
 		return productsToItems.get(p);
 	}
 
 	/**
 	 * Gets the size of the items collection.
 	 * 
 	 * @return int - the number of elements in the items collection.
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public int getItemsSize() {
 		return items.size();
 	}
 
 	/**
 	 * Gets the size of the items collection for this ProductContainer and all its children.
 	 * 
 	 * @return int - the number of elements in the items collection and all this Container's
 	 *         children's items collections
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public int getItemsSizeRecursive() {
 		int total = items.size();
 
 		for (ProductGroup productGroup : productGroups.values()) {
 			total += productGroup.getItemsSizeRecursive();
 		}
 
 		return total;
 	}
 
 	/**
 	 * Attribute getter - name
 	 * 
 	 * @return The String name of the ProductContainer
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public String getName() {
 		return name.toString();
 	}
 
 	/**
 	 * Finds and returns the requested Product object
 	 * 
 	 * @param barcode
 	 *            - the String barcode of the product to find
 	 * @return Product - the requested Product
 	 * 
 	 * @pre barcode != null
 	 * @post true
 	 * 
 	 */
 	public Product getProduct(String barcode) {
 		if (barcode == null) {
 			throw new NullPointerException("Null String barcode");
 		}
 
 		return products.get(barcode);
 	}
 
 	/**
 	 * Finds and returns the requested ProductGroup object
 	 * 
 	 * @param pgToFind
 	 *            - the name of the ProductGroup to find
 	 * @return ProductGroup - the requested ProductGroup
 	 * 
 	 * @pre pgToFind != null
 	 * @post true
 	 */
 	public ProductGroup getProductGroup(String pgToFind) {
 		if (pgToFind == null) {
 			throw new NullPointerException("Null String pgToFind");
 		}
 
 		return productGroups.get(pgToFind);
 	}
 
 	/**
 	 * Gets all of the child Product Groups for this Product Container.
 	 * 
 	 * @return an *unmodifiable* Collection of all of the child Product Groups
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public Collection<ProductGroup> getProductGroups() {
 		return Collections.unmodifiableCollection(productGroups.values());
 	}
 
 	/**
 	 * Gets the size of the productGroups collection
 	 * 
 	 * @return int - the number of elements in the pGroups collection.
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public int getProductGroupsSize() {
 		return productGroups.size();
 	}
 
 	/**
 	 * Gets all of the Products in this Product Container (non-recursive).
 	 * 
 	 * @return an *unmodifiable* Collection of all of the Products in this Product Container
 	 *         (non-recursive)
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public Collection<Product> getProducts() {
 		return Collections.unmodifiableCollection(products.values());
 	}
 
 	/**
 	 * Gets the size of the products collection.
 	 * 
 	 * @return int - the number of elements in the products collection.
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public int getProductsSize() {
 		return products.size();
 	}
 
 	/**
 	 * Determines if the given ProductContainer is a descendant of this ProductContainer or
 	 * returns true if this.equals(other), indicating that ProductContainer other is in this
 	 * ProductContainers same tree.
 	 * 
 	 * @param other
 	 *            ProductContainer to find in this tree
 	 * @return true if ProductContainer other is found as a descendant of this
 	 * 
 	 * @pre other != null
 	 * @post true
 	 * 
 	 */
 	public boolean hasChild(ProductContainer other) {
 		if (other instanceof StorageUnit)
 			return false;
 
 		if (equals(other))
 			return true;
 
 		ProductGroup group = (ProductGroup) other;
 		if (containsExactProductGroup(group)) {
 			return true;
 		}
 		for (ProductGroup productGroup : productGroups.values()) {
 			if (productGroup.hasChild(other))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Removes the specified item from this ProductContainer. Use this only
 	 * 
 	 * @param item
 	 *            the Item to be moved
 	 * @param destination
 	 *            the ProductContainer to move the item to
 	 * 
 	 * @pre item != null
 	 * @pre destination != null
 	 * @pre contains(item)
 	 * @pre !destination.contains(item)
 	 * @post !items.contains(item)
 	 * @post destination.contains(item)
 	 * 
 	 */
 	public void moveIntoContainer(Item item, ProductContainer destination) {
 		if (item == null) {
 			throw new NullPointerException("Null Item item");
 		}
 		if (destination == null) {
 			throw new NullPointerException("Null ProductContainer destination");
 		}
 		if (!contains(item)) {
 			throw new IllegalStateException(
 					"Item does not exist in this container; cannot move");
 		}
 		if (destination.contains(item)) {
 			throw new IllegalStateException(
 					"Destination container already contains the item to be moved");
 		}
 
 		Product product = item.getProduct();
 		ProductContainer containerInTree = getContainerForProduct(product);
 		containerInTree.unregisterItem(item);
 		destination.registerItem(item);
 		manager.notifyObservers(new Action(this, ActionType.MOVE));
 	}
 
 	public Item remove(Item item, ItemManager manager) {
 		return remove(item, manager, true);
 	}
 
 	/**
 	 * Removes the specified item from this ProductContainer.
 	 * 
 	 * @param item
 	 *            the Item to be removed
 	 * @param manager
 	 *            the ItemManager to notify of the removal. If null, no manager is notified.
 	 * 
 	 * @return the removed item
 	 * 
 	 * @pre item != null
 	 * @pre manager != null
 	 * @pre productsToItems.containsKey(item.getProduct())
 	 * @post !containsItem(item.getBarcode())
 	 */
 	public Item remove(Item item, ItemManager manager, boolean keepHistory) {
 		if (item == null) {
 			throw new NullPointerException("Null Item item");
 		}
 		if (manager == null) {
 			throw new NullPointerException("Null Manager manager");
 		}
 		if (!contains(item)) {
 			throw new IllegalStateException("ProductContainer does not contain Item item");
 		}
 
 		ProductContainer container = getContainerForProduct(item.getProduct());
 		container.unregisterItem(item);
 		if (keepHistory)
 			manager.unmanage(item);
 		else
 			manager.undoManage(item);
 		return item;
 	}
 
 	/**
 	 * Method that removes a Product object from the collection.
 	 * 
 	 * @param barcode
 	 *            - the String barcode of the Product object to be removed from the collection
 	 * @return the removed Product
 	 * 
 	 * @pre product != null
 	 * @pre canRemove(product)
 	 * @post !products.contains(product)
 	 */
 	public Product remove(Product product) {
 		if (product == null) {
 			throw new NullPointerException("Null Product product");
 		}
 		if (!canRemove(product))
 			throw new IllegalStateException(
 					"Cannot remove product; product container still has items that refer to it");
 		productsToItems.remove(product);
 		product.removeContainer(this);
 		return products.remove(product.getBarcode());
 	}
 
 	/**
 	 * Removes a specified ProductGroup from this container.
 	 * 
 	 * @param productGroup
 	 *            The ProductGroup to remove
 	 * 
 	 * @pre productGroup != null
 	 * @pre productGroup.canRemove()
 	 * @post !contains(productGroup)
 	 */
 	public void remove(ProductGroup productGroup) {
 		if (productGroup == null) {
 			throw new NullPointerException("Null ProductGroup productGroup");
 		}
 		if (!productGroup.canRemove()) {
 			throw new IllegalStateException("Cannot remove child product group");
 		}
 
 		if (containsExactProductGroup(productGroup)) {
 			productGroups.remove(productGroup.getName());
 		} else if (hasChild(productGroup)) {
 			// remove nested product group
 			for (ProductGroup group : productGroups.values()) {
 				if (group.containsExactProductGroup(productGroup)
 						|| group.hasChild(productGroup)) {
 					group.remove(productGroup);
 					return;
 				}
 			}
 
 		} else {
 			throw new RuntimeException(
 					"This Container doesn't have the given ProductContainer to remove");
 		}
 
 	}
 
 	/**
 	 * Sets this Container's name
 	 * 
 	 * @param name
 	 *            Name to set to
 	 * 
 	 * @pre manager.isValidStorageUnitName(name)
 	 * @post getName() == name
 	 * 
 	 */
 	public void setName(String name) {
 		if (!manager.isValidStorageUnitName(name)) {
 			throw new IllegalStateException(
 					"Invalid Storage Unit name. A Storage Unit with this name may already exist");
 		}
 
 		this.name = new NonEmptyString(name);
 	}
 
 	public void updateChildProductGroup(String oldName, ProductGroup p) {
 		productGroups.remove(oldName);
 		productGroups.put(p.getName(), p);
 	}
 
 	/**
 	 * Method that adds a ProductGroup object to the collection. This method should only be
 	 * called from the ProductGroup constructor.
 	 * 
 	 * @param productGroup
 	 *            - the ProductGroup object to add to the collection
 	 * 
 	 * @pre productGroup != null
 	 * @pre canAddProductGroup(productGroup.getName())
 	 * @post pGroups.size() == pGroups.size()@pre + 1
 	 * @post pGroups.contains(productGroup)
 	 * 
 	 */
 	protected void add(ProductGroup productGroup) {
 		if (productGroup == null) {
 			throw new NullPointerException("Null ProductGroup productGroup");
 		}
 		if (!canAddProductGroup(productGroup.getName()))
 			throw new IllegalStateException(
 					"Cannot add two product groups of the same name into a container");
 		productGroups.put(productGroup.getName(), productGroup);
 		productGroup.setContainer(this);
 	}
 
 	/**
 	 * Internal method: Used to determine whether it is possible to add a Product
 	 * 
 	 * @param productBarcode
 	 *            Product to look for
 	 * @return
 	 */
 	protected boolean hasDescendantProduct(String productBarcode) {
 		if (containsProduct(productBarcode)) {
 			return true;
 		}
 		for (ProductGroup productGroup : productGroups.values()) {
 			if (productGroup.hasDescendantProduct(productBarcode))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Helper Method for adding items to this productContainer or any of its children
 	 * (recursively). If no product matching i's product is found, no item is registered.
 	 * 
 	 * @param i
 	 *            Item to add
 	 * 
 	 * @return true if item is registered, false if otherwise.
 	 */
 	protected boolean recursivelyRegisterItem(Item i) {
 		if (productGroups == null)
 			return false;
 
 		if (containsProduct(i.getProductBarcode())) {
 			registerItem(i);
 			return true;
 		}
 
 		if (!productGroups.isEmpty()) {
 			for (ProductContainer container : productGroups.values()) {
 				return container.recursivelyRegisterItem(i);
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Helper method for adding items
 	 * 
 	 * @param i
 	 *            Item to add
 	 */
 	protected void registerItem(Item i) {
 		items.put(i.getBarcode(), i);
		products.put(i.getProductBarcode(), i.getProduct());
 		i.setContainer(this);
 		i.getProduct().addItem(i);
 		Set<Item> newItemsForProduct;
 		if (productsToItems.containsKey(i.getProduct())) {
 			newItemsForProduct = productsToItems.get(i.getProduct());
 		} else {
 			newItemsForProduct = new TreeSet<Item>();
 		}
 		newItemsForProduct.add(i);
 		productsToItems.put(i.getProduct(), newItemsForProduct);
 	}
 
 	/**
 	 * Helper method for moving / removing items
 	 * 
 	 * @param i
 	 *            Item to move/remove
 	 */
 	protected void unregisterItem(Item i) {
 		items.remove(i.getBarcode());
 		i.getProduct().removeItem(i);
 		Set<Item> newItemsForProduct;
 		if (productsToItems.containsKey(i.getProduct())) {
 			newItemsForProduct = productsToItems.get(i.getProduct());
 		} else {
 			newItemsForProduct = new TreeSet<Item>();
 		}
 		newItemsForProduct.remove(i);
 		productsToItems.put(i.getProduct(), newItemsForProduct);
 	}
 }
