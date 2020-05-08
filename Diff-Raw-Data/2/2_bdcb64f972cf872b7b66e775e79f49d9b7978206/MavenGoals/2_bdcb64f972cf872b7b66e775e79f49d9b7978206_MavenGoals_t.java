 package com.atlassian.maven.plugins.amps;
 
 import com.atlassian.core.util.FileUtils;
 import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
 import com.atlassian.maven.plugins.amps.util.VersionUtils;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.PluginManager;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.version;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
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
             put("tomcat5x", new Container("tomcat5x", "https://m2proxy.atlassian.com/repository/public/org/apache/tomcat/apache-tomcat/5.5.25/apache-tomcat-5.5.25.zip"));
             put("tomcat6x", new Container("tomcat6x", "http://apache.mirror.aussiehq.net.au/tomcat/tomcat-6/v6.0.18/bin/apache-tomcat-6.0.18.zip"));
             put("resin3x", new Container("resin3x", "http://www.caucho.com/download/resin-3.0.26.zip"));
             put("jboss42x", new Container("jboss42x", "http://internode.dl.sourceforge.net/sourceforge/jboss/jboss-4.2.3.GA.zip"));
             put("jetty6x", new Container("jetty6x"));
 
         }};
 
     private final Map<String, String> defaultArtifactIdToVersionMap = new HashMap<String, String>()
     {{
            put("maven-cli-plugin", "0.6.9");
             put("cargo-maven2-plugin", "1.0-beta-2-db2");
             put("atlassian-pdk", "2.1.6");
             put("maven-archetype-plugin", "2.0-alpha-4");
             put("maven-bundle-plugin", "2.0.0");
 
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
                         .append("com.atlassian.maven.plugins:maven-" + pluginId + "-plugin:filter-plugin-descriptor").append(" ")
                         .append("compile").append(" ")
                         .append("com.atlassian.maven.plugins:maven-" + pluginId + "-plugin:copy-bundled-dependencies").append(" ")
                         .append("com.atlassian.maven.plugins:maven-" + pluginId + "-plugin:generate-manifest").append(" ")
                         .append("com.atlassian.maven.plugins:maven-" + pluginId + "-plugin:validate-manifest").append(" ")
                         .append("com.atlassian.maven.plugins:maven-" + pluginId + "-plugin:jar").append(" ")
                         .append("com.atlassian.maven.plugins:maven-" + pluginId + "-plugin:install").toString()),
                 element(name("pu"), new StringBuilder()
                         .append("com.atlassian.maven.plugins:maven-" + pluginId + "-plugin:uninstall").toString())));
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
                 executionEnvironment(project, session, pluginManager)
         );
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
                         element(name("archetypeRepository"), "http://maven.atlassian.com/public"),
                         element(name("archetypeGroupId"), "com.atlassian.maven.archetypes"),
                         element(name("archetypeArtifactId"), productId + "-plugin-archetype"),
                         element(name("archetypeVersion"), VersionUtils.getVersion())
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
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
                         element(name("type"), "jar"),
                         element(name("outputDirectory"), "${project.build.outputDirectory}/META-INF/lib")
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
      * @param artifacts the list of artifact to copy to the given directory
      */
     public void copyPlugins(final File outputDirectory, final List<ProductArtifact> artifacts)
             throws MojoExecutionException
     {
         for (ProductArtifact artifact : artifacts)
         {
             final MavenProject artifactReactorProject = getReactorProjectForArtifact(artifact);
             if (artifactReactorProject != null)
             {
                 try
                 {
                     log.debug(artifact + " will be copied from reactor project " + artifactReactorProject);
                     final File artifactFile = artifactReactorProject.getArtifact().getFile();
                     log.debug("Copying " + artifactFile + " to " + outputDirectory);
                     FileUtils.copyFile(artifactFile, new File(outputDirectory, artifactFile.getName()));
                 }
                 catch (IOException e)
                 {
                     throw new MojoExecutionException("Could not copy " + artifact + " to " + outputDirectory, e);
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
 
     public int startWebapp(final String productId, final File war, final Map<String, String> systemProperties, final List<ProductArtifact> extraContainerDependencies,
             final Product webappContext) throws MojoExecutionException
     {
         final int rmiPort = pickFreePort(0);
         final int actualHttpPort = pickFreePort(webappContext.getHttpPort());
         final Container container = findContainer(webappContext.getContainerId());
         final List<Element> sysProps = new ArrayList<Element>();
         if (webappContext.getJvmArgs() == null)
         {
             webappContext.setJvmArgs("-Xmx512m -XX:MaxPermSize=160m");
         }
         for (final Map.Entry<String, String> entry : systemProperties.entrySet())
         {
             webappContext.setJvmArgs(webappContext.getJvmArgs() + " -D" + entry.getKey() + "=" + entry.getValue());
             sysProps.add(element(name(entry.getKey()), entry.getValue()));
         }
         log.info("Starting " + productId + " on the " + container.getId() + " container on ports "
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
                                 element(name("zipUrlInstaller"),
                                         element(name("url"), container.getUrl())
                                 ),
                                 element(name("systemProperties"), sysProps.toArray(new Element[sysProps.size()])),
                                 element(name("dependencies"), deps.toArray(new Element[deps.size()]))
                         ),
                         element(name("configuration"),
                                 element(name("home"), "${project.build.directory}/" + productId + "/" + container.getId()),
                                 element(name("properties"), props.toArray(new Element[props.size()])),
                                 element(name("deployables"),
                                         element(name("deployable"),
                                                 element(name("groupId"), "foo"),
                                                 element(name("artifactId"), "bar"),
                                                 element(name("type"), "war"),
                                                 element(name("location"), war.getPath())
                                         )
                                 )
                         )
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
         return actualHttpPort;
     }
 
     private String getBaseUrl(final String server, final int actualHttpPort, final String contextPath)
     {
         return "http://" + server + ":" + actualHttpPort + contextPath;
     }
 
     public void runTests(final String productId, final String containerId, final String functionalTestPattern, final int httpPort, final String contexPath, final String pluginJar)
             throws MojoExecutionException
     {
 
         // Automatically exclude tests for other products
         final List<Element> excludes = new ArrayList<Element>();
         excludes.add(element(name("exclude"), "**/*$*"));
         for (final String type : ProductHandlerFactory.getIds())
         {
             if (!type.equals(productId))
             {
                 excludes.add(element(name("exclude"), "**/" + type + "/**"));
             }
         }
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-surefire-plugin"),
                         version(defaultArtifactIdToVersionMap.get("maven-surefire-plugin"))
                 ),
                 goal("test"),
                 configuration(
                         element(name("includes"),
                                 element(name("include"), functionalTestPattern)
                         ),
                         element(name("excludes"),
                                 excludes.toArray(new Element[excludes.size()])
                         ),
                         element(name("systemProperties"),
                                 element(name("property"),
                                         element(name("name"), "http.port"),
                                         element(name("value"), String.valueOf(httpPort))
                                 ),
                                 element(name("property"),
                                         element(name("name"), "context.path"),
                                         element(name("value"), contexPath)
                                 ),
                                 element(name("property"),
                                         element(name("name"), "plugin.jar"),
                                         element(name("value"), pluginJar)
                                 )
                         ),
                         element(name("reportsDirectory"), "${project.build.directory}/" + productId + "/" + containerId + "/surefire-reports")
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
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
 
     private int pickFreePort(final int requestedPort)
     {
         if (requestedPort > 0)
         {
             return requestedPort;
         }
         ServerSocket socket = null;
         try
         {
             socket = new ServerSocket(0);
             return socket.getLocalPort();
         }
         catch (final IOException e)
         {
             throw new RuntimeException("Error opening socket", e);
         }
         finally
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
                                 element(name("home"), "${project.build.directory}/" + productId + "/" + container.getId())
                         )
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void installPlugin(final String pluginKey, final String server, final int port, final String contextPath, final String username, final String password)
             throws MojoExecutionException
     {
         final String baseUrl = getBaseUrl(server, port, contextPath);
         executeMojo(
                 plugin(
                         groupId("com.atlassian.maven.plugins"),
                         artifactId("atlassian-pdk"),
                         version(pluginArtifactIdToVersionMap.get("atlassian-pdk"))
                 ),
                 goal("install"),
                 configuration(
                         element(name("username"), username),
                         element(name("password"), password),
                         element(name("serverUrl"), baseUrl),
                         element(name("pluginKey"), pluginKey)
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
 
     public File copyHome(final File targetDirectory, final ProductArtifact artifact) throws MojoExecutionException
     {
         final File testResourcesZip = new File(targetDirectory, "test-resources.zip");
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
                                         element(name("destFileName"), testResourcesZip.getName()))),
                         element(name("outputDirectory"), testResourcesZip.getParent())
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
         return testResourcesZip;
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
             archive = new Element[] { element(name("manifestFile"), "${project.build.outputDirectory}/META-INF/MANIFEST.MF") };
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
                         element(name("groupId"), "doesntmatter"),
                         element(name("artifactId"), "doesntmatter"),
                         element(name("version"), "doesntmatter"),
                         element(name("packaging"), "jar"),
                         element(name("file"), dep.getPath())
 
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     private static class Container
     {
         private final String id;
         private final String type;
         private final String url;
 
         public Container(final String id, final String url)
         {
             this.id = id;
             this.type = "installed";
             this.url = url;
         }
 
         public Container(final String id)
         {
             this.id = id;
             this.type = "embedded";
             this.url = null;
         }
 
         public String getId()
         {
             return id;
         }
 
         public String getType()
         {
             return type;
         }
 
         public String getUrl()
         {
             return url;
         }
     }
 }
