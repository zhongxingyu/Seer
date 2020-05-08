 package cytoscape.data.readers.unitTests;
 
 import cytoscape.data.CyAttributesImpl;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.readers.CyAttributesReader;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 /**
  * Tests the CyAttributesReader Class.
  */
 public class CyAttributesReaderTest extends TestCase {
 
     /**
      * Testing Reading, Take 1.
      * @throws IOException IO Errors.
      */
     public void testRead1() throws IOException {
         String attributeName = "TestNodeAttribute1";
         CyAttributes cyAttributes = new CyAttributesImpl();
         File file =  new File ("testData/galFiltered.nodeAttrs1");
         FileReader reader = new FileReader(file);
         CyAttributesReader.loadAttributes(cyAttributes, reader);
         byte type = cyAttributes.getType(attributeName);
         assertEquals (CyAttributes.TYPE_INTEGER, type);
 
         //  Test a value
         Integer value = cyAttributes.getIntegerAttribute("YKR026C",
                 attributeName);
         assertEquals (1, value.intValue());
 
         //  Test a Second value
         value = cyAttributes.getIntegerAttribute("YMR043W",
                 attributeName);
         assertEquals (2, value.intValue());
 
         //  Test the last value in the file
         value = cyAttributes.getIntegerAttribute("YBR043C",
                 attributeName);
         assertEquals (3, value.intValue());
 
         //  Test a non-existent value
         value = cyAttributes.getIntegerAttribute("Nerius",
                 attributeName);
         assertTrue (value == null);
     }
 
     /**
      * Testing Reading, Take 2.
      * @throws IOException IO Errors.
      */
     public void testRead2() throws IOException {
         CyAttributes cyAttributes = new CyAttributesImpl();
         File file =  new File ("testData/galFiltered.edgeAttrs2");
         FileReader reader = new FileReader(file);
         CyAttributesReader.loadAttributes(cyAttributes, reader);
        byte type = cyAttributes.getType("TestNodeAttribute2");
         assertEquals (CyAttributes.TYPE_INTEGER, type);
 
         //  Test a value
         Integer value = cyAttributes.getIntegerAttribute("YKR026C (pp) YGL122C",
                 "TestNodeAttribute2");
         assertEquals (2, value.intValue());
 
         //  Test a Second value
         value = cyAttributes.getIntegerAttribute("YDR382W (pp) YFL029C",
                 "TestNodeAttribute2");
         assertEquals (3, value.intValue());
 
         //  Test the last value in the file
         value = cyAttributes.getIntegerAttribute("YBL026W (pp) YOR127C",
                 "TestNodeAttribute2");
         assertEquals (3, value.intValue());
 
         //  Test a non-existent value
         value = cyAttributes.getIntegerAttribute("Nerius",
                 "TestNodeAttribute2");
         assertTrue (value == null);
     }
 
     /**
      * Testing Read, Take 3
      * @throws IOException IO Errors.
      */
     public void testRead3() throws IOException {
         CyAttributes cyAttributes = new CyAttributesImpl();
 
         //  This file contains an explicit class declaration, like so:
         //  Score (class=Java.lang.Double)
         //  All the integer values should therefore be stored as Doubles.
         File file =  new File ("testData/explicitDouble.attribute");
         FileReader reader = new FileReader(file);
         CyAttributesReader.loadAttributes(cyAttributes, reader);
         byte type = cyAttributes.getType("Score");
         assertEquals (CyAttributes.TYPE_FLOATING, type);
 
         //  Test a value
         Double value = cyAttributes.getDoubleAttribute("a", "Score");
         assertEquals (1.0, value.doubleValue(), 0.01);
 
         //  Test the last value in the file
         value = cyAttributes.getDoubleAttribute("c", "Score");
         assertEquals (3.7, value.doubleValue(), 0.01);
     }
 
     /**
      * Testing Reading of Simple Lists.
      * @throws IOException IO Errors.
      */
     public void testReadSimpleLists() throws IOException {
         String attributeName = "GO_molecular_function_level_4";
         CyAttributes cyAttributes = new CyAttributesImpl();
         File file =  new File ("testData/implicitStringArray.attribute");
         FileReader reader = new FileReader(file);
         CyAttributesReader.loadAttributes(cyAttributes, reader);
         byte type = cyAttributes.getType(attributeName);
         assertEquals (CyAttributes.TYPE_SIMPLE_LIST, type);
 
         //  Test the First List
         List list = cyAttributes.getAttributeList("AP1G1",
                 attributeName);
         assertEquals (3, list.size());
         String value = (String) list.get(0);
         assertEquals ("intracellular", value);
         value = (String) list.get(1);
         assertEquals ("clathrin adaptor", value);
         value = (String) list.get(2);
         assertEquals ("intracellular transporter", value);
 
         //  Test the Last List
         list = cyAttributes.getAttributeList("CDH3", attributeName);
         assertEquals (1, list.size());
         value = (String) list.get(0);
         assertEquals ("cell adhesion molecule", value);
     }
 
     /**
      * Runs just this one unit test.
      */
     public static void main(String[] args) {
         junit.textui.TestRunner.run(CyAttributesReaderTest.class);
     }
 }
