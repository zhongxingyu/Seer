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
import mostrare.matrix.miniImpl.DenseDoubleMatrix3D;
import mostrare.matrix.miniImpl.DenseIntArrayListMatrix2D;
import mostrare.matrix.miniImpl.DenseIntArrayListMatrix3D;
import mostrare.matrix.miniImpl.DoubleMatrix2D;
import mostrare.matrix.miniImpl.DoubleMatrix3D;
import mostrare.matrix.miniImpl.IntArrayListMatrix2D;
import mostrare.matrix.miniImpl.IntArrayListMatrix3D;
import mostrare.operations.parts.tree.DeltaParts;
import mostrare.operations.parts.tree.FlexMParts;
import mostrare.tree.Node;
import mostrare.tree.Tree;
import mostrare.tree.impl.NodeAST;
import mostrare.util.ConfigurationTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;
import org.javatuples.Quartet;

import cern.colt.list.IntArrayList;

/**
 * @author missi
 */
public class DeltaPartsImpl_KBest implements DeltaParts
{
	private CRF						crf;

	private Tree					tree;

	private DoubleMatrix2D[]			deltaarray;

	private DoubleMatrix3D[]			deltaPrimearray;

	/**
	 * deltaAnnotationPath[i,j]: array of annotation index associated with delta[<code>i</code>,<code>j</code>].
	 * The k-th element of this array is the annotation index associated with the k-th child of the
	 * node of index <code>i</code>-th.
	 */
	private IntArrayListMatrix2D[]	deltaAnnotationPatharray;
	
	private IntArrayListMatrix2D[]	deltaAnnotationPathIndexarray;

	/**
	 * 
	 */
	private IntArrayListMatrix3D[]	deltaPrimeAnnotationPatharray;
	
	private IntArrayListMatrix3D[]	deltaPrimeAnnotationPathIndexarray;

	/**
	 * where M1, M2 and M3 values are calculated and stored
	 */
	private FlexMParts				mparts;

	/**
	 * Constructs the matrix that will hold delta and deltaPrime values.
	 * 
	 * @param crf
	 * @param tree
	 * @param mparts
	 */
	
	private Map<String, ArrayList<Integer>>  nodetransformmapinteger;

	public DeltaPartsImpl_KBest(CRFWithConstraintNode crf, FlexMParts mparts)
	{
		this.crf = crf;
		this.mparts = mparts;
		this.nodetransformmapinteger = crf.nodetransformmapinteger;
	}

	@Override
	public int[] argmaxDelta() {
		
		return new int[0];
	}
	
	@Override
	public int[][] argmaxDeltaTopk()
	{			
		Node root = tree.getRoot();
		int nodeIndex = root.getIndex();
		// get the labels to use for the root
		IntArrayList parentAnnotationsList = crf.getAnnotationArray();
		int parentAnnotationsSize = parentAnnotationsList.size();
		
		List<Triple<Integer, Integer, Double>> wholeinformation = new ArrayList<Triple<Integer, Integer, Double>>();

	    for (int parentAnnotationIndex = 0; parentAnnotationIndex < parentAnnotationsSize; parentAnnotationIndex += 1)
		 {
			// get the current annotation
			int parentAnnotation = parentAnnotationsList.getQuick(parentAnnotationIndex);
		//	if (!root.isAnnotationAllowedForNode(parentAnnotation))
			if(!nodetransformmapinteger.get(root.getNodeType()).contains(parentAnnotation))
				continue; 
			
			if(!crf.isLogicTransformSuitable(root, parentAnnotation)) {
				continue;
			}
						
			int legalnumber=0;
						
			for(int index_delta=0; index_delta < deltaarray.length; index_delta++) {
				if(deltaarray[index_delta].getQuick(nodeIndex, parentAnnotation)>-100000) {		
					legalnumber+=1;
				} 
			}
			
			double delta_interm[] = new double[legalnumber];

            for(int index_delta=0; index_delta < legalnumber; index_delta++) {				
				 delta_interm[index_delta]=deltaarray[index_delta].getQuick(nodeIndex, parentAnnotation);
			}
			
            for(int index_delta=0; index_delta < legalnumber; index_delta++) {
                 wholeinformation.add(Triple.of(index_delta, parentAnnotation, 
                		delta_interm[index_delta]));
            }
		}
		
		List<Triple<Integer, Integer, Double>> calcuatedtopKvalue= determineTopKvalue (wholeinformation);
				
		int[][] annotationresult=new int[calcuatedtopKvalue.size()][tree.getNodesNumber()];
		int resultindex=0;
		
		for(int index=0; index<calcuatedtopKvalue.size(); index++) {
			
			int[] argmax = new int[tree.getNodesNumber()];
			Triple<Integer, Integer, Double> specificvale=calcuatedtopKvalue.get(index);
            int indexofannotation=specificvale.getMiddle();
            
            int groupnumber=specificvale.getLeft();

    		fillArgmax(argmax, nodeIndex, indexofannotation, groupnumber);
    		
    		annotationresult[resultindex]=argmax;
    		resultindex=resultindex+1;
		}
		
		return annotationresult;
	}

	/**
	 * Fills <code>argmax</code> from the annotations associated with delta[<code>nodeIndex</code>,<code>annotationIndex</code>].
	 * 
	 * @param argmax
	 * @param nodeIndex
	 * @param annotationIndex
	 */
	private void fillArgmax(int[] argmax, int nodeIndex, int annotationIndex, int group)
	{
		argmax[nodeIndex] = annotationIndex;

		IntArrayList annotations = deltaAnnotationPatharray[group].getQuick(nodeIndex, annotationIndex);
		
		IntArrayList groupindexs = deltaAnnotationPathIndexarray[group].getQuick(nodeIndex, annotationIndex);

		Node node = tree.getNode(nodeIndex);
		int orderedNodesNumber = node.getOrderedNodesNumber();
		if (orderedNodesNumber > 0)
		{
			NodeAST child = (NodeAST) node.getOrderedNodeAt(0);
			for (int i = 0; i < orderedNodesNumber; i += 1)
			{
				fillArgmax(argmax, child.getIndex(), annotations.get(i), groupindexs.get(i));
				child = (NodeAST) child.getNextSibling();
			}
		}
	}

	@Override
	public void fillDeltas(Tree tree)
	{
		// init values with the tree
		this.tree = tree;

		int nodesNumber = tree.getNodesNumber();
		int annotationsNumber = crf.getAnnotationsNumber();
		
		int topk = ConfigurationTool.getInstance().getOutputNumber();

		deltaarray=new DoubleMatrix2D[topk];
		
		for(int index=0; index<topk; index++)
			deltaarray[index] = new DenseDoubleMatrix2D(nodesNumber, annotationsNumber);

		deltaPrimearray=new DoubleMatrix3D[topk];

		for(int index=0; index<topk; index++)
			deltaPrimearray[index] = new DenseDoubleMatrix3D(nodesNumber, annotationsNumber);
		
		deltaAnnotationPatharray=new IntArrayListMatrix2D[topk];

		for(int index=0; index<topk; index++)
			deltaAnnotationPatharray[index] = new DenseIntArrayListMatrix2D(nodesNumber, annotationsNumber);
		
		deltaAnnotationPathIndexarray = new IntArrayListMatrix2D[topk];
		
		for(int index=0; index<topk; index++)
			deltaAnnotationPathIndexarray[index] = new DenseIntArrayListMatrix2D(nodesNumber, annotationsNumber);
		
		deltaPrimeAnnotationPatharray=new IntArrayListMatrix3D[topk];
		
		for(int index=0; index<topk; index++)
			deltaPrimeAnnotationPatharray[index] = new DenseIntArrayListMatrix3D(nodesNumber, annotationsNumber);
		
		deltaPrimeAnnotationPathIndexarray=new IntArrayListMatrix3D[topk];
		
		for(int index=0; index<topk; index++)
			deltaPrimeAnnotationPathIndexarray[index] = new DenseIntArrayListMatrix3D(nodesNumber, annotationsNumber);

		// set mparts
		mparts.setNewTree(tree, false);
		
		fillDelta(tree.getRoot());
	}

	/**
	 * Computes log delta for the <code>node</code> and for each annotation.
	 * 
	 * @param node
	 *            node of the tree
	 */
	private void fillDelta(Node node)
	{
		int nodeIndex = node.getIndex();
		
		IntArrayList parentAnnotationsList, childAnnotationsList;
		// get the labels to use
		parentAnnotationsList = crf.getAnnotationArray();
		int parentAnnotationsSize = parentAnnotationsList.size();
		// if there are ordered children
		if (node.getOrderedNodesNumber() > 0)
		{
			NodeAST firstChild = (NodeAST) node.getOrderedNodeAt(0);
			int firstChildIndex = firstChild.getIndex();
			NodeAST lastChild = (NodeAST) node.getOrderedNodeAt(node
					.getOrderedNodesNumber() - 1);

			// first, calculates delta values for node children
			int childrenNumber = node.getOrderedNodesNumber();
			for (int childPos = 0; childPos < childrenNumber; childPos += 1)
				fillDelta(node.getOrderedNodeAt(childPos));
			// then calculates deltaPrime values for node children
			fillDeltaPrime_ordered(lastChild, -1);
			// at last, calculates delta[y] for each y (annotation)
			for (int parentAnnotationIndex = 0; parentAnnotationIndex < parentAnnotationsSize; parentAnnotationIndex += 1)
			{
				// get the current annotation to use
				int parentAnnotation = parentAnnotationsList.getQuick(parentAnnotationIndex);
				// tests constraint on parent node (subtree root)
				//if (!node.isAnnotationAllowedForNode(parentAnnotation))
				if(!nodetransformmapinteger.get(node.getNodeType()).contains(parentAnnotation))
					continue;
				
				if(!crf.isLogicTransformSuitable(node, parentAnnotation)) {
					continue;
				}
				//
			//	childAnnotationsList = labelSet.getChildAnnotations(parentAnnotation);
				childAnnotationsList = crf.getAnnotationArray();
				int childAnnotationsSize = childAnnotationsList.size();
				// log delta = log M1 + max log deltaPrime
				
				double res = mparts.calcLogM1(node, parentAnnotation);
				
				List<Triple<Integer, Integer, Double>> wholeinformation = new ArrayList<Triple<Integer, Integer, Double>>();

				for (int firstChildAnnotationIndex = 0; firstChildAnnotationIndex < childAnnotationsSize; firstChildAnnotationIndex += 1)
				{
					// get the label to use for the child node
					int childAnnotation = childAnnotationsList.getQuick(firstChildAnnotationIndex);
					// test constraint on edge
//					if (!firstChild.isAnnotationAllowedForEdgeChild(
//							parentAnnotation, childAnnotation))
					if(!nodetransformmapinteger.get(firstChild.getNodeType()).contains(childAnnotation))
						continue;
					
					if(!crf.isLogicTransformSuitable(firstChild, childAnnotation)) {
						continue;
					}
					
					int legalnumberprime=0;
					
					for(int index_deltaprime=0; index_deltaprime < deltaPrimearray.length; index_deltaprime++) {
						if(deltaPrimearray[index_deltaprime].getQuick(firstChildIndex,
								parentAnnotation, childAnnotation)> -100000) {	
							legalnumberprime+=1;
						} 
					}
					
					double delta_intermprime[] = new double[legalnumberprime];

                    for(int index_deltaprime=0; index_deltaprime < legalnumberprime; index_deltaprime++) {				
                    	delta_intermprime[index_deltaprime]=deltaPrimearray[index_deltaprime].getQuick(firstChildIndex,
								parentAnnotation, childAnnotation);
					}
					
                    for(int index_deltaprime=0; index_deltaprime < legalnumberprime; index_deltaprime++) {
                        wholeinformation.add(Triple.of(index_deltaprime, childAnnotation, 
                        		res+delta_intermprime[index_deltaprime]));
                    }
                    
				}
				
				List<Triple<Integer, Integer, Double>> calcuatedtopKvalue=determineTopKvalue(wholeinformation);

				for(int index=0; index<calcuatedtopKvalue.size(); index++) {
					Triple<Integer, Integer, Double> specificvale=calcuatedtopKvalue.get(index);
					
					deltaAnnotationPatharray[index].setQuick(nodeIndex, parentAnnotation, 
							deltaPrimeAnnotationPatharray[specificvale.getLeft()]
							.getQuick(firstChildIndex, parentAnnotation, specificvale.getMiddle()).copy());
					
					deltaarray[index].setQuick(node.getIndex(), parentAnnotation, specificvale.getRight());

					deltaAnnotationPathIndexarray[index].setQuick(nodeIndex, parentAnnotation, deltaPrimeAnnotationPathIndexarray[specificvale.getLeft()]
							.getQuick(firstChildIndex, parentAnnotation, specificvale.getMiddle()).copy());

			   }
				
			   if(calcuatedtopKvalue.size()<deltaarray.length) {
					for(int index=(calcuatedtopKvalue.size()); index<deltaarray.length; index++) {
						deltaarray[index].setQuick(node.getIndex(), parentAnnotation, -120000);

						deltaAnnotationPatharray[index].setQuick(nodeIndex, parentAnnotation, 
								 new IntArrayList(0));
						
						deltaAnnotationPathIndexarray[index].setQuick(nodeIndex, parentAnnotation, new IntArrayList(0));
				 }
			  } 
		   }
		}
		if (node.getOrderedNodesNumber() == 0)
		{
				// case of a leaf
				// calculates log delta[y] for each y (annotation)
				for (int annotationIndex = 0; annotationIndex < parentAnnotationsSize; annotationIndex += 1)
				{
					int parentAnnotation = parentAnnotationsList.getQuick(annotationIndex);
					// tests constraint on parent node (subtree root)
				//	if (!node.isAnnotationAllowedForNode(parentAnnotation))
				    if(!nodetransformmapinteger.get(node.getNodeType()).contains(parentAnnotation))
						continue;
				    
				    if(!crf.isLogicTransformSuitable(node, parentAnnotation)) {
						continue;
					}
					//
				    for(int index=0; index<deltaAnnotationPatharray.length; index++) {
						deltaAnnotationPatharray[index].setQuick(nodeIndex, parentAnnotation, new IntArrayList(0));
						deltaAnnotationPathIndexarray[index].setQuick(nodeIndex, parentAnnotation, new IntArrayList(0));
				    }
				    					
					deltaarray[0].setQuick(nodeIndex, parentAnnotation, mparts.calcLogM1(node,
							parentAnnotation));

					for(int index=1; index<deltaarray.length; index++) {
					    deltaarray[index].setQuick(nodeIndex, parentAnnotation, -120000);
				    }
			 }	
		}
	}

	  private List<Triple<Integer, Integer, Double>> determineTopKvalue(List<Triple<Integer, Integer, Double>> wholeinfo) {
		   
			List<Triple<Integer, Integer, Double>> retrunvale = new ArrayList<Triple<Integer, Integer, Double>>();
			
			Collections.sort(wholeinfo, new Comparator<Triple<Integer, Integer, Double>>() {		
			    @Override
			    public int compare(Triple<Integer, Integer, Double> lhs, Triple<Integer, Integer, Double> rhs) {
			        //-1 - less than, 1 - greater than, 0 - equal, all inversed for descending
			    	return Double.compare(lhs.getRight().doubleValue(), rhs.getRight().doubleValue()) > 0 ? 
			    			-1 : Double.compare(lhs.getRight().doubleValue(), rhs.getRight().doubleValue()) < 0 ? 1 : 0;		
			    }
			});
			
			int numbertoreturn=deltaarray.length;
			
			if(wholeinfo.size()<numbertoreturn)
				return wholeinfo;
			else {
				for (int index=0; index<numbertoreturn; index++)
					retrunvale.add(wholeinfo.get(index));
				
				return retrunvale;
			}
	}
	
	/**
	 * Computes log deltaPrime for the <code>node</code> and for all the labels.
	 * 
	 * @param node
	 *            node of the tree
	 * @param indexNonOrderedNode
	 *            index of the "first" attribute of the parent node
	 */
	private void fillDeltaPrime_ordered(NodeAST node, int indexNonOrderedNode)
	{
		Node parent = node.getParentNode();
		Node nextSibling = node.getNextSibling();
		int childIndex = node.getIndex();
		IntArrayList parentAnnotationsList, childAnnotationsList, siblingAnnotationsList;
		// get the labels that can be used for a node
	//	parentAnnotationsList = labelSet.getParentAnnotations();
		parentAnnotationsList = crf.getAnnotationArray();

		int parentAnnotationsSize = parentAnnotationsList.size();
		
		if (nextSibling == null)
		{
			// case of a last child
			for (int parentAnnotationIndex = 0; parentAnnotationIndex < parentAnnotationsSize; parentAnnotationIndex += 1)
			{
				// get the annotation of the parent node
				int parentAnnotation = parentAnnotationsList.getQuick(parentAnnotationIndex);
				// tests constraint on parent node (subtree root)
		//		if (!parent.isNodeAnnotable(parentAnnotation))
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
					// test constraints on edge
//					if (!node.isAnnotationAllowedForEdgeChild(
//							parentAnnotation, childAnnotation))
						if(!nodetransformmapinteger.get(node.getNodeType()).contains(childAnnotation))
						   continue;
						
						if(!crf.isLogicTransformSuitable(node, childAnnotation)) {
							continue;
						}
					
					double deltaPrimeValue = 
							mparts.calcLogM2(node, parentAnnotation, childAnnotation);

					IntArrayList value;
					
					value = new IntArrayList(new int[] { childAnnotation });
					
					for(int index=0; index<deltaPrimearray.length; index++) {
						if(deltaarray[index].getQuick(childIndex, childAnnotation)<-100000) {
							deltaPrimearray[index].setQuick(childIndex, parentAnnotation, childAnnotation, -120000);
							deltaPrimeAnnotationPatharray[index].setQuick(childIndex, parentAnnotation,
									childAnnotation, new IntArrayList(0));
							deltaPrimeAnnotationPathIndexarray[index].setQuick(childIndex, parentAnnotation,
									childAnnotation, new IntArrayList(0));
						} else {
							deltaPrimearray[index].setQuick(childIndex, parentAnnotation, childAnnotation,
									deltaPrimeValue+deltaarray[index].getQuick(childIndex, childAnnotation));
							deltaPrimeAnnotationPatharray[index].setQuick(childIndex, parentAnnotation,
									childAnnotation, value);
							deltaPrimeAnnotationPathIndexarray[index].setQuick(childIndex, parentAnnotation,
									childAnnotation, new IntArrayList(new int[] {index}));
						}
					}
				}
			}
		}
		else
		{
			int siblingIndex = nextSibling.getIndex();
			for (int parentAnnotationIndex = 0; parentAnnotationIndex < parentAnnotationsSize; parentAnnotationIndex += 1)
			{
				// get an annotation for the parent node
				int parentAnnotation = parentAnnotationsList.getQuick(parentAnnotationIndex);
				// tests constraint on parent node (subtree root)
			//	if (!parent.isAnnotationAllowedForNode(parentAnnotation))
				if(!nodetransformmapinteger.get(parent.getNodeType()).contains(parentAnnotation))
					continue;
				
				if(!crf.isLogicTransformSuitable(parent, parentAnnotation)) {
					continue;
				}
				// get the annotations for the node implied by the annotation of the parent node by
				// parentAnnotation
			//	childAnnotationsList = labelSet.getChildAnnotations(parentAnnotation);
				childAnnotationsList = crf.getAnnotationArray();

				int childAnnotationsSize = childAnnotationsList.size();
				// 
				if (childAnnotationsSize == 0)
				{
					// can't use parentAnnotation to annotate the parent node
					continue;
				}
				// counts the number of annotations that can't be used because of constraints
				for (int childAnnotationIndex = 0; childAnnotationIndex < childAnnotationsSize; childAnnotationIndex += 1)
				{
					// get an annotation for the child node
					int childAnnotation = childAnnotationsList.getQuick(childAnnotationIndex);
					// test constraints on edge
//					if (!node.isAnnotationAllowedForEdgeChild(
//							parentAnnotation, childAnnotation))
						if(!nodetransformmapinteger.get(node.getNodeType()).contains(childAnnotation))
						    continue;
						
						if(!crf.isLogicTransformSuitable(node, childAnnotation)) {
							continue;
						}
					// get the list of labels that can be used to annotate the sibling node
//					siblingAnnotationsList = labelSet.getSiblingAnnotations(parentAnnotation,
//							childAnnotation);
					siblingAnnotationsList = crf.getAnnotationArray();

					int siblingAnnotationsSize = siblingAnnotationsList.size();
					//
					// 
					double res = mparts.calcLogM2(node, parentAnnotation, childAnnotation);
										
					int legalnumber=0;
					
					for(int index_delta=0; index_delta < deltaPrimearray.length; index_delta++) {
						if(deltaarray[index_delta].getQuick(childIndex, childAnnotation)> -100000) {		
							legalnumber+=1;
						} 
					}
					
					double delta_interm[] = new double[legalnumber];

                    for(int index_delta=0; index_delta < legalnumber; index_delta++) {				
						 delta_interm[index_delta]=deltaarray[index_delta].getQuick(childIndex, childAnnotation);
					}
					
					List<Quartet<Integer, Integer, Integer, Double>> wholeinformation = new ArrayList<Quartet<Integer, Integer, Integer, Double>>();

					for (int siblingAnnotationIndex = 0; siblingAnnotationIndex < siblingAnnotationsSize; siblingAnnotationIndex += 1)
					{
						// get an annotation for the sibling node
						int siblingAnnotation = siblingAnnotationsList
								.getQuick(siblingAnnotationIndex);
						// 
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
						
						int legalnumberprime=0;
						
						for(int index_deltaprime=0; index_deltaprime < deltaPrimearray.length; index_deltaprime++) {
							if(deltaPrimearray[index_deltaprime].getQuick(siblingIndex, parentAnnotation,
									siblingAnnotation)> -100000) {		
								legalnumberprime+=1;
							} 
						}

						double delta_intermprime[] = new double[legalnumberprime];

	                    for(int index_deltaprime=0; index_deltaprime < legalnumberprime; index_deltaprime++) {				
	                    	delta_intermprime[index_deltaprime]=deltaPrimearray[index_deltaprime].getQuick(siblingIndex, parentAnnotation,
									siblingAnnotation);
						}
						
	                    for(int index_delta=0; index_delta<legalnumber; index_delta++) {
	                    	for(int index_deltaprime=0; index_deltaprime<legalnumberprime; index_deltaprime++) {
	                    		wholeinformation.add(Quartet.with(index_delta, index_deltaprime, siblingAnnotation,
	                    				res+delta_interm[index_delta]+delta_intermprime[index_deltaprime]
	                    		  + mparts.calcLogM3(node, parentAnnotation, childAnnotation, siblingAnnotation)));
	                    	}
	                    }						
					}

					List<Quartet<Integer, Integer, Integer, Double>> calcuatedtopKvalue=determineTopKvaluePrime(wholeinformation);	

					for(int index=0; index<calcuatedtopKvalue.size(); index++) {
							Quartet<Integer, Integer, Integer, Double> specificvale=calcuatedtopKvalue.get(index);
										

							IntArrayList oldValuepath = deltaPrimeAnnotationPatharray[specificvale.getValue1()].getQuick(siblingIndex,
									parentAnnotation, specificvale.getValue2());
							IntArrayList valuepath = new IntArrayList(oldValuepath.size() + 1);
							valuepath.setQuick(0, childAnnotation);
							for (int i = 0; i < oldValuepath.size(); i += 1)
								valuepath.setQuick(i + 1, oldValuepath.getQuick(i));
							valuepath.setSize(oldValuepath.size() + 1);


							IntArrayList oldValuepathIndex = deltaPrimeAnnotationPathIndexarray[specificvale.getValue1()].getQuick(siblingIndex,
									parentAnnotation, specificvale.getValue2());
							IntArrayList valuepathIndex = new IntArrayList(oldValuepathIndex.size() + 1);
							valuepathIndex.setQuick(0, specificvale.getValue0());
							for (int i = 0; i < oldValuepathIndex.size(); i += 1)
								valuepathIndex.setQuick(i + 1, oldValuepathIndex.getQuick(i));
							valuepathIndex.setSize(oldValuepathIndex.size() + 1);

							
							deltaPrimeAnnotationPatharray[index].setQuick(childIndex, parentAnnotation,
									childAnnotation, valuepath);
							deltaPrimeAnnotationPathIndexarray[index].setQuick(childIndex, parentAnnotation,
									childAnnotation, valuepathIndex);
							// set deltaPrime
							deltaPrimearray[index].setQuick(childIndex, parentAnnotation, childAnnotation, specificvale.getValue3());
					}
					
					if(calcuatedtopKvalue.size()<deltaarray.length) {
						for(int index=(calcuatedtopKvalue.size()); index<deltaarray.length; index++) {
							deltaPrimearray[index].setQuick(childIndex, parentAnnotation, childAnnotation, -120000);
							deltaPrimeAnnotationPatharray[index].setQuick(childIndex, parentAnnotation,
										childAnnotation, new IntArrayList(0));
							deltaPrimeAnnotationPathIndexarray[index].setQuick(childIndex, parentAnnotation,
										childAnnotation, new IntArrayList(0));
						}
					} 
				}
			}
		}
		// at last, calculates log betaPrime for the previous sibling if it
		// exists.
		NodeAST previousSibling = (NodeAST) node.getPreviousSibling();
		
		if (previousSibling != null)
			fillDeltaPrime_ordered(previousSibling, indexNonOrderedNode);

	}
	
   private List<Quartet<Integer, Integer, Integer, Double>> determineTopKvaluePrime(List<Quartet<Integer, Integer, Integer, Double>> wholeinfo) {
			   
		List<Quartet<Integer, Integer, Integer, Double>> retrunvale = new ArrayList<Quartet<Integer, Integer, Integer, Double>>();
		
		Collections.sort(wholeinfo, new Comparator<Quartet<Integer, Integer, Integer, Double>>() {		
		    @Override
		    public int compare(Quartet<Integer, Integer, Integer, Double> lhs, Quartet<Integer, Integer, Integer, Double> rhs) {
		        //-1 - less than, 1 - greater than, 0 - equal, all inversed for descending
		    	return Double.compare(lhs.getValue3().doubleValue(), rhs.getValue3().doubleValue()) > 0 ? 
		    			-1 : Double.compare(lhs.getValue3().doubleValue(), rhs.getValue3().doubleValue()) < 0 ? 1 : 0;		
		    }
		});
		
		int numbertoreturn=deltaarray.length;
		
		if(wholeinfo.size()<numbertoreturn)
			return wholeinfo;
		else {
			for (int index=0; index<numbertoreturn; index++)
				retrunvale.add(wholeinfo.get(index));
			
			return retrunvale;
		}
	}
}