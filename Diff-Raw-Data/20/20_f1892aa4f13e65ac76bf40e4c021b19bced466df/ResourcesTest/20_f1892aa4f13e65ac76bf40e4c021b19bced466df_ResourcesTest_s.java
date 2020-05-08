 /**
  * 
  */
 package pl.psnc.dl.wf4ever;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.http.HttpStatus;
 import org.junit.Test;
 import org.openrdf.rio.RDFFormat;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.test.framework.JerseyTest;
 import com.sun.jersey.test.framework.WebAppDescriptor;
 
 /**
  * @author piotrhol
  * 
  */
 public class ResourcesTest extends JerseyTest {
 
     private final String clientName = "ROSRS testing app written in Ruby";
 
     private final String clientRedirectionURI = "OOB"; // will not be used
 
     private String clientId;
 
     private final String adminCreds = StringUtils.trim(Base64.encodeBase64String("wfadmin:wfadmin!!!".getBytes()));
 
     private final String userId = UUID.randomUUID().toString();
 
     private final String userIdUrlSafe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId.getBytes()));
 
     private final String userId2 = UUID.randomUUID().toString();
 
     private final String userId2UrlSafe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId2.getBytes()));
 
     private WebResource webResource;
 
     private String accessToken;
 
     private String accessToken2;
 
     private final String r = "r";
 
     private final String r2 = "r 2";
 
     private final String r2Encoded = "r%202";
 
     private final String filePath = "foo/bar ra.txt";
 
     private final String filePathEncoded = "foo/bar%20ra.txt";
 
     private final String rdfFilePath = "foo/bar.rdf";
 
     private URI rdfProxy;
 
     private final String externalResource = "http://example.com/external/resource.txt";
 
     private final String annotationBodyPath = ".ro/ann1.ttl";
 
     private final String annotationBodyURIRDF = ".ro/ann1.rdf";
 
     private final String username = "John Doe";
 
     private final String username2 = "May Gray";
 
     private final List<String> linkHeadersR = new ArrayList<>();
 
 
     public ResourcesTest() {
         super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").servletClass(TestServletContainer.class).build());
     }
 
 
     @Test
     public final void test()
             throws URISyntaxException {
         client().setFollowRedirects(true);
         if (resource().getURI().getHost().equals("localhost")) {
             webResource = resource();
         } else {
             webResource = resource().path("rodl/");
         }
         linkHeadersR.clear();
         linkHeadersR.add("<" + resource().getURI() + "ROs/r/.ro/manifest.rdf>; rel=bookmark");
         linkHeadersR.add("<" + resource().getURI() + "zippedROs/r/>; rel=bookmark");
         linkHeadersR.add("<http://sandbox.wf4ever-project.org/portal/ro?ro=" + resource().getURI()
                 + "ROs/r/>; rel=bookmark");
 
         createClient();
         try {
             getClientsList();
             getClient();
             createUsers();
             getUser();
 
             try {
                 createAccessTokens();
                 try {
                     getAccessTokensList();
                     checkWhoAmI();
                     try {
                         webResource.path("ROs/" + r + "/").header("Authorization", "Bearer " + accessToken).delete();
                     } catch (Exception e) {
                     }
                     try {
                         webResource.path("ROs/" + r2 + "/").header("Authorization", "Bearer " + accessToken).delete();
                     } catch (Exception e) {
                     }
                     createROs();
                     try {
                         getROsList();
                         getROMetadata();
                         getROHtml();
                         getROZip();
                         getInitialManifest();
                         updateManifest();
                         addFile();
                         getFileContent();
                         aggregateExternalResource();
                         addRDFFile();
                         getRDFFileContent();
                         addAnnotationBody();
                         getAnnotationBody();
                         getManifest();
                         getManifestWithAnnotationBody();
                         deleteAnnotationBody();
                         deleteFile();
                         deleteRDFFile();
                         getInitialManifest();
                     } finally {
                         deleteROs();
                     }
                 } finally {
                     deleteAccessTokens();
                 }
             } finally {
                 deleteUsers();
             }
         } finally {
             deleteClient();
         }
     }
 
 
     private void getUser() {
         String user = webResource.path("users/" + userIdUrlSafe).header("Authorization", "Bearer " + adminCreds)
                 .get(String.class);
         assertTrue(user.contains(userId));
 
         user = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
                 .get(String.class);
         assertTrue(user.contains(userId2));
 
         user = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
                 .accept("application/x-turtle").get(String.class);
         assertTrue(user.contains(userId2));
     }
 
 
     private void createClient() {
         ClientResponse response = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds)
                 .post(ClientResponse.class, clientName + "\r\n" + clientRedirectionURI);
         assertEquals(201, response.getStatus());
         URI clientURI = response.getLocation();
         clientId = clientURI.resolve(".").relativize(clientURI).toString();
         response.close();
     }
 
 
     private void getClientsList() {
         String list = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds).get(String.class);
         assertTrue(list.contains(clientId));
     }
 
 
     private void getClient() {
         String client = webResource.path("clients/" + clientId).header("Authorization", "Bearer " + adminCreds)
                 .get(String.class);
         assertTrue(client.contains(clientId));
     }
 
 
     private void createUsers() {
         ClientResponse response = webResource.path("users/" + userIdUrlSafe)
                 .header("Authorization", "Bearer " + adminCreds).put(ClientResponse.class, username);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         response.close();
 
         response = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
                 .put(ClientResponse.class, username2);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         response.close();
     }
 
 
     private void createAccessTokens() {
         ClientResponse response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                 .post(ClientResponse.class, clientId + "\r\n" + userId);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         URI accessTokenURI = response.getLocation();
         accessToken = accessTokenURI.resolve(".").relativize(accessTokenURI).toString();
         response.close();
 
         response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                 .post(ClientResponse.class, clientId + "\r\n" + userId2);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         accessTokenURI = response.getLocation();
         accessToken2 = accessTokenURI.resolve(".").relativize(accessTokenURI).toString();
         response.close();
     }
 
 
     private void getAccessTokensList() {
         String list = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                 .get(String.class);
         assertTrue(list.contains(accessToken));
     }
 
 
     private void checkWhoAmI() {
         String whoami = webResource.path("whoami/").header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue(whoami.contains(userId));
         assertTrue(whoami.contains(username));
 
         whoami = webResource.path("whoami/").header("Authorization", "Bearer " + accessToken2).get(String.class);
         assertTrue(whoami.contains(userId2));
         assertTrue(whoami.contains(username2));
 
         try {
             webResource.path("whoami/").get(Response.class);
             fail("WhoAmI requests without access tokens should throw 401 Unauthorized");
         } catch (UniformInterfaceException e) {
             assertEquals("WhoAmI requests without access tokens should throw 401 Unauthorized", 401, e.getResponse()
                     .getStatus());
 
         }
     }
 
 
     private void createROs() {
         String manifest = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken).header("Slug", r)
                 .post(String.class);
         assertTrue(manifest.contains(userId));
 
         ClientResponse response = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken2)
                 .header("Slug", r2).post(ClientResponse.class);
         response.close();
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
     }
 
 
     private void getROsList() {
         String list = webResource.path("ROs").header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue(list.contains(r));
         assertTrue(!list.contains(r2));
 
         list = webResource.path("ROs").get(String.class);
         assertTrue(list.contains(r));
         assertTrue(list.contains(r2Encoded));
     }
 
 
     private void getROMetadata() {
         client().setFollowRedirects(false);
         ClientResponse response = webResource.path("ROs").path(r).path("/").accept("text/turtle")
                 .get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
         assertEquals(webResource.path("ROs").path(r).path(".ro/manifest.rdf").getURI().getPath(), response
                 .getLocation().getPath());
         response.close();
         client().setFollowRedirects(true);
     }
 
 
     private void getROHtml()
             throws URISyntaxException {
         client().setFollowRedirects(false);
         ClientResponse response = webResource.path("ROs").path(r2).path("/").accept("text/html")
                 .get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
         URI portalURI = new URI("http", "sandbox.wf4ever-project.org", "/portal/ro", "ro="
                 + webResource.path("ROs").path(r2Encoded).path("/").getURI().toString(), null);
         assertEquals(portalURI.getPath(), response.getLocation().getPath());
         assertTrue(portalURI.getQuery().contains("ro="));
         response.close();
         client().setFollowRedirects(true);
     }
 
 
     private void getROZip()
             throws URISyntaxException {
         client().setFollowRedirects(false);
         ClientResponse response = webResource.path("ROs").path(r).path("/").accept("application/zip")
                 .get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_SEE_OTHER, response.getStatus());
         assertEquals(webResource.path("zippedROs").path(r).path("/").getURI().getPath(), response.getLocation()
                 .getPath());
         response.close();
 
         response = webResource.path("zippedROs").path(r).path("/").get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_OK, response.getStatus());
         assertEquals("application/zip", response.getType().toString());
         response.close();
 
         response = webResource.path("zippedROs").path(r).path("/").accept("text/html;q=0.9,*/*;q=0.8")
                 .get(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_OK, response.getStatus());
         assertEquals("application/zip", response.getType().toString());
         //        assertEquals(linkHeadersR, response.getHeaders().get("Link"));
         response.close();
         client().setFollowRedirects(true);
     }
 
 
     private void updateManifest() {
         InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
         ClientResponse response = webResource.path("ROs/" + r + "/.ro/manifest.rdf")
                 .header("Authorization", "Bearer " + accessToken).type("text/turtle").put(ClientResponse.class, is);
         assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
         response.close();
     }
 
 
     private void addFile() {
         ClientResponse response = webResource.path("ROs/" + r + "/").header("Slug", filePath)
                 .header("Authorization", "Bearer " + accessToken).type("text/plain")
                 .post(ClientResponse.class, "lorem ipsum");
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         response.close();
     }
 
 
     private void aggregateExternalResource() {
         String body = String.format("<rdf:RDF xmlns:ore=\"http://www.openarchives.org/ore/terms/\""
                 + " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >"
                 + "<ore:Proxy> <ore:proxyFor rdf:resource=\"%s\" /> </ore:Proxy> </rdf:RDF>", externalResource);
         ClientResponse response = webResource.path("ROs/" + r + "/").header("Authorization", "Bearer " + accessToken)
                 .type("application/vnd.wf4ever.proxy").post(ClientResponse.class, body);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         response.close();
     }
 
 
     private void getFileContent() {
         String metadata = webResource.path("ROs/" + r + "/" + filePath)
                 .header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue(metadata.contains("lorem ipsum"));
 
     }
 
 
     private void addRDFFile() {
         String body = "<rdf:RDF" + "  xmlns:ore=\"http://www.openarchives.org/ore/terms/\" \n"
                 + "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n" + "   <ore:Proxy>\n"
                 + "   </ore:Proxy>\n" + " </rdf:RDF>";
         ClientResponse response = webResource.path("ROs/" + r + "/").header("Authorization", "Bearer " + accessToken)
                 .type("application/vnd.wf4ever.proxy").header("Slug", rdfFilePath).post(ClientResponse.class, body);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         rdfProxy = response.getLocation();
         response.close();
 
         response = webResource.path("ROs/" + r + "/" + rdfFilePath).header("Authorization", "Bearer " + accessToken)
                 .type(RDFFormat.RDFXML.getDefaultMIMEType()).put(ClientResponse.class, "lorem ipsum");
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         response.close();
     }
 
 
     private void getRDFFileContent() {
         String metadata = webResource.path("ROs/" + r + "/" + rdfFilePath).queryParam("content", "true")
                 .header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue(metadata.contains("lorem ipsum"));
 
     }
 
 
     private void getManifest() {
         String manifest = webResource.path("/ROs/" + r + "/.ro/manifest.rdf")
                 .header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue(manifest.contains(userId));
         assertTrue(manifest.contains(filePathEncoded));
         assertTrue(manifest.contains(rdfProxy.toString()));
 
         manifest = webResource.path("/ROs/" + r + "/.ro/manifest.rdf").header("Authorization", "Bearer " + accessToken)
                 .accept("application/x-turtle").get(String.class);
         assertTrue(manifest.contains(userId));
         assertTrue(manifest.contains(filePathEncoded));
 
         manifest = webResource.path("/ROs/" + r + "/.ro/manifest.n3").queryParam("original", "manifest.rdf")
                 .header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue(manifest.contains(userId));
         assertTrue(manifest.contains(filePathEncoded));
 
         ClientResponse response = webResource.path("/ROs/" + r + "/.ro/manifest.n3")
                 .header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
         assertEquals("Should return 404 for manifest.n3", HttpStatus.SC_NOT_FOUND, response.getStatus());
         response.close();
     }
 
 
     private void getManifestWithAnnotationBody() {
         String manifest = webResource.path("/ROs/" + r + "/.ro/manifest.rdf")
                 .header("Authorization", "Bearer " + accessToken).accept("application/x-trig").get(String.class);
         assertTrue(manifest.contains(userId));
         assertTrue(manifest.contains(filePathEncoded));
         assertTrue("Annotation body should contain file path: " + filePath, manifest.contains("a_workflow.t2flow"));
         assertTrue(manifest.contains("A test"));
 
         manifest = webResource.path("/ROs/" + r + "/.ro/manifest.trig").queryParam("original", "manifest.rdf")
                 .header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue(manifest.contains(userId));
         assertTrue(manifest.contains(filePathEncoded));
         assertTrue("Annotation body should contain file path: " + filePathEncoded,
             manifest.contains("a_workflow.t2flow"));
         assertTrue(manifest.contains("A test"));
     }
 
 
     private void addAnnotationBody() {
         InputStream is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
         ClientResponse response = webResource
                 .path("ROs/" + r + "/")
                 .header("Slug", annotationBodyPath)
                 .header(
                     "Link",
                     "<" + webResource.path("ROs").path(r).path("/").getURI().toString()
                             + ">; rel=\"http://purl.org/ao/annotates\"")
                 .header("Authorization", "Bearer " + accessToken).type("application/x-turtle")
                 .post(ClientResponse.class, is);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         response.close();
     }
 
 
     private void getAnnotationBody() {
         String body = webResource.path("ROs/" + r + "/" + annotationBodyPath)
                 .header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue("Annotation body should contain file path: a_workflow.t2flow", body.contains("a_workflow.t2flow"));
         assertTrue(body.contains("A test"));
     }
 
 
     private void deleteAnnotationBody() {
         ClientResponse response = webResource.path("ROs/" + r + "/" + annotationBodyURIRDF)
                 .queryParam("original", "ann1.ttl").header("Authorization", "Bearer " + accessToken)
                 .delete(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
         response.close();
     }
 
 
     private void deleteFile() {
         ClientResponse response = webResource.path("ROs/" + r + "/" + filePath)
                 .header("Authorization", "Bearer " + accessToken).delete(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
         response.close();
     }
 
 
     private void deleteRDFFile() {
         ClientResponse response = webResource.uri(rdfProxy).header("Authorization", "Bearer " + accessToken)
                 .type(RDFFormat.RDFXML.getDefaultMIMEType()).delete(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
         response.close();
     }
 
 
     private void getInitialManifest() {
         String manifest = webResource.path("ROs/" + r + "/.ro/manifest.rdf")
                 .header("Authorization", "Bearer " + accessToken).get(String.class);
         assertTrue(!manifest.contains(filePathEncoded));
 
         manifest = webResource.path("ROs/" + r + "/.ro/manifest.rdf").header("Authorization", "Bearer " + accessToken)
                 .accept("application/x-turtle").get(String.class);
         assertTrue(!manifest.contains(filePathEncoded));
 
         //        ClientResponse response = webResource.path("ROs/" + r + "/.ro/manifest.rdf")
         //                .header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
         //        assertEquals(linkHeadersR, response.getHeaders().get("Link"));
         //        response.close();
     }
 
 
     private void deleteROs() {
         ClientResponse response = webResource.path("ROs/" + r + "/").header("Authorization", "Bearer " + accessToken)
                 .delete(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
         response.close();
 
         response = webResource.path("ROs/" + r2 + "/").header("Authorization", "Bearer " + accessToken2)
                 .delete(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
         response.close();
     }
 
 
     private void deleteAccessTokens() {
         ClientResponse response = webResource.path("accesstokens/" + accessToken)
                 .header("Authorization", "Bearer " + adminCreds).delete(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
         response.close();
 
         response = webResource.path("accesstokens/" + accessToken2).header("Authorization", "Bearer " + adminCreds)
                 .delete(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
         response.close();
     }
 
 
     private void deleteUsers() {
         ClientResponse response = webResource.path("users/" + userIdUrlSafe)
                 .header("Authorization", "Bearer " + adminCreds).delete(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
         response.close();
 
         response = webResource.path("users/" + userId2UrlSafe).header("Authorization", "Bearer " + adminCreds)
                 .delete(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
         response.close();
     }
 
 
     private void deleteClient() {
         ClientResponse response = webResource.path("clients/" + clientId)
                 .header("Authorization", "Bearer " + adminCreds).delete(ClientResponse.class);
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
         response.close();
     }
 }
