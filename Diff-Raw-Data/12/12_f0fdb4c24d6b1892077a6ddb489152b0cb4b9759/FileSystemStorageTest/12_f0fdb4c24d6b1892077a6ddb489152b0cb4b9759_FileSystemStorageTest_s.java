 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Persist;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author anasalkhatib
  */
 public class FileSystemStorageTest {
     private PersistentStorage instance;
     private String mailboxID;
     private String userHome;
     private String testDirectory;
 
     void delete(File f) throws IOException {
         if (f.isDirectory()) {
             for (File c : f.listFiles()) {
                 delete(c);
             }
         }
         if (!f.delete()) {
             throw new FileNotFoundException("Failed to delete file: " + f);
         }
     }
 
     public FileSystemStorageTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
 
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
         mailboxID = "junit";
         userHome = System.getProperty("user.home") + File.separator;
         testDirectory = userHome + "_mailbox" + File.separator + mailboxID + File.separator;
         instance = PersistentStorage.getFileSystemStorage(mailboxID);
     }
 
     @After
     public void tearDown() {
         File junitDirectory = new File(testDirectory);
         try {
             delete(junitDirectory);
         } catch (IOException ex) {
             Logger.getLogger(FileSystemStorageTest.class.getName()).log(Level.SEVERE, null, ex);
             fail("IO Problem");
         }
     }
 
     /**
      * Test of newFolder method, of class FileSystemStorage.
      */
     @Test
     public void testNewFolder() {
         boolean result = instance.newFolder(mailboxID + File.separator + "newFolder");
         assertEquals(true, result);
     }
 
     /**
      * Test of newFolderInMailbox method, of class FileSystemStorage.
      */
     @Test
     public void testNewFolderInMailbox() {
        String newFolderPath = "Inbox" + File.separator + "junit";
         boolean result = instance.newFolderInMailbox(newFolderPath);
         assertEquals(true, result);
     }
 
     /**
      * Test of loadMessageListFromFolder method, of class FileSystemStorage.
      */
     @Test
     public void testLoadMessageListFromFolder() {
         System.out.println("loadMessageListFromFolder");
         String folder = "";
         ArrayList<String> expResult = new ArrayList<String>();
         ArrayList<String> result = instance.loadMessageListFromFolder(folder);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of loadSubfolders method, of class FileSystemStorage.
      */
     @Test
     public void testLoadSubfolders() {
         System.out.println("loadSubfolders");
         String folder = "Inbox";
         Set<String> emptySet = null;
         Set<String> result = (Set<String>) instance.loadSubfolders(folder);
         assertSame(emptySet, result);
     }
 
     /**
      * Test of deleteFolderAndAllContents method, of class FileSystemStorage.
      */
     @Test
     public void testDeleteFolder() {
         System.out.println("deleteFolder");
         String folder = "";
         FileSystemStorage instance = null;
         instance.deleteFolderAndAllContents(folder);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of moveMessageToFolder method, of class FileSystemStorage.
      */
     @Test
     public void testMoveMessageToFolder() {
         System.out.println("moveMessageToFolder");
         String message = "";
         String folder = "";
         FileSystemStorage instance = null;
         instance.moveMessageToFolder(message, folder);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of newMessage method, of class FileSystemStorage.
      */
     @Test
     public void testNewMessage() {
         System.out.println("newMessage");
         String folder = "";
         FileSystemStorage instance = null;
         instance.newMessage(folder);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of saveMessage method, of class FileSystemStorage.
      */
     @Test
     public void testSaveMessage() {
         System.out.println("saveMessage");
         String message = "";
         String content = "";
         FileSystemStorage instance = null;
         instance.saveMessage(message, content);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of loadMessage method, of class FileSystemStorage.
      */
     @Test
     public void testLoadMessage() {
         System.out.println("loadMessage");
         String message = "";
         FileSystemStorage instance = null;
         String expResult = "";
         String result = instance.loadMessage(message);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of deleteMessage method, of class FileSystemStorage.
      */
     @Test
     public void testDeleteMessage() {
         System.out.println("deleteMessage");
         String message = "";
         FileSystemStorage instance = null;
         instance.deleteMessage(message);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of moveFolder method, of class FileSystemStorage.
      */
     @Test
     public void testMoveFolder() {
         System.out.println("moveFolder");
         String folderToMove = "";
         String destinationFolder = "";
         FileSystemStorage instance = null;
         instance.moveFolder(folderToMove, destinationFolder);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 }
