 package lapd.databases.neo4j;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.imp.pdb.facts.ISet;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.type.TypeStore;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.index.Index;
 
 // Predefined java traversals
 public class Queries {
 	
 	public static ISet recursiveMethodsQ(Iterable<Node> allNodes, IValueFactory vf, Type type, TypeStore ts) throws GraphDbMappingException {
 		Set<IValue> results = new HashSet<IValue>();	
 		for (Node node : allNodes) {
 			if (node.hasRelationship(RelTypes.TO, Direction.OUTGOING)) {
 				for (Relationship rel : node.getRelationships(RelTypes.TO, Direction.OUTGOING)) {
 					if (rel.getEndNode().equals(node))
 						results.add(type.accept(new GraphDbValueRetrievalVisitor(node, vf, ts)));
 				}
 			}
 		}
 		return vf.set(results.toArray(new IValue[results.size()]));
 	}
 	
 	public static ISet reachabilityQ(Node startNode, IValueFactory vf, Type type, TypeStore ts) throws GraphDbMappingException {
 		Set<IValue> results = new HashSet<IValue>();
 		Set<Node> markedNodes = new HashSet<Node>();
 		dfsTraverse(startNode, markedNodes);
 		markedNodes.remove(startNode);
 		for (Node node : markedNodes)
 			results.add(type.accept(new GraphDbValueRetrievalVisitor(node, vf, ts)));
 		return vf.set(results.toArray(new IValue[results.size()]));
 	}
 	
 	private static void dfsTraverse(Node start, Set<Node> markedNodes) {
 		if (!markedNodes.contains(start)) {
 			markedNodes.add(start);
 			for (Relationship rel : start.getRelationships(RelTypes.TO, Direction.OUTGOING))
 				dfsTraverse(rel.getEndNode(), markedNodes);
 		}
 	}
 	
 	public static ISet switchQ(Index<Node> index, IValueFactory vf, Type type, TypeStore ts) throws GraphDbMappingException {
 		Set<IValue> results = new HashSet<IValue>();
 		for (Node switchRefNode : index.get("node", "switch")) {
 			Node switchHead = getHead(switchRefNode);
 			Node switchBodyRefNode = getNextEle(switchHead);
 			if (switchBodyRefNode.hasRelationship(RelTypes.HEAD, Direction.OUTGOING)) { // switch contains statements
 				Node switchBodyHead = getHead(switchBodyRefNode);
 				Node statement = switchBodyHead;
 				boolean hasDefaultCase = false;
 				while (statement.hasRelationship(RelTypes.TO, Direction.OUTGOING)) {
 					if(statement.getProperty("node").toString().equals("defaultCase")) {
 						hasDefaultCase = true;
 						break;
 					}
 					statement = getNextEle(statement);
 				}
 				if (!hasDefaultCase)
 					results.add(type.accept(new GraphDbValueRetrievalVisitor(switchRefNode, vf, ts)));
 			}
 			else
 				results.add(type.accept(new GraphDbValueRetrievalVisitor(switchRefNode, vf, ts)));
 		}
 		return vf.set(results.toArray(new IValue[results.size()]));
 	}
 	
 	public static ISet exceptionQ(Index<Node> index, IValueFactory vf, Type type, TypeStore ts) throws GraphDbMappingException {
 		Set<IValue> results = new HashSet<IValue>();
 		for (Node catchRefNode : index.get("node", "catch")) {
 			Node exceptionNode = getHead(getHead(getHead(getHead(catchRefNode))));
			if (exceptionNode.hasProperty(PropertyNames.STRING))
				if (exceptionNode.getProperty(PropertyNames.STRING).equals("Exception"))
					results.add(type.accept(new GraphDbValueRetrievalVisitor(catchRefNode, vf, ts)));		
 		}
 		return vf.set(results.toArray(new IValue[results.size()]));
 	}
 	
 	private static Node getHead(Node node) {
 		return node.getSingleRelationship(RelTypes.HEAD, Direction.OUTGOING).getEndNode();
 	}
 	
 	private static Node getNextEle(Node node) {
 		return node.getSingleRelationship(RelTypes.TO, Direction.OUTGOING).getEndNode();
 	}
 
 }
