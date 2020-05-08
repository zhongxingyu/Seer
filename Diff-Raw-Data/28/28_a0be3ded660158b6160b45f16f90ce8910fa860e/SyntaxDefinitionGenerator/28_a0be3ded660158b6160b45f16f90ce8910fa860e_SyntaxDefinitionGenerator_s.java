 package org.pathwayeditor.codegenerator;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
import java.io.PrintStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.Arrays;
 import java.util.List;
 
 import joptsimple.OptionException;
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 import joptsimple.OptionSpec;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.DOTTreeGenerator;
import org.antlr.runtime.tree.Tree;
 import org.antlr.stringtemplate.AutoIndentWriter;
 import org.antlr.stringtemplate.StringTemplate;
 import org.antlr.stringtemplate.StringTemplateGroup;
 import org.antlr.stringtemplate.StringTemplateWriter;
 import org.pathwayeditor.codegenerator.gen.NotationGenLexer;
 import org.pathwayeditor.codegenerator.gen.NotationGenParser;
 import org.pathwayeditor.codegenerator.gen.NotationGenTree;
 
 public class SyntaxDefinitionGenerator {
 	private static final String SYNTAX_DEFN_TEMPLATE = "SyntaxServiceTemplate.stg";
 	private static int SUCCESS = 0;
 	private static int FAIL = 1;
 
 	private File fileName = null;
 	private String qualifiedName;
 	private File target = new File(".");
 	private final OptionParser cmdLineParser = new OptionParser();
 	private final OptionSpec<File> targetDirOption;
 	private final OptionSpec<Void> helpOption;
 	private int exitStatus = FAIL;
 	private boolean areParamatersValid;
 	private static final String USAGE = "program <options> <spec file>";
 	private static final int EXPECTED_NUM_SPEC_FILES = 1;
 	private static final List<String> TARGET_DIR_OPTIONS = Arrays.asList("t", "targetDir");
 	private static final List<String> HELP_OPTIONS = Arrays.asList("?", "h", "help");
 	private CommonTokenStream tokens;
 	private NotationGenParser parser;
 	private CommonTreeNodeStream nodes;
 
 	public SyntaxDefinitionGenerator() {
 		this.cmdLineParser.posixlyCorrect(true);
 		this.targetDirOption = cmdLineParser.acceptsAll(TARGET_DIR_OPTIONS, "The target directory").withRequiredArg().ofType(File.class)
 				.describedAs("directory name");
 		this.helpOption = this.cmdLineParser.acceptsAll(HELP_OPTIONS, "Display command line usage");
 	}
 
 	public void runApplication(String[] args) {
 		readCommandLine(args);
 		if (this.fileName != null) {
 			validateParameters();
 			if (this.areParamatersValid()) {
 				try {
 					run();
 					this.exitStatus = SUCCESS;
 				} catch (RuntimeException e) {
 					System.err.println("Error: A bug has been detected. Stacktrace below:");
 					e.printStackTrace(System.err);
 				} catch (Exception e) {
 					System.err.println("An error was detected: " + e.getMessage());
 				}
 			}
 		}
 	}
 
 	private void readCommandLine(String[] args) {
 		try {
 			OptionSet options = cmdLineParser.parse(args);
 			if (options.has(helpOption)) {
 				try {
 					System.out.println(USAGE);
 					this.cmdLineParser.printHelpOn(System.out);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			} else if (options.nonOptionArguments().size() == EXPECTED_NUM_SPEC_FILES) {
 				this.fileName = new File(options.nonOptionArguments().get(0));
 				if (options.has(this.targetDirOption)) {
 					this.target = options.valueOf(this.targetDirOption);
 				}
 			} else {
 				System.err
 						.println("Invalid arguments: type --help for correct usage.");
 			}
 		} catch (OptionException e) {
 			System.err.println(e.getMessage());
 		}
 	}
 
 	private void validateParameters() {
 		this.setParamatersValid(true);
 		if (!this.fileName.isFile() || !this.fileName.canRead()) {
 			System.err.println(this.fileName
 					+ " is not a file or cannot be read");
 			this.setParamatersValid(false);
 		}
 		if (!this.target.isDirectory() || !this.target.canWrite()) {
 			System.err.println(this.target
 					+ " is not a directory or cannot be written to");
 			this.setParamatersValid(false);
 		}
 	}
 
 	private void setParamatersValid(boolean areParamatersValid) {
 		this.areParamatersValid = areParamatersValid;
 	}
 
 	private boolean areParamatersValid() {
 		return areParamatersValid;
 	}
 
 	private void run() throws Exception {
 		InputStream inStream = new FileInputStream(fileName);
 		ANTLRInputStream input = new ANTLRInputStream(inStream);
 		NotationGenLexer lexer = new NotationGenLexer(input);
 		tokens = new CommonTokenStream(lexer);
 		parser = new NotationGenParser(tokens);
 		NotationGenParser.notation_spec_return r = parser.notation_spec();
		dumpTree(r.getTree(), new File("treeGrammar.dot"));
 		inStream.close();
 		CommonTree t = (CommonTree) r.getTree();
 		nodes = new CommonTreeNodeStream(t);
 		nodes.setTokenStream(tokens);
 		InputStream is = this.getClass().getResourceAsStream(SYNTAX_DEFN_TEMPLATE);
 		if(is == null){
 			throw new RuntimeException("Cannot open Resource: " + SYNTAX_DEFN_TEMPLATE);
 		}
 		Reader fr = new InputStreamReader(is);
 		StringTemplateGroup templates = new StringTemplateGroup(fr);
 		fr.close();
 		NotationGenTree walker = new NotationGenTree(nodes);
 //		NotationGenTree walker = new NotationGenTree(nodes, 49000, new RecognizerSharedState());
 		walker.setTemplateLib(templates);
 		NotationGenTree.notation_spec_return walkerRet = walker.notation_spec();
 		
 		this.qualifiedName = walker.getQualifiedName();
 		StringTemplate stj = (StringTemplate)walkerRet.getTemplate();
		System.out.println(stj.toString());
 		String prefix = target.toString();
 		sendOut(prefix, stj);
 	}
 
    private static void dumpTree(Object ast, File oFile) throws FileNotFoundException{
		DOTTreeGenerator dtg = new DOTTreeGenerator();
		StringTemplate st = dtg.toDOT((Tree)ast);
		PrintStream os = new PrintStream(new FileOutputStream(oFile)); 
		os.println(st);
		os.close();
    }
 
 	private void sendOut(String prefix, StringTemplate stj) throws IOException {
 		StringBuilder buf = new StringBuilder(prefix);
 		buf.append("/");
 		buf.append(this.qualifiedName.replace('.', '/'));
 		buf.append("NotationSyntaxService.java");
 		File outFile = new File(buf.toString());
 		Writer w = new FileWriter(outFile);
 		StringTemplateWriter wr = new AutoIndentWriter(w);
 		stj.write(wr);
 		w.close();
 	}
 
 
 	public int getExitStatus() {
 		return this.exitStatus;
 	}
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		SyntaxDefinitionGenerator cag = new SyntaxDefinitionGenerator();
 		cag.runApplication(args);
 		System.exit(cag.getExitStatus());
 	}
 
 }
