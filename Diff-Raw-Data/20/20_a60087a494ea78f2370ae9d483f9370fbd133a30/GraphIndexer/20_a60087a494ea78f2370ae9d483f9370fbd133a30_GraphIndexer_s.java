 package org.triple_brain.module.search;
 
 import com.google.inject.Inject;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.common.SolrInputDocument;
 import org.apache.solr.core.CoreContainer;
 import org.triple_brain.module.model.FriendlyResource;
import org.triple_brain.module.model.User;
 import org.triple_brain.module.model.WholeGraph;
 import org.triple_brain.module.model.graph.Edge;
 import org.triple_brain.module.model.graph.GraphElement;
import org.triple_brain.module.model.graph.SubGraph;
 import org.triple_brain.module.model.graph.Vertex;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import static org.triple_brain.module.common_utils.Uris.encodeURL;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 
 public class GraphIndexer {
 
     @Inject
     WholeGraph wholeGraph;
 
     private CoreContainer coreContainer;
     private SearchUtils searchUtils;
 
     public static GraphIndexer withCoreContainer(CoreContainer coreContainer) {
         return new GraphIndexer(coreContainer);
     }
 
     private GraphIndexer(CoreContainer coreContainer) {
         this.coreContainer = coreContainer;
         this.searchUtils = SearchUtils.usingCoreCoreContainer(coreContainer);
     }
 
     private String configFilePath() {
         return coreContainer.getConfigFile().getParent() + "/" +
                 coreContainer.getCore(coreContainer.getDefaultCoreName()).getCoreDescriptor().getConfigName();
     }
 
     private String schemaFilePath() {
         return coreContainer.getConfigFile().getParent() + "/" +
                 coreContainer.getCore(coreContainer.getDefaultCoreName()).getCoreDescriptor().getSchemaName();
     }
 
     public void indexWholeGraph(){
        indexAllVertices();
         indexAllEdges();
     }
 
     public void indexVertex(Vertex vertex) {
         try {
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
             SolrServer solrServer = searchUtils.getServer();
             solrServer.add(document);
             solrServer.commit();
         } catch (IOException | SolrServerException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void indexRelation(Edge edge) {
         try {
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
             SolrServer solrServer = searchUtils.getServer();
             solrServer.add(document);
             solrServer.commit();
         } catch (IOException | SolrServerException e) {
             throw new RuntimeException(e);
         }
     }
 
    public void deleteGraphElementOfUser(GraphElement graphElement, User user) {
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
 
    public void indexGraph(SubGraph subGraph){

    }

     private void indexAllVertices(){
         Iterator<Vertex> vertexIt = wholeGraph.getAllVertices();
         while(vertexIt.hasNext()){
             indexVertex(
                     vertexIt.next()
             );
         }
     }
 
     private void indexAllEdges(){
         Iterator<Edge> edgeIt = wholeGraph.getAllEdges();
         while(edgeIt.hasNext()){
             indexRelation(
                     edgeIt.next()
             );
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
