 /*
  * Copyright 2010-2013, CloudBees Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.cloudbees.sdk;
 
 import com.cloudbees.api.BeesClientConfiguration;
 import com.cloudbees.sdk.cli.BeesCommand;
 import com.cloudbees.sdk.cli.CLICommand;
 import com.cloudbees.sdk.cli.DirectoryStructure;
 import com.cloudbees.sdk.cli.Verbose;
 import com.cloudbees.sdk.maven.LocalRepositorySetting;
 import com.cloudbees.sdk.maven.MavenRepositorySystemSessionFactory;
 import com.cloudbees.sdk.maven.RepositoryService;
 import com.thoughtworks.xstream.XStream;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.repository.internal.MavenRepositorySystemSession;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.installation.InstallRequest;
 import org.sonatype.aether.repository.LocalRepository;
 import org.sonatype.aether.resolution.ArtifactResult;
 import org.sonatype.aether.resolution.VersionRangeResult;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 import org.sonatype.aether.util.artifact.SubArtifact;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.charset.Charset;
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
 
     @Inject
     MavenRepositorySystemSession session;
 
     @Inject
     RepositorySystem rs;
 
     @Inject
     private DirectoryStructure directoryStructure;
 
     @Inject
     private Verbose verbose;
 
     @Inject
     LocalRepositorySetting localRepositorySetting;
 
     @Inject
     CliMavenRepositorySystemSessionDecorator mavenSessionDecorator;
 
     @Inject
     MavenRepositorySystemSessionFactory mavenRepositorySystemSessionFactory;
 
     @Inject
     Provider<RepositoryService> repo; // needs to be indirect because setForceInstall can potentially reconfigure the session factory after this component is instantiated
 
     public ArtifactInstallFactory() {
     }
 
     /**
      * @deprecated
      *      no need to call this
      */
     public void setBeesClientConfiguration(BeesClientConfiguration beesClientConfiguration) {
     }
 
     /**
      * @deprecated
      *      Use {@link CliMavenRepositorySystemSessionDecorator#setForce(boolean)}.
      */
     public void setForceInstall(boolean force) {
         mavenSessionDecorator.setForce(force);
         session = mavenRepositorySystemSessionFactory.get(); // we need to get a new session
     }
 
     /**
      * @deprecated
      *      Use {@link LocalRepositorySetting#set(LocalRepository)}
      */
     public void setLocalRepository(String repository) {
         localRepositorySetting.set(new LocalRepository(repository));
     }
 
     public VersionRangeResult findVersions(GAV gav) throws Exception {
         return repo.get().resolveVersionRange(gav);
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
 
         rs.install(session, installRequest);
 
         return install(gav);
     }
 
     /**
      * Installs the given artifact and all its transitive dependencies
      */
     public GAV install(GAV gav) throws Exception {
         Artifact a = toArtifact(gav);
         List<ArtifactResult> artifactResults = repo.get().resolveDependencies(gav).getArtifactResults();
 
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
         OutputStreamWriter fos = null;
         try {
             xmlFile.getParentFile().mkdirs();
             FileOutputStream outputStream = new FileOutputStream(xmlFile);
             fos = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
            xStream.toXML(plugin, outputStream);
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
