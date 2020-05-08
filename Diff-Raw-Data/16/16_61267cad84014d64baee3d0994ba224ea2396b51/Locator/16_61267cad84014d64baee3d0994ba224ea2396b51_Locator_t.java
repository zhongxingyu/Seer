 /**
  * 
  */
 package utils;
 
 import models.Node;
 
 import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.ow2.play.governance.api.EventGovernance;
 import org.ow2.play.metadata.api.service.MetadataBootstrap;
 import org.ow2.play.metadata.api.service.MetadataService;
 import org.ow2.play.service.registry.api.Constants;
 import org.ow2.play.service.registry.api.Registry;
 
 /**
  * Deprecated...
  * 
  * @author chamerling
  * 
  */
 public class Locator {
 
 	public static MetadataService getMetaService(Node node) throws Exception {
 
 		Registry registry = getServiceRegistry(node);
 		String url = registry.get(Constants.METADATA);
 
 		if (url == null) {
 			throw new Exception(
 					"Can not find the metadata endpoint in the registry");
 		}
 
 		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
 		factory.setAddress(url);
 		factory.setServiceClass(MetadataService.class);
 		Object o = factory.create();
 		return MetadataService.class.cast(o);
 	}
 
 	public static MetadataBootstrap getMetaBootstrap(Node node)
 			throws Exception {
 
 		Registry registry = getServiceRegistry(node);
 		String url = registry.get(Constants.METADATA + ".boot");
 
 		if (url == null) {
 			throw new Exception(
 					"Can not find the metadata endpoint in the registry");
 		}
 
 		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
 		factory.setAddress(url);
 		factory.setServiceClass(MetadataBootstrap.class);
 		Object o = factory.create();
 		return MetadataBootstrap.class.cast(o);
 	}
 
 	public static Registry getServiceRegistry(Node node) {
 		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
 		factory.setAddress(node.baseURL);
 		factory.setServiceClass(Registry.class);
 		Object o = factory.create();
 		return Registry.class.cast(o);
 	}
 
 	public static EventGovernance getEventGovernance(Node node)
 			throws Exception {
 		Registry registry = getServiceRegistry(node);
 		String url = registry.get(Constants.GOVERNANCE);
 
 		if (url == null) {
 			throw new Exception(
 					"Can not find the topic endpoint in the registry");
 		}
 
 		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
 		factory.setAddress(url);
 		factory.setServiceClass(EventGovernance.class);
 		Object o = factory.create();
 		return EventGovernance.class.cast(o);
 	}
 
 }
