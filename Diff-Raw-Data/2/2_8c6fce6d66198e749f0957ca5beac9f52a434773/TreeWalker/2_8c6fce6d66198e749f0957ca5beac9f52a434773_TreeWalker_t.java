 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import java.io.*;
 import org.antlr.runtime.*;
 import java.util.*;
 
 public class TreeWalker {	
 		LinkedList<CommonTree> printedAlready = new LinkedList<CommonTree>();
 	HashSet<String> nodes = new HashSet<String>();
 	public void walkTree(CommonTree t, String filename) 
 	{
 		try {
 		BufferedWriter out = new BufferedWriter(new FileWriter(filename + ".rb"));
 	//	out.write("require \"set\"\n");
 
 		if(!(t.getType() == 0)){
 			
 			walk((CommonTree) t, out);
 		}
 		//traverse all the child nodes of the root if root was empty
 		else{
 		//	System.out.println("ERROR: ROOT IS NULL");
 			walk((CommonTree) t, out);
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
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(1), out);
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
 						if(printedAlready.contains((CommonTree)t)){
 							printedAlready.remove(t);
 						}else
 						out.write(t.getText().substring(0, t.getText().length()-4) + "\" ");
 						break;
 					case TanGParser.FLOAT:
 						if(printedAlready.contains((CommonTree)t)){
 							printedAlready.remove(t);
 						}else
 							out.write(t.getText()  + " ");
 						break;
 					case TanGParser.FOR:
 						out.write(t.getText());
 						break;
 					case TanGParser.FORK:
 						break;
 					case TanGParser.FROM:
 						break;
 					case TanGParser.FUNCID:
 						if(printedAlready.contains((CommonTree)t)){
 							printedAlready.remove(t);
 						}else{
 						out.write("td_"+t.getText() + " ");
 							}
 						
 						break;	
 					case TanGParser.HEX:
 						if(printedAlready.contains((CommonTree)t)){
 							printedAlready.remove(t);
 						}else
 						out.write(t.getText()  + " ");
 						break;
 					case TanGParser.HEX_DIGIT:
 						if(printedAlready.contains((CommonTree)t)){
 							printedAlready.remove(t);
 						}else
 							out.write(t.getText() + " ");
 						break;
 					case TanGParser.ID:
 						
 						if(printedAlready.contains((CommonTree)t)){
 							printedAlready.remove(t);
 						}else{
 							if((t.getParent()).getType() == TanGParser.DOT && t.getChildIndex()!=0){
 							String param = "";
 							int w = (t.getParent().getParent()).getChildCount();
 							int i=0;
 						
 							while(t.getParent().getParent().getChild(i)!=t.getParent() && i<w){
 								
 								i++;
 							}
 							i++;
 							
 							while(t.getParent().getParent().getChild(i) != null && t.getParent().getParent().getChild(i).getType() != TanGParser.NEWLINE && i<w){
 									
 								if((t.getParent().getParent().getChild(i)).getType() ==TanGParser.ID || (t.getParent().getParent().getChild(i).getType()) ==TanGParser.FUNCID){
 									param = param + "td_"+ t.getParent().getParent().getChild(i).getText() + ", ";
 								}else{
 									param = param + t.getParent().getParent().getChild(i).getText() + ", ";
 								
 								}
 								
 								
 								printedAlready.add((CommonTree) t.getParent().getParent().getChild(i));
 							
 
 								i++;
 							}
 							//	System.out.println(printedAlready);
 							
 							if(param.length()>0){							
 								out.write(t.getText() + "(" + param.substring(0, param.length()-2) + ")");
 							}else{
 								out.write(t.getText() + "(" + param + ")");
 							}
 							}
 							else{
 							
 							if(t.getType() == TanGParser.ID || t.getType() == TanGParser.FUNCID){
 								out.write("td_"+t.getText() + " ");
 							}
 							else{
 								if(t.getParent().getType() == TanGParser.DOT && t.getChildIndex()==0){
 									out.write(t.getText());
 								}else{
 									out.write(t.getText() + " ");
 								}
 							}
 							}
 						}
 						break;
 					case TanGParser.IF:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.IMPORT:
 						out.write("require ");
 						walk((CommonTree)t.getChild(0), out);
 						break;
 					case TanGParser.IN:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.INT:
 						if(printedAlready.contains((CommonTree)t)){
 							printedAlready.remove(t);
 						}else
 						out.write(t.getText() + " ");
 						break;
 				
 					case TanGParser.IS:
 						out.write("==");
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
 					case TanGParser.MAIN:
 						for (int i =0; i<t.getChildCount(); i++)
 							walk((CommonTree)t.getChild(i), out);
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
 						out.write("\n");
 						break;
 					case TanGParser.NODE:
 						LinkedList<CommonTree> list = new LinkedList<CommonTree>();
 
 						//every node will be converted to a class with the name of the node as the class name
 						if (t.getText().equals("public node"))
 						{
 							out.write("class ");
 							out.write(t.getChild(0).getText());
 							nodes.add(t.getChild(0).getText());
 						}
 						//if the class is private, add private after writing the constructor of the class
 						else
 						{
 							out.write("class ");
 							out.write(t.getChild(0).getText());
 							nodes.add(t.getChild(0).getText());
 							out.newLine();
 						}
 						out.newLine();
 						//then each class will have a main method with the node definition code
 						out.write("def main");
 						for ( int i = 1; i < t.getChildCount(); i++ ) 
 						{
 							if (t.getChild(i).getType()==TanGParser.MAIN){
 								for(int k=0; k < t.getChild(i).getChildCount(); k++){
 									if (t.getChild(i).getChild(k).getType()==TanGParser.NODE){
 										list.addLast(((CommonTree)t.getChild(i).getChild(k)));
 									}else{
 										walk((CommonTree)t.getChild(i).getChild(k), out);
 									}
 								}
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
 						out.write("end\n");
 						
 						out.newLine();
 						
 						break;
 					case TanGParser.NODEID:
 						doCheck(t, out);
 					
 						break;
 					case TanGParser.NOT:
 						out.write(t.getText());
 						walk((CommonTree)t.getChild(0), out);
 					
 						break;	
 					case TanGParser.NONE:
 						out.write(t.getText()+ " ");
 						break;
 					case TanGParser.NULL:
 						out.write("nil ");
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
 								params = params + "td_" + t.getChild(i) + ",";
 
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
 					case TanGParser.PIPEROOT:
 					for (int i =0; i<t.getChildCount(); i++)
 						walk((CommonTree)t.getChild(i), out);
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
 					case TanGParser.REQUIRE:
						out.write(t.getText() + " " + t.getChild(0));
 						break;
 					case TanGParser.RETURN:
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.RPAREN:
 						out.write(t.getText());
 						break;
 					case TanGParser.ROOTNODE:	
 						for (int i =0; i<t.getChildCount(); i++)
 							walk((CommonTree)t.getChild(i), out);
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
 						if(printedAlready.contains((CommonTree)t)){
 							printedAlready.remove(t);
 						}else
 						out.write(t.getText() + " ");
 						break;
 					case TanGParser.TF:
 						if(printedAlready.contains((CommonTree)t)){
 							printedAlready.remove(t);
 						}else
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
 						for (int i =0; i<t.getChildCount(); i++)
 							walk((CommonTree)t.getChild(i), out);
 						break;
 					case TanGParser.XOR:
 						walk((CommonTree)t.getChild(0), out);
 						out.write(t.getText()  + " ");
 						walk((CommonTree)t.getChild(1), out);
 						break;	
 					case 0:
 							for (int i =0; i<t.getChildCount(); i++)
 							walk((CommonTree)t.getChild(i), out);
 						break;						
 				}						
 		
 	}	}
 		catch (IOException e) {}
 }
 private void doCheck(CommonTree t, BufferedWriter out){
 	try{
 
 	if (t.getParent().getType() != 0 && t.getParent().getType() != TanGParser.PIPE && t.getParent().getType() != TanGParser.DOT){
 					String param = "";
 							int w = (t.getParent()).getChildCount();
 							int i=0;						
 							while(!(t.getParent().getChild(i).toStringTree().equals(t.toStringTree())) && i<w){
 								i++;
 							}
 							i++;
 							while(!(t.getParent().getChild(i).getText().contains("\n")) && i<w){
 								if(t.getParent().getChild(i).getType() ==TanGParser.ID || t.getParent().getChild(i).getType() == TanGParser.FUNCID){
 									param = param + "td_"+ t.getParent().getChild(i).getText() + ", ";
 								}else{
 									param = param + t.getParent().getChild(i).getText() + ", ";			
 								}
 								printedAlready.addLast((CommonTree)(t.getParent()).getChild(i));
 								i++;
 							}
 							if(t.getText().equals("Print")){
 								if(param.length()>0){							
 									out.write("print(" + param.substring(0, param.length()-2) + ")");
 							}else{
 								out.write("print(" + param + ")");
 							}
 							}
 							//this set checks if NodeID a system function or not. if not, .main is added
 							else if(nodes.contains(t.getText()) ){
 								if(param.length()>0){							
 								out.write(t.getText() + ".new().main(" + param.substring(0, param.length()-2) + ")");
 								}else{
 								out.write(t.getText() + ".new().main(" + param + ")");
 								}									
 							}else{
 								//the function must be a system function, so no main
 								if(param.length()>0){							
 									out.write(t.getText() + "(" + param.substring(0, param.length()-2) + ")");
 								}else{
 									out.write(t.getText() + "(" + param + ")");
 								}
 							}	
 						}else if (t.getParent().getType() == TanGParser.DOT){
 									out.write(t.getText());
 						}
 						else
 						{
 						//	if(!(t.getText().equals("Kernel")))
 						//			out.write(t.getText() + ".new().main");
 						//	else
 									out.write(t.getText() + ".new().main" );
 						}	
 	}
 		catch (IOException e) {}}
 
 }
 
