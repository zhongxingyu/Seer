 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 package org.netbeans.modules.javafx.editor.completion;
 
 import com.sun.javafx.api.tree.BlockExpressionTree;
 import com.sun.javafx.api.tree.ExpressionTree;
 import com.sun.javafx.api.tree.ForExpressionInClauseTree;
 import com.sun.javafx.api.tree.ForExpressionTree;
 import com.sun.javafx.api.tree.FunctionDefinitionTree;
 import com.sun.javafx.api.tree.FunctionInvocationTree;
 import com.sun.javafx.api.tree.FunctionValueTree;
 import com.sun.javafx.api.tree.IdentifierTree;
 import com.sun.javafx.api.tree.InstanceOfTree;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.MemberSelectTree;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.javafx.api.tree.Tree.JavaFXKind;
 import com.sun.javafx.api.tree.VariableTree;
 import com.sun.javafx.api.tree.OnReplaceTree;
 import com.sun.javafx.api.tree.SourcePositions;
 import com.sun.javafx.api.tree.UnitTree;
 import com.sun.tools.javac.code.Scope;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javafx.api.JavafxcScope;
 import com.sun.tools.javafx.api.JavafxcTrees;
 import com.sun.tools.javafx.code.JavafxTypes;
 import com.sun.tools.javafx.tree.JFXClassDeclaration;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.PackageElement;
 import static javax.lang.model.element.Modifier.*;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.ArrayType;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.ExecutableType;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 import javax.lang.model.util.Elements;
 import javax.lang.model.util.Types;
 import javax.tools.Diagnostic;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.ClassIndex.NameKind;
 import org.netbeans.api.javafx.source.ClassIndex.SearchScope;
 import org.netbeans.api.javafx.source.ClasspathInfo;
 import org.netbeans.api.javafx.source.ClasspathInfo.PathKind;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.api.javafx.source.ElementUtilities;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 import org.netbeans.api.javafx.source.TreeUtilities;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.openide.filesystems.FileObject;
 import org.openide.util.NbBundle;
 import static org.netbeans.modules.javafx.editor.completion.JavaFXCompletionQuery.*;
 
 /**
  *
  * @author David Strupl, Anton Chechel
  */
 public class JavaFXCompletionEnvironment<T extends Tree> {
 
     private static final Logger logger = Logger.getLogger(JavaFXCompletionEnvironment.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
     protected int offset;
     protected String prefix;
     protected boolean isCamelCasePrefix;
     protected CompilationController controller;
     protected JavaFXTreePath path;
     protected SourcePositions sourcePositions;
     protected boolean insideForEachExpressiion = false;
     protected UnitTree root;
     protected JavaFXCompletionQuery query;
 
     protected JavaFXCompletionEnvironment() {
     }
 
     /*
      * Thies method must be called after constructor before a call to resolveCompletion
      */
     void init(int offset, String prefix, CompilationController controller, JavaFXTreePath path, SourcePositions sourcePositions, JavaFXCompletionQuery query) {
         this.offset = offset;
         this.prefix = prefix;
         this.isCamelCasePrefix = prefix != null && prefix.length() > 1 && JavaFXCompletionQuery.camelCasePattern.matcher(prefix).matches();
         this.controller = controller;
         this.path = path;
         this.sourcePositions = sourcePositions;
         this.query = query;
         this.root = path.getCompilationUnit();
     }
 
     /**
      * This method should be overriden in subclasses
      */
     protected void inside(T t) throws IOException {
         if (LOGGABLE) log("NOT IMPLEMENTED " + t.getJavaFXKind() + " inside " + t);
     }
 
     public int getOffset() {
         return offset;
     }
 
     public String getPrefix() {
         return prefix;
     }
 
     public boolean isCamelCasePrefix() {
         return isCamelCasePrefix;
     }
 
     public CompilationController getController() {
         return controller;
     }
 
     public UnitTree getRoot() {
         return root;
     }
 
     public JavaFXTreePath getPath() {
         return path;
     }
 
     public SourcePositions getSourcePositions() {
         return sourcePositions;
     }
 
     public void insideForEachExpressiion() {
         this.insideForEachExpressiion = true;
     }
 
     public boolean isInsideForEachExpressiion() {
         return insideForEachExpressiion;
     }
 
     /**
      * If the tree is broken we are in fact not in the compilation unit.
      * @param env
      * @return
      */
     protected boolean isTreeBroken() {
         int start = (int) sourcePositions.getStartPosition(root, root);
         int end = (int) sourcePositions.getEndPosition(root, root);
         if (LOGGABLE) log("isTreeBroken start: " + start + " end: " + end);
         return start == -1 || end == -1;
     }
 
     protected String fullName(Tree tree) {
         switch (tree.getJavaFXKind()) {
             case IDENTIFIER:
                 return ((IdentifierTree) tree).getName().toString();
             case MEMBER_SELECT:
                 String sname = fullName(((MemberSelectTree) tree).getExpression());
                 return sname == null ? null : sname + '.' + ((MemberSelectTree) tree).getIdentifier();
             default:
                 return null;
         }
     }
 
     void insideTypeCheck() throws IOException {
         InstanceOfTree iot = (InstanceOfTree) getPath().getLeaf();
         TokenSequence<JFXTokenId> ts = findLastNonWhitespaceToken(iot, getOffset());
     }
 
     protected void insideExpression(JavaFXTreePath exPath) throws IOException {
         if (LOGGABLE) log("insideExpression " + exPath.getLeaf());
         Tree et = exPath.getLeaf();
         Tree parent = exPath.getParentPath().getLeaf();
         int endPos = (int) getSourcePositions().getEndPosition(root, et);
         if (endPos != Diagnostic.NOPOS && endPos < offset) {
             TokenSequence<JFXTokenId> last = findLastNonWhitespaceToken(endPos, offset);
             if (LOGGABLE) log("  last: " + last);
             if (last != null) {
                 return;
             }
         }
         if (LOGGABLE) log("NOT IMPLEMENTED: insideExpression " + exPath.getLeaf());
 
     }
 
     protected void addResult(JavaFXCompletionItem i) {
         query.results.add(i);
     }
 
     protected void addMembers(final TypeMirror type, final boolean methods, final boolean fields) {
         JavafxcScope sc = controller.getTreeUtilities().getScope(path);
         boolean isStatic = controller.getTreeUtilities().isStaticContext(sc);
         addMembers(type, methods, fields, null,sc, true, !isStatic);
     }
     
     protected void addMembers(final TypeMirror type,
             final boolean methods, final boolean fields,
             final String textToAdd, JavafxcScope scope,boolean statics, boolean instance) {
         if (LOGGABLE) log("addMembers: " + type);
         if (type == null || type.getKind() != TypeKind.DECLARED) {
            if (LOGGABLE) log("RETURNING: type.getKind() == " + type.getKind());
             return;
         }
 
         DeclaredType dt = (DeclaredType) type;
         if (LOGGABLE) log("  elementKind == " + dt.asElement().getKind());
         if (dt.asElement().getKind() != ElementKind.CLASS) {
             return;
         }
         
         Elements elements = controller.getElements();
         final TypeElement te = (TypeElement) dt.asElement();
         for (Element member : te.getEnclosedElements()) {
             if (LOGGABLE) log("    member1 = " + member + " member1.getKind() " + member.getKind());
             if ("<error>".equals(member.getSimpleName().toString())) {
                 continue;
             }
             boolean isStatic = member.getModifiers().contains(STATIC);
             String s = member.getSimpleName().toString();
               if (!controller.getTreeUtilities().isAccessible(scope, member, dt)) {
                 if (LOGGABLE) log("    not accessible " + s);
                 continue;
             }
             if (isStatic && !statics) {
                 if (LOGGABLE) log("    is static and we don't want them " + s);
                 continue;
             }
             if (!isStatic && !instance) {
                 if (LOGGABLE) log("     is instance and we don't want them " + s);
                 continue;
             }
             String tta = textToAdd;
             if (fields && member.getKind() == ElementKind.FIELD) {
                 if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                     if (":".equals(textToAdd)) {
                         JavafxTypes types = controller.getJavafxTypes();
                         TypeMirror tm = member.asType();
                         if (types.isSequence((Type) tm)) {
                             tta += " []";
                         }
                     }
                     addResult(JavaFXCompletionItem.createVariableItem(member.asType(), s, query.anchorOffset, tta, true));
                 }
             }
         }
 
         for (Element member : elements.getAllMembers(te)) {
             if (LOGGABLE) log("    member2 == " + member + " member2.getKind() " + member.getKind());
             String s = member.getSimpleName().toString();
             if ("<error>".equals(member.getSimpleName().toString())) {
                 continue;
             }
             if (!controller.getTreeUtilities().isAccessible(scope, member, dt)) {
                 if (LOGGABLE) log("    not accessible " + s);
                 continue;
             }
             boolean isStatic = member.getModifiers().contains(STATIC);
             if (isStatic && !statics) {
                 if (LOGGABLE) log("    is static and we don't want them " + s);
                 continue;
             }
             if (!isStatic && !instance) {
                 if (LOGGABLE) log("     is instance and we don't want them " + s);
                 continue;
             }
             if (methods && member.getKind() == ElementKind.METHOD) {
                 if (s.contains("$")) {
                     continue;
                 }
 
                 if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                     addResult(
                             JavaFXCompletionItem.createExecutableItem(
                             (ExecutableElement) member,
                             (ExecutableType) member.asType(),
                             query.anchorOffset, false, false, false, false));
                 }
             } else if (fields && member.getKind() == ElementKind.FIELD) {
                 String tta = textToAdd;
                 if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                     if (":".equals(textToAdd)) {
                         JavafxTypes types = controller.getJavafxTypes();
                         TypeMirror tm = member.asType();
                         if (types.isSequence((Type) tm)) {
                             tta += " []";
                         }
                     }
                     addResult(JavaFXCompletionItem.createVariableItem(member.asType(), s, query.anchorOffset, tta, false));
                 }
             }
         }
     }
 
     protected void localResult(TypeMirror smart) {
         addLocalMembersAndVars(smart);
         addLocalAndImportedTypes(null, null, null, false, smart);
         addLocalAndImportedFunctions();
     }
 
     protected void addMemberConstantsAndTypes(final TypeMirror type, final Element elem) throws IOException {
         if (LOGGABLE) log("addMemberConstantsAndTypes: " + type + " elem: " + elem);
     }
 
     protected void addLocalMembersAndVars(TypeMirror smart) {
         if (LOGGABLE) log("addLocalMembersAndVars: " + prefix);
 
         final JavafxcTrees trees = controller.getTrees();
         if (smart != null && smart.getKind() == TypeKind.DECLARED) {
             if (LOGGABLE) log("adding declared type + subtypes: " + smart);
             DeclaredType dt = (DeclaredType) smart;
             TypeElement elem = (TypeElement) dt.asElement();
             addResult(JavaFXCompletionItem.createTypeItem(elem, dt, query.anchorOffset, false, false, true, false));
 
             for (DeclaredType subtype : getSubtypesOf((DeclaredType) smart)) {
                 TypeElement subElem = (TypeElement) subtype.asElement();
                 addResult(JavaFXCompletionItem.createTypeItem(subElem, subtype, query.anchorOffset, false, false, true, false));
             }
         }
 
         for (JavaFXTreePath tp = getPath(); tp != null; tp = tp.getParentPath()) {
             Tree t = tp.getLeaf();
             if (LOGGABLE) log("  tree kind: " + t.getJavaFXKind());
             if (t instanceof UnitTree) {
                 UnitTree cut = (UnitTree) t;
                 for (Tree tt : cut.getTypeDecls()) {
                     if (LOGGABLE) log("      tt: " + tt);
                     JavaFXKind kk = tt.getJavaFXKind();
                     if (kk == JavaFXKind.CLASS_DECLARATION) {
                         JFXClassDeclaration cd = (JFXClassDeclaration) tt;
                         for (Tree jct : cd.getClassMembers()) {
                             if (LOGGABLE) log("            jct == " + jct);
                             JavaFXKind k = jct.getJavaFXKind();
                             if (LOGGABLE) log("       kind of jct = " + k);
                             if (k == JavaFXKind.FUNCTION_DEFINITION) {
                                 JFXFunctionDefinition fdt = (JFXFunctionDefinition) jct;
                                 if (LOGGABLE) log("      fdt == " + fdt.name.toString());
                                 if ("javafx$run$".equals(fdt.name.toString())) {
                                     addBlockExpressionLocals(fdt.getBodyExpression(), tp, smart);
                                     JavaFXTreePath mp = JavaFXTreePath.getPath(cut, tt);
                                     TypeMirror tm = trees.getTypeMirror(mp);
                                     if (LOGGABLE) log("  javafx$run$ tm == " + tm + " ---- tm.getKind() == " + (tm == null ? "null" : tm.getKind()));
                                     JavaFXTreePath mp2 = JavaFXTreePath.getPath(cut, fdt);
                                     addMembers(tm, true, true,
                                             null, controller.getTreeUtilities().getScope(mp2),
                                             true, false);
                                 }
                             }
                         }
                     }
                 }
             }
             JavaFXKind k = t.getJavaFXKind();
             if (LOGGABLE) log("  fx kind: " + k);
             if (k == JavaFXKind.CLASS_DECLARATION) {
                 TypeMirror tm = trees.getTypeMirror(tp);
                 if (LOGGABLE) log("  tm == " + tm + " ---- tm.getKind() == " + (tm == null ? "null" : tm.getKind()));
                 addMembers(tm, true, true);
             }
             if (k == JavaFXKind.BLOCK_EXPRESSION) {
                 addBlockExpressionLocals((BlockExpressionTree) t, tp, smart);
             }
             if (k == JavaFXKind.FOR_EXPRESSION_FOR) {
                 ForExpressionTree fet = (ForExpressionTree) t;
                 if (LOGGABLE) log("  for expression: " + fet + "\n");
                 for (ForExpressionInClauseTree fetic : fet.getInClauses()) {
                     if (LOGGABLE) log("  fetic: " + fetic + "\n");
                     String s = fetic.getVariable().getName().toString();
                     if (LOGGABLE) log("    adding(2) " + s + " with prefix " + prefix);
                     TypeMirror tm = trees.getTypeMirror(new JavaFXTreePath(tp, fetic));
                     if (smart != null && tm != null && tm.getKind() == smart.getKind()) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s, query.anchorOffset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s, query.anchorOffset, false));
                     }
                 }
             }
             if (k == JavaFXKind.FUNCTION_VALUE) {
                 FunctionValueTree fvt = (FunctionValueTree) t;
                 for (VariableTree var : fvt.getParameters()) {
                     if (LOGGABLE) log("  var: " + var + "\n");
                     String s = var.getName().toString();
                     if (LOGGABLE) log("    adding(3) " + s + " with prefix " + prefix);
                     TypeMirror tm = trees.getTypeMirror(new JavaFXTreePath(tp, var));
                     if (smart != null && tm.getKind() == smart.getKind()) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s, query.anchorOffset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s, query.anchorOffset, false));
                     }
                 }
             }
             if (k == JavaFXKind.ON_REPLACE) {
                 OnReplaceTree ort = (OnReplaceTree) t;
                 // commented out log because of JFXC-1205
                 // if (LOGGABLE) log("  OnReplaceTree: " + ort + "\n");
                 VariableTree varTree = ort.getNewElements();
                 if (varTree != null) {
                     String s1 = varTree.getName().toString();
                     if (LOGGABLE) log("    adding(4) " + s1 + " with prefix " + prefix);
                     TypeMirror tm = trees.getTypeMirror(new JavaFXTreePath(tp, varTree));
                     if (smart != null && tm.getKind() == smart.getKind()) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s1, query.anchorOffset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s1, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s1, query.anchorOffset, false));
                     }
                 }
                 VariableTree varTree2 = ort.getOldValue();
                 if (varTree2 != null) {
                     String s2 = varTree2.getName().toString();
                     if (LOGGABLE) log("    adding(5) " + s2 + " with prefix " + prefix);
                     TypeMirror tm = trees.getTypeMirror(new JavaFXTreePath(tp, varTree2));
                     if (smart != null && tm.getKind() == smart.getKind()) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s2, query.anchorOffset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s2, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(tm, s2, query.anchorOffset, false));
                     }
                 }
             }
         }
     }
 
     private void addBlockExpressionLocals(BlockExpressionTree bet, JavaFXTreePath tp, TypeMirror smart) {
         if (LOGGABLE) log("  block expression: " + bet + "\n");
         for (ExpressionTree st : bet.getStatements()) {
             addLocal(st, tp, smart);
         }
         addLocal(bet.getValue(), tp, smart);
     }
 
     private void addLocal(ExpressionTree st, JavaFXTreePath tp, TypeMirror smart) {
         if (st == null) {
             return;
         }
         JavaFXTreePath expPath = new JavaFXTreePath(tp, st);
         if (LOGGABLE) log("    expPath == " + expPath.getLeaf());
         JavafxcTrees trees = controller.getTrees();
         Element type = trees.getElement(expPath);
         if (type == null) {
             return;
         }
         if (LOGGABLE) log("    type.getKind() == " + type.getKind());
         if (type.getKind() == ElementKind.LOCAL_VARIABLE ||
                 type.getKind() == ElementKind.FIELD) {
             String s = type.getSimpleName().toString();
             if (LOGGABLE) log("    adding(1) " + s + " with prefix " + prefix);
             TypeMirror tm = trees.getTypeMirror(expPath);
             if (smart != null && tm.getKind() == smart.getKind()) {
                 addResult(JavaFXCompletionItem.createVariableItem(tm,
                         s, query.anchorOffset, true));
             }
             if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                 addResult(JavaFXCompletionItem.createVariableItem(tm,
                         s, query.anchorOffset, false));
             }
         }
     }
 
     protected void addPackages(String fqnPrefix) {
         if (LOGGABLE) log("addPackages " + fqnPrefix);
         if (fqnPrefix == null) {
             fqnPrefix = "";
         }
         JavaFXSource js = controller.getJavaFXSource();
         
         ClasspathInfo info = js.getCpInfo();
         ArrayList<FileObject> fos = new ArrayList<FileObject>();
         ClassPath cp = info.getClassPath(PathKind.SOURCE);
         fos.addAll(Arrays.asList(cp.getRoots()));
         cp = info.getClassPath(PathKind.COMPILE);
         fos.addAll(Arrays.asList(cp.getRoots()));
         cp = info.getClassPath(PathKind.BOOT);
         fos.addAll(Arrays.asList(cp.getRoots()));
         String pr = "";
         if (fqnPrefix.lastIndexOf('.') >= 0) {
             pr = fqnPrefix.substring(0, fqnPrefix.lastIndexOf('.'));
         }
         if (LOGGABLE) log("  pr == " + pr);
         for (String name : pr.split("\\.")) {
             ArrayList<FileObject> newFos = new ArrayList<FileObject>();
             if (LOGGABLE) log("  traversing to " + name);
             for (FileObject f : fos) {
                 if (f.isFolder()) {
                     FileObject child = f.getFileObject(name);
                     if (child != null) {
                         newFos.add(child);
                     }
                 }
             }
             if (LOGGABLE) log("  replacing " + fos + "\n   with " + newFos);
             fos = newFos;
         }
         for (FileObject fo : fos) {
             if (fo.isFolder()) {
                 for (FileObject child : fo.getChildren()) {
                     if (child.isFolder()) {
                         if (LOGGABLE) log(" found : " + child);
                         if (("META-INF".equals(child.getName())) || 
                             ("doc-files".equals(child.getName()))) {
                             continue;
                         }
                         String s = child.getPath().replace('/', '.');
                         addResult(JavaFXCompletionItem.createPackageItem(s, query.anchorOffset, false));
                     }
                 }
             }
         }
     }
 
     protected List<DeclaredType> getSubtypesOf(DeclaredType baseType) {
         if (LOGGABLE) log("NOT IMPLEMENTED: getSubtypesOf " + baseType);
         return Collections.emptyList();
     }
 
     void resolveToolTip(final CompilationController controller) throws IOException {
         Phase resPhase = controller.toPhase(Phase.ANALYZED);
 
         if  ((resPhase.lessThan(Phase.ANALYZED)) || (isTreeBroken())) {
             if (LOGGABLE) log("resolveToolTip: phase: " + resPhase);
             return;
         }
         if (LOGGABLE) {
             log("  resolveToolTip start");
         }
         Tree lastTree = null;
         while (path != null) {
             Tree tree = path.getLeaf();
             if (LOGGABLE) log("  resolveToolTip on " + tree.getJavaFXKind());
             if (tree.getJavaFXKind() == Tree.JavaFXKind.METHOD_INVOCATION) {
                 FunctionInvocationTree mi = (FunctionInvocationTree) tree;
                 int startPos = lastTree != null ? (int) sourcePositions.getStartPosition(root, lastTree) : offset;
                 if (LOGGABLE) log("  startPos == " + startPos);
                 List<Tree> argTypes = getArgumentsUpToPos(mi.getArguments(), (int) sourcePositions.getEndPosition(root, mi.getMethodSelect()), startPos);
                 if (LOGGABLE) log("  argTypes = " + argTypes);
                 if (argTypes != null) {
                     TypeMirror[] types = new TypeMirror[argTypes.size()];
                     int j = 0;
                     for (Tree t : argTypes) {
                         types[j++] = controller.getTrees().getTypeMirror(JavaFXTreePath.getPath(root, t));
                         if (LOGGABLE) {
                             log("  types[j-1] == " + types[j-1]);
                         }
                     }
                     List<List<String>> params = null;
                     Tree mid = mi.getMethodSelect();
                     if (LOGGABLE) log("   mid == " + mid.getJavaFXKind() + mid);
                     if (LOGGABLE) {
                         log("    path " + path);
                         if (path != null) {
                             log("    path.getLeaf() == " + path.getLeaf());
                         }
                     }
                     path = new JavaFXTreePath(path, mid);
                     switch (mid.getJavaFXKind()) {
                         case MEMBER_SELECT: {
                             ExpressionTree exp = ((MemberSelectTree) mid).getExpression();
                             path = new JavaFXTreePath(path, exp);
                             if (LOGGABLE) log("   path == " + path.getLeaf());
                             JavafxcTrees trees = controller.getTrees();
                             final TypeMirror type = trees.getTypeMirror(path);
                             if (LOGGABLE) log("    type == " + type);
                             final Element element = trees.getElement(path);
                             if (LOGGABLE) log("    element == " + element);
                             final boolean isStatic = element != null && (element.getKind().isClass() || element.getKind().isInterface());
                             if (LOGGABLE) log("     isStatic == " + isStatic);
                             final boolean isSuperCall = element != null && element.getKind().isField() && element.getSimpleName().contentEquals(SUPER_KEYWORD);
                             final JavafxcScope scope = controller.getTreeUtilities().getScope(path);
                             if (LOGGABLE) log("   scope == " + scope);
                             final TreeUtilities tu = controller.getTreeUtilities();
                             TypeElement enclClass = scope.getEnclosingClass();
                             final TypeMirror enclType = enclClass != null ? enclClass.asType() : null;
                             ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
 
                                 public boolean accept(Element e, TypeMirror t) {
                                     boolean res = (!isStatic || e.getModifiers().contains(STATIC)) && tu.isAccessible(scope, e, isSuperCall && enclType != null ? enclType : t);
                                     if (LOGGABLE) {
                                         log("   accept for " + e + " on " + t);
                                         log("   returning " + res);
                                     }
                                     return res;
                                 }
                             };
                             params = getMatchingParams(type, controller.getElementUtilities().getMembers(type, acceptor), ((MemberSelectTree) mid).getIdentifier().toString(), types, controller.getTypes());
                             if (LOGGABLE) log("  params == " + params);
                             break;
                         }
                         case IDENTIFIER: {
                             final JavafxcScope scope = controller.getTreeUtilities().getScope(path);
                             if (LOGGABLE) log("   scope (2) == " + scope);
                             final TreeUtilities tu = controller.getTreeUtilities();
                             final TypeElement enclClass = scope.getEnclosingClass();
                             final boolean isStatic = enclClass != null ? (tu.isStaticContext(scope) || (path.getLeaf().getJavaFXKind() == Tree.JavaFXKind.BLOCK_EXPRESSION && ((BlockExpressionTree) path.getLeaf()).isStatic())) : false;
                             final ExecutableElement method = scope.getEnclosingMethod();
                             ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
 
                                 public boolean accept(Element e, TypeMirror t) {
                                     switch (e.getKind()) {
                                         case CONSTRUCTOR:
                                             return !e.getModifiers().contains(PRIVATE);
                                         case METHOD:
                                             return (!isStatic || e.getModifiers().contains(STATIC)) && tu.isAccessible(scope, e, t);
                                         default:
                                             return false;
                                     }
                                 }
                             };
                             String name = ((IdentifierTree) mid).getName().toString();
                             params = getMatchingParams(enclClass != null ? enclClass.asType() : null, controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), name, types, controller.getTypes());
                             if (LOGGABLE) log("  params (2) == " + params);
                             break;
                         }
                     }
                     if (LOGGABLE) log("  params (3) == " + params);
                     if (params != null) {
                         query.toolTip = new MethodParamsTipPaintComponent(params, types.length, query.component);
                     }
                     startPos = (int) sourcePositions.getEndPosition(root, mi.getMethodSelect());
                     String text = controller.getText().substring(startPos, offset);
                     query.anchorOffset = startPos + text.indexOf('('); //NOI18N
                     query.toolTipOffset = startPos + text.lastIndexOf(','); //NOI18N
                     if (query.toolTipOffset < query.anchorOffset) {
                         query.toolTipOffset = query.anchorOffset;
                     }
                     return;
                 }
             }
             lastTree = tree;
             path = path.getParentPath();
         }
     }
 
     protected void addMethodArguments(FunctionInvocationTree mit) throws IOException {
         if (LOGGABLE) log("addMethodArguments " + mit);
         List<Tree> argTypes = getArgumentsUpToPos(mit.getArguments(), (int)sourcePositions.getEndPosition(root, mit.getMethodSelect()), offset);
         JavafxcTrees trees = controller.getTrees();
         if (argTypes != null) {
             TypeMirror[] types = new TypeMirror[argTypes.size()];
             int j = 0;
             for (Tree t : argTypes) {
                 JavaFXTreePath jfxtp = new JavaFXTreePath(path, t);
                 if (LOGGABLE) log("    jfxtp == " + jfxtp.getLeaf());
                 types[j++] = controller.getTrees().getTypeMirror(jfxtp);
                 if (LOGGABLE) log("      types[j-1] == " + types[j-1]);
             }
             List<Pair<ExecutableElement, ExecutableType>> methods = null;
             String name = null;
             Tree mid = mit.getMethodSelect();
             if (LOGGABLE) {
                 log("    path " + path);
                 if (path != null) {
                     log("    path.getLeaf() == " + path.getLeaf());
                 }
             }
             path = new JavaFXTreePath(path, mid);
             switch (mid.getJavaFXKind()) {
                 case MEMBER_SELECT: {
                     ExpressionTree exp = ((MemberSelectTree)mid).getExpression();
                     path = new JavaFXTreePath(path, exp);
                     final TypeMirror type = trees.getTypeMirror(path);
                     final Element element = trees.getElement(path);
                     final boolean isStatic = element != null && (element.getKind().isClass() || element.getKind().isInterface());
                     final boolean isSuperCall = element != null && element.getKind().isField() && element.getSimpleName().contentEquals(SUPER_KEYWORD);
                     final TreeUtilities tu = controller.getTreeUtilities();
                     final JavafxcScope scope = tu.getScope(path);
                     TypeElement enclClass = scope.getEnclosingClass();
                     final TypeMirror enclType = enclClass != null ? enclClass.asType() : null;
                     ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                         public boolean accept(Element e, TypeMirror t) {
                             return (!isStatic || e.getModifiers().contains(STATIC) ) && tu.isAccessible(scope, e, isSuperCall && enclType != null ? enclType : t);
                         }
                     };
                     methods = getMatchingExecutables(type, controller.getElementUtilities().getMembers(type, acceptor), ((MemberSelectTree)mid).getIdentifier().toString(), types, controller.getTypes());
                     break;
                 }
                 case IDENTIFIER: {
                     final TreeUtilities tu = controller.getTreeUtilities();
                     final JavafxcScope scope = tu.getScope(path);
                     final TypeElement enclClass = scope.getEnclosingClass();
                     final boolean isStatic = enclClass != null ? (tu.isStaticContext(scope) || (path.getLeaf().getJavaFXKind() == JavaFXKind.BLOCK_EXPRESSION && ((BlockExpressionTree)path.getLeaf()).isStatic())) : false;
                     final ExecutableElement method = scope.getEnclosingMethod();
                     ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                         public boolean accept(Element e, TypeMirror t) {
                             switch (e.getKind()) {
                                 case LOCAL_VARIABLE:
                                 case EXCEPTION_PARAMETER:
                                 case PARAMETER:
                                     return (method == e.getEnclosingElement() || e.getModifiers().contains(FINAL));
                                 case FIELD:
                                     if (e.getSimpleName().contentEquals(THIS_KEYWORD) || e.getSimpleName().contentEquals(SUPER_KEYWORD))
                                         return !isStatic;
                             }
                             return (!isStatic || e.getModifiers().contains(STATIC)) && tu.isAccessible(scope, e, t);
                         }
                     };
                     name = ((IdentifierTree)mid).getName().toString();
                     if (SUPER_KEYWORD.equals(name) && enclClass != null) {
                         TypeMirror superclass = enclClass.getSuperclass();
                         methods = getMatchingExecutables(superclass, controller.getElementUtilities().getMembers(superclass, acceptor), INIT, types, controller.getTypes());
                     } else if (THIS_KEYWORD.equals(name) && enclClass != null) {
                         TypeMirror thisclass = enclClass.asType();
                         methods = getMatchingExecutables(thisclass, controller.getElementUtilities().getMembers(thisclass, acceptor), INIT, types, controller.getTypes());
                     } else {
                         Iterable<? extends Element> locals = controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor);
                         methods = getMatchingExecutables(enclClass != null ? enclClass.asType() : null, locals, name, types, controller.getTypes());
                         name = null;
                     }
                     break;
                 }
             }
             if (methods != null) {
                 Elements elements = controller.getElements();
                 for (Pair<ExecutableElement, ExecutableType> method : methods)
                    addResult(JavaFXCompletionItem.createParametersItem(method.a, method.b, query.anchorOffset, elements.isDeprecated(method.a), types.length, name));
             }
         }
     }
         private List<Tree> getArgumentsUpToPos(Iterable<? extends ExpressionTree> args, int startPos, int position) {
             List<Tree> ret = new ArrayList<Tree>();
             for (ExpressionTree e : args) {
                 int pos = (int)sourcePositions.getEndPosition(root, e);
                 if (pos != Diagnostic.NOPOS && position > pos) {
                     startPos = pos;
                     ret.add(e);
                 }
             }
             if (startPos < 0)
                 return ret;
             if (position > startPos) {
                 TokenSequence<JFXTokenId> last = findLastNonWhitespaceToken(startPos, position);
                 if (last != null && (last.token().id() == JFXTokenId.LPAREN || last.token().id() == JFXTokenId.COMMA))
                     return ret;
             }
             return null;
         }
 
         private List<Pair<ExecutableElement, ExecutableType>> getMatchingExecutables(TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, Types types) {
             List<Pair<ExecutableElement, ExecutableType>> ret = new ArrayList<Pair<ExecutableElement, ExecutableType>>();
             for (Element e : elements) {
                 if ((e.getKind() == ElementKind.CONSTRUCTOR || e.getKind() == ElementKind.METHOD) && name.contentEquals(e.getSimpleName())) {
                     List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();
                     int parSize = params.size();
                     boolean varArgs = ((ExecutableElement)e).isVarArgs();
                     if (!varArgs && (parSize < argTypes.length)) {
                         continue;
                     }
                     ExecutableType eType = (ExecutableType)asMemberOf(e, type, types);
                     if (parSize == 0) {
                         ret.add(new Pair<ExecutableElement,ExecutableType>((ExecutableElement)e, eType));
                     } else {
                         Iterator<? extends TypeMirror> parIt = eType.getParameterTypes().iterator();
                         TypeMirror param = null;
                         for (int i = 0; i <= argTypes.length; i++) {
                             if (parIt.hasNext()) {
                                 param = parIt.next();
 //                                if (!parIt.hasNext() && param.getKind() == TypeKind.ARRAY)
 //                                    param = ((ArrayType)param).getComponentType();
                             } else if (!varArgs) {
                                 break;
                             }
                             if (i == argTypes.length) {
                                 ret.add(new Pair<ExecutableElement, ExecutableType>((ExecutableElement)e, eType));
                                 break;
                             }
                             if (argTypes[i] == null || !types.isAssignable(argTypes[i], param))
                                 break;
                         }
                     }
                 }
             }
             return ret;
         }
 
         private List<List<String>> getMatchingParams(TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, Types types) {
             if (LOGGABLE) log("getMatchingParams type == " + type + " name == " + name);
             List<List<String>> ret = new ArrayList<List<String>>();
             for (Element e : elements) {
                 if (LOGGABLE) log("   e == " + e);
                 if ((e.getKind() == ElementKind.CONSTRUCTOR || e.getKind() == ElementKind.METHOD) && name.contentEquals(e.getSimpleName())) {
                     List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();
                     int parSize = params.size();
                     if (LOGGABLE) log("   parSize == " + parSize);
                     boolean varArgs = ((ExecutableElement)e).isVarArgs();
                     if (!varArgs && (parSize < argTypes.length)) {
                         continue;
                     }
                     if (parSize == 0) {
                         ret.add(Collections.<String>singletonList(NbBundle.getMessage(JavaFXCompletionProvider.class, "JCP-no-parameters")));
                     } else {
                         ExecutableType eType = (ExecutableType)asMemberOf(e, type, types);
                         if (LOGGABLE) log("  eType == " + eType);
                         Iterator<? extends TypeMirror> parIt = eType.getParameterTypes().iterator();
                         TypeMirror param = null;
                         for (int i = 0; i <= argTypes.length; i++) {
                             log("    i == " + i);
                             if (parIt.hasNext()) {
                                 param = parIt.next();
                                 if (LOGGABLE) log("      param == " + param);
                                 if (!parIt.hasNext() && param.getKind() == TypeKind.ARRAY)
                                     param = ((ArrayType)param).getComponentType();
                             } else if (!varArgs) {
                                 break;
                             }
                             if (i == argTypes.length) {
                                 if (LOGGABLE) log("   i == argTypes.length");
                                 List<String> paramStrings = new ArrayList<String>(parSize);
                                 Iterator<? extends TypeMirror> tIt = eType.getParameterTypes().iterator();
                                 for (Iterator<? extends VariableElement> it = params.iterator(); it.hasNext();) {
                                     VariableElement ve = it.next();
                                     StringBuffer sb = new StringBuffer();
                                     sb.append(tIt.next());
                                     if (varArgs && !tIt.hasNext())
                                         sb.delete(sb.length() - 2, sb.length()).append("..."); //NOI18N
                                     CharSequence veName = ve.getSimpleName();
                                     if (veName != null && veName.length() > 0) {
                                         sb.append(" "); // NOI18N
                                         sb.append(veName);
                                     }
                                     if (it.hasNext()) {
                                         sb.append(", "); // NOI18N
                                     }
                                     paramStrings.add(sb.toString());
                                 }
                                 ret.add(paramStrings);
                                 break;
                             }
                             if (LOGGABLE) log("    will check " + argTypes[i] + " " + param);
                             if (argTypes[i] != null && !types.isAssignable(argTypes[i], param)) {
                                 break;
                             }
                         }
                     }
                 } else {
                     if (LOGGABLE) log("   e.getKind() == " + e.getKind());
                 }
             }
             return ret.isEmpty() ? null : ret;
         }
 
 //        private Set<TypeMirror> getMatchingArgumentTypes(TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, TypeMirror[] typeArgTypes, Types types, TypeUtilities tu) {
 //            Set<TypeMirror> ret = new HashSet<TypeMirror>();
 //            for (Element e : elements) {
 //                if ((e.getKind() == CONSTRUCTOR || e.getKind() == METHOD) && name.contentEquals(e.getSimpleName())) {
 //                    List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();
 //                    int parSize = params.size();
 //                    boolean varArgs = ((ExecutableElement)e).isVarArgs();
 //                    if (!varArgs && (parSize <= argTypes.length))
 //                        continue;
 //                    ExecutableType meth = (ExecutableType)asMemberOf(e, type, types);
 //                    Iterator<? extends TypeMirror> parIt = meth.getParameterTypes().iterator();
 //                    TypeMirror param = null;
 //                    for (int i = 0; i <= argTypes.length; i++) {
 //                        if (parIt.hasNext())
 //                            param = parIt.next();
 //                        else if (!varArgs)
 //                            break;
 //                        if (i == argTypes.length) {
 //                            if (typeArgTypes != null && param.getKind() == TypeKind.DECLARED && typeArgTypes.length == meth.getTypeVariables().size())
 //                                param = tu.substitute(param, meth.getTypeVariables(), Arrays.asList(typeArgTypes));
 //                            TypeMirror toAdd = null;
 //                            if (i < parSize)
 //                                toAdd = param;
 //                            if (varArgs && !parIt.hasNext() && param.getKind() == TypeKind.ARRAY)
 //                                toAdd = ((ArrayType)param).getComponentType();
 //                            if (toAdd != null && ret.add(toAdd)) {
 //                                TypeMirror toRemove = null;
 //                                for (TypeMirror tm : ret) {
 //                                    if (tm != toAdd) {
 //                                        TypeMirror tmErasure = types.erasure(tm);
 //                                        TypeMirror toAddErasure = types.erasure(toAdd);
 //                                        if (types.isSubtype(toAddErasure, tmErasure)) {
 //                                            toRemove = toAdd;
 //                                            break;
 //                                        } else if (types.isSubtype(tmErasure, toAddErasure)) {
 //                                            toRemove = tm;
 //                                            break;
 //                                        }
 //                                    }
 //                                }
 //                                if (toRemove != null)
 //                                    ret.remove(toRemove);
 //                            }
 //                            break;
 //                        }
 //                        if (argTypes[i] == null)
 //                            break;
 //                        if (varArgs && !parIt.hasNext() && param.getKind() == TypeKind.ARRAY) {
 //                            if (types.isAssignable(argTypes[i], param))
 //                                varArgs = false;
 //                            else if (!types.isAssignable(argTypes[i], ((ArrayType)param).getComponentType()))
 //                                break;
 //                        } else if (!types.isAssignable(argTypes[i], param))
 //                            break;
 //                    }
 //                }
 //            }
 //            return ret.isEmpty() ? null : ret;
 //        }
 
     protected void addKeyword(String kw, String postfix, boolean smartType) {
         if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(kw, postfix, query.anchorOffset, smartType));
         }
     }
 
     protected void addKeywordsForClassBody() {
         for (String kw : CLASS_BODY_KEYWORDS) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, SPACE, query.anchorOffset, false));
             }
         }
     }
 
     @SuppressWarnings("fallthrough")
     protected void addKeywordsForStatement() {
         if (LOGGABLE) log("addKeywordsForStatement");
         for (String kw : STATEMENT_KEYWORDS) {
             String postfix = " (";
             if (TRY_KEYWORD.equals(kw)) {
                 postfix = " {";
             }
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, postfix, query.anchorOffset, false));
             }
         }
         for (String kw : STATEMENT_SPACE_KEYWORDS) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, SPACE, query.anchorOffset, false));
             }
         }
         if (JavaFXCompletionProvider.startsWith(RETURN_KEYWORD, prefix)) {
             JavaFXTreePath mth = JavaFXCompletionProvider.getPathElementOfKind(Tree.JavaFXKind.FUNCTION_DEFINITION, path);
             if (LOGGABLE) log("   mth == " + mth);
             String postfix = SPACE;
             if (mth != null) {
                 Tree rt = ((FunctionDefinitionTree) mth.getLeaf()).getFunctionValue().getType();
                 if (LOGGABLE) log("    rt == " + rt + "   kind == " + (rt == null?"":rt.getJavaFXKind()));
                 if ((rt == null) || (rt.getJavaFXKind() == JavaFXKind.TYPE_UNKNOWN)) {
                     postfix = SEMI;
                 }
                 // TODO: handle Void return type ...
             }
             addResult(JavaFXCompletionItem.createKeywordItem(RETURN_KEYWORD, postfix, query.anchorOffset, false));
         }
         JavaFXTreePath tp = getPath();
         while (tp != null) {
             switch (tp.getLeaf().getJavaFXKind()) {
                 case FOR_EXPRESSION_IN_CLAUSE:
                 case FOR_EXPRESSION_FOR:
                 case WHILE_LOOP:
                     if (JavaFXCompletionProvider.startsWith(CONTINUE_KEYWORD, prefix)) {
                         addResult(JavaFXCompletionItem.createKeywordItem(CONTINUE_KEYWORD, SEMI, query.anchorOffset, false));
                     }
 
 /*
                 case SWITCH:
                     if (JavaFXCompletionProvider.startsWith(BREAK_KEYWORD, prefix)) {
                         addResult(JavaFXCompletionItem.createKeywordItem(BREAK_KEYWORD, SEMI, query.anchorOffset, false));
                     }
                     break;*/
             }
             tp = tp.getParentPath();
         }
     }
 
     protected void addValueKeywords() {
         if (JavaFXCompletionProvider.startsWith(FALSE_KEYWORD, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(FALSE_KEYWORD, null, query.anchorOffset, false));
         }
         if (JavaFXCompletionProvider.startsWith(TRUE_KEYWORD, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(TRUE_KEYWORD, null, query.anchorOffset, false));
         }
         if (JavaFXCompletionProvider.startsWith(NULL_KEYWORD, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(NULL_KEYWORD, null, query.anchorOffset, false));
         }
         if (JavaFXCompletionProvider.startsWith(NEW_KEYWORD, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(NEW_KEYWORD, SPACE, query.anchorOffset, false));
         }
         if (JavaFXCompletionProvider.startsWith(BIND_KEYWORD, prefix)) {
             addResult(JavaFXCompletionItem.createKeywordItem(BIND_KEYWORD, SPACE, query.anchorOffset, false));
         }
     }
 
     protected void addClassModifiers(Set<Modifier> modifiers) {
         List<String> kws = new ArrayList<String>();
         if (!modifiers.contains(PUBLIC) && !modifiers.contains(PRIVATE)) {
             kws.add(PUBLIC_KEYWORD);
         }
         if (!modifiers.contains(FINAL) && !modifiers.contains(ABSTRACT)) {
             kws.add(ABSTRACT_KEYWORD);
         }
         kws.add(CLASS_KEYWORD);
         for (String kw : kws) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, SPACE, query.anchorOffset, false));
             }
         }
     }
 
     protected void addMemberModifiers(Set<Modifier> modifiers, boolean isLocal) {
         if (LOGGABLE) log("addMemberModifiers");
         List<String> kws = new ArrayList<String>();
         if (isLocal) {
             // TODO:
         } else {
             kws.add(PUBLIC_KEYWORD);
             kws.add(PROTECTED_KEYWORD);
             kws.add(PACKAGE_KEYWORD);
             kws.add(PUBLIC_INIT_KEYWORD);
             kws.add(PUBLIC_READ_KEYWORD);
             // TODO: check this:
             if (!modifiers.contains(FINAL) && !modifiers.contains(ABSTRACT)) {
                 kws.add(ABSTRACT_KEYWORD);
             }
         }
         for (String kw : kws) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, SPACE, query.anchorOffset, false));
             }
         }
     }
 
     /**
      * This methods hacks over issue #135926. To prevent NPE we first complete
      * all symbols that are classes and not inner classes in a package.
      * @param pe
      * @return pe.getEnclosedElements() but without the NPE
      */
     private List<? extends Element> getEnclosedElements(PackageElement pe) {
         Symbol s = (Symbol)pe;
         for (Scope.Entry e = s.members().elems; e != null; e = e.sibling) {
             if (e.sym != null) {
                 try {
                     e.sym.complete();
                 } catch (RuntimeException x) {
                     if (logger.isLoggable(Level.FINEST)) {
                         logger.log(Level.FINEST,"Let's see whether we survive this: ",x);
                     }
                 }
             }
         }
         return pe.getEnclosedElements();
     }
     
     protected void addPackageContent(PackageElement pe, EnumSet<ElementKind> kinds, DeclaredType baseType, boolean insideNew) {
         if (LOGGABLE) log("addPackageContent " + pe);
         Elements elements = controller.getElements();
         JavafxTypes types = controller.getJavafxTypes();
         JavafxcScope scope = controller.getTreeUtilities().getScope(path);
         for (Element e : getEnclosedElements(pe)) {
             if (e.getKind().isClass() || e.getKind() == ElementKind.INTERFACE) {
                 String name = e.getSimpleName().toString();
                 if (! controller.getTreeUtilities().isAccessible(scope, e)) {
                     if (LOGGABLE) log("    not accessible " + name);
                     continue;
                 }
                 if (JavaFXCompletionProvider.startsWith(name, prefix) &&
                         !name.contains("$")) {
                     addResult(JavaFXCompletionItem.createTypeItem((TypeElement) e, (DeclaredType) e.asType(), query.anchorOffset, elements.isDeprecated(e), insideNew, false, false));
                 }
                 for (Element ee : e.getEnclosedElements()) {
                     if (ee.getKind().isClass() || ee.getKind() == ElementKind.INTERFACE) {
                         String ename = ee.getSimpleName().toString();
                         if (!controller.getTreeUtilities().isAccessible(scope, ee)) {
                             if (LOGGABLE) log("    not accessible " + ename);
                             continue;
                         }
                         log(ename + " isJFXClass " + types.isJFXClass((Symbol) ee));
                         if (JavaFXCompletionProvider.startsWith(ename, prefix) &&
                                 types.isJFXClass((Symbol) ee)) {
                             addResult(JavaFXCompletionItem.createTypeItem((TypeElement) ee, (DeclaredType) ee.asType(), query.anchorOffset, elements.isDeprecated(ee), insideNew, false, false));
                         }
                     }
                 }
             }
         }
         String pkgName = pe.getQualifiedName() + "."; //NOI18N
         if (prefix != null && prefix.length() > 0) {
             pkgName += prefix;
         }
         addPackages(pkgName);
     }
 
     protected void addBasicTypes() {
         if (LOGGABLE) log("addBasicTypes ");
         addBasicType("Boolean", "boolean");
         addBasicType("Integer", "int");
         addBasicType("Number", "double");
         addBasicType("String", "String");
     }
 
     private void addBasicType(String name1, String name2) {
         if (LOGGABLE) log("  addBasicType " + name1 + " : " + name2);
         JavafxcScope scope = controller.getTreeUtilities().getScope(path);
         while (scope != null) {
             if (LOGGABLE) log("  scope == " + scope);
             for (Element local : scope.getLocalElements()) {
                 if (LOGGABLE) log("    local == " + local.getSimpleName() + "  kind: " + local.getKind() + "  class: " + local.getClass().getName() + "  asType: " + local.asType());
                 if (local.getKind().isClass() || local.getKind() == ElementKind.INTERFACE) {
                     if (! (local instanceof TypeElement)) {
                         if (LOGGABLE) log("    " + local.getSimpleName() + " not TypeElement");
                         continue;
                     }
                     TypeElement te = (TypeElement) local;
                     String name = local.getSimpleName().toString();
                     if (name.equals(name2)) {
                         if (JavaFXCompletionProvider.startsWith(name1, prefix)) {
                             if (LOGGABLE) log("    found " + name1);
                             if (local.asType() == null || local.asType().getKind() != TypeKind.DECLARED) {
                                 addResult(JavaFXCompletionItem.createTypeItem(name1, query.anchorOffset, false, false, true));
                             } else {
                                 DeclaredType dt = (DeclaredType) local.asType();
                                 addResult(JavaFXCompletionItem.createTypeItem(te, dt, query.anchorOffset, false, false, true, false));
                             }
                             return;
                         }
                     }
                 }
             }
             scope = scope.getEnclosingScope();
         }
     }
     
     protected void addLocalAndImportedTypes(final EnumSet<ElementKind> kinds, final DeclaredType baseType, final Set<? extends Element> toExclude, boolean insideNew, TypeMirror smart) {
         if (LOGGABLE) log("addLocalAndImportedTypes");
         JavafxcTrees trees = controller.getTrees();
         JavafxcScope scope = controller.getTreeUtilities().getScope(path);
         JavafxcScope originalScope = scope;
         while (scope != null) {
             if (LOGGABLE) log("  scope == " + scope);
             addLocalAndImportedTypes(scope.getLocalElements(), kinds, baseType, toExclude, insideNew, smart, originalScope, null,false);
             scope = scope.getEnclosingScope();
         }
         Element e = trees.getElement(path);
         while (e != null && e.getKind() != ElementKind.PACKAGE) {
             e = e.getEnclosingElement();
         }
         if (e != null) {
             if (LOGGABLE) log("will scan package " + e.getSimpleName());
             PackageElement pkge = (PackageElement)e;
             addLocalAndImportedTypes(getEnclosedElements(pkge), kinds, baseType, toExclude, insideNew, smart, originalScope, pkge,false);
         }
         addPackages("");
         if (query.queryType == JavaFXCompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
             addAllTypes(kinds, insideNew, prefix);
         } else {
             query.hasAdditionalItems = true;
         }
     }
     
     private void addLocalAndImportedTypes(Iterable<? extends Element> from,
             final EnumSet<ElementKind> kinds, final DeclaredType baseType, 
             final Set<? extends Element> toExclude,
             boolean insideNew, TypeMirror smart, JavafxcScope originalScope,
             PackageElement myPackage,boolean simpleNameOnly) {
         final Elements elements = controller.getElements();
         for (Element local : from) {
             if (LOGGABLE) log("    local == " + local);
             String name = local.getSimpleName().toString();
             if (name.contains("$")) {
                 continue;
             }
             if (local.getKind().isClass() || local.getKind() == ElementKind.INTERFACE) {
                 if (local.asType() == null || local.asType().getKind() != TypeKind.DECLARED) {
                     continue;
                 }
                 DeclaredType dt = (DeclaredType) local.asType();
                 TypeElement te = (TypeElement) local;
                 if (!controller.getTreeUtilities().isAccessible(originalScope, te)) {
                     if (LOGGABLE) log("    not accessible " + name);
                     continue;
                 }
                 Element parent = te.getEnclosingElement();
                 if (parent.getKind() == ElementKind.CLASS) {
                     if (!controller.getTreeUtilities().isAccessible(originalScope, parent)) {
                         if (LOGGABLE) log("    parent not accessible " + name);
                         continue;
                     }
                 }
                 if (smart != null && local.asType() == smart) {
                     addResult(JavaFXCompletionItem.createTypeItem(te, dt, query.anchorOffset, elements.isDeprecated(local), insideNew, true, false));
                 }
                 if (JavaFXCompletionProvider.startsWith(name, prefix) && !name.contains("$")) {
                     if (simpleNameOnly) {
                         addResult(JavaFXCompletionItem.createTypeItem(local.getSimpleName().toString(), query.anchorOffset, elements.isDeprecated(local), insideNew, false));
                     } else {
                         addResult(JavaFXCompletionItem.createTypeItem(te, dt, query.anchorOffset, elements.isDeprecated(local), insideNew, false, false));
                     }
                 }
                 if (parent == myPackage) {
                    if (LOGGABLE) log("   will check inner classes of: " + local);
                    if (local.getEnclosedElements() != null) {
                         addLocalAndImportedTypes(local.getEnclosedElements(), kinds, baseType, toExclude, insideNew, smart, originalScope, null,true);
                    }
                 }
             }
         }
     }
 
     protected void addLocalAndImportedFunctions() {
         if (LOGGABLE) log("addLocalAndImportedFunctions");
         JavafxcScope scope = controller.getTreeUtilities().getScope(path);
         while (scope != null) {
             if (LOGGABLE) log("  scope == " + scope);
             for (Element local : scope.getLocalElements()) {
                 if (LOGGABLE) log("    local == " + local);
                 String name = local.getSimpleName().toString();
                 if (name.contains("$")) {
                     continue;
                 }
                 if (local.getKind() == ElementKind.METHOD) {
                     if (JavaFXCompletionProvider.startsWith(name, prefix) && !name.contains("$")) {
                         addResult(JavaFXCompletionItem.createExecutableItem(
                                 (ExecutableElement) local,
                                 (ExecutableType) local.asType(),
                                 query.anchorOffset, false, false, false, false));
                     }
                 }
             }
             scope = scope.getEnclosingScope();
         }
     }
 
     /**
      * @param simpleName name of a class or fully qualified name of a class
      * @return TypeElement or null if the passed in String does not denote a class
      */
     protected TypeElement findTypeElement(String simpleName) {
         if (LOGGABLE) log("findTypeElement: " + simpleName);
         JavafxcTrees trees = controller.getTrees();
         JavaFXTreePath p = new JavaFXTreePath(root);
         JavafxcScope scope = controller.getTreeUtilities().getScope(path);
         while (scope != null) {
             if (LOGGABLE) log("  scope == " + scope);
             TypeElement res = findTypeElement(scope.getLocalElements(), simpleName,null);
             if (res != null) {
                 return res;
             }
             scope = scope.getEnclosingScope();
         }
         Element e = trees.getElement(p);
         while (e != null && e.getKind() != ElementKind.PACKAGE) {
             e = e.getEnclosingElement();
         }
         if (e != null) {
             PackageElement pkge = (PackageElement)e;
             return findTypeElement(getEnclosedElements(pkge), simpleName,pkge);
         }
         return null;
     }
 
     /**
      * @param simpleName name of a class or fully qualified name of a class
      * @param myPackage can be null - if not null the inner classes of classes from this package will be checked
      * @return TypeElement or null if the passed in String does not denote a class
      */
     private TypeElement findTypeElement(Iterable<? extends Element> from, String simpleName,PackageElement myPackage) {
         if (LOGGABLE) log("  private findTypeElement " + simpleName + " in package " + myPackage);
         Elements elements = controller.getElements();
         for (Element local : from) {
             if (LOGGABLE) log("    local == " + local.getSimpleName() + "  kind: " + local.getKind() + "  class: " + local.getClass().getName() + "  asType: " + local.asType());
             if (local.getKind().isClass() || local.getKind() == ElementKind.INTERFACE) {
                 if (local.asType() == null || local.asType().getKind() != TypeKind.DECLARED) {
                     if (LOGGABLE) log("        is not TypeKind.DECLARED -- ignoring");
                     continue;
                 }
                 if (local instanceof TypeElement) {
                     String name = local.getSimpleName().toString();
                     if (name.equals(simpleName)) {
                         return (TypeElement) local;
                     }
 
                     PackageElement pe = elements.getPackageOf(local);
                     String fullName = pe.getQualifiedName().toString() + '.' + name;
                     if (fullName.equals(simpleName)) {
                         return (TypeElement) local;
                     }
                     if (pe == myPackage) {
                         if (LOGGABLE) log("   will check inner classes of: " + local);
                         TypeElement res = findTypeElement(local.getEnclosedElements(), simpleName,null);
                         if (res != null) {
                             return res;
                         }
                     }
                 }
             }
         }
         return null;
     }
     
     protected void addAllTypes(EnumSet<ElementKind> kinds, boolean insideNew, String myPrefix) {
         if (LOGGABLE) log(" addAllTypes ");
         for (ElementHandle<TypeElement> name :
             controller.getJavaFXSource().getCpInfo().getClassIndex().getDeclaredTypes(
                 myPrefix != null ? myPrefix : EMPTY,
                 NameKind.PREFIX,
                 EnumSet.allOf(SearchScope.class))) {
             String[] sigs = name.getSignatures();
             if ((sigs == null) || (sigs.length == 0)) {
                 continue;
             }
             String sig = sigs[0];
             int firstDollar = sig.indexOf('$');
             if (firstDollar >= 0) {
                 int secondDollar = sig.indexOf('$', firstDollar);
                 if (secondDollar >= 0) {
                     // we don't want to show second level inner classes
                     continue;
                 }
             }
             if (!name.getQualifiedName().startsWith("com.sun.") &&
                (!name.getQualifiedName().startsWith("sun."))
                 ) {
                 LazyTypeCompletionItem item = LazyTypeCompletionItem.create(
                         name, kinds,
                         query.anchorOffset,
                         controller.getJavaFXSource(), insideNew);
                 addResult(item);
             }
         }
     }
 
     protected TokenSequence<JFXTokenId> findLastNonWhitespaceToken(Tree tree, int position) {
         int startPos = (int) getSourcePositions().getStartPosition(root, tree);
         return findLastNonWhitespaceToken(startPos, position);
     }
 
     protected TokenSequence<JFXTokenId> findLastNonWhitespaceToken(int startPos, int endPos) {
         TokenSequence<JFXTokenId> ts = ((TokenHierarchy<?>) controller.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
         ts.move(endPos);
         ts = previousNonWhitespaceToken(ts);
         if (ts == null || ts.offset() < startPos) {
             return null;
         }
         return ts;
     }
 
     private TokenSequence<JFXTokenId> findFirstNonWhitespaceToken(Tree tree, int position) {
         int startPos = (int) getSourcePositions().getStartPosition(root, tree);
         return findFirstNonWhitespaceToken(startPos, position);
     }
 
     protected TokenSequence<JFXTokenId> findFirstNonWhitespaceToken(int startPos, int endPos) {
         TokenSequence<JFXTokenId> ts = ((TokenHierarchy<?>) controller.getTokenHierarchy()).tokenSequence(JFXTokenId.language());
         ts.move(startPos);
         ts = nextNonWhitespaceToken(ts);
         if (ts == null || ts.offset() >= endPos) {
             return null;
         }
         return ts;
     }
 
     protected Tree getArgumentUpToPos(Iterable<? extends ExpressionTree> args, int startPos, int cursorPos) {
         for (ExpressionTree e : args) {
             int argStart = (int) sourcePositions.getStartPosition(root, e);
             int argEnd = (int) sourcePositions.getEndPosition(root, e);
             if (argStart == Diagnostic.NOPOS || argEnd == Diagnostic.NOPOS) {
                 continue;
             }
             if (cursorPos >= argStart && cursorPos < argEnd) {
                 return e;
             } else {
                 TokenSequence<JFXTokenId> last = findLastNonWhitespaceToken(startPos, cursorPos);
                 if (last == null) {
                     continue;
                 }
                 if (last.token().id() == JFXTokenId.LPAREN) {
                     return e;
                 }
                 if (last.token().id() == JFXTokenId.COMMA && cursorPos - 1 == argEnd) {
                     return e;
                 }
             }
         }
         return null;
     }
     
     protected static TokenSequence<JFXTokenId> nextNonWhitespaceToken(TokenSequence<JFXTokenId> ts) {
         while (ts.moveNext()) {
             switch (ts.token().id()) {
                 case WS:
                 case LINE_COMMENT:
                 case COMMENT:
                 case DOC_COMMENT:
                     break;
                 default:
                     return ts;
             }
         }
         return null;
     }
 
     private static TokenSequence<JFXTokenId> previousNonWhitespaceToken(TokenSequence<JFXTokenId> ts) {
         while (ts.movePrevious()) {
             switch (ts.token().id()) {
                 case WS:
                 case LINE_COMMENT:
                 case COMMENT:
                 case DOC_COMMENT:
                     break;
                 default:
                     return ts;
             }
         }
         return null;
     }
 
     protected static TypeMirror asMemberOf(Element element, TypeMirror type, Types types) {
         TypeMirror ret = element.asType();
         TypeMirror enclType = element.getEnclosingElement().asType();
         if (enclType.getKind() == TypeKind.DECLARED) {
             enclType = types.erasure(enclType);
         }
         while (type != null && type.getKind() == TypeKind.DECLARED) {
             if (types.isSubtype(type, enclType)) {
                 ret = types.asMemberOf((DeclaredType) type, element);
                 break;
             }
             type = ((DeclaredType) type).getEnclosingType();
         }
         return ret;
     }
 
     private static void log(String s) {
         if (LOGGABLE) {
             logger.fine(s);
         }
     }
 
     private static class Pair<A, B> {
 
         private A a;
         private B b;
 
         private Pair(A a, B b) {
             this.a = a;
             this.b = b;
         }
     }
 
 }
