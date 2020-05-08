 package sugar.token.codeParser;
 
 import sugar.KToken;
 import sugar.token.*;
 
 import commons.konoha2.CTX;
 import commons.konoha2.kclass.KMethod;
 import commons.konoha2.kclass.KString;
 
 /**
  * This class is used to parse "BLOCK" 
  * @author okachin
  *
  */
 
public final class ParseBLOCK implements FTokenizer {
 	
 	@Override public final int parse(CTX ctx,  KToken tk, TEnv tenv, int tok_start, KMethod thunk) {
 		int ch, level = 1, pos = tok_start + 1;
 		FTokenizer[] fmat = tenv.fmat;
 		tk.lpos += 1;
 		while((ch = Tokenizer.kchar(tenv.source, pos)) != 0) {
 			if(ch == Tokenizer._RBR/*}*/) {
 				level--;
 				if(level == 0) {
 					if(tk != null /* CTX.IS_NOTNULL(tk) */) {
						tk.text = new KString(tenv.source.substring(tok_start + 1, pos - 2)); // TODO KSETv(tk->text, new_kString(tenv->source + tok_start + 1, (pos-2)-(tok_start)+1), 0));
						System.out.println(tk.text.text);
 						tk.tt = KToken.TK_CODE;
 					}
 					return pos + 1;
 				}
 				pos++;
 			}
 			else if(ch == Tokenizer._LBR/*'{'*/) {
 				level++; pos++;
 			}
 			else {
 				pos = fmat[ch].parse(ctx, /* TODO K_NULLTOKEN*/null, tenv, pos, null);
 			}
 		}
 		if(tk != null /* CTX.IS_NOTNULL(tk) */) {
 			// TODO
 			// size_t errref = SUGAR_P(ERR_, tk->uline, tk->lpos, "must close with }");
 			// Token_toERR(_ctx, tk, errref);
 		}
 		return pos - 1;
 	}	
 }
