 package com.maxifier.guice.jpa;
 
 import com.intellij.codeHighlighting.HighlightDisplayLevel;
 import com.intellij.codeInspection.*;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.*;
 import com.intellij.psi.util.PsiElementFilter;
 import com.intellij.psi.util.PsiTreeUtil;
 import org.jetbrains.annotations.Nls;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.maxifier.guice.jpa.GuiceJPAInspection.*;
 
 /**
  * Created by: Aleksey Didik
  * Date: 3/16/11
  * Time: 4:30 PM
  * <p/>
  * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
  * Code proprietary and confidential.
  * Use is subject to license terms.
  *
  * @author Aleksey Didik
  */
 public class EntityManagerInspection extends AbstractDBInspection {
 
     private static final String EM_TYPE = "EntityManager";
     private static final String EM_FULL_TYPE = "javax.persistence.EntityManager";
     private static final String TRANSACTION_ATTRIBUTE = "transaction";
 
 
     @NotNull
     @Override
     public String getID() {
         return "EntityManagerInspection";
     }
 
     @Override
     public String getAlternativeID() {
         return "EntityManagerInspection";
     }
 
     @Nls
     @NotNull
     @Override
     public String getGroupDisplayName() {
         return INSPECTIONS_GROUP_NAME;
     }
 
     @Nls
     @NotNull
     @Override
     public String getDisplayName() {
         return "EntityManager usage";
     }
 
     @NotNull
     @Override
     public String getShortName() {
         return "entity-manager";
     }
 
     @Override
     public boolean isEnabledByDefault() {
         return true;
     }
 
     @NotNull
     @Override
     public HighlightDisplayLevel getDefaultLevel() {
         return HighlightDisplayLevel.ERROR;
     }
 
 
     @Override
     public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
         List<ProblemDescriptor> problemDescriptors = new ArrayList<ProblemDescriptor>();
         PsiElement[] classes = PsiTreeUtil.collectElements(file, new PsiElementFilter() {
             @Override
             public boolean isAccepted(PsiElement psiElement) {
                 if (!(psiElement instanceof PsiClass)) {
                     return false;
                 }
                 PsiClass psiClass = (PsiClass) psiElement;
                 return !(psiClass.isEnum() || psiClass.isInterface());
             }
         });
         for (PsiElement psiClass : classes) {
             checkClass((PsiClass) psiClass, manager, problemDescriptors, isOnTheFly);
         }
         return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
     }
 
     private void checkClass(PsiClass psiClass, InspectionManager inspectionManager, List<ProblemDescriptor> problemDescriptors, boolean onTheFly) {
         PsiIdentifier nameIdentifier = psiClass.getNameIdentifier();
         if (nameIdentifier == null) {
             return;
         }
         final PsiField emField = checkEMField(psiClass, inspectionManager, problemDescriptors, onTheFly);
         if (emField != null) {
             checkUsageWithoutDB(psiClass, inspectionManager, problemDescriptors, emField, onTheFly);
             checkNecessityOfTransaction(psiClass, emField, inspectionManager, problemDescriptors, onTheFly);
             checkRawTransactionUsage(psiClass, emField, inspectionManager, problemDescriptors, onTheFly);
         }
         //check annotated but without usages
         checkAnnotatedWithoutUsages(psiClass, inspectionManager, problemDescriptors, emField, onTheFly);
     }
 
     private void checkRawTransactionUsage(PsiClass psiClass,
                                           PsiField emField,
                                           InspectionManager inspectionManager,
                                           List<ProblemDescriptor> problemDescriptors,
                                           boolean onTheFly) {
         for (PsiMethod psiMethod : psiClass.getMethods()) {
             PsiAnnotation dbAnnotation = getAnnotation(psiMethod, DB_NAME);
             if (dbAnnotation != null) {
                 for (PsiElement emFieldUsage : getFieldUsages(emField, psiMethod)) {
                     if (checkMethodCall(emFieldUsage, "getTransaction", "joinTransaction")) {
                         problemDescriptors.add(inspectionManager.createProblemDescriptor(
                                 emFieldUsage,
                                 "Usage of getTransaction() or joinTransaction() methods is disallowed for @DB annotated methods," +
                                         " use @DB(transaction=REQUIRED) instead.",
                                 onTheFly,
                                 LocalQuickFix.EMPTY_ARRAY,
                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                         ));
                     }
                 }
             }
         }
     }
 
     private void checkAnnotatedWithoutUsages(PsiClass psiClass,
                                              InspectionManager inspectionManager,
                                              List<ProblemDescriptor> problemDescriptors,
                                              PsiField emField,
                                              boolean onTheFly) {
         for (PsiMethod psiMethod : psiClass.getMethods()) {
             PsiAnnotation dbAnnotation = getAnnotation(psiMethod, DB_NAME);
             if (dbAnnotation != null) {
                 if (emField == null || getFieldUsages(emField, psiMethod).length == 0) {
                     problemDescriptors.add(inspectionManager.createProblemDescriptor(
                             dbAnnotation,
                             "@DB annotation is not required for this method",
                             onTheFly,
                             new LocalQuickFix[]{new DeleteAnnotationFixAction(dbAnnotation)},
                             ProblemHighlightType.LIKE_UNUSED_SYMBOL
                     ));
                 }
             }
         }
     }
 
     private void checkNecessityOfTransaction(PsiClass psiClass,
                                              final PsiField emField,
                                              InspectionManager inspectionManager,
                                              List<ProblemDescriptor> problemDescriptors,
                                              boolean onTheFly) {
         for (PsiMethod psiMethod : psiClass.getMethods()) {
             PsiAnnotation dbAnnotation = getAnnotation(psiMethod, DB_NAME);
             if (dbAnnotation != null) {
                 PsiAnnotationMemberValue transactionAttribute = dbAnnotation.findAttributeValue(TRANSACTION_ATTRIBUTE);
                 @SuppressWarnings({"ConstantConditions"})
                 boolean isTransaction = !transactionAttribute.getText().contains("NOT_REQUIRED");
                 PsiElement[] methodCalls = getFieldUsages(emField, psiMethod);
                 boolean trRequired = false;
                 for (PsiElement methodCall : methodCalls) {
                    if (checkMethodCall(methodCall, "persist", "remove", "merge")) {
                         trRequired = true;
                         if (!isTransaction)
                             problemDescriptors.add(inspectionManager.createProblemDescriptor(
                                     methodCall.getParent(),
                                     "Transaction support is required for this usage of EntityManager",
                                     onTheFly,
                                     new LocalQuickFix[]{new AddTransactionRequiredFixAction(dbAnnotation)},
                                     ProblemHighlightType.GENERIC_ERROR_OR_WARNING
 
                             ));
                     }
                 }
                 if (!trRequired && isTransaction) {
                     problemDescriptors.add(inspectionManager.createProblemDescriptor(
                             dbAnnotation.getParameterList(),
                             "Transaction support is not required for this method",
                             true,
                             new LocalQuickFix[]{new DeleteTransactionRequiredFixAction(dbAnnotation)},
                             ProblemHighlightType.LIKE_UNUSED_SYMBOL
 
                     ));
                 }
             }
         }
     }
 
 
     private void checkUsageWithoutDB(PsiClass psiClass,
                                      InspectionManager inspectionManager,
                                      List<ProblemDescriptor> problemDescriptors,
                                      final PsiField emField,
                                      boolean onTheFly) {
         for (PsiMethod psiMethod : psiClass.getMethods()) {
             if (getAnnotation(psiMethod, DB_NAME) == null && psiMethod.getBody() != null) {
                 PsiElement[] emFieldRefs = getFieldUsages(emField, psiMethod);
                 if (emFieldRefs.length != 0) {
                     //noinspection ConstantConditions
                     problemDescriptors.add(inspectionManager.createProblemDescriptor(
                             psiMethod.getNameIdentifier(),
                             "Method which use EntityManager should be annotated with @DB annotation",
                             onTheFly,
                             new LocalQuickFix[]{new AddAnnotationFixAction(psiMethod)},
                             ProblemHighlightType.GENERIC_ERROR_OR_WARNING
 
                     ));
                 }
             }
         }
     }
 
     private void checkFieldModifiers(PsiField psiField,
                                      InspectionManager inspectionManager,
                                      List<ProblemDescriptor> problemDescriptors,
                                      boolean onTheFly) {
         PsiModifierList modifierList = psiField.getModifierList();
         if (modifierList != null) {
             if (modifierList.hasModifierProperty(STATIC_MODIFIER)) {
                 PsiElement staticModifier = getModifier(modifierList, STATIC_MODIFIER);
                 problemDescriptors.add(
                         inspectionManager.createProblemDescriptor(
                                 staticModifier,
                                 "EntityManager field should not be static",
                                 new DeleteModifierFixAction(staticModifier),
                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                 onTheFly
                         )
                 );
             }
 
             if (!modifierList.hasModifierProperty(FINAL_MODIFIER)) {
                 problemDescriptors.add(
                         inspectionManager.createProblemDescriptor(
                                 psiField,
                                 "EntityManager field should be final",
                                 new AddModifierFixAction(modifierList, FINAL_MODIFIER),
                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                 true
 
                         )
                 );
             }
         }
     }
 
     private PsiField checkEMField(PsiClass psiClass,
                                   InspectionManager inspectionManager,
                                   List<ProblemDescriptor> problemDescriptors,
                                   boolean onTheFly) {
         for (PsiField psiField : psiClass.getAllFields()) {
             String typeName = psiField.getType().getCanonicalText();
             if (typeName.equals(EM_TYPE) || typeName.equals(EM_FULL_TYPE)) {
                 //noinspection ConstantConditions
                 if (psiField.getContainingClass().equals(psiClass)) {
                     checkFieldModifiers(psiField, inspectionManager, problemDescriptors, onTheFly);
                 }
                 return psiField;
             }
         }
 //        //add error that we need to have EM field
 //        //noinspection ConstantConditions
 //        problemDescriptors.add(
 //                inspectionManager.createProblemDescriptor(
 //                        psiClass.getNameIdentifier(),
 //                        "Class with @DB annotated methods should have an EntityManager field",
 //                        true,
 //                        LocalQuickFix.EMPTY_ARRAY,
 //                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
 //
 //                )
 //        );
         return null;
     }
 
     private PsiElement[] getFieldUsages(final PsiField emField, PsiMethod psiMethod) {
         return PsiTreeUtil.collectElements(psiMethod.getBody(), new PsiElementFilter() {
             @Override
             public boolean isAccepted(PsiElement element) {
                 if (element instanceof PsiMethodCallExpression) {
                     PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
                     PsiExpression qualifierExpression = methodCall.getMethodExpression().getQualifierExpression();
                     if (qualifierExpression != null) {
                         PsiReference reference = qualifierExpression.getReference();
                         if (reference != null && reference.isReferenceTo(emField)) {
                             return true;
                         }
                     }
                     PsiExpressionList argumentList = methodCall.getArgumentList();
                     for (PsiExpression psiExpression : argumentList.getExpressions()) {
                         PsiReference reference = psiExpression.getReference();
                         if ((reference != null) && reference.isReferenceTo(emField)) {
                             return true;
                         }
                     }
                 }
                 return false;
             }
         });
     }
 
 
     private boolean checkMethodCall(PsiElement fieldRef, String... methods) {
         String methodCall = fieldRef.getText();
         for (String method : methods) {
             if (methodCall.contains(method + "(")) {
                 return true;
             }
         }
         return false;
     }
 
     private static class AddAnnotationFixAction extends IntentionAndQuickFixAction {
 
         private final PsiMethod psiMethod;
 
         AddAnnotationFixAction(PsiMethod psiMethod) {
             this.psiMethod = psiMethod;
         }
 
         @NotNull
         @Override
         public String getName() {
             return "Add @DB annotation to method";
         }
 
         @NotNull
         @Override
         public String getFamilyName() {
             return "@DB fix actions";
         }
 
         @Override
         public void applyFix(Project project, PsiFile psiFile, @Nullable Editor editor) {
             psiMethod.getModifierList().addAnnotation("DB");
         }
     }
 
     private static class AddTransactionRequiredFixAction extends IntentionAndQuickFixAction {
 
         private final PsiAnnotation psiAnnotation;
 
         AddTransactionRequiredFixAction(PsiAnnotation psiAnnotation) {
             this.psiAnnotation = psiAnnotation;
         }
 
         @NotNull
         @Override
         public String getName() {
             return "Add transaction required attribute to @DB";
         }
 
         @NotNull
         @Override
         public String getFamilyName() {
             return "@DB fix actions";
         }
 
         @Override
         public void applyFix(Project project, PsiFile psiFile, @Nullable Editor editor) {
             PsiAnnotationOwner modList = psiAnnotation.getOwner();
             psiAnnotation.delete();
             PsiElement[] psiElements = PsiTreeUtil.collectElements(psiFile, new PsiElementFilter() {
                 @Override
                 public boolean isAccepted(PsiElement element) {
                     if (element instanceof PsiImportStaticStatement) {
                         PsiImportStaticStatement importStatement = (PsiImportStaticStatement) element;
                         String referenceName = importStatement.getReferenceName();
                         if (referenceName != null && referenceName.equals("REQUIRED")) {
                             return true;
                         }
                     }
                     return false;
                 }
             });
             if (psiElements.length == 0) {
                 modList.addAnnotation("DB(transaction = DB.Transaction.REQUIRED)");
             } else {
                 modList.addAnnotation("DB(transaction = REQUIRED)");
             }
         }
     }
 
     private static class DeleteTransactionRequiredFixAction extends IntentionAndQuickFixAction {
 
         private final PsiAnnotation psiAnnotation;
 
         DeleteTransactionRequiredFixAction(PsiAnnotation psiAnnotation) {
             this.psiAnnotation = psiAnnotation;
         }
 
         @NotNull
         @Override
         public String getName() {
             return "Delete transaction required attribute to @DB";
         }
 
         @NotNull
         @Override
         public String getFamilyName() {
             return "@DB fix actions";
         }
 
         @Override
         public void applyFix(Project project, PsiFile psiFile, @Nullable Editor editor) {
             PsiAnnotationOwner modList = psiAnnotation.getOwner();
             psiAnnotation.delete();
             modList.addAnnotation("DB");
         }
     }
 
     private static class DeleteAnnotationFixAction extends IntentionAndQuickFixAction {
 
         private final PsiAnnotation psiAnnotation;
 
         DeleteAnnotationFixAction(PsiAnnotation psiAnnotation) {
             this.psiAnnotation = psiAnnotation;
         }
 
         @NotNull
         @Override
         public String getName() {
             return "Delete not required annotation";
         }
 
         @NotNull
         @Override
         public String getFamilyName() {
             return "@DB fix actions";
         }
 
         @Override
         public void applyFix(Project project, PsiFile psiFile, @Nullable Editor editor) {
             psiAnnotation.delete();
         }
     }
 
 }
