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
			ArrayNode constraintsArray = new ArrayNode(factory);
 			for (int i = 0; i < constraints.size(); i++) {
				constraintsArray.add(generate(factory, constraints.get(i)));
 			}
			root.put("constraints", constraintsArray);
 		}
 
 		return root;
 	}

	private static JsonNode generate(JsonNodeFactory factory, Constraint constraint) {
		ObjectNode node = new ObjectNode(factory);
		node.put(constraint.getName(), constraint.getValue().toString());
		return node;
	}
 }
