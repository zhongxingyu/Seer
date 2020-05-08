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

import mostrare.crf.tree.CRF;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.matrix.miniImpl.DenseDoubleMatrix3D;
import mostrare.matrix.miniImpl.DenseIntIntMatrix2D;
import mostrare.matrix.miniImpl.FlexDenseDoubleMatrix2D;
import mostrare.matrix.miniImpl.FlexDenseDoubleMatrix3D;
import mostrare.matrix.miniImpl.IntIntMatrix2D;
import mostrare.operations.TrickUtil;
import mostrare.operations.parts.tree.FlexBetaParts;
import mostrare.operations.parts.tree.MParts;
import mostrare.tree.Node;
import mostrare.tree.Tree;
import mostrare.tree.impl.NodeAST;

import java.util.ArrayList;
import java.util.Map;

import cern.colt.list.IntArrayList;

/**
 * @author missi
 */
public class FlexBetaPartsImpl implements FlexBetaParts
{
	private CRF						crf;

	private Tree					tree;

	private FlexDenseDoubleMatrix2D	logBeta;

	private FlexDenseDoubleMatrix3D	logBetaPrime;

	private FlexDenseDoubleMatrix3D	logBetaSecond;

	private int						annotationsNumber;

	/**
	 * where M1, M2 and M3 values are calculated and stored
	 */
	private MParts					mparts;

	// prevents from creating a new array before calling logSumExp
	private double[]				values;

	/**
	 * used to hold temporary computation values before log-sum-exp
	 */
	private DenseDoubleMatrix3D		valuesMat;

	private IntIntMatrix2D			valuesMatCpt;

	/**
	 * Constructs the matrix that will hold beta and betaPrime values.
	 * 
	 * @param crf
	 * @param mparts
	 */
	
	private Map<String, ArrayList<Integer>>  nodetransformmapinteger;

	public FlexBetaPartsImpl(CRFWithConstraintNode crf, MParts mparts)
	{
		this.crf = crf;
		this.tree = null;
		this.mparts = mparts;
		annotationsNumber = crf.getAnnotationsNumber();
		logBeta = new FlexDenseDoubleMatrix2D(0, 0);
		logBetaPrime = new FlexDenseDoubleMatrix3D(0, 0);
		logBetaSecond = new FlexDenseDoubleMatrix3D(0, 0);

		values = new double[crf.getAnnotationsNumber()];
		valuesMat = new DenseDoubleMatrix3D(annotationsNumber, annotationsNumber);
		valuesMatCpt = new DenseIntIntMatrix2D(annotationsNumber, annotationsNumber);
		
		this.nodetransformmapinteger = crf.nodetransformmapinteger;
	}

	@Override
	public void fillLogBetas()
	{
		fillLogBeta(tree.getRoot());
	}

	private void fillLogBeta(Node node)
	{
		if (node.getOrderedNodesNumber() > 0)
		{
			NodeAST firstChild = (NodeAST) node.getOrderedNodeAt(0);
			NodeAST lastChild = (NodeAST) node.getOrderedNodeAt(node
					.getOrderedNodesNumber() - 1);
			// first, calculates log beta values for node children
			int childrenNumber = node.getOrderedNodesNumber();
			for (int childPos = 0; childPos < childrenNumber; childPos += 1)
				fillLogBeta(node.getOrderedNodeAt(childPos));
			// then calculates log betaPrime values for node children
			//
			fillLogBetaPrime_ordered(lastChild, -1);
			fillLogBetaSecond_ordered(firstChild);
			
			// at last, calculates log beta[y] for each y (annotation)
			fillLogBetaFromBetaPrime(node, firstChild.getIndex());
		}
		if (node.getOrderedNodesNumber() == 0)
		{
				// case of a node without children
				// calculates log beta[y] for each y (annotation)
			//	IntArrayList annotations = labelSet.getParentAnnotations();
				IntArrayList annotations = crf.getAnnotationArray();

				int annotationsSize = annotations.size();
				for (int annotationIndex = 0; annotationIndex < annotationsSize; annotationIndex += 1)
				{
					int annotation = annotations.getQuick(annotationIndex);
					// constraint test
					//if (!node.isAnnotationAllowedForNode(annotation))
						if(!nodetransformmapinteger.get(node.getNodeType()).contains(annotation))
						   continue;
						
						if(!crf.isLogicTransformSuitable(node, annotation)) {
							continue;
						}
						
					// log beta(n,Yn) = log M1(n,Yn)
					logBeta.setQuick(node.getIndex(), annotation, mparts.getLogM1Quick(node
							.getIndex(), annotation));
				}
		}
	}

	private void fillLogBetaFromBetaPrime(Node node, int childIndex)
	{
		// childIndex is the index of the first child of the provided node (n0)
		// this child may be either an element node or a text node or an attribute node
		//IntArrayList parentAnnotationsList = labelSet.getParentAnnotations(), childAnnotationsList;
		IntArrayList parentAnnotationsList = crf.getAnnotationArray(), childAnnotationsList;

		int nodeIndex = node.getIndex();
		Node child = tree.getNode(childIndex);
//		ConstraintsTrack parentConstraintsTrack = node.getConstraintsTrack(), childConstraintsTrack = child
//				.getConstraintsTrack();
		int parentAnnotationsSize = parentAnnotationsList.size();
		for (int parentAnnotationIndex = 0; parentAnnotationIndex < parentAnnotationsSize; parentAnnotationIndex += 1)
		{
			int parentAnnotation = parentAnnotationsList.getQuick(parentAnnotationIndex);
			// constraint test
			// if (!node.isAnnotationAllowedForNode(parentAnnotation))
				if(!nodetransformmapinteger.get(node.getNodeType()).contains(parentAnnotation))
				   continue;
				
				if(!crf.isLogicTransformSuitable(node, parentAnnotation)) {
					continue;
				}
			//
			//childAnnotationsList = labelSet.getChildAnnotations(parentAnnotation);
				childAnnotationsList = crf.getAnnotationArray();
			int childAnnotationsSize = childAnnotationsList.size();
			int childAnnNumber = 0;
			for (int childAnnotationIndex = 0; childAnnotationIndex < childAnnotationsSize; childAnnotationIndex += 1)
			{
				int childAnnotation = childAnnotationsList.getQuick(childAnnotationIndex);
				// constraint test
//				if (!child.isAnnotationAllowedForEdgeChild(parentAnnotation,
//						childAnnotation))
					if(!nodetransformmapinteger.get(child.getNodeType()).contains(childAnnotation))
					   continue;
					
					if(!crf.isLogicTransformSuitable(child, childAnnotation)) {
						continue;
					}
				//
				values[childAnnNumber] = logBetaPrime.getQuick(childIndex, parentAnnotation,
						childAnnotation);
				childAnnNumber += 1;
			}
			// log beta(n,Yn) = log M1(n,Yn) + log sum/Yn0 exp (log beta'(n0,Yn0))
			logBeta.setQuick(nodeIndex, parentAnnotation, mparts.getLogM1Quick(nodeIndex,
					parentAnnotation) +
					TrickUtil.logSumExp(values, 0, childAnnNumber));
		}
	}

	private void fillLogBetaPrime_ordered(NodeAST node, int indexNonOrderedNode)
	{
		IntArrayList parentAnnotationsList = crf.getAnnotationArray(), childAnnotationsList, siblingAnnotationsList;
		Node parent = node.getParentNode();
		Node nextSibling = node.getNextSibling();
//		ConstraintsTrack parentConstraintsTrack = parent.getConstraintsTrack(), nodeConstraintsTrack = node
//				.getConstraintsTrack();
		int childIndex = node.getIndex();
		int parentAnnotationsSize = parentAnnotationsList.size();
		
		if (nextSibling == null)
		{
			// case of a last child
			for (int parentAnnotationIndex = 0; parentAnnotationIndex < parentAnnotationsSize; parentAnnotationIndex += 1)
			{
				int parentAnnotation = parentAnnotationsList.getQuick(parentAnnotationIndex);
				// parentAnnotation not usable because of previous contraint application
			//	if (!parent.isAnnotationAllowedForNode(parentAnnotation))
					if(!nodetransformmapinteger.get(parent.getNodeType()).contains(parentAnnotation))
					   continue;
					
					if(!crf.isLogicTransformSuitable(parent, parentAnnotation)) {
						continue;
					}
				//
				childAnnotationsList = crf.getAnnotationArray();
				int childAnnotationsSize = childAnnotationsList.size();
				
				for (int annotationIndex = 0; annotationIndex < childAnnotationsSize; annotationIndex += 1)
				{
					// log beta'(n,Yn) = log M2(n,Yn',Yn) + log beta(n,Yn) if the parent node has no attributes
					// log beta'(n,Yn) = log M2(n,Yn',Yn) + log beta(n,Yn) + log sum/Yatt exp(log beta'(att,Yn,Yatt)) if the parent node has attributes
					int childAnnotation = childAnnotationsList.getQuick(annotationIndex);
					// constraint test
//					if (!node.isAnnotationAllowedForEdgeChild(parentAnnotation,
//							childAnnotation))
					if(!nodetransformmapinteger.get(node.getNodeType()).contains(childAnnotation))
						continue;
					
					if(!crf.isLogicTransformSuitable(node, childAnnotation)) {
						continue;
					}
					//
					double logBetaValue = logBeta.getQuick(childIndex, childAnnotation);
					logBetaPrime.setQuick(childIndex, parentAnnotation, childAnnotation,
							logBetaValue +
									mparts.getLogM2Quick(childIndex, parentAnnotation,
											childAnnotation));
				}
			}
		}
		else
		{
			// the node has siblings
			int siblingIndex = nextSibling.getIndex();
		//	ConstraintsTrack nextSiblingConstraintsTrack = nextSibling.getConstraintsTrack();
			for (int parentAnnotationIndex = 0; parentAnnotationIndex < parentAnnotationsSize; parentAnnotationIndex += 1)
			{
				int parentAnnotation = parentAnnotationsList.getQuick(parentAnnotationIndex);
				// parentAnnotation not usable because of previous contraint application
			//	if (!parent.isAnnotationAllowedForNode(parentAnnotation))
				if(!nodetransformmapinteger.get(parent.getNodeType()).contains(parentAnnotation))
					continue;
				
				if(!crf.isLogicTransformSuitable(parent, parentAnnotation)) {
					continue;
				}
				//
			//	childAnnotationsList = labelSet.getChildAnnotations(parentAnnotation);
				childAnnotationsList = crf.getAnnotationArray();

				int childAnnotationsSize = childAnnotationsList.size();
				for (int childAnnotationIndex = 0; childAnnotationIndex < childAnnotationsSize; childAnnotationIndex += 1)
				{
					int childAnnotation = childAnnotationsList.getQuick(childAnnotationIndex);
					// constraint test
//					if (!node.isAnnotationAllowedForEdgeChild(parentAnnotation,
//							childAnnotation))
						if(!nodetransformmapinteger.get(node.getNodeType()).contains(childAnnotation))
						  continue;
						
						if(!crf.isLogicTransformSuitable(node, childAnnotation)) {
							continue;
						}
					//
//					siblingAnnotationsList = labelSet.getSiblingAnnotations(parentAnnotation,
//							childAnnotation);
					siblingAnnotationsList = crf.getAnnotationArray();

					double res = mparts
							.getLogM2Quick(childIndex, parentAnnotation, childAnnotation) +
							logBeta.getQuick(childIndex, childAnnotation);
					//
					int siblingAnnotationsSize = siblingAnnotationsList.size();
					int siblingAnnNumber = 0;
					for (int siblingAnnotationIndex = 0; siblingAnnotationIndex < siblingAnnotationsSize; siblingAnnotationIndex += 1)
					{
						int siblingAnnotation = siblingAnnotationsList
								.getQuick(siblingAnnotationIndex);
						// constraint
//						if (!nextSibling.isAnnotationAllowedForEdgeChild(
//								parentAnnotation, siblingAnnotation) ||
//								!node.isTriangleAnnotable(parentAnnotation,
//										childAnnotation, siblingAnnotation))
							if(!nodetransformmapinteger.get(nextSibling.getNodeType()).contains(siblingAnnotation))
							    continue;
							
							if(!crf.isLogicTransformSuitable(nextSibling, siblingAnnotation)) {
								continue;
							}
						//
						values[siblingAnnNumber++] = mparts.getLogM3Quick(childIndex,
								parentAnnotation, childAnnotation, siblingAnnotation) +
								logBetaPrime.getQuick(siblingIndex, parentAnnotation,
										siblingAnnotation);

					}
					res = res + TrickUtil.logSumExp(values, 0, siblingAnnNumber);
					// log beta'(n_i,Yn_i) = log M2(n_i,Yn,Yn_i) + log beta(n_i,Yn_i) + log sum exp/Ynj (log beta'(n_j,Yn,Yn_j) + log M3(n_i,Yn,Yn_i,Yn_j))
					logBetaPrime.setQuick(childIndex, parentAnnotation, childAnnotation, res);
				}
			}

		}
		// at last, calculates log betaPrime for the previous sibling if it
		// exists.
		NodeAST previousSibling = (NodeAST) node.getPreviousSibling();
		if (previousSibling != null)
			fillLogBetaPrime_ordered(previousSibling, indexNonOrderedNode);
	}

	private void fillLogBetaSecond_ordered(NodeAST node)
	{
		IntArrayList parentAnnotationsList = crf.getAnnotationArray(), childAnnotationsList, siblingAnnotationsList;
		Node previousSibling = node.getPreviousSibling();
		int childIndex = node.getIndex();
		Node parent = node.getParentNode();
//		ConstraintsTrack parentConstraintsTrack = parent.getConstraintsTrack(), nodeConstraintsTrack = node
//				.getConstraintsTrack();
		int parentAnnotationsSize = parentAnnotationsList.size();
		if (previousSibling == null)
		{
			// case of a first child
			for (int parentAnnotationIndex = 0; parentAnnotationIndex < parentAnnotationsSize; parentAnnotationIndex += 1)
			{
				int parentAnnotation = parentAnnotationsList.getQuick(parentAnnotationIndex);
				// constraint test
			//	if (!parent.isAnnotationAllowedForNode(parentAnnotation))
					if(!nodetransformmapinteger.get(parent.getNodeType()).contains(parentAnnotation))
					   continue;
					
					if(!crf.isLogicTransformSuitable(parent, parentAnnotation)) {
						continue;
					}
				//
			//	childAnnotationsList = labelSet.getChildAnnotations(parentAnnotation);
				childAnnotationsList = crf.getAnnotationArray();

				int childAnnotationsSize = childAnnotationsList.size();
				for (int childAnnotationIndex = 0; childAnnotationIndex < childAnnotationsSize; childAnnotationIndex += 1)
				{
					int childAnnotation = childAnnotationsList.getQuick(childAnnotationIndex);
					// constraint test
//					if (!node.isAnnotationAllowedForEdgeChild(parentAnnotation,
//							childAnnotation))
					if(!nodetransformmapinteger.get(node.getNodeType()).contains(childAnnotation))
						continue;
					
					if(!crf.isLogicTransformSuitable(node, childAnnotation)) {
						continue;
					}
					// log beta''(n_i,Yn_i) = 0
					logBetaSecond.setQuick(childIndex, parentAnnotation, childAnnotation, 0.0);
				}
			}
		}
		else
		{
			int siblingIndex = previousSibling.getIndex();
//			ConstraintsTrack previousSiblingConstraintsTrack = previousSibling
//					.getConstraintsTrack();
			double resS, resP;
			//
			valuesMatCpt.reset();
			for (int parentAnnotationIndex = 0; parentAnnotationIndex < parentAnnotationsSize; parentAnnotationIndex += 1)
			{
				int parentAnnotation = parentAnnotationsList.getQuick(parentAnnotationIndex);
				// constraint test
			//	if (!parent.isAnnotationAllowedForNode(parentAnnotation))
				if(!nodetransformmapinteger.get(parent.getNodeType()).contains(parentAnnotation))
					continue;
				
				if(!crf.isLogicTransformSuitable(parent, parentAnnotation)) {
					continue;
				}
				//
			//	siblingAnnotationsList = labelSet.getChildAnnotations(parentAnnotation);
				siblingAnnotationsList = crf.getAnnotationArray();

				int siblingAnnotationsSize = siblingAnnotationsList.size();
				
				if (siblingAnnotationsSize == 0)
					continue;
				
				for (int siblingAnnotationIndex = 0; siblingAnnotationIndex < siblingAnnotationsSize; siblingAnnotationIndex += 1)
				{
					int siblingAnnotation = siblingAnnotationsList.getQuick(siblingAnnotationIndex);
					// constraint test
//					if (!previousSibling.isAnnotationAllowedForEdgeChild(
//							parentAnnotation, siblingAnnotation))
					if(!nodetransformmapinteger.get(previousSibling.getNodeType()).contains(siblingAnnotation))
						continue;
					
					if(!crf.isLogicTransformSuitable(previousSibling, siblingAnnotation)) {
						continue;
					}
					//
//					childAnnotationsList = labelSet.getSiblingAnnotations(parentAnnotation,
//							siblingAnnotation);
					childAnnotationsList = crf.getAnnotationArray();

					int childAnnotationsSize = childAnnotationsList.size();

					resS = logBeta.getQuick(siblingIndex, siblingAnnotation);
					// log beta(n_i,Yn_i) + log beta''(n_i,Yn,Yn_i) + log M2(n_i,Yn,Yn_i) ...
					resP = resS +
							logBetaSecond.getQuick(siblingIndex, parentAnnotation,
									siblingAnnotation) +
							mparts.getLogM2Quick(siblingIndex, parentAnnotation, siblingAnnotation);
					for (int childAnnotationIndex = 0; childAnnotationIndex < childAnnotationsSize; childAnnotationIndex += 1)
					{
						int childAnnotation = childAnnotationsList.getQuick(childAnnotationIndex);
						// constraint test
//						if (!node.isAnnotationAllowedForEdgeChild(parentAnnotation,
//								childAnnotation) ||
//								!previousSibling.isTriangleAnnotable(
//										parentAnnotation, siblingAnnotation, childAnnotation))
						if(!nodetransformmapinteger.get(node.getNodeType()).contains(childAnnotation))
							continue;
						
						if(!crf.isLogicTransformSuitable(node, childAnnotation)) {
							continue;
						}
						//
						int siblingCpt = valuesMatCpt.getQuick(parentAnnotation, childAnnotation);
						// ... + log M3(n_i,Yn,Yn_i,Yn_j)
						valuesMat.setQuick(parentAnnotation, childAnnotation, siblingCpt, resP +
								mparts.getLogM3Quick(siblingIndex, parentAnnotation,
										siblingAnnotation, childAnnotation));
						valuesMatCpt.setQuick(parentAnnotation, childAnnotation, siblingCpt + 1);
					}
				}
				// fill logBetaSecond
			//	childAnnotationsList = labelSet.getChildAnnotations(parentAnnotation);
				childAnnotationsList = crf.getAnnotationArray();

				int childAnnotationsSize = childAnnotationsList.size();
				for (int childAnnotationIndex = 0; childAnnotationIndex < childAnnotationsSize; childAnnotationIndex += 1)
				{
					int childAnnotation = childAnnotationsList.getQuick(childAnnotationIndex);
					// constraint test
//					if (!node.isAnnotationAllowedForEdgeChild(parentAnnotation,
//							childAnnotation))
						if(!nodetransformmapinteger.get(node.getNodeType()).contains(childAnnotation))
						   continue;
						
						if(!crf.isLogicTransformSuitable(node, childAnnotation)) {
							continue;
						}
					//
					int siblingCpt = valuesMatCpt.getQuick(parentAnnotation, childAnnotation);
					if (siblingCpt == 0)
						logBetaSecond.setQuick(childIndex, parentAnnotation, childAnnotation,
								Double.NEGATIVE_INFINITY);
					else
					{
						// the sum of the elements of a row matrix gives log beta''(n_j,Yn,Yn_j)
						// log beta''(n_j,Yn,Yn_j) = log sum/Yn_i exp(log beta(n_i,Yn_i) + log beta''(n_i,Yn,Yn_i) + log M2(n_i,Yn,Yn_i) + log M3(n_i,Yn_Yn_i,Yn_j))
						int startIndex = parentAnnotation * annotationsNumber * annotationsNumber +
								childAnnotation * annotationsNumber;
						logBetaSecond.setQuick(childIndex, parentAnnotation, childAnnotation,
								TrickUtil.logSumExp(valuesMat.getElements(), startIndex,
										startIndex + siblingCpt));
					}
				}
			}
		}
		// at last, calculates log betaPrime for the next sibling if it
		// exists.

		NodeAST nextSibling = (NodeAST) node.getNextSibling();
		if (nextSibling != null)
			fillLogBetaSecond_ordered(nextSibling);

	}

	@Override
	public double getLogBeta(Node node, int annotationIndex)
	{
		return logBeta.getQuick(node.getIndex(), annotationIndex);
	}

	@Override
	public double getLogBetaPrime(Node childNode, int parentAnnotationIndex,
			int childAnnotationIndex)
	{
		return logBetaPrime.getQuick(childNode.getIndex(), parentAnnotationIndex,
				childAnnotationIndex);
	}

	@Override
	public double getLogBetaSecond(Node childNode, int parentAnnotationIndex,
			int childAnnotationIndex)
	{
		return logBetaSecond.getQuick(childNode.getIndex(), parentAnnotationIndex,
				childAnnotationIndex);
	}

	@Override
	public void setNewTree(Tree tree)
	{
		int nodesNumber = tree.getNodesNumber();
		logBeta.setNewSizeOrInit(nodesNumber, annotationsNumber);
		logBetaPrime.setNewSize(nodesNumber, annotationsNumber);
		logBetaSecond.setNewSize(nodesNumber, annotationsNumber);
		this.tree = tree;
	}

}
