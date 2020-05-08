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
 
 import net.oneandone.maven.plugins.prerelease.util.Maven;
 import net.oneandone.maven.plugins.prerelease.core.Descriptor;
 import net.oneandone.maven.plugins.prerelease.core.Prerelease;
 import net.oneandone.maven.plugins.prerelease.core.Target;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.MavenProject;
 
 /**
  * Perform update without a working copy. Svn url and revision are passed as arguments, not determined from a working copy.
  */
 @Mojo(name = "bare-update", requiresProject = false)
 public class BareUpdate extends BareBase {
     /**
     * Email of the user invoking this goal. Determined via devreg when not specified.
      */
     @Parameter(property = "prerelease.user", required = true)
     private String user;
 
     public String getUser() {
         return user;
     }
 
     public void doExecute(Maven maven, MavenProject project, Target target, Descriptor descriptor) throws Exception {
         Prerelease prerelease;
 
         // TODO: duplicated code from update Mojo ...
         if (target.exists()) {
             getLog().info("prerelease already exists: " + descriptor.getName());
         } else {
             prerelease = Prerelease.create(getLog(), descriptor, target, alwaysUpdate, session.getUserProperties());
             try {
                 descriptor.check(world, project);
             } catch (RuntimeException e) {
                 throw e;
             } catch (Exception e) {
                 prerelease.target.scheduleRemove(getLog(), "build ok, but prerelease is not promotable: " + e.getMessage());
             }
         }
     }
 }
