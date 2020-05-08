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
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 package org.netbeans.modules.javafx.editor.semantic;
 
 import com.sun.javafx.api.tree.*;
 import com.sun.javafx.api.tree.Tree.JavaFXKind;
 import com.sun.tools.mjavac.code.Symbol;
 import com.sun.tools.javafx.code.JavafxFlags;
 import com.sun.tools.javafx.tree.JFXClassDeclaration;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.TreeUtilities;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 
 import javax.lang.model.element.Name;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.Modifier;
 import org.netbeans.api.javafx.editor.Cancellable;
 import org.netbeans.api.javafx.editor.SafeTokenSequence;
 
 /**
  *
  * @author Jan Lahoda
  */
 public class Utilities {
     
     private static final Logger LOG = Logger.getLogger(Utilities.class.getName());
     
     @Deprecated
     private static final boolean DEBUG = false;
     
     /** Creates a new instance of Utilities */
     public Utilities() {
     }
     
     private static Token<JFXTokenId> findTokenWithText(CompilationInfo info, String text, int start, int end) {
         TokenHierarchy<?> th = info.getTokenHierarchy();
         TokenSequence<JFXTokenId> ts_ = th.tokenSequence(JFXTokenId.language()).subSequence(start, end);
         SafeTokenSequence<JFXTokenId> ts = new SafeTokenSequence<JFXTokenId>(ts_, info.getDocument(), Cancellable.Dummy.getInstance());
         
         while (ts.moveNext()) {
             Token<JFXTokenId> t = ts.token();
             
             if (text.equals(t.text().toString())) {
                 return t;
             }
         }
         
         return null;
     }
     
     private static Tree normalizeLastLeftTree(Tree lastLeft) {
 /*        while (lastLeft != null && lastLeft.getJavaFXKind() == JavaFXKind.ARRAY_TYPE) {
             lastLeft = ((ArrayTypeTree) lastLeft).getType();
         }
   */      
         return lastLeft;
     }
     
     private static Token<JFXTokenId> findIdentifierSpanImpl(CompilationInfo info, Tree decl, Tree lastLeft, List<? extends Tree> firstRight, String name, UnitTree cu, SourcePositions positions) {
         int declStart = (int) positions.getStartPosition(cu, decl);
         
         lastLeft = normalizeLastLeftTree(lastLeft);
         
         int start = lastLeft != null ? (int)positions.getEndPosition(cu, lastLeft) : declStart;
         
         if (start == (-1)) {
             start = declStart;
             if (start == (-1)) {
                 return null;
             }
         }
         
         int end = (int)positions.getEndPosition(cu, decl);
 
         for (Tree t : firstRight) {
             if (t == null)
                 continue;
 
             int proposedEnd = (int)positions.getStartPosition(cu, t);
 
             if (proposedEnd != (-1) && proposedEnd < end)
                 end = proposedEnd;
         }
 
         if (end == (-1)) {
             return null;
         }
 
         if (start > end) {
             //may happend in case:
             //public static String s() [] {}
             //(meaning: method returning array of Strings)
             //use a conservative start value:
             start = (int) positions.getStartPosition(cu, decl);
         }
 
         return findTokenWithText(info, name, start, end);
     }
     
     private static Token<JFXTokenId> findIdentifierSpanImpl(CompilationInfo info, MemberSelectTree tree, UnitTree cu, SourcePositions positions) {
         int start = (int)positions.getStartPosition(cu, tree);
         int endPosition = (int)positions.getEndPosition(cu, tree);
         
         if (start == (-1) || endPosition == (-1))
             return null;
 
         String member = tree.getIdentifier().toString();
 
         TokenHierarchy<?> th = info.getTokenHierarchy();
         TokenSequence<JFXTokenId> ts_ = th.tokenSequence(JFXTokenId.language());
         SafeTokenSequence<JFXTokenId> ts = new SafeTokenSequence<JFXTokenId>(ts_, info.getDocument(), Cancellable.Dummy.getInstance());
 
         if (ts.move(endPosition) == Integer.MAX_VALUE) {
             return null;
         }
 
         if (ts.moveNext()) {
             while (ts.offset() >= start) {
                 Token<JFXTokenId> t = ts.token();
                 if (member.equals(t.text().toString())) {
                     return t;
                 }
                 if (!ts.movePrevious()) {
                     break;
                 }
             }
         }
         return null;
     }
     
     private static final Map<Class, List<JavaFXKind>> class2Kind;
     
     static {
         class2Kind = new HashMap<Class, List<JavaFXKind>>();
         
         for (JavaFXKind k : JavaFXKind.values()) {
             Class c = k.asInterface();
             List<JavaFXKind> kinds = class2Kind.get(c);
             
             if (kinds == null) {
                 class2Kind.put(c, kinds = new ArrayList<JavaFXKind>());
             }
             
             kinds.add(k);
         }
     }
     
     private static Token<JFXTokenId> findIdentifierSpanImpl(CompilationInfo info, JavaFXTreePath decl) {
         TreeUtilities tu = TreeUtilities.create(info);
         if (tu.isSynthetic(decl))
             return null;
         
         Tree leaf = decl.getLeaf();
                 
         if (leaf instanceof JFXFunctionDefinition) {
             // XXX: should use FunctionDefinitionTree, but it lacks the API
             JFXFunctionDefinition function = (JFXFunctionDefinition) leaf;
             List<Tree> rightTrees = new ArrayList<Tree>();
 
             rightTrees.addAll(function.getParams());
 //            rightTrees.addAll(function.getThrows());
             rightTrees.add(function.getBodyExpression());
 
             Name name = function.getName();
 
             if (function.getJFXReturnType() == null)
                 name = ((ClassDeclarationTree) decl.getParentPath().getLeaf()).getSimpleName();
 
             return findIdentifierSpanImpl(info, leaf, function.getJFXReturnType(), rightTrees, name.toString(), info.getCompilationUnit(), info.getTrees().getSourcePositions());
         }
         
         if (class2Kind.get(VariableTree.class).contains(leaf.getJavaFXKind())) {
             VariableTree var = (VariableTree) leaf;
             List<Tree> rightTrees = new ArrayList<Tree>();
 
             rightTrees.add(var.getInitializer());
             rightTrees.add(var.getType());
 
 //            return findIdentifierSpanImpl(info, leaf, var.getType(), Collections.singletonList(var.getInitializer()), var.getName().toString(), info.getCompilationUnit(), info.getTrees().getSourcePositions());
             if (var.getName() == null) { // Handle empty try-catch
                 return null;
             }
             return findIdentifierSpanImpl(info, leaf, var.getModifiers(), rightTrees, var.getName().toString(), info.getCompilationUnit(), info.getTrees().getSourcePositions());
         }
         
         if (class2Kind.get(MemberSelectTree.class).contains(leaf.getJavaFXKind())) {
             return findIdentifierSpanImpl(info, (MemberSelectTree) leaf, info.getCompilationUnit(), info.getTrees().getSourcePositions());
         }
         
         if (class2Kind.get(ClassDeclarationTree.class).contains(leaf.getJavaFXKind())) {
             String name = ((ClassDeclarationTree) leaf).getSimpleName().toString();
             
             if (name.length() == 0)
                 return null;
             
             SourcePositions positions = info.getTrees().getSourcePositions();
             UnitTree cu = info.getCompilationUnit();
             int start = (int)positions.getStartPosition(cu, leaf);
             int end   = (int)positions.getEndPosition(cu, leaf);
             
             if (start == (-1) || end == (-1)) {
                 return null;
             }
             
             return findTokenWithText(info, name, start, end);
         }
         
         
         if (JavaFXKind.CLASS_DECLARATION == leaf.getJavaFXKind()) {
             String name = ((JFXClassDeclaration) leaf).getName().toString();
             
             if (name.length() == 0)
                 return null;
             
             SourcePositions positions = info.getTrees().getSourcePositions();
             UnitTree cu = info.getCompilationUnit();
             int start = (int)positions.getStartPosition(cu, leaf);
             int end   = (int)positions.getEndPosition(cu, leaf);
             
             if (start == (-1) || end == (-1)) {
                 return null;
             }
             
             return findTokenWithText(info, name, start, end);
         }
         
         throw new IllegalArgumentException("Only MethodDecl, VariableDecl and ClassDecl are accepted by this method."); // NOI18N
     }
 
     public static int[] findIdentifierSpan( final JavaFXTreePath decl, final CompilationInfo info, final Document doc) {
         final int[] result = new int[] {-1, -1};
         doc.render(new Runnable() {
             public void run() {
                 Token<JFXTokenId> t = findIdentifierSpan(info, doc, decl);
                 if (t != null) {
                     result[0] = t.offset(null);
                     result[1] = t.offset(null) + t.length();
                 }
             }
         });
         
         return result;
     }
     
     public static Token<JFXTokenId> findIdentifierSpan(final CompilationInfo info, final Document doc, final JavaFXTreePath decl) {
         @SuppressWarnings("unchecked")
         final Token<JFXTokenId>[] result = new Token[1];
         doc.render(new Runnable() {
             public void run() {
                 result[0] = findIdentifierSpanImpl(info, decl);
             }
         });
         
         return result[0];
     }
     
     private static int findBodyStartImpl(Tree cltree, UnitTree cu, SourcePositions positions, Document doc) {
         int start = (int)positions.getStartPosition(cu, cltree);
         int end   = (int)positions.getEndPosition(cu, cltree);
         
         if (start == (-1) || end == (-1)) {
             return -1;
         }
         
         if (start > doc.getLength() || end > doc.getLength()) {
             if (DEBUG) {
                 System.err.println("Log: position outside document: "); // NOI18N
                 System.err.println("decl = " + cltree); // NOI18N
                 System.err.println("startOffset = " + start); // NOI18N
                 System.err.println("endOffset = " + end); // NOI18N
                 Thread.dumpStack();
             }
             
             return (-1);
         }
         
         try {
             String text = doc.getText(start, end - start);
             
             int index = text.indexOf('{');
             
             if (index == (-1)) {
                 return -1;
 //                throw new IllegalStateException("Should NEVER happen.");
             }
             
             return start + index;
         } catch (BadLocationException e) {
             LOG.log(Level.INFO, null, e);
         }
         
         return (-1);
     }
     
     public static int findBodyStart(final Tree cltree, final UnitTree cu, final SourcePositions positions, final Document doc) {
         JavaFXKind kind = cltree.getJavaFXKind();
         if (kind != JavaFXKind.CLASS_DECLARATION && kind != JavaFXKind.FUNCTION_DEFINITION)
             throw new IllegalArgumentException("Unsupported kind: "+ kind); // NOI18N
         final int[] result = new int[1];
         
         doc.render(new Runnable() {
             public void run() {
                 result[0] = findBodyStartImpl(cltree, cu, positions, doc);
             }
         });
         
         return result[0];
     }
     
     private static int findLastBracketImpl(FunctionDefinitionTree tree, UnitTree cu, SourcePositions positions, Document doc) {
         int start = (int)positions.getStartPosition(cu, tree);
         int end   = (int)positions.getEndPosition(cu, tree);
         
         if (start == (-1) || end == (-1)) {
             return -1;
         }
         
         if (start > doc.getLength() || end > doc.getLength()) {
             if (DEBUG) {
                 System.err.println("Log: position outside document: "); // NOI18N
                 System.err.println("decl = " + tree); // NOI18N
                 System.err.println("startOffset = " + start); // NOI18N
                 System.err.println("endOffset = " + end); // NOI18N
                 Thread.dumpStack();
             }
             
             return (-1);
         }
         
         try {
             String text = doc.getText(end - 1, 1);
             
             if (text.charAt(0) == '}')
                 return end - 1;
         } catch (BadLocationException e) {
             LOG.log(Level.INFO, null, e);
         }
         
         return (-1);
     }
     
     public static int findLastBracket(final FunctionDefinitionTree tree, final UnitTree cu, final SourcePositions positions, final Document doc) {
         final int[] result = new int[1];
         
         doc.render(new Runnable() {
             public void run() {
                 result[0] = findLastBracketImpl(tree, cu, positions, doc);
             }
         });
         
         return result[0];
     }
     
     private static Token<JFXTokenId> createHighlightImpl(CompilationInfo info, Document doc, JavaFXTreePath tree) {
         Tree leaf = tree.getLeaf();
 //        SourcePositions positions = info.getTrees().getSourcePositions();
 //        CompilationUnitTree cu = info.getCompilationUnit();
         
         //XXX: do not use instanceof:
         if (leaf instanceof VariableTree || 
 /*                leaf instanceof ClassTree || */
                 leaf instanceof JFXClassDeclaration || 
                 leaf instanceof JFXFunctionDefinition || 
                 leaf instanceof MemberSelectTree) {
 //        Element element = info.getTrees().getElement(tree);
 //        ElementKind kind = element.getKind();
 //        if (kind == ElementKind.METHOD || kind == ElementKind.FIELD || kind == ElementKind.LOCAL_VARIABLE || kind == ElementKind.PARAMETER || kind == ElementKind.CLASS) {
             return findIdentifierSpan(info, doc, tree);
         }
         
         int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), leaf);
         
         TokenHierarchy<?> th = info.getTokenHierarchy();
         TokenSequence<JFXTokenId> ts_ = th.tokenSequence(JFXTokenId.language());
         SafeTokenSequence<JFXTokenId> ts = new SafeTokenSequence<JFXTokenId>(ts_, info.getDocument(), Cancellable.Dummy.getInstance());
         
         if (ts.move(start) == Integer.MAX_VALUE) {
             return null;
         }
         
         ts.moveNext();
         
         if (ts.offset() == start && ts.token().id() == JFXTokenId.IDENTIFIER) {
              return ts.token();
         }
         
         return null;
     }
     
     public static Token<JFXTokenId> getToken(final CompilationInfo info, final Document doc, final JavaFXTreePath tree) {
         @SuppressWarnings("unchecked")
         final Token<JFXTokenId>[] result = new Token[1];
         
         doc.render(new Runnable() {
             public void run() {
                 result[0] = createHighlightImpl(info, doc, tree);
             }
         });
         
         return result[0];
     }
     
     private static final Set<String> keywords;
     
     static { // TODO - FX keywords
         keywords = new HashSet<String>();
         
         keywords.add("true"); // NOI18N
         keywords.add("false"); // NOI18N
         keywords.add("null"); // NOI18N
         keywords.add("this"); // NOI18N
         keywords.add("super"); // NOI18N
         keywords.add("class"); // NOI18N
     }
     
     public static boolean isKeyword(Tree tree) {
         if (tree.getJavaFXKind() == JavaFXKind.IDENTIFIER) {
            final Name name = ((IdentifierTree) tree).getName();
            if (null != name) {
                return keywords.contains(name.toString());
            }
         }
         if (tree.getJavaFXKind() == JavaFXKind.MEMBER_SELECT) {
            final Name identifier = ((MemberSelectTree) tree).getIdentifier();
            if (null != identifier) {
                return keywords.contains(identifier.toString());
            }
         }
         
         return false;
     }
     
     public static boolean isPrivateElement(Element el) {
         if (el.getKind() == ElementKind.PARAMETER)
             return true;
         
         if (el.getKind() == ElementKind.LOCAL_VARIABLE)
             return true;
         
         if (el.getKind() == ElementKind.EXCEPTION_PARAMETER)
             return true;
         
         Symbol sy = (Symbol)el;
         return el.getModifiers().contains(Modifier.PRIVATE) || (sy.flags() & JavafxFlags.SCRIPT_PRIVATE) > 0;
     }
     
 
 }
