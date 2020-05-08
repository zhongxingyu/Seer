 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.unrealscriptsupport.lexer;
 
 /**
  *
  * @author geertjan
  */
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.netbeans.spi.lexer.LanguageHierarchy;
 import org.netbeans.spi.lexer.Lexer;
 import org.netbeans.spi.lexer.LexerRestartInfo;
 import org.unrealscriptsupport.jcclexer.UnrealScriptParserConstants;
 
 /**
 *
 * @author eppleton
 */
 public class UnrealScriptLanguageHierarchy extends LanguageHierarchy<UnrealScriptTokenId> {
 
    private static List<UnrealScriptTokenId> tokens;
    private static Map<Integer, UnrealScriptTokenId> idToToken;
 
    private static void init() {
        tokens = Arrays.<UnrealScriptTokenId>asList(new UnrealScriptTokenId[]{
                 new UnrealScriptTokenId("EOF", "whitespace", UnrealScriptParserConstants.EOF),
                 new UnrealScriptTokenId("WHITESPACE", "whitespace", UnrealScriptParserConstants.WHITESPACE),
                 new UnrealScriptTokenId("SINGLE_LINE_COMMENT", "comment", UnrealScriptParserConstants.SINGLE_LINE_COMMENT),
                 new UnrealScriptTokenId("JAVADOC_COMMENT", "javadoc-comment", UnrealScriptParserConstants.JAVADOC_COMMENT),
                 new UnrealScriptTokenId("MULTI_LINE_COMMENT", "comment", UnrealScriptParserConstants.MULTI_LINE_COMMENT),
                 //new UnrealScriptTokenId("JAVADOC_TAG", "javadoc-tag", UnrealScriptParserConstants.JAVADOC_TAG),
                 new UnrealScriptTokenId("NAME_LITERAL", "name", UnrealScriptParserConstants.NAME_LITERAL),
                 new UnrealScriptTokenId("STRING_LITERAL", "string", UnrealScriptParserConstants.STRING_LITERAL),
                 new UnrealScriptTokenId("ABSTRACT", "keyword", UnrealScriptParserConstants.ABSTRACT),
                 new UnrealScriptTokenId("ARRAY", "keyword", UnrealScriptParserConstants.ARRAY),
                 new UnrealScriptTokenId("AUTO", "keyword", UnrealScriptParserConstants.AUTO),
                 new UnrealScriptTokenId("BOOLEAN", "keyword", UnrealScriptParserConstants.BOOLEAN),
                 new UnrealScriptTokenId("BREAK", "keyword", UnrealScriptParserConstants.BREAK),
                 new UnrealScriptTokenId("BYTE", "keyword", UnrealScriptParserConstants.BYTE),
                 new UnrealScriptTokenId("CACHEEXEMPT", "keyword", UnrealScriptParserConstants.CACHEEXEMPT),
                 new UnrealScriptTokenId("CASE", "keyword", UnrealScriptParserConstants.CASE),
                 new UnrealScriptTokenId("CLASS", "class", UnrealScriptParserConstants.CLASS),
                 new UnrealScriptTokenId("CLOCKWISEFROM", "keyword", UnrealScriptParserConstants.CLOCKWISEFROM),
                 new UnrealScriptTokenId("COERCE", "keyword", UnrealScriptParserConstants.COERCE),
                 new UnrealScriptTokenId("COLLAPSECATEGORIES", "keyword", UnrealScriptParserConstants.COLLAPSECATEGORIES),
                 new UnrealScriptTokenId("CONFIG", "keyword", UnrealScriptParserConstants.CONFIG),
                 new UnrealScriptTokenId("CONST", "keyword", UnrealScriptParserConstants.CONST),
                 new UnrealScriptTokenId("CONTINUE", "keyword", UnrealScriptParserConstants.CONTINUE),
                 new UnrealScriptTokenId("CROSS", "keyword", UnrealScriptParserConstants.CROSS),
                 new UnrealScriptTokenId("_DEFAULT", "keyword", UnrealScriptParserConstants._DEFAULT),
                 new UnrealScriptTokenId("_DEFAULTPROPERTIES", "method-declaration", UnrealScriptParserConstants._DEFAULTPROPERTIES),
                 new UnrealScriptTokenId("DELEGATE", "method-declaration", UnrealScriptParserConstants.DELEGATE),
                 new UnrealScriptTokenId("DEPENDSON", "keyword", UnrealScriptParserConstants.DEPENDSON),
                 new UnrealScriptTokenId("DEPRECATED", "keyword", UnrealScriptParserConstants.DEPRECATED),
                 new UnrealScriptTokenId("DO", "keyword", UnrealScriptParserConstants.DO),
                 new UnrealScriptTokenId("DONTCOLLAPSECATEGORIES", "keyword", UnrealScriptParserConstants.DONTCOLLAPSECATEGORIES),
                 new UnrealScriptTokenId("DOTPRODUCT", "keyword", UnrealScriptParserConstants.DOTPRODUCT),
                 new UnrealScriptTokenId("EDFINDABLE", "keyword", UnrealScriptParserConstants.EDFINDABLE),
                 new UnrealScriptTokenId("EDITCONST", "keyword", UnrealScriptParserConstants.EDITCONST),
                 new UnrealScriptTokenId("EDITINLINE", "keyword", UnrealScriptParserConstants.EDITINLINE),
                 new UnrealScriptTokenId("EDITINLINENEW", "keyword", UnrealScriptParserConstants.EDITINLINENEW),
                 new UnrealScriptTokenId("EDITINLINEUSE", "keyword", UnrealScriptParserConstants.EDITINLINEUSE),
                 new UnrealScriptTokenId("ELSE", "keyword", UnrealScriptParserConstants.ELSE),
                 new UnrealScriptTokenId("ENUM", "keyword", UnrealScriptParserConstants.ENUM),
                 new UnrealScriptTokenId("EVENT", "method-declaration", UnrealScriptParserConstants.EVENT),
                 new UnrealScriptTokenId("EXEC", "keyword", UnrealScriptParserConstants.EXEC),
                 new UnrealScriptTokenId("EXPORT", "keyword", UnrealScriptParserConstants.EXPORT),
                 new UnrealScriptTokenId("EXPORTSTRUCTS", "keyword", UnrealScriptParserConstants.EXPORTSTRUCTS),
                 new UnrealScriptTokenId("EXTENDS", "keyword", UnrealScriptParserConstants.EXTENDS),
                 new UnrealScriptTokenId("FALSE", "keyword", UnrealScriptParserConstants.FALSE),
                 new UnrealScriptTokenId("FINAL", "keyword", UnrealScriptParserConstants.FINAL),
                 new UnrealScriptTokenId("FLOAT", "keyword", UnrealScriptParserConstants.FLOAT),
                 new UnrealScriptTokenId("FOR", "keyword", UnrealScriptParserConstants.FOR),
                 new UnrealScriptTokenId("FOREACH", "keyword", UnrealScriptParserConstants.FOREACH),
                 new UnrealScriptTokenId("FUNCTION", "method-declaration", UnrealScriptParserConstants.FUNCTION),
                 new UnrealScriptTokenId("GLOBALCONFIG", "keyword", UnrealScriptParserConstants.GLOBALCONFIG),
                 new UnrealScriptTokenId("GOTO", "keyword", UnrealScriptParserConstants.GOTO),
                 new UnrealScriptTokenId("GUID", "keyword", UnrealScriptParserConstants.GUID),
                 new UnrealScriptTokenId("HIDECATEGORIES", "keyword", UnrealScriptParserConstants.HIDECATEGORIES),
                 new UnrealScriptTokenId("HIDEDROPDOWN", "keyword", UnrealScriptParserConstants.HIDEDROPDOWN),
                 new UnrealScriptTokenId("IF", "keyword", UnrealScriptParserConstants.IF),
                 new UnrealScriptTokenId("IGNORES", "keyword", UnrealScriptParserConstants.IGNORES),
                 new UnrealScriptTokenId("INPUT", "keyword", UnrealScriptParserConstants.INPUT),
                 new UnrealScriptTokenId("INSTANCED", "keyword", UnrealScriptParserConstants.INSTANCED),
                 new UnrealScriptTokenId("INT", "keyword", UnrealScriptParserConstants.INT),
                 new UnrealScriptTokenId("ITERATOR", "keyword", UnrealScriptParserConstants.ITERATOR),
                 new UnrealScriptTokenId("LATENT", "keyword", UnrealScriptParserConstants.LATENT),
                 new UnrealScriptTokenId("LOCAL", "local-declaration", UnrealScriptParserConstants.LOCAL),
                 new UnrealScriptTokenId("LOCALIZED", "keyword", UnrealScriptParserConstants.LOCALIZED),
                 new UnrealScriptTokenId("NAME", "keyword", UnrealScriptParserConstants.NAME),
                 new UnrealScriptTokenId("NATIVE", "keyword", UnrealScriptParserConstants.NATIVE),
                 new UnrealScriptTokenId("NATIVEREPLICATION", "keyword", UnrealScriptParserConstants.NATIVEREPLICATION),
                 new UnrealScriptTokenId("NEW", "keyword", UnrealScriptParserConstants.NEW),
                 new UnrealScriptTokenId("NOEDITINLINEW", "keyword", UnrealScriptParserConstants.NOEDITINLINEW),
                 new UnrealScriptTokenId("NOEXPORT", "keyword", UnrealScriptParserConstants.NOEXPORT),
                 new UnrealScriptTokenId("NONE", "keyword", UnrealScriptParserConstants.NONE),
                 new UnrealScriptTokenId("NOTPLACEABLE", "keyword", UnrealScriptParserConstants.NOTPLACEABLE),
                 new UnrealScriptTokenId("OPERATOR", "keyword", UnrealScriptParserConstants.OPERATOR),
                 new UnrealScriptTokenId("OPTIONAL", "keyword", UnrealScriptParserConstants.OPTIONAL),
                 new UnrealScriptTokenId("OUT", "keyword", UnrealScriptParserConstants.OUT),
                 new UnrealScriptTokenId("PACKAGE", "keyword", UnrealScriptParserConstants.PACKAGE),
                 new UnrealScriptTokenId("PEROBJECTCONFIG", "keyword", UnrealScriptParserConstants.PEROBJECTCONFIG),
                 new UnrealScriptTokenId("PLACEABLE", "keyword", UnrealScriptParserConstants.PLACEABLE),
                 new UnrealScriptTokenId("POSTOPERATOR", "keyword", UnrealScriptParserConstants.POSTOPERATOR),
                 new UnrealScriptTokenId("PREOPERATOR", "keyword", UnrealScriptParserConstants.PREOPERATOR),
                 new UnrealScriptTokenId("PRIVATE", "keyword", UnrealScriptParserConstants.PRIVATE),
                 new UnrealScriptTokenId("PROTECTED", "keyword", UnrealScriptParserConstants.PROTECTED),
                 new UnrealScriptTokenId("PUBLIC", "keyword", UnrealScriptParserConstants.PUBLIC),
                 new UnrealScriptTokenId("RELIABLE", "keyword", UnrealScriptParserConstants.RELIABLE),
                 new UnrealScriptTokenId("RETURN", "keyword", UnrealScriptParserConstants.RETURN),
                 new UnrealScriptTokenId("SAFEREPLACE", "keyword", UnrealScriptParserConstants.SAFEREPLACE),
                 new UnrealScriptTokenId("SHOWCATEGORIES", "keyword", UnrealScriptParserConstants.SHOWCATEGORIES),
                 new UnrealScriptTokenId("SIMULATED", "keyword", UnrealScriptParserConstants.SIMULATED),
                 new UnrealScriptTokenId("SINGULAR", "keyword", UnrealScriptParserConstants.SINGULAR),
                 new UnrealScriptTokenId("STATE", "keyword", UnrealScriptParserConstants.STATE),
                 new UnrealScriptTokenId("STATIC", "keyword", UnrealScriptParserConstants.STATIC),
                 new UnrealScriptTokenId("STRUCT", "keyword", UnrealScriptParserConstants.STRUCT),
                 new UnrealScriptTokenId("SUPER", "keyword", UnrealScriptParserConstants.SUPER),
                 new UnrealScriptTokenId("SWITCH", "keyword", UnrealScriptParserConstants.SWITCH),
                 new UnrealScriptTokenId("TRANSIENT", "keyword", UnrealScriptParserConstants.TRANSIENT),
                 new UnrealScriptTokenId("TRAVEL", "keyword", UnrealScriptParserConstants.TRAVEL),
                 new UnrealScriptTokenId("TRUE", "keyword", UnrealScriptParserConstants.TRUE),
                 new UnrealScriptTokenId("UNRELIABLE", "keyword", UnrealScriptParserConstants.UNRELIABLE),
                 new UnrealScriptTokenId("UNTIL", "keyword", UnrealScriptParserConstants.UNTIL),
                 new UnrealScriptTokenId("VAR", "var-declaration", UnrealScriptParserConstants.VAR),
                 new UnrealScriptTokenId("VOID", "keyword", UnrealScriptParserConstants.VOID),
                 new UnrealScriptTokenId("WITHIN", "keyword", UnrealScriptParserConstants.WITHIN),
                 new UnrealScriptTokenId("WHILE", "keyword", UnrealScriptParserConstants.WHILE),
                 new UnrealScriptTokenId("INTEGER_LITERAL", "literal", UnrealScriptParserConstants.INTEGER_LITERAL),
                 new UnrealScriptTokenId("DECIMAL_LITERAL", "literal", UnrealScriptParserConstants.DECIMAL_LITERAL),
                 new UnrealScriptTokenId("HEX_LITERAL", "literal", UnrealScriptParserConstants.HEX_LITERAL),
                 new UnrealScriptTokenId("OCTAL_LITERAL", "literal", UnrealScriptParserConstants.OCTAL_LITERAL),
                 new UnrealScriptTokenId("FLOATING_POINT_LITERAL", "literal", UnrealScriptParserConstants.FLOATING_POINT_LITERAL),
                 new UnrealScriptTokenId("DECIMAL_FLOATING_POINT_LITERAL", "literal", UnrealScriptParserConstants.DECIMAL_FLOATING_POINT_LITERAL),
                 new UnrealScriptTokenId("DECIMAL_EXPONENT", "number", UnrealScriptParserConstants.DECIMAL_EXPONENT),
                 new UnrealScriptTokenId("HEXADECIMAL_FLOATING_POINT_LITERAL", "literal", UnrealScriptParserConstants.HEXADECIMAL_FLOATING_POINT_LITERAL),
                 new UnrealScriptTokenId("HEXADECIMAL_EXPONENT", "number", UnrealScriptParserConstants.HEXADECIMAL_EXPONENT),
                 new UnrealScriptTokenId("IDENTIFIER", "identifier", UnrealScriptParserConstants.IDENTIFIER),
                 new UnrealScriptTokenId("LETTER", "literal", UnrealScriptParserConstants.LETTER),
                 new UnrealScriptTokenId("PART_LETTER", "literal", UnrealScriptParserConstants.PART_LETTER),
                 new UnrealScriptTokenId("LPAREN", "separator", UnrealScriptParserConstants.LPAREN),
                 new UnrealScriptTokenId("RPAREN", "separator", UnrealScriptParserConstants.RPAREN),
                 new UnrealScriptTokenId("LBRACE", "separator", UnrealScriptParserConstants.LBRACE),
                 new UnrealScriptTokenId("RBRACE", "separator", UnrealScriptParserConstants.RBRACE),
                 new UnrealScriptTokenId("LBRACKET", "separator", UnrealScriptParserConstants.LBRACKET),
                 new UnrealScriptTokenId("RBRACKET", "separator", UnrealScriptParserConstants.RBRACKET),
                 new UnrealScriptTokenId("SEMICOLON", "separator", UnrealScriptParserConstants.SEMICOLON),
                 new UnrealScriptTokenId("COMMA", "separator", UnrealScriptParserConstants.COMMA),
                 new UnrealScriptTokenId("DOT", "separator", UnrealScriptParserConstants.DOT),
                 new UnrealScriptTokenId("ASSIGN", "operator", UnrealScriptParserConstants.ASSIGN),
                 new UnrealScriptTokenId("AT", "operator", UnrealScriptParserConstants.AT),
                 new UnrealScriptTokenId("DOLLARS", "operator", UnrealScriptParserConstants.DOLLARS),
                 new UnrealScriptTokenId("LT", "operator", UnrealScriptParserConstants.LT),
                 new UnrealScriptTokenId("BANG", "operator", UnrealScriptParserConstants.BANG),
                 new UnrealScriptTokenId("TILDE", "operator", UnrealScriptParserConstants.TILDE),
                 new UnrealScriptTokenId("HOOK", "operator", UnrealScriptParserConstants.HOOK),
                 new UnrealScriptTokenId("COLON", "operator", UnrealScriptParserConstants.COLON),
                 new UnrealScriptTokenId("EQ", "operator", UnrealScriptParserConstants.EQ),
                 new UnrealScriptTokenId("LE", "operator", UnrealScriptParserConstants.LE),
                 new UnrealScriptTokenId("GE", "operator", UnrealScriptParserConstants.GE),
                 new UnrealScriptTokenId("NE", "operator", UnrealScriptParserConstants.NE),
                 new UnrealScriptTokenId("SC_OR", "operator", UnrealScriptParserConstants.SC_OR),
                 new UnrealScriptTokenId("SC_AND", "operator", UnrealScriptParserConstants.SC_AND),
                 new UnrealScriptTokenId("INCR", "operator", UnrealScriptParserConstants.INCR),
                 new UnrealScriptTokenId("DECR", "operator", UnrealScriptParserConstants.DECR),
                 new UnrealScriptTokenId("PLUS", "operator", UnrealScriptParserConstants.PLUS),
                 new UnrealScriptTokenId("MINUS", "operator", UnrealScriptParserConstants.MINUS),
                 new UnrealScriptTokenId("STAR", "operator", UnrealScriptParserConstants.STAR),
                 new UnrealScriptTokenId("SLASH", "operator", UnrealScriptParserConstants.SLASH),
                 new UnrealScriptTokenId("BIT_AND", "operator", UnrealScriptParserConstants.BIT_AND),
                 new UnrealScriptTokenId("BIT_OR", "operator", UnrealScriptParserConstants.BIT_OR),
                 new UnrealScriptTokenId("XOR", "operator", UnrealScriptParserConstants.XOR),
                 new UnrealScriptTokenId("REM", "operator", UnrealScriptParserConstants.REM),
                 new UnrealScriptTokenId("LSHIFT", "operator", UnrealScriptParserConstants.LSHIFT),
                 new UnrealScriptTokenId("EXPONENT", "operator", UnrealScriptParserConstants.EXPONENT),
                 new UnrealScriptTokenId("ATASSIGN", "operator", UnrealScriptParserConstants.ATASSIGN),
                 new UnrealScriptTokenId("DOLLARSASSIGN", "operator", UnrealScriptParserConstants.DOLLARSASSIGN),
                 new UnrealScriptTokenId("PLUSASSIGN", "operator", UnrealScriptParserConstants.PLUSASSIGN),
                 new UnrealScriptTokenId("MINUSASSIGN", "operator", UnrealScriptParserConstants.MINUSASSIGN),
                 new UnrealScriptTokenId("STARASSIGN", "operator", UnrealScriptParserConstants.STARASSIGN),
                 new UnrealScriptTokenId("SLASHASSIGN", "operator", UnrealScriptParserConstants.SLASHASSIGN),
                 new UnrealScriptTokenId("ANDASSIGN", "operator", UnrealScriptParserConstants.ANDASSIGN),
                 new UnrealScriptTokenId("ORASSIGN", "operator", UnrealScriptParserConstants.ORASSIGN),
                 new UnrealScriptTokenId("XORASSIGN", "operator", UnrealScriptParserConstants.XORASSIGN),
                 new UnrealScriptTokenId("REMASSIGN", "operator", UnrealScriptParserConstants.REMASSIGN),
                 new UnrealScriptTokenId("LSHIFTASSIGN", "operator", UnrealScriptParserConstants.LSHIFTASSIGN),
                 new UnrealScriptTokenId("RSIGNEDSHIFTASSIGN", "operator", UnrealScriptParserConstants.RSIGNEDSHIFTASSIGN),
                 new UnrealScriptTokenId("RUNSIGNEDSHIFTASSIGN", "operator", UnrealScriptParserConstants.RUNSIGNEDSHIFTASSIGN),
                 new UnrealScriptTokenId("RUNSIGNEDSHIFT", "operator", UnrealScriptParserConstants.RUNSIGNEDSHIFT),
                 new UnrealScriptTokenId("RSIGNEDSHIFT", "operator", UnrealScriptParserConstants.RSIGNEDSHIFT),
                 new UnrealScriptTokenId("GT", "operator", UnrealScriptParserConstants.GT)
 
                 // DO NOT add
                 // DEFAULT, IN_FORMAL_COMMENT, IN_MULTI_LINE_COMMENT, etc
                 
                });
        idToToken = new HashMap<Integer, UnrealScriptTokenId>();
        for (UnrealScriptTokenId token : tokens) {
            idToToken.put(token.ordinal(), token);
        }
    }
 
    static synchronized UnrealScriptTokenId getToken(int id) {
        if (idToToken == null) {
            init();
        }
        return idToToken.get(id);
    }
 
     @Override
    protected synchronized Collection<UnrealScriptTokenId> createTokenIds() {
        if (tokens == null) {
            init();
        }
        return tokens;
    }
 
     @Override
    protected synchronized Lexer<UnrealScriptTokenId> createLexer(LexerRestartInfo<UnrealScriptTokenId> info) {
        return new UnrealScriptLexer(info);
    }
 
     @Override
    protected String mimeType() {
        return "text/x-uc";
    }
 }
