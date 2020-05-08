 /*
  * Copyright (c) 2011, Sussex University.
  * All rights reserved.
  */
 package uk.ac.susx.mlcl.parser;
 
 
 import com.beust.jcommander.Parameter;
 import uk.ac.susx.mlcl.featureextraction.*;
 import uk.ac.susx.mlcl.featureextraction.annotations.Annotations;
 import uk.ac.susx.mlcl.featureextraction.featureconstraint.*;
 import uk.ac.susx.mlcl.featureextraction.featurefactory.FeatureFactory;
 import uk.ac.susx.mlcl.featureextraction.featurefunction.*;
 import uk.ac.susx.mlcl.lib.MiscUtil;
 import uk.ac.susx.mlcl.lib.io.Files;
 import uk.ac.susx.mlcl.strings.NewlineReader;
 import uk.ac.susx.mlcl.strings.StringSplitter;
 import uk.ac.susx.mlcl.util.Config;
 import uk.ac.susx.mlcl.util.Configurable;
 import uk.ac.susx.mlcl.util.IntSpan;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static edu.stanford.nlp.util.StringUtils.join;
 
 /**
  * Change Log 07-03-2012
  * ==============================
  * * Added command line usage descriptions and "--help" option.
  * * Added ability to have key constraints in the configuration options
  *
  * @author Simon Wibberley
  */
 public abstract class AbstractParser implements Configurable {
 
 	private static final Logger LOG =
 	Logger.getLogger(AbstractParser.class.getName());
 
 	protected abstract static class AbstractParserConfig extends Config {
 
 		private static final long serialVersionUID = 1L;
 
 		@Parameter(names = {"-cb", "--useChunkAsBase"},
 		description = "produce phrases as base-entries")
 		private boolean useChunkAsBase = false;
 
 		@Parameter(names = {"-tb", "--useTokenAsBase"},
 		description = "produce tokens as base-entries")
 		private boolean useTokenAsBase = false;
 
 		@Parameter(names = {"-cf", "--useChunkAsFeature"},
 		description = "produce co-occurent phrases as features")
 		private boolean useChunkAsFeature = false;
 
 		@Parameter(names = {"-tf", "--useTokenAsFeature"},
 		description = "produce co-occurent tokens as features")
 		private boolean useTokenAsFeature = false;
 
 		@Parameter(names = {"-es", "--entrySeparator"},
 		description = "base term / feature delimiter for the thesaurus output.")
 		private String entrySeparator = "\t";
 
 		@Parameter(names = {"-op", "--outPath"},
 		required = true)
 		private String outPath;
 
 		@Parameter(names = {"-ip", "--inPath"},
 		required = true)
 		private String inPath;
 
 		@Parameter(names = {"-r", "--recursive"},
 		description = "Descend into subfolder of the input path.")
 		private boolean recursive = false;
 
 		@Parameter(names = {"-is", "--inSuffix"},
 		description = "Only read files with the given suffix.",
 		required = true)
 		private String inSuffix;
 
 		@Parameter(names = {"-z", "--gzip"})
 		private boolean useGzip = false;
 
 		@Parameter(names = {"-of", "--outputFormatter"},
 		description = "class that specifies the output format.",
 		converter = FormatterConverter.class)
 		private OutputFormatter outputFormatter = new TabOutputFormatter();
 
 		@Parameter(names = {"-v", "--verbose"})
 		private boolean verbose = false;
 
 		@Parameter(names = {"-ep", "--posTagToExclude"},
 		description = "Excludes from the output all entry tokens with a given PoS tag")
 		private String exPoS = null;
 
 		@Parameter(names = {"-l", "--limit"}, description = "Maximum number of documents (not files) to process.")
 		private int limit = 0;
 
 		@Parameter(names = {"-nl", "--useNounsOnLeft"},
 		description = "Use nouns on left of another noun as features")
 		private int useNounsOnLeft = 0;
 
 		@Parameter(names = {"-nr", "--useNounsOnRight"},
 		description = "Use nouns on left of another noun as features")
 		private int useNounsOnRight = 0;
 
 		@Parameter(names = {"-al", "--useAdjectivesLeft"},
 		description = "Use Adjective on left as a feature")
 		private int useAdjectiveLeft = 0;
 
 		@Parameter(names = {"-ar", "--useAdjectivesRight"},
 		description = "Use Adjective on right as a feature")
 		private int useAdjectiveRight = 0;
 
 		@Parameter(names = {"-pr", "--usePrepositionOnRight"},
 		description = "Use a preposition on the right as a feature if it is immediately to the right")
 		private int usePrepositionOnRight = 0;
 
 		@Parameter(names = {"-pl", "--usePrepositionOnLeft"},
 		description = "Use a preposition on the left as a feature if it is immediately to the right")
 		private int usePrepositionOnLeft = 0;
 
 		@Parameter(names = {"-posl", "--usePoSLeft"},
 		description = "Use PoS tag on left as feature")
 		private int usePoSTagLeft = 0;
 
 		@Parameter(names = {"-posr", "--usePoSRight"},
 		description = "Use PoS tag on the right as feature")
 		private int usePoSTagRight = 0;
 
 		@Parameter(names = {"-rt", "--parseRawText"},
 		description = "Parse raw text for PoS tagging, Sentence splitting etc...")
 		private boolean rawText = false;
 
 		@Parameter(names = {"-posKy", "--posKeyConstraint"},
 		description = "specify a pos tag as a entry constraint")
 		private String posCon = null;
 
 
 		@Parameter(names = {"-chnkKy", "--chunkKeyConsraint"},
 		description = "specify a chunk tag as a entry constraint")
 		private String chnkCon = null;
 
 		@Parameter(names = {"-cw", "--contextWindow"},
 		converter = ContextWindowStringConverter.class,
 		description = "in the form \"-LEFT+RIGHT\" for LEFT tokens to the left and RIGHT tokens to the right")
 		private IntSpan contextWindow = new IntSpan(-5, 5);
 
 
 		@Parameter(names = {"-lce", "--useLowercaseEntries"},
 		description = "convert all entries and their token features to lower-case")
 		private boolean useLowercaseEntries = false;
 
 		public boolean isUseLowercaseEntries() {
 			return useLowercaseEntries;
 		}
 
 		public String getEntrySeparator() {
 			return entrySeparator;
 		}
 
 		public String getOutPath() {
 			return outPath;
 		}
 
 		public String getInPath() {
 			return inPath;
 		}
 
 		public boolean isRecursive() {
 			return recursive;
 		}
 
 		public String getInSuffix() {
 			return inSuffix;
 		}
 
 		public boolean isUseGzip() {
 			return useGzip;
 		}
 
 		public OutputFormatter getOutputFormatter() {
 			return outputFormatter;
 		}
 
 		public boolean isVerbose() {
 			return verbose;
 		}
 
 		public int getLimit() {
 			return limit;
 		}
 
 
 		public boolean isUseChunkAsBase() {
 			return useChunkAsBase;
 		}
 
 		public boolean isUseTokenAsBase() {
 			return useTokenAsBase;
 		}
 
 		public boolean isUseChunkAsFeature() {
 			return useChunkAsFeature;
 		}
 
 		public boolean isUseTokenAsFeature() {
 			return useTokenAsFeature;
 		}
 
 		public int getNounsLeft() {
 			return useNounsOnLeft;
 		}
 
 		public int getNounsRight() {
 			return useNounsOnRight;
 		}
 
 		public int getAdjectiveLeft() {
 			return useAdjectiveLeft;
 		}
 
 		public int getAdjectiveRight() {
 			return useAdjectiveRight;
 		}
 
 		public int getPoSLeft() {
 			return usePoSTagLeft;
 		}
 
 		public int getPoSRight() {
 			return usePoSTagRight;
 		}
 
 		public int getPrepositionLeft() {
 			return usePrepositionOnLeft;
 		}
 
 		public int getPrepositionRight() {
 			return usePrepositionOnRight;
 		}
 
 		public String getExPoSCon() {
 			return exPoS;
 		}
 
 		public IntSpan getContextWindow() {
 			return contextWindow;
 		}
 
 		public String getPoSConstraint() {
 			return posCon;
 		}
 
 		public String getChunkConstraint() {
 			return chnkCon;
 		}
 
 		public void setContextWindow(IntSpan contextWindow) {
 			this.contextWindow = contextWindow;
 		}
 
 		public boolean isUseNounsOnRight() {
 			return useNounsOnRight != 0;
 		}
 
 		public boolean isUseNounsOnLeft() {
 			return useNounsOnLeft != 0;
 		}
 
 		public boolean isUseAdjectiveLeft() {
 			return useAdjectiveLeft != 0;
 		}
 
 		public boolean isUseAdjectiveRight() {
 			return useAdjectiveRight != 0;
 		}
 
 		public boolean isUsePoSLeft() {
 			return usePoSTagLeft != 0;
 		}
 
 		public boolean isUsePoSRight() {
 			return usePoSTagRight != 0;
 		}
 
 		public boolean isUsePrepositionLeft() {
 			return usePrepositionOnLeft != 0;
 		}
 
 		public boolean isUsePrepositionRight() {
 			return usePrepositionOnRight != 0;
 		}
 
 		public boolean isRawText() {
 			return rawText;
 		}
 	}
 
 	private BufferedWriter outFile;
 
 	private StringSplitter splitter;
 
 	private final AtomicInteger count = new AtomicInteger();
 
 	private FeatureFactory featureFactory;
 
 	private ExecutorService exec;
 
 	private Semaphore throttle;
 
 	private Queue<Future<Void>> futures;
 
 	private List<FeatureConstraint> constraints;
 
 
 	private static final String TERM_FEAT_PREFIX = "T:";
 
 	private static final String CHUNK_FEAT_PREFIX = "C:";
 
 	// NEW ADDITIONS ************PREFIXES*********************************
 
 	private static final String NOUN_LEFT_PREFIX = "NL:";
 
 	private static final String NOUN_RIGHT_PREFIX = "NR:";
 
 	private static final String VERB_LEFT_PREFIX = "VHL:";
 
 	private static final String VERB_RIGHT_PREFIX = "VHR:";
 
 	private static final String PREPOS_LEFT_PREFIX = "PL:";
 
 	private static final String PREPOS_RIGHT_PREFIX = "PR:";
 
 	private static final String ADJEC_RIGHT_PREFIX = "AR:";
 
 	private static final String ADJEC_LEFT_PREFIX = "AL:";
 
 	private static final String POS_LEFT_PREFIX = "POSL:";
 
 	private static final String POS_RIGHT_PREFIX = "POSR:";
 
 	private static final String HEAD_NOUN_PREFIX = "HN:";
 
 	private static final String ONTOLOGY_PREFIX_LEFT = "OML:";
 
 	private static final String ONTOLOGY_PREFIX_RIGHT = "OMR:";
 
 	private static final String DET_LEFT_PREFIX = "DL:";
 
 	private static final String NOUN_GROUP_NOUN_PREFIX = "NGN:";
 
 	public StringSplitter getSplitter() {
 		return splitter;
 	}
 
 	public List<FeatureConstraint> getConstraints() {
 		return constraints;
 	}
 
 	public void setSplitter(StringSplitter splitter) {
 		this.splitter = splitter;
 	}
 
 	protected String getOutPath() {
 
 		String outPath = config().getOutPath();
 
 		outPath += (config().isUseTokenAsFeature()) ? "-tf" : "";
 
 		outPath += (config().isUseChunkAsFeature()) ? "-cf" : "";
 
 		outPath += (config().isUseTokenAsBase()) ? "-tb" : "";
 
 		outPath += (config().isUseChunkAsBase()) ? "-cb" : "";
 
 		outPath += (config().isUsePrepositionRight()) ? "-pr" : "";
 
 		outPath += (config().isUsePrepositionLeft()) ? "-pl" : "";
 
 		outPath += (config().isUsePoSRight()) ? "-posr" : "";
 
 		outPath += (config().isUsePoSLeft()) ? "-posl" : "";
 
 		outPath += (config().isUseNounsOnRight()) ? "-nr" : "";
 
 		outPath += (config().isUseNounsOnLeft()) ? "-nl" : "";
 
 		outPath += (config().isUseAdjectiveRight()) ? "-ar" : "";
 
 		outPath += (config().isUseAdjectiveLeft()) ? "-al" : "";
 
 		outPath += (config().isRawText()) ? "-rt" : "";
 
 		outPath += (config().getPoSConstraint() == null) ? "" : "-posKy-" + config().getPoSConstraint();
 
 		outPath += (config().getChunkConstraint() == null) ? "" : "-chnkKy-" + config().getChunkConstraint();
 
 		return outPath;
 	}
 
 	/*
 		 * Used when creating the output file path to prevent already existing
 		 * files from being overwritten.
 		 */
 	protected String enumeratePath(String outPath, int enu) {
 		String path = "";
 		if (enu > 0) {
 			path = outPath + "-(" + enu + ")";
 		} else {
 			path = outPath;
 		}
 		if (new File(path).exists()) {
 			enu++;
 			outPath = enumeratePath(outPath, enu);
 		} else {
 			if (enu > 0) {
 				outPath = outPath + "-(" + enu + ")";
 			}
 		}
 		return outPath;
 	}
 
 	public void parse() {
 
 		System.err.println("Building file list...");
 
 		List<String> files = Files.getFileList(
 		config().getInPath(), config().getInSuffix(),
 		false, false);
 
 		for (String file : files) {
 			System.err.println(file);
 		}
 
 		System.err.println("File list built.");
 
 		constraints = new ArrayList<FeatureConstraint>();
 		setKeyConstraints();
 
 		featureFactory = buildFeatureFactory();
 
 		parse(config().getInPath(), files);
 
 	}
 
 	public FeatureFactory getFeatureFactory() {
 		return featureFactory;
 	}
 
 	private void parse(String prefix, List<String> folders) {
 		handleOutputPre();
 
 		initThreads();
 
 		for (String folderName : folders) {
 			final File folder = new File(folderName);
 			final List<String> files;
 			if (folder.isDirectory()) {
 				files = Files.getFileList(
 				folderName, config().getInSuffix(),
 				false, config().isRecursive());
 			} else {
 				files = new ArrayList<String>();
 				files.add(folderName);
 			}
 			for (String fileName : files) {
 
 
 				if (config().getLimit() > 0 && count.get() > config().getLimit()) {
 					break;
 				}
 
 				File file = new File(fileName);
 				System.err.println("Processing file: " + file);
 				try {
 					CharSequence text = Files.getText(prefix + File.separator + file, false, true);
 					parse(text, false);
 				} catch (IOException e) {
 					LOG.log(Level.SEVERE, null, e);
 				}
 
 				try {
 					while (!futures.isEmpty()) {
 						futures.poll().get();
 					}
 				} catch (InterruptedException e) {
 					LOG.log(Level.SEVERE, null, e);
 				} catch (ExecutionException e) {
 					LOG.log(Level.SEVERE, null, e);
 				}
 			}
 		}
 		cleanupThreads();
 
 		handleOutputPost();
 	}
 
 	protected void parse(final CharSequence input, final boolean prePost) {
 		if (prePost) {
 			handleOutputPre();
 		}
 
 		//final List<String> entries = getSplitter().split(input.toString());
 		final NewlineReader reader = new NewlineReader(input, newLineDelim());
 		//for (final String entry : entries) {
 		while (reader.hasLine()) {
 			final String entry = reader.readLine();
 			try {
 				throttle.acquire();
 			} catch (InterruptedException e) {
 				LOG.log(Level.SEVERE, null, e);
 			}
 
 			if (config().getLimit() > 0 && count.get() > config().getLimit()) {
 				throttle.release();
 				break;
 			}
 
 			final int entryid = count.addAndGet(1);
 			if (entryid % 100 == 0) {
 				System.err.println("Queing entry " + entryid);
 				System.err.println(MiscUtil.memoryInfoString());
 			}
 
 			futures.offer(exec.submit(new Callable<Void>() {
 
 				@Override
 				public final Void call() throws IOException {
 					try {
 						Map<Object, Object> preprocessedEntry = null;
 						if (config().isRawText()) {
 							try {
 								preprocessedEntry = rawTextParse(entry);
 							} catch (ModelNotValidException e) {
 								e.printStackTrace();
 							}
 						}
 						final CharSequence output = handleEntry(preprocessedEntry);
 						handleOutput(output);
					} catch (InvalidEntryException e) {
						// just ignore singleton or empty sentences
					} finally {
 						throttle.release();
 					}
 					return null;
 
 				}
 
 			}));
 		}
 
 		if (prePost) {
 			handleOutputPost();
 		}
 	}
 
 	private void initThreads() {
 		if (exec != null || throttle != null || futures != null) {
 			throw new IllegalStateException();
 		}
 		exec = Executors.newFixedThreadPool(config().getNumCores());
 		throttle = new Semaphore(3 * config().getNumCores());
 		futures = new ArrayDeque<Future<Void>>();
 	}
 
 	private void cleanupThreads() {
 		exec.shutdown();
 
 		try {
 			exec.awaitTermination(1, TimeUnit.DAYS);
 		} catch (InterruptedException e) {
 			LOG.log(Level.SEVERE, null, e);
 			exec.shutdownNow();
 		} catch (Exception e) {
 			e.printStackTrace();
 			LOG.log(Level.SEVERE, null, e);
 		}
 	}
 
 	private void handleOutputPre() {
 		String outPath = getOutPath();
 		String[] tokens = outPath.split(File.separator);
 		String[] subArray = Arrays.copyOfRange(tokens, 0, tokens.length - 1);
 		String outDir = join(subArray, File.separator);
 
 		File outDirFile = new File(outDir);
 		if (!outDirFile.exists()) {
 			LOG.warning("Output file does not exist, creating...");
 			outDirFile.mkdirs();
 		}
 		if (!outPath.endsWith(".txt"))
 			outPath += ".txt";
 		try {
 			outFile = new BufferedWriter(new FileWriter(outPath));
 		} catch (IOException e) {
 			System.err.println(e);
 		}
 	}
 
 	private void handleOutputPost() {
 
 		try {
 			outFile.close();
 		} catch (IOException e) {
 			System.err.println(e);
 		}
 
 	}
 
 	private void handleOutput(final CharSequence output) throws IOException {
 		if (output.length() == 0) {
 			return;
 		}
 		synchronized (outFile) {
 			outFile.append(output);
 		}
 	}
 
 //	/**
 //	 * Process one entry. An Entry is the scope of the context.
 //	 *
 //	 * @param entry
 //	 * @return
 //	 */
 //	private CharSequence handleEntry(String entry, Object preprocessor) {
 //
 //		List<Sentence> annotated = annotate(entry, preprocessor);
 //
 //		if (annotated.isEmpty()) {
 //			throw new InvalidEntryException("empty sentence!");
 //		} else if (annotated.size() <= 1) {
 //			throw new InvalidEntryException("single entry sentence!");
 //		}
 //
 //		CharSequence lines = getLines(annotated);
 //
 //		return lines;
 //	}
 
 	private CharSequence handleEntry(Map<Object, Object> map) {
 		List<Sentence> annotated = annotate(map);
 
 		if (annotated.isEmpty()) {
 			throw new InvalidEntryException("empty sentence!");
		} else if (annotated.size() <= 1) {
			throw new InvalidEntryException("single entry sentence!");
 		}
 
 		CharSequence lines = getLines(annotated);
 
 		return lines;
 	}
 
 	protected CharSequence getLines(List<Sentence> sentences) {
 
 		StringBuilder out = new StringBuilder();
 
 		for (Sentence sentence : sentences) {
 			for (IndexToken<?> t : sentence.getKeys()) {
 				CharSequence output = "";
 				if (t.getKey() != null) {
 					output = config().getOutputFormatter().getOutput(t);
 				}
 
 				out.append(output);
 			}
 		}
 		return out;
 	}
 
 
 	public void applyFeatureFactory(FeatureFactory featureFactory, Sentence s) {
 
 		Collection<FeatureFunction> fns = featureFactory.getAllFeatures();
 		for (IndexToken<?> key : s.getKeys()) {
 			boolean accept = true;
 			for (FeatureConstraint constraint : constraints) {
 				if (!constraint.accept(s, key, key.getSpan().left)) {
 					accept = false;
 					break;
 				}
 			}
 			if (accept) {
 				CharSequence keyStr = s.getKeyString(config().getExPoSCon(), key);
 				List<CharSequence> featureList = new ArrayList<CharSequence>();
 				key.setKey(keyStr);
 
 				for (FeatureFunction f : fns) {
 					Collection<String> features = f.extractFeatures(s, key);
 					featureList.addAll(features);
 				}
 
 				key.setFeatures(featureList);
 			}
 		}
 	}
 
 	public class ModelNotValidException extends Exception {
 
 		public ModelNotValidException() {
 		}
 
 		public ModelNotValidException(String msg) {
 			super(msg);
 		}
 	}
 
 	protected abstract List<Sentence> annotate(Map<Object, Object> map);
 
 	protected void setKeyConstraints() {
 
 		if (config().getPoSConstraint() != null) {
 			getConstraints().add(new PoSKeyConstraint(config().getPoSConstraint()));
 		}
 
 		if (config().getChunkConstraint() != null) {
 			getConstraints().add(new ChunkTagKeyConstraint(config().getChunkConstraint()));
 		}
 	}
 
 	protected FeatureFactory buildFeatureFactory() {
 		FeatureFactory featureFactory = new FeatureFactory();
 
 		if (config().isUseTokenAsFeature()) {
 
 			TokenFeatureFunction fn = new TokenFeatureFunction();
 			fn.addConstraint(new ContextWindowsFeatureConstraint(config().getContextWindow()));
 			fn.addConstraint(new DisjointFeatureConstraint());
 			fn.setPrefix(TERM_FEAT_PREFIX);
 
 			featureFactory.addFeature("tokenFeature", fn);
 		}
 
 		if (config().isUseChunkAsFeature()) {
 			ChunkFeatureFunction fn = new ChunkFeatureFunction();
 			fn.addConstraint(new ContextWindowsFeatureConstraint(config().getContextWindow()));
 			if (config().isUseTokenAsFeature()) {
 				fn.addConstraint(new MinChunkLengthFeatureConstraint(2));
 			}
 
 			fn.addConstraint(new TheNonIntersectingChunkTokenThingFeatureConstraintTingDem());
 			fn.setPrefix(CHUNK_FEAT_PREFIX);
 
 			featureFactory.addFeature("chunkFeature", fn);
 		}
 
 		// NEW ADDITIONS ***********FEATURE*FUNCTIONS********************
 
 		if (config().isUseNounsOnLeft()) {
 			GrammarFeatureFunction fn = new GrammarFeatureFunction("NN", -config().getNounsLeft(), Annotations.LeftNounAnnotation.class, true);
 			fn.setPrefix(NOUN_LEFT_PREFIX);
 
 			featureFactory.addFeature("leftNounFeature", fn);
 		}
 
 		if (config().isUseNounsOnRight()) {
 			//NounToRightFeatureFunction fn = new NounToRightFeatureFunction("NN");
 			GrammarFeatureFunction fn = new GrammarFeatureFunction("NN", config().getNounsRight(), Annotations.RightNounAnnotation.class, true);
 			fn.setPrefix(NOUN_RIGHT_PREFIX);
 
 			featureFactory.addFeature("rightNounFeature", fn);
 		}
 
 		if (config().isUsePrepositionLeft()) {
 			GrammarFeatureFunction fn = new GrammarFeatureFunction("PREP", -config().getPrepositionLeft(), Annotations.LeftPrepositionAnnotation.class, false);
 			fn.setPrefix(PREPOS_LEFT_PREFIX);
 
 			featureFactory.addFeature("leftPrepositionFeature", fn);
 		}
 
 		if (config().isUsePrepositionRight()) {
 			GrammarFeatureFunction fn = new GrammarFeatureFunction("PREP", config().getPrepositionRight(), Annotations.RightPrepositionAnnotation.class, false);
 			fn.setPrefix(PREPOS_RIGHT_PREFIX);
 
 			featureFactory.addFeature("rightPrepositionFeature", fn);
 		}
 
 		if (config().isUseAdjectiveRight()) {
 			GrammarFeatureFunction fn = new GrammarFeatureFunction("JJ", config().getAdjectiveRight(), Annotations.RightAdjectiveAnnotation.class, true);
 			fn.setPrefix(ADJEC_RIGHT_PREFIX);
 
 			featureFactory.addFeature("rightAdjectiveFeature", fn);
 		}
 
 		if (config().isUseAdjectiveLeft()) {
 			GrammarFeatureFunction fn = new GrammarFeatureFunction("JJ", -config().getAdjectiveLeft(), Annotations.LeftAdjectiveAnnotation.class, true);
 			fn.setPrefix(ADJEC_LEFT_PREFIX);
 
 			featureFactory.addFeature("leftAdjectiveFeature", fn);
 		}
 
 		if (config().isUsePoSLeft()) {
 			PoSTagLeftFeatureFunction fn = new PoSTagLeftFeatureFunction("NN");
 			fn.setPrefix(POS_LEFT_PREFIX);
 
 			featureFactory.addFeature("posTagLeftFeature", fn);
 		}
 
 		if (config().isUsePoSRight()) {
 			PoSTagRightFeatureFunction fn = new PoSTagRightFeatureFunction("NN");
 			fn.setPrefix(POS_RIGHT_PREFIX);
 
 			featureFactory.addFeature("posTagRightFeature", fn);
 		}
 
 		return featureFactory;
 	}
 
 	protected abstract AbstractParserConfig config();
 
 	protected abstract String newLineDelim();
 
 	/**
 	 * Return the original text and the information the preprocessor object attached to it. Each subclasses can map
 	 * objects of different types--- see the implementing class' method
 	 * @param text
 	 * @return
 	 * @throws ModelNotValidException
 	 */
 	protected abstract Map<Object, Object> rawTextParse(CharSequence text) throws ModelNotValidException;
 
 }
