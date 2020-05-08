 package pl.psnc.dl.wf4ever.resources;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.UUID;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.naming.NamingException;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 
 import pl.psnc.dl.wf4ever.IntegrationTest;
 import pl.psnc.dl.wf4ever.W4ETest;
 import pl.psnc.dl.wf4ever.job.Job.State;
 import pl.psnc.dl.wf4ever.job.JobStatus;
 import pl.psnc.dl.wf4ever.zip.ROFromZipJobStatus;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource.Builder;
 
 @Category(IntegrationTest.class)
 public class ZipTest extends W4ETest {
 
     protected String createdFromZipResourceObject = UUID.randomUUID().toString();
     protected Integer maxSeonds = 300;
 
 
     @Override
     public void setUp()
             throws Exception {
         super.setUp();
         createUserWithAnswer(userIdSafe, username).close();
         accessToken = createAccessToken(userId);
         ro = createRO(accessToken);
         ro2 = createRO(accessToken);
     }
 
 
     @Override
     public void tearDown()
             throws Exception {
         deleteROs();
         deleteAccessToken(accessToken);
         deleteUser(userIdSafe);
         super.tearDown();
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
 
 
     @Test
     public void storeROFromZip()
             throws IOException, ClassNotFoundException, NamingException, InterruptedException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/ro1.zip");
         Builder wr = webResource.path("zip/upload").accept("application/json")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip");
         ClientResponse response = wr.post(ClientResponse.class, is);
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response.getStatus());
         JobStatus status = getStatus(response.getLocation());
         response.close();
         Assert.assertEquals(State.DONE, status.getState());
         response = webResource.uri(status.getTarget()).accept("application/zip")
                 .header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
         is = response.getEntity(InputStream.class);
         File file = File.createTempFile("storeFromZipTests", "zip");
         FileOutputStream outputStream = new FileOutputStream(file);
         IOUtils.copy(is, outputStream);
         is.close();
         outputStream.close();
         ZipFile zip = new ZipFile(file);
         Enumeration<? extends ZipEntry> e = zip.entries();
         ArrayList<String> entries = new ArrayList<String>();
         while (e.hasMoreElements()) {
             entries.add(e.nextElement().getName());
         }
         Assert.assertTrue(entries.contains("conclusion.pdf"));
         Assert.assertTrue(entries.contains(".ro/manifest.rdf"));
         Assert.assertTrue(entries.contains(".ro/evo_info.ttl"));
         Assert.assertTrue(entries.contains("Hypothesis.txt"));
         file.delete();
     }
 
 
     @Test
     public void storeWrongROFromZip()
             throws IOException, ClassNotFoundException, NamingException, InterruptedException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/wrong.zip");
         ClientResponse response = webResource.path("zip/upload").accept("application/json")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, is);
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response.getStatus());
         response.close();
         JobStatus status = getStatus(response.getLocation());
         Assert.assertEquals(State.SERVICE_ERROR, status.getState());
     }
 
 
     @Test
     public void createROFromZipWithWhitespaces()
             throws Exception {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/white_spaces_ro.zip");
         ClientResponse response = webResource.path("zip/upload").accept("application/json")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, is);
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response.getStatus());
         response.close();
         JobStatus status = getStatus(response.getLocation());
         Assert.assertEquals(State.DONE, status.getState());
     }
 
 
     @Test
     public void createROFromZipWithEvoAnnotation()
             throws Exception {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/zip_with_evo.zip");
         ClientResponse response = webResource.path("zip/upload").accept("application/json")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, is);
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response.getStatus());
         response.close();
         JobStatus status = getStatus(response.getLocation());
         Assert.assertEquals(State.DONE, status.getState());
     }
 
 
     @Test
     public void createConflictedROFromZip()
             throws Exception {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/ro1.zip");
         ClientResponse response1 = webResource.path("zip/upload").accept("application/json")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, IOUtils.toByteArray(is));
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response1.getStatus());
         JobStatus status1 = getStatus(response1.getLocation());
         Assert.assertEquals(State.DONE, status1.getState());
 
         is = getClass().getClassLoader().getResourceAsStream("singleFiles/ro1.zip");
         ClientResponse response2 = webResource.path("zip/upload").accept("application/json")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, IOUtils.toByteArray(is));
 
         JobStatus status2 = getStatus(response2.getLocation());
         Assert.assertEquals(State.DONE, status2.getState());
         Assert.assertNotEquals(status1.getTarget(), status2.getTarget());
     }
 
 
     @Test
     public void createROFromGivenZip()
             throws Exception {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/aggregatedNoFolder.zip");
         ClientResponse response = webResource.path("zip/create").accept("application/json")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, is);
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response.getStatus());
         response.close();
         JobStatus status = getStatus(response.getLocation());
         Assert.assertEquals(State.DONE, status.getState());
         response = webResource.uri(status.getTarget()).accept("application/zip")
                 .header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
         is = response.getEntity(InputStream.class);
         File file = File.createTempFile("storeFromZipTests", "zip");
         FileOutputStream outputStream = new FileOutputStream(file);
         IOUtils.copy(is, outputStream);
         is.close();
         outputStream.close();
         ZipFile zip = new ZipFile(file);
         Enumeration<? extends ZipEntry> e = zip.entries();
         ArrayList<String> entries = new ArrayList<String>();
         while (e.hasMoreElements()) {
             entries.add(e.nextElement().getName());
         }
         Assert.assertTrue(entries.contains("1.txt"));
         Assert.assertTrue(entries.contains("2.txt"));
         Assert.assertTrue(entries.contains("3.txt"));
         Assert.assertTrue(entries.contains(".ro/manifest.rdf"));
         Assert.assertTrue(entries.contains(".ro/evo_info.ttl"));
         file.delete();
 
     }
 
 
     @Test
     public void createROFromGivenZipWithFolders()
             throws Exception {
         InputStream is = getClass().getClassLoader().getResourceAsStream("singleFiles/aggregatedWithFolder.zip");
         ClientResponse response = webResource.path("zip/create").accept("application/json")
                 .header("Authorization", "Bearer " + accessToken).header("Slug", createdFromZipResourceObject)
                 .type("application/zip").post(ClientResponse.class, is);
         assertEquals("Research object should be created correctly", HttpServletResponse.SC_CREATED,
             response.getStatus());
         response.close();
         JobStatus status = getStatus(response.getLocation());
         Assert.assertEquals(State.DONE, status.getState());
 
         response = webResource.uri(status.getTarget()).accept("application/zip")
                 .header("Authorization", "Bearer " + accessToken).get(ClientResponse.class);
         is = response.getEntity(InputStream.class);
         File file = File.createTempFile("storeFromZipTests", "zip");
         FileOutputStream outputStream = new FileOutputStream(file);
         IOUtils.copy(is, outputStream);
         is.close();
         outputStream.close();
         ZipFile zip = new ZipFile(file);
         Enumeration<? extends ZipEntry> e = zip.entries();
         List entries = new ArrayList<String>();
         while (e.hasMoreElements()) {
             entries.add(e.nextElement().getName());
         }
         Assert.assertTrue(entries.contains("1.txt"));
         Assert.assertTrue(entries.contains("2.txt"));
         Assert.assertTrue(entries.contains("3.txt"));
         Assert.assertTrue(entries.contains(".ro/manifest.rdf"));
         Assert.assertTrue(entries.contains(".ro/evo_info.ttl"));
         Assert.assertTrue(entries.contains("innerFolder/innerFolder.rdf"));
         Assert.assertTrue(entries.contains("innerFolder/5.txt"));
         Assert.assertTrue(entries.contains("innerFolder/4.txt"));
         file.delete();
     }
 
 
     //helpers
 
     /**
      * Get job status.
      * 
      * @param jobUri
      *            .
      * @return .
      * @throws InterruptedException .
      */
     private JobStatus getStatus(URI jobUri)
             throws InterruptedException {
         int counter = 0;
         while (counter < maxSeonds) {
             Thread.sleep(1000);
             ROFromZipJobStatus status = webResource.uri(jobUri).accept("application/json")
                     .get(ROFromZipJobStatus.class);
             if (status.getState() != State.RUNNING) {
                 if (status.getReason() != null) {
                     System.out.println(status.getReason());
                 }
                 return status;
 
             }
             counter++;
         }
         return null;
 
     }
 
 }
