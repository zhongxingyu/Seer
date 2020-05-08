 package org.sakaiproject.maven.plugin.component;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
 import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectBuilder;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.artifact.InvalidDependencyVersionException;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 
 /**
  * Generate the exploded webapp
  * 
  * @goal deploy
  * @requiresDependencyResolution runtime
  */
 public class ComponentDeployMojo extends AbstractComponentMojo {
 	
 	/**
 	 * The directory where the webapp is built.
 	 * 
 	 * @parameter expression="${maven.tomcat.home}"
 	 * @required
 	 */
 	private File deployDirectory;
 
 	/**
 	 * A map to define the destination where items are unpacked.
 	 * 
 	 * @parameter expression="${sakai.app.server}"
 	 */
 	private String appServer = null;
 	
 	/**
 	 * The ID of the artifact to use when deploying.
 	 * 
 	 * @parameter expression="${project.artifactId}"
 	 * @required
 	 */
 	
 	/** @component */
 	private MavenProjectBuilder mavenProjectBuilder;
 	
 	/** @component */
 	private ArtifactMetadataSource metadataSource;
 	
 	private Properties locationMap;
 
 	private static Properties defaultLocatioMap;
 	
 	static {
 		defaultLocatioMap = new Properties();
 		defaultLocatioMap.setProperty("components", "components/");
 		defaultLocatioMap.setProperty("webapps", "webapps/");
 		defaultLocatioMap.setProperty("shared/lib", "shared/lib/");
 		defaultLocatioMap.setProperty("server/lib", "server/lib/");
 		defaultLocatioMap.setProperty("common/lib", "common/lib/");
 		defaultLocatioMap.setProperty("configuration", "/");
 	}
 
 	public File getDeployDirectory() {
 		return deployDirectory;
 	}
 
 	public void setDeployDirectory(File deployDirectory) {
 		this.deployDirectory = deployDirectory;
 	}
 	
 	public String getAppServer()
 	{
 		return appServer;
 	}
 
 	public void setAppServer(String appServer)
 	{
 		this.appServer = appServer;
 	}
 	
 	public String getDeployId(MavenProject project)
 	{
 		String deployId = project.getBuild().getFinalName();
		if (deployId == null || deployId.equals(project.getArtifactId()+ "-"+ project.getVersion())) {
 			deployId = project.getProperties().getProperty("deployId", project.getArtifactId());
 		}
 		return deployId;
 	}
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		deployToContainer(project);
 	}
 
 	public void deployToContainer(MavenProject project) throws MojoExecutionException,
 			MojoFailureException
 
 	{
 		try {
 			Set artifacts = project.getDependencyArtifacts();
 			// iterate through the this to extract dependencies and deploy
 
 			String packaging = project.getPackaging();
 			File deployDir = getDeployDirectory();
 			if (deployDir == null) {
 				throw new MojoFailureException(
 						"deployDirectory has not been set");
 			}
 			if ("sakai-component".equals(packaging)) {
 				// UseCase: Sakai component in a pom
 				// deploy to component and unpack as a
 				getLog().info(
 						"Deploying " + project.getGroupId() + ":"
 								+ project.getArtifactId() + ":"
 								+ project.getPackaging()
 								+ " as an unpacked component");
 				File destination = new File(deployDir, getDeploySubDir("components"));
 				String fileName = project.getArtifactId();
 				File destinationDir = new File(destination, fileName);
 				Artifact artifact = project.getArtifact();
 				if (artifact == null) {
 					getLog().error(
 							"No Artifact found in project " + getProjectId());
 					throw new MojoFailureException(
 							"No Artifact found in project");
 				}
 				File artifactFile = artifact.getFile();
 				if (artifactFile == null) {
 					artifactResolver.resolve(artifact, remoteRepositories,
 							artifactRepository);
 					artifactFile = artifact.getFile();
 				}
 				if (artifactFile == null) {
 					getLog().error(
 							"Artifact File is null for " + getProjectId());
 					throw new MojoFailureException("Artifact File is null ");
 				}
 				getLog().info(
 						"Unpacking " + artifactFile + " to " + destinationDir);
 				deleteAll(destinationDir);
 				destinationDir.mkdirs();
 				unpack(artifactFile, destinationDir, "war", false);
 			}
 			else if ("sakai-configuration".equals(packaging)) {
 				// UseCase: Sakai configuration in a pom
 				// deploy to component and unpack as a
 				getLog().info(
 						"Deploying " + project.getGroupId() + ":"
 								+ project.getArtifactId() + ":"
 								+ project.getPackaging()
 								+ " as an unpacked configuration");
 				File destinationDir = new File(deployDir, getDeploySubDir("configuration"));
 				Artifact artifact = project.getArtifact();
 				if (artifact == null) {
 					getLog().error(
 							"No Artifact found in project " + getProjectId());
 					throw new MojoFailureException(
 							"No Artifact found in project");
 				}
 				File artifactFile = artifact.getFile();
 				if (artifactFile == null) {
 					artifactResolver.resolve(artifact, remoteRepositories,
 							artifactRepository);
 					artifactFile = artifact.getFile();
 				}
 				if (artifactFile == null) {
 					getLog().error(
 							"Artifact File is null for " + getProjectId());
 					throw new MojoFailureException("Artifact File is null ");
 				}
 				getLog().info(
 						"Unpacking " + artifactFile + " to " + destinationDir);
 				destinationDir.mkdirs();
 				// we use a zip unarchiver
 				unpack(artifactFile, destinationDir, "zip" , false);
 			} else if ("war".equals(packaging)) {
 				// UseCase: war webapp
 				// deploy to webapps but dont unpack
 				getLog().info(
 						"Deploying " + project.getGroupId() + ":"
 								+ project.getArtifactId() + ":"
 								+ project.getPackaging() + " as a webapp");
 				deployProjectArtifact(new File(deployDir,  getDeploySubDir("webapps")), false,
 						true, project);
 
 			} else if ("jar".equals(packaging)) {
 				// UseCase: jar, marked with a property
 				// deploy the target
 				Properties p = project.getProperties();
 				String deployTarget = p.getProperty("deploy.target");
 				if ("shared".equals(deployTarget)) {
 					deployProjectArtifact(new File(deployDir, getDeploySubDir("shared/lib")),
 							true, false, project);
 				} else if ("common".equals(deployTarget)) {
 					deployProjectArtifact(new File(deployDir, getDeploySubDir("common/lib")),
 							true, false, project);
 				} else if ("server".equals(deployTarget)) {
 					deployProjectArtifact(new File(deployDir, getDeploySubDir("server/lib")),
 							true, false, project);
 				} else {
 					getLog().info(
 							"No deployment specification -- skipping "
 									+ getProjectId());
 				}
 			} else if ("pom".equals(packaging)) {
 				// UseCase: pom, marked with a property
 				// deploy the contents
 				Properties p = project.getProperties();
 				String deployTarget = p.getProperty("deploy.target");
 				if ("shared".equals(deployTarget)) {
 					File destinationDir = new File(deployDir, getDeploySubDir("shared/lib"));
 					destinationDir.mkdirs();
 					deployArtifacts(artifacts, destinationDir);
 				} else if ("common".equals(deployTarget)) {
 					File destinationDir = new File(deployDir, getDeploySubDir("common/lib"));
 					destinationDir.mkdirs();
 					deployArtifacts(artifacts, destinationDir);
 				} else if ("server".equals(deployTarget)) {
 					File destinationDir = new File(deployDir, getDeploySubDir("server/lib"));
 					destinationDir.mkdirs();
 					deployArtifacts(artifacts, destinationDir);
 				} else if ( "tomcat-overlay".equals(deployTarget)) {
 				        String cleanTargetPaths = p.getProperty("clean.targets");
 				        String[] cleanPaths = cleanTargetPaths.split(";");
 				        for ( String pathToClean : cleanPaths ) {
 	                                  File destinationDir = new File(deployDir, getDeploySubDir(pathToClean));
 	                                  getLog().info("Deleting "+destinationDir);
 				          deleteAll(destinationDir);
 				        }
 				        deployDir.mkdirs();
 				        deployOverlay(artifacts, deployDir);
 				        
 				} else if ("distro".equals(deployTarget)) {
 					// Big deploy of all artifacts....
 					for (Artifact artifact: (Set<Artifact>)artifacts) {
 						try {
 							MavenProject dependentProject = mavenProjectBuilder.buildFromRepository(artifact,
 									remoteRepositories, artifactRepository);
 							dependentProject.setDependencyArtifacts(dependentProject.createArtifacts(artifactFactory, null, null));
 							deployToContainer(dependentProject);
 						} catch (ProjectBuildingException e) {
 							throw new MojoFailureException("Failed to build project for :"+ artifact.getId());
 						} catch (InvalidDependencyVersionException e) {
 							throw new MojoFailureException("Failed to find depdendencies for: "+ artifact.getId());
 						}
 					}
 					
 				} else {
 					getLog().info(
 							"No deployment specification -- skipping "
 									+ getProjectId());
 				}
 			} else {
 				getLog().info(
 						"No deployment specification -- skipping "
 								+ getProjectId());
 			}
 		} catch (IOException ex) {
 			getLog().debug("Failed to deploy to container ", ex);
 			throw new MojoFailureException("Fialed to deploy to container :"
 					+ ex.getMessage());
 		} catch (NoSuchArchiverException ex) {
 			getLog().debug("Failed to deploy to container ", ex);
 			throw new MojoFailureException("Fialed to deploy to container :"
 					+ ex.getMessage());
 		} catch (AbstractArtifactResolutionException ex) {
 			getLog().debug("Failed to deploy to container ", ex);
 			throw new MojoFailureException("Fialed to deploy to container :"
 					+ ex.getMessage());
 		}
 
 	}
 
 	/**
 	 * @param string
 	 * @param string2
 	 * @return
 	 */
 	private String getDeploySubDir(String key)
 	{
 		
 		if ( locationMap == null ) {
 			if ( appServer != null  ) {
 				try
 				{
 					InputStream in = getClass().getClassLoader().getResourceAsStream(
 							"deploy." + appServer + ".properties");
 					Properties p = new Properties();
 					p.load(in);
 					in.close();
 					locationMap = p;
 				}
 				catch (Exception ex)
 				{
 					this.getLog().warn("No Config for appserver "+appServer+" cause:"+ex.getMessage());
 				}
 			}
 			if ( locationMap == null ) {
 				locationMap = defaultLocatioMap;
 			}
 		}
 		String deploySubDir = locationMap.getProperty(key);
 		if ( deploySubDir == null || deploySubDir.trim().length() == 0 ) {
 			deploySubDir = defaultLocatioMap.getProperty(key);
 		}
 		if (deploySubDir == null ) {
 		  deploySubDir = key;
 		}
 		if ( !deploySubDir.endsWith("/") ) {
 			deploySubDir = deploySubDir + "/";
 		}
 		return deploySubDir;
 	}
 
 	protected void deployOverlay(Set artifacts, File destination)
 			throws IOException, MojoFailureException,
 			AbstractArtifactResolutionException, MojoExecutionException, NoSuchArchiverException {
 		for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
 			Artifact artifact = (Artifact) iter.next();
 			if (artifact == null) {
 				getLog().error(
 						"Null Artifact found, sould never happen, in artifacts for project "
 								+ getProjectId());
 				throw new MojoFailureException(
 						"Null Artifact found, sould never happen, in artifacts for project ");
 			}
 			File artifactFile = artifact.getFile();
 			if (artifactFile == null) {
 				artifactResolver.resolve(artifact, remoteRepositories,
 						artifactRepository);
 				artifactFile = artifact.getFile();
 			}
 			if (artifactFile == null) {
 				getLog().error(
 						"Artifact File is null for dependency "
 								+ artifact.getId() + " in " + getProjectId());
 				throw new MojoFailureException(
 						"Artifact File is null for dependency "
 								+ artifact.getId() + " in " + getProjectId());
 			}
                         getLog().debug("Processing: " + artifact.getId());
                         if ( !"test".equals(artifact.getScope()) ) {
                           unpack(artifact.getFile(), destination, artifact.getType(),true);
                         }
 		}
 
 	}
 	protected void deployArtifacts(Set artifacts, File destination)
 	throws IOException, MojoFailureException,
 	AbstractArtifactResolutionException {
 		for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
 			Artifact artifact = (Artifact) iter.next();
 			if (artifact == null) {
 				getLog().error(
 						"Null Artifact found, sould never happen, in artifacts for project "
 						+ getProjectId());
 				throw new MojoFailureException(
 				"Null Artifact found, sould never happen, in artifacts for project ");
 			}
 			File artifactFile = artifact.getFile();
 			if (artifactFile == null) {
 				artifactResolver.resolve(artifact, remoteRepositories,
 						artifactRepository);
 				artifactFile = artifact.getFile();
 			}
 			if (artifactFile == null) {
 				getLog().error(
 						"Artifact File is null for dependency "
 						+ artifact.getId() + " in " + getProjectId());
 				throw new MojoFailureException(
 						"Artifact File is null for dependency "
 						+ artifact.getId() + " in " + getProjectId());
 			}
 			String targetFileName = getDefaultFinalName(artifact);
 
 			getLog().debug("Processing: " + targetFileName);
 			File destinationFile = new File(destination, targetFileName);
 			if ("provided".equals(artifact.getScope())
 					|| "test".equals(artifact.getScope())) {
 				getLog().info(
 						"Skipping " + artifactFile + " Scope "
 						+ artifact.getScope());
 
 			} else {
 				getLog()
 				.info("Copy " + artifactFile + " to " + destinationFile);
 				copyFileIfModified(artifact.getFile(), destinationFile);
 			}
 		}
 
 	}
 
 	private void deployProjectArtifact(File destination, boolean withVersion,
 			boolean deleteStub, MavenProject project) throws MojoFailureException, IOException,
 			AbstractArtifactResolutionException {
 		Artifact artifact = project.getArtifact();
 		String fileName = null;
 		String stubName = null;
 		if (withVersion) {
 			stubName = getDeployId(project) + "-" + project.getVersion();
 			fileName = stubName+ "." + project.getPackaging();
 		} else {
 			stubName = getDeployId(project);
 			fileName = stubName+ "." + project.getPackaging();
 		}
 		File destinationFile = new File(destination, fileName);
 		File stubFile = new File(destination, stubName);
 		Set artifacts = project.getArtifacts();
 		getLog().info("Found " + artifacts.size() + " artifacts");
 		for (Iterator i = artifacts.iterator(); i.hasNext();) {
 			Artifact a = (Artifact) i.next();
 			getLog()
 					.info(
 							"Artifact Id " + a.getArtifactId() + " file "
 									+ a.getFile());
 		}
 		if (artifact == null) {
 			getLog().error("No Artifact found in project " + getProjectId());
 			throw new MojoFailureException(
 					"No Artifact found in project, target was "
 							+ destinationFile);
 		}
 		File artifactFile = artifact.getFile();
 		if (artifactFile == null) {
 			artifactResolver.resolve(artifact, remoteRepositories,
 					artifactRepository);
 			artifactFile = artifact.getFile();
 		}
 		if (artifactFile == null) {
 			getLog().error(
 					"Artifact File is null for " + getProjectId()
 							+ ", target was " + destinationFile);
 			throw new MojoFailureException("Artifact File is null ");
 		}
 		getLog().info("Copy " + artifactFile + " to " + destinationFile);
 		destinationFile.getParentFile().mkdirs();
 		if (deleteStub && stubFile.exists()) {
 			deleteAll(stubFile);
 		}
 		copyFileIfModified(artifactFile, destinationFile);
 	}
 
 }
