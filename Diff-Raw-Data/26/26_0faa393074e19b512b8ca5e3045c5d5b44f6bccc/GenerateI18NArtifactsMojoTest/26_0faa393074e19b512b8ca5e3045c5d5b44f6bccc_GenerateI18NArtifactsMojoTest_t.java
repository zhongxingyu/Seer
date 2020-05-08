 /***************************************************
  *
  * cismet GmbH, Saarbruecken, Germany
  *
  *              ... and it just works.
  *
  ****************************************************/
 /*
  *  Copyright (C) 2010 mscholl
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.cismet.cids.mavenplugin;
 
 import java.io.File;
 import java.io.FileFilter;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.maven.model.Build;
 import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.DefaultMavenProjectHelper;
 import org.apache.maven.project.MavenProject;
 import org.junit.Ignore;
 
 import org.junit.Test;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  $Revision$, $Date$
  */
 public class GenerateI18NArtifactsMojoTest extends AbstractMojoTestCase {
 
     //~ Methods ----------------------------------------------------------------
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @Test
     @Ignore
     public void testExecute() throws Exception {
        final File testpom = new File(getBasedir(), "src/test/resources/de/cismet/cids/mavenplugin/testpom.xml");

        final MavenProject project = new MavenProject();
        final DefaultMavenProjectHelper helper = new DefaultMavenProjectHelper();
        final Build build = new Build();
        build.setFinalName("testArtifact-1.0");
        build.setDirectory(getBasedir() + "/target/GenerateI18NArtifactsMojoTest");
        project.setBuild(build);
 
        final GenerateI18NArtifacts mojo = (GenerateI18NArtifacts) lookupMojo("generate-i18n", testpom);
 
         assertNotNull("mojo is null", mojo);
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "projectHelper", helper);

        mojo.execute();
     }
 
     /**
      * DOCUMENT ME!
      */
     public void testGenerateLocalisedJar() throws Exception {
 
         final File testpom = new File(getBasedir(), "src/test/resources/de/cismet/cids/mavenplugin/testpom.xml");
 
         // set up the mojo
         final MavenProject project = new MavenProject();
         final Build build = new Build();
         build.setFinalName("testArtifact-1.0");
         build.setDirectory(getBasedir() + "/target/GenerateI18NArtifactsMojoTest");
         project.setBuild(build);
         final GenerateI18NArtifacts mojo = (GenerateI18NArtifacts) lookupMojo("generate-i18n", testpom);
         assertNotNull("mojo is null", mojo);
         setVariableValueToObject(mojo, "project", project);
 
         final Method method = mojo.getClass().getDeclaredMethod("generateLocalisedJar", Locale.class, Array.newInstance(File.class, 0).getClass());
         method.setAccessible(true);
         final Method getAllFilesMethod = mojo.getClass().getDeclaredMethod("getAllFiles", File.class, FileFilter.class, boolean.class);
         getAllFilesMethod.setAccessible(true);
 
         
 
         File root = new File("src/test/resources/teststructure");
         File[] files = (File[])getAllFilesMethod.invoke(mojo, root, null, true); // get every file below the testroot
 
         Locale locale = null;
         setVariableValueToObject(mojo, "defaultLocale", null);
         File result = null;
         
         
         // default locale && parameter locale are null resulting in an exception
         try {
             result = (File)method.invoke(mojo, locale, files);
             fail("expected IllegalArgumentException");
         } catch (final InvocationTargetException e) {
             // expected IllegalArgumentExcpetion
             if (!(e.getCause() instanceof IllegalArgumentException)) {
                 fail("expected IllegalArgumentException");
             }
         }
 
         locale = new Locale("de", "DE");
         result = (File)method.invoke(mojo, locale, files);
         assertEquals("testArtifact-1.0-de_DE.jar", result.getName());
 
 
         locale = new Locale("en", "GB");
         result = (File)method.invoke(mojo, locale, files);
         assertEquals("testArtifact-1.0-en_GB.jar", result.getName());
 
         try {
             locale = new Locale("es", "ES");
             result = (File)method.invoke(mojo, locale, files);
             fail("expected IllegalArgumentException");
         } catch (final InvocationTargetException e) {
             // expected IllegalArgumentExcpetion
             if (!(e.getCause() instanceof IllegalArgumentException)) {
                 fail("expected IllegalArgumentException");
             }
         }
 
     }
 
     /**
      * Testing private method chRoot.
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @Test
     public void testChRoot() throws Exception {
         final File testpom = new File(getBasedir(), "src/test/resources/de/cismet/cids/mavenplugin/testpom.xml");
 
         final GenerateI18NArtifacts mojo = (GenerateI18NArtifacts) lookupMojo("generate-i18n", testpom);
         assertNotNull("mojo is null", mojo);
 
         final Method method = mojo.getClass().getDeclaredMethod("chRoot", String.class, String.class);
         method.setAccessible(true);
 
         try {
             method.invoke(mojo, null, null);
             fail("expected IllegalArgumentException");
         } catch (final InvocationTargetException e) {
             // expected IllegalArgumentExcpetion
             if (!(e.getCause() instanceof IllegalArgumentException)) {
                 fail("expected IllegalArgumentException");
             }
         }
 
         String result = (String) method.invoke(mojo, null, "c:\\a\\b\\c\\d");
         assertEquals("c:/a/b/c/d", result);
 
         result = (String) method.invoke(mojo, null, "/a/b/c/d");
         assertEquals("a/b/c/d", result);
 
         result = (String) method.invoke(mojo, "\\a\\b\\", "c:\\a\\b\\c\\d");
         assertEquals("c:/a/b/c/d", result);
 
         result = (String) method.invoke(mojo, "a/b/", "/a/b/c/d");
         assertEquals("a/b/c/d", result);
 
         result = (String) method.invoke(mojo, "c:\\a\\b\\", "c:\\a\\b\\c\\d");
         assertEquals("c/d", result);
 
         result = (String) method.invoke(mojo, "/a/b/", "/a/b/c/d");
         assertEquals("c/d", result);
 
         result = (String) method.invoke(mojo, "c:\\a\\b", "c:\\a\\b\\c\\d");
         assertEquals("c/d", result);
 
         result = (String) method.invoke(mojo, "/a/b", "/a/b/c/d");
         assertEquals("c/d", result);
     }
 
     /**
      * Testing private method stripLocale.
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @Test
     public void testStripLocale() throws Exception {
         final File testpom = new File(getBasedir(), "src/test/resources/de/cismet/cids/mavenplugin/testpom.xml");
 
         final GenerateI18NArtifacts mojo = (GenerateI18NArtifacts) lookupMojo("generate-i18n", testpom);
         assertNotNull("mojo is null", mojo);
 
         final Method method = mojo.getClass().getDeclaredMethod("stripLocale", String.class, Locale.class);
         method.setAccessible(true);
 
         String path = null;
         Locale locale = null;
         String result = (String) method.invoke(mojo, path, locale);
         assertNull(result);
 
         path = null;
         locale = new Locale("de", "DE");
         result = (String) method.invoke(mojo, path, locale);
         assertNull(result);
 
         path = null;
         locale = new Locale("en", "GB");
         result = (String) method.invoke(mojo, path, locale);
         assertNull(result);
 
         path = "myFile.file";
         locale = null;
         result = (String) method.invoke(mojo, path, locale);
         assertEquals(path, result);
 
         path = "a/b/c/d/myFile.path";
         locale = null;
         result = (String) method.invoke(mojo, path, locale);
         assertEquals(path, result);
 
         path = "/a/b/c/d/myFile.path";
         locale = null;
         result = (String) method.invoke(mojo, path, locale);
         assertEquals(path, result);
 
         path = "a\\b\\c\\d\\myFile.path";
         locale = null;
         result = (String) method.invoke(mojo, path, locale);
         assertEquals(path, result);
 
         path = "c:\\a\\b\\c\\d\\myFile.path";
         locale = null;
         result = (String) method.invoke(mojo, path, locale);
         assertEquals(path, result);
 
         path = "myFile_de_DE_VAR.path";
         locale = null;
         result = (String) method.invoke(mojo, path, locale);
         assertEquals(path, result);
 
         path = "myFile_de_DE_VAR";
         locale = null;
         result = (String) method.invoke(mojo, path, locale);
         assertEquals(path, result);
 
         path = "myFile_de_DE_VAR.path";
         locale = new Locale("de", "DE");
         result = (String) method.invoke(mojo, path, locale);
         assertEquals(path, result);
 
         path = "myFile_de_DE_VAR.path";
         locale = new Locale("de", "DE", "VAR");
         result = (String) method.invoke(mojo, path, locale);
         assertEquals("myFile.path", result);
 
         path = "myFile_de_DE_VAR";
         locale = new Locale("de", "DE");
         result = (String) method.invoke(mojo, path, locale);
         assertEquals(path, result);
 
         path = "myFile_de_DE_VAR";
         locale = new Locale("de", "DE", "VAR");
         result = (String) method.invoke(mojo, path, locale);
         assertEquals("myFile", result);
     }
 
     /**
      * Test private method createOutputFile.
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @Test
     public void testCreateOutputFile() throws Exception {
         final File testpom = new File(getBasedir(), "src/test/resources/de/cismet/cids/mavenplugin/testpom.xml");
 
         // set up the mojo
         final MavenProject project = new MavenProject();
         final Build build = new Build();
         build.setFinalName("testArtifact-1.0");
         build.setDirectory(getBasedir() + "/target/GenerateI18NArtifactsMojoTest");
         project.setBuild(build);
         final GenerateI18NArtifacts mojo = (GenerateI18NArtifacts) lookupMojo("generate-i18n", testpom);
         assertNotNull("mojo is null", mojo);
         setVariableValueToObject(mojo, "project", project);
 
         final Method method = mojo.getClass().getDeclaredMethod("createOutputFile", Locale.class);
         method.setAccessible(true);
 
         setVariableValueToObject(mojo, "defaultLocale", null);
         try {
             method.invoke(mojo, null);
             fail("expected IllegalArgumentException");
         } catch (final IllegalArgumentException e) {
             // expected IllegalArgumentExcpetion
         }
 
         final String prefix = build.getDirectory() + File.separator + build.getFinalName();
 
         Locale locale = null;
         setVariableValueToObject(mojo, "defaultLocale", "de_DE");
         File result = (File) method.invoke(mojo, locale);
         assertEquals(prefix + "-de_DE.jar", result.getAbsolutePath());
 
         setVariableValueToObject(mojo, "defaultLocale", "_de_DE");
         result = (File) method.invoke(mojo, locale);
         assertEquals(prefix + "-_de_DE.jar", result.getAbsolutePath());
 
         locale = new Locale("de", "DE");
         result = (File) method.invoke(mojo, locale);
         assertEquals(prefix + "-de_DE.jar", result.getAbsolutePath());
 
         locale = new Locale("en");
         result = (File) method.invoke(mojo, locale);
         assertEquals(prefix + "-en.jar", result.getAbsolutePath());
 
         locale = new Locale("EN");
         result = (File) method.invoke(mojo, locale);
         assertEquals(prefix + "-en.jar", result.getAbsolutePath());
 
         locale = new Locale("EN", "gb");
         result = (File) method.invoke(mojo, locale);
         assertEquals(prefix + "-en_GB.jar", result.getAbsolutePath());
 
         locale = new Locale("a", "b", "c");
         result = (File) method.invoke(mojo, locale);
         assertEquals(prefix + "-a_B_c.jar", result.getAbsolutePath());
     }
 
     /**
      * DOCUMENT ME!
      */
     public void testScanLocales() throws Exception {
 
         final File testpom = new File(getBasedir(), "src/test/resources/de/cismet/cids/mavenplugin/testpom.xml");
 
         final GenerateI18NArtifacts mojo = (GenerateI18NArtifacts) lookupMojo("generate-i18n", testpom);
         assertNotNull("mojo is null", mojo);
 
         final Method method = mojo.getClass().getDeclaredMethod("scanLocales", Array.newInstance(File.class, 0).getClass());
         method.setAccessible(true);
 
         File f1 = new File("Bundle_de_DE_EUR.file");
         File f2 = new File("Bundle_de_DE_WIN.properties");
         File f3 = new File("fileWithoutExtension");
         File f4 = new File("Bundle_de_DE_EUR.properties");
         File f5 = new File("qwerde_DE.properties");
         File f6 = new File("Bundle_en_US");
 
         File[] values = new File[]{f1, f2, f3, f4, f5, f6};
         Set<Locale> result = (Set<Locale>) method.invoke(mojo, (Object) values);
         assertTrue(result.size() == 4);
         assertTrue(result.contains(new Locale("en", "US")));
         assertTrue(result.contains(new Locale("de")));
         assertTrue(result.contains(new Locale("de", "DE", "EUR")));
         assertTrue(result.contains(new Locale("de", "DE", "WIN")));
 
         values = new File[]{f1, f3, f4, f5};
         result = (Set<Locale>) method.invoke(mojo, (Object) values);
         assertTrue(result.size() == 2);
         assertTrue(result.contains(new Locale("de")));
         assertTrue(result.contains(new Locale("de", "DE", "EUR")));
 
         values = new File[]{f1, f3, f5, f6};
         result = (Set<Locale>) method.invoke(mojo, (Object) values);
         assertTrue(result.size() == 3);
         assertTrue(result.contains(new Locale("en", "US")));
         assertTrue(result.contains(new Locale("de")));
         assertTrue(result.contains(new Locale("de", "DE", "EUR")));
 
     }
 
     /**
      * @throws Exception
      */
     @Test
     public void testCreateLocale() throws Exception {
         final File testpom = new File(getBasedir(), "src/test/resources/de/cismet/cids/mavenplugin/testpom.xml");
 
         final GenerateI18NArtifacts mojo = (GenerateI18NArtifacts) lookupMojo("generate-i18n", testpom);
         assertNotNull("mojo is null", mojo);
 
         final Method method = mojo.getClass().getDeclaredMethod("createLocale", Array.newInstance(String.class, 0).getClass());
         method.setAccessible(true);
 
         String[] values = null;
 
         try {
             method.invoke(mojo, (Object)values);
             fail("expected IllegalArgumentException");
         } catch (final InvocationTargetException e) {
             // expected IllegalArgumentExcpetion
             if (!(e.getCause() instanceof IllegalArgumentException)) {
                 e.printStackTrace();
                 fail("expected IllegalArgumentException" );
             }
         }
 
         Locale result = null;
 
         values = new String[]{""};
         result = (Locale) method.invoke(mojo, (Object) values);
         assertNull(result);
 
         values = new String[]{"de"};
         result = (Locale) method.invoke(mojo, (Object) values);
         assertEquals("de", result.getLanguage());
         assertEquals("", result.getCountry());
         assertEquals("", result.getVariant());
 
         values = new String[]{"", "de", "DE"};
         result = (Locale) method.invoke(mojo, (Object) values);
         assertEquals("_de", result.getLanguage());
         assertEquals("DE", result.getCountry());
         assertEquals("", result.getVariant());
 
         values = new String[]{"DE", "de"};
         result = (Locale) method.invoke(mojo, (Object) values);
         assertEquals("de", result.getLanguage());
         assertEquals("DE", result.getCountry());
         assertEquals("", result.getVariant());
 
         values = new String[]{"DE", "de", "Traditional_WIN"};
         result = (Locale) method.invoke(mojo, (Object) values);
         assertEquals("de", result.getLanguage());
         assertEquals("DE", result.getCountry());
         assertEquals("Traditional_WIN", result.getVariant());
 
         values = new String[]{"DE", "de", "Traditional", "WIN"};
         result = (Locale) method.invoke(mojo, (Object) values);
         assertNull(result);
 
 
     }
 
     /**
      * DOCUMENT ME!
      */
     public void testNormaliseSplittedLocale() throws Exception {
         final File testpom = new File(getBasedir(), "src/test/resources/de/cismet/cids/mavenplugin/testpom.xml");
 
         final GenerateI18NArtifacts mojo = (GenerateI18NArtifacts) lookupMojo("generate-i18n", testpom);
         assertNotNull("mojo is null", mojo);
 
         final Method method = mojo.getClass().getDeclaredMethod("normaliseSplittedLocale",
                 Array.newInstance(String.class, 0).getClass());
         method.setAccessible(true);
 
         String[] values = null;
         String[] result = null;
 
         try {
             method.invoke(mojo, (Object)values);
             fail("expected IllegalArgumentException");
         } catch (final InvocationTargetException e) {
             // expected IllegalArgumentExcpetion
             if (!(e.getCause() instanceof IllegalArgumentException)) {
                 e.printStackTrace();
                 fail("expected IllegalArgumentException");
             }
         }
 
         values = new String[]{"", "de", "DE"};
         result = (String[]) method.invoke(mojo, (Object) values);
         assertTrue(result.length == 2);
         assertEquals("_de", result[0]);
         assertEquals("DE", result[1]);
 
         values = new String[]{"", "", "de", "DE"};
         result = (String[]) method.invoke(mojo, (Object) values);
         assertTrue(result.length == 3);
         assertEquals("_", result[0]);
         assertEquals("de", result[1]);
         assertEquals("DE", result[2]);
 
         values = new String[]{"", "", ""};
         result = (String[]) method.invoke(mojo, (Object) values);
         assertTrue(result.length == 1);
         assertEquals("_", result[0]);
 
         values = new String[]{"", "", "", ""};
         result = (String[]) method.invoke(mojo, (Object) values);
         assertTrue(result.length == 2);
         assertEquals("_", result[0]);
         assertEquals("_", result[1]);
 
         values = new String[]{"de", "", "DE"};
         result = (String[]) method.invoke(mojo, (Object) values);
         assertTrue(result.length == 2);
         assertEquals("de", result[0]);
         assertEquals("_DE", result[1]);
 
 
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @Test
     public void testConcat() throws Exception {
         final File testpom = new File(getBasedir(), "src/test/resources/de/cismet/cids/mavenplugin/testpom.xml");
 
         final GenerateI18NArtifacts mojo = (GenerateI18NArtifacts) lookupMojo("generate-i18n", testpom);
         assertNotNull("mojo is null", mojo);
 
         final Method method = mojo.getClass().getDeclaredMethod("concat", String.class, Array.newInstance(String.class, 0).getClass());
         method.setAccessible(true);
 
         String delim = null;
         String[] values = null;
         try {
             method.invoke(mojo, delim, values);
             fail("expected IllegalArgumentException");
         } catch (final InvocationTargetException e) {
             // expected IllegalArgumentExcpetion
             if (!(e.getCause() instanceof IllegalArgumentException)) {
                 fail("expected IllegalArgumentException");
             }
         }
 
         delim = null;
         values = new String[]{};
         String result = (String) method.invoke(mojo, delim, values);
         assertNotNull(result);
         assertTrue(result.isEmpty());
 
         delim = null;
         values = new String[]{"a", "b"};
         result = (String) method.invoke(mojo, delim, values);
         assertEquals("ab", result);
 
         delim = ",";
         values = new String[]{"a", "b", "c"};
         result = (String) method.invoke(mojo, delim, values);
         assertEquals("a,b,c", result);
 
         delim = "+++";
         values = new String[]{};
         result = (String) method.invoke(mojo, delim, values);
         assertEquals("", result);
 
         delim = "#";
         values = new String[]{"", "", ""};
         result = (String) method.invoke(mojo, delim, values);
         assertEquals("##", result);
 
     }
 
     /**
      * DOCUMENT ME!
      */
     public void testGetAllFiles() throws Exception {
 
         final File testpom = new File(getBasedir(), "src/test/resources/de/cismet/cids/mavenplugin/testpom.xml");
 
         // set up the mojo
         final MavenProject project = new MavenProject();
         final Build build = new Build();
         build.setFinalName("testArtifact-1.0");
         build.setDirectory(getBasedir() + "/target/GenerateI18NArtifactsMojoTest");
         project.setBuild(build);
         final GenerateI18NArtifacts mojo = (GenerateI18NArtifacts) lookupMojo("generate-i18n", testpom);
         assertNotNull("mojo is null", mojo);
         setVariableValueToObject(mojo, "project", project);
 
         final Method method = mojo.getClass().getDeclaredMethod("getAllFiles", File.class, FileFilter.class, boolean.class);
         method.setAccessible(true);
 
         final Class[] mojoClasses = mojo.getClass().getDeclaredClasses();
         Class RecursiveNBPropertiesFilter = null;
         Class RecursiveLocalisedNBPropertiesFilter = null;
         for (Class c : mojoClasses) {
             if(c.getName().equals("de.cismet.cids.mavenplugin.GenerateI18NArtifacts$RecursiveNBPropertiesFilter"))
                 RecursiveNBPropertiesFilter = c;
 
             if(c.getName().equals("de.cismet.cids.mavenplugin.GenerateI18NArtifacts$RecursiveLocalisedNBPropertiesFilter"))
                 RecursiveLocalisedNBPropertiesFilter = c;
         }
 
         Constructor cRecursiveNBPropertiesFilter = RecursiveNBPropertiesFilter.getDeclaredConstructor(new Class[]{});
         Constructor cRecursiveLocalisedNBPropertiesFilter = 
                 RecursiveLocalisedNBPropertiesFilter.getDeclaredConstructor(new Class[]{Locale.class});
 
         cRecursiveNBPropertiesFilter.setAccessible(true);
         cRecursiveLocalisedNBPropertiesFilter.setAccessible(true);
 
         File root = null;
         FileFilter filter = null;
         boolean recursive = false;
 
         File[] result = null;
 
         try {
             method.invoke(mojo, root, filter, recursive);
             fail("expected IllegalArgumentException");
         } catch (final InvocationTargetException e) {
             // expected IllegalArgumentExcpetion
             if (!(e.getCause() instanceof IllegalArgumentException)) {
                 fail("expected IllegalArgumentException");
             }
         }
 
         root = new File("src/test/resources/teststructure");
         // filefilter == null => noFilter used
         recursive = true;
         result = (File[]) method.invoke(mojo, root, filter, recursive);
//        for (File file : result) {
//            System.err.println(file.getName());
//        }
        assertEquals(32, result.length);
 
         recursive = false;
         result = (File[]) method.invoke(mojo, root, filter, recursive);
         assertEquals(0, result.length);
 
         recursive = true;
         filter = (FileFilter) cRecursiveLocalisedNBPropertiesFilter.newInstance(new Locale("de", "DE"));
         result = (File[]) method.invoke(mojo, root, filter, recursive);
         assertEquals(2, result.length);
 
         recursive = true;
         filter = (FileFilter) cRecursiveNBPropertiesFilter.newInstance();
         result = (File[]) method.invoke(mojo, root, filter, recursive);
         assertEquals(6, result.length);
 
     }
 }
