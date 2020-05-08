 package de.fuberlin.wiwiss.ng4j.sparql;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.impl.ModelCom;
 import com.hp.hpl.jena.shared.Lock;
 import com.hp.hpl.jena.shared.LockMutex;
 import com.hp.hpl.jena.sparql.core.DatasetGraph;
 import com.hp.hpl.jena.sparql.core.Quad;
 import com.hp.hpl.jena.sparql.util.Context;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphImpl;
 
 /**
  * Implementation of ARQ's Dataset interface on top of an NG4J NamedGraphSet
  */
 public class NamedGraphDataset implements Dataset, DatasetGraph {
 	private NamedGraphSet set;
 	private Graph defaultGraph;
 	private Lock lock;
 
 	/**
 	 * Creates a new instance whose default graph is the merge of
 	 * all named graphs. 
 	 */
 	public NamedGraphDataset(NamedGraphSet baseNamedGraphSet) {
 		this(baseNamedGraphSet, baseNamedGraphSet.asJenaGraph(null));
 	}
 	
 	/**
 	 * Creates a new instance where one of the named graph is
 	 * used as the default graph. The graph must already exist
 	 * in the NamedGraphSet.
 	 */
 	public NamedGraphDataset(NamedGraphSet baseNamedGraphSet,
 			Node defaultGraphName) {
 		this(baseNamedGraphSet, baseNamedGraphSet.getGraph(defaultGraphName));
 	}
 	
 	/**
 	 * Creates a new instance with a given default graph.
 	 */
 	public NamedGraphDataset(NamedGraphSet baseNamedGraphSet,
 			Graph defaultGraph) {
 		this.set = baseNamedGraphSet;
 		this.defaultGraph = defaultGraph;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.query.Dataset#containsNamedModel(java.lang.String)
 	 */
 	public boolean containsNamedModel(String uri) {
 		return this.set.containsGraph(uri);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.query.Dataset#getDefaultModel()
 	 */
 	public Model getDefaultModel() {
 		return new ModelCom(this.defaultGraph);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.query.Dataset#getNamedModel(java.lang.String)
 	 */
 	public Model getNamedModel(String graphName) {
 		return new ModelCom(this.set.getGraph(graphName));
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.query.Dataset#listNames()
 	 */
 	public Iterator<String> listNames() {
 		final Iterator<NamedGraph> it = this.set.listGraphs();
 		return new Iterator<String>() {
 			
 			/* (non-Javadoc)
 			 * @see java.util.Iterator#next()
 			 */
 			public String next() {
 				return (it.next()).getGraphName().getURI();
 			}
 			
 			/* (non-Javadoc)
 			 * @see java.util.Iterator#hasNext()
 			 */
 			public boolean hasNext() {
 				return it.hasNext();
 			}
 			
 			/* (non-Javadoc)
 			 * @see java.util.Iterator#remove()
 			 */
 			public void remove() {
 				it.remove();
 			}
 		};
 	}
 
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.query.Dataset#getLock()
 	 */
 	public Lock getLock() {
 		// Returns a LockMutex. 
 		// TODO Revisit: I've no idea if this works or what it is supposed to do.
 		if (this.lock == null) {
 			this.lock = new LockMutex();
 		}
 		return this.lock;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.query.Dataset#asDatasetGraph()
 	 */
 	public DatasetGraph asDatasetGraph() {
 		return this;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#containsGraph(com.hp.hpl.jena.graph.Node)
 	 */
 	public boolean containsGraph(Node graphNode) {
 		return set.containsGraph(graphNode);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#getDefaultGraph()
 	 */
 	public Graph getDefaultGraph() {
 		return defaultGraph;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#setDefaultGraph(com.hp.hpl.jena.graph.Graph)
 	 */
 	public void setDefaultGraph ( Graph g ) {
 		defaultGraph = g;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#getGraph(com.hp.hpl.jena.graph.Node)
 	 */
 	public Graph getGraph(Node graphNode) {
 		return set.getGraph(graphNode);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#addGraph(com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Graph)
 	 */
 	public void addGraph(Node graphName, Graph graph)
 	{
 		set.addGraph( new NamedGraphImpl(graphName,graph) );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#removeGraph(com.hp.hpl.jena.graph.Node)
 	 */
 	public void removeGraph(Node graphName)
 	{
 		set.removeGraph( graphName );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#add(com.hp.hpl.jena.sparql.core.Quad)
 	 */
 	public void add(Quad quad)
 	{
 		set.addQuad( convert(quad) );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#delete(com.hp.hpl.jena.sparql.core.Quad)
 	 */
 	public void delete(Quad quad)
 	{
 		set.removeQuad( convert(quad) );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#deleteAny(com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node)
 	 */
 	public void deleteAny(Node g, Node s, Node p, Node o)
 	{
 		de.fuberlin.wiwiss.ng4j.Quad q = new de.fuberlin.wiwiss.ng4j.Quad( (g==null) ? Node.ANY : g,
 		                                                                   (s==null) ? Node.ANY : s,
 		                                                                   (p==null) ? Node.ANY : p,
 		                                                                   (o==null) ? Node.ANY : o );
 		set.removeQuad( q );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#find()
 	 */
 	public Iterator<Quad> find()
 	{
 		return find( Node.ANY, Node.ANY, Node.ANY, Node.ANY );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#find(com.hp.hpl.jena.sparql.core.Quad)
 	 */
 	public Iterator<Quad> find(Quad quad)
 	{
 		return new ConvertingIterator( set.findQuads(convert(quad)) );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#find(com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node)
 	 */
 	public Iterator<Quad> find(Node g, Node s, Node p , Node o)
 	{
 		de.fuberlin.wiwiss.ng4j.Quad q = new de.fuberlin.wiwiss.ng4j.Quad( (g==null) ? Node.ANY : g,
 		                                                                   (s==null) ? Node.ANY : s,
 		                                                                   (p==null) ? Node.ANY : p,
 		                                                                   (o==null) ? Node.ANY : o );
 		return new ConvertingIterator( set.findQuads(q) );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#findNG(com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node)
 	 */
 	public Iterator<Quad> findNG(Node g, Node s, Node p , Node o)
 	{
 		return find( g, s, p, o );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#contains(com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node,com.hp.hpl.jena.graph.Node)
 	 */
 	public boolean contains(Node g, Node s, Node p , Node o)
 	{
 		de.fuberlin.wiwiss.ng4j.Quad q = new de.fuberlin.wiwiss.ng4j.Quad( (g==null) ? Node.ANY : g,
 		                                                                   (s==null) ? Node.ANY : s,
 		                                                                   (p==null) ? Node.ANY : p,
 		                                                                   (o==null) ? Node.ANY : o );
 		return set.containsQuad( q );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#contains(com.hp.hpl.jena.sparql.core.Quad)
 	 */
 	public boolean contains(Quad arqQuad)
 	{
 		return set.containsQuad( convert(arqQuad) );
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#isEmpty()
 	 */
 	public boolean isEmpty()
 	{
 		return set.isEmpty();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#listGraphNodes()
 	 */
 	public Iterator<Node> listGraphNodes() {
 		Set<Node> graphNodes = new HashSet<Node>();
 		for ( Iterator<NamedGraph> it = set.listGraphs(); it.hasNext(); ) {
			NamedGraph ng = (NamedGraph) it.next();
 			graphNodes.add(ng.getGraphName());
 		}
 		return graphNodes.iterator();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.hp.hpl.jena.sparql.core.DatasetGraph#size()
 	 */
 	public long size() {
 		long numGraphs = set.countGraphs();
 		int graphNum = (int) numGraphs;
 		return graphNum;
 	}
 
 	public Context getContext ()
 	{
		return null;
 	}
 
 	/**
 	 * @see com.hp.hpl.jena.query.Dataset#close()
 	 */
 	public void close() {
 		set.close();
 		defaultGraph.close();
 	}
 
 
 	class ConvertingIterator implements Iterator<Quad>
 	{
		final protected Iterator base;
		public ConvertingIterator ( Iterator base ) { this.base = base; }
 		public boolean hasNext () { return base.hasNext(); }
		public Quad next () { return convert( (de.fuberlin.wiwiss.ng4j.Quad) base.next() ); }
 		public void remove () { base.remove(); }
 	}
 
 	static public de.fuberlin.wiwiss.ng4j.Quad convert(Quad arqQuad)
 	{
 		return new de.fuberlin.wiwiss.ng4j.Quad( (arqQuad.getGraph()==null) ? Node.ANY : arqQuad.getGraph(),
 		                                         (arqQuad.getSubject()==null) ? Node.ANY : arqQuad.getSubject(),
 		                                         (arqQuad.getPredicate()==null) ? Node.ANY : arqQuad.getPredicate(),
 		                                         (arqQuad.getObject()==null) ? Node.ANY : arqQuad.getObject() );
 	}
 
 	static public Quad convert(de.fuberlin.wiwiss.ng4j.Quad ng4jQuad)
 	{
 		return new Quad( ng4jQuad.getGraphName(),
 		                 ng4jQuad.getSubject(),
 		                 ng4jQuad.getPredicate(),
 		                 ng4jQuad.getObject() );
 	}
 }
