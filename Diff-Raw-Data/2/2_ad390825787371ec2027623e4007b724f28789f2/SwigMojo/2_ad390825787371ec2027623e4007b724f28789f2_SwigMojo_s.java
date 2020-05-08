 package org.apache.maven.plugin.swig;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.DefaultArtifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.handler.ArtifactHandler;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.artifact.versioning.VersionRange;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.nar.AbstractNarLayout;
 import org.apache.maven.plugin.nar.Linker;
 import org.apache.maven.plugin.nar.NarArtifact;
 import org.apache.maven.plugin.nar.NarInfo;
 import org.apache.maven.plugin.nar.NarLayout;
 import org.apache.maven.plugin.nar.NarManager;
 import org.apache.maven.plugin.nar.NarUtil;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
 import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
 import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
 import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 
 /**
  * Compiles swg files using the swig compiler.
  * 
  * @goal generate
  * @description Compiles swg files using the swig compiler.
  * @phase generate-sources
  * @requiresDependencyResolution compile
  * @author Mark Donszelmann
  */
 public class SwigMojo
     extends AbstractMojo
 {
 
     /**
      * Skip the running of SWIG
      * 
      * @parameter expression="${swig.skip}" default-value="false"
      */
     private boolean skip;
 
     /**
      * Force the running of SWIG
      * 
      * @parameter expression="${swig.force}" default-value="false"
      */
     private boolean force;
 
     /**
      * Define symbol for conditional compilation, same as -D option for swig.
      * 
      * @parameter
      */
     private List defines;
 
     /**
      * Sets a fake version number, same as -fakeversion for swig.
      * 
      * @parameter
      */
     private String fakeVersion;
 
     /**
      * Enable C++ processing, same as -c++ option for swig.
      * 
      * @parameter expression="${swig.cpp}" default-value="false"
      */
     private boolean cpp;
 
     /**
      * Add include paths. By default the current directory is scanned.
      * 
      * @parameter
      */
     private List includePaths;
 
     /**
      * List of warning numbers to be suppressed, same as -w option for swig.
      * 
      * @parameter expression="${swig.noWarn}"
      */
     private String noWarn;
 
     /**
      * Enable all warnings, same as -Wall
      * 
      * @parameter expression="${swig.warnAll}" default-value="false"
      */
     private boolean warnAll;
 
     /**
      * Treat warnings as errors, same as -Werror
      * 
      * @parameter expression="${swig.warnError}" default-value="false"
      */
     private boolean warnError;
 
     /**
      * The target directory into which to generate the output.
      * 
      * @parameter expression="${project.build.directory}/swig"
      */
     private File targetDirectory;
 
     /**
      * The unpack directory into which to unpack the swig executable.
      * 
      * @parameter expression="${project.build.directory}/nar/dependencies"
      */
     private File unpackDirectory;
 
     /**
      * The package name for the generated java files (fully qualified ex: org.apache.maven.jni).
      * 
      * @parameter expression="${swig.packageName}"
      */
     private String packageName;
 
     /**
      * The output filename. Defaults to ${source}.cpp or .c depending on cpp option.
      * 
      * @parameter
      */
     private String outFile;
 
     /**
      * The target directory into which to generate the java output, becomes -outdir option for swig.
      * 
      * @parameter expression="${project.build.directory}/swig/java"
      */
     private String javaTargetDirectory;
 
     /**
      * Remove all *.java files from the output directory. The output directory is formed by
      * ${javaTargetDirectory}/${packageName}. This setting is ignored (false) if no packageName is supplied. All *.java
      * are deleted from the output directory just before the swig command is run. This allows the user to configure to
      * have the java files of the swig command in the src directory tree.
      * 
      * @parameter expression="false"
      */
     private boolean cleanOutputDirectory;
 
     /**
      * The directory to look for swig files and swig include files. Also added to -I flag when swig is run.
      * 
      * @parameter expression="${basedir}/src/main/swig"
      * @required
      */
     private String sourceDirectory;
 
     /**
      * The swig file to process, normally in source directory set by swigDirectory.
      * 
      * @parameter
      * @required
      */
     private String source;
 
     /**
      * The Architecture for picking up swig, Some choices are: "x86", "i386", "amd64", "ppc", "sparc", ... Defaults to a
      * derived value from ${os.arch}
      * 
      * @parameter expression="${os.arch}"
      * @required
      */
     private String architecture;
 
     /**
      * The Operating System for picking up swig. Some choices are: "Windows", "Linux", "MacOSX", "SunOS", ... Defaults
      * to a derived value from ${os.name}
      * 
      * @parameter expression=""
      */
     private String os;
 
     /**
      * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation
      * 
      * @parameter expression="${idlj.staleMillis}" default-value="0"
      * @required
      */
     private int staleMillis;
 
     /**
      * Swig Executable (overrides built-in or user configured reference to NAR)
      * 
      * @parameter expression="${swig.exec}"
      */
     private String exec;
 
     /**
      * GroupId for the swig NAR
      * 
      * @parameter expression="${swig.groupId}" default-value="org.swig"
      */
     private String groupId;
 
     /**
      * ArtifactId for the swig NAR
      * 
      * @parameter expression="${swig.artifactId}" default-value="nar-swig"
      */
     private String artifactId;
 
     /**
      * Version for the swig NAR
      * 
      * @parameter expression="${swig.version}"
      */
     private String version;
 
     /**
      * Layout to be used for building and unpacking artifacts
      * 
      * @parameter expression="${nar.layout}" default-value="org.apache.maven.plugin.nar.NarLayout21"
      * @required
      */
     private String layout;
 
     /**
      * Adds system libraries to the linker. Will work in combination with &lt;sysLibs&gt;. The format is comma
      * separated, colon-delimited values (name:type), like "dl:shared, pthread:shared".
      *
      * @parameter expression=""
      */
     private String sysLibSet;
 
     /**
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * @parameter expression="${localRepository}"
      * @required
      * @readonly
      */
     private ArtifactRepository localRepository;
 
     /**
      * Artifact handler
      * 
      * @component role="org.apache.maven.artifact.handler.ArtifactHandler"
      * @required
      * @readonly
      */
     private ArtifactHandler artifactHandler;
 
     /**
      * Artifact resolver, needed to download source jars for inclusion in classpath.
      * 
      * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
      * @required
      * @readonly
      */
     private ArtifactResolver artifactResolver;
 
    /**
      * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
      * @required
      * @readonly
      */
    private ArtifactFactory artifactFactory;
 
    /**
      * Remote repositories which will be searched for source attachments.
      * 
      * @parameter expression="${project.remoteArtifactRepositories}"
      * @required
      * @readonly
      */
     private List remoteArtifactRepositories;
 
     /**
      * To look up Archiver/UnArchiver implementations
      * 
      * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
      * @required
      */
     private ArchiverManager archiverManager;
     
     /**
      * To provide some compiler options to the nar plugin
      * @parameter
      */
     private String[] compilerOptions;
     
     /**
      * To provide some linker options to the nar plugin
      * @parameter
      */
     private String[] linkerOptions;
 
     private NarManager narManager;
 
     public final void execute()
         throws MojoExecutionException, MojoFailureException
     {
         if ( skip )
         {
             getLog().warn( "SKIPPED Running SWIG compiler on " + source + " ..." );
             return;
         }
 
         configureNarPlugin();
 
         if ( !sourceDirectory.endsWith( "/" ) )
         {
             sourceDirectory = sourceDirectory + "/";
         }
         File sourceDir = new File( sourceDirectory );
         File sourceFile = new File( sourceDir, source );
         if ( !sourceDir.exists() || !sourceFile.exists() )
         {
             getLog().info( "No SWIG sources found" );
             return;
         }
 
         os = NarUtil.getOS( os );
 
         Linker linker = new Linker( NarUtil.getLinkerName( architecture, os, null ) );
         narManager = new NarManager( getLog(), localRepository, project, architecture, os, linker );
 
         targetDirectory = new File( targetDirectory, cpp ? "c++" : "c" );
         if ( !targetDirectory.exists() )
         {
             targetDirectory.mkdirs();
         }
 
         if ( project != null )
         {
             project.addCompileSourceRoot( javaTargetDirectory );
             project.addCompileSourceRoot( targetDirectory.getPath() );
         }
 
         if ( packageName != null )
         {
             if ( !javaTargetDirectory.endsWith( "/" ) )
             {
                 javaTargetDirectory = javaTargetDirectory + "/";
             }
             javaTargetDirectory += packageName.replace( '.', File.separatorChar );
         }
 
         if ( !FileUtils.fileExists( javaTargetDirectory ) )
         {
             FileUtils.mkdir( javaTargetDirectory );
         }
 
         // make sure all NAR dependencies are downloaded and unpacked
         // even if packaging is NOT nar
         // in nar packaging, downloading happens in generate-sources phase and
         // thus may be too late
         // unpacking happens in process-sources which is definitely too late
         // so we need to handle this here ourselves.
         NarLayout narLayout = AbstractNarLayout.getLayout( layout, getLog() );
         List narArtifacts = narManager.getNarDependencies( "compile" );
         narManager.downloadAttachedNars( narArtifacts, remoteArtifactRepositories, artifactResolver, null );
         narManager.unpackAttachedNars( narArtifacts, archiverManager, null, os, narLayout, unpackDirectory );
 
         File swig, swigInclude, swigJavaInclude;
         if ( exec == null )
         {
             // NOTE, since a project will just load this as a plugin, there is
             // no way to look up the org.swig:nar-swig dependency, so we hardcode
             // that in here, but it is configurable in the configuration part of this plugin.
 
             // if version not specified use maven-swig-plugin version
             if ( version == null )
             {
                 Plugin swigPlugin =
                     (Plugin) project.getBuild().getPluginsAsMap().get( "org.apache.maven.plugins:maven-swig-plugin" );
                 version = swigPlugin.getVersion();
             }
             Artifact swigJar = artifactFactory.createArtifactWithClassifier(
                     groupId, artifactId, version, "nar", "");
 //                new DefaultArtifact( groupId, artifactId, VersionRange.createFromVersion( version ), "compile", "nar",
 //                                     "", artifactHandler );
 
             // download jar file
             try
             {
                 getLog().debug( "maven-swig-plugin: Resolving " + swigJar );
                 artifactResolver.resolve( swigJar, remoteArtifactRepositories, localRepository );
             }
             catch ( ArtifactNotFoundException e )
             {
                 String message = "Jar not found " + swigJar;
                 throw new MojoExecutionException( message, e );
             }
             catch ( ArtifactResolutionException e )
             {
                 String message = "Jar cannot be resolved " + swigJar;
                 throw new MojoExecutionException( message, e );
             }
 
             NarInfo info = narManager.getNarInfo( swigJar );
             getLog().debug( info.toString() );
             NarArtifact swigNar = new NarArtifact( swigJar, info );
 
             // download attached nars, in which the executable and the include files sit
             List swigNarArtifacts = new ArrayList();
             swigNarArtifacts.add( swigNar );
             narManager.downloadAttachedNars( swigNarArtifacts, remoteArtifactRepositories, artifactResolver, null );
             narManager.unpackAttachedNars( swigNarArtifacts, archiverManager, null, os, narLayout, unpackDirectory );
 
             swigInclude =
                 narLayout.getIncludeDirectory( unpackDirectory, swigJar.getArtifactId(), swigJar.getVersion() );
             swigJavaInclude = new File( swigInclude, "java" );
             swig =
                 new File( narLayout.getBinDirectory( unpackDirectory, swigJar.getArtifactId(), swigJar.getVersion(),
                                                      NarUtil.getAOL( architecture, os, linker, null ).toString() ),
                           "swig" );
         }
         else
         {
             swig = new File( exec );
             swigInclude = null;
             swigJavaInclude = null;
         }
 
         File targetFile = targetDirectory;
         SourceInclusionScanner scanner =
             new StaleSourceScanner( staleMillis, Collections.singleton( source ), Collections.EMPTY_SET );
         String extension = "." + FileUtils.getExtension( source );
         SuffixMapping mapping = new SuffixMapping( extension, extension + ".flag" );
         scanner.addSourceMapping( mapping );
         try
         {
             Set files = scanner.getIncludedSources( sourceDir, targetFile );
 
             if ( !files.isEmpty() || force )
             {
                 if ( cleanOutputDirectory && ( packageName != null ) && FileUtils.fileExists( javaTargetDirectory ) )
                 {
                     getLog().info( "Cleaning " + javaTargetDirectory );
                     String[] filesToRemove =
                         FileUtils.getFilesFromExtension( javaTargetDirectory, new String[] { "java" } );
                     for ( int i = 0; i < filesToRemove.length; i++ )
                     {
                         File f = new File( filesToRemove[i] );
                         f.delete();
                     }
                 }
 
                 getLog().info( ( force ? "FORCE " : "" ) + "Running SWIG compiler on " + source + " ..." );
                 int error = runCommand( generateCommandLine( swig, swigInclude, swigJavaInclude ) );
                 if ( error != 0 )
                 {
                     throw new MojoFailureException( "SWIG returned error code " + error );
                 }
                 File flagFile = new File( targetDirectory, source + ".flag" );
                 FileUtils.fileDelete( flagFile.getPath() );
                 FileUtils.fileWrite( flagFile.getPath(), "" );
             }
             else
             {
                 getLog().info( "Nothing to swig - all classes are up to date" );
             }
         }
         catch ( InclusionScanException e )
         {
             throw new MojoExecutionException( "IDLJ: Source scanning failed", e );
         }
         catch ( IOException e )
         {
             throw new MojoExecutionException( "SWIG: Creation of timestamp flag file failed", e );
         }
     }
 
     private void configureNarPlugin()
         throws MojoExecutionException, MojoFailureException
     {
         // configure NAR plugin
         Plugin narPlugin =
             (Plugin) project.getBuild().getPluginsAsMap().get( "org.apache.maven.plugins:maven-nar-plugin" );
         if ( narPlugin == null )
         {
             return;
         }
         if ( narPlugin.getConfiguration() != null )
         {
             throw new MojoExecutionException(
                                               "Please configure maven-nar-plugin without <configuration> element, so that the maven-swig-plugin can configure it" );
         }
 
         getLog().info( "Configuring maven-nar-plugin to create jni library" );
 
         Xpp3Dom narConfig = new Xpp3Dom( "configuration" );
         narPlugin.setConfiguration( narConfig );
 
         // set type to jni and generate NarSystem
         Xpp3Dom libraries = new Xpp3Dom( "libraries" );
         narConfig.addChild( libraries );
 
         Xpp3Dom library = new Xpp3Dom( "library" );
         libraries.addChild( library );
 
         Xpp3Dom type = new Xpp3Dom( "type" );
         type.setValue( "jni" );
         library.addChild( type );
 
         Xpp3Dom narSystemPackage = new Xpp3Dom( "narSystemPackage" );
         narSystemPackage.setValue( packageName );
         library.addChild( narSystemPackage );
 
         // additional options for the compiler
         if( compilerOptions != null && compilerOptions.length > 0 )
         {
         	Xpp3Dom comp = null;
         	if ( cpp )
         		comp = new Xpp3Dom( "cpp" );
         	else
         		comp = new Xpp3Dom( "c" );
         	Xpp3Dom options = new Xpp3Dom( "options" );
         	int i = 0;
         	while( i < compilerOptions.length )
         	{
         		Xpp3Dom option = new Xpp3Dom( "option" );
         		option.setValue( compilerOptions[i] );
         		options.addChild( option );
         		i++;
         	}
         	comp.addChild( options );
         	narConfig.addChild( comp );
         }
 
         // include and link with java
         Xpp3Dom java = new Xpp3Dom( "java" );
         narConfig.addChild( java );
 
         Xpp3Dom include = new Xpp3Dom( "include" );
         include.setValue( "true" );
         java.addChild( include );
 
         Xpp3Dom link = new Xpp3Dom( "link" );
         link.setValue( "true" );
         java.addChild( link );
 
         Xpp3Dom javah = new Xpp3Dom( "javah" );
         narConfig.addChild( javah );
 
         // do not run javah
         Xpp3Dom excludes = new Xpp3Dom( "excludes" );
         javah.addChild( excludes );
 
         Xpp3Dom exclude = new Xpp3Dom( "exclude" );
         exclude.setValue( packageName.replace( '.', File.separatorChar ) + File.separatorChar + "*.class" );
         excludes.addChild( exclude );
 
        if( ( sysLibSet != null && (sysLibSet != "") ) ||	
            ( linkerOptions != null && linkerOptions.length > 0 ) )        
        {
            Xpp3Dom linker = new Xpp3Dom( "linker" );
            narConfig.addChild( linker );
 
            // additional options for the linker
           if( linkerOptions.length > 0 )
            {
         	   Xpp3Dom options = new Xpp3Dom( "options" );
         	   int i = 0;
         	   while ( i < linkerOptions.length )
         	   {
         		   Xpp3Dom option = new Xpp3Dom( "option" );
         		   option.setValue( linkerOptions[i] );
         		   options.addChild( option );
         		   i++;
     		   }
            	linker.addChild( options );
            }
            
            if( sysLibSet != null && !sysLibSet.isEmpty() )
            {
 	           Xpp3Dom sysLibSetNode = new Xpp3Dom( "sysLibSet" );
 	           sysLibSetNode.setValue( sysLibSet );
 	           linker.addChild( sysLibSetNode );
            }
         }
         
         if (includePaths != null && !includePaths.isEmpty())
         {
            final String compilerName = cpp ? "cpp" : "c";
            Xpp3Dom compilerConfig = new Xpp3Dom( compilerName );
            narConfig.addChild( compilerConfig );
            
            Xpp3Dom options = new Xpp3Dom( "options" );
            compilerConfig.addChild( options );
 
           for ( Iterator i = includePaths.iterator(); i.hasNext(); )
           {
               Xpp3Dom includePath = new Xpp3Dom( "option" );
               includePath.setValue( "-I" + i.next() );
               options.addChild( includePath );
           }
 
           Xpp3Dom standardIncludePath = new Xpp3Dom( "option" );
           standardIncludePath.setValue( "-I" + "src/main/include" );
           options.addChild( standardIncludePath );
 
           Xpp3Dom rootIncludePath = new Xpp3Dom( "option" );
           rootIncludePath.setValue( "-I" + sourceDirectory );
           options.addChild( rootIncludePath );
         }
     }
 
     private String[] generateCommandLine( File swig, File swigInclude, File swigJavaInclude )
         throws MojoExecutionException, MojoFailureException
     {
 
         List cmdLine = new ArrayList();
 
         cmdLine.add( swig.toString() );
 
         if ( fakeVersion != null )
         {
             cmdLine.add( "-fakeversion" );
             cmdLine.add( fakeVersion );
         }
 
         if ( getLog().isDebugEnabled() )
         {
             cmdLine.add( "-v" );
         }
 
         // FIXME hardcoded
         cmdLine.add( "-java" );
 
         if ( cpp )
         {
             cmdLine.add( "-c++" );
         }
 
         // defines
         if ( defines != null )
         {
             for ( Iterator i = defines.iterator(); i.hasNext(); )
             {
                 cmdLine.add( "-D" + i.next() );
             }
         }
 
         // warnings
         if ( noWarn != null )
         {
             String noWarns[] = noWarn.split( ",| " );
             for ( int i = 0; i < noWarns.length; i++ )
             {
                 cmdLine.add( "-w" + noWarns[i] );
             }
         }
 
         if ( warnAll )
         {
             cmdLine.add( "-Wall" );
         }
 
         if ( warnError )
         {
             cmdLine.add( "-Werror" );
         }
 
         // output file
         String baseName = FileUtils.basename( source );
         cmdLine.add( "-o" );
         File outputFile =
             outFile != null ? new File( targetDirectory, outFile ) : new File( targetDirectory, baseName
                 + ( cpp ? "cxx" : "c" ) );
         cmdLine.add( outputFile.toString() );
 
         // package for java code
         if ( packageName != null )
         {
             cmdLine.add( "-package" );
             cmdLine.add( packageName );
         }
 
         // output dir for java code
         cmdLine.add( "-outdir" );
         cmdLine.add( javaTargetDirectory );
 
         // user added include dirs
         if ( includePaths != null )
         {
             for ( Iterator i = includePaths.iterator(); i.hasNext(); )
             {
                 cmdLine.add( "-I" + i.next() );
             }
         }
         // default include dirs
         cmdLine.add( "-I" + "src/main/include" );
         cmdLine.add( "-I" + sourceDirectory );
 
         // NAR dependency include dirs
         NarLayout narLayout = AbstractNarLayout.getLayout( layout, getLog() );
         List narIncludes = narManager.getNarDependencies( "compile" );
         for ( Iterator i = narIncludes.iterator(); i.hasNext(); )
         {
             Artifact narInclude = (Artifact) i.next();
             File narIncludeDir =
                 narLayout.getIncludeDirectory( unpackDirectory, narInclude.getArtifactId(), narInclude.getVersion() );
             if ( narIncludeDir.isDirectory() )
             {
                 cmdLine.add( "-I" + narIncludeDir );
             }
         }
 
         // system swig include dirs
         if ( swigJavaInclude != null )
         {
             cmdLine.add( "-I" + swigJavaInclude.toString() );
         }
         if ( swigInclude != null )
         {
             cmdLine.add( "-I" + swigInclude.toString() );
         }
 
         // custom include paths
         if (includePaths != null && !includePaths.isEmpty())
         {
           for ( Iterator i = includePaths.iterator(); i.hasNext(); )
           {
              cmdLine.add( "-I" + i.next() );
           }
         }
         
         // swig file
         cmdLine.add( sourceDirectory + source );
 
         StringBuffer b = new StringBuffer();
         for ( Iterator i = cmdLine.iterator(); i.hasNext(); )
         {
             b.append( (String) i.next() );
             if ( i.hasNext() )
             {
                 b.append( ' ' );
             }
         }
         getLog().info( b.toString() );
 
         return (String[]) cmdLine.toArray( new String[cmdLine.size()] );
     }
 
     private int runCommand( String[] cmdLine )
         throws MojoExecutionException
     {
         try
         {
             final int timeout = 5000;
 
             Runtime runtime = Runtime.getRuntime();
             Process process = runtime.exec( cmdLine );
             StreamGobbler errorGobbler = new StreamGobbler( process.getErrorStream(), true );
             StreamGobbler outputGobbler = new StreamGobbler( process.getInputStream(), false );
 
             errorGobbler.start();
             outputGobbler.start();
             process.waitFor();
             errorGobbler.join( timeout );
             outputGobbler.join( timeout );
             return process.exitValue();
         }
         catch ( Exception e )
         {
             throw new MojoExecutionException( "Could not launch " + cmdLine[0], e );
         }
     }
 
     private class StreamGobbler
         extends Thread
     {
         private InputStream is;
 
         private boolean error;
 
         StreamGobbler( InputStream is, boolean error )
         {
             this.is = is;
             this.error = error;
         }
 
         public void run()
         {
             try
             {
                 BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
                 String line = null;
                 while ( ( line = reader.readLine() ) != null )
                 {
                     if ( error )
                     {
                         getLog().error( line );
                     }
                     else
                     {
                         getLog().debug( line );
                     }
                 }
                 reader.close();
             }
             catch ( IOException e )
             {
                 getLog().error( e );
             }
         }
     }
 }
