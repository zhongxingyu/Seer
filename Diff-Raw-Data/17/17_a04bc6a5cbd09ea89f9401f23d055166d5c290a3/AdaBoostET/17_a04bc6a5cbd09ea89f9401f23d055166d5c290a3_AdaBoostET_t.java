 package techniques;
 
 import java.util.List;
 
 import org.encog.ensemble.EnsembleAggregator;
 import org.encog.ensemble.EnsembleMLMethodFactory;
 import org.encog.ensemble.EnsembleTrainFactory;
 import org.encog.ensemble.adaboost.AdaBoost;
 import org.encog.ml.data.MLData;
 
 import helpers.DataLoader;
 import helpers.ChainParams;
 
 public class AdaBoostET extends EvaluationTechnique {
 
 	private int dataSetSize;
	private DataLoader dataLoader;
	private int fold;
 
 	public AdaBoostET(List<Integer> sizes, int dataSetSize, ChainParams fullLabel, EnsembleMLMethodFactory mlMethod, EnsembleTrainFactory trainFactory, EnsembleAggregator aggregator) {
 		this.sizes = sizes;
 		this.dataSetSize = dataSetSize;
 		this.label = fullLabel;
 		this.mlMethod = mlMethod;
 		this.trainFactory = trainFactory;
 		this.aggregator = aggregator;
 	}
 
 	@Override
 	public void init(DataLoader dataLoader, int fold) {
		this.dataLoader = dataLoader;
		this.fold = fold;
		this.restart();
	}
	
	private void restart() {
 		ensemble = new AdaBoost(sizes.get(currentSizeIndex),dataSetSize,mlMethod,trainFactory,aggregator);
 		setTrainingSet(dataLoader.getTrainingSet(fold));
 		setSelectionSet(dataLoader.getTestSet(fold));
 		ensemble.setTrainingData(trainingSet);
 	}
 
 	@Override
 	public MLData compute(MLData input) {
 		return ensemble.compute(input);
 	}
 
 	@Override
 	public void trainStep() {
		System.err.println("Can't to this in AdaBoost");
 	}
 
 	@Override
 	public double trainError() {
 		return ensemble.getMember(0).getTraining().getError();
 	}
 
 	@Override
 	public void step(boolean verbose) {
		currentSizeIndex++;
		if (currentSizeIndex < sizes.size()) {
			this.restart();
 			this.train(false);
 		} else {
 			this.hasStepsLeft = false;
 		}
 	}
 	
 }
