 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
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
 package net.oneandone.maven.plugins.prerelease;
 
 import net.oneandone.maven.plugins.prerelease.core.Archive;
 import net.oneandone.maven.plugins.prerelease.core.Prerelease;
 import net.oneandone.maven.plugins.prerelease.core.WorkingCopy;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.lifecycle.internal.BuilderCommon;
 import org.apache.maven.lifecycle.internal.MojoExecutor;
import org.apache.maven.plugin.MojoExecution;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectHelper;
 
 import java.io.IOException;
 
 /**
  * Promotes a prerelease by commuting the tag and deploying its artifact(s).
  *
  * Execute this goal in the svn working directory of the project you want to release and make sure you have created a prerelease.
  *
  * This goal commits the tag of the current prerelease and deploys the respective artifact(s). It also updates (in your svn trunk or branch
  * on the svn server) the project version in pom.xml to the next development version, and it updates your changes file (if you have one).
  * In your svn working directory, it runs "svn up" to get the adjusted pom and changes files.
  *
  * Artifacts are deployed by invoking all plugin goals tied to the deploy phase. These goals are either mandatory or optional, as configured
  * by the "mandatory" parameter. Mandatory goals have to succeed for this plugin to succeed. Optional goals may fail, which results in a
  * warning only. The maven-deploy plugin is typically (and by default) mandatory, other notification goals are optional.
  *
  * Error handling. In contrast to Maven's Release Plugin there's no rollback goal: when promote fails, artifacts and tags will be properly
  * removed. (If Tag creation fails, this goal fails with an error. If artifact deployment fails, the svn tag will be deleted from
  * the repository and the goal fails with an error. If optional promote goal fail, the plugins succeeds, but it issues a warning.)
  *
  * Note that the deployment date of artifacts in the repository may be later that the release date in the changes.xml.
  */
 @Mojo(name = "promote")
 public class Promote extends ProjectBase {
     /**
      * Email of the user invoking this goal.
      */
     @Parameter(property = "prerelease.user", required = true)
     private String user;
 
     /**
      * Comma-separated list of plugin artifact ids that are mandatory for promotion.
      */
     @Parameter(property = "prerelease.promote.mandatory", defaultValue = "maven-deploy-plugin")
     protected String mandatory;
 
     @Component
     protected BuilderCommon builderCommon;
 
     @Component
     protected MavenProjectHelper projectHelper;
 
     @Component
     protected MojoExecutor mojoExecutor;
 
     public String getUser() {
         return user;
     }
 
     public void doExecute(Archive archive) throws Exception {
         WorkingCopy workingCopy;
         long revision;
         Prerelease prerelease;
         MavenProject releasePom;
 
         workingCopy = checkedWorkingCopy();
         revision = workingCopy.revision();
         setTarget(archive.target(revision));
         prerelease = target.loadOpt();
         if (prerelease == null) {
             throw new MojoExecutionException("no prerelease for revision " + revision);
         }
         releasePom = maven().loadPom(prerelease.checkout.join("pom.xml")); // TODO
        releasePom.setPluginArtifactRepositories(project.getRemoteArtifactRepositories()); // TODO ...
        session.setCurrentProject(releasePom);
        try {
            prerelease.promote(getLog(), user, mandatory, releasePom, session, builderCommon, projectHelper, mojoExecutor);
        } finally {
            session.setCurrentProject(project);
        }
         workingCopy.update(getLog());
     }
 }
