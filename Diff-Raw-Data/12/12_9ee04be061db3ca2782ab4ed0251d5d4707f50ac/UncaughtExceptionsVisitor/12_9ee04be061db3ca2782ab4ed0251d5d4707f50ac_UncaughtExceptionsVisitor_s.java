 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
  *
  * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
  * Other names may be trademarks of their respective owners.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Oracle in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 package org.netbeans.modules.javafx.editor.hints;
 
 import com.sun.javafx.api.tree.BlockExpressionTree;
 import com.sun.javafx.api.tree.CatchTree;
 import com.sun.javafx.api.tree.ExpressionTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.TryTree;
 import com.sun.tools.mjavac.code.Symbol.MethodSymbol;
 import com.sun.tools.mjavac.code.Type;
 import com.sun.tools.javafx.code.JavafxVarSymbol;
 import com.sun.tools.javafx.tree.JFXFunctionInvocation;
 import java.util.*;
 import javax.lang.model.element.Element;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.modules.javafx.editor.hints.HintsModel.Hint;
 
 /**
  *
  * @author karol harezlak
  */
 final class UncaughtExceptionsVisitor extends JavaFXTreePathScanner<Void, HintsModel> {
 
     private CompilationInfo compilationInfo;
 
     UncaughtExceptionsVisitor(CompilationInfo compilationInfo) {
         this.compilationInfo = compilationInfo;
     }
 
     @Override
     public Void visitBlockExpression(BlockExpressionTree node, HintsModel model) {
         List<ExpressionTree> statements = new ArrayList<ExpressionTree>(node.getStatements());
         statements.add(node.getValue());
         Map<Tree, Collection<Type>> throwsMap = getThrowns(statements);
         for (Tree statement : throwsMap.keySet()) {
             model.addHint(throwsMap.get(statement), statement);
         }
 
         return super.visitBlockExpression(node, model);
     }
 
     @Override
     public Void visitTry(TryTree node, HintsModel model) {
         List<ExpressionTree> statements = new ArrayList<ExpressionTree>(node.getBlock().getStatements());
         statements.add(node.getBlock().getValue());
         Map<Tree, Collection<Type>> throwsMap = getThrowns(statements);
         Map<Tree, Tree> catchMap = new HashMap<Tree, Tree>();

         for (CatchTree catchTree : node.getCatches()) {
             JavaFXTreePath path = compilationInfo.getTrees().getPath(compilationInfo.getCompilationUnit(), catchTree.getParameter());
             Element catchVarElement = compilationInfo.getTrees().getElement(path);
             Type catchType = null;
             if (catchVarElement != null) {
                 catchType = ((JavafxVarSymbol) catchVarElement).asType();
             }
             for (Tree statement : throwsMap.keySet()) {
                 if (catchType != null) {
                     Collection<Type> throwsList = new HashSet(throwsMap.get(statement));
                     for (Type type : throwsMap.get(statement)) {
                         if (type == catchType) {
                             throwsList.remove(type);
                         }
                     }
                     throwsMap.put(statement, throwsList);
                 }
                 catchMap.put(statement, catchTree);
             }
         }
         for (Tree statement : throwsMap.keySet()) {
             Hint hint = model.addHint(throwsMap.get(statement), statement);
             model.addCatchTree(hint, catchMap.get(statement));
         }
 
         return super.visitTry(node, model);
     }
 
     private Map<Tree, Collection<Type>> getThrowns(List<ExpressionTree> statements) {
         if (statements.isEmpty()) {
             return Collections.EMPTY_MAP;
         }
         Map<Tree, Collection<Type>> throwsMap = new HashMap<Tree, Collection<Type>>();
         for (ExpressionTree statement : statements) {
             if (!(statement instanceof JFXFunctionInvocation)) {
                 continue;
             }
             JavaFXTreePath path = compilationInfo.getTrees().getPath(compilationInfo.getCompilationUnit(), statement);
             Element element = compilationInfo.getTrees().getElement(path);
             if (!(element instanceof MethodSymbol)) {
                 continue;
             }
             List<Type> throwsList = ((MethodSymbol) element).getThrownTypes();
             if (throwsList == null || throwsList.isEmpty()) {
                 continue;
             }
             throwsMap.put(statement, throwsList);
         }
 
         return throwsMap;
     }
 }
