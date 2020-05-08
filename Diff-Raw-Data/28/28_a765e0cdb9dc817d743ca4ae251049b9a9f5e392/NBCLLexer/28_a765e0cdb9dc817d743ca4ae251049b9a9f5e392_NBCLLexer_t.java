 /*
  * Created on Monday, August 22 2011 02:13
  */
 package com.mbien.opencl.editor;

 import com.mbien.opencl.antlr.CLLexer;
import com.mbien.opencl.antlr.AntlrCharStream;
import org.netbeans.api.lexer.Token;
 import org.netbeans.spi.lexer.Lexer;
 import org.netbeans.spi.lexer.LexerRestartInfo;
 
import static com.mbien.opencl.antlr.CLLexer.*;
import static org.netbeans.api.lexer.PartType.*;

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
    public Token<CLTokenID> nextToken() {

        int tokenType = lexer.nextToken().getType();

        if (tokenType != EOF) {

            CLTokenID tokenId = CLLanguageHierarchy.getToken(tokenType);
             return info.tokenFactory().createToken(tokenId);

        } else if (info.input().readLength() > 0) { // we have an incomplete token -> lets assume its a comment
            
            CLTokenID tokenId = CLLanguageHierarchy.getToken(COMMENT);
            return info.tokenFactory().createToken(tokenId, info.input().readLength(), MIDDLE);
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
