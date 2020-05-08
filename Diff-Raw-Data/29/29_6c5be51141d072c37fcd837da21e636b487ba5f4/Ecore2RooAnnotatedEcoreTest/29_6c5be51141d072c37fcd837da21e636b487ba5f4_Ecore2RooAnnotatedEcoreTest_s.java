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
        File annotationsFile;
 
        // Annotate ecore file
        fileToAnnotate = new File("./tst/ecore/PizzaShop.ecore");
        annotationsFile = new File("./profiles/rooCommand.ecore");
        Ecore2RooAnnotatedEcore.annotateEcore(fileToAnnotate, annotationsFile);
        System.out.println("Se agregaron aotaciones de comandos");
        annotationsFile = new File("./profiles/rooStructure.ecore");
        Ecore2RooAnnotatedEcore.annotateEcore(fileToAnnotate, annotationsFile);
        System.out.println("Se agregaron anotaciones de estructura");
 
         this.testAnnotatedClassifiers(fileToAnnotate);
     }
 
     @After
     public void afterTest() throws JDOMException, IOException {
        this.removeAnnotations(new File("./tst/ecore/PizzaShop.ecore"));
     }
 }
