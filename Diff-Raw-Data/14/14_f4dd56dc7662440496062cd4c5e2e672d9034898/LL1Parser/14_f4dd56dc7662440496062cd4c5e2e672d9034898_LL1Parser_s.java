 import java.io.File;
 import java.util.List;
 import java.util.Stack;
 
 
 public class LL1Parser {
 	private LL1Grammar grammar;
 	private LL1ParsingTable parsingTable;
 	private LL1Lexer lexer;
 	private boolean verbose = true;
 	
 	public static void main(String[] args) {
 		if (args.length == 2) {
 			File grammarFile = new File(args[0]);
 			File fileToParse = new File(args[1]);
 			
 			LL1Parser parser = new LL1Parser(grammarFile, fileToParse);
 			try {
 				parser.parse();
 			} catch (LL1ParseException e) {
 				e.printStackTrace();
 			}
 		} else {
 			System.err.println("Incorrect usage. Required argument filename not provided.");
 		}
 	}
 	
 	public LL1Parser(File grammarFile, File fileToParse) {
 		grammar = new LL1Grammar(grammarFile);
 		System.out.println(grammar);
 		parsingTable = new LL1ParsingTable(grammar);
 		lexer = new LL1Lexer(fileToParse);
 	}
 	
 	public void parse() throws LL1ParseException {
 		Stack<RuleElement> parsingStack = new Stack<RuleElement>();
 		parsingStack.push(grammar.getStartVariable());
 		
 		Token token = lexer.next();
 		while (!parsingStack.isEmpty()) {
 			if (verbose) {
 				System.out.println(parsingStack);
 			}
 			RuleElement re = parsingStack.pop();
 			if (re instanceof Variable) {
 				Variable v = (Variable)re;
 				List<RuleElement> newRuleElements = parsingTable.getRuleElements(v, token.getType());
 				
 				if (newRuleElements != null) {
 					for (int i = newRuleElements.size() - 1; i >= 0; i--) {
 						parsingStack.push(newRuleElements.get(i));
 					}
 				} else {
 					throw new LL1ParseException("Parsing failed with token of type " + token.getType() + " and stack: " + parsingStack);
 				}
 			} else if (re instanceof EmptyString) {
 				
 			} else if (re instanceof Terminal) {
 				Terminal t = (Terminal)re;
 				TokenType tokenType = TokenType.tokenWithIdentifier(t.toString());
 				if (tokenType == token.getType()) {
					token = lexer.next();
 					if (verbose) {
 						System.out.println("Parsed " + token);
 					}
 				} else {
 					throw new LL1ParseException("Unexpected " + token.getType() + " (expected "+ tokenType + ")");
 				}
 			}
 		}
 		
 		System.out.println("Successful parse!");
 	}
 }
