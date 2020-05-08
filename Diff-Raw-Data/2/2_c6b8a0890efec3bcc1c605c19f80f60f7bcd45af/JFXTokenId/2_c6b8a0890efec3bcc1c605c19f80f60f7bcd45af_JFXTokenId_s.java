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
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 
 package org.netbeans.api.javafx.lexer;
 
 import org.netbeans.api.java.lexer.JavadocTokenId;
 import org.netbeans.api.lexer.*;
 import org.netbeans.lib.javafx.lexer.JFXLexer;
 import org.netbeans.spi.lexer.*;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Rastislav Komara (<a href="mailto:rastislav.komara@sun.com">RKo</a>)
  * @todo documentation
  */
 public enum JFXTokenId implements TokenId {
 
     /* v4Lexer tokens. */
     ABSTRACT("keyword", 4, "abstract"), // NOI18N
     AFTER("keyword", 5, "after"), // NOI18N
     AND("keyword", 6, "and"), // NOI18N
     AS("keyword", 7, "as"), // NOI18N
     ASSERT("keyword-directive", 8, "assert"), // NOI18N
     AT("keyword", 9, "at"), // NOI18N
     ATTRIBUTE("keyword", 10, "attribute"), // NOI18N
     BEFORE("keyword", 11, "before"), // NOI18N
     BIND("keyword", 12, "bind"), // NOI18N
     BOUND("keyword", 13, "bound"), // NOI18N
     BREAK("keyword-directive", 14, "break"), // NOI18N
     CATCH("keyword-directive", 15, "catch"), // NOI18N
     CLASS("keyword", 16, "class"), // NOI18N
     COLON("operator", 104, ":"), // NOI18N
     COMMA("separator", 84, ","), // NOI18N
     COMMENT("comment", 131), // NOI18N
     CONTINUE("keyword-directive", 17, "continue"), // NOI18N
     DECIMAL_LITERAL("number", 122), // NOI18N
     DEF("keyword", 18, "def"), // NOI18N
     DELETE("keyword", 19, "delete"), // NOI18N
     DOC_COMMENT("comment", 132), // NOI18N
     DOT("separator", 85, "."), // NOI18N
     DOTDOT("operator", 80, ".."), // NOI18N
     Digits("number", 125), // NOI18N
     DoubleQuoteBody("string", 109), // NOI18N
     ELSE("keyword-directive", 20, "else"), // NOI18N
     EQ("operator", 87, "="),// NOI18N
     EQEQ("operator", 86, "=="),// NOI18N
     EXCLUSIVE("keyword", 21, "exclusive"), // NOI18N
     EXTENDS("keyword", 22, "extends"), // NOI18N
     Exponent("number", 126), // NOI18N
     FALSE("keyword-literal", 23, "false"), // NOI18N
     FINALLY("keyword-directive", 24, "finally"), // NOI18N
     FIRST("keyword", 25, "first"), // NOI18N
     FLOATING_POINT_LITERAL("number", 127), // NOI18N
     FOR("keyword-directive", 26, "for"), // NOI18N
     FORMAT_STRING_LITERAL("format", 118), // NOI18N
     FROM("keyword", 27, "from"), // NOI18N
     FUNCTION("keyword", 28, "function"), // NOI18N
     GT("operator", 88, ">"),// NOI18N
     GTEQ("operator", 92, ">="),// NOI18N
     HEX_LITERAL("number", 124), // NOI18N
     IDENTIFIER("identifier", 130), // NOI18N
     IF("keyword-directive", 29, "if"), // NOI18N
     IMPORT("keyword", 30, "import"), // NOI18N
     IN("keyword", 32, "in"), // NOI18N
     INDEXOF("keyword", 31, "indexof"), // NOI18N
     INIT("keyword", 33, "init"), // NOI18N
     INSERT("keyword", 34, "insert"), // NOI18N
     INSTANCEOF("keyword", 35, "instanceof"), // NOI18N
     INTO("keyword", 36, "into"), // NOI18N
    INVALIDC("keyword", 134, "invalidc"), // NOI18N
     INVERSE("keyword", 37, "inverse"), // NOI18N
     JavaIDDigit("identifier", 129), // NOI18N
     LAST("keyword", 38, "last"), // NOI18N
     LAZY("keyword", 39, "lazy"), // NOI18N
     LBRACE("separator", 114, "{"), // NOI18N
     LBRACKET("separator", 75, "["), // NOI18N
     LINE_COMMENT("comment", 133), // NOI18N
     LPAREN("separator", 76, "("),// NOI18N
     LT("operator", 89, "<"),// NOI18N
     LTEQ("operator", 91, "<="), // NOI18N
     LTGT("operator", 90, "<>"), // NOI18N
     Letter("identifier", 128), // NOI18N
     MIXIN("keyword", 40, "mixin"), // NOI18N
     MOD("keyword", 41, "mod"), // NOI18N
     NATIVEARRAY("keyword", 42, "nativearray"), // NOI18N
     NEW("keyword", 43, "new"), // NOI18N
     NOT("keyword", 44, "not"), // NOI18N
     NOTEQ("operator", 103, "!="), // NOI18N
     NULL("keyword-literal", 45, "null"), // NOI18N
     NextIsPercent("string", 110), // NOI18N
     OCTAL_LITERAL("number", 123), // NOI18N
     ON("keyword", 46, "on"), // NOI18N
     OR("keyword", 47, "or"), // NOI18N
     OVERRIDE("keyword", 48, "override"), // NOI18N
     PACKAGE("keyword", 49, "package"), // NOI18N
     PERCENT("operator", 97, "%"),// NOI18N
     PERCENTEQ("operator", 102, "%="), // NOI18N
     PIPE("operator", 78, "|"), // NOI18N
     PLUS("operator", 93, "+"), // NOI18N
     PLUSEQ("operator", 98, "+="), // NOI18N
     PLUSPLUS("operator", 79, "++"), // NOI18N
     POSTINIT("keyword", 50, "postinit"), // NOI18N
     POUND("operator", 77, "#"), // NOI18N
     PRIVATE("keyword", 51, "private"), // NOI18N
     PROTECTED("keyword", 52, "protected"), // NOI18N
     PUBLIC("keyword", 54, "public"), // NOI18N
     PUBLIC_INIT("keyword", 53, "public-init"), // NOI18N
     PUBLIC_READ("keyword", 55, "public-read"), // NOI18N
     QUES("operator", 105, "?"), // NOI18N
     QUOTE_LBRACE_STRING_LITERAL("string", 113), // NOI18N
     RBRACE("separator", 117, "}"), // NOI18N
     RBRACE_LBRACE_STRING_LITERAL("string", 116), // NOI18N
     RBRACE_QUOTE_STRING_LITERAL("string", 115), // NOI18N
     RBRACKET("separator", 82, "]"), // NOI18N
     REPLACE("keyword", 56, "replace"), // NOI18N
     RETURN("keyword-directive", 57, "return"), // NOI18N
     REVERSE("keyword", 58, "reverse"), // NOI18N
     RPAREN("separator", 81, ")"), // NOI18N
     SEMI("separator", 83, ";"), // NOI18N
     SIZEOF("keyword", 59, "sizeof"), // NOI18N
     SLASH("operator", 96, "/"), // NOI18N
     SLASHEQ("operator", 101, "/="), // NOI18N
     STAR("operator", 95, "*"), // NOI18N
     STAREQ("operator", 100, "*="), // NOI18N
     STATIC("keyword", 60, "static"), // NOI18N
     STEP("keyword", 61, "step"), // NOI18N
     STRING_LITERAL("string", 112), // NOI18N
     SUB("operator", 94, "-"), // NOI18N
     SUBEQ("operator", 99, "-="), // NOI18N
     SUBSUB("operator", 107, "--"), // NOI18N
     SUCHTHAT("operator", 106, "=>"), // NOI18N
     SUPER("keyword", 62, "super"), // NOI18N
     SingleQuoteBody("string", 111), // NOI18N
     THEN("keyword", 63, "then"), // NOI18N
     THIS("keyword", 64, "this"), // NOI18N
     THROW("keyword-directive", 65, "throw"), // NOI18N
     TIME_LITERAL("time", 121), // NOI18N
     TRANSLATION_KEY("i18n-artifact", 120), // NOI18N
     TRIGGER("keyword", 66, "trigger"), // NOI18N
     TRUE("keyword-literal", 67, "true"), // NOI18N
     TRY("keyword-directive", 68, "try"), // NOI18N
     TWEEN("keyword", 69, "tween"), // NOI18N
     TYPEOF("keyword", 70, "typeof"), // NOI18N
     TranslationKeyBody("operator", 119), // NOI18N
     VAR("keyword", 71, "var"), // NOI18N
     WHERE("keyword", 72, "where"), // NOI18N
     WHILE("keyword-directive", 73, "while"), // NOI18N
     WITH("keyword", 74, "with"), // NOI18N
     WS("whitespace", 108), // NOI18N
 
     UNKNOWN("error", 140); // NOI18N
 
     public static final String UNIVERSAL_CATEGORY = "future-literal"; // NOI18N
     private final String primaryCategory;
     private final int tokenType;
     private final String fixedText;
     private static JFXTokenId[] typeToId;
 
     static {
         try {
             final JFXTokenId[] tokenIds = JFXTokenId.values();
             Arrays.sort(tokenIds, new Comparator<JFXTokenId>() {
                 public int compare(JFXTokenId o1, JFXTokenId o2) {
                     return o1.getTokenType() - o2.getTokenType();
                 }
             });
             final JFXTokenId tid = tokenIds[tokenIds.length - 1];
             final int type = tid.getTokenType();
             typeToId = new JFXTokenId[type + 2];
             for (JFXTokenId jfxTokenId : tokenIds) {
                 if (jfxTokenId.getTokenType() > 0) {
                     typeToId[jfxTokenId.getTokenType()] = jfxTokenId;
                 }
             }
         } catch (Throwable t) {
             t.printStackTrace();
         }
     }
 
 
     public static JFXTokenId getId(int tokenType) {
         if (tokenType >= 0 && tokenType < typeToId.length) {
             return typeToId[tokenType];
         } else {
             return UNKNOWN;
         }
     }
 
     JFXTokenId(String primaryCategory, int tokenType) {
         this(primaryCategory, tokenType, null);
     }
 
     JFXTokenId(String primaryCategory, int tokenType, String fixedText) {
         this.fixedText = fixedText;
         this.primaryCategory = primaryCategory;
         this.tokenType = tokenType;
     }
 
     @Override
     public String toString() {
         return super.toString();
     }
 
     public int getTokenType() {
         return tokenType;
     }
 
     public String primaryCategory() {
         return primaryCategory != null ? primaryCategory : UNIVERSAL_CATEGORY;
     }
 
     public String getFixedText() {
         return fixedText;
     }
 
     private static final Language<JFXTokenId> language = new LanguageHierarchy<JFXTokenId>() {
         private Logger log = Logger.getLogger(JFXTokenId.class.getName());
 //        JFXLexer lexer;
 
         protected Collection<JFXTokenId> createTokenIds() {
             return Arrays.asList(JFXTokenId.values());
         }
 
         protected Lexer<JFXTokenId> createLexer(LexerRestartInfo<JFXTokenId> info) {
             /*if (lexer == null) {
                 lexer = new JFXLexer();
             }
             try {
                 lexer.restart(info);
             } catch (IOException e) {
                 e.printStackTrace();
             }*/
             try {
                 return new JFXLexer(info);
             } catch (IOException e) {
                 if (log.isLoggable(Level.SEVERE)) log.severe("Cannot create lexer.\n" + e); // NOI18N
                 throw new IllegalStateException(e);
             }
         }
 
         @Override
         protected String mimeType() {
             return "text/x-fx"; // NOI18N
         }
 
         @Override
         protected LanguageEmbedding<?> embedding(Token<JFXTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
             switch (token.id()) {
                 case DOC_COMMENT:
                     return LanguageEmbedding.create(JavadocTokenId.language(), 3,
                                 (token.partType() == PartType.COMPLETE) ? 2 : 0);
                 case QUOTE_LBRACE_STRING_LITERAL:
                     return LanguageEmbedding.create(JFXStringTokenId.language(), 1, 0);
                 case RBRACE_QUOTE_STRING_LITERAL:
                     return LanguageEmbedding.create(JFXStringTokenId.language(), 0, 1);
                 case RBRACE_LBRACE_STRING_LITERAL:
                     return LanguageEmbedding.create(JFXStringTokenId.language(), 0, 0);
                 case DoubleQuoteBody:
                 case SingleQuoteBody:
                 case STRING_LITERAL:
                     return LanguageEmbedding.create(JFXStringTokenId.language(true), 1,
                             (token.partType() == PartType.COMPLETE) ? 1 : 0);
             }
             return null; // No embedding
         }
 
         @Override
         protected EmbeddingPresence embeddingPresence(JFXTokenId id) {
             switch (id) {
                 case COMMENT:
                 case QUOTE_LBRACE_STRING_LITERAL:
                 case RBRACE_QUOTE_STRING_LITERAL:
                 case RBRACE_LBRACE_STRING_LITERAL:
                 case DoubleQuoteBody:
                 case SingleQuoteBody:
                 case STRING_LITERAL:
                     return EmbeddingPresence.ALWAYS_QUERY;
                 default:
                     return EmbeddingPresence.NONE;
             }
         }
 
     }.language();
 
     public static Language<JFXTokenId> language() {
         return language;
     }
 
     /**
      * Check if provided token is comment token.
      * @param id to check.
      * @return true if <code>id</code> is comment
      */
     public static boolean isComment(JFXTokenId id) {
         if (id == null) return false;
         switch (id) {
             case COMMENT:
             case LINE_COMMENT:
             case DOC_COMMENT:
                 return true;
             default:
                 return false;
         }
     }
 }
