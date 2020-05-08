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
 		System.err.println("Can't to this in Boosting");
 	}
 
 	@Override
 	public double trainError() {
		return 0;
 	}
 
 	@Override
 	public void step(boolean verbose) {
		// TODO Auto-generated method stub
		
 	}
 	
 }
