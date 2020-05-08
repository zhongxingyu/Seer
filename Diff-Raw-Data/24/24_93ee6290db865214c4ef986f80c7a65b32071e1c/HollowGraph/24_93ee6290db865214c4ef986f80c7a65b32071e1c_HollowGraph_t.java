 package com.tinkerpop.blueprints.pgm.impls.hollow;
 
 import com.tinkerpop.blueprints.pgm.*;
 import com.tinkerpop.blueprints.pgm.impls.hollow.util.*;
 
 /**
  * A Blueprints implementation of an SQL database (http://www.oracle.com)
  *
  * @author Daniel Margo (http://www.eecs.harvard.edu/~dmargo)
  */
 public class HollowGraph implements Graph {
 
     /**
      * Creates a new instance of a HollowGraph.
      */
     public HollowGraph() {}
 
     // BLUEPRINTS GRAPH INTERFACE
     public Vertex addVertex(final Object id) {        
         try {
             //autoStartTransaction();
             final Vertex vertex = new HollowVertex(this);
             //autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
             return vertex;
         } catch (RuntimeException e) {
             //autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
             throw e;
         } catch (Exception e) {
             //autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
             throw new RuntimeException(e.getMessage(), e);
         }     
     }
 
    public Vertex getVertex(Object id) {
     	if (id == null)
			id = new Long(0);
     	
     	try {
     		return new HollowVertex(this, id);
     	} catch (Exception e) {
			//System.err.println("Warning: " + e);
     		return null;
     	}
     }
 
     public Iterable<Vertex> getVertices() {
         return new HollowVertexSequence(this);
     }
 
     public void removeVertex(final Vertex vertex) {
         if (vertex == null || vertex.getId() == null)
             return;
         
         try {
             //autoStartTransaction();
             ((HollowVertex) vertex).remove();
             //autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
         } catch (RuntimeException e) {
             //autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
             throw e;
         } catch (Exception e) {
             //autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
             throw new RuntimeException(e.getMessage(), e);
         }
     }
     
     public Edge addEdge(
 		final Object id,
 		final Vertex outVertex,
 		final Vertex inVertex,
 		final String label)
     {    	
         try {
             //autoStartTransaction();
             final Edge edge = new HollowEdge(
         		this,
         		(HollowVertex) outVertex,
         		(HollowVertex) inVertex,
         		label);
             //autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
             return edge;
         } catch (RuntimeException e) {
             //autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
             throw e;
         } catch (Exception e) {
             //autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 
    public Edge getEdge(Object id) {
     	if (id == null)
			id = new Long(0);
     	
     	try {
     		return new HollowEdge(this, id);
     	} catch (Exception e) {
     		return null;
     	}
     }
 
     public Iterable<Edge> getEdges() {
         return new HollowEdgeSequence(this);
     }
 
     public void removeEdge(final Edge edge) {
         if (edge == null || edge.getId() == null)
             return;
         
         try {
             //autoStartTransaction();
             ((HollowEdge) edge).remove();
             //autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
         } catch (RuntimeException e) {
             //autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
             throw e;
         } catch (Exception e) {
             //autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 
     public void clear() {}
     
     public void delete() {}
 
     public void shutdown() {}
 
     public String toString() {
     	return "Hollowgraph";
     }
 
     /* TRANSACTIONAL GRAPH INTERFACE
 
     public void setTransactionMode(final Mode mode) {
     	if (txn != null) {
     		try {
     			txn.commit();
     		} catch (RuntimeException e) {
                 throw e;
             } catch (Exception e) {
                 throw new RuntimeException(e.getMessage(), e);
             } finally {
             	txn = null;
             }
     	}
     	txnMode = mode;
     }
 
     public Mode getTransactionMode() {
         return txnMode;
     }
 
     public void startTransaction() {
         if (txnMode == Mode.AUTOMATIC)
             throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
         if (txn == null) {
         	try {
             	txn = dbEnv.beginTransaction(null, null);
         	} catch (RuntimeException e) {
                 throw e;
             } catch (Exception e) {
                 throw new RuntimeException(e.getMessage(), e);
             }
         } else
             throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
     }
 
     protected void autoStartTransaction() {
         if (txnMode == Mode.AUTOMATIC) {
             if (txn == null) {
             	try {
                 	txn = dbEnv.beginTransaction(null, null);
             	} catch (RuntimeException e) {
                     throw e;
                 } catch (Exception e) {
                     throw new RuntimeException(e.getMessage(), e);
                 }
             } else
                 throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
         }
     }
 
     public void stopTransaction(final Conclusion conclusion) {
         if (txnMode == Mode.AUTOMATIC)
             throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
         if (txn == null)
         	throw new RuntimeException("BdbGraph: There is no active transaction to stop.");
         try {
         	if (conclusion == TransactionalGraph.Conclusion.SUCCESS)
         		txn.commit();
         	else
         		txn.abort();
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new RuntimeException(e.getMessage(), e);
         } finally {
         	txn = null;
         }
     }
 
     protected void autoStopTransaction(final Conclusion conclusion) {
         if (txnMode == Mode.AUTOMATIC) {
             if (txn == null)
             	throw new RuntimeException("BdbGraph: There is no active transaction to stop.");
             try {
             	if (conclusion == TransactionalGraph.Conclusion.SUCCESS)
             		txn.commit();
             	else
             		txn.abort();
             } catch (RuntimeException e) {
                 throw e;
             } catch (Exception e) {
                 throw new RuntimeException(e.getMessage(), e);
             } finally {
             	txn = null;
             }
         }
     }
     
     */
 
     /* INDEXABLE GRAPH INTERFACE
 
     public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
         if (this.manualIndices.containsKey(indexName))
             throw new RuntimeException("Index already exists: " + indexName);
 
         final BdbIndex index = new BdbIndex(this, indexName, indexClass, Index.Type.MANUAL); // Your constructor may vary.
         this.manualIndices.put(index.getIndexName(), index);
 
         return index;
     }
 
     public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> indexKeys) {
         if (this.autoIndices.containsKey(indexName))
             throw new RuntimeException("Index already exists: " + indexName);
 
         final BdbAutomaticIndex index = new BdbAutomaticIndex<BdbElement>(this, indexName, (Class<BdbElement>) indexClass, indexKeys); // Your constructor may vary.
         this.autoIndices.put(index.getIndexName(), index);
         // You may also need to put into manualIndices depending on implementation.
 
         return index;
     }
 
     public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
         Index<?> index = this.manualIndices.get(indexName);
         if (null == index) {
             index = this.autoIndices.get(indexName);
             if (null == index)
                 throw new RuntimeException("No such index exists: " + indexName);
         }
 
         if (indexClass.isAssignableFrom(index.getIndexClass()))
             return (Index<T>) index;
         else
             throw new RuntimeException("Can not convert " + index.getIndexClass() + " to " + indexClass);
     }
 
     public Iterable<Index<? extends Element>> getIndices() {
         final List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
         for (Index<?> index : this.manualIndices.values()) {
             list.add(index);
         }
         for (Index<?> index : this.autoIndices.values()) {
             list.add(index);
         }
         return list;
     }
 
     public void dropIndex(final String iIndexName) {
         if (this.manualIndices.remove(iIndexName) == null)
             this.autoIndices.remove(iIndexName);
 
         // And of course, drop the actual index from the raw graph.
     }
 
     */
     
 }
