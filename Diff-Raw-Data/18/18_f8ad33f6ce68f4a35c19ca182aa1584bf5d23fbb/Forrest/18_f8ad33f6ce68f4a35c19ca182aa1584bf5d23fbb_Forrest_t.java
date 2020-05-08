 package forrest.main;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 
 import TAM.Assembler;
 import TAM.Interpreter;
 
 import fire.ForrestFireException;
 
 public class Forrest {
 	
 	//Streams used for input and output
 	private InputStream in;
 	private PrintStream out;
 	
 	public Forrest(){
 		
 	}
 	
 	/**
 	 * Method to run the ANTLR program on a forrest program
 	 * @param inputFile - String: The forrest program to compile (null means the standard inputfile from ForrestOptions.inputFileLocation)
 	 * @param outputFile - String: The targetFile (null means the stand System.out)
 	 * @param useParser - boolean: Whether to use the parser
 	 * @param useChecker - boolean: Whether to use the checker
 	 * @param useInterpreter - boolean: Whether to use the interpreter
 	 * @param useEncoder - boolean: Whether to use the encoder
 	 * @param useTam - boolean: Whether to use the TAM assembler
 	 * @param runProgram - boolean: Whether to run the assembled program
 	 */
 	public void runForrest(String inputFile, PrintStream outputPS, 
 		boolean useParser, boolean useChecker, boolean useInterpreter, boolean useEncoder, boolean useTam, boolean runProgram)
 		throws Exception{
 			if(inputFile == null){
 				this.in = new FileInputStream(ForrestOptions.inputFileLocation);
 			} else {
 				this.in = new FileInputStream(inputFile);
 			}
 			
 			if(outputPS == null){
 				this.out = new PrintStream(System.out);
 			} else {
 				this.out = outputPS;
 			}
 			
 			//Parse the Forrest program
 			ForrestLexer lexer = new ForrestLexer(new ANTLRInputStream(in));
 			CommonTokenStream tokens = new CommonTokenStream(lexer);
 			ForrestParser parser = new ForrestParser(tokens);
 			parser.setTreeAdaptor(new ForrestTreeAdaptor());
 			
 			ForrestParser.forrest_return result = parser.forrest();
 			CommonTree tree = (CommonTree) result.getTree();
			if(useParser && ForrestOptions.showParser){
 				out.println(tree.toStringTree());
 			}
 			
 			//Check the Forrest program
 			if(useChecker){
 				CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
 				ForrestChecker checker = new ForrestChecker(nodes);
 				checker.forrest();
 			}
 			
 			//Interpret the Forrest program
 //			if(useInterpreter){
 //				TreeNodeStream nodes = new BufferedTreeNodeStream(tree);
 //				ForrestInterpreter interpreter = new ForrestInterpreter(nodes);
 //				interpreter.forrest();
 //			}
 			
 			//Generate code for the Forrest Program
 			if(useEncoder){
 				CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
 				ForrestEncoder encoder = new ForrestEncoder(nodes);
 				encoder.forrest();
 			}
 			
 			//Use the Tam assembler if needed
 			if(useTam){
 				Assembler.main(ForrestOptions.assemblerArguments);
 			}
 			
 			//Run the program if needed
 			if(runProgram){
 				Interpreter.main(ForrestOptions.interpreterArguments);
 			}
 	}
 	
 	/**
 	 * Run the program
 	 * @param args unused
 	 */
 	public static void main(String[] args){
 		Forrest runner = new Forrest();
 		try{
 			runner.runForrest(null, null, ForrestOptions.parser, ForrestOptions.checker, ForrestOptions.interpreter, ForrestOptions.encoder, ForrestOptions.useTam, ForrestOptions.runProgram);
 		} catch (ForrestFireException e){
 			System.out.print("Fire!: ForrestException thrown by compiler ");
 			System.out.println(e.getMessage());
 		} catch (Exception e) {
 			System.out.print("ERROR: uncaught exception thrown by compiler: ");
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 	    }
 	}
 }
