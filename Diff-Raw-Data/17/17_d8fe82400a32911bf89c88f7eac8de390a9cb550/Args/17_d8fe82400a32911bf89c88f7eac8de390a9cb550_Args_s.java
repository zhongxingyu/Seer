 package cli;
 import java.io.File;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import sf.filler.Filler;
 import sf.filler.regex.RegexLocationFiller;
 import sf.filler.tree.TregexOrgPlaceOfHeadquartersFiller;
 import sf.filler.tree.TregexPerPlaceOfBirthFiller;
 import sf.filler.tree.TregexPerPlaceOfDeathFiller;
 import sf.filler.tree.TregexPerPlacesOfResidenceFiller;
 
 /**
  * Processes command-line arguments.
  * @author Jeffrey Booth
  */
 public class Args {
 	// Filler abbreviations for the command-line interface.
 	// NOTE: You should add your fillers here!
 	public static Map<String, Class<? extends Filler>> FILLER_ABBREVS;
 	static {
 		FILLER_ABBREVS = new HashMap<String, Class<? extends Filler>>();
 		//          Abbrev.     Class
 		FILLER_ABBREVS.put("reg",      RegexLocationFiller.class );
 		FILLER_ABBREVS.put("tr-hq",    TregexOrgPlaceOfHeadquartersFiller.class );
 		FILLER_ABBREVS.put("tr-birth", TregexPerPlaceOfBirthFiller.class );
 		FILLER_ABBREVS.put("tr-death", TregexPerPlaceOfDeathFiller.class );
 		FILLER_ABBREVS.put("tr-homes", TregexPerPlacesOfResidenceFiller.class );
 	}
 	
 	public boolean run, eval, verbose, breakdown;
 	public long limit;
 	public Set<Class<? extends Filler>> fillers;
 	private PrintStream out;
 	public File corpus;
 	public File testSet;
 	public long skip;
 	
 	public void usage() {
 		// Display initial arguments.
 		out.println("Arguments:\n" +
 	                "\n" +
 				    "run          - Run the evaluator.\n" +
 	                "eval         - Evaluate the results.\n" +
 				    "-v           - Print verbose output.\n" +
	                "-m			  - Print mistake breakdown.\n" +
 				    "-limit n     - Limit to n sentences. If n == 0, " +
 	                    "the number of sentences is not limited.\n" +
 				    "-skip n      - Skip n sentences.\n");
 		
 		// Display corpus samples.
		out.println("-corpus x    - Use data source x, which is " +
 		            "               the path to the folder containing\n" +
				    "               sentences.txt files. This path may be \n" +
 		            "               relative to the corpus-samples/ \n" +
 				    "               directory in your repository, which\n" +
 		            "               includes the following:");
 		File dir = new File( "corpus-samples" );
 		for ( File child : dir.listFiles() ) {
 			if ( !child.isDirectory() ) continue;
 			out.println( "               * " + child.getName() );
 		}
 		
 		// Display filler names.
 		for ( Map.Entry<String, Class<? extends Filler>> entry :
 			FILLER_ABBREVS.entrySet() ) {
 			out.printf( "%-12s - Use %s\n", entry.getKey(), entry.getValue() );
 		}
 		out.println("(class name) - Use the filler with the given class name.");
 		out.println("all-fillers  - Use all fillers in filterAbbrevs.");
 		
 		// Display test sets.
		out.println("-test-set x  - Use the queries and annotations in the " +
		            "               given directory x. This path may be \n" +
 		            "               relative to the test-data/ \n" +
 				    "               directory in your repository, which\n" +
 		            "               includes the following:");
 		dir = new File( "test-data" );
 		for ( File child : dir.listFiles() ) {
 			if ( !child.isDirectory() ) continue;
 			out.println( "               * " + child.getName() );
 		}
 	}
 
 	private long checkLong( String[] args, int idx, String paramTitle ) {
 		long limit;
 		try {
 			limit = Long.parseLong( args[idx] );
 			if ( limit < 0 ) {
 				throw new IllegalArgumentException(
 						"The " + paramTitle + " must be positive.");
 			}
 		} catch( NumberFormatException ex ) {
 			throw new IllegalArgumentException(
 					"The " + paramTitle + " must be an integer.");
 		} catch( ArrayIndexOutOfBoundsException ex ) {
 			throw new IllegalArgumentException(
 					"Missing the value for the " + paramTitle + ".");
 		}
 		return limit;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Args( String[] args, PrintStream out ) {
 		try {
 			this.out = out;
 			
 			fillers = new HashSet<Class<? extends Filler>>();
 			int idx = 0;
 			Class<? extends Filler> filler = null;
 			while ( idx < args.length ) {
 				String arg = args[ idx ];
 				if ( arg.equals("run") )
 					run = true;
 				else if ( arg.equals("eval") )
 					eval = true;
 				else if ( arg.equals("all-fillers") )
 					fillers.addAll( FILLER_ABBREVS.values() );
 				else if ( arg.equals("-v") )
 					verbose = true;
 				else if ( arg.equals("-m") )
 					breakdown = true;
 				else if ( arg.equals("-skip") ) {
 					idx++;
 					skip = checkLong( args, idx, "sentences to skip" );
 				} else if ( arg.equals("-limit") ) {
 					idx++;
 					limit = checkLong( args, idx, "sentence limit" );
 				}
 				
 				// Pick the corpus
 				else if ( arg.equals("-corpus") ) {
 					idx++;
 					String corpusPath = args[idx];
 					corpus = new File( new File("corpus-samples"),
 							corpusPath );
 					if ( !corpus.isDirectory() ) {
 						corpus = new File( corpusPath );
 						if ( !corpus.isDirectory() ) {
 							throw new IllegalArgumentException( "The corpus" +
 									" path " + corpusPath +
 									" must be a directory.");
 						}
 					}
 				}
 				
 				// Pick the test set
 				else if ( arg.equals("-test-set") ) {
 					idx++;
 					String testPath = args[idx];
 					testSet = new File( new File("test-data"), testPath );
 					if ( !testSet.isDirectory() ) {
 						testSet = new File( testPath );
 						if ( !testSet.isDirectory() ) {
 							throw new IllegalArgumentException( "The test path "
 									+ testPath + " must be a directory.");
 						}
 					}
 				}
 				
 				// If a class abbrev was provided, use it as a filler.
 				else if ( ( filler = FILLER_ABBREVS.get( arg ) ) != null )
 					fillers.add( filler );
 				else {
 					// If a filler's full class name was given, use it too.
 					try {
 						Class<?> cls = Class.forName( arg );
 						if ( Filler.class.isAssignableFrom( cls ) ) {
 							fillers.add( (Class<? extends Filler>)
 									     Class.forName(arg) );
 						} else {
 							throw new IllegalArgumentException(
 									"Class " + arg +
 									" does not extend Filler.");
 						}
 					} catch( ClassNotFoundException ex ) {
 						throw new IllegalArgumentException(
 								"Missing class " + arg + ".");
 					}
 				}
 				idx++;
 			}
 			
 			if ( fillers.size() == 0 ) {
 				throw new IllegalArgumentException(
 						"No fillers specified.");
 			}
 			
 			if ( corpus == null ) {
 				throw new IllegalArgumentException(
 						"No corpus sample specified.");
 			}
 			
 			if ( testSet == null ) {
 				throw new IllegalArgumentException(
 						"A test set (with queries and annotations) is required.");
 			}
 		} catch( IllegalArgumentException ex ) {
 			out.println( ex.getMessage() + "\n" );
 			usage();
 			throw ex;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "run: "       + run       + "\n"
 			 + "eval: "      + eval      + "\n"
 			 + "skip: "      + skip      + "\n"
 			 + "limit: "     + limit     + "\n"
 			 + "fillers: "   + fillers   + "\n"
 			 + "corpus: "    + corpus    + "\n"
 			 + "test-set: "  + testSet   + "\n"
 			 + "breakdown: " + breakdown + "\n";
 	}
 }
