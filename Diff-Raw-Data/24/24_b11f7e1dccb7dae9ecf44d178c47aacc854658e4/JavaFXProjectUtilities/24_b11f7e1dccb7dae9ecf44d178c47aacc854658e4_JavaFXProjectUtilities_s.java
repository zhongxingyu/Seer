 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  * 
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * 
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.profiler.utilities;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import org.netbeans.api.project.Project;
 import org.netbeans.api.project.ProjectUtils;
 import org.netbeans.api.project.SourceGroup;
 import org.netbeans.api.project.Sources;
 import org.netbeans.lib.profiler.client.ClientUtils;
 import org.netbeans.lib.profiler.common.filters.SimpleFilter;
 import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;
 import org.netbeans.spi.project.SubprojectProvider;
 import org.openide.filesystems.FileObject;
 import org.netbeans.modules.javafx.project.JavaFXProject;
 import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
 import org.openide.filesystems.FileUtil;
 
 
 /**
  *
  * @author cms
  */
 public class JavaFXProjectUtilities extends ProjectUtilities {
     
     public static final String SOURCES_TYPE_JAVAFX = "fx";         // NOI18N
     public static final String SOURCES_TYPE_JAVA   = "java";       // NOI18N
     public static final String JAVAFX_MIME_TYPE    = "text/x-fx";  // NOI18N
 
     public static ClientUtils.SourceCodeSelection[] getProjectDefaultRoots(Project project, String[][] projectPackagesDescr) {
         computeProjectPackages(project, true, projectPackagesDescr);
 
         ClientUtils.SourceCodeSelection[] ret = new ClientUtils.SourceCodeSelection[projectPackagesDescr[1].length];
 
         for (int i = 0; i < projectPackagesDescr[1].length; i++) {
             if ("".equals(projectPackagesDescr[1][i])) { //NOI18N
                 ret[i] = new ClientUtils.SourceCodeSelection("", "", ""); //NOI18N
             } else {
                 ret[i] = new ClientUtils.SourceCodeSelection(projectPackagesDescr[1][i] + ".", "", ""); //NOI18N
             }
         }
 
         return ret;
     }
 
     public static void computeProjectPackages(final Project project, boolean subprojects, String[][] storage) {
         if ((storage == null) || (storage.length != 2)) {
             throw new IllegalArgumentException("Storage must be a non-null String[2][] array"); // NOI18N
         }
 
         if (storage[0] == null) {
             Collection<String> packages1 = new ArrayList<String>();
 
             for (FileObject root : getSourceRoots(project, false)) {
                 addSubpackages(packages1, "", root); //NOI18N
             }
 
             storage[0] = packages1.toArray(new String[0]);
         }
 
         if (subprojects && (storage[1] == null)) {
             FileObject[] srcRoots2 = getSourceRoots(project, true); // TODO: should be computed based on already known srcRoots1
             ArrayList<String> packages2 = new ArrayList<String>();
 
             for (FileObject root : srcRoots2) {
                 addSubpackages(packages2, "", root); //NOI18N
             }
 
             storage[1] = packages2.toArray(new String[0]);
         }
     }
 
     private static void addSubpackages(Collection<String> packages, String prefix, FileObject packageFO) {
         if (!packageFO.isFolder()) { // not a folder
             return;
         }
 
         FileObject[] children = packageFO.getChildren();
 
         // 1. check if there are java sources in this folder and if so, add to the list of packages
         if (!packages.contains(prefix)) { // already in there, skip this
             for (int i = 0; i < children.length; i++) {
                 FileObject child = children[i];
 
                 if (child.getExt().equals(SOURCES_TYPE_JAVAFX) || //NOI18N
                     child.getExt().equals(SOURCES_TYPE_JAVA)) {   //NOI18N
                     packages.add(prefix);
                     
                     break;
                 }
             }
         }
 
         // 2. recurse into subfolders
         for (int i = 0; i < children.length; i++) {
             FileObject child = children[i];
 
             if (child.isFolder()) {
                 if ("".equals(prefix)) { //NOI18N
                     addSubpackages(packages, child.getName(), child);
                 } else {
                     addSubpackages(packages, prefix + "." + child.getName(), child); //NOI18N
                 }
             }
         }
     }
 
     public static FileObject[] getSourceRoots(final Project project, final boolean traverse) {
         Set<FileObject> set = new HashSet<FileObject>();
         Set<Project> projects = new HashSet<Project>();
 
         projects.add(project);
         getSourceRoots(project, traverse, projects, set);
 
         return set.toArray(new FileObject[set.size()]);
     }
 
     private static void getSourceRoots(final Project project, final boolean traverse, Set<Project> projects, Set<FileObject> roots) {
         final Sources sources = ProjectUtils.getSources(project);
 
         for (SourceGroup sg : sources.getSourceGroups(SOURCES_TYPE_JAVA)) {
             roots.add(sg.getRootFolder());
         }
         
         if (traverse) {
             // process possible subprojects recursively
             SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);
 
             if (spp != null) {
                 for (Project p : spp.getSubprojects()) {
                     if (projects.add(p)) {
                         getSourceRoots(p, traverse, projects, roots);
                     }
                 }
             }
         }
     }
     
     public static String getToplevelClassName(final Project project, FileObject profiledClassFile) {
         if (SourceUtils.isJavaFile(profiledClassFile)) {
             return SourceUtils.getToplevelClassName(profiledClassFile);
         } else if (isJavaFXFile(profiledClassFile)) {
             JavaFXProject projectJFX = (JavaFXProject)project;
            String clazz = FileUtil.getRelativePath(getRoot(projectJFX.getSourceRoots().getRoots(),profiledClassFile), profiledClassFile);
             return (clazz.substring(0, clazz.length() - 3)).replace('/','.');           
         }
         return ""; //NOI18N // won't be here: other file types are not supported
     }   
     
     public static boolean isJavaFXFile(FileObject f) {
         return JAVAFX_MIME_TYPE.equals(f.getMIMEType()); //NOI18N
     }        
     
     public static FileObject getRoot(FileObject[] roots, FileObject file) {
         FileObject srcDir = null;
         for (int i=0; i< roots.length; i++) {
             if (FileUtil.isParentOf(roots[i],file) || roots[i].equals(file)) {
                 srcDir = roots[i];
                 break;
             }
         }
         return srcDir;
     }    
     
     public static SimpleFilter computeProjectOnlyInstrumentationFilter(Project project, SimpleFilter predefinedInstrFilter,
             String[][] projectPackagesDescr) {
         // TODO: projectPackagesDescr[1] should only contain packages from subprojects, currently contains also toplevel project packages
         if (FILTER_PROJECT_ONLY.equals(predefinedInstrFilter)) {
             computeProjectPackages(project, false, projectPackagesDescr);
 
             StringBuffer projectPackages = new StringBuffer();
 
             for (int i = 0; i < projectPackagesDescr[0].length; i++) {
                 projectPackages.append("".equals(projectPackagesDescr[0][i]) ? getDefaultPackageClassNames(project)
                         : (projectPackagesDescr[0][i] + ". ")); //NOI18N
             }
             
             // TBD: correct filter name!!!
             return new SimpleFilter("", SimpleFilter.SIMPLE_FILTER_INCLUSIVE,
                     projectPackages.toString().trim());
         } else if (FILTER_PROJECT_SUBPROJECTS_ONLY.equals(predefinedInstrFilter)) {
             computeProjectPackages(project, true, projectPackagesDescr);
 
             StringBuffer projectPackages = new StringBuffer();
 
             for (int i = 0; i < projectPackagesDescr[1].length; i++) {
                 projectPackages.append("".equals(projectPackagesDescr[1][i]) ? getDefaultPackageClassNames(project)
                         : (projectPackagesDescr[1][i] + ". ")); //NOI18N // TODO: default packages need to be processed also for subprojects!!!
             }
             // TBD: correct filter name!!!
             return new SimpleFilter("", SimpleFilter.SIMPLE_FILTER_INCLUSIVE,
                     projectPackages.toString().trim());
         }
 
         return null;
     }    
 }
