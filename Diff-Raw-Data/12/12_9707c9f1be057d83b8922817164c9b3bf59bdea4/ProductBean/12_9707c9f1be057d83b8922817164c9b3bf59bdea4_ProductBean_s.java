 package cz.fi.muni.pv243.eshop.controller;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.event.Observes;
 import javax.enterprise.event.Reception;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import cz.fi.muni.pv243.eshop.model.Product;
 import cz.fi.muni.pv243.eshop.service.ProductManager;
 
 @Named("productsBean")
 @SessionScoped
 public class ProductBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	@Inject
 	private ProductManager productManager;
 
 	private static List<Product> productList;
 	@Inject
 	private Logger log;
 
 	private Product newProduct;
 
 	@Inject
 	private FacesContext facesContext;
 
 	public List<Product> getProductList() {
 		return productList;
 	}
 
 	public void onCustomerListChanged(
 			@SuppressWarnings("cdi-observer") @Observes(notifyObserver = Reception.IF_EXISTS) final Product product) {
 		retrieveAllProducts();
 	}
 
 	public void saveAction(Product product) {
 		product.setEditable(false);
 		productManager.update(product);
 	}
 
 	public void editAction(Product product) {
 		product.setEditable(true);
 	}
 
 	@PostConstruct
 	public void retrieveAllProducts() {
 		productList = productManager.getProducts();
 		initNewProduct();
 	}
 
 	@Produces
 	@Named
 	public Product getNewProduct() {
 		return newProduct;
 	}
 
 	public void register() throws Exception {
 		productManager.addProduct(newProduct);
 		facesContext.addMessage(null, new FacesMessage(
 				FacesMessage.SEVERITY_INFO, "Added!", "Product was added"));
 		initNewProduct();
 	}
 
 	public void validateNumberRange(FacesContext context,
 			UIComponent toValidate, Object value) {
 		int input = (Integer) value;
 
 		if (input < 1 || input > 10000) {
 			((UIInput) toValidate).setValid(false);
 
 			FacesMessage message = new FacesMessage("Invalid number");
 			context.addMessage(toValidate.getClientId(context), message);
 		}
 	}
 
 	public void initNewProduct() {
 		newProduct = new Product();
 	}
 
 }
