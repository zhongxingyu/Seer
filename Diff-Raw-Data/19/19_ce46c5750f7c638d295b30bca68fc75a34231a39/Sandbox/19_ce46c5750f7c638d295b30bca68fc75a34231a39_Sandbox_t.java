 package org.corssa.predictiontesting;
 
 import java.util.Properties;
 import java.io.InputStreamReader;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.OutputStreamWriter;
 import java.util.Arrays;
 
 /**
  * @author J. Douglas Zechar zechar at usc.edu
  */
 public class Sandbox {
 
     /** Creates a new instance of Sandbox */
     public Sandbox() {
     }
 
     public static void main(String[] args) {
         if (args.length == 0) {
             System.err.println("Usage: Sandbox \"configurationFile\"");
             System.exit(-1);
         }
 
         String configurationFile = args[0];
 
         String forecastFile = "";
         String referenceForecastFile = "";
         boolean executeLTest = false;
         boolean executeMTest = false;
         boolean executeNTest = false;
         boolean executeSTest = false;
         boolean executeRTest = false;
         boolean executeASSTest = false;
         boolean executeMolchanTest = false;
         boolean executeROCTest = false;
         boolean executeGamblingTest = false;
 
         String observationFile = "";
         String resultsFile = "";
 
         try {
             FileInputStream oFIS = new FileInputStream(configurationFile);
             BufferedInputStream oBIS = new BufferedInputStream(oFIS);
             BufferedReader oReader = new BufferedReader(new InputStreamReader(oBIS));
 
             Properties props = new Properties();
             props.load(oFIS);
             oReader.close();
             oBIS.close();
             oFIS.close();
 
             // Set the properties from the configuration file
             forecastFile = props.getProperty("forecastFile");
             referenceForecastFile = props.getProperty("referenceForecastFile");
             observationFile = props.getProperty("observationFile");
             resultsFile = props.getProperty("resultsFile");
             executeLTest = Boolean.parseBoolean(props.getProperty("executeLTest"));
             executeMTest = Boolean.parseBoolean(props.getProperty("executeMTest"));
             executeNTest = Boolean.parseBoolean(props.getProperty("executeNTest"));
             executeRTest = Boolean.parseBoolean(props.getProperty("executeRTest"));
             executeSTest = Boolean.parseBoolean(props.getProperty("executeSTest"));
             executeMolchanTest = Boolean.parseBoolean(props.getProperty("executeMolchanTest"));
             executeASSTest = Boolean.parseBoolean(props.getProperty("executeASSTest"));
             executeROCTest = Boolean.parseBoolean(props.getProperty("executeROCTest"));
             executeGamblingTest = Boolean.parseBoolean(props.getProperty("executeGamblingTest"));
             if ((executeMolchanTest || executeASSTest || executeGamblingTest)
                     && referenceForecastFile.length() == 0) {
                 System.err.println("If you want to execute a Molchan test, an"
                         + " area skill score test, or a gambling score test, you must" +
                         " specify a reference forecast");
                 System.exit(-1);
 
             }
 
             // Clear the results file of any previous results
             FileOutputStream oOutFIS = new FileOutputStream(resultsFile);
             BufferedOutputStream oOutBIS = new BufferedOutputStream(oOutFIS);
             BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(oOutBIS));
             oWriter.close();
             oOutBIS.close();
             oOutFIS.close();
 
         } catch (Exception ex) {
             System.err.println("Error in reading configuration file.");
             ex.printStackTrace();
             System.exit(-1);
         }
 
         if (executeLTest) {
             System.out.print("Executing L-test...");
             Sandbox.executeLTest(forecastFile, observationFile, resultsFile);
             System.out.println("done.");
         }
         if (executeMTest) {
             System.out.print("Executing M-test...");
             Sandbox.executeMTest(forecastFile, observationFile, resultsFile);
             System.out.println("done.");
         }
         if (executeNTest) {
             System.out.print("Executing N-test...");
             Sandbox.executeNTest(forecastFile, observationFile, resultsFile);
             System.out.println("done.");
         }
         if (executeRTest) {
             System.out.print("Executing R-test...");
             Sandbox.executeRTest(forecastFile, referenceForecastFile, observationFile, resultsFile);
             System.out.println("done.");
         }
         if (executeSTest) {
             System.out.print("Executing S-test...");
             Sandbox.executeSTest(forecastFile, observationFile, resultsFile);
             System.out.println("done.");
         }
         if (executeMolchanTest) {
             System.out.print("Executing Molchan-test...");
             Sandbox.executeMolchanTest(forecastFile, referenceForecastFile, observationFile, resultsFile, executeASSTest);
             System.out.println("done.");
         }
         // only execute the ASS test if it's been requested and it's not already been computed during a Molchan test
         if (!executeMolchanTest && executeASSTest) {
             System.out.print("Executing ASS-test...");
             Sandbox.executeASSTest(forecastFile, referenceForecastFile, observationFile, resultsFile);
             System.out.println("done.");
         }
         if (executeROCTest) {
             System.out.print("Executing ROC-test...");
             Sandbox.executeROCTest(forecastFile, observationFile, resultsFile);
             System.out.println("done.");
         }
         if (executeGamblingTest) {
             System.out.print("Executing Gambling-test...");
             Sandbox.executeGamblingTest(forecastFile, referenceForecastFile, observationFile, resultsFile);
             System.out.println("done.");
         }
     }
 
     /**
      * Perform an ROC computation (results in Matlab and XML format)
      */
     public static void rocComputation(String forecastFile, String catalogFile, String xmlFile, float alpha, long seed, boolean saveResultsInMatlab, boolean useMaskBit) {
         // Instantiate the rate forecast of interest from ForecastML file
         System.out.println("Instantiate the rate forecast of interest from ForecastML file");
         Forecast forecast = new Forecast(forecastFile, useMaskBit);
 
         // Instantiate a catalog of observed eqks from a ZMAP file
         System.out.println("Instantiate a catalog of observed eqks from a ZMAP file");
         Catalog catalog = new Catalog(catalogFile);
 
         // Compute ROC and confidence bounds for the given forecast and catalog, and save in Matlab and then XML format
         System.out.println("Compute ROC and confidence bounds for the given forecast and catalog, and save");
         ROCTools.roc(forecast, catalog, xmlFile, alpha, seed, saveResultsInMatlab);
     }
 
     /**
      * Perform a Molchan/ASS computation (results in Matlab and XML format)
      */
     public static void molchanASSComputation(String forecastFile, String[] otherForecastFiles, String catalogFile, String xmlFile, float alpha,
             long seed, boolean saveResultsInMatlab, boolean useMaskBit) {
        // ---Instantiate the reference rate forecast of interest from ForecastML file
 //        System.out.println("Instantiate the reference rate forecast of interest from ForecastML file");
 //        CSEPForecast referenceForecast = new CSEPForecast(forecastFile, useMaskBit);
 
        // ---Instantiate the rate forecasts that we wish to compare to the reference forecast
 //        System.out.println("Instantiate the rate forecasts that we wish to compare to the reference forecast");
 //        CSEPForecast[] forecasts = new CSEPForecast[otherForecastFiles.length];
 //        for (int i = 0; i < forecasts.length; i++) {
 //            forecasts[i] = new CSEPForecast(otherForecastFiles[i]);
 //        }
 
        // ---Instantiate a catalog of observed eqks from a ZMAP file
         System.out.println("Instantiate a catalog of observed eqks from a ZMAP file");
         Catalog catalog = new Catalog(catalogFile);
 
        // ---Compute Molchan/ASS and confidence bounds for the given forecast and catalog, and save in Matlab and then XML format
         System.out.println("Compute Molchan/ASS and confidence bounds for the given forecast and catalog, and save");
         MolchanTools.molchanAndASS(forecastFile, otherForecastFiles, catalog, xmlFile, alpha, seed, saveResultsInMatlab, useMaskBit);
     }
 
     /**
      * Reproduce the results of Zechar & Zhuang 2010 (section 5), demonstrating
      * that, under a strict interpretation, the RTP alarms of Shebalin et al
      * are not significantly skillful
      */
     public static void GamblingScoreExample() {
         // values from Zechar & Zhuang 2010 Table 4
         float[] reference = {0.089620106f, 0.02739825f,
             0.17576914f, 0.040335927f, 0.027406434f, 0.1316506f, 0.19028357f,
             0.068999484f, 0.020263197f, 0.08755122f, 0.036969304f,
             0.20418657f, 0.22770384f, 0.01987749f, 0.16184068f, 0.09572765f,
             0.029539421f, 0.0666289f, 0.009602685f, 0.06380318f, 0.012210446f,
             0.059132785f, 0.04195809f, 0.028321847f, 0.000983f, 0.019273942f};
         short[] outcomes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
             0, 0, 1, 0, 0, 0, 0, 0, 0};
         boolean[] forecast = {true, true, true, true, true, true, true, true,
             true, true, true, true, true, true, true, true, true,
             true, true, true, true, true, true, true, true, true};
         float significance =
                 GamblingScoreTools.significanceOfReputationGain(forecast,
                 outcomes, reference);
         System.out.println(significance);
 
     }
 
     /**
      * Execute an L-test with the specified forecast and specified observation and
      * append the results to the specified results file
      *
      * @param forecastFile path to forecast in ForecastML format
      * @param observationFile path to observation in ZMAP format
      * @param resultsFile path to which the results will be written
      */
     private static void executeLTest(String forecastFile, String observationFile,
             String resultsFile) {
         Forecast forecast = new Forecast(forecastFile, true);
         float[] forecastValues = forecast.values();
         Catalog cat = new Catalog(observationFile);
         short[] observation = forecast.binnedCatalog(cat);
         float gamma = LikelihoodTools.lTest(forecastValues, observation);
 
         try {
             FileOutputStream oOutFIS = new FileOutputStream(resultsFile, true);
             BufferedOutputStream oOutBIS = new BufferedOutputStream(oOutFIS);
             BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(oOutBIS));
             oWriter.write("======\n");
             oWriter.write("L-TEST\n");
             oWriter.write("======\n");
             oWriter.write("gamma = " + gamma + "\n");
 
             oWriter.close();
             oOutBIS.close();
             oOutFIS.close();
 
         } catch (Exception ex) {
             System.out.println("Error in org.corssa.predictiontesting.Sandbox.executeLTest()");
             ex.printStackTrace();
         }
     }
 
     /**
      * Execute an N-test with the specified forecast and specified observation and
      * append the results to the specified results file
      *
      * @param forecastFile path to forecast in ForecastML format
      * @param observationFile path to observation in ZMAP format
      * @param resultsFile path to which the results will be written
      */
     private static void executeNTest(String forecastFile, String observationFile,
             String resultsFile) {
         Forecast forecast = new Forecast(forecastFile, true);
         float[] forecastValues = forecast.values();
         Catalog cat = new Catalog(observationFile);
         short[] observation = forecast.binnedCatalog(cat);
         float[] deltas = LikelihoodTools.nTest(forecastValues, observation);
         float delta1 = deltas[0];
         float delta2 = deltas[1];
 
         try {
             FileOutputStream oOutFIS = new FileOutputStream(resultsFile, true);
             BufferedOutputStream oOutBIS = new BufferedOutputStream(oOutFIS);
             BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(oOutBIS));
             oWriter.write("======\n");
             oWriter.write("N-TEST\n");
             oWriter.write("======\n");
             oWriter.write("delta1, delta2 = " + delta1 + ", " + delta2 + "\n");
 
             oWriter.close();
             oOutBIS.close();
             oOutFIS.close();
         } catch (Exception ex) {
             System.out.println("Error in org.corssa.predictiontesting.Sandbox.executeNTest()");
             ex.printStackTrace();
         }
     }
 
     /**
      * Execute an M-test with the specified forecast and specified observation and
      * append the results to the specified results file
      *
      * @param forecastFile path to forecast in ForecastML format
      * @param observationFile path to observation in ZMAP format
      * @param resultsFile path to which the results will be written
      */
     private static void executeMTest(String forecastFile, String observationFile,
             String resultsFile) {
         Forecast forecast = new Forecast(forecastFile, true);
         float[] forecastValues = forecast.magnitudeForecast();
         Catalog cat = new Catalog(observationFile);
         short[] observation = forecast.binnedMagnitudeCatalog(cat);
         float kappa = LikelihoodTools.conditionalLTest(forecastValues, observation);
 
         try {
             FileOutputStream oOutFIS = new FileOutputStream(resultsFile, true);
             BufferedOutputStream oOutBIS = new BufferedOutputStream(oOutFIS);
             BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(oOutBIS));
             oWriter.write("======\n");
             oWriter.write("M-TEST\n");
             oWriter.write("======\n");
             oWriter.write("kappa = " + kappa + "\n");
 
             oWriter.close();
             oOutBIS.close();
             oOutFIS.close();
 
         } catch (Exception ex) {
             System.out.println("Error in org.corssa.predictiontesting.Sandbox.executeMTest()");
             ex.printStackTrace();
         }
     }
 
     /**
      * Execute an S-test with the specified forecast and specified observation and
      * append the results to the specified results file
      *
      * @param forecastFile path to forecast in ForecastML format
      * @param observationFile path to observation in ZMAP format
      * @param resultsFile path to which the results will be written
      */
     private static void executeSTest(String forecastFile, String observationFile,
             String resultsFile) {
         Forecast forecast = new Forecast(forecastFile, true);
         float[] forecastValues = forecast.spatialForecast();
         Catalog cat = new Catalog(observationFile);
         short[] observation = forecast.binnedSpatialCatalog(cat);
         float zeta = LikelihoodTools.conditionalLTest(forecastValues, observation);
 
         try {
             FileOutputStream oOutFIS = new FileOutputStream(resultsFile, true);
             BufferedOutputStream oOutBIS = new BufferedOutputStream(oOutFIS);
             BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(oOutBIS));
             oWriter.write("======\n");
             oWriter.write("S-TEST\n");
             oWriter.write("======\n");
             oWriter.write("zeta = " + zeta + "\n");
 
             oWriter.close();
             oOutBIS.close();
             oOutFIS.close();
 
         } catch (Exception ex) {
             System.out.println("Error in org.corssa.predictiontesting.Sandbox.executeSTest()");
             ex.printStackTrace();
         }
     }
 
     /**
      * Execute an R-test with the specified forecasts and specified observation and
      * append the results to the specified results file
      *
      * @param forecastAFile path to forecast A in ForecastML format
      * @param forecastBFile path to forecast B in ForecastML format
      * @param observationFile path to observation in ZMAP format
      * @param resultsFile path to which the results will be written
      */
     private static void executeRTest(String forecastAFile, String forecastBFile,
             String observationFile, String resultsFile) {
         Forecast forecastA = new Forecast(forecastAFile, true);
         float[] forecastAValues = forecastA.values();
         Forecast forecastB = new Forecast(forecastBFile, true);
         float[] forecastBValues = forecastB.values();
         Catalog cat = new Catalog(observationFile);
         short[] observation = forecastA.binnedCatalog(cat);
         float alpha_AB = LikelihoodTools.rTest(forecastAValues, forecastBValues, observation);
         float alpha_BA = LikelihoodTools.rTest(forecastBValues, forecastAValues, observation);
 
         try {
             FileOutputStream oOutFIS = new FileOutputStream(resultsFile, true);
             BufferedOutputStream oOutBIS = new BufferedOutputStream(oOutFIS);
             BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(oOutBIS));
             oWriter.write("======\n");
             oWriter.write("R-TEST\n");
             oWriter.write("======\n");
             oWriter.write("alpha_AB = " + alpha_AB + "\n");
             oWriter.write("alpha_BA = " + alpha_BA + "\n");
 
             oWriter.close();
             oOutBIS.close();
             oOutFIS.close();
 
         } catch (Exception ex) {
             System.out.println("Error in org.corssa.predictiontesting.Sandbox.executeRTest()");
             ex.printStackTrace();
         }
     }
 
     /**
      * Execute a Molchan-test with the specified forecast, specified reference, and specified
      * observation and append the results to the specified results file
      *
      * @param forecastFile path to forecast A in ForecastML format
      * @param referenceForecastFile path to forecast B in ForecastML format
      * @param observationFile path to observation in ZMAP format
      * @param resultsFile path to which the results will be written
      */
     private static void executeMolchanTest(String forecastFile,
             String referenceForecastFile, String observationFile,
             String resultsFile, boolean executeASSTest) {
         Forecast forecast = new Forecast(forecastFile, true);
         float[] forecastValues = forecast.values();
 
         Forecast refForecast = new Forecast(referenceForecastFile, true);
         float[] originalReferenceValues = refForecast.values();
         boolean[] forecastOverlapFilter =
                 new boolean[originalReferenceValues.length];
         for (int i = 0; i < forecastOverlapFilter.length; i++) {
             forecastOverlapFilter[i] = true;
         }
 
         // Calculate the spatially overlapping section
         forecastOverlapFilter = Utility.determineForecastOverlapFilter(
                 forecastOverlapFilter, forecastValues);
 
         refForecast = new Forecast(referenceForecastFile, forecastOverlapFilter);
         float[] refForecastValues = refForecast.values();
         forecast = new Forecast(forecastFile, forecastOverlapFilter);
         forecastValues = forecast.values();
 
         Catalog cat = new Catalog(observationFile);
         short[] observation = refForecast.binnedCatalog(cat);
         float[] molchanTrajectory = MolchanTools.molchanTrajectory(
                 forecastValues, refForecastValues, observation);
 
         float[] assTrajectory = MolchanTools.assTrajectory(molchanTrajectory);
         float ass = MolchanTools.ass(molchanTrajectory, 1f);
 
         try {
             FileOutputStream oOutFIS = new FileOutputStream(resultsFile, true);
             BufferedOutputStream oOutBIS = new BufferedOutputStream(oOutFIS);
             BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(oOutBIS));
             oWriter.write("======\n");
             oWriter.write("Molchan-TEST\n");
             oWriter.write("======\n");
             oWriter.write("TAU = " + Arrays.toString(molchanTrajectory) + "\n");
             if (executeASSTest) {
                 oWriter.write("======\n");
                 oWriter.write("ASS-TEST\n");
                 oWriter.write("======\n");
                 oWriter.write("ASS = " + Arrays.toString(assTrajectory) + "\n");
                 oWriter.write("ASS(tau=1) = " + ass + "\n");
             }
 
             oWriter.close();
             oOutBIS.close();
             oOutFIS.close();
 
         } catch (Exception ex) {
             System.out.println("Error in org.corssa.predictiontesting.Sandbox.executeMolchanTest()");
             ex.printStackTrace();
         }
     }
 
     /**
      * Execute an ASS-test with the specified forecast, specified reference, and specified
      * observation and append the results to the specified results file
      *
      * @param forecastFile path to forecast A in ForecastML format
      * @param referenceForecastFile path to forecast B in ForecastML format
      * @param observationFile path to observation in ZMAP format
      * @param resultsFile path to which the results will be written
      */
     private static void executeASSTest(String forecastFile,
             String referenceForecastFile, String observationFile,
             String resultsFile) {
         Forecast forecast = new Forecast(forecastFile, true);
         float[] forecastValues = forecast.values();
 
         Forecast refForecast = new Forecast(referenceForecastFile, true);
         float[] originalReferenceValues = refForecast.values();
         boolean[] forecastOverlapFilter =
                 new boolean[originalReferenceValues.length];
         for (int i = 0; i < forecastOverlapFilter.length; i++) {
             forecastOverlapFilter[i] = true;
         }
 
         // Calculate the spatially overlapping section
         forecastOverlapFilter = Utility.determineForecastOverlapFilter(
                 forecastOverlapFilter, forecastValues);
 
         refForecast = new Forecast(referenceForecastFile, forecastOverlapFilter);
         float[] refForecastValues = refForecast.values();
         forecast = new Forecast(forecastFile, forecastOverlapFilter);
         forecastValues = forecast.values();
 
         Catalog cat = new Catalog(observationFile);
         short[] observation = refForecast.binnedCatalog(cat);
         float[] molchanTrajectory = MolchanTools.molchanTrajectory(
                 forecastValues, refForecastValues, observation);
         float[] assTrajectory = MolchanTools.assTrajectory(molchanTrajectory);
         float ass = MolchanTools.ass(molchanTrajectory, 1f);
 
         try {
             FileOutputStream oOutFIS = new FileOutputStream(resultsFile, true);
             BufferedOutputStream oOutBIS = new BufferedOutputStream(oOutFIS);
             BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(oOutBIS));
             oWriter.write("======\n");
             oWriter.write("ASS-TEST\n");
             oWriter.write("======\n");
             oWriter.write("ASS = " + Arrays.toString(assTrajectory) + "\n");
             oWriter.write("ASS(tau=1) = " + ass + "\n");
 
             oWriter.close();
             oOutBIS.close();
             oOutFIS.close();
 
         } catch (Exception ex) {
             System.out.println("Error in org.corssa.predictiontesting.Sandbox.executeASSTest()");
             ex.printStackTrace();
         }
     }
 
     /**
      * Execute an ROC-test with the specified forecast and specified
      * observation and append the results to the specified results file
      *
      * @param forecastFile path to forecast A in ForecastML format
      * @param observationFile path to observation in ZMAP format
      * @param resultsFile path to which the results will be written
      */
     private static void executeROCTest(String forecastFile, String observationFile, String resultsFile) {
         Forecast forecast = new Forecast(forecastFile, true);
         float[] forecastValues = forecast.values();
         Catalog cat = new Catalog(observationFile);
         short[] observation = forecast.binnedCatalog(cat);
         int[] rocTrajectory = ROCTools.roc(forecastValues, observation);
 
         try {
             FileOutputStream oOutFIS = new FileOutputStream(resultsFile, true);
             BufferedOutputStream oOutBIS = new BufferedOutputStream(oOutFIS);
             BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(oOutBIS));
             oWriter.write("======\n");
             oWriter.write("ROC-TEST\n");
             oWriter.write("======\n");
             oWriter.write("ROC = " + Arrays.toString(rocTrajectory) + "\n");
 
             oWriter.close();
             oOutBIS.close();
             oOutFIS.close();
 
         } catch (Exception ex) {
             System.out.println("Error in org.corssa.predictiontesting.Sandbox.executeROCTest()");
             ex.printStackTrace();
         }
     }
 
     /**
      * Execute an ASS-test with the specified forecast, specified reference, and specified
      * observation and append the results to the specified results file
      *
      * @param forecastFile path to forecast A in ForecastML format
      * @param referenceForecastFile path to forecast B in ForecastML format
      * @param observationFile path to observation in ZMAP format
      * @param resultsFile path to which the results will be written
      */
     private static void executeGamblingTest(String forecastFile,
             String referenceForecastFile, String observationFile,
             String resultsFile) {
         Forecast forecast = new Forecast(forecastFile, true);
         float[] forecastValues = forecast.values();
 
         Forecast refForecast = new Forecast(referenceForecastFile, true);
         float[] originalReferenceValues = refForecast.values();
         boolean[] forecastOverlapFilter =
                 new boolean[originalReferenceValues.length];
         for (int i = 0; i < forecastOverlapFilter.length; i++) {
             forecastOverlapFilter[i] = true;
         }
 
         // Calculate the spatially overlapping section
         forecastOverlapFilter = Utility.determineForecastOverlapFilter(
                 forecastOverlapFilter, forecastValues);
 
         refForecast = new Forecast(referenceForecastFile, forecastOverlapFilter);
         float[] refForecastValues = refForecast.values();
         forecast = new Forecast(forecastFile, forecastOverlapFilter);
         forecastValues = forecast.values();
         Catalog cat = new Catalog(observationFile);
         short[] observation = refForecast.binnedCatalog(cat);
         float gain = GamblingScoreTools.changeInReputation_RateForecasts(forecastValues, observation, refForecastValues);
         float significance = GamblingScoreTools.significanceOfReputationGain(forecastValues, observation, refForecastValues);
 
         try {
             FileOutputStream oOutFIS = new FileOutputStream(resultsFile, true);
             BufferedOutputStream oOutBIS = new BufferedOutputStream(oOutFIS);
             BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(oOutBIS));
             oWriter.write("======\n");
             oWriter.write("Gambling-Test\n");
             oWriter.write("======\n");
             oWriter.write("gain = " + gain + "\n");
             oWriter.write("significance = " + significance + "\n");
 
             oWriter.close();
             oOutBIS.close();
             oOutFIS.close();
 
         } catch (Exception ex) {
             System.out.println("Error in org.corssa.predictiontesting.Sandbox.executeGamblingTest()");
             ex.printStackTrace();
         }
     }
 }
