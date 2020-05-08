 /*
  *
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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
  *
  */
 
 package org.netbeans.modules.javafx.editor.format;
 
 import com.sun.javafx.api.tree.*;
 import com.sun.tools.javafx.tree.JFXTree;
 import com.sun.tools.javafx.tree.JFXVarScriptInit;
 import org.netbeans.api.java.source.CodeStyle;
 import static org.netbeans.api.java.source.CodeStyle.BracePlacement;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.TreeUtilities;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenId;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.editor.indent.spi.Context;
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.Element;
 import javax.swing.text.Position;
 import java.util.List;
 import java.util.Queue;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 /**
  * Implementation of tree path scanner to work with actual AST to provide formating.
  *
  * @author Rastislav Komara (<a href="mailto:moonko@netbeans.org">RKo</a>)
  */
 class Visitor extends JavaFXTreePathScanner<Queue<Adjustment>, Queue<Adjustment>> {
     private static Logger log = Logger.getLogger(Visitor.class.getName());
     private final TreeUtilities tu;
     private final CompilationInfo info;
     private final Context ctx;
     private int indentOffset = 0;
     private final CodeStyle cs;
     private static final String NEW_LINE_STRING = "\n"; // NOI18N
     //    private static final String NEW_LINE_STRING = System.getProperty("line.separator", "\n"); // NOI18N
     protected final DocumentLinesIterator li;
     private static final String STRING_EMPTY_LENGTH_ONE = " "; // NOI18N
     protected static final String ONE_SPACE = STRING_EMPTY_LENGTH_ONE;
     private TokenSequence<TokenId> ts;
     private static final String STRING_ZERO_LENGTH = ""; // NOI18N
     private boolean disableContinuosIndent;
     private boolean isOrphanObjectLiterar;
     private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org/netbeans/modules/javafx/editor/format/Bundle"); // NOI18N
     private static final String REFORMAT_FAILED_BUNDLE_KEY = "Reformat_failed._";  // NOI18N
 
 
     Visitor(CompilationInfo info, Context ctx, int startOffset) {
         this(info, ctx);
         indentOffset = startOffset;
     }
 
     Visitor(CompilationInfo info, Context ctx) {
         this.info = info;
         this.ctx = ctx;
         tu = new TreeUtilities(info);
         cs = CodeStyle.getDefault(ctx.document());
         li = new DocumentLinesIterator(ctx);
     }
 
     private int getIndentStepLevel() {
         return cs.getIndentSize();
     }
 
 
     @Override
     public Queue<Adjustment> visitInitDefinition(InitDefinitionTree node, Queue<Adjustment> adjustments) {
         try {
             processStandaloneNode(node, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         super.visitInitDefinition(node, adjustments);
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitVariable(VariableTree node, Queue<Adjustment> adjustments) {
         if (node instanceof JFXVarScriptInit) return adjustments;
         try {
             if (isSynthetic((JFXTree) node)) {
                 super.visitVariable(node, adjustments);
                 return adjustments;
             }
             final int start = getStartPos(node);
             if (!holdOnLine(getParent())) {
                 hasComment(node, adjustments);
                 indentLine(start, adjustments);
             }
             verifyVarSpaces(node, adjustments);
             if (isMultiline(node) && node.getOnReplaceTree() == null) {
                 /*li.moveTo(start);
                 if (li.hasNext()) {
                     indentMultiline(li, getEndPos(node), adjustments);
                 }*/
                 indentEndLine(node, adjustments);
             }
 
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         super.visitVariable(node, adjustments);
         return adjustments;
     }
 
     private void hasComment(Tree node, Queue<Adjustment> adjustments) throws BadLocationException {
         hasComment(node, adjustments, (TokenSequence<JFXTokenId>) ts());
     }
 
     private void indentLine(Element line, Queue<Adjustment> adjustments) throws BadLocationException {
         final int ls = ctx.lineStartOffset(line.getStartOffset());
         indentLine(ls, adjustments);
     }
 
     private void indentLine(int ls, Queue<Adjustment> adjustments) throws BadLocationException {
         indentLine(ls, adjustments, indentOffset);
     }
 
     private void indentLine(int ls, Queue<Adjustment> adjustments, int indent) throws BadLocationException {
         if (ctx.lineIndent(ctx.lineStartOffset(ls)) != indent) {
             adjustments.offer(Adjustment.indent(createPosition(ls), indent));
         }
     }
 
     /*    @Override
         public Queue<Adjustment> visitExpressionStatement(ExpressionStatementTree node, Queue<Adjustment> adjustments) {
             try {
                 processStandaloneNode(node, adjustments);
             } catch (BadLocationException e) {
                 if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
             }
             super.visitExpressionStatement(node, adjustments);
             return adjustments;
         }
     */
     private void processStandaloneNode(Tree node, Queue<Adjustment> adjustments) throws BadLocationException {
         final int position = getStartPos(node);
         if (isFirstOnLine(position)) {
             hasComment(node, adjustments);
             indentLine(position, adjustments);
         } else {
             adjustments.offer(Adjustment.add(createPosition(position), NEW_LINE_STRING));
             adjustments.offer(Adjustment.indent(createPosition(position + 1), indentOffset));
         }
         if (isMultiline(node)) {
             indentLine(getEndPos(node), adjustments);
         }
     }
 
     @Override
     public Queue<Adjustment> visitIdentifier(IdentifierTree node, Queue<Adjustment> adjustments) {
         try {
 //            if (isWidow(node)) {
             if (isFirstOnLine(getStartPos(node))
                     && getCurrentPath().getParentPath().getLeaf().getJavaFXKind() != Tree.JavaFXKind.INSTANTIATE_OBJECT_LITERAL) {
                 final int position = getStartPos(node);
                 indentLine(position, adjustments);
                 hasComment(node, adjustments);
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         super.visitIdentifier(node, adjustments);
         return adjustments;
     }
 
 //    private boolean isWidow(Tree node) throws BadLocationException {
 //        final int endPos = getEndPos(node);
 //        int start = getStartPos(node);
 //
 //        return isWidow(endPos, start);
 //    }
 
     private boolean isWidow(int endPos, int start) throws BadLocationException {
         boolean probablyWidow = false;
         final TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) ts();
         ts.move(endPos);
         while (ts.moveNext()) {
             if (JFXTokenId.WS == ts.token().id()) {
                 if (NEW_LINE_STRING.equals(ts.token().text().toString())) {
                     probablyWidow = true;
                     break;
                 }
                 continue;
             }
             break;
         }
 
         return probablyWidow && isFirstOnLine(start);
     }
 
     @Override
     public Queue<Adjustment> visitUnary(UnaryTree node, Queue<Adjustment> adjustments) {
         return super.visitUnary(node, adjustments);
     }
 
     @Override
     public Queue<Adjustment> visitBinary(BinaryTree node, Queue<Adjustment> adjustments) {
         final Tree tree = getParent();
         if (tree instanceof BinaryTree) {
             super.visitBinary((BinaryTree) tree, adjustments);
             return adjustments;
         }
         try {
             boolean blockRetVal = tree instanceof BlockExpressionTree && ((BlockExpressionTree) tree).getValue() == node;
             if (!holdOnLine(tree) && !blockRetVal) {
                 hasComment(node, adjustments);
                 processStandaloneNode(node, adjustments);
             } else if (blockRetVal) {
                 indentReturn(node, adjustments);
             }
             final int offset = getStartPos(node);
             final int end = getEndPos(node);
             final TokenSequence<JFXTokenId> ts = ts(node);
             ts.move(offset);
             while (ts.moveNext() && ts.offset() <= end) {
                 if ("operator".equals(ts.token().id().primaryCategory())) {
                     if (cs.spaceAroundBinaryOps()) {
                         int operatorOffset = ts.offset();
                         if (ts.movePrevious() && ts.token().id() != JFXTokenId.WS) {
                             adjustments.offer(Adjustment.add(createPosition(operatorOffset), ONE_SPACE));
                         }
                         if (!ts.moveNext())
                             throw new BadLocationException(BUNDLE.getString("Concurent_modification_has_occured_on_document."), ts.offset()); // NOI18N
                         if (ts.moveNext() && ts.token().id() != JFXTokenId.WS) {
                             adjustments.offer(Adjustment.add(createPosition(ts.offset()), ONE_SPACE));
                         }
                     }
                 }
             }
 
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);   // NOI18N
         }
         super.visitBinary(node, adjustments);
         return adjustments;
     }
 
     private void indentReturn(Tree node, Queue<Adjustment> adjustments) throws BadLocationException {
         TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) ts();
         int start = getStartPos(node);
         ts.move(start);
         boolean terminate;
         while (ts.movePrevious()) {
             switch (ts.token().id()) {
                 case WS:
                     if ("\n".equals(ts.token().text())) {
                         terminate = true;
                         break;
                     }
                     continue;
                 case RETURN:
                     start = ts.offset();
                     terminate = true;
                     break;
                 default:
                     terminate = true;
                     break;
             }
             if (terminate) break;
         }
         if (isFirstOnLine(start)) {
             hasComment(adjustments, ts, start);
             indentLine(start, adjustments);
         } else {
             adjustments.offer(Adjustment.add(createPosition(start), NEW_LINE_STRING));
             adjustments.offer(Adjustment.indent(createPosition(start + 1), indentOffset));
         }
     }
 
     private TokenSequence<JFXTokenId> ts(Tree node) {
         return tu.tokensFor(node);
     }
 
     @SuppressWarnings({"MethodWithMoreThanThreeNegations"})
     @Override
     public Queue<Adjustment> visitObjectLiteralPart(ObjectLiteralPartTree node, Queue<Adjustment> adjustments) {
         try {
             final int offset = getStartPos(node);
             if (isFirstOnLine(offset)) {
                 hasComment(node, adjustments);
                 indentLine(offset, adjustments);
             } else if (!isOrphanObjectLiterar) {
                 adjustments.offer(Adjustment.add(createPosition(offset), NEW_LINE_STRING));
                 adjustments.offer(Adjustment.indent(createPosition(offset + 1), indentOffset));
             }
             boolean hasContinuosIndent = isMultiline(node) && !isOnSameLine(node, node.getExpression());
             if (hasContinuosIndent) {
                 indentOffset = indentOffset + getCi();
             }
             super.visitObjectLiteralPart(node, adjustments);
             verifyBraces(node, adjustments, cs.getMethodDeclBracePlacement(), cs.spaceBeforeMethodDeclLeftBrace(), false);
             if (isMultiline(node) && !endsOnSameLine(node, getParent()) && !isSpecialCase(node)) {
                 indentEndLine(node, adjustments);
             }
             //verify spaces ocurence.
             verifyOLPTSpaces(node, adjustments);
             if (hasContinuosIndent) {
                 indentOffset = indentOffset - getCi();
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
 
         return adjustments;
     }
 
     private boolean isSpecialCase(ObjectLiteralPartTree node) {
         if (node.getExpression() instanceof ForExpressionTree) {
             ForExpressionTree t = (ForExpressionTree) node.getExpression();
             return !(t.getBodyExpression() instanceof BlockExpressionTree);
         }
         return false;
     }
 
     private void verifyOLPTSpaces(ObjectLiteralPartTree node, Queue<Adjustment> adjustments) throws BadLocationException {
         TokenSequence<JFXTokenId> ts = ts(node);
         // INDETIFIER WS* COLON WS+ ANY
         verifySpacesAroundColon(adjustments, ts);
     }
 
     @SuppressWarnings({"MethodWithMultipleLoops"})
     // NOI18N
     private void verifyVarSpaces(VariableTree node, Queue<Adjustment> adjustments) throws BadLocationException {
         TokenSequence<JFXTokenId> ts = ts(node);
         while (ts.moveNext()) {
             JFXTokenId id = ts.token().id();
             switch (id) {
                 case WS:
                 case PUBLIC:
                 case PUBLIC_INIT:
                 case PUBLIC_READ:
                 case PUBLIC_READABLE:
                 case PRIVATE:
                 case STATIC:
                 case PROTECTED:
                 case DEF:
                 case VAR:
                 case NON_WRITABLE:
                 case OVERRIDE:
                 case READABLE:
                 case REPLACE:
                     continue;
                 case IDENTIFIER:
                     ts.movePrevious();
                     verifySpacesAroundColon(adjustments, ts);
                     return;
                 default:
                     return;
 
             }
         }
     }
 
     @SuppressWarnings({"MethodWithMultipleLoops", "OverlyNestedMethod", "OverlyComplexMethod", // NOI18N
             "MethodWithMoreThanThreeNegations", "OverlyLongMethod" }) // NOI18N
     private void verifySpacesAroundColon(Queue<Adjustment> adjustments, TokenSequence<JFXTokenId> ts) throws BadLocationException {
         // INDETIFIER WS* (COLON|EQ) WS+ ANY
         if (ts.moveNext()) {
             if (ts.token().id() == JFXTokenId.IDENTIFIER) {
                 int start = ts.offset() + ts.token().length();
                 boolean terminate = false;
                 while (!terminate && ts.moveNext()) {
                     JFXTokenId id = ts.token().id();
                     switch (id) {
                         case WS:
                             continue;
                         case COLON: {
                             if (ts.offset() != start) {
                                 adjustments.offer(Adjustment.delete(createPosition(start), createPosition(ts.offset())));
                             }
                             terminate = true;
                             break;
                         }
                         case EQ: {
                             int length = ts.offset() - start;
                             if (cs.spaceAroundAssignOps()) {
                                 if (length != 1) {
                                     adjustments.offer(Adjustment.replace(createPosition(start), createPosition(ts.offset()), ONE_SPACE));
                                 }
                             } else if (length != 0) {
                                 adjustments.offer(Adjustment.delete(createPosition(start), createPosition(ts.offset())));
                             }
                             terminate = true;
                             break;
                         }
                         default: return;
                     }
                 }
                 // verifying spaces beyond COLON
                 start = ts.offset() + ts.offsetToken().length();
                 skipWS(ts);
                 if (ts.offset() - start > 1) {
                     adjustments.offer(Adjustment.replace(createPosition(start), createPosition(ts.offset()), STRING_EMPTY_LENGTH_ONE));
                 } else if (ts.offset() == start) {
                     adjustments.offer(Adjustment.add(createPosition(ts.offset()), STRING_EMPTY_LENGTH_ONE));
                 }
 
             }
         }
     }
 
     private void skipWS(TokenSequence<JFXTokenId> ts, boolean forward) {
         while ((forward ? ts.moveNext() : ts.movePrevious()) && ts.token().id() == JFXTokenId.WS) {
         }
     }
 
     private void skipWS(TokenSequence<JFXTokenId> ts) {
         skipWS(ts, true);
     }
 
     private Position createPosition(int offset) throws BadLocationException {
         return ctx.document().createPosition(offset);
     }
 
     private Tree getParent() {
         return getCurrentPath().getParentPath().getLeaf();
     }
 
     @Override
     public Queue<Adjustment> visitOnReplace(OnReplaceTree node, Queue<Adjustment> adjustments) {
         try {
             hasComment(node, adjustments);
             int start = getStartPos(node);
             boolean firstOnLine = isFirstOnLine(start);
             if (firstOnLine) {
                 indentOffset += getCi();
                 indentLine(start, adjustments);
             }
             super.visitOnReplace(node, adjustments);
             if (firstOnLine) {
                 indentOffset -= getCi();
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         return adjustments;
     }
 
     private boolean isOnSameLine(Tree node, Tree tree) throws BadLocationException {
         int index1 = getStartPos(node);
         int index2 = getStartPos(tree);
         return isOnSameLine(index1, index2);
     }
 
     private boolean isOnSameLine(int index1, int index2) throws BadLocationException {
         return ctx.lineStartOffset(index1) == ctx.lineStartOffset(index2);
     }
 
 
     @Override
     public Queue<Adjustment> visitSequenceDelete(SequenceDeleteTree node, Queue<Adjustment> adjustments) {
         try {
             indentSimpleStructure(node, adjustments);
             incIndent();
             super.visitSequenceDelete(node, adjustments);
             decIndent();
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         return adjustments;
     }
 
     private void decIndent() {
         indentOffset = indentOffset - getIndentStepLevel();
     }
 
     private void incIndent() {
         indentOffset = indentOffset + getIndentStepLevel();
     }
 
     private void indentSimpleStructure(Tree node, Queue<Adjustment> adjustments) throws BadLocationException {
         int start = getStartPos(node);
         if (isFirstOnLine(start)) {
             hasComment(node, adjustments);
             indentLine(start, adjustments);
         }
         if (isMultiline(node)) {
             indentLine(getEndPos(node), adjustments);
         }
 
     }
 
     @Override
     public Queue<Adjustment> visitIndexof(IndexofTree node, Queue<Adjustment> adjustments) {
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitEmptyStatement(EmptyStatementTree node, Queue<Adjustment> adjustments) {
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitBreak(BreakTree node, Queue<Adjustment> adjustments) {
         return adjustments;
     }
 
     /*    @Override
         public Queue<Adjustment> visitPrimitiveType(PrimitiveTypeTree node, Queue<Adjustment> adjustments) {
             return adjustments;
         }
     */
     @Override
     public Queue<Adjustment> visitContinue(ContinueTree node, Queue<Adjustment> adjustments) {
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitSequenceEmpty(SequenceEmptyTree node, Queue<Adjustment> adjustments) {
         try {
             indentSimpleStructure(node, adjustments);
             incIndent();
             super.visitSequenceEmpty(node, adjustments);
             decIndent();
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitSequenceExplicit(SequenceExplicitTree node, Queue<Adjustment> adjustments) {
         try {
             disableContinuosIndent = true;
 //            indentSimpleStructure(node, adjustments);
             int start = getStartPos(node);
             if (isFirstOnLine(start)) {
                 hasComment(node, adjustments);
                 indentLine(start, adjustments);
             }
 
             ExpressionTree tree = null;
             List<ExpressionTree> trees = node.getItemList();
             if (!trees.isEmpty()) {
                 tree = trees.get(trees.size() - 1);
             }
             boolean format = tree != null/* && !endsOnSameLine(tree, node)*/;
             if (isMultiline(node) && format) {
                 indentLine(getEndPos(node), adjustments);
             }
             incIndent();
             super.visitSequenceExplicit(node, adjustments);
             decIndent();
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);  // NOI18N
         } finally {
             disableContinuosIndent = false;
         }
         return adjustments;
     }
 
 
     @Override
     public Queue<Adjustment> visitMemberSelect(MemberSelectTree node, Queue<Adjustment> adjustments) {
         return super.visitMemberSelect(node, adjustments);
     }
 
     @Override
     public Queue<Adjustment> visitInterpolateValue(InterpolateValueTree node, Queue<Adjustment> adjustments) {
         try {
             indentSimpleStructure(node, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         super.visitInterpolateValue(node, adjustments);
         return adjustments;
     }
 
     /*
     @Override
     public Queue<Adjustment> visitSequenceIndexed(SequenceIndexedTree node, Queue<Adjustment> adjustments) {
         try {
             indentSimpleStructure(node, adjustments);
             incIndent();
             super.visitSequenceIndexed(node, adjustments);
             decIndent();
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return adjustments;
     }
 */
 
     @Override
     public Queue<Adjustment> visitSequenceSlice(SequenceSliceTree node, Queue<Adjustment> adjustments) {
         try {
             indentSimpleStructure(node, adjustments);
             incIndent();
             super.visitSequenceSlice(node, adjustments);
             decIndent();
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         return adjustments;
     }
 
 //    @Override
 //    public Queue<Adjustment> visitSequenceInsert(SequenceInsertTree node, Queue<Adjustment> adjustments) {
 //        try {
 //            indentSimpleStructure(node, adjustments);
 //            incIndent();
 //            super.visitSequenceInsert(node, adjustments);
 //            decIndent();
 //        } catch (BadLocationException e) {
 //            if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
 //        }
 //        return adjustments;
 //    }
 
     @Override
     public Queue<Adjustment> visitSequenceRange(SequenceRangeTree node, Queue<Adjustment> adjustments) {
         try {
             indentSimpleStructure(node, adjustments);
             incIndent();
             super.visitSequenceRange(node, adjustments);
             decIndent();
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         return adjustments;
     }
 
     private void indentMultiline(LineIterator<Element> li, int endOffset, Queue<Adjustment> adjustments) throws BadLocationException {
 //        final int ci = getCi();
 //        if (li.hasNext()) {
 //            indentOffset = indentOffset + ci;
 //            while (li.hasNext()) {
 //                final Element element = li.next();
 //                if (element.getStartOffset() > endOffset) {
 //                    break;
 //                }
 //                indentLine(element, adjustments);
 //            }
 //            indentOffset = indentOffset - ci;
 //        }
     }
 
     private boolean isMultiline(Tree tree) throws BadLocationException {
         return ctx.lineStartOffset(getStartPos(tree)) != ctx.lineStartOffset(getEndPos(tree));
     }
 
     private boolean holdInvocationChain = false;
 
     private Tree starter;
 
     @Override
     public Queue<Adjustment> visitMethodInvocation(FunctionInvocationTree node, Queue<Adjustment> adjustments) {
         try {
             if (!holdInvocationChain) {
                 if (!holdOnLine(getParent())) {
                     processStandaloneNode(node, adjustments);
                 } else {
                     indentSimpleStructure(node, adjustments);
                 }
                 starter = node;
             }
             List<? extends ExpressionTree> trees = node.getArguments();
             scan(trees, adjustments);
             holdInvocationChain = true;
             scan(node.getMethodSelect(), adjustments);
             holdInvocationChain = (starter != node);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitFunctionValue(FunctionValueTree node, Queue<Adjustment> adjustments) {
         /*final Tree tree = getParent();
         if (tree instanceof FunctionDefinitionTree) {
             super.visitFunctionValue(node, adjustments);
         } else {
             try {
                 if (isFirstOnLine(getStartPos(node))) {
                     hasComment(node, adjustments);
                     indentLine(getStartPos(node), adjustments);
                 }
                 verifyFunctionSpaces(ts(node), node, adjustments);
                 super.visitFunctionValue(node, adjustments);
                 indentLine(getEndPos(node), adjustments);
             } catch (BadLocationException e) {
                 if (log.isLoggable(Level.SEVERE))
                     log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
             }
         }*/
         super.visitFunctionValue(node, adjustments);
         return adjustments;
 
     }
 
     @Override
     public Queue<Adjustment> visitFunctionDefinition(FunctionDefinitionTree node, Queue<Adjustment> adjustments) {
         if (isSynthetic((JFXTree) node)) {
             super.visitFunctionDefinition(node, adjustments);
             return adjustments;
         }
         final TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) ts();
         try {
             int index = node.getModifiers() != null ? getEndPos(node.getModifiers()) : getStartPos(node);
             processStandaloneNode(node, adjustments);
 
             ts.move(index);             
             while (ts.moveNext()) {
                 final JFXTokenId id = ts.token().id();
                 switch (id) {
                     case WS:
                         continue;
                     case FUNCTION:
                         verifyFunctionSpaces(ts, node, adjustments);
                         break;
                     default:
                         break;
                 }
                 break;
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         super.visitFunctionDefinition(node, adjustments);
         return adjustments;
     }
 
     private void hasComment(Tree node, Queue<Adjustment> adjustments, TokenSequence<JFXTokenId> ts) throws BadLocationException {
         int index = getStartPos(node);
         hasComment(adjustments, ts, index);
     }
 
     private void hasComment(Queue<Adjustment> adjustments, TokenSequence<JFXTokenId> ts, int index) throws BadLocationException {
         if (isPrecededByComment(ts, index)) {
             reformatComment(ts, index, adjustments);
         }
     }
 
     private void reformatComment(TokenSequence<JFXTokenId> ts, int pos, Queue<Adjustment> adjustments) throws BadLocationException {
         int start = pos;
         while (isPrecededByComment(ts, start)) {
             start = ts.offset() - 1;
 //            start = ctx.lineStartOffset(ts.offset());
         }
         LineIterator<Element> li = new DocumentLinesIterator(ctx, start);
 //        indentLines(pos, adjustments, li);
         int oldIndent = indentOffset;
         indentCommentLines(ts, pos, adjustments, li, oldIndent);
         indentOffset = oldIndent;
     }
 
     private void indentCommentLines(TokenSequence<JFXTokenId> ts, int pos, Queue<Adjustment> adjustments, LineIterator<Element> li, int oldIndent) throws BadLocationException {
         while (li.hasNext()) {
             Element element = li.get();
             if (element.getEndOffset() <= pos) {
                 indentLine(element, adjustments);
                 int index = element.getStartOffset();
                 ts.move(index);
                 if (ts.moveNext()) {
                     JFXTokenId id = ts.token().id();
                     if (id == JFXTokenId.DOC_COMMENT || id == JFXTokenId.COMMENT) {
                         indentOffset = oldIndent + 1;
                     }
                 }
                 li.next();
             } else {
                 break;
             }
         }
     }
 
     private boolean isPrecededByComment(TokenSequence<JFXTokenId> ts, int pos) {
         ts.move(pos);
         while (ts.movePrevious()) {
             JFXTokenId tid = ts.token().id();
             if (tid == JFXTokenId.WS) continue;
             if (JFXTokenId.isComment(tid)) return true;
             return false;
         }
         return false;
     }
 
     private void verifyFunctionSpaces(TokenSequence<JFXTokenId> ts, Tree node, Queue<Adjustment> adjustments) throws BadLocationException {
         if (ts.moveNext() && ts.token().id() == JFXTokenId.IDENTIFIER) {
             if (cs.spaceBeforeMethodDeclLeftBrace()) {
                 if (ts.moveNext() && ts.token().id() != JFXTokenId.WS) {
                     adjustments.offer(Adjustment.add(createPosition(ts.offset()), ONE_SPACE));
                 } else {
                     verifyNextIs(JFXTokenId.LPAREN, ts, adjustments, true);
                 }
             } else {
                 verifyNextIs(JFXTokenId.LPAREN, ts, adjustments, true);
             }
         }
         verifyBraces(node, adjustments, cs.getMethodDeclBracePlacement(), cs.spaceBeforeMethodDeclLeftBrace(), true);
         if (!holdOnLine(getParent())) {
             processStandaloneNode(node, adjustments);
         }
     }
 
     private void verifyNextIs(JFXTokenId id, TokenSequence<JFXTokenId> ts, Queue<Adjustment> adjustments, boolean moveNext) throws BadLocationException {
         if ((moveNext ? ts.moveNext() : ts.movePrevious()) && ts.token().id() != id) {
             int startOffset = ts.offset() + (moveNext ? 0 : ts.token().length());
             while (moveNext ? ts.moveNext() : ts.movePrevious()) {
                 if (ts.token().id() == id) {
                     adjustments.offer(
                             Adjustment.delete(createPosition(startOffset),
                                     createPosition(ts.offset() + (moveNext ? 0 : ts.token().length()))));
                 }
             }
         }
     }
 
     @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"}) // NOI18N
     private void verifyBraces(Tree node, Queue<Adjustment> adjustments, BracePlacement bp, boolean spaceBeforeLeftBrace, boolean checkEndBrace) throws BadLocationException {
         final TokenSequence<JFXTokenId> ts = tu.tokensFor(node);
         Token<JFXTokenId> obrace = moveTo(ts, JFXTokenId.LBRACE);
         if (obrace != null) {
             int obraceTokenStart = ts.offset();
             boolean nlFound = false;
             while (ts.movePrevious()) {
 
                 if (ts.token().id() != JFXTokenId.WS) {
                     break;
                 } else {
                     final CharSequence cs = ts.token().text();
                     if ("\n".equals(cs.toString())) {
                         nlFound = true;
                     }
                 }
 
             }
 
             final Document doc = ctx.document();
             int oldIndent = indentOffset;
             switch (bp) {
                 case SAME_LINE:
                     if (nlFound || (obraceTokenStart - (ts.offset() + ts.token().length()) > 1)) {
                         adjustments.offer(Adjustment.replace(doc.createPosition(ts.offset() + ts.token().length()),
                                 doc.createPosition(obraceTokenStart),
                                 spaceBeforeLeftBrace ? ONE_SPACE : STRING_ZERO_LENGTH));
                     }
                     break;
                 case NEW_LINE:
                     verifyNL(adjustments, ts, obraceTokenStart, nlFound);
                     break;
                 case NEW_LINE_HALF_INDENTED:
                     verifyNL(adjustments, ts, obraceTokenStart, nlFound);
                     indentOffset = indentOffset + (getIndentStepLevel() / 2);
                     adjustments.offer(Adjustment.indent(doc.createPosition(obraceTokenStart), indentOffset));
                     break;
                 case NEW_LINE_INDENTED:
                     verifyNL(adjustments, ts, obraceTokenStart, nlFound);
                     incIndent();
                     adjustments.offer(Adjustment.indent(doc.createPosition(obraceTokenStart), indentOffset));
                     break;
 
             }
 
             if (checkEndBrace) {
                 checkEndBrace(node, adjustments, ts);
             }
 //            indentEndLine(node, adjustments);
             indentOffset = oldIndent;
         }
 
     }
 
     private void checkEndBrace(Tree node, Queue<Adjustment> adjustments, TokenSequence<JFXTokenId> ts) throws BadLocationException {
         if (log.isLoggable(Level.INFO)) log.info("isOrphanObjectLiterar? " + isOrphanObjectLiterar);
         if (isOrphanObjectLiterar) return;
         Document doc = ctx.document();
         final int end = getEndPos(node);
         ts.move(end); //getting into last token...
         if (ts.movePrevious() && ts.token().id() != JFXTokenId.RBRACE) {
             return;
         }
         while (ts.movePrevious()) {
             final JFXTokenId id = ts.token().id();
             switch (id) {
                 case WS: {
                     final CharSequence cs = ts.token().text();
                     if (cs != null && "\n".equals(cs.toString())) {
                         indentEndLine(node, adjustments);
                         return;
                     }
                     continue;
                 }
                 default: {
                     adjustments.offer(Adjustment.add(doc.createPosition(end - 1), NEW_LINE_STRING));
                     adjustments.offer(Adjustment.indent(doc.createPosition(end), indentOffset));
                     return;
                 }
             }
 
         }
     }
 
     private SourcePositions sp() {
         return info.getTrees().getSourcePositions();
     }
 
     private void verifyNL(Queue<Adjustment> adjustments, TokenSequence<JFXTokenId> ts, int originTokenStart, boolean nlFound) throws BadLocationException {
         if (!nlFound || (originTokenStart - (ts.offset() + ts.token().length()) > 1)) {
             adjustments.offer(Adjustment.replace(createPosition(ts.offset() + ts.token().length()),
                     createPosition(originTokenStart), NEW_LINE_STRING));
         }
     }
 
     private Token<JFXTokenId> moveTo(TokenSequence<JFXTokenId> ts, JFXTokenId id) {
         while (ts.moveNext()) {
             if (ts.token().id() == id) {
                 return ts.token();
             }
         }
         return null;
     }
 
     private UnitTree cu() {
         return info.getCompilationUnit();
     }
 
     @SuppressWarnings({"unchecked"}) // NOI18N
     private /*<T extends TokenId> */TokenSequence<? extends TokenId> ts() {
         if (ts != null && ts.isValid()) {
             return (TokenSequence) ts;
         }
         this.ts = info.getTokenHierarchy().tokenSequence(JFXTokenId.language());
         return (TokenSequence) this.ts;
     }
 
     @Override
     public Queue<Adjustment> visitAssignment(AssignmentTree node, Queue<Adjustment> adjustments) {
         try {
             processStandaloneNode(node, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);  // NOI18N
         }
         super.visitAssignment(node, adjustments);
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitInstantiate(InstantiateTree node, Queue<Adjustment> adjustments) {
         try {
             final int offset = getStartPos(node);
             final Tree tree = getParent();
             boolean hold = holdOnLine(tree);
             boolean blockRetVal = tree instanceof BlockExpressionTree && ((BlockExpressionTree) tree).getValue() == node;
             if (!blockRetVal && (!hold || isFirstOnLine(offset))) {
                 processStandaloneNode(node, adjustments);
             } else if (blockRetVal) {
                 indentReturn(node, adjustments);
             }
             incIndent();
             isOrphanObjectLiterar = node.getLiteralParts() == null || node.getLiteralParts().size() == 1;
             super.visitInstantiate(node, adjustments);
             decIndent();
 //            if (isOrphanObjectLiterar) {
 //                //TODO: [RKo] keep on same line!.
 //                int endPos = getEndPos(node);
 //                if (!isOnSameLine(offset, endPos)) {
 //                    TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) ts();
 //                    ts.move(endPos);
 //                    if (ts.movePrevious()) {
 //                        skipWS(ts, false);
 //                        if (ts.moveNext()) {
 //                            adjustments.offer(Adjustment.delete(createPosition(ts.offset()), createPosition(endPos - 1 )));
 //                        }
 //                    }
 //                }
 //            } else if (isMultiline(node) && !endsOnSameLine(tree, node)) {
                 indentEndLine(node, adjustments);
 //            }
             isOrphanObjectLiterar = false;
 
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         return adjustments;
     }
 
     private boolean endsOnSameLine(Tree tree, Tree node) throws BadLocationException {
         if (isSynthetic((JFXTree) tree) || isSynthetic((JFXTree) node)) return false;
         return isOnSameLine(getEndPos(node), getEndPos(tree));
     }
 
     private boolean holdOnLine(Tree tree) {
         return tree instanceof ReturnTree
                 || tree instanceof VariableTree
                 || tree instanceof AssignmentTree
                 || tree instanceof UnaryTree
                 || tree instanceof BinaryTree
                 || tree instanceof CatchTree
                 || tree instanceof ConditionalExpressionTree
                 || tree instanceof ObjectLiteralPartTree
                 || tree instanceof FunctionValueTree
                 || tree instanceof SequenceExplicitTree
                 || tree instanceof SequenceInsertTree
                 || tree instanceof SequenceDeleteTree
                 || tree instanceof FunctionInvocationTree
                 || tree instanceof OnReplaceTree
                 || tree instanceof ForExpressionInClauseTree;
     }
 
     private void indentEndLine(Tree node, Queue<Adjustment> adjustments) throws BadLocationException {
         final int endPos = getEndPos(node);
         final TokenSequence<JFXTokenId> ts = ts(node);
         ts.move(endPos);
         boolean shouldIndent = false;
         while (ts.movePrevious()) {
             final JFXTokenId id = ts.token().id();
             switch (id) {
                 case RBRACE:
                 case RPAREN: {
                     shouldIndent = true;
                     continue;
                 }
                 case WS: {
                     final CharSequence cs = ts.token().text();
                     if (cs != null && "\n".equals(cs.toString()) && shouldIndent) {   // NOI18N
                         indentLine(endPos, adjustments);
                         return;
                     }
                     continue;
                 }
                 default:
                     return;
             }
         }
     }
 
     private int getEndPos(Tree node) {
         return (int) sp().getEndPosition(cu(), node);
     }
 
     private int getStartPos(Tree node) {
         int start = (int) sp().getStartPosition(cu(), node);
         //noinspection unchecked
         TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) ts();
         ts.move(start);
         while (ts.movePrevious()) {
             JFXTokenId id = ts.token().id();
             switch (id) {
                 case WS:
                     continue;
                 case LPAREN:
                     return ts.offset();
                 default:
                     return start;
             }
         }
         return start;
         /*int modifiersStart = getModifiersStart(node);
         return Math.min(nodeStart, modifiersStart);*/
     }
 
 /*
     private int getModifiersStart(Tree node) {
         if (node instanceof ClassDeclarationTree) {
             ClassDeclarationTree tree = (ClassDeclarationTree) node;
             return includeModifiers(tree.getModifiers());
         } else if (node instanceof FunctionDefinitionTree) {
             FunctionDefinitionTree tree = (FunctionDefinitionTree) node;
             return includeModifiers(tree.getModifiers());
         } else if (node instanceof VariableTree) {
             VariableTree tree = (VariableTree) node;
             return includeModifiers(tree.getModifiers());
         }
         return Integer.MAX_VALUE;
     }
 */
 
 /*
     private int includeModifiers(ModifiersTree modifiers) {
         final int startPos = getStartPos(modifiers);
         if (startPos < 0) return Integer.MAX_VALUE;
         return startPos;
     }
 */
 
     @Override
     public Queue<Adjustment> visitCompilationUnit(UnitTree node, Queue<Adjustment> adjustments) {
         return super.visitCompilationUnit(node, adjustments);
     }
 
     @SuppressWarnings({"OverlyLongMethod"}) // NOI18N
     @Override
     public Queue<Adjustment> visitClassDeclaration(ClassDeclarationTree node, Queue<Adjustment> adjustments) {
         if (isSynthetic((JFXTree) node)) {
             super.visitClassDeclaration(node, adjustments);
             return adjustments;
         }
         final Document doc = ctx.document();
         try {
             final int sp = getStartPos(node);
             int elc = cs.getBlankLinesBeforeClass();
             processStandaloneNode(node, adjustments);
             if (!isFirstOnLine(sp) && !holdOnLine(getParent())) {
                 adjustments.offer(Adjustment.add(createPosition(sp), buildString(elc, NEW_LINE_STRING).toString()));
                 adjustments.offer(Adjustment.indent(createPosition(sp + elc + 1), indentOffset));
             } else {
                 int pos = ctx.lineStartOffset(sp);
                 indentLine(pos, adjustments);
 
                 final Tree tree = getParent();
                 if (tree instanceof UnitTree || tree instanceof ClassDeclarationTree) {
                     pos = skipPreviousComment(pos);
 
                     int emptyLines = getEmptyLinesBefore(li, pos);
 
                     elc = elc - emptyLines;
 
                     if (elc < 0) {
                         Element nth = getNthElement(Math.abs(elc), li);
                         if (nth != null) {
                             adjustments.offer(Adjustment.delete(doc.createPosition(nth.getStartOffset()), doc.createPosition(pos)));
                         }
                     } else if (elc > 0) {
                         StringBuilder sb = buildString(elc, NEW_LINE_STRING);
                         adjustments.offer(Adjustment.add(doc.createPosition(pos), sb.toString()));
                     }
                 }
             }
 
             incIndent();
             super.visitClassDeclaration(node, adjustments);
             decIndent();
             verifyBraces(node, adjustments, cs.getClassDeclBracePlacement(), cs.spaceBeforeClassDeclLeftBrace(), true);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         return adjustments;
     }
 
     private boolean isSynthetic(JFXTree node) {
 //        JFXTree tree = (JFXTree) getCurrentPath().getLeaf();
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
 
     private Tree firstImport;
     private Tree lastImport;
 
     @Override
     public Queue<Adjustment> visitImport(ImportTree importTree, Queue<Adjustment> adjustments) {
         if (log.isLoggable(Level.FINE)) log.fine("Visiting import" + importTree); // NOI18N
         try {
             final int ls = ctx.lineStartOffset(getStartPos(importTree));
             processStandaloneNode(importTree, adjustments);
             if (firstImport == null) {
                 firstImport = getFirstImport();
             }
             if (lastImport == null) {
                 lastImport = getLastImport();
             }
             if (importTree.equals(firstImport)) {
                 li.moveTo(ls);
                 final int lines = getEmptyLinesBefore(li, ls);
                 final int linesBeforeImports = cs.getBlankLinesBeforeImports();
                 if (linesBeforeImports != lines) {
                     adjustLinesBefore(lines, linesBeforeImports, adjustments, li, ls);
                 }
             }
             if (importTree.equals(lastImport)) {
 
             }
 
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);  // NOI18N
         }
         super.visitImport(importTree, adjustments);
         return adjustments;
     }
 
     private Tree getLastImport() {
         final List<? extends ImportTree> trees = getCurrentPath().getCompilationUnit().getImports();
         if (!trees.isEmpty()) {
             return trees.get(trees.size() - 1);
         }
         return null;
     }
 
     private Tree getFirstImport() {
         final List<? extends ImportTree> trees = getCurrentPath().getCompilationUnit().getImports();
         if (!trees.isEmpty()) {
             return trees.get(0);
         }
         return null;
     }
 
     private void adjustLinesBefore(int realLines, int linesRequired, Queue<Adjustment> list, LineIterator<Element> li, int pos) throws BadLocationException {
         linesRequired = linesRequired - realLines;
 
         if (linesRequired < 0) {
             Element nth = getNthElement(Math.abs(linesRequired), li);
             if (nth != null) {
                 list.add(Adjustment.delete(createPosition(nth.getStartOffset()), createPosition(pos)));
             }
         } else if (linesRequired > 0) {
             StringBuilder sb = buildString(linesRequired, NEW_LINE_STRING);
             list.add(Adjustment.add(createPosition(pos), sb.toString()));
         }
     }
 
     private boolean isFirstOnLine(int offset) throws BadLocationException {
         final int ls = ctx.lineStartOffset(offset);
         final Document doc = ctx.document();
         final String s = doc.getText(ls, offset - ls);
         return isEmpty(s);
 
     }
 
     @Override
     public Queue<Adjustment> visitCompoundAssignment(CompoundAssignmentTree node, Queue<Adjustment> adjustments) {
 /*
         if (getCurrentPath().getParentPath().getLeaf() instanceof ExpressionStatementTree) {
             return adjustments;
         }*/
         try {
             processStandaloneNode(node, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         super.visitCompoundAssignment(node, adjustments);
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitReturn(ReturnTree returnTree, Queue<Adjustment> adjustments) {
         try {
             processStandaloneNode(returnTree, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         super.visitReturn(returnTree, adjustments);
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitStringExpression(StringExpressionTree node, Queue<Adjustment> adjustments) {
         try {
             boolean ml = isMultiline(node);
             if (!(ml || isFirstOnLine(getStartPos(node)))) {
                 return adjustments;
             }
             hasComment(node, adjustments);
             if (ml) {
                 li.moveTo(getStartPos(node));
                 int endPos = getEndPos(node);
                 //second line.
 //                int indent = indentOffset + getCi();
                 incIndent();
                 while (li.hasNext()) {
                     Element element = li.next();
                     if (element.getEndOffset() >= endPos) {
                         break;
                     }
                     indentLine(element.getStartOffset(), adjustments);
                 }
             }
 //            super.visitStringExpression(node, adjustments);
             List<ExpressionTree> trees = node.getPartList();
             trees = trees.subList(1, trees.size() - 2);
             scan(trees, adjustments);
             if (ml) {
                 decIndent();
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitLiteral(LiteralTree literalTree, Queue<Adjustment> adjustments) {
         try {
             final int offset = getStartPos(literalTree);
             if (isFirstOnLine(offset)) {
                 hasComment(literalTree, adjustments);
                 indentLine(offset, adjustments, indentOffset);
 //                indentLine(offset, adjustments, isWidow(literalTree) ? indentOffset : (indentOffset + getCi()));
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e); // NOI18N
         }
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitTimeLiteral(TimeLiteralTree node, Queue<Adjustment> adjustments) {
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitTypeAny(TypeAnyTree node, Queue<Adjustment> adjustments) {
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitTypeUnknown(TypeUnknownTree node, Queue<Adjustment> adjustments) {
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitWhileLoop(WhileLoopTree node, Queue<Adjustment> adjustments) {
         try {
             hasComment(node, adjustments);
             verifySpaceBefore(node, adjustments, cs.spaceBeforeWhile());
             verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeWhileLeftBrace(), true);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);
         }
         super.visitWhileLoop(node, adjustments);
         return adjustments;
     }
 
     private void verifySpaceBefore(Tree node, Queue<Adjustment> adjustments, boolean spaceBefore) throws BadLocationException {
         final int start = getStartPos(node);
         verifySpaceBefore(adjustments, spaceBefore, start);
     }
 
     private void verifySpaceBefore(Queue<Adjustment> adjustments, boolean spaceBefore, int start) throws BadLocationException {
         if (!isFirstOnLine(start) && spaceBefore) {
             //noinspection unchecked
             final TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) ts();
             ts.move(start);
             if (ts.movePrevious() && ts.token().id() != JFXTokenId.WS) {
                 adjustments.offer(Adjustment.add(toPos(start), STRING_EMPTY_LENGTH_ONE));
             }
         }
     }
 
     @Override
     public Queue<Adjustment> visitForExpression(ForExpressionTree node, Queue<Adjustment> adjustments) {
         try {
             if (!holdOnLine(getParent())) {
                 processStandaloneNode(node, adjustments);
                 verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeWhileLeftBrace(), true);
             } 
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);
         }
         boolean followedByBlock = node.getBodyExpression() instanceof BlockExpressionTree;
         if (!followedByBlock) {
             incIndent();
         }
         super.visitForExpression(node, adjustments);
         if (!followedByBlock) {
             decIndent();
         }
         return adjustments;
     }
 
     @Override
 //    public Queue<Adjustment> visitIf(IfTree node, Queue<Adjustment> adjustments) {
     public Queue<Adjustment> visitConditionalExpression(ConditionalExpressionTree node, Queue<Adjustment> adjustments) {
         try {
            if (!(isPreceededByElse(node) || holdOnLine(getParent()))) {
                 processStandaloneNode(node, adjustments);
             }
             verifyBraces(node.getTrueExpression(), adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeIfLeftBrace(), true);
             verifySpaceBefore(node.getFalseExpression(), adjustments, cs.spaceBeforeElse());
             verifyBraces(node.getFalseExpression(), adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeElseLeftBrace(), true);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);
         }
 //        super.visitIf(node, adjustments);
         super.visitConditionalExpression(node, adjustments);
         return adjustments;
     }
 
     private boolean isPreceededByElse(ConditionalExpressionTree node) {
         TokenSequence<? extends TokenId> ts = ts();
         ts.move(getStartPos(node));
         while (ts.movePrevious()) {
             JFXTokenId id = (JFXTokenId) ts.token().id();
             switch (id) {
                 case WS:
                     continue;
                 case ELSE:
                     return true;
                 default:
                     return false;
             }
         }
         return false;
     }
 
     private Position toPos(int index) throws BadLocationException {
         return createPosition(index);
     }
 
     @Override
     public Queue<Adjustment> visitTry(TryTree node, Queue<Adjustment> adjustments) {
         try {
             processStandaloneNode(node, adjustments);
             verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeTryLeftBrace(), true);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);
         }
         super.visitTry(node, adjustments);
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitCatch(CatchTree node, Queue<Adjustment> adjustments) {
         try {
             final int start = getStartPos(node);
             if (cs.placeCatchOnNewLine() && !isFirstOnLine(start)) {
                 adjustments.offer(Adjustment.add(toPos(start), NEW_LINE_STRING));
                 adjustments.offer(Adjustment.indent(toPos(start + 1), indentOffset));
                 verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeCatchLeftBrace(), true);
                 verifyParens(node, adjustments, cs.spaceBeforeCatchParen(), false);
             } else if (!cs.placeCatchOnNewLine() && isFirstOnLine(start)) {
                 final TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) ts();
                 ts.move(start);
                 while (ts.movePrevious()) {
                     if (ts.token().id() == JFXTokenId.RBRACE) {
                         adjustments.offer(Adjustment.replace(toPos(ts.offset() + ts.token().length()),
                                 toPos(start), cs.spaceBeforeCatch() ? STRING_EMPTY_LENGTH_ONE : STRING_ZERO_LENGTH));
                         verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeCatchLeftBrace(), true);
                         verifyParens(node, adjustments, cs.spaceBeforeCatchParen(), false);
                         break;
                     }
                 }
             } else {
                 verifySpaceBefore(node, adjustments, cs.spaceBeforeCatch() && !cs.placeCatchOnNewLine());
                 verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeCatchLeftBrace(), true);
                 verifyParens(node, adjustments, cs.spaceBeforeCatchParen(), cs.spaceWithinCatchParens());
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);
         }
         super.visitCatch(node, adjustments);
         return adjustments;
     }
 
     @SuppressWarnings({"MethodWithMultipleLoops"})
     // NOI18N
     private void verifyParens(Tree node, Queue<Adjustment> adjustments, boolean spaceBeforParen, boolean spaceWithin) throws BadLocationException {
         final TokenSequence<JFXTokenId> ts = ts(node);
         ts.move(getStartPos(node));
         final int endPos = getEndPos(node);
         while (ts.moveNext() && ts.offset() < endPos) {
             JFXTokenId id = ts.token().id();
             if (id == JFXTokenId.LPAREN) {
                 int lps = ts.offset();
                 int tl = ts.token().length();
                 spaceWithin(adjustments, spaceWithin, ts, endPos, lps + tl);
                 ts.move(lps);
                 while (ts.movePrevious()) {
                     id = ts.token().id();
                     if (id != JFXTokenId.WS) {
                         adjustments.offer(Adjustment.replace(toPos(ts.offset() + ts.token().length()),
                                 toPos(lps), spaceBeforParen ? STRING_EMPTY_LENGTH_ONE : STRING_ZERO_LENGTH));
                         return;
                     }
                 }
             }
         }
     }
 
     private void spaceWithin(Queue<Adjustment> adjustments, boolean spaceWithin, TokenSequence<JFXTokenId> ts, int endPos, int parenEndIndex) throws BadLocationException {
         if (spaceWithin) {
             if (ts.moveNext() && ts.token().id() != JFXTokenId.WS) {
                 adjustments.offer(Adjustment.add(toPos(ts.offset()), STRING_EMPTY_LENGTH_ONE));
             }
         } else {
             if (ts.moveNext() && ts.token().id() == JFXTokenId.WS) {
                 while (ts.moveNext() && ts.offset() < endPos) {
                     if (ts.token().id() != JFXTokenId.WS) {
                         adjustments.offer(Adjustment.delete(toPos(parenEndIndex), toPos(ts.offset())));
                         break;
                     }
                 }
             }
         }
     }
 
     @Override
     public Queue<Adjustment> visitPostInitDefinition(InitDefinitionTree node, Queue<Adjustment> adjustments) {
         if (!isSynthetic((JFXTree) node)) {
             try {
                 processStandaloneNode(node, adjustments);
                 verifySpaceBefore(adjustments, cs.spaceBeforeMethodDeclLeftBrace(), getStartPos(node.getBody()));
             } catch (BadLocationException e) {
                 if (log.isLoggable(Level.SEVERE))
                     log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);                
             }
         }
         // POSTINIT is followed by BLOCK so we don't adjust indentation offset.
         super.visitPostInitDefinition(node, adjustments);
         return adjustments; 
     }
 
     /**
      * Gets continuation indent adjustment. This is sum of spaces to be added to current indet to achieve required
      * continuation offset.
      *
      * @return size of continuation indent adjustment.
      */
     private int getCi() {
         return disableContinuosIndent ? 0 : (cs.getContinuationIndentSize() - getIndentStepLevel());
     }
 
     @Override
     public Queue<Adjustment> visitBlockExpression(BlockExpressionTree node, Queue<Adjustment> adjustments) {
         final Tree tree = getParent(); 
         if (isSynthetic((JFXTree) node) || (tree instanceof FunctionValueTree)) {
             super.visitBlockExpression(node, adjustments);
             return adjustments;
         }
         try {
             final int start = ctx.lineStartOffset(getStartPos(node));
 
 /*
             if (isEmptyBlock(node)) {
                 incIndent();
                 li.moveTo(start);
                 int end = findEnd(node);
                 while (li.get().getEndOffset() <= end) {
                     indentLine(li.get(), adjustments);
                     if (li.hasNext()) {
                         li.next();
                     } else {
                         break;
                     }
                 }
             } else {
 */
                 int endPos = getEndPos(node);
                 if (isFirstOnLine(endPos)) {
                     indentLine(start, adjustments);
                 }
                 incIndent();
                 super.visitBlockExpression(node, adjustments);
                 decIndent();
 //                if (isFirstOnLine(endPos) && !endsOnSameLine(node, node.getValue())) {
 //                    final int end = ctx.lineStartOffset(endPos);
 //                    indentLine(end, adjustments);
 //                }
                 indentEndLine(node, adjustments);
 //            }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE))
                 log.severe(BUNDLE.getString(REFORMAT_FAILED_BUNDLE_KEY) + e);
         }
         return adjustments;
     }
 
     private int findEnd(BlockExpressionTree node) {
         TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) ts();
         ts.move(getStartPos(node));
         if (ts.moveNext() && ts.token().id() == JFXTokenId.LBRACE) {
             while (ts.moveNext()) {
                 JFXTokenId id = ts.token().id();
                 switch (id) {
                     case RBRACE:
                         return ts.offset() + 1;
                     case WS:
                         continue;
                     default:
                         return getEndPos(node);
                 }
             }
         }
         return getEndPos(node);
     }
 
     private boolean isEmptyBlock(BlockExpressionTree node) {
         TokenSequence<JFXTokenId> ts = ts(node);
         if (ts.moveNext() && ts.token().id() == JFXTokenId.LBRACE) {
             while (ts.moveNext()) {
                 JFXTokenId id = ts.token().id();
                 switch (id) {
                     case RBRACE:
                         return true;
                     case WS:
                         continue;
                     default:
                         return false;
                 }
             }
         }
         return false;
     }
 
     private int getEmptyLinesBefore(LineIterator<Element> iterator, int pos) throws BadLocationException {
         iterator.moveTo(pos);
         int res = 0;
         while (iterator.hasPrevious()) {
             final Element line = iterator.previous();
             if (!isEmpty(line)) {
                 break;
             }
             res++;
         }
         return res;
     }
 
     private boolean isEmpty(Element line) throws BadLocationException {
         final Document d = line.getDocument();
         final String s = d.getText(line.getStartOffset(), line.getEndOffset() - line.getStartOffset());
         return isEmpty(s);
     }
 
     private int skipPreviousComment(int pos) {
         final TokenSequence<JFXTokenId> ts = (TokenSequence<JFXTokenId>) ts();
         ts.move(pos);
         while (ts.movePrevious()) {
             JFXTokenId tid = ts.token().id();
             if (tid == JFXTokenId.WS) continue;
             if (JFXTokenId.isComment(tid)) return ts.offset();
             return pos;
         }
         return pos;
     }
 
     private Element getNthElement(int n, LineIterator<Element> iterator) {
         Element nth = null;
         while (iterator.hasNext() && n != 0) {
             nth = iterator.next();
             n--;
         }
         return nth;
     }
 
     private StringBuilder buildString(int elc, String str) {
         StringBuilder sb = new StringBuilder(elc);
         while (elc != 0) {
             sb.append(str);
             elc--;
         }
         return sb;
     }
 
 
     private static final Pattern EMPTY_LINE_PTRN = Pattern.compile("\\s+");    // NOI18N
 
     private boolean isEmpty(String text) {
         return text == null || text.length() == 0 || EMPTY_LINE_PTRN.matcher(text).matches();
     }
 
 
 }
