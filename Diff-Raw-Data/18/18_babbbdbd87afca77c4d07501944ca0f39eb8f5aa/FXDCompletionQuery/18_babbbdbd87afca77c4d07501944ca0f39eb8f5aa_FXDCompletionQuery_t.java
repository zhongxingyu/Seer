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
 package org.netbeans.modules.javafx.fxd.composer.editor.completion;
 
 import com.sun.javafx.tools.fxd.schema.model.Element;
 import javax.swing.text.Document;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.modules.editor.structure.api.DocumentElement;
 import org.netbeans.modules.editor.structure.api.DocumentModel;
 import org.netbeans.modules.editor.structure.api.DocumentModelException;
 import org.netbeans.modules.editor.structure.api.DocumentModelUtils;
 import org.netbeans.modules.javafx.fxd.composer.editor.BracketCompletion;
 import org.netbeans.modules.javafx.fxd.composer.lexer.FXDTokenId;
 import org.netbeans.spi.editor.completion.CompletionResultSet;
 import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
 import org.openide.util.Exceptions;
 import com.sun.javafx.tools.fxd.schema.model.FXDSchema;
 import com.sun.javafx.tools.fxd.schema.model.Function;
 import java.util.logging.Logger;
 import org.netbeans.modules.javafx.fxd.composer.editor.completion.providers.AbstractCompletionProvider;
 import org.netbeans.modules.javafx.fxd.composer.lexer.TokenUtils;
 import org.netbeans.modules.javafx.fxd.schemamodel.FXDSchemaModelProvider;
 
 /**
  * TODO: support functions, documentation,
  * @author avk
  */
 public class FXDCompletionQuery extends AsyncCompletionQuery {
 
     private static final Logger LOG = Logger.getLogger(FXDCompletionQuery.class.getName());
     public int m_queryType;
     private static FXDSchema m_schema;
 
     public FXDCompletionQuery(int queryType) {
         super();
         this.m_queryType = queryType;
     }
 
     public static FXDSchema getFXDSchema(){
         return m_schema;
     }
 
     @Override
     protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
         TokenSequence<FXDTokenId> ts = BracketCompletion.getTokenSequence((BaseDocument) doc, caretOffset);
         if (!isInsideCommentOrString(ts, caretOffset)) {
             fillCompletionItems(resultSet, doc, caretOffset, ts);
         }
         resultSet.finish();
 
     }
 
     private void fillCompletionItems(CompletionResultSet resultSet, Document doc,
             int caretOffset, TokenSequence<FXDTokenId> ts) {
 
         DocumentElement el = getDocumentElement(doc, caretOffset);
         if (el == null) {
             return;
         }
 
         initFXDSchema();
         if (m_schema == null) {
             return;
         }
         
         AbstractCompletionProvider.fillItemsForElement(resultSet, el, caretOffset, ts);
 
     }
 
     private void initFXDSchema(){
         if (m_schema == null) {
             m_schema = FXDSchemaModelProvider.getSchema();
         }
     }
 
 
     // TODO functions are not supported my model yet.
     private void fillElementFunctionsCompletion(Element[] elements,
             CompletionResultSet resultSet, int caretOffset) {
         for (Element elem : elements) {
             fillElementFunctionsCompletion(elem, resultSet, caretOffset);
         }
     }
     
     private void fillElementFunctionsCompletion(Element elem,
             CompletionResultSet resultSet, int caretOffset) {
         Function[] funcs = elem.functions;
         for (Function func : funcs) {
             resultSet.addItem(new FXDCompletionItem(func, caretOffset));
         }
         if (elem.extendsElement != null) {
             fillElementFunctionsCompletion(elem.extendsElement, resultSet, caretOffset);
         }
     }
 
     private static boolean isInsideCommentOrString(TokenSequence<FXDTokenId> ts, int offset) {
         ts.move(offset);
         if (!(ts.moveNext() || ts.movePrevious())) {
             return false;
         }
         boolean isComment = TokenUtils.isCommentToken(ts.token());
         boolean isString = ts.token().id() == FXDTokenId.STRING_LITERAL;
         boolean isInsideToken = offset >= ts.offset() + 1;
         
         if (( isComment || isString ) && isInsideToken) {
             return true;
         }
 
         // at the end of line comment
         if (!ts.movePrevious()){
             return false;
         }
         if (ts.token().id() == FXDTokenId.LINE_COMMENT){
             if (offset == ts.offset()+ts.token().length()){
                 return true;
             }
         }
         return false;
     }
 
     private DocumentElement getDocumentElement(Document doc, int caretOffset) {
         try {
             DocumentModel m = DocumentModel.getDocumentModel(doc);
             DocumentElement el = m.getLeafElementForOffset(caretOffset);
             DocumentModelUtils.dumpElementStructure(el);
             return el;
         } catch (DocumentModelException ex) {
             Exceptions.printStackTrace(ex);
         }
         return null;
     }
 
 }
 
 
