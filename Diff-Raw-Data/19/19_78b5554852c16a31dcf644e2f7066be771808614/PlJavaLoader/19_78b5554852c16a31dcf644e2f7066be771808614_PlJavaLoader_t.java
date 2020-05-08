 /*
  * Copyright (c) 2009, 2011 Hal Hildebrand, all rights reserved.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.hellblazer.pljava.loader;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 /**
  * @author hhildebrand
  * 
  * @goal load
  * 
  * @phase package
  * 
  * @requiresDependencyResolution runtime
  */
 public class PlJavaLoader extends AbstractMojo {
     public static class Resource {
         String artifactId;
         String classifier;
         String groupId;
         String type = "jar";
         String version;
     }
 
     /**
      * The list of additional artifacts to load
      * 
      * @parameter
      */
     private List<Resource>             additional = new ArrayList<Resource>();
 
     /**
      * The property name to recieve the generated class path
      * 
      * @parameter
      */
     private String                     classpathProperty;
 
     /**
      * The list of artifacts to load
      * 
      * @parameter
      */
     private List<Resource>             excluded   = new ArrayList<Resource>();
 
     /**
      * The JDBC URL used to connect to the database
      * 
      * @parameter
      * @required
      */
     private String                     jdbcUrl;
 
     /**
      * The password to use when logging into the database
      * 
      * @parameter
      * @required
      */
     private String                     password;
 
     /**
      * @parameter expression="${project}"
      */
     private MavenProject               project;
 
     /**
      * The username to use when logging into the database
      * 
      * @parameter
      * @required
      */
     private String                     username;
 
     /**
      * Used to look up Artifacts in the remote repository.
      * 
      * @parameter expression=
      *            "${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
      * @required
      * @readonly
      */
     protected ArtifactResolver         artifactResolver;
 
     /**
      * Used to look up Artifacts in the remote repository.
      * 
      * @parameter expression=
      *            "${component.org.apache.maven.artifact.factory.ArtifactFactory}"
      * @required
      * @readonly
      */
     protected ArtifactFactory          factory;
 
     /**
      * Location of the local repository.
      * 
      * @parameter expression="${localRepository}"
      * @readonly
      * @required
      */
     protected ArtifactRepository       localRepository;
 
     /**
      * List of Remote Repositories used by the resolver
      * 
      * @parameter expression="${project.remoteArtifactRepositories}"
      * @readonly
      * @required
      */
     protected List<ArtifactRepository> remoteRepositories;
 
     /* (non-Javadoc)
      * @see org.apache.maven.plugin.Mojo#execute()
      */
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         try {
             load();
         } catch (SQLException e) {
             MojoFailureException ex = new MojoFailureException(
                                                                "Unable to load jars");
             ex.initCause(e);
             throw ex;
         } catch (IOException e) {
             MojoFailureException ex = new MojoFailureException(
                                                                "Unable to load jars");
             ex.initCause(e);
             throw ex;
         }
     }
 
     private void add(File base, File source, JarOutputStream jos)
                                                                  throws IOException {
         int baseIndex = base.getAbsolutePath().length() + 1;
         BufferedInputStream in = null;
         try {
             String relative = "";
             String normalized = source.getPath().replace("\\", "/");
             if (normalized.length() >= baseIndex) {
                 relative = normalized.substring(baseIndex);
             }
 
             if (source.isDirectory()) {
                 String name = relative;
                 if (!name.isEmpty()) {
                     if (!name.endsWith("/")) {
                         name += "/";
                     }
                     JarEntry entry = new JarEntry(name);
                     entry.setTime(source.lastModified());
                     jos.putNextEntry(entry);
                     jos.closeEntry();
                 }
                 for (File nestedFile : source.listFiles()) {
                     add(base, nestedFile, jos);
                 }
                 return;
             }
 
             JarEntry entry = new JarEntry(relative);
             entry.setTime(source.lastModified());
             jos.putNextEntry(entry);
             in = new BufferedInputStream(new FileInputStream(source));
 
             byte[] buffer = new byte[1024];
             while (true) {
                 int count = in.read(buffer);
                 if (count == -1) {
                     break;
                 }
                 jos.write(buffer, 0, count);
             }
             jos.closeEntry();
         } finally {
             if (in != null) {
                 in.close();
             }
         }
     }
 
     private void drop(PreparedStatement drop, String name,
                       PreparedStatement validate) throws SQLException,
                                                  MojoExecutionException {
         validate.setString(1, name);
         ResultSet result = validate.executeQuery();
         if (result.next()) {
            getLog().info(String.format("dropping jar %s", name));
            drop.setString(1, name);
            drop.setBoolean(2, false);
            drop.execute();
            result = validate.executeQuery();
            if (result.next()) {
                throw new MojoExecutionException(
                                                 String.format("Did not actually drop %s",
                                                               name));
            }
         }
     }
 
     /**
      * @param artifact
      * @return
      */
     private String generateName(org.apache.maven.artifact.Artifact artifact) {
         return String.format("%s_%s", sanitize(artifact.getArtifactId()),
                              sanitize(artifact.getVersion()));
     }
 
     private String sanitize(String string) {
         return string.replace('-', '_').replace('.', '_');
     }
 
     /**
      * @param file
      * @return
      * @throws IOException
      */
     private byte[] getBytes(File file) throws IOException {
         if (file.isDirectory()) {
             // We need to create the Jarfile first.
             return getDirBytes(file);
         }
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         InputStream is = new FileInputStream(file);
         try {
             byte[] buffer = new byte[16 * 1024];
             for (int read = is.read(buffer); read != -1; read = is.read(buffer)) {
                 baos.write(buffer, 0, read);
             }
         } finally {
             is.close();
         }
         return baos.toByteArray();
     }
 
     /**
      * Jar up the directory and return its byte array
      * 
      * @param file
      * @return
      * @throws IOException
      */
     private byte[] getDirBytes(File directory) throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(200 * 1024);
         JarOutputStream jos = new JarOutputStream(baos);
         add(directory, directory, jos);
         jos.close();
         return baos.toByteArray();
     }
 
     /**
      * @throws SQLException
      * @throws IOException
      * @throws MojoFailureException
      * @throws MojoExecutionException
      */
     private void load() throws SQLException, IOException, MojoFailureException,
                        MojoExecutionException {
         try {
             getLog().info(String.format("using driver %s, url: %s",
                                         Class.forName("org.postgresql.Driver"),
                                         jdbcUrl));
         } catch (ClassNotFoundException e) {
             throw new MojoFailureException("Unable to find the postgres driver");
         }
         Connection connection = DriverManager.getConnection(jdbcUrl, username,
                                                             password);
         connection.setAutoCommit(true);
         PreparedStatement drop = connection.prepareStatement("SELECT sqlj.remove_jar(?, ?)");
         PreparedStatement load = connection.prepareStatement("SELECT sqlj.install_jar(?, ?, ?)");
         PreparedStatement validate = connection.prepareStatement("SELECT jarid from sqlj.jar_repository where jarname=?");
 
         @SuppressWarnings("unchecked")
         Set<Artifact> artifacts = project.getArtifacts();
 
         artifacts.add(project.getArtifact());
 
         for (Resource exclusion : excluded) {
             artifacts.remove(resolveArtifact(exclusion));
         }
         for (Resource add : additional) {
             artifacts.add(resolveArtifact(add));
         }
         String classpath = null;
         for (Artifact artifact : artifacts) {
             String name = generateName(artifact);
             drop(drop, name, validate);
             getLog().info(String.format("loading artifact %s, name: %s file: %s",
                                         artifact, name, artifact.getFile()));
             load(load, artifact.getFile(), name);
             if (classpath == null) {
                 classpath = name;
             } else {
                 classpath += ":" + name;
             }
         }
         if (classpathProperty != null) {
             project.getProperties().put(classpathProperty, classpath);
         }
     }
 
     private void load(PreparedStatement load, File file, String name)
                                                                      throws IOException,
                                                                      SQLException,
                                                                      MojoFailureException {
         byte[] bytes = getBytes(file);
         load.setBytes(1, bytes);
         load.setString(2, name);
         load.setBoolean(3, true);
         load.execute();
     }
 
     protected Artifact resolveArtifact(Resource exclusion) {
         try {
             Artifact artifact = factory.createArtifactWithClassifier(exclusion.groupId,
                                                                      exclusion.artifactId,
                                                                      exclusion.version,
                                                                      exclusion.type,
                                                                      exclusion.classifier);
 
             artifactResolver.resolve(artifact, remoteRepositories,
                                      localRepository);
             return artifact;
         } catch (ArtifactResolutionException e) {
             getLog().error(String.format("error resolving %s", exclusion), e);
         } catch (ArtifactNotFoundException e) {
             getLog().error(String.format("can't resolve %s", exclusion), e);
         }
         return null;
     }
 }
