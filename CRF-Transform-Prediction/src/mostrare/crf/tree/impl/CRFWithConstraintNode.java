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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mostrare.coltAdaptation.list.NodeArrayList;
import mostrare.crf.tree.AnnotationsEnum;
import mostrare.crf.tree.CRF;
import mostrare.crf.tree.CharacterEnum;
import mostrare.crf.tree.Feature1;
import mostrare.crf.tree.Feature1Inverse;
import mostrare.crf.tree.Feature1Pure;
import mostrare.crf.tree.Feature2;
import mostrare.crf.tree.Feature3;
import mostrare.crf.tree.Feature3Observation;
import mostrare.crf.tree.FeaturesBuilder;
import mostrare.matrix.miniImpl.DenseIntArrayListMatrix2D;
import mostrare.matrix.miniImpl.DenseIntArrayListMatrix3D;
import mostrare.matrix.miniImpl.IntArrayListMatrix2D;
import mostrare.matrix.miniImpl.IntArrayListMatrix3D;
import mostrare.tree.Node;
import mostrare.tree.Tree;
import cern.colt.list.IntArrayList;

public class CRFWithConstraintNode implements CRF
{
	/**
	 * one-node features
	 */
	protected Feature1Pure[]			features1Pure;
	
	protected Feature1Inverse[]			features1Inverse;

	protected Feature1[]			features1;
	/**
	 * two-nodes features
	 */
	protected Feature2[]			features2;

	/**
	 * three-nodes features
	 */
	protected Feature3[]			features3;
	
	protected Feature3Observation[]			features3Observation;

	protected double[]				weights1Pure;
	
	protected double[]				weights1Inverse;
	/**
	 * weights relative to the one-node features
	 */
	protected double[]				weights1;

	/**
	 * weights relative to the two-nodes features
	 */
	protected double[]				weights2;

	/**
	 * weights relative to the three-nodes features
	 */
	protected double[]				weights3;
	
	protected double[]				weights3Observation;

	/**
	 * feature1IndexForValidAnnTests[x] returns the list of the index of node features whose
	 * annotation tests are valid against the annotation (x)
	 */
	protected IntArrayList[]		feature1IndexForValidAnnTests;
	
	protected IntArrayList[]		feature1InverseIndexForValidAnnTests;
	
	protected IntArrayList[]		feature1PureIndexForValidAnnTests;

	/**
	 * feature2IndexForValidAnnTests[x][y] returns the list of the index of edge features whose
	 * annotation tests are valid against the annotation pair (x,y)
	 */
	protected IntArrayListMatrix2D	feature2IndexForValidAnnTests;

	/**
	 * feature3IndexForValidAnnTests[x][y][z] returns the list of the index of triangle features
	 * whose annotation tests are valid against the annotation triplet (x,y,z)
	 */
	protected IntArrayListMatrix3D	feature3IndexForValidAnnTests;
	
	protected IntArrayListMatrix3D	feature3ObservationIndexForValidAnnTests;

	protected AnnotationsEnum		annotations;
	public CharacterEnum characters;

	protected Feature1Pure[][]		observableFeatures1Pure;
	protected Feature1Inverse[][]		observableFeatures1Inverse;
	protected Feature1[][]			observableFeatures1;
	protected Feature2[][]			observableFeatures2;
	protected Feature3[][]			observableFeatures3;
	protected Feature3Observation[][]			observableFeatures3Observation;
	
	public Map<String, ArrayList<String>>  nodetransformmap;
	
	public Map<String, ArrayList<Integer>>  nodetransformmapinteger;

	public CRFWithConstraintNode (String[] labels, int emptyAnnotationIndex, String[] allcharacters,
			Map<String, ArrayList<String>> nodetransformrelation, FeaturesBuilder featuresBuilder)
	{
		// store annotations
		this.annotations = new AnnotationsEnumImpl(labels, emptyAnnotationIndex);
		this.characters = new CharactersEnumImpl(allcharacters);
		this.nodetransformmap = nodetransformrelation;
		this.nodetransformmapinteger = new HashMap<String, ArrayList<Integer>>();
		
		transformmapdomain();
		
		// store features
		List<Feature1Inverse> features1Inverse = featuresBuilder.getFeatures1Inverse();
		List<Feature1Pure> features1Pure = featuresBuilder.getFeatures1Pure();
		List<Feature1> features1 = featuresBuilder.getFeatures1();
		List<Feature2> features2 = featuresBuilder.getFeatures2();
		List<Feature3> features3 = featuresBuilder.getFeatures3();
		List<Feature3Observation> features3Observation = featuresBuilder.getFeatures3Observation();

		this.features1Inverse = new Feature1Inverse[features1Inverse.size()];
		this.features1Pure =new Feature1Pure[features1Pure.size()];
		this.features1 = new Feature1[features1.size()];
		this.features2 = new Feature2[features2.size()];
		this.features3 = new Feature3[features3.size()];
		this.features3Observation = new Feature3Observation[features3Observation.size()];
		
		int featureIndex = 0;
		for (Feature1 feature : features1)
		{
			this.features1[featureIndex] = feature;
			featureIndex += 1;
		}
		
		featureIndex = 0;
		for (Feature1Inverse feature : features1Inverse)
		{
			this.features1Inverse[featureIndex] = feature;
			featureIndex += 1;
		}
		
		featureIndex = 0;
		for (Feature1Pure feature : features1Pure)
		{
			this.features1Pure[featureIndex] = feature;
			featureIndex += 1;
		}
		
		featureIndex = 0;
		for (Feature2 feature : features2)
		{
			this.features2[featureIndex] = feature;
			featureIndex += 1;
		}
		
		featureIndex = 0;
		for (Feature3 feature : features3)
		{
			this.features3[featureIndex] = feature;
			featureIndex += 1;
		}
		
		featureIndex = 0;
		for (Feature3Observation feature : features3Observation)
		{
			this.features3Observation[featureIndex] = feature;
			featureIndex += 1;
		}
		
		// store weights
		this.weights1Inverse = featuresBuilder.getWeights1Inverse().elements();
		this.weights1Pure = featuresBuilder.getWeights1Pure().elements();
		this.weights1 = featuresBuilder.getWeights1().elements();
		this.weights2 = featuresBuilder.getWeights2().elements();
		this.weights3 = featuresBuilder.getWeights3().elements();
		this.weights3Observation = featuresBuilder.getWeights3Observation().elements();
		//
		fillFeaturesForValidAnnotationTest();
	}
	
	public void transformmapdomain()
	{
		 for(String key: nodetransformmap.keySet()) {
	         ArrayList<Integer> transformIntegers = new ArrayList<Integer>(); 

			 ArrayList<String> transforminfo=nodetransformmap.get(key);
			 
			 for(int innerindex=0; innerindex<transforminfo.size(); innerindex++ ) {
				 String transformname=transforminfo.get(innerindex);
				 transformIntegers.add(getAnnotationIndex(transformname));
			 }
			 
			 nodetransformmapinteger.put(key, transformIntegers);
		 }
	}
	
	@Override
	public CharacterEnum getCharactersEnum()
	{
		return characters;
	}
	
	@Override
	public AnnotationsEnum getAnnotationsEnum()
	{
		return annotations;
	}
	
	@Override
	public IntArrayList getAnnotationArray()
	{
		return annotations.getAnnotationArray();
	}

	@Override
	public String getAnnotationText(int index)
	{
		return annotations.getAnnotationText(index);
	}

	@Override
	public int getAnnotationIndex(String annotation)
	{
		return annotations.getAnnotationIndex(annotation);
	}

	@Override
	public int getAnnotationsNumber()
	{
		return annotations.getAnnotationsNumber();
	}
	
	@Override
	public String getCharacterText(int index)
	{
		return characters.getCharacterText(index);
	}

	@Override
	public int getCharacterIndex(String character)
	{
		return characters.getCharacterIndex(character);
	}

	@Override
	public int getCharactersNumber()
	{
		return characters.getCharactersNumber();
	}

	@Override
	public boolean observationTestsShouldBeApplied()
	{
		return true;
	}

	@Override
	public int getNoAnnotationIndex()
	{
		return annotations.getNoAnnotationIndex();
	}
	
	@Override
	public double getFeature1PureValue(int featureIndex, Node node, int annotationIndex)
	{
		return features1Pure[featureIndex].getValue(node, annotationIndex);
	}
	
	@Override
	public double getFeature1InverseValue(int featureIndex, Node node, int annotationIndex)
	{
		return features1Inverse[featureIndex].getValue(node, annotationIndex);
	}

	@Override
	public double getFeature1Value(int featureIndex, Node node, int annotationIndex)
	{
		return features1[featureIndex].getValue(node, annotationIndex);
	}

	@Override 
	public double getFeature2Value(int featureIndex, Node childnode, 
			int parentAnnotationIndex, int childAnnotationIndex)
	{
		return features2[featureIndex].getValue(childnode, parentAnnotationIndex, childAnnotationIndex);
	}

	@Override
	public double getFeature3Value(int featureIndex, Node leftchild,
			int parentAnnotationIndex, int childAnnotationIndex,
			int siblingAnnotationIndex)
	{
		return features3[featureIndex].getValue(leftchild, parentAnnotationIndex, childAnnotationIndex,
				siblingAnnotationIndex);
	}
	
	@Override
	public double getFeature3ObservationValue(int featureIndex, Node leftchild,
			int parentAnnotationIndex, int childAnnotationIndex,
			int siblingAnnotationIndex)
	{
		return features3Observation[featureIndex].getValue(leftchild, parentAnnotationIndex, childAnnotationIndex,
				siblingAnnotationIndex);
	}

	@Override
	public double getWeight1Pure(int index)
	{
		return weights1Pure[index];
	}
	
	@Override
	public double getWeight1(int index)
	{
		return weights1[index];
	}
	
	@Override
	public double getWeight1Inverse(int index)
	{
		return weights1Inverse[index];
	}

	@Override
	public double getWeight2(int index)
	{
		return weights2[index];
	}

	@Override
	public double getWeight3(int index)
	{
		return weights3[index];
	}
	
	@Override
	public double getWeight3Observation(int index)
	{
		return weights3Observation[index];
	}
	
	@Override
	public void setWeight1Pure(int index, double value)
	{
		weights1Pure[index] = value;
	}
	
	@Override
	public void setWeight1Inverse(int index, double value)
	{
		weights1Inverse[index] = value;
	}

	@Override
	public void setWeight1(int index, double value)
	{
		weights1[index] = value;
	}

	@Override
	public void setWeight2(int index, double value)
	{
		weights2[index] = value;
	}

	@Override
	public void setWeight3(int index, double value)
	{
		weights3[index] = value;
	}
	
	@Override
	public void setWeight3Observation(int index, double value)
	{
		weights3Observation[index] = value;
	}

	@Override
	public int getFeatures1PureNumber()
	{
		return features1Pure.length;
	}
	
	@Override
	public int getFeatures1InverseNumber()
	{
		return features1Inverse.length;
	}
	
	@Override
	public int getFeatures1Number()
	{
		return features1.length;
	}

	@Override
	public int getFeatures2Number()
	{
		return features2.length;
	}

	@Override
	public int getFeatures3Number()
	{
		return features3.length;
	}
	
	@Override
	public int getFeatures3ObservationNumber()
	{
		return features3Observation.length;
	}

	@Override
	public void applyObservationTests(Tree tree)
	{
		IntArrayList observableFeatureIndex;
		Node[] nodes = tree.getNodes();
			
		int treeIndex = tree.getIndex();
		
		// for each node observation feature, computes the nodes the observation tests are successful with 
		observableFeatureIndex = new IntArrayList();
		for (Feature1 feature : features1)
		{
			NodeArrayList observableNodes = new NodeArrayList();
			for (Node node : nodes)
			{
				if (feature.getObservationTestValue(node)) {
					observableNodes.add(node);
				}
			}
			if (observableNodes.size() > 0)
			{
				observableNodes.trimToSize();
				observableFeatureIndex.add(feature.getIndex());
				feature.setObservableNodes(observableNodes.elements(), treeIndex);
			}
			else
				feature.setObservableNodes(new Node[0], treeIndex);
		}

		Feature1[] observableFeatures1 = new Feature1[observableFeatureIndex.size()];
		for (int index = 0; index < observableFeatureIndex.size(); index++)
			observableFeatures1[index] = features1[observableFeatureIndex.getQuick(index)];
		this.observableFeatures1[treeIndex] = observableFeatures1;
		
		// for each node observation inverse feature, computes the nodes the observation tests are successful with 
		observableFeatureIndex = new IntArrayList();
		for (Feature1Inverse feature : features1Inverse)
		{
			NodeArrayList observableNodes = new NodeArrayList();
			for (Node node : nodes)
			{
				if (feature.getObservationTestValue(node))
					observableNodes.add(node);
			}
			if (observableNodes.size() > 0)
			{
				observableNodes.trimToSize();
				observableFeatureIndex.add(feature.getIndex());
				feature.setObservableNodes(observableNodes.elements(), treeIndex);
			}
			else
				feature.setObservableNodes(new Node[0], treeIndex);
		}

		Feature1Inverse[] observableFeatures1Inverse = new Feature1Inverse[observableFeatureIndex.size()];
		for (int index = 0; index < observableFeatureIndex.size(); index++)
			observableFeatures1Inverse[index] = features1Inverse[observableFeatureIndex.getQuick(index)];
		this.observableFeatures1Inverse[treeIndex] = observableFeatures1Inverse;
		
		// for each node feature, computes the nodes the observation tests are successful with 
		observableFeatureIndex = new IntArrayList();
		for (Feature1Pure feature : features1Pure)
		{
			 NodeArrayList observableNodes = new NodeArrayList();
			 for (Node node : nodes)
			 {
				 if (feature.getObservationTestValue(node))
					 observableNodes.add(node);
			 }
			 if (observableNodes.size() > 0)
			 {
				 observableNodes.trimToSize();
				 observableFeatureIndex.add(feature.getIndex());
				 feature.setObservableNodes(observableNodes.elements(), treeIndex);
			 }
			 else
				 feature.setObservableNodes(new Node[0], treeIndex);
		 }

		 Feature1Pure[] observableFeatures1Pure = new Feature1Pure[observableFeatureIndex.size()];
		 for (int index = 0; index < observableFeatureIndex.size(); index++)
			 observableFeatures1Pure[index] = features1Pure[observableFeatureIndex.getQuick(index)];
		 this.observableFeatures1Pure[treeIndex] = observableFeatures1Pure;
		
		// for each edge feature, computes the nodes the observation tests are successful with 
		observableFeatureIndex = new IntArrayList();
		for (Feature2 feature : features2)
		{
			NodeArrayList observableNodes = new NodeArrayList();
			for (Node node : nodes)
			{
				if (feature.getObservationTestValueChild(node))
					observableNodes.add(node);
			}
			if (observableNodes.size() > 0)
			{
				observableNodes.trimToSize();
				observableFeatureIndex.add(feature.getIndex());
				feature.setObservableNodesChild(observableNodes.elements(), treeIndex);
			}
			else
				feature.setObservableNodesChild(new Node[0], treeIndex);
		}
		Feature2[] observableFeatures2 = new Feature2[observableFeatureIndex.size()];
		for (int index = 0; index < observableFeatureIndex.size(); index++)
			observableFeatures2[index] = features2[observableFeatureIndex.get(index)];
		this.observableFeatures2[treeIndex] = observableFeatures2;
		
		// for each triangle feature, computes the nodes the observation tests are successful with 
		observableFeatureIndex = new IntArrayList();
		for (Feature3 feature : features3)
		{
			NodeArrayList observableNodes = new NodeArrayList();
			for (Node node : nodes)
			{
				if (feature.getObservationTestValueLeftChild(node))
					observableNodes.add(node);
			}
			if (observableNodes.size() > 0)
			{
				observableNodes.trimToSize();
				observableFeatureIndex.add(feature.getIndex());
				feature.setObservableNodesLeftChild(observableNodes.elements(), treeIndex);
			}
			else
				feature.setObservableNodesLeftChild(new Node[0], treeIndex);
		}
		Feature3[] observableFeatures3 = new Feature3[observableFeatureIndex.size()];
		for (int index = 0; index < observableFeatureIndex.size(); index++)
			observableFeatures3[index] = features3[observableFeatureIndex.get(index)];
		this.observableFeatures3[treeIndex] = observableFeatures3;
		
		// for each triangle observation feature, computes the nodes the observation tests are successful with 
		observableFeatureIndex = new IntArrayList();
		for (Feature3Observation feature : features3Observation)
		{
			NodeArrayList observableNodes = new NodeArrayList();
			for (Node node : nodes)
			{
				if (feature.getObservationTestValueLeftChild(node))
					observableNodes.add(node);
			}
			if (observableNodes.size() > 0)
			{
				observableNodes.trimToSize();
				observableFeatureIndex.add(feature.getIndex());
				feature.setObservableNodesLeftChild(observableNodes.elements(), treeIndex);
			}
			else
				feature.setObservableNodesLeftChild(new Node[0], treeIndex);
		}
		Feature3Observation[] observableFeatures3Observation = new Feature3Observation[observableFeatureIndex.size()];
		for (int index = 0; index < observableFeatureIndex.size(); index++)
			observableFeatures3Observation[index] = features3Observation[observableFeatureIndex.get(index)];
		this.observableFeatures3Observation[treeIndex] = observableFeatures3Observation;
		
	}

	@Override
	public Feature1Pure[] getFeatures1Pure()
	{
		return features1Pure;
	}
	
	@Override
	public Feature1Inverse[] getFeatures1Inverse()
	{
		return features1Inverse;
	}
	
	@Override
	public Feature1[] getFeatures1()
	{
		return features1;
	}

	@Override
	public Feature2[] getFeatures2()
	{
		return features2;
	}

	@Override
	public Feature3[] getFeatures3()
	{
		return features3;
	}

	@Override
	public Feature3Observation[] getFeatures3Observation()
	{
		return features3Observation;
	}
	/**
	 * Stores for each clique annotation what features may return a value according to their
	 * annotation tests.
	 */
	private void fillFeaturesForValidAnnotationTest()
	{
		// initialization
		feature1IndexForValidAnnTests = new IntArrayList[annotations.getAnnotationsNumber()];
		for (int i = 0; i < annotations.getAnnotationsNumber(); i++)
			feature1IndexForValidAnnTests[i] = new IntArrayList();
		
		feature1InverseIndexForValidAnnTests = new IntArrayList[annotations.getAnnotationsNumber()];
		for (int i = 0; i < annotations.getAnnotationsNumber(); i++)
			feature1InverseIndexForValidAnnTests[i] = new IntArrayList();
		
		feature1PureIndexForValidAnnTests = new IntArrayList[annotations.getAnnotationsNumber()];
		for (int i = 0; i < annotations.getAnnotationsNumber(); i++)
			feature1PureIndexForValidAnnTests[i] = new IntArrayList();
		
		feature2IndexForValidAnnTests = new DenseIntArrayListMatrix2D(annotations
				.getAnnotationsNumber(), annotations.getAnnotationsNumber());
		feature3IndexForValidAnnTests = new DenseIntArrayListMatrix3D(annotations
				.getAnnotationsNumber(), annotations.getAnnotationsNumber());
		feature3ObservationIndexForValidAnnTests = new DenseIntArrayListMatrix3D(annotations
				.getAnnotationsNumber(), annotations.getAnnotationsNumber());
		
		// for each node observation feature, gets the annotation used in the annotation test and accordingly updates the storage data 
		for (int featureIndex = 0; featureIndex < features1.length; featureIndex++)
		{
			Feature1 feature = features1[featureIndex];
			//
			feature1IndexForValidAnnTests[feature.getAnnotationTestVar()].add(featureIndex);
		}
		
		// for each node observation Inverse feature, gets the annotation used in the annotation test and accordingly updates the storage data 
		for (int featureIndex = 0; featureIndex < features1Inverse.length; featureIndex++)
		{
			 Feature1Inverse feature = features1Inverse[featureIndex];
					//
			 feature1InverseIndexForValidAnnTests[feature.getAnnotationTestVar()].add(featureIndex);
		}
				
		// for each node feature, gets the annotation used in the annotation test and accordingly updates the storage data 
		for (int featureIndex = 0; featureIndex < features1Pure.length; featureIndex++)
		{
			 Feature1Pure feature = features1Pure[featureIndex];
					//
			 feature1PureIndexForValidAnnTests[feature.getAnnotationTestVar()].add(featureIndex);
		}
		
		// for each edge feature, gets the annotations used in the annotation tests and accordingly updates the storage data 
		for (int featureIndex = 0; featureIndex < features2.length; featureIndex++)
		{
			Feature2 feature = features2[featureIndex];
			//
			feature2IndexForValidAnnTests.getQuick(feature.getAnnotationTestParentVar(),
					feature.getAnnotationTestChildVar()).add(featureIndex);
		}
		
		// for each triangle feature, gets the annotations used in the annotation tests and accordingly updates the storage data 
		for (int featureIndex = 0; featureIndex < features3.length; featureIndex++)
		{
			Feature3 feature = features3[featureIndex];
			//
			feature3IndexForValidAnnTests.getQuick(feature.getAnnotationTestParentVar(),
						feature.getAnnotationTestLeftChildVar(),
						feature.getAnnotationTestRightChildVar()).add(featureIndex);
		}
		
		// for each triangle observation feature, gets the annotations used in the annotation tests and accordingly updates the storage data 
		for (int featureIndex = 0; featureIndex < features3Observation.length; featureIndex++)
		{
			Feature3Observation feature = features3Observation[featureIndex];
					//
			feature3ObservationIndexForValidAnnTests.getQuick(feature.getAnnotationTestParentVar(),
								feature.getAnnotationTestLeftChildVar(),
								feature.getAnnotationTestRightChildVar()).add(featureIndex);
		}	
		
		// updates the size of the lists used to store the features
		for (int annotationIndex = 0; annotationIndex < annotations.getAnnotationsNumber(); annotationIndex++)
			feature1IndexForValidAnnTests[annotationIndex].trimToSize();
		
		for (int annotationIndex = 0; annotationIndex < annotations.getAnnotationsNumber(); annotationIndex++)
			feature1InverseIndexForValidAnnTests[annotationIndex].trimToSize();
		
		for (int annotationIndex = 0; annotationIndex < annotations.getAnnotationsNumber(); annotationIndex++)
			feature1PureIndexForValidAnnTests[annotationIndex].trimToSize();
		
		for (int parentAnnotationIndex = 0; parentAnnotationIndex < annotations
				.getAnnotationsNumber(); parentAnnotationIndex++)
			for (int childAnnotationIndex = 0; childAnnotationIndex < annotations
					.getAnnotationsNumber(); childAnnotationIndex++)
				feature2IndexForValidAnnTests.getQuick(parentAnnotationIndex, childAnnotationIndex)
						.trimToSize(); 
		
		for (int parentAnnotationIndex = 0; parentAnnotationIndex < annotations
				.getAnnotationsNumber(); parentAnnotationIndex++)
			for (int childAnnotationIndex = 0; childAnnotationIndex < annotations
					.getAnnotationsNumber(); childAnnotationIndex++)
				for (int siblingAnnotationIndex = 0; siblingAnnotationIndex < annotations
						.getAnnotationsNumber(); siblingAnnotationIndex++)
					feature3IndexForValidAnnTests.getQuick(parentAnnotationIndex,
							childAnnotationIndex, siblingAnnotationIndex).trimToSize();
		
		for (int parentAnnotationIndex = 0; parentAnnotationIndex < annotations
				.getAnnotationsNumber(); parentAnnotationIndex++)
			for (int childAnnotationIndex = 0; childAnnotationIndex < annotations
					.getAnnotationsNumber(); childAnnotationIndex++)
				for (int siblingAnnotationIndex = 0; siblingAnnotationIndex < annotations
						.getAnnotationsNumber(); siblingAnnotationIndex++)
					feature3ObservationIndexForValidAnnTests.getQuick(parentAnnotationIndex,
							childAnnotationIndex, siblingAnnotationIndex).trimToSize();
	}

	@Override
	public IntArrayList getFeaturesForValidAnnotationTest(int annotationIndex)
	{
		return feature1IndexForValidAnnTests[annotationIndex];
	}
	
	@Override
	public IntArrayList getFeaturesForValidAnnotationTestInverse(int annotationIndex)
	{
		return feature1InverseIndexForValidAnnTests[annotationIndex];
	}
	
	@Override
	public IntArrayList getFeaturesForValidAnnotationTestPure(int annotationIndex)
	{
		return feature1PureIndexForValidAnnTests[annotationIndex];
	}

	@Override
	public IntArrayList getFeaturesForValidAnnotationTest(int parentAnnotationIndex,
			int childAnnotationIndex)
	{
		return feature2IndexForValidAnnTests.getQuick(parentAnnotationIndex, childAnnotationIndex);
	}

	@Override
	public IntArrayList getFeaturesForValidAnnotationTest(int parentAnnotationIndex,
			int childAnnotationIndex, int siblingAnnotationIndex)
	{
		return feature3IndexForValidAnnTests.getQuick(parentAnnotationIndex, childAnnotationIndex,
				siblingAnnotationIndex);
	}
	
	@Override
	public IntArrayList getFeaturesForValidAnnotationTestObservation(int parentAnnotationIndex,
			int childAnnotationIndex, int siblingAnnotationIndex)
	{
		return feature3ObservationIndexForValidAnnTests.getQuick(parentAnnotationIndex, childAnnotationIndex,
				siblingAnnotationIndex);
	}

	@Override
	public Feature1Inverse[] getObservableFeatures1Inverse(int treeIndex)
	{
		return observableFeatures1Inverse[treeIndex];
	}
	
	@Override
	public Feature1Pure[] getObservableFeatures1Pure(int treeIndex)
	{
		return observableFeatures1Pure[treeIndex];
	}
	
	@Override
	public Feature1[] getObservableFeatures1(int treeIndex)
	{
		return observableFeatures1[treeIndex];
	}

	@Override
	public Feature2[] getObservableFeatures2(int treeIndex)
	{
		return observableFeatures2[treeIndex];
	}

	@Override
	public Feature3[] getObservableFeatures3(int treeIndex)
	{
		return observableFeatures3[treeIndex];
	}
	
	@Override
	public Feature3Observation[] getObservableFeatures3Observation(int treeIndex)
	{
		return observableFeatures3Observation[treeIndex];
	}

	@Override
	public void initOperationTestsEvaluation(int treesNumber)
	{
		observableFeatures1Inverse = new Feature1Inverse[treesNumber][];
		observableFeatures1Pure = new Feature1Pure[treesNumber][];
		observableFeatures1 = new Feature1[treesNumber][];
		observableFeatures2 = new Feature2[treesNumber][];
		observableFeatures3 = new Feature3[treesNumber][];
		observableFeatures3Observation = new Feature3Observation[treesNumber][];

		for (Feature1Inverse feature : features1Inverse) {
			feature.initObservableNodes(treesNumber);
		}
		for (Feature1Pure feature : features1Pure) {
			feature.initObservableNodes(treesNumber);
		}
		for (Feature1 feature : features1) {
			feature.initObservableNodes(treesNumber);
		}
		for (Feature2 feature : features2) {
			feature.initObservableNodesChild(treesNumber);
		}
		for (Feature3 feature : features3) {
			feature.initObservableNodesLeftChild(treesNumber);
		}
		for (Feature3Observation feature : features3Observation) {
			feature.initObservableNodesLeftChild(treesNumber);
		}
	}
	
	@Override
	public boolean isLogicTransformSuitable (Node nodetype, int annotationindex ) {
		
		if(this.getAnnotationText(annotationindex).indexOf("expLogic")==-1) {
			return true;
		}
		else {
			if(!nodetype.getLogicalExpressionIdentity().isEmpty())
				return true;
			else return false;
		}		
	}
}
