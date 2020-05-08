 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.jasig.portal.plugin.mojo;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.repository.RepositorySystem;
 import org.jasig.portal.plugin.deployer.DeployerConfig;
 import org.jasig.portal.plugin.deployer.EarDeployer;
 
 /**
 * Deploys an EAR file either specified specifically by the file parameter or
 * resolved via a GAV
  * 
  * @author Eric Dalquist
  */
 @Mojo(name = "deploy-ear-file", requiresProject = false)
 public class DeployEarFileMojo extends AbstractDeployEarMojo {
     @Component
     private RepositorySystem repositorySystem;
 
     @Parameter(property = "localRepository")
     private ArtifactRepository localRepository;
 
     @Parameter(property = "project.remoteArtifactRepositories")
     private List<ArtifactRepository> remoteRepositories;
 
     @Parameter(property="artifactId")
     private String artifactId;
 
     @Parameter(property="groupId")
     private String groupId;
 
     @Parameter(property="version")
     private String version;
 
     @Parameter(property="classifier")
     private String classifier;
     
     @Parameter(property="file")
     private File file;
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         final EarDeployer earDeployer = getEarDeployer();
 
         final DeployerConfig deployerConfig = getDeployerConfig();
 
         final File artifactFile;
         if (file != null) {
             artifactFile = file;
         }
         else {
             //Setup artifact resolution request
             final Artifact artifact = repositorySystem.createArtifactWithClassifier(groupId, artifactId, version, "ear", classifier);
             final ArtifactResolutionRequest artifactResolutionRequest = new ArtifactResolutionRequest();
             artifactResolutionRequest.setArtifact(artifact);
             artifactResolutionRequest.setLocalRepository(localRepository);
             artifactResolutionRequest.setRemoteRepositories(remoteRepositories);
             
             //Resolve artifact
             repositorySystem.resolve(artifactResolutionRequest);
             artifactFile = artifact.getFile();
         }
         deployerConfig.setEarLocation(artifactFile);
 
         //Deploy
         earDeployer.deploy(deployerConfig);
     }
 
 }
