 package pl.psnc.dl.wf4ever.resources;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.UUID;
 
 import javax.naming.NamingException;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 import pl.psnc.dl.wf4ever.util.SafeURI;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.tdb.TDBFactory;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 
 public class ResourceTest extends ResourceBase {
 
     protected String createdFromZipResourceObject = UUID.randomUUID().toString();
 
 
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
 
 
     @Test
     public void testGetROList() {
         String list = webResource.path("ROs").header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue(list.contains(ro.toString()));
         assertTrue(list.contains(ro2.toString()));
     }
 
 
     @Test
     public void testGetROWithWhitespaces() {
         URI ro3 = createRO("ro " + UUID.randomUUID().toString(), accessToken);
         String list = webResource.path("ROs").header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue(list.contains(ro3.toString()));
     }
 
 
     @Test
     public void testGetROMetadata()
             throws URISyntaxException {
         client().setFollowRedirects(false);
         ClientResponse response = webResource.uri(ro).accept("text/turtle").get(ClientResponse.class);
        assertTrue(response.getHeaders().get(Constants.LINK_HEADER)
                .contains(webResource.path("/evo/info").queryParam("ro", ro.toString()).toString()));
 
         assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
         assertEquals(webResource.uri(ro).path(".ro/manifest.rdf").getURI().getPath(), response.getLocation().getPath());
         response.close();
     }
 
 
     @Test
     public void testGetROHTML()
             throws URISyntaxException {
         client().setFollowRedirects(false);
         ClientResponse response = webResource.uri(ro).path("/").accept("text/html").get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
         URI portalURI = new URI("http", "sandbox.wf4ever-project.org", "/portal/ro", "ro="
                 + webResource.uri(ro).getURI().toString(), null);
         assertEquals(portalURI.getPath(), response.getLocation().getPath());
         assertTrue(portalURI.getQuery().contains("ro="));
         response.close();
     }
 
 
     @Test
     public void testGetROZip() {
         client().setFollowRedirects(false);
         ClientResponse response = webResource.uri(ro).accept("application/zip").get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
         assertEquals(webResource.path("zippedROs").path(ro.toString().split("ROs")[1]).getURI().getPath(), response
                 .getLocation().getPath());
         response.close();
 
         response = webResource.path("zippedROs").path(ro.toString().split("ROs")[1]).get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_OK, response.getStatus());
         assertEquals("application/zip", response.getType().toString());
         response.close();
 
         response = webResource.path("zippedROs").path(ro.toString().split("ROs")[1])
                 .accept("text/html;q=0.9,*/*;q=0.8").get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_OK, response.getStatus());
         assertEquals("application/zip", response.getType().toString());
         response.close();
     }
 
 
     /**
      * Ziped RO has three annotations. RO added to triple store should be this same.
      * 
      * @throws IOException
      * @throws ClassNotFoundException
      * @throws NamingException
      */
     @Test
     public void createROFromZip()
             throws IOException, ClassNotFoundException, NamingException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/ro1.zip");
         ClientResponse response = webResource.path("ROs").accept("text/turtle")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, is);
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response.getStatus());
         response.close();
     }
 
 
     @Test
     public void createROFromZipWithWhitespaces()
             throws IOException, IncorrectModelException, ClassNotFoundException, NamingException, SQLException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/white_spaces_ro.zip");
         ClientResponse response = webResource.path("ROs/").accept("text/turtle")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, is);
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response.getStatus());
         response.close();
     }
 
 
     @Test
     public void createROFromZipWithEvoAnnotation()
             throws IOException, IncorrectModelException, ClassNotFoundException, NamingException, SQLException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/zip_with_evo.zip");
         ClientResponse response = webResource.path("ROs").accept("text/turtle")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, is);
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response.getStatus());
         response.close();
 
     }
 
 
     @Test
     public void createConflictedROFromZip()
             throws UniformInterfaceException, ClientHandlerException, IOException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/ro1.zip");
         ClientResponse response = webResource.path("ROs").accept("text/turtle")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, IOUtils.toByteArray(is));
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response.getStatus());
         is = getClass().getClassLoader().getResourceAsStream("singleFiles/ro1.zip");
         response = webResource.path("ROs").accept("text/turtle").header("Authorization", "Bearer " + accessToken)
                 .header("Slug", createdFromZipResourceObject).type("application/zip")
                 .post(ClientResponse.class, IOUtils.toByteArray(is));
         assertEquals("Research objects with this same name should be conflicted", HttpServletResponse.SC_CONFLICT,
             response.getStatus());
 
     }
 
 
     @Test
     public void updateEvoInfo() {
         InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
         ClientResponse response = webResource.uri(ro).path("/.ro/evo_info.ttl")
                 .header("Authorization", "Bearer " + accessToken).type("text/turtle").put(ClientResponse.class, is);
         assertEquals("Updating evo_info should be protected", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
         response.close();
         Builder builder = new Builder(null, TDBFactory.createDataset(), false);
         ResearchObject researchObject = builder.buildResearchObject(ro);
         OntModel manifestModel = ModelFactory.createOntologyModel();
         manifestModel.read(researchObject.getManifestUri().toString());
 
         Resource bodyR = manifestModel.createResource(SafeURI.URItoString(researchObject
                 .getFixedEvolutionAnnotationBodyUri()));
         List<Statement> anns = manifestModel.listStatements(null, AO.body, bodyR).toList();
         assertTrue("Cannot find annotation", !anns.isEmpty());
         URI annUri = URI.create(anns.get(0).getSubject().getURI());
         response = webResource.uri(annUri).header("Authorization", "Bearer " + accessToken)
                 .delete(ClientResponse.class);
         response.close();
 
         assertEquals("Removing evo info should be protected", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
         response = webResource.uri(researchObject.getFixedEvolutionAnnotationBodyUri())
                 .header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
         assertEquals("Removing evo info should be protected", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
         response.close();
     }
 }
