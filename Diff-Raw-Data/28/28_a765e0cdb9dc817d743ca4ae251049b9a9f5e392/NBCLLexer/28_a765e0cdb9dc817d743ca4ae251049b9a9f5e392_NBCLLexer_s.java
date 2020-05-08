 /*
  * Created on Monday, August 22 2011 02:13
  */
 package com.mbien.opencl.editor;
import com.mbien.opencl.antlr.AntlrCharStream;
 import com.mbien.opencl.antlr.CLLexer;
import org.antlr.runtime.Token;
 import org.netbeans.spi.lexer.Lexer;
 import org.netbeans.spi.lexer.LexerRestartInfo;
 
 /**
  *
  * @author mbien
  */
 public class NBCLLexer implements Lexer<CLTokenID> {
 
     private final LexerRestartInfo<CLTokenID> info;
     private final CLLexer lexer;
 
     public NBCLLexer(LexerRestartInfo<CLTokenID> info) {
         this.info = info;
         this.lexer = new CLLexer(new AntlrCharStream(info.input(), "CLEditor"));
     }
 
     @Override
    public org.netbeans.api.lexer.Token<CLTokenID> nextToken() {
        Token token = lexer.nextToken();
        if (token.getType() != CLLexer.EOF) {
            CLTokenID tokenId = CLLanguageHierarchy.getToken(token.getType());
             return info.tokenFactory().createToken(tokenId);
         }
         return null;
     }
 
     @Override
     public Object state() {
         return null;
     }
 
     @Override
     public void release() {
     }
 }
