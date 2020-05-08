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
 package game.core;
 
 import game.configuration.errorchecks.CompatibilityCheck;
 import game.plugins.constraints.Compatible;
 
 public abstract class TrainingAlgorithm<B extends Block> extends LongTask<Void, Dataset> implements Compatible<Block> {
 	
 	public B block;
 	
 	public TrainingAlgorithm() {
 		setOptionChecks("block", new CompatibilityCheck(this));
 	}
 	
 	protected abstract void train(Dataset dataset);
 	
 	public abstract String[] getManagedBlockOptions();
 
 	@Override
 	public Void execute(Dataset dataset) {
		if (!block.trained)
 			train(dataset);
 		block.trained = true;
 		
 		return null;
 	}
 
 }
