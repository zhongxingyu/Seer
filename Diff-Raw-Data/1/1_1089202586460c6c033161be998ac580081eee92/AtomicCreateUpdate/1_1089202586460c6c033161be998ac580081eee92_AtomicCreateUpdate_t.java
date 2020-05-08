 package com.okcupidlabs.neo4j.server.plugins;
 
 import java.util.Map;
 import java.net.URI;
 
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.MediaType;
 
 import org.neo4j.graphdb.*;
 import org.neo4j.graphdb.index.UniqueFactory;
 import org.neo4j.kernel.GraphDatabaseAPI;
 import org.neo4j.kernel.impl.transaction.xaframework.ForceMode;
 import org.neo4j.server.rest.domain.PropertySettingStrategy;
 import org.neo4j.server.rest.repr.*;
 import org.neo4j.server.rest.web.DatabaseActions;
 import org.neo4j.server.rest.web.PropertyValueException;
 
 
 /**
  * Class containing JAX-RS endpoints for atomically 'upserting' and 'upconnecting' nodes and edges.
  */
 @Path("/")
 public class AtomicCreateUpdate {
 
     private static final String[] REQUIRED_UPSERT_PARAMETERS = {"index_name", "index_key", "index_value","properties"};
     private static final String[] REQUIRED_UPCONNECT_PARAMETERS = {"from", "to", "relationship_type", "properties"};
 
 
     private final UriInfo uriInfo;
     private final InputFormat input;
     private final OutputFormat output;
     private final DatabaseActions actions;
     private final GraphDatabaseService service;
     private final PropertySettingStrategy propertySetter;
 
     public AtomicCreateUpdate(@Context UriInfo uriInfo, @Context InputFormat input,
                               @Context OutputFormat output, @Context DatabaseActions actions,
                               @Context GraphDatabaseService service)
     {
         this.uriInfo = uriInfo;
         this.input = input;
         this.output = output;
         this.actions = actions;
         this.service = service;
         // NOTE: This is ugly as hell.  I don't want to depend on this cast but I do want
         // the PropertySettingStrategy instead of re-implementing that functionality.
         // WHATCHAGONNADO.
         this.propertySetter = new PropertySettingStrategy((GraphDatabaseAPI)service);
 
     }
 
     /**
      * Inserts or updates a new node by first determining whether that node exists by using a lookup index.
      *
      * @param force Force mode for transaction, normally used internally.
      * @param body JSON encoded parameters.
      *             Required:
      *             - index_name: Name of index to use for lookup
      *             - index_key: Index key to utilize for lookup
      *             - index_value: Index value to utilize for lookup.  Should be unique per index/key.
      *             - properties: Map of node properties to insert/merge
      *
      * @return JSON representation of node. (See: http://docs.neo4j.org/chunked/milestone/rest-api-node-properties.html)
      */
     @POST
     @Path("/upsert")
     public Response upsertNode(
                 final @HeaderParam("Transaction") ForceMode force,
                 final String body)
     {
         final Map<String, Object> properties;
         try {
             properties = input.readMap(body);
         } catch (BadInputException e) {
             return output.badRequest(e);
         }
 
         if(!ensureRequiredParameters(properties, REQUIRED_UPSERT_PARAMETERS)) {
             return missingParameters(properties, REQUIRED_UPSERT_PARAMETERS);
         }
 
         final String indexName = (String)properties.get("index_name");
         final String indexKey = (String)properties.get("index_key");
         final String indexValue = (String)properties.get("index_value");
         final Map<String, Object> nodeProperties = (Map<String, Object>)properties.get("properties");
 
         if (!this.service.index().existsForNodes(indexName)) {
             return output.badRequest(
                     new IllegalArgumentException("Index with index_name: " + indexName + " does not exist."));
         }
 
         UniqueFactory<Node> nodeFactory = new UniqueFactory.UniqueNodeFactory(service, indexName)
         {
             @Override
             protected void initialize( Node created, Map<String, Object> properties )
             {
                 //noop
             }
         };
 
         final Node upsertedNode = nodeFactory.getOrCreate(indexKey, indexValue);
         try {
             this.propertySetter.setProperties(upsertedNode, nodeProperties);
         } catch (BadInputException e) {
 
             return output.badRequest(e);
         } catch (ArrayStoreException e) {
 
             return badJsonFormat(body);
         }
 
         return output.ok(new NodeRepresentation(upsertedNode));
     }
 
     /***
      * Connects two nodes if an edge of the given type does not already exist between them, otherwise updates the
      * edge properties.
      * @param force Force mode for transaction, normally used internally.
      * @param body JSON encoded parameters.
      *             Required:
      *             - from: Name of index to use for lookup
      *             - to: Index key to utilize for lookup
      *             - relationship_type: Index value to utilize for lookup.  Should be unique per index/key.
      *             - properties: Map of node properties to insert/merge
      *
      * @return JSON representation of edge.
      */
     @POST
     @Path("/upconnect")
     public Response upconnectNodes(
             final @HeaderParam("Transaction") ForceMode force,
             final String body)
     {
         final Map<String, Object> properties;
         try {
             properties = input.readMap(body);
         } catch (BadInputException e) {
             return output.badRequest(e);
         }
 
         if(!ensureRequiredParameters(properties, REQUIRED_UPCONNECT_PARAMETERS)) {
             return missingParameters(properties, REQUIRED_UPCONNECT_PARAMETERS);
         }
 
         long fromId = parseNodeIdFromURI(URI.create((String) properties.get("from")));
         long toId = parseNodeIdFromURI(URI.create((String)properties.get("to")));
         Node fromNode = this.service.getNodeById(fromId);
         Node toNode = this.service.getNodeById(toId);
         RelationshipType relationshipType = DynamicRelationshipType.withName(
                 (String)properties.get("relationship_type"));
         Map<String, Object> relationshipProperties = (Map<String, Object>)properties.get("properties");
 
         Relationship upconnectedRelationship;
         try {
             upconnectedRelationship= createOrUpdateRelationship(fromNode, toNode, relationshipType, relationshipProperties);
         } catch (PropertyValueException e) {
             return output.badRequest(e);
         }
 
         return output.ok(new RelationshipRepresentation(upconnectedRelationship));
     }
 
     /**
      * Atomically creates or updates a an edge, utilizing Neo4j transactional locking.
      * @param fromNode Node to attach outgoing side of edge
      * @param toNode Node to attach incoming side of edge
      * @param type Edge type
      * @param properties Key/value pairs to associate with edge
      * @return The created or updated relationship
      * @throws PropertyValueException
      */
     private Relationship createOrUpdateRelationship(
             final Node fromNode,
             final Node toNode,
             final RelationshipType type,
             final Map<String, Object> properties) throws PropertyValueException
     {
         // check if relationship exists first, if it does update properties and GTFO
         Relationship relationship = getRelationshipBetweenNodes(fromNode, toNode, type);
         if (relationship != null) {
             this.propertySetter.setProperties(relationship, properties);
             return relationship;
         }
 
         // otherwise acquire write lock on from node
         Transaction tx = this.service.beginTx();
         Lock writeLock = tx.acquireWriteLock(fromNode);
 
         // check and see if we were beat to the lock, if so update and GTFO
         relationship = getRelationshipBetweenNodes(fromNode, toNode, type);
         if (relationship != null) {
             tx.finish();
             this.propertySetter.setProperties(relationship, properties);
             return relationship;
         }
 
         // alright, let's do the damn thing
         try {
             relationship = fromNode.createRelationshipTo(toNode, type);
             this.propertySetter.setProperties(relationship, properties);
             tx.success();
         } catch (Exception e) {
             System.out.println(e.getMessage());
         } finally {
             tx.finish();
         }
 
         return relationship;
     }
 
     /**
      * Retrieves a relationship of a given type between two nodes if it exists.
      * @param fromNode Starting node
      * @param toNode Ending node
      * @param type Relationship type
      * @return The relationship if it exists, null otherwise.
      */
     private Relationship getRelationshipBetweenNodes(Node fromNode, Node toNode, RelationshipType type)
     {
         // simple linear search over relationships of given type
         Iterable<Relationship> relationships = fromNode.getRelationships(type, Direction.OUTGOING);
         Relationship foundRelationship = null;
         for (Relationship relationship : relationships) {
             if (relationship.getEndNode().equals(toNode)) {
                 foundRelationship = relationship;
                 break;
             }
         }
         return foundRelationship;
     }
 
     /**
      * Parses a node id from a Neo4j REST node URI.
      * @param uri URI to a given node
      * @return Node ID
      */
     private long parseNodeIdFromURI(URI uri)
     {
         String path = uri.getPath();
         String idStr = path.substring(path.lastIndexOf('/') + 1);
         return Long.parseLong(idStr);
     }
 
     /**
      * Validates that required parameters are supplied in upconnect property map
      * @param properties Map containing supplied parameters to upconnect endpoint
      * @return False if any required keys are missing
      */
     private boolean ensureRequiredParameters(Map<String, Object> properties, String ... requiredKeys)
     {
         for (String key : requiredKeys) {
             if (properties.get(key) == null) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Helper method for generating response when required upconnect parameters are missing
      * @param properties Map containing supplied parametes to upconnect endpoint
      * @return Response representing failed conditions on upconnect endpoint
      */
     private Response missingParameters(Map<String, Object> properties, String ... requiredKeys)
     {
         String[] receivedParams = properties.keySet().toArray(new String[0]);
 
         return Response.status( 400 )
                 .type(MediaType.TEXT_PLAIN)
                 .entity("Required parameters: " + implode(requiredKeys) + "\n"
                       + "Received parameters: " + implode(receivedParams))
                 .build();
     }
 
     private static String implode(String[] receivedParams) {
         String receivedParamString = "";
         if (receivedParams.length > 0) {
             StringBuilder sb = new StringBuilder();
             sb.append(receivedParams[0]);
 
             for (int i = 1; i < receivedParams.length; i++) {
                 sb.append(", ");
                 sb.append(receivedParams[i]);
             }
             receivedParamString = sb.toString();
         }
         return receivedParamString;
     }
 
     private Response badJsonFormat(String body) {
         return Response.status( 400 )
                 .type( MediaType.TEXT_PLAIN )
                 .entity( "Invalid JSON array in POST body: " + body )
                 .build();
     }
 
 }
