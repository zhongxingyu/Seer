 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package at.ac.tuwien.infosys.aic.soap;
 
 import at.ac.tuwien.infosys.aic.soap.faults.NegativeAmountFault;
 import at.ac.tuwien.infosys.aic.soap.faults.UnknownProductFault;
 import at.ac.tuwien.infosys.aic.model.Product;
 import at.ac.tuwien.infosys.aic.store.DataStore;
 import java.math.BigDecimal;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 import javax.jws.WebService;
 
 @WebService(targetNamespace = "http://infosys.tuwien.ac.at/aic10/dto/warehouse",
 portName = "WarehousePT",
 endpointInterface = "at.ac.tuwien.infosys.aic.soap.WarehouseService")
 public class WarehouseServiceImpl implements WarehouseService {
 
     DataStore ds = DataStore.getInstance();
     Logger log = Logger.getLogger("WarehouseServiceImpl");
 
     @Override
     public BigDecimal order(Product product, Integer amount) {
 
         log.info("supplier service called!");
 
         Product p = DataStore.getInstance().getProduct(product.getId());
         if (p == null){
             throw new UnknownProductFault();
         }
 
         log.info("product name: " + p.getName());
 
         BigDecimal order = p.getSingleUnitPrice().multiply( new BigDecimal(amount));
 
         log.info("order total: " + order);
 
         return order;
     }
 
     @Override
     public WarehouseResponse check_availability(Product product, Integer amount) {
 
         ProductData pd = ds.getProductData(product);
         WarehouseResponse w = new WarehouseResponse();
 
         if (amount < 1) {
 
             throw new NegativeAmountFault();
             
         } else {
 
            if (pd == null) {
                throw new UnknownProductFault();
             } else {
                w.setDeliveryTime(pd.getDeliveryTime());
 
                if (amount > pd.getAmount()){
                    w.setIsAvailable(false);
                } else {
                    w.setIsAvailable(true);
                }
            }
             return w;
         }
 
     }
 
 
 
 }
