 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
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
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.editor.completion.environment;
 
 import com.sun.javafx.api.tree.ExpressionTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.TryTree;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import com.sun.tools.javafx.tree.JFXType;
 import com.sun.tools.javafx.tree.JFXVar;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.tools.Diagnostic;
 import org.netbeans.modules.javafx.editor.completion.JavaFXCompletionEnvironment;
 import static org.netbeans.modules.javafx.editor.completion.JavaFXCompletionQuery.*;
 
 /**
  *
  * @author David Strupl
  */
 public class FunctionDefinitionEnvironment extends JavaFXCompletionEnvironment<JFXFunctionDefinition> {
     
     private static final Logger logger = Logger.getLogger(FunctionDefinitionEnvironment.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
 
     @Override
     protected void inside(JFXFunctionDefinition t) throws IOException {
         if (LOGGABLE) log("inside JFXFunctionDefinition " + t);
         JFXFunctionDefinition def = t;
         int startPos = (int) sourcePositions.getStartPosition(root, def);
         JFXType retType = def.getJFXReturnType();
         if (LOGGABLE) log("  offset == " + offset + "  startPos == " + startPos + " retType == " + retType);
         String headerText = controller.getText().substring(startPos, offset > startPos ? offset : startPos);
         if (LOGGABLE) log("  headerText(1) == " + headerText);
         int parStart = headerText.indexOf('(');
         if (LOGGABLE) log("  parStart: " + parStart);
         if (parStart >= 0) {
             int parEnd = headerText.indexOf(')', parStart);
             if (parEnd > parStart) {
                 headerText = headerText.substring(parEnd + 1).trim();
             } else {
                for (JFXVar param : def.getParameters()) {
                     int parPos = (int) sourcePositions.getEndPosition(root, param);
                     if (parPos == Diagnostic.NOPOS || offset <= parPos) {
                         break;
                     }
                     parStart = parPos - startPos;
                 }
                 headerText = headerText.substring(parStart).trim();
             }
             if (LOGGABLE) log("  headerText(2) ==" + headerText);
             if (":".equals(headerText)) {
                 addLocalAndImportedTypes(null, null, null, false, null);
                 addBasicTypes();
                 return;
             }
         } else if (retType != null && headerText.trim().length() == 0) {
             if (LOGGABLE) log("  insideExpression for retType:");
             insideExpression(new JavaFXTreePath(path, retType));
             return;
         }
         int bodyPos = (int) sourcePositions.getStartPosition(root, def.getBodyExpression());
         if (LOGGABLE) log("  bodyPos: " + bodyPos);
         if ((bodyPos >=0) && (offset > bodyPos)) {
             if (LOGGABLE) log(" we are inside body of the function:");
             insideFunctionBlock(def.getBodyExpression().getStatements());
         } 
     }
     void insideFunctionBlock(List<ExpressionTree> statements) throws IOException {
         ExpressionTree last = null;
         for (ExpressionTree stat : statements) {
             int pos = (int) sourcePositions.getStartPosition(root, stat);
             if (pos == Diagnostic.NOPOS || offset <= pos) {
                 break;
             }
             last = stat;
         }
         if (last == null) {
         } else if (last.getJavaFXKind() == Tree.JavaFXKind.TRY) {
             if (((TryTree) last).getFinallyBlock() == null) {
                 addKeyword(CATCH_KEYWORD, null, false);
                 addKeyword(FINALLY_KEYWORD, null, false);
                 if (((TryTree) last).getCatches().size() == 0) {
                     return;
                 }
             }
         }
         localResult(null);
         addKeywordsForStatement();
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 }
