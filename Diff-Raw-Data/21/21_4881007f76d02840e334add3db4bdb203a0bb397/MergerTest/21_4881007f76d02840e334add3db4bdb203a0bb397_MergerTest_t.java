 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package serempre.codegen.metamodel.parsers;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.net.URISyntaxException;
 import java.util.Iterator;
 import java.util.Stack;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.ParserConfigurationException;
 import org.apache.velocity.Template;
 import org.apache.velocity.app.Velocity;
 import org.xml.sax.SAXException;
 import serempre.codegen.metamodel.Merger;
 import static resources.ResourceUtility.*;
 import org.junit.*;
 import static org.junit.Assert.*;
 import serempre.codegen.metamodel.ClassDescriptor;
 /**
  *
  * @author Thor
  */
 public class MergerTest extends Merger {
 
     protected java.util.Stack<String> mFilesToDelete = new Stack<String>();
     protected boolean mDeleteTargetFiles = false;
     
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
         
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
         if(mDeleteTargetFiles){
             while(!mFilesToDelete.empty()){
                 File toDelete = new File(mFilesToDelete.pop());
                 toDelete.deleteOnExit();
             }
         }
     }
     
     @Override
     public void merge(ModelParser domain, String outputPath) throws ParserConfigurationException, SAXException, IOException {
         //template location is regarded as the template's name
        String templateName = getTemplateName();
         //prepare to iterate through all classes in this business domain
         Iterator<String> classes = null;
         //get an iterator for the classes in the business domain
         classes = domain.getClasses().keySet().iterator();
         while(classes.hasNext()){
             //get a class from the businss domain
             String key = classes.next();
             ClassDescriptor classDescriptor = domain.getClasses().get(key);
             //if the test template is unavalable, then fail this test
             if(!Velocity.resourceExists(templateName)){
                 fail();
             }
             //build a template using the test template file
             Template template = Velocity.getTemplate(templateName);
             //prepare a buffer to write to an output file
             BufferedWriter writer = null;
             //register transformed class so that it becomes available to the template
             mVelocityContext.put("class", classDescriptor);
             //make sure output folder exists
            buildTargetPath(getPathToFile(outputPath));
             //build a path to the resulting file
             String outputFilePath = classDescriptor.getClassName();
            outputFilePath = outputPath+ System.getProperty("file.separator") +classDescriptor.getClassName()+".java";
             outputFilePath = getPathToFile(outputFilePath);
             try{
                 //schedule generated file for deletion
                 mFilesToDelete.push(outputFilePath);
                 //generate code using target emplate
                 writer = new BufferedWriter(new FileWriter(mFilesToDelete.peek()));
                 //write code into generated file
                 template.merge(mVelocityContext, writer);
                 
             }
             finally{
                 if(writer!=null){
                     writer.flush();
                     writer.close();
                 }
             }
         }
     }
     
     @Test
     public void testMerge(){
         MergerTest test = new MergerTest();
         try {
             test.init();
         } catch (URISyntaxException ex) {
             Logger.getLogger(MergerTest.class.getName()).log(Level.SEVERE, null, ex);
             fail();
         }
         try {
             test.start("SampleModel3.xml", "output");
             String [] expectedFilenames = new String[]{ ".java","2.java" };
             Iterator<String> producedFiles = mFilesToDelete.iterator();
             int index = 0;
             while(producedFiles.hasNext()){
                 String file = producedFiles.next();
                 assertEquals(expectedFilenames[index++], file);
             }
         } catch (ParserConfigurationException ex) {
             Logger.getLogger(MergerTest.class.getName()).log(Level.SEVERE, null, ex);
             fail();
         } catch (SAXException ex) {
             Logger.getLogger(MergerTest.class.getName()).log(Level.SEVERE, null, ex);
             fail();
         } catch (IOException ex) {
             Logger.getLogger(MergerTest.class.getName()).log(Level.SEVERE, null, ex);
             fail();
         }
     }
     
     @Test
     public void testMerge2(){
         MergerTest test = new MergerTest();
         try {
             test.init();
         } catch (URISyntaxException ex) {
             Logger.getLogger(MergerTest.class.getName()).log(Level.SEVERE, null, ex);
             fail();
         }
         try {
             test.mVelocityContext.put("packagename", "hive.models");
             test.start("Orders.xml", "output");
         } catch (ParserConfigurationException ex) {
             Logger.getLogger(MergerTest.class.getName()).log(Level.SEVERE, null, ex);
             fail();
         } catch (SAXException ex) {
             Logger.getLogger(MergerTest.class.getName()).log(Level.SEVERE, null, ex);
             fail();
         } catch (IOException ex) {
             Logger.getLogger(MergerTest.class.getName()).log(Level.SEVERE, null, ex);
             fail();
         }
     }
 
     @Override
     public String getTemplateName() {
         //template location is regarded as the template's name
         String templateName = "resources/jme/class.vsl";
         return templateName;
     }
 
     @Override
     public boolean bulkTransformation() {
         return false;
     }
 
     @Override
     public String getPackageName() {
         return "hive.models";
     }
 
     @Override
     public String getTargetFileExtension() {
         return "java";
     }
 }
