 package sugar.tokenizer.parser;
 
 import sugar.*;
 import sugar.tokenizer.TEnv;
 
 import commons.konoha2.CTX;
 import commons.konoha2.kclass.KMethod;
 
 /**
  * This class is used to parse "LINE" 
  * @author okachin
  *
  */
 
 public final class ParseLINE implements FTokenizer {
 	
 	@Override public final int parse(CTX ctx,  KToken tk, TEnv tenv, int tok_start, KMethod thunk) {
 		int ch, pos = tok_start;
 //		while((ch = tenv.source.charAt(pos++)) != 0) {
 		while(true) {
 			pos++;
			if(pos >= tenv.source.length()) break;
 			if((ch = tenv.source.charAt(pos)) == 0) break;
 			if(ch == '\n') break;
 		}
 //		return pos - 1;/*EOF*/
 		return pos;/*EOF*/
 	}
 }
