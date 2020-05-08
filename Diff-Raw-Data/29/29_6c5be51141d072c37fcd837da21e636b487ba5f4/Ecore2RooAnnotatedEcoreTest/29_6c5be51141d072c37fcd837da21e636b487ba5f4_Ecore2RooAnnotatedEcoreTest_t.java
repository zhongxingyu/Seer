 package mx.itesm.model2roo;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.jdom.JDOMException;
 import org.junit.After;
 import org.junit.Test;
 
 /**
  * 
  * @author jccastrejon
  * 
  */
 public class Ecore2RooAnnotatedEcoreTest extends EcoreTest {
 
     /**
      * 
      * @throws JDOMException
      * @throws IOException
      */
     @Test
     public void testAnnotateEcore() throws JDOMException, IOException {
         File fileToAnnotate;
 
        // Annotate with roo profiles
        fileToAnnotate = new File("./tst/model/toys.ecore");
        Ecore2RooAnnotatedEcore.annotateEcore(fileToAnnotate, new File("./profiles/rooCommand.ecore"), new File(
                        "./profiles/rooStructure.ecore"));
 
         this.testAnnotatedClassifiers(fileToAnnotate);
     }
 
     @After
     public void afterTest() throws JDOMException, IOException {
        this.removeAnnotations(new File("./tst/model/toys.ecore"));
     }
 }
