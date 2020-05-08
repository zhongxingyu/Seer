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
package mostrare.operations.tree;

import mostrare.crf.tree.CRF;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.operations.TrickUtil;
import mostrare.operations.parts.tree.BetaParts;
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
public class ValuesCalc_locCons implements ValuesCalc
{

	private static ValuesCalc	instance	= null;
	
	static
	{
		instance = new ValuesCalc_locCons();
	}

	private ValuesCalc_locCons()
	{
	}

	public static ValuesCalc getInstance()
	{
		return instance;
	}

	@Override
	public double logZ(BetaParts betaParts, Tree tree, CRF crf)
	{
		CRFWithConstraintNode crfC = (CRFWithConstraintNode) crf;
	//	IntArrayList annotations = crfC.getComputationLabels().getParentAnnotations();
		IntArrayList annotations = crfC.getAnnotationArray();
		
		Map<String, ArrayList<Integer>> nodetransformmapinteger = crfC.nodetransformmapinteger;

		double[] valueZ = new double[annotations.size()];
		int annNumber = 0;
		for (int annotationIndex = 0; annotationIndex < annotations.size(); annotationIndex += 1)
		{
			int annotation = annotations.getQuick(annotationIndex);
			// if (tree.getRoot().isAnnotationAllowedForNode(annotation))
			if (nodetransformmapinteger.get(tree.getRoot().getNodeType()).contains(annotation))
				valueZ[annNumber++] = betaParts.getLogBeta(tree.getRoot(), annotation);
		}
		// log Z = log sum/Yroot exp(log beta(root,Yroot))
		return TrickUtil.logSumExp(valueZ, 0, annNumber);
	}

	@Override
	public double logScore(MParts mparts, Tree tree, CRF crf)
	{
		return logScore(mparts, tree.getRoot(), crf);
	}

	private double logScore(MParts mparts, Node node, CRF crf)
	{
		// log score(n) = ...
		double res = 0.0;
		int nodeIndex = node.getIndex();
		// ... + log M1(n,Yn) ...
		res += mparts.getLogM1Quick(nodeIndex, node.getAnnotation());
		if (node.getOrderedNodesNumber() > 0)
		{
			NodeAST child = (NodeAST) node.getOrderedNodeAt(0);
			// ... + sum/n_i log M2(n_i,Yn,Yn_i) + log M3(n_i,Yn,Yn_i,Yn_j) + log score(n_i) ...
			while (child != null)
			{

				int childIndex = child.getIndex();

				res += mparts
						.getLogM2Quick(childIndex, node.getAnnotation(), child.getAnnotation());

				NodeAST sibling = (NodeAST) child.getNextSibling();
				if (sibling != null)
					res += mparts.getLogM3Quick(childIndex, node.getAnnotation(), child
							.getAnnotation(), sibling.getAnnotation());

				res += logScore(mparts, child, crf);
				child = sibling;
			}
		}
		
		return res;
	}
}
