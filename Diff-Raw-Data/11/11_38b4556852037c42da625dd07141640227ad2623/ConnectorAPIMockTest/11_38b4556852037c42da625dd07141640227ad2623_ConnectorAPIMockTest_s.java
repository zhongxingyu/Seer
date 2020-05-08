 package eu.scapeproject;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.namespace.QName;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.purl.dc.elements._1.ElementContainer;
 import org.purl.dc.elements._1.SimpleLiteral;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.scapeproject.model.BitStream;
 import eu.scapeproject.model.BitStream.Type;
 import eu.scapeproject.model.Identifier;
 import eu.scapeproject.model.IntellectualEntity;
 import eu.scapeproject.model.IntellectualEntityCollection;
 import eu.scapeproject.model.LifecycleState;
 import eu.scapeproject.model.LifecycleState.State;
 import eu.scapeproject.model.Representation;
 import eu.scapeproject.model.VersionList;
 import eu.scapeproject.util.DefaultConverter;
 import eu.scapeproject.util.ScapeMarshaller;
 import gov.loc.mets.MetsType;
 
 public class ConnectorAPIMockTest {
 
     private static final ConnectorAPIMock MOCK = new ConnectorAPIMock(8387);
     private static final ConnectorAPIUtil UTIL = new ConnectorAPIUtil("http://localhost:8387");
     private static final HttpClient CLIENT = new DefaultHttpClient();
     private static final Logger log = LoggerFactory.getLogger(ConnectorAPIMockTest.class);
     private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 
     @BeforeClass
     public static void setup() throws Exception {
         Thread t = new Thread(MOCK);
         t.start();
         while (!MOCK.isRunning()) {
             Thread.sleep(10);
         }
     }
 
     @AfterClass
     public static void tearDown() throws Exception {
         MOCK.stop();
         MOCK.close();
         assertFalse(MOCK.isRunning());
     }
 
     public ElementContainer createDCElementContainer(){
         ElementContainer c = new ElementContainer();
         
         SimpleLiteral title = new SimpleLiteral();
         title.getContent().add("A test entity");
         c.getAny().add(new JAXBElement<SimpleLiteral>(new QName("http://purl.org/dc/elements/1.1/", "title"), SimpleLiteral.class, title));
         
         SimpleLiteral date = new SimpleLiteral();
         date.getContent().add(dateFormat.format(new Date()));
         c.getAny().add(new JAXBElement<SimpleLiteral>(new QName("http://purl.org/dc/elements/1.1/", "created"), SimpleLiteral.class, date));
         
         SimpleLiteral lang = new SimpleLiteral();
         lang.getContent().add("en");
         c.getAny().add(new JAXBElement<SimpleLiteral>(new QName("http://purl.org/dc/elements/1.1/", "created"), SimpleLiteral.class, lang));
         
         return c;
     }
     
     @Test
     public void testGetIntellectualEntityList() throws Exception {
         List<String> ids = new ArrayList<String>();
         // ingest entity 1
         IntellectualEntity entity1 = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .descriptive(createDCElementContainer())
                 .build();
         ids.add(entity1.getIdentifier().getValue());
         HttpPost post = UTIL.createPostEntity(entity1);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         // ingest entity 2
         IntellectualEntity entity2 = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .descriptive(createDCElementContainer())
                 .build();
         ids.add(entity2.getIdentifier().getValue());
         post = UTIL.createPostEntity(entity2);
         resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         StringBuilder uriList = new StringBuilder();
         for (String id : ids) {
             uriList.append(id + "\n");
         }
         post = UTIL.createGetUriList(uriList.toString());
         resp = CLIENT.execute(post);
         // IOUtils.copy(resp.getEntity().getContent(), System.out);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
     }
 
     @Test
     public void testGetVersionList() throws Exception {
         IntellectualEntity version1 = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .descriptive(createDCElementContainer())
                 .build();
         HttpPost post = UTIL.createPostEntity(version1);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         IntellectualEntity version2 = new IntellectualEntity.Builder(version1)
                 .build();
         HttpPut put = UTIL.createPutEntity(version2);
         resp = CLIENT.execute(put);
         put.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
 
         HttpGet get = UTIL.createGetVersionList(version1.getIdentifier().getValue());
         resp = CLIENT.execute(get);
         VersionList versions = (VersionList) ScapeMarshaller.newInstance().deserialize(resp.getEntity().getContent());
         get.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         assertTrue(versions.getVersionIdentifiers().size() == 2);
         assertTrue(versions.getEntityId().equals(version1.getIdentifier().getValue()));
 
     }
 
     @Test
     public void testIngestImage() throws Exception {
         IntellectualEntity entity = ModelUtil.createEntity(Arrays.asList(ModelUtil.createImageRepresentation(URI
                 .create("https://upload.wikimedia.org/wikipedia/en/7/71/Quebec_citadelles_200x200.png"), null)));
         HttpPost post = UTIL.createPostEntity(entity);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         String id = IOUtils.toString(resp.getEntity().getContent());
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         HttpGet get = UTIL.createGetEntity(entity.getIdentifier().getValue());
         resp = CLIENT.execute(get);
         IntellectualEntity fetched = ScapeMarshaller.newInstance().deserialize(IntellectualEntity.class, resp.getEntity().getContent());
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         get.releaseConnection();
         assertTrue(fetched.getLifecycleState().getState().equals(State.INGESTED));
         assertEquals(id, fetched.getIdentifier().getValue());
         assertEquals(entity.getIdentifier(), fetched.getIdentifier());
         assertEquals(entity.getAlternativeIdentifiers(), fetched.getAlternativeIdentifiers());
 //        assertEquals(entity.getDescriptive(),fetched.getDescriptive());
         ElementContainer postEc = (ElementContainer) entity.getDescriptive();
         ElementContainer getEc = (ElementContainer) fetched.getDescriptive();
         assertTrue("DC metadata does not match",
         postEc.getAny().get(0).getValue().getContent().equals(getEc.getAny().get(0).getValue().getContent()));
 		
         Representation postRep = entity.getRepresentations().get(0);
         Representation getRep = fetched.getRepresentations().get(0);
         assertTrue("Representation identifier does not match", postRep.getIdentifier().getValue().equals(getRep.getIdentifier().getValue()));
 //        assertTrue(ListUtil.compareLists(Representation.class,entity.getRepresentations(),fetched.getRepresentations()));
     }
 
     @Test
     public void testIngestMinimalIntellectualEntity() throws Exception {
         IntellectualEntity ie = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .descriptive(createDCElementContainer())
                 .build();
         HttpPost post = UTIL.createPostEntity(ie);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         HttpGet get = UTIL.createGetEntity(ie.getIdentifier().getValue());
         resp = CLIENT.execute(get);
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         get.releaseConnection();
     }
 
     @Test
     public void testIngestMinimalIntellectualEntityWithoutId() throws Exception {
         IntellectualEntity ie = new IntellectualEntity.Builder()
                 .descriptive(createDCElementContainer())
                 .build();
         HttpPost post = UTIL.createPostEntity(ie);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         String id = IOUtils.toString(resp.getEntity().getContent());
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         HttpGet get = UTIL.createGetEntity(id);
         resp = CLIENT.execute(get);
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         IntellectualEntity fetched = ScapeMarshaller.newInstance().deserialize(IntellectualEntity.class, resp.getEntity().getContent());
         get.releaseConnection();
         assertEquals(id, fetched.getIdentifier().getValue());
         assertEquals(ie.getAlternativeIdentifiers(), fetched.getAlternativeIdentifiers());
 //        assertEquals(ie.getDescriptive(),fetched.getDescriptive());
 	ElementContainer postEc = (ElementContainer) ie.getDescriptive();
 	ElementContainer getEc = (ElementContainer) fetched.getDescriptive();
         assertTrue("DC metadata does not match",
 				postEc.getAny().get(0).getValue().getContent().equals(getEc.getAny().get(0).getValue().getContent()));
         assertEquals(LifecycleState.State.INGESTED,fetched.getLifecycleState().getState());
     }
 
     @Test
     public void testIngestMinimalIntellectualEntityAsynchronously() throws Exception {
         IntellectualEntity ie = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .descriptive(createDCElementContainer())
                 .build();
         HttpPost post = UTIL.createPostEntityAsync(ie);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
 
         // check the lifecyclestate
         HttpGet get = UTIL.createGetEntityLifecycleState(ie.getIdentifier().getValue());
         resp = CLIENT.execute(get);
         LifecycleState lifecycle = (LifecycleState) ScapeMarshaller.newInstance().deserialize(resp.getEntity().getContent());
         get.releaseConnection();
         assertTrue(lifecycle.getState() == State.INGESTING);
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
 
         // wait for the state to change for 15 secs, then throw an exception
         long timeStart = System.currentTimeMillis();
         while (lifecycle.getState() != State.INGESTED) {
             int elapsed = (int) ((System.currentTimeMillis() - timeStart) / 1000D);
             if (elapsed > 15) {
                 fail("timeout while asynchronously ingesting object");
             }
             log.info("TEST: waiting for ingestion. " + elapsed + " seconds passed");
             Thread.sleep(1000);
             get = UTIL.createGetEntityLifecycleState(ie.getIdentifier().getValue());
             resp = CLIENT.execute(get);
             lifecycle = (LifecycleState) ScapeMarshaller.newInstance().deserialize(resp.getEntity().getContent());
         }
     }
 
     @Test
     public void testInvalidIntellectualEntity() throws Exception {
         HttpGet get = UTIL.createGetEntity("non-existant");
         HttpResponse resp = CLIENT.execute(get);
         assertTrue(resp.getStatusLine().getStatusCode() == 404);
         get.releaseConnection();
     }
 
     @Test
     public void testRetrieveBitStream() throws Exception {
         BitStream bs = new BitStream.Builder()
                 .type(Type.STREAM)
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .build();
 
         IntellectualEntity entity = ModelUtil
                 .createEntity(Arrays.asList(ModelUtil.createImageRepresentation(URI
                        .create("https://upload.wikimedia.org/wikipedia/en/7/71/Quebec_citadelles_200x200.png"),
                         Arrays.asList(bs))));
         HttpPost post = UTIL.createPostEntity(entity);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         HttpGet get = UTIL.createGetBitStream(
                 entity.getRepresentations().get(0).getFiles().get(0).getBitStreams().get(0).getIdentifier().getValue());
         resp = CLIENT.execute(get);
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         BitStream fetched = ScapeMarshaller.newInstance().deserialize(BitStream.class, resp.getEntity().getContent());
         get.releaseConnection();
        assertEquals(entity.getRepresentations().get(0).getFiles().get(0).getBitStreams().get(0), fetched);
     }
 
     @Test
     public void testRetrieveFile() throws Exception {
         IntellectualEntity entity = ModelUtil.createEntity(Arrays.asList(ModelUtil.createImageRepresentation(URI
                 .create("https://upload.wikimedia.org/wikipedia/en/7/71/Quebec_citadelles_200x200.png"), null)));
         HttpPost post = UTIL.createPostEntity(entity);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         HttpGet get = UTIL.createGetFile(entity.getRepresentations().get(0).getFiles().iterator().next());
         resp = CLIENT.execute(get);
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         String xml = IOUtils.toString(resp.getEntity().getContent());
         assertTrue(xml.length() > 10); // check for some content
         get.releaseConnection();
     }
     @Ignore
     @Test
     public void testRetrieveIntellectualEntityWithRefs() throws Exception {
         IntellectualEntity ie = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .descriptive(createDCElementContainer())
                 .build();
         HttpPost post = UTIL.createPostEntity(ie);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         HttpGet get = UTIL.createGetEntity(ie.getIdentifier().getValue(), true);
         resp = CLIENT.execute(get);
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         MetsType doc = (MetsType) ScapeMarshaller.newInstance().deserialize(resp.getEntity().getContent());
         assertTrue(doc.getDmdSec() != null);
         assertTrue(doc.getDmdSec().get(0).getMdRef() != null);
         assertTrue(doc.getDmdSec().get(0).getMdWrap() == null);
         get.releaseConnection();
     }
 
     @Test
     public void testRetrieveMetadataRecord() throws Exception {
         IntellectualEntity entity = ModelUtil.createEntity(Arrays.asList(ModelUtil.createImageRepresentation(URI
                 .create("http://example.com/void"), null)));
         // post an entity without identifiers
         HttpPost post = UTIL.createPostEntity(entity);
         CLIENT.execute(post);
         post.releaseConnection();
 
         // fetch the entity to learn the generated idenifiers
         HttpGet get = UTIL.createGetEntity(entity.getIdentifier().getValue());
         HttpResponse resp = CLIENT.execute(get);
         IntellectualEntity fetched = ScapeMarshaller.newInstance().deserialize(IntellectualEntity.class, resp.getEntity().getContent());
         get.releaseConnection();
 
         // and try to fetch and validate the fecthed entity's metadata
         get = UTIL.createGetMetadata(fetched.getIdentifier().getValue() + "-DESCRIPTIVE");
         resp = CLIENT.execute(get);
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         IOUtils.copy(resp.getEntity().getContent(), bos);
         get.releaseConnection();
         ElementContainer dc = (ElementContainer) ScapeMarshaller.newInstance().deserialize(new ByteArrayInputStream(bos.toByteArray()));
         assertEquals(entity.getDescriptive(), dc);
     }
 
     @Test
     public void testRetrieveRepresentation() throws Exception {
         Representation rep = ModelUtil.createTestRepresentation("test-representation-" + System.currentTimeMillis());
 
         // ingest the entity with it's representation for later fetching
         IntellectualEntity.Builder ie = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .representations(Arrays.asList(rep))
                 .descriptive(createDCElementContainer());
         HttpPost post = UTIL.createPostEntity(ie.build());
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         // and fetch the ingested representation
         HttpGet get = UTIL.createGetRepresentation(rep.getIdentifier().getValue());
         resp = CLIENT.execute(get);
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         IntellectualEntity entity = ScapeMarshaller.newInstance().deserialize(IntellectualEntity.class, resp.getEntity().getContent());
         get.releaseConnection();
 //        assertEquals(rep, entity.getRepresentations().get(0));
         Representation getRep = entity.getRepresentations().get(0);
         assertTrue("Representation identifier does not match", rep.getIdentifier().getValue().equals(getRep.getIdentifier().getValue()));
 		
     }
 
     @Test
     public void testSearchEntity() throws Exception {
         // ingest an entity to search in
         IntellectualEntity.Builder ie = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .descriptive(createDCElementContainer());
         HttpPost post = UTIL.createPostEntity(ie.build());
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         // and search for the ingested entity
         HttpGet get = UTIL.createGetSRUEntity("should");
         resp = CLIENT.execute(get);
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         IntellectualEntityCollection resultSet = (IntellectualEntityCollection) ScapeMarshaller.newInstance().deserialize(resp.getEntity().getContent());
         get.releaseConnection();
         //assertTrue(resultSet.getEntities().size() == 1);
         DefaultConverter conv = new DefaultConverter();       
 
        //IntellectualEntity searched = conv.convertMets(resultSet.getEntities().get(0));
         MetsType mets = (MetsType) ScapeMarshaller.newInstance().deserialize(resp.getEntity().getContent());
         IntellectualEntity searched = conv.convertMets(mets);
         assertEquals(ie.lifecycleState(new LifecycleState("", State.INGESTED)).build(), searched);
     }
 
     @Test
     public void testSearchRepresentation() throws Exception {
         // ingest an entity to search in
         IntellectualEntity.Builder ie = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .representations(Arrays.asList(ModelUtil.createTestRepresentation("testingestrepresentation-" + System.currentTimeMillis())))
                 .descriptive(createDCElementContainer());
         HttpPost post = UTIL.createPostEntity(ie.build());
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         // and search for the ingested entity
         HttpGet get = UTIL.createGetSRUrepresentation("testingestrepresentation");
         resp = CLIENT.execute(get);
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
         IntellectualEntityCollection resultSet = (IntellectualEntityCollection) ScapeMarshaller.newInstance().deserialize(resp.getEntity().getContent());
         get.releaseConnection();
         assertTrue(resultSet.getEntities().size() == 1);
         DefaultConverter conv = new DefaultConverter();
         
         //IntellectualEntity searched = conv.convertMets(resultSet.getEntities().get(0));
         MetsType mets = (MetsType) ScapeMarshaller.newInstance().deserialize(resp.getEntity().getContent());
         IntellectualEntity searched = conv.convertMets(mets);
         assertEquals(ie.lifecycleState(new LifecycleState("", State.INGESTED)).build(), searched);
     }
 
     @Test
     public void testUpdateIntellectualEntity() throws Exception {
         IntellectualEntity version1 = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .descriptive(createDCElementContainer())
                 .build();
         HttpPost post = UTIL.createPostEntity(version1);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
         IntellectualEntity version2 = new IntellectualEntity.Builder(version1)
                 .build();
         HttpPut put = UTIL.createPutEntity(version2);
         resp = CLIENT.execute(put);
         put.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
     }
 
     @Test
     public void testUpdateIntellectualRepresentation() throws Exception {
         Representation oldRep = ModelUtil.createTestRepresentation("ye olde representation");
         // create an intellectual entity with a representation
         IntellectualEntity oldVersion = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .representations(Arrays.asList(oldRep))
                 .descriptive(createDCElementContainer())
                 .build();
         // post it for persisting to the Mock
         HttpPost post = UTIL.createPostEntity(oldVersion);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         // create an an updated representation send a post to persist the
         // changes
         Representation newRep = new Representation.Builder(oldRep)
                 .title("The Brand-New Representation")
                 .build();
 
         HttpPut put = UTIL.createPutRepresentation(newRep);
         resp = CLIENT.execute(put);
         put.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
 
         // get the update entity and check that the title has changed
         HttpGet get = UTIL.createGetEntity(oldVersion.getIdentifier().getValue());
         resp = CLIENT.execute(get);
         IntellectualEntity newVersion = ScapeMarshaller.newInstance().deserialize(IntellectualEntity.class, resp.getEntity().getContent());
         assertEquals(newRep, newVersion.getRepresentations().get(0));
         get.releaseConnection();
     }
 
     @Test
     public void testUpdateMetadata() throws Exception {
         ElementContainer dc = createDCElementContainer();
         IntellectualEntity oldVersion = new IntellectualEntity.Builder()
                 .identifier(new Identifier(UUID.randomUUID().toString()))
                 .descriptive(dc)
                 .build();
         // post it for persisting to the Mock
         log.debug(" || sending metadata record " + oldVersion.getIdentifier().getValue() + "-DESCRIPTIVE");
         HttpPost post = UTIL.createPostEntity(oldVersion);
         HttpResponse resp = CLIENT.execute(post);
         post.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 201);
 
         // update the metadata in order to check the PUT method
         ElementContainer dcUpdated = createDCElementContainer();
         SimpleLiteral title = new SimpleLiteral();
         title.getContent().add("The Brand-New DC record");
         dcUpdated.getAny().add(new JAXBElement<SimpleLiteral>(new QName("http://purl.org/dc/elements/1.1/", "title"), SimpleLiteral.class, title));
         
         
         HttpPut put = UTIL.createPutMetadata(oldVersion.getIdentifier().getValue() + "-DESCRIPTIVE",dcUpdated);
         resp = CLIENT.execute(put);
         put.releaseConnection();
         assertTrue(resp.getStatusLine().getStatusCode() == 200);
 
         // fetch the entity and check for the updated metadata record
         HttpGet get = UTIL.createGetEntity(oldVersion.getIdentifier().getValue());
         resp = CLIENT.execute(get);
         IntellectualEntity newVersion = ScapeMarshaller.newInstance().deserialize(IntellectualEntity.class, resp.getEntity().getContent());
         get.releaseConnection();
         
         ElementContainer newEc = (ElementContainer) newVersion.getDescriptive();
         assertTrue("DC metadata does not match",
         newEc.getAny().get(0).getValue().getContent().equals(dcUpdated.getAny().get(0).getValue().getContent()));
 //        assertEquals(newVersion.getDescriptive(), dcUpdated);
 
     }
 
 }
