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
 import game.core.DatasetTemplate;
 import game.core.blocks.Ensemble;
import game.core.trainingalgorithms.ClassifierTrainingAlgorithm;
 import game.plugins.blocks.combinationstrategies.Majority;
 import game.plugins.blocks.decoders.ProbabilityDecoder;
 import game.plugins.classifiers.DecisionTree;
 import game.utils.Utils;
 
 import com.ios.errorchecks.RangeCheck;
 
 
public class RandomForest extends ClassifierTrainingAlgorithm<Ensemble> {
 	
 	public double bootstrapPercent = 0.66;
 	
 	public int featuresPerNode = 0;
 	
 	public int trees = 10;
 	
 	public RandomForest() {
 		addErrorCheck("bootstrapPercent", new RangeCheck(0.01, 1.0));
 	}
 
 	@Override
 	protected void train(Dataset dataset) {
 		int featuresPerNode = this.featuresPerNode == 0 ? (int)(Utils.log2(block.datasetTemplate.sourceTemplate.size()) + 1) : this.featuresPerNode;
 		
 		block.setContent("strategy", new Majority());
 		
 		updateStatus(0.1, "start growing forest of " + trees + " trees using " + featuresPerNode + " features per node.");
 		
 		for(int i = 0; i < trees; i++) {
 			updateStatus(0.1 + 0.9*i/trees, "growing tree " + (i+1));
 			DecisionTree tree = new DecisionTree();
 			block.classifiers.add(tree);
 			tree.setContent("trainingAlgorithm", new RealFeaturesTree());
 			tree.setContent("decoder", new ProbabilityDecoder());
 			tree.trainingAlgorithm.setContent("featuresPerNode", featuresPerNode);
 			
 			executeAnotherTaskAndWait(0.1+0.9*(i+1)/trees, tree.trainingAlgorithm, dataset.getRandomSubset(bootstrapPercent)); // FIXME Bootstrap sample
 		}
 	}
 
 	@Override
 	protected String getTrainingPropertyNames() {
 		return "strategy classifiers";
 	}
 
 	@Override
 	protected boolean isCompatible(DatasetTemplate datasetTemplate) {
 		return new RealFeaturesTree().isCompatible(datasetTemplate);
 	}
 
 }
