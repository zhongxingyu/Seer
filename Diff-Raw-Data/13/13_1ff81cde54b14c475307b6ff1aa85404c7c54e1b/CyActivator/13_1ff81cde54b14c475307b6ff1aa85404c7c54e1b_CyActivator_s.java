 package org.cytoscape.io.ndex.internal;
 
 import java.util.Properties;
 
 import org.cytoscape.io.ndex.internal.rest.NdexRestClient;
 import org.cytoscape.io.ndex.internal.ui.NdexSearchPanel;
 import org.cytoscape.model.CyNetworkFactory;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.subnetwork.CyRootNetworkManager;
 import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
 import org.cytoscape.view.model.CyNetworkViewFactory;
 import org.cytoscape.view.model.CyNetworkViewManager;
 import org.cytoscape.work.swing.DialogTaskManager;
 import org.osgi.framework.BundleContext;
 
 /**
  * Imports and exports OSGi services.
  */
 public class CyActivator extends AbstractCyActivator {
 
 	public CyActivator() {
 		super();
 	}
 
 	@Override
 	public void start(BundleContext bc) throws Exception {
 
 		Properties properties = new Properties();
 		// registerService(context, sampleAnalyzer, SampleAnalyzer.class,
 		// properties);
 
 		DialogTaskManager taskManager = getService(bc, DialogTaskManager.class);
 		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,
 				CyNetworkFactory.class);
 		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,
 				CyNetworkManager.class);
 		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,
 				CyNetworkViewFactory.class);
 		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
 		CyRootNetworkManager cyRootNetworkManagerServiceRef = getService(bc,
 				CyRootNetworkManager.class);
 
 		// TODO create search and import Panel
 		 NdexSearchPanel panel = new NdexSearchPanel(taskManager, cyNetworkManagerServiceRef, cyNetworkViewFactoryServiceRef, cyNetworkViewManagerServiceRef);
 		// TODO create a web client
 		String uri = "http://localhost/";
 		String displayName = "NDEx client";
 		String description = "this is NDEx client";
 		NdexWebServiceClient ndexWebServiceClient = new NdexWebServiceClient(
 				uri, displayName, description, cyNetworkFactoryServiceRef,
 				cyNetworkViewFactoryServiceRef, cyNetworkManagerServiceRef,
 				cyRootNetworkManagerServiceRef, null,panel);
 
 		panel.setRestClient(ndexWebServiceClient.getClient());
 		registerAllServices(bc, ndexWebServiceClient, new Properties());
 
 		// TODO create jdex write task factory
 
 		// TODO: Export NdexBundleReader and Web Service Clients
 	}
 }
