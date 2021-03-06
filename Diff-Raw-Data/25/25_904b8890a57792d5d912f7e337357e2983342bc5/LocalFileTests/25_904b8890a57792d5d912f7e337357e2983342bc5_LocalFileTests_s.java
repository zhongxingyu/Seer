 package nl.esciencecenter.octopus.adaptors.local;
 
 import static org.junit.Assert.*;
 
 import java.net.URI;
 
 import junit.framework.Assert;
 
 import nl.esciencecenter.octopus.Octopus;
 import nl.esciencecenter.octopus.OctopusFactory;
 import nl.esciencecenter.octopus.files.FileSystem;
 import nl.esciencecenter.octopus.files.AbsolutePath;
 import nl.esciencecenter.octopus.files.RelativePath;
 
 public class LocalFileTests {
 
     @org.junit.Test
     public void test1() throws Exception {
         Octopus octopus = OctopusFactory.newOctopus(null);
 
         String tmpdir = System.getProperty("java.io.tmpdir");
         
         System.err.println("tmpdir = " + tmpdir);
 
        FileSystem fs = octopus.files().newFileSystem(new URI("local"), null, null);
         
         AbsolutePath path = octopus.files().newPath(fs, new RelativePath(tmpdir));
 
         Assert.assertTrue(octopus.files().exists(path));
 
         octopus.end();
 
     }
 
     @org.junit.Test
     public void test2() throws Exception {
         Octopus octopus = OctopusFactory.newOctopus(null);
 
        FileSystem fs = octopus.files().newFileSystem(new URI("local"), null, null);
         
         AbsolutePath tmpDir = octopus.files().newPath(fs, new RelativePath(System.getProperty("java.io.tmpdir")));
 
         System.err.println("tmpdir = " + tmpDir);
 
         assertTrue(octopus.files().exists(tmpDir));
 
         assertTrue(octopus.files().isDirectory(tmpDir));
 
         AbsolutePath sandboxDir = octopus.files().newPath(fs, new RelativePath(System.getProperty("java.io.tmpdir") + "/test-sandbox"));
 
         if (octopus.files().exists(sandboxDir)) {
             System.err.println("deleting " + sandboxDir);
             octopus.files().delete(sandboxDir);
         }
 
         assertFalse(octopus.files().exists(sandboxDir));
 
         octopus.files().createDirectory(sandboxDir);
 
         assertTrue(octopus.files().exists(sandboxDir));
 
         octopus.end();
 
     }
 
 }
