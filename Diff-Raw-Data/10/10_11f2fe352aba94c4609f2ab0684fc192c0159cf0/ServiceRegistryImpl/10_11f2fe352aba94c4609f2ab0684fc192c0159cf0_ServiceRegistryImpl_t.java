 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package at.ac.tuwien.infosys.aic.registry;
 
 import at.ac.tuwien.infosys.aic.model.Product;
 import at.ac.tuwien.infosys.aic.soap.UnknownProductFault;
 import at.ac.tuwien.infosys.aic.store.DataStore;
 import java.util.logging.Logger;
 import javax.jws.WebService;
 import javax.xml.ws.wsaddressing.W3CEndpointReference;
 import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
 
@WebService(endpointInterface = "at.ac.tuwien.infosys.aic.registry.ServiceRegistry")
 public class ServiceRegistryImpl implements ServiceRegistry {
 
     DataStore ds = DataStore.getInstance();
     Logger log = Logger.getLogger("ServiceRegistryImpl");
 
     @Override
     public W3CEndpointReference getSupplier(Product product) {
 
         log.info("Service Registry called!");
         W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        log.info("before null 1!");
         String endpointAddress = ds.getProductEndpointAddress(product);
        log.info("before null 2!");
         if (endpointAddress != null) {
            log.info("after null!");
             builder.address(endpointAddress);
            log.info("after null 2!");
             return builder.build();
         } else {
             throw new UnknownProductFault();
         }
     }
 }
