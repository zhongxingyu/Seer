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
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.artifact.ProjectArtifactMetadata;
 import org.jasig.portal.plugin.deployer.DeployerConfig;
 import org.jasig.portal.plugin.deployer.EarDeployer;
 
/**
 * Deploy the EAR artifact from the current project. Only works when used in a 
 * project that specifies the "ear" packaging type.
 * 
 * @author Eric Dalquist
 */
 @Mojo(name="deploy-ear")
 public class DeployEarMojo extends AbstractDeployEarMojo {
     @Parameter(defaultValue="${project.artifact}", required=true, readonly=true)
     private Artifact artifact;
     
     @Parameter(defaultValue="${project.file}", required=true, readonly=true)
     private File pomFile;
 
     @Parameter(defaultValue="${project.packaging}", required=true, readonly=true)
     private String packaging;
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         if (!"ear".equals(this.packaging)) {
             throw new MojoExecutionException("deploy-ear only works on 'ear' packaged projects");
         }
         
 	    final EarDeployer earDeployer = getEarDeployer();
 	    
 	    final ProjectArtifactMetadata metadata = new ProjectArtifactMetadata( artifact, pomFile );
 	    artifact.addMetadata(metadata);
 
 	    final File earFile = artifact.getFile();
 	    if (earFile == null) {
 	        throw new MojoExecutionException("No build artifact is configured: " + artifact + "/" + artifact.getClass());
 	    }
 	    
 	    final DeployerConfig deployerConfig = getDeployerConfig();
 	    
 	    deployerConfig.setEarLocation(earFile);
 	    
 	    earDeployer.deploy(deployerConfig);
     }
 }
