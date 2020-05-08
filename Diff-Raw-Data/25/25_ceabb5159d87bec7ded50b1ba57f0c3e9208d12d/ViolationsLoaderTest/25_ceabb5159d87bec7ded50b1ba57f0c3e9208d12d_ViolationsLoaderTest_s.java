 package org.sonar.ide.shared;
 
 import org.junit.Test;
 import org.sonar.ide.test.SonarTestServer;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.connectors.ConnectionException;
 import org.sonar.wsclient.services.Source;
 import org.sonar.wsclient.services.Violation;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.hamcrest.Matchers.hasItems;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.*;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class ViolationsLoaderTest {
   private static final String RESOURCE_KEY = "test:test:[default].ClassOnDefaultPackage";
 
   private static final String[] LINES = {
       "/* This comment was added after analyze. */",
       "public class ClassOnDefaultPackage {",
       "",
       "  public ClassOnDefaultPackage(int i) {",
       "    /* Next line was changed */",
       "    //int j = i++;",
       "  }",
       "",
       "  /* Next method was renamed */",
       "  private String myMethod2() {",
       "    return \"hello\";",
       "  }",
       "}"
   };
 
   @Test(expected = ConnectionException.class)
   public void testServerUnavailable() throws Exception {
     ViolationsLoader.getViolations(Sonar.create("http://localhost:9999"), RESOURCE_KEY, LINES);
   }
 
   @Test
   public void testGetHashCode() {
     int hash1 = ViolationsLoader.getHashCode("int i;");
     int hash2 = ViolationsLoader.getHashCode("int\ti;");
     int hash3 = ViolationsLoader.getHashCode("int i;\n");
     int hash4 = ViolationsLoader.getHashCode("int i;\r\n");
     int hash5 = ViolationsLoader.getHashCode("int i;\r");
 
     assertThat(hash2, equalTo(hash1));
     assertThat(hash3, equalTo(hash1));
     assertThat(hash4, equalTo(hash1));
     assertThat(hash5, equalTo(hash1));
   }
 
   @Test
   public void testConvertLines() {
     Violation violation = mock(Violation.class);
     when(violation.getLine()).thenReturn(1);
     Source source = new Source();
     source.addLine(1, LINES[1]);
 
     ViolationsLoader.convertLines(
         Collections.singleton(violation),
         source,
         LINES
     );
 
     verify(violation).getLine();
     verify(violation).setLine(2);
     verifyNoMoreInteractions(violation);
   }
 
   @Test
   public void testSimilarViolations() {
     final String line = "int j = i++;";
     final String[] lines = {
         line,
         line
     };
     Violation violation1 = newViolation(1);
     Violation violation2 = newViolation(2);
     Source source = new Source()
         .addLine(1, line)
         .addLine(2, line);
 
     List<Violation> violations = ViolationsLoader.convertLines(
         Arrays.asList(violation1, violation2),
         source,
         lines
     );
 
     assertThat(violations.size(), is(2));
     assertThat(violations, hasItems(violation1, violation2));
     assertThat(violation1.getLine(), not(equalTo(violation2.getLine())));
   }
 
   @Test
   public void testGetViolations() throws Exception {
     SonarTestServer testServer = new SonarTestServer();
     testServer.start();
 
     List<Violation> violations = ViolationsLoader.getViolations(testServer.getSonar(), RESOURCE_KEY, LINES);
 
     assertThat(violations.size(), is(1));
     assertThat(violations.get(0).getLine(), is(2));
 
     testServer.stop();
   }
 
   protected Violation newViolation(int line) {
     Violation violation = new Violation();
     violation.setLine(line);
     return violation;
   }
 }
