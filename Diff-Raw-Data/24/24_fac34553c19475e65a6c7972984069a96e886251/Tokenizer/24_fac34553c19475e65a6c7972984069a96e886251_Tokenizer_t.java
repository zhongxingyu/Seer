 package weigl.grammar.lltck.rt;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.MethodDescriptor;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 
 import weigl.std.NoMoreElementsException;
 import weigl.std.WIterator;
 
 /**
  * Build an {@link WIterator} for tokens over the given {@link String}
  * 
  * @author Alexander Weigl <alexweigl@gmail.com>
  * @param <E>
  *            the token type class
  */
 public class Tokenizer<E extends TokenDefinition<E>> implements WIterator<Token<E>>
 {
     private List<TokenDefinition<E>> tokens;
     private String                   input;
 
     private MatchType                matchType = MatchType.FIRST;
     private TokenTypeCaller          callback;
     private int                      inputPosition;
 
     /**
      * @param input
      *            the input string
      * @param tokens
      *            {@link TokenDefinition} to searching for
      */
     public Tokenizer(String input, TokenDefinition<E>... tokens)
     {
         this.tokens = Arrays.asList(tokens);
         this.input = input;
     }
 
     public Tokenizer(String input, Object listener, TokenDefinition<E>... tokens)
     {
         this.callback = new TokenTypeCaller(listener);
         this.tokens = Arrays.asList(tokens);
         this.input = input;
     }
 
     /**
      * @see #Tokenizer(String, TokenDefinition...)
      * @param input
      * @param mt
      * @param tokens
      */
     public Tokenizer(String input, MatchType mt, TokenDefinition<E>... tokens)
     {
         this(input, tokens);
         matchType = mt;
     }
 
     /**
      * returns the next found token
      */
     @Override
     public Token<E> next() throws NoMoreElementsException, RecognitionException
     {
         if (input.isEmpty())
             throw new NoMoreElementsException();
         Token<E> tok = findToken();
         callback.onToken(tok);
         return tok;
     }
 
     private Token<E> findToken() throws RecognitionException, NoMoreElementsException
     {
         Token<E> tok = matchType == MatchType.FIRST ? firstMatch() : bestMatch();
 
         if (tok.getType().isHidden())
             return findToken();
         return tok;
     }
 
     public Token<E> bestMatch()
     {
         int best = 0;
         Token<E> matched = null;
         for (TokenDefinition<E> token : tokens)
         {
             if (token.isRule())
                 continue;
             Matcher m = token.getPattern().matcher(input);
             if (m.lookingAt() && m.end() > best)
             {
                 best = m.end();
                 String matchedText = input.substring(0, m.end());
                 matched = new Token<E>(token.getType(), matchedText, inputPosition - m.end());
             }
         }
         inputPosition += matched.getValue().toString().length();
         input = input.substring(best);
         return matched;
     }
 
     public Token<E> firstMatch() throws RecognitionException
     {
         for (TokenDefinition<E> token : tokens)
         {
             if (token.isRule())
                 continue;
             Matcher m = token.getPattern().matcher(input);
             if (m.lookingAt())
             {
                 String matched = input.substring(0, m.end());
                 input = input.substring(m.end());
                 inputPosition += m.end();
                 return new Token<E>(token.getType(), matched, inputPosition - m.end());
             }
         }
         throw new RecognitionException("no token definition matched for rest string: '" + input
                        + ", position:" + inputPosition + "'");
     }
 
     public class TokenTypeCaller
     {
         private Object              obj;
         private Map<String, Method> map = new HashMap<String, Method>();
 
         public TokenTypeCaller(Object obj)
         {
             BeanInfo beanInfo;
             if (obj != null)
             {
                 try
                 {
                     this.obj = obj;
                     beanInfo = Introspector.getBeanInfo(obj.getClass());
 
                     for (MethodDescriptor md : beanInfo.getMethodDescriptors())
                         map.put(md.getName(), md.getMethod());
                 } catch (IntrospectionException e)
                 {
                     obj = null;
                     e.printStackTrace();
                 }
             }
         }
 
         public void onToken(Token<E> token)
         {
             if (obj != null)
             {
                 Method m = map.get(token.getType().toString());
                 if (m != null)
                 {
                     try
                     {
                         Object robj = m.invoke(obj, token);
                         token.setValue(robj);
                     } catch (Exception e)
                     {
                         e.printStackTrace();
                     }
                 }
                 else
                 {
                     System.err.println("no suitable method for  " + token.getType().toString()
                                     + " was found!");
                 }
             }
         }
     }
     // public static void main(String[] args) throws RecognitionException {
     // Tokenizer<MathTokens> t = new Tokenizer<MathTokens>("2*2", MathTokens
     // .values());
     //
     // try {
     // while (true) {
     // Token<MathTokens> f1 = t.next();
     // Token<MathTokens> o = t.next();
     // Token<MathTokens> f2 = t.next();
     //
     // int i = Integer.parseInt(f1.getValue()), j = Integer
     // .parseInt(f2.getValue());
     //
     // switch (o.getType()) {
     // case DIV:
     // System.out.println(i / j);
     // break;
     // case MULTIPLY:
     // System.out.println(i * j);
     // break;
     // case MINUS:
     // System.out.println(i - j);
     // break;
     // case PLUS:
     // System.out.println(i + j);
     // break;
     // default:
     // System.err.println("ERROR: " + o.getType());
     // }
     // }
     // } catch (NumberFormatException e) {
     // e.printStackTrace();
     // } catch (NoMoreElementsException e) {
     // }
     // }
 }
