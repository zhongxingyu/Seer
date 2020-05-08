 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import java.io.*;
 import org.antlr.runtime.*;
 import java.util.*;
 
 public class TreeWalker {	
 
 	public void walkTree(CommonTree t, String filename) 
 	{
 		try {
 		BufferedWriter out = new BufferedWriter(new FileWriter(filename + ".rb"));
 		out.write("require \"set\"\n");
 
 		if(!(t.getType() == 0)){
 			walk((CommonTree) t, out);
 		}
 		//traverse all the child nodes of the root if root was empty
 		else{
 			for ( int i = 0; i < t.getChildCount(); i++ ) 
 			{
 				walk(((CommonTree)t.getChild(i)), out);
 			}
 		}
 		out.close();
 		
 		}
 		catch (IOException e) {}
 	}
 	public void walk(CommonTree t, BufferedWriter out)
 	{
 		try{
 		if ( t != null ) {			
 			//	every unary operator needs to be preceded by a open parenthesis and ended with a closed parenthesis
 				switch(t.getType())
 				{
 					case TanGParser.ADDSUB:	
 						//if the operation is binary, read the two children and output that to the ruby code
 						if(t.getChildCount() > 1)
 						{
 							walk((CommonTree)t.getChild(0), out);
 							out.write(t.getText() + " ");
 							walk((CommonTree)t.getChild(1), out);
 						}
 						//if the operation is a unary minus, surround the right-hand side with parentheses
 						//this is to differenciate between unary operators and operations done within assignment operator
 						else{
 							if(t.getText().equals("- "))
 							{
 								out.write("(");
 								out.write(t.getText());
 								walk((CommonTree)t.getChild(0), out);
 								out.write(")");
 							}
 							else
 							{
 								walk((CommonTree)t.getChild(0), out);
 							}
 						}
 						break;
 						//binary operations like this simply prints out the 1st child, the operation and the 2nd child
 					case TanGParser.AND:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 						//expressions like these do not require translation and can simply to outputed to the ruby file
 					case TanGParser.ASSERT:
 						out.write(t.getText() + " ");
 						break;					
 					case TanGParser.ASSN:
 						walk((CommonTree)t.getChild(0), out);
 						out.write( t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 						//this operator and a few of the following operators are different in ruby so a translation was necessary
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
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.COMMA:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.COMMENT:
 						out.write(t.getText());
 						break;
 					case TanGParser.COND:
 						//we start at the second child node and skip every other one to skip the newlines
 						out.write("case ");
 						out.newLine();
 						for (int j = 1; j < t.getChildCount(); j=j+2 ) 
 						{	
 							//for all the conditions, except the last, begin it with the keyword "when"
 							//begin the last condition with else
 							if(j <  t.getChildCount()-3)
 							{
 								out.write("when ");
 								walk((CommonTree)t.getChild(j), out);
 								int k=0;
 								while (!(((t.getChild(j).getChild(k)).getType())== (TanGParser.NEWLINE))){
 									k++;
 								}
 								while (k < t.getChild(j).getChildCount()-1){
 									walk((CommonTree)(t.getChild(j).getChild(k)), out);
 									k++;
 								}
 							}
 							else if(j ==  t.getChildCount()-3)
 							{
 								out.write("else ");
 								walk((CommonTree)t.getChild(j), out);
 								int k=0;
 								while (!(((t.getChild(j).getChild(k)).getType())==(TanGParser.NEWLINE))){
 									k++;
 								}
 								while (k < t.getChild(j).getChildCount()-1){
 									walk((CommonTree)(t.getChild(j).getChild(k)), out);
 									k++;
 								}
 							}else
 							{
 								walk((CommonTree)t.getChild(j), out);
 							}
 						}
 						break;
 					case TanGParser.CONTINUE:
 						out.write("next ");
 						break;
 					case TanGParser.DO:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.DOT:
 						out.write(t.getText());
 						break;
 					case TanGParser.ELSE:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.END:
 						out.write(t.getText() + " ");
 						out.newLine();
 						break;					
 					case TanGParser.EOF:
 						out.write(t.getText());
 						break;
 					case TanGParser.EQTEST:	
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
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
 						//the power 10 operator in Tandem is simply e. It needs to be transformed to ruby code.
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
 						out.write("td_"+t.getText() + " ");
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
 						LinkedList<CommonTree> list = new LinkedList<CommonTree>();
 						out.newLine();
 						//every node will be converted to a class with the name of the node as the class name
 						if (t.getText().equals("public node"))
 						{
 							out.write("class ");
 							out.write(t.getChild(0).getText());
 						//	walk((CommonTree)t.getChild(0), out);
 						}
 						//if the class is private, add private after writing the constructor of the class
 						else
 						{
 							out.write("class ");
 							//walk((CommonTree)t.getChild(0), out);
 							out.write(t.getChild(0).getText());
 							out.newLine();
 							out.write("private");
 						}
 						out.newLine();
 						//then each class will have a main method with the node definition code
 						out.write("def main");
 						for ( int i = 1; i < t.getChildCount(); i++ ) 
 						{
 							if (t.getChild(i).getType()==TanGParser.NODE){
 								list.addLast(((CommonTree)t.getChild(i)));
 							}
 							else{
 								walk((CommonTree)t.getChild(i), out);
 							}
 						}
 						while(list.isEmpty()==false){
 							walk((CommonTree)list.getFirst(), out);
 							list.remove();
 						}
 						out.newLine();
 						out.write("end ");
 						out.newLine();
 						
 						break;
 					case TanGParser.NODEID:
 						//transform Println to ruby's print
 						 if(t.getText().equals("Println")){
 							out.write("puts");
 						}
 						//transform Print to ruby's print
 						else if(t.getText().equals("Print")){
 							out.write("print");
 						}
 						//if not, just print the id
 						else{
 						//	out.write(t.getText() + ".main(");
							out.write(t.getText());
 						}
 						break;
 					case TanGParser.NOT:
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(0), out);
 					
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
 						String params = "";
 						String first = "";
 						LinkedList<CommonTree> list2 = new LinkedList<CommonTree>();
 						for ( int i = 0; i < t.getChildCount(); i++ ) 
 						{
 							//if child is a node, but not the last node, push it
 							if ((t.getChild(i).getType() == TanGParser.NODEID && i != t.getChildCount()-1))
 							{
 								list2.push((CommonTree)t.getChild(i));
 
 							}
 							//if next token is a pipe, push it
 							else if(t.getChild(i).getType() == TanGParser.PIPE)
 							{
 								list2.push((CommonTree)t.getChild(i));
 
 							}
 							//if next token is an id, it is a parameter so it is not pushed
 							//when we walk the node that has the parameters (the first node), we will print them
 							else if(t.getChild(i).getType() == TanGParser.ID)
 							{
 								first = list2.peek().getText();
 								params = params +  t.getChild(i) + ",";
 
 							}
 							else
 							{
 								//walk the tree if the child is the last node in the chain
 								walk((CommonTree)t.getChild(i), out);
 								while(list2.isEmpty()==false){
 									out.write("(");
 									if((list2.peek().getText()).equals(first))
 									{
 										walk((CommonTree)list2.pop(), out);
 										out.write("(");
 										out.write(params.substring(0, params.length()-1));
 										out.write(")");
 									}else
 									{
 										walk((CommonTree)list2.pop(), out);
 									}
 									out.write(")");
 								}
 							}
 						}
 						break;						
 					case TanGParser.PUBPRIV:
 						break;
 					case TanGParser.RANGE:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.RBRACE:
 						out.write(t.getText());
 						break;
 					case TanGParser.RBRACK:
 						out.write(t.getText());
 						break;
 					case TanGParser.RETURN:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.RPAREN:
 						out.write(t.getText());
 						break;
 					case TanGParser.SOME:
 						out.write(t.getText()+ " ");
 						break;
 					case TanGParser.STAR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText() + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;
 					case TanGParser.STRING:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.TF:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.UNLESS:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.UNTIL:
 						out.write(t.getText() + " ");
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
