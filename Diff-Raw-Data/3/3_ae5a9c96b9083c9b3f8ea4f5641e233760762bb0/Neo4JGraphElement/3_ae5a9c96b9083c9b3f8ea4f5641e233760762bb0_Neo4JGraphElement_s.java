 package org.triple_brain.module.neo4j_graph_manipulator.graph;
 
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.assistedinject.AssistedInject;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import org.joda.time.DateTime;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.PropertyContainer;
 import org.triple_brain.module.common_utils.Uris;
 import org.triple_brain.module.model.ExternalFriendlyResource;
 import org.triple_brain.module.model.TripleBrainUris;
 import org.triple_brain.module.model.User;
 import org.triple_brain.module.model.graph.GraphElement;
 
 import java.net.URI;
 import java.util.*;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class Neo4JGraphElement implements GraphElement {
 
     public static final String CREATION_DATE_PROPERTY_NAME = "creation_date";
     public static final String LAST_MODIFICATION_DATE_PROPERTY_NAME = "last_modification_date";
 
     private PropertyContainer propertyContainer;
     private User owner;
 
     Neo4JExternalFriendlyResourcePersistenceUtils friendlyResourceUtils;
 
     @AssistedInject
     protected Neo4JGraphElement(
             Neo4JExternalFriendlyResourcePersistenceUtils friendlyResourceUtils,
             @Assisted PropertyContainer propertyContainer,
             @Assisted User owner
     ) {
         this.friendlyResourceUtils = friendlyResourceUtils;
         this.propertyContainer = propertyContainer;
         this.owner = owner;
     }
 
     @AssistedInject
     protected Neo4JGraphElement(
             Neo4JExternalFriendlyResourcePersistenceUtils friendlyResourceUtils,
             @Assisted PropertyContainer propertyContainer,
             @Assisted URI uri,
             @Assisted User owner
     ) {
         this(
                 friendlyResourceUtils,
                 propertyContainer,
                 owner
         );
         propertyContainer.setProperty(
                 Neo4JUserGraph.URI_PROPERTY_NAME,
                 uri.toString()
         );
         this.label("");
         Long creationDate = new Date().getTime();
         propertyContainer.setProperty(
                 CREATION_DATE_PROPERTY_NAME,
                 creationDate
         );
         propertyContainer.setProperty(
                 LAST_MODIFICATION_DATE_PROPERTY_NAME,
                 creationDate
         );
     }
 
     @Override
     public DateTime creationDate() {
         return new DateTime((Long) propertyContainer.getProperty(
                 CREATION_DATE_PROPERTY_NAME
         ));
     }
 
     @Override
     public DateTime lastModificationDate() {
         return new DateTime((Long) propertyContainer.getProperty(
                 LAST_MODIFICATION_DATE_PROPERTY_NAME
         ));
     }
 
     public void updateLastModificationDate() {
         propertyContainer.setProperty(
                 LAST_MODIFICATION_DATE_PROPERTY_NAME,
                 new Date().getTime()
         );
     }
 
     @Override
     public String id() {
         return propertyContainer.getProperty(Neo4JUserGraph.URI_PROPERTY_NAME).toString();
     }
 
     @Override
     public String label() {
         return propertyContainer.getProperty(RDFS.label.getURI()).toString();
     }
 
     @Override
     public void label(String label) {
         propertyContainer.setProperty(RDFS.label.getURI(), label);
         updateLastModificationDate();
     }
 
     @Override
     public boolean hasLabel() {
         return propertyContainer.hasProperty(RDFS.label.getURI());
     }
 
     @Override
     public User owner() {
         return owner;
     }
 
     @Override
     public void addSameAs(ExternalFriendlyResource friendlyResource) {
         Node node = friendlyResourceUtils.getOrCreate(
                 friendlyResource
         );
         addResourceUriConnectedWithRelationName(
                 (String) node.getProperty(
                         Neo4JUserGraph.URI_PROPERTY_NAME
                 ),
                 Relationships.SAME_AS.name()
         );
         updateLastModificationDate();
     }
 
     @Override
     public Set<ExternalFriendlyResource> getSameAs() {
         return getExternalFriendlyResourcesOfRelation(
                 Relationships.SAME_AS.name()
         );
     }
 
     @Override
     public ExternalFriendlyResource friendlyResourceWithUri(URI uri) {
         return friendlyResourceUtils.getFromUri(uri);
     }
 
     @Override
     public void addType(ExternalFriendlyResource type) {
         Node node = friendlyResourceUtils.getOrCreate(
                 type
         );
         addResourceUriConnectedWithRelationName(
                 (String) node.getProperty(
                         Neo4JUserGraph.URI_PROPERTY_NAME
                 ),
                 Relationships.TYPE.name()
         );
         updateLastModificationDate();
     }
 
     @Override
     public void removeFriendlyResource(ExternalFriendlyResource friendlyResource) {
         removeRelationToExternalResource(
                 Relationships.SAME_AS.name(),
                 friendlyResource
         );
         removeRelationToExternalResource(
                 Relationships.TYPE.name(),
                 friendlyResource
         );
         updateLastModificationDate();
     }
 
     @Override
     public Set<ExternalFriendlyResource> getAdditionalTypes(){
         Set<ExternalFriendlyResource> friendlyResources = getExternalFriendlyResourcesOfRelation(
                 Relationships.TYPE.name()
         );
         friendlyResources.remove(friendlyResourceUtils.loadFromUri(
                 Uris.get(TripleBrainUris.TRIPLE_BRAIN_VERTEX)
         ));
         return friendlyResources;
     }
 
     private String removeEnclosingCharsOfListAsString(String listAsString) {
         return listAsString.substring(
                 1,
                 listAsString.length() - 1
         );
     }
 
     private Set<ExternalFriendlyResource> getExternalFriendlyResourcesOfRelation(String relationName) {
         Set<ExternalFriendlyResource> set = new HashSet<ExternalFriendlyResource>();
         List<String> uris = getListOfResourcesUriConnectedWithRelationshipName(
                 relationName
         );
         for (String uri : uris) {
             set.add(
                     friendlyResourceUtils.loadFromUri(
                             Uris.get(uri)
                     )
             );
         }
         return set;
     }
 
     private void addResourceUriConnectedWithRelationName(String uri, String relationName) {
         List<String> uris = getListOfResourcesUriConnectedWithRelationshipName(
                 relationName
         );
         uris.add(
                 uri
         );
         propertyContainer.setProperty(
                 relationName,
                 uris.toString()
         );
     }
 
     private List<String> getSameAsResourcesUri() {
         return getListOfResourcesUriConnectedWithRelationshipName(
                 Relationships.SAME_AS.name()
         );
     }
 
     private List<String> getTypeResourcesUri() {
         return getListOfResourcesUriConnectedWithRelationshipName(
                 Relationships.TYPE.name()
         );
     }
 
     private List<String> getListOfResourcesUriConnectedWithRelationshipName(String propertyName) {
         if (!propertyContainer.hasProperty(propertyName)) {
             return new ArrayList<String>();
         }
         String urisAsString = (String) propertyContainer.getProperty(
                 propertyName
         );
         urisAsString = removeEnclosingCharsOfListAsString(
                 urisAsString
         );
         return new ArrayList<String>(
                 Arrays.asList(
                         urisAsString.split("\\s*,\\s*")
                 )
         );
     }
 
     private void removeRelationToExternalResource(String relationName, ExternalFriendlyResource friendlyResource) {
         List<String> uris = getListOfResourcesUriConnectedWithRelationshipName(
                 relationName
         );
         boolean wasUriRemoved = uris.remove(
                 friendlyResource.uri().toString()
         );
         if (wasUriRemoved) {
             propertyContainer.setProperty(
                     relationName,
                     uris.toString()
             );
         }
     }
 }
