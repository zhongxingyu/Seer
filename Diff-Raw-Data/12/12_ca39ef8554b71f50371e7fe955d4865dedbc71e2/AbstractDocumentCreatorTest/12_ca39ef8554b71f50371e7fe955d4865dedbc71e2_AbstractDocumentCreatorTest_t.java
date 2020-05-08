 package se.linkon.sabine.docutil.xml.documentcreator;
 
 import org.custommonkey.xmlunit.Diff;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.junit.Before;
 import org.xml.sax.SAXException;
 import se.linkon.sabine.docutil.shared.DocumentCreator;
 import se.linkon.sabine.docutil.shared.DocumentCreatorFactory;
 import se.linkon.sabine.docutil.xml.XmlDoclet;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import static org.junit.Assert.assertTrue;
 
 public abstract class AbstractDocumentCreatorTest {
 
     @Before
     public void setUp() throws Exception {
         XMLUnit.setIgnoreWhitespace(true);
         XMLUnit.setIgnoreComments(true);
         XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
         XMLUnit.setIgnoreAttributeOrder(true);
     }
 
     protected void performTest(Class testClass, String documentCreatorId) throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
 
         DocumentCreator documentCreator = DocumentCreatorFactory.getDocumentCreator(documentCreatorId);
 
         String testClassFileName = new File(".\\src\\test\\java\\" + testClass.getName().replace('.', File.separatorChar) + ".java").getAbsolutePath();
        File actualFile = File.createTempFile("xmldoclet-test-" + testClass.getName() + "-" + documentCreator.getClass().getSimpleName() + "-", ".xml");
         com.sun.tools.javadoc.Main.execute(
                 "javadoc",
                 XmlDoclet.class.getName(),
                 new String[]{
                         "-doclet",
                         XmlDoclet.class.getName(),
                         "-private",
                         "-action.1.format.name",
                         documentCreatorId,
                         "-action.1.output",
                         actualFile.getAbsolutePath(),
                         "-action.1.format.property.showFields",
                         "false",
                         testClassFileName
                 });
 
        File expectedFile = new File("target\\test-classes\\" + testClass.getName() + "." + documentCreator.getClass().getSimpleName() + ".xml");
         Diff diff = new Diff(new FileReader(expectedFile), new FileReader(actualFile));
 //        actualFile.delete();
 
         assertTrue(diff.identical());
     }
 }
