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
package mostrare.crf.tree;

import java.util.List;

import cern.colt.list.DoubleArrayList;

/**
 * 
 * @author missi
 *
 */
public interface FeaturesBuilder
{
	public abstract List<Feature1Pure> getFeatures1Pure();
	/**
	 * Returns the list of node features.
	 * @return the list of node features.
	 */
	public abstract List<Feature1> getFeatures1();

	public abstract List<Feature1Inverse> getFeatures1Inverse();

	/**
	 * Returns the list of edge features.
	 * @return the list of edge features.
	 */
	public abstract List<Feature2> getFeatures2();

	/**
	 * Returns the list of triangle features.
	 * @return the list of triangle features.
	 */
	public abstract List<Feature3> getFeatures3();
	
	public abstract List<Feature3Observation> getFeatures3Observation();

	public abstract DoubleArrayList getWeights1Pure();
	
	/**
	 * Returns the list of node feature weights.
	 * @return the list of node feature weights.
	 */
	public abstract DoubleArrayList getWeights1();

	public abstract DoubleArrayList getWeights1Inverse();
	/**
	 * Returns the list of edge feature weights.
	 * @return the list of edge feature weights.
	 */
	public abstract DoubleArrayList getWeights2();

	/**
	 * Returns the list of triangle feature weights.
	 * @return the list of triangle feature weights.
	 */
	public abstract DoubleArrayList getWeights3();
	
	public abstract DoubleArrayList getWeights3Observation();

}