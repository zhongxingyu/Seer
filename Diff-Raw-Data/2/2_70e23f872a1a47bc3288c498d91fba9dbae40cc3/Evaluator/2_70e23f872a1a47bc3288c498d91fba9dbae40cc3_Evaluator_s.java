 package helpers;
 
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Calendar;
 
 import org.encog.neural.data.basic.BasicNeuralDataSet;
 
 import techniques.EvaluationTechnique;
 
 public class Evaluator {
 
 	private EvaluationTechnique technique;
 	private DataLoader dataLoader;
 	
 	Evaluator(EvaluationTechnique technique, DataMapper mapper, int inputCols, int inputs, String dataFile, boolean inputsReversed, int nFolds, double targetTrainingError, double selectionError, int fold) {
 		this.setTechnique(technique);
 		dataLoader = new DataLoader(mapper,inputCols,inputs,inputsReversed,nFolds);
 		dataLoader.readData(dataFile);
 		this.technique.init(dataLoader,fold);
 		this.technique.setParams(targetTrainingError, selectionError);
 		this.technique.train(false);
 	}
 	
 	public Evaluator(EvaluationTechnique technique, DataLoader dataLoader, double targetTrainingError, double selectionError, boolean verbose, int fold) {
 		this.setTechnique(technique);
 		this.dataLoader = dataLoader;
 		this.technique.init(dataLoader,fold);
 		this.technique.setParams(targetTrainingError, selectionError);
 		this.technique.train(verbose);
 	}
 	
 	public void makeLine(boolean isTest, double training_error, ChainParams chainPars, BasicNeuralDataSet dataSet, Statement sqlStatement, long chainId) throws SQLException {
 		DataMapper dataMapper = dataLoader.getMapper();
 		PerfResults perf = this.technique.testPerformance(dataSet, dataMapper,false);
 		Calendar cal = Calendar.getInstance();
 		long runId = cal.getTimeInMillis();
 		sqlStatement.executeUpdate("INSERT INTO runs (chain, ml_technique, training_error, dataset_size, misclassified_samples," +
 				"is_test, macro_accuracy, macro_precision, macro_recall, macro_f1, micro_accuracy, micro_precision" +
 				"micro_recall, micro_f1, misclassification, ensemble_size, id, chain) VALUES (" + chainId +
 				", " + chainPars.getMLF() +
 				", " + training_error +
				", " +
 				", " + this.technique.getMisclassificationCount(dataSet,dataMapper) +
 				", " + Boolean.toString(isTest) +
 				", " + perf.getAccuracy(PerfResults.AveragingMethod.MACRO) +
 				", " + perf.getPrecision(PerfResults.AveragingMethod.MACRO) +
 				", " + perf.getRecall(PerfResults.AveragingMethod.MACRO) +
 				", " + perf.FScore(1.0, PerfResults.AveragingMethod.MACRO) +
 				", " + perf.getAccuracy(PerfResults.AveragingMethod.MICRO) +
 				", " + perf.getPrecision(PerfResults.AveragingMethod.MICRO) +
 				", " + perf.getRecall(PerfResults.AveragingMethod.MICRO) +
 				", " + perf.FScore(1.0, PerfResults.AveragingMethod.MICRO) +
 				", " + this.technique.getMisclassification(dataSet,dataMapper) +
 				", " + technique.getCurrentSize() +
 				", " + runId +
 				", " + chainId +
 				");"
 		);
 		int outputs = dataSet.getIdealSize();
 		for (int output = 0; output < outputs; output ++)
 		{
 			sqlStatement.executeUpdate("INSERT INTO class_details (run, class, is_test, tp, tn, fp, fn) VALUES (" + runId +
 					", " + dataMapper.getClassLabel(output) +
 					", " + Boolean.toString(isTest) +
 					", " + perf.getTP(output) + 
 					", " + perf.getTN(output) +
 					", " + perf.getFP(output) +
 					", " + perf.getFN(output) +
 					");"
 			);
 		}
 	}
 	
 	public void getResults (ChainParams prefix, double te, int fold, Statement sqlStatement, long chainId) throws SQLException {
 		while(technique.hasStepsLeft()) {
 			makeLine(false,te,prefix,this.dataLoader.getTrainingSet(fold), sqlStatement, chainId);
 			makeLine(true,te,prefix,this.dataLoader.getTestSet(fold), sqlStatement, chainId);
 			technique.step(false);
 		}
 	}
 
 	public EvaluationTechnique getTechnique() {
 		return technique;
 	}
 
 	public void setTechnique(EvaluationTechnique technique) {
 		this.technique = technique;
 	}
 	
 }
