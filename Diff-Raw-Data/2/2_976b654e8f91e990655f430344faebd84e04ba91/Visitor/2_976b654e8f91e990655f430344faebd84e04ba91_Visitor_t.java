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
 import com.sun.source.tree.*;
 import com.sun.source.util.SourcePositions;
 import org.netbeans.api.java.source.CodeStyle;
 import static org.netbeans.api.java.source.CodeStyle.BracePlacement;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.TreeUtilities;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenId;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.api.project.Project;
 import org.netbeans.modules.editor.indent.spi.Context;
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.Element;
 import javax.swing.text.Position;
 import java.util.List;
 import java.util.Queue;
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
     private static final String NEW_LINE_STRING = "\n";
     //    private static final String NEW_LINE_STRING = System.getProperty("line.separator", "\n");
     protected final DocumentLinesIterator li;
     private static final String STRING_EMPTY_LENGTH_ONE = " ";
     protected static final String ONE_SPACE = STRING_EMPTY_LENGTH_ONE;
     private TokenSequence<TokenId> ts;
     private static final String STRING_ZERO_LENGTH = "";
     private boolean disableContinuosIndent;
 
 
     Visitor(CompilationInfo info, Context ctx, int startOffset, Project project) {
         this(info, ctx, project);
         indentOffset = startOffset;
     }
 
     Visitor(CompilationInfo info, Context ctx, Project project) {
         this.info = info;
         this.ctx = ctx;
         tu = new TreeUtilities(info);
         cs = CodeStyle.getDefault(project);
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
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitInitDefinition(node, adjustments);
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitVariable(JavaFXVariableTree node, Queue<Adjustment> adjustments) {
         try {
             final int start = getStartPos(node);
 //            if (isFirstOnLine(start)) {
 //                indentLine(start, adjustments);
 //            }
             if (!holdOnLine(getCurrentPath().getParentPath().getLeaf())) {
                 processStandaloneNode(node, adjustments);
             }
             if (isMultiline(node)) {
                 if (node.getOnReplaceTree() != null) {
                     indentEndLine(node.getOnReplaceTree(), adjustments);
                 } else {
                     li.moveTo(start);
                     if (li.hasNext()) {
                         indentMultiline(li, getEndPos(node), adjustments);
                     }
                 }
             }
 
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitVariable(node, adjustments);
         return adjustments;
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
             adjustments.offer(Adjustment.indent(ctx.document().createPosition(ls), indent));
         }
     }
 
     @Override
     public Queue<Adjustment> visitExpressionStatement(ExpressionStatementTree node, Queue<Adjustment> adjustments) {
         try {
             processStandaloneNode(node, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitExpressionStatement(node, adjustments);
         return adjustments;
     }
 
     private void processStandaloneNode(Tree node, Queue<Adjustment> adjustments) throws BadLocationException {
         final int position = getStartPos(node);
         if (isFirstOnLine(position)) {
             indentLine(position, adjustments);
         } else {
             adjustments.offer(Adjustment.add(ctx.document().createPosition(position), NEW_LINE_STRING));
             adjustments.offer(Adjustment.indent(ctx.document().createPosition(position + 1), indentOffset));
         }
         if (isMultiline(node)) {
             indentLine(getEndPos(node), adjustments);
         }
     }
 
     @Override
     public Queue<Adjustment> visitIdentifier(IdentifierTree node, Queue<Adjustment> adjustments) {
         try {
             if (isWidow(node)) {
                 final int position = getStartPos(node);
                 indentLine(position, adjustments);
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitIdentifier(node, adjustments);
         return adjustments;
     }
 
 
     private boolean isWidow(Tree node) throws BadLocationException {
         final int endPos = getEndPos(node);
         boolean probablyWidow = false;
 
         final TokenSequence<JFXTokenId> ts = ts();
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
 
         return probablyWidow && isFirstOnLine(getStartPos(node));
     }
 
     @Override
     public Queue<Adjustment> visitUnary(UnaryTree node, Queue<Adjustment> adjustments) {
         return super.visitUnary(node, adjustments);
     }
 
     @Override
     public Queue<Adjustment> visitBinary(BinaryTree node, Queue<Adjustment> adjustments) {
         final Tree tree = getCurrentPath().getParentPath().getLeaf();
         if (tree instanceof BinaryTree) {
             super.visitBinary((BinaryTree) tree, adjustments);
             return adjustments;
         }
         try {
             if (!holdOnLine(tree)) {
                 processStandaloneNode(node, adjustments);
             }
             final int offset = getStartPos(node);
             final int end = getEndPos(node);
             if (isMultiline(node)) {
                 indentMultiline(li, end, adjustments);
             }
 
             final TokenSequence<JFXTokenId> ts = ts(node);
             ts.move(offset);
             while (ts.moveNext() && ts.offset() <= end) {
                 if ("operator".equals(ts.token().id().primaryCategory())) {
                     if (cs.spaceAroundBinaryOps()) {
                         int operatorOffset = ts.offset();
                         if (ts.movePrevious() && ts.token().id() != JFXTokenId.WS) {
                             adjustments.offer(Adjustment.add(ctx.document().createPosition(operatorOffset), ONE_SPACE));
                         }
                         if (!ts.moveNext())
                             throw new BadLocationException("Concurent modification has occured on document.", ts.offset());
                         if (ts.moveNext() && ts.token().id() != JFXTokenId.WS) {
                             adjustments.offer(Adjustment.add(ctx.document().createPosition(ts.offset()), ONE_SPACE));
                         }
                     }
                 }
             }
 
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitBinary(node, adjustments);
         return adjustments;
     }
 
     private TokenSequence<JFXTokenId> ts(Tree node) {
         return tu.tokensFor(node);
     }
 
     @Override
     public Queue<Adjustment> visitObjectLiteralPart(ObjectLiteralPartTree node, Queue<Adjustment> adjustments) {
         if (log.isLoggable(Level.INFO)) log.info("entering: visitObjectLiteralPart " + node);
         try {
             final int offset = getStartPos(node);
             if (isFirstOnLine(offset)) {
                 indentLine(offset, adjustments);
             }
             boolean hasContinuosIndent = isMultiline(node) && !isOnSameLine(node, node.getExpression());
             if (hasContinuosIndent) {
                 indentOffset = indentOffset + getCi();
             }
             super.visitObjectLiteralPart(node, adjustments);
             if (hasContinuosIndent) {
                 indentOffset = indentOffset - getCi();
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
 
         if (log.isLoggable(Level.INFO)) log.info("leaving: visitObjectLiteralPart " + node);
         return adjustments;
     }
 
     private boolean isOnSameLine(Tree node, Tree tree) throws BadLocationException {
         return ctx.lineStartOffset(getStartPos(node)) == ctx.lineStartOffset(getStartPos(tree));
     }
 
 
     @Override
     public Queue<Adjustment> visitSequenceDelete(SequenceDeleteTree node, Queue<Adjustment> adjustments) {
         try {
             indentSimpleStructure(node, adjustments);
             incIndent();
             super.visitSequenceDelete(node, adjustments);
             decIndent();
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
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
 //        if (!holdOnLine(node)) {
 //            processStandaloneNode(node, adjustments);
 //        }
         int start = getStartPos(node);
         if (isFirstOnLine(start)) {
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
 
     @Override
     public Queue<Adjustment> visitPrimitiveType(PrimitiveTypeTree node, Queue<Adjustment> adjustments) {
         return adjustments;
     }
 
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
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitSequenceExplicit(SequenceExplicitTree node, Queue<Adjustment> adjustments) {
         try {
             disableContinuosIndent = true;
             indentSimpleStructure(node, adjustments);
             incIndent();
             super.visitSequenceExplicit(node, adjustments);
             decIndent();
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         } finally {
             disableContinuosIndent = false;
         }
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
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
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
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
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
 
     @Override
     public Queue<Adjustment> visitFunctionValue(FunctionValueTree node, Queue<Adjustment> adjustments) {
         final Tree tree = getCurrentPath().getParentPath().getLeaf();
         if (tree instanceof FunctionDefinitionTree) {
             super.visitFunctionValue(node, adjustments);
         } else {
             try {
                 if (isFirstOnLine(getStartPos(node))) {
                     indentLine(getStartPos(node), adjustments);
                 }
                 verifyFunctionSpaces(ts(node), node, adjustments);
                 super.visitFunctionValue(node, adjustments);
                 indentLine(getEndPos(node), adjustments);
             } catch (BadLocationException e) {
                 if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
             }
         }
         return adjustments;
 
     }
 
     @Override
     public Queue<Adjustment> visitMethodInvocation(MethodInvocationTree node, Queue<Adjustment> adjustments) {
         try {
             indentSimpleStructure(node, adjustments);
             incIndent();
             super.visitMethodInvocation(node, adjustments);
             decIndent();
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitFunctionDefinition(FunctionDefinitionTree node, Queue<Adjustment> adjustments) {
         final TokenSequence<JFXTokenId> ts = ts();
         try {
             processStandaloneNode(node, adjustments);
 
             ts.move(getStartPos(node));
             while (ts.moveNext()) {
                 final JFXTokenId id = ts.token().id();
                 switch (id) {
                     case PUBLIC:
                     case PRIVATE:
                     case STATIC:
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
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitFunctionDefinition(node, adjustments);
         return adjustments;
     }
 
     private void verifyFunctionSpaces(TokenSequence<JFXTokenId> ts, Tree node, Queue<Adjustment> adjustments) throws BadLocationException {
         if (ts.moveNext() && ts.token().id() == JFXTokenId.IDENTIFIER) {
             if (cs.spaceBeforeMethodDeclLeftBrace()) {
                 if (ts.moveNext() && ts.token().id() != JFXTokenId.WS) {
                     adjustments.offer(Adjustment.add(ctx.document().createPosition(ts.offset()), ONE_SPACE));
                 } else {
                     verifyNextIs(JFXTokenId.LPAREN, ts, adjustments, true);
                 }
             } else {
                 verifyNextIs(JFXTokenId.LPAREN, ts, adjustments, true);
             }
         }
         verifyBraces(node, adjustments, cs.getMethodDeclBracePlacement(), cs.spaceBeforeMethodDeclLeftBrace());
         if (!holdOnLine(getCurrentPath().getParentPath().getLeaf())) {
             processStandaloneNode(node, adjustments);
         }
     }
 
     private void verifyNextIs(JFXTokenId id, TokenSequence<JFXTokenId> ts, Queue<Adjustment> adjustments, boolean moveNext) throws BadLocationException {
         if ((moveNext ? ts.moveNext() : ts.movePrevious()) && ts.token().id() != id) {
             int startOffset = ts.offset() + (moveNext ? 0 : ts.token().length());
             while (moveNext ? ts.moveNext() : ts.movePrevious()) {
                 if (ts.token().id() == id) {
                     adjustments.offer(
                             Adjustment.delete(ctx.document().createPosition(startOffset),
                                     ctx.document().createPosition(ts.offset() + (moveNext ? 0 : ts.token().length()))));
                 }
             }
         }
     }
 
     @SuppressWarnings({"OverlyComplexMethod"})
     private void verifyBraces(Tree node, Queue<Adjustment> adjustments, BracePlacement bp, boolean spaceBeforeLeftBrace) throws BadLocationException {
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
 
             checkEndBrace(node, adjustments, ts);
             indentOffset = oldIndent;
         }
 
     }
 
     private void checkEndBrace(Tree node, Queue<Adjustment> adjustments, TokenSequence<JFXTokenId> ts) throws BadLocationException {
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
             adjustments.offer(Adjustment.replace(ctx.document().createPosition(ts.offset() + ts.token().length()),
                     ctx.document().createPosition(originTokenStart), NEW_LINE_STRING));
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
 
     private CompilationUnitTree cu() {
         return info.getCompilationUnit();
     }
 
     @SuppressWarnings({"unchecked"})
     private <T extends TokenId> TokenSequence<T> ts() {
         if (ts != null && ts.isValid()) {
             return (TokenSequence<T>) ts;
         }
         this.ts = info.getTokenHierarchy().tokenSequence(JFXTokenId.language());
         return (TokenSequence<T>) this.ts;
     }
 
     @Override
     public Queue<Adjustment> visitAssignment(AssignmentTree node, Queue<Adjustment> adjustments) {
         try {
             processStandaloneNode(node, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitAssignment(node, adjustments);
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitInstantiate(InstantiateTree node, Queue<Adjustment> adjustments) {
         try {
             final int offset = getStartPos(node);
             final Tree tree = getCurrentPath().getParentPath().getLeaf();
             if (!holdOnLine(tree) || isFirstOnLine(offset)) {
                 processStandaloneNode(node, adjustments);
             }
             incIndent();
             super.visitInstantiate(node, adjustments);
             decIndent();
             if (isMultiline(node)) {
                 indentEndLine(node, adjustments);
             }
 
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return adjustments;
     }
 
     private boolean holdOnLine(Tree tree) {
         return tree instanceof ReturnTree
                 || tree instanceof JavaFXVariableTree
                 || tree instanceof AssignmentTree
                 || tree instanceof UnaryTree
                 || tree instanceof BinaryTree
                 || tree instanceof BindExpressionTree
                 || tree instanceof CatchTree
                 || tree instanceof ConditionalExpressionTree
                 || tree instanceof ObjectLiteralPartTree
                 || tree instanceof FunctionValueTree
                 || tree instanceof SequenceExplicitTree
                || tree instanceof SequenceInsertTree
                || tree instanceof SequenceDeleteTree                
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
                     if (cs != null && "\n".equals(cs.toString()) && shouldIndent) {
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
         int nodeStart = (int) sp().getStartPosition(cu(), node);
         int modifiersStart = getModifiersStart(node);
         return Math.min(nodeStart, modifiersStart);
     }
 
     private int getModifiersStart(Tree node) {
         if (node instanceof ClassDeclarationTree) {
             ClassDeclarationTree tree = (ClassDeclarationTree) node;
             return includeModifiers(tree.getModifiers());
         } else if (node instanceof FunctionDefinitionTree) {
             FunctionDefinitionTree tree = (FunctionDefinitionTree) node;
             return includeModifiers(tree.getModifiers());
         } else if (node instanceof JavaFXVariableTree) {
             JavaFXVariableTree tree = (JavaFXVariableTree) node;
             return includeModifiers(tree.getModifiers());
         }
         return Integer.MAX_VALUE;
     }
 
     private int includeModifiers(ModifiersTree modifiers) {
         final int startPos = getStartPos(modifiers);
         if (startPos < 0) return Integer.MAX_VALUE;
         return startPos;
     }
 
     @Override
     public Queue<Adjustment> visitClassDeclaration(ClassDeclarationTree node, Queue<Adjustment> adjustments) {
         if (tu.isSynthetic(getCurrentPath())) {
             super.visitClassDeclaration(node, adjustments);
             return adjustments;
         }
         final Document doc = ctx.document();
         try {
             final int sp = getStartPos(node);
             int elc = cs.getBlankLinesBeforeClass();
             processStandaloneNode(node, adjustments);
             if (!isFirstOnLine(sp) && !holdOnLine(getCurrentPath().getParentPath().getLeaf())) {
                 adjustments.offer(Adjustment.add(ctx.document().createPosition(sp), buildString(elc, NEW_LINE_STRING).toString()));
                 adjustments.offer(Adjustment.indent(ctx.document().createPosition(sp + elc + 1), indentOffset));
             } else {
                 int pos = ctx.lineStartOffset(sp);
                 indentLine(pos, adjustments);
 
                 final Tree tree = getCurrentPath().getParentPath().getLeaf();
                 if (tree instanceof CompilationUnitTree || tree instanceof ClassDeclarationTree) {
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
             verifyBraces(node, adjustments, cs.getClassDeclBracePlacement(), cs.spaceBeforeClassDeclLeftBrace());
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return adjustments;
     }
 
     private Tree firstImport;
     private Tree lastImport;
 
     @Override
     public Queue<Adjustment> visitImport(ImportTree importTree, Queue<Adjustment> adjustments) {
         if (log.isLoggable(Level.FINE)) log.fine("Visiting import" + importTree);
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
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
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
                 list.add(Adjustment.delete(ctx.document().createPosition(nth.getStartOffset()), ctx.document().createPosition(pos)));
             }
         } else if (linesRequired > 0) {
             StringBuilder sb = buildString(linesRequired, NEW_LINE_STRING);
             list.add(Adjustment.add(ctx.document().createPosition(pos), sb.toString()));
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
         if (getCurrentPath().getParentPath().getLeaf() instanceof ExpressionStatementTree) {
             return adjustments;
         }
         try {
             processStandaloneNode(node, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitCompoundAssignment(node, adjustments);
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitReturn(ReturnTree returnTree, Queue<Adjustment> adjustments) {
         try {
             processStandaloneNode(returnTree, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitReturn(returnTree, adjustments);
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitLiteral(LiteralTree literalTree, Queue<Adjustment> adjustments) {
         try {
             final int offset = getStartPos(literalTree);
             if (isFirstOnLine(offset)) {
                 indentLine(offset, adjustments, isWidow(literalTree) ? indentOffset : (indentOffset + getCi()));
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
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
     public Queue<Adjustment> visitBlock(BlockTree blockTree, Queue<Adjustment> adjustments) {
         if (!tu.isSynthetic(getCurrentPath())) {
             try {
                 final int start = ctx.lineStartOffset(getStartPos(blockTree));
                 indentLine(start, adjustments);
                 incIndent();
                 super.visitBlock(blockTree, adjustments);
                 decIndent();
                 verifyBraces(blockTree, adjustments, cs.getOtherBracePlacement(), false);
             } catch (BadLocationException e) {
                 if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
             }
         }
 
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitWhileLoop(WhileLoopTree node, Queue<Adjustment> adjustments) {
         try {
             verifySpaceBefore(node, adjustments, cs.spaceBeforeWhile());
             verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeWhileLeftBrace());
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitWhileLoop(node, adjustments);
         return adjustments;
     }
 
     private void verifySpaceBefore(Tree node, Queue<Adjustment> adjustments, boolean spaceBefore) throws BadLocationException {
         final int start = getStartPos(node);
         if (!isFirstOnLine(start) && spaceBefore) {
             final TokenSequence<JFXTokenId> ts = ts();
             ts.move(start);
             if (ts.movePrevious() && ts.token().id() != JFXTokenId.WS) {
                 adjustments.offer(Adjustment.add(toPos(start), STRING_EMPTY_LENGTH_ONE));
             }
         }
     }
 
     @Override
     public Queue<Adjustment> visitForExpression(ForExpressionTree node, Queue<Adjustment> adjustments) {
         try {
             processStandaloneNode(node, adjustments);
             verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeWhileLeftBrace());
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitForExpression(node, adjustments);
         return adjustments;
     }
 
     @Override
     public Queue<Adjustment> visitIf(IfTree node, Queue<Adjustment> adjustments) {
         try {
             processStandaloneNode(node, adjustments);
             verifyBraces(node.getThenStatement(), adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeIfLeftBrace());
             verifySpaceBefore(node.getElseStatement(), adjustments, cs.spaceBeforeElse());
             verifyBraces(node.getElseStatement(), adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeElseLeftBrace());
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitIf(node, adjustments);
         return adjustments;
     }
 
     private Position toPos(int index) throws BadLocationException {
         return ctx.document().createPosition(index);
     }
 
     @Override
     public Queue<Adjustment> visitTry(TryTree node, Queue<Adjustment> adjustments) {
         try {
             processStandaloneNode(node, adjustments);
             verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeTryLeftBrace());
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
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
                 verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeCatchLeftBrace());
                 verifyParens(node, adjustments, cs.spaceBeforeCatchParen(), false);
             } else if (!cs.placeCatchOnNewLine() && isFirstOnLine(start)) {
                 final TokenSequence<JFXTokenId> ts = ts();
                 ts.move(start);
                 while (ts.movePrevious()) {
                     if (ts.token().id() == JFXTokenId.RBRACE) {
                         adjustments.offer(Adjustment.replace(toPos(ts.offset() + ts.token().length()),
                                 toPos(start), cs.spaceBeforeCatch() ? STRING_EMPTY_LENGTH_ONE : STRING_ZERO_LENGTH));
                         verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeCatchLeftBrace());
                         verifyParens(node, adjustments, cs.spaceBeforeCatchParen(), false);
                         break;
                     }
                 }
             } else {
                 verifySpaceBefore(node, adjustments, cs.spaceBeforeCatch() && !cs.placeCatchOnNewLine());
                 verifyBraces(node, adjustments, cs.getOtherBracePlacement(), cs.spaceBeforeCatchLeftBrace());
                 verifyParens(node, adjustments, cs.spaceBeforeCatchParen(), cs.spaceWithinCatchParens());
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitCatch(node, adjustments);
         return adjustments;
     }
 
     @SuppressWarnings({"MethodWithMultipleLoops"})
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
 
     //    @Override
 //    public Queue<Adjustment> visitStringExpression(StringExpressionTree stringExpressionTree, Queue<Adjustment> adjustments) {
 //        SourcePositions sps = sp();
 //        try {
 //            final int offset = (int) sps.getStartPosition(cu(), getCurrentPath().getLeaf());
 //            final int endOffset = (int) sps.getEndPosition(cu(), getCurrentPath().getLeaf());
 //            final int start = ctx.lineStartOffset(offset);
 //            indentLine(start, adjustments);
 //            if (start != ctx.lineStartOffset(endOffset)) {
 //                li.moveTo(offset);
 //                indentMultiline(li, endOffset, adjustments);
 //
 //            }
 //
 //        } catch (BadLocationException e) {
 //            if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
 //        }
 //        return super.visitStringExpression(stringExpressionTree, adjustments);
 //    }
 
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
         try {
             final int start = ctx.lineStartOffset(getStartPos(node));
             indentLine(start, adjustments);
             incIndent();
             super.visitBlockExpression(node, adjustments);
             decIndent();
             final int end = ctx.lineStartOffset(getEndPos(node));
             indentLine(end, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return adjustments;
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
         final TokenSequence<JFXTokenId> ts = ts();
         ts.move(pos);
         while (ts.movePrevious()) {
             final Token<JFXTokenId> t = ts.token();
             switch (t.id()) {
                 case COMMENT:
                     return ts.offset();
                 case WS:
                     continue;
                 default:
                     break;
             }
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
 
 
     private static final Pattern EMPTY_LINE_PTRN = Pattern.compile("\\s+");
 
     private boolean isEmpty(String text) {
         return text == null || text.length() == 0 || EMPTY_LINE_PTRN.matcher(text).matches();
     }
 
 
 }
