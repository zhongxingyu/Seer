 // This is a generated file. Not intended for manual editing.
 package org.dylanfoundry.deft.filetypes.dylan.psi;
 
 import java.util.List;
 import org.jetbrains.annotations.*;
 import com.intellij.psi.PsiElement;
 
 public interface DylanLiteral extends PsiElement {
 
   @Nullable
   DylanConstant getConstant();
 
   @Nullable
   DylanConstants getConstants();
 
   @Nullable
   DylanStringLiteral getStringLiteral();
 
   @Nullable
  DylanUniqueString getUniqueString();
 
 }
