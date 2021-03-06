 package org.codehaus.mojo.osxappbundle;
 
 /*
  * Copyright 2001-2008 The Codehaus.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
 import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectHelper;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.exception.MethodInvocationException;
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.ResourceNotFoundException;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.zip.ZipArchiver;
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.codehaus.plexus.velocity.VelocityComponent;
 import org.codehaus.mojo.osxappbundle.encoding.DefaultEncodingDetector;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.ByteArrayInputStream;
 import java.io.Writer;
 import java.io.OutputStreamWriter;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Arrays;
 
 /**
  * Package dependencies as an Application Bundle for Mac OS X.
  *
  * @goal bundle
  * @phase package
  * @requiresDependencyResolution runtime
  */
 public class CreateApplicationBundleMojo
     extends AbstractMojo
 {
 
     /**
      * Default includes - everything is included.
      */
     private static final String[] DEFAULT_INCLUDES = {"**/**"};
 
     /**
      * The Maven Project Object
      *
      * @parameter default-value="${project}"
      * @readonly
      */
     private MavenProject project;
 
     /**
      * The directory where the application bundle will be created
      *
      * @parameter default-value="${project.build.directory}/${project.build.finalName}";
      */
     private File buildDirectory;
 
     /**
      * The location of the generated disk image file
      *
      * @parameter default-value="${project.build.directory}/${project.build.finalName}.dmg"
      */
     private File diskImageFile;
 
 
     /**
      * The location of the Java Application Stub
      *
      * @parameter default-value="/System/Library/Frameworks/JavaVM.framework/Versions/Current/Resources/MacOS/JavaApplicationStub";
      */
     private File javaApplicationStub;
 
     /**
      * The main class to execute when double-clicking the Application Bundle
      *
      * @parameter expression="${mainClass}"
      * @required
      */
     private String mainClass;
 
     /**
      * The name of the Bundle. This is the name that is given to the application bundle;
      * and it is also what will show up in the application menu, dock etc.
      *
      * @parameter default-value="${project.name}"
      * @required
      */
     private String bundleName;
 
     /**
      * The icon file for the bundle
      *
      * @parameter
      */
     private File iconFile;
 
     /**
      * The label of the volume. This is the name that will appear in Finder when mounting the disk image.
      *
      * @parameter
      */
     private String volumeLabel;
 
     /**
      * The version of the project. Will be used as the value of the CFBundleVersion key.
      *
      * @parameter default-value="${project.version}"
      */
     private String version;
 
     /**
      * A value for the JVMVersion key.
      *
      * @parameter default-value="1.4+"
      */
     private String jvmVersion;
 
     /**
      * The location of the produced Zip file containing the bundle.
      *
      * @parameter default-value="${project.build.directory}/${project.build.finalName}-app.zip"
      */
     private File zipFile;
 
     /**
      * Paths to be put on the classpath in addition to the projects dependencies.
      * Might be useful to specify locations of dependencies in the provided scope that are not distributed with
      * the bundle but have a known location on the system.
      * {@see http://jira.codehaus.org/browse/MOJO-874}
      *
      * @parameter
      */
     private List additionalClasspath;
 
     /**
      * Additional resources (as a list of FileSet objects) that will be copies into
      * the build directory and included in the .dmg and .zip files alongside with the
      * application bundle.
      *
      * @parameter
      */
     private List additionalResources;
 
     /**
      * Velocity Component.
      *
      * @component
      * @readonly
      */
     private VelocityComponent velocity;
 
     /**
      * The location of the template for Info.plist.
      * Classpath is checked before the file system.
      *
      * @parameter default-value="org/codehaus/mojo/osxappbundle/Info.plist.template"
      */
     private String dictionaryFile;
 
     /**
      * Options to the JVM, will be used as the value of VMOptions in Info.plist.
      *
      * @parameter
      */
     private String vmOptions;
 
 
     /**
      * The Zip archiver.
      *
      * @component
      * @readonly
      */
     private MavenProjectHelper projectHelper;
 
     /**
      * The Zip archiver.
      *
      * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#zip}"
      * @required
      * @readonly
      */
     private ZipArchiver zipArchiver;
 
     /**
      * If this is set to <code>true</code>, the generated DMG file will be internet-enabled.
      * The default is ${false}
      *
      * @parameter default-value="false"
      */
     private boolean internetEnable;
 
     /**
      * Comma separated list of ArtifactIds to exclude from the dependency copy.
      * @parameter default-value=""
      */
     private Set excludeArtifactIds;
     
     /**
      * Possible locations where the SetFile tool can be.
      */
     private static final String[] SET_FILE_LOCATIONS = new String[]{
         "/usr/bin/SetFile",
         "/Developer/Tools/SetFile",
         "/Applications/Xcode.app/Contents/Developer/Tools/SetFile"
     };
 
     /**
      * The location where the SetFile tool was found; or null if the tool wasn't found in any of the possible locations. 
      */
     private String setFilePath;
     {
         for ( int i = 0; i < SET_FILE_LOCATIONS.length; ++i )
         {
             if ( new File( SET_FILE_LOCATIONS[i] ).exists() )
             {
                 setFilePath = SET_FILE_LOCATIONS[i];
                 break;
             }
         }
     }
 
     /**
      * Bundle project as a Mac OS X application bundle.
      *
      * @throws MojoExecutionException If an unexpected error occurs during packaging of the bundle.
      */
     public void execute()
         throws MojoExecutionException
     {
 
         // Set up and create directories
         buildDirectory.mkdirs();
 
         File bundleDir = new File( buildDirectory, cleanBundleName(bundleName) + ".app" );
         bundleDir.mkdirs();
 
         File contentsDir = new File( bundleDir, "Contents" );
         contentsDir.mkdirs();
 
         File resourcesDir = new File( contentsDir, "Resources" );
         resourcesDir.mkdirs();
 
         File javaDirectory = new File( resourcesDir, "Java" );
         javaDirectory.mkdirs();
 
         File macOSDirectory = new File( contentsDir, "MacOS" );
         macOSDirectory.mkdirs();
 
         // Copy in the native java application stub
         File stub = new File( macOSDirectory, javaApplicationStub.getName() );
         if(! javaApplicationStub.exists()) {
             String message = "Can't find JavaApplicationStub binary. File does not exist: " + javaApplicationStub;
 
             if(! isOsX() ) {
                 message += "\nNOTICE: You are running the osxappbundle plugin on a non OS X platform. To make this work you need to copy the JavaApplicationStub binary into your source tree. Then configure it with the 'javaApplicationStub' configuration property.\nOn an OS X machine, the JavaApplicationStub is typically located under /System/Library/Frameworks/JavaVM.framework/Versions/Current/Resources/MacOS/JavaApplicationStub";
             }
 
             throw new MojoExecutionException( message);
 
         } else {
             try
             {
                 FileUtils.copyFile( javaApplicationStub, stub );
 
                 // Make the stub executable
                 Commandline chmod = new Commandline();
                 chmod.setExecutable( "chmod" );
                 chmod.createArgument().setValue( "755" );
                 chmod.createArgument().setFile( stub );
                 chmod.execute();
             }
             catch ( IOException e )
             {
                 throw new MojoExecutionException(
                     "Could not copy file " + javaApplicationStub + " to directory " + macOSDirectory, e );
             }
             catch ( CommandLineException e )
             {
                 getLog().warn( "Failed to mark the Java stub as executable, application may not run properly." );
             }
         }
 
         // Copy icon file to the bundle if specified
         if ( iconFile != null )
         {
             try
             {
                 FileUtils.copyFileToDirectory( iconFile, resourcesDir );
             }
             catch ( IOException e )
             {
                 throw new MojoExecutionException( "Error copying file " + iconFile + " to " + resourcesDir, e );
             }
         }
 
         // Resolve and copy in all dependencies from the pom
         List files = copyDependencies( javaDirectory );
 
         // Create and write the Info.plist file
         File infoPlist = new File( bundleDir, "Contents/Info.plist" );
         writeInfoPlist( infoPlist, files );
 
         // Copy specified additional resources into the top level directory
         if (additionalResources != null && !additionalResources.isEmpty())
         {
             copyResources( additionalResources );
         }
 
         createZip();
 
         if ( isOsX() )
         {
             // This makes sure that the .app dir is actually registered as an application bundle
             setFileAttributes(bundleDir, "B");
 
             // Create the DMG
             createDMG();
         }
     }
 
     /**
      * The bundle name is used in paths, so we need to clean it for
      * unwanted characters, like ":" on Windows.
      * @param bundleName the "unclean" bundle name.
      * @return a clean bundle name
      */
     private String cleanBundleName(String bundleName) {
         return bundleName.replace(':', '-');
     }
 
     private boolean isOsX()
     {
         return System.getProperty( "mrj.version" ) != null;
     }
 
     /**
      * Copy all dependencies into the $JAVAROOT directory
      *
      * @param javaDirectory where to put jar files
      * @return A list of file names added
      * @throws MojoExecutionException
      */
     private List copyDependencies( File javaDirectory )
         throws MojoExecutionException
     {
 
         ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
 
         List list = new ArrayList();
 
         File repoDirectory = new File(javaDirectory, "repo");
         repoDirectory.mkdirs();
 
         // First, copy the project's own artifact
         File artifactFile = project.getArtifact().getFile();
 
         // Pom modules have no artifact file
         if(artifactFile != null) {
             list.add( repoDirectory.getName() +"/" +layout.pathOf(project.getArtifact()));
 
             try
             {
                 FileUtils.copyFile( artifactFile, new File(repoDirectory, layout.pathOf(project.getArtifact())) );
             }
             catch ( IOException e )
             {
                 throw new MojoExecutionException( "Could not copy artifact file " + artifactFile + " to " + javaDirectory );
             }
         }
 
         Set artifacts = project.getArtifacts();
 
         Iterator i = artifacts.iterator();
 
         while ( i.hasNext() )
         {
             Artifact artifact = (Artifact) i.next();
             
             String artifactId = artifact.getArtifactId();
             if (excludeArtifactIds != null && excludeArtifactIds.contains(artifactId))
             {
                 getLog().info( "Skipping excluded artifact: " + artifact.toString() );
                 continue;
             }
 
             File file = artifact.getFile();
             File dest = new File(repoDirectory, layout.pathOf(artifact));
 
             getLog().debug( "Adding " + file );
 
             try
             {
                 FileUtils.copyFile( file, dest);
             }
             catch ( IOException e )
             {
                 throw new MojoExecutionException( "Error copying file " + file + " into " + javaDirectory, e );
             }
 
             list.add( repoDirectory.getName() +"/" + layout.pathOf(artifact) );
         }
 
         return list;
 
     }
 
     /**
      * Writes an Info.plist file describing this bundle.
      *
      * @param infoPlist The file to write Info.plist contents to
      * @param files     A list of file names of the jar files to add in $JAVAROOT
      * @throws MojoExecutionException
      */
     private void writeInfoPlist( File infoPlist, List files )
         throws MojoExecutionException
     {
 
         VelocityContext velocityContext = new VelocityContext();
 
         velocityContext.put( "mainClass", mainClass );
         velocityContext.put( "cfBundleExecutable", javaApplicationStub.getName());
         velocityContext.put( "vmOptions", vmOptions);
         velocityContext.put( "bundleName", cleanBundleName(bundleName) );
 
         velocityContext.put( "iconFile", iconFile == null ? "GenericJavaApp.icns" : iconFile.getName() );
 
         velocityContext.put( "version", version );
 
         velocityContext.put( "jvmVersion", jvmVersion );
 
         addMavenPropertiesToVelocity( velocityContext );
 
         StringBuffer jarFilesBuffer = new StringBuffer();
 
         jarFilesBuffer.append( "<array>" );
         for ( int i = 0; i < files.size(); i++ )
         {
             String name = (String) files.get( i );
             jarFilesBuffer.append( "<string>" );
             jarFilesBuffer.append( "$JAVAROOT/" ).append( name );
             jarFilesBuffer.append( "</string>" );
 
         }
         if ( additionalClasspath != null )
         {
             for ( int i = 0; i < additionalClasspath.size(); i++ )
             {
                 String pathElement = (String) additionalClasspath.get( i );
                 jarFilesBuffer.append( "<string>" );
                 jarFilesBuffer.append( pathElement );
                 jarFilesBuffer.append( "</string>" );
 
             }
         }
         jarFilesBuffer.append( "</array>" );
 
         velocityContext.put( "classpath", jarFilesBuffer.toString() );
 
         try
         {
 
             String encoding = detectEncoding(dictionaryFile, velocityContext);
 
             getLog().debug( "Detected encoding " + encoding + " for dictionary file " +dictionaryFile  );
 
             Writer writer = new OutputStreamWriter( new FileOutputStream(infoPlist), encoding );
 
             velocity.getEngine().mergeTemplate( dictionaryFile, encoding, velocityContext, writer );
 
             writer.close();
         }
         catch ( IOException e )
         {
             throw new MojoExecutionException( "Could not write Info.plist to file " + infoPlist, e );
         }
         catch ( ParseErrorException e )
         {
             throw new MojoExecutionException( "Error parsing " + dictionaryFile, e );
         }
         catch ( ResourceNotFoundException e )
         {
             throw new MojoExecutionException( "Could not find resource for template " + dictionaryFile, e );
         }
         catch ( MethodInvocationException e )
         {
             throw new MojoExecutionException(
                 "MethodInvocationException occured merging Info.plist template " + dictionaryFile, e );
         }
         catch ( Exception e )
         {
             throw new MojoExecutionException( "Exception occured merging Info.plist template " + dictionaryFile, e );
         }
 
     }
 
     private void addMavenPropertiesToVelocity( VelocityContext velocityContext )
     {
         Properties mavenProps = project.getProperties();
         Iterator propertyIterator = mavenProps.entrySet().iterator();
 
         while ( propertyIterator.hasNext() )
         {
             Map.Entry propertyEntry = (Map.Entry) propertyIterator.next();
             String key = (String) propertyEntry.getKey();
             String value = (String) propertyEntry.getValue();
             if ( key.indexOf( "password" ) >= 0 || key.indexOf( "passphrase" ) >= 0 )
             {
                 // we do not like to export sensible data!
                 continue;
             }
 
             velocityContext.put( "maven." + key, value );
         }
 
     }
 
     private String detectEncoding( String dictionaryFile, VelocityContext velocityContext )
         throws Exception
     {
         StringWriter sw = new StringWriter();
         velocity.getEngine().mergeTemplate( dictionaryFile, "utf-8", velocityContext, sw );
         return new DefaultEncodingDetector().detectXmlEncoding( new ByteArrayInputStream(sw.toString().getBytes( "utf-8" )) );
     }
 
     /**
      * Copies given resources to the build directory. 
      *
      * @param fileSets A list of FileSet objects that represent additional resources to copy.
      * @throws MojoExecutionException In case of a resource copying error.
      */
     private void copyResources( List fileSets )
         throws MojoExecutionException
     {
         final String[] emptyStrArray = {};
         
         for ( Iterator it = fileSets.iterator(); it.hasNext(); )
         {
             FileSet fileSet = (FileSet) it.next();
 
             File resourceDirectory = new File( fileSet.getDirectory() );
             if ( !resourceDirectory.isAbsolute() )
             {
                 resourceDirectory = new File( project.getBasedir(), resourceDirectory.getPath() );
             }
 
             if ( !resourceDirectory.exists() )
             {
                 getLog().info( "Additional resource directory does not exist: " + resourceDirectory );
                 continue;
             }
 
             DirectoryScanner scanner = new DirectoryScanner();
 
             scanner.setBasedir( resourceDirectory );
             if ( fileSet.getIncludes() != null && !fileSet.getIncludes().isEmpty() )
             {
                 scanner.setIncludes( (String[]) fileSet.getIncludes().toArray( emptyStrArray ) );
             }
             else
             {
                 scanner.setIncludes( DEFAULT_INCLUDES );
             }
 
             if ( fileSet.getExcludes() != null && !fileSet.getExcludes().isEmpty() )
             {
                 scanner.setExcludes( (String[]) fileSet.getExcludes().toArray( emptyStrArray ) );
             }
 
             if (fileSet.isUseDefaultExcludes())
             {
                 scanner.addDefaultExcludes();
             }
 
             scanner.scan();
 
             List includedFiles = Arrays.asList( scanner.getIncludedFiles() );
 
             getLog().info( "Copying " + includedFiles.size() + " additional resource"
                            + ( includedFiles.size() > 1 ? "s" : "" ) );
 
             for ( Iterator j = includedFiles.iterator(); j.hasNext(); )
             {
                 String destination = (String) j.next();
                 File source = new File( resourceDirectory, destination );
                 File destinationFile = new File( buildDirectory, destination );
 
                 if ( !destinationFile.getParentFile().exists() )
                 {
                     destinationFile.getParentFile().mkdirs();
                 }
 
                 try
                 {
                     FileUtils.copyFile(source, destinationFile);
                 }
                 catch ( IOException e )
                 {
                     throw new MojoExecutionException( "Error copying additional resource " + source, e );
                 }
             }
         }
     }
 
     /**
      * Create a .dmg disk image with the application.
      *
      * @throws MojoExecutionException if the operation is interrupted
      */
     private void createDMG() throws MojoExecutionException
     {
         getLog().info("Building DMG: " + diskImageFile.getAbsolutePath());
         Commandline dmg = new Commandline();
         try
         {
             dmg.setExecutable( "hdiutil" );
             dmg.createArgument().setValue( "create" );
             // The partition type: force HFS+ since otherwise the type of the source folder will be used,
             // and we don't know what type that might be. For reproducible builds always use a specific type.
             dmg.createArgument().setValue( "-fs" );
             dmg.createArgument().setValue( "HFS+" );
             // Set the volume label
             dmg.createArgument().setValue( "-volname" );
             dmg.createArgument().setValue( volumeLabel != null ? volumeLabel : bundleName );
             // The source directory
             dmg.createArgument().setValue( "-srcfolder" );
             dmg.createArgument().setFile( buildDirectory );
            // The target image file; overwrite if it exists
            dmg.createArgument().setValue( "-ov" );
             dmg.createArgument().setFile( targetFile );
 
             dmg.execute().waitFor();
         }
         catch ( CommandLineException e )
         {
             throw new MojoExecutionException( "Error creating disk image " + diskImageFile, e );
         }
         catch ( InterruptedException e )
         {
             throw new MojoExecutionException( "Thread was interrupted while creating DMG " + diskImageFile, e );
         }
         if(internetEnable) {
             try {
 
                 Commandline internetEnable = new Commandline();
 
                 internetEnable.setExecutable("hdiutil");
                 internetEnable.createArgument().setValue("internet-enable" );
                 internetEnable.createArgument().setValue("-yes");
                 internetEnable.createArgument().setValue(diskImageFile.getAbsolutePath());
 
                 internetEnable.execute();
             } catch (CommandLineException e) {
                 throw new MojoExecutionException("Error internet enabling disk image: " + diskImageFile, e);
             }
         }
         projectHelper.attachArtifact(project, "dmg", null, diskImageFile);
     }
 
     /**
      * Create a .zip archive with the application.
      *
      * @throws MojoExecutionException if the operation is interrupted
      */
     private void createZip() throws MojoExecutionException
     {
         zipArchiver.setDestFile( zipFile );
         try
         {
             String[] stubPattern = {buildDirectory.getName() + "/" + cleanBundleName(bundleName) + ".app/Contents/MacOS/"
                                     + javaApplicationStub.getName()};
 
             zipArchiver.addDirectory( buildDirectory.getParentFile(), new String[]{buildDirectory.getName() + "/**"},
                     stubPattern);
 
             DirectoryScanner scanner = new DirectoryScanner();
             scanner.setBasedir( buildDirectory.getParentFile() );
             scanner.setIncludes( stubPattern);
             scanner.scan();
 
             String[] stubs = scanner.getIncludedFiles();
             for ( int i = 0; i < stubs.length; i++ )
             {
                 String s = stubs[i];
                 zipArchiver.addFile( new File( buildDirectory.getParentFile(), s ), s, 0755 );
             }
 
             zipArchiver.createArchive();
             projectHelper.attachArtifact(project, "zip", null, zipFile);
         }
         catch ( ArchiverException e )
         {
             throw new MojoExecutionException( "Could not create zip archive of application bundle in " + zipFile, e );
         }
         catch ( IOException e )
         {
             throw new MojoExecutionException( "IOException creating zip archive of application bundle in " + zipFile,
                                               e );
         }
     }
 
     /**
      * Set special attributes on a file or directory, using the OS X specific SetFile tool.
      *
      * @param file the file or directory to alter
      * @param attributes the attributes to set, as a set of character flags; see the SetTool manpage for possible values
      */
     private void setFileAttributes(File file, String attributes)
     {
         if ( setFilePath != null )
         {
             Commandline setFile = new Commandline();
             try
             {
                 setFile.setExecutable( setFilePath );
                 setFile.createArgument().setValue( "-a" );
                 setFile.createArgument().setValue( attributes );
                 setFile.createArgument().setFile( file );
 
                 setFile.execute();
             }
             catch ( CommandLineException e )
             {
                 getLog().warn( "Error executing " + setFile, e );
             }
         }
         else
         {
             getLog().warn( "Could  not set special file attributes. SetFile not found, is Xcode installed?" );
         }
     }
 }
