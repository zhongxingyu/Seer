 package org.rhino.js.dependencies.io;
 
 import org.junit.Test;
 import org.rhino.js.dependencies.models.JsFile;
 
 import java.io.File;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 public class TestJsTreeFiles {
 
     @Test
     public void testWalkTree() {
         String dir = "src/test";
         List<File> files = JsTreeFiles.getFiles(dir);
        assertEquals(5, files.size());
 
         List<JsFile> jsFiles = JsTreeFiles.getJsFiles(dir);
         assertEquals(files.size(), jsFiles.size());
         for (JsFile eachJsFile : jsFiles) {
             assertNotNull(eachJsFile.getFile());
         }
 
     }
 }
