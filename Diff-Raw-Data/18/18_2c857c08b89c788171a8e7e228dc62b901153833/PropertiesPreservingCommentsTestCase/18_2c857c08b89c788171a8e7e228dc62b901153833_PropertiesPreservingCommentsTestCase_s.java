 package gov.nih.nci.cagrid.common;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import junit.framework.TestCase;
 
 public class PropertiesPreservingCommentsTestCase extends TestCase {
     
     private static final String RAW_PROPERTIES = 
         "\n" +
         "property1=value1\n" +
         "property2=value2\n" +
         "\n" +
         "\n" +
         "#property3=value3\n" +
         "### some comment\n" +
         "\n" +
         "property4=value4\n";
     
     private static final String SET_PROPERTY1 = 
         "\n" +
         "property1=changed\n" +
         "property2=value2\n" +
         "\n" +
         "\n" +
         "#property3=value3\n" +
         "### some comment\n" +
         "\n" +
         "property4=value4\n";
     
     private static final String SET_PROPERTY4 = 
         "\n" +
         "property1=value1\n" +
         "property2=value2\n" +
         "\n" +
         "\n" +
         "#property3=value3\n" +
         "### some comment\n" +
         "\n" +
         "property4=changed\n";
     
     private static final String ADD_PROPERTY5 = 
         "\n" +
         "property1=value1\n" +
         "property2=value2\n" +
         "\n" +
         "\n" +
         "#property3=value3\n" +
         "### some comment\n" +
         "\n" +
         "property4=value4\n" +
         "property5=value5\n";
     
     private static final String REMOVE_PROPERTY2 = 
         "\n" +
         "property1=value1\n" +
         "\n" +
         "\n" +
         "#property3=value3\n" +
         "### some comment\n" +
         "\n" +
         "property4=value4\n";
     
     private static final String COMMENT_PROPERTY2 = 
         "\n" +
         "property1=value1\n" +
         "#property2=value2\n" +
         "\n" +
         "\n" +
         "#property3=value3\n" +
         "### some comment\n" +
         "\n" +
         "property4=value4\n";
     
     private static final String UNCOMMENT_PROPERTY3 = 
         "\n" +
         "property1=value1\n" +
         "property2=value2\n" +
         "\n" +
         "\n" +
         "property3=value3\n" +
         "### some comment\n" +
         "\n" +
         "property4=value4\n";
     
     private static final String SET_PROPERTY3 = 
         "\n" +
         "property1=value1\n" +
         "property2=value2\n" +
         "\n" +
         "\n" +
         "property3=changed\n" +
         "### some comment\n" +
         "\n" +
         "property4=value4\n";
     
     
     private PropertiesPreservingComments properties = null;
 
     public PropertiesPreservingCommentsTestCase(String name) {
         super(name);
     }
     
     
     public void setUp() {
         properties = new PropertiesPreservingComments();
         InputStream propertiesIn = new ByteArrayInputStream(RAW_PROPERTIES.getBytes());
         try {
             properties.load(propertiesIn);
             propertiesIn.close();
         } catch (IOException ex) {
             ex.printStackTrace();
             fail("Error loading properties: " + ex.getMessage());
         }
     }
     
     
     private void compareProperties(String gold) {
         ByteArrayOutputStream propsOut = new ByteArrayOutputStream();
        properties.write(propsOut);
         String propertiesString = propsOut.toString();
         assertEquals("Edited properties differed from expected", gold, propertiesString);
     }
     
     
     public void testSetProperty1() {
         Object oldValue = properties.setProperty("property1", "changed");
         assertEquals("Old value of property1 was not expected", "value1", oldValue);
         compareProperties(SET_PROPERTY1);
     }
     
     
     public void testSetProperty4() {
         Object oldValue = properties.setProperty("property4", "changed");
         assertEquals("Old value of property4 was not expected", "value4", oldValue);
         compareProperties(SET_PROPERTY4);
     }
     
     
     public void testAddProperty5() {
         properties.setProperty("property5", "value5");
         compareProperties(ADD_PROPERTY5);
     }
     
     
     public void testRemoveProperty2() {
         properties.remove("property2");
         compareProperties(REMOVE_PROPERTY2);
     }
     
     
     public void testCommentProperty2() {
         boolean commented = properties.commentOutProperty("property2");
         assertTrue("Property was apparently not commented out", commented);
         compareProperties(COMMENT_PROPERTY2);
     }
     
     
     public void testUncommentProperty3() {
         boolean uncommented = properties.uncommentProperty("property3");
         assertTrue("Property was apparently not uncommented", uncommented);
         String value = properties.getProperty("property3");
         assertEquals("Value of property3 was not expected", "value3", value);
         compareProperties(UNCOMMENT_PROPERTY3);
     }
     
     
     public void testSetProperty3() {
         Object oldValue = properties.setProperty("property3", "changed");
         assertEquals("Old value of property3 was not expected", "value3", oldValue);
         compareProperties(SET_PROPERTY3);
     }
 }
