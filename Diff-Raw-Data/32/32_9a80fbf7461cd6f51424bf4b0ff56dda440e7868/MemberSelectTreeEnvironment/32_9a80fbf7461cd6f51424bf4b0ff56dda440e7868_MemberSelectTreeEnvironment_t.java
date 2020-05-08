 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
  * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.editor.completion.environment;
 
 import com.sun.javafx.api.tree.*;
 import com.sun.tools.javafx.api.JavafxcScope;
 import java.io.IOException;
 import java.util.EnumSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.Name;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 import javax.lang.model.type.TypeVariable;
 import org.netbeans.api.javafx.editor.SafeTokenSequence;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.ClassIndex.NameKind;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.javafx.editor.completion.JavaFXCompletionEnvironment;
 
 /**
  * @author David Strupl
  */
 public class MemberSelectTreeEnvironment extends JavaFXCompletionEnvironment<MemberSelectTree> {
 
     private static final Logger logger = Logger.getLogger(MemberSelectTreeEnvironment.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
 
     @SuppressWarnings("fallthrough")
     @Override
     protected void inside(MemberSelectTree fa) throws IOException {
         if (LOGGABLE) log("inside MemberSelectTree " + fa); // NOI18N
         int expEndPos = (int)sourcePositions.getEndPosition(root, fa.getExpression());
         boolean afterDot = false;
         JFXTokenId lastNonWhitespaceTokenId = null;
         TokenSequence<JFXTokenId> ts_ = ((TokenHierarchy<?>) controller.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
         SafeTokenSequence<JFXTokenId> ts = new SafeTokenSequence<JFXTokenId>(ts_, controller.getDocument(), cancellable);
         ts.move(expEndPos);
         while (ts.moveNext()) {
             if (ts.offset() >= offset) {
                 break;
             }
             switch (ts.token().id()) {
                 case DECIMAL_LITERAL:
                     if (ts.offset() != expEndPos || ts.token().text().charAt(0) != '.')
                         break;
                 case DOT:
                     afterDot = true;
                     break;
                 case WS:
                 case LINE_COMMENT:
                 case COMMENT:
                 case DOC_COMMENT:
                     break;
                 default:
                     lastNonWhitespaceTokenId = ts.token().id();
             }
         }
         if (!afterDot) {
             if (expEndPos <= offset) {
                 insideExpression(new JavaFXTreePath(path, fa.getExpression()));
             }
             addPackages(""); // NOI18N
             if (LOGGABLE) log("  returning as afterDot==false"); // NOI18N
             return;
         }
 
         if (lastNonWhitespaceTokenId != JFXTokenId.STAR) {
             JavaFXTreePath parentPath = path.getParentPath();
             Tree parent = parentPath != null ? parentPath.getLeaf() : null;
             if (LOGGABLE) log("  parent == " + parent); // NOI18N
             ExpressionTree exp = fa.getExpression();
             if (LOGGABLE) log("   exp == " + exp); // NOI18N
             JavaFXTreePath expPath = new JavaFXTreePath(path, exp);
             TypeMirror type = controller.getTrees().getTypeMirror(expPath);
             if (type != null && type.getKind() == TypeKind.ERROR) {
                 tryToUseSanitizedSource();
             }
             Element el = controller.getTrees().getElement(expPath);
             if (LOGGABLE) log("   type == " + type); // NOI18N
             if (type != null) {
                 if (LOGGABLE) log("   type.getKind() == " + type.getKind()); // NOI18N
                 switch (type.getKind()) {
                     case TYPEVAR:
                         while(type != null && type.getKind() == TypeKind.TYPEVAR)
                             type = ((TypeVariable)type).getUpperBound();
                         if (type == null)
                             return;
                         type = controller.getTypes().capture(type);
                     case ARRAY:
                     case DECLARED:
                     case BOOLEAN:
                     case BYTE:
                     case CHAR:
                     case DOUBLE:
                     case FLOAT:
                     case INT:
                     case LONG:
                     case SHORT:
                     case VOID:
                         addMembers(type, true, true, null, getScope(), true, !isStatic(el));
                         break;
                     default:
                         if (LOGGABLE) log("   el(2) == " + el + "  el.getKind() == " + (el != null? el.getKind():"")); // NOI18N
                         if (type.getKind() == TypeKind.ERROR && el != null && el.getKind().isClass()) {
                             Name qualifiedName = ((TypeElement) el).getQualifiedName();
                             addPackageContent(qualifiedName);
                             addPossibleMembers(el, qualifiedName);
                         }
                 }
             } else if (parent.getJavaFXKind() == Tree.JavaFXKind.COMPILATION_UNIT && ((UnitTree)parent).getPackageName() == fa) {
                 PackageElement pe = controller.getElements().getPackageElement(fullName(exp));
                 if (pe != null) {
                     addPackageContent(pe, EnumSet.of(ElementKind.PACKAGE), null, false);
                 }
             }
         }
     }
 
     private void addPossibleMembers(final Element el, final Name qualifiedName) {
         for (ElementHandle<TypeElement> eh : getTypes(qualifiedName.toString(), NameKind.EXACT)) {
             TypeElement ehType = eh.resolve(controller);
             if (ehType != null) {
                 addMembers(ehType.asType(), true, true, null, getScope(), true, !isStatic(el));
             }
         }
     }
 
     private JavafxcScope getScope() {
         return controller.getTreeUtilities().getScope(path);
     }
 
     private boolean isStatic(final Element el) {
         return el != null && (el.getKind().isClass() || el.getKind().isInterface());
     }
 
     private void addPackageContent(final Name qualifiedName) {
         if (LOGGABLE) {
             log("   will try to find package named " + (qualifiedName)); // NOI18N
         }
        try {
            PackageElement packageEl = controller.getElements().getPackageElement(qualifiedName);
            if (LOGGABLE) {
                log("   packageEl(3) == " + packageEl + "  packageEl.getKind() == " // NOI18N
                        + (packageEl != null ? packageEl.getKind() : ""));
            }
            if (packageEl != null) {
                addPackageContent(packageEl, null, null, false);
            }
        } catch (ClassCastException cce) {
            // XXX: compiler throws CCE in some cases, cf. 178366.
            // TBD: Why exactly?
            log("    cce = " + cce);
            if (LOGGABLE) {
                cce.printStackTrace();
            }
         }
     }
 
     public TypeMirror getSmartType(MemberSelectTree t) throws IOException {
         return null;
         // TODO
 //        final TreePath treePath = new TreePath(path, t.getExpression());
 //        TypeMirror type = controller.getTrees().getTypeMirror(treePath);
 //        if (type == null) {
 //            return null;
 //        }
 //
 //        int dim = 0;
 //        while (dim-- > 0) {
 //            if (type.getKind() == TypeKind.ARRAY) {
 //                type = ((ArrayType) type).getComponentType();
 //            } else {
 //                return null;
 //            }
 //        }
 //
 //        return type;
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 }
