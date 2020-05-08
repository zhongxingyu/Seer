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
package mostrare.operations.weights.tree;

import mostrare.crf.tree.CRF;
import mostrare.crf.tree.Feature1;
import mostrare.crf.tree.Feature1Inverse;
import mostrare.crf.tree.Feature1Pure;
import mostrare.crf.tree.Feature2;
import mostrare.crf.tree.Feature3;
import mostrare.crf.tree.Feature3Observation;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.operations.TrickUtil;
import mostrare.operations.parts.tree.FlexAlphaParts;
import mostrare.operations.parts.tree.FlexBetaParts;
import mostrare.operations.parts.tree.FlexMParts;
import mostrare.operations.tree.ValuesCalc;
import mostrare.tree.Corpus;
import mostrare.tree.Node;
import mostrare.tree.Tree;
import mostrare.tree.impl.NodeAST;
import mostrare.util.ConfigurationTool;
import xcrf.LearnTree;

import java.util.ArrayList;
import java.util.Map;

/**
 * Computes the gradient of the log-likelihood using the "big formula" (no approximation).
 * Computations are done with a global iterator (defined by the global constraints) and with
 * constraint infos provided by each node.
 * 
 * @author missi
 */
public class TreeGradientsCalcCompleteLog_locCons_globalIt_Impl implements GradientsCalc
{
	private CRF					crf;

	private int					annotationsNumber, features1PureNumber, features1InverseNumber, features1Number, features2Number,
			features3Number, features3NumberObservation;

	/*
	 * Used to store values for log exp computation in gradient calculus for respectively "cliques a 1", "cliques a 2" and "cliques a 3".
	 */
	private double[]			valuesNodeAnnotationByFeature;

	private double[]			valuesNodeByFeature;

	/*
	 * Objects for M, alpha and beta values. They are created just once. Only the matrix inside
	 * these objects can be resized. So, be careful, such a matrix can be larger than the matrix
	 * that fits the needs.
	 */
	private FlexMParts			mparts;

	private FlexBetaParts		betaParts;

	private FlexAlphaParts		alphaParts;

	private ValuesCalc			valuesCalc;

	private double[]			emp1Pure, emp1Inverse, emp1, emp2, emp3, emp3Observation, mod1Pure, mod1Inverse, mod1, mod2, mod3, mod3Observation;

	private double[]			partialEmp1Pure, partialEmp1Inverse, partialEmp1, partialEmp2, partialEmp3, partialEmp3Observation; 
	
	private Map<String, ArrayList<Integer>>  nodetransformmapinteger; 

	public TreeGradientsCalcCompleteLog_locCons_globalIt_Impl(CRFWithConstraintNode crf,
			FlexMParts mparts, FlexBetaParts betaParts, FlexAlphaParts alphaParts,
			ValuesCalc valuesCalc)
	{
		this.crf = crf;
		annotationsNumber = crf.getAnnotationsNumber();
		
		features1Number = crf.getFeatures1Number();
		features2Number = crf.getFeatures2Number();
		features3Number = crf.getFeatures3Number();
		features3NumberObservation = crf.getFeatures3ObservationNumber();
		features1PureNumber = crf.getFeatures1PureNumber();
		features1InverseNumber = crf.getFeatures1InverseNumber();

		this.mparts = mparts;
		this.betaParts = betaParts;
		this.alphaParts = alphaParts;
		this.valuesCalc = valuesCalc;

		valuesNodeAnnotationByFeature = new double[annotationsNumber];

		emp1Inverse = new double[features1InverseNumber];
		emp1Pure = new double[features1PureNumber];
		emp1 = new double[features1Number];
		emp2 = new double[features2Number];
		emp3 = new double[features3Number];
		emp3Observation = new double[features3NumberObservation]; 
		
		mod1Inverse = new double[features1InverseNumber];
		mod1Pure = new double[features1PureNumber];
		mod1 = new double[features1Number];
		mod2 = new double[features2Number];
		mod3 = new double[features3Number];
		mod3Observation = new double[features3NumberObservation];
		
		partialEmp1Inverse = new double[features1InverseNumber];
		partialEmp1Pure = new double[features1PureNumber];
		partialEmp1 = new double[features1Number];
		partialEmp2 = new double[features2Number];
		partialEmp3 = new double[features3Number];
		partialEmp3Observation = new double[features3NumberObservation];

		this.nodetransformmapinteger = crf.nodetransformmapinteger;
	}

	@Override
	public double calcGradients(Corpus corpus, double[] gradients1, double[] gradients1Pure, double[] gradients1Inverse, double[] gradients2,
			double[] gradients3, double[] gradients3Observation)
	{
		// Initialization
		
		for (int i = 0; i < features1PureNumber; i++)
		{
			emp1Pure[i] = 0.0;
			mod1Pure[i] = 0.0;
		}
		for (int i = 0; i < features1InverseNumber; i++)
		{
			emp1Inverse[i] = 0.0;
			mod1Inverse[i] = 0.0;
		}
		for (int i = 0; i < features1Number; i++)
		{
			emp1[i] = 0.0;
			mod1[i] = 0.0;
		}
		for (int i = 0; i < features2Number; i++)
		{
			emp2[i] = 0.0;
			mod2[i] = 0.0;
		}
		for (int i = 0; i < features3Number; i++)
		{
			emp3[i] = 0.0;
			mod3[i] = 0.0;
		}
		for (int i = 0; i < features3NumberObservation; i++)
		{
			emp3Observation[i] = 0.0;
			mod3Observation[i] = 0.0;
		}
		
		// empirical computation
		calcExpectationWithEmpiricalDistribution(corpus, emp1, emp1Pure, emp1Inverse, emp2, emp3, emp3Observation);
		

		// model-based computation
		double logLikelihood = calcExpectationWithModelDistribution(corpus, mod1, mod1Pure, mod1Inverse, mod2, mod3, mod3Observation);

		// expectation relative to empirical distribution - expectation relative
		// to model distribution
		double penalty = ConfigurationTool.getInstance().getPenalty();
		double penalty2 = penalty * penalty;
		for (int i = 0; i < gradients1.length; i += 1)
		{
			gradients1[i] = emp1[i] - mod1[i];
			// penalty
			gradients1[i] -= crf.getWeight1(i) / penalty2;
		}	
		for (int i = 0; i < gradients1Pure.length; i += 1)
		{
			gradients1Pure[i] = emp1Pure[i] - mod1Pure[i];
			// penalty
			gradients1Pure[i] -= crf.getWeight1Pure(i) / penalty2;
		}
		for (int i = 0; i < gradients1Inverse.length; i += 1)
		{
			gradients1Inverse[i] = emp1Inverse[i] - mod1Inverse[i];
			// penalty
			gradients1Inverse[i] -= crf.getWeight1Inverse(i) / penalty2;
		}
		for (int i = 0; i < gradients2.length; i += 1)
		{
			gradients2[i] = emp2[i] - mod2[i];
			// penalty
			gradients2[i] -= crf.getWeight2(i) / penalty2;
		}
		for (int i = 0; i < gradients3.length; i += 1)
		{
			gradients3[i] = emp3[i] - mod3[i];
			// penalty
			gradients3[i] -= crf.getWeight3(i) / penalty2;
		}
		for (int i = 0; i < gradients3Observation.length; i += 1)
		{
			gradients3Observation[i] = emp3Observation[i] - mod3Observation[i];
			// penalty
			gradients3Observation[i] -= crf.getWeight3Observation(i) / penalty2;
		}
		return logLikelihood;
	}

	/**
	 * Performs the empirical computation of the expectation.
	 * 
	 * @param corpus
	 *            annotated corpus
	 * @param gradients1
	 *            array where to store results of empirical computation with node features
	 * @param gradients2
	 *            array where to store results of empirical computation with edge features
	 * @param gradients3
	 *            array where to store results of empirical computation with triangle features
	 */
	private void calcExpectationWithEmpiricalDistribution(Corpus corpus, double[] gradients1, double[] gradients1pure,
			double[] gradients1Inverse, double[] gradients2, double[] gradients3, double[] gradients3Observation)
	{
		// performs the empirical computation with each kind of features
		calcExpectationWithEmpiricalDistributionCliqueA1(gradients1, crf, corpus);
		calcExpectationWithEmpiricalDistributionCliqueA1Pure(gradients1pure, crf, corpus);
		calcExpectationWithEmpiricalDistributionCliqueA1Inverse(gradients1Inverse, crf, corpus);

		calcExpectationWithEmpiricalDistributionCliqueA2(gradients2, crf, corpus);
		calcExpectationWithEmpiricalDistributionCliqueA3(gradients3, crf, corpus);
		calcExpectationWithEmpiricalDistributionCliqueA3Observation (gradients3Observation, crf, corpus);

	}

	/**
	 * Performs the empirical computation of the expectation with node features.
	 * 
	 * @param gradients
	 *            array where to store results of empirical computation with node features
	 * @param crf
	 *            CRF
	 * @param corpus
	 *            annotated corpus
	 */
	private void calcExpectationWithEmpiricalDistributionCliqueA1(double[] gradients, CRF crf,
			Corpus corpus)
	{
		int treesNumber = corpus.getTreesNumber();

		double distributionawarecoffecient=0.0;
		
		for (Feature1 feature : crf.getFeatures1())
		{
			int annotation = feature.getAnnotationTestVar();

			for (int treeIndex = 0; treeIndex < treesNumber; treeIndex += 1)
			{
				Tree tree = corpus.getTree(treeIndex);
				//treeIndex may be different from tree.getIndex
				Node[] nodes = feature.getObservableNodes(tree.getIndex());
				double res = 0.0;
				for (Node node : nodes)
				{
					if (node.getAnnotation() == annotation)
						res += feature.getValue(node, annotation);
				}
				
				if(tree.getNumberofTransform()==1)
					distributionawarecoffecient = LearnTree.getCofficentSingle();
				else distributionawarecoffecient = LearnTree.getCofficentMultiple();
				
				gradients[feature.getIndex()] += res
						*distributionawarecoffecient;
			}
		}
	}
	
	private void calcExpectationWithEmpiricalDistributionCliqueA1Inverse(double[] gradients, CRF crf,
			Corpus corpus)
	{
		int treesNumber = corpus.getTreesNumber();
		
		double distributionawarecoffecient=0.0;
		
		for (Feature1Inverse feature : crf.getFeatures1Inverse())
		{
			int annotation = feature.getAnnotationTestVar();

			for (int treeIndex = 0; treeIndex < treesNumber; treeIndex += 1)
			{
				Tree tree = corpus.getTree(treeIndex);
				//treeIndex may be different from tree.getIndex
				Node[] nodes = feature.getObservableNodes(tree.getIndex());
				double res = 0.0;
				for (Node node : nodes)
				{
					if (node.getAnnotation() == annotation)
						res += feature.getValue(node, annotation);
				}
				
				if(tree.getNumberofTransform()==1)
					distributionawarecoffecient = LearnTree.getCofficentSingle();
				else distributionawarecoffecient = LearnTree.getCofficentMultiple();
				
				gradients[feature.getIndex()] += res
						*distributionawarecoffecient;
			}
		}
	}
	
	private void calcExpectationWithEmpiricalDistributionCliqueA1Pure (double[] gradients, CRF crf,
			Corpus corpus)
	{
		int treesNumber = corpus.getTreesNumber();
		
		double distributionawarecoffecient=0.0;
		
		for (Feature1Pure feature : crf.getFeatures1Pure())
		{
			int annotation = feature.getAnnotationTestVar();

			for (int treeIndex = 0; treeIndex < treesNumber; treeIndex += 1)
			{
				Tree tree = corpus.getTree(treeIndex);

				//treeIndex may be different from tree.getIndex
				Node[] nodes = feature.getObservableNodes(tree.getIndex());
				double res = 0.0;
				for (Node node : nodes)
				{
					if (node.getAnnotation() == annotation)
						res += feature.getValue(node, annotation);
				}
				
				if(tree.getNumberofTransform()==1)
					distributionawarecoffecient = LearnTree.getCofficentSingle();
				else distributionawarecoffecient = LearnTree.getCofficentMultiple();
				
				gradients[feature.getIndex()] += res
						*distributionawarecoffecient;
			}
		}
	}

	/**
	 * Performs the empirical computation of the expectation with edge features.
	 * 
	 * @param gradients
	 *            array where to store results of empirical computation with edge features
	 * @param crf
	 *            CRF
	 * @param corpus
	 *            annotated corpus
	 */
	private void calcExpectationWithEmpiricalDistributionCliqueA2(double[] gradients, CRF crf,
			Corpus corpus)
	{
		int treesNumber = corpus.getTreesNumber();
		
		double distributionawarecoffecient=0.0;

		for (Feature2 feature : crf.getFeatures2())
		{
			int parentAnnotation = feature.getAnnotationTestParentVar();
			int childAnnotation = feature.getAnnotationTestChildVar();

			for (int treeIndex = 0; treeIndex < treesNumber; treeIndex += 1)
			{
				Tree tree = corpus.getTree(treeIndex);

				//treeIndex may be different from tree.getIndex
				Node[] nodes = feature.getObservableNodesChild(tree.getIndex());
				double res = 0.0;
				for (Node child : nodes)
				{
					Node parent = child.getParentNode();
					if (parent != null &&
							feature.getAnnotationTestValue(parent.getAnnotation(), child
									.getAnnotation()))
						res += feature.getValue(child, parentAnnotation, childAnnotation);
				}
				
				if(tree.getNumberofTransform()==1)
					distributionawarecoffecient = LearnTree.getCofficentSingle();
				else distributionawarecoffecient = LearnTree.getCofficentMultiple();
				
				gradients[feature.getIndex()] += res
						*distributionawarecoffecient;
			}
		}
	}

	/**
	 * Performs the empirical computation of the expectation with triangle features.
	 * 
	 * @param gradients
	 *            array where to store results of empirical computation with triangle features
	 * @param crf
	 *            CRF
	 * @param corpus
	 *            annotated corpus
	 */
	private void calcExpectationWithEmpiricalDistributionCliqueA3(double[] gradients, CRF crf,
			Corpus corpus)
	{
		int treesNumber = corpus.getTreesNumber();
		
		double distributionawarecoffecient=0.0;

		for (Feature3 feature : crf.getFeatures3())
		{
			int parentAnnotations = feature.getAnnotationTestParentVar();
			int leftChildAnnotation = feature.getAnnotationTestLeftChildVar();
			int rightChildAnnotation = feature.getAnnotationTestRightChildVar();

			for (int treeIndex = 0; treeIndex < treesNumber; treeIndex += 1)
			{
				Tree tree = corpus.getTree(treeIndex);

				//treeIndex may be different from tree.getIndex
				Node[] nodes = feature.getObservableNodesLeftChild(tree.getIndex());
				double res = 0.0;
				for (Node child : nodes)
				{
					if (child instanceof NodeAST)
					{
						NodeAST leftChild = (NodeAST) child;
						Node parent = child.getParentNode();
						Node rightChild = leftChild.getNextSibling();
						if (parent != null &&
								rightChild != null &&
								feature.getAnnotationTestValue(parent.getAnnotation(), leftChild
										.getAnnotation(), rightChild.getAnnotation()))
						//	for (int parentAnnotation : parentAnnotations)
								res += feature.getValue(child, parentAnnotations,
										leftChildAnnotation, rightChildAnnotation);
					}
				}
				
				if(tree.getNumberofTransform()==1)
					distributionawarecoffecient = LearnTree.getCofficentSingle();
				else distributionawarecoffecient = LearnTree.getCofficentMultiple();
				
				gradients[feature.getIndex()] += res
						*distributionawarecoffecient;
			}
		}
	}
	
	private void calcExpectationWithEmpiricalDistributionCliqueA3Observation(double[] gradients, CRF crf,
			Corpus corpus)
	{
		int treesNumber = corpus.getTreesNumber();
		
		double distributionawarecoffecient=0.0;

		for (Feature3Observation feature : crf.getFeatures3Observation())
		{
			int parentAnnotations = feature.getAnnotationTestParentVar();
			int leftChildAnnotation = feature.getAnnotationTestLeftChildVar();
			int rightChildAnnotation = feature.getAnnotationTestRightChildVar();

			for (int treeIndex = 0; treeIndex < treesNumber; treeIndex += 1)
			{
				Tree tree = corpus.getTree(treeIndex);

				//treeIndex may be different from tree.getIndex
				Node[] nodes = feature.getObservableNodesLeftChild(tree.getIndex());
				double res = 0.0;
				for (Node child : nodes)
				{
					if (child instanceof NodeAST)
					{
						NodeAST leftChild = (NodeAST) child;
						Node parent = child.getParentNode();
						Node rightChild = leftChild.getNextSibling();
						if (parent != null &&
								rightChild != null &&
								feature.getAnnotationTestValue(parent.getAnnotation(), leftChild
										.getAnnotation(), rightChild.getAnnotation()))
						//	for (int parentAnnotation : parentAnnotations)
								res += feature.getValue(child, parentAnnotations,
										leftChildAnnotation, rightChildAnnotation);
					}
				}
				
				if(tree.getNumberofTransform()==1)
					distributionawarecoffecient = LearnTree.getCofficentSingle();
				else distributionawarecoffecient = LearnTree.getCofficentMultiple();
				
				gradients[feature.getIndex()] += res
						*distributionawarecoffecient;
			}
		}
	}

	/**
	 * Performs model-based computations of the expectation.
	 * 
	 * @param corpus
	 *            corpus
	 * @param modelExpectations1
	 *            array where to store results of model-based computation with node features
	 * @param modelExpectations2
	 *            array where to store results of model-based computation with edge features
	 * @param modelExpectations3
	 *            array where to store results of model-based computation with triangle features
	 * @return the log-likelihood
	 */
	private double calcExpectationWithModelDistribution(Corpus corpus, double[] modelExpectations1, double[] modelExpectations1Pure, 
			double[] modelExpectations1Inverse, double[] modelExpectations2, double[] modelExpectations3, double[] modelExpectations3Observation)
	{
		double value = 0.0;
		
		double distributionawarecoffecient=0.0;
		
		for (int distinctTreeIndex = 0; distinctTreeIndex < corpus.getTreesNumber(); distinctTreeIndex += 1)
		{
			Tree tree = corpus.getTree(distinctTreeIndex);
			
			value += calcExpectationWithModelDistribution(corpus, distinctTreeIndex, tree, crf,
					partialEmp1, partialEmp1Pure, partialEmp1Inverse, partialEmp2, partialEmp3, partialEmp3Observation);
			
			double treeDistinctNumber = 1;
			
			if(tree.getNumberofTransform()==1)
				distributionawarecoffecient = LearnTree.getCofficentSingle();
			else distributionawarecoffecient = LearnTree.getCofficentMultiple();

			for (int i = 0; i < modelExpectations1.length; i += 1)
				modelExpectations1[i] += partialEmp1[i]*treeDistinctNumber*distributionawarecoffecient;
			for (int i = 0; i < modelExpectations1Pure.length; i += 1)
				modelExpectations1Pure[i] += partialEmp1Pure[i]*treeDistinctNumber*distributionawarecoffecient;
			for (int i = 0; i < modelExpectations1Inverse.length; i += 1)
				modelExpectations1Inverse[i] += partialEmp1Inverse[i]*treeDistinctNumber*distributionawarecoffecient;
			
			for (int i = 0; i < modelExpectations2.length; i += 1)
				modelExpectations2[i] += partialEmp2[i]*treeDistinctNumber*distributionawarecoffecient;
			for (int i = 0; i < modelExpectations3.length; i += 1)
				modelExpectations3[i] += partialEmp3[i]*treeDistinctNumber*distributionawarecoffecient;
			for (int i = 0; i < modelExpectations3Observation.length; i += 1)
				modelExpectations3Observation[i] += partialEmp3Observation[i]*treeDistinctNumber*distributionawarecoffecient;
		}
		

		// penalty
		double norm2 = 0.0;
		for (int k = 0; k < features1Number; k += 1)
			 norm2 += crf.getWeight1(k) * crf.getWeight1(k);
		for (int k = 0; k < features1PureNumber; k += 1)
			 norm2 += crf.getWeight1Pure(k) * crf.getWeight1Pure(k);
		for (int k = 0; k < features1InverseNumber; k += 1)
			 norm2 += crf.getWeight1Inverse(k) * crf.getWeight1Inverse(k);
		for (int k = 0; k < features2Number; k += 1)
			 norm2 += crf.getWeight2(k) * crf.getWeight2(k);
		for (int k = 0; k < features3Number; k += 1)
			 norm2 += crf.getWeight3(k) * crf.getWeight3(k);
		for (int k = 0; k < features3NumberObservation; k += 1)
			 norm2 += crf.getWeight3Observation(k) * crf.getWeight3Observation(k);
		
		double penalty = ConfigurationTool.getInstance().getPenalty();
				
		value = value - norm2 / (2.0 * penalty * penalty);
				
		return value;
	}

	/**
	 * Performs the model-based expectation computation for the provided <code>tree</code> and for
	 * each kind of features.
	 * 
	 * @param corpus
	 * @param distinctTreeIndex
	 * @param tree
	 * @param crf
	 * @param modelExpectationsForTree1
	 *            array where to store the results of the computation of the model-based expectation
	 *            computation for node features
	 * @param modelExpectationsForTree2
	 *            array where to store the results of the computation of the model-based expectation
	 *            computation for edge features
	 * @param modelExpectationsForTree3
	 *            array where to store the results of the computation of the model-based expectation
	 *            computation for triangle features
	 * @return a part of the log-likelihood
	 */
	private double calcExpectationWithModelDistribution(Corpus corpus, int disctinctTreeIndex,
			Tree tree, CRF crf, double[] modelExpectationsForTree1, double[] modelExpectationsForTree1Pure, 
			double[] modelExpectationsForTree1Inverse, double[] modelExpectationsForTree2, double[] modelExpectationsForTree3,
			double[] modelExpectationsForTree3Observation)
	{
		int nodesNumber = tree.getNodesNumber();
		// recalculate alphas, betas and Ms

		mparts.setNewTree(tree, true);
		mparts.fill();
		betaParts.setNewTree(tree);
		betaParts.fillLogBetas();
		alphaParts.setNewTree(tree);
		alphaParts.fillLogAlphas();
		double logZvalue = valuesCalc.logZ(betaParts, tree, crf);
		
		// valuesNode tables per feature
		valuesNodeByFeature = new double[nodesNumber];

		int treeIndex = tree.getIndex();
		// computation with node observation features
		for (int i = 0; i < features1Number; i++)
			partialEmp1[i] = 0.0;
		Feature1[] features = crf.getObservableFeatures1(treeIndex);
		for (Feature1 feature : features)
		{
			Node[] observableNodes = feature.getObservableNodes(treeIndex);
			fillValuesFeatureByNode1(feature, observableNodes, crf, modelExpectationsForTree1,
					logZvalue);
		}

		// computation with node features
		for (int i = 0; i < features1PureNumber; i++)
			partialEmp1Pure[i] = 0.0;
		Feature1Pure[] featuresPure = crf.getObservableFeatures1Pure(treeIndex);
		for (Feature1Pure feature : featuresPure)
		{
			Node[] observableNodes = feature.getObservableNodes(treeIndex);
			fillValuesFeatureByNode1Pure(feature, observableNodes, crf, modelExpectationsForTree1Pure,
					logZvalue);
		}
		
		// computation node observation inverse features
		for (int i = 0; i < features1InverseNumber; i++)
			  partialEmp1Inverse[i] = 0.0;
		Feature1Inverse[] featuresInverse= crf.getObservableFeatures1Inverse(treeIndex);
		for (Feature1Inverse feature : featuresInverse)
		{
			Node[] observableNodes = feature.getObservableNodes(treeIndex);
			fillValuesFeatureByNode1Inverse(feature, observableNodes, crf, modelExpectationsForTree1Inverse,
							logZvalue);
		}		
		
		// computation with edge features
		for (int i = 0; i < features2Number; i++)
			partialEmp2[i] = 0.0;
		Feature2[] features2 = crf.getObservableFeatures2(treeIndex);
		for (Feature2 feature : features2)
		{
			Node[] observableNodes = feature.getObservableNodesChild(treeIndex);
			fillValuesFeatureByNode2(feature, observableNodes, crf, modelExpectationsForTree2,
					logZvalue);
		}

		// computation with triangle features
		for (int i = 0; i < features3Number; i++)
			partialEmp3[i] = 0.0;
		Feature3[] features3 = crf.getObservableFeatures3(treeIndex);
		for (Feature3 feature : features3)
		{
			Node[] observableNodes = feature.getObservableNodesLeftChild(treeIndex);
			fillValuesFeatureByNode3(feature, observableNodes, crf, modelExpectationsForTree3,
					logZvalue);
		}

		// computation with triangle observation features
		for (int i = 0; i < features3NumberObservation; i++)
			 partialEmp3Observation[i] = 0.0;
		Feature3Observation[] features3Observation = crf.getObservableFeatures3Observation(treeIndex);
		for (Feature3Observation feature : features3Observation)
		{
			 Node[] observableNodes = feature.getObservableNodesLeftChild(treeIndex);
			 fillValuesFeatureByNode3Observation (feature, observableNodes, crf, modelExpectationsForTree3Observation,
							logZvalue);
		}		
		// computes the part of the log-likelihood available with the current values
//		double val = 0.0;
//		IntArrayList annTrees = corpus.getDistinctAnnForStruct(disctinctTreeIndex);
//		for (int index = 0; index < annTrees.size(); index += 1)
//		{
//			int annTreeIndex = annTrees.getQuick(index);
//			Tree annTree = corpus.getDistinctAnnotatedTree(annTreeIndex);
//			
//			double distributionawarecoffecient=0.0;
//			
//			if(annTree.getNumberofTransform()==1)
//				distributionawarecoffecient = LearnTree.getCofficentSingle();
//			else distributionawarecoffecient = LearnTree.getCofficentMultiple();
//
//			val += corpus.getDistinctAnnotatedTreeNumber(annTreeIndex) * distributionawarecoffecient * 
//					(valuesCalc.logScore(mparts, annTree, crf) - logZvalue);
//		}
		
		double distributionawarecoffecient=0.0;
		
		if(tree.getNumberofTransform()==1)
			distributionawarecoffecient = LearnTree.getCofficentSingle();
		else distributionawarecoffecient = LearnTree.getCofficentMultiple();
		
		double val = 0.0;

		val += 	distributionawarecoffecient * (valuesCalc.logScore(mparts, tree, crf) - logZvalue);
	
//		// penalty
//		double norm2 = 0.0;
//		for (int k = 0; k < features1Number; k += 1)
//			norm2 += crf.getWeight1(k) * crf.getWeight1(k);
//		for (int k = 0; k < features2Number; k += 1)
//			norm2 += crf.getWeight2(k) * crf.getWeight2(k);
//		for (int k = 0; k < features3Number; k += 1)
//			norm2 += crf.getWeight3(k) * crf.getWeight3(k);
//		double penalty = ConfigurationTool.getInstance().getPenalty();
//		//
//		val = val - norm2 / (2.0 * penalty * penalty);

		return val;
	}

	/**
	 * Performs the computation of the model-based expectation with the provided node
	 * <code>feature</code> and for the provided tree <code>nodes</code>.
	 * 
	 * @param feature
	 *            node feature
	 * @param nodes
	 * @param crf
	 * @param modelExpectations
	 * @param logZvalue
	 */
	private void fillValuesFeatureByNode1(Feature1 feature, Node[] nodes, CRF crf,
			double[] modelExpectations, double logZvalue)
	{
		int numFeature = feature.getIndex();
		int annotation = feature.getAnnotationTestVar();
		int observableNodesNumber = 0;
		double featureValue;
		
		for (Node node : nodes)
		{
//			if (!labelSet.getParentAnnotations().contains(annotation) ||
//					!node.isAnnotationAllowedForNode(annotation))
			if(!nodetransformmapinteger.get(node.getNodeType()).contains(annotation))
			{
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}
			
			if(!crf.isLogicTransformSuitable(node, annotation)) {
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}
			
			if(!feature.getCharacterTestValue(node)) {
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}
			// log alpha(n,Yn) + log beta (n,Yn) + log featureValue
			featureValue = 1.0;
			valuesNodeByFeature[observableNodesNumber++] = (featureValue == 1.0) ? alphaParts
					.getLogAlpha(node, annotation) +
					betaParts.getLogBeta(node, annotation) : alphaParts.getLogAlpha(node,
					annotation) +
					betaParts.getLogBeta(node, annotation) + StrictMath.log(featureValue);
		}

		modelExpectations[numFeature] = StrictMath.exp(TrickUtil.logSumExp(valuesNodeByFeature, 0,
				observableNodesNumber) -
				logZvalue);

	}
	
	private void fillValuesFeatureByNode1Inverse(Feature1Inverse feature, Node[] nodes, CRF crf,
			double[] modelExpectations, double logZvalue)
	{
		int numFeature = feature.getIndex();
		int annotation = feature.getAnnotationTestVar();
		int observableNodesNumber = 0;
		double featureValue;
		
		for (Node node : nodes)
		{
//			if (!labelSet.getParentAnnotations().contains(annotation) ||
//					!node.isAnnotationAllowedForNode(annotation))
			if(!nodetransformmapinteger.get(node.getNodeType()).contains(annotation))
			{
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}
			
			if(!crf.isLogicTransformSuitable(node, annotation)) {
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}
			
			if(!feature.getCharacterTestValue(node)) {
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}
			// log alpha(n,Yn) + log beta (n,Yn) + log featureValue
			featureValue = 1.0;
			valuesNodeByFeature[observableNodesNumber++] = (featureValue == 1.0) ? alphaParts
					.getLogAlpha(node, annotation) +
					betaParts.getLogBeta(node, annotation) : alphaParts.getLogAlpha(node,
					annotation) +
					betaParts.getLogBeta(node, annotation) + StrictMath.log(featureValue);
		}

		modelExpectations[numFeature] = StrictMath.exp(TrickUtil.logSumExp(valuesNodeByFeature, 0,
				observableNodesNumber) -
				logZvalue);

	}

	private void fillValuesFeatureByNode1Pure (Feature1Pure feature, Node[] nodes, CRF crf,
			double[] modelExpectations, double logZvalue)
	{
		int numFeature = feature.getIndex();
		int annotation = feature.getAnnotationTestVar();
		int observableNodesNumber = 0;
		double featureValue;
		
		for (Node node : nodes)
		{
//			if (!labelSet.getParentAnnotations().contains(annotation) ||
//					!node.isAnnotationAllowedForNode(annotation))
			if(!nodetransformmapinteger.get(node.getNodeType()).contains(annotation))
			{
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}
			
			if(!crf.isLogicTransformSuitable(node, annotation)) {
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}
			
			// log alpha(n,Yn) + log beta (n,Yn) + log featureValue
			featureValue = 1.0;
			valuesNodeByFeature[observableNodesNumber++] = (featureValue == 1.0) ? alphaParts
					.getLogAlpha(node, annotation) +
					betaParts.getLogBeta(node, annotation) : alphaParts.getLogAlpha(node,
					annotation) +
					betaParts.getLogBeta(node, annotation) + StrictMath.log(featureValue);
		}

		modelExpectations[numFeature] = StrictMath.exp(TrickUtil.logSumExp(valuesNodeByFeature, 0,
				observableNodesNumber) -
				logZvalue);

	}
	/**
	 * Performs the computation of the model-based expectation with the provided edge
	 * <code>feature</code> and for the provided tree <code>nodes</code>.
	 * 
	 * @param feature
	 *            edge feature
	 * @param nodes
	 * @param crf
	 * @param modelExpectations
	 * @param logZvalue
	 */
	private void fillValuesFeatureByNode2(Feature2 feature, Node[] nodes, CRF crf,
			double[] modelExpectations, double logZvalue)
	{
		int numFeature = feature.getIndex();
		int parentAnnotation = feature.getAnnotationTestParentVar();
		int childAnnotation = feature.getAnnotationTestChildVar();
		int observableNodesNumber = 0;
		double alphaPart, mPartParent, pVal, partialSum;
		double featureValue;

		for (Node child : nodes)
		{
			Node parent = child.getParentNode();
			if (parent == null)
			{
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}

//			if (!labelSet.getParentAnnotations().contains(parentAnnotation) ||
//					!parent.isAnnotationAllowedForNode(parentAnnotation) ||
//					!labelSet.getChildAnnotations(parentAnnotation).contains(childAnnotation) ||
//					!child.isAnnotationAllowedForEdgeChild(parentAnnotation,
//							childAnnotation))
			if(!nodetransformmapinteger.get(parent.getNodeType()).contains(parentAnnotation) ||
					!nodetransformmapinteger.get(child.getNodeType()).contains(childAnnotation))
			{
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}

			if(!crf.isLogicTransformSuitable(parent, parentAnnotation) || 
					!crf.isLogicTransformSuitable(child, childAnnotation)) {
				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				continue;
			}
			
			alphaPart = alphaParts.getLogAlpha(parent, parentAnnotation);
			mPartParent = mparts.getLogM1Quick(parent.getIndex(), parentAnnotation);
			// log alpha(n,Yn) + log M1(n,Yn)
			pVal = alphaPart + mPartParent;
			// ... + log beta'(n_i,Yn,Yn_i) + log beta''(n_i,Yn,Yn_i) + log featureValue
			partialSum = pVal +
					betaParts.getLogBetaPrime(child, parentAnnotation, childAnnotation) +
					betaParts.getLogBetaSecond(child, parentAnnotation, childAnnotation);

			featureValue = 1.0;
			valuesNodeByFeature[observableNodesNumber++] = (featureValue == 1.0) ? partialSum
					: partialSum + StrictMath.log(featureValue);
		}

		modelExpectations[numFeature] = StrictMath.exp(TrickUtil.logSumExp(valuesNodeByFeature, 0,
				observableNodesNumber) -
				logZvalue);

	}

	/**
	 * Performs the computation of the model-based expectation with the provided triangle
	 * <code>feature</code> and for the provided tree <code>nodes</code>.
	 * 
	 * @param feature
	 *            triangle feature
	 * @param nodes
	 * @param crf
	 * @param modelExpectations
	 * @param logZvalue
	 */
	private void fillValuesFeatureByNode3(Feature3 feature, Node[] nodes, CRF crf,
			double[] modelExpectations, double logZvalue)
	{
		int numFeature = feature.getIndex();
		int parentAnnotations = feature.getAnnotationTestParentVar();
		int leftChildAnnotation = feature.getAnnotationTestLeftChildVar();
		int rightChildAnnotation = feature.getAnnotationTestRightChildVar();

		int observableNodesNumber = 0;
		double alphaPart, mPartParent, pVal, partialSum;
		double betaPart, betaPartSecond, mpartChild, cVal;
		double featureValue;
		
		for (Node child : nodes)
		{

				Node parent = child.getParentNode();
				NodeAST leftChild = (NodeAST) child;
				Node rightChild = leftChild.getNextSibling();

				if (parent == null || rightChild == null) {
					valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
					continue;
				}
					
//					for (int parentAnnotation : parentAnnotations)
//					{
//						if (!labelSet.getParentAnnotations().contains(parentAnnotations) ||
//								!parent.isAnnotationAllowedForNode(
//										parentAnnotations) ||
//								!labelSet.getChildAnnotations(parentAnnotations).contains(
//										leftChildAnnotation) ||
//								!child.isAnnotationAllowedForEdgeChild(
//										parentAnnotations, leftChildAnnotation) ||
//								!labelSet.getSiblingAnnotations(parentAnnotations,
//										leftChildAnnotation).contains(rightChildAnnotation) ||
//								!rightChild.isAnnotationAllowedForEdgeChild(
//										parentAnnotations, rightChildAnnotation) ||
//								!child.isTriangleAnnotable(parentAnnotations,
//										leftChildAnnotation, rightChildAnnotation))
					   if(!nodetransformmapinteger.get(parent.getNodeType()).contains(parentAnnotations)||
							!nodetransformmapinteger.get(leftChild.getNodeType()).contains(leftChildAnnotation)||
							!nodetransformmapinteger.get(rightChild.getNodeType()).contains(rightChildAnnotation))
						{
						   valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
							continue;
						}
					   
					   if(!crf.isLogicTransformSuitable(parent, parentAnnotations) || 
								!crf.isLogicTransformSuitable(leftChild, leftChildAnnotation)||
								!crf.isLogicTransformSuitable(rightChild, rightChildAnnotation)) {
						   valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
						   continue;
						}

						alphaPart = alphaParts.getLogAlpha(parent, parentAnnotations);
						mPartParent = mparts.getLogM1Quick(parent.getIndex(), parentAnnotations);
						// log alpha(n,Yn) + log M1(n,Yn) 
						pVal = alphaPart + mPartParent;

						betaPart = betaParts.getLogBeta(child, leftChildAnnotation);
						betaPartSecond = betaParts.getLogBetaSecond(leftChild, parentAnnotations,
								leftChildAnnotation);
						mpartChild = mparts.getLogM2Quick(leftChild.getIndex(), parentAnnotations,
								leftChildAnnotation);
						// log beta(n_i,Yn_i) + log beta''(n_i,Yn,Yn_i) + log M2(n_i,Yn,Yn_i)
						cVal = betaPart + betaPartSecond + mpartChild;

						// ... + log beta'(n_j,Y_n,Yn_j) + log M3(n_i,Yn,Yn_i,Yn_j)
						partialSum = betaParts.getLogBetaPrime(rightChild, parentAnnotations,
								rightChildAnnotation) +
								mparts.getLogM3Quick(leftChild.getIndex(), parentAnnotations,
										leftChildAnnotation, rightChildAnnotation) + pVal + cVal;
						// computes the triangle feature value
						featureValue = 1.0;
						//
						valuesNodeByFeature[observableNodesNumber++] = (featureValue == 1.0) ? partialSum
								: partialSum + StrictMath.log(featureValue);
				//	}
				
//			}
//			else
//				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
		}

		modelExpectations[numFeature] = StrictMath.exp(TrickUtil.logSumExp(valuesNodeByFeature, 0,
				observableNodesNumber) -
				logZvalue);

	}
	
	/**
	 * Performs the computation of the model-based expectation with the provided triangle
	 * <code>feature</code> and for the provided tree <code>nodes</code>.
	 * 
	 * @param feature
	 *            triangle feature
	 * @param nodes
	 * @param crf
	 * @param modelExpectations
	 * @param logZvalue
	 */
	private void fillValuesFeatureByNode3Observation (Feature3Observation feature, Node[] nodes, CRF crf,
			double[] modelExpectations, double logZvalue)
	{
		int numFeature = feature.getIndex();
		int parentAnnotations = feature.getAnnotationTestParentVar();
		int leftChildAnnotation = feature.getAnnotationTestLeftChildVar();
		int rightChildAnnotation = feature.getAnnotationTestRightChildVar();

		int observableNodesNumber = 0;
		double alphaPart, mPartParent, pVal, partialSum;
		double betaPart, betaPartSecond, mpartChild, cVal;
		double featureValue;
		
		for (Node child : nodes)
		{

				Node parent = child.getParentNode();
				NodeAST leftChild = (NodeAST) child;
				Node rightChild = leftChild.getNextSibling();

				if (parent == null || rightChild == null) {
					valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
					continue;
				}
					
//					for (int parentAnnotation : parentAnnotations)
//					{
//						if (!labelSet.getParentAnnotations().contains(parentAnnotations) ||
//								!parent.isAnnotationAllowedForNode(
//										parentAnnotations) ||
//								!labelSet.getChildAnnotations(parentAnnotations).contains(
//										leftChildAnnotation) ||
//								!child.isAnnotationAllowedForEdgeChild(
//										parentAnnotations, leftChildAnnotation) ||
//								!labelSet.getSiblingAnnotations(parentAnnotations,
//										leftChildAnnotation).contains(rightChildAnnotation) ||
//								!rightChild.isAnnotationAllowedForEdgeChild(
//										parentAnnotations, rightChildAnnotation) ||
//								!child.isTriangleAnnotable(parentAnnotations,
//										leftChildAnnotation, rightChildAnnotation))
					   if(!nodetransformmapinteger.get(parent.getNodeType()).contains(parentAnnotations)||
							!nodetransformmapinteger.get(leftChild.getNodeType()).contains(leftChildAnnotation)||
							!nodetransformmapinteger.get(rightChild.getNodeType()).contains(rightChildAnnotation))
						{
						   valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
							continue;
						}
					   
					   if(!crf.isLogicTransformSuitable(parent, parentAnnotations) || 
								!crf.isLogicTransformSuitable(leftChild, leftChildAnnotation)||
								!crf.isLogicTransformSuitable(rightChild, rightChildAnnotation)) {
						   valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
						   continue;
						}
					   
					   if(!feature.whetherNodeHasSameContent(leftChild, rightChild)) {
							valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
							continue;
					    }

						alphaPart = alphaParts.getLogAlpha(parent, parentAnnotations);
						mPartParent = mparts.getLogM1Quick(parent.getIndex(), parentAnnotations);
						// log alpha(n,Yn) + log M1(n,Yn) 
						pVal = alphaPart + mPartParent;

						betaPart = betaParts.getLogBeta(child, leftChildAnnotation);
						betaPartSecond = betaParts.getLogBetaSecond(leftChild, parentAnnotations,
								leftChildAnnotation);
						mpartChild = mparts.getLogM2Quick(leftChild.getIndex(), parentAnnotations,
								leftChildAnnotation);
						// log beta(n_i,Yn_i) + log beta''(n_i,Yn,Yn_i) + log M2(n_i,Yn,Yn_i)
						cVal = betaPart + betaPartSecond + mpartChild;

						// ... + log beta'(n_j,Y_n,Yn_j) + log M3(n_i,Yn,Yn_i,Yn_j)
						partialSum = betaParts.getLogBetaPrime(rightChild, parentAnnotations,
								rightChildAnnotation) +
								mparts.getLogM3Quick(leftChild.getIndex(), parentAnnotations,
										leftChildAnnotation, rightChildAnnotation) + pVal + cVal;
						// computes the triangle feature value
						featureValue = 1.0;
						//
						valuesNodeByFeature[observableNodesNumber++] = (featureValue == 1.0) ? partialSum
								: partialSum + StrictMath.log(featureValue);
				//	}
				
//			}
//			else
//				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
		}

		modelExpectations[numFeature] = StrictMath.exp(TrickUtil.logSumExp(valuesNodeByFeature, 0,
				observableNodesNumber) -
				logZvalue);

	}
	
	private void fillValuesFeatureByNode3old(Feature3 feature, Node[] nodes, CRF crf,
			double[] modelExpectations, double logZvalue)
	{
		int numFeature = feature.getIndex();
		int parentAnnotations = feature.getAnnotationTestParentVar();
		int leftChildAnnotation = feature.getAnnotationTestLeftChildVar();
		int rightChildAnnotation = feature.getAnnotationTestRightChildVar();

		int observableNodesNumber = 0;
		int parentAnnotationCpt;
		double alphaPart, mPartParent, pVal, partialSum;
		double betaPart, betaPartSecond, mpartChild, cVal;
		double featureValue;
		
		for (Node child : nodes)
		{
//			if (child instanceof NodeAST)
//			{
				Node parent = child.getParentNode();
				NodeAST leftChild = (NodeAST) child;
				Node rightChild = leftChild.getNextSibling();

				if (parent == null || rightChild == null)
					valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
				else
				{
					parentAnnotationCpt = 0;
//					for (int parentAnnotation : parentAnnotations)
//					{
//						if (!labelSet.getParentAnnotations().contains(parentAnnotations) ||
//								!parent.isAnnotationAllowedForNode(
//										parentAnnotations) ||
//								!labelSet.getChildAnnotations(parentAnnotations).contains(
//										leftChildAnnotation) ||
//								!child.isAnnotationAllowedForEdgeChild(
//										parentAnnotations, leftChildAnnotation) ||
//								!labelSet.getSiblingAnnotations(parentAnnotations,
//										leftChildAnnotation).contains(rightChildAnnotation) ||
//								!rightChild.isAnnotationAllowedForEdgeChild(
//										parentAnnotations, rightChildAnnotation) ||
//								!child.isTriangleAnnotable(parentAnnotations,
//										leftChildAnnotation, rightChildAnnotation))
					   if(!nodetransformmapinteger.get(parent.getNodeType()).contains(parentAnnotations)||
							!nodetransformmapinteger.get(leftChild.getNodeType()).contains(leftChildAnnotation)||
							!nodetransformmapinteger.get(rightChild.getNodeType()).contains(rightChildAnnotation))
						{
							valuesNodeAnnotationByFeature[parentAnnotationCpt++] = Double.NEGATIVE_INFINITY;
							continue;
						}
					   
					   if(!crf.isLogicTransformSuitable(parent, parentAnnotations) || 
								!crf.isLogicTransformSuitable(leftChild, leftChildAnnotation)||
								!crf.isLogicTransformSuitable(rightChild, rightChildAnnotation)) {
						   valuesNodeAnnotationByFeature[parentAnnotationCpt++] = Double.NEGATIVE_INFINITY;
						   continue;
						}

						alphaPart = alphaParts.getLogAlpha(parent, parentAnnotations);
						mPartParent = mparts.getLogM1Quick(parent.getIndex(), parentAnnotations);
						// log alpha(n,Yn) + log M1(n,Yn) 
						pVal = alphaPart + mPartParent;

						betaPart = betaParts.getLogBeta(child, leftChildAnnotation);
						betaPartSecond = betaParts.getLogBetaSecond(leftChild, parentAnnotations,
								leftChildAnnotation);
						mpartChild = mparts.getLogM2Quick(leftChild.getIndex(), parentAnnotations,
								leftChildAnnotation);
						// log beta(n_i,Yn_i) + log beta''(n_i,Yn,Yn_i) + log M2(n_i,Yn,Yn_i)
						cVal = betaPart + betaPartSecond + mpartChild;

						// ... + log beta'(n_j,Y_n,Yn_j) + log M3(n_i,Yn,Yn_i,Yn_j)
						partialSum = betaParts.getLogBetaPrime(rightChild, parentAnnotations,
								rightChildAnnotation) +
								mparts.getLogM3Quick(leftChild.getIndex(), parentAnnotations,
										leftChildAnnotation, rightChildAnnotation) + pVal + cVal;
						// computes the triangle feature value
						featureValue = 1.0;
						//
						valuesNodeAnnotationByFeature[parentAnnotationCpt++] = (featureValue == 1.0) ? partialSum
								: partialSum + StrictMath.log(featureValue);
				//	}
					valuesNodeByFeature[observableNodesNumber++] = TrickUtil.logSumExp(
							valuesNodeAnnotationByFeature, 0, parentAnnotationCpt);
				}
//			}
//			else
//				valuesNodeByFeature[observableNodesNumber++] = Double.NEGATIVE_INFINITY;
		}

		modelExpectations[numFeature] = StrictMath.exp(TrickUtil.logSumExp(valuesNodeByFeature, 0,
				observableNodesNumber) -
				logZvalue);

	}
}
