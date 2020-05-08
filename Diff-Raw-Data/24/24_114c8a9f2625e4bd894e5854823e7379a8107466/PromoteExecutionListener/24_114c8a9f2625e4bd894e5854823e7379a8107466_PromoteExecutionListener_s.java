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
 package net.oneandone.maven.plugins.prerelease.util;
 
 import net.oneandone.maven.plugins.prerelease.core.BaseExecutionListener;
 import net.oneandone.maven.plugins.prerelease.core.Prerelease;
 import org.apache.maven.execution.ExecutionEvent;
 import org.apache.maven.execution.ExecutionListener;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectHelper;
 
 import java.io.IOException;
 
 public class PromoteExecutionListener extends BaseExecutionListener {
     private final Prerelease prerelease;
     private final MavenProjectHelper projectHelper;
     private int mojosStarted;
     private boolean firstSuccess;
 
     public PromoteExecutionListener(Prerelease prerelease, MavenProjectHelper projectHelper, ExecutionListener base) {
         super(base);
         this.prerelease = prerelease;
         this.projectHelper = projectHelper;
         this.mojosStarted = 0;
         this.firstSuccess = false;
     }
 
     @Override
     public void projectStarted(ExecutionEvent event) {
         MavenProject project;
 
         project = event.getSession().getCurrentProject();
         try {
             prerelease.artifactFiles(project, projectHelper);
             project.getProperties().putAll(prerelease.descriptor.deployProperties);
         } catch (IOException e) {
             throw new RuntimeException("TODO", e);
         }
         super.projectStarted(event);
     }
 
     @Override
     public void mojoStarted(ExecutionEvent event) {
         mojosStarted++;
         super.mojoStarted(event);
     }
 
     @Override
     public void mojoSucceeded(ExecutionEvent event) {
         if (mojosStarted == 1) {
             firstSuccess = true;
         }
         super.mojoSucceeded(event);
     }
 
     @Override
     public void mojoFailed(ExecutionEvent event) {
         super.mojoFailed(event);
     }
 
     public boolean isFirstSuccess() {
         return firstSuccess;
     }
 }
