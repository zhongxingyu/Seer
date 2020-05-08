 /*
 DMEclipseSaveHookPlugin - Eclipse plugin for executing java code on java source code changes - http://www.davide.bz/dmesh
 
 Copyright (C) 2013 Davide Montesin <d@vide.bz> - Bolzano/Bozen - Italy
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>
 */
 
 package bz.davide.dmeclipsesavehookplugin.builder;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 
 /**
  * @author Davide Montesin <d@vide.bz>
  */
 public class DMEclipseSaveHookPluginBuilder extends IncrementalProjectBuilder
 {
 
    public static final String  BUILDER_ID  = "DMEclipseSaveHookPlugin.DMEclipseSaveHookPluginBuilder";
 
    private static final String MARKER_TYPE = "DMEclipseSaveHookPlugin.xmlProblem";
 
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException
    {
       // Check for changes only in the "output" folder!
 
       try
       {
         System.out.println("DMEclipseSaveHookPlugin " + new Date().toString());
          if (this.getProject().hasNature(JavaCore.NATURE_ID))
          {
             IJavaProject javaProject = JavaCore.create(this.getProject());
 
             ArrayList<IProject> depProjects = new ArrayList<IProject>();
             ArrayList<String> fullClasspath = new ArrayList<String>();
             findTransitiveDepProjects(javaProject, depProjects, fullClasspath);
 
             File projectDir = new File(this.getProject().getLocation().toOSString());
 
             DeltaVisitor deltaVisitor = new DeltaVisitor();
 
             for (IProject iprj : depProjects)
             {
                IResourceDelta delta = this.getDelta(iprj);
                if (delta != null)
                {
                   // Check if all changes are only in the bin path
                   // if yes do nothing, is the effect of a previous change!
                   delta.accept(deltaVisitor);
 
                }
             }
             // No src file was changed!
             if (deltaVisitor.countSrc == 0)
             {
                return depProjects.toArray(new IProject[0]);
             }
             System.out.println(" *** SRC FILES CHANGED *** REGENERATING ...");
             IFile jbindFile = this.getProject().getFile("dmxmljson.conf");
             if (jbindFile.exists())
             {
                InputStream is = jbindFile.getContents();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
                String line;
 
                do
                {
 
                   int numClasspaths = Integer.parseInt(br.readLine());
 
                   URL[] urls = new URL[numClasspaths + fullClasspath.size()];
                   for (int i = 0; i < numClasspaths; i++)
                   {
                      File file = new File(projectDir, br.readLine());
                      String path = file.getAbsolutePath() + (file.isDirectory() ? "/" : "");
                      System.out.println(path);
                      urls[i] = new URL("file://" + path);
                   }
                   for (int i = 0; i < fullClasspath.size(); i++)
                   {
                      urls[i + numClasspaths] = new URL("file://" + fullClasspath.get(i));
                   }
 
                   URLClassLoader urlClassLoader = new URLClassLoader(urls);
                   String mainClassName = br.readLine();
                   Class<?> clazz = urlClassLoader.loadClass(mainClassName);
 
                   ArrayList<String> parameters = new ArrayList<String>();
                   // Add project root folder
                   parameters.add(projectDir.getAbsolutePath());
 
                   line = br.readLine();
                   while (line != null && line.length() > 0)
                   {
                      parameters.add(line);
                      line = br.readLine();
                   }
 
                   Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
                   // Exception during invoke must not break the plug-in funcionality!
                   try
                   {
                      mainMethod.invoke(null, new Object[] { parameters.toArray(new String[0]) });
                   }
                   catch (Exception exxx)
                   {
                      exxx.printStackTrace();
                   }
                   urlClassLoader.close();
 
                }
                while (line != null);
 
                br.close();
 
                this.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
 
             } // jf bind.txt exits
             return depProjects.toArray(new IProject[0]);
          } // if java nature
       }
       catch (Exception exxx)
       {
          exxx.printStackTrace();
       }
       return null;
    }
 
    static void findTransitiveDepProjects(IJavaProject current,
                                          ArrayList<IProject> projects,
                                          ArrayList<String> fullClasspath) throws CoreException
    {
       if (!projects.contains(current.getProject()))
       {
          projects.add(current.getProject());
       }
 
       fullClasspath.add(ResourcesPlugin.getWorkspace().getRoot().findMember(current.getOutputLocation()).getLocation().toOSString() +
                         "/");
       ArrayList<IClasspathEntry> classPaths = new ArrayList<IClasspathEntry>();
       classPaths.addAll(Arrays.asList(current.getRawClasspath()));
 
       for (int x = 0; x < classPaths.size(); x++)
       {
          IClasspathEntry cp = classPaths.get(x);
          if (cp.getEntryKind() == IClasspathEntry.CPE_PROJECT)
          {
             String prjName = cp.getPath().lastSegment();
             IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(prjName);
             if (prj.hasNature(JavaCore.NATURE_ID))
             {
                IJavaProject javaProject = JavaCore.create(prj);
                findTransitiveDepProjects(javaProject, projects, fullClasspath);
             }
             continue;
          }
          if (cp.getEntryKind() == IClasspathEntry.CPE_CONTAINER)
          {
             String fullContainerName = cp.getPath().toString();
             if (!fullContainerName.startsWith("org.eclipse.jdt.launching.JRE_CONTAINER/"))
             {
                System.out.println("CP C: " + fullContainerName);
                IClasspathContainer container = JavaCore.getClasspathContainer(cp.getPath(), current);
                classPaths.addAll(Arrays.asList(container.getClasspathEntries()));
 
             }
          }
          if (cp.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
          {
            IPath path = cp.getPath();
            // Check first if this path is relative to workspace
            IResource workspaceMember = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
            if (workspaceMember != null)
            {
               String fullPath = workspaceMember.getLocation().toOSString();
               fullClasspath.add(fullPath);
            }
            else
            {
               fullClasspath.add(path.toOSString());
            }
          }
       }
    }
 
 }
