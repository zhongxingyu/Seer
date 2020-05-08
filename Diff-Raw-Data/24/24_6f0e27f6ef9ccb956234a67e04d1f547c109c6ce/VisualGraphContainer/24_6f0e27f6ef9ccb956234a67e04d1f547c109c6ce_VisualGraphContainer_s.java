 package de.bwvaachen.graph.gui.input.visualgraph;
 
 import java.awt.Point;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import com.fasterxml.jackson.core.JsonGenerationException;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 
 import de.bwvaachen.graph.logic.Graph;
 import de.bwvaachen.graph.logic.Node;
 
 public class VisualGraphContainer {
 
 	
 	private Graph graph;
 	private HashMap<Node, Point>pointMap=new HashMap<Node, Point>();
 	private VisualGraphProperties properties;
 	public void setGraph(Graph graph) {
 		this.graph = graph;
 	}
 	public void setVisualNodeMap(HashMap<Node, Point> visualNodeMap) {
 		this.pointMap = visualNodeMap;
 	}
 	public VisualGraphContainer(Graph graph,  HashMap<Node, VisualNode> visualNodeMap, VisualGraphProperties properties) {
 		this.graph=graph;
 		if(visualNodeMap!=null)
 		for(Entry<Node,VisualNode>entry:visualNodeMap.entrySet())
		this.pointMap.put(entry.getKey(), entry.getValue().getLocation());
 		this.properties=properties;
 	}
 	public VisualGraphContainer(Graph graph,  HashMap<Node, Point> pointMap, VisualGraphProperties properties, boolean Signaturvernderer) {
 		this.graph=graph;
 		if(pointMap!=null)
 		this.pointMap=pointMap;
 		this.properties=properties;
 	}
 	public void save(String filePath) throws JsonGenerationException, JsonMappingException, IOException
 	{
 		new SaveContainer(graph, pointMap,properties).save(filePath);
 	}
 	public static VisualGraphContainer load(String filePath) throws JsonProcessingException, IOException
 	{
 		SaveContainer saveContainer=SaveContainer.load(filePath);
 		HashMap<Node, Point>pointMap=new HashMap<Node, Point>();
 		for(Entry<Node,MyPoint>entry:saveContainer.getPointMap().entrySet())
 			pointMap.put(entry.getKey(), entry.getValue().getPoint());
 		 return new VisualGraphContainer(saveContainer.getGraph(), pointMap,saveContainer.getProperties().getProperties(),false);//new SaveContainer(graph, pointMap);
 	}
 	public HashMap<Node, Point> getPointMap() {
 		return pointMap;
 	}
 	public void setPointMap(HashMap<Node, Point> pointMap) {
 		this.pointMap = pointMap;
 	}
 	public Graph getGraph() {
 		return graph;
 	}
 	public VisualGraphProperties getProperties() {
 		return properties;
 	}
 	
 }
