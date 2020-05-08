 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import java.io.*;
 import org.antlr.runtime.*;
 
 public class TreeWalker {	
 	public void walkTree(CommonTree t, String filename) {
 		try {
 		BufferedWriter out = new BufferedWriter(new FileWriter(filename + ".rb"));
 		for ( int i = 0; i < t.getChildCount(); i++ ) {
 		walk(((CommonTree)t.getChild(i)), out);
 		}
 		out.close();
 		}
 		catch (IOException e) {}
 	}
 	public void walk(CommonTree t, BufferedWriter out){
 		try{
 		if ( t != null ) {			
 			//	for ( int i = 0; i < t.getChildCount(); i++ ) {
 			//	every unary operator needs to be preceded by a open parenthesis and ended with a closed parenthesis
 				switch(t.getType()){
 					case TanGParser.ADDSUB:	
 						if(t.getChildCount() > 1){
 							walk((CommonTree)t.getChild(0), out);
 							out.write(t.getText());
 							walk((CommonTree)t.getChild(1), out);
 						}
 						else{
 							if(t.getText().equals("-")){
 								out.write("(");
 								out.write(t.getText());
 								walk((CommonTree)t.getChild(0), out);
 								out.write(")");
 							}
 							else{
 								walk((CommonTree)t.getChild(0), out);
 							}
 						}
 						break;
 					case TanGParser.AND:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.ASSERT:
 						break;					
 					case TanGParser.ASSN:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(" " + t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.BITAND:
 						walk((CommonTree)t.getChild(0), out);
 						out.write("&");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.BITNOT:
 						out.write("(");
 						out.write(t.getText());						
 						walk((CommonTree)t.getChild(0), out);
 						out.write(")");
 						break;
 					case TanGParser.BITOR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write("|");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.BITSHIFT:						
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;					
 					case TanGParser.BITXOR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write("^");
 						walk((CommonTree)t.getChild(1), out);
 						break;					
 					case TanGParser.BOOLAND:				
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.BOOLOR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;					
 					case TanGParser.BREAK:
 						break;
 					case TanGParser.BYTE:
 						break;
 					case TanGParser.COMMA:
 						out.write(t.getText());
 						break;
 					case TanGParser.COMMENT:
 						out.write(t.getText());
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
 						out.write(t.getText());
 						break;					
 					case TanGParser.EOF:
 						break;
 					case TanGParser.EQTEST:	
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.ESC_SEQ:
 						break;
 					case TanGParser.EXP:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.EXPONENT:
 						out.write("(");
 						walk((CommonTree)t.getChild(0), out);
 						out.write("* 10**");
 						walk((CommonTree)t.getChild(1), out);
 						out.write(")");
 						break;
 					case TanGParser.FATCOMMA:
 						break;
 					case TanGParser.FILENAME:
 						break;
 					case TanGParser.FLOAT:
 						out.write(t.getText());
 						break;
 					case TanGParser.FOR:
 						break;
 					case TanGParser.FORK:
 						break;
 					case TanGParser.FROM:
 						break;
 					case TanGParser.HEX:
 						out.write(t.getText());
 						break;
 					case TanGParser.HEX_DIGIT:
 						out.write(t.getText());
 						break;
 					case TanGParser.ID:
						out.write(t.getText());
 						break;
 					case TanGParser.IF:
 						break;
 					case TanGParser.IMPORT:
 						break;
 					case TanGParser.IN:
 						break;
 					case TanGParser.INT:
 						out.write(t.getText());
 						break;
 					case TanGParser.INTRANGE:
 						break;
 					case TanGParser.IS:
 						break;
 					case TanGParser.LBRACE:
 						out.write(t.getText());
 						break;
 					case TanGParser.LBRACK:
 						out.write(t.getText());
 						break;
 					case TanGParser.LOOP:
 						break;
 					case TanGParser.LPAREN:
 						out.write(t.getText());
 						break;
 					case TanGParser.MAGCOMP:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;					
 					case TanGParser.MOD:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.MULT:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.NEWLINE:
 						out.write(t.getText());
 						break;
 					case TanGParser.NODE:
 						out.write("module ");
 						walk((CommonTree)t.getChild(0), out);
 						out.newLine();
 						out.write("def main");
 						for ( int i = 1; i < t.getChildCount(); i++ ) {
 							walk((CommonTree)t.getChild(i), out);
 						}
 						break;
 					case TanGParser.NOT:
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(0), out);
 						break;					
 					case TanGParser.OR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.PIPE:
 						break;
 					case TanGParser.RANGE:
 						break;
 					case TanGParser.RBRACE:
 						out.write(t.getText());
 						break;
 					case TanGParser.RBRACK:
 						out.write(t.getText());
 						break;
 					case TanGParser.RETURN:
 						break;
 					case TanGParser.RPAREN:
 						out.write(t.getText());
 						break;
 					case TanGParser.STAR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.STRING:
 						out.write(t.getText());
 						break;
 					case TanGParser.TF:
 						out.write(t.getText());
 						break;
 					case TanGParser.UNLESS:
 						break;
 					case TanGParser.UNTIL:
 						break;
 					case TanGParser.WHILE:
 						break;
 					case TanGParser.WS:
 						out.write(t.getText());
 						break;
 					case TanGParser.XOR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;						
 				}				
 				//walktTree((CommonTree)t.getChild(i));
 		//	}
 		
 		
 	}	}
 		catch (IOException e) {}
 		
 }
 }
