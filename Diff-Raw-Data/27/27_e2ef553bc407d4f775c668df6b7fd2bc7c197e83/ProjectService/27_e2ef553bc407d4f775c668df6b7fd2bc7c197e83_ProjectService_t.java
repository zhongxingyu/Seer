 /**
  * Copyright (c) 2010-2011 Martin M Reed
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.hardisonbrewing.maven.core;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.model.Resource;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectBuilder;
 import org.apache.maven.project.ProjectBuildingException;
 import org.hardisonbrewing.maven.core.model.ProjectConfiguration;
 import org.hardisonbrewing.maven.core.model.Source;
 
 public class ProjectService {
 
     private static ProjectConfiguration projectConfiguration;
 
     protected ProjectService() {
 
         // do nothing
     }
 
     public static void setSourceDirectory( String filePath ) {
 
         getProject().getBuild().setSourceDirectory( filePath );
     }
 
     public static void addSourceDirectory( Source source ) {
 
         if ( projectConfiguration == null ) {
             JoJoMojo.getMojo().getLog().error( "ProjectConfiguration is null" );
             throw new IllegalStateException();
         }
 
         projectConfiguration.addSourceDirectory( source );
     }
 
     /**
      * 
      * @return
      */
     public static final String generateSnapshotVersion() {
 
         String version = getProject().getVersion();
         DateFormat dateFormat = new SimpleDateFormat( "yyyyMMddHHmmssZ" );
         String snapshot = dateFormat.format( new Date() );
         if ( version.contains( "SNAPSHOT" ) ) {
             return version.replace( "SNAPSHOT", snapshot );
         }
         return version + "-" + snapshot;
     }
 
     /**
      * 
      * @return
      */
     public static final String getProperty( String key ) {
 
         return getProperties().getProperty( key );
     }
 
     /**
      * 
      * @return
      */
     public static final Properties getProperties() {
 
         return getProject().getProperties();
     }
 
     /**
      * 
      * @return
      */
    public static final String getBuildDirectoryPath() {
 
        return getProject().getBuild().getDirectory();
     }
 
     /**
      * 
      * @return
      */
    public static final File getBuildDirectory() {
 
        return new File( getBuildDirectoryPath() );
     }
 
     /**
      * 
      * @return
      */
     public static final File getBaseDir() {
 
         return getProject().getBasedir();
     }
 
     /**
      * 
      * @return
      */
     public static final String getBaseDirPath() {
 
         return getBaseDir().getPath();
     }
 
     /**
      * 
      * @return
      */
     public static final MavenProject getProject() {
 
         return JoJoMojo.getMojo().getProject();
     }
 
     /**
      * Create the {@link MavenProject} for the specified {@link Artifact}.
      * @param artifact The {@link Artifact} to create the {@link MavenProject} from.
      * @return
      * @throws ProjectBuildingException
      */
     public static final MavenProject getProject( Artifact artifact ) throws ProjectBuildingException {
 
         MavenProjectBuilder mavenProjectBuilder = getProjectBuilder();
         return mavenProjectBuilder.buildFromRepository( artifact, DependencyService.getRemoteRepositories(), DependencyService.getLocalRepository() );
     }
 
     /**
      * 
      * @return
      */
     public static final MavenProjectBuilder getProjectBuilder() {
 
         return JoJoMojo.getMojo().getProjectBuilder();
     }
 
     public static final void writeOriginalModel( File dest ) throws IOException {
 
         FileWriter fileWriter = new FileWriter( dest );
 
         try {
             getProject().writeModel( fileWriter );
         }
         finally {
             fileWriter.close();
         }
     }
 
     public static final String getResolvedVersion() {
 
         String version = getProject().getVersion();
         if ( version.contains( "-SNAPSHOT" ) ) {
             DateFormat dateFormat = new SimpleDateFormat( "yyyyMMddHHmmss" );
             version = version.replace( "-SNAPSHOT", "." + dateFormat.format( new Date() ) );
         }
 
         return version;
     }
 
     public static final Source[] getSourceDirectoryPaths() {
 
         List<Source> sources = new ArrayList<Source>();
 
         String defaultSourceDirectory = getProject().getBuild().getSourceDirectory();
         Source defaultSource = null;
 
         ProjectConfiguration projectConfiguration = getProjectConfiguration();
         if ( projectConfiguration != null ) {
 
             Source[] additionalSources = projectConfiguration.getAdditionalSourceDirectories();
             if ( additionalSources != null ) {
 
                 for (Source additionalSource : additionalSources) {
 
                     if ( defaultSourceDirectory.equals( additionalSource.directory ) ) {
                         defaultSource = additionalSource;
                     }
                     else if ( additionalSource.directory == null ) {
                         defaultSource = additionalSource;
                         defaultSource.directory = defaultSourceDirectory;
                     }
 
                     sources.add( additionalSource );
                 }
             }
         }
 
         if ( defaultSource == null ) {
             defaultSource = new Source();
             defaultSource.directory = defaultSourceDirectory;
             sources.add( defaultSource );
         }
 
         Source[] _filePaths = new Source[sources.size()];
         sources.toArray( _filePaths );
         return _filePaths;
     }
 
     public static final String[] getSourceFilePaths() {
 
         List<String> sourceFilePaths = new ArrayList<String>();
 
         for (Source source : getSourceDirectoryPaths()) {
 
             String directory = source.directory;
             String[] includes = source.includes;
             String[] excludes = source.excludes;
 
             String[] _sourceFilePaths = org.hardisonbrewing.maven.core.ProjectService.getSourceFilePaths( directory, includes, excludes );
             for (String sourceFilePath : _sourceFilePaths) {
                 sourceFilePaths.add( sourceFilePath );
             }
         }
 
         String[] _sourceFilePaths = new String[sourceFilePaths.size()];
         sourceFilePaths.toArray( _sourceFilePaths );
         return _sourceFilePaths;
     }
 
     public static String[] getSourceFilePaths( String directory, String[] includes, String[] excludes ) {
 
         ArrayList<String> filePaths = new ArrayList<String>();
 
         File dirFle = new File( directory );
 
         for (String filePath : FileUtils.listFilePathsRecursive( dirFle, includes, excludes )) {
             if ( !filePaths.contains( filePath ) ) {
                 filePaths.add( filePath );
             }
         }
 
         String[] _filePaths = new String[filePaths.size()];
         filePaths.toArray( _filePaths );
         return _filePaths;
     }
 
     @SuppressWarnings( "unchecked" )
     public static final String[] getResourceFilePaths() {
 
         List<String> resourceFilePaths = new LinkedList<String>();
         for (Resource resource : (List<Resource>) getProject().getResources()) {
             File resourceDirectory = new File( resource.getDirectory() );
             String[] filePaths = FileUtils.listFilePathsRecursive( resourceDirectory );
             for (String filePath : filePaths) {
                 resourceFilePaths.add( filePath );
             }
         }
         return resourceFilePaths.toArray( new String[resourceFilePaths.size()] );
     }
 
     public static ProjectConfiguration getProjectConfiguration() {
 
         return projectConfiguration;
     }
 
     public static void setProjectConfiguration( ProjectConfiguration projectConfiguration ) {
 
         ProjectService.projectConfiguration = projectConfiguration;
     }
 }
