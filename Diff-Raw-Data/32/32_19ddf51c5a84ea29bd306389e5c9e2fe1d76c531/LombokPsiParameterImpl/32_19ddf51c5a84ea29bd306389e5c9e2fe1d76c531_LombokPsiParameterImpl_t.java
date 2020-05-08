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
 package org.consulo.lombok.psi.impl.source;
 
import com.intellij.codeInsight.daemon.impl.analysis.JavaGenericsUtil;
 import com.intellij.lang.ASTNode;
 import com.intellij.psi.*;
 import com.intellij.psi.impl.source.PsiParameterImpl;
 import com.intellij.psi.util.PsiTypesUtil;
 import com.intellij.util.ArrayUtil;
import org.consulo.lombok.LombokClassNames;
 import org.consulo.lombok.processors.util.LombokUtil;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /**
  * @author VISTALL
  * @since 17:23/30.03.13
  */
 public class LombokPsiParameterImpl extends PsiParameterImpl implements LombokValOwner {
   private static final String[] ourAdditionalModifiers = new String[]{PsiModifier.FINAL};
 
   public LombokPsiParameterImpl(@NotNull ASTNode node) {
     super(node);
   }
 
   @NotNull
   @Override
   public PsiType getType() {
     PsiType type = findRightTypeIfCan();
     return type == null ? super.getType() : type;
   }
 
   @Override
   @Nullable
   public PsiType findRightTypeIfCan() {
     if(!LombokUtil.isExtensionEnabled(this)) {
       return null;
     }
     final PsiElement parent = getParent();
 
     if (parent instanceof PsiForeachStatement) {
       final PsiClass resolve = PsiTypesUtil.getPsiClass(super.getType());
       if (resolve == null || !LombokClassNames.LOMBOK_VAL.equals(resolve.getQualifiedName())) {
         return null;
       }
       final PsiExpression expression = ((PsiForeachStatement)parent).getIteratedValue();
       if (expression == null) {
         return null;
       }
      final PsiType collectionItemType = JavaGenericsUtil.getCollectionItemType(expression);
       return collectionItemType == null ? null : collectionItemType;
     }
     return null;
   }
 
   @Override
   public String[] getAdditionalModifiers() {
     return findRightTypeIfCan() == null ? ArrayUtil.EMPTY_STRING_ARRAY : ourAdditionalModifiers;
   }
 }
