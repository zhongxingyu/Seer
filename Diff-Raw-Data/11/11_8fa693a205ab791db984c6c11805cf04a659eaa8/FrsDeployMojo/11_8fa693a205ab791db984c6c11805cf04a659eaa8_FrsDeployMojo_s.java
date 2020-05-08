 package com.maestrodev.plugins.collabnet;
 
 /*
  * Copyright 2012 MaestroDev
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
 
 import com.maestrodev.plugins.collabnet.frs.FrsSession;
 import com.maestrodev.plugins.collabnet.frs.Package;
 import com.maestrodev.plugins.collabnet.frs.Release;
 import com.maestrodev.plugins.collabnet.log.Log;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.ArtifactUtils;
 import org.apache.maven.artifact.manager.WagonManager;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.wagon.authentication.AuthenticationInfo;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Deploy the project's artifacts to the CollabNet File Releases section.
  *
  * @goal deploy-to-releases
  * @phase deploy
  */
 @SuppressWarnings("UnusedDeclaration")
 public class FrsDeployMojo
         extends AbstractMojo {
 
     /**
      * The package (product) to deploy the files to. If it can not be found, it will be created, unless
      * <code>{@linkplain #createRelease}</code> is <code>false</code>.
      *
      * @parameter alias="package" default-value="${project.name}"
      * @required
      */
     private String pkg;
 
     /**
      * The name of the release to deploy the files to.  If it can not be found it will be created, unless
      * <code>{@linkplain #createRelease}</code> is <code>false</code>.
      *
      * @parameter default-value="${project.version}"
      * @required
      */
     private String release;
 
     /**
      * The name of the CollabNet TeamForge project to deploy the files to.
      *
      * @parameter
      * @required
      */
     private String project;
 
     /**
      * The URL of the TeamForge instance to deploy to.
      *
      * @parameter
      * @required
      */
     private String teamForgeUrl;
 
     /**
      * The server ID in <code>settings.xml</code> for retrieving the username and password from.
      *
      * @parameter default-value="teamforge"
      */
     private String teamForgeServerId;
 
     /**
      * The username to login to TeamForge with. Required if <code>{@linkplain #teamForgeServerId}</code> is not specified.
      *
      * @parameter
      */
     private String teamForgeUsername;
 
     /**
      * The password to login to TeamForge with. Required if <code>{@linkplain #teamForgeServerId}</code> is not specified.
      *
      * @parameter
      */
     private String teamForgePassword;
 
     /**
      * Whether to create the release on CollabNet if it does not already exist.
      *
      * @parameter default-value="true"
      * @required
      */
     private boolean createRelease;
 
     /**
      * The description to assign to the package, if it is created.
      *
      * @parameter default-value="${project.description}"
      */
     private String packageDescription;
 
     /**
      * The description to assign to the release, if it is created.
      *
      * @parameter
      */
     private String releaseDescription;
 
     /**
      * The status to assign to the release, if it is created. The default is to set it to <code>active</code> if the
      * project version is a release, and <code>pending</code> if the project version is a snapshot.
      *
      * @parameter
      */
     private String releaseStatus;
 
     /**
      * The maturity to assign to the release, if it is created.
      *
      * @parameter default-value=""
      */
     private String releaseMaturity = ""; // empty string is required, as default-value="" gives null
 
     /**
      * Whether to overwrite the file if it already exists. The default is <code>true</code> if the project version is a
      * snapshot and <code>false</code> if it is a release.
      *
      * @parameter
      */
     private Boolean overwrite;
 
     // ----------------
     // Maven components
     // ----------------
 
     /**
      * @component
      */
     private WagonManager wagonManager;
 
     /**
      * @parameter expression="${project.artifact}"
      * @readonly
      * @required
      */
     private Artifact projectArtifact;
 
     /**
      * @parameter expression="${project.attachedArtifacts}"
      * @readonly
      * @required
      */
     private List<Artifact> projectAttachedArtifacts;
 
     public void execute()
             throws MojoExecutionException, MojoFailureException {
 
         verifyConfiguration();
 
         CollabNetSession session;
         try {
             session = new CollabNetSession(teamForgeUrl, teamForgeUsername, teamForgePassword, new PluginLog());
         } catch (RemoteException e) {
             throw new MojoExecutionException("Failed to login to TeamForge: " + e.getLocalizedMessage(), e);
         }
 
         List<Artifact> artifacts = new ArrayList<Artifact>();
         artifacts.add(projectArtifact);
         artifacts.addAll(projectAttachedArtifacts);
 
         try {
             String projectId = findProjectId(session);
 
             FrsSession frsSession = session.createFrsSession(projectId);
             String packageId;
             String releaseId;
             if (createRelease) {
                 packageId = frsSession.findOrCreatePackage(createPackageTemplate());
                 releaseId = frsSession.findOrCreateRelease(createReleaseTemplate(), packageId);
             } else {
                 packageId = findPackage(frsSession);
                 releaseId = findRelease(frsSession, packageId);
             }
 
             uploadArtifacts(artifacts, releaseId, frsSession);
         } catch (RemoteException e) {
             throw new MojoExecutionException(e.getLocalizedMessage(), e);
         } finally {
             logoff(session);
         }
     }
 
     private com.maestrodev.plugins.collabnet.frs.Package createPackageTemplate() {
         Package template = new Package();
         template.setTitle(pkg);
         template.setDescription(packageDescription);
         return template;
     }
 
     private Release createReleaseTemplate() {
         Release template = new Release();
         template.setTitle(release);
         template.setDescription(releaseDescription);
         template.setMaturity(releaseMaturity);
         template.setStatus(releaseStatus);
         return template;
     }
 
     private String findRelease(FrsSession frsSession, String packageId) throws RemoteException, MojoFailureException {
         String releaseId;
         try {
             releaseId = frsSession.findRelease(release, packageId);
         } catch (ResourceNotFoundException e) {
             throw new MojoFailureException(e.getLocalizedMessage());
         }
         return releaseId;
     }
 
     private String findPackage(FrsSession frsSession) throws RemoteException, MojoFailureException {
         String packageId;
         try {
             packageId = frsSession.findPackage(pkg);
         } catch (ResourceNotFoundException e) {
             throw new MojoFailureException(e.getLocalizedMessage());
         }
         return packageId;
     }
 
     private String findProjectId(CollabNetSession session) throws MojoFailureException {
         String projectId;
         try {
             projectId = session.findProject(project);
         } catch (RemoteException e) {
             getLog().error("Exception retrieving TeamForge project: " + e.getLocalizedMessage(), e);
             throw new MojoFailureException("Failed to retrieve TeamForge project '" + project + "': " + e.getLocalizedMessage());
         }
         getLog().debug("Found CollabNet project '" + projectId + "'");
         return projectId;
     }
 
     private void logoff(CollabNetSession session) {
         try {
             session.logoff();
         } catch (RemoteException e) {
             getLog().error("Error logging off from CollabNet TeamForge (ignoring): " + e.getLocalizedMessage(), e);
         }
     }
 
     private void uploadArtifacts(List<Artifact> artifacts, String releaseId, FrsSession frsSession) throws MojoExecutionException {
         for (Artifact artifact : artifacts) {
             File file = artifact.getFile();
             if (file != null) {
                 getLog().info("Uploading '" + file + "' to release '" + releaseId + "'");
                 try {
                     frsSession.uploadFile(releaseId, file, overwrite);
                 } catch (RemoteException e) {
                     throw new MojoExecutionException("Unable to upload file: " + e.getLocalizedMessage(), e);
                 } catch (MalformedURLException e) {
                     throw new MojoExecutionException("Error trying to convert artifact to an URL: " + e.getLocalizedMessage(), e);
                 }
             } else {
                 getLog().debug("Skipping artifact with no file: " + artifact);
             }
         }
     }
 
     private void verifyConfiguration() throws MojoFailureException {
         if (teamForgeServerId != null) {
             AuthenticationInfo authenticationInfo = wagonManager.getAuthenticationInfo(teamForgeServerId);
            teamForgeUsername = authenticationInfo.getUserName();
            teamForgePassword = authenticationInfo.getPassword();
 
            getLog().debug("Using credentials from settings for user = " + teamForgeUsername);
         }
 
         if (teamForgeUsername == null) {
             throw new MojoFailureException("TeamForge username must be specified");
         }
         if (teamForgePassword == null) {
             throw new MojoFailureException("TeamForge password must be specified");
         }
 
         boolean snapshot = ArtifactUtils.isSnapshot(projectArtifact.getVersion());
         if (overwrite == null) {
             overwrite = snapshot;
             getLog().debug("Setting overwrite flag to: " + overwrite);
         }
 
         if (releaseStatus == null) {
             releaseStatus = snapshot ? "pending" : "active";
             getLog().debug("Using release status: " + releaseStatus);
         }
     }
 
     private class PluginLog implements Log {
         public void debug(String msg) {
             getLog().debug(msg);
         }
 
         public void info(String msg) {
             getLog().info(msg);
         }
     }
 }
