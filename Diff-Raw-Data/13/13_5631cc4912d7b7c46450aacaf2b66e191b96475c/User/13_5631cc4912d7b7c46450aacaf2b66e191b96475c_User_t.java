 package ca.ubc.cpsc310.gitlab.client.user;
 
 import java.util.ArrayList;
 
 import java.util.List;
 import java.util.HashMap;
 
 import java.util.Map;
 
 import ca.ubc.cpsc310.gitlab.client.products.ProductItem;
 
 public class User implements IUser {
 
 	
 	private static final long serialVersionUID = -4678920906536621479L;
 	
 	private List<ProductItem> shoppingCart = new ArrayList<ProductItem>();
 	private List<ProductItem> wishList = new ArrayList<ProductItem>();
 	
 	private String name;
 	private String language;
 
 	
 	public User()
 	{
 		
 	}
 	
 	@Override
 	public String getLanguage() {
 //<<<<<<< HEAD
 //<<<<<<< HEAD
 //		return (String) data.get(LANG);
 //=======
 //		return (String) data.get(LANG);
 //=======
 //>>>>>>> 12da3a7f614bd70de07e340c26ab60eafcb217bf
		omg sdfsdfasdf
 		return language;
 	}
 
 	@Override
 	public String getName() {
 //<<<<<<< HEAD
 //<<<<<<< HEAD
 //		return (String) data.get(NAME);
 //=======
 //		return (string) data.get(NAME);
 //=======
 //>>>>>>> 12da3a7f614bd70de07e340c26ab60eafcb217bf
		omg sdfsdfs
 		return name;
 	}
 
 	@Override
 	public List<ProductItem> getWishList() {
		omg sfsdfsdfs
 		return wishList;
 	}
 
 	@Override
 	public List<ProductItem> getShoppingCart() {
		omg sfsfsdfd
 		return shoppingCart;
 	}
 
 	@Override
 	public void setLanguage(String language) {
		omg sfsdfsdgs
 		this.language = language;
 		
 	}
 
 	@Override
 	public void setName(String name) {
		omg sdfsdfbsdb
 		this.name = name;
 		
 	}
 
 	@Override
 	public void addItemToWishList(ProductItem o) {
		dsfnsdofnsodno
 		this.wishList.add(o);
 		
 	}
 	@Override
 	
 	public void removeItemFromWishList(ProductItem o) {
 		this.wishList.remove(o);
 	}
 	
 	@Override
 	public void addItemToShoppingCart(ProductItem o) {
 		this.shoppingCart.add(o);
 	}
 
 	@Override
 	public void removeItemFromShoppingCart(ProductItem o) {
 		this.shoppingCart.remove(o);
 	}
 }
