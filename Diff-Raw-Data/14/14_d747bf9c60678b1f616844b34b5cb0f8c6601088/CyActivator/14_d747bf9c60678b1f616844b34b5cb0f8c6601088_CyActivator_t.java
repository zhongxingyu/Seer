 package edu.ucsf.rbvi.setsApp.internal;
 
 import static org.cytoscape.work.ServiceProperties.COMMAND;
 import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
 import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
 import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
 import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
 import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
 import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
 import static org.cytoscape.work.ServiceProperties.TITLE;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.cytoscape.application.CyApplicationManager;
 import org.cytoscape.application.swing.CySwingApplication;
 import org.cytoscape.application.swing.CytoPanelComponent;
 import org.cytoscape.io.BasicCyFileFilter;
 import org.cytoscape.io.DataCategory;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.service.util.AbstractCyActivator;
 import org.cytoscape.session.events.SessionLoadedListener;
 import org.cytoscape.task.AbstractNetworkViewTaskFactory;
 import org.cytoscape.task.EdgeViewTaskFactory;
 import org.cytoscape.task.NetworkCollectionTaskFactory;
 import org.cytoscape.task.NetworkTaskFactory;
 import org.cytoscape.task.NetworkViewTaskFactory;
 import org.cytoscape.task.NodeViewTaskFactory;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.view.model.CyNetworkViewManager;
 import org.cytoscape.work.AbstractTaskFactory;
 import org.cytoscape.work.TaskFactory;
 import org.cytoscape.work.TaskManager;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.ucsf.rbvi.setsApp.internal.model.SetOperations;
 import edu.ucsf.rbvi.setsApp.internal.model.SetsManager;
 import edu.ucsf.rbvi.setsApp.internal.tasks.CopySetTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.CreateNodeSetTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.AddEdgeViewTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.AddNodeViewTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.CreateEdgeSetTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.CreateSetFromAttributesTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.CreateSetFromFileTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.CreateSetTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.RemoveEdgeViewTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.RemoveNodeViewTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.RemoveSetTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.RenameSetTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.SetOperationsTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.WriteSetToFileTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.ui.SetsPane;
 
 public class CyActivator extends AbstractCyActivator {
 	private static Logger logger = LoggerFactory
 			.getLogger(edu.ucsf.rbvi.setsApp.internal.CyActivator.class);
 	
 	public CyActivator() {
 		super();
 	}
 
 	public void start(BundleContext bc) {
 		// See if we have a graphics console or not
 		boolean haveGUI = true;
 		CySwingApplication cyApplication = getService(bc, CySwingApplication.class);
 		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
 		CyNetworkManager networkManager = (CyNetworkManager) getService(bc, CyNetworkManager.class);
 		SetsManager sets = new SetsManager(networkManager, cyApplicationManager);
 
 		if (cyApplication == null) {
 			haveGUI = false;
 		} else {
 			SetsPane setsPanel = new SetsPane(bc, sets);
 			registerService(bc,setsPanel,CytoPanelComponent.class, new Properties());
 		}
 
 		registerService(bc,sets,SessionLoadedListener.class, new Properties());
 
 		NodeViewTaskFactory addNode = new AddNodeViewTaskFactory(sets);
 		Properties addNodeToSetProperties = new Properties();
 		setStandardProperties(addNodeToSetProperties, "Add node to set", "addNode", "3.0");
 		registerService(bc,addNode,NodeViewTaskFactory.class,addNodeToSetProperties);
 
 		NodeViewTaskFactory removeNode = new RemoveNodeViewTaskFactory(sets);
 		Properties removeNodeFromSetProperties = new Properties();
 		setStandardProperties(removeNodeFromSetProperties, "Remove node from set", "removeNode", "4.0");
 		registerService(bc,removeNode,NodeViewTaskFactory.class,removeNodeFromSetProperties);
 			
 		EdgeViewTaskFactory addEdge = new AddEdgeViewTaskFactory(sets);
 		Properties addEdgeToSetProperties = new Properties();
 		setStandardProperties(addEdgeToSetProperties, "Add edge to set", "addEdge", "3.0");
 		registerService(bc, addEdge, EdgeViewTaskFactory.class, addEdgeToSetProperties);
 			
 		EdgeViewTaskFactory removeEdge = new RemoveEdgeViewTaskFactory(sets);
 		Properties removeEdgeFromSetProperties = new Properties();
 		setStandardProperties(removeEdgeFromSetProperties, "Remove edge from set", "removeEdge", "4.0");
 		registerService(bc, removeEdge, EdgeViewTaskFactory.class, removeEdgeFromSetProperties);
 			
 		NetworkViewTaskFactory createNodeSetTaskFactory = new CreateNodeSetTaskFactory(sets);
 		Properties createNodeSetProperties = new Properties();
 		setStandardProperties(createNodeSetProperties, "Create node set", null, "1.0");
 		registerService(bc, createNodeSetTaskFactory, NetworkViewTaskFactory.class, createNodeSetProperties);
 
 		Properties createNodeSetProperties2 = new Properties();
 		setStandardProperties(createNodeSetProperties2, "Create node set", null, "1.0");
 		registerService(bc, createNodeSetTaskFactory, TaskFactory.class, createNodeSetProperties2);
 			
 		NetworkViewTaskFactory createEdgeSetTaskFactory = new CreateEdgeSetTaskFactory(sets);
 		Properties createEdgeSetProperties = new Properties();
 		setStandardProperties(createEdgeSetProperties, "Create edge set", null, "2.0");
 		registerService(bc, createEdgeSetTaskFactory, NetworkViewTaskFactory.class, createEdgeSetProperties);
 		
 		Properties createEdgeSetProperties2 = new Properties();
 		setStandardProperties(createEdgeSetProperties2, "Create edge set", null, "1.0");
 		registerService(bc, createEdgeSetTaskFactory, TaskFactory.class, createEdgeSetProperties2);
 		
 		CreateSetTaskFactory createSetTaskFactory = new CreateSetTaskFactory(sets);
 		Properties createSetProperties = new Properties();
		setStandardProperties(createSetProperties, null, "createSet", "3.0");
 		registerService(bc, createSetTaskFactory, TaskFactory.class, createSetProperties);
 		
 		NetworkTaskFactory createNodeSetFromAttributes = new CreateSetFromAttributesTaskFactory(sets, CyNode.class);
 		Properties createNodeSetFromAttributesProperties = new Properties();
 		setStandardProperties(createNodeSetFromAttributesProperties, "Create node set from attributes", null, "1.0");
 		registerService(bc, createNodeSetFromAttributes, NetworkTaskFactory.class, createNodeSetFromAttributesProperties);
 		
 		NetworkTaskFactory createEdgeSetFromAttributes = new CreateSetFromAttributesTaskFactory(sets, CyEdge.class);
 		Properties createEdgeSetFromAttributesProperties = new Properties();
 		setStandardProperties(createEdgeSetFromAttributesProperties, "Create edge set from attributes", null, "2.0");
 		registerService(bc, createEdgeSetFromAttributes, NetworkTaskFactory.class, createEdgeSetFromAttributesProperties);
 		
 		TaskFactory nodeUnion = new SetOperationsTaskFactory(sets, null, SetOperations.UNION);
 		Properties nodeUnionProperties = new Properties();
 		setStandardProperties(nodeUnionProperties, null, "union", "1.0");
 		// nodeUnionProperties.setProperty(PREFERRED_MENU, "Apps.SetsApp.Operations.Nodes");
 		registerService(bc,nodeUnion, TaskFactory.class, nodeUnionProperties);
 		
 		TaskFactory nodeIntersect = new SetOperationsTaskFactory(sets, null, SetOperations.INTERSECT);
 		Properties nodeIntersectProperties = new Properties();
 		setStandardProperties(nodeIntersectProperties, null, "intersect", "2.0");
 		// nodeIntersectProperties.setProperty(PREFERRED_MENU, "Apps.SetsApp.Operations.Nodes");
 		registerService(bc,nodeIntersect, TaskFactory.class, nodeIntersectProperties);
 		
 		TaskFactory nodeDifference = new SetOperationsTaskFactory(sets, null, SetOperations.DIFFERENCE);
 		Properties nodeDifferenceProperties = new Properties();
 		setStandardProperties(nodeDifferenceProperties, null, "difference", "3.0");
 		// nodeDifferenceProperties.setProperty(PREFERRED_MENU, "Apps.SetsApp.Operations.Nodes");
 		registerService(bc,nodeDifference, TaskFactory.class, nodeDifferenceProperties);
 		
 	/*	TaskFactory edgeUnion = new SetOperationsTaskFactory(sets, CyEdge.class, SetOperations.UNION);
 		Properties edgeUnionProperties = new Properties();
 		setStandardProperties(edgeUnionProperties, null, "edgeUnion", "1.0");
 		// edgeUnionProperties.setProperty(PREFERRED_MENU, "Apps.SetsApp.Operations.Edge");
 		registerService(bc,edgeUnion, TaskFactory.class, edgeUnionProperties);
 		
 		TaskFactory edgeIntersection = new SetOperationsTaskFactory(sets, CyEdge.class, SetOperations.INTERSECT);
 		Properties edgeIntersectProperties = new Properties();
 		setStandardProperties(edgeIntersectProperties, null, "edgeIntersect", "2.0");
 		// edgeIntersectProperties.setProperty(PREFERRED_MENU, "Apps.SetsApp.Operations.Edge");
 		registerService(bc,edgeIntersection, TaskFactory.class, edgeIntersectProperties);
 		
 		TaskFactory edgeDifference = new SetOperationsTaskFactory(sets, CyEdge.class, SetOperations.DIFFERENCE);
 		Properties edgeDifferenceProperties = new Properties();
 		setStandardProperties(edgeDifferenceProperties, null, "edgeDifference", "3.0");
 		// edgeDifferenceProperties.setProperty(PREFERRED_MENU, "Apps.SetsApp.Operations.Edge");
 		registerService(bc,edgeDifference, TaskFactory.class, edgeDifferenceProperties); */
 		
 		TaskFactory renameSet = new RenameSetTaskFactory(sets);
 		Properties renameSetProperties = new Properties();
 		setStandardProperties(renameSetProperties, null, "rename", "3.0");
 		registerService(bc, renameSet, TaskFactory.class, renameSetProperties);
 		
 		TaskFactory removeSet = new RemoveSetTaskFactory(sets);
 		Properties removeSetProperties = new Properties();
 		setStandardProperties(removeSetProperties, null, "remove", "4.0");
 		registerService(bc, removeSet, TaskFactory.class, removeSetProperties);
 		
 		TaskFactory createNodeSetFromFile = new CreateSetFromFileTaskFactory(sets, networkManager);
 		Properties setFromFileProperties = new Properties();
 		setStandardProperties(setFromFileProperties, "Import set from file", "import", "5.0");
 		registerService(bc, createNodeSetFromFile, TaskFactory.class, setFromFileProperties);
 
 		NetworkTaskFactory writeNodeSet = new WriteSetToFileTaskFactory(sets, null);
 		Properties writeNodeSetProperties = new Properties();
 		setStandardProperties(writeNodeSetProperties, null, "export", "6.0");
 		registerService(bc, writeNodeSet, NetworkTaskFactory.class, writeNodeSetProperties);
 		
 	/*	NetworkTaskFactory writeEdgeSet = new WriteSetToFileTaskFactory(sets, CyEdge.class);
 		Properties writeEdgeSetProperties = new Properties();
 		setStandardProperties(writeEdgeSetProperties, null, "writeEdgeSet", "7.0");
 		registerService(bc, writeEdgeSet, NetworkTaskFactory.class, writeEdgeSetProperties); */
 		
 		NetworkCollectionTaskFactory copySet = new CopySetTaskFactory(sets);
 		Properties copySetProperties = new Properties();
 		setStandardProperties(copySetProperties, null, "copy", "4.5");
 		registerService(bc, copySet, NetworkCollectionTaskFactory.class, copySetProperties);
 		
 
 		// Now that everything is initialized, ask the SetsManager to look for any existing
 		// sets
 		sets.initialize();
 	}
 	
 	private void setStandardProperties(Properties p, String title, String command, String gravity) {
 		if (title != null) {
 			p.setProperty(TITLE, title);
 			p.setProperty(PREFERRED_MENU, "Apps.SetsApp");
 			p.setProperty(IN_MENU_BAR,"false");
 			p.setProperty(MENU_GRAVITY, gravity);
 		}
 		if (command != null) {
 			p.setProperty(COMMAND,command);
 			p.setProperty(COMMAND_NAMESPACE,"setsApp");
 		}
 	}
 }
