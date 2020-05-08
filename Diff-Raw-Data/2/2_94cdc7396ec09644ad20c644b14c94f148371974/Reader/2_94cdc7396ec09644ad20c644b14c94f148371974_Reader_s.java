 /* XHL - Extensible Host Language
  * Copyright 2012 Sergej Chodarev
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sk.tuke.xhl.core;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.PeekingIterator;
 import sk.tuke.xhl.core.Token.TokenType;
 import sk.tuke.xhl.core.elements.*;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Sets.union;
 import static sk.tuke.xhl.core.MaybeError.fail;
 import static sk.tuke.xhl.core.MaybeError.succeed;
 import static sk.tuke.xhl.core.Token.TokenType.*;
 
 /**
  * XHL parser
  *
  * Grammar:
  *
  * <pre>
  *   block       ::= { expression LINEEND | expression-with-block }
  *   expression  ::= combination { operator combination } | operator
  *   expression-with-block ::= combination ':' LINEEND INDENT block DEDENT
  *   combination ::= term { term }
  *   term        ::= literal | '(' expression ')'
  *   literal     ::= symbol | string | number | boolean | list | map | 'null'
  *   boolean     ::= 'true' | 'false'
  *   list        ::= '[]' | '[' expression { ',' expression } ']'
  *   map         ::= '{}' | '{' key-value { ',' key-value } '}'
  *   key-value   ::= expression ':' expression
  *   symbol      ::= plain-symbol { '.' plain-symbol }
  * </pre>
  *
  * @author Sergej Chodarev
  */
 public class Reader {
     private PeekingIterator<Token> tokens;
     private Token token;
 
     private static final Set<TokenType> literalH = ImmutableSet.of(SYMBOL,
             BRACKET_OPEN, BRACE_OPEN, STRING, NUMBER, TRUE, FALSE, NULL);
     private static final Set<TokenType> termH = union(literalH,
            ImmutableSet.of(PAR_CLOSE));
     private static final Set<TokenType> expressionH = termH;
     private static final Set<TokenType> blockH = expressionH;
 
     private final String filename;
     private final List<Error> errors = new ArrayList<>();
 
     public static MaybeError<Block> read(java.io.Reader input, String filename)
             throws IOException {
         return new Reader(filename).parse(input);
     }
 
     public static MaybeError<Block> read(String code) throws IOException {
         return read(new StringReader(code), "<input>");
     }
 
     private Reader(String filename) {
         this.filename = filename;
     }
 
     private MaybeError<Block> parse(java.io.Reader input) throws IOException {
         MaybeError<Iterator<Token>> tokensOrErrors =
                 Lexer.readTokens(input, filename);
         tokens = Iterators.peekingIterator(tokensOrErrors.get());
         token = tokens.next();
         Block expressions = block(ImmutableSet.of(EOF));
         if (errors.isEmpty()) {
             return succeed(expressions);
         }
         else
             return fail(errors);
     }
 
     private Block block(Set<TokenType> keys) throws IOException {
         Block block = new Block(token.position);
         while (token.type != EOF && token.type != DEDENT) {
             block.add(expression(true, true, keys));
         }
         return block;
     }
 
     /**
      * Syntax analyzer for expression
      *
      * @param withBlock     Can the expression introduce a block?
      * @param colonAccepted Can the expression contain the colon operator
      *                      directly? For example inside maps it can not.
      * @param keys          Key symbols for error recovery.
      * @return              Read expression.
      */
     private Expression expression(boolean withBlock, boolean colonAccepted,
                                   Set<TokenType> keys)
             throws IOException {
         if (token.type == OPERATOR) { // Single operator can be used as a symbol
             Symbol op = new Symbol(token.stringValue, token.position);
             token = tokens.next();
             return op;
         }
         Set<TokenType> k = withBlock
                 ? union(ImmutableSet.of(SYMBOL, LINEEND, INDENT, DEDENT), blockH)
                 : ImmutableSet.of(OPERATOR, LINEEND);
         Expression first = combination(union(k, keys));
         while (token.type == OPERATOR) {
             if (isColon()
                     && (tokens.peek().type == LINEEND || !colonAccepted)) {
                 // Colon is not an operator here, but introduces a block or
                 // value in a map
                 break;
             }
             Combination exp = new Combination(token.position);
             Symbol op = new Symbol(token.stringValue, token.position);
             token = tokens.next();
             Expression second = combination(union(k, keys));
             exp.add(op);
             exp.add(first);
             exp.add(second);
             first = exp;
         }
         if (token.type == LINEEND)
             token = tokens.next();
         else if (withBlock && isColon()) {
             if (isColon())
                 token = tokens.next(); // :
             else
                 error("Colon before a block missing.", union(union(ImmutableSet.of(LINEEND, INDENT, DEDENT),
                         blockH), keys));
             if (token.type == LINEEND)
                 token = tokens.next(); // \n
             else
                 error("Colon before a block missing.", union(union(ImmutableSet.of(INDENT, DEDENT), blockH),
                         keys));
 
             if (token.type == INDENT)
                 token = tokens.next();
             else
                 error("Colon before a block missing.", union(union(blockH, ImmutableSet.of(DEDENT)), keys)
                 );
             Block block = block(union(ImmutableSet.of(DEDENT), keys));
             if (token.type == DEDENT)
                 token = tokens.next(); // DEDENT
             else
                 error("End of block expected.", keys);
             // If block header is not a combination -- create combination
             if (!(first instanceof Combination)) {
                 Combination head = new Combination(first.getPosition());
                 head.add(first);
                 first = head;
             }
             ((Combination) first).add(block);
         }
         return first;
     }
 
     private Expression combination(Set<TokenType> keys) throws IOException {
         Combination list = new Combination(token.position);
         while (termH.contains(token.type)) {
             list.add(term(keys));
         }
         if (list.size() == 1)
             return list.head();
         else
             return list;
     }
 
     private SList list(Set<TokenType> keys) throws IOException {
         SList list = new SList(token.position);
         token = tokens.next(); // [
         if (token.type == BRACKET_CLOSE) { // Empty list
             token = tokens.next(); // ]
             return list;
         }
         // Non-empty list
         list.add(expression(false, true, union(keys, ImmutableSet.of(BRACKET_CLOSE))));
         while (token.type == TokenType.COMMA) {
             token = tokens.next(); // ,
             list.add(expression(false, true, union(ImmutableSet.of(COMMA, BRACKET_CLOSE), keys)));
         }
         if (token.type == BRACKET_CLOSE)
             token = tokens.next(); // ]
         else
             error("Closing bracket missing.", keys);
         return list;
     }
 
     private SMap map(Set<TokenType> keys) throws IOException {
         SMap map = new SMap(token.position);
         token = tokens.next(); // {
         if (token.type == BRACE_CLOSE) { // Empty map
             token = tokens.next(); // }
             return map;
         }
         Set<? extends TokenType> k = ImmutableSet.of(COMMA, BRACE_CLOSE);
         // Non-empty map
         keyValue(map, union(k, keys));
         while (token.type == TokenType.COMMA) {
             token = tokens.next(); // ,
             keyValue(map, union(k, keys));
         }
         if (token.type == BRACE_CLOSE)
             token = tokens.next(); // }
         else
             error("Closing brace missing.", keys);
         return map;
     }
 
     private void keyValue(SMap map, Set<TokenType> keys) throws IOException {
         Expression key = expression(false, false, union(keys, ImmutableSet.of(SYMBOL)));
         if (isColon())
             token = tokens.next(); // :
         else
             error("Expected colon ':'.", union(keys, expressionH));
         Expression value = expression(false, false, keys);
         map.put(key, value);
     }
 
     private Expression term(Set<TokenType> keys) throws IOException {
         Expression sexp = null;
         switch (token.type) {
         case SYMBOL:
             sexp = symbol(keys);
             break;
         case STRING:
             sexp = new SString(token.stringValue, token.position);
             token = tokens.next();
             break;
         case NUMBER:
             sexp = new SNumber(token.doubleValue, token.position);
             token = tokens.next();
             break;
         case TRUE:
             sexp = new SBoolean(true, token.position);
             token = tokens.next();
             break;
         case FALSE:
             sexp = new SBoolean(false, token.position);
             token = tokens.next();
             break;
         case PAR_OPEN:
             token = tokens.next(); // (
             sexp = expression(false, true, union(keys, ImmutableSet.of(PAR_CLOSE)));
             if (token.type == PAR_CLOSE)
                 token = tokens.next(); // )
             else
                 error("Closing parenthesis expected.", keys);
             break;
         case BRACKET_OPEN:
             sexp = list(keys);
             break;
         case BRACE_OPEN:
             sexp = map(keys);
             break;
         }
         return sexp;
     }
 
     private Expression symbol(Set<TokenType> keys) {
         Position position = token.position;
         List<String> name = newArrayList(token.stringValue);
         token = tokens.next();
         while (token.type == DOT) {
             token = tokens.next(); // .
             String component = token.stringValue;
             if (token.type == SYMBOL)
                 token = tokens.next();
             else
                 error("Symbol expected", keys);
             name.add(component);
         }
         return new Symbol(name.toArray(new String[name.size()]), position);
     }
 
     private boolean isColon() {
         return token.type == OPERATOR && token.stringValue.equals(":");
     }
 
     /**
      * Report error end skip tokens while one of the key tokens is not found.
      * @param msg  Error message.
      * @param keys A set of recovery tokens. At these tokens it is possible
  *             to recover syntax analysis process.
      */
     private void error(String msg, Set<TokenType> keys) {
         errors.add(new Error(token.position, msg));
         while (!keys.contains(token.type)) {
             token = tokens.next();
         }
     }
 }
