 package edu.northwestern.bioinformatics.bering.tools;
 
 import edu.northwestern.bioinformatics.bering.BeringTestCase;
 
 import java.net.URI;
 
 /**
  * @author Rhett Sutphin
  */
 public class UriToolsTest extends BeringTestCase {
     public void testResolveFileUri() throws Exception {
         URI file = new URI("file:/path/path/path/basename.ext");
         URI actual = UriTools.resolve(file, "different.ext");
         assertEquals("file:/path/path/path/different.ext", actual.toString());
     }
 
     public void testResolveNonOpaqueJarUri() throws Exception {
         URI jar = new URI("jar:/path/path/path/basename.ext");
         URI actual = UriTools.resolve(jar, "different.ext");
         assertEquals("jar:/path/path/path/different.ext", actual.toString());
     }
 
     public void testResolveJarFileUri() throws Exception {
         URI jarFile = URI.create(
             "jar:file:/path/path/path/diablo.jar!/db/migrate/001_out_the_door/001_add_frogs.groovy");
         URI actual = UriTools.resolve(jarFile, "extra.sql");
         assertEquals(
             "jar:file:/path/path/path/diablo.jar!/db/migrate/001_out_the_door/extra.sql",
             actual.toString()
         );
     }
 }
