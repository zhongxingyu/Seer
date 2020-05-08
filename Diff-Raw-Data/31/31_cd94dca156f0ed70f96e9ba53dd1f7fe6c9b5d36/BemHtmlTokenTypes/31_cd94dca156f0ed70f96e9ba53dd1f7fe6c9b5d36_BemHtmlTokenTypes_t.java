 package bem.idea.bemhtml.lang.lexer;
 
 import com.intellij.psi.TokenType;
 import com.intellij.psi.tree.IElementType;
 
 public interface BemHtmlTokenTypes {
 
     public static final IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
     public static final IElementType BAD_CHARACTER = new BemHtmlElementType("BAD_CHARACTER");
 
     public static final IElementType ERROR_TOO_MANY_VALUES = new BemHtmlElementType("ERROR_TOO_MANY_VALUES");
     public static final IElementType ERROR_ONE_BEM_VALUE_EXPECTED = new BemHtmlElementType("ERROR_ONE_BEM_VALUE_EXPECTED");
     public static final IElementType ERROR_TWO_BEM_VALUES_EXPECTED = new BemHtmlElementType("ERROR_TWO_BEM_VALUES_EXPECTED");
     public static final IElementType ERROR_WHITESPACE_EXPECTED = new BemHtmlElementType("ERROR_WHITESPACE_EXPECTED");
     public static final IElementType ERROR_UNEXPECTED_CHARACTER = new BemHtmlElementType("ERROR_UNEXPECTED_CHARACTER");
     public static final IElementType ERROR_UNFINISHED_ML_COMMENT = new BemHtmlElementType("ERROR_UNFINISHED_ML_COMMENT");
     public static final IElementType ERROR_PUNCTUATION_EXPECTED = new BemHtmlElementType("ERROR_PUNCTUATION_EXPECTED");
    public static final IElementType ERROR_INVALID_JSON_VALUE = new BemHtmlElementType("ERROR_INVALID_JSON_VALUE");
 
     public static final IElementType KEYWORDS_DELIM = new BemHtmlElementType("KEYWORDS_DELIM");
     public static final IElementType KEYWORDS_COLON = new BemHtmlElementType("KEYWORDS_COLON");
     public static final IElementType KEYWORDS_VALUE = new BemHtmlElementType("KEYWORDS_VALUE");
     public static final IElementType BEM_VALUE = new BemHtmlElementType("BEM_VALUE");
     public static final IElementType JS_EXPRESSION = new BemHtmlElementType("JS_EXPRESSION");
     public static final IElementType JSON_PROPERTY = new BemHtmlElementType("JSON_PROPERTY");
 
     public static final IElementType KEYWORD_BLOCK = new BemHtmlElementType("KEYWORD_BLOCK");
     public static final IElementType KEYWORD_ELEM = new BemHtmlElementType("KEYWORD_ELEM");
     public static final IElementType KEYWORD_MOD = new BemHtmlElementType("KEYWORD_MOD");
     public static final IElementType KEYWORD_ELEMMOD = new BemHtmlElementType("KEYWORD_ELEMMOD");
 
     public static final IElementType KEYWORD_DEFAULT = new BemHtmlElementType("KEYWORD_DEFAULT");
     public static final IElementType KEYWORD_TAG = new BemHtmlElementType("KEYWORD_TAG");
     public static final IElementType KEYWORD_ATTRS = new BemHtmlElementType("KEYWORD_ATTRS");
     public static final IElementType KEYWORD_CLS = new BemHtmlElementType("KEYWORD_CLS");
     public static final IElementType KEYWORD_BEM = new BemHtmlElementType("KEYWORD_BEM");
     public static final IElementType KEYWORD_JS = new BemHtmlElementType("KEYWORD_JS");
     public static final IElementType KEYWORD_JSATTR = new BemHtmlElementType("KEYWORD_JSATTR");
     public static final IElementType KEYWORD_MIX = new BemHtmlElementType("KEYWORD_MIX");
     public static final IElementType KEYWORD_CONTENT = new BemHtmlElementType("KEYWORD_CONTENT");
 
     public static final IElementType SL_COMMENT = new BemHtmlElementType("SL_COMMENT");
     public static final IElementType ML_COMMENT = new BemHtmlElementType("ML_COMMENT");
 
     public static final IElementType LEFT_BRACE = new BemHtmlElementType("LEFT_BRACE");
     public static final IElementType RIGHT_BRACE = new BemHtmlElementType("RIGHT_BRACE");
 
     public static final IElementType JAVASCRIPT_CODE = new BemHtmlElementType("JAVASCRIPT_CODE");
 
 }
