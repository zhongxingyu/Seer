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
		calculateSale(item);
 	}
 	
 	public void removeItem(int position) {
 		itemInCart.remove(position);
 	}
 	
 	public void resetCart(){
		this.totalSale = 0.0;
		this.itemInCart = null;
 		this.cart = null;
 	}
 	
	public void calculateSale(Item item){
		
			totalSale += item.getItemPrice();
		
 	}
 	
 	public double getTotalSale(){
 		return totalSale;
 	}
 	
 
 }
