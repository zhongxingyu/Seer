 package com.atlassian.maven.plugins.amps;
 
 import com.atlassian.core.util.FileUtils;
 import com.atlassian.maven.plugins.amps.util.VersionUtils;
 import com.atlassian.maven.plugins.amps.util.ZipUtils;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.PluginManager;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
 
 /**
  * Executes specific maven goals
  */
 public class MavenGoals
 {
     private final MavenProject project;
     private final List<MavenProject> reactor;
     private final MavenSession session;
     private final PluginManager pluginManager;
     private final Log log;
     private final Map<String, String> pluginArtifactIdToVersionMap;
 
     private final Map<String, Container> idToContainerMap = new HashMap<String, Container>()
     {{
             put("tomcat5x", new Container("tomcat5x", "org.apache.tomcat", "apache-tomcat", "5.5.26"));
             put("tomcat6x", new Container("tomcat6x", "org.apache.tomcat", "apache-tomcat", "6.0.20"));
             put("resin3x", new Container("resin3x", "com.caucho", "resin", "3.0.26"));
             put("jboss42x", new Container("jboss42x", "org.jboss.jbossas", "jbossas", "4.2.3.GA"));
             put("jetty6x", new Container("jetty6x"));
         }};
 
     private final Map<String, String> defaultArtifactIdToVersionMap = new HashMap<String, String>()
     {{
             put("maven-cli-plugin", "0.7");
             put("cargo-maven2-plugin", "1.0-beta-2-db2");
             put("atlassian-pdk", "2.1.6");
             put("maven-archetype-plugin", "2.0-alpha-4");
             put("maven-bundle-plugin", "2.0.0");
             put("yuicompressor-maven-plugin", "0.7.1");
 
             // You can't actually override the version a plugin if defined in the project, so these don't actually do
             // anything, since the super pom already defines versions.
             put("maven-dependency-plugin", "2.0");
             put("maven-resources-plugin", "2.3");
             put("maven-jar-plugin", "2.2");
             put("maven-surefire-plugin", "2.4.3");
 
         }};
 
     public MavenGoals(final MavenContext ctx)
     {
         this(ctx, Collections.<String, String>emptyMap());
     }
 
     public MavenGoals(final MavenContext ctx, final Map<String, String> pluginToVersionMap)
     {
         this.project = ctx.getProject();
         this.reactor = ctx.getReactor();
         this.session = ctx.getSession();
         this.pluginManager = ctx.getPluginManager();
         this.log = ctx.getLog();
 
         final Map<String, String> map = new HashMap<String, String>(defaultArtifactIdToVersionMap);
         map.putAll(pluginToVersionMap);
         this.pluginArtifactIdToVersionMap = Collections.unmodifiableMap(map);
 
     }
 
     public void startCli(final PluginInformation pluginInformation, final int port) throws MojoExecutionException
     {
         final String pluginId = pluginInformation.getId();
 
         final List<Element> configs = new ArrayList<Element>();
         configs.add(element(name("commands"),
                 element(name("pi"), new StringBuilder()
                         .append("resources").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:filter-plugin-descriptor").append(" ")
                         .append("compile").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:copy-bundled-dependencies").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:compress-resources").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:generate-manifest").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:validate-manifest").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:jar").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:install").toString()),
                 element(name("tpi"), new StringBuilder()
                         .append("testResources").append(" ")
                         .append("testCompile").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:test-jar").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:test-install").toString()),
                 element(name("package"), new StringBuilder()
                         .append("resources").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:filter-plugin-descriptor").append(" ")
                         .append("compile").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:copy-bundled-dependencies").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:generate-manifest").append(" ")
                         .append("com.atlassian.maven.plugins:maven-").append(pluginId).append("-plugin:jar").append(" ").toString())));
         if (port > 0)
         {
             configs.add(element(name("port"), String.valueOf(port)));
         }
         executeMojo(
                 plugin(
                         groupId("org.twdata.maven"),
                         artifactId("maven-cli-plugin"),
                         version(pluginArtifactIdToVersionMap.get("maven-cli-plugin"))
                 ),
                 goal("execute"),
                 configuration(configs.toArray(new Element[0])),
                 executionEnvironment(project, session, pluginManager));
     }
 
     public void createPlugin(final String productId) throws MojoExecutionException
     {
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-archetype-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-archetype-plugin"))
                 ),
                 goal("generate"),
                 configuration(
                         element(name("archetypeGroupId"), "com.atlassian.maven.archetypes"),
                         element(name("archetypeArtifactId"), productId + "-plugin-archetype"),
                         element(name("archetypeVersion"), VersionUtils.getVersion())
                 ),
                 executionEnvironment(project, session, pluginManager));
     }
 
     public void copyBundledDependencies() throws MojoExecutionException
     {
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-dependency-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-dependency-plugin"))
                 ),
                 goal("copy-dependencies"),
                 configuration(
                         element(name("includeScope"), "runtime"),
                         element(name("excludeScope"), "provided"),
                         element(name("excludeScope"), "test"),
                         element(name("includeTypes"), "jar"),
                         element(name("outputDirectory"), "${project.build.outputDirectory}/META-INF/lib")
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void extractBundledDependencies() throws MojoExecutionException
     {
          executeMojo(
                  plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-dependency-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-dependency-plugin"))
                 ),
                 goal("unpack-dependencies"),
                 configuration(
                         element(name("includeScope"), "runtime"),
                         element(name("excludeScope"), "provided"),
                         element(name("excludeScope"), "test"),
                         element(name("includeTypes"), "jar"),
                         element(name("excludes"), "META-INF/MANIFEST.MF, META-INF/*.DSA, META-INF/*.SF"),
                         element(name("outputDirectory"), "${project.build.outputDirectory}")
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
     
     public void compressResources() throws MojoExecutionException
     {
         executeMojo(
                 plugin(
                         groupId("net.sf.alchim"),
                         artifactId("yuicompressor-maven-plugin"),
                         version(defaultArtifactIdToVersionMap.get("yuicompressor-maven-plugin"))
                 ),
                 goal("compress"),
                 configuration(
                         element(name("suffix"), "-min"),
                         element(name("jswarn"), "false")
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void filterPluginDescriptor() throws MojoExecutionException
     {
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-resources-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-resources-plugin"))
                 ),
                 goal("copy-resources"),
                 configuration(
                         element(name("encoding"), "UTF-8"),
                         element(name("resources"),
                                 element(name("resource"),
                                         element(name("directory"), "src/main/resources"),
                                         element(name("filtering"), "true"),
                                         element(name("includes"),
                                                 element(name("include"), "atlassian-plugin.xml"))
                                 )
                         ),
                         element(name("outputDirectory"), "${project.build.outputDirectory}")
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void runUnitTests() throws MojoExecutionException
     {
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-surefire-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-surefire-plugin"))
                 ),
                 goal("test"),
                 configuration(
                         element(name("excludes"),
                                 element(name("exclude"), "it/**"),
                                 element(name("exclude"), "**/*$*"))
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public File copyWebappWar(final String productId, final File targetDirectory, final ProductArtifact artifact)
             throws MojoExecutionException
     {
         final File webappWarFile = new File(targetDirectory, productId + "-original.war");
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-dependency-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-dependency-plugin"))
                 ),
                 goal("copy"),
                 configuration(
                         element(name("artifactItems"),
                                 element(name("artifactItem"),
                                         element(name("groupId"), artifact.getGroupId()),
                                         element(name("artifactId"), artifact.getArtifactId()),
                                         element(name("type"), "war"),
                                         element(name("version"), artifact.getVersion()),
                                         element(name("destFileName"), webappWarFile.getName()))),
                         element(name("outputDirectory"), targetDirectory.getPath())
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
         return webappWarFile;
     }
 
     /**
      * Copies {@code artifacts} to the {@code outputDirectory}. Artifacts are looked up in order: <ol> <li>in the maven
      * reactor</li> <li>in the maven repositories</li> </ol> This can't be used in a goal that happens before the
      * <em>package</em> phase as artifacts in the reactor will be not be packaged (and therefore 'copiable') until this
      * phase.
      *
      * @param outputDirectory the directory to copy artifacts to
      * @param artifacts       the list of artifact to copy to the given directory
      */
     public void copyPlugins(final File outputDirectory, final List<ProductArtifact> artifacts)
             throws MojoExecutionException
     {
         for (ProductArtifact artifact : artifacts)
         {
             final MavenProject artifactReactorProject = getReactorProjectForArtifact(artifact);
             if (artifactReactorProject != null)
             {
 
                 log.debug(artifact + " will be copied from reactor project " + artifactReactorProject);
                 final File artifactFile = artifactReactorProject.getArtifact().getFile();
                 if (artifactFile == null)
                 {
                     log.warn("The plugin " + artifact + " is in the reactor but not the file hasn't been attached.  Skipping.");
                 }
                 else
                 {
                     log.debug("Copying " + artifactFile + " to " + outputDirectory);
                     try
                     {
                         FileUtils.copyFile(artifactFile, new File(outputDirectory, artifactFile.getName()));
                     }
                     catch (IOException e)
                     {
                         throw new MojoExecutionException("Could not copy " + artifact + " to " + outputDirectory, e);
                     }
                 }
 
             }
             else
             {
                 executeMojo(
                         plugin(
                                 groupId("org.apache.maven.plugins"),
                                 artifactId("maven-dependency-plugin"),
                                 version(defaultArtifactIdToVersionMap.get("maven-dependency-plugin"))
                         ),
                         goal("copy"),
                         configuration(
                                 element(name("artifactItems"),
                                         element(name("artifactItem"),
                                                 element(name("groupId"), artifact.getGroupId()),
                                                 element(name("artifactId"), artifact.getArtifactId()),
                                                 element(name("version"), artifact.getVersion()))),
                                 element(name("outputDirectory"), outputDirectory.getPath())
                         ),
                         executionEnvironment(project, session, pluginManager));
             }
         }
     }
 
     private MavenProject getReactorProjectForArtifact(ProductArtifact artifact)
     {
         for (final MavenProject project : reactor)
         {
             if (project.getGroupId().equals(artifact.getGroupId())
                     && project.getArtifactId().equals(artifact.getArtifactId())
                     && project.getVersion().equals(artifact.getVersion()))
             {
                 return project;
             }
         }
         return null;
     }
 
     private void unpackContainer(final Container container) throws MojoExecutionException
     {
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-dependency-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-dependency-plugin"))
                 ),
                 goal("unpack"),
                 configuration(
                         element(name("artifactItems"),
                                 element(name("artifactItem"),
                                         element(name("groupId"), container.getGroupId()),
                                         element(name("artifactId"), container.getArtifactId()),
                                         element(name("version"), container.getVersion()),
                                         element(name("type"), "zip"))),
                         element(name("outputDirectory"), container.getRootDirectory(getBuildDirectory()))
                 ),
                 executionEnvironment(project, session, pluginManager));
     }
 
     private String getBuildDirectory()
     {
         return project.getBuild().getDirectory();
     }
 
     public int startWebapp(final String productInstanceId, final File war, final Map<String, String> systemProperties, final List<ProductArtifact> extraContainerDependencies,
                            final Product webappContext) throws MojoExecutionException
     {
         final Container container = findContainer(webappContext.getContainerId());
         File containerDir = new File(container.getRootDirectory(getBuildDirectory()));
 
         // retrieve non-embedded containers
         if (!container.isEmbedded())
         {
             if (containerDir.exists())
             {
                 log.info("Reusing unpacked container '" + container.getId() + "' from " + containerDir.getPath());
             }
             else
             {
                 log.info("Unpacking container '" + container.getId() + "' from container artifact: " + container.toString());
                 unpackContainer(container);
             }
         }
 
         final int rmiPort = pickFreePort(0);
         final int actualHttpPort = pickFreePort(webappContext.getHttpPort());
         final List<Element> sysProps = new ArrayList<Element>();
         if (webappContext.getJvmArgs() == null)
         {
             webappContext.setJvmArgs("-Xmx512m -XX:MaxPermSize=160m");
         }
 
         for (final Map.Entry<String, String> entry : systemProperties.entrySet())
         {
             webappContext.setJvmArgs(webappContext.getJvmArgs() + " -D" + entry.getKey() + "=\"" + entry.getValue() + "\"");
             sysProps.add(element(name(entry.getKey()), entry.getValue()));
         }
         log.info("Starting " + productInstanceId + " on the " + container.getId() + " container on ports "
                 + actualHttpPort + " (http) and " + rmiPort + " (rmi)");
 
         final String baseUrl = getBaseUrl(webappContext.getServer(), actualHttpPort, webappContext.getContextPath());
         sysProps.add(element(name("baseurl"), baseUrl));
 
         final List<Element> deps = new ArrayList<Element>();
         for (final ProductArtifact dep : extraContainerDependencies)
 
         {
             deps.add(element(name("dependency"),
                     element(name("location"), webappContext.getArtifactRetriever().resolve(dep))
             ));
         }
 
         final List<Element> props = new ArrayList<Element>();
         for (final Map.Entry<String, String> entry : systemProperties.entrySet())
         {
             props.add(element(name(entry.getKey()), entry.getValue()));
         }
         props.add(element(name("cargo.servlet.port"), String.valueOf(actualHttpPort)));
         props.add(element(name("cargo.rmi.port"), String.valueOf(rmiPort)));
         props.add(element(name("cargo.jvmargs"), webappContext.getJvmArgs()));
 
         executeMojo(
                 plugin(
                         groupId("org.twdata.maven"),
                         artifactId("cargo-maven2-plugin"),
                         version(pluginArtifactIdToVersionMap.get("cargo-maven2-plugin"))
                 ),
                 goal("start"),
                 configuration(
                         element(name("wait"), "false"),
                         element(name("container"),
                                 element(name("containerId"), container.getId()),
                                 element(name("type"), container.getType()),
                                 element(name("home"), container.getInstallDirectory(getBuildDirectory())),
                                 element(name("output"), webappContext.getOutput()),
                                 element(name("systemProperties"), sysProps.toArray(new Element[sysProps.size()])),
                                 element(name("dependencies"), deps.toArray(new Element[deps.size()]))
                         ),
                         element(name("configuration"),
                                 element(name("home"), container.getConfigDirectory(getBuildDirectory(), productInstanceId)),
                                 element(name("type"), "standalone"),
                                 element(name("properties"), props.toArray(new Element[props.size()])),
                                 element(name("deployables"),
                                         element(name("deployable"),
                                                 element(name("groupId"), "foo"),
                                                 element(name("artifactId"), "bar"),
                                                 element(name("type"), "war"),
                                                 element(name("location"), war.getPath()),
                                                 element(name("properties"),
                                                         element(name("context"), webappContext.getContextPath())
                                                 )
                                         )
                                 )
                         )
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
         return actualHttpPort;
     }
 
     static String getBaseUrl(final String server, final int actualHttpPort, final String contextPath)
     {
         return "http://" + server + ":" + actualHttpPort + contextPath;
     }
 
     public void runTests(String productId, String containerId, List<String> includes, List<String> excludes, Map<String, Object> systemProperties, final File targetDirectory)
     		throws MojoExecutionException
 	{
     	List<Element> includeElements = new ArrayList<Element>(includes.size());
     	for (String include : includes)
     	{
     		includeElements.add(element(name("include"), include));
     	}
 
         List<Element> excludeElements = new ArrayList<Element>(excludes.size() + 2);
         excludeElements.add(element(name("exclude"), "**/*$*"));
         excludeElements.add(element(name("exclude"), "**/Abstract*"));
         for (String exclude : excludes)
         {
         	excludeElements.add(element(name("exclude"), exclude));
         }
 
         final String testOutputDir = targetDirectory.getAbsolutePath() + "/" + productId + "/" + containerId + "/surefire-reports";
         final String reportsDirectory = "reportsDirectory";
         systemProperties.put(reportsDirectory, testOutputDir);
         
         final Element systemProps = convertPropsToElements(systemProperties);
 
 
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-surefire-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-surefire-plugin"))
                 ),
                 goal("test"),
                 configuration(
                         element(name("includes"),
                         		includeElements.toArray(new Element[includeElements.size()])
                         ),
                         element(name("excludes"),
                                 excludeElements.toArray(new Element[excludeElements.size()])
                         ),
                         systemProps,
                         element(name(reportsDirectory), testOutputDir)
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
 	}
     
     /**
      * Converts a map of System properties to maven config elements
      */
     private Element convertPropsToElements(Map<String, Object> systemProperties)
     {
         ArrayList<Element> properties = new ArrayList<Element>();
 
         // add extra system properties... overwriting any of the hard coded values above.
         for (Map.Entry<String, Object> entry: systemProperties.entrySet())
         {
             properties.add(
                     element(name("property"),
                             element(name("name"), entry.getKey()),
                             element(name("value"), entry.getValue().toString())));
         }
 
         return element(name("systemProperties"), properties.toArray(new Element[properties.size()]));
     }
 
     private Container findContainer(final String containerId)
     {
         final Container container = idToContainerMap.get(containerId);
         if (container == null)
         {
             throw new IllegalArgumentException("Container " + containerId + " not supported");
         }
         return container;
     }
 
     int pickFreePort(final int requestedPort)
     {
         ServerSocket socket = null;
         try
         {
             socket = new ServerSocket(requestedPort);
             return requestedPort > 0 ? requestedPort : socket.getLocalPort();
         }
         catch (final IOException e)
         {
             // happens if the requested port is taken, so we need to pick a new one
             ServerSocket zeroSocket = null;
             try
             {
                 zeroSocket = new ServerSocket(0);
                 return zeroSocket.getLocalPort();
             }
             catch (final IOException ex)
             {
                 throw new RuntimeException("Error opening socket", ex);
             }
             finally
             {
                 closeSocket(zeroSocket);
             }
         }
         finally
         {
             closeSocket(socket);
         }
     }
 
     private void closeSocket(ServerSocket socket)
     {
         if (socket != null)
         {
             try
             {
                 socket.close();
             }
             catch (final IOException e)
             {
                 throw new RuntimeException("Error closing socket", e);
             }
         }
     }
 
     public void stopWebapp(final String productId, final String containerId) throws MojoExecutionException
     {
         final Container container = findContainer(containerId);
         executeMojo(
                 plugin(
                         groupId("org.twdata.maven"),
                         artifactId("cargo-maven2-plugin"),
                         version(pluginArtifactIdToVersionMap.get("cargo-maven2-plugin"))
                 ),
                 goal("stop"),
                 configuration(
                         element(name("container"),
                                 element(name("containerId"), container.getId()),
                                 element(name("type"), container.getType())
                         ),
                         element(name("configuration"),
                                 element(name("home"), container.getConfigDirectory(getBuildDirectory(), productId))
                         )
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void installPlugin(PdkParams pdkParams)
             throws MojoExecutionException
     {
         final String baseUrl = getBaseUrl(pdkParams.getServer(), pdkParams.getPort(), pdkParams.getContextPath());
         executeMojo(
                 plugin(
                         groupId("com.atlassian.maven.plugins"),
                         artifactId("atlassian-pdk"),
                         version(pluginArtifactIdToVersionMap.get("atlassian-pdk"))
                 ),
                 goal("install"),
                 configuration(
                         element(name("pluginFile"), pdkParams.getPluginFile()),
                         element(name("username"), pdkParams.getUsername()),
                         element(name("password"), pdkParams.getPassword()),
                         element(name("serverUrl"), baseUrl),
                         element(name("pluginKey"), pdkParams.getPluginKey())
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void uninstallPlugin(final String pluginKey, final String server, final int port, final String contextPath)
             throws MojoExecutionException
     {
         final String baseUrl = getBaseUrl(server, port, contextPath);
         executeMojo(
                 plugin(
                         groupId("com.atlassian.maven.plugins"),
                         artifactId("atlassian-pdk"),
                         version(pluginArtifactIdToVersionMap.get("atlassian-pdk"))
                 ),
                 goal("uninstall"),
                 configuration(
                         element(name("username"), "admin"),
                         element(name("password"), "admin"),
                         element(name("serverUrl"), baseUrl),
                         element(name("pluginKey"), pluginKey)
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void installIdeaPlugin() throws MojoExecutionException
     {
         executeMojo(
                 plugin(
                         groupId("org.twdata.maven"),
                         artifactId("maven-cli-plugin"),
                         version(pluginArtifactIdToVersionMap.get("maven-cli-plugin"))
                 ),
                 goal("idea"),
                 configuration(),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public File copyDist(final File targetDirectory, final ProductArtifact artifact) throws MojoExecutionException
     {
         return copyZip(targetDirectory, artifact, "test-dist.zip");
     }
 
     public File copyHome(final File targetDirectory, final ProductArtifact artifact) throws MojoExecutionException
     {
         return copyZip(targetDirectory, artifact, "test-resources.zip");
     }
 
     public File copyZip(final File targetDirectory, final ProductArtifact artifact, final String localName) throws MojoExecutionException
     {
         final File artifactZip = new File(targetDirectory, localName);
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-dependency-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-dependency-plugin"))
                 ),
                 goal("copy"),
                 configuration(
                         element(name("artifactItems"),
                                 element(name("artifactItem"),
                                         element(name("groupId"), artifact.getGroupId()),
                                         element(name("artifactId"), artifact.getArtifactId()),
                                         element(name("type"), "zip"),
                                         element(name("version"), artifact.getVersion()),
                                         element(name("destFileName"), artifactZip.getName()))),
                         element(name("outputDirectory"), artifactZip.getParent())
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
         return artifactZip;
     }
 
     public void generateManifest(final Map<String, String> instructions) throws MojoExecutionException
     {
         final List<Element> instlist = new ArrayList<Element>();
         for (final Map.Entry<String, String> entry : instructions.entrySet())
         {
             instlist.add(element(entry.getKey(), entry.getValue()));
         }
         executeMojo(
                 plugin(
                         groupId("org.apache.felix"),
                         artifactId("maven-bundle-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-bundle-plugin"))
                 ),
                 goal("manifest"),
                 configuration(
                         element(name("supportedProjectTypes"),
                                 element(name("supportedProjectType"), "jar"),
                                 element(name("supportedProjectType"), "bundle"),
                                 element(name("supportedProjectType"), "war"),
                                 element(name("supportedProjectType"), "atlassian-plugin")),
                         element(name("instructions"), instlist.toArray(new Element[instlist.size()]))
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void jarWithOptionalManifest(final boolean manifestExists) throws MojoExecutionException
     {
         Element[] archive = new Element[0];
         if (manifestExists)
         {
             archive = new Element[]{element(name("manifestFile"), "${project.build.outputDirectory}/META-INF/MANIFEST.MF")};
         }
 
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-jar-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-jar-plugin"))
                 ),
                 goal("jar"),
                 configuration(
                         element(name("archive"), archive)
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void jarTests(String finalName) throws MojoExecutionException
     {
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-jar-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-jar-plugin"))
                 ),
                 goal("test-jar"),
                 configuration(
                         element(name("finalName"), finalName),
                         element(name("archive"),
                             element(name("manifestFile"), "${project.build.testOutputDirectory}/META-INF/MANIFEST.MF"))
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void generateObrXml(File dep, File obrXml) throws MojoExecutionException
     {
         executeMojo(
                 plugin(
                         groupId("org.apache.felix"),
                         artifactId("maven-bundle-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-bundle-plugin"))
                 ),
                 goal("install-file"),
                 configuration(
                         element(name("obrRepository"), obrXml.getPath()),
 
                         // the following three settings are required but not really used
                         element(name("groupId"), "doesntmatter"),
                         element(name("artifactId"), "doesntmatter"),
                         element(name("version"), "doesntmatter"),
 
                         element(name("packaging"), "jar"),
                         element(name("file"), dep.getPath())
 
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     /**
      * Copies and creates a zip file of the previous run's home directory minus any installed plugins.
      *
      * @param homeDirectory The path to the previous run's home directory.
      * @param targetZip     The path to the final zip file.
      * @param productId     The name of the product.
      *
      * @since 3.1-m3
      */
     public void createHomeResourcesZip(final File homeDirectory, final File targetZip, final String productId)
     {
         if (homeDirectory == null || !homeDirectory.exists())
         {
             String homePath = "null";
             if(homeDirectory != null) {
                 homePath = homeDirectory.getAbsolutePath();
             }
             log.info("home directory doesn't exist, skipping. [" + homePath + "]");
             return;
         }
 
         final File appDir = new File(project.getBuild().getDirectory(), productId);
         final File tmpDir = new File(appDir, "tmp-resources");
         final File genDir = new File(tmpDir, "generated-home");
         final String entryBase = "generated-resources/" + productId + "-home";
 
         if (genDir.exists())
         {
             FileUtils.deleteDir(genDir);
         }
 
         genDir.mkdirs();
 
         try
         {
             FileUtils.copyDirectory(homeDirectory, genDir, true);
 
             //we want to get rid of the plugins folders.
             File homePlugins = new File(genDir, "plugins");
             File bundledPlugins = new File(genDir, "bundled-plugins");
 
             if (homePlugins.exists())
             {
                 FileUtils.deleteDir(homePlugins);
             }
 
             if (bundledPlugins.exists())
             {
                 FileUtils.deleteDir(bundledPlugins);
             }
 
            ZipUtils.zipDir(targetZip, tmpDir.listFiles()[0], entryBase);
         } catch (IOException e)
         {
             throw new RuntimeException("Error zipping home directory", e);
         }
 
 
     }
 
     private static class Container extends ProductArtifact
     {
         private final String id;
         private final String type;
 
         /**
          * Installable container that can be downloaded by Maven.
          *
          * @param id         identifier of container, eg. "tomcat5x".
          * @param groupId    groupId of container.
          * @param artifactId artifactId of container.
          * @param version    version number of container.
          */
         public Container(final String id, final String groupId, final String artifactId, final String version)
         {
             super(groupId, artifactId, version);
             this.id = id;
             this.type = "installed";
         }
 
         /**
          * Embedded container packaged with Cargo.
          *
          * @param id identifier of container, eg. "jetty6x".
          */
         public Container(final String id)
         {
             this.id = id;
             this.type = "embedded";
         }
 
         /**
          * @return identifier of container.
          */
         public String getId()
         {
             return id;
         }
 
         /**
          * @return "installed" or "embedded".
          */
         public String getType()
         {
             return type;
         }
 
         /**
          * @return <code>true</code> if the container type is "embedded".
          */
         public boolean isEmbedded()
         {
             return "embedded".equals(type);
         }
 
         /**
          * @param buildDir project.build.directory.
          * @return root directory of the container that will house the container installation and configuration.
          */
         public String getRootDirectory(String buildDir)
         {
             return buildDir + File.separator + "container" + File.separator + getId();
         }
 
         /**
          * @param buildDir project.build.directory.
          * @return directory housing the installed container.
          */
         public String getInstallDirectory(String buildDir)
         {
             return getRootDirectory(buildDir) + File.separator + getArtifactId() + "-" + getVersion();
         }
 
         /**
          * @param buildDir  project.build.directory.
          * @param productId product name.
          * @return directory to house the container configuration for the specified product.
          */
         public String getConfigDirectory(String buildDir, String productId)
         {
             return getRootDirectory(buildDir) + File.separator + "cargo-" + productId + "-home";
         }
     }
 }
