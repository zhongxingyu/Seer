 package org.sonar.ide.shared;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.ide.test.SonarTestServer;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.connectors.ConnectionException;
 import org.sonar.wsclient.services.Violation;
 
 import java.util.Collection;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class ViolationsLoaderTest {
   private ViolationsLoader loader;
 
   @Before
   public void setUp() throws Exception {
     loader = new ViolationsLoader();
   }
 
   @Test
   public void testServerAvailable() throws Exception {
     SonarTestServer testServer = new SonarTestServer();
     testServer.start();
 
     Collection<Violation> violations = loader.getViolations(testServer.getSonar(), "test:test:[default].ClassOnDefaultPackage");
     assertEquals(4, violations.size());
 
     testServer.stop();
   }
 
   @Test(expected = ConnectionException.class)
   public void testServerUnavailable() throws Exception {
     loader.getViolations(Sonar.create("http://localhost:9999"), "test");
   }
 }
