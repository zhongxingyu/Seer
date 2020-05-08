 package ku.mobilepos.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Cart {
 	private static Cart cart = null;
 	private List<Item> itemInCart = null;
 	private double totalSale;
 	private Cart(){
 		
 	}
 	
 	public static Cart getCartInstance() {
 		if (cart == null) cart = new Cart();
 		return cart;
 	}
 	
 	public List<Item> getItemListInCart() {
 		if (itemInCart == null) itemInCart = new ArrayList<Item>();
 		return itemInCart;
 	}
 	
 	public void addToCart(Item item) {
 		itemInCart.add(item);
 	}
 	
 	public void removeItem(int position) {
 		itemInCart.remove(position);
 	}
 	
 	public void resetCart(){
 		this.cart = null;
 	}
 	
	public void calculateSale(){
		for (int i = 0; i < itemInCart.size(); i++) {
			totalSale += itemInCart.get(i).getItemPrice();
		}
 	}
 	
 	public double getTotalSale(){
 		return totalSale;
 	}
 	
 
 }
