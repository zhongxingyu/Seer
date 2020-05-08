 /*
  * Copyright 2007 The Kuali Foundation
  * 
  * Licensed under the Educational Community License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.opensource.org/licenses/ecl2.php
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kualigan.maven.plugins.api;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.ExecuteException;
 import org.apache.commons.exec.Executor;
 import org.apache.commons.exec.PumpStreamHandler;
 import org.apache.maven.archetype.Archetype;
 import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
 import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.shared.invoker.DefaultInvocationRequest;
 import org.apache.maven.shared.invoker.DefaultInvoker;
 import org.apache.maven.shared.invoker.InvocationRequest;
 import org.apache.maven.shared.invoker.InvocationResult;
 import org.apache.maven.shared.invoker.Invoker;
 import org.apache.maven.shared.invoker.MavenInvocationException;
 import org.codehaus.plexus.archiver.Archiver;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.UnArchiver;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 import org.codehaus.plexus.archiver.util.DefaultFileSet;
 import org.codehaus.plexus.archiver.zip.ZipArchiver;
 import org.codehaus.plexus.component.annotations.Component;
 import org.codehaus.plexus.component.annotations.Requirement;
 import org.codehaus.plexus.components.interactivity.Prompter;
 import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.StringUtils;
 import org.codehaus.plexus.util.cli.CommandLineUtils;
 
 /**
  * Creates a prototype from the given KFS project resource. A KFS project resource can be either
  * of the following:
  * <ul>
  *   <li>KFS war file</li>
  *   <li>KFS project directory with source</li>
  *   <li>KFS svn repo</li>
  * </ul>
  * 
  * @author Leo Przybylski
  */
 @Component(role = org.kualigan.maven.plugins.api.PrototypeHelper.class, hint="default")
 public class DefaultPrototypeHelper implements PrototypeHelper {
     public static final String ROLE_HINT = "default";
     
     private static final Options OPTIONS = new Options();
 
     private static final char SET_SYSTEM_PROPERTY = 'D';
 
     private static final char OFFLINE = 'o';
 
     private static final char REACTOR = 'r';
 
     private static final char QUIET = 'q';
 
     private static final char DEBUG = 'X';
 
     private static final char ERRORS = 'e';
 
     private static final char NON_RECURSIVE = 'N';
 
     private static final char UPDATE_SNAPSHOTS = 'U';
 
     private static final char ACTIVATE_PROFILES = 'P';
 
     private static final String FORCE_PLUGIN_UPDATES = "cpu";
 
     private static final String FORCE_PLUGIN_UPDATES2 = "up";
 
     private static final String SUPPRESS_PLUGIN_UPDATES = "npu";
 
     private static final String SUPPRESS_PLUGIN_REGISTRY = "npr";
 
     private static final char CHECKSUM_FAILURE_POLICY = 'C';
 
     private static final char CHECKSUM_WARNING_POLICY = 'c';
 
     private static final char ALTERNATE_USER_SETTINGS = 's';
 
     private static final String FAIL_FAST = "ff";
 
     private static final String FAIL_AT_END = "fae";
 
     private static final String FAIL_NEVER = "fn";
     
     private static final String ALTERNATE_POM_FILE = "f";
     
     /**
      */
     @Requirement
     protected Archetype archetype;
 
     /**
      */
     @Requirement
     protected Prompter prompter;
 
     /**
      */
     @Requirement
     protected ArtifactRepositoryFactory artifactRepositoryFactory;
 
     /**
      */
     @Requirement(role=org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout.class, hint="default")
     protected ArtifactRepositoryLayout defaultArtifactRepositoryLayout;
 
     @Requirement
     protected ArchiverManager archiverManager;
     
     private AbstractMojo caller;
 
     protected void logUnpack(final File file, final File location, final String includes, final String excludes ) {
         if (!getCaller().getLog().isInfoEnabled()) {
             return;
         }
 
         StringBuffer msg = new StringBuffer();
         msg.append( "Unpacking " );
         msg.append( file );
         msg.append( " to " );
         msg.append( location );
 
         if ( includes != null && excludes != null ) {
             msg.append( " with includes \"" );
             msg.append( includes );
             msg.append( "\" and excludes \"" );
             msg.append( excludes );
             msg.append( "\"" );
         }
         else if (includes != null) {
             msg.append( " with includes \"" );
             msg.append( includes );
             msg.append( "\"" );
         }
         else if (excludes != null) {
             msg.append( " with excludes \"" );
             msg.append( excludes );
             msg.append( "\"" );
         }
 
         getCaller().getLog().info(msg.toString());
     }
 
    
     protected int executeCommandLine(final String ... args) throws ExecuteException, IOException {
         final Executor exec = new DefaultExecutor();
         final Map enviro = new HashMap();
         try {
             final Properties systemEnvVars = CommandLineUtils.getSystemEnvVars();
             enviro.putAll(systemEnvVars);
         } catch (IOException x) {
             getCaller().getLog().error("Could not assign default system enviroment variables.", x);
         }
 
         final File workingDirectory = new File(System.getProperty("java.io.tmpdir"));
         final org.apache.commons.exec.CommandLine commandLine = new org.apache.commons.exec.CommandLine(args[0]);
         for (int i = 1; i < args.length; i++) {
             commandLine.addArgument(args[i]);
         }
 
         exec.setStreamHandler(new PumpStreamHandler(System.out, System.err, System.in));
         exec.setWorkingDirectory(workingDirectory);
         return exec.execute(commandLine, enviro);
     }
 
     /**
      * Handles repacking of the war as a jar file with classes, etc... Basically makes a jar of everything
      * in the war file's WEB-INF/classes path.
      * 
      * @param file is the war file to repackage
      * @return {@link File} instance of the repacked jar
      */
     public File repack(final File file, final String artifactId) throws MojoExecutionException {
         final File workingDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator
                                                + artifactId + "-repack");
         final File warDirectory     = new File(workingDirectory, "war");
         final File repackDirectory  = new File(workingDirectory, artifactId);
         final File classesDirectory = new File(warDirectory, "WEB-INF/classes");
         final File retval           = new File(workingDirectory, artifactId + ".jar");
         
         try {
             workingDirectory.mkdirs();
             workingDirectory.mkdir();
             workingDirectory.deleteOnExit();
             warDirectory.mkdir();
         }
         catch (Exception e) {
             throw new MojoExecutionException("Unable to create working directory for repackaging", e);
         }
         
         unpack(file, warDirectory, "**/classes/**", null);
         
         try {
             FileUtils.copyDirectoryStructure(classesDirectory, repackDirectory);
         }
         catch (Exception e) {
             throw new MojoExecutionException("Unable to copy files into the repack directory");
         }
 
         try {
             pack(retval, repackDirectory, "**/**", null);
         }
         catch (Exception e) {
             throw new MojoExecutionException("Was unable to create the jar", e);
         }
         
         return retval;
     }
     
     /**
      * Unpacks the archive file.
      *
      * @param file     File to be unpacked.
      * @param location Location where to put the unpacked files.
      * @param includes Comma separated list of file patterns to include i.e. <code>**&#47;.xml,
      *                 **&#47;*.properties</code>
      * @param excludes Comma separated list of file patterns to exclude i.e. <code>**&#47;*.xml,
      *                 **&#47;*.properties</code>
      */
     protected void unpack(final File file, final File location, final String includes, final String excludes) throws MojoExecutionException {
         try {
             logUnpack(file, location, includes, excludes);
 
             location.mkdirs();
 
             final UnArchiver unArchiver;
             unArchiver = archiverManager.getUnArchiver(file);
             unArchiver.setSourceFile(file);
             unArchiver.setDestDirectory(location);
 
             if (StringUtils.isNotEmpty(excludes) || StringUtils.isNotEmpty(includes)) {
                 final IncludeExcludeFileSelector[] selectors =
                     new IncludeExcludeFileSelector[]{ new IncludeExcludeFileSelector() };
 
                 if (StringUtils.isNotEmpty( excludes )) {
                     selectors[0].setExcludes(excludes.split( "," ));
                 }
 
                 if (StringUtils.isNotEmpty( includes )) {
                     selectors[0].setIncludes(includes.split( "," ));
                 }
 
                 unArchiver.setFileSelectors(selectors);
             }
 
             unArchiver.extract();
         }
         catch ( NoSuchArchiverException e ) {
             throw new MojoExecutionException("Unknown archiver type", e);
         }
         catch (ArchiverException e) {
             e.printStackTrace();
             throw new MojoExecutionException(
                 "Error unpacking file: " + file + " to: " + location + "\r\n" + e.toString(), e );
         }
     }
 
     /**
      * Packs a jar
      *
      * @param file     Destination file.
      * @param location Directory source.
      * @param includes Comma separated list of file patterns to include i.e. <code>**&#47;.xml,
      *                 **&#47;*.properties</code>
      * @param excludes Comma separated list of file patterns to exclude i.e. <code>**&#47;*.xml,
      *                 **&#47;*.properties</code>
      */
     protected void pack(final File file, final File location, final String includes, final String excludes) throws MojoExecutionException {
         try {
 
             final Archiver archiver;
             archiver = archiverManager.getArchiver(file);
             archiver.addFileSet(new DefaultFileSet() {{
                 setDirectory(location);
                 if (includes != null) {
                     setIncludes(includes.split(","));
                 }
                 if (excludes != null) {
                     setExcludes(excludes.split(","));
                 }
             }});
             archiver.setDestFile(file);
 
             archiver.createArchive();
         }
         catch ( NoSuchArchiverException e ) {
             throw new MojoExecutionException("Unknown archiver type", e);
         }
         catch (Exception e) {
             e.printStackTrace();
             throw new MojoExecutionException(
                 "Error packing directory: " + location + " to: " + file + "\r\n" + e.toString(), e );
         }
     }
 
     /**
      * 
      */
     private void setupRequest(final InvocationRequest req,
                               final String additionalArguments) throws MojoExecutionException {
         try {
             final String[] args = CommandLineUtils.translateCommandline(additionalArguments);
             CommandLine cli = new PosixParser().parse(OPTIONS, args);
 
             if (cli.hasOption( SET_SYSTEM_PROPERTY))
             {
                 String[] properties = cli.getOptionValues( SET_SYSTEM_PROPERTY );
                 Properties props = new Properties();
                 for ( int i = 0; i < properties.length; i++ )
                 {
                     String property = properties[i];
                     String name, value;
                     int sep = property.indexOf( "=" );
                     if ( sep <= 0 )
                     {
                         name = property.trim();
                         value = "true";
                     }
                     else
                     {
                         name = property.substring( 0, sep ).trim();
                         value = property.substring( sep + 1 ).trim();
                     }
                     props.setProperty( name, value );
                 }
 
                 req.setProperties( props );
             }
 
             if ( cli.hasOption( OFFLINE ) )
             {
                 req.setOffline( true );
             }
 
             if ( cli.hasOption( QUIET ) )
             {
                 // TODO: setQuiet() currently not supported by InvocationRequest
                 req.setDebug( false );
             }
             else if ( cli.hasOption( DEBUG ) )
             {
                 req.setDebug( true );
             }
             else if ( cli.hasOption( ERRORS ) )
             {
                 req.setShowErrors( true );
             }
 
             if ( cli.hasOption( REACTOR ) )
             {
                 req.setRecursive( true );
             }
             else if ( cli.hasOption( NON_RECURSIVE ) )
             {
                 req.setRecursive( false );
             }
 
             if ( cli.hasOption( UPDATE_SNAPSHOTS ) )
             {
                 req.setUpdateSnapshots( true );
             }
 
             if ( cli.hasOption( ACTIVATE_PROFILES ) )
             {
                 String[] profiles = cli.getOptionValues( ACTIVATE_PROFILES );
                 List<String> activatedProfiles = new ArrayList<String>();
                 List<String> deactivatedProfiles = new ArrayList<String>();
 
                 if ( profiles != null )
                 {
                     for ( int i = 0; i < profiles.length; ++i )
                     {
                         StringTokenizer profileTokens = new StringTokenizer( profiles[i], "," );
 
                         while ( profileTokens.hasMoreTokens() )
                         {
                             String profileAction = profileTokens.nextToken().trim();
 
                             if ( profileAction.startsWith( "-" ) || profileAction.startsWith( "!" ) )
                             {
                                 deactivatedProfiles.add( profileAction.substring( 1 ) );
                             }
                             else if ( profileAction.startsWith( "+" ) )
                             {
                                 activatedProfiles.add( profileAction.substring( 1 ) );
                             }
                             else
                             {
                                 activatedProfiles.add( profileAction );
                             }
                         }
                     }
                 }
 
                 if (!deactivatedProfiles.isEmpty()) {
                     getCaller().getLog().warn("Explicit profile deactivation is not yet supported. "
                                               + "The following profiles will NOT be deactivated: " + StringUtils.join(
                                                   deactivatedProfiles.iterator(), ", "));
                 }
 
                 if (!activatedProfiles.isEmpty()) {
                     req.setProfiles(activatedProfiles);
                 }
             }
 
             if (cli.hasOption(FORCE_PLUGIN_UPDATES) || cli.hasOption( FORCE_PLUGIN_UPDATES2)) {
                 getCaller().getLog().warn("Forcing plugin updates is not supported currently.");
             }
             else if (cli.hasOption( SUPPRESS_PLUGIN_UPDATES)) {
                 req.setNonPluginUpdates( true );
             }
 
             if (cli.hasOption( SUPPRESS_PLUGIN_REGISTRY)) {
                 getCaller().getLog().warn("Explicit suppression of the plugin registry is not supported currently." );
             }
 
             if (cli.hasOption(CHECKSUM_FAILURE_POLICY)) {
                 req.setGlobalChecksumPolicy( InvocationRequest.CHECKSUM_POLICY_FAIL );
             }
             else if ( cli.hasOption( CHECKSUM_WARNING_POLICY ) )
             {
                 req.setGlobalChecksumPolicy( InvocationRequest.CHECKSUM_POLICY_WARN );
             }
 
             if ( cli.hasOption( ALTERNATE_USER_SETTINGS ) )
             {
                 req.setUserSettingsFile( new File( cli.getOptionValue( ALTERNATE_USER_SETTINGS ) ) );
             }
 
             if ( cli.hasOption( FAIL_AT_END ) )
             {
                 req.setFailureBehavior( InvocationRequest.REACTOR_FAIL_AT_END );
             }
             else if ( cli.hasOption( FAIL_FAST ) )
             {
                 req.setFailureBehavior( InvocationRequest.REACTOR_FAIL_FAST );
             }
             if ( cli.hasOption( FAIL_NEVER ) )
             {
                 req.setFailureBehavior( InvocationRequest.REACTOR_FAIL_NEVER );
             }
             if ( cli.hasOption( ALTERNATE_POM_FILE ) )
             {
                 if (req.getPomFileName() != null) {
                     getCaller().getLog().info("pomFileName is already set, ignoring the -f argument" );
                 }
                 else {
                     req.setPomFileName( cli.getOptionValue( ALTERNATE_POM_FILE ) );
                 }
             }
         }
         catch ( Exception e ) {
             throw new MojoExecutionException("Failed to re-parse additional arguments for Maven invocation.", e );
         }
     }
     
     /**
      * Executes the {@code install-file} goal with the new pom against the artifact file.
      * 
      * @param artifact {@link File} instance to install
      */
     public void installArtifact(final File artifact, 
                                 final File sources,
                                 final File mavenHome,
                                 final String groupId,
                                 final String artifactId,
                                 final String version,
                                 final String repositoryId) throws MojoExecutionException {
         extractBuildXml();
     
         filterTempPom(groupId, artifactId, artifact.getName().endsWith("jar") ? "jar" : "war", version);
 
         final Invoker invoker = new DefaultInvoker().setMavenHome(mavenHome);
         
         final String additionalArguments = "";
 
         getCaller().getLog().debug("Setting up properties for installing the artifact");
         final InvocationRequest req = new DefaultInvocationRequest()
             .setInteractive(false)
             .setProperties(new Properties() {{
                 setProperty("pomFile", getTempPomPath());
                 if (repositoryId != null) {
                     setProperty("repositoryId", repositoryId);
                 }
                 if (sources != null) {
                     try {
                        setProperty("sources", zipSourcesIfRequired(sources).getCanonicalPath());
                     }
                     catch (Exception e) {
                         throw new MojoExecutionException("Cannot get path for the sources file ", e);
                     }                            
                 }
                 try {
                     setProperty("file", artifact.getCanonicalPath());
                 }
                 catch (Exception e) {
                     throw new MojoExecutionException("Cannot get path for the war file ", e);
                 }
                 setProperty("updateReleaseInfo", "true");
             }});
 
         getCaller().getLog().debug("Properties used for installArtifact are:");
         try {
             req.getProperties().list(System.out);
         }
         catch (Exception e) {
         }
 
         try {
             setupRequest(req, additionalArguments);
 
             if (repositoryId == null) {
                 req.setGoals(new ArrayList<String>() {{ add("install:install-file"); }});
             }
             else {
                 req.setGoals(new ArrayList<String>() {{ add("deploy:deploy-file"); }});
             }
 
             try {
                 final InvocationResult invocationResult = invoker.execute(req);
 
                 if ( invocationResult.getExecutionException() != null ) {
                     throw new MojoExecutionException("Error executing Maven.",
                                                      invocationResult.getExecutionException());
                 }
                     
                 if (invocationResult.getExitCode() != 0) {
                     throw new MojoExecutionException(
                         "Maven execution failed, exit code: \'" + invocationResult.getExitCode() + "\'");
                 }
             }
             catch (MavenInvocationException e) {
                 throw new MojoExecutionException( "Failed to invoke Maven build.", e );
             }
         }
         finally {
             /*
               if ( settingsFile != null && settingsFile.exists() && !settingsFile.delete() )
               {
               settingsFile.deleteOnExit();
               }
             */
         }
     }
 
     /**
      * Executes the {@code install-file} goal with the new pom against the artifact file.
      * 
      * @param artifact {@link File} instance to install
      */
     public void filterTempPom(final String groupId,
                               final String artifactId,
                               final String packaging,
                               final String version) throws MojoExecutionException {
         getCaller().getLog().info("Extracting the Temp POM");
     
         try {
             executeCommandLine("ant",
                                "-Dsource=" + System.getProperty("java.io.tmpdir") + File.separator + "pom.xml",
                                "-Dtarget=" + System.getProperty("java.io.tmpdir") + File.separator + "prototype-pom.xml",
                                "-DgroupId=" +  groupId,
                                "-DartifactId=" + artifactId,
                                "-Dpackaging=" + packaging,
                                "-Dversion=" + version);
         }
         catch (Exception e) {
             throw new MojoExecutionException("Error trying to filter the pom with ant ", e);
         }
     }
 
     /**
      * Temporary POM location
      * 
      * @return String value the path of the temporary POM
      */
     protected String getTempPomPath() {
         return System.getProperty("java.io.tmpdir") + File.separator + "prototype-pom.xml";
     }
     
     /**
      * Puts ant build file in the system temp directory. build.xml is extracted
      * from the plugin.
      */
     public void extractBuildXml() throws MojoExecutionException {
         getCaller().getLog().info("Extracting the build.xml");
         
         final InputStream pom_is = getClass().getClassLoader().getResourceAsStream("prototype-resources/build.xml");
         
         byte[] fileBytes = null;
         try {
             final DataInputStream dis = new DataInputStream(pom_is);
             fileBytes = new byte[dis.available()];
             dis.readFully(fileBytes);
             dis.close();
         }
         catch (Exception e) {
             throw new MojoExecutionException("Wasn't able to read in the prototype pom", e);
         }
         finally {
             try {
                 pom_is.close();
             }
             catch (Exception e) {
                 // Ignore exceptions
             }
         }
         
         try {
             final FileOutputStream fos = new FileOutputStream(System.getProperty("java.io.tmpdir") + File.separator + "build.xml");
             try {
                 fos.write(fileBytes);
             }
             finally {
                 fos.close();
             }
         }
         catch (Exception e) {
             throw new MojoExecutionException("Could not write temporary pom file", e);
         }
     }
 
     /**
      * Puts temporary pom in the system temp directory. prototype-pom.xml is extracted
      * from the plugin.
      */
     public void extractTempPom() throws MojoExecutionException {
         getCaller().getLog().info("Extracting the Temp Pom");
         
         final InputStream pom_is = getClass().getClassLoader().getResourceAsStream("prototype-resources/pom.xml");
         
         byte[] fileBytes = null;
         try {
             final DataInputStream dis = new DataInputStream(pom_is);
             fileBytes = new byte[dis.available()];
             dis.readFully(fileBytes);
             dis.close();
         }
         catch (Exception e) {
             throw new MojoExecutionException("Wasn't able to read in the prototype pom", e);
         }
         finally {
             try {
                 pom_is.close();
             }
             catch (Exception e) {
                 // Ignore exceptions
             }
         }
         
         try {
             final FileOutputStream fos = new FileOutputStream(System.getProperty("java.io.tmpdir") + File.separator + "pom.xml");
             try {
                 fos.write(fileBytes);
             }
             finally {
                 fos.close();
             }
         }
         catch (Exception e) {
             throw new MojoExecutionException("Could not write temporary pom file", e);
         }
     }
     
     protected File zipSourcesIfRequired(File sources) {
         if (sources.isFile()){
             //Already a zip
             return sources;
         }
         final File zipFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "sources.zip");
         zipFile.deleteOnExit();
         ZipArchiver zipArchiver = new ZipArchiver();
         zipArchiver.addDirectory(sources, new String[]{"**/*"}, new String[]{});
         zipArchiver.setDestFile(zipFile);
         try {
             zipArchiver.createArchive();
         } catch (IOException e) {
             throw new RuntimeException("Unable to zip source directory", e);
         }
         return zipFile;
     }
 
     public void setCaller(final AbstractMojo caller) {
         this.caller = caller;
     }
     
     public AbstractMojo getCaller() {
         return this.caller;
     }
 
 }
