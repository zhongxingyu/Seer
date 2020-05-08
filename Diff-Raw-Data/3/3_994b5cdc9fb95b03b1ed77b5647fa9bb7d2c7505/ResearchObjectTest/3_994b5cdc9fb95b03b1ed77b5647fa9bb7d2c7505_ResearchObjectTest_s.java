 package org.purl.wf4ever.rosrs.client;
 
 import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
 import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
 import static com.github.tomakehurst.wiremock.client.WireMock.matching;
 import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
 import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
 import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
 import static com.github.tomakehurst.wiremock.client.WireMock.verify;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.anyOf;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.hasProperty;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.hamcrest.Matchers;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.Assert;
 import org.junit.Rule;
 import org.junit.Test;
 import org.purl.wf4ever.rosrs.client.evo.EvoType;
 import org.purl.wf4ever.rosrs.client.exception.ROException;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import com.github.tomakehurst.wiremock.junit.WireMockRule;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 /**
  * Test the ResearchObject class.
  * 
  * @author piotrekhol
  * 
  */
 public class ResearchObjectTest extends BaseTest {
 
     /** A test HTTP mock server. */
     @Rule
     public static final WireMockRule WIREMOCK_RULE = new WireMockRule(8089); // No-args constructor defaults to port 8080
 
    /** A loaded RO. */
    private static ResearchObject ro1;

 
     /**
      * Test that an initial RO is not loaded.
      */
     @Test
     public final void testResearchObject() {
         ResearchObject ro = new ResearchObject(MOCK_RO, null);
         Assert.assertFalse(ro.isLoaded());
     }
 
 
     /**
      * Test that you can create and delete an RO in ROSRS.
      * 
      * @throws ROSRSException
      *             ROSRS returned an unexpected response.
      */
     @Test
     public final void testCreateDelete()
             throws ROSRSException {
 
         ResearchObject ro = ResearchObject.create(rosrs, "JavaClientTest");
         verify(postRequestedFor(urlMatching("/")).withHeader("Slug", equalTo("JavaClientTest")).withHeader("Accept",
             equalTo("application/rdf+xml")));
 
         ro.delete();
         verify(deleteRequestedFor(urlMatching("/ro1/")));
     }
 
 
     /**
      * Test the example RO URI.
      */
     @Test
     public final void testGetUri() {
         Assert.assertEquals(MOCK_RO, ro1.getUri());
     }
 
 
     /**
      * Test the ROSRS client is saved.
      */
     @Test
     public final void testGetRosrs() {
         Assert.assertNotNull(ro1.getRosrs());
         Assert.assertEquals(URI.create("http://localhost:8089/"), ro1.getRosrs().getRosrsURI());
     }
 
 
     /**
      * Test that the example RO says it's loaded.
      */
     @Test
     public final void testIsLoaded() {
         Assert.assertTrue(ro1.isLoaded());
     }
 
 
     /**
      * Test that an RO can be loaded over HTTP.
      * 
      * @throws ROSRSException
      *             connection problem
      * @throws ROException
      *             remote manifest is incorrect
      */
     @Test
     public final void testLoad()
             throws ROSRSException, ROException {
         ResearchObject ro = new ResearchObject(MOCK_RO, rosrs);
         Assert.assertFalse(ro.isLoaded());
         ro.load();
         Assert.assertTrue(ro.isLoaded());
     }
 
 
     /**
      * Test ro:Resources identified.
      */
     @Test
     public final void testGetResources() {
         Set<Resource> ex = new HashSet<>();
         ex.add(new Resource(ro1, MOCK_RO.resolve("res1.txt"), MOCK_RO.resolve("proxies/1"), PERSON_1, new DateTime(
                 2011, 12, 02, 15, 02, 10, DateTimeZone.UTC)));
         ex.add(new Resource(ro1, MOCK_RO.resolve("res2"), MOCK_RO.resolve("proxies/2"), PERSON_2, new DateTime(2011,
                 12, 02, 15, 02, 11, DateTimeZone.UTC)));
         ex.add(new Resource(ro1, MOCK_RO.resolve("res3"), MOCK_RO.resolve("proxies/5"), PERSON_2, new DateTime(2011,
                 12, 02, 15, 02, 11, DateTimeZone.UTC)));
         Set<Resource> res = new HashSet<>();
         res.addAll(ro1.getResources().values());
         Assert.assertEquals(ex, res);
     }
 
 
     /**
      * Test ro:Folders identified.
      * 
      * @throws ROSRSException
      *             unexpected response from the server
      */
     @Test
     public final void testGetResourcesWithoutFolders()
             throws ROSRSException {
         List<Resource> ex = new ArrayList<>();
         ex.add(new Resource(ro1, MOCK_RO.resolve("res3"), MOCK_RO.resolve("proxies/5"), PERSON_2, new DateTime(2011,
                 12, 02, 15, 02, 11, DateTimeZone.UTC)));
         List<Resource> res = ro1.getResourcesWithoutFolders();
         Assert.assertEquals(ex, res);
     }
 
 
     /**
      * Test ro:Folders identified.
      */
     @Test
     public final void testGetFolders() {
         Set<Folder> folders = new HashSet<>();
         folders.add(new Folder(ro1, MOCK_RO.resolve("folder1/"), MOCK_RO.resolve("proxies/3"), MOCK_RO
                 .resolve("folder1.rdf"), PERSON_3, new DateTime(2011, 12, 02, 15, 02, 12, DateTimeZone.UTC), true));
         folders.add(new Folder(ro1, MOCK_RO.resolve("folder1/folder2/"), MOCK_RO.resolve("proxies/4"), MOCK_RO
                 .resolve("folder2.rdf"), PERSON_3, new DateTime(2011, 12, 02, 15, 02, 12, DateTimeZone.UTC), false));
         Set<Folder> res = new HashSet<>();
         res.addAll(ro1.getFolders().values());
         Assert.assertEquals(folders, res);
     }
 
 
     /**
      * Test ro:AggregatedAnnotations identified.
      */
     @Test
     public final void testGetAnnotations() {
         Multimap<URI, Annotation> ex = HashMultimap.<URI, Annotation> create();
         Set<URI> targets = new HashSet<>();
         targets.add(MOCK_RO);
         targets.add(MOCK_RESOURCE);
         Annotation an1 = new Annotation(ro1, MOCK_RO.resolve(".ro/annotations/1"), MOCK_RO.resolve("body.rdf"),
                 targets, PERSON, new DateTime(2012, 12, 11, 12, 06, 53, 551, DateTimeZone.UTC));
         Annotation an2 = new Annotation(ro1, MOCK_RO.resolve(".ro/annotations/2"),
                 URI.create("http://example.org/externalbody1.rdf"), Collections.singleton(MOCK_RO.resolve("res1.txt")),
                 PERSON, new DateTime(2012, 12, 11, 12, 06, 53, 551, DateTimeZone.UTC));
         Set<URI> targets2 = new HashSet<>();
         targets2.add(MOCK_RO.resolve("folder1/"));
         targets2.add(MOCK_RO.resolve("res2"));
         Annotation an3 = new Annotation(ro1, MOCK_RO.resolve(".ro/annotations/3"), MOCK_RO.resolve("body2.rdf"),
                 targets2, PERSON, new DateTime(2012, 12, 11, 12, 06, 53, 551, DateTimeZone.UTC));
         Annotation an4 = new Annotation(ro1, MOCK_RO.resolve(".ro/annotations/4"), MOCK_RO.resolve("body3.rdf"),
                 Collections.singleton(MOCK_RO.resolve("folder1/")), PERSON, new DateTime(2012, 12, 11, 12, 06, 53, 551,
                         DateTimeZone.UTC));
         ex.put(MOCK_RO, an1);
         ex.put(MOCK_RO.resolve("res1.txt"), an2);
         ex.put(MOCK_RO.resolve("res1.txt"), an1);
         ex.put(MOCK_RO.resolve("folder1/"), an3);
         ex.put(MOCK_RO.resolve("res2"), an3);
         ex.put(MOCK_RO.resolve("folder1/"), an4);
 
         Assert.assertEquals(ex, ro1.getAllAnnotations());
     }
 
 
     /**
      * Test dcterms:creator.
      */
     @Test
     public final void testAuthor() {
         Assert.assertEquals(PERSON, ro1.getAuthor());
     }
 
 
     /**
      * Test RO dcterms:created.
      */
     @Test
     public final void testGetCreated() {
         //2011-12-02T16:01:10Z
         Assert.assertEquals(new DateTime(2011, 12, 02, 16, 01, 10, DateTimeZone.UTC), ro1.getCreated());
     }
 
 
     /**
      * Test RO evo class taken from an annotation.
      */
     @Test
     public final void testGetEvoType() {
         Assert.assertEquals(EvoType.SNAPSHOT, ro1.getEvoType());
     }
 
 
     /**
      * See name.
      * 
      * @throws ROSRSException
      *             wiremock error
      */
     @SuppressWarnings("unchecked")
     @Test
     public final void shouldReturnTwoCommentsJoined()
             throws ROSRSException {
         List<?> list = ro1.getPropertyValues(RDFS_COMMENT, true);
         assertThat(list, hasSize(equalTo(1)));
         assertThat(
             (List<Object>) list,
             hasItem(anyOf(hasProperty("value", Matchers.equalTo("RO comment 1; RO comment 2")),
                 hasProperty("value", Matchers.equalTo("RO comment 2; RO comment 1")))));
     }
 
 
     /**
      * See name.
      * 
      * @throws ROSRSException
      *             wiremock error
      */
     @SuppressWarnings("unchecked")
     @Test
     public final void shouldReturnAllTriples()
             throws ROSRSException {
         List<Object> quads = (List<Object>) ((List<?>) ro1.getAnnotationTriples());
         assertThat(quads, hasSize(5));
         // I've had problems using 'contains' so this test doesn't test the order
         URI desc = URI.create(DCTerms.description.getURI());
         URI title = URI.create(DCTerms.title.getURI());
         URI type = URI.create(RDF.type.getURI());
         URI comment = URI.create(RDFS.comment.getURI());
         assertThat(quads, hasItem(allOf(hasProperty("property", is(desc)), hasProperty("value", is("This RO rocks.")))));
         assertThat(quads,
             hasItem(allOf(hasProperty("property", is(title)), hasProperty("value", is("The rocking RO")))));
         assertThat(
             quads,
             hasItem(allOf(hasProperty("property", is(type)),
                 hasProperty("value", is("http://purl.org/wf4ever/roevo#SnapshotRO")))));
         assertThat(quads,
             hasItem(allOf(hasProperty("property", is(comment)), hasProperty("value", is("RO comment 1")))));
         assertThat(quads,
             hasItem(allOf(hasProperty("property", is(comment)), hasProperty("value", is("RO comment 2")))));
     }
 
 
     /**
      * See name.
      * 
      * @throws ROSRSException
      *             wiremock error
      * @throws ROException
      *             incorrect manifest
      */
     @Test
     public final void shouldCreateAComment()
             throws ROSRSException, ROException {
         List<AnnotationTriple> list = ro1.getPropertyValues(RDFS_COMMENT, true);
         assertThat(list, hasSize(equalTo(1)));
         AnnotationTriple triple = ro1.createPropertyValue(RDFS_COMMENT, "RO comment 3");
         assertThat(triple, notNullValue());
 
         verify(postRequestedFor(urlEqualTo("/ro1/")).withRequestBody(matching(".*RO comment 3.*")));
     }
 }
