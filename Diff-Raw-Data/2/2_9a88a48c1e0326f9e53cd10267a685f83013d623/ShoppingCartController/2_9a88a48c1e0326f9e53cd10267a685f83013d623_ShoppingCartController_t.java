 package cz.muni.fi.pv243.controller;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Model;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 
 import org.jboss.seam.security.Identity;
 
 import cz.muni.fi.pv243.model.OrderItem;
 import cz.muni.fi.pv243.model.Product;
 import cz.muni.fi.pv243.model.ShoppingCart;
 import cz.muni.fi.pv243.service.OrderItemManager;
 import cz.muni.fi.pv243.service.ProductManager;
 import cz.muni.fi.pv243.service.ShoppingCartManager;
 import cz.muni.fi.pv243.service.UserManager;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 @SessionScoped
 @Model
 public class ShoppingCartController implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2118963139420656282L;
 
 	@Inject
 	private Logger log;
 	
 	@Inject
 	private FacesContext facesContext;
 
 	@Inject
 	private Identity identity;
 
 	@Inject
 	private ShoppingCart cart;
 
 	@Inject
 	private ShoppingCartManager cartManager;
 
 	@Inject
 	private ProductManager productManager;
 
 	@Inject
 	private OrderItemManager orderManager;
 
 	@Inject
 	private UserManager userManager;
 
 	private int quantity = 1;
 	private BigDecimal totalPrice = new BigDecimal(0);
 
 	public int getQuantity() {
 		return quantity;
 	}
 
 	public void setQuantity(int quantity) {
 		this.quantity = quantity;
 	}
 
 	public void addToCart(Long productId, Long quantity) {
 		if (quantity == null) {
 			throw new IllegalArgumentException("Long number quantity was null.");
 		}
 		addToCart(productId, quantity.intValue());
 	}
 
 	public void addToCart(Long productId, int quantity) {
 		Product product = productManager.get(productId);
 		List<OrderItem> items = cart.getItems();
 		for (OrderItem element : items) {
 			if (element.getProduct().equals(product)) {
 				element.setQuantity(element.getQuantity() + quantity);
 				setQuantity(1);
 				return;
 			}
 		}
 		OrderItem order = new OrderItem();
 		order.setProduct(product);
 		order.setQuantity(quantity);
 		items.add(order);
 		setQuantity(1);
 	}
 
 	public void deleteOrderItemInCart(Product product) {
 		List<OrderItem> items = cart.getItems();
 		for (OrderItem forItem : items) {
 			if (forItem.getProduct().equals(product)) {
 				items.remove(forItem);
 				break;
 			}
 		}
 	}
 
 	public BigDecimal getTotalPrice() {
 		totalPrice = new BigDecimal(0);
 		for (OrderItem item : cart.getItems()) {
 			totalPrice = totalPrice.add(item.getProduct().getPrice()
 					.multiply(new BigDecimal(item.getQuantity())));
 		}
 		return totalPrice;
 	}
 
 	public List<OrderItem> getAll() {
 		return cart.getItems();
 	}
 
 	public void clearCart() {
 		cart = new ShoppingCart();
 		cart.setFinished(false);
 		cart.setItems(new ArrayList<OrderItem>());
 	}
 
 	public void order() {
 		log.finest("Calling order method");
 		if (identity.getUser() != null) {
 			cart.setUser(userManager.findByEmail(identity.getUser().getId()));
 		}
 		try {
 			log.finest("Redirecting to order page");
 			FacesContext.getCurrentInstance().getExternalContext()
 					.redirect("order.jsf");
 		} catch (IOException e) {
 			log.warning("IO exception raised while making order: " + e.toString());
 		}
 	}
 
 	public void confirm() {
		if(cart.getUser() == null)
			cart.setUser(userManager.findByEmail(identity.getUser().getId()));
 		log.finest("Confirming order");
 		cartManager.create(cart);
 		clearCart();
 		try {
 			log.finest("Order confirmed and redirect to index");
 			FacesContext.getCurrentInstance().getExternalContext()
 					.redirect("index.jsf");
 		} catch (IOException e) {
 			log.warning("IO Exception raised while confirming order");
 		}
 	}
 }
