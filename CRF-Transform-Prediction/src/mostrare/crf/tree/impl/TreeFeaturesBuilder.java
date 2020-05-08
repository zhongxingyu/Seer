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
package mostrare.crf.tree.impl;

import java.util.ArrayList;
import java.util.List;

import mostrare.crf.tree.Feature1;
import mostrare.crf.tree.Feature1Inverse;
import mostrare.crf.tree.Feature1Pure;
import mostrare.crf.tree.Feature2;
import mostrare.crf.tree.Feature3;
import mostrare.crf.tree.Feature3Observation;
import mostrare.crf.tree.FeatureFactory;
import mostrare.crf.tree.FeaturesBuilder;
import cern.colt.list.DoubleArrayList;

/**
 * @author missi
 */
public class TreeFeaturesBuilder implements FeaturesBuilder
{
	private List<Feature1Pure>	features1Pure;
	private List<Feature1Inverse>	features1Inverse;
	private List<Feature1>	features1;
	private List<Feature2>	features2;
	private List<Feature3>	features3;
	private List<Feature3Observation>	features3Observation;

	private DoubleArrayList	weights1Pure;
	private DoubleArrayList	weights1Inverse;
	private DoubleArrayList	weights1;
	private DoubleArrayList	weights2;
	private DoubleArrayList	weights3;
	private DoubleArrayList	weights3Observation;

	private FeatureFactory	featureFactory;

	public TreeFeaturesBuilder(FeatureFactory featureFactory)
	{
		features1Pure = new ArrayList<Feature1Pure>();
		features1Inverse = new ArrayList<Feature1Inverse>();
		features1 = new ArrayList<Feature1>();
		features2 = new ArrayList<Feature2>();
		features3 = new ArrayList<Feature3>();
		features3Observation = new ArrayList<Feature3Observation>();
		weights1Pure = new DoubleArrayList();
		weights1Inverse = new DoubleArrayList();
		weights1 = new DoubleArrayList();
		weights2 = new DoubleArrayList();
		weights3 = new DoubleArrayList();
		weights3Observation = new DoubleArrayList();
		this.featureFactory = featureFactory;
	}

	public void addFeature1Pure(String nodetype, int nodeAnnotationTest, double weight)
	{
		int nbFeature = features1Pure.size();
		features1Pure.add(featureFactory.createPureNodeFeature(nodetype, nodeAnnotationTest,
				nbFeature));
		weights1Pure.add(weight);
	}
	/**
	 * Adds a node feature.
	 * 
	 * @param featureName
	 *            name of the feature
	 * @param nodeAnnotationTest
	 * @param xpaths
	 *            xpath expressions for observation tests
	 * @param xpathsString
	 *            xpath expressions for observation tests
	 * @param featureValueComputer
	 * @param weight
	 *            feature weight
	 */
	public void addFeature1(String nodetype, int nodeAnnotationTest, int characterIndex, 
			 double weight)
	{
		int nbFeature = features1.size();
		features1.add(featureFactory.createNodeFeature(nodetype, nodeAnnotationTest, characterIndex,
				nbFeature));
		weights1.add(weight);
	}
	
	public void addFeature1Inverse (String nodetype, int nodeAnnotationTest, int characterIndex, 
			 double weight)
	{
		int nbFeature = features1Inverse.size();
		features1Inverse.add(featureFactory.createNodeInverseFeature(nodetype, nodeAnnotationTest, characterIndex,
				nbFeature));
		weights1Inverse.add(weight);
	}
	
	/**
	 * Adds an edge feature.
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
	 * @param weight
	 *            feature weight
	 */
	public void addFeature2(String typeofparent, String typeofchild, 
			int parentAnnotationTest, int childAnnotationTest,
			double weight)
	{
		int nbFeature = features2.size();
		features2.add(featureFactory.createEdgeFeature (typeofparent, typeofchild,
				parentAnnotationTest, childAnnotationTest, nbFeature));
		weights2.add(weight);
	}

	/**
	 * Adds a triangle feature.
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
	 * @param weight
	 *            feature weight
	 */
	public void addFeature3(String typeofparent, String typeofleftchild, String typeofrightchild, 
			int parentAnnotationTest, int leftChildAnnotationTest, int rightChildAnnotationTest, 
			 double weight)
	{
		int nbFeature = features3.size();
		features3.add(featureFactory.createTriangleFeature(typeofparent, typeofleftchild, typeofrightchild,
				parentAnnotationTest, leftChildAnnotationTest, rightChildAnnotationTest,
				nbFeature));
		weights3.add(weight);
	}
	
	public void addFeature3Observation(String typeofparent, String typeofleftchild, String typeofrightchild, 
			int parentAnnotationTest, int leftChildAnnotationTest, int rightChildAnnotationTest, 
			 double weight)
	{
		int nbFeature = features3Observation.size();
		features3Observation.add(featureFactory.createTriangleFeatureWithObservation(typeofparent, typeofleftchild, 
				typeofrightchild, parentAnnotationTest, leftChildAnnotationTest, rightChildAnnotationTest,
				nbFeature));
		
		weights3Observation.add(weight);
	}
	
	@Override
	public List<Feature1Pure> getFeatures1Pure()
	{
		return features1Pure;
	}

	@Override
	public List<Feature1> getFeatures1()
	{
		return features1;
	}
	
	@Override
	public List<Feature1Inverse> getFeatures1Inverse()
	{
		return features1Inverse;
	}

	@Override
	public List<Feature2> getFeatures2()
	{
		return features2;
	}

	@Override
	public List<Feature3> getFeatures3()
	{
		return features3;
	}
	
	@Override
	public List<Feature3Observation> getFeatures3Observation()
	{
		return features3Observation;
	}

	@Override
	public DoubleArrayList getWeights1Pure()
	{
		return weights1Pure;
	}
	
	@Override
	public DoubleArrayList getWeights1Inverse()
	{
		return weights1Inverse;
	}
	
	@Override
	public DoubleArrayList getWeights1()
	{
		return weights1;
	}

	@Override
	public DoubleArrayList getWeights2()
	{
		return weights2;
	}

	@Override
	public DoubleArrayList getWeights3()
	{
		return weights3;
	}

	@Override
	public DoubleArrayList getWeights3Observation()
	{
		return weights3Observation;
	}
}
