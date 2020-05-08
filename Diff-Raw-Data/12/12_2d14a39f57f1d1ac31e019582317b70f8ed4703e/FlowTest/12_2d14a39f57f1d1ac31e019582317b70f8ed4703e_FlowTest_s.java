 package org.pingles.cascading.neo4j.hadoop;
 
 import cascading.flow.Flow;
 import cascading.operation.Identity;
 import cascading.pipe.Each;
 import cascading.pipe.Pipe;
 import cascading.tap.SinkMode;
 import cascading.tap.Tap;
 import cascading.test.HadoopPlatform;
 import cascading.tuple.Fields;
 import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 import org.neo4j.kernel.GraphDatabaseAPI;
 import org.neo4j.rest.graphdb.RestGraphDatabase;
 import org.neo4j.server.WrappingNeoServerBootstrapper;
 import org.neo4j.server.configuration.Configurator;
 import org.neo4j.server.configuration.ServerConfigurator;
 import org.pingles.cascading.neo4j.IndexSpec;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static junit.framework.Assert.*;
 import static org.pingles.cascading.neo4j.local.Neo4jTestCase.toList;
 
 @RunWith(JUnit4.class)
 public class FlowTest {
     public static final String NEO4J_DB_DIR = "target/neo4jdb";
     public static final String REST_CONNECTION_STRING = "http://localhost:7575/db/data";
     private WrappingNeoServerBootstrapper server;
     private HadoopPlatform hadoopPlatform;
     private GraphDatabaseService graphDatabaseService;
 
     @Before
     public void beforeEach() throws IOException {
         hadoopPlatform = new HadoopPlatform();
 
         FileUtils.deleteDirectory(new File(NEO4J_DB_DIR));
         GraphDatabaseAPI graphdb = (GraphDatabaseAPI) new GraphDatabaseFactory()
                 .newEmbeddedDatabaseBuilder(NEO4J_DB_DIR)
                 .newGraphDatabase();
         ServerConfigurator config = new ServerConfigurator( graphdb );
         config.configuration().setProperty(Configurator.WEBSERVER_PORT_PROPERTY_KEY, 7575);
         server = new WrappingNeoServerBootstrapper(graphdb, config);
         server.start();
     }
 
     private void clearNeo4jSystemProperties() {
         System.clearProperty("org.neo4j.rest.batch_transaction");
         System.clearProperty("org.neo4j.rest.read_timeout");
         System.clearProperty("org.neo4j.rest.connect_timeout");
         System.clearProperty("org.neo4j.rest.stream");
         System.clearProperty("org.neo4j.rest.logging_filter");
     }
 
     @After
     public void afterEach() throws IOException {
         neoService().index().forNodes("users").delete();    // CAUTION hard coded
         neoService().index().forNodes("nations").delete();
 
         server.stop();
 
         FileUtils.deleteDirectory(new File(NEO4J_DB_DIR));
         clearNeo4jSystemProperties();
     }
 
     @Test
     public void shouldStoreNodes() {
         Fields sourceFields = new Fields("name");
 
         flowNodes(sourceFields, "src/test/resources/names.csv");
 
         assertEquals(2 + 1, toList(neoService().getAllNodes()).size());
     }
 
     @Test
     public void shouldStoreNodesWhenUsingBatchTransaction() {
         Fields sourceFields = new Fields("name");
 
         HashMap<Object, Object> properties = new HashMap<Object, Object>();
         properties.put("org.neo4j.rest.batch_transaction", "true");
         flowNodes(sourceFields, "src/test/resources/names.csv", properties);
 
         assertEquals(2 + 1, toList(neoService().getAllNodes()).size());
     }
 
     @Test
     public void shouldRemoveLeadingQuestionmarksFromPropertyNamesForCascalogsBenefit() {
         Fields sourceFields = new Fields("?name");
         flowNodes(sourceFields, "src/test/resources/names.csv");
         assertEquals("pingles", neoService().getNodeById(1).getProperty("name"));
     }
 
     @Test
     public void withLeadingQuestionMarkIndexField() {
         Fields sourceFields = new Fields("?name");
         flowNodes(sourceFields, "src/test/resources/names.csv", new IndexSpec("users", sourceFields));
 
         List<Node> nodes = toList(neoService().index().forNodes("users").get("name", "pingles"));
         assertEquals(1, nodes.size());
         assertEquals("pingles", nodes.get(0).getProperty("name"));
    }
 
     @Test
     public void shouldRemoveLeadingExclamationMarkFromPropertyNamesForGreatBenefitOfCascalogs() {
         Fields sourceFields = new Fields("!name");
         flowNodes(sourceFields, "src/test/resources/names.csv");
         assertEquals("pingles", neoService().getNodeById(1).getProperty("name"));
     }
 
     @Test
     public void shouldStoreNodeWithMultipleProperties() {
         Fields sourceFields = new Fields("name", "nationality", "relationshipLabel");
         String filename = "src/test/resources/names_and_nationality.csv";
 
         flowNodes(sourceFields,filename);
         Node node = neoService().getNodeById(1);
         assertEquals(1, node.getId());
         assertEquals("pingles", node.getProperty("name"));
         assertEquals("british", node.getProperty("nationality"));
     }
 
     @Test
     public void shouldSkipNullProperties() {
         Fields sourceFields = new Fields("name", "nationality", "city");
         String filename = "src/test/resources/names_and_properties.csv";
 
         flowNodes(sourceFields, filename);
 
         Node node = neoService().getNodeById(2);
         assertEquals("plam", node.getProperty("name"));
         assertFalse(node.hasProperty("nationality"));
         assertFalse(node.hasProperty("city"));
     }
 
     @Test
     public void shouldStoreNodeWithIndexes() {
         Fields sourceFields = new Fields("name", "nationality", "relationshipLabel");
         String filename = "src/test/resources/names_and_nationality.csv";
         IndexSpec indexSpec = new IndexSpec("users", new Fields("name", "nationality"));
 
         flowNodes(sourceFields, filename, indexSpec);
 
         List<Node> nodes = toList(neoService().index().forNodes("users").get("nationality", "british"));
         assertEquals(2, nodes.size());
         assertEquals("pingles", nodes.get(0).getProperty("name"));
         assertEquals("angrymike", nodes.get(1).getProperty("name"));
 
         nodes = toList(neoService().index().forNodes("users").get("name", "plam"));
         assertEquals(1, nodes.size());
         assertEquals("plam", nodes.get(0).getProperty("name"));
         assertEquals("canadian", nodes.get(0).getProperty("nationality"));
     }
 
     @Test
     public void shouldStoreNodeWithIndexesWithNullProperties() {
         Fields sourceFields = new Fields("name", "nationality", "city");
         String filename = "src/test/resources/names_and_properties.csv";
         IndexSpec indexSpec = new IndexSpec("users", new Fields("name"));
 
         flowNodes(sourceFields, filename, indexSpec);
 
         List<Node> nodes = toList(neoService().index().forNodes("users").get("name", "plam"));
         assertEquals(1, nodes.size());
         assertEquals("plam", nodes.get(0).getProperty("name"));
     }
 
     @Test
     public void shouldCreateRelationBetweenNodes() {
         Fields nameField = new Fields("name");
         Fields relFields = new Fields("name", "nationality", "relationship");
 
         IndexSpec usersIndex = new IndexSpec("users", nameField);
         IndexSpec nationsIndex = new IndexSpec("nations", nameField);
 
         flowNodes(relFields, "src/test/resources/names_and_nationality.csv", usersIndex);
         flowNodes(nameField, "src/test/resources/nationalities.csv", nationsIndex);
 
         flowRelations(relFields, "src/test/resources/names_and_nationality.csv", usersIndex, nationsIndex);
 
         Node pingles = neoService().index().forNodes("users").get("name", "pingles").getSingle();
         List<Relationship> relationships = toList(pingles.getRelationships());
         assertEquals(1, relationships.size());
         assertEquals("british", relationships.get(0).getEndNode().getProperty("name"));
         assertEquals("NATIONALITY", relationships.get(0).getType().name());
     }
 
     @Test
     public void shouldCreateRelationBetweenNodesWithID() {
         Fields nameField = new Fields("name");
         Fields relFields = new Fields("name", "to", "relationship");
 
         IndexSpec usersIndex = new IndexSpec("users", nameField);
 
         flowNodes(nameField, "src/test/resources/names.csv", usersIndex);
 
         flowRelations(relFields, new Class[]{String.class, Long.class, String.class}, "src/test/resources/relations_by_id.csv", usersIndex, IndexSpec.BY_ID);
 
         Node plam = neoService().index().forNodes("users").get("name", "plam").getSingle();
         List<Relationship> relationships = toList(plam.getRelationships());
         assertEquals(1, relationships.size());
         assertEquals(0, relationships.get(0).getEndNode().getId());
         assertEquals("TO_ROOT", relationships.get(0).getType().name());
     }
 
     @Test
     public void shouldCreateRelationshipWhenUsingBatchTransaction() {
         Fields nameField = new Fields("name");
         Fields relFields = new Fields("name", "nationality", "relationship");
 
         IndexSpec usersIndex = new IndexSpec("users", nameField);
         IndexSpec nationsIndex = new IndexSpec("nations", nameField);
 
         HashMap<Object, Object> properties = new HashMap<Object, Object>();
         properties.put("org.neo4j.rest.batch_transaction", "true");
         properties.put("org.pingles.neo4j.batch_size", 20);
 
 
         flowNodes(relFields, "src/test/resources/names_and_nationality.csv", usersIndex);
         flowNodes(nameField, "src/test/resources/nationalities.csv", nationsIndex);
 
         flowRelations(relFields, "src/test/resources/names_and_nationality.csv", usersIndex, nationsIndex);
 
         Node pingles = neoService().index().forNodes("users").get("name", "pingles").getSingle();
         List<Relationship> relationships = toList(pingles.getRelationships());
         assertEquals(1, relationships.size());
         assertEquals("british", relationships.get(0).getEndNode().getProperty("name"));
         assertEquals("NATIONALITY", relationships.get(0).getType().name());
     }
 
     @Test
     public void shouldCreateRelationshipsRemovingCascalogCharacterPrefixes() {
         Fields nameField = new Fields("?name");
         Fields relFields = new Fields("?name", "?nationality", "?relationship");
 
         IndexSpec usersIndex = new IndexSpec("users", nameField);
         IndexSpec nationsIndex = new IndexSpec("nations", nameField);
 
         flowNodes(relFields, "src/test/resources/names_and_nationality.csv", usersIndex);
         flowNodes(nameField, "src/test/resources/nationalities.csv", nationsIndex);
         flowRelations(relFields, "src/test/resources/names_and_nationality.csv", usersIndex, nationsIndex);
 
         Node pingles = neoService().index().forNodes("users").get("name", "pingles").getSingle();
         List<Relationship> relationships = toList(pingles.getRelationships());
         assertEquals(1, relationships.size());
         assertEquals("british", relationships.get(0).getEndNode().getProperty("name"));
         assertEquals("NATIONALITY", relationships.get(0).getType().name());
     }
 
     @Test
     public void shouldCreateRelationshipsWithAdditionalProperties() {
         Fields nameField = new Fields("name");
         Fields relFields = new Fields("name", "nationality", "relationship", "yearsofcitizenship", "passportexpiring");
 
         IndexSpec usersIndex = new IndexSpec("users", nameField);
         IndexSpec nationsIndex = new IndexSpec("nations", nameField);
 
         flowNodes(relFields, "src/test/resources/names_nations_and_more.csv", usersIndex);
         flowNodes(nameField, "src/test/resources/nationalities.csv", nationsIndex);
 
         flowRelations(relFields, "src/test/resources/names_nations_and_more.csv", usersIndex, nationsIndex);
 
         Node pingles = neoService().index().forNodes("users").get("name", "pingles").getSingle();
         List<Relationship> relationships = toList(pingles.getRelationships());
         assertEquals(1, relationships.size());
         assertEquals("british", relationships.get(0).getEndNode().getProperty("name"));
         assertEquals("NATIONALITY", relationships.get(0).getType().name());
         assertEquals("31", relationships.get(0).getProperty("yearsofcitizenship"));
         assertEquals("3", relationships.get(0).getProperty("passportexpiring"));
     }
 
     @Test
     public void shouldCreateRelationshipsAndSkipNullProperties() {
         Fields nameField = new Fields("name");
         Fields relFields = new Fields("name", "nationality", "relationship", "yearsofcitizenship", "passportexpiring");
 
         IndexSpec usersIndex = new IndexSpec("users", nameField);
         IndexSpec nationsIndex = new IndexSpec("nations", nameField);
 
         flowNodes(relFields, "src/test/resources/names_nations_and_more.csv", usersIndex);
         flowNodes(nameField, "src/test/resources/nationalities.csv", nationsIndex);
 
         flowRelations(relFields, "src/test/resources/names_nations_and_more.csv", usersIndex, nationsIndex);
 
         Node plam = neoService().index().forNodes("users").get("name", "plam").getSingle();
         List<Relationship> relationships = toList(plam.getRelationships());
         assertEquals(1, relationships.size());
         assertEquals("canadian", relationships.get(0).getEndNode().getProperty("name"));
         assertEquals("NATIONALITY", relationships.get(0).getType().name());
         assertFalse(relationships.get(0).hasProperty("yearsofcitizenship"));
         assertEquals("1", relationships.get(0).getProperty("passportexpiring"));
     }
 
     private void flowRelations(Fields relationshipFields, String filename, IndexSpec fromIndex, IndexSpec toIndex) {
         flowRelations(relationshipFields, null, filename, fromIndex, toIndex);
     }
 
     private void flowRelations(Fields relationshipFields, Class[] types, String filename, IndexSpec fromIndex, IndexSpec toIndex) {
         Tap relationsTap = hadoopPlatform.getDelimitedFile(relationshipFields, ",", types, filename, SinkMode.KEEP);
        Tap sinkTap = new Neo4jTap(REST_CONNECTION_STRING, new RelationshipScheme(relationshipFields, fromIndex, IndexSpec.BY_ID));
         Pipe nodePipe = new Each("relations", relationshipFields, new Identity());
         Flow nodeFlow = hadoopPlatform.getFlowConnector().connect(relationsTap, sinkTap, nodePipe);
         nodeFlow.complete();
     }
 
     private void flowNodes(Fields sourceFields, String filename) {
         flowNodes(sourceFields, filename, null, new HashMap<Object, Object>());
     }
 
     private void flowNodes(Fields sourceFields, String filename, HashMap<Object, Object> properties) {
         flowNodes(sourceFields, filename, null, properties);
     }
 
     private void flowNodes(Fields sourceFields, String filename, IndexSpec indexSpec) {
         flowNodes(sourceFields, filename, indexSpec, new HashMap<Object, Object>());
     }
 
     private void flowNodes(Fields sourceFields, String filename, IndexSpec indexSpec, Map<Object, Object> properties) {
         NodeScheme scheme;
         if (indexSpec != null) {
             scheme = new NodeScheme(sourceFields, indexSpec);
         } else {
             scheme = new NodeScheme(sourceFields);
         }
 
         Tap nodeSourceTap = hadoopPlatform.getDelimitedFile(sourceFields, ",", filename);
         Tap nodeSinkTap = new Neo4jTap(REST_CONNECTION_STRING, scheme);
         Pipe nodePipe = new Each("Nodes", sourceFields, new Identity());
         Flow nodeFlow = hadoopPlatform.getFlowConnector(properties).connect(nodeSourceTap, nodeSinkTap, nodePipe);
         nodeFlow.complete();
     }
 
     protected GraphDatabaseService neoService() {
         if (graphDatabaseService != null) {
             return graphDatabaseService;
         }
         graphDatabaseService = new RestGraphDatabase(REST_CONNECTION_STRING);
         return graphDatabaseService;
     }
 }
