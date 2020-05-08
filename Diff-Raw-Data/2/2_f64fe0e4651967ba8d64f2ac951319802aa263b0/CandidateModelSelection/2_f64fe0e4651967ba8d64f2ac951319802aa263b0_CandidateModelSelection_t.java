 /*
  * Copyright (C) 2008-2009 Institute for Computational Biomedicine,
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
 
 package org.bdval.modelselection;
 
 import cern.jet.random.engine.MersenneTwister;
 import edu.cornell.med.icb.identifier.DoubleIndexedIdentifier;
 import edu.cornell.med.icb.identifier.IndexedIdentifier;
 import edu.cornell.med.icb.io.TSVReader;
 import edu.cornell.med.icb.stat.ZScoreCalculator;
 import edu.cornell.med.icb.tissueinfo.similarity.ScoredTranscriptBoundedSizeQueue;
 import edu.cornell.med.icb.tissueinfo.similarity.TranscriptScore;
 import edu.mssm.crover.cli.CLI;
 import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
 import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
 import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
 import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
 import it.unimi.dsi.fastutil.objects.ObjectArrayList;
 import it.unimi.dsi.fastutil.objects.ObjectList;
 import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
 import it.unimi.dsi.fastutil.objects.ObjectSet;
 import it.unimi.dsi.lang.MutableString;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.LineIterator;
 import org.apache.log4j.Logger;
 import org.bdval.WithProcessMethod;
 import org.bdval.modelconditions.ProcessModelConditionsMode;
 import org.bdval.tools.convert.OptionsSupport;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.Map;
 
 /**
  * Selects candidate models for replication in different validation datasets. Various model candidate
  * picking strategies are supported:
  * <LI>Selection by cross-validation. The model that has the best cross-validated performance is chosen.
  * <LI>Selection by "color picking". Experimental method which aims to minimize validation surprise on independent validation sets.
  * <p/>
  * In prediction mode, this tool outputs a ranking of up to k models. The model of lowest rank (i.e., 1) is
  * expected to outperform other models for the same endpoint, according to the strategy.
  * <p/>
  * In validation mode, this tool must be provided with performance on a validation set. It estimates model
  * ranking, but also estimates how surprising the actual performance of the top k models is compared to
  * random selections of k models among all available models.
  *
  * @author Fabien Campagne
  *         Date: Jul 28, 2008
  */
 public class CandidateModelSelection implements WithProcessMethod {
 
     /**
      * Performance evaluations by cross-validation with embedded feature seletion.
      */
     private Object2ObjectMap<String, ModelPerformance> cvResults;
 
     /**
      * Maximum performance (value) for each endpoint (key) for results obtained by text-book cross-validation.
      */
     private Object2ObjectMap<String, Map<String, ZScoreCalculator>> cvNormFactorPerfs;
 
     /**
      * Performance evaluations by cross-validation consensus features.
      */
     private Object2ObjectMap<String, ModelPerformance> cvcfResults;
     /**
      * Maximum performance (value) for each endpoint (key) for results obtained by cross-validation with embedded feature seletion.
      */
     private Object2ObjectMap<String, Map<String, ZScoreCalculator>> cvcfNormFactorPerfs;
     /**
      * The test results for models that appear in cvResults and cvcfResults.
      */
     private Object2ObjectMap<String, ModelPerformance> testResults;
     /**
      * Maximum performance (value) for each endpoint (key) for results obtained on the test set.
      */
     private Object2ObjectMap<String, Map<String, ZScoreCalculator>> testNormFactorPerfs;
     private ObjectSet<String> modelIds;
     private ObjectSet<String> datasetNames;
     private ObjectSet<String> endpointNames;
 
     IndexedIdentifier modelIdIndices;
     DoubleIndexedIdentifier reverseIndex;
     private double zScoreThreshold = 2.0;
     private boolean verbose;
     private static final String MCC = "MCC";
     private static final String ACC = "ACC";
     private static final String SENS = "SENS";
     private static final String SPEC = "SPEC";
     private static final String AUC = "AUC";
     private boolean evaluateNormFactorAsMean = true;
 
 
     public CandidateModelSelection() {
         super();
     }
 
     private static final Logger LOG = Logger.getLogger(CandidateModelSelection.class);
 
     public static void main(final String[] args) {
         final CandidateModelSelection tool = new CandidateModelSelection();
         tool.process(args);
     }
 
     enum RewardPerformanceBy {
         MCC_AUC,
         AUC_MCC,
         AUC,
         MCC
     }
 
     enum RankBy {
         MCC_AUC,
         MCC_AUC_STD,
         AUC_MCC,
         AUC_MCC_STD,
         MCC,
         MCC_STD,
         AUC,
         AUC_STD,
         SENSITIVITY,
         SENSITIVITY_STD,
         SPECIFICITY,
         SPECIFICITY_STD,
         ACCURACY,
         ACCURACY_STD,
         MIN_AUC,
         MIN_MCC,
         MIN_AUC_MCC,
         MIN_MCC_AUC,
     }
 
     enum ModelName {
         TrainedOnACZ,
         TrainedOnA,
         TrainedOnABCDEGJKZ,
     }
 
     public void process(final String[] args) {
         final ModelSelectionArguments toolsArgs = new ModelSelectionArguments();
         toolsArgs.k = CLI.getIntOption(args, "-k", 10);
         toolsArgs.cvResultsFilename = CLI.getOption(args, "--cv", "maqcii-submission-cv.txt");
         toolsArgs.cvcfResultsFilename = CLI.getOption(args, "--cvcf", "maqcii-consensus-features-cv.txt");
         toolsArgs.testFilename = CLI.getOption(args, "--test", null);
         toolsArgs.rankStrategyName = CLI.getOption(args, "--rank-by", "CV"); // color
         toolsArgs.datasetName = CLI.getOption(args, "--dataset-name", null);
         toolsArgs.endpointName = CLI.getOption(args, "--endpoint-name", null);
         toolsArgs.rankBy = RankBy.valueOf(CLI.getOption(args, "--rank-candidates-by", "MCC_AUC"));
         toolsArgs.rewardPerformance = RankBy.valueOf(CLI.getOption(args, "--reward-performance", AUC));
         toolsArgs.modelIdMapFile = CLI.getOption(args, "--model-ids-map-file", null);
         toolsArgs.outputFilename = CLI.getOption(args, "-o", "-");
         toolsArgs.pValueFilename = CLI.getOption(args, "-op", "-");
         toolsArgs.rankFilename = CLI.getOption(args, "-or", "-");
         toolsArgs.dumpFilename = CLI.getOption(args, "--dump", null);
         toolsArgs.excludeGeneLists = CLI.isKeywordGiven(args, "--exclude-gene-lists");
         toolsArgs.modelNameString = CLI.getOption(args, "--model-name", ModelName.TrainedOnABCDEGJKZ.toString());
         toolsArgs.modelConditionsFilename = CLI.getOption(args, "--model-conditions", null);
         // list of model ids, comma separated.
         toolsArgs.customRanking = CLI.getOption(args, "--custom-ranking", null);
         // a file with one line per ranking. Format is dataset \t endpoint \t rankingStrategyId \t model-id-rank1,model-id-rank2,...  \n
         toolsArgs.customRankings = parseRankings(CLI.getOption(args, "--custom-ranking-file", null));
         //   toolsArgs.useAllModelsForNull = CLI.isKeywordGiven(args, "--all-models-null");
         process(toolsArgs);
     }
 
     private ObjectList<CustomRanking> parseRankings(final String customRankingFilename) {
         try {
             final ObjectList<CustomRanking> result = new ObjectArrayList<CustomRanking>();
             if (customRankingFilename == null) {
                 return result;
             }
             final LineIterator lineIt = new LineIterator(new FileReader(customRankingFilename));
             while (lineIt.hasNext()) {
                 final String line = lineIt.nextLine();
                 final String[] tokens = line.split("[\t]");
                 final CustomRanking ranking = new CustomRanking();
                 ranking.datasetName = tokens[0];
                 ranking.endpointCode = tokens[1];
                 ranking.typeOfRanking = tokens[2];
                 final String customRanking = tokens[3];
                 ranking.modelIds = new ObjectArrayList<String>(customRanking.split("[,]"));
                 result.add(ranking);
             }
             return result;
         } catch (FileNotFoundException e) {
             System.out.println("Cannot open ranking set file  " + customRankingFilename);
             e.printStackTrace();
             System.exit(10);
         }
         return null;
 
     }
 
     enum RankStrategy {
         CV,
         CVCF,
         COLOR,
         CUSTOM,
         MIN_CV_CVCF,
         COLOR_THRESHOLD,
         MODEL,
     }
 
     private void process(final ModelSelectionArguments toolsArgs) {
         try {
             // load everything, irrespective of endpoint:
             load(toolsArgs, false);
             computeNormalizationFactors();
             reverseIndex = new DoubleIndexedIdentifier(modelIdIndices);
             readeModelConditions(toolsArgs);
             filterGeneLists(toolsArgs);
             dump(toolsArgs);
             toolsArgs.modelName = ModelName.valueOf(toolsArgs.modelNameString);
             toolsArgs.rankStrategy = RankStrategy.valueOf(toolsArgs.rankStrategyName);
             System.out.println(String.format("Processing --rank-by %s  --rank-candidates-by %s --reward-performance %s --test %s --cv %s --cvcf %s --model-conditions %s --exclude-gene-lists %s",
                     toolsArgs.rankStrategy, toolsArgs.rankBy, toolsArgs.rewardPerformance,
                     toolsArgs.testFilename, toolsArgs.cvResultsFilename, toolsArgs.cvcfResultsFilename,
                     toolsArgs.modelConditionsFilename, toolsArgs.excludeGeneLists
             ));
             try {
                 if (!"-".equals(toolsArgs.outputFilename)) {
                     final File out = new File(toolsArgs.outputFilename);
                     if (!out.exists()) {
                         toolsArgs.outputCreated = true;
                     }
                     toolsArgs.output = new PrintWriter(new FileWriter(toolsArgs.outputFilename, true));
                 } else {
                     toolsArgs.output = new PrintWriter(System.out);
                 }
 
                 if (!"-".equals(toolsArgs.pValueFilename)) {
                     final File out = new File(toolsArgs.pValueFilename);
                     if (!out.exists()) {
                         toolsArgs.pValuesOutputCreated = true;
                     }
                     toolsArgs.pValuesOutput = new PrintWriter(new FileWriter(toolsArgs.pValueFilename, true));
                     if (toolsArgs.pValuesOutputCreated) {
                         toolsArgs.pValuesOutput.println("Type\tOrganizationCode\tDatasetName\tEndpointName\tStrategy\tCandidateRanking\tRewardRanking\tupToRank\tP-Value(sum)\tP-Value(order)\taverageTopRewardPerformance\taverageRandomPerformance\tActualOverRandom\ttestSetName");
                     }
 
                 } else {
                     toolsArgs.pValuesOutput = new PrintWriter(System.out);
                 }
 
                 if (!"-".equals(toolsArgs.rankFilename)) {
                     final File out = new File(toolsArgs.rankFilename);
                     if (!out.exists()) {
                         toolsArgs.rankOutputCreated = true;
                     }
                     toolsArgs.rankOutput = new PrintWriter(new FileWriter(toolsArgs.rankFilename, true));
                     if (toolsArgs.rankOutputCreated) {
                         toolsArgs.rankOutput.println(String.format("RANK\tdataset\tendpoint\tmodelId\trankStrategy\tIfRankByModel:ModelName\trankBy\treward\trank\tScoreUsedForRanking\tAUC_CV\tnormAUC_CV\tnormMCC_CV\ttestAUC\ttestMCC\tCandidateModel\tTop5Candidate"));
                     }
 
                 } else {
                     toolsArgs.rankOutput = new PrintWriter(System.out);
                 }
 
             } catch (IOException e) {
                 System.err.println("Cannot write to output file");
                 e.printStackTrace();
             }
 
             // if only one dataset name exists in all performance files, use that if none provided on the command line:
             if (toolsArgs.datasetName != null) {
                 datasetNames = new ObjectOpenHashSet<String>();
                 datasetNames.add(toolsArgs.datasetName);
             }
             // if only one endpoint name exists in all performance files, use that if none provided on the command line:
             if (toolsArgs.endpointName != null) {
                 endpointNames = new ObjectOpenHashSet<String>();
                 endpointNames.add(toolsArgs.endpointName);
             }
 
             for (final String endpointName : endpointNames) {
                 for (final String datasetName : datasetNames) {
                     toolsArgs.endpointName = endpointName;
                     toolsArgs.datasetName = datasetName;
                     // load only the specific dataset/ endpoint
                     load(toolsArgs, true);
                     filterGeneLists(toolsArgs);
                     if (cvResults.size() > 0 && cvcfResults.size() > 0) {
                         reverseIndex = new DoubleIndexedIdentifier(modelIdIndices);
                         System.out.println(String.format("dataset %s endpoint %s", datasetName, endpointName));
 
                         final ObjectList<String> rankedList = rankModels(toolsArgs, toolsArgs.datasetName, toolsArgs.endpointName);
                         if (toolsArgs.hasTestSet() && testResults.size() > 0) {
                             // estimate P-value of selection:
                             pValueEstimation(toolsArgs, rankedList);
                         } else {
                             System.out.println("Cannot evaluate p-values for " + endpointName + " " + datasetName + " no test data.");
                         }
                     }
 
                 }
             }
             toolsArgs.output.close();
             if (toolsArgs.pValuesOutputCreated) {
                 toolsArgs.pValuesOutput.close();
             }
             if (toolsArgs.rankOutputCreated) {
                 toolsArgs.rankOutput.close();
             }
             System.exit(0);
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     private void filterGeneLists(final ModelSelectionArguments toolsArgs) {
         if (toolsArgs.excludeGeneLists) {
             // remove models built with gene lists..
             for (final ModelPerformance model : cvResults.values()) {
                 final String whichGeneList = toolsArgs.modelConditions.get(model.modelId).get("which-gene-list");
                 if (whichGeneList == null || "N/A".equals(whichGeneList)) {
 
                 } else {
                     cvResults.remove(model.modelId);
                     cvcfResults.remove(model.modelId);
                     testResults.remove(model.modelId);
                     modelIds.remove(model.modelId);
                 }
             }
         }
     }
 
 
     private void readeModelConditions(final ModelSelectionArguments toolsArgs) {
         final Map<String, Map<String, String>> modelConditions = null;
         if (toolsArgs.modelConditionsFilename != null) {
             System.out.println("Reading model condition file: " + toolsArgs.modelConditionsFilename);
             toolsArgs.modelConditions = ProcessModelConditionsMode.readModelConditionsFile(toolsArgs.modelConditionsFilename, modelIds);
             addFeatureSelectionFoldColumn(toolsArgs.modelConditions);
             addFeatureSelectionStatTypeColumn(toolsArgs.modelConditions);
             addFeatureClassifierTypeColumn(toolsArgs.modelConditions);
         } else {
             System.out.println("Model condition file not specified.");
         }
     }
 
     private void addFeatureClassifierTypeColumn(final Map<String, Map<String, String>> modelConditions) {
         if (modelConditions == null) {
             return;
         }
         for (final Map<String, String> modelCondition : modelConditions.values()) {
             final String wekaClassifier = modelCondition.get("weka-class");
             final String classifierType;
             if (wekaClassifier == null || "N/A".equals(wekaClassifier)) {
                 classifierType = "LibSVM";
             } else {
                 final int index = wekaClassifier.lastIndexOf('.') + 1;
                 classifierType = wekaClassifier.substring(index, wekaClassifier.length());
             }
 
             modelCondition.put("classifier-type", classifierType);
 
             final String svmParameters = modelCondition.get("classifier-parameters");
             final String svmDefaultCParameter;
             if (svmParameters.contains("C=")) {
                 svmDefaultCParameter = "false";
             } else {
                 svmDefaultCParameter = "true";
             }
             modelCondition.put("svm-default-C-parameter", svmDefaultCParameter);
 
         }
     }
 
     private void computeNormalizationFactors() {
         cvNormFactorPerfs = new Object2ObjectOpenHashMap<String, Map<String, ZScoreCalculator>>();
         for (final ModelPerformance cvPerf : cvResults.values()) {
             pushValue(cvNormFactorPerfs, cvPerf);
         }
         calculateStats(cvNormFactorPerfs);
         cvcfNormFactorPerfs = new Object2ObjectOpenHashMap<String, Map<String, ZScoreCalculator>>();
         for (final ModelPerformance cvcfPerf : cvcfResults.values()) {
             pushValue(cvcfNormFactorPerfs, cvcfPerf);
         }
         calculateStats(cvcfNormFactorPerfs);
         testNormFactorPerfs = new Object2ObjectOpenHashMap<String, Map<String, ZScoreCalculator>>();
         if (testResults != null) {
             for (final ModelPerformance testPerf : testResults.values()) {
                 pushValue(testNormFactorPerfs, testPerf);
             }
             calculateStats(testNormFactorPerfs);
         }
 
     }
 
     private void calculateStats(final Map<String, Map<String, ZScoreCalculator>> perfs) {
         for (final Map<String, ZScoreCalculator> calculatorMap : perfs.values()) {
             final String[] measures = {MCC, ACC, SENS, SPEC, AUC};
 
             for (final String measure : measures) {
                 calculatorMap.get(measure).calculateStats();
             }
         }
     }
 
     private void pushValue
             (
                     final Object2ObjectMap<String, Map<String, ZScoreCalculator>> maxPerfs,
                     final ModelPerformance perf) {
         Map<String, ZScoreCalculator> currentMaxPerfCalculator = maxPerfs.get(perf.endpoint);
         if (currentMaxPerfCalculator == null) {
             currentMaxPerfCalculator = new Object2ObjectOpenHashMap<String, ZScoreCalculator>();
             currentMaxPerfCalculator.put(MCC, new ZScoreCalculator());
             currentMaxPerfCalculator.put(AUC, new ZScoreCalculator());
             currentMaxPerfCalculator.put(ACC, new ZScoreCalculator());
             currentMaxPerfCalculator.put(SENS, new ZScoreCalculator());
             currentMaxPerfCalculator.put(SPEC, new ZScoreCalculator());
             maxPerfs.put(perf.endpoint, currentMaxPerfCalculator);
         }
 
         currentMaxPerfCalculator.get(MCC).observe(nanToZero(perf.mcc));
         currentMaxPerfCalculator.get(ACC).observe(nanToZero(perf.accuracy));
         currentMaxPerfCalculator.get(SENS).observe(nanToZero(perf.sens));
         currentMaxPerfCalculator.get(SPEC).observe(nanToZero(perf.spec));
         currentMaxPerfCalculator.get(AUC).observe(nanToZero(perf.auc));
 
 
     }
 
     private double nanToZero(final double mcc) {
         if (mcc != mcc) {
             return 0;
         } else {
             return mcc;
         }
     }
 
     private void dump(final ModelSelectionArguments toolsArgs) {
         if (toolsArgs.dumpFilename != null) {
             System.out.println("Writing integrated dataset to " + toolsArgs.dumpFilename);
             try {
 
 
                 final PrintWriter writer = new PrintWriter(new FileWriter(toolsArgs.dumpFilename));
                 final ObjectList<String> modelConditionColumnNames = getModelConditionColumnNames(toolsArgs.modelConditions);
                 writer.append(
                         String.format(
                                 "dataset\tendpoint\tmodelId\tnumFeaturesInModel\t" +
                                         "MCC_CV\tACC_CV\tSens_CV\tSpec_CV\tAUC_CV\t" +
                                        "norm_MCC_CV\tnorm_ACC_CV\tnorm_Sens_CV\tnorm_Spec_CV\tnorm_AUC_CV\tT&T_accuracy_bias\t" +
                                         "MCC_CVCF\tACC_CVCF\tSens_CVCF\tSpec_CVCF\tAUC_CVCF\t" +
                                         //   "max_MCC_CVCF\tmax_ACC_CVCF\tmax_Sens_CVCF\tmax_Spec_CVCF\tmax_AUC_CVCF\t" +
                                         "norm_MCC_CVCF\tnorm_ACC_CVCF\tnorm_Sens_CVCF\tnorm_Spec_CVCF\tnorm_AUC_CVCF\t" +
                                         "MCC_validation\tACC_validation\tSens_validation\tSpec_validation\tAUC_validation\t" +
 
                                         //    "max_MCC_validation\tmax_ACC_validation\tmax_Sens_validation\tmax_Spec_validation\tmax_AUC_validation\t" +
                                         "norm_MCC_validation\tnorm_ACC_validation\tnorm_Sens_validation\tnorm_Spec_validation\tnorm_AUC_validation\t" +
                                         "delta_MCC_CVCF_CV\tdelta_ACC_CVCF_CV\tdelta_Sens_CVCF_CV\tdelta_Spec_CVCF_CV\tdelta_AUC_CVCF_CV\t" +
                                         "MCC_CV_stdev\tACC_CV_stdev\tSens_CV_stdev\tSpec_CV_stdev\tAUC_CV_stdev" +          // no tab intended
                                         getModelConditionHeaders(toolsArgs.modelConditions, modelConditionColumnNames) + "\n"
                         ));
                 for (final String modelId : modelIds) {
                     final ModelPerformance cvPerf = this.cvResults.get(modelId);
                     final ModelPerformance cvcfPerf = this.cvcfResults.get(modelId);
                     final String datasetName = cvPerf == null ? "unknown" : cvPerf.dataset;
                     final String endpointCode = cvPerf == null ? "unknown" : cvPerf.endpoint;
                     int numActualFeatures = cvPerf == null ? 0 : cvPerf.actualNumberOfFeaturesInModel;
                     if (numActualFeatures == -1) {
                         // get it from CVCF instead:
                         numActualFeatures = this.cvcfResults.get(modelId).actualNumberOfFeaturesInModel;
                     }
                     // modelId CV(MCC, Sens, Spec, AUC) CVCF() validation()
                     final double deltaCVCF_CV_mcc = getNormalizedMeasure(modelId, cvcfResults, cvcfNormFactorPerfs, MeasureName.MCC) - getNormalizedMeasure(modelId, cvResults, cvNormFactorPerfs, MeasureName.MCC);
                     final double deltaCVCF_CV_acc = getNormalizedMeasure(modelId, cvcfResults, cvcfNormFactorPerfs, MeasureName.ACC) - getNormalizedMeasure(modelId, cvResults, cvNormFactorPerfs, MeasureName.ACC);
                     final double deltaCVCF_CV_sens = getNormalizedMeasure(modelId, cvcfResults, cvcfNormFactorPerfs, MeasureName.SENS) - getNormalizedMeasure(modelId, cvResults, cvNormFactorPerfs, MeasureName.SENS);
                     final double deltaCVCF_CV_spec = getNormalizedMeasure(modelId, cvcfResults, cvcfNormFactorPerfs, MeasureName.SPEC) - getNormalizedMeasure(modelId, cvResults, cvNormFactorPerfs, MeasureName.SPEC);
                     final double deltaCVCF_CV_auc = getNormalizedMeasure(modelId, cvcfResults, cvcfNormFactorPerfs, MeasureName.AUC) - getNormalizedMeasure(modelId, cvResults, cvNormFactorPerfs, MeasureName.AUC);
 
                     writer.append(String.format("%s\t%s\t%s\t%d\t%s\t%s\t%s\t%s\t%f\t%f\t%f\t%f\t%f\t%s%s\n",
                             datasetName, endpointCode, modelId,
                             numActualFeatures,
                             formatStats(modelId, this.cvResults, this.cvNormFactorPerfs),
                             formatBias(cvPerf),
                             formatStats(modelId, this.cvcfResults, this.cvcfNormFactorPerfs),
                             formatStats(modelId, this.testResults, this.testNormFactorPerfs),
                             deltaCVCF_CV_mcc, deltaCVCF_CV_acc, deltaCVCF_CV_sens, deltaCVCF_CV_spec, deltaCVCF_CV_auc,
                             formatStdev(modelId, this.cvResults),
                             getModelConditionColumns(modelId, modelConditionColumnNames, toolsArgs.modelConditions)));
                 }
                 writer.flush();
                 writer.close();
 
             } catch (IOException e) {
                 e.printStackTrace();
             }
             System.exit(0);
         }
     }
 
     private String formatBias(ModelPerformance cvPerf) {
         if (cvPerf.bias == cvPerf.bias) {
             return String.format("%f", cvPerf.bias);
         } else {
             // NaN
             return "NaN";
         }
     }
 
     private void addFeatureSelectionFoldColumn
             (final Map<String, Map<String, String>> modelConditions) {
         if (modelConditions == null) {
             return;
         }
         for (final Map<String, String> modelCondition : modelConditions.values()) {
             final String cacheDir = modelCondition.get("cache-dir");
             if (cacheDir.contains("fs=true")) {
                 modelCondition.put("feature-selection-fold", "true");
 
             } else {
                 modelCondition.put("feature-selection-fold", "false");
             }
         }
     }
 
     private void addFeatureSelectionStatTypeColumn
             (final Map<String, Map<String, String>> modelConditions) {
         if (modelConditions == null) {
             return;
         }
         for (final Map<String, String> modelCondition : modelConditions.values()) {
             final String seqFile = modelCondition.get("sequence-file");
             if (seqFile.contains("ttest")) {
                 modelCondition.put("feature-selection-type", "t-test");
             }
             if (seqFile.contains("FC+P")) {
                 modelCondition.put("feature-selection-type", "FC+P");
             } else if (seqFile.contains("foldchange")) {
                 modelCondition.put("feature-selection-type", "fold-change");
             } else if (seqFile.contains("minmax")) {
                 modelCondition.put("feature-selection-type", "min-max");
             } else if (seqFile.contains("pathways")) {
                 modelCondition.put("feature-selection-type", "pathways");
             } else if (seqFile.contains("svmglobal") ||
                     seqFile.contains("baseline")) {
                 modelCondition.put("feature-selection-type", "SVM-weights");
             } else if (seqFile.contains("svmiterative")) {
                 modelCondition.put("feature-selection-type", "RFE");
             } else if (seqFile.contains("weka")) {
                 modelCondition.put("feature-selection-type", "t-test");
             } else if (seqFile.contains("genetic-algorithm")) {
                 modelCondition.put("feature-selection-type", "ga-wrapper");
             }
         }
     }
 
     private MutableString getModelConditionColumns
             (final String
                     modelId,
              final ObjectList<String> modelConditionColumnNames,
              final Map<String, Map<String, String>> modelConditions) {
 
         final MutableString columnValues = new MutableString();
         if (modelConditions == null) {
             return columnValues;
         }
 
         final Map<String, String> modelCondition = modelConditions.get(modelId);
         if (modelCondition == null) {
             return columnValues;
         }
         for (final String columnName : modelConditionColumnNames) {
 
             columnValues.append('\t');
             final String string = normalizeConditionValue(columnName, modelCondition.get(columnName));
             columnValues.append(string != null ? string : "N/A");
         }
         return columnValues;
     }
 
     private String normalizeConditionValue
             (final String
                     columnName, final String
                     value) {
         if (value == null || columnName == null) {
             return null;
         }
         if ("sequence-file".equals(columnName)) {
             // remove the path to the sequence file. The path is not useful for downstream analyses
             final String toReplace = value.replaceAll("fs=true", "fs").replaceAll("fs=false", "fs");
             return FilenameUtils.getBaseName(toReplace) + ".sequence";
         } else {
             return value;
         }
     }
 
     private MutableString getModelConditionHeaders
             (final Map<String, Map<String, String>> modelConditions, final ObjectList<String> columnNames) {
         final MutableString result = new MutableString();
         if (modelConditions == null) {
             return result;
         }
         for (final String columnName : columnNames) {
             result.append('\t');
             result.append(columnName);
 
         }
         return result;
     }
 
     private ObjectList<String> getModelConditionColumnNames
             (final Map<String, Map<String, String>> modelConditions) {
         final ObjectList<String> columnNames = new ObjectArrayList<String>();
         if (modelConditions == null) {
             return columnNames;
         }
         for (final Map<String, String> modelCondition : modelConditions.values()) {
             for (final String columnName : modelCondition.keySet()) {
                 if (!columnNames.contains(columnName)) {
                     columnNames.add(columnName);
                 }
 
             }
         }
         Collections.sort(columnNames);
         return columnNames;
     }
 
     private MutableString formatStats
             (final String
                     modelId, final Object2ObjectMap<String, ModelPerformance> resultsMap,
              final Object2ObjectMap<String, Map<String, ZScoreCalculator>> maxPerfs) {
         final MutableString result = new MutableString();
         final ModelPerformance perfs = resultsMap.get(modelId);
         final MutableString defaultValue = new MutableString("NaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN"
                 //+"\tNaN\tNaN\tNaN\tNaN\tNaN"
         );
 
 
         if (perfs != null) {
             final Map<String, ZScoreCalculator> calculators = maxPerfs.get(perfs.endpoint);
             final ModelPerformance maxPerf = calculateNormalizationFactor(calculators);
             if (maxPerf != null) {
                 result.append(String.format("%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f",
                         perfs.mcc, perfs.accuracy, perfs.sens, perfs.spec, perfs.auc,
                         //maxPerf.mcc, maxPerf.accuracy, maxPerf.sens, maxPerf.spec, maxPerf.auc,
                         (perfs.mcc / maxPerf.mcc), (perfs.accuracy / maxPerf.accuracy),
                         (perfs.sens / maxPerf.sens), (perfs.spec / maxPerf.spec),
                         (perfs.auc / maxPerf.auc)));
 
                 return result;
             } else {
                 //return new MutableString("\t\t\t");
                 return defaultValue;
             }
         } else {
             //return new MutableString("\t\t\t");
             return defaultValue;
         }
     }
 
     private MutableString formatStdev(final String
             modelId, final Object2ObjectMap<String, ModelPerformance> resultsMap
     ) {
         final MutableString result = new MutableString();
         final ModelPerformance perfs = resultsMap.get(modelId);
         final MutableString defaultValue = new MutableString("NaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN\tNaN"
                 //+"\tNaN\tNaN\tNaN\tNaN\tNaN"
         );
 
 
         if (perfs != null) {
             result.append(String.format("%f\t%f\t%f\t%f\t%f",
                     perfs.mccStd,
                     perfs.accuracyStd,
                     perfs.sensStd,
                     perfs.specStd,
                     perfs.aucStd));
 
 
             return result;
 
         } else {
             //return new MutableString("\t\t\t");
             return defaultValue;
         }
     }
 
     enum MeasureName {
         MCC, AUC, SENS, ACC, SPEC
     }
 
 
     public double getNormalizedMeasure
             (final String
                     modelId, final Object2ObjectMap<String, ModelPerformance> resultsMap,
              final Object2ObjectMap<String, Map<String, ZScoreCalculator>> normPerfs, final MeasureName
                     measure) {
         final ModelPerformance cvPerf = resultsMap.get(modelId);
         double result = Double.NaN;
         if (cvPerf != null) {
             final Map<String, ZScoreCalculator> calculators = normPerfs.get(cvPerf.endpoint);
             final ModelPerformance normPerf = calculateNormalizationFactor(calculators);
             if (normPerf != null) {
                 switch (measure) {
                     case MCC:
                         result = (cvPerf.mcc / normPerf.mcc);
                         break;
                     case ACC:
                         result = (cvPerf.accuracy / normPerf.accuracy);
                         break;
                     case SENS:
                         result = (cvPerf.sens / normPerf.sens);
                         break;
                     case SPEC:
                         result = (cvPerf.spec / normPerf.spec);
                         break;
                     case AUC:
                         result = (cvPerf.auc / normPerf.auc);
                         break;
                 }
             }
         }
         return result;
     }
 
     private ModelPerformance calculateNormalizationFactor
             (final Map<String, ZScoreCalculator> calculators) {
         final ModelPerformance result = new ModelPerformance();
         if (evaluateNormFactorAsMean) {
             // normalized value of 1 indicates mean performance.
             result.mcc = calculators.get(MCC).mean();
             result.accuracy = calculators.get(ACC).mean();
             result.sens = calculators.get(SENS).mean();
             result.spec = calculators.get(SPEC).mean();
             result.auc = calculators.get(AUC).mean();
 
         } else {
             // use max estimator, maximum normalized value is 1.0
             result.mcc = calculators.get(MCC).max();
             result.accuracy = calculators.get(ACC).max();
             result.sens = calculators.get(SENS).max();
             result.spec = calculators.get(SPEC).max();
             result.auc = calculators.get(AUC).max();
         }
 
 
         return result;
     }
 
     private void pValueEstimation
             (
                     final ModelSelectionArguments toolsArgs,
                     final ObjectList<String> rankedList) {
         final int numModels = modelIds.size();
         final MersenneTwister randomGenerator = new MersenneTwister();
         int counterScoreOrder = 0;
         int counterSumOfScores = 0;
         final double maxCouter = 1000;
         if (toolsArgs.pValuesOutputCreated) {
             toolsArgs.pValuesOutput.println("Type\tDatasetName\tEndpointName\tStrategy\tCandidateRanking\tRewardRanking\tupToRank\tP-Value(sum)\tP-Value(order)\taverageTopRewardPerformance\taverageRandomPerformance\tActualOverRandom\ttestSetName");
         }
 
         for (int upToRank = 1; upToRank < Math.min(toolsArgs.k, rankedList.size()) + 1; upToRank++) {
 
             counterScoreOrder = 0;
             counterSumOfScores = 0;
 
             verbose = false;
             final double scoreActual = evaluateScore(rankedList, upToRank, toolsArgs) / Math.min(upToRank, rankedList.size());
             final ZScoreCalculator averageScoreRandom = new ZScoreCalculator();
             verbose = false;
             for (int i = 0; i < maxCouter; i++) {
                 final ObjectList<String> randomModelPicks = pickModels(randomGenerator, numModels, Math.min(toolsArgs.k, upToRank));
 /* if (compareScoreOrder(randomModelPicks, rankedList, upToRank, toolsArgs)) {
     counterScoreOrder++;
 }*/
                 final double scoreRandom = evaluateScore(randomModelPicks, upToRank, toolsArgs) / Math.min(upToRank, randomModelPicks.size());
                 averageScoreRandom.observe(scoreRandom);
                 if (scoreRandom >= scoreActual) {
                     counterSumOfScores++;
                 }
             }
             averageScoreRandom.calculateStats();
             System.out.println("Rank " + upToRank + " Average random score:" + averageScoreRandom.mean());
             System.out.println("Rank " + upToRank + " Actual score:" + scoreActual);
             final double pValue_Order = ((double) counterScoreOrder) / maxCouter;
             final double pValue_Sum = ((double) counterSumOfScores) / maxCouter;
             final String testFileShort = FilenameUtils.getBaseName(toolsArgs.testFilename);
             toolsArgs.pValuesOutput.println(String.format("P-VALUE\t%s\t%s\t%s\t%s\t%s\t%d\t%f\t%f\t%f\t%f\t%f\t%s",
                     toolsArgs.datasetName,
                     toolsArgs.endpointName,
                     toolsArgs.rankStrategy,
                     toolsArgs.rankBy,
                     toolsArgs.rewardPerformance,
                     upToRank,
                     pValue_Sum,
                     pValue_Order,
                     scoreActual, averageScoreRandom.mean(),
                     scoreActual / averageScoreRandom.mean(),
                     testFileShort));
             toolsArgs.pValuesOutput.flush();
         }
     }
 
 
     private boolean compareSumOfScores
             (
                     final ObjectList<String> randomModelPicks,
                     final ObjectList<String> actualModelList,
                     final int upToRank,
                     final ModelSelectionArguments toolsArgs) {
         final double scoreRandom = evaluateScore(randomModelPicks, upToRank, toolsArgs) / randomModelPicks.size();
         final double scoreActual = evaluateScore(actualModelList, upToRank, toolsArgs) / actualModelList.size();
         return scoreRandom >= scoreActual;
     }
 
     private boolean compareScoreOrder
             (
                     final ObjectList<String> randomModelPicks,
                     final ObjectList<String> actualModelList,
                     final int upToRank,
                     final ModelSelectionArguments toolsArgs) {
         boolean value = true;
         for (int rank = 0; rank < upToRank; rank++) {
             if (rank >= actualModelList.size()) {
                 {
                     continue;
                 }
             }
             final String randomModelIdAtRank = randomModelPicks.get(rank);
             final String actualModelIdAtRank = actualModelList.get(rank);
 
             final ModelPerformance randomPerf = testResults.get(randomModelIdAtRank);
             final ModelPerformance actualPerf = testResults.get(actualModelIdAtRank);
             if (actualPerf != null) {
                 {
                     if (randomPerf == null) {
                         value = false;
                         continue;
                     }
                     final double randomModelPerf =
                             getRewardPerformanceMeasure(toolsArgs, randomPerf);
                     final double actualModelPerf =
                             getRewardPerformanceMeasure(toolsArgs, actualPerf);
                     value &= randomModelPerf >= actualModelPerf;
                 }
             }
         }
         return value;
     }
 
     private double evaluateScore
             (
                     final ObjectList<String> randomModelPicks,
                     final int upToRank,
                     final ModelSelectionArguments toolsArgs) {
         double value = 0;
         for (int rank = 0; rank < upToRank; rank++) {
             final String modelId = randomModelPicks.get(rank);
             final ModelPerformance testPerf = testResults.get(modelId);
             assert testPerf != null : "performance in test set must exist for model id: " + modelId;
 
             final double rewardPerformance = getRewardPerformanceMeasure(toolsArgs, testPerf);
             if (verbose) {
                 System.out.println(
                         "Rank " + rank + " Observing model-id: " + modelId + " actual performance: "
                                 + rewardPerformance);
             }
             value += rewardPerformance;
 
         }
         return value;
     }
 
     private ObjectList<String> pickModels
             (
                     final MersenneTwister randomGenerator,
                     final int numModels,
                     int k) {
         k = Math.min(k, testResults.size());
         final ObjectList<String> randomList = new ObjectArrayList<String>();
         do {
             final int modelIndex = (int) Math.round(randomGenerator.nextDouble() * numModels);
             final MutableString id = reverseIndex.getId(modelIndex);
             if (id == null) {
                 continue;
             }
             final String modelId = id.toString();
             if (!randomList.contains(modelId) && testResults.containsKey(modelId)) {
                 randomList.add(modelId);
 
             }
         } while (randomList.size() < k);
         return randomList;
     }
 
     private ObjectList<String> rankModels
             (
                     final ModelSelectionArguments toolsArgs,
                     final String dataset,
                     final String endpoint) {
         final ObjectList<String> rankedList = new ObjectArrayList<String>();
 
         switch (toolsArgs.rankStrategy) {
             case CV: {
                 final ScoredTranscriptBoundedSizeQueue queue = new ScoredTranscriptBoundedSizeQueue(toolsArgs.k);
                 for (final String modelId : modelIds) {
                     final ModelPerformance cvPerf = cvResults.get(modelId);
 
                     if (cvPerf != null && cvPerf.dataset.equals(dataset) && cvPerf.endpoint.equals(endpoint)) {
                         final double score = getCandidateRankingPerformanceMeasure(toolsArgs, cvPerf);
                         if (testResults != null && testResults.get(modelId) == null) {
                             // if test set is provided, skip models which have no test perfs.
                             continue;
                         }
                         if (score != score) {
                             //do not enqueue NaN
                             continue;
                         }
 
                         queue.enqueue(reverseIndex.getIndex(modelId), score);
 
                     } else {
 
                         // model not for dataset/endpoint
                     }
                 }
                 dequeue(queue, rankedList);
             }
             break;
             case CVCF: {
                 final ScoredTranscriptBoundedSizeQueue queue = new ScoredTranscriptBoundedSizeQueue(toolsArgs.k);
                 for (final String modelId : modelIds) {
                     final ModelPerformance cvcfPerf = cvcfResults.get(modelId);
                     if (cvcfPerf != null && cvcfPerf.dataset.equals(dataset) && cvcfPerf.endpoint.equals(endpoint)) {
                         final double score = getCandidateRankingPerformanceMeasure(toolsArgs, cvcfPerf);
                         if (testResults != null && testResults.get(modelId) == null) {
                             // if test set is provided, skip models which have no test perfs.
                             continue;
                         }
 
                         if (score != score) {
                             //do not enqueue NaN
                             continue;
                         }
                         queue.enqueue(reverseIndex.getIndex(modelId), score);
                     }
                 }
                 dequeue(queue, rankedList);
             }
             break;
 
 
             case MIN_CV_CVCF: {
                 final ScoredTranscriptBoundedSizeQueue queue = new ScoredTranscriptBoundedSizeQueue(toolsArgs.k);
                 for (final String modelId : modelIds) {
                     final ModelPerformance cvcfPerf = cvcfResults.get(modelId);
                     final ModelPerformance cvPerf = cvResults.get(modelId);
                     final ModelPerformance minPerf = new ModelPerformance();
                     if (cvcfPerf == null || cvPerf == null) {
                         continue;
                     }
 
                     minPerf.auc = Math.min(cvPerf.auc, cvcfPerf.auc);
                     minPerf.mcc = Math.min(cvPerf.mcc, cvcfPerf.mcc);
                     minPerf.accuracy = Math.min(cvPerf.accuracy, cvcfPerf.accuracy);
                     minPerf.sens = Math.min(cvPerf.sens, cvcfPerf.sens);
                     minPerf.spec = Math.min(cvPerf.spec, cvcfPerf.spec);
                     minPerf.rmse = Math.min(cvPerf.rmse, cvcfPerf.rmse);
                     if (cvcfPerf != null && cvcfPerf.dataset.equals(dataset) && cvcfPerf.endpoint.equals(endpoint)) {
                         final double score = getCandidateRankingPerformanceMeasure(toolsArgs, cvcfPerf);
                         if (testResults != null && testResults.get(modelId) == null) {
                             // if test set is provided, skip models which have no test perfs.
                             continue;
                         }
                         if (score != score) {
                             //do not enqueue NaN
                             continue;
                         }
                         queue.enqueue(reverseIndex.getIndex(modelId), score);
                     }
                 }
                 dequeue(queue, rankedList);
             }
             break;
 
             case COLOR_THRESHOLD: {
                 final ScoredTranscriptBoundedSizeQueue queue = new ScoredTranscriptBoundedSizeQueue(toolsArgs.k);
                 for (final String modelId : modelIds) {
                     final ModelPerformance cvcfPerf = cvcfResults.get(modelId);
                     final ModelPerformance cvPerf = cvResults.get(modelId);
                     if (cvcfPerf == null || cvPerf == null) {
                         continue;
                     }
 
 
                     if (cvcfPerf != null && cvcfPerf.dataset.equals(dataset) && cvcfPerf.endpoint.equals(endpoint)) {
                         if (testResults != null && testResults.get(modelId) == null) {
                             // if test set is provided, skip models which have no test perfs.
                             continue;
                         }
                         final double color = getColor(cvcfPerf, cvPerf, toolsArgs);
                         if (color > 0.0) {
                             continue;
                         }
 
 // reject models where color is larger than 0.05.
                         final double score = getCandidateRankingPerformanceMeasure(toolsArgs, cvcfPerf);
                         if (score != score) {
                             //do not enqueue NaN
                             continue;
                         }
                         queue.enqueue(reverseIndex.getIndex(modelId), score);
                     }
                 }
                 dequeue(queue, rankedList);
             }
             break;
             case COLOR:
                 colorPicking(toolsArgs, dataset, endpoint, rankedList);
 
                 break;
             case MODEL:
                 model(toolsArgs, dataset, endpoint, rankedList);
                 break;
             case CUSTOM:
 
                 ObjectList<String> customList = toolsArgs.getCustomModelRankingList(dataset, endpoint);
 
                 if (customList != null) {
                     for (final String modelId : customList) {
                         if (testResults != null && testResults.get(modelId) != null) {
                             // if test set is provided, skip models which have no test perfs.
                             rankedList.add(modelId);
                         }
                     }
 
                 } else {
                     System.err.println("Ignoring custom rankings for " + dataset + " " + endpoint +
                             "Custom ranking list must be provided on the command line (provide a comma separated list of model ids with the --custom-ranking option, or the --custom-ranking-file)");
                     customList = new ObjectArrayList<String>();
                 }
 
                 break;
         }
         int rank = 1;
         System.out.println("Selected model ids (in order or increasing rank):");
 
         for (final String modelId : rankedList) {
             final ModelPerformance cvcfPerf = cvcfResults.get(modelId);
             final ModelPerformance cvPerf = cvResults.get(modelId);
             final Map<String, ZScoreCalculator> calculators = cvNormFactorPerfs.get(cvPerf.endpoint);
             final ModelPerformance maxPerf = calculateNormalizationFactor(calculators);
             final ModelPerformance testPerf = testResults != null ? testResults.get(modelId) : null;
 
             toolsArgs.rankOutput.println(String.format("RANK\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%s\t%s",
                     toolsArgs.datasetName,
                     toolsArgs.endpointName,
                     modelId,
                     toolsArgs.rankStrategy,
                     (toolsArgs.rankStrategy == RankStrategy.MODEL ? toolsArgs.modelNameString : "N/A"),
                     toolsArgs.rankBy,
                     toolsArgs.rewardPerformance,
                     rank,
                     getPerformanceByChosenStrategy(toolsArgs, cvPerf, cvcfPerf),
                     cvPerf.auc,
                     maxPerf == null ? Double.NaN : cvPerf.auc / maxPerf.auc,
                     maxPerf == null ? Double.NaN : cvPerf.mcc / maxPerf.mcc,
                     testPerf == null ? Double.NaN : testPerf.auc,
                     testPerf == null ? Double.NaN : testPerf.mcc,
                     rank == 1 ? "Y" : "N",
                     rank <= 5 ? "Y" : "N"));
             toolsArgs.rankOutput.flush();
             rank++;
         }
         return rankedList;
     }
 
     private void model
             (
                     final ModelSelectionArguments toolsArgs,
                     final String dataset,
                     final String endpoint,
                     final ObjectList<String> rankedList) {
         final ScoredTranscriptBoundedSizeQueue queue = new ScoredTranscriptBoundedSizeQueue(toolsArgs.k);
         for (final String modelId : modelIds) {
             final ModelPerformance cvPerf = cvResults.get(modelId);
             final ModelPerformance cvcfPerf = cvcfResults.get(modelId);
 
             if (cvPerf != null && cvPerf.dataset.equals(dataset) && cvPerf.endpoint.equals(endpoint)) {
                 final double score = getModelRankingPerformance(toolsArgs, cvPerf, cvcfPerf);
 
                 if (testResults != null && testResults.get(modelId) == null) {
                     // if test set is provided, skip models which have no test perfs.
                     continue;
                 }
                 if (score != score) {
                     //do not enqueue NaN
                     continue;
                 }
 
                 queue.enqueue(reverseIndex.getIndex(modelId), score);
 
             } else {
 
                 // model not for dataset/endpoint
             }
         }
         dequeue(queue, rankedList);
     }
 
     public double getPerformanceByChosenStrategy(final ModelSelectionArguments toolsArgs, final ModelPerformance cvPerf,
                                                  final ModelPerformance cvcfPerf) {
 
         final double score = Double.NaN;
         switch (toolsArgs.rankStrategy) {
             case MODEL:
                 return getModelRankingPerformance(toolsArgs, cvPerf, cvcfPerf);
             case CV:
                 return getCandidateRankingPerformanceMeasure(toolsArgs, cvPerf);
             case CVCF:
                 return getCandidateRankingPerformanceMeasure(toolsArgs, cvcfPerf);
         }
         return score;
     }
 
     private double getModelRankingPerformance
             (
                     final ModelSelectionArguments toolsArgs,
                     final ModelPerformance cvPerf,
                     final ModelPerformance cvcfPerf) {
         final String modelId = cvPerf.modelId;
         final double norm_AUC_CV = cvPerf.auc;
 // if model has no associated model condition, not much we can do:
         if (toolsArgs.modelConditions.get(modelId) == null) {
             System.err.println("Warning: model id is not found in model conditions: " + modelId);
             return Double.NaN;
         }
 
         final double delta_AUC_CVCF_CV = (cvcfPerf == null || cvcfPerf == null) ? 0 : cvcfPerf.auc - cvPerf.auc;
 // model trained on Hamner endpoint A:
 
         double predictedPerformance = Double.NaN;
         final BMFCalibrationModel bmfModel = BMFCalibrationModel.load(toolsArgs.modelNameString);
         final Object2DoubleMap<String> map = new Object2DoubleOpenHashMap<String>();
         map.put("actualNumberOfFeaturesInModel", cvPerf.actualNumberOfFeaturesInModel);
         map.put("norm_auc", norm_AUC_CV);
         map.put("delta_auc_cvcf_cv", delta_AUC_CVCF_CV);
 
         predictedPerformance = bmfModel.calibrateEstimate(toolsArgs, modelId, map);
         switch (toolsArgs.modelName) {
 
             case TrainedOnACZ:
                 predictedPerformance = modelTrainedOnACZ(toolsArgs, cvPerf.actualNumberOfFeaturesInModel,
                         modelId, norm_AUC_CV, delta_AUC_CVCF_CV);
                 break;
 
             default:
                 System.err.println("Model name must be specified.");
                 System.exit(1);
         }
 
         return predictedPerformance;
     }
 
 
     private double modelTrainedOnACZ
             (final ModelSelectionArguments toolsArgs, final int numFeaturesInModel,
              final String modelId, final double norm_auc_cv,
              final double delta_auc_cvcf_cv) {
 
         return 0.459989454833173 +
                 -0.00083173460079395 * numFeaturesInModel +
                 0.536622935579494 * norm_auc_cv +
                 match(value(toolsArgs, modelId, "classifier-type"),
                         "KStar", 0.0135523789492576,
                         "LibSVM", 0.0449462916662318,
                         "Logistic", -0.0681672665697239,
                         "LogitBoost", 0.074435720784513,
                         "NaiveBayesUpdateable", -0.118305143431788,
                         "RandomForest", 0.0535380186015092
                 )
                 + match(value(toolsArgs, modelId, "feature-selection-fold"),
                 "false", 0.00810507959138625,
                 "true", -0.00810507959138625)
                 + match(value(toolsArgs, modelId, "feature-selection-type"),
                 "fold-change", 0.0410682769672393,
                 "ga-wrapper", 0.0197988254395799,
                 "pathways", -0.0326917219922266,
                 "RFE", -0.0420895743946066,
                 "SVM-weights", 0.00335502231253732,
                 "t-test", 0.0105591716674767)
                 + match(value(toolsArgs, modelId, "svm-default-C-parameter"),
                 "false", -0.0255816480713828,
                 "true", 0.0255816480713828
         )
                 + 0.614443519922211 * delta_auc_cvcf_cv;
     }
 
 
     private double modelTrainedOnZ
             (final ModelSelectionArguments
                     toolsArgs, final String
                     modelId, final double norm_AUC_CV,
              final double delta_AUC_CVCF_CV) {
         final double predictedPerformance = 0.470987343174626 +
                 match(value(toolsArgs, modelId, "num-features"),
                         5, 0,
                         10, 0.00184503480230022,
                         20, -0.00511622355407557,
                         30, -0.00174779452052177,
                         40, -0.00394711098519292,
                         50, -0.00929902003179512,
                         60, -0.00393336552478679,
                         70, -0.00657227247623677,
                         80, -0.00887633148802223,
                         90, -0.00846155158003597,
                         100, -0.00623028013059541)
                 + match(value(toolsArgs, modelId, "sequence-file"),
                 "baseline.sequence", 0.0352283587893766,
                 "foldchange-genetic-algorithm.sequence", 0.00991939764640446,
                 "foldchange-svmglobal.sequence", 0.0312692428977958,
                 "foldchange-svmiterative.sequence", 0.02223336836696,
                 "genelist-genetic-algorithm.sequence", -0.0351890394599106,
                 "genelist-svmglobal.sequence", -0.0515009066490208,
                 "genetic-algorithm.sequence", -0.0648320810055556,
                 "minmax-svmglobal.sequence", -0.0117747449135709,
                 "pathways-global-svm-weights.sequence", -0.0339363032447067,
                 "pathways-ttest-svmglobal.sequence", -0.0996858611450744,
                 "svmiterative.sequence", 0.0469616379363532,
                 "ttest-genetic-algorithm.sequence", 0.0512143363922545,
                 "ttest-svmglobal.sequence", 0.0355241910638877,
                 "ttest-svmiterative.sequence", 0.0191487707626885,
                 "ttest-weka-classifier-fs=true.sequence", 0.0153367546275863,
                 "ttest-weka-classifier-fs=false.sequence", 0.0153367546275863,
                 "tuneC-baseline.sequence", 0.0300828779345321)
                 + match(value(toolsArgs, modelId, "feature-selection-fold"),
                 "false", -0.00170665764201027,
                 "true", 0.00170665764201027)
 
                 + 0.43221506988658 * norm_AUC_CV
                 + 0.134626233131847 *
                 delta_AUC_CVCF_CV;
         return predictedPerformance;
     }
 
     public static double match
             (final String
                     variableValue, final Object... values) {
 
         for (int i = 0; i < values.length; i += 2) {
 
             final Object key = values[i];
             final String keyAsString = key.toString();
             if (keyAsString.equals(variableValue)) {
                 final Object d = values[i + 1];
                 if (d instanceof Double) {
                     final Double value = (Double) d;
                     return value;
                 }
                 if (d instanceof Integer) {
                     final Integer value = (Integer) d;
                     return value;
                 }
             }
         }
         System.out.println("Returning default value for " + variableValue);
         return Double.NaN;
     }
 
     private String value
             (final ModelSelectionArguments
                     toolsArgs, final String
                     modelId, final String
                     variableName) {
         final String value = toolsArgs.modelConditions.get(modelId).get(variableName);
         return normalizeConditionValue(variableName, value);
     }
 
 
     private double getCandidateRankingPerformanceMeasure
             (
                     final ModelSelectionArguments toolsArgs,
                     final ModelPerformance cvPerf) {
         return getPerformanceMeasure(toolsArgs, cvPerf, toolsArgs.rankBy);
     }
 
     private double getRewardPerformanceMeasure
             (
                     final ModelSelectionArguments toolsArgs,
                     final ModelPerformance cvPerf) {
 
         return getPerformanceMeasure(toolsArgs, cvPerf, toolsArgs.rewardPerformance);
     }
 
     private double getPerformanceMeasure
             (
                     final ModelSelectionArguments toolsArgs,
                     final ModelPerformance cvPerf,
                     final RankBy evaluationMeasure) {
         assert cvPerf != null : "CV performance must not be null";
         switch (evaluationMeasure) {
             case ACCURACY:
                 return cvPerf.accuracy;
             case ACCURACY_STD:
                 return cvPerf.accuracyStd;
             case SENSITIVITY:
                 return cvPerf.sens;
             case SENSITIVITY_STD:
                 return cvPerf.sens - cvPerf.sensStd;
             case SPECIFICITY:
                 return cvPerf.spec;
             case SPECIFICITY_STD:
                 return cvPerf.spec - cvPerf.specStd;
             case MCC_AUC:
             case AUC_MCC:
                 return cvPerf.auc + cvPerf.mcc;
             case MCC_AUC_STD:
             case AUC_MCC_STD:
                 return cvPerf.auc + cvPerf.mcc - cvPerf.aucStd - cvPerf.mccStd;
             case MCC:
                 return cvPerf.mcc;
             case MCC_STD:
                 return cvPerf.mcc - cvPerf.mccStd;
             case AUC:
                 return cvPerf.auc;
             case AUC_STD:
                 return cvPerf.auc - cvPerf.aucStd;
             default:
                 return Double.NaN;
         }
     }
 
     private void colorPicking
             (
                     final ModelSelectionArguments toolsArgs,
                     final String
                             dataset, final String
                     endpoint, final ObjectList<String> rankedList) {
 // filter models to consider only those that match
         final ObjectSet<String> selectedModels = new ObjectOpenHashSet<String>();
 
         for (final String modelId : modelIds) {
             final ModelPerformance cvPerf = cvResults.get(modelId);
             final ModelPerformance cvcfPerf = cvcfResults.get(modelId);
             if (cvcfPerf == null || cvPerf == null) {
                 continue;        // ignore models for which cv or cvcf is not available.
             }
             if (testResults != null && testResults.get(modelId) == null) {
                 // if test set is provided, skip models which have no test perfs.
                 continue;
             }
             final double candidateCVCF_CV = getColor(cvcfPerf, cvPerf, toolsArgs);
 
             final ObjectList<String> closeByModels = new ObjectArrayList<String>();
             findCloseByModels(cvcfPerf, closeByModels);
             final ZScoreCalculator zScore = new ZScoreCalculator();
             if (closeByModels.size() < 3) {
                 continue; // must have at least 5 models close to the candidate model for consideration
             }
 
             for (final String closeByModel : closeByModels) {
                 if (closeByModel != modelId) {
                     final ModelPerformance cvCandidate = cvResults.get(closeByModel);
                     final ModelPerformance cvcfCandidate = cvcfResults.get(closeByModel);
                     final double cvcf_cv = getColor(cvcfCandidate, cvCandidate, toolsArgs);
 
                     zScore.observe(cvcf_cv);
                 }
             }
             zScore.calculateStats();
 /*
 2.11492605399409 + -0.033540795772973 * :MCC of training +
 0.447815977375321 * :Accuracy of training + 0.0249917093282766 * :
 Sensitivity of training + -1.03020749336459 * :
 Specificity of training + -0.175039381197443 * :AUC of training +
 0.726068337659521 * :MCC of CV + -0.0339802852938674 * :
 Accuracy of CV + -0.580308612876182 * :Sensitivity of CV + -
 0.534197867221285 * :Specificity of CV + -0.121039750799259 * :
 AUC of CV + 0.0190346231277872 * :Name( "MCC of CV-CF" ) +
 0.0615443439898757 * :Name( "Accuracy of CV-CF" ) + -
 0.143680970756426 * :Name( "Sensitivity of CV-CF" ) + -
 0.42412068678125 * :Name( "Specificity of CV-CF" ) +
 0.412237795283542 * :Name( "AUC of CV-CF" )hhl
 */
             final double zScoreCandidate = zScore.zScore(candidateCVCF_CV);
 // System.out.println(String.format("%s %f %f %f %f %f %f ", modelId, zScore.min(), zScore.mean(), zScore.max(), zScore.max() - candidateCVCF_CV, zScoreCandidate, candidateCVCF_CV));
             if (zScoreCandidate <= -zScoreThreshold)
 
             {
                 System.out.println(String.format("SELECTED %s %f %f %f %f %f %f ", modelId, zScore.min(), zScore.mean(), zScore.max(), zScore.max() - candidateCVCF_CV, zScoreCandidate, candidateCVCF_CV));
                 selectedModels.add(modelId);
             }
         }
         final ScoredTranscriptBoundedSizeQueue queue = new ScoredTranscriptBoundedSizeQueue(toolsArgs.k);
         for (final String modelId : selectedModels) {
             final ModelPerformance cvcfPerf = cvcfResults.get(modelId);
             final ModelPerformance cvPerf = cvResults.get(modelId);
             if (cvcfPerf.dataset.equals(dataset) && cvcfPerf.endpoint.equals(endpoint)) {
 
 //   if (hasBestColor(modelId, selectedModels, cvcfPerf, cvPerf)) {
                 final double score = getRankOrderScore(cvcfPerf, cvPerf, toolsArgs);
                 if (score != score) {
                     // do not enqueue NaN
                     continue;
                 }
                 queue.enqueue(reverseIndex.getIndex(modelId), score);
 //    }
             }
         }
         dequeue(queue, rankedList);
     }
 
     private boolean hasBestColor
             (
                     final String
                             modelId, final ObjectSet<String> selectedModels,
                     final ModelPerformance
                             cvcfPerf, final ModelPerformance
                     cvPerf, final ModelSelectionArguments toolsArgs) {
         final ObjectList<String> closeByModels = new ObjectArrayList<String>();
         findCloseByModels(cvcfPerf, closeByModels);
         closeByModels.retainAll(selectedModels);
         if (closeByModels.size() == 0) {
             return true;
         }
 // closeByModels.add(modelId);
         final ZScoreCalculator zScore = new ZScoreCalculator();
         for (final String closeByModel : closeByModels) {
 
             final ModelPerformance cvCandidate = cvResults.get(closeByModel);
             final ModelPerformance cvcfCandidate = cvcfResults.get(closeByModel);
             final double cvcf_cv = getColor(cvcfCandidate, cvCandidate, toolsArgs);
             zScore.observe(cvcf_cv);
         }
         zScore.calculateStats();
         final boolean hasBestColor = (getColor(cvcfPerf, cvPerf, toolsArgs) >= zScore.max());
         return hasBestColor;
     }
 
     private double getScore
             (
                     final ModelPerformance
                             cvcfPerf, final ModelPerformance
                     cvPerf, final ModelSelectionArguments toolsArgs) {
         return cvcfPerf.auc + cvcfPerf.mcc - 1 * ((getColor(cvcfPerf, cvPerf, toolsArgs)));
     }
 
     private double getRankOrderScore
             (
                     final ModelPerformance cvcfPerf,
                     final ModelPerformance cvPerf,
                     final ModelSelectionArguments toolsArgs) {
         switch (toolsArgs.rewardPerformance) {
             case MCC:
                 return cvcfPerf.mcc;
             case AUC:
                 return cvcfPerf.auc;
             default:
             case AUC_MCC:
             case MCC_AUC:
                 return cvcfPerf.mcc + cvcfPerf.auc;
             case MIN_AUC:
                 return Math.min(cvcfPerf.auc, cvPerf.auc);
             case MIN_MCC:
                 return Math.min(cvcfPerf.mcc, cvPerf.mcc);
             case MIN_AUC_MCC:
             case MIN_MCC_AUC:
 
                 return Math.min(cvcfPerf.mcc, cvPerf.mcc) + Math.min(cvcfPerf.auc, cvPerf.auc);
 
         }
 
     }
 
     private double getColor
             (
                     final ModelPerformance
                             cvcfPerf, final ModelPerformance
                     cvPerf, final ModelSelectionArguments toolsArgs) {
 
         if (cvcfPerf == null || cvPerf == null) {
             return Double.NaN;
         }
         return 2 * (cvcfPerf.mcc - cvPerf.mcc);
 /*   switch (toolsArgs.rewardPerformance) {
     //   case MCC:
     //       return 2 * (cvcfPerf.auc - cvPerf.auc);
     //   case AUC:
     return 2 * (cvcfPerf.mcc - cvPerf.mcc);
     //    case AUC_MCC:
     //    case MCC_AUC:
     //        return 1 * (cvcfPerf.mcc - cvPerf.mcc + cvcfPerf.auc - cvPerf.auc);
     //    default:
     //        return 2 * (cvcfPerf.mcc - cvPerf.mcc);
 }*/
 
 // return 2* ( cvcfPerf.mcc - cvPerf.mcc+ cvcfPerf.auc - cvPerf.auc);
 // return 1* ( cvcfPerf.mcc - cvPerf.mcc+ cvcfPerf.auc - cvPerf.auc);
 //return 1* Math.abs(( cvcfPerf.mcc - cvPerf.mcc+ cvcfPerf.auc - cvPerf.auc));
     }
 
     /**
      * Find models close to targetModel in CVCF performance space.
      *
      * @param targetModelCVCFPerf
      * @param nearbyModelIds      Ids of models that are close to the target model in CVCF space.
      */
     private void findCloseByModels
             (
                     final ModelPerformance
                             targetModelCVCFPerf, final ObjectList<String> nearbyModelIds) {
 
         for (final String modelId : modelIds) {
             final ModelPerformance candidatePerformance = cvcfResults.get(modelId);
             if (candidatePerformance == null) {
                 continue;
             }
             if (distanceUnderThreshold(targetModelCVCFPerf, candidatePerformance)) {
                 if (!modelId.equals(targetModelCVCFPerf.modelId)) {
                     // exclude the target from the list of close by models.
                     nearbyModelIds.add(modelId);
                 }
             }
         }
     }
 
     private boolean distanceUnderThreshold(
             final ModelPerformance targetModelCVCFPerf, final ModelPerformance candidatePerformance) {
         final double target_AUC_CVCF = targetModelCVCFPerf.auc;
         final double target_MCC_CVCF = targetModelCVCFPerf.mcc;
 
         final double candidate_AUC_CVCF = candidatePerformance.auc;
         final double candidate_MCC_CVCF = candidatePerformance.mcc;
         final double distance = Math.sqrt(Math.pow(target_AUC_CVCF - candidate_AUC_CVCF, 2) + Math.pow(target_MCC_CVCF - candidate_MCC_CVCF, 2));
         final double threshold = (candidate_AUC_CVCF + candidate_MCC_CVCF) * 0.03;
         return (distance < threshold);
     }
 
     private void dequeue
             (
                     final ScoredTranscriptBoundedSizeQueue
                             queue, final ObjectList<String> rankedList) {
         while (!queue.isEmpty()) {
             final TranscriptScore t = queue.dequeue();
             final String modelId = reverseIndex.getId(t.transcriptIndex).toString();
             rankedList.add(0, modelId);
 //   System.out.println("dequeuing " + modelId + " score: " + t.score);
         }
     }
 
     private void load(final ModelSelectionArguments toolsArgs,
                       final boolean filterByEndpoint) throws FileNotFoundException {
         if (filterByEndpoint) {
 
             modelIds.clear();
         }
         modelIdIndices = new IndexedIdentifier();
         cvResults = loadStatistics(toolsArgs.cvResultsFilename, true, filterByEndpoint, toolsArgs);
         cvcfResults = loadStatistics(toolsArgs.cvcfResultsFilename, true, filterByEndpoint, toolsArgs);
 
         if (toolsArgs.testFilename != null) {
             if (toolsArgs.modelIdMapFile == null) {
                 // standalone file with test statistics only:
                 testResults = loadStatistics(toolsArgs.testFilename, false,
                         filterByEndpoint, toolsArgs);
             } else {
                 // MAQCII combined file with CV perf and validation together. Combines data for all analysis teams:
 
                 testResults = loadOtherStatistics(toolsArgs.testFilename, toolsArgs.modelIdMapFile, false,
                         filterByEndpoint, toolsArgs, false);
 
             }
         }
     }
 
     private Object2ObjectMap<String, ModelPerformance> loadOtherStatistics
             (
                     final String filename,
                     final String modelIdMapFile,
                     final boolean readDatasetEndpoint,
                     final boolean filterByEndpoint,
                     final ModelSelectionArguments toolsArgs,
                     final boolean loadAllModels) throws FileNotFoundException {
 
         Map<String, String> modelIdMap = null;
         try {
             modelIdMap = OptionsSupport.readMapFileFromTsv(new File(modelIdMapFile), true, 0, 1);
 
         } catch (IOException e) {
             System.out.println("Error reading model id mapping file." + modelIdMapFile);
             System.exit(1);
         }
         if (modelIds == null) {
             modelIds = new ObjectOpenHashSet<String>();
         }
 
         if (datasetNames == null) {
             datasetNames = new ObjectOpenHashSet<String>();
         }
         if (endpointNames == null) {
             endpointNames = new ObjectOpenHashSet<String>();
         }
 
         final Object2ObjectMap<String, ModelPerformance> modelPerfs = new Object2ObjectOpenHashMap<String, ModelPerformance>();
         final TSVReader reader = new TSVReader(new FileReader(filename), '\t');
         reader.setCommentPrefix("OrganizationCode");
         try {
             while (reader.hasNext()) {
                 if (reader.isCommentLine()) {
                     reader.skip();
                 } else {
                     reader.next();
                     final ModelPerformance measure = new ModelPerformance();
 
                     reader.getString();
                     final String maqciiModelId = reader.getString();
                     if (!modelIdMap.containsKey(maqciiModelId)) {
                         continue;
                     }
                     skipFields(reader, 3);
 
                     measure.dataset = reader.getString();
 
                     measure.endpoint = reader.getString();
                     if (filterByEndpoint && measure.dataset.equals(toolsArgs.datasetName) && measure.endpoint.equals(toolsArgs.endpointName) ||
                             !filterByEndpoint) {
                         if (readDatasetEndpoint) {
                             datasetNames.add(measure.dataset);
                         }
                         if (readDatasetEndpoint) {
                             endpointNames.add(measure.endpoint);
                         }
 
                         measure.excel = reader.getString();
                         skipFields(reader, 19);
 
                         measure.mcc = tryGetDouble(reader);
                         measure.accuracy = tryGetDouble(reader);
                         measure.sens = tryGetDouble(reader);
                         measure.spec = tryGetDouble(reader);
                         measure.auc = tryGetDouble(reader);
                         measure.rmse = tryGetDouble(reader);
 
                         final double validationPresent = tryGetDouble(reader);
                         if (validationPresent == 1) {
                             // Some validation data was provided for this model: register the model id in the validation dataset.
                             measure.modelId = modelIdMap.get(maqciiModelId);
                             modelPerfs.put(measure.modelId, measure);
                             modelIds.add(measure.modelId);
                             final MutableString modelId = new MutableString(measure.modelId);
                             modelIdIndices.registerIdentifier(modelId);
                         }
                     }
 
                 }
             }
             return modelPerfs;
         } catch (IOException e) {
             System.err.println("Cannot read file " + filename);
             System.exit(1);
             return null;
         }
     }
 
     private double tryGetDouble
             (
                     final TSVReader reader) {
         try {
             return reader.getDouble();
         } catch (NumberFormatException e) {
             return Double.NaN;
         }
     }
 
     private void skipFields
             (
                     final TSVReader reader,
                     final int n) {
         for (int i = 0; i < n; i++) {
             reader.getString();
         }
     }
 
     private Object2ObjectMap<String, ModelPerformance> loadStatistics
             (
                     final String filename,
                     final boolean readDatasetEndpoint,
                     final boolean filterByEndpoint,
                     final ModelSelectionArguments toolsArgs) throws FileNotFoundException {
         if (modelIds == null) {
             modelIds = new ObjectOpenHashSet<String>();
         }
 
         if (datasetNames == null) {
             datasetNames = new ObjectOpenHashSet<String>();
         }
         if (endpointNames == null) {
             endpointNames = new ObjectOpenHashSet<String>();
         }
 
         final Object2ObjectMap<String, ModelPerformance> modelPerfs = new Object2ObjectOpenHashMap<String, ModelPerformance>();
         final TSVReader reader = new TSVReader(new FileReader(filename), '\t');
         reader.setCommentPrefix("OrganizationCode");
         try {
             while (reader.hasNext()) {
                 if (reader.isCommentLine()) {
                     reader.skip();
                 } else {
                     reader.next();
                     reader.getString();
                     final ModelPerformance measure = new ModelPerformance();
 
                     measure.dataset = reader.getString();
 
                     measure.endpoint = reader.getString();
                     if (filterByEndpoint && measure.dataset.equals(toolsArgs.datasetName) && measure.endpoint.equals(toolsArgs.endpointName) ||
                             !filterByEndpoint) {
                         if (readDatasetEndpoint) {
                             datasetNames.add(measure.dataset);
                         }
                         if (readDatasetEndpoint) {
                             endpointNames.add(measure.endpoint);
                         }
                         measure.excel = reader.getString();
                         measure.mcc = reader.getDouble();
                         measure.accuracy = reader.getDouble();
                         measure.sens = reader.getDouble();
                         measure.spec = reader.getDouble();
                         measure.auc = reader.getDouble();
                         measure.rmse = reader.getDouble();
                         measure.mccStd = reader.getDouble();
                         measure.accuracyStd = reader.getDouble();
                         measure.sensStd = reader.getDouble();
                         measure.specStd = reader.getDouble();
                         measure.aucStd = reader.getDouble();
                         measure.rmseStd = reader.getDouble();
                         reader.getString();
                         reader.getString();
                         measure.actualNumberOfFeaturesInModel = reader.getInt();
                         for (int i = 0; i < 4; i++) {
                             reader.getString();
                         }
                         measure.modelId = reader.getString();
                         for (int i = 0; i < 3; i++) {
                             reader.getString();
                         }
                         measure.bias = reader.getDouble();
                         modelPerfs.put(measure.modelId, measure);
                         modelIds.add(measure.modelId);
                         final MutableString modelId = new MutableString(measure.modelId);
                         modelIdIndices.registerIdentifier(modelId);
                     }
 
                 }
             }
             return modelPerfs;
         } catch (IOException e) {
             System.err.println("Cannot read file " + filename);
             System.exit(1);
             return null;
         }
     }
 
     private class ModelPerformance {
         String dataset;
         String endpoint;
         String excel;
         double accuracy;
         double sens;
         double spec;
         double auc;
         double mcc;
         double rmse;
         String modelId;
         double bias;
         /**
          * The actual number of features used in this model. May differ from the number of features listed in model condition files, for instance
          * for gene list based models: the gene list may overlap the chip for less probesets than the number indicated to build the model.
          */
         int actualNumberOfFeaturesInModel;
         public double aucStd;
         public double mccStd;
         public double accuracyStd;
         public double sensStd;
         public double specStd;
         public double rmseStd;
     }
 }
