 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 
 import com.sun.javafx.api.tree.AssignmentTree;
 import com.sun.javafx.api.tree.FunctionInvocationTree;
 import com.sun.javafx.api.tree.InstantiateTree;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.tools.javac.code.Symbol.MethodSymbol;
 import com.sun.tools.javac.code.Type;
 import java.util.*;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.ElementHandle;
 
 /**
  *
  * @author karol harezlak
  */
 final class UncaughtExceptionsVisitor extends JavaFXTreePathScanner<Void, HintsModel> {
 
     private CompilationInfo compilationInfo;
     private ClassIndex classIndex;
     private Set<String> instantTypes;
     private EnumSet<ClassIndex.SearchScope> SCOPE = EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES);
 
     UncaughtExceptionsVisitor(CompilationInfo compilationInfo, ClassIndex classIndex) {
         this.compilationInfo = compilationInfo;
         this.classIndex = classIndex;
         instantTypes = new HashSet<String>();
     }
 
     @Override
     public Void visitMethodInvocation(FunctionInvocationTree node, HintsModel model) {
         if (node.toString().contains(".")) { //NOI18N
             instantTypes.add(extractMethodClassName(node.toString()));
         }
         for (String instantType : instantTypes) {
             if (HintsUtils.checkString(instantType)) {
                 continue;
             }
             Set<ElementHandle<TypeElement>> options = classIndex.getDeclaredTypes(instantType, ClassIndex.NameKind.SIMPLE_NAME, SCOPE);
             for (ElementHandle<TypeElement> elementHandle : options) {
                 TypeElement typeElement = elementHandle.resolve(compilationInfo);
                 if (typeElement == null || typeElement.getQualifiedName() == null) {
                     continue;
                 }
                 String elementType = HintsUtils.getMethodName(typeElement.getQualifiedName().toString()).trim();
                 if (elementType == null || !elementType.equals(instantType)) {
                     break;
                 }
                 Collection<? extends Element> elements = getAllMembers(typeElement, compilationInfo);
                 if (elements == null) {
                     continue;
                 }
                 Element currentElement = compilationInfo.getTrees().getElement(getCurrentPath());
                if (currentElement == null || currentElement.getSimpleName() == null) {
                    continue;
                }
                 String methodName = currentElement.getSimpleName().toString();
                 for (Element element : elements) {
                     if (element instanceof MethodSymbol) {
                         MethodSymbol methodSymbol = (MethodSymbol) element;
                         if (!methodSymbol.getSimpleName().toString().equals(methodName)) {
                             continue;
                         }
                         List<Type> thrownExceptions = null;
                         //Hack for JAVAC ISSUE - methodSymbol.getThrownTypes() throws null pointer exception
                         try {
                             thrownExceptions = methodSymbol.getThrownTypes();
                         } catch (NullPointerException ex) {
                             ex.printStackTrace();
                         }
 
                         if (thrownExceptions == null || thrownExceptions.size() == 0) {
                             continue;
                         }
                         model.addHint(thrownExceptions, node);
                     }
                 }
             }
         }
         return super.visitMethodInvocation(node, model);
     }
     //TODO Temporary log for issue 148890
 
     private Collection<? extends Element> getAllMembers(TypeElement typeElement, CompilationInfo compilationInfo) {
         Collection<? extends Element> elements = null;
         try {
             elements = compilationInfo.getElements().getAllMembers(typeElement);
         } catch (NullPointerException npe) {
             npe.printStackTrace();
             System.err.println("* e = " + typeElement); //NOI18N
             System.err.println("* e.getKind() = " + typeElement.getKind()); //NOI18N
             System.err.println("* e.asType() = " + typeElement.asType()); //NOi18N
         }
         return elements;
     }
 
     @Override
     public Void visitAssignment(AssignmentTree node, HintsModel p) {
         String typeName = (node.getExpression().toString().replace("{}", "").trim()); //NOI18N
         instantTypes.add(typeName);
         return super.visitAssignment(node, p);
     }
 
     @Override
     public Void visitInstantiate(InstantiateTree node, HintsModel p) {
         String typeName = (node.getIdentifier().toString().replace("{}", "").trim()); //NOI18N
         instantTypes.add(typeName);
         return super.visitInstantiate(node, p);
     }
 
     private static String extractMethodClassName(String fullMethodName) {
         int last = fullMethodName.lastIndexOf("."); //NOI18N
         if (last < 0) {
             return fullMethodName;
         }
         String fqn = fullMethodName.substring(0, last);
         int fqnLast = fqn.lastIndexOf("."); //NOi18N
         if (fqnLast > 0) {
             fqn = fqn.substring(fqnLast + 1, fqn.length());
         }
         String className = fqn.replace("{}", "").replace("()", "").trim(); //NOI18N
         return className;
     }
 }
