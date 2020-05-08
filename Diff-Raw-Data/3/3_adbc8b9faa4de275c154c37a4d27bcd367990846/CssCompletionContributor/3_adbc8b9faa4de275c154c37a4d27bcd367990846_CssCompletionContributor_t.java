 package org.consulo.css.editor.completion;
 
 import com.intellij.codeInsight.completion.*;
 import com.intellij.patterns.PlatformPatterns;
 import com.intellij.util.ProcessingContext;
 import org.consulo.xstylesheet.definition.XStyleSheetPropertyValuePart;
 import org.consulo.xstylesheet.psi.PsiXStyleSheetPropertyValuePart;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * @author VISTALL
  * @since 03.07.13.
  */
 public class CssCompletionContributor extends CompletionContributor {
   public CssCompletionContributor() {
     extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(PsiXStyleSheetPropertyValuePart.class), new CompletionProvider<CompletionParameters>() {
       @Override
       protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
 
         PsiXStyleSheetPropertyValuePart parent = (PsiXStyleSheetPropertyValuePart) completionParameters.getPosition().getParent();
        if(parent == null) {
          return;
        }
 
         for (XStyleSheetPropertyValuePart o : parent.getValueParts()) {
           completionResultSet.addAllElements(o.getLookupElements());
         }
       }
     });
   }
 }
