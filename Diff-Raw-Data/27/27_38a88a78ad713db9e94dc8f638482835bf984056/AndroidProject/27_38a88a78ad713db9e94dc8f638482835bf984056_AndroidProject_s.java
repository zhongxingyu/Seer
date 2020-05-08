 /**
  * Copyright 2012 Martin Wallgren
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
 
 package it.wallgren.android.platform.project;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceDescription;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 
 public abstract class AndroidProject implements Comparable<AndroidProject> {
     private final IWorkspace workspace;
     private IProject project;
 
     public abstract String getName();
 
     public abstract IPath getPath();
 
     public AndroidProject() {
         workspace = ResourcesPlugin.getWorkspace();
     }
 
     /**
      * Create the actual project in the workspace
      */
     protected abstract void doCreate(IProgressMonitor monitor) throws CoreException;
 
     protected IProject createProject(String name, IProgressMonitor monitor) throws CoreException {
         project = workspace.getRoot().getProject(name);
        if (project.exists()) {
            // TODO: Handle existing projects more graceful
            project.delete(true, monitor);
         }
        project.create(monitor);
         project.open(monitor);
 
         return project;
     }
 
     protected void addJavaNature(IClasspathEntry[] classPath, IProgressMonitor monitor)
             throws CoreException {
         if (project == null) {
             throw new IllegalStateException(
                     "Project must be created before giving it a Java nature");
         }
 
         final IProjectDescription description = project.getDescription();
         final String[] natures = description.getNatureIds();
         final String[] newNatures = Arrays.copyOf(natures, natures.length + 1);
         newNatures[natures.length] = JavaCore.NATURE_ID;
         description.setNatureIds(newNatures);
         project.setDescription(description, null);
         final IJavaProject javaProject = JavaCore.create(project);
 
         @SuppressWarnings("rawtypes")
         final
         Map options = javaProject.getOptions(true);
         // Compliance level need to be 1.6
         JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
         javaProject.setOptions(options);
         javaProject.setRawClasspath(classPath, monitor);
         javaProject.setOutputLocation(javaProject.getPath().append("out"), monitor);
     }
 
     public final void create(IProgressMonitor monitor) throws CoreException {
 
         boolean autobuild = workspace.isAutoBuilding();
         if (autobuild) {
             // Disable auto build during project setup.
             final IWorkspaceDescription wsDescription = workspace.getDescription();
             autobuild = wsDescription.isAutoBuilding();
             wsDescription.setAutoBuilding(false);
             workspace.setDescription(wsDescription);
         }
         doCreate(monitor);
         if (autobuild) {
             // re-enable auto build
             final IWorkspaceDescription wsDescription = workspace.getDescription();
             wsDescription.setAutoBuilding(true);
             workspace.setDescription(wsDescription);
         }
     }
 
     public abstract boolean preSelected();
 
     @Override
     public int compareTo(AndroidProject o) {
         if (o == null) {
             return -1;
         }
         return getName().compareTo(o.getName());
     }
 }
