 package br.usp.ime.futuremarket;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.jws.WebMethod;
 import javax.jws.WebService;
 
 @WebService(targetNamespace = "http://futuremarket.ime.usp.br",
 endpointInterface = "br.usp.ime.futuremarket.Supermarket")
 public class SupermarketImpl implements Supermarket {
 
     protected HashMap<String, Double> priceTable;
     protected HashMap<String, Integer> stockItems;
     private long currentId;
     private FutureMarket futureMarket;
     private static String WSDL;
     private static Orchestrator orchestrator;
     private String shipperName;
     private String serviceName;
     private String serviceRole;
     private String sellerName;
     
     private Integer purchaseTrigger;
     private Integer purchaseQuantity;
     
     private CustomerInfo customerInfo;
     
     private ClassLoader loader = SupermarketImpl.class.getClassLoader();   
 
     public SupermarketImpl() {
         futureMarket = new FutureMarket();
         priceTable = new HashMap<String, Double>();
         stockItems = new HashMap<String, Integer>();
         currentId = 0l;
         Properties properties = new Properties();
 
         try {
             properties.load(loader.getResourceAsStream("supermarket.properties"));
         } catch (IOException e) {
             System.out.println("Could not read resources/supermarket.properties");
         }
 
         serviceName = properties.getProperty("this.name");
         serviceRole = properties.getProperty("this.role");
         shipperName = properties.getProperty("shipper.name");
         purchaseTrigger = Integer.parseInt(properties.getProperty("purchase.trigger"));
         purchaseQuantity = Integer.parseInt(properties.getProperty("purchase.quantity"));
         
         
         final String relPath = getRelativePath();
         futureMarket.register(serviceRole, serviceName, relPath);
         WSDL = futureMarket.getMyWsdl(relPath);
 
 		orchestrator = futureMarket.getFirstClient(FutureMarket.ORCHESTRATOR_ROLE, 
 				FutureMarket.ORCHESTRATOR_SERVICE, Orchestrator.class);
         
         sellerName = properties.getProperty("seller.name");
         
         customerInfo = new CustomerInfo();
         customerInfo.setEndpoint(this.getWsdl());
         customerInfo.setId(serviceName+"ID");
         customerInfo.setName(serviceName);
         customerInfo.setZipcode("5555555");
 
         this.registerProducts(properties);
     }
 
     public String getWsdl() {
         return WSDL;
     }
 
     private long getListId() {
         synchronized (this) {
             currentId++;
         }
 
         return currentId;
     }
 
     private String getRelativePath() {
         String path = serviceName + "/endpoint";
 
         return path;
     }
 
     @WebMethod
     public ProductPrice[] getPrices(Set<ProductQuantity> products) {
         List<ProductPrice> productPriceList = new ArrayList<ProductPrice>();
         for (ProductQuantity product : products) {
             Double price = priceTable.get(product.getProduct());
             if (price != null) {
                 productPriceList.add(new ProductPrice(product.getProduct(), price));
             }
         }
 
         return productPriceList.toArray(new ProductPrice[1]);
     }
 
     private void printStock() {
     	for(String  p: stockItems.keySet()) {
     		System.out.println(p + " - " + stockItems.get(p));
     	}
     }
     
     @WebMethod
     public PurchaseInfo purchase(final Set<ProductQuantity> products, final CustomerInfo customerInfo) {
     	final PurchaseInfo purchaseInfo = new PurchaseInfo();
     	try {
 	        purchaseInfo.setCustomerInfo(customerInfo);
 	        purchaseInfo.setProducts(products);
 	        purchaseInfo.setValue(getTotalPrice(products));
 	        purchaseInfo.setId("" + getListId());
 	        purchaseInfo.setSellerEndpoint(WSDL);
 	        purchaseInfo.setShipperName(shipperName);
 	        
	        if (sellerName != null)
 	        	updateStock(products);
     	} catch(Exception e) {
     		e.printStackTrace();
     	}
         return purchaseInfo;
     }
 
     private void updateStock(Set<ProductQuantity> products) {
     	Set<ProductQuantity> productsToPurchase = new HashSet<ProductQuantity>();
 
     	for(ProductQuantity productQuantity:products) {
     		String product = productQuantity.getProduct();
     		Integer quantity = productQuantity.getQuantity();
     		Integer stock = stockItems.get(product);
     		Integer newStock = stock - quantity;
     		stockItems.put(product, newStock);
     		
     		if (newStock <= purchaseTrigger) {
     			productsToPurchase.add(new ProductQuantity(product, quantityToPurchase(product)));
     		}
     	}
     	
     	if (!productsToPurchase.isEmpty()) {
     		PurchaseInfo[] purchaseInfoList = orchestrator.makeSMPurchase(sellerName, productsToPurchase, customerInfo);
     		for(PurchaseInfo purchaseInfo: purchaseInfoList){
     			orchestrator.getShipmentData(purchaseInfo);
     		}
     	}	
     	
     	for(ProductQuantity p: productsToPurchase) {
     		stockItems.put(p.getProduct(), stockItems.get(p.getProduct()) + p.getQuantity());
     	}
 		
 	}
 
     private Integer quantityToPurchase(String product) {
 
     	Integer quantity = purchaseQuantity;
     	Integer currentStock = stockItems.get(product);
     	if (quantity + currentStock <= purchaseTrigger){
     		if (currentStock < 0) {
     			quantity += purchaseTrigger - currentStock;
     		} else { 
     			quantity += purchaseTrigger;
     		}
     	}
 
 		return quantity;
 	}
 
 	private Double getTotalPrice(Set<ProductQuantity> products) {
         Double total = 0d;
 
         for (ProductQuantity product : products) {
             total += priceTable.get(product.getProduct()) * product.getQuantity();
         }
 
         return total;
     }
     
     private void registerProducts(Properties properties){
         for(int i=1; i<=10; i++){
         	String product = "product"+i;
         	Double price = Double.parseDouble(properties.getProperty("product"+i+".price"));
         	Integer stock = Integer.parseInt(properties.getProperty("product"+i+".stock"));
         	priceTable.put(product, price);
         	stockItems.put(product, stock);
         }
     }
     
     public ProductQuantity[] getStock() {
     	Set<ProductQuantity> result = new HashSet<ProductQuantity>();
     	Iterator<Map.Entry<String, Integer>> it = this.stockItems.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry<String, Integer> pair = it.next();
             result.add(new ProductQuantity(pair.getKey(), pair.getValue()));
         }
     	
     	return result.toArray(new ProductQuantity[1]);
     }
     
 }
