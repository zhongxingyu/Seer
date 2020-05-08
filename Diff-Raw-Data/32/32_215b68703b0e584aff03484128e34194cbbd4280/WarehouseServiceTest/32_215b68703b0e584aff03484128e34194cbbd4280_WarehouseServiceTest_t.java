 package at.ac.tuwien.infosys.aic.it;

 import at.ac.tuwien.infosys.aic.soap.WarehouseService;
 import at.ac.tuwien.infosys.aic.model.Product;
 import at.ac.tuwien.infosys.aic.store.DataStore;
 import java.math.BigDecimal;
 import javax.xml.ws.soap.SOAPFaultException;
 import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 import org.junit.Before;
 import org.junit.Test;
 import static at.ac.tuwien.infosys.aic.Constants.*;
 
 public class WarehouseServiceTest extends BaseIntegrationTest{
 
     private DataStore ds = DataStore.getInstance();
     private HttpClient httpclient = new DefaultHttpClient();
 
     @Before
     public void init() {
         ds.init();
     }
 
         @Test
     public void orderTest() {
 
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(WarehouseService.class);
         factory.setAddress(WAREHOUSEADDRESS);
         WarehouseService sw = (WarehouseService) factory.create();
 
         //order something
         int ammount = 5;
         BigDecimal totalPrice;
         //price of product is 0
         Product p = DataStore.getInstance().getProduct("a777070b-96f3-47ac-9fe9-dfe2dadc00cb");
         totalPrice = sw.order(p, ammount);
         int comp = totalPrice.compareTo(new BigDecimal(50));
         assertTrue(comp == 0);
     }
 
     @Test
     public void orderWrongProductTest() {
 
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(WarehouseService.class);
         factory.setAddress(WAREHOUSEADDRESS);
         WarehouseService ws = (WarehouseService) factory.create();
 
         //order something
         int ammount = 5;
         BigDecimal totalPrice;
         //price of product is 0
         Product p = DataStore.getInstance().getProduct("a777070b-96f3-47ac-9fe9-dfe2dadc00cb");
         p.setId("unknownProduct");
 
         try {
             totalPrice = ws.order(p, ammount);
         } catch (SOAPFaultException e) {
             assertThat(e.getMessage(), is("unknown product fault"));
         }
 
     }
 
        @Test
     public void check_availabilityTests() {
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(WarehouseService.class);
         factory.setAddress(WAREHOUSEADDRESS);
         WarehouseService ss = (WarehouseService) factory.create();
 
         //order something
         int amount = 5;
         BigDecimal totalPrice;
         Product p = DataStore.getInstance().getProduct("a777070b-96f3-47ac-9fe9-dfe2dadc00cb");
         totalPrice = ss.order(p, amount);
         assertEquals(new BigDecimal(50), totalPrice);
 
 
         amount = 1;
         p = DataStore.getInstance().getProduct("aec0737d-e783-4c16-9b26-66040caf4aff");
         totalPrice = ss.order(p, amount);
         assertEquals(new BigDecimal(0), totalPrice);
 
         amount = 6;
         p = DataStore.getInstance().getProduct("aec0737d-e783-4c16-9b26-66040caf4aff");
         assertFalse(ss.check_availability(p, amount).isIsAvailable());
 
         amount = 5;
         p = DataStore.getInstance().getProduct("aec0737d-e783-4c16-9b26-66040caf4aff");
         assertTrue(ss.check_availability(p, amount).isIsAvailable());
 
         amount = -1;
         p = DataStore.getInstance().getProduct("aec0737d-e783-4c16-9b26-66040caf4aff");
         try {
             assertFalse(ss.check_availability(p, amount).isIsAvailable());
             fail("exception expected");
         } catch (SOAPFaultException e) {
             assertThat(e.getFault().getFaultString(), is("negative amount fault"));
         }
 
     }
 
     @Test
     public void check_availabilityWrongProductTests() {
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(WarehouseService.class);
         factory.setAddress(WAREHOUSEADDRESS);
         WarehouseService ss = (WarehouseService) factory.create();
 
         int   amount = 1;
         Product p = DataStore.getInstance().getProduct("aec0737d-e783-4c16-9b26-66040caf4aff");
         p.setId("WrongProduct");
         try {
             assertFalse(ss.check_availability(p, amount).isIsAvailable());
             fail("exception expected");
         } catch (SOAPFaultException e) {
             assertThat(e.getFault().getFaultString(), is("unknown product fault"));
         }
 
     }
 
 }
