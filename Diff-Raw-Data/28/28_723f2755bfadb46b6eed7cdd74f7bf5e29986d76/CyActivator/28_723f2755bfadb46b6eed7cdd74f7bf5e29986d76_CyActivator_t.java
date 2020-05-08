 package org.cytoscape.fluxviz.internal;
 
 import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
 import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
 import static org.cytoscape.work.ServiceProperties.PREFERRED_ACTION;
 import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
 import static org.cytoscape.work.ServiceProperties.TITLE;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.cytoscape.fluxviz.internal.logic.ColumnsCreator;
 import org.cytoscape.fluxviz.internal.logic.CyActivatorHelper;
 import org.cytoscape.fluxviz.internal.logic.EdgeDefaultsSetter;
 import org.cytoscape.fluxviz.internal.logic.EdgeViewHandler;
 import org.cytoscape.fluxviz.internal.logic.NodeDefaultsSetter;
 import org.cytoscape.fluxviz.internal.logic.NodeViewHandler;
 import org.cytoscape.fluxviz.internal.tasks.StartFlowNetworkViewTaskFactory;
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.events.AddedEdgesListener;
 import org.cytoscape.model.events.AddedNodesListener;
 import org.cytoscape.model.events.NetworkAddedListener;
 import org.cytoscape.service.util.AbstractCyActivator;
 import org.cytoscape.service.util.CyServiceRegistrar;
 import org.cytoscape.task.NetworkViewTaskFactory;
 import org.cytoscape.view.model.CyNetworkViewFactory;
 import org.cytoscape.view.vizmap.VisualMappingManager;
 import org.osgi.framework.BundleContext;
 
 public class CyActivator extends AbstractCyActivator {
 
 	@Override
 	public void start(BundleContext context) throws Exception {
 
 		VisualMappingManager visualMappingManager = getService(context, VisualMappingManager.class);
 		CyNetworkManager cyNetworkManager = getService(context, CyNetworkManager.class);
 		CyServiceRegistrar cyServiceRegistrar = getService(context, CyServiceRegistrar.class);
 		CyNetworkViewFactory cyNetworkViewFactory = getService(context, CyNetworkViewFactory.class);
 		
 		EdgeViewHandler edgeViewHandler = new EdgeViewHandler(cyNetworkViewFactory, visualMappingManager);
 		NodeViewHandler nodeViewHandler = new NodeViewHandler(cyNetworkViewFactory, visualMappingManager);
 		CyActivatorHelper helper = new CyActivatorHelper(cyServiceRegistrar, nodeViewHandler, edgeViewHandler);
 
 	
 		//add app-specific columns to default tables
 		Set<CyNetwork> allNets = new HashSet<CyNetwork>();
 		allNets = cyNetworkManager.getNetworkSet();
 		
 		for(CyNetwork currNet : allNets)
 		{
 			ColumnsCreator.createColumns(currNet, nodeViewHandler, edgeViewHandler);		
 		}
 		
 		cyServiceRegistrar.registerService(new ColumnsCreator(nodeViewHandler, edgeViewHandler), NetworkAddedListener.class, new Properties() );
 		
 		//set defaults for attributes of newly added nodes
 		cyServiceRegistrar.registerService(new NodeDefaultsSetter(nodeViewHandler), AddedNodesListener.class, new Properties());
 		
 		//set defaults for the attributes for newly added edges
 		cyServiceRegistrar.registerService(new EdgeDefaultsSetter(edgeViewHandler), AddedEdgesListener.class, new Properties());
 		
 		//add fluxviz menu to node context menu
 		helper.addNodeSetTypeMenus();
 	  	  	
 		//add fluxviz menu to edge context menu
 		helper.addEdgeSetTypeMenus();
 		
 		//add fluxviz menu to network context menu	
 		Properties startProps = new Properties();
 		startProps.setProperty(PREFERRED_ACTION, "NEW");
 		startProps.setProperty(PREFERRED_MENU, "Apps.FluxViz");
 		startProps.setProperty(MENU_GRAVITY, "10.0f");
 		startProps.setProperty(IN_MENU_BAR, "false");
		startProps.setProperty(TITLE, "Start");
 	  	
 	  	cyServiceRegistrar.registerService(new StartFlowNetworkViewTaskFactory(nodeViewHandler, edgeViewHandler), 
 	  			NetworkViewTaskFactory.class, startProps);
 	}
 
 }
