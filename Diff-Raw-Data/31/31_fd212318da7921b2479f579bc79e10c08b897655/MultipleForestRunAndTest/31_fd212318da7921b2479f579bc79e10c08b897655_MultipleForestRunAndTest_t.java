 package featureselection;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import tree.Forest;
 import tree.TreeGrowthControl;
 
 public class MultipleForestRunAndTest
 {
 	/**
 	 * @param confusionMatrix
 	 * @param weights
 	 * @param ctrl
 	 * @param inputFile
 	 * @param seeds
 	 * @param repetitions
 	 * @param negClass
 	 * @param posClass
 	 * @param resultsLocation
 	 * @param mccResultsLocation
 	 * @param analysisBeingRun - 1 for weight and mtry, 2 for sample size and 3 for node size.
 	 */
 	static void forestTraining(Map<String, Map<String, Double>> confusionMatrix,
 			Map<String, Double> weights, TreeGrowthControl ctrl, String inputFile, List<Long> seeds, int repetitions,
 			String negClass, String posClass, String resultsLocation, String mccResultsLocation, int analysisBeingRun)
 	{
 		double cumulativeOOBError = 0.0;
 		List<Double> mccValues = new ArrayList<Double>();
 		long oobTime = 0l;
 
 		Date startTime;
 		Date endTime;
 		for (int j = 0; j < repetitions; j++)
 		{
 			// Get the seed for this repetition.
 			long seed = seeds.get(j);
 
 			startTime = new Date();
 			Forest forest;
 			forest = new Forest(inputFile, ctrl, weights, seed);
 			cumulativeOOBError += forest.oobErrorEstimate;
 			Map<String, Map<String, Double>> oobConfMatrix = forest.oobConfusionMatrix;
     		for (String s : oobConfMatrix.keySet())
     		{
     			Double oldTruePos = confusionMatrix.get(s).get("TruePositive");
     			Double newTruePos = oldTruePos + oobConfMatrix.get(s).get("TruePositive");
     			confusionMatrix.get(s).put("TruePositive", newTruePos);
     			Double oldFalsePos = confusionMatrix.get(s).get("FalsePositive");
     			Double newFalsePos = oldFalsePos + oobConfMatrix.get(s).get("FalsePositive");
     			confusionMatrix.get(s).put("FalsePositive", newFalsePos);
     		}
     		endTime = new Date();
     		oobTime += endTime.getTime() - startTime.getTime();
     		Double oobTP = oobConfMatrix.get(posClass).get("TruePositive");
 			Double oobFP = oobConfMatrix.get(posClass).get("FalsePositive");
 			Double oobTN = oobConfMatrix.get(negClass).get("TruePositive");
 			Double oobFN = oobConfMatrix.get(negClass).get("FalsePositive");
 			mccValues.add((((oobTP * oobTN)  - (oobFP * oobFN)) / Math.sqrt((oobTP + oobFP) * (oobTP + oobFN) * (oobTN + oobFP) * (oobTN + oobFN))));
 		}
 		oobTime /= (double) repetitions;
 
 		// Aggregate OOB results over all the repetitions.
 		cumulativeOOBError /= repetitions;
 		Double oobTP = confusionMatrix.get(posClass).get("TruePositive");
 		Double oobFP = confusionMatrix.get(posClass).get("FalsePositive");
 		Double oobTN = confusionMatrix.get(negClass).get("TruePositive");
 		Double oobFN = confusionMatrix.get(negClass).get("FalsePositive");
 		Double oobSensitivityOrRecall = oobTP / (oobTP + oobFN);
 		Double oobSpecificity = oobTN / (oobTN + oobFP);
 		Double oobAccuracy = (oobTP + oobTN) / (oobTP + oobTN + oobFP + oobFN);
 		Double oobPrecisionOrPPV = oobTP / (oobTP + oobFP);
 		Double oobNegPredictiveVal = oobTN / (oobTN + oobFN);
 		Double oobFHalf = (1 + (0.5 * 0.5)) * ((oobPrecisionOrPPV * oobSensitivityOrRecall) / ((0.5 * 0.5 * oobPrecisionOrPPV) + oobSensitivityOrRecall));;
 		Double oobFOne = 2 * ((oobPrecisionOrPPV * oobSensitivityOrRecall) / (oobPrecisionOrPPV + oobSensitivityOrRecall));
 		Double oobFTwo = (1 + (2 * 2)) * ((oobPrecisionOrPPV * oobSensitivityOrRecall) / ((2 * 2 * oobPrecisionOrPPV) + oobSensitivityOrRecall));
 		Double oobMCC = (((oobTP * oobTN)  - (oobFP * oobFN)) / Math.sqrt((oobTP + oobFP) * (oobTP + oobFN) * (oobTN + oobFP) * (oobTN + oobFN)));
 
 		// Write out the OOB results for this set of repetitions.
 		try
 		{
 			FileWriter resultsOutputFile = new FileWriter(resultsLocation, true);
 			BufferedWriter resultsOutputWriter = new BufferedWriter(resultsOutputFile);
 			if (analysisBeingRun == 1)
 			{
 				resultsOutputWriter.write(String.format("%.5f", weights.get("Positive")));
 				resultsOutputWriter.write("\t");
 				resultsOutputWriter.write(Integer.toString(ctrl.mtry));
 				resultsOutputWriter.write("\t");
 			}
 			else if (analysisBeingRun == 2)
 			{
 				int sampleSize = 0;
 				for (String s : ctrl.sampSize.keySet())
 				{
 					sampleSize += ctrl.sampSize.get(s);
 				}
 				double posClassFraction = ((double) ctrl.sampSize.get(posClass)) / sampleSize;
 				resultsOutputWriter.write(Integer.toString(sampleSize));
 				resultsOutputWriter.write("\t");
 				resultsOutputWriter.write(String.format("%.5f", posClassFraction));
 				resultsOutputWriter.write("\t");
 				resultsOutputWriter.write(String.format("%.5f", weights.get("Positive")));
 				resultsOutputWriter.write("\t");
 			}
 			else if (analysisBeingRun == 3)
 			{
 				resultsOutputWriter.write(Integer.toString(ctrl.minNodeSize));
 				resultsOutputWriter.write("\t");
 			}
 			resultsOutputWriter.write(String.format("%.5f", oobMCC));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobFHalf));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobFOne));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobFTwo));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobAccuracy));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", cumulativeOOBError));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobPrecisionOrPPV));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobSensitivityOrRecall));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobSpecificity));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobNegPredictiveVal));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobTP));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobFP));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobTN));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(String.format("%.5f", oobFN));
 			resultsOutputWriter.write("\t");
 			resultsOutputWriter.write(Long.toString(oobTime));
 			resultsOutputWriter.newLine();
 			resultsOutputWriter.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		// Write out the MCC results for this set of repetitions.
 		try
 		{
 			FileWriter resultsOutputFile = new FileWriter(mccResultsLocation, true);
 			BufferedWriter resultsOutputWriter = new BufferedWriter(resultsOutputFile);
			if (analysisBeingRun == 1)
			{
				resultsOutputWriter.write(String.format("%.5f", weights.get("Positive")));
				resultsOutputWriter.write("\t");
				resultsOutputWriter.write(Integer.toString(ctrl.mtry));
				resultsOutputWriter.write("\t");
			}
			else if (analysisBeingRun == 2)
			{
				int sampleSize = 0;
				for (String s : ctrl.sampSize.keySet())
				{
					sampleSize += ctrl.sampSize.get(s);
				}
				double posClassFraction = ((double) ctrl.sampSize.get(posClass)) / sampleSize;
				resultsOutputWriter.write(Integer.toString(sampleSize));
				resultsOutputWriter.write("\t");
				resultsOutputWriter.write(String.format("%.5f", posClassFraction));
				resultsOutputWriter.write("\t");
				resultsOutputWriter.write(String.format("%.5f", weights.get("Positive")));
				resultsOutputWriter.write("\t");
			}
			else if (analysisBeingRun == 3)
			{
				resultsOutputWriter.write(Integer.toString(ctrl.minNodeSize));
				resultsOutputWriter.write("\t");
			}
 			for (Double d : mccValues)
 			{
 				resultsOutputWriter.write(String.format("%.5f", d));
 				resultsOutputWriter.write("\t");
 			}
 			resultsOutputWriter.newLine();
 			resultsOutputWriter.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 }
