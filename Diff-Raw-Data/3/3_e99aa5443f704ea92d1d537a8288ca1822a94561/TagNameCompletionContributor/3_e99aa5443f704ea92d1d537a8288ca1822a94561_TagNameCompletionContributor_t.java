 package org.jetbrains.plugins.xml.searchandreplace.ui.view.search;
 
 import com.intellij.codeInsight.completion.*;
 import com.intellij.codeInsight.lookup.LookupElementBuilder;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.patterns.ElementPattern;
 import com.intellij.patterns.ElementPatternCondition;
 import com.intellij.patterns.PlatformPatterns;
 import com.intellij.psi.PsiFile;
 import com.intellij.util.ProcessingContext;
 import com.intellij.xml.index.XmlTagNamesIndex;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.Collection;
 
 public class TagNameCompletionContributor extends CompletionContributor {
 
 
   public static final String TAG_NAME_COMPLETION_WORKS = "TAG_NAME_COMPLETION_WORKS";
 
   public TagNameCompletionContributor() {
     extend(CompletionType.BASIC, PlatformPatterns.psiElement().inFile(new ElementPattern<PsiFile>() {
       @Override
       public boolean accepts(@Nullable Object o) {
         return o instanceof PsiFile && doesTagNameCompletionWork((PsiFile)o);
       }
 
       @Override
       public boolean accepts(@Nullable Object o, ProcessingContext context) {
         return accepts(o);
       }
 
       @Override
       public ElementPatternCondition<PsiFile> getCondition() {
         return null;
       }
     }), new CompletionProvider<CompletionParameters>() {
       @Override
       protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
        String prefix = parameters.getOriginalFile().getText().substring(0, parameters.getOffset()).trim();
        result = result.withPrefixMatcher(prefix);

         final Collection<String>[] allTagNames = new Collection[1];
         ApplicationManager.getApplication().runReadAction(new Runnable() {
           @Override
           public void run() {
             allTagNames[0] = XmlTagNamesIndex.getAllTagNames(parameters.getOriginalFile().getProject());
           }
         });
         for (String tagName : allTagNames[0]) {
           result.addElement(LookupElementBuilder.create(tagName));
         }
       }
     });
   }
 
   static boolean doesTagNameCompletionWork(PsiFile file) {
     return file.getName().indexOf(TAG_NAME_COMPLETION_WORKS) != -1;
   }
 }
