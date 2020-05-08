 package eu.play_project.dcep;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.naming.NamingException;
 
 import org.etsi.uri.gcm.util.GCM;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.Factory;
 import org.objectweb.fractal.api.Component;
 import org.objectweb.fractal.api.NoSuchInterfaceException;
 import org.objectweb.fractal.api.control.IllegalLifeCycleException;
 import org.objectweb.proactive.core.ProActiveException;
 import org.objectweb.proactive.core.component.Fractive;
 import org.objectweb.proactive.core.component.adl.FactoryFactory;
 import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
 import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
 import org.objectweb.proactive.core.util.URIBuilder;
 import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
 import org.objectweb.proactive.gcmdeployment.GCMApplication;
 import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 import eu.play_project.dcep.api.DcepManagmentApi;
 import eu.play_project.dcep.distributedetalis.DistributedEtalis;
 import eu.play_project.dcep.distributedetalis.api.ConfigApi;
 import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigLocal;
 
 /**
  * Manage dEtalis instances.
  * @author sobermeier
  *
  */
 public class DcepManager {
 	Logger logger;
 	List<PAComponentRepresentative>  dEtalis; // Mapping between instance name and instance.
 	//String destinations[]= {"127.0.0.1", "dEtalis1.s-node.de"};
 	String destinations[]= {"127.0.0.1"};
 
 	int lastUsedNode;
 	
 	DcepManager(){
 		logger = LoggerFactory.getLogger(this.getClass());
 		dEtalis = new LinkedList<PAComponentRepresentative>();
 	}
 	
 	/**
 	 * Instantiate dEtalises.
 	 */
 	public void init() {
 		createInstances();
 		
 		for (int i = 0; i < destinations.length; i++) {
			System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaaa");
 			try {
 				dEtalis.add(connectToInstance("dEtalis", destinations[i]));
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (NamingException e) {
 				e.printStackTrace();
 			}
 		}	
 	}
 
 	private void createInstances() {
 		CentralPAPropertyRepository.GCM_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");
 
 		for (int i = 0; i < (destinations.length - 1); i++) {
 			try {
 				// Start node
 				GCMApplication gcma = PAGCMDeployment
 						.loadApplicationDescriptor(DistributedEtalis.class
 								.getResource("/dEtalisApplicationDescriptor-"
 										+ i + ".xml"));
 				gcma.startDeployment();
 
 				GCMVirtualNode vn = gcma.getVirtualNode("dEtalis-node");
 				vn.waitReady();
 
 				// Start component.
 				Factory factory = FactoryFactory.getFactory();
 				HashMap<String, GCMApplication> context = new HashMap<String, GCMApplication>(
 						1);
 				context.put("deployment-descriptor", gcma);
 
 				Component root = (Component) factory.newComponent(
 						"DistributedEtalis", context);
 				GCM.getGCMLifeCycleController(root).startFc();
 
 				// Register apis
 				java.rmi.registry.Registry registry = LocateRegistry
 						.getRegistry();
 
 				Fractive.registerByName(root, "dEtalis");
 			} catch (Exception e) {
 				logger.error("Error while instanciating dEtalis instances. "
 						+ e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private PAComponentRepresentative connectToInstance(String name, String host) throws IOException, NamingException{
 		return Fractive.lookup(URIBuilder.buildURI(host, name, "rmi", 1099).toString());
 	}
 	
 	/**
 	 * Get DcepManagmentApi from one instance after the other.
 	 * @return Proxy to dEtalis instance.
 	 */
 	public DcepManagmentApi getManagementApi(){
 		lastUsedNode++;
 		try {
 			return (DcepManagmentApi)dEtalis.get(lastUsedNode%dEtalis.size()).getFcInterface("DcepManagmentApi");
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
 	
 
 
 
