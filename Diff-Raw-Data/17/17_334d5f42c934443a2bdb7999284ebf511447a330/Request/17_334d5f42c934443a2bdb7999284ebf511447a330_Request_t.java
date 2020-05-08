 package de.uni.stuttgart.informatik.ToureNPlaner.Data;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 
 import java.util.ArrayList;
 
 public class Request {
 
 	private static JsonNode generate(JsonNodeFactory factory, Node node) {
 		ObjectNode o = new ObjectNode(factory);
 		o.put("lt", node.getLaE7());
 		o.put("ln", node.getLoE7());
 		o.put("name", node.getName());
 		return o;
 	}
 
 	public static ObjectNode generate(JsonNodeFactory factory, ArrayList<Node> nodes, ArrayList<Constraint> constraints) {
 		ObjectNode root = new ObjectNode(factory);
 		ArrayNode nodesArray = new ArrayNode(factory);
 
 		for (int i = 0; i < nodes.size(); i++) {
 			nodesArray.add(generate(factory, nodes.get(i)));
 		}
 		root.put("points", nodesArray);
 
 		if (constraints != null && !constraints.isEmpty()) {
			ObjectNode constraintsNode = new ObjectNode(factory);
 			for (int i = 0; i < constraints.size(); i++) {
				if (constraints.get(i).getValue() != null) {
					constraintsNode.put(constraints.get(i).getName(), constraints.get(i).getValue().toString());
				}
 			}
			root.put("constraints", constraintsNode);
 		}
 
 		return root;
 	}
 }
