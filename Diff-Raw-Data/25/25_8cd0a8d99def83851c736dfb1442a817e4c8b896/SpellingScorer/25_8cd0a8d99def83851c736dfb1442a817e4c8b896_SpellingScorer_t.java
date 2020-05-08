 package cs276.pe1.spell;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import cs276.util.IOUtils;
 
 /**
  * Scorer for evaluating a SpellingCorrector.  Reads a file
  * that contains correctly spelled words and, for each, one or
  * more mis-spellings.  A SpellingCorrector class is then
  * instantiated (the specific type is passed as an argument
  * and it is assumed to have a no-arg constructor).
  * 
  * @author dramage
  */
 public class SpellingScorer {
 
 	/**
 	 * Prints usage information, a status message, and an optional exit 
 	 * reason before exiting with status -1.
 	 */
 	public static void usage(String message, Throwable cause) {
 		System.err.println("CS276 PE1 Scoring Script");
 		System.err.println();
 		System.err.println("Usage: ");
 		System.err.println("  (java-cmd) SpellingScorer cs276.pe1.spell.YourSpellingCorrectorClass");
 		System.err.println();
 		System.err.println(message);
 		if (cause != null) {
 			cause.printStackTrace();
 		}
 		System.exit(-1);
 	}
 	
 	/**
 	 * Default usage information with no extra reasons provided.
 	 */
 	public static void usage() {
 		usage("",null);
 	}
 	
 	public static void main(String[] argv) {
 		if (argv.length < 1 || argv.length > 2) {
 			usage();
 		}
 		
 		// instantiate scorer
 		SpellingCorrector scorer = null;
 		try {
 			Class<?> type = Class.forName(argv[0]);
 			scorer = (SpellingCorrector)type.newInstance();
 		} catch (Exception e) {
 			usage("Error while instantiating corrector " + argv[0]
 					+ ": does it have a public no-args constructor?", e);
 		}
 		
 		// load words file
 		File wordsFile = new File(argv.length == 2 ? argv[1]
 				: "/afs/ir/class/cs276/pe1-2011/spelltest.txt");
 		if (!wordsFile.exists() || !wordsFile.canRead()) {
 			usage();
 		}
 		Map<String,List<String>> words = new LinkedHashMap<String,List<String>>();
 		try {
 			for (String line : IOUtils.readLines(IOUtils.openFile(wordsFile))) {
 				List<String> split = Arrays.asList(line.split("\\s+"));
 				words.put(split.get(0), split.subList(1, split.size()));
 				}
 		} catch (Exception e) {
 			usage("Error while loading words file from " + wordsFile, e);
 		}
 		
 		// score words
 		final long startTime = System.currentTimeMillis();
 		int nCorrect = 0;
 		int nTotal = 0;
 		for (Map.Entry<String, List<String>> entry : words.entrySet()) {
 			final String word = entry.getKey();
 			for (String misspelling : entry.getValue()) {
 				List<String> corrections = scorer.corrections(misspelling);
 				if (!corrections.isEmpty() && corrections.get(0).equals(word)) {
 					nCorrect += 1;
 				}
                else
                {
                    //System.out.println(misspelling+" "+corrections);
                }
 				nTotal += 1;
 			}
 		}
 		
 		System.out.println(argv[0] + ": " + nCorrect + "/" + nTotal + " in "
 				+ (System.currentTimeMillis() - startTime + "ms"));
 	}
 	
 }
