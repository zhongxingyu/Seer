 package org.doframework;
 
 import junit.framework.*;
 
 /**
 IMPT: You need this VM Param set before running this test
    -DDOF_DIR=/home/gordonju/dev/DOF/core/dof
  */
 public class DOFTest extends TestCase
 {
     public void testGetObjectFileInfoHandlesBasicName()
     {
         String fileToLoad = "customer.25.xml";
         ObjectFileInfo ofi = DOF.getObjectFileInfo(fileToLoad);
         assertEquals("customer", ofi.getObjectType());
         assertEquals("25", ofi.getPk());
         assertEquals("xml", ofi.getFileType());
     }
 
     public void testGetObjectFileInfoHandlesNameWithManyPeriods()
     {
         String fileToLoad = "customer.25.2.3.xml";
         ObjectFileInfo ofi = DOF.getObjectFileInfo(fileToLoad);
         assertEquals("customer", ofi.getObjectType());
         assertEquals("25.2.3", ofi.getPk());
         assertEquals("xml", ofi.getFileType());
     }
 
     public void testGetPropertiesErrorMessageWhenPropertiesFile()
     {
         if (DOF.DOF_DIR.length() == 0)
         {
             fail("Please define DOF_DIR when invoking the test runner. Use VM parameter -DDOF_DIR={directory to core/test_data}");
         }
        final String resourceAsString = DOF.getResourceAsString("test_data/product.40.xml");
         assertNotNull(resourceAsString);
     }
 
 
     public void testGetFileWhenUnderSubdirectoryUsingDofDefsDir()
     {
         if (DOF.DOF_DIR.length() == 0)
         {
            fail("Please define DOF_DIR when invoking the test runner. Use VM parameter -DDOF_DIR={directory to core/dof/test_data}");
         }
        final String filePath = "test_data/manufacturers/manufacturer.48.xml";
         final String resourceAsString = DOF.getResourceAsString(filePath);
         assertNotNull(resourceAsString);
         ObjectFileInfo ofi = DOF.getObjectFileInfo(filePath);
         assertEquals("manufacturer", ofi.getObjectType());
         assertEquals("48", ofi.getPk());
         assertEquals("xml", ofi.getFileType());
     }
 
 
     public void testGetFileWhenUnderSubdirectoryUsingClassPath()
     {
         String oldValue = "";
         if (DOF.DOF_DIR.length() > 0)
         {
             oldValue = System.setProperty("DOF_DIR", "");
         }
        final String filePath = "test_data/manufacturers/manufacturer.48.xml";
         final String resourceAsString = DOF.getResourceAsString(filePath);
         assertNotNull(resourceAsString);
         System.setProperty("DOF_DIR", oldValue);
     }
 
     public void testGetClassNameWorkWithRegexp()
     {
         String objectType = "ABC";
         String fileType = "xml";
         String className = HandlerMappings.getHandlerClassNameForObject(objectType, fileType);
         assertEquals("dof_xml_handler.GenericXmlFactory", className);
     }
 
     
 }
