 /*
  * Copyright (C) 2010 Evgeny Mandrikov
  *
  * Sonar-IDE is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar-IDE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar-IDE; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.ide.idea.inspection;
 
 import com.intellij.codeInspection.InspectionManager;
 import com.intellij.codeInspection.LocalQuickFix;
 import com.intellij.codeInspection.ProblemDescriptor;
 import com.intellij.codeInspection.ProblemHighlightType;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.PsiDocumentManager;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import org.jetbrains.annotations.Nls;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.sonar.ide.idea.utils.IdeaResourceUtils;
import org.sonar.ide.shared.violations.ViolationUtils;
import org.sonar.ide.shared.violations.ViolationsLoader;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.Violation;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class Violations extends AbstractSonarInspectionTool {
   @Nls
   @NotNull
   @Override
   public String getDisplayName() {
     return "Violations";
   }
 
   @NotNull
   @Override
   public String getShortName() {
     return "Sonar.Violations"; // TODO
   }
 
   @Override
   public String getStaticDescription() {
     return "Violations from Sonar (Checkstyle, PMD, Findbugs, ...)";
   }
 
   @Nullable
   @Override
   public List<ProblemDescriptor> checkFileBySonar(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly, Sonar sonar) {
     if (isOnTheFly) {
       return null;
     }
 
     String text = file.getText();
     Collection<Violation> violations = ViolationsLoader.getViolations(
         sonar,
         IdeaResourceUtils.getInstance().getFileKey(file),
         text
     );
 
     return buildProblemDescriptors(
         violations,
         manager,
         file,
         isOnTheFly
     );
   }
 
   @Nullable
   private List<ProblemDescriptor> buildProblemDescriptors(
       @Nullable Collection<Violation> violations,
       @NotNull InspectionManager manager,
       PsiElement element,
       boolean isOnTheFly
   ) {
     if (violations == null) {
       return null;
     }
 
     Project project = element.getProject();
     PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
     Document document = documentManager.getDocument(element.getContainingFile());
     if (document == null) {
       return null;
     }
 
     List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
     for (Violation violation : violations) {
       int line = violation.getLine() - 1;
       problems.add(manager.createProblemDescriptor(
           element,
           getTextRange(document, line),
           ViolationUtils.getDescription(violation),
           ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
           isOnTheFly,
           LocalQuickFix.EMPTY_ARRAY
       ));
     }
     return problems;
   }
 }
