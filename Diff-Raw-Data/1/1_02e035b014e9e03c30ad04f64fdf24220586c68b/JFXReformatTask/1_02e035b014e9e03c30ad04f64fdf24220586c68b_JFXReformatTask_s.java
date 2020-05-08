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
 
 import com.sun.javafx.api.JavafxBindStatus;
 import com.sun.javafx.api.tree.*;
 import com.sun.javafx.api.tree.Tree.JavaFXKind;
 import com.sun.javafx.api.tree.UnitTree;
 import com.sun.tools.javafx.tree.*;
 import java.io.*;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.lang.model.element.Name;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import org.netbeans.api.javafx.editor.FXSourceUtils;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.*;
 import org.netbeans.api.lexer.*;
 import org.netbeans.modules.editor.indent.spi.Context;
 import org.netbeans.modules.editor.indent.spi.ExtraLock;
 import org.netbeans.modules.editor.indent.spi.ReformatTask;
 import org.netbeans.modules.javafx.editor.format.CodeStyle.WrapStyle;
 import org.netbeans.spi.lexer.MutableTextInput;
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataObject;
 
 
 /**
  * This code based on org.netbeans.modules.java.source.save.Reformatter written by Dusan Balek.
  *
  * @see org.netbeans.modules.java.source.save.Reformatter
  * @see http://openjfx.java.sun.com/current-build/doc/reference/JavaFXReference.html
  * @see http://wikis.sun.com/display/JavaFxCodeConv/Home
  * @author Anton Chechel
  */
 public class JFXReformatTask implements ReformatTask {
 
     private static final Object CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N
     private static final String NEWLINE = "\n"; //NOI18N
     private static final String LCBRACE = "{"; //NOI18N
     private static final String RCBRACE = "}"; //NOI18N
     private static final String MAGIC_FUNCTION = "javafx$run$"; //NOI18N
 
     private static Logger log = Logger.getLogger(JFXReformatTask.class.getName());
 
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
 
     // to be invoked from formatting settings
     // TODO optimize it and refactor
     public static String reformat(final String text, final CodeStyle style) {
         final StringBuilder sb = new StringBuilder(text);
         try {
             final File file = FileUtil.normalizeFile(File.createTempFile("format", ".fx")); // NOI18N
             FileOutputStream os = null;
             InputStream is = null;
             try {
                 os = new FileOutputStream(file);
                 is = new ByteArrayInputStream(text.getBytes("UTF-8")); // NOI18N
                 FileUtil.copy(is, os);
             } finally {
                 if (os != null) {
                     os.close();
                 }
                 if (is != null) {
                     is.close();
                 }
             }
 
             FileObject fObj = FileUtil.toFileObject(file);
             DataObject dObj = DataObject.find(fObj);
             EditorCookie ec = (EditorCookie) dObj.getCookie(EditorCookie.class);
             Document doc = ec.openDocument();
             doc.putProperty(Language.class, JFXTokenId.language());
             doc.putProperty("mimeType", FXSourceUtils.MIME_TYPE); // NOI18N
 
             JavaFXSource src = JavaFXSource.forDocument(doc);
             src.runUserActionTask(new Task<CompilationController>() {
                 public void run(CompilationController controller) throws Exception {
                     if (controller != null && !controller.toPhase(JavaFXSource.Phase.PARSED).lessThan(JavaFXSource.Phase.PARSED)) {
                         TokenSequence<JFXTokenId> tokens = TokenHierarchy.create(text, JFXTokenId.language()).tokenSequence(JFXTokenId.language());
                         UnitTree tree = controller.getCompilationUnit();
                         for (Diff diff : Pretty.reformat(controller, text, tokens, new JavaFXTreePath(tree), style)) {
                             int start = diff.getStartOffset();
                             int end = diff.getEndOffset();
                             sb.delete(start, end);
                             String t = diff.getText();
                             if (t != null && t.length() > 0) {
                                 sb.insert(start, t);
                             }
                         }
                     }
                 }
             }, true);
         } catch (Exception ex) {
             if (log.isLoggable(Level.FINE)) {
                 log.log(Level.FINE, "exception occured", ex); // NOI18N
             }
         }
         return sb.toString();
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
         JavaFXTreePath path = getCommonPath(controller, startOffset);
         if (path == null) {
             return;
         }
 
         // #180620
         MutableTextInput<? extends Document> mti = (MutableTextInput<? extends Document>) doc.getProperty(MutableTextInput.class);
         if (mti != null) {
             mti.tokenHierarchyControl().setActive(false);
         }
         try {
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
                 if (endOffset == start && (text == null || !text.trim().equals(RCBRACE))) {
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
                                 int idx = t.lastIndexOf(NEWLINE);
                                 if (idx >= 0) {
                                     t = t.substring(idx + 1);
                                     idx = text.lastIndexOf(NEWLINE);
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
                                 while ((idx1 = t.indexOf(NEWLINE, lastIdx1)) >= 0 && (idx2 = text.indexOf(NEWLINE, lastIdx2)) >= 0) {
                                     lastIdx1 = idx1 + 1;
                                     lastIdx2 = idx2 + 1;
                                 }
                                 if ((idx2 = text.lastIndexOf(NEWLINE)) >= 0 && idx2 >= lastIdx2) {
                                     if (lastIdx1 == 0) {
                                         t = null;
                                     } else {
                                         text = text.substring(idx2 + 1);
                                         t = t.substring(lastIdx1);
                                     }
                                 } else if ((idx1 = t.lastIndexOf(NEWLINE)) >= 0 && idx1 >= lastIdx1) {
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
                                 while ((idx1 = t.lastIndexOf(NEWLINE)) >= 0 && (idx2 = text.lastIndexOf(NEWLINE)) >= 0) {
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
         } finally {
             if (mti != null) {
                 mti.tokenHierarchyControl().setActive(true);
             }
         }
         shift = region.getEndOffset() - originalEndOffset;
         return;
     }
 
     public ExtraLock reformatLock() {
         return JavaFXReformatExtraLock.getInstance();
     }
 
     private static JavaFXTreePath getCommonPath(final CompilationController controller, final int offset) {
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
 
     private static class TreePosComparator implements Comparator<Tree> {
         private final SourcePositions sp;
         private final UnitTree ut;
 
         public TreePosComparator(SourcePositions sp, UnitTree ut) {
             this.sp = sp;
             this.ut = ut;
         }
 
         public int compare(Tree t1, Tree t2) {
             long t1p = sp.getStartPosition(ut, t1);
             long t2p = sp.getStartPosition(ut, t2);
             if (t1p == t2p) {
                 t1p = sp.getEndPosition(ut, t1);
                 t2p = sp.getEndPosition(ut, t2);
             }
             if (t1p == t2p) {
                 t1p = t1.hashCode();
                 t2p = t2.hashCode();
             }
             return (t1p < t2p ? -1 : (t1p == t2p ? 0 : 1));
 
 //            long diff = sp.getStartPosition(ut, t2) - sp.getStartPosition(ut, t1);
 //            if (diff == 0) {
 //                diff = sp.getEndPosition(ut, t1) - sp.getEndPosition(ut, t2);
 //            }
 //            return (int) diff;
         }
     }
 
     /*
      * This scanner places trees in correct order how they presented actually in the source code.
      * @see JFXC-3633
      */
     private static class FilterScanner<R,P> extends JavaFXTreePathScanner<R, P> {
         private CompilationInfo ci;
         private TreeSet<Tree> displaced;
 
         public FilterScanner(final CompilationInfo ci) {
             this.ci = ci;
             displaced = new TreeSet<Tree>(new TreePosComparator(ci.getTrees().getSourcePositions(), ci.getCompilationUnit()));
             ci.getCompilationUnit().accept(new JavaFXTreeScanner<Void, Void>() {
                 int state = 0;
 
                 public @Override Void scan(Tree node, Void p) {
                     if (state == 4) {// FUNCTION_DEFINITION -> FUNCTION_VALUE -> BLOCK_EXPRESSION
                         state = 0;
                         addTree(node);
                         super.scan(node, p);
                         state = 4;
                         return null;
                     } else {
                         // or CLASS_DECLARATION->VARIABLE(static)
                         // or CLASS_DECLARATION->FUNCTION_DEFINITION(static)
                         return super.scan(node, p);
                     }
                 }
 
                 public @Override Void visitClassDeclaration(ClassDeclarationTree node, Void p) {
                     int oldState = state;
                     state = 1;
                     super.visitClassDeclaration(node, p);
                     state = oldState;
                     return null;
                 }
 
                 // TODO get "static" via flags when they will work in compiler
                 public @Override Void visitVariable(VariableTree node, Void p) {
                     if (state == 1 && node.getModifiers().toString().contains("static")) { // NOI18N
                         addTree(node);
                     }
                     return super.visitVariable(node, p);
                 }
 
                 // TODO get "static" via flags when they will work in compiler
                 public @Override Void visitFunctionDefinition(FunctionDefinitionTree node, Void p) {
                     JFXFunctionDefinition funcDef = (JFXFunctionDefinition) node;
                     boolean magicFunc = MAGIC_FUNCTION.contentEquals(funcDef.getName()) && isSynthetic(funcDef);
                     int oldState = state;
 
                     if (state == 1) {
                         if (magicFunc) {
                             state = 2;
                         } else if (node.getModifiers().toString().contains("static")) { // NOI18N
                             addTree(node);
                         }
                     }
                     super.visitFunctionDefinition(node, p);
                     state = oldState;
                     return null;
                 }
 
                 public @Override Void visitFunctionValue(FunctionValueTree node, Void p) {
                     int oldState = state;
                     if (state == 2) {
                         state = 3;
                     }
                     super.visitFunctionValue(node, p);
                     state = oldState;
                     return null;
                 }
 
                 public @Override Void visitBlockExpression(BlockExpressionTree node, Void p) {
                     int oldState = state;
                     if (state == 3) {
                         state = 4;
                     }
                     super.visitBlockExpression(node, p);
                     state = oldState;
                     return null;
                 }
 
                 private void addTree(Tree tree) {
                     if (!isSynthetic(tree)) {
                         displaced.add(tree);
                     }
                 }
 
             }, null);
         }
 
 //        public @Override R scan(Tree tree, P p) {
 //            if (displaced.contains(tree)) {
 //                return null;
 //            } else {
 //                return super.scan(tree, p);
 //            }
 //        }
 //
 //        public R myScan(Tree tree, P p) {
 //            return super.scan(tree, p);
 //        }
 //
 //        private R myScan(Iterable<? extends Tree> nodes, P p) {
 //            R r = null;
 //            if (nodes != null) {
 //                boolean first = true;
 //                for (Tree node : nodes) {
 //                    r = (first ? super.scan(node, p) : reduce(super.scan(nodes, p), r));
 //                    first = false;
 //                }
 //            }
 //            return r;
 //        }
 
         SortedSet<Tree> getCUTrees(UnitTree node) {
             TreeSet<Tree> cuTrees = (TreeSet<Tree>) displaced.clone();
 //            toNotify.add(node.getPackageName());
             cuTrees.addAll(node.getImports());
             
             final List<? extends Tree> typeDecls = node.getTypeDecls();
             Tree topLevelClass = null;
             for (Tree tree : typeDecls) {
                 // assume FXScript has only 1 top level class and it's named as a file (nice FX feature)
                 if (tree.getJavaFXKind() == JavaFXKind.CLASS_DECLARATION) {
                     topLevelClass = tree;
                     if (!isSynthetic(tree)) {
                         cuTrees.add(tree);
                     }
                 }
             }
             // other class declarations are children of top level (nice FX feature)
             if (topLevelClass != null) {
                 List<Tree> members = ((ClassDeclarationTree) topLevelClass).getClassMembers();
                 for (Tree member : members) {
                     if (member.getJavaFXKind() == JavaFXKind.CLASS_DECLARATION) {
                         cuTrees.add(member);
                     }
                 }
             }
 
             return cuTrees;
         }
 
         private boolean isSynthetic(Tree node) {
             if (node instanceof BlockExpressionTree) {
                 UnitTree cu = ci.getCompilationUnit();
                 final JavaFXTreePath path = ci.getTrees().getPath(cu, node);
                 if (path == null) {
                     return true;
                 }
                 JavaFXTreePath pp = path.getParentPath();
                 if (pp != null && pp instanceof FunctionValueTree) {
                     pp = pp.getParentPath();
                     JFXTree tree = (JFXTree) pp.getLeaf();
                     if (tree instanceof FunctionDefinitionTree) {
                         return synthetic(tree);
                     }
                 }
             }
             return synthetic((JFXTree) node);
         }
 
         private boolean synthetic(JFXTree node) {
             SourcePositions sp = ci.getTrees().getSourcePositions();
             UnitTree cu = ci.getCompilationUnit();
             long startPos = sp.getStartPosition(cu, node);
             long endPos = sp.getEndPosition(cu, node);
             return node.getGenType() == SyntheticTree.SynthType.SYNTHETIC || startPos == endPos;
         }
 
 //        public @Override R visitCompilationUnit(UnitTree node, P p) {
 //            SortedSet<Tree> toNotify = getCUTrees(node);
 //            return myScan(toNotify, p);
 //        }
     }
 
     private static class Pretty extends FilterScanner<Boolean, Void> {
 //    private static class Pretty extends JavaFXTreePathScanner<Boolean, Void> {
 
         private static final String OPERATOR = "operator"; // NOI18N
         private static final String EMPTY = ""; // NOI18N
         private static final String SPACE = " "; // NOI18N
         private static final String ERROR = "<error>"; // NOI18N
         private static final String SEMI = ";"; // NOI18N
         private static final String WS_TEMPLATE = "\\s+"; // NOI18N
         private static final int ANY_COUNT = -1;
 
 //        private final Document doc;
         private final String fText;
         private final CodeStyle cs;
         private final SourcePositions sp;
         private final UnitTree root;
 
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
         private int startOffset;
         private int endOffset;
 
         private SortedSet<Tree> cuTrees;
 
         private Pretty(CompilationInfo info, JavaFXTreePath path, CodeStyle cs, int startOffset, int endOffset, boolean templateEdit) {
             this(info, FXSourceUtils.getText(info), info.getTokenHierarchy().tokenSequence(JFXTokenId.language()),
                     path, cs, startOffset, endOffset);
             this.templateEdit = templateEdit;
         }
 
         private Pretty(CompilationInfo info, String text, TokenSequence<JFXTokenId> tokens, JavaFXTreePath path, CodeStyle cs, int startOffset, int endOffset) {
             super(info);
 //            this.doc = info.getDocument();
             this.root = path.getCompilationUnit();
             this.fText = text;
             this.sp = info.getTrees().getSourcePositions();
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
                 if (pretty.tokens.token().id() != JFXTokenId.WS || pretty.tokens.token().text().toString().indexOf(NEWLINE) < 0) {
                     String text = FXSourceUtils.getText(info);
                     pretty.diffs.addFirst(new Diff(text.length(), text.length(), NEWLINE));
                 }
             }
             return pretty.diffs;
         }
 
         public static LinkedList<Diff> reformat(CompilationInfo info, String text, TokenSequence<JFXTokenId> tokens, JavaFXTreePath path, CodeStyle cs) {
             Pretty pretty = new Pretty(info, text, tokens, path, cs, 0, text.length());
             pretty.scan(path, null);
             tokens.moveEnd();
             tokens.movePrevious();
             if (tokens.token().id() != JFXTokenId.WS || tokens.token().text().toString().indexOf(NEWLINE) < 0) {
                 pretty.diffs.addFirst(new Diff(text.length(), text.length(), NEWLINE));
             }
             return pretty.diffs;
         }
 
         /// ===
         /// === START OF THE VISITOR IMPLEMENTATION
         /// ===
 
         @Override
         public Boolean scan(Tree tree, Void p) {
             if (tree == null) {
                 return false;
             }
             
             int lastEndPos = endPos;
             if (tree != null && tree.getJavaFXKind() != JavaFXKind.COMPILATION_UNIT) {
                 if (tree instanceof FakeBlock) {
                     endPos = Integer.MAX_VALUE;
                 } else {
                     endPos = (int) getEndPos(tree);
                 }
             }
             try {
                 if (endPos < 0) {
                     return false;
                 }
                 // this endPos checking has been disabled due to string expression parsing bug in compiler JFXC-4061
 //                if (tokens.offset() <= endPos) {
                     final Boolean scan = super.scan(tree, p);
 //                    final Boolean scan = super.myScan(tree, p);
                     return scan != null ? scan : false;
 //                }
 //                return true;
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
                 processSemicolon();
                 indent = old;
                 blankLines(cs.getBlankLinesAfterPackage());
             }
 
             // imports in javafx could be where ever
             // TODO remove cs.getBlankLinesBeforeImports() from settings therefore
             cuTrees = getCUTrees(node);
             if (cuTrees != null && !cuTrees.isEmpty()) {
                 // TODO process semicolon between members and expressions
 //                boolean semiRead = false;
 
                 final Tree[] treeArray = (Tree[]) cuTrees.toArray(new Tree[cuTrees.size()]);
                 for (int i = 0; i < treeArray.length; i++) {
                     Tree tree = treeArray[i];
                     boolean isLastInCU = i == treeArray.length - 1;
                     JavaFXKind kind = tree.getJavaFXKind();
                     switch (kind) {
                         case IMPORT:
                             blankLines(cs.getBlankLinesBeforeImports());
                             scan(tree, p);
                             if (!isLastInCU && isLastMemberOfSuchKind(i, treeArray, kind)) {
                                 blankLines(cs.getBlankLinesAfterImports());
                             }
                             break;
                         case CLASS_DECLARATION:
                             blankLines(cs.getBlankLinesBeforeClass());
                             scan(tree, p);
                             if (!isLastInCU) {
                                 blankLines(cs.getBlankLinesAfterClass());
                             }
                             break;
                         case VARIABLE:
                             if (isFirstMemberOfSuchKind(i, treeArray, kind)) {
                                 blankLines(cs.getBlankLinesBeforeFields());
                             }
                             processClassMembers(Arrays.asList(new Tree[]{tree}), p, isLastInCU, false);
                             if (!isLastInCU) {
                                 if (isLastMemberOfSuchKind(i, treeArray, kind)) {
                                     blankLines(cs.getBlankLinesAfterFields());
                                 }
                                 blankLines();
                             }
                             break;
                         case INIT_DEFINITION:
                         case POSTINIT_DEFINITION:
                         case FUNCTION_DEFINITION:
                             if (isFirstMemberOfSuchKind(i, treeArray, kind)) {
                                 blankLines(cs.getBlankLinesBeforeMethods());
                             }
                             processClassMembers(Arrays.asList(new Tree[]{tree}), p, isLastInCU, false);
                             if (!isLastInCU) {
                                 blankLines(cs.getBlankLinesAfterMethods());
                             }
                             break;
                         case METHOD_INVOCATION:
                         case FUNCTION_VALUE:
                         case INSTANTIATE_OBJECT_LITERAL:
                             if (isFirstMemberOfSuchKind(i, treeArray, kind)) {
                                 blankLines(cs.getBlankLinesBeforeNonClassExpression());
                             }
                             processClassMembers(Arrays.asList(new Tree[]{tree}), p, isLastInCU, false);
                             if (!isLastInCU) {
                                 blankLines(cs.getBlankLinesAfterNonClassExpression());
                             }
                             break;
                         default:
                             if (isFirstMemberOfSuchKind(i, treeArray, kind)) {
                                 blankLines(1);
                             }
                             processClassMembers(Arrays.asList(new Tree[] {tree}), p, isLastInCU, false);
                             if (!isLastInCU) {
                                 if (isLastMemberOfSuchKind(i, treeArray, kind)) {
                                     blankLines(1);
                                 }
                                 blankLines();
                             }
                     }
                 }
             }
 
             return true;
         }
 
         private static boolean isFirstMemberOfSuchKind(int i, final Tree[] treeArray, JavaFXKind kind) {
             return (i == 0) || (i > 0 && treeArray[i - 1].getJavaFXKind() != kind);
         }
 
         private static boolean isLastMemberOfSuchKind(int i, final Tree[] treeArray, JavaFXKind kind) {
             int l = treeArray.length;
             return (i == l - 1) || (i < l - 2 && treeArray[i + 1].getJavaFXKind() != kind);
         }
 
         @Override
         public Boolean visitInitDefinition(InitDefinitionTree node, Void p) {
             accept(JFXTokenId.INIT);
 //            space();
             scan(node.getBody(), p);
             return true;
         }
 
         @Override
         public Boolean visitPostInitDefinition(InitDefinitionTree node, Void p) {
             accept(JFXTokenId.POSTINIT);
 //            space();
             scan(node.getBody(), p);
             return true;
         }
 
         @Override
         public Boolean visitImport(ImportTree node, Void p) {
             accept(JFXTokenId.IMPORT);
             int old = indent;
             indent += continuationIndentSize;
             space();
             scan(node.getQualifiedIdentifier(), p);
             processSemicolon();
             indent = old;
             return true;
         }
 
         @Override
         public Boolean visitClassDeclaration(ClassDeclarationTree node, Void p) {
             // members without class belong to synthetic one
             boolean isClassSynthetic = isSynthetic((JFXTree) node);
             if (!isClassSynthetic) {
                 int old = indent;
                 ModifiersTree mods = node.getModifiers();
                 if (ReformatUtils.hasModifiers(mods, tokens.token().id())) {
                     if (scan(mods, p)) {
                         indent += continuationIndentSize;
                         if (cs.placeNewLineAfterModifiers()) {
                             newline();
                         } else {
                             space();
                         }
                     }
                 }
 
                 accept(JFXTokenId.CLASS);
                 if (indent == old) {
                     indent += continuationIndentSize;
                 }
                 space();
                 final Name simpleName = node.getSimpleName();
                 if (simpleName != null && !ERROR.contentEquals(simpleName)) {
                     accept(JFXTokenId.IDENTIFIER);
                 }
 
                 List<ExpressionTree> exts = new ArrayList<ExpressionTree>();
                 List<ExpressionTree> anExtends = node.getExtends();
                 if (anExtends != null && !anExtends.isEmpty()) {
                     exts.addAll(anExtends);
                 }
                 List<ExpressionTree> anImplements = node.getImplements();
                 if (anImplements != null && !anImplements.isEmpty()) {
                     exts.addAll(anImplements);
                 }
                 List<ExpressionTree> aMixins = node.getMixins();
                 if (aMixins != null && !aMixins.isEmpty()) {
                     exts.addAll(aMixins);
                 }
                 if (exts != null && !exts.isEmpty()) {
                     wrapToken(cs.wrapExtendsImplementsKeyword(), -1, 1, JFXTokenId.EXTENDS);
                     wrapExtendsList(cs.wrapExtendsImplementsList(), cs.alignMultilineImplements(), true, exts); // TODO cs.alignMultilineExtends()
                 }
                 indent = old;
 
                 CodeStyle.BracePlacement bracePlacement = cs.getClassDeclBracePlacement();
                 boolean spaceBeforeLeftBrace = cs.spaceBeforeClassDeclLeftBrace();
                 old = indent;
                 int halfIndent = indent;
                 switch (bracePlacement) {
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
                     processClassMembers(node.getClassMembers(), p, false, true);
                     if (lastBlankLinesTokenIndex < 0) {
                         newline();
                     }
                 }
                 indent = halfIndent;
                 processWSBeforeRB();
                 accept(JFXTokenId.RBRACE);
                 indent = old;
             } else {
                 processClassMembers(node.getClassMembers(), p, false, true);
             }
             return true;
         }
 
         private void processClassMembers(List<Tree> members, Void p, boolean isLastInCU, boolean ignoreTopLevel) {
             boolean first = true;
             boolean semiRead = false;
             for (Tree member : members) {
 //                if (member.getJavaFXKind() == JavaFXKind.CLASS_DECLARATION && cuTrees != null && cuTrees.contains(member)) {
                 if (ignoreTopLevel && cuTrees != null && cuTrees.contains(member)) {
                     // this member has been already processed in visitCompilationUnit()
                     continue;
                 }
 
                 boolean magicFunc = false;
                 if (member instanceof JFXFunctionDefinition) {
                     String name = ((JFXFunctionDefinition) member).getName().toString();
                     magicFunc = MAGIC_FUNCTION.contentEquals(name) && isSynthetic((JFXTree) member);
                 }
                 if (magicFunc || !isSynthetic((JFXTree) member)) {
                     switch (member.getJavaFXKind()) {
                         case VARIABLE:
                             boolean bool = tokens.moveNext();
                             if (bool) {
                                 tokens.movePrevious();
                                 if (!first) {
                                     blankLines(cs.getBlankLinesBeforeFields());
                                 }
                                 scan(member, p);
                                 semiRead = processSemicolon();
                                 if (!isLastInCU) {
                                     blankLines(cs.getBlankLinesAfterFields());
                                 }
                             }
                             break;
                         case FUNCTION_DEFINITION:
                         case FUNCTION_VALUE:
                         case INIT_DEFINITION:
                         case POSTINIT_DEFINITION:
                             if (!first) {
                                 blankLines(cs.getBlankLinesBeforeMethods());
                             }
                             scan(member, p);
                             semiRead = processSemicolon();
                             if (!isLastInCU) {
                                 blankLines(cs.getBlankLinesAfterMethods());
                             }
                             break;
                         case BLOCK_EXPRESSION:
                             final BlockExpressionTree blockExpTree = (BlockExpressionTree) member;
                             boolean hasStatements = !(blockExpTree).getStatements().isEmpty();
                             boolean hasValue = (blockExpTree).getValue() != null;
 //                        if (semiRead && !(blockExpTree).isStatic() && !hasStatements && !hasValue) {
                             if (semiRead && !hasStatements && !hasValue) {
                                 semiRead = false;
                                 continue;
                             }
                             if (!first) {
                                 blankLines(cs.getBlankLinesBeforeMethods());
                             }
                             processSemicolon();
                             scan(member, p);
                             if (!isLastInCU) {
                                 blankLines(cs.getBlankLinesAfterMethods());
                             }
                             break;
                         case INSTANTIATE_OBJECT_LITERAL:
                         case CLASS_DECLARATION:
                             if (!first) {
                                 blankLines(cs.getBlankLinesBeforeClass());
                             }
                             scan(member, p);
                             semiRead = processSemicolon();
                             if (!isLastInCU) {
                                 blankLines(cs.getBlankLinesAfterClass());
                             }
                             break;
                         default:
                             scan(member, p);
                             semiRead = processSemicolon();
                     }
                     if (!magicFunc) {
                         first = false;
                     }
                 }
             }
         }
 
         private void processWSBeforeRB() {
             Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
             if (diff != null && diff.end == tokens.offset()) {
                 if (diff.text != null) {
                     int idx = diff.text.lastIndexOf(NEWLINE);
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
                     int idx = text.lastIndexOf(NEWLINE);
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
         }
 
         @Override
         public Boolean visitVariable(VariableTree node, Void p) {
             if (isSynthetic((JFXTree) node)) {
                 return false;
             }
 
             int old = indent;
             Tree parent = getCurrentPath().getParentPath().getLeaf();
             boolean insideFor = parent.getJavaFXKind() == JavaFXKind.FOR_EXPRESSION_FOR;
             ModifiersTree mods = node.getModifiers();
             if (ReformatUtils.hasModifiers(mods, tokens.token().id())) {
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
             
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             JFXTokenId accepted = accept(JFXTokenId.DEF, JFXTokenId.VAR, JFXTokenId.ATTRIBUTE);
             // put space if this VAR is not parameter
             if (accepted != null) {
                 space();
             } else {
                 rollback(index, c, d);
             }
 
             final Name name = node.getName();
             if (name != null && !ERROR.contentEquals(name)) {
                 accept(JFXTokenId.IDENTIFIER);
             }
 
             final Tree type = node.getType();
             if (type != null && type.getJavaFXKind() != JavaFXKind.TYPE_UNKNOWN) {
                 // #180145
                 if (processColon()) {
                     if (type instanceof TypeArrayTree) {
                         index = tokens.index();
                         c = col;
                         d = diffs.isEmpty() ? null : diffs.getFirst();
                         if (accept(JFXTokenId.NATIVEARRAY) == JFXTokenId.NATIVEARRAY) {
                             space();
 //                        accept(JFXTokenId.OF);
                             accept(JFXTokenId.IDENTIFIER); // lexer bug?
                             space();
                         } else {
                             rollback(index, c, d);
                         }
 //                } else if (type.getJavaFXKind() == JavaFXKind.TYPE_FUNCTIONAL) {
 //                    accept(JFXTokenId.FUNCTION);
 //                    spaces(cs.spaceBeforeMethodDeclParen() ? 1 : 0);
                     }
                     scan(type, p);
                 }
             }
 
             ExpressionTree initTree = node.getInitializer();
             if (initTree != null) {
                 int alignIndent = -1;
                 if (cs.alignMultilineAssignment()) {
                     alignIndent = col;
                     if (!ERROR.contentEquals(name)) {
                         alignIndent -= name.length();
                     }
                 }
                 spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                 accept(JFXTokenId.EQ);
 
                 final JavafxBindStatus bindStatus = node.getBindStatus();
                 if (bindStatus.isUnidiBind() || bindStatus.isBidiBind()) {
                     spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                     accept(JFXTokenId.BIND);
                 }
 
                 wrapTree(cs.wrapAssignOps(), alignIndent, cs.spaceAroundAssignOps() ? 1 : 0, initTree);
 
                 if (bindStatus.isBidiBind()) {
                     space();
                     accept(JFXTokenId.WITH);
                     space();
                     accept(JFXTokenId.INVERSE);
                 }
             }
 
             indent = old;
 
             OnReplaceTree onReplaceTree = node.getOnReplaceTree();
             if (onReplaceTree != null) {
                 spaces(1, true);
                 scan(onReplaceTree, p);
             }
             processSemicolon();
             return true;
         }
 
         // TODO isInitialized Built-In Function
         @Override
         public Boolean visitFunctionDefinition(FunctionDefinitionTree node, Void p) {
             JFXFunctionDefinition funcDef = (JFXFunctionDefinition) node;
             boolean magicOverridenFunc = MAGIC_FUNCTION.contentEquals(funcDef.getName());
             boolean magicFunc = magicOverridenFunc && isSynthetic(funcDef);
 
             // magic function processed in visitCompilationUnit()
             if (magicFunc) {
                 return false;
             }
             int old = indent;
 
             // TODO work around for magic function modifiers compiler bug
             // JFXC-3633
             if (magicOverridenFunc) { // even more magic!
                 // ---
                 JFXTokenId id = null;
                 while (tokens.offset() < endPos) {
                     if (id != null) {
                         space();
                     }
                     int index = tokens.index();
                     int c = col;
                     Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                     id = accept(ReformatUtils.MODIFIER_KEYWORDS);
                     if (id == null) {
                         rollback(index, c, d);
                         break;
                     }
                 }
                 // ---
             } else {
                 ModifiersTree mods = funcDef.getModifiers();
                 if (ReformatUtils.hasModifiers(mods, tokens.token().id())) {
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
 //            if (params != null && !params.isEmpty() && !magicOverridenFunc) {
             if (params != null && !params.isEmpty()) {
                 spaces(cs.spaceWithinFunctionDeclParens() ? 1 : 0, true);
                 // TODO work around for magic function modifiers compiler bug
                 // JFXC-4226
                 if (magicOverridenFunc) {
                     accept(JFXTokenId.IDENTIFIER);
                     processColon();
                     accept(JFXTokenId.IDENTIFIER);
                     accept(JFXTokenId.LBRACKET);
                     if (cs.spaceWithinArrayInitBrackets()) {
                         space();
                     }
                     accept(JFXTokenId.RBRACKET);
                 } else {
                     wrapList(cs.wrapMethodParams(), cs.alignMultilineMethodParams(), false, params);
                 }
                 spaces(cs.spaceWithinFunctionDeclParens() ? 1 : 0);
             }
             accept(JFXTokenId.RPAREN);
 
             JFXType retType = funcDef.getJFXReturnType();
             if (retType != null && retType.getJavaFXKind() != JavaFXKind.TYPE_UNKNOWN) {
                 // #179454, if function is overriden then return type could be not specified
                 if (processColon()) {
                     scan(retType, p);
                 }
             }
             indent = old;
 
             JFXBlock body = funcDef.getBodyExpression();
             if (body != null) {
                 scan(body, p);
 //            } else if (!magicFunc) {
             } else {
                 processSemicolon();
             }
             return true;
         }
 
         @Override
         public Boolean visitFunctionValue(FunctionValueTree node, Void p) {
             accept(JFXTokenId.FUNCTION);
            space();
 
             int old = indent;
             indent += continuationIndentSize;
             spaces(cs.spaceBeforeMethodDeclParen() ? 1 : 0);
             accept(JFXTokenId.LPAREN);
             List<? extends VariableTree> params = node.getParameters();
             if (params != null && !params.isEmpty()) {
                 spaces(cs.spaceWithinFunctionDeclParens() ? 1 : 0, true);
                 wrapList(cs.wrapMethodParams(), cs.alignMultilineMethodParams(), false, params);
                 spaces(cs.spaceWithinFunctionDeclParens() ? 1 : 0);
             }
             accept(JFXTokenId.RPAREN);
 
             TypeTree retType = node.getType();
             if (retType != null && retType.getJavaFXKind() != JavaFXKind.TYPE_UNKNOWN) {
                 if (processColon()) {
                     scan(retType, p);
                 }
             }
             indent = old;
 
             BlockExpressionTree body = node.getBodyExpression();
             if (body != null) {
                 scan(body, p);
             }
             return true;
         }
 
         @Override
         public Boolean visitModifiers(ModifiersTree node, Void p) {
             JFXTokenId id = null;
             JavaFXTreePath path = getCurrentPath().getParentPath();
             path = path.getParentPath();
             while (tokens.offset() < endPos) {
                 if (id != null) {
                     space();
                 }
                 int index = tokens.index();
                 int c = col;
                 Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                 id = accept(ReformatUtils.MODIFIER_KEYWORDS);
                 if (id == null) {
                     rollback(index, c, d);
                     break;
                 }
             }
             return true;
         }
 
         @Override
         public Boolean visitBlockExpression(BlockExpressionTree node, Void p) {
             CodeStyle.BracePlacement bracePlacement;
             boolean spaceBeforeLeftBrace = false;
 
             Tree parentTree = getCurrentPath().getParentPath().getLeaf();
             boolean magicFunc = false;
             if (parentTree instanceof JFXFunctionDefinition) {
                 String name = ((JFXFunctionDefinition) parentTree).getName().toString();
                 magicFunc = MAGIC_FUNCTION.contentEquals(name) && isSynthetic((JFXTree) parentTree);
             }
 
             int halfIndent = 0;
             int old = 0;
             if (!magicFunc) {
                 switch (parentTree.getJavaFXKind()) {
                     case CLASS_DECLARATION:
                     case INSTANTIATE_NEW:
                         bracePlacement = cs.getClassDeclBracePlacement();
                         spaceBeforeLeftBrace = cs.spaceBeforeClassDeclLeftBrace();
                         break;
                     case ON_REPLACE:
                         bracePlacement = cs.getOnReplacePlacement();
                         spaceBeforeLeftBrace = cs.spaceBeforeOnReplaceDeclLeftBrace();
                         break;
                     case INIT_DEFINITION:
                         bracePlacement = cs.getFunctionDeclBracePlacement();
                         spaceBeforeLeftBrace = cs.spaceBeforeInitBlockLeftBrace();
                         break;
                     case POSTINIT_DEFINITION:
                         bracePlacement = cs.getFunctionDeclBracePlacement();
                         spaceBeforeLeftBrace = cs.spaceBeforePostInitBlockLeftBrace();
                         break;
                     case INSTANTIATE_OBJECT_LITERAL:
                         bracePlacement = cs.getObjectLiteralBracePlacement();
                         spaceBeforeLeftBrace = cs.spaceBeforeObjectLiteralDeclLeftBrace();
                         break;
                     case FUNCTION_DEFINITION:
                     case FUNCTION_VALUE:
                         bracePlacement = cs.getFunctionDeclBracePlacement();
                         spaceBeforeLeftBrace = cs.spaceBeforeFunctionDeclLeftBrace();
                         break;
                     case TRY:
                         bracePlacement = cs.getOtherBracePlacement();
                         if (((TryTree) parentTree).getBlock() == node) {
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
                         bracePlacement = cs.getOtherBracePlacement();
                         spaceBeforeLeftBrace = cs.spaceBeforeForLeftBrace();
                         break;
                     case CONDITIONAL_EXPRESSION:
                         bracePlacement = cs.getOtherBracePlacement();
                         if (((JFXIfExpression) parentTree).getTrueExpression() == node) {
                             spaceBeforeLeftBrace = cs.spaceBeforeIfLeftBrace();
                         } else {
                             spaceBeforeLeftBrace = cs.spaceBeforeElseLeftBrace();
                         }
                         break;
                     default:
                         bracePlacement = cs.getOtherBracePlacement();
                         break;
                 }
 
                 old = indent;
                 halfIndent = indent;
                 switch (bracePlacement) {
                     case SAME_LINE:
                         spaces(spaceBeforeLeftBrace ? 1 : 0);
                         if (node instanceof FakeBlock) {
                             appendToDiff(LCBRACE);
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
                             appendToDiff(LCBRACE);
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
                             appendToDiff(LCBRACE);
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
                             appendToDiff(LCBRACE);
                             lastBlankLines = -1;
                             lastBlankLinesTokenIndex = -1;
                             lastBlankLinesDiff = null;
                         } else {
                             accept(JFXTokenId.LBRACE);
                         }
                         break;
                 }
             }
 //            boolean isEmpty = true;
             final List<ExpressionTree> expressions = new ArrayList<ExpressionTree>();
             List<? extends ExpressionTree> statements = node.getStatements();
             if (statements != null && !statements.isEmpty()) {
                 expressions.addAll(statements);
             }
             // JFXC-3284
             if (expressions.isEmpty()) {
                 final ExpressionTree value = node.getValue();
                 if (value != null) {
                     expressions.add(value);
                 }
             }
             for (ExpressionTree expression : expressions) {
                 if (magicFunc || !isSynthetic((JFXTree) node)) {
 //                    isEmpty = false;
                     if (node instanceof FakeBlock) {
                         appendToDiff(getNewlines(1) + getIndent());
                         col = indent;
                     } else if (isSimpleBlock(parentTree)) {
                         spaces(cs.spaceWithinBraces() ? 1 : 0, true);
                     } else {
                         blankLines();
                     }
 
                     // Missing return statement, compliler bug http://javafx-jira.kenai.com/browse/JFXC-3528
                     int index = tokens.index();
                     int c = col;
                     Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                     if (accept(JFXTokenId.RETURN) != JFXTokenId.RETURN) {
                         rollback(index, c, d);
                     } else {
                         space();
                     }
 
                     processExpression(expression, p);
                 }
             }
 
 //            if (isEmpty || templateEdit) {
             if (templateEdit) {
                 newline();
             }
             if (node instanceof FakeBlock) {
                 indent = halfIndent;
                 int i = tokens.index();
                 boolean loop = true;
                 while (loop) {
                     switch (tokens.token().id()) {
                         case WS:
                             if (tokens.token().text().toString().indexOf(NEWLINE) < 0) {
                                 tokens.moveNext();
                             } else {
                                 loop = false;
                                 appendToDiff(NEWLINE);
                                 col = 0;
                             }
                             break;
                         case LINE_COMMENT:
                             loop = false;
                         case COMMENT:
                             tokens.moveNext();
                             break;
                         default:
                             if (tokens.index() != i) {
                                 tokens.moveIndex(i);
                                 tokens.moveNext();
                             }
                             loop = false;
                             appendToDiff(NEWLINE);
                             col = 0;
                     }
                 }
                 appendToDiff(getIndent() + RCBRACE);
                 col = indent + 1;
                 lastBlankLines = -1;
                 lastBlankLinesTokenIndex = -1;
                 lastBlankLinesDiff = null;
             } else {
                 if (!magicFunc) {
                     indent = halfIndent;
                     if (isSimpleBlock(parentTree)) {
                         spaces(cs.spaceWithinBraces() ? 1 : 0, true);
                     } else {
                         blankLines();
                     }
 //                    processWSBeforeRB();
                     accept(JFXTokenId.RBRACE);
                     indent = old;
                 }
             }
             return true;
         }
 
         // there is no visitExpression in javafx so far
         private void processExpression(ExpressionTree stat, Void p) {
 //            int old = indent;
 //            indent += continuationIndentSize;
             scan(stat, p);
             processSemicolon();
 //            indent = old;
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
                 spaces(cs.spaceWithinFunctionCallParens() ? 1 : 0, true);
                 wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), false, args);
                 spaces(cs.spaceWithinFunctionCallParens() ? 1 : 0);
             }
             accept(JFXTokenId.RPAREN);
             return true;
         }
 
         // Java NewClassTree --> InstantiateTree
         @Override
         public Boolean visitInstantiate(InstantiateTree node, Void p) {
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             // JFXC-3545
             final boolean isNewKeyWordUsed = accept(JFXTokenId.NEW) == JFXTokenId.NEW;
             if (isNewKeyWordUsed) {
                 space();
             } else {
                 rollback(index, c, d);
             }
 
             scan(node.getIdentifier(), p);
 //            spaces(!isNewKeyWordUsed || cs.spaceBeforeMethodCallParen() ? 1 : 0);
 
             if (isNewKeyWordUsed) {
                 accept(JFXTokenId.LPAREN);
                 List<? extends ExpressionTree> args = node.getArguments();
                 if (args != null && !args.isEmpty()) {
                     spaces(cs.spaceWithinFunctionCallParens() ? 1 : 0, true);
                     wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), false, args);
                     spaces(cs.spaceWithinFunctionCallParens() ? 1 : 0);
                 }
                 accept(JFXTokenId.RPAREN);
 
                 ClassDeclarationTree body = node.getClassBody();
                 if (body != null) {
                     scan(body, p);
                 }
             } else {
                 CodeStyle.BracePlacement bracePlacement = cs.getObjectLiteralBracePlacement();
                 int old = indent;
                 int halfIndent = indent;
                 switch (bracePlacement) {
                     case SAME_LINE:
                         spaces(cs.spaceBeforeObjectLiteralDeclLeftBrace() ? 1 : 0);
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
 
                 TreeSet<Tree> members = new TreeSet<Tree>(new TreePosComparator(sp, root));
                 members.addAll(node.getLiteralParts());
                 ClassDeclarationTree body = node.getClassBody();
                 if (body != null) {
                     members.addAll(body.getClassMembers());
                 }
                 List<VariableTree> localVariables = node.getLocalVariables();
                 if (localVariables != null && !localVariables.isEmpty()) {
                     members.addAll(localVariables);
                 }
                 if (!members.isEmpty()) {
 //                    spaces(cs.spaceWithinFunctionCallParens() ? 1 : 0, true);
                     spaces(cs.spaceWithinBraces() ? 1 : 0, true);
                     wrapLiteralList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), members);
                 }
                 indent = halfIndent;
                 spaces(cs.spaceWithinBraces() && !members.isEmpty() ? 1 : 0, true);
                 accept(JFXTokenId.RBRACE);
                 indent = old;
             }
             
             return true;
         }
 
         @Override
         public Boolean visitReturn(ReturnTree node, Void p) {
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             // there is a compiler bug with dissappearing return keyword from the tree
             boolean accepted = accept(JFXTokenId.RETURN) == JFXTokenId.RETURN;
             if (!accepted) {
                 rollback(index, c, d);
             }
             int old = indent;
             indent += continuationIndentSize;
             ExpressionTree exp = node.getExpression();
             if (exp != null) {
                 if (accepted) {
                     space();
                 }
                 scan(exp, p);
             }
             // should be accepted in processExpression()
 //            accept(JFXTokenId.SEMI);
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
             processSemicolon();
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
 
         @Override
         public Boolean visitWhileLoop(WhileLoopTree node, Void p) {
             accept(JFXTokenId.WHILE);
             int old = indent;
             indent += continuationIndentSize;
             spaces(cs.spaceBeforeWhileParen() ? 1 : 0);
             // missing parenthesized work around
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             boolean wrapped = accept(JFXTokenId.LPAREN) == JFXTokenId.LPAREN;
             if (wrapped) {
                 spaces(cs.spaceWithinWhileParens() ? 1 : 0, true);
             } else {
                 rollback(index, c, d);
             }
             scan(node.getCondition(), p);
             if (wrapped) {
                 spaces(cs.spaceWithinWhileParens() ? 1 : 0);
                 accept(JFXTokenId.RPAREN);
             }
             indent = old;
             CodeStyle.BracesGenerationStyle redundantWhileBraces = cs.redundantWhileBraces();
             if (redundantWhileBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > getStartPos(node) || endOffset < getEndPos(node))) {
                 redundantWhileBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
             }
             wrapStatement(cs.wrapWhileStatement(), redundantWhileBraces, cs.spaceBeforeWhileLeftBrace() ? 1 : 0, node.getBody());
             return true;
         }
 
         @Override
         public Boolean visitForExpression(ForExpressionTree node, Void p) {
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             if (accept(JFXTokenId.FOR) == JFXTokenId.FOR) { // FOR_EXPRESSION
                 int old = indent;
                 indent += continuationIndentSize;
                 spaces(cs.spaceBeforeForParen() ? 1 : 0);
                 accept(JFXTokenId.LPAREN);
                 spaces(cs.spaceWithinForParens() ? 1 : 0, true);
 
                 List<? extends ForExpressionInClauseTree> clauses = node.getInClauses();
                 if (clauses != null && !clauses.isEmpty()) {
                     for (Iterator<? extends ForExpressionInClauseTree> it = clauses.iterator(); it.hasNext();) {
                         ForExpressionInClauseTree feict = it.next();
                         scan(feict, p);
                         if (it.hasNext()) {
                             spaces(cs.spaceBeforeComma() ? 1 : 0);
                             accept(JFXTokenId.COMMA);
                             spaces(cs.spaceAfterComma() ? 1 : 0);
                         }
                     }
                 }
                 spaces(cs.spaceWithinForParens() ? 1 : 0);
                 accept(JFXTokenId.RPAREN);
                 indent = old;
 
                 ExpressionTree bodyExpression = node.getBodyExpression();
                 boolean insideVar = ReformatUtils.isTreeInsideVar(getCurrentPath());
                 boolean becoeo = ReformatUtils.containsOneExpressionOnly(bodyExpression);
                 CodeStyle.BracesGenerationStyle redundantForBraces = cs.redundantForBraces();
                 if (insideVar || becoeo || (redundantForBraces == CodeStyle.BracesGenerationStyle.GENERATE &&
                         (startOffset > getStartPos(node) || endOffset < getEndPos(node)))) {
                     redundantForBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
                 }
                 WrapStyle wrapStyle = insideVar || becoeo ? WrapStyle.WRAP_NEVER : cs.wrapForStatement();
                 wrapStatement(wrapStyle, redundantForBraces, cs.spaceBeforeForLeftBrace() ? 1 : 0, bodyExpression);
             } else { // FOR_EXPRESSION_PREDICATE
                 rollback(index, c, d);
                 ForExpressionInClauseTree feict = node.getInClauses().get(0);
                 scan(feict.getSequenceExpression(), p);
                 spaces(cs.spaceBeforeSequenceInitLeftBrace() ? 1 : 0, false);
                 accept(JFXTokenId.LBRACKET);
                 int old = indent;
                 indent += indentSize;
                 spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0, true);
                 scan(feict.getVariable(), p);
                 spaces(cs.spaceAroundBinaryOps() ? 1 : 0, false);
                 accept(JFXTokenId.PIPE);
                 spaces(cs.spaceAroundBinaryOps() ? 1 : 0, false);
                 scan(feict.getWhereExpression(), p);
                 spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0, true);
                 indent = old;
                 accept(JFXTokenId.RBRACKET);
             }
 
             return true;
         }
 
         @Override
         public Boolean visitForExpressionInClause(ForExpressionInClauseTree node, Void p) {
             scan(node.getVariable(), p);
             space();
             accept(JFXTokenId.IN);
             space();
             scan(node.getSequenceExpression(), p);
             ExpressionTree whereExpression = node.getWhereExpression();
             if (whereExpression != null) {
                 space();
                 accept(JFXTokenId.WHERE);
                 space();
                 scan(whereExpression, p);
             }
             return true;
         }
 
         @Override
         public Boolean visitBreak(BreakTree node, Void p) {
             accept(JFXTokenId.BREAK);
             // TODO check, there should be no label in javafx
             Name label = node.getLabel();
             if (label != null) {
                 space();
                 accept(JFXTokenId.IDENTIFIER);
             }
             processSemicolon();
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
             processSemicolon();
             return true;
         }
 
         // TODO sequence
         @Override
         public Boolean visitAssignment(AssignmentTree node, Void p) {
             int old = indent;
             indent += continuationIndentSize;
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
             indent = old;
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
             do {
                 col += tokens.token().length();
             } while (tokens.moveNext() && tokens.offset() < endPos);
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
             return true;
         }
 
         @Override
         public Boolean visitTypeArray(TypeArrayTree node, Void p) {
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             if (accept(JFXTokenId.NATIVEARRAY) == JFXTokenId.NATIVEARRAY) {
                 space();
                 accept(JFXTokenId.IDENTIFIER); // non-reserved keyword "of"?
                 space();
             } else {
                 rollback(index, c, d);
             }
 
             boolean ret = scan(node.getElementType(), p);
             index = tokens.index();
             c = col;
             d = diffs.isEmpty() ? null : diffs.getFirst();
             if (accept(JFXTokenId.LBRACKET) == JFXTokenId.LBRACKET) {
                 accept(JFXTokenId.RBRACKET);
             } else {
                 rollback(index, c, d);
             }
             return ret;
         }
 
         @Override
         public Boolean visitTypeClass(TypeClassTree node, Void p) {
             // issue #177106: TypeClass before MemberSelect
             boolean accepted = true;
             while (accepted) {
                 accept(JFXTokenId.IDENTIFIER, JFXTokenId.STAR, JFXTokenId.THIS, JFXTokenId.SUPER, JFXTokenId.CLASS);
                 int index = tokens.index();
                 int c = col;
                 Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                 accepted = accept(JFXTokenId.DOT) == JFXTokenId.DOT;
                 if (!accepted) {
                     rollback(index, c, d);
                 }
             }
 
             // JFXC-3954
             boolean quoted = acceptAndRollback(JFXTokenId.LPAREN) == JFXTokenId.LPAREN;
             if (quoted) {
                 accept(JFXTokenId.LPAREN);
                 scan(node.getClassName(), p);
                 accept(JFXTokenId.RPAREN);
             }
 
             // sequence type
             if (acceptAndRollback(JFXTokenId.LBRACKET) == JFXTokenId.LBRACKET) {
                 accept(JFXTokenId.LBRACKET);
                 if (cs.spaceWithinArrayInitBrackets()) {
                     space();
                 }
                 accept(JFXTokenId.RBRACKET);
             }
             return true;
         }
 
         @Override
         public Boolean visitTypeFunctional(TypeFunctionalTree node, Void p) {
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             if (accept(JFXTokenId.FUNCTION) == JFXTokenId.FUNCTION) {
                 spaces(cs.spaceBeforeMethodDeclParen() ? 1 : 0);
             } else {
                 rollback(index, c, d);
             }
             accept(JFXTokenId.LPAREN);
             List<? extends TypeTree> params = node.getParameters();
             if (params != null && !params.isEmpty()) {
                 spaces(cs.spaceWithinFunctionDeclParens() ? 1 : 0, true);
                 wrapFunctionalParamList(cs.wrapMethodParams(), cs.alignMultilineMethodParams(), params);
                 spaces(cs.spaceWithinFunctionDeclParens() ? 1 : 0);
             }
             accept(JFXTokenId.RPAREN);
             TypeTree retType = node.getReturnType();
             if (retType != null && retType.getJavaFXKind() != JavaFXKind.TYPE_UNKNOWN) {
                 if (processColon()) {
                     scan(retType, p);
                 }
             }
             return true;
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
 
         @Override
         public Boolean visitIdentifier(IdentifierTree node, Void p) {
             accept(JFXTokenId.IDENTIFIER, JFXTokenId.THIS, JFXTokenId.SUPER);
             return true;
         }
 
         @Override
         public Boolean visitUnary(UnaryTree node, Void p) {
             final JFXTokenId id = tokens.token().id();
             final JavaFXKind kind = node.getJavaFXKind();
             if (kind == JavaFXKind.SIZEOF) {
                 accept(JFXTokenId.SIZEOF);
                 space();
                 scan(node.getExpression(), p);
             } else if (kind == JavaFXKind.INDEXOF) {
                 accept(JFXTokenId.INDEXOF);
                 space();
                 scan(node.getExpression(), p);
             } else if (kind == JavaFXKind.LOGICAL_COMPLEMENT) {
                 accept(JFXTokenId.NOT);
                 space();
                 scan(node.getExpression(), p);
             } else if (kind == JavaFXKind.REVERSE) {
                 accept(JFXTokenId.REVERSE);
                 space();
                 scan(node.getExpression(), p);
             } else if (OPERATOR.equals(id.primaryCategory())) {
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
             scan(node.getLeftOperand(), p);
             int alignIndent = cs.alignMultilineBinaryOp() ? col : -1;
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
 
             // missing parenthesized work around
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             boolean wrapped = accept(JFXTokenId.LPAREN) == JFXTokenId.LPAREN;
             if (wrapped) {
                 spaces(cs.spaceWithinIfParens() ? 1 : 0, true);
             } else {
                 rollback(index, c, d);
             }
             scan(ifExpr.getCondition(), p);
             if (wrapped) {
                 spaces(cs.spaceWithinIfParens() ? 1 : 0);
                 accept(JFXTokenId.RPAREN);
             }
             indent = old;
 
             index = tokens.index();
             c = col;
             d = diffs.isEmpty() ? null : diffs.getFirst();
             JFXTokenId accepted = accept(JFXTokenId.THEN);
             rollback(index, c, d);
             if (accepted == JFXTokenId.THEN) {
                 space();
                 accept(JFXTokenId.THEN);
             }
 
             JFXExpression trueExpr = ifExpr.getTrueExpression();
             JFXExpression falseExpr = ifExpr.getFalseExpression();
             CodeStyle.BracesGenerationStyle redundantIfBraces = cs.redundantIfBraces();
             if ((falseExpr != null && redundantIfBraces == CodeStyle.BracesGenerationStyle.ELIMINATE && danglingElseChecker.hasDanglingElse(trueExpr)) ||
                     (redundantIfBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > getStartPos(ifExpr) || endOffset < getEndPos(node)))) {
                 redundantIfBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
             }
 
             boolean insideVar = ReformatUtils.isTreeInsideVar(getCurrentPath());
             boolean tecoeo = ReformatUtils.containsOneExpressionOnly(trueExpr);
 //            boolean fecoeo = ReformatUtils.containsOneExpressionOnly(falseExpr);
 
             // TODO make cs.wrapIfExpression
             final WrapStyle wrapIfStatement = insideVar || tecoeo ? WrapStyle.WRAP_NEVER : cs.wrapIfexpression();
             boolean prevblock = wrapStatement(wrapIfStatement, redundantIfBraces, cs.spaceBeforeIfLeftBrace() ? 1 : 0, trueExpr);
             if (falseExpr != null) {
                 if (!insideVar && (cs.placeElseOnNewLine() || !prevblock)) {
 //                if (!insideVar && !fecoeo && (cs.placeElseOnNewLine() || !prevblock)) {
                     newline();
                 } else {
                     spaces(cs.spaceBeforeElse() ? 1 : 0);
                 }
                 accept(JFXTokenId.ELSE);
                 if (falseExpr.getJavaFXKind() == JavaFXKind.CONDITIONAL_EXPRESSION && cs.specialElseIf()) {
                     space();
                     scan(falseExpr, p);
                 } else {
                     redundantIfBraces = cs.redundantIfBraces();
                     if (redundantIfBraces == CodeStyle.BracesGenerationStyle.GENERATE && (startOffset > getStartPos(ifExpr) || endOffset < getEndPos(ifExpr))) {
                         redundantIfBraces = CodeStyle.BracesGenerationStyle.LEAVE_ALONE;
                     }
                     wrapStatement(wrapIfStatement, redundantIfBraces, cs.spaceBeforeElseLeftBrace() ? 1 : 0, falseExpr);
                 }
                 indent = old;
             }
             return true;
         }
 
         @Override
         public Boolean visitEmptyStatement(EmptyStatementTree node, Void p) {
             processSemicolon();
             return true;
         }
 
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
         public Boolean visitIndexof(IndexofTree node, Void p) {
             accept(JFXTokenId.INDEXOF);
             space();
             scan(node.getForVarIdentifier(), p);
             return true;
         }
 
         @Override
         public Boolean visitTypeCast(TypeCastTree node, Void p) {
             scan(node.getExpression(), p);
             space();
             accept(JFXTokenId.AS);
             space();
             scan(node.getType(), p);
             return true;
         }
 
         @Override
         public Boolean visitParenthesized(ParenthesizedTree node, Void p) {
             accept(JFXTokenId.LPAREN);
             boolean spaceWithinParens;
             switch (getCurrentPath().getParentPath().getLeaf().getJavaFXKind()) {
                 case FUNCTION_DEFINITION:
                     spaceWithinParens = cs.spaceWithinFunctionDeclParens();
                     break;
                 case FUNCTION_VALUE:
                     spaceWithinParens = cs.spaceWithinFunctionCallParens();
                     break;
                 case CONDITIONAL_EXPRESSION:
                     spaceWithinParens = cs.spaceWithinIfParens();
                     break;
                 case FOR_EXPRESSION_FOR:
                     spaceWithinParens = cs.spaceWithinForParens();
                     break;
                 case WHILE_LOOP:
                     spaceWithinParens = cs.spaceWithinWhileParens();
                     break;
                 case CATCH:
                     spaceWithinParens = cs.spaceWithinCatchParens();
                     break;
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
         public Boolean visitInterpolateValue(InterpolateValueTree node, Void p) {
             scan(node.getAttribute(), p);
             space();
             accept(JFXTokenId.SUCHTHAT); // nice token, LOL
             space();
             scan(node.getValue(), p);
             ExpressionTree interpolation = node.getInterpolation();
             if (interpolation != null) {
                 space();
                 accept(JFXTokenId.TWEEN);
                 space();
                 scan(interpolation, p);
             }
             return true;
         }
 
         @Override
         public Boolean visitKeyFrameLiteral(KeyFrameLiteralTree node, Void p) {
             accept(JFXTokenId.AT);
             space();
             accept(JFXTokenId.LPAREN);
             scan(node.getStartDuration(), p);
             accept(JFXTokenId.RPAREN);
             space();
 
             accept(JFXTokenId.LBRACE);
             int old = indent;
             indent += indentSize;
 
             TreeSet<Tree> members = new TreeSet<Tree>(new TreePosComparator(sp, root));
             members.addAll(node.getInterpolationValues());
             ExpressionTree trigger = node.getTrigger();
             if (trigger != null) {
                 members.add(trigger);
             }
 
             // TODO process trigger keyword?
             if (!members.isEmpty()) {
                 spaces(cs.spaceWithinFunctionCallParens() ? 1 : 0, true);
                 // TODO cs.alignMultipleInterpolationValues
 //                wrapList(cs.wrapMethodCallArgs(), cs.alignMultipleInterpolationValues(), members);
                 wrapLiteralList(cs.wrapMethodCallArgs(), false, members);
             }
             indent = old;
             spaces(0, true);
             accept(JFXTokenId.RBRACE);
 
             return true;
         }
 
         @Override
         public Boolean visitMissingExpression(ExpressionTree node, Void p) {
             do {
                 col += tokens.token().length();
             } while (tokens.moveNext() && tokens.offset() < endPos);
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
             return true;
         }
 
         @Override
         public Boolean visitObjectLiteralPart(ObjectLiteralPartTree node, Void p) {
             accept(JFXTokenId.IDENTIFIER);
             spaces(cs.spaceBeforeColon() ? 1 : 0);
             accept(JFXTokenId.COLON);
             spaces(cs.spaceAfterColon() ? 1 : 0);
 
             final JavafxBindStatus bindStatus = node.getBindStatus();
             if (bindStatus.isUnidiBind() || bindStatus.isBidiBind()) {
                 accept(JFXTokenId.BIND);
                 space();
             }
             scan(node.getExpression(), p);
             if (bindStatus.isBidiBind()) {
                 space();
                 accept(JFXTokenId.WITH);
                 space();
                 accept(JFXTokenId.INVERSE);
             }
             return true;
         }
 
         @Override
         public Boolean visitOnReplace(OnReplaceTree node, Void p) {
             accept(JFXTokenId.ON);
             space();
             accept(JFXTokenId.REPLACE);
             VariableTree oldValue = node.getOldValue();
             if (oldValue != null) {
                 space();
                 scan(oldValue, p);
                 if (node.getFirstIndex() != null) {
                     accept(JFXTokenId.LBRACKET);
                     spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
                     scan(node.getFirstIndex(), p);
                     spaces(cs.spaceAroundRangeOps() ? 1 : 0);
                     accept(JFXTokenId.DOTDOT);
                     spaces(cs.spaceAroundRangeOps() ? 1 : 0);
                     scan(node.getLastIndex(), p);
                     spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
                     accept(JFXTokenId.RBRACKET);
                 }
             }
 
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             boolean hasInitializer = accept(JFXTokenId.EQ) == JFXTokenId.EQ;
             rollback(index, c, d);
             if (hasInitializer) {
                 spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                 accept(JFXTokenId.EQ);
                 spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                 index = tokens.index();
                 c = col;
                 d = diffs.isEmpty() ? null : diffs.getFirst();
                 if (accept(JFXTokenId.IDENTIFIER) != JFXTokenId.IDENTIFIER) {
                     rollback(index, c, d);
                 }
             }
             scan(node.getBody(), p);
             return true;
         }
 
         @Override
         public Boolean visitTrigger(TriggerTree node, Void p) {
             do {
                 col += tokens.token().length();
             } while (tokens.moveNext() && tokens.offset() < endPos);
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
             return true;
         }
 
         @Override
         public Boolean visitStringExpression(StringExpressionTree node, Void p) {
             List<ExpressionTree> partList = node.getPartList();
             if (partList != null && !partList.isEmpty()) {
                 boolean isQuoted = false;
                 for (Iterator<ExpressionTree> it = partList.iterator(); it.hasNext();) {
                     // #178966
                     processTranslationKey();
 
                     // JFXC-3494
                     ExpressionTree tree = it.next();
                     if (!isQuoted) {
                         isQuoted = acceptAndRollback(JFXTokenId.QUOTE_LBRACE_STRING_LITERAL) == JFXTokenId.QUOTE_LBRACE_STRING_LITERAL;
                         if (isQuoted) {
                             accept(JFXTokenId.QUOTE_LBRACE_STRING_LITERAL);
                             continue;
                         }
                     } else {
                         if (acceptAndRollback(JFXTokenId.RBRACE_QUOTE_STRING_LITERAL) == JFXTokenId.RBRACE_QUOTE_STRING_LITERAL) {
                             accept(JFXTokenId.RBRACE_QUOTE_STRING_LITERAL);
                             spaces(0, true);
                             isQuoted = false;
                         }
                     }
                     scan(tree, p);
                     if (it.hasNext() && !isQuoted) {
                         spaces(0, true);
                     }
                 }
             }
             return true;
         }
 
         @Override
         public Boolean visitTimeLiteral(TimeLiteralTree node, Void p) {
             do {
                 col += tokens.token().length();
             } while (tokens.moveNext() && tokens.offset() < endPos);
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
             return true;
         }
 
         // TODO remove that workarounds for JFXC-3528 after switching to SoMa
         @Override
         public Boolean visitLiteral(LiteralTree node, Void p) {
             // Missing return statement, compliler bug http://javafx-jira.kenai.com/browse/JFXC-3528
             // ---
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             if (accept(JFXTokenId.RETURN) == JFXTokenId.RETURN) {
                 space();
             } else {
                 rollback(index, c, d);
             }
             // ---
 
             JavaFXKind kind = node.getJavaFXKind();
             if (kind == JavaFXKind.STRING_LITERAL) {
                 Tree parent = getCurrentPath().getParentPath().getLeaf();
                 boolean insideSE = parent.getJavaFXKind() == JavaFXKind.STRING_EXPRESSION;
                 if (insideSE) {
                     accept(JFXTokenId.STRING_LITERAL);
                 } else {
                     // JFXC-4061
                     boolean isNextTokenStringLiteral = true;
                     while (isNextTokenStringLiteral) {
                         index = tokens.index();
                         c = col;
                         d = diffs.isEmpty() ? null : diffs.getFirst();
                         boolean accepted = accept(JFXTokenId.STRING_LITERAL) != null;
                         if (accepted) {
                             isNextTokenStringLiteral = acceptAndRollback(JFXTokenId.STRING_LITERAL) != null;
                             if (isNextTokenStringLiteral) {
                                 spaces(0, true);
                             }
                         } else {
                             isNextTokenStringLiteral = false;
                             rollback(index, c, d);
                         }
                     }
                 }
             } else {
                 // #176654: probably compiler bug
                 // for literal "-10" AST literal tree only but lexer has SUB token and INT_LITERAL token
                 // workaround
                 // ---
                 index = tokens.index();
                 c = col;
                 d = diffs.isEmpty() ? null : diffs.getFirst();
                 if (accept(JFXTokenId.SUB) != JFXTokenId.SUB) {
                     rollback(index, c, d);
                 }
                 // ---
                 accept(ReformatUtils.NON_STRING_LITERALS);
             }
 
             return true;
         }
 
         @Override
         public Boolean visitSequenceDelete(SequenceDeleteTree node, Void p) {
             accept(JFXTokenId.DELETE);
             space();
             scan(node.getElement(), p);
             space();
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             if (accept(JFXTokenId.FROM) == JFXTokenId.FROM) {
                 space();
             } else {
                 rollback(index, c, d);
             }
             scan(node.getSequence(), p);
             return true;
         }
 
         @Override
         public Boolean visitSequenceEmpty(SequenceEmptyTree node, Void p) {
             accept(JFXTokenId.LBRACKET);
             spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
             accept(JFXTokenId.RBRACKET);
             return true;
         }
 
         @Override
         public Boolean visitSequenceExplicit(SequenceExplicitTree node, Void p) {
             List<ExpressionTree> itemList = node.getItemList();
             accept(JFXTokenId.LBRACKET);
             int old = indent;
             indent += indentSize;
             spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0, true);
             if (itemList != null) {
                 wrapList(cs.wrapSequenceInit(), cs.alignSequenceInit(), false, itemList);
 //                boolean first = true;
 //                for (Iterator<ExpressionTree> it = itemList.iterator(); it.hasNext();) {
 //                    ExpressionTree expressionTree = it.next();
 //                    if (!first) {
 //                        spaces(cs.spaceAfterComma() ? 1 : 0, true);
 //                    }
 //                    scan(expressionTree, p);
 //
 //                    int index = tokens.index();
 //                    int c = col;
 //                    Diff d = diffs.isEmpty() ? null : diffs.getFirst();
 //                    if (accept(JFXTokenId.COMMA) != JFXTokenId.COMMA) {
 //                        rollback(index, c, d);
 //                    }
 //
 //                    first = false;
 //                }
             }
             indent = old;
             spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0, true);
             accept(JFXTokenId.RBRACKET);
             return true;
         }
 
         @Override
         public Boolean visitSequenceIndexed(SequenceIndexedTree node, Void p) {
             scan(node.getSequence(), p);
             accept(JFXTokenId.LBRACKET);
             scan(node.getIndex(), p);
             accept(JFXTokenId.RBRACKET);
             return true;
         }
 
         @Override
         public Boolean visitSequenceInsert(SequenceInsertTree node, Void p) {
             JFXSequenceInsert insertNode = (JFXSequenceInsert) node;
             JFXExpression position = insertNode.getPosition();
 
             accept(JFXTokenId.INSERT);
             space();
             scan(node.getElement(), p);
             space();
             if (position == null) { // INTO
                 accept(JFXTokenId.INTO);
                 space();
                 scan(node.getSequence(), p);
             } else { // BEFORE/AFTER
                 accept(insertNode.shouldInsertAfter() ? JFXTokenId.AFTER : JFXTokenId.BEFORE);
                 space();
                 accept(JFXTokenId.IDENTIFIER);
                 accept(JFXTokenId.LBRACKET);
                 scan(position, p);
                 accept(JFXTokenId.RBRACKET);
             }
             return true;
         }
 
         @Override
         public Boolean visitSequenceRange(SequenceRangeTree node, Void p) {
             accept(JFXTokenId.LBRACKET);
             spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
             scan(node.getLower(), p);
             spaces(cs.spaceAroundRangeOps() ? 1 : 0);
             accept(JFXTokenId.DOTDOT);
             if (node.isExclusive()) {
                 accept(JFXTokenId.LT);
             }
             spaces(cs.spaceAroundRangeOps() ? 1 : 0);
             scan(node.getUpper(), p);
             ExpressionTree stepOrNull = node.getStepOrNull();
             if (stepOrNull != null) {
                 space();
                 accept(JFXTokenId.STEP);
                 space();
                 scan(stepOrNull, p);
             }
             spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
             accept(JFXTokenId.RBRACKET);
             return true;
         }
 
         @Override
         public Boolean visitSequenceSlice(SequenceSliceTree node, Void p) {
             scan(node.getSequence(), p);
             accept(JFXTokenId.LBRACKET);
             spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
             scan(node.getFirstIndex(), p);
             spaces(cs.spaceAroundRangeOps() ? 1 : 0);
 //            scan(node.getEndKind(), p);
             accept(JFXTokenId.DOTDOT);
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             if (accept(JFXTokenId.LT) != JFXTokenId.LT) {
                 rollback(index, c, d);
             }
             spaces(cs.spaceAroundRangeOps() ? 1 : 0);
             scan(node.getLastIndex(), p);
             accept(JFXTokenId.RBRACKET);
             return true;
         }
 
         @Override
         public Boolean visitErroneous(ErroneousTree node, Void p) {
             for (Tree tree : node.getErrorTrees()) {
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
 
         @Override
         public Boolean visitOverrideClassVar(OverrideClassVarTree node, Void p) {
             do {
                 col += tokens.token().length();
             } while (tokens.moveNext() && tokens.offset() < endPos);
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
             return true;
         }
 
         @Override
         public Boolean visitVariableInvalidate(VariableInvalidateTree node, Void p) {
             do {
                 col += tokens.token().length();
             } while (tokens.moveNext() && tokens.offset() < endPos);
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
             return true;
         }
 
 
 
         /// ===
         /// === END OF THE VISITOR IMPLEMENTATION
         /// ===
 
         private JFXTokenId accept(JFXTokenId first, JFXTokenId... rest) {
             return accept(EnumSet.of(first, rest));
         }
 
         private JFXTokenId accept(EnumSet<JFXTokenId> tokenIds) {
             lastBlankLines = -1;
             lastBlankLinesTokenIndex = -1;
             lastBlankLinesDiff = null;
 
             // JavaFX Non-reserved keywords feature, see v4Parser.g
             if (tokenIds.contains(JFXTokenId.IDENTIFIER)) {
                 tokenIds.addAll(ReformatUtils.NON_RESERVED_KEYWORDS);
             }
 
             // javafx lexer generates one token for each WS
             // java - one for all
             // therefore StringBuilder is been used instead of Token
             StringBuilder lastWSToken = new StringBuilder(); // TODO do not use text var, use this builder.subSequence
             int after = 0;
             do {
                 if (isTokenOutOfTree()) {
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
                         int idx = tokenText.lastIndexOf(NEWLINE);
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
                         idx = tokenText.lastIndexOf(NEWLINE);
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
 
         private JFXTokenId acceptAndRollback(JFXTokenId tokenId) {
             return acceptAndRollback(EnumSet.of(tokenId));
         }
 
         private JFXTokenId acceptAndRollback(EnumSet<JFXTokenId> tokenIds) {
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             JFXTokenId tokenId = accept(tokenIds);
             rollback(index, c, d);
             return tokenId;
         }
 
         // TODO uncomment it after switching to SoMa
         private boolean isTokenOutOfTree() {
 //            return tokens.offset() >= endPos;
             return false;
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
                 if (isTokenOutOfTree()) {
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
                                 int idx = text.lastIndexOf(NEWLINE);
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
                                 int idx = text.lastIndexOf(NEWLINE);
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
                         int idx = tokenText.lastIndexOf(NEWLINE);
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
                                 idx = text.lastIndexOf(NEWLINE);
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
                         idx = tokenText.lastIndexOf(NEWLINE);
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
                                 idx = text.lastIndexOf(NEWLINE);
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
                 if (isTokenOutOfTree()) {
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
                             while (count != 0 && (idx = text.indexOf(NEWLINE, lastIdx)) >= 0) {
                                 if (idx > lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                 }
                                 lastIdx = idx + 1;
                                 count--;
                             }
                             if ((idx = text.lastIndexOf(NEWLINE)) >= 0) {
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
                             while (count != 0 && (idx = text.indexOf(NEWLINE, lastIdx)) >= 0) {
                                 if (idx > lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                 }
                                 lastIdx = idx + 1;
                                 count--;
                             }
                             if ((idx = text.lastIndexOf(NEWLINE)) >= 0) {
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
                             if (count >= 0 && tokens.index() > 1 && after != 1 && text.indexOf(NEWLINE) >= 0) {
                                 count++;
                             }
                             int idx = 0;
                             int lastIdx = 0;
                             while (count != 0 && (idx = text.indexOf(NEWLINE, lastIdx)) >= 0) {
                                 if (idx > lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx, null));
                                 }
                                 lastIdx = idx + 1;
                                 count--;
                             }
                             if ((idx = text.lastIndexOf(NEWLINE)) >= 0) {
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
                             while (count != 0 && (idx = text.indexOf(NEWLINE, lastIdx)) >= 0) {
                                 if (idx > lastIdx) {
                                     addDiff(new Diff(offset + lastIdx, offset + idx, templateEdit ? getIndent() : null));
                                 }
                                 lastIdx = idx + 1;
                                 count--;
                             }
                             if ((idx = text.lastIndexOf(NEWLINE)) >= 0) {
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
 
         private int wrapExtendsTree(CodeStyle.WrapStyle wrapStyle, int alignIndent, int spacesCnt, Tree tree) {
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
                     accept(JFXTokenId.IDENTIFIER);
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
                     accept(JFXTokenId.IDENTIFIER);
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
                         accept(JFXTokenId.IDENTIFIER);
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
                     accept(JFXTokenId.IDENTIFIER);
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
                     processOperator(spacesCnt);
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
                     processOperator(spacesCnt);
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
                         processOperator(spacesCnt);
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
                     processOperator(spacesCnt);
                     spaces(spacesCnt);
                     scan(tree, null);
                     break;
             }
             return ret;
         }
 
         private void processOperator(int spacesCnt) {
             if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                 col += tokens.token().length();
                 lastBlankLines = -1;
                 lastBlankLinesTokenIndex = -1;
                 tokens.moveNext();
             } else {
                 int index = tokens.index();
                 int c = col;
                 Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                 final JFXTokenId accept = accept(JFXTokenId.AND, JFXTokenId.OR, JFXTokenId.MOD);
                 if (accept != JFXTokenId.AND && accept != JFXTokenId.OR && accept != JFXTokenId.MOD) {
                     rollback(index, c, d);
                 }
             }
         }
 
         private boolean wrapStatement(CodeStyle.WrapStyle wrapStyle, CodeStyle.BracesGenerationStyle bracesGenerationStyle, int spacesCnt, ExpressionTree tree) {
             if (tree.getJavaFXKind() == JavaFXKind.EMPTY_STATEMENT) {
                 scan(tree, null);
                 return true;
             }
             if (tree.getJavaFXKind() == JavaFXKind.BLOCK_EXPRESSION) {
                 // check this elimination
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
             // TODO generate braces if parent is function or class
 //            if (bracesGenerationStyle == CodeStyle.BracesGenerationStyle.GENERATE) {
 //                scan(new FakeBlock(tree), null);
 //                return true;
 //            }
             int old = indent;
             indent += indentSize;
             wrapTree(wrapStyle, -1, spacesCnt, tree);
             indent = old;
             return false;
         }
 
         private void wrapList(CodeStyle.WrapStyle wrapStyle, boolean align, boolean prependSpace, List<? extends Tree> trees) {
             boolean first = true;
             int alignIndent = -1;
             for (Iterator<? extends Tree> it = trees.iterator(); it.hasNext();) {
                 Tree tree = it.next();
                 if (tree.getJavaFXKind() == JavaFXKind.ERRONEOUS) {
                     scan(tree, null);
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
                     scan(tree, null);
                     if (wrapStyle != CodeStyle.WrapStyle.WRAP_NEVER && col > rightMargin && c > indent && (wrapDepth == 0 || c <= rightMargin)) {
                         rollback(index, c, d);
                         newline();
                         scan(tree, null);
                     }
                 } else {
                     wrapTree(wrapStyle, alignIndent, cs.spaceAfterComma() ? 1 : 0, tree);
                 }
                 first = false;
 
                 int index = tokens.index();
                 int c = col;
                 Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                 JFXTokenId accepted = accept(JFXTokenId.COMMA);
                 rollback(index, c, d);
                 if (accepted == JFXTokenId.COMMA) {
                     spaces(cs.spaceBeforeComma() ? 1 : 0);
                     accept(JFXTokenId.COMMA);
                 }
             }
         }
 
         private void wrapExtendsList(CodeStyle.WrapStyle wrapStyle, boolean align, boolean prependSpace, List<? extends Tree> trees) {
             boolean first = true;
             int alignIndent = -1;
             for (Iterator<? extends Tree> it = trees.iterator(); it.hasNext();) {
                 Tree tree = it.next();
                 if (tree.getJavaFXKind() == JavaFXKind.ERRONEOUS) {
                     scan(tree, null);
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
                     accept(JFXTokenId.IDENTIFIER);
                     if (wrapStyle != CodeStyle.WrapStyle.WRAP_NEVER && col > rightMargin && c > indent && (wrapDepth == 0 || c <= rightMargin)) {
                         rollback(index, c, d);
                         newline();
                         accept(JFXTokenId.IDENTIFIER);
                     }
                 } else {
                     wrapExtendsTree(wrapStyle, alignIndent, cs.spaceAfterComma() ? 1 : 0, tree);
                 }
                 first = false;
                 if (it.hasNext()) {
                     spaces(cs.spaceBeforeComma() ? 1 : 0);
                     accept(JFXTokenId.COMMA);
                 }
             }
         }
 
         private void wrapFunctionalParamList(CodeStyle.WrapStyle wrapStyle, boolean align, List<? extends TypeTree> trees) {
             boolean first = true;
             int alignIndent = -1;
             for (Iterator<? extends TypeTree> it = trees.iterator(); it.hasNext();) {
                 // issue #176906
                 // accept formal parameter name if exist
                 int index = tokens.index();
                 int c = col;
                 Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                 if (accept(JFXTokenId.IDENTIFIER) != JFXTokenId.IDENTIFIER) {
                     rollback(index, c, d);
                 }
                 
                 accept(JFXTokenId.COLON);
                 spaces(cs.spaceAroundAssignOps() ? 1 : 0); // TODO space around colon in the type definition
 
                 TypeTree param = it.next();
                 if (param.getJavaFXKind() == JavaFXKind.ERRONEOUS) {
                     scan(param, null);
                 } else if (first) {
                     index = tokens.index();
                     c = col;
                     d = diffs.isEmpty() ? null : diffs.getFirst();
                     if (align) {
                         alignIndent = col;
                     }
                     scan(param, null);
                     if (wrapStyle != CodeStyle.WrapStyle.WRAP_NEVER && col > rightMargin && c > indent && (wrapDepth == 0 || c <= rightMargin)) {
                         rollback(index, c, d);
                         newline();
                         scan(param, null);
                     }
                 } else {
                     wrapTree(wrapStyle, alignIndent, cs.spaceAfterComma() ? 1 : 0, param);
                 }
                 first = false;
                 if (it.hasNext()) {
                     spaces(cs.spaceBeforeComma() ? 1 : 0);
                     accept(JFXTokenId.COMMA);
                 }
             }
         }
 
         private void wrapLiteralList(CodeStyle.WrapStyle wrapStyle, boolean align, Set<Tree> trees) {
             boolean first = true;
             int alignIndent = -1;
             for (Iterator<Tree> it = trees.iterator(); it.hasNext();) {
                 Tree part = it.next();
                 if (part.getJavaFXKind() == JavaFXKind.ERRONEOUS) {
                     scan(part, null);
                 } else if (first) {
                     int index = tokens.index();
                     int c = col;
                     Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                     if (align) {
                         alignIndent = col;
                     }
                     scan(part, null);
                     if (wrapStyle != CodeStyle.WrapStyle.WRAP_NEVER && col > rightMargin && c > indent && (wrapDepth == 0 || c <= rightMargin)) {
                         rollback(index, c, d);
                         newline();
                         scan(part, null);
                     }
                 } else {
                     wrapTree(wrapStyle, alignIndent, cs.spaceAfterComma() ? 1 : 0, part);
                 }
                 first = false;
 
                 int index = tokens.index();
                 int c = col;
                 Diff d = diffs.isEmpty() ? null : diffs.getFirst();
                 JFXTokenId accepted = accept(JFXTokenId.COMMA, JFXTokenId.SEMI);
                 if (accepted != JFXTokenId.COMMA && accepted != JFXTokenId.SEMI) {
                     rollback(index, c, d);
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
             while ((idx = text.indexOf(NEWLINE, idx)) >= 0) {
                 int i = idx + 1;
                 while (i < text.length() && text.charAt(i) <= ' ' && text.charAt(i) != '\n') { // NOI18N
                     i++;
                 }
                 if (i >= text.length()) {
                     break;
                 }
                 String s = text.charAt(i) == '*' ? _indent + SPACE : _indent; // NOI18N
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
                 sb.append(SPACE);
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
                 sb.append(NEWLINE);
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
                 sb.append(SPACE);
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
                         int idx = text.lastIndexOf(NEWLINE);
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
                 if (c == '\t') { // NOI18N
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
 
         private boolean processSemicolon() {
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             boolean accepted = accept(JFXTokenId.SEMI) == JFXTokenId.SEMI;
             rollback(index, c, d);
             if (accepted) {
                 if (cs.spaceBeforeSemi()) {
                     space();
                 }
                 accept(JFXTokenId.SEMI);
                 if (cs.spaceAfterSemi()) {
                     space();
                 }
             }
             return accepted;
         }
 
         private boolean processColon() {
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             boolean accepted = accept(JFXTokenId.COLON) == JFXTokenId.COLON;
             rollback(index, c, d);
             if (accepted) {
                 if (cs.spaceBeforeColon()) {
                     space();
                 }
                 accept(JFXTokenId.COLON);
                 if (cs.spaceAfterColon()) {
                     space();
                 }
             }
             return accepted;
         }
 
         private void processTranslationKey() {
             int index = tokens.index();
             int c = col;
             Diff d = diffs.isEmpty() ? null : diffs.getFirst();
             if (accept(JFXTokenId.TRANSLATION_KEY) != JFXTokenId.TRANSLATION_KEY) {
                 rollback(index, c, d);
             }
         }
 
         /**
          * Determines whether given tree contains simple for formatting block expression.
          * All block expression owners except class definition should be here.
          * 
          * @param AST tree
          * @return is given block simple
          */
         private static boolean isSimpleBlock(Tree tree) {
             final JavaFXKind kind = tree.getJavaFXKind();
             return kind == JavaFXKind.FUNCTION_DEFINITION
                     || kind == JavaFXKind.INSTANTIATE_OBJECT_LITERAL
                     || kind == JavaFXKind.INIT_DEFINITION
                     || kind == JavaFXKind.POSTINIT_DEFINITION;
         }
 
         private static class FakeBlock extends JFXBlock {
 //            private ExpressionTree stat;
 
             private FakeBlock(ExpressionTree stat) {
                 super(0L, com.sun.tools.mjavac.util.List.of((JFXExpression) stat), (JFXExpression) stat);
 //                this.stat = stat;
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
                 return visit(node.getBody(), p);
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
