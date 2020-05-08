 package edu.toronto.cs.ontools.main;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 
 import edu.toronto.cs.ontools.annotation.GeneGOAnnotations;
 import edu.toronto.cs.ontools.annotation.GeneHPOAnnotations;
 import edu.toronto.cs.ontools.annotation.OmimHPOAnnotations;
 import edu.toronto.cs.ontools.annotation.TaxonomyAnnotation;
 import edu.toronto.cs.ontools.similarity.SimilarityGenerator;
 import edu.toronto.cs.ontools.taxonomy.GO;
 import edu.toronto.cs.ontools.taxonomy.HPO;
 import edu.toronto.cs.ontools.taxonomy.Taxonomy;
 import edu.toronto.cs.ontools.taxonomy.clustering.BottomUpAnnClustering;
 
 public class CommandDispatcher {
 	@SuppressWarnings("unchecked")
 	private static Map SUPPORTED_TAXONOMY_ANNOTATIONS = new TreeMap<String, TreeMap<String, Boolean>>() {
 		private static final long serialVersionUID = 20120815122350L;
 
 		{
 			put("GO", new TreeMap<String, Boolean>() {
 				private static final long serialVersionUID = 20120815122350L;
 				{
 					put("GENE", true);
 				}
 			});
 			put("HPO", new TreeMap<String, Boolean>() {
 				private static final long serialVersionUID = 20120815122350L;
 
 				{
 					put("OMIM", true);
 					put("GENE", true);
 				}
 			});
 		}
 	};
 
 	private static List<String> DEFAULT_GO_EVIDENCE_SOURCES = new LinkedList<String>() {
 		private static final long serialVersionUID = 20120815122350L;
 		{
 			String[] values = { "EXP", "IDA", "IPI", "IMP", "IGI", "IEP",
 					"ISS", "ISO", "ISA", "ISM" };
 			addAll(Arrays.asList(values));
 		}
 	};
 	private static List<String> GO_EVIDENCE_SOURCES = new LinkedList<String>() {
 		private static final long serialVersionUID = 20120815122350L;
 		{
 			addAll(DEFAULT_GO_EVIDENCE_SOURCES);
 			String[] otherValues = { "IGC", "RCA" };
 			addAll(Arrays.asList(otherValues));
 		}
 	};;
 
 	// private static final List<Object>
 
 	private enum CmdLineOptions {
 		REDUCE("r", "taxonomy-reduction", false,
 				"Perform taxonomy reduction wrt the given annotations"),
 
 		SIMILARITY(
 				"s",
 				"similarity",
 				false,
 				"Compute similarity scores for sets of terms fiven in an input file.\n\nIt is MANDATORY that one and only one of -r and -s be present.\n"),
 
 		@SuppressWarnings("unchecked")
 		TAXONOMY("t", "taxonomy", true, "MANDATORY. Which taxonomy to use?",
 				null, SUPPORTED_TAXONOMY_ANNOTATIONS.keySet(), false),
 
 		@SuppressWarnings("unchecked")
 		ANNOTATION("a", "annotation", true,
 				"MANDATORY. Which annotatoion to use?", null,
 				new HashSet<String>() {
 					private static final long serialVersionUID = 20120815122350L;
 					{
 						for (Object taxonomy : SUPPORTED_TAXONOMY_ANNOTATIONS
 								.values()) {
 							addAll(((TreeMap<String, TaxonomyAnnotation>) taxonomy)
 									.keySet());
 						}
 					}
 				}, false),
 
 		INPUT(
 				"i",
 				"input file",
 				true,
 				"ONLY WITH \"-c simmilarity\", where it is MANDATORY. Ingnored otherwise. "
 						+ "The path to a tab-separated text file, where the first two columns are comma-separated "
 						+ "lists of terms from the taxonomy selected with the -t parameter. "
 						+ "All other columns and all lines starting with \"##\" are ignored"),
 
 		OUTPUT(
 				"o",
 				"output-file",
 				true,
 				"ONLY WITH \"-c simmilarity\", ingnored otherwise."
 						+ "The path of the output file which will be a copy of the input file with "
 						+ "one extra column containing the similarity scores for the sets in the first two columns. "
 						+ "If this parameter is missing, <input file name>.out will be used instead. "),
 
 		EVIDENCE(
 				"e",
 				"go-evidence-sources",
 				true,
 				"ONLY WITH \"-c simmilarity\", ingnored otherwise. A comma-separated list of \"trusted\" evidence "
 						+ "sources for GO annotations. ",
 				DEFAULT_GO_EVIDENCE_SOURCES, GO_EVIDENCE_SOURCES, true),
 
 		HELP("h", "help", false, "Shows command line options and exist");
 
 		private final String shortOption;
 
 		private final String longOption;
 
 		private final boolean hasArgument;
 
 		private final String description;
 
 		private final Object defaultValue;
 
 		private final Collection<String> values;
 
 		private final boolean multipleValues;
 
 		private CmdLineOptions(String shortOption, String longOption,
 				boolean hasArgument, String description) {
 			this(shortOption, longOption, hasArgument, description, null, null,
 					false);
 		}
 
 		private CmdLineOptions(String shortOption, String longOption,
 				boolean hasArgument, String description, Object defaultValue,
 				Collection<String> values, boolean multipleValues) {
 			this.shortOption = shortOption;
 			this.longOption = longOption;
 			this.hasArgument = hasArgument;
 			String tmpDescription = description;
 			this.defaultValue = defaultValue;
 			if (values != null) {
 				this.values = values;
 			} else {
 				this.values = new LinkedList<String>();
 			}
 			if (this.values.size() > 0) {
 				tmpDescription += " Supported values: " + this.values;
 			}
 			if (this.defaultValue != null) {
 				tmpDescription += " Default value: " + this.defaultValue;
 			}
 			this.description = tmpDescription;
 			this.multipleValues = multipleValues;
 		}
 
 		public String getOption() {
 			return this.shortOption;
 		}
 
 		public String getShortOption() {
 			return this.shortOption;
 		}
 
 		public String getLongOption() {
 			return this.longOption;
 		}
 
 		public boolean hasArgument() {
 			return this.hasArgument;
 		}
 
 		public String getDescription() {
 			return this.description;
 		}
 
 		public Object getDefaultValue() {
 			return this.defaultValue;
 		}
 
 		public Collection<String> getValues() {
 			return this.values;
 		}
 
 		public boolean isValueValid(String value) {
 			return this.values.size() == 0 || this.values.contains(value);
 		}
 
 		public boolean isValueValid(Collection<String> value) {
 			return this.values.size() == 0 || this.multipleValues
 					&& this.values.containsAll(value);
 		}
 
 		public static Options generateOptions() {
 			Options options = new Options();
 			for (CmdLineOptions option : CmdLineOptions.values()) {
 				options.addOption(option.getShortOption(), option
 						.getLongOption(), option.hasArgument(), option
 						.getDescription());
 			}
 			return options;
 		}
 	}
 
 	CommandLine cmd;
 
 	private boolean hasOption(CmdLineOptions option) {
 		return this.cmd.hasOption(option.getOption());
 	}
 
 	private String getOptionValue(CmdLineOptions option) {
 		return this.cmd.getOptionValue(option.getOption());
 	}
 
 	/**
 	 * @param args
 	 */
 	@SuppressWarnings("unchecked")
 	public void process(String[] args) {
 		Options options = CmdLineOptions.generateOptions();
 
 		try {
 			CommandLineParser parser = new PosixParser();
 			this.cmd = parser.parse(options, args);
 
 			// Check for mandatory arguments
 
 			if (this.hasOption(CmdLineOptions.HELP)) {
 				showUsage(options);
 				System.exit(0);
 			}
 			if ((!this.hasOption(CmdLineOptions.REDUCE) && !this
 					.hasOption(CmdLineOptions.SIMILARITY))
 					|| (this.hasOption(CmdLineOptions.REDUCE) && this
 							.hasOption(CmdLineOptions.SIMILARITY))) {
 				showUsage(options);
 				failWithMessage("It is MANDATORY that one and only one of -r and -s be present in the command line."
 						+ this.hasOption(CmdLineOptions.REDUCE)
 						+ this.hasOption(CmdLineOptions.SIMILARITY));
 			}
 
 			if (!this.hasOption(CmdLineOptions.TAXONOMY)) {
 				showUsage(options);
 				failWithMessage("The taxonomy name (-t) is a MANDATORY argument");
 			}
 
 			if (!this.hasOption(CmdLineOptions.ANNOTATION)) {
 				showUsage(options);
 				failWithMessage("The annotation type (-a) is a MANDATORY argument");
 			}
 
 			// Get the Taxonomy
 			String taxonomyName = this.getOptionValue(CmdLineOptions.TAXONOMY);
 			String annotationType = this
 					.getOptionValue(CmdLineOptions.ANNOTATION);
 			TreeMap<String, Boolean> supportedAnnotations = (TreeMap<String, Boolean>) SUPPORTED_TAXONOMY_ANNOTATIONS
 					.get(taxonomyName);
 			Taxonomy taxonomy = null;
 			if (supportedAnnotations == null) {
 				failWithMessage("Unsupported taxonomy: " + taxonomyName
 						+ ". please choose one of: "
 						+ CmdLineOptions.TAXONOMY.getValues());
 			} else {
 				taxonomy = taxonomyName.equals("HPO") ? new HPO() : new GO();
 			}
 
 			// Get the annotation
 			TaxonomyAnnotation ann = null;
 			if (supportedAnnotations.get(annotationType) == null) {
 				failWithMessage("Unsupported annotation type: "
 						+ annotationType + ". please choose one of: "
 						+ CmdLineOptions.ANNOTATION.getValues());
 			} else {
 				List<String> ev = DEFAULT_GO_EVIDENCE_SOURCES;
 				// Any custom evidence levels?
 				if (hasOption(CmdLineOptions.EVIDENCE)) {
 					if (annotationType.equals("GENE")
 							&& taxonomyName.equals("GO")) {
 						String evStr = getOptionValue(CmdLineOptions.EVIDENCE);
 						String pieces[] = evStr.split("\\s*,\\s*");
 						if (pieces.length > 0) {
 							List<String> tmpEv = Arrays.asList(pieces);
 							if (!CmdLineOptions.EVIDENCE.isValueValid(tmpEv)) {
 								System.err
 										.println("Found invalid values in the evidence list provided with -e. Ignoring list.");
 							} else {
 								ev = tmpEv;
 							}
 						}
 					}
 				}
				ann = annotationType.equals("OMIM") ? new OmimHPOAnnotations(
 						taxonomy)
 						: (taxonomyName.equals("HPO") ? new GeneHPOAnnotations(
 								taxonomy) : new GeneGOAnnotations(taxonomy, ev));
 			}
 
 			// What does the user want to do?
 			if (this.hasOption(CmdLineOptions.REDUCE)) {
 				// cluster the taxonomy
 				BottomUpAnnClustering mfp = new BottomUpAnnClustering(ann,
 						LocalFileUtils
 								.getTemporaryFile("omim_symptoms_rank_data"),
 						LocalFileUtils.getTemporaryFile("log_"
 								+ new Date(System.currentTimeMillis())
 										.toString()));
 				mfp.buttomUpCluster().display(
 						LocalFileUtils.getTemporaryFile("out_"
 								+ new Date(System.currentTimeMillis())
 										.toString()));
 			} else {
 				// compute similarities
 				// check for the input file
 				if (!this.hasOption(CmdLineOptions.INPUT)) {
 					showUsage(options);
 					failWithMessage("The argument -i is mandatory with -c similarity");
 				}
 				// get i/o files
 				String inputFileName = this
 						.getOptionValue(CmdLineOptions.INPUT);
 				String outputFileName = this.hasOption(CmdLineOptions.OUTPUT) ? this
 						.getOptionValue(CmdLineOptions.OUTPUT)
 						: inputFileName + ".out";
 				// Run the score computation
 				new SimilarityGenerator().generateSimilarityScores(ann,
 						inputFileName, outputFileName);
 			}
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	protected void showUsage(Options options) {
 		String name = "java -jar <name>.jar";
 		HelpFormatter formatter = new HelpFormatter();
 		formatter.printHelp(120, name + " [options]", "", options,
 				"\n EXAMPLES:\n" + name + " -s -t HPO - a OMIM -i sample.in\n"
 						+ name + " -r -t HPO - a OMIM\n");
 	}
 
 	protected void failWithMessage(String message) {
 		System.err.println("\n\nEXITING WITH ERROR. CAUSE:");
 		System.err.println(message);
 		System.exit(1);
 	}
 }
