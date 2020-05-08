 package xbrlcore.junit.instance;
 
 import java.io.File;
 
 import static org.junit.Assert.*;
 
 import org.jdom.Document;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import xbrlcore.instance.Instance;
 import xbrlcore.instance.InstanceFactory;
 import xbrlcore.instance.InstanceOutputter;
 
 /**
  * @author Daniel Hamm
  */
 public final class XBRLInstanceFactoryTest {
 
     /**
      * Instance used in this test case.
      */
     private static Instance instance;
 
     /**
      * Instance used in this test case.
      */
     private static Instance typedInstance;
 
     /**
      * This method is executed before all tests in this JUnit test.
      */
     @BeforeClass
     public static void setUp() {
        final String PATH = "xbrl/instances1.0/";
         try {
             InstanceFactory instanceFactory = InstanceFactory.get();
             instance = instanceFactory.createInstance(new File(PATH
                 + "ai_MKR.xml"));
             typedInstance = instanceFactory.createInstance(new File(PATH
                 + "ai_MKR_COM.xml"));
         } catch (Exception ex) {
             System.err.println(ex.toString());
             ex.printStackTrace();
             fail(ex.toString());
         }
     }
 
     /**
      * TODO: Add comment.
      */
     @Test
     public void printInstance() {
         assertNotNull(instance);
 
         try {
             InstanceOutputter instanceOutputter = new InstanceOutputter(
                 instance);
             Document instanceXML = instanceOutputter.getXML();
 
             /* outputting XML */
             XMLOutputter serializer = new XMLOutputter();
             Format f = Format.getPrettyFormat();
             f.setOmitDeclaration(false);
             serializer.setFormat(f);
             serializer.output(instanceXML, System.out);
         } catch (Exception ex) {
             fail(ex.toString());
         }
     }
 
     /**
      * TODO: Add comment.
      */
     @Test
     public void printInstanceTypedDimension() {
         assertNotNull(typedInstance);
 
         try {
             InstanceOutputter instanceOutputter = new InstanceOutputter(
                 typedInstance);
             Document instanceXML = instanceOutputter.getXML();
 
             /* outputting XML */
             XMLOutputter serializer = new XMLOutputter();
             Format f = Format.getPrettyFormat();
             f.setOmitDeclaration(false);
             serializer.setFormat(f);
             serializer.output(instanceXML, System.out);
         } catch (Exception ex) {
             ex.printStackTrace();
             fail(ex.toString());
         }
     }
 }
