 
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.util.List;
 import java.util.ListIterator;
 
 import uk.ac.ucl.comp2010.bestgroup.AST.Node;
 import uk.ac.ucl.comp2010.bestgroup.AST.ProgramNode;
 import java_cup.runtime.Symbol;
 import uk.ac.ucl.comp2010.bestgroup.*;
 
 public class QC {
 	private static QLex lex;
 	private static QCup pccParser;
    private static String input;
 
 	/**
 	 * Prints the int value of tokens created by the lexer object and values
 	 * within the token object (if any)
 	 * 
 	 * @throws IOException
 	 */
 	private static void lexicalAnalysisOnly() throws IOException {
 		Symbol token;
 		while ((token = lex.next_token()).sym != 0) {
 			System.out.print(token.toString());
 			System.out.println(token.value != null ? "(" + token.value.toString() + ")" : "");
 		}
 	}
 
 
 
 	public static void main(String[] args) {
 		try {
 	        boolean lexOnly = false;
 	        boolean repeatCode = false;
 	        boolean checkSemantics = false;
 	        boolean ast = false;
 	        
 	        for (String arg : args) {
 	            if (arg.equalsIgnoreCase("--lex")) {
 	                lexOnly = true;
 	            } else if (arg.equalsIgnoreCase("--repeat")) {
 	                repeatCode = true;
 	            }else if (arg.equalsIgnoreCase("--semantics")) {
 	            	checkSemantics= true;
 	            }else if (arg.equalsIgnoreCase("--ast")) {
 	                ast = true;
 	            } else {
	                input = arg;
 	            }
 	        }
 	        
            lex = new QLex(new FileReader(input));
             pccParser = new QCup(lex);
             if (lexOnly) {
                 lexicalAnalysisOnly();
             } else {
             	System.out.println("Parsing\n-------");
                 Symbol parse_tree = pccParser.parse();
             	
         		if(repeatCode) {
         			System.out.println("\n\nRepeating code\n--------------");
         			new CodeOutputVisitor().visit((ProgramNode)parse_tree.value);
         		}
         		
         		if(checkSemantics) {
         			System.out.println("\n\nRunning Semantics Visitor\n---------------------");
         			new SemanticsVisitor().visit((ProgramNode)parse_tree.value);
         		}
         		
         		if(ast) {
         			System.out.println("\nAST\n---");
         			displayTree(parse_tree.value, 0);
         		}
             }
             } catch  (Exception ex) {
 			ex.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	
 	public static void displayTree(Object node, int i) {
 		if(node == null) {
 			System.out.println("null");
 			return;
 		}
 		
 		
 		System.out.println(node.getClass().getSimpleName());
 		
 		Field[] nodeFields = node.getClass().getFields();
 		for(Field field: nodeFields) {
 			if(java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.getName() == "lineNumber" || field.getName() == "charNumber") {
 				continue;
 			}
 			
 			try {
 				if(Node.class.isInstance(field.get(node))) {
 					System.out.print(indent(i+2) + field.getName() + " = ");
 					displayTree(field.get(node), i+2 + field.getName().length() + 3);
 					
 				} else if(List.class.isInstance(field.get(node)) && field.get(node) != null){
 					System.out.print(indent(i+2) + field.getName() + " = ");
 					
 					ListIterator<Object> li = ((List) field.get(node)).listIterator();
 					if(! li.hasNext()) {
 						System.out.println("[empty list]");
 					} else {
 						while(li.hasNext()) {
 							if(li.hasPrevious()) {System.out.print(indent(i+2 + field.getName().length() + 3));}
 							System.out.print("*");
 							Object o = li.next();
 							if(Node.class.isInstance(o)) {
 								displayTree(o, i+2 + field.getName().length() + 4);
 							} else {
 								System.out.println(o);
 							}
 						}
 					}
 				//} else if(ExprOperationNode.class.isInstance(node) && field.getName() == "op") {
 					
 					
 				} else if(! (field.getName()=="type" && field.get(node) == null)){
 					System.out.println(indent(i+2) + field.getName() + " = " + field.get(node));
 				}
 				
 			} catch (IllegalArgumentException | IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static String indent(int level) {
 		String s = "";
 		for(int i=0; i<level; i++) {
 			s = s + " ";
 		}
 		return s;
 	}
 }
