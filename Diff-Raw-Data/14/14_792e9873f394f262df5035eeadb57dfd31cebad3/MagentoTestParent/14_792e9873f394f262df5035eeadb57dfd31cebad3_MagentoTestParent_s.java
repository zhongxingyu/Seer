 package org.mule.module.magento.automation.testcases;
 
 import java.util.List;
 import java.util.Map;
 
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.Timeout;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.tck.junit4.FunctionalTestCase;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.magento.api.CatalogCategoryEntityCreate;
 import com.magento.api.CatalogCategoryInfo;
 import com.magento.api.CatalogProductCreateEntity;
 import com.magento.api.CustomerAddressEntityCreate;
 import com.magento.api.CustomerCustomerEntityToCreate;
 import com.magento.api.ShoppingCartCustomerAddressEntity;
 import com.magento.api.ShoppingCartCustomerEntity;
 import com.magento.api.ShoppingCartPaymentMethodEntity;
 import com.magento.api.ShoppingCartProductEntity;
 import com.magento.api.ShoppingCartShippingMethodEntity;
 
 public class MagentoTestParent extends FunctionalTestCase {
 
 	// Set global timeout of tests to 10minutes
     @Rule
     public Timeout globalTimeout = new Timeout(600000);
 
 	protected static final String[] SPRING_CONFIG_FILES = new String[] { "AutomationSpringBeans.xml", "AttributesSpringBeans.xml" };
 	protected static ApplicationContext context;
 	protected Map<String, Object> testObjects;
 
 	public static final int ROOT_CATEGORY_ID = 1;
 	public static final int DEFAULT_CATEGORY_ID = 2;
 	
 	public static final int DEFAULT_PRODUCT_SET = 4;
 	
 	@Override
 	protected String getConfigResources() {
 		return "automation-test-flows.xml";
 	}
 
 	protected MessageProcessor lookupFlowConstruct(String name) {
 		return (MessageProcessor) muleContext.getRegistry().lookupFlowConstruct(name);
 	}
 	
 	@BeforeClass
 	public static void beforeClass() {
 		context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILES);
 	}
 	
 	/*
 	 * Helper methods below
 	 */
 	
 	public int createCustomer(CustomerCustomerEntityToCreate customer) throws Exception {
 		testObjects.put("customerRef", customer);
 		MessageProcessor flow = lookupFlowConstruct("create-customer");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Integer) response.getMessage().getPayload();
 	}
 	
 	public boolean deleteCustomer(int customerId) throws Exception {
 		testObjects.put("customerId", customerId);
 		MessageProcessor flow = lookupFlowConstruct("delete-customer");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Boolean) response.getMessage().getPayload();
 	}
 
 	public int createCustomerAddress(int customerId, CustomerAddressEntityCreate address) throws Exception {
 		testObjects.put("customerId", customerId);
 		testObjects.put("customerAddressRef", address);
 		MessageProcessor flow = lookupFlowConstruct("create-customer-address");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Integer) response.getMessage().getPayload();
 	}
 	
 	public boolean deleteCustomerAddress(int customerAddressId) throws Exception {
 		testObjects.put("addressId", customerAddressId);
 		MessageProcessor flow = lookupFlowConstruct("delete-customer-address");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Boolean) response.getMessage().getPayload();
 	}
 
 	public int createCategory(int parentId, CatalogCategoryEntityCreate category) throws Exception {
 		testObjects.put("parentId", parentId);
 		testObjects.put("catalogCategoryEntityRef", category);
 		MessageProcessor flow = lookupFlowConstruct("create-category");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Integer) response.getMessage().getPayload();
 	}
 	
 	public boolean deleteCategory(int categoryId) throws Exception {
 		testObjects.put("categoryId", categoryId);
 		MessageProcessor flow = lookupFlowConstruct("delete-category");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Boolean) response.getMessage().getPayload();
 	}
 	
 	public int createProduct(String type, int set, String sku, CatalogProductCreateEntity productAttributes) throws Exception {
 		testObjects.put("type", type);
 		testObjects.put("set", set);
 		testObjects.put("sku", sku);
 		testObjects.put("attributesRef", productAttributes);
 		
 		MessageProcessor flow = lookupFlowConstruct("create-product");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Integer) response.getMessage().getPayload();
 	}
 	
 	public int deleteProductById(int productId) throws Exception {
 		testObjects.put("productId", productId);
 		MessageProcessor flow = lookupFlowConstruct("delete-product-by-product-id");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Integer) response.getMessage().getPayload();
 	}
 	
 	public int deleteProductBySku(String productSku) throws Exception {
 		testObjects.put("productSku", productSku);
 		MessageProcessor flow = lookupFlowConstruct("delete-product-by-product-sku");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Integer) response.getMessage().getPayload();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public CatalogCategoryInfo getCategory(int categoryId) throws Exception {
 		List<String> categoryAttributeNames = (List<String>) context.getBean("categoryAttributeNames");
 		testObjects.put("categoryId", categoryId);
 		testObjects.put("attributeNames", categoryAttributeNames);
 		MessageProcessor flow = lookupFlowConstruct("get-category");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (CatalogCategoryInfo) response.getMessage().getPayload();
 	}
 	
 	public int createShoppingCart() throws Exception {
 		MessageProcessor flow = lookupFlowConstruct("create-shopping-cart");
 		MuleEvent response = flow.process(getTestEvent(null));
 		return (Integer) response.getMessage().getPayload();
 	}
 	
 	public boolean addProductsToShoppingCart(int quoteId, List<ShoppingCartProductEntity> products) throws Exception {
 		testObjects.put("quoteId", quoteId);
 		testObjects.put("productsRef", products);
 		
 		// Add the shopping cart products
 		MessageProcessor flow = lookupFlowConstruct("add-shopping-cart-product");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Boolean) response.getMessage().getPayload();
 	}
 	
 	public boolean setShoppingCartCustomer(int quoteId, ShoppingCartCustomerEntity customer) throws Exception {
 		testObjects.put("quoteId", quoteId);
 		testObjects.put("customerRef", customer);
 		
 		// Add the shopping cart customer
 		MessageProcessor flow = lookupFlowConstruct("set-shopping-cart-customer");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Boolean) response.getMessage().getPayload();
 	}
 	
 	public boolean setCustomerAddressesToShoppingCart(int quoteId, List<ShoppingCartCustomerAddressEntity> addresses) throws Exception {
 		testObjects.put("quoteId", quoteId);
 		testObjects.put("shoppingCartCustomerAddressesRef", addresses); 
 		
 		// Add the customer addresses
 		MessageProcessor flow = lookupFlowConstruct("set-shopping-cart-customer-addresses");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Boolean) response.getMessage().getPayload();
 	}
 	
 	public boolean setShoppingCartShippingMethod(int quoteId, String shippingMethod) throws Exception {
 		testObjects.put("quoteId", quoteId);
 		testObjects.put("method", shippingMethod);
 		
 		// Set the shopping cart shipping method
 		MessageProcessor flow = lookupFlowConstruct("set-shopping-cart-shipping-method");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Boolean) response.getMessage().getPayload();
 	}
 	
 	public boolean setShoppingCartPaymentMethod(int quoteId, ShoppingCartPaymentMethodEntity paymentMethod) throws Exception {
 		testObjects.put("quoteId", quoteId);
 		testObjects.put("shoppingCartPaymentMethodRef", paymentMethod);
 		
 		// Set the shopping cart payment method
 		MessageProcessor flow = lookupFlowConstruct("set-shopping-cart-payment-method");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Boolean) response.getMessage().getPayload();
 	}
 	
 	public String createShoppingCartOrder(ShoppingCartCustomerEntity customer,
 								List<ShoppingCartCustomerAddressEntity> addresses,
 								ShoppingCartPaymentMethodEntity paymentMethod,
 								String shippingMethod,
 								List<ShoppingCartProductEntity> products) throws Exception {
 		
 		int quoteId = createShoppingCart();
 
 		testObjects.put("quoteId", quoteId);
 		
 		setShoppingCartCustomer(quoteId, customer);
 		setCustomerAddressesToShoppingCart(quoteId, addresses);
 		setShoppingCartPaymentMethod(quoteId, paymentMethod);
 		setShoppingCartShippingMethod(quoteId, shippingMethod);
 		addProductsToShoppingCart(quoteId, products);
 		
 		MessageProcessor flow = lookupFlowConstruct("create-shopping-cart-order");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return response.getMessage().getPayload().toString();
 	}
 	
 	public Boolean cancelOrder(String orderId) throws Exception {
 		testObjects.put("orderId", orderId);
 		
 		// Cancel the order
 		MessageProcessor flow = lookupFlowConstruct("cancel-order");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Boolean) response.getMessage().getPayload();
 	}
 	
 	public boolean holdOrder(String orderId) throws Exception {
 		testObjects.put("orderId", orderId);
 		
 		MessageProcessor flow = lookupFlowConstruct("hold-order");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Boolean) response.getMessage().getPayload();
 	}
 	
	public boolean unholdOrder(String orderId) throws Exception {
 		testObjects.put("orderId", orderId);
 		
 		MessageProcessor flow = lookupFlowConstruct("unhold-order");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
		return (Boolean) response.getMessage().getPayload();
 	}
 	
 }
