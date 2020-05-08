 package jo4neo.impl;
 
 import static jo4neo.impl.TypeWrapperFactory.$;
 import static org.neo4j.graphdb.Direction.OUTGOING;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
import javax.persistence.PersistenceException;

 import jo4neo.Nodeid;
 import jo4neo.UniqueConstraintViolation;
 import jo4neo.util.Lazy;
 
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.index.Index;
 
 class PersistOperation<T> {
 
 	IndexedNeo neo;
 	Map<Long, Object> visited;
 
 	public PersistOperation(IndexedNeo neo) {
 		this.neo = neo;
 		visited = new HashMap<Long, Object>();
 	}
 
 	public void save(Object... o) {
 		Transaction t = neo.beginTx();
 		try {
 			for (Object bean : o)
 				save(asNode(bean), bean);
 			t.success();
 		} finally {
 			t.finish();
 		}
 	}
 
 	private void save(Node node, Object o) {
 		/*
 		 * cycle detection object graphs may contain cycles, which would cause
 		 * infinite recursion without this check
 		 */
 		if (visited.containsKey(node.getId()))
 			return;
 		visited.put(node.getId(), node);
 		for (FieldContext field : $(o).getFields(o))
 			save(node, field);
 	}
 
 	private void save(Node node, FieldContext field) {
 
 		if (field.isUnique())
 			saveAndIndexUnique(node, field);
 		if (field.isInverse() || field.isTraverser())
 			initializeIfNull(field);
 		else if (field.isSimpleType())
 			saveAndIndex(node, field);
 		else if (field.isPluralPrimitive())
 			saveAndIndex(node, field);
 		else if (field.isSingular())
 			relate(node, field);
 		else if (field.isPlural())
 			relations(node, field);
 
 	}
 
 	private void initializeIfNull(FieldContext field) {
 		if (field.isPluralComplex())
 			field.setProperty(ListFactory.get(field, new LoadOperation<T>(neo)));
 	}
 
 	private void saveAndIndexUnique(Node node, FieldContext field) {
 		field.applyTo(node);
 
 		Object o = field.subject;
 		for (java.lang.reflect.Field f : o.getClass().getDeclaredFields()) {
 			Object fieldval;
 			try {
 				f.setAccessible(true);
 				fieldval = f.get(o);
 				if (fieldval instanceof Nodeid) {
 					Nodeid Id = (Nodeid) fieldval;
 					System.out.println("node id " + Id.id());
 
 				}
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
		// if (index() == null)
		// throw new RuntimeException("index() was null");
		// if (field == null)
		// throw new RuntimeException("field was null");
		// if (field.getIndexName() == null)
		// throw new RuntimeException("field.getIndexName() was null");

		if (field.value() == null)
			throw new PersistenceException(
					"Tried to insert a null Value into a unique Field [" + field.getFieldname() + "] on node " + field.subject);

 		Iterator<Node> n = index().query(field.getIndexName(), field.value())
 				.iterator();
 
 		List<Node> nodes = new ArrayList<Node>();
 
 		List<Long> idlist = new ArrayList<Long>();
 
 		while (n.hasNext()) {
 			Node node2 = n.next();
 			nodes.add(node2);
 			idlist.add(new Long(node2.getId()));
 		}
 
 		boolean found = false;
 		for (Node node2 : nodes) {
 			// System.out.println("iterator node id " + node2.getId());
 			for (String s : node2.getPropertyKeys()) {
 				System.out.println(" { " + s + " : " + node2.getProperty(s)
 						+ " }");
 				if (node2.getProperty(s).equals(field.value())) {
 					found = true;
 				}
 			}
 
 			// System.err.println("found already " + found);
 		}
 		if (!found) {
 			// handle persist
 			index().remove(node, field.getIndexName(), field.value());
 			index().add(node, field.getIndexName(), field.value());
 			System.err.println("persisted");
 		} else {
 			Node node2 = index().getGraphDatabase().getNodeById(node.getId());
 			// System.err.println("found node " + node2.getId());
 			boolean found2 = idlist.contains(node2.getId());
 			// handle update
 			if (found2) {
 				System.err.println("updated");
 				index().remove(node, field.getIndexName(), field.value());
 				index().add(node, field.getIndexName(), field.value());
 			} else {
 				System.err.println("denied (violation) ");
 
 				UniqueConstraintViolation v = new UniqueConstraintViolation();
 
 				v.setContext(field);
 				v.setNode(node);
 
 				throw v;
 			}
 
 		}
 
 		// System.out.println("indexsize " + indexsize);
 	}
 
 	private void saveAndIndex(Node node, FieldContext field) {
 		field.applyTo(node);
 		if (field.value() != null && field.isIndexed()) {
 			index().remove(node, field.getIndexName(), field.value());
 			index().add(node, field.getIndexName(), field.value());
 		} else if (field.value() != null && field.isFullText()) {
 			Index<Node> is = neo.getFullTextIndexService();
 			is.remove(node, field.getIndexName(), field.value());
 			is.add(node, field.getIndexName(), field.value());
 		}
 	}
 
 	private Index<Node> index() {
 		return neo.getIndexService();
 	}
 
 	private void relations(Node node, FieldContext field) {
 		Collection<?> values = field.values();
 		RelationshipType reltype = field.toRelationship(neo
 				.getRelationFactory());
 
 		// initialize null collections to a lazy loader
 		if (values == null) {
 			field.setProperty(ListFactory.get(field, new LoadOperation<T>(neo)));
 			return;
 		}
 
 		/*
 		 * Ignore unmodified collections.
 		 */
 		if (values instanceof Lazy) {
 			if (!((LazyList) values).modified())
 				return;
 			values = ((LazyList) values).newdata();
 		}
 
 		for (Object value : values) {
 			Node n2 = asNode(value);
 			if (!related(node, n2, reltype))
 				node.createRelationshipTo(n2, reltype);
 			save(n2, value);
 		}
 
 		// initialize collections to a lazy loader
 		if (!(values instanceof Lazy))
 			field.setProperty(ListFactory.get(field, new LoadOperation<T>(neo)));
 
 	}
 
 	private boolean related(Node a, Node b, RelationshipType type) {
 		for (Relationship rel : a.getRelationships(type, OUTGOING)) {
 			if (rel.getOtherNode(a).equals(b))
 				return true;
 		}
 		return false;
 	}
 
 	private Node asNode(Object value) {
 		return $(value).id(value).mirror(neo);
 	}
 
 	private void relate(Node node, FieldContext field) {
 		RelationshipType reltype = field.toRelationship(neo
 				.getRelationFactory());
 		deleteAll(node, reltype);
 		if (field.value() == null)
 			return;
 		Object value = field.value();
 		boolean isNew = !$(value).id(value).valid();
 		Node n2 = asNode(value);
 		node.createRelationshipTo(n2, reltype);
 		if (isNew)
 			save(n2, field.value());
 	}
 
 	private void deleteAll(Node node, RelationshipType reltype) {
 		for (Relationship r : node
 				.getRelationships(reltype, Direction.OUTGOING))
 			r.delete();
 	}
 
 }
 
 /**
  * jo4neo is a java object binding library for neo4j Copyright (C) 2009 Taylor
  * Cowan
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
