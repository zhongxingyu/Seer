 /**
  * Created by: Nahuel Barrios.
  * On: 31/10/12 at 12:14hs.
  */
 package org.nbempire.java.filerenamer;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Nahuel Barrios.
 * @since 0.2
  */
 @RunWith(value = SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:/applicationContext-componenteTest.xml")
 public class ApplicationComponentTest {
 
     private boolean rollback;
     private String rollbackPath;
     private String rollbackOutputPattern;
     private String rollbackInputPattern;
 
     @Before
     public void setUp() {
         rollback = false;
     }
 
     /**
      * Test method for main.
      */
     @Test(expected = IllegalArgumentException.class)
     public void main_withInvalidDirectoryPath_throwIllegalArgumentException() throws Exception {
         Application.main(new String[]{"any path", "%a - %t", "%t - %a"});
     }
 
     @Test
     public void main_withValidParameters_renameFiles() throws Exception {
         String fileSeparator = System.getProperty("file.separator");
 
         String path = System.getProperty("user.dir") + fileSeparator;
         path += "src" + fileSeparator + "test" + fileSeparator + "resources" + fileSeparator + "test1";
 
         String inputPattern = "%a - %t";
         String outputPattern = "%t - %a";
         Application.main(new String[]{path, inputPattern, outputPattern, "it doesn't matter"});
 
         //  Sets rollback=true inmediately after conversion because if it fails because of an assertion then it tearDown method does nothing.
         rollback = true;
 
         List<String> filesList = new ArrayList<String>();
         Collections.addAll(filesList, new File(path).list());
         Collections.sort(filesList);
         for (int index = 0; index < filesList.size(); ) {
             String eachFileName = filesList.get(index++);
             assertEquals("titulo" + index + " - artista" + index + ".mp3", eachFileName);
         }
 
         rollbackPath = path;
         rollbackOutputPattern = outputPattern;
         rollbackInputPattern = inputPattern;
     }
 
     @After
     public void tearDown() {
         if (rollback) {
             Application.main(new String[]{rollbackPath, rollbackOutputPattern, rollbackInputPattern, "it doesn't matter"});
         }
     }
 }
