 /*
  * Copyright 2011 Stanley Shyiko
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ivyplug.dependencies;
 
 import com.intellij.openapi.application.Application;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.roots.*;
 import com.intellij.openapi.roots.libraries.Library;
 import com.intellij.openapi.roots.libraries.LibraryTable;
 import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
 import ivyplug.adapters.ModuleComponentAdapter;
 
 import javax.swing.*;
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
  * @since 29.01.2011
  */
 public class DependencySyncManager extends ModuleComponentAdapter {
 
     public static final String LIBRARY_PREFIX = "Ivy: ";
 
     private Application application;
     private Project project;
     private LibraryTablesRegistrar libraryTablesRegistrar;
     private ModuleRootManager moduleRootManager;
     private Set<LibraryDependency> libraryDependencies;
     private Set<ModuleDependency> moduleDependencies;
 
     public DependencySyncManager(Module module) {
         super(module);
         application = ApplicationManager.getApplication();
         project = module.getProject();
         libraryTablesRegistrar = LibraryTablesRegistrar.getInstance();
         moduleRootManager = ModuleRootManager.getInstance(module);
         libraryDependencies = new HashSet<LibraryDependency>();
         moduleDependencies = new HashSet<ModuleDependency>();
     }
 
     public void addLibraryDependency(String org, String module, String rev,
                                      LibraryDependency.ArtifactType artifactType, File file) {
         libraryDependencies.add(new LibraryDependency(org, module, rev, artifactType, file));
     }
 
     public void addModuleDependency(Module module) {
         moduleDependencies.add(new ModuleDependency(module));
     }
 
     public void commit() {
         commit(false);
     }
 
     public void commit(final boolean removeOldLibraries) {
         final Set<LibraryDependency> libraryDependenciesToMerge = libraryDependencies;
         final Set<ModuleDependency> moduleDependenciesToMerge = moduleDependencies;
         libraryDependencies = new HashSet<LibraryDependency>();
         moduleDependencies = new HashSet<ModuleDependency>();
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 application.runWriteAction(new Runnable() {
 
                     public void run() {
                         ModifiableRootModel modifiableModuleModel = moduleRootManager.getModifiableModel();
                         try {
                             if (removeOldLibraries)
                                 removeOldLibraries(modifiableModuleModel);
                             mergeLibraryDependencies(modifiableModuleModel, libraryDependenciesToMerge);
                             mergeModuleDependencies(modifiableModuleModel, moduleDependenciesToMerge);
                             modifiableModuleModel.commit();
                         } finally {
                             if (modifiableModuleModel.isWritable())
                                 modifiableModuleModel.dispose();
                         }
                     }
                 });
             }
         });
     }
 
     private void removeOldLibraries(ModifiableRootModel modifiableModuleModel) {
         for (OrderEntry orderEntry : modifiableModuleModel.getOrderEntries()) {
             if (orderEntry instanceof LibraryOrderEntry) {
                 LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) orderEntry;
                 String libraryName = libraryOrderEntry.getLibraryName();
                 if (libraryName != null && libraryName.startsWith(LIBRARY_PREFIX)) {
                     modifiableModuleModel.removeOrderEntry(libraryOrderEntry);
                 }
             }
         }
     }
 
     private void mergeLibraryDependencies(ModifiableRootModel modifiableModuleModel, Set<LibraryDependency> libraryDependenciesToCommit) {
         LibraryTable projectLibraryTable = libraryTablesRegistrar.getLibraryTable(project);
         Set<String> moduleLibraries = getModuleLibraries(modifiableModuleModel);
         for (LibraryDependency libraryDependency : libraryDependenciesToCommit) {
             String libraryName = getLibraryName(libraryDependency);
             Library library = projectLibraryTable.getLibraryByName(libraryName);
             if (library == null) {
                 library = projectLibraryTable.createLibrary(libraryName);
             }
             if (!moduleLibraries.contains(libraryName)) {
                 modifiableModuleModel.addLibraryEntry(library);
             }
             mergeLibraryDependency(library, libraryDependency);
         }
     }
 
     private Set<String> getModuleLibraries(ModifiableRootModel modifiableModuleModel) {
         Set<String> result = new HashSet<String>();
         for (OrderEntry orderEntry : modifiableModuleModel.getOrderEntries()) {
             if (orderEntry instanceof LibraryOrderEntry) {
                 LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) orderEntry;
                 String libName = libraryOrderEntry.getLibraryName();
                 if (libName != null)
                     result.add(libName);
             }
         }
         return result;
     }
 
     private String getLibraryName(LibraryDependency libraryDependency) {
         return String.format("%s%s:%s:%s", LIBRARY_PREFIX,
                 libraryDependency.getOrg(), libraryDependency.getModule(), libraryDependency.getRev());
     }
 
     private void mergeModuleDependencies(ModifiableRootModel modifiableModuleModel, Set<ModuleDependency> moduleDependenciesToCommit) {
         Module[] moduleDependencies = modifiableModuleModel.getModuleDependencies();
         Set<String> alreadyBoundModules = new HashSet<String>(moduleDependencies.length);
         for (Module module : moduleDependencies) {
             alreadyBoundModules.add(module.getName());
         }
         for (ModuleDependency moduleDependency : moduleDependenciesToCommit) {
             Module module = moduleDependency.getModule();
             if (!alreadyBoundModules.contains(module.getName()))
                 modifiableModuleModel.addModuleOrderEntry(module);
         }
     }
 
     private void mergeLibraryDependency(Library library, LibraryDependency dependency) {
         Library.ModifiableModel modifiableModel = null;
         try {
             String[] searchScope;
             OrderRootType orderRootType;
             switch (dependency.getArtifactType()) {
                 case CLASSES:
                     searchScope = library.getUrls(OrderRootType.CLASSES);
                     orderRootType = OrderRootType.CLASSES;
                     break;
                 case SOURCES:
                     searchScope = library.getUrls(OrderRootType.SOURCES);
                     orderRootType = OrderRootType.SOURCES;
                     break;
                 case JAVADOCS:
                     searchScope = library.getUrls(JavadocOrderRootType.getInstance());
                     orderRootType = JavadocOrderRootType.getInstance();
                     break;
                 default:
                     throw new UnsupportedOperationException("Unsupported dependency's artifact type " +
                         dependency.getArtifactType().name());
             }
             String dependencyURI = toURI(dependency.getFile());
             for (String existingDependency : searchScope) {
                 if (existingDependency.equals(dependencyURI)) {
                     return;
                 }
             }
             modifiableModel = library.getModifiableModel();
             modifiableModel.addRoot(dependencyURI, orderRootType);
         } finally {
             if (modifiableModel != null)
                 modifiableModel.commit();
         }
     }
 
     private String toURI(File file) {
         String result = file.getAbsolutePath();
         if (file.getName().toLowerCase().endsWith(".jar")) {
             result = "jar://" + result + "!/";
         }
         return result;
     }
 }
