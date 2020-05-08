 package models;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.neo4j.graphdb.*;
 import org.neo4j.graphdb.traversal.BranchState;
 import org.neo4j.graphdb.traversal.Evaluation;
 import org.neo4j.graphdb.traversal.Evaluators;
 import org.neo4j.graphdb.traversal.PathEvaluator;
 import org.neo4j.kernel.Traversal;
 
 import data.CategoryData;
 import data.CategoryData.Structure;
 import data.Database;
 import data.Database.Rels;
 import data.NoteData;
 
 import play.Logger;
 import play.api.templates.Html;
 
 
 public class Category implements Comparable<Category>{
 
 
 	public static Category catRoot;
 	
 	public boolean canCreateChild = true;
     
     public boolean canDelete = true;
     
 	// memeory copy data
 	public ArrayList<Category> childs = new ArrayList<Category>();
     
     public CategoryData data;
 
 	public static void startDatabase(Node refNode, int version) {
     	if (refNode.getSingleRelationship(Rels.SUPER_CAT_OF, Direction.OUTGOING) == null) {
     		CategoryData c = new CategoryData(false);
     		c.showStructure = false;
     		c.showIndex = true;
     		createChildInner(refNode.getId(), c);
     	}
     	catRoot = rebuildCategoryTree(refNode.getSingleRelationship(Rels.SUPER_CAT_OF, Direction.OUTGOING)
     			.getEndNode().getId());
 	}
 
 	Category(CategoryData d) {
 		data = d;
 		canDelete = canDelete && !Database.get(data.id).hasRelationship(Rels.SUPER_CAT_OF, Direction.OUTGOING);
 
     	if (data.structure.equals(Structure.MATH_GRAPH)) {
     		canCreateChild = false;
     	}
 	}
 
 	private static Category rebuildCategoryTree(long id) {
 		ArrayList<Category> bfa = new ArrayList<Category>();
 		for (Path p : Traversal.description().depthFirst()
 				.relationships(Rels.SUPER_CAT_OF, Direction.OUTGOING)
 				.evaluator(Evaluators.all())
 				.traverse(Database.get(id))) {
 			CategoryData d = new CategoryData(p.endNode());
 			Category cat = null;
 			// structure -> object router
 			if (d.structure.equals(Structure.LIST)) {
 				cat = new Category(d);
 			} else if (d.structure.equals(Structure.MATH_GRAPH)) {
 				cat = new MathGraph(d);
 			}
 			if (bfa.size() < p.length()+1) {
 				bfa.add(cat);
 			} else {
 				bfa.set(p.length(), cat);
 			}
 			if (p.length() > 0) {
 				bfa.get(p.length()-1).childs.add(cat);
 			}
 		}
 		bfa.get(0).sortChilds();
 		catRoot = bfa.get(0);
 		catRoot.canDelete = false;
 		return catRoot;
 	}
 	
 	private void sortChilds() {
 		for (Category cat : childs) {
 			cat.sortChilds();
 		}
 		Collections.sort(childs);
 	}
 	@Override
 	public int compareTo(Category o) {
 		return data.compareTo(o.data);
 	}
 	
     private static long createChildInner(long pid, CategoryData c) {
 	    Transaction tx = Database.graphDb.beginTx();
 		Node node = Database.get(pid);
 		Node newNode = null;
 	    try {
 	    	newNode = Database.graphDb.createNode();
 	    	node.createRelationshipTo(newNode, Rels.SUPER_CAT_OF);
 	    	c.write(newNode);
 			Node node2 = Database.graphDb.createNode();
 			newNode.createRelationshipTo(node2, Rels.FRONTPAGE_IS);
 			new NoteData(false).write(node2);
 		    tx.success();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return -1;
 	    } finally {
 		    tx.finish();
 	    }
 	    return newNode.getId();
     }
     
     public boolean createChild(CategoryData c) {
     	// cannot create a show nothing child
     	if (!c.showDetail && !c.showStructure) {
     		return false;
     	}
     	// if current page cannot have child
     	if (!canCreateChild) {
     		return false;
     	}
     	// if current page is just a index page, or the child is same type with this, ok
     	if (!(data.showIndex && data.showDetail && !data.showStructure) && !data.sameType(c)) {
     		return false;
     	}
     	boolean succ = -1 != createChildInner(data.id, c);
 	    catRoot = rebuildCategoryTree(catRoot.data.id);
 	    return succ;
     }
 
 	public static Category get(long id) {
 		return catRoot.get2(id);
 	}
 	private Category get2(long id) {
 		if (data.id == id)
 			return this;
 		for (Category c : childs) {
 			Category what = c.get2(id);
 			if (what != null) {
 				return what;
 			}
 		}
 		return null;
 	}
 
 	public boolean remove() {
 		if (!canDelete) {
 			return false;
 		}
 		boolean succ = false;
 		Transaction tx = Database.graphDb.beginTx();
 		Node node = Database.get(data.id);
 		try {
 			for (Relationship r1 : node.getRelationships(Direction.OUTGOING, Rels.INCLUDE, Rels.FRONTPAGE_IS)) {
 				Node n1 = r1.getEndNode();
 				for (Relationship r2 : n1.getRelationships()) {
 					r2.delete();
 				}
 				n1.delete();
 			}
 		    for (Relationship r : node.getRelationships(Direction.INCOMING)) {
 		    	r.delete();
 		    }
 		    node.delete();
 		    tx.success();
 		    succ = true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			tx.finish();
 		}
 	    catRoot = rebuildCategoryTree(catRoot.data.id);
 	    return succ;
 	}
 
 	public Category changeData(CategoryData d) {
 		d.id = data.id;
 		data = d;
 		return this;
 	}
 	
 	public boolean edit() {
 		boolean succ = false;
 		Transaction tx = Database.graphDb.beginTx();
 		Node node = Database.get(data.id);
 		try {
 	    	data.write(node);
 			tx.success();
 		    succ = true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			tx.finish();
 		}
 	    catRoot = rebuildCategoryTree(catRoot.data.id);
 	    return succ;
 	}
 
 
 	public Note getNote(long id) {
 		NoteData d = NoteData.get(id);
		if (d != null && d.cid == data.id)
 			return new Note(d);
 		return null;
 	}
 
     public Html renderStructure(NoteData n) {
     	return renderList();
     }
     
     public Html renderList() {
     	return views.html.structure_list.render(getList(), this);
 	}
 
 	public Note createNote(NoteData n) {
     	n.cid = data.id;
     	return new Note(n);
     }
     
 	public Note mainNote() {
 		if (data.showIndex) {
 			// delete this code please!
 			if (!Database.get(data.id).hasRelationship(Rels.FRONTPAGE_IS)){
 				Transaction tx = Database.graphDb.beginTx();
 				try {
 					Node node2 = Database.graphDb.createNode();
 					Database.get(data.id).createRelationshipTo(node2, Rels.FRONTPAGE_IS);
 					new NoteData(false).write(node2);
 				} catch (Exception e) {
 					e.printStackTrace();
 				} finally {
 					tx.finish();
 				}
 			}
 			return getNote(Database.get(data.id)
 					.getSingleRelationship(Rels.FRONTPAGE_IS, Direction.OUTGOING)
 					.getEndNode().getId());
 		} else {
 			ArrayList<NoteData> notes = getList();
 			if (notes.size() > 0) {
 				return new Note(notes.get(0));
 			} else {
 				return Note.invalid;
 			}
 		}
 	}
 
     @JsonIgnore
 	public ArrayList<NoteData> getList() {
 		ArrayList<NoteData> notes = new ArrayList<NoteData>();
 		for (Path p : Traversal.description().depthFirst()
 				.relationships(Rels.SUPER_CAT_OF, Direction.OUTGOING)
 				.relationships(Rels.INCLUDE, Direction.OUTGOING)
 				.evaluator(new PathEvaluator<Object>() {
 					@Override
 					public Evaluation evaluate(Path p) {
 						if (p.lastRelationship() == null || p.lastRelationship().isType(Rels.SUPER_CAT_OF))
 							return Evaluation.EXCLUDE_AND_CONTINUE;
 						return Evaluation.INCLUDE_AND_CONTINUE;
 					}
 					@Override
 					public Evaluation evaluate(Path p, BranchState<Object> arg1) {
 						if (p.lastRelationship() == null || p.lastRelationship().isType(Rels.SUPER_CAT_OF))
 							return Evaluation.EXCLUDE_AND_CONTINUE;
 						return Evaluation.INCLUDE_AND_CONTINUE;
 					}
 					
 				})
 				.traverse(Database.get(data.id))) {
 			notes.add(new NoteData(p.endNode()));
 		}
 		if (data.itemOrder.equals("random")) {
 			Collections.shuffle(notes);
 		} else {
 			Collections.sort(notes, NoteData.getComparator(data.itemOrder));
 		}
 		return notes;
 	}
 }
