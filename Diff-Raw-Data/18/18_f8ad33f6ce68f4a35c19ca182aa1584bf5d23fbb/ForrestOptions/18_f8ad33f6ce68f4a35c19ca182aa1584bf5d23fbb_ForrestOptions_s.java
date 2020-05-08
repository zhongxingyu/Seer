 package forrest.main;
 
 public class ForrestOptions {
 	
 	//What should the program do?
 	/** Run the parser */
 	protected static final boolean parser = true;
 	/** Run the checker */
 	protected static final boolean checker = true;
 	/** Run the interpreter - unused */
 	protected static final boolean interpreter = false;
 	/** Run the encoder */
 	protected static final boolean encoder = true;
 	/** Use TAM to convert the output to assembly */
 	protected static boolean useTam = true;
 	/** Use the TAM interpreter to run the program */
 	protected static boolean runProgram = true;
 	
 	/** Show the output of the parser */
	protected static final boolean showParser = true;
 	/** Show the output of the checker */
	protected static final boolean showChecker = true;
 	
 	
 	/** Forrest file to read from */
 	protected static final String inputFileLocation = "src/testfiles/ManualTestLex.forrest";
 	/** TAM file to output to and read from */
 	protected static final String tasmFileLocation = "src/TAM/program.tasm";
 	/** TAM assembler file to output to and read from */
 	protected static final String tamFileLocation = "src/TAM/program.tam";
 	
 	/** Arguments to the TAM assembler */
 	protected static final String[] assemblerArguments = {tasmFileLocation, tamFileLocation};
 	/** Arguments tot the TAM interpreter */
 	protected static final String[] interpreterArguments = {tamFileLocation};
 	
 	
 	
 	
 	
 }
