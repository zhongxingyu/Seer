 package jo.release;
 
 /*
  Copyright 2013 John Oliver
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
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
 
 import org.apache.maven.artifact.versioning.ArtifactVersion;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * @goal release-versions
  * @execute lifecycle="release-versions" phase="initialize"
  */
 
 public class MoveToReleaseMojo extends AbstractVersionModMojo {
 
 	/**
 	 * Whether or not to commit and tag the release in the scm
 	 * 
 	 * @parameter
 	 */
 	protected boolean tag = true;
 
 	/**
 	 * Base directory of the project.
 	 * 
 	 * @parameter default-value="${basedir}"
 	 * @required
 	 * @readonly
 	 */
 	private File basedir;
 	
 	public void execute() throws MojoExecutionException {
 
         ArtifactVersion artifactVersion = new Version( project.getVersion() );
         Version newVersion = new Version(    artifactVersion.getMajorVersion(), 
 						        			 artifactVersion.getMinorVersion(),
 						        			 artifactVersion.getIncrementalVersion(),
 						        			 artifactVersion.getBuildNumber(),
 						        			 null);
         writeVersion(newVersion);
         tag(newVersion.toString());
 	}
 
 	private void tag(String newVersion) throws MojoExecutionException {
 		
 		boolean isRootProject = session.getExecutionRootDirectory().equalsIgnoreCase(basedir.toString());
         if(!tag || !isRootProject) {
         	return;
         }
         
         // Commit the version bump
 		String message = "Move to release versions for release " + newVersion; 
 		executeMojo(
 			plugin( groupId("org.apache.maven.plugins"),
 					artifactId("maven-scm-plugin"),
 					version(scmPluginVersion)),
 			goal("checkin"), 
 			configuration(
 			        element(name("basedir"), basedir.getAbsolutePath()),
 			        element(name("message"), message)
 			        ),
 			executionEnvironment(project, session, pluginManager));
 		 
 		//Commit tag
 		executeMojo(
 			plugin( groupId("org.apache.maven.plugins"),
 					artifactId("maven-scm-plugin"),
 					version(scmPluginVersion)),
 			goal("tag"), 
 			configuration(
 			        element(name("basedir"), basedir.getAbsolutePath()),
			        element(name("tag"), project.getName()+"-"+newVersion)
 			        ),
 			executionEnvironment(project, session, pluginManager));
 		
 	}
 }
