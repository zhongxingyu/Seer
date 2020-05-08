 /*******************************************************************************
  * Copyright (c) 2012 Emanuele.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele - initial API and implementation
  ******************************************************************************/
 package game.plugins.algorithms;
 
 import game.configuration.errorchecks.RangeCheck;
 import game.core.Block;
 import game.core.Dataset;
 import game.core.TrainingAlgorithm;
 import game.core.blocks.PredictionGraph;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class RandomSplitTraining extends TrainingAlgorithm<PredictionGraph> {
 	
 	public double splitPercent = 1.0;
 	
 	public RandomSplitTraining() {
		setOptionChecks("splitPercent", new RangeCheck(0.05, 1.00));
 	}
 
 	@Override
 	public boolean isCompatible(Block block) {
 		return block instanceof PredictionGraph;
 	}
 
 	@Override
 	protected void train(Dataset trainingSet) {
 		recursivelyTrainGraph(block.outputClassifier, trainingSet, 1.0/blocksToTrain(block));
 	}
 
 	private void recursivelyTrainGraph(Block current, Dataset trainingSet, double increase) {
 		for (Block parent: current.parents.getList(Block.class))
 			recursivelyTrainGraph(parent, trainingSet, increase);
 
 		if (!current.trained) {
 			executeAnotherTaskAndWait(getCurrentPercent()+increase, current.trainingAlgorithm, trainingSet.getRandomSubset(splitPercent));
 		}
 	}
 	
 	private int blocksToTrain(PredictionGraph graph) {
 		Set<Block> blocks = new HashSet<>();
 		countBlocks(graph.outputClassifier, blocks);
 		int count = 0;
 		for(Block block: blocks)
 			if (!block.trained) count++;
 		return count;
 	}
 	
 	private void countBlocks(Block current, Set<Block> blocks) {
 		if (!blocks.contains(current)) {
 			blocks.add(current);
 			for(Block parent: current.parents.getList(Block.class))
 				countBlocks(parent, blocks);
 		}
 	}
 
//	@Override
//	public String getTaskDescription() {
//		return String.format("graph training with random splits (dataset percent per split: %.2f%%)", splitPercent*100); 
//	}

 	@Override
 	public String[] getManagedBlockOptions() {
		return new String[]{};
 	}
 
 }
