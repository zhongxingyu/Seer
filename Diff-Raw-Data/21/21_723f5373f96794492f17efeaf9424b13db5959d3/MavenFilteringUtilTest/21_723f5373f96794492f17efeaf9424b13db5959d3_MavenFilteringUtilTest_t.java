 package com.github.mikesena.maven.plugins.environments;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.codehaus.plexus.util.FileUtils;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * JUnit test for the {@link MavenFilteringUtil} class.
  * 
  * @author msena
  * 
  */
 public final class MavenFilteringUtilTest {
     /** Variable replacements. */
     private Properties properties;
 
     /** Test directory, used for placing temporary files. */
     private File testDir;
 
     /**
      * Prepare for unit testing.
      * 
      * @throws IOException
      */
     @Before
     public void setUp() throws IOException {
         properties = new Properties();
         properties.put("env.name", "env1");
         testDir = new File("target", "filename-filtering");
         FileUtils.deleteDirectory(testDir);
         testDir.mkdir();
     }
 
     @Test
     public void testFilenameFilteringInASubFolder() throws IOException {
         final File subDir1 = new File(testDir, "sub-folder");
         if (subDir1.exists()) {
             subDir1.delete();
         }
         subDir1.mkdir();
         doFilenameFilterTest("file4-env1.txt", new File(subDir1, "file4-${env.name}.txt"));
     }
 
     @Test
     public void testFilenameFilteringWithMultipleParameters() throws IOException {
         properties.put("env.name", "env1");
         properties.put("project.version", "0.1.2");
         doFilenameFilterTest("file2-env1-0.1.2.txt", new File(testDir, "file2-${env.name}-${project.version}.txt"));
     }
 
     @Test
    public void testFilenameFilteringWithMissingParameter() throws IOException {
        doFilenameFilterTest("file2-${env.fake}.txt", new File(testDir, "file2-${env.fake}.txt"));
    }

    @Test
     public void testFilenameFilteringWithNoParameters() throws IOException {
         properties.put("env.name", "env1");
         doFilenameFilterTest("file3.txt", new File(testDir, "file3.txt"));
     }
 
     private void doFilenameFilterTest(final String expected, final File actual) throws IOException {
         actual.createNewFile();
         MavenFilteringUtil.doFilenameFiltering(testDir, properties);
         assertTrue(new File(actual.getParent(), expected).exists());
         if (!actual.getName().equals(expected)) {
             assertFalse(actual.exists());
         }
     }
 }
