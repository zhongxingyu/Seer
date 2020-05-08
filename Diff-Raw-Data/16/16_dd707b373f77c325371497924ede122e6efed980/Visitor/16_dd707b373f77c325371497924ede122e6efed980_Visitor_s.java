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
 import java.util.List;
 import java.util.Queue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 /**
  * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
 * @todo documentation
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
     public Queue<Adjustment> visitClass(ClassTree classTree, Queue<Adjustment> adjustments) {
         return super.visitClass(classTree, adjustments);
     }
 
     @Override
    public Queue<Adjustment> visitVariable(JavaFXVariableTree node, Queue<Adjustment> adjustments) {
         try {
             final int start = (int) sp().getStartPosition(cu(), node);
             indentLine(start, adjustments);
            if (isMultiline(node)) {
                 li.moveTo(start);
                 if (li.hasNext()) {
                     indentMultiline(li, (int) sp().getEndPosition(cu(), node), adjustments);
                 }
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return super.visitVariable(node, adjustments);
     }
 
     private void indentLine(Element line, Queue<Adjustment> adjustments) throws BadLocationException {
         final int ls = ctx.lineStartOffset(line.getStartOffset());
         indentLine(ls, adjustments);
     }
 
     private void indentLine(int ls, Queue<Adjustment> adjustments) throws BadLocationException {
         if (ctx.lineIndent(ctx.lineStartOffset(ls)) != indentOffset) {
             adjustments.offer(Adjustment.indent(ctx.document().createPosition(ls), indentOffset));
         }
     }
 
     @Override
     public Queue<Adjustment> visitExpressionStatement(ExpressionStatementTree node, Queue<Adjustment> adjustments) {
         try {
             final int position = (int) sp().getStartPosition(cu(), node);
             if (isFirstOnLine(position)) {
                 indentLine(position, adjustments);
                 if (isMultiline(node)) {
                     li.moveTo(position);
                     if (li.hasNext()) {
                         indentMultiline(li, (int) sp().getEndPosition(cu(), node), adjustments);
                     }
                 }
             } else {
                 adjustments.offer(Adjustment.add(ctx.document().createPosition(position), NEW_LINE_STRING));
                 adjustments.offer(Adjustment.indent(ctx.document().createPosition(position + 1), indentOffset));
                 if (isMultiline(node)) {
                     li.moveTo(position);
                     if (li.hasNext()) {
                         indentMultiline(li, (int) sp().getEndPosition(cu(), node), adjustments);
                     }
                 }
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return super.visitExpressionStatement(node, adjustments);
     }
 
     @Override
     public Queue<Adjustment> visitIdentifier(IdentifierTree node, Queue<Adjustment> adjustments) {
         try {
             final long position = sp().getStartPosition(cu(), node);
             if (isFirstOnLine((int) position))
                 indentLine((int) position, adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return super.visitIdentifier(node, adjustments);
     }
 
     @Override
     public Queue<Adjustment> visitUnary(UnaryTree node, Queue<Adjustment> adjustments) {
         return super.visitUnary(node, adjustments);
     }
 
     @Override
     public Queue<Adjustment> visitBinary(BinaryTree node, Queue<Adjustment> adjustments) {
         try {
             SourcePositions sps = sp();
             final int offset = (int) sps.getStartPosition(cu(), node);
             final int end = (int) sps.getEndPosition(cu(), node);
             if (isMultiline(node)) {
                 li.moveTo(offset);
                 if (isFirstOnLine(offset)) {
                     indentLine(li.get(), adjustments);
                 }
                 indentMultiline(li, end, adjustments);
             }
 
             final TokenSequence<JFXTokenId> ts = ts();
             ts.move(offset);
             while (ts.moveNext() && ts.offset() <= end) {
                 if ("operator".equals(ts.token().id().primaryCategory())) {
                     if (cs.spaceAroundBinaryOps()) {
                         if (ts.movePrevious() && ts.token().id() != JFXTokenId.WS) {
                             //TODO: [RKo]
                         }
                     }
                 }
             }
 
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return super.visitBinary(node, adjustments);
     }
 
     @Override
     public Queue<Adjustment> visitObjectLiteralPart(ObjectLiteralPartTree objectLiteralPartTree, Queue<Adjustment> adjustments) {
         try {
             SourcePositions sps = sp();
             final int offset = (int) sps.getStartPosition(cu(), objectLiteralPartTree);
             if (isMultiline(objectLiteralPartTree)) {
                 li.moveTo(offset);
                 final Element element = li.get();
                 if (isFirstOnLine(offset)) {
                     indentLine(element, adjustments);
                 }
                 indentMultiline(li, (int) sps.getEndPosition(cu(), objectLiteralPartTree), adjustments);
             } else {
                 if (isFirstOnLine(offset)) {
                     indentLine(offset, adjustments);
                 }
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return super.visitObjectLiteralPart(objectLiteralPartTree, adjustments);
     }
 
     private void indentMultiline(LineIterator<Element> li, int endOffset, Queue<Adjustment> adjustments) throws BadLocationException {
         final int ci = getCi();
         if (li.hasNext()) {
             indentOffset = indentOffset + ci;
             while (li.hasNext()) {
                 final Element element = li.next();
                 if (element.getStartOffset() > endOffset) {
                     break;
                 }
                 indentLine(element, adjustments);
             }
             indentOffset = indentOffset - ci;
         }
     }
 
     private boolean isMultiline(Tree tree) throws BadLocationException {
         SourcePositions sps = sp();
         return ctx.lineStartOffset((int) sps.getStartPosition(cu(), tree))
                 != ctx.lineStartOffset((int) sps.getEndPosition(cu(), tree));
     }
 
     @Override
     public Queue<Adjustment> visitFunctionDefinition(FunctionDefinitionTree node, Queue<Adjustment> adjustments) {
         final TokenSequence<JFXTokenId> ts = ts();
         SourcePositions sps = sp();
         try {
             ts.move((int) sps.getStartPosition(cu(), node));
             while (ts.moveNext()) {
                 final JFXTokenId id = ts.token().id();
                 switch (id) {
                     case PUBLIC:
                     case PRIVATE:
                     case STATIC:
                     case WS:
                         continue;
                     case FUNCTION:
                         verifyFunctionDefSpaces(ts, node, adjustments);
                     default:
                         break;
 
                 }
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         super.visitFunctionDefinition(node, adjustments);
         return adjustments;
     }
 
     private void verifyFunctionDefSpaces(TokenSequence<JFXTokenId> ts, FunctionDefinitionTree node, Queue<Adjustment> adjustments) throws BadLocationException {
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
         verifyBraces(node, adjustments, cs.getMethodDeclBracePlacement());
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
 
     private void verifyBraces(Tree node, Queue<Adjustment> adjustments, BracePlacement bp) throws BadLocationException {
         final TokenSequence<JFXTokenId> ts = tu.tokensFor(node);
         Token<JFXTokenId> obrace = moveTo(ts, JFXTokenId.LBRACE);
         int obraceTokenStart = ts.offset();
         if (obrace != null) {
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
                                 cs.spaceBeforeClassDeclLeftBrace() ? ONE_SPACE : STRING_ZERO_LENGTH));
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
                     indentOffset = indentOffset + getIndentStepLevel();
                     adjustments.offer(Adjustment.indent(doc.createPosition(obraceTokenStart), indentOffset));
                     break;
 
             }
 
             checkEndBrace(node, adjustments, ts, obraceTokenStart);
             indentOffset = oldIndent;
         }
 
     }
 
     private void checkEndBrace(Tree node, Queue<Adjustment> adjustments, TokenSequence<JFXTokenId> ts, int obraceTokenStart) throws BadLocationException {
         Document doc = ctx.document();
         final int end = (int) sp().getEndPosition(cu(), node);
         ts.move(end - 1); //getting into last token...
         if (ts.moveNext()) {
             //we are @the end.
             do {
                 if (ts.token().id() == JFXTokenId.RBRACE) {
                     li.moveTo(obraceTokenStart);
                     Element st = li.get();
                     li.moveTo(end);
                     if (st.equals(li.get())) {
                         adjustments.offer(Adjustment.add(doc.createPosition(ts.offset()), NEW_LINE_STRING));
                        break;
                     }
                 }
             } while (ts.movePrevious());
             indentLine(ts.offset(), adjustments);
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
     public Queue<Adjustment> visitInstantiate(InstantiateTree instantiateTree, Queue<Adjustment> adjustments) {
         indentOffset = indentOffset + getIndentStepLevel();
         final Queue<Adjustment> list = super.visitInstantiate(instantiateTree, adjustments);
         indentOffset = indentOffset - getIndentStepLevel();
         return list;
     }
 
     @Override
     public Queue<Adjustment> visitClassDeclaration(ClassDeclarationTree node, Queue<Adjustment> adjustments) {
         if (tu.isSynthetic(getCurrentPath())) {
             return super.visitClassDeclaration(node, adjustments);
         }
         SourcePositions sps = sp();
         final Document doc = ctx.document();
         try {
             int pos = ctx.lineStartOffset((int) sps.getStartPosition(cu(), getCurrentPath().getLeaf()));
             indentLine(pos, adjustments);
             pos = skipPreviousComment(pos);
             int elc = cs.getBlankLinesBeforeClass();
 
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
 
             indentOffset = indentOffset + getIndentStepLevel();
             super.visitClassDeclaration(node, adjustments);
             indentOffset = indentOffset - getIndentStepLevel();
 
 //            pos = ctx.lineStartOffset((int) sps.getEndPosition(cu(), getCurrentPath().getLeaf()));
 //            indentLine(pos, adjustments);
 
 //            verifyBracesAndSpaces(node, adjustments);
             verifyBraces(node, adjustments, cs.getClassDeclBracePlacement());
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
         SourcePositions sps = sp();
         try {
             final int ls = ctx.lineStartOffset((int) sps.getStartPosition(cu(), getCurrentPath().getLeaf()));
             if (indentOffset != ctx.lineIndent(ls)) {
                 adjustments.offer(Adjustment.indent(ctx.document().createPosition(ls), indentOffset));
             }
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
         return super.visitImport(importTree, adjustments);
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
     public Queue<Adjustment> visitReturn(ReturnTree returnTree, Queue<Adjustment> adjustments) {
         SourcePositions sps = sp();
         final int offset = (int) sps.getStartPosition(cu(), getCurrentPath().getLeaf());
         try {
             indentLine(ctx.lineStartOffset(offset), adjustments);
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return super.visitReturn(returnTree, adjustments);
     }
 
     @Override
     public Queue<Adjustment> visitLiteral(LiteralTree literalTree, Queue<Adjustment> adjustments) {
         SourcePositions sps = sp();
         final int offset = (int) sps.getStartPosition(cu(), getCurrentPath().getLeaf());
         try {
             final int ls = ctx.lineStartOffset(offset);
             final Document doc = ctx.document();
             final String s = doc.getText(ls, offset - ls);
             // we work only with literal which stands alone as "return statement"
             if (isEmpty(s) && ctx.lineIndent(ls) != indentOffset) {
                 adjustments.offer(Adjustment.indent(doc.createPosition(ls), indentOffset));
             }
         } catch (BadLocationException e) {
             if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
         }
         return super.visitLiteral(literalTree, adjustments);
     }
 
     @Override
     public Queue<Adjustment> visitBlock(BlockTree blockTree, Queue<Adjustment> adjustments) {
 
         if (!tu.isSynthetic(getCurrentPath())) {
             try {
                 SourcePositions sps = sp();
                 final int start = ctx.lineStartOffset((int) sps.getStartPosition(cu(), getCurrentPath().getLeaf()));
                 indentLine(start, adjustments);
                 indentOffset = indentOffset + getIndentStepLevel();
                 super.visitBlock(blockTree, adjustments);
                 indentOffset = indentOffset - getIndentStepLevel();
                 verifyBraces(blockTree, adjustments, cs.getOtherBracePlacement());
             } catch (BadLocationException e) {
                 if (log.isLoggable(Level.SEVERE)) log.severe("Reformat failed. " + e);
             }
         }
 
         return adjustments;
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
         return cs.getContinuationIndentSize() - getIndentStepLevel();
     }
 
     @Override
     public Queue<Adjustment> visitBlockExpression(BlockExpressionTree node, Queue<Adjustment> adjustments) {
         try {
             SourcePositions sps = sp();
             final int start = ctx.lineStartOffset((int) sps.getStartPosition(cu(), getCurrentPath().getLeaf()));
             indentLine(start, adjustments);
             indentOffset = indentOffset + getIndentStepLevel();
             super.visitBlockExpression(node, adjustments);
             indentOffset = indentOffset - getIndentStepLevel();
             final int end = ctx.lineStartOffset((int) sps.getEndPosition(cu(), getCurrentPath().getLeaf()));
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
