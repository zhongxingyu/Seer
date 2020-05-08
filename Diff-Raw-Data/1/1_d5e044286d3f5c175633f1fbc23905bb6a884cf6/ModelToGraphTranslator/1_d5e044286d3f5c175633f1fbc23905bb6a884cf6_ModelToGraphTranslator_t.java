 package org.jims.modules.crossbow.gui.actions;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.zest.core.widgets.Graph;
 import org.eclipse.zest.core.widgets.GraphConnection;
 import org.eclipse.zest.core.widgets.GraphContainer;
 import org.eclipse.zest.core.widgets.GraphItem;
 import org.eclipse.zest.core.widgets.GraphNode;
 import org.eclipse.zest.core.widgets.IContainer;
 import org.jims.modules.crossbow.gui.NetworkStructureHelper;
 import org.jims.modules.crossbow.gui.data.GraphConnectionData;
 import org.jims.modules.crossbow.objectmodel.Assignments;
 import org.jims.modules.crossbow.objectmodel.ObjectModel;
 import org.jims.modules.crossbow.objectmodel.policy.Policy;
 import org.jims.modules.crossbow.objectmodel.resources.Appliance;
 import org.jims.modules.crossbow.objectmodel.resources.ApplianceType;
 import org.jims.modules.crossbow.objectmodel.resources.Endpoint;
 import org.jims.modules.crossbow.objectmodel.resources.Interface;
 import org.jims.modules.crossbow.objectmodel.resources.Switch;
 
 
 public class ModelToGraphTranslator {
 	
 	public enum Element {
 		GRAPH_NODE,
 		GRAPH_EDGE
 	}
 	
 	private List<GraphConnectionData> graphConnectionDataList;
 	
 	// TODO  v it has to be factored out from here!!!
 	private IContainer getContainer( Graph g, String id ) {
 		
 		IContainer res = g;
 		
 		if ( null != id ) {
 		
 			for ( Object o : g.getNodes() ) {
 			
 				if ( ( o instanceof GraphContainer )
 						&& ( id.equals( ( ( GraphContainer ) o ).getText() ) ) ) {
 					
 					res = ( GraphContainer ) o;
 					break;
 					
 				}
 				
 			}
 			
 		}
 		
 		return res;
 		
 	}
 	
 	
 public void translate( Graph graph, ObjectModel om, Assignments assignments,
                        NetworkStructureHelper networkStructureHelper,
                        List< GraphConnectionData > graphConnectionDataList ) {
 		
 		this.graphConnectionDataList = graphConnectionDataList;
 		
 		networkStructureHelper.clearAllItems();
 		
 		Map< Object, GraphNode > nodes = new HashMap< Object, GraphNode >();
 		
 		logger.debug("Restoring deployed appliances in total number : " + om.getAppliances().size());
 		
 		for ( Appliance machine : om.getAppliances( ApplianceType.MACHINE ) ) {
 			logger.debug( "Restoring deployed Appliance (name: " + machine.getResourceId() + ")." );
 			GraphItem item = createGraphItem( graph, machine, "icons/resource.jpg", nodes );
 			networkStructureHelper.addDeployedElement( machine, item );
 		}
 		
 		for ( Appliance router : om.getAppliances( ApplianceType.ROUTER ) ) {
 			logger.debug( "Restoring deployed Router (name: " + router.getResourceId() + ")." );
 			GraphItem item = createGraphItem( graph, router, "icons/router.jpg", nodes );
 			networkStructureHelper.addDeployedElement( router, item );
 		}
 		
 		for ( Switch s : om.getSwitches() ) {
 			logger.debug("Restoring deployed Switch");
 			GraphItem item = createGraphItem( getContainer( graph, assignments.get( s ) ), s, "icons/switch.jpg", nodes );
 			networkStructureHelper.addDeployedElement(s, item);
 		}
 		
 		for(Policy policy : om.getPolicies()) {
 			
 		}
 		
 		restoreGraphNodeConnections( graph, om, nodes );
 		
 		networkStructureHelper.deployed();
 		
 		graph.applyLayout();
 		
 	}
 	
 	
 	public void setColor( Element element, Color color ) {
 		colors.put( element, color );
 	}
 	
 	
 	private GraphItem createGraphItem( IContainer container, Object g, String iconPath,
 	                              Map< Object, GraphNode > nodes ) {
 
 		GraphNode graphNode = new GraphNode( container, SWT.NONE, "");
 		graphNode.setBackgroundColor( colors.get( Element.GRAPH_NODE ) );
 				
 		graphNode.setImage(loadImage(iconPath));
 		graphNode.setData(g);
 		String toolTipText = updateGraphNodeToolTip(g);
 		graphNode.setTooltip(new org.eclipse.draw2d.Label(toolTipText));
 
 		graphNode.setText(toolTipText);
 		
 		nodes.put( g, graphNode );
 		
 		return graphNode;
 
 	}
 	
 	
 	private Image loadImage(String fileName) {
 
 		ImageDescriptor descriptor = ImageDescriptor.createFromFile(null, fileName);
 		return descriptor.createImage();
 		
 	}
 	
 	
 	private String updateGraphNodeToolTip( Object obj ) {
 
 		StringBuilder sb = new StringBuilder();
 		if (obj instanceof Appliance) {
 
 			Appliance appliance = (Appliance) obj;
 			if (appliance.getType() != null
 					&& appliance.getType().equals(ApplianceType.MACHINE)) {
 				sb.append("RepoId: ");
 				if (appliance.getRepoId() != null)
 					sb.append(appliance.getRepoId());
 			}
 			sb.append("\nResourceId: ");
 			if (appliance.getResourceId() != null)
 				sb.append(appliance.getResourceId());
 		} else if (obj instanceof Switch) {
 
 			Switch swit = (Switch) obj;
 			sb.append("\nResourceId: ");
 			if (swit.getResourceId() != null)
 				sb.append(swit.getResourceId());
 		}
 		sb.append("\n");
 		if (obj instanceof Appliance) {
 			Appliance appliance = (Appliance) obj;
 			for (Interface inter : appliance.getInterfaces()) {
 				if (inter.getIpAddress() != null) {
 					sb.append(inter.getIpAddress().toString() + "\n");
 				}
 			}
 		}
 
 		return sb.toString();
 	}
 	
 	
 	protected void restoreGraphNodeConnections( Graph graph, ObjectModel model,
 	                                            Map< Object, GraphNode > nodes ) {
 		
 		for ( Appliance app : model.getAppliances() ) {
 			
 			for ( Interface iface : app.getInterfaces() ) {
 				
 				System.err.println(iface.getIpAddress().getAddress());
 				
 				createGraphConnectionData(
 					graph,
 					nodes.get( app ), nodes.get( iface.getEndpoint() ),
 					iface, iface.getEndpoint()
 				);
 			}
 			
 		}
 
 	}
 	
 	
 	private void createGraphConnectionData( Graph graph,
 	                                        GraphNode graphNode, GraphNode graphNode2,
 	                                        Endpoint endp1, Endpoint endp2 ) {
 
 		System.err.println(graphNode + " restore " + graphNode2);
 		if (graphNode == null || graphNode2 == null) {
 			return;
 		}
 		GraphConnectionData graphConnectionData = new GraphConnectionData(
 				graphNode.getData(), graphNode2.getData(), endp1, endp2);
 
 		graphConnectionDataList.add(graphConnectionData);
 
 		GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE,
 				graphNode, graphNode2);
 		graphConnection.setData(graphConnectionData);
 		graphConnection.setLineWidth(2);
 		graphConnection.setLineColor( colors.get( Element.GRAPH_EDGE ) );
		graphConnection.setVisible( true );
 		
 	}
 	
 	private static final Logger logger = Logger.getLogger(ModelToGraphTranslator.class);
 	
 	Map< Element, Color > colors = new HashMap< Element, Color >();
 	
 }
