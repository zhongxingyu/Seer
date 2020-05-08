 package me.bcfh.neoorm;
 
 import java.lang.reflect.Field;
 import java.net.URI;
 import java.util.Collection;
 import java.util.Date;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 
 import jo4neo.ObjectGraph;
 import jo4neo.neo;
 import jo4neo.fluent.Where;
 
 import org.neo4j.cypher.javacompat.ExecutionEngine;
 import org.neo4j.cypher.javacompat.ExecutionResult;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 
 /**
  * This class delegates calls to the ObjectGraph of jo4neo
  * 
  * @author salgmachine
  * @version 0.5.0
  */
 @Stateless
 @LocalBean
 public class NeoORM implements ObjectGraph {
 
 	public NeoORM() {
 
 	}
 
 	public NeoORM(ObjectGraph graph) {
 		this.objectGraph = graph;
 	}
 
 	private Long timestamp = System.currentTimeMillis();
 
 	private ObjectGraph objectGraph;
 
 	private GraphDatabaseService svc;
 
 	void setSvc(GraphDatabaseService svc) {
 		this.svc = svc;
 	}
 
 	GraphDatabaseService getSvc() {
 		return svc;
 	}
 
 	/**
 	 * Executes a Cypher Query String<br/>
 	 * This can be a written String or a constructed Query from CypherDSL
 	 */
 	public ExecutionResult executeQuery(String str) {
 		return new ExecutionEngine(svc).execute(str);
 	}
 
 	@PreDestroy
 	protected void preDestroy() {
 		svc.shutdown();
 	}
 
 	public ObjectGraph getObjectGraph() {
 		return objectGraph;
 	}
 
 	void setObjectGraph(ObjectGraph objectGraph) {
 		this.objectGraph = objectGraph;
 	}
 
 	@PostConstruct
 	protected void init() {
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((timestamp == null) ? 0 : timestamp.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		NeoORM other = (NeoORM) obj;
 		if (timestamp == null) {
 			if (other.timestamp != null)
 				return false;
 		} else if (!timestamp.equals(other.timestamp))
 			return false;
 		return true;
 	}
 
 	@Override
 	public Transaction beginTx() {
 		return this.objectGraph.beginTx();
 	}
 
 	@Override
 	public <A> void persist(A... o) {
 		// for (Object obj : o) {
 		// Collection<? extends Object> ob = get(obj.getClass());
 		// for (Object p : ob) {
 		// for (Field f : p.getClass().getDeclaredFields()) {
 		// if (f.isAnnotationPresent(neo.class)) {
 		// neo n = (neo) f.getAnnotation(neo.class);
 		// if (n != null)
 		// if (n.index() && n.unique()) {
 		// try {
 		// f.setAccessible(true);
 		// Object val1 = f.get(obj);
 		//
 		// Object val2 = obj.getClass()
 		// .getField(f.getName()).get(obj);
 		//
 		// if(){
 		//
 		// }
 		//
 		// System.out.println(f.get(obj));
 		// Collection<Object> col = find(obj)
 		// .where(f.get(obj)).is(f.get(obj))
 		// .results();
 		// System.out.println("found elements "
 		// + col.size());
 		// f.setAccessible(false);
 		// } catch (Exception e) {
 		// e.printStackTrace();
 		// }
 		// }
 		// }
 		// }
 		//
 		// }
 		//
 		// }
 
 		this.objectGraph.persist(o);
 	}
 
 	@Override
 	public void delete(Object... o) {
 		this.objectGraph.delete(o);
 	}
 
 	@Override
 	public Node get(Object o) {
 		return this.objectGraph.get(o);
 	}
 
 	@Override
 	public <T> Collection<T> get(Class<T> type) {
 		return this.objectGraph.get(type);
 	}
 
 	@Override
 	public <T> T get(Class<T> t, long key) {
 		return this.objectGraph.get(t, key);
 	}
 
 	@Override
 	public Object get(Node node) {
 		return this.objectGraph.get(node);
 	}
 
 	@Override
 	public Node get(URI uri) {
 		return this.objectGraph.get(uri);
 	}
 
 	@Override
 	public <T> Collection<T> get(Class<T> type, Iterable<Node> nodes) {
 		return this.objectGraph.get(type, nodes);
 	}
 
 	@Override
 	public void close() {
 		this.objectGraph.close();
 	}
 
 	@Override
 	public <A> Where<A> find(A a) {
 		return this.objectGraph.find(a);
 	}
 
 	@Override
 	public long count(Collection<? extends Object> values) {
 		return this.objectGraph.count(values);
 	}
 
 	@Override
 	public <T> Collection<T> getAddedSince(Class<T> t, Date d) {
 		return this.objectGraph.getAddedSince(t, d);
 	}
 
 	@Override
 	public <T> Collection<T> getAddedBetween(Class<T> t, Date from, Date to) {
 		return this.objectGraph.getAddedBetween(t, from, to);
 	}
 
 	@Override
 	public <T> Collection<T> getMostRecent(Class<T> t, int max) {
 		return this.objectGraph.getMostRecent(t, max);
 	}
 
 	@Override
 	public <T> T getSingle(Class<T> t, String indexname, Object value) {
 		return this.objectGraph.getSingle(t, indexname, value);
 	}
 
 	@Override
 	public <T> Collection<T> get(Class<T> t, String indexname, Object value) {
 		return this.objectGraph.get(t, indexname, value);
 	}
 
 	@Override
 	public <T> Collection<T> fullTextQuery(Class<T> t, String indexname,
 			Object value) {
 		return this.objectGraph.fullTextQuery(t, indexname, value);
 	}
 
 	// @Override
 	// public Node getRefnode() {
 	// return this.objectGraph.getRefnode();
 	// }
 	//
 	// @Override
 	// public Index<Node> getIndexService(boolean fulltext) {
 	// return this.objectGraph.getIndexService(fulltext);
 	// }
 
 	public <T> boolean exists(Class<T> c) {
 		return !this.get(c).isEmpty();
 	}
 
 	public <T> T getSingle(Class<T> c) {
 		Collection<T> list = this.get(c);
 
 		if (list.size() == 1) {
 			return list.iterator().next();
 		}
 		return null;
 	}
 
 }
