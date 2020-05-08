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
 package org.apache.tuscany.maven.bundle.plugin;
 
 import static org.apache.tuscany.maven.bundle.plugin.BundleUtil.write;
 import static org.osgi.framework.Constants.BUNDLE_CLASSPATH;
 import static org.osgi.framework.Constants.BUNDLE_VERSION;
 import static org.osgi.framework.Constants.RESOLUTION_DIRECTIVE;
 import static org.osgi.framework.Constants.RESOLUTION_OPTIONAL;
 import static org.osgi.framework.Constants.VISIBILITY_DIRECTIVE;
 import static org.osgi.framework.Constants.VISIBILITY_REEXPORT;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.jar.Attributes;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.ArtifactUtils;
 import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
 import org.apache.maven.artifact.resolver.ArtifactCollector;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
 import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.artifact.versioning.ArtifactVersion;
 import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectBuilder;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.artifact.InvalidDependencyVersionException;
 import org.apache.maven.shared.dependency.tree.DependencyNode;
 import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
 import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
 import org.apache.maven.shared.dependency.tree.traversal.CollectingDependencyNodeVisitor;
 import org.osgi.framework.Constants;
 
 /**
  * A maven plugin that generates a modules directory containing OSGi bundles for all the project's module dependencies.
  *
  * @version $Rev$ $Date$
  * @goal generate-modules
  * @phase generate-resources
  * @requiresDependencyResolution test
  * @description Generate a modules directory containing OSGi bundles for all the project's module dependencies.
  */
 public class ModuleBundlesBuildMojo extends AbstractMojo {
 
     private static final String GATEWAY_BUNDLE = "org.apache.tuscany.sca.gateway";
 
     /**
      * The project to create a distribution for.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * Project builder -- builds a model from a pom.xml
      *
      * @component role="org.apache.maven.project.MavenProjectBuilder"
      * @required
      * @readonly
      */
     private MavenProjectBuilder mavenProjectBuilder;
     /**
      * Used to look up Artifacts in the remote repository.
      *
      * @component
      */
     private org.apache.maven.artifact.factory.ArtifactFactory factory;
 
     /**
      * Used to look up Artifacts in the remote repository.
      *
      * @component
      */
     private org.apache.maven.artifact.resolver.ArtifactResolver resolver;
 
     /**
      * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
      *            hint="maven"
      * @required
      * @readonly
      */
     private ArtifactMetadataSource artifactMetadataSource;
 
     /**
      * The artifact collector to use.
      * 
      * @component
      * @required
      * @readonly
      */
     private ArtifactCollector artifactCollector;
     
     /**
      * The dependency tree builder to use.
      * 
      * @component
      * @required
      * @readonly
      */
     private DependencyTreeBuilder dependencyTreeBuilder;
 
 
     /**
      * Location of the local repository.
      *
      * @parameter expression="${localRepository}"
      * @readonly
      * @required
      */
     private org.apache.maven.artifact.repository.ArtifactRepository local;
 
     /**
      * List of Remote Repositories used by the resolver
      *
      * @parameter expression="${project.remoteArtifactRepositories}"
      * @readonly
      * @required
      */
     private java.util.List remoteRepos;
 
     /**
      * Target directory.
      *
      *  @parameter expression="${project.build.directory}/modules"
      */
     private File targetDirectory;
 
     /**
      * @parameter default-value="features"
      */
     private String featuresName = "features";
 
     /**
      * Directories containing artifacts to exclude.
      *
      * @parameter
      */
     private File[] excludeDirectories;
 
     /**
      * Directories containing groupids to exclude.
      *
      * @parameter
      */
     private String[] excludeGroupIds;
 
     /**
      * Directories containing groupids to include.
      *
      * @parameter
      */
     private String[] includeGroupIds;
 
     /**
      * Set to true to generate configurations under a folder named as the distro
      *
      * @parameter default-value="true"
      */
     private boolean useDistributionName = true;
 
     /**
      * Set to true to generate a PDE target platform configuration.
      *
      *  @parameter default-value="true"
      */
     private boolean generateTargetPlatform = true;
 
     /**
      * Expand non-tuscany bundles as a folder
      * @parameter default-value="false"
      */
     private boolean expandThirdPartyBundle = false;
 
     /**
      * OSGi execution environment
      */
     private String executionEnvironment;
 
     /**
      * A list of Eclipse features to be added to the target definition
      * @parameter
      */
     private String[] eclipseFeatures;
 
     /**
      * If we use the running eclipse as the default location for the target
      * @parameter default-value="true"
      */
     private boolean useDefaultLocation = true;
 
     /**
      * Set to true to generate a gateway bundle tuscany-gateway-<version>.jar that handles split packages and META-INF/services.
      *
      *  @parameter default-value="true"
      */
     private boolean generateGatewayBundle;
 
     /**
      * @parameter default-value="false"
      */
     private boolean gatewayReexport;
 
     /**
      * Set to true to generate a plugin.xml.
      *
      *  @parameter default-value="false"
      */
     private boolean generatePlugin;
 
     /**
      * Generate a configuration/config.ini for equinox
      * @parameter default-value="true"
      */
     private boolean generateConfig = true;
 
     /**
      * Generate an aggregated OSGi bundle for each feature
      * @parameter default-value="false"
      */
     private boolean generateAggregatedBundle = false;
     
     /**
      * @parameter default-value="true"
      */
     private boolean generateBundleStart = true;
     
     /**
      * @parameter default-value="true"
      */
     private boolean includeConflictingDepedencies = true;
 
     /**
      * Generete manifest.jar
      * @parameter default-value="true"
      */
     private boolean generateManifestJar = true;
 
     /**
      * @parameter default-value="tuscany-sca-manifest.jar"
      */
     private String manifestJarName = "tuscany-sca-manifest.jar";
 
     /**
      * @parameter default-value="tuscany-sca-equinox-manifest.jar"
      */
     private String equinoxManifestJarName = "tuscany-sca-equinox-manifest.jar";
     
     /**
      * @parameter default-value="jar,bundle"
      */
     private String artifactTypes;
 
     /**
      * @parameter default-value="true"
      */
     private boolean generateAntScript = true;
 
     /**
      * @parameter
      */
     private ArtifactAggregation[] artifactAggregations;
 
     /**
      * @parameter
      */
     private ArtifactManifest[] artifactManifests;
 
     /**
      * Inserts a generic Eclipse-BuddyPolicy header into generated artifacts manifests
      * @parameter
      */
     private String eclipseBuddyPolicy = null;
 
     private static final String XML_PI = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
     private static final String ASL_HEADER =
         "<!--" + "\n * Licensed to the Apache Software Foundation (ASF) under one"
             + "\n * or more contributor license agreements.  See the NOTICE file"
             + "\n * distributed with this work for additional information"
             + "\n * regarding copyright ownership.  The ASF licenses this file"
             + "\n * to you under the Apache License, Version 2.0 (the"
             + "\n * \"License\"); you may not use this file except in compliance"
             + "\n * with the License.  You may obtain a copy of the License at"
             + "\n * "
             + "\n *   http://www.apache.org/licenses/LICENSE-2.0"
             + "\n * "
             + "\n * Unless required by applicable law or agreed to in writing,"
             + "\n * software distributed under the License is distributed on an"
             + "\n * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY"
             + "\n * KIND, either express or implied.  See the License for the"
             + "\n * specific language governing permissions and limitations"
             + "\n * under the License."
             + "\n-->";
 
     /**
      * Group the artifacts by distribution poms
      */
     private class ProjectSet {
         // Distribution projects
         private Map<String, MavenProject> projects;
         // Key: the pom artifact id
         // Value: the names for the artifacts
         private Map<String, Set<String>> nameMap = new HashMap<String, Set<String>>();
 
         private Map<String, String> artifactToNameMap = new HashMap<String, String>();
 
         public ProjectSet(List<MavenProject> projects) {
             super();
             this.projects = new HashMap<String, MavenProject>();
             for (MavenProject p : projects) {
                 this.projects.put(p.getArtifactId(), p);
             }
         }
 
         private MavenProject getProject(String artifactId) {
             return projects.get(artifactId);
         }
 
         private void add(Artifact artifact, String name) {
             String key = ArtifactUtils.versionlessKey(artifact);
             for (MavenProject p : projects.values()) {
                 Artifact a = (Artifact)p.getArtifactMap().get(key);
                 if (a != null) {
                     Set<String> names = nameMap.get(p.getArtifactId());
                     if (names == null) {
                         names = new HashSet<String>();
                         nameMap.put(p.getArtifactId(), names);
                     }
                     names.add(name);
                 }
             }
             artifactToNameMap.put(key, name);
         }
     }
 
     private Manifest findManifest(Artifact artifact) throws IOException {
         if (artifactManifests == null) {
             return null;
         }
         for (ArtifactManifest m : artifactManifests) {
             if (m.matches(artifact)) {
                 File mf = m.getManifestFile();
                 if (mf != null) {
                     FileInputStream is = new FileInputStream(mf);
                     Manifest manifest = new Manifest(is);
                     is.close();
                     getLog().info("MANIFEST.MF found for " + artifact + " (" + mf + ")");
                     return manifest;
                 } else {
                     getLog().info("Overriding the manifest for " + artifact);
                     Manifest manifest = BundleUtil.getManifest(artifact.getFile());
                     Set<File> jarFiles = new HashSet<File>();
                     jarFiles.add(artifact.getFile());
                     String symbolicName = BundleUtil.getBundleSymbolicName(manifest);
                     if (symbolicName == null) {
                         // Not a bundle
                         continue;
                     }
                     String version = manifest.getMainAttributes().getValue(BUNDLE_VERSION);
                     manifest =
                         BundleUtil.libraryManifest(jarFiles,
                                                    symbolicName,
                                                    symbolicName,
                                                    version,
                                                    null,
                                                    this.eclipseBuddyPolicy,
                                                    this.executionEnvironment);
                     // Remove it as it will be added later on
                     manifest.getMainAttributes().remove(new Attributes.Name(BUNDLE_CLASSPATH));
                     return manifest;
                 }
             }
         }
         return null;
     }
     
     /**
      * Gets the artifact filter to use when resolving the dependency tree.
      * 
      * @return the artifact filter
      */
     private ArtifactFilter createResolvingArtifactFilter(String scope) {
         ArtifactFilter filter;
 
         // filter scope
         if (scope != null) {
             getLog().debug("+ Resolving dependency tree for scope '" + scope + "'");
 
             filter = new ScopeArtifactFilter(scope);
         } else {
             filter = null;
         }
 
         return filter;
     }
 
     public void execute() throws MojoExecutionException {
         Log log = getLog();
         
         Set<Artifact> artifacts = null;
         if (includeConflictingDepedencies) {
             try {
                 artifacts = getDependencyArtifacts(project);
             } catch (Exception e) {
                 throw new MojoExecutionException(e.getMessage(), e);
             }
         } else {
             artifacts = project.getArtifacts();
         }
 
         try {
 
             // Create the target directory
             File root;
             if (targetDirectory == null) {
                 root = new File(project.getBuild().getDirectory(), "plugins/");
             } else {
                 root = targetDirectory;
             }
             root.mkdirs();
 
             // Build sets of exclude directories and included/excluded/groupids
             Set<String> excludedFileNames = new HashSet<String>();
             if (excludeDirectories != null) {
                 for (File f : excludeDirectories) {
                     if (f.isDirectory()) {
                         for (String n : f.list()) {
                             excludedFileNames.add(n);
                         }
                     }
                 }
             }
             Set<String> includedGroupIds = new HashSet<String>();
             if (includeGroupIds != null) {
                 for (String g : includeGroupIds) {
                     includedGroupIds.add(g);
                 }
             }
             Set<String> excludedGroupIds = new HashSet<String>();
             if (excludeGroupIds != null) {
                 for (String g : excludeGroupIds) {
                     excludedGroupIds.add(g);
                 }
             }
 
             // Find all the distribution poms
             List<MavenProject> poms = new ArrayList<MavenProject>();
             poms.add(project);
             if (useDistributionName) {
                 for (Object o : project.getArtifacts()) {
                     Artifact artifact = (Artifact)o;
                     if ("pom".equals(artifact.getType()) && artifact.getGroupId().equals(project.getGroupId())
                         && artifact.getArtifactId().startsWith("tuscany-feature-")) {
                         log.info("Dependent distribution: " + artifact);
                         MavenProject pomProject = buildProject(artifact);
                         poms.add(pomProject);
                         // log.info(pomProject.getArtifactMap().toString());
                     }
                 }
             }
 
             // Process all the dependency artifacts
             ProjectSet bundleSymbolicNames = new ProjectSet(poms);
             ProjectSet bundleLocations = new ProjectSet(poms);
             ProjectSet jarNames = new ProjectSet(poms);
             ProjectSet serviceProviders = new ProjectSet(poms);
             
             for (Artifact artifact: artifacts) {
 
                 // Only consider Compile and Runtime dependencies
                 if (!(Artifact.SCOPE_COMPILE.equals(artifact.getScope()) || Artifact.SCOPE_RUNTIME.equals(artifact
                     .getScope())
                     || Artifact.SCOPE_PROVIDED.equals(artifact.getScope()) || (generateTargetPlatform && Artifact.SCOPE_TEST
                     .equals(artifact.getScope())))) {
                     log.info("Skipping artifact: " + artifact);
                     continue;
                 }
 
                 if (artifactTypes == null) {
                     artifactTypes = "jar,bundle";
                 }
                 String types[] = artifactTypes.trim().split("( |\t|\n|\r|\f|,)+");
                 Set<String> typeSet = new HashSet<String>(Arrays.asList(types));
 
                 // Only consider JAR and WAR files
                 if (!typeSet.contains(artifact.getType())) {
                     log.debug("Artifact with unknown type is skipped: " + artifact);
                     continue;
                 }
 
                 // Exclude artifact if its groupId is excluded or if it's not included
                 if (excludedGroupIds.contains(artifact.getGroupId())) {
                     log.debug("Artifact groupId is excluded: " + artifact);
                     continue;
                 }
                 if (!includedGroupIds.isEmpty()) {
                     if (!includedGroupIds.contains(artifact.getGroupId())) {
                         log.debug("Artifact groupId is not included: " + artifact);
                         continue;
                     }
                 }
 
                 File artifactFile = artifact.getFile();
                 if (!artifactFile.exists()) {
                     log.warn("Artifact doesn't exist: " + artifact);
                     continue;
                 }
 
                 if (log.isDebugEnabled()) {
                     log.debug("Processing artifact: " + artifact);
                 }
 
                 Manifest customizedMF = findManifest(artifact);
 
                 // Get the bundle name if the artifact is an OSGi bundle
                 Manifest mf = null;
                 String bundleName = null;
                 try {
                     mf = BundleUtil.getManifest(artifactFile);
                     bundleName = BundleUtil.getBundleSymbolicName(mf);
                 } catch (IOException e) {
                     throw new MojoExecutionException(e.getMessage(), e);
                 }
 
                 if (bundleName != null && customizedMF == null) {
 
                     // Exclude artifact if its file name is excluded
                     if (excludedFileNames.contains(artifactFile.getName())) {
                         log.debug("Artifact file is excluded: " + artifact);
                         continue;
                     }
 
                     // Copy an OSGi bundle as is
                     log.info("Adding OSGi bundle artifact: " + artifact);
 
                     if (!expandThirdPartyBundle || artifact.getGroupId().startsWith("org.apache.tuscany.sca")
                         || artifact.getGroupId().startsWith("org.eclipse")) {
                         copyFile(artifactFile, root);
                         bundleSymbolicNames.add(artifact, bundleName);
                         bundleLocations.add(artifact, artifactFile.getName());
                         jarNames.add(artifact, artifactFile.getName());
                         if (isServiceProvider(mf)) {
                             serviceProviders.add(artifact, bundleName);
                         }
                     } else {
                         // Expanding the bundle into a folder
 
                         setBundleClassPath(mf, artifactFile);
 
                         int index = artifactFile.getName().lastIndexOf('.');
                         String dirName = artifactFile.getName().substring(0, index);
                         File dir = new File(root, dirName);
 
                         File file = new File(dir, "META-INF");
                         file.mkdirs();
                         file = new File(file, "MANIFEST.MF");
 
                         FileOutputStream fos = new FileOutputStream(file);
                         write(mf, fos);
                         fos.close();
                         copyFile(artifactFile, dir);
                         bundleSymbolicNames.add(artifact, bundleName);
                         bundleLocations.add(artifact, dir.getName());
                         jarNames.add(artifact, dirName + "/" + artifactFile.getName());
                         if (isServiceProvider(mf)) {
                             serviceProviders.add(artifact, bundleName);
                         }
                     }
 
                 } else if ("war".equals(artifact.getType())) {
 
                     // Exclude artifact if its file name is excluded
                     if (excludedFileNames.contains(artifactFile.getName())) {
                         log.debug("Artifact file is excluded: " + artifact);
                         continue;
                     }
 
                     // Copy a WAR as is
                     log.info("Adding WAR artifact: " + artifact);
                     copyFile(artifactFile, root);
 
                 } else {
 
                     int index = artifactFile.getName().lastIndexOf('.');
                     String dirName = artifactFile.getName().substring(0, index);
                     File dir = new File(root, dirName);
 
                     // Exclude artifact if its file name is excluded
                     if (excludedFileNames.contains(dir.getName())) {
                         log.debug("Artifact file is excluded: " + artifact);
                         continue;
                     }
 
                     if (artifactAggregations != null) {
                         boolean aggregated = false;
                         for (ArtifactAggregation group : artifactAggregations) {
                             if (group.matches(artifact)) {
                                 group.getArtifacts().add(artifact);
                                 aggregated = true;
                                 break;
                             }
                         }
                         if (aggregated) {
                             continue;
                         }
                     }
 
                     // Create a bundle directory for a non-OSGi JAR
                     log.info("Adding JAR artifact: " + artifact);
 
                     // create manifest directory
                     File file = new File(dir, "META-INF");
                     file.mkdirs();
 
                     String symbolicName = null;
                     if (customizedMF == null) {
                         String version = BundleUtil.osgiVersion(artifact.getVersion());
 
                         Set<File> jarFiles = new HashSet<File>();
                         jarFiles.add(artifactFile);
                         symbolicName = (artifact.getGroupId() + "." + artifact.getArtifactId());
                         mf =
                             BundleUtil.libraryManifest(jarFiles,
                                                        symbolicName,
                                                        symbolicName,
                                                        version,
                                                        null,
                                                        this.eclipseBuddyPolicy,
                                                        this.executionEnvironment);
 
                         file = new File(file, "MANIFEST.MF");
                         FileOutputStream fos = new FileOutputStream(file);
                         write(mf, fos);
                         fos.close();
                         log.info("Writing generated manifest for: " + artifact + " to " + file);
                     } else {
                         mf = customizedMF;
                         symbolicName = BundleUtil.getBundleSymbolicName(mf);
                         if (symbolicName == null) {
                             throw new MojoExecutionException("Invalid customized MANIFEST.MF for " + artifact);
                         }
                         setBundleClassPath(mf, artifactFile);
                         
                         // re-find the custom MF file and copy it
                         // I can't get the  manifest file from the manifest itself
                         // the Manifest read/write operation seems to be filtering
                         // out some entries that I've added manually????
                         File artifactManifest = null;
 
                         if (artifactManifests != null) {
                             for (ArtifactManifest m : artifactManifests) {
                                 if (m.matches(artifact)) {
                                     artifactManifest = m.getManifestFile();
                                     break; 
                                 }
                             }
                         }
 
                         file = new File(file, "MANIFEST.MF");
 
                         if (artifactManifest != null){ 
                             log.info("Copying: " + artifactManifest + " to " + file);
                             copyManifest(artifactManifest, file);                         
                         } else {
                             FileOutputStream fos = new FileOutputStream(file);
                             write(mf, fos);
                             fos.close();
                             log.info("Writing generated manifest for: " + artifact + " to " + file);
                         }
                     }
 
                     copyFile(artifactFile, dir);
                     bundleSymbolicNames.add(artifact, symbolicName);
                     bundleLocations.add(artifact, dir.getName());
                     jarNames.add(artifact, dirName + "/" + artifactFile.getName());
                     if (isServiceProvider(mf)) {
                         serviceProviders.add(artifact, symbolicName);
                     }
                 }
             }
 
             if (artifactAggregations != null) {
                 for (ArtifactAggregation group : artifactAggregations) {
                     if (group.getArtifacts().isEmpty()) {
                         continue;
                     }
                     String symbolicName = group.getSymbolicName();
                     String version = group.getVersion();
                     File dir = new File(root, symbolicName + "-" + version);
                     dir.mkdir();
                     Set<File> jarFiles = new HashSet<File>();
                     Artifact artifact = null;
                     for (Artifact a : group.getArtifacts()) {
                         log.info("Aggragating JAR artifact: " + a);
                         artifact = a;
                         jarFiles.add(a.getFile());
                         copyFile(a.getFile(), dir);
                         jarNames.add(a, symbolicName + "-" + version + "/" + a.getFile().getName());
                     }
                     Manifest mf =
                         BundleUtil.libraryManifest(jarFiles,
                                                    symbolicName,
                                                    symbolicName,
                                                    version,
                                                    null,
                                                    this.eclipseBuddyPolicy,
                                                    this.executionEnvironment);
                     File file = new File(dir, "META-INF");
                     file.mkdirs();
                     file = new File(file, "MANIFEST.MF");
 
                     FileOutputStream fos = new FileOutputStream(file);
                     write(mf, fos);
                     fos.close();
                     log.info("Written aggregate manifest");
                     bundleSymbolicNames.add(artifact, symbolicName);
                     bundleLocations.add(artifact, dir.getName());
                     if (isServiceProvider(mf)) {
                         serviceProviders.add(artifact, symbolicName);
                     }
                 }
             }
 
             if (generateGatewayBundle) {
                 generateGatewayBundle(serviceProviders);
             }
 
             /*
             if (useDistributionName) {
                 bundleLocations.nameMap.remove(project.getArtifactId());
                 jarNames.nameMap.remove(project.getArtifactId());
                 bundleSymbolicNames.nameMap.remove(project.getArtifactId());
             }
             */
             
             // Generate a PDE target
             if (generateTargetPlatform) {
                 generatePDETarget(bundleSymbolicNames, root, log);
             }
 
             // Generate a plugin.xml referencing the PDE target
             if (generatePlugin) {
                 File pluginxml = new File(project.getBasedir(), "plugin.xml");
                 FileOutputStream pluginXMLFile = new FileOutputStream(pluginxml);
                 writePluginXML(new PrintStream(pluginXMLFile));
                 pluginXMLFile.close();
             }
 
             if (generateConfig) {
                 generateEquinoxConfig(bundleLocations, root, log);
             }
 
             if (generateManifestJar) {
                 generateManifestJar(jarNames, root, log);
                 generateEquinoxLauncherManifestJar(jarNames, root, log);
             }
 
             if (generateAntScript) {
                 generateANTPath(jarNames, root, log);
             }
             
             if (generateAggregatedBundle) {
                 generateAggregatedBundles(bundleLocations, root, log);
             }
 
         } catch (Exception e) {
             throw new MojoExecutionException(e.getMessage(), e);
         }
 
     }
 
     private Set<Artifact> getDependencyArtifacts(MavenProject project) throws DependencyTreeBuilderException,
         ArtifactResolutionException, ArtifactNotFoundException {
         Log log = getLog();
         Set<Artifact> artifacts = new HashSet<Artifact>();
         ArtifactFilter artifactFilter = createResolvingArtifactFilter(Artifact.SCOPE_RUNTIME);
 
         // TODO: note that filter does not get applied due to MNG-3236
 
         DependencyNode rootNode =
             dependencyTreeBuilder.buildDependencyTree(project,
                                                       local,
                                                       factory,
                                                       artifactMetadataSource,
                                                       artifactFilter,
                                                       artifactCollector);
         CollectingDependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor();
         rootNode.accept(visitor);
         
         // Add included artifacts
         for (Object node : visitor.getNodes()) {
             DependencyNode depNode = (DependencyNode)node;
             int state = depNode.getState();
             if (state == DependencyNode.INCLUDED ) {
                 Artifact artifact = depNode.getArtifact();
                 // Exclude the project artifact to avoid offline resolution failure
                 if (!artifact.equals(project.getArtifact())) {
                     resolver.resolve(artifact, remoteRepos, local);
                     artifacts.add(artifact);
                 }
             }
         }
         // Scan for newer versions that are omitted
         for (Object node : visitor.getNodes()) {
             DependencyNode depNode = (DependencyNode)node;
             int state = depNode.getState();
             if (state == DependencyNode.OMITTED_FOR_CONFLICT) {
                 Artifact artifact = depNode.getArtifact();
                 resolver.resolve(artifact, remoteRepos, local);
                 if (state == DependencyNode.OMITTED_FOR_CONFLICT) {
                     Artifact related = depNode.getRelatedArtifact();
                     if (log.isDebugEnabled()) {
                         log.debug("Dependency node: " + depNode);
                     }
                     // Compare the version
                     ArtifactVersion v1 = new DefaultArtifactVersion(artifact.getVersion());
                     ArtifactVersion v2 = new DefaultArtifactVersion(related.getVersion());
                     if (v1.compareTo(v2) > 0) {
                         // Only add newer version if it is omitted for conflict
                         if (artifacts.add(artifact)) {
                             log.info("Dependency node added: " + depNode);
                         }
                     }
                 }
             }
         }
         return artifacts;
     }
     
     private static boolean isServiceProvider(Manifest mf) {
         if (mf != null) {
             String export = (String)mf.getMainAttributes().getValue(Constants.EXPORT_PACKAGE);
             if (export != null && export.contains(BundleUtil.META_INF_SERVICES)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Generate a gateway bundle that aggregate other bundles to handle split packages
      * @param bundleSymbolicNames
      * @throws FileNotFoundException
      * @throws IOException
      */
     private void generateGatewayBundle(ProjectSet bundleSymbolicNames) throws FileNotFoundException, IOException {
         Manifest manifest = new Manifest();
         Attributes attrs = manifest.getMainAttributes();
         StringBuffer requireBundle = new StringBuffer();
         for (String name : new HashSet<String>(bundleSymbolicNames.artifactToNameMap.values())) {
             requireBundle.append(name).append(";").append(RESOLUTION_DIRECTIVE).append(":=")
                 .append(RESOLUTION_OPTIONAL);
             if (gatewayReexport) {
                 requireBundle.append(";").append(VISIBILITY_DIRECTIVE).append(":=").append(VISIBILITY_REEXPORT);
             }
             requireBundle.append(",");
         }
         int len = requireBundle.length();
         if (len > 0 && requireBundle.charAt(len - 1) == ',') {
             requireBundle.deleteCharAt(len - 1);
             attrs.putValue(Constants.REQUIRE_BUNDLE, requireBundle.toString());
             attrs.putValue("Manifest-Version", "1.0");
             attrs.putValue("Implementation-Vendor", "The Apache Software Foundation");
             attrs.putValue("Implementation-Vendor-Id", "org.apache");
             attrs.putValue(Constants.BUNDLE_VERSION, "2.0.0");
             attrs.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
             attrs.putValue(Constants.BUNDLE_SYMBOLICNAME, GATEWAY_BUNDLE);
             attrs.putValue(Constants.BUNDLE_NAME, "Apache Tuscany SCA Gateway Bundle");
             attrs.putValue(Constants.BUNDLE_VENDOR, "The Apache Software Foundation");
             attrs.putValue(Constants.EXPORT_PACKAGE, "META-INF.services");
             attrs.putValue(Constants.DYNAMICIMPORT_PACKAGE, "*");
             attrs.putValue(Constants.BUNDLE_ACTIVATIONPOLICY, Constants.ACTIVATION_LAZY);
             File file = new File(targetDirectory, "tuscany-gateway-" + project.getVersion() + ".jar");
             getLog().info("Generating gateway bundle: " + file.getAbsolutePath());
             FileOutputStream fos = new FileOutputStream(file);
             JarOutputStream jos = new JarOutputStream(fos, manifest);
             addFileToJar(jos, "META-INF/LICENSE", getClass().getResource("LICENSE.txt"));
             addFileToJar(jos, "META-INF/NOTICE", getClass().getResource("NOTICE.txt"));
             jos.close();
         }
     }
 
     private void setBundleClassPath(Manifest mf, File artifactFile) {
         // Add the Bundle-ClassPath
         String cp = mf.getMainAttributes().getValue(BUNDLE_CLASSPATH);
         if (cp == null) {
             cp = artifactFile.getName();
         } else {
             cp = cp + "," + artifactFile.getName();
         }
         mf.getMainAttributes().putValue(BUNDLE_CLASSPATH, cp);
     }
 
     private void generateANTPath(ProjectSet jarNames, File root, Log log) throws FileNotFoundException, IOException {
         for (Map.Entry<String, Set<String>> e : jarNames.nameMap.entrySet()) {
             Set<String> jars = e.getValue();
             File feature = new File(root, "../" + featuresName + "/" + (useDistributionName ? trim(e.getKey()) : ""));
             feature.mkdirs();
             File antPath = new File(feature, "build-path.xml");
             log.info("Generating ANT build path: " + antPath.getCanonicalPath());
             FileOutputStream fos = new FileOutputStream(antPath);
             PrintStream ps = new PrintStream(fos);
             // ps.println(XML_PI);
             ps.println(ASL_HEADER);
             String name = trim(e.getKey());
             ps.println("<project name=\"tuscany." + name + "\">");
             ps.println("  <property name=\"tuscany.distro\" value=\"" + name + "\"/>");
             ps.println("  <property name=\"tuscany.manifest\" value=\"" + new File(feature, manifestJarName)
                 .getCanonicalPath()
                 + "\"/>");
             ps.println("  <path id=\"" + "tuscany.path" + "\">");
             ps.println("    <fileset dir=\"" + root.getCanonicalPath() + "\">");
             for (String jar : jars) {
                 ps.println("      <include name=\"" + jar + "\"/>");
             }
             ps.println("    </fileset>");
             ps.println("  </path>");
             ps.println("</project>");
         }
     }
 
     private void generateManifestJar(ProjectSet jarNames, File root, Log log) throws FileNotFoundException, IOException {
         for (Map.Entry<String, Set<String>> e : jarNames.nameMap.entrySet()) {
             MavenProject pom = jarNames.getProject(e.getKey());
             Set<String> jars = e.getValue();
             File feature = new File(root, "../" + featuresName + "/" + (useDistributionName ? trim(e.getKey()) : ""));
             feature.mkdirs();
             File mfJar = new File(feature, manifestJarName);
             log.info("Generating manifest jar: " + mfJar.getCanonicalPath());
             FileOutputStream fos = new FileOutputStream(mfJar);
             Manifest mf = new Manifest();
             StringBuffer cp = new StringBuffer();
             String path = (useDistributionName ? "../../" : "../") + root.getName();
             for (String jar : jars) {
                 cp.append(path).append('/').append(jar).append(' ');
             }
             if (cp.length() > 0) {
                 cp.deleteCharAt(cp.length() - 1);
             }
             Attributes attrs = mf.getMainAttributes();
             attrs.putValue("Manifest-Version", "1.0");
             attrs.putValue("Implementation-Title", pom.getName());
             attrs.putValue("Implementation-Vendor", "The Apache Software Foundation");
             attrs.putValue("Implementation-Vendor-Id", "org.apache");
             attrs.putValue("Implementation-Version", pom.getVersion());
             attrs.putValue("Class-Path", cp.toString());
             attrs.putValue("Main-Class", "org.apache.tuscany.sca.node.launcher.NodeMain");
             JarOutputStream jos = new JarOutputStream(fos, mf);
             addFileToJar(jos, "META-INF/LICENSE", getClass().getResource("LICENSE.txt"));
             addFileToJar(jos, "META-INF/NOTICE", getClass().getResource("NOTICE.txt"));
             jos.close();
         }
     }
 
     private void generateEquinoxLauncherManifestJar(ProjectSet jarNames, File root, Log log) throws Exception {
         String equinoxLauncher = "org.apache.tuscany.sca:tuscany-node-launcher-equinox";
         Artifact artifact = (Artifact)project.getArtifactMap().get(equinoxLauncher);
         if (artifact == null) {
             return;
         }
         Set artifacts = resolveTransitively(artifact).getArtifacts();
         File feature = new File(root, "../" + featuresName + "/");
         feature.mkdirs();
         File mfJar = new File(feature, equinoxManifestJarName);
         log.info("Generating equinox manifest jar: " + mfJar.getCanonicalPath());
         FileOutputStream fos = new FileOutputStream(mfJar);
         Manifest mf = new Manifest();
         StringBuffer cp = new StringBuffer();
         String path = "../" + root.getName();
 
         for (Object o : artifacts) {
             Artifact a = (Artifact)o;
             if (!Artifact.SCOPE_TEST.equals(a.getScope())) {
                 String id = ArtifactUtils.versionlessKey(a);
                 String jar = jarNames.artifactToNameMap.get(id);
                 if (jar != null) {
                     cp.append(path).append('/').append(jar).append(' ');
                 }
             }
         }
         if (cp.length() > 0) {
             cp.deleteCharAt(cp.length() - 1);
         }
         Attributes attrs = mf.getMainAttributes();
         attrs.putValue("Manifest-Version", "1.0");
         attrs.putValue("Implementation-Title", artifact.getId());
         attrs.putValue("Implementation-Vendor", "The Apache Software Foundation");
         attrs.putValue("Implementation-Vendor-Id", "org.apache");
         attrs.putValue("Implementation-Version", artifact.getVersion());
         attrs.putValue("Class-Path", cp.toString());
         attrs.putValue("Main-Class", "org.apache.tuscany.sca.node.equinox.launcher.NodeMain");
         JarOutputStream jos = new JarOutputStream(fos, mf);
         addFileToJar(jos, "META-INF/LICENSE", getClass().getResource("LICENSE.txt"));
         addFileToJar(jos, "META-INF/NOTICE", getClass().getResource("NOTICE.txt"));
         jos.close();
     }
 
     private void generateEquinoxConfig(ProjectSet bundleLocations, File root, Log log) throws IOException {
         for (Map.Entry<String, Set<String>> e : bundleLocations.nameMap.entrySet()) {
             Set<String> locations = new HashSet<String>(e.getValue());
             if (generateGatewayBundle) {
                 locations.add("tuscany-gateway-" + project.getVersion() + ".jar");
             }
             File feature = new File(root, "../" + featuresName + "/" + (useDistributionName ? trim(e.getKey()) : ""));
             File config = new File(feature, "configuration");
             config.mkdirs();
             File ini = new File(config, "config.ini");
             log.info("Generating configuation: " + ini.getCanonicalPath());
             FileOutputStream fos = new FileOutputStream(ini);
             PrintStream ps = new PrintStream(fos);
             int size = locations.size();
             if (size > 0) {
                 ps.println("osgi.bundles=\\");
                 int count = 0;
                 for (String f : locations) {
                     if (f.startsWith("osgi")) {
                         continue;
                     }
                     ps.print("    ");
                     ps.print(f);
                     // FIXME: We should not add @start for fragments
                     if (generateBundleStart) {
                         ps.print("@:start");
                     }
                     if (count == size - 1) {
                         // Last one
                         ps.println();
                     } else {
                         ps.println(",\\");
                     }
                     count++;
                 }
             }
             ps.println("eclipse.ignoreApp=true");
             // Do not shutdown
             ps.println("osgi.noShutdown=true");
             ps.close();
         }
     }
 
     private void generateAggregatedBundles(ProjectSet bundleLocations, File root, Log log) throws Exception {
         for (Map.Entry<String, Set<String>> e : bundleLocations.nameMap.entrySet()) {
             Set<String> locations = new HashSet<String>(e.getValue());
             String featureName = (useDistributionName ? trim(e.getKey()) : "");
             File feature = new File(root, "../" + featuresName + "/" + featureName);
 //            String bundleFileName = "tuscany-bundle-" + featureName + ".jar";
 //            if ("".equals(featureName)) {
 //                bundleFileName = "tuscany-bundle.jar";
 //            }
             String bundleFileName = "tuscany-bundle.jar";
             File bundleFile = new File(feature, bundleFileName);
             log.info("Generating aggregated OSGi bundle: " + bundleFile);
             File[] files = new File[locations.size()];
             int i = 0;
             for (String child : locations) {
                 files[i++] = new File(root, child);
             }
             String bundleVersion = "2.0.0";
             String bundleName = "org.apache.tuscany.sca.bundle";
             
 //            String bundleName = "org.apache.tuscany.sca.bundle." + featureName;
 //            if ("".equals(featureName)) {
 //                bundleName = "org.apache.tuscany.sca.bundle";
 //            }
             BundleAggregatorMojo.aggregateBundles(log, root, files, bundleFile, bundleName, bundleVersion);
         }
     }
 
     private void generatePDETarget(ProjectSet bundleSymbolicNames, File root, Log log) throws FileNotFoundException,
         IOException {
         for (Map.Entry<String, Set<String>> e : bundleSymbolicNames.nameMap.entrySet()) {
             Set<String> bundles = new HashSet<String>(e.getValue());
             String name = trim(e.getKey());
             File feature = new File(root, "../" + featuresName + "/" + (useDistributionName ? name : ""));
             feature.mkdirs();
             File target = new File(feature, "tuscany.target");
             log.info("Generating target definition: " + target.getCanonicalPath());
             FileOutputStream targetFile = new FileOutputStream(target);
             if (!bundles.contains("org.eclipse.osgi")) {
                 bundles.add("org.eclipse.osgi");
             }
             if (generateGatewayBundle) {
                 bundles.add(GATEWAY_BUNDLE);
             }
             writeTarget(new PrintStream(targetFile), name, bundles, eclipseFeatures);
             targetFile.close();
 
             // Generate the PDE target definition file for PDE 3.5
             File target35 = new File(feature, "tuscany-pde35.target");
             log.info("Generating target definition: " + target35.getCanonicalPath());
             FileOutputStream target35File = new FileOutputStream(target35);
             writePDE35Target(new PrintStream(target35File), name, bundles, eclipseFeatures);
             target35File.close();
 
         }
     }
 
     private MavenProject buildProject(Artifact artifact) throws ProjectBuildingException,
         InvalidDependencyVersionException, ArtifactResolutionException, ArtifactNotFoundException, DependencyTreeBuilderException {
         MavenProject pomProject = mavenProjectBuilder.buildFromRepository(artifact, this.remoteRepos, this.local);
         if (pomProject.getDependencyArtifacts() == null) {
             pomProject.setDependencyArtifacts(pomProject.createArtifacts(factory, null, // Artifact.SCOPE_TEST,
                                                                          new ScopeArtifactFilter(Artifact.SCOPE_TEST)));
         }
         if (includeConflictingDepedencies) {
             pomProject.setArtifacts(getDependencyArtifacts(pomProject));
         } else {
             ArtifactResolutionResult result =
                 resolver.resolveTransitively(pomProject.getDependencyArtifacts(),
                                              pomProject.getArtifact(),
                                              remoteRepos,
                                              local,
                                              artifactMetadataSource);
             pomProject.setArtifacts(result.getArtifacts());
         }
         return pomProject;
     }
 
     private ArtifactResolutionResult resolveTransitively(Artifact artifact) throws ArtifactResolutionException,
         ArtifactNotFoundException {
         Artifact originatingArtifact = factory.createBuildArtifact("dummy", "dummy", "1.0", "jar");
 
         return resolver.resolveTransitively(Collections.singleton(artifact),
                                             originatingArtifact,
                                             local,
                                             remoteRepos,
                                             artifactMetadataSource,
                                             null);
     }
 
     /**
      * Convert tuscany-feature-xyz to feature-xyz
      * @param artifactId
      * @return
      */
     private String trim(String artifactId) {
         if (artifactId.startsWith("tuscany-feature-")) {
             return artifactId.substring("tuscany-feature-".length());
         } else {
             return artifactId;
         }
     }
 
     private static void copyFile(File jar, File dir) throws FileNotFoundException, IOException {
         byte[] buf = new byte[4096];
         File jarFile = new File(dir, jar.getName());
         FileInputStream in = new FileInputStream(jar);
         FileOutputStream out = new FileOutputStream(jarFile);
         for (;;) {
             int len = in.read(buf);
             if (len > 0) {
                 out.write(buf, 0, len);
             } else {
                 break;
             }
         }
         in.close();
         out.close();
     }
 
     private static void copyManifest(File mfFrom, File mfTo) throws FileNotFoundException, IOException {
         byte[] buf = new byte[4096];
         FileInputStream in = new FileInputStream(mfFrom);
         FileOutputStream out = new FileOutputStream(mfTo);
         for (;;) {
             int len = in.read(buf);
             if (len > 0) {
                 out.write(buf, 0, len);
             } else {
                 break;
             }
         }
         in.close();
         out.close();
     }
 
     private static void addFileToJar(JarOutputStream out, String entryName, URL file) throws FileNotFoundException,
         IOException {
         byte[] buf = new byte[4096];
         InputStream in = file.openStream();
         out.putNextEntry(new ZipEntry(entryName));
         for (;;) {
             int len = in.read(buf);
             if (len > 0) {
                 out.write(buf, 0, len);
             } else {
                 break;
             }
         }
         in.close();
         out.closeEntry();
     }
 
     private void writeTarget(PrintStream ps, String pom, Set<String> ids, String[] features) {
         ps.println(XML_PI);
         ps.println("<?pde version=\"3.2\"?>");
         ps.println(ASL_HEADER);
 
         ps.println("<target name=\"Eclipse Target - " + pom + "\">");
 
         if (executionEnvironment != null) {
             ps.println("  <targetJRE>");
             ps.println("    <execEnv>" + executionEnvironment + "</execEnv>");
             ps.println("  </targetJRE>");
         }
 
         if (useDefaultLocation) {
             ps.println("  <location useDefault=\"true\"/>");
         } else {
             ps.println("  <location path=\"" + targetDirectory + "\"/>");
         }
 
         // ps.println("<content useAllPlugins=\"true\">");
         ps.println("  <content>");
         ps.println("    <plugins>");
         for (String id : ids) {
             ps.println("      <plugin id=\"" + id + "\"/>");
         }
         ps.println("    </plugins>");
         ps.println("    <features>");
         if (features != null) {
             for (String f : features) {
                 ps.println("      <feature id=\"" + f + "\"/>");
             }
         }
         ps.println("    </features>");
         if (useDefaultLocation) {
             ps.println("    <extraLocations>");
             // Not sure why the extra path needs to the plugins folder
             ps.println("      <location path=\"" + targetDirectory + "\"/>");
             ps.println("    </extraLocations>");
         }
         ps.println("  </content>");
 
         ps.println("</target>");
 
     }
 
     private void writePDE35Target(PrintStream ps, String pom, Set<String> ids, String[] features) {
         ps.println(XML_PI);
         ps.println("<?pde version=\"3.5\"?>");
         ps.println(ASL_HEADER);
 
         ps.println("<target name=\"Eclipse PDE 3.5 Target - " + pom + "\">");
 
         if (executionEnvironment != null) {
             ps
                 .println("  <targetJRE path=\"" + "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/"
                     + executionEnvironment
                     + "\"/>");
         }
 
         ps.println("<locations>");
         if (ids.size() > 0) {
             ps.println("  <location path=\"" + targetDirectory + "\" type=\"Directory\">");
             ps.println("    <includeBundles>");
             for (String id : ids) {
                 ps.println("      <plugin id=\"" + id + "\"/>");
             }
             ps.println("    </includeBundles>");
             ps.println("  </location>");
         }
 
         /*
         if (useDefaultLocation) {
             ps.println("  <location path=\"${eclipse_home}\" type=\"Profile\"/>");
         }
         */
 
         /*
         if (features != null) {
             for (String f : features) {
                 ps.println(" <location id=\"" + f + "\" path=\"\" type=\"Feature\"/>");
             }
         }
         */
 
         ps.println("</locations>");
         ps.println("</target>");
 
     }
 
     private static void writePluginXML(PrintStream ps) {
         ps.println(XML_PI);
         ps.println("<?pde version=\"3.2\"?>");
         ps.println(ASL_HEADER);
         ps.println("<plugin>");
         ps.println("<extension point = \"org.eclipse.pde.core.targets\">");
         ps.println("<target");
         ps.println("id=\"org.apache.tuscany.sca.target\"");
         ps.println("name=\"Apache Tuscany Eclipse Target\"");
         ps.println("path=\"tuscany.target\"/>");
         ps.println("</extension>");
         ps.println("</plugin>");
     }
 }
