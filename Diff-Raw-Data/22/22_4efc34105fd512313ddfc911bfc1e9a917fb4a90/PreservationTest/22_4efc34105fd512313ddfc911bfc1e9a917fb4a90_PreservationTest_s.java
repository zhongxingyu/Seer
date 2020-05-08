 package pl.psnc.dl.wf4ever.preservation;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 
 import pl.psnc.dl.wf4ever.IntegrationTest;
 import pl.psnc.dl.wf4ever.W4ETest;
 import pl.psnc.dl.wf4ever.darceo.client.DArceoException;
 import pl.psnc.dl.wf4ever.db.dao.ResearchObjectPreservationStatusDAO;
 
 import com.sun.jersey.api.client.ClientResponse;
 
 @Category(IntegrationTest.class)
 public class PreservationTest extends W4ETest {
 
     int SINGLE_BREAK = 10;
     int MAX_PAUSES_NUMBER = 100;
     private ResearchObjectPreservationStatusDAO dao = new ResearchObjectPreservationStatusDAO();
 
 
     @Override
     public void setUp()
             throws Exception {
         super.setUp();
         createUserWithAnswer(userIdSafe, username).close();
         accessToken = createAccessToken(userId);
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
     public void testCreateAndPresarve()
             throws DArceoException, IOException, InterruptedException {
         URI cretedRO = createRO(accessToken);
         ResearchObjectPreservationStatus preservationStatus = dao.findById(cretedRO.toString());
         Assert.assertEquals(Status.NEW, preservationStatus.getStatus());
 
     }
 
 
     @Test
     public void testUpdateAndPreserver()
             throws DArceoException, IOException, InterruptedException {
         String filePath = "added/file";
         URI cretedRO = createRO(accessToken);
         ClientResponse response = addFile(cretedRO, filePath, accessToken);
         ResearchObjectPreservationStatus preservationStatus = dao.findById(cretedRO.toString());
         Assert.assertEquals(Status.NEW, preservationStatus.getStatus());
     }
 
 
     @Test
     public void testAnnotateAndPreserve()
             throws InterruptedException, DArceoException, IOException {
         String annotationBodyPath = "annotation/body";
         URI cretedRO = createRO(accessToken);
         InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationGood.rdf");
         ClientResponse response = addAnnotation(is, cretedRO, annotationBodyPath, accessToken);
         IOUtils.closeQuietly(is);
         ResearchObjectPreservationStatus preservationStatus = dao.findById(cretedRO.toString());
         Assert.assertEquals(Status.NEW, preservationStatus.getStatus());
     }
 
 
     @Test
     public void testUpdateSaveAndUpdate()
             throws DArceoException, IOException, InterruptedException {
         String filePath = "added/file";
         URI cretedRO = createRO(accessToken);
         ClientResponse response = addFile(cretedRO, filePath, accessToken);
         response = webResource.uri(cretedRO).path(filePath).header("Authorization", "Bearer " + accessToken)
                 .type("text/plain").put(ClientResponse.class, "lorem ipsum");
         ResearchObjectPreservationStatus preservationStatus = dao.findById(cretedRO.toString());
         preservationStatus.setStatus(Status.UP_TO_DATE);
         dao.save(preservationStatus);
         response = webResource.uri(cretedRO).path(filePath).header("Authorization", "Bearer " + accessToken)
                 .type("text/plain").put(ClientResponse.class, "new content");
         preservationStatus = dao.findById(cretedRO.toString());
         Assert.assertEquals(Status.UPDATED, preservationStatus.getStatus());
     }
 
 }
