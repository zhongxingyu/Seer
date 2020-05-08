 // This is a generated file. Not intended for manual editing.
 package org.dylanfoundry.deft.filetypes.dylan.psi.impl;
 
 import java.util.List;
 import org.jetbrains.annotations.*;
 import com.intellij.lang.ASTNode;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiElementVisitor;
 import com.intellij.psi.util.PsiTreeUtil;
 import static org.dylanfoundry.deft.filetypes.dylan.psi.DylanTypes.*;
 import com.intellij.extapi.psi.ASTWrapperPsiElement;
 import org.dylanfoundry.deft.filetypes.dylan.psi.*;
 
 public class DylanLiteralImpl extends ASTWrapperPsiElement implements DylanLiteral {
 
   public DylanLiteralImpl(ASTNode node) {
     super(node);
   }
 
   @Override
   @Nullable
   public DylanConstant getConstant() {
     return findChildByClass(DylanConstant.class);
   }
 
   @Override
   @Nullable
   public DylanConstants getConstants() {
     return findChildByClass(DylanConstants.class);
   }
 
   @Override
   @Nullable
   public DylanStringLiteral getStringLiteral() {
     return findChildByClass(DylanStringLiteral.class);
   }
 
   @Override
   @Nullable
  public DylanSymbol getSymbol() {
    return findChildByClass(DylanSymbol.class);
   }
 
   public void accept(@NotNull PsiElementVisitor visitor) {
     if (visitor instanceof DylanVisitor) ((DylanVisitor)visitor).visitLiteral(this);
     else super.accept(visitor);
   }
 
 }
