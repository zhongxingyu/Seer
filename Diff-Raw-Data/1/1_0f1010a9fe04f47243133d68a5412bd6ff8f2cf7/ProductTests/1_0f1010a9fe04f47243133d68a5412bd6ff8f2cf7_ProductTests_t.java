 package me.alanfoster.shoppingcart.webservice;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import me.alanfoster.shoppingcart.webservice.util.ProductFactory;
 import me.alanfoster.tests.shoppingcart.wsdl.proxyclasses.GetAllProductsRequest;
 import me.alanfoster.tests.shoppingcart.wsdl.proxyclasses.GetAllProductsResponse;
 import me.alanfoster.tests.shoppingcart.wsdl.proxyclasses.GetProductRequest;
 import me.alanfoster.tests.shoppingcart.wsdl.proxyclasses.GetProductResponse;
 import me.alanfoster.tests.shoppingcart.wsdl.proxyclasses.ProductType;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.junit.Assert.*;
 import static me.alanfoster.shoppingcart.webservice.util.ProductAssert.*;
 
 public class ProductTests {
 	Logger logger = LoggerFactory.getLogger(ProductTests.class);
 
 	@Before
 	public void setUp() {
 	}
 	
 	@After
 	public void tearDown() {
 	}
 	
 	public ShoppingCartPortTypeImpl getShoppingCartPortType() {
 		return new ShoppingCartPortTypeImpl();
 	}
 	
     @Test
     public void testGetKnownProduct() throws Exception {
     	ProductType expectedProduct = ProductFactory.getNewProduct("1", "Duracell AA Battery", "Lasts longer", 4f);
     	List<ProductType> products = Arrays.asList(expectedProduct);
     	
     	ShoppingCartPortTypeImpl shoppingCart = getShoppingCartPortType();
     	shoppingCart.setProducts(Arrays.asList(expectedProduct));
     	
     	GetProductRequest request = new GetProductRequest();
     	request.setProductId(expectedProduct.getProductId());
     	
     	GetProductResponse response = shoppingCart.getProduct(request);
     	ProductType actualProduct = response.getProduct();
     	
     	assertEqual(expectedProduct, actualProduct);
     }
     
     @Test
     public void testGetAllProducts() throws Exception {
 	    List<ProductType> productsExpected = Arrays.asList(
 			ProductFactory.getNewProduct("1", "Duracell AA Battery", "Lasts longer", 4f),
 			ProductFactory.getNewProduct("2", "Milk", "Only the freshest", 0.89f),
 			ProductFactory.getNewProduct("3", "Eggs", "Free Range", 1.19f),
 			ProductFactory.getNewProduct("4", "Bread", "3 day life", 1.20f)
 		);
 	
     	ShoppingCartPortTypeImpl shoppingCart = getShoppingCartPortType();
     	shoppingCart.setProducts(productsExpected);
     	
     	GetAllProductsRequest request = new GetAllProductsRequest();
     	
     	GetAllProductsResponse response = shoppingCart.getAllProducts(request);
     	List<ProductType> productsActual = response.getProducts().getProduct();
     	
     	assertEqual(productsExpected, productsActual);
     }

 }
