 package model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Collection;
 
 /**
  * @author Clayton Watts
  *
  */
 @SuppressWarnings("serial")
 public class ConcreteProductManager implements ProductManager, Serializable {
 	private Collection<Product> products;
 	private Map<String, Product> barcodesToProducts;
 	
 	/** Creates an empty ConcreteProductManager
 	 */
 	public ConcreteProductManager() {
 		products = new ArrayList<Product>();
 		barcodesToProducts = new TreeMap<String, Product>();
 	}
 	
 	/** Adds the given product to this Manager's indices
 	 * @param product Product to manage
 	 * 
 	 * @pre product != null
 	 * @post contains(product) == true
 	 * @post getBy(product.getBarcode()) == product
 	 */
 	@Override
 	public void manage(Product product) {
 		assert(product != null);
 		
 		products.add(product);
 		barcodesToProducts.put(product.getBarcode(), product);
 	}
 	
 	/** Removes the given product from this Manager's indices
 	 * @param product Product to unmanage
 	 * 
 	 * @pre product != null
 	 * @post contains(product) == false
 	 * @post getBy(product.getBarcode()) == null
 	 */
 	@Override
 	public void unmanage(Product product) {
 		assert(product != null);
 		
 		products.remove(product);
 		barcodesToProducts.remove(product.getBarcode());
 	}
 	
 	/** Looks up a product by the given barcode
 	 * @param barcode The barcode of the product to return
 	 * @return The product with the given barcode
 	 * 
 	 * @pre barcode != null
 	 */
 	public Product getBy(String barcode) {
 		assert(barcode != null);
 		
 		return barcodesToProducts.get(barcode);
 	}
 	
 	/**
 	 * Checks if the identified Product exists in the home inventory system.
	 * @param product The Product to check
	 * @return true if the product exists in the home inventory system, 
	 * 		false otherwise.
 	 * 
 	 * @pre product != null
 	 */
 	@Override
 	public boolean contains(Product product) {
 		assert(product != null);
 		
 		return products.contains(product);
 	}
 	
 }
