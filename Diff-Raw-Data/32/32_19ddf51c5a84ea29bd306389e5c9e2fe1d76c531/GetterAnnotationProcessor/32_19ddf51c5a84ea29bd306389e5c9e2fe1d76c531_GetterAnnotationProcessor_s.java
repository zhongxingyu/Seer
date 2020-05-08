 /*
  * Copyright 2013 Consulo.org
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
 package org.consulo.lombok.processors.impl;
 
import org.consulo.lombok.processors.LombokFieldProcessor;
import org.consulo.lombok.processors.util.LombokUtil;
 import com.intellij.psi.*;
 import com.intellij.psi.impl.light.LightMethodBuilder;
 import com.intellij.psi.util.PropertyUtil;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.List;
 
 /**
  * @author VISTALL
  * @since 18:49/29.03.13
  */
 public class GetterAnnotationProcessor extends LombokFieldProcessor {
   public GetterAnnotationProcessor(String annotationClass) {
     super(annotationClass);
   }
 
   @Override
   public void processElement(@NotNull PsiClass parent, @NotNull PsiField psiField, @NotNull List<PsiElement> result) {
    LightMethodBuilder builder = new LightMethodBuilder(parent.getManager(), parent.getLanguage(), PropertyUtil.suggestGetterName(parent.getProject(), psiField));
     builder.setMethodReturnType(psiField.getType());
     builder.setContainingClass(parent);
     builder.setNavigationElement(psiField);
 
    if(psiField.hasModifierProperty(PsiModifier.STATIC)) {
       builder.addModifier(PsiModifier.STATIC);
     }
 
     PsiAnnotation annotation = getAffectedAnnotation(psiField);
 
     LombokUtil.setAccessModifierFromAnnotation(annotation, builder, PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME);
 
     result.add(builder);
   }
 
   @NotNull
   @Override
   public Class<? extends PsiElement> getCollectorPsiElementClass() {
     return PsiMethod.class;
   }
 }
