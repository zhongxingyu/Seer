 package org.maven.maven.plugin.aapt;
 
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
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.android.CommandExecutor;
 import org.apache.maven.android.ExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
 import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
 
 import java.io.*;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.zip.ZipOutputStream;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipEntry;
 
 /**
  * @author Shane Isbell
  * @goal package
  * @phase package
  * @description
  */
 public class AaptPackagerMojo extends AbstractMojo {
 
     /**
      * The maven project.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * @parameter expression="${settings.localRepository}"
      * @required
      */
     private File localRepository;
 
     /**
      * @parameter default-value = "m3-rc20a"
      */
     private String androidVersion;
 
     /**
      * @component
      */
     private ArtifactFactory artifactFactory;
 
     public void execute() throws MojoExecutionException, MojoFailureException {
 
         CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
         executor.setLogger(this.getLog());
         File tmpOutputFile;
         try {
             tmpOutputFile = File.createTempFile("android", "apk");
         } catch (IOException e) {
             throw new MojoExecutionException("", e);
         }
 
         Artifact artifact = artifactFactory.createArtifact("android", "android", androidVersion, "jar", "jar");
         ArtifactRepositoryLayout defaultLayout = new DefaultRepositoryLayout();
 
         File androidJar = new File(localRepository, defaultLayout.pathOf(artifact));
 
         tmpOutputFile.deleteOnExit();
         File outputFile = new File(project.getBasedir(), "target" + File.separator + project.getArtifactId() + "-"
                 + project.getVersion() + ".apk");
         File resourceDirectory = new File(project.getBasedir(), "res");
 
         List<String> commands = new ArrayList<String>();
         commands.add("package");
         commands.add("-f");
         commands.add("-c");
         commands.add("-M");
        commands.add(project.getBasedir().getAbsolutePath() + File.separatorChar + "AndroidManifest.xml");
         if (resourceDirectory.exists()) {
             commands.add("-S");
             commands.add(resourceDirectory.getAbsolutePath());
         }
         commands.add("-I");
         commands.add(androidJar.getAbsolutePath());
         commands.add(tmpOutputFile.getAbsolutePath());
         getLog().info("aapt " + commands.toString());
         try {
            executor.executeCommand("aapt", commands, project.getBasedir(), false);
         } catch (ExecutionException e) {
             throw new MojoExecutionException("", e);
         }
         ZipOutputStream os = null;
         InputStream is = null;
 
         try {
             ZipFile zipFile = new ZipFile(tmpOutputFile);
             os = new ZipOutputStream(new FileOutputStream(outputFile));
 
             for (ZipEntry entry : (List<ZipEntry>) Collections.list(zipFile.entries())) {
                 os.putNextEntry(new ZipEntry(entry.getName()));
                 is = zipFile.getInputStream(entry);
                 byte[] buffer = new byte[1024];
                 int i;
                 while ((i = is.read(buffer)) > 0) {
                     os.write(buffer, 0, i);
                 }
                 is.close();
             }
             os.putNextEntry(new ZipEntry("classes.dex"));
            is = new FileInputStream(project.getBasedir().getAbsolutePath() + File.separatorChar + "target/classes.dex");
             byte[] buffer = new byte[1024];
             int i;
             while ((i = is.read(buffer)) > 0) {
                 os.write(buffer, 0, i);
             }
             is.close();
             os.close();
         } catch (IOException e) {
             throw new MojoExecutionException("", e);
         }
         finally {
             if (os != null) {
                 try {
                     os.close();
                 } catch (IOException e) {
 
                 }
             }
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException e) {
                 }
             }
         }

        project.getArtifact().setFile(outputFile);
     }
 }
