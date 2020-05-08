 package com.tinkerpop.goo;
 
 import com.tinkerpop.blueprints.pgm.*;
 import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;
 import com.tinkerpop.goo.util.IndexMetadata;
 import jdbm.PrimaryTreeMap;
 import jdbm.RecordManager;
 import jdbm.RecordManagerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * @author Marko A. Rodriguez (http://markorodriguez.com)
  */
 public class GooGraph implements TransactionalGraph, IndexableGraph {
 
     private final String directory;
     protected final RecordManager manager;
     private final PrimaryTreeMap<String, Object> metadata;
     private final PrimaryTreeMap<Long, Vertex> vertices;
    private PrimaryTreeMap<Long, Edge> edges;
     private Map<String, Index<? extends Element>> indices;
     private Map<String, AutomaticIndex<? extends Element>> autoIndices;
     private Mode mode = Mode.AUTOMATIC;
     protected boolean inTransaction;
 
     public GooGraph(final String directory) {
         try {
             boolean freshGraph;
             this.directory = directory;
             final File file = new File(this.directory);
             if (!file.exists()) {
                 if (!file.mkdir()) {
                     throw new RuntimeException("Unable to create or open Goo graph directory");
                 }
                 freshGraph = true;
             } else {
                 freshGraph = false;
             }
 
             this.manager = RecordManagerFactory.createRecordManager(this.directory + GooTokens.SLASH_GOO);
 
             // load the persistent tree maps
             this.metadata = this.manager.treeMap(GooTokens.METADATA);
             this.vertices = this.manager.treeMap(GooTokens.VERTICES, new GooElementSerializer<Vertex>(this));
             this.edges = this.manager.treeMap(GooTokens.EDGES, new GooElementSerializer<Edge>(this));
             this.loadIndices();
             if (freshGraph) {
                 this.createAutomaticIndex(Index.VERTICES, GooVertex.class, null);
                 this.createAutomaticIndex(Index.EDGES, GooEdge.class, null);
             }
         } catch (IOException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 
     protected Iterable<AutomaticIndex<? extends Element>> getAutoIndices() {
         return this.autoIndices.values();
     }
 
     protected Iterable<Index> getManualIndices() {
         HashSet<Index> indices = new HashSet<Index>(this.indices.values());
         indices.removeAll(this.autoIndices.values());
         return indices;
     }
 
     private void loadIndices() {
         this.indices = new HashMap<String, Index<? extends Element>>();
         this.autoIndices = new HashMap<String, AutomaticIndex<? extends Element>>();
 
         final Object metas = this.metadata.get(GooTokens.INDEX_METADATA);
         if (null != metas) {
             for (IndexMetadata meta : (List<IndexMetadata>) metas) {
                 final String indexName = meta.getIndexName();
                 if (meta.getIndexType() == Index.Type.MANUAL) {
                     this.indices.put(indexName, new GooIndex(indexName, meta.getIndexClass(), this.manager.treeMap(GooTokens.INDEX_PREFIX + indexName), this));
                 } else {
                     final AutomaticIndex<? extends Element> tempIndex = new GooAutomaticIndex(indexName, meta.getIndexClass(), meta.getAutoKeys(), this.manager.treeMap(GooTokens.INDEX_PREFIX + indexName), this);
                     this.indices.put(indexName, tempIndex);
                     this.autoIndices.put(indexName, tempIndex);
                 }
             }
         } else {
             this.metadata.put(GooTokens.INDEX_METADATA, new ArrayList<IndexMetadata>());
         }
     }
 
     protected void updateElementStores(final GooElement element) {
         if (element instanceof Vertex)
             this.vertices.put((Long) element.getId(), (Vertex) element);
         else
             this.edges.put((Long) element.getId(), (Edge) element);
     }
 
     public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
         final Index<T> index = (Index<T>) this.indices.get(indexName);
         if (null == index)
             throw new RuntimeException("No such index exists: " + indexName);
         if (!indexClass.isAssignableFrom(index.getIndexClass()))
             throw new RuntimeException(indexClass + " is not assignable from " + index.getIndexClass());
         else
             return index;
     }
 
     public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> keys) {
         if (this.indices.containsKey(indexName))
             throw new RuntimeException("Index already exists: " + indexName);
 
         final AutomaticIndex<T> index = new GooAutomaticIndex(indexName, indexClass, keys, this.manager.treeMap(GooTokens.INDEX_PREFIX + indexName), this);
         final List<IndexMetadata> indexMetadata = (List<IndexMetadata>) this.metadata.get(GooTokens.INDEX_METADATA);
         indexMetadata.add(new IndexMetadata(indexName, indexClass, Index.Type.AUTOMATIC, keys));
         this.indices.put(indexName, index);
         this.autoIndices.put(indexName, index);
         this.metadata.put(GooTokens.INDEX_METADATA, indexMetadata);
         this.autoStopTransaction(Conclusion.SUCCESS);
         return index;
     }
 
     public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
         if (this.indices.containsKey(indexName))
             throw new RuntimeException("Index already exists: " + indexName);
 
         final Index<T> index = new GooIndex(indexName, indexClass, this.manager.treeMap(GooTokens.INDEX_PREFIX + indexName), this);
         final List<IndexMetadata> indexMetadata = (List<IndexMetadata>) this.metadata.get(GooTokens.INDEX_METADATA);
         indexMetadata.add(new IndexMetadata(indexName, indexClass, Index.Type.MANUAL, null));
         this.indices.put(indexName, index);
         this.metadata.put(GooTokens.INDEX_METADATA, indexMetadata);
         this.autoStopTransaction(Conclusion.SUCCESS);
         return index;
     }
 
     public Iterable<Index<? extends Element>> getIndices() {
         final List<Index<? extends Element>> temp = new ArrayList<Index<? extends Element>>();
         temp.addAll(this.indices.values());
         return temp;
     }
 
     public void dropIndex(final String indexName) {
         this.indices.remove(indexName);
         this.autoIndices.remove(indexName);
         final List<IndexMetadata> indexMetadata = (List<IndexMetadata>) this.metadata.get(GooTokens.INDEX_METADATA);
         final List<IndexMetadata> newIndexMetadata = new ArrayList<IndexMetadata>();
         for (IndexMetadata metadata : indexMetadata) {
             if (!metadata.getIndexName().equals(indexName)) {
                 newIndexMetadata.add(metadata);
             }
         }
         this.metadata.put(GooTokens.INDEX_METADATA, newIndexMetadata);
         this.autoStopTransaction(Conclusion.SUCCESS);
     }
 
     public Iterable<Edge> getEdges() {
        /* try {
            this.edges.values().iterator();
        } catch (Exception e) {
            this.edges = this.manager.treeMap(GooTokens.EDGES, new GooElementSerializer<Edge>(this));
        }
        */
         return this.edges.values();
     }
 
     public Iterable<Vertex> getVertices() {
         return this.vertices.values();
     }
 
     public Vertex getVertex(final Object id) {
         if (null == id)
             return null;
         final Long longId;
         if (id instanceof Long)
             longId = (Long) id;
         else
             longId = Double.valueOf(id.toString()).longValue();
         return this.vertices.get(longId);
     }
 
     public Edge getEdge(final Object id) {
         if (null == id)
             return null;
 
         final Long longId;
         if (id instanceof Long)
             longId = (Long) id;
         else
             longId = Double.valueOf(id.toString()).longValue();
 
         return this.edges.get(longId);
     }
 
     public Vertex addVertex(final Object id) {
         // provided ids are ignored
         final Long longId = this.vertices.newLongKey();
         final Vertex vertex = new GooVertex(this, longId);
         this.vertices.put(longId, vertex);
         this.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
         return vertex;
     }
 
     public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
         // provided ids are ignored
         final Long longId = this.edges.newLongKey();
         final Edge edge = new GooEdge(this, longId, outVertex, inVertex, label);
         ((GooVertex) outVertex).addOutEdge(edge);
         ((GooVertex) inVertex).addInEdge(edge);
         this.edges.put(longId, edge);
         this.vertices.put((Long) outVertex.getId(), outVertex);
         this.vertices.put((Long) inVertex.getId(), inVertex);
         this.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
         return edge;
     }
 
     public void removeVertex(final Vertex vertex) {
         for (final Edge edge : vertex.getInEdges()) {
             this.removeEdge(edge);
         }
         for (final Edge edge : vertex.getOutEdges()) {
             this.removeEdge(edge);
         }
         this.vertices.remove((Long) vertex.getId());
 
         AutomaticIndexHelper.removeElement(this, vertex);
         // todo make this an IndexHelper method
         for (final Index index : this.getManualIndices()) {
             if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
                 GooIndex<Vertex> idx = (GooIndex<Vertex>) index;
                 idx.removeElement(vertex);
             }
         }
 
         this.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
     }
 
     public void removeEdge(final Edge edge) {
         final GooVertex outVertex = (GooVertex) edge.getOutVertex();
         final GooVertex inVertex = (GooVertex) edge.getInVertex();
         outVertex.removeOutEdge(edge);
         inVertex.removeInEdge(edge);
 
         AutomaticIndexHelper.removeElement(this, edge);
         // todo make this an IndexHelper method
         for (final Index index : this.getManualIndices()) {
             if (Edge.class.isAssignableFrom(index.getIndexClass())) {
                 GooIndex<Edge> idx = (GooIndex<Edge>) index;
                 idx.removeElement(edge);
             }
         }
 
         this.edges.remove((Long) edge.getId());
         this.vertices.put((Long) outVertex.getId(), outVertex);
         this.vertices.put((Long) inVertex.getId(), inVertex);
 
         this.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
     }
 
     public void clear() {
         this.vertices.clear();
         this.edges.clear();
         this.indices.clear();
         this.autoIndices.clear();
         this.metadata.put(GooTokens.INDEX_METADATA, new ArrayList<IndexMetadata>());
         this.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
     }
 
     public void shutdown() {
         try {
             this.manager.commit();
             this.manager.close();
         } catch (IOException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 
     public void startTransaction() {
         if (Mode.AUTOMATIC == this.mode)
             throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
         if (this.inTransaction)
             throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
         this.inTransaction = true;
     }
 
     public void stopTransaction(final Conclusion conclusion) {
         if (Mode.AUTOMATIC == this.mode)
             throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
 
         try {
             this.inTransaction = false;
             if (Conclusion.SUCCESS == conclusion) {
                 this.manager.commit();
             } else {
                 this.manager.rollback();
                 this.manager.clearCache();
             }
         } catch (IOException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 
     protected void autoStopTransaction(Conclusion conclusion) {
         if (this.mode == Mode.AUTOMATIC) {
             try {
                 this.inTransaction = false;
                 if (conclusion == Conclusion.SUCCESS)
                     this.manager.commit();
                 else {
                     this.manager.rollback();
                     this.manager.clearCache();
                 }
             } catch (IOException e) {
                 throw new RuntimeException(e.getMessage(), e);
             }
         }
     }
 
     public void setTransactionMode(Mode mode) {
         try {
             this.manager.commit();
         } catch (IOException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
         this.inTransaction = false;
         this.mode = mode;
     }
 
     public Mode getTransactionMode() {
         return this.mode;
     }
 
     public String toString() {
         return "goograph[" + this.directory + "]";
     }
 }
