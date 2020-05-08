 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import java.io.*;
 import org.antlr.runtime.*;
 
 public class TreeWalker {	
 	public void printTree(CommonTree t) {
 		if ( t != null ) {			
 			for ( int i = 0; i < t.getChildCount(); i++ ) {
 				switch(t.getType()){
 				//TODO: Add Code
 					case TanGParser.ADDSUB:	
 						//print out ruby code to a file
 						break;
 					case TanGParser.AND:
 						break;
 					case TanGParser.ASSERT:
 						break;					
 					case TanGParser.ASSN:
 						break;
 					case TanGParser.BITAND:
 						break;
 					case TanGParser.BITNOT:
 						break;
 					case TanGParser.BITOR:
 						break;
 					case TanGParser.BITSHIFT:
 						break;					
 					case TanGParser.BITXOR:
 						break;					
 					case TanGParser.BOOLAND:
 						break;
 					case TanGParser.BOOLOR:
 						break;					
 					case TanGParser.BREAK:
 						break;
 					case TanGParser.BYTE:
 						break;
 					case TanGParser.COMMA:
 						break;
 					case TanGParser.COMMENT:
 						break;
 					case TanGParser.COND:
 						break;
 					case TanGParser.CONTINUE:
 						break;
 					case TanGParser.DO:
 						break;
 					case TanGParser.DOT:
 						break;
 					case TanGParser.ELSE:
 						break;
 					case TanGParser.END:
 						break;					
 					case TanGParser.EOF:
 						break;
 					case TanGParser.EQTEST:
 						break;
 					case TanGParser.ESC_SEQ:
 						break;
 					case TanGParser.EXP:
 						break;
 					case TanGParser.EXPONENT:
 						break;
 					case TanGParser.FATCOMMA:
 						break;
 					case TanGParser.FILENAME:
 						break;
 					case TanGParser.FLOAT:
 						break;
 					case TanGParser.FOR:
 						break;
 					case TanGParser.FORK:
 						break;
 					case TanGParser.FROM:
 						break;
 					case TanGParser.HEX:
 						break;
 					case TanGParser.HEX_DIGIT:
 						break;
 					case TanGParser.ID:
 						break;
 					case TanGParser.IF:
 						break;
 					case TanGParser.IMPORT:
 						break;
 					case TanGParser.IN:
 						break;
 					case TanGParser.INT:
 						break;
 					case TanGParser.INTRANGE:
 						break;
 					case TanGParser.IS:
 						break;
 					case TanGParser.LBRACE:
 						break;
 					case TanGParser.LBRACK:
 						break;
 					case TanGParser.LOOP:
 						break;
 					case TanGParser.LPAREN:
 						break;
 					case TanGParser.MAGCOMP:
 						break;					
 					case TanGParser.MOD:
 						break;
 					case TanGParser.MULT:
 						break;
 					case TanGParser.NEWLINE:
 						break;
 					case TanGParser.NODE:
 						break;
 					case TanGParser.NOT:
 						break;					
 					case TanGParser.OR:
 						break;
 					case TanGParser.PIPE:
 						break;
 					case TanGParser.RANGE:
 						break;
 					case TanGParser.RBRACE:
 						break;
 					case TanGParser.RBRACK:
 						break;
 					case TanGParser.RETURN:
 						break;
 					case TanGParser.RPAREN:
 						break;
 					case TanGParser.STAR:
 						break;
 					case TanGParser.STRING:
 						break;
 					case TanGParser.TF:
 						break;
 					case TanGParser.UNLESS:
 						break;
 					case TanGParser.UNTIL:
 						break;
 					case TanGParser.WHILE:
 						break;
 					case TanGParser.WS:
 						break;
 					case TanGParser.XOR:
 						break;						
 				}				
 				printTree((CommonTree)t.getChild(i));
 			}
 		}
 	}
 	
 	
 }
 
