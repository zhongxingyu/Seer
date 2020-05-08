 /*
  * Copyright 2013 Web Geo Services
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
 package com.wgs.cucumber.python;
 
 import com.intellij.openapi.fileTypes.FileType;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleManager;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.roots.ModuleRootManager;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiDirectory;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiReference;
 import com.intellij.psi.impl.file.PsiDirectoryFactory;
 import com.intellij.psi.util.PsiTreeUtil;
 import com.intellij.util.PathUtil;
 import com.intellij.util.containers.ContainerUtil;
 import com.jetbrains.python.PythonFileType;
 import com.jetbrains.python.module.PythonModuleType;
 import com.jetbrains.python.psi.PyFile;
 import com.jetbrains.python.psi.PyFunction;
 import com.jetbrains.python.psi.PyStatement;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
 import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
 import org.jetbrains.plugins.cucumber.psi.GherkinFile;
 import org.jetbrains.plugins.cucumber.psi.GherkinRecursiveElementVisitor;
 import org.jetbrains.plugins.cucumber.psi.GherkinStep;
 import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
 import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;
 
 import java.util.*;
 
 /**
  * @author Emmanuel Peralta
  */
 public class CucumberPythonExtension implements CucumberJvmExtensionPoint {
 
     @Override
     public boolean isStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
         return child instanceof PyFile && ((PyFile) child).getName().endsWith(".py");
     }
 
     @Override
     public boolean isWritableStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
         return isStepLikeFile(child, parent);
     }
 
     @NotNull
     @Override
     public List<AbstractStepDefinition> getStepDefinitions(@NotNull PsiFile psiFile) {
         final List<AbstractStepDefinition> newDefs = new ArrayList<AbstractStepDefinition>();
 
         if (psiFile instanceof PyFile) {
             PyFile pyFile = (PyFile) psiFile;
 
             List<PyStatement> statements = pyFile.getStatements();
 
             for (PyStatement statement : statements) {
                 if (statement instanceof PyFunction) {
                     PyFunction func = (PyFunction) statement;
 
                    try {
                        newDefs.add(new LettuceStepDefinition(func));
                    } catch (NullPointerException e) {
                        // silence !
                     }
                 }
             }
         }
         return newDefs;
     }
 
     @NotNull
     @Override
     public FileType getStepFileType() {
         return PythonFileType.INSTANCE;
     }
 
     @NotNull
     @Override
     public StepDefinitionCreator getStepDefinitionCreator() {
         return new PyStepDefinitionCreator();
     }
 
     @NotNull
     @Override
     public String getDefaultStepFileName() {
         return "StepDef";
     }
 
     @Override
     public void collectAllStepDefsProviders(@NotNull List<VirtualFile> providers, @NotNull Project project) {
         final Module[] modules = ModuleManager.getInstance(project).getModules();
         for (Module module : modules) {
             if (PythonModuleType.get(module) instanceof PythonModuleType) {
                 final VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
                 ContainerUtil.addAll(providers, roots);
             }
         }
     }
 
     @Override
     public void loadStepDefinitionRootsFromLibraries(@NotNull Module module, List<PsiDirectory> psiDirectories, @NotNull Set<String> strings) {
     }
 
     @Override
     public List<PsiElement> resolveStep(@NotNull PsiElement psiElement) {
         final CucumberStepsIndex index = CucumberStepsIndex.getInstance(psiElement.getProject());
 
         if (psiElement instanceof GherkinStep) {
             final GherkinStep step = (GherkinStep) psiElement;
             final List<PsiElement> result = new ArrayList<PsiElement>();
             final Set<String> substitutedNameList = step.getSubstitutedNameList();
             if (substitutedNameList.size() > 0) {
                 for (String s : substitutedNameList) {
                     final AbstractStepDefinition definition = index.findStepDefinition(psiElement.getContainingFile(), s);
                     if (definition != null) {
                         result.add(definition.getElement());
                     }
                 }
                 return result;
             }
         }
 
         return Collections.emptyList();
     }
 
     @Override
     public void findRelatedStepDefsRoots(Module module, PsiFile featureFile, List<PsiDirectory> newStepDefinitionsRoots, Set<String> processedStepDirectories) {
         PsiDirectory parent = PsiTreeUtil.getParentOfType(featureFile, PsiDirectory.class);
 
         PsiDirectory steps = null;
         if (parent != null) {
             steps = parent.findSubdirectory("steps");
         }
 
         VirtualFile virtualStepsDir = null;
 
         if (steps != null) {
             virtualStepsDir = steps.getVirtualFile();
         }
 
         if (virtualStepsDir != null && virtualStepsDir.isDirectory()) {
             PsiDirectory sourceRoot = PsiDirectoryFactory.getInstance(module.getProject()).createDirectory(virtualStepsDir);
             if (!processedStepDirectories.contains(sourceRoot.getVirtualFile().getPath())) {
                 newStepDefinitionsRoots.add(sourceRoot);
             }
         }
     }
 
     @Nullable
     public String getGlue(@NotNull GherkinStep step) {
         for (PsiReference ref : step.getReferences()) {
             PsiElement refElement = ref.resolve();
 
             if (refElement != null) {
                 PyFile pyFile = (PyFile) refElement.getContainingFile();
                 VirtualFile virtualFile = pyFile.getVirtualFile();
                 if (virtualFile != null) {
                     VirtualFile parentDir = virtualFile.getParent();
                     return PathUtil.getLocalPath(parentDir);
                 }
             }
 
         }
         return null;
     }
 
 
     @NotNull
     @Override
     public Collection<String> getGlues(@NotNull GherkinFile gherkinFile) {
         final Set<String> glues = ContainerUtil.newHashSet();
 
         gherkinFile.accept(new GherkinRecursiveElementVisitor() {
             @Override
             public void visitStep(GherkinStep step) {
                 final String glue = getGlue(step);
                 if (glue != null) {
                     glues.add(glue);
                 }
             }
         });
         return glues;
 
     }
 }
