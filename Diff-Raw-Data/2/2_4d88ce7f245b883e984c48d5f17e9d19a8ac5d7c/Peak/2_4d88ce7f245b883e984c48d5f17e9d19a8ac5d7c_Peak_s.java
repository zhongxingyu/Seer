 package prefwork.rating.method.normalizer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.configuration.XMLConfiguration;
 
 import prefwork.core.Utils;
 import prefwork.rating.Rating;
 import prefwork.rating.datasource.ContentDataSource;
 import prefwork.rating.method.ContentBased;
 import weka.classifiers.functions.LinearRegression;
 import weka.core.Attribute;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.SparseInstance;
 
 public class Peak implements Normalizer {
 
 	public double getCutValue() {
 		return cutValue;
 	}
 
 	// weka.classifiers.functions.LinearRegression lg = new LinearRegression();
 	LinearRegression lg1 = new LinearRegression();
 	LinearRegression lg2 = new LinearRegression();
 	// MultilayerPerceptron lg = new MultilayerPerceptron();
 	double max,min;
 	Instances isTrainingSet;
 	Instances isTrainingSet1;
 	Instances isTrainingSet2;
 	ArrayList<Attribute> fvWekaAttributes;
 	Instances representants1;
 	Instances representants2;
 	double cutValue;
 	int numberOfClusters = 0;
 	int index;
 
 	public String toString() {
 		return "PeakMAE";
 	}
 
 	public Peak() {
 	}
 
 	/**
 	 * Distributes values between isTrainingSet1 and isTrainingSet2 according to
 	 * the value of cutValue
 	 */
 	private void distributeValues() {
 		for (int i = 0; i < isTrainingSet1.numInstances(); i++) {
 
 			Instance iExample = isTrainingSet1.instance(i);
 			if (iExample.value(0) > cutValue) {
 				isTrainingSet2.add(iExample);
 				isTrainingSet1.delete(i);
 				i--;
 			}
 		}
 		for (int i = 0; i < isTrainingSet2.numInstances(); i++) {
 
 			Instance iExample = isTrainingSet2.instance(i);
 			if (iExample.value(0) <= cutValue) {
 				isTrainingSet1.add(iExample);
 				isTrainingSet2.delete(i);
 				i--;
 			}
 		}
 	}
 
 	/**
 	 * Computes mean error of given classifier on given set.
 	 * 
 	 * @param lg
 	 * @param isTrainingSet
 	 * @return
 	 */
 	private double computeError(LinearRegression lg, Instances isTrainingSet) {
 		double res = 0.0;
 		for (int i = 0; i < isTrainingSet.numInstances(); i++) {
 			try {
 				Instance iExample = isTrainingSet.instance(i);
 				double[] fDistribution;
 				fDistribution = lg.distributionForInstance(iExample);
 				for (int j = 0; j < fDistribution.length; j++)
 					//res += fDistribution[j];
 					res += Math.abs(fDistribution[j]-iExample.classValue());
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		if (isTrainingSet.numInstances() == 0)
 			return 0;
 		return res;
 	}
 
 	protected void computeRepresentants() {
 		try {
 			/*if(numberOfClusters != 0){
 			cluster.setNumClusters(numberOfClusters);
 			cluster.buildClusterer(isTrainingSet);
 			representants = cluster.getClusterCentroids();
 			representants.setClassIndex(1);
 			lg.buildClassifier(representants);
 			}
 			else*/{
 				int indexBest = 0;
 				double errorBest = Double.MAX_VALUE;
 				// Searching for best cutting value (which is the peak value).
 				for (int i = 0; i < isTrainingSet.numInstances(); i++) {
 					try {
 						Instance iExample = isTrainingSet.instance(i);
 						cutValue = iExample.value(0);
 						if(max<cutValue)
 							max = cutValue;
						if(min<cutValue)
 							min = cutValue;
 						distributeValues();						
 						lg1.buildClassifier(isTrainingSet1);
 						lg2.buildClassifier(isTrainingSet2);
 						double err = computeError(lg1, isTrainingSet1);
 						 err += computeError(lg2, isTrainingSet2);
 						 if(err<errorBest){
 							 errorBest = err;
 							 indexBest = i;
 						 }
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						//e.printStackTrace();
 					}
 				}
 				Instance iExample = isTrainingSet.instance(indexBest);
 				cutValue = iExample.value(0);
 				distributeValues();
 				lg1.buildClassifier(isTrainingSet1);
 				lg2.buildClassifier(isTrainingSet2);
 				if(errorBest != Double.MAX_VALUE){
 				/*	System.out.println("Cutvalue-"+cutValue);					
 					System.out.println("X1="+lg1.coefficients()[0]+",C1="+lg1.coefficients()[2]);
 					System.out.println("X2="+lg2.coefficients()[0]+",C2="+lg2.coefficients()[2]);
 				*/
 				}
 			}			
 			
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 	}
 
 	public Double normalize(Rating record) {
 		Instance iExample = new SparseInstance(2);
 		if(record.get(index) == Double.NaN)
 			return null;
 		Double val = Utils.objectToDouble(record.get(index));
 
 		iExample.setDataset(isTrainingSet1);
 		try {
 			iExample.setValue((weka.core.Attribute) fvWekaAttributes
 					.get(0), val);
 			double[] fDistribution;
 			if (val <= cutValue)
 				fDistribution = lg1.distributionForInstance(iExample);
 			else
 				fDistribution = lg2.distributionForInstance(iExample);
 			double res = 0.0;
 			for (int i = 0; i < fDistribution.length; i++)
 				res += fDistribution[i];
 			return res;
 
 		} catch (Exception e) {
 			return 0.0;
 		}
 	}
 
 	public void computeCoefs(List<Double> l, List<Double> ratings) {
 
 	}
 
 	public int compare(Rating arg0, Rating arg1) {
 		if (normalize(arg0) > normalize(arg1))
 			return 1;
 		else if (normalize(arg0) < normalize(arg1))
 			return -1;
 		else
 			return 0;
 	}
 
 	@Override
 	public void addValue(Rating r) {
 		double d = r.getRecord().value(index);
 		Instance iExample = new SparseInstance(2);
 		iExample.setDataset(isTrainingSet1);
 		iExample.setValue((weka.core.Attribute) fvWekaAttributes
 				.get(0), d);
 		iExample.setValue((weka.core.Attribute) fvWekaAttributes
 				.get(1), r.getRating());
 		isTrainingSet1.add(iExample);
 		isTrainingSet.add(iExample);
 	}
 	@Override
 	public void process() {
 		computeRepresentants();
 	}
 	public void init(ContentDataSource data, ContentBased method, int attrIndex) {
 		index = attrIndex;
 		fvWekaAttributes = new ArrayList<Attribute>(2);
 		fvWekaAttributes.add(new weka.core.Attribute("X"));
 		fvWekaAttributes.add(new weka.core.Attribute("Rating"));
 
 		isTrainingSet = new Instances("Rel", fvWekaAttributes, 10);
 		isTrainingSet.setClassIndex(1);
 		isTrainingSet1 = new Instances("Rel", fvWekaAttributes, 10);
 		isTrainingSet1.setClassIndex(1);
 		isTrainingSet2 = new Instances("Rel", fvWekaAttributes, 10);
 		isTrainingSet2.setClassIndex(1);		
 	}
 
 	public Normalizer clone() {
 		return new Peak();
 	}
 
 	public void configClassifier(XMLConfiguration config, String section) {
 	}
 
 	@Override
 	public double compareTo(Normalizer n) {
 		if(!(n instanceof Peak))
 			return 0;
 		Peak n2 = (Peak)n;
 		double ratioPeak = Math.abs(cutValue-n2.cutValue);
 		ratioPeak-=min;
 		ratioPeak/=(max-min);
 		double ratioLgs = 0;
 		double[] lg1coef=lg1.coefficients();
 		double[] lg2coef=lg1.coefficients();
 		double[] n2lg1coef=n2.lg1.coefficients();
 		double[] n2lg2coef=n2.lg1.coefficients();
 		for (int i = 0; i < lg1coef.length && i < n2lg1coef.length; i++) {
 			ratioLgs+=Math.abs(lg1coef[i]-n2lg1coef[i]);
 		}
 		for (int i = 0; i < lg2coef.length && i < n2lg2coef.length; i++) {
 			ratioLgs+=Math.abs(lg2coef[i]-n2lg2coef[i]);
 		}
 		ratioLgs /=lg1coef.length+lg2coef.length;
 		return 0;
 	}
 }
