 package org.burgers.maven.plugins;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.junit.After;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 public class DeleteMojoTest {
     DeleteMojo mojo;
     File file;
     File tempDir;
 
     @Before
     public void setUp() throws IOException {
         mojo = new DeleteMojo();
         file = File.createTempFile("testFile", ".txt");
         tempDir = file.getParentFile();
     }
 
     @Test
     public void execute_delete_file()throws MojoExecutionException {
         writeToFile(file, "hi");
         mojo.setFiles(new File[]{file});
         mojo.execute();
         assertTrue(!file.exists());
     }
 
     @Test
     public void execute_delete_directory()throws MojoExecutionException {
         File directory = new File(tempDir, "myDirectory");
         directory.mkdir();
         mojo.setFiles(new File[]{directory});
         mojo.execute();
         assertTrue(!directory.exists());
     }
 
     @Test
     public void execute_ignore_things_that_dont_exist()throws MojoExecutionException {
         File directory = new File(tempDir, "myDirectory");
         File otherFile = new File(tempDir, "testFile.txt");
         mojo.setFiles(new File[]{directory, otherFile});
         mojo.execute();
         assertTrue(!directory.exists());
         assertTrue(!otherFile.exists());
     }
 
     @Test
     public void execute_everything_within_a_directory_including_sub_directories()throws MojoExecutionException {
         File grandParentDir = new File(tempDir, "grandparent");
         grandParentDir.mkdir();
 
         File grandParentFile = new File(grandParentDir, "grandparent.txt");
         writeToFile(grandParentFile, "a");
 
         File parentDir = new File(grandParentDir, "parent");
         parentDir.mkdir();
 
         File parentFile = new File(parentDir, "parent.txt");
         writeToFile(parentFile, "b");
 
         File childDir = new File(parentDir, "child");
         childDir.mkdir();
 
         File childFile = new File(childDir, "child.txt");
         writeToFile(childFile, "c");
 
        mojo.setFiles(new File[]{grandParentDir});
         mojo.execute();
         assertTrue(!grandParentDir.exists());
         assertTrue(!grandParentFile.exists());
         assertTrue(!parentDir.exists());
         assertTrue(!parentFile.exists());
         assertTrue(!childDir.exists());
         assertTrue(!childFile.exists());
     }
 
     public void writeToFile(File file, String text){
         FileOutputStream stream = null;
         try {
             stream = new FileOutputStream(file);
             stream.write(text.getBytes());
             stream.close();
         }catch (IOException e){
         }
     }
 
     @After
     public void tearDown() {
         file.delete();
     }
 }
