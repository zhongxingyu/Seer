 /* Copyright 2011 Kindleit.net Software Development
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
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
  * Displays times for the next several runs of each cron job
  * from Google's servers.
  *
  * @author rhansen@kindleit.net
  *
  * @goal cron-info
  * @requiresOnline
  * @since 0.7.1
  *
  */
 public class CronInfoGoal extends EngineGoalBase {
 
   public void execute() throws MojoExecutionException, MojoFailureException {
     getLog().info("Getting cron info...");
    runAppCfg("cron_info");
   }
 
 }
