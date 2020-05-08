 package com.github.stkurilin.routes;
 
 import com.github.stkurilin.routes.internal.TokenType;
 import com.github.stkurilin.routes.internal._RulesLexer;
 
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Stanislav Kurilin
  */
 public final class RulesReader {
     private final RuleCreator ruleCreator;
     final Map<String, Class<?>> imported = new HashMap<String, Class<?>>();
 
     public RulesReader(RuleCreator ruleCreator) {
         this.ruleCreator = ruleCreator;
     }
 
     public Iterable<Rule> apply(Reader source) {
         Method method = null;  //start rule
         ArrayList<UriSpec.Item> items = new ArrayList<UriSpec.Item>();
         Class<? extends Object> clazz = null;
         String methodId = null;
         List<String> args = new ArrayList<String>();
         final ArrayList<Rule> result = new ArrayList<Rule>();
         final _RulesLexer lexer = new _RulesLexer(source);
         while (lexer.hasNext()) {
             final TokenType tokenType = lexer.next();
             final String text = lexer.yytext();
             System.out.println(String.format("%s:%s", tokenType, text));
             if (tokenType == null && method != null) {
                 build(method, items, clazz, methodId, args, result);
                 items = new ArrayList<UriSpec.Item>();
                 args = new ArrayList<String>();
                 method = null;
                 continue;
             }
             if (tokenType == null) continue;
             switch (tokenType) {
                 case IMPORT_KEYWORD:
                 case SLASH:
                 case WHITE_SPACE:
                 case ARGS_START:
                 case ARGS_END:
                 case MATCHER_START:
                 case MATCHER_END:
                 case ARG_SEPARATOR:
                     break;
                 case IMPORT_CLASS:
                     try {
                         imported.put(shortFormPart(text), Class.forName(text));
                         break;
                     } catch (ClassNotFoundException e) {
                         throw new RuntimeException(e);
                     }
                 case ARG:
                     args.add(text);
                     break;
                 case ACTION:
                     if (method != null) {
                         build(method, items, clazz, methodId, args, result);
                         items = new ArrayList<UriSpec.Item>();
                         args = new ArrayList<String>();
                         method = null;
                     }
                     args = new ArrayList<String>();
                     method = method(text);
                     break;
                 case LITERAL:
                     items.add(new UriSpec.Literal(text));
                     break;
                 case MATCHER:
                     items.add(new UriSpec.Matcher(text));
                     break;
                 case INSTANCE_ID:
                     clazz = imported.containsKey(text) ? imported.get(text) : findClass(text);
                     break;
                 case INSTANCE_METHOD_SEPARATOR:
                     break;
                 case METHOD_ID:
                     methodId = text;
                     break;
                 case BAD_CHARACTER:
                     throw new RuntimeException(String.format("Error while parsing >%s< on ", text));
                 default:
                     throw new AssertionError(tokenType);
             }
         }
         if (method != null)
             throw new RuntimeException("fuck");
         return result;
     }
 
     private void build(Method method, final ArrayList<UriSpec.Item> items, Class<? extends Object> clazz, String methodId, List<String> args, ArrayList<Rule> result) {
         result.add(ruleCreator.apply(method, new UriSpec() {
             @Override
             public Iterable<Item> path() {
                 return items;
             }
         }, clazz, methodId, args));
     }
 
     private String shortFormPart(String text) {
         return text.substring(text.lastIndexOf(".") + ".".length());
     }
 
     private Method method(String text) {
        return Method.valueOf(text);
     }
 
     private Class<Object> findClass(String text) {
         try {
             return (Class) Class.forName(text);
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     private String uppercaseFirstLatter(String text) {
         return text.substring(0, 1).toUpperCase() + text.substring(1);
     }
 }
