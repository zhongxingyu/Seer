 package spring.petstore.ui.dwr.service;
 
 import java.util.Iterator;
 import java.util.List;
 
 import spring.petstore.core.model.Category;
 import spring.petstore.core.model.Item;
 import spring.petstore.core.service.SpringPetsService;
 import spring.petstore.ui.dwr.model.Cart;
 import spring.petstore.ui.dwr.model.CartItem;
 import spring.petstore.ui.dwr.model.ItemHistory;
 
 public class SpringPetsDWRImpl implements SpringPetsService {
 
 	private SpringPetsService coreService;
 	private Cart cart;
 	private ItemHistory itemHistory;
 
 	public SpringPetsDWRImpl() {
 	}
 
 	/**
 	 * @return
 	 * @see spring.petstore.core.service.SpringPetsService#getCategories()
 	 */
 	public List<Category> getCategories() {
 		return coreService.getCategories();
 	}
 
 	/**
 	 * @param itemId
 	 * @return
 	 * @see spring.petstore.core.service.SpringPetsService#getItem(long)
 	 */
 	public Item getItem(long itemId) {
 		Item item = coreService.getItem(itemId);
 		addHistory(item);
 		return item;
 	}
 
 	/**
 	 * @param categoryId
 	 * @return
 	 * @see spring.petstore.core.service.SpringPetsService#getItems(long)
 	 */
 	public List<Item> getItems(long categoryId) {
 		return coreService.getItems(categoryId);
 	}
 
 	public Cart addItem(long itemId) {
 
 		List<CartItem> items = cart.getItems();
 		CartItem cartItem = null;
 		for (CartItem curCartItem : items) {
 			if (curCartItem.getItem().getId() == itemId) {
 				cartItem = curCartItem;
 				curCartItem.addItem();
 			}
 		}
 		if (cartItem == null) {
 			Item item = coreService.getItem(itemId);
 			cartItem = new CartItem();
 			cartItem.setItem(item);
 			cartItem.setPrice(10.5f);
 			cartItem.addItem();
 			cart.addItem(cartItem);
 		}
 
 		return cart;
 	}
 
 	public Cart getCart() {
 		return cart;
 	}
 
 	public Cart removeCartItem(long itemId) {
 
 		List<CartItem> items = cart.getItems();
 		for (Iterator<CartItem> iter = items.iterator(); iter.hasNext();) {
 			CartItem cartItem = iter.next();
 			if (cartItem.getItem().getId() == itemId) {
 				iter.remove();
 				break;
 			}
 		}
 		return cart;
 	}
 
 	private void addHistory(Item item) {
 		itemHistory.addHistory(item);
 	}
 
 	public ItemHistory getItemHistory() {
 		return itemHistory;

 	}
 
 	/**
 	 * @param coreService
 	 *            the coreService to set
 	 */
 	public void setCoreService(SpringPetsService coreService) {
 		this.coreService = coreService;
 	}
 
 	/**
 	 * @param cart
 	 *            the cart to set
 	 */
 	public void setCart(Cart cart) {
 		this.cart = cart;
 	}
 
 	/**
 	 * @param itemHistory
 	 *            the itemHistory to set
 	 */
 	public void setItemHistory(ItemHistory itemHistory) {
 		this.itemHistory = itemHistory;
 	}
 
 }
