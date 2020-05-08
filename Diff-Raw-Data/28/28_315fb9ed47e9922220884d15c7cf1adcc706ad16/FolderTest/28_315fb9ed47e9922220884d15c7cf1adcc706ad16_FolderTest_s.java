 package pl.psnc.dl.wf4ever.resources;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * Tests of manipulating ro:Folders.
  * 
  * @author piotrekhol
  * 
  */
 
 public class FolderTest extends ResourceBase {
 
     /** folder path. */
     private final String folderPath = "myfolder/";
 
     /** folder path. */
     private final String folderWithSpacesPath = "my folder/";
 
     /** file path. */
     private final String filePath = "file2.txt";
 
 
     @Override
     public void setUp()
             throws Exception {
         super.setUp();
     }
 
 
     @Override
     public void tearDown()
             throws Exception {
         super.tearDown();
     }
 
 
     /**
      * Test for creating a new folder.
      */
     @Test
     public void testAddFolder() {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
         ClientResponse response = addFolder(is, ro, folderPath, accessToken);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         assertCorrectFolderResourceMap(response.getEntityInputStream());
         URI folderProxyURI = response.getLocation();
         Assert.assertNotNull(folderProxyURI);
         response.close();
         webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
 
        is = getClass().getClassLoader().getResourceAsStream("folder.rdf");
         response = addFolder(is, ro, folderWithSpacesPath, accessToken);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         assertCorrectFolderResourceMap(response.getEntityInputStream());
         folderProxyURI = response.getLocation();
         Assert.assertNotNull(folderProxyURI);
         response.close();
         webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
     }
 
 
     /**
      * Test for dereferencing a folder.
      */
     @Test
     public void testGetFolder() {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
         ClientResponse response = addFolder(is, ro, folderPath, accessToken);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         URI folderProxyURI = response.getLocation();
         Assert.assertNotNull(folderProxyURI);
         response.close();
 
         response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                 .get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_OK, response.getStatus());
         assertCorrectFolderResourceMap(response.getEntityInputStream());
 
         webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
     }
 
 
     /**
      * Check if folder resource map is correct.
      * 
      * @param entityInputStream
      *            input stream with the RDF/XML resource map
      */
     private void assertCorrectFolderResourceMap(InputStream entityInputStream) {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(entityInputStream, null);
 
         List<Individual> folders = model.listIndividuals(RO.Folder).toList();
         Assert.assertEquals(1, folders.size());
         List<RDFNode> folderEntries = folders.get(0).listPropertyValues(ORE.aggregates).toList();
         Assert.assertEquals(3, folderEntries.size());
 
         List<Individual> entries = model.listIndividuals(RO.FolderEntry).toList();
         Assert.assertEquals(3, entries.size());
         for (Individual entry : entries) {
             Assert.assertTrue(entry.hasProperty(RO.entryName));
             Assert.assertTrue(entry.hasProperty(ORE.proxyFor));
             Assert.assertTrue(entry.hasProperty(ORE.proxyIn));
         }
     }
 
 
     /**
      * Test for creating a new folder entry.
      */
     @Test
     public void testAddFolderEntry() {
         addFile(ro, filePath, accessToken);
 
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
         ClientResponse response = addFolder(is, ro, folderPath, accessToken);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         URI folderProxyURI = response.getLocation();
         Assert.assertNotNull(folderProxyURI);
         Multimap<String, URI> links = getLinkHeaders(response.getHeaders().get("Link"));
         response.close();
 
         URI folderURI = links.get("http://www.openarchives.org/ore/terms/proxyFor").iterator().next();
         is = getClass().getClassLoader().getResourceAsStream("singleFiles/folderEntry.rdf");
         response = addFolderEntry(is, folderURI, accessToken);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         links = getLinkHeaders(response.getHeaders().get("Link"));
         Assert.assertEquals(URI.create("https://sandbox/rodl/ROs/ro1/file2.txt"),
             links.get("http://www.openarchives.org/ore/terms/proxyFor").iterator().next());
         response.close();
 
         response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                 .get(ClientResponse.class);
         assertCorrectFolderWithEntryResourceMap(response.getEntityInputStream());
         response.close();
 
         webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
     }
 
 
     /**
      * Check if folder resource map is correct after adding a folder entry.
      * 
      * @param entityInputStream
      *            input stream with the RDF/XML resource map
      */
     private void assertCorrectFolderWithEntryResourceMap(InputStream entityInputStream) {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(entityInputStream, null);
 
         List<Individual> folders = model.listIndividuals(RO.Folder).toList();
         Assert.assertEquals(1, folders.size());
         List<RDFNode> folderEntries = folders.get(0).listPropertyValues(ORE.aggregates).toList();
         Assert.assertEquals(4, folderEntries.size());
 
         List<Individual> entries = model.listIndividuals(RO.FolderEntry).toList();
         Assert.assertEquals(4, entries.size());
         for (Individual entry : entries) {
             Assert.assertTrue(entry.hasProperty(RO.entryName));
             Assert.assertTrue(entry.hasProperty(ORE.proxyFor));
             Assert.assertTrue(entry.hasProperty(ORE.proxyIn));
         }
     }
 }
