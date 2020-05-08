 package ca.ubc.cpsc310.gitlab.client.user;
 
 import java.util.ArrayList;
 //<<<<<<< HEAD
 //import java.util.List;
 
 
 //import ca.ubc.cpsc310.gitlab.client.products.ProductItem;
 
 //@SuppressWarnings("unchecked")
 //public class User implements IUser {
 //
 //	/**
 //	 * 
 //	 */
 //	private static final long serialVersionUID = -6968277136462621810L;
 //	private final String LANG = "LANG";
 //	private final String NAME = "NAME";
 //	private final String WISHLIST = "WISHLIST";
 //	private final String SHOPPINGCART = "SHOPPINGCART";
 	
 //	private Map<String, Object> data = new HashMap<String, Object>();
 //	public User()
 //	{
 //		data.put(WISHLIST, new ArrayList<Object>());
 //		data.put(SHOPPINGCART, new ArrayList<Object>());
 //=======
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
 		
 //>>>>>>> async
 	}
 	
 	@Override
 	public String getLanguage() {
 //<<<<<<< HEAD
 //		return (String) data.get(LANG);
 //=======
 		return language;
 //>>>>>>> async
 	}
 
 	@Override
 	public String getName() {
 //<<<<<<< HEAD
 //		return (String) data.get(NAME);
 //=======
 		return name;
 //>>>>>>> async
 	}
 
 	@Override
 	public List<ProductItem> getWishList() {
 //<<<<<<< HEAD
 //		return (List<ProductItem>) data.get(WISHLIST);
 //=======
 		return wishList;
 //>>>>>>> async
 	}
 
 	@Override
 	public List<ProductItem> getShoppingCart() {
 //<<<<<<< HEAD
 //		return (List<ProductItem>) data.get(SHOPPINGCART);
 //=======
 		return shoppingCart;
 //>>>>>>> async
 	}
 
 	@Override
 	public void setLanguage(String language) {
 //<<<<<<< HEAD
 //		data.put(LANG,language);
 //=======
 		this.language = language;
 //>>>>>>> async
 		
 	}
 
 	@Override
 	public void setName(String name) {
 //<<<<<<< HEAD
 //		data.put(NAME, name);
 //=======
 		this.name = name;
 //>>>>>>> async
 		
 	}
 
 	@Override
 	public void addItemToWishList(ProductItem o) {
 /*<<<<<<< HEAD
 		((List<ProductItem>) data.get(WISHLIST)).add(o);
 	}
 
 	@Override
 	public void addItemToShoppingCart(ProductItem o) {
 		((List<ProductItem>) data.get(SHOPPINGCART)).add(o);
 		
 	}
 
 	@Override
 	public void removeItemFromWishList(ProductItem o) {
 		((List<ProductItem>) data.get(WISHLIST)).remove(o);
 =======
 */
 		this.wishList.add(o);
 		
 	}
 	@Override
 	
 	public void removeItemFromWishList(ProductItem o) {
 		this.wishList.remove(o);
 	}
 	
 	@Override
 	public void addItemToShoppingCart(ProductItem o) {
 		this.shoppingCart.add(o);
 		
 //>>>>>>> async
 	}
 
 	@Override
 	public void removeItemFromShoppingCart(ProductItem o) {
 //<<<<<<< HEAD
 //		((List<ProductItem>) data.get(SHOPPINGCART)).add(o);
 //	}
 
 	
 //=======
 		this.shoppingCart.remove(o);
 	}
 
 //>>>>>>> async
 }
