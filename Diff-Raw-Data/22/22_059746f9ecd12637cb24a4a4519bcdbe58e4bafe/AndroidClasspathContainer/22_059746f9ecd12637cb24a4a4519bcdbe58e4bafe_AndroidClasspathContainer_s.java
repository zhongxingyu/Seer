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
 
 package it.wallgren.android.platform;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 
 public class AndroidClasspathContainer implements IClasspathContainer {
     private final IPath repoRoot;
 
     private IClasspathEntry[] entries;
     private final Object lock = new Object();
 
     /**
      * Filter for libraries that should not be added to the classpath.
      *
      * Note! The filter is applied with a starts with match.
      */
     private static final List<String> FILTER = Arrays.asList(new String[] {
             "android_stubs_current_intermediates", "api-stubs_intermediates",
             "offline-sdk_intermediates", "online-sdk_intermediates",
             "sdk_v", // All sdk_v*_intermediates libraries
             "android-support-v" // android-support-v*_intermediates
     });
 
     public AndroidClasspathContainer(IPath repoRoot) {
         this.repoRoot = repoRoot;
     }
 
     @Override
     public IClasspathEntry[] getClasspathEntries() {
         // TODO: Update entries when jars get added to the file system.
         synchronized (lock) {
             if (entries == null) {
                 final List<File> libs = getAndroidDependenceisFile(repoRoot);
                 entries = new IClasspathEntry[libs.size()];
                 int i = 0;
                 for (final File jar : libs) {
                     entries[i++] = JavaCore
                            .newLibraryEntry(new Path(jar.getAbsolutePath()), null, null);
                 }
             }
             final IClasspathEntry[] out = new IClasspathEntry[entries.length];
             System.arraycopy(entries, 0, out, 0, out.length);
             return out;
         }
     }
 
     public void setEntries(IClasspathEntry[] entries) {
         synchronized (lock) {
             this.entries = entries;
         }
     }
 
     private List<File> getAndroidDependenceisFile(IPath repoRoot) {
         final LinkedList<File> jars = new LinkedList<File>();
         findJavaLibs(repoRoot.append("/out/target/common/obj/JAVA_LIBRARIES/").toFile(), FILTER,
                 jars);
         return jars;
     }
 
     /**
      * Recursive search for java libraries (i.e classes.jar)
      *
      * @param path
      * @param filter items in the filter will not be added to the result
      * @param out
      */
     private void findJavaLibs(File path, List<String> filter, List<File> out) {
         if (path.isDirectory()) {
             if (isFiltered(filter, path.getName())) {
                 return;
             }
             if (path.getName().equals("emma_out")) {
                 return;
             }
             for (final File child : path.listFiles()) {
                 findJavaLibs(child, filter, out);
             }
         }
         if (path.isFile() && "classes.jar".equals(path.getName())) {
             out.add(path);
         }
     }
 
     private boolean isFiltered(List<String> filter, String name) {
         for (final String string : filter) {
             if (name.startsWith(string)) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public String getDescription() {
         return repoRoot.lastSegment() + " - java dependencies";
     }
 
     @Override
     public int getKind() {
         return K_SYSTEM;
     }
 
     @Override
     public IPath getPath() {
         return new Path("it.wallgren.android.platform.classpathContainerInitializer"
                 + repoRoot.makeAbsolute());
     }
 }
