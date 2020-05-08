 /*
  *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  *  Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
  * 
  *  The contents of this file are subject to the terms of either the GNU
  *  General Public License Version 2 only ("GPL") or the Common
  *  Development and Distribution License("CDDL") (collectively, the
  *  "License"). You may not use this file except in compliance with the
  *  License. You can obtain a copy of the License at
  *  http://www.netbeans.org/cddl-gplv2.html
  *  or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  *  specific language governing permissions and limitations under the
  *  License.  When distributing the software, include this License Header
  *  Notice in each file and include the License file at
  *  nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  *  particular file as subject to the "Classpath" exception as provided
  *  by Sun in the GPL Version 2 section of the License file that
  *  agetCompilationController()ompanied this code. If applicable, add the following below the
  *  License Header, with the fields enclosed by brackets [] replaced by
  *  your own identifying information:
  *  "Portions Copyrighted [year] [name of copyright owner]"
  * 
  *  Contributor(s):
  * 
  *  Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.refactoring.impl.scanners;
 
 import com.sun.javafx.api.tree.ClassDeclarationTree;
 import com.sun.javafx.api.tree.ExpressionTree;
 import com.sun.javafx.api.tree.FunctionDefinitionTree;
 import com.sun.javafx.api.tree.FunctionInvocationTree;
 import com.sun.javafx.api.tree.IdentifierTree;
 import com.sun.javafx.api.tree.ImportTree;
 import com.sun.javafx.api.tree.InstantiateTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.MemberSelectTree;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.TypeClassTree;
 import com.sun.javafx.api.tree.VariableTree;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Symbol.TypeSymbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javafx.api.JavafxcTrees;
 import com.sun.tools.javafx.tree.JFXIdent;
 import com.sun.tools.javafx.tree.JFXVarScriptInit;
 import java.util.Collection;
 import java.util.Set;
 import java.util.regex.Pattern;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.TypeElement;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.modules.javafx.refactoring.impl.javafxc.SourceUtils;
 import org.netbeans.modules.javafx.refactoring.impl.javafxc.TreePathHandle;
 
 /**
  *
  * @author Jaroslav Bachorik
  */
 public class RenameScanner extends BaseRefactoringScanner<Void, Set<TreePathHandle>> {
     final private static String TYPE_MATCH_PATTERN = "(\\..+)*(\\[\\])*";
     final private String origSimpleName;
     final private String origQualName;
     final private ElementKind origKind;
     private ElementHandle origHandle;
 
     public RenameScanner(String simpleName, String qualName, ElementKind origKind, CompilationController cc) {
         super(null, cc);
         this.origKind = origKind;
         this.origQualName = qualName;
         this.origSimpleName = simpleName;
     }
 
     public RenameScanner(String simpleName, ElementHandle handle, CompilationController cc) {
         this(simpleName, (handle.getKind() == ElementKind.CLASS || handle.getKind() == ElementKind.INTERFACE) ? handle.getQualifiedName().toString() : "", handle.getKind(), cc);
         this.origHandle = handle;
     }
 
     @Override
     public Void visitClassDeclaration(ClassDeclarationTree node, Set<TreePathHandle> p) {
         long[] namePos = getCompilationController().getTreeUtilities().findNameSpan(node);
 
         if (namePos == null) return super.visitClassDeclaration(node, p); // the name is not in the source => synthetically generated class declaration
 
         switch (origKind) {
             case CLASS:
             case INTERFACE: {
                 TypeElement te = (TypeElement)getCompilationController().getTrees().getElement(getCurrentPath());
                 if (Pattern.matches(origSimpleName + TYPE_MATCH_PATTERN, te.getSimpleName().toString())) {
                     p.add(TreePathHandle.create(getCurrentPath(), getCompilationController()));
                 }
                 for(ExpressionTree et : node.getSupertypeList()) {
                     JavaFXTreePath path = JavafxcTrees.getPath(getCurrentPath(), et);
                     te = (TypeElement)getCompilationController().getTrees().getElement(path);
                     if (Pattern.matches(origQualName + TYPE_MATCH_PATTERN, te.getQualifiedName().toString())) {
                         p.add(TreePathHandle.create(path, getCompilationController()));
                     }
                 }
                 break;
             }
         }
         return super.visitClassDeclaration(node, p);
     }
 
     @Override
     public Void visitInstantiate(InstantiateTree node, Set<TreePathHandle> p) {
         switch (origKind) {
             case CLASS:
             case INTERFACE: {
                 TypeElement te = (TypeElement)getCompilationController().getTrees().getElement(JavafxcTrees.getPath(getCurrentPath(), node.getIdentifier()));
                 String typeName = te.getQualifiedName().toString();
                 if (Pattern.matches(origQualName + TYPE_MATCH_PATTERN, typeName)) {
                     p.add(TreePathHandle.create(JavafxcTrees.getPath(getCurrentPath(), node.getIdentifier()), getCompilationController()));
                     return null;
                 }
                 break;
             }
         }
         return super.visitInstantiate(node, p);
     }
 
     @Override
     public Void visitTypeClass(TypeClassTree node, Set<TreePathHandle> p) {
         switch(origKind) {
             case CLASS:
             case INTERFACE: {
                 TypeElement te = (TypeElement)getCompilationController().getTrees().getElement(getCurrentPath());
                 String typeName = te.getQualifiedName().toString();
                 if (Pattern.matches(origQualName + TYPE_MATCH_PATTERN, typeName)) {
                     p.add(TreePathHandle.create(JavafxcTrees.getPath(getCurrentPath(), node.getClassName()), getCompilationController()));
                     return null;
                 }
                 break;
             }
         }
         return super.visitTypeClass(node, p);
     }
 
     @Override
     public Void visitImport(ImportTree node, Set<TreePathHandle> p) {
         switch (origKind) {
             case CLASS:
             case INTERFACE: {
                 String qualName = node.getQualifiedIdentifier().toString();
                 if (qualName.equals(origQualName)) {
                     p.add(TreePathHandle.create(JavafxcTrees.getPath(getCurrentPath(), node.getQualifiedIdentifier()), getCompilationController()));
                     return null;
                 }
                 break;
             }
         }
 
         return super.visitImport(node, p);
     }
 
     @Override
     public Void visitVariable(VariableTree node, Set<TreePathHandle> p) {
         Element e = getCompilationController().getTrees().getElement(getCurrentPath());
 
         switch (e.getKind()) {
             case LOCAL_VARIABLE:
             case PARAMETER:
             case FIELD: {
                 if (node.getName().contentEquals(origSimpleName)) {
                     if (node instanceof JFXVarScriptInit) {
                         p.add(TreePathHandle.create(JavaFXTreePath.getPath(getCompilationController().getCompilationUnit(), ((JFXVarScriptInit)node).getVar()), getCompilationController()));
                     } else {
                         p.add(TreePathHandle.create(getCurrentPath(), getCompilationController()));
                     }
                     return null;
                 }
                 break;
             }
         }
         
         return super.visitVariable(node, p);
     }
 
     @Override
     public Void visitFunctionDefinition(FunctionDefinitionTree node, Set<TreePathHandle> p) {
         if (origKind != ElementKind.METHOD) return super.visitFunctionDefinition(node, p);
         ExecutableElement e = (ExecutableElement)getCompilationController().getTrees().getElement(getCurrentPath());
         ElementHandle eh = ElementHandle.create(e);
 
         if (eh != null && (eh.equals(origHandle) || getCompilationController().getElements().overrides(e, (ExecutableElement)origHandle.resolve(getCompilationController()), (TypeElement)e.getEnclosingElement()))) {
             p.add(TreePathHandle.create(getCurrentPath(), getCompilationController()));
             return null;
         }
         return super.visitFunctionDefinition(node, p);
     }
 
     @Override
     public Void visitMethodInvocation(FunctionInvocationTree node, Set<TreePathHandle> p) {
         if (origKind != ElementKind.METHOD) return super.visitMethodInvocation(node, p);
         Element e = getCompilationController().getTrees().getElement(getCurrentPath());
         ExecutableElement ee = (ExecutableElement)e;
         if (origHandle != null && origHandle.equals(ElementHandle.create(ee))) {
             p.add(TreePathHandle.create(getCurrentPath(), getCompilationController()));
             return null;
         }
         
         return super.visitMethodInvocation(node, p);
     }
 
     @Override
     public Void visitMemberSelect(MemberSelectTree node, Set<TreePathHandle> p) {
         ExpressionTree expression = node.getExpression();
         if (expression instanceof JFXIdent) {
             Type type = ((JFXIdent)expression).type;
             if (type == null) return super.visitMemberSelect(node, p);
             TypeSymbol ts = type.asElement();
             if (ts.getKind() != ElementKind.CLASS) return super.visitMemberSelect(node, p);
             for(Symbol sy : ts.getEnclosedElements()) {
                 if (sy.getKind() == ElementKind.FIELD) {
                     if (origHandle != null && origHandle.equals(ElementHandle.create(sy))) {
                         p.add(TreePathHandle.create(JavafxcTrees.getPath(getCurrentPath(), expression), getCompilationController()));
                         return null;
                     }
                 }
             }
         }
         return super.visitMemberSelect(node, p);
     }
 
     @Override
     public Void visitIdentifier(IdentifierTree node, Set<TreePathHandle> p) {
         switch (origKind) {
             case FIELD:
             case PARAMETER:
             case LOCAL_VARIABLE: {
                 if (node.getName().contentEquals(origSimpleName)) {
                     p.add(TreePathHandle.create(getCurrentPath(), getCompilationController()));
                     return null;
                 }
                 break;
             }
         }
         return super.visitIdentifier(node, p);
     }
 }
