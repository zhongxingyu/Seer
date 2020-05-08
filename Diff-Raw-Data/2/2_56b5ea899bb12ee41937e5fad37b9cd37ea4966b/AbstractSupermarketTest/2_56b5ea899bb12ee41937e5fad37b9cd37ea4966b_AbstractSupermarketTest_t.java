 package br.usp.ime.futuremarket.tests.integration;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.xmlbeans.XmlException;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 
 import br.usp.ime.futuremarket.AbstractFutureMarket;
 import br.usp.ime.futuremarket.AbstractWSInfo;
 import br.usp.ime.futuremarket.CustomerInfo;
 import br.usp.ime.futuremarket.Product;
 import br.usp.ime.futuremarket.Registry;
 import br.usp.ime.futuremarket.ShopList;
 import br.usp.ime.futuremarket.ShopListItem;
 import br.usp.ime.futuremarket.Supermarket;
 import eu.choreos.vv.clientgenerator.Item;
 import eu.choreos.vv.exceptions.MockDeploymentException;
 import eu.choreos.vv.exceptions.WSDLException;
 import eu.choreos.vv.interceptor.MessageInterceptor;
 
 public abstract class AbstractSupermarketTest {
     protected static AbstractFutureMarket market;
 
     private static Supermarket supermarket, supplier, manufacturer;
     private static final String NAME = "supermarket1";
     private static final String SHIPPER = "supplier1";
     private static final String MANUFAC = "manufacturer";
 
     protected MessageInterceptor interceptor;
     private static String nameBak;
     private static String baseAddrBak;
 
     protected static final int QT_INITIAL = 10;
     protected static final int QT_TRIGGER = 3;
     private static final int QT_PURCHASE = 10;
     private String product;
 
     abstract protected String getArchType();
 
     abstract protected AbstractWSInfo getWSInfo();
 
     @Before
     public void setUp() throws IOException, InterruptedException {
         if (supermarket == null) {
             supermarket = market.getClientByName(NAME, Supermarket.class);
             supplier = market.getClientByName(SHIPPER, Supermarket.class);
             manufacturer = market.getClientByName(MANUFAC, Supermarket.class);
         }
         supermarket.reset();
         supplier.reset();
         manufacturer.reset();
     }
 
     @After
     public void restoreBackup() throws IOException {
         register(nameBak, baseAddrBak);
         interceptor.stop();
     }
 
     /**
      * static variables are preserved between children!
      * 
      * @throws IOException
      * @throws InterruptedException
      */
     @AfterClass
     public static void reset() throws IOException, InterruptedException {
         // After all tests, reset must be called
         supermarket.reset();
         supermarket = null;
 
         supplier.reset();
         supplier = null;
 
         manufacturer.reset();
         manufacturer = null;
     }
 
     protected void intercept(final String name) throws WSDLException, MockDeploymentException,
             XmlException, IOException {
         nameBak = name;
         baseAddrBak = market.getBaseAddress(name);
 
         final String wsdl = baseAddrBak + "/" + getArchType() + "?wsdl";
         interceptor = new MessageInterceptor("8081");
        interceptor.interceptMessagesTo(wsdl);
 
         final String newBaseAddr = baseAddrBak.replaceFirst("8080", "8081");
         register(name, newBaseAddr);
     }
 
     private void register(final String name, final String baseAddr) throws IOException {
         final AbstractWSInfo info = getWSInfo();
         info.setName(name);
 
         final Registry registry = market.getRegistry();
         registry.addService(info.getRole().toString(), name, baseAddr);
     }
 
     @Test
     public void shouldNotBuyWhenQuantityIsHigherThanTrigger() throws IOException, WSDLException,
             MockDeploymentException, XmlException {
         intercept(SHIPPER);
 
         buy("product1", QT_INITIAL - QT_TRIGGER - 1);
         final List<Item> messages = interceptor.getMessages();
         assertEquals(0, messages.size());
     }
 
     @Test
     public void shouldBuyWhenQuantityEqualsTrigger() throws IOException, NoSuchFieldException,
             WSDLException, MockDeploymentException, XmlException, InterruptedException {
         intercept(SHIPPER);
         product = "product1";
 
         buy(product, QT_INITIAL - QT_TRIGGER);
         final List<Item> messages = interceptor.getMessages();
 
         assertEquals(QT_PURCHASE, getPurchasedQuantity(messages.get(0)));
         assertEquals(product, getProductName(messages.get(0)));
     }
 
     @Test
     public void shouldBuyWhenQuantityIsLowerThanTrigger() throws IOException, NoSuchFieldException,
             WSDLException, MockDeploymentException, XmlException {
         intercept(SHIPPER);
         product = "product2";
 
         buy(product, QT_INITIAL - QT_TRIGGER + 1);
         final List<Item> messages = interceptor.getMessages();
 
         assertEquals(1, messages.size());
         assertEquals(QT_PURCHASE, getPurchasedQuantity(messages.get(0)));
         assertEquals(product, getProductName(messages.get(0)));
     }
 
     @Test
     public void testSupplyQuantity() throws IOException, WSDLException, MockDeploymentException,
             XmlException {
         intercept(SHIPPER);
         product = "product3";
 
         // Before: 10 items = initial quantity
         buy(product, QT_INITIAL - QT_TRIGGER);
         List<Item> messages = interceptor.getMessages();
         assertEquals(1, messages.size());
         // After: 3 items = trigger quantity
 
         // Before: 13 items = 3 + purchase quantity
         buy(product, QT_TRIGGER + QT_PURCHASE - QT_TRIGGER - 1);
         messages = interceptor.getMessages();
         assertEquals(1, messages.size());
         // After: 4 items = trigger quantity + 1
 
         // One more purchase and trigger should be reached
         buy(product, 1);
         messages = interceptor.getMessages();
         assertEquals(2, messages.size());
     }
 
     @Test
     public void testSupplyQuantityAfterHugePurchase() throws IOException, WSDLException,
             MockDeploymentException, XmlException {
         intercept(SHIPPER);
         product = "product4";
 
         // Before: 10 items = initial quantity
         buy(product, QT_INITIAL * 2);
         List<Item> messages = interceptor.getMessages();
         assertEquals(1, messages.size());
         // After: 0 items
 
         // Before: 10 items = 0 + purchase quantity
         buy(product, QT_PURCHASE - QT_TRIGGER - 1);
         messages = interceptor.getMessages();
         assertEquals(1, messages.size());
         // After: 4 items = trigger quantity + 1
 
         // One more purchase and trigger should be reached
         buy(product, 1);
         messages = interceptor.getMessages();
         assertEquals(2, messages.size());
     }
 
     protected void buy(final String product, final int quantity) throws IOException {
         final ShopList list = getShopList(product, quantity);
         final CustomerInfo info = new CustomerInfo();
         supermarket.purchase(list, info);
     }
 
     private int getPurchasedQuantity(final Item purchaseRequest) throws NoSuchFieldException {
         final Item shopList = purchaseRequest.getChild("arg0");
         final Item items = shopList.getChild("items");
         final Item entry = items.getChild("entry");
         final Item item = entry.getChild("value");
         final String quantity = item.getContent("quantity");
 
         return Integer.parseInt(quantity);
     }
 
     private String getProductName(final Item purchaseRequest) throws NoSuchFieldException {
         final Item shopList = purchaseRequest.getChild("arg0");
         final Item items = shopList.getChild("items");
         final Item entry = items.getChild("entry");
         final Item item = entry.getChild("value");
         final Item product = item.getChild("product");
 
         return product.getContent("name");
     }
 
     private ShopList getShopList(final String productName, final int quantity) {
         final Product product = new Product(productName);
         final ShopListItem item = new ShopListItem(product);
         item.setQuantity(quantity);
         return new ShopList(item);
     }
 }
