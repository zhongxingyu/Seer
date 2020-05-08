 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 package org.netbeans.modules.javafx.editor.format;
 
 import com.sun.javafx.api.tree.*;
 import com.sun.javafx.api.tree.Tree.JavaFXKind;
 import com.sun.tools.javafx.tree.JFXBlock;
 import com.sun.tools.javafx.tree.JFXExpression;
 import com.sun.tools.javafx.tree.JFXFunctionDefinition;
 import com.sun.tools.javafx.tree.JFXIfExpression;
 import com.sun.tools.javafx.tree.JFXTree;
 import com.sun.tools.javafx.tree.JFXVar;
 import java.util.EnumSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import javax.lang.model.element.Name;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import org.netbeans.api.java.source.CodeStyle;
 import org.netbeans.api.javafx.editor.FXSourceUtils;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.editor.indent.spi.Context;
 import org.netbeans.modules.editor.indent.spi.ExtraLock;
 import org.netbeans.modules.editor.indent.spi.ReformatTask;
 
 
 /**
  * This code based on org.netbeans.modules.java.source.save.Reformatter written by Dusan Balek.
  *
  * http://openjfx.java.sun.com/current-build/doc/reference/JavaFXReference.html
  *
  * @author Anton Chechel
  */
 public class JFXReformatTask implements ReformatTask {
 
     private static final Object CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N
 
     private final Context context;
     private CompilationController controller;
     private Document doc;
     private int shift;
 
     public JFXReformatTask(Context context) {
         this.context = context;
         this.doc = context.document();
     }
 
     public void reformat() throws BadLocationException {
         if (controller == null) {
             try {
                 final JavaFXSource source = JavaFXSource.forDocument(context.document());
                 source.runUserActionTask(new Task<CompilationController>() {
                     public void run(CompilationController controller) throws Exception {
                         JFXReformatTask.this.controller = controller;
                     }
                 }, true);
                 if (controller == null) {
                     return;
                 }
                 if (controller.toPhase(JavaFXSource.Phase.PARSED).lessThan(JavaFXSource.Phase.PARSED)) {
                     return;
                 }
             } catch (Exception ex) {
                 controller = null;
                 return;
             }
         }
         CodeStyle cs = CodeStyle.getDefault(doc);
         for (Context.Region region : context.indentRegions()) {
             reformatImpl(region, cs);
         }
     }
 
     private void reformatImpl(Context.Region region, CodeStyle cs) throws BadLocationException {
         boolean templateEdit = doc.getProperty(CT_HANDLER_DOC_PROPERTY) != null;
         int startOffset = region.getStartOffset() - shift;
         int endOffset = region.getEndOffset() - shift;
         int originalEndOffset = endOffset;
         startOffset = controller.getSnapshot().getEmbeddedOffset(startOffset);
         if (startOffset < 0) {
             return;
         }
         endOffset = controller.getSnapshot().getEmbeddedOffset(endOffset);
         if (endOffset < 0) {
             return;
         }
         if (startOffset >= endOffset) {
             return;
         }
         JavaFXTreePath path = getCommonPath(startOffset);
         if (path == null) {
             return;
         }
 
         for (Diff diff : Pretty.reformat(controller, path, cs, startOffset, endOffset, templateEdit)) {
             int start = diff.getStartOffset();
             int end = diff.getEndOffset();
             String text = diff.getText();
             if (startOffset > end) {
                 continue;
             }
             if (endOffset < start) {
                 continue;
             }
             if (endOffset == start && (text == null || !text.trim().equals("}"))) { //NOI18N
                 continue;
             }
 
             if (startOffset >= start) {
                 if (text != null && text.length() > 0) {
                     TokenSequence<JFXTokenId> ts = controller.getTokenHierarchy().tokenSequence(JFXTokenId.language());
                     if (ts == null) {
                         continue;
                     }
                     if (ts.move(startOffset) == 0) {
                         if (!ts.movePrevious() && !ts.moveNext()) {
                             continue;
                         }
                     } else {
                         if (!ts.moveNext() && !ts.movePrevious()) {
                             continue;
                         }
                     }
                     if (ts.token().id() == JFXTokenId.WS) {
                         // JavaFX diffenerce
                         int tsOffset = ts.offset();
                         StringBuilder t1 = new StringBuilder();
                         do {
                             t1.append(ts.token().text().toString());
                         } while (ts.moveNext() && ts.token().id() == JFXTokenId.WS);
 
                         String t = t1.toString();
 //                        String t = ts.token().text().toString();
                         t = t.substring(0, startOffset - tsOffset);
                         if (templateEdit) {
                             int idx = t.lastIndexOf('\n'); //NOI18N
                             if (idx >= 0) {
                                 t = t.substring(idx + 1);
                                 idx = text.lastIndexOf('\n'); //NOI18N
                                 if (idx >= 0) {
                                     text = text.substring(idx + 1);
                                 }
                                 if (text.trim().length() > 0) {
                                     text = null;
                                 } else if (text.length() > t.length()) {
                                     text = text.substring(t.length());
                                 } else {
                                     text = null;
                                 }
                             } else {
                                 text = null;
                             }
                         } else {
                             int idx1 = 0;
                             int idx2 = 0;
                             int lastIdx1 = 0;
                             int lastIdx2 = 0;
                             while ((idx1 = t.indexOf('\n', lastIdx1)) >= 0 && (idx2 = text.indexOf('\n', lastIdx2)) >= 0) { //NOI18N
                                 lastIdx1 = idx1 + 1;
                                 lastIdx2 = idx2 + 1;
                             }
                             if ((idx2 = text.lastIndexOf('\n')) >= 0 && idx2 >= lastIdx2) { //NOI18N
                                 if (lastIdx1 == 0) {
                                     t = null;
                                 } else {
                                     text = text.substring(idx2 + 1);
                                     t = t.substring(lastIdx1);
                                 }
                             } else if ((idx1 = t.lastIndexOf('\n')) >= 0 && idx1 >= lastIdx1) { //NOI18N
                                 t = t.substring(idx1 + 1);
                                 text = text.substring(lastIdx2);
                             } else {
                                 t = t.substring(lastIdx1);
                                 text = text.substring(lastIdx2);
                             }
                             if (text != null && t != null) {
                                 text = text.length() > t.length() ? text.substring(t.length()) : null;
                             }
                         }
                     } else if (templateEdit) {
                         text = null;
                     }
                 }
                 start = startOffset;
             }
             if (endOffset < end) {
                 if (text != null && text.length() > 0 && !templateEdit) {
                     TokenSequence<JFXTokenId> ts = controller.getTokenHierarchy().tokenSequence(JFXTokenId.language());
                     if (ts != null) {
                         ts.move(endOffset);
                         if (ts.moveNext() && ts.token().id() == JFXTokenId.WS) {
                             // JavaFX diffenerce
                             int tsOffset = ts.offset();
                             StringBuilder t1 = new StringBuilder();
                             do {
                                 t1.append(ts.token().text().toString());
                             } while (ts.moveNext() && ts.token().id() == JFXTokenId.WS);
 
                             String t = t1.toString();
 //                            String t = ts.token().text().toString();
                             t = t.substring(endOffset - tsOffset);
                             int idx1, idx2;
                             while ((idx1 = t.lastIndexOf('\n')) >= 0 && (idx2 = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                 t = t.substring(0, idx1);
                                 text = text.substring(0, idx2);
                             }
                             text = text.length() > t.length() ? text.substring(0, text.length() - t.length()) : null;
                         }
                     }
                 }
                 end = endOffset;
             }
             start = controller.getSnapshot().getOriginalOffset(start);
             end = controller.getSnapshot().getOriginalOffset(end);
             start += shift;
             end += shift;
             doc.remove(start, end - start);
             if (text != null && text.length() > 0) {
                 doc.insertString(start, text, null);
             }
         }
         shift = region.getEndOffset() - originalEndOffset;
         return;
     }
 
     public ExtraLock reformatLock() {
         return null;
     }
 
     private JavaFXTreePath getCommonPath(final int offset) {
         JavaFXTreePath path = controller.getTreeUtilities().pathFor(offset);
         if (offset > 0) {
             if (path.getLeaf() instanceof FunctionValueTree) {
                 path = path.getParentPath();
             }
         } else {
             while (path.getParentPath() != null) {
                 path = path.getParentPath();
             }
         }
         return path;
     }
 
     private static class Pretty extends JavaFXTreePathScanner<Boolean, Void> {
 
         private static final String OPERATOR = "operator"; //NOI18N
         private static final String EMPTY = ""; //NOI18N
         private static final String SPACE = " "; //NOI18N
         private static final String NEWLINE = "\n"; //NOI18N
         private static final String ERROR = "<error>"; //NOI18N
         private static final int ANY_COUNT = -1;
 
         private final String fText;
         private final SourcePositions sp;
         private final CodeStyle cs;
 
         private final int rightMargin;
         private final int tabSize;
         private final int indentSize;
         private final int continuationIndentSize;
         private final boolean expandTabToSpaces;
 
         private TokenSequence<JFXTokenId> tokens;
         private int indent;
         private int col;
         private int endPos;
         private int wrapDepth;
         private int lastBlankLines;
         private int lastBlankLinesTokenIndex;
         private Diff lastBlankLinesDiff;
         private boolean templateEdit;
         private LinkedList<Diff> diffs = new LinkedList<Diff>();
         private DanglingElseChecker danglingElseChecker = new DanglingElseChecker();
         private UnitTree root;
         private int startOffset;
         private int endOffset;
 
         private Pretty(CompilationInfo info, JavaFXTreePath path, CodeStyle cs, int startOffset, int endOffset, boolean templateEdit) {
             this(FXSourceUtils.getText(info), info.getTokenHierarchy().tokenSequence(JFXTokenId.language()),
                     path, info.getTrees().getSourcePositions(), cs, startOffset, endOffset);
             this.templateEdit = templateEdit;
         }
 
         private Pretty(String text, TokenSequence<JFXTokenId> tokens, JavaFXTreePath path, SourcePositions sp, CodeStyle cs, int startOffset, int endOffset) {
             this.fText = text;
             this.sp = sp;
             this.cs = cs;
             this.rightMargin = cs.getRightMargin();
             this.tabSize = cs.getTabSize();
             this.indentSize = cs.getIndentSize();
             this.continuationIndentSize = cs.getContinuationIndentSize();
             this.expandTabToSpaces = cs.expandTabToSpaces();
             this.wrapDepth = 0;
             this.lastBlankLines = -1;
             this.lastBlankLinesTokenIndex = -1;
             this.lastBlankLinesDiff = null;
             Tree tree = path.getLeaf();
             this.indent = tokens != null ? getIndentLevel(tokens, path) : 0;
             this.col = this.indent;
             this.tokens = tokens;
             if (tree.getJavaFXKind() == JavaFXKind.COMPILATION_UNIT) {
                 tokens.moveEnd();
                 tokens.movePrevious();
             } else {
                 tokens.move((int) getEndPos(tree));
                 if (!tokens.moveNext()) {
                     tokens.movePrevious();
                 }
             }
             this.endPos = tokens.offset();
             if (tree.getJavaFXKind() == JavaFXKind.COMPILATION_UNIT) {
                 tokens.moveStart();
             } else {
                 tokens.move((int) sp.getStartPosition(path.getCompilationUnit(), tree));
             }
             tokens.moveNext();
             this.root = path.getCompilationUnit();
             this.startOffset = startOffset;
             this.endOffset = endOffset;
         }
 
         public static LinkedList<Diff> reformat(CompilationInfo info, JavaFXTreePath path, CodeStyle cs, int startOffset, int endOffset, boolean templateEdit) {
             Pretty pretty = new Pretty(info, path, cs, startOffset, endOffset, templateEdit);
             if (pretty.indent >= 0) {
                 pretty.scan(path, null);
             }
             if (path.getLeaf().getJavaFXKind() == JavaFXKind.COMPILATION_UNIT) {
                 pretty.tokens.moveEnd();
                 pretty.tokens.movePrevious();
                 if (pretty.tokens.token().id() != JFXTokenId.WS || pretty.tokens.token().text().toString().indexOf('\n') < 0) { // NOI18N
                     String text = FXSourceUtils.getText(info);
                     pretty.diffs.addFirst(new Diff(text.length(), text.length(), NEWLINE));
                 }
             }
             return pretty.diffs;
         }
 
         public static LinkedList<Diff> reformat(String text, TokenSequence<JFXTokenId> tokens, JavaFXTreePath path, SourcePositions sp, CodeStyle cs) {
             Pretty pretty = new Pretty(text, tokens, path, sp, cs, 0, text.length());
             pretty.scan(path, null);
             tokens.moveEnd();
             tokens.movePrevious();
             if (tokens.token().id() != JFXTokenId.WS || tokens.token().text().toString().indexOf('\n') < 0) {
                 pretty.diffs.addFirst(new Diff(text.length(), text.length(), NEWLINE));
             }
             return pretty.diffs;
         }
 
         @Override
         public Boolean scan(Tree tree, Void p) {
             int lastEndPos = endPos;
             if (tree != null && tree.getJavaFXKind() != JavaFXKind.COMPILATION_UNIT) {
                 if (tree instanceof FakeBlock) {
                     endPos = Integer.MAX_VALUE;
                 } else {
                    endPos = (int) getEndPos(tree);
                 }
             }
             try {
                 return endPos < 0 ? false : tokens.offset() <= endPos ? super.scan(tree, p) : true;
             } finally {
                 endPos = lastEndPos;
             }
         }
 
         @Override
         public Boolean visitCompilationUnit(UnitTree node, Void p) {
             ExpressionTree pkg = node.getPackageName();
             if (pkg != null) {
                 blankLines(cs.getBlankLinesBeforePackage());
                 accept(JFXTokenId.PACKAGE);
                 int old = indent;
                 indent += continuationIndentSize;
                 space();
                 scan(pkg, p);
                 accept(JFXTokenId.SEMI);
                 indent = old;
                 blankLines(cs.getBlankLinesAfterPackage());
             }
             List<? extends ImportTree> imports = node.getImports();
             if (imports != null && !imports.isEmpty()) {
                 blankLines(cs.getBlankLinesBeforeImports());
                 for (ImportTree imp : imports) {
                     blankLines();
                     scan(imp, p);
                 }
                 blankLines(cs.getBlankLinesAfterImports());
             }
             boolean semiRead = false;
             for (Tree typeDecl : node.getTypeDecls()) {
                 if (semiRead && typeDecl.getJavaFXKind() == JavaFXKind.EMPTY_STATEMENT) {
                     continue;
                 }
                 blankLines(cs.getBlankLinesBeforeClass());
                 scan(typeDecl, p);
                 int index = tokens.index();
                 int c = col;
                 Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                 if (accept(JFXTokenId.SEMI) == JFXTokenId.SEMI) {
                     semiRead = true;
                 } else {
                     rollback(index, c, d);
                     semiRead = false;
                 }
                 blankLines(cs.getBlankLinesAfterClass());
             }
             return true;
         }
 
         @Override
         public Boolean visitImport(ImportTree node, Void p) {
             accept(JFXTokenId.IMPORT);
             int old = indent;
             indent += continuationIndentSize;
             space();
 //            if (node.isStatic()) {
 //                accept(STATIC);
 //                space();
 //            }
             scan(node.getQualifiedIdentifier(), p);
             accept(JFXTokenId.SEMI);
             indent = old;
             return true;
         }
 
         @Override
         public Boolean visitClassDeclaration(ClassDeclarationTree node, Void p) {
             if (isSynthetic((JFXTree) node)) {
                 return false;
             }
 
             Tree parent = getCurrentPath().getParentPath().getLeaf();
             if (true) {
 //            if (parent.getKind() != Tree.Kind.NEW_CLASS && (parent.getKind() != Tree.Kind.VARIABLE || !isEnumerator((VariableTree)parent))) {
                 int old = indent;
                 ModifiersTree mods = node.getModifiers();
                 if (mods != null) {
                     if (scan(mods, p)) {
                         indent += continuationIndentSize;
                         if (cs.placeNewLineAfterModifiers()) {
                             newline();
                         } else {
                             space();
                         }
                     }
                 }
 //                JavaTokenId id = accept(CLASS, INTERFACE, ENUM, AT);
                 JFXTokenId id = accept(JFXTokenId.CLASS, JFXTokenId.AT);
                 if (indent == old) {
                     indent += continuationIndentSize;
                 }
                 // no interfaces
 //                if (id == JFXTokenId.AT)
 //                    accept(INTERFACE);
                 space();
                 if (!ERROR.contentEquals(node.getSimpleName())) {
                     accept(JFXTokenId.IDENTIFIER);
                 }
 
                 List<? extends ExpressionTree> exts = node.getImplements();
                 if (exts != null && !exts.isEmpty()) {
                     wrapToken(cs.wrapExtendsImplementsKeyword(), -1, 1, JFXTokenId.EXTENDS);
                     wrapList(cs.wrapExtendsImplementsList(), cs.alignMultilineImplements(), true, exts); // TODO cs.alignMultilineExtends()
                 }
                 List<? extends ExpressionTree> impls = node.getImplements();
                 if (impls != null && !impls.isEmpty()) {
                     wrapToken(cs.wrapExtendsImplementsKeyword(), -1, 1, JFXTokenId.EXTENDS);
                     wrapList(cs.wrapExtendsImplementsList(), cs.alignMultilineImplements(), true, impls);
                 }
                 indent = old;
             }
 
             CodeStyle.BracePlacement bracePlacement = cs.getClassDeclBracePlacement();
             boolean spaceBeforeLeftBrace = cs.spaceBeforeClassDeclLeftBrace();
             int old = indent;
             int halfIndent = indent;
             switch(bracePlacement) {
                 case SAME_LINE:
                     spaces(spaceBeforeLeftBrace ? 1 : 0);
                     accept(JFXTokenId.LBRACE);
                     indent += indentSize;
                     break;
                 case NEW_LINE:
                     newline();
                     accept(JFXTokenId.LBRACE);
                     indent += indentSize;
                     break;
                 case NEW_LINE_HALF_INDENTED:
                     indent += (indentSize >> 1);
                     halfIndent = indent;
                     newline();
                     accept(JFXTokenId.LBRACE);
                     indent = old + indentSize;
                     break;
                 case NEW_LINE_INDENTED:
                     indent += indentSize;
                     halfIndent = indent;
                     newline();
                     accept(JFXTokenId.LBRACE);
                     break;
             }
 
             boolean emptyClass = true;
             for (Tree member : node.getClassMembers()) {
                 if (!isSynthetic((JFXTree) member)) {
                     emptyClass = false;
                     break;
                 }
             }
             if (emptyClass) {
                 newline();
             } else {
                 if (!cs.indentTopLevelClassMembers()) {
                     indent = old;
                 }
                 blankLines(cs.getBlankLinesAfterClassHeader());
                 boolean first = true;
                 boolean semiRead = false;
                 for (Tree member : node.getClassMembers()) {
                     if (!isSynthetic((JFXTree) member)) {
                         switch (member.getJavaFXKind()) {
                             case VARIABLE:
                                 boolean bool = tokens.moveNext();
                                 if (bool) {
                                     tokens.movePrevious();
                                     if (!first) {
                                         blankLines(cs.getBlankLinesBeforeFields());
                                     }
                                     scan(member, p);
                                     blankLines(cs.getBlankLinesAfterFields());
                                 }
                                 break;
                             case FUNCTION_DEFINITION:
                                 if (!first) {
                                     blankLines(cs.getBlankLinesBeforeMethods());
                                 }
                                 scan(member, p);
                                 blankLines(cs.getBlankLinesAfterMethods());
                                 break;
                             case BLOCK_EXPRESSION:
                                 if (semiRead && !((BlockExpressionTree) member).isStatic() && ((BlockExpressionTree) member).getStatements().isEmpty()) {
                                     semiRead = false;
                                     continue;
                                 }
                                 if (!first) {
                                     blankLines(cs.getBlankLinesBeforeMethods());
                                 }
                                 int index = tokens.index();
                                 int c = col;
                                 Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                                 if (accept(JFXTokenId.SEMI) == JFXTokenId.SEMI) {
                                     continue;
                                 } else {
                                     rollback(index, c, d);
                                 }
                                 scan(member, p);
                                 blankLines(cs.getBlankLinesAfterMethods());
                                 break;
                             case CLASS_DECLARATION:
                                 if (!first) {
                                     blankLines(cs.getBlankLinesBeforeClass());
                                 }
                                 scan(member, p);
                                 index = tokens.index();
                                 c = col;
                                 d = diffs.isEmpty() ? null : diffs.getFirst();
                                 if (accept(JFXTokenId.SEMI) == JFXTokenId.SEMI) {
                                     semiRead = true;
                                 } else {
                                     rollback(index, c, d);
                                     semiRead = false;
                                 }
                                 blankLines(cs.getBlankLinesAfterClass());
                                 break;
                         }
                         first = false;
                     }
                 }
                 if (lastBlankLinesTokenIndex < 0) {
                     newline();
                 }
             }
             indent = halfIndent;
 
             Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
             if (diff != null && diff.end == tokens.offset()) {
                 if (diff.text != null) {
                     int idx = diff.text.lastIndexOf('\n'); //NOI18N
                     if (idx < 0) {
                         diff.text = getIndent();
                     } else {
                         diff.text = diff.text.substring(0, idx + 1) + getIndent();
                     }
                 }
                 String spaces = diff.text != null ? diff.text : getIndent();
                 if (spaces.equals(fText.substring(diff.start, diff.end))) {
                     diffs.removeFirst();
                 }
             } else if (tokens.movePrevious()) {
                 if (tokens.token().id() == JFXTokenId.WS) {
                     String text = tokens.token().text().toString();
                     int idx = text.lastIndexOf('\n'); //NOI18N
                     if (idx >= 0) {
                         text = text.substring(idx + 1);
                         String ind = getIndent();
                         if (!ind.equals(text)) {
                             addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                         }
                     }
                 }
                 tokens.moveNext();
             }
             accept(JFXTokenId.RBRACE);
             indent = old;
             return true;
         }
 
         // TODO declarative way
         // TODO on replace
         // TOOD sequence
         @Override
         public Boolean visitVariable(VariableTree node, Void p) {
             int old = indent;
             Tree parent = getCurrentPath().getParentPath().getLeaf();
             boolean insideFor = parent.getJavaFXKind() == JavaFXKind.FOR_EXPRESSION_FOR; // TODO other FOR_EXPRESSIONs ?
             ModifiersTree mods = node.getModifiers();
             if (mods != null) {
                 if (scan(mods, p)) {
                     if (!insideFor) {
                         indent += continuationIndentSize;
                         if (cs.placeNewLineAfterModifiers()) {
                             newline();
                         } else {
                             space();
                         }
                     } else {
                         space();
                     }
                 }
             }
             if (indent == old && !insideFor) {
                 indent += continuationIndentSize;
             }
 
             accept(JFXTokenId.DEF, JFXTokenId.VAR, JFXTokenId.ATTRIBUTE);
             space();
 
             final Name name = node.getName();
             if (!ERROR.contentEquals(name)) {
                 accept(JFXTokenId.IDENTIFIER);
             }
             spaces(cs.spaceAroundAssignOps() ? 1 : 0); // TODO space around colon in the type definition
             accept(JFXTokenId.COLON);
             spaces(cs.spaceAroundAssignOps() ? 1 : 0); // TODO space around colon in the type definition
             scan(node.getType(), p);
 
             ExpressionTree init = node.getInitializer();
             if (init != null) {
                 int alignIndent = -1;
                 if (cs.alignMultilineAssignment()) {
                     alignIndent = col;
                     if (!ERROR.contentEquals(name)) {
                         alignIndent -= name.length();
                     }
                 }
                 spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                 accept(JFXTokenId.EQ);
                 wrapTree(cs.wrapAssignOps(), alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, init);
             }
             accept(JFXTokenId.SEMI);
             indent = old;
             return true;
         }
 
         @Override
         public Boolean visitFunctionDefinition(FunctionDefinitionTree node, Void p) {
             JFXFunctionDefinition funcDef = (JFXFunctionDefinition) node;
             int old = indent;
 
             ModifiersTree mods = funcDef.getModifiers();
             if (mods != null) {
                 if (scan(mods, p)) {
                     indent += continuationIndentSize;
                     if (cs.placeNewLineAfterModifiers()) {
                         newline();
                     } else {
                         space();
                     }
                 } else {
                     blankLines();
                 }
             }
 
             accept(JFXTokenId.FUNCTION);
             space();
 
             if (!ERROR.contentEquals(funcDef.getName())) {
                 accept(JFXTokenId.IDENTIFIER);
             }
 
             if (indent == old) {
                 indent += continuationIndentSize;
             }
             spaces(cs.spaceBeforeMethodDeclParen() ? 1 : 0);
             accept(JFXTokenId.LPAREN);
             List<? extends JFXVar> params = funcDef.getParams();
             if (params != null && !params.isEmpty()) {
                 spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0, true);
                 wrapList(cs.wrapMethodParams(), cs.alignMultilineMethodParams(), false, params);
                 spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0);
             }
             accept(JFXTokenId.RPAREN);
 
             spaces(cs.spaceAroundAssignOps() ? 1 : 0); // TODO space around colon in the type definition
             accept(JFXTokenId.COLON);
             spaces(cs.spaceAroundAssignOps() ? 1 : 0); // TODO space around colon in the type definition
 
             FunctionValueTree retType = funcDef.getFunctionValue();
             if (retType != null) {
                 scan(retType, p);
                 if (indent == old) {
                     indent += continuationIndentSize;
                 }
                 space();
             }
 
             indent = old;
             JFXBlock body = funcDef.getBodyExpression();
             if (body != null) {
                 scan(body, p);
             } else {
                 accept(JFXTokenId.SEMI);
             }
             return true;
         }
 
         @Override
         public Boolean visitModifiers(ModifiersTree node, Void p) {
             boolean ret = true;
             JFXTokenId id = null;
             JavaFXTreePath path = getCurrentPath().getParentPath();
             path = path.getParentPath();
             while (tokens.offset() < endPos) {
                 if (id != null) {
                     space();
                 }
                 // TODO other modifiers
                 id = accept(JFXTokenId.PRIVATE, JFXTokenId.PROTECTED, JFXTokenId.PUBLIC, JFXTokenId.PUBLIC_READ, JFXTokenId.PUBLIC_INIT,
                         JFXTokenId.STATIC, JFXTokenId.ABSTRACT, JFXTokenId.NATIVEARRAY, JFXTokenId.AT, JFXTokenId.MIXIN);
                 if (id == null) {
                     break;
                 }
                 ret = id != JFXTokenId.AT;
             }
             return ret;
         }
 
         @Override
         public Boolean visitBlockExpression(BlockExpressionTree node, Void p) {
             if (node.isStatic()) {
                 accept(JFXTokenId.STATIC);
             }
             CodeStyle.BracePlacement bracePlacement;
             boolean spaceBeforeLeftBrace = false;
             switch (getCurrentPath().getParentPath().getLeaf().getJavaFXKind()) {
                 case CLASS_DECLARATION:
                     bracePlacement = cs.getOtherBracePlacement();
                     if (node.isStatic()) {
                         spaceBeforeLeftBrace = cs.spaceBeforeStaticInitLeftBrace();
                     }
                     break;
                 case FUNCTION_DEFINITION:
                     bracePlacement = cs.getMethodDeclBracePlacement();
                     spaceBeforeLeftBrace = cs.spaceBeforeMethodDeclLeftBrace();
                     break;
                 case TRY:
                     bracePlacement = cs.getOtherBracePlacement();
                     if (((TryTree) getCurrentPath().getParentPath().getLeaf()).getBlock() == node) {
                         spaceBeforeLeftBrace = cs.spaceBeforeTryLeftBrace();
                     } else {
                         spaceBeforeLeftBrace = cs.spaceBeforeFinallyLeftBrace();
                     }
                     break;
                 case CATCH:
                     bracePlacement = cs.getOtherBracePlacement();
                     spaceBeforeLeftBrace = cs.spaceBeforeCatchLeftBrace();
                     break;
                 case WHILE_LOOP:
                     bracePlacement = cs.getOtherBracePlacement();
                     spaceBeforeLeftBrace = cs.spaceBeforeWhileLeftBrace();
                     break;
                 case FOR_EXPRESSION_FOR:
                 case FOR_EXPRESSION_IN_CLAUSE: // TODO check it
                 case FOR_EXPRESSION_PREDICATE: // TODO check it
                     bracePlacement = cs.getOtherBracePlacement();
                     spaceBeforeLeftBrace = cs.spaceBeforeForLeftBrace();
                     break;
 //                case IF:
 //                    bracePlacement = cs.getOtherBracePlacement();
 //                    if (((IfTree)getCurrentPath().getParentPath().getLeaf()).getThenStatement() == node)
 //                        spaceBeforeLeftBrace = cs.spaceBeforeIfLeftBrace();
 //                    else
 //                        spaceBeforeLeftBrace = cs.spaceBeforeElseLeftBrace();
 //                    break;
                 default:
                     bracePlacement = cs.getOtherBracePlacement();
                     break;
             }
 
             int old = indent;
             int halfIndent = indent;
             switch (bracePlacement) {
                 case SAME_LINE:
                     spaces(spaceBeforeLeftBrace ? 1 : 0);
                     if (node instanceof FakeBlock) {
                         appendToDiff("{"); //NOI18N
                         lastBlankLines = -1;
                         lastBlankLinesTokenIndex = -1;
                         lastBlankLinesDiff = null;
                     } else {
                         accept(JFXTokenId.LBRACE);
                     }
                     indent += indentSize;
                     break;
                 case NEW_LINE:
                     newline();
                     if (node instanceof FakeBlock) {
                         indent += indentSize;
                         appendToDiff("{"); //NOI18N
                         lastBlankLines = -1;
                         lastBlankLinesTokenIndex = -1;
                         lastBlankLinesDiff = null;
                     } else {
                         accept(JFXTokenId.LBRACE);
                         indent += indentSize;
                     }
                     break;
                 case NEW_LINE_HALF_INDENTED:
                     indent += (indentSize >> 1);
                     halfIndent = indent;
                     newline();
                     if (node instanceof FakeBlock) {
                         indent = old + indentSize;
                         appendToDiff("{"); //NOI18N
                         lastBlankLines = -1;
                         lastBlankLinesTokenIndex = -1;
                         lastBlankLinesDiff = null;
                     } else {
                         accept(JFXTokenId.LBRACE);
                         indent = old + indentSize;
                     }
                     break;
                 case NEW_LINE_INDENTED:
                     indent += indentSize;
                     halfIndent = indent;
                     newline();
                     if (node instanceof FakeBlock) {
                         appendToDiff("{"); //NOI18N
                         lastBlankLines = -1;
                         lastBlankLinesTokenIndex = -1;
                         lastBlankLinesDiff = null;
                     } else {
                         accept(JFXTokenId.LBRACE);
                     }
                     break;
             }
 
             boolean isEmpty = true;
             for (ExpressionTree stat : node.getStatements()) {
                 if (!isSynthetic((JFXTree) node)) {
                     isEmpty = false;
                     if (node instanceof FakeBlock) {
                         appendToDiff(getNewlines(1) + getIndent());
                         col = indent;
 //                    } else if (!fieldGroup || stat.getJavaFXKind() != JavaFXKind.VARIABLE) {
                     } else {
                         blankLines();
                     }
                     scan(stat, p);
                 }
             }
 
             if (isEmpty || templateEdit) {
                 newline();
             }
             if (node instanceof FakeBlock) {
                 indent = halfIndent;
                 int i = tokens.index();
                 boolean loop = true;
                 while (loop) {
                     switch (tokens.token().id()) {
                         case WS:
                             if (tokens.token().text().toString().indexOf('\n') < 0) {
                                 tokens.moveNext();
                             } else {
                                 loop = false;
                                 appendToDiff("\n"); //NOI18N
                                 col = 0;
                             }
                             break;
                         case LINE_COMMENT:
                             loop = false;
 //                        case BLOCK_COMMENT:
                         case COMMENT:
                             tokens.moveNext();
                             break;
                         default:
                             if (tokens.index() != i) {
                                 tokens.moveIndex(i);
                                 tokens.moveNext();
                             }
                             loop = false;
                             appendToDiff("\n"); //NOI18N
                             col = 0;
                     }
                 }
                 appendToDiff(getIndent() + "}"); //NOI18N
                 col = indent + 1;
                 lastBlankLines = -1;
                 lastBlankLinesTokenIndex = -1;
                 lastBlankLinesDiff = null;
             } else {
                 blankLines();
                 indent = halfIndent;
                 Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                 if (diff != null && diff.end == tokens.offset()) {
                     if (diff.text != null) {
                         int idx = diff.text.lastIndexOf('\n'); //NOI18N
                         if (idx < 0) {
                             diff.text = getIndent();
                         } else {
                             diff.text = diff.text.substring(0, idx + 1) + getIndent();
                         }
 
                     }
                     String spaces = diff.text != null ? diff.text : getIndent();
                     if (spaces.equals(fText.substring(diff.start, diff.end))) {
                         diffs.removeFirst();
                     }
                 } else if (tokens.movePrevious()) {
                     if (tokens.token().id() == JFXTokenId.WS) {
                         String text = tokens.token().text().toString();
                         int idx = text.lastIndexOf('\n'); //NOI18N
                         if (idx >= 0) {
                             text = text.substring(idx + 1);
                             String ind = getIndent();
                             if (!ind.equals(text)) {
                                 addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
                             }
                         }
                     }
                     tokens.moveNext();
                 }
                 accept(JFXTokenId.RBRACE);
             }
             indent = old;
             return true;
         }
 
         @Override
         public Boolean visitMemberSelect(MemberSelectTree node, Void p) {
             scan(node.getExpression(), p);
             accept(JFXTokenId.DOT);
             accept(JFXTokenId.IDENTIFIER, JFXTokenId.STAR, JFXTokenId.THIS, JFXTokenId.SUPER, JFXTokenId.CLASS);
             return true;
         }
 
         // Java MethodInvocationTree --> FunctionInvocationTree
         @Override
         public Boolean visitMethodInvocation(FunctionInvocationTree node, Void p) {
             ExpressionTree ms = node.getMethodSelect();
             if (ms.getJavaFXKind() == JavaFXKind.MEMBER_SELECT) {
                 ExpressionTree exp = ((MemberSelectTree) ms).getExpression();
                 scan(exp, p);
                 accept(JFXTokenId.DOT);
 
                 // TODO WTF type args are doing in javafx?
 //                List<? extends Tree> targs = node.getTypeArguments();
 //                if (targs != null && !targs.isEmpty()) {
 //                    accept(JFXTokenId.LT);
 //                    for (Iterator<? extends Tree> it = targs.iterator(); it.hasNext();) {
 //                        Tree targ = it.next();
 //                        scan(targ, p);
 //                        if (it.hasNext()) {
 //                            spaces(cs.spaceBeforeComma() ? 1 : 0);
 //                            accept(JFXTokenId.COMMA);
 //                            spaces(cs.spaceAfterComma() ? 1 : 0);
 //                        }
 //                    }
 ////                    accept(GT, GTGT, GTGTGT);
 //                    accept(JFXTokenId.GT);
 //                }
 
                 CodeStyle.WrapStyle wrapStyle = cs.wrapChainedMethodCalls();
                 if (exp.getJavaFXKind() == JavaFXKind.METHOD_INVOCATION) {
                     wrapToken(wrapStyle, -1, 0, JFXTokenId.IDENTIFIER, JFXTokenId.THIS, JFXTokenId.SUPER);
                 } else {
                     int index = tokens.index();
                     int c = col;
                     Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                     accept(JFXTokenId.IDENTIFIER, JFXTokenId.THIS, JFXTokenId.SUPER);
                     if (wrapStyle != CodeStyle.WrapStyle.WRAP_NEVER && col > rightMargin && c > indent && (wrapDepth == 0 || c <= rightMargin)) {
                         rollback(index, c, d);
                         newline();
                         accept(JFXTokenId.IDENTIFIER, JFXTokenId.THIS, JFXTokenId.SUPER);
                     }
                 }
             } else {
                 scan(node.getMethodSelect(), p);
             }
 
             spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);
             accept(JFXTokenId.LPAREN);
             List<? extends ExpressionTree> args = node.getArguments();
             if (args != null && !args.isEmpty()) {
                 spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                 wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), false, args);
                 spaces(cs.spaceWithinMethodCallParens() ? 1 : 0);
             }
             accept(JFXTokenId.RPAREN);
             return true;
         }
 
         // Java NewClassTree --> InstantiateTree
         @Override
         public Boolean visitInstantiate(InstantiateTree node, Void p) {
 //            ExpressionTree encl = node.getEnclosingExpression();
 //            if (encl != null) {
 //                scan(encl, p);
 //                accept(DOT);
 //            }
             boolean indented = false;
             if (col == indent) {
                 Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                 if (d != null && d.getStartOffset() == tokens.offset() && d.getText() != null && d.getText().indexOf('\n') >= 0) {
                     indented = true;
                 } else {
                     tokens.movePrevious();
                     if (tokens.token().id() == JFXTokenId.WS && tokens.token().text().toString().indexOf('\n') >= 0) {
                         indented = true;
                     }
                     tokens.moveNext();
                 }
             }
 
             accept(JFXTokenId.NEW);
             space();
 //            List<? extends Tree> targs = node.getTypeArguments();
 //            if (targs != null && !targs.isEmpty()) {
 //                accept(LT);
 //                for (Iterator<? extends Tree> it = targs.iterator(); it.hasNext();) {
 //                    Tree targ = it.next();
 //                    scan(targ, p);
 //                    if (it.hasNext()) {
 //                        spaces(cs.spaceBeforeComma() ? 1 : 0);
 //                        accept(COMMA);
 //                        spaces(cs.spaceAfterComma() ? 1 : 0);
 //                    }
 //                }
 //                accept(GT, GTGT, GTGTGT);
 //            }
             scan(node.getIdentifier(), p);
             spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);
             accept(JFXTokenId.LPAREN);
             List<? extends ExpressionTree> args = node.getArguments();
             if (args != null && !args.isEmpty()) {
                 spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                 wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), false, args);
                 spaces(cs.spaceWithinMethodCallParens() ? 1 : 0);
             }
             accept(JFXTokenId.RPAREN);
             ClassDeclarationTree body = node.getClassBody();
             if (body != null) {
                 int old = indent;
                 if (!indented) {
                     indent -= continuationIndentSize;
                 }
                 scan(body, p);
                 indent = old;
             }
             return true;
         }
 
         @Override
         public Boolean visitReturn(ReturnTree node, Void p) {
             accept(JFXTokenId.RETURN);
             int old = indent;
             indent += continuationIndentSize;
             ExpressionTree exp = node.getExpression();
             if (exp != null) {
                 space();
                 scan(exp, p);
             }
             accept(JFXTokenId.SEMI);
             indent = old;
             return true;
         }
 
         @Override
         public Boolean visitThrow(ThrowTree node, Void p) {
             accept(JFXTokenId.THROW);
             int old = indent;
             indent += continuationIndentSize;
             ExpressionTree exp = node.getExpression();
             if (exp != null) {
                 space();
                 scan(exp, p);
             }
             accept(JFXTokenId.SEMI);
             indent = old;
             return true;
         }
 
         @Override
         public Boolean visitTry(TryTree node, Void p) {
             accept(JFXTokenId.TRY);
             scan(node.getBlock(), p);
             for (CatchTree catchTree : node.getCatches()) {
                 if (cs.placeCatchOnNewLine()) {
                     newline();
                 } else {
                     spaces(cs.spaceBeforeCatch() ? 1 : 0);
                 }
                 scan(catchTree, p);
             }
             BlockExpressionTree finallyBlockTree = node.getFinallyBlock();
             if (finallyBlockTree != null) {
                 if (cs.placeFinallyOnNewLine()) {
                     newline();
                 } else {
                     spaces(cs.spaceBeforeFinally() ? 1 : 0);
                 }
                 accept(JFXTokenId.FINALLY);
                 scan(finallyBlockTree, p);
             }
             return true;
         }
 
         @Override
         public Boolean visitCatch(CatchTree node, Void p) {
             accept(JFXTokenId.CATCH);
             int old = indent;
             indent += continuationIndentSize;
             spaces(cs.spaceBeforeCatchParen() ? 1 : 0);
             accept(JFXTokenId.LPAREN);
             spaces(cs.spaceWithinCatchParens() ? 1 : 0);
             scan(node.getParameter(), p);
             spaces(cs.spaceWithinCatchParens() ? 1 : 0);
             accept(JFXTokenId.RPAREN);
             indent = old;
             scan(node.getBlock(), p);
             return true;
         }
 
 //        @Override
 //        public Boolean visitDoWhileLoop(DoWhileLoopTree node, Void p) {
 //            accept(DO);
 //            int old = indent;
 //            CodeStyle.BracesGenerationStyle redundantDoWhileBraces = cs.redundantDoWhileBraces();
 //            if (redundantDoWhileBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < sp.getEndPosition(root, node)))
 //                redundantDoWhileBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
 //            boolean prevblock = wrapStatement(cs.wrapDoWhileStatement(), redundantDoWhileBraces, cs.spaceBeforeDoLeftBrace() ? 1 : 0, node.getStatement());
 //            if (cs.placeWhileOnNewLine() || !prevblock) {
 //                newline();
 //            } else {
 //                spaces(cs.spaceBeforeWhile() ? 1 : 0);
 //            }
 //            accept(WHILE);
 //            indent += continuationIndentSize;
 //            spaces(cs.spaceBeforeWhileParen() ? 1 : 0);
 //            scan(node.getCondition(), p);
 //            accept(SEMICOLON);
 //            indent = old;
 //            return true;
 //        }
 
         @Override
         public Boolean visitWhileLoop(WhileLoopTree node, Void p) {
             accept(JFXTokenId.WHILE);
             int old = indent;
             indent += continuationIndentSize;
             spaces(cs.spaceBeforeWhileParen() ? 1 : 0);
             scan(node.getCondition(), p);
             indent = old;
             CodeStyle.BracesGenerationStyle redundantWhileBraces = cs.redundantWhileBraces();
             if (redundantWhileBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > getStartPos(node) || endOffset < getEndPos(node))) {
                 redundantWhileBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
             }
             wrapStatement(cs.wrapWhileStatement(), redundantWhileBraces, cs.spaceBeforeWhileLeftBrace() ? 1 : 0, node.getStatement());
             return true;
         }
 
 
 //        @Override
 //        public Boolean visitForLoop(ForLoopTree node, Void p) {
 //            accept(FOR);
 //            int old = indent;
 //            indent += continuationIndentSize;
 //            spaces(cs.spaceBeforeForParen() ? 1 : 0);
 //            accept(LPAREN);
 //            spaces(cs.spaceWithinForParens() ? 1 : 0);
 //            List<? extends StatementTree> inits = node.getInitializer();
 //            int alignIndent = -1;
 //            if (inits != null && !inits.isEmpty()) {
 //                if (cs.alignMultilineFor())
 //                    alignIndent = col;
 //                for (Iterator<? extends StatementTree> it = inits.iterator(); it.hasNext();) {
 //                    scan(it.next(), p);
 //                    if (it.hasNext() && !fieldGroup) {
 //                        spaces(cs.spaceBeforeComma() ? 1 : 0);
 //                        accept(COMMA);
 //                        spaces(cs.spaceAfterComma() ? 1 : 0);
 //                    }
 //                }
 //                spaces(cs.spaceBeforeSemi() ? 1 : 0);
 //            }
 //            accept(SEMICOLON);
 //            ExpressionTree cond = node.getCondition();
 //            if (cond != null) {
 //                wrapTree(cs.wrapFor(), alignIndent, cs.spaceAfterSemi() ? 1 : 0, cond);
 //                spaces(cs.spaceBeforeSemi() ? 1 : 0);
 //            }
 //            accept(SEMICOLON);
 //            List<? extends ExpressionStatementTree> updates = node.getUpdate();
 //            if (updates != null && !updates.isEmpty()) {
 //                boolean first = true;
 //                for (Iterator<? extends ExpressionStatementTree> it = updates.iterator(); it.hasNext();) {
 //                    ExpressionStatementTree update = it.next();
 //                    if (first) {
 //                        wrapTree(cs.wrapFor(), alignIndent, cs.spaceAfterSemi() ? 1 : 0, update);
 //                    } else {
 //                        scan(update, p);
 //                    }
 //                    first = false;
 //                    if (it.hasNext()) {
 //                        spaces(cs.spaceBeforeComma() ? 1 : 0);
 //                        accept(COMMA);
 //                        spaces(cs.spaceAfterComma() ? 1 : 0);
 //                    }
 //                }
 //            }
 //            spaces(cs.spaceWithinForParens() ? 1 : 0);
 //            accept(RPAREN);
 //            indent = old;
 //            CodeStyle.BracesGenerationStyle redundantForBraces = cs.redundantForBraces();
 //            if (redundantForBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < sp.getEndPosition(root, node)))
 //                redundantForBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
 //            wrapStatement(cs.wrapForStatement(), redundantForBraces, cs.spaceBeforeForLeftBrace() ? 1 : 0, node.getStatement());
 //            return true;
 //        }
 
         // TODO javafx for loop
 //        @Override
 //        public Boolean visitForExpression(ForExpressionTree node, Void p) {
 //            accept(JFXTokenId.FOR);
 //            int old = indent;
 //            indent += continuationIndentSize;
 //            spaces(cs.spaceBeforeForParen() ? 1 : 0);
 //            accept(JFXTokenId.LPAREN);
 //            spaces(cs.spaceWithinForParens() ? 1 : 0);
 //            int alignIndent = cs.alignMultilineFor() ? col : -1;
 //            scan(node.getVariable(), p);
 //            spaces(cs.spaceBeforeColon() ? 1 : 0);
 //            accept(JFXTokenId.COLON);
 //            wrapTree(cs.wrapFor(), alignIndent, cs.spaceAfterColon() ? 1 : 0, node.getExpression());
 //            spaces(cs.spaceWithinForParens() ? 1 : 0);
 //            accept(JFXTokenId.RPAREN);
 //            indent = old;
 //            CodeStyle.BracesGenerationStyle redundantForBraces = cs.redundantForBraces();
 //            if (redundantForBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > sp.getStartPosition(root, node) || endOffset < sp.getEndPosition(root, node)))
 //                redundantForBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
 //            wrapStatement(cs.wrapForStatement(), redundantForBraces, cs.spaceBeforeForLeftBrace() ? 1 : 0, node.getStatement());
 //            return true;
 //        }
 //
 //        @Override
 //        public Boolean visitForExpressionInClause(ForExpressionInClauseTree node, Void p) {
 //            node.
 //            return super.visitForExpressionInClause(node, p);
 //        }
 
 //        @Override
 //        public Boolean visitSwitch(SwitchTree node, Void p) {
 //            accept(SWITCH);
 //            int old = indent;
 //            indent += continuationIndentSize;
 //            spaces(cs.spaceBeforeSwitchParen() ? 1 : 0);
 //            scan(node.getExpression(), p);
 //            CodeStyle.BracePlacement bracePlacement = cs.getOtherBracePlacement();
 //            boolean spaceBeforeLeftBrace = cs.spaceBeforeSwitchLeftBrace();
 //            boolean indentCases = cs.indentCasesFromSwitch();
 //            indent = old;
 //            int halfIndent = indent;
 //            switch(bracePlacement) {
 //                case SAME_LINE:
 //                    spaces(spaceBeforeLeftBrace ? 1 : 0);
 //                    accept(LBRACE);
 //                    if (indentCases)
 //                        indent += indentSize;
 //                    break;
 //                case NEW_LINE:
 //                    newline();
 //                    accept(LBRACE);
 //                    if (indentCases)
 //                        indent += indentSize;
 //                    break;
 //                case NEW_LINE_HALF_INDENTED:
 //                    indent += (indentSize >> 1);
 //                    halfIndent = indent;
 //                    newline();
 //                    accept(LBRACE);
 //                    if (indentCases)
 //                        indent = old + indentSize;
 //                    else
 //                        indent = old;
 //                    break;
 //                case NEW_LINE_INDENTED:
 //                    indent += indentSize;
 //                    halfIndent = indent;
 //                    newline();
 //                    accept(LBRACE);
 //                    if (!indentCases)
 //                        indent = old;
 //                    break;
 //            }
 //            for (CaseTree caseTree : node.getCases()) {
 //                blankLines();
 //                scan(caseTree, p);
 //            }
 //            blankLines();
 //            indent = halfIndent;
 //            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
 //            if (diff != null && diff.end == tokens.offset()) {
 //                if (diff.text != null) {
 //                    int idx = diff.text.lastIndexOf('\n'); //NOI18N
 //                    if (idx < 0)
 //                        diff.text = getIndent();
 //                    else
 //                        diff.text = diff.text.substring(0, idx + 1) + getIndent();
 //
 //                }
 //                String spaces = diff.text != null ? diff.text : getIndent();
 //                if (spaces.equals(fText.substring(diff.start, diff.end)))
 //                    diffs.removeFirst();
 //            } else if (tokens.movePrevious()) {
 //                if (tokens.token().id() == WHITESPACE) {
 //                    String text =  tokens.token().text().toString();
 //                    int idx = text.lastIndexOf('\n'); //NOI18N
 //                    if (idx >= 0) {
 //                        text = text.substring(idx + 1);
 //                        String ind = getIndent();
 //                        if (!ind.equals(text))
 //                            addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
 //                    }
 //                }
 //                tokens.moveNext();
 //            }
 //            accept(RBRACE);
 //            indent = old;
 //            return true;
 //        }
 
 //        @Override
 //        public Boolean visitCase(CaseTree node, Void p) {
 //            ExpressionTree exp = node.getExpression();
 //            if (exp != null) {
 //                accept(CASE);
 //                space();
 //                scan(exp, p);
 //            } else {
 //                accept(DEFAULT);
 //            }
 //            accept(COLON);
 //            int old = indent;
 //            indent += indentSize;
 //            for (StatementTree stat : node.getStatements()) {
 //                if (stat.getKind() == Tree.Kind.BLOCK) {
 //                    indent = old;
 //                    scan(stat, p);
 //                } else {
 //                    blankLines();
 //                    scan(stat, p);
 //                }
 //            }
 //            indent = old;
 //            return true;
 //        }
 
         @Override
         public Boolean visitBreak(BreakTree node, Void p) {
             accept(JFXTokenId.BREAK);
             Name label = node.getLabel();
             if (label != null) {
                 space();
                 accept(JFXTokenId.IDENTIFIER);
             }
             accept(JFXTokenId.SEMI);
             return true;
         }
 
         @Override
         public Boolean visitContinue(ContinueTree node, Void p) {
             accept(JFXTokenId.CONTINUE);
             Name label = node.getLabel(); // TODO there is no label in javafx, check it
             if (label != null) {
                 space();
                 accept(JFXTokenId.IDENTIFIER);
             }
             accept(JFXTokenId.SEMI);
             return true;
         }
 
         // TODO sequence
         @Override
         public Boolean visitAssignment(AssignmentTree node, Void p) {
             int alignIndent = cs.alignMultilineAssignment() ? col : -1;
             boolean b = scan(node.getVariable(), p);
             if (b) {
                 spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                 accept(JFXTokenId.EQ);
                 ExpressionTree expr = node.getExpression();
 //                if (expr.getJavaFXKind() == JavaFXKind.NEW_ARRAY && ((NewArrayTree)expr).getType() == null) {
 //                    if (cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE)
 //                        spaces(cs.spaceAroundAssignOps() ? 1 : 0);
 //                    scan(expr, p);
 //                } else {
                 wrapTree(cs.wrapAssignOps(), alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, expr);
 //                }
             } else {
                 scan(node.getExpression(), p);
             }
             return true;
         }
 
         @Override
         public Boolean visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
             int alignIndent = cs.alignMultilineAssignment() ? col : -1;
             scan(node.getVariable(), p);
             spaces(cs.spaceAroundAssignOps() ? 1 : 0);
             if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                 col += tokens.token().length();
                 lastBlankLines = -1;
                 lastBlankLinesTokenIndex = -1;
                 lastBlankLinesDiff = null;
                 tokens.moveNext();
             }
             wrapTree(cs.wrapAssignOps(), alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, node.getExpression());
             return true;
         }
 
         @Override
         public Boolean visitTypeAny(TypeAnyTree node, Void p) {
             return super.visitTypeAny(node, p);
         }
 
         // TODO sequences
         @Override
         public Boolean visitTypeArray(TypeArrayTree node, Void p) {
             boolean ret = scan(node.getElementType(), p);
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             JFXTokenId id = accept(JFXTokenId.LBRACKET, JFXTokenId.IDENTIFIER);
             if (id != JFXTokenId.IDENTIFIER) {
                 accept(JFXTokenId.RBRACKET);
                 return ret;
             }
             rollback(index, c, d);
             spaces(1, false);
             accept(JFXTokenId.IDENTIFIER);
             accept(JFXTokenId.LBRACKET);
             accept(JFXTokenId.RBRACKET);
             return false;
         }
 
         @Override
         public Boolean visitTypeClass(TypeClassTree node, Void p) {
             accept(JFXTokenId.IDENTIFIER);
             return true;
         }
 
         @Override
         public Boolean visitTypeFunctional(TypeFunctionalTree node, Void p) {
             return super.visitTypeFunctional(node, p);
         }
 
         @Override
         public Boolean visitTypeUnknown(TypeUnknownTree node, Void p) {
             // copied from java visitOther()
             do {
                 col += tokens.token().length();
             } while (tokens.moveNext() && tokens.offset() < endPos);
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
             return true;
         }
 
 //        @Override
 //        public Boolean visitArrayAccess(ArrayAccessTree node, Void p) {
 //            scan(node.getExpression(), p);
 //            accept(LBRACKET);
 //            scan(node.getIndex(), p);
 //            accept(RBRACKET);
 //            return true;
 //        }
 
 //        @Override
 //        public Boolean visitNewArray(NewArrayTree node, Void p) {
 //            Tree type = node.getType();
 //            List<? extends ExpressionTree> inits = node.getInitializers();
 //            if (type != null) {
 //                accept(NEW);
 //                space();
 //                int n = inits != null ? 1 : 0;
 //                while (type.getKind() == Tree.Kind.ARRAY_TYPE) {
 //                    n++;
 //                    type = ((ArrayTypeTree)type).getType();
 //                }
 //                scan(type, p);
 //                for (ExpressionTree dim : node.getDimensions()) {
 //                    accept(LBRACKET);
 //                    spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
 //                    scan(dim, p);
 //                    spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
 //                    accept(RBRACKET);
 //                }
 //                while(--n >= 0) {
 //                    accept(LBRACKET);
 //                    accept(RBRACKET);
 //                }
 //            }
 //            if (inits != null) {
 //                CodeStyle.BracePlacement bracePlacement = cs.getOtherBracePlacement();
 //                boolean spaceBeforeLeftBrace = cs.spaceBeforeArrayInitLeftBrace();
 //                int oldIndent = indent;
 //                Tree parent = getCurrentPath().getParentPath().getLeaf();
 //                switch (parent.getKind()) {
 //                    case ASSIGNMENT:
 //                        Tree grandParent = getCurrentPath().getParentPath().getParentPath().getLeaf();
 //                        if (grandParent.getKind() != Tree.Kind.BLOCK && grandParent.getKind() != Tree.Kind.CLASS)
 //                            break;
 //                    case VARIABLE:
 //                    case METHOD:
 //                        indent -= continuationIndentSize;
 //                        break;
 //                }
 //                int old = indent;
 //                int halfIndent = indent;
 //                switch(bracePlacement) {
 //                    case SAME_LINE:
 //                        if (type != null)
 //                            spaces(spaceBeforeLeftBrace ? 1 : 0);
 //                        accept(LBRACE);
 //                        indent += indentSize;
 //                        break;
 //                    case NEW_LINE:
 //                        newline();
 //                        accept(LBRACE);
 //                        indent += indentSize;
 //                        break;
 //                    case NEW_LINE_HALF_INDENTED:
 //                        indent += (indentSize >> 1);
 //                        halfIndent = indent;
 //                        newline();
 //                        accept(LBRACE);
 //                        indent = old + indentSize;
 //                        break;
 //                    case NEW_LINE_INDENTED:
 //                        indent += indentSize;
 //                        halfIndent = indent;
 //                        newline();
 //                        accept(LBRACE);
 //                        break;
 //                }
 //                boolean afterNewline = bracePlacement != CodeStyle.BracePlacement.SAME_LINE;
 //                if (!inits.isEmpty()) {
 //                    if (afterNewline)
 //                        newline();
 //                    else
 //                        spaces(cs.spaceWithinBraces() ? 1 : 0, true);
 //                    wrapList(cs.wrapArrayInit(), cs.alignMultilineArrayInit(), false, inits);
 //                    if (tokens.token().text().toString().indexOf('\n') >= 0)
 //                        afterNewline = true;
 //                    int index = tokens.index();
 //                    int c = col;
 //                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
 //                    if (accept(COMMA) == null)
 //                        rollback(index, c, d);
 //                    indent -= indentSize;
 //                    if (afterNewline)
 //                        newline();
 //                    else
 //                        spaces(cs.spaceWithinBraces() ? 1 : 0);
 //                } else if (afterNewline) {
 //                    newline();
 //                }
 //                indent = halfIndent;
 //                if (afterNewline) {
 //                    Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
 //                    if (diff != null && diff.end == tokens.offset()) {
 //                        if (diff.text != null) {
 //                            int idx = diff.text.lastIndexOf('\n'); //NOI18N
 //                            if (idx < 0)
 //                                diff.text = getIndent();
 //                            else
 //                                diff.text = diff.text.substring(0, idx + 1) + getIndent();
 //
 //                        }
 //                        String spaces = diff.text != null ? diff.text : getIndent();
 //                        if (spaces.equals(fText.substring(diff.start, diff.end)))
 //                            diffs.removeFirst();
 //                    } else if (tokens.movePrevious()) {
 //                        if (tokens.token().id() == WHITESPACE) {
 //                            String text =  tokens.token().text().toString();
 //                            int idx = text.lastIndexOf('\n'); //NOI18N
 //                            if (idx >= 0) {
 //                                text = text.substring(idx + 1);
 //                                String ind = getIndent();
 //                                if (!ind.equals(text))
 //                                    addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + tokens.token().length(), ind));
 //                            }
 //                        }
 //                        tokens.moveNext();
 //                    }
 //                }
 //                accept(RBRACE);
 //                indent = oldIndent;
 //            }
 //            return true;
 //        }
 
         @Override
         public Boolean visitIdentifier(IdentifierTree node, Void p) {
             accept(JFXTokenId.IDENTIFIER, JFXTokenId.THIS, JFXTokenId.SUPER);
             return true;
         }
 
         @Override
         public Boolean visitUnary(UnaryTree node, Void p) {
             JFXTokenId id = tokens.token().id();
             if (OPERATOR.equals(id.primaryCategory())) {
                 spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                 col += tokens.token().length();
                 lastBlankLines = -1;
                 lastBlankLinesTokenIndex = -1;
                 lastBlankLinesDiff = null;
                 tokens.moveNext();
                 int index = tokens.index();
                 int c = col;
                 Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                 spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                 if (tokens.token().id() == id) {
                     rollback(index, c, d);
                     space();
                 }
                 scan(node.getExpression(), p);
             } else {
                 scan(node.getExpression(), p);
                 spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                 col += tokens.token().length();
                 lastBlankLines = -1;
                 lastBlankLinesTokenIndex = -1;
                 lastBlankLinesDiff = null;
                 tokens.moveNext();
                 spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
             }
             return true;
         }
 
         @Override
         public Boolean visitBinary(BinaryTree node, Void p) {
             int alignIndent = cs.alignMultilineBinaryOp() ? col : -1;
             scan(node.getLeftOperand(), p);
             wrapOperatorAndTree(cs.wrapBinaryOps(), alignIndent, cs.spaceAroundBinaryOps() ? 1 : 0, node.getRightOperand());
             return true;
         }
 
         // "if else" is here
         @Override
         public Boolean visitConditionalExpression(ConditionalExpressionTree node, Void p) {
             JFXIfExpression ifExpr = (JFXIfExpression) node;
             
             accept(JFXTokenId.IF);
             int old = indent;
             indent += continuationIndentSize;
             spaces(cs.spaceBeforeIfParen() ? 1 : 0);
             scan(ifExpr.getCondition(), p);
             indent = old;
 
             JFXExpression trueExpr = ifExpr.getTrueExpression();
             JFXExpression falseExpr = ifExpr.getFalseExpression();
             CodeStyle.BracesGenerationStyle redundantIfBraces = cs.redundantIfBraces();
             if ((falseExpr != null && redundantIfBraces == CodeStyle.BracesGenerationStyle.ELIMINATE && danglingElseChecker.hasDanglingElse(trueExpr)) ||
                     (redundantIfBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > getStartPos(ifExpr) || endOffset < getEndPos(node)))) {
                 redundantIfBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
             }
             boolean prevblock = wrapStatement(cs.wrapIfStatement(), redundantIfBraces, cs.spaceBeforeIfLeftBrace() ? 1 : 0, trueExpr);
             if (falseExpr != null) {
                 if (cs.placeElseOnNewLine() || !prevblock) {
                     newline();
                 } else {
                     spaces(cs.spaceBeforeElse() ? 1 : 0);
                 }
                 accept(JFXTokenId.ELSE);
                 // TODO special else if
 //                if (falseExpr.getKind() == Tree.Kind.IF && cs.specialElseIf()) {
 //                    space();
 //                    scan(falseExpr, p);
 //                } else {
                     redundantIfBraces = cs.redundantIfBraces();
                     if (redundantIfBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > getStartPos(ifExpr) || endOffset < getEndPos(ifExpr))) {
                         redundantIfBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
                     }
                     wrapStatement(cs.wrapIfStatement(), redundantIfBraces, cs.spaceBeforeElseLeftBrace() ? 1 : 0, falseExpr);
 //                }
                 indent = old;
             }
             return true;
         }
 
         @Override
         public Boolean visitEmptyStatement(EmptyStatementTree node, Void p) {
             accept(JFXTokenId.SEMI);
             return true;
         }
 
         // TODO there is no getExpression(), get it from children of ExpressionTree
 //        @Override
 //        public Boolean visitExpressionStatement(ExpressionTree node, Void p) {
 //            int old = indent;
 //            indent += continuationIndentSize;
 //            scan(node.getExpression(), p);
 //            accept(SEMICOLON);
 //            indent = old;
 //            return true;
 //        }
 
         @Override
         public Boolean visitInstanceOf(InstanceOfTree node, Void p) {
             scan(node.getExpression(), p);
             space();
             accept(JFXTokenId.INSTANCEOF);
             space();
             scan(node.getType(), p);
             return true;
         }
 
         @Override
         public Boolean visitTypeCast(TypeCastTree node, Void p) {
             accept(JFXTokenId.LPAREN);
             boolean spaceWithinParens = cs.spaceWithinTypeCastParens();
             spaces(spaceWithinParens ? 1 : 0);
             scan(node.getType(), p);
             spaces(spaceWithinParens ? 1 : 0);
             accept(JFXTokenId.RPAREN);
             spaces(cs.spaceAfterTypeCast() ? 1 : 0);
             scan(node.getExpression(), p);
             return true;
         }
 
         @Override
         public Boolean visitParenthesized(ParenthesizedTree node, Void p) {
             accept(JFXTokenId.LPAREN);
             boolean spaceWithinParens;
             switch (getCurrentPath().getParentPath().getLeaf().getJavaFXKind()) {
 //                case IF:
 //                    spaceWithinParens = cs.spaceWithinIfParens();
 //                    break;
                 case FOR_EXPRESSION_FOR: // TODO check it
                 case FOR_EXPRESSION_IN_CLAUSE:
                 case FOR_EXPRESSION_PREDICATE:
                     spaceWithinParens = cs.spaceWithinForParens();
                     break;
 //                case DO_WHILE_LOOP:
                 case WHILE_LOOP:
                     spaceWithinParens = cs.spaceWithinWhileParens();
                     break;
 //                case SWITCH:
 //                    spaceWithinParens = cs.spaceWithinSwitchParens();
 //                    break;
 //                case SYNCHRONIZED:
 //                    spaceWithinParens = cs.spaceWithinSynchronizedParens();
 //                    break;
                 default:
                     spaceWithinParens = cs.spaceWithinParens();
             }
             spaces(spaceWithinParens ? 1 : 0);
             scan(node.getExpression(), p);
             spaces(spaceWithinParens ? 1 : 0);
             accept(JFXTokenId.RPAREN);
             return true;
         }
 
         @Override
         public Boolean visitLiteral(LiteralTree node, Void p) {
             do {
                 col += tokens.token().length();
             } while (tokens.moveNext() && tokens.offset() < endPos);
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
             return true;
         }
 
         @Override
         public Boolean visitErroneous(ErroneousTree node, Void p) {
             for (Tree tree : node.getErrorTrees()) {
                 int pos = (int) getStartPos(tree);
                 do {
                     col += tokens.token().length();
                 } while (tokens.moveNext() && tokens.offset() < endPos);
                 lastBlankLines = -1;
                 lastBlankLinesTokenIndex = -1;
                 lastBlankLinesDiff = null;
                 scan(tree, p);
             }
 
             do {
                 col += tokens.token().length();
             } while (tokens.moveNext() && tokens.offset() < endPos);
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
             return true;
         }
 
         private JFXTokenId accept(JFXTokenId first, JFXTokenId... rest) {
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
             EnumSet<JFXTokenId> tokenIds = EnumSet.of(first, rest);
 
             // javafx lexer generates one token for each WS
             // java - one for all
             // therefore StringBuilder is been used instead of Token
             StringBuilder lastWSToken = new StringBuilder(); // TODO do not use text var, use this builder.subSequence
             int after = 0;
             do {
                 if (tokens.offset() >= endPos) {
                     if (lastWSToken.length() != 0) {
                         lastBlankLines = 0;
                         lastBlankLinesTokenIndex = tokens.index() - 1;
                         lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                     }
                     return null;
                 }
                 JFXTokenId id = tokens.token().id();
                 if (tokenIds.contains(id)) {
                     String spaces = after == 1 //after line comment
                             ? getIndent()
                             : after == 2 //after javadoc comment
                             ? getNewlines(1) + getIndent()
                             : null;
                     if (lastWSToken.length() != 0) {
                         if (spaces == null || !spaces.contentEquals(lastWSToken.toString())) {
                             addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                         }
                     } else {
                         if (spaces != null && spaces.length() > 0) {
                             addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                         }
                     }
                     if (after > 0) {
                         col = indent;
                     }
                     col += tokens.token().length();
                     return tokens.moveNext() ? id : null;
                 }
                 switch (id) {
                     case WS:
                         lastWSToken.append(tokens.token().text());
                         break;
                     case LINE_COMMENT:
                         if (lastWSToken.length() != 0) {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : SPACE;
                             if (!spaces.contentEquals(lastWSToken.toString())) {
                                 addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                             }
                             lastWSToken = new StringBuilder();
                         } else {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : null;
                             if (spaces != null && spaces.length() > 0) {
                                 addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                             }
                         }
                         col = 0;
                         after = 1; //line comment
                         break;
                     case DOC_COMMENT:
                         if (lastWSToken.length() != 0) {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : SPACE;
                             if (!spaces.contentEquals(lastWSToken.toString())) {
                                 addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                             }
                             lastWSToken = new StringBuilder();
                             if (after > 0) {
                                 col = indent;
                             } else {
                                 col++;
                             }
                         } else {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : null;
                             if (spaces != null && spaces.length() > 0) {
                                 addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                             }
                             if (after > 0) {
                                 col = indent;
                             }
                         }
                         String tokenText = tokens.token().text().toString();
                         int idx = tokenText.lastIndexOf('\n'); //NOI18N
                         if (idx >= 0) {
                             tokenText = tokenText.substring(idx + 1);
                         }
                         col += getCol(tokenText);
                         indentComment();
                         after = 2; //javadoc comment
                         break;
                     case COMMENT:
                         if (lastWSToken.length() != 0) {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : SPACE;
                             if (!spaces.contentEquals(lastWSToken.toString())) {
                                 addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                             }
                             lastWSToken = new StringBuilder();
                             if (after > 0) {
                                 col = indent;
                             } else {
                                 col++;
                             }
                         } else {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : null;
                             if (spaces != null && spaces.length() > 0) {
                                 addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                             }
                             if (after > 0) {
                                 col = indent;
                             }
                         }
                         tokenText = tokens.token().text().toString();
                         idx = tokenText.lastIndexOf('\n'); //NOI18N
                         if (idx >= 0) {
                             tokenText = tokenText.substring(idx + 1);
                         }
                         col += getCol(tokenText);
                         indentComment();
                         after = 0;
                         break;
                     default:
                         return null;
                 }
             } while (tokens.moveNext());
             return null;
         }
 
         private void space() {
             spaces(1);
         }
 
         private void spaces(int count) {
             spaces(count, false);
         }
 
         private void spaces(int count, boolean preserveNewline) {
             // javafx lexer generates one token for each WS
             // java - one for all
             // therefore StringBuilder is been used instead of Token
             StringBuilder lastWSToken = new StringBuilder(); // TODO do not use text var, use this builder.subSequence
             int after = 0;
             do {
                 if (tokens.offset() >= endPos) {
                     return;
                 }
                 switch (tokens.token().id()) {
                     case WS:
                         lastWSToken.append(tokens.token().text());
                         break;
                     case LINE_COMMENT:
                         if (lastWSToken.length() != 0) {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : SPACE;
                             if (preserveNewline) {
                                 String text = lastWSToken.toString();
                                 int idx = text.lastIndexOf('\n'); //NOI18N
                                 if (idx >= 0) {
                                     spaces = getNewlines(1) + getIndent();
                                     lastBlankLines = 1;
                                     lastBlankLinesTokenIndex = tokens.index();
                                     lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                 }
                             }
                             if (!spaces.contentEquals(lastWSToken.toString())) {
                                 addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                             }
                             lastWSToken = new StringBuilder();
                         } else {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : null;
                             if (spaces != null && spaces.length() > 0) {
                                 addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                             }
                         }
                         col = 0;
                         after = 1; //line comment
                         break;
                     case DOC_COMMENT:
                         if (lastWSToken.length() != 0) {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : SPACE;
                             if (preserveNewline) {
                                 String text = lastWSToken.toString();
                                 int idx = text.lastIndexOf('\n'); //NOI18N
                                 if (idx >= 0) {
                                     spaces = getNewlines(1) + getIndent();
                                     after = 3;
                                     lastBlankLines = 1;
                                     lastBlankLinesTokenIndex = tokens.index();
                                     lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                 }
                             }
                             if (!spaces.contentEquals(lastWSToken.toString())) {
                                 addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                             }
                             lastWSToken = new StringBuilder();
                             if (after > 0) {
                                 col = indent;
                             } else {
                                 col++;
                             }
                         } else {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : null;
                             if (spaces != null && spaces.length() > 0) {
                                 addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                             }
                             if (after > 0) {
                                 col = indent;
                             }
                         }
                         String tokenText = tokens.token().text().toString();
                         int idx = tokenText.lastIndexOf('\n'); //NOI18N
                         if (idx >= 0) {
                             tokenText = tokenText.substring(idx + 1);
                         }
                         col += getCol(tokenText);
                         indentComment();
                         after = 2; //javadoc comment
                         break;
                     case COMMENT:
                         if (lastWSToken.length() != 0) {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : SPACE;
                             if (preserveNewline) {
                                 String text = lastWSToken.toString();
                                 idx = text.lastIndexOf('\n'); //NOI18N
                                 if (idx >= 0) {
                                     spaces = getNewlines(1) + getIndent();
                                     after = 3;
                                     lastBlankLines = 1;
                                     lastBlankLinesTokenIndex = tokens.index();
                                     lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                 }
                             }
                             if (!spaces.contentEquals(lastWSToken.toString())) {
                                 addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                             }
                             lastWSToken = new StringBuilder();
                             if (after > 0) {
                                 col = indent;
                             } else {
                                 col++;
                             }
                         } else {
                             String spaces = after == 1 //after line comment
                                     ? getIndent()
                                     : after == 2 //after javadoc comment
                                     ? getNewlines(1) + getIndent()
                                     : null;
                             if (spaces != null && spaces.length() > 0) {
                                 addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                             }
                             if (after > 0) {
                                 col = indent;
                             }
                         }
                         tokenText = tokens.token().text().toString();
                         idx = tokenText.lastIndexOf('\n'); //NOI18N
                         if (idx >= 0) {
                             tokenText = tokenText.substring(idx + 1);
                         }
                         col += getCol(tokenText);
                         indentComment();
                         after = 0;
                         break;
                     default:
                         String spaces = after == 1 //after line comment
                                 ? getIndent()
                                 : after == 2 //after javadoc comment
                                 ? getNewlines(1) + getIndent()
                                 : getSpaces(count);
                         if (lastWSToken.length() != 0) {
                             if (preserveNewline) {
                                 String text = lastWSToken.toString();
                                 idx = text.lastIndexOf('\n'); //NOI18N
                                 if (idx >= 0) {
                                     spaces = getNewlines(1) + getIndent();
                                     after = 3;
                                     lastBlankLines = 1;
                                     lastBlankLinesTokenIndex = tokens.index();
                                     lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                                 }
                             }
                             if (!spaces.contentEquals(lastWSToken.toString())) {
                                 addDiff(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                             }
                         } else if (spaces.length() > 0) {
                             addDiff(new Diff(tokens.offset(), tokens.offset(), spaces));
                         }
                         if (after > 0) {
                             col = indent;
                         } else {
                             col += count;
                         }
                         return;
                 }
             } while (tokens.moveNext());
         }
 
         private void newline() {
             blankLines(templateEdit ? ANY_COUNT : 0);
         }
 
         private void blankLines() {
             blankLines(ANY_COUNT);
         }
 
         private void blankLines(int count) {
             if (count >= 0) {
                 if (lastBlankLinesTokenIndex < 0) {
                     lastBlankLines = count;
                     lastBlankLinesTokenIndex = tokens.index();
                     lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                 } else if (lastBlankLines < count) {
                     lastBlankLines = count;
                     rollback(lastBlankLinesTokenIndex, lastBlankLinesTokenIndex, lastBlankLinesDiff);
                 } else {
                     return;
                 }
             } else {
                 if (lastBlankLinesTokenIndex < 0) {
                     lastBlankLinesTokenIndex = tokens.index();
                     lastBlankLinesDiff = diffs.isEmpty() ? null : diffs.getFirst();
                 } else {
                     return;
                 }
             }
 
             // javafx lexer generates one token for each WS
             // java - one for all
             // therefore StringBuilder is been used instead of Token
             StringBuilder lastToken = new StringBuilder(); // TODO do not use text var, use this builder.subSequence
             int after = 0;
             do {
                 if (tokens.offset() >= endPos) {
                     return;
                 }
                 switch (tokens.token().id()) {
                     case WS:
                         lastToken.append(tokens.token().text());
                         break;
                     case COMMENT:
                         if (count >= 0 && tokens.index() > 1 && after != 1) {
                             count++;
                         }
                         if (lastToken.length() != 0) {
                             int offset = tokens.offset() - lastToken.length();
                             String text = lastToken.toString();
                             int idx = 0;
                             int lastIdx = 0;
                             while (count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                 if (idx > lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                 }
                                 lastIdx = idx + 1;
                                 count--;
                             }
                             if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                 if (idx > lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                 }
                                 lastIdx = idx + 1;
                             }
                             if (lastIdx > 0) {
                                 String _indent = getIndent();
                                 if (!_indent.contentEquals(text.substring(lastIdx))) {
                                     addDiff(new Diff(offset + lastIdx, tokens.offset(), _indent));
                                 }
                             }
                             lastToken = new StringBuilder();
                         }
                         indentComment();
                         after = 3;
                         break;
                     case DOC_COMMENT:
                         if (count >= 0 && tokens.index() > 1 && after != 1) {
                             count++;
                         }
                         if (lastToken.length() != 0) {
                             int offset = tokens.offset() - lastToken.length();
                             String text = lastToken.toString();
                             int idx = 0;
                             int lastIdx = 0;
                             while (count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                 if (idx > lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                 }
                                 lastIdx = idx + 1;
                                 count--;
                             }
                             if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                 after = 0;
                                 if (idx >= lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                 }
                                 lastIdx = idx + 1;
                             }
                             if (lastIdx == 0 && count < 0 && after != 1) {
                                 count = count == ANY_COUNT ? 1 : 0;
                             }
                             String _indent = after == 3 ? SPACE : getNewlines(count) + getIndent();
                             if (!_indent.contentEquals(text.substring(lastIdx))) {
                                 addDiff(new Diff(offset + lastIdx, tokens.offset(), _indent));
                             }
                             lastToken = new StringBuilder();
                         } else {
                             if (lastBlankLines < 0 && count == ANY_COUNT) {
                                 count = lastBlankLines = 1;
                             }
                             String text = getNewlines(count) + getIndent();
                             if (text.length() > 0) {
                                 addDiff(new Diff(tokens.offset(), tokens.offset(), text));
                             }
                         }
                         indentComment();
                         count = 0;
                         after = 2;
                         break;
                     case LINE_COMMENT:
                         if (lastToken.length() != 0) {
                             int offset = tokens.offset() - lastToken.length();
                             String text = lastToken.toString();
                             if (count >= 0 && tokens.index() > 1 && after != 1 && text.indexOf('\n') >= 0) {
                                 count++;
                             }
                             int idx = 0;
                             int lastIdx = 0;
                             while (count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                 if (idx > lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                 }
                                 lastIdx = idx + 1;
                                 count--;
                             }
                             if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                 if (idx >= lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                 }
                                 lastIdx = idx + 1;
                             }
                             if (lastIdx == 0 && after == 1) {
                                 String _indent = getIndent();
                                 if (!_indent.contentEquals(text)) {
                                     addDiff(new Diff(offset, tokens.offset(), _indent));
                                 }
                             } else if (lastIdx > 0 && lastIdx < lastToken.length()) {
                                 String _indent = getIndent();
                                 if (!_indent.contentEquals(text.substring(lastIdx))) {
                                     addDiff(new Diff(offset + lastIdx, tokens.offset(), _indent));
                                 }
                             }
                             lastToken = new StringBuilder();
                         }
                         after = 1;
                         break;
                     default:
                         if (count >= 0 && tokens.index() > 1 && after != 1) {
                             count++;
                         }
                         if (lastToken.length() != 0) {
                             int offset = tokens.offset() - lastToken.length();
                             String text = lastToken.toString();
                             int idx = 0;
                             int lastIdx = 0;
                             while (count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                 if (idx > lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx, templateEdit ? getIndent() : null));
                                 }
                                 lastIdx = idx + 1;
                                 count--;
                             }
                             if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                 after = 0;
                                 if (idx >= lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx + 1, null));
                                 }
                                 lastIdx = idx + 1;
                             }
                             if (lastIdx == 0 && count < 0 && after != 1) {
                                 count = count == ANY_COUNT ? 1 : 0;
                             }
                             String _indent = after == 3 ? SPACE : getNewlines(count) + getIndent();
                             if (!_indent.contentEquals(text.substring(lastIdx))) {
                                 addDiff(new Diff(offset + lastIdx, tokens.offset(), _indent));
                             }
                         } else {
                             if (lastBlankLines < 0 && count == ANY_COUNT) {
                                 count = lastBlankLines = 1;
                             }
                             String text = after == 1 ? getIndent() : getNewlines(count) + getIndent();
                             if (text.length() > 0) {
                                 addDiff(new Diff(tokens.offset(), tokens.offset(), text));
                             }
                         }
                         col = indent;
                         return;
                 }
             } while (tokens.moveNext());
         }
 
         private void rollback(int index, int col, Diff diff) {
             tokens.moveIndex(index);
             tokens.moveNext();
             if (diff == null) {
                 diffs.clear();
             } else {
                 while (!diffs.isEmpty() && diffs.getFirst() != diff) {
                     diffs.removeFirst();
                 }
             }
             this.col = col;
         }
 
         private void appendToDiff(String s) {
             int offset = tokens.offset();
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             if (d != null && d.getEndOffset() == offset) {
                 d.text += s;
             } else {
                 addDiff(new Diff(offset, offset, s));
             }
         }
 
         private void addDiff(Diff diff) {
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             if (d == null || d.getStartOffset() <= diff.getStartOffset()) {
                 diffs.addFirst(diff);
             }
         }
 
         private int wrapToken(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCnt, JFXTokenId first, JFXTokenId... rest) {
             int ret = -1;
             switch (wrapStyle) {
                 case WRAP_ALWAYS:
                     int old = indent;
                     if (alignIndent >= 0) {
                         indent = alignIndent;
                     }
                     newline();
                     indent = old;
                     ret = col;
                     accept(first, rest);
                     break;
                 case WRAP_IF_LONG:
                     int index = tokens.index();
                     int c = col;
                     Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                     old = indent;
                     if (alignIndent >= 0) {
                         indent = alignIndent;
                     }
                     spaces(spacesCnt, true);
                     indent = old;
                     ret = col;
                     accept(first, rest);
                     if (this.col > rightMargin) {
                         rollback(index, c, d);
                         old = indent;
                         if (alignIndent >= 0) {
                             indent = alignIndent;
                         }
                         newline();
                         indent = old;
                         ret = col;
                         accept(first, rest);
                     }
                     break;
                 case WRAP_NEVER:
                     old = indent;
                     if (alignIndent >= 0) {
                         indent = alignIndent;
                     }
                     spaces(spacesCnt, true);
                     indent = old;
                     ret = col;
                     accept(first, rest);
                     break;
             }
             return ret;
         }
 
         private int wrapTree(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCnt, Tree tree) {
             int ret = -1;
             switch (wrapStyle) {
                 case WRAP_ALWAYS:
                     int old = indent;
                     if (alignIndent >= 0) {
                         indent = alignIndent;
                     }
                     newline();
                     indent = old;
                     ret = col;
                     scan(tree, null);
                     break;
                 case WRAP_IF_LONG:
                     int index = tokens.index();
                     int c = col;
                     Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                     old = indent;
                     if (alignIndent >= 0) {
                         indent = alignIndent;
                     }
                     spaces(spacesCnt, true);
                     indent = old;
                     ret = col;
                     wrapDepth++;
                     scan(tree, null);
                     wrapDepth--;
                     if (col > rightMargin && (wrapDepth == 0 || c <= rightMargin)) {
                         rollback(index, c, d);
                         old = indent;
                         if (alignIndent >= 0) {
                             indent = alignIndent;
                         }
                         newline();
                         indent = old;
                         ret = col;
                         scan(tree, null);
                     }
                     break;
                 case WRAP_NEVER:
                     old = indent;
                     if (alignIndent >= 0) {
                         indent = alignIndent;
                     }
                     spaces(spacesCnt, true);
                     indent = old;
                     ret = col;
                     scan(tree, null);
                     break;
             }
             return ret;
         }
 
         private int wrapOperatorAndTree(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCnt, Tree tree) {
             int ret = -1;
             switch (wrapStyle) {
                 case WRAP_ALWAYS:
                     int old = indent;
                     if (alignIndent >= 0) {
                         indent = alignIndent;
                     }
                     newline();
                     indent = old;
                     ret = col;
                     if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                         col += tokens.token().length();
                         lastBlankLines = -1;
                         lastBlankLinesTokenIndex = -1;
                         tokens.moveNext();
                     }
                     spaces(spacesCnt);
                     scan(tree, null);
                     break;
                 case WRAP_IF_LONG:
                     int index = tokens.index();
                     int c = col;
                     Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                     old = indent;
                     if (alignIndent >= 0) {
                         indent = alignIndent;
                     }
                     spaces(spacesCnt, true);
                     indent = old;
                     ret = col;
                     wrapDepth++;
                     if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                         col += tokens.token().length();
                         lastBlankLines = -1;
                         lastBlankLinesTokenIndex = -1;
                         tokens.moveNext();
                     }
                     spaces(spacesCnt);
                     scan(tree, null);
                     wrapDepth--;
                     if (col > rightMargin && (wrapDepth == 0 || c <= rightMargin)) {
                         rollback(index, c, d);
                         old = indent;
                         if (alignIndent >= 0) {
                             indent = alignIndent;
                         }
                         newline();
                         indent = old;
                         ret = col;
                         if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                             col += tokens.token().length();
                             lastBlankLines = -1;
                             lastBlankLinesTokenIndex = -1;
                             tokens.moveNext();
                         }
                         spaces(spacesCnt);
                         scan(tree, null);
                     }
                     break;
                 case WRAP_NEVER:
                     old = indent;
                     if (alignIndent >= 0) {
                         indent = alignIndent;
                     }
                     spaces(spacesCnt, true);
                     indent = old;
                     ret = col;
                     if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                         col += tokens.token().length();
                         lastBlankLines = -1;
                         lastBlankLinesTokenIndex = -1;
                         tokens.moveNext();
                     }
                     spaces(spacesCnt);
                     scan(tree, null);
                     break;
             }
             return ret;
         }
 
         private boolean wrapStatement(CodeStyle.WrapStyle wrapStyle, CodeStyle.BracesGenerationStyle bracesGenerationStyle, int spacesCnt, ExpressionTree tree) {
             if (tree.getJavaFXKind() == JavaFXKind.EMPTY_STATEMENT) {
                 scan(tree, null);
                 return true;
             }
             if (tree.getJavaFXKind() == JavaFXKind.BLOCK_EXPRESSION) {
                 if (bracesGenerationStyle == CodeStyle.BracesGenerationStyle.ELIMINATE) {
                     Iterator<? extends ExpressionTree> stats = ((BlockExpressionTree) tree).getStatements().iterator();
                     if (stats.hasNext()) {
                         ExpressionTree stat = stats.next();
                         if (!stats.hasNext() && stat.getJavaFXKind() != JavaFXKind.VARIABLE) {
                             int start = tokens.offset();
                             accept(JFXTokenId.LBRACE);
                             Diff d;
                             while (!diffs.isEmpty() && (d = diffs.getFirst()) != null && d.getStartOffset() >= start) {
                                 diffs.removeFirst();
                             }
                             addDiff(new Diff(start, tokens.offset(), null));
                             int old = indent;
                             indent += indentSize;
                             wrapTree(wrapStyle, -1, spacesCnt, stat);
                             indent = old;
                             start = tokens.offset();
                             accept(JFXTokenId.RBRACE);
                             while (!diffs.isEmpty() && (d = diffs.getFirst()) != null && d.getStartOffset() >= start) {
                                 diffs.removeFirst();
                             }
                             addDiff(new Diff(start, tokens.offset(), null));
                             return false;
                         }
                     }
                 }
                 scan(tree, null);
                 return true;
             }
             if (bracesGenerationStyle == CodeStyle.BracesGenerationStyle.GENERATE) {
                 scan(new FakeBlock(tree), null);
                 return true;
             }
             int old = indent;
             indent += indentSize;
             int ret = wrapTree(wrapStyle, -1, spacesCnt, tree);
             indent = old;
             return false;
         }
 
         private void wrapList(CodeStyle.WrapStyle wrapStyle, boolean align, boolean prependSpace, List<? extends Tree> trees) {
             boolean first = true;
             int alignIndent = -1;
             for (Iterator<? extends Tree> it = trees.iterator(); it.hasNext();) {
                 Tree impl = it.next();
                 if (impl.getJavaFXKind() == JavaFXKind.ERRONEOUS) {
                     scan(impl, null);
                 } else if (first) {
                     int index = tokens.index();
                     int c = col;
                     Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                     if (prependSpace) {
                         spaces(1, true);
                     }
                     if (align) {
                         alignIndent = col;
                     }
                     scan(impl, null);
                     if (wrapStyle != CodeStyle.WrapStyle.WRAP_NEVER && col > rightMargin && c > indent && (wrapDepth == 0 || c <= rightMargin)) {
                         rollback(index, c, d);
                         newline();
                         scan(impl, null);
                     }
                 } else {
                     wrapTree(wrapStyle, alignIndent, cs.spaceAfterComma() ? 1 : 0, impl);
                 }
                 first = false;
                 if (it.hasNext()) {
                     spaces(cs.spaceBeforeComma() ? 1 : 0);
                     accept(JFXTokenId.COMMA);
                 }
             }
         }
 
         private void indentComment() {
             if (tokens.token().id() != JFXTokenId.COMMENT && tokens.token().id() != JFXTokenId.DOC_COMMENT) {
                 return;
             }
             String _indent = getIndent();
             String text = tokens.token().text().toString();
             int idx = 0;
             while ((idx = text.indexOf('\n', idx)) >= 0) { //NOI18N
                 int i = idx + 1;
                 while (i < text.length() && text.charAt(i) <= ' ' && text.charAt(i) != '\n') //NOI18N
                 {
                     i++;
                 }
                 if (i >= text.length()) {
                     break;
                 }
                 String s = text.charAt(i) == '*' ? _indent + SPACE : _indent;
                 if (!s.equals(text.substring(idx + 1, i))) {
                     addDiff(new Diff(tokens.offset() + idx + 1, tokens.offset() + i, s)); //NOI18N
                 }
                 idx = i;
             }
         }
 
         private String getSpaces(int count) {
             if (count <= 0) {
                 return EMPTY;
             }
             if (count == 1) {
                 return SPACE;
             }
             StringBuilder sb = new StringBuilder();
             while (count-- > 0) {
                 sb.append(' '); //NOI18N
             }
             return sb.toString();
         }
 
         private String getNewlines(int count) {
             if (count <= 0) {
                 return EMPTY;
             }
             if (count == 1) {
                 return NEWLINE;
             }
             StringBuilder sb = new StringBuilder();
             while (count-- > 0) {
                 sb.append('\n'); //NOI18N
             }
             return sb.toString();
         }
 
         private String getIndent() {
             StringBuilder sb = new StringBuilder();
             int _col = 0;
             if (!expandTabToSpaces) {
                 while (_col + tabSize <= indent) {
                     sb.append('\t'); //NOI18N
                     _col += tabSize;
                 }
             }
             while (_col < indent) {
                 sb.append(SPACE); //NOI18N
                 _col++;
             }
             return sb.toString();
         }
 
         private int getIndentLevel(TokenSequence<JFXTokenId> tokens, JavaFXTreePath path) {
             if (path.getLeaf().getJavaFXKind() == JavaFXKind.COMPILATION_UNIT) {
                 return 0;
             }
             Tree lastTree = null;
             int _indent = -1;
             while (path != null) {
                 int offset = (int) sp.getStartPosition(path.getCompilationUnit(), path.getLeaf());
                 if (offset < 0) {
                     return _indent;
                 }
                 tokens.move(offset);
                 String text = null;
                 while (tokens.movePrevious()) {
                     Token<JFXTokenId> token = tokens.token();
                     if (token.id() == JFXTokenId.WS) {
                         text = token.text().toString();
                         int idx = text.lastIndexOf('\n');
                         if (idx >= 0) {
                             text = text.substring(idx + 1);
                             _indent = getCol(text);
                             break;
                         }
                     } else if (token.id() == JFXTokenId.LINE_COMMENT) {
                         _indent = text != null ? getCol(text) : 0;
                         break;
                     } else if (token.id() == JFXTokenId.COMMENT || token.id() == JFXTokenId.DOC_COMMENT) {
                         text = null;
                     } else {
                         break;
                     }
                 }
                 if (_indent >= 0) {
                     break;
                 }
                 lastTree = path.getLeaf();
                 path = path.getParentPath();
             }
             if (lastTree != null && path != null) {
                 switch (path.getLeaf().getJavaFXKind()) {
                     case CLASS_DECLARATION:
                         for (Tree tree : ((ClassDeclarationTree) path.getLeaf()).getClassMembers()) {
                             if (tree == lastTree) {
                                 _indent += tabSize;
                                 break;
                             }
                         }
                         break;
                     case BLOCK_EXPRESSION:
                         for (Tree tree : ((BlockExpressionTree) path.getLeaf()).getStatements()) {
                             if (tree == lastTree) {
                                 _indent += tabSize;
                                 break;
                             }
                         }
                         break;
                 }
             }
             return _indent;
         }
 
         private int getCol(String text) {
             int _col = 0;
             for (int i = 0; i < text.length(); i++) {
                 char c = text.charAt(i);
                 if (c == '\t') {
                     _col += tabSize;
                     _col -= (_col % tabSize);
                 } else {
                     _col++;
                 }
             }
             return _col;
         }
 
         private boolean isSynthetic(JFXTree node) {
             if (node instanceof BlockExpressionTree) {
                 JavaFXTreePath pp = getCurrentPath().getParentPath();
                 if (pp != null && pp instanceof FunctionValueTree) {
                     pp = pp.getParentPath();
                     JFXTree tree = (JFXTree) pp.getLeaf();
                     if (tree instanceof FunctionDefinitionTree) {
                         return synthetic(tree);
                     }
                 }
             }
             return synthetic(node);
         }
 
         private boolean synthetic(JFXTree node) {
             return node.getGenType() == SyntheticTree.SynthType.SYNTHETIC || getStartPos(node) == getEndPos(node);
         }
 
         private long getEndPos(Tree node) {
             return sp.getEndPosition(root, node);
         }
 
         private long getStartPos(Tree node) {
             return sp.getStartPosition(root, node);
         }
 
         private static class FakeBlock extends JFXBlock {
             private ExpressionTree stat;
 
             private FakeBlock(ExpressionTree stat) {
                 super(0L, com.sun.tools.javac.util.List.of((JFXExpression) stat), (JFXExpression) stat);
                 this.stat = stat;
             }
         }
 
         private static class DanglingElseChecker extends SimpleJavaFXTreeVisitor<Void, Void> {
 
             private boolean foundDanglingElse;
 
             public boolean hasDanglingElse(Tree t) {
                 if (t == null)
                     return false;
                 foundDanglingElse = false;
                 visit(t, null);
                 return foundDanglingElse;
             }
 
             @Override
             public Void visitBlockExpression(BlockExpressionTree node, Void p) {
                 // Do dangling else checks on single statement blocks since
                 // they often get eliminated and replaced by their constained statement
                 Iterator<? extends ExpressionTree> it = node.getStatements().iterator();
                 ExpressionTree stat = it.hasNext() ? it.next() : null;
                 if (stat != null && !it.hasNext()) {
                     visit(stat, p);
                 }
                 return null;
             }
 
             @Override
             public Void visitForExpressionInClause(ForExpressionInClauseTree node, Void p) {
                 return visit(node.getWhereExpression(), p);
             }
 
             @Override
             public Void visitConditionalExpression(ConditionalExpressionTree node, Void p) {
                 if (node.getFalseExpression() == null)
                     foundDanglingElse = true;
                 else
                     visit(node.getFalseExpression(), p);
                 return null;
             }
 
             @Override
             public Void visitWhileLoop(WhileLoopTree node, Void p) {
                 return visit(node.getStatement(), p);
             }
         }
     }
 
     private static class Diff {
         private int start;
         private int end;
         private String text;
 
         private Diff(int start, int end, String text) {
             this.start = start;
             this.end = end;
             this.text = text;
         }
 
         public int getStartOffset() {
             return start;
         }
 
         public int getEndOffset() {
             return end;
         }
 
         public String getText() {
             return text;
         }
 
         @Override
         public String toString() {
             return "Diff<" + start + "," + end + ">:" + text; //NOI18N
         }
     }
 
 }
