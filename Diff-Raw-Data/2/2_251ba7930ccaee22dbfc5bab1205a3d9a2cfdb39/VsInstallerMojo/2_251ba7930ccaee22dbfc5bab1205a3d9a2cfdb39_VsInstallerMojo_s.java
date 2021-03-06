 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package npanday.plugin.vsinstaller;
 
 import npanday.PlatformUnsupportedException;
 import npanday.artifact.ArtifactContext;
 import npanday.artifact.NPandayArtifactResolutionException;
 import npanday.artifact.NetDependenciesRepository;
 import npanday.artifact.NetDependencyMatchPolicy;
 import npanday.executable.ExecutionException;
 import npanday.executable.NetExecutable;
 import npanday.model.netdependency.NetDependency;
 import npanday.registry.NPandayRepositoryException;
 import npanday.registry.RepositoryRegistry;
 import npanday.vendor.Vendor;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.handler.ArtifactHandler;
 import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.settings.Settings;
 import org.apache.commons.io.filefilter.*;
 import org.apache.commons.io.FileUtils;
 import org.codehaus.plexus.util.IOUtil;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.swing.*;
 import javax.swing.filechooser.FileSystemView;
 
 /**
  * Installs Visual Studio 2005 addin.
  *
  * @author Shane Isbell
  * @goal install
  * @requiresProject false
  * @requiresDirectInvocation true
  */
 public class VsInstallerMojo
     extends AbstractMojo
 {
     /**
      * @parameter expression ="${installationLocation}"
      */ 
     public File installationLocation;
     /**
      * @parameter expression = "${project}"
      */
     public org.apache.maven.project.MavenProject mavenProject;
 
     /**
      * @parameter
      */
     public List<File> vsAddinDirectories = new ArrayList<File>();
 
     /**
      * The the path to the local maven repository.
      *
      * @parameter expression="${settings.localRepository}"
      */
     private String localRepository;
 
     /**
      * Provides services for obtaining artifact information and dependencies
      *
      * @component
      */
     private ArtifactContext artifactContext;
 
     /**
      * Provides access to configuration information used by NPanday.
      *
      * @component
      */
     private npanday.NPandayRepositoryRegistry npandayRegistry;
 
     /**
      * Provides services to obtain executables.
      *
      * @component
      */
     private npanday.executable.NetExecutableFactory netExecutableFactory;
 
     /** @component role="org.apache.maven.artifact.handler.ArtifactHandler" */
     private List<ArtifactHandler> artifactHandlers;
 
     /** @component */
     private ArtifactHandlerManager artifactHandlerManager;
 
     /**
      * @parameter expression="${settings}"
      */
     private Settings settings;
 
    private FileSystemView filesystemView = new JFileChooser().getFileSystemView();
 
     public void execute()
         throws MojoExecutionException, MojoFailureException
     {
 
         File logs = new File( localRepository, "embedder-logs" );
         if ( !logs.exists() )
         {
             logs.mkdir();
         }
 
         RepositoryRegistry repositoryRegistry;
         try
         {
             repositoryRegistry = npandayRegistry.createRepositoryRegistry();
         }
         catch ( IOException e )
         {
             throw new MojoExecutionException(
                 "NPANDAY-1600-000: Failed to create the repository registry for this plugin", e );
         }
         catch( NPandayRepositoryException e )
         {
             throw new MojoExecutionException(
                 "NPANDAY-1600-007: Failed to create the repository registry for this plugin", e );
         }
 
         NetDependenciesRepository netRepository = (NetDependenciesRepository) repositoryRegistry.find(
             "net-dependencies" );
 
         artifactContext.init( null, mavenProject.getRemoteArtifactRepositories(), new File( localRepository ) );
         Map<String, ArtifactHandler> map = new HashMap<String, ArtifactHandler>();
 
         for ( ArtifactHandler artifactHandler : artifactHandlers )
         {
             //If I add a handler that already exists, the runtime breaks.
             if ( isDotNetHandler( artifactHandler ) )
             {
                 map.put( artifactHandler.getPackaging(), artifactHandler );
             }
         }
         artifactHandlerManager.addHandlers( map );
 
         try
         {
             artifactContext.getArtifactInstaller().resolveAndInstallNetDependenciesForProfile( "VisualStudio2005", null,
                                                                                                null );
         }
         catch ( NPandayArtifactResolutionException e )
         {
             throw new MojoExecutionException( e.getMessage(), e );
         }
         catch( IOException e )
         {
             throw new MojoExecutionException( e.getMessage(), e );
         }
 
         // GAC Installs
         List<NetDependencyMatchPolicy> gacInstallPolicies = new ArrayList<NetDependencyMatchPolicy>();
         gacInstallPolicies.add( new GacMatchPolicy( true ) );
         List<Dependency> gacInstallDependencies = netRepository.getDependenciesFor( gacInstallPolicies );
         for ( Dependency dependency : gacInstallDependencies )
         {
             List<Artifact> artifacts = artifactContext.getArtifactsFor( dependency.getGroupId(),
                                                                         dependency.getArtifactId(),
                                                                         dependency.getVersion(), dependency.getType() );
             try
             {
                 NetExecutable netExecutable = netExecutableFactory.getNetExecutableFor(
                     Vendor.MICROSOFT.getVendorName(), "2.0.50727", "GACUTIL", getGacInstallCommandsFor( artifacts.get(
                         0 ) ), null );
                 netExecutable.execute();
                 getLog().info( "NPANDAY-1600-004: Installed Assembly into GAC: Assembly = " + artifacts.get(
                     0 ).getFile().getAbsolutePath() + ",  Vendor = " + netExecutable.getVendor().getVendorName() );
             }
             catch ( ExecutionException e )
             {
                 throw new MojoExecutionException( "NPANDAY-1600-005: Unable to execute gacutil:", e );
             }
             catch ( PlatformUnsupportedException e )
             {
                 throw new MojoExecutionException( "NPANDAY-1600-006: Platform Unsupported:", e );
             }
         }
 
         collectDefaultVSAddinDirectories();
 
         getInstallationLocation();
 
         for ( File vsAddinsDir : vsAddinDirectories )
         {
             writePlugin( vsAddinsDir );
         }
 
         copyDependenciesToBin();
     }
 
     /**
      * Returns true if the artifact handler can handle the dotnet types, otherwise returns false
      *
      * @param artifactHandler the artifact handler to check
      * @return true if the artifact handler can handle the dotnet types, otherwise returns false
      */
     private boolean isDotNetHandler( ArtifactHandler artifactHandler )
     {
         String extension = artifactHandler.getExtension();
         return extension.equals( "dll" ) || extension.equals( "nar" ) || extension.equals( "exe" ) ||
             extension.equals( "exe.config" );
     }
 
     private void collectDefaultVSAddinDirectories()
     {
         File homeDir = filesystemView.getDefaultDirectory();
 
         String vs2010 = "Visual Studio 2010";
         
         String vs2008 = "Visual Studio 2008";
         String vs2005 = "Visual Studio 2005";
 
         List<File> defaultVSDirs = new ArrayList<File>();
 
         defaultVSDirs.add( new File( homeDir, vs2010 ) );
         
         defaultVSDirs.add( new File( homeDir, vs2008 ) );
         defaultVSDirs.add( new File( homeDir, vs2005 ) );
 
         File enHomeDir = new File( System.getProperty( "user.home" ), "My Documents" );
         if ( !homeDir.getPath().toLowerCase().equals( enHomeDir.getPath().toLowerCase() ) )
         {
             defaultVSDirs.add( new File( enHomeDir, vs2010 ) );
 
             defaultVSDirs.add( new File( enHomeDir, vs2008 ) );
             defaultVSDirs.add( new File( enHomeDir, vs2005 ) );
         }
 
         for ( File dir : defaultVSDirs )
         {
             if ( dir.exists() )
             {
                 File addInPath = new File( dir, "AddIns" );
 
                 if ( !addInPath.exists() )
                 {
                     addInPath.mkdir();
                 }
 
                 vsAddinDirectories.add( addInPath );
             }
         }
     }
 
     private void writePlugin( File addinPath )
         throws MojoExecutionException
     {
         OutputStreamWriter writer = null;
 
         if ( !addinPath.exists() )
         {
             addinPath.mkdirs();
         }
 
         try
         {
             String addin = IOUtil.toString( VsInstallerMojo.class.getResourceAsStream(
                 "/template/NPanday.VisualStudio.AddIn" ) );
             File outputFile = new File( addinPath, "NPanday.VisualStudio.AddIn" );
 
             writer = new OutputStreamWriter( new FileOutputStream( outputFile ), "Unicode" );
 
             writer.write( addin.replaceAll( "\\$\\{installationLocation\\}", installationLocation.getAbsolutePath().replaceAll( "\\\\", "\\\\\\\\" ) ) );
         }
         catch ( IOException e )
         {
             throw new MojoExecutionException( "Unable to write to Visual Studio AddIns directory: " + e.getMessage() );
         }
         finally
         {
             IOUtil.close( writer );
         }
 
     }
 
     private List<String> getGacInstallCommandsFor( Artifact artifact )
     {
         List<String> commands = new ArrayList<String>();
         commands.add( "/nologo" );
         commands.add( "/i" );
         commands.add( artifact.getFile().getAbsolutePath() );
         return commands;
     }
 
     private class GacMatchPolicy
         implements NetDependencyMatchPolicy
     {
 
         private boolean isGacInstall;
 
         public GacMatchPolicy( boolean isGacInstall )
         {
             this.isGacInstall = isGacInstall;
         }
 
         public boolean match( NetDependency netDependency )
         {
             return netDependency.isIsGacInstall() == isGacInstall;
         }
     }
 
     private void copyDependenciesToBin()
          throws MojoExecutionException
     {
         try
         {
             String src = System.getProperty( "user.dir" ) + File.separator + "target";
 
             File srcFolder = new File( src );
 
             IOFileFilter dllSuffixFilter = FileFilterUtils.suffixFileFilter( ".dll" );
             IOFileFilter dllFiles = FileFilterUtils.andFileFilter( FileFileFilter.FILE, dllSuffixFilter );
 
             FileUtils.copyDirectory(srcFolder, installationLocation, dllFiles, true);
         }
 
         catch ( IOException e )
         {
             throw new MojoExecutionException( e.getMessage(), e );
         }
     }
 
     private void getInstallationLocation()
     {
         if ( installationLocation == null )
         {
             String programFilesPath = System.getenv( "PROGRAMFILES" );
 
             if ( programFilesPath == null || programFilesPath.length() == 0 )
             {
                 programFilesPath = System.getProperty( "user.dir" );
             }
 
             installationLocation = new File ( programFilesPath, "NPanday/bin" );
         }
         else
         {
             installationLocation = new File ( installationLocation, "bin" );
         }
 
         if ( !installationLocation.exists() )
         {
             installationLocation.mkdirs();
         }
     }
 
 }
