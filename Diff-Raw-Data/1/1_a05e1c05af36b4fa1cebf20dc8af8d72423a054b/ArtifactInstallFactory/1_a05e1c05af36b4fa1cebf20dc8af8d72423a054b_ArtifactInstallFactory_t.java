 package com.cloudbees.sdk;
 
 import com.cloudbees.api.BeesClientConfiguration;
 import com.cloudbees.sdk.cli.BeesCommand;
 import com.cloudbees.sdk.cli.CLICommand;
 import com.cloudbees.sdk.cli.DirectoryStructure;
 import com.google.inject.AbstractModule;
 import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
 import com.thoughtworks.xstream.XStream;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.repository.internal.MavenRepositorySystemSession;
 import org.apache.maven.settings.Server;
 import org.codehaus.plexus.DefaultContainerConfiguration;
 import org.codehaus.plexus.DefaultPlexusContainer;
 import org.codehaus.plexus.PlexusContainerException;
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 import org.jboss.shrinkwrap.resolver.impl.maven.MavenDependencyResolverSettings;
 import org.slf4j.LoggerFactory;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.collection.CollectRequest;
 import org.sonatype.aether.graph.Dependency;
 import org.sonatype.aether.graph.DependencyFilter;
 import org.sonatype.aether.impl.VersionResolver;
 import org.sonatype.aether.installation.InstallRequest;
 import org.sonatype.aether.repository.Authentication;
 import org.sonatype.aether.repository.LocalRepository;
 import org.sonatype.aether.repository.Proxy;
 import org.sonatype.aether.repository.RemoteRepository;
 import org.sonatype.aether.resolution.ArtifactResult;
 import org.sonatype.aether.resolution.DependencyRequest;
 import org.sonatype.aether.resolution.VersionRangeRequest;
 import org.sonatype.aether.resolution.VersionRangeResult;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 import org.sonatype.aether.util.artifact.JavaScopes;
 import org.sonatype.aether.util.artifact.SubArtifact;
 import org.sonatype.aether.util.filter.DependencyFilterUtils;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.FileWriter;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Component that talks to {@link org.apache.maven.repository.internal.MavenRepositorySystemSession}, resolve artifacts,
  * and install.
  * <p/>
  * <p/>
  * This component is stateful.
  *
  * @author Fabian Donze
  */
 public class ArtifactInstallFactory {
     private static final Logger LOGGER = Logger.getLogger(ArtifactInstallFactory.class.getName());
 
     MavenRepositorySystemSession sessionFactory;
 
     RepositorySystem rs;
 
     private LocalRepository localRepository;
 
     @Inject
     private DirectoryStructure directoryStructure;
 
     private BeesClientConfiguration beesClientConfiguration;
 
     public ArtifactInstallFactory() {
         // NettyAsyncHttpProvider prints some INFO-level messages. suppress them
         Logger.getLogger("com.ning.http.client.providers.netty.NettyAsyncHttpProvider").setLevel(Level.WARNING);
         LoggerFactory.getLogger(NettyAsyncHttpProvider.class);
     }
 
     public void setBeesClientConfiguration(BeesClientConfiguration beesClientConfiguration) {
         this.beesClientConfiguration = beesClientConfiguration;
     }
 
     private RepositorySystem getRs() {
         if (rs == null) {
             try {
                 DefaultPlexusContainer boot = new DefaultPlexusContainer(
                         new DefaultContainerConfiguration(),
                         new AbstractModule() {
                             @Override
                             protected void configure() {
                                 bind(VersionResolver.class).to(VersionResolverImpl.class);
                             }
                         }
                 );
                 return boot.lookup(RepositorySystem.class);
             } catch (ComponentLookupException e) {
                 throw new RuntimeException("Unable to lookup component RepositorySystem, cannot establish Aether dependency resolver.", e);
             } catch (PlexusContainerException e) {
                 throw new RuntimeException("Unable to load RepositorySystem component by Plexus, cannot establish Aether dependency resolver.", e);
             }
         }
         return rs;
     }
 
     private MavenRepositorySystemSession getSessionFactory() {
         if (sessionFactory == null) {
             sessionFactory = new MavenRepositorySystemSession();
             LocalRepository localRepo = new LocalRepository(new File(new File(System.getProperty("user.home")), ".m2/repository"));
             sessionFactory.setLocalRepositoryManager(getRs().newLocalRepositoryManager(localRepo));
         }
         return sessionFactory;
     }
 
     private List<RemoteRepository> getRepositories() {
         List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();
         MavenDependencyResolverSettings resolverSettings = new MavenDependencyResolverSettings();
         resolverSettings.setUseMavenCentral(true);
         List<RemoteRepository> repos = resolverSettings.getRemoteRepositories();
         for (RemoteRepository remoteRepository : repos) {
             Server server = resolverSettings.getSettings().getServer(remoteRepository.getId());
             if (server != null) {
                 remoteRepository.setAuthentication(new Authentication(server.getUsername(), server.getPassword(), server.getPrivateKey(), server.getPassphrase()));
             }
             setRemoteRepositoryProxy(remoteRepository);
             repositories.add(remoteRepository);
         }
         RemoteRepository r = new RemoteRepository("cloudbees-public-release", "default", "https://repository-cloudbees.forge.cloudbees.com/public-release/");
         setRemoteRepositoryProxy(r);
         repositories.add(r);
         return repositories;
     }
 
     private void setRemoteRepositoryProxy(RemoteRepository repo) {
         if (beesClientConfiguration != null) {
             if (beesClientConfiguration.getProxyHost() != null && beesClientConfiguration.getProxyPort() > 0) {
                 String proxyType = Proxy.TYPE_HTTP;
                 if (repo.getUrl().startsWith("https"))
                     proxyType = Proxy.TYPE_HTTPS;
                 Proxy proxy = new Proxy(proxyType, beesClientConfiguration.getProxyHost(), beesClientConfiguration.getProxyPort(), null);
                 if (beesClientConfiguration.getProxyUser() != null) {
                     Authentication authentication = new Authentication(beesClientConfiguration.getProxyUser(), beesClientConfiguration.getProxyPassword());
                     proxy.setAuthentication(authentication);
                 }
                 repo.setProxy(proxy);
             }
         }
     }
 
     public void setLocalRepository(String repository) {
         localRepository = new LocalRepository(repository);
     }
 
     public VersionRangeResult findVersions(GAV gav) throws Exception {
         GAV findGAV = new GAV(gav.groupId, gav.artifactId, "[0,)");
         Artifact artifact = new DefaultArtifact( findGAV.toString() );
 
         MavenRepositorySystemSession session = getSessionFactory();
         if (localRepository != null)
             session.setLocalRepositoryManager(getRs().newLocalRepositoryManager(localRepository));
 
         VersionRangeRequest rangeRequest = new VersionRangeRequest();
         rangeRequest.setArtifact(artifact);
         rangeRequest.setRepositories(getRepositories());
 
         VersionRangeResult rangeResult = getRs().resolveVersionRange(session, rangeRequest);
 
         return rangeResult;
     }
 
     public GAV install(GAV gav) throws Exception {
         return install(toArtifact(gav));
     }
 
     /**
      * Installs the given artifact and all its transitive dependencies
      */
     public GAV install(GAV gav, File jar, File pom) throws Exception {
         Artifact jarArtifact = toArtifact(gav);
         jarArtifact = jarArtifact.setFile(jar);
 
         Artifact pomArtifact = new SubArtifact(jarArtifact, "", "pom");
         pomArtifact = pomArtifact.setFile(pom);
 
         InstallRequest installRequest = new InstallRequest();
         installRequest.addArtifact(jarArtifact).addArtifact(pomArtifact);
 
         MavenRepositorySystemSession session = getSessionFactory();
         if (localRepository != null)
             session.setLocalRepositoryManager(getRs().newLocalRepositoryManager(localRepository));
         getRs().install(session, installRequest);
 
         return install(gav);
     }
 
     /**
      * Installs the given artifact and all its transitive dependencies
      */
     private GAV install(Artifact a) throws Exception {
         MavenRepositorySystemSession session = getSessionFactory();
         if (localRepository != null)
             session.setLocalRepositoryManager(getRs().newLocalRepositoryManager(localRepository));
 //        System.out.println("Local repo: " + session.getLocalRepositoryManager().getRepository().getBasedir());
 
         DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
 
         CollectRequest collectRequest = new CollectRequest();
         collectRequest.setRoot(new Dependency(a, JavaScopes.COMPILE));
         collectRequest.setRepositories(getRepositories());
 
         DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);
 
         List<ArtifactResult> artifactResults = getRs().resolveDependencies(session, dependencyRequest).getArtifactResults();
 
         Plugin plugin = new Plugin();
         List<CommandProperties> command = plugin.getProperties();
 
         List<String> jars = plugin.getJars();
         List<URL> urls = new ArrayList<URL>();
         for (ArtifactResult artifactResult : artifactResults) {
             URL artifactURL = artifactResult.getArtifact().getFile().toURI().toURL();
             urls.add(artifactURL);
             jars.add(artifactResult.getArtifact().getFile().getAbsolutePath());
         }
         ClassLoader cl = createClassLoader(urls, getClass().getClassLoader());
 
         for (ArtifactResult artifactResult : artifactResults) {
             if (toString(artifactResult.getArtifact()).equals(toString(a))) {
                 plugin.setArtifact(new GAV(artifactResult.getArtifact().toString()).toString());
  //               System.out.println("Analysing... " + plugin.getArtifact());
 
                 JarFile jarFile = new JarFile(artifactResult.getArtifact().getFile());
                 Enumeration<JarEntry> e = jarFile.entries();
                 while (e.hasMoreElements()) {
                     JarEntry entry = e.nextElement();
                     if (entry.getName().endsWith(".class")) {
                         String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                         Class c = Class.forName(className, false, cl);
                         findCommand(true, command, c);
                     }
                 }
 
             }
         }
 
         XStream xStream = new XStream();
         xStream.processAnnotations(Plugin.class);
         xStream.processAnnotations(CommandProperties.class);
 //        System.out.println(xStream.toXML(plugin));
 
         File xmlFile = new File(directoryStructure.getPluginDir(), a.getArtifactId() + ".bees");
         FileWriter fos = null;
         try {
            xmlFile.getParentFile().mkdirs();
             fos = new FileWriter(xmlFile);
             fos.write(xStream.toXML(plugin));
         } finally {
             IOUtils.closeQuietly(fos);
         }
 
         return new GAV(plugin.getArtifact());
     }
 
     private Artifact toArtifact(GAV gav) {
         return new DefaultArtifact(gav.groupId, gav.artifactId, "jar", gav.version);
     }
 
     private String toString(Artifact a) {
         return a.getGroupId() + ":" + a.getArtifactId();
     }
 
     /**
      * Finds all the commands in the given injector.
      */
     private void findCommand(boolean all, List<CommandProperties> list, Class<?> cmd) {
         if (!cmd.isAnnotationPresent(CLICommand.class))
             return;
 
         CommandProperties commandProperties = new CommandProperties();
         commandProperties.setClassName(cmd.getName());
         commandProperties.setName(cmd.getAnnotation(CLICommand.class).value());
 
         if (cmd.isAnnotationPresent(BeesCommand.class)) {
             BeesCommand beesCommand = cmd.getAnnotation(BeesCommand.class);
 
             if (beesCommand.experimental() && !all)
                 return;
 
             commandProperties.setGroup(beesCommand.group());
             if (beesCommand.description().length() > 0)
                 commandProperties.setDescription(beesCommand.description());
             commandProperties.setPriority(beesCommand.priority());
             if (beesCommand.pattern().length() > 0)
                 commandProperties.setPattern(beesCommand.pattern());
             commandProperties.setExperimental(beesCommand.experimental());
         } else {
             try {
                 commandProperties.setGroup("CLI");
                 commandProperties.setPriority((Integer) BeesCommand.class.getMethod("priority").getDefaultValue());
                 commandProperties.setExperimental(false);
             } catch (NoSuchMethodException e) {
                 LOGGER.log(Level.SEVERE, "Internal error", e);
             }
         }
 
         list.add(commandProperties);
 
     }
 
     /**
      * Creates a classloader from all the artifacts resolved thus far.
      */
     private ClassLoader createClassLoader(List<URL> urls, ClassLoader parent) {
         // if (urls.isEmpty()) return parent;  // nothing to load // this makes it hard to differentiate newly loaded stuff from what's already visible
         return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
     }
 
 
 }
