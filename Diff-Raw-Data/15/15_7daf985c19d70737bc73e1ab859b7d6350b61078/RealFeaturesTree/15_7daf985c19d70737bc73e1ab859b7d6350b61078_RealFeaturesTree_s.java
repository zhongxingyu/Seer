 /*******************************************************************************
  * Copyright (c) 2012 Emanuele Tamponi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele Tamponi - initial API and implementation
  ******************************************************************************/
 package game.plugins.algorithms;
 
 import game.core.Dataset;
 import game.core.Dataset.SampleIterator;
 import game.core.DatasetTemplate;
 import game.core.Experiment;
 import game.core.Instance;
 import game.core.Sample;
 import game.core.ValueTemplate;
import game.core.trainingalgorithms.StandardClassifierTraining;
 import game.plugins.classifiers.Criterion;
 import game.plugins.classifiers.DecisionTree;
 import game.plugins.classifiers.Node;
 import game.plugins.classifiers.criteria.SingleThreshold;
 import game.plugins.valuetemplates.LabelTemplate;
 import game.plugins.valuetemplates.VectorTemplate;
 import game.utils.Utils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.math3.linear.ArrayRealVector;
 import org.apache.commons.math3.linear.RealVector;
 
public class RealFeaturesTree extends StandardClassifierTraining<DecisionTree> {
 	
 	public boolean binarySplitNominal = false;
 	
 	public int featuresPerNode = 0;
 	
 	public int minimumSamples = 2;
 	
 	public RealFeaturesTree() {
 		// TODO Error check for featuresPerNode
 	}
 
 	@Override
 	protected void train(Dataset dataset) {
 		Node root = new Node();
 		
 		recursiveTrain(dataset, root);
 		
 		block.setContent("root", root);
 	}
 
 	protected void recursiveTrain(Dataset dataset, Node node) {
 		if (dataset.size() <= minimumSamples) {
 			// This is a leaf
 			node.setProbability(getProbabilities(dataset));
 			return;
 		}
 		
 		if (information(dataset) == 0) {
 			// This is a leaf
 			node.setProbability(getProbabilities(dataset));
 			return;
 		}
 		
 		Criterion criterion = bestCriterion(dataset);
 		if (criterion == null) {
 			// This is a leaf
 			node.setProbability(getProbabilities(dataset));
 			return;
 		}
 		
 		node.setCriterion(criterion);
 		for(Dataset split: split(dataset, criterion)) {
 			Node child = new Node();
 			node.getChildren().add(child);
 			recursiveTrain(split, child);
 		}
 	}
 	
 	protected static class CriterionWithGain {
 		private Criterion criterion;
 		private double gain;
 		
 		public CriterionWithGain(Criterion criterion, double gain) {
 			this.criterion = criterion;
 			this.gain = gain;
 		}
 		
 		public Criterion getCriterion() {
 			return criterion;
 		}
 		
 		public double getGain() {
 			return gain;
 		}
 	}
 	
 	protected Criterion bestCriterion(Dataset dataset) {
 		CriterionWithGain ret = new CriterionWithGain(null, 0);
 		
 		List<Integer> featurePositions = Utils.range(0, block.datasetTemplate.sourceTemplate.size());
 		int totalAttempts = featuresPerNode == 0 ? featurePositions.size() : featuresPerNode;
 		if (featuresPerNode > 0)
 			Collections.shuffle(featurePositions, Experiment.getRandom());
 		
 		for(int i = 0; i < totalAttempts; i++) {
 			CriterionWithGain current = bestCriterionFor(featurePositions.get(i), dataset);
 			if (current.gain > ret.gain)
 				ret = current;
 		}
 		
 		return ret.criterion;
 	}
 	
 	protected static class FeatureValue implements Comparable<FeatureValue> {
 		private double value;
 		private String label;
 		
 		private FeatureValue(double value, String label) {
 			this.value = value;
 			this.label = label;
 		}
 		
 		@Override
 		public int compareTo(FeatureValue o) {
 			return Double.compare(this.value, o.value);
 		}
 	}
 	
 	protected CriterionWithGain bestCriterionFor(int featureIndex, Dataset dataset) {
 		CriterionWithGain ret = new CriterionWithGain(null, 0);
 			
 		List<FeatureValue> values = new ArrayList<>(dataset.size());
 		SampleIterator it = dataset.sampleIterator();
 		while(it.hasNext()) {
 			Sample sample = it.next();
 			values.add(new FeatureValue(
 					sample.getSource().get(featureIndex, RealVector.class).getEntry(0), 
 					sample.getTarget().get(0, String.class)));
 		}
 		Collections.sort(values);
 		
 		Map<String, Double> lesserCount = new HashMap<>();
 		Map<String, Double> greaterCount = countPerLabel(values);
 		double information = information(greaterCount);
 		double threshold = Double.NaN;
 		
 		FeatureValue prev = values.get(0);
 		int count = 1;
 		for(int i = 1; i < values.size(); i++) {
 			FeatureValue curr = values.get(i);
 			if (!lesserCount.containsKey(prev.label))
 				lesserCount.put(prev.label, 0.0);
 			if (!prev.label.equals(curr.label)) {
 				lesserCount.put(prev.label, lesserCount.get(prev.label)+count);
 				greaterCount.put(prev.label, greaterCount.get(prev.label)-count);
 				count = 1;
 				if (prev.value < curr.value) {
 					double gain = information + gain(lesserCount, greaterCount);
 					if (gain > ret.gain) {
 						threshold = (prev.value + curr.value)/2;
 						ret.gain = gain;
 					}
 				}
 			} else {
 				count++;
 			}
 			prev = curr;
 		}
 		
 		if (!Double.isNaN(threshold)) {
 			SingleThreshold c = new SingleThreshold(featureIndex, threshold);
 			ret.criterion = c;
 		}
 		return ret;
 	}
 	
 	private Map<String, Double> countPerLabel(List<FeatureValue> values) {
 		Map<String, Double> ret = new HashMap<>();
 		for(FeatureValue value: values) {
 			if (!ret.containsKey(value.label))
 				ret.put(value.label, 0.0);
 			ret.put(value.label, ret.get(value.label)+1);
 		}
 		return ret;
 	}
 
 	private double gain(Map<String, Double> split1, Map<String, Double> split2) {
 		double ret = 0;
 		
 		double totalCount = count(split1) + count(split2);
 		ret -= information(split1) * count(split1) / totalCount;
 		ret -= information(split2) * count(split2) / totalCount;
 		
 		return ret;
 	}
 	
 	private double information(Map<String, Double> map) {
 		double[] prob = getProbabilities(map);
 		double info = 0;
 		for (double p: prob)
 			if (p > 0)
 				info += p * Math.log(p);
 		return -info;
 	}
 
 	private double[] getProbabilities(Map<String, Double> map) {
 		double total = count(map);
 		double[] ret = new double[map.keySet().size()];
 		int i = 0;
 		for(double count: map.values())
 			ret[i++] = count / total;
 		return ret;
 	}
 
 	private double count(Map<String, Double> map) {
 		double ret = 0;
 		for(double count: map.values())
 			ret += count;
 		return ret;
 	}
 
 	protected double information(Dataset dataset) {
 		RealVector prob = getProbabilities(dataset);
 		double info = 0;
 		for (double p: prob.toArray())
 			if (p > 0)
 				info += p * Math.log(p);
 		return -info;
 	}
 	
 	protected double gain(List<Dataset> splits) {
 		double ret = 0;
 		double total = 0;
 		for (Dataset dataset: splits) {
 			ret -= dataset.size() * information(dataset);
 			total += dataset.size();
 		}
 		ret = ret / total;
 		return ret;
 	}
 
 	protected List<Dataset> split(Dataset dataset, Criterion criterion) {
 		List<Dataset> splits = new ArrayList<>();
 		splits.add(new Dataset(block.datasetTemplate));
 		splits.add(new Dataset(block.datasetTemplate));
 		
 		Iterator<Instance> it = dataset.iterator();
 		while(it.hasNext()) {
 			Instance instance = it.next();
 			int split = criterion.decide(instance.getSource().get(0));
 			splits.get(split).add(instance);
 		}
 		
 		return splits;
 	}
 
 	private RealVector getProbabilities(Dataset dataset) {
 		Map<String, Double> prob = new HashMap<>();
 		Iterator<Instance> it = dataset.iterator();
 		double sum = 0;
 		while(it.hasNext()) {
 			Instance i = it.next();
 			String key = (String) i.getTarget().get(0).get(0);
 			if (!prob.containsKey(key))
 				prob.put(key, 0.0);
 			prob.put(key, prob.get(key)+1.0);
 			sum++;
 		}
 		
 		List<String> labels = dataset.getTemplate().targetTemplate.getSingleton().getContent("labels");
 		
 		RealVector ret = new ArrayRealVector(labels.size());
 		for(String key: prob.keySet()) {
 			ret.setEntry(labels.indexOf(key), prob.get(key)/sum);
 		}
 
 		return ret;
 	}
 
 	@Override
 	protected String getTrainingPropertyNames() {
 		return "root";
 	}
 
 	@Override
 	protected boolean isCompatible(DatasetTemplate template) {
 		if (template.sequences != false || !template.targetTemplate.isSingletonTemplate(LabelTemplate.class))
 			return false;
 		if (template.sourceTemplate.isEmpty())
 			return false;
 		for (ValueTemplate tpl: template.sourceTemplate) {
 			if (!(tpl instanceof VectorTemplate) || tpl.getContent("dimension", int.class) != 1)
 				return false;
 		}
 		
 		return true;
 	}
 
 }
