 package com.crypticbit.javelin;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 
 import org.junit.Test;
 
 import com.crypticbit.javelin.neo4j.Neo4JJsonPersistenceService;
 import com.crypticbit.javelin.neo4j.nodes.ComplexNode;
 import com.crypticbit.javelin.neo4j.nodes.json.ComplexGraphNode;
 
 public class Neo4JJsonPersistenceServiceTest extends Neo4JTestSupport {
 
     @Test
     public void getHistorySimpleReplacement() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write("\"new value 1\"");
 	ps.getRootNode().write("\"new value 2\"");
 	ps.getRootNode().write("\"new value 3\"");
 
 	assertEquals(3, ps.getRootNode().getHistory().size());
 
 	ps.getRootNode().write("\"new value 4\"");
 
 	assertEquals(4, ps.getRootNode().getHistory().size());
 
 	// FIXME - re-insert
 	// assertTrue("Check most recent history comes first", ps.getRootNode().getHistory().get(0).getTimestamp() > ps
 	// .getRootNode().getHistory().get(3).getTimestamp());
 
 	// assertEquals(MAPPER.readTree("\"new value 2\""),
 	// MAPPER.readTree(ps.getRootNode().getHistory().get(2).getVersion().toJsonString()));
     }
 
     @Test
     public void getHistoryOfAdd() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ComplexNode rootNode = ps.getRootNode();
 	rootNode.write(JSON_TEXT);
 	rootNode.navigate("second").add().write("\"new value 1\"");
 
 	// ps.startWebServiceAndWait();
 
 	assertEquals(2, ((ComplexNode) ps.getRootNode().navigate("second")).getHistory().size());
 
 	ps.getRootNode().navigate("second").add().write("\"new value 2\"");
 
 	// FIXME - stop need for cast
 	assertEquals(3, ((ComplexNode) ps.getRootNode().navigate("second")).getHistory().size());
     }
 
     @Test
     public void testAddOfNewNonRootArrayNode() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write(JSON_TEXT);
 	ps.getRootNode().navigate("second").add().write("\"new value\"");
 
 	// ps.startWebServiceAndWait();
 
 	assertEquals(
 		MAPPER.readTree("{\"first\": 123, \"second\": [{\"k1\":{\"id\":\"sd1 p\"}}, 4, 5, 6, {\"id\": 123},\"new value\"], \"third\": 789, \"xid\": null}"),
 		MAPPER.readTree(ps.getRootNode().toJsonString()));
     }
 
     @Test
     public void testBasicWriteFromRoot() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write(JSON_TEXT);
 	assertEquals(MAPPER.readTree(JSON_TEXT), MAPPER.readTree(ps.getRootNode().toJsonString()));
     }
 
     @Test
     public void testNavigate() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write(JSON_TEXT);
 
 	assertEquals(MAPPER.readTree("{\"k1\":{\"id\":\"sd1 p\"}}"),
 		MAPPER.readTree(ps.getRootNode().navigate("second[0]").toJsonString()));
 
 	assertEquals(MAPPER.readTree("{\"id\":\"sd1 p\"}"),
 		MAPPER.readTree(ps.getRootNode().navigate("second[0].k1").toJsonString()));
 
     }
 
     @Test
     public void testNavigateWithWildcards() throws IOException, JsonPersistenceException, IllegalJsonException {
 	// these are currently not supported
 
     }
 
     @Test
     public void testOverwriteOfNonRootArrayNode() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write(JSON_TEXT);
 
 	ps.getRootNode().navigate("second[0]").write("\"new value 1\"");
 
 	assertEquals(
 		MAPPER.readTree("{\"first\": 123, \"second\": [\"new value 1\", 4, 5, 6, {\"id\": 123}], \"third\": 789, \"xid\": null}"),
 		MAPPER.readTree(ps.getRootNode().toJsonString()));
     }
 
     @Test
     public void testOverwriteOfNonRootMapNode() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write(JSON_TEXT);
 	ps.getRootNode().navigate("second").write("\"new value\"");
 
 	assertEquals(MAPPER.readTree("{\"first\": 123, \"second\": \"new value\", \"third\": 789, \"xid\": null}"),
 		MAPPER.readTree(ps.getRootNode().toJsonString()));
 
 	ps.getRootNode().navigate("second").write("[0,1,2]");
 
 	assertEquals(MAPPER.readTree("{\"first\": 123, \"second\": [0,1,2], \"third\": 789, \"xid\": null}"),
 		MAPPER.readTree(ps.getRootNode().toJsonString()));
     }
 
     @Test
     public void testNavigateToNewNode() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write(JSON_TEXT);
 	ps.getRootNode().navigate("second[0].k1.newNode").write("\"very new stuff\"");
 
 	assertEquals(MAPPER.readTree("{\"newNode\":\"very new stuff\",\"id\":\"sd1 p\"}"),
 		MAPPER.readTree(ps.getRootNode().navigate("second[0].k1").toJsonString()));
 
 	// FIXME - remove cast
 	ComplexNode foundNode = ((ComplexNode) ps.getRootNode().navigate("second[0].k1.a1.a2"));
 	foundNode.write("\"even newer stuff\"");
	
 	assertEquals(
 		MAPPER.readTree("{\"id\":\"sd1 p\", \"newNode\":\"very new stuff\", \"a1\":{\"a2\":\"even newer stuff\"}}"),
 		MAPPER.readTree(ps.getRootNode().navigate("second[0].k1").toJsonString()));
 
 	ps.getRootNode().navigate("second[0].k1.a1.a3[0].b.c").write("\"at end of newly created chain\"");
 	assertEquals(
 		MAPPER.readTree("{\"a3\":[{\"b\":{\"c\":\"at end of newly created chain\"}}], \"a2\":\"even newer stuff\"}"),
 		MAPPER.readTree(ps.getRootNode().navigate("second[0].k1.a1").toJsonString()));
 
     }
 
     @Test
     public void testOverwriteRoot() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write(JSON_TEXT);
 	ps.getRootNode().write("\"new value 1\"");
 
 	assertEquals(MAPPER.readTree("\"new value 1\""), MAPPER.readTree(ps.getRootNode().toJsonString()));
     }
 
     @Test
     public void testPersistenceBetweenSessions() throws IOException, JsonPersistenceException, IllegalJsonException {
 	File file = Files.createTempDirectory("neo4j_test").toFile();
 	Neo4JJsonPersistenceService ps = new Neo4JJsonPersistenceService(file, "i1");
 
 	ComplexGraphNode rootNode = ps.getRootNode();
 	rootNode.write(JSON_TEXT);
 	assertEquals(MAPPER.readTree(JSON_TEXT), MAPPER.readTree(ps.getRootNode().toJsonString()));
 	ps.close();
 
 	ps = new Neo4JJsonPersistenceService(file, "i1");
 
 	rootNode = ps.getRootNode();
 	assertEquals(MAPPER.readTree(JSON_TEXT), MAPPER.readTree(ps.getRootNode().toJsonString()));
 	ps.close();
 
     }
 
     @Test
     public void testPutOfExistingNonRootMapNode() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write(JSON_TEXT);
 	ps.getRootNode().put("second").write("\"new value\"");
 
 	assertEquals(MAPPER.readTree("{\"first\": 123, \"second\": \"new value\", \"third\": 789, \"xid\": null}"),
 		MAPPER.readTree(ps.getRootNode().toJsonString()));
     }
 
     @Test
     public void testPutOfNewNonRootMapNode() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write(JSON_TEXT);
 
 	ps.getRootNode().put("new").write("\"new value\"");
 
 	assertEquals(
 		MAPPER.readTree("{\"new\":\"new value\",\"first\": 123, \"second\": [{\"k1\":{\"id\":\"sd1 p\"}}, 4, 5, 6, {\"id\": 123}], \"third\": 789, \"xid\": null}"),
 		MAPPER.readTree(ps.getRootNode().toJsonString()));
     }
 
     @Test
     public void testMultipleWriteToSameNode() throws IOException, JsonPersistenceException, IllegalJsonException {
 	Neo4JJsonPersistenceService ps = createNewService();
 
 	ps.getRootNode().write(JSON_TEXT);
 	// FIXME remove cast
 	ComplexNode aNode = (ComplexNode) ps.getRootNode().navigate("second");
 	aNode.write("[2,3,4]");
 	String writeValue = "[2,3,4,5]";
 
 	aNode.write(writeValue);
 	assertEquals(MAPPER.readTree(writeValue), MAPPER.readTree(ps.getRootNode().navigate("second").toJsonString()));
     }
 
     // @Test
     // public void testUpdateToSameNodeAfterWrite() throws IOException, JsonPersistenceException, IllegalJsonException {
     // Neo4JJsonPersistenceService ps = createNewService();
     //
     // ps.getRootNode().write(JSON_TEXT);
     // ComplexNode aNode = ps.getRootNode().navigate("second");
     // String writeValue = "[2,3,4,5]";
     // aNode.write(writeValue);
     // assertEquals(MAPPER.readTree(writeValue), MAPPER.readTree(aNode.toJsonString()));
     // }
 
     // test ideas
     // * different strategies
     // * another strategy that requires one to many in series with
     // timestampedhistoryadapter
     // * dynamic change of strategy from one node to next
 
 }
