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
  *  accompanied this code. If applicable, add the following below the
  *  License Header, with the fields enclosed by brackets [] replaced by
  *  your own identifying information:
  *  "Portions Copyrighted [year] [name of copyright owner]"
  * 
  *  Contributor(s):
  * 
  *  Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.refactoring.impl.scanners;
 
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.Tree;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.modules.javafx.refactoring.impl.javafxc.TreePathHandle;
 
 /**
  *
  * @author Jaroslav Bachorik
  */
 abstract public class BaseRefactoringScanner<R, P> extends JavaFXTreePathScanner<R, P>{
     final private TreePathHandle searchHandle;
     final private Element origElement;
     final private ElementHandle origHandle;
     final private ElementKind origKind;
     final private CompilationController cc;
     final private SourcePositions positions;
     
     /**
      * 
      * @param searchHandle A {@linkplain TreePathHandle} instance to rename from (if "eh" not provided it is resolved from "searchHandle")
      * @param eh If provided (not NULL) this takes precedence over the searchHandle
      * @param cc {@linkplain CompilationController} instance
      */
     public BaseRefactoringScanner(TreePathHandle searchHandle, ElementHandle eh, CompilationController cc) {
         this.searchHandle = searchHandle;
         if (eh != null) {
             this.origHandle = eh;
             this.origElement = eh.resolve(cc);
             this.origKind = eh.getKind();
         } else {
             this.origElement = searchHandle.resolveElement(cc);
             this.origHandle = ElementHandle.create(origElement);
             this.origKind = origElement != null ? origElement.getKind() : null;
         }
         this.cc = cc;
         this.positions = cc.getTrees().getSourcePositions();
     }
 
     public BaseRefactoringScanner(TreePathHandle searchHandle, CompilationController cc) {
         this(searchHandle, null, cc);
     }
     
     public BaseRefactoringScanner(CompilationController cc) {
         this(null, null, cc);
     }
 
     @Override
     final public R scan(Tree tree, P p) {
        if (tree != null) { // unbelievable, but can happen
            long start = positions.getStartPosition(getCompilationController().getCompilationUnit(), tree);
            long end =  positions.getEndPosition(getCompilationController().getCompilationUnit(), tree);
            if (end == start &&
                tree.getJavaFXKind() != Tree.JavaFXKind.PARENTHESIZED) // this is a workaround for javafxc bug setting PARENTHESIZED positions such as start == end
                return null;
        }
         return super.scan(tree, p);
     }
 
     final protected TreePathHandle getTreePathHandle() {
         return searchHandle;
     }
 
     final protected ElementHandle getElementHandle() {
         return origHandle;
     }
 
     final protected ElementKind getElementKind() {
         return origKind;
     }
 
     final protected CompilationController getCompilationController() {
         return cc;
     }
 
     final protected boolean isSameElement() {
         return isSameElement(getCurrentPath());
     }
 
     final protected boolean isSameElement(JavaFXTreePath path) {
         Element el = getCompilationController().getTrees().getElement(path);
         if (el != null) {
             return el == origElement;
         }
         return false;
     }
 }
