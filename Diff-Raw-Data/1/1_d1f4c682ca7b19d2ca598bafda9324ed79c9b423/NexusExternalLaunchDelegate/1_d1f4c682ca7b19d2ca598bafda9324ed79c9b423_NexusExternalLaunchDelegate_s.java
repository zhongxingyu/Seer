 package com.ifedorenko.m2e.nexusdev.internal.launch;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.codehaus.plexus.util.ReaderFactory;
 import org.codehaus.plexus.util.StringUtils;
 import org.codehaus.plexus.util.WriterFactory;
 import org.codehaus.plexus.util.xml.XmlStreamWriter;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
 import org.codehaus.plexus.util.xml.Xpp3DomWriter;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
 import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
 import org.eclipse.jdt.launching.JavaLaunchDelegate;
 import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;
 import org.eclipse.m2e.core.MavenPlugin;
 import org.eclipse.m2e.core.embedder.ArtifactKey;
 import org.eclipse.m2e.core.project.IMavenProjectFacade;
 import org.eclipse.m2e.core.project.IMavenProjectRegistry;
 
 import com.ifedorenko.m2e.binaryproject.BinaryProjectPlugin;
 import com.ifedorenko.m2e.nexusdev.internal.NexusdevActivator;
 import com.ifedorenko.m2e.sourcelookup.internal.SourceLookupMavenLaunchParticipant;
 
 @SuppressWarnings( "restriction" )
 public class NexusExternalLaunchDelegate
     extends JavaLaunchDelegate
 {
     public static final String LAUNCHTYPE_ID = "com.ifedorenko.m2e.nexusdev.externalLaunchType";
 
     public static final String ATTR_INSTALLATION_LOCATION = "nexusdev.installationLocation";
 
     public static final String ATTR_WORKDIR_LOCATION = "nexusdev.workdirLocation";
 
     public static final String ATTR_APPLICATION_PORT = "nexusdev.applicationPort";
 
     public static final String ATTR_SELECTED_PROJECTS = "nexusdev.selectedProjects";
 
     private static final SourceLookupMavenLaunchParticipant sourcelookup = new SourceLookupMavenLaunchParticipant();
 
     private ILaunch launch;
 
     private IProgressMonitor monitor;
 
     private String mode;
 
     private final IMavenProjectRegistry projectRegistry;
 
     private IWorkspaceRoot root;
 
     public NexusExternalLaunchDelegate()
     {
         this.projectRegistry = MavenPlugin.getMavenProjectRegistry();
         this.root = ResourcesPlugin.getWorkspace().getRoot();
     }
 
     @Override
     public void launch( final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
                         final IProgressMonitor monitor )
         throws CoreException
     {
         this.mode = mode;
         this.launch = launch;
         this.monitor = monitor;
         try
         {
             final List<ISourceLookupParticipant> participants = new ArrayList<ISourceLookupParticipant>();
             if ( ILaunchManager.DEBUG_MODE.equals( mode ) )
             {
                 participants.addAll( sourcelookup.getSourceLookupParticipants( configuration, launch, monitor ) );
             }
             participants.add( new JavaSourceLookupParticipant() );
             JavaSourceLookupDirector sourceLocator = new JavaSourceLookupDirector()
             {
                 @Override
                 public void initializeParticipants()
                 {
                     addParticipants( participants.toArray( new ISourceLookupParticipant[participants.size()] ) );
                 }
             };
             sourceLocator.initializeParticipants();
 
             launch.setSourceLocator( sourceLocator );
 
             writePluginRepositoryXml( getPluginRepositoryXml( configuration ),
                                       SelectedProjects.fromLaunchConfig( configuration ) );
 
             super.launch( configuration, mode, launch, monitor );
         }
         finally
         {
             this.mode = null;
             this.launch = null;
             this.monitor = null;
         }
     }
 
     private File getPluginRepositoryXml( final ILaunchConfiguration configuration )
         throws CoreException
     {
         File parent = new File( NexusdevActivator.getStateLocation().toFile(), configuration.getName() );
         parent.mkdirs();
         return new File( parent, "plugin-repository.xml" );
     }
 
     private File getNexusWorkingDirectory( ILaunchConfiguration configuration )
         throws CoreException
     {
         // String defaultWorkdirLocation = ResourcesPlugin.getWorkspace().get;
         String location = configuration.getAttribute( ATTR_WORKDIR_LOCATION, (String) null );
 
         if ( location != null )
         {
             return new File( location ).getAbsoluteFile();
         }
 
         return new File( NexusdevActivator.getStateLocation().toFile(), configuration.getName() );
     }
 
     @Override
     public File verifyWorkingDirectory( ILaunchConfiguration configuration )
         throws CoreException
     {
         return getNexusInstallationDirectory( configuration );
     }
 
     private File getNexusInstallationDirectory( ILaunchConfiguration configuration )
         throws CoreException
     {
         String location = configuration.getAttribute( ATTR_INSTALLATION_LOCATION, (String) null );
 
         if ( location == null || "".equals( location.trim() ) )
         {
             throw new CoreException( new Status( IStatus.ERROR, NexusdevActivator.BUNDLE_ID,
                                                  "Installation location is null" ) );
         }
 
         File directory = new File( location );
 
         if ( !directory.isDirectory() )
         {
             throw new CoreException( new Status( IStatus.ERROR, NexusdevActivator.BUNDLE_ID,
                                                  "Installation location is not a directory" ) );
         }
 
         return directory;
     }
 
     @Override
     public String verifyMainTypeName( ILaunchConfiguration configuration )
         throws CoreException
     {
         return "org.sonatype.nexus.bootstrap.Launcher";
     }
 
     @Override
     public String[] getClasspath( ILaunchConfiguration configuration )
         throws CoreException
     {
 
         DirectoryScanner ds = new DirectoryScanner();
         ds.setBasedir( getNexusInstallationDirectory( configuration ) );
         ds.setIncludes( new String[] { "lib/*.jar", "conf" } );
         ds.scan();
 
         List<String> cp = new ArrayList<String>();
 
         for ( String path : ds.getIncludedFiles() )
         {
             cp.add( path );
         }
 
         for ( String path : ds.getIncludedDirectories() )
         {
             cp.add( path );
         }
 
         return cp.toArray( new String[cp.size()] );
     }
 
     @Override
     public String getProgramArguments( ILaunchConfiguration configuration )
         throws CoreException
     {
         return "./conf/jetty.xml";
     }
 
     @Override
     public String getVMArguments( ILaunchConfiguration configuration )
         throws CoreException
     {
         StringBuilder sb = new StringBuilder();
         append( sb, super.getVMArguments( configuration ) );
         if ( ILaunchManager.DEBUG_MODE.equals( mode ) )
         {
             append( sb, sourcelookup.getVMArguments( configuration, launch, monitor ) );
         }
         sb.append( " -Dnexus.nexus-work=" ).append( quote( getNexusWorkingDirectory( configuration ) ) );
         sb.append( " -Dnexus.xml-plugin-repository=" ).append( quote( getPluginRepositoryXml( configuration ) ) );
         sb.append( " -Djetty.application-port=" ).append( configuration.getAttribute( ATTR_APPLICATION_PORT, "8081" ) );
         return sb.toString();
     }
 
     private String quote( File file )
     {
         return StringUtils.quoteAndEscape( file.getAbsolutePath(), '"' );
     }
 
     private void append( StringBuilder sb, String str )
     {
         if ( str != null && !"".equals( str.trim() ) )
         {
             sb.append( ' ' ).append( str );
         }
     }
 
     private void writePluginRepositoryXml( File pluginRepositoryXml, SelectedProjects selectedProjects )
         throws CoreException
     {
         Xpp3Dom repositoryDom = new Xpp3Dom( "plugin-repository" );
 
         Xpp3Dom artifactsDom = new Xpp3Dom( "artifacts" );
         repositoryDom.addChild( artifactsDom );
 
         Set<ArtifactKey> processed = new LinkedHashSet<ArtifactKey>();
 
         for ( IMavenProjectFacade project : projectRegistry.getProjects() )
         {
             IFolder output = root.getFolder( project.getOutputLocation() );
             String packaging = project.getPackaging();
             if ( "nexus-plugin".equals( packaging ) && output.isAccessible() )
             {
                 if ( !selectedProjects.isSelected( project ) )
                 {
                     continue;
                 }
 
                 ArtifactKey artifactKey = project.getArtifactKey();
                 if ( processed.add( artifactKey ) )
                 {
                     addArtifact( artifactsDom, artifactKey, packaging, output.getLocation().toOSString() );
 
                     IFile nexusPluginXml =
                         project.getProject().getWorkspace().getRoot().getFile( project.getOutputLocation().append( "META-INF/nexus/plugin.xml" ) );
 
                     MavenProject mavenProject = project.getMavenProject( monitor );
                     Map<ArtifactKey, Artifact> dependencies = toDependencyMap( mavenProject.getArtifacts() );
 
                     try
                     {
                         Xpp3Dom dom =
                             Xpp3DomBuilder.build( ReaderFactory.newPlatformReader( nexusPluginXml.getContents() ) );
                         Xpp3Dom cp = dom.getChild( "classpathDependencies" );
                         if ( cp != null )
                         {
                             for ( Xpp3Dom cpe : cp.getChildren( "classpathDependency" ) )
                             {
                                 ArtifactKey dependencyKey = toDependencyKey( cpe );
                                 Artifact dependency = dependencies.get( dependencyKey );
 
                                 // dependency == null means workspace project was not fully resolved
                                 if ( dependency != null )
                                 {
                                     addArtifact( artifactsDom, dependencyKey,
                                                  dependency.getArtifactHandler().getExtension(),
                                                  dependency.getFile().getAbsolutePath() );
                                 }
                             }
                         }
                     }
                     catch ( IOException e )
                     {
                     }
                     catch ( XmlPullParserException e )
                     {
                     }
                 }
             }
         }
 
         try
         {
             pluginRepositoryXml.getParentFile().mkdirs();
             XmlStreamWriter writer = WriterFactory.newXmlWriter( pluginRepositoryXml );
             try
             {
                 Xpp3DomWriter.write( writer, repositoryDom );
             }
             finally
             {
                 writer.close();
             }
         }
         catch ( IOException e )
         {
             throw new CoreException( new Status( IStatus.ERROR, NexusdevActivator.BUNDLE_ID,
                                                  "Could not write nexus plugin-repository.xlm file", e ) );
         }
     }
 
     private Map<ArtifactKey, Artifact> toDependencyMap( Set<Artifact> artifacts )
     {
         Map<ArtifactKey, Artifact> result = new LinkedHashMap<ArtifactKey, Artifact>();
         for ( Artifact a : artifacts )
         {
             ArtifactKey k = new ArtifactKey( a.getGroupId(), a.getArtifactId(), a.getVersion(), a.getClassifier() );
             result.put( k, a );
         }
         return result;
     }
 
     private ArtifactKey toDependencyKey( Xpp3Dom cpe )
     {
         String groupId = cpe.getChild( "groupId" ).getValue();
         String artifactId = cpe.getChild( "artifactId" ).getValue();
         String version = cpe.getChild( "version" ).getValue();
         String classifier = getChildText( cpe, "classifier" );
 
         return new ArtifactKey( groupId, artifactId, version, classifier );
     }
 
     private void addArtifact( Xpp3Dom artifactsDom, ArtifactKey artifactKey, String packaging, String location )
         throws CoreException
     {
         String _location = null;
         if ( location.endsWith( "/pom.xml" ) )
         {
             // TODO find a better way to identify and resolve workspace binary projects
 
             IMavenProjectFacade facade =
                 projectRegistry.getMavenProject( artifactKey.getGroupId(), artifactKey.getArtifactId(),
                                                  artifactKey.getVersion() );
             if ( facade != null )
             {
                 _location = facade.getProject().getPersistentProperty( BinaryProjectPlugin.QNAME_JAR );
             }
         }
 
         if ( _location == null )
         {
             _location = location;
         }
 
         Xpp3Dom artifactDom = new Xpp3Dom( "artifact" );
         addChild( artifactDom, "location", _location );
         addChild( artifactDom, "groupId", artifactKey.getGroupId() );
         addChild( artifactDom, "artifactId", artifactKey.getArtifactId() );
         addChild( artifactDom, "version", artifactKey.getVersion() );
        addChild( artifactDom, "groupId", artifactKey.getGroupId() );
         addChild( artifactDom, "type", packaging );
 
         artifactsDom.addChild( artifactDom );
     }
 
     private static void addChild( Xpp3Dom dom, String name, String value )
     {
         Xpp3Dom child = new Xpp3Dom( name );
         child.setValue( value );
         dom.addChild( child );
     }
 
     private String getChildText( Xpp3Dom dom, String childName )
     {
         Xpp3Dom child = dom.getChild( childName );
         return child != null ? child.getValue() : null;
     }
 
 }
