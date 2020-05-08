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
import mostrare.matrix.miniImpl.DenseDoubleMatrix2D;
import mostrare.matrix.miniImpl.DoubleMatrix2D;
import mostrare.matrix.miniImpl.FlexDenseDoubleMatrix2D;
import mostrare.operations.TrickUtil;
import mostrare.operations.parts.tree.BetaParts;
import mostrare.operations.parts.tree.FlexAlphaParts;
import mostrare.operations.parts.tree.MParts;
import mostrare.tree.Node;
import mostrare.tree.Tree;

import java.util.ArrayList;
import java.util.Map;

import cern.colt.list.IntArrayList;

/**
 * @author missi
 */
public class FlexAlphaPartsImpl implements FlexAlphaParts
{
	private CRF						crf;

	/**
	 * iterator over annotations
	 */
	
	private Tree					tree;

	/**
	 * beta values used to compute alpha values
	 */
	private BetaParts				betaParts;

	/**
	 * stores alpha values
	 */
	private FlexDenseDoubleMatrix2D	logAlpha;

	/**
	 * used to hold temporary computation values before log-sum-exp
	 */
	private DoubleMatrix2D			matrix2d;

	private int						annotationsNumber;

	private int[]					allowedLabelsNumber;

	/**
	 * where M1, M2 and M3 values are calculated and stored
	 */
	private MParts					mparts;

	/**
	 * Constructs the matrix that will hold alpha and alphaPrime values.
	 * 
	 * @param crf
	 * @param mparts
	 * @param betaParts
	 */
	private Map<String, ArrayList<Integer>>  nodetransformmapinteger;

	public FlexAlphaPartsImpl(CRFWithConstraintNode crf, MParts mparts, BetaParts betaParts)
	{
		this.crf = crf;
		this.tree = null;
		this.mparts = mparts;
		this.betaParts = betaParts;
		annotationsNumber = crf.getAnnotationsNumber();
		allowedLabelsNumber = new int[annotationsNumber];

		logAlpha = new FlexDenseDoubleMatrix2D(0, 0);

		matrix2d = new DenseDoubleMatrix2D(annotationsNumber, annotationsNumber);
		
		this.nodetransformmapinteger = crf.nodetransformmapinteger;

	}

	@Override
	public void fillLogAlphas()
	{
		fillLogAlpha(tree.getRoot());
	}

	private void fillLogAlpha(Node node)
	{
		int nodeIndex = node.getIndex();
		if (tree.getRoot() == node)
		{
			// log alpha(root,Yroot) = 0.0
			logAlpha.setQuick(nodeIndex, 0, 0.0);
			for (int annotationIndex = 1; annotationIndex < annotationsNumber; annotationIndex += 1)
				logAlpha.setQuickNext(0.0);
		}
		else
		{
			Node parent = node.getParentNode();
//			ConstraintsTrack parentConstraintsTrack = parent.getConstraintsTrack(), nodeConstraintsTrack = node
//					.getConstraintsTrack();
			int parentIndex = parent.getIndex();
			// inits the array that stores the number of allowed annotations for a node given the annotation of its parent node
			for (int i = 0; i < annotationsNumber; i++)
				allowedLabelsNumber[i] = 0;
			//
		//	IntArrayList parentAnnotationsList = labelSet.getParentAnnotations(), childAnnotationsList;
			IntArrayList parentAnnotationsList = crf.getAnnotationArray(), childAnnotationsList;

			int parentAnnotationsSize = parentAnnotationsList.size();
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
				// childAnnotationsList = labelSet.getChildAnnotations(parentAnnotation);
				childAnnotationsList = crf.getAnnotationArray();
				
				double valPart = logAlpha.getQuick(parentIndex, parentAnnotation) +
						mparts.getLogM1Quick(parentIndex, parentAnnotation);

				int childAnnotationsSize = childAnnotationsList.size();

				for (int annotationIndex = 0; annotationIndex < childAnnotationsSize; annotationIndex += 1)
				{
					int childAnnotation = childAnnotationsList.getQuick(annotationIndex);
					// constraint test
//					if (!node.isAnnotationAllowedForEdgeChild(parentAnnotation,
//							childAnnotation))
					if(!nodetransformmapinteger.get(node.getNodeType()).contains(childAnnotation))
						continue;
					
					if(!crf.isLogicTransformSuitable(node, childAnnotation)) {
						continue;
					}
					// log alpha(n',Yn') + log M1(n',Yn') + log beta''(n,Yn',Yn) + log beta'(n,Yn',Yn) - log beta(n,Yn) 
					matrix2d.setQuick(childAnnotation, allowedLabelsNumber[childAnnotation],
							valPart +
									betaParts.getLogBetaSecond(node, parentAnnotation,
											childAnnotation) +
									betaParts.getLogBetaPrime(node, parentAnnotation,
											childAnnotation) -
									betaParts.getLogBeta(node, childAnnotation));
					allowedLabelsNumber[childAnnotation] = allowedLabelsNumber[childAnnotation] + 1;
				}
			}
			//
			for (int annotationIndex = 0; annotationIndex < parentAnnotationsSize; annotationIndex += 1)
			{
				int annotation = parentAnnotationsList.getQuick(annotationIndex);
				// constraint test
			//	if (!node.isAnnotationAllowedForNode(annotation))
				if(!nodetransformmapinteger.get(node.getNodeType()).contains(annotation))
					continue;
				
				if(!crf.isLogicTransformSuitable(node, annotation)) {
					continue;
				}
				// sum the values of a matrix raw to compute log alpha(n,Yn)
				// log alpha(n,Yn) = log sum/Yn' exp (log alpha(n',Yn') + log M1(n',Yn') + log beta''(n,Yn',Yn) + log beta'(n,Yn',Yn) - log beta(n,Yn))
				if (allowedLabelsNumber[annotation] == 0)
					logAlpha.setQuick(nodeIndex, annotation, Double.NEGATIVE_INFINITY);
				else
				{
					int startIndex = annotationsNumber * annotation;
					logAlpha.setQuick(nodeIndex, annotation, TrickUtil.logSumExp(matrix2d
							.getElements(), startIndex, startIndex +
							allowedLabelsNumber[annotation]));
				}
			}
		}
		if (node.getOrderedNodesNumber() > 0)
			for (int childPos = 0; childPos < node.getOrderedNodesNumber(); childPos += 1)
				fillLogAlpha(node.getOrderedNodeAt(childPos));
	}

	@Override
	public double getLogAlpha(Node node, int annotationIndex)
	{
		return logAlpha.getQuick(node.getIndex(), annotationIndex);
	}

	@Override
	public void setNewTree(Tree tree)
	{
		int nodesNumber = tree.getNodesNumber();
		logAlpha.setNewSizeOrInit(nodesNumber, annotationsNumber);
		this.tree = tree;
	}
}
