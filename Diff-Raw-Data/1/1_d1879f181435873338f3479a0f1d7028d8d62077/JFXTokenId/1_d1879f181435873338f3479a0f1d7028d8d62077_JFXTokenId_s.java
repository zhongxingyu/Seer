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
 	LAZY("keyword", 39),
 	COMMA("separator", 85),
 	DEF("keyword", 18),
 	AS("keyword", 7),
 	NOTEQ("operator", 104),
 	INTO("keyword", 36),
 	TranslationKeyBody("operator", 119),
 	FALSE("keyword-literal", 23),
 	ABSTRACT("keyword", 4),
 	THEN("keyword", 64),
 	STEP("keyword", 62),
 	PLUSPLUS("operator", 80),
 	IMPORT("keyword", 30),
 	PACKAGE("keyword", 48),
 	SIZEOF("keyword", 60),
 	PIPE("operator", 79),
 	CONTINUE("keyword-directive", 17),
 	ON("keyword", 45),
 	DOT("separator", 86),
 	SingleQuoteBody("string", 110),
 	PRIVATE("keyword", 50),
 	Letter("identifier", 128),
 	AND("keyword", 6),
 	FUNCTION("keyword", 28),
 	TRIGGER("keyword", 67),
 	STRING_LITERAL("string", 111),
 	RBRACKET("separator", 83),
 	RPAREN("separator", 82),
 	RBRACE_LBRACE_STRING_LITERAL("string", 116),
 	ASSERT("keyword-directive", 8),
 	PLUS("operator", 94),
 	FINALLY("keyword-directive", 24),
 	EXTENDS("keyword", 22),
 	AT("keyword", 9),
 	PUBLIC_READABLE("keyword", 54),
 	TIME_LITERAL("time", 121),
 	SUPER("keyword", 63),
 	DECIMAL_LITERAL("number", 122),
 	WS("whitespace", 131),
 	SUBSUB("operator", 108),
 	NEW("keyword", 41),
 	PUBLIC_READ("keyword", 55),
 	EQ("operator", 88),
 	EXCLUSIVE("keyword", 21),
 	LT("operator", 90),
 	BOUND("keyword", 13),
 	LINE_COMMENT("comment", 134),
 	EQEQ("operator", 87),
 	QUOTE_LBRACE_STRING_LITERAL("string", 113),
 	FLOATING_POINT_LITERAL("number", 127),
 	CATCH("keyword-directive", 15),
 	STATIC("keyword", 61),
 	SEMI("separator", 84),
 	ELSE("keyword-directive", 20),
 	INDEXOF("keyword", 31),
 	FORMAT_STRING_LITERAL("format", 118),
 	LTEQ("operator", 92),
 	FIRST("keyword", 25),
 	BREAK("keyword-directive", 14),
 	NULL("keyword-literal", 44),
 	QUES("operator", 106),
 	COLON("operator", 105),
 	DOTDOT("operator", 81),
 	IDENTIFIER("identifier", 130),
 	NextIsPercent("string", 112),
 	INSERT("keyword", 34),
 	TRUE("keyword-literal", 68),
 	DOC_COMMENT("comment", 133),
 	POUND("operator", 78),
 	POSTINIT("keyword", 49),
 	THROW("keyword-directive", 66),
 	WHERE("keyword", 73),
 	PUBLIC("keyword", 53),
 	LTGT("operator", 91),
 	PERCENT("operator", 98),
 	TYPEOF("keyword", 71),
 	LAST("keyword", 38),
 	LBRACKET("separator", 76),
 	MOD("keyword", 40),
 	INIT("keyword", 33),
 	OCTAL_LITERAL("number", 123),
 	HEX_LITERAL("number", 124),
 	OR("keyword", 46),
 	LBRACE("separator", 114),
 	AFTER("keyword", 5),
 	RBRACE("separator", 117),
 	PROTECTED("keyword", 51),
 	INVERSE("keyword", 37),
 	SUBEQ("operator", 100),
 	INSTANCEOF("keyword", 35),
 	TRANSLATION_KEY("i18n-artifact", 120),
 	LPAREN("separator", 77),
 	DoubleQuoteBody("string", 109),
 	SLASHEQ("operator", 102),
 	FROM("keyword", 27),
 	PERCENTEQ("operator", 103),
 	DELETE("keyword", 19),
 	Exponent("number", 126),
 	SLASH("operator", 97),
 	WHILE("keyword-directive", 74),
 	STAREQ("operator", 101),
 	READABLE("keyword", 56),
 	PLUSEQ("operator", 99),
 	PUBLIC_INIT("keyword", 52),
 	REPLACE("keyword", 57),
 	GT("operator", 89),
 	COMMENT("comment", 132),
 	OVERRIDE("keyword", 47),
 	GTEQ("operator", 93),
 	THIS("keyword", 65),
 	WITH("keyword", 75),
 	IN("keyword", 32),
 	REVERSE("keyword", 59),
 	INVALIDC("keyword", 136),
 	JavaIDDigit("identifier", 129),
 	VAR("keyword", 72),
 	CLASS("keyword", 16),
 	TWEEN("keyword", 70),
 	RETURN("keyword-directive", 58),
 	IF("keyword-directive", 29),
 	SUCHTHAT("operator", 107),
 	FOR("keyword-directive", 26),
 	LAST_TOKEN("future-literal", 135),
 	NON_WRITABLE("keyword", 42),
 	BEFORE("keyword", 11),
 	STAR("operator", 96),
 	ATTRIBUTE("keyword", 10),
 	SUB("operator", 95),
 	BIND("keyword", 12),
 	Digits("number", 125),
 	NOT("keyword", 43),
 	TRY("keyword-directive", 69),
 	RBRACE_QUOTE_STRING_LITERAL("string", 115),    
     UNKNOWN("error", 200);
 
     /*v3.g lexer
     EXPR_LIST("error", 120),
 	LAZY("keyword", 69),
 	COMMA("separator", 83),
 	SEQ_INDEX("error", 125),
 	AS("keyword", 55),
 	HexDigit("error", 158),
 	SEQ_SLICE_EXCLUSIVE("error", 127),
 	NOTEQ("operator", 102),
 	INTO("keyword", 66),
 	TranslationKeyBody("operator", 151),
 	FALSE("keyword-literal", 15),
 	ABSTRACT("keyword", 5),
 	THEN("keyword", 75),
 	STEP("keyword", 74),
 	PLUSPLUS("operator", 49),
 	IMPORT("keyword", 19),
 	PACKAGE("keyword", 28),
 	SIZEOF("keyword", 37),
 	PIPE("operator", 51),
 	CONTINUE("keyword-directive", 13),
 	ON("keyword", 71),
 	DOT("separator", 84),
 	SingleQuoteBody("string", 142),
 	PRIVATE("keyword", 30),
 	Letter("identifier", 162),
 	TYPED_ARG_LIST("error", 138),
 	EXPRESSION("error", 114),
 	AND("keyword", 54),
 	FUNCTION("keyword", 17),
 	TRIGGER("keyword", 76),
 	STRING_LITERAL("string", 143),
 	EMPTY_MODULE_ITEM("error", 108),
 	MODULE("error", 107),
 	RBRACKET("separator", 81),
 	RPAREN("separator", 80),
 	SEMI_INSERT_START("error", 4),
 	RBRACE_LBRACE_STRING_LITERAL("string", 148),
 	ASSERT("keyword-directive", 6),
 	PLUS("operator", 92),
 	OBJECT_LIT("error", 128),
 	ON_REPLACE("error", 119),
 	FINALLY("keyword-directive", 61),
 	EXTENDS("keyword", 60),
 	AT("keyword", 7),
 	TIME_LITERAL("time", 156),
 	SUPER("keyword", 36),
 	DECIMAL_LITERAL("number", 153),
 	SLICE_CLAUSE("error", 117),
 	WS("whitespace", 165),
 	NEW("keyword", 24),
 	SUBSUB("operator", 50),
 	EQ("operator", 86),
 	FUNC_EXPR("error", 112),
 	EXCLUSIVE("keyword", 59),
 	LT("operator", 88),
 	BOUND("keyword", 10),
 	LINE_COMMENT("comment", 167),
 	RangeDots("error", 160),
 	NEGATIVE("error", 122),
 	EQEQ("operator", 85),
 	QUOTE_LBRACE_STRING_LITERAL("string", 145),
 	FLOATING_POINT_LITERAL("number", 161),
 	TYPE_ANY("error", 135),
 	STATIC("keyword", 38),
 	CATCH("keyword-directive", 57),
 	SEMI("separator", 82),
 	ELSE("keyword-directive", 58),
 	INDEXOF("keyword", 20),
 	FORMAT_STRING_LITERAL("format", 150),
 	LTEQ("operator", 90),
 	BREAK("keyword-directive", 11),
 	FIRST("keyword", 62),
 	NULL("keyword-literal", 26),
 	QUES("operator", 104),
 	COLON("operator", 103),
 	DOTDOT("operator", 79),
 	IDENTIFIER("identifier", 164),
 	NextIsPercent("string", 144),
 	TYPE_UNKNOWN("error", 136),
 	INSERT("keyword", 22),
 	TRUE("keyword-literal", 42),
 	DOC_COMMENT("comment", 139),
 	POUND("operator", 46),
 	THROW("keyword-directive", 40),
 	POSTINIT("keyword", 29),
 	WHERE("keyword", 78),
 	POSTINCR("error", 123),
 	OBJECT_LIT_PART("error", 129),
 	PUBLIC("keyword", 32),
 	LTGT("operator", 89),
 	STATEMENT("error", 113),
 	PERCENT("operator", 96),
 	TYPEOF("keyword", 43),
 	LAST("keyword", 68),
 	SEQ_EMPTY("error", 130),
 	READONLY("error", 33),
 	LBRACKET("separator", 48),
 	INIT("keyword", 21),
 	MOD("keyword", 70),
 	OCTAL_LITERAL("number", 157),
 	SEQ_SLICE("error", 126),
 	FUNC_APPLY("error", 121),
 	HEX_LITERAL("number", 159),
 	OR("keyword", 72),
 	LBRACE("separator", 146),
 	AFTER("keyword", 53),
 	RBRACE("separator", 149),
 	BLOCK("error", 115),
 	EMPTY_FORMAT_STRING("error", 132),
 	PROTECTED("keyword", 31),
 	INVERSE("keyword", 67),
 	TYPE_NAMED("error", 133),
 	SUBEQ("operator", 98),
 	POSTDECR("error", 124),
 	INSTANCEOF("keyword", 65),
 	TRANSLATION_KEY("i18n-artifact", 152),
 	PARAM("error", 111),
 	ON_REPLACE_SLICE("error", 118),
 	LPAREN("separator", 47),
 	DoubleQuoteBody("string", 141),
 	SLASHEQ("operator", 100),
 	FROM("keyword", 63),
 	PERCENTEQ("operator", 101),
 	DELETE("keyword", 14),
 	Exponent("number", 155),
 	SLASH("operator", 95),
 	WHILE("keyword-directive", 45),
 	STAREQ("operator", 99),
 	CLASS_MEMBERS("error", 110),
 	PLUSEQ("operator", 97),
 	REPLACE("keyword", 73),
 	GT("operator", 87),
 	COMMENT("comment", 166),
 	OVERRIDE("keyword", 27),
 	GTEQ("operator", 91),
 	SEQ_EXPLICIT("error", 131),
 	THIS("keyword", 39),
 	WITH("keyword", 77),
 	REVERSE("keyword", 35),
 	IN("keyword", 64),
 	JavaIDDigit("identifier", 163),
 	VAR("keyword", 44),
 	CLASS("keyword", 12),
 	TWEEN("keyword", 105),
 	RETURN("keyword-directive", 34),
 	LET("error", 23),
 	IF("keyword-directive", 18),
 	SUCHTHAT("operator", 106),
 	SEMI_INSERT_END("error", 52),
 	TYPE_FUNCTION("error", 134),
 	FOR("keyword-directive", 16),
 	LAST_TOKEN("future-literal", 168),
 	BEFORE("keyword", 56),
 	ATTR_INTERPOLATE("error", 140),
 	MISSING_NAME("error", 116),
 	STAR("operator", 94),
 	ATTRIBUTE("keyword", 8),
 	MODIFIER("error", 109),
 	SUB("operator", 93),
 	BIND("keyword", 9),
 	Digits("number", 154),
 	TYPE_ARG("error", 137),
 	TRY("keyword-directive", 41),
 	NOT("keyword", 25),
 	RBRACE_QUOTE_STRING_LITERAL("string", 147),
     UNKNOWN("error", 200);                      */
 
     public static final String UNIVERSAL_CATEGORY = "future-literal";
     private final String primaryCategory;
     private final int tokenType;
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
                 if (log.isLoggable(Level.SEVERE)) log.severe("Cannot create lexer.\n" + e);
                 throw new IllegalStateException(e);
             }
         }
 
         @Override
         protected String mimeType() {
             return "text/x-fx";
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
