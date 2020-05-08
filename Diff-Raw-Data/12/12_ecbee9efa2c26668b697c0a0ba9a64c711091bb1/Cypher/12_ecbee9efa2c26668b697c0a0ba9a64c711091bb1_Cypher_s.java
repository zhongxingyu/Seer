 package ch.hsr.bieridee.utils;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.neo4j.cypher.javacompat.ExecutionEngine;
 import org.neo4j.cypher.javacompat.ExecutionResult;
 import org.neo4j.graphdb.Node;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 /**
  * Class containing all the cipher queries.
  */
 public final class Cypher {
 
 	private static EmbeddedGraphDatabase DB;
 
 	private Cypher() {
 		// do not instantiate.
 	}
 
 	/**
 	 * Returns the nodes found by the given cypher-query as a list.
 	 * 
 	 * @param query
 	 *            The Cypher query.
 	 * @param column
 	 *            Column name containing the Nodes that will be returned.
 	 * @return A List containing the nodes returned by the query.
 	 */
 	public static List<Node> executeAndGetNodes(String query, String column) {
 		final ExecutionEngine engine = new ExecutionEngine(DB);
 		final ExecutionResult result = (ExecutionResult) engine.execute(query);
 		final Iterator<Node> iterator = result.columnAs(column);
 		final List<Node> resultNodes = new LinkedList<Node>();
 		while (iterator.hasNext()) {
 			resultNodes.add(iterator.next());
 		}
 		return resultNodes;
 	}
 
 	/**
 	 * Sets the database.
 	 * 
 	 * @param db
 	 *            The database.
 	 */
 	public static void setDB(EmbeddedGraphDatabase db) {
 		DB = db;
 	}
 
 	/**
 	 * Returns the nodes found by the given cypher-query as a list.
 	 * 
 	 * @param query
 	 *            The cypher query
 	 * @param column
 	 *            Column name containing the Nodes that will be returned.
 	 * @param params
	 *            The parameter will be used as a replacement for the first occurence of the literal '$1' in the cypher
	 *            query
 	 * @return A List containing the nodes returned by the query.
 	 */
 	public static List<Node> executeAndGetNodes(String query, String column, String... params) {
 		for (String param : params) {
 			query = query.replaceFirst("\\$\\$", param);
 		}
 		return executeAndGetNodes(query, column);
 	}
 
 	/**
 	 * @param query
 	 *            The cypher query
 	 * @param column
 	 *            Column name containing the Nodes that will be returned
 	 * @param limit
 	 *            Max. number of Nodes returned.
 	 * @param params
 	 *            These parameters will be used as replacements for the occurences of the literal '$$' in the cypher
 	 *            query (in the given order). First $$ would be replaced by the first params value, second $$ by the
 	 *            params' second and so on.
 	 * @return A List containing the nodes returned by the query.
 	 */
 	public static List<Node> executeAndGetNodes(String query, String column, int limit, String... params) {
 		if (limit <= 0) {
 			return Cypher.executeAndGetNodes(query, column, params);
 		}
 		final String limitClause = " LIMIT " + limit;
 		return Cypher.executeAndGetNodes(query + limitClause, column, params);
 
 	}
 
 	/**
 	 * @param query
 	 *            The cypher query
 	 * @param column
 	 *            Column name containing the Nodes that will be returned.
 	 * @param limit
 	 *            number of Nodes returned.
 	 * @return A List containg the nodes returned by the query.
 	 */
 	public static List<Node> executeAndGetNodes(String query, String column, int limit) {
 		if (limit <= 0) {
 			return Cypher.executeAndGetNodes(query, column);
 		}
 		final String limitClause = " LIMIT " + limit;
 		return executeAndGetNodes(query + limitClause, column);
 	}
 
 	/**
 	 * Returns a single node found by the given cypher-query.
 	 * 
 	 * @param query
 	 *            The cypher query
 	 * @param column
 	 *            Column name containing the Nodes that will be returned.
 	 * @return The desired node or null if none found
 	 */
 	public static Node executeAndGetSingleNode(String query, String column) {
 		final ExecutionEngine engine = new ExecutionEngine(DB);
 		final ExecutionResult result = (ExecutionResult) engine.execute(query);
 		final Iterator<Node> iterator = result.columnAs(column);
 		Node resultNode = null;
 		if (iterator.hasNext()) {
 			resultNode = iterator.next();
 		}
 		return resultNode;
 	}
 
 	/**
 	 * Returns a single node found by the given cypher-query.
 	 * 
 	 * @param query
 	 *            The cypher query
 	 * @param column
 	 *            Column name containing the Nodes that will be returned.
 	 * 
 	 * @param params
 	 *            These parameters will be used as replacements for the occurences of the literal '$$' in the cypher
 	 *            query (in the given order). First $$ would be replaced by the first params value, second $$ by the
 	 *            params' second and so on.
 	 * 
 	 * @return The desired node
 	 */
 	public static Node executeAndGetSingleNode(String query, String column, String... params) {
 		for (String param : params) {
 			query = query.replaceFirst("\\$\\$", param);
 		}
 		return Cypher.executeAndGetSingleNode(query, column);
 	}
 
 	/**
 	 * Returns the double value returned by the given cypher-query.
 	 * 
 	 * @param query
 	 *            The cypher query to be executed
 	 * @param valueName
 	 *            Name of the value in the query
 	 * @return The calculated value
 	 */
 	public static double executeAndGetDouble(String query, String valueName) {
 		final ExecutionEngine engine = new ExecutionEngine(DB);
 		final ExecutionResult result = (ExecutionResult) engine.execute(query);
 		final Iterator<Double> doubles = result.columnAs(valueName);
 		return doubles.next();
 	}
 
 	/**
 	 * @param query
 	 *            The cypher query to be executed
 	 * @param valueName
 	 *            Name of the value in the query
 	 * @param params
 	 *            Parameters to be replaced in the query (in the given order)
 	 * @return Double value.
 	 */
 	public static double executeAndGetDouble(String query, String valueName, String... params) {
 		for (String param : params) {
 			query = query.replaceFirst("\\$\\$", param);
 		}
 		return executeAndGetDouble(query, valueName);
 	}
 
 }
