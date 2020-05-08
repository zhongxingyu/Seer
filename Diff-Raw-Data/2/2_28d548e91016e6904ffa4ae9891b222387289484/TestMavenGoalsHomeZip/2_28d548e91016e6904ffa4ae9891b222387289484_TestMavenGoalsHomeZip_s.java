 package com.atlassian.maven.plugins.amps;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.model.Build;
 import org.apache.maven.plugin.PluginManager;
 import org.apache.maven.plugin.logging.SystemStreamLog;
 import org.apache.maven.project.MavenProject;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.UUID;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 public class TestMavenGoalsHomeZip
 {
     public static final String PROJECT_ID = "noplacelike";
     public static final String TMP_RESOURCES = "tmp-resources";
     public static final String GENERATED_HOME = "generated-home";
     public static final String PLUGINS = "plugins";
     public static final String BUNDLED_PLUGINS = "bundled-plugins";
     public static final String ZIP_PREFIX = "generated-resources/" + PROJECT_ID + "-home";
 
     private MavenContext ctx;
     private MavenGoals goals;
     private File tempDir;
     private File productDir;
     private File tempResourcesDir;
     private File generatedHomeDir;
     private File pluginsDir;
     private File bundledPluginsDir;
     private ZipFile zip;
 
 
     @Before
     public void setup(){
         //Create the temp dir
        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
         String dirName = UUID.randomUUID().toString();
         tempDir = new File(sysTempDir, dirName);
         productDir = new File(tempDir,PROJECT_ID);
         tempResourcesDir = new File(productDir,TMP_RESOURCES);
         generatedHomeDir = new File(tempResourcesDir,GENERATED_HOME);
         pluginsDir = new File(generatedHomeDir,PLUGINS);
         bundledPluginsDir = new File(generatedHomeDir,BUNDLED_PLUGINS);
 
         //setup maven mocks
         MavenProject project = mock(MavenProject.class);
         Build build = mock(Build.class);
 
         //Mockito throws NoClassDefFoundError: org/apache/maven/project/ProjectBuilderConfiguration
         //when mocking the session
         //MavenSession session = mock(MavenSession.class);
 
         PluginManager pluginManager = mock(PluginManager.class);
         List<MavenProject> reactor = Collections.<MavenProject>emptyList();
         ctx = mock(MavenContext.class);
 
         when(build.getDirectory()).thenReturn(tempDir.getAbsolutePath());
         when(project.getBuild()).thenReturn(build);
         when(ctx.getProject()).thenReturn(project);
         when(ctx.getLog()).thenReturn(new SystemStreamLog());
         when(ctx.getReactor()).thenReturn(reactor);
         when(ctx.getSession()).thenReturn(null);
         when(ctx.getPluginManager()).thenReturn(pluginManager);
 
         goals = new MavenGoals(ctx);
     }
 
     @After
     public void removeTempDir() throws IOException
     {
         //make sure zip is closed, else delete fails on windows
         if (zip != null) {
             try {
                 zip.close();
             } catch (IOException e) {
                 //ignore
             }
             zip = null;
         }
         FileUtils.deleteDirectory(tempDir);
     }
 
     @Test
     public void skipNullHomeDir(){
         File zip = new File(tempDir,"nullHomeZip.zip");
 
         goals.createHomeResourcesZip(null,zip,PROJECT_ID);
 
         assertFalse("zip for null home should not exist", zip.exists());
     }
 
     @Test
     public void skipNonExistentHomeDir(){
         File zip = new File(tempDir,"noExistHomeZip.zip");
         File fakeHomeDir = new File(tempDir,"this-folder-does-not-exist");
 
         goals.createHomeResourcesZip(fakeHomeDir,zip,PROJECT_ID);
 
         assertFalse("zip for non-existent home should not exist", zip.exists());
     }
 
     @Test
     public void existingGeneratedDirGetsDeleted() throws IOException
     {        
         generatedHomeDir.mkdirs();
         File deletedFile = new File(generatedHomeDir,"should-be-deleted.txt");
         FileUtils.writeStringToFile(deletedFile,"This file should have been deleted!");
 
         File zip = new File(tempDir,"deleteGenHomeZip.zip");
         File homeDir = new File(tempDir,"deleteGenHomeDir");
         homeDir.mkdirs();
 
         goals.createHomeResourcesZip(homeDir,zip,PROJECT_ID);
 
         assertFalse("generated text file should have been deleted",deletedFile.exists());
     }
 
     @Test
     public void pluginsNotIncluded() throws IOException
     {
         pluginsDir.mkdirs();
 
         File pluginFile = new File(pluginsDir,"plugin.txt");
         FileUtils.writeStringToFile(pluginFile,"This file should have been deleted!");
 
         File zip = new File(tempDir,"deletePluginsHomeZip.zip");
         File homeDir = new File(tempDir,"deletePluginsHomeDir");
         homeDir.mkdirs();
 
         goals.createHomeResourcesZip(homeDir,zip,PROJECT_ID);
 
         assertFalse("plugins file should have been deleted",pluginFile.exists());
     }
 
     @Test
     public void bundledPluginsNotIncluded() throws IOException
     {
         bundledPluginsDir.mkdirs();
 
         File pluginFile = new File(bundledPluginsDir,"bundled-plugin.txt");
         FileUtils.writeStringToFile(pluginFile,"This file should have been deleted!");
 
         File zip = new File(tempDir,"deleteBundledPluginsHomeZip.zip");
         File homeDir = new File(tempDir,"deleteBundledPluginsHomeDir");
         homeDir.mkdirs();
 
         goals.createHomeResourcesZip(homeDir,zip,PROJECT_ID);
 
         assertFalse("bundled-plugins file should have been deleted",pluginFile.exists());
     }
 
     @Test
     public void zipContainsProperPrefix() throws IOException
     {
         File zipFile = new File(tempDir,"prefixedHomeZip.zip");
         File homeDir = new File(tempDir,"prefixedHomeDir");
         File dataDir = new File(homeDir,"data");
 
         dataDir.mkdirs();
 
         goals.createHomeResourcesZip(homeDir,zipFile,PROJECT_ID);
 
         zip = new ZipFile(zipFile);
         final Enumeration<? extends ZipEntry> entries = zip.entries();
 
         while (entries.hasMoreElements())
         {
             final ZipEntry zipEntry = entries.nextElement();
             String zipPath = zipEntry.getName();
             String[] segments = zipPath.split("/");
             if(segments.length > 1) {
                 String testPrefix = segments[0] + "/" + segments[1];
                 assertEquals(ZIP_PREFIX,testPrefix);
             }
 
         }
     }
 
     @Test
     public void zipContainsTestFile() throws IOException
     {
         File zipFile = new File(tempDir,"fileHomeZip.zip");
         File homeDir = new File(tempDir,"fileHomeDir");
         File dataDir = new File(homeDir,"data");
         File dataFile = new File(dataDir,"data.txt");
 
         dataDir.mkdirs();
         FileUtils.writeStringToFile(dataFile,"This is some data.");
 
         goals.createHomeResourcesZip(homeDir,zipFile,PROJECT_ID);
 
         boolean dataFileFound = false;
         zip = new ZipFile(zipFile);
         final Enumeration<? extends ZipEntry> entries = zip.entries();
 
         while (entries.hasMoreElements())
         {
             final ZipEntry zipEntry = entries.nextElement();
             String zipPath = zipEntry.getName();
             String fileName = zipPath.substring(zipPath.lastIndexOf("/") + 1);
             if(fileName.equals(dataFile.getName())) {
                 dataFileFound = true;
                 break;
             }
         }
 
         assertTrue("data file not found in zip.",dataFileFound);
     }
 }
