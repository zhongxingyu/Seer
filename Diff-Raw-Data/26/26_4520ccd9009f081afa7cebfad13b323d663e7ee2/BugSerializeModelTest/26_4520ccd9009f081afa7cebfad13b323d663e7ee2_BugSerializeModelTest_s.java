 package hudson.plugins.pmd.parser;
 
 import hudson.XmlFile;
 import hudson.plugins.analysis.test.AbstractSerializeModelTest;
 import hudson.plugins.analysis.util.model.AbstractAnnotation;
 import hudson.plugins.analysis.util.model.AnnotationStream;
 import hudson.plugins.analysis.util.model.FileAnnotation;
 import hudson.plugins.analysis.util.model.JavaProject;
 import hudson.plugins.analysis.util.model.Priority;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.net.URISyntaxException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.thoughtworks.xstream.XStream;
 
 /**
  * Tests the serialization of the model.
  *
  * @see <a href="http://www.ibm.com/developerworks/library/j-serialtest.html">Testing object serialization</a>
  */
 public class BugSerializeModelTest extends AbstractSerializeModelTest {
     /** Serialization provider. */
     private static final XStream XSTREAM = new AnnotationStream();
 
     static {
         XSTREAM.alias("bug", Bug.class);
     }
 
     /**
      * Verifies the first created annotation.
      *
      * @param annotation
      *            the first created annotation
      */
     @Override
     protected void verifyFirstAnnotation(final AbstractAnnotation annotation) {
         Bug bug = (Bug)annotation;
         Assert.assertEquals("Wrong detail message." , TEST_TASK1, bug.getMessage());
     }
 
     /**
      * Creates an annotation.
      *
      * @param line
      *            the line
      * @param message
      *            the message
      * @param priority
      *            the priority
      * @param fileName
      *            the file name
      * @param packageName
      *            the package name
      * @param moduleName
      *            the module name
      * @return the annotation
      */
     @Override
     protected AbstractAnnotation createAnnotation(final int line, final String message, final Priority priority, final String fileName, final String packageName, final String moduleName) {
         Bug annotation = new Bug(priority, message, message, message, line);
         annotation.setFileName(fileName);
         annotation.setPackageName(packageName);
         annotation.setModuleName(moduleName);
 
         return annotation;
     }
 
     /**
      * Test whether a serialized project is the same object after deserialization of the file format of release 2.2.
      *
     * @throws Exception Signals a test failure
      */
     @Test
    public void ensureSameSerialization() throws Exception {
         InputStream inputStream = BugSerializeModelTest.class.getResourceAsStream("project.ser");
         ObjectInputStream objectStream = new ObjectInputStream(inputStream);
         Object deserialized = objectStream.readObject();
         JavaProject project = (JavaProject) deserialized;
 
         verifyProject(project);
     }
 
     /**
      * Test whether a serialized project is the same object after
      * deserialization of the file format of release 2.2.
      *
      * @throws IOException
      *             Signals that an I/O exception has occurred.
      * @throws URISyntaxException
      *             if URI is wrong
      */
     @Test
     public void ensureSameXmlSerialization() throws IOException, URISyntaxException {
         XmlFile xmlFile = createXmlFile(new File(BugSerializeModelTest.class.getResource("project.ser.xml").toURI()));
         Object deserialized = xmlFile.read();
 
         FileAnnotation[] files = (FileAnnotation[]) deserialized;
         JavaProject project = new JavaProject();
         project.addAnnotations(files);
 
         verifyProject(project);
     }
 
     /** {@inheritDoc} */
     @Override
     protected XmlFile createXmlFile(final File file) {
         return new XmlFile(XSTREAM, file);
     }
 }
 
