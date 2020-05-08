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
 
 import it.wallgren.android.platform.DirectoryAnalyzer;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.FileInfoMatcherDescription;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceFilterDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 
 public class AndroidPlatformProject extends AndroidProject {
     private static String FILE_FILTER_ID = "org.eclipse.ui.ide.patternFilterMatcher";
 
     private final IPath repoPath;
     private final String projectName;
 
     public AndroidPlatformProject(IPath repoPath) {
         this.repoPath = repoPath;
        this.projectName = repoPath.lastSegment() + "-platform";
     }
 
     @Override
     public String getName() {
         return projectName;
     }
 
     @Override
     public IPath getPath() {
         return repoPath;
     }
 
     @Override
     public void doCreate(IProgressMonitor monitor) throws CoreException {
         final IProject project = createProject(projectName, monitor);
 
         final IFolder repoLink = createRepoLink(monitor, project, repoPath);
 
         final IClasspathEntry[] srcFolders = getSourceFolders(project, repoLink, monitor);
         final IClasspathEntry[] classPath = new IClasspathEntry[srcFolders.length + 1];
         System.arraycopy(srcFolders, 0, classPath, 0, srcFolders.length);
         classPath[srcFolders.length] = getAndroidDependenceis(repoPath);
         addJavaNature(classPath, monitor);
     }
 
     private IFolder createRepoLink(IProgressMonitor monitor, IProject project, IPath repoPath)
             throws CoreException {
         final IFolder repoLink = project.getFolder(repoPath.lastSegment());
         if (repoLink.exists()) {
             // This is an existing project the link already exists
             return repoLink;
         }
         // Let's filter out some content we don't need. To avoid it being
         // indexed
         repoLink.createFilter(IResourceFilterDescription.EXCLUDE_ALL
                 | IResourceFilterDescription.FOLDERS | IResourceFilterDescription.INHERITABLE,
                 new FileInfoMatcherDescription(FILE_FILTER_ID, "bin"), 0, monitor);
         repoLink.createFilter(IResourceFilterDescription.EXCLUDE_ALL
                 | IResourceFilterDescription.FOLDERS, new FileInfoMatcherDescription(
                         FILE_FILTER_ID, ".repo"), 0, monitor);
         repoLink.createFilter(IResourceFilterDescription.EXCLUDE_ALL
                 | IResourceFilterDescription.FOLDERS | IResourceFilterDescription.INHERITABLE,
                 new FileInfoMatcherDescription(FILE_FILTER_ID, ".git"), 0, monitor);
         // repoLink.createFilter(IResourceFilterDescription.EXCLUDE_ALL
         // | IResourceFilterDescription.FOLDERS, new FileInfoMatcherDescription(
         // FILE_FILTER_ID, "out"), 0, monitor);
         final IFolder out = repoLink.getFolder("out");
         // Only allow target/common/R in the out folder
         final int filterFlags = IResourceFilterDescription.INCLUDE_ONLY
                 | IResourceFilterDescription.FOLDERS | IResourceFilterDescription.FILES;
         out.createFilter(filterFlags, new FileInfoMatcherDescription(FILE_FILTER_ID, "target"), 0,
                 monitor);
 
         final IFolder target = out.getFolder("target");
         target.createFilter(filterFlags, new FileInfoMatcherDescription(FILE_FILTER_ID, "common"),
                 0, monitor);
 
         final IFolder common = target.getFolder("common");
         common.createFilter(filterFlags, new FileInfoMatcherDescription(FILE_FILTER_ID, "R"), 0,
                 monitor);
         repoLink.createLink(repoPath, 0, monitor);
 
         return repoLink;
     }
 
     private IClasspathEntry getAndroidDependenceis(IPath repoPath) {
         return JavaCore.newContainerEntry(new Path(
                 "it.wallgren.android.platform.classpathContainerInitializer"
                         + repoPath.makeAbsolute()));
     }
 
     private IClasspathEntry[] getSourceFolders(IProject project, IFolder repoLink,
             IProgressMonitor monitor) throws CoreException {
         final List<IPath> files = getJavaSourceFolders(repoLink);
         final LinkedList<IClasspathEntry> entries = new LinkedList<IClasspathEntry>();
         for (final IPath folder : files) {
             if (folder.toString().contains("tools") || folder.toString().contains("tests")) {
                 // Ugly hack to skip tools and test folders
                 continue;
             }
             entries.add(JavaCore.newSourceEntry(folder));
         }
         return entries.toArray(new IClasspathEntry[0]);
     }
 
     private List<IPath> getJavaSourceFolders(IFolder repoLink) {
         final List<IPath> srcFolders = new LinkedList<IPath>();
         final DirectoryAnalyzer da = new DirectoryAnalyzer(new File(repoLink.getRawLocation().toFile(),
                 "frameworks/base"));
         try {
             final Set<File> javaSources = da.findJavaSourceDirectories();
             final int startPos = repoPath.toOSString().length();
             for (final File file : javaSources) {
                 final IPath path = repoLink.getFullPath().append(
                         file.getAbsolutePath().substring(startPos));
                 srcFolders.add(path);
             }
         } catch (final IOException e) {
             e.printStackTrace();
         }
         srcFolders.add(repoLink.getFullPath().append("libcore/luni/src/main/java"));
         srcFolders.add(repoLink.getFullPath().append("out/target/common/R"));
         Collections.sort(srcFolders, new Comparator<IPath>() {
 
             @Override
             public int compare(IPath o1, IPath o2) {
                 // Sort the source folder list.
                 return o1.toString().compareTo(o2.toString());
             }
         });
         return srcFolders;
     }
 
     @Override
     public boolean preSelected() {
         return true;
     }
 }
