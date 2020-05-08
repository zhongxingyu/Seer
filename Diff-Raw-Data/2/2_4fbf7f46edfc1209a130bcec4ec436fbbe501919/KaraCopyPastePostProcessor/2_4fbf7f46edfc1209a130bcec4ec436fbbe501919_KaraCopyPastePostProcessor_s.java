 /*
  * Copyright 2010-2013 JetBrains s.r.o.
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
 
 package org.jetbrains.kara.plugin.converter.copy;
 
 import com.intellij.codeInsight.editorActions.CopyPastePostProcessor;
 import com.intellij.codeInsight.editorActions.TextBlockTransferableData;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.RangeMarker;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Ref;
 import com.intellij.psi.PsiDocumentManager;
 import com.intellij.psi.PsiFile;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.jet.lang.psi.JetFile;
 import org.jetbrains.kara.plugin.KaraPluginOptions;
 import org.jetbrains.kara.plugin.converter.KaraHTMLConverter;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 
 public class KaraCopyPastePostProcessor implements CopyPastePostProcessor<TextBlockTransferableData> {
     private static final Logger LOG = Logger.getInstance(KaraCopyPastePostProcessor.class);
 
     @Override
     public TextBlockTransferableData collectTransferableData(final PsiFile file, final Editor editor, final int[] startOffsets, final int[] endOffsets) {
         return null;
     }
 
     private boolean containsHtml(Transferable content) {
         try {
             String text = (String) content.getTransferData(DataFlavor.stringFlavor);
             if (KaraHTMLConverter.instance$.itMayContentHTML(text)) {
                 return true;
             }
         }
         catch (Throwable e) {
             LOG.error(e);
         }
         return false;
     }
 
     @Override
     public TextBlockTransferableData extractTransferableData(Transferable content) {
         try {
             if (containsHtml(content)) {
                 String text = (String) content.getTransferData(DataFlavor.stringFlavor);
                String newText = KaraHTMLConverter.instance$.converter(text, 0);
                 return new KaraCode(newText);
             }
         }
         catch (Throwable e) {
             LOG.error(e);
         }
         return null;
     }
 
     @Override
     public void processTransferableData(final Project project, final Editor editor, final RangeMarker bounds,
                                         int caretOffset, Ref<Boolean> indented, final TextBlockTransferableData value) {
         try {
             final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
             if (!(file instanceof JetFile) || !(value instanceof KaraCode)) {
                 return;
             }
             if (allowConvert(project)) {
                 final String text = ((KaraCode) value).getData();
                 ApplicationManager.getApplication().runWriteAction(new Runnable() {
                     @Override
                     public void run() {
                         editor.getDocument().replaceString(bounds.getStartOffset(), bounds.getEndOffset(), text);
                         editor.getCaretModel().moveToOffset(bounds.getStartOffset() + text.length());
                         PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
                     }
                 });
             }
         } catch (Throwable t) {
             LOG.error(t);
         }
     }
 
     private static boolean allowConvert(@NotNull Project project) {
         KaraPluginOptions options = KaraPluginOptions.getInstance();
         if (options.isDonTShowConversionDialog()) {
             return options.isEnableHtmlToKaraConversion();
         }
 
         KaraPasteFromHtmlDialog dialog = new KaraPasteFromHtmlDialog(project);
         dialog.show();
         return dialog.isOK();
     }
 }
