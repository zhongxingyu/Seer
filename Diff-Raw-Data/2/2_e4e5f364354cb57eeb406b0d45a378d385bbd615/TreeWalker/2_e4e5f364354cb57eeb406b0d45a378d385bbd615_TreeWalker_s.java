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
 		walk((CommonTree) t, out);
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
 			//	every unary operator needs to be preceded by a open parenthesis and ended with a closed parenthesis
 				switch(t.getType()){
 					case TanGParser.ADDSUB:	
 						if(t.getChildCount() > 1){
 							walk((CommonTree)t.getChild(0), out);
 							out.write(t.getText() + " ");
 							walk((CommonTree)t.getChild(1), out);
 						}
 						else{
 							if(t.getText().equals("- ")){
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
 						out.write(t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.ASSERT:
 						out.write(t.getText() + " ");
 						break;					
 					case TanGParser.ASSN:
 						walk((CommonTree)t.getChild(0), out);
 						out.write( t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.BITAND:
 						walk((CommonTree)t.getChild(0), out);
 						out.write("& ");
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
 						out.write("| ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.BITSHIFT:					
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;					
 					case TanGParser.BITXOR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write("^ ");
 						walk((CommonTree)t.getChild(1), out);
 						break;					
 					case TanGParser.BOOLAND:				
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText()  + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.BOOLOR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText()  + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;					
 					case TanGParser.BREAK:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.BYTE:
 						break;
 
 					case TanGParser.COMMA:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.COMMENT:
 						out.write(t.getText());
 						break;
 					case TanGParser.COND:
 						
 						for (int j = 1; j < t.getChildCount(); j=j+2 ) {
 							if(j ==  t.getChildCount()-3){
 								out.write("else ");
 								walk((CommonTree)t.getChild(j), out);
 							}
 							else if(j <  t.getChildCount()-3){
 								out.write("when ");
 								walk((CommonTree)t.getChild(j), out);
 							}else{
 								walk((CommonTree)t.getChild(j), out);
 							}
 						}
 						break;
 					case TanGParser.CONTINUE:
 						break;
 					case TanGParser.DO:
 						break;
 					case TanGParser.DOT:
 						out.write(t.getText());
 						break;
 					case TanGParser.ELSE:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.END:
 						out.write(t.getText());
 						break;					
 					case TanGParser.EOF:
 						break;
 					case TanGParser.EQTEST:	
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						for ( int i = 2; i < t.getChildCount()-1; i++ ) {
 							walk((CommonTree)t.getChild(i), out);
 						}
 						break;
 					case TanGParser.ESC_SEQ:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.EXP:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.EXPONENT:
 						out.write("(");
 						walk((CommonTree)t.getChild(0), out);
 						out.write("* 10 ** ");
 						walk((CommonTree)t.getChild(1), out);
 						out.write(")");
 						break;
 					case TanGParser.FATCOMMA:
 						out.write(t.getText());
 						break;
 					case TanGParser.FILENAME:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.FLOAT:
 						out.write(t.getText()  + " ");
 						break;
 					case TanGParser.FOR:
 						out.write(t.getText());
 						break;
 					case TanGParser.FORK:
 						break;
 					case TanGParser.FROM:
 						break;
 					case TanGParser.HEX:
 						out.write(t.getText()  + " ");
 						break;
 					case TanGParser.HEX_DIGIT:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.ID:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.IF:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.IMPORT:
 						out.write("require ");
 						break;
 					case TanGParser.IN:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.INT:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.INTRANGE:
 						out.write(t.getText());
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
 						out.write("while true ");
 						break;
 					case TanGParser.LPAREN:
 						out.write(t.getText());
 						break;
 					case TanGParser.MAGCOMP:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						for ( int i = 2; i < t.getChildCount()-1; i++ ) {
 							walk((CommonTree)t.getChild(i), out);
 						}
 						break;					
 					case TanGParser.MOD:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.MULT:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.NEWLINE:
 						out.write(t.getText());
 						break;
 					case TanGParser.NODE:
 						
 						if (t.getText().equals("public node")){
 							out.write("class ");
 							walk((CommonTree)t.getChild(0), out);
 						}
 						else{
 							out.write("class ");
 							walk((CommonTree)t.getChild(0), out);
 							out.newLine();
 							out.write("private");
 						}
 						out.newLine();
 						out.write("def main");
 						for ( int i = 1; i < t.getChildCount(); i++ ) {
 							walk((CommonTree)t.getChild(i), out);
 						}
 						out.write("end");
 						break;
 					case TanGParser.NOT:
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(0), out);
 						for ( int i = 1; i < t.getChildCount()-1; i++ ) {
 							walk((CommonTree)t.getChild(i), out);
 						}
 						break;	
 					case TanGParser.NONE:
 						out.write(t.getText()+ " ");
 						break;
 					case TanGParser.NULL:
 						out.write(t.getText()+ " ");
 						break;				
 					case TanGParser.OR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.PIPE:
 					//	for ( int i = t.getChildCount()-1; i > -1; i--) {
 						//	if (i==0){
 
 						//	walk((CommonTree)t.getChild(t.getChildCount()-1), out);
 						//	}
 						//						}
 
 						break;
 					case TanGParser.RANGE:
 						out.write(t.getText());
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
 					case TanGParser.SOME:
 						out.write(t.getText()+ " ");
						break:
 					case TanGParser.STAR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.STRING:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.TF:
 						out.write(t.getText());
 						break;
 					case TanGParser.UNLESS:
 						out.write("! while");
 						break;
 					case TanGParser.UNTIL:
 						out.write(t.getText());
 						break;
 					case TanGParser.WHILE:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.WS:
 						out.write(t.getText());
 						break;
 					case TanGParser.XOR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText()  + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;						
 				}						
 		
 	}	}
 		catch (IOException e) {}
 		
 }
 }
