 package net.risimo.nullannotationinspection;
 
 import com.intellij.codeHighlighting.HighlightDisplayLevel;
 import com.intellij.codeInsight.NullableNotNullManager;
 import com.intellij.codeInsight.intention.impl.AddNotNullAnnotationFix;
 import com.intellij.codeInsight.intention.impl.AddNullableAnnotationFix;
 import com.intellij.codeInspection.InspectionManager;
 import com.intellij.codeInspection.LocalQuickFix;
 import com.intellij.codeInspection.ProblemDescriptor;
 import com.intellij.codeInspection.ProblemHighlightType;
 import com.intellij.codeInspection.ex.BaseLocalInspectionTool;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.*;
 import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
 import org.jetbrains.annotations.Nls;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * User: rfalke
  * Date: 2012-11-20
  */
 public class NullAnnotationsInspection extends BaseLocalInspectionTool {
     @Nls
     @NotNull
     public String getGroupDisplayName() {
         return "Code style issues";
     }
 
     @Nls
     @NotNull
     public String getDisplayName() {
         return "Missing @Nullable/@Nonnull annotation";
     }
 
     @NonNls
     @NotNull
     public String getShortName() {
         return "MissingNullableNonnullAnnotation";
     }
 
 
     @Override
     @NotNull
     public HighlightDisplayLevel getDefaultLevel() {
         return HighlightDisplayLevel.WARNING;
     }
 
     @Override
     public boolean isEnabledByDefault() {
         return true;
     }
 
     @Override
     public ProblemDescriptor[] checkMethod(@NotNull PsiMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
         final List<ProblemDescriptor> problemDescriptors = new ArrayList<ProblemDescriptor>();
         if (acceptMethod(method)) {
             List<MethodSignatureBackedByPsiMethod> superMethodSignatures = findSuperMethods(method);
             final PsiParameter[] parameters = method.getParameterList().getParameters();
             for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
                 PsiParameter parameter = parameters[i];
                 if (parameterNeedsAnnotation(parameter)) {
                     if (!hasAnnotation(parameter) && !superMethodHasAnnotation(i, superMethodSignatures)) {
                         final LocalQuickFix[] localQuickFixes = {new AddNullableAnnotationFix(parameter), new AddNotNullAnnotationFix(parameter)};
                        final ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                                parameter,
                                "Missing @Nullable/@Nonnull annotation",
                                localQuickFixes,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, false);
                        problemDescriptors.add(problemDescriptor);
                     }
                 }
             }
             if (!method.isConstructor() && !(method.getReturnType() instanceof PsiPrimitiveType) && !hasAnnotation(method)) {
                 final LocalQuickFix[] localQuickFixes = {new AlwaysEnabledAddNullableAnnotationFix(method), new AlwaysEnabledAddNotNullAnnotationFix(method)};
                 final PsiTypeElement returnTypeElement = method.getReturnTypeElement();
                 if (returnTypeElement == null) {
                     throw new RuntimeException("Bad method " + method);
                 }
                final ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                        returnTypeElement,
                        "Missing @Nullable/@Nonnull annotation",
                        localQuickFixes,
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, false);
                problemDescriptors.add(problemDescriptor);
             }
         }
 
         return problemDescriptors.isEmpty() ? null : problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
     }
 
     private boolean parameterNeedsAnnotation(PsiParameter parameter) {
         return !(parameter.getType() instanceof PsiPrimitiveType) && !parameter.isVarArgs();
     }
 
     private boolean superMethodHasAnnotation(int parameter, List<MethodSignatureBackedByPsiMethod> superMethodSignatures) {
         for (MethodSignatureBackedByPsiMethod methodSignature : superMethodSignatures) {
             PsiMethod superMethod = methodSignature.getMethod();
             PsiParameter[] superParameters = superMethod.getParameterList().getParameters();
             PsiParameter superParameter = superParameters[parameter];
             if (hasAnnotation(superParameter)) {
                 return true;
             }
         }
         return false;
     }
 
     private List<MethodSignatureBackedByPsiMethod> findSuperMethods(PsiMethod method) {
         final List<MethodSignatureBackedByPsiMethod> signatures = method.findSuperMethodSignaturesIncludingStatic(true);
         final Iterator<MethodSignatureBackedByPsiMethod> iter = signatures.iterator();
         while (iter.hasNext()) {
             final MethodSignatureBackedByPsiMethod superSignature = iter.next();
             if (superSignature.getMethod().getParameterList().getParametersCount() != method.getParameterList().getParametersCount()) {
                 iter.remove();
             }
         }
         return signatures;
     }
 
     private boolean acceptMethod(@NotNull PsiMethod method) {
         return !method.hasModifierProperty(PsiModifier.PRIVATE);
     }
 
     private boolean hasAnnotation(PsiModifierListOwner psiModifierListOwner) {
         return NullableNotNullManager.isNullable(psiModifierListOwner) || NullableNotNullManager.isNotNull(psiModifierListOwner);
     }
 
     private static class AlwaysEnabledAddNullableAnnotationFix extends AddNullableAnnotationFix {
         public AlwaysEnabledAddNullableAnnotationFix(@NotNull PsiModifierListOwner modifierListOwner) {
             super(modifierListOwner);
         }
 
         @Override
         public boolean isAvailable(@NotNull Project project,
                                    @NotNull PsiFile file,
                                    @NotNull PsiElement startElement,
                                    @NotNull PsiElement endElement) {
             return true;
         }
     }
 
     private class AlwaysEnabledAddNotNullAnnotationFix extends AddNotNullAnnotationFix {
         public AlwaysEnabledAddNotNullAnnotationFix(@NotNull PsiModifierListOwner modifierListOwner) {
             super(modifierListOwner);
         }
 
         @Override
         public boolean isAvailable(@NotNull Project project,
                                    @NotNull PsiFile file,
                                    @NotNull PsiElement startElement,
                                    @NotNull PsiElement endElement) {
             return true;
         }
     }
 }
