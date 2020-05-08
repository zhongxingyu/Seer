 package com.wickedspiral.jacss.lexer;
 
 import com.wickedspiral.jacss.lexer.builder.CommentTokenBuilder;
 import com.wickedspiral.jacss.lexer.builder.IdentifierTokenBuilder;
 import com.wickedspiral.jacss.lexer.builder.NumberTokenBuilder;
 import com.wickedspiral.jacss.lexer.builder.OpTokenBuilder;
 import com.wickedspiral.jacss.lexer.builder.StringTokenBuilder;
 import com.wickedspiral.jacss.lexer.builder.WhiteSpaceTokenBuilder;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author wasche
  * @since 2011.08.04
  */
 public class Lexer implements ParserState
 {
     private static final char[] OPS_CHARACTERS = "{}[]()+*=.,;:>~|\\%$#@^!".toCharArray();
     private static final char[] IDENTIFIER_CHARS = "_-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
     private static final char[] NUMBER_CHARS = "-.0123456789".toCharArray();
 
     private static WhiteSpaceTokenBuilder whiteSpaceTokenBuilder = new WhiteSpaceTokenBuilder();
     private static OpTokenBuilder opTokenBuilder = new OpTokenBuilder();
     private static IdentifierTokenBuilder identifierTokenBuilder = new IdentifierTokenBuilder();
     private static NumberTokenBuilder numberTokenBuilder = new NumberTokenBuilder();
     private static StringTokenBuilder stringTokenBuilder = new StringTokenBuilder();
     private static CommentTokenBuilder commentTokenBuilder = new CommentTokenBuilder();
 
     private static final TokenBuilder[] TOKEN_MAP = new TokenBuilder[255];
 
     static {
         for (char t : OPS_CHARACTERS)
         {
             TOKEN_MAP[t] = opTokenBuilder;
         }
         for (char t : IDENTIFIER_CHARS)
         {
             TOKEN_MAP[t] = identifierTokenBuilder;
         }
         for (char t : NUMBER_CHARS)
         {
             TOKEN_MAP[t] = numberTokenBuilder;
         }
         TOKEN_MAP['\''] = stringTokenBuilder;
         TOKEN_MAP['"'] = stringTokenBuilder;
         TOKEN_MAP['/'] = commentTokenBuilder;
         TOKEN_MAP[' '] = whiteSpaceTokenBuilder;
         TOKEN_MAP['\t'] = whiteSpaceTokenBuilder;
         TOKEN_MAP['\n'] = whiteSpaceTokenBuilder;
 
     }
 
     private static TokenBuilder getBuilderForToken(Token token)
     {
         switch (token)
         {
             case OP:
                 return opTokenBuilder;
             case IDENTIFIER:
                 return identifierTokenBuilder;
             case NUMBER:
                 return numberTokenBuilder;
             case STRING:
                 return stringTokenBuilder;
             case COMMENT:
                 return commentTokenBuilder;
             case WHITESPACE:
                 return whiteSpaceTokenBuilder;
         }
         return null;
     }
 
     private static final char EOF = (char) 65535;
 
     private List<TokenListener> tokenListeners;
 
     private CharBuffer token;
     private char lastChar;
     private String lastToken;
     private TokenBuilder builder;
     private long offset = 0;
 
     public Lexer()
     {
         tokenListeners = new LinkedList<>();
 
         token = new CharBuffer();
     }
 
     public void addTokenListener(TokenListener listener)
     {
         tokenListeners.add(listener);
     }
 
     public void parse(InputStream in) throws IOException, UnrecognizedCharacterException
     {
         BufferedInputStream bis = new BufferedInputStream(in);
         InputStreamReader reader = new InputStreamReader(bis);
         char c;
         while ((c = (char) reader.read()) != EOF)
         {
             tokenize(c);
             offset++;
         }
 
         // finish whatever is in the buffer
         if (builder != null)
         {
             builder.handle(this, '\0');
         }
         else if (token.length() > 0)
         {
             tokenFinished(Token.OP);
         }
 
         for (TokenListener listener : tokenListeners)
         {
             listener.end();
         }
     }
 
     public ParserState tokenize(char c) throws UnrecognizedCharacterException
     {
         if ('\0' == c) return this;
        if (builder == null && c < TOKEN_MAP.length)
         {
             builder = TOKEN_MAP[c];
        }
        if (builder == null)
        {
            throw new UnrecognizedCharacterException("Unrecognized character at offset " + offset + ": " + c + " (" + ((int)c) +")");
         }
         builder.handle(this, c);
         return this;
     }
 
     public ParserState tokenFinished(Token t)
     {
         String s = token.toString();
         for (TokenListener listener : tokenListeners)
         {
             try
             {
                 listener.token(t, s);
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }
         }
         token.clear();
         lastToken = s;
         return this;
     }
 
     public ParserState push(char c)
     {
         token.append(c);
         lastChar = c;
         return this;
     }
 
     public ParserState unsetTokenBuilder()
     {
         builder = null;
         return this;
     }
 
     public ParserState setTokenBuilder(Token token)
     {
         builder = getBuilderForToken(token);
         return this;
     }
 
     public int getTokenLength()
     {
         return token.length();
     }
 
     public char getLastCharacter()
     {
         return lastChar;
     }
 
     public String getLastToken()
     {
         return lastToken;
     }
 
     public int get(int index)
     {
         return token.charAt(index);
     }
 
     @Override
     public ParserState clearToken()
     {
         token.clear();
         return this;
     }
 }
