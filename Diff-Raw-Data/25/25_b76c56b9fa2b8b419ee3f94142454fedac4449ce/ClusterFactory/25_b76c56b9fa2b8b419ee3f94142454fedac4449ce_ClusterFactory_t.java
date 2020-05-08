 package it.polito.dp2.PJS.sol5;
 
 import it.polito.dp2.PJS.Cluster;
import it.polito.dp2.PJS.sol5.gen.PJSInfoService;
 
 import java.net.URL;
 
import javax.xml.namespace.QName;

 public class ClusterFactory extends it.polito.dp2.PJS.ClusterFactory {
 
 	private static Cluster cluster = null;
 	
 	@Override
 	public Cluster newCluster() throws it.polito.dp2.PJS.ClusterException {
 
 		if (cluster == null) {
 			//get service URL
 			String serviceURL = System.getProperty("it.polito.dp2.PJS.sol5.URL");
 			
 			if (serviceURL != null) {
 					try {
						PJSInfoService service = new PJSInfoService(new URL(serviceURL), new QName("http://pad.polito.it/PJSInfo", "PJSInfoService"));
 						
 						cluster = this.parse(service);
 					} catch (Exception e) {
 						e.printStackTrace();
 						throw new it.polito.dp2.PJS.ClusterException("Bad service URL provided...");
 					}
 			} else {
 				throw new it.polito.dp2.PJS.ClusterException("No service URL provided. Set 'it.polito.dp2.PJS.sol5.URL' property with service URL...");
 			}
 		}
 		return cluster;
 	}
 	
 	private Cluster parse(PJSInfoService service) {
 		return new ClusterImpl(service);
 	}
 }
