 package main;
 
 import java.util.List;
 
 import org.encog.ensemble.EnsembleAggregator;
 import org.encog.ensemble.EnsembleMLMethodFactory;
 import org.encog.ensemble.EnsembleTrainFactory;
 import techniques.EvaluationTechnique;
 import helpers.ArgParser;
 import helpers.ArgParser.BadArgument;
 import helpers.DataLoader;
 import helpers.Evaluator;
 import helpers.Labeler;
 import helpers.ProblemDescription;
 
 public class Test {
 
 	Evaluator ev;
 	static DataLoader dataLoader;
 	static ProblemDescription problem;
 	
 	private static List<Integer> sizes;
 	private static List<Integer> dataSetSizes;
 	private static List<Double> trainingErrors;
 	private static int nFolds;
 	private static double activationThreshold;
 	private static EnsembleTrainFactory etf;
 	private static List<EnsembleMLMethodFactory> mlfs;
 	private static EnsembleAggregator agg;
 	private static String etType;
 	private static boolean verbose;
 	private static double selectionError;
 	
 	public static void loop() {
 		for(Integer dataSetSize : dataSetSizes)
 		for (int fold=0; fold < nFolds; fold++)
 		for(EnsembleMLMethodFactory mlf: mlfs)
 		{
 
 			EvaluationTechnique et = null;
 			Labeler fullLabel = new Labeler(problem.getLabel(),etType,etf.getLabel(),mlf.getLabel(),agg.getLabel(),dataSetSize);
 			try {
 				et = ArgParser.technique(etType,sizes,dataSetSize,fullLabel,mlf,etf,agg);
 			} catch (BadArgument e) {
 				help();
 			}
 			for (double te: trainingErrors) {
 				Evaluator ev = new Evaluator(et, dataLoader, te, selectionError, verbose,fold);
 				ev.getResults(fullLabel,te,fold);
 			}
 		}
 	}
 	
 	public static void main(String[] args) {
 		if (args.length != 12) {
 			help();
 		} 
 		try {
 			etType = args[0];
 			problem = ArgParser.problem(args[1]);
 			sizes = ArgParser.intList(args[2]);
 			dataSetSizes = ArgParser.intList(args[3]);
 			trainingErrors = ArgParser.doubleList(args[4]);
 			nFolds = ArgParser.intSingle(args[5]);
 			activationThreshold = ArgParser.doubleSingle(args[6]);
 			etf = ArgParser.ETF(args[7]);
 			mlfs = ArgParser.MLFS(args[8]);
 			agg = ArgParser.AGG(args[9]);
 			verbose = Boolean.parseBoolean(args[10]);
 			selectionError = ArgParser.doubleSingle(args[11]);
 		} catch (BadArgument e) {
 			help();
 		}
 		
 		try {
 			dataLoader = problem.getDataLoader(activationThreshold,nFolds);
 		} catch (helpers.ProblemDescriptionLoader.BadArgument e) {
 			System.err.println("Could not create dataLoader - perhaps the mapper_type property is wrong");
 			e.printStackTrace();
 		}
 		loop();
 		System.exit(0);
 	}
 
 	private static void help() {
 		System.err.println("Usage: Test <technique> <problem> <sizes> <dataSetSizes> <trainingErrors> <nFolds> <activationThreshold> <training> <membertypes> <aggregator> <verbose> <selectionError>");
 		System.exit(2);
 	}
 }
