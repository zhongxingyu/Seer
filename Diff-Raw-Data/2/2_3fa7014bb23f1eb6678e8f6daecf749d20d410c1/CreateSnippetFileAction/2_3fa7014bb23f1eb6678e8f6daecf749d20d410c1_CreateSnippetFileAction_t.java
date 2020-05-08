 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.mvnsearch.snippet.plugin.actions;
 
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.actionSystem.PlatformDataKeys;
 import com.intellij.openapi.actionSystem.LangDataKeys;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.ui.DialogBuilder;
 import com.intellij.openapi.util.text.StringUtil;
 import com.intellij.psi.*;
 import com.intellij.util.FileContentUtil;
 import org.mvnsearch.snippet.plugin.SnippetAppComponent;
 import org.mvnsearch.snippet.impl.mvnsearch.SnippetService;
 import org.mvnsearch.snippet.plugin.ui.SnippetFileCreationForm;
 
 /**
  * action to create a new file from snippet file
  *
  * @author linux_china@hotmail.com
  */
 public class CreateSnippetFileAction extends AnAction {
     public void actionPerformed(AnActionEvent event) {
         final Project project = event.getData(PlatformDataKeys.PROJECT);
         final PsiElement psiElement = event.getData(LangDataKeys.PSI_ELEMENT);
         if (psiElement instanceof PsiDirectory) {
             final SnippetFileCreationForm form = new SnippetFileCreationForm();
             final DialogBuilder builder = new DialogBuilder(project);
             builder.setTitle("Create file from snippet");
             builder.setCenterPanel(form.getRootPanel());
             builder.setOkOperation(new Runnable() {
                 public void run() {
                     String result = generateFile((PsiDirectory) psiElement, form.getMnemonic(), form.getFileName(), project);
                     if (StringUtil.isEmpty(result)) { //create successfully
                         builder.getDialogWrapper().close(1);
                     } else {   //failed
                         builder.setTitle(result);
                     }
                 }
             });
             builder.showModal(true);
         }
     }
 
     /**
      * generate file from snippet
      *
      * @param psiDirectory directory
      * @param mnemonic     mnemonic
      * @param fileName     file name
      * @param project      project
      * @return error information
      */
     public String generateFile(final PsiDirectory psiDirectory, final String mnemonic, final String fileName, final Project project) {
         final StringBuffer buffer = new StringBuffer();
         ApplicationManager.getApplication().runWriteAction(new Runnable() {
             public void run() {
                 try {
                     SnippetService service = SnippetAppComponent.getInstance().getSnippetService();
                     if (service != null) {
                         PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
                         String packageName = psiPackage == null ? null : psiPackage.getName();
                         String content = service.renderTemplate(mnemonic, packageName, fileName, SnippetAppComponent.getInstance().userName);
                         if (content == null) {
                             buffer.append("Mnemonic not found!");
                             return;
                         }
                        //\r is forbidden for Intellij document
                        content = content.replaceAll("\r","");
                         PsiFile destinationFile = psiDirectory.findFile(fileName);
                         if (destinationFile == null) {
                             destinationFile = psiDirectory.createFile(fileName);
                             FileContentUtil.setFileText(project, destinationFile.getVirtualFile(), content);
                         }
                         destinationFile.navigate(true);
                     } else {
                         buffer.append("Cannot create connection to snippet repository!");
                     }
                 } catch (Exception e) {
                     buffer.append("Failed to create file");
                 }
             }
         });
         return buffer.toString();
     }
 }
 
