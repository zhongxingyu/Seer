 package cli;
 import java.io.*;
 import java.util.*;
 
 import tackbp.KbpConstants;
 import sf.SFConstants;
 import sf.SFEntity;
 import sf.SFEntity.SingleAnswer;
 import sf.eval.MistakeBreakdown;
 import sf.eval.SFScore;
 import sf.filler.Filler;
 
 import sf.filler.regex.*;
 import sf.filler.tree.*;
 
 import sf.retriever.CorefEntity;
 import sf.retriever.CorefIndex;
 import sf.retriever.CorefMention;
 import sf.retriever.CorefProvider;
 import sf.retriever.ProcessedCorpus;
 import util.FileUtil;
 
 /**
  * CSE 454 Assignment 1 main class. Java 7 required.
  * 
  * In the main method, a pipeline is run as follows: 1) Read the queries. 2) For
  * each query, retrieve relevant documents. In this assignment, only the
  * documents containing an answer will be returned to save running time. In
  * practice, a search-engine-style retriever will be used. Iterate through all
  * the sentences returned and the slot filler will applied to extract answers.
  * 3) Spit out answers and evaluate them against the labels.
  * 
  * In this assignment, you only need to write a new class for the assigned slots
  * implementing the <code>sf.filler.Filler</code> interface. An example class on
  * birthdate is implemented in <code>sf.filler.RegexPerDateOfBirthFiller.java</code>.
  * 
  * @author Xiao Ling
  */
 
 public class Extractor {
 	// TODO: actually calculate number of sentences in a corpus.
 	protected static long ESTIMATED_SENTENCE_COUNT = 27350000;
 
 	protected static String formatTime(long nanoseconds) {
 		double seconds = nanoseconds / 1000000000.;
 		boolean negative = seconds < 0;
 		if ( negative ) seconds *= -1;
 
 		int minutes = (int)(seconds / 60);
 		seconds -= minutes * 60;
 
 		int hours = minutes / 60;
 		minutes -= hours * 60;
 
 		int days = hours / 24;
 		hours -= days * 24;
 
 		return String.format("%s%d:%02d:%02d:%02.3f", negative ? "-" : "",
 			days, hours, minutes, seconds);
 	}
 
 	public static void run(Args args) throws InstantiationException, IllegalAccessException {
 		// read the queries
 		sf.query.QueryReader queryReader = new sf.query.QueryReader();
 		queryReader.readFrom( new File( args.testSet, "queries.xml" )
 			.getPath() );
 		
 		// Construct fillers
 		Filler[] fillers = new Filler[ args.fillers.size() ];
 		int i = 0;
 		for ( Class<? extends Filler> filler : args.fillers ) {
 			fillers[ i++ ] = filler.newInstance(); 
 		}
 		
 		StringBuilder answersString = new StringBuilder();
 		
 		String basePath = args.corpus.getPath();
 		
 		// initialize the corpus
 		// FIXME replace the list by a generic class with an input of slot
 		// name and an output of all the relevant files from the answer file
 		long startTime = System.nanoTime();
 		ProcessedCorpus corpus = null;
 		CorefIndex corefIndex = null;
 		try {
 			corpus = new ProcessedCorpus( basePath );
 			corefIndex = new CorefIndex( basePath );
 			
 			// Predict annotations
 			Map<String, String> annotations = null;
 			int c = 0;
 			while (corpus.hasNext()) {
 				// Get next annotation
 				annotations = corpus.next();
 				c++;
 				if ( c < args.skip ) {
 					if ( c % 100000 == 0 ) {
 						System.out.println("Skipped " + c + " sentences.");
 					}
 					continue;
 				}
 
 				// Report status
 				if (c % 1000 == 0) {
 					long elapsed = System.nanoTime() - startTime;
 					long estTime = (long)( elapsed *
							(double) ( ESTIMATED_SENTENCE_COUNT / (double) c - 1 ));
 					System.out.println("===== Read " + c + " lines in " +
 							formatTime(elapsed) + ", remaining time " +
 							formatTime(estTime));
 				}
 				
 				String[] sentenceArticle = annotations.get(
 						SFConstants.ARTICLE_IDS ).split("\t");
 				
 				// Advance index to current document ID.
 				long desiredDocId = Long.parseLong( sentenceArticle[1] );
 				corefIndex.nextDoc( desiredDocId );
 				
 				// Get a coreference provider for the current sentence.
 				long sentenceId = Long.parseLong( sentenceArticle[0] );
 				CorefProvider sentenceCoref =
 						corefIndex.getSentenceProvider( sentenceId );
 
 				// Report coreference information
 				if ( args.verbose && c % 1000 == 0 ) {
 					System.out.println("Sentence " + sentenceId + ": " +
 							annotations.get( SFConstants.TEXT ) );
 					System.out.println("Coreference mentions: " +
 							sentenceCoref.all());
 					Set<CorefEntity> entities = new HashSet<CorefEntity>();
 					for ( CorefMention mention : sentenceCoref.all() ) {
 						entities.add( mention.entity );
 					}
 					System.out.println("Coreference entities: " + entities);
 				}
 
 				// For each query and filler, attempt to fill the slot.
 				for (SFEntity query : queryReader.queryList) {
 					for ( Filler filler : fillers ) {
 						filler.predict(query, annotations, sentenceCoref);
 					}
 				}
 				
 				// Exit if we have exceeded the limit.
 				if ( args.limit > 0 && c >= args.limit )
 					break;
 			}
 			
 			// Collect answers
 			for (Filler filler : fillers) {
 				for (String slotName : filler.slotNames) {
 					// for each query, print out the answer, or NIL if nothing is found
 					for (SFEntity query : queryReader.queryList) {
 						if (query.answers.containsKey(slotName)) {
 							// The output file format
 							// Column 1: query id
 							// Column 2: the slot name
 							// Column 3: a unique run id for the submission
 							// Column 4: NIL, if the system believes no
 							// information is learnable for this slot. Or, 
 							// a single docid which supports the slot value
 							// Column 5: a slot value
 							SingleAnswer ans = query.answers.get(slotName).get(0);
 							for (SingleAnswer a : query.answers.get(slotName)) {
 								if (a.count > ans.count) // choose answer with highest count
 									ans = a;
 							}
 							answersString.append(String.format(
 									"%s\t%s\t%s\t%s\t%s\n", query.queryId,
 									slotName, "MyRun", ans.doc,
 									ans.answer));
 						} else {
 							answersString.append(String.format(
 									"%s\t%s\t%s\t%s\t%s\n", query.queryId,
 									slotName, "MyRun", "NIL", ""));
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			// TODO: handle errors more intelligently:
 			try {
 				if ( corpus != null ) corpus.close();
 				if ( corefIndex != null ) corefIndex.close();
 			} catch ( IOException e ) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		FileUtil.writeTextToFile(answersString.toString(),
 				new File( args.testSet, "annotations.pred" ).getPath() );
 	}
 	
 	public static void main(String[] argsList) throws Exception {
 		Args args = new Args(argsList, System.err);
 		System.out.println("Arguments:\n" + args);
 
 		// The slot filling pipeline
 		if (args.run)
 			run(args);
 		
 		// Evaluate against the gold standard labels
 		// The label file format (11 fields):
 		// 1. NA
 		// 2. query id
 		// 3. NA
 		// 4. slot name
 		// 5. from which doc
 		// 6., 7., 8. NA
 		// 9. answer string
 		// 10. equiv. class for the answer in different strings
 		// 11. judgement. Correct ones are labeled as 1.
 		if (args.eval) {
 			String goldAnnotationPath =
 					new File( args.testSet, "annotations.gold" ).getPath();
 			String predictedAnnotationPath =
 					new File( args.testSet, "annotations.pred" ).getPath();
 			SFScore.main(new String[] { predictedAnnotationPath,
 										goldAnnotationPath,
 										"anydoc" });
 		}
 		
 		if (args.breakdown) {
 			String goldAnnotationPath =
 					new File( args.testSet, "annotations.gold" ).getPath();
 			String predictedAnnotationPath =
 					new File( args.testSet, "annotations.pred" ).getPath();
 			String queryFilePath = 
 					new File( args.testSet, "queries.xml" ).getPath();
 			String sentMetaPath = 
 					new File( args.corpus, "sentences.meta" ).getPath();
 			String sentTextPath = 
 					new File( args.corpus, "sentences.text" ).getPath();
 			MistakeBreakdown.main(new String[] { predictedAnnotationPath,
 												 goldAnnotationPath,
 												 queryFilePath,
 												 sentMetaPath,
 												 sentTextPath });
 		}
 	}
 }
