 package org.xcube.nfc.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ItemsWithQuantities {
 	private List<ItemWithQuantity> items = new ArrayList<ItemWithQuantity>();
 
 	/**
 	 * Add item, incrementing quantity if item is already in list
 	 * @param item
 	 * @return
 	 */
 	public int addItem(Item item) {
 		ItemWithQuantity existingItem = find(item);
 		if (null == existingItem) {
 			existingItem = new ItemWithQuantity(item);
 			items.add(existingItem);
 			
 		} else {
 			existingItem.incQuantity();
 		}
 		
 		return existingItem.getQuantity();
 	}
 	
 	public int addItem(ItemWithQuantity iq) {
 		ItemWithQuantity existingItem = find(iq.getItem());
 		if (null == existingItem) {
 			// create a new ItemWithQuantity to isolate us from changes to the basket
 			 existingItem = new ItemWithQuantity(iq.getItem(), iq.decQuantity());
 			items.add(existingItem);
 		} else {
 			existingItem.setQuantity(existingItem.getQuantity() + iq.getQuantity());
 		}
 		
 		return existingItem.getQuantity();
 	}
 	
 	/**
 	 * decrement quantity of item and remove if quantity falls to zero
 	 * @param item
 	 * @return
 	 */
 	public int removeItem(Item item) {
 		ItemWithQuantity existingItem = find(item);
 		if (null == existingItem) {
 			return 0;
 			
 		} else {
 			existingItem.decQuantity();
			if (0 == existingItem.getQuantity()) {
 				items.remove(existingItem);
 			}
 		}
 		
 		return existingItem.getQuantity();
 	}
 	
 	public boolean hasItem(Item item) {
 		return null != find(item);
 	}
 	
 	protected ItemWithQuantity find(Item item) {
 		return findByUpc(item.getUpc());
 	}
 	
 	public ItemWithQuantity findByUpc(String upc) {
 		for (ItemWithQuantity iq : items) {
 			if (iq.getItem().getUpc().equals(upc)) {
 				return iq;
 			}
 		}
 		return null;
 	}
 	
 	public List<ItemWithQuantity> getItems() {
 		return items;
 	}
 
 	public void setItems(List<ItemWithQuantity> items) {
 		this.items = items;
 	}
 	
 	
 }
