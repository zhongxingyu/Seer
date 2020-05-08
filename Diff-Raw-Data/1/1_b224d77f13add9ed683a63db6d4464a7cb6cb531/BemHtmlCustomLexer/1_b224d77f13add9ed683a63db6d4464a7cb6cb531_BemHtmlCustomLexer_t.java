 package bem.idea.bemhtml.lang.lexer.custom;
 
 import bem.idea.bemhtml.lang.lexer.BemHtmlTokenTypes;
 import com.intellij.psi.tree.IElementType;
 
 import java.util.*;
 
 public class BemHtmlCustomLexer {
 
     private String src;
     private List<BHToken> tokens;
     private List<BHBrace> bBraces;
     private Map<Integer, Integer> bBracesIdx;
     private int current = -1;
 
     public BemHtmlCustomLexer() {}
 
     public void parse(String src) {
         this.src = src;
         current = -1;
         init();
         tokenize();
         tokens = retokenize();
         validate();
     }
 
     public void next() {
         current++;
     }
 
     public IElementType getType() {
         if (current < tokens.size()) return types.get(tokens.get(current).getType());
         return null;
     }
 
     public int getPushback(int yyl) {
         if (current < tokens.size()) {
             BHToken t = tokens.get(current);
             return yyl - (t.getEnd() - t.getStart() + 1);
         }
         return 0;
     }
 
     private void init() {
         tokens = new ArrayList<BHToken>();
         bBraces = new ArrayList<BHBrace>();
         bBracesIdx = new HashMap<Integer, Integer>();
     }
 
     private void tokenize() {
         StringBuilder sb = new StringBuilder();
 
         int x, bs, ts, i, j, l;
         char c, nc;
         BHToken toAdd, last = null;
 
         boolean bDelayL, bDelayR;
         BHBrace lub;
 
         boolean skip;
 
         if (src != null) {
             for (i = 0, j = i + 1, l = src.length(); i < l; i++, j = i + 1) {
                 toAdd = null;
                 bDelayL = false;
                 bDelayR = false;
                 skip = false;
 
                 c = src.charAt(i);
                 if (c == '"' || c == '\'') {
                     x = findStringEnd(src, c, i + 1);
                     if (x == -1) {
                         toAdd = new BHToken(BHTokenType.ERROR, i, l - 1);
                         i = l - 1;
                     } else {
                         toAdd = new BHToken(BHTokenType.STRING, i, x);
                         i = x;
                     }
                 } else {
                     switch(c) {
                         case ' ': case '\t':
                             if (last != null && last.getType() == BHTokenType.WHITESPACE) {
                                 last.increment();
                                 toAdd = last;
                                 skip = true;
                             } else toAdd = new BHToken(BHTokenType.WHITESPACE, i, i);
                             break;
                         case '\n': case '\r':
                             if (last != null && last.getType() == BHTokenType.NEWLINE) {
                                 last.increment();
                                 toAdd = last;
                                 skip = true;
                             } else toAdd = new BHToken(BHTokenType.NEWLINE, i, i);
                             break;
                         case '/':
                             if (j < l) {
                                 nc = src.charAt(j);
                                 if (nc == '/') {
                                     if ((x = findSLCommentEnd(src, j)) != j) {
                                         toAdd = new BHToken(BHTokenType.SL_COMMENT, i, x);
                                         i = x;
                                     } else {
                                         toAdd = new BHToken(BHTokenType.SL_COMMENT, i, j);
                                         i = j;
                                     }
                                     break;
                                 } else if (nc == '*') {
                                     if ((x = findMLCommentEnd(src, j)) != -1) {
                                         toAdd = new BHToken(BHTokenType.ML_COMMENT, i, x);
                                         i = x;
                                     } else {
                                         toAdd = new BHToken(BHTokenType.ERROR_UNFINISHED_ML_COMMENT, i, l - 1);
                                         i = l;
                                     }
                                     break;
                                 }
                             }
                             if (last != null && last.getType() == BHTokenType.OPERATOR) {
                                 last.increment();
                                 skip = true;
                             } else toAdd = new BHToken(BHTokenType.OPERATOR, i, i);
                             break;
                         case '{':
                             toAdd = new BHToken(BHTokenType.L_BBRACE, i, i);
                             bDelayL = true;
                             break;
                         case '}':
                             toAdd = new BHToken(BHTokenType.R_BBRACE, i, i);
                             bDelayR = true;
                             break;
                         case '(':
                             x = findBraceEnd(i, '(', ')');
                             if (x == -1) {
                                 toAdd = new BHToken(BHTokenType.ERROR, i, l - 1);
                                 i = l - 1;
                             } else {
                                 toAdd = new BHToken(BHTokenType.RB_BLOCK, i, x);
                                 i = x;
                             }
                             break;
                         case '[':
                             x = findBraceEnd(i, '[', ']');
                             if (x == -1) {
                                 toAdd = new BHToken(BHTokenType.ERROR, i, l - 1);
                                 i = l - 1;
                             } else {
                                 toAdd = new BHToken(BHTokenType.SB_BLOCK, i, x);
                                 i = x;
                             }
                             break;
                         case ')': case ']':
                             toAdd = new BHToken(BHTokenType.ERROR, i, i); break;
                         case ':':
                             toAdd = new BHToken(BHTokenType.COLON, i, i); break;
                         case ';':
                             toAdd = new BHToken(BHTokenType.SEMICOLON, i, i); break;
                         case '.':
                             toAdd = new BHToken(BHTokenType.DOT, i, i); break;
                         case ',':
                             toAdd = new BHToken(BHTokenType.COMMA, i, i); break;
                         case '?':
                             toAdd = new BHToken(BHTokenType.IFQ, i, i); break;
                         case '+': case '-': case '*': case '%': case '=':
                         case '!': case '<': case '>': case '&': case '|':
                             if (last != null && last.getType() == BHTokenType.OPERATOR) {
                                 last.increment();
                                toAdd = last;
                                 skip = true;
                             } else toAdd = new BHToken(BHTokenType.OPERATOR, i, i);
                             break;
                         default:
                             sb.append(c);
                     }
                 }
 
                 last = toAdd;
                 if (!skip && toAdd != null) {
                     if (sb.length() > 0) {
                         tokens.add(new BHToken(BHTokenType.IDENT, toAdd.getStart() - sb.length(), toAdd.getStart() - 1));
                         sb = new StringBuilder();
                     }
                     tokens.add(toAdd);
                 }
 
                 ts = tokens.size();
                 if (bDelayL) {
                     bBraces.add(new BHBrace(ts - 1));
                 } else if (bDelayR) {
                     bs = bBraces.size();
                     lub = findLastUnmatchedBrace(bBraces);
                     if (bs == 0 || lub == null) toAdd.invalidate(BHTokenType.ERROR);
                     else {
                         lub.setR(ts - 1);
                         bBracesIdx.put(lub.getL(), lub.getR());
                     }
                 }
             }
             if (sb.length() > 0) tokens.add(new BHToken(BHTokenType.IDENT, i - sb.length(), i - 1));
         }
     }
 
     private BHBrace findLastUnmatchedBrace(List<BHBrace> braces) {
         BHBrace b;
         for (int i = braces.size() - 1; i > -1; i--) {
             b = braces.get(i);
             if (b.getR() == -1) return b;
         }
         return null;
     }
 
     private int findBraceEnd(int i, char lb, char rb) {
         int bc = 1, x;
         char c;
         i++;
         for (int l = src.length(); i < l; i++) {
             c = src.charAt(i);
             if (c == lb) {
                 bc++;
             } else if (c == rb) {
                 bc--;
                 if (bc == 0) return i;
             } else if (c == '"' || c == '\'') {
                 x = findStringEnd(src, c, i + 1);
                 if (x == -1) return -1;
                 else i = x;
             }
         }
         return -1;
     }
 
     private static Map<String, BHTokenType> bemKwd0;
     private static Map<String, BHTokenType> bemKwd1;
     private static Map<BHTokenType, IElementType> types;
     private static Set<BHTokenType> invalidateBemValueSet;
     private static Set<BHTokenType> invalidateValueOrJSSet;
     private static Set<BHTokenType> aloneTypesSet;
     private static Set<BHTokenType> bemTypesSet;
     private static Set<BHTokenType> invalidateKwd1Set;
     private static Set<BHTokenType> invalidateAfterCommaSet;
     private static Set<BHTokenType> wantJSONSet;
     private static Set<BHTokenType> skipBeforeColonSet;
     private static Set<BHTokenType> ignoreSet0;
     private static Set<BHTokenType> ignoreSet1;
 
     static {
         bemKwd0 = new HashMap<String, BHTokenType>();
         bemKwd0.put("block", BHTokenType.BH_BLOCK);
         bemKwd0.put("elem", BHTokenType.BH_ELEM);
         bemKwd0.put("mod", BHTokenType.BH_MOD);
         bemKwd0.put("elemMod", BHTokenType.BH_ELEMMOD);
 
         bemKwd1 = new HashMap<String, BHTokenType>();
         bemKwd1.put("default", BHTokenType.BH_DEFAULT);
         bemKwd1.put("tag", BHTokenType.BH_TAG);
         bemKwd1.put("attrs", BHTokenType.BH_ATTRS);
         bemKwd1.put("cls", BHTokenType.BH_CLS);
         bemKwd1.put("bem", BHTokenType.BH_BEM);
         bemKwd1.put("js", BHTokenType.BH_JS);
         bemKwd1.put("jsAttr", BHTokenType.BH_JSATTR);
         bemKwd1.put("mix", BHTokenType.BH_MIX);
         bemKwd1.put("content", BHTokenType.BH_CONTENT);
 
         types = new HashMap<BHTokenType, IElementType>();
 
         types.put(BHTokenType.BH_BLOCK, BemHtmlTokenTypes.KEYWORD_BLOCK);
         types.put(BHTokenType.BH_ELEM, BemHtmlTokenTypes.KEYWORD_ELEM);
         types.put(BHTokenType.BH_MOD, BemHtmlTokenTypes.KEYWORD_MOD);
         types.put(BHTokenType.BH_ELEMMOD, BemHtmlTokenTypes.KEYWORD_ELEMMOD);
 
         types.put(BHTokenType.BH_DEFAULT, BemHtmlTokenTypes.KEYWORD_DEFAULT);
         types.put(BHTokenType.BH_TAG, BemHtmlTokenTypes.KEYWORD_TAG);
         types.put(BHTokenType.BH_ATTRS, BemHtmlTokenTypes.KEYWORD_ATTRS);
         types.put(BHTokenType.BH_CLS, BemHtmlTokenTypes.KEYWORD_CLS);
         types.put(BHTokenType.BH_BEM, BemHtmlTokenTypes.KEYWORD_BEM);
         types.put(BHTokenType.BH_JS, BemHtmlTokenTypes.KEYWORD_JS);
         types.put(BHTokenType.BH_JSATTR, BemHtmlTokenTypes.KEYWORD_JSATTR);
         types.put(BHTokenType.BH_MIX, BemHtmlTokenTypes.KEYWORD_MIX);
         types.put(BHTokenType.BH_CONTENT, BemHtmlTokenTypes.KEYWORD_CONTENT);
 
         types.put(BHTokenType.ERROR, BemHtmlTokenTypes.BAD_CHARACTER);
         types.put(BHTokenType.ERROR_TOO_MANY_VALUES, BemHtmlTokenTypes.ERROR_TOO_MANY_VALUES);
         types.put(BHTokenType.ERROR_WHITESPACE_EXPECTED, BemHtmlTokenTypes.ERROR_WHITESPACE_EXPECTED);
         types.put(BHTokenType.ERROR_ONE_BEM_VALUE_EXPECTED, BemHtmlTokenTypes.ERROR_ONE_BEM_VALUE_EXPECTED);
         types.put(BHTokenType.ERROR_TWO_BEM_VALUES_EXPECTED, BemHtmlTokenTypes.ERROR_TWO_BEM_VALUES_EXPECTED);
         types.put(BHTokenType.ERROR_UNEXPECTED_CHARACTER, BemHtmlTokenTypes.ERROR_UNEXPECTED_CHARACTER);
         types.put(BHTokenType.ERROR_UNFINISHED_ML_COMMENT, BemHtmlTokenTypes.ERROR_UNFINISHED_ML_COMMENT);
         types.put(BHTokenType.ERROR_PUNCTUATION_EXPECTED, BemHtmlTokenTypes.ERROR_PUNCTUATION_EXPECTED);
         types.put(BHTokenType.ERROR_INVALID_JSON_VALUE, BemHtmlTokenTypes.ERROR_INVALID_JSON_VALUE);
         types.put(BHTokenType.ERROR_BEM_OR_JS_EXPECTED, BemHtmlTokenTypes.ERROR_BEM_OR_JS_EXPECTED);
 
         types.put(BHTokenType.COLON, BemHtmlTokenTypes.KEYWORDS_COLON);
         types.put(BHTokenType.COMMA, BemHtmlTokenTypes.KEYWORDS_DELIM);
         types.put(BHTokenType.L_BBRACE, BemHtmlTokenTypes.LEFT_BRACE);
         types.put(BHTokenType.R_BBRACE, BemHtmlTokenTypes.RIGHT_BRACE);
         types.put(BHTokenType.BEM_VALUE, BemHtmlTokenTypes.BEM_VALUE);
         types.put(BHTokenType.WHITESPACE, BemHtmlTokenTypes.WHITE_SPACE);
         types.put(BHTokenType.NEWLINE, BemHtmlTokenTypes.WHITE_SPACE);
         types.put(BHTokenType.JAVASCRIPT, BemHtmlTokenTypes.JAVASCRIPT_CODE);
         types.put(BHTokenType.JS_EXPRESSION, BemHtmlTokenTypes.JS_EXPRESSION);
         types.put(BHTokenType.BH_JSONPROP, BemHtmlTokenTypes.JSON_PROPERTY);
 
         types.put(BHTokenType.SL_COMMENT, BemHtmlTokenTypes.SL_COMMENT);
         types.put(BHTokenType.ML_COMMENT, BemHtmlTokenTypes.ML_COMMENT);
 
         invalidateBemValueSet = new HashSet<BHTokenType>();
         invalidateBemValueSet.add(BHTokenType.COLON);
         invalidateBemValueSet.add(BHTokenType.COMMA);
         invalidateBemValueSet.add(BHTokenType.NEWLINE);
         invalidateBemValueSet.add(BHTokenType.L_BBRACE);
         invalidateBemValueSet.add(BHTokenType.R_BBRACE);
 
         invalidateValueOrJSSet = new HashSet<BHTokenType>();
         invalidateValueOrJSSet.add(BHTokenType.JS_EXPRESSION);
         invalidateValueOrJSSet.add(BHTokenType.BEM_VALUE);
 
         aloneTypesSet = new HashSet<BHTokenType>();
         aloneTypesSet.add(BHTokenType.BH_DEFAULT);
         aloneTypesSet.add(BHTokenType.BH_TAG);
         aloneTypesSet.add(BHTokenType.BH_ATTRS);
         aloneTypesSet.add(BHTokenType.BH_CLS);
         aloneTypesSet.add(BHTokenType.BH_BEM);
         aloneTypesSet.add(BHTokenType.BH_JS);
         aloneTypesSet.add(BHTokenType.BH_JSATTR);
         aloneTypesSet.add(BHTokenType.BH_MIX);
         aloneTypesSet.add(BHTokenType.BH_CONTENT);
 
         bemTypesSet = new HashSet<BHTokenType>();
         bemTypesSet.add(BHTokenType.BH_BLOCK);
         bemTypesSet.add(BHTokenType.BH_ELEM);
         bemTypesSet.add(BHTokenType.BH_MOD);
         bemTypesSet.add(BHTokenType.BH_ELEMMOD);
 
         invalidateKwd1Set = new HashSet<BHTokenType>();
         invalidateKwd1Set.add(BHTokenType.COMMA);
         invalidateKwd1Set.add(BHTokenType.COLON);
         invalidateKwd1Set.add(BHTokenType.L_BBRACE);
         invalidateKwd1Set.add(BHTokenType.JAVASCRIPT);
 
         invalidateAfterCommaSet = new HashSet<BHTokenType>();
         invalidateAfterCommaSet.add(BHTokenType.JS_EXPRESSION);
         invalidateAfterCommaSet.add(BHTokenType.BH_JSONPROP);
         invalidateAfterCommaSet.addAll(aloneTypesSet);
         invalidateAfterCommaSet.addAll(bemTypesSet);
 
         ignoreSet0 = new HashSet<BHTokenType>();
         ignoreSet0.add(BHTokenType.WHITESPACE);
         ignoreSet0.add(BHTokenType.SL_COMMENT);
         ignoreSet0.add(BHTokenType.ML_COMMENT);
 
         ignoreSet1 = new HashSet<BHTokenType>();
         ignoreSet1.addAll(ignoreSet0);
         ignoreSet1.add(BHTokenType.NEWLINE);
 
         wantJSONSet = new HashSet<BHTokenType>();
         wantJSONSet.add(BHTokenType.WHITESPACE);
         wantJSONSet.add(BHTokenType.SL_COMMENT);
         wantJSONSet.add(BHTokenType.ML_COMMENT);
         wantJSONSet.add(BHTokenType.L_BBRACE);
         wantJSONSet.add(BHTokenType.ERROR_UNFINISHED_ML_COMMENT);
         wantJSONSet.add(BHTokenType.NEWLINE);
 
         skipBeforeColonSet = new HashSet<BHTokenType>();
         skipBeforeColonSet.add(BHTokenType.WHITESPACE);
         skipBeforeColonSet.add(BHTokenType.SL_COMMENT);
         skipBeforeColonSet.add(BHTokenType.ML_COMMENT);
     }
 
     private List<BHToken> retokenize() {
         List<BHToken> _tokens = new ArrayList<BHToken>();
         BHToken t, nt = null, t0, t1;
         BHTokenType tt, ntt = null, bvt;
         String v;
         int i, j, l, x;
         boolean wantJSON = false;
         for (i = 0, j = 1, l = tokens.size(); i < l; i++, j = i + 1) {
             t = tokens.get(i);
             tt = t.getType();
             if (j < l) {
                 nt = tokens.get(j); ntt = nt.getType();
             }
 
             v = src.substring(t.getStart(), t.getEnd() + 1);
 
             if (wantJSON && !wantJSONSet.contains(tt)) {
                 if ((x = addJSExpression(i, _tokens)) != -1) i = x;
                 else _tokens.add(t);
                 wantJSON = false;
             } else if (tt == BHTokenType.L_BBRACE) {
                 if (wantJSON) {
                     if (isJSONBlock(j)) {
                         _tokens.add(t);
                     } else {
                         if (bBracesIdx.containsKey(i)) {
                             x = bBracesIdx.get(i);
                             t1 = tokens.get(x);
                             _tokens.add(new BHToken(BHTokenType.JAVASCRIPT, t.getStart(), t1.getEnd()));
                             i = x;
                         } else {
                             _tokens.add(new BHToken(BHTokenType.JAVASCRIPT, t.getStart(), tokens.get(l - 1).getEnd()));
                             i = l;
                         }
                     }
                     wantJSON = false;
                 } else _tokens.add(t);
             } else if (tt == BHTokenType.IDENT) {
                 if (isJSONProperty(i)) {
                     if (bemKwd1.containsKey(v)) t.setType(bemKwd1.get(v));
                     else t.setType(BHTokenType.BH_JSONPROP);
                     _tokens.add(t);
                 } else if (bemKwd0.containsKey(v)) {
                     t.setType(bemKwd0.get(v));
                     _tokens.add(t);
                     if (nt != null && ntt == BHTokenType.WHITESPACE) {
                         _tokens.add(nt);
                         x = getJSExpression(j + 1);
                         if (x > j) {
                             t0 = tokens.get(j + 1);
                             t1 = tokens.get(x);
                             bvt = src.substring(t0.getStart(), t1.getEnd() + 1).matches("^[\\w\\-]+$") ? BHTokenType.BEM_VALUE : BHTokenType.JS_EXPRESSION;
                             if (t0 != t1) {
                                 _tokens.add(new BHToken(bvt, t0.getStart(), t1.getEnd()));
                             } else {
                                 t0.setType(bvt);
                                 _tokens.add(t0);
                             }
                             i = x;
                             j = x + 1;
                             if (j < l && (v.equals("mod") || v.equals("elemMod"))) {
                                 nt = tokens.get(j); ntt = nt.getType();
                                 if (ntt == BHTokenType.WHITESPACE) {
                                     _tokens.add(nt);
                                     x = getJSExpression(j + 1);
                                     if (x > j) {
                                         t0 = tokens.get(j + 1);
                                         t1 = tokens.get(x);
                                         bvt = src.substring(t0.getStart(), t1.getEnd() + 1).matches("^[\\w\\-]+$") ? BHTokenType.BEM_VALUE : BHTokenType.JS_EXPRESSION;
                                         if (t0 != t1) {
                                             _tokens.add(new BHToken(bvt, t0.getStart(), t1.getEnd()));
                                         } else {
                                             t0.setType(bvt);
                                             _tokens.add(t0);
                                         }
                                         i = x;
                                     } else i++;
                                 }
                             }
                         } else i++;
                     }
                 } else if (bemKwd1.containsKey(v)) {
                     t.setType(bemKwd1.get(v));
                     _tokens.add(t);
                 } else if ((x = addJSExpression(i, _tokens)) != -1) i = x;
             } else if (tt == BHTokenType.OPERATOR ||
                        tt == BHTokenType.STRING ||
                        tt == BHTokenType.RB_BLOCK ||
                        tt == BHTokenType.SB_BLOCK) {
                 if ((x = addJSExpression(i, _tokens)) != -1) i = x;
             } else if (tt == BHTokenType.COLON) {
                 if (!isWrongColon(_tokens, _tokens.size() - 1)) wantJSON = true;
                 _tokens.add(t);
             } else {
                 _tokens.add(t);
             }
             nt = null; ntt = null;
         }
 
         return _tokens;
     }
 
     private boolean isJSONBlock(int i) {
         BHToken t;
         BHTokenType tt;
         for (int l = tokens.size(); i < l; i++) {
             t = tokens.get(i);
             tt = t.getType();
             if (tt == BHTokenType.IDENT) {
                 return isJSONProperty(i);
             } else if (tt != BHTokenType.WHITESPACE && tt != BHTokenType.NEWLINE) {
                 return false;
             }
         }
         return false;
     }
 
     private void validate() {
         BHToken t;
         BHTokenType tt;
         BHList sub;
         boolean valid;
         int x;
         for (int i = 0, l = tokens.size(); i < l; i++) {
             t = tokens.get(i);
             tt = t.getType();
             if (tt == BHTokenType.OPERATOR ||
                     tt == BHTokenType.SEMICOLON ||
                     tt == BHTokenType.DOT ||
                     tt == BHTokenType.IFQ) {
                 t.setType(BHTokenType.ERROR_UNEXPECTED_CHARACTER);
             } else if (tt == BHTokenType.BH_BLOCK || tt == BHTokenType.BH_ELEM) {
                 sub = getSemanticList(i + 1, 1, ignoreSet0);
                 if (sub.getFiltered().size() == 1) {
                     valid = validateList(sub.getFiltered(), invalidateValueOrJSSet, BHTokenType.ERROR_ONE_BEM_VALUE_EXPECTED);
 
                     if (valid) validateTill(i + sub.getAll().size() + 1, invalidateBemValueSet, BHTokenType.ERROR_TOO_MANY_VALUES);
 
                     i += sub.getAll().size();
                 }
             } else if (tt == BHTokenType.BH_MOD || tt == BHTokenType.BH_ELEMMOD) {
                 sub = getSemanticList(i + 1, 2, ignoreSet0);
                 if (sub.getFiltered().size() == 2) {
                     valid = validateList(sub.getFiltered(), invalidateValueOrJSSet, BHTokenType.ERROR_ONE_BEM_VALUE_EXPECTED);
 
                     if (valid) validateTill(i + sub.getAll().size() + 1, invalidateBemValueSet, BHTokenType.ERROR_TOO_MANY_VALUES);
 
                     i += sub.getAll().size();
                 }
             } else if (tt == BHTokenType.COLON) {
                 if (isWrongColon(tokens, i - 1)) {
                     t.invalidate(BHTokenType.ERROR_UNEXPECTED_CHARACTER);
                 } else if (i + 1 < l) {
                     if ((x = isValidJSONValue(i + 1)) != -1) {
                         tokens.get(x).invalidate(BHTokenType.ERROR_INVALID_JSON_VALUE);
                     }
                 }
             } else if (tt == BHTokenType.COMMA) {
                 sub = getSemanticList(i + 1, 1, ignoreSet1);
                 if (sub.getFiltered().size() == 1) {
                     if (!validateList(sub.getFiltered(), invalidateAfterCommaSet, BHTokenType.ERROR_BEM_OR_JS_EXPECTED)) {
                         i += sub.getAll().size();
                     }
                 }
             } else if (aloneTypesSet.contains(tt)) {
                 sub = getSemanticList(i + 1, 1, ignoreSet0);
                 if (sub.getFiltered().size() == 1) {
                     if (!validateList(sub.getFiltered(), invalidateKwd1Set, BHTokenType.ERROR_PUNCTUATION_EXPECTED)) {
                         i += sub.getAll().size();
                     }
                 }
             } else if (tt == BHTokenType.JS_EXPRESSION) {
                 validateTill(i + 1, invalidateBemValueSet, BHTokenType.ERROR_TOO_MANY_VALUES);
             }
         }
     }
 
     private boolean isWrongColon(List<BHToken> tokens, int i) {
         BHTokenType tt;
         for (; i > -1; i--) {
             tt = tokens.get(i).getType();
             if (!skipBeforeColonSet.contains(tt)) {
                 return (!aloneTypesSet.contains(tt) &&
                        !bemTypesSet.contains(tt) &&
                        tt != BHTokenType.JS_EXPRESSION &&
                        tt != BHTokenType.BH_JSONPROP) ||
                        wasColon(tokens, i);
             }
         }
         return true;
     }
 
     private boolean wasColon(List<BHToken> tokens, int i) {
         BHTokenType tt;
         for (; i > -1; i--) {
             tt = tokens.get(i).getType();
             if (!skipBeforeColonSet.contains(tt)) {
                 switch (tt) {
                     case COMMA:
                     case NEWLINE:
                         return false;
                     case COLON:
                         return true;
                 }
             }
         }
         return false;
     }
 
     private boolean validateList(List<BHToken> tokens,
                                  Set<BHTokenType> expected,
                                  BHTokenType errorType) {
         BHToken t;
         BHTokenType tt;
         boolean valid = true;
         for (int i = 0, l = tokens.size(); i < l; i++) {
             t = tokens.get(i);
             tt = t.getType();
             if (!expected.contains(tt)) {
                 t.invalidate(errorType);
                 valid = false;
             }
         }
         return valid;
     }
 
     private BHList getSemanticList(int start, int num, Set<BHTokenType> ignoreSet) {
         List<BHToken> filtered = new ArrayList<BHToken>();
         List<BHToken> all = new ArrayList<BHToken>();
         BHToken t;
         for (int i = start, l = tokens.size(); i < l && filtered.size() < num; i++) {
             t = tokens.get(i);
             if (!ignoreSet.contains(t.getType())) filtered.add(t);
             all.add(t);
         }
         return new BHList(filtered, all);
     }
 
     private void validateTill(int i, Set<BHTokenType> tillSet, BHTokenType errorType) {
         BHToken t;
         BHTokenType tt;
         for (int l = tokens.size(); i < l; i++) {
             t = tokens.get(i);
             tt = t.getType();
             if (isSemanticToken(tt)) {
                 if (!tillSet.contains(tt)) t.invalidate(errorType);
                 else return;
             }
         }
     }
 
     private boolean isSemanticToken(BHTokenType tokenType) {
         return tokenType != BHTokenType.WHITESPACE &&
                tokenType != BHTokenType.SL_COMMENT &&
                tokenType != BHTokenType.ML_COMMENT;
     }
 
     private int isValidJSONValue(int i) {
         BHTokenType tt;
         for (int l = tokens.size(); i < l; i++) {
             tt = tokens.get(i).getType();
             if (tt == BHTokenType.ERROR_UNFINISHED_ML_COMMENT) return -1;
             if (tt != BHTokenType.WHITESPACE &&
                     tt != BHTokenType.SL_COMMENT &&
                     tt != BHTokenType.ML_COMMENT &&
                     tt != BHTokenType.NEWLINE) {
                 if (tt != BHTokenType.JS_EXPRESSION &&
                         tt != BHTokenType.L_BBRACE &&
                         tt != BHTokenType.JAVASCRIPT) return i;
                 else return -1;
             }
         }
         return -1;
     }
 
     private int addJSExpression(int i, List<BHToken> _tokens) {
         int x = getJSExpression(i);
         if (x >= i) {
             BHToken t0 = tokens.get(i);
             BHToken t1 = tokens.get(x);
             if (t0 != t1) {
                 _tokens.add(new BHToken(BHTokenType.JS_EXPRESSION, t0.getStart(), t1.getEnd()));
             } else {
                 t0.setType(BHTokenType.JS_EXPRESSION);
                 _tokens.add(t0);
             }
             return x;
         }
         return -1;
     }
 
     private int getJSExpression(int i) {
         int x = _getJSExpression(i);
         if (x > i - 1) {
             BHTokenType tt;
             for (; x > -1; x--) {
                 tt = tokens.get(x).getType();
                 if (tt != BHTokenType.SL_COMMENT &&
                     tt != BHTokenType.ML_COMMENT &&
                     tt != BHTokenType.WHITESPACE &&
                     tt != BHTokenType.NEWLINE) return x;
             }
         }
         return x;
     }
 
     private int _getJSExpression(int i) {
         int l = tokens.size(), x;
         if (i < l) {
             BHToken t;
             BHTokenType tt, ltt = null;
             boolean wasSCToken = false, ifQMode = false;
             for (; i < l; i++) {
                 t = tokens.get(i); tt = t.getType();
                 if (!isSemanticToken(tt)) {
                     if (ltt == BHTokenType.DOT) return i - 1;
                     wasSCToken = true;
                 } else {
                     switch (tt) {
                         case IDENT:
                             if (ltt != BHTokenType.OPERATOR &&
                                 ltt != BHTokenType.IFQ &&
                                 ltt != BHTokenType.DOT &&
                                 ltt != null) return i - (wasSCToken ? 2 : 1);
                             break;
                         case DOT:
                             if (wasSCToken) return i - 2;
                             break;
                         case OPERATOR:
                             if (ltt != BHTokenType.IDENT &&
                                 ltt != BHTokenType.IFQ &&
                                 ltt != BHTokenType.RB_BLOCK &&
                                 ltt != BHTokenType.SB_BLOCK &&
                                 ltt != BHTokenType.STRING &&
                                 ltt != null) return i - (wasSCToken ? 2 : 1);
                             break;
                         case IFQ:
                             ifQMode = true;
                             break;
                         case STRING:
                             if (ltt != BHTokenType.OPERATOR &&
                                 ltt != BHTokenType.IFQ &&
                                 ltt != BHTokenType.DOT &&
                                 ltt != null) return i - (wasSCToken ? 2 : 1);
                             break;
                         case COLON:
                             if (!ifQMode) return i - (wasSCToken ? 2 : 1);
                             if ((x = getJSExpression(i + 1)) != -1) {
                                 i = x;
                                 tt = tokens.get(i).getType();
                             }
                             ifQMode = false;
                             break;
                         case SEMICOLON:
                         case COMMA:
                         case L_BBRACE:
                         case R_BBRACE:
                             return i - (wasSCToken ? 2 : 1);
                     }
                     ltt = tt;
                     wasSCToken = false;
                 }
             }
         }
         return i - 1;
     }
 
     private boolean isJSONProperty(int i) {
         int l = tokens.size();
         for (i++; i < l; i++) {
             switch (tokens.get(i).getType()) {
                 case COLON:
                     return true;
                 case WHITESPACE:
                 case SL_COMMENT:
                 case ML_COMMENT:
                     break;
                 default:
                     return false;
             }
         }
         return false;
     }
 
     private static int findStringEnd(String s, char q, int start) {
         char c;
         for (int i = start, l = s.length(); i < l; i++) {
             c = s.charAt(i);
             if (c == q) return i;
             else if (c == '\\') i++;
         }
         return -1;
     }
 
     private static int findSLCommentEnd(String s, int start) {
         char c;
         int i = start;
         for (int l = s.length(); i < l; i++) {
             c = s.charAt(i);
             if (c == '\n' || c == '\r') return i - 1;
         }
         return i > start ? i - 1 : start;
     }
 
     private static int findMLCommentEnd(String s, int start) {
         char c;
         int i = start, j = i + 1;
         for (int l = s.length(); i < l; i++, j++) {
             c = s.charAt(i);
             if (c == '*' && j < l) {
                 if (s.charAt(j) == '/') return j;
             }
         }
         return -1;
     }
 
 }
