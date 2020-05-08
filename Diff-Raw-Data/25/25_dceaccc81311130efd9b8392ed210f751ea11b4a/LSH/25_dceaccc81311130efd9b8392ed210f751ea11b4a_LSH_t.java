 package de.uni_potsdam.de.hpi.fgnaumann.art.lsh;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import de.uni_potsdam.de.hpi.fgnaumann.art.permutation.FisherYates;
 import de.uni_potsdam.de.hpi.fgnaumann.art.permutation.PermutationGenerator;
 import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
 import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.PrimitiveMapFeatureVector;
 
 public class LSH {
 
 	private static Logger logger = LogManager.getFormatterLogger(LSH.class
 			.getName());
 
 	private static Random rnd = new Random();
 	
 	public static List<Pair<Double, Long>> computeNeighbours(
 			FeatureVector<?> searchVector, Set<FeatureVector<?>> inputVectors,
 			double maxDistance, int topK, int NTHREADS,
 			int NUMBER_OF_PERMUTATIONS_q, int WINDOW_SIZE_B) {
		FeatureVector<? extends Number> exampleVector;
		Iterator<FeatureVector<? extends Number>> inputVectorsIterator = inputVectors
				.iterator();
		if (inputVectorsIterator.hasNext()) {
			exampleVector = inputVectorsIterator.next();
		} else {
			throw new IllegalArgumentException(
					"Your input vectors seem to be empty.");
		}
 		// step 4 of paper: random permutation of bit vectors and window lookup
 		// for hamming distance calculation
 		logger.trace("started creation of random permutations");
 		Set<int[]> randomPermutations = new HashSet<int[]>();
 		PermutationGenerator permutationGenerator = new FisherYates();
 		for (int i = 0; i < NUMBER_OF_PERMUTATIONS_q; i++) {
 			randomPermutations.add(permutationGenerator
					.generateRandomPermutation(exampleVector.getDimensionality()));
 		}
 		logger.trace("finished creation of random permutations");
 
 		logger.trace("starting assignment of permutation workers to executor");
 		Map<Long, Double> candidates = new HashMap<Long, Double>();
 
 		ExecutorService pemutationExecutor = Executors
 				.newFixedThreadPool(NTHREADS);
 		List<Future<Map<Long, Double>>> list = new ArrayList<Future<Map<Long, Double>>>();
 
 		for (int[] randomPermutation : randomPermutations) {
 			Callable<Map<Long, Double>> worker = new PermutationWorker(
 					randomPermutation, searchVector, inputVectors,
 					WINDOW_SIZE_B);
 			Future<Map<Long, Double>> submit = pemutationExecutor
 					.submit(worker);
 			list.add(submit);
 		}
 		logger.trace(
 				"finished assignment of %d %s permutation workers to executor",
 				randomPermutations.size(),
 				(randomPermutations.size() > 1 ? "workers" : "worker"));
 
 		for (Future<Map<Long, Double>> future : list) {
 			try {
 				candidates.putAll(future.get());
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 			}
 		}
 		pemutationExecutor.shutdown();
 
 		logger.trace("started filtering of neighbours by threshold");
 		List<Pair<Double, Long>> resultList = new ArrayList<Pair<Double, Long>>();
 		for (Entry<Long, Double> hammingDistances : candidates.entrySet()) {
 			if (hammingDistances.getValue() <= maxDistance) {
 				resultList.add(new ImmutablePair<Double, Long>(hammingDistances
 						.getValue(), hammingDistances.getKey()));
 			}
 		}
 		logger.trace("finished filtering of neighbours by threshold");
 
 		return resultList;
 	}
 
 	public static Set<FeatureVector<? extends Number>> computeLSH(
 			Set<FeatureVector<? extends Number>> inputVectors, int NTHREADS,
 			int CHUNK_SIZE_CLASSIFIER_WORKER, int NUMBER_OF_RANDOM_VECTORS_d) {
 
 		// step 2 of paper: generation of random hyperplanes
 		logger.trace("starting generation of random vectors");
 		FeatureVector<? extends Number> exampleVector;
 		Iterator<FeatureVector<? extends Number>> inputVectorsIterator = inputVectors
 				.iterator();
 		if (inputVectorsIterator.hasNext()) {
 			exampleVector = inputVectorsIterator.next();
 		} else {
 			throw new IllegalArgumentException(
 					"Your input vectors seem to be empty.");
 		}
 		Set<FeatureVector<? extends Number>> randomVectors = generateRandomWeightVectors(
 				NUMBER_OF_RANDOM_VECTORS_d, exampleVector.getDimensionality());
 		logger.trace("finished generation of random vectors");
 
 		// step 3 of paper: classification
 		ExecutorService classifierExecutor = Executors
 				.newFixedThreadPool(NTHREADS);
 
 		logger.trace(
 				"starting assignment of classifier workers to executor with %s threads",
 				NTHREADS);
 		int chunkSize = 0;
 		Set<FeatureVector<? extends Number>> tempSet = new HashSet<FeatureVector<? extends Number>>();
 		int chunkCount = 0;
 		for (FeatureVector<? extends Number> inputVector : inputVectors) {
 			if (!(chunkSize != 0 && chunkSize % CHUNK_SIZE_CLASSIFIER_WORKER == 0)) {
 				tempSet.add(inputVector);
 				chunkSize++;
 			} else {
 				Runnable worker = new ClassifierWorker(tempSet, randomVectors);
 				classifierExecutor.execute(worker);
 				tempSet = new HashSet<FeatureVector<? extends Number>>();
 				chunkSize = 0;
 				chunkCount++;
 				tempSet.add(inputVector);
 				chunkSize++;
 			}
 		}
 
 		if (tempSet.size() > 0) {
 			Runnable worker = new ClassifierWorker(tempSet, randomVectors);
 			classifierExecutor.execute(worker);
 			chunkCount++;
 		}
 
 		logger.trace("finished assignment of %d %s chunks to executor",
 				chunkCount, (chunkCount > 1 ? "workers" : "worker"));
 		classifierExecutor.shutdown();
 		while (!classifierExecutor.isTerminated()) {
 
 		}
 		logger.trace("all classification workers finished");
 		return inputVectors;
 	}
 
 	public static List<Pair<Double, Long>> computeNeighbours(
 			FeatureVector<?> searchVector, Set<FeatureVector<?>> inputVectors,
 			double maxDistance, int NTHREADS, int CHUNK_SIZE_CLASSIFIER_WORKER,
 			int NUMBER_OF_RANDOM_VECTORS_d, int NUMBER_OF_PERMUTATIONS_q,
 			int WINDOW_SIZE_B) {
 
 		// for convenience we add the search vector to the input vectors set
 		inputVectors.add(searchVector);
 
 		// step 2 of paper: generation of random hyperplanes
 		logger.trace("starting generation of random vectors");
 		Set<FeatureVector<? extends Number>> randomVectors = generateRandomWeightVectors(
 				NUMBER_OF_RANDOM_VECTORS_d, searchVector.getDimensionality());
 		logger.trace("finished generation of random vectors");
 
 		// step 3 of paper: classification
 		ExecutorService classifierExecutor = Executors
 				.newFixedThreadPool(NTHREADS);
 
 		logger.trace(
 				"starting assignment of classifier workers to executor with %s threads",
 				NTHREADS);
 		int chunkSize = 0;
 		Set<FeatureVector<? extends Number>> tempSet = new HashSet<FeatureVector<? extends Number>>();
 		int chunkCount = 0;
 		for (FeatureVector<? extends Number> inputVector : inputVectors) {
 			if (!(chunkSize != 0 && chunkSize % CHUNK_SIZE_CLASSIFIER_WORKER == 0)) {
 				tempSet.add(inputVector);
 				chunkSize++;
 			} else {
 				Runnable worker = new ClassifierWorker(tempSet, randomVectors);
 				classifierExecutor.execute(worker);
 				tempSet = new HashSet<FeatureVector<? extends Number>>();
 				chunkSize = 0;
 				chunkCount++;
 				tempSet.add(inputVector);
 				chunkSize++;
 			}
 		}
 
 		if (tempSet.size() > 0) {
 			Runnable worker = new ClassifierWorker(tempSet, randomVectors);
 			classifierExecutor.execute(worker);
 			chunkCount++;
 		}
 
 		logger.trace("finished assignment of %d %s chunks to executor",
 				chunkCount, (chunkCount > 1 ? "workers" : "worker"));
 		classifierExecutor.shutdown();
 		while (!classifierExecutor.isTerminated()) {
 
 		}
 		logger.trace("all classification workers finished");
 
 		// step 4 of paper: random permutation of bit vectors and window lookup
 		// for hamming distance calculation
 		logger.trace("started creation of random permutations");
 		Set<int[]> randomPermutations = new HashSet<int[]>();
 		PermutationGenerator permutationGenerator = new FisherYates();
 		for (int i = 0; i < NUMBER_OF_PERMUTATIONS_q; i++) {
 			randomPermutations.add(permutationGenerator
 					.generateRandomPermutation(NUMBER_OF_RANDOM_VECTORS_d));
 		}
 		logger.trace("finished creation of random permutations");
 
 		logger.trace("starting assignment of permutation workers to executor");
 		Map<Long, Double> candidates = new HashMap<Long, Double>();
 
 		ExecutorService pemutationExecutor = Executors
 				.newFixedThreadPool(NTHREADS);
 		List<Future<Map<Long, Double>>> list = new ArrayList<Future<Map<Long, Double>>>();
 
 		for (int[] randomPermutation : randomPermutations) {
 			Callable<Map<Long, Double>> worker = new PermutationWorker(
 					randomPermutation, searchVector, inputVectors,
 					WINDOW_SIZE_B);
 			Future<Map<Long, Double>> submit = pemutationExecutor
 					.submit(worker);
 			list.add(submit);
 		}
 		logger.trace(
 				"finished assignment of %d %s permutation workers to executor",
 				randomPermutations.size(),
 				(randomPermutations.size() > 1 ? "workers" : "worker"));
 
 		for (Future<Map<Long, Double>> future : list) {
 			try {
 				candidates.putAll(future.get());
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 			}
 		}
 		pemutationExecutor.shutdown();
 
 		logger.trace("started filtering of neighbours by threshold");
 		List<Pair<Double, Long>> resultList = new ArrayList<Pair<Double, Long>>();
 		for (Entry<Long, Double> hammingDistances : candidates.entrySet()) {
 			if (hammingDistances.getValue() <= maxDistance) {
 				resultList.add(new ImmutablePair<Double, Long>(hammingDistances
 						.getValue(), hammingDistances.getKey()));
 			}
 		}
 		logger.trace("finished filtering of neighbours by threshold");
 
 		return resultList;
 	}
 
 	private static Set<FeatureVector<? extends Number>> generateRandomWeightVectors(
 			Integer numberOfRandomVectors, Integer dimensionality) {
 		Set<FeatureVector<? extends Number>> randomWeightVectors = new HashSet<FeatureVector<? extends Number>>();
 		// For d random projections.
 		for (long di = 0; di < numberOfRandomVectors; di++) {
 			// Randomly generate a weight vector of random normal mean 0 and
 			// variance 1 weights.
 			FeatureVector<Double> dI = new PrimitiveMapFeatureVector<Double>(
 					di, dimensionality);
 			// FeatureVector<Double> dI = new
 			// NumberListFeatureVector<Double>((long)di);
 			for (int ki = 0; ki < dimensionality; ki++) {
 				dI.setValue(ki, rnd.nextGaussian());
 			}
 			randomWeightVectors.add(dI);
 		}
 		return randomWeightVectors;
 	}
 }
