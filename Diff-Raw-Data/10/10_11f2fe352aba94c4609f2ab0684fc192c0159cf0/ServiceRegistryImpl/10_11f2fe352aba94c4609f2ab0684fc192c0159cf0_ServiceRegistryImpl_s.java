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
 
@WebService(endpointInterface = "at.ac.tuwien.infosys.aic.soap.ServiceRegistry")
 public class ServiceRegistryImpl implements ServiceRegistry {
 
     DataStore ds = DataStore.getInstance();
     Logger log = Logger.getLogger("ServiceRegistryImpl");
 
     @Override
     public W3CEndpointReference getSupplier(Product product) {
 
         log.info("Service Registry called!");
         W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
         String endpointAddress = ds.getProductEndpointAddress(product);
         if (endpointAddress != null) {
             builder.address(endpointAddress);
             return builder.build();
         } else {
             throw new UnknownProductFault();
         }
     }
 }
