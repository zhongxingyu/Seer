/*
 * Copyright (C) 2006-2007 MOSTRARE INRIA Project
 * 
 * This file is part of XCRF, an implementation of CRFs for trees (http://treecrf.gforge.inria.fr)
 * 
 * XCRF is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * XCRF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XCRF; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mostrare.operations.weights.tree;

import mostrare.crf.tree.CRF;
import mostrare.tree.Corpus;

/**
 * This implantation uses the Bridge design pattern.
 * 
 * @author missi
 */
public abstract class AbstractWeightsFinder implements WeightsFinder
{
	private GradientsCalc	gradCalc;

	protected double calcGradients(Corpus corpus, double[] gradients1, double[] gradients1Pure, double[] gradients1Inverse,
			double[] gradients2, double[] gradients3, double[] gradients3Observation)
	{
		return gradCalc.calcGradients(corpus, gradients1, gradients1Pure, gradients1Inverse, gradients2, 
				gradients3, gradients3Observation);
	}

	/**
	 * Sets the method to calculate the gradients of logLikelihood.
	 * 
	 * @param gradCalc
	 */
	protected void setGradientsCalc(GradientsCalc gradCalc)
	{
		this.gradCalc = gradCalc;
	}

	@Override
	public abstract void argMaxLogLikelihood(Corpus corpus, CRF crf);

}
