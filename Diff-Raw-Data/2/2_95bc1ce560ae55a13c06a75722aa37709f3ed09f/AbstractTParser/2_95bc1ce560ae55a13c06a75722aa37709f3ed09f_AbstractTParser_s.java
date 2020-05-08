 package de.uni_siegen.informatik.bs.alvic;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.antlr.runtime.*;
 
 /**
  * The super class of the generated parser. It is extended by the generated code
  * because of the superClass option in the .g file.
  * 
  * This class is based on the one given as a template by the Eclipes Maven
  * plugin.
  */
 public abstract class AbstractTParser extends Parser {
 	/**
 	 * List of all exceptions that are thrown while parsing.
 	 */
 	private List<RecognitionException> exceptions = new ArrayList<RecognitionException>();
 
 	/**
 	 * The name of a token given the number used by ANTLR to represent that
 	 * token.
 	 */
 	private Map<Integer, String> numToTokenName = null;;
 
 	/**
 	 * Read the TParser.tokens file to initialize numToTokenName.
 	 */
 	private void readTokenNumbers() {
 		InputStream tokenFile = getClass().getClassLoader()
 				.getResourceAsStream(
 						"/target/generated-sources/antlr3/TParser.tokens");
 		if (null == tokenFile)
 			return;
 		BufferedReader fstream = new BufferedReader(new InputStreamReader(
 				tokenFile));
 		List<String> tmp = new ArrayList<String>();
 		numToTokenName = new HashMap<Integer, String>();
 
 		try {
 			while (fstream.ready())
 				tmp.add(fstream.readLine());
 		} catch (IOException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		for (String s : tmp) {
 			String[] line = s.split("=");
 			numToTokenName.put(Integer.parseInt(line[1]), line[0]);
 		}
 	}
 
 	/**
 	 * Create a new parser instance, pre-supplying the input token stream.
 	 * 
 	 * @param input
 	 *            The stream of tokens that will be pulled from the lexer
 	 */
 	protected AbstractTParser(TokenStream input) {
 		super(input);
 		readTokenNumbers();
 	}
 
 	/**
 	 * Create a new parser instance, pre-supplying the input token stream and
 	 * the shared state.
 	 * 
 	 * This is only used when a grammar is imported into another grammar, but we
 	 * must supply this constructor to satisfy the super class contract.
 	 * 
 	 * @param input
 	 *            The stream of tokens that will be pulled from the lexer
 	 * @param state
 	 *            The shared state object created by an interconnected grammar
 	 */
 	protected AbstractTParser(TokenStream input, RecognizerSharedState state) {
 		super(input, state);
 		readTokenNumbers();
 	}
 
 	/**
 	 * @return a list of all exceptions the parser encountered.
 	 */
 	public List<RecognitionException> getExceptions() {
 		return exceptions;
 	}
 
 	/**
 	 * This method is called whenever an exception occurs when parsing. It
 	 * simply stores the exception in a list.
 	 * 
 	 * @param e
 	 *            The exception that occurred
 	 */
 	@Override
 	public void reportError(RecognitionException e) {
 		exceptions.add(e);
 	}
 
 	/**
 	 * Given the internal number of a token get its name.
 	 * 
 	 * @param tokenNumber
 	 *            The number of the token
 	 * @return the name of the token
 	 */
 	public String getTokenName(int tokenNumber) {
 		return numToTokenName.get(tokenNumber);
 	}
 
 	/**
 	 * When given the name of a token returns what tokens could possibly be
 	 * following it. Which tokens could be next is determined using the parsers
 	 * internal data structures.
 	 * 
 	 * @param parser
 	 *            This has to be set to TParser.class.
 	 * @param tokenName
 	 *            The name of the token, i.e. "ID" for identifiers, "LPAREN" for
 	 *            "(" etc.
 	 * @return Possible next tokens.
 	 */
 	public List<String> possibleFollowingTokens(
 			Class<? extends AbstractTParser> parser, String tokenName) {
 		Set<String> tokenNums = new TreeSet<String>();
 
 		try {
 			for (Field f : parser.getFields())
 				if (f.getName().startsWith("FOLLOW_" + tokenName + "_in_")) {
 					BitSet b = (BitSet) f.get(null);
 					for (int i : numToTokenName.keySet())
 						if (b.member(i))
 							tokenNums.add(numToTokenName.get(i));
 				}
 		} catch (SecurityException e) {
 			// This should not happen as long as the parser is a public class.
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// This does not happen as long as ANTLR generates the FOLLOW_* sets
 			// like it does now.
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// This does not happen as long as ANTLR generates the FOLLOW_* sets
 			// like it does now.
 			e.printStackTrace();
 		}
 
 		return new ArrayList<String>(tokenNums);
 	}
 }
