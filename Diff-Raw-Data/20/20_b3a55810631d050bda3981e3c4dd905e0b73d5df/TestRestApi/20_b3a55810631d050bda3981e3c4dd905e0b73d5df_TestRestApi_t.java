 package org.xorrr.integrationtests;
 
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
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.xorrr.util.EnvironmentVars;
 import org.xorrr.util.LinkObjects;
 
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
 import earth.xor.db.DatastoreFacade;
 import earth.xor.db.LinksDatastore;
 import earth.xor.db.MongoLinksDatastore;
 import earth.xor.helpers.IntegrationTest;
 import earth.xor.model.Link;
 import earth.xor.model.LinkFields;
 import earth.xor.rest.SparkFacade;
 import earth.xor.rest.SparkRestApi;
 import earth.xor.rest.routes.GetAllLinksRoute;
 import earth.xor.rest.routes.GetLinkByIdRoute;
 import earth.xor.rest.routes.PostLinkRoute;
 import earth.xor.rest.routes.Routes;
 import earth.xor.rest.transformation.JSONTransformator;
 
 @Category(IntegrationTest.class)
 public class TestRestApi {
     private static Gson gson;
     private static MongoClient mongoClient;
     private static LinksDatastore linksData;
 
     @BeforeClass
     public static void setUpEmbeddedMongo() throws UnknownHostException,
             IOException {
         EmbedMongo.getInstance();
 
         gson = new Gson();
         mongoClient = new MongoClient("localhost", EmbedMongoProperties.PORT);
         linksData = new MongoLinksDatastore(mongoClient);
 
         RestAssured.port = Integer
                 .parseInt(System.getenv(EnvironmentVars.PORT));
 
         DatastoreFacade facade = new DatastoreFacade(linksData);
         JSONTransformator transformator = new JSONTransformator();
 
         SparkRestApi rest = new SparkRestApi(new SparkFacade());
         rest.setPort(Integer.valueOf(System.getenv(EnvironmentVars.PORT)));
         rest.createGETlinkByIdRoute(new GetLinkByIdRoute(facade, transformator));
         rest.createPOSTlinksRoute(new PostLinkRoute(facade, transformator));
         rest.createGETlinksRoute(new GetAllLinksRoute(facade, transformator));
     }
 
     @Test
     public void authenticationNecessary() {
         expect().statusCode(400).when().post(Routes.POST_LINK);
     }
 
     @Test
     public void dealsWithIncorrectAuth() {
         given().queryParam("pw", "").expect().statusCode(401).when()
                 .post(Routes.POST_LINK);
     }
 
     @Test
     public void dealWithMissingPayload() {
         given().queryParam("pw", System.getenv(EnvironmentVars.PW)).expect()
                 .statusCode(400).when().post(Routes.POST_LINK);
     }
 
     @Test
     public void canPostLink() {
         addAlinkViaRestApi();
 
         DBObject foundEntry = getSavedLinkFromDb();
         assertEquals("http://www.foo.org", foundEntry.get(LinkFields.URL));
         assertEquals("foo", foundEntry.get(LinkFields.TITLE));
         assertEquals("test", foundEntry.get(LinkFields.USER));
     }
 
     @Test
     public void canGetListOfAllSavedLinks() {
         linksData.addLink(LinkObjects.testLink1);
         linksData.addLink(LinkObjects.testLink2);
         linksData.addLink(LinkObjects.testLink3);
 
         expect().header("Access-Control-Allow-Origin", equalTo("*")).when()
                 .get(Routes.GET_ALL_LINKS);
         String jsonResponse = expect().when().get(Routes.GET_ALL_LINKS).asString();
 
         Type type = new TypeToken<Map<String, List<Link>>>() {
         }.getType();
         Map<String, List<Link>> returnedUrls = new HashMap<String, List<Link>>();
         returnedUrls = gson.fromJson(jsonResponse, type);
         checkIfPreviouslyAddedLinksAreShown(returnedUrls);
     }
 
     @Test
    public void canGetSavedLinkById() {
         String id = addLinkAndGetItsId();
 
        String jsonString = expect()
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
         String jsonString = given().body(tryGetLinkToPOSTinJson())
                 .queryParam("pw", System.getenv(EnvironmentVars.PW)).expect()
                 .header("Access-Control-Allow-Origin", equalTo("*")).when()
                 .post("/links").asString();
 
         Link savedLink = gson.fromJson(jsonString, Link.class);
 
         assertEquals("http://www.foo.org", savedLink.getUrl());
         assertEquals("foo", savedLink.getTitle());
         assertEquals("test", savedLink.getUser());
     }
 
     private DBObject getSavedLinkFromDb() {
         DB urlsDb = mongoClient.getDB(LinkFields.DATABASE_NAME);
         DBCollection col = urlsDb.getCollection(LinkFields.LINKS_NAME);
 
         return col.findOne(new BasicDBObject(LinkFields.URL,
                 "http://www.foo.org"));
     }
 
     private String tryGetLinkToPOSTinJson() {
         String jsonString = null;
 
         try {
             File testFile = new File(TestRestApi.class.getResource(
                     "/linksPost.JSON").toURI());
             jsonString = IOUtils.toString(new FileInputStream(testFile));
         } catch (IOException | URISyntaxException e) {
             e.printStackTrace();
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
         linksData.addLink(LinkObjects.testLink1);
 
         DB linksDb = mongoClient.getDB(LinkFields.DATABASE_NAME);
         DBCollection col = linksDb.getCollection(LinkFields.LINKS_NAME);
 
         DBObject foundEntry = col.findOne(new BasicDBObject(LinkFields.URL,
                 "http://www.foo.org"));
 
         return foundEntry.get(LinkFields.ID).toString();
     }
 
     private void checkIfItsTheCorrectlink(
             Map<String, Link> returnedUrlRepresentation) {
        Link foundLink = returnedUrlRepresentation.get("link");
 
         assertEquals("http://www.foo.org", foundLink.getUrl());
         assertEquals("foo", foundLink.getTitle());
         assertEquals("user1", foundLink.getUser());
     }
 
     @After
     public void dropCollection() {
         mongoClient.getDB(LinkFields.DATABASE_NAME)
                 .getCollection(LinkFields.LINKS_NAME).drop();
     }
 }
