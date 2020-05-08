 /*
  * (C) Copyright 2013 Sébastien Richard
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.sebrichard.mfgen.inspection;
 
 import com.google.common.collect.Sets;
 import com.intellij.codeHighlighting.HighlightDisplayLevel;
 import com.intellij.codeInsight.daemon.GroupNames;
 import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
 import com.intellij.codeInspection.ProblemsHolder;
 import com.intellij.psi.*;
 import com.intellij.psi.util.PropertyUtil;
 import com.sebrichard.mfgen.MetaFieldUtil;
 import org.apache.commons.lang.StringUtils;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Set;
 
 public class InspectingMetaFields extends BaseJavaLocalInspectionTool {
     @NotNull
     public String getDisplayName() {
         return "Meta-field validations";
     }
 
     @NotNull
     public String getGroupDisplayName() {
         return GroupNames.BUGS_GROUP_NAME;
     }
 
     @NotNull
     public String getShortName() {
         return "ValidatingMetaField";
     }
 
     @NotNull
     @Override
     public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
         return new JavaElementVisitor() {
 
             @Override
             public void visitClass(PsiClass psiClass) {
                 Set<String> acceptedMetaFieldNames = generateAcceptedMetaFieldNames(psiClass);
 
                 Set<PsiField> metaFields = findMetaFields(psiClass);
 
                 for (PsiField metaField : metaFields) {
 
                     if (!metaField.hasModifierProperty(PsiModifier.FINAL)) {
                         holder.registerProblem(metaField, "Meta-field not final");
                         continue;
                     }
 
                     if (!metaField.getType().equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
                         // The field with the 'meta-field' name pattern does not have the String type!
                         holder.registerProblem(metaField, "Meta-field is not a String");
                         continue;
                     }
 
                     PsiExpression initializer = metaField.getInitializer();
                     if (initializer != null) {
                         if (initializer instanceof PsiLiteral) {
                             PsiLiteral literal = (PsiLiteral) initializer;
                             if (literal.getValue() != null && literal.getValue() instanceof String) {
                                 String initializationValue = (String) literal.getValue();
                                 if (!StringUtils.equals(metaField.getName(), MetaFieldUtil.generateMetaFieldName(initializationValue))) {
                                     holder.registerProblem(metaField, "Meta-field name does not match its value");
                                     continue;
                                 }
                             } else {
                                 holder.registerProblem(metaField, "Meta-field initializing value is not a String value");
                                 continue;
                             }
                         } else {
                             holder.registerProblem(metaField, "Meta-field initializing value is not a String constant value");
                             continue;
                         }
                     } else {
                         holder.registerProblem(metaField, "Uninitialized Meta-field");
                     }
 
                     if (!acceptedMetaFieldNames.contains(metaField.getName())) {
                         // We didn't find any matching field!
                         holder.registerProblem(metaField, "Did not find any matching non-static field.");
                         continue;
                     }
                 }
             }
 
         };
     }
 
     private Set<String> generateAcceptedMetaFieldNames(PsiClass psiClass) {
         Set<String> acceptedMetaFields = Sets.newHashSet();
 
         for (PsiField field : psiClass.getAllFields()) {
             if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                 acceptedMetaFields.add(MetaFieldUtil.generateMetaFieldName(field.getName()));
             }
         }
         for (PsiMethod method : psiClass.getAllMethods()) {
             if (!method.hasModifierProperty(PsiModifier.STATIC) && PropertyUtil.isSimplePropertyGetter(method)) {
                 String propertyName = PropertyUtil.getPropertyNameByGetter(method);
                 acceptedMetaFields.add(MetaFieldUtil.generateMetaFieldName(propertyName));
             }
         }
 
         return acceptedMetaFields;
     }
 
     private Set<PsiField> findMetaFields(PsiClass psiClass) {
         Set<PsiField> metaFields = Sets.newHashSet();
        for (PsiField metaField : psiClass.getFields()) {
             if (MetaFieldUtil.isMetaField(metaField)) {
                 metaFields.add(metaField);
             }
         }
         return metaFields;
     }
 
 
     public boolean isEnabledByDefault() {
         return true;
     }
 
     @NotNull
     @Override
     public HighlightDisplayLevel getDefaultLevel() {
         return HighlightDisplayLevel.ERROR;
     }
 
 }
