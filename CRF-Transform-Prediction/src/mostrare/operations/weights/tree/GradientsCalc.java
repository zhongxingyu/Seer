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

import mostrare.tree.Corpus;

/**
 * @author missi
 */
public interface GradientsCalc
{
	/**
	 * Returns the value of logLikelihood and puts the values of the gradients of logLikelihood in
	 * <code>gradients1</code>, <code>gradients2</code> and <code>gradients3</code>.
	 * 
	 * @param corpus
	 *            corpus
	 * @param gradients1
	 *            table of the gradients of logLikelihood relative to the one-node cliques
	 * @param gradients2
	 *            table of the gradients of logLikelihood relative to the two-nodes cliques
	 * @param gradients3
	 *            table of the gradients of logLikelihood relative to the three-nodes cliques
	 * @return the value of logLikelihood.
	 */
	public double calcGradients(Corpus corpus, double[] gradients1, double[] gradients1Pure, double[] gradients1Inverse, 
			double[] gradients2, double[] gradients3, double[] gradients3Observation);
}
