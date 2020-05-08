 package sugar.tokenizer.parser;
 
 import sugar.*;
 
 import sugar.tokenizer.TEnv;
 
 import commons.konoha2.CTX;
 import commons.konoha2.kclass.KMethod;
 import commons.konoha2.kclass.KString;
 
 /**
  * This class is used to parse "OP1" 
  * @author okachin
  *
  */
 
 public final class ParseOP1 implements FTokenizer {
 
 	@Override public final int parse(CTX ctx,  KToken tk, TEnv tenv, int tok_start, KMethod thunk) {
 		if(tk != null /* CTX.IS_NOTNULL(tk) */) {
 			String s = tenv.source.substring(tok_start);
 			tk.text = new KString(s.substring(0, 1)); // KSETv(tk->text, new_kString(s, 1, SPOL_ASCII|SPOL_POOL));
 			tk.tt = KToken.TK_OPERATOR;
			tk.topch = s.charAt(0);
 		}
 		return tok_start + 1;
 	}
 }
