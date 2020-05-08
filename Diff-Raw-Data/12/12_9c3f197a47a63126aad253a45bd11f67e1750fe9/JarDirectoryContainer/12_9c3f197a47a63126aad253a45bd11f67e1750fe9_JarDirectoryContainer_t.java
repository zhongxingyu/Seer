 package org.antifun.eclipsejardirectory;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 public class JarDirectoryContainer implements IClasspathContainer {
 
     public static final String ID = "org.antifun.JarDirectoryContainer";
     
     static final FileFilter JAR_FILTER = new FileFilter() {
         @Override
         public boolean accept(File pathname) {
             return !pathname.isDirectory() && pathname.getName().endsWith(".jar");
         }
     };
 
     static final FileFilter DIR_FILTER = new FileFilter() {
         @Override
         public boolean accept(File pathname) {
             return pathname.isDirectory() && !pathname.getName().startsWith(".");
         }
     };
 
     private IPath configuredPath;
     private IJavaProject project;
 
     private Collection<IClasspathEntry> getJustJarsInDirectory(File baseDir) {
        ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
         Log.info("asked to get classpath entries for " + baseDir);
         File[] jars = baseDir.listFiles(JAR_FILTER);
        if (jars != null) {
            Log.info("contains " + jars.length + " jars");
            for (File jar : jars) {
                entries.add(JavaCore.newLibraryEntry(new Path(jar.getAbsolutePath()), null, null));
            }
         }
         return entries;
     }
 
     private Collection<IClasspathEntry> getJarsFromDirectory(File baseDir) {
         ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
         entries.addAll(getJustJarsInDirectory(baseDir));
         File[] subdirs = baseDir.listFiles(DIR_FILTER);        
         for (File subdir : subdirs) {
             entries.addAll(getJarsFromDirectory(subdir));
         }
         return entries;
     }
 
     @Override
     public IClasspathEntry[] getClasspathEntries() {
         File baseDir = getAbsolutePath(project, getRelativePath(configuredPath)).toFile();
         Collection<IClasspathEntry> entries = getJarsFromDirectory(baseDir);
         return entries.toArray(new IClasspathEntry[0]);    
     }
     
     public JarDirectoryContainer(IPath path, IJavaProject project) {
         this.configuredPath = path;
         this.project = project;
     }
     
     @Override
     public String getDescription() {
         return "Jar Directory: " + getRelativePath(configuredPath).toString();
     }
     
     @Override
     public int getKind() {
         return IClasspathContainer.K_APPLICATION;
     }
     
     @Override
     public IPath getPath() {
         return configuredPath;
     }
     
     public boolean isValid() {
         return configuredPath.segment(0).equals(ID);
     }
 
     /**
      * Convert a "munged" path looking like "org.antifun.JarDirectoryContainer/lib" into just "/lib".
      * @param fullyMungedPath
      * @return
      */
     public static IPath getRelativePath(IPath fullyMungedPath) {        
         if (fullyMungedPath.segment(0).equals(ID)) {
             return fullyMungedPath.removeFirstSegments(1);
         } else {
             return fullyMungedPath;
         }
     }
     
     /**
      * Convert a relative path like "/lib" into a fully-filesystem-realized path based on the project configuration.
      * @param project
      * @param relativePath
      * @return
      */
     public static IPath getAbsolutePath(IJavaProject project, IPath relativePath) {
         IPath projectPath = project.getResource().getLocation();
         return projectPath.append(relativePath);
     }
 
     public static IPath getProjectRelativePath(IPath absolutePath, IJavaProject project) {
         try {
             IPath projectPath = project.getCorrespondingResource().getLocation();
             if (projectPath.isPrefixOf(absolutePath)) {
                 int segmentsMatching = 0;
                 for (segmentsMatching = 0; segmentsMatching < projectPath.segmentCount(); segmentsMatching++) {
                     if (!projectPath.segment(segmentsMatching).equals(absolutePath.segment(segmentsMatching))) {
                         break;
                     }
                 }
                 // remainder should be the unique portion.
                 IPath remainingPath = absolutePath.removeFirstSegments(segmentsMatching);
                 return remainingPath;
             } else {
                 Log.error("Somehow the project path '" + projectPath + "' is not a prefix of the chosen path '" + absolutePath + "'.");
                 return null;            
             }
         } catch (JavaModelException e) {
             Log.error("Impossible exception " + e);
             return null;
         }
     }
 }
