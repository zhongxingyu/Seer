 /*
  * Copyright 2005 Davy Verstappen.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jsmiparser.phase.lexer;
 
 import org.apache.log4j.Logger;
 import org.jsmiparser.util.symbol.IdSymbolImpl;
 import antlr.TokenStream;
 import antlr.Token;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class LexerModule extends IdSymbolImpl {
     private static final Logger m_log = Logger.getLogger(LexerModule.class);
 
     private String m_source;
     private List<Token> m_tokens = new ArrayList<Token>(100);
 
     public LexerModule(String id, String source) {
         super(id);
         m_source = source;
 
         m_log.debug("Created " + id + " from " + source);        
     }
 
     public String getSource() {
         return m_source;
     }
 
     public TokenStream createTokenStream() {
         return new TokenArrayStream(m_tokens);
     }
 
     public void add(Token token) {
         m_tokens.add(token);
     }

    public void clear() {
        m_tokens.clear();
    }
 }
