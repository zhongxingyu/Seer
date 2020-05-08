 /**
  *
  * Copyright to the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations under the License.
  */
 
 package org.apache.maven.eventspy;
 
 import org.apache.maven.execution.ExecutionEvent;
 import org.apache.maven.plugin.MojoExecution;
 import org.apache.maven.project.MavenProject;
 
 import java.util.Date;
 
 public class PluginStatsFactory {
     public PluginStats build(ExecutionEvent event) {
         PluginStats stats = new PluginStats();
         stats.type = PluginStats.Type.valueOf(event.getType());
         stats.executionId = event.getMojoExecution().getExecutionId();
         stats.timestamp = new Date();
         stats.project = buildProject(event);
         stats.plugin = buildPlugin(event);
         stats.executionHashCode = event.getMojoExecution().hashCode();
         stats.session = event.getSession();
         return stats;
     }
 
     private PluginStats.Plugin buildPlugin(ExecutionEvent event) {
         PluginStats.Plugin plugin = new PluginStats.Plugin();
         MojoExecution mojoExecution = event.getMojoExecution();
         plugin.groupId = mojoExecution.getGroupId();
         plugin.artifactId = mojoExecution.getArtifactId();
         plugin.version = mojoExecution.getVersion();
         plugin.goal = mojoExecution.getGoal();
         return plugin;
     }
 
     private PluginStats.Project buildProject(ExecutionEvent event) {
         MavenProject projectBeingBuilt = event.getProject();
         PluginStats.Project project = new PluginStats.Project();
         project.groupId = projectBeingBuilt.getGroupId();
         project.artifactId = projectBeingBuilt.getArtifactId();
        project.version = projectBeingBuilt.getVersion();
         return project;
     }
 }
