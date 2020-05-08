 package pl.psnc.dl.wf4ever.resources;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletResponse;
 
 import junit.framework.Assert;
 
 import org.apache.http.HttpStatus;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
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
         Set<URI> expected = new HashSet<>(Arrays.asList(URI.create("https://sandbox/rodl/ROs/ro1/myfolder/file1.txt"),
             URI.create("https://sandbox/rodl/ROs/ro1/anotherfolder/file2.txt"), URI.create("http://example.org")));
         assertCorrectFolderResourceMap(response.getEntityInputStream(), expected);
         URI folderProxyURI = response.getLocation();
         Assert.assertNotNull(folderProxyURI);
         response.close();
         webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
 
         is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
         response = addFolder(is, ro, folderWithSpacesPath, accessToken);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         assertCorrectFolderResourceMap(response.getEntityInputStream(), expected);
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
         Set<URI> expected = new HashSet<>(Arrays.asList(URI.create("https://sandbox/rodl/ROs/ro1/myfolder/file1.txt"),
             URI.create("https://sandbox/rodl/ROs/ro1/anotherfolder/file2.txt"), URI.create("http://example.org")));
         assertCorrectFolderResourceMap(response.getEntityInputStream(), expected);
 
         webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
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
         Set<URI> expected = new HashSet<>(Arrays.asList(URI.create("https://sandbox/rodl/ROs/ro1/myfolder/file1.txt"),
             URI.create("https://sandbox/rodl/ROs/ro1/anotherfolder/file2.txt"), URI.create("http://example.org"),
             URI.create("https://sandbox/rodl/ROs/ro1/file2.txt")));
         assertCorrectFolderResourceMap(response.getEntityInputStream(), expected);
         response.close();
 
         webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
     }
 
 
     /**
      * Test for deleting a folder entry.
      */
     @Test
     public void testDeleteFolderEntry() {
         addFile(ro, filePath, accessToken);
 
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
         ClientResponse response = addFolder(is, ro, folderPath, accessToken);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         URI folderProxyURI = response.getLocation();
         Assert.assertNotNull(folderProxyURI);
         response.close();
 
         response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                 .get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_OK, response.getStatus());
         URI folderEntry = getFolderEntryURI(response.getEntityInputStream(),
             URI.create("https://sandbox/rodl/ROs/ro1/anotherfolder/file2.txt"));
         response.close();
 
         response = webResource.uri(folderEntry).header("Authorization", "Bearer " + accessToken)
                 .delete(ClientResponse.class);
         assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
         response.close();
 
         response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                 .get(ClientResponse.class);
         Set<URI> expected = new HashSet<>(Arrays.asList(URI.create("https://sandbox/rodl/ROs/ro1/myfolder/file1.txt"),
             URI.create("http://example.org")));
         assertCorrectFolderResourceMap(response.getEntityInputStream(), expected);
         response.close();
 
         webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
     }
 
 
     /**
      * Test for deleting a folder.
      */
     @Test
     public void testDeleteFolder() {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/folder.rdf");
         ClientResponse response = addFolder(is, ro, folderPath, accessToken);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         URI folderProxyURI = response.getLocation();
         Assert.assertNotNull(folderProxyURI);
         response.close();
 
         response.getClient().setFollowRedirects(false);
         response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                 .get(ClientResponse.class);
         response = webResource.uri(response.getLocation()).header("Authorization", "Bearer " + accessToken)
                 .get(ClientResponse.class);
         URI resourceMap = response.getLocation();
         response.getClient().setFollowRedirects(true);
         response = webResource.uri(resourceMap).header("Authorization", "Bearer " + accessToken)
                 .get(ClientResponse.class);
         assertEquals(HttpStatus.SC_OK, response.getStatus());
 
         response = webResource.uri(folderProxyURI).header("Authorization", "Bearer " + accessToken)
                 .delete(ClientResponse.class);
         assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
 
         response = webResource.uri(resourceMap).header("Authorization", "Bearer " + accessToken)
                 .get(ClientResponse.class);
         assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());
     }
 
 
     /**
      * Check if folder resource map is correct.
      * 
      * @param entityInputStream
      *            input stream with the RDF/XML resource map
      * @param proxyFors
      *            list of aggregated resources
      */
     private void assertCorrectFolderResourceMap(InputStream entityInputStream, Set<URI> proxyFors) {
        //        try {
        //            System.out.println(IOUtils.toString(entityInputStream));
        //        } catch (IOException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(entityInputStream, null);
 
         List<Individual> folders = model.listIndividuals(RO.Folder).toList();
         Assert.assertEquals(1, folders.size());
         List<RDFNode> folderEntries = folders.get(0).listPropertyValues(ORE.aggregates).toList();
         Assert.assertEquals(proxyFors.size(), folderEntries.size());
 
         List<Individual> entries = model.listIndividuals(RO.FolderEntry).toList();
         Assert.assertEquals(proxyFors.size(), entries.size());
         Set<URI> entryProxyFors = new HashSet<>();
         for (Individual entry : entries) {
             Assert.assertTrue(entry.hasProperty(RO.entryName));
             Assert.assertTrue(entry.hasProperty(ORE.proxyFor));
             Assert.assertTrue(entry.hasProperty(ORE.proxyIn));
             entryProxyFors.add(URI.create(entry.getPropertyResourceValue(ORE.proxyFor).getURI()));
         }
         Assert.assertEquals(proxyFors, entryProxyFors);
     }
 
 
     /**
      * Find a folder entry URI given a resource.
      * 
      * @param entityInputStream
      *            input stream with the RDF/XML resource map
      * @param proxyFor
      *            the URI of the aggregated resource
      */
     private URI getFolderEntryURI(InputStream entityInputStream, URI proxyFor) {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(entityInputStream, null);
 
         Resource proxyForR = model.createResource(proxyFor.toString());
         List<Resource> entries = model.listSubjectsWithProperty(ORE.proxyFor, proxyForR).toList();
         Assert.assertEquals(1, entries.size());
         return URI.create(entries.get(0).getURI());
     }
 }
