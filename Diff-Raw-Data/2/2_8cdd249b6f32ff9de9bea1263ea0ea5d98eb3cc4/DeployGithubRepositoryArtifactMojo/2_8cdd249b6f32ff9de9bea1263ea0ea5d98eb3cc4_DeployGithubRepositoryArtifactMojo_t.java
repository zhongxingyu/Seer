 /*
  * Copyright 2011 Kevin Pollet
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
 package com.github.maven.plugin;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.github.maven.plugin.client.GithubClient;
 import com.github.maven.plugin.client.exceptions.GithubArtifactAlreadyExistException;
 import com.github.maven.plugin.client.exceptions.GithubArtifactNotFoundException;
 import com.github.maven.plugin.client.exceptions.GithubException;
 import com.github.maven.plugin.client.exceptions.GithubRepositoryNotFoundException;
 import com.github.maven.plugin.client.impl.GithubClientImpl;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.codehaus.plexus.util.DirectoryScanner;
 
 /**
  * Uploads artifacts to the download section of
  * the configured github repository.
  *
  * @author Kevin Pollet
  *
  * @goal upload
  * @phase deploy
  * @threadSafe
  * @requiresOnline true
  * @since 1.0
  */
 public class DeployGithubRepositoryArtifactMojo extends AbstractGithubMojo {
 	/**
 	 * Allows to disable the upload of artifacts which match at least one
 	 * pattern of the given list.
 	 *
 	 * @parameter
 	 */
 	private String[] excludes;
 
 	/**
 	 * Allows to configure the artifacts to upload.
 	 *
 	 * @parameter
 	 */
 	private Artifact[] artifacts;
 
 	/**
 	 * If true, artifacts will be overridden even if they exist
 	 * in the repository download section.
 	 *
	 * @parameter default-value="false"
 	 */
 	private boolean overrideExistingFile;
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		try {
 
 			//if no artifacts are configured, upload main artifact and attached artifacts
 			if ( artifacts == null ) {
 				final Set<Artifact> githubArtifacts = new HashSet<Artifact>();
 
 				final DirectoryScanner scanner = new DirectoryScanner();
 				scanner.setExcludes( excludes );
 				scanner.setBasedir( mavenProject.getBuild().getDirectory() );
 				scanner.scan();
 
 				final List<String> includedFiles = Arrays.asList( scanner.getIncludedFiles() );
 
 				//add main artifact
 				if ( includedFiles.contains( mavenProject.getArtifact().getFile().getName() ) ) {
 					githubArtifacts.add(
 							new Artifact(
 									mavenProject.getArtifact().getFile(),
 									mavenProject.getDescription(),
 									overrideExistingFile
 							)
 					);
 				}
 
 				//add attached artifacts
 				for ( org.apache.maven.artifact.Artifact attachedArtifact : mavenProject.getAttachedArtifacts() ) {
 					if ( includedFiles.contains( attachedArtifact.getFile().getName() ) ) {
 						githubArtifacts.add(
 								new Artifact(
 										attachedArtifact.getFile(), mavenProject.getDescription(), overrideExistingFile
 								)
 						);
 					}
 				}
 
 				artifacts = githubArtifacts.toArray( new Artifact[0] );
 			}
 
 			//upload artifacts to configured github repository
 			uploadArtifacts( artifacts );
 
 		}
 		catch ( GithubRepositoryNotFoundException e ) {
 			throw new MojoFailureException( e.getMessage(), e );
 		}
 		catch ( GithubArtifactNotFoundException e ) {
 			throw new MojoFailureException( e.getMessage(), e );
 		}
 		catch ( GithubArtifactAlreadyExistException e ) {
 			throw new MojoFailureException( e.getMessage(), e );
 		}
 		catch ( GithubException e ) {
 			throw new MojoExecutionException( "Unexpected error", e );
 		}
 	}
 
 	/**
 	 * Upload the given artifacts into the configured github repository.
 	 *
 	 * @param artifacts The artifacts to upload.
 	 *
 	 * @throws MojoExecutionException if artifacts are not properly configured.
 	 */
 	private void uploadArtifacts(Artifact... artifacts) throws MojoExecutionException {
 		final Log log = getLog();
 		final GithubClient githubClient = new GithubClientImpl( login, token );
 
 		log.info( "" );
 		for ( Artifact artifact : artifacts ) {
 			if ( artifact.getFile() == null ) {
 				throw new MojoExecutionException( "Missing <file> into artifact configuration " );
 			}
 
 			log.info( "Uploading " + artifact );
 			if ( artifact.getOverride() ) {
 				githubClient.replace(
 						artifact.getName(), artifact.getFile(), artifact.getDescription(), repository
 				);
 			}
 			else {
 				githubClient.upload(
 						artifact.getName(), artifact.getFile(), artifact.getDescription(), repository
 				);
 			}
 		}
 		log.info( "" );
 	}
 }
 
