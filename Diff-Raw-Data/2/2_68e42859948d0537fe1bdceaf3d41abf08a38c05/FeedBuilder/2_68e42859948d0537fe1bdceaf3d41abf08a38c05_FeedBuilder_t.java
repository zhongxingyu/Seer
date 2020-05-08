 package net.zeroinstall.pom2feed.core;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Strings.isNullOrEmpty;
 import java.io.IOException;
 import java.net.URL;
 import net.zeroinstall.model.*;
 import org.apache.maven.model.*;
 import static net.zeroinstall.pom2feed.core.VersionUtils.*;
 import static net.zeroinstall.pom2feed.core.ManifestUtils.*;
 import static net.zeroinstall.pom2feed.core.MavenUtils.*;
 import static net.zeroinstall.pom2feed.core.UrlUtils.*;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 
 /**
  * Iteratively builds Zero Install feeds using data from Maven projects.
  */
 public class FeedBuilder {
 
     /**
      * The base URL of the Maven repository used to provide binaries.
      */
     private final URL mavenRepository;
     /**
      * The base URL of the pom2feed service used to provide dependencies.
      */
     private final URL pom2feedService;
     /**
      * The feed being built.
      */
     private final InterfaceDocument document;
     /**
      * Shortcut to {@link InterfaceDocument#getInterface()}.
      */
     private final Feed feed;
     /**
      * Indicates whether lax versioning (allowing newer versions to substitute
      * older versions without explicit ranges) is used for dependencies.
      */
     private boolean laxDependencyVersions;
 
     /**
      * Creates feed builder for a new feed.
      *
      * @param mavenRepository The base URL of the Maven repository used to
      * provide binaries.
      * @param pom2feedService The base URL of the pom2feed service used to
      * provide dependencies.
      */
     public FeedBuilder(URL mavenRepository, URL pom2feedService) {
         this.mavenRepository = checkNotNull(mavenRepository);
         this.pom2feedService = checkNotNull(pom2feedService);
         this.document = InterfaceDocument.Factory.newInstance();
         this.feed = document.addNewInterface();
     }
 
     /**
      * Creates a feed builder an existing feed.
      *
      * @param pom2feedService The base URL of the pom2feed service used to
      * provide dependencies.
      * @param document The existing feed document.
      */
     public FeedBuilder(URL mavenRepository, URL pom2feedService, InterfaceDocument document) {
         this.mavenRepository = checkNotNull(mavenRepository);
         this.pom2feedService = checkNotNull(pom2feedService);
         this.document = checkNotNull(document);
         this.feed = (document.getInterface() != null) ? document.getInterface() : document.addNewInterface();
     }
 
     /**
      * Enables use of lax versioning (allowing newer versions to substitute
      * older versions without explicit ranges) for dependencies.
      */
     public FeedBuilder enableLaxDependencyVersions() {
         laxDependencyVersions = true;
         return this;
     }
 
     /**
      * Returns the generated feed/interface.
      *
      * @return An XML representation of the feed/interface.
      */
     public InterfaceDocument getDocument() {
         return document;
     }
 
     /**
      * Fills the feed with project-wide metadata from a Maven model.
      *
      * @param model The Maven model to extract the metadata from. Should be from
      * the latest version of the project.
      * @return The {@link FeedBuilder} instance for calling further methods in a
      * fluent fashion.
      */
     public FeedBuilder addMetadata(Model model) {
         checkNotNull(model);
 
         feed.addName(isNullOrEmpty(model.getName())
                 ? model.getArtifactId()
                 : model.getName());
         feed.addNewSummary().setStringValue("Maven artifact " + model.getGroupId() + ":" + model.getArtifactId());
         if (!isNullOrEmpty(model.getDescription())) {
             feed.addNewDescription().setStringValue(model.getDescription());
         }
         if (!isNullOrEmpty(model.getUrl())) {
             feed.addHomepage(model.getUrl());
         }
         return this;
     }
 
     /**
      * Adds a local-path implementation to the feed using version and dependency
      * information from a Maven model.
      *
      * @param model The Maven model to extract the version and dependency
      * information from.
      * @param directory The relative (Unix-stlye) path to the directory
      * containing the implementation
      * @return The {@link FeedBuilder} instance for calling further methods in a
      * fluent fashion.
      */
     public FeedBuilder addLocalImplementation(Model model, String directory) {
         checkNotNull(model);
 
         Implementation implementation = addNewImplementation(model);
         addDependencies(implementation, model);
 
         if (model.getPackaging().equals("jar")) {
             Command command = addNewCommand(implementation);
             command.setPath(getArtifactLocalFileName(model));
         }
         implementation.setLocalPath(checkNotNull(directory));
 
         return this;
     }
 
     /**
      * Adds a "download single file" implementation to the feed using version
      * and dependency information from a Maven model.
      *
      * @param model The Maven model to extract the version and dependency
      * information from.
      * @return The {@link FeedBuilder} instance for calling further methods in a
      * fluent fashion.
      * @throws IOException A file could not be retrieved from the Maven
      * repository.
      */
     public FeedBuilder addRemoteImplementation(Model model) throws IOException {
         checkNotNull(model);
 
         URL fileUrl = getArtifactFileUrl(mavenRepository, model.getGroupId(), model.getArtifactId(), model.getVersion(), model.getPackaging());
         long size = getRemoteFileSize(fileUrl);
         String hash = getRemoteWord(new URL(fileUrl.toString() + ".sha1"));
         String fileName = getArtifactFileName(model.getArtifactId(), model.getVersion(), model.getPackaging());
 
         Implementation implementation = addNewImplementation(model);
         addDependencies(implementation, model);
 
         ManifestDigest digest = implementation.addNewManifestDigest();
         digest.setSha1New(getSha1ManifestDigest(hash, size, fileName));
 
         File file = implementation.addNewFile();
         file.setHref(fileUrl.toString());
         file.setSize(size);
         file.setDest(fileName);
 
         if (model.getPackaging().equals("jar")) {
             Command command = addNewCommand(implementation);
             command.setPath(getArtifactFileName(model.getArtifactId(), model.getVersion(), "jar"));
         }
 
         return this;
     }
 
     /**
      * Adds an implementation to the feed using version and dependency
      * information from a Maven model.
      *
      * @param model The Maven model to extract the version and dependency
      * information from.
      * @return The implementation that was created and added to the feed.
      */
     private Implementation addNewImplementation(Model model) {
         Implementation implementation = feed.addNewImplementation();
         implementation.setId(model.getVersion());
         implementation.setVersion(convertVersion(model.getVersion()));
         implementation.setStability(Stability.STABLE);
         if (!model.getLicenses().isEmpty()) {
             implementation.setLicense(model.getLicenses().get(0).getName());
         }
         return implementation;
     }
 
     /**
      * Adds a Java run command for an implementation.
      *
      * @param implementation The implementation to add the command to.
      */
     private Command addNewCommand(Implementation implementation) {
         Command command = implementation.addNewCommand();
         command.setName("run");
 
         Runner runner = command.addNewRunner();
         runner.setInterface("http://repo.roscidus.com/java/openjdk-jre");
         runner.addArg("-jar");
 
         return command;
     }
 
     /**
      * Converts Maven dependencies to Zero Install dependencies.
      *
      * @param implementation The implementation to add the dependencies to.
      * @param model The Maven model to extract the dependencies from.
      */
     private void addDependencies(Implementation implementation, Model model) {
         if (model.getBuild() != null && model.getBuild().getPluginsAsMap() != null) {
             Plugin compilerPlugin = model.getBuild().getPluginsAsMap().get("org.apache.maven.plugins:maven-compiler-plugin");
             if (compilerPlugin != null) {
                 Xpp3Dom config = (Xpp3Dom) compilerPlugin.getConfiguration();
                 if (config != null) {
                     Xpp3Dom targetConfig = config.getChild("target");
                     if (targetConfig != null && !isNullOrEmpty(targetConfig.getValue())) {
                         addJavaDependency(implementation, targetConfig.getValue());
                     }
                 }
             }
         }
 
         for (org.apache.maven.model.Dependency mavenDep : model.getDependencies()) {
             if (isNullOrEmpty(mavenDep.getScope()) || mavenDep.getScope().equals("runtime") || mavenDep.getScope().equals("compile")) {
                 addArtifactDependency(implementation, mavenDep);
             }
         }
     }
 
     private void addArtifactDependency(Implementation implementation, org.apache.maven.model.Dependency mavenDep) {
         // HACK: Workaround for broken POMs referencing test code at runtime
         if (mavenDep.getGroupId().equals("junit") || mavenDep.getGroupId().equals("com.google.code.findbugs")) {
             return;
         }
 
         net.zeroinstall.model.Dependency ziDep = implementation.addNewRequires();
         ziDep.setInterface(MavenUtils.getServiceUrl(pom2feedService, mavenDep.getGroupId(), mavenDep.getArtifactId()));
         if (laxDependencyVersions && !isMavenRange(mavenDep.getVersion())) {
             ziDep.setVersion(convertVersion(mavenDep.getVersion()) + "..");
         } else {
             ziDep.setVersion(convertRange(mavenDep.getVersion()));
         }
         if ("true".equals(mavenDep.getOptional())) {
             ziDep.setImportance(Importance.RECOMMENDED);
         }
 
         Environment environment = ziDep.addNewEnvironment();
         environment.setName("CLASSPATH");
         environment.setInsert(".");
     }
 
     private void addJavaDependency(Implementation implementation, String javaVersion) {
         net.zeroinstall.model.Dependency javaDep = implementation.addNewRequires();
         javaDep.setInterface("http://repo.roscidus.com/java/openjdk-jre");
        javaDep.setVersion(javaVersion + "..");
     }
 }
