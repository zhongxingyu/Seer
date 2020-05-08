 package greenrubber.parser;
 
 import greenrubber.scanner.Token;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 
 public class ParseUtils {
 	
 	public static final String EPSILON = "<epsilon>";
 	
 	public static final int LHS = 0;
 	public static final int RHS = 1;
 	
 	private ParseUtils() { }
 	
 	public static void convertSpecToFlex(String specFileName, String outputFileName) throws IOException {
 		StringBuffer buf = new StringBuffer();
 		buf.append("%option noyywrap\n\n");
 		buf.append("%{\n" + 
 				"int line_num = 1;\n" + 
 				"static FILE* outputFileStream;\n" + 
 				"%}\n\n");
 		
 		Map<String, String> definedClasses = new HashMap<String, String>();
 		List<String> lines = FileUtils.readLines(new File(specFileName));
 		
 		int i = 0;
 		
 		/* Read defined classes */
 		while (!lines.get(i).trim().equals("")) {
 			String modifiedLine = lines.get(i).replace("$", "");
 			int firstSpaceIndex = modifiedLine.indexOf(' ');
 			String className = modifiedLine.substring(0, firstSpaceIndex);
 			String regex = modifiedLine.substring(firstSpaceIndex + 1);
 			
 			if (regex.contains("IN")) {
 				regex = new InConverter(regex, definedClasses).convert();
 			}
 			
 			buf.append(className);
 			buf.append(" ");
 			buf.append(regex);
 			buf.append("\n");
 			
 			definedClasses.put(className, regex);
 			
 			++i;
 		}
 		
 		/* Skip blank line after defined classes */
 		++i;
 		buf.append("\n\n");
 		buf.append("%%\n\n");
 		
 		while (i < lines.size()) {
 			String modifiedLine = lines.get(i).replace("$", "");
 			int firstSpaceIndex = modifiedLine.indexOf(' ');
 			String className = modifiedLine.substring(0, firstSpaceIndex);
 			String regex = modifiedLine.substring(firstSpaceIndex + 1).replace(" ", "");
 			for (Map.Entry<String, String> e : definedClasses.entrySet()) {
 				regex = regex.replace(e.getKey(), "{" + e.getKey() + "}");
 			}
 			
 			buf.append(regex);
 			buf.append(" { fprintf(outputFileStream, \"");
 			buf.append(className);
 			buf.append(" %s\\n\", yytext); } ");
 			buf.append("\n\n");
 			
 			++i;
 		}
 		
 		buf.append("%%\n\n");
 		
 		buf.append("int main(int argc, char *argv[]) {\n" + 
 				"	outputFileStream = fopen(\"generatedTokens.txt\", \"w\");\n" + 
 				"	\n" + 
 				"	yyin = fopen(\"input.txt\", \"r\");\n" + 
 				"  	yylex();\n" + 
 				"  	fclose(yyin);\n" + 
 				"  	\n" + 
 				"  	fclose(outputFileStream);\n" + 
 				"}");
 		
 		FileUtils.write(new File(outputFileName), buf);
 	}
 	
 	public static Grammar generateGrammarFromFile(String filename) throws IOException {
 		Grammar grammar = new Grammar();
 		List<String> file = FileUtils.readLines(new File(filename));
 		
 		int count = 0;
 		String[] ruleLine, blocks, currBlock;
 		String ruleName, rhs;
 		List<String> rhsList;
 		Rule rule;
 		for(String s : file)
 		{
 			rhsList = new LinkedList<String>();
 			ruleLine = s.split("::=");
 			ruleName = ruleLine[LHS].trim();
 			rhs = ruleLine[RHS].trim();
 			
 			blocks = rhs.split("\\|");
 			for (int i = 0; i < blocks.length; i++) {
 				rule = new Rule(grammar, ruleName);
 				currBlock = blocks[i].trim().split(" ");
 				for (int j = 0; j < currBlock.length; j++) {
 					currBlock[j] = currBlock[j].trim();
 					if (!currBlock[j].equals("")) {
 						rule.concatenate(currBlock[j].trim());	
 					}				
 				}
 				grammar.addRule(ruleName, rule, (count == 0 ? true : false));
 				count++;
 			}
 		}
 		
 		grammar.buildFollowSets();
 		
 		return grammar;
 	}
 	
 	public static List<Token> readTokensFromFile(String filename) throws IOException {
 		List<String> lines = FileUtils.readLines(new File(filename));
 		List<Token> tokens = new LinkedList<Token>();
 		
 		for (String line : lines) {
 			int firstSpaceIndex = line.indexOf(' ');
 			tokens.add(new Token(line.substring(0, firstSpaceIndex), line.substring(firstSpaceIndex + 1)));
 		}
 		
 		return tokens;
 	}
 	
 	public static void main(String[] args) throws IOException {
//		convertSpecToFlex("testCases/ll1TestCase/LL1Spec", "testCases/ll1TestCase/LL1Input");
//		if (1 == 1) {
//			return;
//		}
 		
 		Grammar g = generateGrammarFromFile("testCases/ll1TestCase/LL1Grammar");
 		System.out.println(g);
 		
 //		Set<String> firstSet = g.getFullFirstSet("<B>");
 //		System.out.println(firstSet);
 //		
 //		Set<String> followSet = g.getFullFollowSet("<B>");
 //		System.out.println(followSet);
 		
 		ParseTable t = g.buildParseTable();
 		System.out.println(t);
 		
 //		List<Token> tokens = new LinkedList<Token>();
 //		tokens.add(new Token("Water", "Hippo"));
 //		tokens.add(new Token("Water", "Hippo"));
 //		tokens.add(new Token("Land", "Giraffe"));
 		
 //		List<String> tokens = new LinkedList<String>();
 //		tokens.add("Hippo");
 //		tokens.add("Giraffe");
 //		tokens.add("Giraffe");
 		
 		System.out.println("<exp-tail>");
 		System.out.println(g.getFullFirstSet("<exp-tail>"));
 		System.out.println(g.getFullFollowSet("<exp-tail>"));
 		
 		System.out.println("<exp>");
 		System.out.println(g.getFullFirstSet("<exp>"));
 		System.out.println(g.getFullFollowSet("<exp>"));
 		
 		ParseTableWalker w = new ParseTableWalker(readTokensFromFile("testCases/ll1TestCase/Tokens"), t);
 		boolean success = w.parse();
 		
 		System.out.println(success);
 		
 		
 	}
 }
