 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.javavfs;
 
 /**
  *
  * @author mzk
  */
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Collections;
 import java.util.List;
 import org.javavfs.impl.nativefs.NativeFileSystemSession;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public abstract class AbstractDirectoryTest {
 
     public AbstractDirectoryTest(FileSystem filesystem) {
         this.filesystem=filesystem;
     }
 
     FileSystem filesystem;
     
      @After
     public void tearDown() throws IOException {
         clean();
     }
 
     private void clean() throws IOException{
         List<Node> nodes = filesystem.createSession(null).getRoot().getChildren();
         
         for(Node node:nodes){
             if(node.isDirectory()){
                 ((Directory)node).delete(true);
             } else {
                 node.delete();
             }
         }
     } 
     
     /**
      * Test of createDirectory method, of class Directory.
      */
     @Test
     public void testCreateDirectory() throws Exception {
         System.out.println("createDirectory");
         String name = "dir";
         Directory root = filesystem.createSession(null).getRoot();
         Directory result = root.createDirectory(name);
         assertNotNull(result);
         
         assertEquals("dir", result.getName());
 
         Path expectedPath = root.getPath();
         expectedPath.addLevel("dir");
 
         Path resultPath = result.getPath();
         assertEquals(expectedPath, resultPath);
         result.delete();
     }
 
    @Test
    public void testCreateBundle() throws Exception {
        System.out.println("createBundle");
        String name = "Safari.app";
        Directory root = filesystem.createSession(null).getRoot();
        Directory result = root.createDirectory(name);
        assertNotNull(result);

        assertTrue(result.isBundle());
        assertEquals("Safari", result.getBaseName());
        assertEquals("app", result.getSuffix());

        Path expectedPath = root.getPath();
        expectedPath.addLevel("Safari.app");

        Path resultPath = result.getPath();
        assertEquals(expectedPath, resultPath);
        result.delete();
    }

     /**
      * Test of createFile method, of class Directory.
      */
     @Test
     public void testCreateFile() throws Exception {
         System.out.println("createFile");
         String name = "file";
         Directory root = filesystem.createSession(null).getRoot();
         File result = root.createFile(name);
         assertNotNull(result);
         
         assertEquals("file", result.getName());
 
         Path expectedPath = root.getPath();
         expectedPath.addLevel("file");
 
         Path resultPath = result.getPath();
         assertEquals(expectedPath, resultPath);
 
         result.delete();
     }
 
     /**
      * Test of hasChild method, of class Directory.
      */
     @Test
     public void testHasChild() throws Exception {
         System.out.println("hasChild");
         String name = "tmp";
         Directory instance = filesystem.createSession(null).getRoot();
         File tmp = instance.createFile(name);
         boolean expResult = true;
         boolean result = instance.hasChild(name);
         assertEquals(expResult, result);
 
         tmp.delete();
     }
 
     /**
      * Test of hasFile method, of class Directory.
      */
     @Test
     public void testHasFile() throws Exception {
         System.out.println("hasFile");
         String name = "tmp";
         Directory instance = filesystem.createSession(null).getRoot();
         File tmp = instance.createFile(name);
         boolean expResult = true;
         boolean result = instance.hasFile(name);
         assertEquals(expResult, result);
 
         tmp.delete();
     }
 
     /**
      * Test of hasDirectory method, of class Directory.
      */
     @Test
     public void testHasDirectory() throws Exception {
         System.out.println("hasDirectory");
         String name = "tmp";
         Directory instance = filesystem.createSession(null).getRoot();
         Directory tmp = instance.createDirectory(name);
         boolean expResult = true;
         boolean result = instance.hasDirectory(name);
         assertEquals(expResult, result);
 
         tmp.delete();
     }
 
     /**
      * Test of getChild method, of class Directory.
      */
     @Test
     public void testGetChild() throws Exception {
         System.out.println("getChild");
         String name = "tmp";
         Directory instance = filesystem.createSession(null).getRoot();
         Node tmp = instance.createFile(name);
         
         Node result = instance.getChild(name);
         assertEquals(tmp, result);
         
         tmp.delete();
     }
 
     /**
      * Test of getFile method, of class Directory.
      */
     @Test
     public void testGetFile_String() throws Exception {
         System.out.println("getFile");
         String name = "tmp";
         Directory instance = filesystem.createSession(null).getRoot();
         File tmp = instance.createFile(name);
         
         File result = instance.getFile(name);
         assertEquals(tmp, result);
         
         tmp.delete();
     }
 
     /**
      * Test of getDirectory method, of class Directory.
      */
     @Test
     public void testGetDirectory_String() throws Exception {
         System.out.println("getDirectory");
         String name = "tmp";
         Directory instance = filesystem.createSession(null).getRoot();
         Directory tmp = instance.createDirectory(name);
         
         Directory result = instance.getDirectory(name);
         assertEquals(tmp, result);
         
         tmp.delete();
     }
 
     /**
      * Test of getFile method, of class Directory.
      */
     @Test
     public void testGetFile_String_boolean() throws Exception {
         System.out.println("getFile");
         String name = "tmp";
         Directory instance = filesystem.createSession(null).getRoot();
         File tmp = instance.getFile(name,true);
         
         File result = instance.getFile(name);
         assertEquals(tmp, result);
         
         tmp.delete();
     }
 
     /**
      * Test of getDirectory method, of class Directory.
      */
     @Test
     public void testGetDirectory_String_boolean() throws Exception {
         System.out.println("getDirectory");
         String name = "tmp";
         Directory instance = filesystem.createSession(null).getRoot();
         Directory tmp = instance.getDirectory(name,true);
         
         Directory result = instance.getDirectory(name);
         assertEquals(tmp, result);
         
         tmp.delete();
     }
 
     /**
      * Test of getChildren method, of class Directory.
      */
     @Test
     public void testGetChildren_0args() throws Exception {
         System.out.println("getChildren");
         Directory instance = filesystem.createSession(null).getRoot();
         Directory dir = instance.createDirectory("dir");
         dir.createFile("file1");
         dir.createFile("file2");
         dir.createFile("file3");
         
         List<Node> result = dir.getChildren();
         assertEquals(3, result.size());
 
         Collections.sort(result);
         assertEquals("file1", result.get(0).getName());
         assertEquals("file2", result.get(1).getName());
         assertEquals("file3", result.get(2).getName());
         
         dir.delete(true);
 
     }
 
     /**
      * Test of getDirectories method, of class Directory.
      */
     @Test
     public void testGetDirectories_0args() throws Exception {
         System.out.println("getDirectories");
         Directory instance = filesystem.createSession(null).getRoot();
         Directory dir = instance.createDirectory("dir");
         dir.createFile("file1");
         dir.createFile("file2");
         dir.createFile("file3");
         dir.createDirectory("dir1");
         
         List<Directory> result = dir.getDirectories();
         assertEquals(1, result.size());
 
         assertEquals("dir1", result.get(0).getName());
         
         dir.delete(true);
     }
 
     /**
      * Test of getFiles method, of class Directory.
      */
     @Test
     public void testGetFiles_0args() throws Exception {
         System.out.println("getFiles");
         Directory instance = filesystem.createSession(null).getRoot();
         Directory dir = instance.createDirectory("dir");
         dir.createFile("file1");
         dir.createFile("file2");
         dir.createFile("file3");
         dir.createDirectory("dir1");
         
         List<File> result = dir.getFiles();
         Collections.sort(result);
         
         assertEquals(3, result.size());
 
         assertEquals("file1", result.get(0).getName());
         
         dir.delete(true);
     }
 
     /**
      * Test of getChildren method, of class Directory.
      */
     @Test
     public void testGetChildren_NodeFilter() throws Exception {
         System.out.println("getChildren");
         Directory instance = filesystem.createSession(null).getRoot();
         Directory dir = instance.createDirectory("dir");
         dir.createFile("file1");
         dir.createFile("file2");
         dir.createFile("file3");
         dir.createDirectory("dir1");
         
         List<Node> result = dir.getChildren(new NodeFilter() {
             public boolean accept(Node node) {
                 if(node.getName().equals("file1") || node.getName().equals("dir1"))
                     return true;
                 return false;
             }
         });
         assertEquals(2, result.size());
 
 
         
         dir.delete(true);
         
     }
 
     /**
      * Test of getDirectories method, of class Directory.
      */
     @Test
     public void testGetDirectories_NodeFilter() throws Exception {
         System.out.println("getDirectories");
         Directory instance = filesystem.createSession(null).getRoot();
         Directory dir = instance.createDirectory("dir");
         dir.createFile("file1");
         dir.createFile("file2");
         dir.createDirectory("dir1");
         dir.createDirectory("dir2");
         
         List<Directory> result = dir.getDirectories(new NodeFilter() {
             public boolean accept(Node node) {
                 if(node.getName().equals("dir2"))
                     return true;
                 return false;
             }
         });
         assertEquals(1, result.size());
         
         dir.delete(true);
 
     }
 
     /**
      * Test of getFiles method, of class Directory.
      */
     @Test
     public void testGetFiles_NodeFilter() throws Exception{
         System.out.println("getFiles");
         Directory instance = filesystem.createSession(null).getRoot();
         Directory dir = instance.createDirectory("dir");
         dir.createFile("file1");
         dir.createFile("file2");
         dir.createDirectory("dir1");
         dir.createDirectory("dir2");
         
         List<File> result = dir.getFiles(new NodeFilter() {
             public boolean accept(Node node) {
                 if(node.getName().equals("file2"))
                     return true;
                 return false;
             }
         });
         assertEquals(1, result.size());
         
         dir.delete(true);
     }
 
 
 }
