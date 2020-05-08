 package br.usp.ime.futuremarket.choreography;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import br.usp.ime.futuremarket.CustomerInfo;
 import br.usp.ime.futuremarket.Delivery;
 import br.usp.ime.futuremarket.Product;
 import br.usp.ime.futuremarket.Purchase;
 import br.usp.ime.futuremarket.Shipper;
 import br.usp.ime.futuremarket.ShopList;
 import br.usp.ime.futuremarket.ShopListItem;
 import br.usp.ime.futuremarket.Supermarket;
 
 public class BrokerImpl implements Broker {
     protected final FutureMarket market = new FutureMarket();
 
     @Override
     public ShopList getLowestPrice(final ShopList list) throws IOException {
         ShopList smList;
 
         for (Supermarket supermarket : getSupermarkets()) {
             smList = supermarket.getPrices(list);
             lookForCheaperProducts(list, smList);
         }
         return list;
     }
 
     @Override
     public Delivery getDelivery(final Purchase purchase) throws MalformedURLException {
         final String baseAddress = purchase.getShipper();
         final Shipper shipper = market.getClient(baseAddress, ServiceName.SHIPPER, Shipper.class);
         return shipper.getDelivery(purchase);
     }
 
     @Override
     public Set<Purchase> purchase(final ShopList list, final CustomerInfo customer)
             throws IOException {
         final Set<Purchase> purchases = new HashSet<Purchase>();
         final Collection<ShopList> listsPerSm = splitListBySm(list);
 
         Purchase purchase;
         for (ShopList smList : listsPerSm) {
             purchase = purchaseFromOneStore(smList, customer);
             purchases.add(purchase);
         }
 
         return purchases;
     }
 
     public Purchase purchaseFromOneStore(final ShopList list, final CustomerInfo customer)
             throws IOException {
         final String baseAddr = list.getSeller();
         final Supermarket supermarket = market.getClient(baseAddr, ServiceName.SUPERMARKET,
                 Supermarket.class);
         return supermarket.purchase(list, customer);
     }
 
     private Collection<ShopList> splitListBySm(final ShopList list) {
         // <SM baseAddr, ShopList>
         final Map<String, ShopList> result = new HashMap<String, ShopList>();
 
         ShopList smList;
         for (ShopListItem item : list.getItems()) {
             smList = getSupermarketList(result, item.getSellerEndpoint());
             smList.put(item);
         }
         return result.values();
     }
 
     private ShopList getSupermarketList(final Map<String, ShopList> lists, final String supermarket) {
         if (!lists.containsKey(supermarket)) {
             lists.put(supermarket, new ShopList());
         }
         return lists.get(supermarket);
     }
 
     private void lookForCheaperProducts(final ShopList list, final ShopList candidateList) {
         Product candidateProduct;
         ShopListItem item;
 
         for (ShopListItem candidateItem : candidateList.getItems()) {
             candidateProduct = candidateItem.getProduct();
             item = list.getItem(candidateProduct);
             lookForCheaperProduct(item, candidateItem, candidateProduct);
         }
     }
 
     private void lookForCheaperProduct(final ShopListItem item, final ShopListItem candidateItem,
             final Product candidateProduct) {
         final Product product = item.getProduct();
 
         if (candidateProduct.getPrice() < product.getPrice()) {
             product.setPrice(candidateProduct.getPrice());
             item.setSellerEndpoint(candidateItem.getSellerEndpoint());
         }
     }
 
     private Collection<Supermarket> getSupermarkets() throws IOException {
         return market.getClients(Role.SUPERMARKET, ServiceName.SUPERMARKET, Supermarket.class);
     }
 }
