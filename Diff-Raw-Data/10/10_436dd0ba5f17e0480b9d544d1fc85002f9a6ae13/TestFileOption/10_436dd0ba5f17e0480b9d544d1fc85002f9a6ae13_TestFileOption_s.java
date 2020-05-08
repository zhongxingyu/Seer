 package org.hackystat.sensor.xmldata.option;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hackystat.sensor.xmldata.XmlDataController;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Tests if the FileOption operates as intended.
  * @author aito
  * 
  */
 public class TestFileOption {
   /** Tests if isValid returns false when no arguments are specified. */
   @Test
   public void testNoFileArguments() {
     XmlDataController controller = new XmlDataController();
     Option fileOption = OptionFactory.getInstance(controller, FileOption.OPTION_NAME,
         new ArrayList<String>());
     Assert.assertFalse("A file option should have at least 1 file argument.", fileOption
         .isValid());
   }
 
   /** Tests if isValid returns true if one file is specified. */
   @Test
   public void testOneFileArgument() {
     XmlDataController controller = new XmlDataController();
     List<String> parameters = new ArrayList<String>();
     String testPackage = "src/org/hackystat/sensor/xmldata/testdataset/";
    parameters.add(new File("") + testPackage + "testdata.xml");
     Option fileOption = OptionFactory.getInstance(controller, FileOption.OPTION_NAME,
         parameters);
     Assert.assertTrue("A file option with one file should be valid.", fileOption.isValid());
   }
 
   /** Tests if isValid returns true if more than one file is specified. */
   @Test
   public void testMultipleFileArguments() {
     XmlDataController controller = new XmlDataController();
     List<String> parameters = new ArrayList<String>();
     String testPackage = "src/org/hackystat/sensor/xmldata/testdataset/";
    parameters.add(new File("") + testPackage + "testdata.xml");
    parameters.add(new File("") + testPackage + "testdata2.xml");
     Option fileOption = OptionFactory.getInstance(controller, FileOption.OPTION_NAME,
         parameters);
     Assert.assertTrue("A file option with more than one file should be valid.", fileOption
         .isValid());
   }
 
   /** Tests if isValid returns false if a non-existant file is specified. */
   @Test
   public void testNonExistantFile() {
     XmlDataController controller = new XmlDataController();
     List<String> parameters = new ArrayList<String>();
     parameters.add("Test.xml");
     Option fileOption = OptionFactory.getInstance(controller, FileOption.OPTION_NAME,
         parameters);
     Assert.assertFalse("A non-existant file should invalidate this option.", fileOption
         .isValid());
   }
 }
