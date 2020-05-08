 package helpers;
 
 import org.encog.neural.data.basic.BasicNeuralDataSet;
 
 import techniques.EvaluationTechnique;
 
 public class Evaluator {
 
 	private EvaluationTechnique technique;
 	private DataLoader dataLoader;
 	
 	Evaluator(EvaluationTechnique technique, DataMapper mapper, int inputCols, int inputs, String dataFile, boolean inputsReversed, int trainingSetSize, double targetTrainingError, double selectionError) {
 		this.setTechnique(technique);
 		dataLoader = new DataLoader(mapper,inputCols,inputs,trainingSetSize,inputsReversed);
 		dataLoader.readData(dataFile);
 		this.technique.init(dataLoader);
 		this.technique.setParams(targetTrainingError, selectionError);
 		this.technique.train(false);
 	}
 	
 	public Evaluator(EvaluationTechnique technique, DataLoader dataLoader, double targetTrainingError, double selectionError, boolean verbose) {
 		this.setTechnique(technique);
 		this.dataLoader = dataLoader;
 		this.technique.init(dataLoader);
 		this.technique.setParams(targetTrainingError, selectionError);
 		this.technique.train(verbose);
 	}
 	
 	public void makeLine(String type, double te, Labeler prefix, BasicNeuralDataSet dataSet) {
 		DataMapper dataMapper = dataLoader.getMapper();
 		PerfResults perf = this.technique.testPerformance(dataSet, dataMapper,false);
 		System.out.println(type + "," + prefix.get(technique.getCurrentSize()) + "," + te + "," +
 				(this.technique.getMisclassification(dataSet,dataMapper)) + "," +
 				(perf.getAccuracy(PerfResults.AveragingMethod.MICRO)) + "," +
 				(perf.getPrecision(PerfResults.AveragingMethod.MICRO)) + "," +
 				(perf.getRecall(PerfResults.AveragingMethod.MICRO)) + "," +
 				(perf.FScore(1.0, PerfResults.AveragingMethod.MICRO)) + "," +
 				(perf.getAccuracy(PerfResults.AveragingMethod.MACRO)) + "," +
 				(perf.getPrecision(PerfResults.AveragingMethod.MACRO)) + "," +
 				(perf.getRecall(PerfResults.AveragingMethod.MACRO)) + "," +
 				(perf.FScore(1.0, PerfResults.AveragingMethod.MACRO)) + "," +
 				(this.technique.getMisclassificationCount(dataSet,dataMapper))
 		);
 		int outputs = dataSet.getIdealSize();
 		for (int output = 0; output < outputs; output ++)
 		{
			System.out.println(prefix + "," + type + "," + "for-class-" + dataMapper.getClassLabel(output) + 
 				"," + perf.getTP(output) + 
 				"," + perf.getTN(output) +
 				"," + perf.getFP(output) +
 				"," + perf.getFN(output) 
 			);
 		}
 	}
 	
 	public void getResults (Labeler prefix, double te) {
 		while(technique.hasStepsLeft()) {
 			makeLine("train",te,prefix,this.dataLoader.getTrainingSet());
 			makeLine("test",te,prefix,this.dataLoader.getTestSet());
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
