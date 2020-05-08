 package org.jetbrains.plugins.xml.searchandreplace.ui.controller.replace;
 
 import com.intellij.lang.Language;
 import com.intellij.openapi.project.Project;
 import org.jetbrains.plugins.xml.searchandreplace.replace.ReplaceWithContents;
 import org.jetbrains.plugins.xml.searchandreplace.replace.ReplacementProvider;
 
 public class ReplaceWithContentsController extends CreatingXmlController {
   public ReplaceWithContentsController(Project project, Language language) {
     super(project, language);
   }
 
   @Override
   protected ReplacementProvider getReplacementProvider() {
    ReplacementProvider replacementProvider = createReplacementProviderWithMyXml();
    if (replacementProvider == null) return null;
    return new ReplaceWithContents(replacementProvider);
   }
 
   @Override
   public String toString() {
     return "Replace with contents";
   }
 }
