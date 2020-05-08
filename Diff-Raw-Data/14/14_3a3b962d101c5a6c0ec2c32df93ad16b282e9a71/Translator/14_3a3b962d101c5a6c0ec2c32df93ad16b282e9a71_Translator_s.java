 package translator;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 
 import xtc.lang.JavaFiveParser;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 import xtc.util.Tool;
 
 public class Translator extends Tool {
 	/** Create a new translator. */
 	public Translator() {
		// Nothing to do.
 	}
 
 	public String getCopy() {
 		return "(C) 2010 5-Leaf Clover";
 	}
 
 	public String getName() {
 		return "Java to C++ Translator";
 	}
 
 	public String getExplanation() {
 		return "This tool translates a subset of Java to a subset of C++.";
 	}
 
 	public void init() {
 		super.init();
 
 		// Declare command line arguments.
 		runtime
 			.bool("printJavaAST", "printJavaAST", false, "Print the Java AST.")
 			.bool("countMethods", "optionCountMethods", false, "Print the number of method declarations.")
 			.word("outputFile", "outputFile", false, "The file to which to output the translated code (defaults to stdout; when the file cannot be written, goes to stdout)")
 		;
 	}
 
 	public void prepare() {
 		super.prepare();
 		// Perform consistency checks on command line arguments.
 	}
 
 	public File locate(String name) throws IOException {
 		File file = super.locate(name);
 
 		if (Integer.MAX_VALUE < file.length())
 			throw new IllegalArgumentException(file + ": file too large");
 
 		return file;
 	}
 
 	public Node parse(Reader in, File file) throws IOException, ParseException {
 		JavaFiveParser parser = new JavaFiveParser(in, file.toString(), (int)file.length());
 		Result result = parser.pCompilationUnit(0);
 		return (Node)parser.value(result);
 	}
 
 	public void process(Node node) {
		// Handle the printJavaAST option
 		if (runtime.test("printJavaAST"))
 			runtime.console().format(node).pln().flush();
 
 		if (runtime.test("optionCountMethods"))
 			new MethodCounter(runtime).dispatch(node);
 		
 		CppWriter.nukeFile(runtime.getString("outputFile"));
 		CppWriter writer = new CppWriter(runtime.getString("outputFile"));
 		writer.writeln("Test");
 		writer.close();
 	}
 
 	public static void main(String args[]) {
 		new Translator().run(args);
 	}
 }
