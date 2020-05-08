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
 package game.core.blocks;
 
 import game.configuration.Configurable;
 import game.configuration.ErrorCheck;
 import game.configuration.errorchecks.SizeCheck;
import game.core.Dataset;
 
 import java.util.List;
 
 
 
 public abstract class Combiner extends Transducer {
 	
	public boolean trained = false;
	
 	public Combiner() {
		setPrivateOptions("trained");
		
 		setOptionChecks("parents", new SizeCheck(1),
 		new ErrorCheck<List>() {
 			@Override public String getError(List value) {
 				for (Object element: value) {
 					if (!(element instanceof Transducer))
 						return "parents of Combiner can only be Transducers";
 				}
 				return null;
 			}
 		},
 		new ErrorCheck<List>() {
 			@Override public String getError(List value) {
 				if (value.isEmpty())
 					return null;
 				for (Object parent: value) {
 					if (!outputEncoder.equals(((Configurable)parent).getOption("outputEncoder")))
 						return "parent Transducers must have the same outputEncoder as this Combiner";
 				}
 				return null;
 			}
 		});
 	}
 
	@Override
	public boolean isTrained() {
		return trained;
	}

	@Override
	protected void train(Dataset trainingSet) {
		trained = true;
	}

 }
