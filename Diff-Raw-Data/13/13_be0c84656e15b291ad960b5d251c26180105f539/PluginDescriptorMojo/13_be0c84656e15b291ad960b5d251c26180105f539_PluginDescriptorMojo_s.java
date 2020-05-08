 /*
  * Sonatype Application Build Lifecycle
  * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  *
  */
 package org.sonatype.maven.plugin.app.descriptor;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.DependencyResolutionRequiredException;
 import org.apache.maven.model.License;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.interpolation.InterpolationException;
 import org.codehaus.plexus.util.IOUtil;
 import org.sonatype.maven.plugin.app.ApplicationInformation;
 import org.sonatype.maven.plugin.app.ClasspathUtils;
 import org.sonatype.plugin.ExtensionPoint;
 import org.sonatype.plugin.Managed;
 import org.sonatype.plugin.metadata.GAVCoordinate;
 import org.sonatype.plugin.metadata.PluginMetadataGenerationRequest;
 import org.sonatype.plugin.metadata.PluginMetadataGenerator;
 import org.sonatype.plugin.metadata.gleaner.GleanerException;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Generates a plugin's <tt>plugin.xml</tt> descriptor file based on the project's pom and class annotations.
  * 
  * @goal generate-metadata
  * @phase process-classes
  * @requiresDependencyResolution test
  */
 public class PluginDescriptorMojo
     extends AbstractMojo
 {
     /**
      * The output location for the generated plugin descriptor. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included via build
      * extension.
      * 
      * @parameter
      */
     private File generatedPluginMetadata;
 
     /**
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject mavenProject;
 
     /**
      * The ID of the target application. For example if this plugin was for the Nexus Repository Manager, the ID would
      * be, 'nexus'. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included via build
      * extension.
      * 
      * @parameter
      */
     private String applicationId;
 
     /**
      * The edition of the target application. Some applications come in multiple flavors, OSS, PRO, Free, light, etc. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included via build
      * extension.
      * 
      * @parameter expression="OSS"
      */
     private String applicationEdition;
 
     /**
      * The minimum product version of the target application. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included via build
      * extension.
      * 
      * @parameter
      */
     private String applicationMinVersion;
 
     /**
      * The maximum product version of the target application. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included via build
      * extension, if it specified at all.
      * 
      * @parameter
      */
     private String applicationMaxVersion;
 
     /**
      * The list of user defined MIME types
      * 
      * @parameter
      */
     private List<String> userMimeTypes;
 
     /**
      * The output location for the generated plugin descriptor. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included via build
      * extension.
      * 
      * @parameter
      */
     private File userMimeTypesFile;
 
     /** @component */
     private PluginMetadataGenerator metadataGenerator;
 
     /**
      * Brought in via build extension, this supplies default values specific to the application being built. <br/>
      * <b>NOTE:</b> There should be <b>AT MOST ONE</b> {@link ApplicationInformation} component present in any given
      * build. If this component is missing, it should be created on-the-fly, and will be empty...which means the plugin
      * parameters given here will be required.
      * 
      * @component
      */
     private ApplicationInformation mapping;
 
     @SuppressWarnings( "unchecked" )
     public void execute()
         throws MojoExecutionException, MojoFailureException
     {
         if ( !this.mavenProject.getPackaging().equals( mapping.getPluginPackaging() ) )
         {
             this.getLog().info( "Project is not of packaging type '" + mapping.getPluginPackaging() + "'." );
             return;
         }
 
         initConfig();
 
         // get the user customization
         Properties userMimeTypes = null;
 
         if ( userMimeTypes != null && !userMimeTypes.isEmpty() )
         {
             FileOutputStream fos = null;
 
             try
             {
                 fos = new FileOutputStream( userMimeTypesFile );
 
                 userMimeTypes.store( fos, "User MIME types" );
             }
             catch ( IOException e )
             {
                 throw new MojoFailureException( "Cannot write the User MIME types file!", e );
             }
             finally
             {
                 IOUtil.close( fos );
             }
         }
 
         PluginMetadataGenerationRequest request = new PluginMetadataGenerationRequest();
         request.setGroupId( this.mavenProject.getGroupId() );
         request.setArtifactId( this.mavenProject.getArtifactId() );
         request.setVersion( this.mavenProject.getVersion() );
         request.setName( this.mavenProject.getName() );
         request.setDescription( this.mavenProject.getDescription() );
         request.setPluginSiteURL( this.mavenProject.getUrl() );
 
         request.setApplicationId( applicationId );
         request.setApplicationEdition( applicationEdition );
         request.setApplicationMinVersion( applicationMinVersion );
         request.setApplicationMaxVersion( applicationMaxVersion );
 
         // licenses
         if ( this.mavenProject.getLicenses() != null )
         {
             for ( License mavenLicenseModel : (List<License>) this.mavenProject.getLicenses() )
             {
                 request.addLicense( mavenLicenseModel.getName(), mavenLicenseModel.getUrl() );
             }
         }
 
         // dependencies
         List<Artifact> artifacts = mavenProject.getTestArtifacts();
         Set<Artifact> classpathArtifacts = new HashSet<Artifact>();
         if ( artifacts != null )
         {
             Set<String> excludedArtifactIds = new HashSet<String>();
 
             artifactLoop: for ( Artifact artifact : artifacts )
             {
                 GAVCoordinate artifactCoordinate =
                     new GAVCoordinate( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact
                         .getClassifier(), artifact.getType() );
 
                 if ( artifact.getType().equals( mapping.getPluginPackaging() ) )
                 {
                    artifactCoordinate.setVersion( artifact.getBaseVersion() );
 
                     if ( !Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) )
                     {
                         throw new MojoFailureException( "Plugin dependency \""
                             + artifact.getDependencyConflictId() + "\" must have the \"provided\" scope!" );
                     }
 
                     excludedArtifactIds.add( artifact.getId() );
 
                     request.addPluginDependency( artifactCoordinate );
                 }
                 else if ( Artifact.SCOPE_PROVIDED.equals( artifact.getScope() )
                     || Artifact.SCOPE_TEST.equals( artifact.getScope() ) )
                 {
                     excludedArtifactIds.add( artifact.getId() );
                 }
                 else if ( ( Artifact.SCOPE_COMPILE.equals( artifact.getScope() ) || Artifact.SCOPE_RUNTIME
                     .equals( artifact.getScope() ) )
                     && ( !mapping.matchesCoreGroupIds( artifact.getGroupId() ) ) )
                 {
                     if ( artifact.getDependencyTrail() != null )
                     {
                         for ( String trailId : (List<String>) artifact.getDependencyTrail() )
                         {
                             if ( excludedArtifactIds.contains( trailId ) )
                             {
                                 getLog()
                                     .debug(
                                             "Dependency artifact: "
                                                 + artifact.getId()
                                                 + " is part of the transitive dependency set for a dependency with 'provided' or 'test' scope: "
                                                 + trailId
                                                 + "\nThis artifact will be excluded from the plugin classpath." );
                                 continue artifactLoop;
                             }
                         }
                     }
 
                     request.addClasspathDependency( artifactCoordinate );
                     classpathArtifacts.add( artifact );
                 }
             }
         }
 
         request.setOutputFile( this.generatedPluginMetadata );
         request.setClassesDirectory( new File( mavenProject.getBuild().getOutputDirectory() ) );
         try
         {
             if ( mavenProject.getCompileClasspathElements() != null )
             {
                 for ( String classpathElement : (List<String>) mavenProject.getCompileClasspathElements() )
                 {
                     request.getClasspath().add( new File( classpathElement ) );
                 }
             }
         }
         catch ( DependencyResolutionRequiredException e )
         {
             throw new MojoFailureException( "Plugin failed to resolve dependencies: " + e.getMessage(), e );
         }
 
         request.getAnnotationClasses().add( ExtensionPoint.class );
         request.getAnnotationClasses().add( Managed.class );
 
         // do the work
         try
         {
             this.metadataGenerator.generatePluginDescriptor( request );
         }
         catch ( GleanerException e )
         {
             throw new MojoFailureException( "Failed to generate plugin xml file: " + e.getMessage(), e );
         }
 
         try
         {
             ClasspathUtils.write( classpathArtifacts, mavenProject );
         }
         catch ( IOException e )
         {
             throw new MojoFailureException( "Failed to generate classpath properties file: " + e.getMessage(), e );
         }
     }
 
     private void initConfig()
         throws MojoFailureException
     {
         if ( userMimeTypesFile == null )
         {
             try
             {
                 userMimeTypesFile = mapping.getUserMimeTypesFile( mavenProject );
             }
             catch ( InterpolationException e )
             {
                 throw new MojoFailureException( "Cannot calculate User MIME types file location from expression: "
                     + mapping.getUserMimeTypesPath(), e );
             }
         }
 
         if ( this.generatedPluginMetadata == null )
         {
             try
             {
                 this.generatedPluginMetadata = mapping.getPluginMetadataFile( this.mavenProject );
             }
             catch ( InterpolationException e )
             {
                 throw new MojoFailureException( "Cannot calculate plugin metadata file location from expression: "
                     + mapping.getPluginMetadataPath(), e );
             }
         }
 
         this.applicationId = applicationId == null ? mapping.getApplicationId() : applicationId;
         this.applicationEdition = applicationEdition == null ? mapping.getApplicationEdition() : applicationEdition;
         this.applicationMinVersion =
             applicationMinVersion == null ? mapping.getApplicationMinVersion() : applicationMinVersion;
         this.applicationMaxVersion =
             applicationMaxVersion == null ? mapping.getApplicationMaxVersion() : applicationMaxVersion;
     }
 }
