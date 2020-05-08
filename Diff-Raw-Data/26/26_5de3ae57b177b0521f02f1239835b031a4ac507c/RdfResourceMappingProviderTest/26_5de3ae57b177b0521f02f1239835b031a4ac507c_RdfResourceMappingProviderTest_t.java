 package uk.ac.bristol.dundry.webresources.providers;
 
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
 import com.hp.hpl.jena.util.FileManager;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 import javax.ws.rs.core.MediaType;
 import static org.junit.Assert.*;
 import org.junit.Ignore;
 import org.junit.Test;
 import uk.ac.bristol.dundry.model.ResourceCollection;
 
 /**
  *
  * @author Damian Steer <d.steer@bris.ac.uk>
  */
 public class RdfResourceMappingProviderTest {
     
     private RdfResourceMappingProvider get(String... confA) {
         Map<String, String> conf = new HashMap<>();
         
         for (int i = 0; i < confA.length; i+= 2) conf.put(confA[i], confA[i+1]);
         
         return new RdfResourceMappingProvider(conf);
     }
     
     private Resource makeResource(String uri) {
         Model m = ModelFactory.createDefaultModel();
         return m.createResource(uri);
     }
     
     /**
      * Test of map method, of class RdfResourceMappingProvider.
      */
     @Test
     public void testMap() throws Exception {
     }
 
     /**
      * Test of isWriteable method, of class RdfResourceMappingProvider.
      */
     @Test
     public void testIsWriteable() {
         RdfResourceMappingProvider i = get();
         assertTrue("Can write resources", i.isWriteable(Resource.class, null, null, null));
         assertTrue("Can write resources impls", i.isWriteable(ResourceImpl.class, null, null, null));
         assertFalse("Can write literals", i.isWriteable(Literal.class, null, null, null));
         
         ResourceCollection t = new ResourceCollection(Collections.EMPTY_LIST);
         assertTrue("Can write list of resources", i.isWriteable(t.getClass(), t.getClass(), null, null));
     }
 
     /**
      * Test of getSize method, of class RdfResourceMappingProvider.
      */
     @Test
     public void testGetSize() {
         RdfResourceMappingProvider i = get();
         assertEquals("Size is unknown", -1, i.getSize(null, null, null, null, null));
     }
 
     /**
      * Test of writeTo method, of class RdfResourceMappingProvider.
      */
     @Test
     public void testWriteTo() throws Exception {
         RdfResourceMappingProvider i = get();
         
         Resource r = makeResource("http://example.com/r");
         r.addLiteral(DCTerms.title, "the title");
         
        assertEquals("{\"item\":{\"id\":\"http:\\/\\/example.com\\/r\",\"ignore\":\"\"}}", map(i, r, "application/json"));
         
         i = get("title", DCTerms.title.getURI());
         
         assertEquals("{\"item\":{\"id\":\"http:\\/\\/example.com\\/r\",\"title\":\"the title\"}}", map(i, r, "application/json"));
     }
     
     @Test
     public void testWritingMultipleItems() throws IOException {
         RdfResourceMappingProvider i = get(
                 "source", "http://purl.org/dc/terms/source",
                 "title", "http://purl.org/dc/terms/title");
         
         Resource r = makeResource("http://example.com/r");
         r.addLiteral(DCTerms.title, "title 1");
         r.addLiteral(DCTerms.title, "title 2");
         
         // ALERT: this relies on the order of the titles, which may change
         assertEquals("Repeated title",
                 "{\"item\":{\"id\":\"http:\\/\\/example.com\\/r\",\"title\":[\"title 2\",\"title 1\"]}}",
                 map(i, r, "application/json")
                 );
         
         r = makeResource("http://example.com/r");
         r.addProperty(DCTerms.source, makeResource("http://example.com/a"));
         r.addProperty(DCTerms.source, makeResource("http://example.com/b"));
         
         assertEquals("Repeated source",
                "{\"item\":{\"id\":\"http:\\/\\/example.com\\/r\",\"source\":[{\"id\":\"http:\\/\\/example.com\\/b\",\"ignore\":\"\"},{\"id\":\"http:\\/\\/example.com\\/a\",\"ignore\":\"\"}]}}",
                 map(i, r, "application/json")
                 );
     }
     
     @Test
     public void testWritingCollection() throws IOException {
         RdfResourceMappingProvider i = get(
                 "source", "http://purl.org/dc/terms/source",
                 "title", "http://purl.org/dc/terms/title");
         
         List<Resource> rs = new ArrayList<>();
         
         Resource r = makeResource("http://example.com/a");
         r.addLiteral(DCTerms.title, "title 1");
         
         rs.add(r);
         
         r = makeResource("http://example.com/b");
         r.addLiteral(DCTerms.title, "title 2");
         
         rs.add(r);
         
         // ALERT: this relies on the order of the resources, which may change
         assertEquals("Repeated resource",
                 "{\"item\":[{\"id\":\"http:\\/\\/example.com\\/a\",\"title\":\"title 1\"},{\"id\":\"http:\\/\\/example.com\\/b\",\"title\":\"title 2\"}]}",
                 map(i, new ResourceCollection(rs), "application/json")
                 );
     }
     
     @Test
     public void testWritingEmptyCollection() throws IOException {
         RdfResourceMappingProvider i = get();
         assertEquals("Empty resource list",
                 "{\"item\":[]}",
                 map(i, new ResourceCollection(Collections.EMPTY_LIST), "application/json")
                 );
     }
     
     @Test
     public void testWritingSingletonCollection() throws IOException {
         RdfResourceMappingProvider i = get();
         Resource r = makeResource("http://example.com/a");
         r.addLiteral(RDFS.label, "label");
         assertEquals("Singleton resource list includes array",
                 "{\"item\":[{\"id\":\"http:\\/\\/example.com\\/a\",\"label\":\"label\"}]}",
                 map(i, new ResourceCollection(Collections.singleton(r)), "application/json")
                 );
     }
     
     // Map object r, and return result as a string
     private String map(RdfResourceMappingProvider i, Object r, String mType) throws IOException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         i.writeTo(r, null, null, null, MediaType.valueOf(mType), null, out);
         return new String(out.toByteArray(), "UTF-8");
     }
     
     @Test
     public void testReadFrom() throws Exception {
         RdfResourceMappingProvider i = get(
                 "contributor", "http://purl.org/dc/terms/contributor",
                 "source", "http://purl.org/dc/terms/source",
                 "title", "http://purl.org/dc/terms/title",
                 "description", "http://purl.org/dc/terms/description"
                 );
         InputStream in = this.getClass().getResourceAsStream("/ex1.json");
         Resource r = i.readFrom(Resource.class, null, null, MediaType.valueOf("application/json"), null, in);
         Model expected = FileManager.get().loadModel("ex1.ttl");
                 
         assertTrue("ex1 json correct", expected.isIsomorphicWith(r.getModel()));
         
         in = this.getClass().getResourceAsStream("/ex2.json");
         r = i.readFrom(Resource.class, null, null, MediaType.valueOf("application/json"), null, in);
         expected = FileManager.get().loadModel("ex2.ttl");
         assertTrue("ex2 (missing desc) json correct", expected.isIsomorphicWith(r.getModel()));
         
         in = this.getClass().getResourceAsStream("/ex3.json");
         r = i.readFrom(Resource.class, null, null, MediaType.valueOf("application/json"), null, in);
         expected = FileManager.get().loadModel("ex3.ttl");
         assertTrue("ex3 (empty id) json correct", expected.isIsomorphicWith(r.getModel()));
     }
     
     @Ignore // I'd like this to not explode :-(
     @Test
     public void testReadRegression() throws Exception {
         RdfResourceMappingProvider i = get(
                 "contributor", "http://purl.org/dc/terms/contributor",
                 "source", "http://purl.org/dc/terms/source",
                 "title", "http://purl.org/dc/terms/title",
                 "description", "http://purl.org/dc/terms/description"
                 );
         
         InputStream in = this.getClass().getResourceAsStream("/boom.json");
         Resource r = i.readFrom(Resource.class, null, null, MediaType.valueOf("application/json"), null, in);
         assertNotNull(r);
     }
     
     @Test
     public void testVocabInit() throws Exception {
         RdfResourceMappingProvider i = new RdfResourceMappingProvider(
                 Arrays.asList("vocabs/dcterms.rdf"),
                 Collections.EMPTY_LIST
                 );
         
         InputStream in = this.getClass().getResourceAsStream("/ex1.json");
         Resource r = i.readFrom(Resource.class, null, null, MediaType.valueOf("application/json"), null, in);
         Model expected = FileManager.get().loadModel("ex1.ttl");
                 
         assertTrue("ex1 json correct", expected.isIsomorphicWith(r.getModel()));
         
         in = this.getClass().getResourceAsStream("/ex2.json");
         r = i.readFrom(Resource.class, null, null, MediaType.valueOf("application/json"), null, in);
         expected = FileManager.get().loadModel("ex2.ttl");
         assertTrue("ex2 (missing desc) json correct", expected.isIsomorphicWith(r.getModel()));
         
         in = this.getClass().getResourceAsStream("/ex3.json");
         r = i.readFrom(Resource.class, null, null, MediaType.valueOf("application/json"), null, in);
         expected = FileManager.get().loadModel("ex3.ttl");
         assertTrue("ex3 (empty id) json correct", expected.isIsomorphicWith(r.getModel()));
     }
 }
