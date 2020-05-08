 /* Copyright 2009 Kindleit.net Software Development
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.kindleit.gae;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
  * Runs the WAR project locally on the Google App Engine development server.
  *
  * You can specify jvm flags via the jvmFlags in the configuration section.
  *
  * @author rhansen@kindleit.net
  * @goal run
  * @requiresDependencyResolution runtime
  * @execute phase="package"
  */
 public class RunGoal extends EngineGoalBase {
 
   /** Port to run in.
    *
    * @parameter expression="${gae.port}" default-value="8080"
    */
   protected int port;
 
   /** Address to bind to.
    *
    * @parameter expression="${gae.address}" default-value="0.0.0.0"
    */
   protected String address;
 
   /** Do not check for new SDK versions.
   *
   * @parameter expression="${gae.disableUpdateCheck}" default-value="false"
   */
   protected boolean disableUpdateCheck;
 
   /** Arbitrary list of JVM Flags to send to the KickStart task.
    *
    * @parameter
    */
  protected List<String> jvmFlags = new ArrayList<String>();
 
   public void execute() throws MojoExecutionException, MojoFailureException {
     final List<String> arguments = new ArrayList<String>();
 
     arguments.add("--address=" + address);
     arguments.add("--port=" + port);
     if (disableUpdateCheck) {
       arguments.add("--disable_update_check");
     }
     for (final String jvmFlag : jvmFlags) {
       arguments.add("--jvm_flag=" + jvmFlag);
     }
     arguments.add(appDir);
 
     runKickStart("com.google.appengine.tools.development.DevAppServerMain",
         arguments.toArray(new String[] {}));
   }
 
 }
