 package net.contextfw.web.application.internal.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import net.contextfw.web.application.WebApplicationException;
 
 import org.junit.Test;
 
 public class ResourceScannerTest {
 
     @Test
     public void testValidRoots() {
         List<String> paths = new ArrayList<String>();
         
         paths.add("net.contextfw.web");
         paths.add("file:foo/bar");
         paths.add("classpath:net/contextfw/web");
         
         List<URI> roots = ResourceScanner.toURIs(paths);
         
         assertEquals("classpath:net/contextfw/web", roots.get(0).toString());
         assertEquals("file:foo/bar", roots.get(1).toString());
         assertEquals("classpath:net/contextfw/web", roots.get(2).toString());
     }
     
     @Test(expected=WebApplicationException.class)
     public void testInvalidRoots() {
         List<String> paths = new ArrayList<String>();
         paths.add("http://www.contextfw.net");
         ResourceScanner.toURIs(paths);
     }
     
     @Test
     public void testRootFiles() {
         
         List<String> paths = new ArrayList<String>();
         
         paths.add("net.contextfw.web");
         paths.add("javax.servlet");
         
         paths.add("file:src/main/resources");
         
         // This is a duplicate of the first package and should get ignored
         paths.add("classpath:net/contextfw/web");
         
         List<ResourceEntry> rootFiles = AbstractScanner.findResourceEntries(paths);
        assertEquals(269, rootFiles.size());
         
 //        assertTrue(rootFiles.get(0).getAbsolutePath().endsWith("/target/test-classes/net/contextfw/web"));
 //        assertTrue(rootFiles.get(1).getAbsolutePath().endsWith("/target/classes/net/contextfw/web"));
 //        assertTrue(rootFiles.get(2).getAbsolutePath().endsWith("/src/main/resources"));
     }
     
     @Test
     public void testFindResources() {
         List<String> paths = new ArrayList<String>();
         paths.add("net.contextfw.web.application.internal.util");
         
 //        List<File> files = ResourceScanner.findResources(paths, 
 //                Pattern.compile(".*\\.class"));
 //        
 //        for (File file : files) {
 //            System.out.println(file.getName());
 //        }
     }
 }
