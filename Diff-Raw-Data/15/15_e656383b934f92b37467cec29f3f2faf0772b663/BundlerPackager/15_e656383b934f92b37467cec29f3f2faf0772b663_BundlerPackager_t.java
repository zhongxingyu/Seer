 /*
  * Copyright 2010 Proofpoint, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.proofpoint.platform;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.io.Closeables;
 import com.google.common.io.Resources;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.jruby.Ruby;
 import org.jruby.RubyInstanceConfig;
 import org.jruby.RubyObjectAdapter;
 import org.jruby.javasupport.JavaEmbedUtils;
 import org.jruby.runtime.builtin.IRubyObject;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 
 import static com.google.common.io.Files.copy;
 import static com.google.common.io.Files.createTempDir;
 import static com.google.common.io.Files.deleteRecursively;
 import static org.jruby.javasupport.JavaEmbedUtils.javaToRuby;
 
 /**
  * @goal bundler-package
  */
 public class BundlerPackager
         extends AbstractMojo
 {
     /**
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * The output directory of the assembled distribution file.
      *
      * @parameter default-value="${project.build.directory}"
      * @required
      */
     private File outputDirectory;
 
     public void execute()
             throws MojoExecutionException, MojoFailureException
     {
         URL bundlerLib = Resources.getResource("bundler/lib");
         checkNotNull(bundlerLib, "Couldn't find a gem repo that contains bundler.  Please ensure the Bundler Packaging plugin is properly built.");
 
         String gemfileLocation = locateFileInProjectRoot("Gemfile");
         String gemfileLockLocation = locateFileInProjectRoot("Gemfile.lock");
 
         Ruby runtime = JavaEmbedUtils.initialize(ImmutableList.of(bundlerLib.getPath()), createRuntimeConfig());
         RubyObjectAdapter adapter = JavaEmbedUtils.newObjectAdapter();
 
         IRubyObject gemRepositoryBuilder = createNewGemRepositoryBuilder(runtime);
 
         String gemrepoTempDirectoryPath = createTemporaryDirectory();
 
         IRubyObject response = null;
         try {
             response = adapter.callMethod(gemRepositoryBuilder, "build_repository_using_bundler",
                     new IRubyObject[]{
                             javaToRuby(runtime, gemrepoTempDirectoryPath),
                             javaToRuby(runtime, gemfileLocation),
                             javaToRuby(runtime, gemfileLockLocation)
                     });
         } catch (Exception e) {
             throw new MojoExecutionException("Gem repo jar was not properly constructed.  Please check the output for errors.  " +
                             "Try running bundle install manually to verify the contents of the Gemfile.  [" + gemfileLocation + "]", e);
 
         }
 
         String gemrepoGeneratedLocation = response.asJavaString();
 
         try {
             deleteRecursively(new File(gemrepoGeneratedLocation + "/bin"));
         }
         catch (IOException e) {
             //If it fails, no big deal, we eliminate the whole repo later on.
         }
         try {
             deleteRecursively(new File(gemrepoGeneratedLocation + "/cache"));
         }
         catch (IOException e) {
             //If it fails, no big deal, we eliminate the whole repo later on.
         }
         try {
             deleteRecursively(new File(gemrepoGeneratedLocation + "/doc"));
         }
         catch (IOException e) {
             //If it fails, no big deal, we eliminate the whole repo later on.
         }
 
         generateJarFile(new File(gemrepoGeneratedLocation), gemfileLocation);
 
         try {
             deleteRecursively(new File(gemrepoTempDirectoryPath));
         } catch (IOException e) {
             throw new MojoExecutionException("Error trying to delete temporary directory for the gems, " +
                     "please ensure the plugin is properly built and the temporary directory [" +
                     gemrepoTempDirectoryPath + "] is writeable." );
         }
     }
 
     private IRubyObject createNewGemRepositoryBuilder(Ruby runtime) throws MojoExecutionException {
 
         URL gemRepositoryBuilder = Resources.getResource("proofpoint/GemRepositoryBuilder.rb");
 
         InputStream gemRepositoryBuilderStream = null;
         try {
             try {
                 gemRepositoryBuilderStream = gemRepositoryBuilder.openStream();
             }
             catch (Exception e) {
                 throw new MojoExecutionException("Couldn't find GemRepositoryBuilder.rb.  Please ensure the Bundler Packaging plugin is properly built.");
             }
 
             runtime.loadFile("GemRepositoryBuilder", gemRepositoryBuilderStream, false);
         }
         finally {
             Closeables.closeQuietly(gemRepositoryBuilderStream);
         }
 
         return runtime.evalScriptlet("Proofpoint::GemToJarPackager::GemRepositoryBuilder.new");
     }
 
     private String createTemporaryDirectory() throws MojoExecutionException {
         try {
             return createTempDir().getCanonicalPath();
         } catch (IOException e) {
             throw new MojoExecutionException("Error trying to create temporary directory for the gems, " +
                     "please ensure the plugin is properly built and the temporary directory [" +
                     System.getProperty("java.io.tmpdir") + "] isn't full and is writeable." );
         }
     }
 
     private String locateFileInProjectRoot(String fileName) throws MojoExecutionException {
         String fileLocation = getProjectBaseDir() + "/" + fileName;
 
         if (!(new File(fileLocation)).exists())
         {
             throw new MojoExecutionException("No " + fileName + " was found in the root of your project.  " +
                     "Please ensure a " + fileName + " exists, is readable, and is in the root of your project structure.  "  +
                     "The project root appears to be at [" + getProjectBaseDir() + "].");
         }
 
         return fileLocation;
     }
 
     private String getProjectBaseDir() throws MojoExecutionException {
         try {
             return project.getBasedir().getCanonicalPath();
         }
         catch (IOException e) {
             throw new MojoExecutionException("Error trying to locate the project base directory, please ensure the plugin is properly built.");
         }
     }
 
     private void generateJarFile(File gemrepoDirectory, String gemfileLocation)
             throws MojoExecutionException
     {
         try {
             if (!outputDirectory.exists()) {
                 outputDirectory.mkdirs();
             }
            File gemrepoJarFile = new File(String.format("%s/%s-%s-gemrepo.jar", outputDirectory.getCanonicalPath(), project.getArtifactId(), project.getVersion()));
             getLog().info("Building gem repository jar: " + gemrepoJarFile.getName());
 
             Manifest manifest = new Manifest();
             JarOutputStream gemrepoJarOutputStream = new JarOutputStream(new FileOutputStream(gemrepoJarFile), manifest);
 
             addFilesToJarRecursivelyIgnoringSymlinks(gemrepoJarOutputStream, gemrepoDirectory, "");
 
             addFileToJar(gemrepoJarOutputStream, new File(gemfileLocation), "META-INF/");
 
             Closeables.closeQuietly(gemrepoJarOutputStream);
         } catch (IOException e) {
             throw new MojoExecutionException("Error trying to create the jar containing the gems repository, " +
                     "please ensure the plugin is properly built and the target directory [" +
                     outputDirectory.getPath() + "] exists or is creatable, is not full, and is writeable." );
         }
     }
 
     private void addFilesToJarRecursivelyIgnoringSymlinks(JarOutputStream jarOutputStream, File directory, String path) throws IOException
     {
         for(File child : directory.listFiles()) {
             if (!isSymbolicLink(child)) {
 
                 addFileToJar(jarOutputStream, child, path);
 
                 if(child.isDirectory()) {
                     addFilesToJarRecursivelyIgnoringSymlinks(jarOutputStream, child, path + child.getName() + "/");
                 }
             }
         }
     }
 
     private void addFileToJar(JarOutputStream jarOutputStream, File fileToAdd, String path) throws IOException {
         String entryName = path + fileToAdd.getName() + (fileToAdd.isDirectory() ? "/" : "");
         getLog().debug("Adding entry [" + entryName + "] to the gem repository jar");
         JarEntry jarEntry = new JarEntry(entryName);
         jarEntry.setTime(fileToAdd.lastModified());
         jarOutputStream.putNextEntry(jarEntry);
 
         if (!fileToAdd.isDirectory()) {
             copy(fileToAdd, jarOutputStream);
         }
     }
 
     private boolean isSymbolicLink(File file)
     {
         try {
             File canonicalFile = file.getCanonicalFile();
             File absoluteFile = file.getAbsoluteFile();
             // a symbolic link has a different name between the canonical and absolute path
             return !canonicalFile.getName().equals(absoluteFile.getName()) ||
                     // or the canonical parent path is not the same as the files parent path
                     !canonicalFile.getParent().equals(file.getParentFile().getCanonicalPath());
         }
         catch (IOException e) {
             // error on the side of caution
             return true;
         }
     }
 
     private RubyInstanceConfig createRuntimeConfig()
     {
         RubyInstanceConfig config = new RubyInstanceConfig();
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         if (classLoader == null) {
             classLoader = ClassLoader.getSystemClassLoader();
         }
         config.setClassCache(JavaEmbedUtils.createClassCache(classLoader));
 
         URL resource = RubyInstanceConfig.class.getResource("/META-INF/jruby.home");
         if (resource != null && resource.getProtocol().equals("jar")) {
             try { // http://weblogs.java.net/blog/2007/04/25/how-convert-javaneturl-javaiofile
                 config.setJRubyHome(resource.toURI().getSchemeSpecificPart());
             }
             catch (URISyntaxException e) {
                 config.setJRubyHome(resource.getPath());
             }
         }
 
         return config;
     }
 
     private void checkNotNull(Object argument, String message)
             throws MojoExecutionException
     {
         if (argument == null)
         {
             throw new MojoExecutionException(message);
         }
     }
 
     public void setProject(MavenProject project)
     {
         this.project = project;
     }
 
     public void setOutputDirectory(File outputDirectory)
     {
         this.outputDirectory = outputDirectory;
     }
 }
