 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2010 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.ide.idea.utils;
 
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.*;
 import com.intellij.psi.impl.PsiManagerImpl;
 import com.intellij.psi.impl.file.impl.FileManager;
 import org.apache.commons.lang.StringUtils;
 import org.jetbrains.annotations.Nullable;
 import org.sonar.ide.idea.IdeaSonarModuleComponent;
 import org.sonar.ide.shared.AbstractResourceUtils;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class IdeaFolderResourceUtils extends AbstractResourceUtils<VirtualFile> {
   private final Project project;
   private final FileManager fileManager;
 
   public IdeaFolderResourceUtils(Project project) {
     this.project = project;
     PsiManager psiManager = PsiManager.getInstance(project);
     fileManager = ((PsiManagerImpl)psiManager).getFileManager();
   }
 
   @Override
   protected boolean isJavaFile(VirtualFile file) {
     return true;
   }
 
   @Override
   public String getFileName(VirtualFile file) {
     return null;
   }
 
   @Nullable
   @Override
   public String getPackageName(VirtualFile file) {
     PsiDirectory directory = fileManager.findDirectory(file);
     PsiPackage aPackage = directory == null ? null : JavaDirectoryService.getInstance().getPackage(directory);
    if (aPackage.getClasses().length == 0) {
      aPackage = null; //Sonar doesnt support packages without classes - return null package (i.e. use module request)
    }
     String result = aPackage == null ? null : aPackage.getQualifiedName();
 
     if (StringUtils.isWhitespace(result))
       result = null;
     return result;
   }
 
   @Nullable
   @Override
   protected String getDirectoryPath(VirtualFile file) {
     return null;
   }
 
   @Nullable
   @Override
   public String getProjectKey(VirtualFile file) {
     IdeaSonarModuleComponent sonarModule = IdeaSonarModuleComponent.getInstance(file, project);
     if (sonarModule == null) {
       return null;
     }
     return getProjectKey(sonarModule.getGroupId(), sonarModule.getArtifactId(), sonarModule.getBranch());
   }
 }
 
