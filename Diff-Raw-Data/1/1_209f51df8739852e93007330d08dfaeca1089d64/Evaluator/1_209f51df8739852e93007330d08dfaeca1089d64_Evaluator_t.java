 package cs3240.sp09.MicroAWKIntepreter;
 import cs3240.sp09.AbstractSyntaxTree.*;
 import cs3240.sp09.AbstractSyntaxTree.ASTNode.NodeType;
 import cs3240.sp09.DataStrucutres.DynamicList;
 import cs3240.sp09.MicroAWKIntepreter.Interpreter.Options;
 import cs3240.sp09.RegularLanguage.*;
 
 /**
  * Program flow:
  * 	0. Step through tree, converting all regex nodes to DFAs.
  * 	1. Step through the tree, executing any BEGIN statements.
  *  2a. For each input string, step through the tree executing any matching regex-action pairs.
  *  2b. If an action doesn't have a matching regex, execute it anyway.
  *  3. Step through the tree, executing any END statements.
  * @author Anthony Marion
  *
  */
 public class Evaluator {
 	
 	public static void evaluate(ASTNode root, DynamicList<String> input){
 		evaluateRegexes(root);
 		executeBegin(root);
 		for(String str : input){
 			try {
 				execute(root, str);
 			} catch(InvalidInputStringException e){
 				System.err.println(String.format("Invalid input: %c", e.getChar()));
 			}
 		}
 		executeEnd(root);
 	}
 
 	private static void evaluateRegexes(ASTNode node) {
 		if(node.type == NodeType.Regex){
 			RegexNode rex = (RegexNode)node;
 			rex.nfa = new NFA(rex);
 			if(Interpreter.options.contains(Options.NFA)) {
 				System.out.println(rex.nfa);
 			}
 			rex.dfa = new DFA(rex.nfa);
 			//System.err.println();
 			if(Interpreter.options.contains(Options.DFA)) {
 				System.out.println(rex.dfa);
 			}
 		} else { // don't really care to evaluate regex node's children.
 			if(node.leftChild != null)
 				evaluateRegexes(node.leftChild);
 			if(node.rightChild != null)
 				evaluateRegexes(node.rightChild);
 		}
 	}
 
 	private static String execute(ASTNode node, String string) throws InvalidInputStringException {
 		if(node == null) return string;
 
 		// copy of the input string
 		String line = new String(string);
 		switch(node.type){
 		case Program:
 			line = execute(node.leftChild, line);
 			line = execute(node.rightChild, line);
 			break;
 		case Statement:
 			switch(node.leftChild.type) {
 			case RFPair:
 			case FunctionBlock:
 			case WhileLoop:
 			case ForLoop:
 				line = execute(node.leftChild, line);
 				break;
 			default:
 				break;
 			}
 			break;
 		case RFPair:
 			RegexNode regexNode = (RegexNode)node.leftChild;
 			if(regexNode.dfa.matches(line)){
 				line = execute(node.rightChild, line);
 			}
 			break;
 		case FunctionBlock:
 			line = execute(node.leftChild, line);
 			if(node.rightChild != null)
 				line = execute(node.rightChild, line);
 			break;
 		case Function:
 			switch(node.leftChild.type){
 			case SubstringFunction:
 				if(Interpreter.options.contains(Options.DEBUG)) {
 					System.out.print("substring(");
 					System.out.print(integer(node.leftChild.leftChild));
 					System.out.print(",");
 					if(integer(node.leftChild.rightChild) == -1) {
 						System.out.print("END");
 					}
 					else {
 						System.out.print(integer(node.leftChild.rightChild));
 					}
 					System.out.print(") : ");
 					// pdf: after each pair has been interpreted, the modified text should be printed.
 					line = substringFunction(node.leftChild, line);  
 					System.out.println(line); 
 				} else {
 					// not quite dry, but somewhat elegant.
 					line = substringFunction(node.leftChild, line);
 				}
 				break;
 			case InsertFunction:
 				if(Interpreter.options.contains(Options.DEBUG)) {
 					System.out.print("insert(");
 					if(integer(node.leftChild.leftChild) == -1) {
 						System.out.print("END");
 					}
 					else {
 						System.out.print(integer(node.leftChild.leftChild));
 					}
 					System.out.print(",");
 					System.out.print(character(node.leftChild.rightChild));
 					System.out.print(") : ");
 					line = insertFunction(node.leftChild, line);
 					System.out.println(line);
 				} else {
 					line = insertFunction(node.leftChild, line);
 				}
 				break;
 			case PrintFunction:
 				if(Interpreter.options.contains(Options.DEBUG)) {
 					System.out.print("print");
 					System.out.print(" : ");
 					if(node.leftChild.leftChild.type == NodeType.Line) {
 						System.out.println(line);
 					} else if (node.leftChild.leftChild.type == NodeType.String) {
 						StringInnerNode inner = (StringInnerNode)node.leftChild.leftChild.leftChild;
 						System.out.println(inner.getValue());
 					}
 				}
 				printFunction(node.leftChild, line);
 				break;
 			case ReplaceFunction:
 				if(Interpreter.options.contains(Options.DEBUG)) {
 					System.out.print("replace(");
 					System.out.print(character(node.leftChild.leftChild));
 					System.out.print(",");
 					System.out.print(character(node.leftChild.rightChild));
 					System.out.print(") : ");
 					line = replaceFunction(node.leftChild, line);
 					System.out.println(line);
 				} else {
 					line = replaceFunction(node.leftChild, line);
 				}
 				break;
 			case RemoveFunction:
 				if(Interpreter.options.contains(Options.DEBUG)) {
 					System.out.print("insert(");
 					System.out.print(character(node.leftChild.leftChild));
 					System.out.print(") : ");
 					line = removeFunction(node.leftChild, line);
 					System.out.println(line);
 				} else {
 					line = removeFunction(node.leftChild, line);
 				}
 				break;
 			default:
 				error();
 				break;
 			}
 			break;
 		case WhileLoop:
 			RegexNode whileRegexNode = (RegexNode)node.leftChild;
 			if(Interpreter.options.contains(Options.DEBUG)) {
 				System.out.println("while(" +
 						             line +
 						             ")");
 			}
 			//FIXME is this correct?
 			ASTNode whileLoopNode = (ASTNode)node.rightChild;
 			while(whileRegexNode.dfa.matches(line)) {
 				ASTNode rfPairNode = whileLoopNode.leftChild;
 				line = execute(rfPairNode, line);
 				
 				// Go to next node in loop, restarting if at end
 				whileLoopNode = whileLoopNode.rightChild;
 				if(whileLoopNode == null) {
 					whileLoopNode = node.rightChild;
 				}
 			}
 			break;
 		case ForLoop:
 			int count = integer(node.leftChild);
 			if(Interpreter.options.contains(Options.DEBUG)) {
 				System.out.println("for(" +
 						             count +
 						             ")");
 			}
 			ASTNode forLoopNode = (ASTNode)node.rightChild;
 			for(int i = 0; i < count; i++) {
 				ASTNode rfPairNode = forLoopNode.leftChild;
 				line = execute(rfPairNode, line);
 				
 				// Go to next node in loop, restarting if at end
 				forLoopNode = forLoopNode.rightChild;
 				if(forLoopNode == null) {
 					forLoopNode = node.rightChild;
 				}
 			}
 			break;
 		case Begin:
 		case End:
 			// do nothing
 			return line;
 		default:
 			error();
 			break;
 		}
 		return line;
 	}
 
 	private static String substringFunction(ASTNode node, String line) {
 		int startIndex = integer(node.leftChild);
 		int endIndex = integer(node.rightChild);
 		if(endIndex == -1)
 			endIndex = line.length();
 		char[] charArr = new char[endIndex - startIndex];
 		for(int i = 0; i < endIndex - startIndex; i++){
 			charArr[i] = line.charAt(startIndex + i);
 		}
 		return new String(charArr);
 	}
 
 	private static String insertFunction(ASTNode node, String line) {
 		int index = integer(node.leftChild);
 		char c = character(node.rightChild);
 		char[] charArr = new char[line.length() + 1];
 		if(index > line.length() || index == -1) 
 			index = line.length();
 		for(int i = 0; i < index; i++){
 			charArr[i] = line.charAt(i);
 		}
 		charArr[index] = c;
 		for(int i = index; i < line.length(); i++){
 			charArr[i + 1] = line.charAt(i);
 		}
 		return new String(charArr);
 	}
 
 	private static int integer(ASTNode node) {
 		if(node.type == NodeType.EndIndex){
 			return -1;
 		} else if(node.type == NodeType.Integer){
 			int number = 0;
 			ASTNode intNode = node;
 			while(intNode != null){
 				number *= 10;
 				number += number(intNode.leftChild);
 				intNode = intNode.rightChild;
 			}
 			return number;
 		} else {
 			error();
 			return -1;
 		}
 	}
 
 	private static int number(ASTNode node) {
 		switch(node.leftChild.type){
 		case Zero:
 			return 0;
 		case One:
 			return 1;
 		case Two:
 			return 2;
 		case Three:
 			return 3;
 		case Four:
 			return 4;
 		case Five:
 			return 5;
 		case Six:
 			return 6;
 		case Seven:
 			return 7;
 		case Eight:
 			return 8;
 		case Nine:
 			return 9;
 		default:
 			error();
 			break;
 		}
 		return -1;
 	}
 
 	private static void printFunction(ASTNode node, String line) {
 		if(!Interpreter.options.contains(Options.DEBUG)) {
 			if(node.leftChild.type == NodeType.Line){
 				System.out.println(line);
 			} else if (node.leftChild.type == NodeType.String){
 				StringInnerNode inner = (StringInnerNode)node.leftChild.leftChild;
 				System.out.println(inner.getValue());
 			} else {
 				error();
 			}
 		}
 	}
 
 	private static String replaceFunction(ASTNode node, String line) {
 		char[] lineArr = line.toCharArray();
 		char a = character(node.leftChild);
 		char b = character(node.rightChild);
 		for(int i = 0; i < line.length(); i++){
 			if(lineArr[i] == a){
 				lineArr[i] = b;
 			}
 		}
 		return new String(lineArr);
 	}
 
 	private static String removeFunction(ASTNode node, String line) {
 		char c = character(node.leftChild);
 		DynamicList<Character> charList = new DynamicList<Character>();
 		for(int i = 0; i < line.length(); i++){
 			if(line.charAt(i) != c){
 				charList.add(line.charAt(i));
 			}
 		}
 		char[] charArr = new char[charList.size()];
 		for(int i = 0; i < charArr.length; i++){
 			charArr[i] = charList.get(i);
 		}
 		return new String(charArr);
 	}
 
 	private static char character(ASTNode node) {
 		switch(node.leftChild.type){
 		case A:
 			return 'a';
 		case B:
 			return 'b';
 		case C:
 			return 'c';
 		default:
 			error();
 			return (char)-1;
 		}
 	}
 
 	private static void executeEnd(ASTNode node) {
 		if(node == null) return;
 		try {
 			switch(node.type){
 			case End:
 				execute(node.leftChild, "<<NOLINE>>");
 				break;
 			default:
 				executeEnd(node.leftChild);
 				executeEnd(node.rightChild);
 				break;
 			}
 		} catch (InvalidInputStringException e){
 			// impossible error.
 			assert(false);
 		}
 	}
 
 	private static void executeBegin(ASTNode node) {
 		if(node == null) return;
 		try {
 			switch(node.type){
 			case Begin:
 				execute(node.leftChild, "<<NOLINE>>");
 				break;
 			default:
 				executeBegin(node.leftChild);
 				executeBegin(node.rightChild);
 				break;
 			}
 		} catch (InvalidInputStringException e){
 			// impossible error
 			assert(false);
 		}
 	}
 	
 	private static void error(){
 		errorIf(false);
 	}
 	
 	private static void errorIf(boolean test) {
 		assert test : "Evaluation error!";
 	}
 	
 }
