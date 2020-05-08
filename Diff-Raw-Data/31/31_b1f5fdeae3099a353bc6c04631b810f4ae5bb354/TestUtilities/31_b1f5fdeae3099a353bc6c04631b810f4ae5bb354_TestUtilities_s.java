 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.jan.keysaver.utilities;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import net.jan.keysaver.sources.Utilities;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author janhorak
  */
 public class TestUtilities {
     
     public TestUtilities() {
     }
     
     @Test
     public void testHash(){
        String test = "test";
         String hashedTest = new Utilities().getHash(test);
        String test2 = "test2";
         assertTrue(new Utilities().getHash(test).equals(hashedTest));
         assertTrue(!test2.equals(hashedTest));
     }
     
     
     @Test
     public void shouldGenerateAZipFile(){
         String path = "test.zip";
         
         File structure = new File("AppData/structure.xml");
         File key = new File("AppData/private.key");
         List<String> fileList = new ArrayList<String>();
         fileList.add(key.getAbsolutePath());
         fileList.add(structure.getAbsolutePath());
         
         new Utilities().generateZip(path, fileList);
         ZipFile file = null;
         try {
             file = new ZipFile(new File(path));
         } catch (ZipException ex) {
             Logger.getLogger(TestUtilities.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(TestUtilities.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         assertTrue(file.entries().hasMoreElements());
         assertTrue(file.getEntry("structure.xml").getSize() > 0);
         
         File f = new File(path);
         f.delete();
     }
     
     
     @Test
     public void shouldReturnedTheFilesFromFolderAndCreateZip(){
         List<String> resultList = new ArrayList<String>();
         resultList = Utilities.getFilePathesFromFolder("AppData/Images/intern");
         assertTrue(!resultList.isEmpty());
         Utilities.generateZip("images.zip", resultList);
         ZipFile file = null;
         try {
             file = new ZipFile(new File("images.zip"));
         } catch (ZipException ex) {
             Logger.getLogger(TestUtilities.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(TestUtilities.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         assertTrue(file.entries().hasMoreElements());
         
         File f = new File("images.zip");
         f.delete();
     }
     
 
 }
