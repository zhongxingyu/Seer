 /*
  * (C) Copyright 2006-2012 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     slacoin
  *     Vladimir Pasquier <vpasquier@nuxeo.com>
  */
 package org.nuxeo.ide.sdk.java;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IAccessRule;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 /**
  * 
  * @author matic
  * @since 1.1
  */
 public class ClasspathEditor {
 
     public ClasspathEditor(IProject project) throws JavaModelException {
         this.project = project;
         java = JavaCore.create(project);
         entries.addAll(Arrays.asList(java.getRawClasspath()));
     }
 
     protected final IJavaProject java;
 
     protected final IProject project;
 
     protected final List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
 
     boolean dirty = false;
 
     /**
      * Extends classpath target project with src folder
      * 
      * @param name
      * @throws JavaModelException
      */
     public void extendClasspath(String name) throws JavaModelException {
         IProject project = java.getProject();
         IFolder folder = project.getFolder("src/main/" + name);
         IPackageFragmentRoot root = java.getPackageFragmentRoot(folder);
         if (root.exists()) {
             IClasspathEntry entry = root.getRawClasspathEntry();
             if (entry.getOutputLocation() != null) {
                 return;
             }
             entries.remove(entry);
         }
         // extend project class path
         IFolder binFolder = project.getFolder("bin/" + name);
         IClasspathEntry newEntry = JavaCore.newSourceEntry(
                 folder.getFullPath(), new IPath[0], new IPath[0],
                 binFolder.getFullPath());
         entries.add(newEntry);
         dirty = true;
     }
 
     /**
      * Adding containers to the project classpath
      * 
      * @param containers
      * @throws JavaModelException
      */
     public void addContainers(List<String> containers)
             throws JavaModelException {
         for (String container : containers) {
             IClasspathEntry classPathEntry = JavaCore.newContainerEntry(
                    new Path(container), new IAccessRule[0], null, false);
             entries.add(classPathEntry);
         }
         dirty = true;
     }
 
     public void addLibrary(IPath path) {
         IClasspathEntry newEntry = JavaCore.newLibraryEntry(path, null, null);
         entries.add(newEntry);
         dirty = true;
     }
 
     public void removeLibrary(Path path) {
         Iterator<IClasspathEntry> it = entries.iterator();
         while (it.hasNext()) {
             IClasspathEntry entry = it.next();
             if (path.equals(entry.getPath())) {
                 it.remove();
                 dirty = true;
             }
         }
     }
 
     public void flush() throws JavaModelException {
         if (dirty) {
             java.setRawClasspath(
                     entries.toArray(new IClasspathEntry[entries.size()]), null);
         }
         dirty = false;
     }
 
     /**
      * Removing containers to the project classpath
      * 
      * @param containers
      * @throws JavaModelException
      */
     public void removeContainers(List<String> containers) {
         for (String container : containers) {
             IClasspathEntry classPathEntry = JavaCore.newContainerEntry(
                    new Path(container), new IAccessRule[0], null, false);
             entries.remove(classPathEntry);
         }
         dirty = true;
     }
 }
