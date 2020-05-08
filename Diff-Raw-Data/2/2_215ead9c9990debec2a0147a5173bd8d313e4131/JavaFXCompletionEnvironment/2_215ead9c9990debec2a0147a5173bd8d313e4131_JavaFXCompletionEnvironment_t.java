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
 import com.sun.javafx.api.tree.ErroneousTree;
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
 import com.sun.tools.javafx.api.JavafxcScope;
 import com.sun.tools.javafx.api.JavafxcTrees;
 import com.sun.tools.javafx.code.JavafxTypes;
 import com.sun.tools.javafx.tree.JFXClassDeclaration;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.PackageElement;
 import static javax.lang.model.element.Modifier.*;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.ExecutableType;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 import javax.lang.model.util.Elements;
 import javax.lang.model.util.Types;
 import javax.swing.text.BadLocationException;
 import javax.tools.Diagnostic;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.ClasspathInfo;
 import org.netbeans.api.javafx.source.ClasspathInfo.PathKind;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.editor.indent.api.IndentUtils;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileSystem;
 import org.openide.filesystems.FileUtil;
 import static org.netbeans.modules.javafx.editor.completion.JavaFXCompletionQuery.*;
 
 /**
  *
  * @author David Strupl, Anton Chechel
  */
 public class JavaFXCompletionEnvironment<T extends Tree> {
 
     private static final Logger logger = Logger.getLogger(JavaFXCompletionEnvironment.class.getName());
     private static final boolean LOGGABLE = logger.isLoggable(Level.FINE);
     private static int usingFakeSource = 0;
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
         if (LOGGABLE) log("NOT IMPLEMENTED inside " + t);
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
 
     protected void addMembers(final TypeMirror type, final boolean methods, final boolean fields) throws IOException {
         addMembers(type, methods, fields, null);
     }
     
     protected void addMembers(final TypeMirror type, final boolean methods, final boolean fields, final String textToAdd) throws IOException {
         if (LOGGABLE) log("addMembers: " + type);
         JavafxcTrees trees = controller.getTrees();
         JavaFXTreePath p = new JavaFXTreePath(root);
         JavafxcScope scope = trees.getScope(p);
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
             String s = member.getSimpleName().toString();
             if (!trees.isAccessible(scope, member,dt)) {
                 if (LOGGABLE) log("    not accessible " + s);
                 continue;
             }
             if (fields && member.getKind() == ElementKind.FIELD) {
                 if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                     addResult(JavaFXCompletionItem.createVariableItem(s, offset, textToAdd, true));
                 }
             }
         }
 
         for (Element member : elements.getAllMembers(te)) {
             if (LOGGABLE) log("    member2 == " + member + " member2.getKind() " + member.getKind());
             String s = member.getSimpleName().toString();
             if ("<error>".equals(member.getSimpleName().toString())) {
                 continue;
             }
             if (!trees.isAccessible(scope, member,dt)) {
                 if (LOGGABLE) log("    not accessible " + s);
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
                             offset, false, false, false, false));
                 }
             } else if (fields && member.getKind() == ElementKind.FIELD) {
                 if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                     addResult(JavaFXCompletionItem.createVariableItem(s, offset, textToAdd, false));
                 }
             }
         }
     }
 
     protected void localResult(TypeMirror smart) throws IOException {
         addLocalMembersAndVars(smart);
         addLocalAndImportedTypes(null, null, null, false, smart);
     }
 
     protected void addMemberConstantsAndTypes(final TypeMirror type, final Element elem) throws IOException {
         if (LOGGABLE) log("addMemberConstantsAndTypes: " + type + " elem: " + elem);
     }
 
     protected void addLocalMembersAndVars(TypeMirror smart) throws IOException {
         if (LOGGABLE) log("addLocalMembersAndVars: " + prefix);
 //        controller.toPhase(Phase.ANALYZED);
 
         final JavafxcTrees trees = controller.getTrees();
         if (smart != null && smart.getKind() == TypeKind.DECLARED) {
             if (LOGGABLE) log("adding declared type + subtypes: " + smart);
             DeclaredType dt = (DeclaredType) smart;
             TypeElement elem = (TypeElement) dt.asElement();
             addResult(JavaFXCompletionItem.createTypeItem(elem, dt, offset, false, false, true));
 
             for (DeclaredType subtype : getSubtypesOf((DeclaredType) smart)) {
                 TypeElement subElem = (TypeElement) subtype.asElement();
                 addResult(JavaFXCompletionItem.createTypeItem(subElem, subtype, offset, false, false, true));
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
                         addResult(JavaFXCompletionItem.createVariableItem(s, offset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(s, offset, false));
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
                         addResult(JavaFXCompletionItem.createVariableItem(s, offset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(s, offset, false));
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
                         addResult(JavaFXCompletionItem.createVariableItem(s1, offset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s1, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(s1, offset, false));
                     }
                 }
                 VariableTree varTree2 = ort.getOldValue();
                 if (varTree2 != null) {
                     String s2 = varTree2.getName().toString();
                     if (LOGGABLE) log("    adding(5) " + s2 + " with prefix " + prefix);
                     TypeMirror tm = trees.getTypeMirror(new JavaFXTreePath(tp, varTree2));
                     if (smart != null && tm.getKind() == smart.getKind()) {
                         addResult(JavaFXCompletionItem.createVariableItem(s2, offset, true));
                     }
                     if (JavaFXCompletionProvider.startsWith(s2, prefix)) {
                         addResult(JavaFXCompletionItem.createVariableItem(s2, offset, false));
                     }
                 }
             }
         }
     }
 
     private void addBlockExpressionLocals(BlockExpressionTree bet, JavaFXTreePath tp, TypeMirror smart) {
         if (LOGGABLE) log("  block expression: " + bet + "\n");
         for (ExpressionTree st : bet.getStatements()) {
             JavaFXTreePath expPath = new JavaFXTreePath(tp, st);
             if (LOGGABLE) log("    expPath == " + expPath.getLeaf());
             JavafxcTrees trees = controller.getTrees();
             Element type = trees.getElement(expPath);
             if (type == null) {
                 continue;
             }
             if (LOGGABLE) log("    type.getKind() == " + type.getKind());
             if (type.getKind() == ElementKind.LOCAL_VARIABLE) {
                 String s = type.getSimpleName().toString();
                 if (LOGGABLE) log("    adding(1) " + s + " with prefix " + prefix);
                 TypeMirror tm = trees.getTypeMirror(expPath);
                 if (smart != null && tm.getKind() == smart.getKind()) {
                     addResult(JavaFXCompletionItem.createVariableItem(
                             s, offset, true));
                 }
                 if (JavaFXCompletionProvider.startsWith(s, getPrefix())) {
                     addResult(JavaFXCompletionItem.createVariableItem(
                             s, offset, false));
                 }
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
                         addResult(JavaFXCompletionItem.createPackageItem(s, offset, false));
                     }
                 }
             }
         }
     }
 
     protected List<DeclaredType> getSubtypesOf(DeclaredType baseType) throws IOException {
         if (LOGGABLE) log("NOT IMPLEMENTED: getSubtypesOf " + baseType);
         return Collections.emptyList();
     }
 
     protected void addMethodArguments(FunctionInvocationTree mit) throws IOException {
         if (LOGGABLE) log("NOT IMPLEMENTED: addMethodArguments " + mit);
     }
 
     protected void addKeyword(String kw, String postfix, boolean smartType) {
         if (JavaFXCompletionProvider.startsWith(kw, getPrefix())) {
             addResult(JavaFXCompletionItem.createKeywordItem(kw, postfix, query.anchorOffset, smartType));
         }
     }
 
     protected void addKeywordsForCU() {
         List<String> kws = new ArrayList<String>();
         kws.add(ABSTRACT_KEYWORD);
         kws.add(CLASS_KEYWORD);
         kws.add(VAR_KEYWORD);
         kws.add(FUNCTION_KEYWORD);
         kws.add(PUBLIC_KEYWORD);
         kws.add(IMPORT_KEYWORD);
         boolean beforeAnyClass = true;
         for (Tree t : root.getTypeDecls()) {
             if (t.getJavaFXKind() == Tree.JavaFXKind.CLASS_DECLARATION) {
                 int pos = (int) sourcePositions.getEndPosition(root, t);
                 if (pos != Diagnostic.NOPOS && offset >= pos) {
                     beforeAnyClass = false;
                 }
             }
         }
         if (beforeAnyClass) {
             Tree firstImport = null;
             for (Tree t : root.getImports()) {
                 firstImport = t;
                 break;
             }
             Tree pd = root.getPackageName();
             if ((pd != null && offset <= sourcePositions.getStartPosition(root, root)) || (pd == null && (firstImport == null || sourcePositions.getStartPosition(root, firstImport) >= offset))) {
                 kws.add(PACKAGE_KEYWORD);
             }
         }
         for (String kw : kws) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, SPACE, query.anchorOffset, false));
             }
         }
         addKeywordsForStatement();
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
         for (String kw : STATEMENT_KEYWORDS) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, null, query.anchorOffset, false));
             }
         }
         for (String kw : STATEMENT_SPACE_KEYWORDS) {
             if (JavaFXCompletionProvider.startsWith(kw, prefix)) {
                 addResult(JavaFXCompletionItem.createKeywordItem(kw, SPACE, query.anchorOffset, false));
             }
         }
         if (JavaFXCompletionProvider.startsWith(RETURN_KEYWORD, prefix)) {
             JavaFXTreePath mth = JavaFXCompletionProvider.getPathElementOfKind(Tree.JavaFXKind.FUNCTION_DEFINITION, getPath());
             String postfix = SPACE;
             if (mth != null) {
                  // XXX[pn]: is this right?
                 Tree rt = ((FunctionDefinitionTree) mth.getLeaf()).getFunctionValue().getType();
                 if (rt == null) {
                     postfix = SEMI;
                 }
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
 
     protected void addValueKeywords() throws IOException {
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
         } else {
             if (!modifiers.contains(PUBLIC) && !modifiers.contains(PROTECTED) && !modifiers.contains(PRIVATE)) {
                 kws.add(PUBLIC_KEYWORD);
                 kws.add(PROTECTED_KEYWORD);
                 kws.add(PRIVATE_KEYWORD);
             }
             if (!modifiers.contains(FINAL) && !modifiers.contains(ABSTRACT)) {
                 kws.add(ABSTRACT_KEYWORD);
             }
             if (!modifiers.contains(STATIC)) {
                 kws.add(STATIC_KEYWORD);
             }
             kws.add(READONLY_KEYWORD);
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
             if ((e.sym != null) && (!e.sym.toString().contains("$"))){
                 try {
                     e.sym.complete();
                 } catch (RuntimeException x) {
                     if (LOGGABLE) {
                         logger.log(Level.FINE,"Let's see whether we survive this: ",x);
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
         JavafxcTrees trees = controller.getTrees();
         JavaFXTreePath p = new JavaFXTreePath(root);
         JavafxcScope scope = trees.getScope(p);
         for (Element e : getEnclosedElements(pe)) {
             if (e.getKind().isClass() || e.getKind() == ElementKind.INTERFACE) {
                 String name = e.getSimpleName().toString();
                 if (!trees.isAccessible(scope, (TypeElement) e)) {
                     if (LOGGABLE) log("    not accessible " + name);
                     continue;
                 }
                 if (JavaFXCompletionProvider.startsWith(name, prefix) &&
                         !name.contains("$")) {
                     addResult(JavaFXCompletionItem.createTypeItem((TypeElement) e, (DeclaredType) e.asType(), offset, elements.isDeprecated(e), insideNew, false));
                 }
                 for (Element ee : e.getEnclosedElements()) {
                     if (ee.getKind().isClass() || ee.getKind() == ElementKind.INTERFACE) {
                         String ename = ee.getSimpleName().toString();
                         if (!trees.isAccessible(scope, (TypeElement) ee)) {
                             if (LOGGABLE) log("    not accessible " + ename);
                             continue;
                         }
                         log(ename + " isJFXClass " + types.isJFXClass((Symbol) ee));
                         if (JavaFXCompletionProvider.startsWith(ename, prefix) &&
                                 types.isJFXClass((Symbol) ee)) {
                             addResult(JavaFXCompletionItem.createTypeItem((TypeElement) ee, (DeclaredType) ee.asType(), offset, elements.isDeprecated(ee), insideNew, false));
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
         JavafxcTrees trees = controller.getTrees();
         JavaFXTreePath p = new JavaFXTreePath(root);
         JavafxcScope scope = trees.getScope(p);
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
                             addResult(JavaFXCompletionItem.createTypeItem(name1, offset, false, false, false));
                         } else {
                             DeclaredType dt = (DeclaredType) local.asType();
                             addResult(JavaFXCompletionItem.createTypeItem(te, dt, offset, false, false, false));
                         }
                         return;
                     }
                 }
             }
         }
     }
     
     protected void addLocalAndImportedTypes(final EnumSet<ElementKind> kinds, final DeclaredType baseType, final Set<? extends Element> toExclude, boolean insideNew, TypeMirror smart) throws IOException {
         if (LOGGABLE) log("addLocalAndImportedTypes");
         JavafxcTrees trees = controller.getTrees();
         JavaFXTreePath p = new JavaFXTreePath(root);
         JavafxcScope scope = trees.getScope(p);
         JavafxcScope originalScope = scope;
         while (scope != null) {
             if (LOGGABLE) log("  scope == " + scope);
             addLocalAndImportedTypes(scope.getLocalElements(), kinds, baseType, toExclude, insideNew, smart, originalScope, null,false);
             scope = scope.getEnclosingScope();
         }
         Element e = trees.getElement(p);
         while (e != null && e.getKind() != ElementKind.PACKAGE) {
             e = e.getEnclosingElement();
         }
         if (e != null) {
             if (LOGGABLE) log("will scan package " + e.getSimpleName());
             PackageElement pkge = (PackageElement)e;
             addLocalAndImportedTypes(getEnclosedElements(pkge), kinds, baseType, toExclude, insideNew, smart, originalScope, pkge,false);
         }
         addPackages("");
     }
     
     private void addLocalAndImportedTypes(Iterable<? extends Element> from,
             final EnumSet<ElementKind> kinds, final DeclaredType baseType, 
             final Set<? extends Element> toExclude,
             boolean insideNew, TypeMirror smart, JavafxcScope originalScope,
             PackageElement myPackage,boolean simpleNameOnly) throws IOException {
         final Elements elements = controller.getElements();
         JavafxcTrees trees = controller.getTrees();
         for (Element local : from) {
             if (LOGGABLE) log("    local == " + local);
             if (local.getKind().isClass() || local.getKind() == ElementKind.INTERFACE) {
                 if (local.asType() == null || local.asType().getKind() != TypeKind.DECLARED) {
                     continue;
                 }
                 DeclaredType dt = (DeclaredType) local.asType();
                 TypeElement te = (TypeElement) local;
                 String name = local.getSimpleName().toString();
                 if (!trees.isAccessible(originalScope, te)) {
                     if (LOGGABLE) log("    not accessible " + name);
                     continue;
                 }
                 Element parent = te.getEnclosingElement();
                 if (parent.getKind() == ElementKind.CLASS) {
                     if (!trees.isAccessible(originalScope, (TypeElement) parent)) {
                         if (LOGGABLE) log("    parent not accessible " + name);
                         continue;
                     }
                 }
                 if (smart != null && local.asType() == smart) {
                     addResult(JavaFXCompletionItem.createTypeItem(te, dt, offset, elements.isDeprecated(local), insideNew, true));
                 }
                 if (JavaFXCompletionProvider.startsWith(name, prefix) && !name.contains("$")) {
                     if (simpleNameOnly) {
                         addResult(JavaFXCompletionItem.createTypeItem(local.getSimpleName().toString(), offset, elements.isDeprecated(local), insideNew, false));
                     } else {
                         addResult(JavaFXCompletionItem.createTypeItem(te, dt, offset, elements.isDeprecated(local), insideNew, false));
                     }
                 }
                 if (parent == myPackage) {
                    if (LOGGABLE) log("   will check inner classes of: " + local);
                     addLocalAndImportedTypes(local.getEnclosedElements(), kinds, baseType, toExclude, insideNew, smart, originalScope, null,true);
                 }
             }
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
         JavafxcScope scope = trees.getScope(p);
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
     
     private void addAllTypes(EnumSet<ElementKind> kinds, boolean insideNew) {
         if (LOGGABLE) log("NOT IMPLEMENTED addAllTypes ");
 //            for(ElementHandle<TypeElement> name : controller.getJavaSource().getClasspathInfo().getClassIndex().getDeclaredTypes(prefix != null ? prefix : EMPTY, kind, EnumSet.allOf(ClassIndex.SearchScope.class))) {
 //                LazyTypeCompletionItem item = LazyTypeCompletionItem.create(name, kinds, anchorOffset, controller.getJavaSource(), insideNew);
 //                if (item.isAnnonInner())
 //                    continue;
 //                results.add(item);
 //            }
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
     
     /**
      * We don't have an AST - let's try some heuristics
      */
     void useCrystalBall() {
         try {
             int lineStart = IndentUtils.lineStartOffset(controller.getJavaFXSource().getDocument(), offset);
             if (LOGGABLE) log("useCrystalBall lineStart " + lineStart + " offset " + offset);
             TokenSequence<JFXTokenId> ts = findFirstNonWhitespaceToken(lineStart, offset);
             if (ts == null) {
                 // nothing interesting on this line? let's try to delete it:
                 tryToDeleteCurrentLine(lineStart);
                 if (query.results.isEmpty()) {
                     tryToTypeSomeNonsense(lineStart);
                 }
                 return;
             }
             if (LOGGABLE) log("  first == " + ts.token().id() + " at " + ts.offset());
             if (ts.token().id() == JFXTokenId.IMPORT) {
                 int count = ts.offset();
                 char[] chars = new char[count];
                 while (count>0) chars[--count] = ' ';
                 String helper = new String(chars);
                 String next = ts.token().text().toString();
                 while (ts.offset() < offset && ts.moveNext()) {
                     if (LOGGABLE) log("Watching : " + ts.token().id() + " " + ts.token().text().toString());
                     helper += next;
                     next = ts.token().text().toString();
                     if (LOGGABLE) log("helper == " + helper);
                 }
                 helper += "*;";
                 if (LOGGABLE) log("Helper prepared: " + helper);
                 if (!helper.endsWith("import *;")) {
                     useFakeSource(helper, helper.length()-2);
                 } else {
                     addPackages("");
                 }
             } else {
                 tryToTypeSomeNonsense(lineStart);
             }
         } catch (BadLocationException ex) {
             if (LOGGABLE) {
                 logger.log(Level.FINE,"Crystal ball failed: ",ex);
             }
         }
     }
 
     private void tryToTypeSomeNonsense(int lineStart) {
         String text = controller.getText();
         StringBuilder builder = new StringBuilder(text);
         builder.insert(offset, 'x');
         useFakeSource(builder.toString(), offset);
         if (query.results.isEmpty()) {
             builder = new StringBuilder(text);
             builder.insert(offset, "x;");
             useFakeSource(builder.toString(), offset);
         }
         if (query.results.isEmpty()) {
             builder = new StringBuilder(text);
             builder.insert(offset, "x]");
             useFakeSource(builder.toString(), offset);
         }
         if (query.results.isEmpty()) {
             builder = new StringBuilder(text);
             builder.insert(offset, "x];");
             useFakeSource(builder.toString(), offset);
         }
         if (query.results.isEmpty()) {
             // still nothing? let's be desperate:
             String currentLine = controller.getText().substring(lineStart, offset);
             if ((!currentLine.contains(":"))  && 
                 (!currentLine.contains("(")) &&
                 (!currentLine.contains(".")) &&
                 (!currentLine.contains("{"))
             ) {
                 tryToDeleteCurrentLine(lineStart);
             }
         }
     }
     
     /**
      * 
      * @param lineStart
      */
     private void tryToDeleteCurrentLine(int lineStart) {
         if (LOGGABLE) log("tryToDeleteCurrentLine lineStart == " + lineStart + " offset == " + offset);
         if (offset < lineStart) {
             if (LOGGABLE) log("   no line, sorry");
             return;
         }
         int pLength = (prefix != null ? prefix.length():0);
         int count = offset - lineStart + pLength;
         char[] chars = new char[count];
         while (count>0) chars[--count] = ' ';
         String text = controller.getText();
         StringBuilder builder = new StringBuilder(text);
         builder.replace(lineStart, offset + pLength, new String(chars));
         useFakeSource(builder.toString(), offset);
     }
     
     /**
      * 
      * @param source
      */
     protected void useFakeSource(String source, final int pos) {
         if (LOGGABLE) log("useFakeSource " + source + " pos == " + pos);
         if (usingFakeSource > 1) {
             // allow to recurse only twice ;-)
             return;
         }
         try {
             usingFakeSource++;
             FileSystem fs = FileUtil.createMemoryFileSystem();
             final FileObject fo = fs.getRoot().createData("tmp" + (new Random().nextLong()) + ".fx");
             Writer w = new OutputStreamWriter(fo.getOutputStream());
             w.write(source);
             w.close();
             if (LOGGABLE) log("  source written to " + fo);
             ClasspathInfo info = ClasspathInfo.create(controller.getFileObject());
             JavaFXSource s = JavaFXSource.create(info,Collections.singleton(fo));
             if (LOGGABLE) log("  jfxsource obtained " + s);
             s.runWhenScanFinished(new Task<CompilationController>() {
                 public void run(CompilationController fakeController) throws Exception {
                     if (LOGGABLE) log("    scan finished");
                     JavaFXCompletionEnvironment env = query.getCompletionEnvironment(fakeController, pos);
                     if (LOGGABLE) log("    env == " + env);
                     fakeController.toPhase(Phase.ANALYZED);
                     if (LOGGABLE) log("    fake analyzed");
                     if (! env.isTreeBroken()) {
                         if (LOGGABLE) log("    fake non-broken tree");
                         final Tree leaf = env.getPath().getLeaf();
                         env.inside(leaf);
                         // try to remove faked entries:
                         String fakeName = fo.getName();
                         Set<JavaFXCompletionItem> toRemove = new TreeSet<JavaFXCompletionItem>();
                         for (JavaFXCompletionItem r : query.results) {
                             if (LOGGABLE) log("    checking " + r.getLeftHtmlText());
                             if (r.getLeftHtmlText().contains(fakeName)) {
                                 if (LOGGABLE) log("    will remove " + r);
                                 toRemove.add(r);
                             }
                         }
                         query.results.removeAll(toRemove);
                     } 
                 }
             },true);
         } catch (IOException ex) {
             if (LOGGABLE) {
                 logger.log(Level.FINE,"useFakeSource failed: ",ex);
             }
         } finally {
             usingFakeSource--;
         }
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
 
     protected static Tree unwrapErrTree(Tree tree) {
         if (tree != null && tree.getJavaFXKind() == Tree.JavaFXKind.ERRONEOUS) {
             Iterator<? extends Tree> it = ((ErroneousTree) tree).getErrorTrees().iterator();
             tree = it.hasNext() ? it.next() : null;
         }
         return tree;
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
 }
