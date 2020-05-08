 package org.triple_brain.module.neo4j_graph_manipulator.graph;
 
 import com.hp.hpl.jena.vocabulary.RDFS;
 import org.neo4j.graphdb.Node;
 import org.triple_brain.module.common_utils.Uris;
 import org.triple_brain.module.model.FriendlyResource;
 
 import javax.inject.Inject;
 import java.net.URI;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class FriendlyResourceNeo4JUtils {
 
     @Inject
     Neo4JExternalResourceUtils externalResourceUtils;
 
     public FriendlyResource loadFromNode(Node node) {
         return FriendlyResource.withUriAndLabel(
                 Uris.get(node.getProperty(Neo4JUserGraph.URI_PROPERTY_NAME).toString()),
                 node.getProperty(RDFS.label.getURI()).toString()
         );
     }
 
     public FriendlyResource getFromUri(URI uri) {
         Node node = externalResourceUtils.getFromUri(uri);
         return FriendlyResource.withUriAndLabel(
                 Uris.get(node.getProperty(Neo4JUserGraph.URI_PROPERTY_NAME).toString()),
                 node.getProperty(RDFS.label.getURI()).toString()
         );
     }
 
     public Node getOrCreate(FriendlyResource friendlyResource) {
         if (externalResourceUtils.alreadyExists(friendlyResource.uri())) {
            return externalResourceUtils.getFromUri(
                     friendlyResource.uri()
             );
         } else {
             return addInGraph(friendlyResource);
         }
     }
 
     public Node addInGraph(FriendlyResource friendlyResource) {
         Node friendlyResourceAsNode = externalResourceUtils.create(
                 friendlyResource.uri()
         );
         friendlyResourceAsNode.setProperty(
                 RDFS.label.getURI(),
                 friendlyResource.label()
         );
         return friendlyResourceAsNode;
     }
 
 }
 
