 package br.usp.ime.futuremarket;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.jws.WebMethod;
 import javax.jws.WebService;
 
 import br.usp.ime.futuremarket.models.LowestPrice;
 
 @WebService(targetNamespace = "http://futuremarket.ime.usp.br",
 endpointInterface = "br.usp.ime.futuremarket.Customer")
 public class CustomerImpl implements Customer {
 
     private List<Supermarket> supermarkets;
     private FutureMarket futureMarket;
     private Shipper shipper;
     // <listID, <supermarket,<product>>>
     private HashMap<String, HashMap<Supermarket, Set<String>>> customerProductLists;
     private long currentList = 1L;
 
     public CustomerImpl() {
         customerProductLists = new HashMap<String, HashMap<Supermarket, Set<String>>>();
 
         futureMarket = new FutureMarket();
         supermarkets = futureMarket.getClients(FutureMarket.SUPERMARKET_ROLE,
                 FutureMarket.SUPERMARKET_SERVICE, Supermarket.class);
         shipper = futureMarket.getFirstClient(FutureMarket.SHIPPER_ROLE,
                 FutureMarket.SHIPPER_SERVICE, Shipper.class);
     }
 
     @WebMethod
     public LowestPrice getLowestPriceForList(String[] products) {
         HashMap<HashMap<String, Double>, Supermarket> supermarketsProductList = new HashMap<HashMap<String, Double>, Supermarket>();
         // gets prices from supermarkets
         for (Supermarket supermarket : supermarkets) {
             ProductPrice[] productPrices = supermarket.getPrices(products);
             HashMap<String, Double> productsMap = new HashMap<String, Double>();
             for (ProductPrice productPrice : productPrices) {
                 productsMap.put(productPrice.getProduct(), productPrice.getPrice());
             }
             supermarketsProductList.put(productsMap, supermarket);
         }
 
         String listId = "" + getListId();
         customerProductLists.put(listId, new HashMap<Supermarket, Set<String>>());
         Double listPrice = 0d;
         // finds lowest prices
         for (String product : products) {
             Supermarket supermarket = null;
             Double lowestPrice = Double.MAX_VALUE;
             for (HashMap<String, Double> productsHash : supermarketsProductList.keySet()) {
                 Double price = productsHash.get(product);
                 if (price < lowestPrice) {
                     lowestPrice = price;
                     supermarket = supermarketsProductList.get(productsHash);
                 }
             }
             addProduct(listId, supermarket, product);
             listPrice += lowestPrice;
         }
 
         return new LowestPrice(listId, listPrice);
     }
 
     private void addProduct(String listId, Supermarket supermarket, String product) {
         if (customerProductLists.get(listId).get(supermarket) == null) {
             customerProductLists.get(listId).put(supermarket, new HashSet<String>());
         }
         customerProductLists.get(listId).get(supermarket).add(product);
     }
 
     private synchronized long getListId() {
         return currentList++;
     }
 
     @WebMethod
     public DeliveryInfo getShipmentData(PurchaseInfo purchaseInfo) {
         return shipper.getDeliveryStatus(purchaseInfo);
     }
 
     @WebMethod
     public PurchaseInfo[] makePurchase(String listId, CustomerInfo customerInfo) {
         HashMap<Supermarket, Set<String>> purchaseLists = customerProductLists.get(listId);
         List<PurchaseInfo> result = new ArrayList<PurchaseInfo>();
 
         String[] products;
         PurchaseInfo purchaseInfo;
         for (Supermarket supermarket : purchaseLists.keySet()) {
             products = purchaseLists.get(supermarket).toArray(new String[1]);
             purchaseInfo = supermarket.purchase(products, customerInfo);
             result.add(purchaseInfo);
         }

         return result.toArray(new PurchaseInfo[1]);
     }
 
 }
