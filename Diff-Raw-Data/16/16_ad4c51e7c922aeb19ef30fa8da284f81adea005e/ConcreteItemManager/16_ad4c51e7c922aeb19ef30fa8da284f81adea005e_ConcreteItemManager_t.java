 package model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 @SuppressWarnings("serial")
 /** Class that manages Items 
  * 
  * @author Clayton Watts
  * @version 1.0 -- CS 340 Snell Phase 1
  * 
  * @invariant items != null
  * @invariant removedItems != null
  * @invariant productsToItems != null
  *
  */
 public class ConcreteItemManager implements ItemManager, Serializable {
 	private Collection<Item> items;
 	private Collection<Item> removedItems;
 	private Map<Product, Collection<Item>> productsToItems;
 	
 	/** Constructor
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public ConcreteItemManager() {
 		items = new ArrayList<Item>();
 		removedItems = new TreeSet<Item>();
 		productsToItems = new TreeMap<Product, Collection<Item>>();
 	}
 	
 	/** Adds the given item to this Manager's indexes
 	 * @param item Item to manage
 	 * 
 	 * @pre item != null
 	 * @post items.contains(item)
 	 * @post productHasItems(item.getProduct()) != null
 	 * @post !productHasItems(item.getProduct()).isEmpty()
 	 * 
 	 */
 	@Override
 	public void manage(Item item) {
 		Collection<Item> found = productsToItems.get(item.getProduct());
 		if(found != null)
 			found.add(item);
 		else {
 			TreeSet<Item> prItems = new TreeSet<Item>();
 			prItems.add(item);
 			productsToItems.put(item.getProduct(),prItems);
 		}
 		
 		items.add(item);
 	}
 	
 	/** Adds the specified item to a productContainer
 	 * @param item		The Item to add
 	 * @param container	The ProductContainer to add the Item to
 	 * 
 	 * @pre item != null
 	 * @pre productsToItems.get(item.getProduct()).contains(item)
 	 * @post removedItems.contains(item)
	 * 
 	 */
 	@Override
 	public void unmanage(Item item) {
 		assert(item != null);
 		
 		Collection<Item> found = productsToItems.get(item.getProduct());
 		found.remove(item);
 		if(found.isEmpty()) 
 			productsToItems.remove(item.getProduct());
 		
 		item.remove();
 		removedItems.add(item);
 	}	
 
 	/** Returns an Iterator to access all of the removed items.
 	 * @return	an Iterator allowing access to all of the removed Items
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public Iterator<Item> removedItemsIterator() {
 		return removedItems.iterator();
 	}
 
 	/** Determines whether a given product has items in the system.
 	 * @param product		The Product to test
 	 * @return				true if the specified product has items that use it, false otherwise
 	 * 
 	 * @pre product != null
 	 * @post true
 	 */
 	public boolean productHasItems(Product product) {
		if(product == null)
			throw new IllegalArgumentException("Null product given to find");
		
 		Collection<Item> found = productsToItems.get(product);
 		if(found == null)
 			return false;
 		
 		return !found.isEmpty();
 	}
 
 }
