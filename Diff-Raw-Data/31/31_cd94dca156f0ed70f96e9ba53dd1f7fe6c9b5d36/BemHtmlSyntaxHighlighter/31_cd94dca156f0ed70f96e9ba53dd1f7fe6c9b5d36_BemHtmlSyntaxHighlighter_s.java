 package bem.idea.bemhtml.highlighter;
 
 import bem.idea.bemhtml.lang.lexer.BemHtmlHighlighterLexer;
 import bem.idea.bemhtml.lang.lexer.BemHtmlTokenTypes;
 import com.intellij.lexer.Lexer;
 import com.intellij.openapi.editor.HighlighterColors;
 import com.intellij.openapi.editor.colors.TextAttributesKey;
 import com.intellij.openapi.editor.markup.EffectType;
 import com.intellij.openapi.editor.markup.TextAttributes;
 import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
 import com.intellij.psi.tree.IElementType;
 import org.jetbrains.annotations.NotNull;
 
 import java.awt.*;
 import java.util.HashMap;
 import java.util.Map;
 
 public class BemHtmlSyntaxHighlighter extends SyntaxHighlighterBase {
 
     private final BemHtmlHighlighterLexer lexer = new BemHtmlHighlighterLexer();
 
     private static final TextAttributes taBemEntityKeyword = new TextAttributes(new Color(0, 0, 139), null, null, null, Font.BOLD);
     private static final TextAttributes taBemModKeyword = new TextAttributes(new Color(0, 0, 139), null, null, null, Font.BOLD);
     private static final TextAttributes taBemValue = new TextAttributes(new Color(69, 139, 0), null, null, null, Font.BOLD);
     private static final TextAttributes taJSONProperty = new TextAttributes(new Color(128, 128, 0), null, null, null, Font.BOLD);
     private static final TextAttributes taJSBraces = new TextAttributes(new Color(0, 0, 0), null, null, null, Font.PLAIN);
     private static final TextAttributes taBemKeywordsDelim = new TextAttributes(new Color(0, 0, 0), null, null, null, Font.PLAIN);
     private static final TextAttributes taBemKeywordsColon = new TextAttributes(new Color(0, 0, 0), null, null, null, Font.PLAIN);
     private static final TextAttributes taBemError = new TextAttributes(new Color(0, 0, 0), null, new Color(255, 0, 0), EffectType.WAVE_UNDERSCORE, Font.PLAIN);
     private static final TextAttributes taBemComment = new TextAttributes(new Color(128, 128, 0), null, null, null, Font.ITALIC);
 
     private static final Map<IElementType, TextAttributesKey> TOKENS_TO_STYLES;
 
     @NotNull
     public Lexer getHighlightingLexer() {
         return lexer;
     }
 
     static final TextAttributesKey BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
             "bemhtml.bem.error", taBemError);
 
     static final TextAttributesKey KEYWORDS_DELIM = TextAttributesKey.createTextAttributesKey(
             "bemhtml.keywords.delim", taBemKeywordsDelim);
 
     static final TextAttributesKey KEYWORDS_COLON = TextAttributesKey.createTextAttributesKey(
             "bemhtml.keywords.colon", taBemKeywordsColon);
 
     static final TextAttributesKey BEM_VALUE = TextAttributesKey.createTextAttributesKey(
             "bemhtml.bem.value", taBemValue);
 
     static final TextAttributesKey BEM_SL_COMMENT = TextAttributesKey.createTextAttributesKey(
             "bemhtml.bem.slcomment", taBemComment);
 
     static final TextAttributesKey BEM_ML_COMMENT = TextAttributesKey.createTextAttributesKey(
             "bemhtml.bem.mlcomment", taBemComment);
 
     static final TextAttributesKey JSON_PROPERTY = TextAttributesKey.createTextAttributesKey(
             "bemhtml.json.property", taJSONProperty);
 
     static final TextAttributesKey KEYWORD_ENTITY = TextAttributesKey.createTextAttributesKey(
             "bemhtml.keyword.entity", taBemEntityKeyword);
 
     static final TextAttributesKey KEYWORD_MOD = TextAttributesKey.createTextAttributesKey(
             "bemhtml.keyword.mod", taBemModKeyword);
 
     static final TextAttributesKey BRACES = TextAttributesKey.createTextAttributesKey(
             "bemhtml.braces", taJSBraces);
 
     static {
         TOKENS_TO_STYLES = new HashMap<IElementType, TextAttributesKey>();
 
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.WHITE_SPACE, HighlighterColors.TEXT);
 
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.BAD_CHARACTER, BAD_CHARACTER);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.ERROR_ONE_BEM_VALUE_EXPECTED, BAD_CHARACTER);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.ERROR_TWO_BEM_VALUES_EXPECTED, BAD_CHARACTER);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.ERROR_UNEXPECTED_CHARACTER, BAD_CHARACTER);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.ERROR_WHITESPACE_EXPECTED, BAD_CHARACTER);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.ERROR_TOO_MANY_VALUES, BAD_CHARACTER);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.ERROR_UNFINISHED_ML_COMMENT, BAD_CHARACTER);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.ERROR_PUNCTUATION_EXPECTED, BAD_CHARACTER);
 
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORDS_DELIM, KEYWORDS_DELIM);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORDS_COLON, KEYWORDS_COLON);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.BEM_VALUE, BEM_VALUE);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.JSON_PROPERTY, JSON_PROPERTY);
 
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.SL_COMMENT, BEM_SL_COMMENT);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.ML_COMMENT, BEM_ML_COMMENT);
 
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_BLOCK, KEYWORD_ENTITY);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_ELEM, KEYWORD_ENTITY);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_MOD, KEYWORD_ENTITY);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_ELEMMOD, KEYWORD_ENTITY);
 
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_DEFAULT, KEYWORD_MOD);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_TAG, KEYWORD_MOD);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_ATTRS, KEYWORD_MOD);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_CLS, KEYWORD_MOD);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_BEM, KEYWORD_MOD);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_JS, KEYWORD_MOD);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_JSATTR, KEYWORD_MOD);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_MIX, KEYWORD_MOD);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.KEYWORD_CONTENT, KEYWORD_MOD);
 
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.LEFT_BRACE, BRACES);
         TOKENS_TO_STYLES.put(BemHtmlTokenTypes.RIGHT_BRACE, BRACES);
     }
 
     @NotNull
     public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
         return pack(TOKENS_TO_STYLES.get(tokenType));
     }
 
 }
