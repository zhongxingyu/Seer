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

import java.io.Serializable;

import mostrare.tree.Node;
import mostrare.tree.Tree;
import cern.colt.list.IntArrayList;

/**
 * @author missi
 */
public interface CRF extends Serializable
{
	public CharacterEnum getCharactersEnum();
	
	public AnnotationsEnum getAnnotationsEnum();
	
	public IntArrayList getAnnotationArray();

	/**
	 * Returns the string relative to the annotation associated with the provided <code>index</code>.
	 * 
	 * @param index
	 *            the index of an annotation
	 * @return the string version of the annotation
	 */
	public String getAnnotationText(int index);

	/**
	 * Returns the index relative to the annotation associated with the provided
	 * <code>stringValue</code>.
	 * 
	 * @param stringValue
	 *            string value of the annotation
	 * @return the index relative to the annotation.
	 */
	public int getAnnotationIndex(String stringValue);
		
	public double getFeature1PureValue(int featureIndex, Node node, int annotationIndex);

	public double getFeature1InverseValue(int featureIndex, Node node, int annotationIndex);
	/**
	 * Returns the value of the <code>featureIndex</code>-th feature applied to the node and the
	 * annotation.
	 * 
	 * @param featureIndex
	 *            index of the feature
	 * @param node
	 *            node
	 * @param annotationIndex index of the annotation of the node of the clique
	 * @return the value of the feature
	 */
	public double getFeature1Value(int featureIndex, Node node, int annotationIndex);

	/**
	 * Returns the value of the <code>featureIndex</code>-th feature applied to the two-nodes
	 * clique. The <code>node</code> is the child node of the edge.
	 * 
	 * @param featureIndex
	 *            index of the feature
	 * @param node
	 *            node
	 * @param parentAnnotationIndex
	 *            index of the annotation of the parent node of the clique
	 * @param childAnnotationIndex
	 *            index of the annotation of the parent node of the clique
	 * @return the value of the feature
	 */
	public double getFeature2Value(int featureIndex, Node childnode, 
			int parentAnnotationIndex, int childAnnotationIndex);
	/**
	 * Returns the value of the <code>featureIndex</code>-th feature applied to the three-nodes
	 * clique. The <code>node</code> is the left child node of the triangle clique.
	 * 
	 * @param featureIndex
	 *            index of the feature
	 * @param node
	 *            node
	 * @param parentAnnotationIndex
	 *            index of the annotation of the parent node of the clique
	 * @param childAnnotationIndex
	 *            index of the annotation of the left child node of the clique
	 * @param siblingAnnotationIndex
	 *            index of the annotation of the right child node of the clique
	 * @return the value of the feature
	 */
	
	public double getFeature3Value(int featureIndex, Node leftchild,
			int parentAnnotationIndex, int childAnnotationIndex,
			int siblingAnnotationIndex);
	
	public double getFeature3ObservationValue(int featureIndex, Node leftchild,
			int parentAnnotationIndex, int childAnnotationIndex,
			int siblingAnnotationIndex);
	
	public double getWeight1Pure(int index);

	public double getWeight1Inverse(int index);

	/**
	 * Returns the weight associated with the <code>index</code>-th one-node feature.
	 * 
	 * @param index
	 *            index of the one-node feature
	 * @return the weight associated with an one-node feature.
	 */
	public double getWeight1(int index);

	/**
	 * Returns the weight associated with the <code>index</code>-th two-nodes feature.
	 * 
	 * @param index
	 *            index of the two-nodes feature
	 * @return the weight associated with a two-nodes feature.
	 */
	public double getWeight2(int index);

	/**
	 * Returns the weight associated with the <code>index</code>-th three-nodes feature.
	 * 
	 * @param index
	 *            index of the three-nodes feature
	 * @return the weight associated with a three-nodes feature.
	 */
	public double getWeight3(int index);

	public double getWeight3Observation(int index);
	
	public void setWeight1Pure(int index, double value);

	public void setWeight1Inverse(int index, double value);

	/**
	 * Sets the weight of the <code>index</code>-th one-node feature at <code>value</code>.
	 * 
	 * @param index
	 *            index of the one-node feature
	 * @param value
	 *            weight value
	 */
	public void setWeight1(int index, double value);

	/**
	 * Sets the weight of the <code>index</code>-th two-nodes feature at <code>value</code>.
	 * 
	 * @param index
	 *            index of the two-nodes feature
	 * @param value
	 *            weight value
	 */
	public void setWeight2(int index, double value);

	/**
	 * Sets the weight of the <code>index</code>-th three-nodes feature at <code>value</code>.
	 * 
	 * @param index
	 *            index of the three-nodes feature
	 * @param value
	 *            weight value
	 */
	public void setWeight3(int index, double value);
	
	public void setWeight3Observation(int index, double value);

	public int getFeatures1PureNumber();

	public int getFeatures1InverseNumber();

	/**
	 * Returns the number of one-node features.
	 * 
	 * @return the number of one-node features.
	 */
	public int getFeatures1Number();

	/**
	 * Returns the number of two-nodes features.
	 * 
	 * @return the number of two-nodes features.
	 */
	public int getFeatures2Number();

	/**
	 * Returns the number of three-nodes features.
	 * 
	 * @return the number of three-nodes features.
	 */
	public int getFeatures3Number();

	public int getFeatures3ObservationNumber();
	/**
	 * Returns the number of annotation labels.
	 * 
	 * @return the number of annotation labels.
	 */
	public int getAnnotationsNumber();

	/**
	 * Precalculates "observation tests" on <code>tree</code>.
	 * 
	 * @param tree
	 */
	public void applyObservationTests(Tree tree);

	/**
	 * Returns <code>true</code> if the "observation tests" should be applied before getting any
	 * feature value.
	 * 
	 * @return <code>true</code> if the "observation tests" should be applied before getting any
	 *         feature value.
	 */
	public boolean observationTestsShouldBeApplied();
	
	/**
	 * Returns the index for the "empty" annotation label. The index is a positive number.
	 * It returns a negative number is no empty annotation is defined.
	 * @return the index for the "empty" annotation label.
	 */
	public int getNoAnnotationIndex();
	
	public Feature1Pure[] getFeatures1Pure();
	
	public Feature1Inverse[] getFeatures1Inverse();

	public Feature1[] getFeatures1();
	
	public Feature2[] getFeatures2();
	
	public Feature3[] getFeatures3();
	
	public Feature3Observation[] getFeatures3Observation();
	
	public IntArrayList getFeaturesForValidAnnotationTestPure(int annotationIndex);
	
	public IntArrayList getFeaturesForValidAnnotationTestInverse(int annotationIndex);

	/**
	 * Returns the list of index of features whose annotation test is valid against the annotation
	 * <code>annotationIndex<code>.
	 * @param annotationIndex
	 * @return a list of feature index
	 */
	public IntArrayList getFeaturesForValidAnnotationTest(int annotationIndex);

	/**
	 * Returns the list of index of features whose annotation test is valid against the annotation
	 * pair (<code>parentAnnotationIndex<code>,<code>childAnnotationIndex</code>).
	 * @param parentAnnotationIndex
	 * @param childAnnotationIndex
	 * @return a list of feature index
	 */
	public IntArrayList getFeaturesForValidAnnotationTest(int parentAnnotationIndex,
			int childAnnotationIndex);

	/**
	 * Returns the list of index of features whose annotation test is valid against the annotation
	 * triplet (<code>parentAnnotationIndex<code>,<code>childAnnotationIndex</code>,<code>siblingAnnotationIndex</code>).
	 * @param parentAnnotationIndex
	 * @param childAnnotationIndex
	 * @param siblingAnnotationIndex
	 * @return a list of feature index
	 */
	public IntArrayList getFeaturesForValidAnnotationTest(int parentAnnotationIndex,
			int childAnnotationIndex, int siblingAnnotationIndex);
	
	public IntArrayList getFeaturesForValidAnnotationTestObservation(int parentAnnotationIndex,
			int childAnnotationIndex, int siblingAnnotationIndex);
	
	public Feature1Inverse[] getObservableFeatures1Inverse(int treeIndex);
	
	public Feature1Pure[] getObservableFeatures1Pure(int treeIndex);
	
	public Feature1[] getObservableFeatures1(int treeIndex);

	public Feature2[] getObservableFeatures2(int treeIndex);

	public Feature3[] getObservableFeatures3(int treeIndex);
	
	public Feature3Observation[] getObservableFeatures3Observation(int treeIndex);

	public void initOperationTestsEvaluation(int treesNumber);
	
	public String getCharacterText(int index);

	public int getCharacterIndex(String character);

	public int getCharactersNumber();
	
	public boolean isLogicTransformSuitable (Node nodetype, int annotationindex);
	
}
