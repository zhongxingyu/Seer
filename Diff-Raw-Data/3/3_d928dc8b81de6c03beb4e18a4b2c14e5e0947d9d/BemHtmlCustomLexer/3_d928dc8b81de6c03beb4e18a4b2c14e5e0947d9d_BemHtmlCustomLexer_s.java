 package bem.idea.bemhtml.lang.lexer;
 
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
 
         int x, bs, ts, i, l;
         char c;
         BHToken toAdd, last = null;
 
         boolean bDelayL, bDelayR;
         BHBrace lub;
 
         boolean skip;
 
         if (src != null) {
             for (i = 0, l = src.length(); i < l; i++) {
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
                         case '+': case '-': case '/': case '*': case '%':
                         case '=': case '!': case '<': case '>': case '&':
                         case '|':
                             if (last != null && last.getType() == BHTokenType.OPERATOR) {
                                 last.increment();
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
 
         invalidateBemValueSet = new HashSet<BHTokenType>();
         invalidateBemValueSet.add(BHTokenType.COLON);
         invalidateBemValueSet.add(BHTokenType.COMMA);
         invalidateBemValueSet.add(BHTokenType.NEWLINE);
         invalidateBemValueSet.add(BHTokenType.L_BBRACE);
     }
 
     private List<BHToken> retokenize() {
         List<BHToken> _tokens = new ArrayList<BHToken>();
         BHToken t, nt = null, t0, t1;
         BHTokenType tt, ntt = null, bvt;
         String v;
         int i, j, l, x;
         boolean wantJSExpression = false;
         for (i = 0, j = 1, l = tokens.size(); i < l; i++, j = i + 1) {
             t = tokens.get(i);
             tt = t.getType();
             if (j < l) {
                 nt = tokens.get(j); ntt = nt.getType();
             }
 
             v = src.substring(t.getStart(), t.getEnd() + 1);
 
             if (wantJSExpression && tt != BHTokenType.WHITESPACE && tt != BHTokenType.L_BBRACE) {
                 if ((x = addJSExpression(i, _tokens)) != -1) i = x;
                 else _tokens.add(t);
                 wantJSExpression = false;
             } else if (tt == BHTokenType.L_BBRACE) {
                 if (wantJSExpression) {
                     if (isJSONBlock(j)) {
                         _tokens.add(t);
                     } else {
                         if (bBracesIdx.containsKey(i)) {
                             x = bBracesIdx.get(i);
                             t1 = tokens.get(x);
                             _tokens.add(new BHToken(BHTokenType.JAVASCRIPT, t.getStart(), t1.getEnd()));
                             i = x;
                         } else {
                             _tokens.add(new BHToken(BHTokenType.JAVASCRIPT, t.getStart(), l - 1));
                             i = l;
                         }
                     }
                     wantJSExpression = false;
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
                 wantJSExpression = true;
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
         BHToken t, st;
         BHTokenType tt;
         List<BHToken> sub;
         boolean valid;
         int x;
         for (int i = 0, l = tokens.size(); i < l; i++) {
             valid = true;
             t = tokens.get(i);
             tt = t.getType();
             if (tt == BHTokenType.OPERATOR ||
                     tt == BHTokenType.SEMICOLON ||
                     tt == BHTokenType.DOT ||
                     tt == BHTokenType.STRING ||
                     tt == BHTokenType.IFQ) {
                 t.setType(BHTokenType.ERROR);
             } else if (tt == BHTokenType.BH_BLOCK || tt == BHTokenType.BH_ELEM) {
                 if (i + 2 < l) {
                     sub = tokens.subList(i + 1, i + 3);
 
                     if ((st = sub.get(0)).getType() != BHTokenType.WHITESPACE) { st.invalidate(BHTokenType.ERROR_WHITESPACE_EXPECTED); valid = false; }
                     if ((st = sub.get(1)).getType() != BHTokenType.BEM_VALUE &&
                             st.getType() != BHTokenType.JS_EXPRESSION) { st.invalidate(BHTokenType.ERROR_ONE_BEM_VALUE_EXPECTED); valid = false; }
 
                     if (valid) validateTill(i + 3, invalidateBemValueSet, BHTokenType.ERROR_TOO_MANY_VALUES);
                 }
             } else if (tt == BHTokenType.BH_MOD || tt == BHTokenType.BH_ELEMMOD) {
                 if (i + 4 < l) {
                     sub = tokens.subList(i + 1, i + 5);
 
                     if ((st = sub.get(0)).getType() != BHTokenType.WHITESPACE) { st.invalidate(BHTokenType.ERROR_WHITESPACE_EXPECTED); valid = false; }
                     if ((st = sub.get(1)).getType() != BHTokenType.BEM_VALUE &&
                             st.getType() != BHTokenType.JS_EXPRESSION) { st.invalidate(BHTokenType.ERROR_TWO_BEM_VALUES_EXPECTED); valid = false; }
                     if ((st = sub.get(2)).getType() != BHTokenType.WHITESPACE) { st.invalidate(BHTokenType.ERROR_WHITESPACE_EXPECTED); valid = false; }
                     if ((st = sub.get(3)).getType() != BHTokenType.BEM_VALUE &&
                             st.getType() != BHTokenType.JS_EXPRESSION) { st.invalidate(BHTokenType.ERROR_TWO_BEM_VALUES_EXPECTED); valid = false; }
 
                     if (valid) validateTill(i + 5, invalidateBemValueSet, BHTokenType.ERROR_TOO_MANY_VALUES);
                 }
             } else if (tt == BHTokenType.COLON) {
                 if (i + 1 < l) {
                     if ((x = isValidJSONValue(i + 1)) != -1) {
                         tokens.get(x).invalidate(BHTokenType.ERROR);
                     }
                 }
             }
         }
     }
 
     private void validateTill(int i, Set<BHTokenType> tillSet, BHTokenType errorType) {
         BHToken t;
         BHTokenType tt;
         for (int l = tokens.size(); i < l; i++) {
             t = tokens.get(i);
             tt = t.getType();
             if (tt != BHTokenType.WHITESPACE) {
                 if (!tillSet.contains(tt)) t.invalidate(errorType);
                 else return;
             }
         }
     }
 
     private int isValidJSONValue(int i) {
         BHTokenType tt;
         for (int l = tokens.size(); i < l; i++) {
             tt = tokens.get(i).getType();
             if (tt != BHTokenType.WHITESPACE && tt != BHTokenType.NEWLINE) {
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
         int l = tokens.size(), x;
         if (i < l) {
             BHToken t;
             BHTokenType tt, ltt = null;
             boolean wasWhite = false, ifQMode = false;
             for (; i < l; i++) {
                 t = tokens.get(i); tt = t.getType();
                 if (tt == BHTokenType.WHITESPACE) {
                     if (ltt == BHTokenType.DOT) return i - 1;
                     wasWhite = true;
                 } else {
                     switch (tt) {
                         case IDENT:
                             if (ltt != BHTokenType.OPERATOR &&
                                 ltt != BHTokenType.IFQ &&
                                 ltt != BHTokenType.DOT &&
                                 ltt != null) return i - (wasWhite ? 2 : 1);
                             break;
                         case DOT:
                             if (wasWhite) return i - 2;
                             break;
                         case OPERATOR:
                             if (ltt != BHTokenType.IDENT &&
                                 ltt != BHTokenType.IFQ &&
                                 ltt != BHTokenType.RB_BLOCK &&
                                 ltt != BHTokenType.SB_BLOCK &&
                                 ltt != BHTokenType.STRING &&
                                 ltt != null) return i - (wasWhite ? 2 : 1);
                             break;
                         case IFQ:
                             ifQMode = true;
                             break;
                         case STRING:
                             if (ltt != BHTokenType.OPERATOR &&
                                 ltt != BHTokenType.IFQ &&
                                 ltt != BHTokenType.DOT &&
                                 ltt != null) return i - (wasWhite ? 2 : 1);
                             break;
                         case COLON:
                             if (!ifQMode) return i - (wasWhite ? 2 : 1);
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
                             return i - (wasWhite ? 2 : 1);
                     }
                     ltt = tt;
                     wasWhite = false;
                 }
             }
         }
         return i - 1;
     }
 
     private boolean isJSONProperty(int i) {
         BHToken t;
         BHTokenType tt;
         int l = tokens.size();
         i++;
         if (i < l - 1) {
             t = tokens.get(i); tt = t.getType();
             if (tt == BHTokenType.WHITESPACE) {
                 i++;
                 if (i < l - 1) {
                     t = tokens.get(i); tt = t.getType();
                     return (tt == BHTokenType.COLON);
                 }
             } else return (tt == BHTokenType.COLON);
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
 
     private enum BHTokenType {
         L_BBRACE,
         R_BBRACE,
         RB_BLOCK,
         SB_BLOCK,
 
         IDENT,
         STRING,
         COLON,
         SEMICOLON,
         DOT,
         COMMA,
         IFQ,
         WHITESPACE,
         NEWLINE,
         OPERATOR,
         JAVASCRIPT,
         ERROR,
 
         ERROR_TOO_MANY_VALUES,
         ERROR_ONE_BEM_VALUE_EXPECTED,
         ERROR_TWO_BEM_VALUES_EXPECTED,
         ERROR_WHITESPACE_EXPECTED,
 
         BH_JSONPROP,
         BEM_VALUE,
         JS_EXPRESSION,
 
         BH_BLOCK,
         BH_ELEM,
         BH_MOD,
         BH_ELEMMOD,
 
         BH_DEFAULT,
         BH_TAG,
         BH_ATTRS,
         BH_CLS,
         BH_BEM,
         BH_JS,
         BH_JSATTR,
         BH_MIX,
         BH_CONTENT
     }
 
     private class BHToken {
         private BHTokenType type;
         private int start;
         private int end;
 
         BHToken(BHTokenType type, int start, int end) {
             this.type = type;
             this.start = start;
             this.end = end;
         }
 
         public BHTokenType getType() {
             return type;
         }
 
         public void setType(BHTokenType type) {
             this.type = type;
         }
 
         public int getStart() {
             return start;
         }
 
         public int getEnd() {
             return end;
         }
 
         public void increment() {
             end++;
         }
 
         public void invalidate(BHTokenType errorType) {
             setType(errorType);
         }
 
         public String toString() {
             return type + "[" + start + ":" + end + "]";
         }
     }
 
     private class BHBrace {
         private int l;
         private int r = -1;
 
         BHBrace(int l) {
             this.l = l;
         }
 
         public void setR(int r) {
             this.r = r;
         }
 
         public int getL() {
             return l;
         }
 
         public int getR() {
             return r;
         }
 
         public String toString() {
             return l + "/" + r;
         }
     }
 
 }
