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
 
 package org.netbeans.modules.javafx.fxd.composer.editor.completion.providers;
 
 import com.sun.javafx.tools.fxd.schema.model.AbstractSchemaElement;
 import com.sun.javafx.tools.fxd.schema.model.Element;
import com.sun.javafx.tools.fxd.schema.model.Enumeration;
 import com.sun.javafx.tools.fxd.schema.model.SchemaVisitor;
 import java.util.logging.Logger;
 import javax.swing.text.BadLocationException;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.editor.structure.api.DocumentElement;
 import org.netbeans.modules.javafx.fxd.composer.editor.completion.FXDCompletionItem;
 import org.netbeans.modules.javafx.fxd.composer.editor.completion.FXDCompletionQuery;
 import org.netbeans.modules.javafx.fxd.composer.lexer.FXDTokenId;
 import org.netbeans.modules.javafx.fxd.composer.lexer.TokenUtils;
 import org.netbeans.spi.editor.completion.CompletionResultSet;
 
 /**
  *
  * @author Andrey Korostelev
  */
 class NodeCompletionProvider extends AbstractCompletionProvider {
 
     private static final Logger LOG = Logger.getLogger(NodeCompletionProvider.class.getName());
 
     @Override
    protected void fillCompletionItems(CompletionResultSet resultSet, DocumentElement el, int caretOffset, TokenSequence<FXDTokenId> ts) {
         FXDTokenId prev = getPrevNonWhiteID(el, caretOffset, ts);
         FXDTokenId next = getNextNonWhiteID(el, caretOffset, ts);
 
         if (prev == null && next == FXDTokenId.IDENTIFIER) {
             // move ts to next non-white token. DO NOT REMOVE
             TokenUtils.getNextNonWhite(ts, caretOffset);
             if (ts.offset() < caretOffset) {
                 // inside identifier
                processNodeId(resultSet, el, caretOffset);
             } else {
                 // before identifier
                 processParentDocElement(resultSet, el, caretOffset, ts);
             }
         } else if (prev == FXDTokenId.IDENTIFIER && next == FXDTokenId.LBRACE) {
             // move ts to previous non-white token
             Token<FXDTokenId> prevT = TokenUtils.getPrevNonWhite(ts, caretOffset);
             if (ts.offset() + prevT.length() == caretOffset) {
                 // at the end of id before {
                processNodeId(resultSet, el, caretOffset);
             } else {
                 // between id and {
                 // nothing to suggest?
             }
         } else if (prev == FXDTokenId.RBRACE && next == null) {
             // after }.
             processParentDocElement(resultSet, el, caretOffset, ts);
         } else if (next != FXDTokenId.UNKNOWN) {
             // between { and }, but not before error
             processNodeBody(resultSet, el, caretOffset, ts);
         }
     }
 
     private void processNodeId(final CompletionResultSet resultSet,
            DocumentElement el, int caretOffset) {
        // TODO: use the following when it is NOT in attribute value
        fillNodeIdItems(resultSet, el, caretOffset);
     }
 
     private void fillNodeIdItems(final CompletionResultSet resultSet,
             DocumentElement el, final int caretOffset) {
         final String nameStart = el.getName().substring(0, caretOffset - el.getStartOffset());
         final int startOffset = el.getStartOffset();
         FXDCompletionQuery.getFXDSchema().visit(new SchemaVisitor() {
 
             public void visitSchemaElement(AbstractSchemaElement ae) {
                 // collect schema elements with matching ids || element and enum names
                 if (ae instanceof Element) {
                     if (idStartsWith(ae.id, nameStart)) {
                         resultSet.addItem(new FXDCompletionItem(ae, startOffset));
                     }
                 }
             }
 
         });
     }
 
     private void processNodeBody(final CompletionResultSet resultSet,
             DocumentElement el, int caretOffset, TokenSequence<FXDTokenId> ts) {
         FXDTokenId prev = getPrevNonWhiteID(el, caretOffset, ts);
         FXDTokenId next = getNextNonWhiteID(el, caretOffset, ts);
 
         if (prev == FXDTokenId.LBRACE || prev == FXDTokenId.COMMA){
             if (next == FXDTokenId.IDENTIFIER_ATTR) {
                 // move ts to next non-white token. DO NOT REMOVE
                 TokenUtils.getNextNonWhite(ts, caretOffset);
                 if (caretOffset <= ts.offset()) {
                     // between { and attr id
                     fillItemsWithNodeAttrs(resultSet, el, caretOffset, null);
                 } else {
                     // inside identifier (caretOffset > ts.offset())
                     processAttrId(resultSet, el, caretOffset, ts);
                 }
             } else {
                 fillItemsWithNodeAttrs(resultSet, el, caretOffset, null);
             }
         } else if (prev == FXDTokenId.IDENTIFIER_ATTR && next == FXDTokenId.COLON){
             Token<FXDTokenId> prevT = TokenUtils.getPrevNonWhite(ts, caretOffset);
             if (ts.offset() + prevT.length() == caretOffset) {
                 // at the end of id before :
                 processAttrId(resultSet, el, caretOffset, ts);
             } else {
                 // between id and :
                 // nothing to suggest?
             }
         } else if (prev == FXDTokenId.COLON){
             // attr value completion
             processAttrValue(resultSet, el, caretOffset, ts);
         } else if (next == FXDTokenId.COMMA || next == FXDTokenId.RBRACE){
             if (prev == FXDTokenId.IDENTIFIER){
                 TokenUtils.getPrevNonWhite(ts, caretOffset);
                 FXDTokenId prevPrev = getPrevNonWhiteID(el, ts.offset(), ts);
                 if (prevPrev == FXDTokenId.COLON){
                     processAttrValue(resultSet, el, caretOffset, ts);
                 } else {
                     // at the end of id before , or }
                     processAttrId(resultSet, el, caretOffset, ts);
                 }
             } else {
                 // started attr value (not identifier).
                 // do not complete numbers, strings, etc.
             }
         }
 
     }
 
     private void processAttrId(final CompletionResultSet resultSet,
             DocumentElement el, int caretOffset, TokenSequence<FXDTokenId> ts) {
         String nameStart;
         try {
             // move ts to prev token.
             FXDTokenId id = getPrevNonWhiteID(el, caretOffset, ts);
             if ( id != FXDTokenId.IDENTIFIER_ATTR) {
                 //then move ts to next token
                 id = getNextNonWhiteID(el, caretOffset, ts);
                 if ( id != FXDTokenId.IDENTIFIER_ATTR) {
                     return;
                 }
             }
             nameStart = el.getDocument().getText(ts.offset(), caretOffset - ts.offset());
             fillItemsWithNodeAttrs(resultSet, el, caretOffset, nameStart);
         } catch (BadLocationException ex) {
             // do not suggest while not sure
         }
     }
 
 }
