 package sugar.token.codeParser;
 
 import sugar.*;
 
 import sugar.token.TEnv;
 
 import commons.konoha2.CTX;
 import commons.konoha2.kclass.KMethod;
 
 /**
  * This class is used to parse "SLASH" 
  * @author okachin
  *
  */
 
 public final class ParseSLASH implements FTokenizer {
 	
 	@Override public final int parse(CTX ctx,  KToken tk, TEnv tenv, int tok_start, KMethod thunk) {
 		String ts = tenv.source.substring(tok_start);
 		if(ts.charAt(1) == '/') {
 			return new ParseLINE().parse(ctx, tk, tenv, tok_start, thunk);
 		}
 		if(ts.charAt(1) == '*') {
 			return new ParseCOMMENT().parse(ctx, tk, tenv, tok_start, thunk);
 		}
 		return new ParseOP().parse(ctx, tk, tenv, tok_start, thunk);
 	}	
 }
