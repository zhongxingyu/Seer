 package de.uni_leipzig.simba.saim;
 
 import giny.view.NodeView;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Window.Notification;
 
 import csplugins.layout.algorithms.force.ForceDirectedLayout;
 import cytoscape.Cytoscape;
 
 import de.uni_leipzig.simba.saim.core.Configuration;
 import de.uni_leipzig.simba.saim.core.metric.Measure;
 import de.uni_leipzig.simba.saim.core.metric.MetricParser;
 import de.uni_leipzig.simba.saim.core.metric.Node;
 import de.uni_leipzig.simba.saim.core.metric.Operator;
 import de.uni_leipzig.simba.saim.core.metric.Output;
 import de.uni_leipzig.simba.saim.core.metric.Property;
 import de.uni_leipzig.simba.saim.core.metric.Property.Origin;
 import de.uni_leipzig.simba.saim.cytoprocess.Cytoprocess;
 import de.uni_leipzig.simba.saim.cytoprocess.PaintController;
 /**
  * 
  * @author rspeck
  *
  */
 public class SAIMCytoprocess extends Cytoprocess {
 
 	private static final long serialVersionUID = 3569445479906834120L;
 
 	private Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
 
 	public enum NODE_TYPE{
 		OUTPUT,
 		SOURCE, 
 		TARGET,
 		MEASURE,
 		OPERATOR
 	}
 
 	//SAIMApplication
 	private final Messages messages;
 	//SAIMApplication
 	private Configuration config;
 	//SAIMApplication
 	private Window mainWindow;
 	
 	private SAIMCytoprocessModalWindows modal;
 	private SAIMCytoprocessProperties properties;
 
 	public SAIMCytoprocess(int width, int height, SAIMApplication saimApp) {
 		super(width, height);
 
 		config = saimApp.getConfig();
 		messages = saimApp.messages;	
 		mainWindow = saimApp.getMainWindow();
 		
 		modal = new SAIMCytoprocessModalWindows(messages,config,mainWindow);
 		
 		properties = new SAIMCytoprocessProperties();
 	}
 
 	public void loadMetricExpression(){
 		loadMetricExpression(config.getMetricExpression());
 	}
 	
 	public void loadMetricExpression(String metricExpression){
 
 		if( metricExpression != null){
 			Output o = MetricParser.parse(metricExpression, config.getSource().var.replaceAll("\\?", ""));
 			o.param1 = config.getAcceptanceThreshold();
 			o.param2 = config.getVerificationThreshold();
 			
 			makeMetric(o, -1);
 					
 			if(
 					!metricExpression.trim().equals(o.toString().trim()) ||
 					!metricExpression.trim().equals(getMetric().toString().trim())
 					) {
 				LOGGER.error("Metric parse error!");
 
 			}
 			
 			if(LOGGER.isDebugEnabled()){
 				LOGGER.debug(("metricExpression from File: \t\t" + metricExpression));
 				LOGGER.debug(("metricExpression from MetricParser: \t" + o.toString()));
 				LOGGER.debug(("metricExpression from current Metric: \t" + getMetric().toString()));
 			}
 			
 			applyLayoutAlgorithm(new ForceDirectedLayout(),false);
 			repaintGraph();	
 						
 		}else{
 			Node n = new Output();
 			addNode(n.id, NODE_TYPE.OUTPUT);	
 			repaintGraph();
 		}
 	}
 
 	@Override
 	public void deleteEdge(int id){		
 		if(LOGGER.isDebugEnabled())LOGGER.debug("deleteEdge ...");
 		
 		giny.model.Edge edge = graphProperties.getEdge(id);
 		if(edge != null){			
 			Node nodeA = nodeMap.get(Integer.valueOf(edge.getSource().getRootGraphIndex()));
 			Node nodeB = nodeMap.get(Integer.valueOf(edge.getTarget().getRootGraphIndex()));
 			
 			if(nodeA != null && nodeB != null){
 				if(nodeA.getChilds().contains(nodeB))
 					nodeA.removeChild(nodeB);
 				else 
 					nodeB.removeChild(nodeA);
 			}else
 				LOGGER.error("Node to remove not found!");
 			
 			super.deleteEdge(id);
 			
 			if(nodeA instanceof Operator)
 				setOperatorEdgeLabels(Integer.valueOf(edge.getSource().getRootGraphIndex()));
 			
 			repaintGraph();
 		}
 	}
 	/**
 	 * Gets the metrics output node / root node
 	 */
 	public Node getMetric(){
 		for(Node n : nodeMap.values())
 			if(n instanceof Output)
 				return n;
 		return null;
 	}
 	
 	@Override
 	public void deleteNode(int id){
 		if(LOGGER.isDebugEnabled())LOGGER.debug("deleteNode ...");
 		
 		Node n = nodeMap.remove(id);
 		
		if(n != null){		
 			for(Node node : nodeMap.values())
				for(Node child : node.getChilds())
					if(child.hashCode() == n.hashCode()){
						node.removeChild(child);
						break;
					}
 	
 			while(n.getChilds().size()>0)
 				n.removeChild(n.getChilds().get(0));
 			
 			super.deleteNode(id);
 			
 			repaintGraph();
 		}else
 			if(LOGGER.isDebugEnabled())LOGGER.debug("node not found!");
 	}
 	
 	@Override
 	public void nodeDoubleClick(int nodeid, int x, int y){
 
 		Node n = nodeMap.get(nodeid);
 		
 		if(n instanceof Output){
 			if(LOGGER.isDebugEnabled())LOGGER.debug("Output nodeDoubleClick ...");
 			
 			modal.makeOutputModalWindow(this, nodeid, (Output)n);
 			
 		}else if(n instanceof Measure ){
 			if(LOGGER.isDebugEnabled())LOGGER.debug("Measure nodeDoubleClick ... ...");
 			
 		}else if(n instanceof Operator ){
 			if(LOGGER.isDebugEnabled())LOGGER.debug("Operator nodeDoubleClick ... ...");
 			
 			modal.makeOperatorModalWindow(this, nodeid, (Operator)n);
 			
 		}else if(n instanceof Property){
 			Property p = (Property)n;
 			
 			String nodeLabel = 	Cytoscape.getNodeAttributes().getStringAttribute(nodeid+"", "label");
 			
 			if(p.getOrigin() == Origin.SOURCE){
 				if(LOGGER.isDebugEnabled())LOGGER.debug("SOURCE nodeDoubleClick ... ...");
 				
 				modal.makePropertiesModalWindow(nodeLabel.substring(nodeLabel.indexOf('.')+1),config.getSource());
 				
 			}else if(p.getOrigin() == Origin.TARGET){
 				if(LOGGER.isDebugEnabled())LOGGER.debug("TARGET nodeDoubleClick ... ...");
 				
 				modal.makePropertiesModalWindow(nodeLabel.substring(nodeLabel.indexOf('.')+1),config.getTarget());
 			}
 		}			
 		//super.nodeDoubleClick(nodeid, x, y);
 	}
 	
 //	@Override
 //	public void edgeDoubleClick(String edgeid, int x, int y){
 //		currentGraphOperation = GraphOperation.UPDATE_EDGE;
 //		requestRepaint();
 //	}
 
 	public Integer addNode(String name, SAIMCytoprocess.NODE_TYPE shape, Double para1, Double para2){
 		
 		int nodeView = 0;
 		String rgb = "";
 		Node n = null;		
 
 		switch(shape){			
 		case OUTPUT :{
 			n = new Output(); 
 			nodeView = NodeView.ELLIPSE;			
 			rgb = PaintController.getRGB(Color.decode(properties.getProperty(SAIMCytoprocessProperties.OUTPUT_COLOR)));
 			break;
 		}
 		case SOURCE:{
 			n = new Property(name, Origin.SOURCE);			
 			nodeView = NodeView.RECTANGLE;
 			rgb = PaintController.getRGB(Color.decode(properties.getProperty(SAIMCytoprocessProperties.SOURCE_COLOR)));
 			break;
 		}
 		case TARGET:{
 			n = new Property(name, Origin.TARGET);
 			nodeView = NodeView.RECTANGLE;
 			rgb = PaintController.getRGB(Color.decode(properties.getProperty(SAIMCytoprocessProperties.TARGET_COLOR)));
 			break;
 		}
 		case MEASURE:{
 			n = new Measure(name); 
 			nodeView = NodeView.DIAMOND;
 			rgb = PaintController.getRGB(Color.decode(properties.getProperty(SAIMCytoprocessProperties.MEASURE_COLOR)));
 			break;
 		}
 		case OPERATOR:{
 			n = new Operator(name); 
 			nodeView = NodeView.HEXAGON;
 			rgb = PaintController.getRGB(Color.decode(properties.getProperty(SAIMCytoprocessProperties.OPERATOR_COLOR)));
 			break;
 		}
 		}
 
 		if(n != null){
 			
 			int id = super.addNode(name, graphProperties.getWidth()/2, graphProperties.getHeight()/2, nodeView, rgb);
 			n.param1 = para1;
 			n.param2 = para2;
 			nodeMap.put(id, n);
 			return id;
 		}
 		return null;
 	}
 	
 	public Integer addNode(String name, SAIMCytoprocess.NODE_TYPE shape){
 		return addNode(name, shape,null,null);
 	}	
 
 	@Override
 	public Integer addEdge(int nodeAid, int nodeBid, String attribute) {
 		
 		if(LOGGER.isDebugEnabled())LOGGER.debug(nodeAid + " : "+ nodeBid);
 		
 		 if(nodeAid == nodeBid)
 			return null;
 
 		Node nodeA = nodeMap.get(nodeAid);
 		Node nodeB = nodeMap.get(nodeBid);
 		
 		if(LOGGER.isDebugEnabled())LOGGER.debug(nodeA.id + " : "+ nodeB.id);
 		
 //		if(!nodeA.isValidParentOf(nodeB) && nodeB.isValidParentOf(nodeA)){
 //			if(LOGGER.isDebugEnabled())LOGGER.debug(" implicit direction is clearly meant the other way around, reverse it");
 //			
 //			addEdge(nodeBid, nodeAid,"");
 //		}
 		if(!nodeA.isValidParentOf(nodeB) && !nodeB.isValidParentOf(nodeA)){
 		
 			mainWindow.showNotification(
 					"Edges between the types "+nodeA.getClass().getSimpleName()+" and "+ nodeB.getClass().getSimpleName() +" are not allowed.",
 					Notification.TYPE_WARNING_MESSAGE);
 			
 		}
 		else if(nodeA.acceptsChild(nodeB)){		
 
 			String label = " ";			
 			if(nodeA instanceof Operator){
 					
 				if(LOGGER.isDebugEnabled())LOGGER.debug("its an operator: " + nodeAid);
 				
 				if(attribute.equals("newEdge")){
 					if(LOGGER.isDebugEnabled())LOGGER.debug("newEdge ...");
 					// edge creation from client
 					label = getFreeOperatorLabel(nodeA,nodeB);
 				}
 				else {
 					// edge creation from server
 					if(LOGGER.isDebugEnabled())LOGGER.debug("parse edges ...");
 					if(nodeA.getChilds().size() == 0){
 						label = (nodeA.param1 != null) ? nodeA.param1.toString() : " ";
 					}else
 						label = (nodeA.param2 != null) ? nodeA.param2.toString() : " ";
 				}
 			}
 			
 			nodeA.addChild(nodeB);
 			Integer id = super.addEdge(nodeAid, nodeBid, label);
 			if( id == null){
 				throw new IllegalStateException("Edge creation failed.");
 			}else {
 				// 
 				return id;
 			}
 		}
 
 		else{
 			mainWindow.showNotification(
 					"Edge from "+nodeB.id+" to "+ nodeA.id +" not allowed: "+nodeA.acceptsChildWithReason(nodeB),
 					Notification.TYPE_WARNING_MESSAGE);
 		}
 		return null;
 	}
 	
 
 	
 	// private
 	private void makeMetric(Output n, int parentId) {
 		
 		// create node and edges
 		makeMetricRecursive((Node)n, parentId);
 			
 		// find all operator ids
 		List<Integer> operatorIDs = new ArrayList<Integer>();
 		for(Entry<Integer, Node> e : nodeMap.entrySet())
 			if(e.getValue() instanceof Operator)
 				if(e.getValue().getChilds().size() > 0)
 					operatorIDs.add(e.getKey());
 		
 		// add operator values as labels to edge
 		for(Integer operatorID : operatorIDs)
 			setOperatorEdgeLabels(operatorID);		
 	}
 	
 
 	/**
 	 * Recursive  function to create a graphical representation out of a output node.
 	 * @param n Call with the Output (root) node.
 	 * @param parentId On call just use an arbitrary value: 
 	 */
 	private void makeMetricRecursive(Node n, int parentId) {
 			
 		if(n.getClass()==Output.class) {
 			parentId = addNode(n);
 		}
 		HashMap<Integer, Node> cList = new LinkedHashMap<Integer, Node>();
 		for(Node c : n.getChilds()) {
 				cList.put(addNode(c), c);
 		}
 		for(Entry<Integer, Node> c : cList.entrySet())
 			addEdge(parentId, c.getKey(),"");	
 		
 		for(Entry<Integer, Node> c : cList.entrySet()) {
 			makeMetricRecursive(c.getValue(), c.getKey());
 		}
 	}
 	
 	private int addNode(Node n){
 		if(LOGGER.isDebugEnabled())LOGGER.debug("addNode...");
 		if(LOGGER.isDebugEnabled())LOGGER.debug(n.id);
 		
 		Integer id = null;
 		// make node
 		if(n instanceof Output){
 			
 			id = addNode(((Output)n).id, NODE_TYPE.OUTPUT,n.param1,n.param2);
 			setOutputValues(id, ((Output)n).param1, ((Output)n).param2);
 			
 		}else if(n instanceof Operator){
 			
 			id = addNode(n.id, NODE_TYPE.OPERATOR,n.param1,n.param2);
 			setOperatorValues(id, ((Operator)n).param1, ((Operator)n).param2);
 			
 		}else if(n instanceof Property){
 		
 				id = addNode(n.id,((Property)n).getOrigin().equals(Property.Origin.TARGET) ?  NODE_TYPE.TARGET : NODE_TYPE.SOURCE,n.param1,n.param2);
 			
 		}else if(n instanceof Measure)
 			id = addNode(n.id, NODE_TYPE.MEASURE,n.param1,n.param2);
 		
 		return id;
 	}
 	// TODO
 	private String getFreeOperatorLabel(Node operator, Node target){
 		
 		if(LOGGER.isDebugEnabled())LOGGER.debug("getFreeOperatorLabel ... ");
 		if(LOGGER.isDebugEnabled())LOGGER.debug("operator:" + operator.toString());
 		
 		String label = " ";
 		if(operator.getChilds().size() <= 0)
 			label = (operator.param1 != null) ? operator.param1.toString() : " ";
 			
 		else if(operator.getChilds().size() >= 2){
 			// something wrong
 			if(LOGGER.isDebugEnabled())LOGGER.debug("Operator has more than 1 child. Size: " + operator.getChilds().size());
 		}else{
 			
 			Node    child      = operator.getChilds().get(0);
 			Integer operatorID = getIDtoNode(operator);
 			Integer childID    = getIDtoNode(child);						
 			
 			
 			giny.model.Node ginyOperator = graphProperties.getNode(operatorID);
 			giny.model.Node ginyChild    = graphProperties.getNode(childID);
 			
 			@SuppressWarnings("unchecked")
 			List<giny.model.Edge> edgeListOut = graphProperties.getCyNetwork().edgesList(ginyOperator, ginyChild);
 			@SuppressWarnings("unchecked")
 			List<giny.model.Edge> edgeListIn  = graphProperties.getCyNetwork().edgesList(ginyChild,    ginyOperator);
 			
 					
 			if(edgeListOut.size() == 0 &&  edgeListIn.size() == 0){
 					if(LOGGER.isDebugEnabled())LOGGER.debug("No edge found, edge count: " + graphProperties.getCyNetwork().getEdgeCount());
 			}
 			else {
 				
 				String usedlabel = Cytoscape.getEdgeAttributes().getStringAttribute(String.valueOf(edgeListOut.get(0).getRootGraphIndex()), "label").trim();
 				
 				String opPara1 = (operator.param1 != null) ? operator.param1.toString() : " ";
 				String opPara2 = (operator.param2 != null) ? operator.param2.toString() : " ";
 				
 				if( usedlabel.trim().equals(opPara1))
 					label = String.valueOf(opPara2);
 				else
 					label = String.valueOf(opPara1);
 			}
 			
 		}
 		return label;
 	}
 
 	public void setOperatorEdgeLabels(Integer operatorID){
 		
 		if(operatorID == null) return;
 		if(LOGGER.isDebugEnabled())LOGGER.debug("setOperatorEdgeLabels ...");
 		
 		Node n = nodeMap.get(operatorID);
 		if(n != null && n instanceof Operator){
 			
 			Operator operator = (Operator)n;
 			giny.model.Node ginyOperator = graphProperties.getNode(operatorID);
 			if(operator.getChilds().size() > 0){
 				
 				Node childA = operator.getChilds().get(0);			
 				Integer childAID = getIDtoNode(childA);
 				giny.model.Node ginyChildA = graphProperties.getNode(childAID);
 				//int[] edgeids = graphProperties.getCyNetwork().getAdjacentEdgeIndicesArray(operatorID, false, false, true);
 				@SuppressWarnings("unchecked")
 				List<giny.model.Edge> edgeListA = graphProperties.getCyNetwork().edgesList(ginyOperator, ginyChildA);
 				Cytoscape.getEdgeAttributes().setAttribute(String.valueOf(edgeListA.get(0).getRootGraphIndex()), "label", String.valueOf(operator.param1));
 			}
 			
 			if(operator.getChilds().size() == 2){
 				
 				Node childB = operator.getChilds().get(1);
 				Integer childBID = getIDtoNode(childB);
 				giny.model.Node ginyChildB = graphProperties.getNode(childBID);
 				//int[] edgeids = graphProperties.getCyNetwork().getAdjacentEdgeIndicesArray(operatorID, false, false, true);
 				@SuppressWarnings("unchecked")
 				List<giny.model.Edge> edgeListB = graphProperties.getCyNetwork().edgesList(ginyOperator, ginyChildB);
 				Cytoscape.getEdgeAttributes().setAttribute(String.valueOf(edgeListB.get(0).getRootGraphIndex()), "label", String.valueOf(operator.param2));
 			}
 		}
 	}
 	
 	private Integer getIDtoNode(Node node){
 		Integer id = null;
 		for(Entry<Integer, Node> entry: nodeMap.entrySet()){
 			if(entry.getValue().hashCode() == node.hashCode()){
 				id = entry.getKey();
 				break;
 			}
 		}
 		return id;
 	}
 	public void setOperatorEdgeLabels(Operator operator){
 		setOperatorEdgeLabels(getIDtoNode(operator));
 	}
 	
 	public void setOperatorValues(int nodeid, Double value1, Double value2){
		//String label1 = messages.getString("Cytographer.modalWindowTextField1LabelOperator");
		//String label2 = messages.getString("Cytographer.modalWindowTextField2LabelOperator");
 		//TODO add an other label for operator values to messsage class maybe?
 //		String label1 = "edge 1";
 //		String label2 = "edge 2";
 		
		//setNodeValues(nodeid,label1, value1, label2, value2);		
 	}
 
 	public void setOutputValues(int nodeid, Double value1, Double value2){
 		String label1 = messages.getString("Cytographer.modalWindowTextField1LabelOutput");
 		String label2 = messages.getString("Cytographer.modalWindowTextField2LabelOutput");
 		
 		setNodeValues(nodeid,label1, value1, label2, value2);		
 	}
 	private void setNodeValues(int nodeid, String label1, Double value1, String label2, Double value2){
 		setNodeValue1(nodeid,label1, value1);
 		setNodeValue2(nodeid,label2,value2);
 	}
 	
 	private void setNodeValue1(int nodeid, String label1, Double value1){
 		Cytoscape.getNodeAttributes().setAttribute(String.valueOf(nodeid), "label1", label1);
 		Cytoscape.getNodeAttributes().setAttribute(String.valueOf(nodeid), "value1", value1);
 	}
 
 	private void setNodeValue2(int nodeid, String label2, Double value2){
 		Cytoscape.getNodeAttributes().setAttribute(String.valueOf(nodeid), "label2", label2);
 		Cytoscape.getNodeAttributes().setAttribute(String.valueOf(nodeid), "value2", value2);
 	}
 }
