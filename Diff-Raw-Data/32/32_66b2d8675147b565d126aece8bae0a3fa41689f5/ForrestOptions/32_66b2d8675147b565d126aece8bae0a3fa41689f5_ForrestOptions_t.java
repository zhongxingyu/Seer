 package forrest.main;
 
 public class ForrestOptions {
 	
 	//What should the program do?
 	protected static final boolean parser = true;
 	protected static final boolean checker = true;
 	protected static final boolean interpreter = false;
 	protected static final boolean encoder = true;
	protected static boolean useTam = false;
	protected static boolean runProgram = false;
 	
 	protected static final boolean showParser = true;
 	protected static final boolean showChecker = true;
 	protected static final boolean showInterpreter = false;
 	protected static final boolean showEncoder = false;
 	
 	
 	
 	protected static final String inputFileLocation = "src/testfiles/ManualTestLex.forrest";	
 	protected static final String tasmFileLocation = "src/TAM/program.tasm";
 	protected static final String tamFileLocation = "src/TAM/program.tam";
 	
 	protected static final String[] assemblerArguments = {tasmFileLocation, tamFileLocation};
 	protected static final String[] interpreterArguments = {tamFileLocation};
 	
 	
 	
 	
 	
 }
