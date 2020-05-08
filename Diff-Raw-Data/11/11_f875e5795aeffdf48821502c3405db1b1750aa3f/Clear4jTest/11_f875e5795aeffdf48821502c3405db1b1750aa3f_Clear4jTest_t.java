 package clear4j;
 
 import clear4j.processors.FileUtils;
 import junit.framework.Assert;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 /**
  * User: alexb
  * Date: 24/05/12
  * Time: 09:23
  */
 public class Clear4jTest {
 
     @BeforeClass
     public static void setup(){
         FileUtils.writeTextToFile(TestConfig.TEST_FILE_PATH.getValue(), TestConfig.TEST_FILE_CONTENT.getValue());
     }
 
     @AfterClass
     public static void tearDown(){
         FileUtils.removeFile(TestConfig.TEST_FILE_PATH.getValue());
     }
 
     @Test
     public void testSimpleWorkFlow() throws Exception {
 
         // start the workflow process
         String filePath = TestConfig.TEST_FILE_PATH.getValue();
         Workflow workflow = new Workflow(filePath, Functions.loadText(), Functions.println());
         Clear.run(workflow);
 
         // wait for the result
         String text1 = workflow.waitFor();
 
         // load it the boring way
         String text2 = FileUtils.loadTextFromFile(TestConfig.TEST_FILE_PATH.getValue());
 
         // assert same content
         Assert.assertEquals(text2, text1);
     	
     }
    
    //TODO remote processors in 2 jvms with one final processor somewhere.
 
 }
