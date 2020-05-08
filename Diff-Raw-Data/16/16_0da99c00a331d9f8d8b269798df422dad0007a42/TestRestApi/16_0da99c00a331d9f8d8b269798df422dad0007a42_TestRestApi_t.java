 package earth.xor.rest;
 
 import static com.jayway.restassured.RestAssured.expect;
 import static com.jayway.restassured.RestAssured.given;
 import static org.hamcrest.Matchers.equalTo;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.After;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.jayway.restassured.RestAssured;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 import earth.xor.EmbedMongo;
 import earth.xor.EmbedMongoProperties;
 import earth.xor.ExampleLinks;
import earth.xor.db.DatastoreFacade;
 import earth.xor.db.Link;
 import earth.xor.db.LinkFields;
import earth.xor.db.MongoLinksDatastore;
 
 @Ignore
 public class TestRestApi {
 
     private static Gson gson;
     private static MongoClient mongoClient;
     private static MongoLinksDatastore linksData;
 
     @BeforeClass
     public static void setUpEmbeddedMongo() throws UnknownHostException,
             IOException {
         EmbedMongo.getInstance();
 
         gson = new Gson();
         mongoClient = new MongoClient("localhost", EmbedMongoProperties.PORT);
         linksData = new MongoLinksDatastore(mongoClient);
 
         RestAssured.port = Integer.parseInt(System.getenv("PORT"));
        
        DatastoreFacade facade = new DatastoreFacade(linksData);
        SparkRestApi rest = new SparkRestApi(facade);
        rest.startApi();
     }
 
     @Test
     public void testAddingALinkViaRestApi() {
         addAlinkViaRestApi();
         checkIfLinkWasAddedToDatabase();
     }
 
     @Test
     public void testGettingAListOfAllSavedUrls() {
 
         linksData.addLink(ExampleLinks.testLink1);
         linksData.addLink(ExampleLinks.testLink2);
         linksData.addLink(ExampleLinks.testLink3);
 
         expect().contentType("application/json").when()
                 .get(LinkFields.LINKS_ROUTE);
         expect().header("Access-Control-Allow-Origin", equalTo("*")).when()
                 .get(LinkFields.LINKS_ROUTE);
 
         String jsonResponse = expect().when().get(LinkFields.LINKS_ROUTE)
                 .asString();
 
         Type type = new TypeToken<Map<String, List<Link>>>() {
         }.getType();
 
         Map<String, List<Link>> returnedUrls = new HashMap<String, List<Link>>();
 
         returnedUrls = gson.fromJson(jsonResponse, type);
 
         checkIfPreviouslyAddedLinksAreShown(returnedUrls);
     }
 
     @Test
     public void testGettingASavedLinkById() {
         String id = addLinkAndGetItsId();
 
        String jsonString = expect().contentType("application/json").and()
                 .header("Access-Control-Allow-Origin", equalTo("*")).when()
                 .get("/links/" + id).asString();
 
         assertTrue(isIdSurroundedWithDoubleQuotes(id, jsonString));
 
         Type type = new TypeToken<Map<String, Link>>() {
         }.getType();
 
         Map<String, Link> returnedUrlRepresentation = new HashMap<String, Link>();
 
         returnedUrlRepresentation = gson.fromJson(jsonString, type);
 
         checkIfItsTheCorrectlink(returnedUrlRepresentation);
     }
 
     private boolean isIdSurroundedWithDoubleQuotes(String id, String jsonString) {
         return jsonString.contains("\"" + id + "\"");
     }
 
     private void addAlinkViaRestApi() {
         String jsonString = given().body(getLinkToPOSTjson())
                 .queryParam("pw", System.getenv("PASS")).expect()
                 .contentType("application/json").and()
                 .header("Access-Control-Allow-Origin", equalTo("*")).when()
                 .post("/links").asString();
 
         Link savedLink = gson.fromJson(jsonString, Link.class);
 
         assertEquals("http://www.foo.org", savedLink.getUrl());
         assertEquals("foo", savedLink.getTitle());
         assertEquals("test", savedLink.getUser());
     }
 
     private void checkIfLinkWasAddedToDatabase() {
         DB urlsDb = mongoClient.getDB(LinkFields.DATABASE_NAME);
         DBCollection col = urlsDb.getCollection(LinkFields.LINKS_NAME);
 
         DBObject foundEntry = col.findOne(new BasicDBObject(LinkFields.URL,
                 "http://www.foo.org"));
 
         assertEquals("http://www.foo.org", foundEntry.get(LinkFields.URL));
         assertEquals("foo", foundEntry.get(LinkFields.TITLE));
         assertEquals("test", foundEntry.get(LinkFields.USER));
     }
 
     private String getLinkToPOSTjson() {
         return tryGetLinkToPOSTinJson();
     }
 
     private String tryGetLinkToPOSTinJson() {
         String jsonString = null;
         try {
             File testFile = new File(TestRestApi.class.getResource(
                     "/linksPost.JSON").toURI());
             jsonString = IOUtils.toString(new FileInputStream(testFile));
         } catch (IOException e) {
             e.printStackTrace();
         } catch (URISyntaxException e1) {
             e1.printStackTrace();
         }
         return jsonString;
     }
 
     private void checkIfPreviouslyAddedLinksAreShown(
             Map<String, List<Link>> returnedUrls) {
 
         ArrayList<Link> allUrls = (ArrayList<Link>) returnedUrls
                 .get(LinkFields.LINKS_NAME);
 
         assertEquals("http://www.foo.org", allUrls.get(0).getUrl());
         assertEquals("foo", allUrls.get(0).getTitle());
         assertEquals("user1", allUrls.get(0).getUser());
 
         assertNotNull(allUrls.get(0).getTimeStamp());
 
         assertEquals("http://www.bar.org", allUrls.get(1).getUrl());
         assertEquals("bar", allUrls.get(1).getTitle());
         assertEquals("user2", allUrls.get(1).getUser());
 
         assertNotNull(allUrls.get(1).getTimeStamp());
 
         assertEquals("http://www.baz.org", allUrls.get(2).getUrl());
         assertEquals("baz", allUrls.get(2).getTitle());
         assertEquals("user3", allUrls.get(2).getUser());
 
         assertNotNull(allUrls.get(2).getTimeStamp());
     }
 
     private String addLinkAndGetItsId() {
         linksData.addLink(ExampleLinks.testLink1);
 
         DB linksDb = mongoClient.getDB(LinkFields.DATABASE_NAME);
         DBCollection col = linksDb.getCollection(LinkFields.LINKS_NAME);
 
         DBObject foundEntry = col.findOne(new BasicDBObject(LinkFields.URL,
                 "http://www.foo.org"));
 
         return foundEntry.get(LinkFields.ID).toString();
     }
 
     private void checkIfItsTheCorrectlink(
             Map<String, Link> returnedUrlRepresentation) {
         Link foundLink = returnedUrlRepresentation.get(LinkFields.URL);
 
         assertEquals("http://www.foo.org", foundLink.getUrl());
         assertEquals("foo", foundLink.getTitle());
         assertEquals("user1", foundLink.getUser());
     }
 
     @Test
     public void testAuthentication() {
         expect().statusCode(401).when().post(LinkFields.LINKS_ROUTE);
         given().queryParam("pw", System.getenv("PASS")).expect()
                 .statusCode(200).when().get(LinkFields.LINKS_ROUTE);
     }
 
     @After
     public void dropCollection() {
         mongoClient.getDB(LinkFields.DATABASE_NAME)
                 .getCollection(LinkFields.LINKS_NAME).drop();
     }
 }
