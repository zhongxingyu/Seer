 /* Dette er "kompilatoren" (frontenden), som blir kjørt
    med JPT.jar. -svk */
 
 package testutils;
 
 import jargs.gnu.CmdLineParser;
 import jargs.gnu.CmdLineParser.IllegalOptionValueException;
 import jargs.gnu.CmdLineParser.UnknownOptionException;
 
 import java.io.File;
 
 import testutils.exceptions.CompileErrorException;
 import testutils.exceptions.FatalErrorException;
 import testutils.tester.GenerateJava;
 import testutils.utils.FileIO;
 
 public class PTToJavaPackage {
 
 	private String sourceFolderName;
 	private String outputFolderName;
 	private boolean verbose;
 	private String[] inputfilenames;
 	private GenerateJava compiler;
 
 	public static void main(String[] args) {
 		PTToJavaPackage controller;
 		String errorCause;
 		try {
 			controller = parseArgsAndInstantiate(args);
 			controller.run();
 		} catch (IllegalOptionValueException e) {
 			errorCause = e.getMessage();
 			printUsage(errorCause);
 		} catch (UnknownOptionException e) {
 			errorCause = e.getMessage();
 			printUsage(errorCause);
 		} catch (FatalErrorException e) {
 			errorCause = e.getMessage();
 			printUsage(errorCause);
		} catch (CompileErrorException e) {
            // the compile errors have already been printed at this point (?)

            // note that printing the exception also prints a prettyprinted
            // source code dump, which I mostly found confusing since
            // it was a code form of an AST during rewriting treatment
            // (which was not necessarily a complete representation of
            //  the AST) and e.g. gave the impression that internal things
            // were ending up in the final code when they were really
            // just used internally (e.g. tabstract, notwithstanding that
            // at one point there _was_ a bug where that was printed to the
            // final code). feel free to add exception printing back if you find
            // this information useful.

            System.out.println( "Compilation failed: fatal errors." );
 		}
 	}
 
 	private static void printUsage(String errorCause) {
 		System.out.println("Unable to run because of:\t" + errorCause);
 		System.out.println("\n");
 		System.out
 				.println("Required argument:\tinputfolder"
 						+ "Optional arguments:\t[{-o,--outputFolder} a_folder_path] [{-v,--verbose}]");
 		System.exit(1);
 	}
 
 	private void run() {
 		readSourceFolder();
 		compileAndWrite();
 		System.out.println("Compilation completed. Java package(s) written to "
 				+ outputFolderName);
 	}
 
 	private void compileAndWrite() {
 		compiler = new GenerateJava(inputfilenames, outputFolderName);
 		compiler.compile();
 		compiler.write();
 
 	}
 
 	private void error(String message) {
 		throw new FatalErrorException(message);
 
 	}
 
 	private void readSourceFolder() {
 		FileIO sourceFolder;
 
 		try {
 			sourceFolder = new FileIO(sourceFolderName);
 			verbose("SourceFolderName: " + sourceFolderName);
 		} catch (NullPointerException e) {
 			sourceFolder = null;
 			error("Missing sourceFolder");
 		}
 		if (sourceFolder.isDirectory()) {
 			inputfilenames = sourceFolder.getFilePaths("java");
 			verbose("Input is a directory.");
 		} else if (sourceFolder.isFile()) {
 			String[] tmp = { sourceFolder.getPath() };
 			inputfilenames = tmp;
 			verbose("Input is a file");
 		} else
 			error(String.format("Source file/directory '%s' not found.",
 					sourceFolderName));
 		verbose("Printing all [" + inputfilenames.length + "] inputfilenames.");
 		for (String filename : inputfilenames)
 			verbose("\tinputfilename: " + filename);
 	}
 
 	private void verbose(String string) {
 		if (verbose)
 			System.out.println("Verbose: " + string);
 	}
 
 	private static PTToJavaPackage parseArgsAndInstantiate(String[] args)
 			throws IllegalOptionValueException, UnknownOptionException {
 		CmdLineParser parser = new CmdLineParser();
 		CmdLineParser.Option verboseOption = parser.addBooleanOption('v',
 				"verbose");
 		CmdLineParser.Option singleFileOption = parser.addBooleanOption('s',
 				"singleFile");
 		CmdLineParser.Option outputFolderOption = parser.addStringOption('o',
 				"outputFolder");
 
 		parser.parse(args);
 
 		boolean verbose = (Boolean) parser.getOptionValue(verboseOption,
 				Boolean.FALSE);
 
 		String[] remainingArgs = parser.getRemainingArgs();
 		if (remainingArgs.length == 0) {
 			throw new FatalErrorException("missing input folder [file].");
 		}
 		String sourceFolder = new File(remainingArgs[0]).getPath();
 		String outputFolder = (String) parser.getOptionValue(
 				outputFolderOption, sourceFolder + "_output");
 		PTToJavaPackage controller = new PTToJavaPackage(sourceFolder,
 				outputFolder, verbose);
 		controller.verbose("Verbose flag turned on.");
 		return controller;
 	}
 
 	public PTToJavaPackage(String sourceFolder, String outputFolder,
 			boolean verbose) {
 		super();
 		this.sourceFolderName = sourceFolder;
 		this.outputFolderName = outputFolder;
 		this.verbose = verbose;
 	}
 }
