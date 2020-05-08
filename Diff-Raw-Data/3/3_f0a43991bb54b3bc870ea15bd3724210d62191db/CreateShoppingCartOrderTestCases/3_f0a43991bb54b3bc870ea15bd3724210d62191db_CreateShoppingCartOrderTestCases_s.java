 package org.mule.module.magento.automation.testcases;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 
 import com.magento.api.CatalogProductCreateEntity;
 import com.magento.api.ShoppingCartCustomerAddressEntity;
 import com.magento.api.ShoppingCartCustomerEntity;
 import com.magento.api.ShoppingCartPaymentMethodEntity;
 import com.magento.api.ShoppingCartProductEntity;
 
 public class CreateShoppingCartOrderTestCases extends MagentoTestParent {
 
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (Map<String, Object>) context.getBean("createShoppingCartOrder");
 			
 			// Create the shopping cart
 			int quoteId = createShoppingCart();
 			testObjects.put("quoteId", quoteId);
 
 			// Create the shopping cart customer
 			ShoppingCartCustomerEntity customer = (ShoppingCartCustomerEntity) testObjects.get("customer");
 			setShoppingCartCustomer(quoteId, customer);
 			
 			// Set the customer addresses to the shopping cart
 			List<ShoppingCartCustomerAddressEntity> customerAddresses = (List<ShoppingCartCustomerAddressEntity>) testObjects.get("customerAddresses");
 			setCustomerAddressesToShoppingCart(quoteId, customerAddresses);
 			
 			// Set the shipping method
 			String shippingMethod = testObjects.get("shippingMethod").toString();
 			setShoppingCartShippingMethod(quoteId, shippingMethod);
 
 			// Set the payment method
 			ShoppingCartPaymentMethodEntity paymentMethod = (ShoppingCartPaymentMethodEntity) testObjects.get("paymentMethod");
 			setShoppingCartPaymentMethod(quoteId, paymentMethod);
 			
 			// Create the products and add to shopping cart
 			List<HashMap<String, Object>> products = (List<HashMap<String, Object>>) testObjects.get("products");	
 			List<ShoppingCartProductEntity> shoppingCartProducts = new ArrayList<ShoppingCartProductEntity>();
 			List<Integer> productIds = new ArrayList<Integer>();
 			
 			for (HashMap<String, Object> product : products) {
 				// Get the product data
 				String productType = (String) product.get("type");
 				int productSet = (Integer) product.get("set");
 				String productSKU = (String) product.get("sku");
 				CatalogProductCreateEntity attributes = (CatalogProductCreateEntity) product.get("attributesRef");
 			
 				// Create the product and get the product ID
 				int productId = createProduct(productType, productSet, productSKU, attributes);
 				
 				// Get the quantity to place in the shopping cart
 				double qtyToPurchase = (Double) product.get("qtyToPurchase");
 
 				// Create the shopping cart product entity
 				ShoppingCartProductEntity shoppingCartProduct = new ShoppingCartProductEntity();
 				shoppingCartProduct.setProduct_id(productId + "");
 				shoppingCartProduct.setQty(qtyToPurchase);
 				
 				shoppingCartProducts.add(shoppingCartProduct);
 				productIds.add(productId);
 			}
 
 			// Add the products to the shopping cart
 			addProductsToShoppingCart(quoteId, shoppingCartProducts);
 			
 			testObjects.put("productIds", productIds);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({SmokeTests.class, RegressionTests.class})
 	@Test
 	public void testCreateShoppingCartOrder() {
 		try {
 			// Create the order
 			MessageProcessor flow = lookupFlowConstruct("create-shopping-cart-order");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 			
 			// Assert that the order number (in the form of a string) is not null
 			String orderId = (String) response.getMessage().getPayload();
 			assertNotNull(orderId);
 
 			// Try parsing the order number as an integer
 			// If no exception was thrown, then the result is correct
 			Integer.parseInt(orderId);
 			
 			testObjects.put("orderId", orderId);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@After
 	public void tearDown() {
 		try {
 			List<Integer> productIds = (List<Integer>) testObjects.get("productIds");
 			for (Integer productId : productIds) {
 				deleteProductById(productId);
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 }
