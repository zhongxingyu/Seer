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
 package org.netbeans.modules.javafx.fxd.composer.lexer;
 
 
 import com.sun.javafx.tools.fxd.FXDObjectElement;
 import com.sun.javafx.tools.fxd.FXDReference;
 import com.sun.javafx.tools.fxd.container.scene.fxd.FXDException;
 import com.sun.javafx.tools.fxd.container.scene.fxd.FXDParser;
 import com.sun.javafx.tools.fxd.container.scene.fxd.FXDSyntaxErrorException;
 import com.sun.javafx.tools.fxd.container.scene.fxd.lexer.ContentLexer;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeMap;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.spi.lexer.Lexer;
 import org.netbeans.spi.lexer.LexerInput;
 import org.netbeans.spi.lexer.LexerRestartInfo;
 import org.netbeans.spi.lexer.TokenPropertyProvider;
 import org.openide.util.Exceptions;
 
 /**
  *
  * @author Andrew Korostelev
  */
 public class FXDLexer implements Lexer<FXDTokenId> {
 
     private LexerRestartInfo<FXDTokenId> m_info;
     private int m_tokenIdx;
     private List<TokenData<FXDTokenId>> m_tokensList;
 
     FXDLexer(LexerRestartInfo<FXDTokenId> info) {
         this.m_info = info;
         //buildTokensList();
     }
 
     private void buildTokensList() {
         final LexerInput input = m_info.input();
         Reader reader = new LexerInputReader(input);
         ContentLexerImpl contLexer = new ContentLexerImpl(m_info, m_tokensList);
         FXDParser parser = null;
 
         try {
             parser= new FXDParser(reader, contLexer);
             parser.parseObject();
         } catch (FXDSyntaxErrorException syntaxEx) {
             syntaxEx.printStackTrace();
             try {
                 // workaround for #183149.
                 // TODO: FXDSyntaxErrorException thrown by FXDReference.parse should have offset.
                 if (syntaxEx.getOffset() == -1 && parser!= null){
                     syntaxEx = new FXDSyntaxErrorException(syntaxEx.getLocalizedMessage(), parser.getPosition());
                 }
                 contLexer.markError(syntaxEx);
             } catch (Exception e) {
                 Exceptions.printStackTrace(e);
             }
         } catch (Exception ex) {
             Exceptions.printStackTrace(ex);
         }
     }
 
     public Token<FXDTokenId> nextToken() {
         if (m_tokensList == null) {
             m_tokensList = new ArrayList<TokenData<FXDTokenId>>();
             buildTokensList();
         }
 
         TokenData<FXDTokenId> tData = getNextTokenData();
         if (tData.id() == FXDTokenId.EOF){
             m_tokenIdx = 0;
             tData = null;
             return null;
         }
         return createToken(tData);
     }
 
     private Token<FXDTokenId> createToken(TokenData<FXDTokenId> tData){
         if(tData.hasProperties()){
             return m_info.tokenFactory().createPropertyToken(tData.id(),
                     tData.length(), tData.getPropertyProvider());
         }
         return m_info.tokenFactory().createToken(tData.id(), tData.length());
     }
 
     private TokenData<FXDTokenId> getNextTokenData(){
         return m_tokensList.get(m_tokenIdx++);
     }
 
     public void release() {
        m_tokensList = null;
     }
 
     public Object state() {
         return null;
     }
 
     private static class LexerInputReader extends Reader {
 
         private final LexerInput m_input;
 
         public LexerInputReader(LexerInput input) {
             m_input = input;
         }
 
         @Override
         public int read() throws IOException {
 
             int c = m_input.read();
             if (m_input.readLength() == 0){
                 return -1;
             }
             return c;
         }
 
         @Override
         public int read(char[] cbuf, int off, int len) throws IOException {
             int c = m_input.read();
             int read = 0;
             while (c != LexerInput.EOF && m_input.readLength() < len) {
                 cbuf[off++] = (char) c;
                 read++;
                 c = m_input.read();
             } 
 
             if (read > 0) {
                 return read;
             } else {
                 return -1;
             }
         }
 
         @Override
         public void close() throws IOException {
         }
     }
 
     private static class ContentLexerImpl implements ContentLexer {
         private TreeMap<Integer, Integer> m_comments = new TreeMap<Integer, Integer>();
         private TreeMap<Integer, FXDTokenId> m_commentTypes = new TreeMap<Integer, FXDTokenId>();
         private FXDParser m_parser;
         private int m_tokenizedLength = 0;
         private LexerRestartInfo<FXDTokenId> m_info;
         private List<TokenData<FXDTokenId>> m_tokensList;
 
         public ContentLexerImpl(LexerRestartInfo<FXDTokenId> info,
                 List<TokenData<FXDTokenId>> tokensList) {
             this.m_info = info;
             this.m_tokensList = tokensList;
         }
 
         public void parsingStarted(FXDParser parser) {
             m_parser = parser;
         }
 
         public void parsingFinished() throws IOException, FXDException {
             // TODO tokenize the rest instead of marking it as error?
             if (m_parser.peekClean() != 0){
                 markError(null);
             } else {
                 markTailParsed();
             }
         }
 
         private void markTailParsed() throws IOException {
             char c = m_parser.peek();
             while (c != 0) {
                 c = m_parser.fetch();
             }
             if (m_tokenizedLength < m_parser.getPosition()) {
                 addTokenData(FXDTokenId.WS, m_tokenizedLength,
                         m_parser.getPosition() - m_tokenizedLength);
             }
             addTokenData(FXDTokenId.EOF, m_parser.getPosition(), 1);
         }
 
         public void identifier(String str, int startOff) throws FXDException {
             addTokenData(FXDTokenId.IDENTIFIER, startOff, str.length());
         }
 
         public void separator(char c, int offset) throws FXDException {
             switch (c) {
                 case '{':
                     addTokenData(FXDTokenId.LBRACE, offset, 1);
                     break;
                 case '}':
                     addTokenData(FXDTokenId.RBRACE, offset, 1);
                     break;
                 case '[':
                     addTokenData(FXDTokenId.LBRACKET, offset, 1);
                     break;
                 case ']':
                     addTokenData(FXDTokenId.RBRACKET, offset, 1);
                     break;
                 case '(':
                     addTokenData(FXDTokenId.LPAREN, offset, 1);
                     break;
                 case ')':
                     addTokenData(FXDTokenId.RPAREN, offset, 1);
                     break;
                 case ',':
                     addTokenData(FXDTokenId.COMMA, offset, 1);
                     break;
                 case '.':
                     addTokenData(FXDTokenId.DOT, offset, 1);
                     break;
                 case ';':
                     addTokenData(FXDTokenId.SEMI, offset, 1);
                     break;
                 case ' ':
                     addTokenData(FXDTokenId.COMMA, offset, 1);
                     break;
             }
         }
 
         public void attributeName(String name, int startOff, boolean isMeta) throws FXDException {
             addTokenData(FXDTokenId.IDENTIFIER_ATTR, startOff, name.length());
         }
 
         public void attributeValue(String value, int droppedChars, int startOff) throws FXDException {
             Object obj = m_parser.parseValue(value);
             FXDTokenId id = objectToToken(obj, value);
             int len = value.length();
             if (id == FXDTokenId.STRING_LITERAL){
                 len += droppedChars;
                 startOff -= droppedChars;
             }
             addTokenData(id, startOff, len);
         }
 
         private FXDTokenId objectToToken(Object obj, String value) {
             if (obj.equals(Boolean.TRUE)) {
                 return FXDTokenId.TRUE;
             } else if (obj.equals(Boolean.FALSE)) {
                 return FXDTokenId.FALSE;
             } else if (obj.equals(FXDObjectElement.NULL_VALUE)) {
                 return FXDTokenId.NULL;
             } else if (obj instanceof String) {
                 char c = value.charAt(0);
                 if (c == '"') {
                     return FXDTokenId.STRING_LITERAL;
                 }
             } else if (obj instanceof FXDReference) {
                 // TODO should nark reference in any special way?
                 return FXDTokenId.STRING_LITERAL;
             } else if (obj instanceof Integer) {
                 return FXDTokenId.NUMERIC_LITERAL;
             } else if (obj instanceof Long) {
                 return FXDTokenId.NUMERIC_LITERAL;
             } else if (obj instanceof Float) {
                 return FXDTokenId.FLOATING_POINT_LITERAL;
             }
             return FXDTokenId.IDENTIFIER;
         }
 
         public void operator(char c, int offset) throws FXDException {
             switch (c) {
                 case ':':
                     addTokenData(FXDTokenId.COLON, offset, 1);
                     break;
                 case '=':
                     addTokenData(FXDTokenId.EQ, offset, 1);
                     break;
             }
         }
 
         public void comment(int startOff, int endOff) {
             createComment(FXDTokenId.COMMENT, startOff, endOff);
         }
 
         public void lineComment(int startOff, int endOff) {
             createComment(FXDTokenId.LINE_COMMENT, startOff, endOff);
         }
 
         protected void markError(FXDSyntaxErrorException syntaxEx) throws IOException {
 
             char c = m_parser.peek();
             while (c != 0){
                 c = m_parser.fetch();
             }
 
             if (m_tokenizedLength < m_parser.getPosition()) {
                 SyntaxErrorPropertyProvider provider = syntaxEx == null ? null
                         : new SyntaxErrorPropertyProvider(syntaxEx);
                 addTokenData(FXDTokenId.UNKNOWN, m_tokenizedLength,
                         m_parser.getPosition() - m_tokenizedLength, provider);
             }
             addTokenData(FXDTokenId.EOF, m_tokenizedLength, 1);
         }
 
         protected TokenData<FXDTokenId> addTokenData(FXDTokenId id, int startOff, int len) {
             return addTokenData(id, startOff, len, null);
         }
 
         protected TokenData<FXDTokenId> addTokenData(FXDTokenId id, int startOff, int len,
                 TokenPropertyProvider<FXDTokenId> propProvider) {
 
             if (m_tokenizedLength < startOff) {
                 for (Iterator<Integer> i = m_comments.keySet().iterator(); i.hasNext();) {
                     int k = i.next();
                     if (k >= m_tokenizedLength && k < startOff) {
                         int v = m_comments.get(k);
                         i.remove();
                         addTokenData(m_commentTypes.remove(k), k, v);
                     } else {
                         break;
                     }
                 }
                 if (m_tokenizedLength < startOff) {
                     addTokenData(FXDTokenId.WS, m_tokenizedLength, startOff - m_tokenizedLength);
                 }
             }
 
             m_tokenizedLength += len;
             TokenData<FXDTokenId> tData = new TokenData<FXDTokenId>(id, len, propProvider);
             m_tokensList.add(tData);
             return tData;
         }
 
         private void createComment(FXDTokenId id, int startOff, int endOff){
             if (startOff == m_tokenizedLength) {
                 addTokenData(id, startOff, endOff - startOff);
             } else {
                 m_comments.put(startOff, endOff - startOff);
                 m_commentTypes.put(startOff, id);
             }
         }
 
     }
 
     private static class TokenData<E> {
 
         private E m_id;
         private int m_lenght;
         private TokenPropertyProvider<FXDTokenId> m_propProvider;
 
         public TokenData(E id, int lenght) {
             this(id, lenght, null);
         }
 
         public TokenData(E id, int lenght, TokenPropertyProvider<FXDTokenId> propProvider) {
             assert id != null;
             assert lenght > 0;
             m_id = id;
             m_lenght = lenght;
             m_propProvider = propProvider;
         }
 
         public E id(){
             return m_id;
         }
 
         public int length(){
             return m_lenght;
         }
 
         public boolean hasProperties(){
             return m_propProvider != null;
         }
 
         public TokenPropertyProvider<FXDTokenId> getPropertyProvider(){
             return m_propProvider;
         }
 
     }
 }
