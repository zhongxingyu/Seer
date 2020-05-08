 package org.triple_brain.module.search;
 
 import com.google.inject.Inject;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.common.SolrInputDocument;
 import org.apache.solr.core.CoreContainer;
 import org.triple_brain.module.model.FriendlyResource;
 import org.triple_brain.module.model.WholeGraph;
 import org.triple_brain.module.model.graph.Edge;
 import org.triple_brain.module.model.graph.GraphElement;
 import org.triple_brain.module.model.graph.Vertex;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import static org.triple_brain.module.common_utils.Uris.encodeURL;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 
 public class GraphIndexer {
 
     @Inject
     WholeGraph wholeGraph;
 
     private int INDEX_AFTER_HOW_NB_DOCUMENTS = 100;
     private CoreContainer coreContainer;
     private SearchUtils searchUtils;
 
     public static GraphIndexer withCoreContainer(CoreContainer coreContainer) {
         return new GraphIndexer(coreContainer);
     }
 
     private GraphIndexer(CoreContainer coreContainer) {
         this.coreContainer = coreContainer;
         this.searchUtils = SearchUtils.usingCoreCoreContainer(coreContainer);
     }
 
     public void indexWholeGraph(){
         indexAllVertices();
         indexAllEdges();
     }
 
     public void indexVertex(Vertex vertex) {
         try {
             SolrServer solrServer = searchUtils.getServer();
             solrServer.add(
                     vertexDocument(vertex)
             );
             solrServer.commit();
         } catch (IOException | SolrServerException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void indexRelation(Edge edge) {
         try {
             SolrServer solrServer = searchUtils.getServer();
             solrServer.add(
                     edgeDocument(edge)
             );
             solrServer.commit();
         } catch (IOException | SolrServerException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void deleteGraphElement(GraphElement graphElement) {
         try {
             SolrServer solrServer = searchUtils.getServer();
             solrServer.deleteByQuery(
                     "uri:" + encodeURL(
                             graphElement.uri()
                     )
             );
             solrServer.commit();
         } catch (IOException | SolrServerException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void close() {
         coreContainer.shutdown();
     }
 
     private SolrInputDocument edgeDocument(Edge edge){
         SolrInputDocument document = graphElementToDocument(edge);
         document.addField("is_vertex", false);
         document.addField(
                 "source_vertex_uri",
                 encodeURL(edge.sourceVertex().uri())
         );
         document.addField(
                 "destination_vertex_uri",
                 encodeURL(edge.destinationVertex().uri())
         );
         return document;
     }
 
     private SolrInputDocument vertexDocument(Vertex vertex){
         SolrInputDocument document = graphElementToDocument(vertex);
         document.addField("is_vertex", true);
         document.addField("is_public", vertex.isPublic());
         document.addField("comment", vertex.comment());
         for (Edge edge : vertex.connectedEdges()) {
             document.addField(
                     "relation_name",
                     edge.label()
             );
         }
         return document;
     }
 
     private void indexAllVertices(){
         Iterator<Vertex> vertexIt = wholeGraph.getAllVertices();
         Set<SolrInputDocument> vertexDocuments = new HashSet<>();
         int totalIndexed = 0;
         int nbInCycle = 0;
         while(vertexIt.hasNext()){
             vertexDocuments.add(
                     vertexDocument(vertexIt.next())
             );
             nbInCycle++;
             if(nbInCycle == INDEX_AFTER_HOW_NB_DOCUMENTS){
                 totalIndexed += INDEX_AFTER_HOW_NB_DOCUMENTS;
                addDocumentsAndCommit(vertexDocuments);
                 System.out.println(
                        "Indexing vertices ... total indexed yet " + totalIndexed
                 );
                 nbInCycle = 0;
                 vertexDocuments = new HashSet<>();
             }
         }
         addDocumentsAndCommit(vertexDocuments);
        totalIndexed += nbInCycle;
        System.out.println(
                "Indexing vertices ... total indexed " + totalIndexed
        );
     }
 
     private void indexAllEdges(){
         Iterator<Edge> edgeIt = wholeGraph.getAllEdges();
         Set<SolrInputDocument> edgesDocument = new HashSet<>();
         int totalIndexed = 0;
         int nbInCycle = 0;
         while(edgeIt.hasNext()){
             edgesDocument.add(
                 edgeDocument(edgeIt.next())
             );
             nbInCycle++;
             if(nbInCycle == INDEX_AFTER_HOW_NB_DOCUMENTS){
                 totalIndexed += INDEX_AFTER_HOW_NB_DOCUMENTS;
                addDocumentsAndCommit(edgesDocument);
                 System.out.println(
                        "Indexing edges ... total indexed yet " + totalIndexed
                 );
                 nbInCycle = 0;
                 edgesDocument = new HashSet<>();
             }
         }
         addDocumentsAndCommit(edgesDocument);
        totalIndexed += nbInCycle;
        System.out.println(
                "Indexing edges ... total indexed " + totalIndexed
        );
     }
 
     private void addDocumentsAndCommit(Collection<SolrInputDocument> collection){
         try{
             SolrServer solrServer = searchUtils.getServer();
             solrServer.add(collection);
             solrServer.commit();
         }catch(SolrServerException | IOException e){
             throw new RuntimeException(e);
         }
     }
     private SolrInputDocument graphElementToDocument(GraphElement graphElement) {
         SolrInputDocument document = new SolrInputDocument();
         document.addField("uri", encodeURL(graphElement.uri()));
         document.addField("label", graphElement.label());
         document.addField("label_lower_case", graphElement.label().toLowerCase());
         document.addField("owner_username", graphElement.ownerUsername());
         for(FriendlyResource identification : graphElement.getIdentifications()){
             document.addField(
                     "identification",
                     encodeURL(identification.uri())
             );
         }
         return document;
     }
 }
