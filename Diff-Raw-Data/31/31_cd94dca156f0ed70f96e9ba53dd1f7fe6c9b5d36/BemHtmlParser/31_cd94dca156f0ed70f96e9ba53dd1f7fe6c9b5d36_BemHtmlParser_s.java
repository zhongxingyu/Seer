 package bem.idea.bemhtml.lang.parser;
 
 import com.intellij.lang.ASTNode;
 import com.intellij.lang.PsiBuilder;
 import com.intellij.lang.PsiParser;
 import com.intellij.psi.tree.IElementType;
 import org.jetbrains.annotations.NotNull;
 
 import static bem.idea.bemhtml.lang.lexer.BemHtmlTokenTypes.*;
 
 public class BemHtmlParser implements PsiParser {
 
     @NotNull
     public ASTNode parse(IElementType root, PsiBuilder builder) {
         final PsiBuilder.Marker rootMarker = builder.mark();
 
         while (!builder.eof()) {
             parse(builder);
         }
         rootMarker.done(root);
 
         return builder.getTreeBuilt();
     }
 
     private void parse(PsiBuilder builder) {
         IElementType first = builder.getTokenType();
         if (first == JAVASCRIPT_CODE ||
                 first == KEYWORDS_VALUE ||
                 first == KEYWORDS_DELIM ||
                 first == KEYWORDS_COLON ||
                 first == BEM_VALUE ||
                 first == JS_EXPRESSION ||
                 first == JSON_PROPERTY ||
                 first == SL_COMMENT ||
                 first == ML_COMMENT ||
                 first == ERROR_TOO_MANY_VALUES ||
                 first == ERROR_ONE_BEM_VALUE_EXPECTED ||
                 first == ERROR_TWO_BEM_VALUES_EXPECTED ||
                 first == ERROR_WHITESPACE_EXPECTED ||
                 first == ERROR_UNEXPECTED_CHARACTER ||
                 first == ERROR_UNFINISHED_ML_COMMENT ||
                 first == ERROR_PUNCTUATION_EXPECTED ||
                 first == KEYWORD_ATTRS ||
                 first == KEYWORD_BEM ||
                 first == KEYWORD_BLOCK ||
                 first == KEYWORD_CLS ||
                 first == KEYWORD_CONTENT ||
                 first == KEYWORD_DEFAULT ||
                 first == KEYWORD_ELEM ||
                 first == KEYWORD_ELEMMOD ||
                 first == KEYWORD_JS ||
                 first == KEYWORD_JSATTR ||
                 first == KEYWORD_MIX ||
                 first == KEYWORD_MOD ||
                 first == KEYWORD_TAG) {
             parseToken(builder, first);
         } else {
             builder.advanceLexer();
         }
     }
 
     private void parseToken(PsiBuilder builder, IElementType type) {
         PsiBuilder.Marker marker = builder.mark();
         builder.advanceLexer();
         marker.done(type);
     }
 
 }
