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
package mostrare.operations.parts.tree.withConst_globalIt;

import java.util.ArrayList;
import java.util.Map;

import cern.colt.list.IntArrayList;
import mostrare.crf.tree.CRF;
import mostrare.crf.tree.Feature1;
import mostrare.crf.tree.Feature1Inverse;
import mostrare.crf.tree.Feature1Pure;
import mostrare.crf.tree.Feature2;
import mostrare.crf.tree.Feature3;
import mostrare.crf.tree.Feature3Observation;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.matrix.miniImpl.EmptyFlexDenseDoubleMatrix2D;
import mostrare.matrix.miniImpl.EmptyFlexDenseDoubleMatrix3D;
import mostrare.matrix.miniImpl.EmptyFlexDenseDoubleMatrix4D;
import mostrare.matrix.miniImpl.FlexDenseDoubleMatrix2D;
import mostrare.matrix.miniImpl.FlexDenseDoubleMatrix3D;
import mostrare.matrix.miniImpl.FlexDenseDoubleMatrix4D;
import mostrare.operations.parts.tree.FlexMParts;
import mostrare.tree.Node;
import mostrare.tree.Tree;
import mostrare.tree.impl.NodeAST;

/**
 * @author missi
 */
public class FlexMPartsImpl implements FlexMParts
{
	private CRF						crf;

	/**
	 * iterator over annotations
	 */

	private Tree					tree;

	private FlexDenseDoubleMatrix2D	logM1;

	private FlexDenseDoubleMatrix3D	logM2;

	private FlexDenseDoubleMatrix4D	logM3;

	private int						annotationsNumber;

	private int						nodesNumber;
	
	private Map<String, ArrayList<Integer>>  nodetransformmapinteger;


	public FlexMPartsImpl(CRFWithConstraintNode crf)
	{
		this.crf = crf;
		this.tree = null;
		annotationsNumber = crf.getAnnotationsNumber();
		// if there is no node feature, no need to create the matrix at the required size
		if (crf.getFeatures1Number() + crf.getFeatures1PureNumber() +crf.getFeatures1InverseNumber() > 0)
			logM1 = new FlexDenseDoubleMatrix2D(0, 0);
		else
			logM1 = new EmptyFlexDenseDoubleMatrix2D(0, 0);
		// if there is no edge feature, no need to create the matrix at the required size
		if (crf.getFeatures2Number() > 0)
			logM2 = new FlexDenseDoubleMatrix3D(0, 0);
		else
			logM2 = new EmptyFlexDenseDoubleMatrix3D(0, 0);
		// more memory expensive than a SparseDoubleMatrix4D but faster
		// if there is no triangle feature, no need to create the matrix at the required size
		if (crf.getFeatures3Number() + crf.getFeatures3ObservationNumber() > 0)
			logM3 = new FlexDenseDoubleMatrix4D(0, 0);
		else
			logM3 = new EmptyFlexDenseDoubleMatrix4D(0, 0);
		
		this.nodetransformmapinteger = crf.nodetransformmapinteger;

	}

	@Override
	public double getLogM1Quick(int nodeIndex, int annotationIndex)
	{
		return logM1.getQuick(nodeIndex, annotationIndex);
	}

	@Override
	public double calcLogM1(Node node, int annotationIndex)
	{
		double res = 0.0;
		// gets the node features whose annotation test is satisfied by the annotation 
		IntArrayList featuresIndex = crf.getFeaturesForValidAnnotationTest(annotationIndex);
		for (int index : featuresIndex.elements())
		{
			Feature1 feature = crf.getFeatures1()[index];
			// checks if the node satisfies the observation tests of the feature
			if (feature.isNodeObservable(node) && feature.getCharacterTestValue(node))
				// log M1(n,Yn) = sum/k w_k * f_k(n,Yn)
				res += crf.getWeight1(feature.getIndex()) *
						1.0;
		}
		
		IntArrayList featuresIndexPure = crf.getFeaturesForValidAnnotationTestPure(annotationIndex);
		for (int index : featuresIndexPure.elements())
		{
			Feature1Pure feature = crf.getFeatures1Pure()[index];
			// checks if the node satisfies the observation tests of the feature
			if (feature.isNodeObservable(node))
				// log M1(n,Yn) = sum/k w_k * f_k(n,Yn)
				res += crf.getWeight1Pure(feature.getIndex()) *
						1.0;
		}

		IntArrayList featuresIndexInverse = crf.getFeaturesForValidAnnotationTestInverse(annotationIndex);
		for (int index : featuresIndexInverse.elements())
		{
			Feature1Inverse feature = crf.getFeatures1Inverse()[index];
			// checks if the node satisfies the observation tests of the feature
			if (feature.isNodeObservable(node) && feature.getCharacterTestValue(node))
				// log M1(n,Yn) = sum/k w_k * f_k(n,Yn)
				res += crf.getWeight1Inverse(feature.getIndex()) *
						1.0;
		}
		return res;

	}

	@Override
	public double getLogM1(int nodeIndex, int annotationIndex)
	{
		double res;
		res = getLogM1Quick(nodeIndex, annotationIndex);
		if (res != 0.0)
			return res;
		return calcLogM1(tree.getNode(nodeIndex), annotationIndex);
	}

	@Override
	public double getLogM2Quick(int childIndex, int parentAnnotationIndex, int childAnnotationIndex)
	{
		return logM2.getQuick(childIndex, parentAnnotationIndex, childAnnotationIndex);
	}

	@Override
	public double calcLogM2(Node child, int parentAnnotationIndex, int childAnnotationIndex)
	{
		double res = 0.0;
		// gets the edge features whose annotation test is satisfied by the couple of annotations 
		IntArrayList featuresIndex = crf.getFeaturesForValidAnnotationTest(parentAnnotationIndex,
				childAnnotationIndex);
		for (int index : featuresIndex.elements())
		{
			Feature2 feature = crf.getFeatures2()[index];
			// checks if the node satisfies the observation tests of the feature
			if (feature.isNodeObservableChild(child))
				// log M2(n_i,Yn,Yn_i) = sum/k w_k * f_k(n_i,Yn,Yn_i)
				res += crf.getWeight2(feature.getIndex()) *
						1.0;
		}

		return res;
	}

	@Override
	public double getLogM2(int childIndex, int parentAnnotationIndex, int childAnnotationIndex)
	{
		double res;
		res = getLogM2Quick(childIndex, parentAnnotationIndex, childAnnotationIndex);
		if (res != 0.0)
			return res;
		return calcLogM2(tree.getNode(childIndex), parentAnnotationIndex, childAnnotationIndex);
	}

	@Override
	public double getLogM3Quick(int childIndex, int parentAnnotationIndex,
			int childAnnotationIndex, int siblingAnnotationIndex)
	{
		return logM3.getQuick(childIndex, parentAnnotationIndex, childAnnotationIndex,
				siblingAnnotationIndex);
	}

	@Override
	public double calcLogM3(Node child, int parentAnnotationIndex, int childAnnotationIndex,
			int siblingAnnotationIndex)
	{
		double res = 0.0;
		// gets the triangle features whose annotation test is satisfied by the triplet of annotations 
		IntArrayList featuresIndex = crf.getFeaturesForValidAnnotationTest(parentAnnotationIndex,
				childAnnotationIndex, siblingAnnotationIndex);
		for (int index : featuresIndex.elements())
		{
			Feature3 feature = crf.getFeatures3()[index];
			// checks if the node satisfies the observation tests of the feature
			if (feature.isNodeObservableLeftChild(child)) {
				// log M3(n_i,Yn,Yn_i,Yn_j) = sum/k w_k * f_k(n_i,Yn,Yn_i,Yn_j)
				res += crf.getWeight3(feature.getIndex()) *
						1.0;
			}
		}
		
		IntArrayList featuresIndexObservation = crf.getFeaturesForValidAnnotationTestObservation(parentAnnotationIndex,
				childAnnotationIndex, siblingAnnotationIndex);
		for (int index : featuresIndexObservation.elements())
		{
			Feature3Observation feature = crf.getFeatures3Observation()[index];
			// checks if the node satisfies the observation tests of the feature
			if (feature.isNodeObservableLeftChild(child)) {
				// log M3(n_i,Yn,Yn_i,Yn_j) = sum/k w_k * f_k(n_i,Yn,Yn_i,Yn_j)
				if(child.getNextSibling()!=null && feature.whetherNodeHasSameContent(child, child.getNextSibling()))
				   res += crf.getWeight3Observation(feature.getIndex()) *
						1.0;
			}
		}
		
		return res;
	}

	@Override
	public double getLogM3(int childIndex, int parentAnnotationIndex, int childAnnotationIndex,
			int siblingAnnotationIndex)
	{
		double res;
		res = getLogM3Quick(childIndex, parentAnnotationIndex, childAnnotationIndex,
				siblingAnnotationIndex);
		if (res != 0.0)
			return res;
		return calcLogM3(tree.getNode(childIndex), parentAnnotationIndex, childAnnotationIndex,
				siblingAnnotationIndex);
	}

	@Override
	public void fillLogM1()
	{
		// log M1(n,Yn) = sum/k w_k * f_k(n,Yn)
		int nodeIndex;
		double featureValue;
		int treeIndex = tree.getIndex();
		// gets the features whose observation tests are satisfied by a node of the tree at least once
		Feature1[] features = crf.getObservableFeatures1(treeIndex);
		Node[] nodes;
				
		for (Feature1 feature : features)
		{
			// gets the annotation specified in the annotation test of the feature
			int annotation = feature.getAnnotationTestVar();
			double weight = crf.getWeight1(feature.getIndex());
			// gets the nodes that satisfy the observation tests of the feature
			nodes = feature.getObservableNodes(treeIndex);
			
			for (Node node : nodes)
			{
//				if (!labelSet.getParentAnnotations().contains(annotation) ||
//						!node.isAnnotationAllowedForNode(annotation))
				if(!nodetransformmapinteger.get(node.getNodeType()).contains(annotation))
					continue;
				
				if(!feature.getCharacterTestValue(node))
					continue;
				
				if(!crf.isLogicTransformSuitable(node, annotation)) {
					continue;
				}
				
				featureValue = 1.0;
				nodeIndex = node.getIndex();
				
				// log M1(n,Yn) = log M1(n,Yn) + w_k * f_k(n,Yn)
				
				logM1.setQuick(nodeIndex, annotation, (featureValue == 1.0) ? logM1.getQuick(
						nodeIndex, annotation) +
						weight : logM1.getQuick(nodeIndex, annotation) + weight * featureValue);
			}
		}
		
		int nodeIndexPure;
		double featureValuePure;
		int treeIndexPure = tree.getIndex();
		// gets the features whose observation tests are satisfied by a node of the tree at least once
		Feature1Pure[] featuresPure = crf.getObservableFeatures1Pure(treeIndexPure);
		Node[] nodesPure;
		for (Feature1Pure feature : featuresPure)
		{
			// gets the annotation specified in the annotation test of the feature
			int annotation = feature.getAnnotationTestVar();
			double weight = crf.getWeight1Pure(feature.getIndex());
			// gets the nodes that satisfy the observation tests of the feature
			nodesPure = feature.getObservableNodes(treeIndexPure);
			for (Node node : nodesPure)
			{
//				if (!labelSet.getParentAnnotations().contains(annotation) ||
//						!node.isAnnotationAllowedForNode(annotation))
				if(!nodetransformmapinteger.get(node.getNodeType()).contains(annotation))
					continue;
				
				if(!crf.isLogicTransformSuitable(node, annotation)) {
					continue;
				}
				
				featureValuePure = 1.0;
				nodeIndexPure = node.getIndex();
				// log M1(n,Yn) = log M1(n,Yn) + w_k * f_k(n,Yn) 
				logM1.setQuick(nodeIndexPure, annotation, (featureValuePure == 1.0) ? logM1.getQuick(
						nodeIndexPure, annotation) +
						weight : logM1.getQuick(nodeIndexPure, annotation) + weight * featureValuePure);
			}
		}
		
		int nodeIndexInverse;
		double featureValueInverse;
		int treeIndexInverse = tree.getIndex();
		// gets the features whose observation tests are satisfied by a node of the tree at least once
		Feature1Inverse[] featuresInverse = crf.getObservableFeatures1Inverse(treeIndexInverse);
		Node[] nodesInverse;
		for (Feature1Inverse feature : featuresInverse)
		{
			// gets the annotation specified in the annotation test of the feature
			int annotation = feature.getAnnotationTestVar();
			double weight = crf.getWeight1Inverse(feature.getIndex());
			// gets the nodes that satisfy the observation tests of the feature
			nodesInverse = feature.getObservableNodes(treeIndexInverse);
			for (Node node : nodesInverse)
			{
//				if (!labelSet.getParentAnnotations().contains(annotation) ||
//						!node.isAnnotationAllowedForNode(annotation))
				if(!nodetransformmapinteger.get(node.getNodeType()).contains(annotation))
					continue;
				
				if(!feature.getCharacterTestValue(node))
					continue;
				
				if(!crf.isLogicTransformSuitable(node, annotation)) {
					continue;
				}
				
				featureValueInverse = 1.0;
				nodeIndexInverse = node.getIndex();
				// log M1(n,Yn) = log M1(n,Yn) + w_k * f_k(n,Yn) 
				logM1.setQuick(nodeIndexInverse, annotation, (featureValueInverse == 1.0) ? logM1.getQuick(
						nodeIndexInverse, annotation) +
						weight : logM1.getQuick(nodeIndexInverse, annotation) + weight * featureValueInverse);
			}
		}
	}

	@Override
	public void fillLogM2()
	{
		// log M2(n_i,Yn,Yn_i) = sum/k w_k * f_k(n_i,Yn,Yn_i)
		double featureValue;
		int nodeIndex;
		int treeIndex = tree.getIndex();
		// gets the features whose observation tests are satisfied by a node of the tree at least once
		Feature2[] features = crf.getObservableFeatures2(treeIndex);
		Node[] nodes;
		for (Feature2 feature : features)
		{
			// gets the couple of annotations specified in the annotation test of the feature
			int parentAnnotation = feature.getAnnotationTestParentVar();
			int childAnnotation = feature.getAnnotationTestChildVar();
			double weight = crf.getWeight2(feature.getIndex());
			// gets the nodes that satisfy the observation tests of the feature
			nodes = feature.getObservableNodesChild(treeIndex);
			for (Node node : nodes)
			{
//				if (node.getParentNode() != null &&
//						(!labelSet.getParentAnnotations().contains(parentAnnotation) ||
//								!node.getParentNode()
//										.isAnnotationAllowedForNode(parentAnnotation) ||
//								!labelSet.getChildAnnotations(parentAnnotation).contains(
//										childAnnotation) || !node
//								.isAnnotationAllowedForEdgeChild(parentAnnotation, childAnnotation)))
				
				    if(node.getParentNode()==null)
				    	continue;
				    
					if (node.getParentNode() != null &&
					(!nodetransformmapinteger.get(node.getParentNode().getNodeType()).contains(parentAnnotation) ||
							!nodetransformmapinteger.get(node.getNodeType()).contains(childAnnotation)))
					   continue;
					
					if(!crf.isLogicTransformSuitable(node.getParentNode(), parentAnnotation) || 
							!crf.isLogicTransformSuitable(node, childAnnotation)) {
						continue;
					}
					
				nodeIndex = node.getIndex();
				featureValue = 1.0;
				// log M2(n_i,Yn,Yn_i) = log M2(n_i,Yn,Yn_i) + w_k * f_k(n_i,Yn,Yn_i) 
				logM2.setQuick(nodeIndex, parentAnnotation, childAnnotation,
						(featureValue == 1.0) ? logM2.getQuick(nodeIndex, parentAnnotation,
								childAnnotation) +
								weight : logM2.getQuick(nodeIndex, parentAnnotation,
								childAnnotation) +
								weight * featureValue);
			}
		}

	}

	@Override
	public void fillLogM3()
	{
		// log M3(n_i,Yn,Yn_i,Yn_j) = sum/k w_k * f_k(n_i,Yn,Yn_i,Yn_j)
		int nodeIndex;
		int treeIndex = tree.getIndex();
		double featureValue;
		// gets the features whose observation tests are satisfied by a node of the tree at least once
		Feature3[] features = crf.getObservableFeatures3(treeIndex);
		Node[] nodes;

		Node parent, sibling;

		for (Feature3 feature : features)
		{
			// gets the triplets specified in the annotation test of the feature
			int parentAnnotations = feature.getAnnotationTestParentVar();
			int leftChildAnnotation = feature.getAnnotationTestLeftChildVar();
			int rightChildAnnotation = feature.getAnnotationTestRightChildVar();
			double weight = crf.getWeight3(feature.getIndex());
			// gets the nodes that satisfy the observation tests of the feature
			nodes = feature.getObservableNodesLeftChild(treeIndex);
			for (Node node : nodes)
			{
				sibling = ((NodeAST) node).getNextSibling();
				if (sibling == null)
					continue;
				
				parent = node.getParentNode();
				if (parent == null)
					continue;
				
				nodeIndex = node.getIndex();
//				for (int parentAnnotation : parentAnnotations)
//				{
//					if (!labelSet.getParentAnnotations().contains(parentAnnotations) ||
//							!parent.isAnnotationAllowedForNode(
//									parentAnnotations) ||
//							!labelSet.getChildAnnotations(parentAnnotations).contains(
//									leftChildAnnotation) ||
//							!node.isAnnotationAllowedForEdgeChild(
//									parentAnnotations, leftChildAnnotation) ||
//							!labelSet.getSiblingAnnotations(parentAnnotations, leftChildAnnotation)
//									.contains(rightChildAnnotation) ||
//							!sibling.isAnnotationAllowedForEdgeChild(
//									parentAnnotations, rightChildAnnotation) ||
//							!node.isTriangleAnnotable(parentAnnotations,
//									leftChildAnnotation, rightChildAnnotation))
				if (!nodetransformmapinteger.get(parent.getNodeType()).contains(parentAnnotations)
				   ||!nodetransformmapinteger.get(node.getNodeType()).contains(leftChildAnnotation)
				   ||!nodetransformmapinteger.get(sibling.getNodeType()).contains(rightChildAnnotation))
						continue;
				
				 if(!crf.isLogicTransformSuitable(parent, parentAnnotations) || 
							!crf.isLogicTransformSuitable(node, leftChildAnnotation)||
							!crf.isLogicTransformSuitable(sibling, rightChildAnnotation)) {
					   continue;
				  }

					featureValue = 1.0;
					// log M3(n_i,Yn,Yn_i,Yn_j) = log M3(n_i,Yn,Yn_i,Yn_j) + w_k * f_k(n_i,Yn,Yn_i,Yn_j)
					logM3.setQuick(nodeIndex, parentAnnotations, leftChildAnnotation,
							rightChildAnnotation, (featureValue == 1.0) ? logM3.getQuick(nodeIndex,
									parentAnnotations, leftChildAnnotation, rightChildAnnotation) +
									weight : logM3.getQuick(nodeIndex, parentAnnotations,
									leftChildAnnotation, rightChildAnnotation) +
									weight * featureValue);
				//}
			}
		}
		
		
		int nodeIndexObservation;
		int treeIndexObservation = tree.getIndex();
		double featureValueObservation;
		// gets the features whose observation tests are satisfied by a node of the tree at least once
		Feature3Observation[] featuresObservation = crf.getObservableFeatures3Observation(treeIndexObservation);
		Node[] nodesObservation;
		Node parentObservation, siblingObservation;

		for (Feature3Observation feature : featuresObservation)
		{
			// gets the triplets specified in the annotation test of the feature
			int parentAnnotations = feature.getAnnotationTestParentVar();
			int leftChildAnnotation = feature.getAnnotationTestLeftChildVar();
			int rightChildAnnotation = feature.getAnnotationTestRightChildVar();
			double weight = crf.getWeight3Observation(feature.getIndex());
			// gets the nodes that satisfy the observation tests of the feature
			nodesObservation = feature.getObservableNodesLeftChild(treeIndexObservation);
			for (Node node : nodesObservation)
			{
				siblingObservation = ((NodeAST) node).getNextSibling();
				if (siblingObservation == null)
					continue;
				
				parentObservation = node.getParentNode();
				if (parentObservation == null)
					continue;
				
				nodeIndexObservation = node.getIndex();
//				for (int parentAnnotation : parentAnnotations)
//				{
//					if (!labelSet.getParentAnnotations().contains(parentAnnotations) ||
//							!parent.isAnnotationAllowedForNode(
//									parentAnnotations) ||
//							!labelSet.getChildAnnotations(parentAnnotations).contains(
//									leftChildAnnotation) ||
//							!node.isAnnotationAllowedForEdgeChild(
//									parentAnnotations, leftChildAnnotation) ||
//							!labelSet.getSiblingAnnotations(parentAnnotations, leftChildAnnotation)
//									.contains(rightChildAnnotation) ||
//							!sibling.isAnnotationAllowedForEdgeChild(
//									parentAnnotations, rightChildAnnotation) ||
//							!node.isTriangleAnnotable(parentAnnotations,
//									leftChildAnnotation, rightChildAnnotation))
				if (!nodetransformmapinteger.get(parentObservation.getNodeType()).contains(parentAnnotations)
				   ||!nodetransformmapinteger.get(node.getNodeType()).contains(leftChildAnnotation)
				   ||!nodetransformmapinteger.get(siblingObservation.getNodeType()).contains(rightChildAnnotation))
						continue;
				
				 if(!crf.isLogicTransformSuitable(parentObservation, parentAnnotations) || 
							!crf.isLogicTransformSuitable(node, leftChildAnnotation)||
							!crf.isLogicTransformSuitable(siblingObservation, rightChildAnnotation)) {
					   continue;
				  }
				 
				 if(!feature.whetherNodeHasSameContent(node, siblingObservation))
					 continue;

				    featureValueObservation = 1.0;
					// log M3(n_i,Yn,Yn_i,Yn_j) = log M3(n_i,Yn,Yn_i,Yn_j) + w_k * f_k(n_i,Yn,Yn_i,Yn_j)
					logM3.setQuick(nodeIndexObservation, parentAnnotations, leftChildAnnotation,
							rightChildAnnotation, (featureValueObservation == 1.0) ? logM3.getQuick(nodeIndexObservation,
									parentAnnotations, leftChildAnnotation, rightChildAnnotation) +
									weight : logM3.getQuick(nodeIndexObservation, parentAnnotations,
									leftChildAnnotation, rightChildAnnotation) +
									weight * featureValueObservation);
				//}
			}
		}
	}

	@Override
	public void fill()
	{
		if (crf.getFeatures1Number() + crf.getFeatures1PureNumber() + crf.getFeatures1InverseNumber() > 0)
			fillLogM1();
		if (crf.getFeatures2Number() > 0)
			fillLogM2();
		if (crf.getFeatures3Number() + crf.getFeatures3ObservationNumber() > 0)
			fillLogM3();
	}

	@Override
	public void setNewTree(Tree tree, boolean init)
	{
		int nodesNumber = tree.getNodesNumber();
		
		if (init)
		{
			logM1.setNewSizeOrInit(nodesNumber, annotationsNumber);
			logM2.setNewSize(nodesNumber, annotationsNumber);
			logM3.setNewSize(nodesNumber, annotationsNumber);
		}
		
		this.nodesNumber = nodesNumber;
		this.tree = tree;
	}

}
