 /**
  * 
  */
 package rinde.evo4mas.gendreau06;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newHashMap;
 import static java.util.Collections.unmodifiableList;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.jppf.client.JPPFJob;
 import org.jppf.task.storage.DataProvider;
 import org.jppf.task.storage.MemoryMapDataProvider;
 
 import rinde.ecj.GPBaseNode;
 import rinde.ecj.GPEvaluator;
 import rinde.ecj.GPProgram;
 import rinde.ecj.GPProgramParser;
 import rinde.ecj.Heuristic;
 import rinde.evo4mas.common.ExperimentUtil;
 import rinde.evo4mas.common.ResultDTO;
 import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;
 import ec.EvolutionState;
 import ec.gp.GPIndividual;
 import ec.gp.GPTree;
 import ec.util.Parameter;
 
 /**
  * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
  * 
  */
 public class Gendreau06Evaluator extends GPEvaluator<GSimulationTask, ResultDTO, Heuristic<GendreauContext>> {
 
 	private static final long serialVersionUID = 5944679648563955812L;
 
 	static List<List<String>> folds = ExperimentUtil.createFolds("files/scenarios/gendreau06/", 5, "");
 
 	protected List<String> trainSet;
 	protected List<String> testSet;
 	protected int numScenariosPerGeneration;
 	protected int numScenariosAtLastGeneration;
 	protected SolutionType solutionType;
 	private final Map<String, String> scenarioCache;
 
 	public final static String P_SOLUTION_VARIANT = "solution-variant";
 
 	public final static String P_TEST_SET_DIR = "test-set-dir";
 	public final static String P_TRAIN_SET_DIR = "train-set-dir";
 
 	public final static String P_NUM_SCENARIOS_PER_GENERATION = "num-scenarios-per-generation";
 	public final static String P_NUM_SCENARIOS_AT_LAST_GENERATION = "num-scenarios-at-last-generation";
 
 	public Gendreau06Evaluator() {
 		scenarioCache = newHashMap();
 	}
 
 	@Override
 	public void setup(final EvolutionState state, final Parameter base) {
 		super.setup(state, base);
 		final String testSetDir = state.parameters.getString(base.push(P_TEST_SET_DIR), null);
 		checkArgument(testSetDir != null && new File(testSetDir).isDirectory(), "A valid test set directory should be specified, "
 				+ base.push(P_TEST_SET_DIR) + "=" + testSetDir);
 		final String trainSetDir = state.parameters.getString(base.push(P_TRAIN_SET_DIR), null);
 		checkArgument(trainSetDir != null && new File(trainSetDir).isDirectory(), "A valid train set directory should be specified, "
 				+ base.push(P_TRAIN_SET_DIR) + "=" + trainSetDir);
 
 		testSet = unmodifiableList(ExperimentUtil.getFilesFromDir(testSetDir, "_240_24"));
 		trainSet = unmodifiableList(ExperimentUtil.getFilesFromDir(trainSetDir, "_240_24"));
 		System.out.println("test: " + removeDirPrefix(testSet) + "\ntrain: " + removeDirPrefix(trainSet));
 
 		final String sv = state.parameters.getString(base.push(P_SOLUTION_VARIANT), null);
		checkArgument(SolutionType.hasValue(sv), base.push(P_TRAIN_SET_DIR)
 				+ " should be assigned one of the following values: " + Arrays.toString(SolutionType.values()));
 		solutionType = SolutionType.valueOf(sv);
 
 		try {
 			for (final String s : testSet) {
 				scenarioCache.put(s, ExperimentUtil.textFileToString(s));
 			}
 			for (final String s : trainSet) {
 				scenarioCache.put(s, ExperimentUtil.textFileToString(s));
 			}
 		} catch (final FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} catch (final IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		numScenariosPerGeneration = state.parameters.getInt(base.push(P_NUM_SCENARIOS_PER_GENERATION), null, 0);
 		checkArgument(numScenariosPerGeneration > 0, "Number of scenarios per generation must be defined, found "
 				+ base.push(P_NUM_SCENARIOS_PER_GENERATION) + "="
 				+ (numScenariosPerGeneration == -1 ? "undefined" : numScenariosPerGeneration));
 
 		numScenariosAtLastGeneration = state.parameters.getInt(base.push(P_NUM_SCENARIOS_AT_LAST_GENERATION), null, 0);
 		checkArgument(numScenariosAtLastGeneration > 0, "Number of scenarios at last generation must be defined, found "
 				+ base.push(P_NUM_SCENARIOS_AT_LAST_GENERATION)
 				+ "="
 				+ (numScenariosAtLastGeneration == -1 ? "undefined" : numScenariosAtLastGeneration));
 
 	}
 
 	List<String> getCurrentScenarios(EvolutionState state) {
 		final List<String> list = newArrayList();
 		final int numScens = state.generation == state.numGenerations - 1 ? numScenariosAtLastGeneration
 				: numScenariosPerGeneration;
 		for (int i = 0; i < numScens; i++) {
 			list.add(trainSet.get((state.generation * numScenariosPerGeneration + i) % trainSet.size()));
 		}
 		return list;
 	}
 
 	@Override
 	public void evaluatePopulation(EvolutionState state) {
 		System.out.println(removeDirPrefix(getCurrentScenarios(state)));
 		super.evaluatePopulation(state);
 	}
 
 	List<String> removeDirPrefix(List<String> files) {
 		final List<String> names = newArrayList();
 		for (final String f : files) {
 			names.add(f.substring(f.lastIndexOf('/') + 1));
 		}
 		return names;
 	}
 
 	Collection<ResultDTO> experimentOnTestSet(GPIndividual ind) {
 		final GPProgram<GendreauContext> heuristic = GPProgramParser
 				.convertToGPProgram((GPBaseNode<GendreauContext>) ind.trees[0].child);
 
 		final DataProvider dataProvider = new MemoryMapDataProvider();
 		final JPPFJob job = new JPPFJob(dataProvider);
 		job.setBlocking(true);
 		job.setName("Evaluation on test set");
 
 		final List<GSimulationTask> list = newArrayList();
 		final List<String> scenarios = testSet;
 		for (final String s : scenarios) {
 			try {
 				dataProvider.setValue(s, scenarioCache.get(s));
 			} catch (final Exception e) {
 				throw new RuntimeException(e);
 			}
 			final int numVehicles = s.contains("_450_") ? 20 : 10;
 			list.add(new GSimulationTask(s, heuristic.clone(), numVehicles, -1, solutionType));
 		}
 		try {
 
 			for (final GSimulationTask j : list) {
 				if (compStrategy == ComputationStrategy.LOCAL) {
 					j.setDataProvider(dataProvider);
 				}
 				job.addTask(j);
 			}
 			final Collection<ResultDTO> results = compute(job);
 			return results;
 
 		} catch (final Exception e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	@Override
 	protected Collection<GSimulationTask> createComputationJobs(DataProvider dataProvider, GPTree[] trees,
 			EvolutionState state) {
 
 		final GPProgram<GendreauContext> heuristic = GPProgramParser
 				.convertToGPProgram((GPBaseNode<GendreauContext>) trees[0].child);
 
 		final List<GSimulationTask> list = newArrayList();
 		final List<String> scenarios = getCurrentScenarios(state);
 		for (final String s : scenarios) {
 			try {
 				dataProvider.setValue(s, scenarioCache.get(s));
 			} catch (final Exception e) {
 				throw new RuntimeException(e);
 			}
 			final int numVehicles = s.contains("_450_") ? 20 : 10;
 			list.add(new GSimulationTask(s, heuristic.clone(), numVehicles, 60000, SolutionType.AUCTION));
 		}
 		return list;
 	}
 
 	@Override
 	protected int expectedNumberOfResultsPerGPIndividual(EvolutionState state) {
 		return state.generation == state.numGenerations - 1 ? numScenariosAtLastGeneration : numScenariosPerGeneration;
 	}
 
 }
