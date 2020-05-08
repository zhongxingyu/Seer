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

/**
 * @author missi
 */
public interface FeatureFactory
{

	public abstract PureNodeFeature createPureNodeFeature(String nodetype, int nodeAnnotationTest, int nbFeature);
	
	/**
	 * Creates a node feature.
	 * 
	 * @param featureName
	 *            name of the feature
	 * @param nodeAnnotationTest
	 * @param xpaths
	 *            xpath expressions for observation tests
	 * @param xpathsString
	 *            xpath expressions for observation tests
	 * @param featureValueComputer
	 * @param nbFeature
	 *            index of the feature
	 * @return a node feature.
	 */
	
	public abstract NodeFeature createNodeFeature(String nodetype, int nodeAnnotationTest, int characterIndex, 
			int nbFeature);
	
	public abstract NodeFeatureInverse createNodeInverseFeature(String nodetype, int nodeAnnotationTest, int characterIndex, 
			int nbFeature);
	/**
	 * Creates an edge feature.
	 * 
	 * @param featureName
	 *            name of the feature
	 * @param parentAnnotationTest
	 * @param childAnnotationTest
	 * @param xpaths
	 *            xpath expressions for observation tests
	 * @param xpathsString
	 *            xpath expressions for observations tests
	 * @param featureValueComputer
	 * @param nbFeature
	 *            index of the feature
	 * @return an edge feature.
	 */
	
	public abstract EdgeFeature createEdgeFeature (String typeofparent, String typeofchild, 
			int parentAnnotationTest, int childAnnotationTest,
			 int numFeature);
	/**
	 * Creates a triangle feature.
	 * 
	 * @param featureName
	 *            name of the feature
	 * @param parentAnnotationTest
	 * @param leftChildAnnotationTest
	 * @param rightChildAnnotationTest
	 * @param xpaths
	 *            xpath expressions for observation tests
	 * @param xpathsString
	 *            xpath expressions for observation tests
	 * @param featureValueComputer
	 * @param nbFeature
	 *            index of the feature
	 * @return a triangle feature.
	 */

	public abstract TriangleFeature createTriangleFeature (String typeofparent, String typeofleftchild, String typeofrightchild, 
			int parentAnnotationTest, int leftChildAnnotationTest,
			int rightChildAnnotationTest, int numFeature);
	
	public abstract TriangleFeatureWithObservation createTriangleFeatureWithObservation (String typeofparent, String typeofleftchild, String typeofrightchild, 
			int parentAnnotationTest, int leftChildAnnotationTest,
			int rightChildAnnotationTest, int numFeature);
	

}