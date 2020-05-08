 /*
  * Copyright (C) 2007-2009 Institute for Computational Biomedicine,
  *                         Weill Medical College of Cornell University
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.bdval;
 
 import com.martiansoftware.jsap.FlaggedOption;
 import com.martiansoftware.jsap.JSAP;
 import com.martiansoftware.jsap.JSAPException;
 import com.martiansoftware.jsap.JSAPResult;
 import com.martiansoftware.jsap.Parameter;
 import com.martiansoftware.jsap.Switch;
 import edu.cornell.med.icb.geo.tools.ClassificationTask;
 import edu.cornell.med.icb.learning.CrossValidation;
 import edu.cornell.med.icb.tools.svmlight.EvaluationMeasure;
 import edu.mssm.crover.tables.ColumnTypeException;
 import edu.mssm.crover.tables.InvalidColumnException;
 import edu.mssm.crover.tables.Table;
 import edu.mssm.crover.tables.TypeMismatchException;
 import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
 import it.unimi.dsi.fastutil.doubles.DoubleList;
 import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
 import it.unimi.dsi.fastutil.objects.ObjectArrayList;
 import it.unimi.dsi.fastutil.objects.ObjectArraySet;
 import it.unimi.dsi.fastutil.objects.ObjectList;
 import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
 import it.unimi.dsi.fastutil.objects.ObjectSet;
 import it.unimi.dsi.io.FastBufferedReader;
 import it.unimi.dsi.io.LineIterator;
 import it.unimi.dsi.lang.MutableString;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Make predictions for unlabeled samples. Can be used to evaluate predictions on an independent
  * test set. True labels can be provided for purpose of evaluation in the form of a cids file.
  *
  * @author Fabien Campagne Date: Oct 23, 2007 Time: 6:39:51 PM
  */
 public class Predict extends DAVMode {
     /**
      * Used to log debug and informational messages.
      */
     private static final Log LOG = LogFactory.getLog(Predict.class);
 
     private Map<String, String> sample2TrueLabelMap;
 
     /**
      * When True, print statistics like Cross-validation, but for the test set.
      * False, print a detailed table of predictions.
      */
     private boolean printStats;
     private String survivalFileName;
     private ObjectSet<String> testSampleIds;
 
     static final CharSequence[] MEASURES = {
             "auc", "mat", "rmse", "acc", "f", "spec", "sens", "prec", "rec", "MCC"
     };
 
     private final MaqciiHelper maqciiHelper = new MaqciiHelper();
     private boolean sampleWithReplacement = true;
     private String testSampleFilename;
 
     private String modelFilenamePrefix;
     private String modelFilenamePrefixNoPath;
     private BDVModel model;
     private String trueLabelFilename;
 
     /**
      * Define command line options for this mode.
      *
      * @param jsap the JSAP command line parser
      * @throws JSAPException if there is a problem building the options
      */
     @Override
     public void defineOptions(final JSAP jsap) throws JSAPException {
         // there is no need for task definitions.
         jsap.getByID("task-list").addDefault("N/A");
         //jsap.unregisterParameter(jsap.getByID("task-list"));
         // there is no need for condition ids.
         jsap.getByID("conditions").addDefault("N/A");
         //jsap.unregisterParameter(jsap.getByID("conditions"));
         // there is no need for random seed.
         jsap.getByID("seed").addDefault("" + 1);
         //jsap.unregisterParameter(jsap.getByID("seed"));
         // there is no need for a gene list. The model has enough information to recreate it.
         //  jsap.unregisterParameter(jsap.getByID("gene-lists"));
         jsap.getByID("gene-lists").addDefault("N/A");
         final Parameter inputFilenameOption = new FlaggedOption("model")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(true)
                 .setLongFlag("model")
                 .setHelp("Model filename prefix. Models have several files named with a common "
                         + "prefix. The model that will be used to predict the label of the "
                         + "samples in the input file.");
         jsap.registerParameter(inputFilenameOption);
 
         final Parameter testSampleListFilenameOption = new FlaggedOption("test-samples")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setLongFlag("test-samples")
                 .setHelp("Filename for list of test sample ids. Path to a file with one line per "
                         + "test sample id. The input dataset will be filtered "
                         + "to keep only those samples in the list for prediction and "
                         + "performance calculation.");
         jsap.registerParameter(testSampleListFilenameOption);
 
         final Parameter statOption = new Switch("print-stats")
                 .setLongFlag("print-stats")
                 .setHelp("Print statistics instead of detailed result table.");
         jsap.registerParameter(statOption);
 
 
         final Parameter estimateWithReplacementOption = new FlaggedOption("estimate-with-replacement")
                 .setStringParser(JSAP.BOOLEAN_PARSER)
                 .setRequired(false)
                 .setDefault("false")
                 .setLongFlag("estimate-with-replacement")
                 .setHelp("Estimate performance measure as an average over a number of test set samples constructed by sampling the fixed test set with replacement. A thousand samplings are considered. This makes it possible to estimate std deviation of each measure on the test set and acknowledges that the test set is just another sample of a very large population. Default is false.");
         jsap.registerParameter(estimateWithReplacementOption);
 
         final Parameter trueLabelOption = new FlaggedOption("true-labels")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setLongFlag("true-labels")
                 .setHelp("True labels for this dataset, in the cids format. Providing true "
                         + "labels makes it possible to report evaluation measures on the "
                         + "test set.");
         jsap.registerParameter(trueLabelOption);
 
         final Parameter survivalFilenameOption = new FlaggedOption("survival")
                 .setStringParser(JSAP.STRING_PARSER)
                 .setDefault(JSAP.NO_DEFAULT)
                 .setRequired(false)
                 .setLongFlag("survival")
                 .setHelp("Survival filename. This file contains survival data "
                         + "in tab delimited table; column 1: chipID has to match cids and "
                         + "tmm, column 2: time to event, column 3 censor with 1 as event 0 "
                         + "as censor, column 4 and beyond are all numerical covariates that "
                         + "will be included in the regression model");
         jsap.registerParameter(survivalFilenameOption);
 
         maqciiHelper.defineSubmissionFileOption(jsap);
         jsap.getByID("label").addDefault("auto");
         jsap.getByID("folds").addDefault("0");
     }
 
     @Override
     protected void setupGeneLists(final JSAPResult result, final DAVOptions options) {
         // do nothing.
     }
 
     @Override
     public void interpretArguments(final JSAP jsap, final JSAPResult result, final DAVOptions options) {
         checkArgumentsSound(jsap, result, false);
 
         if (result.contains("model")) {
             modelFilenamePrefix = result.getString("model");
             modelFilenamePrefix = BDVModel.removeSuffix(modelFilenamePrefix, ".model");
             modelFilenamePrefixNoPath = FilenameUtils.getName(modelFilenamePrefix);
         }
         setupOutput(result, options);
         setupModelId(result, options);
         setupDatasetName(result, options);
         setupSplitPlan(result, options);
 
         loadModel(options);
 
         preparePathwayOptions(result, options);
 
         setupTableCache(result, options);
         setupInput(result, options);
         setupPlatforms(result, options);
         setupGeneLists(result, options);
         setupRandomArguments(result, options);
         setupArrayAttributes(result, options);
         setupRservePort(result, options);
         setupClassifier(result, options);
 
         if (result.contains("estimate-with-replacement")) {
             sampleWithReplacement = result.getBoolean("estimate-with-replacement");
         }
 
         printStats = result.getBoolean("print-stats");
         if (result.contains("survival")) {
             survivalFileName = result.getString("survival");
         }
 
          trueLabelFilename = result.getString("true-labels");
         sample2TrueLabelMap =
                 readSampleToTrueLabelsMap(trueLabelFilename, printStats);
 
         final String testSampleFilename = result.getString("test-samples");
         this.testSampleFilename = testSampleFilename;
         if (testSampleFilename != null) {
             LOG.info("Reading test sample filename: " + testSampleFilename);
 
             testSampleIds = new ObjectOpenHashSet<String>();
             FastBufferedReader reader = null;
             try {
                 reader = new FastBufferedReader(new FileReader(testSampleFilename));
                 final LineIterator lit = new LineIterator(reader);
                 while (lit.hasNext()) {
                     final String sampleId = lit.next().toString().trim();
                     testSampleIds.add(sampleId);
                 }
             } catch (FileNotFoundException e) {
                 LOG.fatal("Cannot read test sample file: " + testSampleFilename, e);
                 System.exit(10);
             } finally {
                 IOUtils.closeQuietly(reader);
             }
         }
 
         if (printStats) {
             if ("auto".equals(result.getString("label"))) {
                 final File filename = new File(modelFilenamePrefix);
                 final String cleanModelPrefix = filename.getName();
                 String label = StatsMode.guessLabel("", cleanModelPrefix, "[^-]*-(.*)-.....");
                 if (label == null) {
                     label = "unknown-label";
                 }
                 maqciiHelper.setupSubmissionFile(result, options, label);
             } else {
                 maqciiHelper.setupSubmissionFile(result, options);
             }
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Inside predict process filename = " + survivalFileName);
             }
 
             if (survivalFileName != null && !survivalFileName.equals("-")) {
                 maqciiHelper.printSubmissionHeaders(options, true);
             } else {
                 maqciiHelper.printSubmissionHeaders(options);
             }
         }
 
         if (LOG.isTraceEnabled()) {
             LOG.trace("sample2TrueLabelMap:");
             LOG.trace(MapUtils.toProperties(sample2TrueLabelMap).toString());
         }
     }
 
     /**
      * Setup pathway options. Some options can be specified from the command line. In this case,
      * they override the model property pathway options. Options that can be overriden in this
      * way are:
      * <LI> --pathways-component-dir
      * <LI> --pathways
      * <LI> --gene-to-probes
      * In contrast, the pathway aggregation method is always read from the models properties.
      */
     private void preparePathwayOptions(final JSAPResult result, final DAVOptions options) {
         final it.unimi.dsi.util.Properties modelProperties = model.getProperties();
         String pathwayComponentsDir = null;
         final String pathwayAggregationMethod = modelProperties.getString("pathway.aggregation.method");
         String pathwayFilename = null;
         String gene2ProbeFilename = null;
 
         if (result.contains("pathway-aggregation-method")) {
             System.err.println("Ignoring command line --pathway-aggregation-method option. The aggregation method is already specified by the model's properties.");
         }
 
         if (result.contains("pathway-components-dir")) {
             pathwayComponentsDir = result.getString("pathway-components-dir");
             System.out.println("Using --pathway-components-dir=" + pathwayComponentsDir);
         }
 
         if (result.contains("pathways")) {
             pathwayFilename = result.getString("pathways");
             gene2ProbeFilename = result.getString("gene2probes");
 
             if (pathwayFilename == null) {
                 System.out.println("Obtaining --pathways option from model's properties. Provide on the command line to override.");
                 pathwayFilename = modelProperties.getString("pathway.option.pathways");
             }
 
             if (gene2ProbeFilename == null) {
                 System.out.println("Obtaining --gene2probes option from model's properties. Provide on the command line to override.");
                 gene2ProbeFilename = modelProperties.getString("pathway.option.gene-to-probe");
             }
             System.out.println("Using --pathways=" + pathwayFilename);
             System.out.println("Using --gene-to-probes=" + gene2ProbeFilename);
 
         }
         setupPathwayOptions(options,
                 pathwayAggregationMethod, pathwayFilename, gene2ProbeFilename, pathwayComponentsDir);
 
     }
 
     private void loadModel(final DAVOptions options) {
         try {
             model = new BDVModel(modelFilenamePrefix);
             model.load(options);
         } catch (IOException e) {
             LOG.fatal("Error loading model " + modelFilenamePrefix, e);
             System.exit(10);
         } catch (ClassNotFoundException e) {
             LOG.fatal("Error loading model " + modelFilenamePrefix, e);
             System.exit(10);
         }
     }
 
     @Override
     public void process(final DAVOptions options) {
         super.process(options);
         try {
             if ("no_model_id".equals(options.modelId)) {
                 // try to guess model-id from model filename:
                 // e.g., Cologne_OS_MO-genelists-NC01-2000-svmglobal-SIYCT.model
                 final Pattern pattern = Pattern.compile(".*-(.....)");
                 final CharSequence cleanedModelPrefix = FilenameUtils.getName(model.getModelFilenamePrefix());
                 final Matcher matcher = pattern.matcher(cleanedModelPrefix);
                 if (!matcher.matches()) {
                     throw new IllegalArgumentException(
                             "model prefix must follow naming conventions to extract model-id.");
                 }
                 options.modelId = matcher.group(1);
             }
             if (!printStats) {
                 if (!options.outputFilePreexist) {
                     options.output.println(PredictedItem.getHeaders());
                     options.output.flush();
                 }
                 maqciiHelper.printSubmissionHeaders(options);
             }
 
             final List<Set<String>> labelValueGroups = new ArrayList<Set<String>>();
             assert model.getGeneList() != null : " gene list must not be null";
 
             final Table testSet = model.loadTestSet(this, options,
                     model.getGeneList(), labelValueGroups, testSampleIds);
 
             final int filteredNumberOfSamples = testSet.getRowNumber();
             LOG.info("Test set has " + filteredNumberOfSamples + " samples.");
 
             if (testSampleIds != null && filteredNumberOfSamples != testSampleIds.size()) {
                 System.err.println(String.format("Error: The number of samples must match after "
                         + "test set filter. Filtered test set was found to contain "
                         + "%d, but test set file %s named exactly %d samples.",
                         testSampleIds.size(), testSampleFilename, filteredNumberOfSamples));
                 System.exit(1);
             }
 
             final String[] idRefs = extractIdRefs(testSet);
             model.prepareClassificationProblem(testSet);
 
             final DoubleList decisions = new DoubleArrayList();
             final DoubleList trueLabels = new DoubleArrayList();
             final int numberOfSamples = testSet.getRowNumber();
 
             accumulatePredictions(options, idRefs, decisions, trueLabels, numberOfSamples, model);
 //   CrossValidation CV=new CrossValidation(helper.classifier, helper.problem,options.randomGenerator);
 
             if (printStats) {
                 final ObjectSet<CharSequence> evaluationMeasureNames = new ObjectArraySet<CharSequence>();
                 evaluationMeasureNames.addAll(Arrays.asList(MEASURES));
                 final EvaluationMeasure measure;
 
                 if (sampleWithReplacement) {
                     final int numberOfBootstrapSamples = 1000;
                     final ObjectList<double[]> decisionList = new ObjectArrayList<double[]>();
                     final ObjectList<double[]> trueLabelList = new ObjectArrayList<double[]>();
                     measure = new EvaluationMeasure();
                     for (int i = 0; i < numberOfBootstrapSamples; i++) {
                         final DoubleList sampleDecisions = new DoubleArrayList();
                         final DoubleList sampleTrueLabels = new DoubleArrayList();
 
                         buildSample(options, decisions, trueLabels, sampleDecisions, sampleTrueLabels);
                         decisionList.add(sampleDecisions.toDoubleArray());
                         trueLabelList.add(sampleTrueLabels.toDoubleArray());
                     }
                     CrossValidation.evaluate(decisionList, trueLabelList, evaluationMeasureNames, measure, "", true);
                 } else {
                     measure = CrossValidation.testSetEvaluation(decisions.toDoubleArray(),
                             trueLabels.toDoubleArray(), evaluationMeasureNames, true);
                 }
                 final ClassificationTask task = new ClassificationTask();
                 task.setExperimentDataFilename(modelFilenamePrefixNoPath);
                 task.setFirstConditionName(model.getSymbolicClassLabel()[0]);
                 task.setSecondConditionName(model.getSymbolicClassLabel()[1]);
 
                 CrossValidationMode.printHeaders(options, MEASURES, task);
                 CrossValidationMode.printAllStatResults(options, task, model.getGeneList(), measure);
 
                 if (survivalFileName != null && !survivalFileName.equals("-")) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Inside predict process filename = " + survivalFileName);
                     }
                     final SurvivalMeasures survivalMeasures = new SurvivalMeasures(
                             survivalFileName, decisions, trueLabels, idRefs);
                     final List<SurvivalMeasures> survivalMeasuresList =
                             new ArrayList<SurvivalMeasures>();
                     survivalMeasuresList.add(survivalMeasures);
                     maqciiHelper.printSubmissionResults(options, measure, model.getNumberOfFeatures(), 1, survivalMeasuresList);
                 } else {
                     maqciiHelper.printSubmissionResults(options, measure, model.getNumberOfFeatures(), 1, null);
                 }
             }
 
             // CrossValidation.plotRocCurveLOO(decisions.toDoubleArray(), trueLabels.toDoubleArray(), "c:/dev/ti-maqcii/roc.pdf");
 
         } catch (IOException e) {
             LOG.fatal("Error loading model " + modelFilenamePrefix, e);
             System.exit(10);
         } catch (ClassNotFoundException e) {
             LOG.fatal("Error loading model " + modelFilenamePrefix, e);
             System.exit(10);
         } catch (ColumnTypeException e) {
             LOG.fatal("Error processing input file ", e);
             System.exit(10);
         } catch (TypeMismatchException e) {
             LOG.fatal("Error processing input file ", e);
             System.exit(10);
         } catch (InvalidColumnException e) {
             LOG.fatal("Error processing input file ", e);
             System.exit(10);
         }
     }
 
     protected static void buildSample(final DAVOptions options, final DoubleList decisions,
                                       final DoubleList trueLabels, final DoubleList sampleDecisions,
                                       final DoubleList sampleTrueLabels) {
         if (decisions.size() != trueLabels.size()) {
             throw new IllegalArgumentException(
                     " the number of decision values and labels must match.");
         }
 
         sampleDecisions.clear();
         sampleTrueLabels.clear();
 
         final int sampleSize = decisions.size();
         final int[] instanceIndices = new int[sampleSize];
         for (int instanceIndex = 0; instanceIndex < sampleSize; instanceIndex++) {
             instanceIndices[instanceIndex] = (int) (options.randomGenerator.nextDouble() * sampleSize); //[0-sampleSize[  array indexing convention
         }
         for (final int instanceIndex : instanceIndices) {
             sampleDecisions.add(decisions.get(instanceIndex));
             sampleTrueLabels.add(trueLabels.get(instanceIndex));
         }
     }
 
     private void accumulatePredictions(final DAVOptions options, final String[] idRefs,
                                        final DoubleList decisions, final DoubleList trueLabels,
                                        final int numberOfSamples, final BDVModel model) {
         for (int sampleIndex = 0; sampleIndex < numberOfSamples; sampleIndex++) {
             // predict the class of the each training in the input, according to the model:
             final String sampleId = idRefs[sampleIndex];
             // Consider only sampleIds in the specified split:
             if (!splitPlanContainsSampleId(sampleId)) {
                 continue;
             }
 
             final double[] probabilities = new double[2];
             final double decision = model.predict(sampleIndex, probabilities);
             assert decision == 1 || decision == -1 : "decision must be binary.";
 
             final double probability = Math.max(probabilities[0], probabilities[1]);
             final String[] symbolicClassLabel = model.getSymbolicClassLabel();
 
             if (!printStats) {
                 final PredictedItem predictedItem = new PredictedItem(getSplitId(),
                         getSplitType(),
                         getRepeatId(),
                         modelFilenamePrefixNoPath,
                         sampleIndex,
                         sampleId,
                         decision,
                         symbolicClassLabel[convertDecisionToLabelIndex(decision)],
                         probability,
                         convertDecisionToLabelIndex(decision) == 1 ? probability : 1 - probability,    // the model probability that the test instance belongs to class 1
                         trueLabel(sampleId), convertToNumeric(symbolicClassLabel, trueLabel(sampleId)),
                         trueLabel(sampleId).equals(symbolicClassLabel[convertDecisionToLabelIndex(decision)]) ? "correct" : "incorrect",
                         model.getNumberOfFeatures());
                 options.output.println(predictedItem.format());
                 options.output.flush();
             }
 
             if (!"unknown".equals(trueLabel(sampleId))) {
                 decisions.add(probability * decision);
                 trueLabels.add(convertToNumeric(symbolicClassLabel, trueLabel(sampleId)));
             }
         }
     }
 
     private String[] extractIdRefs(final Table processedTable) throws InvalidColumnException {
         // Get the sample IDs before we remove them from the input table:
         final int idRefIndex = processedTable.getColumnIndex("ID_REF");
         return processedTable.getColumnValues(idRefIndex).getStrings();
     }
 
 
     private double convertToNumeric(final String[] symbolicClassLabel, final String label) {
         if (LOG.isTraceEnabled()) {
             LOG.trace("Converting Symbolic: " + ArrayUtils.toString(symbolicClassLabel)
                     + " Label: " + label);
         }
 
         if (label.equals(symbolicClassLabel[0])) {
             return 0;
         } else if (label.equals(symbolicClassLabel[1])) {
             return 1;
         } else {
             // if true labels were not provided, simply return NaN.
             if (sample2TrueLabelMap == null) {
                 return Double.NaN;
             }
             LOG.fatal("Label is not recognized: " + label);
             System.exit(10);
             return -1;
         }
     }
 
     protected String trueLabel(final String sampleId) {
         final String label = sample2TrueLabelMap == null ? "unknown" : sample2TrueLabelMap.get(sampleId.intern());
         if (LOG.isTraceEnabled()) {
             LOG.trace("True label for " + sampleId + " is " + label);
         }
         if (label == null) {
             LOG.warn("Cannot find sampleId " + sampleId + " in true label information, for model  " +
                    this.modelFilenamePrefixNoPath + ". We read true labels from filename: "+trueLabelFilename +"" +
                    "The test set was read from "+testSampleFilename);
             return "unknown";
         } else {
             return label;
         }
     }
 
     private int convertDecisionToLabelIndex(final double decision) {
         return decision == -1 ? 0 : (decision == +1 ? 1 : -1);
     }
 
     public static Map<String, String> readSampleToTrueLabelsMap(final String trueLabelCidFilename) {
         return readSampleToTrueLabelsMap(trueLabelCidFilename, false);
     }
 
     /**
      * Read the true labels file if specified.
      *
      * @param trueLabelCidFilename
      * @param printStats
      */
     private static Map<String, String> readSampleToTrueLabelsMap(
             final String trueLabelCidFilename, final boolean printStats) {
         if (printStats && trueLabelCidFilename == null) {
             System.err.println("True labels must be provided for "
                     + "statistics to be evaluated (--print-stats option).");
             System.exit(1);
         }
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Reading true labels from: " + trueLabelCidFilename);
         }
 
         Map<String, String> sample2TrueLabelMap = null;
         if (trueLabelCidFilename != null) {
             FastBufferedReader labelReader = null;
             try {
                 sample2TrueLabelMap = new Object2ObjectOpenHashMap<String, String>();
                 labelReader = new FastBufferedReader(new FileReader(trueLabelCidFilename));
                 final LineIterator lit = new LineIterator(labelReader);
                 int lineNumber = 0;
                 MutableString line;
                 while (lit.hasNext()) {
                     line = lit.next();
                     if (lineNumber++ == 0 || line.startsWith("#")) {
                         // Skip the first line or comment line
                         continue;
                     }
                     final String[] tokens = line.toString().split("[\t]");
                     final LabelSample s = new LabelSample();
                     s.label = tokens[0];
                     s.sampleId = tokens[1];
                     if (LOG.isTraceEnabled()) {
                         LOG.trace("Tokens: " + ArrayUtils.toString(tokens));
                         LOG.trace("Label: " + s.label + " SampleId: " + s.sampleId);
                     }
                     sample2TrueLabelMap.put(s.sampleId.intern(), s.label);
                 }
             } catch (IOException e) {
                 LOG.fatal("Cannot read true labels in cids format from file: "
                         + trueLabelCidFilename, e);
                 System.exit(10);
             } finally {
                 IOUtils.closeQuietly(labelReader);
             }
         }
         return sample2TrueLabelMap;
     }
 }
