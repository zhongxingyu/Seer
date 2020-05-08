 /*
  * Copyright (c) 2011-2012 Julien Nicoulaud <julien.nicoulaud@gmail.com>
  *
  * This file is part of idea-byteman.
  *
  * idea-byteman is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * idea-byteman is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with idea-byteman.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.nicoulaj.idea.byteman.lang;
 
 import com.intellij.psi.tree.TokenSet;
 
 import static com.intellij.psi.TokenType.ERROR_ELEMENT;
 import static com.intellij.psi.TokenType.WHITE_SPACE;
 import static net.nicoulaj.idea.byteman.lang.BytemanTypes.*;
 
 /**
  * Token type sets for the Byteman language.
  *
  * @author <a href="mailto:julien.nicoulaud@gmail.com">Julien Nicoulaud</a>
  * @since 0.1
  */
 public interface BytemanTokenTypeSets {
 
     /** TODO token type set. */
     TokenSet KEYWORD_SET = TokenSet.create(KEYWORD_BIND,
                                            KEYWORD_IF,
                                            KEYWORD_DO,
                                            KEYWORD_RULE,
                                            KEYWORD_CLASS,
                                            KEYWORD_METHOD,
                                            KEYWORD_ENDRULE,
                                            KEYWORD_NOTHING,
                                            BOOLEAN_LITERAL,
                                            KEYWORD_RETURN,
                                            KEYWORD_THROW,
                                            KEYWORD_NEW,
                                            KEYWORD_NULL,
                                            KEYWORD_AFTER,
                                            KEYWORD_ALL,
                                            KEYWORD_AT,
                                            KEYWORD_ENTRY,
                                            KEYWORD_HELPER,
                                            KEYWORD_INVOKE,
                                            KEYWORD_LINE,
                                            KEYWORD_READ,
                                            KEYWORD_SYNCHRONIZE,
                                            KEYWORD_WRITE);
 
     /** TODO token type set. */
     TokenSet BRACKETS_SET = TokenSet.create(LPAREN,
                                             RPAREN,
                                             LSQUARE,
                                             RSQUARE);
 
     /** TODO token type set. */
     TokenSet EXPRESSION_SEPARATOR_SET = TokenSet.create(SEMI, COMMA);
 
     /** TODO token type set. */
     TokenSet BINDING_SEPARATOR_SET = TokenSet.create(COMMA);
 
     /** TODO token type set. */
     TokenSet IDENTIFIER_PUNCTUATOR_SET = TokenSet.create(DOT);
 
     /** TODO token type set. */
     TokenSet ASSIGN_OPERATOR_SET = TokenSet.create(ASSIGN);
 
     /** TODO token type set. */
     TokenSet LOGICAL_OPERATOR_SET = TokenSet.create(AND,
                                                     OR,
                                                     NOT);
 
     /** TODO token type set. */
     TokenSet COMPARISON_OPERATOR_SET = TokenSet.create(LT,
                                                        LE,
                                                        EQ,
                                                        NE,
                                                        GE,
                                                        GT);
 
     /** TODO token type set. */
     TokenSet BITWISE_OPERATOR_SET = TokenSet.create(URSH,
                                                     RSH,
                                                     LSH,
                                                     BOR,
                                                     BAND,
                                                     BXOR,
                                                     TWIDDLE);
 
     /** TODO token type set. */
     TokenSet ARITHMETIC_OPERATOR_SET = TokenSet.create(MUL,
                                                        DIV,
                                                        PLUS,
                                                        MINUS,
                                                        MOD);
 
     /** TODO token type set. */
     TokenSet BINARY_EXPRESSION_OPERATOR_SET = TokenSet.orSet(TokenSet.create(AND, OR),
                                                              COMPARISON_OPERATOR_SET,
                                                              TokenSet.create(BOR, BAND, BXOR),
                                                              ARITHMETIC_OPERATOR_SET);
 
     /** TODO token type set. */
     TokenSet UNARY_EXPRESSION_OPERATOR_SET = TokenSet.create(NOT,
                                                              TWIDDLE,
                                                              MINUS);
 
     /** TODO token type set. */
     TokenSet TERNARY_CONDITION_SET = TokenSet.create(TERN_IF,
                                                      COLON);
 
     /** TODO token type set. */
     TokenSet DOLLAR_PREFIXED_IDENTIFIER_SET = TokenSet.create(DOLLAR);
 
     /** TODO token type set. */
     TokenSet IDENTIFIER_SET = TokenSet.create(IDENTIFIER, QUOTED_IDENTIFIER);
 
     /** TODO token type set. */
     TokenSet NUMBER_SET = TokenSet.create(INTEGER_LITERAL,
                                           FLOAT_LITERAL);
 
     /** TODO token type set. */
     TokenSet STRING_SET = TokenSet.create(STRING_LITERAL);
 
     /** TODO token type set. */
     TokenSet WHITE_SPACE_SET = TokenSet.create(WHITE_SPACE);
 
     /** TODO token type set. */
     TokenSet COMMENT_SET = TokenSet.create(COMMENT);
 
     /** TODO token type set. */
     TokenSet COMMENT_OR_WHITE_SPACE_SET = TokenSet.orSet(COMMENT_SET, WHITE_SPACE_SET);
 
     /** TODO token type set. */
     TokenSet ERROR_SET = TokenSet.create(ERROR_ELEMENT);
 }
