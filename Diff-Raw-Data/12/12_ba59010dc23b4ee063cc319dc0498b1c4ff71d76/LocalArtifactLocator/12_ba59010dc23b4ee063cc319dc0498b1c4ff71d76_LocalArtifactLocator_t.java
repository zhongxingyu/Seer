 package com.athaydes.osgimonitor.impl.manage;
 
 import com.athaydes.osgimonitor.api.manage.Artifact;
 import com.athaydes.osgimonitor.api.manage.ArtifactLocator;
 import com.athaydes.osgimonitor.api.manage.VersionedArtifact;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.jar.JarFile;
 
 /**
  * User: Renato
  */
 public class LocalArtifactLocator implements ArtifactLocator {
 
 	private FilesHelper filesHelper = new FilesHelper();
 	private JarInspector jarInspector = new JarInspector();
 
 	public void setFilesHelper( FilesHelper filesHelper ) {
 		this.filesHelper = filesHelper;
 	}
 
 	public void setJarInspector( JarInspector jarInspector ) {
 		this.jarInspector = jarInspector;
 	}
 
 	@Override
 	public Set<? extends VersionedArtifact> findByClassName( String className ) {
 		Set<VersionedArtifact> result = new HashSet<>();
		Path repositoryDir = Paths.get( filesHelper.getMavenRepoHome() );
 		try {
 			List<Path> files = filesHelper.findAllFilesIn( repositoryDir );
 			for ( JarFile jarFile : jarInspector.filterJars( files ) ) {
 				String[] classNames = jarInspector.findAllClassNamesIn( jarFile );
 				if ( contains( classNames, className ) ) {
 					System.out.println( "Found class " + className + " in jar: " + jarFile.getName() );
 					result.add( jarInspector.jar2artifact( jarFile ) );
 				}
 			}
 		} catch ( IOException e ) {
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	private boolean contains( String[] classNames, String className ) {
 		return new HashSet<>( Arrays.asList( classNames ) ).contains( className );
 	}
 
 	@Override
 	public Set<Artifact> findByGroupId( String groupId ) {
 		return null;  //To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public Set<Artifact> findByArtifactId( String artifactId ) {
 		return null;  //To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public Artifact findArtifact( String groupId, String artifactId ) {
 		return null;  //To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public Set<String> getVersionsOf( Artifact artifact ) {
 		return null;  //To change body of implemented methods use File | Settings | File Templates.
 	}
 }
