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
 
 import com.sun.javafx.api.tree.CatchTree;
import com.sun.javafx.api.tree.ErroneousTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.VariableTree;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javafx.code.JavafxTypes;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.ArrayType;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.javafx.editor.completion.JavaFXCompletionEnvironment;
 
 /**
  *
  * @author David Strupl
  */
 public class CatchEnvironment extends JavaFXCompletionEnvironment<CatchTree> {
 
     private static final Logger logger = Logger.getLogger(CatchEnvironment.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
 
     @Override
     protected void inside(CatchTree t) throws IOException {
         if (LOGGABLE) log("inside CatchTree " + t + "  offset == " + offset);
         VariableTree var = t.getParameter();
         Tree type = var.getType();
        int typePos = type.getJavaFXKind() == Tree.JavaFXKind.ERRONEOUS && ((ErroneousTree) type).getErrorTrees().isEmpty() ? (int) sourcePositions.getEndPosition(root, type) : (int) sourcePositions.getStartPosition(root, type);
         if (LOGGABLE) log("  type == " + type + "  typePos == " + typePos);
         if (offset <= typePos) {
             TokenSequence<JFXTokenId> last = findLastNonWhitespaceToken((int) sourcePositions.getStartPosition(root, t), offset);
             if (LOGGABLE) log("    last(1) == " + (last == null ? "null" : last.token().id()));
             if ((last != null) && (last.token().id() == JFXTokenId.COLON)){
                 addLocalAndImportedTypes(null, null, null, false, getSmartType());
             }
             return;
         }
         // TODO:
         if (LOGGABLE) log("   NOT IMPLEMENTED: suggest a name?");
     }
 
     /**
      * 
      * @return
      */
     private TypeMirror getSmartType() {
         // TODO: find what exceptions are being thrown in the corresponding try
         //       for now just make "smart" all throwables
         TypeElement te = controller.getElements().getTypeElement("java.lang.Throwable");
         if (LOGGABLE) log("   throwable == " + te);
         TypeMirror type = te.asType();
         if (LOGGABLE) log("   getSmartType returning " + type);
         return type;
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 }
