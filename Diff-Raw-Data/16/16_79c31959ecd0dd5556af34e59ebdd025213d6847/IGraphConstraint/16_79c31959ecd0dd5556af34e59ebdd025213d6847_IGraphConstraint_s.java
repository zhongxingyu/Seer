 package be.uclouvain.jail.graph.deco;
 
 /**
  * Defines a constraint to be respected by a graph.
  * 
  * <p>Contraints are the way to ensure some properties on graphs. They typically
  * implement or use a {@link IGraphListener} to listen to graph changes and throw 
 * @{link GraphConstraintViolationException} when changes tend to violate the 
  * graph constraint.</p>
  * 
  * <p>Constraints may be used in two different ways: by installation on graph, or 
  * by external test. The first way uses the following syntax (for some GraphUniqueIndex 
  * constraint, for example):</p>
  * <pre>
  *     // consider some graph instance
  *     DirectedGraph graph = [...]
  *     
  *     // create a unique constraint and install it on graph
  *     GraphUniqueIndex index = new GraphUniqueIndex(...).installOn(graph);
  *     
  *     // the index may also be used after installation
  *     index.get(...)
  * </pre>
  * <p>Warning that, besides the strange API of constraints, the same constraint 
  * instance may not be installed on two different graphs. The only reason why the
  * constraint returns itself at installOn invocation is to provide a simple way to
  * create and install the constraint at once (because, as for GraphUniqueIndex, some 
  * constraints provide useful effects to be used after installation.</p>
  * 
  * <p>The second way is quite simple and has no effect on the graph:</p>
  * <pre>
  *     // consider some graph instance
  *     DirectedGraph graph = [...]
  *     
  *     // just check some graph constraint from the outside
  *     // are vertex ids unique on my graph?
  *     boolean unique = new GraphUniqueIndex(GraphUniqueIndex.VERTEX,"id",true)
  *                         .isRespectedBy(graph):
  * </pre> 
  * 
  * @author blambeau
  */
 public interface IGraphConstraint {
 
 	/** 
 	 * Installs the constraint on the graph. 
 	 * 
 	 * <p>This method must ensure that the constraint is respected on the
 	 * graph argument. It has no effect on the graph when the constraint is
 	 * not respected but throws a violation exception.</p>
 	 * 
 	 * @param graph the graph on which the constraint must be installed.
 	 * @throws GraphConstraintViolatioException if the constraint is not 
 	 * already respected by the graph.
 	 */
 	public <T extends IGraphConstraint> T installOn(DirectedGraph graph) throws GraphConstraintViolationException;
 
 	/** 
 	 * Uninstalls constraint on graph. 
 	 * 
 	 * <p>This method throws an IllegalStateException when the constraint has
 	 * not been previously installed on a graph.</p>
 	 */
 	public void uninstall();
 	
 	/** Returns true if this constraint is respected by the graph, false
 	 * otherwise. */
 	public boolean isRespectedBy(DirectedGraph graph);
 	
 }
