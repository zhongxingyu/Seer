 package com.antonzherdev.objd;
 
 import com.antonzherdev.objd.psi.ObjDTypes;
 import com.intellij.lexer.FlexAdapter;
 import com.intellij.lexer.Lexer;
 import com.intellij.openapi.editor.SyntaxHighlighterColors;
 import com.intellij.openapi.editor.colors.TextAttributesKey;
 import com.intellij.openapi.editor.markup.TextAttributes;
 import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
 import com.intellij.psi.TokenType;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.ui.JBColor;
 import org.jetbrains.annotations.NotNull;
 
 import java.awt.*;
 import java.io.Reader;
 import java.util.*;
 import java.util.List;
 
 import static com.antonzherdev.objd.psi.ObjDTypes.*;
 import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;
 
 public class ObjDSyntaxHighlighter extends SyntaxHighlighterBase {
     public static final TextAttributesKey KEYWORD = createTextAttributesKey("OBJD_KEYWORD", SyntaxHighlighterColors.KEYWORD);
     public static final TextAttributesKey DATATYPE = createTextAttributesKey("OBJD_DATATYPE", SyntaxHighlighterColors.KEYWORD);
     public static final TextAttributesKey COMMENT = createTextAttributesKey("OBJD_COMMENT", SyntaxHighlighterColors.LINE_COMMENT);
     public static final TextAttributesKey STRING = createTextAttributesKey("OBJD_STRING", SyntaxHighlighterColors.STRING);
 
     static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("OBJD_BAD_CHARACTER",
             new TextAttributes(JBColor.RED, null, null, null, Font.BOLD));
 
     private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
     private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
     private static final TextAttributesKey[] DATATYPE_KEYS = new TextAttributesKey[]{DATATYPE};
     private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
     private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
     private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
     private static final List<IElementType> KEYWORDS = Arrays.asList(
             W_CLASS, W_IMPORT, W_VAL, W_VAR, W_DEF, W_EXTENDS, W_IF, W_ELSE,
            W_PRIVATE, W_STATIC, W_SET, W_GET, W_NIL, W_TRUE, W_FALSE, W_STUB, W_TRAIT, W_ENUM);
     private static final List<IElementType> DATATYPES = Arrays.asList(
             TP_BOOL, TP_FLOAT, TP_INT, TP_STRING, TP_UINT, TP_VOID);
 
     @NotNull
     @Override
     public Lexer getHighlightingLexer() {
         return new FlexAdapter(new ObjDLexer((Reader) null));
     }
 
     @NotNull
     @Override
     public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
         if (KEYWORDS.contains(tokenType)) {
             return KEYWORD_KEYS;
         } else if (DATATYPES.contains(tokenType)) {
             return DATATYPE_KEYS;
         } else if (tokenType.equals(ObjDTypes.COMMENT)) {
             return COMMENT_KEYS;
         } else if (tokenType.equals(ObjDTypes.STRING)) {
             return STRING_KEYS;
         } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
             return BAD_CHAR_KEYS;
         } else {
             return EMPTY_KEYS;
         }
     }
 }
