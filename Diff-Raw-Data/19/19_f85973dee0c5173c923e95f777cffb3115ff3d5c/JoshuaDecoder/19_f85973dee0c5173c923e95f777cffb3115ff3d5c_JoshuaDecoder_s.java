 package joshua.decoder;
 
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import joshua.corpus.Vocabulary;
 import joshua.decoder.ff.FeatureVector;
 import joshua.decoder.ff.FeatureFunction;
 import joshua.decoder.ff.ArityPhrasePenaltyFF;
 import joshua.decoder.ff.OOVFF;
 import joshua.decoder.ff.PhraseModelFF;
 import joshua.decoder.ff.SourcePathFF;
 import joshua.decoder.ff.WordPenaltyFF;
 import joshua.decoder.ff.lm.LanguageModelFF;
 import joshua.decoder.ff.lm.NGramLanguageModel;
 import joshua.decoder.ff.lm.berkeley_lm.LMGrammarBerkeley;
 import joshua.decoder.ff.lm.buildin_lm.LMGrammarJAVA;
 import joshua.decoder.ff.lm.kenlm.jni.KenLM;
 import joshua.decoder.ff.similarity.EdgePhraseSimilarityFF;
 import joshua.decoder.ff.state_maintenance.NgramStateComputer;
 import joshua.decoder.ff.state_maintenance.StateComputer;
 import joshua.decoder.ff.tm.Grammar;
 import joshua.decoder.ff.tm.GrammarFactory;
 import joshua.decoder.ff.tm.hash_based.MemoryBasedBatchGrammar;
 import joshua.decoder.ff.tm.packed.PackedGrammar;
 // import joshua.ui.hypergraph_visualizer.HyperGraphViewer;
 import joshua.util.FileUtility;
 import joshua.util.Regex;
 import joshua.util.io.LineReader;
 
 /**
  * Implements decoder initialization, including interaction with <code>JoshuaConfiguration</code>
  * and <code>DecoderThread</code>.
  * 
  * @author Zhifei Li, <zhifei.work@gmail.com>
  * @author wren ng thornton <wren@users.sourceforge.net>
  * @author Lane Schwartz <dowobeha@users.sourceforge.net>
  * @version $LastChangedDate$
  */
 public class JoshuaDecoder {
   /*
    * Many of these objects themselves are global objects. We pass them in when constructing other
    * objects, so that they all share pointers to the same object. This is good because it reduces
    * overhead, but it can be problematic because of unseen dependencies (for example, in the
    * Vocabulary shared by language model, translation grammar, etc).
    */
   /** The DecoderFactory is the main thread of decoding */
   private DecoderFactory decoderFactory;
   private List<GrammarFactory> grammarFactories;
   private ArrayList<FeatureFunction> featureFunctions;
   private ArrayList<NGramLanguageModel> languageModels;
 
   private List<StateComputer> stateComputers;
 
   /* The feature weights. */
   public static FeatureVector weights;
 
   /** Logger for this class. */
   private static final Logger logger = Logger.getLogger(JoshuaDecoder.class.getName());
 
   // ===============================================================
   // Constructors
   // ===============================================================
 
   /**
    * Constructs a new decoder using the specified configuration file.
    * 
    * @param configFile Name of configuration file.
    */
   public JoshuaDecoder(String configFile) {
     this();
     this.initialize(configFile);
   }
 
   /**
    * Constructs an uninitialized decoder for use in testing.
    * <p>
    * This method is private because it should only ever be called by the
    * {@link #getUninitalizedDecoder()} method to provide an uninitialized decoder for use in
    * testing.
    */
   private JoshuaDecoder() {
     this.grammarFactories = new ArrayList<GrammarFactory>();
   }
 
   /**
    * Gets an uninitialized decoder for use in testing.
    * <p>
    * This method is called by unit tests or any outside packages (e.g., MERT) relying on the
    * decoder.
    */
   static public JoshuaDecoder getUninitalizedDecoder() {
     return new JoshuaDecoder();
   }
 
   // ===============================================================
   // Public Methods
   // ===============================================================
 
   public void changeBaselineFeatureWeights(FeatureVector weights) {
     changeFeatureWeightVector(weights);
   }
 
   /**
    * Sets the feature weight values used by the decoder.
    * 
    * @param weights Feature weight values
    */
   public void changeFeatureWeightVector(FeatureVector newWeights) {
     if (newWeights != null) {
 
       for (String feature: this.weights.keySet()) {
         float oldWeight = this.weights.get(feature);
 				float newWeight = newWeights.get(feature);
 				this.weights.put(feature, newWeights.get(feature));
         logger.info(String.format("Feature %s: weight changed from %.3f to %.3f", feature, oldWeight, newWeight));
       }
     }
     // FIXME: this works for Batch grammar only; not for sentence-specific grammars
     for (GrammarFactory grammarFactory : this.grammarFactories) {
       // if (grammarFactory instanceof Grammar) {
       grammarFactory.getGrammarForSentence(null).sortGrammar(this.featureFunctions);
       // }
     }
   }
 
 
   /**
    * Decode a whole test set. This may be parallel.
    * 
    * @param testFile
    * @param nbestFile
    * @param oracleFile
    */
   public void decodeTestSet(String testFile, String nbestFile, String oracleFile)
       throws IOException {
     this.decoderFactory.decodeTestSet(testFile, nbestFile, oracleFile);
   }
 
   public void decodeTestSet(String testFile, String nbestFile) {
     this.decoderFactory.decodeTestSet(testFile, nbestFile, null);
   }
 
 
   /** Decode a sentence. This must be non-parallel. */
   public void decodeSentence(String testSentence, String[] nbests) {
     // TODO
   }
 
 
   public void cleanUp() {
     // TODO
     // this.languageModel.end_lm_grammar(); //end the threads
   }
 
 
   // public void visualizeHyperGraphForSentence(String sentence) {
   //   HyperGraphViewer.visualizeHypergraphInFrame(this.decoderFactory
   //       .getHyperGraphForSentence(sentence));
   // }
 
 
   public static void writeConfigFile(double[] newWeights, String template, String outputFile,
       String newDiscriminativeModel) {
     try {
       int columnID = 0;
 
       BufferedWriter writer = FileUtility.getWriteFileStream(outputFile);
       LineReader reader = new LineReader(template);
       try {
         for (String line : reader) {
           line = line.trim();
           if (Regex.commentOrEmptyLine.matches(line) || line.indexOf("=") != -1) {
             // comment, empty line, or parameter lines: just copy
             writer.write(line);
             writer.newLine();
 
           } else { // models: replace the weight
             String[] fds = Regex.spaces.split(line);
             StringBuffer newSent = new StringBuffer();
             if (!Regex.floatingNumber.matches(fds[fds.length - 1])) {
               throw new IllegalArgumentException("last field is not a number; the field is: "
                   + fds[fds.length - 1]);
             }
 
             if (newDiscriminativeModel != null && "discriminative".equals(fds[0])) {
               newSent.append(fds[0]).append(' ');
               newSent.append(newDiscriminativeModel).append(' ');// change the file name
               for (int i = 2; i < fds.length - 1; i++) {
                 newSent.append(fds[i]).append(' ');
               }
             } else {// regular
               for (int i = 0; i < fds.length - 1; i++) {
                 newSent.append(fds[i]).append(' ');
               }
             }
             if (newWeights != null)
               newSent.append(newWeights[columnID++]);// change the weight
             else
               newSent.append(fds[fds.length - 1]);// do not change
 
             writer.write(newSent.toString());
             writer.newLine();
           }
         }
       } finally {
         reader.close();
         writer.close();
       }
 
       if (newWeights != null && columnID != newWeights.length) {
         throw new IllegalArgumentException("number of models does not match number of weights");
       }
 
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 
 
 
   // ===============================================================
   // Initialization Methods
   // ===============================================================
 
   /**
    * Initialize all parts of the JoshuaDecoder.
    * 
    * @param configFile File containing configuration options
    * @return An initialized decoder
    */
   public JoshuaDecoder initialize(String configFile) {
     try {
 
       long pre_load_time = System.currentTimeMillis();
 
       // Load the weights.
       JoshuaDecoder.weights = this.readWeights(JoshuaConfiguration.weights_file);
 
 			this.featureFunctions = new ArrayList<FeatureFunction>();
 
 			/* Backwards compatibility.  Before initializing the grammars, the language models, or the
 			 * other feature functions, we need to take a pass through features and their weights
 			 * initialized in the old style, which was accomplished for many of the features simply by
 			 * setting a weight.  The new style puts all the weights in the weights file above, and has a
 			 * separate line that initializes the feature function.  Here, we look for the old-style, and
 			 * (1) add the weight for it and (2) trigger the feature with a new-style line.
 			 */
 			for (int i = 0; i < JoshuaConfiguration.features.size(); i++ ) {
 				String featureLine = JoshuaConfiguration.features.get(i);
 
 				// Check if this is an old-style feature.
 				if (! featureLine.startsWith("feature_function")) {
 					String fields[] = featureLine.split("\\s+");
 					String type = fields[0].toLowerCase();
 
 					if (type.equals("phrasemodel")) {
 						String name = "PhraseModel_" + fields[1] + "_" + fields[2];
 						float weight = Float.parseFloat(fields[3]);
 
 						weights.put(name, weight);
 
 						// No feature_function lines are created for LMs
 						JoshuaConfiguration.features.remove(i);
 						i--;
 					} 
 					else if (type.equals("lm")) {
 						String name = "";
 						float weight = 0.0f;
 						if (fields.length == 3) {
 							name = "lm_" + fields[1];
 							weight = Float.parseFloat(fields[2]);
 						} else {
 							name = "lm_0";
 							weight = Float.parseFloat(fields[1]);
 						}
 
 						weights.put(name, weight);
 
 						// No feature_function lines are created for LMs
 						JoshuaConfiguration.features.remove(i);
 						i--;
 					}
 					else if (type.equals("latticecost")) {
 						String name = "SourcePath";
 						float weight = Float.parseFloat(fields[1]);
 
 						weights.put(name, weight);
 						JoshuaConfiguration.features.set(i, "feature_function = " + name);
 					}
 					else if (type.equals("arityphrasepenalty")) {
 						String name = "ArityPenalty";
 						String owner = fields[1];
 						int min = Integer.parseInt(fields[2]);
 						int max = Integer.parseInt(fields[3]);
 						float weight = Float.parseFloat(fields[4]);
 
 						weights.put(name, weight);
 						JoshuaConfiguration.features.set(i, String.format("feature_function = %s %s %d %d", name, owner, min, max));
 					}
 					else if (type.equals("wordpenalty")) {
 						String name = "WordPenalty";
 						float weight = Float.parseFloat(fields[1]);
 
 						weights.put(name, weight);
 						JoshuaConfiguration.features.set(i, String.format("feature_function = %s", name));
 					}
 					else if (type.equals("oovpenalty")) {
 						String name = "OOVPenalty";
 						float weight = Float.parseFloat(fields[1]);
 
 						weights.put(name, weight);
 						JoshuaConfiguration.features.set(i, String.format("feature_function = %s", name));
 					}
 					else if (type.equals("edge-sim")) {
 						String name = "EdgePhraseSimilarity";
 						String host = fields[1];
 						int port = Integer.parseInt(fields[2]);
 						float weight = Float.parseFloat(fields[3]);
 
 						weights.put(name, weight);
 						JoshuaConfiguration.features.set(i, String.format("feature_function = %s %s %d", name, host, port));
 					}
 				}
 			}
 
       // Initialize and load grammars.
       this.initializeTranslationGrammars();
       logger.info(String.format("Grammar loading took: %d seconds.",
           (System.currentTimeMillis() - pre_load_time) / 1000));
 
       // Initialize features that contribute to state (currently only n-grams).
       this.initializeStateComputers();
 
       // Initialize the LM.
       initializeLanguageModels();
 
       // Initialize the features: requires that LM model has been initialized.
       this.initializeFeatureFunctions();
 
       long pre_sort_time = System.currentTimeMillis();
       // Sort the TM grammars (needed to do cube pruning)
       for (GrammarFactory grammarFactory : this.grammarFactories) {
         if (grammarFactory instanceof Grammar) {
           Grammar batchGrammar = (Grammar) grammarFactory;
           batchGrammar.sortGrammar(this.featureFunctions);
         }
       }
       logger.info(String.format("Grammar sorting took: %d seconds.",
           (System.currentTimeMillis() - pre_sort_time) / 1000));
 
       this.decoderFactory =
           new DecoderFactory(this.grammarFactories, JoshuaConfiguration.use_max_lm_cost_for_oov,
 						this.featureFunctions, this.weights, this.stateComputers);
 
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     return this;
   }
 
   private void initializeLanguageModels() throws IOException {
 
     // Indexed by order.
     HashMap<Integer, NgramStateComputer> ngramStateComputers = new HashMap<Integer, NgramStateComputer>();
 
     this.languageModels = new ArrayList<NGramLanguageModel>();
 
     // lm = kenlm 5 0 0 100 file
     for (String lmLine : JoshuaConfiguration.lms) {
 
       String tokens[] = lmLine.split("\\s+");
       String lm_type = tokens[0];
       int lm_order = Integer.parseInt(tokens[1]);
       boolean left_equiv_state = Boolean.parseBoolean(tokens[2]);
       boolean right_equiv_state = Boolean.parseBoolean(tokens[3]);
       double lm_ceiling_cost = Double.parseDouble(tokens[4]);
       String lm_file = tokens[5];
 
       if (! ngramStateComputers.containsKey(lm_order)) {
         // Create a new state computer.
         NgramStateComputer ngramState = new NgramStateComputer(lm_order);
         // Record that we've created it.
         stateComputers.add(ngramState);
         ngramStateComputers.put(lm_order, ngramState);
       }
 
       if (lm_type.equals("kenlm")) {
         if (left_equiv_state || right_equiv_state) {
           throw new IllegalArgumentException(
               "KenLM supports state.  Joshua should get around to using it.");
         }
 
         KenLM lm = new KenLM(lm_order, lm_file);
         this.languageModels.add(lm);
         Vocabulary.registerLanguageModel(lm);
         Vocabulary.id(JoshuaConfiguration.default_non_terminal);
 
       } else if (lm_type.equals("berkeleylm")) {
         LMGrammarBerkeley lm = new LMGrammarBerkeley(lm_order, lm_file);
         this.languageModels.add(lm);
         Vocabulary.registerLanguageModel(lm);
         Vocabulary.id(JoshuaConfiguration.default_non_terminal);
 
       } else if (lm_type.equals("none")) {
         ; // do nothing
 
       } else {
         logger.warning("WARNING: using built-in language model; you probably didn't intend this");
         logger.warning("  Valid lm types are 'kenlm', 'berkeleylm', 'javalm' and 'none'");
 
         this.languageModels.add(new LMGrammarJAVA(lm_order, lm_file, left_equiv_state,
             right_equiv_state));
       }
     }
 
     this.languageModels = new ArrayList<NGramLanguageModel>();
     for (int i = 0; i < this.languageModels.size(); i++) {
       NGramLanguageModel lm = this.languageModels.get(i);
       int order = lm.getOrder();
       this.featureFunctions.add(new LanguageModelFF(weights, lm, ngramStateComputers.get(lm.getOrder())));
 
			logger.info(String.format("FEATURE: language model #%d, order %d (weight %.3f)",
 					(i + 1), languageModels.get(i).getOrder(), weights.get(String.format("lm_%d",i))));
     }
   }
 
   private void initializeGlueGrammar() throws IOException {
     logger.info("Constructing glue grammar...");
 
     MemoryBasedBatchGrammar gr = (JoshuaConfiguration.glue_file == null) 
       ? new MemoryBasedBatchGrammar(JoshuaConfiguration.glue_format, 
             System.getenv().get("JOSHUA") + "/data/" + "glue-grammar",
             JoshuaConfiguration.glue_owner, JoshuaConfiguration.default_non_terminal, -1,
             JoshuaConfiguration.oov_feature_cost)
       : new MemoryBasedBatchGrammar(JoshuaConfiguration.glue_format, JoshuaConfiguration.glue_file,
             JoshuaConfiguration.glue_owner, JoshuaConfiguration.default_non_terminal, -1,
             JoshuaConfiguration.oov_feature_cost);
 
     this.grammarFactories.add(gr);
 
   }
 
   private void initializeTranslationGrammars() throws IOException {
 
 		if (JoshuaConfiguration.tms.size() > 0) {
 
       // tm = {thrax/hiero,packed,samt} OWNER LIMIT FILE
       for (String tmLine: JoshuaConfiguration.tms) {
         String tokens[] = tmLine.split("\\s+");
         String format = tokens[0];
         String owner = tokens[1];
         int span_limit = Integer.parseInt(tokens[2]);
         String file = tokens[3];
 
         logger.info("Using grammar read from file " + file);
 
 				GrammarFactory grammar = null;
         if (format.equals("packed")) {
 					grammar = new PackedGrammar(file, span_limit);
 
         } else {
 					grammar = new MemoryBasedBatchGrammar(format, file, owner, 
 						JoshuaConfiguration.default_non_terminal, span_limit,
 						JoshuaConfiguration.oov_feature_cost);
         }
 
 				this.grammarFactories.add(grammar);
 				this.featureFunctions.add(new PhraseModelFF(weights, grammar, owner));
 
        logger.info(String.format("FEATURE: phrase model with owner %s", owner));
       }
     } else {
       logger.warning("* WARNING: no grammars supplied!  Supplying dummy glue grammar.");
       // TODO: this should initialize the grammar dynamically so that the goal symbol and default
       // non terminal match
       MemoryBasedBatchGrammar glueGrammar = new MemoryBasedBatchGrammar(JoshuaConfiguration.glue_format, 
         System.getenv().get("JOSHUA") + "/data/" + "glue-grammar",
         JoshuaConfiguration.glue_owner, JoshuaConfiguration.default_non_terminal, -1,
         JoshuaConfiguration.oov_feature_cost);
       this.grammarFactories.add(glueGrammar);
 		}
 
 		logger.info(String.format("Memory used %.1f MB", ((Runtime.getRuntime().totalMemory() - Runtime
         .getRuntime().freeMemory()) / 1000000.0)));
   }
 	
 
   private void initializeMainTranslationGrammar() throws IOException {
 		if (JoshuaConfiguration.tm_file == null) {
       logger.warning("* WARNING: no TM specified");
 			return;
 		}
 
     if (JoshuaConfiguration.use_sent_specific_tm) {
       logger.info("Basing sentence-specific grammars on file " + JoshuaConfiguration.tm_file);
       return;
     } else if ("packed".equals(JoshuaConfiguration.tm_format)) {
       this.grammarFactories.add(new PackedGrammar(JoshuaConfiguration.tm_file,
           JoshuaConfiguration.span_limit));
     } else {
       logger.info("Using grammar read from file " + JoshuaConfiguration.tm_file);
       this.grammarFactories.add(new MemoryBasedBatchGrammar(JoshuaConfiguration.tm_format,
           JoshuaConfiguration.tm_file, JoshuaConfiguration.phrase_owner,
           JoshuaConfiguration.default_non_terminal, JoshuaConfiguration.span_limit,
           JoshuaConfiguration.oov_feature_cost));
     }
 
     logger.info(String.format("Memory used %.1f MB", ((Runtime.getRuntime().totalMemory() - Runtime
         .getRuntime().freeMemory()) / 1000000.0)));
   }
 
   private void initializeStateComputers() {
     stateComputers = new ArrayList<StateComputer>();
   }
 
   /* This function reads the weights for the model.  For backwards compatibility, weights may be
    * listed in the Joshua configuration file, but the preferred method is to list the weights in a
    * separate file, specified by the Joshua parameter "weights-file".
    *
    * Feature names and their weights are listed one per line in the following format
    *
    * FEATURE NAME WEIGHT
    *
    * Fields are space delimited.  The first k-1 fields are concatenated with underscores to form the
    * feature name (putting them there explicitly is preferred, but the concatenation is in place for
    * backwards compatibility
    */
   private FeatureVector readWeights(String fileName) {
     FeatureVector weights = new FeatureVector();
 
     try {
       LineReader lineReader = new LineReader(fileName);
 
       for (String line: lineReader) {
         if (line.equals("") || line.startsWith("#") || line.startsWith("//") || line.indexOf(' ') == -1)
           continue;
 
         String feature = line.substring(0, line.lastIndexOf(' ')).replaceAll(" ", "_");
         Float value = Float.parseFloat(line.substring(line.lastIndexOf(' ')));
 
         weights.put(feature.toLowerCase(), value);
       }
     } catch (FileNotFoundException ioe) {
       System.err.println("* WARNING: Can't find weights-file '" + fileName + "'");
     } catch (IOException ioe) {
       System.err.println("* FATAL: Can't read file weights-file '" + fileName + "'");
       ioe.printStackTrace();
       System.exit(1);
     }
 
     return weights;
   }
 
   /**
    * This function supports two means of activating features.  (1) The old format turns on a feature
    * when it finds a line of the form "FEATURE OPTIONS WEIGHTS" (lines with an = sign, which signify
    * configuration options).  (2) The new format requires lines that are of the form
    * "feature_function = FEATURE OPTIONS", and expects to find the weights loaded separately in the
    * weights file.
    *
    */
   private void initializeFeatureFunctions() {
 
     for (String featureLine : JoshuaConfiguration.features) {
 
       // Get rid of the leading crap.
       featureLine = featureLine.replaceFirst("^feature_function\\s*=\\s*", "");
 
       String fields[] = featureLine.split("\\s+");
       String feature = fields[0].toLowerCase();
 
       if (feature.equals("latticecost") || feature.equals("sourcepath")) {
         this.featureFunctions.add(new SourcePathFF(JoshuaDecoder.weights));
        logger.info(String.format("FEATURE: lattice cost (weight %.3f)", weights.get("sourcepath")));
       }
 
       else if (feature.equals("arityphrasepenalty") || feature.equals("aritypenalty")) {
         String owner = fields[1];
         int startArity = Integer.parseInt(fields[2].trim());
         int endArity = Integer.parseInt(fields[3].trim());
         float weight = Float.parseFloat(fields[4].trim());
 
         weights.put("aritypenalty", weight);
         this.featureFunctions.add(new ArityPhrasePenaltyFF(weights, String.format("%s %d %d", owner, startArity, endArity)));
 
        logger.info(String.format(
            "FEATURE: arity phrase penalty: owner %s, start %d, end %d (weight %.3f)", owner,
            startArity, endArity, weight));
       }
 
       else if (feature.equals("wordpenalty")) {
         this.featureFunctions.add(new WordPenaltyFF(weights));
 
        logger.info(String.format("FEATURE: word penalty (weight %.3f)", weights.get("wordpenalty")));
       }
 
       else if (feature.equals("oovpenalty")) {
         this.featureFunctions.add(new OOVFF(weights));
 
        logger.info(String.format("FEATURE: OOV penalty (weight %.3f)", weights.get("oovpenalty")));
 
       } else if (feature.equals("edgephrasesimilarity")) {
         String host = fields[1].trim();
         int port = Integer.parseInt(fields[2].trim());
         double weight = Double.parseDouble(fields[3].trim());
 
 				// Find the language model with the largest state.
 				int maxOrder = 0;
 				NgramStateComputer ngramStateComputer = null;
 				for (StateComputer stateComputer: this.stateComputers) {
 					if (stateComputer instanceof NgramStateComputer)
 						if (((NgramStateComputer)stateComputer).getOrder() > maxOrder) {
 							maxOrder = ((NgramStateComputer)stateComputer).getOrder();
 							ngramStateComputer = (NgramStateComputer)stateComputer;
 						}
 				}
 
         try {
           this.featureFunctions.add(new EdgePhraseSimilarityFF(weights, ngramStateComputer, host, port));
         } catch (Exception e) {
           e.printStackTrace();
           System.exit(1);
         }
         logger.info(String.format("FEATURE: edge similarity (weight %.3f)", weights.get("edgephrasesimilarity")));
       } else {
         System.err.println("* WARNING: invalid feature '" + featureLine + "'");
       }
     }
   }
 
 
   // ===============================================================
   // Main
   // ===============================================================
   public static void main(String[] args) throws IOException {
 
     String logFile = System.getenv().get("JOSHUA") + "/logging.properties";
     try {
       java.util.logging.LogManager.getLogManager().readConfiguration(new FileInputStream(logFile));
     } catch (IOException e) {
       logger.warning("Couldn't initialize logging properties from '" + logFile + "'");
     }
 
     long startTime = System.currentTimeMillis();
 
     // if (args.length < 1) {
     //   System.out.println("Usage: java " + JoshuaDecoder.class.getName()
     //       + " -c configFile [other args]");
     //   System.exit(1);
     // }
 
     String configFile = null;
     String testFile = "-";
     String nbestFile = "-";
     String oracleFile = null;
 
     // Step-0: Process the configuration file. We accept two use
     // cases. (1) For backwards compatility, Joshua can be called
     // with as "Joshua configFile [testFile [outputFile
     // [oracleFile]]]". (2) Command-line options can be used, in
     // which case we look for an argument to the "-config" flag.
     // We can distinguish these two cases by looking at the first
     // argument; if it starts with a hyphen, the new format has
     // been invoked.
 
 		if (args.length >= 1) {
 			if (args[0].startsWith("-")) {
 
 				// Search for the configuration file
 				for (int i = 0; i < args.length; i++) {
 					if (args[i].equals("-c") || args[i].equals("-config")) {
 
 						configFile = args[i + 1].trim();
 						JoshuaConfiguration.readConfigFile(configFile);
 
 						break;
 					}
 				}
 
 				// now process all the command-line args
 				JoshuaConfiguration.processCommandLineOptions(args);
 
 				oracleFile = JoshuaConfiguration.oracleFile;
 
 			} else {
 
 				configFile = args[0].trim();
 
 				JoshuaConfiguration.readConfigFile(configFile);
 
 				if (args.length >= 2) testFile = args[1].trim();
 				if (args.length >= 3) nbestFile = args[2].trim();
 				if (args.length == 4) oracleFile = args[3].trim();
 			}
 		}
 
     /* Step-0: some sanity checking */
     JoshuaConfiguration.sanityCheck();
 
     /* Step-1: initialize the decoder, test-set independent */
     JoshuaDecoder decoder = new JoshuaDecoder(configFile);
 
     logger.info(String.format("Model loading took %d seconds",
         (System.currentTimeMillis() - startTime) / 1000));
     logger.info(String.format("Memory used %.1f MB", ((Runtime.getRuntime().totalMemory() - Runtime
         .getRuntime().freeMemory()) / 1000000.0)));
 
     /* Step-2: Decoding */
     decoder.decodeTestSet(testFile, nbestFile, oracleFile);
 
     logger.info("Decoding completed.");
     logger.info(String.format("Memory used %.1f MB", ((Runtime.getRuntime().totalMemory() - Runtime
         .getRuntime().freeMemory()) / 1000000.0)));
 
     /* Step-3: clean up */
     decoder.cleanUp();
     logger.info(String.format("Total running time: %d seconds",
         (System.currentTimeMillis() - startTime) / 1000));
   }
 }
