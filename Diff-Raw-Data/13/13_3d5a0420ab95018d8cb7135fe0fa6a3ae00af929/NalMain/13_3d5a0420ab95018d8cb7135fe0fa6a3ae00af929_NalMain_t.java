 /*
  * Copyright (c) 2013 Patrick Fromberg
  * See BSD 3 clause license in LICENSE.txt or
  * LICENSE.txt or http://opensource.org/licenses/BSD-3-Clause
  */
 
 package nal.antlr;
 
 import org.antlr.v4.runtime.*;
 import org.stringtemplate.v4.ST;
 import org.stringtemplate.v4.STGroup;
 import org.stringtemplate.v4.STGroupFile;
 
 import java.io.FileInputStream;
 import java.lang.reflect.Field;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 
 /*
  * The compiler consists of following classes:
  *
  * NalWalker walks the AST and populates the NalNode objects which implement the 
  * semantics of the language. The NalWalker only knows about class NalNode and nothing else
  * except the knowledge it inherits from the ANTLR system.
  *
  * NalNode and its specialized descendants implement the Nal language and know
  * only about the Nal language  itself and nothing else.
  *
  * NalEvaluator visits the NalNodes and executes them. NalEvaluator only knows
  * about NalNodes and the Nal functions exposed via SWIG.
  *
  * NalTranslator is similar to the NalEvaluator but instead of interpreting it
  * populates a template engine used to translate the NalNode tree.
  * NalTranslator only knows about NalNode objects and the template engine.
  * We use StringTemplate engine from Terence Parr
  *
  * NalMain plugs everything together.
  *
  */
 
 class NalMain
 {
 	public static void main(String[] args) throws Exception
 	{
 		// open all files first
 		if (args.length < 2) {
 			System.out.println("usage: java -classpath ... nal.antlr.NalMain <template_file>.stg " +
 					"<code_file>.nal <data_file>.xls");
 		}
 
 		String tmplFileName = args[0];
 		String codeFileName = args[1];
 		NalException.codeFileName = codeFileName;
 		//String dataFileName = args[2];
 
 		//BufferedReader dataReader = new BufferedReader(new InputStreamReader(new FileInputStream
 		// (dataFileName)));
 		ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(codeFileName));
 		STGroup stg = new STGroupFile(tmplFileName);
 
 		// do lexing and parsing
 		NalLexer lexer = new NalLexer(input);
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		NalParser parser = new NalParser(tokens);
 		parser.removeErrorListeners();
 		parser.addErrorListener(new ThrowingErrorListener());
 
 		// when generating code we need the operator name
 		// not the operator. i.e. we need 'OP_ADD' not '+'
 		HashMap<Integer, String> operatorNames = new HashMap<Integer, String>();
 		for (Field f : NalParser.class.getFields()) {
 			String name = f.getName();
 			if (f.getType() == int.class) {
 				if (name.indexOf("OP_") == 0) {
 					operatorNames.put(f.getInt(parser), name);
 				}
 			}
 		}
 
 		// Walk the tree and create program representation as a tree of NalNode objects.
 		try {
 			ParserRuleContext tree = parser.obj(); // parse
 			NalWalker walker = new NalWalker(operatorNames);
 			NalNode root = walker.visit(tree, null);
 
 			// If comments can not be ignored (e.g. pretty printing or refactoring) then add
 			// those comments to the corresponding nodes. Distinguish between pre, post and intra comments
 			tokens.reset();
 			for (Token t : tokens.getTokens()) {
 				if (t.getChannel() == Token.HIDDEN_CHANNEL) {
 					if (t.getType() == NalParser.PRE_COMMENT) {
 						int i = t.getTokenIndex();
 						NalNode n = walker.pre_comment_nodes.get(i);
 						if (n != null) {
 							n.pre_comment = t.getText().trim();
 						}
 						if (n == null || n.ruleType.equals("expr")) {
 							String text = t.getText().trim();
 							if (text.length() > 20) {
 								text = (text.substring(0, 20) + "...");
 							}
 							throw new NalException("pre-comments (comments using '/*' " +
 									"and" +
 									" " +
 									"" + "'*/) not allowed on expressions':",
 									text, t.getLine(), t.getCharPositionInLine());
 						}
 					} else if (t.getType() == NalParser.POST_COMMENT) {
 						int i = t.getTokenIndex();
 						NalNode n = walker.post_comment_nodes.get(i);
 						if (n != null) {
 							n.post_comment = t.getText().trim();
 						} else {
 							n = walker.intra_comment_nodes.get(i);
							if (n != null) {
								n.intra_comment = t.getText().trim();
							}
 						}
 						if (n == null) {
 							String text = t.getText().trim();
 							if (text.length() > 20) {
 								text = (text.substring(0, 20) + "...");
 							}
 							throw new NalException("post-comments using '//') only " +
 									"allowed" + " after expressions or function " +
 									"identifiers:", text, t.getLine(),
 									t.getCharPositionInLine());
 						}
 					}
 				}
 			}
 
 
 			// Call the translator and render the resulting root template
 			ST t = NalTranslator.visit(root, stg);
 			String code = t.render();
 
 			///////////
 			// BEGIN Workaround for StringTemplates indentation
 			// and newline problems
 			String nl = System.getProperty("line.separator");
 			String buffer = "";
 			int indent = 0;
 			int indent_width = 4;
 			int end_position = code.length();
 			int i = 0;
 			while (i < end_position) {
 				// indentation
 				if (buffer.length() == 0) {
 					while (code.startsWith(" ", i) || code.startsWith("\t", i)) {
 						i++;
 					}
 				}
 				if (code.startsWith("_nal_empty_line_", i)) {
 					System.out.println();
 					i += "_nal_empty_line_".length();
 					continue;
 				}
 				if (code.startsWith("_nal_push_indent_", i)) {
 					indent += indent_width;
 					i += "_nal_push_indent_".length();
 				}
 				if (code.startsWith("_nal_pop_indent_", i)) {
 					indent -= indent_width;
 					i += "_nal_pop_indent_".length();
 				}
 				if (buffer.length() == 0) {
 					for (int j = 0; j < indent; ++j) {
 						buffer += " ";
 					}
 				}
 
 				// remove empty lines
 
 				if (buffer.length() == indent) {
 					if (code.startsWith(nl, i) || i == end_position) {
 						i++;
 						buffer = "";
 						continue;
 					}
 				}
 
 				// comment aligning
 				if (code.startsWith("//", i)) {
 					String test = buffer;
 					test = test.replaceAll("</span>", "");
 					test = test.replaceAll("<span class=[^>]*>", "");
 					int ignore = buffer.length() - test.length();
 					while (buffer.length() - ignore < 40) {
 						buffer += " ";
 					}
 					while (!code.startsWith(nl, i) && i < end_position) {
 						buffer += code.charAt(i++);
 					}
 				}
 				// read next character or next line if at line end
 				if (code.startsWith(nl, i)) {
 					i += nl.length();
 					System.out.println(buffer);
 					buffer = "";
 				} else {
 					buffer += code.charAt(i++);
 				}
 			}
 			// END Workaround StringTemplates indentation problems
 			////////////////
 			if (NalException.messages.isEmpty()) {
 				System.out.println(buffer);
 			} else {
 				for (String m : NalException.messages) {
 					System.err.println(m);
 				}
 
 				System.exit(1);
 			}
 		} catch (Exception e) {
 			for (String m : NalException.messages) {
 				System.err.println(m);
 			}
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	static final private class ThrowingErrorListener extends BaseErrorListener
 	{
 		@Override
 		public void syntaxError(
 				Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
 				String msg, RecognitionException e)
 		{
 			List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
 			Collections.reverse(stack);
 
 			NalException.messages.add(NalException.codeFileName.replaceFirst(".*/",
 					"") + "[" + line + "," +
 					"" + charPositionInLine + "]" + (msg == null ? "" : msg) + " rule stack: " +
 					stack);
 
 
 		}
 	}
 }
