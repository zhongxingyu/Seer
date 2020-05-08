 package src.symmreducer.testing;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.StringTokenizer;
 
 import src.TopSpin;
 import src.etch.testing.EtchTestCase;
 import src.etch.testing.EtchTestOutcome;
 import src.promela.lexer.LexerException;
 import src.promela.parser.ParserException;
 import src.symmextractor.testing.SymmExtractorFailTestOutcome;
 import src.symmextractor.testing.SymmExtractorRunTestOutcome;
 import src.symmextractor.testing.SymmExtractorTestCase;
 import src.symmreducer.SymmReducer;
 import src.testing.SystemErrorTestOutcome;
 import src.utilities.AbsentConfigurationFileException;
 import src.utilities.BadConfigurationFileException;
 import src.utilities.Config;
 import src.utilities.ErrorStreamHandler;
 
 public class SymmReducerTestCase extends SymmExtractorTestCase {
 
 	private int searchDepth;
 	private String compilerDirectives;
 
 	public SymmReducerTestCase(String foldername, String modelFilename, SymmReducerTestOutcome expectedOutcome, String compilerDirectives, int searchDepth) {
 		super(foldername, modelFilename, expectedOutcome);
 		this.compilerDirectives = compilerDirectives;
 		this.searchDepth = searchDepth;
 	}
 	
 	public SymmReducerTestCase(String foldername, String modelFilename, SymmReducerTestOutcome expectedOutcome, int searchDepth) {
 		this(foldername, modelFilename, expectedOutcome, "", searchDepth);
 	}
 
 	public SymmReducerTestCase(String foldername, String modelFilename, SymmReducerTestOutcome expectedOutcome) {
 		this(foldername, modelFilename, expectedOutcome, 10000);
 	}
 	
 	public SymmReducerTestCase(String foldername, String modelFilename, SymmReducerTestOutcome expectedOutcome, String compilerDirectives) {
 		this(foldername, modelFilename, expectedOutcome, compilerDirectives, 10000);
 	}
 
 	@Override
 	public void run() {
 
 		try {
 
 
 			EtchTestCase etchTest = new EtchTestCase(filename, EtchTestOutcome.WellTyped);
 			
 			etchTest.run();
 
 			
 			if(!etchTest.isPass()) {
 				actualOutcome = etchTest.getOutcome();
 			} else {
 			
 				Config.readConfigFile("symmextractor_common_config.txt");
 
 				Config.readConfigFile(foldername + "config.txt");
 
 				SymmReducer reducer = new SymmReducer(filename);
 
 				TopSpin.doAutomaticSymmetryReduction(reducer);
 
 				if(!reducer.isWellTyped()) {
 					actualOutcome = SymmExtractorFailTestOutcome.BreaksRestrictions;
 				} else if(!reducer.obeysSymmetryRestrictions) {
 					actualOutcome = SymmExtractorFailTestOutcome.BreaksRestrictions;
 				} else {
 
 					try {
 						if(compileSympanFiles() != 0) {
 							actualOutcome = SymmReducerFailTestOutcome.GCCCompilationFailure;
 							return;
 						}
 
 						ModelCheckingResult result;
 
 						try {
 							result = runVerifier();
 							
 							if(result.getExitValue()!=0)
 							{
 								actualOutcome = SymmReducerFailTestOutcome.VerificationFailure;
 							} else {
 								actualOutcome = new SymmReducerTestOutcome(
 										new SymmExtractorRunTestOutcome(true, reducer.getSizeOfGroup(), reducer.requiredCosetSearch()), 
 										result.getNumberOfStates(), result.getNumberOfTransitions());
 							}
 						} catch(IOException e) {
 							actualOutcome = SymmReducerFailTestOutcome.VerificationIOError;
 						} catch(InterruptedException e) {
 							actualOutcome = SymmReducerFailTestOutcome.VerificationInterrupted;
 						} catch(NumberFormatException e) {
 							actualOutcome = SymmReducerFailTestOutcome.ErrorParsingVerificationResult;
 						}
 
 					} catch(IOException e) {
 						actualOutcome = SymmReducerFailTestOutcome.GCCCompilationIOError;
 					} catch(InterruptedException e) {
 						actualOutcome = SymmReducerFailTestOutcome.GCCCompilationInterrupted;
 					}
 				}
 			}
 
 		} catch(IOException e) {
 			
 			actualOutcome = SystemErrorTestOutcome.IOError;
 			
 		} catch (InterruptedException e) {
 
 			actualOutcome = SystemErrorTestOutcome.InterruptedError;
 
 		} catch (BadConfigurationFileException e) {
 
 			actualOutcome = SystemErrorTestOutcome.BadConfigurationFile;
 
 		} catch (AbsentConfigurationFileException e) {
 
 			actualOutcome = SystemErrorTestOutcome.NoConfigurationFileFound;
 
 		} catch (ParserException e) {
 
 			actualOutcome = EtchTestOutcome.ParserError;
 
 		} catch (LexerException e) {
 
 			actualOutcome = EtchTestOutcome.ParserError;
 
 		}
 		
 	}
 
 	private static final String EXECUTABLE = "__topspinmain__";
 	
 	private ModelCheckingResult runVerifier() throws IOException, InterruptedException, NumberFormatException {
 
		Process sympan = Runtime.getRuntime().exec(EXECUTABLE + " -m" + searchDepth);
 		
 		// Had to put this line in to stop the program from hanging.  Not sure why.
 		BufferedReader br = new BufferedReader(new InputStreamReader(sympan.getInputStream()));
 
 		int numberOfStates = 0;
 		int numberOfTransitions = 0;
 		
 		String line;
 
 		try
 		{
 			while((line = br.readLine())!=null) {
 				if(line.contains(" states, stored")) {
 					numberOfStates = Integer.parseInt(new StringTokenizer(line, " ").nextToken());
 				} else if(line.contains(" transitions (= stored+matched)")) {
 					numberOfTransitions = Integer.parseInt(new StringTokenizer(line, " ").nextToken());
 				}
 			
 			}
 		} catch(NumberFormatException e) {
 			sympan.destroy();
 			throw e;
 		}
 
 		sympan.waitFor();
 		return new ModelCheckingResult(sympan.exitValue(), numberOfStates, numberOfTransitions);
 	}
 
 	private int compileSympanFiles() throws IOException, InterruptedException {
 		
 		Process gcc = Runtime.getRuntime().exec("gcc -O2 -o " + EXECUTABLE + " sympan.c group.c " +
 				(Config.PARALLELISE ? "parallel_symmetry_pthreads.c " : "")
 				+ "-DSAFETY -DNOREDUCE " + compilerDirectives);
 
 		ErrorStreamHandler errorHandler = new ErrorStreamHandler(gcc, false);
 		errorHandler.start();
 		
 		gcc.waitFor();
 		
 		return gcc.exitValue();
 
 	}
 
 }
