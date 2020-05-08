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
 
 import game.core.Block;
 import game.core.Dataset;
 import game.core.TrainingAlgorithm;
 import game.core.blocks.MetaEnsemble;
 import game.plugins.classifiers.DecisionTree;
 import game.plugins.classifiers.FeatureSelector;
 import game.plugins.classifiers.MajorityCombiner;
 import game.plugins.encoders.OneHotEncoder;
 import game.utils.Utils;
 
 import com.ios.ErrorCheck;
 import com.ios.Property;
 import com.ios.errorchecks.RangeCheck;
 import com.ios.listeners.ExactPathListener;
 import com.ios.triggers.MasterSlaveTrigger;
 import com.ios.triggers.SimpleTrigger;
 
 public class RandomForest extends TrainingAlgorithm<MetaEnsemble> {
 	
 	public double bootstrapPercent = 0.66;
 	
 	public int featuresPerNode = 0;
 	
 	public int trees = 10;
 	
 	public FeatureSelector selector;
 	
 	public RandomForest() {
 		addErrorCheck("bootstrapPercent", new RangeCheck(0.01, 1.0));
 		
 		addErrorCheck("featuresPerNode", new ErrorCheck<Integer>() {
 			public RandomForest wrapper = RandomForest.this;
 			@Override public String getError(Integer value) {
 				if (wrapper.block != null && wrapper.block.getParent(0) != null) {
 					if (value > wrapper.block.getParent(0).getFeatureNumber())
 						return "cannot be greater than input feature number (" + block.getParent(0).getFeatureNumber() + ")";
 				}
 				return null;
 			}
 		});
 		
 		addTrigger(new MasterSlaveTrigger(this, "block.parents.0", "selector.inputEncoder"));
 		
 		addTrigger(new SimpleTrigger(new ExactPathListener(new Property(this, "block"))) {
 			@Override
 			public void action(Property changedPath) {
 				Block block = changedPath.getContent();
 				block.setContent("outputEncoder", new OneHotEncoder());
 				block.setContent("combiner", new MajorityCombiner());
 			}
 		});
 	}
 
 	@Override
 	public boolean isCompatible(Block block) {
 		return block instanceof MetaEnsemble;
 	}
 
 	@Override
 	protected void train(Dataset dataset) {
 		int selectedFeatures = featuresPerNode == 0 ? (int)Utils.log2(block.getParent(0).getFeatureNumber()) + 1 : featuresPerNode;
 		
 		updateStatus(0.1, "start growing forest of " + trees + " trees using " + selectedFeatures + " features per node.");
 		
 		for(int i = 0; i < trees; i++) {
 			updateStatus(0.1 + 0.9*i/trees, "growing tree " + (i+1));
 			DecisionTree tree = new DecisionTree();
 			tree.setContent("template", block.template);
 			tree.setContent("trainingAlgorithm", new C45Like());
 			tree.parents.add(block.getParent(0));
 			tree.trainingAlgorithm.setContent("featuresPerNode", selectedFeatures);
 			tree.trainingAlgorithm.setContent("selector", selector);
 			executeAnotherTaskAndWait(0.1+0.9*(i+1)/trees, tree.trainingAlgorithm, dataset.getRandomSubset(bootstrapPercent)); // FIXME Bootstrap sample
 			block.combiner.parents.add(tree);
 		}
 	}
 
 	@Override
 	public String getManagedPropertyNames() {
 		return "combiner outputEncoder";
 	}
 
 }
