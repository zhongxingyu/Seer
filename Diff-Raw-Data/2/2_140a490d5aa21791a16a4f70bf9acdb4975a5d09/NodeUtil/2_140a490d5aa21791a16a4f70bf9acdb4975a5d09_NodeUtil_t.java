 package ch.hsr.bieridee.utils;
 
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.NotFoundException;
 
 import ch.hsr.bieridee.exceptions.WrongNodeTypeException;
 
 /**
  * Node utilities.
  * 
  */
 public final class NodeUtil {
 
 	private NodeUtil() {
 		// do not instanciate.
 	}
 
 	/**
 	 * Checks if a given node is valid in terms of type and availabiltiy.
 	 * 
 	 * @param node
 	 *            The nodes to be checked
 	 * @param expectedType
 	 *            The name of the expected node type
 	 * @throws WrongNodeTypeException
 	 *             Thrown if the node is of the wrong type
 	 * @throws NotFoundException
 	 *             Thrown if the node is not present/null
 	 * @return true if the node is valid
 	 */
 	public static boolean checkNode(Node node, String expectedType) throws WrongNodeTypeException, NotFoundException {
 		if (node == null) {
 			throw new NotFoundException("Node is null");
 		}
 		String type = null;
 		try {
 			type = (String) node.getProperty("type");
 		} catch (NotFoundException e) {
 			throw new WrongNodeTypeException(e);
 		}
 		if (!expectedType.equals(type)) {
			throw new WrongNodeTypeException("Not a " + expectedType + " node. Type is " + type + ".");
 		}
 		return true;
 	}
 
 }
