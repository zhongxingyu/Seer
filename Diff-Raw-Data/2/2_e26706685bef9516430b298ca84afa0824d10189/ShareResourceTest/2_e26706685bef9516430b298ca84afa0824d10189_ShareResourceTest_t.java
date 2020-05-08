 package x1.stomp.test;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.UUID;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response.Status;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.resteasy.client.ClientRequest;
 import org.jboss.resteasy.client.ClientResponse;
 import org.jboss.resteasy.util.GenericType;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
 import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import static org.junit.Assert.*;
 import javax.inject.Inject;
 import x1.stomp.model.Share;
 import x1.stomp.rest.ErrorResponse;
 
 @RunWith(Arquillian.class)
 public class ShareResourceTest {
   private static final String BASE_URL = "http://localhost:8080/stomp-test/rest";
 
   @Inject
   private Logger log;
 
   @Deployment
   public static Archive<?> createTestArchive() {
     Collection<JavaArchive> libraries = 
     DependencyResolvers.use(MavenDependencyResolver.class)
       .loadMetadataFromPom("pom.xml")
       .artifact("org.apache.httpcomponents:fluent-hc")
      .artifact("org.apache.commons:commons-lang3")
       .artifact("org.codehaus.jettison:jettison")
       .resolveAs(JavaArchive.class);
     
     return ShrinkWrap.create(WebArchive.class, "stomp-test.war").addPackages(true, "x1.stomp")
         .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
         .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addAsWebInfResource("test-ds.xml", "test-ds.xml")
         .addAsLibraries(libraries);
   }
 
   @Test
   public void testFindShareNotFound() throws Exception {
     log.debug("begin testFindShareNotFound");
     ClientRequest request = new ClientRequest(BASE_URL + "/shares/{key}");
     request.accept(MediaType.APPLICATION_JSON);
     request.pathParameter("key", "AAPL");
     ClientResponse<Share> response = request.get(Share.class);
     assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
     log.debug("end testFindShareNotFound");
   }
 
   @Test
   public void testAddAndFindShare() throws Exception {
     log.debug("begin testAddAndFindShare");
     Share share = new Share();
     share.setKey("MSFT");
     share.setName("Microsoft");
     ClientRequest request = new ClientRequest(BASE_URL + "/shares/");
     request.body(MediaType.APPLICATION_JSON, share);
     request.header("Correlation-Id", UUID.randomUUID().toString());
     ClientResponse<Share> response = request.post(Share.class);
     assertEquals(Status.OK.getStatusCode(), response.getStatus());
     Share created = response.getEntity();
     assertNotNull(created);
     assertNull(created.getId());
     assertEquals("MSFT", share.getKey());
     Thread.sleep(2500);
     request = new ClientRequest(BASE_URL + "/shares/{key}");
     request.accept(MediaType.APPLICATION_JSON);
     request.pathParameter("key", "MSFT");
     response = request.get(Share.class);
     assertEquals(Status.OK.getStatusCode(), response.getStatus());
     Share found = response.getEntity();
     assertNotNull(found);
     assertNull(created.getId());
     assertEquals("MSFT", share.getKey());
     request = new ClientRequest(BASE_URL + "/shares");
     request.accept(MediaType.APPLICATION_JSON);
 
     ClientResponse<List<Share>> response2 = request.get(new GenericType<List<Share>>() {
     });
     assertEquals(Status.OK.getStatusCode(), response2.getStatus());
     List<Share> shares = response2.getEntity();
     assertEquals(1, shares.size());
     log.debug("end testAddAndFindShare");
   }
 
   @Test
   public void testAddShareInvalid() throws Exception {
     log.debug("begin testAddShareInvalid");
     Share share = new Share();
     share.setKey("GOOG");
     ClientRequest request = new ClientRequest(BASE_URL + "/shares/");
     request.body(MediaType.APPLICATION_JSON, share);
     ClientResponse<ErrorResponse> response = request.post(ErrorResponse.class);
     assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
     ErrorResponse errorResponse = response.getEntity();
     assertNotNull(errorResponse);
     assertEquals(2, errorResponse.getErrors().size());
 
     request = new ClientRequest(BASE_URL + "/shares/{key}");
     request.accept(MediaType.APPLICATION_JSON);
     request.pathParameter("key", "GOOG");
     ClientResponse<Share> response2 = request.get(Share.class);
     assertEquals(Status.NOT_FOUND.getStatusCode(), response2.getStatus());
     log.debug("end testAddShareInvalid");
   }
 }
