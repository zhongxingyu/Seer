 package model;
 
 /**
  * Product Group class. Inherits from ProductContainer.
  * 
  * @author - Matt Hess
  * @version 1.0 - Snell 340 Group 4 Phase 1
  * 
  * @invariant container != null
  */
 @SuppressWarnings("serial")
 public class ProductGroup extends ProductContainer {
 	private ProductQuantity threeMonthSupply;
 	private ProductContainer container;
 	private Unit groupUnit;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param pcName
 	 *            - String name of the product group.
 	 * @param tmSupply
 	 *            - the three-month supply (ProductQuantity) to be specified for this object
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public ProductGroup(String pcName, ProductQuantity tmSupply, Unit groupUnit,
 			ProductContainer parent, ProductContainerManager manager) {
 		super(pcName, manager);
 		container = parent;
 		container.add(this);
 		setThreeMonthSupply(tmSupply);
 		this.groupUnit = groupUnit;
 		manager.manage(this);
 	}
 
 	/**
 	 * Determines whether the specified Product can be added to this Product Group.
 	 * 
 	 * @param productBarcode
 	 *            the Product barcode to check
 	 * @return true if it can be added, false otherwise
 	 * @pre productBarcode != null
 	 * @post true
 	 */
 	@Override
 	public boolean canAddProduct(String productBarcode) {
 		if (productBarcode == null) {
 			throw new NullPointerException("Null String productBarcode");
 		}
 
 		// A Product may appear at most once in a given Storage Unit.
 		if (containsProduct(productBarcode))
 			return false;
 
 		// We need to ask daddy (the Storage Unit) if the Product exists
 		// elsewhere in his children.
 		ProductContainer parent = container;
 		while (parent instanceof ProductGroup) {
 			parent = ((ProductGroup) parent).container;
 		}
 
 		return !parent.hasDescendantProduct(productBarcode);
 	}
 
 	/**
 	 * Allows the name and three month supply of a productContainer to be modified. Notifies
 	 * manager a change took place. If either is null, they will not be updated.
 	 * 
 	 * @param newName
 	 *            The new name of the ProductGroup. If null, no change to the name will take
 	 *            place
 	 * 
 	 * @param newTMS
 	 *            The new value for ProductQuantity. If null, no change to the threeMonthSupply
 	 *            will take place.
 	 * @pre newName != ""
 	 * @post value of this.name equals newName
 	 * @post this.threeMonthSupply equals newTMS
 	 */
 	public void edit(String newName, ProductQuantity newTMS) {
 
 		if (newTMS != null)
 			setThreeMonthSupply(newTMS);
 		String oldName = getName();
 		super.edit(newName);
 		container.updateChildProductGroup(oldName, this);
 	}
 
 	/**
 	 * Gets this ProductGroup's parent container
 	 * 
 	 * @return this ProductGroup's container
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public ProductContainer getContainer() {
 		return container;
 	}
 
 	/**
 	 * Gets the current supply of items in this ProductGroup.
 	 * 
 	 * @return the current supply of items in this ProductGroup
 	 */
 	public ProductQuantity getCurrentSupply() {
 		if (threeMonthSupply.getUnits() == Unit.COUNT) {
 			return new ProductQuantity(getItemsSizeRecursive(), Unit.COUNT);
 		}
 
 		return getCurrentSupplyRecursive(threeMonthSupply.getUnits());
 	}
 
	public ProductQuantity getNMonthSupply(int months) {
		return threeMonthSupply.multiplyBy(months / 3.0f);
	}

 	public StorageUnit getRoot() {
 		ProductContainer parent = container;
 		while (parent instanceof ProductGroup) {
 			parent = ((ProductGroup) parent).container;
 		}
 		return (StorageUnit) parent;
 	}
 
 	/**
 	 * Attribute getter - threeMonthSupply
 	 * 
 	 * @return ProductQuantity - the three-month supply
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public ProductQuantity getThreeMonthSupply() {
 		return threeMonthSupply;
 	}
 
 	/**
 	 * Determines whether a given ProductQuantity is a valid three-month supply
 	 * 
 	 * @param threeMonthSupply
 	 *            - the ProductQuantity to test
 	 * @return true if the ProductQuantity is valid, false otherwise.
 	 * 
 	 * @pre threeMonthSupply != null
 	 * @post true
 	 * 
 	 */
 	public boolean isValidThreeMonthSupply(ProductQuantity threeMonthSupply) {
 		// From the Data Dictionary:
 		// The magnitude can be any non-negative float value. Zero means
 		// "unspecified". The unit of measurement can be any of the following:
 		// count, pounds, ounces, grams, kilograms, gallons, quarts, pints,
 		// fluid ounces, liters. If the unit of measurement is count , the
 		// magnitude must be an integer (i.e., no fraction).
 		if (threeMonthSupply == null) {
 			throw new NullPointerException("Null ProductQuantity threeMonthSupply");
 		}
 
 		return ProductQuantity.isValidProductQuantity(threeMonthSupply.getQuantity(),
 				threeMonthSupply.getUnits());
 	}
 
 	/**
 	 * Sets this ProductGroup's parent container to the specified container.
 	 * 
 	 * @param container
 	 *            the new parent container
 	 * 
 	 * @pre container != null
 	 * @post this.container == container
 	 */
 	public void setContainer(ProductContainer container) {
 		// From the Data Dictionary:
 		// Must be non-empty. A Product Group is always contained within one
 		// Product Container.
 		if (container == null) {
 			throw new NullPointerException("Null ProductContainer container");
 		}
 
 		this.container = container;
 	}
 
 	/**
 	 * Sets this ProductGroup's three-month supply
 	 * 
 	 * @param threeMonthSupply
 	 *            - the new quantity for the three-month supply
 	 * 
 	 * @pre true
 	 * @post this.threeMonthSupply.equals(threeMonthSupply)
 	 */
 	public void setThreeMonthSupply(ProductQuantity threeMonthSupply) {
 		if (!isValidThreeMonthSupply(threeMonthSupply)) {
 			throw new IllegalStateException("Invalid Product Quantity for Product Group");
 		}
 		this.threeMonthSupply = threeMonthSupply;
 		groupUnit = threeMonthSupply.getUnits();
 	}
 
 	private ProductQuantity getCurrentSupplyRecursive(Unit unit) {
 		ProductQuantity sum = new ProductQuantity(0, unit);
 		for (Item item : getItems()) {
 			try {
 				sum.add(item.getProduct().getProductQuantity());
 			} catch (IllegalArgumentException e) {
 				// Ignore items of incompatible product types
 			}
 		}
 
 		for (ProductGroup productGroup : getProductGroups()) {
 			sum.add(productGroup.getCurrentSupplyRecursive(unit));
 		}
 
 		return sum;
 	}
 }
