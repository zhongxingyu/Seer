 /**
  *  Copyright 2014 Diego Ceccarelli
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package eu.europeana.querylog.learn;
 
 import it.cnr.isti.hpc.io.IOUtils;
 import it.cnr.isti.hpc.property.ProjectProperties;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.apache.commons.math3.analysis.MultivariateFunction;
 import org.apache.commons.math3.optim.ConvergenceChecker;
 import org.apache.commons.math3.optim.InitialGuess;
 import org.apache.commons.math3.optim.MaxEval;
 import org.apache.commons.math3.optim.PointValuePair;
 import org.apache.commons.math3.optim.SimpleBounds;
 import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
 import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
 import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
 import org.apache.commons.math3.random.MersenneTwister;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.europeana.querylog.QueryAssessment;
 import eu.europeana.querylog.QueryAssessmentReader;
 import eu.europeana.querylog.learn.measure.Measure;
 import eu.europeana.querylog.learn.measure.Recall;
 import eu.europeana.querylog.learn.measure.filter.TopKFilter;
 import eu.europeana.querylog.learn.query.BM25FSolrResults;
 import eu.europeana.querylog.learn.query.EuropeanaSolrResults;
 import eu.europeana.querylog.learn.query.SolrResultsRetriever;
 
 /**
  * 
  * Evaluate finds the best parameter set for BM25F using the <i>line search</i>
  * algorithm, or CMA-ES algorithms.
  * 
  * @see research.microsoft.com/apps/pubs/default.aspx?id=65237
  * @see http://en.wikipedia.org/wiki/CMA-ES
  * 
  * @author diego.ceccarelli@isti.cnr.it
  * 
  */
 public class Evaluate {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(Evaluate.class);
 
 	private int nFields = 0;
 	private List<String> fields;
 	private float[] bm25fParamsOpt;
 	private float[] bm25fParams;
 	private float[] increments;
 
 	private float[] maxValues;
 
 	private float[] minValues;
 
 	final private int N = 24;
 	private final int UNSUCCESSFUL_UPDATES = 10;
 	private final int RANDOM_JUMPS = 10;
 	private int maxSteps = 100;
 
 	private long startTime;
 
 	private Point maxValue;
 	private final List<QueryAssessment> assessment;
 	private final Measure measure;
 	private final SolrResultsRetriever results;
 	private BufferedWriter logFile;
 
 	private final ProjectProperties properties = new ProjectProperties(
 			Evaluate.class);
 	private final Random rng = new Random(
 			properties.getInt("bm25f.learn.random.seed"));
 	private ExecutorService pool = null;
 
 	public Evaluate(File assessmentFolder, List<String> fields,
 			Measure measure, SolrResultsRetriever results) {
 		nFields = fields.size();
 		this.fields = fields;
 		this.measure = measure;
 		this.results = results;
 		Collections.sort(this.fields);
 
 		// nFields bParams + nFields boosts + k1
 		bm25fParamsOpt = new float[nFields * 2 + 1];
 		bm25fParams = new float[nFields * 2 + 1];
 
 		increments = getParamsVector(1.0f, 1.0f, 0.05f);
 		maxValues = getParamsVector(40f, 40f, 1.0f);
 		minValues = getParamsVector(1.0f, 0f, 0f);
 
 		bm25fParams = getParamsVector(1.0f, 1.0f, 0.05f);
 
 		assessment = loadAllAssessments(assessmentFolder);
 	}
 
 	public Evaluate(File assessmentFolder, Measure measure,
 			SolrResultsRetriever results) {
 		this.measure = measure;
 		this.results = results;
 		assessment = loadAllAssessments(assessmentFolder);
 
 	}
 
 	public void setLog(String file) {
 		logFile = IOUtils.getPlainOrCompressedUTF8Writer(file);
 	}
 
 	private float[] getParamsVector(float k1Value, float boostsValue,
 			float bParamsValue) {
 		float[] vector = new float[nFields * 2 + 1];
 		// sets k1 values
 		vector[0] = k1Value;
 		// sets boosts values
 		for (int i = 0; i < nFields; i++) {
 			vector[1 + i] = boostsValue;
 		}
 		// sets bParams values
 		for (int i = 0; i < nFields; i++) {
 			vector[1 + nFields + i] = bParamsValue;
 		}
 		return vector;
 	}
 
 	public double evaluateAssessments(final float[] bm25fParams) {
 
 		List<Callable<List<String>>> tasks = new ArrayList<Callable<List<String>>>();
 
 		for (final QueryAssessment qa : assessment) {
 			tasks.add(new Callable<List<String>>() {
 
 				@Override
 				public List<String> call() throws Exception {
 					SolrQuery query = new SolrQuery(qa.getQuery());
 					query.set("b", boostsToString(bm25fParams));
 					query.set("lb", bParamsToString(bm25fParams));
 					query.set("k1", k1ToString(bm25fParams));
 					query.set("defType", "bm25f");
 
 					return results.results(query, N);
 				}
 
 			});
 		}
 
 		List<Future<List<String>>> queriesResults = null;
 		try {
 			queriesResults = pool.invokeAll(tasks);
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return 0;
 		}
 
 		double totalScore = 0d;
 		int queries = 0;
 
 		for (int i = 0; i < assessment.size(); ++i) {
 			QueryAssessment qa = assessment.get(i);
 			List<String> topDocId = null;
 			try {
 				topDocId = queriesResults.get(i).get();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				continue;
 			}
 			double score = measure.getScore(topDocId, qa);
 			// logger.info("query {} score {}", qa.getQuery(), score);
 
 			totalScore += score;
 			queries++;
 		}
 		double avgScore = totalScore / queries;
 		logger.info(String.format("[%s] %s = %f", results.getName(),
 				measure.getName(), avgScore));
 
 		return avgScore;
 	}
 
 	public double evaluateCurrentRankingFunction() {
 
 		double totalScore = 0d;
 		int queries = 0;
 		logger.info("assessment size = {} ", assessment.size());
 		boolean partial = false;
 		if (System.getProperty("partial") != null) {
 			partial = new Boolean(System.getProperty("partial"));
 
 		}
 		for (QueryAssessment qa : assessment) {
 			SolrQuery query = new SolrQuery(qa.getQuery());
 			List<String> topDocId = results.results(query, N);
 			double score = measure.getScore(topDocId, qa);
 			if (partial) {
 				System.out.println(String.format("%s\t%s\t%s\t%f",
 						qa.getQuery(), results.getName(), measure.getName(),
 						score));
 			}
 
 			totalScore += score;
 			queries++;
 		}
 		double avgScore = totalScore / queries;
 		logger.info(String.format("[%s] %s = %f", results.getName(),
 				measure.getName(), avgScore));
 		return avgScore;
 	}
 
 	private float[] getBParams(float[] params) {
 		return Arrays.copyOfRange(params, 1 + nFields, 1 + 2 * nFields);
 	}
 
 	private float[] getBoosts(float[] params) {
 		return Arrays.copyOfRange(params, 1, 1 + nFields);
 	}
 
 	private float getK1(float[] params) {
 		return params[0];
 	}
 
 	private String bParamsToString(float[] params) {
 		float[] values = getBParams(params);
 		StringBuilder sb = new StringBuilder();
 		for (float val : values) {
 			sb.append(val).append(':');
 		}
 		sb.setLength(sb.length() - 1);
 		return sb.toString();
 	}
 
 	private String boostsToString(float[] params) {
 		float[] values = getBoosts(params);
 		StringBuilder sb = new StringBuilder();
 		for (float val : values) {
 			sb.append(val).append(':');
 		}
 		sb.setLength(sb.length() - 1);
 		return sb.toString();
 	}
 
 	private String k1ToString(float[] params) {
 		return String.valueOf(getK1(params));
 
 	}
 
 	public int getMaxSteps() {
 		return maxSteps;
 	}
 
 	public void setMaxSteps(int maxSteps) {
 		this.maxSteps = maxSteps;
 	}
 
 	public static List<QueryAssessment> loadAllAssessments(File assessmentFolder) {
 		List<QueryAssessment> assessments = new LinkedList<QueryAssessment>();
 
 		for (File f : assessmentFolder.listFiles()) {
 			int i = 0;
 			logger.info("loading assessments in {} ", f.getName());
 			QueryAssessmentReader reader = new QueryAssessmentReader(
 					f.getAbsolutePath());
 			while (reader.hasNext()) {
 				assessments.add(reader.next());
 				i++;
 			}
 			logger.info("loaded {} queries ", i);
 			// logger.info("loaded assessment for the query "
 			// + assessment[i].getQuery());
 
 		}
 		return assessments;
 	}
 
 	public void optimizeK1() {
 		optimizeParameter(0);
 
 	}
 
 	/**
 	 * optimizes only the parameter in position index
 	 * 
 	 * @param index
 	 *            - the position of the parameter
 	 */
 	private void optimizeParameter(int index) {
 		double score = 0;
 		float initialValue = bm25fParams[index];
 		bm25fParams[index] = minValues[index];
 		while (bm25fParams[index] < maxValues[index]) {
 			double currentScore = evaluateAssessments(bm25fParams);
 			if (currentScore > score) {
 				bm25fParamsOpt[index] = bm25fParams[index];
 				score = currentScore;
 				if (score > maxValue.getScore()) {
 					logger.info("new max {} = {}", measure.getName(), maxValue);
 
 					maxValue = new Point(bm25fParams, score);
 					writeLogFile();
 				}
 			}
 			bm25fParams[index] += increments[index];
 		}
 
 		bm25fParams[index] = initialValue;
 	}
 
 	private void writeLogFile() {
 		if (logFile == null)
 			return;
 
 		String s = paramsToXML();
 		try {
 			logFile.write(s);
 			logFile.flush();
 		} catch (IOException e) {
 			logger.error("writing the log: ");
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * performs a search along each coordinate axis by varying one parameter
 	 * only and keeping fixed the others
 	 */
 	public void findBestValueForEachParameter() {
 		logger.info("STEP 1: Find the best value for each parameter");
 		for (int i = 0; i < bm25fParams.length; i++) {
 
 			optimizeParameter(i);
 		}
 		logger.info("optimized parameters: " + Arrays.toString(bm25fParamsOpt));
 	}
 
 	/**
 	 * Identifies a promising search direction, given by the starting point (
 	 * <code>bm25fParams</code>) and the point identified by each single
 	 * parameter optimized in <code>findBestValueForEachParameter()<code>
 	 * 
 	 * @return the promising search direction
 	 * 
 	 * @see findBestValueForEachParameter
 	 */
 	public float[] computeDirection() {
 		float[] direction = new float[bm25fParams.length];
 		logger.info("STEP 2: Compute the promising direction");
 		logger.info("initial configuration \t " + Arrays.toString(bm25fParams));
 		logger.info("final configuration \t " + Arrays.toString(bm25fParamsOpt));
 		double norm = 0;
 		for (int i = 0; i < bm25fParams.length; i++) {
 			direction[i] = bm25fParamsOpt[i] - bm25fParams[i];
 			norm += direction[i] * direction[i];
 		}
 
 		while (norm == 0) {
 			// if norm is 0 then i'll move randomly
 			logger.info("norm is 0: moving randomly");
 			for (int i = 0; i < bm25fParams.length; i++) {
 				direction[i] = (float) rng.nextGaussian();
 				norm += direction[i] * direction[i];
 			}
 		}
 
 		norm = Math.sqrt(norm);
 		logger.info("direction: \t " + Arrays.toString(direction));
 
 		for (int i = 0; i < bm25fParams.length; i++) {
 			direction[i] /= norm;
 		}
 		logger.info("normalized: \t " + Arrays.toString(direction));
 
 		return direction;
 	}
 
 	/**
 	 * given a vector direction, allows to move over the direction using the
 	 * index.
 	 * 
 	 * @param index
 	 *            - specify the distance from the bm25fParams point (positive or
 	 *            negative)
 	 * @param direction
 	 *            - the direction
 	 * @return
 	 */
 	private float[] getPointOnTheLine(int index, float[] direction) {
 		float[] point = new float[bm25fParams.length];
 		for (int i = 0; i < bm25fParams.length; i++) {
 			point[i] = bm25fParams[i] + direction[i] * index;
 		}
 		discretize(point);
 		return point;
 	}
 
 	/**
 	 * Checks if a point is legal, i.e. if at least one parameter in position i
 	 * is greater/lower then the max/min value allowable for the parameter
 	 * 
 	 * @returns true if it is a legal point, false otherwise.
 	 * 
 	 */
 	private boolean isLegalPoint(float[] point) {
 
 		for (int i = 0; i < point.length; i++) {
 			if ((point[i] < minValues[i]) || (point[i] > maxValues[i]))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * moves over a given direction and finds the point that optimize the
 	 * measure.
 	 * 
 	 * @param the
 	 *            direction
 	 * @return whether a better point than maxValue has been found
 	 */
 	public boolean findOptimumOnTheLine(float[] direction) {
 		logger.info("STEP 3: Find the optimal point in the promising direction");
 		int i = 1;
 		boolean improved = false;
 
 		float[] point = null;
 		boolean finished = false;
 		while (!finished) {
 			point = getPointOnTheLine(i, direction);
 			// logger.info("Trying point \t" + Arrays.toString(point));
 			finished = !isLegalPoint(point);
 			if (!finished) {
 				double currentValue = evaluateAssessments(point);
 				if (currentValue > maxValue.getScore()) {
 					improved = true;
 					maxValue = new Point(point, currentValue);
 					logger.info("max point found  = {}", maxValue);
 					writeLogFile();
 
 				}
 			}
 			i++;
 		}
 
 		assert point != null;
 		return improved;
 
 	}
 
 	/**
 	 * approximates each component of the direction to a multiple of the
 	 * corresponding element in increments.
 	 * 
 	 * @param direction
 	 * @modify direction is approximated
 	 */
 	private void discretize(float[] direction) {
 		for (int i = 0; i < direction.length; i++) {
 			float f = direction[i] / increments[i];
 			int val = Math.round(f);
 			direction[i] = increments[i] * val;
 		}
 	}
 
 	/**
 	 * Performs the line search algorithm Returns a map with the best
 	 * combination of parameters
 	 */
 	public String learningToRank() {
 		startTime = System.currentTimeMillis();
 		pool = Executors.newFixedThreadPool(properties
 				.getInt("bm25f.learn.concurrency"));
 		try {
 			maxValue = new Point(bm25fParams, evaluateAssessments(bm25fParams));
 			int unsuccessfullUpdates = 0;
 			int randomJumps = 0;
 			int steps = 0;
 			logger.info("starting learning to rank");
 			while (randomJumps < RANDOM_JUMPS && steps < maxSteps) {
 				Point r = getRandomPoint();
 				bm25fParams = r.getPoint();
 
 				while (unsuccessfullUpdates < UNSUCCESSFUL_UPDATES) {
 					findBestValueForEachParameter();
 
 					float[] direction = computeDirection();
 					boolean improved = findOptimumOnTheLine(direction);
 					if (improved) {
 						unsuccessfullUpdates = 0;
 						logger.info("max point found  = " + maxValue);
 					} else {
 						unsuccessfullUpdates++;
 						logger.info("unsuccessful updates \t ="
 								+ unsuccessfullUpdates + " / "
 								+ UNSUCCESSFUL_UPDATES);
 					}
 					unsuccessfullUpdates = 0;
 					bm25fParams = Arrays.copyOf(maxValue.getPoint(),
 							bm25fParams.length);
 
 					steps++;
 					logger.info("step ={}/{}", steps, maxSteps);
 					if (steps >= maxSteps)
 						break;
 
 				}
 				logger.info("best configuration = {}", maxValue);
 				randomJumps++;
 				logger.info(
 						"Restart from a random configuration ( attempt {}/{}) ",
 						randomJumps, RANDOM_JUMPS);
 			}
 			return paramsToXML();
 		} finally {
 			pool.shutdown();
 			pool = null;
 		}
 	}
 
 	private String paramsToXML() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(String.format("# %f %f\n",
 				(System.currentTimeMillis() - startTime) / 1000.,
 				maxValue.value));
 		sb.append("<float name=\"k1\">").append(getK1(maxValue.getPoint()))
 				.append("</float> \n");
 		sb.append("<lst name=\"fieldsBoost\">\n");
 		int i = 0;
 		for (String f : fields) {
 			sb.append("\t<float name=\"").append(f).append("\"> ");
 			sb.append(getBoosts(maxValue.getPoint())[i]);
 			i++;
 			sb.append("</float>\n");
 		}
 		sb.append("</lst>\n");
 		i = 0;
 		sb.append("<lst name=\"fieldsB\">\n");
 		for (String f : fields) {
 			sb.append("\t<float name=\"").append(f).append("\"> ");
 			sb.append(getBParams(maxValue.getPoint())[i]);
 			i++;
 			sb.append("</float>\n");
 		}
 		sb.append("</lst>\n");
 
 		return sb.toString();
 	}
 
 	float[] doubleArrayToFloat(double[] array) {
 		float[] floatArray = new float[array.length];
 		for (int i = 0; i < array.length; ++i)
 			floatArray[i] = (float) array[i];
 		return floatArray;
 	}
 
 	double[] floatArrayToDouble(float[] array) {
 		double[] doubleArray = new double[array.length];
 		for (int i = 0; i < array.length; ++i)
 			doubleArray[i] = array[i];
 		return doubleArray;
 	}
 
 	/**
 	 * Performs the CMA learning algorithm.
 	 */
 	public String learningToRankWithCMAES() {
 		startTime = System.currentTimeMillis();
 		pool = Executors.newFixedThreadPool(properties
 				.getInt("bm25f.learn.concurrency"));
 		try {
 			final int dim = bm25fParams.length;
 			int lambda = 4 + (int) (3. * Math.log(dim));
 			int maxEvaluations = 1800;
 
 			double[] inSigma = floatArrayToDouble(getParamsVector(0.5f, 0.5f,
 					0.25f));
 
 			CMAESOptimizer optim = new CMAESOptimizer(maxEvaluations / 10, 1.0,
 					false, 0, 10, new MersenneTwister(), true,
 					new ConvergenceChecker<PointValuePair>() {
 						@Override
 						public boolean converged(int iteration,
 								PointValuePair previous, PointValuePair pv) {
 
 							maxValue = new Point(
 									doubleArrayToFloat(pv.getFirst()), pv
 											.getSecond().floatValue());
 							writeLogFile();
 							logger.info("{}", maxValue);
 							return false;
 						}
 					});
 
 			PointValuePair pv = optim.optimize(
 					new MaxEval(maxEvaluations),
 					new ObjectiveFunction(new MultivariateFunction() {
 
 						@Override
 						public double value(double[] point) {
 							return evaluateAssessments(doubleArrayToFloat(point));
 						}
 					}), GoalType.MAXIMIZE, new SimpleBounds(
 							floatArrayToDouble(minValues),
 							floatArrayToDouble(maxValues)), new InitialGuess(
 							floatArrayToDouble(bm25fParams)),
 					new CMAESOptimizer.Sigma(inSigma),
 					new CMAESOptimizer.PopulationSize(lambda));
 
 			maxValue = new Point(doubleArrayToFloat(pv.getFirst()), pv
 					.getSecond().floatValue());
 			writeLogFile();
 			logger.info("final value: \n{}", maxValue);
 			return paramsToXML();
 		} finally {
 			pool.shutdown();
 			pool = null;
 		}
 	}
 
 	private Point getRandomPoint() {
 		float[] newPoint = new float[nFields * 2 + 1];
 		for (int i = 0; i < newPoint.length; i++) {
 			double start = minValues[i];
 			double end = maxValues[i];
 			newPoint[i] = (float) (start + (end - start) * rng.nextDouble());
 		}
 		return new Point(newPoint);
 	}
 
 	public class Point {
 		private float[] point;
 		private double value;
 
 		public Point() {
 		}
 
 		public Point(float[] point) {
 			this.point = point;
 		}
 
 		public Point(float[] point, double value) {
 			this.point = point;
 			this.value = value;
 		}
 
 		public float[] getPoint() {
 			return point;
 		}
 
 		public void setPoint(float[] point) {
 			this.point = point;
 		}
 
 		public double getScore() {
 			return value;
 		}
 
 		public double getNorm() {
 			float norm = 0;
 			for (int i = 0; i < point.length; i++) {
 				norm += point[i] * point[i];
 			}
 			return Math.sqrt(norm);
 		}
 
 		public void setScore(double score) {
 			this.value = score;
 		}
 
 		public boolean greaterThan(Point p) {
 			return value > p.getScore();
 		}
 
 		@Override
 		public String toString() {
 			float k1 = getK1(point);
 			float[] bParams = getBParams(point);
 			float[] boosts = getBoosts(point);
 			StringBuilder bParamsSB = new StringBuilder();
 			StringBuilder boostsSB = new StringBuilder();
 			int i = 0;
 			for (String s : fields) {
 				bParamsSB.append(s).append(":").append(bParams[i]).append(",");
 				boostsSB.append(s).append(":").append(boosts[i]).append(",");
 				i++;
 			}
 			bParamsSB.setLength(bParamsSB.length() - 1);
 			boostsSB.setLength(boostsSB.length() - 1);
 			return "[k1=" + k1 + ", boosts=[" + boostsSB.toString()
					+ "], bParams=[" + bParamsSB.toString() + "]  ndcg="
 					+ value + "]";
 
 		}
 
 	}
 
 	public static void main(String[] args) {
 		Measure m = new Recall();
 		m.addFilter(new TopKFilter(24));
 
 		Evaluate evaluator = new Evaluate(new File("/tmp/test"), Arrays.asList(
 				"title", "author", "description", "text"), m,
 				new BM25FSolrResults());
 
 		Evaluate evaluator1 = new Evaluate(new File("/tmp/test"),
 				Arrays.asList("title", "author", "description", "text"), m,
 				new EuropeanaSolrResults());
 		double europeana = evaluator1.evaluateCurrentRankingFunction();
 		double bm25f = evaluator.evaluateAssessments(evaluator.bm25fParams);
 		logger.info("europeana {}  = {}", m.getName(), europeana);
 		logger.info("bm25f {}  = {}", m.getName(), bm25f);
 
 		evaluator.learningToRank();
 
 		// evaluator.learningToRank();
 
 	}
 }
