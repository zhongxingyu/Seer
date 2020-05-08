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
 
 import com.sun.javafx.api.tree.ExpressionTree;
 import com.sun.javafx.api.tree.FunctionInvocationTree;
 import com.sun.javafx.api.tree.IdentifierTree;
import com.sun.javafx.api.tree.InstantiateTree;
import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.MemberSelectTree;
 import com.sun.javafx.api.tree.ObjectLiteralPartTree;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Symbol.TypeSymbol;
 import com.sun.tools.javac.code.Type;
import com.sun.tools.javafx.api.JavafxcTrees;
 import com.sun.tools.javafx.tree.JFXIdent;
 import java.util.Set;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.TypeElement;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.modules.javafx.refactoring.impl.javafxc.TreePathHandle;
 
 /**
  *
  * @author Jaroslav Bachorik
  */
 public class FindUsagesScanner extends BaseRefactoringScanner<Void, Set<TreePathHandle>> {
     private String targetSimpleName;
     private String targetQualName;
 
     public FindUsagesScanner(TreePathHandle handle, CompilationController cc) {
         this(handle, ElementHandle.create(handle.resolveElement(cc)), cc);
     }
 
     public FindUsagesScanner(TreePathHandle handle, ElementHandle elementHandle, CompilationController cc) {
         super(handle, elementHandle, cc);
         this.targetSimpleName = handle.getSimpleName();
         this.targetQualName = (elementHandle.getKind() == ElementKind.CLASS || elementHandle.getKind() == ElementKind.INTERFACE || elementHandle.getKind() == ElementKind.OTHER) ? elementHandle.getQualifiedName() : "";
     }
 
     @Override
     public Void visitMethodInvocation(FunctionInvocationTree node, Set<TreePathHandle> handles) {
         if (getElementKind() != ElementKind.METHOD) return super.visitMethodInvocation(node, handles);
 
         if (isSameElement()) {
             handles.add(TreePathHandle.create(getCurrentPath(), getCompilationController()));
         }
 
         return super.visitMethodInvocation(node, handles);
     }
 
 
     @Override
     public Void visitMemberSelect(MemberSelectTree node, Set<TreePathHandle> handles) {
         if (!node.getIdentifier().contentEquals(targetSimpleName)) return super.visitMemberSelect(node, handles);
         if (getElementKind() == ElementKind.CLASS || getElementKind() == ElementKind.INTERFACE) {
             Element e = getCompilationController().getTrees().getElement(getCurrentPath());
             if (e.getKind() == getElementKind()) {
                 if (((TypeElement)e).getQualifiedName().contentEquals(targetQualName)) {
                     handles.add(TreePathHandle.create(getCurrentPath(), getCompilationController()));
                 }
             }
         } else {
             ExpressionTree expression = node.getExpression();
             if (expression instanceof JFXIdent) {
                 Type type = ((JFXIdent)expression).type;
                 if (type == null) return super.visitMemberSelect(node, handles);
                 TypeSymbol ts = type.asElement();
                 if (ts.getKind() != ElementKind.CLASS) return super.visitMemberSelect(node, handles);
                 for(Symbol sy : ts.getEnclosedElements()) {
                     if (sy.getKind() == ElementKind.FIELD) {
                         if (getElementHandle() != null && getElementHandle().equals(ElementHandle.create(sy))) {
                             handles.add(TreePathHandle.create(getCurrentPath(), getCompilationController()));
                             return null;
                         }
                     }
                 }
             }
         }
         return super.visitMemberSelect(node, handles);
     }
 
     @Override
     public Void visitIdentifier(IdentifierTree node, Set<TreePathHandle> handles) {
         switch (getElementKind()) {
             case FIELD:
             case PARAMETER:
             case LOCAL_VARIABLE:
             case CLASS:
             case INTERFACE: {
                 if (isSameElement()) {
                     handles.add(TreePathHandle.create(getCurrentPath(), getCompilationController()));
                 }
                 break;
             }
         }
         return super.visitIdentifier(node, handles);
     }
 
     @Override
     public Void visitObjectLiteralPart(ObjectLiteralPartTree node, Set<TreePathHandle> handles) {
         switch (getElementKind()) {
             case FIELD:
             case PARAMETER:
             case LOCAL_VARIABLE: {
                 if (isSameElement()) {
                     handles.add(TreePathHandle.create(getCurrentPath(), getCompilationController()));
                 }
             }
         }
         return super.visitObjectLiteralPart(node, handles);
     }

    @Override
    public Void visitInstantiate(InstantiateTree node, Set<TreePathHandle> handles) {
        switch (getElementKind()) {
            case CLASS: {
                JavaFXTreePath path = JavafxcTrees.getPath(getCurrentPath(), node.getIdentifier());
                if (isSameElement(path)) {
                    handles.add(TreePathHandle.create(path, getCompilationController()));
                }
            }
        }
        return super.visitInstantiate(node, handles);
    }


 }
