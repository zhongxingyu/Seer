 package techniques;
 
 import java.util.List;
 
 import org.encog.ensemble.Ensemble.TrainingAborted;
 import org.encog.ensemble.EnsembleAggregator;
 import org.encog.ensemble.EnsembleMLMethodFactory;
 import org.encog.ensemble.EnsembleTrainFactory;
 import org.encog.ensemble.bagging.Bagging;
 import org.encog.ensemble.stacking.Stacking;
 import org.encog.ml.data.MLData;
 
 import helpers.DataLoader;
 import helpers.ChainParams;
 
 public class StackingET extends EvaluationTechnique {
 
 	private int dataSetSize;
 
 	public StackingET(List<Integer> sizes, int dataSetSize, int maxIterations, ChainParams fullLabel, EnsembleMLMethodFactory mlMethod, EnsembleTrainFactory trainFactory, EnsembleAggregator aggregator) {
 		this.sizes = sizes;
 		this.dataSetSize = dataSetSize;
 		this.label = fullLabel;
 		this.mlMethod = mlMethod;
 		this.trainFactory = trainFactory;
 		this.aggregator = aggregator;
 		this.maxIterations = maxIterations;
 	}
 
 	@Override
 	public void init(DataLoader dataLoader, int fold) {
 		ensemble = new Stacking(sizes.get(currentSizeIndex),dataSetSize,mlMethod,trainFactory,aggregator);
 		setTrainingSet(dataLoader.getTrainingSet(fold));
 		setSelectionSet(dataLoader.getTestSet(fold));
 		ensemble.setTrainingData(trainingSet);
 	}	
 	
 	@Override
 	public void trainStep() {
 		((Stacking) ensemble).trainStep();
 	}
 
 	@Override
 	public MLData compute(MLData input) {
 		return ensemble.compute(input);
 	}
 	
 	@Override
 	public void step(boolean verbose) throws TrainingAborted {
 		if (currentSizeIndex < sizes.size() -1) {
 			for (int i = sizes.get(currentSizeIndex++); i < sizes.get(currentSizeIndex); i++) {
 				ensemble.addNewMember();
 				ensemble.trainMember(i, trainToError, selectionError, selectionSet, verbose);
 			}
 			ensemble.retrainAggregator();
 		} else {
 			this.hasStepsLeft = false;
 		}
 	}
 	
 }
