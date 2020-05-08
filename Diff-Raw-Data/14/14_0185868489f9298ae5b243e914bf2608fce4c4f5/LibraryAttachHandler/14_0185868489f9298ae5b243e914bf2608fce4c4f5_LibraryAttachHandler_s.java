 /*
  * Copyright 2013, Bruce Mitchener, Jr.
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
 
 package org.dylanfoundry.deft.library;
 
 import com.intellij.CommonBundle;
 import com.intellij.openapi.application.AccessToken;
 import com.intellij.openapi.application.WriteAction;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.roots.OrderRootType;
 import com.intellij.openapi.roots.libraries.NewLibraryConfiguration;
 import com.intellij.openapi.roots.libraries.ui.OrderRoot;
 import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
 import com.intellij.openapi.ui.DialogWrapper;
 import com.intellij.openapi.ui.Messages;
 import com.intellij.openapi.util.Ref;
 import com.intellij.openapi.vfs.VfsUtil;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.openapi.vfs.VirtualFileManager;
 import org.dylanfoundry.deft.registry.DeftRegistryInfo;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import javax.swing.*;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class LibraryAttachHandler {
   public static final Logger LOG = Logger.getInstance("#org.dylanfoundry.deft.library");
 
   @Nullable
   public static NewLibraryConfiguration chooseLibrary(final @NotNull Project project,
                                                       JComponent parentComponent) {
     LibraryAttachDialog dialog = new LibraryAttachDialog(project);
     dialog.setTitle("Attach Library");
     dialog.show();
     if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
       return null;
     }
 
     NewLibraryConfiguration configuration = attachLibrary(project, dialog.getRegistryEntries());
     if (configuration == null) {
       Messages.showErrorDialog(parentComponent, "No libraries attached", CommonBundle.getErrorTitle());
     }
     return configuration;
   }
 
   @Nullable
   public static NewLibraryConfiguration attachLibrary(final @NotNull Project project,
                                                       List<DeftRegistryInfo> registryEntries) {
     final Ref<NewLibraryConfiguration> result = Ref.create(null);
 
     for (DeftRegistryInfo registryEntry : registryEntries) {
       AccessToken accessToken = WriteAction.start();
       try {
         final List<OrderRoot> roots = createRoots(registryEntry);
 
         result.set(new NewLibraryConfiguration(registryEntry.getLibraryName(),
            DeftLibraryType.getInstance(),
            new DeftLibraryProperties(registryEntry.getLibraryName())) {
          @Override
          public void addRoots(@NotNull LibraryEditor editor) {
            editor.addRoots(roots);
          }
        });
       } finally {
         accessToken.finish();
       }
     }
 
     return result.get();
   }
 
   public static List<DeftRegistryInfo> parseRegistryFile(String location) {
     List<DeftRegistryInfo> results = new ArrayList<DeftRegistryInfo>();
 
     File file = new File(location);
 
     // registry files are relative
     File sourceRoot = file.getParentFile().getParentFile().getParentFile();
 
     try {
       BufferedReader reader = new BufferedReader(new FileReader(file));
 
       String line;
       while ((line = reader.readLine()) != null) {
 
         try {
           URI registryUri = new URI(line);
 
           if (!registryUri.getScheme().equals("abstract")) {
             LOG.warn("Registry entries should begin with abstract://");
             continue;
           }
 
           String[] pathComponents = registryUri.getPath().split("/");
 
           StringBuilder sourcePath = new StringBuilder();
           sourcePath.append(sourceRoot.getAbsolutePath());
           sourcePath.append(File.separator);
           for (int i = 1; i < pathComponents.length; i++) {
             if (i > 1) {
               sourcePath.append(File.separator);
             }
 
             sourcePath.append(pathComponents[i]);
           }
 
           // TODO: Wire up a LID parser here, so we get the name from the library itself
           DeftRegistryInfo registryInfo = new DeftRegistryInfo(pathComponents[pathComponents.length - 1],
               sourcePath.toString());
 
           results.add(registryInfo);
         } catch (URISyntaxException use) {
           LOG.warn("Invalid registry entry", use);
         }
       }
     } catch (IOException ioe) {
       LOG.warn("Invalid registry file", ioe);
     }
 
     return results;
   }
 
   public static List<OrderRoot> createRoots(@NotNull DeftRegistryInfo registryInfo) {
     final List<OrderRoot> result = new ArrayList<OrderRoot>();
     final VirtualFileManager manager = VirtualFileManager.getInstance();
 
     File repoFolder = new File(registryInfo.getLocation());
     final String url = VfsUtil.getUrlForLibraryRoot(repoFolder);
     final VirtualFile file = manager.refreshAndFindFileByUrl(url);
     if (file != null) {
       OrderRootType rootType = OrderRootType.SOURCES;
       result.add(new OrderRoot(file, rootType));
     }
 
     return result;
   }
 
 }
