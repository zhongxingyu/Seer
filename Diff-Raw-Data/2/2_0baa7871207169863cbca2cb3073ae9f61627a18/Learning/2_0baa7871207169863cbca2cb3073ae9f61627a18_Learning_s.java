 package org.uncertweb.et.learning;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.uncertweb.et.Config;
 import org.uncertweb.et.ConfigException;
 import org.uncertweb.et.MATLAB;
 import org.uncertweb.et.design.Design;
 import org.uncertweb.et.design.NormalisedDesign;
 import org.uncertweb.et.process.NormalisedProcessEvaluationResult;
 import org.uncertweb.et.process.ProcessEvaluationResult;
 import org.uncertweb.matlab.MLException;
 import org.uncertweb.matlab.MLRequest;
 import org.uncertweb.matlab.MLResult;
 import org.uncertweb.matlab.value.MLArray;
 import org.uncertweb.matlab.value.MLCell;
 import org.uncertweb.matlab.value.MLMatrix;
 import org.uncertweb.matlab.value.MLString;
 import org.uncertweb.matlab.value.MLValue;
 
 public class Learning {
 
 	private static final Logger logger = LoggerFactory.getLogger(Learning.class);
 
 	public static LearningResult learn(Design design, ProcessEvaluationResult evaluationResult, String selectedOutputIdentifier, String covarianceFunction, double lengthScale, double processVariance, Double nuggetVariance, String meanFunction, boolean normalisation) throws LearningException {
 			// setup x
 			Design x = design;
 			
 			// setup y
 			ProcessEvaluationResult y = evaluationResult;
 			
 			// normalise?
 			if (normalisation) {
 				logger.info("Normalising inputs and outputs...");
 				x = NormalisedDesign.fromDesign(x);
 				y = NormalisedProcessEvaluationResult.fromProcessEvaluationResult(y);
 			}
 
 			// setup request
			MLRequest request = new MLRequest("learn_emulator", 5);
 			request.addParameter(new MLString((String)Config.getInstance().get("matlab", "gpml_path")));
 
 			// add x
 			request.addParameter(new MLMatrix(x.getPoints()));
 
 			// add y
 			// select output and transpose
 			Double[] results = y.getResults(selectedOutputIdentifier);
 			double[][] yt = new double[results.length][];
 			for (int i = 0; i < results.length; i++) {
 				yt[i] = new double[] { results[i] };
 			}
 			request.addParameter(new MLMatrix(yt));
 			
 			// setup covariance function
 			MLValue covfname;
 			double[] covfpar;
 			if (covarianceFunction.equals("squared_exponential")) {
 				covfname = new MLString("covSEiso");
 			}
 			else {
 				covfname = new MLString("covMatern3iso");
 			}
 			if (nuggetVariance != null) {
 				covfname = new MLCell(new MLValue[] {
 					new MLString("covSum"),
 					new MLCell(new MLValue[] { covfname, new MLString("covNoise") }) });
 				covfpar = new double[] { lengthScale, Math.sqrt(processVariance), nuggetVariance };
 			}
 			else {
 				covfpar = new double[] { lengthScale, Math.sqrt(processVariance) };
 			}
 			request.addParameter(covfname);		
 			request.addParameter(new MLArray(covfpar));
 
 			// setup mean function
 			String meanfname;
 			double[] meanfpar;
 			if (meanFunction.equals("zero")) {
 				meanfname = "";
 				meanfpar = new double[] {};
 			}
 			else {
 				meanfname = "mean_poly";
 				meanfpar = new double[1];
 				if (meanFunction.equals("constant")) {
 					meanfpar[0] = 0;
 				}
 				else if (meanFunction.equals("linear")) {
 					meanfpar[0] = 1;
 				}
 				else {
 					meanfpar[0] = 2;
 				}
 			}
 			request.addParameter(new MLString(meanfname));
 			request.addParameter(new MLArray(meanfpar));
 
 			// call matlab to: create training set, setup gp, predict, optimise
 			logger.info("Learning emulator...");
 			for (String identifier : design.getInputIdentifiers()) {
 				DescriptiveStatistics stats = new DescriptiveStatistics(ArrayUtils.toPrimitive(x.getPoints(identifier)));
 				logger.debug("identifier='" + identifier + "', min=" + stats.getMin() + ", max=" + stats.getMax());
 			}
 			logger.debug("meanfname=" + meanfname + ", meanfpar=" + Arrays.toString(meanfpar) + ", covfname=" + covfname + ", covfpar=" + Arrays.toString(covfpar));
 
 
 			// send
 		try {
 			MLResult result = MATLAB.sendRequest(request);
 
 			double[] predictedMean = result.getResult(0).getAsArray().getArray();
 			double[] predictedCovariance = result.getResult(1).getAsArray().getArray();
 			double[] optCovParams = result.getResult(2).getAsArray().getArray();
 			
 			if (normalisation) {
 				NormalisedDesign nd = (NormalisedDesign)x;
 				NormalisedProcessEvaluationResult nper = (NormalisedProcessEvaluationResult)y;
 				return new LearningResult(predictedMean, predictedCovariance, nd, nper, optCovParams[0], optCovParams[1] * optCovParams[1], nd.getMeans(), nd.getStdDevs(), nper.getMean(selectedOutputIdentifier), nper.getStdDev(selectedOutputIdentifier));
 			}
 			else {
 				return new LearningResult(predictedMean, predictedCovariance, x, y, optCovParams[0], optCovParams[1] * optCovParams[1]);
 			}
 		}
 		catch (MLException e) {
 			throw new LearningException("Couldn't perform learning: " + e.getMessage());
 		}
 		catch (IOException e) {
 			throw new LearningException("Couldn't connect to MATLAB.");
 		}
 		catch (ConfigException e) {
 			throw new LearningException("Couldn't load MATLAB config details.");
 		}
 	}
 
 }
