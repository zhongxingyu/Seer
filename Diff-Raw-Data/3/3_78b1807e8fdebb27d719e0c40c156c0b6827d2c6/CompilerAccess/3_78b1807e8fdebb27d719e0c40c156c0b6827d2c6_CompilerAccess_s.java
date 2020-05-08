 package de.unisiegen.informatik.bs.alvis.compiler;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.Token;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 
 import de.unisiegen.informatik.bs.alvis.io.files.FileCopy;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 import de.uni_siegen.informatik.bs.alvic.AbstractTLexer;
 import de.uni_siegen.informatik.bs.alvic.Compiler;
 
 /**
  * @author mays
  * @author Colin
  * 
  */
 public class CompilerAccess {
 	/**
 	 * The compiler used to do all the real work.
 	 */
 	private Compiler compiler;
 
 	/**
 	 * These are the types the user may use.
 	 */
 	private Collection<PCObject> types;
 
 	/**
 	 * These are the packages available to the user. They will automatically be
 	 * imported by the compiler.
 	 */
 	private Collection<String> packages;
 
 	/**
 	 * This is the absolute path to the file that is to be compiled.
 	 */
 	private String algorithmPath = null;
 
 	private String javaCode;
 
 	private static CompilerAccess instance;
 
 	private static Map<String, List<String>> translateCompletion = null;
 
 	private static void add(String key, String... arg) {
 		translateCompletion.put(key, Arrays.asList(arg));
 	}
 
 	@SuppressWarnings("static-access")
 	private void loadTranslation() {
 		translateCompletion = new HashMap<String, List<String>>();
 		add("MAIN", "main ");
 		add("IF", "if (");
 		add("FOR", "for ");
 		add("WHILE", "while (");
 		add("IN", "in ");
 		add("RETURN", "return");
 
 		add("SIGN", "+", "-");
 		add("BANG", "!");
 
 		add("EQUAL", "=");
 		add("PLUS", "+");
 		add("MINUS", "-");
 		add("STAR", "*");
 		add("SLASH", "/");
 		add("PERCENT", "%");
 		add("AMPAMP", "&&");
 		add("PIPEPIPE", "||");
 		add("EQEQ", "==");
 		add("BANGEQ", "!=");
 		add("LESS", "<");
 		add("GREATER", ">");
 		add("LESSEQ", "<=");
 		add("GREATEREQ", ">=");
 
 		add("LPAREN", "(");
 		add("RPAREN", ")");
 		add("LARRAY", "[");
 		add("RARRAY", "]");
 
 		add("SEMICOLON", ";");
 		add("COMMA", ",");
 		add("COLON", ":");
 
 		add("SCOPEL", "begin", "{");
 		add("SCOPER", "end", "}");
 
 		add("NULL", "null");
 		add("INFTY", "infty");
 
 		translateCompletion.put("TYPE", new ArrayList<String>(compiler
 				.getLexer().getTypes()));
 	}
 
 	public static CompilerAccess getDefault() {
 		if (instance == null)
 			instance = new CompilerAccess();
 		return instance;
 	}
 
 	public File compile() throws RecognitionException, IOException {
 		return compile(algorithmPath, true);
 	}
 
 	/**
 	 * Use the compiler to compile the source code found in the given file.
 	 * Before calling this method you should provide the names of all packages
 	 * and classes that the user may use (using the setDatatypes and
 	 * setDatatypePackages methods).
 	 * 
 	 * @param path
 	 *            path to the source code that
 	 * @return path to the generated .java file if it exists, null otherwise
 	 * 
 	 * @throws IOException
 	 */
 	public File compile(String path) throws IOException {
 		return compile(path, false);
 
 	}
 
 	/**
 	 * Use the compiler to compile the source code found in the given file.
 	 * Before calling this method you should provide the names of all packages
 	 * and classes that the user may use (using the setDatatypes and
 	 * setDatatypePackages methods).
 	 * 
 	 * @param path
 	 *            path to the source code that we want to compile.
 	 * @param isAbsolutePath
 	 *            true if the path is absolute.
 	 * @return path to the generated .java file if it exists, null otherwise
 	 * 
 	 * @throws IOException
 	 */
 	public File compile(String path, Boolean isAbsolutePath) throws IOException {
 		if (isAbsolutePath)
 			algorithmPath = path;
 		else
 			algorithmPath = currentPath() + path;
 
 		compileString(readFile(algorithmPath));
 
 		if (null == javaCode) {
 			System.err.println("Compiling code from " + algorithmPath
 					+ " failed");
 			// for (Exception e : compiler.getExceptions())
 			// e.printStackTrace();
 			return null;
 		}
 
 		return writeJavaCode(getWorkspacePath(algorithmPath), "Algorithm");
 	}
 
 	/**
 	 * Write the generated Java code to a file. The file will be placed in the
 	 * given directory and will be called 'algorithmName + ".java"'.
 	 * 
 	 * @param directory
 	 *            The directory in which the file will be created.
 	 * @param algorithmName
 	 *            The name of the class used for the algorithm.
 	 * @return the file with the Java source code.
 	 * @throws IOException
 	 */
 	public File writeJavaCode(File directory, String algorithmName)
 			throws IOException {
 		File result = null;
 		BufferedWriter out = null;
 		FileWriter fstream;
 		try {
 			result = new File(directory, algorithmName + ".java");
 			fstream = new FileWriter(result);
 			out = new BufferedWriter(fstream);
 			out.write(javaCode.replaceAll("#ALGORITHM_NAME#", algorithmName));
 		} finally {
 			if (out != null)
 				out.close();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Given the pseudo code to compile create a string containing the generated
 	 * Java code.
 	 * 
 	 * @param code
 	 *            The pseudo code to be compiled.
 	 * @return the Java code the compiler generated.
 	 * @throws IOException
 	 */
 	public String compileString(String code) throws IOException {
		loadTranslation();
 		compiler = new Compiler(types, packages);
 		return javaCode = compiler.compile(code);
 	}
 
 	/**
 	 * Run lexer, parser and the type checker on the code given.
 	 * 
 	 * @param code
 	 *            The code to check.
 	 * @return list of the exceptions created when lexing, parsing and type
 	 *         checking the code.
 	 */
 	public List<RecognitionException> checkCode(String code) {
 		compiler = new Compiler(types, packages);
 		return compiler.check(code);
 	}
 
 	/**
 	 * @return path to working directory.
 	 */
 	private String currentPath() {
 		return Platform.getInstanceLocation().getURL().getPath();
 	}
 
 	/**
 	 * Get parent directory of the file given by its path.
 	 * 
 	 * @param fileWithPath
 	 *            The file of which we want to get the parent directory.
 	 * @return the path to the parent directory.
 	 */
 	private File getWorkspacePath(String fileWithPath) {
 		return new File(fileWithPath).getAbsoluteFile().getParentFile();
 	}
 
 	/**
 	 * Read a file given by its path into a String.
 	 * 
 	 * @param fileName
 	 *            the file to read
 	 * @return the contents of the file
 	 * @throws IOException
 	 */
 	private String readFile(String fileName) throws IOException {
 		BufferedReader fstream = new BufferedReader(new FileReader(fileName));
 		String result = "";
 
 		while (fstream.ready())
 			result += fstream.readLine() + System.getProperty("line.separator");
 
 		System.out.println("read file " + fileName);
 		return result;
 	}
 
 	private List<String> translateAutocompletionString(List<String> possibleTokens) {
 		ArrayList<String> translatedCompletions = new ArrayList<String>();
 		for (String toTranslate : possibleTokens) {
 			System.out.println(toTranslate);
 			List<String> translations = translateCompletion.get(toTranslate);
 			if (null == translations)
 				translatedCompletions.add(toTranslate);
 			else
 				for (String t : translations)
 					translatedCompletions.add(t);
 		}
 
 		return translatedCompletions;
 	}
 
 	/**
 	 * @return exceptions produced when lexing, parsing and type checking the
 	 *         code.
 	 */
 	public List<RecognitionException> getExceptions() {
 		return compiler.getExceptions();
 	}
 
 	/**
 	 * This method copies the Dummy Algorithm file next to the PCAlgorithm file
 	 * that is written by the user. To get the path of the created file see
 	 * getAlgorithmPath().
 	 * 
 	 * @param pathToAlgorithm
 	 *            relative to Alvis-workspace e.g.: "project/src/Algorithm.algo"
 	 * @return Name of the Java Algorithm file
 	 */
 	public File compileThisDummy(String pathToAlgorithm) {
 		String SLASH = File.separator;
 
 		// the path were the translated java file is.
 		String pathWhereTheJavaIs = "";
 		try {
 			pathWhereTheJavaIs = FileLocator
 					.getBundleFile(Activator.getDefault().getBundle())
 					.getCanonicalPath().toString();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// The compiled algorithm
 		File source = new File(pathWhereTheJavaIs + SLASH + "Algorithm.java");
 
 		// Get the path to algorithm and separate path and filename
 		String algoWorkSpacePath = new File(pathToAlgorithm).getParent();
 		algorithmPath = currentPath() + algoWorkSpacePath + SLASH;
 		File destination = new File(algorithmPath + "Algorithm.java");
 
 		// Copy compiled file into the workspace
 		FileCopy fileCopy = new FileCopy();
 		fileCopy.copy(source, destination);
 
 		return destination;
 	}
 
 	/**
 	 * Tell the compiler which types are allowed.
 	 * 
 	 * @param datatypes
 	 *            the types
 	 */
 	public void setDatatypes(Collection<PCObject> datatypes) {
 		this.types = datatypes;
 	}
 
 	/**
 	 * Tell the compiler which packages the user can use. I.e. what packages the
 	 * compiler is supposed to import.
 	 * 
 	 * @param datatypePackages
 	 *            the packages
 	 */
 	public void setDatatypePackages(Collection<String> datatypePackages) {
 		this.packages = datatypePackages;
 	}
 
 	/**
 	 * Tell the lexer to read the input stream so it can provide auto completion
 	 * and help for syntax highlighting.
 	 */
 	public void reLex() {
 		compiler.getLexer().scan();
 	}
 
 	/**
 	 * Computes the possible autoCompletion for the line and charPositionInLine
 	 * given. Returns a List containing all available auto completion Strings.
 	 *
 	 * @param line
 	 *            the line
 	 * @param charPositionInLine
 	 *            the offset in the line given
 	 * @return the List of Strings containing all available auto completion
 	 *         Strings.
 	 */
 	public List<String> tryAutoCompletion(int line, int charPositionInLine) {
 		Token tokenToComplete = compiler.getLexer().getTokenByNumbers(line,
 				charPositionInLine);
 
 		if (tokenToComplete == null) {
 			List<Token> tokens = compiler.getLexer().getTokens();
 			for (Token token : tokens) {
 				if (line > token.getLine()
 						|| ((line == token.getLine()) && charPositionInLine <= token
 								.getCharPositionInLine())) {
 					tokenToComplete = token;
 				} else {
 					// "next" token found
 					tokenToComplete = token;
 					break;
 				}
 			}
 		}
 		if (tokenToComplete != null) {
 			int previousTokenIndex = tokenToComplete.getTokenIndex() - 1;
 			// channel = 99 indicates a whitespace token
 			Token previousToken = null;
 			while (previousToken == null || previousToken.getChannel() == 99) {
 				if (previousTokenIndex < 0) {
 					/** tokenToComplete is first token */
 					// TODO return correct List
 					return new ArrayList<String>();
 
 				} else {
 					previousToken = compiler.getLexer().getTokens()
 							.get(previousTokenIndex);
 					previousTokenIndex--;
 				}
 			}
 
 			/**
 			 * if currentChar was a whitespace and offset was higher than the
 			 * one of the last Token
 			 */
 			if (tokenToComplete.getLine() < line
 					|| ((line == tokenToComplete.getLine()) && charPositionInLine > tokenToComplete
 							.getCharPositionInLine())) {
 				previousToken = compiler.getLexer().getTokens()
 						.get(previousTokenIndex + 2);
 			}
 
 			String previousTokenName = getTokenName(previousToken.getType());
 			System.out.println(previousTokenName);
 			List<String> possibleTokens = compiler.getParser()
 					.possibleFollowingTokens(compiler.getParser().getClass(),
 							previousTokenName);
 			System.out.println(possibleTokens);
 			List<String> translatedCompletions = translateAutocompletionString(possibleTokens);
 			return (List<String>) translatedCompletions;
 		} else {
 			return new ArrayList<String>();
 		}
 	}
 
 	/**
 	 * @return List of Tokens that are forbidden because they are Java keywords.
 	 */
 	public List<Token> getForbidden() {
 		return compiler.getLexer().getForbidden();
 	}
 
 	/**
 	 * @return List of all identifiers used in the code
 	 */
 	public List<Token> getIdentifiers() {
 		return compiler.getLexer().getIdentifiers();
 	}
 
 	/**
 	 * @return List of all keywords the compiler recognizes (e.g. 'if', 'else',
 	 *         ...)
 	 */
 	public List<Token> getKeywords() {
 		return compiler.getLexer().getKeywords();
 	}
 
 	/**
 	 * Create a list of all the tokens in the given source code that mark the
 	 * beginning of a block.
 	 * 
 	 * @return List of tokens that mark the beginning of a block
 	 */
 	public List<Token> beginBlock() {
 		return compiler.getLexer().beginBlock();
 	}
 
 	/**
 	 * Create a list of all the tokens in the given source code that mark the
 	 * end of a block.
 	 * 
 	 * @return List of tokens that mark the end of a block
 	 */
 	public List<Token> endBlock() {
 		return compiler.getLexer().endBlock();
 	}
 
 	/**
 	 * @return List of all available keywords.
 	 */
 	public List<String> allKeywords() {
 
 		return AbstractTLexer.allKeywords();
 	}
 
 	/**
 	 * Return a list of all the Java keywords that the pseudo code does not use.
 	 * 
 	 * @return List of forbidden words
 	 */
 	public List<String> allForbidden() {
 		return AbstractTLexer.allForbidden();
 	}
 
 	/**
 	 * @return List of all tokens created by the lexer.
 	 */
 	public List<Token> getTokens() {
 		return compiler.getLexer().getTokens();
 	}
 
 	public String getTokenName(int tokenNumber) {
 		return compiler.getParser().getTokenName(tokenNumber);
 	}
 
 	/**
 	 * Method for checking whether the compiler is informed correctly about
 	 * available packages and types.
 	 */
 	@SuppressWarnings("unchecked")
 	public void testDatatypes() {
 		try {
 			System.out.println("Compiler shows its datatypes:");
 			for (PCObject obj : types) {
 				System.out.println(obj.getClass());
 				List<String> tmp = ((List<String>) obj.getClass()
 						.getMethod("getMembers").invoke(obj));
 				System.out.println("available attributes:"
 						+ (tmp == null ? "null" : tmp));
 				tmp = ((List<String>) obj.getClass().getMethod("getMethods")
 						.invoke(obj));
 				System.out.println("available methods:"
 						+ (tmp == null ? "null" : tmp));
 			}
 			System.out.println("Compiler shows its packages:");
 			for (String obj : packages) {
 				System.out.println(obj);
 			}
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 	}
 }
