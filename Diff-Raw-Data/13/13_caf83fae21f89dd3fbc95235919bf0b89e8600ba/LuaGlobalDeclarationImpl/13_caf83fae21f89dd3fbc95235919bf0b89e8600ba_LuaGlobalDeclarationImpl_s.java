 /*
  * Copyright 2011 Jon S Akhtar (Sylvanaar)
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
 
 package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;
 
 import com.intellij.lang.ASTNode;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiElementVisitor;
 import com.intellij.psi.ResolveState;
 import com.intellij.psi.StubBasedPsiElement;
 import com.intellij.psi.scope.PsiScopeProcessor;
 import com.intellij.psi.stubs.IStubElementType;
 import com.intellij.util.IncorrectOperationException;
 import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
 import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
 import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaDeclarationExpression;
 import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
 import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementFactoryImpl;
 import com.sylvanaar.idea.Lua.lang.psi.impl.LuaStubElementBase;
 import com.sylvanaar.idea.Lua.lang.psi.stubs.api.LuaGlobalDeclarationStub;
 import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalDeclaration;
 import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobalIdentifier;
 import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
 import com.sylvanaar.idea.Lua.lang.psi.types.LuaType;
 import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Jon S Akhtar
  * Date: 1/15/11
  * Time: 1:31 AM
  */
 public class LuaGlobalDeclarationImpl extends LuaStubElementBase<LuaGlobalDeclarationStub>
         implements LuaGlobalDeclaration, StubBasedPsiElement<LuaGlobalDeclarationStub> {
     public LuaGlobalDeclarationImpl(ASTNode node) {
         super(node);
     }
 
     @Override
     public PsiElement getParent() {
         return getParentByTree();
     }
 
     public LuaGlobalDeclarationImpl(LuaGlobalDeclarationStub stub) {
         super(stub, LuaElementTypes.GLOBAL_NAME_DECL);
     }
 
     @Override
     public String toString() {
         return "Global Decl: " + ((getStub() != null) ? getStub().getName() : getText());
     }
 
     @Override
     public String getDefinedName() {
 //        final LuaGlobalDeclarationStub stub = (LuaGlobalDeclarationStub) getStub();
 //        if (stub != null) {
 //            return stub.getName();
 //        }
 //
         return getName();
     }
 
 
     @Override
     public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                        @NotNull ResolveState state, PsiElement lastParent,
                                        @NotNull PsiElement place) {
         return processor.execute(this, state);
     }
 
     @Override
     public String getName() {
         String module = ((LuaPsiFile) getContainingFile()).getModuleName();
         module = module == null ? "" : module;
         final LuaGlobalDeclarationStub stub = (LuaGlobalDeclarationStub) getStub();
         if (stub != null) {
             return module + stub.getName();
         }
 
         return module + super.getName();    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     public void accept(LuaElementVisitor visitor) {
         visitor.visitDeclarationExpression(this);
     }
 
     @Override
     public void accept(@NotNull PsiElementVisitor visitor) {
         if (visitor instanceof LuaElementVisitor) {
             ((LuaElementVisitor) visitor).visitDeclarationExpression(this);
         } else {
             visitor.visitElement(this);
         }
     }
 
     @Override
     public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
 
     @Override
     public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
         LuaDeclarationExpression decl = LuaPsiElementFactoryImpl.getInstance(getProject()).createGlobalNameIdentifierDecl(name);
 
         return replace(decl);
     }
 
     @NotNull
     @Override
     public IStubElementType getElementType() {
         return LuaElementTypes.GLOBAL_NAME_DECL;
     }
 
     @Override
     public boolean isSameKind(LuaSymbol symbol) {
         return symbol instanceof LuaGlobalIdentifier;
     }
 
     @Override
     public boolean isAssignedTo() {
         return true;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public LuaType getLuaType() {
         return LuaType.ANY;
     }
 }
