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
 
 import com.intellij.codeInsight.lookup.*;
 import com.intellij.openapi.actionSystem.DataConstants;
 import com.intellij.openapi.actionSystem.DataContext;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.EditorModificationUtil;
 import com.intellij.openapi.editor.actionSystem.EditorAction;
 import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
 import com.intellij.openapi.util.IconLoader;
 import com.intellij.openapi.util.text.StringUtil;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.JavaDirectoryService;
 import com.intellij.psi.PsiDirectory;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiPackage;
 import org.mvnsearch.snippet.impl.mvnsearch.SnippetService;
 import org.mvnsearch.snippet.plugin.SnippetAppComponent;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * insert snippet fragment action in editor
  *
  * @author linux_china@hotmail.com
  */
 public class InsertSnippetFragmentAction extends EditorAction {
     /**
      * construct snippet fragment action
      */
     public InsertSnippetFragmentAction() {
         super(new InsertSnippetFragmentHanlder());
     }
 
     /**
      * insert snippet fragment handler
      */
     public static class InsertSnippetFragmentHanlder extends EditorWriteActionHandler {
         /**
          * execute insert logic
          *
          * @param editor      editor
          * @param dataContext data context
          */
         public void executeWriteAction(final Editor editor, DataContext dataContext) {
             // Get position before caret
             int caretOffset = editor.getCaretModel().getOffset();
             int documentOffset = caretOffset - 1;
             if (documentOffset > 0) {
                 StringBuilder prefixBuilder = new StringBuilder();
                 CharSequence charSequence = editor.getDocument().getCharsSequence();
                 for (char c; documentOffset >= 0 && isMnemonicPart(c = charSequence.charAt(documentOffset)); documentOffset--) {
                     prefixBuilder.append(c);
                 }
                 documentOffset = caretOffset;
                 StringBuilder suffixBuilder = new StringBuilder();
                 for (char c; documentOffset < charSequence.length() && isMnemonicPart(c = charSequence.charAt(documentOffset)); documentOffset++) {
                     suffixBuilder.append(c);
                 }
                 SnippetService snippetService = SnippetAppComponent.getInstance().getSnippetService();
                 if (snippetService != null) {
                     PsiFile currentPsiFile = (PsiFile) dataContext.getData(DataConstants.PSI_FILE);
                     //delete snippet mnemonic and replaced by fragment content
                     String prefix = prefixBuilder.reverse().toString();
                     String suffix = suffixBuilder.reverse().toString();
                     String mnemonic = prefix + suffix;
                     if (StringUtil.isEmpty(suffix)) {
                         editor.getSelectionModel().setSelection(caretOffset - prefix.length(), caretOffset);
                     } else {
                         editor.getSelectionModel().setSelection(caretOffset - prefix.length(), caretOffset + suffix.length());
                     }
                     boolean result = executeSnippetInsert(editor, currentPsiFile, mnemonic);
                     if (!result) { //snippet not found
                         List<String> variants = snippetService.findMnemonicList(prefix);
                         List<LookupItem<Object>> lookupItems = new ArrayList<LookupItem<Object>>();
                         for (String variant : variants) {
                             Object obj = LookupValueFactory.createLookupValue(variant, IconLoader.findIcon("/org/mvnsearch/snippet/plugin/icons/logo.png"));
                             lookupItems.add(new LookupItem<Object>(obj, variant));
                         }
                         LookupItem[] items = new LookupItem[lookupItems.size()];
                         items = lookupItems.toArray(items);
                         LookupManager lookupManager = LookupManager.getInstance(editor.getProject());
                        lookupManager.showLookup(editor, items, new LookupItemPreferencePolicyImpl(editor, currentPsiFile));
                     }
                 }
             }
         }
     }
 
     /**
      * add macro support
      *
      * @param psiFile psi file
      * @param editor  editor
      * @param rawCode rawcode
      * @return new code
      */
     private static String addMacroSupport(PsiFile psiFile, Editor editor, String rawCode) {
         String newCode = rawCode;
         VirtualFile virtualFile = psiFile.getVirtualFile();
         if (virtualFile != null) {
             String fileName = virtualFile.getName();
             newCode = newCode.replace("${FILE_NAME}", fileName);
             PsiDirectory psiDirectory = psiFile.getParent();
             if (psiDirectory != null) {
                 PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
                 if (psiPackage != null && psiPackage.getName() != null) {
                     newCode = newCode.replace("${PACKAGE_NAME}", psiPackage.getName());
                 }
             }
         }
         return newCode;
     }
 
     /**
      * validator is part of mnemonic
      *
      * @param part part character
      * @return validator result
      */
     public static boolean isMnemonicPart(char part) {
         return Character.isJavaIdentifierPart(part) || part == '-';
     }
 
     /**
      * execute snippet insert
      *
      * @param editor   editor
      * @param psiFile  current psi file
      * @param mnemonic mnemonic
      * @return success mark
      */
     public static boolean executeSnippetInsert(final Editor editor, PsiFile psiFile, String mnemonic) {
         SnippetService snippetService = SnippetAppComponent.getInstance().getSnippetService();
         String rawCode = snippetService.renderTemplate(mnemonic, null, null, SnippetAppComponent.getInstance().userName);
         if (StringUtil.isNotEmpty(rawCode)) {     //found and replace
             final String code = addMacroSupport(psiFile, editor, rawCode);
             ApplicationManager.getApplication().runWriteAction(new Runnable() {
                 public void run() {
                     EditorModificationUtil.deleteBlockSelection(editor);
                     EditorModificationUtil.insertStringAtCaret(editor, code);
                 }
             });
             return true;
         }
         return false;
     }
 
     /**
      * lookup item preference policy
      */
     private static class LookupItemPreferencePolicyImpl implements LookupItemPreferencePolicy {
         private Editor editor;
         private PsiFile psiFile;
 
         /**
          * constructure method
          *
          * @param editor  current editor
          * @param psiFile psi file
          */
         public LookupItemPreferencePolicyImpl(Editor editor, PsiFile psiFile) {
             this.editor = editor;
             this.psiFile = psiFile;
         }
 
         /**
          * execute snippet insert
          *
          * @param lookupElement lookup element
          * @param lookup        lookup object
          */
         public void itemSelected(LookupElement lookupElement, Lookup lookup) {
             executeSnippetInsert(editor, psiFile, lookupElement.getLookupString());
         }
 
         /**
          * item compare
          *
          * @param element1 element1
          * @param element2 element2
          * @return result
          */
         public int compare(LookupElement element1, LookupElement element2) {
             return element1.getLookupString().compareTo(element2.getLookupString());
         }
     }
 }
