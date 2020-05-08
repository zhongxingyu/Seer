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
 
 import com.sun.tools.javafx.tree.JFXClassDeclaration;
 import java.io.IOException;
 import java.util.EnumSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.javafx.editor.completion.JavaFXCompletionEnvironment;
 import static javax.lang.model.element.ElementKind.*;
 import static org.netbeans.modules.javafx.editor.completion.JavaFXCompletionQuery.EXTENDS_KEYWORD;
 
 
 /**
  *
  * @author David Strupl
  */
 public class ClassDeclarationEnvironment extends JavaFXCompletionEnvironment<JFXClassDeclaration> {
     
    private static final Logger logger = Logger.getLogger(ClassDeclarationEnvironment.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
 
     @Override
     protected void inside(JFXClassDeclaration cldecl) throws IOException {
         if (LOGGABLE) log("inside JFXClassDeclaration " + cldecl);
         if (LOGGABLE) log("  prefix: " + prefix);
         int start = (int)sourcePositions.getStartPosition(root, cldecl);
         if (LOGGABLE) log("  offset: " + offset);
         if (LOGGABLE) log("  start: " + start);
         TokenSequence<JFXTokenId> ts = ((TokenHierarchy<?>)controller.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
         ts.move(start);
         boolean afterLBrace = false;
         boolean afterExtends = false;
         loop: while (ts.moveNext()) {
             if (ts.offset() >= offset) {
                 break;
             }
             switch (ts.token().id()) {
                 case WS:
                 case LINE_COMMENT:
                 case COMMENT:
                 case DOC_COMMENT:
                     continue;
                 case LBRACE:
                     afterLBrace = true;
                     break loop;
                 case EXTENDS:
                     afterExtends = true;
                     break;
                 default:
                     // TODO:
             }
         }
         if (LOGGABLE) log("  afterLBrace: " + afterLBrace);
         if (afterLBrace) {
             addKeywordsForClassBody();
         } else {
             if (afterExtends) {
                 if (LOGGABLE) log("  afterExtends: " + afterExtends);
                 addLocalAndImportedTypes(EnumSet.of(CLASS), null, null, false, null);
             } else {
                 addKeyword(EXTENDS_KEYWORD, " ", false);
             }
         }
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 }
