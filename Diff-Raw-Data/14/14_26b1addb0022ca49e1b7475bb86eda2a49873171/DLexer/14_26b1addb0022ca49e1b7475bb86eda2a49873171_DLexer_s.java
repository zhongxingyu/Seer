 package com.wyrdtech.d.intellid.lexer;
 
 import com.intellij.lexer.Lexer;
 import com.intellij.lexer.LexerPosition;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.util.text.CharSequenceReader;
import com.wyrdtech.parsed.lexer.LexerException;
import com.wyrdtech.parsed.lexer.LexerStream;
 import com.wyrdtech.parsed.lexer.token.BaseTokenFactory;
 import com.wyrdtech.parsed.lexer.token.Token;
 import com.wyrdtech.parsed.lexer.token.TokenFactory;
 import com.wyrdtech.parsed.lexer.token.TokenType;
 import org.jetbrains.annotations.Nullable;
 
 import java.io.IOException;
 
 /**
  * IntelliJ Lexer for D, wrapper for the ParseD lexer.
  */
 public class DLexer extends Lexer {
 
     private final TokenFactory factory;
 
     private CharSequence buffer;
 
     private int start_offset;
     private int end_offset;
     private int cur_offset;
 
     private com.wyrdtech.parsed.lexer.Lexer lexer; // actual lexer
     private Token token; // current token
 
     private int state; // current token type, as ordinal
 
 
     //TODO: inject token factory?
     public DLexer() {
         // TODO: generate custom tokens
         this.factory = new BaseTokenFactory();
     }
 
 
     @Override
    public void start(final CharSequence buffer, final int startOffset, final int endOffset, final int initialState) {
         this.buffer = buffer;
         this.start_offset = startOffset;
         this.end_offset = endOffset;
         this.cur_offset = start_offset;
 
         this.lexer = new com.wyrdtech.parsed.lexer.Lexer(factory, new CharSequenceReader(buffer.subSequence(startOffset, endOffset)));
 
         this.token = null;
 
         this.state = initialState;
 
         if (state <= 0) {
             // Not inside a known token, advance to get next token
             this.advance();
         }
         else {
             // Started in the middle of a token, set as current token
             TokenType t = TokenType.Unknown;
             for (TokenType type : TokenType.values()) {
                 if (type.ordinal() == state) {
                     t = type;
                     break;
                 }
             }
             this.token = factory.create(t, lexer.position(), 0, 0);
         }
 
     }
 
     @Override
     public int getState() {
         return state;
     }
 
     @Nullable
     @Override
     public IElementType getTokenType() {
         if (token == null) {
             return null;
         }
         return DTokenType.valueOf(token.getType());
     }
 
     @Override
     public int getTokenStart() {
         if (token == null) {
             return start_offset;
         }
         return start_offset + token.getStartIndex();
     }
 
     @Override
     public int getTokenEnd() {
         if (token == null) {
             return start_offset;
         }
         return start_offset + token.getEndIndex();
     }
 
     @Override
     public void advance() {
         try {
             token = lexer.next();
             cur_offset = start_offset + lexer.position();
             if (token != null) {
                 state = token.getType().ordinal();
             } else {
                 state = -1;
             }
         } catch (IOException e) {
             state = -1;
         }
     }
 
     @Override
     public LexerPosition getCurrentPosition() {
         return new Position(cur_offset, getState());
     }
 
     @Override
    public void restore(final LexerPosition position) {
         this.lexer = new com.wyrdtech.parsed.lexer.Lexer(factory, new CharSequenceReader(buffer.subSequence(position.getOffset(), end_offset)));
     }
 
     @Override
     public CharSequence getBufferSequence() {
         return this.buffer;
     }
 
     @Override
     public int getBufferEnd() {
         return this.end_offset;
     }
 
 
     // Immutable version, in case it matters
     private static class Position implements LexerPosition {
         private final int offset;
         private final int state;
 
         public Position(int offset, int state) {
             this.offset = offset;
             this.state = state;
         }
 
         @Override
         public int getOffset() {
             return offset;
         }
 
         @Override
         public int getState() {
             return state;
         }
     }
 }
