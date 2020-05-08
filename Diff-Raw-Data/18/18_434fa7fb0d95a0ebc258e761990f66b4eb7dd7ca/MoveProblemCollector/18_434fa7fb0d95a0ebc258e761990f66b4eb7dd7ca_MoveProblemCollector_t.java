 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  *  Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
  *  The Original Software is NetBeans. The Initial Developer of the Original
  *  Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  *  Microsystems, Inc. All Rights Reserved.
  * 
  *  If you wish your version of this file to be governed by only the CDDL
  *  or only the GPL Version 2, indicate your decision by adding
  *  "[Contributor] elects to include this software in this distribution
  *  under the [CDDL or GPL Version 2] license." If you do not indicate a
  *  single choice of license, a recipient has the option to distribute
  *  your version of this file under either the CDDL, the GPL Version 2 or
  *  to extend the choice of license to its licensees as provided above.
  *  However, if you add GPL Version 2 code and therefore, elected the GPL
  *  Version 2 license, then the option applies only if the new code is
  *  made subject to such option by the copyright holder.
  */
 
 package org.netbeans.modules.javafx.refactoring.impl.scanners;
 
 import com.sun.javafx.api.tree.ClassDeclarationTree;
 import com.sun.javafx.api.tree.ExpressionTree;
 import com.sun.javafx.api.tree.FunctionInvocationTree;
 import com.sun.javafx.api.tree.InstantiateTree;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.MemberSelectTree;
 import com.sun.javafx.api.tree.ObjectLiteralPartTree;
 import com.sun.javafx.api.tree.TypeClassTree;
 import com.sun.javafx.api.tree.UnitTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javafx.code.JavafxFlags;
 import java.util.EnumSet;
 import java.util.Map;
 import java.util.Set;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.TypeMirror;
 import org.netbeans.api.javafx.source.ClassIndex;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.modules.javafx.refactoring.impl.plugins.MoveRefactoringPlugin;
 import org.netbeans.modules.refactoring.api.Problem;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author Jaroslav Bachorik <jaroslav.bachorik@sun.com>
  */
 public class MoveProblemCollector<R, P> extends JavaFXTreePathScanner<R, P> {
     private interface MoveProblemCallback {
         Problem createProblem(String oldPkgName, String newPkgName, String srcTypeName, String targetTypeName, String feature);
     }
 
     private String myPkgName;
     private Problem problem = null;
 
     private CompilationController cc;
     private Set<String> movedClasses;
     private Map<String, String> renameMap;
 
     public <R, P> MoveProblemCollector(CompilationController cc, Set<String> movedClasses, Map<String, String> renameMap) {
         this.cc = cc;
         this.movedClasses = movedClasses;
         this.renameMap = renameMap;
     }
 
     final protected String getPackageName() {
         return myPkgName;
     }
 
     final protected TypeMirror getClassType() {
         return currentClass;
     }
 
     final public Problem getProblem() {
         return problem;
     }
 
     @Override
     public R visitCompilationUnit(UnitTree node, P p) {
         ExpressionTree packageNameTree = node.getPackageName();
         myPkgName = packageNameTree != null ? packageNameTree.toString() : "";
         return super.visitCompilationUnit(node, p);
     }
 
     private TypeMirror currentClass = null;
 
     @Override
     public R visitClassDeclaration(ClassDeclarationTree node, P p) {
         TypeElement te = (TypeElement)cc.getTrees().getElement(getCurrentPath());
         currentClass = te.asType();
         return super.visitClassDeclaration(node, p);
     }
 
     @Override
     public R visitTypeClass(TypeClassTree node, P p) {
         Element e = cc.getTrees().getElement(getCurrentPath());
 
         problem = chainProblems(problem, checkVisibilty(e, new MoveProblemCallback() {
 
             public Problem createProblem(String oldPkgName, String newPkgName, String srcTypeName, String targetTypeName, String feature) {
                 return new Problem(true, NbBundle.getMessage(MoveRefactoringPlugin.class, "ERR_AccessesPackagePrivateClass", new String[]{srcTypeName, targetTypeName, newPkgName}));
             }
         }));
 
         return super.visitTypeClass(node, p);
     }
 
     @Override
     public R visitInstantiate(InstantiateTree node, P p) {
         TypeElement clzElement = (TypeElement)cc.getTrees().getElement(getCurrentPath());
         problem = chainProblems(problem,
             checkVisibilty(clzElement, new MoveProblemCallback() {
 
                 public Problem createProblem(String oldPkgName, String newPkgName, String srcTypeName, String targetTypeName, String feature) {
                     return new Problem(true, NbBundle.getMessage(MoveRefactoringPlugin.class, "ERR_AccessesPackagePrivateClass", new String[]{srcTypeName, targetTypeName, newPkgName}));
                 }
             })
         );
         return super.visitInstantiate(node, p);
     }
 
     @Override
     public R visitObjectLiteralPart(ObjectLiteralPartTree node, P p) {
         Element e = cc.getTrees().getElement(getCurrentPath());
         problem = chainProblems(problem, checkVisibilty(e, new MoveProblemCallback() {
             public Problem createProblem(String oldPkgName, String newPkgName, String srcTypeName, String targetTypeName, String feature) {
                 return new Problem(true, NbBundle.getMessage(MoveRefactoringPlugin.class, "ERR_AccessesPackagePrivateFeature", new String[]{srcTypeName, feature, targetTypeName}));
             }
         }));
         return super.visitObjectLiteralPart(node, p);
     }
 
     @Override
     public R visitMemberSelect(MemberSelectTree node, P p) {
         Element e = cc.getTrees().getElement(getCurrentPath());
         if (e != null && e.getKind() == ElementKind.FIELD) {
             problem = chainProblems(problem, checkVisibilty(e, new MoveProblemCallback() {
                 public Problem createProblem(String oldPkgName, String newPkgName, String srcTypeName, String targetTypeName, String feature) {
                     return new Problem(true, NbBundle.getMessage(MoveRefactoringPlugin.class, "ERR_AccessesPackagePrivateFeature", new String[]{srcTypeName, feature, targetTypeName}));
                 }
             }));
         }
         return super.visitMemberSelect(node, p);
     }
 
     @Override
     public R visitMethodInvocation(FunctionInvocationTree node, P p) {
         Element e = cc.getTrees().getElement(getCurrentPath());
         problem = chainProblems(problem, checkVisibilty(e, new MoveProblemCallback() {
 
             public Problem createProblem(String oldPkgName, String newPkgName, String srcTypeName, String targetTypeName, String feature) {
                 return new Problem(true, NbBundle.getMessage(MoveRefactoringPlugin.class, "ERR_AccessesPackagePrivateFeature", new String[]{srcTypeName, feature, targetTypeName}));
             }
         }));
         return super.visitMethodInvocation(node, p);
     }
 
     private Problem checkVisibilty(Element e, MoveProblemCallback callback) {
         if (e == null) return null;
        if (isAffected(e)) {
             boolean packageAccess = true;
 
             String feature = e.toString();
             String targetTypeName = null;
             while (e != null && e.getKind() != ElementKind.PACKAGE) {
                 if (targetTypeName == null && e.getKind() == ElementKind.CLASS) {
                     targetTypeName = e.asType().toString();
                    if (isProtected(e) && cc.getTypes().isSubtype(e.asType(), currentClass)) {
                         packageAccess = false;
                     }
                 }
                 e = e.getEnclosingElement();
             }
             if (e != null && !movedClasses.contains(targetTypeName) && packageAccess) {
                 String pkgName = ((PackageElement)e).getQualifiedName().toString();
                 String newPkgName = renameMap.get(pkgName);
                 return callback.createProblem(pkgName, newPkgName, currentClass.toString(), targetTypeName, feature);
             }
         }
         return null;
     }
 
    private boolean isProtected(Element e) {
        return (((Symbol)e).flags_field & Flags.PROTECTED) == 0L;
    }

    private boolean isAffected(Element e) {
        return (((Symbol)e).flags_field & (JavafxFlags.PACKAGE_ACCESS | Flags.PROTECTED)) != 0L;
    }

     private static Problem chainProblems(Problem p,Problem p1) {
         Problem problem;
 
         if (p==null) return p1;
         if (p1==null) return p;
         problem=p;
         while(problem.getNext()!=null) {
             problem=problem.getNext();
         }
         problem.setNext(p1);
         return p;
     }
 }
