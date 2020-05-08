 package com.atolcd.alfresco;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.maven.archiver.MavenArchiveConfiguration;
 import org.apache.maven.archiver.MavenArchiver;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.ArtifactUtils;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.Execute;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.plugins.annotations.ResolutionScope;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.shared.filtering.MavenFilteringException;
 import org.apache.maven.shared.filtering.MavenResourcesExecution;
 import org.apache.maven.shared.filtering.MavenResourcesFiltering;
 import org.apache.maven.shared.utils.io.IOUtil;
 import org.codehaus.plexus.archiver.Archiver;
 import org.codehaus.plexus.archiver.jar.JarArchiver;
 import org.codehaus.plexus.archiver.zip.ZipArchiver;
 import org.codehaus.plexus.util.ReaderFactory;
 import org.codehaus.plexus.util.StringUtils;
 
 /**
  * Amp-packaging goal
  *
  */
 @Mojo(name = "amp", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
 @Execute(phase = LifecyclePhase.PACKAGE)
 public class AmpMojo extends AbstractMojo {
   @Component(role = Archiver.class, hint = "zip")
   private ZipArchiver zipArchiver;
 
   @Component(role = Archiver.class, hint = "jar")
   private JarArchiver jarArchiver;
 
   @Component
   private MavenProject project;
 
   @Component
   protected MavenSession session;
 
   @Component(role = MavenResourcesFiltering.class, hint = "default")
   private MavenResourcesFiltering resourcesFiltering;
 
   @Parameter(property = "moduleProperties")
   private Properties moduleProperties;
 
   @Parameter(property = "filemappingProperties")
   private Properties filemappingProperties;
 
   @Parameter(defaultValue = "${project.basedir}/src/main/web")
   private File webDirectory;
 
   @Parameter(defaultValue = "${project.build.directory}/web")
   private File webBuildDirectory;
 
   @Parameter(defaultValue = "${project.basedir}/src/main/webscripts")
   private File webscriptsDirectory;
 
   @Parameter(defaultValue = "${project.basedir}/src/main/alfresco-webscripts")
   private File alfrescoWebscriptsDirectory;
 
   @Parameter(defaultValue = "${project.basedir}/src/main/licenses")
   private File licensesDirectory;
 
   @Parameter
   private File rootDirectory;
 
   @Parameter(defaultValue = "${project.build.directory}/${project.groupId}.${project.artifactId}-${project.version}.jar")
   private File jarFile;
 
   @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
   private File outputDirectory;
 
   @Parameter(defaultValue = "${project.build.directory}", required = true)
   private File targetDirectory;
 
   @Parameter(defaultValue = "${project.build.sourceEncoding}")
   private String encoding;
 
   @Parameter(defaultValue = "false")
   private boolean shareModule;
 
   public void execute() throws MojoExecutionException {
     // Module properties
     getLog().info("Processing module.properties file");
     moduleProperties.put("module.id", project.getGroupId() + "." + project.getArtifactId());
 
     String finalVersion = project.getVersion();
     if (ArtifactUtils.isSnapshot(finalVersion)) {
       getLog().info("Stripping \"-SNAPSHOT\" from version number");
       finalVersion = finalVersion.replace("-" + Artifact.SNAPSHOT_VERSION, "");
     }
     moduleProperties.put("module.version", finalVersion);
 
     File modulePropertiesFile = new File(targetDirectory, "module.properties");
     FileOutputStream fos = null;
     try {
       fos = new FileOutputStream(modulePropertiesFile);
       moduleProperties.store(fos, null);
     } catch (IOException e) {
       throw new MojoExecutionException("Could not process module.properties", e);
     } finally {
       IOUtil.close(fos);
     }
     zipArchiver.addFile(modulePropertiesFile, "module.properties");
 
     // File-mapping properties file (automatically created for Share projects)
     if (shareModule) {
       if (filemappingProperties == null) {
         filemappingProperties = new Properties();
       }
       filemappingProperties.put("/web", "/");
     }
     if (filemappingProperties != null && !filemappingProperties.isEmpty()) {
       if (!filemappingProperties.containsKey("include.default")) {
         filemappingProperties.put("include.default", "true");
       }
 
       File filemappingPropertiesFile = new File(targetDirectory, "file-mapping.properties");
       try {
         fos = new FileOutputStream(filemappingPropertiesFile);
         filemappingProperties.store(fos, null);
       } catch (IOException e) {
         throw new MojoExecutionException("Could not process file-mapping.properties", e);
       } finally {
         IOUtil.close(fos);
       }
       zipArchiver.addFile(filemappingPropertiesFile, "file-mapping.properties");
     }
 
     // Alfresco configuration files
     // Mapped from configured resources to their respective target paths
     getLog().info("Adding configuration files");
     MavenResourcesExecution resourcesExecution;
     File targetConfigDirectory = new File(targetDirectory, "config");
     targetConfigDirectory.mkdir();
     try {
       if (StringUtils.isEmpty(encoding)) {
         getLog().warn(
             "File encoding has not been set, using platform encoding " + ReaderFactory.FILE_ENCODING
                 + ", i.e. build is platform dependent!");
       }
 
       resourcesExecution = new MavenResourcesExecution(project.getResources(), targetConfigDirectory, project,
           encoding, null, Collections.<String> emptyList(), session);
       resourcesFiltering.filterResources(resourcesExecution);
     } catch (MavenFilteringException e) {
       throw new MojoExecutionException("Could not filter resources", e);
     }
     zipArchiver.addDirectory(targetConfigDirectory, "config/");
 
     // Web sources directory
     if (webDirectory.exists()) {
       getLog().info("Adding web sources");
       zipArchiver.addDirectory(webDirectory, "web/");
     }
 
     // Web build directory (minified sources)
     if (webBuildDirectory.exists()) {
       getLog().info("Adding minified web sources");
       zipArchiver.addDirectory(webBuildDirectory, "web/");
     }
 
     // Webscripts
     if (webscriptsDirectory.exists()) {
       getLog().info("Adding webscripts");
       zipArchiver.addDirectory(webscriptsDirectory, "config/alfresco/extension/templates/webscripts/");
     }
 
     // Alfresco webscripts overrides
     if (alfrescoWebscriptsDirectory.exists()) {
       getLog().info("Adding webscripts overrides");
       zipArchiver.addDirectory(alfrescoWebscriptsDirectory, "config/alfresco/templates/webscripts/org/alfresco/");
     }
 
     // Licenses
     if (licensesDirectory.exists()) {
       getLog().info("Adding licenses");
       zipArchiver.addDirectory(licensesDirectory, "licenses/");
     }
 
     // Root
     if (rootDirectory != null && rootDirectory.exists()) {
       getLog().info("Adding root directory files");
      zipArchiver.addDirectory(rootDirectory, "/");
     }
 
     // JAR file
     MavenArchiver archiver = new MavenArchiver();
     archiver.setArchiver(jarArchiver);
     archiver.setOutputFile(jarFile);
     jarArchiver.addDirectory(outputDirectory, new String[] { "**/*.class" }, null);
     MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
     try {
       archiver.createArchive(session, project, archive);
     } catch (Exception e) {
       throw new MojoExecutionException("Could not build the jar file", e);
     }
 
     if (jarFile.exists()) {
       getLog().info("Adding JAR file");
       zipArchiver.addFile(jarFile, "lib/" + jarFile.getName());
     }
 
     // Dependencies (mapped to the AMP file "lib" directory)
     getLog().info("Adding JAR dependencies");
     Set<Artifact> artifacts = project.getArtifacts();
     for (Iterator<Artifact> iter = artifacts.iterator(); iter.hasNext();) {
       Artifact artifact = (Artifact) iter.next();
       ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
       if (!artifact.isOptional() && filter.include(artifact) && "jar".equals(artifact.getType())) {
         zipArchiver.addFile(artifact.getFile(), "lib/" + artifact.getFile().getName());
       }
     }
 
     File ampFile = new File(targetDirectory, project.getBuild().getFinalName() + ".amp");
     zipArchiver.setDestFile(ampFile);
     try {
       zipArchiver.createArchive();
     } catch (IOException e) {
       throw new MojoExecutionException("Could not build the amp file", e);
     }
     project.getArtifact().setFile(ampFile);
   }
 }
