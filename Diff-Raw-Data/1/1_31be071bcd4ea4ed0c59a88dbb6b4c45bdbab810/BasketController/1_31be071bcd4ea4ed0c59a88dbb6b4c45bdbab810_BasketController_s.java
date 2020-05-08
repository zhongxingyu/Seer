 package cz.fi.muni.pv243.eshop.controller;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.inject.Model;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import cz.fi.muni.pv243.eshop.model.ProductInBasket;
 import cz.fi.muni.pv243.eshop.model.ProductToBasket;
 import cz.fi.muni.pv243.eshop.model.ProductUpdateBasket;
 import cz.fi.muni.pv243.eshop.service.Basket;
 
 /**
  * @author Matous Jobanek
  */
 @Model
 public class BasketController implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Inject
 	private Basket basket;
 
 	private ProductInBasket productInBasket;
 
 	private ProductToBasket productToBasket;
 
 	private ProductUpdateBasket productUpdateBasket;
 
 	@Produces
 	@Named
 	public ProductInBasket getProductInBasket() {
 		return productInBasket;
 	}
 
 	@Produces
 	@Named
 	public ProductToBasket getProductToBasket() {
 		return productToBasket;
 	}
 
 	@Produces
 	@Named
 	public ProductUpdateBasket getProductUpdateBasket() {
 		return productUpdateBasket;
 	}
 
 	@PostConstruct
 	public void initNewProduct() {
 		productInBasket = new ProductInBasket();
 		productToBasket = new ProductToBasket(null, 1);
 		productUpdateBasket = new ProductUpdateBasket();
 	}
 
 	public void addProductToBasket() throws Exception {
 		basket.addProduct(
 				Long.parseLong(productToBasket.getProductId().getValue()
 						.toString()), productToBasket.getQuantity());
 		productToBasket = new ProductToBasket(null, 1);
 	}
 
 	public void updateProductInBasket() throws Exception {
 		basket.updateProduct(
 				Long.parseLong(productUpdateBasket.getProductId().getValue().toString()), 
 						
 						productUpdateBasket.getQuantity());
 	}
 
 	@Produces
 	@Named("productsInBasket")
 	public List<ProductInBasket> getProducts() throws Exception {
 		return basket.getAllMessages();
 	}
 
 }
